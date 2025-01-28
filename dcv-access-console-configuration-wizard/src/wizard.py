#!/usr/bin/env python3
# Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

import sys

from dcv_access_console_config_wizard.utils import logger


def main(argv, arc):
    python_major = sys.version_info.major
    python_minor = sys.version_info.minor

    if python_major < 3 or (python_major == 3 and python_minor < 8):
        logger.get().error(
            "Python 3.8 or greater is required to run the DCV Session Manager CLI, but version {}.{} is in use.".format(
                python_major, python_minor
            )
        )
        return 1

    from dcv_access_console_config_wizard import cli

    if arc >= 2 and argv[1] == "update":
        return cli.update()
    return cli.run()


if __name__ == "__main__":
    sys.exit(main(sys.argv, len(sys.argv)))
