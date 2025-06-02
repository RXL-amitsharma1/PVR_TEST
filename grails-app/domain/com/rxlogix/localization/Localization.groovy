package com.rxlogix.localization


import com.rxlogix.hibernate.EscapedILikeExpression
import grails.util.GrailsWebUtil
import grails.util.Holders
import grails.web.context.ServletContextHolder
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.core.io.Resource
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.support.WebApplicationContextUtils
import org.springframework.web.servlet.support.RequestContextUtils

class Localization implements Serializable {

    private static cache = new LinkedHashMap((int) 16, (float) 0.75, (boolean) true)
    private static long maxCacheSize = 128L * 1024L // Cache size in KB (default is 128kb)
    private static long currentCacheSize = 0L
    private static final missingValue = "\b" // an impossible value signifying that no such code exists in the database
    private static final keyDelimiter = missingValue
    private static long cacheHits = 0L
    private static long cacheMisses = 0L

    String code
    String locale
    Byte relevance = 0
    String text
    Date dateCreated = new Date()
    Date lastUpdated = new Date()

    static hasOne = [helpMessage: LocalizationHelpMessage]


    static mapping = {
        columns {
            code index: "localizations_idx"
            locale column: "loc"
        }
    }

    static constraints = {
        code(blank: false, size: 1..250)
        locale(size: 1..4, unique: 'code', blank: false, matches: "\\*|([a-z][a-z]([A-Z][A-Z])?)")
        relevance(validator: { val, obj ->
            if (obj.locale) obj.relevance = obj.locale.length()
            return true
        })
        text(blank: true, size: 0..2000)
        helpMessage nullable: true
    }

    def localeAsObj() {
        switch (locale.size()) {
            case 4:
                return new Locale(locale[0..1], locale[2..3])
            case 2:
                return new Locale(locale)
            default:
                return null
        }
    }

    static String decodeMessage(String code, Locale locale) {

        def key = code + keyDelimiter + locale.getLanguage() + locale.getCountry()
        def msg
        if (maxCacheSize > 0) {
            synchronized (cache) {
                msg = cache.get(key)
                if (msg) {
                    cacheHits++
                } else {
                    cacheMisses++
                }
            }
        }

        if (!msg) {
            Localization selectedLocalization
            Localization.withNewSession {
                def lst = Localization.findAll(
                        "from Localization as x where x.code = ?0 and x.locale in ('*', ?1, ?2) order by x.relevance desc",
                        [code, locale.getLanguage(), locale.getLanguage() + locale.getCountry()])
                msg = lst.size() > 0 ? lst[0].text : missingValue
                selectedLocalization = lst.size() > 0 ? lst[0] : null
            }

            if (maxCacheSize > 0) {
                synchronized (cache) {
                    if (selectedLocalization) {
                        Localization.withNewSession {
                            LocalizationHelpMessage helpMessage = LocalizationHelpMessage.findByLocalization(selectedLocalization)
                            if (helpMessage) {
                                msg = msg += " <span class='localizationHelpIcon glyphicon glyphicon-question-sign' data-id='${helpMessage.id}'></span>"
                            }
                        }
                    }
                    // Put it in the cache
                    def prev = cache.put(key, msg)
                    // Another user may have inserted it while we weren't looking
                    if (prev != null) currentCacheSize -= key.length() + prev.length()

                    // Increment the cache size with our data
                    currentCacheSize += key.length() + msg.length()

                    // Adjust the cache size if required
                    if (currentCacheSize > maxCacheSize) {
                        def entries = cache.entrySet().iterator()
                        def entry
                        while (entries.hasNext() && currentCacheSize > maxCacheSize) {
                            entry = entries.next()
                            currentCacheSize -= entry.getKey().length() + entry.getValue().length()
                            entries.remove()
                        }
                    }
                }
            }
        }
        //removing icon tag if we are showing message NOT on gsp page
        if(msg?.toString()?.contains("<span") && !Thread.currentThread().getStackTrace().find{it.toString().contains("_gsp")}){
            msg=msg.toString().substring(0, msg.toString().indexOf(" <span"))
        }

        return (msg == missingValue) ? null : msg
    }

    static getMessage(parameters) {
        def requestAttributes = RequestContextHolder.getRequestAttributes()
        def applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(ServletContextHolder.getServletContext())
        boolean unbindRequest = false

        // Outside of an executing request, establish a mock version
        if (!requestAttributes) {
            requestAttributes = GrailsWebUtil.bindMockWebRequest(applicationContext)
            unbindRequest = true
        }

        def messageSource = applicationContext.getBean("messageSource")
        def locale = RequestContextUtils.getLocale(requestAttributes.request)

        // What the heck is going on here with RequestContextUtils.getLocale() returning a String?
        // Beats the hell out of me, so just fix it!
        if (locale instanceof String) {

            // Now Javasoft have lost the plot and you can't easily get from a Locale.toString() back to a locale. Aaaargh!
            if (locale.length() >= 5) {
                locale = new Locale(locale[0..1], locale[3..4])
            } else {
                locale = new Locale(locale)
            }
        }

        def msg = messageSource.getMessage(parameters.code, parameters.args as Object[], parameters.default, locale)

        if (unbindRequest) RequestContextHolder.setRequestAttributes(null)
        if (parameters.encodeAs) {
            switch (parameters.encodeAs.toLowerCase()) {
                case 'html':
                    msg = msg.encodeAsHTML()
                    break

                case 'xml':
                    msg = msg.encodeAsXML()
                    break

                case 'url':
                    msg = msg.encodeAsURL()
                    break

                case 'javascript':
                    msg = msg.encodeAsJavaScript()
                    break

                case 'base64':
                    msg = msg.encodeAsBase64()
                    break
            }
        }

        return msg
    }

