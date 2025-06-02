import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
import grails.util.Environment
import ch.qos.logback.core.util.FileSize
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

import java.nio.charset.Charset

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

// See http://logback.qos.ch/manual/groovy.html for details on configuration
if (Environment.getCurrent() == Environment.PRODUCTION) {
    instanceName = InetAddress.getLocalHost().getHostName()
    logsDir = "${System.getProperty('logs.folder') ?: (System.getProperty('catalina.base') + '/logs')}"
    logFileName = "${logsDir}/pvreports-${instanceName ?: ''}"

    appender("APP", RollingFileAppender) {
            encoder(PatternLayoutEncoder) {

                pattern = "%date %replace(- M:%X{ipAddress} - U:%X{user} ){'- M: - U: ', '- M: APPServer '}%highlight(%level)  %logger.%M - %L - %msg%n"
            }
            rollingPolicy(SizeAndTimeBasedRollingPolicy) {
                fileNamePattern = "${logFileName}-%d{yyyy-MM-dd}.%i.log"
                maxHistory = 60
                maxFileSize = FileSize.valueOf("2GB")
            }
    }

}

if(Environment.getCurrent() == Environment.DEVELOPMENT || Environment.getCurrent() == Environment.TEST){
    appender('APP', ConsoleAppender) {
        encoder(PatternLayoutEncoder) {
            charset = Charset.forName('UTF-8')
            pattern = "%date %replace(- M:%X{ipAddress} - U:%X{user} ){'- M: - U: ', '- M: APPServer '}%highlight(%level)  %logger.%M - %L - %msg%n"
        }
    }
}

logger('grails.plugins.mail', DEBUG, ['APP'], false)
logger('com.sun.mail', DEBUG, ['APP'], false)
logger('com.rxlogix.ReportFieldService', DEBUG, ['APP'], false)
logger('org.springframework.cache', DEBUG, ['APP'], false)
logger('com.rxlogix.liquibase.MigrationCallbacks', DEBUG, ['APP'], false)

logger('reports.BootStrap', INFO, ['APP'], false)
logger('com.rxlogix', INFO, ['APP'], false) // all our services
logger('com.rxlogix.util.marshalling', INFO, ['APP'], false)

logger('org.grails.web.servlet', ERROR, ['APP'], false) // controllers
logger('org.grails.web.pages', ERROR, ['APP'], false) // GSP
logger('org.grails.web.sitemesh', ERROR, ['APP'], false) // layouts
logger('org.grails.web.mapping.filter', ERROR, ['APP'], false) // URL mapping
logger('org.grails.web.mapping', ERROR, ['APP'], false) // URL mapping
logger('org.grails.commons', ERROR, ['APP'], false) // core / classloading
logger('org.grails.plugins', ERROR, ['APP'], false) // plugins
logger('org.grails.orm.hibernate', ERROR, ['APP'], false) //// hibernate integration
logger('org.springframework', ERROR, ['APP'], false)
logger('org.hibernate', ERROR, ['APP'], false)
logger('net.sf.ehcache.hibernate', ERROR, ['APP'], false)
logger('com.rxlogix.UserService', ERROR, ['APP'], false)
logger('com.rxlogix.htmlunit', ERROR, ['APP'], false)

//To log filters (now interceptors)
logger('grails.app.controllers.interceptor', ERROR, ['APP'], false)
//To Enable SQL logs
//logger 'org.hibernate.type.descriptor.sql.BasicBinder', TRACE, ['APP'], false
//logger 'org.hibernate.SQL', TRACE, ['APP'], false


if (System.getProperty('debug') == 'true') {
    logger('com.rxlogix.util.marshalling', DEBUG, ['APP'], false)
    logger('reports.BootStrap', DEBUG, ['APP'], false)
    logger('com.rxlogix', DEBUG, ['APP'], false)
    logger('com.rxlogix.UserService', DEBUG, ['APP'], false)
    logger('org.springframework.security.ldap', DEBUG, ['APP'], false)
}

if (System.getProperty('saml.debug') == 'true') {
    logger('org.opensaml.xml.signature', DEBUG, ['APP'], false)
    logger('org.springframework.security.saml', DEBUG, ['APP'], false)
    logger('org.springframework.security.web', DEBUG, ['APP'], false)
    logger('org.springframework.security.saml2.provider.service.web.authentication.logout', TRACE, ['APP'], false)
    logger('org.springframework.security.saml2.provider.service.web', TRACE, ['APP'], false)
    logger('com.rxlogix.user', TRACE, ['APP'], false)
}

if (System.getProperty('trace') == 'true') {
    logger('reports.BootStrap', TRACE, ['APP'], false)
    logger('com.rxlogix', TRACE, ['APP'], false)
    logger('com.rxlogix.UserService', TRACE, ['APP'], false)
}

root(INFO, ['APP'])
root(ERROR, ['APP'])