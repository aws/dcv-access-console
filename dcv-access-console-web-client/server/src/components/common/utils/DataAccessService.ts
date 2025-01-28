// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {
    Configuration,
    CreateSessionTemplateRequestData,
    CreateSessionUIRequestData,
    CreateUserGroupRequestData,
    DeleteSessionTemplatesRequestData,
    DeleteSessionUIRequestData,
    DeleteUserGroupsRequestData,
    DescribeServersUIRequestData,
    DescribeSessionsUIRequestData,
    DescribeSessionTemplatesRequestData,
    DescribeUserGroupsRequestData,
    DescribeUserGroupsSharedWithSessionTemplateRequestData, DescribeUserGroupsSharedWithSessionTemplateResponse,
    DescribeUserInfoResponse,
    DescribeUsersRequestData,
    DescribeUsersSharedWithSessionTemplateRequestData, DescribeUsersSharedWithSessionTemplateResponse,
    EditSessionTemplateRequestData,
    EditUserGroupRequestData, GetSessionConnectionDataUIResponse,
    GetSessionScreenshotsUIRequestData,
    PublishSessionTemplateRequestData,
    RemoveFromUserGroupRequestData,
    ServersApi,
    SessionsApi,
    SessionScreenshotImage,
    SessionTemplatesApi,
    SessionWithPermissions,
    UnpublishSessionTemplateRequestData,
    UserGroupsApi,
    UsersApi, UserWithPermissions, ValidateSessionTemplateRequestData
} from "@/generated-src/client";
import {startWorker} from "@/generated-src/msw/mock";
import {getToken} from "@/components/common/utils/TokenAccessService";
import {getSession} from "next-auth/react";
import React from "react";
import {SessionScreenshotState} from "@/components/common/utils/SessionScreenshotState";
import {SESSION_SCREENSHOT_CONSTANTS} from "@/constants/session-screenshot-constants";
import axios, {AxiosResponse} from "axios";

let isMocked = false

const BROKER_SCREENSHOT_LIMIT = 5

export type ScreenshotsMap = {
    sessionScreenshotMap: Map<string, SessionScreenshotImage>;
    failedSessionsMap: Map<string, string>;
}

export default class DataAccessService {
    config: Configuration;

    constructor() {
        this.config = new Configuration({
        })

        if (process.env.NODE_ENV == "development" && process.env.NEXT_PUBLIC_ENABLE_MOCK_WORKER == "true" && !isMocked) {
            console.log("Mocking the API requests")
            startWorker()
            isMocked = true;
        }
    }

    public async describeServers(describeServersRequest?: DescribeServersUIRequestData) {
        console.log("DescribeServersRequest: ", describeServersRequest)
        return (await this.getServersApi()).describeServers(describeServersRequest)
    }

    public async describeSessions(describeSessionsRequest?: DescribeSessionsUIRequestData) {
        return (await this.getSessionsApi()).describeSessions(describeSessionsRequest)
    }

    public async describeSessionTemplates(describeSessionTemplatesRequest?: DescribeSessionTemplatesRequestData) {
        return (await this.getSessionTemplatesApi()).describeSessionTemplates(describeSessionTemplatesRequest)
    }
    public async describeUsersSharedWithSessionTemplate(request?: DescribeUsersSharedWithSessionTemplateRequestData) {
        return (await this.getUsersApi()).describeUsersSharedWithSessionTemplate(request)
    }
    public async describeUserGroupsSharedWithSessionTemplate(request?: DescribeUserGroupsSharedWithSessionTemplateRequestData) {
        return (await this.getUserGroupsApi()).describeUserGroupsSharedWithSessionTemplate(request)
    }
    public async describeUserGroups(describeUserGroupsRequest?: DescribeUserGroupsRequestData) {
        return (await this.getUserGroupsApi()).describeUserGroups(describeUserGroupsRequest)
    }
    public async describeUsers(describeUsersRequest?: DescribeUsersRequestData) {
        return (await this.getUsersApi()).describeUsers(describeUsersRequest)
    }

