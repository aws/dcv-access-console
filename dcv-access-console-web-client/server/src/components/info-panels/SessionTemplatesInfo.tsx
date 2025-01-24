import * as React from "react";
import {HelpPanel, Icon} from '@cloudscape-design/components';
import {DOC_CONSTANTS} from "@/constants/doc-constants";

export default function SessionTemplatesInfo() {
    return (
        <HelpPanel
            header={<h2>Session Templates</h2>}
            footer={
                <div>
                    <h3>Learn more <Icon name="external" /></h3>
                    <ul>
                        <li><a href={DOC_CONSTANTS.CREATE_SESSION_TEMPLATE}>Creating a session template</a></li>
                        <li><a href={DOC_CONSTANTS.ASSIGN_SESSION_TEMPLATE}>Assigning a session template to users or groups</a></li>
                        <li><a href={DOC_CONSTANTS.DUPLICATE_SESSION_TEMPLATE}>Duplicating a session template</a></li>
                        <li><a href={DOC_CONSTANTS.EDIT_SESSION_TEMPLATE}>Editing a session template</a></li>
                        <li><a href={DOC_CONSTANTS.DELETE_SESSION_TEMPLATE}>Deleting a session template</a></li>
                    </ul>
                </div>
            }
        >
            <div>
                <p>
                    Session templates are sets of specified parameters that you can create a session with. To create a
                    session, you must first have an existing session template that you will use to create a session
                    from.
                    <br/><br/>
                    You can view session templates that you created, and their detailed information. You can configure
                    the visible fields in the top navigation bar by selecting the gear icon.
                    <br/><br/>
                    To view more details in a split panel view, select a template, and then select
                    the <code>^</code> button at the bottom-right corner of the page.
                </p>

                <h3>Creating a session template</h3>
                <p>
                    Only administrators can create session templates.
                </p>

                <h3>Assigning a session template to users or groups</h3>
                <p>
                    In order for users to create sessions, they must first have a session template assigned to them.
                    <br/><br/>
                    You may assign a session template to users or groups either during the original template creation
                    process or after a template has already been created.
                </p>

                <h3>Duplicating a session template</h3>
                <p>
                    Instead of creating a new session template, you can choose to duplicate an existing session template
                    and change its parameters to your specifications.
                </p>

                <h3>Editing a session template</h3>
                <p>
                    If you need to adjust any sessions details, you can edit the parameters of an existing session template.
                    <br/><br/>
                    Editing an existing template could affect users already assigned to it. Any changes you make will not affect the sessions already created. However, it will affect users the next time they create a session using the modified template.
                </p>

                <h3>Deleting a session template</h3>
                <p>
                    You can delete a session template when you're completely done with it.
                    <br/><br/>
                    Deleting a session can't be undone. Active sessions that were created with a deleted template won't be affected. However, any assigned users will no longer see the template available when they create a new session.
                </p>
            </div>
        </HelpPanel>
    )
}
