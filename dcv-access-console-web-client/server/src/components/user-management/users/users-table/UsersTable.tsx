import {CollectionPreferencesProps, SpaceBetween, Table, TableProps} from "@cloudscape-design/components";
import ConsoleHeader from "@/components/common/console-header/ConsoleHeader";
import {GLOBAL_CONSTANTS} from "@/constants/global-constants";
import Button from "@cloudscape-design/components/button";
import * as React from "react";
import {useState} from "react";
import {CancelableEventHandler} from "@cloudscape-design/components/internal/events";
import {LinkProps} from "@cloudscape-design/components/link/interfaces";
import {USERS_CONSTANTS} from "@/constants/users-constants";
import {USERS_PAGE_CONSTANTS} from "@/constants/users-page-constants";
import {User} from "@/generated-src/client";
import Box from "@cloudscape-design/components/box";
import {USERS_TABLE_CONSTANTS} from "@/constants/users-table-constants";
import {
    USERS_TABLE_COLUMN_DEFINITIONS
} from "@/components/user-management/users/users-table/UsersTableColumnDefinitions";
import ImportUsersModal, {
    ImportUsersProps
} from "@/components/user-management/users/import-users-modal/ImportUsersModal";
import {IMPORT_USERS_CONSTANTS} from "@/constants/import-users-constants";
import TableWithPagination from "@/components/common/table-with-pagination/TableWithPagination";
import {
    DataAccessServiceParams,
    DataAccessServiceResult
} from "@/components/common/hooks/DataAccessServiceHooks";
import {CONTENT_DISPLAY_OPTIONS} from "@/components/user-management/users/users-table/UsersTableColumnPreferences";
import {useRouter} from "next/navigation";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import FilterBar, {DescribeResponse} from "@/components/common/filter-bar/FilterBar";
import {
    FILTER_USERS_CONSTANTS,
    SEARCH_TOKEN_TO_ID,
    TOKEN_NAME_GROUP_MAP
} from "@/constants/filter-users-bar-constants";
import users_search_tokens from "@/generated-src/client/users_search_tokens";
import {PropertyFilterQuery, PropertyFilterToken} from "@cloudscape-design/collection-hooks";

export type UsersTableProps = {
    selectedUsers: User[] | undefined,
    setSelectedUsers: (user: (User | unknown)[]) => void,
    addHeader: boolean,
    variant: TableProps.Variant,
    selectionType: TableProps.SelectionType,
    columnPreferences: CollectionPreferencesProps.Preferences,
    query: PropertyFilterQuery,
    setQuery: (query: PropertyFilterQuery) => void,
    usersInGroupFilterToken?: PropertyFilterToken,
    filterBarQuery?: PropertyFilterQuery,
    setFilterBarQuery?: (query: PropertyFilterQuery) => void,
    filterTokenNamesMap: Record<string, string>,
    infoLinkFollow?: CancelableEventHandler<LinkProps.FollowDetail>,
    dataAccessServiceFunction: (params: DataAccessServiceParams<User>) => DataAccessServiceResult<User>
}

