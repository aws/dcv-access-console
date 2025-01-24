import subprocess
from datetime import datetime, timezone

import boto3
import botocore.exceptions

from dcv_access_console_config_wizard.utils import logger

log = logger.get()


def create_mysql_admin_user(
    database_name: str, users_table_name: str, admin_name: str, db_username, db_password
) -> bool:
    log.info(f"Creating admin user '{admin_name}' in MariaDB")
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:
        now = datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")
        if (
            subprocess.run(
                [
                    "mysql",
                    "-u",
                    db_username,
                    f"--password={db_password}",
                    "-e",
                    f"INSERT INTO {database_name}.{users_table_name} (userId, creationTime, displayName, isDisabled, isImported, role) "
                    f"VALUES ('{admin_name}', '{now}', '{admin_name}', 0, 0, 'Admin')",
                ],
                stderr=logging_file,
                stdout=logging_file,
            ).returncode
            != 0
        ):
            log.error("Unable to create admin user in MariaDB")
            return False

    log.info(f"Successfully created admin user '{admin_name}' in MariaDB")
    return True


def create_ddb_admin_user(region: str, users_table_name: str, admin_name) -> bool:
    log.info(f"Creating admin user '{admin_name}' in DynamoDB")
    try:
        dynamodb = boto3.resource("dynamodb", region_name=region)
        users_table = dynamodb.Table(users_table_name)
        if (
            users_table.put_item(
                Item={
                    "userId": admin_name,
                    "creationTime": datetime.now(tz=timezone.utc).strftime(
                        "%Y-%m-%dT%H:%M:%S.%f+00:00"
                    ),
                    "displayName": admin_name,
                    "isDisabled": False,
                    "isImported": False,
                    "role": "Admin",
                }
            )
            .get("ResponseMetadata")
            .get("HTTPStatusCode")
            != 200
        ):
            log.error("Unable to create admin user in DynamoDB")
            return False
    except botocore.exceptions.ClientError:
        log.error(f"Failed to create admin user '{admin_name}' in DynamoDB")
        return False
    log.info(f"Successfully created admin user '{admin_name}' in DynamoDB")
    return True
