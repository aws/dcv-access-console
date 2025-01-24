import {render, screen} from "@testing-library/react";
import preview from "../../../../.storybook/preview";
import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";
import {SideNavPanelNormal} from "@/components/common/side-nav-panel/SideNavPanel.stories";
import {useRouter} from "next/navigation";
import {userEvent} from "@storybook/testing-library";
import {sideNavPanelConstants} from "@/constants/side-nav-panel-constants";
import React from "react";

jest.mock('next/navigation', () => ({
    useRouter: jest.fn(),
    usePathname() {
        return preview.parameters.nextjs.navigation.pathname;
    },

}));

describe('SideNavPanel', () => {

    it.skip('Should render', () => {
        render(<SideNavPanelNormal {...SideNavPanelNormal.args}/>)

        expect(screen.getByRole("link", {name: SideNavPanelNormal.args.pages[0].displayName}))
        expect(screen.getByRole("link", {name: SideNavPanelNormal.args.pages[1].displayName}))
        expect(screen.getByRole("link", {name: sideNavPanelConstants.documentation}))
        expect(screen.getByRole("separator", {name: ""}))
    })

    it.skip('Should push to router when clicked', async () => {
        const push = jest.fn();

        // Create a mock function for useRouter
        (useRouter as jest.Mock).mockImplementation(() => ({
            push,
        }));

        // Render
        render(<SideNavPanelNormal {...SideNavPanelNormal.args}/>)

        // Click on Home
        await userEvent.click(screen.getByRole("link", {name: SideNavPanelNormal.args.pages[0].displayName}));

        // Expect mock function to be called
        expect(push).toHaveBeenCalledWith(SideNavPanelNormal.args.pages[0].path);
    })

})
