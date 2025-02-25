// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import NextAuth, {AuthOptions, NextAuthOptions} from "next-auth"
import {getCsrfToken, signOut} from "next-auth/react";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {DescribeUserInfoResponse} from "@/generated-src/client";
import {UserinfoResponse} from "openid-client";
import {logger} from "@/components/common/utils/LoggerUtil";
import {NextRequest} from "next/server";

const ERROR_MESSAGE_500 = "An error occured while processing the request."
const BAD_CALLBACK_URL_MESSAGE = "Callback URL must begin with '/'."
const BAD_REQUEST_MESSAGE = "Request body could not be parsed"

async function getLogoutEndpoint() {
    try {
        const enableLogoutRedirect = process.env.SM_UI_AUTH_ENABLE_PROVIDER_LOGOUT === 'true'
        if (!enableLogoutRedirect) {
            return undefined
        }
        // Getting the logout endpoint from the wellknown uri
        const wellKnownJson = await (await fetch(process.env.SM_UI_AUTH_WELL_KNOWN_URI)).json()
        return wellKnownJson?.end_session_endpoint ?? process.env.SM_UI_AUTH_LOGOUT_URI
    } catch (error) {
        console.log("Error while retrieving the logout endpoint", error)
        return process.env.SM_UI_AUTH_LOGOUT_URI
    }
}

/**
 * Takes a token, and returns a new token with updated
 * `accessToken` and `accessTokenExpires`. If an error occurs,
 * returns the old token and an error property
 */
async function refreshAccessToken(token) {
    try {
        // Getting the token endpoint from the wellknown uri
        const wellKnownJson = await (await fetch(process.env.SM_UI_AUTH_WELL_KNOWN_URI)).json()
        const url = wellKnownJson.token_endpoint

        const response = await fetch(url, {
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "Authorization": "Basic " + Buffer.from(process.env.SM_UI_AUTH_CLIENT_ID + ":" + process.env.SM_UI_AUTH_CLIENT_SECRET, 'binary').toString('base64')
            },
            body: new URLSearchParams({
                grant_type: "refresh_token",
                refresh_token: token.refresh_token,
            }).toString(),
            method: "POST",
        })

        const refreshedTokens = await response.json()

        if (!response.ok) {
            throw response
        }
        return {
            ...token,
            access_token: refreshedTokens.access_token,
            access_token_expires_at: Date.now() + (refreshedTokens.expires_in * 1000),
            refresh_token: refreshedTokens.refresh_token ?? token.refresh_token, // Fall back to old refresh token
        }
    } catch (error) {
        console.log("Error while trying to obtain the refresh token", error)
        console.log("Logging out")
        await signOut()
        return
    }
}

async function revokeRefreshToken(token) {
    try {
        // Getting the revocation endpoint from the wellknown uri
        const wellKnownJson = await (await fetch(process.env.SM_UI_AUTH_WELL_KNOWN_URI)).json()
        const url = wellKnownJson.revocation_endpoint

        const response = await fetch(url, {
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "Authorization": "Basic " + Buffer.from(process.env.SM_UI_AUTH_CLIENT_ID + ":" + process.env.SM_UI_AUTH_CLIENT_SECRET, 'binary').toString('base64')
            },
            body: new URLSearchParams({
                token: token.refresh_token,
                client_id: process.env.SM_UI_AUTH_CLIENT_ID!,
                client_secret: process.env.SM_UI_AUTH_CLIENT_SECRET!,
                token_type_hint: "refresh_token"
            }).toString(),
            method: "POST",
        })

        if (!response.ok) {
            throw response
        }

        return true
    } catch (error) {
        console.log("Error while trying to revoke the refresh token", error)
        return false
    }
}

