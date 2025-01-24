import * as React from "react";
import {HelpPanel, Icon} from '@cloudscape-design/components';
import {DOC_CONSTANTS} from "@/constants/doc-constants";

export default function SessionInfo() {
    return (
        <HelpPanel
            header={<h2>Sessions</h2>}
            footer={
                    <div>
                        <h3>Learn more <Icon name="external" /></h3>
                        <ul>
                            <li><a href={DOC_CONSTANTS.SESSIONS}>Sessions</a></li>
                            <li><a href={DOC_CONSTANTS.CREATE_SESSIONS}>Creating a session</a></li>
                            <li><a href={DOC_CONSTANTS.CONNECT_TO_SESSION}>Connecting to a session</a></li>
                            <li><a href={DOC_CONSTANTS.CLOSE_SESSION}>Closing a session</a></li>
                        </ul>
                    </div>
                }
        >
            <div>
                <p>
                    A session is a span of time when the Amazon DCV server is able to accept connections from a client. Each session has a specified owner and set of permissions.
                    <br/><br/>
                    You can view available sessions with the detailed information. You can also configure the visible fields in the top navigation bar by selecting the icon.
                    <br/><br/>
                    To view more details in a split panel view, to choose the template and then select the <code>^</code> button at the bottom-right corner of the page.
                </p>

                <h3>Creating a session</h3>
                <p>
                    By creating a session, your default level of access is <b>owner</b>, which gives you administrator permissions.
                </p>

                <h3>Connecting to a session</h3>
                <p>
                    Connecting to a session connects you to the selected remote session. You can connect to a session from either the DCV web client, or a native Windows or macOS client application.
                </p>

                <h3>Closing a session</h3>
                <p>
                    After you’re completely done with your work, you can <b>close</b> a session and release the underlying resource back to the host server.
                    <br/><br/>
                    Closing a session can't be undone. All locally saved work will be lost. Closing a session does not shut down the underlying host server.
                </p>
            </div>
        </HelpPanel>
    )
}
