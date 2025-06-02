grails.pvreports.config.root = "file:${userHome}/.reports"
grails.config.locations = ["file:${userHome}/.reports/config.groovy"]

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination

System.setProperty("javamelody.gzip-compression-disabled", "true")

pvreports.seeding.user = pvr_user
grails.mime.use.accept.header = true
grails.mime.file.extensions = false
grails.mime.types = [ // the first one is the default format
                      html         : ['text/html', 'application/xhtml+xml'],
                      form         : 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data',
                      all          : '*/*', // 'all' maps to '*' or the first available format in withFormat
                      atom         : 'application/atom+xml',
                      css          : 'text/css',
                      csv          : 'text/csv',
                      js           : 'text/javascript',
                      json         : ['application/json', 'text/json', 'application/scim+json'],
                      rss          : 'application/rss+xml',
                      text         : 'text/plain',
                      hal          : ['application/hal+json', 'application/hal+xml'],
                      xml          : ['application/xml'],
                      r3xml        : ['application/xml'],
                      pdf          : ['application/pdf'],
                      xlsx         : ['application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'],
                      docx         : ['application/vnd.openxmlformats-officedocument.wordprocessingml.document'],
                      pptx         : ['application/vnd.openxmlformats-officedocument.presentationml.presentation'],
                      rtf          : 'application/rtf',
                      excel        : 'application/vnd.ms-excel',
                      xls          : 'application/vnd.ms-excel',
                      ods          : 'application/vnd.oasis.opendocument.spreadsheet',
                      zip          : ['application/zip']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// Legacy setting for codec used to encode data with ${}
grails.views.default.codec = "html"

// The default scope for controllers. May be prototype, session or singleton.
// If unspecified, controllers are prototype scoped.
grails.controllers.defaultScope = 'singleton'
grails.controllers.upload.maxRequestSize=5242880

// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside ${}
                scriptlet = 'html' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        // filteringCodecForContentType.'text/html' = 'html'
    }
}


grails.converters.encoding = "UTF-8"
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart = false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

// configure passing transaction's read-only attribute to Hibernate session, queries and criterias
// set "singleSession = false" OSIV mode in hibernate configuration after enabling
grails.hibernate.pass.readonly = false
// configure passing read-only to OSIV session by default, requires "singleSession = false" OSIV mode
grails.hibernate.osiv.readonly = false

// based on requirements at start of application we can use for clearing locks and checksum of liquibase at start of applications.
liquibase {
    clearLockAtStart = false
    clearCheckSumAtStart = false
}

shared.directory = "${System.getProperty('catalina.base')}" //add to IQ, mandatory for cluster setup in 3.3
swagger {
    info {
        description = "Swagger API Documentation for API's exposed to"
        version = "RxLogix-swagger-5.3"
        title = "Swagger API"
        termsOfServices = "http://swagger.io/"
        contact {
            name = "Contact Us"
            url = "https://www.rxlogix.com"
            email = "sachin.verma@rxlogix.com"
        }
        license {
            name = "licence under https://www.rxlogix.com/"
            url = "https://www.rxlogix.com/"
        }
    }
    schemes = [io.swagger.models.Scheme.HTTP]
    consumes = ["application/json"]
}
externalDirectory = "${userHome}/.reports/"
tempDirectory = "${System.getProperty("java.io.tmpdir")}/${appName ?: 'reports'}/"

//set this property in external config with unique server name to identify server in cluster environment
pvr.cluster.instance.name = "" //add to IQ, mandatory for cluster setup in 3.3
pvr.instance.ipaddress.display = false

// Date binding for SQL TimeStamps and DatePicker date. TODO need to work for JAPANESE
grails.databinding.dateFormats = ['dd-MMM-yyyy', 'yyyy-MM-dd HH:mm:ss.S', "yyyy-MM-dd'T'hh:mm:ss'Z'", 'dd-MMM-yyyy HH:mm:ss.S', 'yyyy/MM/dd', 'yyyy/MM/dd HH:mm:ss.S', "yyyy/MM/dd'T'hh:mm:ss'Z'"]

// Basic auth for REST services
grails.plugin.springsecurity.useBasicAuth = true
rxlogix.security.saml.provider.name = 'pvreports'
//LDAP Configuration
//This ensures daoAuthenticationProvider is not used and thus LDAP failures do not fall back to database lookups.
grails.plugin.springsecurity.providerNames = [/*'samlAuthenticationProvider', */ 'ldapAuthProvider', 'daoAuthenticationProvider', 'anonymousAuthenticationProvider']
grails.plugin.springsecurity.logout.handlerNames = [
        'pvrLogoutHandler' ,'rememberMeServices', 'securityContextLogoutHandler'
]

grails.plugin.springsecurity.ldap.context.managerDn = 'cn=admin,dc=eng,dc=rxlogix,dc=com'
grails.plugin.springsecurity.ldap.context.managerPassword = "ldapldap123" //{cipher}vWLXzOfm0/FEParhQS8HRA==
grails.plugin.springsecurity.ldap.context.server = 'ldap://ldap.eng.rxlogix.com:389'
grails.plugin.springsecurity.ldap.search.base = 'ou=users,dc=eng,dc=rxlogix,dc=com'
grails.plugin.springsecurity.ldap.users.search.filter = '(&(|(uid=*{0}*)(cn=*{0}*)(mail=*{0}*)))'
grails.plugin.springsecurity.ldap.authorities.groupSearchBase = 'ou=groups,dc=eng,dc=rxlogix,dc=com'

grails.plugin.springsecurity.ldap.search.filter = '(uid={0})'
grails.plugin.springsecurity.ldap.authorities.groupSearchFilter = 'uniqueMember={0}'
grails.plugin.springsecurity.ldap.authorities.groupRoleAttribute = 'cn'
grails.plugin.springsecurity.ldap.useRememberMe = false
grails.plugin.springsecurity.ldap.auth.hideUserNotFoundExceptions = false
grails.plugin.springsecurity.ldap.mapper.userDetailsClass = 'com.rxlogix.user.CustomUserDetails'
grails.plugin.springsecurity.ldap.authorities.retrieveGroupRoles = false
grails.plugin.springsecurity.ldap.authorities.retrieveDatabaseRoles = true
grails.plugin.springsecurity.ldap.fullName.attribute = "cn"
grails.plugin.springsecurity.ldap.email.attribute = "mail"
grails.plugin.springsecurity.ldap.uid.attribute = "uid"

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'com.rxlogix.user.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'com.rxlogix.user.UserRole'
grails.plugin.springsecurity.userLookup.usernameIgnoreCase = true
grails.plugin.springsecurity.authority.className = 'com.rxlogix.user.Role'
grails.plugin.springsecurity.apf.postOnly = false

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
        [pattern: "/services/**", access: ['permitAll']],
        [pattern: "/advancedReportViewer/**", access: ['permitAll']],
        [pattern: "/stomp", access: ['permitAll']],
        [pattern: "/stomp/**", access: ['permitAll']],
        [pattern: '/productDictionary/**', access: ['permitAll']], //security handled through InternalApiSecurityInterceptor
        [pattern: '/eventDictionary/**', access: ['permitAll']], //security handled through InternalApiSecurityInterceptor
        [pattern: '/studyDictionary/**', access: ['permitAll']], //security handled through InternalApiSecurityInterceptor
        [pattern: '/dictionaryGroup/**', access: ['permitAll']], //security handled through InternalApiSecurityInterceptor
        [pattern: '/debug/**', access: ['permitAll']], //security handled through IP Restrictions via local only.
        [pattern: '/assets/**', access: ['permitAll']],
        [pattern: '/wopi/**' , access:  ['permitAll']],
        [pattern: '/static/**', access: ['permitAll']],
        [pattern: '/health/**', access: ['permitAll']],
        [pattern: '/manage/**', access: ['ROLE_ADMIN']],
        [pattern: '/public/api/**', access: ['permitAll']], //ALL Public API's are managed through PublicRestInterceptor
        [pattern: '/console/**', access: ['ROLE_DEV']],
        [pattern: '/quartz/**', access: ['ROLE_DEV']],
        [pattern: '/monitoring/**', access: ['ROLE_DEV']],
        [pattern: '/localization/**', access: ['ROLE_DEV']],
        [pattern: '/dbconsole/**', access: ['ROLE_DEV']],
        [pattern: '/apidoc/**', access: ['ROLE_DEV']],
        [pattern: '/webjars/**', access: ['ROLE_DEV']],
        [pattern: '/mailOAuth/**', access: ['ROLE_SYSTEM_CONFIGURATION']],
        [pattern: '/auditLogEvent/**', access: ['IS_AUTHENTICATED_FULLY']],
        [pattern: '/auditLogEventRest/**', access: ['IS_AUTHENTICATED_FULLY']],
        [pattern: '/readerToken/**', access: ['ROLE_DEV']], //We don't need this as such
        [pattern: '/scim/v2/**', access: ['permitAll']], //Required for Grails SCIM end points.
        [pattern: '/scim**/**', access: ['permitAll']], //Required for Grails SCIM end points.
        [pattern: '/applicationNotification/**', access: ['permitAll']], //Required for hazelcast notification for configuration update on
        [pattern: '/saml2/**', access: ['permitAll']],
        [pattern: '/login/**', access: ['permitAll']],
        [pattern: '/logout/**', access: ['permitAll']]
]

