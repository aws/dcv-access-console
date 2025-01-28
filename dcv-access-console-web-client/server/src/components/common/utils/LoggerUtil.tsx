// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {format} from "logform";
import combine = format.combine;

const winston = require('winston');
require('winston-daily-rotate-file')

let transport = new winston.transports.DailyRotateFile({
    filename: 'dcv-access-console-web-client-%DATE%.log',
    dirname: process.env.SM_UI_WEBCLIENT_LOG_DIR,
    datePattern: 'YYYY-MM-DD',
    zippedArchive: true,
    maxSize: '20m',
    maxFiles: '7d',
    createSymlink: true,
    symlinkName: 'dcv-access-console-web-client.log',
});


export const logger = winston.createLogger({
    level: 'info',
    format: combine(
        format.timestamp({
            format: 'DD MMM YYYY HH:mm:ss.SSS'
        }),
        format.align(),
        format.printf(({ level, message, timestamp }) => {
            return `${timestamp} ${level.toUpperCase()} ${message}`;
        }),
    ),
    defaultMeta: {service: 'dcv-access-console-web-client'},
    transports: [transport],
});
