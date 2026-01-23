// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.utils;

import handler.exceptions.BadRequestException;
import handler.model.SortToken;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Component
public class Sort<T, U> {
    private static final String USER_ID_KEY = "UserId";
    private static final String LOGIN_USERNAME_PROPERTY = "loginUsername";
    private static final String USER_ID_PROPERTY = "userId";

    @AllArgsConstructor
    private class ValueComparator implements Comparator<U> {
        private SortToken sortToken;
        @Override
        public int compare(U o1, U o2) {
            Object propertyValue1;
            Object propertyValue2;
            
            if (USER_ID_KEY.equals(sortToken.getKey())) {
                propertyValue1 = getLoginUsernameOrUserId(o1);
                propertyValue2 = getLoginUsernameOrUserId(o2);
            } else {
                propertyValue1 = PropertyAccessorFactory.forBeanPropertyAccess(o1).getPropertyValue(sortToken.getKey());
                propertyValue2 = PropertyAccessorFactory.forBeanPropertyAccess(o2).getPropertyValue(sortToken.getKey());
            }
            
            if(propertyValue1 == null && propertyValue2 == null) {
                return 0;
            }
            if(propertyValue1 == null) {
                return -1;
            }
            if(propertyValue2 == null) {
                return 1;
            }
            if(propertyValue1 instanceof String value1) {
                return value1.compareTo((String) propertyValue2);
            }
            if(propertyValue1 instanceof Long value1) {
                return value1.compareTo((Long) propertyValue2);
            }
            if(propertyValue1 instanceof OffsetDateTime value1) {
                return value1.compareTo((OffsetDateTime) propertyValue2);
            }
            if(propertyValue1 instanceof Boolean value1) {
                return value1.compareTo((Boolean) propertyValue2);
            }
            throw new UnsupportedOperationException("Failed to sort: Sorting not defined for " + propertyValue1.getClass());
        }
        
        private Object getLoginUsernameOrUserId(U obj) {
            Object loginUsername = PropertyAccessorFactory.forBeanPropertyAccess(obj).getPropertyValue(LOGIN_USERNAME_PROPERTY);
            if (loginUsername != null) return loginUsername;
            return PropertyAccessorFactory.forBeanPropertyAccess(obj).getPropertyValue(USER_ID_PROPERTY);
        }
    }

    public List<U> getSorted(T request, List<U> list) {
        try {
            SortToken token;
            try {
                token = (SortToken) PropertyAccessorFactory.forBeanPropertyAccess(request).getPropertyValue("sortToken");
            }
            catch(Exception e) {
                throw new UnsupportedOperationException("Failed to sort: SortToken not defined in " + request.getClass());
            }

            if(token != null) {
                if(token.getOperator() == SortToken.OperatorEnum.ASC) {
                    list.sort(new ValueComparator(token));
                }
                else if(token.getOperator() == SortToken.OperatorEnum.DESC) {
                    list.sort(new ValueComparator(token).reversed());
                }
                else {
                    throw new RuntimeException("Failed to sort: SortToken Operator is invalid/null");
                }
            }
            return list;
        }
        catch(Exception e) {
            throw new BadRequestException(e);
        }
    }
}