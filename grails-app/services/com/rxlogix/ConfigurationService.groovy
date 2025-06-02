package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dictionary.DictionaryGroup
import com.rxlogix.enums.*
import com.rxlogix.mapping.LmIngredient
import com.rxlogix.mapping.LmLicense
import com.rxlogix.mapping.LmProduct
import com.rxlogix.mapping.LmProductFamily
import com.rxlogix.mapping.SafetyCalendar
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.pvdictionary.product.view.LmProdDic200
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import grails.gorm.transactions.NotTransactional
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.time.TimeCategory
import net.fortuna.ical4j.model.*
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.DtStart
import net.fortuna.ical4j.model.property.Duration
import net.fortuna.ical4j.model.property.RRule
import net.fortuna.ical4j.model.property.Summary
import net.fortuna.ical4j.transform.recurrence.Frequency
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.json.JSONObject
import org.joda.time.Interval
import org.joda.time.PeriodType
import org.springframework.validation.FieldError
import org.hibernate.SessionFactory

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.Date
import java.sql.Clob
import java.sql.Connection
import java.sql.ResultSet
import grails.converters.JSON
import java.sql.Timestamp
import groovy.sql.GroovyResultSet
import groovy.sql.Sql

@Transactional
class ConfigurationService {


    def queryService
    def customMessageService
    def springSecurityService
    def CRUDService
    def userService
    def templateService
    def periodicReportService
    def reportExecutorService
    def sessionFactory
    def productDictionaryService
    UtilService utilService


    def getSetOperatorI18n() {
        return SetOperatorEnum.values().collect {
            [name: it, display: (customMessageService.getMessage(it?.getI18nKey()))]
        }
    }


    // required for PVR-11586 : Report Scheduler should not kick off from the Start Date, but from the Next Scheduled Run Date
    @ReadOnly
    String correctSchedulerJSONForCurrentDate(String scheduleDateJSON, Date nextRunDate) {
        if (scheduleDateJSON) {
            JSONObject parsedScheduleDateJSON = JSON.parse(scheduleDateJSON)
            Date startDateTime = DateUtil.parseDate(parsedScheduleDateJSON.startDateTime, DateUtil.JSON_DATE_WITHOUT_OFFSET, parsedScheduleDateJSON.timeZone.name)
            DateTime currentDate = new DateTime()
            String country = parsedScheduleDateJSON.timeZone.name
            def zone = TimeZone.getTimeZone(country)
            if (currentDate > startDateTime && nextRunDate == null) {
                parsedScheduleDateJSON.startDateTime = currentDate.format(DateUtil.JSON_DATE, zone)

            } else if (nextRunDate != null) {
                parsedScheduleDateJSON.startDateTime = nextRunDate.format(DateUtil.JSON_DATE, zone)
            }
            scheduleDateJSON = new groovy.json.JsonOutput().toJson(parsedScheduleDateJSON)
        }
        return scheduleDateJSON
    }

    ReportConfiguration copyConfig(Configuration configuration, User owner, String namePrefix = null, Long tenantId = null, boolean isCreateFromTemplate = false) {
        User currentUser = userService.getUser()
        String scheduleDateJSON = getCopiedConfigurationScheduledDateJson(configuration) ?: configuration.scheduleDateJSON
        DeliveryOption deliveryOption = new DeliveryOption(sharedWith: [currentUser],executableBy: [currentUser],
                emailToUsers: configuration.deliveryOption.emailToUsers, additionalAttachments:configuration.deliveryOption.additionalAttachments,
                attachmentFormats: configuration.deliveryOption.attachmentFormats)

        Configuration newConfig = new Configuration(reportName: generateUniqueName(configuration, owner, namePrefix),
                owner: owner, scheduleDateJSON: scheduleDateJSON, nextRunDate: null,
                description: configuration.description, qbeForm: configuration.qbeForm, pvqType: configuration.pvqType,
                isDeleted: configuration.isDeleted, isEnabled: false,
                tags: configuration.tags, dateRangeType: configuration.dateRangeType, sourceProfile: configuration.sourceProfile,
                productSelection: configuration.productSelection, studySelection: configuration.studySelection, eventSelection: configuration.eventSelection, productGroupSelection: configuration.productGroupSelection, eventGroupSelection: configuration.eventGroupSelection,
                configSelectedTimeZone: configuration.configSelectedTimeZone,
                asOfVersionDate: configuration.asOfVersionDate, evaluateDateAs: configuration.evaluateDateAs,
                excludeFollowUp: configuration.excludeFollowUp, includeLockedVersion: configuration.includeLockedVersion, includeAllStudyDrugsCases: configuration.includeAllStudyDrugsCases,
                adjustPerScheduleFrequency: configuration.adjustPerScheduleFrequency,
                globalQuery: configuration.globalQuery,
                suspectProduct: configuration.suspectProduct,
                deliveryOption: deliveryOption,
                createdBy: currentUser.username,
                modifiedBy: currentUser.username, excludeNonValidCases: configuration?.excludeNonValidCases, excludeDeletedCases: configuration?.excludeDeletedCases,
                limitPrimaryPath: configuration?.limitPrimaryPath, blankValuesJSON: configuration?.blankValuesJSON,
                includeMedicallyConfirmedCases: configuration?.includeMedicallyConfirmedCases,
                emailConfiguration: (configuration.emailConfiguration && !configuration.emailConfiguration?.isDeleted) ? (EmailConfiguration) CRUDService.save(new EmailConfiguration(configuration.emailConfiguration.properties)) : null,
                dmsConfiguration: (configuration.dmsConfiguration && !configuration.dmsConfiguration?.isDeleted) ? (DmsConfiguration) CRUDService.save(new DmsConfiguration(configuration.dmsConfiguration.properties)) : null,
                qualityChecked: false, useCaseSeries: configuration?.useCaseSeries, tenantId: tenantId ?: configuration.tenantId, isMultiIngredient: configuration.isMultiIngredient, includeWHODrugs: configuration.includeWHODrugs)
        if(configuration.isTemplate && isCreateFromTemplate){
            newConfig.configurationTemplate = configuration
        }
        configuration.reportTasks?.each {
            ReportTask reportTask = new ReportTask(MiscUtil.getObjectProperties(it))
            newConfig.addToReportTasks(reportTask)
        }
        newConfig.includeNonSignificantFollowUp = configuration.includeNonSignificantFollowUp
        createGlobalQueryDateRange(configuration, newConfig)
        configuration.templateQueries.each {
            TemplateQuery tq = new TemplateQuery(template: it.template, query: it.query,
                    dateRangeInformationForTemplateQuery: new DateRangeInformation(
                            dateRangeEndAbsolute: it.dateRangeInformationForTemplateQuery.dateRangeEndAbsolute,
                            dateRangeStartAbsolute: it.dateRangeInformationForTemplateQuery.dateRangeStartAbsolute,
                            relativeDateRangeValue: it.dateRangeInformationForTemplateQuery.relativeDateRangeValue,
                            dateRangeEnum: it.dateRangeInformationForTemplateQuery.dateRangeEnum),
                    queryLevel: it.queryLevel, createdBy: currentUser.username,
                    issueType: it.issueType, rootCause: it.rootCause, responsibleParty: it.responsibleParty, assignedToUser: it.assignedToUser, assignedToGroup: it.assignedToGroup, priority: it.priority,
                    investigation: it.investigation, summary: it.summary, actions: it.actions, investigationSql: it.investigationSql, actionsSql: it.actionsSql, summarySql: it.summarySql,
                    modifiedBy: currentUser.username, header: it.header, title: it.title, footer: it.footer,
                    headerProductSelection: it.headerProductSelection, headerDateRange: it.headerDateRange,granularity: it.granularity,
                    reassessListednessDate: it.reassessListednessDate, templtReassessDate: it.templtReassessDate,
                    draftOnly: it.draftOnly, blindProtected: it.blindProtected, privacyProtected: it.privacyProtected, displayMedDraVersionNumber: it.displayMedDraVersionNumber ?: false)

            newConfig.addToTemplateQueries(copyBlankValues(tq, it.queryValueLists, it.templateValueLists))
        }
        CRUDService.save(newConfig)
        return newConfig
    }

    private void createGlobalQueryDateRange(ReportConfiguration configuration, ReportConfiguration newConfig) {
        if (configuration.globalDateRangeInformation) {
            newConfig.globalDateRangeInformation = new GlobalDateRangeInformation(MiscUtil.getObjectProperties(configuration.globalDateRangeInformation, GlobalDateRangeInformation.propertiesToUseForCopying))
            newConfig.globalDateRangeInformation.reportConfiguration = newConfig
        }
        configuration.globalQueryValueLists?.each {
            QueryValueList queryValueList = new QueryValueList(query: it.query)
            it.parameterValues.each {
                if (it.hasProperty('reportField')) {
                    queryValueList.addToParameterValues(new QueryExpressionValue(key: it.key,
                            reportField: it.reportField, operator: it.operator, value: it.value, specialKeyValue: it.specialKeyValue))
                } else {
                    queryValueList.addToParameterValues(new CustomSQLValue(key: it.key,
                            value: it.value))
                }
            }
            newConfig.addToGlobalQueryValueLists(queryValueList)
        }
    }

    String generateUniqueName(Configuration config, User owner) {
        String prefix = ViewHelper.getMessage("app.configuration.copy.of") + " "
        String newName = trimName(prefix, config.reportName, "")
        if (Configuration.countByReportNameIlikeAndOwnerAndIsDeleted(newName, owner, false)) {
            int count = 1
            newName = trimName(prefix, config.reportName, " ($count)")
            while (Configuration.countByReportNameIlikeAndOwnerAndIsDeleted(newName, owner, false)) {
                newName =trimName(prefix,config.reportName," (${++count})")
            }
        }

        return newName
    }

