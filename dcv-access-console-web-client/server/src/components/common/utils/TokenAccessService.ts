import {getSession} from "next-auth/react";
import {Session} from "next-auth";

export function getToken(session) {
    return 'Bearer ' + session.access_token
}
export async function isAdminRole() {
    let session = await getSession();
    return session?.userRole === "Admin"
}

export function isAdminRoleFromSession(session: Session) {
    return session?.userRole === "Admin"
}
