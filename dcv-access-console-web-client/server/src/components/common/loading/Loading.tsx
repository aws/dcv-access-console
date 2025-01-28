// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

'use client'

import './Loading.css'
import * as React from "react";
import {Spinner} from "@cloudscape-design/components";
import Box from "@cloudscape-design/components/box";
import SpaceBetween from "@cloudscape-design/components/space-between";
import {service} from "@/constants/service-constants";

export default function Loading({title = "Loading..."}) {
    return (
        <div
            className="loading-page"
        >
            <SpaceBetween direction="vertical" alignItems={"center"} size={"xxl"}>
                <Box textAlign="center">
                    <SpaceBetween direction="vertical" size="xs">
                        <img src={service.nameImage.src} alt={service.nameImage.alt} width="253.81" height="34"/>
                        <Spinner size={"normal"} variant={"inverted"}/>
                        <div className="loading-text"><Box variant={"p"} color={"inherit"}>{title}</Box></div>
                    </SpaceBetween>
                </Box>
            </SpaceBetween>
        </div>)
}
