import * as React from "react";
import {useState} from "react";
import Table from "@cloudscape-design/components/table";
import Box from "@cloudscape-design/components/box";
import {
    SESSION_TEMPLATES_TABLE_COLUMN_DEFINITIONS,
} from "@/components/session-templates/session-templates-table/SessionTemplatesTableColumnDefinitions";
import {
    CONTENT_DISPLAY_OPTIONS
} from "@/components/session-templates/session-templates-table/SessionTemplatesTableColumnPreferences";

import {
    DeleteSessionTemplatesRequestData,
    DescribeSessionTemplatesRequestData,
    SessionTemplate
} from "@/generated-src/client";
import {SESSION_TEMPLATES_TABLE_CONSTANTS} from "@/constants/session-templates-table-constants";
import {CollectionPreferencesProps, SpaceBetween, TableProps} from "@cloudscape-design/components";
import ConsoleHeader from "@/components/common/console-header/ConsoleHeader";
import Button from "@cloudscape-design/components/button";
import {useRouter} from "next/navigation";
import {GLOBAL_CONSTANTS} from "@/constants/global-constants";
import {DataAccessServiceParams, DataAccessServiceResult} from "@/components/common/hooks/DataAccessServiceHooks";
import {
    FILTER_SESSION_TEMPLATES_CONSTANTS,
    SEARCH_TOKEN_TO_ID,
    TOKEN_NAME_GROUP_MAP,
    TOKEN_NAMES_MAP
} from "@/constants/filter-session-templates-bar-constants";
import ButtonDropdown from "@cloudscape-design/components/button-dropdown";
import {SESSION_TEMPLATES_CREATE_CONSTANTS} from "@/constants/session-templates-create-constants";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import DeleteSessionTemplateModal, {
    DeleteSessionTemplateProps
} from "@/components/session-templates/delete-session-template/DeleteSessionTemplateModal";
import {CancelableEventHandler} from "@cloudscape-design/components/internal/events";
import {LinkProps} from "@cloudscape-design/components/link/interfaces";
import TableWithPagination from "@/components/common/table-with-pagination/TableWithPagination";
import FilterBar, {DescribeResponse} from "@/components/common/filter-bar/FilterBar";
import session_template_search_tokens from "@/generated-src/client/session_template_search_tokens";
import {PropertyFilterQuery} from "@cloudscape-design/collection-hooks";

