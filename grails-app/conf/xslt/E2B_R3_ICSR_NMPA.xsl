<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="urn:hl7-org:v3">
    <!--<xsl:include href="upgrade.xsl"/>
	<xsl:include href="upgrade-m.xsl"/>
	<xsl:include href="upgrade-a1.xsl"/>-->
    <xsl:output indent="yes" method="xml" omit-xml-declaration="no" encoding="utf-8"/>
    <xsl:strip-space elements="*"/>
    <!--ICH ICSR : conversion of the main structure incl. root element and controlActProcess
	E2B(R2): root element "ichicsr"
	E2B(R3): root element "PORR_IN049016UV"
	-->


    <xsl:include href="ICH_COMMON_VARIABLE.xsl"/>
    <xsl:include href="ICH_COMMON_TAGS.xsl"/>

    <xsl:template match="/">
        <MCCI_IN200100UV01 ITSVersion="XML_1.0">
            <!--edit schema location as needed-->
            <xsl:attribute name="xsi:schemaLocation">urn:hl7-org:v3 MCCI_IN200100UV01.xsd</xsl:attribute>
            <!--M.x - Message Header-->
            <xsl:apply-templates select="/ichicsr/ichicsrtransmissionidentification" mode="part-a"/>
            <!--Report-->
            <xsl:apply-templates select="/ichicsr/safetyreport" mode="report"/>
            <!--M.x - Message Footer-->
            <xsl:apply-templates select="/ichicsr/ichicsrtransmissionidentification" mode="part-c"/>
        </MCCI_IN200100UV01>
    </xsl:template>

    <!--Safety Report (main) :
E2B(R2): element "safetyreport" inside "ichicsr"
E2B(R3): element "investigationEvent"
-->
    <xsl:template match="safetyreport" mode="main">
        <subject typeCode="SUBJ">
            <investigationEvent classCode="INVSTG" moodCode="EVN">
                <!--A.1.0.1 - Senders (Case) Safety Report Unique Identifier-->
                <id root="{$oidSendersReportNamespace}" extension="{safetyreportid}"/>
                <xsl:comment>C.1.1:Senders (case) Safety Report Unique Identifier</xsl:comment>
                <!--A.1.10.1 - Worldwide Unique Case Identification Number - Rule STR-03-->
                <xsl:choose>
                    <xsl:when test="string-length(wwuid) > 0">
                        <id root="{$oidWorldWideCaseID}" extension="{wwuid}"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <id root="{$oidWorldWideCaseID}" nullFlavor="UNK"/>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:comment>C.1.8.1:Worldwide Unique Case Identification</xsl:comment>

                <code code="PAT_ADV_EVNT" codeSystem="2.16.840.1.113883.5.4"/>
                <!--B.5.1 Case Narrative-->
                <xsl:apply-templates select="summary/narrativeincludeclinical"/>
                <!--<statusCode code="active"/>-->
                <!--<xsl:if test="(completeorincomplete)> 0"/>
<xsl:choose>
<xsl:when test="(completeorincomplete)= 1"><statusCode code="Active"/> </xsl:when>
<xsl:when test="(completeorincomplete)= 2"><statusCode code="InActive"/> </xsl:when>
</xsl:choose>
<xsl:comment> J2.7.1: 完了䚸未完了༊分	Complete/Incomplete </xsl:comment>-->
                <!--A.1.6 - Date Report Was First Received from Source-->
                <xsl:if test="string-length(receivedate) > 0">
                    <statusCode/>
                    <effectiveTime>
                        <low value="{receivedate}"/>
                    </effectiveTime>
                    <xsl:comment>C.1.4:Date Report Was First Received from Source</xsl:comment>
                </xsl:if>
                <!--A.1.7 - Date of Most Recent Information for this Case-->
                <xsl:if test="string-length(receiptdate) > 0">
                    <availabilityTime value="{receiptdate}"/>
                    <xsl:comment>C.1.5:Date of Most Recent Information for This Report</xsl:comment>
                </xsl:if>
                <!--A.1.8.1.r Document Held by Sender-->
                <xsl:comment>C.1.6.1.r.1: Documents Held by Sender (repeat as necessary)</xsl:comment>
                <xsl:apply-templates select="additionaldocuments"/>

                <!--A.4.r Literature References-->
                <xsl:comment>C.4.r.1: Literature Reference(s)</xsl:comment>
                <xsl:apply-templates select="literature"/>
                <!--J2・15・r-->
                <xsl:apply-templates select="pmdapublishedcountry"/>

                <!--B.1.x - Patient-->
                <xsl:comment>D.1: Patient (name or initials)</xsl:comment>
                <xsl:apply-templates select="patient" mode="identification"/>
                <xsl:apply-templates select="patient/summary/narrativesendercommentnative"/>
                <!--A.1.8.1 - Are Additional Documents Available?-->

                <xsl:if test="string-length(additionaldocument) > 0">
                    <xsl:comment>C.1.6.1: Are Additional Documents Available?  </xsl:comment>
                    <component typeCode="COMP">
                        <observationEvent classCode="OBS" moodCode="EVN">
                            <code code="{$AdditionalDocumentsAvailable}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$ichObservationCLVersion}"/>
                            <xsl:variable name="isNullFlavourAddDoc">
                                <xsl:call-template name="isNullFlavour">
                                    <xsl:with-param name="value" select="additionaldocument"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:choose>
                                <xsl:when test="$isNullFlavourAddDoc = 'yes'">
                                    <xsl:variable name="NullFlavourWOSqBrcktC161">
                                        <xsl:call-template name="getNFValueWithoutSqBrckt">
                                            <xsl:with-param name="nfvalue" select="additionaldocument"/>
                                        </xsl:call-template>
                                    </xsl:variable>
                                    <value xsi:type="BL" nullFlavor="{$NullFlavourWOSqBrcktC161}"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <value xsi:type="BL" value="{additionaldocument}"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </observationEvent>
                    </component>
                </xsl:if>

                <component typeCode="COMP">
                    <observationEvent classCode="OBS" moodCode="EVN">
                        <code code="{$LocalCriteriaForExpedited}" codeSystem="{$oidObservationCode}"
                              displayName="localCriteriaForExpedited"/>
                        <xsl:choose>
                            <xsl:when test="fulfillexpeditecriteria= 1">
                                <value xsi:type="BL" value="true"/>
                            </xsl:when>
                            <xsl:when test="fulfillexpeditecriteria= 2">
                                <value xsi:type="BL" value="false"/>
                            </xsl:when>
                            <xsl:when test="fulfillexpeditecriteria= 'TRUE'">
                                <value xsi:type="BL" value="true"/>
                            </xsl:when>
                            <xsl:when test="fulfillexpeditecriteria= 'FALSE'">
                                <value xsi:type="BL" value="false"/>
                            </xsl:when>
                            <xsl:when test="fulfillexpeditecriteria= 'True'">
                                <value xsi:type="BL" value="true"/>
                            </xsl:when>
                            <xsl:when test="fulfillexpeditecriteria= 'False'">
                                <value xsi:type="BL" value="false"/>
                            </xsl:when>
                            <xsl:when test="fulfillexpeditecriteria= 'true'">
                                <value xsi:type="BL" value="true"/>
                            </xsl:when>
                            <xsl:when test="fulfillexpeditecriteria= 'false'">
                                <value xsi:type="BL" value="false"/>
                            </xsl:when>
                            <xsl:when test="fulfillexpeditecriteria= 'Yes'">
                                <value xsi:type="BL" value="true"/>
                            </xsl:when>
                            <xsl:when test="fulfillexpeditecriteria= 'No'">
                                <value xsi:type="BL" value="false"/>
                            </xsl:when>
                            <xsl:when test="fulfillexpeditecriteria= 'yes'">
                                <value xsi:type="BL" value="true"/>
                            </xsl:when>
                            <xsl:when test="fulfillexpeditecriteria= 'no'">
                                <value xsi:type="BL" value="false"/>
                            </xsl:when>
                            <xsl:when test="fulfillexpeditecriteria= 'YES'">
                                <value xsi:type="BL" value="true"/>
                            </xsl:when>
                            <xsl:when test="fulfillexpeditecriteria= 'NO'">
                                <value xsi:type="BL" value="false"/>
                            </xsl:when>

                            <xsl:otherwise>
                                <value xsi:type="BL" nullFlavor="NI"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </observationEvent>
                    <xsl:comment>C.1.7: Does This Case Fulfil the Local Criteria for an Expedited Report?</xsl:comment>
                </component>

                <!-- C.1.CN.1: Report Source -->
                <xsl:if test="string-length(reportsource) > 0 and string-length(reportsource) = 1">
                    <component typeCode="COMP">
                        <observationEvent classCode="OBS" moodCode="EVN">
                            <code code="{$ReportSource}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$nmpaoidC1CN1LVersion}" displayName="reportSource"/>
                            <xsl:choose>
                                <xsl:when test="reportsource= 1">
                                    <value xsi:type="CE" code="{reportsource}" />
                                    <!-- displayName="Regulatory agency" -->
                                </xsl:when>
                                <xsl:when test="reportsource= 2">
                                    <value xsi:type="CE" code="{reportsource}" />
                                    <!-- displayName="Patient/Family and Friends" -->
                                </xsl:when>
                                <xsl:when test="reportsource= 3">
                                    <value xsi:type="CE" code="{reportsource}" />
                                    <!-- displayName="Medical institution" -->
                                </xsl:when>
                                <xsl:when test="reportsource= 4">
                                    <value xsi:type="CE" code="{reportsource}" />
                                    <!-- displayName="Operating business" -->
                                </xsl:when>
                                <xsl:when test="reportsource= 5">
                                    <value xsi:type="CE" code="{reportsource}" />
                                    <!-- displayName="Document (need to attach the attachment of the full text)" -->
                                </xsl:when>
                                <xsl:when test="reportsource= 6">
                                    <value xsi:type="CE" code="{reportsource}" />
                                    <!-- displayName="Research (refers to the adverse reaction report from post-market research Research)" -->
                                </xsl:when>
                                <xsl:when test="reportsource= 7">
                                    <value xsi:type="CE" code="{reportsource}" />
                                    <!-- displayName="Item (refers to the adverse reaction report from the patient-oriented or Doctor's market items, etc.)" -->
                                </xsl:when>
                                <xsl:when test="reportsource= 8">
                                    <value xsi:type="CE" code="{reportsource}" />
                                    <!-- displayName="other" -->
                                </xsl:when>
                            </xsl:choose>
                            <xsl:comment>C.1.CN.1 报告来源</xsl:comment>
                        </observationEvent>
                    </component>
                </xsl:if>

                <!-- C.1.CN.2: Report Classification -->
                <xsl:if test="string-length(reportcategory) > 0 and string-length(reportcategory) = 2">
                    <component typeCode="COMP">
                        <observationEvent classCode="OBS" moodCode="EVN">
                            <code code="{$ReportCategory}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$nmpaoidC1CN1LVersion}" displayName="classifyOfReport"/>
                            <xsl:choose>
                                <xsl:when test="reportcategory= 11">
                                    <value xsi:type="CE" code="{reportcategory}" />
                                    <!-- displayName="Pre-IPO domestic report" -->
                                </xsl:when>
                                <xsl:when test="reportcategory= 12">
                                    <value xsi:type="CE" code="{reportcategory}" />
                                    <!-- displayName="Post-IPO domestic reporting" -->
                                </xsl:when>
                                <xsl:when test="reportcategory= 21">
                                    <value xsi:type="CE" code="{reportcategory}" />
                                    <!-- displayName="Pre-IPO overseas reporting" -->
                                </xsl:when>
                                <xsl:when test="reportcategory= 22">
                                    <value xsi:type="CE" code="{reportcategory}" />
                                    <!-- displayName="Post-IPO overseas reporting" -->
                                </xsl:when>
                            </xsl:choose>
                            <xsl:comment>C.1.CN.2 报告分类</xsl:comment>
                        </observationEvent>
                    </component>
                </xsl:if>

                <!-- C.1.CN.3: Holder ID (free text) -->
                <xsl:if test="string-length(holderidentification) > 0">
                    <component typeCode="COMP">
                        <observationEvent classCode="OBS" moodCode="EVN">
                            <code code="{$MAHID}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$nmpaoidC1CN1LVersion}" displayName="mahID"/>
                            <value xsi:type="CE" code="{holderidentification}"/>
                            <xsl:comment>C.1.CN.3 持有人标识</xsl:comment>
                        </observationEvent>
                    </component>
                </xsl:if>

                <xsl:apply-templates select="summary/casesummarynarrative"  mode="case-summary"/>

                <!--	A.1.9 - Does this Case Fulfil the Local Criteria for an Expedited Report?
<xsl:comment> C.1.7: Does This Case Fulfil the Local Criteria for an Expedited Report? </xsl:comment>
<xsl:call-template name="fulfillexpeditecriteria"/>-->
                <!-- C.1.8.2: First Sender of This Case -->

                <xsl:if test="string-length(icsrsource)> 0">
                    <xsl:comment>C.1.8.2: First Sender of This Case</xsl:comment>
                    <outboundRelationship typeCode="SPRT">
                        <relatedInvestigation classCode="INVSTG" moodCode="EVN">
                            <code code="{$InitialReport}" codeSystem="{$oidReportRelationCode}"
                                  codeSystemVersion="{$emaReportRelationCLVersion}"/>
                            <subjectOf2 typeCode="SUBJ">
                                <controlActEvent classCode="CACT" moodCode="EVN">
                                    <author typeCode="AUT">
                                        <assignedEntity classCode="ASSIGNED">
                                            <code code="{icsrsource}" codeSystem="{$oidFirstSender}"
                                                  codeSystemVersion="{$emaoidC182CLVersion}"/>
                                        </assignedEntity>
                                    </author>
                                </controlActEvent>
                            </subjectOf2>
                        </relatedInvestigation>
                    </outboundRelationship>
                </xsl:if>
                <!--</xsl:if>-->
                <xsl:apply-templates select="linkreport"/>
                <!--A.2.r Primary Sources-->
                <xsl:comment>C.2.r Primary Sources</xsl:comment>
                <xsl:apply-templates select="primarysource"/>
                <!--A.3 Sender-->
                <xsl:comment>C.3 Sender</xsl:comment>
                <xsl:apply-templates select="sender"/>
                <!--A.1.11 Report Duplicate-->
                <xsl:apply-templates select="reportduplicate"/>
                <!--A.1.4 - Type of Report-->
                <xsl:comment>C.1.3 Type of Report</xsl:comment>
                <xsl:comment>C.1.3.CSV Type of Report Code System Version</xsl:comment>
                <subjectOf2 typeCode="SUBJ">
                    <investigationCharacteristic classCode="OBS" moodCode="EVN">
                        <code code="{$ReportType}" codeSystem="{$oidReportCharacterizationCode}"
                              codeSystemVersion="{$oidReportTypeCSV}" displayName="ichReportType"/>
                        <xsl:choose>
                            <xsl:when test="reporttype= 1">
                                <value xsi:type="CE" code="{reporttype}" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="SpontaneousReport"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 2">
                                <value xsi:type="CE" code="{reporttype}" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="ReportFromStudy"/>
                            </xsl:when>
                            <xsl:when test="reporttype=3">
                                <value xsi:type="CE" code="{reporttype}" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="Other"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 4">
                                <value xsi:type="CE" code="{reporttype}" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="Notavailabletosender/unknown"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 01">
                                <value xsi:type="CE" code="1" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="SpontaneousReport"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 02">
                                <value xsi:type="CE" code="2" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="ReportFromStudy"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 03">
                                <value xsi:type="CE" code="3" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="Other"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 04">
                                <value xsi:type="CE" code="4" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="Notavailabletosender/unknown"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 001">
                                <value xsi:type="CE" code="1" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="SpontaneousReport"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 002">
                                <value xsi:type="CE" code="2" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="ReportFromStudy"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 003">
                                <value xsi:type="CE" code="3" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="Other"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 004">
                                <value xsi:type="CE" code="4" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="Notavailabletosender/unknown"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 'StimulatedSpontaneous'">
                                <value xsi:type="CE" code="1" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="StimulatedSpontaneous"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 'SpontaneousReport'">
                                <value xsi:type="CE" code="1" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="SpontaneousReport"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 'ReportFromStudy'">
                                <value xsi:type="CE" code="2" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="Reportfromstudy"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 'Other'">
                                <value xsi:type="CE" code="3" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="Other"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 'Notavailabletosender/unknown'">
                                <value xsi:type="CE" code="4" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="Notavailabletosender/unknown"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 'STIMULATEDSPONTANEOUS'">
                                <value xsi:type="CE" code="1" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="StimulatedSpontaneous"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 'SPONTANEOUSREPORT'">
                                <value xsi:type="CE" code="1" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="SpontaneousReport"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 'REPORTFROMSTUDY'">
                                <value xsi:type="CE" code="2" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="Reportfromstudy"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 'OTHER'">
                                <value xsi:type="CE" code="3" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="Other"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 'NOTAVAILABLETOSENDER/UNKNOWN'">
                                <value xsi:type="CE" code="4" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="Notavailabletosender/unknown"/>
                            </xsl:when>

                            <xsl:when test="reporttype= 'stimulatedspontaneous'">
                                <value xsi:type="CE" code="1" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="StimulatedSpontaneous"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 'spontaneousreport'">
                                <value xsi:type="CE" code="1" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="SpontaneousReport"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 'reportfromstudy'">
                                <value xsi:type="CE" code="2" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="Reportfromstudy"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 'other'">
                                <value xsi:type="CE" code="3" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="Other"/>
                            </xsl:when>
                            <xsl:when test="reporttype= 'Notavailabletosender/unknown'">
                                <value xsi:type="CE" code="4" codeSystemVersion="{reporttypecsv}"
                                       codeSystem="{$oidReportType}" displayName="Notavailabletosender/unknown"/>
                            </xsl:when>
                        </xsl:choose>
                    </investigationCharacteristic>
                </subjectOf2>

                <!--A.1.11 - Other Case Identifiers in Previous Transmissions-->
                <xsl:comment>C.1.9.1: Other Case Identifiers in Previous Transmissions</xsl:comment>
                <subjectOf2 typeCode="SUBJ">
                    <investigationCharacteristic classCode="OBS" moodCode="EVN">
                        <code code="{$OtherCaseIDs}" codeSystem="{$oidReportCharacterizationCode}"
                              codeSystemVersion="{$emaReportCharacterizationCLVersion}"/>
                        <xsl:variable name="isNullFlavourDuplicate">
                            <xsl:call-template name="isNullFlavour">
                                <xsl:with-param name="value" select="duplicate"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:choose>
                            <xsl:when test="$isNullFlavourDuplicate = 'yes'">
                                <xsl:variable name="NullFlavourWOSqBrcktC191">
                                    <xsl:call-template name="getNFValueWithoutSqBrckt">
                                        <xsl:with-param name="nfvalue" select="duplicate"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <value xsi:type="BL" nullFlavor="{$NullFlavourWOSqBrcktC191}"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <value xsi:type="BL" value="{duplicate}"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </investigationCharacteristic>
                </subjectOf2>

                <!--A.1.13 Report Nullification / Amendment-->
                <xsl:if test="string-length(casenullificationoramendment) > 0">
                    <xsl:comment>C.1.11.1: Report Nullification / Amendment</xsl:comment>
                    <subjectOf2 typeCode="SUBJ">
                        <investigationCharacteristic classCode="OBS" moodCode="EVN">
                            <code code="{$NullificationAmendmentCode}" codeSystem="{$oidReportCharacterizationCode}" codeSystemVersion="{$emaReportCharacterizationCLVersion}"/>
                            <value xsi:type="CE" code="{casenullificationoramendment}" codeSystem="{$oidNullificationAmendment}" codeSystemVersion="{casenullificationoramendmentcsv}"/>
                        </investigationCharacteristic>
                    </subjectOf2>
                </xsl:if>
                <!--A.1.13.1 Reason for Nullification / Amendment-->
                <xsl:if test="string-length(nullificationoramendmentreason) > 0">
                    <xsl:comment>C.1.11.2: Reason for Nullification / Amendment</xsl:comment>
                    <subjectOf2 typeCode="SUBJ">
                        <investigationCharacteristic classCode="OBS" moodCode="EVN">
                            <code code="{$NullificationAmendmentReason}" codeSystem="{$oidReportCharacterizationCode}" codeSystemVersion="{$emaReportCharacterizationCLVersion}"/>
                            <value xsi:type="CE">
                                <originalText mediaType="text/plain">
                                    <xsl:value-of select="nullificationoramendmentreason"/>
                                </originalText>
                            </value>
                        </investigationCharacteristic>
                    </subjectOf2>
                </xsl:if>
                <!--A.1.13.1 Reason for Nullification / Amendment-->
            </investigationEvent>
        </subject>
    </xsl:template>

    <xsl:for-each select="narrativesendercommentnative/Native">
        <subject typeCode="SUBJ">
            <investigationEvent classCode="INVSTG" moodCode="EVN">
                <xsl:if test="string-length(summaryandreportercomments) and string-length(summaryandreportercommentslang) > 0">
                    <component typeCode="COMP">
                        <code code="{$SummaryAndComment}" codeSystem="{$oidObservationCode}"/>
                        <author typeCode="AUT">
                            <assignedEntity classCode="ASSIGNED">
                                <code code="{$Reporter}" codeSystem="{$oidAssignedEntityRoleCode}"/>
                                <observationEvent moodCode="EVN" classCode="OBS">
                                    <value language="{summaryandreportercommentslang}" xsi:type="ED"
                                           mediaType="text/plain">
                                        <xsl:value-of select="summaryandreportercomments"/>
                                    </value>
                                </observationEvent>
                            </assignedEntity>
                        </author>
                        <xsl:comment>H.5.1a and H.5.1b Narrative and Sendercomment in Native Languague</xsl:comment>
                    </component>
                </xsl:if>
            </investigationEvent>
        </subject>
    </xsl:for-each>
    <!--Narrative Include Clinical :
E2B(R2): element "narrativeincludeclinical"
E2B(R3): element "investigationEvent"
-->

    <xsl:template match="summary/narrativeincludeclinical">
        <xsl:if test="string-length(.) > 0">
            <text mediaType="text/plain">
                <xsl:value-of select= "substring((.),1,100000)"/>
            </text>
            <xsl:comment>H.1: Case Narrative Including Clinical Course, Therapeutic Measures, Outcome and Additional Relevant Information</xsl:comment>
        </xsl:if>
    </xsl:template>


    <!--Document List :
E2B(R2): element "documentlist" inside "safetyreport"
E2B(R3): element "reference"
-->
    <xsl:template match="additionaldocuments">
        <!--A.1.8.1.r.1 - Documents Held by Sender-->
        <xsl:if test="string-length(documentlist) > 0">
            <reference typeCode="REFR">
                <document classCode="DOC" moodCode="EVN">
                    <code codeSystem="{$oidichreferencesource}" code="{$documentsHeldBySender}"
                          displayName="documentsHeldBySender"/>
                    <title>
                        <xsl:value-of select="documentlist"/>
                    </title>
                    <xsl:comment>C.1.6.1.r.1:Documents Held by Sender</xsl:comment>
                    <xsl:variable name="MediaType">
                        <xsl:value-of select="substring-after(mediatype2,'.')"/>
                    </xsl:variable>
                    <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    <xsl:if test="$MediaType= 'txt'">
                        <text mediaType="text/plain" representation="TXT">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
                    <xsl:if test="$MediaType= 'pdf'">
                        <text mediaType="application/pdf" representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
<!--                    <xsl:if test="$MediaType= 'png'">-->
<!--                        <text mediaType="image/png" representation="B64" compression="DF">-->
<!--                            <xsl:value-of select="includedocuments"/>-->
<!--                        </text>-->
<!--                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>-->
<!--                    </xsl:if>-->
                    <xsl:if test="$MediaType= 'jpeg'">
                        <text mediaType="image/jpeg" representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
                    <xsl:if test="$MediaType= 'jpg'">
                        <text mediaType="image/jpeg" representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
<!--                    <xsl:if test="$MediaType= 'html'">-->
<!--                        <text mediaType="text/html" representation="B64" compression="DF">-->
<!--                            <xsl:value-of select="includedocuments"/>-->
<!--                        </text>-->
<!--                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>-->
<!--                    </xsl:if>-->
<!--                    <xsl:if test="$MediaType= 'psd'">-->
<!--                        <text mediaType="application/octet-stream" representation="B64" compression="DF">-->
<!--                            <xsl:value-of select="includedocuments"/>-->
<!--                        </text>-->
<!--                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>-->
<!--                    </xsl:if>-->
                    <xsl:if test="$MediaType= 'tif'">
                        <text mediaType="image/tiff" representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
                    <xsl:if test="$MediaType= 'docx'">
                        <text mediaType="application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                              representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
                    <xsl:if test="$MediaType= 'xls'">
                        <text mediaType="application/vnd.ms-excel" representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
                    <xsl:if test="$MediaType= 'xlsx'">
                        <text mediaType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                              representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
