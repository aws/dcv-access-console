// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import ServerAvailability from "@/components/servers/server-availability/ServerAvailability";
import {
    Availability,
    UnavailabilityReason
} from "@/generated-src/client";

export default {
    title: 'components/server/ServerAvailability',
    component: ServerAvailability,
}
const Template = args => <ServerAvailability{...args}/>
export const ServerAvailabilityAvailable = Template.bind({})
ServerAvailabilityAvailable.args = {
    availability: Availability.Available
}

export const ServerAvailabilityServerFull = Template.bind({})
ServerAvailabilityServerFull.args = {
    availability: Availability.Unavailable,
    unavailabilityReason: UnavailabilityReason.ServerFull
}

export const ServerAvailabilityUnavailable = Template.bind({})
ServerAvailabilityUnavailable.args = {
    availability: Availability.Unavailable,
    unavailabilityReason: UnavailabilityReason.UnreachableAgent

}

export const ServerAvailabilityUnknown = Template.bind({})
ServerAvailabilityUnknown.args = {
}

