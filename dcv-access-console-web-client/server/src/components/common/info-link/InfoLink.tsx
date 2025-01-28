// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import * as React from "react";
import Link from "@cloudscape-design/components/link";
import {GLOBAL_CONSTANTS} from "@/constants/global-constants";
import {CancelableEventHandler} from "@cloudscape-design/components/internal/events";
import {LinkProps} from "@cloudscape-design/components/link/interfaces";

export type InfoLinkProps = {
    onFollow: CancelableEventHandler<LinkProps.FollowDetail>
}

export default ({onFollow}: InfoLinkProps) => {
    return (
        <Link onFollow={onFollow} variant="info">
            {GLOBAL_CONSTANTS.INFO_LABEL}
        </Link>
    );
}