    public async createSessionTemplate(createSessionTemplateRequest?: CreateSessionTemplateRequestData) {
        return (await this.getSessionTemplatesApi()).createSessionTemplate(createSessionTemplateRequest)
    }

    public async editSessionTemplate(editSessionTemplateRequest?: EditSessionTemplateRequestData) {
        return (await this.getSessionTemplatesApi()).editSessionTemplate(editSessionTemplateRequest)
    }

    public async publishSessionTemplate(publishSessionTemplateRequest?: PublishSessionTemplateRequestData) {
        return (await this.getSessionTemplatesApi()).publishSessionTemplate(publishSessionTemplateRequest)
    }

    public async unpublishSessionTemplate(unpublishSessionTemplateRequest?: UnpublishSessionTemplateRequestData) {
        return (await this.getSessionTemplatesApi()).unpublishSessionTemplate(unpublishSessionTemplateRequest)
    }

    public async validateSessionTemplate(validateSessionTemplateRequestData?: ValidateSessionTemplateRequestData) {
        return (await this.getSessionTemplatesApi()).validateSessionTemplate(validateSessionTemplateRequestData)
    }

    public async createSessions(createSessionsRequest?: CreateSessionUIRequestData[]) {
        return (await this.getSessionsApi()).createSessions(createSessionsRequest)
    }

    public async getSessionConnectionData(sessionId: string, owner?: string) {
        return ((await this.getSessionsApi()).getSessionConnectionData(sessionId, owner))
    }

    public async deleteSessions(deleteSessionsRequest?: DeleteSessionUIRequestData[]) {
        return (await this.getSessionsApi()).deleteSessions(deleteSessionsRequest)
    }
    public async deleteSessionTemplate(deleteSessionTemplatesRequestData: DeleteSessionTemplatesRequestData) {
        return (await this.getSessionTemplatesApi()).deleteSessionTemplates(deleteSessionTemplatesRequestData)
    }
    public async deleteUserGroups(deleteUserGroupRequestData: DeleteUserGroupsRequestData) {
        return (await this.getUserGroupsApi()).deleteUserGroups(deleteUserGroupRequestData)
    }

    public async importUsers(file: File, overwriteExistingUsers: boolean, overwriteGroups: boolean) {
        return (await this.getUsersApi()).importUsers(file, overwriteExistingUsers, overwriteGroups)
    }

    public async createUserGroup(createUserGroupRequestData: CreateUserGroupRequestData) {
        return (await this.getUserGroupsApi()).createUserGroup(createUserGroupRequestData)
    }

    public async editUserGroup(editUserGroupDisplayNameRequestData: EditUserGroupRequestData) {
        return (await this.getUserGroupsApi()).editUserGroup(editUserGroupDisplayNameRequestData)
    }

    public async removeFromUserGroup(removeFromUserGroupRequestData: RemoveFromUserGroupRequestData) {
        return (await this.getUserGroupsApi()).removeFromUserGroup(removeFromUserGroupRequestData)
    }

