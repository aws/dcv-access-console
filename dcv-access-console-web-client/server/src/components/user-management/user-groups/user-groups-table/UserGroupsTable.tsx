// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {CollectionPreferencesProps, SpaceBetween, Table, TableProps} from "@cloudscape-design/components";
import ConsoleHeader from "@/components/common/console-header/ConsoleHeader";
import {GLOBAL_CONSTANTS} from "@/constants/global-constants";
import Button from "@cloudscape-design/components/button";
import * as React from "react";
import {useState} from "react";
import {CancelableEventHandler} from "@cloudscape-design/components/internal/events";
import {LinkProps} from "@cloudscape-design/components/link/interfaces";
import {USER_GROUPS_CONSTANT} from "@/constants/user-groups-constants";
import {USERS_PAGE_CONSTANTS} from "@/constants/users-page-constants";
import {DeleteUserGroupsRequestData, UserGroup} from "@/generated-src/client";
import {
    USER_GROUPS_TABLE_COLUMN_DEFINITIONS
} from "@/components/user-management/user-groups/user-groups-table/UserGroupsTableColumnDefinitions";
import {USER_GROUPS_TABLE_CONSTANTS} from "@/constants/user-groups-table-constants";
import Box from "@cloudscape-design/components/box";
import DeleteUserGroupModal, {
    DeleteUserGroupProps
} from "@/components/user-management/user-groups/delete-user-group-modal/DeleteUserGroupModal";
import TableWithPagination from "@/components/common/table-with-pagination/TableWithPagination";
import {
    DataAccessServiceParams,
    DataAccessServiceResult,
} from "@/components/common/hooks/DataAccessServiceHooks";
import {CONTENT_DISPLAY_OPTIONS, DEFAULT_PREFERENCES} from "@/components/user-management/user-groups/user-groups-table/UserGroupsTableColumnPreferences";
import {useRouter} from "next/navigation";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import {EDIT_USER_GROUP_CONSTANTS} from "@/constants/edit-user-group-constants";
import {CREATE_USER_GROUP_CONSTANTS} from "@/constants/create-user-group-constants";
import FilterBar, {DescribeResponse} from "@/components/common/filter-bar/FilterBar";
import user_groups_search_tokens from "@/generated-src/client/user_groups_search_tokens";
import {
    FILTER_USER_GROUPS_CONSTANTS,
    SEARCH_TOKEN_TO_ID,
    TOKEN_NAME_GROUP_MAP,
    TOKEN_NAMES_MAP
} from "@/constants/filter-user-groups-bar-constants";
import {PropertyFilterQuery} from "@cloudscape-design/collection-hooks";

export type UserGroupsTableProps = {
    selectedUserGroup: UserGroup | undefined,
    setSelectedUserGroup: (userGroup: (UserGroup | unknown)[]) => void,
    addHeader: boolean,
    variant: TableProps.Variant,
    query: PropertyFilterQuery,
    setQuery: (query: PropertyFilterQuery) => void,
    infoLinkFollow: CancelableEventHandler<LinkProps.FollowDetail>,
    dataAccessServiceFunction: (params: DataAccessServiceParams<UserGroup>) => DataAccessServiceResult<UserGroup>,
}

