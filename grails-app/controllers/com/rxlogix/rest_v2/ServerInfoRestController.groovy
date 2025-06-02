package com.rxlogix.rest_v2

import com.jaspersoft.jasperserver.dto.serverinfo.ServerInfo
import com.rxlogix.util.MiscUtil
import grails.plugin.springsecurity.annotation.Secured

/**
 * Created by gologuzov on 20.01.17.
 */
@Secured('permitAll')
class ServerInfoRestController {
    def index() {
        def serverInfo = new ServerInfo(
                build: "20141121_1750",
                dateFormatPattern: "yyyy-MM-dd",
                datetimeFormatPattern: "yyyy-MM-dd'T'HH:mm:ss",
                edition: ServerInfo.ServerEdition.CE,
                editionName: "Community",
                features: "Fusion AHD EXP DB AUD ANA MT",
                licenseType: "Community",
                version: "6.4.3"
        )
        return render(text: MiscUtil.marshal(serverInfo), contentType: "text/xml", encoding: "UTF-8")
    }
}
