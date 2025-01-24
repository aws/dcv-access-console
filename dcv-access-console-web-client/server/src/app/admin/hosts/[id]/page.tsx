'use client'

import DataAccessService from "@/components/common/utils/DataAccessService";
import {DescribeServersUIRequestData, FilterTokenOperatorEnum, Server} from "@/generated-src/client";
import {useEffect, useState} from "react";
import ServerDetails from "@/components/servers/server-details/ServerDetails";
import TopNavBar from "@/components/common/top-nav-bar/TopNavBar";
import * as React from "react";
import {AppLayout, Flashbar} from "@cloudscape-design/components";
import Breadcrumb from "@/components/common/breadcrumb/Breadcrumb";
import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import usePageLoading from "@/components/common/hooks/PageLoadingHook";
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";

export default function Host({params}: { params: { id: string } }) {
    const [server, setServer] = useState(undefined as Server);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);
    const {items, addFlashBar} = useFlashBarContext()
    const {loading: pageLoading, userSession} = usePageLoading()

    const getServer = () => {
        const dataAccessService = new DataAccessService();
        const describeServersRequest: DescribeServersUIRequestData = {
            Ids: [
                {
                    Operator: FilterTokenOperatorEnum.Equal,
                    Value: decodeURIComponent(params.id)
                }
            ],
            MaxResults: 1
        }
        dataAccessService.describeServers(describeServersRequest).then(r => {
            if (r.data.Servers) {
                setServer(r.data.Servers[0])
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
    useEffect(() => getServer(), [])

    if(pageLoading) return <LoadingSkeleton/>
    return (
        <div>
            <TopNavBar session={userSession}/>
            <AppLayout
                toolsHide={true}
                breadcrumbs={server ? <Breadcrumb id={server.Id} name={server.Hostname}/> : undefined}
                notifications={<Flashbar items={items} stackItems/>}
                navigation={
                    <SideNavPanel session={userSession}/>
                }
                maxContentWidth={Number.MAX_VALUE}
                content={<ServerDetails server={server} error={error} loading={loading}/>}/>
        </div>
    )
}
