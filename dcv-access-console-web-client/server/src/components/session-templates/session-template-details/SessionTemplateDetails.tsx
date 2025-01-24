import {
    DeleteSessionTemplatesRequestData,
    DescribeUserGroupsSharedWithSessionTemplateRequestData,
    DescribeUsersSharedWithSessionTemplateRequestData,
    SessionTemplate,
} from "@/generated-src/client";
import {ContentLayout, SpaceBetween, StatusIndicator} from "@cloudscape-design/components";
import * as React from "react";
import {useEffect, useState} from "react";
import {getValueOrUnknown} from "@/components/common/utils/SearchUtils";
import Box from "@cloudscape-design/components/box";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import {useRouter} from "next/navigation";
import SessionTemplateOverview from "@/components/session-templates/session-template-overview/SessionTemplateOverview";
import {SESSION_TEMPLATES_DETAILS_CONSTANTS} from "@/constants/session-templates-details-constants";
import UsersTab from "@/components/session-templates/session-templates-split-panel/users-tab/UsersTab";
import GroupsTab from "@/components/session-templates/session-templates-split-panel/groups-tab/GroupsTab";
import {UserGroupsState, UsersState} from "@/components/user-management/common/SplitPanelStates";
import {SESSION_TEMPLATES_CREATE_CONSTANTS} from "@/constants/session-templates-create-constants";
import {GLOBAL_CONSTANTS} from "@/constants/global-constants";
import ButtonDropdown from "@cloudscape-design/components/button-dropdown";
import DeleteSessionTemplateModal
    , {
    DeleteSessionTemplateProps
} from "@/components/session-templates/delete-session-template/DeleteSessionTemplateModal";
import HeaderWithCounter from "@/components/common/header-with-counter/HeaderWithCounter";

