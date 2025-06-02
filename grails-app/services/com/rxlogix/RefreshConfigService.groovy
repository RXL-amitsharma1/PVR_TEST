package com.rxlogix

import com.rxlogix.config.Dashboard
import com.rxlogix.config.EtlSchedule
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.config.ReportTemplate
import com.rxlogix.config.SuperQuery
import com.rxlogix.config.Tag
import grails.config.Config
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import net.sf.jasperreports.engine.DefaultJasperReportsContext


class RefreshConfigService implements AppRefreshableConfigs {
    def bootstrap
    def seedDataService
    def ldapService
    def qualityService
    def dataSource_pva
    def detailedCaseSeriesService


    /*
    Method to refresh dependencies for a configuration key
     */
    @Override
    void refreshConfigData(List<String> ConfigKeys) {
        log.debug("Refresh Config has been Started....")

        ConfigKeys.each { keys ->
            try {
                switch (keys) {
                    case "dynamicJasper.config.net.sf.jasperreports.governor.max.pages":
                    case "dynamicJasper.config.net.sf.jasperreports.governor.timeout":
                        log.debug("DYNAMIC JASPER")
                        if (Holders.config.dynamicJasper.config) {
                            Holders.config.dynamicJasper.config?.toFlatConfig()?.each {
                                DefaultJasperReportsContext.getInstance().setProperty(it.key.toString(), it.value?.toString())
                            }
                        }
                        break
                    case "pvreports.multiTenancy.defaultTenant":
                        log.debug("DEFAULT TENANT")
                        seedDataService.seedDefaultTenant()
                        seedDataService.seedUsers()
                        break
                    case "etl.schedule.admin.emails":
                        log.debug("SCHEDULE EMAILS")
                        if (!EtlSchedule.count()) {
                            seedDataService.seedETLSchedule()
                        }
                        break
                    case "pvreports.seeding.user":
                        log.debug("SEEDING USER")
                        seedDataService.getApplicationUserForSeeding()
                        break
                    case "grails.plugin.springsecurity.ldap.active":
                        log.debug("LDAP ACTIVE")
                        if (Holders.config.grails.plugin.springsecurity.ldap.active) {
                            log.debug("Seeding updated data from LDAP for existing pvr users...")
                            ldapService.mirrorLdapValues()
                        }
                        break
                    case "dynamicJasper.swapVirtualizerMaxSize":
                    case "dynamicJasper.swapFile.minGrowCount":
                        log.debug("swapVirtualizerMaxSize")
                        bootstrap.initJasperReports()
                        break

                    case "localizations.cache.size.kb":
                        log.debug("LOCALIZATION SIZE")
                        bootstrap.initLocalization()
                        break

                    case "csrfProtection.enabled":
                        log.debug("CSRF PROTECTION")
                        if (Holders.config.csrfProtection.enabled) {
                            if (!SpringSecurityUtils.configuredOrderedFilters.get(SecurityFilterPosition.LAST.order + 10)) {
                                SpringSecurityUtils.clientRegisterFilter('csrfFilter', SecurityFilterPosition.LAST.order + 10)
                            }
                        }
                        break
                    case "singleUserSession.enabled":
                        log.debug("SINGLE USER SESSION")
                        if (!Holders.config.singleUserSession.enabled) {
                            if (!SpringSecurityUtils.configuredOrderedFilters.get(SecurityFilterPosition.CONCURRENT_SESSION_FILTER.order)) {
                                SpringSecurityUtils.clientRegisterFilter('concurrentSessionFilter', SecurityFilterPosition.CONCURRENT_SESSION_FILTER)
                            }
                        }
                        break
                    case 'show.xml.option':
                        Holders.config.pv.app.settings = getPvAppSettingMap()
                        break
                    case 'pvr.report.field.localization.delete':
                        seedDataService.seedReportFieldLocalizations(null, null, null)
                        break
                    case 'pvadmin.privacy.field.profile':
                        seedDataService.seedPrivacyFieldProfile()
                        break
                }
            }
            catch (Exception exception) {
                log.error("Failed while refreshing config for the key -> ${keys}")
                exception.printStackTrace()
            }
            finally {
                return
            }
        }
        log.debug("Refresh Config has been finished....")
    }

    /*
    Method to update interdependent configurations on startup
     */
    void updateOnetimeDependentConfigurations() {

        def config = Holders.config
        config.pv.app.settings = config.pv.app.settings ?: getPvAppSettingMap()
    }

