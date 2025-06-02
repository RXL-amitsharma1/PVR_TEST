<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="urn:hl7-org:v3" xmlns:mif="urn:hl7-org:v3/mif">
<!-- 	<xsl:include href="upgrade.xsl"/>
	<xsl:include href="upgrade-m.xsl"/>
	<xsl:include href="upgrade-a1.xsl"/> -->
	<xsl:output indent="yes" method="xml" omit-xml-declaration="no" encoding="utf-8"/>
	<xsl:strip-space elements="*"/>
	<!-- ICH ICSR : conversion of the main structure incl. root element and controlActProcess
	E2B(R2): root element "ichicsr"
	E2B(R3): root element "PORR_IN049016UV"
	-->
	<xsl:template match="/">
		<MCCI_IN200100UV01 ITSVersion="XML_1.0">
		<!-- edit schema location as needed-->
			<xsl:attribute name="xsi:schemaLocation">urn:hl7-org:v3 http://eudravigilance.ema.europa.eu/xsd/multicacheschemas/MCCI_IN200100UV01.xsd</xsl:attribute>
			<!-- M.x - Message Header -->
			<xsl:apply-templates select="/ichicsr/ichicsrtransmissionidentification" mode="part-a"/>
			<!-- Report -->
			<xsl:apply-templates select="/ichicsr/safetyreport" mode="report"/>
			<!-- M.x - Message Footer -->
			<xsl:apply-templates select="/ichicsr/ichicsrtransmissionidentification" mode="part-c"/>
		</MCCI_IN200100UV01>
	</xsl:template>
	<xsl:template match="safetyreport" mode="report">
		<PORR_IN049016UV>
			<!-- M.2.r.4 - Message Number-->
			
			
			<xsl:variable name="tempVariable">
			
			<xsl:value-of select="position()"/>
			
			</xsl:variable>
			
			<xsl:apply-templates mode="part-b" select="../ichicsrmessageheader[number($tempVariable)]"/>
			<controlActProcess classCode="CACT" moodCode="EVN">
				<code code="PORR_TE049016UV" codeSystem="2.16.840.1.113883.1.18"/>
				<!-- A.1.3 - Date of Transmission -->
				<effectiveTime value="{creationdate}"/>
				<xsl:comment>C.1.2:Date of Creation</xsl:comment>
				<xsl:apply-templates select="transmissiondate"/>
				<!-- A.1.x - Safety Report -->
				<xsl:apply-templates select="." mode="main"/>
			</controlActProcess>
		</PORR_IN049016UV>
	</xsl:template>

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
	<xsl:variable name="oidReportJapCharacterizationCode">2.16.840.1.113883.3.989.5.1.3.2.1.12</xsl:variable>
	<xsl:variable name="oidPMDAReportType">2.16.840.1.113883.3.989.5.1.3.2.1.1</xsl:variable>
	<!-- Convert an R2 code into the corresponding R3 code as specificed in the "mapping-codes.xml" file -->
	<xsl:template name="getMapping">
		<xsl:param name="type"/>
		<xsl:param name="code"/>
		
		
		<!-- check length if numeric and add leading zeros if missing  -->
			<xsl:choose>
				<xsl:when test="string-length($code) &lt; 3">
							<xsl:choose>
								<xsl:when test="string(number($code)) != 'NaN'">
										<xsl:if test="string-length($code) = 1">
											<xsl:choose>
												<xsl:when test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = concat('00',$code)]) = 1">
													<xsl:value-of select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = concat('00',$code)]/@r3"/>
												</xsl:when>
												<xsl:otherwise>{<xsl:value-of select="concat('00',$code)"/>}</xsl:otherwise>
												</xsl:choose>
										</xsl:if>
										<xsl:if test="string-length($code) = 2">
											<xsl:choose>
												<xsl:when test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = concat('0',$code)]) = 1">
													<xsl:value-of select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = concat('0',$code)]/@r3"/>
												</xsl:when>
												<xsl:otherwise>{<xsl:value-of select="$code"/>}</xsl:otherwise>
											</xsl:choose>
										</xsl:if>
										<xsl:if test="string-length($code) > 2">
											<xsl:choose>
												<xsl:when test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = $code]) = 1">
													<xsl:value-of select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = $code]/@r3"/>
												</xsl:when>
												<xsl:otherwise>{<xsl:value-of select="$code"/>}</xsl:otherwise>
											</xsl:choose>
										</xsl:if>
								</xsl:when>
								<xsl:otherwise>
									<xsl:choose>
										<xsl:when test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = $code]) = 1">
											<xsl:value-of select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = $code]/@r3"/>
										</xsl:when>
										<xsl:when test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[text()= $code]) = 1">
											<xsl:value-of select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[text()= $code]/@r3"/>
										</xsl:when>
										<xsl:when test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r3 = $code]) > 0">
											<xsl:value-of select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r3 = $code][1]/@r3"/>
										</xsl:when>
											<!--	<xsl:value-of select="$code"/> -->
										<xsl:otherwise>{<xsl:value-of select="translate($code, ' ', '-')"/>}</xsl:otherwise>
									</xsl:choose>
								</xsl:otherwise>
							</xsl:choose>
					</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = $code]) = 1">
							<xsl:value-of select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = $code]/@r3"/>
						</xsl:when>
						<xsl:when test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[text()= $code]) = 1">
							<xsl:value-of select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[text()= $code]/@r3"/>
						</xsl:when>
						<xsl:when test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r3 = $code]) > 0">
							<xsl:value-of select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r3 = $code][1]/@r3"/>
						</xsl:when>
							<!--	<xsl:value-of select="$code"/> -->
						<xsl:otherwise>{<xsl:value-of select="translate($code, ' ', '-')"/>}</xsl:otherwise>
				</xsl:choose>
				</xsl:otherwise>				
			</xsl:choose>
	</xsl:template>
	
	<!-- Return yes if N8, no otherwise -->
	<xsl:template name="isMeddraCode">
		<xsl:param name="code"/>
		<xsl:choose>
			<xsl:when test="string-length($code) = 8">
				<xsl:choose>
					<xsl:when test="number($code) = $code">
						<xsl:choose>
							<xsl:when test="floor($code) = $code">yes</xsl:when>
							<xsl:otherwise>no</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>no</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>no</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
<!-- Batch Header : M.1.1, M.1.4 and M.1.7 -->
	<xsl:template match="ichicsrtransmissionidentification" mode="part-a">
	<xsl:choose>
		<xsl:when test="string-length(messagenumb) > 0">
			<id extension="{messagenumb}" root="{$oidBatchNumber}"/>										<!-- M.1.4	- Batch Number-->
			<xsl:comment>N.1.2:Batch Number</xsl:comment>
			<creationTime value="{transmissiondate}"/>
			<xsl:comment>N.1.5:Date of Batch Transmission</xsl:comment>
		</xsl:when>		
		<xsl:otherwise>
				<id extension="NOTAVAILABLE" root="{$oidBatchNumber}"/>	
				<xsl:comment>N.1.2:Batch Number</xsl:comment>
			<creationTime value="20150101"/>	
		</xsl:otherwise>															<!-- M.1.7 - Date of Batch Transmission -->
	</xsl:choose>	
		<!-- Mandatory element -->
		<responseModeCode code="D"/>
		<interactionId root="2.16.840.1.113883.1.6" extension="MCCI_IN200100UV01"/>
		<!-- EU message types -->
		<xsl:choose>
			<xsl:when test="messagetype = 'ichicsr'">
				<name code="1" codeSystem="{$oidMessageType}" codeSystemVersion="{messagetypecsv}"/>					
			</xsl:when>
		<xsl:otherwise>
			<xsl:choose>
				<xsl:when test="messagetype = 'backlog'"><name code="1" codeSystem="{$oidEUMessageType}" codeSystemVersion="{$oidEUMessageTypeCSV}" displayName="backlog"/></xsl:when>
				<xsl:when test="messagetype = 'backlogct'"><name code="1" codeSystem="{$oidEUMessageType}" codeSystemVersion="{$oidEUMessageTypeCSV}" displayName="backlog"/></xsl:when>
				<xsl:when test="messagetype = 'psur'"><name code="1" codeSystem="{$oidMessageType}" codeSystemVersion="{$oidMessageTypeCSV}" displayName="ichicsr"/></xsl:when>
				<xsl:when test="messagetype = 'ctasr'"><name code="1" codeSystem="{$oidMessageType}" codeSystemVersion="{$oidMessageTypeCSV}" displayName="ichicsr"/></xsl:when>
				<xsl:when test="messagetype = 'masterichicsr'"><name code="2" codeSystem="{$oidEUMessageType}" codeSystemVersion="{$oidEUMessageTypeCSV}" displayName="masterichicsr"/></xsl:when>
			</xsl:choose>		
		</xsl:otherwise>
</xsl:choose>
<xsl:comment>N.1.1[Ver]:Types of Message in batch</xsl:comment>
<!-- M.1.1 - Message Type in Batch -->
	</xsl:template>
	<!-- Date of this transmission -->
	<xsl:template match="safetyreport" mode="header">
		
		
	</xsl:template>

	<!-- Message Header : M.2.r.5 and M.2.r.6 -->
	<xsl:template match="ichicsrmessageheader" mode="part-b">
		<!-- M.2.r.7 - Message Date -->
		<xsl:apply-templates select="../safetyreport[1]" mode="header"/>		
		<id extension="{messageidentifier}" root="{$oidMessageNumber}"/>
		<xsl:comment>N.2.r.1:Message Identifier</xsl:comment>
		<creationTime value="{messagedate}"/>
		<xsl:comment>N.2.r.4:Date of Message Creation</xsl:comment>
		
		<interactionId root="2.16.840.1.113883.1.6" extension="MCCI_IN200100UV01"/>
		<processingCode code="P"/>
		<processingModeCode code="T"/>
		<acceptAckCode code="AL"/>
		<receiver typeCode="RCV">
			<device classCode="DEV" determinerCode="INSTANCE">
			<xsl:choose>
					<xsl:when test="string-length(messagereceiveridentifier) > 0">
						<id extension="{messagereceiveridentifier}" root="{$oidMessageReceiverId}"/>
					</xsl:when>					
					<xsl:otherwise>
						<id extension="NOTAVAILABLE" root="{$oidMessageReceiverId}"/>
					</xsl:otherwise>
			</xsl:choose>
			<xsl:comment>N.2.r.3:Message Receiver Identifier</xsl:comment>
			</device>
		</receiver>
		<sender typeCode="SND">
			<device classCode="DEV" determinerCode="INSTANCE">
				<xsl:choose>
					<xsl:when test="string-length(messagesenderidentifier) > 0">
						<id extension="{messagesenderidentifier}" root="{$oidMessageSenderId}"/>
					</xsl:when>
					<xsl:otherwise>
						<id extension="NOTAVAILABLE" root="{$oidMessageSenderId}"/>
					</xsl:otherwise>
			</xsl:choose>
			<xsl:comment>N.2.r.2:Message Sender Identifier</xsl:comment>
			</device>
		</sender>
	</xsl:template>
	
	<!-- Batch Footer : M.1.5 and M.1.6 -->
	<xsl:template match="ichicsrtransmissionidentification" mode="part-c">
		<receiver typeCode="RCV">
			<device classCode="DEV" determinerCode="INSTANCE">
				<xsl:choose>
					<xsl:when test="string-length(messagereceiveridentifier) > 0">
						<id extension="{messagereceiveridentifier}" root="{$oidBatchReceiverId}"/>
					</xsl:when>
					<xsl:otherwise>
						<id extension="NOTAVAILABLE" root="{$oidBatchReceiverId}"/>
					</xsl:otherwise>
			</xsl:choose>
			</device>
		</receiver>
		<xsl:comment>N.1.4:Batch Receiver Identifier</xsl:comment>
		<sender typeCode="SND">
			<device classCode="DEV" determinerCode="INSTANCE">
				<xsl:choose>
					<xsl:when test="string-length(messagesenderidentifier) > 0">
						<id extension="{messagesenderidentifier}" root="{$oidBatchSenderId}"/>
					</xsl:when>
					<xsl:otherwise>
						<id extension="NOTAVAILABLE" root="{$oidBatchSenderId}"/>
					</xsl:otherwise>
			</xsl:choose>
			</device>
		</sender>
		<xsl:comment>N.1.3:Batch Sender Identifier</xsl:comment>
	</xsl:template>
