'use client'

import * as React from "react";
import {useEffect, useState} from "react";
import TopNavBar from "@/components/common/top-nav-bar/TopNavBar";
import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";
import {
    AppLayout,
    AppLayoutProps,
    Flashbar,
    FormField,
    NonCancelableCustomEvent,
    RadioGroup
} from "@cloudscape-design/components";
import {DeleteSessionUIRequestData, SessionWithPermissions, State} from "@/generated-src/client";
import DataAccessService, {ScreenshotsMap} from "@/components/common/utils/DataAccessService";
import SessionsSplitPanel from "@/components/common/sessions-split-panel/SessionsSplitPanel";
import SessionCards from "@/components/sessions/sessions-cards/SessionCards";
import SessionInfo from "@/components/info-panels/SessionInfo";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import {PropertyFilterQuery, PropertyFilterToken} from "@cloudscape-design/collection-hooks";
import usePageLoading from "@/components/common/hooks/PageLoadingHook";
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";
import DeleteSessionsModal, {DeleteSessionProps} from "@/components/sessions/delete-sessions/DeleteSessionsModal";
import FilterBar, {DescribeResponse} from "@/components/common/filter-bar/FilterBar";
import sessions_search_tokens from "@/generated-src/client/session_search_tokens";
import {
    FILTER_SESSIONS_CONSTANTS,
    SEARCH_TOKEN_TO_ID,
    TOKEN_NAME_GROUP_MAP,
    TOKEN_NAMES_MAP
} from "@/constants/filter-sessions-bar-constants";
import {GLOBAL_CONSTANTS} from "@/constants/global-constants";
import ButtonDropdown from "@cloudscape-design/components/button-dropdown";
import LaunchSessionModal, {LaunchSessionProps} from "@/components/sessions/launch-session-modal/LaunchSessionModal";
import {getNativeOsName} from "@/components/common/utils/ClientUtils";
import {COOKIE_CONSTANTS} from "@/constants/cookie-constants";
import Button from "@cloudscape-design/components/button";
import {SESSIONS_CONSTANTS} from "@/constants/sessions-constants";
import {useRouter} from "next/navigation";
import Box from "@cloudscape-design/components/box";
import SessionsTable from "@/components/sessions/sessions-table/SessionsTable";
import {useSessionsService} from "@/components/common/hooks/DataAccessServiceHooks";
import {isAdminRoleFromSession} from "@/components/common/utils/TokenAccessService";
import {VALUE_TO_LABELS} from "@/components/common/utils/SearchUtils";

export type SessionsState = {
    sessions: SessionWithPermissions[]
    selectedSession?: SessionWithPermissions
    screenshotsMap?: ScreenshotsMap
    loading: boolean
    error: boolean
    errorMessage?: string
}

