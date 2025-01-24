import ServerTags from "@/components/sessions/ServerTags";
import {sessions} from "@/components/sessions/sessions-cards/SessionCards.stories";

export default {
    title: 'components/common/ServerTags',
    component: ServerTags,
}

const Template = args => <ServerTags{...args}/>

export const ServerTagsEmpty = Template.bind({})
ServerTagsEmpty.args = {
    tags: []
}

export const ServerTagsWithTags = Template.bind({})
ServerTagsWithTags.args = {
    tags: sessions[0].Server.Tags
}
