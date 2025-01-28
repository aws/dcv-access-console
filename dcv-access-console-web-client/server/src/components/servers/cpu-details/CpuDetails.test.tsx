// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {render, screen} from "@testing-library/react";
import {SERVERS_TABLE_CONSTANTS} from "@/constants/servers-table-constants";
import CpuDetails from "@/components/servers/cpu-details/CpuDetails";
import {CpuDetailsEmpty, CpuDetailsNormal} from "@/components/servers/cpu-details/CpuDetails.stories";
import {CPU_DETAILS_CONSTANTS} from "@/constants/cpu-details-constants";

describe('CpuDetails', () => {
    it('Should render when empty', () => {
        render(<CpuDetails {...CpuDetailsEmpty.args}/>)
        expect(screen.getByText(CPU_DETAILS_CONSTANTS.EMPTY_TEXT, {})).toBeVisible()
    })
    it('Should render with cpu', () => {
        render(<CpuDetails {...CpuDetailsNormal.args}/>)
        expect(screen.getByText(CPU_DETAILS_CONSTANTS.CPU_HEADER, {})).toBeVisible();
    })
})