    String generateUniqueName(IcsrProfileConfiguration config, String namePrefix) {
        String prefix = namePrefix == null ? ViewHelper.getMessage("app.configuration.copy.of") + " " : (namePrefix.length() > 0 ? (namePrefix + ' ') : "")
        String newName = trimName(prefix, config.reportName, "")
        if (IcsrProfileConfiguration.countByReportNameIlikeAndIsDeleted(newName, false)) {
            int count = 1
            newName = trimName(prefix, config.reportName, " ($count)")
            while (IcsrProfileConfiguration.countByReportNameIlikeAndIsDeleted(newName,false)) {
                newName =trimName(prefix,config.reportName," (${++count})")
            }
        }

        return newName
    }

    ReportConfiguration copyConfig(PeriodicReportConfiguration configuration, User user, String namePrefix = null, Long tenantId = null, boolean isCreateFromTemplate = false,boolean isBulkImport = false) {
        DeliveryOption deliveryOption = new DeliveryOption(MiscUtil.getObjectProperties(configuration.deliveryOption, DeliveryOption.propertiesToUseWhileCopying))
        deliveryOption.sharedWith = [user]
        PeriodicReportConfiguration newPeriodicReportConfiguration = new PeriodicReportConfiguration(MiscUtil.getObjectProperties(configuration, PeriodicReportConfiguration.propertiesToUseWhileCopying))
        newPeriodicReportConfiguration.isEnabled = false
        newPeriodicReportConfiguration.scheduleDateJSON = getCopiedConfigurationScheduledDateJson(configuration) ?: configuration.scheduleDateJSON
        newPeriodicReportConfiguration.emailConfiguration = (configuration.emailConfiguration && !configuration.emailConfiguration?.isDeleted) ? (EmailConfiguration) CRUDService.save(new EmailConfiguration(configuration.emailConfiguration.properties)) : null
        newPeriodicReportConfiguration.dmsConfiguration = (configuration.dmsConfiguration && !configuration.dmsConfiguration?.isDeleted) ? (DmsConfiguration) CRUDService.save(new DmsConfiguration(configuration.dmsConfiguration.properties)) : null
        newPeriodicReportConfiguration.reportName = generateUniqueName(configuration, user, namePrefix)
        newPeriodicReportConfiguration.owner = user
        newPeriodicReportConfiguration.createdBy = user.username
        newPeriodicReportConfiguration.modifiedBy = user.username
        newPeriodicReportConfiguration.deliveryOption = deliveryOption
        if (tenantId) {
            newPeriodicReportConfiguration.tenantId = tenantId
        }

        if(configuration.isTemplate && isCreateFromTemplate){
            newPeriodicReportConfiguration.configurationTemplate = configuration
        }
        configuration.reportingDestinations?.each {
            newPeriodicReportConfiguration.addToReportingDestinations(it)
        }
        createGlobalQueryDateRange(configuration, newPeriodicReportConfiguration)
        configuration.templateQueries.each {
            TemplateQuery tq = new TemplateQuery(MiscUtil.getObjectProperties(it, TemplateQuery.propertiesToUseWhileCopying))
            tq.createdBy = user.username
            tq.modifiedBy = user.username
            tq.templtReassessDate = it.templtReassessDate
            tq.reassessListednessDate = it.reassessListednessDate
            tq.dateRangeInformationForTemplateQuery = new DateRangeInformation(MiscUtil.getObjectProperties(it.dateRangeInformationForTemplateQuery, DateRangeInformation.propertiesToUseWhileCopying))
            tq.dateRangeInformationForTemplateQuery.templateQuery = tq
            newPeriodicReportConfiguration.addToTemplateQueries(copyBlankValues(tq, it.queryValueLists, it.templateValueLists))
        }
        configuration.reportTasks?.each{
            ReportTask reportTask = new ReportTask(MiscUtil.getObjectProperties(it))
            newPeriodicReportConfiguration.addToReportTasks(reportTask)
        }
        newPeriodicReportConfiguration.isPublisherReport = configuration.isPublisherReport
        if (configuration.publisherContributors)
            newPeriodicReportConfiguration.addToPublisherContributors(configuration.publisherContributors.collect { it })
        newPeriodicReportConfiguration.primaryPublisherContributor = configuration.primaryPublisherContributor
        configuration.publisherConfigurationSections?.each {
            PublisherConfigurationSection publisherConfigurationSection = new PublisherConfigurationSection(
                    name: it.name,
                    taskTemplate: it.taskTemplate,
                    sortNumber: it.sortNumber,
                    templateFileData: it.templateFileData,
                    filename: it.filename,
                    assignedToGroup: it.assignedToGroup,
                    publisherTemplate: it.publisherTemplate,
                    parameterValues: it.parameterValues.collectEntries { k, v -> [k, v] },
                    destination: it.destination,
                    dueDate: it.dueDate,
                    author: it.author,
                    reviewer: it.reviewer,
                    approver: it.approver
            )
            newPeriodicReportConfiguration.addToPublisherConfigurationSections(publisherConfigurationSection)
        }
        configuration.attachments?.each{
            PublisherSource publisherSource = new PublisherSource(it.properties)
            publisherSource.configuration=newPeriodicReportConfiguration
            if(it.oneDriveUserSettings)
                publisherSource.oneDriveUserSettings=new OneDriveUserSettings(it.oneDriveUserSettings.properties)
            newPeriodicReportConfiguration.addToAttachments(publisherSource)
        }
        if(!isBulkImport) {
            CRUDService.save(newPeriodicReportConfiguration)
        }
        return newPeriodicReportConfiguration
    }

    String generateUniqueName(def config, User user, String namePrefix = null) {
        String prefix = namePrefix == null ? ViewHelper.getMessage("app.configuration.copy.of") + " ": (namePrefix.length()>0?(namePrefix+ ' '):"")
        String newName = trimName(prefix,config.reportName,"")
        def configClass = config.getClass()
        if (configClass.countByReportNameIlikeAndOwnerAndIsDeleted(newName, user, false)) {
            int count = 1
            newName = trimName(prefix,config.reportName," ($count)")
            while (configClass.countByReportNameIlikeAndOwnerAndIsDeleted(newName, user, false)) {
                newName =trimName(prefix,config.reportName," (${++count})")
            }
        }
        return newName
    }

    String trimName(String prefix, String name, String postfix) {
        int maxSize = Configuration.constrainedProperties.reportName.maxSize - 2
        int overflow = prefix.length() + name.length() + postfix.length() - maxSize
        if (overflow > 0) {
            return prefix + name.substring(0,name.length() - overflow) + postfix
        }
        return prefix + name + postfix
    }

    TemplateQuery copyBlankValues(TemplateQuery tqnew, List<QueryValueList> queryValueLists, List<TemplateValueList> templateValueLists) {
        queryValueLists.each {
            QueryValueList queryValueList = new QueryValueList(query: it.query)
            it.parameterValues.each {
                if (it.hasProperty('reportField')) {
                    queryValueList.addToParameterValues(new QueryExpressionValue(key: it.key,
                            reportField: it.reportField, operator: it.operator, value: it.value, specialKeyValue: it.specialKeyValue))
                } else {
                    queryValueList.addToParameterValues(new CustomSQLValue(key: it.key,
                            value: it.value))
                }
            }
            tqnew.addToQueryValueLists(queryValueList)
        }

        templateValueLists.each {
            TemplateValueList templateValueList = new TemplateValueList(template: it.template)
            it.parameterValues.each {
                templateValueList.addToParameterValues(new CustomSQLValue(key: it.key,
                        value: it.value))
            }
            tqnew.addToTemplateValueLists(templateValueList)
        }

        return tqnew
    }

    /**
     * Method to fetch the next run date
     * @param config a Configuration object or CaseSeries Object with a scheduleDateJSON field
     * @return a Date in the local timezone or null if there is no next run date
     */
    @NotTransactional
    def getNextDate(def config) {
        return getNextDateValue(config, null)
    }

    /**
     * Method to fetch the next run date object based on the previously run object and the
     * configuration.
     * @param config
     * @param lastRunDateObj
     * @return
     */
    @NotTransactional
    def getNextDateObj(config, lastRunDateObj) {
        return getNextDateValue(config, lastRunDateObj)
    }

    @NotTransactional
    private Date getNextDateValue(config, lastRunDateObj) {
        List<Date> futureDates = getFutureRunDates(config, lastRunDateObj)
        if (futureDates) {
            return futureDates.sort().first()
        }
        return null

    }

    @NotTransactional
    Date getNextDateAsPerScheduler(Date startDate, org.joda.time.Period period) {
        if (!period) {
            return startDate
        }
        Date date = new Date(startDate.time)
        use(TimeCategory) {
            date = date + period.years.years
            date = date + period.months.months
            date = date + period.days.days
        }
        return date
    }

