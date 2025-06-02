import com.rxlogix.DBTokenStore
import com.rxlogix.csrf.CustomAccessDeniedHandler
import com.rxlogix.csrf.CustomRequestMatcher
import com.rxlogix.e2b.LocalFolderDriveService
import com.rxlogix.e2b.OneDriveService
import com.rxlogix.file.ArgusFileAttachmentService
import com.rxlogix.file.PVCMFileAttachmentService
import com.rxlogix.file.FileAttachmentLocator
import com.rxlogix.health.ConnectedAppHealthIndicator
import com.rxlogix.jasperserver.converters.*
import com.rxlogix.liquibase.MigrationCallbacks
import com.rxlogix.scim.ScimGroupRepositoryImpl
import com.rxlogix.scim.ScimUserRepositoryImpl
import com.rxlogix.security.DummyFilter
import com.rxlogix.security.PvrAuthenticationSuccessHandler
import com.rxlogix.security.PvrLogoutHandler
import com.rxlogix.security.RxSaml2LogoutResponseFilter
import com.rxlogix.security.SecurityEventListener
import com.rxlogix.security.SecurityHeadersFilter
import com.rxlogix.user.CustomUserDetailsContextMapper
import com.rxlogix.user.CustomUserDetailsService
import com.rxlogix.util.marshalling.CustomMarshallerRegistry
import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.web.authentication.GrailsUsernamePasswordAuthenticationFilter
import grails.plugin.springsession.*

import grails.plugins.quartz.listeners.ExceptionPrinterJobListener
import grails.plugins.quartz.listeners.SessionBinderJobListener
import grails.util.Holders
import org.cryptacular.util.CertUtil
import org.cryptacular.util.KeyPairUtil
import org.grails.plugin.springsecurity.saml.SpringSecuritySamlGrailsPlugin
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator
import org.springframework.boot.actuate.ldap.LdapHealthIndicator
import org.springframework.boot.actuate.mail.MailHealthIndicator
import org.springframework.context.support.ConversionServiceFactoryBean
import org.springframework.core.io.Resource
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.ldap.SpringSecurityLdapTemplate
import org.springframework.security.saml2.core.Saml2X509Credential
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding
import org.springframework.security.web.access.ExceptionTranslationFilter
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import org.springframework.security.web.csrf.CsrfFilter
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository
import org.springframework.security.web.session.ConcurrentSessionFilter
import org.springframework.security.web.util.matcher.AndRequestMatcher
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.session.MapSessionRepository
import org.springframework.session.security.SpringSessionBackedSessionRegistry
import org.springframework.session.web.http.DefaultCookieSerializer
import org.springframework.session.web.http.SessionRepositoryFilter
import com.rxlogix.localization.LocalizationMessageSource
import com.rxlogix.ApplicationNotificationService
import com.rxlogix.HazelcastNotificationService
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPrivateKey

