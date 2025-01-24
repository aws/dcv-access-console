import * as React from "react";
import {FileUpload} from "@cloudscape-design/components";
import {IMPORT_USERS_CONSTANTS} from "@/constants/import-users-constants";

export default ({file, setFile}: {
    file: File[],
    setFile: (file: File[]) => void}) => {
    return (
        <FileUpload
            onChange={({ detail }) => {setFile(detail.value)}}
            value={file}
            accept=".csv"
            i18nStrings={{
                uploadButtonText: e => IMPORT_USERS_CONSTANTS.UPLOAD_FILE_TEXT,
                dropzoneText: e => IMPORT_USERS_CONSTANTS.DROP_FILES_TEXT,
                removeFileAriaLabel: e =>
                    `Remove file ${e + 1}`,
                limitShowFewer: "Show fewer files",
                limitShowMore: "Show more files",
                errorIconAriaLabel: "Error"
            }}
            showFileLastModified
            showFileSize
        />
    )
}