grails.plugin.springsecurity.logout.afterLogoutUrl = "/login/auth"
grails.plugin.springsecurity.logout.postOnly = false
grails.plugin.springsecurity.rejectIfNoRule = true //We want to reject if no explicit permissions mentioned so that proper permissions would be given while developing itself.
grails.plugin.springsecurity.adh.errorPage = '/Errors/forbidden' //to throw 403 page on access denied
grails.plugin.springsecurity.useSecurityEventListener = true

grails.plugin.springsecurity.roleHierarchy = '''
   ROLE_DEV > ROLE_ADMIN
   ROLE_DEV > ROLE_USER_MANAGER

   ROLE_ADMIN > ROLE_SYSTEM_CONFIGURATION
   ROLE_ADMIN > ROLE_BQA_EDITOR
   ROLE_BQA_EDITOR > ROLE_CONFIGURATION_CRUD
   ROLE_CONFIGURATION_CRUD > ROLE_CONFIGURATION_VIEW
   ROLE_SYSTEM_CONFIGURATION > ROLE_CONFIGURATION_VIEW

   ROLE_ADMIN > ROLE_PERIODIC_CONFIGURATION_CRUD
   ROLE_PERIODIC_CONFIGURATION_CRUD > ROLE_PERIODIC_CONFIGURATION_VIEW
   ROLE_SYSTEM_CONFIGURATION > ROLE_PERIODIC_CONFIGURATION_VIEW

   ROLE_ADMIN > ROLE_QUERY_ADVANCED
   ROLE_QUERY_ADVANCED > ROLE_QUERY_CRUD
   ROLE_QUERY_CRUD > ROLE_QUERY_VIEW
   ROLE_SYSTEM_CONFIGURATION > ROLE_QUERY_VIEW

   ROLE_ADMIN > ROLE_TEMPLATE_ADVANCED
   ROLE_TEMPLATE_ADVANCED > ROLE_TEMPLATE_CRUD
   ROLE_TEMPLATE_CRUD > ROLE_TEMPLATE_VIEW
   ROLE_SYSTEM_CONFIGURATION > ROLE_TEMPLATE_VIEW
   ROLE_TEMPLATE_ADVANCED > ROLE_TEMPLATE_SET_CRUD
   ROLE_TEMPLATE_SET_CRUD > ROLE_TEMPLATE_SET_VIEW
   ROLE_SYSTEM_CONFIGURATION > ROLE_TEMPLATE_SET_VIEW
   

   ROLE_ADMIN > ROLE_DATA_ANALYSIS

   ROLE_ADMIN > ROLE_TASK_CRUD
   ROLE_TASK_CRUD > ROLE_TASK_VIEW

   ROLE_ADMIN > ROLE_COGNOS_CRUD
   ROLE_COGNOS_CRUD > ROLE_COGNOS_VIEW

   ROLE_ADMIN > ROLE_CASE_SERIES_EDIT 
   ROLE_CASE_SERIES_EDIT > ROLE_CASE_SERIES_CRUD 
   ROLE_CASE_SERIES_CRUD > ROLE_CASE_SERIES_VIEW

   ROLE_ADMIN > ROLE_REPORT_REQUEST_CRUD
   ROLE_REPORT_REQUEST_CRUD > ROLE_REPORT_REQUEST_VIEW
   ROLE_ADMIN > ROLE_REPORT_REQUEST_PLANNING_TEAM
   ROLE_ADMIN > ROLE_REPORT_REQUEST_PLAN_VIEW
   ROLE_ADMIN > ROLE_REPORT_REQUEST_ASSIGN
   
   ROLE_ADMIN > ROLE_CHART_TEMPLATE_EDITOR

   ROLE_ADMIN > ROLE_ACTION_ITEM

   ROLE_ADMIN > ROLE_CALENDAR
   
   ROLE_SHARE_ALL > ROLE_SHARE_GROUP
   
   ROLE_ADMIN > ROLE_CUSTOM_EXPRESSION
   
   ROLE_ADMIN > ROLE_CUSTOM_FIELD

   ROLE_ADMIN > ROLE_PVQ_EDIT
   
   ROLE_PVQ_EDIT > ROLE_PVQ_VIEW 
   ROLE_SYSTEM_CONFIGURATION > ROLE_PVQ_VIEW
   
   ROLE_ADMIN > ROLE_PVC_EDIT
   
   ROLE_PVC_EDIT > ROLE_PVC_VIEW 
   ROLE_SYSTEM_CONFIGURATION > ROLE_PVC_VIEW
   
   ROLE_ADMIN > ROLE_PVC_INBOUND_EDIT
   ROLE_ADMIN > ROLE_PVC_INBOUND_VIEW
   ROLE_PVC_INBOUND_EDIT > ROLE_PVC_INBOUND_VIEW 
   
   ROLE_ADMIN > ROLE_CONFIG_TMPLT_CREATOR
   
   ROLE_ADMIN > ROLE_ICSR_REPORTS_EDITOR
   ROLE_ICSR_REPORTS_EDITOR >  ROLE_ICSR_REPORTS_VIEWER
   ROLE_SYSTEM_CONFIGURATION > ROLE_ICSR_REPORTS_VIEWER
   
   ROLE_ADMIN > ROLE_ICSR_PROFILE_EDITOR
   ROLE_ICSR_PROFILE_EDITOR >  ROLE_ICSR_PROFILE_VIEWER
   ROLE_SYSTEM_CONFIGURATION > ROLE_ICSR_PROFILE_VIEWER
   
   ROLE_ADMIN > ROLE_DMS
   
   ROLE_ADMIN > ROLE_PUBLISHER_TEMPLATE_EDITOR
   ROLE_PUBLISHER_TEMPLATE_EDITOR > ROLE_PUBLISHER_TEMPLATE_VIEWER
   
   ROLE_ADMIN > ROLE_PUBLISHER_SECTION_EDITOR
   ROLE_ADMIN > ROLE_TEMPLATE_LIBRARY_ACCESS
   ROLE_ADMIN > ROLE_DOCUMENT_AUTHOR
   ROLE_ADMIN > ROLE_DOCUMENT_REVIEWER
   ROLE_ADMIN > ROLE_DOCUMENT_APPROVER
   
   ROLE_ADMIN > ROLE_RUN_PRIORITY_RPT
   
   ROLE_ICSR_DISTRIBUTION > ROLE_ICSR_PROFILE_VIEWER
   ROLE_ICSR_DISTRIBUTION_ADMIN > ROLE_ICSR_DISTRIBUTION
   ROLE_ADMIN > ROLE_ICSR_DISTRIBUTION_ADMIN
'''

grails.plugin.springsecurity.fii.rejectPublicInvocations = false

grails.plugin.springsecurity.filterChain.chainMap = [
        [pattern: '/rest_v2/**', filters: 'JOINED_FILTERS,-exceptionTranslationFilter,-rememberMeAuthenticationFilter,-restTokenValidationFilter,-restExceptionTranslationFilter,-concurrentSessionFilter'],
        [pattern: '/odata/**', filters: 'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter,-rememberMeAuthenticationFilter,-concurrentSessionFilter'],  // Stateless chain
        [pattern: '/**', filters: 'JOINED_FILTERS,-restTokenValidationFilter,-restExceptionTranslationFilter,-basicAuthenticationFilter,-basicExceptionTranslationFilter']
]

grails.plugin.springsecurity.ipRestrictions = [
        [pattern: '/debug/**', access: '127.0.0.1']
]

reports {
    includeDataCleanupVersion = true
    includeMedConfCases = ""
}

endpoints {
    enabled = true
    health.sensitive = false
}
management."context-path" = "/manage"
management.security.enabled = false

mail.default.delivery.subject = '[pv-reports] Report delivery for: "[reportName]"'

