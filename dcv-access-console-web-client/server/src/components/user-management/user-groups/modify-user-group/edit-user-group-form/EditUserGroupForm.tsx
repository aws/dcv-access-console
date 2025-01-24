import {Container, Grid, Header, SpaceBetween} from "@cloudscape-design/components";
import EditGroupTextField
    from "@/components/user-management/user-groups/modify-user-group/edit-user-group-display-name/EditGroupTextField";
import * as React from "react";
import {useEffect, useRef, useState} from "react";
import {
    DescribeSessionTemplatesRequestData,
    DescribeSessionTemplatesResponse,
    DescribeUsersRequestData,
    DescribeUsersResponse,
    FilterToken, FilterTokenStrict,
    User,
    UserGroup
} from "@/generated-src/client";
import Form from "@cloudscape-design/components/form";
import AddUserGroupUsers
    from "@/components/user-management/user-groups/modify-user-group/add-user-group-users/AddUserGroupUsers";
import DataAccessService from "@/components/common/utils/DataAccessService";
import Button from "@cloudscape-design/components/button";
import EditUserGroupSessionTemplates, {
    SelectStateType
} from "@/components/user-management/user-groups/modify-user-group/edit-user-group-session-templates/EditUserGroupSessionTemplates";
import {OptionDefinition} from "@cloudscape-design/components/internal/components/option/interfaces";
import {OptionsLoadItemsDetail} from "@cloudscape-design/components/internal/components/dropdown/interfaces";
import RemoveUserGroupUsers
    from "@/components/user-management/user-groups/modify-user-group/remove-user-group-users/RemoveUserGroupUsers";
import {PropertyFilterProps} from "@cloudscape-design/components/property-filter";
import {PropertyFilterToken} from "@cloudscape-design/collection-hooks";

export type UserGroupFormSubmitButtonProps = {
    groupId: string,
    displayName?: string,
    userIdsToAdd: string[]
    userIdsToRemove: string[]
    sessionTemplateIdsToPublish: string[]
    sessionTemplateIdsToUnpublish: string[]
}

export type EditUserGroupFormProps = {
    group: UserGroup | undefined,
    formConstants: any,
    submitButtonFunction: (props: UserGroupFormSubmitButtonProps) => void,
    cancelButtonFunction: () => void,
    isEditPage: boolean
}

const SESSION_TEMPLATE_SEARCH_LIST_SIZE = 20
const USERS_SEARCH_LIST_SIZE = 20

