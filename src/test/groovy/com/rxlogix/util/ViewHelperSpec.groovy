package com.rxlogix.util

import com.rxlogix.config.BaseConfiguration
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.PeriodicReportConfiguration
import com.rxlogix.enums.AssignedToFilterEnum
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.pvdictionary.config.DictionaryConfig
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import grails.plugin.springsecurity.SpringSecurityUtils
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([SpringSecurityUtils, ViewHelper])
class ViewHelperSpec extends Specification {

    @Unroll
    def "getDictionaryValues for different type of Configuration objects"() {
        setup:
        PVDictionaryConfig.setProductConfig(new DictionaryConfig(levels: ['Ingredient', 'Family', 'Product Generic Name', 'Trade Name']))
        PVDictionaryConfig.setEventConfig(new DictionaryConfig(levels: ['SOC', 'HLGT', 'HLT', 'PT', 'LLT', 'Synonyms', 'SMQ Broad', 'SMQ Narrow']))
        PVDictionaryConfig.setStudyConfig(new DictionaryConfig(levels: ['Project Number', 'Study Number', 'Center']))

        // Initialize mock configuration object as needed
        Configuration productConfig = new Configuration(productSelection: '{"1":[{"name":"INBRX 106","id":"10477564"}],"2":[],"3":[],"4":[]}')

        expect:
        ViewHelper.getDictionaryValues((BaseConfiguration) configurationObj, (DictionaryTypeEnum) dictionaryType) == result

        where:
        configurationObj = productConfig  // Assign mock configuration object here
        dictionaryType   || result
        DictionaryTypeEnum.EVENT | '5q-syndrome (LLT)'
        DictionaryTypeEnum.STUDY | ''
        DictionaryTypeEnum.PRODUCT | 'INBRX 106 (Ingredient)'
        DictionaryTypeEnum.PRODUCT | ''
    }

}