grails.mail.default.from = "pvreports-app@rxlogix.com"

grails {
    mail {
        host = "outlook.office365.com"
        port = 587
        username = "" //pvreports-app@rxlogix.com
        password = "" //{cipher}iAV9hrYC/z/1H5YwFA9iFQ==
        props = ["mail.smtp.starttls.enable": "true",
                 "mail.smtp.port"           : "587",
                 "mail.smtp.debug"          : "true",
                 "mail.smtp.sendpartial"    : "true"]
        oAuth {
            enabled = false
            client_id = 'b5351907-3792-4590-ac3a-ee9ba737e7d1'
            secret_val = 'DvW7Q~CaMbkKrX2DDifpHQbOOasr3k-RrmSAN'
            api_scope = 'https://outlook.office.com/Mail.Read https://outlook.office.com/mail.send https://outlook.office.com/SMTP.Send offline_access' //For SMTP
//            api_scope = 'Mail.Send offline_access Mail.ReadWrite openid' for Graph
            callback_url = 'http://localhost:9090/reports/mailOAuth/callback'
            tenant_id = 'common'
            token {
                refresh.frequency = 300 //every 5mins
                refresh.time.difference = 600 //if less than 10mins then refresh
            }
            redirect.uri = '/controlPanel/index'
            graph {
                enabled = false
                attachmentMax = 3 //MB
            }

            reader {
                enabled = false
                graph {
                    enabled = false
                }
                imap {
                    enabled = false
                }
            }
        }
    }
}

// Cacheable Plugin
grails.cache.clearAtStartup = true
grails.cache.enabled = true
grails.cache.config = {
    cache {
        name 'selectableValues'
    }
}
deletePreviewQueryJob.cronExpression = "0 0 3 * * ?" // Triggers morning 3 a.m
pvcEmailNotificationJob.cronExpression = "0 0/30 * * * ? *" // each half an hour
pvqEmailNotificationJob.cronExpression= "0 0/30 * * * ? *" // each half an hour

pvreports.cumulative.startDate = "01-Jan-0001" //default Cumulative start date


beans {
    cacheManager {
        shared = true
    }
}

// Product dictionary additional filters configurations
productDictionary.productSector.enabled = false
productDictionary.productSectorType.enabled = false
productDictionary.deviceType.enabled = false
productDictionary.companyUnit.enabled = false

// Cognos report configuration
cognosReport.view.enabled = false

action.item.category.report.request.value='Report Request'
jdbcProperties {
    fetch_size = 50
    batch_size = 5
}

grails.plugins.remotepagination.enableBootstrap = true

grails.plugin.springwebsocket.notificationChanelPrefix = "/notifications/"

// Spring Session configuration <works for both hazelcast and embedded spring session>
springsession.enabled = true // this is always true
springsession.timeout.interval = 1800 //30 minutes
springsession.map.name = 'spring:session'
springsession.allow.persist.mutable = true // setting this true replaces session on every request, this is done to fix FlashMessages

//pvr hazelcast notifications & session management
hazelcast.enabled = true
hazelcast.server.instance.name = '127_0_0_1' //this should be unique for every pvr application server, you can use server ip address here
hazelcast.server.port = 5701
hazelcast.server.auto.increment.port = false // set true if running multiple PVR applications on same server
hazelcast.server.portCount = 3 // only works when auto.increment.port is true, limits number of next ports to try for starting hazelcast server
hazelcast.server.outbound.port.definition = "35000-35010" // range should be more-than or equal-to number of pvr application servers
hazelcast.network.nodes = ["localhost:5701"]
hazelcast.group.name = "pvr-local-dev"
hazelcast.group.password = "" //{cipher}xGzeC+FEA6JKHZnNlf5dgI5Zcym+GRlsJ9vUVpj9eSM=
hazelcast.notification.channel = "notifications"
hazelcast.notification.dmsCache = "dms_cache_refresh"
hazelcast.notification.killCaseGeneration = "kill_case_generation"
hazelcast.notification.reportFieldCache = "report_field_cache_refresh"
hazelcast.notification.console = "console_channel"
hazelcast.notification.cacheChannel = "cache_channel"
hazelcast.management.center.enabled = false
hazelcast.management.center.url = "http://localhost:9999/hazelcast"
hazelcast.management.center.update.interval = 60 // 1 minute

pvsignal.report.date.delimiter = ":::"
pvsignal.report.tag.name = "Signal Report"
//Spring security SAML | SSO
grails.plugin.springsecurity.saml.active = false
grails.plugin.springsecurity.saml.metadata.url = '/saml2/service-provider-metadata/{registrationId}'
grails.plugin.springsecurity.saml.afterLoginUrl = "/"
grails.plugin.springsecurity.saml.loginFailUrl = "/login/ssoAuthFail?login_error=1"
grails.plugin.springsecurity.saml.userGroupAttribute = "memberOf"
//grails.plugin.springsecurity.saml.metadata.providers = [pvreports: "file:${userHome}/.reports/idp-local-metadata.xml"]
grails.plugin.springsecurity.saml.metadata.sp.file = "file:${userHome}/.reports/reports_sp.xml"
grails.plugin.springsecurity.saml.autoCreate.active = true // this is must set TRUE for getCurrentUser() to work with SAML - PVR-6910
grails.plugin.springsecurity.saml.autoCreate.assignAuthorities = true
grails.plugin.springsecurity.saml.keyManager.storeFile = "file:${userHome}/.reports/pvrSamlPingKeystore.jks"
grails.plugin.springsecurity.saml.keyManager.storePass = "rxlogix"
grails.plugin.springsecurity.saml.keyManager.passwords.keyAlias = "${rxlogix.security.saml.provider.name}"
grails.plugin.springsecurity.saml.keyManager.passwords.keyPass = "rxlogix"
grails.plugin.springsecurity.saml.keyManager.passwords = ["${rxlogix.security.saml.provider.name}": 'rxlogix']
grails.plugin.springsecurity.saml.defaultKey = "${rxlogix.security.saml.provider.name}"
grails.plugin.springsecurity.saml.userAttributeMappings.username = 'username'
//grails.plugin.springsecurity.saml.signingAlgorithm = 'RSA'
grails.plugin.springsecurity.saml.metadata.sp.defaults = [
        local                       : true,
        entityID                    : "${rxlogix.security.saml.provider.name}",
        alias                       : "${rxlogix.security.saml.provider.name}",
        securityProfile             : 'metaiop',
        signingKey                  : "${rxlogix.security.saml.provider.name}",
        encryptionKey               : "${rxlogix.security.saml.provider.name}",
        tlsKey                      : "${rxlogix.security.saml.provider.name}",
        requireArtifactResolveSigned: false,
        requireLogoutRequestSigned  : true,
        requireLogoutResponseSigned : true,
        assertionConsumerService    : "http://localhost:9191/reports/login/saml2/sso/${rxlogix.security.saml.provider.name}",
        singleLogoutService         : "http://localhost:9191/reports/logout/saml2/slo"
]
grails.plugin.springsecurity.saml.responseSkew = 60
grails.plugin.springsecurity.saml.maxAuthenticationAge = 7200 //use in case authentication issue date can be of long back.
//https://stackoverflow.com/questions/24966944/saml2-idp-session-timeout-and-slo
grails.plugin.springsecurity.saml.idp.session.timeout.infinite = false //Custom handling due to session sync issue between IDP and SP. Need to find solution using webservice or ping to keep live idp session.
//Options for reauthenticate on every login
grails.plugin.springsecurity.saml.options.forceAuthN = false
grails.plugin.springsecurity.saml.options.relayState = null
//Logout URI's
grails.plugin.springsecurity.logout.uri = 'logout'
grails.plugin.springsecurity.saml.logout.uri = 'logout/saml2'//in case of global logout use 'saml/logout'
grails.plugin.springsecurity.saml.logout.response.verify.disable = true
grails.plugin.springsecurity.saml.logout.binding.urn = 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST'
//grails.plugin.springsecurity.saml.afterLogoutUrl = '/logout/local'

//Properties to set the load balancer related SAML context provider.
saml.lb.enabled = false
saml.lb.serverName = "proxy-server"
saml.lb.port = 443
saml.lb.scheme = "https"
saml.lb.contextPath = "/reports"

