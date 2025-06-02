package com.reports

import com.rxlogix.config.ReportRequest
import grails.util.Holders

class ReportRequestTagLib {
    static namespace = "rx"

    def showReportRequest = {attrs, body ->
        if(attrs.actionItemId){
            ReportRequest reportRequest = ReportRequest.getByActionItem(attrs.actionItemId)?.get()
            if(reportRequest){
                def content = """
                    <div class="row" style="width:100%">
                        <div style="width:50%">
                            <span><b>${message(code: 'app.label.action.item.report.request.id')} : </b></span>
                            <span>
                                ${reportRequest.id}
                            </span>
                        </div>
                    </div>
                    <div class="row" style="width:100%">
                        <div style="width:100%">
                            <span><b>${message(code:"app.actionItem.associatedRequest.label")} :</b></span>
                            <span>
                                <a href="${grailsApplication.config.getProperty('grails.appBaseURL')}/reportRequest/show/${reportRequest.id}" >${reportRequest.reportName.encodeAsHTML()}</a>
                            </span>
                        </div>
                    </div>
                """
                out << content
            }
        }
    }

    def renderRRSettingsEntityName = { attrs, body ->
        switch (attrs.type){
            case 'priority':
                out << message(code: 'app.label.reportRequestPriority.appName')
                break
            case 'link':
                out << message(code: 'app.label.reportRequestLinkType.appName')
                break
            case 'field':
                out << message(code: 'app.label.reportRequestField.appName')
                break
             case 'type':
                out << message(code: 'app.label.reportRequestType.appName')
                break
            case 'PSR_TYPE_FILE':
                out << message(code: 'app.label.reportRequest.psrTypeFile')
                break
            case 'INN':
                out << message(code: 'app.label.reportRequest.inn')
                break
            case 'DRUG':
                out << message(code: 'app.label.reportRequest.drugCode')
                break
            default:
                out << message(code: 'app.label.UserDictioname.appName')
        }
    }

}
