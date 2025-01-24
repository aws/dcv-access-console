import {act, render, screen} from "@testing-library/react";
import SessionCards from "@/components/sessions/sessions-cards/SessionCards";
import {sessionCardsConstants} from "@/constants/sessions-constants";
import {
    SessionCardsEmpty,
    SessionCardsWithErrorScreenshots,
    SessionCardsWithScreenshots
} from "@/components/sessions/sessions-cards/SessionCards.stories";
import createWrapper from '@cloudscape-design/components/test-utils/dom';
import {
    AppRouterContext,
    type AppRouterInstance,
} from 'next/dist/shared/lib/app-router-context.shared-runtime';


describe('SessionCards', () => {
    it.skip('Should render when empty', () => {
        render(<SessionCards {...SessionCardsEmpty.args}/>)
        expect(screen.getByText(sessionCardsConstants.noSessionsMessage, {}));
    })

    it.skip('Should contain cards', () => {
        const {container} = render(<SessionCards {...SessionCardsWithScreenshots.args}/>)
        const component = createWrapper(container)

        for (let i = 0; i < SessionCardsWithScreenshots.args.sessionsState.sessions.length; i++){
            const session = SessionCardsWithScreenshots.args.sessionsState.sessions[i];
            let header = component.findCards()!.findItems()[i].findCardHeader()
            expect(header!.getElement()).toContainHTML("data-session-id=\"" + session.Id + "\"")
        }
    })

    it.skip('Should be selectable', () => {
        const mockFn = jest.fn()
        const {container} = render(<AppRouterContext.Provider value={{} as AppRouterInstance}>
            <SessionCards
                {...SessionCardsWithScreenshots.args}
                sessionsState={SessionCardsWithScreenshots.args.sessionsState}
                onSelectionChange={mockFn}/>
        </AppRouterContext.Provider>)
        const component = createWrapper(container)
        const cards = component.findCards();
        const cardList = cards?.findItems()

        act(() => {
            cardList?.[0].findSelectionArea().click()
        })
        expect(mockFn).toBeCalled()
    })

    it.skip('Should show error', () => {
        const mockFn = jest.fn()
        render(<SessionCards sessionsState={SessionCardsWithErrorScreenshots.args.sessionsState}
                             onSelectionChange={mockFn}/>)
        expect(screen.getAllByText("Unable to get screenshot, Expected Error", {})).toHaveLength(SessionCardsWithErrorScreenshots.args.sessionsState.sessions.length)
    })

})
