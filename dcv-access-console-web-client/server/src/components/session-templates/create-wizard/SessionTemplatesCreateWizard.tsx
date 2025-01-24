import * as React from "react";
import {useEffect, useState} from "react";
import {
    ColumnLayout,
    Container,
    ExpandableSection,
    FormField,
    Header,
    Input,
    Link,
    MultiselectProps,
    SpaceBetween,
    Toggle,
    Wizard,
} from "@cloudscape-design/components";
import Button from "@cloudscape-design/components/button";
import {SESSION_TEMPLATES_CREATE_CONSTANTS} from "@/constants/session-templates-create-constants";
import OsTiles from "@/components/session-templates/create-wizard/os-tiles/OsTiles";
import TypeTiles from "@/components/session-templates/create-wizard/type-tiles/TypeTiles";
import Textarea from "@cloudscape-design/components/textarea";
import {ValueWithLabel} from "@/components/common/key-value-pairs/ValueWithLabel";
import {useRouter} from "next/navigation";
import {
    CreateSessionTemplateRequestData,
    DescribeServersUIRequestData,
    EditSessionTemplateRequestData,
    FilterToken,
    FilterTokenOperatorEnum,
    SessionTemplate,
    ValidateSessionTemplateRequestData,
} from "@/generated-src/client";
import DataAccessService from "@/components/common/utils/DataAccessService";
import OsLabel from "@/components/session-templates/os-label/OsLabel";
import InfoLink from "@/components/common/info-link/InfoLink";
import {capitalizeFirstLetter, formatFileSize, getCleanArray} from "@/components/common/utils/TextUtils";
import {publishSessionTemplate} from "@/app/admin/sessionTemplates/[id]/assignUsersAndGroups/page";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import {CancelableEventHandler} from "@cloudscape-design/components/internal/events";
import {LinkProps} from "@cloudscape-design/components/link/interfaces";
import {GLOBAL_CONSTANTS} from "@/constants/global-constants";
import {getFieldValues} from "@/components/common/utils/SearchUtils";
import {SEARCH_TOKEN_TO_ID} from "@/constants/filter-severs-bar-constants";
import {DropdownStatusProps} from "@cloudscape-design/components/internal/components/dropdown-status";
import Multiselect from "@/components/common/multiselect/Multiselect";
import Server_search_tokens from "@/generated-src/client/server_search_tokens";
import SessionTemplatesConfigureInfo from "@/components/info-panels/SessionTemplatesConfigureInfo";
import SessionTemplatesAssignInfo from "@/components/info-panels/SessionTemplatesAssignInfo";
import SessionTemplatesReviewInfo from "@/components/info-panels/SessionTemplatesReviewInfo";
import AssignUsersGroups from "@/components/session-templates/assign-users-groups/AssignUsersGroups";
import {OptionDefinition} from "@cloudscape-design/components/internal/components/option/interfaces";

let inferred_requirements: string = ""
let existingSessionTemplate: SessionTemplate = undefined