<xsl:template match="transmissiondate">
		<xsl:variable name="version-number" select="../safetyreportversion"/>
		<xsl:choose>
			<xsl:when test="string-length($version-number) = 0"><effectiveTime value="{.}"/></xsl:when>
			<xsl:when test="string-length($version-number) = 1"><effectiveTime value="{.}00000{$version-number}"/></xsl:when>
			<xsl:when test="string-length($version-number) = 2"><effectiveTime value="{.}0000{$version-number}"/></xsl:when>
			<xsl:otherwise><effectiveTime value="{.}"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<!-- Safety Report (main) : 
	E2B(R2): element "safetyreport" inside "ichicsr"
	E2B(R3): element "investigationEvent"
	-->
	<xsl:template match="safetyreport" mode="main">
		<subject typeCode="SUBJ">
			<investigationEvent classCode="INVSTG" moodCode="EVN">
				<!-- A.1.0.1 - Senders (Case) Safety Report Unique Identifier  -->
				<id root="{$oidSendersReportNamespace}" extension="{safetyreportid}"/>
				<xsl:comment>C.1.1:Senders (case) Safety Report Unique Identifier</xsl:comment>
				<!-- A.1.10.1 - Worldwide Unique Case Identification Number - Rule STR-03  -->
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
				<!-- B.5.1 Case Narrative -->
				<xsl:apply-templates select="patient/summary/narrativeincludeclinical"/>
				<!-- J2・7・1 -->
				<statusCode code="{completeorincomplete}"/>
				<!-- A.1.6 - Date Report Was First Received from Source -->
				<xsl:if test="string-length(receivedate) > 0">
					<effectiveTime>
						<low value="{receivedate}"/>
						<xsl:comment>C.1.4:Date Report Was First Received from Source</xsl:comment>
					</effectiveTime>
				</xsl:if>
				<!-- A.1.7 - Date of Most Recent Information for this Case -->
				<xsl:if test="string-length(receiptdate) > 0">
					<availabilityTime value="{receiptdate}"/>
					<xsl:comment>C.1.5:Date of Most Recent Information for This Report</xsl:comment>
				</xsl:if>
				<!-- A.1.8.1.r Document Held by Sender -->
				<xsl:comment> C.1.6.1.r.1: Documents Held by Sender (repeat as necessary) </xsl:comment>
				<xsl:apply-templates select="additionaldocuments"/>
				
				<!-- A.4.r Literature References -->
				<xsl:comment> C.4.r.1: Literature Reference(s) </xsl:comment>
				<xsl:apply-templates select="literature"/>
				<!-- J2・15・r -->
				<xsl:apply-templates select="pmdapublishedcountry"/>
				<!-- B.1.x - Patient -->
				<xsl:comment> D.1: Patient (name or initials) </xsl:comment>
				<xsl:apply-templates select="patient" mode="identification"/>
				<xsl:comment> H.5.1a and H.5.1b Narrative and Sendercomment in Native Languague </xsl:comment>
				<xsl:apply-templates select="patient/summary/narrativesendercommentnative"/>
				<!-- A.1.8.1 - Are Additional Documents Available? -->
				<xsl:comment> C.1.6.1: Are Additional Documents Available? </xsl:comment>
				<component typeCode="COMP">
					<observationEvent classCode="OBS" moodCode="EVN">
						<code code="{$AdditionalDocumentsAvailable}" codeSystem="{$oidObservationCode}" displayName="additionalDocumentsAvailable"/>
						<value xsi:type="BL" value="true"/>
					</observationEvent>
				</component>
				<!--  J2・18・1  -->
				<xsl:apply-templates select="pmdareceiverinfo"/>
				
				<!--   J2・7・2  -->
				<xsl:if test="string-length(completeorincompletecomments) > 0">
				<component typeCode="COMP">
					<observationEvent classCode="OBS" moodCode="EVN">
						<code code="3" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.10"/>
						<value xsi:type="ED">{completeorincompletecomments}</value>
					</observationEvent>
				</component>
				</xsl:if>
				<!-- A.1.9 - Does this Case Fulfil the Local Criteria for an Expedited Report? -->
				<xsl:comment> C.1.7: Does This Case Fulfil the Local Criteria for an Expedited Report? </xsl:comment>
				<xsl:call-template name="fulfillexpeditecriteria"/>
				<!-- A.1.10.2 First Sender of this Case -->
				<xsl:comment> C.1.8.2 First Sender of this Case </xsl:comment>
				<xsl:if test="string-length(authoritynumb) + string-length(reportertel) > 0">
					<outboundRelationship typeCode="SPRT">
						<relatedInvestigation classCode="INVSTG" moodCode="EVN">
							<code code="{$InitialReport}" codeSystem="{$oidReportRelationCode}"/>
							<subjectOf2 typeCode="SUBJ">
								<controlActEvent classCode="CACT" moodCode="EVN">
									<author typeCode="AUT">
										<assignedEntity classCode="ASSIGNED">
											<xsl:choose>
												<xsl:when test="string-length(authoritynumb) > 0">
													<code code="1" codeSystem="{$oidFirstSender}" codeSystemVersion="{$oidFirstSenderCSV}" />
												</xsl:when>
												<xsl:otherwise>
													<code code="2" codeSystem="{$oidFirstSender}"  codeSystemVersion="{$oidFirstSenderCSV}"/>
												</xsl:otherwise>
											</xsl:choose>
										</assignedEntity>
									</author>
								</controlActEvent>
							</subjectOf2>
						</relatedInvestigation>
					</outboundRelationship>
				</xsl:if>
				<!--  J2・29・r -->
				<xsl:if test="string-length(jpncasesource) > 0">
					<outboundRelationship typeCode="SPRT">
						<relatedInvestigation classCode="INVSTG" moodCode="EVN">
							<code code="{$InitialReport}" codeSystem="{$oidReportRelationCode}"/>
								<subjectOf2 typeCode="SUBJ">
									<controlActEvent classCode="CACT" moodCode="EVN">
										<author typeCode="AUT">
										<assignedEntity classCode="ASSIGNED">
										<assignedPerson classCode="PSN" determinerCode="INSTANCE">
										<asIdentifiedEntity classCode="IDENT">
										<code code="{jpncasesource}" codeSystem="2.16.840.1.113883.3.989.5.1.3. 2.1.9" codeSystemVersion="{jpncasesourcecsv}" />
										</asIdentifiedEntity>
										</assignedPerson>
											
										</assignedEntity>
										</author>
									</controlActEvent>
								</subjectOf2>
						</relatedInvestigation>
					</outboundRelationship>
				</xsl:if>
				<!--  J2・27・r -->
				<xsl:if test="string-length(jpnreportdate) > 0">
					<outboundRelationship typeCode="SPRT">
						<relatedInvestigation classCode="INVSTG" moodCode="EVN">
							<code code="{$InitialReport}" codeSystem="{$oidReportRelationCode}"/>
								<subjectOf2 typeCode="SUBJ">
									<controlActEvent classCode="CACT" moodCode="EVN">
										<author typeCode="AUT">
											<assignedEntity classCode="ASSIGNED">
												<assignedPerson classCode="PSN" determinerCode="INSTANCE">
													<asIdentifiedEntity classCode="IDENT">
														<code code="{jpnreportdate}" codeSystem="2.16.840.1.113883.3.989.5.1.3. 2.1.15" codeSystemVersion="{jpnreportdatecsv}" />
													</asIdentifiedEntity>
												</assignedPerson>
															
											</assignedEntity>
										</author>
									</controlActEvent>
								</subjectOf2>
						</relatedInvestigation>
					</outboundRelationship>
				</xsl:if>
				
				
				
				
				<!-- A.1.12.r Linked Reports -->
				<xsl:comment> C.1.10.r: Identification Number of the Report Which Is Linked to This Report (repeat as necessary </xsl:comment>
				<xsl:apply-templates select="linkreport"/>
				<!-- A.2.r Primary Sources -->
				<xsl:comment> C.2.r Primary Sources </xsl:comment>
				<xsl:apply-templates select="primarysource"/>
				<!-- A.3 Sender -->
				<xsl:comment> C.3 Sender </xsl:comment>
				<xsl:apply-templates select="sender"/>
				<!-- A.1.11 Report Duplicate -->
				<xsl:apply-templates select="reportduplicate"/>
				<!-- A.1.4 - Type of Report -->
				<xsl:comment> C.1.3 Type of Report </xsl:comment>
				<xsl:if test="string-length(reporttype) > 0">
					<subjectOf2 typeCode="SUBJ">
						<investigationCharacteristic classCode="OBS" moodCode="EVN">
							<code code="{$ReportType}" codeSystem="{$oidReportCharacterizationCode}" />
							<value xsi:type="CE" code="{reporttype}" codeSystem="{$oidReportType}" codeSystemVersion="{reporttypecsv}"/>
						</investigationCharacteristic>
					</subjectOf2>
				</xsl:if>
				<!--  J2・1a  -->
				<xsl:if test="string-length(pmdareporttype) > 0">
					<subjectOf2 typeCode="SUBJ">
						<investigationCharacteristic classCode="OBS" moodCode="EVN">
							 <code code="{$ReportType}" codeSystem="{$oidReportJapCharacterizationCode}" />
							 <value xsi:type="CE" code="{pmdareporttype}" codeSystem="{$oidPMDAReportType}" codeSystemVersion="{pmdareporttypecsv}"/>
						</investigationCharacteristic>
					</subjectOf2>
				</xsl:if>
				<!--  J2・2・1  -->
				<xsl:if test="string-length(regulatoryclockstartdate) > 0">
					<subjectOf2 typeCode="SUBJ">
						<investigationCharacteristic classCode="OBS" moodCode="EVN">
							 <code code="2" codeSystem="{$oidReportJapCharacterizationCode}" />
							 <value xsi:type="TS" value="{regulatoryclockstartdate}"/>
						</investigationCharacteristic>
					</subjectOf2>
				</xsl:if>
				<!--  J2・2・1  -->
				<xsl:if test="string-length(regulatoryclockstartdate) > 0">
					<subjectOf2 typeCode="SUBJ">
						<investigationCharacteristic classCode="OBS" moodCode="EVN">
							 <code code="2" codeSystem="{$oidReportJapCharacterizationCode}" />
							 <value xsi:type="TS" value="{regulatoryclockstartdate}"/>
						</investigationCharacteristic>
					</subjectOf2>
				</xsl:if>
				<!--  J2・2・2  -->
				<xsl:if test="string-length(regulatoryclockstartdate) > 0">
					<subjectOf2 typeCode="SUBJ">
						<investigationCharacteristic classCode="OBS" moodCode="EVN">
							 <code code="3" codeSystem="{$oidReportJapCharacterizationCode}" />
							 <value xsi:type="ED">{regulatoryclockstartdatecomments}</value>
						</investigationCharacteristic>
					</subjectOf2>
				</xsl:if>
				<!--  J2・3  -->
				<xsl:if test="string-length(immediatereportflag) > 0">
					<subjectOf2 typeCode="SUBJ">
						<investigationCharacteristic classCode="OBS" moodCode="EVN">
							 <code code="3" codeSystem="{$oidReportJapCharacterizationCode}" />
							 <value xsi:type="CE" code="{immediatereportflag}"  codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.2" codeSystemVersion="{immediatereportflagcsv}"/>
						</investigationCharacteristic>
					</subjectOf2>
				</xsl:if>
				<!--  J2・8・1  -->
				<xsl:if test="string-length(cancellation) > 0">
					<subject typeCode="SUBJ">
						<investigationCharacteristic classCode="OBS" moodCode="EVN">
							<code code="5" codeSystem="{$oidReportJapCharacterizationCode}" />
							<value xsi:type="CE" code="{cancellation}"  codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.6" codeSystemVersion="{cancellationcsv}"/>
						</investigationCharacteristic>
					</subject>
				</xsl:if>
				<!--  J2・8・2  -->
				<xsl:if test="string-length(cancellation) > 0">
					<subject typeCode="SUBJ">
						<investigationCharacteristic classCode="OBS" moodCode="EVN">
							<code code="6" codeSystem="{$oidReportJapCharacterizationCode}" />
							<value xsi:type="ED">{cancellationreason}</value>
						</investigationCharacteristic>
					</subject>
				</xsl:if>
				<!-- A.1.11 - Other Case Identifiers in Previous Transmissions -->
				<xsl:comment>C.1.9.1:Other Case Identifiers in Previous Transmissions</xsl:comment>
				<subjectOf2 typeCode="SUBJ">
					<investigationCharacteristic classCode="OBS" moodCode="EVN">
						<code code="{$OtherCaseIDs}" codeSystem="{$oidReportCharacterizationCode}"/>
						<xsl:choose>
							<xsl:when test="duplicate = 1">
								<value xsi:type="BL" value="true"/>
							</xsl:when>
							<xsl:otherwise>
								<value xsi:type="BL" nullFlavor="NI"/>
							</xsl:otherwise>
						</xsl:choose>
					</investigationCharacteristic>
				</subjectOf2>
				<!-- A.1.13 Report Nullification / Amendment -->
				<xsl:if test="casenullificationoramendment = 1">
					<subjectOf2 typeCode="SUBJ">
						<investigationCharacteristic classCode="OBS" moodCode="EVN">
							<code code="{$NullificationAmendmentCode}" codeSystem="{$oidReportCharacterizationCode}"/>
							<value xsi:type="CE" code="{casenullificationoramendment}" codeSystem="{$oidNullificationAmendment}" codeSystemVersion="{casenullificationoramendmentcsv}"/>
							<xsl:comment>C.1.11.1:Report Nullification / Amendment</xsl:comment>
						</investigationCharacteristic>
					</subjectOf2>
				</xsl:if>
				<!-- A.1.13.1 Reason for Nullification / Amendment -->
				<xsl:comment>C.1.11.2:Reason for Nullification Amendment</xsl:comment>
				<xsl:if test="nullificationoramendmentreason">
					<subjectOf2 typeCode="SUBJ">
						<investigationCharacteristic classCode="OBS" moodCode="EVN">
							<code code="{casenullificationoramendment}" codeSystem="{$oidReportCharacterizationCode}"/>
							<value xsi:type="CE">
								<originalText mediaType="text/plain"><xsl:value-of select="nullificationoramendmentreason"/></originalText>
							</value>
						</investigationCharacteristic>
					</subjectOf2>
				</xsl:if>
						
			</investigationEvent>
		</subject>
	</xsl:template>
	
	<!-- Narrative Include Clinical : 
		E2B(R2): element "narrativeincludeclinical"
		E2B(R3): element "investigationEvent"
		-->
		
		<xsl:template match="narrativeincludeclinical">
		<xsl:comment>H.1: Case Narrative Including Clinical Course, Therapeutic Measures, Outcome and Additional Relevant Information</xsl:comment>
			<xsl:if test="string-length(.) > 0">
				<text mediaType="text/plain">
					<xsl:value-of select="."/>
				</text>				
			</xsl:if>	
	</xsl:template>
	
	<!-- Document List : 
	E2B(R2): element "documentlist" inside "safetyreport"
	E2B(R3): element "reference"
	-->
	<xsl:template match="additionaldocuments">
		<!-- A.1.8.1.r.1 - Documents Held by Sender -->
		<xsl:if test="string-length(documentlist) > 0">
			<reference typeCode="REFR">
				<document classCode="DOC" moodCode="EVN">
				<code codeSystem="{$oidichreferencesource}" code="{$documentsHeldBySender}" displayName="documentsHeldBySender"/>
				<title>
				<xsl:value-of select="documentlist"/>
				</title>
				<xsl:comment>C.1.6.1.r.1:Documents Held by Sender</xsl:comment>
				<xsl:variable name="MediaType">
				<xsl:value-of select="substring-after(documentlist,'.')"/>
				</xsl:variable>
				
				
				<xsl:if test="$MediaType='txt'">
				<text mediaType="text/plain">
				<xsl:value-of select="includedocuments"/>
				</text>
				<xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
				</xsl:if>
				<xsl:if test="$MediaType='pdf'">
				<text mediaType="application/pdf" representation="B64">
				<xsl:value-of select="includedocuments"/>
				</text>
				<xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
				</xsl:if>
				<xsl:if test="$MediaType='png'">
				<text mediaType="image/png" representation="B64">
				<xsl:value-of select="includedocuments"/>
				</text>
				<xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
				</xsl:if>
				<xsl:if test="$MediaType='jpeg'">
				<text mediaType="image/jpeg" representation="B64">
				<xsl:value-of select="includedocuments"/>
				</text>
				<xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
				</xsl:if>
				<xsl:if test="$MediaType='html'">
				<text mediaType="text/html" representation="B64">
				<xsl:value-of select="includedocuments"/>
				</text>
				</xsl:if>
				<xsl:if test="$MediaType='PSD'">
								<text mediaType="application/octet-stream" representation="B64">
								<xsl:value-of select="includedocuments"/>
				</text>
				</xsl:if>
				<xsl:if test="$MediaType='TIF'">
								<text mediaType="image/tiff" representation="B64">
								<xsl:value-of select="includedocuments"/>
				</text>
				</xsl:if>
				<xsl:if test="$MediaType='DOCX'">
								<text mediaType="application/vnd.openxmlformats-officedocument.wordprocessingml.document" representation="B64">
								<xsl:value-of select="includedocuments"/>
				</text>
				</xsl:if>
				<xsl:if test="$MediaType='XLS'">
												<text mediaType="application/vnd.ms-excel" representation="B64">
												<xsl:value-of select="includedocuments"/>
				</text>
				</xsl:if>
				<xsl:if test="$MediaType='XLSX'">
												<text mediaType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" representation="B64">
												<xsl:value-of select="includedocuments"/>
				</text>
				</xsl:if>
				<xsl:if test="$MediaType='VSD'">
												<text mediaType="application/x-visio" representation="B64">
												<xsl:value-of select="includedocuments"/>
				</text>
				</xsl:if>
				<xsl:if test="$MediaType='RTF'">
												<text mediaType="application/rtf" representation="B64">
												<xsl:value-of select="includedocuments"/>
				</text>
				</xsl:if>
				<xsl:if test="$MediaType='DOC'">
												<text mediaType="application/msword" representation="B64">
												<xsl:value-of select="includedocuments"/>
				</text>
				</xsl:if>
				<xsl:if test="$MediaType='PS'">
								<text mediaType="application/postscript" representation="B64">
								<xsl:value-of select="includedocuments"/>
				</text>
				</xsl:if>
				<xsl:if test="$MediaType='MDB'">
								<text mediaType="application/x-msaccess" representation="B64">
								<xsl:value-of select="includedocuments"/>
				</text>
				</xsl:if>
				<xsl:if test="$MediaType='BMP'">
								<text mediaType="image/bmp" representation="B64">
								<xsl:value-of select="includedocuments"/>
				</text>
				</xsl:if>
				<xsl:if test="$MediaType='XML'">
								<text mediaType="text/xml" representation="B64">
								<xsl:value-of select="includedocuments"/>
				</text>
				</xsl:if>
				<xsl:if test="$MediaType='SGM'">
								<text mediaType="text/sgml" representation="B64">
								<xsl:value-of select="includedocuments"/>
				</text>
				</xsl:if>
				<xsl:if test="$MediaType='MSG'">
								<text mediaType="application/vnd.ms-outlook" representation="B64">
								<xsl:value-of select="includedocuments"/>
				</text>
				
				<xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
				
				</xsl:if>
				</document>
			</reference>
		</xsl:if>
	</xsl:template>
	
	<!-- Fulfil Expedited Criteria: 
	E2B(R2): element "documentlist" inside "safetyreport"
	E2B(R3): element "component"
	-->
	<xsl:template name="fulfillexpeditecriteria">
		<!-- A.1.9 - Does this Case Fulfil the Local Criteria for an Expedited Report? -->
		<component typeCode="COMP">
			<observationEvent classCode="OBS" moodCode="EVN">
				<code code="{$LocalCriteriaForExpedited}" codeSystem="{$oidObservationCode}"/>
				<xsl:choose>
					<xsl:when test="fulfillexpeditecriteria = 1">
						<value xsi:type="BL" value="true"/>
					</xsl:when>
					<xsl:when test="fulfillexpeditecriteria = 2">
						<value xsi:type="BL" value="false"/>
					</xsl:when>
					<xsl:otherwise>
						<value xsi:type="BL" nullFlavor="NI"/>
					</xsl:otherwise>
				</xsl:choose>
			</observationEvent>
		</component>
	</xsl:template>
	
	<!-- Linked Report: 
	E2B(R2): element "linkedreport" inside "safetyreport"
	E2B(R3): element "relatedInvestigation"
	-->
	<xsl:template match="linkreport">
		<!-- A.1.12.r Identification Number of the Report Which Is Linked to this Report -->
		<xsl:if test="string-length(linkreportnumber)>0">
			<outboundRelationship typeCode="SPRT">
				<relatedInvestigation classCode="INVSTG" moodCode="EVN">
					<code nullFlavor="NA"/>
					<subjectOf2 typeCode="SUBJ">
						<controlActEvent classCode="CACT" moodCode="EVN">
							<id extension="{linkreportnumber}" root="{$oidWorldWideCaseID}"/>
							<xsl:comment>C.1.10.r:Identification Number of the Report Which Is Linked to This Report</xsl:comment>
						</controlActEvent>
					</subjectOf2>
				</relatedInvestigation>
			</outboundRelationship>
		</xsl:if>
	</xsl:template>
	
	<!-- Report Duplicate : 
	E2B(R2): element "reportduplicate" inside "safetyreport"
	E2B(R3): element "controlActEvent"
	-->
	
	<xsl:template match="reportduplicate">
	<xsl:comment> C.1.9.1.r.1: Source(s) of the Case Identifier (repeat as necessary) </xsl:comment>
	<xsl:comment> C.1.9.1.r.2 Case Identifier(s) </xsl:comment>
		<xsl:if test="string-length(duplicatesource)>0 or string-length(duplicatenumb)>0">
			<subjectOf1 typeCode="SUBJ">
				<controlActEvent classCode="CACT" moodCode="EVN">
					<!-- A.1.11.r.1 Source(s) of the Case Identifier -->
					<!-- A.1.11.r.2 Case Identifier(s) -->
					<xsl:choose>
						<xsl:when test="string-length(duplicatesource) = 0"><id assigningAuthorityName="-" extension="{duplicatenumb}" root="{$oidCaseIdentifier}"/></xsl:when>
						<xsl:when test="string-length(duplicatenumb) = 0"><id assigningAuthorityName="{duplicatesource}" extension="-" root="{$oidCaseIdentifier}"/></xsl:when>
						<xsl:otherwise><id assigningAuthorityName="{duplicatesource}" extension="{duplicatenumb}" root="{$oidCaseIdentifier}"/></xsl:otherwise>
					</xsl:choose>
					
					</controlActEvent>
			</subjectOf1>
		</xsl:if>
		
	</xsl:template>
	<xsl:comment> H.5.1a and H.5.1b Narrative and Sendercomment in Native Languague </xsl:comment>
	<xsl:template match="patient/summary/narrativesendercommentnative">
	<xsl:for-each select="Nativedata">
				<xsl:if test="string-length(summaryandreportercomments) > 0">
									<component typeCode="COMP">
										<observationEvent moodCode="EVN" classCode="OBS">
											<code code="{$SummaryAndComment}" codeSystem="{$oidObservationCode}"/>
											<value language="{summaryandreportercommentslang}" xsi:type="ED" mediaType="text/plain"><xsl:value-of select="summaryandreportercomments"/></value>
											<author typeCode="AUT">
												<assignedEntity classCode="ASSIGNED">
													<code code="{$Reporter}" codeSystem="{$oidAssignedEntityRoleCode}"/>
												</assignedEntity>
											</author>
										</observationEvent>
									</component>
				</xsl:if>
				</xsl:for-each>
		</xsl:template>
		<xsl:template match="icsrsource">
					<outboundRelationship typeCode="SPRT">
						<relatedInvestigation classCode="INVSTG" moodCode="EVN">
							<code code="{$InitialReport}" codeSystem="{$oidReportRelationCode}"/>
							<subjectOf2 typeCode="SUBJ">
								<controlActEvent classCode="CACT" moodCode="EVN">
									<author typeCode="AUT">
										<assignedEntity classCode="ASSIGNED">
											<code code="{icsrsource}" codeSystem="{$oidAssignedEntityRoleCode}" codeSystemVersion ="{icsrsourcecsv}"/>
										<xsl:comment>C.1.8.2: First Sender of This Case </xsl:comment>
										</assignedEntity>
									</author>
								</controlActEvent>
							</subjectOf2>
						</relatedInvestigation>
					</outboundRelationship>
				</xsl:template>
<xsl:template match="primarysource">
		<outboundRelationship typeCode="SPRT">
			<!-- A.2.r.1.5 Primary Source for Regulatory Purposes -->
			<xsl:if test="position() = 1"><priorityNumber value="{casefirstsource}"/></xsl:if>
			<xsl:comment>C.2.r.5:Primary Source for Regulatory Purposes</xsl:comment>
			<relatedInvestigation classCode="INVSTG" moodCode="EVN">
				<code code="{$SourceReport}" codeSystem="{$oidReportRelationCode}"/>
				<subjectOf2 typeCode="SUBJ">
					<controlActEvent classCode="CACT" moodCode="EVN">
						<author typeCode="AUT">
							<assignedEntity classCode="ASSIGNED">
								<!-- A.2.r.1.2.cdef Reporter Address -->
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
								<!--  Reporter Telephone -->
								<xsl:if test="string-length(reportertel) > 0">
									<telecom>
										<xsl:attribute name="value">
											<xsl:text>tel: </xsl:text>
											<xsl:if test="string-length(reportertelcountrycode) > 0">+<xsl:value-of select="reportertelcountrycode"/><xsl:text> </xsl:text></xsl:if>
											<xsl:value-of select="reportertel"/>
											<xsl:if test="string-length(reportertelextension) > 0"><xsl:text> </xsl:text><xsl:value-of select="reportertelextension"/></xsl:if>
										</xsl:attribute>
										<xsl:comment>C.2.r.2.7:Reporter’s Telephone</xsl:comment>
									</telecom>
						</xsl:if>
								<assignedPerson classCode="PSN" determinerCode="INSTANCE">
									<!-- A.2.r.1.1 Reporter Identifier -->
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
									<!-- A.2.r.1.4 Reporter Qualification -->
									<asQualifiedEntity classCode="QUAL">
										<xsl:choose>
										<xsl:when test="string-length(qualification) > 0"><code code="{qualification}" codeSystem="{$oidQualification}" codeSystemVersion="{qualificationcsv}" /></xsl:when>
										<xsl:otherwise><code nullFlavor="UNK"/></xsl:otherwise>
										</xsl:choose>
										<xsl:comment>C.2.r.4:Qualification</xsl:comment>
									</asQualifiedEntity>
									<!-- A.2.r.1.3 Reporter Country -->
									<xsl:if test="string-length(reportercountry) > 0">
										<asLocatedEntity classCode="LOCE">
											<location determinerCode="INSTANCE" classCode="COUNTRY">
												<code code="{reportercountry}" codeSystem="{$oidISOCountry}"/>
												<xsl:comment>C.2.r.3:Reporter’s Country Code</xsl:comment>
											</location>											
										</asLocatedEntity>
									</xsl:if>
									<xsl:if test="position() = 1 and string-length(reportercountry) = 0">
										<xsl:variable name="primreportercountry" select="../primarysourcecountry"/>
										<xsl:if test="string-length($primreportercountry) > 0">
											<asLocatedEntity classCode="LOCE">
												<location determinerCode="INSTANCE" classCode="COUNTRY">
													<code code="{$primreportercountry}" codeSystem="{$oidISOCountry}"/>
												</location>
											</asLocatedEntity>
										</xsl:if>
									</xsl:if>
								</assignedPerson>
								<!-- A.2.r.1.2.ab Reporter Organization -->
								<xsl:if test="string-length(reporteroraginisation) + string-length(reporterdepartment) > 0">
									<representedOrganization classCode="ORG" determinerCode="INSTANCE">
										<xsl:call-template name="field-or-mask">
											<xsl:with-param name="element">name</xsl:with-param>
											<xsl:with-param name="value" select="reporterdepartment"/>
										</xsl:call-template>
										<xsl:comment>C.2.r.2.2:Reporter’s Department</xsl:comment>
										<xsl:if test="string-length(reporteroraginisation) > 0">
											<assignedEntity classCode="ASSIGNED">
												<representedOrganization classCode="ORG" determinerCode="INSTANCE">
													<xsl:call-template name="field-or-mask">
														<xsl:with-param name="element">name</xsl:with-param>
														<xsl:with-param name="value" select="reporteroraginisation"/>
													</xsl:call-template>
													<xsl:comment>C.2.r.2.1:Reporter’s Organisation</xsl:comment>
												</representedOrganization>
											</assignedEntity>
										</xsl:if>
									</representedOrganization>
								</xsl:if>
							</assignedEntity>
						</author>
					</controlActEvent>
				</subjectOf2>
			</relatedInvestigation>
		</outboundRelationship>
	</xsl:template>

	<!-- display content of a field, unless it is masked -->
	<xsl:template name="field-or-mask">
		<xsl:param name="element"/>
		<xsl:param name="value"/>
		
		<xsl:if test="string-length($value) > 0">
			<xsl:element name="{$element}">
				<xsl:choose>
					<xsl:when test="$value = 'PRIVACY'"><xsl:attribute name="nullFlavor">MSK</xsl:attribute></xsl:when>
					<xsl:when test="$value = 'UNKNOWN'"><xsl:attribute name="nullFlavor">UNK</xsl:attribute></xsl:when>
					<xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise>
				</xsl:choose>
			</xsl:element>
		</xsl:if>
	</xsl:template>
<xsl:template match="sender">
		<subjectOf1 typeCode="SUBJ">
			<controlActEvent classCode="CACT" moodCode="EVN">
				<author typeCode="AUT">
					<assignedEntity classCode="ASSIGNED">
						<!-- A.3.1	Sender Organization Type -->
						<xsl:choose>
							<xsl:when test="string-length(sendertype) = 0"><code code="6" codeSystem="{oidSenderType}" codeSystemVersion="{sendertypecsv}"/></xsl:when>
							<xsl:otherwise><code code="{sendertype}" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}"/></xsl:otherwise>
						</xsl:choose>
						<xsl:comment>C.3.1:Sender Type</xsl:comment>
						<!-- A.3.4.abcd Sender Address -->
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
						<!-- A.3.4.fgh Sender Telephone -->
						<xsl:if test="string-length(sendertel) > 0">
							<telecom>
								<xsl:attribute name="value">
									<xsl:text>tel: </xsl:text>
									<xsl:if test="string-length(sendertelcountrycode) > 0">+<xsl:value-of select="sendertelcountrycode"/><xsl:text> </xsl:text></xsl:if>
									<xsl:value-of select="sendertel"/>
									<xsl:if test="string-length(sendertelextension) > 0"><xsl:text> </xsl:text><xsl:value-of select="sendertelextension"/></xsl:if>
								</xsl:attribute>
								<xsl:comment>C.3.4.6:Sender’s Telephone</xsl:comment>
							</telecom>
						</xsl:if>
						<!-- A.3.4.ijk Sender Fax -->
						<xsl:if test="string-length(senderfax) > 0">
							<telecom>
								<xsl:attribute name="value">
									<xsl:text>fax: </xsl:text>
									<xsl:if test="string-length(senderfaxcountrycode) > 0">+<xsl:value-of select="senderfaxcountrycode"/><xsl:text> </xsl:text></xsl:if>
									<xsl:value-of select="senderfax"/>
									<xsl:if test="string-length(senderfaxextension) > 0"><xsl:text> </xsl:text><xsl:value-of select="senderfaxextension"/></xsl:if>
								</xsl:attribute>
								<xsl:comment>C.3.4.7:Sender’s Fax</xsl:comment>
							</telecom>
						</xsl:if>
						<!-- A.3.4.l Sender Email -->
						<xsl:if test="string-length(senderemailaddress) > 0">
							<telecom value="mailto:{senderemailaddress}"/>
						</xsl:if>
						<xsl:comment>C.3.4.8:Sender’s E-mail Address</xsl:comment>
						<assignedPerson classCode="PSN" determinerCode="INSTANCE">
							<!-- A.3.3.bcde Sender Name -->
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
							<!-- A.3.4.e Sender Country Code -->
							<xsl:if test="string-length(sendercountrycode) > 0">
								<asLocatedEntity classCode="LOCE">
									<location classCode="COUNTRY" determinerCode="INSTANCE">
										<code code="{sendercountrycode}" codeSystem="{$oidISOCountry}"/>
										<xsl:comment>C.3.4.5:Sender’s Country Code</xsl:comment>
									</location>
								</asLocatedEntity>
							</xsl:if>
						</assignedPerson>
						<!-- A.3.2 Sender Organization -->
						<!-- A.3.3.a Sender Department -->
						<representedOrganization classCode="ORG" determinerCode="INSTANCE">
							<xsl:call-template name="field-or-mask">
								<xsl:with-param name="element">name</xsl:with-param>
								<xsl:with-param name="value" select="senderdepartment"/>
							</xsl:call-template>
							<xsl:comment>C.3.3.1:Sender’s Department</xsl:comment>
							<assignedEntity classCode="ASSIGNED">
								<representedOrganization classCode="ORG" determinerCode="INSTANCE">
									<xsl:choose>
									<xsl:when test="senderorganization = 'PRIVACY'"><name nullFlavor="MSK"/></xsl:when>
									<xsl:otherwise>
										<xsl:call-template name="field-or-mask">
											<xsl:with-param name="element">name</xsl:with-param>
											<xsl:with-param name="value" select="senderorganization"/>
										</xsl:call-template>
									</xsl:otherwise>
								</xsl:choose>
								<xsl:comment>C.3.2:Sender’s Organisation</xsl:comment>
								</representedOrganization>
							</assignedEntity>
						</representedOrganization>
					</assignedEntity>
				</author>
			</controlActEvent>
		</subjectOf1>
	</xsl:template>
	<xsl:template match="literature">
		<xsl:if test="string-length(.) > 0">
			<reference typeCode="REFR">
				<!-- A.4.r Literature Reference(s) -->
				<document classCode="DOC" moodCode="EVN">
					<code code="{$literatureReference}" codeSystem="{$oidichreferencesource}" displayName="literatureReference"/>
					<text mediaType="text/plain" representation="B64"><xsl:value-of select="literaturedocuments"/></text>
					<xsl:comment>C.4.r.2:Included Documents</xsl:comment>
					<bibliographicDesignationText><xsl:value-of select="literaturereference"/></bibliographicDesignationText>
					<xsl:comment>C.4.r.1:Literature Reference(s)</xsl:comment>
				</document>
			</reference>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="pmdapublishedcountry">
		<xsl:if test="string-length(publishedcountry) > 0">
			<reference typeCode="REFR">
				<!-- A.4.r Literature Reference(s) -->
				<document classCode="DOC" moodCode="EVN">
					<code code="{$literatureReference}" codeSystem="{$oidichreferencesource}"/>
					<participation typeCode="AUT">
					<assignedEntity classCode="ASSIGNED">
					<representedOrganization classCode="ORG" determinerCode="INSTANCE">
					<asLocatedEntity classCode="LOCE">
					<location classCode="COUNTRY" determinerCode="INSTANCE">
					<code codeSystem="1.0.3166.1.2.2" code="{publishedcountry}">
					</code>
					</location>
					</asLocatedEntity>
					</representedOrganization>
					</assignedEntity>
					</participation>
					<title>
					(clinicalornonclinicalclassificaiton)
					</title>
				</document>
			</reference>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="patient" mode="identification">
		<component typeCode="COMP">
			<adverseEventAssessment classCode="INVSTG" moodCode="EVN">
				<subject1 typeCode="SBJ">
					<primaryRole classCode="INVSBJ">
						<player1 classCode="PSN" determinerCode="INSTANCE">
							<!-- B.1.1 Patient Name - Rule LEN-13 -->
							<name>
								<xsl:choose>
									<xsl:when test="translate(patientinitial, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ') = 'PRIVACY'"><xsl:attribute name="nullFlavor">MSK</xsl:attribute></xsl:when>
									<xsl:when test="translate(patientinitial, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ') = 'UNKNOWN'"><xsl:attribute name="nullFlavor">UNK</xsl:attribute></xsl:when>
									<xsl:when test="string-length(patientinitial) = 0"><xsl:attribute name="nullFlavor">UNK</xsl:attribute></xsl:when>
									<xsl:otherwise><xsl:value-of select="patientinitial"/></xsl:otherwise>
								</xsl:choose>
							</name>
							<xsl:comment>D.1: Patient (name or initials)</xsl:comment>
							<!-- B.1.5 Patient Sex -->
							<xsl:choose>
								<xsl:when test="string-length(patientsex) > 0"><administrativeGenderCode code="{patientsex}" codeSystem="{$oidGenderCode}"/></xsl:when>
								<xsl:otherwise><administrativeGenderCode nullFlavor="UNK"/></xsl:otherwise>
							</xsl:choose>
							<xsl:comment>D.5: Sex </xsl:comment>
							<!-- B.1.2.1 Patient Birth Time -->
							<xsl:if test="string-length(patientage/patientbirthdate) > 0"><birthTime value="{patientage/patientbirthdate}"/></xsl:if>
							<xsl:comment>D.2.1: Date of Birth</xsl:comment>
							<!-- B.1.9.1 Date of Death -->
							<xsl:if test="string-length(patientdeathdate) > 0">
								<deceasedTime value="{patientdeathdate}"/>
							</xsl:if>
							<xsl:comment>D.9.1: Date of Death</xsl:comment>
							<!-- B.1.1.1a Patient GP Medical Record Number -->
							<xsl:if test="string-length(patientgpmedicalrecordnumb) > 0">
								<asIdentifiedEntity classCode="IDENT">
									<id extension="{patientgpmedicalrecordnumb}" root="{$oidGPMedicalRecordNumber}"/>
									<code code="{$GPMrn}" codeSystem="{$oidSourceMedicalRecord}"/>
								</asIdentifiedEntity>
							</xsl:if>
							<xsl:comment>D.1.1.1: Patient Medical Record Number(s) and Source(s) of the Record Number (GP Medical Record Number) </xsl:comment>
							<!-- B.1.1.1b Patient Specialist Record Number -->
							<xsl:if test="string-length(patientspecialistrecordnumb) > 0">
								<asIdentifiedEntity classCode="IDENT">
									<id extension="{patientspecialistrecordnumb}" root="{$oidSpecialistRecordNumber}"/>
									<code code="{$SpecialistMrn}" codeSystem="{$oidSourceMedicalRecord}"/>
								</asIdentifiedEntity>
							</xsl:if>
							<xsl:comment>D.1.1.2: Patient Medical Record Number(s) and Source(s) of the Record Number (Specialist Record Number) </xsl:comment>
							<!-- B.1.1.1c Patient Hospital Record Number -->
							<xsl:if test="string-length(patienthospitalrecordnumb) > 0">
								<asIdentifiedEntity classCode="IDENT">
									<id extension="{patienthospitalrecordnumb}" root="{$oidHospitalRecordNumber}"/>
									<code code="{$HospitalMrn}" codeSystem="{$oidSourceMedicalRecord}"/>
								</asIdentifiedEntity>
							</xsl:if>
							<xsl:comment>D.1.1.3: Patient Medical Record Number(s) and Source(s) of the Record Number (Hospital Record Number)</xsl:comment>
							<!-- B.1.1.1d Patient Investigation Number -->
							<xsl:if test="string-length(patientinvestigationnumb) > 0">
								<asIdentifiedEntity classCode="IDENT">
									<id extension="{patientinvestigationnumb}" root="{$oidInvestigationNumber}"/>
									<code code="{$Investigation}" codeSystem="{$oidSourceMedicalRecord}"/>
								</asIdentifiedEntity>
							</xsl:if>
							<xsl:comment>D.1.1.4: Patient Medical Record Number(s) and Source(s) of the Record Number (Investigation Number)</xsl:comment>
							<!-- B.1.10 - Parent -->
							<xsl:apply-templates select="parent" mode="identification"/>
						</player1>
						<!-- A.5 - Study -->
						<xsl:apply-templates select="../studyidentification" mode="study"/>
						<!-- B.1 - Patient -->
						<xsl:apply-templates select="." mode="characteristics"/>
						<!-- B.1.7 - Patient Medical History -->
						<xsl:if test="count(medicalhistoryepisode) > 0 or string-length(patientmedicalhistorytext) > 0">
							<subjectOf2 typeCode="SBJ">
								<organizer classCode="CATEGORY" moodCode="EVN">
									<code code="{$RelevantMedicalHistoryAndConcurrentConditions}" codeSystem="{$oidValueGroupingCode}"/>
									<xsl:apply-templates select="medicalhistoryepisode"/>
									<xsl:if test="string-length(patientmedicalhistorytext) > 0">
										<component typeCode="COMP">
											<observation moodCode="EVN" classCode="OBS">
												<code code="{$HistoryAndConcurrentConditionText}" codeSystem="{$oidObservationCode}"/>
												<value xsi:type="ED" mediaType="text/plain"><xsl:value-of select="patientmedicalhistorytext"/></value>
												<xsl:comment>D.7.2:Text for Relevant Medical History and Concurrent Conditions (not including reaction / event)</xsl:comment>
											</observation>
										</component>
									</xsl:if>
									<xsl:if test="string-length(concomitanttherapies) > 0">
										<component typeCode="COMP">
											<observation moodCode="EVN" classCode="OBS">
												<code code="{$ConcommitantTherapy}" codeSystem="{$oidObservationCode}"/>
												
												<xsl:choose>
												<xsl:when test="concomitanttherapies = 1"><value xsi:type="BL" value="true"/></xsl:when>
												<xsl:when test="concomitanttherapies = 2"><value xsi:type="BL" value="false"/></xsl:when>
												<xsl:when test="concomitanttherapies = 3"><value xsi:type="BL" nullFlavor="NASK" /></xsl:when>
												</xsl:choose>
												<xsl:comment>D.7.3: Concomitant Therapies</xsl:comment>
											</observation>
										</component>
									</xsl:if>
								</organizer>
							</subjectOf2>
						</xsl:if>
						
						
						<!--   J2・13・r・1  -->
						<xsl:if test="count(clinicaltrialnotification) > 0">
							<subjectOf2 typeCode="SBJ">
								<organizer classCode="CATEGORY" moodCode="EVN">
									<code code="1" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.11"/>
									
									
										<component typeCode="COMP">
											<observation moodCode="EVN" classCode="OBS">
												<code code="7" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.10"/>
												<value xsi:type="INT" value="{clinicaltrialnotification}"></value>
												
											</observation>
										</component>
								</organizer>
							</subjectOf2>
						</xsl:if>
						
						
						<!--   J2・13・r・2  -->
						<xsl:if test="count(taegetteddisease) > 0">
							<subjectOf2 typeCode="SBJ">
								<organizer classCode="CATEGORY" moodCode="EVN">
									<code code="1" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.11"/>
									
															
										<component typeCode="COMP">
											<observation moodCode="EVN" classCode="OBS">
												<code code="8" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.10"/>
												<value xsi:type="ED">(taegetteddisease)</value>
																		
											</observation>
										</component>
								</organizer>
							</subjectOf2>
						</xsl:if>
						
						<!--   J2・13・r・3 -->
						<xsl:if test="count(studyphase) > 0">
							<subjectOf2 typeCode="SBJ">
								<organizer classCode="CATEGORY" moodCode="EVN">
									<code code="1" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.11"/>
										<component typeCode="COMP">
											<observation moodCode="EVN" classCode="OBS">
												<code code="9" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.10"/>
												<value xsi:type="CE" code="{studyphase}" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.7" codeSystemVersion="{studyphasecsv}"></value>
											</observation>
										</component>
								</organizer>
							</subjectOf2>
						</xsl:if>
							
						<!--   J2・13・r・4 -->
						<xsl:if test="count(patientundertreatment) > 0">
							<subjectOf2 typeCode="SBJ">
								<organizer classCode="CATEGORY" moodCode="EVN">
									<code code="1" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.11"/>
										<component typeCode="COMP">
											<observation moodCode="EVN" classCode="OBS">
												<code code="10" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.10"/>
												<value xsi:type="BL" value="{patientundertreatment}"></value>
											</observation>
										</component>
								</organizer>
							</subjectOf2>
						</xsl:if>
							
							<!-- B.1.8 - Patient Past Drug Therapy -->
						<xsl:if test="count(patientpastdrugtherapy) > 0">
							<subjectOf2 typeCode="SBJ">
								<organizer classCode="CATEGORY" moodCode="EVN">
									<code code="{$DrugHistory}" codeSystem="{$oidValueGroupingCode}"/>
									<xsl:apply-templates select="patientpastdrugtherapy"/>
								</organizer>
							</subjectOf2>
						</xsl:if>
						<!-- B.1.9 - Patient Death -->
						<xsl:apply-templates select="reportedcauseofdeath"/>
						<!-- B.2.i - Reaction -->
						<xsl:apply-templates select="reaction"/>
						<!-- B.3.r - Test -->
						<!-- <xsl:if test="string-length(test/testdate) > 0 or string-length(test/testname) > 0 or string-length(test/resultstestsprocedures) > 0"> -->
							<subjectOf2 typeCode="SBJ">
								<organizer classCode="CATEGORY" moodCode="EVN">
									<code code="{$TestsAndProceduresRelevantToTheInvestigation}" codeSystem="{$oidValueGroupingCode}"/>
										<xsl:apply-templates select="test"/>
										<xsl:apply-templates select="test/resulttestprocedures"/>
										
								</organizer>
							</subjectOf2>
						<!-- </xsl:if> -->
						
						<!-- B.4.k - Drug (main) -->
						<subjectOf2 typeCode="SBJ">
							<organizer classCode="CATEGORY" moodCode="EVN">
								<code code="{$DrugInformation}" codeSystem="{$oidValueGroupingCode}"/>
								<xsl:apply-templates select="drug" mode="main"/>
							</organizer>
						</subjectOf2>
						
						<!-- J2・25・k・r -->
						<subjectOf2 typeCode="SBJ">
							<organizer classCode="CATEGORY" moodCode="EVN">
								<code code="{$DrugInformation}" codeSystem="{$oidValueGroupingCode}"/>
								<xsl:apply-templates select="ingredientdetail" mode="main"/>
							</organizer>
						</subjectOf2>
					</primaryRole>
				</subject1>
				<!-- J2・19 -->
				<xsl:if test="string-length(jpnremarks1) > 0">
					<component1 typeCode="COMP">
						<observationEvent moodCode="EVN" classCode="OBS">
							<code code="13" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.10"/>
							<value xsi:type="ED"><xsl:value-of select="jpnremarks1"/></value>
															
						</observationEvent>
					</component1>
				</xsl:if>
				<!-- J2・20 -->
				<xsl:if test="string-length(jpnremarks2) > 0">
					<component1 typeCode="COMP">
						<observationEvent moodCode="EVN" classCode="OBS">
							<code code="14" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.10"/>
							<value xsi:type="ED"><xsl:value-of select="jpnremarks2"/></value>
																			
						</observationEvent>
					</component1>
				</xsl:if>
				<!-- J2・21 -->
				<xsl:if test="string-length(jpnremarks3) > 0">
					<component1 typeCode="COMP">
						<observationEvent moodCode="EVN" classCode="OBS">
							<code code="15" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.10"/>
							<value xsi:type="ED"><xsl:value-of select="jpnremarks3"/></value>
																			
						</observationEvent>
					</component1>
				</xsl:if>
				<!-- J2・22 -->
				<xsl:if test="string-length(jpnremarks4) > 0">
					<component1 typeCode="COMP">
						<observationEvent moodCode="EVN" classCode="OBS">
							<code code="16" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.10"/>
							<value xsi:type="ED"><xsl:value-of select="jpnremarks4"/></value>
																			
						</observationEvent>
					</component1>
				</xsl:if>
				<!-- J2・16 -->
				<xsl:if test="string-length(reportsummary) > 0">
					<component1 typeCode="COMP">
						<observationEvent moodCode="EVN" classCode="OBS">
							<code code="12" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.10"/>
							<value xsi:type="ED"><xsl:value-of select="reportsummary"/></value>
											
						</observationEvent>
					</component1>
				</xsl:if>
				<!-- J2・9 -->
				<xsl:if test="string-length(retrospectivesurgeryofinfection) > 0">
					<component1 typeCode="COMP">
						<observationEvent moodCode="EVN" classCode="OBS">
							<code code="4" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.10"/>
							<value xsi:type="ED"><xsl:value-of select="retrospectivesurgeryofinfection"/></value>
							
						</observationEvent>
					</component1>
				</xsl:if>
				
				<!-- J2・10 -->
				<xsl:if test="string-length(futureapproach) > 0">
					<component1 typeCode="COMP">
						<observationEvent moodCode="EVN" classCode="OBS">
							<code code="5" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.10"/>
							<value xsi:type="ED"><xsl:value-of select="futureapproach"/></value>
											
						</observationEvent>
					</component1>
				</xsl:if>
				<!-- J2・11 -->
				<xsl:if test="string-length(othercomments) > 0">
					<component1 typeCode="COMP">
						<observationEvent moodCode="EVN" classCode="OBS">
							<code code="6" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.10"/>
							<value xsi:type="ED"><xsl:value-of select="othercomments"/></value>
															
						</observationEvent>
					</component1>
				</xsl:if>
				<!-- B.4.k - Drug (causality) -->
						<xsl:apply-templates select="drug" mode="causality"/>
				<!-- B.5 - Summary -->
				<xsl:apply-templates select="summary"/>
			</adverseEventAssessment>
		</component>
	</xsl:template>
	
	<!-- Parent (identification) : 
	E2B(R2): element "parent"
	E2B(R3): element "role"
	-->
	<xsl:template match="parent" mode="identification">
		<xsl:if test="string-length(parentidentification) > 0 or string-length(parentbirthdate) > 0 or string-length(parentsex) > 0 or string-length(parentage) > 0">
		<role classCode="PRS">
			<code code="{$Parent}" codeSystem="2.16.840.1.113883.5.111"/>
				<associatedPerson determinerCode="INSTANCE" classCode="PSN">
					<!-- B.1.10.1 Parent Identification - Rule COD-21-->
					<xsl:if test="string-length(parentidentification) > 0">
						<name>
							<xsl:choose>
								<xsl:when test="translate(parentidentification, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')= 'PRIVACY'"><xsl:attribute name="nullFlavor">MSK</xsl:attribute></xsl:when>
								<xsl:when test="translate(parentidentification, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ') = 'UNKNOWN'"><xsl:attribute name="nullFlavor">UNK</xsl:attribute></xsl:when>
								<xsl:otherwise><xsl:value-of select="parentidentification"/></xsl:otherwise>
							</xsl:choose>
						</name>
						<xsl:comment>D.10.1: Parent Identification</xsl:comment>
					</xsl:if>
					<!-- B.1.10.6	Sex of Parent -->
					<xsl:if test="string-length(parentsex) > 0">
						<administrativeGenderCode code="{parentsex}" codeSystem="{$oidGenderCode}"/>
						<xsl:comment>D.10.6: Sex of Parent</xsl:comment>
					</xsl:if>
					<!-- B.1.10.2.1	Date of Birth of Parent -->
					<xsl:if test="string-length(parentbirthdate) > 0">
						<birthTime value="{parentbirthdate}"/>
						<xsl:comment>D.10.2.1: Date of Birth of Parent</xsl:comment>
					</xsl:if>
				</associatedPerson>
			<!-- B.1.10.2.2	Age of Parent -->
			<xsl:if test="string-length(parentage) > 0">
				<subjectOf2 typeCode="SBJ">
					<observation moodCode="EVN" classCode="OBS">
						<code code="{$Age}" codeSystem="{$oidObservationCode}"/>
						<value xsi:type="PQ" value="{parentage}">
							<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="parentageunit"/></xsl:call-template></xsl:attribute>
						</value>
						<xsl:comment>D.10.2.2a: Age of Parent (number)</xsl:comment>
						<xsl:comment>D.10.2.2b: Age of Parent (unit)</xsl:comment>
					</observation>
				</subjectOf2>
			</xsl:if>
			<!-- B.1.10.4 Weight -->
			<xsl:if test="string-length(parentweight) > 0">
				<subjectOf2 typeCode="SBJ">
					<observation moodCode="EVN" classCode="OBS">
						<code code="{$BodyWeight}" codeSystem="{$oidObservationCode}"/>
						<value xsi:type="PQ" value="{parentweight}" unit="kg"/>
						<xsl:comment>D.10.4: Body Weight (kg) of Parent</xsl:comment>
					</observation>
				</subjectOf2>
			</xsl:if>
			<!-- B.1.10.5 Height -->
			<xsl:if test="string-length(parentheight) > 0">
				<subjectOf2 typeCode="SBJ">
					<observation moodCode="EVN" classCode="OBS">
						<code code="{$Height}" codeSystem="{$oidObservationCode}"/>
						<value xsi:type="PQ" value="{parentheight}" unit="cm"/>
						<xsl:comment>D.10.5: Height (cm) of Parent</xsl:comment>
					</observation>
				</subjectOf2>
			</xsl:if>
			<!-- B.1.10.3 Last Menstrual Period Date -->
			<xsl:if test="string-length(parentlastmenstrualdate) > 0">
				<subjectOf2 typeCode="SBJ">
					<observation moodCode="EVN" classCode="OBS">
						<code code="{$LastMenstrualPeriodDate}" codeSystem="{$oidObservationCode}"/>
						<value xsi:type="TS" value="{parentlastmenstrualdate}"/>
						<xsl:comment>D.10.3: Last Menstrual Period Date of Parent</xsl:comment>
					</observation>
				</subjectOf2>
			</xsl:if>
			<!-- B.1.10.7 - Parent Medical History -->
			<xsl:if test="parentmedicalhistoryepisode">
				<subjectOf2 typeCode="SBJ">
					<organizer classCode="CATEGORY" moodCode="EVN">
						<code code="{$RelevantMedicalHistoryAndConcurrentConditions}" codeSystem="{$oidValueGroupingCode}"/>
						<!-- B.1.10.7.1.r - Parent Medical History -->
						<xsl:apply-templates select="parentmedicalhistoryepisode"/>
						<!-- B.1.10.7.2 - Text for Relevant Medical History and Concurrent Condition of Parent -->
						<xsl:if test="string-length(parentmedicalrelevanttext) > 0">
							<component typeCode="COMP">
								<observation moodCode="EVN" classCode="OBS">
									<code code="{$HistoryAndConcurrentConditionText}" codeSystem="{$oidObservationCode}"/>
									<value xsi:type="ED" mediaType="text/plain"><xsl:value-of select="parentmedicalrelevanttext"/></value>
									<xsl:comment>D.10.7.2: Text for Relevant Medical History and Concurrent Conditions of Parent</xsl:comment>
								</observation>
							</component>
						</xsl:if>
					</organizer>
				</subjectOf2>
				</xsl:if>
				<!-- Parent (past drug therapy) : 
	E2B(R2): element "parentpastdrugtherapy"
	E2B(R3): element "role"
	-->
	<!-- <xsl:template match="parentpastdrugtherapy"> -->
									

