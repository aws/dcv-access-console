// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {Session, SessionTemplate, User, UserGroup} from "@/generated-src/client";

export type SessionTemplatesState = {
    sessionTemplates: SessionTemplate[]
    loading: boolean
    error: boolean
    errorMessage?: string
}

export type UsersState = {
    users: User[]
    loading: boolean
    error: boolean
    errorMessage?: string
}

export type UserGroupsState = {
    groups: UserGroup[]
    loading: boolean
    error: boolean
    errorMessage?: string
}

export type SessionState = {
    sessions: Session[]
    loading: boolean
    error: boolean
    errorMessage?: string
}