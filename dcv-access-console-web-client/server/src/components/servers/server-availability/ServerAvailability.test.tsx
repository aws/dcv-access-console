// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {render, screen} from "@testing-library/react";
import ServerAvailability from "@/components/servers/server-availability/ServerAvailability";
import {
    ServerAvailabilityAvailable,
    ServerAvailabilityServerFull, ServerAvailabilityUnavailable, ServerAvailabilityUnknown
} from "@/components/servers/server-availability/ServerAvailability.stories";

describe('ServerAvailability', () => {
    it('Should render available', () => {
        render(<ServerAvailability {...ServerAvailabilityAvailable.args}/>)
        expect(screen.getByText("Available", {})).toBeVisible()
    })
    it('Should render server full', () => {
        render(<ServerAvailability {...ServerAvailabilityServerFull.args}/>)
        expect(screen.getByText("Server full", {})).toBeVisible()
    })
    it('Should render unavailable', () => {
        render(<ServerAvailability {...ServerAvailabilityUnavailable.args}/>)
        expect(screen.getByText("Unreachable agent", {})).toBeVisible()
    })

    it('Should render Not available', () => {
        render(<ServerAvailability {...ServerAvailabilityUnknown.args}/>)
        expect(screen.getByText("Not available", {})).toBeVisible()
    })

})
