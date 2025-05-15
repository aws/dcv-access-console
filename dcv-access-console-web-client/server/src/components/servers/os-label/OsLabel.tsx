// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {Os} from "@/generated-src/client";
import * as React from "react";
import {service} from "@/constants/service-constants";
import {capitalizeFirstLetter} from "@/components/common/utils/TextUtils";
import {SpaceBetween, TextContent} from "@cloudscape-design/components";
import {SERVERS_TABLE_CONSTANTS} from "@/constants/servers-table-constants";
import {useEffect, useState} from "react";

export type OsLabelProps = {
    os: Os | undefined,
}
export default function OsLabel(props: OsLabelProps) {
    const [osLogoExists, setOsLogoExists] = useState(false);

    let imgSrc
    if(!props.os) {
        return <TextContent>{SERVERS_TABLE_CONSTANTS.UNKNOWN}</TextContent>
    }
    switch (props.os?.Family) {
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
                {capitalizeFirstLetter(props.os?.Family?.toString())}
            </TextContent>
        </SpaceBetween>
}
