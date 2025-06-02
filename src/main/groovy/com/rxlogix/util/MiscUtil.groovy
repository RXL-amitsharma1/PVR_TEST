package com.rxlogix.util

import com.rxlogix.Constants
import com.rxlogix.config.BaseDeliveryOption
import com.rxlogix.config.CaseSeries
import com.rxlogix.config.DistributionChannel
import com.rxlogix.config.EmailConfiguration
import com.rxlogix.config.IcsrProfileConfiguration
import com.rxlogix.config.IcsrTemplateQuery
import com.rxlogix.config.InboundCompliance
import com.rxlogix.config.QueryCompliance
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.config.ReportResult
import com.rxlogix.config.ReportTemplate
import com.rxlogix.config.TemplateQuery
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.jasperserver.FileResource
import com.rxlogix.jasperserver.Query
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.mapping.AllowedAttachment
import com.rxlogix.config.BalanceMinusQuery
import com.rxlogix.config.BmQuerySection
import com.rxlogix.repo.RepoFileResource
import grails.converters.JSON
import grails.util.BuildSettings
import grails.util.Environment
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang.SystemUtils
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.docx4j.fonts.PhysicalFonts
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.json.JSONObject
import com.rxlogix.dynamicReports.reportTypes.CustomSQLReportBuilder
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import java.awt.Color
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.text.DateFormat
import java.text.Format
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.logging.Logger
import com.rxlogix.mapping.SafetyCalendar

import java.util.regex.Matcher
import java.util.regex.Pattern

class MiscUtil {
    private static final Logger LOG = Logger.getLogger(MiscUtil.class.getName());
    static Map isoCodesMap = [:]

    // Disallowed patterns: <script>, <iframe>, <object>, <embed>, javascript: in URLs, data: URLs, and inline event handlers
    static final String DISALLOWED_PATTERN = "(?i)(" +
            "<.*?>" +                                         // blocks any HTML tag
            "|<script.*?>.*?</script>" +                      // blocks <script> tags
            "|<iframe.*?>.*?</iframe>" +                      // blocks <iframe> tags
            "|<object.*?>.*?</object>" +                      // blocks <object> tags
            "|<embed.*?>.*?</embed>" +                        // blocks <embed> tags
            "|<style.*?>.*?</style>" +                        // blocks <style> tags
            "|<meta.*?>" +                                    // blocks <meta> tags
            "|<svg.*?>.*?</svg>" +                            // blocks <svg> tags
            "|<form.*?>.*?</form>" +                          // blocks <form> tags
            "|<img.*?>" +                                     // blocks <img> tags
            "|<[^>]*\\s+on\\w+\\s*=\\s*['\"].*?['\"]" +        // blocks inline event handlers like onclick=
            "|javascript:" +                                  // blocks javascript: pseudo-protocol
            "|data:" +                                        // blocks data: URIs
            ")";

    def static md5ChecksumForFile(String filePath) {
        if (!filePath) null
        FileInputStream fis = new FileInputStream(new File(filePath));
        def checksum = md5ChecksumForFile fis
        fis.close()

        checksum
    }

    def static md5ChecksumForFile(FileInputStream fis) {
        DigestUtils.md5Hex(fis);
    }

    static Map getObjectNonSyntheticProperties(Object o) {
        if (!o) {
            return [:]
        }
        return o.getClass().declaredFields.findAll {
            !it.synthetic && it.name != '$defaultDatabindingWhiteList'
        }.collectEntries {
            [(it.name): (o."${it.name}")]
        }
    }

    static Map getObjectProperties(Object o, List<String> includeFields = []) {
        List<PropertyValue> metaProps = getObjectsFields(o, includeFields)
        Map<String, Object> props = new LinkedHashMap<String, Object>(metaProps.size());
        for (PropertyValue mp : metaProps) {
            try {
                props.put(mp.getName(), mp.getValue());
            } catch (Exception e) {
                LOG.throwing(o.getClass().getName(), "getProperty(" + mp.getName() + ")", e);
            }
        }
        return props;
    }

    static List<PropertyValue> getObjectsFields(Object o, List<String> includeFields = []) {
        List<PropertyValue> metaProps = DefaultGroovyMethods.getMetaPropertyValues(o)
        if (DomainClassArtefactHandler.isDomainClass(o.class, true)) {
            List nonTransients = getPersistentProperties(o)
            metaProps.removeAll { !(it.name in nonTransients) }
        }
        else {
            metaProps.removeAll { it.name == 'all' }
        }
        if (includeFields) {
            metaProps.retainAll {
                it.name in includeFields
            }
        }
        return metaProps
    }


