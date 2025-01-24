import fnmatch
import os
import platform
import shutil
import socket
import subprocess
import time
from pathlib import Path

import requests as requests

from dcv_access_console_config_wizard.constants.constants import PRODUCT_NAME
from dcv_access_console_config_wizard.utils import logger

os_package_manager_priority_list = ["dnf", "yum", "apt"]

component_names = ["auth-server", "web-client", "handler"]

log = logger.get()

TIMEOUT_LENGTH_SECONDS = 300
TIME_BETWEEN_PORT_CHECKS = 5
NUMBER_OF_RETRIES = round(TIMEOUT_LENGTH_SECONDS / 5)

# The self-signed certificate we create doesn't contain an alt name, which causes some distributions of
# urllib3 to produce a warning to stderr. There's no way to redirect that output, so we disable it.
try:
    from urllib3.exceptions import SubjectAltNameWarning

    requests.packages.urllib3.disable_warnings(category=SubjectAltNameWarning)
except (ImportError, AttributeError):
    log.debug("Unable to disable urllib3 subject alt name warning")


def update_installed_packages():
    log.info("Updating installed packages...")
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:
        if "amzn2023" in platform.release():
            if (
                subprocess.run(
                    ["sudo", "dnf", "-y", "update"],
                    stdout=logging_file,
                    stderr=logging_file,
                ).returncode
                != 0
            ):
                log.error("Unable to update packages with dnf")
                return False
        else:
            for package_manager in os_package_manager_priority_list:
                if shutil.which(package_manager) is None:
                    log.debug(f"Could not find package manager {package_manager}")
                    continue

                log.debug(f"Updating installed packages with {package_manager}...")

                if (
                    subprocess.run(
                        ["sudo", package_manager, "-y", "update"],
                        stdout=logging_file,
                        stderr=logging_file,
                    ).returncode
                    == 0
                ):
                    log.debug("Successfully updated installed packages with " + package_manager)
                    break

                if package_manager == os_package_manager_priority_list[-1]:
                    log.error(
                        "Unable to update packages with any of these package managers: "
                        + ", ".join(os_package_manager_priority_list)
                    )
                    return False
                log.debug(
                    f"Unable to update packages with {package_manager}. Trying next package manager..."
                )
        log.info("Successfully updated installed packages.")
    return True


def install_mariadb_with_package_manager():
    log.info("Installing MariaDB...")
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:
        if "amzn2023" in platform.release():
            if (
                subprocess.run(
                    ["sudo", "dnf", "install", "-y", "mariadb105-server"],
                    stdout=logging_file,
                    stderr=logging_file,
                ).returncode
                != 0
            ):
                log.error("Unable to install mariadb105-server with dnf")
                return False
        else:
            for package_manager in os_package_manager_priority_list:
                if shutil.which(package_manager) is None:
                    log.debug(f"Could not find package manager {package_manager}")
                    continue

                log.debug(f"Installing MariaDB with {package_manager}...")

                if (
                    subprocess.run(
                        ["sudo", package_manager, "install", "-y", "mariadb-server"],
                        stdout=logging_file,
                        stderr=logging_file,
                    ).returncode
                    == 0
                ):
                    log.debug("Successfully installed mariadb-server with " + package_manager)
                    break

                if package_manager == os_package_manager_priority_list[-1]:
                    log.error(
                        "Unable to install mariadb-server with any of these package managers: "
                        + ", ".join(os_package_manager_priority_list)
                    )
                    return False
                log.debug(
                    f"Unable to install mariadb-server with {package_manager}. Trying next package manager..."
                )
        log.info("Successfully installed MariaDB.")
        log.info("Starting MariaDB service...")

        if (
            subprocess.run(
                ["sudo", "systemctl", "enable", "mariadb"], stdout=logging_file, stderr=logging_file
            ).returncode
            != 0
        ):
            log.error("Unable to enable mariadb")
            return False

        if (
            subprocess.run(
                ["sudo", "systemctl", "start", "mariadb"], stdout=logging_file
            ).returncode
            != 0
        ):
            log.error("Unable to start mariadb")
            return False

        if (
            subprocess.run(
                ["sudo", "systemctl", "status", "mariadb"], stdout=logging_file
            ).returncode
            != 0
        ):
            log.error("Unable to start mariadb")
            return False

    log.info("Successfully started MariaDB")
    return True


