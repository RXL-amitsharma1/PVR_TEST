package com.rxlogix.helper

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat

class LocaleHelper {

    static List<Locale> getSupportedLocales() {
        return [new Locale("en"), new Locale("ja")]
    }

    static buildLocaleSelectList(Boolean excludeEnglish = false) {
        return getSupportedLocales().findAll { availableLocale ->
            !(excludeEnglish && availableLocale.language == "en")
        }.collect { availableLocale ->
            return convertLocaleToMap(availableLocale)
        }
    }

    static convertLocaleToMap(Locale locale) {
        def localeMap = [:]
        localeMap.put('lang_code', locale.toString())
        if (locale.toString().find(/_/)) {
            localeMap.put('display', locale.getDisplayLanguage(locale) + " (" + locale.getDisplayCountry(locale) + ")")
        } else {
            localeMap.put('display', locale.getDisplayLanguage(locale))
        }
        return localeMap
    }

    static buildLocaleListAsPerUserLocale(String userLocale) {
        List<Locale> localeList = getSupportedLocales()
        Locale currentLocale = new Locale(userLocale)
        return localeList.collectEntries { availableLocale ->
            [availableLocale.toLanguageTag(), availableLocale.getDisplayLanguage(currentLocale)]
        }
    }

    static buildLocaleSelectListAsPerUserLocale(String userLocale, Boolean excludeEnglish = false) {
        List<Locale> localeList = getSupportedLocales().findAll { availableLocale ->
            !(excludeEnglish && availableLocale.language == "en")
        }
        Locale currentLocale = new Locale(userLocale)
        return localeList.collect{locale ->
            return convertLocaleToMap(currentLocale)
        }
    }
}
