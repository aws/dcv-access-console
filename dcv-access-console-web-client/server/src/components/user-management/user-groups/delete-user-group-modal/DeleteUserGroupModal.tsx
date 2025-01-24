import * as React from "react";
import Modal from "@cloudscape-design/components/modal";
import Box from "@cloudscape-design/components/box";
import SpaceBetween from "@cloudscape-design/components/space-between";
import Button from "@cloudscape-design/components/button";
import {DELETE_USER_GROUP_MODAL_CONSTANTS} from "@/constants/delete-user-group-modal-constants";

export type DeleteUserGroupProps = {
    userGroupId?: string,
    userGroupDisplayName?: string
}

export default ({visible, setVisible, deleteUserGroup, deleteUserGroupProps}: {
    visible: boolean,
    setVisible: (visible: boolean) => void,
    deleteUserGroup: (deleteSessionProps: DeleteUserGroupProps) => void,
    deleteUserGroupProps: DeleteUserGroupProps}) => {

    const deleteUserGroupAndSetVisibleFalse = () => {
        deleteUserGroup(deleteUserGroupProps)
        setVisible(false)
    }

    return (
        <Modal
            onDismiss={() => setVisible(false)}
            visible={visible}
            footer={
                <Box float="right">
                    <SpaceBetween direction="horizontal" size="xs">
                        <Button variant="link" onClick={() => setVisible(false)}>{DELETE_USER_GROUP_MODAL_CONSTANTS.CANCEL_BUTTON_TEXT}</Button>
                        <Button variant="primary" onClick={deleteUserGroupAndSetVisibleFalse}>{DELETE_USER_GROUP_MODAL_CONSTANTS.DELETE_GROUP_BUTTON_TEXT}</Button>
                    </SpaceBetween>
                </Box>
            }
            header={DELETE_USER_GROUP_MODAL_CONSTANTS.MODAL_HEADER_FN(deleteUserGroupProps.userGroupDisplayName)}
        >
            {DELETE_USER_GROUP_MODAL_CONSTANTS.MODAL_BODY}
        </Modal>
    );
}