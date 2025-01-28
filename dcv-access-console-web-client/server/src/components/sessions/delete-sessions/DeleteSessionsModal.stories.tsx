// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import DeleteSessionsModal from "@/components/sessions/delete-sessions/DeleteSessionsModal";

export default {
    title: 'components/common/DeleteSessionModal',
    component: DeleteSessionsModal,
}

const Template = args => <DeleteSessionsModal{...args}/>

export const DeleteSessionModalNormal = Template.bind({})
DeleteSessionModalNormal.args = {
    visible: true,
    setVisible: (visible: boolean) => {},
    closeSession: (sessionId: string, owner: string, sessionName: string) => {},
    deleteSessionProps: {
        sessionId: "test-id",
        owner: "admin",
        sessionName: "test session name"
    }
}