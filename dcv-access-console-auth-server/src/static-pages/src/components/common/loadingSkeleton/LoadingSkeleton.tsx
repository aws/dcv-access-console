'use client'

import './LoadingSkeleton.css'
import * as React from "react";
import SpaceBetween from "@cloudscape-design/components/space-between";
import Box from "@cloudscape-design/components/box";
import {Spinner} from "@cloudscape-design/components";

export default function LoadingSkeleton({title = "Loading"}) {
    return <div className={"loading-skeleton"}>
        <div
            className="loading-page"
        >
            <SpaceBetween direction="vertical" alignItems={"center"} size={"xxl"}>
                <Box textAlign="center">
                    <SpaceBetween direction="vertical" size="xs">
                        <Spinner size={"normal"}/>
                        <div className="loading-text"><Box variant={"p"} color={"inherit"}>{title}</Box></div>
                    </SpaceBetween>
                </Box>
            </SpaceBetween>
        </div>
    </div>
}
