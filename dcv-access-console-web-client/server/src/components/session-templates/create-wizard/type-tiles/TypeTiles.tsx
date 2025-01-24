import * as React from "react";
import Tiles from "@cloudscape-design/components/tiles";
import {service} from "@/constants/service-constants";
import {SESSION_TEMPLATES_CREATE_CONSTANTS} from "@/constants/session-templates-create-constants";

export type TypeTilesProps = {
    columns: number
    os: string
    type: string
    setType: (typeValue: string) => void
}

export default function TypeTiles({columns, os, type, setType}: TypeTilesProps) {
    const items = [{
        label: "Console",
        image: (
            <center>
                <img
                    src={SESSION_TEMPLATES_CREATE_CONSTANTS.CONSOLE_PNG}
                    alt="Console"
                    style={{width: 96, padding: 5}}
                />
            </center>
        ),
        value: "CONSOLE",
        description: SESSION_TEMPLATES_CREATE_CONSTANTS.CONSOLE_DESCRIPTION
    },
    {
        label: "Virtual",
        image: (
            <center>
                <img
                    src={SESSION_TEMPLATES_CREATE_CONSTANTS.VIRTUAL_PNG}
                    alt="Virtual"
                    style={{width: 96, padding: 5}}
                />
            </center>
        ),
        value: "VIRTUAL",
        description: SESSION_TEMPLATES_CREATE_CONSTANTS.VIRTUAL_DESCRIPTION,
        disabled: os == "windows"
    }]
    return (
        <Tiles
            onChange={({ detail }) => setType(detail.value)}
            value={type}
            columns={columns}
            items={items}
        />
    );
}
