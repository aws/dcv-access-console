# Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

import re
import subprocess

from dcv_access_console_config_wizard.utils import logger

log = logger.get()


def register_new_client_with_broker() -> (str, str):
    log.info("Registering new client with broker...")
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:

        proc = subprocess.run(
            [
                "sudo",
                "dcv-session-manager-broker",
                "register-api-client",
                "-cn",
                "nice-dcv-access-console",
            ],
            stdout=subprocess.PIPE,
            stderr=logging_file,
        )

        pattern = r"client-id: (.*?)\n.*?client-password: (.*?)\n"
        match = re.search(pattern, proc.stdout.decode("unicode_escape"), re.DOTALL)

        try:
            client_id = match[1]
            client_password = match[2]

            return client_id, client_password
        except AttributeError:
            log.warning("Unable to parse broker output")
            return None
