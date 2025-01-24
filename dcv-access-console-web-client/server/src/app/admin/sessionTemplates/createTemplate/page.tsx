'use client'

import TopNavBar from "@/components/common/top-nav-bar/TopNavBar";
import * as React from "react";
import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";
import {AppLayout, AppLayoutProps, Flashbar, NonCancelableCustomEvent} from "@cloudscape-design/components";
import Breadcrumb from "@/components/common/breadcrumb/Breadcrumb";
import SessionTemplatesCreateWizard
    from "@/components/session-templates/create-wizard/SessionTemplatesCreateWizard";
import SessionTemplatesConfigureInfo
    from "@/components/info-panels/SessionTemplatesConfigureInfo";
import {useEffect, useState} from "react";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import {useRouter, useSearchParams} from "next/navigation";
import {SESSION_TEMPLATES_CREATE_CONSTANTS} from "@/constants/session-templates-create-constants";
import usePageLoading from "@/components/common/hooks/PageLoadingHook";
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";

export default function CreateSessionTemplate() {
    const [tools, setTools] = useState<>(<SessionTemplatesConfigureInfo/>)
    const searchParams = useSearchParams()
    const {replace} = useRouter()
    const {loading: pageLoading, userSession} = usePageLoading()

    useEffect(() => {
        if (searchParams.get("templateId") == undefined) {
            replace(SESSION_TEMPLATES_CREATE_CONSTANTS.CREATE_TEMPLATE_URL)
        }
    }, [])
    const {items} = useFlashBarContext()

    const [toolsOpen, setToolsOpen] = useState<boolean>(false)
    const onToolChange = (event: NonCancelableCustomEvent<AppLayoutProps.ChangeDetail>): void => {
        setToolsOpen(event.detail.open);
    }

    if(pageLoading) return <LoadingSkeleton/>
    return (
        <div>
            <TopNavBar session={userSession}/>
            <AppLayout
                breadcrumbs={<Breadcrumb/>}
                notifications={<Flashbar items={items} stackItems/>}
                navigation={
                    <SideNavPanel session={userSession}/>
                }
                maxContentWidth={Number.MAX_VALUE}
                content={
                    <SessionTemplatesCreateWizard infoLinkFollow={() => setToolsOpen(true)} existingSessionTemplateId={searchParams.get("templateId")} isEditWizard={false} setTools={setTools}/>
                }
                tools={tools}
                toolsOpen={toolsOpen}
                onToolsChange={onToolChange}
            >
            </AppLayout>
        </div>
    )
}
