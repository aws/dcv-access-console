import * as React from "react";
import {HelpPanel, Icon} from '@cloudscape-design/components';
import {DOC_CONSTANTS} from "@/constants/doc-constants";

export default function UserGroupsInfo() {
    return (
        <HelpPanel
            header={<h2>User Groups</h2>}
            footer={
                <div>
                    <h3>Learn more <Icon name="external" /></h3>
                    <ul>
                        <li><a href={DOC_CONSTANTS.USER_GROUPS}>User groups</a></li>
                        <li><a href={DOC_CONSTANTS.IMPORT_USER_GROUPS}>Importing users and groups</a></li>
                    </ul>
                </div>
            }
        >
            <div>
                <p>
                    User groups can be imported, or created and can only include users that are saved in your datastore.
                    You can import user groups for the <b>Users</b> page. You can modify user groups and their assigned
                    templates here.
                    <br/><br/>
                    You can view user groups, and their detailed information. You can configure the visible fields in
                    the top navigation bar by selecting the gear icon.
                    <br/><br/>
                    To view more details in a split panel view, select a user, and then select the <code>^</code> button
                    at the bottom-right corner of the page.
                </p>

                <h3>Creating user groups</h3>
                <p>
                    You can create a user group by selecting users and assigning templates.
                </p>

                <h3>Editing user groups</h3>
                <p>
                    You can edit a user group by modifying the group name, users in the group and templates assigned to the group.
                </p>
            </div>
        </HelpPanel>
    )
}
