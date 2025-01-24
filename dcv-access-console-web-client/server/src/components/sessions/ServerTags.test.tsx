import {render, screen} from "@testing-library/react";
import ServerTags from "@/components/sessions/ServerTags";
import {ServerTagsEmpty, ServerTagsWithTags} from "@/components/sessions/ServerTags.stories";
import {SERVER_TAG_CONSTANTS} from "@/constants/server-tags-constants";

describe('ServerTags', () => {
    it('Should render when empty', () => {
        render(<ServerTags {...ServerTagsEmpty.args}/>)
        expect(screen.getByText(SERVER_TAG_CONSTANTS.EMPTY_TEXT, {}));
    })
    it('Should render many', () => {
        render(<ServerTags {...ServerTagsWithTags.args}/>)
        ServerTagsWithTags.args.tags.forEach(tag => {
            expect(screen.getByRole("row", {name: tag.Key + " " + tag.Value}));
        })
    })

})
