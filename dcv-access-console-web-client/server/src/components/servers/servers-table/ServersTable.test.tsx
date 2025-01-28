// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {render, screen} from "@testing-library/react";
import {
    ServerTableAllColumns,
    ServerTableEmpty,
    ServerTableNormal
} from "@/components/servers/servers-table/ServersTable.stories";
import ServersTable from "@/components/servers/servers-table/ServersTable";
import {SERVERS_TABLE_CONSTANTS} from "@/constants/servers-table-constants";
import createWrapper from "@cloudscape-design/components/test-utils/dom";

jest.mock('next/navigation', () => ({
    useRouter: jest.fn(),
    usePathname() {
        return "/home/admin/servers";
    },

}));

describe('ServerTables', () => {
    it('Should render when empty', () => {
        render(<ServersTable {...ServerTableEmpty.args}/>)
        expect(screen.getByText(SERVERS_TABLE_CONSTANTS.EMPTY_TEXT, {}));
    })
    it('Should render default columns', () => {
        const {container} = render(<ServersTable {...ServerTableNormal.args}/>)
        const component = createWrapper(container)
        let table = component.findTable()

        ServerTableNormal.args.dataAccessServiceFunction({}).items.forEach((server, index) => {
            expect(table.findBodyCell(index + 1, 3).getElement()).toHaveTextContent(server.Hostname)
        });
    })

    it('Should render all columns', () => {
        const {container} = render(<ServersTable {...ServerTableAllColumns.args}/>)
        const component = createWrapper(container)
        let table = component.findTable()

        ServerTableNormal.args.dataAccessServiceFunction({}).items.forEach((server, index) => {
            expect(table.findBodyCell(index + 1, 3).getElement()).toHaveTextContent(server.Hostname)
        });

    })

    it('Should find preferences and click confirm', () => {
        const {container} = render(<ServersTable {...ServerTableNormal.args}/>)
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
