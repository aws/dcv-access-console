// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

'use client'

import {
    AppLayout,
    AppLayoutProps,
    Flashbar,
    NonCancelableCustomEvent,
} from "@cloudscape-design/components";
import * as React from "react";
import {useState} from "react";
import TopNavBar from "@/components/common/top-nav-bar/TopNavBar";
import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import UserGroupsTable from "@/components/user-management/user-groups/user-groups-table/UserGroupsTable";
import {UserGroup} from "@/generated-src/client";
import UserGroupsSplitPanel
    from "@/components/user-management/user-groups/user-groups-split-panel/UserGroupsSplitPanel";
import {useUserGroupsService, DEFAULT_FILTERING_QUERY} from "@/components/common/hooks/DataAccessServiceHooks";
import {PropertyFilterQuery} from "@cloudscape-design/collection-hooks";
import usePageLoading from "@/components/common/hooks/PageLoadingHook";
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";
import UserGroupsInfo from "@/components/info-panels/UserGroupsInfo";

export default function UserGroups() {
    const [selectedGroups, setSelectedGroups] = useState<UserGroup[]>([])
    const [toolsOpen, setToolsOpen] = useState<boolean>(false)
    const [describeUserGroupsFilteringQuery, setDescribeUserGroupsFilteringQuery] = useState<PropertyFilterQuery>(DEFAULT_FILTERING_QUERY)
    const {loading: pageLoading, userSession} = usePageLoading()

    const userGroupsTable = <UserGroupsTable
        selectedUserGroup={selectedGroups ? selectedGroups[0] : undefined}
        setSelectedUserGroup={setSelectedGroups}
        addHeader={true}
        variant={"full-page"}
        query={describeUserGroupsFilteringQuery}
        setQuery={setDescribeUserGroupsFilteringQuery}
        infoLinkFollow={() => setToolsOpen(true)}
        dataAccessServiceFunction={useUserGroupsService}
    />

    const {items} = useFlashBarContext()

    const onToolChange = (event: NonCancelableCustomEvent<AppLayoutProps.ChangeDetail>): void => {
        setToolsOpen(event.detail.open);
    }

    if(pageLoading) return <LoadingSkeleton/>
    return (
        <div>
            <TopNavBar session={userSession}/>
            <AppLayout
                notifications={<Flashbar items={items} stackItems/>}
                navigation={
                    <SideNavPanel session={userSession}/>
                }
                maxContentWidth={Number.MAX_VALUE}
                content={userGroupsTable}
                tools={<UserGroupsInfo/>}
                splitPanel={
                    <UserGroupsSplitPanel
                        group={selectedGroups ? selectedGroups[0] : undefined}
                    />
                }
                toolsOpen={toolsOpen}
                onToolsChange={onToolChange}
            />
        </div>
    )
}
