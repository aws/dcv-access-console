# Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

import json
import os
import secrets
import shutil
import sys
from json import JSONDecodeError
from pathlib import Path

import click
import distro
import requests.exceptions
from click.core import ParameterSource

import dcv_access_console_config_wizard.constants.prompts.prompts_en as prompts
from dcv_access_console_config_wizard.conf.configuration_generator import (
    create_configuration_files,
    delete_temp_folder,
)
from dcv_access_console_config_wizard.constants.constants import PRODUCT_NAME
from dcv_access_console_config_wizard.utils import logger
from dcv_access_console_config_wizard.utils.broker_utils import register_new_client_with_broker
from dcv_access_console_config_wizard.utils.cert_utils import generate_certificates
from dcv_access_console_config_wizard.utils.component_utils import (
    check_handler_is_unauthorized,
    check_if_webclient_redirects,
    install_access_console_components,
    install_mariadb_with_package_manager,
    install_nginx_with_package_manager,
    setup_mariadb_database,
    setup_node_dependency,
    start_all_access_console_components,
    start_service,
    update_access_console_components,
    update_installed_packages,
    wait_for_component_to_be_ready,
    wait_for_components_to_start,
)
from dcv_access_console_config_wizard.utils.db_utils import (
    create_ddb_admin_user,
    create_mysql_admin_user,
)

HTTPS_PREFIX = "https://"

log = logger.get()

# This will contain all the information about the system the wizard is running on before the prompts are presented
environment_configuration = {}

# Arguments that should not be prompted when not doing a OneBox setup
only_onebox_args = [
    "register_with_broker",
    "install_mariadb",
    "setup_mariadb",
    "install_nginx",
    "onebox_address",
    "install_components",
    "generate_cert",
    "keystore_password",
    "cert_path",
    "cert_key_path",
    "keystore_path",
    "admin_user",
]

# Arguments that are not compatible when doing a OneBox setup
non_onebox_args = ["handler_address", "webclient_address", "authserver_address"]

# Arguments that are not compatible when we automatically register the broker
non_register_broker_args = ["broker_client_id", "broker_client_password"]

# Arguments that are not compatible when using MariaDB
dynamodb_args = [
    "dynamodb_region",
]

# Arguments that are not compatible when using DynamoDB
mariadb_args = [
    "install_mariadb",
    "setup_mariadb",
    "mariadb_host",
    "mariadb_database_name",
    "mariadb_port",
    "mariadb_username",
    "mariadb_password",
]

# Arguments that are not required when the wizard installs MariaDB
mariadb_args_not_required_when_we_install = [
    "mariadb_database_name",
]

# Arguments that are not compatible when the wizard installs MariaDB
mariadb_args_not_compatible_when_we_install = ["mariadb_host", "mariadb_port"]

# Args that are required if the certificate has already been generated
preinstalled_cert_location_args = ["cert_path", "cert_key_path", "keystore_path", "root_ca_path"]

# Args that are used to specify different cert locations/passwords for each component
multibox_cert_args = [
    "handler_keystore_password",
    "handler_keystore_path",
    "auth_server_keystore_password",
    "auth_server_keystore_path",
    "webclient_cert_path",
    "webclient_cert_key_path",
]

cookie_link_args = ["show_cookie_link", "cookie_link_target", "cookie_link_label"]
privacy_link_args = ["show_privacy_link", "privacy_link_target", "privacy_link_label"]

pam_args = ["use_pam_authentication", "normalize_userid", "pam_service_name"]

non_pam_args = ["header_name"]

connection_gateway_args = [
    "enable_connection_gateway",
    "connection_gateway_host",
    "connection_gateway_port",
]


# Populate the environment configuration map with the values needed for the prompting logic
def set_environment_configuration():
    environment_configuration["mariadb_installed"] = shutil.which("mysql") is not None
    environment_configuration["nginx_installed"] = shutil.which("nginx") is not None
    environment_configuration["broker_installed"] = (
        shutil.which("dcv-session-manager-broker") is not None
    )

    if sys.platform == "linux":
        environment_configuration["os_type"] = distro.id()
        environment_configuration["os_version"] = distro.version()
    else:
        environment_configuration["os_type"] = sys.platform

    log.debug(
        f"Detected the following environment configuration of the current machine: {environment_configuration}"
    )


set_environment_configuration()


def set_non_onebox_prompts(ctx, param, value):
    if value:
        for p in ctx.command.params:
            if p.name in non_onebox_args:
                p.required = True
    return value


# Check if we've set both the broker address and port, so we can figure out the default for the broker auth url
def update_broker_auth_url_default(ctx, param, value):
    if ctx.params.get("broker_auth_url"):
        return value
    if ctx.params.get("broker_address") and param.name == "broker_port":
        for p in ctx.command.params:
            if p.name == "broker_auth_url":
                p.default = f"{ctx.params['broker_address']}:{value}/oauth2/token"
    elif ctx.params.get("broker_port") and param.name == "broker_address":
        for p in ctx.command.params:
            if p.name == "broker_auth_url":
                p.default = f"{value}:{ctx.params['broker_port']}/oauth2/token"

    return value


def set_enable_connection_gateway(ctx, param, value):
    if value:
        for p in ctx.command.params:
            if p.name in connection_gateway_args:
                p.required = True
    else:
        for p in ctx.command.params:
            if p.name in connection_gateway_args:
                p.prompt = None
                p.required = False
    return value


def set_connection_gateway_prompts(ctx, param, value):
    if ctx.get_parameter_source(param.name) != ParameterSource.DEFAULT:
        for p in ctx.command.params:
            if p.name == "enable_connection_gateway" and p.name not in ctx.params:
                p.default = True
    return value


def enable_cookie_link_callback(ctx, param, value):
    if value:
        for p in ctx.command.params:
            if p.name in cookie_link_args:
                p.required = True
    else:
        for p in ctx.command.params:
            if p.name == "cookie_link_target":
                p.default = ""
            if p.name in cookie_link_args:
                p.required = False
                p.prompt = None

    return value


def set_cookie_link_target_callback(ctx, param, value):
    if ctx.get_parameter_source(param.name) != ParameterSource.DEFAULT:
        for p in ctx.command.params:
            if p.name == "cookie_link_label":
                p.required = True
            elif p.name == "show_cookie_link" and p.name not in ctx.params:
                p.default = True
                p.prompt = None
                p.required = False
    return value


def set_cookie_link_label_callback(ctx, param, value):
    if ctx.get_parameter_source(param.name) != ParameterSource.DEFAULT:
        for p in ctx.command.params:
            if p.name == "cookie_link_target":
                p.required = True
                p.default = None
            elif p.name == "show_cookie_link" and p.name not in ctx.params:
                p.default = True
                p.prompt = None
                p.required = False
    return value


