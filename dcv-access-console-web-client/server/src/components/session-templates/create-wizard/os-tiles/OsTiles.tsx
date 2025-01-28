// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import * as React from "react";
import Tiles from "@cloudscape-design/components/tiles";
import {service} from "@/constants/service-constants";
import {useEffect, useState} from "react";

export type OsTilesProps = {
    columns: number
    os: string
    setOs: (Os: string) => void
    setType: (type: string) => void
}

export default ({columns, os, setOs, setType}: OsTilesProps) => {
    const [linuxLogoExists, setLinuxLogoExists] = useState(false);
    const [windowsLogoExists, setWindowsLogoExists] = useState(false);

    useEffect(() => {
        const linux = new Image();
        linux.onload = () => setLinuxLogoExists(true);
        linux.onerror = () => setLinuxLogoExists(false);
        linux.src = service.osLogos.linux;

        const windows = new Image();
        windows.onload = () => setWindowsLogoExists(true);
        windows.onerror = () => setWindowsLogoExists(false);
        windows.src = service.osLogos.windows;
    }, []);

    const items = [
        {
            label: "Linux",
            image: linuxLogoExists ? (
                <center>
                    <img
                        src={service.osLogos.linux}
                        alt="Linux"
                        style={{width: 96, padding: 5}}
                    />
                </center>
            ) : undefined,
            value: "linux"
        },
        {
            label: "Windows",
            image: windowsLogoExists ? (
                <center>
                    <img
                        src={service.osLogos.windows}
                        alt="Windows"
                        style={{width: 96, padding: 5}}
                    />
                </center>
            ) : undefined,
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