<!--                    <xsl:if test="$MediaType= 'vsd'">-->
<!--                        <text mediaType="application/x-visio" representation="B64" compression="DF">-->
<!--                            <xsl:value-of select="includedocuments"/>-->
<!--                        </text>-->
<!--                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>-->
<!--                    </xsl:if>-->
                    <xsl:if test="$MediaType= 'rtf'">
                        <text mediaType="application/rtf" representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
                    <xsl:if test="$MediaType= 'doc'">
                        <text mediaType="application/msword" representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
<!--                    <xsl:if test="$MediaType= 'ps'">-->
<!--                        <text mediaType="application/postscript" representation="B64" compression="DF">-->
<!--                            <xsl:value-of select="includedocuments"/>-->
<!--                        </text>-->
<!--                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>-->
<!--                    </xsl:if>-->
<!--                    <xsl:if test="$MediaType= 'mdb'">-->
<!--                        <text mediaType="application/x-msaccess" representation="B64" compression="DF">-->
<!--                            <xsl:value-of select="includedocuments"/>-->
<!--                        </text>-->
<!--                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>-->
<!--                    </xsl:if>-->
<!--                    <xsl:if test="$MediaType= 'bmp'">-->
<!--                        <text mediaType="image/bmp" representation="B64" compression="DF">-->
<!--                            <xsl:value-of select="includedocuments"/>-->
<!--                        </text>-->
<!--                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>-->
<!--                    </xsl:if>-->
<!--                    <xsl:if test="$MediaType= 'xml'">-->
<!--                        <text mediaType="text/xml" representation="B64" compression="DF">-->
<!--                            <xsl:value-of select="includedocuments"/>-->
<!--                        </text>-->
<!--                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>-->
<!--                    </xsl:if>-->
<!--                    <xsl:if test="$MediaType= 'sgm'">-->
<!--                        <text mediaType="text/sgml" representation="B64" compression="DF">-->
<!--                            <xsl:value-of select="includedocuments"/>-->
<!--                        </text>-->
<!--                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>-->
<!--                    </xsl:if>-->
<!--                    <xsl:if test="$MediaType= 'msg'">-->
<!--                        <text mediaType="application/vnd.ms-outlook" representation="B64" compression="DF">-->
<!--                            <xsl:value-of select="includedocuments"/>-->
<!--                        </text>-->
<!--                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>-->
<!--                    </xsl:if>-->
                    <xsl:if test="$MediaType= 'dicom'">
                        <text mediaType="application/dicom" representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
                </document>
            </reference>
        </xsl:if>
    </xsl:template>

    <!--Linked Report:
