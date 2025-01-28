// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import {AttributeEditor, Input} from "@cloudscape-design/components";
import {SESSION_TEMPLATES_CREATE_CONSTANTS} from "@/constants/session-templates-create-constants";
import * as React from "react";
import {useEffect} from "react";
import DataAccessService from "@/components/common/utils/DataAccessService";
import {DescribeUserGroupsSharedWithSessionTemplateRequestData} from "@/generated-src/client";

export function AssignUsersAttributeEditor({items,
                                            setItems,
                                           isUsers,
                                           setLoading,
                                           sessionTemplateId}:
                                           {
                                            items: string[],
                                            setItems: (string) => void,
                                            isUsers: boolean,
                                            setLoading: (boolean) => void,
                                            sessionTemplateId: string | null
                                           }) {
    useEffect(() => {
        setLoading(true)
        if (sessionTemplateId) {
            if (isUsers) {
                new DataAccessService().describeUsersSharedWithSessionTemplate({
                    SessionTemplateId: sessionTemplateId
                }).then(result => {
                    // TODO: Add pagination to the users list
                    if (result.data.Users?.length) {
                        setItems(result.data.Users?.map(user => {
                            return user.UserId!
                        }))
                    }
                })
            } else {
                new DataAccessService().describeUserGroupsSharedWithSessionTemplate({
                    SessionTemplateId: sessionTemplateId
                } as DescribeUserGroupsSharedWithSessionTemplateRequestData).then(result => {
                    // TODO: Add pagination to the groups list
                    if (result.data.UserGroups?.length) {
                        setItems(result.data.UserGroups?.map(group => {
                            return group.UserGroupId!
                        }))
                    }
                })
            }
        }
        setLoading(false)
    }, [])

    return <AttributeEditor
            onAddButtonClick={() => setItems([...items, ""])}
            onRemoveButtonClick={({
                                      detail: { itemIndex }
                                  }) => {
                const tmpItems = [...items];
                tmpItems.splice(itemIndex, 1);
                setItems(tmpItems);
            }}
            isItemRemovable={() => {
                return items.length != 1;
            }}
            items={items}
            addButtonText={isUsers ? SESSION_TEMPLATES_CREATE_CONSTANTS.ADD_USER_TEXT : SESSION_TEMPLATES_CREATE_CONSTANTS.ADD_GROUP_TEXT}
            removeButtonText={SESSION_TEMPLATES_CREATE_CONSTANTS.REMOVE_TEXT}
            definition={[
                {
                    label: isUsers ? SESSION_TEMPLATES_CREATE_CONSTANTS.USER : SESSION_TEMPLATES_CREATE_CONSTANTS.GROUP,
                    control: (item, itemIndex) => (
                        <Input
                            value={item}
                            placeholder={isUsers ? SESSION_TEMPLATES_CREATE_CONSTANTS.USERS_PLACEHOLDER : SESSION_TEMPLATES_CREATE_CONSTANTS.GROUPS_PLACEHOLDER}
                            onChange={({ detail }) => {
                                if(!detail.value || detail.value.trim()) {
                                    let change = [...items]
                                    change[itemIndex] = detail.value
                                    setItems(change)
                                }
                            }}
                            inputMode="search"
                            type="search" //Search needs to be implemented once describeUsers is available
                        />
                    )
                }
            ]}
        />
}