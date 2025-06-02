package com.rxlogix

import grails.core.GrailsApplication
import grails.gorm.transactions.ReadOnly
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOCase
import org.apache.commons.io.filefilter.AndFileFilter
import org.apache.commons.io.filefilter.FileFileFilter
import org.apache.commons.io.filefilter.NotFileFilter
import org.apache.commons.io.filefilter.SuffixFileFilter

@ReadOnly
class FileService {
    GrailsApplication grailsApplication

    /**
     * Deletes all files in the temp directory older than 30 days.
     */
    void deleteOldTempFiles(Long time = 2592000000) {
        String directory = grailsApplication.config.tempDirectory
        File directoryFile = new File(directory)
        Iterator iterator = FileUtils.iterateFiles(directoryFile, new AndFileFilter([FileFileFilter.FILE, new NotFileFilter(new SuffixFileFilter(['.xml', '.r3xml'] as String[], IOCase.INSENSITIVE))]), FileFileFilter.FILE)
        long now = Calendar.instance.timeInMillis
        // 30 days in milliseconds = 2592000000
        long nowMinus30Days = now - time
        while (iterator.hasNext()) {
            File current = iterator.next()
            if (nowMinus30Days > current.lastModified()) {
                String currentFile = current.absolutePath
                boolean deleteResult = current.delete()
                log.debug("Deleted ${currentFile}: ${deleteResult}")
            }
        }
    }
}
