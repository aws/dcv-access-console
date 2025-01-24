'use client'

import DataAccessService from "@/components/common/utils/DataAccessService";
import {
    DescribeUsersRequestData,
    FilterTokenOperatorEnum,
    User
} from "@/generated-src/client";
import {useEffect, useState} from "react";
import UserDetails from "@/components/user-management/users/user-details/UserDetails";
import TopNavBar from "@/components/common/top-nav-bar/TopNavBar";
import * as React from "react";
import {AppLayout, Flashbar} from "@cloudscape-design/components";
import Breadcrumb from "@/components/common/breadcrumb/Breadcrumb";
import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";
import usePageLoading from "@/components/common/hooks/PageLoadingHook";

export default function User({params}: { params: { id: string } }) {
    const [user, setUser] = useState<User | undefined>(undefined);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);
    const {items, addFlashBar} = useFlashBarContext()
    const {loading: pageLoading, userSession} = usePageLoading()

    const getUser = () => {
        const dataAccessService = new DataAccessService();
        const describeUsersRequest: DescribeUsersRequestData = {
            UserIds: [{
                Operator: FilterTokenOperatorEnum.Equal,
                Value: decodeURIComponent(params.id)
            }],
            MaxResults: 1
        }
        dataAccessService.describeUsers(describeUsersRequest).then(r => {
            if (r.data.Users) {
                setUser(r.data.Users[0])
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
    useEffect(() => getUser(), [])

    if(pageLoading) return <LoadingSkeleton/>
    return (
        <div>
            <TopNavBar session={userSession}/>
            <AppLayout
                toolsHide={true}
                breadcrumbs={user ? <Breadcrumb id={user.UserId} name={user.DisplayName}/> : undefined}
                notifications={<Flashbar items={items} stackItems/>}
                navigation={
                    <SideNavPanel session={userSession}/>
                }
                maxContentWidth={Number.MAX_VALUE}
                content={<UserDetails user={user} error={error} loading={loading}/>}/>
        </div>
    )
}