// Place your Spring DSL code here
beans = {
    messageSource(LocalizationMessageSource)

    conversionService(ConversionServiceFactoryBean)

    migrationCallbacks(MigrationCallbacks) { bean ->
        grailsApplication = ref('grailsApplication')
    }

    authenticationProcessingFilter(GrailsUsernamePasswordAuthenticationFilter) {
        authenticationManager = ref('authenticationManager')
        sessionAuthenticationStrategy = ref('sessionAuthenticationStrategy')
        authenticationSuccessHandler = ref('authenticationSuccessHandler')
        authenticationFailureHandler = ref('authenticationFailureHandler')
        rememberMeServices = ref('rememberMeServices')
        authenticationDetailsSource = ref('authenticationDetailsSource')
        requiresAuthenticationRequestMatcher = ref('filterProcessUrlRequestMatcher')
        usernameParameter = 'username' // username
        passwordParameter = 'password' // password
        continueChainBeforeSuccessfulAuthentication = false // false
        allowSessionCreation = true // true
        postOnly = true // true
        storeLastUsername = false // false
    }

    customMarshallerRegistry(CustomMarshallerRegistry)

    userDetailsService(CustomUserDetailsService) {
        isSamlActive = Holders.config.getProperty('grails.plugin.springsecurity.saml.active', Boolean, false)
        grailsApplication = ref('grailsApplication')
        samlUserAttributeMappings = Holders.config.getProperty('grails.plugin.springsecurity.saml.userAttributeMappings', Map, [:])
    }

    if (grailsApplication.config.getProperty('grails.plugin.springsecurity.ldap.active', Boolean)) {

        ldapUserDetailsMapper(CustomUserDetailsContextMapper) {
            grailsApplication = ref('grailsApplication')
        }

        ldapTemplate(SpringSecurityLdapTemplate, ref('contextSource')) {
            ignorePartialResultException = true
        }
    }
    securityEventListener(SecurityEventListener)

    authenticationSuccessHandler(PvrAuthenticationSuccessHandler) {
        /* Reusing the security configuration */
        def conf = SpringSecurityUtils.securityConfig
        /* Configuring the bean */
        requestCache = ref('requestCache')
        defaultTargetUrl = conf.successHandler.defaultTargetUrl // '/'
        alwaysUseDefaultTargetUrl = conf.successHandler.alwaysUseDefault // false
        targetUrlParameter = conf.successHandler.targetUrlParameter // 'spring-security-redirect'
        ajaxSuccessUrl = conf.successHandler.ajaxSuccessUrl // '/login/ajaxSuccess'
        useReferer = conf.successHandler.useReferer // false
        redirectStrategy = ref('redirectStrategy')
        if (Holders.config.getProperty('csrfProtection.enabled', Boolean)) {
            csrfFilter = ref('csrfFilter')
        }
    }

    basicProcessingFilterEntryPoint(BasicAuthenticationEntryPoint) {
        realmName = 'Protected Area'
    }

    /**
     * If there is no BASIC auth header, this filter will display a 401 error thanks to the entry point
     */
    basicAuthExceptionTranslationFilter(ExceptionTranslationFilter, ref('basicProcessingFilterEntryPoint'))

    binaryDataResourceConverter(BinaryDataResourceConverter) {
        fileResourceConverter = ref('fileResourceConverter')
        contentResourceConverter = ref('contentResourceConverter')
    }
    contentResourceConverter(ContentResourceConverter)
    fileResourceConverter(FileResourceConverter) {
        repositoryService = ref('repositoryService')
    }
    folderResourceConverter(FolderResourceConverter)
    lookupResourceConverter(LookupResourceConverter) {
        resourceConverterProvider = ref('resourceConverterProvider')
    }
    reportUnitResourceConverter(ReportUnitResourceConverter) {
        resourceReferenceConverterProvider = ref('resourceReferenceConverterProvider')
    }
    resourceConverterProvider(ResourceConverterProvider) {
        binaryDataResourceConverter = ref('binaryDataResourceConverter')
    }
    resourceReferenceConverterProvider(ResourceReferenceConverterProvider) {
        resourceConverterProvider = ref('resourceConverterProvider')
        repositoryService = ref('repositoryService')
    }

    if (Holders.config.getProperty('csrfProtection.enabled', Boolean)) {
        customAccessDeniedHandler(CustomAccessDeniedHandler)
        customRequestMatcher(CustomRequestMatcher)
        csrfFilter(CsrfFilter, new HttpSessionCsrfTokenRepository()) {
            accessDeniedHandler = ref('customAccessDeniedHandler')
            requireCsrfProtectionMatcher = ref('customRequestMatcher')
        }
    }

    cookieSerializer(DefaultCookieSerializer) {
        if (Holders.config.getProperty('pvreports.request.cookieName')) {
            cookieName = Holders.config.getProperty('pvreports.request.cookieName')
            sameSite = Holders.config.getProperty('pvreports.request.cookie.sameSite', String, null)
            if (Holders.config.containsKey('pvreports.request.cookie.secure')) {
                useSecureCookie = Holders.config.getProperty('pvreports.request.cookie.secure', Boolean)
            }
        }
    }

//    httpSessionStrategy(CookieHttpSessionStrategy) {
//        cookieSerializer = ref('cookieSerializer')
//    }

    if (Holders.config.getProperty('springsession.enabled', Boolean)) {
        log.info("Initializing spring session...")
        springSessionConfigProperties(SpringSessionConfigProperties, Holders.config.getProperty('springsession', Map))
        if (Holders.config.getProperty('hazelcast.enabled', Boolean)) {
            log.info("Hazelcast is enabled via config, starting Hazelcast Instance...")
            hazelcastInstance(HazelcastInstanceInitializer) {
                grailsApplication = ref('grailsApplication')
                hazelcastConfig = Holders.config.hazelcast
                springSessionConfigProperties = ref('springSessionConfigProperties')
            }
            sessionStoreConfiguration(HazelcastStoreSessionConfig, ref('grailsApplication'), ref('springSessionConfigProperties'))
        } else {
            sessionRepository(MapSessionRepository) {
                defaultMaxInactiveInterval = Holders.config.springsession.timeout.interval as Integer
            }
            sessionStoreConfiguration(SpringSessionConfig)
        }

        httpSessionSynchronizer(HttpSessionSynchronizer, ref('springSessionConfigProperties'))

        springSessionRepositoryFilter(SessionRepositoryFilter, ref('sessionRepository'))

        log.info("...spring session initialization done.")
    }


    if (Holders.config.getProperty('springsession.enabled', Boolean) && Holders.config.getProperty('hazelcast.enabled', Boolean)) {
        sessionRegistry(SpringSessionBackedSessionRegistry, ref('sessionRepository'))
    } else {
        sessionRegistry(SessionRegistryImpl)
    }

    sessionFixationProtectionStrategy(SessionFixationProtectionStrategy) {
        migrateSessionAttributes = true
        alwaysCreateSession = true
    }

    pvrLogoutHandler(PvrLogoutHandler) {
        sessionRegistry = ref('sessionRegistry')
    }
    //Initiate the bean
    if (Holders.config.getProperty('singleUserSession.enabled', Boolean)) {
        concurrentSingleSessionAuthenticationStrategy(ConcurrentSessionControlAuthenticationStrategy, ref('sessionRegistry')) {
            exceptionIfMaximumExceeded = !Holders.config.getProperty('singleUserSession.invalidateOld', Boolean)
        }

        registerSessionAuthenticationStrategy(RegisterSessionAuthenticationStrategy, ref('sessionRegistry'))

        sessionAuthenticationStrategy(CompositeSessionAuthenticationStrategy, [ref('concurrentSingleSessionAuthenticationStrategy'), ref('sessionFixationProtectionStrategy'), ref('registerSessionAuthenticationStrategy')])

        concurrentSessionFilter(ConcurrentSessionFilter, ref('sessionRegistry'), '/login/authfail?sessionInvalidated=true') {
            redirectStrategy = ref('redirectStrategy')
        }
    } else {
        concurrentSessionFilter(DummyFilter)
    }

    webSocketConfig websocket.WebSocketConfig

//    if (Holders.config.saml.lb.enabled && Holders.config.grails.plugin.springsecurity.saml.active) {
//        contextProvider(org.springframework.security.saml.context.SAMLContextProviderLB) {
//            scheme = Holders.config.saml.lb.scheme
//            serverName = Holders.config.saml.lb.serverName
//            serverPort = Holders.config.saml.lb.port as Integer
//            contextPath = Holders.config.saml.lb.contextPath
//        }
//    }

    nonclusterdQuartzScheduler(org.springframework.scheduling.quartz.SchedulerFactoryBean) {
        Properties properties = new Properties()
        properties.setProperty('org.quartz.threadPool.threadCount', '2')
        properties.setProperty('org.quartz.scheduler.misfirePolicy', 'doNothing')
        quartzProperties = properties
        autoStartup = false
        waitForJobsToCompleteOnShutdown = false
        exposeSchedulerInRepository = false
        jobFactory = ref('quartzJobFactory')
        globalJobListeners = [ref("${SessionBinderJobListener.NAME}"), ref("${ExceptionPrinterJobListener.NAME}")]
    }

    icsrDriveService(LocalFolderDriveService)

    if (Holders.config.getProperty('safety.source') == "argus") {
        argusFileAttachmentService(ArgusFileAttachmentService)
        fileAttachmentLocator(FileAttachmentLocator) {
            argusFileAttachmentService = ref('argusFileAttachmentService')
        }
    } else if (Holders.config.getProperty('safety.source') == "pvcm") {
        pvcmFileAttachmentService(PVCMFileAttachmentService)
        fileAttachmentLocator(FileAttachmentLocator) {
            pvcmFileAttachmentService = ref('pvcmFileAttachmentService')
        }
    }

    if (Holders.config.getProperty('icsr.drive.oneclient', Map)) {
        icsrDriveService(OneDriveService)
    }

    if (Holders.config.getProperty('grails.mail.oAuth.enabled', Boolean)) {
        tokenStore(DBTokenStore)
    }

    if (Holders.config.grails?.plugin?.springsecurity?.saml?.active) {
        def loginEndpointURL = grailsApplication.config.rxlogix.security.saml.provider.name
        if (loginEndpointURL) {
            authenticationEntryPoint(LoginUrlAuthenticationEntryPoint, "/saml2/authenticate/${loginEndpointURL}")
        } else {
            log.error "Login endpoint URL not found in SAML metadata providers."
        }
    }

    scimUserRepository(ScimUserRepositoryImpl) {
        CRUDService = ref('CRUDService')
        allowExistingUserMigrate = grailsApplication.config.getProperty('grails.scim.migrate.existing', Boolean, false)
    }

    scimGroupRepository(ScimGroupRepositoryImpl) {
        CRUDService = ref('CRUDService')
        allowExistingGroupMigrate = grailsApplication.config.getProperty('grails.scim.migrate.existing', Boolean, false)
    }

    if (grailsApplication.config.endpoints.enabled) {
        dataSourceHealthIndicator(DataSourceHealthIndicator, ref('dataSource'))
        dataSourcePVAHealthIndicator(DataSourceHealthIndicator, ref('dataSource_pva'))
        dataSourceSpotfireHealthIndicator(DataSourceHealthIndicator, ref('dataSource_spotfire'))
        mailHealthIndicator(MailHealthIndicator, ref('mailSender'))
        if (grailsApplication.config.getProperty('grails.plugin.springsecurity.ldap.active', Boolean)) {
            ldapHealthIndicator(LdapHealthIndicator, ref('ldapTemplate'))
        }
        if (grailsApplication.config.getProperty('pvsignal.url')) {
            signalHealthIndicator(ConnectedAppHealthIndicator) {
                appName = 'Signal'
                healthUrl = grailsApplication.config.getProperty('pvsignal.url') + '/ping'
            }
        }
    }

    applicationNotificationService(ApplicationNotificationService) {
        configurationDataSource = ref('dataSource_pva') //pva data source
        configuredAppName = "PVR"
        appRedisService = ""
        grailsApplication = ref('grailsApplication')
    }

    hazelcastNotificationService(HazelcastNotificationService) {
        configurationDataSource = ref('dataSource_pva') //pva data source
        configuredAppName = "PVR"
        grailsApplication = ref('grailsApplication')
    }
    applicationConfigService(com.rxlogix.ApplicationConfigService) {
        refreshDependenciesService = ref('refreshConfigService')
        grailsApplication = ref('grailsApplication')
    }

    securityHeadersFilter(SecurityHeadersFilter)
    SpringSecurityUtils.registerFilter('securityHeadersFilter', SecurityFilterPosition.FIRST.order + 1)

    // Define the session hijacking filter bean
//    sessionHijackingFilter(SessionHijackingFilter, ref('sessionRegistry'))

    // Register the session hijacking filter in the Spring Security filter chain
//    SpringSecurityUtils.registerFilter('sessionHijackingFilter', SecurityFilterPosition.SESSION_MANAGEMENT_FILTER.order - 1)

    //TODO we need to remove later once we have fix in SpringSaml plugin
    def conf = grails.util.Holders.config.grails.plugin.springsecurity
    if (conf.saml.active) {
        println "Defining overridden relyingPartyRegistrationRepository via Resources.groovy"
        List registrations = []
        def storePass = conf.saml.keyManager.storePass.toCharArray()
        def keystore = null
        if (conf.saml.keyManager.storeFile) {
            keystore = loadKeystore(getResource(conf.saml.keyManager.storeFile), storePass)
        }
        log.debug "Dynamically defining bean metadata providers... "
        def providers = conf.saml.metadata.providers
        providers.each { registrationId, metadataLocation ->
            println "Registering registrationId ${registrationId} from ${metadataLocation}"
            registrations << registrationFromMetadata(conf, registrationId, metadataLocation, keystore)
        }
        relyingPartyRegistrationRepository(InMemoryRelyingPartyRegistrationRepository, registrations)
        //disable logout response verification
        if (conf.saml.logout.response.verify.disable) {
            println "Defining overridden saml2LogoutResponseFilter via Resources.groovy"
            saml2LogoutResponseFilter(RxSaml2LogoutResponseFilter, ref('relyingPartyRegistrationRepositoryResolver'),
                    ref('logoutResponseValidator'), ref('logoutSuccessHandler')) {
                logoutRequestMatcher = new AndRequestMatcher(new AntPathRequestMatcher('/logout/saml2/slo'),
                        new SpringSecuritySamlGrailsPlugin.ParameterRequestMatcher("SAMLResponse"))
                logoutRequestRepository = ref('logoutRequestRepository')
            }
        }
    }
}


