// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import * as React from "react";
import {HelpPanel, Icon} from '@cloudscape-design/components';
import {DOC_CONSTANTS} from "@/constants/doc-constants";

export default function CreateSessionInfo() {
    return (
        <HelpPanel
            header={<h2>Create Session</h2>}
            footer={
                <div>
                    <h3>Learn more <Icon name="external" /></h3>
                    <ul>
                        <li><a href={DOC_CONSTANTS.CREATE_SESSIONS}>Creating a session</a></li>
                    </ul>
                </div>
            }
        >
            <div>
                <p>
                    To create a new session, you must select a template already provided by the administrator. Session templates are sets of specified parameters that you can create a session with.
                    <br/><br/>
                    By creating a new session, your default level of access is <b>owner</b>, which gives you admin permissions.
                </p>
            </div>
        </HelpPanel>
    )
}
