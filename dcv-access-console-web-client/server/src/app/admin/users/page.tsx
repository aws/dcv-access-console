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
import {User} from "@/generated-src/client";
import UsersTable from "@/components/user-management/users/users-table/UsersTable";
import UsersSplitPanel from "@/components/user-management/users/users-split-panel/UsersSplitPanel";
import {PropertyFilterQuery} from "@cloudscape-design/collection-hooks";
import {useUsersService, DEFAULT_FILTERING_QUERY} from "@/components/common/hooks/DataAccessServiceHooks";
import usePageLoading from "@/components/common/hooks/PageLoadingHook";
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";
import {DEFAULT_PREFERENCES} from "@/components/user-management/users/users-table/UsersTableColumnPreferences";
import {TOKEN_NAMES_MAP} from "@/constants/filter-users-bar-constants";
import UsersInfo from "@/components/info-panels/UsersInfo";

export default function Users() {
    const [selectedUsers, setSelectedUsers] = useState<User[]>([])
    const [toolsOpen, setToolsOpen] = useState<boolean>(false)
    const [describeUsersFilteringQuery, setDescribeUsersFilteringQuery] = useState<PropertyFilterQuery>(DEFAULT_FILTERING_QUERY)
    const {loading: pageLoading, userSession} = usePageLoading()

    const usersTable = <UsersTable
        selectedUsers={selectedUsers}
        setSelectedUsers={setSelectedUsers}
        addHeader={true}
        variant={"full-page"}
        selectionType={"single"}
        columnPreferences={DEFAULT_PREFERENCES}
        query={describeUsersFilteringQuery}
        setQuery={setDescribeUsersFilteringQuery}
        filterTokenNamesMap={TOKEN_NAMES_MAP}
        infoLinkFollow={() => setToolsOpen(true)}
        dataAccessServiceFunction={useUsersService}
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
                content={usersTable}
                tools={<UsersInfo/>}
                splitPanel={
                    <UsersSplitPanel
                        user={selectedUsers ? selectedUsers[0] : undefined}
                    />
                }
                toolsOpen={toolsOpen}
                onToolsChange={onToolChange}
            />
        </div>
    )
}
