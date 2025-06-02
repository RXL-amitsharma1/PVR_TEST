package com.rxlogix.config

import com.rxlogix.RxCodec
import com.rxlogix.util.DbUtil
import grails.util.Holders

class XMLResultData {

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
    boolean isAttachmentExist = false
    byte[] attachmentData //merged xml attachment files and stored here in pdf format

    static transients = ['encryptedValue', 'decryptedValue']

    static constraints = {
        value nullable: true, maxSize: (1* 1024 * 1024 * 1024)      // 1GB
        isEncrypted(nullable: false)
        executedTemplateQueryId unique: ['caseNumber', 'versionNumber']
        executedON(nullable: true)
        isAttachmentExist(nullable: false)
        attachmentData nullable: true, maxSize: (1* 1024 * 1024 * 1024) //1 GB
    }

    static mapping = {
        table name: "XML_RESULT_DATA"
        value column: "VALUE", sqlType: DbUtil.longBlobType
        valueBytesLength formula: "dbms_lob.getlength(value)"
        isEncrypted column: 'IS_ENCRYPTED'
        totalTime column: "TOTAL_TIME"
        executedTemplateQueryId column: "EX_TEMPLT_QUERY_ID"
        caseNumber column: "CASE_NUM"
        versionNumber column: "VERSION_NUM"
        executedON column: "EXECUTED_ON"
        isAttachmentExist column: "IS_ATTACHMENT_EXIST"
        attachmentData column: "ATTACHMENT_DATA"
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
        return caseNumber
    }
}
