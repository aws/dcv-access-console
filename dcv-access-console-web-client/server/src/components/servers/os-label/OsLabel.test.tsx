// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {render, screen} from "@testing-library/react";
import OsLabel from "@/components/servers/os-label/OsLabel";
import {OsLabelLinux, OsLabelUnknown, OsLabelWindows} from "@/components/servers/os-label/OsLabel.stories";

describe('OsLabel', () => {
    it('Should render windows', () => {
        render(<OsLabel {...OsLabelWindows.args}/>)
        expect(screen.getByText("Windows", {})).toBeVisible()
    })
    it('Should render linux', () => {
        render(<OsLabel {...OsLabelLinux.args}/>)
        expect(screen.getByText("Linux", {})).toBeVisible()
    })
    it('Should render unknown', () => {
        render(<OsLabel {...OsLabelUnknown.args}/>)
        expect(screen.getByText("UNKNOWN", {})).toBeVisible()
    })
})
