import * as React from "react";
import {HelpPanel, Icon} from '@cloudscape-design/components';
import {DOC_CONSTANTS} from "@/constants/doc-constants";

export default function SessionTemplatesReviewInfo() {
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
                <h3>Review and Create (step 3)</h3>
                <p>
                    You can review your session template configuration and make any necessary edits, before you finish creating it.
                </p>
            </div>
        </HelpPanel>
    )
}