export type SessionTemplateDetailsProps = {
    sessionTemplate: SessionTemplate | undefined,
    loading: boolean,
    error: boolean,
}
export default function SessionTemplateDetails(props: SessionTemplateDetailsProps) {
    const dataAccessService = new DataAccessService()

    const [usersState, setUsersState] = useState<UsersState>({
        users: [],
        loading: true,
        error: false
    })

    const [userGroupsState, setUserGroupsState] = useState<UserGroupsState>({
        groups: [],
        loading: true,
        error: false
    })

    const [refreshKey, setRefreshKey] = useState("")
    const [resetPaginationKey, setResetPaginationKey] = useState("")
    const [modalVisible, setModalVisible] = useState<boolean>(false)
    const [deleteSessionTemplateProps, setDeleteSessionTemplateProps] = useState<DeleteSessionTemplateProps>()
    const {push} = useRouter()
    const {addFlashBar} = useFlashBarContext()

    const getUsersForSessionTemplate = (sessionTemplate: SessionTemplate) => {
        // TODO: Paginate
        const describeUsersSharedWithSessionTemplateRequest: DescribeUsersSharedWithSessionTemplateRequestData = {
            SessionTemplateId: sessionTemplate.Id
        } as DescribeUsersSharedWithSessionTemplateRequestData

        console.log(describeUsersSharedWithSessionTemplateRequest)
        dataAccessService.describeUsersSharedWithSessionTemplate(describeUsersSharedWithSessionTemplateRequest)
            .then(r => {
                setUsersState(prevState => {
                    return {
                        ...prevState,
                        users: r.data.Users || [],
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

    const getGroupsForSessionTemplate = (sessionTemplate: SessionTemplate) => {
        // TODO: Paginate
        const describeUserGroupsSharedWithSessionTemplateRequest: DescribeUserGroupsSharedWithSessionTemplateRequestData = {
            SessionTemplateId: sessionTemplate.Id
        } as DescribeUserGroupsSharedWithSessionTemplateRequestData

        console.log(describeUserGroupsSharedWithSessionTemplateRequest)
        dataAccessService.describeUserGroupsSharedWithSessionTemplate(describeUserGroupsSharedWithSessionTemplateRequest)
            .then(r => {
                setUserGroupsState(prevState => {
                    return {
                        ...prevState,
                        groups: r.data.UserGroups || [],
                        loading: false,
                        error: false
                    }
                })
            }).catch(e => {
            console.error("Failed to retrieve groups: ", e)
            setUserGroupsState(prevState => {
                return {
                    ...prevState,
                    loading: false,
                    error: true,
                    errorMessage: e.errorMessage
                }
            })
        })
    }

    const deleteSessionTemplate = (deleteSessionTemplateProps: DeleteSessionTemplateProps) => {
        new DataAccessService().deleteSessionTemplate({Ids: [deleteSessionTemplateProps.sessionTemplateIds[0]]} as DeleteSessionTemplatesRequestData).then(result => {
                result.data.UnsuccessfulList?.forEach(template => {
                    addFlashBar("error", deleteSessionTemplateProps.sessionTemplateIds[0], 'Error while deleting "' + deleteSessionTemplateProps.sessionTemplateNames[0] + '" : ' + result.data.Error?.message)
                })
                result.data.SuccessfulList?.forEach(template => {
                    addFlashBar("success", deleteSessionTemplateProps.sessionTemplateIds[0], 'Successfully deleted session template "' + deleteSessionTemplateProps.sessionTemplateNames[0] + '"')
                    push(GLOBAL_CONSTANTS.SESSION_TEMPLATES_URL)
                })
            }
        ).catch(e => {
            console.log("Error while deleting session template ", deleteSessionTemplateProps.sessionTemplateIds[0], e)
            addFlashBar("error", deleteSessionTemplateProps.sessionTemplateIds[0], 'Error while deleting session template "' + deleteSessionTemplateProps.sessionTemplateNames[0] + '"')
        })
    }

    useEffect(() => {
        setRefreshKey(Date.now().toString())
    }, [useFlashBarContext().items])

    useEffect(() => {
        if (props.sessionTemplate) {
            getUsersForSessionTemplate(props.sessionTemplate)
            getGroupsForSessionTemplate(props.sessionTemplate)
        }
        setResetPaginationKey(Date.now().toString())
    }, [props.sessionTemplate])

    const actionsButton = () => {
        return <ButtonDropdown
            items={[
                {
                    id: "assign",
                    text: SESSION_TEMPLATES_CREATE_CONSTANTS.ASSIGN_USERS,
                    href: SESSION_TEMPLATES_CREATE_CONSTANTS.ASSIGN_USERS_URL(props.sessionTemplate?.Id),
                },
                {
                    id: "duplicate",
                    text: SESSION_TEMPLATES_CREATE_CONSTANTS.DUPLICATE,
                    href: SESSION_TEMPLATES_CREATE_CONSTANTS.CREATE_TEMPLATE_URL + "?templateId=" + props.sessionTemplate?.Id,
                },
                {
                    id: "edit",
                    text: SESSION_TEMPLATES_CREATE_CONSTANTS.EDIT,
                    href: SESSION_TEMPLATES_CREATE_CONSTANTS.EDIT_TEMPLATE_URL(props.sessionTemplate?.Id),
                },
                {
                    id: "delete",
                    text: SESSION_TEMPLATES_CREATE_CONSTANTS.DELETE,
                }
            ]}
            onItemClick={(event) => {
                if (event.detail.id === "delete") {
                    setDeleteSessionTemplateProps({
                        sessionTemplateIds: [props.sessionTemplate!.Id!],
                        sessionTemplateNames: [props.sessionTemplate!.Name!]
                    })
                    setModalVisible(true)
                }
            }}
            disabled={props.sessionTemplate == undefined}
        >
            {GLOBAL_CONSTANTS.ACTIONS}
        </ButtonDropdown>
    }

    let content

    if (props.loading) {
        content = <ContentLayout>
            <SpaceBetween size={"l"} direction={"vertical"}>
                <Box textAlign="center" color="inherit">
                    <StatusIndicator type="loading">
                        {SESSION_TEMPLATES_DETAILS_CONSTANTS.LOADING_TEXT}
                    </StatusIndicator>
                </Box>
            </SpaceBetween>
        </ContentLayout>
    } else if (props.error) {
        content =  <ContentLayout>
            <Box textAlign="center" color="inherit">
                <StatusIndicator type="error">
                    {SESSION_TEMPLATES_DETAILS_CONSTANTS.ERROR_TEXT}
                </StatusIndicator>
            </Box>
        </ContentLayout>
    } else {
        content = <>
            <ContentLayout
                header={<HeaderWithCounter actions={actionsButton()}>{getValueOrUnknown(props.sessionTemplate?.Name)}</HeaderWithCounter>}
            >
                <SpaceBetween size={"l"}>
                    <SessionTemplateOverview sessionTemplate={props.sessionTemplate!}/>
                    <UsersTab users={usersState.users || []} sessionTemplateId={props.sessionTemplate?.Id}/>
                    <GroupsTab groups={userGroupsState.groups || []} sessionTemplateId={props.sessionTemplate?.Id}/>
                </SpaceBetween>
            </ContentLayout>
            <DeleteSessionTemplateModal visible={modalVisible} setVisible={setModalVisible}
                                        deleteSessionTemplateProps={deleteSessionTemplateProps!}
                                        deleteSessionTemplate={deleteSessionTemplate}/>
        </>
    }

    return content
}