E2B(R2): element "linkedreport" inside "safetyreport"
E2B(R3): element "relatedInvestigation"
-->


    <xsl:template match="reportduplicate">
        <xsl:comment> C.1.9.1.r.1: Source(s) of the Case Identifier (repeat as necessary) </xsl:comment>
        <xsl:comment> C.1.9.1.r.2 Case Identifier(s) </xsl:comment>
        <xsl:if test="string-length(duplicatesource)>0 and string-length(duplicatenumb)>0">
            <subjectOf1 typeCode="SUBJ">
                <controlActEvent classCode="CACT" moodCode="EVN">
                    <!--A.1.11.r.1 Source(s) of the Case Identifier-->
                    <!--A.1.11.r.2 Case Identifier(s)-->
                    <xsl:choose>
                        <xsl:when test="string-length(duplicatesource) = 0"><id assigningAuthorityName="-" extension="{duplicatenumb}" root="{$oidCaseIdentifier}"/> </xsl:when>
                        <xsl:when test="string-length(duplicatenumb) = 0"><id assigningAuthorityName="{duplicatesource}" extension="-" root="{$oidCaseIdentifier}"/> </xsl:when>
                        <xsl:otherwise><id assigningAuthorityName="{duplicatesource}" extension="{duplicatenumb}" root="{$oidCaseIdentifier}"/></xsl:otherwise>
                    </xsl:choose>
                </controlActEvent>
            </subjectOf1>
        </xsl:if>
    </xsl:template>

    <!--E2B(R2): element "linkedreport" inside "ichicsr/ichicsrbatchheader/ichicsrmessageheader/safetyreport"
	E2B(R3): element "relatedInvestigation"
	-->
    <xsl:template match="linkreport">
        <xsl:if test="string-length(linkreportnumber)>0">
            <xsl:comment>C.1.10: Linked Report Information</xsl:comment>
            <xsl:variable name="positionLinkRptNum">
                <xsl:value-of select="position()"/>
            </xsl:variable>
            <outboundRelationship typeCode="SPRT">
                <relatedInvestigation classCode="INVSTG" moodCode="EVN">
                    <code nullFlavor="NA"/>
                    <subjectOf2 typeCode="SUBJ">
                        <controlActEvent classCode="CACT" moodCode="EVN">
                            <xsl:comment>C.1.10.r: Identification Number of the Report Which Is Linked to This Report -
                                (<xsl:value-of select="$positionLinkRptNum"/>)
                            </xsl:comment>
                            <id extension="{linkreportnumber}" root="{$oidWorldWideCaseID}"/>
                        </controlActEvent>
                    </subjectOf2>
                </relatedInvestigation>
            </outboundRelationship>
        </xsl:if>
    </xsl:template>

    <!-- Reporter Block -->
    <xsl:template match="primarysource">
        <xsl:variable name="positionPriSrc">
            <xsl:value-of select="position()"/>
        </xsl:variable>
        <xsl:comment>C.2.r: Primary Source(s) of Information - (<xsl:value-of select="$positionPriSrc"/>)</xsl:comment>
        <outboundRelationship typeCode="SPRT">
            <!-- C.2.r.5: Primary Source for Regulatory Purposes  -->
            <xsl:choose>
                <xsl:when test="casefirstsource = 1">
                    <xsl:comment>C.2.r.5: Primary Source for Regulatory Purposes</xsl:comment>
                    <priorityNumber value="1"/>
                </xsl:when>
            </xsl:choose>
            <relatedInvestigation classCode="INVSTG" moodCode="EVN">
                <code code="{$SourceReport}" codeSystem="{$oidReportRelationCode}" displayName="sourceReport"/>
                <subjectOf2 typeCode="SUBJ">
                    <controlActEvent classCode="CACT" moodCode="EVN">
                        <author typeCode="AUT">
                            <assignedEntity classCode="ASSIGNED">
                                <addr>
                                    <xsl:call-template name="field-or-mask">
                                        <xsl:with-param name="element">streetAddressLine</xsl:with-param>
                                        <xsl:with-param name="value" select="reporterstreet"/>
                                    </xsl:call-template>
                                    <xsl:comment>C.2.r.2.3:Reporter’s Street</xsl:comment>
                                    <xsl:call-template name="field-or-mask">
                                        <xsl:with-param name="element">city</xsl:with-param>
                                        <xsl:with-param name="value" select="reportercity"/>
                                    </xsl:call-template>
                                    <xsl:comment>C.2.r.2.4:Reporter’s City</xsl:comment>
                                    <xsl:call-template name="field-or-mask">
                                        <xsl:with-param name="element">state</xsl:with-param>
                                        <xsl:with-param name="value" select="reporterstate"/>
                                    </xsl:call-template>
                                    <xsl:comment>C.2.r.2.5:Reporter’s State or Province</xsl:comment>
                                    <xsl:call-template name="field-or-mask">
                                        <xsl:with-param name="element">postalCode</xsl:with-param>
                                        <xsl:with-param name="value" select="reporterpostcode"/>
                                    </xsl:call-template>
                                    <xsl:comment>C.2.r.2.6:Reporter’s Postcode</xsl:comment>
                                </addr>
                                <!--Reporter Telephone-->

                                <xsl:if test="string-length(reportertel) > 0">
                                    <xsl:comment>C.2.r.2.7: Reporter's Phone Number</xsl:comment>
                                    <telecom>
                                        <xsl:variable name="isNullFlavourMaskTelecom">
                                            <xsl:call-template name="isNullFlavour">
                                                <xsl:with-param name="value" select="reportertel"/>
                                            </xsl:call-template>
                                        </xsl:variable>
                                        <xsl:choose>
                                            <xsl:when test="$isNullFlavourMaskTelecom = 'yes'">
                                                <xsl:variable name="NullFlavourWOSqBrcktTelecom">
                                                    <xsl:call-template name="getNFValueWithoutSqBrckt">
                                                        <xsl:with-param name="nfvalue" select="reportertel"/>
                                                    </xsl:call-template>
                                                </xsl:variable>
                                                <xsl:attribute name="nullFlavor">
                                                    <xsl:value-of select="$NullFlavourWOSqBrcktTelecom"/>
                                                </xsl:attribute>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:attribute name="value">
                                                    <xsl:value-of select="reportertel"/>
                                                </xsl:attribute>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </telecom>
                                </xsl:if>


                                <!-- C.2.r.CN.1 : Email -->
                                <xsl:if test="string-length(reporteremail) > 0">
                                    <telecom value="mailto:{reporteremail}"/>
                                    <xsl:comment>C.2.r.CN.1 电子邮箱</xsl:comment>
                                </xsl:if>

                                <assignedPerson classCode="PSN" determinerCode="INSTANCE">
                                    <!--A.2.r.1.1 Reporter Identifier-->
                                    <name>
                                        <xsl:call-template name="field-or-mask">
                                            <xsl:with-param name="element">prefix</xsl:with-param>
                                            <xsl:with-param name="value" select="reportertitle"/>
                                        </xsl:call-template>
                                        <xsl:comment>C.2.r.1.1:Reporter’s Title</xsl:comment>
                                        <xsl:call-template name="field-or-mask">
                                            <xsl:with-param name="element">given</xsl:with-param>
                                            <xsl:with-param name="value" select="reporterfirstname"/>
                                        </xsl:call-template>
                                        <xsl:comment>C.2.r.1.2:Reporter’s Given Name</xsl:comment>
                                        <xsl:call-template name="field-or-mask">
                                            <xsl:with-param name="element">given</xsl:with-param>
                                            <xsl:with-param name="value" select="reportermiddlename"/>
                                        </xsl:call-template>
                                        <xsl:comment>C.2.r.1.3:Reporter’s Middle Name</xsl:comment>
                                        <xsl:call-template name="field-or-mask">
                                            <xsl:with-param name="element">family</xsl:with-param>
                                            <xsl:with-param name="value" select="reporterlastname"/>
                                        </xsl:call-template>
                                        <xsl:comment>C.2.r.1.4:Reporter’s Family Name</xsl:comment>
                                    </name>

                                    <!-- C.2.r.4: Qualification -->
                                    <xsl:if test="string-length(qualification) > 0">
                                        <xsl:comment>C.2.r.4: Qualification</xsl:comment>
                                        <asQualifiedEntity classCode="QUAL">
                                            <xsl:variable name="isNullFlavourMaskQual">
                                                <xsl:call-template name="isNullFlavour">
                                                    <xsl:with-param name="value" select="qualification"/>
                                                </xsl:call-template>
                                            </xsl:variable>
                                            <xsl:choose>
                                                <xsl:when test="$isNullFlavourMaskQual = 'yes'">
                                                    <xsl:variable name="NullFlavourWOSqBrcktQual">
                                                        <xsl:call-template name="getNFValueWithoutSqBrckt">
                                                            <xsl:with-param name="nfvalue" select="qualification"/>
                                                        </xsl:call-template>
                                                    </xsl:variable>
                                                    <code nullFlavor="{$NullFlavourWOSqBrcktQual}"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <code code="{qualification}" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            <!-- <xsl:if test="string-length(krmedicalprofessionals) > 0">
                                                    <xsl:comment>C.2.r.4.KR.1: Other medical professionals</xsl:comment>
                                                    <code code="{krmedicalprofessionals}" codeSystem="{$oidKRQualification}" codeSystemVersion="1.0" DisplayName="Other medical professionals" />
                                                </xsl:if> -->
                                        </asQualifiedEntity>
                                    </xsl:if>
                                    <!--A.2.r.1.3 Reporter Country-->

                                    <xsl:if test="string-length(reportercountry) > 0">
                                        <xsl:comment>C.2.r.3: Reporter's Country Code</xsl:comment>
                                        <asLocatedEntity classCode="LOCE">
                                            <location determinerCode="INSTANCE" classCode="COUNTRY">
                                                <xsl:variable name="isNullFlavourMaskCountry">
                                                    <xsl:call-template name="isNullFlavour">
                                                        <xsl:with-param name="value" select="reportercountry"/>
                                                    </xsl:call-template>
                                                </xsl:variable>
                                                <xsl:choose>
                                                    <xsl:when test="$isNullFlavourMaskCountry = 'yes'">
                                                        <xsl:variable name="NullFlavourWOSqBrcktCountry">
                                                            <xsl:call-template name="getNFValueWithoutSqBrckt">
                                                                <xsl:with-param name="nfvalue" select="reportercountry"/>
                                                            </xsl:call-template>
                                                        </xsl:variable>
                                                        <code nullFlavor="{$NullFlavourWOSqBrcktCountry}"/>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <code code="{reportercountry}" codeSystem="{$OidISOCountry}"/>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </location>
                                        </asLocatedEntity>
                                    </xsl:if>
                                </assignedPerson>


                                <!--A.2.r.1.2.ab Reporter Organization-->
                                <xsl:if test="string-length(reporteroraganisation) + string-length(reporterdepartment) > 0">
                                    <representedOrganization classCode="ORG" determinerCode="INSTANCE">
                                        <xsl:call-template name="field-or-mask">
                                            <xsl:with-param name="element">name</xsl:with-param>
                                            <xsl:with-param name="value" select="reporterdepartment"/>
                                        </xsl:call-template>
                                        <xsl:comment>C.2.r.2.2:Reporter’s Department</xsl:comment>
                                        <!--<xsl:if test="string-length(reporteroraganisation) > 0">-->
                                        <assignedEntity classCode="ASSIGNED">
                                            <representedOrganization classCode="ORG" determinerCode="INSTANCE">
                                                <xsl:call-template name="field-or-mask">
                                                    <xsl:with-param name="element">name</xsl:with-param>
                                                    <xsl:with-param name="value" select="reporteroraganisation"/>
                                                </xsl:call-template>
                                                <xsl:comment>C.2.r.2.1:Reporter’s Organisation</xsl:comment>
                                            </representedOrganization>
                                        </assignedEntity>
                                        <!--</xsl:if>-->
                                    </representedOrganization>
                                </xsl:if>
                            </assignedEntity>
                        </author>
                    </controlActEvent>
                </subjectOf2>
            </relatedInvestigation>
        </outboundRelationship>
    </xsl:template>







    <xsl:template match="patient/summary/narrativesendercommentnative">
        <xsl:for-each select="Nativedata">
            <xsl:if test="string-length(summaryandreportercomments) > 0">
                <component typeCode="COMP">
                    <observationEvent moodCode="EVN" classCode="OBS">
                        <code code="{$SummaryAndComment}" codeSystem="{$oidObservationCode}"/>
                        <value language="{summaryandreportercommentslang}" xsi:type="ED" mediaType="text/plain">
                            <xsl:value-of select="summaryandreportercomments"/>
                        </value>
                        <author typeCode="AUT">
                            <assignedEntity classCode="ASSIGNED">
                                <code code="{$Reporter}" codeSystem="{$oidAssignedEntityRoleCode}"/>
                            </assignedEntity>
                        </author>
                    </observationEvent>
                </component>
            </xsl:if>
            <xsl:comment>H.5.1a and H.5.1b Narrative and Sendercomment in Native Languague</xsl:comment>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="sender">
        <subjectOf1 typeCode="SUBJ">
            <controlActEvent classCode="CACT" moodCode="EVN">
                <author typeCode="AUT">
                    <assignedEntity classCode="ASSIGNED">
                        <!--A.3.1	Sender Organization Type-->
                        <xsl:choose>
                            <xsl:when test="sendertype= 1">
                                <code code="{sendertype}" codeSystem="{$oidSenderType}"
                                      codeSystemVersion="{sendertypecsv}" displayName="PharmaceuticalCompany"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 2">
                                <code code="{sendertype}" codeSystem="{$oidSenderType}"
                                      codeSystemVersion="{sendertypecsv}" displayName="RegulatoryAuthority"/>
                            </xsl:when>
                            <xsl:when test="sendertype=3">
                                <code code="{sendertype}" codeSystem="{$oidSenderType}"
                                      codeSystemVersion="{sendertypecsv}" displayName="HealthProfessional"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 4">
                                <code code="{sendertype}" codeSystem="{$oidSenderType}"
                                      codeSystemVersion="{sendertypecsv}"
                                      displayName="RegionalPharmacovigilanceCentre"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 5">
                                <code code="{sendertype}" codeSystem="{$oidSenderType}"
                                      codeSystemVersion="{sendertypecsv}"
                                      displayName="WHOcollaboratingcentresforinternationaldrugmonitoring"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 6">
                                <code code="{sendertype}" codeSystem="{$oidSenderType}"
                                      codeSystemVersion="{sendertypecsv}"
                                      displayName="Other(e.g.distributororotherorganisation)"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 7">
                                <code code="{sendertype}" codeSystem="{$oidSenderType}"
                                      codeSystemVersion="{sendertypecsv}" displayName="Patient/Consumer"/>
                            </xsl:when>

                            <xsl:when test="sendertype= 01">
                                <code code="1" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="PharmaceuticalCompany"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 02">
                                <code code="2" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="RegulatoryAuthority"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 03">
                                <code code="3" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="HealthProfessional"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 04">
                                <code code="4" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="RegionalPharmacovigilanceCentre"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 05">
                                <code code="5" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="WHOcollaboratingcentresforinternationaldrugmonitoring"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 06">
                                <code code="6" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="Other(e.g.distributororotherorganisation)"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 07">
                                <code code="7" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="Patient/Consumer"/>
                            </xsl:when>

                            <xsl:when test="sendertype= 001">
                                <code code="1" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="PharmaceuticalCompany"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 002">
                                <code code="2" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="RegulatoryAuthority"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 003">
                                <code code="3" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="HealthProfessional"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 004">
                                <code code="4" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="RegionalPharmacovigilanceCentre"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 005">
                                <code code="5" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="WHOcollaboratingcentresforinternationaldrugmonitoring"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 006">
                                <code code="6" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="Other(e.g.distributororotherorganisation)"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 007">
                                <code code="7" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="Patient/Consumer"/>
                            </xsl:when>

                            <xsl:when test="sendertype= 'PHARMACEUTICAL COMPANY'">
                                <code code="1" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="PharmaceuticalCompany"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'REGULATORY AUTHORITY'">
                                <code code="2" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="RegulatoryAuthority"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'HEALTH PROFESSIONAL'">
                                <code code="3" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="HealthProfessional"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'REGIONAL PHARMACOVIGILANCE CENTRE'">
                                <code code="{$oidSenderType}" codeSystem="{$oidSenderType}"
                                      codeSystemVersion="{sendertypecsv}"
                                      displayName="RegionalPharmacovigilanceCentre"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'WHO COLLABORATING CENTRES FOR INTERNATIONAL DRUG MONITORING'">
                                <code code="5" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="WHOcollaboratingcentresforinternationaldrugmonitoring"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'OTHER'">
                                <code code="6" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="Other(e.g.distributororotherorganisation)"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'PATIENT / CONSUMER'">
                                <code code="7" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="Patient/Consumer"/>
                            </xsl:when>

                            <xsl:when test="sendertype= 'pharmaceutical company'">
                                <code code="1" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="PharmaceuticalCompany"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'regulatory authority'">
                                <code code="2" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="RegulatoryAuthority"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'health professional'">
                                <code code="3" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="HealthProfessional"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'regional pharmacovigilance centre'">
                                <code code="4" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="RegionalPharmacovigilanceCentre"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'who collaborating centres for international drug monitoring'">
                                <code code="5" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="WHOcollaboratingcentresforinternationaldrugmonitoring"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'other'">
                                <code code="6" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="Other(e.g.distributororotherorganisation)"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'patient / consumer'">
                                <code code="7" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="Patient/Consumer"/>
                            </xsl:when>

                            <xsl:when test="sendertype= 'Pharmaceutical Company'">
                                <code code="1" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="PharmaceuticalCompany"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'Regulatory Authority'">
                                <code code="2" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="RegulatoryAuthority"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'Health Professional'">
                                <code code="3" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="HealthProfessional"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'Regional Pharmacovigilance Centre'">
                                <code code="4" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="RegionalPharmacovigilanceCentre"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'WHO collaborating centres for international drug monitoring'">
                                <code code="5" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="WHOcollaboratingcentresforinternationaldrugmonitoring"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'Other'">
                                <code code="6" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="Other(e.g.distributororotherorganisation)"/>
                            </xsl:when>
                            <xsl:when test="sendertype= 'Patient / Consumer'">
                                <code code="7" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"
                                      displayName="Patient/Consumer"/>
                            </xsl:when>
                            <!--<xsl:otherwise><code code="{sendertype}" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"/></xsl:otherwise>-->
                        </xsl:choose>
                        <xsl:comment>C.3.1:SenderType</xsl:comment>
                        <xsl:comment>C.3.1.CSV:SenderTypeCodeSystemVersion</xsl:comment>
                        <!--A.3.4.abcd Sender Address-->
                        <addr>
                            <xsl:call-template name="field-or-mask">
                                <xsl:with-param name="element">streetAddressLine</xsl:with-param>
                                <xsl:with-param name="value" select="senderstreet"/>
                            </xsl:call-template>
                            <xsl:comment>C.3.4.1:Sender’s Street Address</xsl:comment>
                            <xsl:call-template name="field-or-mask">
                                <xsl:with-param name="element">city</xsl:with-param>
                                <xsl:with-param name="value" select="sendercity"/>
                            </xsl:call-template>
                            <xsl:comment>C.3.4.2:Sender’s City</xsl:comment>
                            <xsl:call-template name="field-or-mask">
                                <xsl:with-param name="element">state</xsl:with-param>
                                <xsl:with-param name="value" select="senderstate"/>
                            </xsl:call-template>
                            <xsl:comment>C.3.4.3:Sender’s State or Province</xsl:comment>
                            <xsl:call-template name="field-or-mask">
                                <xsl:with-param name="element">postalCode</xsl:with-param>
                                <xsl:with-param name="value" select="senderpostcode"/>
                            </xsl:call-template>
                            <xsl:comment>C.3.4.4:Sender’s Postcode</xsl:comment>
                        </addr>
                        <!--A.3.4.fgh Sender Telephone-->
                        <xsl:if test="string-length(sendertel) > 0">
                            <telecom>
                                <xsl:attribute name="value">
                                    <xsl:text>tel:</xsl:text>
                                    <xsl:if test="string-length(sendertelcountrycode) > 0">+<xsl:value-of
                                            select="sendertelcountrycode"/><xsl:text> </xsl:text>
                                    </xsl:if>
                                    <xsl:value-of select="sendertel"/>
                                    <xsl:if test="string-length(sendertelextension) > 0">
                                        <xsl:text> </xsl:text><xsl:value-of select="sendertelextension"/>
                                    </xsl:if>
                                </xsl:attribute>
                                <xsl:comment>C.3.4.6:Sender’s Telephone</xsl:comment>
                            </telecom>
                        </xsl:if>
                        <!--A.3.4.ijk Sender Fax-->
                        <xsl:if test="string-length(senderfax) > 0">
                            <telecom>
                                <xsl:attribute name="value">
                                    <xsl:text>fax:</xsl:text>
                                    <xsl:if test="string-length(senderfaxcountrycode) > 0">+<xsl:value-of
                                            select="senderfaxcountrycode"/><xsl:text> </xsl:text>
                                    </xsl:if>
                                    <xsl:value-of select="senderfax"/>
                                    <xsl:if test="string-length(senderfaxextension) > 0">
                                        <xsl:text> </xsl:text><xsl:value-of select="senderfaxextension"/>
                                    </xsl:if>
                                </xsl:attribute>
                                <xsl:comment>C.3.4.7:Sender’s Fax</xsl:comment>
                            </telecom>
                        </xsl:if>
                        <!--A.3.4.l Sender Email-->
                        <xsl:if test="string-length(senderemailaddress) > 0">
                            <telecom value="mailto:{senderemailaddress}"/>
                        </xsl:if>
                        <xsl:comment>C.3.4.8:Sender’s E-mail Address</xsl:comment>


                        <assignedPerson classCode="PSN" determinerCode="INSTANCE">
                            <!--A.3.3.bcde Sender Name-->
                            <name>
                                <xsl:call-template name="field-or-mask">
                                    <xsl:with-param name="element">prefix</xsl:with-param>
                                    <xsl:with-param name="value" select="sendertitle"/>
                                </xsl:call-template>
                                <xsl:comment>C.3.3.2:Sender’s Title</xsl:comment>
                                <xsl:call-template name="field-or-mask">
                                    <xsl:with-param name="element">given</xsl:with-param>
                                    <xsl:with-param name="value" select="senderfirstname"/>
                                </xsl:call-template>
                                <xsl:comment>C.3.3.3:Sender’s Given Name</xsl:comment>
                                <xsl:call-template name="field-or-mask">
                                    <xsl:with-param name="element">given</xsl:with-param>
                                    <xsl:with-param name="value" select="sendermiddlename"/>
                                </xsl:call-template>
                                <xsl:comment>C.3.3.4:Sender’s Middle Name</xsl:comment>
                                <xsl:call-template name="field-or-mask">
                                    <xsl:with-param name="element">family</xsl:with-param>
                                    <xsl:with-param name="value" select="senderlastname"/>
                                </xsl:call-template>
                                <xsl:comment>C.3.3.5:Sender’s Family Name</xsl:comment>
                            </name>
                            <!--A.3.4.e Sender Country Code-->
                            <xsl:if test="string-length(sendercountrycode)>0">
                                <asLocatedEntity classCode="LOCE">
                                    <location classCode="COUNTRY" determinerCode="INSTANCE">
                                        <xsl:choose>
                                            <xsl:when test="sendercountrycode = 'AFGHANISTAN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country1}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ALAND ISLANDS !ÅLAND ISLANDS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country2}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ALBANIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country3}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ALGERIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country4}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'AMERICAN SAMOA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country5}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ANDORRA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country6}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ANGOLA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country7}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ANGUILLA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country8}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ANTARCTICA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country9}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ANTIGUA AND BARBUDA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country10}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ARGENTINA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country11}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ARMENIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country12}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ARUBA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country13}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'AUSTRALIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country14}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'AUSTRIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country15}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'AZERBAIJAN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country16}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BAHAMAS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country17}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BAHRAIN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country18}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BANGLADESH'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country19}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BARBADOS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country20}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BELARUS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country21}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BELGIUM'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country22}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BELIZE'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country23}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BENIN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country24}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BERMUDA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country25}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BHUTAN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country26}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BOLIVIA (PLURINATIONAL STATE OF)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country27}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BONAIRE, SINT EUSTATIUS AND SABA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country28}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BOSNIA AND HERZEGOVINA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country29}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BOTSWANA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country30}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BOUVET ISLAND'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country31}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BRAZIL'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country32}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BRITISH INDIAN OCEAN TERRITORY'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country33}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BRUNEI DARUSSALAM'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country34}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BULGARIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country35}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BURKINA FASO'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country36}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'BURUNDI'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country37}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'CABO VERDE'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country38}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'CAMBODIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country39}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'CAMEROON'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country40}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'CANADA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country41}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'CAYMAN ISLANDS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country42}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'CENTRAL AFRICAN REPUBLIC'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country43}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'CHAD'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country44}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'CHILE'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country45}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'CHINA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country46}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'CHRISTMAS ISLAND'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country47}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'COCOS (KEELING) ISLANDS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country48}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'COLOMBIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country49}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'COMOROS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country50}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'CONGO'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country51}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'CONGO (DEMOCRATIC REPUBLIC OF THE)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country52}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'COOK ISLANDS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country53}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'COSTA RICA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country54}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'EUROPEAN UNION'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country250}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'CROATIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country56}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'CUBA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country57}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'CURACAO !CURAÇAO'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country58}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'CYPRUS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country59}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'CZECH REPUBLIC'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country60}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'DENMARK'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country61}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'DJIBOUTI'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country62}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'DOMINICA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country63}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'DOMINICAN REPUBLIC'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country64}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ECUADOR'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country65}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'EGYPT'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country66}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'EL SALVADOR'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country67}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'EQUATORIAL GUINEA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country68}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ERITREA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country69}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ESTONIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country70}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ETHIOPIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country71}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'FALKLAND ISLANDS (MALVINAS)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country72}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'FAROE ISLANDS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country73}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'FIJI'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country74}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'FINLAND'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country75}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'FRANCE'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country76}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'FRENCH GUIANA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country77}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'FRENCH POLYNESIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country78}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'FRENCH SOUTHERN TERRITORIES'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country79}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'GABON'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country80}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'GAMBIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country81}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'GEORGIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country82}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'GERMANY'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country83}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'GHANA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country84}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'GIBRALTAR'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country85}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'GREECE'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country86}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'GREENLAND'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country87}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'GRENADA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country88}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'GUADELOUPE'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country89}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'GUAM'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country90}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'GUATEMALA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country91}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'GUERNSEY'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country92}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'GUINEA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country93}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'GUINEA-BISSAU'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country94}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'GUYANA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country95}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'HAITI'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country96}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'HEARD ISLAND AND MCDONALD ISLANDS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country97}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'HOLY SEE'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country98}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'HONDURAS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country99}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'HONG KONG'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country100}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'HUNGARY'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country101}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ICELAND'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country102}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'INDIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country103}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'INDONESIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country104}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'IRAN (ISLAMIC REPUBLIC OF)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country105}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'IRAQ'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country106}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'IRELAND'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country107}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ISLE OF MAN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country108}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ISRAEL'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country109}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ITALY'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country110}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'JAMAICA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country111}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'JAPAN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country112}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'JERSEY'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country113}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'JORDAN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country114}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'KAZAKHSTAN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country115}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'KENYA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country116}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'KIRIBATI'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country117}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'KUWAIT'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country120}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'KYRGYZSTAN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country121}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'LATVIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country123}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'LEBANON'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country124}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'LESOTHO'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country125}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'LIBERIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country126}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'LIBYA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country127}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'LIECHTENSTEIN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country128}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'LITHUANIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country129}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'LUXEMBOURG'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country130}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MACAO'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country131}"/>
                                            </xsl:when>
                                            <xsl:when
                                                    test="sendercountrycode = 'MACEDONIA (THE FORMER YUGOSLAV REPUBLIC OF)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country132}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MADAGASCAR'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country133}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MALAWI'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country134}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MALAYSIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country135}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MALDIVES'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country136}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MALI'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country137}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MALTA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country138}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MARSHALL ISLANDS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country139}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MARTINIQUE'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country140}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MAURITANIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country141}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MAURITIUS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country142}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MAYOTTE'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country143}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MEXICO'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country144}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MICRONESIA (FEDERATED STATES OF)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country145}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MOLDOVA (REPUBLIC OF)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country146}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MONACO'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country147}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MONGOLIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country148}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MONTENEGRO'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country149}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MONTSERRAT'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country150}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MOROCCO'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country151}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MOZAMBIQUE'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country152}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'MYANMAR'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country153}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'NAMIBIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country154}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'NAURU'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country155}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'NEPAL'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country156}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'NETHERLANDS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country157}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'NEW CALEDONIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country158}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'NEW ZEALAND'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country159}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'NICARAGUA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country160}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'NIGER'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country161}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'NIGERIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country162}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'NIUE'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country163}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'NORFOLK ISLAND'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country164}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'NORTHERN MARIANA ISLANDS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country165}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'NORWAY'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country166}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'OMAN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country167}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'PAKISTAN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country168}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'PALAU'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country169}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'PALESTINE, STATE OF'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country170}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'PANAMA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country171}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'PAPUA NEW GUINEA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country172}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'PARAGUAY'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country173}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'PERU'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country174}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'PHILIPPINES'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country175}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'PITCAIRN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country176}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'POLAND'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country177}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'PORTUGAL'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country178}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'PUERTO RICO'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country179}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'QATAR'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country180}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'REUNION !RÉUNION'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country181}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ROMANIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country182}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'RUSSIAN FEDERATION'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country183}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'RWANDA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country184}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SAINT BARTHELEMY !SAINT BARTHÉLEMY'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country185}"/>
                                            </xsl:when>
                                            <xsl:when
                                                    test="sendercountrycode = 'SAINT HELENA, ASCENSION AND TRISTAN DA CUNHA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country186}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SAINT KITTS AND NEVIS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country187}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SAINT LUCIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country188}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SAINT MARTIN (FRENCH PART)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country189}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SAINT PIERRE AND MIQUELON'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country190}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SAINT VINCENT AND THE GRENADINES'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country191}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SAMOA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country192}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SAN MARINO'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country193}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SAO TOME AND PRINCIPE'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country194}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SAUDI ARABIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country195}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SENEGAL'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country196}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SERBIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country197}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SEYCHELLES'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country198}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SIERRA LEONE'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country199}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SINGAPORE'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country200}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SINT MAARTEN (DUTCH PART)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country201}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SLOVAKIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country202}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SLOVENIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country203}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SOLOMON ISLANDS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country204}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SOMALIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country205}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SOUTH AFRICA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country206}"/>
                                            </xsl:when>
                                            <xsl:when
                                                    test="sendercountrycode = 'SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country207}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SOUTH SUDAN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country208}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SPAIN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country209}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SRI LANKA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country210}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SUDAN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country211}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SURINAME'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country212}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SVALBARD AND JAN MAYEN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country213}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SWAZILAND'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country214}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SWEDEN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country215}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SWITZERLAND'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country216}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'SYRIAN ARAB REPUBLIC'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country217}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'TAIWAN, PROVINCE OF CHINA[A]'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country218}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'TAJIKISTAN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country219}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'TANZANIA, UNITED REPUBLIC OF'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country220}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'THAILAND'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country221}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'TIMOR-LESTE'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country222}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'TOGO'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country223}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'TOKELAU'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country224}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'TONGA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country225}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'TRINIDAD AND TOBAGO'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country226}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'TUNISIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country227}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'TURKEY'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country228}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'TURKMENISTAN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country229}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'TURKS AND CAICOS ISLANDS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country230}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'TUVALU'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country231}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'UGANDA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country232}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'UKRAINE'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country233}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'UNITED ARAB EMIRATES'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country234}"/>
                                            </xsl:when>
                                            <xsl:when
                                                    test="sendercountrycode = 'UNITED KINGDOM OF GREAT BRITAIN AND NORTHERN IRELAND'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country235}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'UNITED KINGDOM'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country235}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'UNITED STATES OF AMERICA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country236}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'UNITED STATES'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country236}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'UNITED STATES MINOR OUTLYING ISLANDS'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country237}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'URUGUAY'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country238}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'UZBEKISTAN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country239}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'VANUATU'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country240}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'VENEZUELA (BOLIVARIAN REPUBLIC OF)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country241}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'VIET NAM'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country242}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'VIRGIN ISLANDS (BRITISH)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country243}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'VIRGIN ISLANDS (U.S.)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country244}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'WALLIS AND FUTUNA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country245}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'WESTERN SAHARA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country246}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'YEMEN'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country247}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ZAMBIA'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country248}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ZIMBABWE'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country249}"/>
                                            </xsl:when>


                                            <xsl:when test="sendercountrycode = 'Afghanistan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country1}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Aland Islands !Åland Islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country2}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Albania'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country3}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Algeria'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country4}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'American Samoa'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country5}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Andorra'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country6}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Angola'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country7}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Anguilla'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country8}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Antarctica'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country9}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Antigua and Barbuda'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country10}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Argentina'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country11}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Armenia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country12}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Aruba'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country13}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Australia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country14}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Austria'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country15}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Azerbaijan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country16}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Bahamas'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country17}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Bahrain'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country18}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Bangladesh'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country19}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Barbados'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country20}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Belarus'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country21}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Belgium'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country22}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Belize'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country23}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Benin'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country24}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Bermuda'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country25}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Bhutan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country26}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Bolivia (Plurinational State of)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country27}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Bonaire, Sint Eustatius and Saba'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country28}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Bosnia and Herzegovina'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country29}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Botswana'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country30}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Bouvet Island'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country31}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Brazil'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country32}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'British Indian Ocean Territory'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country33}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Brunei Darussalam'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country34}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Bulgaria'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country35}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Burkina Faso'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country36}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Burundi'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country37}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Cabo Verde'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country38}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Cambodia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country39}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Cameroon'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country40}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Canada'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country41}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Cayman Islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country42}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Central African Republic'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country43}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Chad'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country44}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Chile'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country45}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'China'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country46}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Christmas Island'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country47}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Cocos (Keeling) Islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country48}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Colombia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country49}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Comoros'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country50}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Congo'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country51}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Congo (Democratic Republic of the)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country52}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Cook Islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country53}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Costa Rica'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country54}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'European Union'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country250}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Croatia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country56}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Cuba'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country57}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Curacao !Curaçao'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country58}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Cyprus'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country59}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Czech Republic'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country60}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Denmark'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country61}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Djibouti'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country62}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Dominica'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country63}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Dominican Republic'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country64}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Ecuador'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country65}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Egypt'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country66}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'El Salvador'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country67}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Equatorial Guinea'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country68}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Eritrea'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country69}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Estonia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country70}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Ethiopia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country71}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Falkland Islands (Malvinas)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country72}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Faroe Islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country73}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Fiji'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country74}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Finland'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country75}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'France'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country76}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'French Guiana'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country77}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'French Polynesia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country78}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'French Southern Territories'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country79}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Gabon'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country80}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Gambia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country81}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Georgia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country82}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Germany'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country83}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Ghana'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country84}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Gibraltar'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country85}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Greece'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country86}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Greenland'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country87}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Grenada'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country88}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Guadeloupe'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country89}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Guam'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country90}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Guatemala'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country91}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Guernsey'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country92}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Guinea'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country93}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Guinea-Bissau'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country94}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Guyana'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country95}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Haiti'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country96}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Heard Island and McDonald Islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country97}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Holy See'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country98}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Honduras'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country99}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Hong Kong'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country100}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Hungary'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country101}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Iceland'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country102}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'India'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country103}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Indonesia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country104}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Iran (Islamic Republic of)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country105}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Iraq'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country106}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Ireland'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country107}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Isle of Man'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country108}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Israel'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country109}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Italy'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country110}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Jamaica'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country111}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Japan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country112}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Jersey'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country113}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Jordan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country114}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Kazakhstan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country115}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Kenya'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country116}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Kiribati'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country117}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Kuwait'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country120}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Kyrgyzstan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country121}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Latvia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country123}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Lebanon'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country124}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Lesotho'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country125}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Liberia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country126}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Libya'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country127}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Liechtenstein'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country128}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Lithuania'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country129}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Luxembourg'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country130}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Macao'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country131}"/>
                                            </xsl:when>
                                            <xsl:when
                                                    test="sendercountrycode = 'Macedonia (the former Yugoslav Republic of)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country132}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Madagascar'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country133}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Malawi'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country134}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Malaysia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country135}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Maldives'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country136}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Mali'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country137}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Malta'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country138}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Marshall Islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country139}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Martinique'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country140}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Mauritania'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country141}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Mauritius'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country142}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Mayotte'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country143}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Mexico'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country144}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Micronesia (Federated States of)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country145}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Moldova (Republic of)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country146}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Monaco'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country147}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Mongolia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country148}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Montenegro'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country149}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Montserrat'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country150}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Morocco'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country151}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Mozambique'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country152}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Myanmar'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country153}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Namibia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country154}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Nauru'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country155}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Nepal'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country156}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Netherlands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country157}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'New Caledonia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country158}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'New Zealand'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country159}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Nicaragua'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country160}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Niger'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country161}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Nigeria'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country162}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Niue'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country163}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Norfolk Island'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country164}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Northern Mariana Islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country165}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Norway'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country166}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Oman'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country167}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Pakistan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country168}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Palau'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country169}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Palestine, State of'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country170}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Panama'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country171}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Papua New Guinea'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country172}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Paraguay'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country173}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Peru'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country174}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Philippines'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country175}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Pitcairn'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country176}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Poland'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country177}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Portugal'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country178}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Puerto Rico'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country179}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Qatar'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country180}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Reunion !Réunion'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country181}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Romania'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country182}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Russian Federation'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country183}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Rwanda'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country184}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Saint Barthelemy !Saint Barthélemy'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country185}"/>
                                            </xsl:when>
                                            <xsl:when
                                                    test="sendercountrycode = 'Saint Helena, Ascension and Tristan da Cunha'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country186}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Saint Kitts and Nevis'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country187}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Saint Lucia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country188}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Saint Martin (French part)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country189}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Saint Pierre and Miquelon'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country190}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Saint Vincent and the Grenadines'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country191}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Samoa'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country192}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'San Marino'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country193}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Sao Tome and Principe'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country194}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Saudi Arabia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country195}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Senegal'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country196}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Serbia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country197}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Seychelles'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country198}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Sierra Leone'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country199}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Singapore'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country200}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Sint Maarten (Dutch part)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country201}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Slovakia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country202}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Slovenia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country203}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Solomon Islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country204}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Somalia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country205}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'South Africa'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country206}"/>
                                            </xsl:when>
                                            <xsl:when
                                                    test="sendercountrycode = 'South Georgia and the South Sandwich Islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country207}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'South Sudan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country208}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Spain'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country209}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Sri Lanka'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country210}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Sudan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country211}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Suriname'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country212}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Svalbard and Jan Mayen'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country213}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Swaziland'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country214}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Sweden'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country215}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Switzerland'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country216}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Syrian Arab Republic'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country217}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Taiwan, Province of China[a]'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country218}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Tajikistan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country219}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Tanzania, United Republic of'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country220}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Thailand'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country221}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Timor-Leste'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country222}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Togo'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country223}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Tokelau'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country224}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Tonga'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country225}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Trinidad and Tobago'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country226}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Tunisia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country227}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Turkey'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country228}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Turkmenistan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country229}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Turks and Caicos Islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country230}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Tuvalu'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country231}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Uganda'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country232}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Ukraine'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country233}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'United Arab Emirates'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country234}"/>
                                            </xsl:when>
                                            <xsl:when
                                                    test="sendercountrycode = 'United Kingdom of Great Britain and Northern Ireland'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country235}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'United Kingdom'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country235}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'United States of America'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country236}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'United States'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country236}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'United States Minor Outlying Islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country237}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Uruguay'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country238}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Uzbekistan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country239}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Vanuatu'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country240}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Venezuela (Bolivarian Republic of)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country241}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Viet Nam'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country242}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Virgin Islands (British)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country243}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Virgin Islands (U.S.)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country244}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Wallis and Futuna'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country245}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Western Sahara'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country246}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Yemen'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country247}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Zambia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country248}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'Zimbabwe'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country249}"/>
                                            </xsl:when>

                                            <xsl:when test="sendercountrycode = 'afghanistan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country1}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'aland islands !åland islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country2}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'albania'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country3}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'algeria'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country4}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'american samoa'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country5}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'andorra'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country6}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'angola'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country7}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'anguilla'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country8}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'antarctica'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country9}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'antigua and barbuda'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country10}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'argentina'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country11}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'armenia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country12}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'aruba'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country13}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'australia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country14}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'austria'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country15}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'azerbaijan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country16}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'bahamas'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country17}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'bahrain'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country18}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'bangladesh'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country19}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'barbados'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country20}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'belarus'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country21}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'belgium'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country22}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'belize'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country23}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'benin'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country24}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'bermuda'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country25}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'bhutan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country26}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'bolivia (plurinational state of)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country27}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'bonaire, sint eustatius and saba'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country28}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'bosnia and herzegovina'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country29}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'botswana'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country30}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'bouvet island'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country31}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'brazil'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country32}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'british indian ocean territory'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country33}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'brunei darussalam'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country34}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'bulgaria'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country35}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'burkina faso'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country36}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'burundi'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country37}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'cabo verde'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country38}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'cambodia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country39}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'cameroon'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country40}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'canada'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country41}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'cayman islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country42}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'central african republic'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country43}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'chad'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country44}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'chile'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country45}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'china'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country46}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'christmas island'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country47}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'cocos (keeling) islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country48}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'colombia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country49}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'comoros'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country50}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'congo'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country51}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'congo (democratic republic of the)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country52}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'cook islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country53}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'costa rica'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country54}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'european union'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country250}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'croatia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country56}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'cuba'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country57}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'curacao !curaçao'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country58}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'cyprus'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country59}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'czech republic'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country60}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'denmark'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country61}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'djibouti'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country62}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'dominica'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country63}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'dominican republic'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country64}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ecuador'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country65}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'egypt'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country66}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'el salvador'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country67}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'equatorial guinea'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country68}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'eritrea'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country69}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'estonia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country70}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ethiopia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country71}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'falkland islands (malvinas)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country72}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'faroe islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country73}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'fiji'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country74}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'finland'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country75}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'france'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country76}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'french guiana'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country77}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'french polynesia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country78}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'french southern territories'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country79}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'gabon'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country80}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'gambia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country81}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'georgia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country82}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'germany'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country83}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ghana'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country84}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'gibraltar'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country85}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'greece'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country86}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'greenland'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country87}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'grenada'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country88}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'guadeloupe'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country89}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'guam'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country90}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'guatemala'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country91}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'guernsey'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country92}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'guinea'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country93}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'guinea-bissau'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country94}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'guyana'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country95}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'haiti'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country96}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'heard island and mcdonald islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country97}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'holy see'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country98}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'honduras'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country99}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'hong kong'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country100}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'hungary'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country101}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'iceland'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country102}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'india'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country103}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'indonesia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country104}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'iran (islamic republic of)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country105}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'iraq'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country106}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ireland'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country107}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'isle of man'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country108}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'israel'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country109}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'italy'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country110}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'jamaica'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country111}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'japan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country112}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'jersey'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country113}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'jordan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country114}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'kazakhstan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country115}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'kenya'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country116}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'kiribati'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country117}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'kuwait'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country120}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'kyrgyzstan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country121}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'latvia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country123}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'lebanon'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country124}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'lesotho'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country125}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'liberia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country126}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'libya'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country127}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'liechtenstein'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country128}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'lithuania'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country129}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'luxembourg'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country130}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'macao'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country131}"/>
                                            </xsl:when>
                                            <xsl:when
                                                    test="sendercountrycode = 'macedonia (the former yugoslav republic of)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country132}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'madagascar'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country133}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'malawi'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country134}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'malaysia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country135}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'maldives'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country136}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'mali'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country137}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'malta'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country138}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'marshall islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country139}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'martinique'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country140}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'mauritania'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country141}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'mauritius'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country142}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'mayotte'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country143}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'mexico'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country144}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'micronesia (federated states of)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country145}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'moldova (republic of)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country146}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'monaco'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country147}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'mongolia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country148}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'montenegro'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country149}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'montserrat'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country150}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'morocco'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country151}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'mozambique'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country152}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'myanmar'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country153}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'namibia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country154}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'nauru'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country155}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'nepal'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country156}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'netherlands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country157}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'new caledonia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country158}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'new zealand'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country159}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'nicaragua'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country160}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'niger'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country161}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'nigeria'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country162}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'niue'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country163}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'norfolk island'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country164}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'northern mariana islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country165}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'norway'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country166}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'oman'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country167}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'pakistan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country168}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'palau'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country169}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'palestine, state of'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country170}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'panama'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country171}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'papua new guinea'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country172}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'paraguay'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country173}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'peru'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country174}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'philippines'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country175}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'pitcairn'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country176}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'poland'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country177}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'portugal'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country178}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'puerto rico'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country179}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'qatar'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country180}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'reunion !réunion'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country181}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'romania'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country182}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'russian federation'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country183}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'rwanda'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country184}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'saint barthelemy !saint barthélemy'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country185}"/>
                                            </xsl:when>
                                            <xsl:when
                                                    test="sendercountrycode = 'saint helena, ascension and tristan da cunha'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country186}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'saint kitts and nevis'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country187}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'saint lucia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country188}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'saint martin (french part)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country189}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'saint pierre and miquelon'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country190}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'saint vincent and the grenadines'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country191}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'samoa'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country192}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'san marino'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country193}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'sao tome and principe'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country194}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'saudi arabia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country195}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'senegal'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country196}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'serbia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country197}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'seychelles'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country198}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'sierra leone'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country199}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'singapore'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country200}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'sint maarten (dutch part)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country201}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'slovakia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country202}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'slovenia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country203}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'solomon islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country204}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'somalia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country205}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'south africa'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country206}"/>
                                            </xsl:when>
                                            <xsl:when
                                                    test="sendercountrycode = 'south georgia and the south sandwich islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country207}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'south sudan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country208}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'spain'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country209}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'sri lanka'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country210}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'sudan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country211}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'suriname'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country212}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'svalbard and jan mayen'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country213}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'swaziland'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country214}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'sweden'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country215}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'switzerland'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country216}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'syrian arab republic'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country217}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'taiwan, province of china[a]'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country218}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'tajikistan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country219}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'tanzania, united republic of'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country220}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'thailand'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country221}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'timor-leste'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country222}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'togo'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country223}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'tokelau'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country224}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'tonga'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country225}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'trinidad and tobago'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country226}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'tunisia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country227}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'turkey'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country228}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'turkmenistan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country229}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'turks and caicos islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country230}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'tuvalu'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country231}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'uganda'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country232}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'ukraine'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country233}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'united arab emirates'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country234}"/>
                                            </xsl:when>
                                            <xsl:when
                                                    test="sendercountrycode = 'united kingdom of great britain and northern ireland'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country235}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'united kingdom'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country235}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'united states of america'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country236}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'united states'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country236}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'united states minor outlying islands'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country237}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'uruguay'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country238}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'uzbekistan'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country239}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'vanuatu'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country240}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'venezuela (bolivarian republic of)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country241}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'viet nam'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country242}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'virgin islands (british)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country243}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'virgin islands (u.s.)'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country244}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'wallis and futuna'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country245}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'western sahara'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country246}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'yemen'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country247}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'zambia'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country248}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode = 'zimbabwe'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country249}"/>
                                            </xsl:when>

                                            <xsl:when test="sendercountrycode= 'af'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country1}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ax'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country2}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'al'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country3}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'dz'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country4}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'as'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country5}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ad'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country6}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ao'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country7}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ai'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country8}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'aq'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country9}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ag'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country10}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ar'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country11}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'am'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country12}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'aw'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country13}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'au'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country14}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'at'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country15}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'az'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country16}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'bs'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country17}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'bh'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country18}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'bd'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country19}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'bb'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country20}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'by'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country21}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'be'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country22}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'bz'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country23}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'bj'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country24}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'bm'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country25}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'bt'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country26}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'bo'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country27}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'bq'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country28}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ba'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country29}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'bw'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country30}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'bv'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country31}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'br'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country32}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'io'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country33}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'bn'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country34}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'bg'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country35}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'bf'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country36}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'bi'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country37}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'cv'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country38}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'kh'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country39}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'cm'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country40}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ca'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country41}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ky'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country42}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'cf'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country43}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'td'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country44}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'cl'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country45}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'cn'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country46}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'cx'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country47}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'cc'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country48}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'co'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country49}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'km'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country50}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'cg'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country51}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'cd'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country52}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ck'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country53}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'cr'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country54}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ci'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country55}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'hr'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country56}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'cu'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country57}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'cw'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country58}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'cy'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country59}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'cz'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country60}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'dk'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country61}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'dj'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country62}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'dm'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country63}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'do'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country64}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ec'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country65}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'eg'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country66}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'sv'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country67}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'gq'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country68}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'er'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country69}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ee'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country70}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'et'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country71}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'fk'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country72}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'fo'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country73}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'fj'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country74}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'fi'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country75}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'fr'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country76}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'gf'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country77}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'pf'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country78}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'tf'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country79}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ga'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country80}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'gm'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country81}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ge'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country82}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'de'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country83}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'gh'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country84}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'gi'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country85}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'gr'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country86}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'gl'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country87}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'gd'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country88}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'gp'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country89}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'gu'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country90}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'gt'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country91}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'gg'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country92}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'gn'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country93}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'gw'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country94}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'gy'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country95}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ht'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country96}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'hm'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country97}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'va'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country98}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'hn'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country99}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'hk'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country100}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'hu'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country101}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'is'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country102}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'in'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country103}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'id'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country104}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ir'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country105}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'iq'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country106}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ie'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country107}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'im'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country108}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'il'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country109}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'it'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country110}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'jm'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country111}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'jp'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country112}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'je'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country113}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'jo'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country114}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'kz'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country115}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ke'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country116}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ki'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country117}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'kp'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country118}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'kr'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country119}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'kw'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country120}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'kg'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country121}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'la'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country122}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'lv'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country123}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'lb'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country124}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ls'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country125}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'lr'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country126}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ly'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country127}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'li'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country128}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'lt'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country129}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'lu'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country130}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'mo'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country131}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'mk'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country132}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'mg'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country133}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'mw'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country134}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'my'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country135}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'mv'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country136}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ml'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country137}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'mt'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country138}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'mh'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country139}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'mq'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country140}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'mr'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country141}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'mu'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country142}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'yt'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country143}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'mx'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country144}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'fm'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country145}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'md'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country146}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'mc'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country147}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'mn'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country148}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'me'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country149}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ms'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country150}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ma'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country151}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'mz'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country152}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'mm'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country153}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'na'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country154}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'nr'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country155}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'np'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country156}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'nl'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country157}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'nc'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country158}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'nz'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country159}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ni'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country160}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ne'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country161}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ng'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country162}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'nu'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country163}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'nf'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country164}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'mp'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country165}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'no'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country166}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'om'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country167}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'pk'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country168}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'pw'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country169}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ps'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country170}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'pa'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country171}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'pg'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country172}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'py'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country173}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'pe'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country174}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ph'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country175}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'pn'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country176}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'pl'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country177}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'pt'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country178}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'pr'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country179}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'qa'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country180}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 're'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country181}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ro'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country182}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ru'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country183}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'rw'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country184}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'bl'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country185}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'sh'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country186}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'kn'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country187}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'lc'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country188}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'mf'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country189}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'pm'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country190}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'vc'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country191}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ws'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country192}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'sm'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country193}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'st'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country194}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'sa'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country195}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'sn'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country196}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'rs'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country197}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'sc'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country198}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'sl'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country199}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'sg'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country200}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'sx'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country201}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'sk'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country202}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'si'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country203}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'sb'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country204}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'so'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country205}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'za'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country206}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'gs'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country207}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ss'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country208}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'es'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country209}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'lk'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country210}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'sd'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country211}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'sr'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country212}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'sj'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country213}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'sz'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country214}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'se'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country215}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ch'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country216}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'sy'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country217}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'tw'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country218}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'tj'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country219}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'tz'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country220}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'th'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country221}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'tl'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country222}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'tg'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country223}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'tk'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country224}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'to'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country225}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'tt'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country226}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'tn'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country227}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'tr'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country228}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'tm'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country229}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'tc'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country230}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'tv'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country231}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ug'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country232}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ua'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country233}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ae'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country234}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'gb'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country235}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'us'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country236}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'um'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country237}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'uy'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country238}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'uz'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country239}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'vu'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country240}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 've'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country241}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'vn'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country242}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'vg'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country243}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'vi'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country244}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'wf'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country245}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'eh'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country246}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'ye'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country247}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'zm'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country248}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'zw'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country249}"/>
                                            </xsl:when>
                                            <xsl:when test="sendercountrycode= 'eu'">
                                                <code codeSystem="{$oidISOCountry}" code="{$country250}"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <code code="{sendercountrycode}" codeSystem="{$oidISOCountry}"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <xsl:comment>C.3.4.5:Sender’sCountryCode</xsl:comment>
                                    </location>
                                </asLocatedEntity>
                            </xsl:if>
                        </assignedPerson>
                        <!--A.3.2 Sender Organization-->
                        <!--A.3.3.a Sender Department-->
                        <representedOrganization classCode="ORG" determinerCode="INSTANCE">
                            <xsl:call-template name="field-or-mask">
                                <xsl:with-param name="element">name</xsl:with-param>
                                <xsl:with-param name="value" select="senderdepartment"/>
                            </xsl:call-template>
                            <xsl:comment>C.3.3.1:Sender’s Department</xsl:comment>
                            <assignedEntity classCode="ASSIGNED">
                                <representedOrganization classCode="ORG" determinerCode="INSTANCE">
                                    <xsl:call-template name="field-or-mask">
                                        <xsl:with-param name="element">name</xsl:with-param>
                                        <xsl:with-param name="value" select="senderorganization"/>
                                    </xsl:call-template>
                                    <xsl:if test="senderorganization = 'PRIVACY'">
                                        <name nullFlavor="MSK"/>
                                    </xsl:if>
                                    <xsl:comment>C.3.2:Sender’s Organisation</xsl:comment>
                                </representedOrganization>
                            </assignedEntity>
                        </representedOrganization>
                    </assignedEntity>
                </author>

            </controlActEvent>
        </subjectOf1>
    </xsl:template>

    <!--Parent (Identification) :
