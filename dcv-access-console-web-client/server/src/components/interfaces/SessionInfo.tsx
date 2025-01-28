// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

export default interface SessionInfo {
    key: string,
    details: {
        desktop_name: string,
        status: string,
        role: string,
        operating_system: string,
        ip_address: string,
        session_id: string,
        cpu: string,
        gpu: string,
        last_connected: string,
        number_connected: number,
        created_at: string,
        screenshot: string
    }
}