package com.rxlogix

import com.rxlogix.config.Tenant
import com.rxlogix.user.AIEmailPreference
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import com.rxlogix.util.FileUtil
import com.rxlogix.util.MiscUtil
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.RandomStringUtils
import spock.lang.Specification

class ReportResultServiceSpec extends Specification implements DataTest, ServiceUnitTest<ReportResultService> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains AIEmailPreference, Preference, User, Tenant
    }

    def "test of adding entries to tar" () {
        given:
        File tempDirectory = new File(grailsApplication.config.tempDirectory)
        tempDirectory.mkdirs()
        File directoryToArchive = new File(tempDirectory, "${MiscUtil.generateRandomName()}")
        if (directoryToArchive.exists()) {
            FileUtils.deleteDirectory(directoryToArchive)
        }
        directoryToArchive.mkdir()
        String charset = (('A'..'Z') + ('0'..'9')).join()
        Integer length = 255

        when:
        long startTime = System.currentTimeMillis()

        for (int i = 0; i < 10_000; i++) {
            String caseNumber = "CS${i}"
            File caseNumberDir = new File(directoryToArchive, caseNumber)
            for (int j = 0; j < 10; j++) {
                String randomString = RandomStringUtils.random(length, charset.toCharArray())
                service.appendEntryToDir(caseNumberDir, "${j}.txt", randomString.bytes)
            }

        }
        directoryToArchive.eachDir {
            File caseTarFile = new File(directoryToArchive, "${it.name}.tar.gz")
            FileUtil.compressFiles(it.listFiles().toList(), caseTarFile)
            FileUtils.deleteDirectory(it)
        }
        // Compress the entire directory of all the executed templates into one archive
        File tarFile = new File(grailsApplication.config.tempDirectory, "${MiscUtil.generateRandomName()}.tar.gz")
        FileUtil.compressFiles(directoryToArchive.listFiles().toList().sort(), tarFile)
        FileUtils.cleanDirectory(directoryToArchive)
        directoryToArchive.delete()

        long endTime = System.currentTimeMillis()
        long executionTime = endTime - startTime

        then:
        tarFile.size() > 0
        // less than 10 minutes
        executionTime < 10 * 60 * 1000
    }

}