    /*
    Returns default getPvAppSettingMap value
     */
    Map getPvAppSettingMap() {
        [
                PVReports: [
                        [
                                name    : 'app.label.adhoc.report',
                                icon    : 'md md-grid',
                                link    : '#',
                                position: 2,
                                role    : 'ROLE_CONFIGURATION_VIEW, ROLE_COGNOS_VIEW',
                                children: [
                                        [
                                                name    : 'app.viewReports.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/configuration/index',
                                                position: 4,
                                                role    : 'ROLE_CONFIGURATION_VIEW',
                                        ],
                                        [
                                                name       : 'app.menu.createFromTemplate',
                                                icon       : Constants.BLANK_STRING,
                                                link       : '#',
                                                customclass: 'createAdhocFromTemplate',
                                                position   : 3,
                                                role       : 'ROLE_CONFIGURATION_CRUD',
                                        ],
                                        [
                                                name    : 'app.newReport.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/configuration/create',
                                                position: 2,
                                                role    : 'ROLE_CONFIGURATION_CRUD',
                                        ],
                                        [
                                                name    : 'app.label.generated.reports',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/report/index',
                                                position: 5,
                                                role    : 'ROLE_CONFIGURATION_VIEW',
                                        ],
                                        [
                                                name    : 'app.label.cognos.library',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/cognosReport/index',
                                                position: 6,
                                                role    : 'ROLE_COGNOS_VIEW',
                                                hide    : "${-> Holders.config?.cognosReport?.view?.enabled ? Constants.BLANK_STRING : true}"
                                        ],
                                        [
                                                name    : 'app.menu.bulkImportConfiguration',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/configuration/bulkUpdateConfig',
                                                position: 7,
                                                role    : 'ROLE_CONFIGURATION_CRUD',
                                        ],
                                        [
                                                name    : 'app.loadConfiguration.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/configuration/load',
                                                position: 1,
                                                role    : 'ROLE_SYSTEM_CONFIGURATION',
                                        ]
                                ]
                        ],
                        [
                                name    : 'app.label.aggregateReport',
                                icon    : 'md md-call-merge',
                                link    : '#',
                                position: 3,
                                role    : 'ROLE_PERIODIC_CONFIGURATION_VIEW',
                                children: [
                                        [
                                                name    : 'app.viewReports.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/periodicReport/index',
                                                position: 3,
                                                role    : 'ROLE_PERIODIC_CONFIGURATION_VIEW',
                                        ],
                                        [
                                                name       : 'app.menu.createFromTemplate',
                                                icon       : Constants.BLANK_STRING,
                                                link       : '#',
                                                customclass: 'createAggregateFromTemplate',
                                                position   : 2,
                                                role       : 'ROLE_PERIODIC_CONFIGURATION_CRUD',
                                        ],
                                        [
                                                name    : 'app.newReport.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/periodicReport/create',
                                                position: 1,
                                                role    : 'ROLE_PERIODIC_CONFIGURATION_CRUD',
                                        ],
                                        [
                                                name    : 'app.label.generated.reports',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/periodicReport/reports',
                                                position: 4,
                                                role    : 'ROLE_PERIODIC_CONFIGURATION_VIEW',
                                        ],
                                        [
                                                name    : 'app.viewReportsSubmission.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/reportSubmission/index',
                                                position: 5,
                                                role    : 'ROLE_PERIODIC_CONFIGURATION_VIEW',
                                        ],
                                        [
                                                name    : 'app.menu.bulkImportConfiguration',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/periodicReport/bulkUpdate',
                                                position: 6,
                                                role    : 'ROLE_PERIODIC_CONFIGURATION_CRUD',
                                        ]
                                ]
                        ],
                        [
                                name    : 'app.label.icsrReport',
                                icon    : 'md md-file-document-box',
                                link    : '#',
                                position: 4,
                                role    : 'ROLE_ICSR_REPORTS_VIEWER',
                                hide    : "${-> Holders.config?.show?.xml?.option ? Constants.BLANK_STRING : true}",
                                children: [
                                        [
                                                name    : 'app.viewReports.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/icsrReport/index',
                                                position: 2,
                                                role    : 'ROLE_ICSR_REPORTS_VIEWER',
                                        ],
                                        [
                                                name    : 'app.newReport.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/icsrReport/create',
                                                position: 1,
                                                role    : 'ROLE_ICSR_REPORTS_EDITOR',
                                        ],
                                        [
                                                name    : 'app.label.generated.reports',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/icsrReport/reports',
                                                position: 3,
                                                role    : 'ROLE_ICSR_REPORTS_VIEWER',
                                        ]
                                ]
                        ],
                        [
                                name    : 'app.label.new.icsr.profile',
                                icon    : 'md md-domain',
                                link    : '#',
                                position: 5,
                                role    : 'ROLE_ICSR_PROFILE_VIEWER',
                                hide    : "${-> Holders.config?.show?.xml?.option ? Constants.BLANK_STRING : true}",
                                children: [
                                        [
                                                name    : 'app.label.profile.library',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/icsrProfileConfiguration/index',
                                                position: 2,
                                                role    : 'ROLE_ICSR_PROFILE_VIEWER',
                                        ],
                                        [
                                                name    : 'app.label.new.profile',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/icsrProfileConfiguration/create',
                                                position: 1,
                                                role    : 'ROLE_ICSR_PROFILE_EDITOR',
                                        ],
                                        [
                                                name    : 'app.label.generated.profile',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/executedIcsrProfile/index',
                                                position: 3,
                                                role    : 'ROLE_ICSR_PROFILE_VIEWER',
                                        ],
                                        [
                                                name    : 'app.label.view.cases',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/icsrProfileConfiguration/viewCases',
                                                position: 4,
                                                role    : 'ROLE_ICSR_PROFILE_VIEWER',
                                        ]
                                ]
                        ],
                        [
                                name    : 'app.label.case.series',
                                icon    : 'md md-view-column',
                                link    : '#',
                                position: 6,
                                role    : 'ROLE_CASE_SERIES_VIEW',
                                children: [
                                        [
                                                name    : 'caseSeries.library.label',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/caseSeries/index',
                                                position: 2,
                                                role    : 'ROLE_CASE_SERIES_VIEW',
                                        ],
                                        [
                                                name    : 'app.label.case.new.series',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/caseSeries/create',
                                                position: 1,
                                                role    : 'ROLE_CASE_SERIES_CRUD',
                                        ],


                                        [
                                                name    : 'app.label.case.generated.series',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/executedCaseSeries/index',
                                                position: 3,
                                                role    : 'ROLE_CASE_SERIES_VIEW',
                                        ]
                                ]
                        ],
                        [
                                name    : 'app.label.templates',
                                icon    : 'md md-widgets',
                                link    : '#',
                                position: 7,
                                role    : 'ROLE_TEMPLATE_VIEW, ROLE_TEMPLATE_SET_VIEW',
                                children: [
                                        [
                                                name    : 'app.viewTemplate.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/template/index',
                                                position: 2,
                                                role    : 'ROLE_TEMPLATE_VIEW',
                                        ],
                                        [
                                                name    : 'app.newTemplate.caseLineListing.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/template/create?templateType=CASE_LINE',
                                                position: 3,
                                                role    : 'ROLE_TEMPLATE_CRUD',
                                        ],
                                        [
                                                name    : 'app.newTemplate.dataTabulation.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/template/create?templateType=DATA_TAB',
                                                position: 4,
                                                role    : 'ROLE_TEMPLATE_CRUD',
                                        ],
                                        [
                                                name    : 'app.newTemplate.customSQL.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/template/create?templateType=CUSTOM_SQL',
                                                position: 5,
                                                role    : 'ROLE_TEMPLATE_ADVANCED',
                                        ],
                                        [
                                                name    : 'app.newTemplate.nonCase.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/template/create?templateType=NON_CASE',
                                                position: 6,
                                                role    : 'ROLE_TEMPLATE_ADVANCED',
                                        ],
                                        [
                                                name    : 'app.newTemplate.xml.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/template/create?templateType=ICSR_XML',
                                                position: 7,
                                                role    : 'ROLE_TEMPLATE_ADVANCED',
                                                hide    : "${-> Holders.config?.show?.xml?.option ? Constants.BLANK_STRING : true}"
                                        ],
                                        [
                                                name    : 'app.newTemplate.templateSet.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/template/create?templateType=TEMPLATE_SET',
                                                position: 8,
                                                role    : 'ROLE_TEMPLATE_SET_CRUD',
                                        ],
                                        [
                                                name    : 'app.loadTemplate.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/template/load',
                                                position: 1,
                                                role    : 'ROLE_SYSTEM_CONFIGURATION',
                                        ]
                                ]
                        ],
                        [
                                name    : 'app.label.queries',
                                icon    : 'md md-filter-variant',
                                link    : '#',
                                position: 8,
                                role    : 'ROLE_QUERY_VIEW',
                                children: [
                                        [
                                                name    : 'app.viewQueries.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/query/index',
                                                position: 2,
                                                role    : 'ROLE_QUERY_VIEW',
                                        ],
                                        [
                                                name    : 'app.NewQuery.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/query/create',
                                                position: 3,
                                                role    : 'ROLE_QUERY_CRUD',
                                        ],
                                        [
                                                name    : 'app.loadQuery.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/query/load',
                                                position: 1,
                                                role    : 'ROLE_SYSTEM_CONFIGURATION',
                                        ]
                                ]
                        ],
                        [
                                name    : 'app.label.dataAnalysis',
                                icon    : 'md md-trending-up',
                                link    : '#',
                                position: 9,
                                role    : 'ROLE_DATA_ANALYSIS',
                                children: [
                                        [
                                                name    : 'app.viewSpotfireFiles.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/dataAnalysis/index',
                                                position: 1,
                                                role    : 'ROLE_DATA_ANALYSIS',
                                        ],
                                        [
                                                name    : 'app.newSpotfireFile.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/dataAnalysis/create',
                                                position: 2,
                                                role    : 'ROLE_DATA_ANALYSIS',
                                        ],
                                ]
                        ],
                        [
                                name    : 'app.label.tasks',
                                icon    : 'md md-clipboard-text',
                                link    : '#',
                                position: 10,
                                role    : 'ROLE_REPORT_REQUEST_VIEW,ROLE_ACTION_ITEM,ROLE_PERIODIC_CONFIGURATION_VIEW',
                                children: [
                                        [
                                                name    : 'app.label.topnav.action.items',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/actionItem/index',
                                                position: 1,
                                                role    : 'ROLE_ACTION_ITEM',
                                        ],
                                        [
                                                name    : 'app.label.topnav.report.request',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/reportRequest/index',
                                                position: 2,
                                                role    : 'ROLE_REPORT_REQUEST_VIEW',
                                        ],
                                        [
                                                name    : 'app.label.topnav.report.request.plan',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/reportRequest/plan',
                                                position: 3,
                                                role    : 'ROLE_REPORT_REQUEST_PLAN_VIEW'
                                        ],
                                        [
                                                name    : 'app.aggregate.report.calendar',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/calendar/reports',
                                                position: 4,
                                                role    : 'ROLE_PERIODIC_CONFIGURATION_VIEW'
                                        ]
                                ]
                        ],
                        [
                                name    : 'app.label.calendar.topNav.name',
                                icon    : 'md md-calendar',
                                link    : '/calendar',
                                position: 11,
                                role    : 'ROLE_CALENDAR'
                        ],

                ],
                PVQuality: [
                        [
                                name    : 'app.label.quality.observations.title',
                                icon    : 'md md-widgets',
                                link    : '#',
                                position: 2,
                                role    : 'ROLE_PVQ_VIEW',
                                children: [
                                        [
                                                name    : 'app.label.quality.observations.case.data.quality',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/quality/caseDataQuality',
                                                position: 1,
                                                role    : 'ROLE_PVQ_VIEW',
                                        ],
                                        [
                                                name    : 'app.label.quality.observations.submissions.quality',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/quality/submissionQuality',
                                                position: 2,
                                                role    : 'ROLE_PVQ_VIEW',
                                        ]
                                ]
                        ],
                        [
                                name    : 'app.label.adhoc.report',
                                icon    : 'md md-grid',
                                link    : '#',
                                position: 3,
                                role    : 'ROLE_CONFIGURATION_VIEW, ROLE_COGNOS_VIEW',
                                children: [
                                        [
                                                name    : 'app.viewReports.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/configuration/index',
                                                position: 2,
                                                role    : 'ROLE_CONFIGURATION_VIEW',
                                        ],
                                        [
                                                name       : 'app.menu.createFromTemplate',
                                                icon       : Constants.BLANK_STRING,
                                                link       : '#',
                                                customclass: 'createAdhocFromTemplate',
                                                position   : 3,
                                                role       : 'ROLE_CONFIGURATION_CRUD',
                                        ],
                                        [
                                                name    : 'app.newReport.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/configuration/create',
                                                position: 1,
                                                role    : 'ROLE_CONFIGURATION_CRUD',
                                        ],
                                        [
                                                name    : 'app.label.generated.reports',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/report/index',
                                                position: 3,
                                                role    : 'ROLE_CONFIGURATION_VIEW',
                                        ],
                                ]
                        ],
                        [
                                name    : 'app.label.templates',
                                icon    : 'md md-widgets',
                                link    : '#',
                                position: 4,
                                role    : 'ROLE_TEMPLATE_VIEW, ROLE_TEMPLATE_SET_VIEW',
                                children: [
                                        [
                                                name    : 'app.loadTemplate.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/template/load',
                                                position: 1,
                                                role    : 'ROLE_SYSTEM_CONFIGURATION',
                                        ],
                                        [
                                                name    : 'app.viewTemplate.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/template/index',
                                                position: 2,
                                                role    : 'ROLE_TEMPLATE_VIEW',
                                        ],
                                        [
                                                name    : 'app.newTemplate.caseLineListing.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/template/create?templateType=CASE_LINE',
                                                position: 3,
                                                role    : 'ROLE_TEMPLATE_CRUD',
                                        ],
                                        [
                                                name    : 'app.newTemplate.dataTabulation.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/template/create?templateType=DATA_TAB',
                                                position: 4,
                                                role    : 'ROLE_TEMPLATE_CRUD',
                                        ],
                                        [
                                                name    : 'app.newTemplate.customSQL.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/template/create?templateType=CUSTOM_SQL',
                                                position: 5,
                                                role    : 'ROLE_TEMPLATE_ADVANCED',
                                        ],
                                        [
                                                name    : 'app.newTemplate.nonCase.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/template/create?templateType=NON_CASE',
                                                position: 6,
                                                role    : 'ROLE_TEMPLATE_ADVANCED',
                                        ],
                                        [
                                                name    : 'app.newTemplate.xml.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/template/create?templateType=ICSR_XML',
                                                position: 7,
                                                role    : 'ROLE_TEMPLATE_ADVANCED',
                                                hide    : "${-> Holders.config?.show?.xml?.option ? Constants.BLANK_STRING : true}"
                                        ],

                                        [
                                                name    : 'app.newTemplate.templateSet.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/template/create?templateType=TEMPLATE_SET',
                                                position: 8,
                                                role    : 'ROLE_TEMPLATE_SET_CRUD',
                                        ]
                                ]
                        ],
                        [
                                name    : 'app.label.queries',
                                icon    : 'md md-filter-variant',
                                link    : '#',
                                position: 5,
                                role    : 'ROLE_QUERY_VIEW',
                                children: [
                                        [
                                                name    : 'app.loadQuery.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/query/load',
                                                position: 1,
                                                role    : 'ROLE_SYSTEM_CONFIGURATION',
                                        ],
                                        [
                                                name    : 'app.viewQueries.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/query/index',
                                                position: 2,
                                                role    : 'ROLE_QUERY_VIEW',
                                        ],
                                        [
                                                name    : 'app.NewQuery.menu',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/query/create',
                                                position: 3,
                                                role    : 'ROLE_QUERY_CRUD',
                                        ],
                                ]
                        ],
                        [
                                name    : 'app.label.quality.issue.management',
                                icon    : 'md md-calendar',
                                link    : '#',
                                position: 6,
                                role    : 'ROLE_PVQ_VIEW',
                                children: [
                                        [
                                                name    : 'app.actionPlan.actionPlan',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/quality/actionPlan',
                                                position: 1,
                                                role    : 'ROLE_PVQ_VIEW',
                                        ], [
                                                name    : 'app.label.quality.create.report.issue',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/issue/create',
                                                position: 2,
                                                role    : 'ROLE_PVQ_EDIT',
                                        ],
                                        [
                                                name    : 'app.label.quality.libraries.issue',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/issue/index',
                                                position: 3,
                                                role    : 'ROLE_PVQ_VIEW',
                                        ]
                                ]
                        ],
                        [
                                name    : 'app.label.tasks',
                                icon    : 'md md-clipboard-text',
                                link    : '#',
                                position: 7,
                                role    : 'ROLE_ACTION_ITEM',
                                children: [
                                        [
                                                name    : 'app.label.topnav.action.items',
                                                icon    : Constants.BLANK_STRING,
                                                link    : '/actionItem/index',
                                                position: 1,
                                                role    : 'ROLE_ACTION_ITEM',
                                        ],
                                ]
                        ]
                ]

        ]
    }
}