export const authOptions: NextAuthOptions = {
    logger: {
        error(code, metadata) {
            logger.error(code, metadata)
        },
        warn(code) {
            logger.warn(code)
        },
    },
    pages: {
        error: "error"
    },
    // https://next-auth.js.org/configuration/providers/oauth
    providers: [{
        id: "dcv-access-console-auth-server",
        name: "dcv-access-console-auth-server",
        type: "oauth",
        wellKnown: process.env.SM_UI_AUTH_WELL_KNOWN_URI,
        clientId: process.env.SM_UI_AUTH_CLIENT_ID,
        clientSecret: process.env.SM_UI_AUTH_CLIENT_SECRET,
        authorization: {params: {scope: "openid"}},
        checks: ["nonce", "pkce", "state"],
        idToken: true,
        userinfo: {
            url: process.env.SM_UI_HANDLER_BASE_URL + process.env.SM_UI_HANDLER_API_PREFIX! + "/describeUserInfo",
            // The result of this method will be the input to the `profile` callback.
            async request(context) {
                let user: DescribeUserInfoResponse | void = await new DataAccessService().getUserInfo(context.tokens.access_token!)
                if(!user) {
                    throw "Error while contacting the handler"
                }
                return {
                    UserInfo: {
                        id: user.Id,
                        displayName: user.DisplayName,
                        role: user.Role
                    }
                } as UserinfoResponse
            }
        },
        profile(profile) {
            return {
                id: profile.UserInfo.id,
                ...profile
            }
        },
    }],

    theme: {
        colorScheme: "light",
    },
    callbacks: {
        async jwt({token, account, profile}) {
            if (account) {
                token.access_token = account?.access_token
                token.refresh_token = account?.refresh_token
                token.token_type = account?.token_type
                if (account?.expires_at) {
                    token.access_token_expires_at = account?.expires_at
                }
            }
            if (profile) {
                token.sub = profile.UserInfo.id
                token.name = profile.UserInfo.displayName
                token.userRole = profile.UserInfo.role
            }

            // Return previous token if the access token has not expired yet
            if (token.access_token_expires_at && Date.now() < token.access_token_expires_at) {
                return token
            }

            // Access token has expired, try to update it
            return refreshAccessToken(token)
        },
        async session({session, user, token}) {
            const logoutEndpoint = await getLogoutEndpoint()

            // Send properties to the client, like an access_token and user id from a provider.
            session.userRole = token.userRole
            session.user.id = token.sub
            session.user.name = token.name
            session.user.role = token.userRole
            session.access_token = token.access_token
            session.sm_ui_handler_base_url = process.env.SM_UI_HANDLER_BASE_URL! + process.env.SM_UI_HANDLER_API_PREFIX!
            session.logout_endpoint = logoutEndpoint
            session.client_id = process.env.SM_UI_AUTH_CLIENT_ID
            session.post_logout_uri = process.env.NEXTAUTH_URL
            return session
        },
    },
    events: {
        async signOut({session, token}) {
            await revokeRefreshToken(token)
            await fetch("/api/auth/postSignOut", {
                method: "POST",
            })
            await getCsrfToken()
        }
    }
}
const handler = NextAuth(authOptions)

export {handler as GET}

export async function POST(req: NextRequest, context: any, options: AuthOptions) {
    if (req.nextUrl.searchParams.get("callbackUrl") !== null &&
        !req.nextUrl.searchParams.get("callbackUrl")?.startsWith("/")) {

        console.warn(BAD_CALLBACK_URL_MESSAGE + " Request: ", req)
        return new Response(BAD_CALLBACK_URL_MESSAGE, {
            status: 400,
        })
    }

    try {
        let response = await handler(req, context, options)
        if (response.status === 500) {
            console.error("NextAuth handler returned 500 error code. Request: ", req)
            console.error("Response: ", response)

            return new Response(ERROR_MESSAGE_500, {
                status: response.status,
                statusText: response.statusText,
                headers: response.headers
            })

        }
        return response
    } catch (error) {
        console.log("NextAuth handler was unable to fulfill request: ", error)
        if (error instanceof TypeError) {
            return new Response(BAD_REQUEST_MESSAGE, {
                status: 400,
            })
        }
        return new Response(ERROR_MESSAGE_500, {
            status: 500,
        })
    }
}
