// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

'use client'

import {useRouter} from 'next/navigation'
import './Error.css'
import * as React from "react";
import Modal from "@cloudscape-design/components/modal";
import Box from "@cloudscape-design/components/box";
import SpaceBetween from "@cloudscape-design/components/space-between";
import Button from "@cloudscape-design/components/button";

export type ErrorProps = {
    title?: string
    text?: string
    confirmPath: string
    confirmText?: string
}
export default function Error({title = "Error", text = "Something went wrong", confirmPath = "/", confirmText = "Return home"}: ErrorProps) {
    const router = useRouter()

    return (
        <div
            className="error-page"
        >
            <Modal
                onDismiss={() => router.push(confirmPath)}
                visible={true}
                footer={
                    <Box float="right">
                        <SpaceBetween direction="horizontal" size="xs">
                            <Button variant="primary" onClick={() => router.push(confirmPath)}>{confirmText}</Button>
                        </SpaceBetween>
                    </Box>
                }
                header={title}
            >
                {text}
            </Modal>
        </div>)
}