//RestServices for OData Security
grails.plugin.springsecurity.rest.token.storage.useGorm = true
grails.plugin.springsecurity.rest.token.storage.gorm.tokenDomainClassName = 'com.rxlogix.user.User'
grails.plugin.springsecurity.rest.token.storage.gorm.tokenValuePropertyName = 'apiToken'
grails.plugin.springsecurity.rest.token.storage.gorm.usernamePropertyName = 'username'
grails.plugin.springsecurity.rest.login.active=false
grails.plugin.springsecurity.rest.token.validation.useBearerToken = false
grails.plugin.springsecurity.rest.token.validation.headerName='X-Auth-Token'


caseSeries.list.primaryFields = """{
    "CASE_UNIQUE_ID": {
        "type": "Number",
        "visible": false
    },
    "CASE_NUM": {
        "type": "String",
        "visible": false,
        "search": "upper(cm.case_num)"
    },
    "VERSION_NUM": {
        "type": "Number",
        "visible": false,
        "search": "cm.version_num"
    },
    "REPORT_TYPE": {
        "type": "String",
        "visible": true,
        "defaultHide":true,
        "search": "upper(lrt.report_type)",
        "order" : 1
    },
    "NAME": {
        "type": "String",
        "visible": true,
        "defaultHide":true,
        "search": "upper(pcsd.prim_prod_name)",
        "order" : 2
    },
    "PRIMARY_EVENT": {
        "type": "String",
        "visible": true,
        "search": "upper(pcsd.PRIM_EVT_PREF_TERM)",
        "order" : 3
    },
    "SERIOUSNESS_FLAG": {
        "type": "String",
        "visible": true,
        "search": "upper(vps.seriousness)",
        "order" : 4
    },
    "EVT_OUTCOME": {
        "type": "String",
        "visible": false,
        "search": "upper(lec.evt_outcome)"
    },
    "LISTEDNESS": {
        "type": "String",
        "visible": true,
        "search": "upper(lll.listedness_text)",
        "order" : 5
    },
    "REPORTABILITY": {
        "type": "String",
        "visible": true,
        "search": "upper(vpc.causality)",
        "order" : 6
    },
    "COMMENTS": {
        "type": "String",
        "visible": false
    },
    "JUSTIFICATION": {
        "type": "String",
        "visible": false
    },
    "CASE_LOCKED_DATE": {
        "type": "Date",
        "visible": true,
        "search": "NVL(to_char(cmF.DATE_LOCKED,''dd-mon-yyyy''), to_char(CMF.DATE_ARCHIVED,''dd-mon-yyyy''))",
        "order" : 7
    },
    "CASE_ID": {
        "type": "Number",
        "visible": false
    },
    "ALERT_TAG_TEXT": {
        "type": "String[]",
        "visible": false
    },
    "GLOBAL_TAG_TEXT": {
        "type": "String[]",
        "visible": false
    },
    "ADDED_MANUAL_FLAG": {
        "type": "Boolean",
        "visible": false
    },
    "UNLOCKED_TO_LOCKED_FLAG": {
        "type": "Boolean",
        "visible": false
    },
    "HIGHER_VERSION_FLAG": {
        "type": "Boolean",
        "visible": false
    },
    "NEW_CASE_FLAG": {
        "type": "Boolean",
        "visible": false
    }
}"""

caseSeries.list.secondaryFields = """{
}"""

caseSeries.list.downloadFields = """["CASE_NUM", "VERSION_NUM", "GLOBAL_TAG_TEXT","ALERT_TAG_TEXT","REPORT_TYPE","NAME","PRIMARY_EVENT","SERIOUSNESS_FLAG","LISTEDNESS","REPORTABILITY","CASE_LOCKED_DATE"]"""

caseSeries.list.pvd.query="""select nvl(max(case_series_exec_id),-1) as EX_PVD_CASESERIES_ID from pvr_query_info where case_series_id = :EX_CASESERIES_ID and case_series_owner=:CASESERIES_OWNER"""

caseSeries.list.query = """SELECT case_num, version_num, report_type, NAME, primary_event,
       seriousness_flag, workflow_state, listedness, reportability,
       evt_outcome, case_unique_id, comments,justification,case_locked_date, added_manual_flag,cnt,
       unlocked_to_locked_flag , new_case_flag ,
       (select count(1) from pvr_query_case_list t1
       join pvr_case_series_details t2
         on (t1.case_unique_id  = t2.case_unique_id and t1.case_series_owner=t2.case_series_owner)
       where t1.case_series_exec_id = :EX_PVD_CASESERIES_ID and t1.case_series_owner=:CASESERIES_OWNER
       and NVL (t1.deleted_flag, 0) = 0 and t2.case_locked_date is not null) total_count, higher_version_flag,case_id,
--       null global_tag_text,
       (select distinct listagg(tag_text,',') within group (order by tag_text) over(partition by null) global_tag_text
from CASE_GLOBAL_TAG_LIST_MAPPING cgtl,tag_list til
    where cgtl.tag_id=til.tag_id
    and tag_type=0
    and cgtl.case_id=main1.casE_id) as global_tag_text,
--   null alert_tag_text
   (select distinct listagg(tag_text,',') within group (order by tag_text) over(partition by null) alert_tag_text
from CASE_SERIES_TAG_LIST_MAPPING cgtl,tag_list til
    where cgtl.tag_id=til.tag_id
    and tag_type=1
    and cgtl.case_id=main1.casE_id
    and cgtl.case_series_id=:EX_CASESERIES_ID) as alert_tag_text
  FROM (SELECT case_num, version_num, report_type, NAME, primary_event,
               seriousness_flag, workflow_state, listedness, reportability,
               evt_outcome, case_unique_id, comments,justification ,added_manual_flag,case_locked_date,
               cnt,  unlocked_to_locked_flag , new_case_flag, ROWNUM row_num,higher_version_flag,case_id
          FROM (
                  SELECT cm.case_num, cm.version_num, lrt.report_type,
                       NVL(decode(cm.flag_unblind,0,decode(cp.flag_study_drug,1,pcsd.prim_prod_name,vp.product_name),vp.product_name),pcsd.prim_prod_name) AS name,
                       pcsd.prim_evt_pref_term primary_event,
                       vps.seriousness seriousness_flag,
                       vcsn.state_name workflow_state, lll.listedness_text as listedness,
                       vpc.causality reportability, lec.evt_outcome, pqcl.added_manual_flag,
                       pqcl.case_unique_id, pqcc.comments,pqcc.justification,
                       pcsd.case_locked_date,count(*) over (partition by null) cnt,cm.case_id , decode(bitand(pcsd.case_category,1),1,1,0)  new_case_flag   ,
                 decode(bitand(pcsd.case_category,2) ,2,1,0) unlocked_to_locked_flag,
                 decode(bitand(pcsd.case_category,4) ,4,1,0) higher_version_flag
                 FROM C_IDENTIFICATION cm
JOIN  C_IDENTIFICATION_FU cmF ON (
               cm.tenant_id = cmF.tenant_id
              AND cm.case_id = cmF.case_id
              AND cm.version_num = cmF.version_num)
LEFT OUTER JOIN C_PROD_IDENTIFICATION cp ON (
               cm.tenant_id = cp.tenant_id
              AND cm.case_id = cp.case_id
              AND cm.version_num = cp.version_num
              and cp.rank_id = 1)
       JOIN pvr_query_case_list pqcl
          ON (    cm.tenant_id = pqcl.tenant_id
              AND cm.case_id = pqcl.case_id
              AND cm.version_num = pqcl.version_num
              AND pqcl.case_series_owner=:CASESERIES_OWNER)
       JOIN pvr_case_series_details pcsd
          ON (pqcl.case_unique_id = pcsd.case_unique_id
          and pqcl.case_series_owner = pcsd.case_series_owner
\t\t\tand pcsd.tenant_id = pqcl.tenant_id)
       LEFT JOIN vw_pud_seriousness vps
          ON (pcsd.seriousness_flag = to_number(vps.ID))
       LEFT JOIN pvr_query_cl_mod_comment pqcc
          ON (pqcl.case_unique_id = pqcc.case_unique_id and pqcl.tenant_id = pqcc.tenant_id)
       LEFT JOIN vw_lrty_report_type lrt
          ON (pcsd.rpt_type_id = lrt.rpt_type_id
              AND pqcl.tenant_id = lrt.tenant_id)
       LEFT JOIN vw_llist_listedness lll
          ON (to_char(pcsd.listedness_id) = to_char(lll.listedness_id)
              AND pqcl.tenant_id = lll.tenant_id)
       LEFT JOIN vw_pud_causality vpc
          ON (pcsd.causality_id = vpc.ID)
       LEFT JOIN vw_cws_state_name vcsn
          ON (pcsd.workflow_state_id = vcsn.workflow_state_id
              AND pqcl.tenant_id = vcsn.tenant_id)
       LEFT JOIN vw_leo_evt_outcome lec
          ON (pcsd.outcome_id = lec.evt_outcome_id
              AND pqcl.tenant_id = lec.tenant_id)
       LEFT OUTER JOIN vw_product vp
          ON (cp.prod_id_resolved = vp.product_id)

                 WHERE NVL (pqcl.deleted_flag, 0) = 0 :SEARCH_QUERY
             AND pqcl.case_series_exec_id = :EX_PVD_CASESERIES_ID ORDER BY :ORDER_BY :SORT_DIRECTION )) main1"""