    public async getSessionScreenshots(sessions: SessionWithPermissions[], setScreenshotReady:  React.Dispatch<React.SetStateAction<Map<string, SessionScreenshotState>>>, sessionScreenshotMaxWidth:string | undefined, sessionScreenshotMaxHeight:string | undefined): Promise<ScreenshotsMap> {
        let sessionIds: string[] = []
        let sessionScreenshotMap = new Map<string, SessionScreenshotImage>();
        let failedSessionsMap = new Map<string, string>();
        const parsedMaxWidth = sessionScreenshotMaxWidth ? parseInt(sessionScreenshotMaxWidth) : SESSION_SCREENSHOT_CONSTANTS.DEFAULT_MAX_WIDTH;
        const parsedMaxHeight = sessionScreenshotMaxHeight ? parseInt(sessionScreenshotMaxHeight) : SESSION_SCREENSHOT_CONSTANTS.DEFAULT_MAX_HEIGHT;
        if (sessions.length < 1) {
            return {
                sessionScreenshotMap: sessionScreenshotMap,
                failedSessionsMap: failedSessionsMap
            }
        }

        sessions.forEach(session => {
            sessionIds.push(session.Id!)
        })

        let chunkedSessionsIds = []
        while (sessionIds.length > 0) {
            chunkedSessionsIds.push(sessionIds.pop()!)
            if (chunkedSessionsIds.length >= BROKER_SCREENSHOT_LIMIT || sessionIds.length == 0) {
                const request: { MaxWidth: number; MaxHeight: number; SessionIds: any[] } = {
                    SessionIds: chunkedSessionsIds,
                    MaxWidth: parsedMaxWidth,
                    MaxHeight: parsedMaxHeight
                };
                (await this.getSessionsApi()).getSessionScreenshots(request).then((response) => {
                    response.data.SuccessfulList?.forEach((screenshot) => {
                        const sessionId = screenshot.SessionScreenshot?.SessionId
                        console.log(screenshot.SessionScreenshot)
                        console.log(screenshot.SessionScreenshot?.Images)
                        const primaryScreenshot = screenshot.SessionScreenshot?.Images?.find((image) => {
                            return image.Primary
                        })
                        if (sessionId != null) {
                            sessionScreenshotMap.set(sessionId, primaryScreenshot)
                        }
                        setScreenshotReady(map => new Map<string, SessionScreenshotState>(map.set(sessionId!, SessionScreenshotState.READY)));
                    })
                    response.data.UnsuccessfulList?.forEach((screenshot) => {
                        const sessionId = screenshot.GetSessionScreenshotRequestData?.SessionId
                        const failureReason = screenshot.FailureReason ? screenshot.FailureReason : "UNKNOWN"
                        if (sessionId != null) {
                            failedSessionsMap.set(sessionId, failureReason)
                        }
                        setScreenshotReady(map => new Map<string, SessionScreenshotState>(map.set(sessionId!, SessionScreenshotState.FAILED)));
                    })
                }).catch((e) => {
                    console.log("Error while getting the screenshots", e)
                    sessions.forEach(session => {
                        failedSessionsMap.set(session.Id!, e)
                    })
                })
                chunkedSessionsIds = []
            }
        }
        return {
            sessionScreenshotMap: sessionScreenshotMap,
            failedSessionsMap: failedSessionsMap
        }
    }

    private async getAxiosConfig(): Promise<Configuration> {
        let session = await getSession();
        return new Configuration({
            ...this.config,
            basePath: session.sm_ui_handler_base_url,
            baseOptions: {
                basePath: session.sm_ui_handler_base_url,
                headers: {
                    Authorization: getToken(session)
                }
            },
        })
    }

    private async getServersApi() {
        const config = await this.getAxiosConfig()
        return new ServersApi(config);
    }

    private async getSessionsApi() {
        const config = await this.getAxiosConfig()
        return new SessionsApi(config);
    }

    private async getSessionTemplatesApi() {
        const config = await this.getAxiosConfig()
        return new SessionTemplatesApi(config);
    }

    private async getUserGroupsApi() {
        const config = await this.getAxiosConfig()
        return new UserGroupsApi(config)
    }

    private async getUsersApi() {
        const config = await this.getAxiosConfig()
        return new UsersApi(config)
    }

