import {
    DescribeSessionsUIRequestData,
    DescribeSessionTemplatesRequestData,
    DescribeUsersRequestData,
    SessionTemplate,
    UnpublishSessionTemplateRequestData,
    User,
    UserGroup
} from "@/generated-src/client";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {SessionsState} from "@/app/sessions/page";
import {SessionTemplatesState, UsersState} from "@/components/user-management/common/SplitPanelStates";
import {FlashbarProps} from "@cloudscape-design/components/flashbar/interfaces";

const dataAccessService = new DataAccessService()

export const getSessionsForUser = (user: User, setSessionState: (state: SessionsState) => void) => {
    // TODO: Paginate
    const describeSessionsRequest: DescribeSessionsUIRequestData = {
        UserId: user.UserId,
        States: [
            {
                Operator: "!=",
                Value: "DELETED"
            }
        ]
    }

    setSessionState(prevState => {
        return {
            ...prevState,
            loading: true
        }
    })

    console.log(describeSessionsRequest)
    dataAccessService.describeSessions(describeSessionsRequest)
        .then(r => {
            setSessionState(prevState => {
                return {
                    ...prevState,
                    sessions: r.data.Sessions ? r.data.Sessions : [],
                    loading: false,
                    error: false
                }
            })
        }).catch(e => {
        console.error("Failed to retrieve sessions: ", e)
        setSessionState(prevState => {
            return {
                ...prevState,
                loading: false,
                sessions: [],
                error: true,
                errorMessage: e.errorMessage
            }
        })
    })
}

export const getSessionsForGroup = (group: UserGroup, setSessionState: (state: SessionsState) => void) => {
    // TODO: Paginate
    const describeSessionsRequest: DescribeSessionsUIRequestData = {
        GroupsSharedWith: [{
                Operator: "=",
                Value: group.UserGroupId
        }],
        UserGroupId: group.UserGroupId,
        States: [
            {
                Operator: "!=",
                Value: "DELETED"
            }
        ]
    }

    setSessionState(prevState => {
        return {
            ...prevState,
            loading: true
        }
    })

    console.log(describeSessionsRequest)
    dataAccessService.describeSessions(describeSessionsRequest)
        .then(r => {
            setSessionState(prevState => {
                return {
                    ...prevState,
                    sessions: r.data.Sessions ? r.data.Sessions : [],
                    loading: false,
                    error: false
                }
            })
        }).catch(e => {
        console.error("Failed to retrieve sessions: ", e)
        setSessionState(prevState => {
            return {
                ...prevState,
                sessions: [],
                loading: false,
                error: true,
                errorMessage: e.errorMessage
            }
        })
    })
}

export const getSessionTemplatesForUser = (user: User, setSessionTemplateState: (state: SessionTemplatesState) => void) => {
    // TODO: Paginate :(
    const describeSessionTemplatesRequest: DescribeSessionTemplatesRequestData = {
        UsersSharedWith: [{
            Operator: "=",
            Value: user.UserId
        }],
        UserId: user.UserId
    }

    setSessionTemplateState(prevState => {
        return {
            ...prevState,
            loading: true
        }
    })

    console.log(describeSessionTemplatesRequest)
    dataAccessService.describeSessionTemplates(describeSessionTemplatesRequest)
        .then(r => {
            setSessionTemplateState(prevState => {
                return {
                    ...prevState,
                    sessionTemplates: r.data.SessionTemplates ? r.data.SessionTemplates : [],
                    loading: false,
                    error: false
                }
            })
        }).catch(e => {
        console.error("Failed to retrieve session templates: ", e)
        setSessionTemplateState(prevState => {
            return {
                ...prevState,
                sessionTemplates: [],
                loading: false,
                error: true,
                errorMessage: e.errorMessage
            }
        })
    })
}

