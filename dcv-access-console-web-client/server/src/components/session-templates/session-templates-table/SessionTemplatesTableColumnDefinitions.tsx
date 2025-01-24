import {TableProps} from "@cloudscape-design/components";
import {SessionTemplate} from "@/generated-src/client";
import {SESSION_TEMPLATES_TABLE_CONSTANTS} from "@/constants/session-templates-table-constants";
import OsLabel from "@/components/session-templates/os-label/OsLabel";
import {capitalizeFirstLetter, formatDate, formatFileSize} from "@/components/common/utils/TextUtils";

export const SESSION_TEMPLATES_TABLE_COLUMN_DEFINITIONS: TableProps.ColumnDefinition<SessionTemplate>[] =
    [
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.NAME_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.NAME_HEADER,
            cell: sessionTemplate => sessionTemplate.Name,
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.NAME_ID,
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.OS_FAMILY_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.OS_FAMILY_HEADER,
            cell: sessionTemplate => <OsLabel osFamily={sessionTemplate.OsFamily}/>,
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.OS_FAMILY_ID,
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.OS_VERSIONS_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.OS_VERSIONS_HEADER,
            cell: sessionTemplate => sessionTemplate.OsVersions,
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.OS_VERSIONS_ID
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.INSTANCE_TYPES_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.INSTANCE_TYPES_HEADER,
            cell: sessionTemplate => sessionTemplate.InstanceTypes,
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.INSTANCE_TYPES_ID
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.DESCRIPTION_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.DESCRIPTION_HEADER,
            cell: sessionTemplate => sessionTemplate.Description,
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.DESCRIPTION_ID
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.INSTANCE_IDS_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.INSTANCE_IDS_HEADER,
            cell: sessionTemplate => sessionTemplate.InstanceIds,
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.INSTANCE_IDS_ID
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.INSTANCE_REGIONS_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.INSTANCE_REGIONS_HEADER,
            cell: sessionTemplate => sessionTemplate.InstanceRegions,
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.INSTANCE_REGIONS_ID
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.HOST_NUM_OF_CPUS_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.HOST_NUM_OF_CPUS_HEADER,
            cell: sessionTemplate => sessionTemplate.HostNumberOfCpus,
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.HOST_NUM_OF_CPUS_ID
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.HOST_MEMORY_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.HOST_MEMORY_HEADER,
            cell: sessionTemplate => sessionTemplate.HostMemoryTotalBytes?.split(";").map(number => formatFileSize(number)).join(";"),
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.HOST_MEMORY_ID
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.TYPE_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.TYPE_HEADER,
            cell: sessionTemplate => capitalizeFirstLetter(sessionTemplate.Type),
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.TYPE_ID,
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.CREATED_BY_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.CREATED_BY_HEADER,
            cell: sessionTemplate => sessionTemplate.CreatedBy,
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.CREATED_BY_ID,
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.CREATION_TIME_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.CREATION_TIME_HEADER,
            cell: sessionTemplate => formatDate(sessionTemplate.CreationTime),
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.CREATION_TIME_ID,
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.LAST_MODIFIED_BY_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.LAST_MODIFIED_BY_HEADER,
            cell: sessionTemplate => sessionTemplate.LastModifiedBy,
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.LAST_MODIFIED_BY_ID,
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.LAST_MODIFIED_TIME_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.LAST_MODIFIED_TIME_HEADER,
            cell: sessionTemplate => formatDate(sessionTemplate.LastModifiedTime),
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.LAST_MODIFIED_TIME_ID,
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.DCV_GL_ENABLED_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.DCV_GL_ENABLED_HEADER,
            cell: sessionTemplate => sessionTemplate.DcvGlEnabled.toString(),
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.DCV_GL_ENABLED_ID,
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.MAX_CONCURRENT_CLIENTS_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.MAX_CONCURRENT_CLIENTS_HEADER,
            cell: sessionTemplate => sessionTemplate.MaxConcurrentClients,
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.MAX_CONCURRENT_CLIENTS_ID,
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.INIT_FILE_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.INIT_FILE_HEADER,
            cell: sessionTemplate => sessionTemplate.InitFile,
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.INIT_FILE_ID,
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.AUTORUN_FILE_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.AUTORUN_FILE_HEADER,
            cell: sessionTemplate => sessionTemplate.AutorunFile,
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.AUTORUN_FILE_ID,
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.AUTORUN_FILE_ARGUMENTS_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.AUTORUN_FILE_ARGUMENTS_HEADER,
            cell: sessionTemplate => sessionTemplate.AutorunFileArguments,
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.AUTORUN_FILE_ARGUMENTS_ID,
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.REQUIREMENTS_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.REQUIREMENTS_HEADER,
            cell: sessionTemplate => sessionTemplate.Requirements,
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.REQUIREMENTS_ID,
        },
        {
            id: SESSION_TEMPLATES_TABLE_CONSTANTS.STORAGE_ROOT_ID,
            header: SESSION_TEMPLATES_TABLE_CONSTANTS.STORAGE_ROOT_HEADER,
            cell: sessionTemplate => sessionTemplate.StorageRoot,
            sortingField: SESSION_TEMPLATES_TABLE_CONSTANTS.STORAGE_ROOT_ID,
        },
    ];
