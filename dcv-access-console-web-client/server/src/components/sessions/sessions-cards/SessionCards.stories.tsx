// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import SessionCards from "@/components/sessions/sessions-cards/SessionCards";
import {getDescribeSessions200Response} from "@/generated-src/msw/mock";
import {SessionWithPermissions, SessionScreenshotImage} from "@/generated-src/client";
import {ScreenshotsMap} from "@/components/common/utils/DataAccessService";
export default {
    title: 'components/common/Sessions',
    component: SessionCards,
}

const Template = args => <SessionCards{...args}/>

// @ts-expect-error
export const sessions: Session[] = getDescribeSessions200Response().Sessions
export const validScreenshotMap: ScreenshotsMap = getValidScreenshotMap(sessions)
export const errorScreenshotMap: ScreenshotsMap = getErrorScreenshotMap(sessions)

export const SessionCardsEmpty = Template.bind({})
SessionCardsEmpty.args = {
    sessionsState: {
        sessions: [],
        loading: false,
        error: false,
    }

}
export const SessionCardsLoading = Template.bind({})
SessionCardsLoading.args = {
    sessionsState: {
        sessions: [],
        loading: true,
        error: false,
    },

}

export const SessionCardsWithScreenshotsLoading = Template.bind({})
SessionCardsWithScreenshotsLoading.args = {
    sessionsState: {
        sessions: sessions,
        loading: false,
        error: false,
    },

}

export const SessionCardsWithScreenshots = Template.bind({})
SessionCardsWithScreenshots.args = {
    sessionsState: {
        sessions: sessions,
        screenshotsMap: validScreenshotMap,
        loading: false,
        error: false,
    },
    describeSessionsQuery: {
        tokens: [],
        operation: "or"
    },
    userSession: {
        user: {
            id: "UserID"
        }
    }
}

export const SessionCardsWithErrorScreenshots = Template.bind({})
SessionCardsWithErrorScreenshots.args = {
    sessionsState: {
        sessions: sessions,
        screenshotsMap: errorScreenshotMap,
        loading: false,
        error: false,
    },

}


function getValidScreenshotMap(sessions: SessionWithPermissions[]): ScreenshotsMap {
    const defaultImage:SessionScreenshotImage = {
        Format: "png",
        Data: "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==",
        Primary: true
    }
    const sessionScreeshotMap = new Map<string, SessionScreenshotImage>();
    const failedSessionsMap = new Map<string, string>();

    sessions.forEach((session) => {
        sessionScreeshotMap.set(session.Id!, defaultImage)
    })

    return {
        failedSessionsMap: failedSessionsMap,
        sessionScreeshotMap: sessionScreeshotMap
    }
}

function getErrorScreenshotMap(sessions: SessionWithPermissions[]): ScreenshotsMap {
    const sessionScreeshotMap = new Map<string, SessionScreenshotImage>();
    const failedSessionsMap = new Map<string, string>();

    sessions.forEach((session) => {
        failedSessionsMap.set(session.Id!, "Expected Error")
    })

    return {
        failedSessionsMap: failedSessionsMap,
        sessionScreeshotMap: sessionScreeshotMap
    }
}

