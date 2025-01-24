import * as React from "react";
import SideNavigation, {SideNavigationProps} from "@cloudscape-design/components/side-navigation";
import {service} from "@/constants/service-constants";
import {sideNavPanelConstants} from "@/constants/side-nav-panel-constants";
import {usePathname, useRouter} from "next/navigation";
import {Session} from "next-auth";
import {getSideNavPages} from "@/components/common/utils/PageUtils";
import Box from "@cloudscape-design/components/box";
import packageInfo from '@/../package-lock.json';
import {SpaceBetween} from "@cloudscape-design/components";

function getPageItems(pages: []) {
    const items: Array<SideNavigationProps.ExpandableLinkGroup | SideNavigationProps.Link | SideNavigationProps.Divider> =
        pages
    items.push({type: "divider"})
    items.push({
        type: "link",
        text: sideNavPanelConstants.documentation,
        href: service.documentation,
        external: true
    })
    items.push({
        type: "link",
        text: sideNavPanelConstants.downloads,
        info: <SpaceBetween size={"xs"}><Box></Box><Box color="text-body-secondary">{"Version "+ packageInfo.version}</Box></SpaceBetween>,
        href: service.download,
        external: true
    })
    return items
}

export default function SideNavPanel({session}: { session: Session }) {
    const router = useRouter();
    const pathname = usePathname();

    const [activeHref, setActiveHref] = React.useState(
        pathname + "/"
    );
    return (
        <SideNavigation
            activeHref={activeHref}
            onFollow={event => {
                if (!event.detail.external && event.detail.type !== "expandable-link-group") {
                    event.preventDefault();
                    setActiveHref(event.detail.href);
                    router.push(event.detail.href)
                }
            }}
            items={getPageItems(getSideNavPages(session))}
        />
    );
}