def setup_mariadb_database(mariadb_database_name, mariadb_username, mariadb_password):
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:

        # Check if user already exists
        proc = subprocess.run(
            [
                "sudo",
                "mysql",
                "-sNe",
                f"SELECT COUNT(*) FROM mysql.user WHERE mysql.user.User = '{mariadb_username}';",
            ],
            stdout=subprocess.PIPE,
            stderr=logging_file,
        )

        if proc.returncode != 0:
            log.error("Unable to check if user already exists")
            return False

        try:
            user_count = int(proc.stdout)
        except ValueError:
            log.error("Unable to parse user count")
            return False

        if user_count > 0:
            log.warning(
                f"User '{mariadb_username}' already exists. Continuing without creating. It may not use the password specified..."
            )
        elif (
            subprocess.run(
                [
                    "sudo",
                    "mysql",
                    "-e",
                    f"CREATE USER '{mariadb_username}'@'localhost' IDENTIFIED BY '{mariadb_password}';",
                ],
                stdout=logging_file,
                stderr=logging_file,
            ).returncode
            != 0
        ):
            log.warning(f"We weren't able to create the user {mariadb_username}.")
            return False

        if (
            subprocess.run(
                ["sudo", "mysql", "-e", f"CREATE DATABASE IF NOT EXISTS {mariadb_database_name};"],
                stdout=logging_file,
                stderr=logging_file,
            ).returncode
            != 0
        ):
            log.error("Unable to create mariadb database")
            return False

        if (
            subprocess.run(
                [
                    "sudo",
                    "mysql",
                    "-e",
                    f"GRANT ALL PRIVILEGES ON {mariadb_database_name}.* TO '{mariadb_username}'@'localhost';",
                ],
                stdout=logging_file,
                stderr=logging_file,
            ).returncode
            != 0
        ):
            log.error("Unable to grant user privileges on MariaDB database")
            return False
    log.info("Successfully created MariaDB database")
    return True


def install_nginx_with_package_manager():
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:
        log.info("Installing NGINX...")
        if "amzn2" in platform.release() and "amzn2023" not in platform.release():
            if (
                subprocess.run(
                    ["sudo", "amazon-linux-extras", "enable", "nginx1"],
                    stdout=logging_file,
                    stderr=logging_file,
                ).returncode
                != 0
            ):
                log.error("Unable to enable nginx with amazon-linux-extras")
                return False
            if (
                subprocess.run(
                    ["sudo", "amazon-linux-extras", "install", "-y", "nginx1"],
                    stdout=logging_file,
                    stderr=logging_file,
                ).returncode
                != 0
            ):
                log.error("Unable to install nginx with amazon-linux-extras")
                return False
            log.debug("Successfully installed nginx with amazon-linux-extras")

        else:
            for package_manager in os_package_manager_priority_list:
                if shutil.which(package_manager) is None:
                    log.debug(f"Could not find package manager {package_manager}")
                    continue

                returncode = subprocess.run(
                    ["sudo", package_manager, "install", "-y", "nginx"],
                    stdout=logging_file,
                    stderr=logging_file,
                ).returncode

                if returncode == 0:
                    log.debug("Successfully installed nginx with " + package_manager)
                    break
    log.info("Successfully installed NGINX")
    return True


def setup_node_dependency(os_type: str, os_version: str):
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:
        log.info("Setting up NodeJS...")
        node_link = None
        if os_type == "amzn" and os_version == "2":
            node_link = "https://rpm.nodesource.com/setup_16.x"
        elif os_type in ["rhel", "centos", "rocky"]:
            if os_version.startswith("7"):
                node_link = "https://rpm.nodesource.com/setup_16.x"
            elif os_version.startswith("8"):
                node_link = "https://rpm.nodesource.com/setup_16.x"
        elif os_type == "ubuntu":
            if os_version.startswith("20"):
                node_link = "https://deb.nodesource.com/setup_16.x"
            elif os_version.startswith("22"):
                node_link = "https://deb.nodesource.com/setup_18.x"
            elif os_version.startswith("24"):
                node_link = "https://deb.nodesource.com/setup_18.x"

        if not node_link:
            log.debug("Downloading NodeJS not required. Skipping.")
            return True

        curl_process = subprocess.run(
            ["curl", "-sL", node_link], check=True, stdout=subprocess.PIPE, stderr=logging_file
        )
        returncode = subprocess.run(
            ["sudo", "-E", "bash", "-"],
            input=curl_process.stdout,
            stdout=logging_file,
            stderr=logging_file,
        ).returncode

        if returncode == 0:
            log.debug(f"Successfully installed and downloaded NodeJS from {node_link}")
        else:
            log.warn(f"Unable to download NodeJS from {node_link}")
            return False
    log.info("Successfully setup NodeJS.")
    return True


