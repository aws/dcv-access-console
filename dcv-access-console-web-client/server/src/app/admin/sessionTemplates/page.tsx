'use client'

import * as React from "react";
import {useState} from "react";
import TopNavBar from "@/components/common/top-nav-bar/TopNavBar";
import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";
import {AppLayout, AppLayoutProps, Flashbar, NonCancelableCustomEvent} from "@cloudscape-design/components";
import SessionTemplatesSplitPanel
    from "@/components/session-templates/session-templates-split-panel/SessionTemplatesSplitPanel";
import {SessionTemplate} from "@/generated-src/client";
import SessionTemplatesTable from "@/components/session-templates/session-templates-table/SessionTemplatesTable";
import SessionTemplatesInfo from "@/components/info-panels/SessionTemplatesInfo";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import {useSessionTemplatesService, DEFAULT_FILTERING_QUERY} from "@/components/common/hooks/DataAccessServiceHooks";
import {PropertyFilterQuery} from "@cloudscape-design/collection-hooks";
import usePageLoading from "@/components/common/hooks/PageLoadingHook";
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";
import {
    SESSION_TEMPLATES_DEFAULT_PREFERENCES
} from "@/components/session-templates/session-templates-table/SessionTemplatesTableColumnPreferences";

export default function SessionTemplates() {
    const [selectedSessionTemplates, setSelectedSessionTemplates] = useState<SessionTemplate[]>([])
    const [toolsOpen, setToolsOpen] = useState<boolean>(false)
    const [describeSessionTemplatesQuery, setDescribeSessionTemplatesQuery] = useState<PropertyFilterQuery>(DEFAULT_FILTERING_QUERY)
    const {loading: pageLoading, userSession} = usePageLoading()

    const sessionTemplatesTable = <SessionTemplatesTable
        selectedSessionTemplates={selectedSessionTemplates}
        setSelectedSessionTemplates={setSelectedSessionTemplates}
        addHeader={true}
        variant={"full-page"}
        allowSessionTemplateManagement={true}
        query={describeSessionTemplatesQuery}
        setQuery={setDescribeSessionTemplatesQuery}
        infoLinkFollow={() => setToolsOpen(true)}
        dataAccessServiceFunction={useSessionTemplatesService}
        columnPreferences={SESSION_TEMPLATES_DEFAULT_PREFERENCES}
        multiselect={true}
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
                content={sessionTemplatesTable}
                tools={<SessionTemplatesInfo/>}
                splitPanel={<SessionTemplatesSplitPanel sessionTemplates={selectedSessionTemplates}/>}
                toolsOpen={toolsOpen}
                onToolsChange={onToolChange}
            />
        </div>
    )
}
