// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

/** @type {import('next').NextConfig} */
const nextConfig = {
    // output: 'export', // Export to static HTML for the login screen
    experimental: {
        serverActions: true
    },
    // TODO: re-enable linting and TypeScript checks
    eslint: {
        // Warning: This allows production builds to successfully complete even if
        // your project has ESLint errors.
        ignoreDuringBuilds: true,
    },
    typescript: {
        // !! WARN !!
        // Dangerously allow production builds to successfully complete even if
        // your project has type errors.
        // !! WARN !!
        ignoreBuildErrors: true,
    },
    poweredByHeader: false,

    async headers() {
        return [
            {
                source: '/(.*?)', // Applies to all pages
                headers: [
                    {
                        key: 'X-Content-Type-Options',
                        value: 'nosniff'
                    },
                    {
                        key: 'X-Frame-Options',
                        value: 'DENY'
                    },
                    {
                        key: 'Strict-Transport-Security',
                        value: 'max-age=47304000; includeSubDomains'
                    },
                    {
                        key: 'Cache-Control',
                        value: 'no-store, no-cache'
                    }
                ]
            }
        ]
    }
}

module.exports = nextConfig
