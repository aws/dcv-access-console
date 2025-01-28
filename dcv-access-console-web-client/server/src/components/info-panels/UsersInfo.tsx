// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import * as React from "react";
import {HelpPanel, Icon} from '@cloudscape-design/components';
import {DOC_CONSTANTS} from "@/constants/doc-constants";

export default function UsersInfo() {
    return (
        <HelpPanel
            header={<h2>Users</h2>}
            footer={
                <div>
                    <h3>Learn more <Icon name="external" /></h3>
                    <ul>
                        <li><a href={DOC_CONSTANTS.USERS}>Users</a></li>
                        <li><a href={DOC_CONSTANTS.IMPORT_USER_GROUPS}>Importing users and groups</a></li>
                    </ul>
                </div>
            }
        >
            <div>
                <p>
                    Users are saved in your datastore, and appear here if they have been directly imported from the Access Console, or have already logged in. For a complete list of users that are authorized to log into the Access Console, you must refer to your externally configured users datastore.
                    <br/><br/>
                    You can view available users, and their detailed information. You can configure the visible fields in the top navigation bar by selecting the gear icon.
                    <br/><br/>
                    To view more details in a split panel view, select a user, and then select the <code>^</code> button at the bottom-right corner of the page.
                </p>

                <h3>User roles</h3>
                <p>
                    There are two roles a user can have with the Access Console - <b>admin</b> and <b>user</b>. To change a user’s role, you must edit the user directly from your configured datastore.
                    <br/><br/>
                    <b>Admins</b> can create sessions, view and connect to all sessions, create and modify session templates, view host information, view and import users, and create and modify user groups.
                    <br/><br/>
                    <b>Users</b> can create and connect to their own sessions.
                </p>
            </div>
        </HelpPanel>
    )
}