hibernate {
    cache {
        use_second_level_cache = true
        use_query_cache = false
        setProperty 'region.factory_class', 'org.hibernate.cache.ehcache.internal.EhcacheRegionFactory'
    }
    allow_update_outside_transaction = true
    singleSession = true // configure OSIV singleSession mode
}


csrfProtection {
    enabled = true
    excludeURLPatterns = ["/login/saml2/sso/${rxlogix.security.saml.provider.name}",
                          "/saml2/authenticate/${rxlogix.security.saml.provider.name}",
                          "/logout", "/keep-alive",
                          "/console/", "/rest_v2/", "/importConfiguration", "/executedCaseSeries", '/odata', '/importExcel', '/query/validateValue', '/configuration/validateValue', '/public/', '/debug/', '/wopi', '/scim/', '/icsrProfileConfiguration/', '/icsrCaseTrackingRest/', '/advancedReportViewer/cllAjax', '/applicationNotification/']

}

singleUserSession {
    enabled = true
    invalidateOld = true
}

health.check.token = 'alihoih398yewkj3298hfeiub'


grails.plugin.localizations.enabled = true

grails.plugin.springsecurity.rest.token.storage.jwt.useEncryptedJwt = true


server{
    servlet.'context-path' = '/reports'
    port = 9090
}

pv {
    plugin {
        dictionary {
            select2v4 = false
        }
    }
}

applications {
    current = 'PVReports'
    installed = [
            [app: 'PVIntake', role: 'ROLE_PV_INTAKE', link: "#"],
            [app: 'PVPublisher', role: 'ROLE_PV_PUBLISHER', link: "#"],
            [app: 'PVSignal', role: 'ROLE_PV_SIGNAL', link: "#"],
            [app: 'PVCentral', role: 'ROLE_PVC_VIEW', link: "/central/index"],
            [app: 'PVQuality', role: 'ROLE_PVQ_VIEW', link: "/quality/index"]
    ]

}

settings {
    adminUrl = ''
    prefUrl = ''
    helpUrl = '/help'
    topBar = [PVReports: [
            [
                    listId : 'help',
                    link: $ { -> grails.util.Holders.config?.settings?.helpUrl },
                    code: "app.label.help",
                    additionalHtml: "<i class=\"md md-help\"></i>",
            ],
            [
                    listId : 'executionStatus',
                    link : '/executionStatus/list',
                    code: "app.label.executionStatus",
                    additionalHtml: "<i class=\"md md-alarm\"></i>",
                    role: 'ROLE_CONFIGURATION_VIEW,ROLE_PERIODIC_CONFIGURATION_VIEW'

            ],
            [
                    listId : 'userManagement',
                    link : '/user/index',
                    code: "app.label.userManagement",
                    role: 'ROLE_USER_MANAGER'

            ],
            [
                    listId : 'userManagement',
                    link : '/userGroup/index',
                    code: "app.label.group.management",
                    role: 'ROLE_USER_MANAGER'

            ],
            [
                    listId : 'roleManagement',
                    link : '/role/index',
                    code: "app.label.roleManagement",
                    role: 'ROLE_USER_MANAGER'

            ],
            [
                    listId : 'fieldManagement',
                    link : '/reportField/index',
                    code: "app.label.field.management",
                    role: 'ROLE_ADMIN'

            ],
            [
                    listId : 'jsonDownloaded',
                    link : '/queryTemplateJSON/index',
                    code: "app.label.download.json",
                    role: 'ROLE_SYSTEM_CONFIGURATION'

            ],
            [
                    listId : 'customField',
                    link : '/customField/index',
                    code: "app.label.customField.title",
                    role: 'ROLE_CUSTOM_FIELD'

            ],
            [
                    listId : 'userManagement',
                    link : '/dashboardDictionary/index',
                    code: "app.label.dashBoard.nenuItem",
                    role: 'ROLE_SYSTEM_CONFIGURATION,ROLE_USER_MANAGER'

            ],
            [
                    listId : 'userManagement',
                    link : '/fieldProfile/index',
                    code: "app.label.field.profile",
                    role: 'ROLE_SYSTEM_CONFIGURATION'

            ],
            [
                    listId : 'sourceProfileManagement',
                    link : '/sourceProfile/index',
                    code: "app.sourceProfile.label",
                    role: 'ROLE_SYSTEM_CONFIGURATION'

            ],
            [
                    listId : 'workflowState',
                    link : '/workflowState/index',
                    code: "app.label.workflow.appName",
                    role: 'ROLE_SYSTEM_CONFIGURATION'

            ],
            [
                    listId : 'workflowRule',
                    link : '/workflowRule/index',
                    code: "app.label.workflow.rule.appName",
                    role: 'ROLE_SYSTEM_CONFIGURATION'

            ],
            [
                    listId : 'reportRequestType',
                    link : '/reportRequestType/index',
                    code: "app.label.reportRequest.settings",
                    role: 'ROLE_SYSTEM_CONFIGURATION'

            ],
            [
                    listId : 'taskTemplate',
                    link : '/taskTemplate/index',
                    code: "app.label.task.template.appName",
                    role: 'ROLE_SYSTEM_CONFIGURATION'

            ],
            [
                    listId : 'etlScheduler',
                    link : '/etlSchedule/index',
                    code: "app.label.etlScheduler",
                    role: 'ROLE_SYSTEM_CONFIGURATION'

            ],
            [
                    listId : 'auditLog',
                    link : '/auditLogEvent/list',
                    code: "auditLog.label",
                    role: 'ROLE_SYSTEM_CONFIGURATION'

            ],
            [
                    listId : 'controlPanel',
                    link : '/controlPanel/index',
                    code: "app.label.controlPanel",
                    role: 'ROLE_SYSTEM_CONFIGURATION'

            ],
            [
                    listId : 'reportFooter',
                    link : '/reportFooter/index',
                    code: "app.label.reportFooter.menuItem",
                    role: 'ROLE_SYSTEM_CONFIGURATION'

            ],
            [
                    listId : 'reportFooter',
                    link : '/email/index',
                    code: "app.label.email.menuItem",
                    role: 'ROLE_SYSTEM_CONFIGURATION'

            ],
            [
                    listId : 'reportFooter',
                    link : '/emailTemplate/index',
                    code: "app.label.emailTemplate.appName",
                    role: 'ROLE_SYSTEM_CONFIGURATION'

            ],
            [
                    listId : 'userManagement',
                    link : '/dashboardDictionary/index',
                    code: "app.label.dashBoard.nenuItem",
                    role: 'ROLE_SYSTEM_CONFIGURATION, ROLE_USER_MANAGER'

            ],
            [
                    listId : 'menuPreference',
                    link : '/preference/index',
                    code: "app.label.preference",
                    additionalHtml: "<i class=\"md md-settings\"></i>"

            ],
            [
                    listId : 'reportMonitoring',
                    link : pvreports.monitoring.url,
                    code: "app.label.monitoring",
                    role: 'ROLE_DEV'

            ],
            [
                    listId : 'quartzMonitoring',
                    link : "/quartz",
                    code: "app.label.quartz.monitoring",
                    role: 'ROLE_DEV'

            ],
            [
                    listId : 'logoutLi',
                    link : "/logout",
                    code: "app.label.logout",
                    additionalHtml: "<i class=\"md md-exit-to-app\"></i>"
            ]


    ]
    ]
}


// PVR Public API Token; Applications trying to access PVR's public API must have this token
publicApi.token = null

pvreports.cookie.secured = false //enabled only when HTTPS
pvreports.request.cookieName = 'SESSION'
pvreports.encrypt.data = false

spotfire.serverURL = "http://localhost:8080" //URL of PVReports app server

show.pvq.module = false

show.pvc.module = false

show.pvp.module = false

//oneDrive.secret=URLEncoder.encode("lhcZURMD28~=qwqxNB026~\$", "UTF-8")
oneDrive.secret="lhcZURMD28~=qwqxNB026~\$"

