import * as React from "react";
import {service} from "@/constants/service-constants";
import {capitalizeFirstLetter} from "@/components/common/utils/TextUtils";
import {SpaceBetween, TextContent} from "@cloudscape-design/components";
import {SESSION_TEMPLATES_TABLE_CONSTANTS} from "@/constants/session-templates-table-constants";

export type OsLabelProps = {
    osFamily: String | undefined,
}
export default function OsLabel(props: OsLabelProps) {
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
    return <SpaceBetween size={"xxs"} direction={"horizontal"} alignItems={"center"}>
            <img src={imgSrc} alt={props.osFamily}/>
            <TextContent>
                {capitalizeFirstLetter(props.osFamily)}
            </TextContent>
        </SpaceBetween>
}
