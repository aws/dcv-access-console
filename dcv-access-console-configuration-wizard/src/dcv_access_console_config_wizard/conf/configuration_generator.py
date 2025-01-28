# Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

import os
import re
import secrets
import shutil
import subprocess
from datetime import datetime
from pathlib import Path
from typing import Any

from dcv_access_console_config_wizard.utils import logger
from dcv_access_console_config_wizard.utils.file_utils import (
    move_file,
    move_file_with_reference_permissions,
    read_plaintext_file_to_lines,
    set_line,
    write_lines_to_file,
)

log = logger.get()

AUTH_SERVER_CONFIG_NAME = "access-console-auth-server.properties"
AUTH_SERVER_SECRETS_CONFIG_NAME = "access-console-auth-server-secrets.properties"

HANDLER_CONFIG_NAME = "access-console-handler.properties"
HANDLER_SECRETS_CONFIG_NAME = "access-console-handler-secrets.properties"

WEBCLIENT_CONFIG_NAME = "access-console-web-client.properties"
WEBCLIENT_SECRETS_CONFIG_NAME = "access-console-web-client-secrets.properties"

NGINX_CONFIG_NAME = "dcv-access-console.conf"

AUTH_SERVER_DEFAULT_CONFIG_PATH = (
    Path("/etc/dcv-access-console-auth-server") / AUTH_SERVER_CONFIG_NAME
)
AUTH_SERVER_SECRETS_DEFAULT_CONFIG_PATH = (
    Path("/etc/dcv-access-console-auth-server") / AUTH_SERVER_SECRETS_CONFIG_NAME
)

HANDLER_DEFAULT_CONFIG_PATH = Path("/etc/dcv-access-console-handler") / HANDLER_CONFIG_NAME
HANDLER_SECRETS_DEFAULT_CONFIG_PATH = (
    Path("/etc/dcv-access-console-handler") / HANDLER_SECRETS_CONFIG_NAME
)

WEBCLIENT_DEFAULT_CONFIG_PATH = Path("/etc/dcv-access-console-web-client") / WEBCLIENT_CONFIG_NAME
WEBCLIENT_SECRETS_DEFAULT_CONFIG_PATH = (
    Path("/etc/dcv-access-console-web-client") / WEBCLIENT_SECRETS_CONFIG_NAME
)

NGINX_BASE_CONFIG_PATH = Path("/etc/nginx/")
NGINX_CONF_D_PATH = Path("conf.d") / NGINX_CONFIG_NAME
NGINX_DEFAULT_CONFIG_PATH = NGINX_BASE_CONFIG_PATH / NGINX_CONF_D_PATH

ASSETS_PATH = Path(__file__).parent.parent / "assets"
DEFAULT_CONFIG_PATH = ASSETS_PATH / "default_configs"

AUTH_SERVER_LOCAL_CONFIG_PATH = DEFAULT_CONFIG_PATH / AUTH_SERVER_CONFIG_NAME
AUTH_SERVER_SECRETS_LOCAL_CONFIG_PATH = DEFAULT_CONFIG_PATH / AUTH_SERVER_SECRETS_CONFIG_NAME

HANDLER_LOCAL_CONFIG_PATH = DEFAULT_CONFIG_PATH / HANDLER_CONFIG_NAME
HANDLER_SECRETS_LOCAL_CONFIG_PATH = DEFAULT_CONFIG_PATH / HANDLER_SECRETS_CONFIG_NAME

WEBCLIENT_LOCAL_CONFIG_PATH = DEFAULT_CONFIG_PATH / WEBCLIENT_CONFIG_NAME
WEBCLIENT_SECRETS_LOCAL_CONFIG_PATH = DEFAULT_CONFIG_PATH / WEBCLIENT_SECRETS_CONFIG_NAME

BACKUP_LOCATION = Path("./output/dcv_ac_config_backups/")

TEMP_PATH = Path("./.tmp")