E2B(R2): element "parent"
E2B(R3): element "role"	E2B(R2): element "parentmedicalhistoryepisode"
E2B(R3): element "role"  -->

    <!--PARENT-->
    <!-- D.10 FOR A PARENT-CHILD / FOETUS REPORT, INFORMATION CONCERNING THE PARENT
	E2B(R2): element "parent" - "ichicsr\ichicsrbatchheader\ichicsrmessageheader\safetyreport\patient\parent"
	E2B(R3): element "role"
	-->
    <xsl:template match="parent" mode="identification">
        <xsl:comment>D.10: FOR A PARENT-CHILD / FOETUS REPORT, INFORMATION CONCERNING THE PARENT</xsl:comment>
        <role classCode="PRS">
            <code code="{$Parent}" codeSystem="2.16.840.1.113883.5.111"/>
            <xsl:choose>
                <xsl:when
                        test="string-length(parentidentification) > 0 or string-length(parentsex) > 0 or string-length(parentbirthdate) > 0">
                    <associatedPerson determinerCode="INSTANCE" classCode="PSN">
                        <!-- D.10.1 Parent Identification -->
                        <xsl:if test="string-length(parentidentification) > 0">
                            <xsl:comment>D.10.1: Parent Identification</xsl:comment>
                            <xsl:call-template name="field-or-mask">
                                <xsl:with-param name="element">name</xsl:with-param>
                                <xsl:with-param name="value" select="parentidentification"/>
                            </xsl:call-template>
                        </xsl:if>

                        <!-- D.10.6 Sex of Parent -->
                        <xsl:if test="string-length(parentsex) > 0">
                            <xsl:comment>D.10.6: Sex of Parent</xsl:comment>
                            <xsl:call-template name="gender">
                                <xsl:with-param name="value" select="parentsex"/>
                            </xsl:call-template>
                        </xsl:if>

                        <!-- D.10.2.1 Date of Birth of Parent -->
                        <xsl:if test="string-length(parentbirthdate) > 0">
                            <xsl:comment>D.10.2.1: Date of Birth of Parent</xsl:comment>
                            <xsl:call-template name="attribute-value-or-mask">
                                <xsl:with-param name="element">birthTime</xsl:with-param>
                                <xsl:with-param name="value" select="parentbirthdate"/>
                            </xsl:call-template>
                        </xsl:if>

                    </associatedPerson>
                </xsl:when>
                <xsl:otherwise>
                    <associatedPerson determinerCode="INSTANCE" classCode="PSN"/>
                </xsl:otherwise>
            </xsl:choose>

            <!-- D.10.2.2 Age of Parent -->
            <xsl:if test="string-length(parentage) > 0">
                <subjectOf2 typeCode="SBJ">
                    <observation moodCode="EVN" classCode="OBS">
                        <code code="{$Age}" codeSystem="{$oidObservationCode}"
                              codeSystemVersion="{$emaObservationCLVersion}"/>
                        <xsl:comment>D.10.2.2a: Age of Parent (number)</xsl:comment>
                        <xsl:comment>D.10.2.2b: Age of Parent (unit)</xsl:comment>
                        <value xsi:type="PQ" value="{parentage}" unit="{parentageunit}"/>
                        <!-- 	<xsl:attribute name="unit">
			<xsl:call-template name="getMapping">
			<xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="parentageunit"/>
			</xsl:call-template>
			</xsl:attribute> -->
                    </observation>
                </subjectOf2>
            </xsl:if>

            <!-- D.10.4 Body Weight (kg) of Parent -->
            <xsl:if test="string-length(parentweight) > 0">
                <xsl:comment>D.10.4: Body Weight (kg) of Parent</xsl:comment>
                <subjectOf2 typeCode="SBJ">
                    <observation moodCode="EVN" classCode="OBS">
                        <code code="{$BodyWeight}" codeSystem="{$oidObservationCode}"
                              codeSystemVersion="{$emaObservationCLVersion}"/>
                        <value xsi:type="PQ" value="{parentweight}" unit="kg"/>
                    </observation>
                </subjectOf2>
            </xsl:if>

            <!-- D.10.5 Height (cm) of Parent -->
            <xsl:if test="string-length(parentheight) > 0">
                <xsl:comment>D.10.5: Height (cm) of Parent</xsl:comment>
                <subjectOf2 typeCode="SBJ">
                    <observation moodCode="EVN" classCode="OBS">
                        <code code="{$Height}" codeSystem="{$oidObservationCode}"
                              codeSystemVersion="{$emaObservationCLVersion}"/>
                        <value xsi:type="PQ" value="{parentheight}" unit="cm"/>
                    </observation>
                </subjectOf2>
            </xsl:if>

            <!-- D.10.3 Last Menstrual Period Date of Parent -->
            <xsl:if test="string-length(parentlastmenstrualdate) > 0">
                <xsl:comment>D.10.3: Last Menstrual Period Date of Parent</xsl:comment>
                <xsl:variable name="isNullFlavourParLMP">
                    <xsl:call-template name="isNullFlavour">
                        <xsl:with-param name="value" select="parentlastmenstrualdate"/>
                    </xsl:call-template>
                </xsl:variable>
                <subjectOf2 typeCode="SBJ">
                    <observation moodCode="EVN" classCode="OBS">
                        <code code="{$LastMenstrualPeriodDate}" codeSystem="{$oidObservationCode}"
                              codeSystemVersion="{$emaObservationCLVersion}"/>
                        <xsl:choose>
                            <xsl:when test="$isNullFlavourParLMP = 'yes'">
                                <xsl:variable name="NullFlavourWOSqBrcktD103">
                                    <xsl:call-template name="getNFValueWithoutSqBrckt">
                                        <xsl:with-param name="nfvalue" select="parentlastmenstrualdate"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <value xsi:type="TS" nullFlavor="{$NullFlavourWOSqBrcktD103}"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <value xsi:type="TS" value="{parentlastmenstrualdate}"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </observation>
                </subjectOf2>
            </xsl:if>


            <!-- D.10.7 Relevant Medical History and Concurrent Conditions of Parent -->
            <xsl:if test="parentmedicalhistoryepisode | parentmedicalrelevanttext">
                <xsl:comment>D.10.7: Relevant Medical History and Concurrent Conditions of Parent</xsl:comment>
                <subjectOf2 typeCode="SBJ">
                    <organizer classCode="CATEGORY" moodCode="EVN">
                        <code code="{$RelevantMedicalHistoryAndConcurrentConditions}"
                              codeSystem="{$oidValueGroupingCode}" codeSystemVersion="{$emaValueGroupingCLVersion}"/>


                        <!-- D.10.7.1.r Structured Information of Parent (repeat as necessary) -->
                        <xsl:if test="string-length(parentmedicalhistoryepisode) > 0">
                            <xsl:apply-templates select="parentmedicalhistoryepisode" mode="EMA-par-structured-info"/>
                        </xsl:if>
                        <!-- D.10.7.2 Text for Relevant Medical History and Concurrent Conditions of Parent -->
                        <xsl:if test="string-length(parentmedicalrelevanttext) > 0">
                            <xsl:comment>D.10.7.2: Text for relevant medical history and concurrent conditions of parent
                                (not including reaction/event)
                            </xsl:comment>
                            <component typeCode="COMP">
                                <observation moodCode="EVN" classCode="OBS">
                                    <code code="{$HistoryAndConcurrentConditionText}" codeSystem="{$oidObservationCode}"
                                          codeSystemVersion="{$emaObservationCLVersion}"/>
                                    <value xsi:type="ED" mediaType="text/plain">
                                        <xsl:value-of select="parentmedicalrelevanttext"/>
                                    </value>
                                </observation>
                            </component>
                        </xsl:if>

                    </organizer>
                </subjectOf2>
            </xsl:if>

            <!--Patient Past Medical History-->
            <!--B.1.10.8.r Past Drug Therapy-->


            <!-- D.10.8.r Relevant Past Drug History of Parent (repeat as necessary) -->
            <xsl:if test="parentpastdrugtherapy">
                <subjectOf2 typeCode="SBJ">
                    <organizer classCode="CATEGORY" moodCode="EVN">
                        <code code="{$DrugHistory}" codeSystem="{$oidValueGroupingCode}"
                              codeSystemVersion="{$emaValueGroupingCLVersion}"/>
                        <xsl:apply-templates select="parentpastdrugtherapy" mode="EMA-par-past-drug-hist"/>
                    </organizer>
                </subjectOf2>
            </xsl:if>

        </role>
    </xsl:template>



    <xsl:template match="parentpastdrugtherapy" mode="EMA-par-past-drug-hist">
        <xsl:variable name="positionParDrugHist">
            <xsl:value-of select="position()"/>
        </xsl:variable>
        <xsl:comment>D.10.8.r: Relevant Past Drug History of Parent - (<xsl:value-of select="$positionParDrugHist"/>)
        </xsl:comment>
        <component typeCode="COMP">
            <substanceAdministration moodCode="EVN" classCode="SBADM">

                <xsl:if test="string-length(parentdrugstartdate) > 0 or string-length(parentdrugenddate) > 0">
                    <effectiveTime xsi:type="IVL_TS">
                        <!--D.10.8.r.4 Start Date-->
                        <xsl:if test="string-length(parentdrugstartdate) > 0">
                            <xsl:comment>D.10.8.r.4: Start Date</xsl:comment>
                            <xsl:call-template name="effectiveTime">
                                <xsl:with-param name="element">low</xsl:with-param>
                                <xsl:with-param name="value" select="parentdrugstartdate"/>
                            </xsl:call-template>
                        </xsl:if>

                        <!--D.10.8.r.5 End Date-->
                        <xsl:if test="string-length(parentdrugenddate) > 0">
                            <xsl:comment>D.10.8.r.5: End Date</xsl:comment>
                            <xsl:call-template name="effectiveTime">
                                <xsl:with-param name="element">high</xsl:with-param>
                                <xsl:with-param name="value" select="parentdrugenddate"/>
                            </xsl:call-template>
                        </xsl:if>
                    </effectiveTime>
                </xsl:if>

                <!--D.10.8.r.1 Name of Drug as Reported-->
                <xsl:if test="string-length(parentpastdrug) > 0">
                    <xsl:variable name="isNullFlavourParDrugName">
                        <xsl:call-template name="isNullFlavour">
                            <xsl:with-param name="value" select="parentpastdrug"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <consumable typeCode="CSM">
                        <instanceOfKind classCode="INST">
                            <kindOfProduct classCode="MMAT" determinerCode="KIND">
                                <xsl:choose>
                                    <xsl:when test="$isNullFlavourParDrugName = 'yes'">
                                        <xsl:variable name="NullFlavourWOSqBrcktParDrugName">
                                            <xsl:call-template name="getNFValueWithoutSqBrckt">
                                                <xsl:with-param name="nfvalue" select="parentpastdrug"/>
                                            </xsl:call-template>
                                        </xsl:variable>
                                        <xsl:comment>D.10.8.r.1: Name of Drug as Reported-</xsl:comment>
                                        <name nullFlavor="{$NullFlavourWOSqBrcktParDrugName}"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:if test="string-length(parentmpidversion) > 0 and string-length(parentmpid) > 0">
                                            <xsl:comment>D.10.8.r.2a: MPID Version Date / Number</xsl:comment>
                                            <xsl:comment>D.10.8.r.2b: Medicinal Product Identifier (MPID)</xsl:comment>
                                            <code code="{parentmpid}" codeSystem="MPID"
                                                  codeSystemVersion="{parentmpidversion}"/>
                                        </xsl:if>
                                        <xsl:if test="string-length(parentphpidversion) > 0 and string-length(parentphpid) > 0">
                                            <xsl:comment>D.10.8.r.3a: PhPID Version Date/Number</xsl:comment>
                                            <xsl:comment>D.10.8.r.3b: Pharmaceutical Product Identifier (PhPID)
                                            </xsl:comment>
                                            <code code="{parentphpid}" codeSystem="PhPID"
                                                  codeSystemVersion="{parentphpidversion}"/>
                                        </xsl:if>
                                        <name>
                                            <xsl:comment>D.10.8.r.1: Name of Drug as Reported-</xsl:comment>
                                            <xsl:value-of select="parentpastdrug"/>
                                        </name>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </kindOfProduct>
                        </instanceOfKind>
                    </consumable>
                </xsl:if>

                <!-- D.10.8.r.6a: MedDRA Version for Indication -->
                <!-- D.10.8.r.6b: Indication (MedDRA code)-->
                <xsl:if test="string-length(parentdrgindication) > 0">
                    <xsl:comment>D.10.8.r.6a: MedDRA Version for Indication</xsl:comment>
                    <xsl:comment>D.10.8.r.6b: Indication (MedDRA code)</xsl:comment>
                    <outboundRelationship2 typeCode="RSON">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="{$Indication}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$emaObservationCLVersion}" displayName="Parent Past indication"/>
                            <value xsi:type="CE" code="{parentdrgindication}"
                                   codeSystemVersion="{parentdrgindicationmeddraversion}" codeSystem="{$oidMedDRA}"/>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <!-- D.10.8.r.7a MedDRA Version for Reaction -->
                <!-- D.10.8.r.7b Reactions (MedDRA code) -->
                <xsl:if test="string-length(parentdrgreaction) > 0">
                    <xsl:comment>D.10.8.r.7a: MedDRA Version for Reaction</xsl:comment>
                    <xsl:comment>D.10.8.r.7b: Reactions (MedDRA code)</xsl:comment>
                    <outboundRelationship2 typeCode="CAUS">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="{$Reaction}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$emaObservationCLVersion}" displayName="Parent Past Reaction"/>
                            <value xsi:type="CE" code="{parentdrgreaction}"
                                   codeSystemVersion="{parentdrgreactionmeddraversion}" codeSystem="{$oidMedDRA}"/>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

            </substanceAdministration>
        </component>
    </xsl:template>


    <xsl:template match="patient" mode="characteristics">
        <!--B.1.2.2.ab Age at time of onset of reaction/event - Rule COD-10-->
        <xsl:if test="string-length(patientonsetage) > 0">
            <subjectOf2 typeCode="SBJ">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$Age}" codeSystem="{$oidObservationCode}" displayName="age"/>
                    <value xsi:type="PQ" value="{patientonsetage}" unit="{patientonsetageunit}"/>
                    <!-- <xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="patientonsetageunit"/></xsl:call-template></xsl:attribute>
</value> -->
                    <xsl:comment>D.2.2a: Age at Time of Onset of Reaction / Event (number)</xsl:comment>
                    <xsl:comment>D.2.2b: Age at Time of Onset of Reaction / Event (unit)</xsl:comment>
                </observation>
            </subjectOf2>
        </xsl:if>

        <!--B.1.2.2.1.ab Gestation Period-->
        <xsl:if test="string-length(gestationperiod) > 0">
            <subjectOf2 typeCode="SBJ">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$GestationPeriod}" codeSystem="{$oidObservationCode}" displayName="gestationPeriod"/>
                    <value xsi:type="PQ" value="{gestationperiod}" unit="{gestationperiodunit}"/>
                    <!--<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="gestationperiodunit"/></xsl:call-template></xsl:attribute>