def enable_privacy_link_callback(ctx, param, value):
    if value:
        for p in ctx.command.params:
            if p.name in privacy_link_args:
                p.required = True
    else:
        for p in ctx.command.params:
            if p.name == "privacy_link_target":
                p.default = ""
            if p.name in privacy_link_args:
                p.required = False
                p.prompt = None

    return value


def set_privacy_link_target_callback(ctx, param, value):
    if ctx.get_parameter_source(param.name) != ParameterSource.DEFAULT:
        for p in ctx.command.params:
            if p.name == "privacy_link_label":
                p.required = True
            elif p.name == "show_privacy_link" and p.name not in ctx.params:
                p.default = True
                p.prompt = None
                p.required = False
    return value


def set_privacy_link_label_callback(ctx, param, value):
    if ctx.get_parameter_source(param.name) != ParameterSource.DEFAULT:
        for p in ctx.command.params:
            if p.name == "privacy_link_target":
                p.required = True
                p.default = None
            elif p.name == "show_privacy_link" and p.name not in ctx.params:
                p.default = True
                p.prompt = None
                p.required = False
    return value


# This is called after a DynamoDB option is set, and disables prompting for any MariaDB options
def set_prompts_for_using_dynamodb(ctx, param, value):
    if ctx.get_parameter_source(param.name) == ParameterSource.DEFAULT:
        return value
    if value:
        for p in ctx.command.params:
            if p.name in mariadb_args:
                p.prompt = None
            elif p.name in dynamodb_args:
                p.required = True
    return value


# This is called after a MariaDB option is set, and disables prompting for any DynamoDB options
def set_prompts_for_using_mariadb(ctx, param, value):
    if ctx.get_parameter_source(param.name) == ParameterSource.DEFAULT:
        return value
    if value:
        for p in ctx.command.params:
            if p.name in dynamodb_args:
                p.prompt = None
            elif p.name in mariadb_args:
                p.required = True
    return value


# This is called after the generate_cert flag is set, and disables
# prompting for the args related to using a pre-created cert
def set_prompts_for_generating_cert(ctx, param, value):
    if value is None:
        return value
    if value:
        for p in ctx.command.params:
            if p.name in preinstalled_cert_location_args:
                p.required = False
                p.prompt = None
    if not value:
        for p in ctx.command.params:
            if p.name == "cert_save_location":
                p.prompt = None
    return value


# This is called after an option specifying a cert location is called, and disables prompting for generating a cert.
def deactivate_generate_cert_prompt(ctx, param, value):
    if value:
        for p in ctx.command.params:
            if p.name == "generate_cert":
                p.prompt = None
    return value


# This is called after an option related to using PAM is set, and if the user is using PAM, it marks the other
# PAM options as required. Otherwise, it disables the prompting and marks them as not required
def set_prompts_for_using_pam(ctx, param, value):
    if value:
        for p in ctx.command.params:
            if p.name == "use_pam_authentication" and p.name not in ctx.params:
                p.default = True
                p.prompt = None
                p.required = False
            elif p.name in non_pam_args:
                p.required = False
                p.prompt = None
    else:
        for p in ctx.command.params:
            if p.name in pam_args and p.name not in ctx.params:
                p.prompt = None
                p.required = False
                p.default = None
            elif p.name in non_pam_args:
                p.required = True
    return value


def set_prompts_for_using_header_based(ctx, param, value):
    if value:
        for p in ctx.command.params:
            if p.name in pam_args:
                p.required = False
                p.prompt = None
    return value


# This is called after the option to install the configuration files is set, and disables prompting for setting the
# save location.
def set_prompts_for_install_configuration_files(ctx, param, value):
    if value:
        for p in ctx.command.params:
            if p.name == "configuration_file_save_location":
                p.prompt = None
    else:
        for p in ctx.command.params:
            if p.name == "install_nginx_conf":
                p.prompt = None
    return value


# This is called after the option to set the save location is set, and disables prompting for installing the
# configuration files.
def set_prompts_for_setting_save_location(ctx, param, value):
    if value:
        for p in ctx.command.params:
            if p.name == "install_configuration_files" or p.name == "install_nginx_conf":
                p.prompt = None
    return value


# This is called after the quiet or force flag is set, and either disables prompting, or disables requirement and
# prompting for all flags.
def deactivate_prompts(ctx, param, value):
    if param.name == "quiet" and value:
        for p in ctx.command.params:
            p.prompt = None
    elif param.name == "force" and value:
        for p in ctx.command.params:
            p.prompt = None
            p.required = False
    return value


def verbose_callback(ctx, param, value):
    if value:
        logger.set_debug_level()
    return value


def _load_params_from_json(ctx, json_file, only_eager=False):
    loaded_params = []
    for p in ctx.command.params:
        if only_eager and not p.is_eager:
            continue
        if p.name == "input_json":
            continue
        elif p.name.replace("_", "-") in json_file:
            json_value = json_file[p.name.replace("_", "-")]
            log.debug(f"Loaded value of {json_value} for parameter {p.name}")
            loaded_params.append(p.name)
            p.prompt = None
            p.required = False
            p.default = json_value
            ctx.params[p.name] = json_value

            if p.name in callback_map:
                callback_map[p.name](ctx, p, json_value)
        elif (
            len(p.secondary_opts) > 0
            and p.secondary_opts[0].strip("--").replace("_", "-") in json_file
        ):
            secondary_opt = p.secondary_opts[0].strip("--").replace("_", "-")
            json_value = json_file[secondary_opt]

            if type(json_value) is not bool:
                log.warn(f"Value of {secondary_opt} is {json_value} but needs to be a bool")
                continue

            log.debug(
                f"Loaded value of {json_value} for parameter {p.name} using secondary opt {secondary_opt}"
            )
            loaded_params.append(secondary_opt)
            p.prompt = None
            p.required = False
            p.default = not json_value
            ctx.params[p.name] = not json_value

            if p.name in callback_map:
                callback_map[p.name](ctx, p, not json_value)
    return loaded_params


def update_log_location(ctx, param, value):
    if ctx.get_parameter_source(param.name) == ParameterSource.DEFAULT:
        logger.set_logging_path()
        return value
    logger.set_logging_path(Path(value))
    return value


# Parse through the input_json file, and pull each parameter out. If we find it, we can set the default of the option
# to the value and disable the prompt
def parse_json_file(ctx, param, value):
    if ctx.get_parameter_source(param.name) == ParameterSource.DEFAULT:
        return value
    if value:
        try:
            json_file = json.load(open(Path(value).absolute()))
            loaded_params = _load_params_from_json(ctx, json_file, only_eager=True)
            loaded_params += _load_params_from_json(ctx, json_file, only_eager=False)

            log.info(f"Successfully loaded parameters {', '.join(loaded_params)} from file {value}")

            return ""
        except FileNotFoundError:
            log.warning(f"Unable to find json file: {value}. Continuing...")
        except JSONDecodeError:
            log.warning(f"File {value} is not valid JSON. Continuing...")
    return value


