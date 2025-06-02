package com.rxlogix

import com.rxlogix.config.ApplicationSettings
import grails.gorm.transactions.ReadOnly

import java.util.concurrent.ConcurrentHashMap
import com.rxlogix.Constants

@ReadOnly
class ExecutorThreadInfoService {

    def hazelService
    def grailsApplication
    def utilService

    Map<Long, ExecutorThreadInfoDTO> currentlyRunning = new ConcurrentHashMap<Long, ExecutorThreadInfoDTO>([:])

    public static String CURRENTLY_RUNNING_EX_IDS = "currentlyRunningExIds"

    Map<Long, ExecutorThreadInfoDTO> currentlyRunningIcsr = new ConcurrentHashMap<Long, ExecutorThreadInfoDTO>([:])

    public static String CURRENTLY_RUNNING_EX_ICSR_IDS = "currentlyRunningExIcsrIds"

    Map<BigDecimal, ExecutorThreadInfoDTO> currentlyGeneratingCases = new ConcurrentHashMap<BigDecimal, ExecutorThreadInfoDTO>([:])

    public static String CURRENTLY_GENERATING_CASES = "currentlyGeneratingCases"

    Map<String, ExecutorThreadInfoDTO> currentlyTransmittingFiles = new ConcurrentHashMap<String, ExecutorThreadInfoDTO>([:])

    public static String CURRENTLY_TRANSMITTING_FILES = "currentlyTransmittingFiles"

    Map<String, Boolean> currentlyPriorityRunStatus = new ConcurrentHashMap<String, Boolean>([:])

    public static String CURRENTLY_PRIORITY_RUN_STATUS = "currentlyPriorityRunStatus"

    Map<BigDecimal, ExecutorThreadInfoDTO> currentlyGeneratingInbounds = new ConcurrentHashMap<BigDecimal, ExecutorThreadInfoDTO>([:])

    public static String CURRENTLY_GENERATING_INBOUNDS = "currentlyGeneratingInbounds"

    public static String PVADMIN_AUTH_TOKEN="pvAdminAuthToken"

    Map<String, String> adminTempTokenMap = new ConcurrentHashMap<>();

