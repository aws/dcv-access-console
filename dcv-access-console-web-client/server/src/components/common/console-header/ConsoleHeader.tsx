import './ConsoleHeader.css'
import {ContentLayout, Link, SpaceBetween, Tabs} from "@cloudscape-design/components";
import * as React from "react";
import {CancelableEventHandler} from "@cloudscape-design/components/internal/events";
import {LinkProps} from "@cloudscape-design/components/link/interfaces";
import HeaderWithCounter from "@/components/common/header-with-counter/HeaderWithCounter";

export default function ConsoleHeader({counter,
                                          headerTitle,
                                          headerDescription,
                                          infoLinkFollow,
                                          infoLinkLabel,
                                          actions,
                                          tabs,
                                          openEnd,
                                          loading
                                         }:
                                         {
                                             counter?: number
                                             headerTitle: string, headerDescription: string
                                             infoLinkFollow?: CancelableEventHandler<LinkProps.FollowDetail>, infoLinkLabel?: string
                                             actions?: React.ReactNode
                                             tabs?: Tabs,
                                             openEnd?: boolean,
                                             loading?: boolean
                                         }) {
    return (
        <ContentLayout
            disableOverlap
            header={
                <SpaceBetween size="m">
                    {tabs ? tabs : undefined}
                    <HeaderWithCounter
                        counter={counter}
                        openEnd={openEnd}
                        variant={"h1"}
                        info={infoLinkFollow && infoLinkLabel ? <Link variant={"info"} onFollow={infoLinkFollow}>{infoLinkLabel}</Link> : ""}
                        description={headerDescription}
                        actions={actions}
                        loading={loading}
                    >
                        <span className={"header-text"}><b>{headerTitle}</b></span>
                    </HeaderWithCounter>
                </SpaceBetween>
            }
        >
        </ContentLayout>
    )
}
