package reports

import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
class FileServiceSpec extends Specification {

    def grailsApplication
    def fileService

    def setup() {}

    def cleanup() {}

    void "Files older than 30 days should be deleted, and files modified within the last 30 days should be kept."() {
        given: "Files to delete"
        String directory = grailsApplication.config.tempDirectory

        File toDelete = new File(directory + "toDelete")
        toDelete.delete()
        assert toDelete.createNewFile()
        toDelete.setLastModified(0)

        File toKeep = new File(directory + "toKeep")
        toKeep.delete()
        assert toKeep.createNewFile()


        when: "Deletion job is run"
        fileService.deleteOldTempFiles()

        then: "Files were correctly deleted"
        assert !toDelete.exists()
        assert toKeep.exists()
        toKeep.delete()
    }
}
