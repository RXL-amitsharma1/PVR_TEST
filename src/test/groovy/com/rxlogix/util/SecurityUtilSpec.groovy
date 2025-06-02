package com.rxlogix.util

import groovy.json.JsonSlurper

import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset


class SecurityUtilSpec extends Specification {
    def "encryption"() {
        expect:
        def encryptedText = SecurityUtil.encrypt("rxlogix", "admin")
        encryptedText != null
    }


    def "decryption"() {
        expect:
        def decryptedText = SecurityUtil.decrypt("rxlogix", "+fZYHmUmcVE=")
        decryptedText == 'admin'
    }

    def "generateAPIToken"() {
        setup:
        expect:
        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.of(2018, 05, 03), LocalTime.of(10, 10, 10, 10))
        def token = SecurityUtil.generateAPIToken("rxlogix", "test", 'test', Date.from(localDateTime.toInstant(ZoneOffset.MAX)))

        token != null
    }

    def "decodeAPIToken"() {
        expect:
        def tokenStr = SecurityUtil.decodeAPIToken("rxlogix",
                """zn9MrreyDiDy/I1sITyAcWt2GW5tcaa3CK7udd20/U5VWm1cINKB41OV/P+RgLx+b8rd5dF6yigXuTC18cdXXpHpYjzbn5VR""")
        def jsp =  new JsonSlurper()
        def token  = jsp.parseText(tokenStr)

        token.id == "test"
    }
}
