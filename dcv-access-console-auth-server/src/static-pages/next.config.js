/** @type {import('next').NextConfig} */
const nextConfig = {
    output: 'export', // Export to static HTML for the login screen
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
}

module.exports = nextConfig