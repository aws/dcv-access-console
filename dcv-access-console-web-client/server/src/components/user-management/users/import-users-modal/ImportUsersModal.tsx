// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import * as React from "react";
import Modal from "@cloudscape-design/components/modal";
import Box from "@cloudscape-design/components/box";
import SpaceBetween from "@cloudscape-design/components/space-between";
import Button from "@cloudscape-design/components/button";
import {IMPORT_USERS_CONSTANTS} from "@/constants/import-users-constants";
import ImportUsersFileUpload from "@/components/user-management/users/import-users-file-upload/ImportUsersFileUpload";

export type ImportUsersProps = {
    file?: File,
    overwriteExistingUsers?: boolean,
    overwriteGroups?: boolean
}

export default ({visible, setVisible, importUsers}: {
    visible: boolean,
    setVisible: (visible: boolean) => void,
    importUsers: (importUsersProps: ImportUsersProps) => void}) => {
    const [file, setFile] = React.useState<File[]>([])
    const importUsersAndSetVisibleFalse = () => {
        const importUsersProps: ImportUsersProps = {
            file: file? file[0] : undefined,
            overwriteExistingUsers: true,
            overwriteGroups: false
        }
        importUsers(importUsersProps)
        setFile([])
        setVisible(false)
    }

    return (
        <Modal
            onDismiss={() => setVisible(false)}
            visible={visible}
            footer={
                <Box float="right">
                    <SpaceBetween direction="horizontal" size="xs">
                        <Button variant="link" onClick={() => setVisible(false)}>{IMPORT_USERS_CONSTANTS.CANCEL_BUTTON_TEXT}</Button>
                        <Button variant="primary" disabled={!file.length} onClick={importUsersAndSetVisibleFalse}>{IMPORT_USERS_CONSTANTS.IMPORT_USERS_TEXT}</Button>
                    </SpaceBetween>
                </Box>
            }
            header={IMPORT_USERS_CONSTANTS.IMPORT_USERS_TEXT}
        >
            <ul>

            </ul>
            {IMPORT_USERS_CONSTANTS.IMPORT_USERS_HELP_TEXT}
            <ImportUsersFileUpload file={file} setFile={setFile}/>
        </Modal>
    )
}
