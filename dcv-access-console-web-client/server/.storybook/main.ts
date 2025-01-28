// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import type { StorybookConfig } from "@storybook/nextjs";
import {WebpackConfiguration} from "@storybook/core-webpack";
const path = require("path");

const config: { webpackFinal: (config) => | WebpackConfiguration; staticDirs: string[]; stories: string[]; framework: { name: string; options: {} }; docs: { autodocs: string }; addons: string[] } = {
    stories: ["../src/**/*.mdx", "../src/**/*.stories.@(js|jsx|ts|tsx)"],
    addons: [
        "@storybook/addon-links",
        "@storybook/addon-essentials",
        "@storybook/addon-interactions",
    ],
    framework: {
        name: "@storybook/nextjs",
        options: {},
    },
    docs: {
        autodocs: "tag",
    },
    staticDirs: ['../public'],
    webpackFinal: (config) => {
        config.resolve.alias = {
            ...config.resolve.alias,
            "@": path.resolve(__dirname, "../src"),
        };
        return config;
    },
};
export default config;