export default function UserGroupsTable(
    {
        selectedUserGroup,
        setSelectedUserGroup,
        addHeader,
        variant,
        query,
        setQuery,
        infoLinkFollow,
        dataAccessServiceFunction
    }:
        UserGroupsTableProps
) {
    const [preferences, setPreferences] = useState<CollectionPreferencesProps.Preferences>(DEFAULT_PREFERENCES)
    const [refreshKey, setRefreshKey] = useState("")
    const [deleteItemsKey, setDeleteItemsKey] = useState("")
    const [modalVisible, setModalVisible] = useState<boolean>(false);
    const [resetPaginationKey, setResetPaginationKey] = useState("")
    const {push} = useRouter()
    const {addFlashBar} = useFlashBarContext()
    const dataAccessService = new DataAccessService()

    const viewUserGroupDetailsButton = () => {
        return <Button variant="normal" disabled={!selectedUserGroup}
                       onClick={() => {
                           push(GLOBAL_CONSTANTS.USER_GROUPS_URL + selectedUserGroup?.UserGroupId)
                       }}>
            {USER_GROUPS_CONSTANT.VIEW_USER_GROUP_DETAILS_BUTTON}
        </Button>
    }

    const editUserGroupButton = () => {
        return <Button variant="normal" disabled={!selectedUserGroup}
                       onClick={() => {
                           push(EDIT_USER_GROUP_CONSTANTS.EDIT_USER_GROUP_URL(selectedUserGroup?.UserGroupId))
                       }}>
            {USER_GROUPS_CONSTANT.EDIT_USER_GROUP_BUTTON}
        </Button>
    }

    const deleteUserGroupButton = () => {
        return <Button variant="normal" disabled={!selectedUserGroup}
                       onClick={() => {
                           setModalVisible(true)
                       }}>
            {USER_GROUPS_CONSTANT.DELETE_USER_GROUP_BUTTON}
        </Button>
    }

    const createUserGroupButton = () => {
        return <Button variant="primary"
                       onClick={() => {
                           push(CREATE_USER_GROUP_CONSTANTS.CREATE_USER_GROUP_URL)
                       }}>
            {USER_GROUPS_CONSTANT.CREATE_USER_GROUP_BUTTON}
        </Button>
    }

    const deleteUserGroup = (deleteUserGroupProps: DeleteUserGroupProps) => {
        const deleteUserGroupsRequest: DeleteUserGroupsRequestData = {
            UserGroupIds: [deleteUserGroupProps.userGroupId!],
            DeleteIfNotEmpty: true
        }
        dataAccessService.deleteUserGroups(deleteUserGroupsRequest).then(result => {
            if (result.data.Error) {
                addFlashBar("error", deleteUserGroupProps.userGroupId, 'An error occurred while deleting user group "' + deleteUserGroupProps.userGroupDisplayName + '" : ' + result.data.Error?.message + '.')
            } else {
                if (result.data.UnsuccessfulList?.length !== 0) {
                    addFlashBar("success", deleteUserGroupProps.userGroupId, 'Successfully deleted user group "' + deleteUserGroupProps.userGroupDisplayName + '".')
                    // reload list
                    setRefreshKey(deleteUserGroupProps.userGroupId!)
                } else {
                    addFlashBar("error", deleteUserGroupProps.userGroupId, 'An error occurred while deleting user group "' + deleteUserGroupProps.userGroupDisplayName + '".')
                }
            }

            setSelectedUserGroup([])
            setDeleteItemsKey(Date.now().toString())
        }).catch(e => {
            console.log("Error while deleting user group ", deleteUserGroupProps.userGroupId, e)
            addFlashBar("error", deleteUserGroupProps.userGroupId, 'An error occurred while deleting user group "' + deleteUserGroupProps.userGroupDisplayName + '".')
        })
    }

    const header = addHeader ? <ConsoleHeader headerDescription={USER_GROUPS_CONSTANT.USER_GROUPS_HEADER_DESCRIPTION}
                       headerTitle={USERS_PAGE_CONSTANTS.USER_GROUPS}
                       infoLinkFollow={infoLinkFollow}
                       infoLinkLabel={GLOBAL_CONSTANTS.INFO_LABEL}
                       actions={
                           <SpaceBetween direction="horizontal" size="xs">
                               {viewUserGroupDetailsButton()}
                               {editUserGroupButton()}
                               {deleteUserGroupButton()}
                               {createUserGroupButton()}
                           </SpaceBetween>
                        }
        /> : undefined

    const empty = <Box textAlign="center" color="inherit">
            <Box
                padding={{bottom: "s"}}
                variant="p"
                color="inherit"
            >
                <b>{USER_GROUPS_TABLE_CONSTANTS.EMPTY_TEXT}</b>
            </Box>
            {query ? undefined : <Button onClick={() => push(CREATE_USER_GROUP_CONSTANTS.CREATE_USER_GROUP_URL)}>{USER_GROUPS_TABLE_CONSTANTS.CREATE_GROUP_TEXT}</Button>}
        </Box>

    const describeUserGroups = async (describeUserGroupsRequest) => {
        const r = await dataAccessService.describeUserGroups(describeUserGroupsRequest)
        return {"objects": r.data.UserGroups, "nextToken": r.data.NextToken} as DescribeResponse
    }

    const filter = <FilterBar
        filteringQuery={query}
        handlePropertyFilteringChange={({detail}) => {
            setQuery(detail)
            setResetPaginationKey(Date.now().toString())
        }}
        searchTokens={user_groups_search_tokens}
        tokenNamesMap={TOKEN_NAMES_MAP}
        tokenNameGroupMap={TOKEN_NAME_GROUP_MAP}
        searchTokenToId={SEARCH_TOKEN_TO_ID}
        filteringPlaceholder={FILTER_USER_GROUPS_CONSTANTS.filteringPlaceholder}
        dataAccessServiceFunction={describeUserGroups}/>

    const table = <Table
        variant={variant}
        columnDefinitions={USER_GROUPS_TABLE_COLUMN_DEFINITIONS}
        columnDisplay={preferences.contentDisplay}
        items={[]}
        selectedItems={selectedUserGroup ? [selectedUserGroup] as ReadonlyArray<UserGroup> : []}
        onSelectionChange={event => setSelectedUserGroup(event.detail.selectedItems)}
        trackBy={USER_GROUPS_TABLE_CONSTANTS.GROUP_ID}
        loadingText={USER_GROUPS_TABLE_CONSTANTS.LOADING_TEXT}
        selectionType="single"
        filter={filter}
        empty={empty}
        resizableColumns={true}
    />

    return (
        <>
            <TableWithPagination
                table={table}
                header={header}
                deleteItemsKey={deleteItemsKey}
                defaultSortingColumn={USER_GROUPS_TABLE_COLUMN_DEFINITIONS[0]}
                query={query}
                preferences={preferences}
                setPreferences={setPreferences}
                dataAccessServiceFunction={dataAccessServiceFunction}
                refreshKey={refreshKey}
                setRefreshKey={setRefreshKey}
                resetPaginationKey={resetPaginationKey}
                contentDisplayOptions={CONTENT_DISPLAY_OPTIONS}
            />

            <DeleteUserGroupModal
                visible={modalVisible}
                setVisible={setModalVisible}
                deleteUserGroup={deleteUserGroup}
                deleteUserGroupProps={{
                    userGroupId: selectedUserGroup?.UserGroupId,
                    userGroupDisplayName: selectedUserGroup?.DisplayName
                }}
            />
        </>
    )
}
