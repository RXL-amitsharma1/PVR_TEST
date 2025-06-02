package com.rxlogix.spotfire

import com.rxlogix.Constants
import com.rxlogix.commandObjects.SpotfireCommand
import com.rxlogix.config.BaseCaseSeries
import com.rxlogix.config.ExecutedCaseSeries
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.CaseSeries
import com.rxlogix.config.SpotfireSession
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.mapping.LmProductFamily
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.SpotfireUtil
import com.rxlogix.util.ViewHelper
import com.rxlogix.util.spotfire.HttpGet
import grails.core.GrailsApplication
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql
import groovy.transform.CompileStatic
import org.apache.commons.lang.StringEscapeUtils
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.joda.time.LocalDateTime

class SpotfireService {

    def dataSource_spotfire
    GrailsApplication grailsApplication
    def userService
    def utilService
    def grailsLinkGenerator
    def customMessageService
    def configurationService

    def getSpotFireConfig() {
        grailsApplication.config.spotfire
    }

    Set<String> fileNameCache = Collections.synchronizedSet(new HashSet<String>())

    List<String> getReportFiles() {
        List<String> titles = []
        try {
            if (dataSource_spotfire) {
                def sql = new Sql(dataSource_spotfire)
                try {
                    sql.eachRow("$spotFireConfig.query.sql.findReportFilesByTitle", [spotFireConfig.libraryFolder]) {
                        titles.push it.title
                    }
                } finally {
                    sql?.close()
                }
                titles
            }
        } catch (Throwable ex) {
            log.error("Error happened when querying Spotfire database", ex)
        }
        titles.sort()
    }


    List<Map> getReportFilesMapData() {
        List<Map> spoftfireFilesData = []
        User user = userService.getCurrentUser()

        // This list will contain the total spotfire files generated from Case Series and Periodic Report Configuration.
        List associatedSpotfireFilesNames = []
        associatedSpotfireFilesNames = populateSpotfireFileName(associatedSpotfireFilesNames, "EX_CASE_SERIES")
        associatedSpotfireFilesNames = populateSpotfireFileName(associatedSpotfireFilesNames, "EX_RCONFIG")
        List CaseSeriesSpotfireFiles = ExecutedCaseSeries.fetchDistinctSpotfireFileNames(user).list()
        List PeriodicReportSpotfireList = ExecutedPeriodicReportConfiguration.fetchDistinctSpotfireFileNames(user).list()
        try {
            if (dataSource_spotfire) {
                def sql = new Sql(dataSource_spotfire)
                try {
                    sql.eachRow("$spotFireConfig.query.sql.findReportFilesByTitle", [spotFireConfig.libraryFolder]) {
                        String encodedName = encodeFileName(it.title)
                        Date dateCreated = DateUtil.parseDate(it.dateCreated.toString(), "$spotFireConfig.date.parseFormat")
                        Date dateAccessed = DateUtil.parseDate(it.dateAccessed.toString(), "$spotFireConfig.date.parseFormat")
                        Date lastUpdated = DateUtil.parseDate(it.lastUpdated.toString(), "$spotFireConfig.date.parseFormat")
                        // Add the files from CaseSeriesSpotfireFiles and PeriodicReportSpotfireList
                        if (CaseSeriesSpotfireFiles.contains(it.title) || PeriodicReportSpotfireList.contains(it.title)) {
                            spoftfireFilesData.add([
                                    fileName       : it.title,
                                    dateCreated    : dateCreated?.format(DateUtil.DATEPICKER_UTC_FORMAT),
                                    lastUpdated    : lastUpdated?.format(DateUtil.DATEPICKER_UTC_FORMAT),
                                    executionTime  : it.executionTime,
                                    dateAccessed   : (dateCreated && dateAccessed && dateAccessed > dateCreated) ? dateAccessed.format(DateUtil.DATEPICKER_UTC_FORMAT) : "",
                                    encodedFileName: encodedName
                            ])
                        }
                        // Also add files that are not in associatedSpotfireFilesNames and on the basis of the user is blinded or not
                        if (!associatedSpotfireFilesNames.contains(it.title)) {
                            if ((it.title.startsWith("B_") && user.isBlinded) ||
                                    (it.title.startsWith("U_") && !user.isBlinded) ||
                                    (!it.title.startsWith("B_") && !it.title.startsWith("U_"))) {
                                spoftfireFilesData.add([
                                        fileName       : it.title,
                                        dateCreated    : dateCreated?.format(DateUtil.DATEPICKER_UTC_FORMAT),
                                        lastUpdated    : lastUpdated?.format(DateUtil.DATEPICKER_UTC_FORMAT),
                                        executionTime  : it.executionTime,
                                        dateAccessed   : (dateCreated && dateAccessed && dateAccessed > dateCreated) ? dateAccessed.format(DateUtil.DATEPICKER_UTC_FORMAT) : "",
                                        encodedFileName: encodedName
                                ])
                            }
                        }
                    }
                } finally {
                    sql?.close()
                }
            }
        } catch (Throwable ex) {
            log.error("Error happened when querying Spotfire database", ex)
        }
        return spoftfireFilesData
    }

