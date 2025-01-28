// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {act, render, screen} from "@testing-library/react";
import createWrapper from "@cloudscape-design/components/test-utils/dom";
import {
    ServerOverviewEmpty,
    ServerOverviewWithServer, ServerOverviewWithUnknownCpu
} from "@/components/servers/server-overview/ServerOverview.stories";
import ServerOverview from "@/components/servers/server-overview/ServerOverview";
import {SERVER_OVERVIEW_CONSTANTS} from "@/constants/server-overview-contants";
import {SERVERS_TABLE_CONSTANTS} from "@/constants/servers-table-constants";
import {SERVER_DETAILS_CONSTANTS} from "@/constants/server-details-constants";

describe('ServerOverview', () => {
    it('Should render when empty', () => {
        render(<ServerOverview {...ServerOverviewEmpty.args}/>)
        expect(screen.getByText(SERVER_DETAILS_CONSTANTS.UNKNOWN, {})).toBeVisible()
    })
    it('Should render with server', () => {
        let wrapper
        act(() => {
            const {container} = render(<ServerOverview {...ServerOverviewWithServer.args}/>)
            wrapper = createWrapper(container)
        })
        expect(screen.getByText(SERVER_OVERVIEW_CONSTANTS.HEADER, {})).toBeVisible()
    })

    it.skip('Should render with unknown cpu', () => {
        render(<ServerOverview {...ServerOverviewWithUnknownCpu.args}/>)
        expect(screen.getAllByText(SERVERS_TABLE_CONSTANTS.UNKNOWN, {})).toHaveLength(1)
    })

})
