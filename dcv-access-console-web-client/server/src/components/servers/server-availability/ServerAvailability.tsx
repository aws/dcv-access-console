import {Availability, UnavailabilityReason} from "@/generated-src/client";
import {StatusIndicator} from "@cloudscape-design/components";
import {capitalizeFirstLetter} from "@/components/common/utils/TextUtils";
import {SERVER_DETAILS_CONSTANTS} from "@/constants/server-details-constants";
import * as React from "react";

export type ServerAvailabilityProps = {
    availability: Availability | undefined,
    unavailabilityReason: UnavailabilityReason | undefined
}
export default function ServerAvailability(props: ServerAvailabilityProps) {
    switch (props.availability) {
        case Availability.Available:
            return <StatusIndicator
                type="success">{capitalizeFirstLetter(props.availability.toString())}</StatusIndicator>;
        case Availability.Unavailable:
            return <StatusIndicator
                type={props.unavailabilityReason == UnavailabilityReason.ServerFull ? "info" : "error"}>{capitalizeFirstLetter(props.unavailabilityReason?.toString())}</StatusIndicator>;
        default:
            return <StatusIndicator type="warning">{SERVER_DETAILS_CONSTANTS.UNKNOWN}</StatusIndicator>;
    }
}
