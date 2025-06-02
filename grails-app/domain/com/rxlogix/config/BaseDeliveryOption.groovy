package com.rxlogix.config

import com.rxlogix.config.OneDriveUserSettings
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
abstract class BaseDeliveryOption {
    List<User> sharedWith = []
    List<UserGroup> sharedWithGroup = []
    List<String> emailToUsers = []
    List<ReportFormatEnum> attachmentFormats = []
    String additionalAttachments
    String oneDriveFolderName
    String oneDriveFolderId
    String oneDriveSiteId
    OneDriveUserSettings oneDriveUserSettings
    List<ReportFormatEnum> oneDriveFormats = []

    static mapWith = "none"

    // @TODO emailToUsers can be stored as a JSON array, there is no real need to save them in the DB
    static hasMany = [sharedWith: User, sharedWithGroup: UserGroup, emailToUsers: String, attachmentFormats: ReportFormatEnum, oneDriveFormats: ReportFormatEnum]

    @SuppressWarnings("GroovyAssignabilityCheck")
    static constraints = {
        additionalAttachments(nullable: true, maxSize: 32000)
        oneDriveUserSettings(nullable: true)
        oneDriveFolderName(nullable: true)
        oneDriveFolderId(nullable: true)
        oneDriveSiteId(nullable: true)
        oneDriveFormats(nullable: true)
        attachmentFormats(nullable: true, validator: {val, obj ->
            if (!(obj.sharedWith || obj.sharedWithGroup || obj.emailToUsers || val)) {
                return "com.rxlogix.config.Configuration.deliveryOptionRequired"
            } else {
                if(!obj.emailToUsers && (val || obj.additionalAttachments)) {
                    return "com.rxlogix.config.emailToUsers.required"
                }
            }
        })
    }

    boolean isSharedWith(User currentUser) {
        if (sharedWith?.any { it.id == currentUser.id }) {
            return true
        }
        List<UserGroup> userGroups = UserGroup.fetchAllUserGroupByUser(currentUser).flatten()
        return sharedWithGroup?.any { it.id in userGroups*.id }
    }

    public String toString() {
        return "${emailToUsers ? emailToUsers.join(",") : sharedWith ? sharedWith.join(",") : sharedWithGroup.join(",")} ${attachmentFormats ? ' - ' + attachmentFormats.join(",") : ''}"
    }
}
