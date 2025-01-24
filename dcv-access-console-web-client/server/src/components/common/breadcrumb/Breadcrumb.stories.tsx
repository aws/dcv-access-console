import Breadcrumb from "@/components/common/breadcrumb/Breadcrumb";

export default {
    title: 'components/common/Breadcrumb',
    component: Breadcrumb,
}


const Template = (args) => {
    return <Breadcrumb{...args}/>
}

export const BreadcrumbNormal = Template.bind({})
BreadcrumbNormal.args = {
}
