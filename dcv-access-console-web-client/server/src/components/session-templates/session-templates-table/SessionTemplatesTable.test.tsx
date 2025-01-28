// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {render, screen} from "@testing-library/react";
import {
    SessionTemplatesTableAllColumns,
    SessionTemplatesTableEmpty,
    SessionTemplatesTableNormal
} from "@/components/session-templates/session-templates-table/SessionTemplatesTable.stories";
import SessionTemplatesTable from "@/components/session-templates/session-templates-table/SessionTemplatesTable";
import {SESSION_TEMPLATES_TABLE_CONSTANTS} from "@/constants/session-templates-table-constants";
import createWrapper from "@cloudscape-design/components/test-utils/dom";

jest.mock('next/navigation', () => ({
    useRouter: jest.fn(),
    usePathname() {
        return "/home/admin/session-templates";
    },

}));

describe('SessionTemplatesTables', () => {
    it.skip('Should render when empty', () => {
        render(<SessionTemplatesTable {...SessionTemplatesTableEmpty.args}/>)
        expect(screen.getByText(SESSION_TEMPLATES_TABLE_CONSTANTS.EMPTY_TEXT, {}));
    })
    it.skip('Should render default columns', () => {
        const {container} = render(<SessionTemplatesTable {...SessionTemplatesTableNormal.args}/>)
        const component = createWrapper(container)
        let table = component.findTable()

        SessionTemplatesTableNormal.args.sessionTemplates.forEach((sessionTemplate, index) => {
            expect(table.findBodyCell(index + 1, 2).getElement()).toHaveTextContent(sessionTemplate.Name)
        });
    })

    it.skip('Should render all columns', () => {
        const {container} = render(<SessionTemplatesTable {...SessionTemplatesTableAllColumns.args}/>)
        const component = createWrapper(container)
        let table = component.findTable()

        SessionTemplatesTableAllColumns.args.sessionTemplates.forEach((sessionTemplate, index) => {
            expect(table.findBodyCell(index + 1, 2).getElement()).toHaveTextContent(sessionTemplate.Name)
        });

    })

    it.skip('Should find preferences and click confirm', () => {
        const {container} = render(<SessionTemplatesTable {...SessionTemplatesTableNormal.args}/>)
        const component = createWrapper(container)

        // Check that the settings is not visible
        expect(component.findCollectionPreferences().findModal()).toBeNull()

        // Click on the setting icon
        component.findCollectionPreferences().findTriggerButton().click()

        // Find the modal
        let preferencesModal = component.findCollectionPreferences().findModal()
        expect(preferencesModal).not.toBeNull()
        expect(preferencesModal.findConfirmButton()).not.toBeNull()

        // Click on Confirm
        preferencesModal.findConfirmButton().click()

        // Check that the settings is not visible
        expect(component.findCollectionPreferences().findModal()).toBeNull()

    })

})
