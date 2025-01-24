import * as React from "react";
import Tiles from "@cloudscape-design/components/tiles";
import {service} from "@/constants/service-constants";

export type OsTilesProps = {
    columns: number
    os: string
    setOs: (Os: string) => void
    setType: (type: string) => void
}

export default ({columns, os, setOs, setType}: OsTilesProps) => {
    const items = [
        {
            label: "Linux",
            image: (
                <center>
                    <img
                        src={service.osLogos.linux}
                        alt="Linux"
                        style={{width: 96, padding: 5}}
                    />
                </center>
            ),
            value: "linux"
        },
        {
            label: "Windows",
            image: (
                <center>
                    <img
                        src={service.osLogos.windows}
                        alt="Windows"
                        style={{width: 96, padding: 5}}
                    />
                </center>
            ),
            value: "windows"
        }
    ]
    return (
        <Tiles
            onChange={({ detail }) => {
                setOs(detail.value)
                if(detail.value == "windows") {
                    setType("CONSOLE")
                }
            }}
            value={os}
            columns={columns}
            items={items}
        />
    );
}