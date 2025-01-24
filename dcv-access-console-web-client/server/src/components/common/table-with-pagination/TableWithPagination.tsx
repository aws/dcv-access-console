import * as React from "react";
import {cloneElement, useEffect, useState} from "react";
import {CollectionPreferences, CollectionPreferencesProps, TableProps} from "@cloudscape-design/components";
import {DEFAULT_PAGE_SIZE_PREFERENCES} from "@/components/common/table-with-pagination/PaginationTableColumnPreferences";
import {DataAccessServiceParams, DataAccessServiceResult} from "@/components/common/hooks/DataAccessServiceHooks";
import Pagination from "@cloudscape-design/components/pagination";
import {GENERIC_TABLE_CONSTANTS} from "@/constants/generic-table-constants";
import {PropertyFilterQuery} from "@cloudscape-design/collection-hooks";

export type TableWithPaginationProps<T> = {
    table: JSX.Element,
    header?: JSX.Element | undefined,
    defaultSortingColumn: TableProps.SortingColumn<T>,
    query: PropertyFilterQuery,
    dataAccessServiceFunction: (params: DataAccessServiceParams<T>) => DataAccessServiceResult<T>,
    extraRequestsParams?: { [key: string]: string },
    preferences: CollectionPreferencesProps.Preferences,
    setPreferences: (preferences: CollectionPreferencesProps.Preferences) => void,
    refreshKey: string,
    setRefreshKey: (refreshKey: string) => void,
    resetPaginationKey: string,
    deleteItemsKey?: string, // This needs to be changed whenever items are deleted, so the table can invalidate the nextTokens
    customPageSizePreferences?: CollectionPreferencesProps.PageSizePreference,
    customPreference?: (value: string, setValue: (value: string) => void) => JSX.Element
    setView?: (view : string) => void
    contentDisplayOptions:  readonly CollectionPreferencesProps.ContentDisplayOption[]
}

export default function TableWithPagination<T>(
    {
        table,
        header,
        defaultSortingColumn,
        query,
        dataAccessServiceFunction,
        extraRequestsParams,
        preferences,
        setPreferences,
        refreshKey,
        setRefreshKey,
        deleteItemsKey,
        resetPaginationKey,
        customPageSizePreferences,
        customPreference,
        setView,
        contentDisplayOptions
    }: TableWithPaginationProps<T>) {

    const [currentPageIndex, setCurrentPageIndex] = useState(1)
    const [pageTokens, setPageTokens] = useState<(string | null)[]>([null])
    const [pagesCount, setPagesCount] = useState(1)
    const [openEnd, setOpenEnd] = useState(true)
    const [totalCount, setTotalCount] = useState(0)

    const [sortingColumn, setSortingColumn] = useState(defaultSortingColumn)
    const [sortingDescending, setSortingDescending] = useState(false)

    const [pageSizePreferences, _] = useState<CollectionPreferencesProps.PageSizePreference>(customPageSizePreferences || DEFAULT_PAGE_SIZE_PREFERENCES)

    const params: DataAccessServiceParams<T> = {
        pagination: {
            currentPageIndex: currentPageIndex,
            pageSize: preferences?.pageSize || 20,
            nextToken: pageTokens[currentPageIndex - 1]
        },
        sorting: {
            sortingDescending: sortingDescending,
            sortingColumn: sortingColumn,
        },
        filtering: {
            filteringTokens: query.tokens,
            filteringOperation: query.operation
        },
        refreshKey: refreshKey,
        extraRequestParams: extraRequestsParams
    }

    const result: DataAccessServiceResult<T> = dataAccessServiceFunction(params);

    useEffect(() => {
        // Check if this page has no items
        if (!result.loading && result.items.length === 0 && currentPageIndex !== 0) {
            // Remove the token for the empty page
            setPageTokens(pageTokens.filter((_, index) => index !== currentPageIndex - 1))
            // Reduce the number of pages
            setPagesCount(pagesCount - 1)
            // Move the user to the previous page
            setCurrentPageIndex(currentPageIndex - 1)

            setOpenEnd(false)
        // Check if we've navigated to a new page that we haven't seen before
        } else if (currentPageIndex === pageTokens.length && (result.nextToken != '')) {
            // Check if there are no more pages remaining. If so, we know exactly how many pages there are
            if (result.nextToken === null) {
                setPagesCount(pageTokens.length)
                setOpenEnd(false)
            // There are pages remaining, so add the nextToken to our list of nextTokens and add a page
            } else {
                setPagesCount(pageTokens.length + 1)
                setPageTokens([...pageTokens, result.nextToken])
            }
            // This is the first page, so we know there is at least as many items as the result returned.
            if (currentPageIndex === 1) {
                setTotalCount(result.items.length)
            } else {
                // This is not the first page, so there are at least as many items as pages multiplied by the items
                // per page, plus the number of items in the result we just got.
                setTotalCount((currentPageIndex - 1) * (preferences?.pageSize || 20) + result.items.length)
            }
        }
    }, [result.items])

    useEffect(() => {
        // We are not on the last page, so we are openended again
        if (currentPageIndex < pageTokens.length) {
            setOpenEnd(true)
        }
        // We only know that the total is equal to the number of pages still loaded, plus the number of items on this page
        setTotalCount((currentPageIndex - 1) * (preferences?.pageSize || 20) + result.items.length)
        // Invalidate all the nextTokens after the current page
        setPageTokens(pageTokens.slice(0, currentPageIndex))
    }, [deleteItemsKey])

    const resetPagination = () => {
        setPageTokens([null])
        setOpenEnd(true)
        setTotalCount(0)

        // Ensure the data access hook is called exactly once
        if (currentPageIndex == 1) {
            setRefreshKey(Date.now().toString())
        } else {
            setCurrentPageIndex(1)
        }
    }

    useEffect(() => {
        resetPagination();
    }, [preferences.pageSize, resetPaginationKey])

    const handleSortingChange = event => {
        setSortingDescending(event.detail.isDescending)
        setSortingColumn(event.detail.sortingColumn)
    }

    const handlePageChange = event => {
        setCurrentPageIndex(event.detail.currentPageIndex);
    }

    if (header){
        header = cloneElement(
            header,
            {
                counter: totalCount,
                openEnd: openEnd,
                loading: result.loading,
            })
    }


    return cloneElement(
        table,
        {
            header: header,
            pagination: <Pagination
                currentPageIndex={currentPageIndex}
                onChange={handlePageChange}
                pagesCount={pagesCount}
                openEnd={openEnd}
                disabled={result.loading}
            />,
            preferences: <CollectionPreferences
                onConfirm={({detail}) => {
                    setPreferences(detail)
                    if (setView) {
                        setView(detail.custom)
                    }
                }}
                preferences={preferences}
                pageSizePreference={pageSizePreferences}
                customPreference={customPreference}
                title={GENERIC_TABLE_CONSTANTS.COLLECTION_PREFERENCES}
                cancelLabel={GENERIC_TABLE_CONSTANTS.COLLECTION_PREFERENCES_CANCEL}
                confirmLabel={GENERIC_TABLE_CONSTANTS.COLLECTION_PREFERENCES_CONFIRM}
                contentDisplayPreference={{
                    title: GENERIC_TABLE_CONSTANTS.COLUMN_PREFERENCES_DESCRIPTION,
                    options: contentDisplayOptions
                }}
            />,
            onSortingChange: handleSortingChange,
            sortingColumn: sortingColumn,
            sortingDescending: sortingDescending,
            items: result.items,
            loading: result.loading,
        }
    );
}