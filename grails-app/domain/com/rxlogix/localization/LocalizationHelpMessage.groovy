package com.rxlogix.localization

import com.rxlogix.util.DbUtil

class LocalizationHelpMessage {

    String message

    static belongsTo = [localization: Localization]


    static mapping = {
        table name: "LOCALIZATION_HELP"
        message column: "MESSAGE", sqlType: DbUtil.longStringType
        localization column: "LOCALIZATION_ID"
    }

}
