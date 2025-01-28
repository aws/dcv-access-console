// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

'use client'

import DataAccessService from "@/components/common/utils/DataAccessService";
import {
    DescribeUserGroupsRequestData,
    FilterTokenOperatorEnum,
    UserGroup
} from "@/generated-src/client";
import {useEffect, useState} from "react";
import UserGroupDetails from "@/components/user-management/user-groups/user-group-details/UserGroupDetails";
import TopNavBar from "@/components/common/top-nav-bar/TopNavBar";
import * as React from "react";
import {AppLayout, ContentLayout, Flashbar} from "@cloudscape-design/components";
import Breadcrumb from "@/components/common/breadcrumb/Breadcrumb";
import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import usePageLoading from "@/components/common/hooks/PageLoadingHook";
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";

export default function Group({params}: { params: { id: string } }) {
    const [group, setGroup] = useState<UserGroup | undefined>(undefined);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);
    const {items, addFlashBar} = useFlashBarContext()
    const {loading: pageLoading, userSession} = usePageLoading()

    const getGroup = () => {
        const dataAccessService = new DataAccessService();
        const describeUserGroupsRequest: DescribeUserGroupsRequestData = {
            UserGroupIds: [{
                Operator: FilterTokenOperatorEnum.Equal,
                Value: decodeURIComponent(params.id)
            }],
            MaxResults: 1
        }
        dataAccessService.describeUserGroups(describeUserGroupsRequest).then(r => {
            if (r.data.UserGroups) {
                setGroup(r.data.UserGroups[0])
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
    useEffect(() => getGroup(), [])

    if(pageLoading) return <LoadingSkeleton/>
    return (
        <div>
            <TopNavBar session={userSession}/>
            <AppLayout
                toolsHide={true}
                breadcrumbs={group ? <Breadcrumb id={group.UserGroupId} name={group.DisplayName}/> : undefined}
                notifications={<Flashbar items={items} stackItems/>}
                navigation={
                    <SideNavPanel session={userSession}/>
                }
                maxContentWidth={Number.MAX_VALUE}
                content={<UserGroupDetails group={group} error={error} loading={loading}/>}/>
        </div>
    )
}
