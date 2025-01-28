// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import type { Preview } from "@storybook/react";

const preview: Preview = {
    parameters: {
        actions: {argTypesRegex: "^on[A-Z].*"},
        controls: {
            matchers: {
                color: /(background|color)$/i,
                date: /Date$/,
            },
        },
        nextjs: {
            appDirectory: true,
            navigation: {
                pathname: '/home/admin/console'
            }
        },
    },
};

export default preview;