    public async getUserInfo(accessToken: string): Promise<DescribeUserInfoResponse | void> {
        return await new UsersApi(new Configuration({
            ...this.config,
            basePath: process.env.SM_UI_HANDLER_BASE_URL + process.env.SM_UI_HANDLER_API_PREFIX!,
            baseOptions: {
                basePath: process.env.SM_UI_HANDLER_BASE_URL + process.env.SM_UI_HANDLER_API_PREFIX!,
                headers: {
                    Authorization: 'Bearer ' + accessToken
                }
            },
        })).describeUserInfo().then((response) => {
            return response.data
        }).catch((e) => {
            console.log("Unable to obtain userInfo", e)
            throw "Unable to obtain userInfo"
        })
    }
    public async getLaunchSessionUrl(clientType: string, sessionId: string, user?: string) {
        const connectionData: AxiosResponse<GetSessionConnectionDataUIResponse> = await this.getSessionConnectionData(sessionId, user)
        const session: SessionWithPermissions = connectionData.data.Session!
        if(session && connectionData.data.ConnectionToken) {
            return clientType == "web" ? connectionData.data.WebConnectionUrl! : connectionData.data.NativeConnectionUrl!
        }
        console.error("Failed to get sessionConnectionData")
        return "#"
    }
    public launchSession(clientType: string, sessionId: string, user?: string) {
        console.log(`getSessionConnectionDataRequest for sessionId ${sessionId} and optional user ${user}`)
        this.getSessionConnectionData(sessionId, user)
            .then(r => {
                const session: SessionWithPermissions = r.data.Session!
                if(session && r.data.ConnectionToken) {
                    console.log("Successfully obtained sessionConnectionData for session: ", session)
                    const url = clientType == "web" ? r.data.WebConnectionUrl! : r.data.NativeConnectionUrl!
                    if (clientType == "web") {
                        const newWindow = window.open(url, "_blank", "noopener,noreferrer")
                        if (newWindow) {
                            newWindow.opener = null
                        }
                    } else {
                        window.location.assign(url)
                    }
                } else {
                    console.log("No sessionConnectionData for session: ", session)
                }
            }).catch(e => {
            console.error("Failed to get sessionConnectionData: ", e)
        })
    }

    public async describeAllUsersSharedWithSessionTemplate(request: DescribeUsersSharedWithSessionTemplateRequestData) {
        let response: DescribeUsersSharedWithSessionTemplateResponse = {
            Users: []
        }
        let hasNextPage: boolean = true
        while (hasNextPage) {
            let result: axios.AxiosResponse<DescribeUsersSharedWithSessionTemplateResponse> = await (await this.getUsersApi()).describeUsersSharedWithSessionTemplate(request)
            if (result.status !== 200 || !result.data || result.data.Error) {
                console.log("Error while obtaining all the users")
                hasNextPage = false
                if (result.data.Error) {
                    response.Error = result.data.Error
                } else {
                    response.Error = {
                        message: "Error while getting all the users",
                        name: "Error while getting all the users",
                    }
                }
                break
            }
            result.data.Users?.forEach(user => {
                response.Users?.push(user)
            })
            if(!result.data.NextToken) {
                hasNextPage = false
            }
        }
        return response
    }
    public async describeAllGroupsSharedWithSessionTemplate(request: DescribeUserGroupsSharedWithSessionTemplateRequestData) {
        let response: DescribeUserGroupsSharedWithSessionTemplateResponse = {
            UserGroups: []
        }
        let hasNextPage: boolean = true
        while (hasNextPage) {
            let result: axios.AxiosResponse<DescribeUserGroupsSharedWithSessionTemplateResponse> = await (await this.getUserGroupsApi()).describeUserGroupsSharedWithSessionTemplate(request)
            if (result.status !== 200 || !result.data || result.data.Error) {
                console.log("Error while obtaining all the groups")
                hasNextPage = false
                if (result.data.Error) {
                    response.Error = result.data.Error
                } else {
                    response.Error = {
                        message: "Error while getting all the groups",
                        name: "Error while getting all the groups",
                    }
                }
                break
            }
            result.data.UserGroups?.forEach(user => {
                response.UserGroups?.push(user)
            })
            if(!result.data.NextToken) {
                hasNextPage = false
            }
        }
        return response
    }

}