export default function SessionTemplatesTable({   selectedSessionTemplates,
                                                  setSelectedSessionTemplates,
                                                  addHeader,
                                                  allowSessionTemplateManagement = false,
                                                  variant,
                                                  query,
                                                  setQuery,
                                                  infoLinkFollow,
                                                  dataAccessServiceFunction,
                                                  columnPreferences,
                                                  multiselect,
                                              }: {
    selectedSessionTemplates: SessionTemplate[],
    setSelectedSessionTemplates:  React.Dispatch<React.SetStateAction<SessionTemplate[]>>,
    addHeader: boolean,
    allowSessionTemplateManagement: boolean,
    variant: TableProps.Variant,
    query: PropertyFilterQuery,
    setQuery: (query: PropertyFilterQuery) => void,
    infoLinkFollow?: CancelableEventHandler<LinkProps.FollowDetail>,
    dataAccessServiceFunction: (params: DataAccessServiceParams<SessionTemplate>) => DataAccessServiceResult<SessionTemplate>
    columnPreferences: CollectionPreferencesProps.Preferences,
    multiselect?: boolean
}) {
    const [preferences, setPreferences] = useState<CollectionPreferencesProps.Preferences>(columnPreferences)
    const [refreshKey, setRefreshKey] = useState("")
    const [deleteItemsKey, setDeleteItemsKey] = useState("")
    const [modalVisible, setModalVisible] = useState<boolean>(false);
    const [deleteSessionTemplateProps, setDeleteSessionTemplateProps] = useState<DeleteSessionTemplateProps>()
    const [resetPaginationKey, setResetPaginationKey] = useState("")
    const {push} = useRouter()
    const {addFlashBar} = useFlashBarContext()
    const dataAccessService = new DataAccessService()

    const describeSessionTemplates = async (describeSessionTemplatesRequest: DescribeSessionTemplatesRequestData) => {
        const r = await dataAccessService.describeSessionTemplates(describeSessionTemplatesRequest)
        return {"objects": r.data.SessionTemplates, "nextToken": r.data.NextToken} as DescribeResponse
    }

    const filter = <FilterBar
        filteringQuery={query}
        handlePropertyFilteringChange={({detail}) => {
            setQuery(detail)
            setResetPaginationKey(Date.now().toString())
        }}
        searchTokens={session_template_search_tokens}
        tokenNamesMap={TOKEN_NAMES_MAP}
        tokenNameGroupMap={TOKEN_NAME_GROUP_MAP}
        searchTokenToId={SEARCH_TOKEN_TO_ID}
        filteringPlaceholder={FILTER_SESSION_TEMPLATES_CONSTANTS.filteringPlaceholder}
        dataAccessServiceFunction={describeSessionTemplates}/>

    const viewUserDetailsButton = () => {
        return <Button variant="normal" disabled={selectedSessionTemplates?.length != 1}
                       onClick={() => {
                           push(GLOBAL_CONSTANTS.SESSION_TEMPLATES_URL + selectedSessionTemplates[0].Id)
                       }}>
            {SESSION_TEMPLATES_TABLE_CONSTANTS.VIEW_USER_DETAILS_BUTTON}
        </Button>
    }

    const createSessionTemplateButton = () => {
        if (allowSessionTemplateManagement) {
            return <Button variant="primary"
                           onClick={() => {
                               push(SESSION_TEMPLATES_CREATE_CONSTANTS.CREATE_TEMPLATE_URL)
                           }}>
                {SESSION_TEMPLATES_TABLE_CONSTANTS.CREATE_TEMPLATE_TEXT}
            </Button>
        }
        return <></>
    }

    const createActionsButton = () => {
        if (allowSessionTemplateManagement) {
            return <ButtonDropdown
                items={[
                    {
                        id: "assign",
                        text: SESSION_TEMPLATES_CREATE_CONSTANTS.ASSIGN_USERS,
                        href: SESSION_TEMPLATES_CREATE_CONSTANTS.ASSIGN_USERS_URL(selectedSessionTemplates[0]?.Id),
                        disabled: selectedSessionTemplates.length != 1
                    },
                    {
                        id: "duplicate",
                        text: SESSION_TEMPLATES_CREATE_CONSTANTS.DUPLICATE,
                        href: SESSION_TEMPLATES_CREATE_CONSTANTS.CREATE_TEMPLATE_URL + "?templateId=" + selectedSessionTemplates[0]?.Id,
                        disabled: selectedSessionTemplates.length != 1
                    },
                    {
                        id: "edit",
                        text: SESSION_TEMPLATES_CREATE_CONSTANTS.EDIT,
                        href: SESSION_TEMPLATES_CREATE_CONSTANTS.EDIT_TEMPLATE_URL(selectedSessionTemplates[0]?.Id),
                        disabled: selectedSessionTemplates.length != 1
                    },
                    {
                        id: "delete",
                        text: SESSION_TEMPLATES_CREATE_CONSTANTS.DELETE,
                    }
                ]}
                onItemClick={(event) => {
                    if (event.detail.id === "delete") {
                        setDeleteSessionTemplateProps({
                            sessionTemplateIds: selectedSessionTemplates!.map((st) => st.Id!),
                            sessionTemplateNames: selectedSessionTemplates!.map((st) => st.Name!)
                        })
                        setModalVisible(true)
                    }
                }}
                disabled={selectedSessionTemplates.length == 0}
            >
                {GLOBAL_CONSTANTS.ACTIONS}
            </ButtonDropdown>
        }
        return <></>
    }
    const deleteSessionTemplate = (deleteSessionTemplateProps: DeleteSessionTemplateProps) => {
        dataAccessService.deleteSessionTemplate({Ids: deleteSessionTemplateProps.sessionTemplateIds} as DeleteSessionTemplatesRequestData).then(result => {
            console.log("Deleting session template: ", result)
            if (result.data.UnsuccessfulList) {
                if (result.data.UnsuccessfulList.length === 1) {
                    const template = result.data.UnsuccessfulList[0]
                    addFlashBar("error", template, `An error occurred while deleting "${deleteSessionTemplateProps.sessionTemplateNames[0]}": ${result.data.Error?.message}.`)
                } else if (result.data.UnsuccessfulList.length !== 0) {
                    console.log("Request to delete session templates failed due to: " + result.data.Error?.message)
                    result.data.UnsuccessfulList.forEach(template => {
                        console.log("Failed to delete session template: ", template)
                    })
                    addFlashBar("error", result.data.UnsuccessfulList.toString(), `Failed to delete ${result.data.UnsuccessfulList.length} session templates: ${result.data.Error?.message}.`)
                }
            }

            if (result.data.SuccessfulList) {
                if (result.data.SuccessfulList.length === 1) {
                    const template = result.data.SuccessfulList[0]
                    addFlashBar("success", template, `Successfully deleted session template "${deleteSessionTemplateProps.sessionTemplateNames[0]}".`)
                } else if (result.data.SuccessfulList.length != 0) {
                    addFlashBar("success", result.data.SuccessfulList?.toString(), `Successfully deleted ${result.data.SuccessfulList.length} session templates.`)
                }
            }

            // reload list
            setRefreshKey(Date.now().toString())
            setDeleteItemsKey(Date.now().toString())
            setSelectedSessionTemplates([])
        }).catch(e => {
            console.log("Error while deleting session template(s) ", deleteSessionTemplateProps.sessionTemplateIds.toString(), e)
            addFlashBar("error", deleteSessionTemplateProps.sessionTemplateIds.toString(), 'An error occurred while deleting session template(s) "' + deleteSessionTemplateProps.sessionTemplateNames.toString() + '".')
        })
    }

    const header = addHeader ? <ConsoleHeader
                                  headerDescription={SESSION_TEMPLATES_TABLE_CONSTANTS.HEADER_DESCRIPTION}
                                  headerTitle={SESSION_TEMPLATES_TABLE_CONSTANTS.SESSION_TEMPLATES}
                                  infoLinkFollow={infoLinkFollow}
                                  infoLinkLabel={GLOBAL_CONSTANTS.INFO_LABEL}
                                  actions={<SpaceBetween direction="horizontal" size="xs">
                                      {viewUserDetailsButton()}
                                      {createActionsButton()}
                                      {createSessionTemplateButton()}
                                  </SpaceBetween>}
                                  flashItems={[]}/> : undefined;

    const contactAdmin = () => {
        if (!allowSessionTemplateManagement) {
            return <>
                    <br/>
                    <b>{SESSION_TEMPLATES_TABLE_CONSTANTS.CONTACT_TEXT}</b>
                </>
        }
    }

    const table = <Table
        variant={variant}
        columnDefinitions={SESSION_TEMPLATES_TABLE_COLUMN_DEFINITIONS}
        columnDisplay={preferences.contentDisplay}
        selectedItems={selectedSessionTemplates as ReadonlyArray<SessionTemplate>}
        onSelectionChange={event => setSelectedSessionTemplates(event.detail.selectedItems)}
        trackBy={SESSION_TEMPLATES_TABLE_CONSTANTS.ID}
        loadingText={SESSION_TEMPLATES_TABLE_CONSTANTS.LOADING_TEXT}
        selectionType={multiselect ? "multi" : "single"}
        sortingDisabled={true}  // Delete this line it to reenable sorting (once sorting has been fixed post-pagination)
        filter={filter}
        header={header}
        empty={
            <Box textAlign="center" color="inherit">
                <Box
                    padding={{bottom: "s"}}
                    variant="p"
                    color="inherit"
                >
                    <b>{SESSION_TEMPLATES_TABLE_CONSTANTS.EMPTY_TEXT}</b>
                    {contactAdmin()}
                </Box>
                {query ? undefined : <Button onClick={() => {
                    push(SESSION_TEMPLATES_CREATE_CONSTANTS.CREATE_TEMPLATE_URL)
                }}>{SESSION_TEMPLATES_TABLE_CONSTANTS.CREATE_TEMPLATE_TEXT}</Button>}
            </Box>
        }
        resizableColumns={true}
        items={[]}
    />

    return (
        <>
            <TableWithPagination
                table={table}
                header={header}
                defaultSortingColumn={SESSION_TEMPLATES_TABLE_COLUMN_DEFINITIONS[0]}
                query={query}
                dataAccessServiceFunction={dataAccessServiceFunction}
                preferences={preferences}
                setPreferences={setPreferences}
                refreshKey={refreshKey}
                setRefreshKey={setRefreshKey}
                deleteItemsKey={deleteItemsKey}
                resetPaginationKey={resetPaginationKey}
                contentDisplayOptions={CONTENT_DISPLAY_OPTIONS}
            />
            <DeleteSessionTemplateModal visible={modalVisible} setVisible={setModalVisible}
                                        deleteSessionTemplateProps={deleteSessionTemplateProps!}
                                        deleteSessionTemplate={deleteSessionTemplate}/>
        </>
    )
}