<subjectOf2 typeCode="SBJ">
	<organizer classCode="CATEGORY" moodCode="EVN">
		<code code="2" codeSystem="2.16.840.1.113883.3.989.2.1.1.20" codeSystemVersion="1.0" displayName="drugHistory"/>
		<component typeCode="COMP">
			<substanceAdministration classCode="SBADM" moodCode="EVN">
				<xsl:if test="string-length(parentdrugstartdate) > 0 or string-length(parentdrugenddate) > 0">
					<effectiveTime xsi:type="IVL_TS">
						<xsl:if test="string-length(parentdrugstartdate) > 0">
							<!-- B.1.10.8.r.c - Parent Drug Start Date -->
							<low value="{parentdrugstartdate}"/>
							<xsl:comment>D.10.8.r.4: Start Date</xsl:comment>
						</xsl:if>
						<xsl:if test="string-length(parentdrugenddate) > 0">
							<!-- B.1.10.8.r.e - Parent Drug End Date -->
							<high value="{parentdrugenddate}"/>
							<xsl:comment>D.10.8.r.5: End Date</xsl:comment>
						</xsl:if>
					</effectiveTime>
				</xsl:if>
				<xsl:if test="string-length(parentphpidversion) > 0">
					<consumable typeCode="CSM">
						<instanceOfKind classCode="INST">
							<kindOfProduct classCode="MMAT" determinerCode="KIND">
								
								
									<code codeSystem="TBD-PhPID" codeSystemVersion="{parentphpidversion}" code="{parentphpid}"></code>
									<xsl:comment>D.10.8.r.3a: PhPID Version Date/Number</xsl:comment>
									<xsl:comment>D.10.8.r.3b: Pharmaceutical Product Identifier (PhPID)</xsl:comment>
								
							</kindOfProduct>
						</instanceOfKind>
					</consumable>
				</xsl:if>	
				
				<!-- J2・12 -->
				<xsl:if test="string-length(clinicaldrugcode) > 0">
					<consumable typeCode="CSM">
						<instanceOfKind classCode="INST">
							<kindOfProduct classCode="MMAT" determinerCode="KIND">
								<asSpecializedKind classCode="GEN">
								<generalizedMaterialKind classCode="MAT" determinerCode="KIND">
								<code>
								<originalText>(clinicaldrugcode)</originalText>
								</code>
								</generalizedMaterialKind>
								</asSpecializedKind>
							</kindOfProduct>
						</instanceOfKind>
					</consumable>
				</xsl:if>	
				  
				
        <!-- <component typeCode="COMP">
			<substanceAdministration moodCode="EVN" classCode="SBADM"> -->
				
				<xsl:if test="string-length(parentmpidversion) > 0">
					<consumable typeCode="CSM">
						<instanceOfKind classCode="INST">
							<kindOfProduct classCode="MMAT" determinerCode="KIND">								
									<code codeSystem="TBD-MPID" codeSystemVersion="{parentmpidversion}" code="{parentmpid}"></code>
									<xsl:comment>D.10.8.r.2a: MPID Version Date/Number</xsl:comment>
									<xsl:comment>D.10.8.r.2b: Medicinal Product Identifier (MPID)</xsl:comment>
								
							</kindOfProduct>
						</instanceOfKind>
					</consumable>
				</xsl:if>
				
			<!-- </substanceAdministration>
		</component> -->
	<!-- </xsl:template> -->
			
