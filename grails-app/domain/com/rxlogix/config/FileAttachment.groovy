package com.rxlogix.config

import com.rxlogix.util.DbUtil

class FileAttachment {

    byte[] data

    static mapping = {
        version false
        data sqlType: DbUtil.longBlobType
    }

    static constraints = {
        data(nullable: true, maxSize: 20971520)
    }
    String toString() {
        return "FileAttachment id=" + id
    }
}