def modify_auth_server_config(
    data: Any,
    webclient_address: str,
    authserver_address: str,
    authserver_port: str,
    use_pam_authentication: bool,
    pam_service_name: str,
    normalize_userid: bool,
    header_name: str,
    cookie_link_target: str,
    cookie_link_label: str,
    privacy_link_target: str,
    privacy_link_label: str,
) -> Any:
    log.debug("Creating authentication configuration data...")
    header_auth_lines = {"authentication-header-name": header_name}

    pam_auth_lines = {
        "pam-service-name": pam_service_name,
        "pam-normalize-userid-enabled": normalize_userid,
    }

    pam_comment_keys = [
        "pam-helper-path",
        "enable-pam-debug",
        "pam-process-timeout",
        "pam-normalize-userid-command",
    ]

    replacement_lines = {
        "server-port": authserver_port,
        "redirect-uris": f"{webclient_address}/api/auth/callback/dcv-access-console-auth-server",
        "post-logout-redirect-uris": webclient_address,
        "authorization-server-hostname": authserver_address,
        "login-page-cookie-link-label": cookie_link_label,
        "login-page-cookie-link-target": cookie_link_target,
        "login-page-privacy-link-label": privacy_link_label,
        "login-page-privacy-link-target": privacy_link_target,
    }

    if use_pam_authentication:
        replacement_lines.update(pam_auth_lines)
        comment_lines = header_auth_lines
    else:
        replacement_lines.update(header_auth_lines)
        comment_lines = pam_auth_lines

    for line in data:
        key = line.split("=")[0].replace(" ", "").replace("#", "")
        if key in replacement_lines:
            set_line(data, line, key, replacement_lines[key])
        if key in comment_lines:
            set_line(data, line, f"# {key}", comment_lines[key])
        if key in pam_comment_keys and not use_pam_authentication:
            set_line(data, line, f"# {key}", line.split("=")[1])

    return data


def modify_auth_server_secrets_config(
    is_onebox: bool,
    data: Any,
    auth_server_keystore_path: Path,
    auth_server_keystore_password: str,
    auth_client_id: str,
    auth_client_secret: str,
) -> Any:
    ssl_lines = {
        "ssl-key-store": auth_server_keystore_path,
        "ssl-key-store-password": auth_server_keystore_password,
        "ssl-key-store-type": "PKCS12",
        "ssl-enabled": "true",
    }

    comment_lines = {}

    replacement_lines = {
        "auth-server-client-id": auth_client_id,
        "auth-server-client-secret": auth_client_secret,
    }

    # If this is a onebox setup, we can disable SSL
    if is_onebox:
        comment_lines.update(ssl_lines)
    else:
        replacement_lines.update(ssl_lines)

    for line in data:
        key = line.split("=")[0].replace(" ", "").replace("#", "")
        if key in replacement_lines:
            set_line(data, line, key, replacement_lines[key])
        if key in comment_lines:
            set_line(data, line, f"# {key}", comment_lines[key])

    return data


def modify_handler_config(
    data: Any,
    handler_port: str,
    webclient_address: str,
    authserver_address: str,
    broker_address: str,
    broker_port: str,
    broker_auth_url: str,
    enable_connection_gateway: bool,
    connection_gateway_host: str,
    connection_gateway_port: str,
    handler_prefix: str,
    uses_dynamodb: bool,
    dynamodb_region: str,
    mariadb_address: str,
    mariadb_port: str,
    mariadb_tablename: str,
    database_prefix: str,
) -> Any:
    dynamodb_lines = {
        "dynamodb-region": dynamodb_region,
    }

    mysql_lines = {
        "jdbc-connection-url": f"jdbc:mariadb://{mariadb_address}:{mariadb_port}/{mariadb_tablename}",
        "jpa-db-platform": "org.hibernate.dialect.MariaDBDialect",
    }

    replacement_lines = {
        "web-client-url": f"{webclient_address}",
        "client-to-broker-connector-url": f"{broker_address}:{broker_port}",
        "client-to-broker-connector-auth-url": broker_auth_url,
        "table-name-prefix": database_prefix,
        "server-port": handler_port,
        "request-prefix": f"/{handler_prefix}",
        "jwt-issuer-uri": f"{authserver_address}",
        "enable-connection-gateway": str(enable_connection_gateway).lower(),
        "connection-gateway-host": connection_gateway_host or "gatewayhostname",
        "connection-gateway-port": connection_gateway_port,
    }

    if uses_dynamodb:
        replacement_lines.update(dynamodb_lines)
        replacement_lines["persistence-db"] = "dynamodb"
        comment_lines = mysql_lines
    else:
        replacement_lines.update(mysql_lines)
        replacement_lines["persistence-db"] = "mysql"
        comment_lines = dynamodb_lines

    for line in data:
        key = line.split("=")[0].replace(" ", "").replace("#", "")
        if key in replacement_lines:
            set_line(data, line, key, replacement_lines[key])
        if key in comment_lines:
            set_line(data, line, f"# {key}", comment_lines[key])

    return data


