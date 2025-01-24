import * as React from "react";
import {HelpPanel, Icon} from '@cloudscape-design/components';
import {DOC_CONSTANTS} from "@/constants/doc-constants";

export default function SessionTemplatesAssignInfo() {
    return (
        <HelpPanel
            header={<h2>Create session templates</h2>}
            footer={
                <div>
                    <h3>Learn more <Icon name="external" /></h3>
                    <ul>
                        <li><a href={DOC_CONSTANTS.CREATE_SESSION_TEMPLATE}>Creating a session template</a></li>
                    </ul>
                </div>
            }
        >
            <div>
                <h3>Assign Users and Groups (step 2)</h3>
                <p>
                    You can assign users and groups to this template, so they may use it to create sessions. You can also assign users and groups to a template after creation from the <b>Session Templates</b> page.
                </p>
            </div>
        </HelpPanel>
    )
}
