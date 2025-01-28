# Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

import subprocess
from pathlib import Path

from dcv_access_console_config_wizard.utils import logger

ASSETS_PATH = Path(__file__).parent.parent / "assets"
GENERATE_CERTIFICATES_SH_PATH = ASSETS_PATH / "scripts" / "generate_certificates.sh"

log = logger.get()


def generate_certificates(save_location: Path, password: str, webclient_address) -> bool:
    log.info("Generating self signed certificates...")
    # Call generate_certificates.sh
    with open(logger.get_verbose_logging_file(), "a+") as logging_file:

        if len(webclient_address.split("//")) > 1:
            webclient_address = webclient_address.split("//")[1]

        log.debug(f"Using script at {GENERATE_CERTIFICATES_SH_PATH} to generate certificate")
        log.debug(
            f"Saving certificates to {save_location}, using {password} as password and {webclient_address} as the CN"
        )
        if (
            subprocess.run(
                [
                    "sudo",
                    "-S",
                    "bash",
                    GENERATE_CERTIFICATES_SH_PATH,
                    save_location,
                    password,
                    webclient_address,
                ],
                stdout=logging_file,
                stderr=logging_file,
            ).returncode
            != 0
        ):
            log.error("Self-Signed certificate script failed with non-zero return code")
            return False
    log.info(f"Successfully generated self signed certificates and saved them to {save_location}")
    return True