export const getSessionTemplatesForGroup = (group: UserGroup, setSessionTemplateState: (state: SessionTemplatesState) => void) => {
    // TODO: Paginate :(
    const describeSessionTemplatesRequest: DescribeSessionTemplatesRequestData = {
        GroupsSharedWith: [{
            Operator: "=",
            Value: group.UserGroupId
        }],
        UserGroupId: group.UserGroupId
    }

    setSessionTemplateState(prevState => {
        return {
            ...prevState,
            loading: true
        }
    })

    console.log(describeSessionTemplatesRequest)
    dataAccessService.describeSessionTemplates(describeSessionTemplatesRequest)
        .then(r => {
            setSessionTemplateState(prevState => {
                return {
                    ...prevState,
                    sessionTemplates: r.data.SessionTemplates ? r.data.SessionTemplates : [],
                    loading: false,
                    error: false
                }
            })
        }).catch(e => {
        console.error("Failed to retrieve session templates: ", e)
        setSessionTemplateState(prevState => {
            return {
                ...prevState,
                sessionTemplates: [],
                loading: false,
                error: true,
                errorMessage: e.errorMessage
            }
        })
    })
}

export const getUsersForGroup = (group: UserGroup, setUsersState: (state: UsersState) => void) => {
    // TODO: Paginate
    const describeUsersRequest: DescribeUsersRequestData = {
        UserGroupIds: [{
            Operator: "=",
            Value: group.UserGroupId
        }]
    }

    setUsersState(prevState => {
        return {
            ...prevState,
            loading: true
        }
    })

    console.log(describeUsersRequest)
    dataAccessService.describeUsers(describeUsersRequest)
        .then(r => {
            setUsersState(prevState => {
                return {
                    ...prevState,
                    users: r.data.Users ? r.data.Users : [],
                    loading: false,
                    error: false
                }
            })
        }).catch(e => {
        console.error("Failed to retrieve users: ", e)
        setUsersState(prevState => {
            return {
                ...prevState,
                loading: false,
                error: true,
                errorMessage: e.errorMessage
            }
        })
    })
}

export const unpublishSessionTemplateFromUser = (sessionTemplate: SessionTemplate, user: User, addFlashBar: (type: FlashbarProps.Type, id: string | undefined, message: string) => void) => {
    const unpublishSessionTemplateRequest: UnpublishSessionTemplateRequestData = {
        Id: sessionTemplate.Id,
        UserIds: [user!.UserId!]
    }

    dataAccessService.unpublishSessionTemplate(unpublishSessionTemplateRequest).then(result => {
        if (result.data.Error || result.data.SuccessfulUsersList?.length !== 1) {
            addFlashBar("error", sessionTemplate.Id, `Error while removing Session Template ${sessionTemplate.Name} from ${user?.DisplayName}`)
        } else {
            addFlashBar("success", sessionTemplate.Id, `Successfully removed Session Template ${sessionTemplate.Name} from User ${user?.DisplayName}`)
        }
    }).catch(e => {
        addFlashBar("error", sessionTemplate.Id, `Error while removing Session Template ${sessionTemplate.Name} from ${user?.DisplayName}`)
        console.error("Failed to unpublish session template: ", sessionTemplate.Id, e)
    })
}

export const unpublishSessionTemplateFromGroup = (sessionTemplate: SessionTemplate, group: UserGroup, addFlashBar: (type: FlashbarProps.Type, id: string | undefined, message: string) => void) => {
    const unpublishSessionTemplateRequest: UnpublishSessionTemplateRequestData = {
        Id: sessionTemplate.Id,
        GroupIds: [group!.UserGroupId!]
    }

    dataAccessService.unpublishSessionTemplate(unpublishSessionTemplateRequest).then(result => {
        if (result.data.Error || result.data.SuccessfulGroupsList?.length !== 1) {
            addFlashBar("error", sessionTemplate.Id, `Error while removing Session Template ${sessionTemplate.Name} from ${group?.DisplayName}`)
        } else {
            addFlashBar("success", sessionTemplate.Id, `Successfully removed Session Template ${sessionTemplate.Name} from User Group ${group?.DisplayName}`)
        }
    }).catch(e => {
        addFlashBar("error", sessionTemplate.Id, `Error while removing Session Template ${sessionTemplate.Name} from ${group?.DisplayName}`)
        console.error("Failed to unpublish session template: ", sessionTemplate.Id, e)
    })
}

