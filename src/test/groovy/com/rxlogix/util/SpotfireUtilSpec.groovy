package com.rxlogix.util

import com.rxlogix.util.spotfire.HttpGet
import jespa.security.PasswordCredential
import jespa.security.RunAs
import spock.lang.Specification

import java.security.PrivilegedExceptionAction

class SpotfireUtilSpec extends Specification {
    def server
    Integer port
    def protocol
    String automationUsername
    String automationPassword
    String ntlmAcct
    String ntlmPass
    String xmlContent = """
<as:Job xmlns:as='urn:tibco:spotfire.dxp.automation'>
  <as:Tasks>
    <OpenAnalysisFromLibrary xmlns='urn:tibco:spotfire.dxp.automation.tasks'>
      <as:Title>Open Analysis from Library</as:Title>
      <AnalysisPath>Report Templates/Drug Template</AnalysisPath>
      <ConfigurationBlock>drug_p1.prod_family={"100084"};drug_p2.start_date={"01-01-1900"};drug_p3.end_date={"20-04-2018"};drug_p4.as_of_date={"20-04-2018"};drug_p5.prod_family={"100084"};drug_p6.start_date={"01-01-1900"};drug_p7.end_date={"20-04-2018"};drug_p8.as_of_date={"20-04-2018"};drug_p9.prod_family={"100084"};drug_p10.start_date={"01-01-1900"};drug_p11.end_date={"20-04-2018"};drug_p12.as_of_date={"20-04-2018"};drug_p13.prod_family={"100084"};drug_p14.start_date={"01-01-1900"};drug_p15.end_date={"20-04-2018"};drug_p16.as_of_date={"20-04-2018"};drug_p17.prod_family={"100084"};drug_p18.start_date={"01-01-1900"};drug_p19.end_date={"20-04-2018"};drug_p20.as_of_date={"20-04-2018"};drug_p21.prod_family={"100084"};drug_p22.start_date={"01-01-1900"};drug_p23.end_date={"20-04-2018"};drug_p24.as_of_date={"20-04-2018"};drug_p25.prod_family={"100084"};drug_p26.start_date={"01-01-1900"};drug_p27.end_date={"20-04-2018"};drug_p28.as_of_date={"20-04-2018"};drug_p29.prod_family={"100084"};drug_p30.start_date={"01-01-1900"};drug_p31.end_date={"20-04-2018"};drug_p32.as_of_date={"20-04-2018"};drug_p33.prod_family={"100084"};drug_p34.start_date={"01-01-1900"};drug_p35.end_date={"20-04-2018"};drug_p36.as_of_date={"20-04-2018"};drug_p37.prod_family={"100084"};drug_p38.start_date={"01-01-1900"};drug_p39.end_date={"20-04-2018"};drug_p40.as_of_date={"20-04-2018"};drug_p41.prod_family={"100084"};drug_p42.start_date={"01-01-1900"};drug_p43.end_date={"20-04-2018"};drug_p44.as_of_date={"20-04-2018"};drug_p45.prod_family={"100084"};drug_p46.start_date={"01-01-1900"};drug_p47.end_date={"20-04-2018"};drug_p48.as_of_date={"20-04-2018"};drug_p49.prod_family={"100084"};drug_p50.start_date={"01-01-1900"};drug_p51.end_date={"20-04-2018"};drug_p52.as_of_date={"20-04-2018"};drug_p53.prod_family={"100084"};drug_p54.start_date={"01-01-1900"};drug_p55.end_date={"20-04-2018"};drug_p56.as_of_date={"20-04-2018"};drug_p57.prod_family={"100084"};drug_p58.start_date={"01-01-1900"};drug_p59.end_date={"20-04-2018"};drug_p60.as_of_date={"20-04-2018"};drug_p61.prod_family={"100084"};drug_p62.start_date={"01-01-1900"};drug_p63.end_date={"20-04-2018"};drug_p64.as_of_date={"20-04-2018"};drug_p65.prod_family={"100084"};drug_p66.start_date={"01-01-1900"};drug_p67.end_date={"20-04-2018"};drug_p68.as_of_date={"20-04-2018"};drug_p69.prod_family={"100084"};drug_p70.start_date={"01-01-1900"};drug_p71.end_date={"20-04-2018"};drug_p72.as_of_date={"20-04-2018"};drug_p73.prod_family={"100084"};drug_p74.start_date={"01-01-1900"};drug_p75.end_date={"20-04-2018"};drug_p76.as_of_date={"20-04-2018"};drug_p77.case_list_id={"-1"};drug_p78.case_list_id={"-1"};drug_p79.case_list_id={"-1"};drug_p80.case_list_id={"-1"};drug_p81.case_list_id={"-1"};drug_p82.case_list_id={"-1"};drug_p83.case_list_id={"-1"};drug_p84.case_list_id={"-1"};drug_p85.case_list_id={"-1"};drug_p86.case_list_id={"-1"};drug_p87.case_list_id={"-1"};drug_p88.case_list_id={"-1"};drug_p89.case_list_id={"-1"};drug_p90.case_list_id={"-1"};drug_p91.case_list_id={"-1"};drug_p92.case_list_id={"-1"};drug_p93.case_list_id={"-1"};drug_p94.case_list_id={"-1"};drug_p95.case_list_id={"-1"};drug_p96.case_list_id={"-1"};drug_p97.case_list_id={"-1"};drug_p98.case_list_id={"-1"};drug_p99.case_list_id={"-1"};drug_p100.case_list_id={"-1"};server_url={"http://localhost:8080/reports/report/exportSingleCIOMS?caseNumber="};</ConfigurationBlock>
    </OpenAnalysisFromLibrary>
    <SaveAnalysisToLibrary xmlns='urn:tibco:spotfire.dxp.automation.tasks'>
      <as:Title>Save Analysis to Library</as:Title>
      <LibraryPath>/Reports/Auto Prod 1_01-Jan-1900_20-Apr-2018_AoD_20-Apr-2018_Drugda</LibraryPath>
      <EmbedData>true</EmbedData>
      <DeleteExistingBookmarks>false</DeleteExistingBookmarks>
      <AnalysisDescription>{jobid}</AnalysisDescription>
    </SaveAnalysisToLibrary>
    <SendEmail xmlns='urn:tibco:spotfire.dxp.automation.tasks'>
      <as:Title>Send Email</as:Title>
      <Recipients>
        <string>admin@rxlogix.com</string>
      </Recipients>
      <Subject>Spotfire Report Generated. File name: Auto Prod 1_01-Jan-1900_20-Apr-2018_AoD_20-Apr-2018_Drugda</Subject>
      <Message>Hi, 
 
The PV Analytics analysis file you have generated for Auto Prod 1 is complete. The details of the PV Analytics analysis file you have requested are shown below:
 
 
Product Family Name: Auto Prod 1
Reporting Period Date Range: 01-Jan-1900 to 20-Apr-2018
As Of Date: 20-Apr-2018
File Type: Drug
File Name: Auto Prod 1_01-Jan-1900_20-Apr-2018_AoD_20-Apr-2018_Drugda
File Generation Request Date/Time: 20-Apr-2018 12:13:18 AM
 
To open your analysis file:
1. Navigate to http://localhost:8080/reports/dataAnalysis/index
2. Log into application using username and password
3. In the Search box on the page, Enter the File Name above
4. Click on ‘View’ button in action column corresponding to the above filename 
5. The PV Analytics analysis file will open in the new tab
 
</Message>
      <Links />
      <Attachments />
    </SendEmail>
  </as:Tasks>
</as:Job>
"""

