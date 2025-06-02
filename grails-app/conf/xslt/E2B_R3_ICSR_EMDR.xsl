<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="urn:hl7-org:v3">

    <xsl:output indent="yes" method="xml" omit-xml-declaration="no" encoding="utf-8"/>
    <xsl:strip-space elements="*"/>

    <!-- File to have access to common variables and common tags-->
    <xsl:include href="ICH_COMMON_VARIABLE.xsl"/>
    <xsl:include href="ICH_COMMON_TAGS.xsl"/>

    <!--ICH ICSR : conversion of the main structure incl. root element and controlActProcess
    E2B(R2): root element "ichicsr"
    E2B(R3): root element "PORR_IN049016UV"
    -->
    <xsl:template match="/ichicsr">
        <PORR_IN040001UV01 xmlns="urn:hl7-org:v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ITSVersion="XML_1.0" >
            <!--edit schema location as needed-->
            <xsl:attribute name="xsi:schemaLocation">urn:hl7-org:v3 ../../../../eMDRHL7/Impl_Files/Con170227.xsd</xsl:attribute>
            <!-- Message Header-->
            <xsl:apply-templates select="ichicsrbatchheader" mode="part-a"/>
            <!--Report-->
            <xsl:apply-templates select="icsreport/safetyreport" mode="report" />

        </PORR_IN040001UV01>
    </xsl:template>

    <xsl:template match="/ichicsr/ichicsrbatchheader" mode="part-a">
        <!-- creating element Id with attributes root , assigningAuthName and extension -->
        <id assigningAuthorityName="MessageSender" extension="{batchmessagenumb}" root="1.1"/>
        <xsl:comment>Created element Id with attributes root , assigningAuthName and extension</xsl:comment>

        <!-- Creating element creationTime-->
        <xsl:choose>
            <xsl:when test="string-length(batchmessagedate) > 0">
                <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                    <xsl:with-param name="element">creationTime</xsl:with-param>
                    <xsl:with-param name="value" select="batchmessagedate"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <creationTime/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:comment>Created element creationTime</xsl:comment>

        <!-- Creating element responseModeCode -->
        <responseModeCode code="D"/>
        <xsl:comment>Created element responseModeCode</xsl:comment>

        <!-- Creating element versionCode -->
        <versionCode code="V3NORMED_2016"/>
        <xsl:comment>Created element versionCode</xsl:comment>

        <!-- Creating element interactionId -->
        <interactionId extension="MCCI_IN200100UV01" root="{$emdrOidInteractionId}"/>
        <xsl:comment>Created element interactionId</xsl:comment>

        <!-- Creating element batchTotalNumber -->
        <batchTotalNumber value="1"/>
        <xsl:comment>Created element batchTotalNumber</xsl:comment>

        <!-- Creating receiver: Message Receiver Identifier -->
        <receiver>
            <telecom/>
            <device>
                <id nullFlavor="NA"/>
                <asAgent>
                    <representedOrganization>
                        <id nullFlavor="NA"/>
                        <xsl:if test="string-length(batchmessagereceiveridentifier) > 0">
                            <name><xsl:value-of select="batchmessagereceiveridentifier"/></name>
                        </xsl:if>
                    </representedOrganization>
                </asAgent>
            </device>
        </receiver>
        <xsl:comment>Created receiver: Message Receiver Identifier</xsl:comment>

        <!-- Creating sender: Message Sender Identifier -->
        <sender>
            <telecom/>
            <device>
                <id nullFlavor="NA"/>
                <softwareName></softwareName>
                <asAgent>
                    <representedOrganization>
                        <id nullFlavor="NA"/>
                        <xsl:if test="string-length(batchmessagesenderidentifier) > 0">
                            <name><xsl:value-of select="batchmessagesenderidentifier"/></name>
                        </xsl:if>
                    </representedOrganization>
                </asAgent>
            </device>
        </sender>
        <xsl:comment>Created sender: Message Sender Identifier</xsl:comment>

    </xsl:template>

    <xsl:template match="safetyreport" mode="report">
        <message>
            <xsl:apply-templates select="../icsrmessageheader" mode="part-b"/>

            <xsl:apply-templates select="./documentheldbysender" mode="part-y"/>


            <controlActProcess moodCode="EVN">
                <!-- HL7 Trigger Event ID -->
                <code code="PORR_TE040001UV01" codeSystem="HL7" codeSystemName="HL7 Trigger Event Id"/>
                <xsl:comment>HL7 Trigger Event ID </xsl:comment>

                <xsl:apply-templates select="./adverseevent" mode="part-c"/>

                <xsl:apply-templates select="." mode="main"/>

                <!-- end of item subject content: end of item subject content -->
                <xsl:if test="string-length(suspectdevice/allmanufacturers) > 0">
                    <xsl:comment>end of item subject content: end of item subject content</xsl:comment>
                    <xsl:apply-templates select="suspectdevice/allmanufacturers" mode="part-w"/>
                </xsl:if>


                <!-- H1: Type of Reportable Event (Death, Malfunction, etc) -->
                <xsl:apply-templates select="suspectdevice/hdevicemfrinfo" mode="part-x"/>

                <xsl:if test="string-length(exemptions/exemptionno) > 0">
                    <reasonOf>
                        <detectedIssueEvent>
                            <code code="F77776" codeSystem="{$emdrOidRootCode}" codeSystemName="Exemption_No"/>
                            <value xsi:type="CE" code="{exemptions/exemptionno}" codeSystem="{$emdrOidRootCode}"/>
                        </detectedIssueEvent>
                    </reasonOf>
                </xsl:if>

            </controlActProcess>
        </message>
    </xsl:template>

    <xsl:template match="/ichicsr/icsreport/icsrmessageheader" mode="part-b">
        <!-- adding id under message tag: Message Identifier -->
        <xsl:choose>
            <xsl:when test="string-length(messageidentifier) > 0">
                <xsl:call-template name="ElemWithExtensionAttrOrNullFlavor">
                    <xsl:with-param name="element">id</xsl:with-param>
                    <xsl:with-param name="value" select="messageidentifier"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <id/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:comment>adding id under message tag: Message Identifier</xsl:comment>

        <!-- N.2.r.4: Date of Message Creation -->
        <xsl:choose>
            <xsl:when test="string-length(messagecreationdate) > 0">
                <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                    <xsl:with-param name="element">creationTime</xsl:with-param>
                    <xsl:with-param name="value" select="messagecreationdate"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <creationTime/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:comment>N.2.r.4: Date of Message Creation</xsl:comment>

        <!-- Creating element interactionId -->
        <interactionId root="{$emdrOidInteractionId}" extension="PORR_IN04001" assigningAuthorityName="HL7"/>
        <xsl:comment>Created element interactionId</xsl:comment>

        <processingCode nullFlavor="NA" />
        <processingModeCode nullFlavor="NA" />
        <acceptAckCode nullFlavor="NA" />

        <!-- N.2.r.3: Message Receiver Identifier -->
        <receiver>
            <telecom/>
            <device>
                <id nullFlavor="NA" />
            </device>
        </receiver>
        <xsl:comment>N.2.r.3: Message Receiver Identifier</xsl:comment>

        <!-- N.2.r.2: Message Sender Identifier -->
        <sender>
            <telecom/>
            <device>
                <id nullFlavor="NA" />
            </device>
        </sender>
        <xsl:comment>N.2.r.2: Message Sender Identifier</xsl:comment>
    </xsl:template>

    <xsl:template match="documentheldbysender" mode="part-y">
        <!-- adding C53614 tag: Attachment Tag -->
        <xsl:if test="string-length(includeddocument) > 0 or string-length(docfilename) > 0 ">
            <attachment>
                <xsl:comment>adding attachment tag: Attachment Tag</xsl:comment>

                <!-- Id under Attachment: Date of Message Creation -->
                <id nullFlavor="NA" />
                <xsl:comment>Id under Attachment: Date of Message Creation</xsl:comment>

                <!-- Creating attachment text node: Creating attachment text node -->
                <xsl:comment>Created attachment text node: Created attachment text node</xsl:comment>
                <xsl:variable name="MediaType">
                    <xsl:value-of select="substring-after(docmediatype,'.')"/>
                </xsl:variable>
                <xsl:if test="$MediaType = 'txt'">
                    <text mediaType="text/plain" representation="TXT">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'pdf'">
                    <text mediaType="application/pdf" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'png'">
                    <text mediaType="image/png" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'jpeg'">
                    <text mediaType="image/jpeg" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'jpg'">
                    <text mediaType="image/jpeg" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'html'">
                    <text mediaType="text/html" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'psd'">
                    <text mediaType="application/octet-stream" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'tif'">
                    <text mediaType="image/tiff" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'docx'">
                    <text mediaType="application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                          representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'xls'">
                    <text mediaType="application/vnd.ms-excel" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'xlsx'">
                    <text mediaType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                          representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'vsd'">
                    <text mediaType="application/x-visio" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'rtf'">
                    <text mediaType="text/rtf" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'doc'">
                    <text mediaType="application/msword" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'ps'">
                    <text mediaType="application/postscript" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'mdb'">
                    <text mediaType="application/x-msaccess" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'bmp'">
                    <text mediaType="image/bmp" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'xml'">
                    <text mediaType="text/xml" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'sgm'">
                    <text mediaType="text/sgml" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'msg'">
                    <text mediaType="application/vnd.ms-outlook" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
                <xsl:if test="$MediaType = 'dicom'">
                    <text mediaType="application/dicom" representation="B64" compression="DF">
                        <xsl:value-of select="includeddocument"/>
                        <reference>
                            <xsl:if test="string-length(docfilename) > 0 ">
                                <xsl:attribute name="value"><xsl:value-of select="docfilename"/></xsl:attribute>
                            </xsl:if>
                        </reference>
                    </text>
                </xsl:if>
            </attachment>
            <xsl:comment>adding attachment end tag: Attachment end Tag</xsl:comment>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/ichicsr/icsreport/safetyreport/adverseevent" mode="part-c">
        <!-- Report Creation date: Date of Creation -->
        <xsl:if test="string-length(dateofreport) > 0">
            <xsl:comment>Report Creation date: Date of Creation</xsl:comment>
        </xsl:if>
        <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
            <xsl:with-param name="element">effectiveTime</xsl:with-param>
            <xsl:with-param name="value" select="dateofreport"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="safetyreport" mode="main">
        <subject>
            <investigationEvent>
                <!-- C.1.1: Sender's Safety Report Unique Identifier -->
                <xsl:comment>C.1.1: Sender's Safety Report Unique Identifier</xsl:comment>
                <id assigningAuthorityName="FDA" root="{$emdrOidRootCode}">
                    <xsl:attribute name="extension"><xsl:value-of select="suspectdevice/allmanufacturers/mfrreportnumber"/></xsl:attribute>
                </id>

                <!-- Doubt -  that code element will be repeated but based on which r2 tag the code value will be assigned -->
                <!-- Adverse_Event_Or_Product_Problem_Report -->
                <xsl:apply-templates select="adverseevent" mode="part-d"/>

                <xsl:if test="string-length(suspectdevice/hdevicemfrinfo/addmfrnarrative) > 0">
                    <xsl:comment>Created text node for H11: Created  text node for H11</xsl:comment>
                    <text mediaType="text/plain"><xsl:value-of select="suspectdevice/hdevicemfrinfo/addmfrnarrative"></xsl:value-of></text>
                </xsl:if>

                <!-- elements not found -->
                <statusCode/>
                <activityTime/>
                <availabilityTime/>

                <authorOrPerformer typeCode="AUT">
                    <assignedEntity/>
                </authorOrPerformer>

                <trigger>
                    <reaction>
                        <!-- Creating  text node for B5 -->
                        <xsl:if test="string-length(adverseevent/narrativeincludeclinical) > 0">
                            <xsl:comment>Created  text node for B5: Created  text node for B5</xsl:comment>
                            <text mediaType="text/plain">
                                <xsl:value-of select="adverseevent/narrativeincludeclinical"/>
                            </text>
                        </xsl:if>

                        <!-- Creating  text node for B5-->
                        <xsl:apply-templates select="suspectdevice/allmanufacturers" mode="part-e"/>

                        <!-- Effective time under trigger: Effective time under trigger -->
                        <xsl:if test="string-length(adverseevent/reactionstartdater3) > 0">
                            <xsl:comment>Effective time under trigger: Effective time under trigger</xsl:comment>
                            <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                                <xsl:with-param name="element">effectiveTime</xsl:with-param>
                                <xsl:with-param name="value" select="adverseevent/reactionstartdater3"/>
                            </xsl:call-template>
                        </xsl:if>

                        <!-- Patient identification -->
                        <subject>
                            <investigativeSubject>
                                <xsl:choose>
                                    <xsl:when test="count(patient) > 0">
                                        <xsl:apply-templates select="patient" mode="identification"/>
                                    </xsl:when>
                                    <xsl:when test="count(adverseevent) > 0">
                                        <xsl:if test="string-length(../adverseevent/patientdeathdate) > 0">
                                            <xsl:comment>Deceased Time: Date of Death</xsl:comment>
                                            <deceasedTime value="{../adverseevent/patientdeathdate}"/>
                                        </xsl:if>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <subjectAffectedPerson/>
                                    </xsl:otherwise>
                                </xsl:choose>

                                <xsl:if test="count(adverseevent) > 0">
                                    <xsl:apply-templates select="adverseevent" mode="part-g"/>
                                </xsl:if>

                                <xsl:if test="count(ufimporterinfo/patientcodes) > 0">
                                    <xsl:apply-templates select="ufimporterinfo/patientcodes" mode="patientcodes"/>
                                </xsl:if>

                                <xsl:if test="count(suspectdevice/hdevicemfrinfo/mfrpatientcodes) > 0">
                                    <xsl:apply-templates select="suspectdevice/hdevicemfrinfo/mfrpatientcodes" mode="mfrpatientcodes"/>
                                </xsl:if>

                                <xsl:if test="count(ufimporterinfo/healthimpactcodes) > 0">
                                    <xsl:apply-templates select="ufimporterinfo/healthimpactcodes" mode="healthimpactcodes"/>
                                </xsl:if>

                                <xsl:if test="count(suspectdevice/hdevicemfrinfo/mfrhealthimpactcodes) > 0">
                                    <xsl:apply-templates select="suspectdevice/hdevicemfrinfo/mfrhealthimpactcodes" mode="mfrhealthimpactcodes"/>
                                </xsl:if>
                            </investigativeSubject>
                        </subject>
                        <!-- Location where event occurred -->
                        <xsl:if test="count(ufimporterinfo) > 0">
                            <xsl:apply-templates select="ufimporterinfo" mode="part-f"/>
                        </xsl:if>

                        <!-- Initial Reporter information -->
                        <xsl:if test="count(initialreporter) > 0">
                            <xsl:apply-templates select="initialreporter" mode="part-i"/>
                        </xsl:if>
                    </reaction>
                </trigger>

                <!--User Facility Importer Details (Devices only) -->
                <xsl:choose>
                    <xsl:when test="count(ufimporterinfo) > 0">
                        <xsl:apply-templates select="ufimporterinfo" mode="part-j"/>
                    </xsl:when>
                    <xsl:when test="count(ufimporterinfo) = 0 and string-length(suspectdevice/allmanufacturers/receiptdate) > 0">
                        <!-- All Manufacuturer -->
                        <xsl:apply-templates select="suspectdevice/allmanufacturers" mode="part-allmfr"/>
                    </xsl:when>
                </xsl:choose>

                <xsl:if test="count(adverseevent) > 0">
                    <xsl:apply-templates select="adverseevent" mode="part-n"/>
                </xsl:if>

                <xsl:if test="count(suspectdevice) > 0">
                    <!-- D: Suspect Medical Device  -->
                    <xsl:apply-templates select="suspectdevice" mode="part-o"/>
                </xsl:if>

                <!-- C: Suspect Products -->
                <xsl:if test="count(suspectproducts) > 0">
                    <xsl:apply-templates select="." mode="part-v"/>
                </xsl:if>
            </investigationEvent>
        </subject>
    </xsl:template>

    <xsl:template match="adverseevent" mode="part-d">
        <xsl:if test="string-length(reporttype) > 0">
            <xsl:comment>Adverse_Event_Or_Product_Problem_Report</xsl:comment>
        </xsl:if>
        <code codeSystem="{$emdrOidObservationCode}" codeSystemName="Adverse_Event_Or_Product_Problem_Report">
            <xsl:if test="string-length(reporttype) > 0">
                <xsl:attribute name="code"><xsl:value-of select="reporttype"/></xsl:attribute>
            </xsl:if>
        </code>
    </xsl:template>

    <xsl:template match="allmanufacturers" mode="part-e">
        <xsl:if test="string-length(adverseeventterms) > 0">
            <xsl:comment>G7: ADVERSEEVENTTERMS</xsl:comment>
            <term mediaType="text/plain">
                <xsl:value-of select="adverseeventterms" />
            </term>
        </xsl:if>
    </xsl:template>

    <xsl:template match="patient" mode="identification">
        <xsl:choose>
            <xsl:when test="string-length(patientinitial) > 0 or string-length(patientsexr3) > 0 or string-length(patientbirthdater3) > 0 or count(../adverseevent) > 0">
                <subjectAffectedPerson>
                    <!-- Patient Name -->
                    <xsl:if test="string-length(patientinitial) > 0">
                        <xsl:comment>A1: PATIENT INITIAL</xsl:comment>
                        <xsl:call-template name="ElemWithTextFieldOrNullFlavor">
                            <xsl:with-param name="element">name</xsl:with-param>
                            <xsl:with-param name="value" select="patientinitial"/>
                        </xsl:call-template>
                    </xsl:if>

                    <!-- Patient sex -->
                    <xsl:if test="string-length(patientsexr3) > 0">
                        <administrativeGenderCode code="{patientsexr3}" codeSystem="{$emdrOidObservationCode}" codeSystemName="Sex"/>
                    </xsl:if>

                    <!-- Patient Birth Time: Date of Birth -->
                    <xsl:if test="string-length(patientbirthdater3) > 0 ">
                        <xsl:comment>Birth Time: Date of Birth</xsl:comment>
                        <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                            <xsl:with-param name="element">birthTime</xsl:with-param>
                            <xsl:with-param name="value" select="patientbirthdater3"/>
                        </xsl:call-template>
                    </xsl:if>

                    <!-- Patient Deceased Time: Date of Death -->
                    <xsl:if test="string-length(../adverseevent/patientdeathdate) > 0">
                        <xsl:comment>Deceased Time: Date of Death</xsl:comment>
                        <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                            <xsl:with-param name="element">deceasedTime</xsl:with-param>
                            <xsl:with-param name="value" select="../adverseevent/patientdeathdate"/>
                        </xsl:call-template>
                    </xsl:if>

                    <!-- Patient race -->
                    <xsl:if test="count(patientrace) > 0">
                        <xsl:for-each select="patientrace">
                            <xsl:if test="string-length(./patientraceinfo) > 0">
                                <raceCode code="{./patientraceinfo}" codeSystem="{$emdrOidObservationCode}" codeSystemName="Race"/>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:if>

                    <!-- Patient Ethnic Group-->
                    <xsl:if test="string-length(patientethnicitygroup) > 0">
                        <ethnicGroupCode code="{patientethnicitygroup}" codeSystem="{$emdrOidObservationCode}" codeSystemName="Ethnicity"/>
                    </xsl:if>
                </subjectAffectedPerson>
            </xsl:when>
            <xsl:otherwise>
                <subjectAffectedPerson/>
            </xsl:otherwise>
        </xsl:choose>

        <!-- Patient Age-->
        <xsl:if test="string-length(patientonsetage) > 0 and string-length(patientonsetageunitr3) > 0">
            <xsl:comment>Age: Patient Age</xsl:comment>
            <subjectOf>
                <observation moodCode="EVN">
                    <code code="C25150" codeSystem="{$emdrOidObservationCode}" codeSystemName="Age"/>
                    <value xsi:type="PQ" value="{patientonsetage}" unit="{patientonsetageunitr3}"/>
                </observation>
            </subjectOf>
        </xsl:if>

        <!-- Patient Weight-->
        <xsl:if test="string-length(patientweight) > 0 and string-length(patientweightunit) > 0">
            <xsl:comment>Weight: Patient Weight</xsl:comment>
            <subjectOf>
                <observation moodCode="EVN">
                    <code code="C25208" codeSystem="{$emdrOidObservationCode}" codeSystemName="Weight"/>
                    <value xsi:type="PQ" value="{patientweight}" unit="{patientweightunit}"/>
                </observation>
            </subjectOf>
        </xsl:if>

        <xsl:if test="string-length(patientgender) > 0">
            <subjectOf>
                <observation moodCode="EVN">
                    <code code="C17357" codeSystem="2.16.840.1.113883.3.26.1.1" codeSystemName="Gender"/>
                    <value xsi:type="CE" code="{patientgender}"/>
                </observation>
            </subjectOf>
        </xsl:if>
        <xsl:if test="string-length(patientgenderother) > 0 and patientgender = 'C154420' ">
            <subjectOf>
                <observation moodCode="EVN">
                    <code code="C17357" codeSystem="2.16.840.1.113883.3.26.1.1" codeSystemName="Gender"/>
                    <value xsi:type="ED"><xsl:value-of select="patientgenderother"/></value>
                </observation>
            </subjectOf>
        </xsl:if>

    </xsl:template>

    <xsl:template match="adverseevent" mode="part-g">
        <!-- Relevant Data: Relevant Data - B6 -->
        <xsl:if test="string-length(testresulttext) > 0">
            <xsl:comment>Relevant Data: Relevant Data - B6</xsl:comment>
            <subjectOf>
                <observation moodCode="EVN">
                    <code code="C36292" codeSystem="NCI" codeSystemName="Test_Result"/>
                    <value mediaType="text/plain" xsi:type="ED">
                        <xsl:value-of select="testresulttext"/>
                    </value>
                </observation>
            </subjectOf>
        </xsl:if>

        <!-- Other Relevant Data: Relevant Data - B7 -->
        <xsl:if test="string-length(patientmedicalhistorytext) > 0">
            <xsl:comment>Other Relevant Data: Relevant Data - B7</xsl:comment>
            <subjectOf>
                <observation moodCode="EVN">
                    <code code="C53263" codeSystem="{$emdrOidObservationCode}" codeSystemName="Other_Personal_Medical_History"/>
                    <value mediaType="text/plain" xsi:type="ED">
                        <xsl:value-of select="patientmedicalhistorytext"/>
                    </value>
                </observation>
            </subjectOf>
        </xsl:if>
    </xsl:template>

    <xsl:template match="patientcodes" mode="patientcodes">
        <!-- F10: Health Effect - Clinical Code -->
        <xsl:if test="string-length(fdapatientcodes) > 0">
            <xsl:comment>F10: Health Effect - Clinical Code</xsl:comment>
            <subjectOf>
                <observation moodCode="EVN">
                    <code code="C53983" codeSystem="{$emdrOidObservationCode}" codeSystemName="Patient_Problem_Code"/>
                    <value xsi:type="CE" codeSystem="{$emdrOidObservationCode}">
                        <xsl:attribute name="code"><xsl:value-of select="fdapatientcodes"/></xsl:attribute>
                    </value>
                </observation>
            </subjectOf>
        </xsl:if>
    </xsl:template>

    <xsl:template match="mfrpatientcodes" mode="mfrpatientcodes">
        <!-- H6: Health Effect - Clinical Code -->
        <xsl:if test="string-length(mfrpatientcode) > 0">
            <xsl:comment>H6: Health Effect - Clinical Code</xsl:comment>
            <subjectOf>
                <observation moodCode="EVN">
                    <code code="C53983" codeSystem="{$emdrOidObservationCode}" codeSystemName="Patient_Problem_Code"/>
                    <value code="{mfrpatientcode}" xsi:type="CE" codeSystem="{$emdrOidObservationCode}"/>
                </observation>
            </subjectOf>
        </xsl:if>
    </xsl:template>

    <xsl:template match="healthimpactcodes" mode="healthimpactcodes">
        <!-- F10: Health Effect - Impact Code -->
        <xsl:if test="string-length(fdahealthimpactcodes) > 0">
            <xsl:comment>F10: Health Effect - Impact Code</xsl:comment>
            <subjectOf>
                <observation moodCode="EVN">
                    <code code="C122929" codeSystem="{$emdrOidObservationCode}" codeSystemName="Patient_Impact_Code"/>
                    <value xsi:type="CE" code="{fdahealthimpactcodes}" codeSystem="{$emdrOidObservationCode}">
                        <xsl:attribute name="code"><xsl:value-of select="fdahealthimpactcodes"/></xsl:attribute>
                    </value>
                </observation>
            </subjectOf>
        </xsl:if>
    </xsl:template>

    <xsl:template match="mfrhealthimpactcodes" mode="mfrhealthimpactcodes">
        <xsl:if test="string-length(mfrhealthimpactcode) > 0">
            <xsl:comment>H6: Health Effect - Impact Code</xsl:comment>
            <subjectOf>
                <observation moodCode="EVN">
                    <code code="C122929" codeSystem="{$emdrOidObservationCode}" codeSystemName="Patient_Impact_Code"/>
                    <value xsi:type="CE" codeSystem="{$emdrOidObservationCode}">
                        <xsl:attribute name="code"><xsl:value-of select="mfrhealthimpactcode"/></xsl:attribute>
                    </value>
                </observation>
            </subjectOf>
        </xsl:if>
    </xsl:template>

    <xsl:template match="ufimporterinfo" mode="part-f">
        <xsl:if test="string-length(locationeventoccur) > 0">
            <xsl:comment>F12: LOCATION EVENT OCCUR</xsl:comment>
            <location>
                <locatedEntity>
                    <location>
                        <code code="{locationeventoccur}" codeSystem="{$emdrOidObservationCode}" codeSystemName="Location">
                            <xsl:if test="string-length(locationeventoccurtext) > 0">
                                <originalText>
                                    <xsl:value-of select="locationeventoccurtext"/>
                                </originalText>
                            </xsl:if>
                        </code>
                    </location>
                </locatedEntity>
            </location>
        </xsl:if>
    </xsl:template>

    <!-- initial Reporter Details-->
    <xsl:template match="initialreporter" mode="part-i">
        <pertinentInformation>
            <primarySourceReport>
                <id nullFlavor="ASKU" />
                <code nullFlavor="ASKU" />

                <xsl:if test="string-length(initialreportsenttofda) > 0 ">
                    <xsl:comment>E4: INITIAL REPORT SENT TO FDA</xsl:comment>
                    <receiver negationInd="{initialreportsenttofda}">
                        <assignedEntity>
                            <assignedOrganization>
                                <name>FDA</name>
                            </assignedOrganization>
                        </assignedEntity>
                    </receiver>
                </xsl:if>

                <author>
                    <assignedEntity>
                        <xsl:choose>
                            <xsl:when test="string-length(reporteroccupation) > 0">
                                <xsl:comment>E3: REPORTER OCCUPATION</xsl:comment>
                                <code code="{reporteroccupation}" codeSystem="{$emdrOidObservationCode}" codeSystemName="Occupation">
                                    <xsl:if test="string-length(reporteroccupationtext) > 0 ">
                                        <originalText mediaType="text/plain">
                                            <xsl:value-of select="reporteroccupationtext"/>
                                        </originalText>
                                    </xsl:if>
                                </code>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:if test="string-length(reporteroccupationtext) > 0 ">
                                    <xsl:comment>E3: REPORTER OCCUPATION TEXT</xsl:comment>
                                    <code>
                                        <originalText mediaType="text/plain">
                                            <xsl:value-of select="reporteroccupationtext"/>
                                        </originalText>
                                    </code>
                                </xsl:if>
                            </xsl:otherwise>
                        </xsl:choose>

                        <assignedPerson>
                            <xsl:comment>E: INITIAL REPORTER</xsl:comment>
                            <name>
                                <xsl:if test="string-length(reportertitle) > 0">
                                    <xsl:call-template name="ElemWithTextFieldOrNullFlavor">
                                        <xsl:with-param name="element">prefix</xsl:with-param>
                                        <xsl:with-param name="value" select="reportertitle"/>
                                    </xsl:call-template>
                                </xsl:if>

                                <xsl:if test="string-length(reportergivenname) > 0">
                                    <xsl:call-template name="ElemWithTextFieldOrNullFlavor">
                                        <xsl:with-param name="element">given</xsl:with-param>
                                        <xsl:with-param name="value" select="reportergivenname"/>
                                    </xsl:call-template>
                                </xsl:if>

                                <xsl:if test="string-length(reportermiddlename) > 0">
                                    <xsl:call-template name="ElemWithTextFieldOrNullFlavor">
                                        <xsl:with-param name="element">given</xsl:with-param>
                                        <xsl:with-param name="value" select="reportermiddlename"/>
                                    </xsl:call-template>
                                </xsl:if>

                                <xsl:if test="string-length(reporterlastname) > 0">
                                    <xsl:call-template name="ElemWithTextFieldOrNullFlavor">
                                        <xsl:with-param name="element">family</xsl:with-param>
                                        <xsl:with-param name="value" select="reporterlastname"/>
                                    </xsl:call-template>
                                </xsl:if>
                            </name>

                            <xsl:if test="string-length(reporterphone) > 0">
                                <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                                    <xsl:with-param name="element">telecom</xsl:with-param>
                                    <xsl:with-param name="value" select="reporterphone"/>
                                </xsl:call-template>
                            </xsl:if>

                            <xsl:if test="string-length(reporteremail) > 0">
                                <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                                    <xsl:with-param name="element">telecom</xsl:with-param>
                                    <xsl:with-param name="value" select="reporteremail"/>
                                </xsl:call-template>
                            </xsl:if>

                            <xsl:if test="string-length(reporterfax) > 0">
                                <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                                    <xsl:with-param name="element">telecom</xsl:with-param>
                                    <xsl:with-param name="value" select="reporterfax"/>
                                </xsl:call-template>
                            </xsl:if>

                            <addr>
                                <xsl:if test="string-length(reporterstreetaddress1) > 0">
                                    <xsl:call-template name="ElemWithTextFieldOrNullFlavor">
                                        <xsl:with-param name="element">streetAddressLine</xsl:with-param>
                                        <xsl:with-param name="value" select="reporterstreetaddress1"/>
                                    </xsl:call-template>
                                </xsl:if>

                                <xsl:if test="string-length(reporterstreetaddress2) > 0">
                                    <streetAddressLine>
                                        <xsl:value-of select="reporterstreetaddress2"/>
                                    </streetAddressLine>
                                </xsl:if>

                                <xsl:if test="string-length(reportercity) > 0">
                                    <xsl:call-template name="ElemWithTextFieldOrNullFlavor">
                                        <xsl:with-param name="element">city</xsl:with-param>
                                        <xsl:with-param name="value" select="reportercity"/>
                                    </xsl:call-template>
                                </xsl:if>

                                <xsl:if test="string-length(reporterstate) > 0">
                                    <xsl:call-template name="ElemWithTextFieldOrNullFlavor">
                                        <xsl:with-param name="element">state</xsl:with-param>
                                        <xsl:with-param name="value" select="reporterstate"/>
                                    </xsl:call-template>
                                </xsl:if>

                                <xsl:if test="string-length(reporterpostcode) > 0">
                                    <xsl:call-template name="ElemWithTextFieldOrNullFlavor">
                                        <xsl:with-param name="element">postalCode</xsl:with-param>
                                        <xsl:with-param name="value" select="reporterpostcode"/>
                                    </xsl:call-template>
                                </xsl:if>

                                <xsl:if test="string-length(reportercountryr3) > 0">
                                    <country>
                                        <xsl:value-of select="reportercountryr3"/>
                                    </country>
                                </xsl:if>
                            </addr>
                        </assignedPerson>

                        <xsl:if test="string-length(reporterorganization) > 0">
                            <representedOrganization>
                                <xsl:call-template name="ElemWithTextFieldOrNullFlavor">
                                    <xsl:with-param name="element">name</xsl:with-param>
                                    <xsl:with-param name="value" select="reporterorganization"/>
                                </xsl:call-template>
                            </representedOrganization>
                        </xsl:if>
                    </assignedEntity>
                </author>
            </primarySourceReport>
        </pertinentInformation>
    </xsl:template>

    <!-- User Facility importer info-->
    <xsl:template match="ufimporterinfo" mode="part-j">
        <pertinentInformation1>
            <xsl:if test="string-length(uffollowupnumber) > 0">
                <xsl:comment>F7: UFFOLLOWUPNUMBER</xsl:comment>
                <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                    <xsl:with-param name="element">sequenceNumber</xsl:with-param>
                    <xsl:with-param name="value" select="uffollowupnumber"/>
                </xsl:call-template>
            </xsl:if>

            <secondaryCaseNotification>
                <xsl:choose>
                    <xsl:when test="string-length(ufreportnumber) > 0">
                        <xsl:comment>F2: UF REPORT NUMBER</xsl:comment>
                        <id assigningAuthorityName="FDA" extension="{ufreportnumber}" root="{$emdrOidRootCode}"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <id/>
                    </xsl:otherwise>
                </xsl:choose>

                <xsl:choose>
                    <xsl:when test="string-length(uftypeofreport) > 0">
                        <xsl:comment>F7: UF TYPE OF REPORT</xsl:comment>
                        <code code="{uftypeofreport}" codeSystem="{$emdrOidObservationCode}" codeSystemName="Type_of_Report"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <code/>
                    </xsl:otherwise>
                </xsl:choose>

                <xsl:if test="string-length(ufdistawaredate) > 0">
                    <xsl:comment>F6: UFDISTAWAREDATE</xsl:comment>
                    <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                        <xsl:with-param name="element">effectiveTime</xsl:with-param>
                        <xsl:with-param name="value" select="ufdistawaredate"/>
                    </xsl:call-template>
                </xsl:if>

                <xsl:if test="string-length(reportsenttofda) > 0 or string-length(datereportsent) > 0 ">
                    <receiver negationInd="{reportsenttofda}">
                        <xsl:if test="string-length(datereportsent) > 0">
                            <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                                <xsl:with-param name="element">time</xsl:with-param>
                                <xsl:with-param name="value" select="datereportsent"/>
                            </xsl:call-template>
                        </xsl:if>

                        <assignedEntity>
                            <assignedOrganization>
                                <code code="C17237" codeSystem="{$emdrOidObservationCode}" codeSystemName="Report_Receiver"/>
                            </assignedOrganization>
                        </assignedEntity>
                    </receiver>
                </xsl:if>

                <!--                <xsl:comment>F3-5: Contact Person</xsl:comment>-->
                <!--                <xsl:if test="count(uffacility) > 0 or string-length(uforimporter) > 0">-->
                <!--                    <xsl:apply-templates select="uffacility" mode="part-k"/>-->
                <!--                </xsl:if>-->

            </secondaryCaseNotification>
        </pertinentInformation1>

        <xsl:if test="string-length(reportsenttomfr) > 0 or string-length(datesenttomfr) > 0 or string-length(../suspectdevice/allmanufacturers/receiptdate) > 0 or
                      count(fmanufacturer) > 0">
            <pertinentInformation1>
                <secondaryCaseNotification>
                    <id nullFlavor="NA" />
                    <code nullFlavor="NI" />
                    <receiver>
                        <xsl:if test="string-length(reportsenttomfr) > 0">
                            <xsl:attribute name="negationInd"><xsl:value-of select="reportsenttomfr"/></xsl:attribute>
                        </xsl:if>
                        <xsl:if test="string-length(datesenttomfr) > 0 or string-length(../suspectdevice/allmanufacturers/receiptdate) > 0">
                            <time>
                                <xsl:choose>
                                    <xsl:when test="string-length(datesenttomfr) > 0">
                                        <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                                            <xsl:with-param name="element">low</xsl:with-param>
                                            <xsl:with-param name="value" select="datesenttomfr"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <low/>
                                    </xsl:otherwise>
                                </xsl:choose>

                                <xsl:apply-templates select="../suspectdevice/allmanufacturers" mode="part-l"/>
                            </time>
                        </xsl:if>

                        <xsl:choose>
                            <xsl:when test="count(fmanufacturer) > 0">
                                <xsl:apply-templates select="fmanufacturer" mode="part-m"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <assignedEntity/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </receiver>
                </secondaryCaseNotification>
            </pertinentInformation1>
        </xsl:if>
    </xsl:template>

    <xsl:template match="allmanufacturers" mode="part-allmfr">
        <pertinentInformation1>
            <secondaryCaseNotification>
                <id nullFlavor="NA" />
                <code nullFlavor="NI" />
                <receiver>
                    <time>
                        <xsl:apply-templates select="." mode="part-l"/>
                    </time>
                    <assignedEntity/>
                </receiver>
            </secondaryCaseNotification>
        </pertinentInformation1>
    </xsl:template>
    <!-- F3-5: Contact Person -->
    <xsl:template match="uffacility" mode="part-k">
        <author>
            <assignedEntity>
                <xsl:choose>
                    <xsl:when test="string-length(../uforimporter) > 0">
                        <xsl:comment>F1: uforimporter</xsl:comment>
                        <code code="{../uforimporter}" codeSystem="{$emdrOidObservationCode}" codeSystemName="Type_of_Reporter"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <code/>
                    </xsl:otherwise>
                </xsl:choose>

                <assignedOrganization>
                    <name nullFlavor="NA"/>

                    <addr>
                        <xsl:if test="string-length(uffacilityname) > 0">
                            <xsl:comment>F3: UFFACILITYNAME</xsl:comment>
                            <additionalLocator><xsl:value-of select="uffacilityname"/></additionalLocator>
                        </xsl:if>

                        <xsl:if test="string-length(ufaddress1) > 0">
                            <xsl:comment>F3: UFADDRESS1</xsl:comment>
                            <streetAddressLine><xsl:value-of select="ufaddress1"/></streetAddressLine>
                        </xsl:if>

                        <xsl:if test="string-length(ufaddress2) > 0">
                            <xsl:comment>F3: UFADDRESS2</xsl:comment>
                            <streetAddressLine><xsl:value-of select="ufaddress2"/></streetAddressLine>
                        </xsl:if>

                        <xsl:if test="string-length(ufcity) > 0">
                            <xsl:comment>F3: UFCITY</xsl:comment>
                            <city><xsl:value-of select="ufcity"/></city>
                        </xsl:if>

                        <xsl:if test="string-length(ufstate) > 0">
                            <xsl:comment>F3: UFSTATE</xsl:comment>
                            <state><xsl:value-of select="ufstate"/></state>
                        </xsl:if>

                        <xsl:if test="string-length(ufpostalcode) > 0">
                            <xsl:comment>F3: UFPOSTALCODE</xsl:comment>
                            <postalCode><xsl:value-of select="ufpostalcode"/></postalCode>
                        </xsl:if>

                        <xsl:if test="string-length(ufcountry) > 0">
                            <xsl:comment>F3: UFCOUNTRY</xsl:comment>
                            <country><xsl:value-of select="ufcountry"/></country>
                        </xsl:if>
                    </addr>
                    <contactParty>
                        <contactPerson>
                            <name>
                                <xsl:if test="string-length(ufcontacttitle) > 0">
                                    <xsl:comment>F4: UFCONTACTTITLE</xsl:comment>
                                    <prefix><xsl:value-of select="ufcontacttitle"/></prefix>
                                </xsl:if>

                                <xsl:if test="string-length(ufcontactfirstname) > 0">
                                    <xsl:comment>F4: UFCONTACTFIRSTNAME</xsl:comment>
                                    <given><xsl:value-of select="ufcontactfirstname"/></given>
                                </xsl:if>

                                <xsl:if test="string-length(ufcontactmiddlename) > 0">
                                    <xsl:comment>F4: UFCONTACTMIDDLENAME</xsl:comment>
                                    <given><xsl:value-of select="ufcontactmiddlename"/></given>
                                </xsl:if>

                                <xsl:if test="string-length(ufcontactlastname) > 0">
                                    <xsl:comment>F4: UFCONTACTLASTNAME</xsl:comment>
                                    <family><xsl:value-of select="ufcontactlastname"/></family>
                                </xsl:if>
                            </name>

                            <xsl:if test="string-length(ufcontactphone) > 0">
                                <xsl:comment>F5: UFCONTACTPHONE</xsl:comment>
                                <telecome value="{ufcontactphone}"/>
                            </xsl:if>

                            <xsl:if test="string-length(ufcontactemail) > 0">
                                <xsl:comment>F3: UFCONTACTEMAIL</xsl:comment>
                                <telecome value="{ufcontactemail}"/>
                            </xsl:if>

                        </contactPerson>
                    </contactParty>
                </assignedOrganization>
            </assignedEntity>
        </author>
    </xsl:template>

    <xsl:template match="allmanufacturers" mode="part-l">
        <xsl:choose>
            <xsl:when test="string-length(receiptdate) >0">
                <xsl:comment>G3: RECEIPTDATE</xsl:comment>
                <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                    <xsl:with-param name="element">high</xsl:with-param>
                    <xsl:with-param name="value" select="receiptdate"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <high/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- F14: Fmanufacturer-->
    <xsl:template match="fmanufacturer" mode="part-m">
        <assignedEntity>
            <xsl:comment>F14: Manufacturer Name/Address</xsl:comment>
            <assignedOrganization>
                <code code="C53616" codeSystem="{$emdrOidObservationCode}" codeSystemName="Report_Receiver"/>
                <xsl:if test="string-length(fmfrname) > 0">
                    <name><xsl:value-of select="fmfrname"/></name>
                </xsl:if>

                <xsl:if test="string-length(fmfrfax) > 0">
                    <telecom value="{fmfrfax}"/>
                </xsl:if>

                <xsl:if test="string-length(fmfremail) > 0">
                    <telecom value="{fmfremail}"/>
                </xsl:if>

                <addr>
                    <xsl:if test="string-length(fmfraddress1) > 0">
                        <streetAddressLine><xsl:value-of select="fmfraddress1"/></streetAddressLine>
                    </xsl:if>

                    <xsl:if test="string-length(fmfraddress2) > 0">
                        <streetAddressLine><xsl:value-of select="fmfraddress2"/></streetAddressLine>
                    </xsl:if>

                    <xsl:if test="string-length(fmfrcity) > 0">
                        <city><xsl:value-of select="fmfrcity"/></city>
                    </xsl:if>

                    <xsl:if test="string-length(fmfrstate) > 0">
                        <state><xsl:value-of select="fmfrstate"/></state>
                    </xsl:if>

                    <xsl:if test="string-length(fmfrpostalcode) > 0">
                        <postalCode><xsl:value-of select="fmfrpostalcode"/></postalCode>
                    </xsl:if>

                    <xsl:if test="string-length(fmfrcountry) > 0">
                        <country><xsl:value-of select="fmfrcountry"/></country>
                    </xsl:if>
                </addr>
            </assignedOrganization>
        </assignedEntity>
    </xsl:template>

    <!-- B2 : Outcomes Attributed to Adverse Event -->
    <xsl:template match="adverseevent" mode="part-n">
        <xsl:if test="string-length(seriousnessdeath) > 0">
            <pertinentInformation2>
                <caseSeriousness>
                    <code code="C49489" codeSystem="{$emdrOidObservationCode}" codeSystemName="Adverse_Event_Outcome"/>
                    <value code="{seriousnessdeath}" codeSystem="{$emdrOidObservationCode}" xsi:type="CE"/>
                </caseSeriousness>
            </pertinentInformation2>
        </xsl:if>

        <xsl:if test="string-length(seriousnesslifethreatening) > 0">
            <pertinentInformation2>
                <caseSeriousness>
                    <code code="C49489" codeSystem="{$emdrOidObservationCode}" codeSystemName="Adverse_Event_Outcome"/>
                    <value code="{seriousnesslifethreatening}" codeSystem="{$emdrOidObservationCode}" xsi:type="CE"/>
                </caseSeriousness>
            </pertinentInformation2>
        </xsl:if>

        <xsl:if test="string-length(seriousnesshospitalization) > 0">
            <pertinentInformation2>
                <caseSeriousness>
                    <code code="C49489" codeSystem="{$emdrOidObservationCode}" codeSystemName="Adverse_Event_Outcome"/>
                    <value code="{seriousnesshospitalization}" codeSystem="{$emdrOidObservationCode}" xsi:type="CE"/>
                </caseSeriousness>
            </pertinentInformation2>
        </xsl:if>

        <xsl:if test="string-length(seriousnessdisabling) > 0">
            <pertinentInformation2>
                <caseSeriousness>
                    <code code="C49489" codeSystem="{$emdrOidObservationCode}" codeSystemName="Adverse_Event_Outcome"/>
                    <value code="{seriousnessdisabling}" codeSystem="{$emdrOidObservationCode}" xsi:type="CE"/>
                </caseSeriousness>
            </pertinentInformation2>
        </xsl:if>

        <xsl:if test="string-length(seriousnesscongenitalanomali) > 0">
            <pertinentInformation2>
                <caseSeriousness>
                    <code code="C49489" codeSystem="{$emdrOidObservationCode}" codeSystemName="Adverse_Event_Outcome"/>
                    <value code="{seriousnesscongenitalanomali}" codeSystem="{$emdrOidObservationCode}" xsi:type="CE"/>
                </caseSeriousness>
            </pertinentInformation2>
        </xsl:if>

        <xsl:if test="string-length(seriousnessreqintervention) > 0">
            <pertinentInformation2>
                <caseSeriousness>
                    <code code="C49489" codeSystem="{$emdrOidObservationCode}" codeSystemName="Adverse_Event_Outcome"/>
                    <value code="{seriousnessreqintervention}" codeSystem="{$emdrOidObservationCode}" xsi:type="CE"/>
                </caseSeriousness>
            </pertinentInformation2>
        </xsl:if>

        <xsl:if test="string-length(seriousnessother) > 0">
            <pertinentInformation2>
                <caseSeriousness>
                    <code code="C49489" codeSystem="{$emdrOidObservationCode}" codeSystemName="Adverse_Event_Outcome"/>
                    <value code="{seriousnessother}" codeSystem="{$emdrOidObservationCode}" xsi:type="CE"/>
                </caseSeriousness>
            </pertinentInformation2>
        </xsl:if>
    </xsl:template>

    <xsl:template match="suspectdevice" mode="part-o">
        <pertainsTo>
            <procedureEvent>
                <code nullFlavor="ASKU" />
                <device>
                    <identifiedDevice>
                        <identifiedDevice>
                            <!-- Id -->
                            <xsl:choose>
                                <xsl:when test="string-length(serialnumber)> 0">
                                    <xsl:comment>D4: SERIAL NUMBER</xsl:comment>
                                    <xsl:call-template name="ElemWithExtensionAttrOrNullFlavor">
                                        <xsl:with-param name="element">id</xsl:with-param>
                                        <xsl:with-param name="value" select="serialnumber"/>
                                    </xsl:call-template>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:comment>D4: SERIAL NUMBER</xsl:comment>
                                    <id/>
                                </xsl:otherwise>
                            </xsl:choose>

                            <!-- existenceTime-->
                            <xsl:choose>
                                <xsl:when test="string-length(hdevicemfrinfo/devmfrdate)> 0">
                                    <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                                        <xsl:with-param name="element">existenceTime</xsl:with-param>
                                        <xsl:with-param name="value" select="hdevicemfrinfo/devmfrdate"/>
                                    </xsl:call-template>
                                </xsl:when>
                                <xsl:otherwise>
                                    <existenceTime/>
                                </xsl:otherwise>
                            </xsl:choose>

                            <xsl:choose>
                                <xsl:when test="string-length(lotnumber)> 0">
                                    <lotNumberText mediaType="text/plain">
                                        <xsl:value-of select="lotnumber"/>
                                    </lotNumberText>
                                </xsl:when>
                                <xsl:otherwise>
                                    <lotNumberText/>
                                </xsl:otherwise>
                            </xsl:choose>



                            <!-- expiration Time-->
                            <xsl:choose>
                                <xsl:when test="string-length(expirationdate)> 0">
                                    <xsl:comment>D4: EXPIRATION DATE</xsl:comment>
                                    <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                                        <xsl:with-param name="element">expirationTime</xsl:with-param>
                                        <xsl:with-param name="value" select="expirationdate"/>
                                    </xsl:call-template>
                                </xsl:when>
                                <xsl:otherwise>
                                    <expirationTime/>
                                </xsl:otherwise>
                            </xsl:choose>

                            <!-- Manufacturer details-->
                            <asManufacturedProduct>
                                <!-- Id -->
                                <xsl:choose>
                                    <xsl:when test="string-length(catalognumber)> 0">
                                        <xsl:comment>D4: CATALOG NUMBER</xsl:comment>
                                        <xsl:call-template name="ElemWithExtensionAttrOrNullFlavor">
                                            <xsl:with-param name="element">id</xsl:with-param>
                                            <xsl:with-param name="value" select="catalognumber"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:comment>D4: CATALOG NUMBER</xsl:comment>
                                        <id/>
                                    </xsl:otherwise>
                                </xsl:choose>

                                <xsl:choose>
                                    <xsl:when test="string-length(udinumber)> 0">
                                        <xsl:comment>D4: UDINUMBER</xsl:comment>
                                        <xsl:call-template name="ElemWithCodeAttrOrNullFlavor">
                                            <xsl:with-param name="element">code</xsl:with-param>
                                            <xsl:with-param name="value" select="udinumber"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <code/>
                                    </xsl:otherwise>
                                </xsl:choose>

                                <manufacturerOrReprocessor>
                                    <xsl:choose>
                                        <xsl:when test="count(devicemfrinfo) > 0">
                                            <xsl:comment>D3: DEVICE MANUFACTURER INFO</xsl:comment>
                                            <xsl:apply-templates select="devicemfrinfo" mode="part-p"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:comment>D3: DEVICE MANUFACTURER INFO</xsl:comment>
                                            <code/>
                                            <name/>
                                            <telecom/>
                                        </xsl:otherwise>
                                    </xsl:choose>

                                    <xsl:if test="count(hdevicemfrinfo)> 0 ">
                                        <xsl:apply-templates select="hdevicemfrinfo" mode="part-q"/>
                                    </xsl:if>

                                    <xsl:if test="count(allmanufacturers/gmfrcontactinfo)> 0 ">
                                        <xsl:apply-templates select="allmanufacturers/gmfrcontactinfo" mode="part-r"/>
                                    </xsl:if>

                                </manufacturerOrReprocessor>
                            </asManufacturedProduct>

                            <!-- Reprocessor Details-->
                            <xsl:comment>D7b: Reprocessor Details</xsl:comment>
                            <asManufacturedProduct>
                                <manufacturerOrReprocessor>
                                    <code code="C53614" codeSystem="{$emdrOidObservationCode}" codeSystemName="Type_of_Manufacturer"/>
                                    <xsl:apply-templates select="reprocessorinfo" mode="part-s"/>
                                </manufacturerOrReprocessor>
                            </asManufacturedProduct>

                            <inventoryItem>
                                <manufacturedDeviceModel>

                                    <xsl:choose>
                                        <xsl:when test="string-length(modelnumber) > 0">
                                            <xsl:call-template name="ElemWithExtensionAttrOrNullFlavor">
                                                <xsl:with-param name="element">id</xsl:with-param>
                                                <xsl:with-param name="value" select="modelnumber"/>
                                            </xsl:call-template>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <id/>
                                        </xsl:otherwise>
                                    </xsl:choose>

                                    <xsl:choose>
                                        <xsl:when test="string-length(typeofdevicename) > 0 and string-length(typeofdevicecode) > 0">
                                            <xsl:comment>D2: TYPE OF DEVICE</xsl:comment>
                                            <code code="{typeofdevicecode}" codeSystemName="Type_of_Device" codeSystem="{$emdrOidObservationCode}">
                                                <originalText mediaType="text/plain"><xsl:value-of select="./typeofdevicename"/></originalText>
                                            </code>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:comment>D2: TYPE OF DEVICE</xsl:comment>
                                            <code/>
                                        </xsl:otherwise>
                                    </xsl:choose>

                                    <xsl:choose>
                                        <xsl:when test="string-length(brandname) > 0">
                                            <xsl:comment>D1: BRAND NAME</xsl:comment>
                                            <manufacturerModelName mediaType="text/plain"><xsl:value-of select="brandname"/></manufacturerModelName>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:comment>D1: BRAND NAME</xsl:comment>
                                            <manufacturerModelName/>
                                        </xsl:otherwise>
                                    </xsl:choose>

                                    <xsl:if test="string-length(allmanufacturers/premarketnumber) > 0">
                                        <xsl:comment>G4: PREMARKET NUMBER/PMA/510k number</xsl:comment>
                                        <asRegulatedProduct>
                                            <xsl:call-template name="ElemWithExtensionAttrOrNullFlavor">
                                                <xsl:with-param name="element">id</xsl:with-param>
                                                <xsl:with-param name="value" select="allmanufacturers/premarketnumber"/>
                                            </xsl:call-template>
                                        </asRegulatedProduct>
                                    </xsl:if>

                                </manufacturedDeviceModel>
                            </inventoryItem>
                        </identifiedDevice>

                        <!-- D9: Device available for evaluation -->
                        <xsl:if test="string-length(datedevicereturntomfr) > 0 or string-length(deviceavailableforeval) > 0">
                            <xsl:comment>D9: Device available for evaluation</xsl:comment>
                            <subjectOf>
                                <deviceObservation>
                                    <code code="C53449" codeSystem="{$emdrOidObservationCode}" codeSystemName="Device_available_for_evaluation"/>
                                    <xsl:if test="string-length(datedevicereturntomfr) > 0">
                                        <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                                            <xsl:with-param name="element">effectiveTime</xsl:with-param>
                                            <xsl:with-param name="value" select="datedevicereturntomfr"/>
                                        </xsl:call-template>
                                    </xsl:if>

                                    <xsl:if test="string-length(deviceavailableforeval) > 0">
                                        <value xsi:type="BL">
                                            <xsl:variable name="isElemHasNullFlavour">
                                                <xsl:call-template name="hasNullFlavour">
                                                    <xsl:with-param name="value" select="deviceavailableforeval"/>
                                                </xsl:call-template>
                                            </xsl:variable>
                                            <xsl:choose>
                                                <xsl:when test="$isElemHasNullFlavour = 'yes'">
                                                    <xsl:variable name="NullFlavourValue">
                                                        <xsl:call-template name="getNFValue">
                                                            <xsl:with-param name="nfvalue" select="deviceavailableforeval"/>
                                                        </xsl:call-template>
                                                    </xsl:variable>
                                                    <xsl:attribute name="nullFlavor">
                                                        <xsl:value-of select="$NullFlavourValue"/>
                                                    </xsl:attribute>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:attribute name="value">
                                                        <xsl:value-of select="deviceavailableforeval"/>
                                                    </xsl:attribute>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </value>
                                    </xsl:if>
                                </deviceObservation>
                            </subjectOf>
                        </xsl:if>


                        <xsl:if test="string-length(../ufimporterinfo/approxdeviceage) > 0 and string-length(../ufimporterinfo/approxdeviceagetext) > 0">
                            <xsl:comment>F9: APPROXDEVICEAGE and APPROXDEVICEAGETEXT</xsl:comment>
                            <subjectOf>
                                <deviceObservation>
                                    <code code="C53451" codeSystem="{$emdrOidObservationCode}" codeSystemName="Approximate_Age_of_Device"/>
                                    <value xsi:type="PQ">
                                        <xsl:if test="string-length(../ufimporterinfo/approxdeviceage) > 0">
                                            <xsl:variable name="isElemHasNullFlavour">
                                                <xsl:call-template name="hasNullFlavour">
                                                    <xsl:with-param name="value" select="../ufimporterinfo/approxdeviceage"/>
                                                </xsl:call-template>
                                            </xsl:variable>
                                            <xsl:choose>
                                                <xsl:when test="$isElemHasNullFlavour = 'yes'">
                                                    <xsl:variable name="NullFlavourValue">
                                                        <xsl:call-template name="getNFValue">
                                                            <xsl:with-param name="nfvalue" select="../ufimporterinfo/approxdeviceage"/>
                                                        </xsl:call-template>
                                                    </xsl:variable>
                                                    <xsl:attribute name="nullFlavor">
                                                        <xsl:value-of select="$NullFlavourValue"/>
                                                    </xsl:attribute>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:attribute name="value">
                                                        <xsl:value-of select="../ufimporterinfo/approxdeviceage"/>
                                                    </xsl:attribute>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:if>
                                        <xsl:if test="string-length(../ufimporterinfo/approxdeviceagetext) > 0">
                                            <xsl:attribute name="unit"><xsl:value-of select="../ufimporterinfo/approxdeviceagetext"/></xsl:attribute>
                                        </xsl:if>
                                    </value>
                                </deviceObservation>
                            </subjectOf>
                        </xsl:if>

                        <xsl:if test="count(../ufimporterinfo/devicecodes) > 0">
                            <xsl:apply-templates select="../ufimporterinfo/devicecodes" mode="part-devicecode"/>
                        </xsl:if>

                        <xsl:if test="count(hdevicemfrinfo/mfrdevicecodes) > 0">
                            <xsl:apply-templates select="hdevicemfrinfo/mfrdevicecodes" mode="part-t"/>
                        </xsl:if>

                        <xsl:if test="count(../ufimporterinfo/componentcodes) > 0">
                            <xsl:apply-templates select="../ufimporterinfo/componentcodes" mode="part-componentcodes"/>
                        </xsl:if>

                        <xsl:if test="count(hdevicemfrinfo/mfrcomponentcodes) > 0">
                            <xsl:apply-templates select="hdevicemfrinfo/mfrcomponentcodes" mode="part-mfrcomponentcodes"/>
                        </xsl:if>

                        <xsl:if test="count(hdevicemfrinfo) > 0">
                            <xsl:apply-templates select="hdevicemfrinfo" mode="part-hdevicemfrinfo"/>
                        </xsl:if>

                    </identifiedDevice>
                </device>

                <xsl:choose>
                    <xsl:when test="string-length(operatorofdevice) > 0">
                        <authorOrPerformer typeCode="AUT">
                            <assignedEntity>
                                <code code="{operatorofdevice}" codeSystemName="Operator_of_Medical_Device" codeSystem="{$emdrOidObservationCode}">
                                    <xsl:if test="string-length(operatorofdeviceother) > 0">
                                        <originalText mediaType="text/plain"><xsl:value-of select="operatorofdeviceother"/></originalText>
                                    </xsl:if>
                                </code>
                            </assignedEntity>
                        </authorOrPerformer>
                    </xsl:when>
                    <xsl:otherwise>
                        <authorOrPerformer typeCode="AUT">
                            <assignedEntity />
                        </authorOrPerformer>
                    </xsl:otherwise>
                </xsl:choose>


                <xsl:choose>
                    <xsl:when test="string-length(singleusereprocorreuse) > 0">
                        <pertinentInformation1>
                            <observation moodCode="EVN">
                                <code code="C53563" codeSystem="{$emdrOidObservationCode}" codeSystemName="Single-Use_Device_Reprocessed_and_Reused_on_Patient"/>
                                <value xsi:type="BL" value="{singleusereprocorreuse}"/>
                            </observation>
                        </pertinentInformation1>
                    </xsl:when>
                    <xsl:otherwise>
                        <pertinentInformation1>
                            <observation moodCode="EVN">
                                <code/>
                            </observation>
                        </pertinentInformation1>
                    </xsl:otherwise>
                </xsl:choose>


                <!-- D8: Serviced by Third Party -->
                <xsl:comment>D8: Serviced by Third Party</xsl:comment>
                <xsl:choose>
                    <xsl:when test="string-length(thirdpartyserviced) > 0">
                        <pertinentInformation1>
                            <observation moodCode="EVN">
                                <code code="C85488" codeSystem="{$emdrOidObservationCode}" codeSystemName="Device_Serviced_By_Third_Party"/>
                                <value xsi:type="BL">
                                    <xsl:variable name="isElemHasNullFlavour">
                                        <xsl:call-template name="hasNullFlavour">
                                            <xsl:with-param name="value" select="thirdpartyserviced"/>
                                        </xsl:call-template>
                                    </xsl:variable>
                                    <xsl:choose>
                                        <xsl:when test="$isElemHasNullFlavour = 'yes'">
                                            <xsl:variable name="NullFlavourValue">
                                                <xsl:call-template name="getNFValue">
                                                    <xsl:with-param name="nfvalue" select="thirdpartyserviced"/>
                                                </xsl:call-template>
                                            </xsl:variable>
                                            <xsl:attribute name="nullFlavor">
                                                <xsl:value-of select="$NullFlavourValue"/>
                                            </xsl:attribute>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:attribute name="value">
                                                <xsl:value-of select="thirdpartyserviced"/>
                                            </xsl:attribute>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </value>
                            </observation>
                        </pertinentInformation1>
                    </xsl:when>
                    <xsl:otherwise>
                        <pertinentInformation1>
                            <observation moodCode="EVN">
                                <code/>
                            </observation>
                        </pertinentInformation1>
                    </xsl:otherwise>
                </xsl:choose>


                <!-- D10: Concomitant medical products -->
                <xsl:if test="count(concommedicalproducts) > 0">
                    <xsl:comment>D10: Concomitant medical products </xsl:comment>
                    <xsl:for-each select="concommedicalproducts">
                        <pertinentInformation1>
                            <observation moodCode="EVN">
                                <code code="C53630" codeSystem="{$emdrOidObservationCode}" codeSystemName="Concomitant_Therapy"/>
                                <xsl:choose>
                                    <xsl:when test="string-length(./concomtherapydates) > 0">
                                        <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                                            <xsl:with-param name="element">effectiveTime</xsl:with-param>
                                            <xsl:with-param name="value" select="./concomtherapydates"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <effectiveTime/>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <value xsi:type="ED" mediaType="text/plain"><xsl:value-of select="./concomproductname"/></value>
                            </observation>
                        </pertinentInformation1>
                    </xsl:for-each>
                </xsl:if>

                <xsl:if test="string-length(dateimplanted) > 0 ">
                    <component1>
                        <implantation>
                            <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                                <xsl:with-param name="element">effectiveTime</xsl:with-param>
                                <xsl:with-param name="value" select="dateimplanted"/>
                            </xsl:call-template>
                        </implantation>
                    </component1>
                </xsl:if>

                <xsl:if test="string-length(dateexplanted) > 0 ">
                    <component2>
                        <explantation>
                            <xsl:call-template name="ElemWithValueAttrOrNullFlavor">
                                <xsl:with-param name="element">effectiveTime</xsl:with-param>
                                <xsl:with-param name="value" select="dateexplanted"/>
                            </xsl:call-template>
                        </explantation>
                    </component2>
                </xsl:if>
            </procedureEvent>
        </pertainsTo>
    </xsl:template>

    <xsl:template match="devicemfrinfo" mode="part-p">
        <code code="C53616" codeSystem="{$emdrOidObservationCode}" codeSystemName="Type_of_Manufacturer"/>

        <!-- D3: manufacturere details -->

        <!-- name -->
        <xsl:choose>
            <xsl:when test="string-length(mfrname) > 0">
                <name><xsl:value-of select="mfrname"/></name>
            </xsl:when>
            <xsl:otherwise>
                <name/>
            </xsl:otherwise>
        </xsl:choose>

        <!--fax and email -->
        <xsl:if test="string-length(mfrfax) > 0">
            <telecom value="{mfrfax}"/>
        </xsl:if>

        <xsl:if test="string-length(mfremail) > 0">
            <telecom value="{mfremail}"/>
        </xsl:if>

        <xsl:if test="string-length(mfrfax) = 0 and string-length(mfremail) = 0">
            <telecom/>
        </xsl:if>


        <addr>
            <!-- street address details -->
            <xsl:if test="string-length(mfraddressline1) > 0">
                <streetAddressLine><xsl:value-of select="mfraddressline1"/></streetAddressLine>
            </xsl:if>

            <xsl:if test="string-length(mfraddressline2) > 0">
                <streetAddressLine><xsl:value-of select="mfraddressline2"/></streetAddressLine>
            </xsl:if>

            <!-- city, state , postal code and country details -->
            <xsl:if test="string-length(mftcity) > 0">
                <city><xsl:value-of select="mftcity"/></city>
            </xsl:if>

            <xsl:if test="string-length(mfrstate) > 0">
                <state><xsl:value-of select="mfrstate"/></state>
            </xsl:if>

            <xsl:if test="string-length(mfrpostalcode) > 0">
                <postalCode><xsl:value-of select="mfrpostalcode"/></postalCode>
            </xsl:if>

            <xsl:if test="string-length(mfrcountry) > 0">
                <country><xsl:value-of select="mfrcountry"/></country>
            </xsl:if>
        </addr>
    </xsl:template>

    <xsl:template match="/ichicsr/icsreport/safetyreport/suspectdevice/hdevicemfrinfo" mode="part-q">
        <asRole>
            <performance>
                <investigationEvent>
                    <!-- H6: evaluation result-->
                    <xsl:if test="count(evalresult) > 0">
                        <xsl:for-each select="evalresult">
                            <xsl:if test="string-length(./evalcoderesult) > 0 ">
                                <xsl:comment>H6: EVAL CODE RESULT</xsl:comment>
                                <component1>
                                    <evaluationResult>
                                        <code code="C53985" codeSystem="{$emdrOidObservationCode}" codeSystemName="Evaluation_Result_Code"/>
                                        <value xsi:type="CE" code="{./evalcoderesult}" codeSystem="{$emdrOidObservationCode}"/>
                                    </evaluationResult>
                                </component1>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:if>

                    <!-- H6: evaluation conclusion-->
                    <xsl:if test="count(evalconclusion) > 0">
                        <xsl:for-each select="evalconclusion">
                            <xsl:if test="string-length(./evalcodeconclusion) > 0 ">
                                <xsl:comment>H6: EVAL CONCLUSION</xsl:comment>
                                <component2>
                                    <evaluationConclusion>
                                        <code code="C53986" codeSystem="{$emdrOidObservationCode}" codeSystemName="Evaluation_Conclusion_Code"/>
                                        <value xsi:type="CE" code="{./evalcodeconclusion}" codeSystem="{$emdrOidObservationCode}"/>
                                    </evaluationConclusion>
                                </component2>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:if>

                    <!-- H6: evaluation method-->
                    <xsl:if test="evalmethod">
                        <xsl:for-each select="evalmethod">
                            <xsl:if test="string-length(./evalcodemethod) > 0 ">
                                <xsl:comment>H6: EVAL METHOD</xsl:comment>
                                <component3>
                                    <evaluationMethod>
                                        <code code="C53984" codeSystem="{$emdrOidObservationCode}" codeSystemName="Evaluation_Method_Code"/>
                                        <value xsi:type="CE" code="{./evalcodemethod}" codeSystem="{$emdrOidObservationCode}"/>
                                    </evaluationMethod>
                                </component3>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:if>
                </investigationEvent>
            </performance>
        </asRole>
    </xsl:template>

    <xsl:template match="gmfrcontactinfo" mode="part-r">
        <xsl:variable name="positiongmfrcontactinfo">
            <xsl:value-of select="position()"/>
        </xsl:variable>
        <xsl:comment>G1: GMFR CONTACT INFORMATION</xsl:comment>
        <contactParty>
            <xsl:if test="string-length(mfrcontactfacility) > 0 or string-length(mfrcontactstreetaddress1) > 0 or string-length(mfrcontactstreetaddress2) > 0 or string-length(mfrcontactcity) > 0 or
              string-length(mfrcontactstate) > 0 or string-length(mfrcontactpostcode) > 0 or string-length(mfrcontactcountrycode) > 0">
                <addr>
                    <xsl:if test="string-length(mfrcontactfacility) > 0">
                        <additionalLocator><xsl:value-of select="mfrcontactfacility"/></additionalLocator>
                    </xsl:if>

                    <xsl:if test="string-length(mfrcontactstreetaddress1) > 0">
                        <streetAddressLine><xsl:value-of select="mfrcontactstreetaddress1"/></streetAddressLine>
                    </xsl:if>

                    <xsl:if test="string-length(mfrcontactstreetaddress2) > 0">
                        <streetAddressLine><xsl:value-of select="mfrcontactstreetaddress2"/></streetAddressLine>
                    </xsl:if>

                    <xsl:if test="string-length(mfrcontactcity) > 0">
                        <city><xsl:value-of select="mfrcontactcity"/></city>
                    </xsl:if>

                    <xsl:if test="string-length(mfrcontactstate) > 0">
                        <state><xsl:value-of select="mfrcontactstate"/></state>
                    </xsl:if>

                    <xsl:if test="string-length(mfrcontactpostcode) > 0">
                        <postalCode><xsl:value-of select="mfrcontactpostcode"/></postalCode>
                    </xsl:if>

                    <xsl:if test="string-length(mfrcontactcountrycode) > 0">
                        <country><xsl:value-of select="mfrcontactcountrycode"/></country>
                    </xsl:if>
                </addr>
            </xsl:if>

            <xsl:if test="string-length(mfrcontactfax) > 0">
                <telecom value="{mfrcontactfax}"/>
            </xsl:if>

            <xsl:if test="string-length(mfrcontactemailaddress) > 0">
                <telecom value="{mfrcontactemailaddress}"/>
            </xsl:if>

            <xsl:if test="string-length(mfrcontacttel) > 0">
                <telecom value="{mfrcontacttel}"/>
            </xsl:if>

            <xsl:if test="string-length(mfrcontactfax) = 0 and string-length(mfrcontactemailaddress) = 0 and string-length(mfrcontacttel) = 0">
                <telecom/>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="string-length(mfrcontacttitle) > 0 or string-length(mfrcontactgivename) > 0 or string-length(mfrcontactmiddlename) > 0 or string-length(mfrcontactfamilyname) > 0">
                    <contactManufacturerContact>
                        <name>
                            <xsl:if test="string-length(mfrcontacttitle) > 0">
                                <prefix><xsl:value-of select="mfrcontacttitle"/></prefix>
                            </xsl:if>

                            <xsl:if test="string-length(mfrcontactgivename) > 0">
                                <given><xsl:value-of select="mfrcontactgivename"/></given>
                            </xsl:if>

                            <xsl:if test="string-length(mfrcontactmiddlename) > 0">
                                <given><xsl:value-of select="mfrcontactmiddlename"/></given>
                            </xsl:if>

                            <xsl:if test="string-length(mfrcontactfamilyname) > 0">
                                <family><xsl:value-of select="mfrcontactfamilyname"/></family>
                            </xsl:if>
                        </name>
                    </contactManufacturerContact>
                </xsl:when>
                <xsl:otherwise>
                    <contactManufacturerContact/>
                </xsl:otherwise>
            </xsl:choose>
        </contactParty>
    </xsl:template>

    <xsl:template match="reprocessorinfo" mode="part-s">

        <xsl:choose>
            <xsl:when test="string-length(reprocname) > 0">
                <name><xsl:value-of select="reprocname"/></name>
            </xsl:when>
            <xsl:otherwise>
                <name/>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="string-length(reprocfax)> 0 ">
            <telecom value="{reprocfax}"/>
        </xsl:if>

        <xsl:if test="string-length(reprocemail)> 0 ">
            <telecom value="{reprocemail}"/>
        </xsl:if>

        <xsl:if test="string-length(reprocfax) = 0 and string-length(reprocemail) = 0">
            <telecom />
        </xsl:if>

        <addr>
            <xsl:if test="string-length(reprocaddress1) > 0 ">
                <streetAddressLine><xsl:value-of select="reprocaddress1"/></streetAddressLine>
            </xsl:if>

            <xsl:if test="string-length(reprocaddress2) > 0 ">
                <streetAddressLine><xsl:value-of select="reprocaddress2"/></streetAddressLine>
            </xsl:if>

            <xsl:if test="string-length(reproccity) > 0 ">
                <city><xsl:value-of select="reproccity"/></city>
            </xsl:if>

            <xsl:if test="string-length(reprocstate) > 0 ">
                <state><xsl:value-of select="reprocstate"/></state>
            </xsl:if>

            <xsl:if test="string-length(reprocpostalcode) > 0 ">
                <postalCode><xsl:value-of select="reprocpostalcode"/></postalCode>
            </xsl:if>

            <xsl:if test="string-length(reproccountry) > 0 ">
                <country><xsl:value-of select="reproccountry"/></country>
            </xsl:if>
        </addr>
    </xsl:template>

    <xsl:template match="devicecodes" mode="part-devicecode">
        <xsl:for-each select="devicecodes">
            <xsl:if test="string-length(./fdadevicecodes) > 0">
                <subjectOf>
                    <deviceObservation>
                        <code code="C53982" codeSystem="{$emdrOidObservationCode}" codeSystemName="Device_Problem_Code"/>
                        <value xsi:type="CE" codeSystem="{$emdrOidObservationCode}">
                            <xsl:attribute name="code"><xsl:value-of select="./fdadevicecodes"/></xsl:attribute>
                        </value>
                    </deviceObservation>
                </subjectOf>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="mfrdevicecodes" mode="part-t">
        <xsl:variable name="positionmfrdevicecodes">
            <xsl:value-of select="position()"/>
        </xsl:variable>
        <xsl:if test="string-length(mfrdevicecode) > 0">
            <xsl:comment>H6: Device Problem Code</xsl:comment>
            <subjectOf>
                <deviceObservation>
                    <code code="C53982" codeSystem="{$emdrOidObservationCode}" codeSystemName="Device_Problem_Code"/>
                    <value xsi:type="CE" code="{mfrdevicecode}" codeSystem="{$emdrOidObservationCode}"/>
                </deviceObservation>
            </subjectOf>
        </xsl:if>
    </xsl:template>

    <xsl:template match="componentcodes" mode="part-componentcodes">
        <xsl:for-each select="componentcodes">
            <xsl:if test="string-length(./fdacomponentcodes) > 0">
                <subjectOf>
                    <deviceObservation>
                        <code code="C54577" codeSystem="{$emdrOidObservationCode}" codeSystemName="Component_Code"/>
                        <value xsi:type="CE" codeSystem="{$emdrOidObservationCode}">
                            <xsl:attribute name="code"><xsl:value-of select="./fdacomponentcodes"/></xsl:attribute>
                        </value>
                    </deviceObservation>
                </subjectOf>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <!-- H6: Component Code -->
    <xsl:template match="mfrcomponentcodes" mode="part-mfrcomponentcodes">
        <xsl:variable name="positionmfrcomponentcodes">
            <xsl:value-of select="position()"/>
        </xsl:variable>
        <xsl:if test="string-length(mfrcomponentcode) > 0">
            <xsl:comment>H6: Component Code - (<xsl:value-of select="$positionmfrcomponentcodes"/>)</xsl:comment>
            <subjectOf>
                <deviceObservation>
                    <code code="C54577" codeSystem="{$emdrOidObservationCode}" codeSystemName="Component_Code"/>
                    <value xsi:type="CE" code="{mfrcomponentcode}" codeSystem="{$emdrOidObservationCode}"/>
                </deviceObservation>
            </subjectOf>
        </xsl:if>
    </xsl:template>

    <xsl:template match="hdevicemfrinfo" mode="part-hdevicemfrinfo">
        <!-- Start: F10 Component Code -->
        <xsl:if test="string-length(deviceevalbymfr) > 0 ">
            <xsl:comment>Start: F10 Component Code</xsl:comment>
            <subjectOf>
                <deviceObservation>
                    <code code="C53629" codeSystem="{$emdrOidObservationCode}" codeSystemName="Device_Evaluated_By_Manufacturer"/>
                    <value xsi:type="BL" value="{deviceevalbymfr}"/>
                </deviceObservation>
            </subjectOf>
        </xsl:if>

        <xsl:if test="string-length(devreturntomfr) > 0 ">
            <xsl:comment>H3: DEVICE RETURN TO MFR</xsl:comment>
            <subjectOf>
                <deviceObservation>
                    <code code="C53591" codeSystem="{$emdrOidObservationCode}" codeSystemName="Device_Returned_To_Manufacturer_For_Evaluation"/>
                    <value xsi:type="BL" value="{devreturntomfr}"/>
                </deviceObservation>
            </subjectOf>
        </xsl:if>

        <xsl:if test="string-length(deviceevalsummaryattach) > 0 ">
            <xsl:comment>H3: DEVICE EVALUATION SUMMARY ATTACH</xsl:comment>
            <subjectOf>
                <deviceObservation>
                    <code code="C53592" codeSystem="{$emdrOidObservationCode}" codeSystemName="Evaluation_Summary_Status"/>
                    <value xsi:type="BL" value="{deviceevalsummaryattach}"/>
                </deviceObservation>
            </subjectOf>
        </xsl:if>

        <xsl:if test="string-length(devicenotevaluatedreason) > 0 ">
            <xsl:comment>H3: DEVICE NOT EVALUATED REASON</xsl:comment>
            <subjectOf>
                <deviceObservation>
                    <code code="C53593" codeSystem="{$emdrOidObservationCode}" codeSystemName="Reason_for_Non-Evaluation"/>
                    <value xsi:type="CE" code="{devicenotevaluatedreason}" codeSystem="{$emdrOidObservationCode}">
                        <xsl:if test="string-length(devicenotevaluatedreasonother) > 0">
                            <originalText><xsl:value-of select="devicenotevaluatedreasonother"/></originalText>
                        </xsl:if>
                    </value>
                </deviceObservation>
            </subjectOf>
        </xsl:if>

        <xsl:if test="string-length(labelsingleuse) > 0 ">
            <xsl:comment>H5: LABEL SINGLE USE</xsl:comment>
            <subjectOf>
                <deviceObservation>
                    <code code="C53602" codeSystem="{$emdrOidObservationCode}" codeSystemName="Device_Labeled_for_single_use"/>
                    <value xsi:type="BL" value="{labelsingleuse}"/>
                </deviceObservation>
            </subjectOf>
        </xsl:if>

        <!-- H7 : REMEDIALACTION -->
        <xsl:if test="count(remedialaction) > 0">
            <xsl:for-each select="remedialaction">
                <xsl:if test="string-length(./remedialactinitiated) > 0 ">
                    <xsl:comment>H7 : REMEDIAL ACTION </xsl:comment>
                    <subjectOf>
                        <deviceObservation>
                            <code code="C53603" codeSystem="{$emdrOidObservationCode}" codeSystemName="Type_of_Remedial_Action"/>
                            <value xsi:type="CE" codeSystem="{$emdrOidObservationCode}">
                                <xsl:attribute name="code"><xsl:value-of select="./remedialactinitiated"/></xsl:attribute>
                                <xsl:if test="./remedialactinitiated = 'C17649'">
                                    <originalText><xsl:value-of select="./remedialactother"/></originalText>
                                </xsl:if>
                            </value>
                        </deviceObservation>
                    </subjectOf>
                </xsl:if>
            </xsl:for-each>
        </xsl:if>

        <!-- H8: Usage of Device -->

        <xsl:if test="string-length(usageofdevice) > 0 ">
            <xsl:comment>H8: Usage of Device</xsl:comment>
            <subjectOf>
                <deviceObservation>
                    <code code="C53645" codeSystem="{$emdrOidObservationCode}" codeSystemName="Usage_of_Device"/>
                    <value xsi:type="CE" code="{usageofdevice}" codeSystem="{$emdrOidObservationCode}"/>
                </deviceObservation>
            </subjectOf>
        </xsl:if>

        <xsl:if test="string-length(uscnumber) > 0 ">
            <xsl:comment>H9: USC NUMBER</xsl:comment>
            <subjectOf>
                <deviceObservation>
                    <code code="C53619" codeSystem="{$emdrOidObservationCode}" codeSystemName="Corrective_Action_Number"/>
                    <value xsi:type="ED" mediaType="text/plain"><xsl:value-of select="uscnumber"/></value>
                </deviceObservation>
            </subjectOf>
        </xsl:if>

        <xsl:if test="string-length(relatedreportnum) > 0">
            <xsl:comment>H10: Related Report Numbers</xsl:comment>
            <subjectOf>
                <deviceObservation>
                    <code code="C103162" codeSystem="2.16.840.1.113883.3.26.1.1" codeSystemName="Related Report Numbers"/>
                    <value xsi:type="ED" mediaType="text/plain"><xsl:value-of select="relatedreportnum"/></value>
                </deviceObservation>
            </subjectOf>
        </xsl:if>
    </xsl:template>

    <xsl:template match="safetyreport" mode="part-v">
        <component>
            <adverseEventAssessment>
                <subject1>
                    <primaryRole>
                        <xsl:if test="count(suspectproducts) > 0">
                            <xsl:for-each select="suspectproducts">
                                <xsl:comment>C : SUSPECT PRODUCTS</xsl:comment>
                                <subjectOf2>
                                    <xsl:if test="string-length(./drugseqnum) > 0">
                                        <xsl:attribute name="drugSeqNo"><xsl:value-of select="./drugseqnum"/></xsl:attribute>
                                    </xsl:if>
                                    <organizer>
                                        <component>
                                            <substanceAdministration>
                                                <xsl:if test="string-length(./drugadministrationroutetext) > 0">
                                                    <xsl:comment>C3: DRUG ADMINISTRATION ROUTE TEXT</xsl:comment>
                                                    <routeCode code="{./drugadministrationroutetext}"/>
                                                </xsl:if>
                                                <xsl:if test="string-length(./drugstructuredosagenumb) > 0 and string-length(./drugstructuredosageunit) > 0">
                                                    <xsl:comment>C3: DRUG STRUCTURE DOSAGE NUMBER and DRUG STRUCTURE DOSAGE UNIT</xsl:comment>
                                                    <doseQuantity value="{./drugstructuredosagenumb}" unit="{./drugstructuredosageunit}"/>
                                                </xsl:if>
                                                <consumable>
                                                    <instanceOfKind>
                                                        <productInstance>
                                                            <xsl:if test="string-length(./drugbatchnumb) > 0">
                                                                <xsl:comment>C1: DRUG BATCH NUMBER</xsl:comment>
                                                                <lotNumberText><xsl:value-of select="./drugbatchnumb"/></lotNumberText>
                                                            </xsl:if>

                                                            <xsl:choose>
                                                                <xsl:when test="string-length(./drugexpirationdate)> 0">
                                                                    <xsl:comment>C7: DRUG EXPIRATION DATE</xsl:comment>
                                                                    <expirationTime value="{./drugexpirationdate}"/>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:comment>C7: DRUG EXPIRATION DATE</xsl:comment>
                                                                    <expirationTime/>
                                                                </xsl:otherwise>
                                                            </xsl:choose>
                                                        </productInstance>
                                                        <kindOfProduct>
                                                            <xsl:if test="string-length(./ndcoruniqueid) > 0">
                                                                <xsl:comment>C1: ndcoruniqueid</xsl:comment>
                                                                <code><xsl:value-of select="./ndcoruniqueid"/></code>
                                                            </xsl:if>

                                                            <xsl:if test="string-length(./drugname) > 0">
                                                                <xsl:comment>C1: DRUG NAME</xsl:comment>
                                                                <name><xsl:value-of select="./drugname"/></name>
                                                            </xsl:if>

                                                            <asManufacturedProduct>

                                                                <!-- Start: C6 Product Type -->
                                                                <xsl:if test="string-length(./compoundedproduct) > 0 ">
                                                                    <xsl:comment>Start: C6 Product Type</xsl:comment>
                                                                    <subjectOf>
                                                                        <characteristic>
                                                                            <code code="C94031" codeSystem="{$emdrOidObservationCode}" codeSystemName="Product_Compounded"/>
                                                                            <value xsi:type="BL" value="{./compoundedproduct}"/>
                                                                        </characteristic>
                                                                    </subjectOf>
                                                                </xsl:if>

                                                                <xsl:if test="string-length(./otcproduct) > 0 ">
                                                                    <xsl:comment>C6: OTCPRODUCT</xsl:comment>
                                                                    <subjectOf>
                                                                        <characteristic>
                                                                            <code code="C54068" codeSystem="{$emdrOidObservationCode}" codeSystemName="Product_OTC"/>
                                                                            <value xsi:type="BL" value="{./otcproduct}"/>
                                                                        </characteristic>
                                                                    </subjectOf>
                                                                </xsl:if>

                                                                <xsl:if test="string-length(./generic) > 0 ">
                                                                    <xsl:comment>C6: GENERIC</xsl:comment>
                                                                    <subjectOf>
                                                                        <characteristic>
                                                                            <code code="C151960" codeSystem="{$emdrOidObservationCode}" codeSystemName="Product_Generic"/>
                                                                            <value xsi:type="BL" value="{./generic}"/>
                                                                        </characteristic>
                                                                    </subjectOf>
                                                                </xsl:if>

                                                                <xsl:if test="string-length(./biosimilar) > 0 ">
                                                                    <xsl:comment>C6: BIOSIMILAR</xsl:comment>
                                                                    <subjectOf>
                                                                        <characteristic>
                                                                            <code code="C156644" codeSystem="{$emdrOidObservationCode}" codeSystemName="Product_Biosimilar"/>
                                                                            <value xsi:type="BL" value="{./biosimilar}"/>
                                                                        </characteristic>
                                                                    </subjectOf>
                                                                </xsl:if>

                                                                <xsl:if test="string-length(./preanda) > 0 ">
                                                                    <xsl:comment>C6: PREANDA</xsl:comment>
                                                                    <subjectOf>
                                                                        <characteristic>
                                                                            <code code="C73584" codeSystem="{$emdrOidObservationCode}" codeSystemName="PreANDA"/>
                                                                            <value xsi:type="BL" value="{./preanda}"/>
                                                                        </characteristic>
                                                                    </subjectOf>
                                                                </xsl:if>

                                                                <xsl:if test="string-length(./pre1938) > 0 ">
                                                                    <xsl:comment>C6: PRE1938</xsl:comment>
                                                                    <subjectOf>
                                                                        <characteristic>
                                                                            <code code="C93630" codeSystem="{$emdrOidObservationCode}" codeSystemName="Pre1938"/>
                                                                            <value xsi:type="BL" value="{./pre1938}"/>
                                                                        </characteristic>
                                                                    </subjectOf>
                                                                </xsl:if>

                                                                <approval>
                                                                    <xsl:if test="string-length(./approvalnum) > 0">
                                                                        <xsl:comment>C1: APPROVAL NUMBER</xsl:comment>
                                                                        <id extension="{./approvalnum}"/>
                                                                    </xsl:if>

                                                                    <xsl:if test="string-length(./drugtype) > 0">
                                                                        <xsl:comment>C1: DRUG TYPE</xsl:comment>
                                                                        <code code="{./drugtype}"/>
                                                                    </xsl:if>

                                                                    <xsl:if test="string-length(./protocolnum) > 0">
                                                                        <xsl:comment>C1: PROTOCOL NUMBER</xsl:comment>
                                                                        <text><xsl:value-of select="./protocolnum"/></text>
                                                                    </xsl:if>
                                                                </approval>

                                                                <manufacturerOrganization>
                                                                    <name><xsl:value-of select="./manufacturername"/></name>
                                                                </manufacturerOrganization>
                                                            </asManufacturedProduct>
                                                        </kindOfProduct>
                                                    </instanceOfKind>
                                                </consumable>

                                                <outboundRelationship2>
                                                    <!-- C9: Product Reappeared -->
                                                    <observation moodCode="EVN">
                                                        <code code="C54055" codeSystem="{$emdrOidObservationCode}" codeSystemName="Event_Reappeared"/>
                                                        <xsl:if test="string-length(./rechallenge) > 0 ">
                                                            <xsl:comment>C9: Product Reappeared</xsl:comment>
                                                            <value xsi:type="BL" value="{./rechallenge}"/>
                                                        </xsl:if>
                                                    </observation>


                                                    <!-- C8: Product Event Abated -->
                                                    <observation moodCode="EVN">
                                                        <code code="C86045" codeSystem="{$emdrOidObservationCode}" codeSystemName="Event_Abated"/>
                                                        <xsl:if test="string-length(./dechallenge) > 0 ">
                                                            <xsl:comment>C8: Product Event Abated</xsl:comment>
                                                            <value xsi:type="BL" value="{./dechallenge}"/>
                                                        </xsl:if>
                                                    </observation>

                                                    <substanceAdministration>
                                                        <xsl:if test="string-length(./drugseparatedosagenumb) > 0 ">
                                                            <numDosage value="{./drugseparatedosagenumb}"/>
                                                        </xsl:if>

                                                        <xsl:if test="string-length(./drugintervaldosageunitnumb) > 0 and string-length(./drugintervaldosagedefinition) > 0">
                                                            <effectiveTime>
                                                                <period value="{./drugintervaldosageunitnumb}" unit="{./drugintervaldosagedefinition}"/>
                                                            </effectiveTime>
                                                        </xsl:if>

                                                        <effectiveTime>
                                                            <phase>
                                                                <xsl:if test="string-length(./drugstartdate) > 0">
                                                                    <xsl:comment>C4: DRUG START DATE</xsl:comment>
                                                                    <low value="{./drugstartdate}"/>
                                                                </xsl:if>

                                                                <xsl:if test="string-length(./drugenddate) > 0">
                                                                    <xsl:comment>C4: DRUG END DATE</xsl:comment>
                                                                    <high value="{./drugenddate}"/>
                                                                </xsl:if>
                                                            </phase>
                                                        </effectiveTime>
                                                    </substanceAdministration>
                                                </outboundRelationship2>

                                                <xsl:if test="string-length(./diagnosisuse) > 0 ">
                                                    <xsl:comment>C5: DIAGNOSIS USE</xsl:comment>
                                                    <inboundRelationship>
                                                        <observation moodCode="EVN">
                                                            <code code="C41184" codeSystem="{$emdrOidObservationCode}" codeSystemName="Indication"/>
                                                            <value mediaType="text/plain" xsi:type="ED"><xsl:value-of select="./diagnosisuse"/></value>
                                                        </observation>
                                                    </inboundRelationship>
                                                </xsl:if>
                                            </substanceAdministration>
                                        </component>
                                    </organizer>
                                </subjectOf2>
                            </xsl:for-each>
                        </xsl:if>
                    </primaryRole>
                </subject1>
            </adverseEventAssessment>
        </component>
    </xsl:template>

    <xsl:template match="allmanufacturers" mode="part-w">
        <xsl:if test="count(reportsources) > 0">
            <xsl:for-each select="reportsources">
                <xsl:if test="string-length(./../reportsourceothertext) > 0 or string-length(./reportsource) > 0 ">
                    <reasonOf>
                        <detectedIssueEvent>
                            <code code="C53566" codeSystem="{$emdrOidObservationCode}" codeSystemName="Report_Source">
                                <xsl:if test="string-length(./../reportsourceothertext) > 0 and ./reportsource = 'C17649' ">
                                    <xsl:comment>G2: Report source text</xsl:comment>
                                    <originalText><xsl:value-of select="./../reportsourceothertext"/></originalText>
                                </xsl:if>
                            </code>
                            <xsl:if test="string-length(./reportsource) > 0 ">
                                <xsl:comment>G2: REPORT SOURCE</xsl:comment>
                                <value xsi:type="CE" code="{./reportsource}" codeSystem="{$emdrOidObservationCode}"/>
                            </xsl:if>
                        </detectedIssueEvent>
                    </reasonOf>
                </xsl:if>
            </xsl:for-each>
        </xsl:if>

        <xsl:if test="greporttype">
            <xsl:for-each select="greporttype">
                <xsl:if test="string-length(./gtypeofreport) > 0 ">
                    <xsl:comment>G6: GTYPEOFREPORT</xsl:comment>
                    <reasonOf>
                        <detectedIssueEvent>
                            <code code="C53571" codeSystem="{$emdrOidObservationCode}" codeSystemName="Type_of_Report"/>
                            <xsl:if test="string-length(../gfollowupnumber) > 0 and (./gtypeofreport = 'C53620' or ./gtypeofreport = 'C53578' or ./gtypeofreport = 'C53579')">
                                <xsl:comment>G6: GFOLLOWUPNUMBER</xsl:comment>
                                <text mediaType="text/plain"><xsl:value-of select="../gfollowupnumber"/></text>
                            </xsl:if>
                            <value xsi:type="CE" code="{./gtypeofreport}" codeSystem="{$emdrOidObservationCode}"/>
                        </detectedIssueEvent>
                    </reasonOf>
                </xsl:if>
            </xsl:for-each>
        </xsl:if>

        <xsl:if test="string-length(combinationproduct) > 0">
            <xsl:comment>G4: COMBINATION PRODUCT</xsl:comment>
            <reasonOf>
                <detectedIssueEvent>
                    <code code="C53571" codeSystem="{$emdrOidObservationCode}" codeSystemName="Combination_Product"/>
                    <value xsi:type="CE" code="{combinationproduct}" codeSystem="{$emdrOidObservationCode}"/>
                </detectedIssueEvent>
            </reasonOf>
        </xsl:if>

    </xsl:template>

    <xsl:template match="hdevicemfrinfo" mode="part-x">
        <xsl:if test="string-length(typeofreportableevent) > 0 ">
            <xsl:choose>
                <xsl:when test="typeofreportableevent = 'C84831' ">
                    <xsl:comment> H1: Summary Report </xsl:comment>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:comment> H1: Type of Reportable Event (Death, Malfunction, etc </xsl:comment>
                </xsl:otherwise>
            </xsl:choose>
            <reasonOf>
                <detectedIssueEvent>
                    <code code="C53570" codeSystem="{$emdrOidObservationCode}" codeSystemName="Type_of_Reportable_Event"/>
                    <value xsi:type="CE" code="{typeofreportableevent}" codeSystem="{$emdrOidObservationCode}">
                        <xsl:if test="typeofreportableevent = 'C28554' ">
                            <originalText/>
                        </xsl:if>
                    </value>
                </detectedIssueEvent>
            </reasonOf>
            <xsl:if test="typeofreportableevent = 'C84831' " >
                <xsl:comment> H1: No. of Events Summarized </xsl:comment>
                <reasonOf>
                    <detectedIssueEvent>
                        <code code="C53570" codeSystem="{$emdrOidObservationCode}" codeSystemName="Type_of_Reportable_Event"/>
                        <text mediaType="text/plain"><xsl:value-of select="eventsummarized"/></text>
                        <value xsi:type="CE" code="{typeofreportableevent}" codeSystem="{$emdrOidObservationCode}"/>
                    </detectedIssueEvent>
                </reasonOf>
            </xsl:if>
        </xsl:if>

        <xsl:if test="followuptype">
            <xsl:for-each select="followuptype">
                <xsl:if test="string-length(./hfollowuptype) > 0 ">
                    <reasonOf>
                        <detectedIssueEvent>
                            <code code="C53584" codeSystem="{$emdrOidObservationCode}" codeSystemName="Type_of_Follow-up"/>
                            <value xsi:type="CE" code="{./hfollowuptype}" codeSystem="{$emdrOidObservationCode}"/>
                        </detectedIssueEvent>
                    </reasonOf>
                </xsl:if>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>


    <!-- ============== FOR ================== EMDR ================================================-->

    <!-- CHecks if nullFlavor is there or not======returns true or false -->
    <xsl:template name="hasNullFlavour">
        <xsl:param name="value"/>
        <xsl:choose>
            <xsl:when test="string-length($value) > 0">
                <xsl:choose>
                    <xsl:when test="$value = 'NA' or $value = '[NA]'">yes</xsl:when>
                    <xsl:when test="$value = 'NI' or $value = '[NI]'">yes</xsl:when>
                    <xsl:when test="$value = 'NASK' or $value = '[NASK]'">yes</xsl:when>
                    <xsl:when test="$value = 'ASKU' or $value = '[ASKU]'">yes</xsl:when>
                    <xsl:when test="$value = 'MSK' or $value = '[MSK]'">yes</xsl:when>
                    <xsl:when test="$value = 'UNK' or $value = '[UNK]'">yes</xsl:when>
                    <xsl:when test="$value = 'NINF' or $value = '[NINF]'">yes</xsl:when>
                    <xsl:when test="$value = 'PINF' or $value = '[PINF]'">yes</xsl:when>
                    <xsl:otherwise>no</xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>no</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- fetches the value of NullFlavor -->
    <xsl:template name="getNFValue">
        <xsl:param name="nfvalue"/>
        <xsl:choose>
            <xsl:when test="string-length($nfvalue) > 0">
                <xsl:choose>
                    <xsl:when test="$nfvalue = 'NA' or $nfvalue = '[NA]'">NA</xsl:when>
                    <xsl:when test="$nfvalue = 'NI' or $nfvalue = '[NI]'">NI</xsl:when>
                    <xsl:when test="$nfvalue = 'NASK' or $nfvalue = '[NASK]'">NASK</xsl:when>
                    <xsl:when test="$nfvalue = 'ASKU' or $nfvalue = '[ASKU]'">ASKU</xsl:when>
                    <xsl:when test="$nfvalue = 'MSK' or $nfvalue = '[MSK]'">MSK</xsl:when>
                    <xsl:when test="$nfvalue = 'UNK' or $nfvalue = '[UNK]'">UNK</xsl:when>
                    <xsl:when test="$nfvalue = 'NINF' or $nfvalue = '[NINF]'">NINF</xsl:when>
                    <xsl:when test="$nfvalue = 'PINF' or $nfvalue = '[PINF]'">PINF</xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="@nfvalue"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@nfvalue"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- for element having only extension or nullFlavor attribute-->
    <xsl:template name="ElemWithExtensionAttrOrNullFlavor">
        <xsl:param name="element"/>
        <xsl:param name="value"/>
        <xsl:if test="string-length($value) > 0">
            <xsl:element name="{$element}">
                <xsl:variable name="isElemHasNullFlavour">
                    <xsl:call-template name="hasNullFlavour">
                        <xsl:with-param name="value" select="$value"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="$isElemHasNullFlavour = 'yes'">
                        <xsl:variable name="NullFlavourValue">
                            <xsl:call-template name="getNFValue">
                                <xsl:with-param name="nfvalue" select="$value"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:attribute name="nullFlavor">
                            <xsl:value-of select="$NullFlavourValue"/>
                        </xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="extension">
                            <xsl:value-of select="$value"/>
                        </xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <!-- for element having only value attr or nullFlavor-->
    <xsl:template name="ElemWithValueAttrOrNullFlavor">
        <xsl:param name="element"/>
        <xsl:param name="value"/>

        <xsl:if test="string-length($value) > 0">
            <xsl:element name="{$element}">
                <xsl:variable name="isElemHasNullFlavour">
                    <xsl:call-template name="hasNullFlavour">
                        <xsl:with-param name="value" select="$value"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="$isElemHasNullFlavour = 'yes'">
                        <xsl:variable name="NullFlavourValue">
                            <xsl:call-template name="getNFValue">
                                <xsl:with-param name="nfvalue" select="$value"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:attribute name="nullFlavor">
                            <xsl:value-of select="$NullFlavourValue"/>
                        </xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="value">
                            <xsl:value-of select="$value"/>
                        </xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template name="ElemWithTextFieldOrNullFlavor">
        <xsl:param name="element"/>
        <xsl:param name="value"/>
        <xsl:if test="string-length($value) > 0">
            <xsl:element name="{$element}">
                <xsl:variable name="isNullFlavourMask">
                    <xsl:call-template name="hasNullFlavour">
                        <xsl:with-param name="value" select="$value"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="$isNullFlavourMask = 'yes'">
                        <xsl:variable name="NullFlavourValue">
                            <xsl:call-template name="getNFValue">
                                <xsl:with-param name="nfvalue" select="$value"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:attribute name="nullFlavor">
                            <xsl:value-of select="$NullFlavourValue"/>
                        </xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$value"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template name="ElemWithCodeAttrOrNullFlavor">
        <xsl:param name="element"/>
        <xsl:param name="value"/>
        <xsl:if test="string-length($value) > 0">
            <xsl:element name="{$element}">
                <xsl:variable name="isElemHasNullFlavour">
                    <xsl:call-template name="hasNullFlavour">
                        <xsl:with-param name="value" select="$value"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="$isElemHasNullFlavour = 'yes'">
                        <xsl:variable name="NullFlavourValue">
                            <xsl:call-template name="getNFValue">
                                <xsl:with-param name="nfvalue" select="$value"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:attribute name="nullFlavor">
                            <xsl:value-of select="$NullFlavourValue"/>
                        </xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="code">
                            <xsl:value-of select="$value"/>
                        </xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>