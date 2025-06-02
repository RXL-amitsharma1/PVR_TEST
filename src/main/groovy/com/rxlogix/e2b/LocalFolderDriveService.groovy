package com.rxlogix.e2b

import groovy.io.FileType
import groovy.util.logging.Slf4j

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@Slf4j
class LocalFolderDriveService implements IcsrDriveService {

    static String STATIC_ACK_FILE = "CASE_ACK_FILE.xml"


    void upload(String folderPath, File file) {
        log.debug("Uploading file on local drive on Path ${folderPath}")
        Files.copy(Paths.get(file.absolutePath), Paths.get(folderPath + '/' + file.name), StandardCopyOption.REPLACE_EXISTING);
        log.debug("Uploaded file on local drive on Path ${folderPath}")
    }

    void checkIfAckReceived(String incomingFolder) {
        if (!incomingFolder) {
            return
        }
        log.debug("Checking ACK Files")

        new File(incomingFolder).eachFileRecurse(FileType.FILES) { file ->
            if (file.name == STATIC_ACK_FILE) {
                log.debug("Mark case as acknowledged")
                file.delete()
            }
        }
    }
}