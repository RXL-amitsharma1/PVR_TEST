package com.rxlogix.commandObjects

import com.rxlogix.Constants
import com.rxlogix.spotfire.SpotfireService
import grails.validation.Validateable
import org.springframework.beans.factory.annotation.Autowired

class SpotfireCommand implements Validateable{
    static final Date defaultDate =  Date.parse("MM/DD/YYYY","01/01/1800") // when existing casesId used
    static final Long defaultCaseSeriesId = -1

    String productFamilyIds
    Date fromDate
    Date endDate
    Date asOfDate
    String type
    String fullFileName
    Long caseSeriesId

    @Autowired
    SpotfireService spotfireService

    Set<String> getAllProductFamilyIds(){
        return productFamilyIds?.split(Constants.MULTIPLE_AJAX_SEPARATOR)?:[]
    }

    static constraints = {
        productFamilyIds(nullable: false)
        fromDate(nullable: false, blank: false, validator: {value, obj ->
            if(obj.caseSeriesId){
                return true
            }
        })

        endDate(nullable: false, blank: false, validator: { value, obj ->
            if(obj.caseSeriesId){
                return true
            }

            if (value < obj.fromDate) {
                return "com.rxlogix.commandObjects.SpotfireCommand.endDate.before.fromDate"
            }
        })

        asOfDate(nullable: false, blank: false, validator: { value, obj ->
            if (obj.caseSeriesId) {
                return true
            }
        })

        fullFileName nullable: false, blank: false, validator: { value, obj ->
            if (obj.spotfireService.invalidFileNameLength(value)) {
                return ["tooLong"]
            }
            if (!(value ==~ /^[a-zA-Z0-9]+((-|_|\s)+[a-zA-Z0-9]+)*$/)) {
//                TODO We need to work for Japanese names validator
//                We are allowing only Alphabets of English, Numeric Numbers, Underscores(in between of name), dashes(in between of name), spaces(in between of name) only. Start should be with Alphabet or Numeric and end should be with Alphabet or Numeric
                return ["invalid"]
            }
            if (obj.spotfireService.fileNameExist(value)) {
                return ["duplicated"]
            }
        }
        caseSeriesId nullable: true
    }

    Date getFromDate() {
        return caseSeriesId ? defaultDate : fromDate
    }

    Date getEndDate() {
        return caseSeriesId ? defaultDate : endDate
    }

    Date getAsOfDate() {
        return caseSeriesId ? defaultDate : asOfDate
    }

    Long getCaseSeriesId() {
        return caseSeriesId ?: defaultCaseSeriesId
    }
}
