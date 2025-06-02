package com.rxlogix.config

import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.util.DateUtil
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([GrailsHibernateUtil])
class BaseDateRangeInformationSpec extends Specification implements DataTest, ControllerUnitTest<BaseDateRangeInformation> {

    def setup() {
        grailsApplication.config.pvreports.cumulative.startDate = "01-Jan-0001" //default Cumulative start date
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains DateRangeInformation, BaseDateRangeInformation, TemplateQuery
        GrailsHibernateUtil.metaClass.static.unwrapIfProxy = { Object instance -> return instance}
    }

    void "test hasSameRange"(){
        BaseDateRangeInformation baseDateRangeInformation = new DateRangeInformation(dateRangeStartAbsolute: dateRangeStartAbsolute,dateRangeEndAbsolute: dateRangeEndAbsolute,dateRangeEnum: dateRangeEnum,relativeDateRangeValue:relativeDateRangeValue)
        baseDateRangeInformation.save(failOnError:true,validate:false)
        when:
        result = baseDateRangeInformation.hasSameRange(new DateRangeInformation(dateRangeEnum: dateRangeEnumComp,dateRangeStartAbsolute: dateRangeStartAbsoluteComp,dateRangeEndAbsolute: dateRangeEndAbsoluteComp,relativeDateRangeValue: relativeDateRangeValueComp))
        then:
        result.class == java.lang.Boolean
        where:
        dateRangeStartAbsolute |     dateRangeEndAbsolute |        dateRangeEnum            | relativeDateRangeValue|     dateRangeStartAbsoluteComp   |   dateRangeEndAbsoluteComp |        dateRangeEnumComp         | relativeDateRangeValueComp ||  result
            new Date()         |       new Date()+30      |     DateRangeEnum.CUMULATIVE    |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.YESTERDAY      |             1              ||  false
            new Date()         |       new Date()+31      |     DateRangeEnum.CUMULATIVE    |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.CUMULATIVE     |             1              ||  true
            new Date()         |       new Date()+32      |     DateRangeEnum.YESTERDAY     |         2             |          new Date()              |      new Date()+30         |     DateRangeEnum.YESTERDAY      |             1              ||  true
            new Date()         |       new Date()+33      |     DateRangeEnum.LAST_WEEK     |         3             |          new Date()              |      new Date()+30         |     DateRangeEnum.LAST_WEEK      |             1              ||  true
            new Date()         |       new Date()+34      |     DateRangeEnum.LAST_MONTH    |         4             |          new Date()              |      new Date()+30         |     DateRangeEnum.LAST_MONTH     |             1              ||  true
            new Date()         |       new Date()+35      |     DateRangeEnum.LAST_YEAR     |         5             |          new Date()              |      new Date()+30         |     DateRangeEnum.LAST_YEAR      |             1              ||  true
            new Date()         |       new Date()+36      |     DateRangeEnum.TOMORROW      |         6             |          new Date()              |      new Date()+30         |     DateRangeEnum.TOMORROW       |             1              ||  true
            new Date()+2       |       new Date()+30      |     DateRangeEnum.CUSTOM        |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.CUSTOM         |             1              ||  false
            new Date()         |       new Date()+37      |     DateRangeEnum.NEXT_WEEK     |         7             |          new Date()              |      new Date()+30         |     DateRangeEnum.NEXT_WEEK      |             1              ||  true
            new Date()         |       new Date()+38      |     DateRangeEnum.NEXT_MONTH    |         8             |          new Date()              |      new Date()+30         |     DateRangeEnum.NEXT_MONTH     |             1              ||  true
            new Date()         |       new Date()+39      |     DateRangeEnum.NEXT_YEAR     |         10            |          new Date()              |      new Date()+30         |     DateRangeEnum.NEXT_YEAR      |             1              ||  true
            new Date()         |       new Date()+30      |     DateRangeEnum.LAST_X_DAYS   |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.LAST_X_DAYS    |             2              ||  false
            new Date()         |       new Date()+30      |     DateRangeEnum.LAST_X_WEEKS  |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.LAST_X_WEEKS   |             2              ||  false
            new Date()         |       new Date()+30      |     DateRangeEnum.LAST_X_MONTHS |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.LAST_X_MONTHS  |             2              ||  false
            new Date()         |       new Date()+30      |     DateRangeEnum.LAST_X_YEARS  |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.LAST_X_YEARS   |             2              ||  false
            new Date()         |       new Date()+30      |     DateRangeEnum.NEXT_X_DAYS   |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.NEXT_X_DAYS    |             2              ||  false
            new Date()         |       new Date()+30      |     DateRangeEnum.NEXT_X_WEEKS  |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.NEXT_X_WEEKS   |             2              ||  false
            new Date()         |       new Date()+30      |     DateRangeEnum.NEXT_X_MONTHS |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.NEXT_X_MONTHS  |             2              ||  false
            new Date()         |       new Date()+30      |     DateRangeEnum.NEXT_X_YEARS  |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.NEXT_X_YEARS   |             2              ||  false
            new Date()         |       new Date()+30      |     DateRangeEnum.LAST_X_DAYS   |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.LAST_X_DAYS    |             1              ||  true
            new Date()         |       new Date()+30      |     DateRangeEnum.LAST_X_WEEKS  |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.LAST_X_WEEKS   |             1              ||  true
            new Date()         |       new Date()+30      |     DateRangeEnum.LAST_X_MONTHS |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.LAST_X_MONTHS  |             1              ||  true
            new Date()         |       new Date()+30      |     DateRangeEnum.LAST_X_YEARS  |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.LAST_X_YEARS   |             1              ||  true
            new Date()         |       new Date()+30      |     DateRangeEnum.NEXT_X_DAYS   |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.NEXT_X_DAYS    |             1              ||  true
            new Date()         |       new Date()+30      |     DateRangeEnum.NEXT_X_WEEKS  |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.NEXT_X_WEEKS   |             1              ||  true
            new Date()         |       new Date()+30      |     DateRangeEnum.NEXT_X_MONTHS |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.NEXT_X_MONTHS  |             1              ||  true
            new Date()         |       new Date()+30      |     DateRangeEnum.NEXT_X_YEARS  |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.NEXT_X_YEARS   |             1              ||  true
            new Date()         |       new Date()+30      |     DateRangeEnum.CUSTOM        |         2             |          new Date()              |      new Date()+30         |     DateRangeEnum.CUSTOM         |             1              ||  true
            new Date()         |       new Date()+32      |     DateRangeEnum.CUSTOM        |         1             |          new Date()              |      new Date()+30         |     DateRangeEnum.CUSTOM         |             1              ||  false
    }
    void "test default cumulative daterange"(){
        given:
        BaseDateRangeInformation baseDateRangeInformation = new DateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date(),dateRangeEnum: DateRangeEnum.CUMULATIVE, relativeDateRangeValue:1)
        baseDateRangeInformation.save(failOnError:true,validate:false,flush : true)
        TemplateQuery tq = new TemplateQuery(dateRangeInformationForTemplateQuery:baseDateRangeInformation )
        tq.save(flush : true, validate:false)
        Date startDate = new Date().parse(DateUtil.DATEPICKER_FORMAT, Holders.config.pvreports.cumulative.startDate)
        when:
        def list = baseDateRangeInformation.getReportStartAndEndDateForDate(new Date())
        def list1 = [startDate,new Date()]
        then:
        list[0].getDate() == list1[0].getDate()
        list[0].getMonth() == list1[0].getMonth()
        list[0].getYear() == list1[0].getYear()
        list[1].getDate() == list1[1].getDate()
        list[1].getMonth() == list1[1].getMonth()
        list[1].getYear() == list1[1].getYear()

    }
}
