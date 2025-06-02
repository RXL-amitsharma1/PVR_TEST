package com.rxlogix.dto

import com.rxlogix.user.User
import groovy.transform.CompileStatic

// DTO for sending data of blinded users based on field profile to PVS
@CompileStatic
class BlindedUsersDTO {

    String userName
    Boolean isBlinded = null
    Boolean isDeleted = null
    List<String> blindedFieldIds
    List<String> protectedFieldIds

    BlindedUsersDTO(String userName) {
        this.userName = userName
    }

    BlindedUsersDTO(Boolean isDeleted, User user, String userName) {
        this.userName = userName
        if (isDeleted) {
            this.isDeleted = true
        } else {
            this.isDeleted = !user.enabled
            this.isBlinded = user.isBlinded ?: false
            this.blindedFieldIds = User.getBlindedFieldsForUser(user).collect {it.name}
            this.protectedFieldIds = User.getProtectedFieldsForUserForPVS(user).collect {it.name}
        }
    }
}