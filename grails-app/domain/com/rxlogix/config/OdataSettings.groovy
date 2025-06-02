package com.rxlogix.config

import com.rxlogix.RxCodec
import com.rxlogix.util.DbUtil

class OdataSettings {

    String dsName
    String dsLogin
    String dsPassword
    String dsUrl
    String settings
    Boolean isDeleted = false

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy


    static constraints = {
        dsName(validator: { val, obj ->
            if (!obj.id || obj.isDirty("dsName")) {
                long count = OdataSettings.createCriteria().count {
                    ilike('dsName', "${val}")
                    eq('isDeleted', false)
                    if (obj.id) {
                        ne('id', obj.id)
                    }
                }
                if (count) {
                    return "com.rxlogix.config.OdataSettings.dsName.unique"
                }
            }
        })
        settings nullable: true
    }

    static mapping = {
        table name: "ODATA_SETTINGS"
        settings column: "SETTINGS", sqlType: DbUtil.longStringType
        dsName column: "DS_NAME"
        dsLogin column: "DS_LOGIN"
        dsPassword column: "DS_PASSWORD"
        dsUrl column: "DS_URL"
        isDeleted column: "IS_DELETED"
    }

    void setPasswordEncoded(String pass) {
        dsPassword = RxCodec.encode(pass)
    }

    String getPasswordDecoded() {
        return RxCodec.decode(dsPassword)
    }

}