def modify_handler_secrets_config(
    is_onebox: bool,
    data: Any,
    broker_client_id: str,
    broker_client_password: str,
    mariadb_username: str,
    mariadb_password: str,
    handler_keystore_path: Path,
    handler_keystore_password: str,
) -> Any:
    ssl_lines = {
        "ssl-key-store": handler_keystore_path,
        "ssl-key-store-password": handler_keystore_password,
        "ssl-key-store-type": "PKCS12",
        "ssl-enabled": "true",
    }

    comment_lines = {}

    replacement_lines = {
        "broker-client-id": broker_client_id,
        "broker-client-password": broker_client_password,
        "jdbc-user": mariadb_username,
        "jdbc-password": mariadb_password,
    }

    # If this is a onebox setup, we can disable SSL
    if is_onebox:
        comment_lines.update(ssl_lines)
    else:
        replacement_lines.update(ssl_lines)

    for line in data:
        key = line.split("=")[0].replace(" ", "").replace("#", "")
        if key in replacement_lines:
            set_line(data, line, key, replacement_lines[key])

    return data


def modify_webclient_config(
    data: Any,
    webclient_address: str,
    authserver_address: str,
    handler_address: str,
    handler_prefix: str,
    root_ca_path: Path,
) -> Any:

    replacement_lines = {
        "handler-base-url": handler_address,
        "handler-api-prefix": f"/{handler_prefix}",
        "auth-server-well-known-uri": f"{authserver_address}/.well-known/oauth-authorization-server",
        "web-client-url": webclient_address,
        "extra-ca-certs": f"{root_ca_path}",
    }

    for line in data:
        key = line.split("=")[0].replace(" ", "").replace("#", "")
        if key in replacement_lines:
            set_line(data, line, key, replacement_lines[key])
    return data


def modify_webclient_secrets_config(
    data: Any,
    auth_client_id: str,
    auth_client_secret: str,
) -> Any:

    replacement_lines = {
        "auth-server-client-id": auth_client_id,
        "auth-server-client-secret": auth_client_secret,
        "cookie-secret": secrets.token_urlsafe(32),
    }

    for line in data:
        key = line.split("=")[0].replace(" ", "").replace("#", "")
        if key in replacement_lines:
            set_line(data, line, key, replacement_lines[key])
    return data


def modify_nginx_config(
    is_onebox: bool,
    webclient_address: str,
    webclient_port: str,
    authserver_address: str,
    authserver_port: str,
    handler_address: str,
    handler_port: str,
    webclient_cert_path: Path,
    webclient_key_path: Path,
    handler_prefix: str,
) -> Any:

    if is_onebox:
        authserver_address = "http://127.0.0.1"
        handler_address = "http://127.0.0.1"

    if len(webclient_address.split("//")) > 1:
        webclient_address = webclient_address.split("//")[1]

    nginx_conf = f"""server {{
    listen  443 ssl;

    server_tokens off;

    server_name {webclient_address};

    ssl_certificate {webclient_cert_path};
    ssl_certificate_key {webclient_key_path};

    ssl_session_cache shared:SSL:1m;
    ssl_session_timeout 5m;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    location @access_console_webclient {{
        proxy_pass  http://127.0.0.1:{webclient_port}$uri;
        proxy_intercept_errors on;
        recursive_error_pages on;
        error_page 404 = @access_console_auth_server;
    }}

    location @access_console_auth_server {{
        proxy_pass  {authserver_address}:{authserver_port}$uri;
        proxy_intercept_errors on;
        recursive_error_pages on;
    }}

    location ~* (/_next/static/|/.well-known/security.txt) {{
        try_files $uri @access_console_webclient;
    }}

    location ~* (/.well-known|/oauth2|/login$|/login-background.svg|/eula.html) {{
        proxy_pass  {authserver_address}:{authserver_port};
    }}

    location ~* (/{handler_prefix}) {{
       proxy_pass   {handler_address}:{handler_port};
    }}

    location / {{
        proxy_pass   http://127.0.0.1:{webclient_port};
    }}
}}
"""
    return nginx_conf


