// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import * as React from "react";
import {ReactNode, useEffect, useState} from "react";
import Cards, {CardsProps} from "@cloudscape-design/components/cards";
import Box from "@cloudscape-design/components/box";
import Pagination from "@cloudscape-design/components/pagination";
import {SESSIONS_CONSTANTS} from "@/constants/sessions-constants";
import {SESSION_CARDS_DEFAULT_PREFERENCES} from "@/components/sessions/SessionsPreferences.tsx";
import {
    DescribeSessionsUIRequestData,
    SessionWithPermissions
} from "@/generated-src/client";
import {CancelableEventHandler, NonCancelableEventHandler} from "@cloudscape-design/components/internal/events";
import {SessionsState} from "@/app/sessions/page";
import './SessionCards.css'
import {
    CollectionPreferences,
    CollectionPreferencesProps, Icon,
    SpaceBetween, Spinner
} from "@cloudscape-design/components";
import ConsoleHeader from "@/components/common/console-header/ConsoleHeader";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {GLOBAL_CONSTANTS} from "@/constants/global-constants";
import DeleteSessionsModal from "@/components/sessions/delete-sessions/DeleteSessionsModal";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import {SessionScreenshotState} from "@/components/common/utils/SessionScreenshotState";
import StatusLabel from "@/components/sessions/sessions-cards/status-label/StatusLabel";
import {PropertyFilterQuery} from "@cloudscape-design/collection-hooks";
import LaunchSessionModal from "@/components/sessions/launch-session-modal/LaunchSessionModal";
import {LinkProps} from "@cloudscape-design/components/link/interfaces";
import {GENERIC_TABLE_CONSTANTS} from "@/constants/generic-table-constants";
import {SPECIAL_OPERATORS} from "@/components/common/utils/SearchUtils";
import {
    DEFAULT_PAGE_SIZE_PREFERENCES
} from "@/components/common/table-with-pagination/PaginationTableColumnPreferences";
import type {ResponseData} from "@/app/api/config/route";
import {SESSION_SCREENSHOT_CONSTANTS} from "@/constants/session-screenshot-constants";

export type SessionCardsProps = {
    getViewForm: (value: string, setValue: (string) => void) => JSX.Element
    setView: (view : string) => void
    sessionsState: SessionsState
    onSelectionChange: NonCancelableEventHandler<CardsProps.SelectionChangeDetail<SessionWithPermissions>>
    setSessionsState: (sessionsState: any) => void
    describeSessionsQuery: PropertyFilterQuery
    getFilter: (resetPaginationFunc: () => void) => JSX.Element
    createButton: ReactNode
    actions: (session: SessionWithPermissions) => ReactNode
    connectButton: (session: SessionWithPermissions) => ReactNode
    deleteModal: DeleteSessionsModal
    launchModal: LaunchSessionModal
    empty: ReactNode
    userSession
    infoLinkFollow: CancelableEventHandler<LinkProps.FollowDetail>
}

