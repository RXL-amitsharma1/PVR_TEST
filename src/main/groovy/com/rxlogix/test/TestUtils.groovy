package com.rxlogix.test

import com.rxlogix.config.SourceProfile
import com.rxlogix.enums.SourceProfileTypeEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole

class TestUtils {

    private static final rootUser = "rootUser"

    static Role fetchOrCreateRole(String authority = 'ROLE_ADMIN') {
        Role role = Role.findByAuthority(authority)
        if(!role) {
            role = new Role(authority: authority, createdBy: rootUser, modifiedBy: rootUser).save(flush: true)
        }
        return role
    }

    static User fetchOrCreateAdminUser(String username = 'admin') {
        User.metaClass.encodePassword = { "password" }
        User adminUser = User.findByUsername(username)
        if(!adminUser) {
            Preference preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: rootUser, modifiedBy: rootUser)
            adminUser = new User(username: username, password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: rootUser, modifiedBy: rootUser)
            adminUser.save(flush: true)
            UserRole.create(adminUser, fetchOrCreateRole('ROLE_ADMIN'), true)
        }
        return adminUser
    }

    static SourceProfile createSourceProfile(){
        SourceProfile sourceProfile = new SourceProfile(sourceId: 9999, sourceName: 'All Source Profile', sourceAbbrev: 'ALLSP', sourceProfileTypeEnum: SourceProfileTypeEnum.ALL, isCentral: false, isDeleted: false,dateRangeTypes: [])
        sourceProfile.save()
        return sourceProfile
    }
}
