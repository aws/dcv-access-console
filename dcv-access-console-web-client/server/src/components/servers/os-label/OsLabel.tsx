import {Os} from "@/generated-src/client";
import * as React from "react";
import {service} from "@/constants/service-constants";
import {capitalizeFirstLetter} from "@/components/common/utils/TextUtils";
import {SpaceBetween, TextContent} from "@cloudscape-design/components";
import {SERVERS_TABLE_CONSTANTS} from "@/constants/servers-table-constants";

export type OsLabelProps = {
    os: Os | undefined,
}
export default function OsLabel(props: OsLabelProps) {
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
    return <SpaceBetween size={"xxs"} direction={"horizontal"} alignItems={"center"}>
            <img src={imgSrc} alt={props.os?.Family}/>
            <TextContent>
                {capitalizeFirstLetter(props.os?.Family?.toString())}
            </TextContent>
        </SpaceBetween>
}