    def setup() {
        server = '10.100.6.8'
        port = 443
        protocol = 'https'
        automationUsername = 'admin'
        automationPassword = 'admin'
        ntlmAcct = 'spotfiretest$@rxlogix.com'
        ntlmPass = 'rxlogix1!'
    }

    /*
    def "test trigger the job"() {
        expect:
        SpotfireUtil.triggerJob("10.100.6.8", "8888" as Integer,
                'ntlm', xmlContent, 'admin', 'admin') != null

    }

    def "test build authToken"() {
        expect:
        SpotfireUtil.buildAuthToken("admin", "abdddddddd") == ""
    }

    def "test with NTML"() {
        setup:
        def resp = SpotfireUtil.triggerJobOnNTML(server, port, protocol, xmlContent, ntlmAcct, ntlmPass)
        println("The response is: ${resp}")
        expect:
        resp != null
    }
    */

    def "test for generateAutomationXml"() {
        setup:
        File folder = new File("test-folder")
        folder.mkdir()
        (0..3).each { new File(folder, "job-${it}.xml").write("test")}

        SpotfireUtil.generateAutomationXml(folder, "no-test")

        expect:
        File theFile = new File(folder, 'job-5.xml')
        theFile.exists()
        cleanup:
        folder.deleteDir()
    }
}