export default function SessionCards({getViewForm,
                                setView,
                                sessionsState,
                                onSelectionChange,
                                setSessionsState,
                                describeSessionsQuery,
                                getFilter,
                                createButton,
                                actions,
                                connectButton,
                                deleteModal,
                                launchModal,
                                empty,
                                userSession,
                                infoLinkFollow,
                             }: SessionCardsProps) {
    const getDescribeSessionsUIRequestData : () => DescribeSessionsUIRequestData = () => {
        let request: DescribeSessionsUIRequestData = {MaxResults: preferences.pageSize,
            NextToken: sessionsPageTokens[sessionsPage - 1]} as DescribeSessionsUIRequestData
        describeSessionsQuery.tokens.forEach(token => {
            let key = token.propertyKey as keyof DescribeSessionsUIRequestData
            let operator: string = SPECIAL_OPERATORS.get(token.operator) || token.operator

            let filterToken = {
                Operator: operator,
                Value: token.value
            }

            request[key] ? (request[key] as []).push(filterToken) : request[key] = [filterToken]
        })
        return request
    }

    const [sessionsPage, setSessionsPage] = useState(1)
    const [sessionsPageTokens, setSessionsPageTokens] = useState([null])
    const [preferences, setPreferences] = useState<CollectionPreferencesProps.Preferences>(SESSION_CARDS_DEFAULT_PREFERENCES)
    const [pageSizePreferences, setPageSizePreferences] = useState<CollectionPreferencesProps.PageSizePreference>(DEFAULT_PAGE_SIZE_PREFERENCES)
    const [screenshotReady, setScreenshotReady] = useState<Map<string, SessionScreenshotState>>(new Map())
    const [pagesCount, setPagesCount] = useState(1)
    const [openEnd, setOpenEnd] = useState(true)
    const [totalCount, setTotalCount] = useState(0)
    const [describeSessionsRequest, setDescribeSessionsRequest] = useState(getDescribeSessionsUIRequestData)
    const dataAccessService = new DataAccessService()

    const getSessionsWithFilter = (describeSessionsRequest: DescribeSessionsUIRequestData) => {
        console.log(describeSessionsRequest)
        dataAccessService.describeSessions(describeSessionsRequest)
            .then(r => {
                setSessionsState(prevState => {
                    return {
                        ...prevState,
                        sessions: r.data.Sessions ? r.data.Sessions : [],
                        loading: false,
                        error: false
                    }
                })

                if (sessionsPage == sessionsPageTokens.length) {
                    setSessionsPageTokens([...sessionsPageTokens, r.data.NextToken])

                    if (sessionsPage == 1) {
                        setTotalCount(r.data.Sessions.length)
                    } else {
                        setTotalCount(prevState => prevState + r.data.Sessions.length)
                    }
                }
            }).catch(e => {
            console.error("Failed to retrieve sessions: ", e)
            setSessionsState(prevState => {
                return {
                    ...prevState,
                    loading: false,
                    error: true,
                    errorMessage: e.errorMessage
                }
            })
        })
    }

    useEffect(() => {
        if (sessionsPageTokens.length > 1 && sessionsPageTokens[sessionsPageTokens.length - 1] == null) {
            setOpenEnd(false)
            setPagesCount(sessionsPageTokens.length - 1)
        } else {
            setPagesCount(sessionsPageTokens.length)
        }
    }, [sessionsPageTokens])

    const getScreenshotWithDefaultAndFailed = (session: SessionWithPermissions | undefined) => {
        if(session?.Id && screenshotReady.get(session.Id) == SessionScreenshotState.READY && sessionsState.screenshotsMap?.sessionScreenshotMap.get(session.Id)?.Data) {
            const imgData = "data:image/" + sessionsState.screenshotsMap?.sessionScreenshotMap.get(session.Id)?.Format + ";base64," +  sessionsState.screenshotsMap?.sessionScreenshotMap.get(session.Id)?.Data
            const img = <image
                            href={imgData}
                            alt={session.Id + " screenshot"}
                            width="100%" height="100%"
                            role="button"
                            onClick={
                                () => {
                                    if (session?.LevelOfAccess === "Admin") {
                                        dataAccessService.launchSession("web", session.Id!, session.Owner)
                                    } else {
                                        dataAccessService.launchSession("web", session!.Id!)
                                    }
                                }
                            }
                        />
            return <div>
                <div className={"sessions-card-screenshot-success"}>
                    <svg
                        viewBox="0 0 946 488"
                    >
                        {img}
                    </svg>
                </div>
            </div>
        }
        if (session?.Id && screenshotReady.get(session.Id) == SessionScreenshotState.FAILED && session.State != "CREATING") {
            return <div className="sessions-card-screenshot-non-success sessions-card-screenshot">
                <SpaceBetween size={"xxs"} direction={"horizontal"}>
                    <Icon name="status-warning" size="medium" variant={"warning"}/>
                    <span className={"preview-loading-text"}><Box color={"inherit"}>Preview currently not available</Box></span>
                </SpaceBetween>
            </div>
        }
        return <div className="sessions-card-screenshot-non-success sessions-card-screenshot">
            <SpaceBetween size={"xxs"} direction={"horizontal"}>
                <Spinner size={"normal"} variant={"inverted"}/>
                <span className={"preview-loading-text"}><Box color={"inherit"}>Preview loading</Box></span>
            </SpaceBetween>
        </div>
    }

    useEffect(() => {
        setDescribeSessionsRequest(getDescribeSessionsUIRequestData)
    }, [describeSessionsQuery])

    useEffect(() => {
        setSessionsState(prevState => {
            return {
                ...prevState,
                loading: true
            }
        })
        getSessionsWithFilter(describeSessionsRequest)
    }, [describeSessionsRequest])

    useEffect(() => {
        // No need to set loading to true to avoid flashes on the screen
        getSessionsWithFilter(describeSessionsRequest)
    }, [useFlashBarContext().items])

    // Get screenshots whenever there is a change in sessions
    useEffect(() => {
        fetch(SESSION_SCREENSHOT_CONSTANTS.CONFIG_ENDPOINT)
            .then(res => res.json())
            .then((config: ResponseData) => {
                dataAccessService.getSessionScreenshots(sessionsState.sessions, setScreenshotReady, config.sessionScreenshotMaxWidth, config.sessionScreenshotMaxHeight).then((screenshotsMap) => {
                    setSessionsState(prevState => {
                        return {
                            ...prevState,
                            screenshotsMap: screenshotsMap
                        }
                    })
                }).catch(e => {
                    console.error("Failed to retrieve sessionScreenshots: ", e)
                })
            })
    }, [sessionsState.sessions])

    useEffect(() => {
        resetPagination()
        setDescribeSessionsRequest(prevState => {
            return {
                ...prevState,
                NextToken: null,
                MaxResults: preferences.pageSize
            } as DescribeSessionsUIRequestData
        })
    }, [preferences.pageSize])

    const resetPagination = () => {
        setSessionsPageTokens([null])
        setSessionsPage(1)
        setTotalCount(0)
    }

    const handlePageChange = event => {
        setSessionsPage(event.detail.currentPageIndex);
        setDescribeSessionsRequest(prevState => {
            return {
                ...prevState,
                NextToken: sessionsPageTokens[event.detail.currentPageIndex - 1]
            }
        })
    }

    const header= <ConsoleHeader counter={totalCount || 0}
                       headerDescription={SESSIONS_CONSTANTS.HEADER_DESCRIPTION}
                       headerTitle={SESSIONS_CONSTANTS.SESSIONS}
                       infoLinkFollow={infoLinkFollow}
                       infoLinkLabel={GLOBAL_CONSTANTS.INFO_LABEL}
                       actions={<SpaceBetween direction="horizontal" size="xs">
                           {createButton}
                       </SpaceBetween>}
                       openEnd={openEnd}
                       loading={sessionsState.loading}
        />

    return (<>
            <Cards
                entireCardClickable
                header={header}
                filter={getFilter(resetPagination)}
                variant={"full-page"}
                items={sessionsState.sessions}
                selectedItems={sessionsState.selectedSession ? [sessionsState.selectedSession] : []}
                onSelectionChange={onSelectionChange}
                loadingText="Loading Sessions"
                loading={sessionsState.loading}
                pagination={
                    <Pagination
                        currentPageIndex={sessionsPage}
                        onChange={handlePageChange}
                        pagesCount={pagesCount}
                        openEnd={openEnd}
                        disabled={sessionsState.loading}
                    />
                }
                cardDefinition={{
                    header: (session: SessionWithPermissions) => {
                        return <div data-session-id={session.Id}>{session.Name}</div>
                    },
                    sections: [
                        {
                            id: "screenshot",
                            content: (session: SessionWithPermissions) => getScreenshotWithDefaultAndFailed(session)
                        },
                        {
                            id: "status",
                            header: "Status",
                            content: (session: SessionWithPermissions) => <StatusLabel state={session.State}/>,
                            width: 30
                        },
                        {
                            id: "actions",
                            content: (session: SessionWithPermissions) => <div className={"sessions-card-right-section"}>
                                <SpaceBetween direction ={"horizontal"} size={"xs"}>
                                    {connectButton(session)}
                                    {actions(session)}
                                </SpaceBetween>
                            </div>,
                            width: 70

                        },
                        {
                            id: "role",
                            content: (session: SessionWithPermissions) => {
                                return <SpaceBetween size={"xxxs"} direction={"vertical"} alignItems={"start"}>
                                    <Box variant={"awsui-key-label"} float={"left"} margin={"n"} padding={"n"}>Your level of access</Box>
                                    <Box variant={"span"} float={"left"} margin={"n"} padding={"n"}>{session.LevelOfAccess}</Box>
                                </SpaceBetween>
                            },
                            width: 50
                        },
                        {
                            id: "owner",
                            content: (session: SessionWithPermissions) => {
                                if(session.Owner != userSession.user.id) {
                                    return <SpaceBetween size={"xxxs"} direction={"vertical"} alignItems={"end"}>
                                            <Box variant={"awsui-key-label"} float={"right"} margin={"n"} padding={"n"}>Owner</Box>
                                            <Box variant={"span"} float={"right"} margin={"n"} padding={"n"}>{session.Owner}</Box>
                                        </SpaceBetween>
                                }},
                            width: 50
                        }
                    ]
                }}
                    cardsPerRow={[
                        {cards: 1},
                        {minWidth: 500, cards: 2},
                        {minWidth: 1000,cards: 3}
                    ]}
                    selectionType="single"
                    trackBy="Id"
                    visibleSections={["screenshot", "status", "actions", "role", "owner", "connect"]}
                    empty={empty}
                    ariaLabels={{
                        itemSelectionLabel: (e, n) => `${n.Name}`,
                        selectionGroupLabel: "Select"
                    }}
                    preferences={
                        <CollectionPreferences onConfirm={({detail}) => {
                            setPreferences(detail)
                            setView(detail.custom)
                        }}
                                               preferences={preferences}
                                               pageSizePreference={pageSizePreferences}
                                               customPreference={(radioValue, setRadioValue) => getViewForm(radioValue, setRadioValue)}
                                               title={GENERIC_TABLE_CONSTANTS.COLLECTION_PREFERENCES}
                                               cancelLabel={GENERIC_TABLE_CONSTANTS.COLLECTION_PREFERENCES_CANCEL}
                                               confirmLabel={GENERIC_TABLE_CONSTANTS.COLLECTION_PREFERENCES_CONFIRM}
                        />
                    }
                />
            {deleteModal}
            {launchModal}
        </>
    )
}
