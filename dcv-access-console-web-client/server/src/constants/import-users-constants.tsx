export const IMPORT_USERS_CONSTANTS = {
    IMPORT_USERS_TEXT: "Import users",
    UPLOAD_FILE_TEXT: "Upload file",
    DROP_FILES_TEXT: "Drop file to upload",
    CANCEL_BUTTON_TEXT: "Cancel",
    IMPORT_USERS_HELP_TEXT: <div>Upload a CSV file with the following header:<br/><br/>
        "userId","displayName","role","groups"<br/>
        <ul>
        <li>userId - This field is required.<br/></li>
        <li>displayName - This field is optional. It will be set to the same as userId, if left empty.<br/></li>
        <li>role - This field is optional, and can be set to either Admin or User. It will be set to User, if left empty.<br/></li>
        <li>groups - This field is optional. You can include multiple GroupIDs, separated by “|”.</li>
        </ul>
    </div>
}