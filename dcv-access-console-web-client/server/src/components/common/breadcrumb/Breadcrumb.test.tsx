import Breadcrumb from "@/components/common/breadcrumb/Breadcrumb";
import {render, screen} from "@testing-library/react";
import {BreadcrumbNormal} from "@/components/common/breadcrumb/Breadcrumb.stories";
import {userEvent} from "@storybook/testing-library";
import {useRouter} from "next/navigation";
import preview from "../../../../.storybook/preview";

jest.mock('next/navigation', () => ({
    useRouter: jest.fn(),
    usePathname() {
        return preview.parameters.nextjs.navigation.pathname;
    },

}));

describe('Breadcrumb', () => {

    it('Should render', () => {
        render(<BreadcrumbNormal/>)
    })

    it('Should have links with camel case names', () => {

        // Render
        render(<BreadcrumbNormal/>)

        // Link names should be camel cased and contain hrefs
        expect(screen.getByRole("link", {name: 'Home'})).toHaveAttribute("href", "/home")
    })

    it('Should push to router when clicked', async () => {
        const push = jest.fn();

        // Create a mock function for useRouter
        (useRouter as jest.Mock).mockImplementation(() => ({
            push,
        }));

        // Render
        render(<BreadcrumbNormal/>)

        // Click on Home
        await userEvent.click(screen.getByRole("link", {name: 'Home'}));

        // Expect mock function to be called
        expect(push).toHaveBeenCalledWith("/home");
    })
})
