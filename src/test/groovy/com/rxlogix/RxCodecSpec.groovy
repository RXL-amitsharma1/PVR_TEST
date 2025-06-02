package com.rxlogix

import spock.lang.Specification

class RxCodecSpec extends Specification {

    def "Test for encode"() {
        expect:
        RxCodec.encode(text) == ((System.getProperty('encryption.key.size') == '256') ? result256 : result)
        where:
        text         || result                     | result256
        "Hello Test" || "UuZjKMM9GUs8Kz5385DT6g==" | 'exYYGhPhGmahAVs3DMAtoQ=='
        "rxlogix"    || "9nczNincKB4+bk0wWFqVfQ==" | 'Bcm00gmFZx7//EdefDVQQA=='
        ""           || "AuPzBzDBb1dDKltmyyc2TQ==" | 'E49hfuFdKvQVy8DGMQr/KQ=='
        '$abx'       || "gtpybeYvz5jQgvrpfP4aiw==" | '8IJcIMclyVt/PcKvYlWmSA=='
        '#abx#'      || "Dgb5ipHzbGhKsIG5TzIVGg==" | 'OhPWdm+QvrUOZ9im8wESHQ=='
    }

    def "Test for decode"() {
        expect:
        RxCodec.decode((System.getProperty('encryption.key.size') == '256') ? encoded256 : encoded) == result
        where:
        encoded256                 | encoded                    || result
        'exYYGhPhGmahAVs3DMAtoQ==' | "UuZjKMM9GUs8Kz5385DT6g==" || 'Hello Test'
        'Bcm00gmFZx7//EdefDVQQA==' | "9nczNincKB4+bk0wWFqVfQ==" || 'rxlogix'
        'E49hfuFdKvQVy8DGMQr/KQ==' | "AuPzBzDBb1dDKltmyyc2TQ==" || ''
        '8IJcIMclyVt/PcKvYlWmSA==' | "gtpybeYvz5jQgvrpfP4aiw==" || '$abx'
        'OhPWdm+QvrUOZ9im8wESHQ==' | "Dgb5ipHzbGhKsIG5TzIVGg==" || '#abx#'
    }

}
