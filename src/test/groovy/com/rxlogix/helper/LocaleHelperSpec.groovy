package com.rxlogix.helper

import spock.lang.Specification


class LocaleHelperSpec extends Specification {

    private static final ENGLISH_LANGUAGE_CODE = "en"
    private static final ENGLISH_LANGAUGE_DISPLAY_NAME = "English"

    def "Test getSupportedLocales"() {
        when:
        List<Locale> localeList = LocaleHelper.getSupportedLocales()

        then:
        // Guaranteed one entry in English
        assert localeList.find { it.language == ENGLISH_LANGUAGE_CODE }
    }


    def "Test buildLocaleSelectList"() {
        when:
        def list = LocaleHelper.buildLocaleSelectList()

        then:
        //Guaranteed one entry in English
        list[0].lang_code == ENGLISH_LANGUAGE_CODE
        list[0].display == ENGLISH_LANGAUGE_DISPLAY_NAME
    }

    def "Test convertLocaleToMap"() {
        when:
        def map = LocaleHelper.convertLocaleToMap(new Locale(ENGLISH_LANGUAGE_CODE))

        then:
        //Guaranteed one entry in English
        map.lang_code == ENGLISH_LANGUAGE_CODE
        map.display == ENGLISH_LANGAUGE_DISPLAY_NAME

    }
}