    static setError(domain, parameters) {
        def msg = Localization.getMessage(parameters)
        if (parameters.field) {
            domain.errors.rejectValue(parameters.field, null, msg)
        } else {
            domain.errors.reject(null, msg)
        }

        return msg
    }

    // Repopulates the localization table from the i18n property files
    static reload(Boolean updateFromLocalizationPropertyFile = false) {
//        Localization.executeUpdate("delete Localization")
        load(true)
        resetAll()
    }

    // Leaves the existing data in the database table intact and pulls in newly messages in the property files not found in the database
    static syncWithPropertyFiles() {
        load()
        resetAll()
    }

    static load(Boolean updateFromLocalizationPropertyFile = false) {
        List<Resource> propertiesResources = []
        LocalizationsPluginUtils.i18nResources?.each {
            propertiesResources << it
        }
        LocalizationsPluginUtils.allPluginI18nResources?.each {
            propertiesResources << it
        }


        Localization.log.debug("Properties files for localization : " + propertiesResources*.filename)

        propertiesResources.each {
            def locale = getLocaleForFileName(it.filename)
            Localization.loadPropertyFile(new InputStreamReader(it.inputStream, "UTF-8"), locale, updateFromLocalizationPropertyFile)
        }
        def size = Holders.config.getProperty('localizations.cache.size.kb', Integer)
        if (size != null && size >= 0 && size <= 1024 * 1024) {
            maxCacheSize = size * 1024L
        }
    }

    static loadPropertyFile(InputStreamReader inputStreamReader, locale, Boolean updateFromLocalizationPropertyFile = false) {
        def loc = locale ? locale.getLanguage() + locale.getCountry() : "*"
        def props = new Properties()
        def reader = new BufferedReader(inputStreamReader)
        try {
            props.load(reader)
        } finally {
            if (reader) reader.close()
        }

        def rec, txt
        def counts = [imported: 0, skipped: 0]
        Localization.withNewTransaction { transStatus ->
            props.stringPropertyNames().each { key ->
                rec = Localization.findByCodeAndLocale(key, loc)
                txt = props.getProperty(key)
                if (!rec) {
                    rec = new Localization([code: key, locale: loc, text: txt])
                    if (rec.validate()) {
                        rec.save()
                        counts.imported++
                    } else {
                        counts.skipped++
                    }
                } else if (updateFromLocalizationPropertyFile) {
                    rec.text = txt
                    if (rec.validate()) {
                        rec.save()
                        counts.imported++
                    } else {
                        counts.skipped++
                    }
                } else {
                    counts.skipped++
                }
            }
            // Clear the whole cache if we actually imported any new keys
            if (counts.imported > 0) {
                Localization.resetAll()
                transStatus.flush()
            }
        }
        return counts
    }

    static getLocaleForFileName(String fileName) {
        def locale = null

        if (fileName ==~ /.+_[a-z][a-z]_[A-Z][A-Z]\.properties$/) {
            locale = new Locale(fileName.substring(fileName.length() - 16, fileName.length() - 14), fileName.substring(fileName.length() - 13, fileName.length() - 11))
        } else if (fileName ==~ /.+_[a-z][a-z]\.properties$/) {
            locale = new Locale(fileName.substring(fileName.length() - 13, fileName.length() - 11))
        }

        locale
    }

    static resetAll() {
        synchronized (cache) {
            cache.clear()
            currentCacheSize = 0L
            cacheHits = 0L
            cacheMisses = 0L
        }
    }

    static resetThis(String key) {
        key += keyDelimiter
        synchronized (cache) {
            def entries = cache.entrySet().iterator()
            def entry
            while (entries.hasNext()) {
                entry = entries.next()
                if (entry.getKey().startsWith(key)) {
                    currentCacheSize -= entry.getKey().length() + entry.getValue().length()
                    entries.remove()
                }
            }
        }
    }

    static statistics() {
        def stats = [:]
        synchronized (cache) {
            stats.max = maxCacheSize
            stats.size = currentCacheSize
            stats.count = cache.size()
            stats.hits = cacheHits
            stats.misses = cacheMisses
        }

        return stats
    }

    static Object search(params) {
        def expr = "%${params.q}%".toString().toLowerCase()
        Localization.createCriteria().list(limit: params.max, order: params.order, sort: params.sort) {
            if (params.locale) {
                eq 'locale', params.locale
            }
            or {
                ilike 'code', expr
                ilike 'text', expr
            }
        }
    }

    static List<String> getUniqLocales() {
        return Localization.createCriteria().list {
            projections {
                distinct 'locale'
            }
        }.sort()
    }

    static namedQueries = {
        fetchByString { String search, Boolean helpOnly ->
            if (search) {
                or {
                    iLikeWithEscape('text', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('code', "%${EscapedILikeExpression.escapeString(search)}%")
                }
            }
            if (helpOnly) {
                createAlias('helpMessage', 'helpMessage', CriteriaSpecification.INNER_JOIN)
            }
        }
    }
}
