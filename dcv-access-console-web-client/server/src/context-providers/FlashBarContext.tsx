// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

'use client';

import {createContext, useContext, useState} from 'react';
import {FlashbarProps} from "@cloudscape-design/components/flashbar/interfaces";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {FilterToken, FilterTokenOperatorEnum, SessionWithPermissions, State} from "@/generated-src/client";
import {useInterval} from "@/components/common/hooks/IntervalHooks";
import Button from "@cloudscape-design/components/button";

export type FlashBarContextProps = {
    items: FlashbarProps.MessageDefinition[],
    addSessionFlashBar: (sessionId: string, sessionName: string) => void,
    addDeletedSessionFlashBar: (sessionId: string, sessionName: string) => void,
    addDeletingSessionFlashBar: (sessionId: string, sessionName: string) => void,
    addDeletedSessionFailedFlashBar: (sessionId: string, sessionName: string, reason: string) => void,
    addCreateSessionFailedFlashBar: (sessionName: string, reason: string) => void,
    addFlashBar: (type: FlashbarProps.Type, id: string | undefined, message: string) => void,
    addLoadingFlashBar: (id: string | undefined, message: string) => void,
    removeFromFlashBar: (id: string | undefined, type: FlashBarType) => void,
}
export type FlashBarType = "session" | "sessionTemplate" | "message"
const FlashBarContext = createContext<FlashBarContextProps>({
    items: [] as FlashbarProps.MessageDefinition[],
    addSessionFlashBar: (sessionId: string, sessionName: string) => {
    },
    addDeletingSessionFlashBar: (sessionId: string, sessionName: string) => {
    },
    addDeletedSessionFlashBar: (sessionId: string, sessionName: string) => {
    },
    addDeletedSessionFailedFlashBar: (sessionId: string, sessionName: string, reason: string) => {
    },
    addCreateSessionFailedFlashBar: (sessionName: string, reason: string) => {
    },
    addFlashBar: (type: FlashbarProps.Type, id: string | undefined, message: string) => {
    },
    addLoadingFlashBar: (id: string | undefined, message: string) => {
    },
    removeFromFlashBar: (id: string | undefined, type: FlashBarType) => {}
})

