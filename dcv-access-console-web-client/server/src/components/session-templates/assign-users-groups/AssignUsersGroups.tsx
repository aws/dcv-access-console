import * as React from "react";
import AddUserGroupUsers
    from "@/components/user-management/user-groups/modify-user-group/add-user-group-users/AddUserGroupUsers";
import {Header, SpaceBetween} from "@cloudscape-design/components";
import {EDIT_USER_GROUP_FORM_CONSTANTS} from "@/constants/edit-user-group-form-constants";
import {SESSION_TEMPLATES_CREATE_CONSTANTS} from "@/constants/session-templates-create-constants";
import {OptionsLoadItemsDetail} from "@cloudscape-design/components/internal/components/dropdown/interfaces";
import {useEffect, useRef, useState} from "react";
import {OptionDefinition} from "@cloudscape-design/components/internal/components/option/interfaces";
import {
    DescribeUserGroupsRequestData, DescribeUserGroupsResponse,
    DescribeUsersRequestData,
    DescribeUsersResponse,
    FilterToken,
    User
} from "@/generated-src/client";
import {
    SelectStateType
} from "@/components/user-management/user-groups/modify-user-group/edit-user-group-session-templates/EditUserGroupSessionTemplates";
import DataAccessService from "@/components/common/utils/DataAccessService";

export type AssignUsersGroupsProps = {
    sessionTemplateId: string
    handleUsersChange: (userIds: OptionDefinition[]) => void
    handleGroupsChange: (groupIds: OptionDefinition[]) => void
    handleError: (error: string) => void
}

