package com.rxlogix.mapping

class Country implements Serializable{
    Long countryId
    Long langId
    String name
    String iso2
    String iso3

    static constraints = {
        name nullable: true
        iso2 nullable: true
        iso3 nullable: true
    }

    static mapping = {
        datasource "pva"
        table "VW_COUNTRIES_DSP"

        cache: "read-only"
        version false

        countryId column: "COUNTRY_ID"
        name column: "COUNTRY"
        iso2 column: "A2"
        iso3 column: "A3"
        langId column: "LANG_ID"
        id composite: ['countryId', 'langId']
    }

    static Map<String, String> getNameIso2Map() {
        Country.withNewSession {
            Map<String, String> result = [:]
            Country.findAll().each {
                if (it.name && it.iso2) {
                    result[it.name.toLowerCase()] = it.iso2.toLowerCase()
                }
            }
            return result
        }
    }
}