//https://github.com/grails/grails-core/issues/10584 (why on all requests no allow origin header comes)
grails.cors.enabled = false // make true with below allowedOrigins config enabled with actual origins.
grails.cors.allowedOrigins = ['*'] //specify domains to restrict e.g ['http://localhost:9090','http://auth.sso.com'] to allow only two domains.  For SSO server with CORS enable its must to override.
//grails.cors.mappings = ['/api/**':    [allowedOrigins: ["http://localhost:9090"]]] //to Enable CORS for particular sites with particular uri's we can add here and cors enable true by default it doesn't allow any cors requests.

// Using request attribute and session trying to get exception if that doesn't work uncomment below.
//grails.plugin.springsecurity.failureHandler.exceptionMappings = [
//        [exception: com.rxlogix.user.sso.exception.SSOUserDisabledException.name, url: '/login/ssoAuthFail?disabled=true'],
//        [exception: com.rxlogix.user.sso.exception.SSOUserLockedException.name, url: '/login/ssoAuthFail?locked=true'],
//        [exception: com.rxlogix.user.sso.exception.SSOUserNotConfiguredException.name, url: '/login/ssoAuthFail?notfound=true'],
//        [exception: com.rxlogix.user.sso.exception.SSOConfigurationException.name, url: '/login/ssoAuthFail?configError=true']
//]

dataSource {
    type = 'com.zaxxer.hikari.HikariDataSource'
    pooled = true
    jmxExport = true
    logSql = false
    format_sql = true
    use_sql_comments = true
    properties {
        maximumPoolSize = 50
        minimumIdle = 5
        connectionTestQuery = 'SELECT 1 FROM DUAL'
        poolName = 'HikariPool-PVR'
        registerMbeans = true
        dataSourceProperties {
            connectTimeout = 15000
            socketTimeout = 120000
            maintainTimeStats = false
            enableQueryTimeouts = false
            prepStmtCacheSize=256
            prepStmtCacheSqlLimit=2048
            cachePrepStmts=true
            useServerPrepStmts=true
        }
    }
}


dataSources {

    pva {
        driverClassName = 'oracle.jdbc.OracleDriver'
        dialect = "org.hibernate.dialect.Oracle10gDialect"
        type = 'com.zaxxer.hikari.HikariDataSource'
        properties {
            maximumPoolSize = 50
            minimumIdle = 5
            connectionTestQuery = 'SELECT 1 FROM DUAL'
            poolName = 'HikariPool-PVA'
            connectionInitSql = "{call pkg_mart_set_context.set_context('',-99999)}" // Need to give correct MART DB name to allow override here.
            dataSourceProperties {
                connectTimeout = 15000
                socketTimeout = 120000
                maintainTimeStats = false
                enableQueryTimeouts = false
                prepStmtCacheSize = 256
                prepStmtCacheSqlLimit = 2048
                cachePrepStmts = true
                useServerPrepStmts = true
            }
        }
    }

    spotfire {
        driverClassName = 'oracle.jdbc.OracleDriver'
        dialect = "org.hibernate.dialect.Oracle10gDialect"
        type = 'com.zaxxer.hikari.HikariDataSource'
        properties {
            maximumPoolSize = 20
            minimumIdle = 2
            connectionTestQuery = 'SELECT 1 FROM DUAL'
            poolName = 'HikariPool-SPOTFIRE'
            readOnly = true
            dataSourceProperties {
                connectTimeout = 15000
                socketTimeout = 120000
                maintainTimeStats = false
                enableQueryTimeouts = false
                prepStmtCacheSize = 256
                prepStmtCacheSqlLimit = 2048
                cachePrepStmts = true
                useServerPrepStmts = true
            }
        }
    }
}


