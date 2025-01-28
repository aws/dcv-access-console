// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

'use client'

import {useEffect, useState} from "react";
import {DescribeUserGroupsRequestData, FilterTokenOperatorEnum, UserGroup} from "@/generated-src/client";
import DataAccessService from "@/components/common/utils/DataAccessService";
import EditUserGroup from "@/components/user-management/user-groups/modify-user-group/edit-user-group/EditUserGroup";
import TopNavBar from "@/components/common/top-nav-bar/TopNavBar";
import * as React from "react";
import Breadcrumb from "@/components/common/breadcrumb/Breadcrumb";
import {AppLayout, Flashbar} from "@cloudscape-design/components";
import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import {useRouter} from "next/navigation";
import {GLOBAL_CONSTANTS} from "@/constants/global-constants";
import usePageLoading from "@/components/common/hooks/PageLoadingHook";
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";

export default function EditGroup({params}:  {params: {id: string}}) {
    const [group, setGroup] = useState<UserGroup>()
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(false)
    const {loading: pageLoading, userSession} = usePageLoading()
    const {items} = useFlashBarContext()
    const router = useRouter()

    const getGroup = () => {
        const dataAccessService = new DataAccessService()
        const describeUserGroupsRequest: DescribeUserGroupsRequestData = {
            UserGroupIds: [
                {
                    Operator: FilterTokenOperatorEnum.Equal,
                    Value: decodeURIComponent(params.id)
                }
            ],
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
    useEffect(() => {
        params.id && getGroup()
    }, [params.id])

    const redirectToUserGroupsPageFunction = () => {
        console.log("Pushing to view user groups page")
        router.push(GLOBAL_CONSTANTS.USER_GROUPS_URL)
    }

    const cancelEditUserGroupsPageFunction = () => {
        console.log("Cancelling edit users page")
        router.back()
    }

    if(pageLoading) return <LoadingSkeleton/>
    return (
        <div>
            <TopNavBar session={userSession}/>
            <AppLayout
                breadcrumbs={group ? <Breadcrumb id={params.id} name={group?.DisplayName}/> : undefined}
                notifications={<Flashbar items={items} stackItems/>}
                maxContentWidth={Number.MAX_VALUE}

                content={<EditUserGroup
                    group={group}
                    error={error}
                    loading={loading}
                    redirectToUserGroupsPageFunction={redirectToUserGroupsPageFunction}
                    cancelEditUserGroupsPageFunction={cancelEditUserGroupsPageFunction}
                />}
                navigation={
                    <SideNavPanel session={userSession}/>
                }
                toolsHide={true}
            />
        </div>
    )
}
