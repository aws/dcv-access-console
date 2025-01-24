import {PropertyFilterProps} from "@cloudscape-design/components/property-filter";
import {useEffect, useState} from "react";
import {
    DescribeServersUIRequestData,
    DescribeSessionsUIRequestData,
    DescribeSessionTemplatesRequestData,
    DescribeUserGroupsRequestData,
    DescribeUsersRequestData,
    FilterDateToken,
    FilterDateTokenOperatorEnum,
    FilterStateToken,
    FilterStateTokenOperatorEnum,
    FilterToken,
    FilterTokenOperatorEnum,
    Server,
    Session,
    SessionTemplate,
    SortToken,
    SortTokenOperatorEnum,
    User,
    UserGroup
} from "@/generated-src/client";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {TableProps} from "@cloudscape-design/components";
import {SPECIAL_OPERATORS} from "@/components/common/utils/SearchUtils";
import {PropertyFilterQuery, PropertyFilterToken} from "@cloudscape-design/collection-hooks";

export type FILTER_TOKEN = FilterToken | FilterStateToken | FilterDateToken
export type FILTER_TOKEN_OPERATOR = FilterTokenOperatorEnum | FilterDateTokenOperatorEnum | FilterStateTokenOperatorEnum
export const DEFAULT_FILTERING_QUERY = { tokens: [], operation: 'and' } as PropertyFilterQuery

export type DataAccessServiceParams<T> = {
    pagination?: {
        currentPageIndex: number,
        pageSize: number,
        nextToken: string | null
    },
    sorting?: {
        sortingColumn: TableProps.SortingColumn<T>,
        sortingDescending: boolean
    },
    filtering?: {
        filteringTokens: PropertyFilterToken[] | readonly PropertyFilterToken[],
        filteringOperation: PropertyFilterProps.JoinOperation
    }
    refreshKey?: string,
    extraRequestParams?: { [key: string]: string }
}

export type DataAccessServiceResult<T> = {
    items: ReadonlyArray<T>,
    loading: boolean,
    totalCount: number,
    pagesCount: number,
    currentPageIndex: number,
    nextToken: string,
    error: boolean,
    errorMessage: string
}

export function useSessionsService(params: DataAccessServiceParams<Session>): DataAccessServiceResult<Session> {
    const {pageSize, currentPageIndex: clientPageIndex} = params.pagination || {};
    const {sortingDescending, sortingColumn} = params.sorting || {};
    const {filteringText, filteringTokens, filteringOperation} = params.filtering || {};
    const [loading, setLoading] = useState(true);
    const [items, setItems] = useState([] as Server[]);
    const [totalCount, setTotalCount] = useState(0);
    const [currentPageIndex, setCurrentPageIndex] = useState(clientPageIndex as number);
    const [pagesCount, setPagesCount] = useState(0);
    const [nextToken, setNextToken] = useState("");
    const [error, setError] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const refreshKey = params.refreshKey;

    const dataService = new DataAccessService();

    useEffect(() => {
        setCurrentPageIndex(clientPageIndex);
    }, [clientPageIndex]);

    useEffect(() => {
        setLoading(true);
        const sortToken: SortToken = {
            Key: params.sorting?.sortingColumn.sortingField,
            Operator: params.sorting?.sortingDescending ? SortTokenOperatorEnum.Desc : SortTokenOperatorEnum.Asc,
        }

        const describeSessionsUIRequest: DescribeSessionsUIRequestData = {
            SortToken: sortToken,
            MaxResults: params.pagination?.pageSize,
            NextToken: params.pagination?.nextToken
        } as DescribeSessionsUIRequestData

        params.filtering?.filteringTokens.forEach(token => {
            let key = token.propertyKey as keyof DescribeServersUIRequestData
            let operator: string = SPECIAL_OPERATORS.get(token.operator) || token.operator;

            let tokenArray: Array<FILTER_TOKEN> = describeSessionsUIRequest[key] as Array<FILTER_TOKEN> || []
            tokenArray.push({
                Operator: operator as FILTER_TOKEN_OPERATOR,
                Value: token.value
            } as FILTER_TOKEN)
            describeSessionsUIRequest[key] = tokenArray
        })

        if (params.extraRequestParams) {
            for (let [key, value] of Object.entries(params.extraRequestParams)) {
                describeSessionsUIRequest[key] = value
            }
        }
        console.log("describeSessionsUIRequest: ", describeSessionsUIRequest)
        dataService.describeSessions(describeSessionsUIRequest)
            .then(r => {
                r.data.Sessions
                setLoading(false);
                setItems(r.data.Sessions || []);
                setPagesCount(1);
                setTotalCount(r.data.Sessions?.length || 0);
                setNextToken(r.data.NextToken || null);
            }).catch(e => {
            console.error("Failed to retrieve sessions: ", e)
            setLoading(false);
            setError(true);
            setErrorMessage(e.message);
            setPagesCount(1);
            setCurrentPageIndex(1);
        });
    }, [
        sortingDescending,
        sortingColumn,
        currentPageIndex,
        refreshKey,
    ]);

    return {
        items,
        loading,
        totalCount,
        pagesCount,
        currentPageIndex,
        nextToken,
        error,
        errorMessage
    };
}

