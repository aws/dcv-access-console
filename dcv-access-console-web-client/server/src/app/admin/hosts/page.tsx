'use client'

import * as React from "react";
import {useState} from "react";
import ServersTable from "@/components/servers/servers-table/ServersTable";
import ServerSplitPanel from "@/components/servers/server-split-panel/ServerSplitPanel";
import TopNavBar from "@/components/common/top-nav-bar/TopNavBar";
import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";
import {
    AppLayout,
    AppLayoutProps,
    CollectionPreferencesProps,
    Flashbar,
    NonCancelableCustomEvent
} from "@cloudscape-design/components";
import {DEFAULT_PREFERENCES} from "@/components/servers/servers-table/ServersTableColumnPreferences";
import {PropertyFilterProps} from "@cloudscape-design/components/property-filter";
import {useRouter} from "next/navigation";
import HostsInfo from "@/components/info-panels/HostsInfo";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import {useServersService} from "@/components/common/hooks/DataAccessServiceHooks";
import usePageLoading from "@/components/common/hooks/PageLoadingHook";
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";
import server_search_tokens from "@/generated-src/client/server_search_tokens";
import {
    FILTER_SERVER_CONSTANTS,
    SEARCH_TOKEN_TO_ID,
    TOKEN_NAME_GROUP_MAP,
    TOKEN_NAMES_MAP
} from "@/constants/filter-severs-bar-constants";
import FilterBar, {DescribeResponse} from "@/components/common/filter-bar/FilterBar";
import DataAccessService from "@/components/common/utils/DataAccessService";

const DEFAULT_FILTERING_QUERY = { tokens: [], operation: 'and' };

export default function Servers() {
    const [selectedItems, setSelectedItems] = useState([]);
    const [preferences, setPreferences] = useState<CollectionPreferencesProps.Preferences>(DEFAULT_PREFERENCES)
    const [filteringQuery, setFilteringQuery] = useState(DEFAULT_FILTERING_QUERY as PropertyFilterProps.Query);
    const [toolsOpen, setToolsOpen] = useState<boolean>(false)
    const [resetPaginationKey, setResetPaginationKey] = useState("")
    const {loading: pageLoading, userSession} = usePageLoading()
    const dataAccessService = new DataAccessService()

    const onToolChange = (event: NonCancelableCustomEvent<AppLayoutProps.ChangeDetail>): void => {
        setToolsOpen(event.detail.open);
    };

    const {push} = useRouter();

    const describeServers = async (describeServersRequest) => {
        const r =  await dataAccessService.describeServers(describeServersRequest)
        return {"objects": r.data.Servers, "nextToken": r.data.NextToken} as DescribeResponse
    }

    const filter = <FilterBar
        filteringQuery={filteringQuery}
        handlePropertyFilteringChange={({detail}) => {
            setFilteringQuery(detail)
            setResetPaginationKey(Date.now().toString())
        }}
        searchTokens={server_search_tokens}
        tokenNamesMap={TOKEN_NAMES_MAP}
        tokenNameGroupMap={TOKEN_NAME_GROUP_MAP}
        searchTokenToId={SEARCH_TOKEN_TO_ID}
        filteringPlaceholder={FILTER_SERVER_CONSTANTS.filteringPlaceholder}
        dataAccessServiceFunction={describeServers}
    />

    const table = <ServersTable
        selectedServer={selectedItems ? selectedItems[0] : undefined}
        onSelectionChange={event => setSelectedItems(event.detail.selectedItems)}
        preferences={preferences}
        setPreferences={setPreferences}
        filter={filter}
        buttonFunction={() => {
            if (selectedItems && selectedItems[0]) {
                console.log("Pushing to details page " + selectedItems[0]?.Id)
                push('/admin/hosts/' + selectedItems[0]?.Id)
            }
        }}
        infoLinkFollow={() => setToolsOpen(true)}
        resetPaginationKey={resetPaginationKey}
        dataAccessServiceFunction={useServersService}
        query={filteringQuery}
    />
    const {items} = useFlashBarContext()
    const hostsInfo = <HostsInfo/>

    const splitPanel = <ServerSplitPanel
        server={selectedItems ? selectedItems[0] : undefined}/>

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
                content={
                    table
                }
                tools={hostsInfo}
                splitPanel={(splitPanel)}
                toolsOpen={toolsOpen}
                onToolsChange={onToolChange}
            >
            </AppLayout>
        </div>
    )
}
