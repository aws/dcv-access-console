import SideNavPanel from "@/components/common/side-nav-panel/SideNavPanel";

export default {
    title: 'components/common/SideNavPanel',
    component: SideNavPanel
}

const Template = args => <SideNavPanel{...args}/>
export const SideNavPanelNormal = Template.bind({})
SideNavPanelNormal.args = {
    pages: [
        {
            displayName: "Virtual Desktops",
            path: "/home/admin/console",
        },
        {
            displayName: "Users",
            path: "/home/admin/addUser",
        }
    ]
}