export default function EditUserGroupForm(
    {
        group,
        formConstants,
        submitButtonFunction,
        cancelButtonFunction,
        isEditPage
    }: EditUserGroupFormProps
) {
    const [displayName, setDisplayName] = React.useState(group?.DisplayName || "")
    const [groupId, setGroupId] = React.useState(group?.UserGroupId || "")
    const [selectedSessionTemplates, setSelectedSessionTemplates] = useState<OptionDefinition[]>([])
    const [prevSelected, setPrevSelected] = useState<ReadonlyArray<OptionDefinition>>([])
    const [sessionTemplatesToAdd, setSessionTemplatesToAdd] = useState<string[]>([])
    const [sessionTemplatesToRemove, setSessionTemplatesToRemove] = useState<string[]>([])
    const [selectedUsersToAdd, setSelectedUsersToAdd] = useState<OptionDefinition[]>([])
    const [selectedUsersToRemove, setSelectedUsersToRemove] = useState<User[]>([])
    const [groupIdErrorText, setGroupIdErrorText] = React.useState("")
    const [sessionsNextToken, setSessionsNextToken] = useState<string | null>()
    const [usersNextToken, setUsersNextToken] = useState<string | null>()

    const [selectSessionTemplatesState, setSelectSessionTemplatesState] = useState<SelectStateType>({
        options: [],
        status: "pending",
        errorMessage: undefined
    })

    const [selectUsersState, setSelectUsersState] = useState<SelectStateType>({
        options: [],
        status: "pending",
        errorMessage: undefined
    })

    const usersInGroupFilterToken = {
        propertyKey: "UserGroupIds",
        operator: "=",
        value: group?.UserGroupId
    }

    const DEFAULT_FILTERING_QUERY = {
        tokens: [usersInGroupFilterToken] as ReadonlyArray<PropertyFilterToken>,
        operation: 'and'
    };

    const [removeUsersTableQuery, setRemoveUsersTableQuery] = useState(DEFAULT_FILTERING_QUERY as PropertyFilterProps.Query)

    const sessionTemplateRequest = useRef({filteringText: ''})
    const usersRequest = useRef({filteringText: ''})

    const dataAccessService = new DataAccessService()

    useEffect(() => {
        setPrevSelected(selectedSessionTemplates)
    }, [selectedSessionTemplates])

    const handleSessionTemplateSelectionChanged = (sessionTemplates: OptionDefinition[]) => {
        if (prevSelected.length > sessionTemplates.length) { // We removed a Session Template
            const removedTemplate = prevSelected.find(template => !sessionTemplates.includes(template))
            console.log("Removed session template ", removedTemplate)
            if (removedTemplate) {
                if (sessionTemplatesToAdd.includes(removedTemplate.value!)) {
                    // The user previously added this Session Template
                    setSessionTemplatesToAdd(sessionTemplatesToAdd.filter(template => template !== removedTemplate.value))
                } else {
                    setSessionTemplatesToRemove([...sessionTemplatesToRemove, removedTemplate.value!])
                }
            } else {
                console.warn("Could not find removed template: ", sessionTemplates)
            }
        } else if (prevSelected.length < sessionTemplates.length) { // We added a Session Template
            const addedTemplate = sessionTemplates.find(template => !prevSelected.includes(template))
            console.log("Added session template ", addedTemplate)
            if (addedTemplate) {
                if (sessionTemplatesToRemove.includes(addedTemplate.value!)) {
                    // The user previously removed this Session Template
                    setSessionTemplatesToRemove(sessionTemplatesToRemove.filter(template => template !== addedTemplate.value))
                } else {
                    setSessionTemplatesToAdd([...sessionTemplatesToAdd, addedTemplate.value!])
                }
            } else {
                console.warn("Could not find added template: ", sessionTemplates)
            }
        }
        setPrevSelected(sessionTemplates)
        setSelectedSessionTemplates(sessionTemplates)
    }

    const handleLoadSessionTemplatesForSearch = ({filteringText, firstPage, samePage}: OptionsLoadItemsDetail) => {
        sessionTemplateRequest.current = {
            filteringText: filteringText
        }
        if (firstPage) {
            setSessionsNextToken(null)
        }
        setSelectSessionTemplatesState(prevState => {
            return {
                ...prevState,
                status: "loading",
            }
        })

        fetchSessionTemplatesForAutocomplete(filteringText, firstPage)
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

    const fetchSessionTemplatesForAutocomplete = async (filteringText: string, clearItems: boolean) => {
        try {
            const [result, errorMessage] = await getFilteredSessionTemplates(filteringText, sessionsNextToken)

            if (errorMessage) {
                setSelectSessionTemplatesState(prevState => {
                    return {
                        ...prevState,
                        status: "error",
                        errorMessage: errorMessage
                    }
                })
            }

            if (!sessionTemplateRequest.current || sessionTemplateRequest.current.filteringText !== filteringText) {
                // there is another request in progress, discard the result of this one
                return
            }

            const options = result.SessionTemplates?.map(sessionTemplate => {
                return {
                    value:  sessionTemplate.Id,
                    label: sessionTemplate.Name,
                    description: sessionTemplate.Description,
                }
            }) || []

            const uniqueOptions = clearItems ? options : selectSessionTemplatesState.options.concat(options.filter(sessionTemplate =>
                !selectSessionTemplatesState.options.includes(sessionTemplate))
            )

            setSelectSessionTemplatesState({
                status: !result.NextToken ? "finished" : "pending",
                options: uniqueOptions
            })
            setSessionsNextToken(result.NextToken)
        } catch (error) {
            console.log("Error retrieving filtered session templates: ", error)
            setSelectSessionTemplatesState(prevState => {
                return {
                    ...prevState,
                    status: "error"
                }
            })
        }
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
                    value:  user.UserId,
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

    const validateGroupId = (groupId: string) => {
        if (groupId === "") {
            setGroupIdErrorText(formConstants.EMPTY_ID_TEXT)
        } else if(groupId.match(formConstants.INVALID_ID_MATCHER)) {
            setGroupIdErrorText(formConstants.INVALID_ID_TEXT)
        } else {
            setGroupIdErrorText("")
        }
    }

    const getSessionTemplatesForGroup = (group: UserGroup | undefined) => {
        if (!group) {
            setSelectedSessionTemplates([])
            return
        }
        const describeSessionTemplatesRequest: DescribeSessionTemplatesRequestData = {
            GroupsSharedWith: [{
                Operator: "=",
                Value: group.UserGroupId
            } as FilterTokenStrict]
        }

        let nextToken = null
        do { // We need to keep retrieving session templates until the next token is null
            dataAccessService.describeSessionTemplates(describeSessionTemplatesRequest)
                .then(r => {
                    console.log(r.data.SessionTemplates)
                    setSelectedSessionTemplates(prevState => {
                            const newSessionTemplates = r.data.SessionTemplates ? r.data.SessionTemplates.map(sessionTemplate => {
                                return {
                                    label: sessionTemplate.Name,
                                    value: sessionTemplate.Id,
                                    description: sessionTemplate.Description,
                                }
                            }).filter(newOption => !prevState.find(oldOption => oldOption.value == newOption.value)) : []
                            return prevState.concat(newSessionTemplates)
                        }
                    )
                    nextToken = r.data.NextToken
                    console.log("Got next token ", nextToken);
                })
                .catch(e => {
                    console.error("Failed to retrieve session templates shared with group: ", e)
                })
        } while (nextToken !== null);
    }

    const getFilteredSessionTemplates = async (filterString: string, nextToken?: string | null) => {
        const request: DescribeSessionTemplatesRequestData = {
            Names: [{
                Operator: "CONTAINS",
                Value: filterString
            }],
            SortToken: {
                Operator: "DESC",
                Key: "Name"
            },
            NextToken: nextToken,
            MaxResults: SESSION_TEMPLATE_SEARCH_LIST_SIZE
        } as DescribeSessionTemplatesRequestData
        console.log(request)

        return dataAccessService.describeSessionTemplates(request).then(r => {
            return [r.data, r.status !== 200 ? r.statusText : undefined] as
                [DescribeSessionTemplatesResponse, string | undefined];
        }).catch(e => {
            console.error("Failed to retrieve Session Templates", e)
            return [undefined, "Failed to retrieve Session Templates"]
        })
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

        if (group) { // We are modifying an existing group, so we should only add users that are not already members
            describeUsersRequest["UserGroupIds"] = [{
                Operator: "!=",
                Value: group?.UserGroupId
            }]
        }

        return dataAccessService.describeUsers(describeUsersRequest).then(r => {
            return [r.data, r.status !== 200 ? r.statusText : undefined] as
                [DescribeUsersResponse, string | undefined];
        }).catch(e => {
            console.error("Failed to retrieve Users", e)
            return [undefined, "Failed to retrieve Users"]
        })

    }

    useEffect(() => {
        getSessionTemplatesForGroup(group)
        getFilteredSessionTemplates("")
    }, [group])

    return (
        <Grid gridDefinition={[{colspan: 8}, {colspan: 0}]}>
            <form onSubmit={e => e.preventDefault()}>
                <Form
                    variant="embedded"
                    actions={
                        <SpaceBetween direction={"horizontal"} size={"xs"}>
                            <Button
                                formAction={"none"}
                                variant={"normal"}
                                onClick={cancelButtonFunction}>
                                {formConstants.FORM_CANCEL_BUTTON_LABEL}
                            </Button>
                            <Button
                                formAction={"submit"}
                                variant={"primary"}
                                disabled={groupIdErrorText !== ""}
                                onClick={() => {
                                    validateGroupId(groupId)
                                    if (groupIdErrorText !== "") {
                                        return
                                    }
                                    submitButtonFunction({
                                        groupId: groupId,
                                        displayName: displayName,
                                        userIdsToAdd: selectedUsersToAdd.map(option => option.value!),
                                        userIdsToRemove: selectedUsersToRemove.map(option => option.UserId!),
                                        sessionTemplateIdsToPublish:  sessionTemplatesToAdd,
                                        sessionTemplateIdsToUnpublish:  sessionTemplatesToRemove,
                                    })
                                }}>
                                {isEditPage ? formConstants.FORM_EDIT_BUTTON_LABEL : formConstants.FORM_CREATE_BUTTON_LABEL}
                            </Button>
                        </SpaceBetween>
                    }>

                    <Container
                        header={
                            <Header
                                variant={"h1"}
                            >
                                {formConstants.FORM_TITLE}
                            </Header>
                        }>
                        <SpaceBetween size={"m"}>
                            <EditGroupTextField
                                text={displayName}
                                setText={(text: string) => {
                                    setDisplayName(text)
                                }}
                                label={formConstants.EDIT_DISPLAY_NAME_LABEL}
                                descriptionText={formConstants.EDIT_DISPLAY_NAME_DESCRIPTION}
                                placeholderText={formConstants.EDIT_DISPLAY_NAME_PLACEHOLDER}
                                disabled={false}
                            />
                            <EditGroupTextField
                                text={groupId}
                                setText={(text: string) => {
                                    setGroupId(text)
                                    validateGroupId(text)
                                }}
                                label={formConstants.EDIT_ID_LABEL}
                                descriptionText={formConstants.EDIT_ID_DESCRIPTION}
                                placeholderText={formConstants.EDIT_ID_PLACEHOLDER}
                                disabled={isEditPage}
                                errorText={groupIdErrorText}
                            />
                            <hr/>
                            <Header variant={"h3"}
                                description={formConstants.ADD_USERS_DESCRIPTION}>
                                {formConstants.ADD_USERS_TITLE}
                            </Header>
                            <AddUserGroupUsers
                                handleLoadItems={handleLoadUsersForSearch}
                                selectItemsState={selectUsersState}
                                itemsToAdd={selectedUsersToAdd}
                                setItemsToAdd={setSelectedUsersToAdd}
                                formConstants={{
                                    LOADING: formConstants.LOADING_USERS_TEXT,
                                    EMPTY: formConstants.ADD_USER_EMPTY_TEXT,
                                    ERROR: formConstants.USER_SEARCH_ERROR_MESSAGE,
                                    PLACEHOLDER: formConstants.ADD_USER_PLACEHOLDER
                                }}
                            />
                            {isEditPage ? <div>
                                <Header variant={"h3"}
                                    description={formConstants.REMOVE_USERS_DESCRIPTION}>
                                    {formConstants.REMOVE_USERS_TITLE}
                                </Header>
                                <RemoveUserGroupUsers
                                    selectedUsers={selectedUsersToRemove}
                                    setSelectedUsers={setSelectedUsersToRemove}
                                    query={removeUsersTableQuery}
                                    setQuery={setRemoveUsersTableQuery}
                                    usersInGroupFilterToken={usersInGroupFilterToken}
                                /><hr/>
                            </div> : undefined
                            }
                            <Header variant={"h3"}
                                description={formConstants.CHOOSE_TEMPLATES_SUBTITLE}>
                                {formConstants.SESSION_TEMPLATES_TITLE}
                            </Header>
                            <EditUserGroupSessionTemplates
                                handleLoadItems={handleLoadSessionTemplatesForSearch}
                                sessionTemplatesSharedWithGroup={selectedSessionTemplates}
                                setSessionTemplatesSharedWithGroup={handleSessionTemplateSelectionChanged}
                                selectSessionTemplatesState={selectSessionTemplatesState}
                                formConstants={formConstants}
                            />
                        </SpaceBetween>
                    </Container>
                </Form>
            </form>
        </Grid>
    )
}