export const FlashBarContextProvider = ({children}) => {
    const [items, setItems] = useState([] as FlashbarProps.MessageDefinition[])
    const [loopDelay, setLoopDelay] = useState(null as Number)
    const [sessionsInProgress, setSessionInProgress] = useState([] as string[])

    const addFlashBar = (type: FlashbarProps.Type, id: string | undefined, message: string): void => {
        setItems((items) => {
            return [...items, {
                id: id,
                type: type,
                loading: false,
                content: message,
                dismissible: true,
                dismissLabel: 'Dismiss message',
                onDismiss: (event) => {
                    removeFromFlashBar(id, "message")
                }
            } as FlashbarProps.MessageDefinition]
        })
    }

    const addLoadingFlashBar = (id: string | undefined, message: string): void => {
        setItems((items) => {
            return [...items, {
                id: id,
                type: "in-progress",
                loading: true,
                content: message,
                dismissible: true,
                dismissLabel: 'Dismiss message',
                onDismiss: (event) => {
                    removeFromFlashBar(id, "message")
                }
            } as FlashbarProps.MessageDefinition]
        })
    }

    const addSessionFlashBar = (sessionId: string, sessionName: string): void => {
        console.log("Flash Bar: Adding sessionId to flash bar", sessionId)
        setSessionInProgress([sessionId])
        setItems(items => {
            return [...items, {
                id: sessionId,
                type: 'success',
                loading: true,
                content: 'Creating your "' + sessionName + '" session.',
                dismissible: true,
                dismissLabel: 'Dismiss message',
                onDismiss: (event) => {
                    removeFromFlashBar(sessionId, "session")
                }
            } as FlashbarProps.MessageDefinition]
        })
        if (!loopDelay) {
            setLoopDelay(process.env.NEXT_PUBLIC_FLASH_BAR_UPDATE_IN_SECONDS * 1000)
        }
    }

    const addDeletedSessionFlashBar = (sessionId: string, sessionName: string): FlashbarProps.MessageDefinition => {
        console.log("Flash Bar: Adding deleted session to flash bar:", sessionId)
        return {
            id: sessionId,
            type: "success",
            loading: false,
            content: 'Successfully closed "' + sessionName + '" session.',
            dismissible: true,
            dismissLabel: 'Dismiss message',
            onDismiss: (event) => {
                removeFromFlashBar(sessionId, "session")
            }
        }
    }

    const addDeletingSessionFlashBar = (sessionId: string, sessionName: string): void => {
        console.log("Flash Bar: Adding deleting session to flash bar:", sessionId)
        setSessionInProgress([sessionId])
        setItems(item => {
            return [...items, {
                id: sessionId,
                type: "success",
                loading: true,
                content: 'Closing "' + sessionName + '" session.',
                dismissible: true,
                dismissLabel: 'Dismiss message',
                onDismiss: (event) => {
                    removeFromFlashBar(sessionId, "session")
                }
            } as FlashbarProps.MessageDefinition]
        })
        if (!loopDelay) {
            setLoopDelay(process.env.NEXT_PUBLIC_FLASH_BAR_UPDATE_IN_SECONDS * 1000)
        }
    }

    const addDeletedSessionFailedFlashBar = (sessionId: string, sessionName: string, reason: string): void => {
        console.log("Flash Bar: Adding deleted session to flash bar:", sessionId)
        setItems(items => {
            return [...items, {
                id: sessionId,
                type: "error",
                loading: false,
                content: `Unable to close session ${sessionName} due to: ${reason}.`,
                dismissible: true,
                dismissLabel: 'Dismiss message',
                onDismiss: (event) => {
                    removeFromFlashBar(sessionId, "session")
                }
            } as FlashbarProps.MessageDefinition]
        })
    }

    const addCreateSessionFailedFlashBar = (sessionName: string, reason: string): void => {
        console.log("Flash Bar: Adding create session to flash bar:", sessionName)
        setItems(items => {
            return [...items, {
                id: sessionName,
                type: "error",
                loading: false,
                content: <>
                        Unable to create session "{sessionName}" due to: {reason}.
                        <br/>
                        Please contact your administrator.
                    </>,
                dismissible: true,
                dismissLabel: 'Dismiss message',
                onDismiss: (event) => {
                    removeFromFlashBar(sessionName, "session")
                }
            } as FlashbarProps.MessageDefinition]
        })
    }

    const removeFromFlashBar = (id: string | undefined, type: FlashBarType) => {
        setItems(items => {
            return items.filter(item => item.id !== id)
        })

        if (type === "session") {
            let filteredSessions = sessionsInProgress.filter(session => session !== id)
            setSessionInProgress(filteredSessions);
        }
    }

    useInterval(async () => {
        if (sessionsInProgress.length === 0) {
            console.log("sessionsInProgress is empty")
            return
        }
        let describeSessionIds = sessionsInProgress.map(sessionId => {
            return {
                Operator: FilterTokenOperatorEnum.Equal,
                Value: sessionId
            } as FilterToken
        })
        console.log("Flash Bar: describeSessionIds: ", describeSessionIds)
        let currentInProgressSessionIds = [] as string[]
        let successfulSessionIds = [] as string[]
        let failedSessionIds = [] as string[]

        let successfulSessions = [] as SessionWithPermissions[]
        let failedSessions = [] as SessionWithPermissions[]
        let describedSessionIds = [] as string[]
        console.log("Flash Bar: calling describeSessions: ", describeSessionIds)
        await new DataAccessService().describeSessions({
            SessionIds: describeSessionIds
        }).then(result => {
            result.data.Sessions?.forEach(session => {
                console.log("Flash Bar: Looping through session response:", session)
                describedSessionIds.push(session.Id!)
                switch (session.State) {
                    case State.Deleting:
                    case State.Creating: {
                        console.log("Flash Bar: ", session)
                        currentInProgressSessionIds.push(session.Id!)
                        break;
                    }

                    case State.Deleted:
                    case State.Ready: {
                        successfulSessions.push(session)
                        successfulSessionIds.push(session.Id!)
                        break;
                    }
                    default: {
                        failedSessions.push(session)
                        failedSessionIds.push(session.Id!)
                        break;
                    }
                }
            })

            // If an in-progress session id does not appear in the describe call result, remove it from the flash bar
            let missingSessionIds = sessionsInProgress.filter(item => describedSessionIds.indexOf(item) < 0)
            missingSessionIds.forEach(id => {
                console.log("Flash Bar: Session id "+id+" not returned in the describe call, removing it")
                successfulSessionIds.push(id)
            })

        }).finally(() => {
                console.log("Flash Bar: describeSessions completed")
                // Update the current in progress
                setSessionInProgress(currentInProgressSessionIds)

                // Remove completed items from flash bar
                let inProgressFlashBarItems = items.filter(item => successfulSessionIds.indexOf(item.id!) < 0)

                // Add completed flash bar
                let successFlashBarItems = successfulSessions.map(session => {
                    if(session.State == State.Ready) {
                        // Create session was successful
                        return {
                            id: session.Id!,
                            type: 'success',
                            loading: false,
                            content: 'Your "' + session.Name + '" session is available and ready to use.',
                            dismissible: true,
                            dismissLabel: 'Dismiss message',
                            action: <Button
                                onClick={() => new DataAccessService().launchSession("web", session.Id!)}> Connect
                            </Button>,
                            onDismiss: (event) => {
                                removeFromFlashBar(session.Id!, "message")
                            }
                        } as FlashbarProps.MessageDefinition
                    }

                    // DeleteSession was successful
                    return addDeletedSessionFlashBar(session.Id!, session.Name!)
                })

                // Add error flash bar
                let errorFlashBarItem = failedSessions.map(session => {
                    let message = 'An error occurred while creating session "' + session.Name + '": '
                    if (session.StateReason && session.StateReason.includes("Specified user is not a local user")) {
                        message = 'An error occurred while creating session "' + session.Name + ': Specified user is not a local user.'
                    } else {
                        message = message + (session.StateReason || "Unknown reason.")
                    }
                    return {
                        id: session.Id!,
                        type: 'error',
                        loading: false,
                        content: message,
                        dismissible: true,
                        dismissLabel: 'Dismiss message',
                        onDismiss: (event) => {
                            removeFromFlashBar(session.Id!, "message")
                        }
                    } as FlashbarProps.MessageDefinition
                })
                console.log("Flash Bar: Current state: ", inProgressFlashBarItems, successFlashBarItems, errorFlashBarItem)

                setItems([...inProgressFlashBarItems, ...successFlashBarItems, ...errorFlashBarItem]);

                // Stop the background thread if there aren't any pending sessions
                if (!currentInProgressSessionIds || currentInProgressSessionIds.length === 0) {
                    console.log("Flash Bar: Disable auto update on flash bar")
                    setLoopDelay(null as Number)
                }
            }
        )

    }, loopDelay)

    return (
        <FlashBarContext.Provider value={{
            items: items,
            addSessionFlashBar: addSessionFlashBar,
            addDeletedSessionFlashBar: addDeletedSessionFlashBar,
            addDeletingSessionFlashBar: addDeletingSessionFlashBar,
            addDeletedSessionFailedFlashBar: addDeletedSessionFailedFlashBar,
            addCreateSessionFailedFlashBar: addCreateSessionFailedFlashBar,
            addFlashBar: addFlashBar,
            addLoadingFlashBar: addLoadingFlashBar,
            removeFromFlashBar: removeFromFlashBar
        }}>
            {children}
        </FlashBarContext.Provider>
    )
}

export const useFlashBarContext = () => useContext(FlashBarContext)