X509Certificate readCertificate(Resource resource) throws Exception {
    return resource.getInputStream().withCloseable { is -> return CertUtil.readCertificate(is)
    }
}

RSAPrivateKey readPrivateKey(Resource resource) throws Exception {
    return resource.getInputStream().withCloseable { is -> return KeyPairUtil.readPrivateKey(is)
    }
}

KeyStore loadKeystore(resource, storePass) {
    KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType())
    resource.URL.withInputStream { is -> keystore.load(is, storePass)
    }
    return keystore
}

def registrationFromMetadata(conf, registrationId, metadataLocation, keystore) {

    String relyingPartyEntityId = conf.saml.metadata.sp.defaults.entityID ?: "{baseUrl}/saml2/service-provider-metadata/{registrationId}"
    String assertionConsumerServiceLocation = conf.saml.metadata.sp.defaults.assertionConsumerService ?: "{baseUrl}/login/saml2/sso/{registrationId}"
    String relyingSingleLogoutServiceLocation = conf.saml.metadata.sp.defaults.singleLogoutService ?: "{baseUrl}/logout/saml2/sso/{registrationId}"

    String signingKey = conf.saml.metadata.sp.defaults.signingKey

    Saml2X509Credential relyingPartySigningCredential

    if (conf.saml.keyManager.storeFile && keystore != null) {
        KeyStore.PrivateKeyEntry signingEntry
        if (conf.saml.keyManager.passwords) {
            def entryPass = conf.saml.keyManager.passwords[signingKey]
            if (entryPass) {
                def passwordProtection = new KeyStore.PasswordProtection(entryPass.toCharArray())
                signingEntry = (KeyStore.PrivateKeyEntry) keystore.getEntry(signingKey, passwordProtection)
            } else {
                throw new IOException("Password for keystore entry ${signingKey} cannot be found at " + "'grails.plugin.springsecurity.saml.keyManager.passwords.${signingKey}' in your application.yml.")
            }
        }
        if (signingEntry == null) {
            throw new IOException("Keystore entry ${signingKey} cannot be loaded from file '${conf.saml.keyManager.storeFile}'. " + "Please check that the path configured in " + "'grails.plugin.springsecurity.saml.keyManager.storeFile' in your application.yml is correct.")
        }

        relyingPartySigningCredential = new Saml2X509Credential(signingEntry.privateKey,
                signingEntry.certificate, Saml2X509Credential.Saml2X509CredentialType.SIGNING, Saml2X509Credential.Saml2X509CredentialType.DECRYPTION)
    } else if (conf.saml.keyManager.privateKeyFile || conf.saml.keyManager.certificateFile) {

        Resource certificateFile = getResource(conf.saml.keyManager.certificateFile)
        Resource privateKeyFile = getResource(conf.saml.keyManager.privateKeyFile)

        if (!certificateFile.exists()) {
            throw new FileNotFoundException("Public key file '${conf.saml.keyManager.certificateFile}' configured " + "in 'grails.plugin.springsecurity.saml.keyManager.certificateFile' could not be found.")
        }
        if (!privateKeyFile.exists()) {
            throw new FileNotFoundException("Private key file '${conf.saml.keyManager.privateKeyFile}' configured " + "in 'grails.plugin.springsecurity.saml.keyManager.privateKeyFile' could not be found.")
        }

        X509Certificate publicKey = (X509Certificate) readCertificate(certificateFile)
        PrivateKey privateKey = (PrivateKey) readPrivateKey(privateKeyFile)

        relyingPartySigningCredential = new Saml2X509Credential(privateKey, publicKey,
                Saml2X509Credential.Saml2X509CredentialType.SIGNING, Saml2X509Credential.Saml2X509CredentialType.DECRYPTION)
    }

    if (!relyingPartySigningCredential) {
        throw new IOException("Neither the keystore nor PEM files could be loaded. Please configure either " + "'grails.plugin.springsecurity.saml.keyManager.storeFile' or 'grails.plugin.springsecurity.saml.keyManager.privateKeyFile' " + "and 'grails.plugin.springsecurity.saml.keyManager.certificateFile'.")
    }

    return RelyingPartyRegistrations.fromMetadataLocation(metadataLocation)
            .registrationId(registrationId)
            .entityId(relyingPartyEntityId)
            .assertionConsumerServiceLocation(assertionConsumerServiceLocation)
            .singleLogoutServiceLocation(relyingSingleLogoutServiceLocation)
            .signingX509Credentials((c) -> c.add(relyingPartySigningCredential))
            .decryptionX509Credentials((c) -> c.add(relyingPartySigningCredential))
            .singleLogoutServiceBinding(Saml2MessageBinding.from(Holders.config.getProperty('grails.plugin.springsecurity.saml.logout.binding.urn', 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST')))
            .build()
}