    public static <T> List<T> getPagedResult(List<T> original, int offset, int max) {
        Integer totalCount = original?.size()
        if (!totalCount || totalCount < offset) {
            return []
        }
        if (offset + max > totalCount) {
            return original.subList(offset, totalCount)
        }
        return original.subList(offset, offset + max)
    }

    public static def getBean(String name) {
        return Holders.applicationContext.getBean(name)
    }

    public static boolean validateScheduleDateJSON(String scheduleDateJSON) {
        // PVR-2945: "Save and run" button calls populateModel, trying to use scheduleDateJSON before we validate/save it.
        if(!validateRecurrence(scheduleDateJSON)){
            return false
        }
        if (isScheduleDateJSONEmpty(scheduleDateJSON)) {
            return false
        }
        return true
    }

    public static boolean validateRecurrence(String recurrence) {
        // PVR-2945: "Save and run" button calls populateModel, trying to use scheduleDateJSON before we validate/save it.
        if (recurrence.contains("FREQ=WEEKLY") && recurrence.contains("BYDAY=;")) {
            return false
        }

        return true
    }

    public static boolean isScheduleDateJSONEmpty(String scheduleDateJSON) {
        try {
            JSONObject timeObject = JSON.parse(scheduleDateJSON)
            if (timeObject.startDateTime && timeObject.timeZone && timeObject.recurrencePattern) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtil.JSON_DATE)
                simpleDateFormat.setLenient(false)
                simpleDateFormat.parse(timeObject.startDateTime.toString())
            } else {
                return true
            }
        }
        catch (ParseException e) {
            return true
        }
        return false
    }

    public static List<Map> getEventDictionaryValues(String usedEventSelection) {
        List<Map> result = PVDictionaryConfig.EventConfig.levels.collect { [:] }
        parseDictionary(result, usedEventSelection)
        return result
    }

    public static List<Map> getProductDictionaryValues(String productSelection) {
        List<Map> result =  PVDictionaryConfig.ProductConfig.columns.collect { [:] }
        parseDictionary(result, productSelection)
        return result
    }

    static List<Map> addProductDictionaryValues(List<Map> result, String productSelection) {
        parseDictionary(result, productSelection)
        return result
    }

    public static List<Map> getStudyDictionaryValues(String studySelection) {
        List<Map> result = PVDictionaryConfig.StudyConfig.columns.collect { [:] }
        parseDictionary(result, studySelection)
        return result
    }

    public static parseDictionary(List<Map> result, String dictionarySelection) {
        if (dictionarySelection) {
            Map values = parseJsonText(dictionarySelection)
            values.each { k, v ->
                if (k != "100") {
                    int level = k.toInteger()
                    v.each {
                        // result[level - 1].add(it["id"])
                        result[level - 1].putIfAbsent(it["id"], it["name"])
                    }
                }
            }
        }
    }

    public static List<String> getExceptionMessage(Exception e) {
        List<String> exceptionMessageList = []
        String message = e?.localizedMessage?.length() > 254 ? e?.localizedMessage?.substring(0, 254) : e.localizedMessage
        StringWriter sw = new StringWriter()
        e.printStackTrace(new PrintWriter(sw))
        String exceptionAsString = sw.toString()
        if (!message) {
            message = exceptionAsString?.length() > 254 ? exceptionAsString?.substring(0, 254) : exceptionAsString
        }
        exceptionMessageList.add(message)
        exceptionMessageList.add(exceptionAsString)
        exceptionMessageList
    }

    static def parseJsonText(String json) {
        def object = new JsonSlurper().parseText(json)
//        if (object instanceof LazyMap) { TODO remove later //http://stackoverflow.com/questions/37864542/jenkins-pipeline-notserializableexception-groovy-json-internal-lazymap
//            return new LinkedHashMap<>(object)
//        }
        return object
    }
    static def parseJsonTextForQuery(String json) {
        def object = new JsonSlurper().parseText(json.replace("\\","\\\\"))
        return object
    }