<xsl:if test="string-length(parentdrugname) > 0">
					<consumable typeCode="CSM">
						<instanceOfKind classCode="INST">
							<kindOfProduct classCode="MMAT" determinerCode="KIND">
							<code/>
							<!-- B.1.10.8.r.a0	Name of Drug as Reported -->
							<name><xsl:value-of select="parentdrugname"/></name>
							</kindOfProduct>
						</instanceOfKind>
						<xsl:comment>D.10.8.r.1: Parent Drug Name</xsl:comment>
					</consumable>
				</xsl:if>
<outboundRelationship2 typeCode="COMP">
	<substanceAdministration classCode="SBADM" moodCode="EVN">
	<xsl:if test="string-length(parentdrginventedname) > 0">
	<consumable typeCode="CSM">
				<instanceOfKind classCode="INST">
				<kindOfProduct classCode="MMAT" determinerCode="KIND">
					<name><xsl:value-of select="parentdrginventedname"/></name>
							</kindOfProduct>
</instanceOfKind>
</consumable>
									<xsl:comment>D.10.8.r.1.EU.1: parentdrginventedname</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(parentdrginventedname) > 0">
					<consumable typeCode="CSM">
									<instanceOfKind classCode="INST">
				<kindOfProduct classCode="MMAT" determinerCode="KIND">
										<name><xsl:value-of select="parentdrginventedname"/></name>
									</kindOfProduct>
</instanceOfKind>
</consumable>
									<xsl:comment>D.10.8.r.1.EU.2: parentdrgscientificname</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(parentdrgtrademarkname) > 0">
					<consumable typeCode="CSM">
									<instanceOfKind classCode="INST">
				<kindOfProduct classCode="MMAT" determinerCode="KIND">
										<name><xsl:value-of select="parentdrgtrademarkname"/></name>
									</kindOfProduct>
</instanceOfKind>
</consumable>
									<xsl:comment>D.10.8.r.1.EU.3: parentdrgtrademarkname</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(parentdrgstrengthname) > 0">
					<consumable typeCode="CSM">
									<instanceOfKind classCode="INST">
				<kindOfProduct classCode="MMAT" determinerCode="KIND">
										<name><xsl:value-of select="parentdrgstrengthname"/></name>
									</kindOfProduct>
</instanceOfKind>
</consumable>
									<xsl:comment>D.10.8.r.1.EU.4: parentdrgstrengthname</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(parentdrgformname) > 0">
					<consumable typeCode="CSM">
									<instanceOfKind classCode="INST">
				<kindOfProduct classCode="MMAT" determinerCode="KIND">
										<name><xsl:value-of select="parentdrgformname"/></name>
									</kindOfProduct>
</instanceOfKind>
</consumable>
									<xsl:comment>D.10.8.r.1.EU.5: parentdrgformname</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(parentdrgcontainername) > 0">
					<consumable typeCode="CSM">
									<instanceOfKind classCode="INST">
				<kindOfProduct classCode="MMAT" determinerCode="KIND">
										<name><xsl:value-of select="parentdrgcontainername"/></name>
									</kindOfProduct>
</instanceOfKind>
</consumable>
									<xsl:comment>D.10.8.r.1.EU.6: parentdrgcontainername</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(parentdrgdevicename) > 0">
					<consumable typeCode="CSM">
									<instanceOfKind classCode="INST">
				<kindOfProduct classCode="MMAT" determinerCode="KIND">
										<name><xsl:value-of select="parentdrgdevicename"/></name>
									</kindOfProduct>
</instanceOfKind>
</consumable>
									<xsl:comment>D.10.8.r.1.EU.7: parentdrgdevicename</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(parentdrgintendedname) > 0">
					<consumable typeCode="CSM">
									<instanceOfKind classCode="INST">
				<kindOfProduct classCode="MMAT" determinerCode="KIND">
										<name><xsl:value-of select="parentdrgintendedname"/></name>
									</kindOfProduct>
</instanceOfKind>
</consumable>
									<xsl:comment>D.10.8.r.1.EU.8: parentdrgintendedname</xsl:comment>
				</xsl:if>	
			</substanceAdministration>
		</outboundRelationship2>
					<xsl:if test="string-length(parentdrgsubstancename) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="parentdrgsubstancename"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.10.8.r.EU.r.1 : parentdrgsubstancename</xsl:comment>
				</xsl:if>
				
	<xsl:if test="string-length(parentdrugindication) > 0">
					<outboundRelationship2 typeCode="RSON">	
					<observation moodCode="EVN" classCode="OBS">
							<code code="{$Indication}" codeSystem="{$oidObservationCode}"/>
							<!-- B.1.10.8.r.f1/2 Indication -->
							<xsl:variable name="isIndicationMeddraCode">
								<xsl:call-template name="isMeddraCode">
									<xsl:with-param name="code" select="parentdrugindication"/>
								</xsl:call-template>
							</xsl:variable>
							<xsl:choose>
								<xsl:when test="$isIndicationMeddraCode = 'yes'">
									<value xsi:type="CE" code="{parentdrugindication}" codeSystemVersion="{parentdrgindicationmeddraversion}" codeSystem="{$oidMedDRA}"/>
									<xsl:comment>D.10.8.r.6a: MedDRA Version for Indication</xsl:comment>
									<xsl:comment>D.10.8.r.6b: Indication (MedDRA code)</xsl:comment>
								</xsl:when>
								<xsl:otherwise>
									<value xsi:type="CE">
										<originalText>
											<xsl:value-of select="parentdrugindication"/>
											<xsl:if test="string-length(parentdrgindicationmeddraversion) > 0"> (<xsl:value-of select="parentdrgindicationmeddraversion"/>)</xsl:if>
										</originalText>
									</value>
									<xsl:comment>D.10.8.r.6a: MedDRA Version for Indication</xsl:comment>
									<xsl:comment>D.10.8.r.6b: Indication (MedDRA code)</xsl:comment>
								</xsl:otherwise>
							</xsl:choose>
							
						</observation>
					</outboundRelationship2>
				</xsl:if>
				<xsl:if test="string-length(parentdrugreaction) > 0">
					<outboundRelationship2 typeCode="CAUS">
						<observation moodCode="EVN" classCode="OBS">
							<code code="{$Reaction}" codeSystem="{$oidObservationCode}"/>
							<!-- B.1.10.8.r.g1/2 Reaction -->
							<xsl:variable name="isReractionMeddraCode">
								<xsl:call-template name="isMeddraCode">
									<xsl:with-param name="code" select="parentdrugreaction"/>
								</xsl:call-template>
							</xsl:variable>
							<xsl:choose>
								<xsl:when test="$isReractionMeddraCode = 'yes'">
									<value xsi:type="CE" code="{parentdrugreaction}" codeSystemVersion="{parentdrgreactionmeddraversion}" codeSystem="{$oidMedDRA}"/>
									<xsl:comment>D.10.8.r.7a: MedDRA Version for Reaction</xsl:comment>
									<xsl:comment>D.10.8.r.7b: Reactions (MedDRA code)</xsl:comment>
								</xsl:when>
								<xsl:otherwise>
									<value xsi:type="CE">
										<originalText>
											<xsl:value-of select="parentdrugreaction"/>
											<xsl:if test="string-length(parentdrgreactionmeddraversion) > 0"> (<xsl:value-of select="parentdrgreactionmeddraversion"/>)</xsl:if>
										</originalText>
									</value>
									<xsl:comment>D.10.8.r.7a: MedDRA Version for Reaction</xsl:comment>
									<xsl:comment>D.10.8.r.7b: Reactions (MedDRA code)</xsl:comment>
								</xsl:otherwise>
							</xsl:choose>
						</observation>
					</outboundRelationship2>
				</xsl:if>
<outboundRelationship2 typeCode	= 'COMP'>
<substanceAdministration classCode="SBADM" moodCode="EVN">			
				<xsl:if test="string-length(parentdrgsubstancetermid) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="parentdrgsubstancetermid"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.10.8.r.EU.r.2a : parentdrgsubstancetermid</xsl:comment>
				</xsl:if>
				<xsl:if test="string-length(parentdrgsubstancetermidversion) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="parentdrgsubstancetermidversion"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.10.8.r.EU.r.2b : parentdrgsubstancetermidversion</xsl:comment>
				</xsl:if>
						
				<xsl:if test="string-length(parentdrgsubstancestrength) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="parentdrgsubstancestrength"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.10.8.r.EU.r.3a : parentdrgsubstancestrength</xsl:comment>
				</xsl:if>
				<xsl:if test="string-length(parentdrgsubstancestrengthunit) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="parentdrgsubstancestrengthunit"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.10.8.r.EU.r.3b : parentdrgsubstancestrengthunit</xsl:comment>
				</xsl:if>
				</substanceAdministration>
				</outboundRelationship2>
			</substanceAdministration>
				</component>
				</organizer>
