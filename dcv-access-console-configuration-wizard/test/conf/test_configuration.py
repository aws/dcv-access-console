# Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

from unittest import TestCase, main

from dcv_access_console_config_wizard.conf.configuration_generator import modify_webclient_config


class ConfigurationTest(TestCase):
    def test_modify_webclient_config(self):
        modify_webclient_config([], "lorem", "ipsum", "dolor", "sit", "amet", "consectetur")
        self.assertTrue(True)


if __name__ == "__main__":
    main()