def get_component_paths(os_type: str, component_installers_location: str):
    component_paths = {}
    for component in component_names:
        for fname in os.listdir(component_installers_location):
            if os_type == "debian" or os_type == "ubuntu":
                filename_matcher = f"*{component}*.deb"
            else:
                filename_matcher = f"*{component}*.rpm"

            if fnmatch.fnmatch(fname, filename_matcher):
                log.debug(f"Found {component} installer: {fname}")
                component_paths[component] = Path(component_installers_location) / fname
    return component_paths


def install_access_console_components(
        os_type: str, component_installers_location: str, read_existing_configs: bool
):
    component_paths = get_component_paths(os_type, component_installers_location)
    for component in component_names:
        if component_paths.get(component) is None:
            log.error(f"Unable to find {component} installer")
            return False
        if not install_sm_console_component(component_paths[component]):
            return False
        log.info(f"Successfully installed {component_paths[component]}")
    return True


def update_access_console_components(
        os_type: str, component_installers_location: str, read_existing_configs: bool
):
    completed_tasks = []
    failed_tasks = []

    try:
        component_paths = get_component_paths(os_type, component_installers_location)
    except Exception as e:
        log.error(
            f"Error while getting the component paths from {component_installers_location} Error: {e}"
        )
        failed_tasks.append(
            f"Error while getting the component paths from {component_installers_location}"
        )
        return completed_tasks, failed_tasks

    log.info(f"Upgrading components from location {component_installers_location}...")
    for component in component_names:
        if component_paths.get(component) is None:
            log.error(f"Unable to find {component} installer at {component_installers_location}")
            failed_tasks.append(
                f"Unable to find {component} installer at {component_installers_location}"
            )
            return completed_tasks, failed_tasks
        if not upgrade_access_console_component(
                os_type, component, component_paths[component], read_existing_configs
        ):
            log.warning(f"Error while upgrading {component}, continuing...")
            failed_tasks.append(f"Error while upgrading {component}")
        else:
            log.info(f"Successfully upgraded {component_paths[component]}")
            completed_tasks.append(f"\u2713 Updated {component}")
    reload_return = reload_systemctl_daemon()
    if reload_return == 0:
        log.info("Successfully reloaded the systemctl daemon")
        completed_tasks.append("\u2713 Reloaded the systemctl daemon")
    else:
        log.warning("Error while reloading the systemctl daemon")
    restart_return = restart_components()
    if restart_return:
        log.info("Successfully restarted all the components")
        completed_tasks.append("\u2713 Restarted all the components")
    else:
        log.warning("Error while restarting one or more components")
    return completed_tasks, failed_tasks


def upgrade_access_console_component(
        os_type: str, component: str, component_path: str, read_existing_configs: bool
):
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:
        log.info(f"Upgrading {component_path}...")
        if (
            subprocess.run(
                ["sudo", "systemctl", "status", f"dcv-access-console-{component}"],
                stdout=logging_file,
                stderr=logging_file,
            ).returncode
            == 4
        ):
            log.warning(f"dcv-access-console-{component} was not found, cannot upgrade it")
            return False
        if (os_type == "debian" or os_type == "ubuntu") and (component == "web-client"):
            try:
                web_client_version = (
                    subprocess.check_output(
                        [
                            "sudo",
                            "dpkg-query",
                            "--showformat='${Version}'",
                            "--show",
                            f"nice-dcv-access-console-{component}",
                        ]
                    )
                    .strip()
                    .decode("ascii")
                    .replace("'", "")
                )
                version = web_client_version.split("-")[0].split(".")
                if int(version[0]) == 2023 and int(version[1]) == 1 and int(version[2]) <= 25:
                    log.info("Uninstalling old version of the web client...")
                    if uninstall_access_console_component(component):
                        log.info("Successfully uninstalled old version of the web client")
                    else:
                        log.error("Error while uninstalling old version of the web client")
            except subprocess.CalledProcessError as e:
                log.warning(f"Could not get version of: {component}", e)
        return install_sm_console_component(
            component_path=component_path, read_existing_configs=read_existing_configs
        )


def uninstall_access_console_component(component: str):
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:
        log.info(f"Uninstalling component nice-dcv-access-console-{component}")
        for package_manager in os_package_manager_priority_list:
            if shutil.which(package_manager) is None:
                log.debug(f"Could not find package manager {package_manager}")
                continue
            returncode = subprocess.run(
                ["sudo", package_manager, "remove", "-y", f"nice-dcv-access-console-{component}"],
                stdout=logging_file,
                stderr=logging_file,
            ).returncode

            if returncode == 0:
                log.debug(f"Successfully uninstalled {component} with {package_manager}")
                return True
            else:
                log.warning(f"Unable to uninstall {component} with {package_manager}")
    return False