export default function UsersTable(
    {
        selectedUsers,
        setSelectedUsers,
        addHeader,
        variant,
        selectionType,
        columnPreferences,
        query,
        setQuery,
        usersInGroupFilterToken,
        filterBarQuery,
        setFilterBarQuery,
        filterTokenNamesMap,
        infoLinkFollow,
        dataAccessServiceFunction
    }:
        UsersTableProps
) {
    const [preferences, setPreferences] = useState<CollectionPreferencesProps.Preferences>(columnPreferences)
    const [refreshKey, setRefreshKey] = useState("")
    const [modalVisible, setModalVisible] = useState<boolean>(false)
    const [resetPaginationKey, setResetPaginationKey] = useState("")
    const {push} = useRouter()
    const {addFlashBar, addLoadingFlashBar, removeFromFlashBar} = useFlashBarContext()
    const dataAccessService = new DataAccessService()

    const viewUserDetailsButton = () => {
        return <Button variant="normal" disabled={!selectedUsers?.length}
                       onClick={() => {
                           if (selectedUsers) {
                               push(GLOBAL_CONSTANTS.USERS_URL + selectedUsers[0]?.UserId)
                           }
                       }}>
            {USERS_CONSTANTS.VIEW_USER_DETAILS_BUTTON}
        </Button>
    }

    const importUsersButton = () => {
        return <Button
            variant="primary"
            onClick={() => {
                setModalVisible(true)
            }}>
            {IMPORT_USERS_CONSTANTS.IMPORT_USERS_TEXT}
        </Button>
    }

    const importUsers = (importUsersProps: ImportUsersProps) => {
        addLoadingFlashBar(importUsersProps.file?.name, "Importing users")
        new DataAccessService().importUsers(importUsersProps.file, importUsersProps.overwriteExistingUsers, importUsersProps.overwriteGroups).then(r => {
            removeFromFlashBar(importUsersProps.file?.name, "message")
            if (r.data.Error) {
                addFlashBar("error", importUsersProps.file?.name, 'Failed to import users. Please try again.')
            }
            if (r.data.UnsuccessfulUsersList?.length) {
                addFlashBar("warning", importUsersProps.file?.name + 'unsuccessful', 'Failed to import [' + r.data.UnsuccessfulUsersList.length + '] users. Please try again.')
            }
            if (r.data.SuccessfulUsersList?.length) {
                addFlashBar("success", importUsersProps.file?.name + 'successful', 'Successfully imported [' + r.data.SuccessfulUsersList.length + '] users.')
                setRefreshKey(Date.now().toString())
                setResetPaginationKey(Date.now().toString())
            }
        }).catch(e => {
            removeFromFlashBar(importUsersProps.file?.name, "message")
            console.log("Error while importing users", e)
            addFlashBar("error", importUsersProps.file?.name, 'Failed to import users. Please try again.')
        })
    }

    const header = addHeader ? <ConsoleHeader headerDescription={USERS_CONSTANTS.USERS_HEADER_DESCRIPTION}
                       headerTitle={USERS_PAGE_CONSTANTS.USERS}
                       infoLinkFollow={infoLinkFollow}
                       infoLinkLabel={GLOBAL_CONSTANTS.INFO_LABEL}
                       actions={
                           <SpaceBetween direction="horizontal" size="xs">
                               {viewUserDetailsButton()}
                               {importUsersButton()}
                           </SpaceBetween>
                        }
        /> : undefined

    const empty = <Box textAlign="center" color="inherit">
            <Box
                padding={{bottom: "s"}}
                variant="p"
                color="inherit"
            >
                <b>{USERS_TABLE_CONSTANTS.EMPTY_TEXT}</b>
            </Box>
        </Box>

    const describeUsers = async (describeUsersRequest) => {
        const r = await dataAccessService.describeUsers(describeUsersRequest)
        return {"objects": r.data.Users, "nextToken": r.data.NextToken} as DescribeResponse
    }

    const filter = <FilterBar
        filteringQuery={filterBarQuery ? filterBarQuery : query}
        handlePropertyFilteringChange={({detail}) => {
            setQuery(prev => {
                if (setFilterBarQuery) {
                    let tokens = [...detail.tokens]
                    tokens.push(usersInGroupFilterToken)
                    const updatedQuery = {
                        operation: "and",
                        tokens: tokens
                    }
                    return updatedQuery
                }
                return detail
            })
            if (setFilterBarQuery) {
                setFilterBarQuery(detail)
            }
            setResetPaginationKey(Date.now().toString())
        }}
        searchTokens={users_search_tokens}
        tokenNamesMap={filterTokenNamesMap}
        tokenNameGroupMap={TOKEN_NAME_GROUP_MAP}
        searchTokenToId={SEARCH_TOKEN_TO_ID}
        filteringPlaceholder={FILTER_USERS_CONSTANTS.filteringPlaceholder}
        dataAccessServiceFunction={describeUsers}/>

    const table = <Table
        variant={variant}
        columnDefinitions={USERS_TABLE_COLUMN_DEFINITIONS}
        columnDisplay={preferences.contentDisplay}
        items={[]}
        selectedItems={selectedUsers ? selectedUsers as ReadonlyArray<User> : []}
        onSelectionChange={event => setSelectedUsers(event.detail.selectedItems)}
        trackBy={USERS_TABLE_CONSTANTS.USER_ID}
        loadingText={USERS_TABLE_CONSTANTS.LOADING_TEXT}
        selectionType={selectionType}
        filter={filter}
        empty={empty}
        resizableColumns={true}
    />

    return (
        <>
            <TableWithPagination
                table={table}
                header={header}
                defaultSortingColumn={USERS_TABLE_COLUMN_DEFINITIONS[1]}
                query={query}
                dataAccessServiceFunction={dataAccessServiceFunction}
                preferences={preferences}
                setPreferences={setPreferences}
                refreshKey={refreshKey}
                setRefreshKey={setRefreshKey}
                resetPaginationKey={resetPaginationKey}
                contentDisplayOptions={CONTENT_DISPLAY_OPTIONS}
            />
            <ImportUsersModal
                visible={modalVisible}
                setVisible={setModalVisible}
                importUsers={importUsers}
            />
        </>
    )
}