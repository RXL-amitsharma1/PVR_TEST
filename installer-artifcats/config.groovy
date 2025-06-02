println "############## Loading External Config###############"

//LDAP Configuration
grails.plugin.springsecurity.providerNames = [/*'samlAuthenticationProvider', */'ldapAuthProvider', 'anonymousAuthenticationProvider']
grails.plugin.springsecurity.logout.handlerNames = [
        'pvrLogoutHandler' ,'rememberMeServices', 'securityContextLogoutHandler'
]
grails.plugin.springsecurity.ldap.context.managerDn = 'LDAP_CONTEXT_MANAGER_DN'
grails.plugin.springsecurity.ldap.context.managerPassword = "{cipher}LDAP_ADMIN_USER_PASSWORD"
grails.plugin.springsecurity.ldap.context.server = 'LDAP_SERVER_FQDN_NAME'
grails.plugin.springsecurity.ldap.search.base = 'LDAP_SEARCH_BASE'
grails.plugin.springsecurity.ldap.users.search.filter = 'LDAP_USERS_SEARCH_FILTER'
grails.plugin.springsecurity.ldap.authorities.groupSearchBase = 'LDAP_GROUP_SEARCH_BASE'
grails.plugin.springsecurity.ldap.search.filter = 'LDAP_SEARCH_FILTER'
grails.plugin.springsecurity.ldap.authorities.groupSearchFilter = 'LDAP_GROUP_SEARCH_FILTER'
grails.plugin.springsecurity.ldap.authorities.groupRoleAttribute = 'cn'
grails.plugin.springsecurity.ldap.authorities.retrieveGroupRoles = false
grails.plugin.springsecurity.ldap.authorities.retrieveDatabaseRoles = true
grails.plugin.springsecurity.ldap.fullName.attribute = "LDAP_FULLNAME_ATTRIBUTE"
grails.plugin.springsecurity.ldap.email.attribute = "LDAP_MAIL_ATTRIBUTE"
grails.plugin.springsecurity.ldap.uid.attribute = "LDAP_USERNAME_ATTRIBUTE"



pvreports.seeding.user = "SEEDING_USER"

grails.appBaseURL = "APP_BASE_URL"

grails.serverURL = "APP_BASE_URL"

helpUrl = "HELP_URL"

shared.directory = "SHARED_DIRECTORY"  // when load balancer is configured
tempDirectory = "TEMP_DIRECTORY"   // when load balancer is configured
pvr.cluster.instance.name = "SERVER_DOMAIN" // when load balancer is configured



grails.plugin.springsecurity.logout.afterLogoutUrl = "/login/auth"

grails.mail.default.from = "DEFAULT_EMAIL_ID"
grails.mail.disabled = false

grails {
    mail {
        host = "SMTP_SERVER"
        port = SMTP_PORT
        username = "SMTP_USER_NAME"
        password = "{cipher}SMTP_USER_PASSWORD"
        props = ["mail.smtp.starttls.enable": "true",
                 "mail.smtp.port"           : "587",
                 "mail.smtp.debug"          : "true"]
    }
}

//When load balancer is configured , below properties are added
hazelcast.enabled = HAZELCAST_ENABLED  //false
hazelcast.server.instance.name = ''
hazelcast.server.port = HAZELCAST_SERVER_PORT
hazelcast.server.auto.increment.port = false
hazelcast.network.nodes = HAZELCAST_NETWORK_NODES
hazelcast.group.name = "HAZELCAST_GROUP_NAME"
hazelcast.group.password = "{cipher}HAZELCAST_GROUP_PASSWORD"

//Spring security SAML | SSO
grails.plugin.springsecurity.saml.active = SAML_ACTIVATED_FLAG
grails.plugin.springsecurity.saml.metadata.url = '/saml/metadata'
grails.plugin.springsecurity.saml.afterLoginUrl = "/"
grails.plugin.springsecurity.saml.userGroupAttribute = "memberOf"
grails.plugin.springsecurity.saml.metadata.providers = ["IDP_METADATA_FILE_PATH"]
grails.plugin.springsecurity.saml.metadata.sp.file = "SP_METADATA_FILE_NAME"
grails.plugin.springsecurity.saml.autoCreate.assignAuthorities = true
grails.plugin.springsecurity.saml.keyManager.storeFile = "KEY_STORE_FILE_PATH"
grails.plugin.springsecurity.saml.keyManager.storePasscode = 'KEY_STORE_FILE_PASSCODE'
grails.plugin.springsecurity.saml.keyManager.storePass = "KEY_STORE_FILE_PASS"
grails.plugin.springsecurity.saml.keyManager.passwords.keyAlias = 'KEY_ALIAS'
grails.plugin.springsecurity.saml.keyManager.passwords.keyPasscode = 'KEY_PASSCODE'
grails.plugin.springsecurity.saml.keyManager.passwords.keyPass = "KEY_PASS"
grails.plugin.springsecurity.saml.keyManager.passwords = [ping: 'Rxlogix1']
grails.plugin.springsecurity.saml.defaultKey = 'DEFAULT_KEY'
grails.plugin.springsecurity.saml.userAttributeMappings.username = 'username'
grails.plugin.springsecurity.saml.metadata.sp.defaults = [
        local                       : SP_DEFAULTS_LOCAL,
        alias                       : 'SP_DEFAULT_ALIAS',
        securityProfile             : 'SP_DEFAULTS_SECURITY_PROFILE',
        signingKey                  : 'SIGNING_KEY',
        encryptionKey               : 'ENCRYPTION_KEY',
        tlsKey                      : 'TLS_KEY',
        requireArtifactResolveSigned: REQUIRE_ARTIFACT_RESOLVE_SIGNED,
        requireLogoutRequestSigned  : REQUIRE_LOGOUT_REQUEST_SIGNED,
        requireLogoutResponseSigned : REQUIRE_LOGOUT_RESPONSE_SIGNED
]

grails.plugin.springsecurity.saml.idp.session.timeout.infinite = false

//Logout URI's
grails.plugin.springsecurity.logout.uri = 'logout'

dataSource {
                url = "jdbc:oracle:thin:@PVR_DB_IP_PORT_NO_AND_SID"
                username = 'PVA_APP_USER'
                password = '{cipher}PVA_USER_PASSWORD'
                dbCreate = ''
}

dataSources {

   	pva {
		url = 'jdbc:oracle:thin:@PVR_DB_IP_PORT_NO_AND_SID'
		username = 'PVR_USER'
                password = '{cipher}PVR_USER_PASSWORD'
                dbCreate = ''
            }

        spotfire {
                url = "jdbc:oracle:thin:@PVR_DB_IP_PORT_NO_AND_SID"
                username = 'PVR_USER'
                password = '{cipher}PVR_USER_PASSWORD'
                dbCreate = ''
            }

}

//Delta config for 4.6 release

//Delta config for 4.7 release

//Delta config for 5.0 release

//Delta config for 5.1 release

//Delta config for 5.2 release
println "##############Finished Loading External Config###############"
