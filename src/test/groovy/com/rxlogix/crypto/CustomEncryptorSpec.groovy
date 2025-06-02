package com.rxlogix.crypto


import spock.lang.Specification

class CustomEncryptorSpec extends Specification {

    def "test encrypt"() {
        given: String text = "rxlogix"
        when: RxTextEncryptor customEncrytor = new RxTextEncryptor()
        String result = customEncrytor.encrypt(text)

        then: result == '9nczNincKB4+bk0wWFqVfQ=='
    }

    def "test decrypt"() {
        given: String text = "9nczNincKB4+bk0wWFqVfQ=="
        when: RxTextEncryptor customEncrytor = new RxTextEncryptor()
        String result = customEncrytor.decrypt(text)

        then: result == 'rxlogix'
    }
}