def get_temp_backup_format(filename: Path) -> Path:
    return Path(f"{str(filename)}.backup")


# Make Config backups, but don't move them yet. We need to copy the permissions to the new files.
def make_config_backups(backup_path: Path) -> bool:
    log.info("Making backups of the old config files...")
    backup_path.mkdir(parents=True, exist_ok=True)
    for file in [
        AUTH_SERVER_DEFAULT_CONFIG_PATH,
        AUTH_SERVER_SECRETS_DEFAULT_CONFIG_PATH,
        HANDLER_DEFAULT_CONFIG_PATH,
        HANDLER_SECRETS_DEFAULT_CONFIG_PATH,
        WEBCLIENT_DEFAULT_CONFIG_PATH,
        WEBCLIENT_SECRETS_DEFAULT_CONFIG_PATH,
        NGINX_DEFAULT_CONFIG_PATH,
    ]:
        with open(logger.get_verbose_logging_file(), "a+") as logging_file:
            if subprocess.run(["sudo", "test", "-f", file]).returncode != 0:
                if file == NGINX_DEFAULT_CONFIG_PATH:
                    log.debug(
                        "Did not find an existing NGINX config. The wizard probably hasn't created one yet."
                    )
                    continue
                else:
                    log.warning(
                        f"Could not locate configuration file {file}. The components may not have been installed correctly."
                    )
                    return False

            if file == NGINX_DEFAULT_CONFIG_PATH:
                # We don't use this backup to copy permissions, so we can move it straight to the destination
                destination = backup_path / NGINX_CONFIG_NAME
            else:
                destination = get_temp_backup_format(file)

            if (
                subprocess.run(
                    ["sudo", "cp", "-p", file, destination],
                    stdout=logging_file,
                    stderr=logging_file,
                ).returncode
                != 0
            ):
                log.warning(f"Unable to take backup of configuration file {file}")
                return False
            else:
                log.debug(
                    f"Successfully made backup of {file} and moved it to {get_temp_backup_format(file)}"
                )
    log.info("Finished making backups")
    return True


def get_nginx_save_location() -> Path:
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:
        ps = subprocess.run(["sudo", "nginx", "-t"], stdout=logging_file, stderr=subprocess.PIPE)
        log.debug(f"NGINX returned {ps.stderr} for 'nginx -t'")
        pattern = r"(?:\/[a-zA-Z]*)*\/nginx\.conf"
        matches = re.findall(pattern, str(ps.stderr))
        if len(matches) == 0:
            log.warning(
                f"Unable to find nginx save location, using default of {NGINX_DEFAULT_CONFIG_PATH}"
            )
            return Path(NGINX_DEFAULT_CONFIG_PATH)
        nginx_save_location = Path(matches[0][:-10]) / NGINX_CONF_D_PATH
        log.debug(f"Using {nginx_save_location} as the NGINX save location")
        return nginx_save_location


# Move the file from the ./.tmp folder into the proper location, and copy the permissions of the old config.
# Then move the backup into the backup folder
def install_configuration_file(
    installation_location: Path, config_file_name: str, backup_path: Path
) -> bool:
    backup_file = get_temp_backup_format(installation_location)

    if not move_file_with_reference_permissions(
        TEMP_PATH / config_file_name, installation_location, backup_file
    ):
        return False

    return move_file(backup_file, backup_path / config_file_name)