def reload_systemctl_daemon():
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:
        log.info("Reloading systemctl daemon")
        return_code = subprocess.run(
            ["sudo", "systemctl", "daemon-reload"],
            stdout=logging_file,
            stderr=logging_file,
        ).returncode
        log.info(f"systemctl daemon reload return code {return_code}")
        return return_code


def restart_components():
    ret_val = True
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:
        log.info("Restarting components..")
        for component in component_names:
            log.info(f"Restarting component dcv-access-console-{component}")
            if (
                subprocess.run(
                    ["sudo", "systemctl", "restart", f"dcv-access-console-{component}"],
                    stdout=logging_file,
                    stderr=logging_file,
                ).returncode
                != 0
            ):
                log.warning(f"Error while restarting dcv-access-console-{component}")
                ret_val = False
        log.info("Successfully restarted all the components")
    return ret_val


def install_sm_console_component(component_path: Path, read_existing_configs: bool):
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:
        log.info(f"Installing {component_path}...")

        for package_manager in os_package_manager_priority_list:

            if shutil.which(package_manager) is None:
                log.debug(f"Could not find package manager {package_manager}")
                continue

            if package_manager == "apt":
                env = os.environ.copy()
                env["DEBIAN_FRONTEND"] = "noninteractive"

                if read_existing_configs:
                    install_cmd = [
                        "sudo",
                        "-E",
                        package_manager,
                        "install",
                        "-y",
                        "-o",
                        "Dpkg::Options::=--force-confold",
                        "-o",
                        "Dpkg::Options::=--force-confdef",
                        str(os.path.join(".", component_path)),
                    ]
                else:
                    install_cmd = [
                        "sudo",
                        "-E",
                        package_manager,
                        "install",
                        "-y",
                        "-o",
                        "Dpkg::Options::=--force-confnew",
                        str(os.path.join(".", component_path)),
                    ]
            else:
                install_cmd = [
                    "sudo",
                    package_manager,
                    "install",
                    "-y",
                    str(os.path.join(".", component_path)),
                ]
            returncode = subprocess.run(
                install_cmd,
                stdout=logging_file,
                stderr=logging_file,
            ).returncode

            if returncode == 0:
                log.debug(f"Successfully installed {component_path} with {package_manager}")
                return True
            else:
                log.error(f"Unable to install {component_path} with {package_manager}")
    return False


def check_server(address, port):
    # Create a TCP socket
    s = socket.socket()
    log.debug(f"Attempting to connect to {address} on port {port}")
    try:
        s.connect((address, port))
        log.debug(f"Connected to {address} on port {port}")
        return True
    except socket.error as e:
        log.debug(f"Unable to connect to {address} on port {port}. Error: {e}")
        return False
    finally:
        s.close()


def start_all_access_console_components():
    log.info(f"Starting the {PRODUCT_NAME} components...")
    for component in component_names:
        if not start_service(f"dcv-access-console-{component}"):
            return False
    return True


def wait_for_components_to_start(component_port_map: dict):
    log.info("Waiting until all components are running.")
    for component in component_port_map.keys():
        port = component_port_map[component]
        if not wait_for_component_to_be_ready(component, port):
            return False

    log.info("All components successfully started")
    return True


def wait_for_component_to_be_ready(component, port):
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:
        log.info(f"Waiting for the {component} to be listening on port {port}...")
        for _ in range(NUMBER_OF_RETRIES):
            port_ready = check_server("127.0.0.1", port)
            if (
                subprocess.run(
                    ["sudo", "systemctl", "status", f"dcv-access-console-{component}"],
                    stdout=subprocess.DEVNULL,
                    stderr=logging_file,
                ).returncode
                != 0
            ):
                log.error(
                    f"Component dcv-access-console-{component} failed to start. Run 'sudo journalctl -u "
                    f"dcv-access-console-{component}' for more info."
                )
                return False

            if port_ready:
                break
            time.sleep(TIME_BETWEEN_PORT_CHECKS)
        else:
            log.debug(f"Timed out while waiting for the {component} to listen on port {port}.")
            return False

    log.info(f"The {component} is ready")
    return True