    @NotTransactional
    List<Date> getFutureRunDates(def config, Date lastRunDateObj, Date endDate = null) {
        if (config.scheduleDateJSON && config.isEnabled) {
            JSONObject timeObject = JSON.parse(config.scheduleDateJSON)
            if (timeObject.startDateTime && timeObject.timeZone && timeObject.recurrencePattern) {
                Date now = new Date()
                net.fortuna.ical4j.model.TimeZone timeZone = DateUtil.TZ_REGISTRY.getTimeZone(timeObject.timeZone.name)
                Date startDate = DateUtil.parseDate(timeObject.startDateTime, DateUtil.JSON_DATE_WITHOUT_OFFSET, timeObject.timeZone.name)

                ZonedDateTime from = startDate.toInstant().atZone(timeZone.toZoneId())
                ZonedDateTime to = new Date(Long.MAX_VALUE).toInstant().atZone(timeZone.toZoneId())

                Date lastRunDate = config?.nextRunDate

                //If last run date objaect passed as argument is not null then we set the
                //last run date as the last run date object.
                if (lastRunDateObj) {
                    lastRunDate = lastRunDateObj
                }

                RRule recurRule = new RRule(timeObject.recurrencePattern)

                if (endDate && recurRule.recur.count == -1 && !recurRule.recur.until) {
                    recurRule.recur.setUntil(endDate.toInstant().atZone(timeZone.toZoneId()))
                }

                //Check if the scheduler will never end
                if (recurRule.recur.count == -1 && !recurRule.recur.until) {

                    //We temporarily set the recurrence count to 2 because we only need the next recur date
                    recurRule.recur.setCount(2)
                    if (lastRunDate) {
                        from = lastRunDate.toInstant().atZone(timeZone.toZoneId())
                    }
                }

                //Check if the recurrence is run once/now
                if (recurRule?.recur?.count == 1 && startDate.before(now)) {

                    //Do not return a nextRunDate if we have already run this configuration once
                    if (lastRunDate) {
                        return null
                    }

                    //Run once anytime in the past is generated with today's date
                    from = now.toInstant().atZone(timeZone.toZoneId())
                }

                // PVR-2824, PVR-2909: End on date should include the date it ends on (23:59:59 of the UNTIL date)
                if (recurRule.recur.until && recurRule.recur.frequency) {
                    switch (recurRule.recur.frequency) {
                    // HOURLY also adds an entire day's worth of reports
                        case Frequency.MINUTELY:
                        case Frequency.HOURLY:
                        case Frequency.DAILY:
                        case Frequency.WEEKLY:
                        case Frequency.MONTHLY:
                        case Frequency.YEARLY:
                            if (recurRule.recur.until instanceof LocalDate) {
                                recurRule.recur.until = recurRule.recur.until.atTime(23, 59)
                            } else {
                                recurRule.recur.until = recurRule.recur.until.with(LocalTime.of(23, 59))
                            }
                            break;
                    }
                }
                VEvent event = new VEvent()
                DtStart<ZonedDateTime> dtfrom = new DtStart<ZonedDateTime>(from)
                event.add(dtfrom)
                event.add(new Summary("event"))
                event.add(recurRule)
                if (from.getDayOfMonth() > 27 &&
                        ((recurRule.recur.frequency == Frequency.MONTHLY) ||
                                (recurRule.recur.frequency == Frequency.YEARLY && 2 in recurRule.recur?.monthList*.monthOfYear && -1 in recurRule.recur?.setPosList))) {
                    dtfrom.date = dtfrom.date.withDayOfMonth(27)
                    from = from.withDayOfMonth(27)
                }
                Period<LocalDateTime> period = new Period(from, to)
                Set<Period<ZonedDateTime>> periodList = event.calculateRecurrenceSet(period)

                if (periodList) {
                    if (!lastRunDate) {
                        lastRunDate = startDate - 1
                    }
                    return periodList.collect {
                        Date.from(it.start.toInstant())
                    }.findAll { it.after(lastRunDate) }
                }
            }
        }
        return null
    }

    @NotTransactional
    boolean checkStartDate(Date startDate, net.fortuna.ical4j.model.TimeZone timeZone, RRule originalRule) {
        def interval = originalRule.recur.getInterval()
        String freq = originalRule.recur.getFrequency()
        if (interval < 0) interval = 1
        ZonedDateTime from = startDate.toInstant().atZone(timeZone.toZoneId())
        ZonedDateTime fromBefore
        ZonedDateTime toLater
        switch (freq) {
            case Recur.WEEKLY:
                fromBefore = from.minusWeeks(interval)
                toLater = from.plusWeeks(interval)
                break
            case Recur.MONTHLY:
                fromBefore = from.minusMonths(interval)
                toLater = from.plusMonths(interval)
                break
            case Recur.YEARLY:
                fromBefore = from.minusYears(interval)
                toLater = from.plusYears(interval)
                break
            default:
                fromBefore = from.minusDays(interval)
                toLater = from.plusDays(interval)
                break
        }
        VEvent eventTest = new VEvent()
        DtStart<ZonedDateTime> dtfrom = new DtStart<ZonedDateTime>(fromBefore)
        dtfrom.setTimeZoneRegistry(DateUtil.TZ_REGISTRY)
        eventTest.add(dtfrom)
        eventTest.add(new Summary("event test"))
        RRule ruleTest = originalRule
        ruleTest.recur.setCount(10)
        eventTest.add(ruleTest)
        Period periodTest = new Period(fromBefore, toLater)
        Set testPeriodList = eventTest.calculateRecurrenceSet(periodTest)
        Period start = new Period(from, new Duration(java.time.Duration.ZERO).getDuration())
        if (testPeriodList.contains(start) || originalRule.recur.frequency == "HOURLY" || originalRule.recur.frequency == "MINUTELY") {
            return false
        }
        return true
    }