    List populateSpotfireFileName(List associatedSpotfireFilesNames, String tableName) {
        Sql sql = null
        try {
            sql = new Sql(utilService.getReportConnectionForPVR())
            String query = "SELECT ASSOCIATED_SPOTFIRE_FILE FROM " + tableName + " WHERE ASSOCIATED_SPOTFIRE_FILE IS NOT NULL"
            sql.eachRow(query) { row ->
                associatedSpotfireFilesNames << row.ASSOCIATED_SPOTFIRE_FILE
            }
        } catch (Exception ex) {
            log.error("Error happened when querying associated Spotfire fileName", ex)
        } finally {
            sql?.close()
        }
        return associatedSpotfireFilesNames
    }

    String getUniqueName(String name) {
        if (dataSource_spotfire) {
            def sql = new Sql(dataSource_spotfire)
            try {
                int iter = 1
                String newName = name
                while (sql.rows("$spotFireConfig.query.sql.findReportFilesByTitle", [spotFireConfig.libraryFolder]).find { it.title == newName }) {
                    int maxSize = CaseSeries.constrainedProperties.generateSpotfire.maxSize
                    int postfixSize = String.valueOf(iter).length() + 1
                    if ((name.length() + postfixSize) > maxSize) {
                        name = name.substring(0, (maxSize - postfixSize) + 1)
                    }
                    newName = name + "-" + iter
                    iter++
                }
                return newName
            } finally {
                sql?.close()
            }
        }
        return name
    }

    def getUserByName(username) {
        try {
            if (dataSource_spotfire) {
                def sql = new Sql(dataSource_spotfire)
                def usernameFound
                try {
                    usernameFound = sql.firstRow("$spotFireConfig.query.sql.findByUserName", [username])
                } finally {
                    sql?.close()
                }
                if (usernameFound) return usernameFound.USER_NAME
            }
        } catch (Throwable ex) {
            log.error("Error happened when querying Spotfire database", ex)
        }

        null
    }

    String getHashedValue(String username) {
        if (!spotFireConfig.secureAccess) {
            log.debug("###### Secure Access for Spotfire has not been activated ##########")
            return username
        }
        return username?.encodeAsMD5()
    }

    String getActualValue(String hashedUsername) {
        if (!spotFireConfig.secureAccess) {
            log.debug("###### Secure Access for Spotfire has not been activated ##########")
            return hashedUsername
        }
        if (!hashedUsername) {
            return null
        }
        List<String> usersNamesList = User.createCriteria().list {
            projections {
                property("username")
            }
        }
        return usersNamesList.find { it.encodeAsMD5() == hashedUsername }
    }

