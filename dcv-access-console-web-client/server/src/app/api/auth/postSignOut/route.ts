// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

'use server'

import {NextResponse} from "next/server";

export async function POST(request) {
    console.log('request:', request)
    let csrfCookie = '__Host-next-auth.csrf-token'
    let response = new NextResponse();
    response.cookies.set({
        name: csrfCookie,
        value: '',
        httpOnly: true,
        sameSite: 'lax',
        path: '/',
        secure: true,
        maxAge: -1,
    })
    return response
}
