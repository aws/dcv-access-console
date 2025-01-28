// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {JWT} from "next-auth/jwt";
import {NextResponse} from "next/server";
import {withAuth} from "next-auth/middleware";

const PUBLIC_PAGES = ["/dcv-logo.svg", "/footer-logo.svg", "/login-background.svg", "/service-name.svg", "/error"]
const HANDLER_URL = process.env.SM_UI_HANDLER_BASE_URL
const AUTH_URL = process.env.SM_UI_AUTH_WELL_KNOWN_URI.replace("/.well-known/oauth-authorization-server", "")
export default withAuth(function middleware(request) {
        const nonce = Buffer.from(crypto.randomUUID()).toString('base64')
        const cspHeader = `
    default-src 'self' 'nonce-${nonce}' ${HANDLER_URL} ${AUTH_URL};
    script-src 'self' 'nonce-${nonce}' 'strict-dynamic'${process.env.NODE_ENV == "development" ? ' \'unsafe-eval\'' : ''};
    style-src 'self' 'unsafe-inline';
    img-src 'self' blob: data:;
    font-src 'self' data:;
    object-src 'none';
    base-uri 'self';
    form-action 'self';
    frame-ancestors 'none';
    block-all-mixed-content;
    upgrade-insecure-requests;
`
        const requestHeaders = new Headers()

        // Setting request headers
        requestHeaders.set('x-nonce', nonce)
        requestHeaders.set(
            'Content-Security-Policy',
            // Replace newline characters and spaces
            cspHeader.replace(/\s{2,}/g, ' ').trim()
        )

        // Redirect empty path to sessions
        if (request.nextUrl.pathname === "/") {
            return NextResponse.redirect(new URL(process.env.NEXT_PUBLIC_DEFAULT_PATH, request.url))
        }

        // Check that user is admin for pages where the path starts with admin
        if (request.nextUrl.pathname.startsWith("/admin") && !hasAccess("admin", request.nextauth.token)) {
            return NextResponse.redirect(new URL("/denied", request.url))
        }

        return NextResponse.next({
            headers: requestHeaders,
            request: {
                headers: requestHeaders,
            },
        })
    },
    {

        callbacks: {

            authorized: ({req, token}) => {
                // Return public pages/images without auth
                if (PUBLIC_PAGES.indexOf(req.nextUrl.pathname) > -1) {
                    return true
                }
                return !!token
            },
        },
    })
function hasAccess(path: string, token: JWT | null) {
    if (path === "admin") {
        return token?.userRole === "Admin"
    }
}
