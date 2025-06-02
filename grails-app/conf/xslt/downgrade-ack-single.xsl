<?xml version="1.0" encoding="UTF-8"?>
<!--
		Conversion Style-Sheet (Downgrade - ACK)
		Input : 			ICSR ACK File compliant with E2B(R3)
		Output : 		ICSR ACK File compliant with E2B(R2)

		Version:		0.9
		Date:			21/06/2011
		Status:		Step 2
		Author:		Laurent DESQUEPER (EU)

-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:hl7="urn:hl7-org:v3" xmlns:mif="urn:hl7-org:v3/mif"  exclude-result-prefixes="hl7 xsi xsl fo mif">

    <xsl:variable name="oidISOCountry">1.0.3166.1.2.2</xsl:variable>
    <xsl:variable name="oidGenderCode">1.0.5218</xsl:variable>
    <xsl:variable name="oidUCUM">2.16.840.1.113883.6.8</xsl:variable>
    <xsl:variable name="oidMedDRA">2.16.840.1.113883.6.163</xsl:variable>
    <xsl:variable name="oidSenderIdentifierValue">2.16.840.1.113883.3.989.2.1.1.1</xsl:variable>
    <xsl:variable name="oidObservationCode">2.16.840.1.113883.3.989.2.1.1.19</xsl:variable>
    <xsl:variable name="oidAttentionLineCode">2.16.840.1.113883.3.989.2.1.1.24</xsl:variable>
    <xsl:variable name="oidAssignedEntityRoleCode">2.16.840.1.113883.3.989.2.1.1.21</xsl:variable>
    <xsl:variable name="oidActionPerformedCode">2.16.840.1.113883.3.989.2.1.1.18</xsl:variable>
    <xsl:variable name="oidReportRelationCode">2.16.840.1.113883.3.989.2.1.1.22</xsl:variable>
    <xsl:variable name="oidReportCharacterizationCode">2.16.840.1.113883.3.989.2.1.1.23</xsl:variable>

    <!-- B.3 - Test -->
    <!-- MedDRA version for Tests change this value to the correct version of MedDRA being used-->
    <xsl:variable name="testResMedDRAver">20.0</xsl:variable>
    <!-- B.3 -->

    <!-- Use Recoded EudraVigilance product and substance names instead of verbatim term -->
    <!-- set value to 1 to use EV recoding-->
    <xsl:variable name="XEVMPD"></xsl:variable>
    <!-- Use N.2.r.2 to populate A.3.2.1 for NCAs to preserve original sending organisation -->
    <!-- set value to 1 to enable it-->
    <xsl:variable name="NCAREROUTE"></xsl:variable>
    <!-- EV product -->

    <!-- M.1 Fields - Batch Wrapper -->
    <!-- M.1.1 -->
    <xsl:variable name="oidMessageType">2.16.840.1.113883.3.989.2.1.1.1</xsl:variable>
    <xsl:variable name="oidMessageTypeCSV">2.0</xsl:variable>
    <!-- M.1.4 -->
    <xsl:variable name="oidBatchNumber">2.16.840.1.113883.3.989.2.1.3.22</xsl:variable>
    <!-- M.1.5 -->
    <xsl:variable name="oidBatchSenderId">2.16.840.1.113883.3.989.2.1.3.13</xsl:variable>
    <!-- M.1.6 -->
    <xsl:variable name="oidBatchReceiverId">2.16.840.1.113883.3.989.2.1.3.14</xsl:variable>
    <!-- M.2 Fields - Message Wrapper -->
    <!-- M.2.r.4 -->
    <xsl:variable name="oidMessageNumber">2.16.840.1.113883.3.989.2.1.3.1</xsl:variable>
    <!-- M.2.r.5 -->
    <xsl:variable name="oidMessageSenderId">2.16.840.1.113883.3.989.2.1.3.11</xsl:variable>
    <!-- M.2.r.6 -->
    <xsl:variable name="oidMessageReceiverId">2.16.840.1.113883.3.989.2.1.3.12</xsl:variable>
    <!-- A.1 Fields - Case Safety Report -->
    <!-- A.1.0.1 -->
    <xsl:variable name="oidSendersReportNamespace">2.16.840.1.113883.3.989.2.1.3.1</xsl:variable>
    <!-- A.1.4 -->
    <xsl:variable name="ReportType">1</xsl:variable>
    <xsl:variable name="oidReportType">2.16.840.1.113883.3.989.2.1.1.2</xsl:variable>
    <xsl:variable name="oidReportTypeCSV">2.0</xsl:variable>
    <!-- A.1.8 -->
    <xsl:variable name="AdditionalDocumentsAvailable">1</xsl:variable>
    <!-- A.1.9 -->
    <xsl:variable name="LocalCriteriaForExpedited">23</xsl:variable>
    <!-- A.1.10.1/12 -->
    <xsl:variable name="oidWorldWideCaseID">2.16.840.1.113883.3.989.2.1.3.2</xsl:variable>
    <!-- A.1.10.2 -->
    <xsl:variable name="InitialReport">1</xsl:variable>
    <xsl:variable name="oidFirstSender">2.16.840.1.113883.3.989.2.1.1.3</xsl:variable>
    <xsl:variable name="oidFirstSenderCSV">2.0</xsl:variable>
    <!-- A.1.11 -->
    <xsl:variable name="OtherCaseIDs">2</xsl:variable>
    <!-- A.1.11.r.2 -->
    <xsl:variable name="oidCaseIdentifier">2.16.840.1.113883.3.989.2.1.3.3</xsl:variable>
    <!-- A.1.13 -->
    <xsl:variable name="NullificationAmendmentCode">3</xsl:variable>
    <xsl:variable name="oidNullificationAmendment">2.16.840.1.113883.3.989.2.1.1.5</xsl:variable>
    <xsl:variable name="oidNullificationAmendmentCSV">2.0</xsl:variable>
    <!-- A.1.13.1 -->
    <xsl:variable name="NullificationAmendmentReason">4</xsl:variable>
    <!-- A.2 - Primary Source -->
    <!-- A.2 -->
    <xsl:variable name="SourceReport">2</xsl:variable>
    <!-- A.2.r.1.4 -->
    <xsl:variable name="oidQualification">2.16.840.1.113883.3.989.2.1.1.6</xsl:variable>
    <xsl:variable name="oidQualificationCSV">2.0</xsl:variable>
    <!-- A.3 - Sender -->
    <!-- A.3.1 -->
    <xsl:variable name="oidSenderType">2.16.840.1.113883.3.989.2.1.1.7</xsl:variable>
    <xsl:variable name="oidSenderTypeCSV">2.0</xsl:variable>
    <!-- A.5 - Study Identification -->
    <!-- A.5 -->
    <xsl:variable name="SponsorStudyNumber">2.16.840.1.113883.3.989.2.1.3.5</xsl:variable>
    <!-- A.5.1.r.1 -->
    <xsl:variable name="StudyRegistrationNumber">2.16.840.1.113883.3.989.2.1.3.6</xsl:variable>
    <!-- A.5.4 -->
    <xsl:variable name="oidStudyType">2.16.840.1.113883.3.989.2.1.1.8</xsl:variable>
    <xsl:variable name="oidStudyTypeCSV">2.0</xsl:variable>
    <!-- B.1 / B.1.10 - Patient / Parent -->
    <xsl:variable name="oidSourceMedicalRecord">2.16.840.1.113883.3.989.2.1.1.4</xsl:variable>
    <!-- B.1.1.1a -->
    <xsl:variable name="GPMrn">1</xsl:variable>
    <xsl:variable name="oidGPMedicalRecordNumber">2.16.840.1.113883.3.989.2.1.3.7</xsl:variable>
    <!-- B.1.1.1b -->
    <xsl:variable name="SpecialistMrn">2</xsl:variable>
    <xsl:variable name="oidSpecialistRecordNumber">2.16.840.1.113883.3.989.2.1.3.8</xsl:variable>
    <!-- B.1.1.1c -->
    <xsl:variable name="HospitalMrn">3</xsl:variable>
    <xsl:variable name="oidHospitalRecordNumber">2.16.840.1.113883.3.989.2.1.3.9</xsl:variable>
    <!-- B.1.1.1d -->
    <xsl:variable name="Investigation">4</xsl:variable>
    <xsl:variable name="oidInvestigationNumber">2.16.840.1.113883.3.989.2.1.3.10</xsl:variable>
    <!-- B.1.2.2 -->
    <xsl:variable name="Age">3</xsl:variable>
    <!-- B.1.2.2.1 -->
    <xsl:variable name="GestationPeriod">16</xsl:variable>
    <!-- B.1.2.3 -->
    <xsl:variable name="AgeGroup">4</xsl:variable>
    <!-- B.1.2.3 -->
    <xsl:variable name="oidAgeGroup">2.16.840.1.113883.3.989.2.1.1.9</xsl:variable>
    <xsl:variable name="oidAgeGroupCSV">2.0</xsl:variable>
    <!-- B.1.3 -->
    <xsl:variable name="BodyWeight">7</xsl:variable>
    <!-- B.1.4 -->
    <xsl:variable name="Height">17</xsl:variable>
    <!-- B.1.6 -->
    <xsl:variable name="LastMenstrualPeriodDate">22</xsl:variable>
    <!-- B.1.10 -->
    <xsl:variable name="Parent">PRN</xsl:variable>
    <!-- B.1.7 / B.1.10.7 - Medical History -->
    <!-- B.1.7 -->
    <xsl:variable name="RelevantMedicalHistoryAndConcurrentConditions">1</xsl:variable>
    <!-- B.1.7.1.r.d -->
    <xsl:variable name="Continuing">13</xsl:variable>
    <!-- B.1.7.1.r.g -->
    <xsl:variable name="Comment">10</xsl:variable>
    <!-- B.1.7.2 -->
    <xsl:variable name="HistoryAndConcurrentConditionText">18</xsl:variable>
    <!-- B.1.7.3 -->
    <xsl:variable name="ConcommitantTherapy">11</xsl:variable>
    <!-- B.1.8 / B.1.10.8 - Drug History -->
    <!-- B.1.8 -->
    <xsl:variable name="DrugHistory">2</xsl:variable>
    <!-- B.1.8.r.a1 -->
    <xsl:variable name="MPID">MPID</xsl:variable>
    <!-- B.1.8.r.a3 -->
    <xsl:variable name="PhPID">PhPID</xsl:variable>
    <!-- B.1.8.r.f.2 -->
    <xsl:variable name="Indication">19</xsl:variable>
    <!-- B.1.8.r.g.2 -->
    <xsl:variable name="Reaction">29</xsl:variable>
    <!-- B.1.9 -->
    <!-- B.1.9.2 -->
    <xsl:variable name="ReportedCauseOfDeath">32</xsl:variable>
    <!-- B.1.9.3 -->
    <xsl:variable name="Autopsy">5</xsl:variable>
    <!-- B.1.9.4 -->
    <xsl:variable name="CauseOfDeath">8</xsl:variable>
    <!-- B.2 - Reaction -->
    <!-- B.2.i -->
    <xsl:variable name="oidInternalReferencesToReaction">oidInternalReferencesToReaction</xsl:variable>
    <!-- B.2.i.0.b -->
    <xsl:variable name="ReactionForTranslation">30</xsl:variable>
    <!-- B.2.i.2.1 -->
    <xsl:variable name="TermHighlightedByReporter">37</xsl:variable>
    <xsl:variable name="oidTermHighlighted">2.16.840.1.113883.3.989.2.1.1.10</xsl:variable>
    <xsl:variable name="oidTermHighlightedCSV">2.0</xsl:variable>
    <!-- B.2.i.2.2 -->
    <xsl:variable name="ResultsInDeath">34</xsl:variable>
    <xsl:variable name="LifeThreatening">21</xsl:variable>
    <xsl:variable name="CausedProlongedHospitalisation">33</xsl:variable>
    <xsl:variable name="DisablingIncapaciting">35</xsl:variable>
    <xsl:variable name="CongenitalAnomalyBirthDefect">12</xsl:variable>
    <xsl:variable name="OtherMedicallyImportantCondition">26</xsl:variable>
    <!-- B.2.i.6 -->
    <xsl:variable name="Outcome">27</xsl:variable>
    <xsl:variable name="oidOutcome">2.16.840.1.113883.3.989.2.1.1.11</xsl:variable>
    <xsl:variable name="oidOutcomeCSV">2.0</xsl:variable>

    <xsl:variable name="TestsAndProceduresRelevantToTheInvestigation">3</xsl:variable>
    <!-- B.3.r.4 -->
    <xsl:variable name="MoreInformationAvailable">25</xsl:variable>
    <!-- B.4 - Drug -->
    <!-- B.4 -->
    <xsl:variable name="DrugInformation">4</xsl:variable>
    <xsl:variable name="oidValueGroupingCode">2.16.840.1.113883.3.989.2.1.1.20</xsl:variable>
    <!-- B.4.k.1 -->
    <xsl:variable name="InterventionCharacterization">20</xsl:variable>
    <xsl:variable name="oidDrugRole">2.16.840.1.113883.3.989.2.1.1.13</xsl:variable>
    <xsl:variable name="oidDrugRoleCSV">2.0</xsl:variable>
    <!-- B.4.k.2.4 -->
    <xsl:variable name="RetailSupply">1</xsl:variable>
    <!-- B.4.k.2.5 -->
    <xsl:variable name="Blinded">6</xsl:variable>
    <!-- B.4.k.3 -->
    <xsl:variable name="oidAuthorisationNumber">2.16.840.1.113883.3.989.2.1.3.4</xsl:variable>
    <!-- B.4.k.4.r.12/13-->
    <xsl:variable name="oidICHRoute">2.16.840.1.113883.3.989.2.1.1.14</xsl:variable>
    <xsl:variable name="oidICHRouteCSV">2.0</xsl:variable>
    <!-- B.4.k.4.r.13.2 -->
    <xsl:variable name="ParentRouteOfAdministration">28</xsl:variable>
    <!-- B.4.k.5.1 -->
    <xsl:variable name="CumulativeDoseToReaction">14</xsl:variable>
    <!-- B.4.k.7 -->
    <xsl:variable name="SourceReporter">3</xsl:variable>
    <!-- B.4.k.8 -->
    <xsl:variable name="oidActionTaken">2.16.840.1.113883.3.989.2.1.1.15</xsl:variable>
    <xsl:variable name="oidActionTakenCSV">2.0</xsl:variable>
    <!-- B.4.k.9.i.2 -->
    <xsl:variable name="Causality">39</xsl:variable>
    <!-- B.4.k.9.i.4 -->
    <xsl:variable name="RecurranceOfReaction">31</xsl:variable>
    <xsl:variable name="oidRechallenge">2.16.840.1.113883.3.989.2.1.1.16</xsl:variable>
    <xsl:variable name="oidRechallengeCSV">2.0</xsl:variable>
    <!-- B.4.k.10 -->
    <xsl:variable name="CodedDrugInformation">9</xsl:variable>
    <xsl:variable name="AdditionalInformationOnDrug">2.16.840.1.113883.3.989.2.1.1.17</xsl:variable>
    <xsl:variable name="AdditionalInformationOnDrugCSV">2.0</xsl:variable>
    <!--A.1.8.2 -->
    <xsl:variable name="documentsHeldBySender">1</xsl:variable>
    <xsl:variable name="literatureReference">2</xsl:variable>
    <xsl:variable name="oidichreferencesource">2.16.840.1.113883.3.989.2.1.1.27</xsl:variable>

    <!-- B.4.k.11 -->
    <xsl:variable name="AdditionalInformation">2</xsl:variable>
    <!-- B.5 - Summary -->
    <!-- B.5.3 -->
    <xsl:variable name="Diagnosis">15</xsl:variable>
    <xsl:variable name="Sender">1</xsl:variable>
    <!-- B.5.5 -->
    <xsl:variable name="SummaryAndComment">36</xsl:variable>
    <xsl:variable name="Reporter">2</xsl:variable>
    <!-- ACK -->
    <xsl:variable name="oidAckBatchNumber">2.16.840.1.113883.3.989.2.1.3.20</xsl:variable>
    <xsl:variable name="oidAckBatchReceiverID">2.16.840.1.113883.3.989.2.1.3.18</xsl:variable>
    <xsl:variable name="oidAckBatchSenderID">2.16.840.1.113883.3.989.2.1.3.17</xsl:variable>
    <xsl:variable name="AckLocalMessageNumber">2</xsl:variable>
    <xsl:variable name="oidAckLocalMessageNumber">2.16.840.1.113883.3.989.2.1.3.21</xsl:variable>
    <xsl:variable name="DateOfIcsrBatchTransmission">3</xsl:variable>
    <xsl:variable name="oidLocalReportNumber">2.16.840.1.113883.3.989.2.1.3.19</xsl:variable>
    <xsl:variable name="oidAckReceiverID">2.16.840.1.113883.3.989.2.1.3.16</xsl:variable>
    <xsl:variable name="oidAckSenderID">2.16.840.1.113883.3.989.2.1.3.15</xsl:variable>
    <xsl:variable name="oidDateOfCreation">2.16.840.1.113883.3.989.2.1.1.24</xsl:variable>
    <xsl:variable name="receiptDate">1</xsl:variable>
    <!--EU Specific Data fields -->
    <xsl:variable name="oidEUMethodofAssessment">2.16.840.1.113883.3.989.5.1.1.5.2</xsl:variable>
    <xsl:variable name="oidEUMethodofAssessmentCSV">1.0</xsl:variable>
    <xsl:variable name="oidEUResultofAssessment">2.16.840.1.113883.3.989.5.1.1.5.3</xsl:variable>
    <xsl:variable name="oidEUResultofAssessmentCSV">1.0</xsl:variable>
    <xsl:variable name="oidEUSourceofAssessment">2.16.840.1.113883.3.989.5.1.1.5.4</xsl:variable>
    <xsl:variable name="oidEUSourceofAssessmentCSV">1.0</xsl:variable>
    <xsl:variable name="oidEUMessageType">2.16.840.1.113883.3.989.5.1.1.5.1</xsl:variable>
    <xsl:variable name="oidEUMessageTypeCSV">1.0</xsl:variable>

    <!-- Other Variables used for Special Cases -->
    <xsl:variable name="Decade">800</xsl:variable>
    <xsl:variable name="Year">801</xsl:variable>
    <xsl:variable name="Month">802</xsl:variable>
    <xsl:variable name="Week">803</xsl:variable>
    <xsl:variable name="Day">804</xsl:variable>
    <xsl:variable name="Trimester">810</xsl:variable>

    <xsl:template name="getDateFormat">
        <xsl:param name="precision"/>

        <xsl:choose>
            <xsl:when test="$precision = 4">602</xsl:when> 	<!-- CCYY -->
            <xsl:when test="$precision = 6">610</xsl:when> 	<!-- CCYYMM -->
            <xsl:when test="$precision = 8">102</xsl:when> 	<!-- CCYYMMDD -->
            <xsl:when test="$precision = 10">202</xsl:when> 	<!-- CCYYMMDDHH -->
            <xsl:when test="$precision = 12">203</xsl:when> 	<!-- CCYYMMDDHHMM -->
            <xsl:when test="$precision = 14">204</xsl:when> 	<!-- CCYYMMDDHHMMSS -->
        </xsl:choose>
    </xsl:template>

    <!-- Date Conversion
        Input:	the element name of the date field
                    the date value
                    the minimum date precision expected
                    the maximum date precision expected
    -->
    <xsl:template name="convertDate">
        <xsl:param name="elementName"/>
        <xsl:param name="date-value"/>
        <xsl:param name="min-format"/>
        <xsl:param name="max-format"/>

        <xsl:variable name="before-dot">
            <xsl:choose>
                <xsl:when test="string-length(substring-before($date-value, '.')) > 0"><xsl:value-of select="substring-before($date-value, '.')"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$date-value"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="before-tz1">
            <xsl:choose>
                <xsl:when test="string-length(substring-before($date-value, '+')) > 0"><xsl:value-of select="substring-before($date-value, '+')"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$date-value"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="before-tz2">
            <xsl:choose>
                <xsl:when test="string-length(substring-before($date-value, '-')) > 0"><xsl:value-of select="substring-before($date-value, '-')"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$date-value"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="date">
            <xsl:choose>
                <xsl:when test="string-length($before-dot) &lt;= string-length($before-tz1) and string-length($before-dot) &lt;= string-length($before-tz2)"><xsl:value-of select="$before-dot"/></xsl:when>
                <xsl:when test="string-length($before-tz1) &lt;= string-length($before-dot) and string-length($before-tz1) &lt;= string-length($before-tz2)"><xsl:value-of select="$before-tz1"/></xsl:when>
                <xsl:when test="string-length($before-tz2) &lt;= string-length($before-dot) and string-length($before-tz2) &lt;= string-length($before-tz1)"><xsl:value-of select="$before-tz2"/></xsl:when>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="precision" select="string-length($date)"/>
        <xsl:variable name="min-precision" select="string-length($min-format)"/>
        <xsl:variable name="max-precision" select="string-length($max-format)"/>

        <xsl:variable name="elementFormatName">
            <xsl:choose>
                <xsl:when test="$elementName='patientlastmenstrualdate'">lastmenstrualdateformat</xsl:when>
                <xsl:otherwise><xsl:value-of select="concat($elementName, 'format')"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:choose>
            <!-- Same precision is accepted -->
            <xsl:when test="$precision >= $min-precision and $max-precision >= $precision">
                <xsl:element name="{$elementFormatName}">
                    <xsl:call-template name="getDateFormat">
                        <xsl:with-param name="precision" select="$precision"/>
                    </xsl:call-template>
                </xsl:element>
                <xsl:element name="{$elementName}">
                    <xsl:value-of select="$date"/>
                </xsl:element>
            </xsl:when>
            <!-- More precision than in R2 - Need to truncate -->
            <xsl:when test="$precision > $max-precision">
                <xsl:element name="{$elementFormatName}">
                    <xsl:call-template name="getDateFormat">
                        <xsl:with-param name="precision" select="$max-precision"/>
                    </xsl:call-template>
                </xsl:element>
                <xsl:element name="{$elementName}">
                    <xsl:value-of select="substring($date, 1, $max-precision)"/>
                </xsl:element>
            </xsl:when>
            <!-- Less precision than in R2 - Need to extend with default digits -->
            <xsl:when test="$min-precision > $precision">
                <xsl:element name="{$elementFormatName}">
                    <xsl:call-template name="getDateFormat">
                        <xsl:with-param name="precision" select="$min-precision"/>
                    </xsl:call-template>
                </xsl:element>
                <xsl:element name="{$elementName}">
                    <xsl:value-of select="$date"/>
                    <xsl:call-template name="extend-date">
                        <xsl:with-param name="beg" select="$precision + 1"/>
                        <xsl:with-param name="end" select="$min-precision"/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:when>
            <xsl:when test="$precision = 0">
                <xsl:element name="{$elementFormatName}"/>
                <xsl:element name="{$elementName}"/>
            </xsl:when>
            <xsl:otherwise>
                TODO : <xsl:value-of select="$precision"/> with <xsl:value-of select="$min-precision"/> - <xsl:value-of select="$max-precision"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Extend a date with default digits -->
    <xsl:template name="extend-date">
        <xsl:param name="beg"/>
        <xsl:param name="end"/>

        <xsl:choose>
            <xsl:when test="$beg = 1">0</xsl:when>		<!-- C -->
            <xsl:when test="$beg = 2">0</xsl:when>		<!-- C -->
            <xsl:when test="$beg = 3">0</xsl:when>		<!-- Y -->
            <xsl:when test="$beg = 4">0</xsl:when>		<!-- Y -->
            <xsl:when test="$beg = 5">0</xsl:when>		<!-- M -->
            <xsl:when test="$beg = 6">1</xsl:when>		<!-- M -->
            <xsl:when test="$beg = 7">0</xsl:when>		<!-- D -->
            <xsl:when test="$beg = 8">1</xsl:when>		<!-- D -->
            <xsl:when test="$beg = 9">0</xsl:when>		<!-- H -->
            <xsl:when test="$beg = 10">0</xsl:when>	<!-- H -->
            <xsl:when test="$beg = 11">0</xsl:when>	<!-- M -->
            <xsl:when test="$beg = 12">0</xsl:when>	<!-- M -->
            <xsl:when test="$beg = 13">0</xsl:when>	<!-- S -->
            <xsl:when test="$beg = 14">0</xsl:when>	<!-- S -->
        </xsl:choose>
        <xsl:if test="$end > $beg">
            <xsl:call-template name="extend-date">
                <xsl:with-param name="beg" select="$beg + 1"/>
                <xsl:with-param name="end" select="$end"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!--	Code mapping	-->
    <xsl:template name="getMapping">
        <xsl:param name="type"/>
        <xsl:param name="code"/>
        <xsl:choose>
            <xsl:when test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r3 = $code]) = 1">
                <xsl:value-of select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r3 = $code]/@r2"/>
            </xsl:when>
            <xsl:when test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r3 = $code]) > 1">
                <xsl:value-of select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r3 = $code]/@r2[1]"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="$type = 'ROUTE'">050</xsl:when>
                    <xsl:otherwise>
                        <!--<xsl:value-of select="$code" />--></xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <!--	Truncate -->
    <xsl:template name="truncate">
        <xsl:param name="string"/>
        <xsl:param name="string-length"/>
        <xsl:choose>
            <xsl:when test="string-length($string)>$string-length">
                <xsl:value-of select="substring($string, 1, $string-length - 3)"/>
                <xsl:text>...</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$string"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--	ID - TEXT mapping functions -->
    <xsl:template name="getText">
        <xsl:param name="id"/>
        <xsl:choose>
            <xsl:when test="count(document('mapping-codes.xml')/mapping-codes/mapping-id-text/text[./@id=$id]) = 1">
                <xsl:value-of select="document('mapping-codes.xml')/mapping-codes/mapping-id-text/text[./@id=$id]"/>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select="$id" /></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:output indent="yes" method="xml" omit-xml-declaration="no" encoding="utf-8"/>

    <!-- ICH ICSR ACK -->
    <xsl:template match="/">
        <ichicsrack lang="en">
            <xsl:apply-templates select="hl7:MCCI_IN200101UV01" mode="header"/>
            <xsl:apply-templates select="hl7:MCCI_IN200101UV01" mode="message"/>
        </ichicsrack>
    </xsl:template>

    <!-- M - ACK Message Header -->
    <xsl:template match="hl7:MCCI_IN200101UV01" mode="header">
        <ichicsrmessageheader>
            <!-- M.1.1 - Message Type -->
            <messagetype>ichicsrack</messagetype>
            <!-- M.1.2 - Message Format Version -->
            <messageformatversion>2.1</messageformatversion>
            <!-- M.1.3 - Message Release Version -->
            <messageformatrelease>1.0</messageformatrelease>
            <!-- M.1.4 - Message Number -->
            <messagenumb><xsl:value-of select="hl7:id/@extension"/></messagenumb>
            <!-- M.1.5 - Message Sender Identifier -->
            <messagesenderidentifier><xsl:value-of select="hl7:sender/hl7:device/hl7:id/@extension"/></messagesenderidentifier>
            <!-- M.1.6 - Message Receiver Identifier -->
            <messagereceiveridentifier><xsl:value-of select="hl7:receiver/hl7:device/hl7:id/@extension"/></messagereceiveridentifier>
            <!-- M.1.7 - Message Date -->
            <xsl:call-template name="convertDate">
                <xsl:with-param name="elementName">messagedate</xsl:with-param>
                <xsl:with-param name="date-value" select="hl7:creationTime/@value"/>
                <xsl:with-param name="min-format">CCYYMMDDHHMMSS</xsl:with-param>
                <xsl:with-param name="max-format">CCYYMMDDHHMMSS</xsl:with-param>
            </xsl:call-template>
        </ichicsrmessageheader>
    </xsl:template>

    <!-- A - Message Acknowledgment -->
    <xsl:template match="hl7:MCCI_IN200101UV01" mode="message">
        <acknowledgment>
            <messageacknowledgment>
                <!-- A.1.1 - ICSR Message Number -->
                <icsrmessagenumb><xsl:value-of select="hl7:acknowledgement/hl7:targetBatch/hl7:id/@extension"/></icsrmessagenumb>
                <!-- A.1.2 - Local Message Number -->
                <localmessagenumb><xsl:value-of select="hl7:attentionLine[hl7:keyWordText/@code = $AckLocalMessageNumber and hl7:keyWordText/@codeSystem=$oidAttentionLineCode]/hl7:value/@extension"/></localmessagenumb>
                <!-- A.1.3 - ICSR Message Sender ID -->
                <icsrmessagesenderidentifier><xsl:value-of select="hl7:MCCI_IN000002UV01[1]/hl7:sender/hl7:device/hl7:id/@extension"/></icsrmessagesenderidentifier>
                <!-- A.1.4 - ICSR Message Receiver ID -->
                <icsrmessagereceiveridentifier><xsl:value-of select="hl7:MCCI_IN000002UV01[1]/hl7:receiver/hl7:device/hl7:id/@extension"/></icsrmessagereceiveridentifier>
                <!-- A.1.5 - ICSR Message Date -->
                <xsl:call-template name="convertDate">
                    <xsl:with-param name="elementName">icsrmessagedate</xsl:with-param>
                    <xsl:with-param name="date-value" select="hl7:attentionLine[hl7:keyWordText/@code = $DateOfIcsrBatchTransmission and hl7:keyWordText/@codeSystem=$oidAttentionLineCode]/hl7:value/@value"/>
                    <xsl:with-param name="min-format">CCYYMMDDHHMMSS</xsl:with-param>
                    <xsl:with-param name="max-format">CCYYMMDDHHMMSS</xsl:with-param>
                </xsl:call-template>
                <!-- A.1.6 - Transmission ACK Code -->
                <xsl:variable name="ackCode" select="hl7:acknowledgement/@typeCode"/>
                <transmissionacknowledgmentcode>
                    <xsl:choose>
                        <xsl:when test="$ackCode = 'AA'">01</xsl:when>
                        <xsl:when test="$ackCode = 'AE'">02</xsl:when>
                        <xsl:otherwise>03</xsl:otherwise>
                    </xsl:choose>
                </transmissionacknowledgmentcode>
                <!-- A.1.7 - Parsing Error Message -->
                <parsingerrormessage><xsl:value-of select="hl7:acknowledgement/hl7:acknowledgementDetail/hl7:text"/></parsingerrormessage>
            </messageacknowledgment>
            <!-- B - Report Acknowledgment -->
            <xsl:apply-templates select="hl7:MCCI_IN000002UV01"/>
        </acknowledgment>
    </xsl:template>

    <!-- B - Report Acknowledgment -->
    <xsl:template match="hl7:MCCI_IN000002UV01">
        <reportacknowledgment>
            <!-- B.1.1 - Safety Report ID -->
            <safetyreportid><xsl:value-of select="hl7:acknowledgement/hl7:targetMessage/hl7:id/@extension"/></safetyreportid>
            <!-- B.1.3 - Local Report Number -->
            <localreportnumb><xsl:value-of select="hl7:id/@extension"/></localreportnumb>
            <!-- B.1.7 - Receipt Date -->
            <xsl:if test="string-length(hl7:attentionLine[hl7:keyWordText/@code = $receiptDate]/hl7:value/@value) > 0">
                <xsl:call-template name="convertDate">
                    <xsl:with-param name="elementName">receiptdate</xsl:with-param>
                    <xsl:with-param name="date-value" select="hl7:attentionLine[hl7:keyWordText/@code = $receiptDate]/hl7:value/@value"/>
                    <xsl:with-param name="min-format">CCYYMMDD</xsl:with-param>
                    <xsl:with-param name="max-format">CCYYMMDD</xsl:with-param>
                </xsl:call-template>
            </xsl:if>
            <!-- B.1.8 - ACK Code for Report -->
            <xsl:variable name="ackCode" select="hl7:acknowledgement/@typeCode"/>
            <reportacknowledgmentcode>
                <xsl:choose>
                    <xsl:when test="$ackCode = 'CA'">01</xsl:when>
                    <xsl:when test="$ackCode = 'CR'">02</xsl:when>
                </xsl:choose>
            </reportacknowledgmentcode>
            <!-- B.1.9 - Error Message or Comment -->
            <errormessagecomment><xsl:value-of select="hl7:acknowledgement/hl7:acknowledgementDetail/hl7:text"/></errormessagecomment>
        </reportacknowledgment>
    </xsl:template>
</xsl:stylesheet>