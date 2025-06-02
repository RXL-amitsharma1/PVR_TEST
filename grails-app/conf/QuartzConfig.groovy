//  Quartz Config

// Global
quartz {
    threadPool.'class' = 'org.quartz.simpl.SimpleThreadPool'
    threadPool.threadCount = 7
    threadPool.threadPriority = 5
}

// Environment specific
environments {
    test {
        quartz.autoStartup = false

    }
    development {
        quartz.autoStartup = true
        quartz {
            jdbcStore = true
            waitForJobsToCompleteOnShutdown = true
            monitor.layout = "quartzMonitor"
            exposeSchedulerInRepository = true // Allows monitoring in Java Melody

            props {
                scheduler.skipUpdateCheck = true
                scheduler.instanceName = 'PVR_INSTANCE_SCHEDULER'
                scheduler.instanceId = 'AUTO'
                scheduler.misfirePolicy =  'doNothing'
                scheduler.idleWaitTime = 1000

                jobStore.misfireThreshold = 5000
//                jobStore.'class' = 'org.quartz.impl.jdbcjobstore.JobStoreTX'
                jobStore.driverDelegateClass = 'org.quartz.impl.jdbcjobstore.StdJDBCDelegate'
                jobStore.useProperties = false
                jobStore.tablePrefix = 'QRTZ_'
                jobStore.isClustered = true
                jobStore.clusterCheckinInterval = 3000

                plugin.shutdownhook.'class' = 'org.quartz.plugins.management.ShutdownHookPlugin'
                plugin.shutdownhook.cleanShutdown = true
            }
        }
    }
    production {
        quartz.autoStartup = true
        quartz {
            jdbcStore = true
            waitForJobsToCompleteOnShutdown = true
            monitor.layout = "quartzMonitor"
            exposeSchedulerInRepository = true // Allows monitoring in Java Melody

            props {
                scheduler.skipUpdateCheck = true
                scheduler.instanceName = 'PVR_INSTANCE_SCHEDULER'
                scheduler.instanceId = 'AUTO'
                scheduler.misfirePolicy =  'doNothing' // https://stackoverflow.com/questions/31423003/quartz-error-misfire-handling-and-failure-on-job-recovery
                scheduler.idleWaitTime = 1000

                jobStore.misfireThreshold = 5000 //https://stackoverflow.com/questions/32075128/avoiding-misfires-with-quartz
                jobStore.'class' = 'org.springframework.scheduling.quartz.LocalDataSourceJobStore'
                jobStore.driverDelegateClass = 'org.quartz.impl.jdbcjobstore.StdJDBCDelegate'
                jobStore.useProperties = false
                jobStore.tablePrefix = 'QRTZ_'
                jobStore.isClustered = true
                jobStore.clusterCheckinInterval = 3000

                plugin.shutdownhook.'class' = 'org.quartz.plugins.management.ShutdownHookPlugin'
                plugin.shutdownhook.cleanShutdown = true
            }
        }
    }
}

