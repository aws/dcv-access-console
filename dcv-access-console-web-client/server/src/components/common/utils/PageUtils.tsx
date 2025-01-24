import {Session} from "next-auth";
import {isAdminRoleFromSession} from "@/components/common/utils/TokenAccessService";
import {GLOBAL_CONSTANTS} from "@/constants/global-constants";

export function getSideNavPages(session: Session) {
    if (!session) {
        return []
    }
    let pages : any[] = [
        {
            type: "expandable-link-group",
            text: "Session management",
            defaultExpanded: true,
            items: [
                {
                    type: "link",
                    text: GLOBAL_CONSTANTS.SESSIONS,
                    href: GLOBAL_CONSTANTS.SESSIONS_URL,
                }
            ]
        }
    ]
    if (isAdminRoleFromSession(session)) {
        pages[0].items.push(
            {
                type: "link",
                text: GLOBAL_CONSTANTS.SESSION_TEMPLATES,
                href: GLOBAL_CONSTANTS.SESSION_TEMPLATES_URL,
            }
        )
        pages.push(
            {
                type: "link",
                text: GLOBAL_CONSTANTS.HOSTS,
                href: GLOBAL_CONSTANTS.HOSTS_URL
            },
            {
                type: "expandable-link-group",
                text: "User management",
                defaultExpanded: true,
                items: [
                    {
                        type: "link",
                        text: GLOBAL_CONSTANTS.USERS,
                        href: GLOBAL_CONSTANTS.USERS_URL,
                    },
                    {
                        type: "link",
                        text: GLOBAL_CONSTANTS.USER_GROUPS,
                        href: GLOBAL_CONSTANTS.USER_GROUPS_URL,
                    }
                ]
            }
        )
    }
    return pages
}
