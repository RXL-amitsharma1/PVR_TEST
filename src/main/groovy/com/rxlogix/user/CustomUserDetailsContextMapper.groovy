package com.rxlogix.user

import com.rxlogix.enums.AuthType
import com.rxlogix.enums.UserType
import grails.core.GrailsApplication
import groovy.util.logging.Slf4j
import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.core.DirContextOperations
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper

@Slf4j
class CustomUserDetailsContextMapper implements UserDetailsContextMapper {

    GrailsApplication grailsApplication

    @Override
    UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection authorities) {
        User user = null

        User.withNewSession {
            user = User.findByUsernameIlike(username)
        }

        try {
            if (!user) {
                throw new UsernameNotFoundException('User not in local database table')
            }

            if (!user.enabled) throw new DisabledException("Account Disabled")
            if (user.accountExpired) throw new AccountExpiredException("Account Expired")
            if (user.passwordExpired) throw new CredentialsExpiredException("Password Expired")
            if (user.accountLocked) throw new LockedException("Account is locked")
            if(user.type == UserType.NON_LDAP && user.passwordExpired){
                throw new CredentialsExpiredException("Password Expired")
            }
            user.authType = AuthType.Database
        } catch (AuthenticationException e) {
            log.error("AuthenticationException: ${e.message} for ${username}")
            throw e
        }
        Map<String, List<Object>> attributes = [:]

        def userDetails = new CustomUserDetails(user.username,user.password, user.enabled,
                !user.accountExpired,
                user.passwordExpired,
                !user.accountLocked,
                authorities,
                user.id,
                user.fullName,
                user.email,
                user.type,
                AuthType.Database,
                user.passwordModifiedTime ?: user.dateCreated, attributes)
        return userDetails
    }

    @Override
    void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        throw new IllegalStateException("Only retrieving data from LDAP is currently supported")
    }

}
