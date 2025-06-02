package com.rxlogix.config

import com.rxlogix.user.UserGroup
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
abstract class BasicPublisherSource {
    String name
    byte[] data
    int sortNumber
    UserGroup userGroup
    String ext
    String path
    String script
    Source fileSource
    FileType fileType
    String oneDriveFolderName
    String oneDriveFolderId
    String oneDriveSiteId
    OneDriveUserSettings oneDriveUserSettings
    //todo:fix
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy = "-"
    String modifiedBy = "-"

    static mapWith = "none"

    static mapping = {
        tablePerHierarchy false

        name column: "NAME"
        data column: "DATA", lazy: true
        path column: "PATH"
        script column: "SCRIPT"
        fileSource column: "SOURCE"
        fileType column: "TYPE"
        oneDriveFolderName column: "OD_FOLDER_NAME"
        oneDriveFolderId column: "OD_FOLDER_ID"
        oneDriveSiteId column: "OD_SITE_ID"
        oneDriveUserSettings column: "OD_SETTINGS_ID"
    }

    static constraints = {
        oneDriveFolderName(nullable: true)
        oneDriveFolderId(nullable: true)
        oneDriveSiteId(nullable: true)
        oneDriveUserSettings(nullable: true)
        ext(nullable: true)
        path(nullable: true, maxSize: 4000)
        script(nullable: true)
        fileSource nullable: true, validator: { val, obj ->
            if ((obj.fileSource == Source.FILE) && !obj.data)
                return "com.rxlogix.config.BasicConfigurationAttachment.nofile"
            if ((obj.fileSource == Source.ONEDRIVE) && (!obj.oneDriveFolderName || !obj.oneDriveSiteId || !obj.oneDriveFolderId || !obj.oneDriveUserSettings))
                return "com.rxlogix.config.BasicConfigurationAttachment.noonedrive"
        }
        fileType(nullable: true)
        userGroup(nullable: true)
        name(blank: false, maxSize: 400)
        data(nullable: true, maxSize: 20971520)
    }

    static beforeInsert = {
        lastUpdated = new Date() // required to update lastUpdated field when attachment is created.
    }

    static beforeUpdate = {
        lastUpdated = new Date() // required to update lastUpdated field when attachment is updated.
    }

    static enum FileType {
        WORD,
        EXCEL,
        PDF,
        IMAGE,
        XML,
        JSON

        public getI18nKey() {
            return "app.PublisherTemplate.ConfigurationAttachment.Type.${this.name()}"
        }
    }

    static enum Source {
        FILE,
        HTTP,
        HTTPS,
        //      FTP,
        FOLDER,
        ONEDRIVE,
        SERVICE

        public getI18nKey() {
            return "app.PublisherTemplate.ConfigurationAttachment.Source.${this.name()}"
        }
    }

    Map toMap() {
        return [id                : this.id,
                name              : this.name,
                data              : this.data ? this.data?.length + " B" : null,
                sortNumber        : this.sortNumber,
                userGroup         : this.userGroup?.name,
                ext               : this.ext,
                path              : this.path,
                script            : this.script,
                fileSource        : this.fileSource.name(),
                fileType          : this.fileType.name(),
                oneDriveFolderName: this.oneDriveFolderName,
                oneDriveFolderId  : this.oneDriveFolderId,
                oneDriveSiteId    : this.oneDriveSiteId,
        ]
    }

    String toString() {
        return name
    }
}