# If this is a onebox setup, there are some options that need to be disabled/enabled
def setup_for_onebox_callback(ctx, param, value):
    if not value:
        for p in ctx.command.params:
            # We only try and install/setup MariaDB/NGINX for a OneBox setup
            if p.name in only_onebox_args:
                log.debug(f"This is not a onebox setup, so we don't need to prompt for {p.name}")
                p.prompt = None

            if p.name == "handler_port" and p.name not in ctx.params:
                p.default = 443
            elif p.name == "authserver_port" and p.name not in ctx.params:
                p.default = 443
    else:
        for p in ctx.command.params:
            if p.name == "register_with_broker" and p.name not in ctx.params:
                if not environment_configuration["broker_installed"]:
                    log.debug(
                        "We didn't detect that the broker is installed, so we won't ask to register a new client."
                    )
                    p.prompt = None
                    p.default = False
            elif p.name == "install_mariadb" and p.name not in ctx.params:
                if environment_configuration["mariadb_installed"]:
                    log.debug("MariaDB is already installed, so we don't need to ask to install it")
                    p.prompt = None
                    p.default = False
            elif p.name == "install_nginx" and p.name not in ctx.params:
                if environment_configuration["nginx_installed"]:
                    log.debug("NGINX is already installed, so we don't need to ask to install it")
                    p.prompt = None
                    p.default = False
            if p.name == "pam_service_name" and p.name not in ctx.params:
                if environment_configuration["os_type"] == "darwin":
                    p.default = "login"
                elif (
                    environment_configuration["os_type"] == "debian"
                    or environment_configuration["os_type"] == "ubuntu"
                ):
                    p.default = "common-auth"
                elif (
                    environment_configuration["os_type"] == "rhel"
                    or environment_configuration["os_type"] == "centos"
                    or environment_configuration["os_type"] == "rocky"
                    or environment_configuration["os_type"] == "amzn"
                ):
                    p.default = "system-auth"
            if p.name == "onebox_address":
                p.required = True
            if (
                p.name in non_onebox_args or p.name in multibox_cert_args
            ) and p.name not in ctx.params:
                p.prompt = None
                p.required = False
                p.default = None
    return value


def onebox_address_callback(ctx, param, value):
    if value:
        for p in ctx.command.params:
            if p.name == "is_onebox" and p.name not in ctx.params:
                # If they specified the onebox address, we can assume this is a onebox setup
                p.default = True
                p.prompt = None
                p.required = False
            if (
                p.name in non_onebox_args or p.name in multibox_cert_args
            ) and p.name not in ctx.params:
                p.prompt = None
                p.required = False
                p.default = None
    return value


def register_broker_callback(ctx, param, value):
    if value:
        if not environment_configuration["broker_installed"]:
            log.warning("Unable to detect broker installation, not registering new broker client")
            return False

        for p in ctx.command.params:
            if p.name == "broker_address" and p.name not in ctx.params:
                p.default = "https://127.0.0.1"
                p.prompt = None
            elif p.name == "broker_auth_url":
                p.prompt = None
            elif p.name == "broker_client_id":
                p.prompt = None
                p.required = False
            elif p.name == "broker_client_password":
                p.prompt = None
                p.required = False

    return value


def install_mariadb_callback(ctx, param, value):
    for p in ctx.command.params:
        if value:
            if (
                p.name in mariadb_args_not_required_when_we_install
                or p.name in mariadb_args_not_compatible_when_we_install
            ):
                p.prompt = None
            # If we are installing mariadb, we can assume the user also wants to set it up
            if p.name == "setup_mariadb" and p.name not in ctx.params:
                p.prompt = None
                p.default = True
        else:
            # The user didn't want to install mariadb, and it wasn't already installed, so we don't need to try and
            # set it up
            if p.name == "setup_mariadb" and not environment_configuration["mariadb_installed"]:
                p.prompt = None
    return value


def setup_mariadb_callback(ctx, param, value):
    if value:
        for param in ctx.command.params:
            if param.name in mariadb_args_not_required_when_we_install:
                param.prompt = None
            if param.name == "mariadb_password" and not (
                ctx.params.get("force") or ctx.params.get("quiet")
            ):
                param.prompt = prompts.MARIADB_PASSWORD_NOT_ALREADY_SETUP

    return value


def install_components_callback(ctx, param, value):
    if value:
        for p in ctx.command.params:
            if p.name == "install_configuration_files" and p.name not in ctx.params:
                p.prompt = None
                p.default = True
    if not value:
        for p in ctx.command.params:
            if p.name == "component_installers_location":
                p.prompt = None
    return value


# Save each of the callbacks used by each function so that we can call them as we parse the JSON file
callback_map = {
    "log_location": update_log_location,
    "is_onebox": setup_for_onebox_callback,
    "onebox_address": onebox_address_callback,
    "handler_address": set_non_onebox_prompts,
    "webclient_address": set_non_onebox_prompts,
    "authserver_address": set_non_onebox_prompts,
    "register_with_broker": register_broker_callback,
    "broker_address": update_broker_auth_url_default,
    "broker_port": update_broker_auth_url_default,
    "enable_connection_gateway": set_enable_connection_gateway,
    "connection_gateway_host": set_connection_gateway_prompts,
    "connection_gateway_port": set_connection_gateway_prompts,
    "show_cookie_link": enable_cookie_link_callback,
    "cookie_link_target": set_cookie_link_target_callback,
    "cookie_link_label": set_cookie_link_label_callback,
    "show_privacy_link": enable_privacy_link_callback,
    "privacy_link_target": set_privacy_link_target_callback,
    "privacy_link_label": set_privacy_link_label_callback,
    "generate_cert": set_prompts_for_generating_cert,
    "cert_path": deactivate_generate_cert_prompt,
    "cert_key_path": deactivate_generate_cert_prompt,
    "keystore_path": deactivate_generate_cert_prompt,
    "root_ca_path": deactivate_generate_cert_prompt,
    "use_pam_authentication": set_prompts_for_using_pam,
    "pam_service_name": set_prompts_for_using_pam,
    "normalize_userid": set_prompts_for_using_pam,
    "header_name": set_prompts_for_using_header_based,
    "dynamodb_region": set_prompts_for_using_dynamodb,
    "install_mariadb": install_mariadb_callback,
    "setup_mariadb": setup_mariadb_callback,
    "mariadb_host": set_prompts_for_using_mariadb,
    "mariadb_database_name": set_prompts_for_using_mariadb,
    "mariadb_port": set_prompts_for_using_mariadb,
    "mariadb_username": set_prompts_for_using_mariadb,
    "mariadb_password": set_prompts_for_using_mariadb,
    "install_components": install_components_callback,
    "install_configuration_files": set_prompts_for_install_configuration_files,
    "configuration_file_save_location": set_prompts_for_setting_save_location,
    "verbose": verbose_callback,
    "force": deactivate_prompts,
    "quiet": deactivate_prompts,
}


