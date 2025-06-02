package com.rxlogix

import com.rxlogix.commandObjects.LdapCommand
import com.rxlogix.user.User
import grails.core.GrailsApplication
import grails.validation.ValidationException
import org.springframework.ldap.core.AttributesMapper

import javax.naming.AuthenticationException
import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.NamingException
import javax.naming.directory.Attributes
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult

class LdapService {

    static transactional = false

    GrailsApplication grailsApplication
    def ldapTemplate
    def ldapService
    def userService
    def CRUDService

    /**
     * For all active users in the application User table, update their email and fullName properties with the values in LDAP
     * since LDAP is considered the source of record. This then avoids the need to search LDAP for those properties during
     * normal usage which is a performance killer.
     *
     * This can be invoked manually, but is also done daily via a Quartz job.
     */
    def mirrorLdapValues() {

        def activeUsers = userService.getActiveUsers()
        String uid = grailsApplication.config.grails.plugin.springsecurity.ldap.uid.attribute
        for (User user: activeUsers) {
            def ldapEntry = ldapService.getLdapEntry("$uid=$user.username")

            User existingUser = User.findByUsernameAndEnabled(ldapEntry[0]?.getUserName(), true)

            if (existingUser) {
                existingUser.fullName = ldapEntry[0]?.getFullName()
                existingUser.email = ldapEntry[0]?.getEmail()
                try {
                    /*  This method is used in a daily Quartz job and creating audit log entries for all users daily will
                        create a large number of useless entries.
                      */
                    CRUDService.saveWithoutAuditLog(existingUser)
                } catch (ValidationException ve) {
                    //There's nothing we can do; move onto the next record
                }

            }
        }
    }

    /**
     * Return an entire LDAP entry.  This is useful when you want to process multiple LDAP attributes for a single LDAP entry
     * and calling getLdapAttribute() multiple times would be more tedious.
     * @param filter (the search criteria)                          i.e. "uid=$username" or "uid=" + user.username
     * @return
     */
    def List<LdapCommand> getLdapEntry(String filter) {
        String searchBase = grailsApplication.config.grails.plugin.springsecurity.ldap.search.base
        return ldapTemplate.search(searchBase, filter, new LdapCommandMapper())
    }

    /**
     * Used to get a single LDAP attribute such as an email or the fullname.  Currently unused, but could be useful
     * in the future.
     * @param filter (the search criteria)                          i.e. "uid=$username" or "uid=" + user.username
     * @param attribute (the attribute we want to return back)      i.e. "cn"
     * @return
     */
    def getLdapAttribute(String filter, String attribute) {
        String searchBase = grailsApplication.config.grails.plugin.springsecurity.ldap.search.base

        def attributeMapper = new AttributesMapper() {
            public Object mapFromAttributes(Attributes attrs) throws NamingException {
                return attrs?.get(attribute)?.get()?.toString()
            }
        }

        return ldapTemplate.search(searchBase, filter, attributeMapper)
    }

    /**
     * Searches LDAP by a search term and returns a list of values containing the username, fullName and email which is
     * used to populated a Select2 dropdown to add a new user.
     * @param filter    i.e. "uid=$searchTerm"
     * @return
     */
    def searchLdapToAddUser(String filter) {
        String searchBase = grailsApplication.config.grails.plugin.springsecurity.ldap.search.base
        String uid = grailsApplication.config.grails.plugin.springsecurity.ldap.uid.attribute
        String fullName = grailsApplication.config.grails.plugin.springsecurity.ldap.fullName.attribute
        String email = grailsApplication.config.grails.plugin.springsecurity.ldap.email.attribute
        //We are trying to get only those attributes which we would need to display.
        return ldapTemplate.search(searchBase, filter, SearchControls.SUBTREE_SCOPE, (String[]) [uid, fullName, email],
                new AttributesMapper() {
                    public Object mapFromAttributes(Attributes attrs) throws NamingException {
                        List ldapResults = []
                        Map ldapResultsMap = [:]
                        String key = attrs?.get(uid)?.get()?.toString()
                        String value =
                                attrs?.get(uid)?.get()?.toString() + " - " +
                                        attrs?.get(fullName)?.get()?.toString() + " - " +
                                        attrs?.get(email)?.get()?.toString()
                        if (!User.countByUsername(key)) {
                            ldapResultsMap.put(key, value)
                            ldapResults.add(ldapResultsMap)
                        }
                        return ldapResults
                    }
                })
    }

    /**
     * This is a convenience class to make it easier to transfer LDAP entries to the User object.
     */
    private class LdapCommandMapper implements AttributesMapper {
        public Object mapFromAttributes(Attributes attrs) throws NamingException {
            LdapCommand ldapCommand = new LdapCommand();
            String uid = grailsApplication.config.grails.plugin.springsecurity.ldap.uid.attribute
            String fullName = grailsApplication.config.grails.plugin.springsecurity.ldap.fullName.attribute
            String email = grailsApplication.config.grails.plugin.springsecurity.ldap.email.attribute
            ldapCommand.setUserName((String)attrs?.get(uid)?.get())
            ldapCommand.setFullName((String)attrs?.get(fullName)?.get())
            ldapCommand.setEmail((String)attrs?.get(email)?.get())
            return ldapCommand;
        }
    }

    boolean isLoginPasswordValid(String login, String password) {

        ConfigObject ldap = grailsApplication.config.grails.plugin.springsecurity.ldap
        Properties serviceEnv = new Properties()
        serviceEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
        serviceEnv.put(Context.PROVIDER_URL, ldap.context.server)
        serviceEnv.put(Context.SECURITY_AUTHENTICATION, "simple")
        serviceEnv.put(Context.SECURITY_PRINCIPAL, ldap.context.managerDn as String)
        serviceEnv.put(Context.SECURITY_CREDENTIALS, ldap.context.managerPassword as String)
        def serviceCtx = new InitialDirContext(serviceEnv)

        String uid = ldap.uid.attribute
        SearchControls sc = new SearchControls()
        sc.setReturningAttributes([uid] as String[])
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE)
        String searchFilter = "($uid=$login)"
        NamingEnumeration<SearchResult> results = serviceCtx.search(ldap.search.base as String, searchFilter, sc)

        // get the users DN (distinguishedName) from the result
        if (!results.hasMore()) return false
        SearchResult result = results.next()
        String distinguishedName = result.getNameInNamespace()

        // attempt another authentication, now with the user
        Properties authEnv = new Properties();
        authEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
        authEnv.put(Context.PROVIDER_URL, ldap.context.server)
        authEnv.put(Context.SECURITY_PRINCIPAL, distinguishedName)
        authEnv.put(Context.SECURITY_CREDENTIALS, password)
        try {
            new InitialDirContext(authEnv)
            //Authentication successful
            return true;
        } catch (AuthenticationException e) {
            //Authentication sfault
        }
        return false;
    }

}