export function useServersService(params: DataAccessServiceParams<Server>): DataAccessServiceResult<Server> {
    const {pageSize, currentPageIndex: clientPageIndex} = params.pagination || {};
    const {sortingDescending, sortingColumn} = params.sorting || {};
    const {filteringText, filteringTokens, filteringOperation} = params.filtering || {};
    const [loading, setLoading] = useState(true);
    const [items, setItems] = useState([] as Server[]);
    const [totalCount, setTotalCount] = useState(0);
    const [currentPageIndex, setCurrentPageIndex] = useState(clientPageIndex as number);
    const [pagesCount, setPagesCount] = useState(0);
    const [nextToken, setNextToken] = useState("");
    const [error, setError] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const refreshKey = params.refreshKey;

    const dataService = new DataAccessService();

    useEffect(() => {
        setCurrentPageIndex(clientPageIndex);
    }, [clientPageIndex]);

    useEffect(() => {
        setLoading(true);
        const sortToken: SortToken = {
            Key: params.sorting?.sortingColumn.sortingField,
            Operator: params.sorting?.sortingDescending ? SortTokenOperatorEnum.Desc : SortTokenOperatorEnum.Asc,
        }

        const describeServersRequest: DescribeServersUIRequestData = {
            SortToken: sortToken,
            MaxResults: params.pagination?.pageSize,
            NextToken: params.pagination?.nextToken
        } as DescribeServersUIRequestData

        params.filtering?.filteringTokens.forEach(token => {
            let key = token.propertyKey as keyof DescribeServersUIRequestData
            let operator: string = SPECIAL_OPERATORS.get(token.operator) || token.operator;

            let tokenArray: Array<FILTER_TOKEN> = describeServersRequest[key] as Array<FILTER_TOKEN> || []
            tokenArray.push({
                Operator: operator as FILTER_TOKEN_OPERATOR,
                Value: token.value
            } as FILTER_TOKEN)
            describeServersRequest[key] = tokenArray
        })

        console.log("describeServersRequest: ", describeServersRequest)
        dataService.describeServers(describeServersRequest)
            .then(r => {
                r.data.Servers
                setLoading(false);
                setItems(r.data.Servers || []);
                setPagesCount(1);
                setTotalCount(r.data.Servers?.length || 0);
                setNextToken(r.data.NextToken || null);
            }).catch(e => {
            console.error("Failed to retrieve servers: ", e)
            setLoading(false);
            setError(true);
            setErrorMessage(e.message);
            setPagesCount(1);
            setCurrentPageIndex(1);
        });
    }, [
        sortingDescending,
        sortingColumn,
        currentPageIndex,
        refreshKey,
    ]);

    return {
        items,
        loading,
        totalCount,
        pagesCount,
        currentPageIndex,
        nextToken,
        error,
        errorMessage
    };
}