</subjectOf2>			
			
				
			<!-- B.1.10.8.r Past Drug Therapy -->
			<xsl:if test="parentpastdrugtherapy">
				<subjectOf2 typeCode="SBJ">
					<organizer classCode="CATEGORY" moodCode="EVN">
						<code code="{$DrugHistory}" codeSystem="{$oidValueGroupingCode}"/>
						<xsl:apply-templates select="parentpastdrugtherapy"/>
					</organizer>
				</subjectOf2>
			</xsl:if>
		</role>
		</xsl:if>
	</xsl:template>

	<!-- Parent (medical history episode) : 
	E2B(R2): element "parentmedicalhistoryepisode"
	E2B(R3): element "role"
	-->
	<xsl:template match="parentmedicalhistoryepisode">
		<component typeCode="COMP">
			<observation moodCode="EVN" classCode="OBS">
				<!-- B.1.10.7.1r.a Disease / Surgical Procedure/ etc. -->
				<xsl:variable name="isMeddraCode">
					<xsl:call-template name="isMeddraCode">
						<xsl:with-param name="code" select="parentmedicalepisodename"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:choose>
					<xsl:when test="$isMeddraCode = 'yes'">
						<code code="{parentmedicalepisodename}" codeSystemVersion="{parentmdepisodemeddraversion}" codeSystem="{$oidMedDRA}"/>
						<xsl:comment>D.10.7.1.r.1a: MedDRA Version for Medical History</xsl:comment>
						<xsl:comment>D.10.7.1.r.1b: Medical History (disease / surgical procedure / etc.) (MedDRA code)</xsl:comment>
					</xsl:when>
					<xsl:otherwise>
						<code>
							<originalText>
								<xsl:value-of select="parentmedicalepisodename"/>
								<xsl:if test="string-length(parentmdepisodemeddraversion) > 0"> (<xsl:value-of select="parentmdepisodemeddraversion"/>)</xsl:if>
							</originalText>
							<xsl:comment>D.10.7.1.r.1a: MedDRA Version for Medical History</xsl:comment>
							<xsl:comment>D.10.7.1.r.1b: Medical History (disease / surgical procedure / etc.) (MedDRA code)</xsl:comment>
						</code>
					</xsl:otherwise>
				</xsl:choose>
				<!-- B.1.10.7.1r.cd Start Date and End Date -->
				<xsl:if test="string-length(parentmedicalstartdate) > 0 or string-length(parentmedicalenddate) > 0">
					<effectiveTime xsi:type="IVL_TS">
						<xsl:if test="string-length(parentmedicalstartdate) > 0"><low value="{parentmedicalstartdate}"/>
						<xsl:comment>D.10.7.1.r.2: Start Date</xsl:comment>
						</xsl:if>
						<xsl:if test="string-length(parentmedicalenddate) > 0"><high value="{parentmedicalenddate}"/>
						<xsl:comment>D.10.7.1.r.4: End Date</xsl:comment>
						</xsl:if>
					</effectiveTime>
				</xsl:if>
				<!-- B.1.10.7.1r.g Comments  -->
				<xsl:if test="string-length(parentmedicalcomment) > 0">
					<outboundRelationship2 typeCode="COMP">
						<observation moodCode="EVN" classCode="OBS">
							<code code="{$Comment}" codeSystem="{$oidObservationCode}"/>
							<value xsi:type="ED"><xsl:value-of select="parentmedicalcomment"/></value>
							<xsl:comment>D.10.7.1.r.5: Comments</xsl:comment>
						</observation>
					</outboundRelationship2>
				</xsl:if>
				<!-- B.1.10.7.1.r.f - Continuing -->
				<xsl:if test="parentmedicalcontinue = 1 or parentmedicalcontinue = 2 or parentmedicalcontinue = 3">
					<inboundRelationship typeCode="REFR">
						<observation moodCode="EVN" classCode="OBS">
							<code code="{$Continuing}"  codeSystem="{$oidObservationCode}"/>
							<xsl:choose>
								<xsl:when test="parentmedicalcontinue = 1"><value xsi:type="BL" value="true"/><xsl:comment>D.10.7.1.r.3: Continuing</xsl:comment></xsl:when>
								<xsl:when test="parentmedicalcontinue = 2"><value xsi:type="BL" value="false"/><xsl:comment>D.10.7.1.r.3: Continuing</xsl:comment></xsl:when>
								<xsl:when test="parentmedicalcontinue = 3"><value xsi:type="BL" nullFlavor="NASK" /><xsl:comment>D.10.7.1.r.3: Continuing</xsl:comment></xsl:when>
								
							</xsl:choose>
						</observation>
					</inboundRelationship>
				</xsl:if>
			</observation>
		</component>
	</xsl:template>

	
	
		<!-- Patient (characteristics) : 
	E2B(R2): element "patient"
	E2B(R3): element "primaryRole"
	-->
	<xsl:template match="patient" mode="characteristics">
		<!-- B.1.2.2.ab Age at time of onset of reaction/event - Rule COD-10 -->
		<xsl:if test="string-length(patientage/patientonsetage) > 0">
			<subjectOf2 typeCode="SBJ">
				<observation moodCode="EVN" classCode="OBS">
					<code code="{$Age}" codeSystem="{$oidObservationCode}"/>
					<value xsi:type="PQ" value="{patientage/patientonsetage}">
						<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="patientage/patientonsetageunit"/></xsl:call-template></xsl:attribute>
					</value>
					<xsl:comment>D.2.2a: Age at Time of Onset of Reaction / Event (number) </xsl:comment>
					<xsl:comment>D.2.2b: Age at Time of Onset of Reaction / Event (unit) </xsl:comment>
				</observation>
			</subjectOf2>
		</xsl:if>
		<!-- B.1.2.2.1.ab Gestation Period -->
		<xsl:if test="string-length(patientage/gestationperiod) > 0">
			<subjectOf2 typeCode="SBJ">
				<observation moodCode="EVN" classCode="OBS">
					<code code="{$GestationPeriod}" codeSystem="{$oidObservationCode}"/>
					<value xsi:type="PQ" value="{patientage/gestationperiod}">
						<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="patientage/gestationperiodunit"/></xsl:call-template></xsl:attribute>
					</value>
					<xsl:comment>D.2.2.1a: Gestation Period When Reaction / Event Was Observed in the Foetus (number)</xsl:comment>
					<xsl:comment>D.2.2.1b: Gestation Period When Reaction / Event Was Observed in the Foetus (unit) </xsl:comment>
				</observation>
			</subjectOf2>
		</xsl:if>	
		<!-- B.1.2.3. Age Group -->
		<xsl:if test="string-length(patientage/patientagegroup) > 0">
			<subjectOf2 typeCode="SBJ">
				<observation moodCode="EVN" classCode="OBS">
					<code code="{$AgeGroup}" codeSystem="{$oidObservationCode}"/>
					<value xsi:type="CE" code="{patientage/patientagegroup}" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientage/patientagegroupcsv}"/>
					<xsl:comment>D.2.3: Patient Age Group (as per reporter)</xsl:comment>
					<xsl:comment>D.2.3[Ver]: ICH Code List Version for Patient Age Group (as per reporter) </xsl:comment>
				</observation>
			</subjectOf2>
		</xsl:if>
		<!-- B.1.3. Body Weight -->
		<xsl:if test="string-length(patientweight) > 0">
			<subjectOf2 typeCode="SBJ">
				<observation moodCode="EVN" classCode="OBS">
					<code code="{$BodyWeight}" codeSystem="{$oidObservationCode}"/>
					<value xsi:type="PQ" value="{patientweight}" unit="kg"/>
					<xsl:comment>D.3: Body Weight (kg)</xsl:comment>
				</observation>
			</subjectOf2>
		</xsl:if>
		<!-- B.1.4 Height -->
		<xsl:if test="string-length(patientheight) > 0">
			<subjectOf2 typeCode="SBJ">
				<observation moodCode="EVN" classCode="OBS">
					<code code="{$Height}" codeSystem="{$oidObservationCode}"/>
					<value xsi:type="PQ" value="{patientheight}" unit="cm"/>
					<xsl:comment>D.4: Height (cm)</xsl:comment>
				</observation>
			</subjectOf2>
		</xsl:if>
		<!-- B.1.6 Last Menstrual Period Date -->
		<xsl:if test="string-length(patientlastmenstrualdate) > 0">
			<subjectOf2 typeCode="SBJ">
				<observation moodCode="EVN" classCode="OBS">
					<code code="{$LastMenstrualPeriodDate}" codeSystem="{$oidObservationCode}"/>
					<value xsi:type="TS" value="{patientlastmenstrualdate}"/>
					<xsl:comment>D.6: Last Menstrual Period Date</xsl:comment>
				</observation>
			</subjectOf2>
		</xsl:if>
	</xsl:template>
	
	<!-- Patient (medical history episode) : 
	E2B(R2): element "medicalhistoryepisode"
	E2B(R3): element "primaryRole"
	-->
	<xsl:template match="medicalhistoryepisode">
		<component typeCode="COMP">
			<observation moodCode="EVN" classCode="OBS">
				<!-- B.1.7.1r.a Disease / Surgical Procedure/ etc. -->
				<xsl:variable name="isMeddraCode">
					<xsl:call-template name="isMeddraCode">
						<xsl:with-param name="code" select="patientepisodename"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:choose>
					<xsl:when test="$isMeddraCode = 'yes'">
						<code code="{patientepisodename}" codeSystemVersion="{patientepisodenamemeddraversion}" codeSystem="{$oidMedDRA}"/>
					</xsl:when>
					<xsl:otherwise>
						<code>
							<originalText>
								<xsl:value-of select="patientepisodename"/>
								<xsl:if test="string-length(patientepisodenamemeddraversion) > 0"> (<xsl:value-of select="patientepisodenamemeddraversion"/>)</xsl:if>
							</originalText>
						</code>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:comment>D.7.1.r.1a: MedDRA Version for Medical History</xsl:comment>
				<xsl:comment>D.7.1.r.1b: Medical History (disease / surgical procedure / etc.) (MedDRA code)</xsl:comment>
				<!-- B.1.7.1r.cdf Start Date and End Date -->
				<xsl:if test="string-length(patientmedicalstartdate) > 0 or string-length(patientmedicalenddate) > 0">
					<effectiveTime xsi:type="IVL_TS">
						<xsl:if test="string-length(patientmedicalstartdate) > 0"><low value="{patientmedicalstartdate}"/></xsl:if>
						<xsl:if test="string-length(patientmedicalenddate) > 0"><high value="{patientmedicalenddate}"/></xsl:if>
					</effectiveTime>
				</xsl:if>
				<xsl:comment>D.7.1.r.2: Start Date</xsl:comment>
				<xsl:comment>D.7.1.r.4: End Date</xsl:comment>
				<!-- B.1.7.1.g - Comments -->
				<xsl:if test="string-length(patientmedicalcomment) > 0">
					<outboundRelationship2 typeCode="COMP">
						<observation moodCode="EVN" classCode="OBS">
							<code code="{$Comment}" codeSystem="{$oidObservationCode}"/>
							<value xsi:type="ED"><xsl:value-of select="patientmedicalcomment"/></value>
							<xsl:comment>D.7.1.r.5: Comments </xsl:comment>
						</observation>
					</outboundRelationship2>
				</xsl:if>
				
				<xsl:if test="string-length(patientfamilyhistory) > 0">
					<outboundRelationship2 typeCode="EXPL">
						<observation moodCode="EVN" classCode="OBS">
							<code code="38" codeSystem="{$oidObservationCode}"/>
							<xsl:choose>
								<xsl:when test="patientfamilyhistory = 1"><value xsi:type="BL" value="true"/></xsl:when>
								<xsl:when test="patientfamilyhistory = 2"><value xsi:type="BL" value="false"/></xsl:when>
								<xsl:when test="patientfamilyhistory = 3"><value xsi:type="BL" nullFlavor="NASK" /></xsl:when>
							</xsl:choose>
							<xsl:comment>D.7.1.r.6: Family History</xsl:comment>
						</observation>
					</outboundRelationship2>
				</xsl:if>
				<!-- B.1.7.1.r.f - Continuing -->
				<xsl:if test="patientmedicalcontinue = 1 or patientmedicalcontinue = 2 or patientmedicalcontinue = 3">
					<inboundRelationship typeCode="REFR">
						<observation moodCode="EVN" classCode="OBS">
							<code code="{$Continuing}" codeSystem="{$oidObservationCode}"/>
							<xsl:choose>
								<xsl:when test="patientmedicalcontinue = 1"><value xsi:type="BL" value="true"/></xsl:when>
								<xsl:when test="patientmedicalcontinue = 2"><value xsi:type="BL" value="false"/></xsl:when>
								<xsl:when test="patientmedicalcontinue = 3"><value xsi:type="BL" nullFlavor="NASK" /></xsl:when>
							</xsl:choose>
							<xsl:comment>D.7.1.r.3: Continuing </xsl:comment>
						</observation>
					</inboundRelationship>
				</xsl:if>
			</observation>
		</component>
	</xsl:template>
	
	<!-- Patient (past drug therapy) : 
	E2B(R2): element "patientpastdrugtherapy"
	E2B(R3): element "primaryRole"
	-->
	<xsl:template match="patientpastdrugtherapy">
		<component typeCode="COMP">
			<substanceAdministration moodCode="EVN" classCode="SBADM">
				<!-- B.1.8.r.ce Start and End Date -->
				<xsl:if test="string-length(patientdrugstartdate) > 0 or string-length(patientdrugenddate) > 0">
					<effectiveTime xsi:type="IVL_TS">
						<xsl:if test="string-length(patientdrugstartdate) > 0">
							<low value="{patientdrugstartdate}"/>
							<xsl:comment>D.8.r.4: Start Date</xsl:comment>
						</xsl:if>
						<xsl:if test="string-length(patientdrugenddate) > 0">
							<high value="{patientdrugenddate}"/>
							<xsl:comment>D.8.r.5: End Date</xsl:comment>
						</xsl:if>
					</effectiveTime>
				</xsl:if>
				<!-- B.1.8.r.a Name of Drug as Reported -->
				<xsl:if test="string-length(patientdrugname) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="patientdrugname"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.8.r.1: patientdrugname</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(patientdruginventedname) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="patientdruginventedname"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.8.r.1.EU.1: patientdruginventedname</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(patientdrugscientificname) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="patientdrugscientificname"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.8.r.1.EU.2: patientdrugscientificname</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(patientdrugtrademarkname) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="patientdrugtrademarkname"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.8.r.1.EU.3: patientdrugtrademarkname</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(patientdrugstrengthname) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="patientdrugstrengthname"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.8.r.1.EU.4: patientdrugstrengthname</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(patientdrugformname) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="patientdrugformname"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.8.r.1.EU.5: patientdrugformname</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(patientdrugcontainername) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="patientdrugcontainername"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.8.r.1.EU.6: patientdrugcontainername</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(patientdrugdevicename) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="patientdrugdevicename"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.8.r.1.EU.7: patientdrugdevicename</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(patientdrugintendedname) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="patientdrugintendedname"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.8.r.1.EU.8: patientdrugintendedname</xsl:comment>
				</xsl:if>
				<xsl:if test="string-length(patientmpidversion) > 0">
					<consumable typeCode="CSM">
						<instanceOfKind classCode="INST">
							<kindOfProduct classCode="MMAT" determinerCode="KIND">
				
					<code codeSystem="TBD-MPID" codeSystemVersion="{patientmpidversion}" code="{patientmpid}"></code>
					<xsl:comment>D.8.r.2a: MPID Version Date/Number</xsl:comment>
					<xsl:comment>D.8.r.2b: Medicinal Product Identifier (MPID)</xsl:comment>
					
							</kindOfProduct>
						</instanceOfKind>
					</consumable>
				</xsl:if>
				<xsl:if test="string-length(patientphpidversion) > 0">
					<consumable typeCode="CSM">
						<instanceOfKind classCode="INST">
							<kindOfProduct classCode="MMAT" determinerCode="KIND">
								<code codeSystem="TBD-PhPID" codeSystemVersion="{patientphpidversion}" code="{patientphpid}"></code>
								<xsl:comment>D.8.r.3a: PhPID Version Date/Number</xsl:comment>
								<xsl:comment>D.8.r.3b: Pharmaceutical Product Identifier (PhPID)</xsl:comment>
							</kindOfProduct>
						</instanceOfKind>
					</consumable>
				</xsl:if>
				<xsl:if test="string-length(patienteudrugsubstance/patientdrgsubstancename) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="patienteudrugsubstance/patientdrgsubstancename"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.8.r.EU.r.1 : patientdrgsubstancename</xsl:comment>
				</xsl:if>
				<xsl:if test="string-length(patienteudrugsubstance/patientdrgsubstancetermidversion) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="patienteudrugsubstance/patientdrgsubstancetermidversion"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.8.r.EU.r.2a : patientdrgsubstancetermidversion</xsl:comment>
				</xsl:if>
				<xsl:if test="string-length(patienteudrugsubstance/patientdrgsubstancetermid) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="patienteudrugsubstance/patientdrgsubstancetermid"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.8.r.EU.r.2b : patientdrgsubstancetermid</xsl:comment>
				</xsl:if>
				<xsl:if test="string-length(patienteudrugsubstance/patientdrgsubstancestrength) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="patienteudrugsubstance/patientdrgsubstancestrength"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.8.r.EU.r.3a : patientdrgsubstancestrength</xsl:comment>
				</xsl:if>
				<xsl:if test="string-length(patienteudrugsubstance/patientdrgsubstancestrengthunit) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="patienteudrugsubstance/patientdrgsubstancestrengthunit"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>D.8.r.EU.r.3b : patientdrgsubstancestrengthunit</xsl:comment>
				</xsl:if>
				<!-- B.1.8.r.f - Indication -->
				<xsl:if test="string-length(patientdrugindication) > 0">
					<xsl:variable name="isIndicationMeddraCode">
						<xsl:call-template name="isMeddraCode">
							<xsl:with-param name="code" select="patientdrugindication"/>
						</xsl:call-template>
					</xsl:variable>
					<outboundRelationship2 typeCode="RSON">
						<observation moodCode="EVN" classCode="OBS">
							<code code="{$Indication}" codeSystem="{$oidObservationCode}"/>
							<xsl:choose>
								<xsl:when test="$isIndicationMeddraCode = 'yes'">
									<value xsi:type="CE" code="{patientdrugindication}" codeSystemVersion="{patientdrugindicationmeddraversion}" codeSystem="{$oidMedDRA}"/>
								</xsl:when>
								<xsl:otherwise>
									<value xsi:type="CE">
										<originalText>
											<xsl:value-of select="patientdrugindication"/>
											<xsl:if test="string-length(patientindicationmeddraversion) > 0"> (<xsl:value-of select="patientindicationmeddraversion"/>)</xsl:if>
										</originalText>
									</value>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:comment>D.8.r.6a: MedDRA Version for Indication</xsl:comment>
							<xsl:comment>D.8.r.6b: Indication (MedDRA code)</xsl:comment>
						</observation>
					</outboundRelationship2>
				</xsl:if>
				<!-- B.1.8.r.f - Reaction -->
				<xsl:if test="string-length(patientdrugreaction) > 0">
					<xsl:variable name="isReactionMeddraCode">
						<xsl:call-template name="isMeddraCode">
							<xsl:with-param name="code" select="patientdrugreaction"/>
						</xsl:call-template>
					</xsl:variable>
					<outboundRelationship2 typeCode="CAUS">
						<observation moodCode="EVN" classCode="OBS">
							<code code="{$Reaction}" codeSystem="{$oidObservationCode}"/>
							<xsl:choose>
								<xsl:when test="$isReactionMeddraCode = 'yes'">
									<value xsi:type="CE" code="{patientdrugreaction}" codeSystemVersion="{patientdrgreactionmeddraversion}" codeSystem="{$oidMedDRA}"/>
								</xsl:when>
								<xsl:otherwise>
									<value xsi:type="CE">
										<originalText>
											<xsl:value-of select="patientdrugreaction"/>
											<xsl:if test="string-length(patientdrgreactionmeddraversion) > 0"> (<xsl:value-of select="patientdrgreactionmeddraversion"/>)</xsl:if>
										</originalText>
									</value>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:comment>D.8.r.7a: MedDRA Version for Reaction</xsl:comment>
							<xsl:comment>D.8.r.7b: Reaction (MedDRA code)</xsl:comment>
						</observation>
					</outboundRelationship2>
				</xsl:if>
			</substanceAdministration>
		</component>
	</xsl:template>

	<!-- Patient (reported cause of death) : 
	E2B(R2): element "patientdeathreport"
	E2B(R3): element "primaryRole"
	-->
	<xsl:template match="patientdeathcause">
		<subjectOf2 typeCode="SBJ">
			<!-- B.1.9.2 Reported Cause of Death -->
			<observation moodCode="EVN" classCode="OBS">
				<code code="{$ReportedCauseOfDeath}" codeSystem="{$oidObservationCode}"/>
				<xsl:variable name="isMeddraCode">
					<xsl:call-template name="isMeddraCode">
						<xsl:with-param name="code" select="patientdeathreport"/>
					</xsl:call-template>
				</xsl:variable>
				
						<value xsi:type="CE" code="{patientdeathreport}" codeSystemVersion="{patientdeathreportmeddraversion}" codeSystem="{$oidMedDRA}">
							<originalText>
								<xsl:value-of select="patientdeathreporttxt"/>
								
							</originalText>
						</value>
					<xsl:comment>D.9.2.r.1a: MedDRA Version for Reported Cause(s) of Death</xsl:comment>
					<xsl:comment>D.9.2.r.1b: Reported Cause(s) of Death (MedDRA code)</xsl:comment>
					<xsl:comment>D.9.2.r.2: Reported Cause(s) of Death (free text)</xsl:comment>
			</observation>
		</subjectOf2>
	</xsl:template>
	
	<!-- Patient (death) : 
	E2B(R2): element "patientdeath"
	E2B(R3): element "primaryRole"
	-->
	<xsl:template match="reportedcauseofdeath">
		<!-- B.1.9.2 - Patient Death Cause -->
		<xsl:apply-templates select="patientdeathcause"/>
		<!-- B.1.9.3-4 Autopsy -->
		<xsl:if test="string-length(../patientautopsyyesno) > 0">
			<subjectOf2 typeCode="SBJ">
				<!-- B.1.9.3 Autopsy Done Yes/No -->
				<observation moodCode="EVN" classCode="OBS">
					<code code="{$Autopsy}" codeSystem="{$oidObservationCode}"/>
					<xsl:choose>
						<xsl:when test="../patientautopsyyesno = 1">
							<value xsi:type="BL" value="true"/>
						</xsl:when>
						<xsl:when test="../patientautopsyyesno = 2">
							<value xsi:type="BL" value="false"/>
						</xsl:when>
						<xsl:when test="../patientautopsyyesno = 3">
							<value xsi:type="BL" nullFlavor="UNK"/>
						</xsl:when>
					</xsl:choose>
					<xsl:comment>D.9.3: Was Autopsy Done?</xsl:comment>
					<xsl:apply-templates select="patientautopsy"/>
				</observation>
			</subjectOf2>
		</xsl:if>
	</xsl:template>
	
	<!-- Patient (autopsy-determined cause of death) : 
	E2B(R2): element "patientautopsy"
	E2B(R3): element "primaryRole"
	-->
	<xsl:template match="patientautopsy">
		<!-- B.1.9.4 Autopsy-determined Cause of Death -->
		<outboundRelationship2 typeCode="DRIV">
			<observation moodCode="EVN" classCode="OBS">
				<code code="{$CauseOfDeath}" codeSystem="{$oidObservationCode}"/>
				<xsl:variable name="isMeddraCode">
					<xsl:call-template name="isMeddraCode">
						<xsl:with-param name="code" select="patientdetermineautopsy"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:choose>
					<xsl:when test="$isMeddraCode = 'yes'">
						<value xsi:type="CE" code="{patientdetermineautopsy}" codeSystemVersion="{patientdetermautopsmeddraversion}" codeSystem="{$oidMedDRA}"/>
					</xsl:when>
					<xsl:otherwise>
						<value xsi:type="CE" codeSystem="{$oidMedDRA}">
							<originalText>
								<xsl:value-of select="patientdeterminedautopsytxt"/>
						
							</originalText>
						</value>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:comment>D.9.4.r.1a: MedDRA Version for Autopsy-determined Cause(s) of Death</xsl:comment>
				<xsl:comment>D.9.4.r.1b: Autopsy-determined Cause(s) of Death (MedDRA code)</xsl:comment>
				<xsl:comment>D.9.4.r.2: Autopsy-determined Cause(s) of Death (free text)</xsl:comment>
			</observation>
		</outboundRelationship2>
	</xsl:template>
	<xsl:template match="studyidentification" mode="study">
		<xsl:if test ="position()=1">
			<xsl:if test="string-length(studyname) > 0">
				<xsl:choose>
					<xsl:when test="substring(studyname,15,1) ='#'">
					<subjectOf1 typeCode="SBJ">
							<researchStudy classCode="CLNTRL" moodCode="EVN">
								<!-- A.5.3 Sponsor Study Number -->
								<xsl:if test="string-length(sponsorstudynumb) > 0">
									<id extension="{sponsorstudynumb}" root="{$SponsorStudyNumber}"/>
								</xsl:if>
								<xsl:comment>C.5.3: Sponsor Study Number</xsl:comment>
								<!-- A.5.4 Study Type in which the Reaction(s)/Event(s) were Observed -->
								<xsl:if test="string-length(observestudytype) > 0">
									<code code="{observestudytype}" codeSystem="{$oidStudyType}" codeSystemVersion="{observestudytypecsv}"/>
								</xsl:if>
								<xsl:comment>C.5.4: Study Type Where Reaction(s) / Event(s) Were Observed </xsl:comment>
								<xsl:comment>C.5.4[Ver]: ICH Code List Version for Study Type Where Reaction(s) / Event(s) Were Observed</xsl:comment>
								<!-- A.5.2 Study Name -->
									<title><xsl:value-of select="substring(studyname,16,string-length(studyname))"/></title>
								<xsl:for-each select="studyregistration">
								<authorization typeCode="AUTH">
									<studyRegistration classCode="ACT" moodCode="EVN">
										<id extension="{studyregnumb}" root="{$StudyRegistrationNumber}"/>
										<xsl:comment>C.5.1.r.1: Study Registration Number</xsl:comment>
										<!-- C.5.1.r.1: Study Registration Number #1 -->
										<author typeCode="AUT">
											<territorialAuthority classCode="TERR">
												<governingPlace classCode="COUNTRY" determinerCode="INSTANCE">
													<code code="{studyregcountry}" codeSystem="1.0.3166.1.2.2"/>
													<xsl:comment>C.5.1.r.2: Study Registration Country</xsl:comment>
													<!-- C.5.1.r.2: Study Registration Country #1 -->
												</governingPlace>
											</territorialAuthority>
										</author>
									</studyRegistration>
								</authorization>
								</xsl:for-each>
							</researchStudy>
						</subjectOf1>
					</xsl:when>			
					<xsl:otherwise>
						<subjectOf1 typeCode="SBJ">
							<researchStudy classCode="CLNTRL" moodCode="EVN">
								<!-- A.5.3 Sponsor Study Number -->
								<xsl:if test="string-length(sponsorstudynumb) > 0">
									<id extension="{sponsorstudynumb}" root="{$SponsorStudyNumber}"/>
								</xsl:if>
								<xsl:comment>C.5.3: Sponsor Study Number</xsl:comment>
								<!-- A.5.4 Study Type in which the Reaction(s)/Event(s) were Observed -->
								<xsl:if test="string-length(observestudytype) > 0">
									<code code="{observestudytype}" codeSystem="{$oidStudyType}" codeSystemVersion="{observestudytypecsv}"/>
								</xsl:if>
								<xsl:comment>C.5.4: Study Type Where Reaction(s) / Event(s) Were Observed </xsl:comment>
								<xsl:comment>C.5.4[Ver]: ICH Code List Version for Study Type Where Reaction(s) / Event(s) Were Observed</xsl:comment>
								<!-- A.5.2 Study Name -->
								<xsl:if test="string-length(studyname) > 0">
									<title><xsl:value-of select="studyname"/></title>
								</xsl:if>
								<xsl:comment>C.5.2: Study Name</xsl:comment>
								<xsl:for-each select="studyregistration">
																<authorization typeCode="AUTH">
																	<studyRegistration classCode="ACT" moodCode="EVN">
																		<id extension="{studyregnumb}" root="{$StudyRegistrationNumber}"/>
																		<xsl:comment>C.5.1.r.1: Study Registration Number</xsl:comment>
																		<!-- C.5.1.r.1: Study Registration Number #1 -->
																		<author typeCode="AUT">
																			<territorialAuthority classCode="TERR">
																				<governingPlace classCode="COUNTRY" determinerCode="INSTANCE">
																					<code code="{studyregcountry}" codeSystem="1.0.3166.1.2.2"/>
																					<!-- C.5.1.r.2: Study Registration Country #1 -->
																				</governingPlace>
																			</territorialAuthority>
																		</author>
																	</studyRegistration>
																</authorization>
								</xsl:for-each>
							</researchStudy>
						</subjectOf1>
					</xsl:otherwise>
				</xsl:choose>		
			</xsl:if>
		</xsl:if>
	</xsl:template>
	<xsl:template match="reaction">
		<subjectOf2 typeCode="SBJ">
			<observation moodCode="EVN" classCode="OBS">
				<!-- internal reaction id -->
				<id root="RID{position()}"/>
				<code code="{$Reaction}" codeSystem="{$oidObservationCode}"/>
				<!-- B.2.i.3, 4, 5 Start, End and Duration of Reaction/Event -->
				<xsl:if test="string-length(reactionstartdate) > 0 or string-length(reactionenddate) > 0 or string-length(reactionduration) > 0">
					<xsl:choose>
						<xsl:when test="string-length(reactionstartdate) = 0 or string-length(reactionenddate) = 0 or string-length(reactionduration) = 0">
							<effectiveTime xsi:type="IVL_TS">
								<xsl:if test="string-length(reactionstartdate) > 0">
									<low value="{reactionstartdate}"/>
									<xsl:comment>E.i.4: Date of Start of Reaction / Event</xsl:comment>
								</xsl:if>
								<xsl:if test="string-length(reactionduration) > 0 and (string-length(reactionstartdate) = 0 or string-length(reactionenddate) = 0)">
									<width value="{reactionduration}">
										<xsl:attribute name="unit">
											<xsl:call-template name="getMapping">
												<xsl:with-param name="type">UCUM</xsl:with-param>
												<xsl:with-param name="code" select="reactiondurationunit"/>
											</xsl:call-template>
										</xsl:attribute>
									</width>
									<xsl:comment>E.i.6a: Duration of Reaction / Event (number)</xsl:comment>
									<xsl:comment>E.i.6b: Duration of Reaction / Event (unit)</xsl:comment>
								</xsl:if>
								<xsl:if test="string-length(reactionenddate) > 0">
									<high value="{reactionenddate}"/>
									<xsl:comment>E.i.5: Date of End of Reaction / Event</xsl:comment>
								</xsl:if>
							</effectiveTime>
						</xsl:when>
						<xsl:when test="string-length(reactionstartdate) > 0 and string-length(reactionenddate) > 0 and string-length(reactionduration) > 0">
						<effectiveTime xsi:type="SXPR_TS">
							<comp xsi:type="IVL_TS">
								<low value="{reactionstartdate}"/>
								<xsl:comment>E.i.4: Date of Start of Reaction / Event</xsl:comment>
								<high value="{reactionenddate}"/>
							</comp>
							<comp xsi:type="IVL_TS" operator="A">
								<width value="{reactionduration}">
										<xsl:attribute name="unit">
											<xsl:call-template name="getMapping">
												<xsl:with-param name="type">UCUM</xsl:with-param>
												<xsl:with-param name="code" select="reactiondurationunit"/>
											</xsl:call-template>
										</xsl:attribute>
									</width>
									<xsl:comment>E.i.6a: Duration of Reaction / Event (number)</xsl:comment>
									<xsl:comment>E.i.6b: Duration of Reaction / Event (unit)</xsl:comment>
							</comp>
						</effectiveTime>
						</xsl:when>
						<xsl:otherwise>
							<effectiveTime xsi:type="IVL_TS">
								<low value="{reactionstartdate}"/>
								<xsl:comment>E.i.4: Date of Start of Reaction / Event</xsl:comment>
								<high value="{reactionenddate}"/>
								<xsl:comment>E.i.5: Date of End of Reaction / Event</xsl:comment>
							</effectiveTime>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<!-- B.2.i.0, 1 Reaction/Event as Reported by Primary Source and in MedDRA terminology -->
				<xsl:if test="string-length(reactionmeddrallt) > 0 or string-length(primarysourcereactionnative) > 0">
					<value xsi:type="CE">
						<xsl:if test="string-length(reactionmeddracode) > 0">
							<xsl:attribute name="codeSystem"><xsl:value-of select="$oidMedDRA"/></xsl:attribute>
							<xsl:attribute name="code"><xsl:value-of select="reactionmeddracode"/></xsl:attribute>
							<xsl:comment>E.i.2.1b: Reaction / Event (MedDRA code)</xsl:comment>
						</xsl:if>
						<xsl:if test="string-length(reactionmeddraversion) > 0">
							<xsl:attribute name="codeSystemVersion"><xsl:value-of select="reactionmeddraversion"/></xsl:attribute>
							<xsl:comment>E.i.2.1a: MedDRA Version for Reaction / Event</xsl:comment>
						</xsl:if>
						<xsl:if test="string-length(primarysourcereactionnative) > 0">
							<originalText language="{primarysourcereactionnativelang}"><xsl:value-of select="primarysourcereactionnative"/></originalText>
							<xsl:comment>E.i.1.1a: Reaction / Event as Reported by the Primary Source in Native Language</xsl:comment>
							<xsl:comment>E.i.1.1b: Reaction / Event as Reported by the Primary Source Language</xsl:comment>
						</xsl:if>
					</value>
				</xsl:if>
				<!-- B.2.i Identification of the Country where the Reaction Occurred -->
				<xsl:if test="string-length(reactionoccurcountry) > 0">
					<location typeCode="LOC">
						<locatedEntity classCode="LOCE">
							<locatedPlace classCode="COUNTRY" determinerCode="INSTANCE">
								<code code="{reactionoccurcountry}" codeSystem="{$oidISOCountry}"/>
								<xsl:comment>E.i.9: Identification of the Country Where the Reaction / Event Occurred</xsl:comment>
							</locatedPlace>
						</locatedEntity>
					</location>
				</xsl:if>
				<!-- B.2.i.2.1 Term Highlighted by Reporter -->
				<xsl:if test="string-length(termhighlighted) > 0">
					<outboundRelationship2 typeCode="PERT">
						<observation moodCode="EVN" classCode="OBS">
							<code code="{$TermHighlightedByReporter}" codeSystem="{$oidObservationCode}"/>
							<value xsi:type="CE" code="{termhighlighted}" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}"/>
							<xsl:comment>E.i.3.1: Term Highlighted by the Reporter</xsl:comment>
							<xsl:comment>E.i.3.1[Ver]: Term Highlighted by the Reporter code system version</xsl:comment>
						</observation>
					</outboundRelationship2>
				</xsl:if>
				<!-- B.2.i.2.2 Seriousness Criteria at Event Level -->
				<outboundRelationship2 typeCode="PERT">
					<observation moodCode="EVN" classCode="OBS">
						<code code="{$ResultsInDeath}" codeSystem="{$oidObservationCode}"/>
						<xsl:choose>
							<xsl:when test="seriousnessdeath = 1">
								<value xsi:type="BL" value="true"/>
								<xsl:comment>E.i.3.2a: Results in Death</xsl:comment>
							</xsl:when>
							<xsl:otherwise>
								<value xsi:type="BL" nullFlavor="NI"/>
								<xsl:comment>E.i.3.2a: Results in Death</xsl:comment>
							</xsl:otherwise>
						</xsl:choose>
					</observation>
				</outboundRelationship2>
				<outboundRelationship2 typeCode="PERT">
					<observation moodCode="EVN" classCode="OBS">
						<code code="{$LifeThreatening}" codeSystem="{$oidObservationCode}"/>
						<xsl:choose>
							<xsl:when test="seriousnesslifethreatening = 1">
								<value xsi:type="BL" value="true"/>
								<xsl:comment>E.i.3.2b: Life Threatening</xsl:comment>
							</xsl:when>
							<xsl:otherwise>
								<value xsi:type="BL" nullFlavor="NI"/>
								<xsl:comment>E.i.3.2b: Life Threatening</xsl:comment>
							</xsl:otherwise>
						</xsl:choose>
					</observation>
				</outboundRelationship2>
				<outboundRelationship2 typeCode="PERT">
					<observation moodCode="EVN" classCode="OBS">
						<code code="{$CausedProlongedHospitalisation}" codeSystem="{$oidObservationCode}"/>
						<xsl:choose>
							<xsl:when test="seriousnesshospitalization = 1">
								<value xsi:type="BL" value="true"/>
								<xsl:comment>E.i.3.2c: Caused / Prolonged Hospitalisation</xsl:comment>
							</xsl:when>
							<xsl:otherwise>
								<value xsi:type="BL" nullFlavor="NI"/>
								<xsl:comment>E.i.3.2c: Caused / Prolonged Hospitalisation</xsl:comment>
							</xsl:otherwise>
						</xsl:choose>
					</observation>
				</outboundRelationship2>
				<outboundRelationship2 typeCode="PERT">
					<observation moodCode="EVN" classCode="OBS">
						<code code="{$DisablingIncapaciting}" codeSystem="{$oidObservationCode}"/>
						<xsl:choose>
							<xsl:when test="seriousnessdisabling = 1">
								<value xsi:type="BL" value="true"/>
								<xsl:comment>E.i.3.2d: Disabling / Incapacitating</xsl:comment>
							</xsl:when>
							<xsl:otherwise>
								<value xsi:type="BL" nullFlavor="NI"/>
								<xsl:comment>E.i.3.2d: Disabling / Incapacitating</xsl:comment>
							</xsl:otherwise>
						</xsl:choose>
					</observation>
				</outboundRelationship2>
				<outboundRelationship2 typeCode="PERT">
					<observation moodCode="EVN" classCode="OBS">
						<code code="{$CongenitalAnomalyBirthDefect}" codeSystem="{$oidObservationCode}"/>
						<xsl:choose>
							<xsl:when test="seriousnesscongenitalanomali = 1">
								<value xsi:type="BL" value="true"/>
								<xsl:comment>E.i.3.2e: Congenital Anomaly / Birth Defect</xsl:comment>
							</xsl:when>
							<xsl:otherwise>
								<value xsi:type="BL" nullFlavor="NI"/>
								<xsl:comment>E.i.3.2e: Congenital Anomaly / Birth Defect</xsl:comment>
							</xsl:otherwise>
						</xsl:choose>
					</observation>
				</outboundRelationship2>
				<outboundRelationship2 typeCode="PERT">
					<observation moodCode="EVN" classCode="OBS">
						<code code="{$OtherMedicallyImportantCondition}" codeSystem="{$oidObservationCode}"/>
						<xsl:choose>
							<xsl:when test="seriousnessother = 1">
								<value xsi:type="BL" value="true"/>
								<xsl:comment>E.i.3.2f: Other Medically Important Condition</xsl:comment>
							</xsl:when>
							<xsl:otherwise>
								<value xsi:type="BL" nullFlavor="NI"/>
								<xsl:comment>E.i.3.2f: Other Medically Important Condition</xsl:comment>
							</xsl:otherwise>
						</xsl:choose>
					</observation>
				</outboundRelationship2>
				<!-- B.2.i.8 Outcome of the Reaction -->
				<xsl:if test="string-length(reactionoutcome)>0">
					<outboundRelationship2 typeCode="PERT">
						<observation moodCode="EVN" classCode="OBS">
							<code code="{$Outcome}" codeSystem="{$oidObservationCode}"/>
							<xsl:choose>
								<xsl:when test="reactionoutcome = 6"><value xsi:type="CE" code="0" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}"/></xsl:when>
								<xsl:otherwise>
									<value xsi:type="CE" code="{reactionoutcome}" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}"/>
									<xsl:comment>E.i.7: Outcome of Reaction / Event at the Time of Last Observation</xsl:comment>
									<xsl:comment>Outcome of Reaction / Event at the Time of Last Observation code system version</xsl:comment>
								</xsl:otherwise>
							</xsl:choose>
						</observation>
					</outboundRelationship2>
				</xsl:if>
				<xsl:if test="string-length(reactionmedconfirmed)>0">
					<outboundRelationship2 typeCode="PERT">
						<observation moodCode="EVN" classCode="OBS">
						<code code="24" codeSystem="{$oidObservationCode}"/>
											
												
						
						<xsl:choose>
							<xsl:when test="reactionmedconfirmed = 1">
								<value xsi:type="BL" value="true"/>
							</xsl:when>
							<xsl:otherwise>
								<value xsi:type="BL" nullFlavor="NI"/>
							</xsl:otherwise>
						</xsl:choose>
											<xsl:comment>E.i.8: Medical Confirmation by Healthcare Professional</xsl:comment>
										</observation>
									</outboundRelationship2>
				</xsl:if>
				<xsl:if test="string-length(primarysourcereaction)>0">
					<outboundRelationship2 typeCode="PERT">
						<observation moodCode="EVN" classCode="OBS">
							<code code="30" codeSystem="{$oidObservationCode}"/>
								<value xsi:type="ED">
								<xsl:value-of select="primarysourcereaction"/>
								</value>
								<xsl:comment>E.i.1.2: Reaction / Event as Reported by the Primary Source for Translation</xsl:comment>
						</observation>
					</outboundRelationship2>
				</xsl:if>
			</observation>
		</subjectOf2>
	</xsl:template>
	<xsl:template match="test">
	
		<xsl:if test="string-length(testdate) > 0 or string-length(testname) > 0">
			<component typeCode="COMP">
				<observation moodCode="EVN" classCode="OBS">
					<!-- B.3.r.c1-2 Test Name-->
					<xsl:variable name="isMeddraCode">
						<xsl:call-template name="isMeddraCode">
							<xsl:with-param name="code" select="testname"/>
						</xsl:call-template>
					</xsl:variable>
					
						
							<code code="{testmeddracode}" codeSystem="{$oidMedDRA}" codeSystemVersion="{testmeddraversion}">
						
								<originalText>
									<xsl:value-of select="testname"/>
								</originalText>
								<xsl:comment>F.r.2.1: Test Name (free text)</xsl:comment>
							</code>
							<xsl:comment>F.r.2.2a: MedDRA Version for Test Name</xsl:comment>
							<xsl:comment>F.r.2.2b: Test Name (MedDRA code)</xsl:comment>
						
					<!-- B.3.r.b Test Date and Time -->
					<xsl:choose>
						<xsl:when test="string-length(testdate) > 0">
						<!--	<effectiveTime xsi:type="SXCM_TS" value="{testdate}"/> -->
							<effectiveTime value="{testdate}"/>
							<xsl:comment>F.r.1: Test Date</xsl:comment>
						</xsl:when>
						<xsl:otherwise>
							<effectiveTime nullFlavor="UNK"/>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:choose>
											<xsl:when test="number(testresult)">
												<value xsi:type="IVL_PQ">
													<center value="{testresult}">
														<xsl:if test="string-length(testunit) > 0 or not(testunit = 'Not Applicable') or not(testunit = 'NA') or not(testunit = 'N/A')">
															<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="testunit"/></xsl:call-template></xsl:attribute>
														</xsl:if>
														<xsl:if test="string-length(testunit) = 0 or testunit = 'Not Applicable' or testunit = 'NA' or testunit = 'N/A'" >
															<xsl:attribute name="unit">1</xsl:attribute>
														</xsl:if>
													</center>
													<xsl:comment>F.r.3.2: Test Result (value / qualifier)</xsl:comment>
												</value>
											</xsl:when>
											<xsl:otherwise>
												<value xsi:type="ED">
													<xsl:value-of select="testresult"/>
													<xsl:text> </xsl:text>
													<xsl:if test="string-length(testunit)>0">
														<xsl:value-of select="testunit"/>
													</xsl:if>
												</value>
												<xsl:comment>F.r.3.2: Test Result (value / qualifier)</xsl:comment>
												<xsl:comment>F.r.3.3: Test Result (unit)</xsl:comment>
											</xsl:otherwise>
					</xsl:choose>
					
										<value xsi:type="ED">
											<xsl:value-of select="testresulttxt"/>
											
												</value>
							<xsl:comment>F.r.3.4:  Result Unstructured Data (free text)</xsl:comment>
					<interpretationCode code="{testresultcode}" codeSystem="2.16.840.1.113883.3.989.2.1.1.12" codeSystemVersion="{testresultcsv}"/> 
					<xsl:comment>Test Result (code) code system version</xsl:comment>
					<xsl:comment>F.r.3.1: Test Result (code)</xsl:comment>
					<!-- B.3.r.def Results of Tests -->
					
					<!-- B.3.r.1 Lowest Result Range -->
					<xsl:if test="string-length(lowtestrange) > 0">
						<referenceRange  typeCode="REFV">
							<observationRange classCode="OBS" moodCode="EVN.CRT">
								<xsl:choose>
									<xsl:when test="number(lowtestrange)">
										<value xsi:type="PQ" value="{lowtestrange}">
											<xsl:attribute name="unit">
												<xsl:choose><!--	<xsl:when test="string-length(testunit) > 0">{<xsl:value-of select="translate(testunit, ' ', '-')"/>}</xsl:when> -->
													<xsl:when test="string-length(testunit) > 0">
														<xsl:call-template name="getMapping">
															<xsl:with-param name="type">UCUM</xsl:with-param>
															<xsl:with-param name="code" select="testunit"/>
														</xsl:call-template>
													</xsl:when>
													<xsl:otherwise>1</xsl:otherwise>
												</xsl:choose>
											</xsl:attribute>
										</value>
										<xsl:comment>F.r.4 :Normal Low Value</xsl:comment>
										<interpretationCode code="L" codeSystem="2.16.840.1.113883.5.83"/>
									</xsl:when>
									<xsl:otherwise>
										<value xsi:type="ED">
											<xsl:value-of select="lowtestrange"/>
										</value>
										<xsl:comment>F.r.4 :Normal Low Value</xsl:comment>
										<interpretationCode code="L" codeSystem="2.16.840.1.113883.5.83"/>
									</xsl:otherwise>
								</xsl:choose>
							</observationRange>
						</referenceRange>
					</xsl:if>
					<!-- B.3.r.1 Highest Result Range -->
					<xsl:if test="string-length(hightestrange) > 0">
						<referenceRange typeCode="REFV">
							<observationRange classCode="OBS" moodCode="EVN.CRT">
								<xsl:choose>
									<xsl:when test="number(hightestrange)">
										<value xsi:type="PQ" value="{hightestrange}">
											<xsl:attribute name="unit">
												<xsl:choose><!-- <xsl:when test="string-length(testunit) > 0">{<xsl:value-of select="translate(testunit, ' ', '-')"/>}</xsl:when> -->
													<xsl:when test="string-length(testunit) > 0">
														<xsl:call-template name="getMapping">
															<xsl:with-param name="type">UCUM</xsl:with-param>
															<xsl:with-param name="code" select="testunit"/>
														</xsl:call-template>
													</xsl:when>
													<xsl:otherwise>1</xsl:otherwise>
												</xsl:choose>
											</xsl:attribute>
										</value>
										<xsl:comment>F.r.5: Normal High Value</xsl:comment>
										<interpretationCode code="H" codeSystem="2.16.840.1.113883.5.83"/>
									</xsl:when>
									<xsl:otherwise>
										<value xsi:type="ED">
											<xsl:value-of select="hightestrange"/>
										</value>
										<xsl:comment>F.r.5: Normal High Value</xsl:comment>
										<interpretationCode code="H" codeSystem="2.16.840.1.113883.5.83"/>
									</xsl:otherwise>
								</xsl:choose>
							</observationRange>
						</referenceRange>
					</xsl:if>
					<!-- B.3.r.4 More Information Available -->
					<outboundRelationship2 typeCode="REFR">
						<observation moodCode="EVN" classCode="OBS">
							<code code="{$MoreInformationAvailable}" codeSystem="{$oidObservationCode}"/>
							<xsl:choose>
								<xsl:when test="moreinformation = 1">
									<value xsi:type="BL" value="true"/>
									<xsl:comment>F.r.7: More Information Available</xsl:comment>
								</xsl:when>
								<xsl:when test="moreinformation = 2">
									<value xsi:type="BL" value="false"/>
									<xsl:comment>F.r.7: More Information Available</xsl:comment>
								</xsl:when>
								<xsl:otherwise>
									<value xsi:type="BL" nullFlavor="UNK"/>
								</xsl:otherwise>
							</xsl:choose>
						</observation>
					</outboundRelationship2>
				</observation>
			</component>
		</xsl:if>
	</xsl:template>
	
	<!-- B.3.r.3 Test Comments in an additional test occurrence -->
	<xsl:template match="test/resulttestprocedures">
	
		<xsl:if test="string-length(.) > 0">
			<component typeCode="COMP">
				<observation moodCode="EVN" classCode="OBS">
					<code codeSystem="2.16.840.1.113883.6.163"/>
					<outboundRelationship2 typeCode="PERT">
						<observation moodCode="EVN" classCode="OBS">
							<code code="{$Comment}" codeSystem="{$oidObservationCode}"/>
							<value xsi:type="ED">
								<xsl:value-of select="."/>
							</value>
							<xsl:comment>F.r.6: Comments (free text)</xsl:comment>
						</observation>
					</outboundRelationship2>
				</observation>
			</component>
		</xsl:if>
	</xsl:template>
	<xsl:template match="drug" mode="main">
				<component typeCode="COMP">
					<substanceAdministration moodCode="EVN" classCode="SBADM">
						<id root="DID{position()}"/>
						<consumable typeCode="CSM">
							<instanceOfKind classCode="INST">
								<kindOfProduct classCode="MMAT" determinerCode="KIND">
									
									<name>
										<xsl:choose>
												<xsl:when test="string-length(medicinalproduct) > 0"><xsl:value-of select="medicinalproduct"/></xsl:when>
												<xsl:otherwise>
													<xsl:for-each select="./activesubstance/activesubstancename">
														<xsl:value-of select="concat(., substring(',', 2 - (position() != last())))" />
													</xsl:for-each>														
													</xsl:otherwise>
										</xsl:choose>
									</name>
									<asManufacturedProduct classCode="MANU">
										<xsl:comment> G.k.3.1: Authorisation / Application Number </xsl:comment>
										<xsl:comment> G.k.3.2: Country of Authorisation / Application </xsl:comment>
										<xsl:comment> G.k.3.3: Name of Holder / Applicant </xsl:comment>
										<xsl:if test="string-length(drugauthorizationnumb) > 0 or string-length(drugauthorizationholder) > 0 or string-length(drugauthorizationcountry) > 0">
											<subjectOf typeCode="SBJ">
												<approval classCode="CNTRCT" moodCode="EVN">
													<xsl:if test="string-length(drugauthorizationnumb) > 0">
														<id extension="{drugauthorizationnumb}" root="{$oidAuthorisationNumber}"/>
													</xsl:if>
													<xsl:if test="string-length(drugauthorizationholder) > 0">
														<holder typeCode="HLD">
															<role classCode="HLD">
																<playingOrganization classCode="ORG" determinerCode="INSTANCE">
																	<name><xsl:value-of select="drugauthorizationholder"/></name>
																</playingOrganization>
															</role>
														</holder>
													</xsl:if>
													<xsl:if test="string-length(drugauthorizationcountry) > 0">
														<author typeCode="AUT">
															<territorialAuthority classCode="TERR">
																<territory classCode="NAT" determinerCode="INSTANCE">
																	<code codeSystem="{$oidISOCountry}" code="{drugauthorizationcountry}"/>
																</territory>
															</territorialAuthority>
														</author>
													</xsl:if>
												</approval>
											</subjectOf>
										</xsl:if>
									</asManufacturedProduct>
									<xsl:comment> G.k.2.3.r.1: Substance / Specified Substance Name </xsl:comment>
									<xsl:apply-templates select="activesubstance"/>
								</kindOfProduct>
								<xsl:comment> G.k.2.4: Identification of the Country Where the Drug Was Obtained </xsl:comment>
								<xsl:if test="string-length(obtaindrugcountry) > 0">
									<subjectOf typeCode="SBJ">
										<productEvent classCode="ACT" moodCode="EVN">
											<code code="{$RetailSupply}" codeSystem="{$oidActionPerformedCode}"/>
											<performer typeCode="PRF">
												<assignedEntity classCode="ASSIGNED">
													<representedOrganization classCode="ORG" determinerCode="INSTANCE">
														<addr>
															<country><xsl:value-of select="obtaindrugcountry"/></country>
														</addr>
													</representedOrganization>
												</assignedEntity>
											</performer>
										</productEvent>
									</subjectOf>
								</xsl:if>
							</instanceOfKind>
						</consumable>
						
						<consumable typeCode="CSM">
							<instanceOfKind classCode="INST">
								<kindOfProduct classCode="MMAT" determinerCode="KIND">
									<xsl:comment> G.k.2.2: Medicinal Product Name as Reported by the Primary Source </xsl:comment>
									<code codeSystem='TBD-MPID'>
									<xsl:attribute name="codeSystemVersion"><xsl:value-of select="drugmpidversion"/></xsl:attribute>
									<xsl:attribute name="code"><xsl:value-of select="drugmpid"/></xsl:attribute>
									</code>
									
									
									
								</kindOfProduct>
								
							</instanceOfKind>
						</consumable>
						<consumable typeCode="CSM">
							<instanceOfKind classCode="INST">
								<kindOfProduct classCode="MMAT" determinerCode="KIND">
									<code codeSystem='TBD-PhPID'>
									<xsl:attribute name="codeSystemVersion"><xsl:value-of select="drugphpidversion"/></xsl:attribute>
									<xsl:attribute name="code"><xsl:value-of select="drugphpid"/></xsl:attribute>
									</code>
						
								</kindOfProduct>
							</instanceOfKind>
						</consumable>
						<xsl:if test="string-length(druginventedname) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="druginventedname"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>G.k.2.2.EU.1: druginventedname</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(drugscientificname) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="drugscientificname"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>G.k.2.2.EU.2: drugscientificname</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(drugtrademarkname) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="drugtrademarkname"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>G.k.2.2.EU.3: drugtrademarkname</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(drugstrengthname) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="drugstrengthname"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>G.k.2.2.EU.4: drugstrengthname</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(drugformname) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="drugformname"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>G.k.2.2.EU.5: drugformname</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(drugcontainername) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="drugcontainername"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>G.k.2.2.EU.6: drugcontainername</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(drugdevicename) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="drugdevicename"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>G.k.2.2.EU.7: drugdevicename</xsl:comment>
				</xsl:if>
					<xsl:if test="string-length(drugintendedname) > 0">
									<consumable typeCode="CSM">
										<instanceOfKind classCode="INST">
											<kindOfProduct classCode="MMAT" determinerCode="KIND">
												<name><xsl:value-of select="drugintendedname"/></name>
											</kindOfProduct>
										</instanceOfKind>
									</consumable>
									<xsl:comment>G.k.2.2.EU.8: drugintendedname</xsl:comment>
				</xsl:if>												
						
					<xsl:for-each select="drugrelatedness">
							<xsl:if test="string-length(drugstartperiod) > 0">
								<outboundRelationship1 typeCode="SAS">
									<pauseQuantity value="{drugstartperiod}">
										<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="drugstartperiodunit"/></xsl:call-template></xsl:attribute>
									</pauseQuantity>
									<actReference classCode="ACT" moodCode="EVN">
										<id extension="RID1"/>
									</actReference>
								</outboundRelationship1>
							</xsl:if>
							<xsl:if test="string-length(druglastperiod)>0">
								<outboundRelationship1 typeCode="SAE">
									<pauseQuantity value="{druglastperiod}">
										<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="druglastperiodunit"/></xsl:call-template></xsl:attribute>
									</pauseQuantity>
									<actReference classCode="ACT" moodCode="EVN">
										<id extension="RID1"/>
									</actReference>
								</outboundRelationship1>
							</xsl:if> 
						</xsl:for-each> 
						<!-- B.4.k.4 Dosage -->
						<!-- <xsl:if test="string-length(drugstructuredosagenumb) > 0 or string-length(drugintervaldosageunitnumb) > 0 or string-length(drugdosagetext) > 0 or string-length(drugadministrationroute) > 0 or string-length(drugstartdate) > 0 or string-length(drugenddate) > 0"> -->
							<xsl:for-each select="drugdosage">
							<outboundRelationship2 typeCode="COMP">
								
								<substanceAdministration classCode="SBADM" moodCode="EVN">
									<xsl:comment> G.k.4.r.8: Dosage Text </xsl:comment>
									<xsl:if test="string-length(drugdosagetext) > 0">
										<text><xsl:value-of select="drugdosagetext"/></text>
									</xsl:if>
									<xsl:comment> G.k.4.r.6a: Duration of Drug Administration (number) </xsl:comment>
									<xsl:comment> G.k.4.r.6b: Duration of Drug Administration (unit) </xsl:comment>
									<!-- version 2.0 edit to fix SXPR_TS error  on missing PIVL_TS -->
									<xsl:if test="string-length(drugintervaldosageunitnumb) > 0 or string-length(drugstartdate) > 0 or string-length(drugenddate) > 0 or string-length(drugtreatmentduration ) > 0 ">
										<effectiveTime xsi:type="SXPR_TS">
										<xsl:choose>
											<xsl:when test="string-length(drugintervaldosageunitnumb) > 0">
												<comp xsi:type="PIVL_TS">
													<period value="{drugintervaldosageunitnumb}">
														<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="drugintervaldosagedefinition"/></xsl:call-template></xsl:attribute>
													</period>
												</comp>
											</xsl:when>
											<xsl:otherwise>
											<comp xsi:type="PIVL_TS">
													<period nullFlavor="NI"/>
											</comp>
											</xsl:otherwise>
											</xsl:choose>
											<xsl:choose>
												<xsl:when test="string-length(drugstartdate) = 0 or string-length(drugenddate) = 0 or string-length(drugtreatmentduration) = 0">
													<comp xsi:type="IVL_TS" operator="A">
														<xsl:if test="string-length(drugstartdate) > 0">
															<low value="{drugstartdate}"/>
														</xsl:if>
														<xsl:if test="string-length(drugtreatmentduration) > 0 and string-length(drugenddate) = 0">
															<width value="{drugtreatmentduration}">
																<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="drugtreatmentdurationunit"/></xsl:call-template></xsl:attribute>
															</width>
														</xsl:if>
														<xsl:if test="string-length(drugenddate) > 0">
															<high value="{drugenddate}"/>
														</xsl:if>
													</comp>
												</xsl:when>
												<xsl:otherwise>
													<comp xsi:type="IVL_TS" operator="A">
														<low value="{drugstartdate}"/>
														<high value="{drugenddate}"/>
													</comp>
													<comp xsi:type="IVL_TS" operator="A">
														<width value="{drugtreatmentduration}">
															<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="drugtreatmentdurationunit"/></xsl:call-template></xsl:attribute>
														</width>
													</comp>
												</xsl:otherwise>
											</xsl:choose>
										</effectiveTime>
									</xsl:if>
									<xsl:comment> G.k.4.r.10.1: Route of Administration (free text) </xsl:comment>
									<xsl:comment> G.k.4.r.10.2a: Route of Administration TermID Version Date / Number </xsl:comment>
									<xsl:comment> G.k.4.r.10.2b: Route of Administration TermID </xsl:comment>
									<xsl:if test="string-length(drugadministrationroute) > 0">
										<routeCode code="{drugadministrationroute}" codeSystem="{$oidICHRoute}" codeSystemVersion="{$oidICHRouteCSV}"/>
									</xsl:if>
									<xsl:comment> G.k.4.r.1a Dose (number) </xsl:comment>
									<xsl:comment> G.k.4.r.1b: Dose (unit) </xsl:comment>
									<xsl:if test="string-length(drugstructuredosagenumb) > 0 or string-length(drugstructuredosagenumbunit) > 0">
										<doseQuantity>
											<xsl:choose>
												<xsl:when test="string-length(drugseparatedosagenumb) > 0">
													<xsl:attribute name="value"><xsl:value-of select="drugstructuredosagenumb * drugseparatedosagenumb"/></xsl:attribute>
												</xsl:when>
												<xsl:otherwise>
													<xsl:attribute name="value"><xsl:value-of select="drugstructuredosagenumb"/></xsl:attribute>
												</xsl:otherwise>
											</xsl:choose>
											<xsl:attribute name="unit"><xsl:value-of select="drugstructuredosageunit"/></xsl:attribute>
										</doseQuantity>
									</xsl:if>
									<xsl:comment> G.k.4.r.7: Batch / Lot Number </xsl:comment>
									<xsl:if test="string-length(drugbatchnumb) > 0 or string-length(drugdosageform) > 0">
										<consumable typeCode="CSM">
											<instanceOfKind classCode="INST">
												<productInstanceInstance classCode="MMAT" determinerCode="INSTANCE">
													<id nullFlavor="NI"/>
													<lotNumberText><xsl:value-of select="drugbatchnumb"/></lotNumberText>
												</productInstanceInstance>
												<kindOfProduct classCode="MMAT" determinerCode="KIND">
													<formCode codeSystem="TBD-DoseForm">
													<xsl:attribute name="codeSystemVersion"><xsl:value-of select="drugdosageformtermidversion"/></xsl:attribute>
													<xsl:attribute name="code"><xsl:value-of select="drugdosageformtermid"/></xsl:attribute>
														<originalText>
															<xsl:value-of select="drugdosageform"/>
														</originalText>
													</formCode>
												</kindOfProduct>
											</instanceOfKind>
										</consumable>
									</xsl:if>
									<xsl:comment> G.k.4.r.11.1: Parent Route of Administration (free text) </xsl:comment>
									<xsl:comment> G.k.4.r.11.2a: Parent Route of Administration TermID Version Date / Number </xsl:comment>
									<xsl:comment> G.k.4.r.11.2b: Parent Route of Administration TermID </xsl:comment>
									<xsl:if test="string-length(drugparadministration) > 0">
										<inboundRelationship typeCode="REFR">
											<observation moodCode="EVN" classCode="OBS">
												<code code="{$ParentRouteOfAdministration}" codeSystem="{$oidObservationCode}"/>
												<value xsi:type="CE" code="{drugparadministration}" codeSystem="{$oidICHRoute}"/>
											</observation>
										</inboundRelationship>
									</xsl:if>
								</substanceAdministration>
								
							</outboundRelationship2>
							</xsl:for-each>
						<!-- </xsl:if> -->
						<xsl:comment> G.k.5a: Cumulative Dose to First Reaction (number) </xsl:comment>
						<xsl:comment> G.k.5b: Cumulative Dose to First Reaction (unit) </xsl:comment>
						<xsl:if test="string-length(drugcumulativedosagenumb) > 0">
							<outboundRelationship2 typeCode="SUMM">
								<observation moodCode="EVN" classCode="OBS">
									<code code="{$CumulativeDoseToReaction}" codeSystem="{$oidObservationCode}"/>
									<value xsi:type="PQ" value="{drugcumulativedosagenumb}">
										<xsl:attribute name="unit">
											<xsl:choose>
												<xsl:when test="string-length(drugcumulativedosageunit) > 0"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="drugcumulativedosageunit"/></xsl:call-template></xsl:when>
												<xsl:otherwise>1</xsl:otherwise>
											</xsl:choose>
										</xsl:attribute>
									</value>
								</observation>
							</outboundRelationship2>
						</xsl:if>
						<xsl:comment> G.k.6a: Gestation Period at Time of Exposure (number) </xsl:comment>
						<xsl:comment> G.k.6b: Gestation Period at Time of Exposure (unit) </xsl:comment>
						<xsl:if test="string-length(reactiongestationperiod) > 0">
							<outboundRelationship2 typeCode="PERT">
								<observation moodCode="EVN" classCode="OBS">
									<code code="{$GestationPeriod}" codeSystem="{$oidObservationCode}"/>
									<value xsi:type="PQ" value="{reactiongestationperiod}">
										<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="reactiongestationperiodunit"/></xsl:call-template></xsl:attribute>
									</value>
								</observation>
							</outboundRelationship2>
						</xsl:if>
						
						<xsl:comment> G.k.10.r: Additional Information on Drug (coded)(repeat as necessary) </xsl:comment>
						<xsl:if test="string-length(drugadditional) > 0">
							<outboundRelationship2 typeCode="REFR">
								<observation moodCode="EVN" classCode="OBS">
									<code code="{$AdditionalInformation}" codeSystem="{$oidObservationCode}"/>
									<value xsi:type="ST"><xsl:value-of select="drugadditional"/></value>
								</observation>
							</outboundRelationship2>
							<xsl:apply-templates select="drugadditionalstructured"/>		
						</xsl:if>
						<xsl:comment> G.k.9.i.4: Did Reaction Recur on Re-administration? Drug, Reaction </xsl:comment>
						<xsl:apply-templates select="drugrelatedness" mode="recur"/>
						<xsl:comment> G.k.7.r.1: Indication as Reported by the Primary Source </xsl:comment>
						<xsl:for-each select="drugindications">
							<inboundRelationship typeCode="RSON">
								
								<observation moodCode="EVN" classCode="OBS">
																	<code code="{$Indication}" codeSystem="{$oidObservationCode}"/>
																	<value xsi:type="CE">
																		<xsl:if test="string-length(drugindicationmeddraversion) > 0">
																			<xsl:attribute name="codeSystem"><xsl:value-of select="$oidMedDRA"/></xsl:attribute>
																			<xsl:attribute name="codeSystemVersion"><xsl:value-of select="drugindicationmeddraversion"/></xsl:attribute>
																		</xsl:if>
																		
																			<xsl:if test="number(drugindication)">
																				<xsl:attribute name="code"><xsl:value-of select="drugindication"/></xsl:attribute>
																			
																				<originalText>
																					<xsl:value-of select="drugindicationterm"/>
					
																				</originalText>
																			</xsl:if>
																		
																	</value>
																	<performer typeCode="PRF">
																		<assignedEntity classCode="ASSIGNED">
																			<code code="{$SourceReporter}" codeSystem="{$oidAssignedEntityRoleCode}"/>
																		</assignedEntity>
																	</performer>
																</observation>
							
							</inboundRelationship>
						</xsl:for-each>
						<xsl:comment> G.k.8: Action(s) Taken with Drug </xsl:comment>
						<xsl:if test="string-length(actiondrug) > 0">
							<inboundRelationship typeCode="CAUS">
								<act classCode="ACT" moodCode="EVN">
									<xsl:choose>
										<xsl:when test="actiondrug = 5"><code code="0" codeSystem="{$oidActionTaken}" codeSystemVersion="{$oidActionTakenCSV}"/></xsl:when>
										<xsl:when test="actiondrug = 6"><code code="9" codeSystem="{$oidActionTaken}" codeSystemVersion="{$oidActionTakenCSV}"/></xsl:when>
										<xsl:otherwise><code code="{actiondrug}" codeSystem="{$oidActionTaken}" codeSystemVersion="{actiondrugcsv}"/></xsl:otherwise>
									</xsl:choose>
								</act>
							</inboundRelationship>
						</xsl:if>
					</substanceAdministration>
				</component>
	</xsl:template>
	
	
	
	<xsl:template match="ingredientdetail" mode="main">
		<component typeCode="COMP">
			<substanceAdministration moodCode="EVN" classCode="SBADM">
				<consumable typeCode="CSM">
					<instanceOfKind classCode="INST">
						<kindOfProduct classCode="MMAT" determinerCode="KIND">
						
						
						<asManufacturedProduct classCode="MANU">
							<xsl:if test="string-length(newdrugclassification) > 0">
								<subjectOf typeCode="SBJ">
									<characteristic classCode="OBS" moodCode="EVN">
										<code code="1" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.10"></code>
										<value xsi:type="CE" codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.3" code="{newdrugclassification}" codeSystemVersion="{newdrugclassificationcsv}"/>
									</characteristic>
								</subjectOf>
							</xsl:if>
							
							<xsl:if test="string-length(otcclassification) > 0">
								<subjectOf typeCode="SBJ">
									<approval classCode="CNTRCT" moodCode="EVN">
									<pertinentInformation typeCode="PERT">
									<policy classCode="POLICY" moodCode="EVN">
									
									<code codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.4" code="{otcclassification}" codeSystemVersion="{otcclassificationcsv}"/>
									
									</policy>
									</pertinentInformation>
									</approval>
								</subjectOf>
							</xsl:if>
						</asManufacturedProduct>
						
						
						
						<asNamedEntity>
						<name xml:lang="ja">
						(jpndrugnickname)
						</name>
						</asNamedEntity>
						<formCode codeSystem="2.16.840.1.113883.3.989 .5.1.3.2.1.13" code="{jpnproducttype}" codeSystemVersion="{jpnproducttypecsv}">
						</formCode>
						<ingredient classCode="ACTI">
						<ingredientSubstance classCode="MMAT" determinerCode="KIND">
						<asSpecializedKind classCode="GEN">
						<generalizedMaterialKind classCode="MAT" determinerCode="KIND">
						<code codeSystem="2.16.840.1.113883.3.989.5.1.3.2.1.17" code="{typeofingredient}" codeSystemVersion="{typeofingredientcsv}">
						</code>
						</generalizedMaterialKind>
						</asSpecializedKind>
						</ingredientSubstance>
						</ingredient>
								
								
								
						</kindOfProduct>
									
					</instanceOfKind>
				</consumable>
			</substanceAdministration>				
		</component>					
	</xsl:template>
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	<!-- Active Substance : 
	E2B(R2): element "activesubstance"
	E2B(R3): element "drugInformation"
	-->
	<xsl:template match="activesubstance">
		<xsl:comment> G.k.2.3.r.1: Substance / Specified Substance Name </xsl:comment>
		<ingredient classCode="ACTI">
			<quantity>
			
			<numerator value="{substancestrength}" unit="{substancestrengthunit}"/>
			<denominator value="1"/>
			</quantity>
			<ingredientSubstance classCode="MMAT" determinerCode="KIND">
				<name><xsl:value-of select="activesubstancename"/></name>
				<xsl:if test="string-length(activesubstancetermid) > 0">
					<code codeSystem="TBD-Substance" code="{activesubstancetermid}" codeSystemVersion="{activesubstancetermidversion}"/> 
				</xsl:if>
				
			</ingredientSubstance>
		</ingredient>
	</xsl:template>	
	
	<!-- Did Recur on Readministration: 
	E2B(R2): element "recur"
	E2B(R3): element ""
	-->
	<xsl:template match="drugrelatedness" mode="recur">
	
		
			<outboundRelationship2 typeCode="PERT">
				<observation moodCode="EVN" classCode="OBS">
					<code code="{$RecurranceOfReaction}" codeSystem="{$oidObservationCode}"/>
					<xsl:choose>
						<xsl:when test="drugrecurreadministration = 1"><value xsi:type="CE" code="1" codeSystem="{$oidRechallenge}" codeSystemVersion="{$oidRechallengeCSV}"/></xsl:when>
						<xsl:when test="drugrecurreadministration = 2"><value xsi:type="CE" code="2" codeSystem="{$oidRechallenge}" codeSystemVersion="{$oidRechallengeCSV}"/></xsl:when>
						<xsl:otherwise><value xsi:type="CE" code="{drugrecurreadministration}" codeSystem="{$oidRechallenge}" codeSystemVersion="{drugrecurreadministrationcsv}"/></xsl:otherwise>
					</xsl:choose>
					<xsl:variable name="reaction" select="drugrecuraction"/>
					<xsl:variable name="rid">
						<xsl:for-each select="../../reaction">
							<xsl:if test="reactionmeddrallt = $reaction or primarysourcereaction = $reaction">RID<xsl:value-of select="position()"/></xsl:if>
						</xsl:for-each>
					</xsl:variable>
					<xsl:if test="string-length($rid) > 0">
						<outboundRelationship1 typeCode="REFR">
							<actReference moodCode="EVN" classCode="OBS">
								<id root="{$rid}"/>
							</actReference>
						</outboundRelationship1>
					</xsl:if>
				</observation>
			</outboundRelationship2>
		
	</xsl:template>
	
		<!-- Drug (causality): 
	E2B(R2): element "drug"
	E2B(R3): element "causalityAssessment"
	-->
	<xsl:template match="drug" mode="causality">
		<xsl:comment> G.k.1: Characterisation of Drug Role Drug </xsl:comment>
		<xsl:if test="string-length(drugcharacterization)>0">
			<component typeCode="COMP">
				<causalityAssessment classCode="OBS" moodCode="EVN">
					<code code="{$InterventionCharacterization}" codeSystem="{$oidObservationCode}"/>
					<value xsi:type="CE" code="{drugcharacterization}" codeSystem="{$oidDrugRole}" codeSystemVersion="{drugcharacterizationcsv}"/>
					<!-- Reference to Drug -->
					<subject2 typeCode="SUBJ">
						<productUseReference classCode="SBADM" moodCode="EVN">
							<id root="DID{position()}"/>
						</productUseReference>
					</subject2>
				</causalityAssessment>
			</component>
		</xsl:if>
		<xsl:comment> G.k.9.i.2.r Drug Reaction Matrix </xsl:comment>
		<xsl:variable name="pos" select="position()" />
		<xsl:apply-templates select="drugrelatedness">
			<xsl:with-param name="pos" select="$pos" />
		</xsl:apply-templates>
		
	</xsl:template>
	
	<xsl:template match="drugadditionalstructured">
	<outboundRelationship2 typeCode="REFR">
			<observation moodCode="EVN" classCode="OBS">
				<code code="{$CodedDrugInformation}" codeSystem="{$oidObservationCode}"/>
				<xsl:for-each select=".">
				
				<value xsi:type="CE" codeSystem="{$AdditionalInformationOnDrug}" code="{drugadditionalcode}" codeSystemVersion="{drugadditionalcodecsv}"><xsl:value-of select="drugadditional"/></value>
				
				</xsl:for-each>
				
			</observation>
	</outboundRelationship2>		
	</xsl:template>
	
	<!-- Drug Reaction Matrix : 
	E2B(R2): element "drugrelatedness"
	E2B(R3): element ""
	-->
	<xsl:template match="drugrelatedness">
	<xsl:param name="pos"/>
	<xsl:for-each select="drugassesment">
	
	<xsl:choose>
	<!-- See if R2 EU specific drug assessment method is present, if not perform regular free text conversion-->
	
		<xsl:when test="drugeuassessmentmethod= 'EVCTM'">
			<component typeCode="COMP">
						<causalityAssessment classCode="OBS" moodCode="EVN">
							<code code="{$Causality}" codeSystem="{$oidObservationCode}" displayName="causality"/>
							<xsl:comment> G.k.9.i.2.r.3.EU.1: Result of Assessment - captured in the code field  </xsl:comment>
							<xsl:if test="string-length(drugresult) > 0">
								<value xsi:type="CE" code="{drugeuresult}" codeSystem="{$oidEUResultofAssessment}" codeSystemVersion="{$oidEUResultofAssessmentCSV}" />
							</xsl:if>
							<xsl:comment> G.k.9.i.2.r.2.EU.1: EU Method of assessment - captured in the code field as value 1 </xsl:comment>
								<methodCode code="1" codeSystem="{$oidEUMethodofAssessment}" codeSystemVersion="{$oidEUMethodofAssessmentCSV}" />
							<xsl:if test="string-length(drugeuassessmentsource) > 0">
								<author typeCode="AUT">
									<assignedEntity classCode="ASSIGNED">
										<code code="{drugeuassessmentsource}" codeSystem="{$oidEUSourceofAssessment}" codeSystemVersion="{$oidEUMethodofAssessmentCSV}" />
									<xsl:comment> G.k.9.i.2.r.1.EU.1: EU Source of assessment - captured in the code field </xsl:comment>					
									</assignedEntity>
								</author>
							</xsl:if>
												<!-- Reference to Reaction, if a match is found -->
							<xsl:variable name="reaction" select="drugreactionasses"/>
							<xsl:if test="count(../../reaction[reactionmeddrallt = $reaction or primarysourcereaction = $reaction]) > 0">
								<subject1 typeCode="SUBJ">
									<adverseEffectReference classCode="OBS" moodCode="EVN">
										<xsl:variable name="rid">
											<xsl:for-each select="../../reaction">
												<xsl:if test="reactionmeddrallt = $reaction or primarysourcereaction = $reaction">RID<xsl:value-of select="position()"/></xsl:if>
											</xsl:for-each>
										</xsl:variable>
										<id root="{$rid}"/>
									</adverseEffectReference>
								</subject1>
							</xsl:if>
							<!-- Reference to Drug -->
							<subject2 typeCode="SUBJ">
								<productUseReference classCode="SBADM" moodCode="EVN">
