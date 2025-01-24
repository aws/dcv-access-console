'use client'

import TopNavBar from "@/components/common/top-nav-bar/TopNavBar";
import * as React from "react";
import {useEffect, useState} from "react";
import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";
import {
    AppLayout,
    AppLayoutProps,
    Container,
    ContentLayout,
    Flashbar,
    FormField,
    Grid,
    Header,
    Input,
    NonCancelableCustomEvent,
    SpaceBetween
} from "@cloudscape-design/components";
import Breadcrumb from "@/components/common/breadcrumb/Breadcrumb";
import ConsoleHeader from "@/components/common/console-header/ConsoleHeader";
import {SESSIONS_CONSTANTS} from "@/constants/sessions-constants";
import {SESSIONS_CREATE_CONSTANTS} from "@/constants/sessions-create-constants";
import SessionTemplatesTable from "@/components/session-templates/session-templates-table/SessionTemplatesTable";
import CreateSessionInfo from "@/components/info-panels/CreateSessionInfo";
import Button from "@cloudscape-design/components/button";
import {useRouter} from "next/navigation";
import Box from "@cloudscape-design/components/box";
import {CreateSessionUIRequestData, SessionTemplate} from "@/generated-src/client";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {SESSION_TEMPLATES_CREATE_CONSTANTS} from "@/constants/session-templates-create-constants";
import {useFlashBarContext} from "@/context-providers/FlashBarContext";
import {GLOBAL_CONSTANTS} from "@/constants/global-constants";
import {useSessionTemplatesService} from "@/components/common/hooks/DataAccessServiceHooks";
import {PropertyFilterQuery} from "@cloudscape-design/collection-hooks";
import usePageLoading from "@/components/common/hooks/PageLoadingHook";
import LoadingSkeleton from "@/components/common/loadingSkeleton/LoadingSkeleton";
import {
    CREATE_SESSION_PREFERENCES
} from "@/components/session-templates/session-templates-table/SessionTemplatesTableColumnPreferences";
import {isAdminRoleFromSession} from "@/components/common/utils/TokenAccessService";

