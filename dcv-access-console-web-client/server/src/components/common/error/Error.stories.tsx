import Error from "@/components/common/error/Error";

export default {
    title: 'components/common/Error',
    component: Error,
}


const Template = (args) => {
    return <Error{...args}/>
}

export const ErrorNormal = Template.bind({})
ErrorNormal.args = {
}
