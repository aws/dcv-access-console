// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {render} from "@testing-library/react";
import {ErrorNormal} from "@/components/common/error/Error.stories";
import {AppRouterContext, AppRouterInstance} from "next/dist/shared/lib/app-router-context.shared-runtime";

describe('Error', () => {
    it('Should render', () => {
        render(<AppRouterContext.Provider value={{} as AppRouterInstance}><ErrorNormal/></AppRouterContext.Provider>)
    })
})
