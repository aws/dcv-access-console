import {PropertyFilterProps} from "@cloudscape-design/components/property-filter/interfaces";

export const PROPERTY_FILTER_I18N_STRINGS: PropertyFilterProps.I18nStrings = {
    clearFiltersText: "Clear filters",
    operationAndText: "and",
    operationOrText: "or",
    dismissAriaLabel: "Dismiss",
    cancelActionText: "Cancel",
    applyActionText: "Apply",

    clearAriaLabel: 'Clear',

    groupValuesText: 'Values',
    groupPropertiesText: 'Properties',
    operatorsText: 'Operators',

    operatorLessText: 'Less than',
    operatorLessOrEqualText: 'Less than or equal',
    operatorGreaterText: 'Greater than',
    operatorGreaterOrEqualText: 'Greater than or equal',
    operatorContainsText: 'Contains',
    operatorDoesNotContainText: 'Does not contain',
    operatorEqualsText: 'Equals',
    operatorDoesNotEqualText: 'Does not equal',

    editTokenHeader: 'Edit filter',
    propertyText: 'Property',
    operatorText: 'Operator',
    valueText: 'Value',
    allPropertiesLabel: 'All properties',

    tokenLimitShowMore: 'Show more',
    tokenLimitShowFewer: 'Show fewer',
    removeTokenButtonAriaLabel: token => `Remove token ${token.propertyKey} ${token.operator} ${token.value}`,
    enteredTextLabel: text => `Use: "${text}"`,
}

export const FILTER_CONSTANTS = {
    filteringLoadingText: "Loading suggestions",
    filteringErrorText: "Error fetching suggestions.",
    filteringRecoveryText: "Retry",
    filteringEmpty: "No suggestions found",
    filteringFinishedText: "Finished loading suggestions"
}