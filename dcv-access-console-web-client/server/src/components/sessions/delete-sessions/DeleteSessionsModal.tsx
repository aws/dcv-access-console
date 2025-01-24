import * as React from "react";
import Modal from "@cloudscape-design/components/modal";
import Box from "@cloudscape-design/components/box";
import SpaceBetween from "@cloudscape-design/components/space-between";
import Button from "@cloudscape-design/components/button";
import {DELETE_SESSIONS_MODAL_CONSTANTS} from "@/constants/delete-sessions-modal-constants";

export type DeleteSessionProps = {
    sessionId?: string,
    owner?: string,
    sessionName?: string
}

export default ({visible, setVisible, closeSession, deleteSessionProps}: {
    visible: boolean,
    setVisible: (visible: boolean) => void,
    closeSession: (deleteSessionProps: DeleteSessionProps) => void,
    deleteSessionProps: DeleteSessionProps}) => {

    const closeSessionAndSetVisibleFalse = () => {
        closeSession(deleteSessionProps)
        setVisible(false)
    }

    return (
        <Modal
            onDismiss={() => setVisible(false)}
            visible={visible}
            footer={
                <Box float="right">
                    <SpaceBetween direction="horizontal" size="xs">
                        <Button variant="link" onClick={() => setVisible(false)}>{DELETE_SESSIONS_MODAL_CONSTANTS.CANCEL_BUTTON_TEXT}</Button>
                        <Button variant="primary" onClick={closeSessionAndSetVisibleFalse}>{DELETE_SESSIONS_MODAL_CONSTANTS.DELETE_SESSION_BUTTON_TEXT}</Button>
                    </SpaceBetween>
                </Box>
            }
            header={DELETE_SESSIONS_MODAL_CONSTANTS.MODAL_HEADER_FN(deleteSessionProps?.sessionName)}
        >
            {DELETE_SESSIONS_MODAL_CONSTANTS.MODAL_BODY}
        </Modal>
    );
}