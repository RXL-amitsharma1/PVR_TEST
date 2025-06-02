package com.rxlogix

import com.rxlogix.api.SanitizePaginationAttributes
import com.rxlogix.commandObjects.SpotfireCommand
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.mapping.LmProductFamily
import com.rxlogix.user.User
import com.rxlogix.util.SecurityUtil
import com.rxlogix.util.SpotfireUtil
import com.rxlogix.util.Tuple2
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovy.json.JsonSlurper
import javax.servlet.http.Cookie

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class DataAnalysisController implements SanitizePaginationAttributes {
    def spotfireService
    def userService
    def searchService

    def beforeInterceptor = [action: this.&auth, except: ['accessDenied', 'keepAlive']]
    Set<Tuple2> jobIds = new HashSet<>()

    def auth() {
        true
    }

    @Secured(['ROLE_DATA_ANALYSIS'])
    def index() {
        String username = userService.getUser()?.username ?: ""
        String secret = Holders.config.getProperty('spotfire.token_secret')
        String libraryRoot = Holders.config.getProperty('spotfire.libraryRoot')

        return [wp_url         : composeSpotfireUrl(),
                auth_token     : SecurityUtil.encrypt(secret, username),
                libraryRoot    : libraryRoot,
                callback_server: Holders.config.getProperty('spotfire.callbackUrl')
        ]
    }

    @Secured(['ROLE_DATA_ANALYSIS'])
    def create() {
        String cur_username = userService.getUser()?.username
        return [user_name: spotfireService.getHashedValue(cur_username)]
    }

    @Secured(['ROLE_DATA_ANALYSIS'])
    def view(String fileName) {
        String username = userService.getUser()?.username ?: ""
        User user = userService.getUserByUsername(username)

        if (user) {
            String secret = Holders.config.getProperty('spotfire.token_secret')
            String token = SecurityUtil.encrypt(secret, username)
            String libraryRoot = Holders.config.getProperty('spotfire.libraryRoot')

            int lastSlashIndex = fileName.lastIndexOf("/")
            String prefix = fileName.substring(0, lastSlashIndex)
            String actualFileName = spotfireService.decodeFileName(fileName.substring(lastSlashIndex + 1))

            //Handling for the case when blinded user directly accesses unblinded file URL
            if (actualFileName.startsWith("U_") && user.isBlinded) {
                actualFileName = "B_" + actualFileName.substring(2)
            } else if (actualFileName.startsWith("B_") && !user.isBlinded) {
                actualFileName = "U_" + actualFileName.substring(2)
            }

            fileName = "$prefix/$actualFileName"

            log.info("Token is ------ : $token")

            spotfireService.addAuthToken(token, username, user.fullName, user.email)

            response.addCookie(new Cookie("pvr-spotfire-cookie", System.currentTimeMillis().toString()))
            render(view: 'view', model: [fileName: fileName])
        } else {
            render status: 403, contentType: 'text/html', text: "You are not allowed to view the page"
        }
    }

    @Secured(['ROLE_DATA_ANALYSIS'])
    def list() {
        render(spotfireService.reportFilesMapData as JSON)
    }

    @Secured(['ROLE_DATA_ANALYSIS'])
    def generate(SpotfireCommand spotfireCommand) {
        if (request.method == 'GET') {
            notSaved()
            return
        }
        if (spotfireCommand.validate()) {
            spotfireService.reserveFileName(spotfireCommand.fullFileName)
            def params = spotfireService.generateReportParams(spotfireCommand.allProductFamilyIds,
                    spotfireCommand.fromDate,
                    spotfireCommand.endDate,
                    spotfireCommand.asOfDate,
                    spotfireCommand.caseSeriesId,
                    spotfireCommand.type,
                    spotfireCommand.fullFileName,
                    Constants.SPOTFIRE_ANALYSIS
            )

            if (Holders.config.spotfire.fileBasedReportGen) {
                log.info("PVR will generate spotfire report into a file folder ${Holders.config.getProperty('spotfire.fileFolder')}")
                def xml = SpotfireUtil.composeXmlBodyForTask(params)
                try {
                    File file = SpotfireUtil.generateAutomationXml(new File(Holders.config.getProperty('spotfire.fileFolder')), xml)
                    if (file.exists()) {
                        if (Holders.config.getProperty('grails.mail.disabled', Boolean))
                            flash.message = message(code: 'app.spotfire.success.msg.email.disabled')
                        else
                            flash.message = message(code: 'app.spotfire.success.msg')
                        redirect action: 'index'
                    } else {
                        flash.error = message(code: 'app.spotfire.failed.generate.report')
                        redirect view: 'index'
                    }

                    log.info("File [${file.getAbsoluteFile()}] is generated")
                } catch (Exception ex) {
                    log.error("Failed generate spotfire report file", ex)
                    flash.warning = message(code: 'app.spotfire.failed.generate.report')
                    render(view: 'create', model: [spotfireCommand: spotfireCommand])
                }
            } else {
                def respMsg = spotfireService.invokeReportGenerationAPI(params)
                JsonSlurper slurper = new JsonSlurper()
                def jsonRsp = slurper.parseText(respMsg)
                if (jsonRsp.JobId) {
                    if (jobIds == null) {
                        jobIds = new HashSet<>()
                    }

                    jobIds.add(new Tuple2(jsonRsp.JobId, jsonRsp.StatusCode))

                    if (Holders.config.grails.mail.disabled)
                        flash.message = message(code: 'app.spotfire.success.msg.email.disabled')
                    else
                        flash.message = message(code: 'app.spotfire.success.msg')
                    redirect action: 'index'
                } else {
                    flash.error = message(code: 'app.spotfire.failed.generate.report')
                    redirect view: 'index'
                }
            }
        } else {
            log.warn(spotfireCommand.errors.allErrors?.toString())
            flash.warning = message(code: 'app.spotfire.failed.generate.report')
            render(view: 'create', model: [spotfireCommand: spotfireCommand])
        }
    }

    def accessDenied() {
        render view: 'access_denied'
    }

    @Secured(['ROLE_DATA_ANALYSIS'])
    def keepAlive() {
        request.getSession false
        render status: 200
    }

    private notSaved() {
        request.withFormat {
            form {
                flash.error = message(code: 'default.not.saved.message')
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    static String composeSpotfireUrl() {
        def spotfireConfig = Holders.config.spotfire

        StringBuilder spotfireUrl = new StringBuilder()

        spotfireUrl.append(spotfireConfig.protocol ?: 'http')
        spotfireUrl.append('://' + spotfireConfig.server)
        if (spotfireConfig.port) {
            spotfireUrl.append(':' + spotfireConfig.port)
        }
        spotfireUrl.append('/' + spotfireConfig.path)

        return spotfireUrl.toString()
    }

    def getProductFamilyList() {
        forSelectBox(params)
        List items = []
        List productFamilyItems = []
        Integer totalCount = 0
        try {
            LmProductFamily.withNewSession {
                items = LmProductFamily.createCriteria().list([offset: params.offset, max: params.max, order: 'asc', sort: 'name']) {
                    if (params.term) {
                        iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(params.term)}%")
                    }
                }
                totalCount = items?.totalCount
            }
            String userLocale = userService?.getCurrentUser()?.preference?.locale as String
            productFamilyItems = spotfireService.appendLingualSuffix(items, userLocale)
        } catch (Throwable ex) {
            log.error(ex.getMessage())
        }
        render([items: productFamilyItems, total_count: totalCount] as JSON)
    }

    def getDetails() {
        String username = userService.getUser()?.username ?: ""
        User user = userService.getUserByUsername(username)
        if (user) {
            def spotfireConfig = Holders.config.getProperty('spotfire', Map)
            String secret = spotfireConfig.token_secret
            String token = SecurityUtil.encrypt(secret, username)
            spotfireService.addAuthToken(token, username, user.fullName, user.email)
            render([
                    user_name      : spotfireService.getHashedValue(username),
                    libraryRoot    : spotfireConfig.libraryRoot,
                    wp_url         : composeSpotfireUrl(),
                    auth_token     : token,
                    callback_server: spotfireConfig.callbackUrl,
                    server         : spotfireConfig.server,
                    port           : spotfireConfig.port,
                    path           : spotfireConfig.path,
                    domainName     : spotfireConfig.domainName,
                    version        : spotfireConfig.version,
                    file_name      : spotfireConfig.filename,
                    protocol       : spotfireConfig.protocol,
                    interval       : spotfireConfig.keepAlive.interval
            ] as JSON)
        }

    }
}
