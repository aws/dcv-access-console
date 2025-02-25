'use client';

import {Alert} from "@cloudscape-design/components";

export default function AccessDenied() {
    return (
        <Alert
            statusIconAriaLabel="Error"
            type="error"
            header="Access Denied"
        >
            You do not have access to this page
        </Alert>
    );

}
