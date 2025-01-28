// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import * as React from "react";
import {capitalizeFirstLetter} from "@/components/common/utils/TextUtils";
import {StatusIndicator, TextContent} from "@cloudscape-design/components";
import {SESSIONS_CONSTANTS} from "@/constants/sessions-constants";
import {State} from "@/generated-src/client";

export type StatusLabelProps = {
    state: String | undefined,
}
export default function StatusLabel(props: StatusLabelProps) {
    if(!props.state) {
        return <StatusIndicator type={"warning"}>{SESSIONS_CONSTANTS.UNKNOWN}</StatusIndicator>
    }
    switch (props.state) {
        case State.Ready:
            return <StatusIndicator>{SESSIONS_CONSTANTS.AVAILABLE}</StatusIndicator>
        case State.Creating:
            return <StatusIndicator type={"pending"}>{SESSIONS_CONSTANTS.CREATING}</StatusIndicator>
        case State.Unknown:
            return <StatusIndicator type={"warning"}>{SESSIONS_CONSTANTS.UNKNOWN}</StatusIndicator>
        case State.Deleting:
            return <StatusIndicator type={"in-progress"}>{SESSIONS_CONSTANTS.CLOSING}</StatusIndicator>
        case State.Deleted:
            return <StatusIndicator type={"stopped"}>{SESSIONS_CONSTANTS.CLOSED}</StatusIndicator>
    }
    return <TextContent>
        {capitalizeFirstLetter(props.state)}
    </TextContent>
}