export default function Sessions() {
    const [toolsOpen, setToolsOpen] = useState<boolean>(false)
    const {loading, userSession} = usePageLoading()
    const [view, setView] = useState<string>()
    const [deleteSessionProps, setDeleteSessionProps] = useState<DeleteSessionProps>()
    const [launchingSessionProps, setLaunchingSessionProps] = useState<LaunchSessionProps>()
    const [deleteSessionModalVisible, setDeleteSessionModalVisible] = useState<boolean>(false)
    const [launchSessionModalVisible, setLaunchSessionModalVisible] = useState<boolean>(false)
    const [deleteItemsKey, setDeleteItemsKey] = useState("")
    const [describeSessionsQuery, setDescribeSessionsQuery] = useState<PropertyFilterQuery>({
        tokens: [{
            propertyKey: "States",
            operator: "!=",
            value: "DELETED"
        } as PropertyFilterToken],
        operation: "and"
    })
    const [describeSessionsDisplayQuery, setDescribeSessionsDisplayQuery] = useState<PropertyFilterQuery>({
        tokens: [{
            propertyKey: "States",
            operator: "!=",
            value: SESSIONS_CONSTANTS.CLOSED
        } as PropertyFilterToken],
        operation: "and"
    })
    const [sessionsState, setSessionsState] = useState<SessionsState>({
        sessions: [],
        loading: true,
        error: false
    })
    const {addDeletingSessionFlashBar, addDeletedSessionFailedFlashBar} = useFlashBarContext()
    const {push} = useRouter()

    useEffect(() => {
        setView(isAdminRoleFromSession(userSession!) ? "table" : "cards")
    }, [loading])

    const getViewForm = (value : string, setValue : (string) => void) => <FormField label="View as">
        <RadioGroup
            onChange={({ detail }) => setValue(detail.value)}
            value={value}
            items={[
                { value: "table", label: "Table" },
                { value: "cards", label: "Cards" }
            ]}
        />
    </FormField>

    const dataAccessService = new DataAccessService()
    const closeSession = ({sessionId, sessionName, owner}: DeleteSessionProps) => {
        let deleteSessionsRequest: DeleteSessionUIRequestData = {
            SessionId: sessionId,
            Owner: owner
        } as DeleteSessionUIRequestData
        dataAccessService.deleteSessions([deleteSessionsRequest]).then(r => {
            console.log("Deleting session: ", r);
            if (r.data["Error"]?.message) {
                addDeletedSessionFailedFlashBar(sessionId!, sessionName!, r.data["Error"].message)
            }
            if (r.data["SuccessfulList"]) {
                for (const success of r.data["SuccessfulList"]) {
                    addDeletingSessionFlashBar(success.SessionId!, sessionName!);
                }
            }
            if (r.data["UnsuccessfulList"]) {
                for (const failure of r.data["UnsuccessfulList"]) {
                    addDeletedSessionFailedFlashBar(failure.SessionId!, sessionName!, JSON.stringify(failure.FailureReasons))
                }
            }
            setDeleteItemsKey(Date.now().toString())
        }).catch(e => {
            addDeletedSessionFailedFlashBar(sessionId!, sessionName!, "Unknown error")
            console.error("Failed to delete session: ", sessionId, e);
        })
    }

    const describeSessions = async (describeSessionsRequest) => {
        const r = await dataAccessService.describeSessions(describeSessionsRequest)
        return {"objects": r.data.Sessions, "nextToken": r.data.NextToken} as DescribeResponse
    }

    const getFilter = (resetPaginationFunc) => <FilterBar
        filteringQuery={describeSessionsDisplayQuery}
        handlePropertyFilteringChange={({detail}) => {
            setDescribeSessionsQuery(detail)
            setDescribeSessionsDisplayQuery({
                tokens: detail.tokens.map(token => {
                    return {
                        propertyKey: token.propertyKey,
                        value: VALUE_TO_LABELS.has(token.value) ? VALUE_TO_LABELS.get(token.value) : token.value,
                        operator: token.operator
                    }
                }),
                operation: detail.operation
            })
            resetPaginationFunc()
        }}
        searchTokens={sessions_search_tokens}
        tokenNamesMap={TOKEN_NAMES_MAP}
        tokenNameGroupMap={TOKEN_NAME_GROUP_MAP}
        searchTokenToId={SEARCH_TOKEN_TO_ID}
        filteringPlaceholder={FILTER_SESSIONS_CONSTANTS.filteringPlaceholder}
        dataAccessServiceFunction={describeSessions}/>

    const actions = (session: SessionWithPermissions | undefined) =>
    <ButtonDropdown items={[
        {
            id: "connect",
            text: "Connect using",
            items: [
                {
                    text: "Web browser",
                    id: "web:" + session?.Id,
                    disabled: typeof session?.Server == "undefined"
                },
                {
                    text: getNativeOsName() + " client",
                    id: "native:" + session?.Id,
                    disabled: typeof session?.Server == "undefined"
                }
            ],
            disabled: session?.State !== "READY"
        },
        {
            id: "close",
            text: "Close",
            disabled: session?.State == State.Deleting || session?.State == State.Deleted
        },
    ]}
        disabled={!session}
        expandableGroups
        onItemClick={(event) => {
            if (event.detail.id.includes(':')) {
                const id = event.detail.id.split(':')
                setLaunchingSessionProps({
                    sessionName: session?.Name,
                    clientName: getNativeOsName(),
                    webclientUrl: "#"
                })
                if (id[0] === "native" && localStorage.getItem(COOKIE_CONSTANTS.DONT_ASK_FOR_INSTALLATION_COOKIE_NAME) !== "true") {
                    setLaunchSessionModalVisible(true)
                }
                if (session?.LevelOfAccess === "Admin") {
                    dataAccessService.launchSession(id[0], id[1], session.Owner)
                    dataAccessService.getLaunchSessionUrl("web", id[1], session.Owner).then(webclientUrl => {
                        setLaunchingSessionProps(prevState => {
                            return {...prevState, webclientUrl: webclientUrl}
                        })
                    })
                } else {
                    dataAccessService.launchSession(id[0], id[1])
                    dataAccessService.getLaunchSessionUrl("web", id[1]).then(webclientUrl => {
                        setLaunchingSessionProps(prevState => {
                            return {...prevState, webclientUrl: webclientUrl}
                        })
                    })
                }
            } else if (event.detail.id.includes("close")) {
                setDeleteSessionProps({
                    sessionId: session?.Id,
                    owner: session?.Owner,
                    sessionName: session?.Name
                })
                setDeleteSessionModalVisible(true)
            }
        }}
    >
        {GLOBAL_CONSTANTS.ACTIONS}
    </ButtonDropdown>

    const connectButton = (session: SessionWithPermissions | undefined) => <Button variant="primary"
        disabled={!session || session?.State !== "READY"} onClick={
        () => {
            if (session?.LevelOfAccess === "Admin") {
                dataAccessService.launchSession("web", session.Id!, session.Owner)
            } else {
                dataAccessService.launchSession("web", session?.Id!)
            }
        }
    }>
        Connect
    </Button>

    const createButton = <Button variant="primary" onClick={() => {
        push(SESSIONS_CONSTANTS.CREATE_SESSION_URL)
    }}>{SESSIONS_CONSTANTS.CREATE_SESSION_TEXT}</Button>

    const deleteModal = <DeleteSessionsModal visible={deleteSessionModalVisible} setVisible={setDeleteSessionModalVisible} closeSession={closeSession} deleteSessionProps={deleteSessionProps!}/>
    const launchModal = <LaunchSessionModal visible={launchSessionModalVisible} setVisible={setLaunchSessionModalVisible} launchingSessionProps={launchingSessionProps}/>

    const empty = <Box textAlign="center" color="inherit">
        <Box
            padding={{ bottom: "s" }}
            variant="p"
            color="inherit"
        >
            <b>{SESSIONS_CONSTANTS.EMPTY_TEXT}</b>
        </Box>
        <Button onClick={() => {push(SESSIONS_CONSTANTS.CREATE_SESSION_URL)}}>{SESSIONS_CONSTANTS.CREATE_SESSION_TEXT}</Button>
    </Box>

    const sessionCards = <SessionCards
        getViewForm={getViewForm}
        setView={setView}
        sessionsState={sessionsState}
        onSelectionChange={(change) => {
            setSessionsState(prevState => {
                return {
                    ...prevState,
                    selectedSession: change.detail.selectedItems[0]
                }
            })
        }}
        setSessionsState={setSessionsState}
        describeSessionsQuery={describeSessionsQuery}
        getFilter={getFilter}
        createButton={createButton}
        actions={actions}
        connectButton={connectButton}
        deleteModal={deleteModal}
        launchModal={launchModal}
        empty={empty}
        userSession={userSession}
        infoLinkFollow={() => setToolsOpen(true)}
    />

    const sessionsTable = <SessionsTable
        getViewForm={getViewForm}
        setView={setView}
        deleteItemsKey={deleteItemsKey}
        sessionsState={sessionsState}
        describeSessionsQuery={describeSessionsQuery}
        getFilter={getFilter}
        createButton={createButton}
        actions={actions}
        connectButton={connectButton}
        deleteModal={deleteModal}
        launchModal={launchModal}
        empty={empty}
        userSession={userSession}
        infoLinkFollow={() => setToolsOpen(true)}
        setSessionsState={setSessionsState}
        dataAccessServiceFunction={useSessionsService}/>

    const sessionInfo = <SessionInfo/>

    const {items} = useFlashBarContext()

    const onToolChange = (event: NonCancelableCustomEvent<AppLayoutProps.ChangeDetail>): void => {
        setToolsOpen(event.detail.open);
    }
    if(loading) return <LoadingSkeleton/>
    return (
        <div>
            <TopNavBar session={userSession}/>
            <AppLayout
                notifications={<Flashbar items={items} stackItems/>}
                navigation={
                    <SideNavPanel session={userSession}/>
                }
                maxContentWidth={Number.MAX_VALUE}
                content={view === "table" ? sessionsTable : sessionCards}
                tools={sessionInfo}
                splitPanel={<SessionsSplitPanel
                    selectedSession={sessionsState.selectedSession}/>}
                toolsOpen={toolsOpen}
                onToolsChange={onToolChange}
            >
            </AppLayout>
        </div>
    )
}