<!--									<xsl:variable name="drug" select="generate-id(..)"/>
									<xsl:variable name="did">
										<xsl:for-each select="../../drug">
											<xsl:if test="generate-id(.) = $drug">DID<xsl:value-of select="position()"/></xsl:if>
										</xsl:for-each>
									</xsl:variable>-->
										<xsl:variable name="did">DID<xsl:value-of select="$pos" /></xsl:variable>
									<id root="{$did}"/>
								</productUseReference>
							</subject2>
						</causalityAssessment>
				</component>
			</xsl:when>	
		<xsl:otherwise>		
			<xsl:if test="string-length(drugassessmentsource) + string-length(drugassessmentmethod) + string-length(drugresult) > 0">
				<component typeCode="COMP">
					<causalityAssessment classCode="OBS" moodCode="EVN">
						<code code="{$Causality}" codeSystem="{$oidObservationCode}"/>
						<!-- B.4.k.9.i.2.r.3 Assessment Result -->
						<xsl:comment> G.k.9.i.2.r.3: Result of Assessment Drug, Reaction, Assessment </xsl:comment> 
						<xsl:if test="string-length(drugresult) > 0">
							<value xsi:type="ST"><xsl:value-of select="drugresult"/></value>
						</xsl:if>
						<!-- B.4.k.9.i.2.r.2 Assessment Method -->
						<xsl:comment> G.k.9.i.2.r.2: Method of Assessment Drug, Reaction, Assessment </xsl:comment> 
						<xsl:if test="string-length(drugassessmentmethod) > 0">
							<methodCode>
								<originalText><xsl:value-of select="drugassessmentmethod"/></originalText>
							</methodCode>
						</xsl:if>
						<xsl:if test="string-length(drugassessmentsource) > 0">
							<author typeCode="AUT">
								<assignedEntity classCode="ASSIGNED">
									<code>
										<!-- B.4.k.9.i.2.r.1 Assessment Source -->
										<xsl:comment> G.k.9.i.2.r.1: Source of Assessment Drug, Reaction, Assessment </xsl:comment> 
										<originalText><xsl:value-of select="drugassessmentsource"/></originalText>
									</code>
								</assignedEntity>
							</author>
						</xsl:if>
						<!-- Reference to Reaction, if a match is found -->
						<xsl:variable name="reaction" select="drugreactionasses"/>
						<xsl:if test="count(../../reaction[reactionmeddrallt = $reaction or primarysourcereaction = $reaction]) > 0">
							<subject1 typeCode="SUBJ">
								<adverseEffectReference classCode="OBS" moodCode="EVN">
									<xsl:variable name="rid">
										<xsl:for-each select="../../reaction">
											<xsl:if test="reactionmeddrallt = $reaction or primarysourcereaction = $reaction">RID<xsl:value-of select="position()"/></xsl:if>
										</xsl:for-each>
									</xsl:variable>
									<id root="{$rid}"/>
								</adverseEffectReference>
							</subject1>
						</xsl:if>
						<!-- Reference to Drug -->
						<subject2 typeCode="SUBJ">
							<productUseReference classCode="SBADM" moodCode="EVN">
							<!--	<xsl:variable name="drug" select="generate-id(..)"/>
								<xsl:variable name="did">
									<xsl:for-each select="../../drug">
										<xsl:if test="generate-id(.) = $drug">DID<xsl:value-of select="position()"/></xsl:if>
									</xsl:for-each>
								</xsl:variable>-->
								<xsl:variable name="did">DID<xsl:value-of select="$pos" /></xsl:variable>
								<id root="{$did}"/>
							</productUseReference>
						</subject2>
					</causalityAssessment>
				</component>
			</xsl:if>
		</xsl:otherwise>
		</xsl:choose>
		</xsl:for-each>
		</xsl:template>	
		<xsl:template match="summary">
		<!-- B.5.2 Reporter's Comments -->
		<xsl:comment>H.2: Reporter's Comments</xsl:comment>
		<xsl:if test="string-length(reportercomment) > 0">
			<component1 typeCode="COMP">
				<observationEvent moodCode="EVN" classCode="OBS">
					<code code="{$Comment}" codeSystem="{$oidObservationCode}"/>
					<value xsi:type="ED" mediaType="text/plain"><xsl:value-of select="reportercomment"/></value>
					<author typeCode="AUT">
						<assignedEntity classCode="ASSIGNED">
							<code code="{$SourceReporter}" codeSystem="{$oidAssignedEntityRoleCode}"/>
						</assignedEntity>
					</author>
				</observationEvent>
			</component1>
		</xsl:if>
		<!-- B.5.3.r Sender's diagnosis/syndrome code -->
		<xsl:comment>H.3.r.1a: MedDRA Version for Sender's Diagnosis / Syndrome and / or Reclassification of Reaction / Event</xsl:comment>
		<xsl:comment>H.3.r.1b: Sender's Diagnosis / Syndrome and / or Reclassification of Reaction / Event  (MedDRA code)</xsl:comment>
		<xsl:if test="string-length(senderdiagnosis) > 0">
			<component1 typeCode="COMP">
				<observationEvent moodCode="EVN" classCode="OBS">
					<code code="{$Diagnosis}" codeSystem="{$oidObservationCode}"/>
					<xsl:variable name="isMeddraCode">
						<xsl:call-template name="isMeddraCode">
							<xsl:with-param name="code" select="senderdiagnosis"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:choose>
						<xsl:when test="$isMeddraCode = 'yes'">
							<value xsi:type="CE" code="{senderdiagnosis}">
								<xsl:if test="string-length(senderdiagnosismeddraversion) > 0">
									<xsl:attribute name="codeSystem"><xsl:value-of select="$oidMedDRA"/></xsl:attribute>
									<xsl:attribute name="codeSystemVersion"><xsl:value-of select="senderdiagnosismeddraversion"/></xsl:attribute>
								</xsl:if>
							</value>
						</xsl:when>
						<xsl:otherwise>
							<value xsi:type="CE" >
								<originalText>
									<xsl:value-of select="senderdiagnosis"/>
									<xsl:if test="string-length(senderdiagnosismeddraversion) > 0"> (<xsl:value-of select="senderdiagnosismeddraversion"/>)</xsl:if>
								</originalText>
							</value>
						</xsl:otherwise>
					</xsl:choose>
					<author typeCode="AUT">
						<assignedEntity classCode="ASSIGNED">
							<code code="{$Sender}" codeSystem="{$oidAssignedEntityRoleCode}"/>
						</assignedEntity>
					</author>
				</observationEvent>
			</component1>
		</xsl:if>
		<!-- B.5.4 Sender's Comments -->
		<xsl:comment>H.4: Sender's Comments</xsl:comment>
		<xsl:if test="string-length(sendercomment) > 0">
			<component1 typeCode="COMP">
				<observationEvent moodCode="EVN" classCode="OBS">
					<code code="{$Comment}" codeSystem="{$oidObservationCode}"/>
					<value xsi:type="ED" mediaType="text/plain"><xsl:value-of select="sendercomment"/></value>
					<author typeCode="AUT">
						<assignedEntity classCode="ASSIGNED">
							<code code="{$Sender}" codeSystem="{$oidAssignedEntityRoleCode}"/>
						</assignedEntity>
					</author>
				</observationEvent>
			</component1>
		</xsl:if>
		
	
		
	</xsl:template>
	
	<xsl:template match="pmdareceiverinfo">
			<xsl:if test="string-length(receiverorganization)>0">
				<subjectOf1 typeCode="SUBJ">
					<controlActEvent classCode="CACT" moodCode="EVN">
						<primaryInformationRecipient typeCode="PRCP">
						<assignedEntity classCode="ASSIGNED">
						<representedOrganization classCode="ORG" determinerCode="INSTANCE">
						<name>
						(receiverorganization)
						</name>
						</representedOrganization>
						
						<assignedPerson classCode="PSN" determinerCode="INSTANCE">
						<name>
						<prefix>
						(receivertitle)
						</prefix>
						<family>
						(receiverfirstname)
						</family>
						<given>
						(receiverlastname)
						</given>
						</name>
						</assignedPerson>
						</assignedEntity>
						</primaryInformationRecipient>
						</controlActEvent>
				</subjectOf1>
			</xsl:if>
			
	</xsl:template>
	
</xsl:stylesheet>