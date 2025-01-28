// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import * as React from "react";
import {service} from "@/constants/service-constants";
import {capitalizeFirstLetter} from "@/components/common/utils/TextUtils";
import {SpaceBetween, TextContent} from "@cloudscape-design/components";
import {SESSION_TEMPLATES_TABLE_CONSTANTS} from "@/constants/session-templates-table-constants";
import {useEffect, useState} from "react";

export type OsLabelProps = {
    osFamily: String | undefined,
}
export default function OsLabel(props: OsLabelProps) {
    const [osLogoExists, setOsLogoExists] = useState(false);

    let imgSrc
    if(!props.osFamily) {
        return <TextContent>{SESSION_TEMPLATES_TABLE_CONSTANTS.UNKNOWN}</TextContent>
    }
    switch (props.osFamily) {
        case "windows":
            imgSrc = service.osLogos.windows
            break;
        case "linux":
            imgSrc = service.osLogos.linux
            break;
    }

    useEffect(() => {
        const img = new Image();
        img.onload = () => setOsLogoExists(true);
        img.onerror = () => setOsLogoExists(false);
        img.src = imgSrc;
    }, []);

    return <SpaceBetween size={"xxs"} direction={"horizontal"} alignItems={"center"}>
        {osLogoExists ? <img src={imgSrc} alt={props.osFamily}/> : undefined}
            <TextContent>
                {capitalizeFirstLetter(props.osFamily)}
            </TextContent>
        </SpaceBetween>
}
