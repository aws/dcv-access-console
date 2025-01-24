'use client'

import TopNavBar from "@/components/common/top-nav-bar/TopNavBar";
import * as React from "react";
import Breadcrumb from "@/components/common/breadcrumb/Breadcrumb";
import {AppLayout, Flashbar} from "@cloudscape-design/components";
import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import CreateUserGroup from "@/components/user-management/user-groups/modify-user-group/create-user-group/CreateUserGroup";
import {useRouter} from "next/navigation";
import {GLOBAL_CONSTANTS} from "@/constants/global-constants";
import usePageLoading from "@/components/common/hooks/PageLoadingHook";
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";

export default function CreateGroup() {
    const router = useRouter()
    const {loading: pageLoading, userSession} = usePageLoading()
    const {items} = useFlashBarContext()

    const redirectToUserGroupsPageFunction = () => {
        console.log("Pushing to view user groups page")
        router.push(GLOBAL_CONSTANTS.USER_GROUPS_URL)
    }

    const cancelCreateUserGroupsPageFunction = () => {
        console.log("Cancelling edit users page")
        router.back()
    }

    if(pageLoading) return <LoadingSkeleton/>
    return (
        <div>
            <TopNavBar session={userSession}/>
            <AppLayout
                breadcrumbs={<Breadcrumb/>}
                notifications={<Flashbar items={items} stackItems/>}
                maxContentWidth={Number.MAX_VALUE}

                content={<CreateUserGroup
                    redirectToUserGroupsPageFunction={redirectToUserGroupsPageFunction}
                    cancelCreateUserGroupsPageFunction={cancelCreateUserGroupsPageFunction}
                />}
                navigation={
                    <SideNavPanel session={userSession}/>
                }
                toolsHide={true}
            />
        </div>
    )
}
