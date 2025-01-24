import * as React from "react";
import Modal from "@cloudscape-design/components/modal";
import Box from "@cloudscape-design/components/box";
import SpaceBetween from "@cloudscape-design/components/space-between";
import Button from "@cloudscape-design/components/button";
import {DELETE_SESSION_TEMPLATES_MODAL_CONSTANTS} from "@/constants/delete-session-templates-modal-constants";

export type DeleteSessionTemplateProps = {
    sessionTemplateIds: string[],
    sessionTemplateNames: string[],
}
export default ({visible, setVisible, deleteSessionTemplate, deleteSessionTemplateProps}: {
    visible: boolean,
    setVisible: (visible: boolean) => void,
    deleteSessionTemplate: (deleteSessionTemplateProps: DeleteSessionTemplateProps) => void,
    deleteSessionTemplateProps?: DeleteSessionTemplateProps}) => {

    const closeSessionTemplateAndSetVisibleFalse = () => {
        deleteSessionTemplate(deleteSessionTemplateProps!)
        setVisible(false)
    }

    return (
        <Modal
            onDismiss={() => setVisible(false)}
            visible={visible}
            footer={
                <Box float="right">
                    <SpaceBetween direction="horizontal" size="xs">
                        <Button variant="link" onClick={() => setVisible(false)}>{DELETE_SESSION_TEMPLATES_MODAL_CONSTANTS.CANCEL_BUTTON_TEXT}</Button>
                        <Button variant="primary" onClick={closeSessionTemplateAndSetVisibleFalse}>{DELETE_SESSION_TEMPLATES_MODAL_CONSTANTS.DELETE_SESSION_TEMPLATE_BUTTON_TEXT}</Button>
                    </SpaceBetween>
                </Box>
            }
            header={DELETE_SESSION_TEMPLATES_MODAL_CONSTANTS.MODAL_HEADER_FN(deleteSessionTemplateProps?.sessionTemplateNames)}
        >
            {DELETE_SESSION_TEMPLATES_MODAL_CONSTANTS.MODAL_BODY_FN(deleteSessionTemplateProps?.sessionTemplateIds)}
        </Modal>
    );
}