    def produceReportParams(configurationBlock, configurationBlock2, emailMessage, fileName, templatePath, emailToUsers) {
        [
                openTitle              : "$spotFireConfig.analysis.openTitle",
                AnalysisPath           : "$spotFireConfig.analysisRoot/$templatePath",
                ConfigurationBlock     : configurationBlock,
                ConfigurationBlock2    : configurationBlock2,
                saveTitle              : "$spotFireConfig.analysis.saveTitle",
                LibraryPath            : "$spotFireConfig.libraryRoot/B_$fileName",
                LibraryPath2           : "$spotFireConfig.libraryRoot/U_$fileName",
                EmbedData              : true,
                DeleteExistingBookmarks: false,
                Recipients             : emailToUsers ?: [userService.getUser()?.email],
                emailTitle             : "$spotFireConfig.analysis.emailTitle",
                Subject                : "$spotFireConfig.emailSubject. File name: ${StringEscapeUtils.escapeXml(fileName)}",
                EmailMessage           : emailMessage
        ]
    }

    def invokeReportGenerationAPI(params) {
        def xml = SpotfireUtil.composeXmlBodyForTask(params)
        log.info("The request to spotfire server body is: \n" + xml)

        def resp
        if (Holders.config.getProperty('spotfire.automationNTLM', Boolean)) {
            log.info('NTLM will be executed')
            resp = SpotfireUtil.triggerJobOnNTML(
                    Holders.config.getProperty('spotfire.automationServer'),
                    Holders.config.getProperty('spotfire.automationPort', Integer),
                    Holders.config.getProperty('spotfire.automationProtocol'),
                    xml as String,
                    Holders.config.getProperty('spotfire.automationNTLMAcct'),
                    Holders.config.getProperty('spotfire.automationNTLMPass'))

        } else {
            log.info('HTTP(s) will be executed')
            resp = SpotfireUtil.triggerJob(
                    Holders.config.getProperty('spotfire.automationServer'),
                    Holders.config.getProperty('spotfire.automationPort', Integer),
                    Holders.config.getProperty('spotfire.automationProtocol'),
                    xml as String,
                    Holders.config.getProperty('spotfire.automationUsername'),
                    Holders.config.getProperty('spotfire.automationPassword'))

        }

        log.info("Response from Spotfire Server is: " + resp)

        resp
    }

//    TODO in case of CaseSeriesId what to do?? Need to check with Awais.
    private String messageToSend(String products, Date fromDate, Date endDate, Date asOfDate, BaseCaseSeries caseSeries, String fullFileName) {
        String emailMessage1 = customMessageService.getMessage('spotfire.email.message1', products)
        String emailMessage2 = ''
        if (grailsApplication.config.getProperty('safety.source').equals(Constants.PVCM)) {
            emailMessage2 = customMessageService.getMessage('spotfire.email.pvcm.message2', products, convertDateForEmailFormat(fromDate), convertDateForEmailFormat(endDate), convertDateForEmailFormat(asOfDate), caseSeries ? caseSeries.seriesName : '', fullFileName, DateUtil.StringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, userService.user?.preference?.timeZone))
        } else {
            emailMessage2 = customMessageService.getMessage('spotfire.email.message2', products, convertDateForEmailFormat(fromDate), convertDateForEmailFormat(endDate), convertDateForEmailFormat(asOfDate), caseSeries ? caseSeries.seriesName : '', fullFileName, DateUtil.StringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, userService.user?.preference?.timeZone))
        }
        emailMessage2 = emailMessage2.replaceAll(Constants.NA + " to " + Constants.NA, Constants.NA).replaceAll(Constants.NA + "から" + Constants.NA, Constants.NA)
        String emailMessage3 = customMessageService.getMessage('spotfire.email.message3', grailsLinkGenerator.link(controller: 'dataAnalysis', action: 'index', absolute: true))
        String emailMessage = emailMessage1 + " \n \n" + emailMessage2 + " \n \n" + emailMessage3
        return emailMessage
    }

    def buildConfigurationBlock(String productFamilyIds, Date fromDate, Date endDate, Date asOfDate, ExecutedCaseSeries caseSeries, String type, int n, String fileName, String generationFrom) {
        String caseSeriesOwner = caseSeries ? caseSeries.caseSeriesOwner : Constants.PVS_CASE_SERIES_OWNER
        Integer includeLockedVersion = caseSeries?.includeLockedVersion ? 0 : 1
        String serverURL = grailsApplication.config.spotfire.serverURL
        String fileViewURL = grailsLinkGenerator.link(controller: "dataAnalysis", action: "view", base: grailsApplication.config.getProperty("grails.appBaseURL"), params: [fileName: "${grailsApplication.config.spotfire.libraryRoot}/${fileName}"], absolute: true)
        String configBlock = (0..n).inject("") { str, i ->
            String fromDateStr = SpotfireCommand.defaultDate != fromDate ? convertDateString(fromDate) : ''
            String endDateStr = SpotfireCommand.defaultDate != endDate ? convertDateString(endDate) : ''
            String asOfDateStr = SpotfireCommand.defaultDate != asOfDate ? convertDateString(asOfDate) : ''

            "$str${type}_p${i * 4 + 1}.prod_family=$productFamilyIds;" +
                    "${type}_p${i * 4 + 2}.start_date={\"${fromDateStr}\"};" +
                    "${type}_p${i * 4 + 3}.end_date={\"${endDateStr}\"};" +
                    "${type}_p${i * 4 + 4}.as_of_date={\"${asOfDateStr}\"};"
        }
        configBlock += ((n * 4 + 5)..(n * 4 + 4 + 19 + (type == 'drug' ? 5 : 0))).inject("") { str, i ->
            "$str${type}_p${i}.case_list_id={\"${caseSeries?.id ?: "-1"}\"};"
        }
        configBlock += "server_url={\"${grailsLinkGenerator.link(controller: "report", action: "exportSingleCIOMS", absolute: true, params: [caseNumber: ''])}\"};" + "server_url_ip={\"${serverURL}\"};"
        configBlock += "FlagOpenCase={\"${includeLockedVersion}\"};"
        configBlock += "caseSeriesOwner={\"${caseSeriesOwner}\"};"
        configBlock = "File_name={\"${StringEscapeUtils.escapeXml(fileName)}\"};" + configBlock
        configBlock = "LabelChange={\"PVA\"};" + configBlock
        // add code for who flag and who family ids
        // sending default values for now as this needs to be changed later
        configBlock = "FlagWho={\"${0}\"};" + configBlock
        configBlock = "WhoFamilyId={\"${""}\"};" + configBlock
        configBlock = "studycaseflag={\"${caseSeries?.includeAllStudyDrugsCases ? 1 : 0}\"};" + configBlock
        configBlock = "SFURLFlag={\"${fileViewURL}\"};" + configBlock
        configBlock = "FILEGENERATEDFROM={\"${generationFrom}\"};" + configBlock
        if (fileName.startsWith("B_")) {
            configBlock = "flagblind={\"null\"};" + configBlock
        } else {
            configBlock = "flagblind={\"default\"};" + configBlock
        }

        if (caseSeries) {
            String asOfVersion = ViewHelper.getMessage(caseSeries.evaluateDateAs.i18nKey)
            if (caseSeries.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF) {
                asOfVersion = asOfVersion + " " + (caseSeries.asOfVersionDate?.format(DateUtil.DATEPICKER_FORMAT) ?: "")
            }
            String dateRange = configurationService.getDateRangeValue(caseSeries?.executedCaseSeriesDateRangeInformation, Locale.getDefault())
            String url = ""
            if (caseSeries.executedGlobalQuery && grailsApplication.config.grails.appBaseURL)
                url = grailsApplication.config.grails.appBaseURL + "/query/view/" + caseSeries.executedGlobalQuery.id
            configBlock = "PVRCRITERIA={\"{##criteria##:[{##Case_Series_Name##:##${caseSeries.reportName ?: ""}##," +
                    "##Product_Dictionary##:##${ViewHelper.getDictionaryValues(caseSeries, DictionaryTypeEnum.PRODUCT)}##," +
                    "##Study_Dictionary##:##${ViewHelper.getDictionaryValues(caseSeries, DictionaryTypeEnum.STUDY)}##," +
                    "##Event_Dictionary##:##${ViewHelper.getDictionaryValues(caseSeries, DictionaryTypeEnum.EVENT)}##," +
                    "##Include_All_Study_Drugs_Cases##:##${caseSeries.includeAllStudyDrugsCases ? 1 : 0}##," +
                    "##Exclude_Follow_Up##:##${caseSeries.excludeFollowUp ? 1 : 0}##," +
                    "##Include_Locked_Versions_Only##:##${caseSeries.includeLockedVersion ? 1 : 0}##," +
                    "##Exclude_Non_Valid_Cases##:##${caseSeries.excludeNonValidCases ? 1 : 0}##," +
                    "##Limit_to_Suspect_Product##:##${caseSeries.suspectProduct ? 1 : 0}##," +
                    "##Query_Name##:##${caseSeries.executedGlobalQuery?.name ?: ""}##," +
                    "##Query_URL##:##${url ?: ""}##," +
                    "##Date_Range_Type##:##${ViewHelper.getMessage(caseSeries.dateRangeType?.i18nKey)}##," +
                    "##Evaluate_On##:##${asOfVersion}##," +
                    "##Date_Range##:##${dateRange}##," +
                    "##SFURLFlag##:##${fileViewURL}##," +
                    "##Ana_File_Name##:##${fileName}##}]}\"};" + configBlock
        }
        return configBlock
    }

    Map<String, String> generateReportParams(Set<String> productFamilyIds, Date fromDate, Date endDate, Date asOfDate, Long caseSeriesId, String type, String fullFileName, String generationFrom) {
        def formattedProductFamilyIds = "{\"${productFamilyIds.join(",")}\"}"
        ExecutedCaseSeries caseSeries = ExecutedCaseSeries.read(caseSeriesId)
        def configurationBlock = buildConfigurationBlock(formattedProductFamilyIds, fromDate, endDate, asOfDate, caseSeries, type, 18, "B_${fullFileName}", generationFrom)
        def configurationBlock2 = buildConfigurationBlock(formattedProductFamilyIds, fromDate, endDate, asOfDate, caseSeries, type, 18, "U_${fullFileName}", generationFrom)
        String currentLang = userService.getCurrentUser()?.preference?.locale?.language ?: 'en'
        String productFamilyNames = productFamilyIds ? LmProductFamily.getAllNamesForIds(productFamilyIds.toList(), currentLang).join(" , ") : ''
        List emailToUsers = caseSeries?.executedDeliveryOption?.emailToUsers
        String productNames = getNameFieldFromJson(caseSeries?.productSelection)
        String emailMessage = messageToSend(productNames ?: productFamilyNames, fromDate, endDate, asOfDate, caseSeries, fullFileName)
        type = (type == 'drug') ? spotFireConfig.drugPath : (type == 'vacc') ? spotFireConfig.vaccPath : spotFireConfig.pmprPath
        produceReportParams(configurationBlock, configurationBlock2, emailMessage, fullFileName, type, emailToUsers)
    }

    String getNameFieldFromJson(jsonString) {
        def prdName = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = MiscUtil.parseJsonText(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                def prdVal = jsonObj.find { k, v ->
                    v.find { it.containsKey('name') || it.containsKey('genericName') }
                }?.value.findAll {
                    it.containsKey('name') || it.containsKey('genericName')
                }.collect { it.name ? it.name : it.genericName }

                prdName = prdVal ? prdVal.sort().join(',') : ""
            }
        }
        prdName
    }

    def logout(String username) {
        CloseableHttpClient httpclient = HttpClients.createDefault()
        def spotfireServver = Holders.config.getProperty('spotfire.server')
        def spotfirePort = Holders.config.getProperty('spotfire.port', Integer)
        def protocol = Holders.config.getProperty('spotfire.protocol')
        HttpGet httpGet = new HttpGet("$protocol://$spotfireServver:$spotfirePort/spotfire/logout.jsp")
        CloseableHttpResponse response1 = httpclient.execute(httpGet)
        response1.close()

        deleteSession(username)
    }

    @Transactional
    def deleteSession(String username) {
        List<SpotfireSession> sessions = SpotfireSession.findAllByUsername(username)
        if (sessions) {
            sessions.each {
                it.deleted = true
                it.save()
            }
        }
    }

    def findFileNameInDatabase(fileName) {
        fileName ? getReportFiles().any { it.toLowerCase() == fileName.toLowerCase() } : false
    }

    def findFileNameInCache(fileName) {
        fileNameCache.any { it.toLowerCase() == fileName.toLowerCase() }
    }

    def invalidFileNameLength(fileName) {
        fileName ? fileName.length() < 1 || fileName.length() > spotFireConfig.fileNameLimit : true
    }

    def fileNameExist(fileName) {
        (findFileNameInCache(fileName) || findFileNameInDatabase(fileName))
    }

    @Transactional
    void addAuthToken(String authToken, String username, String fullName, String email) {
        SpotfireSession spotfireSession = SpotfireSession.findByToken(authToken)
        if (!spotfireSession) {
            spotfireSession = new SpotfireSession(token: authToken,
                    username: username,
                    fullName: fullName,
                    email: email, timestamp: new Date())
        }
        spotfireSession.deleted = false
        spotfireSession.timestamp = new Date()
        spotfireSession.save()
    }

    SpotfireSession getSpotfireSessionInfo(String authToken) {
        SpotfireSession.findByTokenAndDeleted(authToken, false)
    }

    @Transactional
    boolean expireSpotfireSession() {
        SpotfireSession.findAllByDeleted(false).each { spotfireSession ->
            LocalDateTime then = LocalDateTime.fromDateFields(spotfireSession.timestamp)
            LocalDateTime now = new LocalDateTime()

            if (now.isAfter(then.plusSeconds(Holders.config.getProperty('spotfire.session_interval', Integer)))) {
                //expired
                spotfireSession.deleted = true
                spotfireSession.save()
            }
        }
    }

    def reserveFileName(String fileName) {
        synchronized (this) {
            fileNameCache.add(fileName.toLowerCase())
        }

        Timer timer = new Timer(true)
        def cacheCleanInterval = spotFireConfig.fileNameCachedPeriod ?: 3600
        timer.schedule(new CacheUpdateTimerTask(fileName, timer), cacheCleanInterval * 1000)
    }

    def updateCache(String fileName) {
        fileNameCache.remove(fileName.toLowerCase())
    }

    class CacheUpdateTimerTask extends TimerTask {
        String fileName
        Timer timer

        CacheUpdateTimerTask(String fileName, Timer timer) {
            super()

            if (fileName)
                this.fileName = fileName
            else
                throw new IllegalArgumentException("File name can not be empty")

            this.timer = timer
        }

        void run() {
            updateCache(this.fileName)
            timer.cancel()
        }
    }

    private String convertDateString(Date date) {
        return date?.format("$spotFireConfig.date.xmlFormat")
    }

    private String convertDateForEmailFormat(Date date) {
        if (date == SpotfireCommand.defaultDate) return Constants.NA
        return date?.format(DateUtil.DATEPICKER_FORMAT) ?: Constants.NA
    }

    @NotTransactional
    @CompileStatic
    List appendLingualSuffix(List<LmProductFamily> items, String locale) {
        Map<String, String> suffixMap = [
                'en': " (J)",
                'ja': " (E)"
        ]
        boolean isDuplicate = false
        List uniqueItems = []
        List duplicateItems = []
        List results = []

        // To filter out the duplicate items
        for (item in items) {
            if ((item.productFamilyId + item.name) in uniqueItems) {
                duplicateItems << item.productFamilyId + item.name
            } else {
                uniqueItems << item.productFamilyId + item.name
            }
        }

        // To add appropriate lingual suffix
        for (item in items) {
            isDuplicate = ((item.productFamilyId + item.name) in duplicateItems) && (item.lang != locale)
            results.add([id: item.productFamilyId, text: isDuplicate ? item.name.concat(suffixMap[locale]) : item.name])
        }

        return results
    }

    String encodeFileName(String fileName) {
        try {
            return Base64.encoder.encodeToString(fileName.getBytes("UTF-8"))
        } catch (Exception ex) {
            log.error("Unable to encode filename : ${fileName}", ex)
            return fileName
        }
    }

    String decodeFileName(String encodedName) {
        try {
            return new String(Base64.decoder.decode(encodedName), "UTF-8")
        } catch (Exception ex) {
            log.error("Unable to decode filename : ${encodedName}", ex)
            return encodedName
        }
    }
}