app.p.set.session.context.key = 'PVD_SECURITY_FIELDS'
app.p.set.session.context.value = 'PVR'
// Environments specific settings block to override.
environments {
    development {

        grails.logging.jul.usebridge = true
        grails.plugin.console.enabled = true
        grails.plugin.databasemigration.updateOnStart = true
        grails.plugin.databasemigration.updateOnStartFileName = 'changelog.groovy'
        grails.show.error500.stackTraceButton = true
        grails.appBaseURL = "http://localhost:8080/reports"

//        Quartz Config
        quartz {
            autoStartup = false
            jdbcStore = true
            purgeQuartzTablesOnStartup = false
            waitForJobsToCompleteOnShutdown = false
            // Allows monitoring in Java Melody (if you have the java melody plugin installed in your grails app)
            exposeSchedulerInRepository = true

            threadPool.class = 'org.quartz.simpl.SimpleThreadPool'
            threadPool {
                threadCount = 21               // Quartz max threads (jobs) at the same time
                threadPriority = 5            // Thread.MIN_PRIORITY level
            }

            scheduler {
                instanceName = 'PVR_INSTANCE_SCHEDULER'
                instanceId = 'AUTO'
                skipUpdateCheck = true // get rid of annoying message on startup
                idleWaitTime = 1000
                misfirePolicy =  'doNothing' //https://stackoverflow.com/questions/31423003/quartz-error-misfire-handling-and-failure-on-job-recovery
            }

            // handles its own transactions with the database
            jobStore.class = 'org.springframework.scheduling.quartz.LocalDataSourceJobStore'
            jobStore {
                misfireThreshold = 5000 //https://stackoverflow.com/questions/32075128/avoiding-misfires-with-quartz
                driverDelegateClass = 'org.quartz.impl.jdbcjobstore.StdJDBCDelegate'
                useProperties = false         // Properties passed to the job are NOT all String objects
                tablePrefix = 'QRTZ_'         // Prefix for the Quartz tables in the database
                isClustered = true            // Tell Quartz it is clustered
                clusterCheckinInterval = 3000 // Check in with the cluster every 3000 ms
            }

            // Detect the jvm shutdown and call shutdown on the scheduler
            plugin {
                shutdownhook.cleanShutdown = true
                shutdownhook.class = 'org.quartz.plugins.management.ShutdownHookPlugin'
            }
        }

        dataSource {
            driverClassName = 'oracle.jdbc.OracleDriver'
            dialect = "org.hibernate.dialect.Oracle10gDialect"
            url = "jdbc:oracle:thin:@localhost:1521/orcl"
            username = 'pvr_5_0'
            password = '{cipher}9nczNincKB4+bk0wWFqVfQ=='
            dbCreate = ''

        }

        dataSources {

            pva {
                url = "jdbc:oracle:thin:@10.100.6.162:1521/PVDHDB"
                username = 'PVR_DB_46_MT'
                password = '{cipher}9nczNincKB4+bk0wWFqVfQ=='
                dbCreate = ''
                properties {
                    connectionInitSql = "{call p_set_context('${app.p.set.session.context.key}','${app.p.set.session.context.value}')}" //To call multiple pkg "{call pkg_mart_set_context.set_context('MART_DB_NAME',-99999); p_set_context('${app.p.set.session.context.key}','${app.p.set.session.context.value}')}"
                }
            }

            pvcm {
                url = 'jdbc:oracle:thin:@10.100.22.137:1521/PVCMDEV'
                username = 'PVCM_SERVICE_DEV_61'
                password = '{cipher}9nczNincKB4+bk0wWFqVfQ=='
                dbCreate = ''
                properties {
                    maximumPoolSize = 20
                    minimumIdle = 2
                    connectionTestQuery = 'SELECT 1 FROM DUAL'
                    poolName = 'HikariPool-PVCM'
                    readOnly = true
                    connectionInitSql = ""
                    dataSourceProperties {
                        connectTimeout = 15000
                        socketTimeout = 120000
                        maintainTimeStats = false
                        enableQueryTimeouts = false
                        prepStmtCacheSize = 256
                        prepStmtCacheSqlLimit = 2048
                        cachePrepStmts = true
                        useServerPrepStmts = true
                    }
                }
            }

            safetySource {
                driverClassName = 'oracle.jdbc.OracleDriver'
                dialect = "org.hibernate.dialect.Oracle10gDialect"
                url = "jdbc:oracle:thin:@10.100.21.181:1521/AS812PVD"
                username = 'argus_app'
                password = 'manager'
                dbCreate = ''
                readOnly = true
                //       properties = oracleProperties
                properties {
                    maximumPoolSize = 20
                    minimumIdle = 2
                    connectionTestQuery = 'SELECT 1 FROM DUAL'
                    poolName = 'HikariPool-SAFETYSOURCE'
                    readOnly = true
                    connectionInitSql = ""
                    dataSourceProperties {
                        connectTimeout = 15000
                        socketTimeout = 120000
                        maintainTimeStats = false
                        enableQueryTimeouts = false
                        prepStmtCacheSize = 256
                        prepStmtCacheSqlLimit = 2048
                        cachePrepStmts = true
                        useServerPrepStmts = true
                    }
                }
            }

            // Uncomment for production with Spotfire installed

            spotfire {
                url = "jdbc:oracle:thin:@10.100.22.212:1521/PVRDEMO"
                username = 'PVR_DB_GDPR_DEV1'
                password = '{cipher}9nczNincKB4+bk0wWFqVfQ=='
                dbCreate = ''
            }

            schemaComparator {
                driverClassName = 'oracle.jdbc.OracleDriver'
                url = 'jdbc:oracle:thin:@10.100.6.181:1521/PVDHDB'
                dialect = "org.hibernate.dialect.Oracle10gDialect"
                username = "SCHEMA_VAL_USER_KR"
                password = '{cipher}9nczNincKB4+bk0wWFqVfQ=='
                readOnly = true
                properties {
                    maximumPoolSize = 20
                    minimumIdle = 2
                    connectionTestQuery = 'SELECT 1 FROM DUAL'
                    poolName = 'HikariPool-SCHEMACOMPARATOR'
                    readOnly = true
                    connectionInitSql = ""
                    dataSourceProperties {
                        connectTimeout = 15000
                        socketTimeout = 120000
                        maintainTimeStats = false
                        enableQueryTimeouts = false
                        prepStmtCacheSize = 256
                        prepStmtCacheSqlLimit = 2048
                        cachePrepStmts = true
                        useServerPrepStmts = true
                    }
                }
            }
        }
    }

    test {

        grails.plugin.databasemigration.updateOnStart = false
        quartz {
            autoStartup = false
            jdbcStore = false
        }

        dataSource {
            dbCreate = "update"
            url = "jdbc:h2:file:./TESTPVR.DB;;MODE=Oracle;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=VALUE,KEY,SORT"
            dialect = "org.hibernate.dialect.H2Dialect"
            driverClassName = 'org.h2.Driver'
            username = 'sa'
            password = ''
            type = null
            pooled = false
            properties  {
                connectionTestQuery = ''
                connectionInitSql = ""
            }
        }

        dataSources {

            pva {
                url = "jdbc:oracle:thin:@10.100.22.49:1521/PVCMDEV"
//                username = 'PVR_DB_62_DEV'
                username = 'PVCM_PVR_70_DEV'
                password = '{cipher}9nczNincKB4+bk0wWFqVfQ=='
                dbCreate = 'none'
                type = null
                pooled = false
            }

            // Uncomment for production with Spotfire installed
            spotfire {
                url = "jdbc:oracle:thin:@10.100.21.165:1521/PVSQADB"
                username = 'spotfireadmin_bqa_dry'
                password = 'oracle123'
                dbCreate = 'none'
                type = null
                pooled = true
            }

            pvcm {
                dbCreate = "update"
                url = "jdbc:h2:file:./TESTPVCM.DB;;MODE=Oracle;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=VALUE,KEY,SORT"
                dialect = "org.hibernate.dialect.H2Dialect"
                driverClassName = 'org.h2.Driver'
                username = 'sa'
                password = ''
                properties  {
                    connectionTestQuery = ''
                    connectionInitSql = ""
                }
                type = null
                pooled = true
            }
        }
    }

    production {

        // Changed due to merck specific requirement will remove later
        pvreports.seeding.user = "pvr_admin"
        grails.logging.jul.usebridge = false
        grails.plugin.console.enabled = true
        grails.plugin.databasemigration.updateOnStart = true
        grails.plugin.databasemigration.updateOnStartFileName = 'changelog.groovy'
        //        grails.appBaseURL="http://reports-dev.eng.rxlogix.com:8080/reports" We would override it for mail links dataAnalysis and Reports Generation mail.
        //or
        //        grails.serverURL = "http://www.changeme.com" // We could override  on any live environment (We are using for absolute URLS and links in dataAnalysis emails)

        quartz {
            autoStartup = false
            jdbcStore = true
            purgeQuartzTablesOnStartup = false
            waitForJobsToCompleteOnShutdown = false
            // Allows monitoring in Java Melody (if you have the java melody plugin installed in your grails app)
            exposeSchedulerInRepository = true

            threadPool.class = 'org.quartz.simpl.SimpleThreadPool'
            threadPool {
                threadCount = 11               // Quartz max threads (jobs) at the same time
                threadPriority = 5            // Thread.MIN_PRIORITY level
            }


            scheduler {
                instanceName = 'PVR_INSTANCE_SCHEDULER'
                instanceId = 'AUTO'
                skipUpdateCheck = true // get rid of annoying message on startup
                idleWaitTime = 1000
                misfirePolicy =  'doNothing' //https://stackoverflow.com/questions/31423003/quartz-error-misfire-handling-and-failure-on-job-recovery
            }

            // handles its own transactions with the database
            jobStore.class = 'org.springframework.scheduling.quartz.LocalDataSourceJobStore'
            jobStore {
                misfireThreshold = 5000 //https://stackoverflow.com/questions/32075128/avoiding-misfires-with-quartz
                driverDelegateClass = 'org.quartz.impl.jdbcjobstore.StdJDBCDelegate'
                useProperties = false         // Properties passed to the job are NOT all String objects
                tablePrefix = 'QRTZ_'         // Prefix for the Quartz tables in the database
                isClustered = true            // Tell Quartz it is clustered
                clusterCheckinInterval = 3000 // Check in with the cluster every 3000 ms
            }

            // Detect the jvm shutdown and call shutdown on the scheduler
            plugin {
                shutdownhook.cleanShutdown = true
                shutdownhook.class = 'org.quartz.plugins.management.ShutdownHookPlugin'
            }
        }

        seeding.threads.pool.size = 1 //based on client requirement can be increased to reduce startup time.

        dataSource {
            driverClassName = 'oracle.jdbc.OracleDriver'
            dialect = "org.hibernate.dialect.Oracle10gDialect"
            url = "DB-URL"
            username = 'DB-USERNAME'
            password = 'DB-ENCODED-PASSWORD'
            dbCreate = ''
        }

        dataSources {
            pva {
                url = "DB-URL"
                username = 'DB-USERNAME'
                password = 'DB-ENCODED-PASSWORD'
                dbCreate = ''
                properties {
                    connectionInitSql = "{call p_set_context('${app.p.set.session.context.key}','${app.p.set.session.context.value}')}"//To call multiple pkg "{call pkg_mart_set_context.set_context('MART_DB_NAME',-99999); p_set_context('${app.p.set.session.context.key}','${app.p.set.session.context.value}')}"
                    //connectionInitSql = "" // {call pkg_mart_set_context.set_context('MART_DB_NAME',-99999)} Need to give correct MART DB name to allow override here.
                }
            }

            spotfire {
                url = "DB-URL"
                username = 'DB-USERNAME'
                password = 'DB-ENCODED-PASSWORD'
                dbCreate = ''
            }
        }
    }
}

grails.gorm.multiTenancy.mode = 'DISCRIMINATOR'
grails.gorm.multiTenancy.tenantResolverClass = 'com.rxlogix.RxTenantResolver'

source.profile.lam.irt.enabled = false

pvreports {
    multiTenancy {
        enabled = false
        defaultTenant = 1L
        allTenant = -99999
        martDBName = 'MART_DB_NAME'
    }
}


//No closure support now.
supported.datasource = ["pva"]

enable.e2b.validation = false

pv.app.pvquality.enabled = false
pv.app.pvcentral.enabled = false
pv.app.pvpublisher.enabled = false
pv.app.pvpublisher.gantt.enabled = false

url.field.regex="\\b(https|http|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.]*[-a-zA-Z0-9+&@#/%=~_|]"