static String marshal(def source) {
        StringWriter writer = new StringWriter()
        JAXBContext context = JAXBContext.newInstance(source.class)
        Marshaller marshaller = context.createMarshaller()
        marshaller.marshal(source, writer)
        return writer.toString()
    }

    static <T> T unmarshal(InputStream entityStream, Class<T> declaredType) {
        JAXBContext context = JAXBContext.newInstance(declaredType)
        Unmarshaller marshaller = context.createUnmarshaller()
        return marshaller.unmarshal(entityStream)
    }

    static List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<String, Object>(columns);
            for (int i = 1; i <= columns; ++i) {
                row.put(md.getColumnName(i), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }

    static String escapeExcelSheetName(String sheetName) {
        return sheetName ? sheetName.replaceAll('\\[', '(')
                .replaceAll('\\]', ')')
                .replaceAll('\\:', '-')
                .replaceAll("(\\'|\\*|\\?|/|\\\\)", '_') : sheetName
    }

    static String splitCamelCase(String s) {
        return s.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ), " ")
    }

    static Boolean isValidPattern(String term) {
        !(term =~ /[\^\$|?*+()=]/)
    }

    public static getPersistentProperties(theInstance) {
        def grailsDomainClassMappingContext = getBean("grailsDomainClassMappingContext")
        PersistentEntity entityClass = grailsDomainClassMappingContext.getPersistentEntity(theInstance.class.name)
        return entityClass.persistentProperties*.name
    }

    static String matchCSVPattern(String term) {
        term?.matches(Constants.CSV_INPUT_PATTERN_REGEX) ? "'" + term : term
    }

    static def initIsoCodes () {
        Locale.getISOCountries().each { country ->
            def locale = new Locale("", country)
            isoCodesMap.put(locale.getISO3Country().toUpperCase(), country)
        }
    }

    static String iso3CountryCodeToCountryName(String iso3CountryCode) {
        def iso2CountryCode = isoCodesMap.(iso3CountryCode.toUpperCase())
        return isoCountryCodeToCountryName(iso2CountryCode)
    }


    static String isoCountryCodeToCountryName(String isoCountryCode) {
        Locale locale = new Locale("", isoCountryCode)
        return locale.getDisplayCountry()
    }

    static boolean isLocalEnv() {
        return (Environment.current in [Environment.DEVELOPMENT, Environment.TEST] && BuildSettings.GRAILS_APP_DIR_PRESENT)
    }

    static boolean isLocalTestEnv() {
        return (Environment.current == Environment.TEST && BuildSettings.GRAILS_APP_DIR_PRESENT)
    }

    static Object unwrapProxy(Object obj) {
        return GrailsHibernateUtil.unwrapIfProxy(obj)
    }

    public static String generateRandomName() {
        return UUID.randomUUID().toString().replaceAll('-', '')
    }

    public static List<String> appendsToList(List<String> items) {
        int i = 1
        items = items?.collect {
            if (!it.allWhitespace)
                "#" + (i++) + " " + it?.trim()
        }
        return items
    }

    // Call this method once at the start of the application for WordToHtml Doc4jx
    public static loadFontsRegexSpecificToOs() {
        //Taken from PhysicalFonts.regex for fonts load error (https://stackoverflow.com/questions/72033977/upgrading-to-11-4-6-causes-java-lang-noclassdeffounderror-could-not-initialize)
        if (SystemUtils.OS_NAME?.toLowerCase()?.contains('mac')) {
            PhysicalFonts.regex = ".*(Courier New|Arial|Times New Roman|Comic Sans|Georgia|Impact|Lucida Console|Lucida Sans Unicode|Palatino Linotype|Tahoma|Trebuchet|Verdana|Symbol|Webdings|Wingdings|MS Sans Serif|MS Serif).*"
        } else if (SystemUtils.OS_NAME?.toLowerCase()?.contains('windows')) {
            PhysicalFonts.regex = ".*(calibri|cour|arial|times|comic|georgia|impact|LSANS|pala|tahoma|trebuc|verdana|symbol|webdings|wingding).*"
        }
    }

    public static def getReadableStartDateTime(String startDateTimeStr){
        startDateTimeStr = startDateTimeStr.replaceAll("-\\d{2}:\\d{2}", "")
        Format dateFormatter = new SimpleDateFormat(Constants.DateFormat.SCHEDULE_DATE)
        Date date = (Date) dateFormatter.parseObject(startDateTimeStr)
        DateFormat dateFormat = new SimpleDateFormat(Constants.DateFormat.REGULAR_DATETIME)
        return dateFormat.format(date)
    }

    public static def getReadableTimeZone(def timezone){
        return timezone.name + " - " + timezone.text
    }

    public static def getReadableRecurrencePattern(def recurrencePattern){
        if (recurrencePattern && (Constants.Scheduler.RUN_ONCE == recurrencePattern)) {
            return "FREQ: NONE"
        }
        def patternParts = recurrencePattern.split(';')
        int interval = 0, count = 0
        String frequency, endDate


        patternParts.each { part ->
            def key = part.split('=')[0]
            def value = part.split('=')[1]

            switch (key) {
                case "INTERVAL":
                    interval = value as int
                    break

                case "COUNT":
                    count = value as int
                    break

                case "FREQ":
                    frequency = value.toString().toLowerCase()
                    break

                case "UNTIL":
                    endDate = value
                    break
            }
        }

        String message = "The configuration will be executed $frequency with an interval of $interval and this recurring process will "
        String untilMessage

        if (endDate){
            SimpleDateFormat dateFormatter = new SimpleDateFormat(Constants.DateFormat.BASIC_DATE)
            def d = dateFormatter.parse(endDate)
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DateFormat.REGULAR_DATE)
            dateFormat.applyPattern(Constants.DateFormat.REGULAR_DATE) // Adjusting the pattern
            def formattedDate = dateFormat.format(d)

            untilMessage = "continue until $formattedDate."
        }

        String countMessage = (count == 0 ? "never end" : "occur a total of $count time(s).")
        message += endDate ? untilMessage : countMessage

        // Adding recurrencePattern for complicated patterns
        message += "  (${recurrencePattern}) "

        return message
    }

    static String colorToHex(Color color) {
        int red = color.getRed()
        int green = color.getGreen()
        int blue = color.getBlue()

        return String.format("#%02X%02X%02X", red, green, blue)
    }


    static Long getReasonOfDelayId(Long reportResultId) {
        Long currentId = reportResultId
        while (currentId != null) {
            ReportResult rodResult = ReportResult.findById(currentId)
            if (rodResult.parentId == null)
                break
            currentId = rodResult.parentId
        }

        return currentId
    }

    static Boolean validateStringSize(String str, int maxBytes){
        return (str.getBytes().length <= maxBytes)
    }

    /**
     * Validates the byte size of a specified field in the domain instance.
     *
     * This method ensures the field's byte size does not exceed the maximum limit,
     * preventing Oracle errors related to VARCHAR size constraints. If exceeded,
     * it rejects the field with an appropriate error message.
     *
     * @param theInstance The domain instance containing the field.
     * @param fieldName The name of the field to validate.
     * @param fieldLabel User-friendly label for the field, used in error messages.
     * @param maxBytes The maximum allowed byte size.
     * @throws ValidationException if the byte size exceeds the limit.
     */
    static void validateStringFieldSize(def theInstance, String fieldName, String fieldLabel, int maxBytes){
        String str = theInstance."${fieldName}"
        if(str && str != '' && !validateStringSize(str, maxBytes)){
            theInstance.errors.rejectValue(fieldName, "app.warn.maxBytes.exceeded", [fieldLabel, maxBytes] as Object[], 'Max bytes limit exceeded')
            throw new ValidationException("Max bytes limit exceeded", theInstance.errors)
        }
    }

    //Alternate of Eval.x for speeding up evaluation of object property.
    public static def evaluate(Object obj, String propertyPath) {
        def result = obj
        boolean nullSafe = false
        boolean returnCollection = false
        propertyPath.split('\\.').find { String prop ->
            //to handle nullsafe and * operator handling
            if (prop.endsWith('?')) {
                prop = prop.substring(0, prop.length() - 1)
                nullSafe = true
            }
            if (prop.endsWith('*')) {
                prop = prop.substring(0, prop.length() - 1)
                nullSafe = true
                returnCollection = true
            }
            //logic to handle [0] expressions
            int lb = prop.indexOf("[")
            if (lb > 0) {
                String listName = prop.substring(0, lb)
                int index = prop.substring(lb + 1, prop.length() - 1) as Integer
                result = result."${listName}"[index]
            } else {
                result = result."${prop}"
            }
            //to break loop once result to null
            if (nullSafe && result == null) {
                return true
            } else if (lb > 0 && returnCollection) {
                result = [result]
            }
            nullSafe = false
            returnCollection = false
            return false
        }
        return result
    }

    public static void linkFixedTemplate(String fileName, String relativeFilePath, ReportTemplate template){
        InputStream resourceStream = CustomSQLReportBuilder.class.getResourceAsStream(relativeFilePath + '/' + fileName)
        byte[] data = resourceStream.bytes
        FileResource fileResource = new FileResource(
                name: "${template.name}.jrxml",
                label: template.name,
                fileType: FileResource.TYPE_JRXML)
        RepoFileResource fixedTemplate = new RepoFileResource()
        fixedTemplate.copyFromClient(fileResource)
        fixedTemplate.save()
        template.fixedTemplate = fixedTemplate
        template.useFixedTemplate = true
        template.fixedTemplate.name = fileName
        template.fixedTemplate.data = data
    }

    static boolean validateContent(String body) {
        if (!body) { // Groovy idiomatic null or empty check
            LOG.info("Validation skipped: content is null or empty.")
            return true
        }

        Pattern disallowedPattern = Pattern.compile(DISALLOWED_PATTERN, Pattern.CASE_INSENSITIVE)
        Matcher disallowedMatcher = disallowedPattern.matcher(body)

        if (disallowedMatcher.find()) {
            LOG.info("Validation failed: body contains disallowed elements")
            return false
        }

        LOG.info("Validation passed: no disallowed content found.")
        return true
    }

}
