package com.rxlogix.rest_v2

import com.jaspersoft.jasperserver.dto.authority.ClientRole
import com.jaspersoft.jasperserver.dto.authority.ClientUser
import com.rxlogix.util.MiscUtil
import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class UsersRestController {
    def userService

    def show(String userID) {
        def currentUser = userService.currentUser
        ClientUser clientUser = new ClientUser(
                fullName: currentUser.fullName,
                emailAddress: currentUser.email,
                externallyDefined: false,
                enabled: true,
                previousPasswordChangeTime: new Date(),
                tenantId: "Finance",
                username: currentUser.username,
                roleSet: [new ClientRole(name: "ROLE_USER", externallyDefined: false)] as Set
        )
        return render(text: MiscUtil.marshal(clientUser), contentType: "text/xml", encoding: "UTF-8")
    }
}
