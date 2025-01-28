// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

'use client'

import DataAccessService from "@/components/common/utils/DataAccessService";
import {
    DescribeSessionTemplatesRequestData,
    FilterTokenOperatorEnum,
    SessionTemplate
} from "@/generated-src/client";
import {useEffect, useState} from "react";
import TopNavBar from "@/components/common/top-nav-bar/TopNavBar";
import * as React from "react";
import {AppLayout, Flashbar} from "@cloudscape-design/components";
import Breadcrumb from "@/components/common/breadcrumb/Breadcrumb";
import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import SessionTemplateDetails from "@/components/session-templates/session-template-details/SessionTemplateDetails";
import usePageLoading from "@/components/common/hooks/PageLoadingHook";
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";

export default function SessionTemplate({params}: { params: { id: string } }) {
    const [sessionTemplate, setSessionTemplate] = useState<SessionTemplate>()
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);
    const {items, addFlashBar} = useFlashBarContext()
    const {loading: pageLoading, userSession} = usePageLoading()

    const getSessionTemplate = () => {
        const dataAccessService = new DataAccessService();
        const describeSessionTemplatesRequest: DescribeSessionTemplatesRequestData = {
            Ids: [
                {
                    Operator: FilterTokenOperatorEnum.Equal,
                    Value: decodeURIComponent(params.id)
                }
            ],
            MaxResults: 1
        }
        dataAccessService.describeSessionTemplates(describeSessionTemplatesRequest).then(r => {
            if (r.data.SessionTemplates) {
                setSessionTemplate(r.data.SessionTemplates[0])
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
    useEffect(() => getSessionTemplate(), [])

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
