import os

from dcv_access_console_config_wizard.constants.constants import PRODUCT_NAME

IS_ONEBOX_PROMPT = "\nWill all of the DCV Access Manager components be installed on the same host?"

ONEBOX_ADDRESS_PROMPT = f"\nWhat is the address that the {PRODUCT_NAME} website should be accessible at (e.g. https://example.com)?"

HANDLER_ADDRESS_PROMPT = "\nWhat is the address of the Handler (e.g. https://example.com)?"

HANDLER_PORT_PROMPT = "\nWhat port should the Handler run on? Default:"

WEBCLIENT_ADDRESS_PROMPT = (
    "\nWhat is the address of the Web Client component (e.g. https://example.com)?"
)

WEBCLIENT_PORT_PROMPT = "\nWhat port should the Web Client component run on? Default:"

AUTHSERVER_ADDRESS_PROMPT = (
    "\nWhat is the address of the Authentication Server (e.g. https://example.com)?"
)

AUTHSERVER_PORT_PROMPT = "\nWhat port should the Authentication Server run on? Default:"

REGISTER_WITH_BROKER_PROMPT = (
    "\nThe broker appears to be installed on this host. Do you want to automatically register a new client? "
    "It will use 'nice-dcv-access-console' as the client name and may prompt you for the root user password"
)

BROKER_ADDRESS_PROMPT = "\nWhat is the address of the Broker (e.g. https://example.com)?"

BROKER_PORT_PROMPT = "\nWhat is the client-to-broker https port of the Broker? Default:"

BROKER_AUTH_URL_PROMPT = "\nWhat is the Authentication URL for the Broker?"

BROKER_CLIENT_ID_PROMPT = "\nWhat is the Client ID for the Broker?"

BROKER_CLIENT_PASSWORD_PROMPT = "\nWhat is the password of the Broker Client?"

ENABLE_CONNECTION_GATEWAY_PROMPT = (
    "\nDo you want to use Amazon DCV Connection Gateway to connect to the Amazon DCV Servers?"
)

CONNECTION_GATEWAY_HOST_PROMPT = "\nWhat is the hostname of the Amazon DCV Connection Gateway?"

CONNECTION_GATEWAY_PORT_PROMPT = (
    "\nWhat is the port the Amazon DCV Connection Gateway is listening on?"
)

SHOW_COOKIE_LINK_PROMPT = "\nDo you want to show a link to a Cookie disclaimer on the login page?"

COOKIE_LINK_PROMPT = (
    "\nWhat URL do you want to use for the cookies preferences hyperlink on the login page? Leave "
    "blank to hide the link."
)

COOKIE_LINK_TITLE_PROMPT = (
    "\nWhat text do you want to use as the link label for the cookie preferences hyperlink?"
)
SHOW_PRIVACY_LINK_PROMPT = "\nDo you want to show a link to a Privacy disclaimer on the login page?"

PRIVACY_LINK_PROMPT = (
    "\nWhat URL do you want to use for the privacy preferences hyperlink on the login page? Leave "
    "blank to hide the link."
)

PRIVACY_LINK_TITLE_PROMPT = (
    "\nWhat text do you want to use as the link label for the privacy preferences hyperlink?"
)
GENERATE_SELF_SIGNED_CERT_PROMPT = (
    "\nDo you want to generate a self-signed certificate? This requires Java and may "
    "prompt you for the root user password "
)

SELF_SIGNED_CERT_SAVE_PATH_PROMPT = (
    "\nWhere do you want to save the self-signed certificates? Default:"
)

CERT_STORE_PASSWORD_PROMPT = (
    "\nWhat is the keystore password? Leave it as default if you haven't changed it."
)

CERT_FILE_PATH_PROMPT = "\nWhat is the path to the certificate file?"

KEY_FILE_PATH_PROMPT = "\nWhat is the path to the key file of the certificate?"

KEYSTORE_FILE_PATH_PROMPT = "\nWhat is the path to the keystore file?"

HANDLER_CERT_STORE_PASSWORD_PROMPT = (
    "\nWhat is the keystore password for the Handler host? Leave it as default if you "
    "haven't changed it."
)

HANDLER_KEYSTORE_FILE_PATH_PROMPT = "\nWhat is the path to the keystore file on the Handler host?"

AUTHSERVER_CERT_STORE_PASSWORD_PROMPT = (
    "\nWhat is the keystore password for the Authentication Server host? Leave it "
    "as default if you haven't changed it."
)