export function useSessionTemplatesService(params: DataAccessServiceParams<SessionTemplate>): DataAccessServiceResult<SessionTemplate> {
    const {pageSize, currentPageIndex: clientPageIndex} = params.pagination || {};
    const {sortingDescending, sortingColumn} = params.sorting || {};
    const {filteringText, filteringTokens, filteringOperation} = params.filtering || {};
    const [loading, setLoading] = useState(true);
    const [items, setItems] = useState([] as SessionTemplate[]);
    const [totalCount, setTotalCount] = useState(0);
    const [currentPageIndex, setCurrentPageIndex] = useState(clientPageIndex as number);
    const [pagesCount, setPagesCount] = useState(0);
    const [nextToken, setNextToken] = useState("");
    const [error, setError] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const dataService = new DataAccessService();
    const refreshKey = params.refreshKey;

    useEffect(() => {
        setCurrentPageIndex(clientPageIndex);
    }, [clientPageIndex]);

    useEffect(() => {
        setLoading(true);
        const sortToken: SortToken = {
            Key: params.sorting?.sortingColumn.sortingField,
            Operator: params.sorting?.sortingDescending ? SortTokenOperatorEnum.Desc : SortTokenOperatorEnum.Asc,
        }

        console.log("Describing session templates: ", params)

        const describeSessionTemplatesRequest: DescribeSessionTemplatesRequestData = {
            SortToken: sortToken,
            MaxResults: params.pagination?.pageSize,
            NextToken: params.pagination?.nextToken
        } as DescribeSessionTemplatesRequestData

        params.filtering?.filteringTokens.forEach(token => {
            let key = token.propertyKey as keyof DescribeSessionTemplatesRequestData
            let operator: string = SPECIAL_OPERATORS.get(token.operator) || token.operator;

            let tokenArray: Array<FILTER_TOKEN> = describeSessionTemplatesRequest[key] as Array<FILTER_TOKEN> || []
            tokenArray.push({
                Operator: operator as FILTER_TOKEN_OPERATOR,
                Value: token.value
            } as FILTER_TOKEN)
            describeSessionTemplatesRequest[key] = tokenArray
        })

        console.log("describeSessionTemplatesRequest: ", describeSessionTemplatesRequest)
        dataService.describeSessionTemplates(describeSessionTemplatesRequest)
            .then(r => {
                r.data.SessionTemplates
                setLoading(false);
                setItems(r.data.SessionTemplates || []);
                setPagesCount(1);
                setTotalCount(r.data.SessionTemplates?.length || 0);
                setNextToken(r.data.NextToken || null);
            }).catch(e => {
            console.error("Failed to retrieve sessionTemplates: ", e)
            setLoading(false);
            setError(true);
            setErrorMessage(e.message);
            setPagesCount(1);
        });
    }, [
        currentPageIndex,
        refreshKey,
    ]);

    return {
        items,
        loading,
        totalCount,
        pagesCount,
        currentPageIndex,
        nextToken,
        error,
        errorMessage
    };

}

export function useUsersService(params: DataAccessServiceParams<User>): DataAccessServiceResult<User> {
    const {pageSize, currentPageIndex: clientPageIndex} = params.pagination || {};
    const {sortingDescending, sortingColumn} = params.sorting || {};
    const {filteringText, filteringTokens, filteringOperation} = params.filtering || {};
    const [loading, setLoading] = useState(true);
    const [items, setItems] = useState([] as User[]);
    const [totalCount, setTotalCount] = useState(0);
    const [currentPageIndex, setCurrentPageIndex] = useState(clientPageIndex as number);
    const [pagesCount, setPagesCount] = useState(0);
    const [nextToken, setNextToken] = useState("");
    const [error, setError] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const dataService = new DataAccessService();
    const refreshKey = params.refreshKey;

    useEffect(() => {
        setCurrentPageIndex(clientPageIndex);
    }, [clientPageIndex]);

    useEffect(() => {
        setLoading(true);
        const sortToken: SortToken = {
            Key: params.sorting?.sortingColumn.sortingField,
            Operator: params.sorting?.sortingDescending ? SortTokenOperatorEnum.Desc : SortTokenOperatorEnum.Asc,
        }

        const describeUsersRequest: DescribeUsersRequestData = {
            SortToken: sortToken,
            MaxResults: params.pagination?.pageSize,
            NextToken: params.pagination?.nextToken
        } as DescribeUsersRequestData

        params.filtering?.filteringTokens.forEach(token => {
            let key = token.propertyKey as keyof DescribeUsersRequestData
            let operator: string = SPECIAL_OPERATORS.get(token.operator) || token.operator;

            let tokenArray: Array<FILTER_TOKEN> = describeUsersRequest[key] as Array<FILTER_TOKEN> || []
            tokenArray.push({
                Operator: operator as FILTER_TOKEN_OPERATOR,
                Value: token.value
            } as FILTER_TOKEN)
            describeUsersRequest[key] = tokenArray
        })

        console.log("describeUsersRequest: ", describeUsersRequest)
        dataService.describeUsers(describeUsersRequest)
            .then(r => {
                r.data.Users
                setLoading(false);
                setItems(r.data.Users || []);
                setPagesCount(1);
                setTotalCount(r.data.Users?.length || 0);
                setNextToken(r.data.NextToken || null);
            }).catch(e => {
            console.error("Failed to retrieve users: ", e)
            setLoading(false);
            setError(true);
            setErrorMessage(e.message);
            setPagesCount(1);
        });
    }, [
        currentPageIndex,
        refreshKey,
    ]);

    return {
        items,
        loading,
        totalCount,
        pagesCount,
        currentPageIndex,
        nextToken,
        error,
        errorMessage
    };
}

