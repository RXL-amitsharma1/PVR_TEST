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

                <xsl:if test="string-length(/ichicsr/mhlwadminitemsicsr/mhlwadmicsrcasenum/mhlwadmicsrcasenumb) > 0">
                    <id extension="{/ichicsr/mhlwadminitemsicsr/mhlwadmicsrcasenum/mhlwadmicsrcasenumb}" root="2.16.840.1.113883.3.989.5.1.3.2.3.1"/>
                    <xsl:comment>J2.1b: Report Identifier (Number)</xsl:comment>
                </xsl:if>

                <code code="PAT_ADV_EVNT" codeSystem="2.16.840.1.113883.5.4"/>

                <!--B.5.1 Case Narrative-->
                <xsl:apply-templates select="summary/narrativeincludeclinical" mode="narrincludeclinical"/>

                <statusCode>
                    <xsl:if test="string-length(/ichicsr/mhlwadminitemsicsr/mhlwdummy/mhlwadmicsrcompleteclassr3) > 0">
                        <xsl:attribute name="code"><xsl:value-of select="/ichicsr/mhlwadminitemsicsr/mhlwdummy/mhlwadmicsrcompleteclassr3"/></xsl:attribute>
                    </xsl:if>
                </statusCode>
                <xsl:comment>J2.7.1: Complete, Incomplete category</xsl:comment>


                <!--A.1.6 - Date Report Was First Received from Source-->
                <xsl:if test="string-length(receivedate) > 0">
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
                <xsl:apply-templates select="additionaldocuments"/>

                <!--A.4.r Literature References-->
                <xsl:apply-templates select="literature"/>

                <!--B.1.x - Patient-->
                <xsl:comment>D.1: Patient (name or initials)</xsl:comment>
                <xsl:apply-templates select="patient" mode="identification"/>
                <xsl:apply-templates select="patient/summary/narrativesendercommentnative"/>
                <!--A.1.8.1 - Are Additional Documents Available?-->

                <xsl:if test="string-length(additionaldocument) > 0">
                    <xsl:comment>C.1.6.1: Are Additional Documents Available?</xsl:comment>
                    <component typeCode="COMP">
                        <observationEvent classCode="OBS" moodCode="EVN">
                            <code code="{$AdditionalDocumentsAvailable}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$ichObservationCLVersion}"/>
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

                        <xsl:variable name="isNullFlavourExpCriteria">
                            <xsl:call-template name="isNullFlavour">
                                <xsl:with-param name="value" select="fulfillexpeditecriteria"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:choose>
                            <xsl:when test="$isNullFlavourExpCriteria = 'yes'">
                                <xsl:variable name="NullFlavourWOSqBrcktC17">
                                    <xsl:call-template name="getNFValueWithoutSqBrckt">
                                        <xsl:with-param name="nfvalue" select="fulfillexpeditecriteria"/>
                                    </xsl:call-template>
                                </xsl:variable>
                                <value xsi:type="BL" nullFlavor="{$NullFlavourWOSqBrcktC17}"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <value xsi:type="BL" value="{fulfillexpeditecriteria}"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </observationEvent>
                    <xsl:comment>C.1.7: Does This Case Fulfil the Local Criteria for an Expedited Report?</xsl:comment>
                </component>

                <xsl:apply-templates select="summary/casesummarynarrative" mode="case-summary"/>

                <!-- C.1.8.2: First Sender of This Case -->

                <xsl:if test="string-length(icsrsource)> 0">
                    <xsl:comment>C.1.8.2: First Sender of This Case</xsl:comment>
                    <outboundRelationship typeCode="SPRT">
                        <relatedInvestigation classCode="INVSTG" moodCode="EVN">
                            <code code="{$InitialReport}" codeSystem="{$oidReportRelationCode}"
                                  codeSystemVersion="{$ichReportRelationCLVersion}"/>
                            <subjectOf2 typeCode="SUBJ">
                                <controlActEvent classCode="CACT" moodCode="EVN">
                                    <author typeCode="AUT">
                                        <assignedEntity classCode="ASSIGNED">
                                            <code code="{icsrsource}" codeSystem="{$oidFirstSender}"
                                                  codeSystemVersion="{icsrsourcecsv}"/>
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
                              codeSystemVersion="{$ichReportCharacterizationCLVersion}"/>
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
                            <code code="{$NullificationAmendmentCode}" codeSystem="{$oidReportCharacterizationCode}"
                                  codeSystemVersion="{$ichReportCharacterizationCLVersion}"/>
                            <value xsi:type="CE" code="{casenullificationoramendment}"
                                   codeSystem="{$oidNullificationAmendment}"
                                   codeSystemVersion="{casenullificationoramendmentcsv}"/>
                        </investigationCharacteristic>
                    </subjectOf2>
                </xsl:if>

                <!--A.1.13.1 Reason for Nullification / Amendment-->
                <xsl:if test="string-length(nullificationoramendmentreason) > 0">
                    <xsl:comment>C.1.11.2: Reason for Nullification / Amendment</xsl:comment>
                    <subjectOf2 typeCode="SUBJ">
                        <investigationCharacteristic classCode="OBS" moodCode="EVN">
                            <code code="{$NullificationAmendmentReason}" codeSystem="{$oidReportCharacterizationCode}"
                                  codeSystemVersion="{$ichReportCharacterizationCLVersion}"/>
                            <value xsi:type="CE">
                                <originalText mediaType="text/plain">
                                    <xsl:value-of select="nullificationoramendmentreason"/>
                                </originalText>
                            </value>
                        </investigationCharacteristic>
                    </subjectOf2>
                </xsl:if>
                <!--A.1.13.1 Reason for Nullification / Amendment-->

                <xsl:if test="string-length(/ichicsr/mhlwadminitemsicsr/mhlwadmicsrcasenum/mhlwadmicsrcasenumclassr3) > 0">
                    <xsl:comment>J2.1a: Report Identifier (Category)</xsl:comment>
                    <subjectOf2 typeCode="SUBJ">
                        <investigationCharacteristic classCode="OBS" moodCode="EVN">
                            <code code="{$MhlwAdmicsrCaseNumClassr3}" codeSystem="{$pmdaoidReportingTimeFrameCode}" codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="識別番号（報告分類）"/>
                            <value xsi:type="CE" code="{/ichicsr/mhlwadminitemsicsr/mhlwadmicsrcasenum/mhlwadmicsrcasenumclassr3}" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.1" codeSystemVersion="{/ichicsr/mhlwadminitemsicsr/mhlwadmicsrcasenum/mhlwadmicsrcasenumclassr3csv}"/>
                        </investigationCharacteristic>
                    </subjectOf2>
                </xsl:if>

                <xsl:apply-templates select="/ichicsr/mhlwadminitemsicsr/mhlwdummy" mode="mhlwdummy-a"/>
            </investigationEvent>
        </subject>
    </xsl:template>

    <xsl:template match="mhlwdummy" mode="mhlwdummy-a">
        <xsl:if test="string-length(mhlwstartdatereportingtimefram) > 0">
            <xsl:comment>J2.2.1: Start date of Reporting Time Frame</xsl:comment>
            <subjectOf2 typeCode="SUBJ">
                <investigationCharacteristic classCode="OBS" moodCode="EVN">
                    <code code="{$MhlwStartDateReportingTimefram}" codeSystem="{$pmdaoidReportingTimeFrameCode}"
                          codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="報告起算日"/>
                    <value xsi:type="TS" value="{mhlwstartdatereportingtimefram}"/>
                </investigationCharacteristic>
            </subjectOf2>
        </xsl:if>

        <xsl:if test="string-length(mhlwsttdtreportingtimeframcmnt) > 0">
            <xsl:comment>J2.2.2: Comments on the Start Date of Reporting Time Frame</xsl:comment>
            <subjectOf2 typeCode="SUBJ">
                <investigationCharacteristic classCode="OBS" moodCode="EVN">
                    <code code="{$MhlwsttdtReportingTimeframCmnt}" codeSystem="{$pmdaoidReportingTimeFrameCode}"
                          codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="報告起算日に関するコメント"/>
                    <value xsi:type="ED"><xsl:value-of select="mhlwsttdtreportingtimeframcmnt"/></value>
                </investigationCharacteristic>
            </subjectOf2>
        </xsl:if>

        <xsl:if test="string-length(mhlwflagforurgentreport) > 0">
            <xsl:comment>J2.3: Flag for Urgent Report</xsl:comment>
            <subjectOf2 typeCode="SUBJ">
                <investigationCharacteristic classCode="OBS" moodCode="EVN">
                    <code code="{$MhlwFlagForUrgentReport}" codeSystem="{$pmdaoidReportingTimeFrameCode}"
                          codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="即時報告フラグ"/>
                    <value xsi:type="CE" code="{mhlwflagforurgentreport}" codeSystem="{$pmdaoidUrgentReportCode}" codeSystemVersion="{mhlwflagforurgentreportcsv}"/>
                </investigationCharacteristic>
            </subjectOf2>
        </xsl:if>

        <xsl:if test="string-length(mhlwflagforutofrptcriteria) > 0">
            <xsl:comment>J2.8.1: Non-Reportable Flag</xsl:comment>
            <subjectOf2 typeCode="SUBJ">
                <investigationCharacteristic classCode="OBS" moodCode="EVN">
                    <code code="5" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.12" codeSystemVersion="1.1" displayName="報告対象外フラグ"/>
                    <value xsi:type="CE" code="{mhlwflagforutofrptcriteria}" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.6" codeSystemVersion="{mhlwflagforutofrptcriteriacsv}"/>
                </investigationCharacteristic>
            </subjectOf2>
        </xsl:if>

        <xsl:if test="string-length(mhlwreasonforutofrptcriteria) > 0">
            <xsl:comment>J2.8.2: Reason for not being reported</xsl:comment>
            <subjectOf2 typeCode="SUBJ">
                <investigationCharacteristic classCode="OBS" moodCode="EVN">
                    <code code="6" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.12" codeSystemVersion="1.1" displayName="報告対象外の理由"/>
                    <value xsi:type="ED"><xsl:value-of select="mhlwreasonforutofrptcriteria"/></value>
                </investigationCharacteristic>
            </subjectOf2>
        </xsl:if>
    </xsl:template>

    <!--Narrative Include Clinical :