</value> -->
                    <xsl:comment>D.2.2.1a: Gestation Period When Reaction / Event Was Observed in the Foetus (number)
                    </xsl:comment>
                    <xsl:comment>D.2.2.1b: Gestation Period When Reaction / Event Was Observed in the Foetus (unit)
                    </xsl:comment>
                </observation>
            </subjectOf2>
        </xsl:if>
        <!--B.1.2.3. Age Group-->
        <xsl:if test="string-length(patientagegroup)>0">
            <subjectOf2 typeCode="SBJ">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$AgeGroup}" codeSystem="{$oidObservationCode}" displayName="ageGroup"/>
                    <xsl:choose>
                        <xsl:when test="(patientagegroup)= 0">
                            <value xsi:type="CE" code="{patientagegroup}" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Foetus"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 1">
                            <value xsi:type="CE" code="{patientagegroup}" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}"
                                   displayName="Neonate(PretermandTermnewborns)"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 2">
                            <value xsi:type="CE" code="{patientagegroup}" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Infant"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)=3">
                            <value xsi:type="CE" code="{patientagegroup}" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Child"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 4">
                            <value xsi:type="CE" code="{patientagegroup}" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Adolescent"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 5">
                            <value xsi:type="CE" code="{patientagegroup}" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Adult"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 6">
                            <value xsi:type="CE" code="{patientagegroup}" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Elderly"/>
                        </xsl:when>

                        <xsl:when test="(patientagegroup)= 01">
                            <value xsi:type="CE" code="1" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}"
                                   displayName="Neonate(PretermandTermnewborns)"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 02">
                            <value xsi:type="CE" code="2" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Infant"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 03">
                            <value xsi:type="CE" code="3" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Child"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 04">
                            <value xsi:type="CE" code="4" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Adolescent"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 05">
                            <value xsi:type="CE" code="5" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Adult"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 06">
                            <value xsi:type="CE" code="6" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Elderly"/>
                        </xsl:when>

                        <xsl:when test="(patientagegroup)= 001">
                            <value xsi:type="CE" code="1" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}"
                                   displayName="Neonate(PretermandTermnewborns)"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 002">
                            <value xsi:type="CE" code="2" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Infant"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 003">
                            <value xsi:type="CE" code="3" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Child"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 004">
                            <value xsi:type="CE" code="4" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Adolescent"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 005">
                            <value xsi:type="CE" code="5" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Adult"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 006">
                            <value xsi:type="CE" code="6" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Elderly"/>
                        </xsl:when>

                        <xsl:when test="(patientagegroup)= 'Foetus'">
                            <value xsi:type="CE" code="0" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Foetus"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'Neonate (Preterm and Term newborns)'">
                            <value xsi:type="CE" code="1" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}"
                                   displayName="Neonate(PretermandTermnewborns)"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'Infant'">
                            <value xsi:type="CE" code="2" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Infant"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'Child'">
                            <value xsi:type="CE" code="3" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Child"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'Adolescent'">
                            <value xsi:type="CE" code="4" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Adolescent"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'Adult'">
                            <value xsi:type="CE" code="5" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Adult"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'Elderly'">
                            <value xsi:type="CE" code="6" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Elderly"/>
                        </xsl:when>

                        <xsl:when test="(patientagegroup)= 'FOETUS'">
                            <value xsi:type="CE" code="0" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Foetus"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'NEONATE (PRETERM AND TERM NEWBORNS)'">
                            <value xsi:type="CE" code="1" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}"
                                   displayName="Neonate(PretermandTermnewborns)"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'INFANT'">
                            <value xsi:type="CE" code="2" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Infant"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'CHILD'">
                            <value xsi:type="CE" code="3" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Child"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'ADOLESCENT'">
                            <value xsi:type="CE" code="4" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Adolescent"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'ADULT'">
                            <value xsi:type="CE" code="5" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Adult"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'ELDERLY'">
                            <value xsi:type="CE" code="6" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Elderly"/>
                        </xsl:when>

                        <xsl:when test="(patientagegroup)= 'foetus'">
                            <value xsi:type="CE" code="0" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Foetus"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'neonate (preterm and term newborns)'">
                            <value xsi:type="CE" code="1" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}"
                                   displayName="Neonate(PretermandTermnewborns)"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'infant'">
                            <value xsi:type="CE" code="2" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Infant"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'child'">
                            <value xsi:type="CE" code="3" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Child"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'adolescent'">
                            <value xsi:type="CE" code="4" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Adolescent"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'adult'">
                            <value xsi:type="CE" code="5" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Adult"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'elderly'">
                            <value xsi:type="CE" code="6" codeSystem="{$oidAgeGroup}"
                                   codeSystemVersion="{patientagegroupcsv}" displayName="Elderly"/>
                        </xsl:when>

                        <xsl:when test="(patientagegroup)= 'MSK'">
                            <value xsi:type="CE" nullFlavor="MSK" displayName="DataMasked"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'UNK'">
                            <value xsi:type="CE" nullFlavor="UNK" displayName="DataUnknowntoSender"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'ASKU'">
                            <value xsi:type="CE" nullFlavor="ASKU" displayName="DataAskedbutUnknowntoSender"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'NI'">
                            <value xsi:type="CE" nullFlavor="NI" displayName="NoInformationAvailablewithSender"/>
                        </xsl:when>

                        <xsl:when test="(patientagegroup)= 'msk'">
                            <value xsi:type="CE" nullFlavor="MSK" displayName="DataMasked"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'unk'">
                            <value xsi:type="CE" nullFlavor="UNK" displayName="DataUnknowntoSender"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'asku'">
                            <value xsi:type="CE" nullFlavor="ASKU" displayName="DataAskedbutUnknowntoSender"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'ni'">
                            <value xsi:type="CE" nullFlavor="NI" displayName="NoInformationAvailablewithSender"/>
                        </xsl:when>

                        <xsl:when test="(patientagegroup)= 'Msk'">
                            <value xsi:type="CE" nullFlavor="MSK" displayName="DataMasked"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'Unk'">
                            <value xsi:type="CE" nullFlavor="UNK" displayName="DataUnknowntoSender"/>
                        </xsl:when>
                        <xsl:when test="(patientagegroup)= 'Asku'">
                            <value xsi:type="CE" nullFlavor="ASKU" displayName="DataAskedbutUnknowntoSender"/>
                        </xsl:when>

                    </xsl:choose>
                    <xsl:comment>D.2.3:PatientAgeGroup(asperreporter)</xsl:comment>
                    <xsl:comment>D.2.3[Ver]:ICHCodeListVersionforPatientAgeGroup(asperreporter)</xsl:comment>
                </observation>
            </subjectOf2>
        </xsl:if>
        <!--B.1.3. Body Weight-->
        <xsl:if test="string-length(patientweight) > 0">
            <subjectOf2 typeCode="SBJ">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$BodyWeight}" codeSystem="{$oidObservationCode}" displayName="bodyWeight"/>
                    <value xsi:type="PQ" value="{patientweight}" unit="kg"/>
                    <xsl:comment>D.3: Body Weight (kg)</xsl:comment>
                </observation>
            </subjectOf2>
        </xsl:if>

        <!-- D.CN.1 : Ethnic Group -->
        <xsl:if test="string-length(patientminority) > 0">
            <subjectOf2 typeCode="SBJ">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$PatientMinority}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$nmpaoidC1CN1LVersion}" displayName="nation"/>
                    <value xsi:type="TS" value="{patientminority}" />
                    <xsl:comment>D.CN.1: nation</xsl:comment>
                </observation>
            </subjectOf2>
        </xsl:if>

        <!-- D.CN.2 Race -->
        <xsl:if test="string-length(patientrace) > 0">
            <subjectOf2 typeCode="SBJ">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$PatienTrace}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$nmpaoidC1CN1LVersion}" displayName="race"/>
                    <value xsi:type="TS" value="{patientrace}" />
                    <xsl:comment>D.CN.2: race</xsl:comment>
                </observation>
            </subjectOf2>
        </xsl:if>

        <!-- D.CN.3 Name of medical institution/operating company -->
        <xsl:if test="string-length(medicalinstitutionname) > 0">
            <subjectOf2 typeCode="SBJ">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$MedicalInstitutionName}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$nmpaoidC1CN1LVersion}" displayName="medicalOrgName"/>
                    <value xsi:type="ED">
                        <xsl:value-of select="medicalinstitutionname"/>
                    </value>
                    <xsl:comment>D.CN.3: Name of medical institution</xsl:comment>
                </observation>
            </subjectOf2>
        </xsl:if>

        <!-- D.CN.4 Nationality -->
        <xsl:if test="string-length(patientnationality) > 0 and string-length(patientnationality) = 2">
            <subjectOf2 typeCode="SBJ">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$PatientNationality}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$nmpaoidC1CN1LVersion}" displayName="nationality"/>
                    <value xsi:type="CS" code="{patientnationality}" />
                    <xsl:comment>D.CN.4: 国籍</xsl:comment>
                </observation>
            </subjectOf2>
        </xsl:if>

        <!-- D.CN.5  Patient Telephone -->
        <xsl:if test="string-length(patienttelephone) > 0">
            <subjectOf2 typeCode="SBJ">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$PatientTelephone}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$nmpaoidC1CN1LVersion}" displayName="phone"/>
                    <value xsi:type="TEL" value="{patienttelephone}" />
                    <xsl:comment>D.CN.5: 联系电话</xsl:comment>
                </observation>
            </subjectOf2>
        </xsl:if>

        <!-- D.CN.6 Description of Pregnancy details -->
        <xsl:if test="string-length(pregnancydescription) > 0">
            <subjectOf2 typeCode="SBJ">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$PregnancyDescription}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$nmpaoidC1CN1LVersion}" displayName="pregnancyOfDescription"/>
                    <value xsi:type="ED">
                        <xsl:value-of select="pregnancydescription"/>
                    </value>
                    <xsl:comment>D.CN.6: 妊娠相关描述</xsl:comment>
                </observation>
            </subjectOf2>
        </xsl:if>

        <!--B.1.4 Height-->
        <xsl:if test="string-length(patientheight) > 0">
            <subjectOf2 typeCode="SBJ">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$Height}" codeSystem="{$oidObservationCode}" displayName="height"/>
                    <value xsi:type="PQ" value="{patientheight}" unit="cm"/>
                    <xsl:comment>D.4: Height (cm)</xsl:comment>
                </observation>
            </subjectOf2>
        </xsl:if>
        <!--B.1.6 Last Menstrual Period Date-->
        <xsl:if test="string-length(patientlastmenstrualdate) > 0">
            <xsl:variable name="isNullFlavourPatLMP">
                <xsl:call-template name="isNullFlavour">
                    <xsl:with-param name="value" select="patientlastmenstrualdate"/>
                </xsl:call-template>
            </xsl:variable>
            <subjectOf2 typeCode="SBJ">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$LastMenstrualPeriodDate}" codeSystem="{$oidObservationCode}"
                          displayName="lastMenstrualPeriodDate"/>
                    <xsl:choose>
                        <xsl:when test="$isNullFlavourPatLMP = 'yes'">
                            <xsl:variable name="NullFlavourWOSqBrcktPatLMP">
                                <xsl:call-template name="getNFValueWithoutSqBrckt">
                                    <xsl:with-param name="nfvalue" select="patientlastmenstrualdate"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <value xsi:type="TS" nullFlavor="{$NullFlavourWOSqBrcktPatLMP}"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <value xsi:type="TS" value="{patientlastmenstrualdate}"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:comment>D.6: Last Menstrual Period Date</xsl:comment>
                </observation>
            </subjectOf2>
        </xsl:if>

        <!-- D.7  -->
        <!-- <xsl:apply-templates select="." mode="EMA-pat-characteristics"/> -->
        <xsl:if test="count(medicalhistoryepisode) > 0 or string-length(patientmedicalhistorytext) > 0">
            <subjectOf2 typeCode="SBJ">
                <organizer classCode="CATEGORY" moodCode="EVN">
                    <code code="{$RelevantMedicalHistoryAndConcurrentConditions}" codeSystem="{$oidValueGroupingCode}"
                          codeSystemVersion="{$emaValueGroupingCLVersion}"/>

                    <!-- D.7.1.r - Structured Information on Relevant Medical History (repeat as necessary) -->
                    <xsl:if test="count(medicalhistoryepisode) > 0 ">
                        <xsl:apply-templates select="medicalhistoryepisode" mode="EMA-pat-medical-history-episode"/>
                    </xsl:if>

                    <!-- D.7.2 Text for Relevant Medical History and Concurrent Conditions (not including reaction / event) -->
                    <xsl:if test="string-length(patientmedicalhistorytext) > 0">
                        <xsl:comment>D.7.2: Text for Relevant Medical History and Concurrent Conditions (not including
                            reaction / event)
                        </xsl:comment>
                        <xsl:variable name="isNullFlavourPatMedHist">
                            <xsl:call-template name="isNullFlavour">
                                <xsl:with-param name="value" select="patientmedicalhistorytext"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <component typeCode="COMP">
                            <observation moodCode="EVN" classCode="OBS">
                                <code code="{$HistoryAndConcurrentConditionText}" codeSystem="{$oidObservationCode}"
                                      codeSystemVersion="{$emaObservationCLVersion}"/>
                                <xsl:choose>
                                    <xsl:when test="$isNullFlavourPatMedHist = 'yes'">
                                        <xsl:variable name="NullFlavourWOSqBrcktD72">
                                            <xsl:call-template name="getNFValueWithoutSqBrckt">
                                                <xsl:with-param name="nfvalue" select="patientmedicalhistorytext"/>
                                            </xsl:call-template>
                                        </xsl:variable>
                                        <value xsi:type="ED" nullFlavor="{$NullFlavourWOSqBrcktD72}"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <value xsi:type="ED" mediaType="text/plain">
                                            <xsl:value-of select="patientmedicalhistorytext"/>
                                        </value>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </observation>
                        </component>
                    </xsl:if>

                    <!-- D.7.3: Concomitant Therapies -->
                    <xsl:if test="string-length(concomitanttherapies) > 0">
                        <xsl:comment>D.7.3: Concomitant Therapies</xsl:comment>
                        <component typeCode="COMP">
                            <observation classCode="OBS" moodCode="EVN">
                                <code code="{$ConcommitantTherapy}" codeSystem="{$oidConcomitantTherapies}"
                                      codeSystemVersion="{$emaoidD73CLVersion}"/>
                                <value xsi:type="BL" value="{concomitanttherapies}"/>
                            </observation>
                        </component>
                    </xsl:if>
                </organizer>
            </subjectOf2>
        </xsl:if>

        <!-- D.8.r Relevant Past Drug History (repeat as necessary) -->
        <xsl:if test="count(patientpastdrugtherapy) > 0">
            <subjectOf2 typeCode="SBJ">
                <organizer classCode="CATEGORY" moodCode="EVN">
                    <code code="{$DrugHistory}" codeSystem="{$oidValueGroupingCode}"
                          codeSystemVersion="{$emaValueGroupingCLVersion}"/>
                    <xsl:apply-templates select="patientpastdrugtherapy" mode="EMA-pat-past-drug-hist"/>
                </organizer>
            </subjectOf2>
        </xsl:if>


        <!--Patient Death Information-->
        <xsl:if test="count(patientdeathcause) > 0">
            <xsl:apply-templates select="patientdeathcause"/>
        </xsl:if>

        <!-- D.9.3 Was Autopsy Done? -->
        <xsl:if test="string-length(patientautopsyyesno) > 0">
            <xsl:comment>D.9.3: Was Autopsy Done?</xsl:comment>
            <subjectOf2 typeCode="SBJ">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$Autopsy}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$emaObservationCLVersion}"/>
                    <xsl:variable name="isNullFlavourAutopsy">
                        <xsl:call-template name="isNullFlavour">
                            <xsl:with-param name="value" select="patientautopsyyesno"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:choose>
                        <xsl:when test="$isNullFlavourAutopsy = 'yes'">
                            <xsl:variable name="NullFlavourWOSqBrcktD93">
                                <xsl:call-template name="getNFValueWithoutSqBrckt">
                                    <xsl:with-param name="nfvalue" select="patientautopsyyesno"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <value xsi:type="BL" nullFlavor="{$NullFlavourWOSqBrcktD93}"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <value xsi:type="BL" value="{patientautopsyyesno}"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <!-- B.1.9.3-4 Autopsy -->
                    <xsl:if test="count(patientautopsy) >0">
                        <xsl:apply-templates select="patientautopsy" mode="EMA-pat-autopsy-determined"/>
                    </xsl:if>

                </observation>
            </subjectOf2>
        </xsl:if>
    </xsl:template>


    <!-- Populating the value in an element and handling null flavour -->
    <xsl:template name="patient-record">
        <xsl:param name="value"/>
        <xsl:param name="root"/>
        <xsl:param name="code"/>
        <xsl:param name="codeSystem"/>
        <xsl:if test="string-length($value) > 0">
            <xsl:variable name="isNullFlavourPatRec">
                <xsl:call-template name="isNullFlavour">
                    <xsl:with-param name="value" select="$value"/>
                </xsl:call-template>
            </xsl:variable>
            <asIdentifiedEntity classCode="IDENT">
                <xsl:choose>
                    <xsl:when test="$isNullFlavourPatRec = 'yes'">
                        <xsl:variable name="NullFlavourWOSqBrcktPatRec">
                            <xsl:call-template name="getNFValueWithoutSqBrckt">
                                <xsl:with-param name="nfvalue" select="$value"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <id nullFlavor="{$NullFlavourWOSqBrcktPatRec}" root="{$root}"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <id extension="{$value}" root="{$root}"/>
                    </xsl:otherwise>
                </xsl:choose>
                <code code="{$code}" codeSystem="{$codeSystem}" codeSystemVersion="{$emaSourceMedicalRecordCLVersion}"/>
            </asIdentifiedEntity>
        </xsl:if>
    </xsl:template>

    <!-- Populating the value in an element and handling null flavour -->
    <xsl:template name="gender">
        <xsl:param name="value"/>
        <xsl:if test="string-length($value) > 0">
            <xsl:variable name="isNullFlavourGender">
                <xsl:call-template name="isNullFlavour">
                    <xsl:with-param name="value" select="$value"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="$isNullFlavourGender = 'yes'">
                    <xsl:variable name="NullFlavourWOSqBrcktGender">
                        <xsl:call-template name="getNFValueWithoutSqBrckt">
                            <xsl:with-param name="nfvalue" select="$value"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <administrativeGenderCode codeSystem="{$oidGenderCode}" nullFlavor="{$NullFlavourWOSqBrcktGender}"/>
                </xsl:when>
                <xsl:otherwise>
                    <administrativeGenderCode code="{$value}" codeSystem="{$oidGenderCode}"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:template match="patientpastdrugtherapy" mode="EMA-pat-past-drug-hist">
        <xsl:variable name="positionPatPastDrug">
            <xsl:value-of select="position()"/>
        </xsl:variable>
        <xsl:comment>D.8.r: Relevant Past Drug History - (<xsl:value-of select="$positionPatPastDrug"/>)
        </xsl:comment>
        <component typeCode="COMP">
            <substanceAdministration moodCode="EVN" classCode="SBADM">
                <xsl:if test="string-length(patientdrugstartdate) > 0 or string-length(patientdrugenddate) > 0">
                    <effectiveTime xsi:type="IVL_TS">
                        <!-- D.8.r.4 Start Date -->
                        <xsl:if test="string-length(patientdrugstartdate) > 0">
                            <xsl:comment>D.8.r.4: Start Date</xsl:comment>
                            <xsl:call-template name="effectiveTime">
                                <xsl:with-param name="element">low</xsl:with-param>
                                <xsl:with-param name="value" select="patientdrugstartdate"/>
                            </xsl:call-template>
                        </xsl:if>

                        <!-- D.8.r.5 End Date -->
                        <xsl:if test="string-length(patientdrugenddate) > 0">
                            <xsl:comment>D.8.r.5: End Date</xsl:comment>
                            <xsl:call-template name="effectiveTime">
                                <xsl:with-param name="element">high</xsl:with-param>
                                <xsl:with-param name="value" select="patientdrugenddate"/>
                            </xsl:call-template>
                        </xsl:if>
                    </effectiveTime>
                </xsl:if>

                <!-- D.8.r.1 Name of Drug as Reported -->
                <xsl:if test="string-length(patientdrugname) > 0">
                    <xsl:variable name="isNullFlavourPatDrugName">
                        <xsl:call-template name="isNullFlavour">
                            <xsl:with-param name="value" select="patientdrugname"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <consumable typeCode="CSM">
                        <instanceOfKind classCode="INST">
                            <kindOfProduct classCode="MMAT" determinerCode="KIND">
                                <xsl:choose>
                                    <xsl:when test="$isNullFlavourPatDrugName = 'yes'">
                                        <xsl:variable name="NullFlavourWOSqBrcktD8r1">
                                            <xsl:call-template name="getNFValueWithoutSqBrckt">
                                                <xsl:with-param name="nfvalue" select="patientdrugname"/>
                                            </xsl:call-template>
                                        </xsl:variable>
                                        <xsl:comment>D.8.r.1: Name of Drug as Reported</xsl:comment>
                                        <name nullFlavor="{$NullFlavourWOSqBrcktD8r1}"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:if test="string-length(patientmpidversion) > 0 and string-length(patientmpid) > 0">
                                            <xsl:comment>D.8.r.2b: Medicinal Product Identifier (MPID)</xsl:comment>
                                            <xsl:comment>D.8.r.2a: MPID Version Date/Number</xsl:comment>
                                            <code code="{patientmpid}" codeSystem="MPID"
                                                  codeSystemVersion="{patientmpidversion}"/>
                                        </xsl:if>
                                        <xsl:if test="string-length(patientphpidversion) > 0 and string-length(patientphpid) > 0">
                                            <xsl:comment>D.8.r.3a: PhPID Version Date/Number</xsl:comment>
                                            <xsl:comment>D.8.r.3b: Pharmaceutical Product Identifier (PhPID)
                                            </xsl:comment>
                                            <code code="{patientphpid}" codeSystem="PhPID"
                                                  codeSystemVersion="{patientphpidversion}"/>
                                        </xsl:if>
                                        <name>
                                            <xsl:comment>D.8.r.1: Name of Drug as Reported</xsl:comment>
                                            <xsl:value-of select="patientdrugname"/>
                                        </name>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </kindOfProduct>
                        </instanceOfKind>
                    </consumable>
                </xsl:if>

                <!-- D.8.r.6a MedDRA Version for Indication -->
                <!-- D.8.r.6b Indication (MedDRA code) -->
                <xsl:if test="string-length(patientdrugindication) > 0">
                    <xsl:comment>D.8.r.6a: MedDRA Version for Indication</xsl:comment>
                    <xsl:comment>D.8.r.6b: Indication (MedDRA code)</xsl:comment>
                    <outboundRelationship2 typeCode="RSON">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="{$Indication}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$emaObservationCLVersion}" displayName="Patient Past indication"/>
                            <value xsi:type="CE" code="{patientdrugindication}"
                                   codeSystemVersion="{patientdrugindicationmeddraversion}" codeSystem="{$oidMedDRA}"/>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <!-- D.8.r.7a MedDRA Version for Reaction -->
                <!-- D.8.r.7bReaction (MedDRA code) -->
                <xsl:if test="string-length(patientdrugreaction) > 0">
                    <xsl:comment>D.8.r.7a: MedDRA Version for Reaction</xsl:comment>
                    <xsl:comment>D.8.r.7b: Reaction (MedDRA code)</xsl:comment>
                    <outboundRelationship2 typeCode="CAUS">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="{$Reaction}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$emaObservationCLVersion}" displayName="Patient Past Reaction"/>
                            <value xsi:type="CE" code="{patientdrugreaction}"
                                   codeSystemVersion="{patientdrgreactionmeddraversion}" codeSystem="{$oidMedDRA}"/>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

            </substanceAdministration>
        </component>
    </xsl:template>

    <!--B.1.9.3 Autopsy Done Yes/No-->

    <!-- D.9.4.r Autopsy-determined Cause(s) of Death (repeat as necessary) :
	E2B(R2): element "patientautopsy" -  "ichicsr\ichicsrbatchheader\ichicsrmessageheader\safetyreport\patient\patientautopsy"
	E2B(R3): element "primaryRole"
	-->
    <xsl:template match="patientautopsy" mode="EMA-pat-autopsy-determined">
        <xsl:variable name="positionPatAutopsy">
            <xsl:value-of select="position()"/>
        </xsl:variable>
        <xsl:comment>D.9.4.r: Autopsy-determined Cause(s) of Death - (<xsl:value-of select="$positionPatAutopsy"/>)
        </xsl:comment>
        <outboundRelationship2 typeCode="DRIV">
            <observation moodCode="EVN" classCode="OBS">
                <code code="{$CauseOfDeath}" codeSystem="{$oidObservationCode}"
                      codeSystemVersion="{$emaObservationCLVersion}"/>
                <!-- D.9.4.r.1a: MedDRA Version for Autopsy-determined Cause(s) of Death -->
                <!-- D.9.4.r.1b Autopsy-determined Cause(s) of Death (MedDRA code)-->
                <xsl:if test="string-length(patientdeterminedautopsy) > 0 or string-length(patientdeterminedautopsymeddraversion) > 0 or string-length(patientdeterminedautopsytxt) > 0">
                    <xsl:if test="string-length(patientdeterminedautopsy) > 0 or string-length(patientdeterminedautopsymeddraversion) > 0">
                        <xsl:comment>D.9.4.r.1a: MedDRA Version for Autopsy-determined Cause(s) of Death</xsl:comment>
                        <xsl:comment>D.9.4.r.1b: Autopsy-determined Cause(s) of Death (MedDRA code)</xsl:comment>
                    </xsl:if>
                    <value xsi:type="CE">
                        <xsl:if test="string-length(patientdeterminedautopsy) > 0">
                            <xsl:attribute name="codeSystem">
                                <xsl:value-of select="$oidMedDRA"/>
                            </xsl:attribute>
                            <xsl:attribute name="code">
                                <xsl:value-of select="patientdeterminedautopsy"/>
                            </xsl:attribute>
                        </xsl:if>
                        <xsl:if test="string-length(patientdeterminedautopsymeddraversion) > 0">
                            <xsl:attribute name="codeSystemVersion">
                                <xsl:value-of select="patientdeterminedautopsymeddraversion"/>
                            </xsl:attribute>
                        </xsl:if>
                        <xsl:if test="string-length(patientdeterminedautopsytxt) > 0">
                            <!-- D.9.4.r.2: Autopsy-determined Cause(s) of Death (free text)-->
                            <xsl:comment>D.9.4.r.2: Autopsy-determined Cause(s) of Death (free text)</xsl:comment>
                            <originalText>
                                <xsl:value-of select="patientdeterminedautopsytxt"/>
                            </originalText>
                        </xsl:if>
                    </value>
                </xsl:if>
            </observation>
        </outboundRelationship2>
    </xsl:template>

    <!-- Reaction Block (EMA) E2B R3-->

    <xsl:template match="reaction">
        <xsl:variable name="positionevents">
            <xsl:value-of select="position()"/>
        </xsl:variable>
        <xsl:comment>E.i: REACTION(S)/EVENT(S) - (<xsl:value-of select="$positionevents"/>)</xsl:comment>
        <subjectOf2 typeCode="SBJ">
            <observation moodCode="EVN" classCode="OBS">
                <!-- internal reaction id -->
                <xsl:comment>Reaction/event [reaction/event reference ID]</xsl:comment>
                <!--internal reaction id-->
                <!-- <id root="RID{position()}"/> -->
                <id root="{eventuniversallyuniqueid}"/>
                <code code="{$Reaction}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
                <xsl:if test="string-length(reactionstartdate) > 0 or string-length(reactionenddate) > 0 or string-length(reactionduration) > 0">
                    <xsl:choose>
                        <xsl:when test="string-length(reactionstartdate) = 0 or string-length(reactionenddate) = 0 or string-length(reactionduration) = 0">
                            <effectiveTime xsi:type="IVL_TS">
                                <!-- E.i.4: Date of Start of Reaction / Event -->
                                <xsl:if test="string-length(reactionstartdate) > 0">
                                    <xsl:comment>E.i.4: Date of start of reaction/event</xsl:comment>
                                    <xsl:call-template name="date-of-reaction">
                                        <xsl:with-param name="element">low</xsl:with-param>
                                        <xsl:with-param name="value" select="reactionstartdate"/>
                                    </xsl:call-template>
                                </xsl:if>


                                <!-- E.i.6a: Duration of Reaction / Event (number) -->
                                <!-- E.i.6b: Duration of Reaction / Event (unit) -->
                                <xsl:if test="string-length(reactionduration) > 0">
                                    <xsl:comment>E.i.6a: Duration of Reaction / Event (number)</xsl:comment>
                                    <xsl:comment>E.i.6b: Duration of Reaction / Event (unit)</xsl:comment>
                                    <width value="{reactionduration}" unit="{reactiondurationunit}" />

                                </xsl:if>

                                <!-- E.i.5: Date of End of Reaction / Event -->
                                <xsl:if test="string-length(reactionenddate) > 0">
                                    <xsl:comment>E.i.5: Date of End of Reaction / Event</xsl:comment>
                                    <xsl:call-template name="date-of-reaction">
                                        <xsl:with-param name="element">high</xsl:with-param>
                                        <xsl:with-param name="value" select="reactionenddate"/>
                                    </xsl:call-template>
                                </xsl:if>

                            </effectiveTime>
                        </xsl:when>
                        <xsl:otherwise>
                            <effectiveTime xsi:type="SXPR_TS">
                                <comp xsi:type="IVL_TS">
                                    <!-- E.i.4: Date of Start of Reaction / Event -->
                                    <xsl:if test="string-length(reactionstartdate) > 0">
                                        <xsl:comment>E.i.4: Date of Start of Reaction / Event</xsl:comment>
                                        <xsl:call-template name="date-of-reaction">
                                            <xsl:with-param name="element">low</xsl:with-param>
                                            <xsl:with-param name="value" select="reactionstartdate"/>
                                        </xsl:call-template>
                                    </xsl:if>
                                    <!-- E.i.5: Date of End of Reaction / Event -->
                                    <xsl:if test="string-length(reactionenddate) > 0">
                                        <xsl:comment>E.i.5: Date of End of Reaction / Event</xsl:comment>
                                        <xsl:call-template name="date-of-reaction">
                                            <xsl:with-param name="element">high</xsl:with-param>
                                            <xsl:with-param name="value" select="reactionenddate"/>
                                        </xsl:call-template>
                                    </xsl:if>
                                </comp>
                                <comp xsi:type="IVL_TS" operator="A">
                                    <!-- E.i.6a: Duration of Reaction / Event (number) -->
                                    <!-- E.i.6b: Duration of Reaction / Event (unit) -->
                                    <xsl:if test="string-length(reactionduration) > 0">
                                        <xsl:comment>E.i.6a: Duration of Reaction / Event (number)</xsl:comment>
                                        <xsl:comment>E.i.6b: Duration of Reaction / Event (unit)</xsl:comment>
                                        <width value="{reactionduration}" unit="{reactiondurationunit}"/>

                                    </xsl:if>
                                </comp>
                            </effectiveTime>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:if>

                <!-- E.i.2.1a: MedDRA Version for Reaction / Event -->
                <!-- E.i.2.1b: Reaction / Event (MedDRA code) -->
                <xsl:if test="string-length(reactionmeddracode) > 0 or string-length(primarysourcereactionnative) > 0">
                    <xsl:comment>E.i.2.1a: MedDRA Version for Reaction / Event</xsl:comment>
                    <xsl:comment>E.i.2.1b: Reaction / Event (MedDRA code)</xsl:comment>
                    <value xsi:type="CE">
                        <xsl:if test="string-length(reactionmeddracode) > 0">
                            <xsl:attribute name="codeSystem">
                                <xsl:value-of select="$oidMedDRA"/>
                            </xsl:attribute>
                            <xsl:attribute name="code">
                                <xsl:value-of select="reactionmeddracode"/>
                            </xsl:attribute>
                        </xsl:if>
                        <xsl:if test="string-length(reactionmeddraversion) > 0">
                            <xsl:attribute name="codeSystemVersion">
                                <xsl:value-of select="reactionmeddraversion"/>
                            </xsl:attribute>
                        </xsl:if>
                        <xsl:if test="string-length(primarysourcereactionnative) > 0">
                            <xsl:comment> E.i.1.1a: Reaction / Event as Reported by the Primary Source </xsl:comment>
                            <xsl:comment> E.i.1.1b: Reaction / Event as Reported by the Primary Source Language </xsl:comment>
                            <originalText>
                                <xsl:if test="string-length(primarysourcereactionnativelang) > 0">
                                    <xsl:attribute name="language">
                                        <xsl:value-of select="primarysourcereactionnativelang"/>
                                    </xsl:attribute>
                                </xsl:if>
                                <xsl:value-of select="primarysourcereactionnative"/>
                            </originalText>
                        </xsl:if>
                    </value>
                </xsl:if>

                <!-- E.i.9: Identification of the Country Where the Reaction / Event Occurred -->
                <xsl:if test="string-length(reactionoccurcountry) > 0">
                    <xsl:comment> E.i.9: Identification of the Country Where the Reaction / Event Occurred </xsl:comment>
                    <location typeCode="LOC">
                        <locatedEntity classCode="LOCE">
                            <locatedPlace classCode="COUNTRY" determinerCode="INSTANCE">
                                <code code="{reactionoccurcountry}" codeSystem="{$OidISOCountry}"/>
                            </locatedPlace>
                        </locatedEntity>
                    </location>
                </xsl:if>

                <!-- E.i.1.2: Reaction / Event as Reported by the Primary Source for Translation -->
                <xsl:if test="string-length(primarysourcereaction) > 0">
                    <xsl:comment> E.i.1.2: Reaction / Event as Reported by the Primary Source for Translation </xsl:comment>
                    <outboundRelationship2 typeCode="PERT">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="{$ReactionForTranslation}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
                            <value xsi:type="ED">
                                <xsl:value-of select="primarysourcereaction"/>
                            </value>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <!-- B.2.i.2.1 Term Highlighted by Reporter -->
                <xsl:if test="string-length(termhighlighted) > 0">
                    <xsl:comment>E.i.3.1: Term Highlighted by Reporter </xsl:comment>
                    <outboundRelationship2 typeCode="PERT">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="{$TermHighlightedByReporter}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
                            <value xsi:type="CE" code="{termhighlighted}" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{$emaoidEi31TermHighlightedCLVersion}"/>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <xsl:comment>E.i.3.2a: Results in Death </xsl:comment>
                <outboundRelationship2 typeCode="PERT">
                    <observation moodCode="EVN" classCode="OBS">
                        <code code="{$ResultsInDeath}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
                        <xsl:call-template name="seriousness-criteria">
                            <xsl:with-param name="value" select="seriousnessdeath"/>
                        </xsl:call-template>
                    </observation>
                </outboundRelationship2>

                <xsl:comment>E.i.3.2b: Life Threatening </xsl:comment>
                <outboundRelationship2 typeCode="PERT">
                    <observation moodCode="EVN" classCode="OBS">
                        <code code="{$LifeThreatening}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
                        <xsl:call-template name="seriousness-criteria">
                            <xsl:with-param name="value" select="seriousnesslifethreatening"/>
                        </xsl:call-template>
                    </observation>
                </outboundRelationship2>

                <xsl:comment>E.i.3.2c: Caused / Prolonged Hospitalisation </xsl:comment>
                <outboundRelationship2 typeCode="PERT">
                    <observation moodCode="EVN" classCode="OBS">
                        <code code="{$CausedProlongedHospitalisation}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
                        <xsl:call-template name="seriousness-criteria">
                            <xsl:with-param name="value" select="seriousnesshospitalization"/>
                        </xsl:call-template>
                    </observation>
                </outboundRelationship2>

                <xsl:comment>E.i.3.2d: Disabling / Incapacitating </xsl:comment>
                <outboundRelationship2 typeCode="PERT">
                    <observation moodCode="EVN" classCode="OBS">
                        <code code="{$DisablingIncapaciting}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
                        <xsl:call-template name="seriousness-criteria">
                            <xsl:with-param name="value" select="seriousnessdisabling"/>
                        </xsl:call-template>
                    </observation>
                </outboundRelationship2>

                <xsl:comment>E.i.3.2e: Congenital Anomaly / Birth Defect </xsl:comment>
                <outboundRelationship2 typeCode="PERT">
                    <observation moodCode="EVN" classCode="OBS">
                        <code code="{$CongenitalAnomalyBirthDefect}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
                        <xsl:call-template name="seriousness-criteria">
                            <xsl:with-param name="value" select="seriousnesscongenitalanomali"/>
                        </xsl:call-template>
                    </observation>
                </outboundRelationship2>

                <xsl:comment>E.i.3.2f: Other Medically Important Condition  </xsl:comment>
                <outboundRelationship2 typeCode="PERT">
                    <observation moodCode="EVN" classCode="OBS">
                        <code code="{$OtherMedicallyImportantCondition}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
                        <xsl:call-template name="seriousness-criteria">
                            <xsl:with-param name="value" select="seriousnessother"/>
                        </xsl:call-template>
                    </observation>
                </outboundRelationship2>

                <xsl:if test="string-length(reactionoutcome)>0">
                    <xsl:comment>E.i.7: Outcome of Reaction / Event at the Time of Last Observation  </xsl:comment>
                    <outboundRelationship2 typeCode="PERT">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="{$Outcome}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
                            <value xsi:type="CE" code="{reactionoutcome}" codeSystem="{$oidOutcome}" codeSystemVersion="{$emaoidEi7OutcomeCLVersion}"/>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <xsl:if test="string-length(reactionmedconfirmed) > 0">
                    <xsl:comment>E.i.8: Medical Confirmation by Healthcare Professional  </xsl:comment>
                    <outboundRelationship2 typeCode="PERT">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="{$MedicalConfirmationByHP}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
                            <value xsi:type="BL" value="{reactionmedconfirmed}"/>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>
            </observation>
        </subjectOf2>
    </xsl:template>




    <!-- F.r Results of Tests and Procedures Relevant to the Investigation of the Patient (repeat as necessary)
	E2B(R2): element "test" - "ichicsr\ichicsrbatchheader\ichicsrmessageheader\safetyreport\test"
	E2B(R3): element ""
	-->
    <xsl:template match="test" mode="EMA-lab-test">
        <xsl:if test="string-length(testdate) > 0 or string-length(testname) > 0 or string-length(testmeddracode) > 0 or string-length(testresulttxt) >0">
            <xsl:variable name="positionLabTest">
                <xsl:value-of select="position()"/>
            </xsl:variable>
            <xsl:comment>F.r: Results of Tests and Procedures Relevant to the Investigation of the Patient -
                (<xsl:value-of select="$positionLabTest"/>)
            </xsl:comment>
            <component typeCode="COMP">
                <observation moodCode="EVN" classCode="OBS">
                    <xsl:choose>
                        <xsl:when test="string-length(testmeddracode) > 0 and string-length(testmeddraversion) > 0">
                            <xsl:comment>F.r.2.2a: MedDRA Version for Test Name</xsl:comment>
                            <xsl:comment>F.r.2.2b: Test Name (MedDRA code)</xsl:comment>
                            <code code="{testmeddracode}" codeSystem="{$oidMedDRA}"
                                  codeSystemVersion="{testmeddraversion}">
                                <!-- F.r.2.1: Test Name (free text) -->
                                <xsl:choose>
                                    <xsl:when test="string-length(testname) > 0">
                                        <xsl:comment>F.r.2.1: Test Name (free text)</xsl:comment>
                                        <originalText>
                                            <xsl:value-of select="testname"/>
                                        </originalText>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <originalText/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </code>
                        </xsl:when>
                        <xsl:otherwise>
                            <code codeSystem="{$oidMedDRA}">
                                <xsl:choose>
                                    <xsl:when test="string-length(testname) > 0">
                                        <xsl:comment>F.r.2.1: Test Name (free text)</xsl:comment>
                                        <originalText>
                                            <xsl:value-of select="testname"/>
                                        </originalText>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <originalText/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </code>
                        </xsl:otherwise>
                    </xsl:choose>

                    <!-- F.r.1: Test Date -->
                    <xsl:if test="string-length(testdate) > 0">
                        <xsl:comment>F.r.1: Test Date</xsl:comment>
                        <xsl:variable name="isNullFlavour">
                            <xsl:call-template name="isNullFlavour">
                                <xsl:with-param name="value" select="testdate"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:choose>
                            <xsl:when test="$isNullFlavour = 'yes'">
                                <xsl:variable name="NullFlavourWOSqBrcktFr1">
                                    <xsl:call-template name="getNFValueWithoutSqBrckt">
                                        <xsl:with-param name="nfvalue" select="testdate"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <effectiveTime nullFlavor="{$NullFlavourWOSqBrcktFr1}"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <effectiveTime value="{testdate}"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>

                    <!-- F.r.3.4: Result Unstructured Data (free text) -->
                    <xsl:choose>
                        <xsl:when test="string-length(testresulttxt) > 0">
                            <xsl:comment>F.r.3.4: Result Unstructured Data (free text)</xsl:comment>
                            <value xsi:type="ED">
                                <xsl:value-of select="testresulttxt"/>
                            </value>
                        </xsl:when>
                        <xsl:otherwise>
                            <value xsi:type="ED"/>
                        </xsl:otherwise>
                    </xsl:choose>

                    <!-- F.r.3.2: Test Result (value / qualifier) -->
                    <!-- F.r.3.3: Test Result (unit) -->
                    <xsl:comment>F.r.3.2: Test Result (value / qualifier)</xsl:comment>
                    <xsl:if test="string-length(testresult) > 0">
                        <xsl:apply-templates select="." mode="EMA-testresult-PINF-NINF"/>
                    </xsl:if>

                    <!--F.r.3.1 Test Result (code)-->
                    <xsl:choose>
                        <xsl:when test="string-length(testresultcode) > 0">
                            <xsl:comment>F.r.3.1: Test Result (code)</xsl:comment>
                            <interpretationCode code="{testresultcode}" codeSystem="{$oidTestResultCode}"
                                                codeSystemVersion="{$emaoidFr31CLVersion}"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <interpretationCode codeSystem="{$oidTestResultCode}"
                                                codeSystemVersion="{$emaoidFr31CLVersion}"/>
                        </xsl:otherwise>
                    </xsl:choose>

                    <!-- F.r.4: Normal Low Value -->
                    <xsl:comment>F.r.4: Normal low range</xsl:comment>
                    <referenceRange typeCode="REFV">
                        <observationRange classCode="OBS" moodCode="EVN.CRT">
                            <xsl:comment>F.r.3.3: Test Result (unit)</xsl:comment>
                            <xsl:choose>
                                <xsl:when test="number(lowtestrange) or number(normalize-space(lowtestrange))>=0">
                                    <value xsi:type="PQ" value="{lowtestrange}">
                                        <xsl:attribute name="unit">
                                            <xsl:choose>
                                                <xsl:when test="string-length(testunit) > 0">
                                                    <xsl:value-of select="testunit"/>
                                                </xsl:when>
                                                <!-- <xsl:when test="string-length(testunit) > 0">
								<xsl:call-template name="getMapping">
								<xsl:with-param name="type">UCUM</xsl:with-param>
								<xsl:with-param name="code" select="testunit"/>
						</xsl:call-template>
						</xsl:when> -->

                                                <xsl:otherwise>1</xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:attribute>
                                    </value>
                                </xsl:when>
                                <xsl:when test="string-length(lowtestrange)=0">
                                    <value xsi:type="PQ"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <value xsi:type="ED">
                                        <xsl:value-of select="lowtestrange"/>
                                    </value>
                                </xsl:otherwise>
                            </xsl:choose>
                            <interpretationCode code="L" codeSystem="2.16.840.1.113883.5.83"/>
                        </observationRange>
                    </referenceRange>

                    <!-- F.r.5 Normal High Value -->
                    <xsl:comment>F.r.5: Normal high value</xsl:comment>
                    <referenceRange typeCode="REFV">
                        <observationRange classCode="OBS" moodCode="EVN.CRT">
                            <xsl:choose>
                                <xsl:when test="number(hightestrange) or number(normalize-space(hightestrange))>=0">
                                    <value xsi:type="PQ" value="{hightestrange}">
                                        <xsl:attribute name="unit">
                                            <xsl:choose>
                                                <xsl:when test="string-length(testunit) > 0">
                                                    <xsl:value-of select="testunit"/>
                                                </xsl:when>
                                                <!-- <xsl:call-template name="getMapping">
							<xsl:with-param name="type">UCUM</xsl:with-param>
							<xsl:with-param name="code" select="testunit"/>
						</xsl:call-template> -->

                                                <xsl:otherwise>1</xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:attribute>
                                    </value>
                                </xsl:when>
                                <xsl:when test="string-length(hightestrange)=0">
                                    <value xsi:type="PQ"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <value xsi:type="ED">
                                        <xsl:value-of select="hightestrange"/>
                                    </value>
                                </xsl:otherwise>
                            </xsl:choose>
                            <interpretationCode code="H" codeSystem="2.16.840.1.113883.5.83"/>
                        </observationRange>
                    </referenceRange>

                    <!-- F.r.6 Comments (free text) -->
                    <outboundRelationship2 typeCode="PERT">
                        <observation moodCode="EVN" classCode="OBS">
                            <xsl:comment>F.r.6: Comments (free text)</xsl:comment>
                            <code code="{$Comment}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$emaObservationCLVersion}" displayName="comment"/>
                            <xsl:choose>
                                <xsl:when test="string-length(testresultcomments)>0">
                                    <value xsi:type="ED">
                                        <xsl:value-of select="testresultcomments"/>
                                    </value>
                                </xsl:when>
                                <xsl:otherwise>
                                    <value xsi:type="ED"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </observation>
                    </outboundRelationship2>

                    <!-- F.r.7 More Information Available -->
                    <xsl:comment>F.r.7: More information available</xsl:comment>
                    <outboundRelationship2 typeCode="REFR">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="{$MoreInformationAvailable}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$emaObservationCLVersion}"
                                  displayName="moreInformationAvailable"/>
                            <xsl:choose>
                                <xsl:when test="string-length(moreinformation)>0">
                                    <value xsi:type="BL" value="{moreinformation}"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <value xsi:type="BL"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </observation>
                    </outboundRelationship2>
                </observation>
            </component>
        </xsl:if>
    </xsl:template>


    <!-- Removed Old Test Templtae  -->
    <!-- New Logic for Drug  -->


    <xsl:template match="drug" mode="main">
        <xsl:variable name="positionDrug">
            <xsl:value-of select="position()"/>
        </xsl:variable>
        <xsl:comment>G.k: DRUG(S) INFORMATION - (<xsl:value-of select="$positionDrug"/>)
        </xsl:comment>
        <component typeCode="COMP">
            <substanceAdministration moodCode="EVN" classCode="SBADM">
                <xsl:comment>G.k[GID]: Drug UUID</xsl:comment>
                <id root="{druguniversallyuniqueid}"/>
                <!--<id root="DID{position()}"/>  -->
                <consumable typeCode="CSM">
                    <instanceOfKind classCode="INST">
                        <kindOfProduct classCode="MMAT" determinerCode="KIND">
                            <xsl:if test="string-length(drugmpidversion) > 0 and string-length(drugmpid) > 0">
                                <xsl:comment>G.k.2.1.1a: MPID Version Date / Number</xsl:comment>
                                <xsl:comment>G.k.2.1.1b: Medicinal Product Identifier (MPID)</xsl:comment>
                                <code code="{drugmpid}" codeSystem="MPID" codeSystemVersion="{drugmpidversion}"
                                      displayName="Medicinal Product Identifier and Version"/>
                            </xsl:if>
                            <xsl:if test="string-length(drugphpidversion) > 0 and string-length(drugphpid) > 0">
                                <xsl:comment>G.k.2.1.2a: PhPID Version Date / Number</xsl:comment>
                                <xsl:comment>G.k.2.1.2b: Pharmaceutical Product Identifier (PhPID)</xsl:comment>
                                <code code="{drugphpid}" codeSystem="PhPID" codeSystemVersion="{drugphpidversion}"
                                      displayName="Pharmaceutical Product Identifier and Version"/>
                            </xsl:if>

                            <name>
                                <xsl:comment>G.k.2.2: Medicinal Product Name as Reported by the Primary Source
                                </xsl:comment>
                                <xsl:value-of select="medicinalproduct"/>
                            </name>

                            <asManufacturedProduct classCode="MANU">
                                <xsl:comment>G.k.3: Holder and Authorisation / Application Number of Drug</xsl:comment>
                                <xsl:if test="string-length(drugauthorizationnumb) > 0 or string-length(drugauthorizationcountry) > 0 or string-length(drugauthorizationholder) > 0">
                                    <subjectOf typeCode="SBJ">
                                        <approval classCode="CNTRCT" moodCode="EVN">
                                            <xsl:comment>G.k.3.1: Authorisation / Application Number</xsl:comment>
                                            <xsl:if test="string-length(drugauthorizationnumb) > 0">
                                                <id extension="{drugauthorizationnumb}"
                                                    root="{$oidAuthorisationNumber}"/>
                                            </xsl:if>

                                            <xsl:if test="string-length(drugauthorizationholder) > 0">
                                                <holder typeCode="HLD">
                                                    <role classCode="HLD">
                                                        <xsl:comment>G.k.3.3: Name of Holder / Applicant</xsl:comment>
                                                        <playingOrganization classCode="ORG" determinerCode="INSTANCE">
                                                            <name>
                                                                <xsl:value-of select="drugauthorizationholder"/>
                                                            </name>
                                                        </playingOrganization>
                                                    </role>
                                                </holder>
                                            </xsl:if>

                                            <xsl:if test="string-length(drugauthorizationcountry) > 0">
                                                <author typeCode="AUT">
                                                    <territorialAuthority classCode="TERR">
                                                        <territory classCode="NAT" determinerCode="INSTANCE">
                                                            <xsl:comment>G.k.3.2: Country of Authorisation /
                                                                Application
                                                            </xsl:comment>
                                                            <code codeSystem="{$oidISOCountry}"
                                                                  code="{drugauthorizationcountry}"/>
                                                        </territory>
                                                    </territorialAuthority>
                                                </author>
                                            </xsl:if>

                                        </approval>
                                    </subjectOf>

                                </xsl:if>
                            </asManufacturedProduct>

                            <!-- G.k.2.3.r Substance / Specified Substance Identifier and Strength (repeat as necessary) -->
                            <xsl:if test="count(activesubstance) > 0">
                                <xsl:apply-templates select="activesubstance" mode="EMA-drug-ingredients"/>
                            </xsl:if>

                        </kindOfProduct>

                        <!-- G.k.2.4 Identification of the Country Where the Drug Was Obtained -->
                        <xsl:if test="string-length(obtaindrugcountry) > 0">
                            <xsl:comment>G.k.2.4: Identification of the Country Where the Drug Was Obtained
                            </xsl:comment>
                            <subjectOf typeCode="SBJ">
                                <productEvent classCode="ACT" moodCode="EVN">
                                    <code code="{$RetailSupply}" codeSystem="{$oidActionPerformedCode}"
                                          codeSystemVersion="{$emaoidGk24CLVersion}" displayName="retailSupply"/>
                                    <performer typeCode="PRF">
                                        <assignedEntity classCode="ASSIGNED">
                                            <representedOrganization classCode="ORG" determinerCode="INSTANCE">
                                                <addr>
                                                    <country>
                                                        <xsl:value-of select="obtaindrugcountry"/>
                                                    </country>
                                                </addr>
                                            </representedOrganization>
                                        </assignedEntity>
                                    </performer>
                                </productEvent>
                            </subjectOf>
                        </xsl:if>

                    </instanceOfKind>
                </consumable>

                <!--<xsl:for-each select="../reaction[1]">
							<xsl:if test="string-length(reactionfirsttime) > 0">
								<outboundRelationship1 typeCode="SAS">
									<pauseQuantity value="{reactionfirsttime}">
										<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="reactionfirsttimeunit"/></xsl:call-template></xsl:attribute>
									</pauseQuantity>
									<actReference classCode="ACT" moodCode="EVN">
										<id extension="RID1"/>
									</actReference>
								</outboundRelationship1>
							</xsl:if>
							<xsl:if test="string-length(reactionlasttime)>0">
								<outboundRelationship1 typeCode="SAE">
									<pauseQuantity value="{reactionlasttime}">
										<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="reactionlasttimeunit"/></xsl:call-template></xsl:attribute>
									</pauseQuantity>
									<actReference classCode="ACT" moodCode="EVN">
										<id extension="RID1"/>
									</actReference>
								</outboundRelationship1>
							</xsl:if>
						</xsl:for-each>-->

                <xsl:if test="count(drugrelatedness) > 0">
                    <xsl:apply-templates select="drugrelatedness" mode="EMA-interval"/>
                </xsl:if>

                <!--G.k.2.5 Investigational Product Blinded-->
                <xsl:if test="string-length(investigationalblindedproduct) > 0">
                    <xsl:comment>G.k.2.5: Investigational Product Blinded</xsl:comment>
                    <outboundRelationship2 typeCode="PERT">
                        <observation classCode="OBS" moodCode="EVN">
                            <code code="{$Blinded}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$emaObservationCLVersion}"/>
                            <value xsi:type="BL" value="{investigationalblindedproduct}"/>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <!--G.k.4.r Dosage and Relevant Information (repeat as necessary)-->
                <xsl:if test="count(drugdosage) > 0">
                    <xsl:apply-templates select="drugdosage" mode="EMA-drug-dosage-info"/>
                </xsl:if>

                <xsl:if test="count(drugdosage) = 0 and count(devicecomponentdetails) > 0">
                    <xsl:apply-templates select="." mode="EMA-drug-dosage-info-for-device-component"/>
                </xsl:if>

                <!-- G.k.5a Cumulative Dose to First Reaction (number)  -->
                <!-- G.k.5b Cumulative Dose to First Reaction (unit) -->
                <xsl:if test="string-length(drugcumulativedosagenumb) > 0">
                    <xsl:comment>G.k.5a: Cumulative Dose to First Reaction (number)</xsl:comment>
                    <xsl:comment>G.k.5b: Cumulative Dose to First Reaction (unit)</xsl:comment>
                    <outboundRelationship2 typeCode="SUMM">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="{$CumulativeDoseToReaction}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$emaObservationCLVersion}"
                                  displayName="cumulativeDoseToReaction"/>
                            <value xsi:type="PQ" value="{drugcumulativedosagenumb}" unit="{drugcumulativedosageunit}"/>
                            <!--<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="drugcumulativedosageunit"/></xsl:call-template></xsl:attribute>
                  </value>  -->
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <!-- G.k.6a Gestation Period at Time of Exposure (number) -->
                <!-- G.k.6b Gestation Period at Time of Exposure (unit) -->
                <xsl:if test="string-length(reactiongestationperiod) > 0 and string-length(reactiongestationperiodunit) > 0">
                    <xsl:comment>G.k.6a: Gestation Period at Time of Exposure (number)</xsl:comment>
                    <xsl:comment>G.k.6b: Gestation Period at Time of Exposure (unit)</xsl:comment>
                    <outboundRelationship2 typeCode="PERT">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="{$GestationPeriod}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$emaObservationCLVersion}"/>
                            <value xsi:type="PQ" value="{reactiongestationperiod}"
                                   unit="{reactiongestationperiodunit}"/>
                            <!--<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="reactiongestationperiodunit"/></xsl:call-template></xsl:attribute>
				  </value>  -->
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <!-- G.k.9.i.4 Did Reaction Recur on Re-administration? -->
                <xsl:if test="count(drugrelatedness) > 0">
                    <xsl:apply-templates select="drugrelatedness" mode="EMA-recur"/>
                </xsl:if>

                <!-- G.k.10.r Additional Information on Drug (coded) (repeat as necessary) -->
                <xsl:if test="count(drugadditionalstructured) > 0">
                    <xsl:comment>G.k.10: Additional Information on Drug (coded)</xsl:comment>
                    <xsl:apply-templates select="drugadditionalstructured" mode="EMA-drug-additional-info"/>
                </xsl:if>

                <!-- G.k.11 Additional Information on Drug (free text) -->
                <xsl:if test="string-length(drugadditionaltext) > 0">
                    <outboundRelationship2 typeCode="REFR">
                        <observation classCode="OBS" moodCode="EVN">
                            <code code="{$AdditionalCodedDrugInformation}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$emaObservationCLVersion}"/>
                            <xsl:comment>G.k.11: Additional Information on Drug (free text)</xsl:comment>
                            <value xsi:type="ST">
                                <xsl:value-of select="drugadditionaltext"/>
                            </value>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <!-- G.k.CN.1 Trade name/Generic name of the drug -->
                <xsl:if test="string-length(cngenericname) > 0">
                    <outboundRelationship2 typeCode="REFR">
                        <observation classCode="OBS" moodCode="EVN">
                            <code code="{$CNGenericName}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$nmpaoidC1CN1LVersion}" displayName="drugProductName"/>
                            <value xsi:type="ST">
                                <xsl:value-of select="cngenericname"/>
                            </value>
                            <xsl:comment>G.k.CN.1 药品通用名称</xsl:comment>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <!-- G.k.CN.2 Related Device -->
                <xsl:if test="string-length(relateddevice) > 0">
                    <outboundRelationship2 typeCode="REFR">
                        <observation classCode="OBS" moodCode="EVN">
                            <code code="{$RelatedDevice}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$nmpaoidC1CN1LVersion}" displayName="medicalEquipment"/>
                            <value xsi:type="ST">
                                <xsl:value-of select="relateddevice"/>
                            </value>
                            <xsl:comment>G.k.CN.2 相关器械</xsl:comment>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <!-- G.k.CN.3 Whether it is a product of the Holder (repeat if necessary) -->
                <xsl:if test="string-length(cnmarketauthholder) > 0">
                    <outboundRelationship2 typeCode="REFR">
                        <observation classCode="OBS" moodCode="EVN">
                            <code code="{$CNMarketAuthHolder}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$nmpaoidC1CN1LVersion}" displayName="selfDrugProduct"/>
                            <value xsi:type="BL" value="{cnmarketauthholder}"/>
                            <xsl:comment>G.k.CN.3 是否为本持有人产品</xsl:comment>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <!-- G.k.CN.4 CTA/NDA number -->
                <xsl:if test="string-length(cnapprovalnumber) > 0">
                    <outboundRelationship2 typeCode="REFR">
                        <observation classCode="OBS" moodCode="EVN">
                            <code code="{$CNApprovalNumber}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$nmpaoidC1CN1LVersion}" displayName="approvalOrAcceptNumber"/>
                            <value xsi:type="ST">
                                <xsl:value-of select="cnapprovalnumber"/>
                            </value>
                            <xsl:comment>G.k.CN.4 批准文号/受理号</xsl:comment>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <!-- G.k.7.r Indication for Use in Case (repeat as necessary) -->
                <xsl:if test="count(drugindications) > 0">
                    <xsl:apply-templates select="drugindications" mode="EMA-drug-indication"/>
                </xsl:if>

                <!-- G.k.8 Action(s) Taken with Drug -->
                <xsl:if test="string-length(actiondrug) > 0">
                    <xsl:comment>G.k.8: Action(s) Taken with Drug</xsl:comment>
                    <inboundRelationship typeCode="CAUS">
                        <act classCode="ACT" moodCode="EVN">
                            <code code="{actiondrug}" codeSystem="{$oidActionTaken}"
                                  codeSystemVersion="{$emaoidGk8CLVersion}"/>
                        </act>
                    </inboundRelationship>
                </xsl:if>

            </substanceAdministration>
        </component>
    </xsl:template>


    <!-- G.k.2.3.r Substance / Specified Substance Identifier and Strength (repeat as necessary)
	E2B(R2): element "activesubstance" - "ichicsr/ichicsrbatchheader/ichicsrmessageheader/safetyreport/drug/activesubstance"
	E2B(R3): element "ingredient"
	-->
    <xsl:template match="activesubstance" mode="EMA-drug-ingredients">
        <xsl:if test="string-length(activesubstancename)>0">
            <xsl:variable name="positionActiveSub">
                <xsl:value-of select="position()"/>
            </xsl:variable>
            <xsl:comment>G.k.2.3.r: Substance / Specified Substance Identifier and Strength - (<xsl:value-of
                    select="$positionActiveSub"/>)
            </xsl:comment>
            <ingredient classCode="ACTI">
                <xsl:if test="string-length(substancestrength) > 0 and string-length(substancestrengthunit) > 0">
                    <quantity>
                        <xsl:comment>G.k.2.3.r.3a: Strength (number)</xsl:comment>
                        <xsl:comment>G.k.2.3.r.3b: Strength (unit)</xsl:comment>
                        <numerator value="{substancestrength}" unit="{substancestrengthunit}"/>
                        <!-- <xsl:element name="numerator">
					<xsl:attribute name="value">
					    <xsl:value-of select="substancestrength" />
					</xsl:attribute>
				  	<xsl:attribute name="unit">
					    <xsl:call-template name="getMapping">
							<xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="substancestrengthunit"/>
					    </xsl:call-template>
					</xsl:attribute>
			</xsl:element>	-->
                        <denominator value="1"/>
                    </quantity>
                </xsl:if>
                <ingredientSubstance classCode="MMAT" determinerCode="KIND">
                    <xsl:comment>G.k.2.3.r.2a: Substance / Specified Substance TermID Version Date / Number
                    </xsl:comment>
                    <xsl:comment>G.k.2.3.r.2b: Substance / Specified Substance TermID</xsl:comment>
                    <xsl:if test="string-length(activesubstancetermid) > 0 and string-length(activesubstancetermidversion) > 0">
                        <code code="{activesubstancetermid}" codeSystem="TBD-Substance"
                              codeSystemVersion="{activesubstancetermidversion}" displayName="drugInformation"/>
                    </xsl:if>
                    <xsl:comment>G.k.2.3.r.1: Substance / Specified Substance Name</xsl:comment>
                    <name>
                        <xsl:value-of select="activesubstancename"/>
                    </name>
                </ingredientSubstance>
            </ingredient>
        </xsl:if>
    </xsl:template>

    <!-- G.k.7.r Indication for Use in Case (repeat as necessary)
	E2B(R2): element "drugindicationr3" - "ichicsr/ichicsrbatchheader/ichicsrmessageheader/safetyreport/drug/drugindicationr3"
	E2B(R3): element ""
	-->

    <xsl:template match="drugindications" mode="EMA-drug-indication">
        <xsl:if test="string-length(drugindicationterm) > 0">
            <xsl:variable name="positionDrugInd">
                <xsl:value-of select="position()"/>
            </xsl:variable>
            <xsl:variable name="isNullFlavourDrugIndication">
                <xsl:call-template name="isNullFlavour">
                    <xsl:with-param name="value" select="drugindicationterm"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:comment>G.k.7.r: Indication for Use in Case - (<xsl:value-of select="$positionDrugInd"/>)
            </xsl:comment>
            <inboundRelationship typeCode="RSON">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$Indication}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$emaObservationCLVersion}"/>
                    <xsl:choose>
                        <xsl:when test="$isNullFlavourDrugIndication = 'yes'">
                            <xsl:variable name="NullFlavourWOSqBrcktDrugIndication">
                                <xsl:call-template name="getNFValueWithoutSqBrckt">
                                    <xsl:with-param name="nfvalue" select="drugindicationterm"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:comment>G.k.7.r.1: Indication as Reported by the Primary Source</xsl:comment>
                            <value xsi:type="CE">
                                <originalText nullFlavor="{$NullFlavourWOSqBrcktDrugIndication}"/>
                            </value>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when
                                        test="(string-length(drugindication) > 0 and string-length(drugindicationmeddraversion) > 0)">
                                    <xsl:comment>G.k.7.r.2a: MedDRA Version for Indication</xsl:comment>
                                    <xsl:comment>G.k.7.r.2b: Indication (MedDRA code)</xsl:comment>
                                    <xsl:comment>G.k.7.r.1: Indication as Reported by the Primary Source</xsl:comment>
                                    <value xsi:type="CE" code="{drugindication}" codeSystem="{$oidMedDRA}"
                                           codeSystemVersion="{drugindicationmeddraversion}">
                                        <originalText>
                                            <xsl:value-of select="drugindicationterm"/>
                                        </originalText>
                                    </value>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:if test="string-length(drugindicationterm)>0">
                                        <xsl:comment>G.k.7.r.1: Indication as Reported by the Primary Source
                                        </xsl:comment>
                                        <value xsi:type="CE">
                                            <originalText>
                                                <xsl:value-of select="drugindicationterm"/>
                                            </originalText>
                                        </value>
                                    </xsl:if>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                    <performer typeCode="PRF">
                        <assignedEntity classCode="ASSIGNED">
                            <code code="{$SourceReporter}" codeSystem="{$oidAssignedEntityRoleCode}"
                                  codeSystemVersion="{$emaoidAssignedEntityRoleCodeVersion}"
                                  displayName="sourceReporter"/>
                        </assignedEntity>
                    </performer>
                    <outboundRelationship1 typeCode="REFR">
                        <xsl:comment>G.k[GID]: Drug UUID</xsl:comment>
                        <actReference classCode="SBADM" moodCode="EVN">
                            <id root="{../druguniversallyuniqueid}"/>
                        </actReference>
                    </outboundRelationship1>
                </observation>
            </inboundRelationship>

        </xsl:if>
    </xsl:template>


    <!-- G.k.9.i.4 Did Reaction Recur on Re-administration?
	E2B(R2): element "drugrecurreadministration" - "ichicsr/ichicsrbatchheader/ichicsrmessageheader/safetyreport/drug/drugeventmatrix"
	E2B(R3): element ""
	-->
    <xsl:template match="drugrelatedness" mode="EMA-recur">

        <xsl:variable name="druguniversallyuniqueid">
            <xsl:value-of select="position()"/>
        </xsl:variable>
        <xsl:variable name="eventuniversallyuniqueid">
            <xsl:value-of select="position()"/>
        </xsl:variable>
        <!-- G.k.9.i.CN.1 : Expectedness -->
        <xsl:if test="string-length(cnlistedness)>0 and string-length(cnlistedness) = 1">
            <outboundRelationship2 typeCode="PERT">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$RecurranceOfListedness}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$nmpaoidC1CN1LVersion}" displayName="unexpected"/>
                    <xsl:comment>G.k.9.i.CN.1 : Is it unexpected? Drug #<xsl:value-of select="$druguniversallyuniqueid"/>, Reaction #<xsl:value-of select="$eventuniversallyuniqueid"/></xsl:comment>
                    <value xsi:type="CE" code="{cnlistedness}" />
                    <xsl:variable name="reaction" select="normalize-space(eventuniversallyuniqueid)"/>
                    <xsl:variable name="rid">
                        <xsl:if test="string-length($reaction) > 0 ">
                            <xsl:value-of select="$reaction"/>
                        </xsl:if>
                    </xsl:variable>
                    <xsl:if test="string-length($rid) > 0">
                        <outboundRelationship1 typeCode="REFR">
                            <actReference moodCode="EVN" classCode="OBS">
                                <id root="{normalize-space($rid)}"/>
                            </actReference>
                        </outboundRelationship1>
                    </xsl:if>
                </observation>
            </outboundRelationship2>
        </xsl:if>

        <!-- G.k.9.i.CN.2 : After stopping or reducing the dose, whether the reaction disappears or lessens/De-challenge. -->
        <xsl:if test="string-length(dechallenge)>0 and string-length(dechallenge) = 1">
            <outboundRelationship2 typeCode="PERT">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$DeChallenge}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$nmpaoidC1CN1LVersion}" displayName="recurranceOfReaction"/>
                    <xsl:comment>G.k.9.i.CN.2: Does the reaction disappear or decrease after withdrawal or reduction of dosage？</xsl:comment>
                    <value xsi:type="CE" code="{dechallenge}" />
                    <xsl:variable name="reaction" select="normalize-space(eventuniversallyuniqueid)"/>
                    <xsl:variable name="rid">
                        <xsl:if test="string-length($reaction) > 0 ">
                            <xsl:value-of select="$reaction"/>
                        </xsl:if>
                    </xsl:variable>
                    <xsl:if test="string-length($rid) > 0">
                        <outboundRelationship1 typeCode="REFR">
                            <actReference moodCode="EVN" classCode="OBS">
                                <id root="{normalize-space($rid)}"/>
                            </actReference>
                        </outboundRelationship1>
                    </xsl:if>
                </observation>
            </outboundRelationship2>
        </xsl:if>


        <xsl:if test="string-length(drugrecurreadministration)>0">
            <outboundRelationship2 typeCode="PERT">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$RecurranceOfReaction}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$emaObservationCLVersion}"/>
                    <xsl:comment>G.k.9.i.4: Did Reaction Recur on Re-administration?</xsl:comment>
                    <value xsi:type="CE" code="{drugrecurreadministration}" codeSystem="{$oidRechallenge}"
                           codeSystemVersion="{$emaoidGk9i4CLVersion}"/>
                    <xsl:variable name="reaction" select="normalize-space(eventuniversallyuniqueid)"/>
                    <xsl:variable name="rid">
                        <xsl:if test="string-length($reaction) > 0 ">
                            <xsl:value-of select="$reaction"/>
                        </xsl:if>
                    </xsl:variable>
                    <xsl:if test="string-length($rid) > 0">
                        <xsl:comment>G.k.9.i.1: Reaction(s) / Event(s) Assessed</xsl:comment>
                        <outboundRelationship1 typeCode="REFR">
                            <actReference moodCode="EVN" classCode="OBS">
                                <id root="{normalize-space($rid)}"/>
                            </actReference>
                        </outboundRelationship1>
                    </xsl:if>
                </observation>
            </outboundRelationship2>
        </xsl:if>
    </xsl:template>

    <!-- G.k.4.r Dosage and Relevant Information (repeat as necessary)
	E2B(R2): element "drugdosage" - "ichicsr/ichicsrbatchheader/ichicsrmessageheader/safetyreport/drug/drugdosage"
	E2B(R3): element ""
	-->
    <xsl:template match="drugdosage" mode="EMA-drug-dosage-info">
        <xsl:variable name="positionDrugDosageInfo">
            <xsl:value-of select="position()"/>
        </xsl:variable>
        <xsl:comment>G.k.4.r: Dosage and Relevant Information - (<xsl:value-of select="$positionDrugDosageInfo"/>)
        </xsl:comment>
        <outboundRelationship2 typeCode="COMP">
            <substanceAdministration classCode="SBADM" moodCode="EVN">

                <!-- G.k.4.r.8 Dosage Text -->
                <xsl:if test="string-length(drugdosagetext) > 0">
                    <xsl:comment>G.k.4.r.8: Dosage Text</xsl:comment>
                    <text>
                        <xsl:value-of select="drugdosagetext"/>
                    </text>
                </xsl:if>

                <xsl:if test="string-length(drugintervaldosageunitnumb) > 0 or string-length(drugstartdate) > 0
						or string-length(drugenddate) > 0 or string-length(drugtreatmentduration) > 0 or string-length(drugintervaldosagedefinition) > 0">
                    <xsl:if test="string-length(drugstartdate) > 0">
                        <xsl:comment>G.k.4.r.4: Date and Time of Start of Drug</xsl:comment>
                    </xsl:if>
                    <xsl:if test="string-length(drugenddate) > 0">
                        <xsl:comment>G.k.4.r.5: Date and Time of Last Administration</xsl:comment>
                    </xsl:if>
                    <xsl:if test="string-length(drugintervaldosageunitnumb) > 0">
                        <xsl:comment>G.k.4.r.6a: Duration of Drug Administration (number)</xsl:comment>
                    </xsl:if>
                    <xsl:if test="string-length(drugintervaldosagedefinition) > 0">
                        <xsl:comment>G.k.4.r.6b: Duration of Drug Administration (unit)</xsl:comment>
                    </xsl:if>
                    <xsl:if test="string-length(drugintervaldosageunitnumb) = 0 and string-length(drugintervaldosagedefinition) = 0">
                        <xsl:choose>
                            <xsl:when
                                    test="string-length(drugstartdate) = 0 or string-length(drugenddate) = 0 or string-length(drugtreatmentduration) = 0">
                                <effectiveTime xsi:type="IVL_TS">
                                    <xsl:call-template name="effectiveTime">
                                        <xsl:with-param name="element">low</xsl:with-param>
                                        <xsl:with-param name="value" select="drugstartdate"/>
                                    </xsl:call-template>

                                    <xsl:if test="string-length(drugtreatmentduration) > 0">
                                        <width value="{drugtreatmentduration}" unit="{drugtreatmentdurationunit}"/>
                                    </xsl:if>

                                    <xsl:call-template name="effectiveTime">
                                        <xsl:with-param name="element">high</xsl:with-param>
                                        <xsl:with-param name="value" select="drugenddate"/>
                                    </xsl:call-template>

                                </effectiveTime>
                            </xsl:when>
                            <xsl:otherwise>
                                <effectiveTime xsi:type="SXPR_TS">
                                    <comp xsi:type="IVL_TS" operator="A">
                                        <xsl:call-template name="effectiveTime">
                                            <xsl:with-param name="element">low</xsl:with-param>
                                            <xsl:with-param name="value" select="drugstartdate"/>
                                        </xsl:call-template>

                                        <xsl:call-template name="effectiveTime">
                                            <xsl:with-param name="element">high</xsl:with-param>
                                            <xsl:with-param name="value" select="drugenddate"/>
                                        </xsl:call-template>
                                    </comp>
                                    <comp xsi:type="IVL_TS" operator="A">
                                        <width value="{drugtreatmentduration}" unit="{drugtreatmentdurationunit}"/>
                                    </comp>
                                </effectiveTime>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>

                    <xsl:if test="string-length(drugintervaldosageunitnumb) > 0 or string-length(drugintervaldosagedefinition) > 0">
                        <effectiveTime xsi:type="SXPR_TS">
                            <comp xsi:type="PIVL_TS">
                                <!-- G.k.4.r.2: Number of Units in the Interval  -->                <!-- G.k.4.r.3: Definition of the Time Interval Unit -->
                                <xsl:if test="string-length(drugintervaldosageunitnumb) > 0 and string-length(drugintervaldosagedefinition) > 0">
                                    <xsl:comment>G.k.4.r.2: Number of Units in the Interval</xsl:comment>
                                    <xsl:comment>G.k.4.r.3: Definition of the Time Interval Unit</xsl:comment>
                                    <period value="{drugintervaldosageunitnumb}" unit="{drugintervaldosagedefinition}"/>
                                    <!-- <xsl:element name="period">
					<xsl:attribute name="value">
					    <xsl:value-of select="drugintervaldosageunitnumb" />
					</xsl:attribute>
				  	<xsl:attribute name="unit">
					<xsl:call-template name="getMapping">
					<xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="drugintervaldosagedefinition"/>
					</xsl:call-template>
					</xsl:attribute>
				 </xsl:element>	-->
                                </xsl:if>
                                <xsl:if test="string-length(drugintervaldosageunitnumb) = 0 and string-length(drugintervaldosagedefinition) > 0">
                                    <xsl:comment>G.k.4.r.3: Definition of the Time Interval Unit</xsl:comment>
                                    <period unit="{drugintervaldosagedefinition}"/>
                                    <!--<xsl:element name="period">
				  	<xsl:attribute name="unit">
					<xsl:call-template name="getMapping">
					<xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="drugintervaldosageunitnumb"/>
					</xsl:call-template>
					</xsl:attribute>
				 </xsl:element> -->
                                </xsl:if>
                            </comp>

                            <!--G.k.4.r.4 Date and Time of Start of Drug-->
                            <!--G.k.4.r.5 Date and Time of Last Administration-->
                            <!--G.k.4.r.6a Duration of Drug Administration (number)-->
                            <!--G.k.4.r.6b Duration of Drug Administration (unit)-->
                            <xsl:choose>
                                <xsl:when
                                        test="string-length(drugstartdate) = 0 or string-length(drugenddate) = 0 or string-length(drugtreatmentduration) = 0">

                                    <comp xsi:type="IVL_TS" operator="A">

                                        <xsl:call-template name="effectiveTime">
                                            <xsl:with-param name="element">low</xsl:with-param>
                                            <xsl:with-param name="value" select="drugstartdate"/>
                                        </xsl:call-template>

                                        <xsl:if test="string-length(drugtreatmentduration) > 0">
                                            <width value="{drugtreatmentduration}" unit="{drugtreatmentdurationunit}"/>
                                        </xsl:if>

                                        <xsl:call-template name="effectiveTime">
                                            <xsl:with-param name="element">high</xsl:with-param>
                                            <xsl:with-param name="value" select="drugenddate"/>
                                        </xsl:call-template>

                                    </comp>
                                </xsl:when>
                                <xsl:otherwise>
                                    <comp xsi:type="IVL_TS" operator="A">
                                        <xsl:call-template name="effectiveTime">
                                            <xsl:with-param name="element">low</xsl:with-param>
                                            <xsl:with-param name="value" select="drugstartdate"/>
                                        </xsl:call-template>

                                        <xsl:call-template name="effectiveTime">
                                            <xsl:with-param name="element">high</xsl:with-param>
                                            <xsl:with-param name="value" select="drugenddate"/>
                                        </xsl:call-template>
                                    </comp>
                                    <comp xsi:type="IVL_TS" operator="A">
                                        <width value="{drugtreatmentduration}" unit="{drugtreatmentdurationunit}"/>
                                    </comp>
                                </xsl:otherwise>
                            </xsl:choose>
                        </effectiveTime>
                    </xsl:if>
                </xsl:if>

                <!-- G.k.4.r.10.1 Route of Administration (free text) -->

                <xsl:if test="string-length(drugadministrationroute) > 0 or (string-length(drugadministrationtermid) > 0 and string-length(drugadministrationtermidversion) > 0)">
                    <xsl:variable name="isNullFlavourRouteOfAdmin">
                        <xsl:call-template name="isNullFlavour">
                            <xsl:with-param name="value" select="drugadministrationroute"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:choose>
                        <xsl:when test="$isNullFlavourRouteOfAdmin = 'yes'">
                            <xsl:variable name="NullFlavourWOSqBrcktGk4r101">
                                <xsl:call-template name="getNFValueWithoutSqBrckt">
                                    <xsl:with-param name="nfvalue" select="drugadministrationroute"/>
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:comment>G.k.4.r.10.1: Route of Administration (free text)</xsl:comment>
                            <routeCode codeSystem="{$oidICHRoute}" nullFlavor="{$NullFlavourWOSqBrcktGk4r101}"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when
                                        test="string-length(drugadministrationtermid) > 0 and string-length(drugadministrationtermidversion) > 0">
                                    <xsl:comment>G.k.4.r.10.2a: Route of Administration TermID Version Date / Number
                                    </xsl:comment>
                                    <xsl:comment>G.k.4.r.10.2b: Route of Administration TermID</xsl:comment>
                                    <routeCode code="{drugadministrationtermid}" codeSystem="{$oidICHRoute}"
                                               codeSystemVersion="{drugadministrationtermidversion}">
                                        <xsl:if test="string-length(drugadministrationroute) > 0">
                                            <xsl:comment>G.k.4.r.10.1: Route of Administration (free text)</xsl:comment>
                                            <originalText>
                                                <xsl:value-of select="drugadministrationroute"/>
                                            </originalText>
                                        </xsl:if>
                                    </routeCode>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:comment>G.k.4.r.10.1: Route of Administration (free text)</xsl:comment>
                                    <routeCode codeSystem="{$oidICHRoute}">
                                        <originalText>
                                            <xsl:value-of select="drugadministrationroute"/>
                                        </originalText>
                                    </routeCode>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:if>

                <!-- G.k.4.r.1a Dose (number) -->
                <!-- G.k.4.r.1b Dose (unit) -->
                <xsl:if test="string-length(drugstructuredosagenumb) > 0">
                    <xsl:comment>G.k.4.r.1a: Dose (number)</xsl:comment>
                    <xsl:comment>G.k.4.r.1b: Dose (unit)</xsl:comment>
                    <doseQuantity value="{drugstructuredosagenumb}" unit="{drugstructuredosageunit}"/>
                </xsl:if>

                <xsl:if test="string-length(drugbatchnumb) > 0 or string-length(drugdosageform) > 0 or count(../devicecomponentdetails) > 0">
                    <consumable typeCode="CSM">
                        <instanceOfKind classCode="INST">
                            <productInstanceInstance classCode="MMAT" determinerCode="INSTANCE">
                                <id nullFlavor="NI"/>
                                <xsl:variable name="isNullFlavourDrugBatchNumber">
                                    <xsl:call-template name="isNullFlavour">
                                        <xsl:with-param name="value" select="drugbatchnumb"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:choose>
                                    <xsl:when test="$isNullFlavourDrugBatchNumber = 'yes'">
                                        <xsl:variable name="NullFlavourWOSqBrcktGk4r7">
                                            <xsl:call-template name="getNFValueWithoutSqBrckt">
                                                <xsl:with-param name="nfvalue" select="drugbatchnumb"/>
                                            </xsl:call-template>
                                        </xsl:variable>
                                        <xsl:comment>G.k.4.r.7: Batch / Lot Number</xsl:comment>
                                        <lotNumberText nullFlavor="{$NullFlavourWOSqBrcktGk4r7}"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:comment>G.k.4.r.7: Batch / Lot Number</xsl:comment>
                                        <lotNumberText>
                                            <xsl:value-of select="drugbatchnumb"/>
                                        </lotNumberText>
                                    </xsl:otherwise>
                                </xsl:choose>

                                <!-- Drug - Device component (repeat as necessary) -->
                                <xsl:if test="count(../devicecomponentdetails) > 0 and $positionDrugDosageInfo=1">
                                    <xsl:apply-templates select="../devicecomponentdetails"
                                                         mode="EMA-drug-device-component"/>
                                </xsl:if>

                            </productInstanceInstance>
                            <kindOfProduct classCode="MMAT" determinerCode="KIND">
                                <xsl:variable name="isNullFlavourDosageForm">
                                    <xsl:call-template name="isNullFlavour">
                                        <xsl:with-param name="value" select="drugdosageform"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <xsl:choose>
                                    <xsl:when test="$isNullFlavourDosageForm = 'yes'">
                                        <xsl:variable name="NullFlavourWOSqBrcktGk4r91">
                                            <xsl:call-template name="getNFValueWithoutSqBrckt">
                                                <xsl:with-param name="nfvalue" select="drugdosageform"/>
                                            </xsl:call-template>
                                        </xsl:variable>
                                        <xsl:comment>G.k.4.r.9.1: Pharmaceutical form (Dosage form)</xsl:comment>
                                        <formCode nullFlavor="{$NullFlavourWOSqBrcktGk4r91}"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:choose>
                                            <xsl:when
                                                    test="string-length(drugdosageformtermidversion) > 0 and string-length(drugdosageformtermid) > 0 and string-length(drugdosageform) > 0">
                                                <xsl:comment>G.k.4.r.9.1: Pharmaceutical form (Dosage form)</xsl:comment>
                                                <xsl:comment>G.k.4.r.9.2a: Pharmaceutical Dose Form TermID Version Date / Number</xsl:comment>
                                                <xsl:comment>G.k.4.r.9.2b: Pharmaceutical Dose Form TermID</xsl:comment>
                                                <formCode code="{drugdosageformtermid}" codeSystem="{$oidICHFORM}"
                                                          codeSystemVersion="{drugdosageformtermidversion}">
                                                    <originalText>
                                                        <xsl:value-of select="drugdosageform"/>
                                                    </originalText>
                                                </formCode>
                                            </xsl:when>
                                            <xsl:when
                                                    test="string-length(drugdosageformtermidversion) > 0 and string-length(drugdosageformtermid) > 0">
                                                <xsl:comment>G.k.4.r.9.2a: Pharmaceutical Dose Form TermID Version Date / Number</xsl:comment>
                                                <xsl:comment>G.k.4.r.9.2b: Pharmaceutical Dose Form TermID</xsl:comment>
                                                <formCode code="{drugdosageformtermid}" codeSystem="{$oidICHFORM}"
                                                          codeSystemVersion="{drugdosageformtermidversion}">
                                                </formCode>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:comment>G.k.4.r.9.1: Pharmaceutical form (Dosage form)</xsl:comment>
                                                <formCode>
                                                    <originalText>
                                                        <xsl:value-of select="drugdosageform"/>
                                                    </originalText>
                                                </formCode>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </kindOfProduct>
                        </instanceOfKind>
                    </consumable>
                </xsl:if>

                <!-- G.k.4.r.11.1: Parent Route of Administration (free text)  -->
                <xsl:if test="string-length(drugparadministration) > 0 or (string-length(drugparadministrationtermid) > 0 and string-length(drugparadministrationtermidversion) > 0)">
                    <xsl:variable name="isNullFlavourParRouteOfAdmin">
                        <xsl:call-template name="isNullFlavour">
                            <xsl:with-param name="value" select="drugparadministration"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <inboundRelationship typeCode="REFR">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="{$ParentRouteOfAdministration}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$emaObservationCLVersion}"/>
                            <xsl:choose>
                                <xsl:when test="$isNullFlavourParRouteOfAdmin = 'yes'">
                                    <xsl:variable name="NullFlavourWOSqBrcktGk4r111">
                                        <xsl:call-template name="getNFValueWithoutSqBrckt">
                                            <xsl:with-param name="nfvalue" select="drugparadministration"/>
                                        </xsl:call-template>
                                    </xsl:variable>
                                    <xsl:comment>G.k.4.r.11.1: Parent Route of Administration (free text)</xsl:comment>
                                    <value xsi:type="CE" codeSystem="{$oidICHRoute}" nullFlavor="{$NullFlavourWOSqBrcktGk4r111}"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:choose>
                                        <xsl:when
                                                test="string-length(drugparadministrationtermid) > 0 and string-length(drugparadministrationtermidversion) > 0">
                                            <xsl:comment>G.k.4.r.11.2a: Parent Route of Administration TermID Version
                                                Date / Number
                                            </xsl:comment>
                                            <xsl:comment>G.k.4.r.11.2b: Parent Route of Administration TermID
                                            </xsl:comment>
                                            <value xsi:type="CE" code="{drugparadministrationtermid}"
                                                   codeSystem="{$oidICHRoute}"
                                                   codeSystemVersion="{drugparadministrationtermidversion}">
                                                <xsl:if test="string-length(drugparadministration) > 0">
                                                    <xsl:comment>G.k.4.r.11.1: Parent Route of Administration (free text)</xsl:comment>
                                                    <originalText>
                                                        <xsl:value-of select="drugparadministration"/>
                                                    </originalText>
                                                </xsl:if>
                                            </value>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:comment>G.k.4.r.11.1: Parent Route of Administration (free text)
                                            </xsl:comment>
                                            <value xsi:type="CE" codeSystem="{$oidICHRoute}">
                                                <originalText>
                                                    <xsl:value-of select="drugparadministration"/>
                                                </originalText>
                                            </value>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:otherwise>
                            </xsl:choose>
                        </observation>
                    </inboundRelationship>
                </xsl:if>


                <!-- G.k.4.r.CN.1: Expiry date/Valid until (date/time :: CCYYMMDD)  -->
                <xsl:if test="string-length(drugexpirydate) > 0">
                    <inboundRelationship typeCode="REFR">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="{$DrugExpiryDate}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$nmpaoidC1CN1LVersion}" displayName="drugExpiryDate" />
                            <xsl:comment>G.k.4.r.CN.1 药品失效日期</xsl:comment>
                            <value xsi:type="TS" value="{drugexpirydate}" />
                        </observation>
                    </inboundRelationship>
                </xsl:if>

            </substanceAdministration>
        </outboundRelationship2>
    </xsl:template>


    <!--G.k.9.i.2.r Assessment of Relatedness of Drug to Reaction(s) / Event(s) (repeat as necessary)
	E2B(R2): element "drugassesment" - "ichicsr/ichicsrbatchheader/ichicsrmessageheader/safetyreport/drug/drugeventmatrix"
	E2B(R3): element "causalityAssessment"
	-->
    <xsl:template match="drugassesment" mode="EMA-drug-reaction-relatedness">
        <xsl:param name="drugRef"/>
        <xsl:if test="string-length(drugassessmentsource) + string-length(drugassessmentmethod) + string-length(drugresult) > 0">
            <xsl:variable name="positionDrugReactRel">
                <xsl:value-of select="position()"/>
            </xsl:variable>
            <xsl:comment>G.k.9.i.2.r: Relatedness of Drug to Reaction(s) / Event(s) - (<xsl:value-of
                    select="$positionDrugReactRel"/>)
            </xsl:comment>
            <component typeCode="COMP">
                <causalityAssessment classCode="OBS" moodCode="EVN">
                    <code code="{$Causality}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$emaObservationCLVersion}"/>

                    <!-- G.k.9.i.2.r.3 Result of Assessment -->
                    <xsl:if test="string-length(drugresult) > 0">
                        <xsl:comment>G.k.9.i.2.r.3: Result of Assessment</xsl:comment>
                        <value xsi:type="ST">
                            <xsl:value-of select="drugresult"/>
                        </value>
                    </xsl:if>

                    <!-- G.k.9.i.2.r.2 Method of Assessment -->
                    <xsl:if test="string-length(drugassessmentmethod) > 0">
                        <xsl:comment>G.k.9.i.2.r.2: Method of Assessment</xsl:comment>
                        <methodCode>
                            <originalText>
                                <xsl:value-of select="drugassessmentmethod"/>
                            </originalText>
                        </methodCode>
                    </xsl:if>

                    <xsl:if test="string-length(drugassessmentsource) > 0">
                        <author typeCode="AUT">
                            <assignedEntity classCode="ASSIGNED">
                                <code>
                                    <xsl:comment>G.k.9.i.2.r.1: Source of Assessment</xsl:comment>
                                    <originalText>
                                        <xsl:value-of select="drugassessmentsource"/>
                                    </originalText>
                                </code>
                            </assignedEntity>
                        </author>
                    </xsl:if>

                    <!-- Reference to Reaction, if a match is found -->
                    <xsl:variable name="reaction" select="normalize-space(../eventuniversallyuniqueid)"/>
                    <xsl:if test="string-length($reaction) > 0">
                        <xsl:comment>G.k.9.i.1: Reaction(s) / Event(s) Assessed</xsl:comment>
                        <subject1 typeCode="SUBJ">
                            <adverseEffectReference classCode="OBS" moodCode="EVN">
                                <xsl:variable name="rid">
                                    <xsl:value-of select="$reaction"/>
                                </xsl:variable>
                                <id root="{normalize-space($rid)}"/>
                            </adverseEffectReference>
                        </subject1>
                    </xsl:if>

                    <xsl:comment>Reference to Drug</xsl:comment>
                    <subject2 typeCode="SUBJ">
                        <productUseReference classCode="SBADM" moodCode="EVN">
                            <xsl:variable name="did">
                                <xsl:value-of select="normalize-space($drugRef)"/>
                            </xsl:variable>
                            <id root="{normalize-space($did)}"/>
                        </productUseReference>
                    </subject2>
                </causalityAssessment>
            </component>
        </xsl:if>

        <!--G.k.9.i.2.r.CN.1 : Primary report/MAH Source of Assessment, G.k.9.i.2.r.CN.2 : Result of Assessment/Evaluate the result. -->
        <xsl:if test="string-length(cnresultassess) + string-length(drugassessmentmethod) + string-length(cnsourceofassess) > 0">
            <component typeCode="COMP">
                <causalityAssessment classCode="OBS" moodCode="EVN">
                    <code code="{$CNResultAssess}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$nmpaoidC1CN1LVersion}" displayName="causality"/>
                    <value xsi:type="CE" code="{cnresultassess}" codeSystem="{$oidCNresultassess}" codeSystemVersion="{$nmpaoidEi7CNResultAssessCLVersion}" displayName="Reasonable possibility"/>
                    <xsl:comment>G.k.9.i.2.r.CN.2 评估结果</xsl:comment>


                    <xsl:if test="string-length(drugassessmentmethod) > 0">
                        <methodCode>
                            <originalText>
                                <xsl:value-of select="drugassessmentmethod"/>
                            </originalText>
                        </methodCode>
                    </xsl:if>

                    <xsl:if test="string-length(cnsourceofassess) > 0">
                        <author typeCode="AUT">
                            <assignedEntity classCode="ASSIGNED">
                                <code code="{cnsourceofassess}" codeSystem="{$oidCNresultassess}" codeSystemVersion="{$nmpaoidEi7CNResultAssessCLVersion}" displayName="first reporter or mah"/>
                                <xsl:comment>G.k.9.i.2.r.CN.1 评估来源</xsl:comment>
                            </assignedEntity>
                        </author>
                    </xsl:if>

                    <!-- Reference to Reaction, if a match is found -->
                    <xsl:variable name="reaction" select="normalize-space(../eventuniversallyuniqueid)"/>
                    <xsl:if test="string-length($reaction) > 0">
                        <subject1 typeCode="SUBJ">
                            <adverseEffectReference classCode="OBS" moodCode="EVN">
                                <xsl:variable name="rid">
                                    <xsl:value-of select="$reaction"/>
                                </xsl:variable>
                                <id root="{normalize-space($rid)}"/>
                            </adverseEffectReference>
                        </subject1>
                    </xsl:if>

                    <subject2 typeCode="SUBJ">
                        <productUseReference classCode="SBADM" moodCode="EVN">
                            <xsl:variable name="did">
                                <xsl:value-of select="normalize-space($drugRef)"/>
                            </xsl:variable>
                            <id root="{normalize-space($did)}"/>
                        </productUseReference>
                    </subject2>

                </causalityAssessment>
            </component>
        </xsl:if>
    </xsl:template>


    <!-- Summary :
	E2B(R2): element "summary"
	E2B(R3): element "investigationEvent"
	-->
    <xsl:template match="summary">
        <!-- H.2 Reporter's Comments -->
        <xsl:if test="string-length(reportercomment) > 0">
            <xsl:comment>H.2: Reporter's Comments</xsl:comment>
            <component1 typeCode="COMP">
                <observationEvent moodCode="EVN" classCode="OBS">
                    <code code="{$Comment}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$emaObservationCLVersion}"/>
                    <value xsi:type="ED" mediaType="text/plain">
                        <xsl:value-of select="reportercomment"/>
                    </value>
                    <author typeCode="AUT">
                        <assignedEntity classCode="ASSIGNED">
                            <code code="{$SourceReporter}" codeSystem="{$oidAssignedEntityRoleCode}"
                                  codeSystemVersion="{$emaoidAssignedEntityRoleCodeVersion}"/>
                        </assignedEntity>
                    </author>
                </observationEvent>
            </component1>
        </xsl:if>

        <!-- H.3.r Sender's diagnosis/syndrome code (repeat as necessary)-->
        <xsl:apply-templates select="senderdiagnosisinformation" mode="EMA-case-summary"/>

        <!-- H.4 Sender's Comments -->
        <xsl:if test="string-length(sendercomment) > 0">
            <xsl:comment>H.4: Sender's Comments</xsl:comment>
            <component1 typeCode="COMP">
                <observationEvent moodCode="EVN" classCode="OBS">
                    <code code="{$Comment}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$emaObservationCLVersion}"/>
                    <value xsi:type="ED" mediaType="text/plain">
                        <xsl:value-of select="sendercomment"/>
                    </value>
                    <author typeCode="AUT">
                        <assignedEntity classCode="ASSIGNED">
                            <code code="{$Sender}" codeSystem="{$oidAssignedEntityRoleCode}"
                                  codeSystemVersion="{$emaoidAssignedEntityRoleCodeVersion}"/>
                        </assignedEntity>
                    </author>
                </observationEvent>
            </component1>
        </xsl:if>

    </xsl:template>

    <!-- H.3.r Sender's Diagnosis
  ichicsr/ichicsrmessageheader/safetyreport/summary/senderdiagnosisinfo
  H.3.r.1b Sender's Diagnosis / Syndrome and / or Reclassification of Reaction / Event (MedDRA code)
  H.3.r.1a MedDRA Version for Sender's Diagnosis / Syndrome and / or Reclassification of Reaction / Event
  -->
    <xsl:template match="senderdiagnosisinformation" mode="EMA-case-summary">
        <xsl:if test="string-length(senderdiagnosis) > 0">
            <xsl:variable name="positionSendDiag">
                <xsl:value-of select="position()"/>
            </xsl:variable>
            <xsl:comment>H.3.r: Sender's Diagnosis - (<xsl:value-of select="$positionSendDiag"/>)
            </xsl:comment>
            <component1 typeCode="COMP">
                <observationEvent moodCode="EVN" classCode="OBS">
                    <code code="{$Diagnosis}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$emaObservationCLVersion}"/>
                    <xsl:comment>H.3.r.1b: Sender's Diagnosis / Syndrome and / or Reclassification of Reaction / Event
                        (MedDRA code)
                    </xsl:comment>
                    <xsl:comment>H.3.r.1a: MedDRA Version for Sender's Diagnosis / Syndrome and / or Reclassification of
                        Reaction / Event
                    </xsl:comment>
                    <value xsi:type="CE" code="{senderdiagnosis}">
                        <xsl:attribute name="codeSystem">
                            <xsl:value-of select="$oidMedDRA"/>
                        </xsl:attribute>
                        <xsl:attribute name="codeSystemVersion">
                            <xsl:value-of select="senderdiagnosismeddraversion"/>
                        </xsl:attribute>
                    </value>
                    <author typeCode="AUT">
                        <assignedEntity classCode="ASSIGNED">
                            <code code="{$Sender}" codeSystem="{$oidAssignedEntityRoleCode}"
                                  codeSystemVersion="{$emaoidAssignedEntityRoleCodeVersion}"/>
                        </assignedEntity>
                    </author>
                </observationEvent>
            </component1>
        </xsl:if>
    </xsl:template>

    <!-- H.5.r Case Summary and Reporter’s Comments in Native Language (repeat as necessary) -->
    <xsl:template match="summary/casesummarynarrative"  mode="case-summary">
        <xsl:if test="string-length(reportercommentothlang) > 0 or string-length(narrativeothlang) > 0 or string-length(sendercommentothlang) > 0" >
            <xsl:variable name="positionCaseSumNar">
                <xsl:value-of select="position()"/>
            </xsl:variable>
            <xsl:variable name='newline'><xsl:text>
		</xsl:text></xsl:variable>
            <xsl:comment>H.5.r: Case Summary and Reporter’s Comments in Native Language - (<xsl:value-of select="$positionCaseSumNar"/>)</xsl:comment>
            <component typeCode="COMP">
                <observationEvent moodCode="EVN" classCode="OBS">
                    <code code="{$SummaryAndComment}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$ichObservationCLVersion}"/>
                    <!--<xsl:comment>H.5.r.1a: Case Summary and Reporter's Comments Text </xsl:comment>
          <xsl:comment>H.5.r.1b: Case Summary and Reporter's Comments Language </xsl:comment>
          <value xsi:type="ED" language="{summaryandreportercommentslang}" mediaType="text/plain">
            <xsl:value-of select="concat(narrativekor,sendercommentkor,reportercommentkor,$newline)" />
          </value>  -->
                    <xsl:comment>H.5.r.1a: Case Summary and Reporter's Comments Text </xsl:comment>
                    <xsl:comment>H.5.r.1b: Case Summary and Reporter's Comments Language </xsl:comment>
                    <xsl:variable name="narrativeothFinal">
                        <xsl:choose>
                            <xsl:when test="string-length(narrativeothlang) > 0">
                                <xsl:value-of select="concat('Company Narrative:',narrativeothlang,$newline)" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="''" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="sendercommentothFinal">
                        <xsl:choose>
                            <xsl:when test="string-length(sendercommentothlang) > 0">
                                <xsl:value-of select="concat('Sender comments:',sendercommentothlang,$newline)" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="''" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="reportercommentothFinal">
                        <xsl:choose>
                            <xsl:when test='string-length(reportercommentothlang) > 0'>
                                <xsl:value-of select="concat('Reporter comments:',reportercommentothlang)" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="''" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <value xsi:type="ED" language="{summaryandreportercommentslang}" mediaType="text/plain">
                        <xsl:value-of select="substring(concat($narrativeothFinal,$sendercommentothFinal,$reportercommentothFinal),1,100000)" />
                    </value>
                    <author typeCode="AUT">
                        <assignedEntity classCode="ASSIGNED">
                            <code code="{$Reporter}" codeSystem="{$oidAssignedEntityRoleCode}" codeSystemVersion="{$ichoidAssignedEntityRoleCodeVersion}"/>
                        </assignedEntity>
                    </author>
                </observationEvent>
            </component>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>