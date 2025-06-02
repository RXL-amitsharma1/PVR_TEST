package com.rxlogix

import com.rxlogix.config.ActionItem
import com.rxlogix.config.CaseSeries
import com.rxlogix.config.ExecutedCaseSeries
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.config.ReportRequest
import com.rxlogix.config.SchedulerConfigParams
import com.rxlogix.user.User
import groovyx.gpars.GParsPool
import grails.gorm.transactions.Transactional
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Transactional(readOnly = true)
class CalendarService {

    def configurationService
    def userService
    def CRUDService

    /**
     * The method to render the events.
     * @return event json.
     */
    List<CalendarEventDTO> events(User user, Date start, Date end) {
        List<CalendarEventDTO> calendarEventDTOs = []
        calendarEventDTOs.addAll(getExecutedReportsEvents(user, start, end))
        calendarEventDTOs.addAll(getActionItemsEvents(user, start, end))
        calendarEventDTOs.addAll(getReportRequestsEvents(user, start, end))
        calendarEventDTOs.addAll(getReportConfigurationsEvents(user, start, end))
        calendarEventDTOs.addAll(getExecutedCaseSeriesEvents(user, start, end))
        calendarEventDTOs.addAll(getCaseSeriesEvents(user, start, end))
        return calendarEventDTOs
    }

    private List<CalendarEventDTO> getExecutedReportsEvents(User user, Date startDate, Date endDate) {
        List reportConfigIds = ExecutedReportConfiguration.getAllExecutedReportIdByReportTypeAndUserBetweenDates(user, startDate, endDate).list()
        List<ExecutedReportConfiguration> reportConfigs = []
        reportConfigIds.collate(999).each { chunk ->
            reportConfigs.addAll(ExecutedReportConfiguration.createCriteria().list {
                'in'('id', chunk)
            })
        }
        List<CalendarEventDTO> events = []
        GParsPool.withPool(4) {
            events = reportConfigs.collectParallel { config ->
                CalendarEventDTO.getEvent(config)
            }
        }
        return events
    }

    private List<CalendarEventDTO> getExecutedCaseSeriesEvents(User user, Date startDate, Date endDate) {
        List<CalendarEventDTO> events = []
        List caseSeriesIds = ExecutedCaseSeries.getAllExecutedCaseSeriesIdByUserAndBetweenDates(user, startDate, endDate).list()
        List<ExecutedCaseSeries> caseSeriesList = []
        caseSeriesIds.collate(999).each { chunk ->
            caseSeriesList.addAll(ExecutedCaseSeries.createCriteria().list {
                'in'('id', chunk)
            })
        }
        GParsPool.withPool(4) {
            events = caseSeriesList.collectParallel { config ->
                CalendarEventDTO.getEvent(config)
            }
        }
        return events
    }


    private List<CalendarEventDTO> getActionItemsEvents(User user, Date startDate, Date endDate) {
        List<CalendarEventDTO> events = []
        ActionItem.getActionItemsForUserBetweenDates(user, startDate, endDate).list().each {
            events.add(CalendarEventDTO.getEvent(it))
        }
        return events
    }

    private List<CalendarEventDTO> getReportRequestsEvents(User user, Date startDate, Date endDate) {
        List<CalendarEventDTO> events = []
        ReportRequest.getReportRequestsForUserBetweenDates(user, startDate, endDate).list().each {
            events.add(CalendarEventDTO.getEvent(it))
        }
        return events
    }


    private List<CalendarEventDTO> getReportConfigurationsEvents(User user, Date startDate, Date endDate) {
        List<CalendarEventDTO> events = []
        ReportConfiguration.getAllScheduledReportForUserForStartDateAndEndDate(user, new Date(), endDate).listDistinct().each { ReportConfiguration config ->
//            TODO need to check if we really need this becuase without this its skipping one date.
            ReportConfiguration reportConfiguration = GrailsHibernateUtil.unwrapIfProxy(config)
            List<Date> futureDates = configurationService.getFutureRunDates(reportConfiguration, new Date(), endDate)?.findAll {
                it >= startDate && it <= endDate
            }
            if(futureDates){
                GParsPool.withPool(4) {
                    events.addAll(futureDates.collectParallel { date ->
                        CalendarEventDTO.getEvent(reportConfiguration, date)
                    })
                }
            }
        }
        return events
    }

    private List<CalendarEventDTO> getCaseSeriesEvents(User user, Date startDate, Date endDate) {
        List<CalendarEventDTO> events = []
        CaseSeries.getAllScheduledCaseSeriesForUserAndStartDateAndEndDate(user, new Date(), endDate).listDistinct().each { CaseSeries series ->
            CaseSeries caseSeries = GrailsHibernateUtil.unwrapIfProxy(series)
            configurationService.getFutureRunDates(caseSeries, new Date(), endDate)?.findAll {
                it >= startDate && it <= endDate
            }?.each {
                events.add(CalendarEventDTO.getEvent(caseSeries, it))
            }
        }
        return events
    }

    SchedulerConfigParams getSchedulerConfigParams(def id, Boolean createNew = false) {
        if (id?.contains("_")) {
            String[] parts = id.tokenize("_")
            Date date = new Date(parts[1] as Long)
            ReportConfiguration configuration = ReportConfiguration.get(parts[0] as Long)
            SchedulerConfigParams configParams = SchedulerConfigParams.findByConfigurationAndRunDateAndIsDeleted(configuration, date, false)
            if (!configParams && createNew) {
                configParams = new SchedulerConfigParams(configuration: configuration, runDate: date)
                CRUDService.save(configParams)
            }
            return configParams
        }
        return SchedulerConfigParams.get(id as Long)
    }

}
