package com.rxlogix.liquibase

import grails.core.GrailsApplication
import groovy.util.logging.Slf4j
import liquibase.Liquibase
import liquibase.database.Database

@Slf4j
class MigrationCallbacks {

    GrailsApplication grailsApplication

    void beforeStartMigration(Database database) {
        log.debug("##### Getting Ready for running Migration Scripts #####")

    }

    void onStartMigration(Database database, Liquibase liquibase, String changelogName) {
        if (grailsApplication.config.liquibase.clearLockAtStart) {
            log.debug("Releasing lock on migration tables forcefully")
            liquibase.forceReleaseLocks()
        }
        if (grailsApplication.config.liquibase.clearCheckSumAtStart) {
            log.debug("Clearing checksum on migration tables forcefully")
            liquibase.clearCheckSums()
        }
        log.debug("Running Migration Scripts")
    }

    void afterMigrations(Database Database) {
        log.debug("##### Completed Migration Scripts #####")
    }
}
