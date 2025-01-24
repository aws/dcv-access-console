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
    @AllArgsConstructor
    private class ValueComparator implements Comparator<U> {
        private SortToken sortToken;
        @Override
        public int compare(U o1, U o2) {
            Object propertyValue1 = PropertyAccessorFactory.forBeanPropertyAccess(o1).getPropertyValue(sortToken.getKey());
            Object propertyValue2 = PropertyAccessorFactory.forBeanPropertyAccess(o2).getPropertyValue(sortToken.getKey());
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