def start_service(service_name: str):
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:
        log.debug(f"Starting {service_name}...")

        subprocess.run(
            ["sudo", "systemctl", "stop", service_name],
            stdout=logging_file,
            stderr=logging_file,
        )

        if (
            subprocess.run(
                ["sudo", "systemctl", "enable", service_name],
                stdout=logging_file,
                stderr=logging_file,
            ).returncode
            != 0
        ):
            log.error(f"Unable to enable {service_name}")
            return False

        if (
            subprocess.run(
                ["sudo", "systemctl", "start", service_name],
                stdout=logging_file,
                stderr=logging_file,
            ).returncode
            != 0
        ):
            log.error(f"Unable to start {service_name}")
            return False

        if (
            subprocess.run(
                ["sudo", "systemctl", "status", service_name],
                stdout=logging_file,
                stderr=logging_file,
            ).returncode
            != 0
        ):
            log.error(f"Component did not immediately start {service_name}")
            return False
    log.info(f"Successfully started {service_name}")
    return True


def restart_nginx():
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:
        log.debug("Reloading nginx...")

        # Make sure the service is also enabled
        if (
            subprocess.run(
                ["sudo", "systemctl", "enable", "nginx"],
                stdout=logging_file,
                stderr=logging_file,
            ).returncode
            != 0
        ):
            log.error("Unable to enable nginx")
            return False

        if (
            subprocess.run(
                ["sudo", "nginx", "-s", "restart"],
                stdout=logging_file,
                stderr=logging_file,
            ).returncode
            != 0
        ):
            log.error("Unable to restart nginx")
            return False

    return True


def check_if_webclient_redirects(webclient_url: str, auth_server_url: str, keypath: Path) -> bool:
    log.info("Checking that the WebClient correctly redirects to the AuthServer")
    try:
        r = requests.head(webclient_url, verify=keypath, allow_redirects=True, timeout=10)
    except requests.exceptions.Timeout:
        log.warning("Request to WebClient timed out. Is the service and NGINX running?")
        return False

    # Keep track of whether any validation has failed, so we can try and check as much as possible before returning
    successful = True

    log.debug("Response from WebClient: " + str(r.__dict__))

    if r.history is not None and len(r.history) > 0:
        if 307 == r.history[0].status_code:
            log.debug("WebClient successfully redirected from the home page")
        else:
            log.warning(
                f"The WebClient history does not show a 307 redirect. Status code is instead {r.history[0].status_code}"
            )
            successful = False
    else:
        # The WebClient didn't redirect. Lets check if the httpd_can_network_connect bool is set properly
        log.debug("Checking if httpd_can_network_connect bool is set to true")
        with open(logger.get_verbose_logging_file(), "a+") as logging_file:
            if shutil.which("getsebool") is not None:
                getsebool_proc = subprocess.run(
                    ["getsebool", "httpd_can_network_connect"],
                    stdout=subprocess.PIPE,
                    stderr=logging_file,
                )
                if "httpd_can_network_connect --> off" in getsebool_proc.stdout.decode(
                    "unicode_escape"
                ):
                    log.warning(
                        "The WebClient did not redirect successfully to the AuthServer. The SELinux bool "
                        "'httpd_can_network_connect' is set to false. This may be causing NGINX to fail. To fix, run:\n"
                        "'sudo setsebool -P httpd_can_network_connect 1'\n"
                    )
                    return False
            log.warning(
                "Response from the WebClient shows no history, which means it did not redirect properly"
            )
        successful = False

    if r.status_code != 200 and not successful:
        log.warning(
            "WebClient did not seem to attempt to redirect at all, is the WebClient address correct?"
        )
    elif r.status_code != 200:
        log.warning(
            "WebClient redirected successfully, but the AuthServer could not complete the request. The AuthServer "
            f"request responded with status code {r.status_code}. Check the AuthServer logs for more informaton."
        )
        successful = False

    if f"{auth_server_url}/api/auth/signin" not in r.url:
        log.warning("Did not redirect to AuthServer url. Check the WebClient configuration.")
        successful = False

    if successful:
        log.info("Successfully validated that the WebClient redirects to the AuthServer")
        return True
    return False


def check_handler_is_unauthorized(handler_url: str, handler_prefix: str, keypath: Path) -> bool:
    log.info("Checking that the Handler is reachable via NGINX...")
    try:
        r = requests.head(
            f"{handler_url}/{handler_prefix}/getUserInfo",
            verify=keypath,
            allow_redirects=True,
            timeout=10,
        )
    except requests.exceptions.Timeout:
        log.warning("Request to the Handler timed out. Is the service and NGINX running?")
        return False

    log.debug("Response from Handler: " + str(r.__dict__))

    if r.status_code != 401:
        log.warning(
            f"Request to the Handler returned status {r.status_code} instead of expected 401."
        )
        return False
    return True