export default function AssignUsersGroups({
                                              sessionTemplateId,
                                              handleUsersChange,
                                              handleGroupsChange,
                                              handleError
                                          }: AssignUsersGroupsProps) {
    const [usersNextToken, setUsersNextToken] = useState<string | null>()
    const usersRequest = useRef({filteringText: ''})
    const dataAccessService = new DataAccessService()
    const [selectedUsersToAdd, setSelectedUsersToAdd] = useState<OptionDefinition[]>([])
    const [selectUsersState, setSelectUsersState] = useState<SelectStateType>({
        options: [],
        status: "pending",
        errorMessage: undefined
    })
    const [userGroupsNextToken, setUserGroupsNextToken] = useState<string | null>()
    const [selectedGroupsToAdd, setSelectedUserGroupsToAdd] = useState<OptionDefinition[]>([])
    const [selectGroupsState, setSelectUserGroupsState] = useState<SelectStateType>({
        options: [],
        status: "pending",
        errorMessage: undefined
    })

    const USERS_SEARCH_LIST_SIZE = 20
    useEffect(() => {
        // Load all the users and groups that have been shared
        let userIds: OptionDefinition[] = []
        let groupIds: OptionDefinition[] = []
        if (sessionTemplateId != null) {
            new DataAccessService().describeAllUsersSharedWithSessionTemplate({
                SessionTemplateId: sessionTemplateId
            }).then((result) => {
                if (result.Error) {
                    console.log("Error while obtaining users for session template: ", sessionTemplateId)
                    handleError("Error while obtaining users for the session template")
                }
                result.Users?.forEach(user => {
                    userIds.push({label: user.UserId, value: user.UserId, description: user.DisplayName})
                })
                handleUsersChangeInternal(userIds)
            })

            new DataAccessService().describeAllGroupsSharedWithSessionTemplate({
                SessionTemplateId: sessionTemplateId
            }).then((result) => {
                if (result.Error) {
                    console.log("Error while obtaining users for session template: ", sessionTemplateId)
                    handleError("Error while obtaining users for the session template")
                }
                result.UserGroups?.forEach(group => {
                    groupIds.push({label: group.UserGroupId, value: group.UserGroupId, description: group.DisplayName})
                })
                handleGroupChangeInternal(groupIds)
            })
        }
    }, [])

    const getFilteredGroups = async (filterString: string, nextToken?: string | null) => {
        const describeUserGroupsRequest: DescribeUserGroupsRequestData = {
            UserGroupIds: [{
                Operator: "CONTAINS",
                Value: filterString
            } as FilterToken],
            SortToken: {
                Operator: "DESC",
                Key: "UserGroupId"
            },
            NextToken: nextToken,
            MaxResults: USERS_SEARCH_LIST_SIZE
        } as DescribeUserGroupsRequestData

        return dataAccessService.describeUserGroups(describeUserGroupsRequest).then(r => {
            return [r.data, r.status !== 200 ? r.statusText : undefined] as
                [DescribeUserGroupsResponse, string | undefined];
        }).catch(e => {
            console.error("Failed to retrieve Groups", e)
            return [undefined, "Failed to retrieve Groups"]
        })

    }
    const fetchUserGroupsForAutocomplete = async (filteringText: string, clearItems: boolean) => {
        try {
            const [result, errorMessage] = await getFilteredGroups(filteringText, userGroupsNextToken)
            if (errorMessage) {
                setSelectUserGroupsState(prevState => {
                    return {
                        ...prevState,
                        status: "error",
                        errorMessage: errorMessage
                    }
                })
            }
            if (!usersRequest.current || usersRequest.current.filteringText !== filteringText) {
                // there is another request in progress, discard the result of this one
                return
            }
            const options = result.UserGroups?.map(group => {
                return {
                    value: group.UserGroupId,
                    label: group.UserGroupId,
                    description: group.DisplayName,
                }
            }) || []

            const uniqueOptions = clearItems ? options : selectGroupsState.options.concat(options.filter(group =>
                !selectGroupsState.options.includes(group))
            )
            setSelectUserGroupsState({
                status: !result.NextToken ? "finished" : "pending",
                options: uniqueOptions
            })
            setUserGroupsNextToken(result.NextToken)
        } catch (error) {
            console.log("Error retrieving filtered users: ", error)
            setSelectUserGroupsState(prevState => {
                return {
                    ...prevState,
                    status: "error"
                }
            })
        }
    }
    const handleLoadUserGroupsForSearch = ({filteringText, firstPage, samePage}: OptionsLoadItemsDetail) => {
        usersRequest.current = {
            filteringText: filteringText
        }
        if (firstPage) {
            setUserGroupsNextToken(null)
        }
        setSelectUserGroupsState(prevState => {
            return {
                ...prevState,
                status: "loading",
            }
        })
        fetchUserGroupsForAutocomplete(filteringText, firstPage)
    }

    const getFilteredUsers = async (filterString: string, nextToken?: string | null) => {
        const describeUsersRequest: DescribeUsersRequestData = {
            UserIds: [{
                Operator: "CONTAINS",
                Value: filterString
            } as FilterToken],
            SortToken: {
                Operator: "DESC",
                Key: "UserId"
            },
            NextToken: nextToken,
            MaxResults: USERS_SEARCH_LIST_SIZE
        } as DescribeUsersRequestData

        return dataAccessService.describeUsers(describeUsersRequest).then(r => {
            return [r.data, r.status !== 200 ? r.statusText : undefined] as
                [DescribeUsersResponse, string | undefined];
        }).catch(e => {
            console.error("Failed to retrieve Users", e)
            return [undefined, "Failed to retrieve Users"]
        })

    }
    const fetchUsersForAutocomplete = async (filteringText: string, clearItems: boolean) => {
        try {
            const [result, errorMessage] = await getFilteredUsers(filteringText, usersNextToken)
            if (errorMessage) {
                setSelectUsersState(prevState => {
                    return {
                        ...prevState,
                        status: "error",
                        errorMessage: errorMessage
                    }
                })
            }
            if (!usersRequest.current || usersRequest.current.filteringText !== filteringText) {
                // there is another request in progress, discard the result of this one
                return
            }
            const options = result.Users?.map(user => {
                return {
                    value: user.UserId,
                    label: user.UserId,
                    description: user.DisplayName,
                }
            }) || []

            const uniqueOptions = clearItems ? options : selectUsersState.options.concat(options.filter(user =>
                !selectUsersState.options.includes(user))
            )
            setSelectUsersState({
                status: !result.NextToken ? "finished" : "pending",
                options: uniqueOptions
            })
            setUsersNextToken(result.NextToken)
        } catch (error) {
            console.log("Error retrieving filtered users: ", error)
            setSelectUsersState(prevState => {
                return {
                    ...prevState,
                    status: "error"
                }
            })
        }
    }
    const handleLoadUsersForSearch = ({filteringText, firstPage, samePage}: OptionsLoadItemsDetail) => {
        usersRequest.current = {
            filteringText: filteringText
        }
        if (firstPage) {
            setUsersNextToken(null)
        }
        setSelectUsersState(prevState => {
            return {
                ...prevState,
                status: "loading",
            }
        })
        fetchUsersForAutocomplete(filteringText, firstPage)
    }

    const handleUsersChangeInternal = (users: OptionDefinition[] ) => {
        setSelectedUsersToAdd(users)
        handleUsersChange(users)
    }
    const handleGroupChangeInternal = (groups: OptionDefinition[] ) => {
        setSelectedUserGroupsToAdd(groups)
        handleGroupsChange(groups)
    }

    return <SpaceBetween size={"l"}>
        <Header variant={"h3"}
                description={SESSION_TEMPLATES_CREATE_CONSTANTS.ASSIGN_USERS_ONLY_DESCRIPTION}>
            {SESSION_TEMPLATES_CREATE_CONSTANTS.ADD_USERS}
        </Header>
        <AddUserGroupUsers
            handleLoadItems={handleLoadUsersForSearch}
            selectItemsState={selectUsersState}
            itemsToAdd={selectedUsersToAdd}
            setItemsToAdd={handleUsersChangeInternal}
            formConstants={{
                LOADING: EDIT_USER_GROUP_FORM_CONSTANTS.LOADING_USERS_TEXT,
                EMPTY: EDIT_USER_GROUP_FORM_CONSTANTS.ADD_USER_EMPTY_TEXT,
                ERROR: EDIT_USER_GROUP_FORM_CONSTANTS.USER_SEARCH_ERROR_MESSAGE,
                PLACEHOLDER: EDIT_USER_GROUP_FORM_CONSTANTS.ADD_USER_PLACEHOLDER
            }}
            addHorizontalLine={true}
        />
        <Header variant={"h3"}
                description={SESSION_TEMPLATES_CREATE_CONSTANTS.ASSIGN_GROUP_ONLY_DESCRIPTION}>
            {SESSION_TEMPLATES_CREATE_CONSTANTS.ADD_GROUPS}
        </Header>
        <AddUserGroupUsers
            handleLoadItems={handleLoadUserGroupsForSearch}
            selectItemsState={selectGroupsState}
            itemsToAdd={selectedGroupsToAdd}
            setItemsToAdd={handleGroupChangeInternal}
            formConstants={{
                LOADING: EDIT_USER_GROUP_FORM_CONSTANTS.LOADING_GROUPS_TEXT,
                EMPTY: EDIT_USER_GROUP_FORM_CONSTANTS.ADD_GROUP_EMPTY_TEXT,
                ERROR: EDIT_USER_GROUP_FORM_CONSTANTS.GROUP_SEARCH_ERROR_MESSAGE,
                PLACEHOLDER: EDIT_USER_GROUP_FORM_CONSTANTS.ADD_GROUP_PLACEHOLDER
            }}
            addHorizontalLine={false}
        />
    </SpaceBetween>
}
