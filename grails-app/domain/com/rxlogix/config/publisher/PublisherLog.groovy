package com.rxlogix.config.publisher

import com.rxlogix.util.DbUtil

class PublisherLog {
    String executionLogJson

    static belongsTo = [publisherExecutedTemplate: PublisherExecutedTemplate]

    static mapping = {
        executionLogJson(column: "LOG", sqlType: DbUtil.longStringType)
        publisherExecutedTemplate column: "publisher_ex_tpl_id"

    }
    static constraints = {
        executionLogJson(nullable: true, maxSize: 1000000)
    }
}
