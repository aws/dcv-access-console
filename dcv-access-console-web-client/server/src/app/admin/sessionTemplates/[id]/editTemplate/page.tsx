'use client'

import TopNavBar from "@/components/common/top-nav-bar/TopNavBar";
import * as React from "react";
import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";
import {Alert, AppLayout, AppLayoutProps, Flashbar, NonCancelableCustomEvent} from "@cloudscape-design/components";
import Breadcrumb from "@/components/common/breadcrumb/Breadcrumb";
import SessionTemplatesCreateWizard
    from "@/components/session-templates/create-wizard/SessionTemplatesCreateWizard";
import SessionTemplatesConfigureInfo
    from "@/components/info-panels/SessionTemplatesConfigureInfo";
import {useEffect, useState} from "react";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import {SESSION_TEMPLATES_CREATE_CONSTANTS} from "@/constants/session-templates-create-constants";
import DataAccessService from "@/components/common/utils/DataAccessService";
import usePageLoading from "@/components/common/hooks/PageLoadingHook";
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";

export default function EditSessionTemplate({params}: { params: { id: string } }) {
    const [tools, setTools] = useState<>(<SessionTemplatesConfigureInfo/>)
    const [sessionTemplateName, setSessionTemplateName] = useState<string>()
    const [alert, setAlert] = useState<JSX.Element>()
    const {loading: pageLoading, userSession} = usePageLoading()

    useEffect(() => {
        new DataAccessService().describeUsersSharedWithSessionTemplate({
            SessionTemplateId: params.id
        }).then(result => {
            if (result.data.Users?.length) {
                setAlert(<Alert statusIconAriaLabel="Warning" type="warning"> {SESSION_TEMPLATES_CREATE_CONSTANTS.EDIT_ALERT} </Alert>)
            }
        })

        new DataAccessService().describeUserGroupsSharedWithSessionTemplate({
            SessionTemplateId: params.id
        }).then(result => {
            if (result.data.UserGroups?.length) {
                setAlert(<Alert statusIconAriaLabel="Warning" type="warning"> {SESSION_TEMPLATES_CREATE_CONSTANTS.EDIT_ALERT} </Alert>)
            }
        })
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
                breadcrumbs={sessionTemplateName ? <Breadcrumb id={params.id} name={sessionTemplateName}/> : undefined}
                notifications={<div>
                    <Flashbar items={items} stackItems/>
                    {alert}
                </div>}
                navigation={
                    <SideNavPanel session={userSession}/>
                }
                maxContentWidth={Number.MAX_VALUE}
                content={
                    <SessionTemplatesCreateWizard infoLinkFollow={() => setToolsOpen(true)} setSessionTemplateName={setSessionTemplateName} existingSessionTemplateId={params.id} isEditWizard={true} setTools={setTools}/>
                }
                tools={tools}
                toolsOpen={toolsOpen}
                onToolsChange={onToolChange}
            >
            </AppLayout>
        </div>
    )
}
