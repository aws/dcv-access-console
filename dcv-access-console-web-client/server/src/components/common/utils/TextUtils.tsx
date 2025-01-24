import {LoggedInUser} from "@/generated-src/client";
import {SERVERS_TABLE_CONSTANTS} from "@/constants/servers-table-constants";
import {getValueOrUnknown} from "@/components/common/utils/SearchUtils";
import moment from "moment/moment";

const USER_OVER_TOTAL_DELIMITER = " / "

export function capitalizeFirstLetter(string: string) {
    string = string.replace(/_/g, ' ');
    string = string.toLowerCase();
    return string.charAt(0).toUpperCase() + string.slice(1);
}

export function usersToCommaSeperatedString(users: LoggedInUser[] | undefined) {
    if (!users || users.length === 0) {
        // TODO: Make this formatted as placeholder text
        return <i>None</i>;
    }
    return users.map(user => user.Username).join(', ');
}

export function getCleanArray(array: string[]) {
    if(!array) return []
    return array.map(item => item && item.trim() ? item.trim() : undefined).filter(Boolean)
}

export function formatFileSize(bytes: number | undefined) {
    if (bytes === undefined) {
        return SERVERS_TABLE_CONSTANTS.UNKNOWN;
    }
    if (bytes === 0) {
        return '0 B';
    }
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

export function formatPercentage(percentage: number | undefined, fractionDigits = 0) {
    if (percentage === undefined) {
        return SERVERS_TABLE_CONSTANTS.UNKNOWN;
    }
    return percentage.toFixed(fractionDigits) + "%"
}

export function formatUserOverTotal(used: number | string, total: number | string) {
    return used + USER_OVER_TOTAL_DELIMITER + total
}

export function formatDate(date?: string) {
    if (!date) {
        return "Never"
    }
    return moment(date).local(true).format('ll hh:mmA')
}