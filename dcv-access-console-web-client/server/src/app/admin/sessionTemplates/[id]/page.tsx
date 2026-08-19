// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

'use client'

import DataAccessService from "@/components/common/utils/DataAccessService";
import {
    DescribeSessionTemplatesRequestData,
    FilterTokenOperatorEnum,
    SessionTemplate
} from "@/generated-src/client";
import {use, useEffect, useState} from "react";
import TopNavBar from "@/components/common/top-nav-bar/TopNavBar";
import * as React from "react";
import {AppLayout, Flashbar} from "@cloudscape-design/components";
import Breadcrumb from "@/components/common/breadcrumb/Breadcrumb";
import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import SessionTemplateDetails from "@/components/session-templates/session-template-details/SessionTemplateDetails";
import usePageLoading from "@/components/common/hooks/PageLoadingHook";
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";
import {useSession} from "next-auth/react";

export default function SessionTemplate({params}: { params: Promise<{ id: string }> }) {
    const { id } = use(params);
    const [sessionTemplate, setSessionTemplate] = useState<SessionTemplate>()
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);
    const {items, addFlashBar} = useFlashBarContext()
    const {loading: pageLoading, userSession} = usePageLoading()
    const {data: session} = useSession()
    const usingExternalAuth = session?.usingExternalAuth === true

    const getSessionTemplate = () => {
        const dataAccessService = new DataAccessService();
        const describeSessionTemplatesRequest: DescribeSessionTemplatesRequestData = {
            Ids: [
                {
                    Operator: FilterTokenOperatorEnum.Equal,
                    Value: decodeURIComponent(id)
                }
            ],
            MaxResults: 1
        }
        dataAccessService.describeSessionTemplates(describeSessionTemplatesRequest)
            .then(async r => {
                if (r.data.SessionTemplates && r.data.SessionTemplates.length > 0) {
                    const template = r.data.SessionTemplates[0]
                    if (usingExternalAuth) {
                        await dataAccessService.replaceUserIdsWithLoginUsernames([template])
                    }
                    setSessionTemplate(template)
                    setLoading(false)
                } else {
                    setLoading(false)
                    setError(true)
                }
            }).catch(() => {
                setLoading(false)
                setError(true)
            })
    }
    useEffect(() => getSessionTemplate(), [usingExternalAuth])

    if(pageLoading) return <LoadingSkeleton/>
    return (
        <div>
            <TopNavBar session={userSession}/>
            <AppLayout
                toolsHide={true}
                breadcrumbs={sessionTemplate ? <Breadcrumb id={sessionTemplate.Id} name={sessionTemplate.Name}/> : undefined}
                notifications={<Flashbar items={items} stackItems/>}
                navigation={
                    <SideNavPanel session={userSession}/>
                }
                maxContentWidth={Number.MAX_VALUE}
                content={<SessionTemplateDetails sessionTemplate={sessionTemplate} error={error} loading={loading}/>}/>
        </div>
    )
}