export function useUserGroupsService(params: DataAccessServiceParams<UserGroup>): DataAccessServiceResult<UserGroup> {
    const {pageSize, currentPageIndex: clientPageIndex} = params.pagination || {};
    const {sortingDescending, sortingColumn} = params.sorting || {};
    const {filteringText, filteringTokens, filteringOperation} = params.filtering || {};
    const [loading, setLoading] = useState(true);
    const [items, setItems] = useState([] as UserGroup[]);
    const [totalCount, setTotalCount] = useState(0);
    const [currentPageIndex, setCurrentPageIndex] = useState(clientPageIndex as number);
    const [pagesCount, setPagesCount] = useState(0);
    const [nextToken, setNextToken] = useState("");
    const [error, setError] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const dataService = new DataAccessService();
    const refreshKey = params.refreshKey;

    useEffect(() => {
        setCurrentPageIndex(clientPageIndex);
    }, [clientPageIndex]);

    useEffect(() => {
        setLoading(true);
        const sortToken: SortToken = {
            Key: params.sorting?.sortingColumn.sortingField,
            Operator: params.sorting?.sortingDescending ? SortTokenOperatorEnum.Desc : SortTokenOperatorEnum.Asc,
        }

        const describeUserGroupsRequest: DescribeUserGroupsRequestData = {
            SortToken: sortToken,
            MaxResults: params.pagination?.pageSize,
            NextToken: params.pagination?.nextToken
        } as DescribeUserGroupsRequestData

        params.filtering?.filteringTokens.forEach(token => {
            let key = token.propertyKey as keyof DescribeUserGroupsRequestData
            let operator: string = SPECIAL_OPERATORS.get(token.operator) || token.operator;

            let tokenArray: Array<FILTER_TOKEN> = describeUserGroupsRequest[key] as Array<FILTER_TOKEN> || []
            tokenArray.push({
                Operator: operator as FILTER_TOKEN_OPERATOR,
                Value: token.value
            } as FILTER_TOKEN)
            describeUserGroupsRequest[key] = tokenArray
        })

        console.log("describeUserGroupsRequest: ", describeUserGroupsRequest)
        dataService.describeUserGroups(describeUserGroupsRequest)
            .then(r => {
                r.data.UserGroups
                setLoading(false);
                setItems(r.data.UserGroups || []);
                setPagesCount(1);
                setTotalCount(r.data.UserGroups?.length || 0);
                setNextToken(r.data.NextToken || null);
            }).catch(e => {
            console.error("Failed to retrieve userGroups: ", e)
            setLoading(false);
            setError(true);
            setErrorMessage(e.message);
            setPagesCount(1);
        });
    }, [
        currentPageIndex,
        refreshKey,
    ]);

    return {
        items,
        loading,
        totalCount,
        pagesCount,
        currentPageIndex,
        nextToken,
        error,
        errorMessage
    };
}