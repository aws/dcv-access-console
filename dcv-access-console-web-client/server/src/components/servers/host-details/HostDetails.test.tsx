// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {render, screen} from "@testing-library/react";
import ServerDetails from "@/components/servers/server-details/ServerDetails";
import {
    HostDetailsEmpty,
    HostDetailsNormal,
    HostDetailsWithFormattedSize
} from "@/components/servers/host-details/HostDetails.stories";
import HostDetails from "@/components/servers/host-details/HostDetails";
import {SERVERS_TABLE_CONSTANTS} from "@/constants/servers-table-constants";
import {HOST_DETAILS_CONSTANTS} from "@/constants/host-details-constants";

describe('ServerDetails', () => {
    it('Should render when empty', () => {
        render(<HostDetails {...HostDetailsEmpty.args}/>)
        expect(screen.getByText(HOST_DETAILS_CONSTANTS.EMPTY_TEXT, {})).toBeVisible()
    })
    it('Should render with host', () => {
        render(<HostDetails {...HostDetailsNormal.args}/>)
        expect(screen.getByText(SERVERS_TABLE_CONSTANTS.HOST_HEADER, {})).toBeVisible();
        expect(screen.getByText(SERVERS_TABLE_CONSTANTS.MEMORY_HEADER, {})).toBeVisible();
        expect(screen.getByText(SERVERS_TABLE_CONSTANTS.SWAP_HEADER, {})).toBeVisible();
    })
    it('Should render with formatted filesize', () => {
        render(<HostDetails {...HostDetailsWithFormattedSize.args}/>)
        expect(screen.getByText("UNKNOWN", {})).toBeVisible();
        expect(screen.getByText("None", {})).toBeVisible();
        expect(screen.getByText("0 B", {})).toBeVisible();
        expect(screen.getByText("2 KB", {})).toBeVisible();
        expect(screen.getByText("2 GB", {})).toBeVisible();
    })
})
