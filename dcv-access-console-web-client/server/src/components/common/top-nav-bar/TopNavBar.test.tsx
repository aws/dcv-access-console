import {act, render, screen} from "@testing-library/react";
import {TopNavNormal} from "@/components/common/top-nav-bar/TopNavBar.stories";
import {service} from "@/constants/service-constants";
import {userEvent} from "@storybook/testing-library";
import {SessionProvider} from "next-auth/react";

describe('TopNavBar', () => {

    it.skip('Should render', () => {

        render(
            <SessionProvider>
                <TopNavNormal {...TopNavNormal.args}/>
            </SessionProvider>
        )

        expect(screen.getByRole("link", {name: service.dcvLogo.alt + " " + service.name}))
        expect(screen.getByRole("button", {name: TopNavNormal.args.userInfo.displayName}))
    })

    it.skip('Should have the user email', async () => {

        // See https://legacy.reactjs.org/docs/test-utils.html#act
        await act(()=> {
            render(
                <SessionProvider>
                    <TopNavNormal {...TopNavNormal.args}/>
                </SessionProvider>
            )
        });

        await act(() => {
            userEvent.click(screen.getByRole("button", {name: TopNavNormal.args.userInfo.displayName}));
        })
        // Verify the email is present
        expect(screen.getByRole("menu", {name: TopNavNormal.args.userInfo.email}))

    })
})