    List getTotalCurrentlyRunningIds() {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_RUNNING_EX_IDS)?.keySet()?.toList()
        } else {
            currentlyRunning.keySet().toList()
        }
    }

    List getTotalCurrentlyRunningIcsrIds() {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_RUNNING_EX_ICSR_IDS)?.keySet()?.toList()
        } else {
            currentlyRunningIcsr.keySet().toList()
        }
    }

    void addToTotalCurrentlyRunningIds(Long id, String priorityType) {
        if (hazelService.isEnabled()) {
            if(priorityType.equals(Constants.ICSR_PROFILE)) {
                hazelService.createMap(CURRENTLY_RUNNING_EX_ICSR_IDS)?.put(id, "${utilService.hostIdentifier}-"+new Date()+ "-" +priorityType)
            }else {
                hazelService.createMap(CURRENTLY_RUNNING_EX_IDS)?.put(id, "${utilService.hostIdentifier}-"+new Date()+ "-" +priorityType)
            }
        }
        if(priorityType.equals(Constants.ICSR_PROFILE)) {
            currentlyRunningIcsr.put(id, new ExecutorThreadInfoDTO(threadObj: Thread.currentThread(), priorityType: priorityType))
        }else {
            currentlyRunning.put(id, new ExecutorThreadInfoDTO(threadObj: Thread.currentThread(), priorityType: priorityType))
        }
    }

    void removeFromTotalCurrentlyRunningIds(Long id) {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_RUNNING_EX_IDS)?.remove(id)
            hazelService.createMap(CURRENTLY_RUNNING_EX_ICSR_IDS)?.remove(id)
        }
        currentlyRunning?.remove(id)
        currentlyRunningIcsr?.remove(id)
    }

    int getExecutionQueueSize(String priorityType) {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_RUNNING_EX_IDS).findAll { it.value.substring(it.value.lastIndexOf('-') + 1) == priorityType }?.keySet().size()
        }
        currentlyRunning.findAll { it.value.priorityType == priorityType }?.keySet().size()
    }

    List<BigDecimal> getTotalCurrentlyGeneratingCases() {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_GENERATING_CASES)?.keySet()?.toList()
        } else {
            currentlyGeneratingCases.keySet().toList()
        }
    }

    void addToTotalCurrentlyGeneratingCases(BigDecimal id) {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_GENERATING_CASES)?.put(id, "${utilService.hostIdentifier}-" + new Date())
        }
        currentlyGeneratingCases.put(id, new ExecutorThreadInfoDTO(threadObj: Thread.currentThread()))
    }

    void removeFromTotalCurrentlyGeneratingCases(BigDecimal id) {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_GENERATING_CASES)?.remove(id)
        }
        currentlyGeneratingCases.remove(id)
    }

    int getCasesGenerationQueueSize() {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_GENERATING_CASES).size()
        } else {
            currentlyGeneratingCases.size()
        }
    }

    int availableSlotsForCasesGeneration() {
        def threadPoolSize = (grailsApplication.config.icsr.cases.executor.size ?: 5)
        int totalNodes = grailsApplication.config.hazelcast.network.nodes?.size() ?: 1
        int slots = ((threadPoolSize / totalNodes) - currentlyGeneratingCases.size())
        return (slots > 0 ? slots : 0)
    }

    //Currently Transmitting Cases
    List<String> getTotalCurrentlyTransmittingFiles() {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_TRANSMITTING_FILES)?.keySet()?.toList()
        } else {
            currentlyTransmittingFiles.keySet().toList()
        }
    }

    void addToTotalCurrentlyTransmittingFiles(String id) {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_TRANSMITTING_FILES)?.put(id, "${utilService.hostIdentifier}-" + new Date())
        }
        currentlyTransmittingFiles.put(id, new ExecutorThreadInfoDTO(threadObj: Thread.currentThread()))
    }

    void addTempTokenForAdmin(String tempToken,String originalToken ){
        if (hazelService.isEnabled()) {
            hazelService.createMap(PVADMIN_AUTH_TOKEN).put(tempToken,originalToken)
        }
        else{
            adminTempTokenMap.put(tempToken, originalToken)
        }
    }

    String getTempTokenForAdmin(String tempToken) {
        if (hazelService.isEnabled()) {
            return hazelService.createMap(PVADMIN_AUTH_TOKEN).remove(tempToken);
        }
        return adminTempTokenMap.remove(tempToken);
    }

    void removeFromTotalCurrentlyTransmittingFiles(String id) {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_TRANSMITTING_FILES)?.remove(id)
        }
        currentlyTransmittingFiles.remove(id)
    }

    int getFileTransmittingQueueSize() {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_TRANSMITTING_FILES).size()
        } else {
            currentlyTransmittingFiles.size()
        }
    }

    int availableSlotsForTransmittingFiles() {
        def threadPoolSize = (grailsApplication.config.icsr.transmitting.file.executor.size ?: 2)
        int totalNodes = grailsApplication.config.hazelcast.network.nodes?.size() ?: 1
        int slots = ((threadPoolSize / totalNodes) - currentlyTransmittingFiles.size())
        return (slots > 0 ? slots : 0)
    }

    //The Below Method is used to add current status of Priority Report Run is checked or not in Job Monitoring Page
    void addStatusOfRunPriorityOnly(Boolean runPriorityOnly) {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_PRIORITY_RUN_STATUS)?.put("RUN_PRIORITY_ONLY", runPriorityOnly)
        }
        currentlyPriorityRunStatus.put("RUN_PRIORITY_ONLY", runPriorityOnly)
    }

    //The Below Method is used to get current status of Priority Report Run is checked or not in Job Monitoring Page
    Boolean getStatusOfRunPriorityOnly() {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_PRIORITY_RUN_STATUS)?.get("RUN_PRIORITY_ONLY")
        } else {
            currentlyPriorityRunStatus.get("RUN_PRIORITY_ONLY") ?: ApplicationSettings.first().runPriorityOnly
        }
    }

    List<BigDecimal> getTotalCurrentlyGeneratingInbounds() {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_GENERATING_INBOUNDS)?.keySet()?.toList()
        } else {
            currentlyGeneratingInbounds.keySet().toList()
        }
    }

    void addToTotalCurrentlyGeneratingInbounds(BigDecimal id) {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_GENERATING_INBOUNDS)?.put(id, "${utilService.hostIdentifier}-" + new Date())
        }
        currentlyGeneratingInbounds.put(id, new ExecutorThreadInfoDTO(threadObj: Thread.currentThread()))
    }

    void removeFromTotalCurrentlyGeneratingInbounds(BigDecimal id) {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_GENERATING_INBOUNDS)?.remove(id)
        }
        currentlyGeneratingInbounds.remove(id)
    }

    int getInboundGenerationQueueSize() {
        if (hazelService.isEnabled()) {
            hazelService.createMap(CURRENTLY_GENERATING_INBOUNDS).size()
        } else {
            currentlyGeneratingInbounds.size()
        }
    }

    int availableSlotsForInboundsGeneration() {
        def threadPoolSize = (grailsApplication.config.inbound.compliance.executor.size ?: 5)
        int totalNodes = grailsApplication.config.hazelcast.network.nodes?.size() ?: 1
        int slots = ((threadPoolSize / totalNodes) - currentlyGeneratingInbounds.size())
        return (slots > 0 ? slots : 0)
    }

}
