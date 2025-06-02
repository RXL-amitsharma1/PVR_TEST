package com.rxlogix.util

class FilterTranslatableUtil {
    static translatableFiltersMap = [
            'icsrTrackingFollowupInfo': [
                    'originalName': 'followupInfo',
                    'valuesMap': [
                            'Initial': 'app.followupInfo.Initial',
                            'Nullification': 'app.followupInfo.Nullification',
                            'Followup': 'app.followupInfo.Followup',
                            'Amendment': 'app.followupInfo.Amendment',
                            'Unknown': 'app.followupInfo.Unknown',
                            'Downgrade': 'app.followupInfo.Downgrade'
                    ]
            ],
            'icsrTrackingAuthorizationType': [
                    'originalName': 'authorizationType',
                    'valuesMap': [
                            'Investigational Drug': 'app.authorizationType.investigational_drug',
                            'Marketed Drug': 'app.authorizationType.marketed_drug',
                            'Investigational Device': 'app.authorizationType.investigational_device',
                            'Marketed Device': 'app.authorizationType.marketed_device',
                            'Investigational Vaccine': 'app.authorizationType.investigational_vaccine',
                            'Marketed Vaccine': 'app.authorizationType.marketed_vaccine',
                            'Investigational Biologic': 'app.authorizationType.investigational_biologic',
                            'Marketed Biologic': 'app.authorizationType.marketed_biologic'
                    ]
            ]
    ]

    static boolean isFilterTranslatable(String key) {
        return translatableFiltersMap.containsKey(key)
    }

    static List<Closure> filterByTranslatable(String key, String type, String value) {
        if (!key || !value || !translatableFiltersMap.get(key)) {
            return []
        }

        switch (type) {
            case 'text':
                return filterByText(key, value)
            case 'multi-value-text':
                return filterByMultiValueText(key, value)
            default:
                return []
        }
    }

    static List<Closure> filterByText(String key, String value) {
        List<Closure> closureList = []

        List<String> filterValues = getFilterValuesByInputValue(key, value)
        String dataKey = translatableFiltersMap.get(key)?.get('originalName')

        closureList.add {
            'in'(dataKey, filterValues)
        }

        return closureList
    }

    static List<Closure> filterByMultiValueText(String key, String value) {
        List<Closure> closureList = []

        Set<String> filterValues = new HashSet<>()
        value.split(",")*.trim().findAll { val ->
            filterValues.addAll(getFilterValuesByInputValue(key, val))
        }

        String dataKey = translatableFiltersMap.get(key)?.get('originalName')

        closureList.add {
            'in'(dataKey, filterValues.toList())
        }

        return closureList
    }

    static List<String> getFilterValuesByInputValue(String filterKey, String inputValue) {
        Map filterValuesMap = translatableFiltersMap.get(filterKey)?.get('valuesMap')
        return filterValuesMap.findAll { key, value -> ViewHelper.getMessage(value)?.toLowerCase()?.contains(inputValue?.trim()?.toLowerCase()) }
                .collect {it.key}
    }
}