AUTHSERVER_KEYSTORE_FILE_PATH_PROMPT = (
    "\nWhat is the path to the keystore file on the Authentication Server host?"
)

WEBCLIENT_CERT_FILE_PATH_PROMPT = (
    "\nWhat is the path to the certificate file on the Web Client host?"
)

WEBCLIENT_KEY_FILE_PATH_PROMPT = (
    "\nWhat is the path to the key file of the certificate on the Web Client host?"
)

ROOT_CA_PATH_PROMPT = "\nWhat is the path to the root CA certificate file on the Web Client host?"

USE_PAM_AUTH_PROMPT = (
    "\nDo you want to use PAM authentication? Otherwise, header-based authentication will be used"
)

PAM_SERVICE_NAME_PROMPT = (
    "\nWhat is the name of the PAM service the Authentication Server should use? If /etc/pam.d/dcv is installed, you "
    "can use 'dcv'. Otherwise, for Amazon Linux/RedHat/CentOS use 'system-auth' and for Ubuntu/Debian use 'common-auth'"
)

NORMALIZE_USER_ID_PROMPT = "\nDo you want to normalize the User ID to lowercase?"

HEADER_NAME_PROMPT = (
    "\nWhat do you want to use as the name of the header field to retrieve the username from?"
)

DYNAMODB_REGION_PROMPT = (
    "\nIf you are using DynamoDB, what is the region of the DynamoDB table? In order to use DynamoDB, this host needs "
    "to have access to DynamoDB via the AWS Credentials Provider Chain. \nIf you are using MariaDB, leave blank."
)

INSTALL_MARIADB_PROMPT = "\nDo you want to install MariaDB on this host?"

SETUP_MARIADB_PROMPT = (
    "\nIt appears MariaDB is already installed. Do you want the wizard to create the user and database in MariaDB "
    "needed by the Access Console?"
)

_mariadb_set_environ_text = f"MYSQL_HOST' is currently {os.environ.get('MYSQL_HOST')}"

_mariadb_blank_environ_text = "'MYSQL_HOST' is not currently set, so localhost will be used."

MARIADB_HOST_PROMPT = (
    "\nWhat is the address of the MariaDB server? To retrieve this from the 'MYSQL_HOST' "
    "environment variable, leave blank. "
    f"{_mariadb_set_environ_text if os.environ.get('MYSQL_HOST') else _mariadb_blank_environ_text}"
)


MARIADB_DATABASE_PROMPT = "\nWhat is the name of the MariaDB database that you want to use?"

MARIADB_PORT_PROMPT = "\nWhat is the port of the MariaDB server? Default:"

MARIADB_USERNAME_PROMPT = "\nWhat is the username to use with the MariaDB database? Default:"

MARIADB_PASSWORD_PROMPT = "\nWhat is the password of the MariaDB user? The input will be hidden"

MARIADB_PASSWORD_NOT_ALREADY_SETUP = (
    "\nWhat would you like to use as the password of the MariaDB user?"
)

INSTALL_NGINX_PROMPT = "\nDo you want to install NGINX on this host?"

INSTALL_COMPONENTS_PROMPT = f"\nDo you want to install the {PRODUCT_NAME} components on this host?"

COMPONENT_INSTALLERS_LOCATION_PROMPT = (
    "\nWhat is the path to the three component installers? Default is the current directory. "
)

INSTALL_CONFIG_FILES_PROMPT = (
    "\nDo you want to install the configuration files to the correct destinations? This "
    "should only be done on a host that already has the components installed. This will "
    "also take backups of the existing configuration files and place them in "
    "the current directory. May prompt you to enter the root user password. This will also place a custom "
    "nginx.conf file in the /etc/nginx/conf.d/ directory. This will also try to reload each component."
)

SAVE_CONFIG_FILES_PATH_PROMPT = (
    "\nWhere do you want to save the configuration files? Leave blank to save in the 'output' folder in the same "
    "directory as the Wizard "
)

READ_EXISTING_CONFIG_FILES_PROMPT = (
    "\nDo you want to read the existing configuration files from their default "
    "installation paths and use them as defaults? If the files are missing, "
    "it will use the default configuration files. "
)

ADMIN_USER_PROMPT = (
    "\nDo you want to set a user as admin? If so, specify their username here. Leave blank if you "
    "don't want to set anyone as admin "
)
