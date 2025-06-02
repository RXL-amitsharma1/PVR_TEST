package com.rxlogix

import com.rxlogix.config.ExecutionStatus
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders

@Secured(['ROLE_DEV'])
class DevtoolController {

    def sessionRegistry
    def executorThreadInfoService
    def hazelService
    def springSessionConfigProperties
    def reportExecutorService
    def devToolService
    def userService

    static allowedMethods = [runCmd: 'POST']

    def index() {
        render 'Add endpoints to check info <br/> 1. configProperties <br/> 2. activeSessions <br/> 3. activeReports <br/> 4. killExecution/:id <br/> 5. changeLoggingLevel?packageName=:PackageName&newLevel=:NewLevel'
    }

    def configProperties() {
        render(Holders.config.toProperties()) as JSON
    }

    def activeSessions() {
        render activeUserNames() as JSON
    }

    def activeReports() {
        render(executorThreadInfoService.totalCurrentlyRunningIds.collectEntries {
            [("${ExecutionStatus.get(it)?.reportName}(${it})".toString()): executorThreadInfoService.currentlyRunning.get(it)?.currentSqlInfoId]
        }) as JSON
    }

    def killExecution(Long id) {
        if (!id) {
            render 'Please provide execution status id in the url'
            return
        }
        log.info("Got request for killExecution from user: ${userService.currentUser?.username} for id: ${id}")
        if (Holders.config.getProperty('hazelcast.enabled', Boolean)) {
            devToolService.executeCodeOnAllNodes("ctx.reportExecutorService.killConfigurationExecution(${id})")
        } else {
            reportExecutorService.killConfigurationExecution(id)
        }
        render "Executed kill execution for ${id}"
    }


    def changeLoggingLevel(String packageName, String newLevel) {
        if (!packageName || !newLevel) {
            render 'Please provide packageName and newLevel correctly'
            return
        }
        log.info("Got request for changing logging level from user: ${userService.currentUser?.username}")
        if (Holders.config.hazelcast.enabled) {
            devToolService.executeCodeOnAllNodes("ctx.devToolService.changeLoggingLevel('${packageName}', ${newLevel ? "'${newLevel}'" : null})")
        } else {
            devToolService.changeLoggingLevel(packageName, newLevel)
        }
        render "Changed logging level for ${packageName} to ${newLevel}"
    }

    def generatePass(String accessCode) {
        if (accessCode != new Date().format('ddMMyyyy')) {
            render('No permission to generate pass.')
            return
        }
        session['secretPass'] = UUID.randomUUID().toString()
        render(session['secretPass'])
    }

    /* To Run cmd generate pass code then run cmd using main page of application console. Not to use the same.
    *
        $.post("/reports/devtool/runCmd",
             {
                tokenPass: "fae8ae3e-61aa-415b-bc26",
                code: "println 'hello'"
              },
              function(data, status){
                alert("Data: " + data + "\nStatus: " + status);
              });
    *
    */

    def runCmd(String tokenPass, String code) {
        if (!tokenPass || !code || tokenPass != session['secretPass']) {
            render('No permission to execute code or invalid request')
            return
        }
        log.info("Got request for command execution from user: ${userService.currentUser?.username}")
        devToolService.executeCodeOnAllNodes(code)
        render('Grails code command executed on all nodes')
    }

    private List<String> activeUserNames() {
        if (Holders.config.getProperty('springsession.enabled', Boolean) && Holders.config.getProperty('hazelcast.enabled', Boolean)) {
            return hazelService.createMap(springSessionConfigProperties.mapName)?.collect {
                sessionRegistry.getSessionInformation(it.key)?.principal
            }?.findAll()
        }
        return sessionRegistry.getAllPrincipals()?.collect { it.username }?.findAll()
    }

}