    def getDateRangeValueForCriteria(ExecutedTemplateQuery templateQuery, Locale locale) {
        if (templateQuery) {
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.getShortDateFormatForLocale(locale))
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
            if (templateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEnum.name() == DateRangeValueEnum.CUMULATIVE.name()) {
                Date endDate = (templateQuery.executedConfiguration instanceof ExecutedPeriodicReportConfiguration) ?
                        templateQuery.getGlobalEndDate() : templateQuery.getMaxEndDate() ?:templateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute
                String dateValue = sdf.format(endDate)
                // gives string date original value
                return customMessageService.getMessage("app.dateRangeType.executed.cumulative", dateValue)
            } else {
                String dateStartValue = sdf.format(templateQuery.executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute)
                String dateEndValue = sdf.format(templateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute)
                Date endDate = templateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute
                if(templateQuery.executedConfiguration.executedGlobalDateRangeInformation?.dateRangeEnum?.name() == DateRangeValueEnum.CUMULATIVE.name()) {
                    if (templateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEnum.name() == DateRangeValueEnum.PR_DATE_RANGE.name() && templateQuery.executedConfiguration instanceof ExecutedConfiguration) {
                        endDate = templateQuery.getMaxEndDate() ?: templateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute
                        dateEndValue = sdf.format(endDate)
                    }
                }
                 // gives string date original value
                if (dateStartValue && dateStartValue == dateEndValue) {
                    return dateStartValue
                }
                return customMessageService.getMessage("app.dateRangeType.executed.range", dateStartValue, dateEndValue)
            }
        }
    }

    def getDateRangeValueForCriteriaWithoutString(ExecutedTemplateQuery templateQuery, Locale locale) {
        if (templateQuery) {
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.getShortDateFormatForLocale(locale))
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
            if (templateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEnum.name() == DateRangeValueEnum.CUMULATIVE.name()) {
                Date endDate = (templateQuery.executedConfiguration instanceof ExecutedPeriodicReportConfiguration) ?
                        templateQuery.getGlobalEndDate() :
                        templateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute

                String dateValue = sdf.format(endDate)
                return ['Cumulative', dateValue]
            } else {
                String dateStartValue = sdf.format(templateQuery.executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute)
                String dateEndValue = sdf.format(templateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute)
                if (dateStartValue && dateStartValue == dateEndValue) {
                    return [dateStartValue,dateStartValue]
                }
                return [dateStartValue, dateEndValue]
            }
        }
    }
    def getGlobalQueryDateRangeValueForCriteriaWithoutString(ExecutedReportConfiguration executedReportConfiguration, Locale locale) {
        if (executedReportConfiguration) {
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.getShortDateFormatForLocale(locale))
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
            if (executedReportConfiguration.executedGlobalDateRangeInformation.dateRangeEnum.name() == DateRangeValueEnum.CUMULATIVE.name()) {
                Date endDate = executedReportConfiguration.executedGlobalDateRangeInformation.dateRangeEndAbsolute

                String dateValue = sdf.format(endDate)
                return ['Cumulative', dateValue]
            } else {
                String dateStartValue = sdf.format(executedReportConfiguration.executedGlobalDateRangeInformation.dateRangeStartAbsolute)
                String dateEndValue = sdf.format(executedReportConfiguration.executedGlobalDateRangeInformation.dateRangeEndAbsolute)
                if (dateStartValue && dateStartValue == dateEndValue) {
                    return [dateStartValue, dateStartValue]
                }
                return [dateStartValue, dateEndValue]
            }
        }
    }

    def calculateFrequency(BaseConfiguration configuration) {
        if (configuration.scheduleDateJSON && configuration.nextRunDate) {
            if (configuration.scheduleDateJSON.contains(FrequencyEnum.HOURLY.name())) {
                return FrequencyEnum.HOURLY
            } else if (configuration.scheduleDateJSON.contains(FrequencyEnum.DAILY.name())) {
                if (configuration.scheduleDateJSON.contains("COUNT=1")) {
                    return FrequencyEnum.RUN_ONCE
                }
                return FrequencyEnum.DAILY
            } else if (configuration.scheduleDateJSON.contains("FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR;")) {
                return FrequencyEnum.WEEKDAYS
            } else if (configuration.scheduleDateJSON.contains(FrequencyEnum.WEEKLY.name())) {
                return FrequencyEnum.WEEKLY
            } else if (configuration.scheduleDateJSON.contains(FrequencyEnum.MONTHLY.name())) {
                return FrequencyEnum.MONTHLY
            } else if (configuration.scheduleDateJSON.contains(FrequencyEnum.YEARLY.name())) {
                return FrequencyEnum.YEARLY
            } else if (configuration.scheduleDateJSON.contains(FrequencyEnum.MINUTELY.name())) {
                return FrequencyEnum.MINUTELY
            }
        }
        return FrequencyEnum.RUN_ONCE

    }

    @NotTransactional
    def getQueriesId(TemplateQuery templateQuery) {
        SuperQuery query = GrailsHibernateUtil.unwrapIfProxy(templateQuery?.query)
        if (query) {
            if (query.queryType == QueryTypeEnum.SET_BUILDER) {
                query = (QuerySet) query
                String ids = query.queries.id.toString()
                return ids.substring(1, ids.length() - 1) // remove "[" and "]" from the list
            } else {
                return query.id
            }
        }
        return null
    }

    String getDateRangeValue(BaseDateRangeInformation executedDateRangeInformation, Locale locale) {
        if (executedDateRangeInformation) {
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.getShortDateFormatForLocale(locale))
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
            if (executedDateRangeInformation.dateRangeEnum.name() == DateRangeValueEnum.CUMULATIVE.name()) {
                String dateValue = sdf.format(executedDateRangeInformation.dateRangeEndAbsolute)
                // gives string date original value
                return customMessageService.getMessage("app.dateRangeType.executed.cumulative", dateValue)
            } else {
                String dateStartValue = sdf.format(executedDateRangeInformation.dateRangeStartAbsolute)
                // gives string date original value
                String dateEndValue = sdf.format(executedDateRangeInformation.dateRangeEndAbsolute)
                // gives string date original value
                return customMessageService.getMessage("app.dateRangeType.executed.range", dateStartValue, dateEndValue)
            }
        }
    }

    @NotTransactional
    org.joda.time.Period getDeltaPeriod(def configuration) {
        if (configuration.isEnabled) {
            Date currentToNextRunDate = getNextDate(configuration)
            if (currentToNextRunDate && configuration.nextRunDate) {
                Interval interval = new Interval(configuration.nextRunDate.getTime(), currentToNextRunDate.getTime())
                return interval.toPeriod().normalizedStandard(PeriodType.yearMonthDay())
            }
            log.debug("#### Returning Delta Period as Null for configuration = ${configuration.id} because currentToNextRunDate = ${currentToNextRunDate} and configuration.nextRunDate = ${configuration.nextRunDate}")
        }
        return null
    }

    // Export JSON Report Configuration
    @NotTransactional
    JSON getConfigurationAsJSON(ReportConfiguration reportConfiguration) {
        HashMap configurationMap = new HashMap(MiscUtil.getObjectProperties(reportConfiguration))
        configurationMap.put("classNameData", reportConfiguration.getClass().name)
        configurationMap.remove("configurationService")
        configurationMap.remove("modifiedBy")
        configurationMap.remove("createdBy")
        configurationMap.remove("lastUpdated")
        configurationMap.remove("dateCreated")
        configurationMap.remove("executing")
        configurationMap.remove("expectedExecutionTime")
        configurationMap.remove("numOfExecutions")
        configurationMap.remove("globalQuery")
        configurationMap.remove("globalDateRangeInformation")
        configurationMap.remove("emailConfiguration")
        configurationMap.remove("dmsConfiguration")
        configurationMap.remove("dateRangeType")
        configurationMap.remove("deliveryOption")
        configurationMap.remove("useCaseSeries")
        configurationMap.remove("templateQueries")
        configurationMap.remove("globalQueryValueLists")
        configurationMap.remove("shareWithUsers")
        configurationMap.remove("shareWithGroups")
        configurationMap.remove("tags")
        configurationMap.remove("sourceProfile")
        configurationMap.remove("recipientOrganization")
        configurationMap.remove("senderOrganization")
        configurationMap.remove("e2bDistributionChannel")
        configurationMap.remove("fieldProfile")
        configurationMap.remove("calendars")
        if (reportConfiguration.globalDateRangeInformation) {
            configurationMap.globalDateRangeInformation = MiscUtil.getObjectProperties(reportConfiguration.globalDateRangeInformation)
            configurationMap.globalDateRangeInformation.remove("reportConfiguration")
        }
        if (reportConfiguration.globalQuery) {
            configurationMap.globalQuery = [name: reportConfiguration.globalQuery?.name]
        }
        if (reportConfiguration.dateRangeType) {
            configurationMap.dateRangeType = [name: reportConfiguration.dateRangeType?.name]
        }

        if (reportConfiguration.emailConfiguration) {
            configurationMap.emailConfiguration = MiscUtil.getObjectProperties(reportConfiguration.emailConfiguration)
        }

        if (reportConfiguration.dmsConfiguration) {
            configurationMap.dmsConfiguration = MiscUtil.getObjectProperties(reportConfiguration.dmsConfiguration)
        }

        if (reportConfiguration.sourceProfile) {
            configurationMap.sourceProfile = MiscUtil.getObjectProperties(reportConfiguration.sourceProfile)
        }

        if (reportConfiguration.deliveryOption) {
            Map deliveryOption = new HashMap(MiscUtil.getObjectProperties(reportConfiguration.deliveryOption))
            deliveryOption.remove('report')
            deliveryOption.remove('sharedWith')
            deliveryOption.remove('sharedWithUsers')
            deliveryOption.remove('executableBy')
            deliveryOption.remove('executableByGroup')
            if (reportConfiguration.deliveryOption.sharedWith) {
                deliveryOption.sharedWith = reportConfiguration.deliveryOption.sharedWith.collect {
                    [username: it.username]
                }
            }
            if (reportConfiguration.deliveryOption.sharedWithGroup) {
                deliveryOption.sharedWithGroup = reportConfiguration.deliveryOption.sharedWithGroup.collect {
                    [name: it.name]
                }
            }
            if (reportConfiguration.deliveryOption.executableBy) {
                deliveryOption.executableBy = reportConfiguration.deliveryOption.executableBy.collect {
                    [username: it.username]
                }
            }
            if (reportConfiguration.deliveryOption.executableByGroup) {
                deliveryOption.executableByGroup = reportConfiguration.deliveryOption.executableByGroup.collect {
                    [name: it.name]
                }
            }
            configurationMap.deliveryOption = deliveryOption
        }
        if (reportConfiguration.templateQueries) {
            configurationMap.templateQueries = reportConfiguration.templateQueries.collect {
                return getTemplateQueryMap(it)
            }
        }
        if (reportConfiguration.globalQueryValueLists) {
            configurationMap.globalQueryValueLists = reportConfiguration.globalQueryValueLists.collect {
                [query: [name: it.query.name], parameterValues: it.parameterValues.collect {
                    MiscUtil.getObjectProperties(GrailsHibernateUtil.unwrapIfProxy(it))
                }]
            }
        }
        if (reportConfiguration.tags) {
            configurationMap.tags = reportConfiguration.tags.collect {
                [name: it.name]
            }
        }
        if(reportConfiguration instanceof Configuration && reportConfiguration.useCaseSeries) {
            configurationMap.useCaseSeries = MiscUtil.getObjectProperties(reportConfiguration.useCaseSeries)
            configurationMap.useCaseSeries?.remove("executedDeliveryOption")
            configurationMap.useCaseSeries?.remove("tags")
        }
        if (reportConfiguration instanceof IcsrReportConfiguration || reportConfiguration instanceof IcsrProfileConfiguration) {
            if (reportConfiguration.recipientOrganization) {
                configurationMap.recipientOrganization = MiscUtil.getObjectProperties(reportConfiguration.recipientOrganization, ['unitName', 'unitRegisteredId', 'unitType', 'id', 'unitRetired'])
            }
            if (reportConfiguration.senderOrganization) {
                configurationMap.senderOrganization = MiscUtil.getObjectProperties(reportConfiguration.senderOrganization, ['unitName', 'unitRegisteredId', 'unitType', 'id', 'unitRetired'])
            }
            if (reportConfiguration instanceof IcsrProfileConfiguration && reportConfiguration.e2bDistributionChannel) {
                configurationMap.e2bDistributionChannel = MiscUtil.getObjectProperties(reportConfiguration.e2bDistributionChannel)
            }
            if (reportConfiguration instanceof IcsrProfileConfiguration && reportConfiguration.fieldProfile) {
                configurationMap.fieldProfile = MiscUtil.getObjectProperties(reportConfiguration.fieldProfile, ['id', 'name', 'description', 'isDeleted'])
            }
            if (reportConfiguration instanceof IcsrReportConfiguration && reportConfiguration.referenceProfile) {
                configurationMap.referenceProfile = MiscUtil.getObjectProperties(reportConfiguration.referenceProfile,['id','reportName', 'description', 'isDeleted'])
            }
            if(reportConfiguration instanceof IcsrProfileConfiguration && reportConfiguration.adjustDueDate){
                SafetyCalendar.withNewSession {
                    configurationMap.holidayCalendars = reportConfiguration.calendars.collect {
                        [name: SafetyCalendar.read(it).name]
                    }
                }
            }

        }

        return configurationMap as JSON
    }

    Map getTemplateQueryMap(TemplateQuery templateQuery) {
        Map templateQueryMap = new HashMap(MiscUtil.getObjectProperties(templateQuery))
        templateQueryMap.remove("template")
        templateQueryMap.remove("report")
        templateQueryMap.remove("query")
        templateQueryMap.remove("dateRangeInformationForTemplateQuery")
        templateQueryMap.remove("queryValueLists")
        templateQueryMap.remove("templateValueLists")
        templateQueryMap.remove("emailConfiguration")
        templateQueryMap.template = [name: templateQuery.template.name]
        templateQueryMap.query = templateQuery.query ? [name: templateQuery.query.name] : null
        templateQueryMap.dateRangeInformationForTemplateQuery = new HashMap(MiscUtil.getObjectProperties(templateQuery.dateRangeInformationForTemplateQuery))
        templateQueryMap.dateRangeInformationForTemplateQuery.remove("templateQuery")
        if (templateQuery.queryValueLists) {
            templateQueryMap.queryValueLists = templateQuery.queryValueLists.collect { qvl ->
                return [query: [name: qvl.query.name], parameterValues: qvl.parameterValues.collect {
                    return MiscUtil.getObjectProperties(GrailsHibernateUtil.unwrapIfProxy(it))
                }]
            }
        }
        if (templateQuery.templateValueLists) {
            templateQueryMap.templateValueLists = templateQuery.templateValueLists.collect { tvl ->
                return [template: [name: tvl.template.name], parameterValues: tvl.parameterValues.collect {
                    MiscUtil.getObjectProperties(GrailsHibernateUtil.unwrapIfProxy(it))
                }]
            }
        }
        if (templateQuery instanceof IcsrTemplateQuery && templateQuery.emailConfiguration) {
            templateQueryMap.emailConfiguration = MiscUtil.getObjectProperties(templateQuery.emailConfiguration)
        }
        return templateQueryMap
    }

    def setFavorite(ReportConfiguration configuration, Boolean isFavorite) {
        def user = userService.getUser()
        ConfigurationUserState configurationUserState = ConfigurationUserState.findByUserAndConfiguration(user, configuration)
        if (!configurationUserState) {
            configurationUserState = new ConfigurationUserState(user: user, configuration: configuration)
        }
        configurationUserState.isFavorite = isFavorite ? true : null
        configurationUserState.save()
    }

    Map fetchConfigurationMapFromSession(params, session) {
        Map configurationParams = null
        Map templateQueryIndexMap = null
        Map configurationMap = session.editingConfiguration
        if (configurationMap?.configurationParams && params.continueEditing) {
            session.editingConfiguration.templateId = params.templateId
            session.editingConfiguration.queryId = params.queryId
            templateQueryIndexMap = [index: configurationMap.templateQueryIndex, type: configurationMap.templateId ? "template" : "query"]
            configurationParams = new JsonSlurper().parseText(configurationMap.configurationParams) as Map
        }
        [configurationParams: configurationParams, templateQueryIndex: templateQueryIndexMap]
    }

    @NotTransactional
    void initConfigurationTemplatesFromSession(session, ReportConfiguration configurationInstance) {
        Integer templateQueryIndex = session.editingConfiguration.templateQueryIndex as Integer
        if (session.editingConfiguration.templateId) {
            if (templateQueryIndex != null &&
                    templateQueryIndex < configurationInstance.templateQueries.size() &&
                    configurationInstance.templateQueries[templateQueryIndex])
                configurationInstance.templateQueries[templateQueryIndex as Integer].template = ReportTemplate.get(session.editingConfiguration.templateId as Long)
            else if (templateQueryIndex != null && (templateQueryIndex == 0) && !configurationInstance.templateQueries) {
                if (configurationInstance instanceof IcsrProfileConfiguration || configurationInstance instanceof IcsrReportConfiguration) {
                    configurationInstance.addToTemplateQueries(new IcsrTemplateQuery())
                } else {
                    configurationInstance.addToTemplateQueries(new TemplateQuery())
                }
                configurationInstance.templateQueries[templateQueryIndex as Integer].template = ReportTemplate.get(session.editingConfiguration.templateId as Long)
            } else {
                for (int i = 0; i < configurationInstance.templateQueries.size(); i++) {
                    if (configurationInstance.templateQueries[i].template == null) {
                        configurationInstance.templateQueries[i].template = ReportTemplate.get(session.editingConfiguration.templateId as Long)
                        session.editingConfiguration.templateQueryIndex = i
                        break
                    }
                }
            }
        }
    }

    @NotTransactional
    void initConfigurationQueriesFromSession(session, ReportConfiguration configurationInstance) {
        Integer templateQueryIndex = session.editingConfiguration.templateQueryIndex as Integer
        if (session.editingConfiguration.queryId) {
            if (templateQueryIndex != null &&
                    templateQueryIndex < configurationInstance.templateQueries.size() &&
                    configurationInstance.templateQueries[templateQueryIndex])
                configurationInstance.templateQueries[templateQueryIndex].query = SuperQuery.get(session.editingConfiguration.queryId as Long)
            else if ((templateQueryIndex == 0) && !configurationInstance.templateQueries) {
                if (configurationInstance instanceof IcsrProfileConfiguration || configurationInstance instanceof IcsrReportConfiguration) {
                    configurationInstance.addToTemplateQueries(new IcsrTemplateQuery())
                } else {
                    configurationInstance.addToTemplateQueries(new TemplateQuery())
                }
                configurationInstance.templateQueries[templateQueryIndex as Integer].query = SuperQuery.get(session.editingConfiguration.queryId as Long)
            } else {
                for (int i = 0; i < configurationInstance.templateQueries.size(); i++) {
                    if (configurationInstance.templateQueries[i].query == null) {
                        configurationInstance.templateQueries[i].query = SuperQuery.get(session.editingConfiguration.queryId as Long)
                        session.editingConfiguration.templateQueryIndex = i
                        break
                    }
                }
            }
        }
    }
    @NotTransactional
    void checkProductCheckboxes(ReportConfiguration configurationInstance) {
        if (!configurationInstance.productSelection && !configurationInstance.validProductGroupSelection && !configurationInstance.isTemplate) {
            configurationInstance.suspectProduct = false
            configurationInstance.includeAllStudyDrugsCases = false
            configurationInstance.templateQueries.each {
                it.headerProductSelection = false
            }
        }
    }

    String getScheduledDateJsonAfterDisable(ReportConfiguration configurationInstance) {
        Map parsedScheduledDateJSON = JSON.parse(configurationInstance.scheduleDateJSON)
        String startDateTime = parsedScheduledDateJSON?.startDateTime
        String timeZone = parsedScheduledDateJSON?.timeZone?.name
        String scheduledTimeZone = """name" :"${timeZone}","offset" : "${DateUtil.getOffsetString(timeZone)}"""
        return """{"startDateTime":"${
            startDateTime
        }","timeZone":{"${scheduledTimeZone}"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1"}"""
    }

    String getCopiedConfigurationScheduledDateJson(ReportConfiguration configurationInstance) {
        if (configurationInstance.scheduleDateJSON) {
            Map parsedScheduledDateJSON = JSON.parse(configurationInstance.scheduleDateJSON)
            String timeZone = userService.currentUser?.preference?.timeZone?:parsedScheduledDateJSON?.timeZone?.name
            String timeZoneText = (parsedScheduledDateJSON?.timeZone?.text)?.replace("\n", "\\n")
            String startDateTime = DateUtil.StringFromDate(new Date(), DateUtil.SCHEDULE_DATE_JSON_FORMAT, timeZone)
            String scheduledTimeZone = """name" :"${timeZone}","offset" : "${DateUtil.getOffsetString(timeZone)}","text" : "${timeZoneText}"""
            return """{"startDateTime":"${
                startDateTime
            }","timeZone":{"${scheduledTimeZone}"},"recurrencePattern":"${
                parsedScheduledDateJSON?.recurrencePattern
            }"}"""
        }
    }

    ReportConfiguration copyConfig(IcsrProfileConfiguration configuration, User user, String namePrefix = null, Long tenantId = null) {
        DeliveryOption deliveryOption = configuration.deliveryOption ? new DeliveryOption(MiscUtil.getObjectProperties(configuration.deliveryOption, DeliveryOption.propertiesToUseWhileCopying)) : null
        if (deliveryOption)
        deliveryOption.sharedWith = [user]
        IcsrProfileConfiguration newIcsrProfileConfiguration = new IcsrProfileConfiguration(MiscUtil.getObjectProperties(configuration, IcsrProfileConfiguration.propertiesToUseWhileCopying))
        newIcsrProfileConfiguration.scheduleDateJSON = getCopiedConfigurationScheduledDateJson(configuration) ?: configuration.scheduleDateJSON
        newIcsrProfileConfiguration.emailConfiguration = (configuration.emailConfiguration && !configuration.emailConfiguration?.isDeleted) ? (EmailConfiguration) CRUDService.save(new EmailConfiguration(configuration.emailConfiguration.properties)) : null
        newIcsrProfileConfiguration.dmsConfiguration = (configuration.dmsConfiguration && !configuration.dmsConfiguration?.isDeleted) ? (DmsConfiguration) CRUDService.save(new DmsConfiguration(configuration.dmsConfiguration.properties)) : null
        newIcsrProfileConfiguration.reportName = generateUniqueName(configuration, namePrefix)
        newIcsrProfileConfiguration.owner = user
        newIcsrProfileConfiguration.createdBy = user.username
        newIcsrProfileConfiguration.modifiedBy = user.username
        newIcsrProfileConfiguration.deliveryOption = deliveryOption
        if (tenantId) {
            newIcsrProfileConfiguration.tenantId = tenantId
        }
        configuration.templateQueries.each {
            IcsrTemplateQuery tq = new IcsrTemplateQuery(MiscUtil.getObjectProperties(it, IcsrTemplateQuery.propertiesToUseWhileCopying))
            tq.createdBy = user.username
            tq.modifiedBy = user.username
            tq.dateRangeInformationForTemplateQuery = new DateRangeInformation(MiscUtil.getObjectProperties(it.dateRangeInformationForTemplateQuery, DateRangeInformation.propertiesToUseWhileCopying))
            tq.dateRangeInformationForTemplateQuery.templateQuery = tq
            newIcsrProfileConfiguration.addToTemplateQueries(copyBlankValues(tq, it.queryValueLists, it.templateValueLists))
        }
        newIcsrProfileConfiguration.e2bDistributionChannel = configuration.e2bDistributionChannel ? (DistributionChannel) CRUDService.save(new DistributionChannel(configuration.e2bDistributionChannel.properties)) : null
        configuration.calendars?.each {
            newIcsrProfileConfiguration.addToCalendars(it)
        }
        configuration.authorizationTypes.each {
            newIcsrProfileConfiguration.addToAuthorizationTypes(it)
        }
        CRUDService.save(newIcsrProfileConfiguration)
        return newIcsrProfileConfiguration
    }

    ReportConfiguration copyConfig(IcsrReportConfiguration configuration, User user, String namePrefix = null, Long tenantId = null, boolean isCreateFromTemplate = false) {
        DeliveryOption deliveryOption = new DeliveryOption(MiscUtil.getObjectProperties(configuration.deliveryOption, DeliveryOption.propertiesToUseWhileCopying))
        deliveryOption.sharedWith = [user]
        IcsrReportConfiguration newIcsrReportConfiguration = new IcsrReportConfiguration(MiscUtil.getObjectProperties(configuration, IcsrReportConfiguration.propertiesToUseWhileCopying))
        newIcsrReportConfiguration.isEnabled = false
        newIcsrReportConfiguration.scheduleDateJSON = getCopiedConfigurationScheduledDateJson(configuration) ?: configuration.scheduleDateJSON
        newIcsrReportConfiguration.emailConfiguration = (configuration.emailConfiguration && !configuration.emailConfiguration?.isDeleted) ? (EmailConfiguration) CRUDService.save(new EmailConfiguration(configuration.emailConfiguration.properties)) : null
        newIcsrReportConfiguration.dmsConfiguration = (configuration.dmsConfiguration && !configuration.dmsConfiguration?.isDeleted) ? (DmsConfiguration) CRUDService.save(new DmsConfiguration(configuration.dmsConfiguration.properties)) : null
        newIcsrReportConfiguration.reportName = generateUniqueName(configuration, user, namePrefix)
        newIcsrReportConfiguration.owner = user
        newIcsrReportConfiguration.createdBy = user.username
        newIcsrReportConfiguration.modifiedBy = user.username
        newIcsrReportConfiguration.deliveryOption = deliveryOption
        newIcsrReportConfiguration.referenceProfile = configuration.referenceProfile
        if (tenantId) {
            newIcsrReportConfiguration.tenantId = tenantId
        }
        if (configuration.isTemplate && isCreateFromTemplate) {
            newIcsrReportConfiguration.configurationTemplate = configuration
        }
        configuration.reportingDestinations?.each {
            newIcsrReportConfiguration.addToReportingDestinations(it)
        }
        if (configuration.globalDateRangeInformation) {
            newIcsrReportConfiguration.globalDateRangeInformation = new GlobalDateRangeInformation(MiscUtil.getObjectProperties(configuration.globalDateRangeInformation, GlobalDateRangeInformation.propertiesToUseForCopying))
            newIcsrReportConfiguration.globalDateRangeInformation.reportConfiguration = newIcsrReportConfiguration
        }
        configuration.globalQueryValueLists?.each {
            QueryValueList queryValueList = new QueryValueList(query: it.query)
            it.parameterValues.each {
                if (it.hasProperty('reportField')) {
                    queryValueList.addToParameterValues(new QueryExpressionValue(key: it.key,
                            reportField: it.reportField, operator: it.operator, value: it.value, specialKeyValue: it.specialKeyValue))
                } else {
                    queryValueList.addToParameterValues(new CustomSQLValue(key: it.key,
                            value: it.value))
                }
            }
            newIcsrReportConfiguration.addToGlobalQueryValueLists(queryValueList)
        }
        configuration.templateQueries.each {
            IcsrTemplateQuery tq = new IcsrTemplateQuery(MiscUtil.getObjectProperties(it, IcsrTemplateQuery.propertiesToUseWhileCopying))
            tq.createdBy = user.username
            tq.modifiedBy = user.username
            tq.dateRangeInformationForTemplateQuery = new DateRangeInformation(MiscUtil.getObjectProperties(it.dateRangeInformationForTemplateQuery, DateRangeInformation.propertiesToUseWhileCopying))
            tq.dateRangeInformationForTemplateQuery.templateQuery = tq
            newIcsrReportConfiguration.addToTemplateQueries(copyBlankValues(tq, it.queryValueLists, it.templateValueLists))
        }
        configuration.reportTasks?.each {
            ReportTask reportTask = new ReportTask(MiscUtil.getObjectProperties(it))
            newIcsrReportConfiguration.addToReportTasks(reportTask)
        }
        CRUDService.save(newIcsrReportConfiguration)
        return newIcsrReportConfiguration
    }

    String generateUniqueName(IcsrReportConfiguration config, User user, String namePrefix = null) {
        String prefix = namePrefix == null ? ViewHelper.getMessage("app.configuration.copy.of") + " " : (namePrefix.length() > 0 ? (namePrefix + ' ') : "")
        String newName = trimName(prefix,config.reportName,"")
        if (IcsrReportConfiguration.findByReportNameAndOwnerAndIsDeleted(newName, user, false)) {
            int count = 1
            newName = prefix + config.reportName + " ($count)"
            while (IcsrReportConfiguration.countByReportNameIlikeAndOwnerAndIsDeleted(newName, user, false)) {
                newName = prefix + config.reportName + " (${++count})"
            }
        }
        return newName
    }

    Map toBulkTableMap(Configuration conf){
        [id                         : conf.id,
         reportName                 : conf.reportName,
         isTemplate                 : conf.isTemplate,
         productSelection           : ViewHelper.getDictionaryValues(conf, DictionaryTypeEnum.PRODUCT),
         productsJson               : conf.productSelection as String,
         groupsJson                 : conf.validProductGroupSelection as String,
         nextRunDate                : DateUtil.getLongDateStringForTimeZone(conf.nextRunDate, userService.currentUser?.preference?.timeZone),
         status                     : (conf.isEnabled && conf.nextRunDate),
         scheduleDateJSON           : periodicReportService.parseScheduler(conf.scheduleDateJSON, userService.currentUser?.preference?.locale),
         schedulerJSON              : conf.scheduleDateJSON,
         configurationTemplate      : conf.configurationTemplate?.reportName?:"",
         sharedWithUsers            : conf.deliveryOption.sharedWith,
         sharedWithGroup            : conf.deliveryOption.sharedWithGroup,
         emailToUsers               : conf.deliveryOption.emailToUsers
        ]
    }

    Map importFromExcel(workbook) {
        def errors = []
        def added = []
        def updated = []
        User currentUser = userService.getCurrentUser()
        Sheet sheet = workbook?.getSheetAt(0);
        Row row;
        if (sheet) {
            int groupCount = Holders.config.getProperty('pv.dictionary.group.enabled', Boolean) ? 1 : 0
            if (sheet.getRow(2).getLastCellNum() == (9 + groupCount + PVDictionaryConfig.ProductConfig.views.size())) {
                int lastCell = sheet.getRow(2).getLastCellNum() - 1
                for (int i = 3; i <= sheet?.getLastRowNum(); i++) {
                    if ((row = sheet.getRow(i)) != null) {
                        Boolean empty = true
                        [0..6].each { empty = empty & !getExcelCell(row, 0) }
                        if (empty) continue;
                        boolean update = true
                        String reportName = getExcelCell(row, 0)
                        String tenant = getExcelCell(row, lastCell - 1)
                        String username = getExcelCell(row, lastCell)
                        if (!reportName) {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.reportName", i + 1)
                            continue;
                        }
                        if (!tenant) {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.tenant.does.not.exist", i+1)
                            continue;
                        }
                        if (!username) {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.userName.does.not.exist", i + 1)
                            continue;
                        }
                        User owner = User.findByUsernameIlike(username)
                        if (!owner) {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.userName.invalid", i+1, username)
                            continue;
                        }
                        if (added.contains(reportName)) {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.reportName.exist", i + 1)
                            continue;
                        }
                        Integer tenantId = Integer.parseInt(tenant)
                        Configuration configuration = Configuration.findByReportNameAndIsDeletedAndTenantIdAndOwner(reportName, false, tenantId, owner)
                        if (!configuration) {
                            String templateName = getExcelCell(row, 1)
                            if (templateName) {
                                Configuration template = Configuration.findByReportNameAndIsDeletedAndIsTemplate(templateName, false, true)
                                if (!template) {
                                    errors << ViewHelper.getMessage("app.bulkUpdate.error.template", i + 1, templateName)
                                    continue;
                                }
                                configuration = copyConfig(template, owner, null, tenantId)
                                update = false
                            } else {
                                errors << ViewHelper.getMessage("app.bulkUpdate.error.template.empty", i + 1)
                                continue;
                            }
                        }
                        try {
                            int columNumber = 1
                            def product = [:]
                            String lang = currentUser.preference.locale
                            columNumber = parseProducts(row, product, columNumber, lang)
                            configuration.reportName = reportName
                            configuration.productSelection = new JsonBuilder(product).toString()
                            if (Holders.config.getProperty('pv.dictionary.group.enabled', Boolean)) {
                                parseProductGroup(row, ++columNumber, configuration, currentUser)
                            }
                            if (getExcelCell(row, ++columNumber)) {
                                JSON.parse(getExcelCell(row, columNumber)) //just to check json
                                configuration.scheduleDateJSON = getExcelCell(row, columNumber)
                            }
                            if (!bindSharedWithUsers(configuration, getExcelCell(row, ++columNumber), update, errors, i + 1)) {
                                configuration.discard()
                                continue
                            }
                            if (!bindSharedWithGroups(configuration, getExcelCell(row, ++columNumber), update, errors, i + 1)) {
                                configuration.discard()
                                continue
                            }
                            configuration?.deliveryOption?.emailToUsers?.clear()
                            if (getExcelCell(row, ++columNumber)) {
                                getExcelCell(row, columNumber).split(", ").each { String emailId ->
                                    configuration.deliveryOption.emailToUsers.add(emailId)
                                }
                            }
                            if (getExcelCell(row, ++columNumber)) {
                                if (configuration.deliveryOption.emailToUsers) {
                                    List<ReportFormatEnum> attachmentFormats = []
                                    boolean toContinue = false
                                    getExcelCell(row, columNumber).split(", ").each { String deliveryMedia ->
                                        if (ReportFormatEnum.collect().find { it.toString() == deliveryMedia.toUpperCase() })
                                            attachmentFormats.add(ReportFormatEnum.valueOf(deliveryMedia.toUpperCase()))
                                        else {
                                            errors << ViewHelper.getMessage("app.bulkUpdate.error.attachmentFormat.invalid", i + 1, deliveryMedia)
                                            toContinue = true;
                                            return
                                        }
                                    }
                                    if (toContinue) {
                                        configuration.discard()
                                        continue
                                    }
                                    if (attachmentFormats)
                                        configuration.deliveryOption.attachmentFormats = attachmentFormats
                                } else {
                                    errors << ViewHelper.getMessage("app.bulkUpdate.error.email.nullable")
                                    configuration.discard()
                                    continue

                                }
                            }
                            if (!getExcelCell(row, columNumber)) {
                                configuration?.deliveryOption?.attachmentFormats?.clear()
                            }
                            CRUDService.saveOrUpdate(configuration)
                            if (update) updated << reportName + " (" + owner.username + ")"
                            else
                                added << reportName + " (" + owner.username + ")"
                        } catch (ValidationException v) {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.row", i + 1) + " " + v.errors.allErrors.collect { error ->
                                String errSting = error.toString()
                                if (error instanceof FieldError) errSting = ViewHelper.getMessage("app.label.field.invalid.value", error.field)
                                errSting
                            }.join(";")
                        } catch (Exception e) {
                            errors << ViewHelper.getMessage("app.bulkUpdate.error.row", i + 1) + " " + e.getMessage()
                        }
                    }
                }
            } else {
                errors << ViewHelper.getMessage("app.bulkUpdate.error.excelFile.invalid")
            }
        } else {
            errors << ViewHelper.getMessage('app.label.no.data.excel.error')
        }
        updated = updated.unique();
        [errors: errors, added: added, updated: updated]
    }

    void parseProductGroup(row, columNumber, configuration, currentUser) {
        String prodGroup = getExcelCell(row, columNumber)
        configuration.productGroupSelection = null
        if (prodGroup) {
            DictionaryGroup dictionaryGroup = DictionaryGroup.findByGroupName(prodGroup)
            List<DictionaryGroup> productGroupJsonList = []
            prodGroup.tokenize(",")?.collect { it.trim() }?.findAll { it }?.each {

                List<DictionaryGroup> dictionaryGroupLst = DictionaryGroup.getAllRecordsBySearch(1, it, null, currentUser, configuration.tenantId as Integer, true, dictionaryGroup ? dictionaryGroup.isMultiIngredient : false).list()
                productGroupJsonList.addAll(dictionaryGroupLst)
            }
            if (productGroupJsonList) {
                configuration.productGroupSelection =new JsonBuilder(productGroupJsonList.collect { it ->
                    [
                            id               : it.id,
                            name             : it.groupName + " (" + it.id + ")",
                            isMultiIngredient: it.isMultiIngredient
                    ]
                } ).toString()
            }
        }
    }

    int parseProducts(row, product, columNumber, lang) {
        int columNumberLocal = columNumber
        LmProdDic200."pva".withNewSession { sess ->
            PVDictionaryConfig.ProductConfig.views.eachWithIndex { v, ind ->
                String prodValue = getExcelCell(row, ++columNumberLocal)
                if (prodValue) {
                    product["${ind + 1}"] = []
                    prodValue.tokenize(",")?.collect { it.trim() }?.findAll { it }?.each {
                        def elem = searchProduct(ind, it, lang, false)
                        if (!elem) elem = searchProduct(ind, it, lang, true)
                        if (elem) product["${ind + 1}"].addAll(elem)
                    }
                } else {
                    product["${ind + 1}"] = []
                }
            }
        }
        return columNumberLocal
    }

    private List searchProduct(ind, term, lang, isMultiIngredient) {
        lang = getPVALanguageId(lang ?: 'en').toString()
        return productDictionaryService.getViewsData("${ind + 1}", null, term, lang, "true", null, null, null, 'pva', isMultiIngredient)?.collect {
            [name: it.name, id: it.viewId, isMultiIngredient: it.isMultiIngredient]
        }
    }

    private String getExcelCell(Row row, int i) {
        Cell cell = row?.getCell(i)
        cell?.setCellType(CellType.STRING);
        return cell?.getStringCellValue()?.trim()
    }

    private boolean bindSharedWithUsers(Configuration configuration, String sharedWithUsers, Boolean isUpdate = false, List errors, int rowNum) {
        List<User> allowedUsers = userService.getAllowedSharedWithUsersForCurrentUser()
        if (isUpdate) {
            if (configuration.getShareWithUsers()) {
                allowedUsers.addAll(configuration.getShareWithUsers())
                allowedUsers.unique { it.id }
            }
            configuration?.deliveryOption?.sharedWith?.clear()
        }

        if (sharedWithUsers) {
            boolean hasError = false
            sharedWithUsers.split(", ").each { String shared ->
                DeliveryOption deliveryOption = configuration.deliveryOption
                User user = User.findByUsernameIlike(shared)
                if (user) {
                    if (deliveryOption.sharedWith) {
                        if (user && allowedUsers.find { it.id == user.id }) {
                            if (!deliveryOption.sharedWith.find { it.id == user.id })
                                deliveryOption.addToSharedWith(user)
                        }
                    } else {
                        if (user && allowedUsers.find { it.id == user.id }) {
                            deliveryOption.addToSharedWith(user)
                        }
                    }
                } else {
                    errors << ViewHelper.getMessage("app.bulkUpdate.error.userName.invalid", rowNum, shared)
                    hasError = true
                }
                deliveryOption.report = configuration
            }
            if (hasError) return false
        }
        return true;
    }

    private boolean bindSharedWithGroups(Configuration configuration, String sharedWithGroups, Boolean isUpdate = false, List errors, int rowNum) {
        List<UserGroup> allowedGroups = userService.getAllowedSharedWithGroupsForCurrentUser();

        if (isUpdate) {
            if (configuration.getShareWithGroups()) {
                allowedGroups.addAll(configuration.getShareWithGroups())
                allowedGroups.unique { it.id }
            }
            configuration?.deliveryOption?.sharedWithGroup?.clear()
        }

        if (sharedWithGroups) {
            boolean hasError = false
            sharedWithGroups.split(", ").each { String shared ->
                DeliveryOption deliveryOption = configuration.deliveryOption
                UserGroup userGroup = UserGroup.findByName(shared)
                if (userGroup && allowedGroups.find { it.id == userGroup.id }) {
                    deliveryOption.addToSharedWithGroup(userGroup)
                } else if (!userGroup) {
                    errors << ViewHelper.getMessage("app.bulkUpdate.error.groupName.invalid", rowNum, shared)
                    hasError = true
                }
                deliveryOption.report = configuration
            }
            if (hasError) return false
        }

        DeliveryOption deliveryOption = configuration.deliveryOption
        if (!deliveryOption.sharedWith && !deliveryOption.sharedWithGroup) {
            deliveryOption.addToSharedWith(userService.currentUser)
            deliveryOption.report = configuration
        }
        return true
    }

    void fixBindDateRange(def globalDateRangeInformation, def instance, def params) {
        if (globalDateRangeInformation.dateRangeEnum == DateRangeEnum.CUSTOM) {
            Locale locale = userService.currentUser?.preference?.locale
            if(instance instanceof AutoReasonOfDelay) {
                globalDateRangeInformation.dateRangeStartAbsolute = DateUtil.getStartDate(params.globalDateRangeInformationAutoROD.dateRangeStartAbsolute, locale)
                globalDateRangeInformation.dateRangeEndAbsolute = DateUtil.getEndDate(params.globalDateRangeInformationAutoROD.dateRangeEndAbsolute, locale)
            }else if(instance instanceof InboundCompliance) {
                globalDateRangeInformation.dateRangeStartAbsolute = DateUtil.getStartDate(params.globalDateRangeInbound.dateRangeStartAbsolute, locale)
                globalDateRangeInformation.dateRangeEndAbsolute = DateUtil.getEndDate(params.globalDateRangeInbound.dateRangeEndAbsolute, locale)
            }
            else{
                globalDateRangeInformation.dateRangeStartAbsolute = DateUtil.getStartDate(params.globalDateRangeInformation.dateRangeStartAbsolute, locale)
                globalDateRangeInformation.dateRangeEndAbsolute = DateUtil.getEndDate(params.globalDateRangeInformation.dateRangeEndAbsolute, locale)
            }
        } else {
            globalDateRangeInformation?.dateRangeStartAbsolute = null
            globalDateRangeInformation?.dateRangeEndAbsolute = null
        }
        if(instance instanceof AutoReasonOfDelay){
            globalDateRangeInformation.autoReasonOfDelay = instance
        }else if(instance instanceof  InboundCompliance) {
            globalDateRangeInformation.inboundCompliance = instance
        }else{
            globalDateRangeInformation.reportConfiguration = instance
        }
    }

    void bindParameterValuesToGlobalQuery(ReportConfiguration periodicReportConfiguration, def params) {
        if (periodicReportConfiguration.globalQueryValueLists) {
            params.put("oldglobalQueryValueLists${periodicReportConfiguration.id}", periodicReportConfiguration.globalQueryValueLists.toString())
        }
        periodicReportConfiguration.globalQueryValueLists?.each {
            it.parameterValues?.each {
                ParameterValue.get(it.id)?.delete()
            }
            it.parameterValues?.clear()
        }
        periodicReportConfiguration.globalQueryValueLists = []
        if (params.containsKey("qev[0].key")) {

            // for each single query
            int start = 0
            params.("validQueries").split(",").each { queryId -> // if query set
                QueryValueList queryValueList = new QueryValueList(query: queryId)

                int size = SuperQuery.get(queryId).getParameterSize()

                // if query set, iterate each query in query set
                for (int j = start; params.containsKey("qev[" + j + "].key") && j < (start + size); j++) {
                    ParameterValue tempValue
                    String key = params.("qev[" + j + "].key")
                    String value = params.("qev[" + j + "].value")
                    String specialKeyValue = params.("qev[" + j + "].specialKeyValue")
                    boolean isFromCopyPaste = false
                    if (params.("qev[" + j + "].copyPasteValue")) {
                        value = params.("qev[" + j + "].copyPasteValue")
                    }
                    if (params.("qev[" + j + "].isFromCopyPaste") == "true") {
                        isFromCopyPaste = true
                    }

                    ReportField reportField = ReportField.findByNameAndIsDeleted(params.("qev[" + j + "].field"), false)
                    if (specialKeyValue) {
                        if (!periodicReportConfiguration.poiInputsParameterValues*.key?.contains(specialKeyValue)) {
                            periodicReportConfiguration.addToPoiInputsParameterValues(new ParameterValue(key: specialKeyValue, value: value, isFromCopyPaste: isFromCopyPaste))
                        } else if (periodicReportConfiguration.poiInputsParameterValues*.key?.contains(specialKeyValue)) {
                            ParameterValue parameterValue = periodicReportConfiguration.poiInputsParameterValues.find {
                                it.key == specialKeyValue
                            }
                            value = parameterValue?.value
                            isFromCopyPaste = parameterValue?.isFromCopyPaste
                        }
                    }
                    if (params.containsKey("qev[" + j + "].field")) {
                        tempValue = new QueryExpressionValue(key: key, value: value, isFromCopyPaste: isFromCopyPaste,
                                reportField: reportField,
                                operator: QueryOperatorEnum.valueOf(params.("qev[" + j + "].operator")), specialKeyValue: specialKeyValue)
                    } else {
                        tempValue = new CustomSQLValue(key: key, value: value)
                    }
                    queryValueList.addToParameterValues(tempValue)
                }

                start += size
                periodicReportConfiguration.addToGlobalQueryValueLists(queryValueList)
            }
        }
    }

    boolean isUniqueName(String newName, User owner) {
        if(Configuration.countByReportNameIlikeAndOwnerAndIsDeleted(newName, owner, false) == 0){
            return true
        }
        return false
    }

    Map getCriteriaDate (ExecutedTemplateQuery executedTemplateQuery, Locale locale) {
        Map<String, String> criteriaDateMap = new HashMap<>()
        List dateRange = getDateRangeValueForCriteriaWithoutString(executedTemplateQuery, locale)
        criteriaDateMap.put(executedTemplateQuery.id + Constants.REPORTING_PERIOD_START_DATE, dateRange[0])
        criteriaDateMap.put(executedTemplateQuery.id + Constants.REPORTING_PERIOD_END_DATE, dateRange[1])
        criteriaDateMap
    }

    String replaceStringWithDate(String criteriaString, ExecutedTemplateQuery templateQuery, boolean isCriteriaSheet, Locale locale) {
        List<String> startNEndDates = []
        String result = criteriaString
        if(result.contains(Constants.REPORTING_PERIOD_START_DATE) || result.contains(Constants.REPORTING_PERIOD_END_DATE)) {
            startNEndDates = getDateRangeValueForCriteriaWithoutString(templateQuery, locale)
            result = result.replaceAll(Constants.REPORTING_PERIOD_START_DATE, isCriteriaSheet ? startNEndDates[0] + Constants.SPACE_STRING : startNEndDates[0])
            result = result.replaceAll(Constants.REPORTING_PERIOD_END_DATE, isCriteriaSheet ? startNEndDates[1] + Constants.SPACE_STRING : startNEndDates[1])
        }
        return result
    }
    //For Global query in Appendix
    String replaceGlobalQueryStringWithDate(String criteriaString, ExecutedReportConfiguration executedReportConfiguration ,boolean isCriteriaSheet, Locale locale) {
        List<String> globalStartNEndDates = []
        String result = criteriaString
        if(result.contains(Constants.REPORTING_PERIOD_START_DATE) || result.contains(Constants.REPORTING_PERIOD_END_DATE)) {
            globalStartNEndDates = getGlobalQueryDateRangeValueForCriteriaWithoutString(executedReportConfiguration, locale)
            result = result.replaceAll(Constants.REPORTING_PERIOD_START_DATE, isCriteriaSheet ? globalStartNEndDates[0] + Constants.SPACE_STRING : globalStartNEndDates[0])
            result = result.replaceAll(Constants.REPORTING_PERIOD_END_DATE, isCriteriaSheet ? globalStartNEndDates[1] + Constants.SPACE_STRING : globalStartNEndDates[1])
        }
        return result
    }

    void removeRemovedTemplateQueries(ReportConfiguration configurationInstance) {
        def _toBeRemoved = configurationInstance.templateQueries?.findAll {
            (it?.dynamicFormEntryDeleted || (it == null) )
        }
        if (_toBeRemoved) {
            configurationInstance.templateQueries?.removeAll(_toBeRemoved)
        }
        configurationInstance.templateQueries?.eachWithIndex() { templateQuery, i ->
            if (templateQuery) {
                templateQuery.index = i
            }
        }
    }

    @NotTransactional
    void bindSharedWith(ReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate = false) {
        List<User> allowedUsers = userService.getAllowedSharedWithUsersForCurrentUser();
        List<UserGroup> allowedGroups = userService.getAllowedSharedWithGroupsForCurrentUser();
        List<User> editableUsers = allowedUsers.collect{it}
        List<UserGroup> editableGroups = allowedGroups.collect{it}

        if (isUpdate) {
            configurationInstance.deliveryOption.attach()
            if (configurationInstance.getShareWithUsers()) {
                allowedUsers.addAll(configurationInstance.getShareWithUsers())
                allowedUsers.unique { it.id }
            }
            if (configurationInstance.getShareWithGroups()) {
                allowedGroups.addAll(configurationInstance.getShareWithGroups())
                allowedGroups.unique { it.id }
            }
            if (configurationInstance.getExecutableByUser()) {
                editableUsers.addAll(configurationInstance.getExecutableByUser())
                editableUsers.unique { it.id }
            }
            if (configurationInstance.getExecutableByGroup()) {
                editableGroups.addAll(configurationInstance.getExecutableByGroup())
                editableGroups.unique { it.id }
            }
            configurationInstance?.deliveryOption?.sharedWith?.clear()
            configurationInstance?.deliveryOption?.sharedWithGroup?.clear()
            configurationInstance?.deliveryOption?.executableBy?.clear()
            configurationInstance?.deliveryOption?.executableByGroup?.clear()
        }
        DeliveryOption deliveryOption = configurationInstance.deliveryOption
        setUserUserGroup(sharedWith, deliveryOption, allowedGroups, allowedUsers, "addToSharedWith", "addToSharedWithGroup", configurationInstance)
        setUserUserGroup(executableBy, deliveryOption, editableGroups, editableUsers, "addToExecutableBy", "addToExecutableByGroup", configurationInstance)
        deliveryOption.report = configurationInstance
    }

    private void setUserUserGroup(List<String> sharedWith, deliveryOption, allowedGroups, allowedUsers, String addUser, String addUserGroup, ReportConfiguration configurationInstance) {
        if (sharedWith) {
            sharedWith.each { String shared ->
                if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                    UserGroup userGroup = UserGroup.get(Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                    if (userGroup && allowedGroups.find { it.id == userGroup.id }) {
                        deliveryOption."${addUserGroup}"(userGroup)
                    }
                } else if (shared.startsWith(Constants.USER_TOKEN)) {
                    User user = User.get(Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '')))
                    if (user && allowedUsers.find { it.id == user.id }) {
                        deliveryOption."${addUser}"(user)
                    }
                }
            }
        } else {
            deliveryOption."${addUser}"(configurationInstance.owner ?: userService.currentUser)
        }
    }

    @NotTransactional
    def getQueriesRCAId(QueryRCA queryRCA) {
        SuperQuery query = queryRCA?.query
        if (query) {
            if (query.queryType == QueryTypeEnum.SET_BUILDER) {
                query = (QuerySet) query
                String ids = query.queries.id.toString()
                return ids.substring(1, ids.length() - 1) // remove "[" and "]" from the list
            } else {
                return query.id
            }
        }
        return null
    }

    Date getYesterday() {
        return new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
    }

    def adjustTimeZoneOffsetForReports() {
        Map<String,TimeZoneEnum> distortedTimeZones = [:]
        TimeZoneEnum.values().each { it ->
            def timeZoneToday = DateUtil.getOffsetString(it?.timezoneId, new Date())
            def yesterdayTimeZone = DateUtil.getOffsetString(it?.timezoneId, getYesterday())
            if (timeZoneToday != yesterdayTimeZone) {
                distortedTimeZones[it.timezoneId] = it
            }
        }
        if(distortedTimeZones.isEmpty()){
            return
        }
        log.info("updating offsets of timezones "+distortedTimeZones)
        List reportsIds = []
        Sql sql = new Sql(reportExecutorService.getReportConnectionForPVR())
        try {
            sql.eachRow("select id,SCHEDULE_DATE from RCONFIG where SCHEDULE_DATE is not null") { row ->
                def x = JSON.parse(row.SCHEDULE_DATE)
                if (distortedTimeZones[x.timeZone.name]) {
                    reportsIds.push(row.id)
                }
            }
            log.debug('Impacted configurations due to timezone change')
            log.debug(reportsIds?.join(','))
            reportsIds.each {
                def rconfig = ReportConfiguration.get(it)
                if (!rconfig.executing) {
                    JSONObject timeObject = JSON.parse(rconfig.scheduleDateJSON)
                    String oldValue = timeObject.timeZone.offset
                    def tz = distortedTimeZones[timeObject.timeZone.name]
                    timeObject.timeZone.offset = tz?.getGmtOffset()
                    String newValue = timeObject.timeZone.offset
                    rconfig.scheduleDateJSON = timeObject.toString()
                    log.info("updating timezone offset for configuration with ID ${it} from old value ${oldValue} to newValue ${newValue}")
                    rconfig.save()
                } else {
                    log.warn('Missed updating configuration with Id '+it)
                }
            }
            sessionFactory.currentSession.flush()
        }
        catch(Exception e){
            log.error("Exception encountered while updating timezones for reports --> ${e}")
        } finally {
            sql?.close()
        }

    }

    @NotTransactional
    def getQueriesComplianceId(QueryCompliance queryCompliance) {
        SuperQuery query = GrailsHibernateUtil.unwrapIfProxy(queryCompliance?.query)
        if (query) {
            if (query.queryType == QueryTypeEnum.SET_BUILDER) {
                query = (QuerySet) query
                String ids = query.queries.id.toString()
                return ids.substring(1, ids.length() - 1) // remove "[" and "]" from the list
            } else {
                return query.id
            }
        }
        return null
    }

    Integer getPVALanguageId(String locale) {
        Connection connection = utilService.getReportConnection()
        Sql sql = new Sql(connection)
        try {
            Integer langId = null
            sql.call("{?= call F_GET_LANG_ID(?)}", [Sql.INTEGER, locale.toUpperCase()]) { result ->
                langId = result
            }
            return langId
        } catch (Exception e) {
            log.error("Error while fetch lang id from Mart : "+e.getMessage())
        } finally {
            sql?.close()
        }
    }
}