E2B(R2): element "narrativeincludeclinical"
E2B(R3): element "investigationEvent"
-->

    <xsl:template match="narrativeincludeclinical" mode="narrincludeclinical">
        <xsl:if test="string-length(.) > 0">
            <text>
                <xsl:value-of select="substring((.),1,100000)"/>
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
            <xsl:variable name="positionDoc">
                <xsl:value-of select="position()"/>
            </xsl:variable>
            <xsl:comment>C.1.6.1.r.1: Documents Held by Sender (repeat as necessary) (<xsl:value-of select="$positionDoc"/>)</xsl:comment>
            <reference typeCode="REFR">
                <document classCode="DOC" moodCode="EVN">
                    <code codeSystem="{$oidichreferencesource}" code="{$documentsHeldBySender}" codeSystemVersion="2.0"
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
                    <xsl:if test="$MediaType= 'png'">
                        <text mediaType="image/png" representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
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
                    <xsl:if test="$MediaType= 'html'">
                        <text mediaType="text/html" representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
                    <xsl:if test="$MediaType= 'psd'">
                        <text mediaType="application/octet-stream" representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
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
                    <xsl:if test="$MediaType= 'vsd'">
                        <text mediaType="application/x-visio" representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
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
                    <xsl:if test="$MediaType= 'ps'">
                        <text mediaType="application/postscript" representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
                    <xsl:if test="$MediaType= 'mdb'">
                        <text mediaType="application/x-msaccess" representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
                    <xsl:if test="$MediaType= 'bmp'">
                        <text mediaType="image/bmp" representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
                    <xsl:if test="$MediaType= 'xml'">
                        <text mediaType="text/xml" representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
                    <xsl:if test="$MediaType= 'sgm'">
                        <text mediaType="text/sgml" representation="B64" compression="DF">
                            <xsl:value-of select="includedocuments"/>
                        </text>
                        <xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
                    </xsl:if>
                    <xsl:if test="$MediaType= 'msg'">
                        <text mediaType="application/vnd.ms-outlook" representation="B64" compression="DF">
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
        <xsl:if test="string-length(duplicatesource)>0 and string-length(duplicatenumb)>0">
            <xsl:comment>C.1.9.1.r.1: Source(s) of the Case Identifier (repeat as necessary)</xsl:comment>
            <xsl:comment>C.1.9.1.r.2 Case Identifier(s)</xsl:comment>
            <subjectOf1 typeCode="SUBJ">
                <controlActEvent classCode="CACT" moodCode="EVN">
                    <!--A.1.11.r.1 Source(s) of the Case Identifier-->
                    <!--A.1.11.r.2 Case Identifier(s)-->
                    <xsl:choose>
                        <xsl:when test="string-length(duplicatesource) = 0">
                            <id assigningAuthorityName="-" extension="{duplicatenumb}" root="{$oidCaseIdentifier}"/>
                        </xsl:when>
                        <xsl:when test="string-length(duplicatenumb) = 0">
                            <id assigningAuthorityName="{duplicatesource}" extension="-" root="{$oidCaseIdentifier}"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <id assigningAuthorityName="{duplicatesource}" extension="{duplicatenumb}"
                                root="{$oidCaseIdentifier}"/>
                        </xsl:otherwise>
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
                            <xsl:comment>C.1.10.r: Identification Number of the Report Which Is Linked to This Report - (<xsl:value-of select="$positionLinkRptNum"/>)</xsl:comment>
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
                                <addr/>
                                <!--Reporter Telephone-->

                                <xsl:if test="string-length(reportertel) > 0">
                                    <xsl:comment>C.2.r.2.7: Reporter's Phone Number</xsl:comment>
                                    <telecom/>
                                </xsl:if>


                                <assignedPerson classCode="PSN" determinerCode="INSTANCE">
                                    <!--A.2.r.1.1 Reporter Identifier-->
                                    <name/>
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
                                                    <code code="{qualification}" codeSystem="{$oidQualification}"
                                                          codeSystemVersion="{qualificationcsv}"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
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
                                                                <xsl:with-param name="nfvalue"
                                                                                select="reportercountry"/>
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
                                        <name/>
                                        <xsl:comment>C.2.r.2.2:Reporter’s Department</xsl:comment>
                                        <!--<xsl:if test="string-length(reporteroraganisation) > 0">-->
                                        <assignedEntity classCode="ASSIGNED">
                                            <representedOrganization classCode="ORG" determinerCode="INSTANCE">
                                                <name/>
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
                            <telecom/>
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
                                        <code codeSystem="{$oidISOCountry}" code="{sendercountrycode}"/>
                                        <xsl:comment>C.3.4.5:Sender’sCountryCode</xsl:comment>
                                    </location>
                                </asLocatedEntity>
                            </xsl:if>
                        </assignedPerson>
                        <!--A.3.2 Sender Organization-->
                        <!--A.3.3.a Sender Department-->
                        <representedOrganization classCode="ORG" determinerCode="INSTANCE">
                            <name/>
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

                <primaryInformationRecipient typeCode="PRCP">
                    <assignedEntity classCode="ASSIGNED">
                        <xsl:if test="string-length(/ichicsr/mhlwadminitemsicsr/mhlwdummy/mhlwrecipientstitle) > 0 or string-length(/ichicsr/mhlwadminitemsicsr/mhlwdummy/mhlwrecipientsfamilyname) > 0
                        or string-length(/ichicsr/mhlwadminitemsicsr/mhlwdummy/mhlwrecipientsgivenname) > 0">
                            <assignedPerson classCode="PSN" determinerCode="INSTANCE">
                                <name>
                                    <xsl:if test="string-length(/ichicsr/mhlwadminitemsicsr/mhlwdummy/mhlwrecipientstitle) > 0">
                                        <xsl:comment>J2.18.2: Recipient's Title</xsl:comment>
                                        <prefix>
                                            <xsl:value-of select="/ichicsr/mhlwadminitemsicsr/mhlwdummy/mhlwrecipientstitle"/>
                                        </prefix>
                                    </xsl:if>
                                    <xsl:if test="string-length(/ichicsr/mhlwadminitemsicsr/mhlwdummy/mhlwrecipientsfamilyname) > 0">
                                        <xsl:comment>J2.18.3: Recipient’s Family Name</xsl:comment>
                                        <family>
                                            <xsl:value-of select="/ichicsr/mhlwadminitemsicsr/mhlwdummy/mhlwrecipientsfamilyname"/>
                                        </family>
                                    </xsl:if>
                                    <xsl:if test="string-length(/ichicsr/mhlwadminitemsicsr/mhlwdummy/mhlwrecipientsgivenname) > 0">
                                        <xsl:comment>J2.18.4: Recipient’s Given Name</xsl:comment>
                                        <given>
                                            <xsl:value-of select="/ichicsr/mhlwadminitemsicsr/mhlwdummy/mhlwrecipientsgivenname"/>
                                        </given>
                                    </xsl:if>
                                </name>
                            </assignedPerson>
                        </xsl:if>
                        <xsl:if test="string-length(/ichicsr/mhlwadminitemsicsr/mhlwdummy/mhlwrecipientsorg) > 0">
                            <representedOrganization classCode="ORG" determinerCode="INSTANCE">
                                <xsl:comment>J2.18.1: Recipient's Organisation Name</xsl:comment>
                                <name>
                                    <xsl:value-of select="/ichicsr/mhlwadminitemsicsr/mhlwdummy/mhlwrecipientsorg"/>
                                </name>
                            </representedOrganization>
                        </xsl:if>
                    </assignedEntity>
                </primaryInformationRecipient>

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
                              codeSystemVersion="{$ichoidObservationCLVersion}"/>
                        <xsl:comment>D.10.2.2a: Age of Parent (number)</xsl:comment>
                        <xsl:comment>D.10.2.2b: Age of Parent (unit)</xsl:comment>
                        <value xsi:type="PQ" value="{parentage}" unit="{parentageunit}"/>
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
                              codeSystemVersion="{$ichoidObservationCLVersion}"/>
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

            <!-- D.10.4 Body Weight (kg) of Parent -->
            <xsl:if test="string-length(parentweight) > 0">
                <xsl:comment>D.10.4: Body Weight (kg) of Parent</xsl:comment>
                <subjectOf2 typeCode="SBJ">
                    <observation moodCode="EVN" classCode="OBS">
                        <code code="{$BodyWeight}" codeSystem="{$oidObservationCode}"
                              codeSystemVersion="{$ichoidObservationCLVersion}"/>
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
                              codeSystemVersion="{$ichoidObservationCLVersion}"/>
                        <value xsi:type="PQ" value="{parentheight}" unit="cm"/>
                    </observation>
                </subjectOf2>
            </xsl:if>

            <!-- D.10.7 Relevant Medical History and Concurrent Conditions of Parent -->
            <xsl:if test="parentmedicalhistoryepisode | parentmedicalrelevanttext">
                <xsl:comment>D.10.7: Relevant Medical History and Concurrent Conditions of Parent</xsl:comment>
                <subjectOf2 typeCode="SBJ">
                    <organizer classCode="CATEGORY" moodCode="EVN">
                        <code code="{$RelevantMedicalHistoryAndConcurrentConditions}"
                              codeSystem="{$oidValueGroupingCode}" codeSystemVersion="{$ichValueGroupingCLVersion}"/>


                        <!-- D.10.7.1.r Structured Information of Parent (repeat as necessary) -->
                        <xsl:if test="string-length(parentmedicalhistoryepisode) > 0">
                            <xsl:apply-templates select="parentmedicalhistoryepisode" mode="EMA-par-structured-info"/>
                        </xsl:if>
                        <!-- D.10.7.2 Text for Relevant Medical History and Concurrent Conditions of Parent -->
                        <xsl:if test="string-length(parentmedicalrelevanttext) > 0">
                            <xsl:comment>D.10.7.2: Text for relevant medical history and concurrent conditions of parent (not including reaction/event)</xsl:comment>
                            <component typeCode="COMP">
                                <observation moodCode="EVN" classCode="OBS">
                                    <code code="{$HistoryAndConcurrentConditionText}" codeSystem="{$oidObservationCode}"
                                          codeSystemVersion="{$ichoidObservationCLVersion}"/>
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
                              codeSystemVersion="{$ichValueGroupingCLVersion}"/>
                        <xsl:apply-templates select="parentpastdrugtherapy" mode="ICH-par-past-drug-hist"/>
                    </organizer>
                </subjectOf2>
            </xsl:if>

        </role>
    </xsl:template>


    <xsl:template match="parentpastdrugtherapy" mode="ICH-par-past-drug-hist">
        <xsl:variable name="positionParDrugHist">
            <xsl:value-of select="position()"/>
        </xsl:variable>
        <xsl:comment>D.10.8.r: Relevant Past Drug History of Parent - (<xsl:value-of select="$positionParDrugHist"/>)</xsl:comment>
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
                                        <xsl:if test="string-length(parentmpid) > 0">
                                            <xsl:comment>D.10.8.r.2a: MPID Version Date / Number</xsl:comment>
                                            <xsl:comment>D.10.8.r.2b: Medicinal Product Identifier (MPID)</xsl:comment>
                                            <code code="{parentmpid}" codeSystem="MPID"
                                                  codeSystemVersion="{parentmpidversion}"/>
                                        </xsl:if>
                                        <xsl:if test="string-length(parentphpid) > 0">
                                            <xsl:comment>D.10.8.r.3a: PhPID Version Date/Number</xsl:comment>
                                            <xsl:comment>D.10.8.r.3b: Pharmaceutical Product Identifier (PhPID)</xsl:comment>
                                            <code code="{parentphpid}" codeSystem="PhPID"
                                                  codeSystemVersion="{parentphpidversion}"/>
                                        </xsl:if>
                                        <xsl:comment>D.10.8.r.1: Name of Drug as Reported-</xsl:comment>
                                        <name>
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
                                  codeSystemVersion="{$ichoidObservationCLVersion}" displayName="indication"/>
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
                                  codeSystemVersion="{$ichoidObservationCLVersion}" displayName="Parent Past Reaction"/>
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
                    <xsl:comment>D.2.2.1a: Gestation Period When Reaction / Event Was Observed in the Foetus (number)</xsl:comment>
                    <xsl:comment>D.2.2.1b: Gestation Period When Reaction / Event Was Observed in the Foetus (unit)</xsl:comment>
                </observation>
            </subjectOf2>
        </xsl:if>
        <!--B.1.2.3. Age Group-->
        <xsl:if test="string-length(patientagegroup)>0">
            <subjectOf2 typeCode="SBJ">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$AgeGroup}" codeSystem="{$oidObservationCode}" codeSystemVersion="2.0" displayName="ageGroup"/>
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
        <!-- <xsl:apply-templates select="." mode="ICH-pat-characteristics"/> -->
        <xsl:if test="count(medicalhistoryepisode) > 0 or string-length(patientmedicalhistorytext) > 0">
            <subjectOf2 typeCode="SBJ">
                <organizer classCode="CATEGORY" moodCode="EVN">
                    <code code="{$RelevantMedicalHistoryAndConcurrentConditions}" codeSystem="{$oidValueGroupingCode}"
                          codeSystemVersion="{$ichValueGroupingCLVersion}"/>

                    <!-- D.7.1.r - Structured Information on Relevant Medical History (repeat as necessary) -->
                    <xsl:if test="count(medicalhistoryepisode) > 0 ">
                        <xsl:apply-templates select="medicalhistoryepisode" mode="EMA-pat-medical-history-episode"/>
                    </xsl:if>

                    <!-- D.7.2 Text for Relevant Medical History and Concurrent Conditions (not including reaction / event) -->
                    <xsl:if test="string-length(patientmedicalhistorytext) > 0">
                        <xsl:comment>D.7.2: Text for Relevant Medical History and Concurrent Conditions (not including reaction / event)</xsl:comment>
                        <xsl:variable name="isNullFlavourPatMedHist">
                            <xsl:call-template name="isNullFlavour">
                                <xsl:with-param name="value" select="patientmedicalhistorytext"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <component typeCode="COMP">
                            <observation moodCode="EVN" classCode="OBS">
                                <code code="{$HistoryAndConcurrentConditionText}" codeSystem="{$oidObservationCode}"
                                      codeSystemVersion="{$ichoidObservationCLVersion}"/>
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
                                      codeSystemVersion="{$ichoidD73CLVersion}"/>
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
                          codeSystemVersion="{$ichValueGroupingCLVersion}"/>
                    <xsl:apply-templates select="patientpastdrugtherapy" mode="ICH-pat-past-drug-hist"/>
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
                          codeSystemVersion="{$ichoidObservationCLVersion}"/>
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
                        <xsl:apply-templates select="patientautopsy" mode="ICH-pat-autopsy-determined"/>
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
                <code code="{$code}" codeSystem="{$codeSystem}" codeSystemVersion="{$ichSourceMedicalRecordCLVersion}"/>
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

    <xsl:template match="patientpastdrugtherapy" mode="ICH-pat-past-drug-hist">
        <xsl:variable name="positionPatPastDrug">
            <xsl:value-of select="position()"/>
        </xsl:variable>
        <xsl:comment>D.8.r: Relevant Past Drug History - (<xsl:value-of select="$positionPatPastDrug"/>)</xsl:comment>
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
                                        <xsl:if test="string-length(patientmpid) > 0">
                                            <xsl:comment>D.8.r.2b: Medicinal Product Identifier (MPID)</xsl:comment>
                                            <xsl:comment>D.8.r.2a: MPID Version Date/Number</xsl:comment>
                                            <code code="{patientmpid}" codeSystem="MPID"
                                                  codeSystemVersion="{patientmpidversion}"/>
                                        </xsl:if>
                                        <xsl:if test="string-length(patientphpid) > 0">
                                            <xsl:comment>D.8.r.3a: PhPID Version Date/Number</xsl:comment>
                                            <xsl:comment>D.8.r.3b: Pharmaceutical Product Identifier (PhPID)</xsl:comment>
                                            <code code="{patientphpid}" codeSystem="PhPID"
                                                  codeSystemVersion="{patientphpidversion}"/>
                                        </xsl:if>
                                        <xsl:comment>D.8.r.1: Name of Drug as Reported</xsl:comment>
                                        <name><xsl:value-of select="patientdrugname"/></name>
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
                                  codeSystemVersion="{$ichoidObservationCLVersion}" displayName="Patient Past indication"/>
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
                                  codeSystemVersion="{$ichoidObservationCLVersion}" displayName="Patient Past Reaction"/>
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
    <xsl:template match="patientautopsy" mode="ICH-pat-autopsy-determined">
        <xsl:variable name="positionPatAutopsy">
            <xsl:value-of select="position()"/>
        </xsl:variable>
        <xsl:comment>D.9.4.r: Autopsy-determined Cause(s) of Death - (<xsl:value-of select="$positionPatAutopsy"/>)</xsl:comment>
        <outboundRelationship2 typeCode="DRIV">
            <observation moodCode="EVN" classCode="OBS">
                <code code="{$CauseOfDeath}" codeSystem="{$oidObservationCode}"
                      codeSystemVersion="{$ichoidObservationCLVersion}"/>
                <!-- D.9.4.r.1a: MedDRA Version for Autopsy-determined Cause(s) of Death -->
                <!-- D.9.4.r.1b Autopsy-determined Cause(s) of Death (MedDRA code)-->
                <xsl:if test="string-length(patientdeterminedautopsy) > 0 or string-length(patientdeterminedautopsytxt) > 0">
                    <xsl:if test="string-length(patientdeterminedautopsy) > 0 ">
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
                            <xsl:if test="string-length(patientdeterminedautopsymeddraversion) > 0">
                                <xsl:attribute name="codeSystemVersion">
                                    <xsl:value-of select="patientdeterminedautopsymeddraversion"/>
                                </xsl:attribute>
                            </xsl:if>
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

    <!-- Reaction Block (ICH) E2B R3-->

    <xsl:template match="reaction">
        <xsl:if test="string-length(eventuniversallyuniqueid) > 0">
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
                <code code="{$Reaction}" codeSystem="{$oidObservationCode}"
                      codeSystemVersion="{$ichoidObservationCLVersion}"/>
                <xsl:if test="string-length(reactionstartdate) > 0 or string-length(reactionenddate) > 0 or string-length(reactionduration) > 0">
                    <xsl:choose>
                        <xsl:when
                                test="string-length(reactionstartdate) = 0 or string-length(reactionenddate) = 0 or string-length(reactionduration) = 0">
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
                                    <width value="{reactionduration}" unit="{reactiondurationunit}"/>

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
                            <xsl:if test="string-length(reactionmeddraversion) > 0">
                                <xsl:attribute name="codeSystemVersion">
                                    <xsl:value-of select="reactionmeddraversion"/>
                                </xsl:attribute>
                            </xsl:if>
                        </xsl:if>

                        <xsl:if test="string-length(primarysourcereactionnative) > 0">
                            <xsl:comment>E.i.1.1a: Reaction / Event as Reported by the Primary Source</xsl:comment>
                            <xsl:comment>E.i.1.1b: Reaction / Event as Reported by the Primary Source Language</xsl:comment>
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
                    <xsl:comment>E.i.9: Identification of the Country Where the Reaction / Event Occurred</xsl:comment>
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
                    <xsl:comment>E.i.1.2: Reaction / Event as Reported by the Primary Source for Translation</xsl:comment>
                    <outboundRelationship2 typeCode="PERT">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="{$ReactionForTranslation}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$ichoidObservationCLVersion}"/>
                            <value xsi:type="ED">
                                <xsl:value-of select="primarysourcereaction"/>
                            </value>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <!-- B.2.i.2.1 Term Highlighted by Reporter -->
                <xsl:if test="string-length(termhighlighted) > 0">
                    <xsl:comment>E.i.3.1: Term Highlighted by Reporter</xsl:comment>
                    <outboundRelationship2 typeCode="PERT">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="{$TermHighlightedByReporter}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$ichoidObservationCLVersion}"/>
                            <value xsi:type="CE" code="{termhighlighted}" codeSystem="{$oidTermHighlighted}"
                                   codeSystemVersion="{termhighlightedcsv}"/>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <xsl:comment>E.i.3.2a: Results in Death</xsl:comment>
                <outboundRelationship2 typeCode="PERT">
                    <observation moodCode="EVN" classCode="OBS">
                        <code code="{$ResultsInDeath}" codeSystem="{$oidObservationCode}"
                              codeSystemVersion="{$ichoidObservationCLVersion}"/>
                        <xsl:call-template name="seriousness-criteria">
                            <xsl:with-param name="value" select="seriousnessdeath"/>
                        </xsl:call-template>
                    </observation>
                </outboundRelationship2>

                <xsl:comment>E.i.3.2b: Life Threatening</xsl:comment>
                <outboundRelationship2 typeCode="PERT">
                    <observation moodCode="EVN" classCode="OBS">
                        <code code="{$LifeThreatening}" codeSystem="{$oidObservationCode}"
                              codeSystemVersion="{$ichoidObservationCLVersion}"/>
                        <xsl:call-template name="seriousness-criteria">
                            <xsl:with-param name="value" select="seriousnesslifethreatening"/>
                        </xsl:call-template>
                    </observation>
                </outboundRelationship2>

                <xsl:comment>E.i.3.2c: Caused / Prolonged Hospitalisation</xsl:comment>
                <outboundRelationship2 typeCode="PERT">
                    <observation moodCode="EVN" classCode="OBS">
                        <code code="{$CausedProlongedHospitalisation}" codeSystem="{$oidObservationCode}"
                              codeSystemVersion="{$ichoidObservationCLVersion}"/>
                        <xsl:call-template name="seriousness-criteria">
                            <xsl:with-param name="value" select="seriousnesshospitalization"/>
                        </xsl:call-template>
                    </observation>
                </outboundRelationship2>

                <xsl:comment>E.i.3.2d: Disabling / Incapacitating</xsl:comment>
                <outboundRelationship2 typeCode="PERT">
                    <observation moodCode="EVN" classCode="OBS">
                        <code code="{$DisablingIncapaciting}" codeSystem="{$oidObservationCode}"
                              codeSystemVersion="{$ichoidObservationCLVersion}"/>
                        <xsl:call-template name="seriousness-criteria">
                            <xsl:with-param name="value" select="seriousnessdisabling"/>
                        </xsl:call-template>
                    </observation>
                </outboundRelationship2>

                <xsl:comment>E.i.3.2e: Congenital Anomaly / Birth Defect</xsl:comment>
                <outboundRelationship2 typeCode="PERT">
                    <observation moodCode="EVN" classCode="OBS">
                        <code code="{$CongenitalAnomalyBirthDefect}" codeSystem="{$oidObservationCode}"
                              codeSystemVersion="{$ichoidObservationCLVersion}"/>
                        <xsl:call-template name="seriousness-criteria">
                            <xsl:with-param name="value" select="seriousnesscongenitalanomali"/>
                        </xsl:call-template>
                    </observation>
                </outboundRelationship2>

                <xsl:comment>E.i.3.2f: Other Medically Important Condition</xsl:comment>
                <outboundRelationship2 typeCode="PERT">
                    <observation moodCode="EVN" classCode="OBS">
                        <code code="{$OtherMedicallyImportantCondition}" codeSystem="{$oidObservationCode}"
                              codeSystemVersion="{$ichoidObservationCLVersion}"/>
                        <xsl:call-template name="seriousness-criteria">
                            <xsl:with-param name="value" select="seriousnessother"/>
                        </xsl:call-template>
                    </observation>
                </outboundRelationship2>

                <xsl:if test="string-length(reactionoutcome)> 0 ">
                    <xsl:comment>E.i.7: Outcome of Reaction / Event at the Time of Last Observation</xsl:comment>
                    <outboundRelationship2 typeCode="PERT">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="{$Outcome}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$ichoidObservationCLVersion}"/>
                            <value xsi:type="CE" code="{reactionoutcome}" codeSystem="{$oidOutcome}"
                                   codeSystemVersion="{reactionoutcomecsv}"/>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <xsl:if test="string-length(reactionmedconfirmed) > 0">
                    <xsl:comment>E.i.8: Medical Confirmation by Healthcare Professional</xsl:comment>
                    <outboundRelationship2 typeCode="PERT">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="{$MedicalConfirmationByHP}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$ichoidObservationCLVersion}"/>
                            <value xsi:type="BL" value="{reactionmedconfirmed}"/>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>


                <xsl:if test="string-length(mhlwreactionknownunknown) > 0">
                    <xsl:comment>J2.14.i: Unknown / Known (<xsl:value-of select="$positionevents"/>)</xsl:comment>
                    <outboundRelationship2 typeCode="PERT">
                        <observation moodCode="EVN" classCode="OBS">
                            <code code="11" codeSystem="{$pmdaoidRetrospectiveAnalysisCode}" codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="未知・既知"/>
                            <value xsi:type="CE" code="{mhlwreactionknownunknown}" codeSystem="{$oidKnownUnknownCode}" codeSystemVersion="{$pmdaoidObservationCLVersion}"/>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>
            </observation>
        </subjectOf2>
        </xsl:if>
    </xsl:template>


    <!-- F.r Results of Tests and Procedures Relevant to the Investigation of the Patient (repeat as necessary)
	E2B(R2): element "test" - "ichicsr\ichicsrbatchheader\ichicsrmessageheader\safetyreport\test"
	E2B(R3): element ""
	-->
    <xsl:template match="test" mode="EMA-lab-test">
        <xsl:if test="string-length(testdate) > 0 or string-length(testname) > 0 or string-length(testmeddracode) > 0 or string-length(testresultcomments) >0">
            <xsl:variable name="positionLabTest">
                <xsl:value-of select="position()"/>
            </xsl:variable>
            <xsl:comment>F.r: Results of Tests and Procedures Relevant to the Investigation of the Patient - (<xsl:value-of select="$positionLabTest"/>)</xsl:comment>
            <component typeCode="COMP">
                <observation moodCode="EVN" classCode="OBS">
                    <xsl:choose>
                        <xsl:when test="string-length(testmeddracode) > 0 ">
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
                                                codeSystemVersion="{testresultcsv}"/>
                        </xsl:when>
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
                                  codeSystemVersion="{$ichoidObservationCLVersion}" displayName="comment"/>
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
                                  codeSystemVersion="{$ichoidObservationCLVersion}"
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
        <xsl:comment>G.k: DRUG(S) INFORMATION - (<xsl:value-of select="$positionDrug"/>)</xsl:comment>
        <component typeCode="COMP">
            <substanceAdministration moodCode="EVN" classCode="SBADM">
                <xsl:comment>G.k[GID]: Drug UUID</xsl:comment>
                <id root="{druguniversallyuniqueid}"/>
                <!--<id root="DID{position()}"/>  -->
                <consumable typeCode="CSM">
                    <instanceOfKind classCode="INST">
                        <kindOfProduct classCode="MMAT" determinerCode="KIND">
                            <xsl:if test="string-length(drugmpid) > 0">
                                <xsl:comment>G.k.2.1.1a: MPID Version Date / Number</xsl:comment>
                                <xsl:comment>G.k.2.1.1b: Medicinal Product Identifier (MPID)</xsl:comment>
                                <code code="{drugmpid}" codeSystem="MPID" codeSystemVersion="{drugmpidversion}"
                                      displayName="Medicinal Product Identifier and Version"/>
                            </xsl:if>
                            <xsl:if test="string-length(drugphpid) > 0">
                                <xsl:comment>G.k.2.1.2a: PhPID Version Date / Number</xsl:comment>
                                <xsl:comment>G.k.2.1.2b: Pharmaceutical Product Identifier (PhPID)</xsl:comment>
                                <code code="{drugphpid}" codeSystem="PhPID" codeSystemVersion="{drugphpidversion}"
                                      displayName="Pharmaceutical Product Identifier and Version"/>
                            </xsl:if>

                            <name> <xsl:value-of select="medicinalproduct"/> </name>
                            <xsl:comment>G.k.2.2: Medicinal Product Name as Reported by the Primary Source</xsl:comment>

                            <asManufacturedProduct classCode="MANU">
                                <xsl:comment>G.k.3: Holder and Authorisation / Application Number of Drug</xsl:comment>
                                <xsl:if test="string-length(drugauthorizationnumb) > 0 or string-length(drugauthorizationcountry) > 0 or string-length(drugauthorizationholder) > 0 or string-length(mhlwriskcategoryofotcdrugs) > 0">
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
                                                            <xsl:comment>G.k.3.2: Country of Authorisation / Application</xsl:comment>
                                                            <code codeSystem="{$oidISOCountry}"
                                                                  code="{drugauthorizationcountry}"/>
                                                        </territory>
                                                    </territorialAuthority>
                                                </author>
                                            </xsl:if>

                                            <xsl:if test="string-length(mhlwriskcategoryofotcdrugs) > 0">
                                                <xsl:comment>J2.5.k: Risk Category of Over-the-Counter drugs, etc</xsl:comment>
                                                <pertinentInformation typeCode="PERT">
                                                    <policy classCode="POLICY" moodCode="EVN">
                                                        <code code="{mhlwriskcategoryofotcdrugs}" codeSystem="{$pmdaoidDrugCategoryRiskCode}" codeSystemVersion="{mhlwriskcategoryofotcdrugscsv}"/>
                                                    </policy>
                                                </pertinentInformation>
                                            </xsl:if>
                                        </approval>
                                    </subjectOf>

                                </xsl:if>


                                <!-- J2.4.k: Status category of new drugs -->
                                <xsl:if test="string-length(mhlwstatuscategoryofnewdrugs) > 0">
                                    <xsl:comment>J2.4.k: Status category of new drugs</xsl:comment>
                                    <subjectOf typeCode="SBJ">
                                        <characteristic classCode="OBS" moodCode="EVN">
                                            <code code="{$MhlwStatusCategoryOfNewDrugs}" codeSystem="{$pmdaoidRetrospectiveAnalysisCode}" codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="新医薬品等の状況区分"/>
                                            <value xsi:type="CE" code="{mhlwstatuscategoryofnewdrugs}" codeSystem="{$pmdaoidDrugCategoryStatusCode}" codeSystemVersion="{mhlwstatuscategoryofnewdrugscsv}"/>
                                        </characteristic>
                                    </subjectOf>
                                </xsl:if>
                            </asManufacturedProduct>

                            <!-- J2.12: Clinical Compound Number -->
                            <xsl:if test="string-length(../../mhlwadminitemsicsr/mhlwdummy/mhlwcompoundnum) > 0 and position() = 1">
                                <xsl:comment>J2.12: Clinical Compound Number</xsl:comment>
                                <asSpecializedKind classCode="GEN">
                                    <generalizedMaterialKind classCode="MAT" determinerCode="KIND">
                                        <code>
                                            <originalText>
                                                <xsl:value-of select="../../mhlwadminitemsicsr/mhlwdummy/mhlwcompoundnum"/>
                                            </originalText>
                                        </code>
                                    </generalizedMaterialKind>
                                </asSpecializedKind>
                            </xsl:if>

                            <!-- G.k.2.3.r Substance / Specified Substance Identifier and Strength (repeat as necessary) -->
                            <xsl:if test="count(activesubstance) > 0">
                                <xsl:apply-templates select="activesubstance" mode="ICH-drug-ingredients"/>
                            </xsl:if>

                        </kindOfProduct>

                        <!-- G.k.2.4 Identification of the Country Where the Drug Was Obtained -->
                        <xsl:if test="string-length(obtaindrugcountry) > 0">
                            <xsl:comment>G.k.2.4: Identification of the Country Where the Drug Was Obtained</xsl:comment>
                            <subjectOf typeCode="SBJ">
                                <productEvent classCode="ACT" moodCode="EVN">
                                    <code code="{$RetailSupply}" codeSystem="{$oidActionPerformedCode}"
                                          codeSystemVersion="1.0" displayName="retailSupply"/>
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

                        <xsl:if test="string-length(mhlwrouteforacquiringotcdrugs) > 0 " >
                            <xsl:comment>J2.6.k: Routes for obtaining over-the-counter drugs</xsl:comment>
                            <subjectOf typeCode="SBJ">
                                <observationEvent classCode="OBS" moodCode="EVN">
                                    <code code="2" codeSystem="{$pmdaoidRetrospectiveAnalysisCode}" codeSystemVersion="1.1" displayName="一般用医薬品の入手経路"/>
                                    <value xsi:type="CE" code="{mhlwrouteforacquiringotcdrugs}" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.5" codeSystemVersion="{mhlwrouteforacquiringotcdrugscsv}"/>
                                </observationEvent>
                            </subjectOf>
                        </xsl:if>
                    </instanceOfKind>
                </consumable>

                <xsl:if test="count(drugrelatedness) > 0">
                    <xsl:apply-templates select="drugrelatedness" mode="EMA-interval"/>
                </xsl:if>

                <!--G.k.2.5 Investigational Product Blinded-->
                <xsl:if test="string-length(investigationalblindedproduct) > 0">
                    <xsl:comment>G.k.2.5: Investigational Product Blinded</xsl:comment>
                    <outboundRelationship2 typeCode="PERT">
                        <observation classCode="OBS" moodCode="EVN">
                            <code code="{$Blinded}" codeSystem="{$oidObservationCode}"
                                  codeSystemVersion="{$ichoidObservationCLVersion}"/>
                            <value xsi:type="BL" value="{investigationalblindedproduct}"/>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <!--G.k.4.r Dosage and Relevant Information (repeat as necessary)-->
                <xsl:if test="count(drugdosage) > 0">
                    <xsl:apply-templates select="drugdosage" mode="ICH-drug-dosage-info"/>
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
                                  codeSystemVersion="{$ichoidObservationCLVersion}"
                                  displayName="cumulativeDoseToReaction"/>
                            <value xsi:type="PQ" value="{drugcumulativedosagenumb}" unit="{drugcumulativedosageunit}"/>
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
                                  codeSystemVersion="{$ichoidObservationCLVersion}"/>
                            <value xsi:type="PQ" value="{reactiongestationperiod}"
                                   unit="{reactiongestationperiodunit}"/>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <!-- G.k.9.i.4 Did Reaction Recur on Re-administration? -->
                <xsl:if test="count(drugrelatedness) > 0">
                    <xsl:apply-templates select="drugrelatedness" mode="ICH-recur"/>
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
                                  codeSystemVersion="{$ichoidObservationCLVersion}"/>
                            <xsl:comment>G.k.11: Additional Information on Drug (free text)</xsl:comment>
                            <value xsi:type="ST">
                                <xsl:value-of select="drugadditionaltext"/>
                            </value>
                        </observation>
                    </outboundRelationship2>
                </xsl:if>

                <!-- G.k.7.r Indication for Use in Case (repeat as necessary) -->
                <xsl:if test="count(drugindications) > 0">
                    <xsl:apply-templates select="drugindications" mode="ICH-drug-indication"/>
                </xsl:if>

                <!-- G.k.8 Action(s) Taken with Drug -->
                <xsl:if test="string-length(actiondrug) > 0">
                    <xsl:comment>G.k.8: Action(s) Taken with Drug</xsl:comment>
                    <inboundRelationship typeCode="CAUS">
                        <act classCode="ACT" moodCode="EVN">
                            <code code="{actiondrug}" codeSystem="{$oidActionTaken}"
                                  codeSystemVersion="{$ichoidGk8CLVersion}"/>
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
    <xsl:template match="activesubstance" mode="ICH-drug-ingredients">
        <xsl:if test="string-length(activesubstancename)>0">
            <xsl:variable name="positionActiveSub">
                <xsl:value-of select="position()"/>
            </xsl:variable>
            <xsl:comment>G.k.2.3.r: Substance / Specified Substance Identifier and Strength - (<xsl:value-of select="$positionActiveSub"/>)</xsl:comment>
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
                    <xsl:comment>G.k.2.3.r.2a: Substance / Specified Substance TermID Version Date / Number</xsl:comment>
                    <xsl:comment>G.k.2.3.r.2b: Substance / Specified Substance TermID</xsl:comment>
                    <xsl:if test="string-length(activesubstancetermid) > 0 ">
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

    <xsl:template match="drugindications" mode="ICH-drug-indication">
        <xsl:if test="string-length(drugindicationterm) > 0">
            <xsl:variable name="positionDrugInd">
                <xsl:value-of select="position()"/>
            </xsl:variable>
            <xsl:variable name="isNullFlavourDrugIndication">
                <xsl:call-template name="isNullFlavour">
                    <xsl:with-param name="value" select="drugindicationterm"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:comment>G.k.7.r: Indication for Use in Case - (<xsl:value-of select="$positionDrugInd"/>)</xsl:comment>
            <inboundRelationship typeCode="RSON">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$Indication}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$ichoidObservationCLVersion}"/>
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
                                <xsl:when test="string-length(drugindication) > 0 ">
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
                                        <xsl:comment>G.k.7.r.1: Indication as Reported by the Primary Source</xsl:comment>
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
                                  codeSystemVersion="{$ichoidAssignedEntityCodeVersion}"
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

    <xsl:template match="drugrelatedness" mode="ICH-recur">
        <xsl:if test="string-length(eventuniversallyuniqueid) > 0">
        <xsl:if test="string-length(drugrecurreadministration)>0">
            <outboundRelationship2 typeCode="PERT">
                <observation moodCode="EVN" classCode="OBS">
                    <code code="{$RecurranceOfReaction}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$ichoidObservationCLVersion}"/>
                    <xsl:comment>G.k.9.i.4: Did Reaction Recur on Re-administration?</xsl:comment>
                    <value xsi:type="CE" code="{drugrecurreadministration}" codeSystem="{$oidRechallenge}"
                           codeSystemVersion="{$ichoidGk9i4CLVersion}"/>
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
        </xsl:if>
    </xsl:template>

    <!-- G.k.4.r Dosage and Relevant Information (repeat as necessary)
	E2B(R2): element "drugdosage" - "ichicsr/ichicsrbatchheader/ichicsrmessageheader/safetyreport/drug/drugdosage"
	E2B(R3): element ""
	-->
    <xsl:template match="drugdosage" mode="ICH-drug-dosage-info">
        <xsl:variable name="positionDrugDosageInfo">
            <xsl:value-of select="position()"/>
        </xsl:variable>
        <xsl:comment>G.k.4.r: Dosage and Relevant Information - (<xsl:value-of select="$positionDrugDosageInfo"/>)</xsl:comment>
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
                                </xsl:if>
                                <xsl:if test="string-length(drugintervaldosageunitnumb) = 0 and string-length(drugintervaldosagedefinition) > 0">
                                    <xsl:comment>G.k.4.r.3: Definition of the Time Interval Unit</xsl:comment>
                                    <period unit="{drugintervaldosagedefinition}"/>
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
                                <xsl:when test="string-length(drugadministrationtermid) > 0 ">
                                    <xsl:comment>G.k.4.r.10.2a: Route of Administration TermID Version Date / Number</xsl:comment>
                                    <xsl:comment>G.k.4.r.10.2b: Route of Administration TermID</xsl:comment>
                                    <routeCode code="{drugadministrationtermid}" codeSystem="2.16.840.1.113883.3.989.2.1.1.14"
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
                                            <xsl:when test="string-length(drugdosageformtermid) > 0 and string-length(drugdosageform) > 0">
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
                                  codeSystemVersion="{$ichoidObservationCLVersion}"/>
                            <xsl:choose>
                                <xsl:when test="$isNullFlavourParRouteOfAdmin = 'yes'">
                                    <xsl:variable name="NullFlavourWOSqBrcktGk4r111">
                                        <xsl:call-template name="getNFValueWithoutSqBrckt">
                                            <xsl:with-param name="nfvalue" select="drugparadministration"/>
                                        </xsl:call-template>
                                    </xsl:variable>
                                    <xsl:comment>G.k.4.r.11.1: Parent Route of Administration (free text)</xsl:comment>
                                    <value xsi:type="CE" codeSystem="{$oidICHRoute}"
                                           nullFlavor="{$NullFlavourWOSqBrcktGk4r111}"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:choose>
                                        <xsl:when test="string-length(drugparadministrationtermid) > 0 ">
                                            <xsl:comment>G.k.4.r.11.2a: Parent Route of Administration TermID Version Date / Number</xsl:comment>
                                            <xsl:comment>G.k.4.r.11.2b: Parent Route of Administration TermID</xsl:comment>
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
                                            <xsl:comment>G.k.4.r.11.1: Parent Route of Administration (free text)</xsl:comment>
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

            </substanceAdministration>
        </outboundRelationship2>
    </xsl:template>


    <!--G.k.9.i.2.r Assessment of Relatedness of Drug to Reaction(s) / Event(s) (repeat as necessary)
	E2B(R2): element "drugassesment" - "ichicsr/ichicsrbatchheader/ichicsrmessageheader/safetyreport/drug/drugeventmatrix"
	E2B(R3): element "causalityAssessment"
	-->
    <xsl:template match="drugassesment" mode="EMA-drug-reaction-relatedness">
        <xsl:param name="drugRef"/>
        <xsl:if test="string-length(eventuniversallyuniqueid) > 0">
        <xsl:if test="string-length(drugassessmentsource) + string-length(drugassessmentmethod) + string-length(drugresult) > 0">
            <xsl:variable name="positionDrugReactRel">
                <xsl:value-of select="position()"/>
            </xsl:variable>
            <xsl:comment>G.k.9.i.2.r: Relatedness of Drug to Reaction(s) / Event(s) - (<xsl:value-of select="$positionDrugReactRel"/>)</xsl:comment>
            <component typeCode="COMP">
                <causalityAssessment classCode="OBS" moodCode="EVN">
                    <code code="{$Causality}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$ichoidObservationCLVersion}"/>

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
                          codeSystemVersion="{$ichoidObservationCLVersion}"/>
                    <value xsi:type="ED" mediaType="text/plain">
                        <xsl:value-of select="reportercomment"/>
                    </value>
                    <author typeCode="AUT">
                        <assignedEntity classCode="ASSIGNED">
                            <code code="{$SourceReporter}" codeSystem="{$oidAssignedEntityRoleCode}"
                                  codeSystemVersion="{$ichoidAssignedEntityCodeVersion}"/>
                        </assignedEntity>
                    </author>
                </observationEvent>
            </component1>
        </xsl:if>

        <!-- H.3.r Sender's diagnosis/syndrome code (repeat as necessary)-->
        <xsl:apply-templates select="senderdiagnosisinformation" mode="ICH-case-summary"/>

        <!-- H.4 Sender's Comments -->
        <xsl:if test="string-length(sendercomment) > 0">
            <xsl:comment>H.4: Sender's Comments</xsl:comment>
            <component1 typeCode="COMP">
                <observationEvent moodCode="EVN" classCode="OBS">
                    <code code="{$Comment}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$ichoidObservationCLVersion}"/>
                    <value xsi:type="ED" mediaType="text/plain">
                        <xsl:value-of select="sendercomment"/>
                    </value>
                    <author typeCode="AUT">
                        <assignedEntity classCode="ASSIGNED">
                            <code code="{$Sender}" codeSystem="{$oidAssignedEntityRoleCode}"
                                  codeSystemVersion="{$ichoidAssignedEntityCodeVersion}"/>
                        </assignedEntity>
                    </author>
                </observationEvent>
            </component1>
        </xsl:if>

        <xsl:apply-templates select="../../mhlwadminitemsicsr/mhlwdummy" mode="mhlwdummy-b"/>
    </xsl:template>

    <xsl:template match="mhlwdummy" mode="mhlwdummy-b">
        <!-- J2.7.2: Comments relating to incomplete reports -->
        <xsl:if test="string-length(mhlwadmicsrcommentsincomplete) > 0">
            <xsl:comment>J2.7.2: Comments relating to incomplete reports</xsl:comment>
            <component1 typeCode="COMP">
                <observationEvent classCode="OBS" moodCode="EVN">
                    <code code="{$MhlwAdmIcsrCommentsIncomplete}" codeSystem="{$pmdaoidRetrospectiveAnalysisCode}" codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="未完了に対するコメント"/>
                    <value xsi:type="ED">
                        <xsl:value-of select="mhlwadmicsrcommentsincomplete"/>
                    </value>
                </observationEvent>
            </component1>
        </xsl:if>

        <!-- J2.9 Retrospective Analysis of Infection -->
        <xsl:if test="string-length(mhlwretrospectiveanalysisofinf) > 0">
            <xsl:comment>J2.9: Retrospective Analysis of Infection</xsl:comment>
            <component1 typeCode="COMP">
                <observationEvent moodCode="EVN" classCode="OBS">
                    <code code="{$RetrospectiveAnalysisInfection}" codeSystem="{$pmdaoidRetrospectiveAnalysisCode}"
                          codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="感染症の遡及調査"/>
                    <value xsi:type="ED">
                        <xsl:value-of select="mhlwretrospectiveanalysisofinf"/>
                    </value>
                </observationEvent>
            </component1>
        </xsl:if>

        <!-- J2.10 Company's action to be taken -->
        <xsl:if test="string-length(mhlwadmicsrcountermeasures) > 0">
            <xsl:comment>J2.10: Future Measures</xsl:comment>
            <component1 typeCode="COMP">
                <observationEvent moodCode="EVN" classCode="OBS">
                    <code code="{$FutureMeasures}" codeSystem="{$pmdaoidRetrospectiveAnalysisCode}"
                          codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="今後の対応"/>
                    <value xsi:type="ED">
                        <xsl:value-of select="mhlwadmicsrcountermeasures"/>
                    </value>
                </observationEvent>
            </component1>
        </xsl:if>

        <!-- J2.11 Other references -->
        <xsl:if test="string-length(mhlwadmicsrreporttimesevent) > 0">
            <xsl:comment>J2.11: Other Reference Items</xsl:comment>
            <component1 typeCode="COMP">
                <observationEvent moodCode="EVN" classCode="OBS">
                    <code code="{$OtherReferencesItem}" codeSystem="{$pmdaoidRetrospectiveAnalysisCode}"
                          codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="その他参考事項等"/>
                    <value xsi:type="ED">
                        <xsl:value-of select="mhlwadmicsrreporttimesevent"/>
                    </value>
                </observationEvent>
            </component1>
        </xsl:if>

        <!-- J2.16: Summary of report content -->
        <xsl:if test="string-length(mhlwsummaryofreportcontent) > 0">
            <xsl:comment>J2.16: Summary of report content</xsl:comment>
            <component1 typeCode="COMP">
                <observationEvent moodCode="EVN" classCode="OBS">
                    <code code="{$SummaryReportContent}" codeSystem="{$pmdaoidRetrospectiveAnalysisCode}"
                          codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="報告内容の要点"/>
                    <value xsi:type="ED">
                        <xsl:value-of select="mhlwsummaryofreportcontent"/>
                    </value>
                </observationEvent>
            </component1>
        </xsl:if>

        <xsl:apply-templates select="../mhlwadmicsrremarks" mode="mhlwadmicsrremarks-a"/>
    </xsl:template>

    <xsl:template match="mhlwadmicsrremarks" mode="mhlwadmicsrremarks-a">
        <!-- J2.19: Remark 1 -->
        <xsl:if test="string-length(mhlwadmicsrremarks1) > 0">
            <xsl:comment>J2.19: Remark 1</xsl:comment>
            <component1 typeCode="COMP">
                <observationEvent moodCode="EVN" classCode="OBS">
                    <code code="{$pmdaRemark1}" codeSystem="{$pmdaoidRetrospectiveAnalysisCode}"
                          codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="備考1"/>
                    <value xsi:type="ED">
                        <xsl:value-of select="mhlwadmicsrremarks1"/>
                    </value>
                </observationEvent>
            </component1>
        </xsl:if>

        <!-- J2.20: Remark 2 -->
        <xsl:if test="string-length(mhlwadmicsrremarks2) > 0">
            <xsl:comment>J2.20: Remark 2</xsl:comment>
            <component1 typeCode="COMP">
                <observationEvent moodCode="EVN" classCode="OBS">
                    <code code="{$pmdaRemark2}" codeSystem="{$pmdaoidRetrospectiveAnalysisCode}"
                          codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="備考2"/>
                    <value xsi:type="ED">
                        <xsl:value-of select="mhlwadmicsrremarks2"/>
                    </value>
                </observationEvent>
            </component1>
        </xsl:if>


        <!-- J2.21: Remark 3 -->
        <xsl:if test="string-length(mhlwadmicsrremarks3) > 0">
            <xsl:comment>J2.21: Remark 3</xsl:comment>
            <component1 typeCode="COMP">
                <observationEvent moodCode="EVN" classCode="OBS">
                    <code code="{$pmdaRemark3}" codeSystem="{$pmdaoidRetrospectiveAnalysisCode}"
                          codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="備考3"/>
                    <value xsi:type="ED">
                        <xsl:value-of select="mhlwadmicsrremarks3"/>
                    </value>
                </observationEvent>
            </component1>
        </xsl:if>

        <!-- J2.22: Remark 4 -->
        <xsl:if test="string-length(mhlwadmicsrremarks4) > 0">
            <xsl:comment>J2.22: Remark 4</xsl:comment>
            <component1 typeCode="COMP">
                <observationEvent moodCode="EVN" classCode="OBS">
                    <code code="{$pmdaRemark4}" codeSystem="{$pmdaoidRetrospectiveAnalysisCode}"
                          codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="備考4"/>
                    <value xsi:type="ED">
                        <xsl:value-of select="mhlwadmicsrremarks4"/>
                    </value>
                </observationEvent>
            </component1>
        </xsl:if>
    </xsl:template>

    <!-- H.3.r Sender's Diagnosis
  ichicsr/ichicsrmessageheader/safetyreport/summary/senderdiagnosisinfo
  H.3.r.1b Sender's Diagnosis / Syndrome and / or Reclassification of Reaction / Event (MedDRA code)
  H.3.r.1a MedDRA Version for Sender's Diagnosis / Syndrome and / or Reclassification of Reaction / Event
  -->
    <xsl:template match="senderdiagnosisinformation" mode="ICH-case-summary">
        <xsl:if test="string-length(senderdiagnosis) > 0">
            <xsl:variable name="positionSendDiag">
                <xsl:value-of select="position()"/>
            </xsl:variable>
            <xsl:comment>H.3.r: Sender's Diagnosis - (<xsl:value-of select="$positionSendDiag"/>)</xsl:comment>
            <component1 typeCode="COMP">
                <observationEvent moodCode="EVN" classCode="OBS">
                    <code code="{$Diagnosis}" codeSystem="{$oidObservationCode}"
                          codeSystemVersion="{$ichoidObservationCLVersion}"/>
                    <xsl:comment>H.3.r.1b: Sender's Diagnosis / Syndrome and / or Reclassification of Reaction / Event (MedDRA code)</xsl:comment>
                    <xsl:comment>H.3.r.1a: MedDRA Version for Sender's Diagnosis / Syndrome and / or Reclassification of Reaction / Event</xsl:comment>
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
                                  codeSystemVersion="{$ichoidAssignedEntityCodeVersion}"/>
                        </assignedEntity>
                    </author>
                </observationEvent>
            </component1>
        </xsl:if>
    </xsl:template>

    <!-- H.5.r Case Summary and Reporter’s Comments in Native Language (repeat as necessary) -->
    <xsl:template match="summary/casesummarynarrative" mode="case-summary">
        <xsl:if test="string-length(reportercommentothlang) > 0 or string-length(narrativeothlang) > 0 or string-length(sendercommentothlang) > 0">
            <xsl:variable name="positionCaseSumNar">
                <xsl:value-of select="position()"/>
            </xsl:variable>
            <xsl:variable name='newline'><xsl:text>
		</xsl:text>
            </xsl:variable>
            <xsl:comment>H.5.r: Case Summary and Reporter’s Comments in Native Language - (<xsl:value-of select="$positionCaseSumNar"/>)</xsl:comment>
            <component typeCode="COMP">
                <observationEvent moodCode="EVN" classCode="OBS">
                    <code code="{$SummaryAndComment}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$ichObservationCLVersion}"/>
                    <xsl:comment>H.5.r.1a: Case Summary and Reporter's Comments Text</xsl:comment>
                    <xsl:comment>H.5.r.1b: Case Summary and Reporter's Comments Language</xsl:comment>
                    <xsl:variable name="narrativeothFinal">
                        <xsl:choose>
                            <xsl:when test="string-length(narrativeothlang) > 0">
                                <xsl:value-of select="concat('Company Narrative:',narrativeothlang,$newline)"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="''"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="sendercommentothFinal">
                        <xsl:choose>
                            <xsl:when test="string-length(sendercommentothlang) > 0">
                                <xsl:value-of select="concat('Sender comments:',sendercommentothlang,$newline)"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="''"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="reportercommentothFinal">
                        <xsl:choose>
                            <xsl:when test='string-length(reportercommentothlang) > 0'>
                                <xsl:value-of select="concat('Reporter comments:',reportercommentothlang)"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="''"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <value xsi:type="ED" language="{summaryandreportercommentslang}" mediaType="text/plain">
                        <xsl:value-of
                                select="substring(concat($narrativeothFinal,$sendercommentothFinal,$reportercommentothFinal),1,100000)"/>
                    </value>
                    <author typeCode="AUT">
                        <assignedEntity classCode="ASSIGNED">
                            <code code="{$Reporter}" codeSystem="{$oidAssignedEntityRoleCode}"
                                  codeSystemVersion="{$ichoidAssignedEntityRoleCodeVersion}"/>
                        </assignedEntity>
                    </author>
                </observationEvent>
            </component>
        </xsl:if>
    </xsl:template>

    <!-- J2.13: Outline of clinical trial -->
    <xsl:template match="mhlwabstractofstudy" mode="mhlwabstractofstudy-a">
        <xsl:variable name="positionClinicalTrial">
            <xsl:value-of select="position()"/>
        </xsl:variable>
        <xsl:comment>J2.13: Outline of clinical trial - (<xsl:value-of select="$positionClinicalTrial"/>)</xsl:comment>
        <subjectOf2 typeCode="SBJ">
            <organizer classCode="CATEGORY" moodCode="EVN">
                <code code="1" codeSystem="{$pmdaoidClinicalTrialCode}" codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="治験の概要"/>

                <!-- J2.13.r.1: Notification Number -->
                <xsl:if test="string-length(mhlwnotificationnumber) > 0">
                    <xsl:comment>J2.13.r.1: Notification Number (<xsl:value-of select="$positionClinicalTrial"/>)</xsl:comment>
                    <component typeCode="COMP">
                        <observation classCode="OBS" moodCode="EVN">
                            <code code="7" codeSystem="{$pmdaoidRetrospectiveAnalysisCode}" codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="届出回数"/>
                            <value xsi:type="INT" value="{mhlwnotificationnumber}"/>
                        </observation>
                    </component>
                </xsl:if>

                <!-- J2.13.r.2: Target Disease -->
                <xsl:if test="string-length(mhlwindicationforstudy) > 0">
                    <xsl:comment>J2.13.r.2: Target Disease (<xsl:value-of select="$positionClinicalTrial"/>)</xsl:comment>
                    <component typeCode="COMP">
                        <observation classCode="OBS" moodCode="EVN">
                            <code code="8" codeSystem="{$pmdaoidRetrospectiveAnalysisCode}" codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="対象疾患"/>
                            <value xsi:type="ED">
                                <xsl:value-of select="mhlwindicationforstudy"/>
                            </value>
                        </observation>
                    </component>
                </xsl:if>

                <!-- J2.13.r.3: Development Phase -->
                <xsl:if test="string-length(mhlwphaseofstudies) > 0">
                    <xsl:comment>J2.13.r.3: Development Phase (<xsl:value-of select="$positionClinicalTrial"/>)</xsl:comment>
                    <component typeCode="COMP">
                        <observation classCode="OBS" moodCode="EVN">
                            <code code="9" codeSystem="{$pmdaoidRetrospectiveAnalysisCode}" codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="開発相"/>
                            <value xsi:type="CE" code="{mhlwphaseofstudies}" codeSystem="{$pmdaoidPhaseOfStudiesCode}" codeSystemVersion="{mhlwphaseofstudiescsv}"/>
                        </observation>
                    </component>
                </xsl:if>

                <!-- J2.13.r.4: Are any subjects given this investigational drug? -->
                <xsl:if test="string-length(mhlwnumbofptundertreatmentr3) > 0">
                    <xsl:comment>J2.13.r.4: Are any subjects given this investigational drug? (<xsl:value-of select="$positionClinicalTrial"/>)</xsl:comment>
                    <component typeCode="COMP">
                        <observation classCode="OBS" moodCode="EVN">
                            <code code="10" codeSystem="{$pmdaoidRetrospectiveAnalysisCode}" codeSystemVersion="{$pmdaoidObservationCLVersion}" displayName="投薬中の症例の有無"/>
                            <value xsi:type="BL" value="{mhlwnumbofptundertreatmentr3}"/>
                        </observation>
                    </component>
                </xsl:if>
            </organizer>
        </subjectOf2>
    </xsl:template>

</xsl:stylesheet>