export type SessionTemplatesCreateWizardProps = {
    existingSessionTemplateId: string | null
    isEditWizard: boolean
    setSessionTemplateName?: (string) => void
    infoLinkFollow: CancelableEventHandler<LinkProps.FollowDetail>
    setTools:  (tools: any) => void
}
export default function SessionTemplatesCreateWizard({infoLinkFollow, isEditWizard, existingSessionTemplateId, setSessionTemplateName, setTools}: SessionTemplatesCreateWizardProps) {
    const dataService = new DataAccessService()

    function getOpenGL() {
        if(type == "VIRTUAL") {
            return <FormField label={<span>
                                    {SESSION_TEMPLATES_CREATE_CONSTANTS.OPENGL} | <InfoLink onFollow={infoLinkFollow}/>
                                    </span>}
                              description={SESSION_TEMPLATES_CREATE_CONSTANTS.OPENGL_DESCRIPTION}>
                <Toggle
                    onChange={({ detail }) =>
                        setOpenGL(detail.checked)
                    }
                    checked={openGL}
                >
                    {openGL? "Enabled" : "Disabled"}
                </Toggle>
            </FormField>
        }
    }

    function getOpenGLReviewItem() {
        if(type == "VIRTUAL") {
            return <ValueWithLabel label={SESSION_TEMPLATES_CREATE_CONSTANTS.OPENGL}>
                {openGL || "Not specified"}
            </ValueWithLabel>
        }
    }

    function getAutorun() {
        if((type == "VIRTUAL" && os == "linux") || (type == "CONSOLE" && os == "windows")) {
            return <Header variant="h3">
                {SESSION_TEMPLATES_CREATE_CONSTANTS.AUTORUN}
            </Header>
        }
    }

    function getAutorunFile() {
        if((type == "VIRTUAL" && os == "linux") || (type == "CONSOLE" && os == "windows")) {
            return <FormField label={<span>
                                        {SESSION_TEMPLATES_CREATE_CONSTANTS.AUTORUN_FILE} <i>- optional</i> | <InfoLink onFollow={infoLinkFollow}/>
                                        </span>}
                              description={SESSION_TEMPLATES_CREATE_CONSTANTS.AUTORUN_FILE_DESCRIPTION}>
                <Input
                    onChange={({ detail }) => {
                        if(!detail.value || detail.value.trim()) {
                            setAutorunFile(detail.value)
                        }
                    }}
                    value={autorunFile}
                    placeholder={SESSION_TEMPLATES_CREATE_CONSTANTS.AUTORUN_FILE_PLACEHOLDER}
                    spellcheck
                />
            </FormField>
        }
    }

    function getAutorunFileReviewItem() {
        if((type == "VIRTUAL" && os == "linux") || (type == "CONSOLE" && os == "windows")) {
            return <ValueWithLabel label={SESSION_TEMPLATES_CREATE_CONSTANTS.AUTORUN_FILE}>
                {autorunFile || "Not specified"}
            </ValueWithLabel>
        }
    }

    function getAutorunArguments() {
        if((type == "VIRTUAL" && os == "linux") || (type == "CONSOLE" && os == "windows")) {
            return <FormField label={<span>
                                        {SESSION_TEMPLATES_CREATE_CONSTANTS.AUTORUN_ARGUMENTS} <i>- optional</i> | <InfoLink onFollow={infoLinkFollow}/>
                                        </span>}
                              description={SESSION_TEMPLATES_CREATE_CONSTANTS.AUTORUN_ARGUMENTS_DESCRIPTION}>
                <Textarea
                    onChange={({ detail }) => {
                        if(!detail.value || detail.value.trim()) {
                            setAutorunArguments(detail.value)
                        }
                    }}
                    value={autorunArguments}
                    placeholder={SESSION_TEMPLATES_CREATE_CONSTANTS.AUTORUN_ARGUMENTS_PLACEHOLDER}
                    spellcheck
                    disabled={!autorunFile || !autorunFile.trim()}
                />
            </FormField>
        }
    }

    function getAutorunArgumentsReviewItem() {
        if((type == "VIRTUAL" && os == "linux") || (type == "CONSOLE" && os == "windows")) {
            return <ValueWithLabel label={SESSION_TEMPLATES_CREATE_CONSTANTS.AUTORUN_ARGUMENTS}>
                {autorunArguments || "Not specified"}
            </ValueWithLabel>
        }
    }

    function getInitFile() {
        if(type == "VIRTUAL") {
            return <FormField label={<span>
                                        {SESSION_TEMPLATES_CREATE_CONSTANTS.INIT_FILE} <i>- optional</i> | <InfoLink onFollow={infoLinkFollow}/>
                                        </span>}
                              description={SESSION_TEMPLATES_CREATE_CONSTANTS.INIT_FILE_DESCRIPTION}>
                <Input
                    onChange={({ detail }) => {
                        if(!detail.value || detail.value.trim()) {
                            setInitFile(detail.value)
                        }
                    }}
                    value={initFile}
                    placeholder={SESSION_TEMPLATES_CREATE_CONSTANTS.INIT_FILE_PLACEHOLDER}
                    spellcheck
                />
            </FormField>
        }
    }

    function getInitFileReviewItem() {
        if(type == "VIRTUAL") {
            return <ValueWithLabel label={SESSION_TEMPLATES_CREATE_CONSTANTS.INIT_FILE}>
                {initFile || "Not specified"}
            </ValueWithLabel>
        }
    }

    function getAwsInstanceFields() {
        if (isAwsServerAvailable) {
            return <div>
                <SpaceBetween direction="vertical" size="l">
                <Header variant="h3"
                    description={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_DESCRIPTION}>
                    {SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE}
                </Header>
                <FormField label={<span>
                                                {SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_ID} <i>- optional</i> | <InfoLink onFollow={infoLinkFollow}/>
                                                </span>}
                           description={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_ID_DESCRIPTION}>
                    <Multiselect selectedItems={instanceIds}
                                       setSelectedItems={setInstanceIds}
                                       charactersToSearchAfter={-1}
                                       filteringProperty={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_ID_FILTERING_PROPERTY}
                                       getOptions={getServerPropertyOptions}
                                       loadingText={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_ID_LOADING_TEXT}
                                       errorText={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_ID_EMPTY_TEXT}
                                       continueText={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_ID_CONTINUE_TEXT}
                                       emptyText={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_ID_EMPTY_TEXT}
                                       placeholderText={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_ID_PLACEHOLDER}/>
                </FormField>
                <FormField label={<span>
                                                {SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_TYPE} <i>- optional</i> | <InfoLink onFollow={infoLinkFollow}/>
                                                </span>}
                           description={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_TYPE_DESCRIPTION}>
                    <Multiselect selectedItems={instanceTypes}
                                       setSelectedItems={setInstanceTypes}
                                       charactersToSearchAfter={-1}
                                       filteringProperty={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_TYPE_FILTERING_PROPERTY}
                                       getOptions={getServerPropertyOptions}
                                       loadingText={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_TYPE_LOADING_TEXT}
                                       errorText={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_TYPE_EMPTY_TEXT}
                                       continueText={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_TYPE_CONTINUE_TEXT}
                                       emptyText={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_TYPE_EMPTY_TEXT}
                                       placeholderText={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_TYPE_PLACEHOLDER}/>
                </FormField>
                <FormField label={<span>
                                                {SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_REGION} <i>- optional</i> | <InfoLink onFollow={infoLinkFollow}/>
                                                </span>}
                           description={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_REGION_DESCRIPTION}>
                    <Multiselect selectedItems={instanceRegions}
                                       setSelectedItems={setInstanceRegions}
                                       charactersToSearchAfter={-1}
                                       filteringProperty={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_REGION_FILTERING_PROPERTY}
                                       getOptions={getServerPropertyOptions}
                                       loadingText={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_REGION_LOADING_TEXT}
                                       errorText={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_REGION_EMPTY_TEXT}
                                       continueText={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_REGION_CONTINUE_TEXT}
                                       emptyText={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_REGION_EMPTY_TEXT}
                                       placeholderText={SESSION_TEMPLATES_CREATE_CONSTANTS.INSTANCE_REGION_PLACEHOLDER}/>
                </FormField>
                </SpaceBetween>
            </div>
        }
    }

    async function createSessionTemplate() : Promise<string> {
        const createSessionTemplateRequestData: CreateSessionTemplateRequestData = {
            Name: name && name.trim() ? name.trim() : undefined,
            Description: description && description.trim() ? description.trim() : undefined,
            OsFamily: os,
            OsVersions:  osVersions?.join("; "),
            InstanceIds: instanceIds?.join("; "),
            InstanceTypes: instanceTypes?.join("; "),
            InstanceRegions: instanceRegions?.join("; "),
            HostNumberOfCpus: hostVCpus?.join("; "),
            HostMemoryTotalBytes: hostMemoryBytes?.join("; "),
            Type: type,
            DcvGlEnabled: openGL,
            MaxConcurrentClients: maxConcurrentClients,
            InitFile: initFile && initFile.trim() ? initFile.trim() : undefined,
            AutorunFile: autorunFile && autorunFile.trim() ? autorunFile.trim() : undefined,
            AutorunFileArguments: autorunArguments && autorunArguments.trim() ? autorunArguments.trim().split('\n') : undefined,
            Requirements: requirements && requirements.trim() ? requirements.trim() : undefined,
            StorageRoot: storageRoot && storageRoot.trim() ? storageRoot.trim() : undefined
        } as CreateSessionTemplateRequestData

        console.log("createSessionTemplateRequest: ", createSessionTemplateRequestData)
        return dataService.createSessionTemplate(createSessionTemplateRequestData)
            .then(r => {
                console.log("SessionTemplate successfully created", r.data.SessionTemplate)
                addFlashBar("success", r.data.SessionTemplate.Id, 'Successfully created session template "' +createSessionTemplateRequestData.Name+ '".')
                return r.data.SessionTemplate.Id
            }).catch(e => {
                console.error("Failed to create sessionTemplate: ", e)
                addFlashBar("error", createSessionTemplateRequestData.Name, 'An error occurred while creating session template "' +createSessionTemplateRequestData.Name+ '".')
                return undefined
            }).finally(() => setLoading(false))
    }

    async function editSessionTemplate() : Promise<string> {
        const createSessionTemplateRequestData: CreateSessionTemplateRequestData = {
            Name: name && name.trim() ? name.trim() : undefined,
            Description: description && description.trim() ? description.trim() : undefined,
            OsFamily: os,
            OsVersions:  osVersions?.join("; "),
            InstanceIds: instanceIds?.join("; "),
            InstanceTypes: instanceTypes?.join("; "),
            InstanceRegions: instanceRegions?.join("; "),
            HostNumberOfCpus: hostVCpus?.join("; "),
            HostMemoryTotalBytes: hostMemoryBytes?.join("; "),
            Type: type,
            DcvGlEnabled: openGL,
            MaxConcurrentClients: maxConcurrentClients,
            InitFile: initFile && initFile.trim() ? initFile.trim() : undefined,
            AutorunFile: autorunFile && autorunFile.trim() ? autorunFile.trim() : undefined,
            AutorunFileArguments: autorunArguments && autorunArguments.trim() ? autorunArguments.trim().split('\n') : undefined,
            Requirements: requirements && requirements.trim() ? requirements.trim() : undefined,
            StorageRoot: storageRoot && storageRoot.trim() ? storageRoot.trim() : undefined
        } as CreateSessionTemplateRequestData

        const editSessionTemplateRequestData: EditSessionTemplateRequestData = {
            TemplateId: existingSessionTemplateId,
            CreateSessionTemplateRequestData: createSessionTemplateRequestData
        } as EditSessionTemplateRequestData

        console.log("editSessionTemplateRequest: ", editSessionTemplateRequestData)
        return dataService.editSessionTemplate(editSessionTemplateRequestData)
            .then(r => {
                console.log("SessionTemplate successfully updated", r.data.SessionTemplate)
                addFlashBar("success", existingSessionTemplateId!, 'Successfully updated session template "' +createSessionTemplateRequestData.Name+ '".')
                return r.data.SessionTemplate.Id
            }).catch(e => {
                console.error("Failed to update sessionTemplate: ", e)
                addFlashBar("error", existingSessionTemplateId!, 'An error occurred while updating session template "' +createSessionTemplateRequestData.Name+ '".')
                return undefined
            }).finally(() => setLoading(false))
    }

    async function validateSessionTemplate(): Promise<boolean> {
        const createSessionTemplateRequestData: CreateSessionTemplateRequestData = {
            Name: name && name.trim() ? name.trim() : undefined,
            Description: description && description.trim() ? description.trim() : undefined,
            OsFamily: os,
            OsVersions:  osVersions?.join("; "),
            InstanceIds: instanceIds?.join("; "),
            InstanceTypes: instanceTypes?.join("; "),
            InstanceRegions: instanceRegions?.join("; "),
            HostNumberOfCpus: hostVCpus?.join("; "),
            HostMemoryTotalBytes: hostMemoryBytes?.join("; "),
            Type: type,
            DcvGlEnabled: openGL,
            MaxConcurrentClients: maxConcurrentClients,
            InitFile: initFile && initFile.trim() ? initFile.trim() : undefined,
            AutorunFile: autorunFile && autorunFile.trim() ? autorunFile.trim() : undefined,
            AutorunFileArguments: autorunArguments && autorunArguments.trim()? autorunArguments.trim().split('\n') : undefined,
            Requirements: requirements && requirements.trim() ? requirements.trim() : undefined,
            StorageRoot: storageRoot && storageRoot.trim() ? storageRoot.trim() : undefined
        } as CreateSessionTemplateRequestData

        const validateSessionTemplateRequestData: ValidateSessionTemplateRequestData = {
            CreateSessionTemplateRequestData: createSessionTemplateRequestData,
            IgnoreExisting: isEditWizard && existingSessionTemplate.Name === name
        }

        console.log("validateSessionTemplateRequest: ", validateSessionTemplateRequestData)
        return await dataService.validateSessionTemplate(validateSessionTemplateRequestData)
            .then(r => {
                console.log("Failure reasons: ", r.data.FailureReasons)
                if(Object.keys(r.data.FailureReasons).length > 0) {
                    setErrorName(r.data.FailureReasons.Name)
                    setErrorRequirements(r.data.FailureReasons.Requirements)
                    return false
                }
                return true
            }).catch(e => {
                console.error("Failed to validate sessionTemplate: ", e)
                return false
            })
    }

    async function getServerPropertyOptions(filteringText: string,
                                            filteringProperty: string,
                                            setStatus: (status: DropdownStatusProps.StatusType) => void,
                                            setOptions: (options: MultiselectProps.Option[]) => void) {
        setStatus("loading")
        let request = {
            OsFamilies: [{
                Operator: FilterTokenOperatorEnum.Equal,
                Value: os
            }],
            SortToken: {
                Operator: "ASC",
                Key: "Id"
            }
        } as DescribeServersUIRequestData
        if ((Server_search_tokens[filteringProperty].Operators as string[]).includes(":")) {
            request[filteringProperty] = [{
                Operator: FilterTokenOperatorEnum.Contains,
                Value: filteringText
            } as FilterToken]
        }

        let uniqueOptions: Set<string> = new Set<string>()
        await dataService.describeServers(request)
            .then(r => {
                r.data.Servers?.forEach(server => {
                    const fieldValues = getFieldValues(server, SEARCH_TOKEN_TO_ID.get(filteringProperty))
                    fieldValues.forEach(value => uniqueOptions.add(value.toString()))
                })
                setStatus("finished")
            }).catch(e => {
                console.error("Failed to retrieve servers: ", e)
                setStatus("error")
            })
        let options: MultiselectProps.Option[] = []
        uniqueOptions.forEach(option => {
            options.push({value: option, label: filteringProperty === SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_MEMORY_FILTERING_PROPERTY ? formatFileSize(Number(option)) : option})
        })

        //Pushing filteringText to Options if there is no match
        if (!options.length && filteringText.trim() && filteringProperty != SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_MEMORY_FILTERING_PROPERTY) {
            options.push({value: filteringText})
            setStatus("error")
        }
        setOptions(options)
    }

    const [activeStepIndex, setActiveStepIndex] = useState(0)
    const [name, setName] = useState<string>()
    const [description, setDescription] = useState<string>()
    const [os, setOs] = useState<string>("linux")
    const [osVersions, setOsVersions] = useState<(string | undefined) []>()
    const [instanceIds, setInstanceIds] = useState<(string | undefined) []>()
    const [instanceTypes, setInstanceTypes] = useState<(string | undefined) []>()
    const [instanceRegions, setInstanceRegions] = useState<(string | undefined) []>()
    const [hostVCpus, setHostVCpus] = useState<(string | undefined) []>()
    const [hostMemoryBytes, setHostMemoryBytes] = useState<(string | undefined) []>()
    const [type, setType] = useState<string>("VIRTUAL")
    const [openGL, setOpenGL] = useState<boolean>(true)
    const [requirements, setRequirements] = useState<string>()
    const [autorunFile, setAutorunFile] = useState<string>()
    const [autorunArguments, setAutorunArguments] = useState()
    const [maxConcurrentClients, setMaxConcurrentClients] = useState<number>(0)
    const [initFile, setInitFile] = useState<string>()
    const [storageRoot, setStorageRoot] = useState<string>()
    const {push} = useRouter()
    const [loading, setLoading] = useState(false)
    const [errorName, setErrorName] = useState()
    const [errorRequirements, setErrorRequirements] = useState()
    const [users, setUsers] = React.useState([null])
    const [groups, setGroups] = React.useState([null])
    const [isAwsServerAvailable, setIsAwsServerAvailable] = React.useState(false)
    const {items, addFlashBar} = useFlashBarContext()
    const {replace} = useRouter()

    useEffect(() => {
        setLoading(true)
        const recoveryUrl: string = isEditWizard ? GLOBAL_CONSTANTS.SESSION_TEMPLATES_URL : SESSION_TEMPLATES_CREATE_CONSTANTS.CREATE_TEMPLATE_URL
        const recoveryError: string = isEditWizard ? SESSION_TEMPLATES_CREATE_CONSTANTS.EDIT_ERROR : SESSION_TEMPLATES_CREATE_CONSTANTS.DUPLICATE_ERROR
        if (existingSessionTemplateId) {
            new DataAccessService().describeSessionTemplates({
                Ids: [{
                    Operator: FilterTokenOperatorEnum.Equal,
                    Value: existingSessionTemplateId
                }]
            }).then(result => {
                if (result.data.SessionTemplates?.length == 1) {
                    existingSessionTemplate = result.data.SessionTemplates[0]
                    if (setSessionTemplateName) {
                        setSessionTemplateName(existingSessionTemplate.Name)
                    }
                    setName(existingSessionTemplate.Name + (isEditWizard ? "" : " - copy"))
                    setDescription(existingSessionTemplate.Description)
                    setOs(existingSessionTemplate.OsFamily)
                    setOsVersions(existingSessionTemplate.OsVersions ? existingSessionTemplate.OsVersions.split("; ") : null)
                    setInstanceIds(existingSessionTemplate.InstanceIds ? existingSessionTemplate.InstanceIds.split("; ") : null)
                    setInstanceTypes(existingSessionTemplate.InstanceTypes ? existingSessionTemplate.InstanceTypes.split("; ") : null)
                    setInstanceRegions(existingSessionTemplate.InstanceRegions ? existingSessionTemplate.InstanceRegions.split("; ") : null)
                    setHostVCpus(existingSessionTemplate.HostNumberOfCpus ? existingSessionTemplate.HostNumberOfCpus.split("; ") : null)
                    setHostMemoryBytes(existingSessionTemplate.HostMemoryTotalBytes ? existingSessionTemplate.HostMemoryTotalBytes.split("; ") : null)
                    setType(existingSessionTemplate.Type)
                    setOpenGL(existingSessionTemplate.DcvGlEnabled)
                    setRequirements(existingSessionTemplate.Requirements)
                    setAutorunFile(existingSessionTemplate.AutorunFile)
                    setAutorunArguments(existingSessionTemplate.AutorunFileArguments)
                    setMaxConcurrentClients(existingSessionTemplate.MaxConcurrentClients)
                    setInitFile(existingSessionTemplate.InitFile)
                    setStorageRoot(existingSessionTemplate.StorageRoot)
                } else {
                    addFlashBar("warning", existingSessionTemplateId, recoveryError)
                    replace(recoveryUrl)
                }
            }).catch(result => {
                addFlashBar("error", existingSessionTemplateId, recoveryError)
                replace(recoveryUrl)
            })
        }

        dataService.describeServers({} as DescribeServersUIRequestData)
            .then(r => {
                for (let i = 0; i < r.data.Servers?.length; i++) {
                    if (r.data.Servers[i].Host?.Aws) {
                        setIsAwsServerAvailable(true)
                        break
                    }
                }
                setLoading(false)
            }).catch(e => {
            console.error("Failed to retrieve servers: ", e)
            setLoading(false)
        })
    }, [])

    useEffect(() => {
        if (!loading) {
            inferred_requirements = "(server:Host.Os.Family = \'" + os + "\'"
            if (osVersions?.length) {
                inferred_requirements += " and (" + osVersions.map(osVersion => "server:Host.Os.Version = \'" + osVersion + "\'").join(" or ") + ")"
            }
            if (instanceIds?.length) {
                inferred_requirements += " and (" + instanceIds.map(instanceId => "server:Host.Aws.Ec2InstanceId = \'" + instanceId + "\'").join(" or ") + ")"
            }
            if (instanceTypes?.length) {
                inferred_requirements += " and (" + instanceTypes.map(instanceType => "server:Host.Aws.Ec2InstanceType = \'" + instanceType + "\'").join(" or ") + ")"
            }
            if (instanceRegions?.length) {
                inferred_requirements += " and (" + instanceRegions.map(instanceRegion => "server:Host.Aws.Region = \'" + instanceRegion + "\'").join(" or ") + ")"
            }
            if (hostVCpus?.length) {
                inferred_requirements += " and (" + hostVCpus.map(number => "server:Host.CpuInfo.NumberOfCpus = " + number).join(" or ") + ")"
            }
            if (hostMemoryBytes?.length) {
                inferred_requirements += " and (" + hostMemoryBytes.map(number => "server:Host.Memory.TotalBytes = " + number).join(" or ") + ")"
            }
            inferred_requirements += ")"
            setRequirements(inferred_requirements)
        }
    }, [os, osVersions, instanceIds, instanceTypes, instanceRegions, hostVCpus, hostMemoryBytes])

    useEffect(() => {
        if (activeStepIndex == 0) {
            setTools(<SessionTemplatesConfigureInfo/>)
        } else if (activeStepIndex == 1) {
            setTools(<SessionTemplatesAssignInfo/>)
        } else if (activeStepIndex == 2) {
            setTools(<SessionTemplatesReviewInfo/>)
        }
    }, [activeStepIndex])

    useEffect(() => {
        setOpenGL(type == "VIRTUAL"? true: null)
    }, [type])

    return (
        <Wizard
            i18nStrings={{
                stepNumberLabel: stepNumber =>
                    `Step ${stepNumber}`,
                collapsedStepsLabel: (stepNumber, stepsCount) =>
                    `Step ${stepNumber} of ${stepsCount}`,
                skipToButtonLabel: (step, stepNumber) =>
                    `Skip to ${step.title}`,
                navigationAriaLabel: "Steps",
                cancelButton: "Cancel",
                previousButton: "Previous",
                nextButton: "Next",
                submitButton: isEditWizard ? SESSION_TEMPLATES_CREATE_CONSTANTS.UPDATE_TEMPLATE : SESSION_TEMPLATES_CREATE_CONSTANTS.CREATE_TEMPLATE,
                optional: "optional"
            }}
            onNavigate={async ({detail}) => {
                if (activeStepIndex == 0 && detail.reason == "next") {
                    setLoading(true)
                    if (await validateSessionTemplate()) {
                        setActiveStepIndex(detail.requestedStepIndex)
                    }
                    setLoading(false)
                } else {
                    setActiveStepIndex(detail.requestedStepIndex)
                }
            }}
            isLoadingNextStep={loading}
            onSubmit={async ({detail}) => {
                setLoading(true)
                const sessionTemplateId: string = isEditWizard? await editSessionTemplate() : await createSessionTemplate()
                if(sessionTemplateId) {
                    publishSessionTemplate(sessionTemplateId, users, groups).then(result => {
                        console.log("SessionTemplate published", result.data)
                        if (result.data.UnsuccessfulUsersList?.length > 0) {
                            addFlashBar("error", sessionTemplateId + "errorUsers", 'An error occurred while assigning users [' + getCleanArray(result.data.UnsuccessfulUsersList!) + '] to "' + name + '".')
                        }
                        if (result.data.UnsuccessfulGroupsList?.length > 0) {
                            addFlashBar("error", sessionTemplateId + "errorGroups", 'An error occurred while assigning groups [' + getCleanArray(result.data.UnsuccessfulGroupsList!) + '] to "' + name + '".')
                        }
                        if ((!result.data.UnsuccessfulUsersList || !result.data.UnsuccessfulUsersList?.length) && ((!result.data.UnsuccessfulGroupsList || !result.data.UnsuccessfulGroupsList?.length))) {
                            addFlashBar("success", sessionTemplateId + "success", 'Successfully assigned users and/or groups to "' + name + '".')
                            push(GLOBAL_CONSTANTS.SESSION_TEMPLATES_URL)
                        }
                    }).catch(e => {
                        console.error("Failed to publish sessionTemplate: ", e)
                        addFlashBar("error", sessionTemplateId, 'An error occurred while assigning users and /or groups to "' + name + '".')
                    }).finally(() => {
                            setLoading(false)
                            push(GLOBAL_CONSTANTS.SESSION_TEMPLATES_URL)
                        }
                    )
                }
            }}
            onCancel={({ detail }) => {
                push(GLOBAL_CONSTANTS.SESSION_TEMPLATES_URL)
            }}
            activeStepIndex={activeStepIndex}
            steps={[
                {
                    title: SESSION_TEMPLATES_CREATE_CONSTANTS.CONFIGURE_DETAILS,
                    info: <Link variant={"info"} onFollow={infoLinkFollow}>{GLOBAL_CONSTANTS.INFO_LABEL}</Link>,
                    description: SESSION_TEMPLATES_CREATE_CONSTANTS.CONFIGURE_DESCRIPTION,
                    content: (
                        <Container
                            header={
                                <Header variant="h2">
                                    {SESSION_TEMPLATES_CREATE_CONSTANTS.CONFIGURE_HEADER}
                                </Header>
                            }
                            footer={<ExpandableSection
                                variant="footer"
                                headerText={SESSION_TEMPLATES_CREATE_CONSTANTS.ADDITIONAL_CONFIGURATIONS}
                            >
                                <SpaceBetween direction="vertical" size="l">
                                    <FormField label={<span>
                                            {SESSION_TEMPLATES_CREATE_CONSTANTS.MAX_CONCURRENT_CLIENTS} <i>- optional</i> | <InfoLink onFollow={infoLinkFollow}/>
                                            </span>}
                                               description={SESSION_TEMPLATES_CREATE_CONSTANTS.MAX_CONCURRENT_CLIENTS_DESCRIPTION}>
                                        <Input
                                            onChange={({ detail }) => {
                                                if(detail.value >= 0) {
                                                    setMaxConcurrentClients(Number(detail.value))
                                                }
                                            }}
                                            value={maxConcurrentClients}
                                            spellcheck
                                            step={1}
                                            type="number"
                                        />
                                    </FormField>

                                    {getInitFile()}

                                    <FormField label={<span>
                                                    {SESSION_TEMPLATES_CREATE_CONSTANTS.STORAGE_ROOT} <i>- optional</i> | <InfoLink onFollow={infoLinkFollow}/>
                                                    </span>}
                                               description={SESSION_TEMPLATES_CREATE_CONSTANTS.STORAGE_ROOT_DESCRIPTION}>
                                        <Input
                                            onChange={({ detail }) => {
                                                if(!detail.value || detail.value.trim()) {
                                                    setStorageRoot(detail.value)
                                                }
                                            }}
                                            value={storageRoot}
                                            placeholder={SESSION_TEMPLATES_CREATE_CONSTANTS.STORAGE_ROOT_PLACEHOLDER}
                                            spellcheck
                                        />
                                    </FormField>

                                    <FormField label={<span>
                                                {SESSION_TEMPLATES_CREATE_CONSTANTS.REQUIREMENTS} <i>- optional</i> | <InfoLink onFollow={infoLinkFollow}/>
                                                </span>}
                                               description={<span>
                                               {SESSION_TEMPLATES_CREATE_CONSTANTS.REQUIREMENTS_DESCRIPTION} <br/>
                                                   Please see our <Link external={true} href={SESSION_TEMPLATES_CREATE_CONSTANTS.REQUIREMENTS_INFO}> CreateSessions API documentation </Link> for a complete list of requirement parameters, and example formatting.
                                                </span>}
                                               errorText={errorRequirements}>
                                        <Textarea
                                            onChange={({ detail }) => {
                                                if(detail.value.length >= inferred_requirements?.length && detail.value.trim()) {
                                                    setRequirements(detail.value)
                                                    setErrorRequirements(undefined)
                                                }
                                            }}
                                            value={requirements!}
                                            placeholder={SESSION_TEMPLATES_CREATE_CONSTANTS.REQUIREMENTS_PLACEHOLDER}
                                            spellcheck
                                        />
                                    </FormField>
                                </SpaceBetween>
                            </ExpandableSection>}
                        >
                            <SpaceBetween direction="vertical" size="l">
                                <FormField label={SESSION_TEMPLATES_CREATE_CONSTANTS.TEMPLATE_NAME}
                                           description={SESSION_TEMPLATES_CREATE_CONSTANTS.NAME_DESCRIPTION}
                                           errorText={errorName}>
                                    <Input
                                        onChange={({ detail }) => {
                                            if(!detail.value || detail.value.trim()) {
                                                setName(detail.value)
                                                setErrorName(undefined)
                                            }
                                        }}
                                        value={name}
                                        placeholder={SESSION_TEMPLATES_CREATE_CONSTANTS.NAME_PLACEHOLDER}
                                        spellcheck
                                    />
                                </FormField>
                                <FormField label={<span>
                                                    {SESSION_TEMPLATES_CREATE_CONSTANTS.TEMPLATE_DESCRIPTION} <i>- optional</i>
                                                    </span>}
                                           description={SESSION_TEMPLATES_CREATE_CONSTANTS.DESCRIPTION_DESCRIPTION}>
                                    <Textarea
                                        onChange={({ detail }) => {
                                            if(!detail.value || detail.value.trim()) {
                                                setDescription(detail.value)
                                            }
                                        }}
                                        value={description}
                                        placeholder={SESSION_TEMPLATES_CREATE_CONSTANTS.DESCRIPTION_PLACEHOLDER}
                                        spellcheck
                                    />
                                </FormField>
                                <FormField label={<span>
                                                {SESSION_TEMPLATES_CREATE_CONSTANTS.OS} | <InfoLink onFollow={infoLinkFollow}/>
                                                </span>}
                                           description={SESSION_TEMPLATES_CREATE_CONSTANTS.OS_DESCRIPTION}>
                                    <OsTiles columns={2} os={os} setOs={setOs} setType={setType}/>
                                </FormField>

                                <FormField label={<span>
                                            {SESSION_TEMPLATES_CREATE_CONSTANTS.OS_VERSION} <i>- optional</i> | <InfoLink onFollow={infoLinkFollow}/>
                                            </span>}
                                           description={SESSION_TEMPLATES_CREATE_CONSTANTS.OS_VERSION_DESCRIPTION}>
                                    <Multiselect selectedItems={osVersions}
                                                       setSelectedItems={setOsVersions}
                                                       charactersToSearchAfter={-1}
                                                       filteringProperty={"OsVersions"}
                                                       getOptions={getServerPropertyOptions}
                                                       loadingText={SESSION_TEMPLATES_CREATE_CONSTANTS.OS_VERSION_LOADING_TEXT}
                                                       errorText={SESSION_TEMPLATES_CREATE_CONSTANTS.OS_VERSION_EMPTY_TEXT}
                                                       continueText={SESSION_TEMPLATES_CREATE_CONSTANTS.OS_VERSION_CONTINUE_TEXT}
                                                       emptyText={SESSION_TEMPLATES_CREATE_CONSTANTS.OS_VERSION_EMPTY_TEXT}
                                                       placeholderText={SESSION_TEMPLATES_CREATE_CONSTANTS.OS_VERSION_PLACEHOLDER}/>
                                </FormField>

                                <FormField label={<span>
                                                {SESSION_TEMPLATES_CREATE_CONSTANTS.TYPE} | <InfoLink onFollow={infoLinkFollow}/>
                                                </span>}
                                           description={SESSION_TEMPLATES_CREATE_CONSTANTS.TYPE_DESCRIPTION}>
                                    <TypeTiles columns={2}  os={os} type={type} setType={setType}/>
                                </FormField>
                                {getOpenGL()}
                                <SpaceBetween size="m"/>

                                {getAwsInstanceFields()}
                                {isAwsServerAvailable ? <SpaceBetween size="m"/> : null}

                                <Header variant="h3"
                                        description={SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_DESCRIPTION}>
                                    {SESSION_TEMPLATES_CREATE_CONSTANTS.HOST}
                                </Header>
                                <FormField label={<span>
                                                {SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_NUM_OF_CPUS} <i>- optional</i> | <InfoLink onFollow={infoLinkFollow}/>
                                                </span>}
                                           description={SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_NUM_OF_CPUS_DESCRIPTION}>
                                    <Multiselect selectedItems={hostVCpus}
                                                 setSelectedItems={setHostVCpus}
                                                 charactersToSearchAfter={-1}
                                                 filteringProperty={SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_NUM_OF_CPUS_FILTERING_PROPERTY}
                                                 getOptions={getServerPropertyOptions}
                                                 loadingText={SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_NUM_OF_CPUS_LOADING_TEXT}
                                                 errorText={SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_NUM_OF_CPUS_EMPTY_TEXT}
                                                 continueText={SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_NUM_OF_CPUS_CONTINUE_TEXT}
                                                 emptyText={SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_NUM_OF_CPUS_EMPTY_TEXT}
                                                 placeholderText={SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_NUM_OF_CPUS_PLACEHOLDER}/>
                                </FormField>
                                <FormField label={<span>
                                                {SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_MEMORY} <i>- optional</i> | <InfoLink onFollow={infoLinkFollow}/>
                                                </span>}
                                           description={SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_MEMORY_DESCRIPTION}>
                                    <Multiselect selectedItems={hostMemoryBytes}
                                                 setSelectedItems={setHostMemoryBytes}
                                                 charactersToSearchAfter={-1}
                                                 filteringProperty={SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_MEMORY_FILTERING_PROPERTY}
                                                 getOptions={getServerPropertyOptions}
                                                 loadingText={SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_MEMORY_LOADING_TEXT}
                                                 errorText={SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_MEMORY_EMPTY_TEXT}
                                                 continueText={SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_MEMORY_CONTINUE_TEXT}
                                                 emptyText={SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_MEMORY_EMPTY_TEXT}
                                                 placeholderText={SESSION_TEMPLATES_CREATE_CONSTANTS.HOST_MEMORY_PLACEHOLDER}/>
                                </FormField>
                                <SpaceBetween size="m"/>

                                {getAutorun()}
                                {getAutorunFile()}
                                {getAutorunArguments()}

                            </SpaceBetween>
                        </Container>
                    )
                },
                {
                    title: SESSION_TEMPLATES_CREATE_CONSTANTS.ASSIGN_USERS,
                    description: SESSION_TEMPLATES_CREATE_CONSTANTS.ASSIGN_USERS_DESCRIPTION,
                    content: (<Container>
                    <AssignUsersGroups
                        sessionTemplateId={existingSessionTemplateId!}
                        handleUsersChange={(users: [OptionDefinition]) => {
                            let userIds: [string] = []
                            users.forEach(user => {
                                if (user.value != null) {
                                    userIds.push(user.value)
                                }
                            })
                            setUsers(userIds)
                        }}
                        handleGroupsChange={(groups: [OptionDefinition]) => {
                            let groupIds: [string] = []
                            groups?.forEach(group => {
                                if(group.value != null) {
                                    groupIds.push(group.value)
                                }
                            })
                            setGroups(groupIds)
                        }}
                        handleError={(error: string) => {
                            console.log("Error", error)
                            addFlashBar("error", existingSessionTemplateId!, 'Error while retrieving users and/or groups assigned to "' + existingSessionTemplateId + '".')
                        }}
                    />
                    </Container>),
                    isOptional: true
                },
                {
                    title: isEditWizard? SESSION_TEMPLATES_CREATE_CONSTANTS.REVIEW_AND_UPDATE : SESSION_TEMPLATES_CREATE_CONSTANTS.REVIEW_AND_CREATE,
                    content: (<SpaceBetween size="xxl">
                                <SpaceBetween size="xs">
                                    <Header
                                        variant="h3"
                                        actions={
                                            <Button
                                                onClick={() => setActiveStepIndex(0)}
                                            >
                                                Edit
                                            </Button>
                                        }
                                    >
                                        Step 1: {SESSION_TEMPLATES_CREATE_CONSTANTS.CONFIGURE_DETAILS}
                                    </Header>
                                    <Container
                                        header={
                                            <Header variant="h2">
                                                {SESSION_TEMPLATES_CREATE_CONSTANTS.CONFIGURE_HEADER}
                                            </Header>
                                        }
                                        footer={<ExpandableSection
                                            variant="footer"
                                            headerText={SESSION_TEMPLATES_CREATE_CONSTANTS.ADDITIONAL_CONFIGURATIONS}
                                        >
                                            <ColumnLayout
                                                columns={2}
                                                variant="text-grid"
                                            >
                                                <SpaceBetween size="s">
                                                    <ValueWithLabel label={SESSION_TEMPLATES_CREATE_CONSTANTS.MAX_CONCURRENT_CLIENTS}>
                                                        {maxConcurrentClients || "Not specified"}
                                                    </ValueWithLabel>

                                                    <ValueWithLabel label={SESSION_TEMPLATES_CREATE_CONSTANTS.STORAGE_ROOT}>
                                                        {storageRoot || "Not specified"}
                                                    </ValueWithLabel>
                                                </SpaceBetween>
                                                <SpaceBetween size="s">
                                                    {getInitFileReviewItem()}

                                                    <ValueWithLabel label={SESSION_TEMPLATES_CREATE_CONSTANTS.REQUIREMENTS}>
                                                        {requirements || "Not specified"}
                                                    </ValueWithLabel>
                                                </SpaceBetween>
                                            </ColumnLayout>
                                        </ExpandableSection>}
                                    >
                                        <ColumnLayout
                                            columns={2}
                                            variant="text-grid"
                                        >
                                            <SpaceBetween size="s">
                                                <ValueWithLabel label={SESSION_TEMPLATES_CREATE_CONSTANTS.TEMPLATE_NAME}>
                                                    {name}
                                                </ValueWithLabel>
                                                <ValueWithLabel label={SESSION_TEMPLATES_CREATE_CONSTANTS.TEMPLATE_DESCRIPTION}>
                                                    {description || "Not specified"}
                                                </ValueWithLabel>
                                                <ValueWithLabel label={SESSION_TEMPLATES_CREATE_CONSTANTS.OS}>
                                                    {<OsLabel osFamily={os}/>}
                                                </ValueWithLabel>
                                                <ValueWithLabel label={SESSION_TEMPLATES_CREATE_CONSTANTS.TYPE}>
                                                    {capitalizeFirstLetter(type)}
                                                </ValueWithLabel>
                                            </SpaceBetween>
                                            <SpaceBetween size="s">
                                                {getOpenGLReviewItem()}
                                                {getAutorunFileReviewItem()}
                                                {getAutorunArgumentsReviewItem()}
                                            </SpaceBetween>
                                        </ColumnLayout>
                                    </Container>
                                </SpaceBetween>
                                <SpaceBetween size="xs">
                                    <Header
                                        variant="h3"
                                        actions={
                                            <Button
                                                onClick={() => setActiveStepIndex(1)}
                                            >
                                                Edit
                                            </Button>
                                        }
                                    >
                                        Step 2: {SESSION_TEMPLATES_CREATE_CONSTANTS.ASSIGN_USERS}
                                    </Header>
                                    <Container
                                        header={
                                            <Header variant="h2">
                                                {SESSION_TEMPLATES_CREATE_CONSTANTS.ASSIGN_USERS}
                                            </Header>
                                        }
                                    >
                                        <ColumnLayout
                                            columns={2}
                                            variant="text-grid"
                                        >
                                            <SpaceBetween size="s">
                                                <ValueWithLabel label={SESSION_TEMPLATES_CREATE_CONSTANTS.USERS}>
                                                    {getCleanArray(users).join(', ') || "Not specified"}
                                                </ValueWithLabel>
                                            </SpaceBetween>
                                            <SpaceBetween size="s">
                                                <ValueWithLabel label={SESSION_TEMPLATES_CREATE_CONSTANTS.GROUPS}>
                                                    {getCleanArray(groups).join(', ') || "Not specified"}
                                                </ValueWithLabel>
                                            </SpaceBetween>
                                        </ColumnLayout>
                                    </Container>
                                </SpaceBetween>
                            </SpaceBetween>
                    )
                }
            ]}
        />
    );
}
