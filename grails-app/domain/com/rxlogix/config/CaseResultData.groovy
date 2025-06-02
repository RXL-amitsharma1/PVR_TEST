package com.rxlogix.config

import com.rxlogix.RxCodec
import com.rxlogix.util.DbUtil
import grails.util.Holders

class CaseResultData {

    String caseNumber
    Long versionNumber
    byte[] value
    Long executedTemplateQueryId
    Long valueBytesLength
    boolean isEncrypted = false
    Long totalTime = 0L
    Date dateCreated
    Date lastUpdated
    String executedON



    static transients = ['encryptedValue', 'decryptedValue']


    static constraints = {
        value(nullable: true, maxSize: (1 * 1024 * 1024 * 1024))      // 1GB
        isEncrypted(nullable: false)
        executedTemplateQueryId unique: ['caseNumber', 'versionNumber']
        executedON (nullable: true)
    }

    static mapping = {
        table name: "CASE_RESULT_DATA"
        value column: "VALUE", sqlType: DbUtil.longBlobType
        valueBytesLength formula: "dbms_lob.getlength(value)"
        isEncrypted column: 'IS_ENCRYPTED'
        totalTime column: "TOTAL_TIME"
        executedTemplateQueryId column: "EX_TEMPLT_QUERY_ID"
        caseNumber column: "CASE_NUM"
        versionNumber column: "VERSION_NUM"
        executedON column: "EXECUTED_ON"
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

    String toString(){
        return caseNumber
    }
}