def delete_temp_folder():
    if os.path.isdir(TEMP_PATH):
        shutil.rmtree(TEMP_PATH)


def create_configuration_files(
    is_onebox: bool,
    authserver_address: str,
    authserver_port: str,
    webclient_address: str,
    webclient_port: str,
    handler_address: str,
    handler_port: str,
    broker_address: str,
    broker_port: str,
    broker_auth_url: str,
    broker_client_id: str,
    broker_client_password: str,
    enable_connection_gateway: bool,
    connection_gateway_host: str,
    connection_gateway_port: str,
    auth_client_id: str,
    auth_client_secret: str,
    cookie_link_target: str,
    cookie_link_label: str,
    privacy_link_target: str,
    privacy_link_label: str,
    handler_prefix: str,
    webclient_cert_path: Path,
    webclient_key_path: Path,
    root_ca_path: Path,
    handler_keystore_path: Path,
    handler_keystore_password: str,
    auth_server_keystore_path: Path,
    auth_server_keystore_password: str,
    use_pam_authentication: bool,
    pam_service_name: str,
    normalize_userid: bool,
    header_name: str,
    using_dynamodb: bool,
    dynamodb_region: str,
    mariadb_address: str,
    mariadb_port: str,
    mariadb_username: str,
    mariadb_password: str,
    mariadb_tablename: str,
    database_prefix: str,
    read_existing_configs: bool,
    save_location: Path = None,
) -> bool:
    log.info("Creating configuration files...")
    # Set SQL Address to retrieve from MySQL host if not specified
    if mariadb_address == "":
        mariadb_address = "${MYSQL_HOST:localhost}"

    backup_path = BACKUP_LOCATION / f"backup-{datetime.now().strftime('%m.%d.%Y.%H.%M.%S')}"

    if not save_location:
        if not make_config_backups(backup_path):
            return False
        TEMP_PATH.mkdir(exist_ok=True, parents=True)

    # Create AuthServer config file
    log.debug("Loading default authentication server config data...")
    auth_server_config_data = read_plaintext_file_to_lines(AUTH_SERVER_LOCAL_CONFIG_PATH)
    if read_existing_configs:
        log.debug("Loading existing authentication server config data...")
        try:
            auth_server_config_data = read_plaintext_file_to_lines(AUTH_SERVER_DEFAULT_CONFIG_PATH)
        except FileNotFoundError:
            log.warning(
                f"Unable to find AuthServer file at {AUTH_SERVER_DEFAULT_CONFIG_PATH}. "
                f"Using {AUTH_SERVER_LOCAL_CONFIG_PATH} instead"
            )

    auth_server_config_data = modify_auth_server_config(
        data=auth_server_config_data,
        webclient_address=webclient_address,
        authserver_address=authserver_address,
        authserver_port=authserver_port,
        use_pam_authentication=use_pam_authentication,
        pam_service_name=pam_service_name,
        normalize_userid=normalize_userid,
        header_name=header_name,
        cookie_link_target=cookie_link_target,
        cookie_link_label=cookie_link_label,
        privacy_link_target=privacy_link_target,
        privacy_link_label=privacy_link_label,
    )
    write_lines_to_file(
        auth_server_config_data,
        (
            save_location / AUTH_SERVER_CONFIG_NAME
            if save_location
            else TEMP_PATH / AUTH_SERVER_CONFIG_NAME
        ),
    )

    if not save_location:
        if not install_configuration_file(
            AUTH_SERVER_DEFAULT_CONFIG_PATH, AUTH_SERVER_CONFIG_NAME, backup_path
        ):
            return False

    # Create AuthServer Secrets config file
    auth_server_secrets_config_data = read_plaintext_file_to_lines(
        AUTH_SERVER_SECRETS_LOCAL_CONFIG_PATH
    )
    if read_existing_configs:
        try:
            auth_server_secrets_config_data = read_plaintext_file_to_lines(
                AUTH_SERVER_SECRETS_DEFAULT_CONFIG_PATH
            )
        except FileNotFoundError:
            log.warning(
                f"Unable to find AuthServer file at {AUTH_SERVER_SECRETS_DEFAULT_CONFIG_PATH}. "
                f"Using {AUTH_SERVER_SECRETS_LOCAL_CONFIG_PATH} instead"
            )

    auth_server_secrets_config_data = modify_auth_server_secrets_config(
        is_onebox=is_onebox,
        data=auth_server_secrets_config_data,
        auth_server_keystore_path=auth_server_keystore_path,
        auth_server_keystore_password=auth_server_keystore_password,
        auth_client_id=auth_client_id,
        auth_client_secret=auth_client_secret,
    )
    write_lines_to_file(
        auth_server_secrets_config_data,
        (
            save_location / AUTH_SERVER_SECRETS_CONFIG_NAME
            if save_location
            else TEMP_PATH / AUTH_SERVER_SECRETS_CONFIG_NAME
        ),
    )

    if not save_location:
        if not install_configuration_file(
            AUTH_SERVER_SECRETS_DEFAULT_CONFIG_PATH, AUTH_SERVER_SECRETS_CONFIG_NAME, backup_path
        ):
            return False

    # Create Handler config file
    handler_config_data = read_plaintext_file_to_lines(HANDLER_LOCAL_CONFIG_PATH)
    if read_existing_configs:
        try:
            handler_config_data = read_plaintext_file_to_lines(HANDLER_DEFAULT_CONFIG_PATH)
        except FileNotFoundError:
            log.warning(
                f"Unable to find Handler file at {HANDLER_DEFAULT_CONFIG_PATH}. "
                f"Using {HANDLER_LOCAL_CONFIG_PATH} instead"
            )

    handler_config_data = modify_handler_config(
        data=handler_config_data,
        handler_port=handler_port,
        webclient_address=webclient_address,
        authserver_address=authserver_address,
        broker_address=broker_address,
        broker_port=broker_port,
        broker_auth_url=broker_auth_url,
        enable_connection_gateway=enable_connection_gateway,
        connection_gateway_host=connection_gateway_host,
        connection_gateway_port=connection_gateway_port,
        handler_prefix=handler_prefix,
        uses_dynamodb=using_dynamodb,
        dynamodb_region=dynamodb_region,
        mariadb_address=mariadb_address,
        mariadb_port=mariadb_port,
        mariadb_tablename=mariadb_tablename,
        database_prefix=database_prefix,
    )

    write_lines_to_file(
        handler_config_data,
        save_location / HANDLER_CONFIG_NAME if save_location else TEMP_PATH / HANDLER_CONFIG_NAME,
    )

    if not save_location:
        if not install_configuration_file(
            HANDLER_DEFAULT_CONFIG_PATH, HANDLER_CONFIG_NAME, backup_path
        ):
            return False

    # Create Handler Secrets config file
    handler_secrets_config_data = read_plaintext_file_to_lines(HANDLER_SECRETS_LOCAL_CONFIG_PATH)
    if read_existing_configs:
        try:
            handler_secrets_config_data = read_plaintext_file_to_lines(
                HANDLER_SECRETS_DEFAULT_CONFIG_PATH
            )
        except FileNotFoundError:
            log.warning(
                f"Unable to find Handler Secrets file at {HANDLER_SECRETS_DEFAULT_CONFIG_PATH}. "
                f"Using {HANDLER_SECRETS_LOCAL_CONFIG_PATH} instead"
            )

    handler_secrets_config_data = modify_handler_secrets_config(
        is_onebox=is_onebox,
        data=handler_secrets_config_data,
        broker_client_id=broker_client_id,
        broker_client_password=broker_client_password,
        mariadb_username=mariadb_username,
        mariadb_password=mariadb_password,
        handler_keystore_path=handler_keystore_path,
        handler_keystore_password=handler_keystore_password,
    )

    write_lines_to_file(
        handler_secrets_config_data,
        (
            save_location / HANDLER_SECRETS_CONFIG_NAME
            if save_location
            else TEMP_PATH / HANDLER_SECRETS_CONFIG_NAME
        ),
    )

    if not save_location:
        if not install_configuration_file(
            HANDLER_SECRETS_DEFAULT_CONFIG_PATH, HANDLER_SECRETS_CONFIG_NAME, backup_path
        ):
            return False

    # Create WebClient Config
    webclient_config_data = read_plaintext_file_to_lines(WEBCLIENT_LOCAL_CONFIG_PATH)
    if read_existing_configs:
        try:
            webclient_config_data = read_plaintext_file_to_lines(WEBCLIENT_DEFAULT_CONFIG_PATH)
        except FileNotFoundError:
            log.warning(
                f"Unable to find WebClient file at {WEBCLIENT_DEFAULT_CONFIG_PATH}. "
                f"Using {WEBCLIENT_LOCAL_CONFIG_PATH} instead"
            )
    webclient_config_data = modify_webclient_config(
        data=webclient_config_data,
        webclient_address=webclient_address,
        authserver_address=authserver_address,
        handler_address=handler_address,
        handler_prefix=handler_prefix,
        root_ca_path=root_ca_path,
    )

    write_lines_to_file(
        webclient_config_data,
        (
            save_location / WEBCLIENT_CONFIG_NAME
            if save_location
            else TEMP_PATH / WEBCLIENT_CONFIG_NAME
        ),
    )

    if not save_location:
        if not install_configuration_file(
            WEBCLIENT_DEFAULT_CONFIG_PATH, WEBCLIENT_CONFIG_NAME, backup_path
        ):
            return False

    # Create WebClient Secrets Config
    webclient_secrets_config_data = read_plaintext_file_to_lines(
        WEBCLIENT_SECRETS_LOCAL_CONFIG_PATH
    )
    if read_existing_configs:
        try:
            webclient_secrets_config_data = read_plaintext_file_to_lines(
                WEBCLIENT_SECRETS_DEFAULT_CONFIG_PATH
            )
        except FileNotFoundError:
            log.warning(
                f"Unable to find WebClient file at {WEBCLIENT_SECRETS_DEFAULT_CONFIG_PATH}. "
                f"Using {WEBCLIENT_SECRETS_LOCAL_CONFIG_PATH} instead"
            )

    webclient_secrets_config_data = modify_webclient_secrets_config(
        data=webclient_secrets_config_data,
        auth_client_id=auth_client_id,
        auth_client_secret=auth_client_secret,
    )

    write_lines_to_file(
        webclient_secrets_config_data,
        (
            save_location / WEBCLIENT_SECRETS_CONFIG_NAME
            if save_location
            else TEMP_PATH / WEBCLIENT_SECRETS_CONFIG_NAME
        ),
    )

    if not save_location:
        if not install_configuration_file(
            WEBCLIENT_SECRETS_DEFAULT_CONFIG_PATH, WEBCLIENT_SECRETS_CONFIG_NAME, backup_path
        ):
            return False

    nginx_config_data = modify_nginx_config(
        is_onebox=is_onebox,
        webclient_address=webclient_address,
        webclient_port=webclient_port,
        authserver_address=authserver_address,
        authserver_port=authserver_port,
        handler_address=handler_address,
        handler_port=handler_port,
        webclient_cert_path=webclient_cert_path,
        webclient_key_path=webclient_key_path,
        handler_prefix=handler_prefix,
    )

    write_lines_to_file(
        nginx_config_data,
        (save_location / NGINX_CONF_D_PATH if save_location else TEMP_PATH / NGINX_CONFIG_NAME),
    )

    if not save_location:
        if not move_file_with_reference_permissions(
            TEMP_PATH / NGINX_CONFIG_NAME,
            NGINX_DEFAULT_CONFIG_PATH,
            NGINX_BASE_CONFIG_PATH / "nginx.conf",
        ):
            return False

    if not save_location:
        log.info(
            f"Finished creating configuration files and installed them in the correct paths. Backups of the "
            f"old configuration files have been placed in {backup_path}"
        )
    else:
        log.info(
            f"Finished creating configuration files and saved them in {save_location}. Place the generated "
            f"dcv-access-console.conf file in /etc/ngnix/conf.d/ folder: {save_location / NGINX_CONF_D_PATH}\n"
        )
    return True
