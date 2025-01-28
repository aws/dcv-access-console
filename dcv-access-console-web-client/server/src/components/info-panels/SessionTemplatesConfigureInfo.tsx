// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import * as React from "react";
import {HelpPanel, Icon} from '@cloudscape-design/components';
import {DOC_CONSTANTS} from "@/constants/doc-constants";

export default function SessionTemplatesConfigureInfo() {
    return (
        <HelpPanel
            header={<h2>Create session templates</h2>}
            footer={
                <div>
                    <h3>Learn more <Icon name="external" /></h3>
                    <ul>
                        <li><a href={DOC_CONSTANTS.CREATE_SESSION_TEMPLATE}>Creating a session template</a></li>
                        <li><a href={DOC_CONSTANTS.CREATE_SESSIONS}>Create sessions</a></li>
                    </ul>
                </div>
            }
        >
            <div>
                <h3>Configure Template Parameters (step 1)</h3>
                <p>
                    Session templates are sets of specified parameters that you can create a session with. To create a session template, you can specify template parameters and assign the template to users and groups. You may edit a session template after creation.
                    <br/><br/>
                    To create a session, users must first have an existing session template that they will use to create a session from.
                </p>
            </div>
        </HelpPanel>
    )
}
