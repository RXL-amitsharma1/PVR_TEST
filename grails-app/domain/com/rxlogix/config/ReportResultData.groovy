package com.rxlogix.config

import com.rxlogix.RxCodec
import com.rxlogix.util.DbUtil
import grails.util.Holders

import java.util.zip.GZIPInputStream

class ReportResultData {
    String crossTabHeader
    byte[] value
    String reportSQL
    String querySQL
    String versionSQL
    String gttSQL
    String headerSQL
    Long valueBytesLength
    boolean isEncrypted = false

    static belongsTo = [reportResult: ReportResult]
    static transients = ['encryptedValue', 'decryptedValue']


    static constraints = {
        value(nullable: true, maxSize: (1 * 1024 * 1024 * 1024))      // 1GB
        crossTabHeader(maxSize: 6 * 1024 * 1024, nullable: true)     // 6M
        reportSQL(maxSize: 6 * 1024 * 1024, nullable: true)          // 6M
        querySQL(maxSize: 6 * 1024 * 1024, nullable: true)          // 6M
        versionSQL(maxSize: 6 * 1024 * 1024, nullable: true)        // 6M
        gttSQL(maxSize: 6 * 1024 * 1024, nullable: true)            // 6M
        headerSQL(maxSize: 6 * 1024 * 1024, nullable: true)         // 6M
        headerSQL(maxSize: 6 * 1024 * 1024, nullable: true)         // 6M
        isEncrypted(nullable: false)
    }

    static mapping = {
        table name: "RPT_RESULT_DATA"
        crossTabHeader column: "CROSS_TAB_SQL", sqlType: DbUtil.longStringType
        value column: "VALUE", sqlType: DbUtil.longBlobType
        reportSQL column: "REPORT_SQL", sqlType: DbUtil.longStringType
        querySQL column: "QUERY_SQL", sqlType: DbUtil.longStringType
        versionSQL column: "VERSION_SQL", sqlType: DbUtil.longStringType
        gttSQL column: "GTT_SQL", sqlType: DbUtil.longStringType
        headerSQL column: "HEADER_SQL", sqlType: DbUtil.longStringType
        valueBytesLength formula: "dbms_lob.getlength(value)"
        isEncrypted column: 'IS_ENCRYPTED'
    }

    public boolean isGzippedValue() {
        return value[0] == (byte) GZIPInputStream.GZIP_MAGIC && value[1] == (byte) (GZIPInputStream.GZIP_MAGIC >>> 8)
    }

    void setEncryptedValue(byte[] value) {
        if (!Holders.config.getProperty('pvreports.encrypt.data', Boolean)) {
            this.isEncrypted = false
            this.setValue(value)
            return
        }
        this.isEncrypted = true
        this.setValue(RxCodec.encodeBytes(value))
    }

    byte[] getDecryptedValue() {
        if (!this.isEncrypted) {
            return this.value
        }
        return RxCodec.decodeBytes(this.value)
    }

    String toString() {
        return "Report Result Data Size:" + value?.size()?:0
    }
}