grails {
    scim {
        enabled = false
        schemas = ['urn:scim:schemas:core:2.0']
        api_token = 'cnhsb2dpeA=='
        migrate {
            existing = true //To Allow existing matching username/groupName update with scimId.
        }
    }
}
appConfig.view.name= "VW_ADMIN_APP_CONFIG"
rxlogix.pvreports.publicApi.token = 'zn9MrreyDiATUdoUs/FMmw70qMDExQOya/9LFs1uE5lCp2eCxNeOZCdTgubUCdbYWpLu3bRJRL5zD79iOm+sewLbXnt9r1KbSBNJhWd9BKhbGFhpYPVodA5J7P87aUnXfHLSSXB1F5xTJkCjyMszHA=='
rxlogix.ms.channel.message.url = 'https://graph.microsoft.com/v1.0' //microsoft graph url
rxlogix.ms.teamsId = '' //MS Team Id, for example :  'ed0344bc-70b8-485d-a5f4-37dde1235341'
rxlogix.ms.teams.channelId = '' //MS Team Channel Id, for example :  '19%3aTBVQaB0WK14x5obfgJdzSwM_LwpDpOLuqvHHOx_tRL41%40thread.tacv2'
rxlogix.onedrive.accessToken= '' //Onedrive access token, for example : 'eyJ0eXAiOiJKV1QiLCJub25jZSI6IkdpQ1NaaUlKZ3o5RGkzd3k1djBqaG92ME9CdFdHRmZqTTdVSm55NFMwUWsiLCJhbGciOiJSUzI1NiIsIng1dCI6Ii1LSTNROW5OUjdiUm9meG1lWm9YcWJIWkdldyIsImtpZCI6Ii1LSTNROW5OUjdiUm9meG1lWm9YcWJIWkdldyJ9.eyJhdWQiOiIwMDAwMDAwMy0wMDAwLTAwMDAtYzAwMC0wMDAwMDAwMDAwMDAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC8yYTE4ODVkMi1mMjI4LTRiNDctYjE2Ni01ZDg3MzAyOWExM2QvIiwiaWF0IjoxNjg0NDkwMDk5LCJuYmYiOjE2ODQ0OTAwOTksImV4cCI6MTY4NDQ5NDgzMywiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkFUUUF5LzhUQUFBQXdPTHJXQ3Y3TmsyUEpwdUNYYUd5N05Hd3FWUmF1VDhBSmtFQzNPTHZNSE1ONjBIckVjVEVyUVVsK3VQRlR1K1UiLCJhbXIiOlsid2lhIl0sImFwcF9kaXNwbGF5bmFtZSI6IlBWUCIsImFwcGlkIjoiNTRjMDQ4NjUtMmEyMi00MzBmLTg3ODgtYmU4ZTkyYjNkMGI4IiwiYXBwaWRhY3IiOiIxIiwiZmFtaWx5X25hbWUiOiJUaXdhcmkiLCJnaXZlbl9uYW1lIjoiUHJhZ3lhIiwiaWR0eXAiOiJ1c2VyIiwiaXBhZGRyIjoiMTQuOTcuMjE0LjYyIiwibmFtZSI6IlByYWd5YSBUaXdhcmkiLCJvaWQiOiJjZGY0Zjk5NS1mZWQ1LTQ3M2YtOWZlOS05NTE1YzFmZjRkMjEiLCJwbGF0ZiI6IjgiLCJwdWlkIjoiMTAwMzIwMDFENjQ0RkM2MiIsInJoIjoiMC5BVm9BMG9VWUtpanlSMHV4WmwySE1DbWhQUU1BQUFBQUFBQUF3QUFBQUFBQUFBQmFBRDAuIiwic2NwIjoiQ2hhbm5lbE1lc3NhZ2UuU2VuZCBGaWxlcy5SZWFkV3JpdGUuQWxsIFNpdGVzLlJlYWRXcml0ZS5BbGwgcHJvZmlsZSBvcGVuaWQgZW1haWwiLCJzdWIiOiJLWG1jUWNDQlF6M01xaEltWlM3a1hDXzZ4TGl1LVVERUY1d21xeWRCU1ljIiwidGVuYW50X3JlZ2lvbl9zY29wZSI6Ik5BIiwidGlkIjoiMmExODg1ZDItZjIyOC00YjQ3LWIxNjYtNWQ4NzMwMjlhMTNkIiwidW5pcXVlX25hbWUiOiJwcmFneWEudGl3YXJpQHJ4bG9naXguY29tIiwidXBuIjoicHJhZ3lhLnRpd2FyaUByeGxvZ2l4LmNvbSIsInV0aSI6Inc5bC1XZHBIS0Vxc2tYUlNfR054QUEiLCJ2ZXIiOiIxLjAiLCJ3aWRzIjpbImI3OWZiZjRkLTNlZjktNDY4OS04MTQzLTc2YjE5NGU4NTUwOSJdLCJ4bXNfc3QiOnsic3ViIjoiaFlwLXVFVEJyNG42clpkNjNYUFlfYU9CQU5kcUtyeFpHTndmLWhKN3N2TSJ9LCJ4bXNfdGNkdCI6MTMxNzkxMTAwMX0.axr2vkm4fnOHNKtAfNSzmPCLx0jVuTj0IlmC044r4f6XI4AT1MELsgNv5OWWXCe18FPocLOTOcr6EvMGTg-o0jtCJ4v-X-giEeJ1oVzj7tCSvM1EALTTSBRp86cMKGepZVh6iwGodRyV8nbDzXyR-oZ0R0jz-hntGwVPIlfOQew0SBc4rVN9-rwnrrjqTGRbP2c_YQkeD2ASKPAt2KJyQVPubKD6m77uRdq60fMrItjMfCAQOCmeUd9EVrbv9ceUC8D9qQ7eIxdGNRO3Q47fEUiKhy6d8ECPbq_oAbYIea52wMha7WZHwG6XmcBPJNj_Ai7EWp18o1bRD13AsOfTMw'

safety.source='argus'

//release_6.1
grails.plugin.auditLog.applicationName = 'PV Reports'
grails.plugin.auditLog.defaultActor = 'PVR System User'
grails.plugin.auditLog.titlePrefix = 'PV Reports - '
grails.plugin.auditLog.server = false
grails.plugin.auditLog.logIds = false
grails.plugin.auditLog.showSectionNames = true
grails.plugin.auditLog.deleteKey = 'isDeleted'
grails.plugin.auditLog.applications = ['PV Reports']
grails.plugin.auditLog.logFullClassName = false
grails.plugin.auditLog.syncInterval = 600L
grails.plugin.auditLog.dateTimeFields=['lastUpdated', 'dateCreated','nextRunDate','exportedDate','emailSentOn']
grails.plugin.auditLog.auditDomainClassName = 'com.rxlogix.audit.AuditTrail'
grails.plugin.auditLog.auditChildDomainClassName = 'com.rxlogix.audit.AuditTrailChild'
grails.plugin.auditLog.max.export.records = 50000
grails.plugin.auditLog.defaultIgnore = ['version']
grails.plugin.date.format = "dd-MMM-yyyy HH:mm:ss"
audit.custom.module.names=[]
grails {
    plugin {
        auditLog {
            actorClosure = { request, session ->
                def user = grails.util.Holders.applicationContext.getBean("userService").currentUser
                String userName = user?.username
                String fullName = user?.fullName ?: ""
                String signalUsername = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes().session.signalUsername
                String signalFullname = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes().session.signalFullname
                if (signalUsername)
                    userName = signalUsername
                if (signalFullname)
                    fullName = signalFullname
                if (!userName || (userName == "__grails.anonymous.user__")) userName = "PVR System User"
                return [actor: userName, fullName: fullName]
            }
        }
    }
}

grails {
    plugin {
        auditLog {
            currentActorTimezoneClosure = { request, session ->
                try {
                    if (request.applicationContext.userService) {
                        return request.applicationContext.userService.getCurrentUser().getPreference().getTimeZone()
                    }
                } catch (e) {
                }
                return 'GMT'
            }
        }
    }
}



grails.controllers.upload.maxFileSize=768000
icsr.case.ack.attachment.code.path="/acknowledgment/Code/text()"
icsr.case.ack.attachment.message.identifier.path="/acknowledgment/AttachmentFileName/text()"
icsr.case.ack.attachment.message.text.path="/acknowledgment/Message/text()"
app.gotenberg.api.url="http://localhost:3000"
app.gotenberg.forms.libreoffice.convert="/forms/libreoffice/convert"
icsr.profile.bulk.export.maxCount = 500
rxlogix.pvreports.JSON.load.maxLength=1551754
pvr.allowed.frame.ancestors=[]
pv.app.headers.additional = [
        'Access-Control-Allow-Headers': 'origin, authorization, accept, content-type, x-requested-with, pvi_public_token, *',
        'Access-Control-Allow-Methods': 'GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS',
        'Access-Control-Max-Age': '3600',
        "Access-Control-Allow-Origin": "${-> grails.util.Holders.config?.getProperty('pvr.allowed.frame.ancestors', List, [])?.join(",") ?: '*'}",
        'Access-Control-Allow-Credentials': 'true',
        'Access-Control-Expose-Headers': '*'
]
grails.assets.commonJs=false