def exit_with_failure_message(tasks_completed, failed_task):
    if len(tasks_completed) > 0:
        msg = "The Wizard successfully: \n{}\n but failed while".format("\n".join(tasks_completed))
    else:
        msg = "The Wizard failed while"
    log.error(
        f"Wizard completed with errors. {msg} {failed_task}. You can find the full logs at {logger.get_verbose_logging_file()}"
    )
    exit(1)


@click.command()
@click.option(
    "--log-location",
    default=None,
    help="Override the default log storage location",
    required=False,
    callback=update_log_location,
)
@click.option(
    "--input-json",
    default="",
    help="Path to a JSON file containing some or all of the options",
    required=False,
    callback=parse_json_file,
)
@click.option(
    "--is-onebox/--not-onebox",
    default=False,
    is_flag=True,
    help="Whether or not all three components will be installed on the same machine",
    required=False,
    prompt=prompts.IS_ONEBOX_PROMPT,
    callback=callback_map["is_onebox"],
)
@click.option(
    "--onebox-address",
    help="The address to use if all three components are installed on the same machine. Cannot be used with "
    "--handler_address, --webclient_address, or --authserver_address.",
    required=False,
    prompt=prompts.ONEBOX_ADDRESS_PROMPT,
    callback=callback_map["onebox_address"],
)
@click.option(
    "--handler-address",
    default=None,
    help="Address of the handler, e.g. https://example.com",
    prompt=prompts.HANDLER_ADDRESS_PROMPT,
    callback=callback_map["handler_address"],
)
@click.option(
    "--handler-port",
    default=8080,
    help="Port of the handler",
    required=False,
    prompt=prompts.HANDLER_PORT_PROMPT,
)
@click.option(
    "--webclient-address",
    default=None,
    help="Address of the Web Client, e.g. https://example.com",
    prompt=prompts.WEBCLIENT_ADDRESS_PROMPT,
    callback=callback_map["webclient_address"],
)
@click.option(
    "--webclient-port",
    default=3000,
    help="Port of the Web Client",
    required=False,
    prompt=prompts.WEBCLIENT_PORT_PROMPT,
)
@click.option(
    "--authserver-address",
    default=None,
    help="Address of the Authentication Server, e.g. https://example.com",
    prompt=prompts.AUTHSERVER_ADDRESS_PROMPT,
    callback=callback_map["authserver_address"],
)
@click.option(
    "--authserver-port",
    default=9000,
    help="Port of the Authentication Server",
    required=False,
    prompt=prompts.AUTHSERVER_PORT_PROMPT,
)
@click.option(
    "--register-with-broker/--no-register-with-broker",
    is_flag=True,
    default=False,
    required=False,
    help="Attempt to register a new client with the broker on the local machine",
    prompt=prompts.REGISTER_WITH_BROKER_PROMPT,
    callback=callback_map["register_with_broker"],
)
@click.option(
    "--broker-address",
    default=None,
    help="The public address of the broker, e.g. https://example.com",
    required=True,
    prompt=prompts.BROKER_ADDRESS_PROMPT,
    callback=callback_map["broker_address"],
)
@click.option(
    "--broker-port",
    default=8443,
    help="The client-to-broker https port for the broker",
    required=False,
    prompt=prompts.BROKER_PORT_PROMPT,
    callback=callback_map["broker_port"],
)
@click.option(
    "--broker-auth-url",
    default=None,
    help="The auth URL of the broker",
    required=False,
    prompt=prompts.BROKER_AUTH_URL_PROMPT,
)
@click.option(
    "--broker-client-id",
    default=None,
    help="The id of the broker client",
    required=True,
    prompt=prompts.BROKER_CLIENT_ID_PROMPT,
)
@click.option(
    "--broker-client-password",
    default=None,
    help="The password of the broker client",
    required=True,
    prompt=prompts.BROKER_CLIENT_PASSWORD_PROMPT,
)
@click.option(
    "--enable-connection-gateway/--disable-connection-gateway",
    default=False,
    is_flag=True,
    help="Use Amazon DCV Connection Gateway to connect to Sessions",
    prompt=prompts.ENABLE_CONNECTION_GATEWAY_PROMPT,
    callback=callback_map["enable_connection_gateway"],
    required=False,
)
@click.option(
    "--connection-gateway-host",
    default=None,
    help="The Amazon DCV Connection Gateway hostname",
    prompt=prompts.CONNECTION_GATEWAY_HOST_PROMPT,
    callback=callback_map["connection_gateway_host"],
    required=False,
)
@click.option(
    "--connection-gateway-port",
    default=8443,
    help="The port the Amazon DCV Connection Gateway is listening on",
    prompt=prompts.CONNECTION_GATEWAY_PORT_PROMPT,
    callback=callback_map["connection_gateway_port"],
    required=False,
)
@click.option(
    "--auth-client-id",
    default="",
    help="The client ID for the AuthServer. Leave blank to generate a random ID",
    required=False,
)
@click.option(
    "--auth-client-secret",
    default="",
    help="The secret used by the AuthServer. Leave blank to generate a random secret",
    required=False,
)
@click.option(
    "--show-cookie-link/--hide-cookie-link",
    is_flag=True,
    required=False,
    help="Whether to show the link to the Cookie information on the login screen",
    prompt=prompts.SHOW_COOKIE_LINK_PROMPT,
    callback=callback_map["show_cookie_link"],
)
@click.option(
    "--cookie-link-target",
    default="",
    help="What to link to for the Cookie information on the login page",
    required=False,
    prompt=prompts.COOKIE_LINK_PROMPT,
    callback=callback_map["cookie_link_target"],
)
@click.option(
    "--cookie-link-label",
    default="Cookie preferences",
    help="What to title the Cookie Information link on the login page",
    required=False,
    prompt=prompts.COOKIE_LINK_TITLE_PROMPT,
    callback=callback_map["cookie_link_label"],
)
@click.option(
    "--show-privacy-link/--hide-privacy-link",
    is_flag=True,
    required=False,
    help="Whether to show the link to the Privacy information on the login screen",
    prompt=prompts.SHOW_PRIVACY_LINK_PROMPT,
    callback=callback_map["show_privacy_link"],
)
@click.option(
    "--privacy-link-target",
    default="",
    help="What to link to for the Privacy information on the login page",
    required=False,
    prompt=prompts.PRIVACY_LINK_PROMPT,
    callback=callback_map["privacy_link_target"],
)
@click.option(
    "--privacy-link-label",
    default="Privacy",
    help="What to title the Privacy link on the login page",
    required=False,
    prompt=prompts.PRIVACY_LINK_TITLE_PROMPT,
    callback=callback_map["privacy_link_label"],
)
@click.option(
    "--handler-prefix",
    help="The prefix to use for all handler requests",
    default="accessconsolehandler",
    required=False,
    prompt=None,
)
@click.option(
    "--generate-cert/--no-generate-cert",
    is_flag=True,
    help="Generate a self-signed certificate. Requires the JDK to be installed.",
    required=False,
    prompt=prompts.GENERATE_SELF_SIGNED_CERT_PROMPT,
    callback=callback_map["generate_cert"],
)
@click.option(
    "--cert-save-location",
    default="/usr/local/var/dcv-access-console/security",
    help="Where to save the self-signed certificates",
    required=False,
    prompt=prompts.SELF_SIGNED_CERT_SAVE_PATH_PROMPT,
)
@click.option(
    "--keystore-password",
    default="changeit",
    help="Password of the certificate file",
    required=False,
    prompt=prompts.CERT_STORE_PASSWORD_PROMPT,
)
@click.option(
    "--cert-path",
    default="/usr/local/var/dcv-access-console/security/server.pem",
    help="Path to the certificate file",
    required=False,
    prompt=prompts.CERT_FILE_PATH_PROMPT,
    callback=callback_map["cert_path"],
)
@click.option(
    "--cert-key-path",
    default="/usr/local/var/dcv-access-console/security/server.key",
    help="Path to the key file of the certificate",
    required=False,
    prompt=prompts.KEY_FILE_PATH_PROMPT,
    callback=callback_map["cert_key_path"],
)
@click.option(
    "--keystore-path",
    default="/usr/local/var/dcv-access-console/security/keystore.p12",
    help="Path to the keystore file",
    required=False,
    prompt=prompts.KEYSTORE_FILE_PATH_PROMPT,
    callback=callback_map["keystore_path"],
)
@click.option(
    "--handler-keystore-password",
    default="changeit",
    help="Keystore password for the Handler",
    required=False,
    prompt=prompts.HANDLER_CERT_STORE_PASSWORD_PROMPT,
)
@click.option(
    "--handler-keystore-path",
    default="/usr/local/var/dcv-access-console/security/keystore.p12",
    help="Path to the keystore file for the Handler",
    required=False,
    prompt=prompts.HANDLER_KEYSTORE_FILE_PATH_PROMPT,
)
@click.option(
    "--auth-server-keystore-password",
    default="changeit",
    help="Keystore password for the Authorization Server",
    required=False,
    prompt=prompts.AUTHSERVER_CERT_STORE_PASSWORD_PROMPT,
)
@click.option(
    "--auth-server-keystore-path",
    default="/usr/local/var/dcv-access-console/security/keystore.p12",
    help="Path to the keystore file for the Authorization Server",
    required=False,
    prompt=prompts.AUTHSERVER_KEYSTORE_FILE_PATH_PROMPT,
)
@click.option(
    "--webclient-cert-path",
    default="/usr/local/var/dcv-access-console/security/server.pem",
    help="Path to the certificate file for the Web Client",
    required=False,
    prompt=prompts.WEBCLIENT_CERT_FILE_PATH_PROMPT,
)
@click.option(
    "--webclient-cert-key-path",
    default="/usr/local/var/dcv-access-console/security/server.key",
    help="Path to the key file of the certificate for the Web Client",
    required=False,
    prompt=prompts.WEBCLIENT_KEY_FILE_PATH_PROMPT,
)
@click.option(
    "--root-ca-path",
    default="/usr/local/var/dcv-access-console/security/rootCA.pem",
    help="Path to the root CA certificate file for the Web Client",
    required=False,
    prompt=prompts.ROOT_CA_PATH_PROMPT,
    callback=callback_map["root_ca_path"],
)
@click.option(
    "--use-pam-authentication/--use-header-authentication",
    is_flag=True,
    help="Use PAM authentication. This requires dcvpamhelper to be installed on the system",
    required=False,
    prompt=prompts.USE_PAM_AUTH_PROMPT,
    callback=callback_map["use_pam_authentication"],
)
@click.option(
    "--pam-service-name",
    default="system-auth",
    help="The name of the PAM service. If /etc/pam.d/dcv is installed, you can use 'dcv'. Otherwise, for RedHat "
    "use 'system-auth' or for Ubuntu/Debian use 'common-auth'.",
    required=False,
    prompt=prompts.PAM_SERVICE_NAME_PROMPT,
    callback=callback_map["pam_service_name"],
)
@click.option(
    "--normalize-userid/--no-normalize-userid",
    is_flag=True,
    help="Normalize the User ID to lowercase. Can only be used with --use_pam_authentication.",
    required=False,
    prompt=prompts.NORMALIZE_USER_ID_PROMPT,
    callback=callback_map["normalize_userid"],
)
@click.option(
    "--header-name",
    help="The name of the header field to use to retrieve the username",
    required=False,
    default="username",
    prompt=prompts.HEADER_NAME_PROMPT,
    callback=callback_map["header_name"],
)
@click.option(
    "--dynamodb-region",
    default="",
    help="The region of the DynamoDB table. Leave blank if using MariaDB. Cannot be used with any of the following "
    "options: " + ", ".join(mariadb_args),
    required=False,
    callback=callback_map["dynamodb_region"],
    prompt=prompts.DYNAMODB_REGION_PROMPT,
)
@click.option(
    "--install-mariadb/--no-install-mariadb",
    default=False,
    is_flag=True,
    help="Installs MariaDB on this machine. This will also setup the database necessary for the Access Console unless the "
    "--setup_mariadb flag is set to false",
    required=False,
    callback=callback_map["install_mariadb"],
    prompt=prompts.INSTALL_MARIADB_PROMPT,
)
@click.option(
    "--setup-mariadb/--no-setup-mariadb",
    default=False,
    is_flag=True,
    help="Set up the MariaDB database without installing it",
    required=False,
    prompt=prompts.SETUP_MARIADB_PROMPT,
    callback=callback_map["setup_mariadb"],
)
@click.option(
    "--mariadb-host",
    default="",
    help="The address of the MariaDB server. Leave blank to retrieve from the"
    "'MYSQL_HOST' environment variable. Cannot be used with any of the following options: "
    + ", ".join(dynamodb_args),
    required=False,
    callback=callback_map["mariadb_host"],
    prompt=prompts.MARIADB_HOST_PROMPT,
)
@click.option(
    "--mariadb-database-name",
    default="dcv_access_console",
    help="The name of the MariaDB database. Cannot be used with any of the following options: "
    + ", ".join(dynamodb_args),
    required=False,
    callback=callback_map["mariadb_database_name"],
    prompt=prompts.MARIADB_DATABASE_PROMPT,
)
@click.option(
    "--mariadb-port",
    default=3306,
    help="The port of the MariaDB table. Cannot be used with any of the following options: "
    + ", ".join(dynamodb_args),
    required=False,
    callback=callback_map["mariadb_port"],
    prompt=prompts.MARIADB_PORT_PROMPT,
)
@click.option(
    "--mariadb-username",
    default="smaccessconsole",
    help="The username to use with the MariaDB database. Cannot be used with any of the following options: "
    + ", ".join(dynamodb_args),
    required=False,
    callback=callback_map["mariadb_username"],
    prompt=prompts.MARIADB_USERNAME_PROMPT,
)
@click.option(
    "--mariadb-password",
    hide_input=True,
    confirmation_prompt=True,
    default=None,
    help="The password of the MariaDB user. Cannot be used with any of the following options: "
    + ", ".join(dynamodb_args),
    required=False,
    callback=callback_map["mariadb_password"],
    prompt=prompts.MARIADB_PASSWORD_PROMPT,
)
@click.option(
    "--database-prefix",
    default="dcv_access_console_",
    help="The prefix to use for the database tables.",
    required=False,
    prompt=None,
)
@click.option(
    "--install-nginx/--no-install-nginx",
    is_flag=True,
    help="Installs NGINX on this machine",
    required=False,
    prompt=prompts.INSTALL_NGINX_PROMPT,
)
@click.option(
    "--install-components/--no-install-components",
    is_flag=True,
    help="Installs the components on this machine",
    required=False,
    prompt=prompts.INSTALL_COMPONENTS_PROMPT,
    callback=callback_map["install_components"],
)
@click.option(
    "--component-installers-location",
    default=".",
    help="The path to the folder where the installers for the three components can be found. Only used when "
    "--install_components is set. By default, looks in the current directory.",
    required=False,
    prompt=prompts.COMPONENT_INSTALLERS_LOCATION_PROMPT,
)
@click.option(
    "--install-configuration-files/--no-install-configuration-files",
    is_flag=True,
    help="Install the configuration files (except for nginx.conf) to the correct destinations and reload the components",
    required=False,
    prompt=prompts.INSTALL_CONFIG_FILES_PROMPT,
    callback=callback_map["install_configuration_files"],
)
@click.option(
    "--configuration-file-save-location",
    default="./output",
    help="The location to save the new configuration files to. Leave blank to save in the current directory.",
    show_default=False,
    required=False,
    callback=callback_map["configuration_file_save_location"],
    prompt=prompts.SAVE_CONFIG_FILES_PATH_PROMPT,
)
@click.option(
    "--read-existing-configs/--no-read-existing-configs",
    is_flag=True,
    help="Read the existing configuration files and use them as defaults",
    required=False,
    prompt=prompts.READ_EXISTING_CONFIG_FILES_PROMPT,
)
@click.option(
    "--admin-user",
    default="",
    help="If specified, will create an admin user in the appropriate database with the specified username.",
    required=False,
    prompt=prompts.ADMIN_USER_PROMPT,
)
@click.option(
    "-v",
    "--verbose",
    is_eager=True,
    help="Increase the amount of logging printed",
    required=False,
    is_flag=True,
    callback=callback_map["verbose"],
)
@click.option(
    "-f",
    "--force",
    is_flag=True,
    help="Force the wizard to run with all non-specified flags set to the default",
    required=False,
    prompt=False,
    callback=callback_map["force"],
    is_eager=True,
)
@click.option(
    "-q",
    "--quiet",
    is_flag=True,
    help="Don't prompt for missing values",
    required=False,
    prompt=False,
    callback=callback_map["quiet"],
    is_eager=True,
)
@click.pass_context
def run(
    ctx,
    input_json,
    log_location,
    is_onebox,
    onebox_address,
    handler_address,
    handler_port,
    webclient_address,
    webclient_port,
    authserver_address,
    authserver_port,
    register_with_broker,
    broker_address,
    broker_port,
    broker_auth_url,
    broker_client_id,
    broker_client_password,
    enable_connection_gateway,
    connection_gateway_host,
    connection_gateway_port,
    auth_client_secret,
    auth_client_id,
    show_cookie_link,
    cookie_link_target,
    cookie_link_label,
    show_privacy_link,
    privacy_link_target,
    privacy_link_label,
    handler_prefix,
    generate_cert,
    read_existing_configs,
    cert_path,
    cert_key_path,
    keystore_path,
    keystore_password,
    handler_keystore_password,
    handler_keystore_path,
    auth_server_keystore_password,
    auth_server_keystore_path,
    webclient_cert_path,
    webclient_cert_key_path,
    root_ca_path,
    use_pam_authentication,
    pam_service_name,
    normalize_userid,
    header_name,
    dynamodb_region,
    install_mariadb,
    setup_mariadb,
    mariadb_host,
    mariadb_database_name,
    mariadb_port,
    mariadb_username,
    mariadb_password,
    database_prefix,
    install_nginx,
    install_components,
    component_installers_location,
    install_configuration_files,
    configuration_file_save_location,
    cert_save_location,
    admin_user,
    verbose,
    force,
    quiet,
):
    click.echo("\n")
    click.echo(
        f"Welcome to the {PRODUCT_NAME} Configuration Setup Wizard".center(
            shutil.get_terminal_size((80, 20))[0], "-"
        )
    )
    click.echo("\n")

    tasks_completed = []

    using_dynamodb = False

    # Perform validation to ensure no incompatible arguments are used

    if onebox_address and not is_onebox:
        raise click.UsageError("Cannot use 'onebox-address' set 'is-onebox' to false")

    if is_onebox:
        if not onebox_address:
            raise click.UsageError("Cannot use 'is-onebox' without specifying a onebox address")
        for arg in ctx.command.params:
            if arg in non_onebox_args:
                raise click.UsageError(
                    "Cannot use the 'is-onebox' option with any of the following options: "
                    + ", ".join(mariadb_args)
                )

        if "://" not in onebox_address:
            onebox_address = HTTPS_PREFIX + onebox_address

        handler_address = onebox_address
        webclient_address = onebox_address
        authserver_address = onebox_address

    else:
        if "://" not in handler_address:
            handler_address = HTTPS_PREFIX + handler_address
        if "://" not in webclient_address:
            webclient_address = HTTPS_PREFIX + webclient_address
        if "://" not in authserver_address:
            authserver_address = HTTPS_PREFIX + authserver_address

    if "://" not in broker_address:
        broker_address = HTTPS_PREFIX + broker_address

    if "://" not in broker_auth_url:
        broker_auth_url = HTTPS_PREFIX + broker_auth_url

    if enable_connection_gateway and "://" not in connection_gateway_host:
        connection_gateway_host = HTTPS_PREFIX + connection_gateway_host

    if header_name:
        for arg in ctx.command.params:
            if arg in pam_args:
                raise click.UsageError(
                    "Cannot use 'header-name' with any of the following options: "
                    + ", ".join(pam_args)
                )

    if register_with_broker:
        for arg in ctx.command.params:
            if arg in non_register_broker_args:
                raise click.UsageError(
                    "Cannot use the 'register-with-broker' with any of the following options: "
                    + ", ".join(non_register_broker_args)
                )

    if dynamodb_region != "":
        using_dynamodb = True
        for arg in ctx.command.params:
            if arg in mariadb_args:
                raise click.UsageError(
                    "Cannot use the 'dynamodb-region' option with any of the following options: "
                    + ", ".join(mariadb_args)
                )

    if show_cookie_link and (cookie_link_target is None or cookie_link_target == ""):
        raise click.UsageError(
            "You must specify the 'cookie-link-target' if you are using 'show-cookie-link'"
        )

    if not show_cookie_link:
        cookie_link_target = ""

    if show_privacy_link and (privacy_link_target is None or privacy_link_target == ""):
        raise click.UsageError(
            "You must specify the 'privacy-link-target' if you are using 'show-privacy-link'"
        )

    if not show_privacy_link:
        privacy_link_target = ""

    if install_mariadb:
        for arg in ctx.command.params:
            if arg in mariadb_args_not_compatible_when_we_install:
                raise click.UsageError(
                    "Cannot use 'install-mariadb' option with any of the following options: "
                    + ", ".join(mariadb_args_not_compatible_when_we_install)
                )

    if generate_cert:
        for arg in ctx.command.params:
            if arg in preinstalled_cert_location_args:
                raise click.UsageError(
                    "Cannot use the 'generate-cert' option with any of the following options: "
                    + ", ".join(preinstalled_cert_location_args)
                )

    if not use_pam_authentication:
        for arg in ctx.command.params:
            if arg in pam_args:
                raise click.UsageError(f"Cannot use f{arg} without use-pam-authentication")

    # Format files and generate secrets

    if not install_configuration_files and configuration_file_save_location == "":
        configuration_file_save_location = os.getcwd()
    elif install_configuration_files:
        configuration_file_save_location = None

    if auth_client_secret is None or auth_client_secret == "":
        auth_client_secret = secrets.token_urlsafe(32)

    if auth_client_id is None or auth_client_id == "":
        auth_client_id = secrets.token_urlsafe(32)

    if configuration_file_save_location:
        configuration_file_save_location = Path(configuration_file_save_location)

    # Go through and complete each requested task

    if register_with_broker:
        result = register_new_client_with_broker()
        if result is None:
            exit_with_failure_message(tasks_completed, "registering new client with the broker")
        else:
            broker_client_id, broker_client_password = result
            tasks_completed.append("\u2713 Registered new client with the broker")

    if install_mariadb:
        if not update_installed_packages():
            log.info("Failed to update installed packages. Attempting to install MariaDB anyways.")
        else:
            tasks_completed.append("\u2713 Updated installed packages using package manager.")
        if not install_mariadb_with_package_manager():
            exit_with_failure_message(tasks_completed, "installing MariaDB")
        else:
            tasks_completed.append("\u2713 Installed MariaDB")

    if setup_mariadb:
        if not setup_mariadb_database(mariadb_database_name, mariadb_username, mariadb_password):
            exit_with_failure_message(tasks_completed, "setting up MariaDB.")
        else:
            tasks_completed.append("\u2713 Set up the MariaDB database")

    if install_nginx:
        if not install_nginx_with_package_manager():
            exit_with_failure_message(tasks_completed, "installing NGINX")
        else:
            tasks_completed.append("\u2713 Installed NGINX")

    if install_components:
        if not setup_node_dependency(
            environment_configuration["os_type"], environment_configuration["os_version"]
        ):
            log.warning(
                "Failed to download the required version of NodeJS. Attempting to install components anyway..."
            )
        if not install_access_console_components(
            environment_configuration["os_type"],
            component_installers_location,
            read_existing_configs,
        ):
            exit_with_failure_message(tasks_completed, f"installing {PRODUCT_NAME} Components")
        else:
            tasks_completed.append(f"\u2713 Installed {PRODUCT_NAME} Components")

    cert_save_location = Path(cert_save_location)

    if generate_cert:
        cert_path = f"{cert_save_location}/server.pem"
        cert_key_path = f"{cert_save_location}/server.key"
        keystore_path = f"{cert_save_location}/keystore.p12"
        root_ca_path = f"{cert_save_location}/rootCA.pem"

        if not shutil.which("keytool"):
            log.warning(
                "Unable to generate self-signed certificates. Keytool is not installed. Ensure that the AuthServer or the JDK is installed"
            )
        else:
            if not generate_certificates(
                cert_save_location,
                password=keystore_password,
                webclient_address=webclient_address,
            ):
                exit_with_failure_message(tasks_completed, "generating self-signed certificates")
            else:
                tasks_completed.append("\u2713 Generated self-signed certificate")

    cert_key_path = Path(cert_key_path)
    cert_path = Path(cert_path)
    keystore_path = Path(keystore_path)
    root_ca_path = Path(root_ca_path)

    if is_onebox:
        # Unless the component specific cert path is specified, use the default
        handler_keystore_path = (
            keystore_path if handler_keystore_path is None else handler_keystore_path
        )
        handler_keystore_password = (
            keystore_password if handler_keystore_password is None else handler_keystore_password
        )
        auth_server_keystore_path = (
            keystore_path if auth_server_keystore_path is None else auth_server_keystore_password
        )
        auth_server_keystore_password = (
            keystore_password
            if auth_server_keystore_password is None
            else auth_server_keystore_password
        )
        webclient_cert_key_path = (
            cert_key_path if webclient_cert_key_path is None else webclient_cert_key_path
        )
        webclient_cert_path = cert_path if webclient_cert_path is None else webclient_cert_path

    try:
        if not create_configuration_files(
            is_onebox=is_onebox,
            authserver_address=authserver_address,
            authserver_port=authserver_port,
            broker_address=broker_address,
            broker_port=broker_port,
            broker_auth_url=broker_auth_url,
            broker_client_id=broker_client_id,
            broker_client_password=broker_client_password,
            webclient_address=webclient_address,
            webclient_port=webclient_port,
            handler_address=handler_address,
            handler_port=handler_port,
            enable_connection_gateway=enable_connection_gateway,
            connection_gateway_host=connection_gateway_host,
            connection_gateway_port=connection_gateway_port,
            auth_client_secret=auth_client_secret,
            auth_client_id=auth_client_id,
            cookie_link_target=cookie_link_target,
            cookie_link_label=cookie_link_label,
            privacy_link_target=privacy_link_target,
            privacy_link_label=privacy_link_label,
            handler_prefix=handler_prefix,
            webclient_cert_path=webclient_cert_path,
            webclient_key_path=webclient_cert_key_path,
            root_ca_path=root_ca_path,
            handler_keystore_path=handler_keystore_path,
            handler_keystore_password=handler_keystore_password,
            auth_server_keystore_path=auth_server_keystore_path,
            auth_server_keystore_password=auth_server_keystore_password,
            use_pam_authentication=use_pam_authentication,
            pam_service_name=pam_service_name,
            normalize_userid=normalize_userid,
            header_name=header_name,
            using_dynamodb=using_dynamodb,
            dynamodb_region=dynamodb_region,
            mariadb_address=mariadb_host,
            mariadb_port=mariadb_port,
            mariadb_username=mariadb_username,
            mariadb_password=mariadb_password,
            mariadb_tablename=mariadb_database_name,
            database_prefix=database_prefix,
            read_existing_configs=read_existing_configs,
            save_location=configuration_file_save_location,
        ):
            exit_with_failure_message(tasks_completed, "creating configuration files")

        tasks_completed.append("\u2713 Generated configuration files")
        if configuration_file_save_location:
            tasks_completed.append(
                f"\u2713 Saved the configuration files to {configuration_file_save_location}"
            )
        else:
            tasks_completed.append("\u2713 Installed the configuration files")
    except PermissionError:
        exit_with_failure_message(
            tasks_completed,
            "creating configuration files. Received a permissions error",
        )
    finally:
        delete_temp_folder()

    if install_configuration_files:
        if not start_all_access_console_components():
            exit_with_failure_message(
                tasks_completed, f"starting/reloading {PRODUCT_NAME} Components"
            )
        else:
            tasks_completed.append(f"\u2713 Started/Reloaded {PRODUCT_NAME} Components")

        log.info("Starting nginx...")
        if not start_service("nginx"):
            exit_with_failure_message(tasks_completed, "starting/reloading NGINX")
        else:
            tasks_completed.append("\u2713 Started/Reloaded NGINX")

        if not wait_for_components_to_start(
            {
                "auth-server": authserver_port,
                "handler": handler_port,
                "web-client": webclient_port,
            }
        ):
            exit_with_failure_message(tasks_completed, "waiting for components to start")

    if admin_user and admin_user != "":
        if using_dynamodb:
            if not create_ddb_admin_user(dynamodb_region, f"{database_prefix}User", admin_user):
                exit_with_failure_message(tasks_completed, "inserting the admin user into DynamoDB")
            else:
                tasks_completed.append("\u2713 Set the admin user in DynamoDB")
        else:
            if not create_mysql_admin_user(
                mariadb_database_name,
                f"{database_prefix}User",
                admin_user,
                mariadb_username,
                mariadb_password,
            ):
                exit_with_failure_message(tasks_completed, "inserting the admin user into MariaDB")
            else:
                tasks_completed.append("\u2713 Set the admin user in MariaDB")

        if install_configuration_files:
            if not start_service("dcv-access-console-handler"):
                exit_with_failure_message(
                    tasks_completed,
                    f"starting/reloading {PRODUCT_NAME} Handler after inserting admin user '{admin_user}'",
                )
            if not wait_for_component_to_be_ready("handler", handler_port):
                exit_with_failure_message(
                    tasks_completed,
                    f"waiting for {PRODUCT_NAME} Handler to start after inserting admin user '{admin_user}'",
                )
            tasks_completed.append(f"\u2713 Restarted the {PRODUCT_NAME} Handler")

    if install_configuration_files:
        try:
            if not check_if_webclient_redirects(webclient_address, authserver_address, cert_path):
                exit_with_failure_message(
                    tasks_completed,
                    "checking if the WebClient successfully redirects to the AuthServer",
                )
            else:
                tasks_completed.append(
                    "\u2713 Ensured WebClient is running and successfully redirects to the AuthServer"
                )
            if not check_handler_is_unauthorized(handler_address, handler_prefix, cert_path):
                exit_with_failure_message(
                    tasks_completed,
                    "checking that NGINX redirects to the Handler, and that it returns an unauthorized response",
                )
            else:
                tasks_completed.append(
                    "\u2713 Ensured NGINX redirects requests to the Handler and that it returns an unauthorized response"
                )
        except requests.exceptions.RequestException as e:
            log.debug(
                "Requests to Handler/WebClient/AuthServer failed for some reason. Full stack trace: "
            )
            log.debug(e)
            log.warn(
                "Failed to verify that Handler/WebClient/AuthServer are set up properly while verifying the certificate. Trying "
                "again without verifing..."
            )
            try:
                if not check_if_webclient_redirects(webclient_address, authserver_address, False):
                    exit_with_failure_message(
                        tasks_completed,
                        "checking if the WebClient successfully redirects to the AuthServer",
                    )
                else:
                    tasks_completed.append(
                        "\u2713 Ensured WebClient is running and successfully redirects to the AuthServer, but was unable to "
                        "verify the certificate."
                    )
                if not check_handler_is_unauthorized(handler_address, handler_prefix, False):
                    exit_with_failure_message(
                        tasks_completed,
                        "checking that NGINX redirects to the Handler, and that it returns an unauthorized response",
                    )
                else:
                    tasks_completed.append(
                        "\u2713 Ensured NGINX redirects requests to the Handler and that it returns an unauthorized response, but "
                        "was unable to verify the certificate"
                    )
                log.warn(
                    "Successfully verified that the Handler, Web Client and Authorization Server are running, but we weren't "
                    "able to use the certificate to verify the requests. Note that if this "
                    "is an AL2 system, it may have TLS issues that prevent us from verifying the certificate."
                )
            except requests.exceptions.RequestException as e:
                log.debug(
                    "Requests to Handler/WebClient/AuthServer failed for some reason. Full stack trace: "
                )
                log.debug(e)

                exit_with_failure_message(
                    tasks_completed, "verifying the components are communicating properly"
                )

    log.info(
        "Wizard has completed successfully. The Wizard: \n{}\n You can find the full logs at {}".format(
            "\n".join(tasks_completed), logger.get_verbose_logging_file()
        )
    )

    if install_configuration_files:
        log.info(f"You should be able to view the {PRODUCT_NAME} console at {webclient_address}")


@click.command(context_settings=dict(allow_extra_args=True))
@click.pass_context
@click.option(
    "--component-installers-location",
    default=".",
    help="The path to the folder where the installers for the three components can be found. "
    "By default, looks in the current directory.",
    required=False,
    prompt=prompts.COMPONENT_INSTALLERS_LOCATION_PROMPT,
)
def update(ctx, component_installers_location, read_existing_configs):
    completed_tasks, failed_tasks = update_access_console_components(
        environment_configuration["os_type"], component_installers_location, read_existing_configs
    )
    if failed_tasks:
        exit_with_failure_message(completed_tasks, failed_tasks)
    log.info(
        "Wizard has completed successfully. The Wizard: \n{}\n You can find the full logs at {}".format(
            "\n".join(completed_tasks), logger.get_verbose_logging_file()
        )
    )


if __name__ == "__main__":
    run()
