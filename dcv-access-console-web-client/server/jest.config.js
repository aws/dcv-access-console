// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

const nextJest = require('next/jest')

const createJestConfig = nextJest({
    // Provide the path to your Next.js app to load next.config.js and .env files in your test environment
    dir: './'

})

// Add any custom config to be passed to Jest
const customJestConfig = {
    collectCoverageFrom: ["src/components/**/*.tsx"],
    coverageDirectory: "coverage",
    coverageReporters: ['json-summary', 'lcov', 'text'],
    coveragePathIgnorePatterns: [".*stories.*"],
    setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
    testEnvironment: "jest-environment-jsdom",
    preset: "@cloudscape-design/jest-preset"
}

// createJestConfig is exported this way to ensure that next/jest can load the Next.js config which is async
module.exports = createJestConfig(customJestConfig)