export default function CreateSession() {
    const [name, setName] = useState()
    const [selectedSessionTemplates, setSelectedSessionTemplates] = useState<SessionTemplate[]>([])
    const [errorName, setErrorName] = useState()
    const [errorSessionTemplate, setErrorSessionTemplate] = useState()
    const {loading: pageLoading, userSession} = usePageLoading()
    const [loading, setLoading] = React.useState(false)
    const [describeSessionTemplatesQuery, setDescribeSessionTemplatesQuery] = useState<PropertyFilterQuery>({
        tokens: [],
        operation: "and"
    })

    const {push} = useRouter()
    const {addSessionFlashBar, addCreateSessionFailedFlashBar} = useFlashBarContext()

    const [toolsOpen, setToolsOpen] = useState<boolean>(false)
    const onToolChange = (event: NonCancelableCustomEvent<AppLayoutProps.ChangeDetail>): void => {
        setToolsOpen(event.detail.open);
    };

    const sessionsCreateContainer = <Container
        header={
            <Header
                variant="h2">
                {SESSIONS_CREATE_CONSTANTS.SESSION_DETAILS}
            </Header>
        }>
        <SpaceBetween direction="vertical" size="l">
            <FormField label={SESSIONS_CREATE_CONSTANTS.DISPLAY_NAME}
                       description={SESSIONS_CREATE_CONSTANTS.DISPLAY_NAME_DESCRIPTION}
                       errorText={errorName}
                       stretch={true} >
                <Input
                    onChange={({detail}) => {
                        if (!detail.value || detail.value.trim()) {
                            setName(detail.value)
                            setErrorName(undefined)
                        }
                    }}
                    value={name}
                    placeholder={SESSIONS_CREATE_CONSTANTS.DISPLAY_NAME_PLACEHOLDER}
                    spellcheck
                />
            </FormField>

            <FormField label={SESSIONS_CREATE_CONSTANTS.SESSION_TEMPLATE}
                       description={SESSIONS_CREATE_CONSTANTS.SESSION_TEMPLATE_DESCRIPTION}
                       errorText={errorSessionTemplate}
                       stretch={true} >
                <SessionTemplatesTable
                    selectedSessionTemplates={selectedSessionTemplates}
                    setSelectedSessionTemplates={setSelectedSessionTemplates}
                    addHeader={false}
                    variant={"borderless"}
                    allowSessionTemplateManagement={isAdminRoleFromSession(userSession)}
                    query={describeSessionTemplatesQuery}
                    setQuery={setDescribeSessionTemplatesQuery}
                    dataAccessServiceFunction={useSessionTemplatesService}
                    columnPreferences={CREATE_SESSION_PREFERENCES}
                />
            </FormField>
        </SpaceBetween>
    </Container>

    const createSessionInfo = <CreateSessionInfo/>

    function createSession() {
        const createSessionRequest: CreateSessionUIRequestData = {
            Name: name && name.trim() ? name.trim() : undefined,
            SessionTemplateId: selectedSessionTemplates.length ? selectedSessionTemplates[0].Id : undefined
        } as CreateSessionUIRequestData

        const dataService = new DataAccessService();
        console.log("createSessionRequest: ", createSessionRequest)
        dataService.createSessions([createSessionRequest])
            .then(r => {
                console.log("Successful sessions: ", r.data.SuccessfulList)

                r.data.SuccessfulList?.forEach(session => {
                    console.log("Adding sessionId to flashbar: ", session.Id)
                    addSessionFlashBar(session.Id, session.Name)
                })

                r.data.UnsuccessfulList?.forEach(unsuccessfulCreateSessionUIRequestData => {
                    addCreateSessionFailedFlashBar(unsuccessfulCreateSessionUIRequestData.CreateSessionRequestData.Name, unsuccessfulCreateSessionUIRequestData.FailureReasons["Broker"])
                })
            }).catch(e => {
            console.error("Failed to create session: ", e)
            addCreateSessionFailedFlashBar(name && name.trim() ? name.trim() : undefined, e.code? e.code : "Unknown error")
        })
    }

    useEffect(() => {
        if(selectedSessionTemplates.length) {
            setErrorSessionTemplate(undefined)
        }
    }, [selectedSessionTemplates])
    const {items} = useFlashBarContext()

    if(pageLoading) return <LoadingSkeleton/>
    return (
        <div>
            <TopNavBar session={userSession}/>
            <AppLayout
                breadcrumbs={<Breadcrumb/>}
                notifications={<Flashbar items={items} stackItems/>}
                navigation={
                    <SideNavPanel session={userSession}/>
                }
                maxContentWidth={Number.MAX_VALUE}
                content={
                    <ContentLayout
                        header={
                            <ConsoleHeader headerTitle={SESSIONS_CONSTANTS.CREATE_SESSION_TEXT}
                                           headerDescription={SESSIONS_CREATE_CONSTANTS.CREATE_SESSION_DESCRIPTION}
                                           infoLinkFollow={() => setToolsOpen(true)}
                                           infoLinkLabel={GLOBAL_CONSTANTS.INFO_LABEL}
                            />
                        }>
                        <Grid gridDefinition={[{colspan: 10}, {colspan: 2}]}>
                            <SpaceBetween size={"m"}>
                                {sessionsCreateContainer}
                                <Box float="right">
                                    <SpaceBetween direction="horizontal" size="xs">
                                        <Button onClick={() => {
                                            push(GLOBAL_CONSTANTS.SESSIONS_URL)
                                        }}>Cancel</Button>
                                        <Button variant={"primary"} onClick={() => {
                                            setLoading(true)
                                            if (!name || !name.trim()) {
                                                setErrorName(SESSIONS_CREATE_CONSTANTS.DISPLAY_NAME_REQUIRED)
                                            }
                                            if (!selectedSessionTemplates.length) {
                                                setErrorSessionTemplate(SESSION_TEMPLATES_CREATE_CONSTANTS.SESSION_TEMPLATE_REQUIRED)
                                            }
                                            if (name && name.trim() && selectedSessionTemplates.length) {
                                                createSession()
                                                push(GLOBAL_CONSTANTS.SESSIONS_URL)
                                            }
                                            setLoading(false)
                                        }}
                                        loading={loading}>{SESSIONS_CONSTANTS.CREATE_SESSION_TEXT}</Button>
                                    </SpaceBetween>
                                </Box>
                            </SpaceBetween>
                            <div></div>
                        </Grid>
                    </ContentLayout>
                }
                tools={createSessionInfo}
                toolsOpen={toolsOpen}
                onToolsChange={onToolChange}
            >
            </AppLayout>
        </div>
    )
}
