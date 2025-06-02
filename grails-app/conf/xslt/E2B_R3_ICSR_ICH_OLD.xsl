<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="urn:hl7-org:v3" xmlns:mif="urn:hl7-org:v3/mif" xmlns:xalan="http://xml.apache.org/xslt" >

	<xsl:output indent="yes" method="xml" omit-xml-declaration="no" encoding="utf-8" xalan:indent-amount="2"/>
	<xsl:strip-space elements="*"/>
	<!--ICH ICSR : conversion of the main structure incl. root element and controlActProcess
E2B(R2): root element "ichicsr"
E2B(R3): root element "PORR_IN049016UV"
-->
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

	<xsl:template match="safetyreport" mode="report">
		<PORR_IN049016UV>
			<!--M.2.r.4-MessageNumber-->
			<xsl:variable name="tempVariable">
				<xsl:value-of select="position()"/>
			</xsl:variable>

			<xsl:apply-templates mode="part-b" select="../ichicsrmessageheader[number($tempVariable)]"/>
			<controlActProcess classCode="CACT" moodCode="EVN">
				<code code="PORR_TE049016UV" codeSystem="2.16.840.1.113883.1.18"/>
				<!--A.1.3 - Date of Transmission-->
				<effectiveTime value="{creationdate}"/>
				<xsl:comment>C.1.2:Date of Creation</xsl:comment>
				<xsl:apply-templates select="transmissiondate"/>
				<!--A.1.x - Safety Report-->
				<xsl:apply-templates select="." mode="main"/>
			</controlActProcess>
		</PORR_IN049016UV>
	</xsl:template>
	<xsl:variable name="OidISOCountry">1.0.3166.1.2.2</xsl:variable>
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
	<!--B.3 - Test-->
	<!--MedDRA version for Tests change this value to the correct version of MedDRA being used-->
	<xsl:variable name="testResMedDRAver">20.0</xsl:variable>
	<!--B.3-->
	<!--Use Recoded EudraVigilance product and substance names instead of verbatim term-->
	<!--set value to 1 to use EV recoding-->
	<xsl:variable name="XEVMPD"></xsl:variable>
	<!--Use N.2.r.2 to populate A.3.2.1 for NCAs to preserve original sending organisation-->
	<!--set value to 1 to enable it-->
	<xsl:variable name="NCAREROUTE"></xsl:variable>
	<!--EV product-->
	<!--M.1 Fields - Batch Wrapper-->
	<!--M.1.1-->
	<xsl:variable name="oidMessageType">2.16.840.1.113883.3.989.2.1.1.1</xsl:variable>
	<xsl:variable name="oidMessageTypeCSV">2.0</xsl:variable>
	<!--M.1.4-->
	<xsl:variable name="oidBatchNumber">2.16.840.1.113883.3.989.2.1.3.22</xsl:variable>
	<!--M.1.5-->
	<xsl:variable name="oidBatchSenderId">2.16.840.1.113883.3.989.2.1.3.13</xsl:variable>
	<!--M.1.6-->
	<xsl:variable name="oidBatchReceiverId">2.16.840.1.113883.3.989.2.1.3.14</xsl:variable>
	<!--M.2 Fields - Message Wrapper-->
	<!--M.2.r.4-->
	<xsl:variable name="oidMessageNumber">2.16.840.1.113883.3.989.2.1.3.1</xsl:variable>
	<!--M.2.r.5-->
	<xsl:variable name="oidMessageSenderId">2.16.840.1.113883.3.989.2.1.3.11</xsl:variable>
	<!--M.2.r.6-->
	<xsl:variable name="oidMessageReceiverId">2.16.840.1.113883.3.989.2.1.3.12</xsl:variable>
	<!--A.1 Fields - Case Safety Report-->
	<!--A.1.0.1-->
	<xsl:variable name="oidSendersReportNamespace">2.16.840.1.113883.3.989.2.1.3.1</xsl:variable>
	<!--A.1.4-->
	<xsl:variable name="ReportType">1</xsl:variable>
	<xsl:variable name="oidReportType">2.16.840.1.113883.3.989.2.1.1.2</xsl:variable>
	<xsl:variable name="oidReportTypeCSV">2.0</xsl:variable>
	<xsl:variable name="qualificationcsv">2.0</xsl:variable>
	<!--A.1.8-->
	<xsl:variable name="AdditionalDocumentsAvailable">1</xsl:variable>
	<!--A.1.9-->
	<xsl:variable name="LocalCriteriaForExpedited">23</xsl:variable>
	<!--A.1.10.1/12-->

	<xsl:variable name="MPIDCSV">2.16.840.1.113883.6.69</xsl:variable>
	<!--G.k.2.1.1b: Medicinal Product Identifier (MPID)-->

	<xsl:variable name="oidWorldWideCaseID">2.16.840.1.113883.3.989.2.1.3.2</xsl:variable>
	<!--A.1.10.2-->
	<xsl:variable name="InitialReport">1</xsl:variable>
	<xsl:variable name="oidFirstSender">2.16.840.1.113883.3.989.2.1.1.3</xsl:variable>
	<xsl:variable name="oidFirstSenderCSV">2.0</xsl:variable>
	<!--A.1.11-->
	<xsl:variable name="OtherCaseIDs">2</xsl:variable>
	<!--A.1.11.r.2-->
	<xsl:variable name="oidCaseIdentifier">2.16.840.1.113883.3.989.2.1.3.3</xsl:variable>
	<!--A.1.13-->
	<xsl:variable name="NullificationAmendmentCode">3</xsl:variable>
	<xsl:variable name="oidNullificationAmendment">2.16.840.1.113883.3.989.2.1.1.5</xsl:variable>
	<xsl:variable name="oidNullificationAmendmentCSV">2.0</xsl:variable>
	<!--A.1.13.1-->
	<xsl:variable name="NullificationAmendmentReason">4</xsl:variable>
	<!--A.2 - Primary Source-->
	<!--A.2-->
	<xsl:variable name="SourceReport">2</xsl:variable>
	<!--A.2.r.1.4-->
	<xsl:variable name="oidQualification">2.16.840.1.113883.3.989.2.1.1.6</xsl:variable>
	<xsl:variable name="oidQualificationCSV">2.0</xsl:variable>
	<!--A.3 - Sender-->
	<!--A.3.1-->
	<xsl:variable name="oidSenderType">2.16.840.1.113883.3.989.2.1.1.7</xsl:variable>
	<xsl:variable name="oidSenderTypeCSV">2.0</xsl:variable>
	<!--A.5 - Study Identification-->
	<!--A.5-->
	<xsl:variable name="SponsorStudyNumber">2.16.840.1.113883.3.989.2.1.3.5</xsl:variable>
	<!--A.5.1.r.1-->
	<xsl:variable name="StudyRegistrationNumber">2.16.840.1.113883.3.989.2.1.3.6</xsl:variable>
	<!--A.5.4-->
	<xsl:variable name="oidStudyType">2.16.840.1.113883.3.989.2.1.1.8</xsl:variable>
	<xsl:variable name="oidStudyTypeCSV">2.0</xsl:variable>
	<!--B.1 / B.1.10 - Patient / Parent-->
	<xsl:variable name="oidSourceMedicalRecord">2.16.840.1.113883.3.989.2.1.1.4</xsl:variable>
	<!--B.1.1.1a-->
	<xsl:variable name="GPMrn">1</xsl:variable>
	<xsl:variable name="oidGPMedicalRecordNumber">2.16.840.1.113883.3.989.2.1.3.7</xsl:variable>
	<!--B.1.1.1b-->
	<xsl:variable name="SpecialistMrn">2</xsl:variable>
	<xsl:variable name="oidSpecialistRecordNumber">2.16.840.1.113883.3.989.2.1.3.8</xsl:variable>
	<!--B.1.1.1c-->
	<xsl:variable name="HospitalMrn">3</xsl:variable>
	<xsl:variable name="oidHospitalRecordNumber">2.16.840.1.113883.3.989.2.1.3.9</xsl:variable>
	<!--B.1.1.1d-->
	<xsl:variable name="Investigation">4</xsl:variable>
	<xsl:variable name="oidInvestigationNumber">2.16.840.1.113883.3.989.2.1.3.10</xsl:variable>
	<!--B.1.2.2-->
	<xsl:variable name="Age">3</xsl:variable>
	<!--B.1.2.2.1-->
	<xsl:variable name="GestationPeriod">16</xsl:variable>
	<!--B.1.2.3-->
	<xsl:variable name="AgeGroup">4</xsl:variable>
	<!--B.1.2.3-->
	<xsl:variable name="oidAgeGroup">2.16.840.1.113883.3.989.2.1.1.9</xsl:variable>
	<xsl:variable name="oidAgeGroupCSV">2.0</xsl:variable>
	<!--B.1.3-->
	<xsl:variable name="BodyWeight">7</xsl:variable>
	<!--B.1.4-->
	<xsl:variable name="Height">17</xsl:variable>
	<!--B.1.6-->
	<xsl:variable name="LastMenstrualPeriodDate">22</xsl:variable>
	<!--B.1.10-->
	<xsl:variable name="Parent">PRN</xsl:variable>
	<xsl:variable name="PastDrug">2</xsl:variable>
	<!--B.1.7 / B.1.10.7 - Medical History-->
	<!--B.1.7-->
	<xsl:variable name="RelevantMedicalHistoryAndConcurrentConditions">1</xsl:variable>
	<!--B.1.7.1.r.d-->
	<xsl:variable name="Continuing">13</xsl:variable>
	<!--B.1.7.1.r.g-->
	<xsl:variable name="Comment">10</xsl:variable>
	<!--B.1.7.2-->
	<xsl:variable name="HistoryAndConcurrentConditionText">18</xsl:variable>
	<!--B.1.7.3-->
	<xsl:variable name="ConcommitantTherapy">11</xsl:variable>
	<!--B.1.8 / B.1.10.8 - Drug History-->

	<!--B.1.8-->
	<xsl:variable name="DrugHistory">2</xsl:variable>
	<!--B.1.8.r.a1-->
	<xsl:variable name="MPID">MPID</xsl:variable>
	<!--B.1.8.r.a3-->
	<xsl:variable name="PhPID">PhPID</xsl:variable>
	<!--B.1.8.r.f.2-->
	<xsl:variable name="Indication">19</xsl:variable>
	<xsl:variable name="IndicationCSV">2.0</xsl:variable>
	<!--B.1.8.r.g.2-->
	<xsl:variable name="Reaction">29</xsl:variable>
	<!--B.1.9-->
	<!--B.1.9.2-->
	<xsl:variable name="ReportedCauseOfDeath">32</xsl:variable>
	<!--B.1.9.3-->
	<xsl:variable name="Autopsy">5</xsl:variable>
	<!--B.1.9.4-->
	<xsl:variable name="CauseOfDeath">8</xsl:variable>
	<!--B.2 - Reaction-->
	<!--B.2.i-->
	<xsl:variable name="oidInternalReferencesToReaction">oidInternalReferencesToReaction</xsl:variable>
	<!--B.2.i.0.b-->
	<xsl:variable name="ReactionForTranslation">30</xsl:variable>
	<!--B.2.i.2.1-->
	<xsl:variable name="TermHighlightedByReporter">37</xsl:variable>
	<xsl:variable name="oidTermHighlighted">2.16.840.1.113883.3.989.2.1.1.10</xsl:variable>
	<xsl:variable name="oidTermHighlightedCSV">2.0</xsl:variable>
	<!--B.2.i.2.2-->
	<xsl:variable name="FamilyHistory">38</xsl:variable>
	<xsl:variable name="ResultsInDeath">34</xsl:variable>
	<xsl:variable name="LifeThreatening">21</xsl:variable>
	<xsl:variable name="CausedProlongedHospitalisation">33</xsl:variable>
	<xsl:variable name="DisablingIncapaciting">35</xsl:variable>
	<xsl:variable name="CongenitalAnomalyBirthDefect">12</xsl:variable>
	<xsl:variable name="OtherMedicallyImportantCondition">26</xsl:variable>
	<!--B.2.i.6-->
	<xsl:variable name="Outcome">27</xsl:variable>
	<xsl:variable name="oidOutcome">2.16.840.1.113883.3.989.2.1.1.11</xsl:variable>
	<xsl:variable name="oidOutcomeCSV">2.0</xsl:variable>

	<xsl:variable name="TestsAndProceduresRelevantToTheInvestigation">3</xsl:variable>
	<!--B.3.r.4-->
	<xsl:variable name="MoreInformationAvailable">25</xsl:variable>
	<!--B.4 - Drug-->
	<!--B.4-->
	<xsl:variable name="DrugInformation">4</xsl:variable>
	<xsl:variable name="oidValueGroupingCode">2.16.840.1.113883.3.989.2.1.1.20</xsl:variable>
	<!--B.4.k.1-->
	<xsl:variable name="InterventionCharacterization">20</xsl:variable>
	<xsl:variable name="oidDrugRole">2.16.840.1.113883.3.989.2.1.1.13</xsl:variable>
	<xsl:variable name="oidDrugRoleCSV">2.0</xsl:variable>
	<!--B.4.k.2.4-->
	<xsl:variable name="RetailSupply">1</xsl:variable>
	<!--B.4.k.2.5-->
	<xsl:variable name="Blinded">6</xsl:variable>
	<xsl:variable name="BlindedCSV">2.0</xsl:variable>
	<!--B.4.k.3-->
	<xsl:variable name="oidAuthorisationNumber">2.16.840.1.113883.3.989.2.1.3.4</xsl:variable>
	<!--B.4.k.4.r.12/13-->
	<xsl:variable name="oidICHRoute">2.16.840.1.113883.3.989.2.1.1.14</xsl:variable>
	<xsl:variable name="oidICHRouteCSV">2.2</xsl:variable>
	<!--B.4.k.4.r.13.2-->
	<xsl:variable name="ParentRouteOfAdministration">28</xsl:variable>
	<!--B.4.k.5.1-->
	<xsl:variable name="CumulativeDoseToReaction">14</xsl:variable>
	<!--B.4.k.7-->
	<xsl:variable name="SourceReporter">3</xsl:variable>
	<!--B.4.k.8-->
	<xsl:variable name="oidActionTaken">2.16.840.1.113883.3.989.2.1.1.15</xsl:variable>
	<xsl:variable name="oidActionTakenCSV">2.0</xsl:variable>
	<!--B.4.k.9.i.2-->
	<xsl:variable name="Causality">39</xsl:variable>
	<!--B.4.k.9.i.4-->
	<xsl:variable name="RecurranceOfReaction">31</xsl:variable>
	<xsl:variable name="oidRechallenge">2.16.840.1.113883.3.989.2.1.1.16</xsl:variable>
	<xsl:variable name="oidRechallengeCSV">2.0</xsl:variable>
	<!--B.4.k.10-->
	<xsl:variable name="CodedDrugInformation">9</xsl:variable>
	<xsl:variable name="AdditionalCodedDrugInformation">2</xsl:variable>
	<xsl:variable name="AdditionalInformationOnDrug">2.16.840.1.113883.3.989.2.1.1.17</xsl:variable>
	<xsl:variable name="AdditionalInformationOnDrugCSV">2.0</xsl:variable>
	<!--A.1.8.2-->
	<xsl:variable name="documentsHeldBySender">1</xsl:variable>
	<xsl:variable name="literatureReference">2</xsl:variable>
	<xsl:variable name="oidichreferencesource">2.16.840.1.113883.3.989.2.1.1.27</xsl:variable>
	<xsl:variable name="emaObservationCLVersion">2.0</xsl:variable>

	<!-- C.1.8.2: ICSR CASE SOURCE-->
	<xsl:variable name="emaoidC182CLVersion">2.0</xsl:variable>
	<xsl:variable name="emaReportRelationCLVersion">2.0</xsl:variable>

	<!-- C.1.9.1: Duplicate-->
	<xsl:variable name="emaReportCharacterizationCLVersion">1.0</xsl:variable>

	<!-- C.1.11.1: Report Nullification / Amendment -->
	<xsl:variable name="NullificationAmendmentCode">3</xsl:variable>

	<!-- C.1.11.2: Reason for Nullification / Amendment -->
	<xsl:variable name="NullificationAmendmentReason">4</xsl:variable>

	<!-- C.2.r.4: Qualification-->
	<xsl:variable name="emaoidC2r4CLVersion">2.0</xsl:variable>

	<!-- C.5.4: Study type in which the reaction(s)/event(s) were observed-->
	<xsl:variable name="emaoidC54CLVersion">2.0</xsl:variable>

	<!-- C.4.r.1 Literature Reference -->
	<xsl:variable name="ichoidC4rCLVersion">2.16.840.1.113883.3.989.5.1.10.1.4</xsl:variable>

	<xsl:variable name="ZIPSTREAM_COMPRESSION_ALGORITHM">DF</xsl:variable>

	<!--D.7.1.r.6: Family History-->
	<xsl:variable name="emaoidD71r6CLVersion">2.0</xsl:variable>
	<xsl:variable name="emaValueGroupingCLVersion">2.0</xsl:variable>

	<!-- D.1.1.1 Patient Medical Record Number(s) and Source(s) of the Record Number (GP Medical Record Number) -->
	<xsl:variable name="emaSourceMedicalRecordCLVersion">2.0</xsl:variable>

	<!-- D.7.3: Concomitant Therapies-->
	<xsl:variable name="emaoidD73CLVersion">2.0</xsl:variable>
	<xsl:variable name="oidConcomitantTherapies">2.16.840.1.113883.3.989.2.1.1.19</xsl:variable>

	<!-- E.i.3.1: Term Highlighted by Reporter-->
	<xsl:variable name="emaoidEi31TermHighlightedCLVersion">2.0</xsl:variable>

	<!-- E.i.7: Outcome of Reaction / Event at the Time of Last Observation-->
	<xsl:variable name="emaoidEi7OutcomeCLVersion">2.0</xsl:variable>

	<!-- E.i.8: Medical Confirmation by Healthcare Professional -->
	<xsl:variable name="MedicalConfirmationByHP">24</xsl:variable>

	<!-- F.r.3.1: Test Result (code)-->
	<xsl:variable name="emaoidFr31CLVersion">2.0</xsl:variable>

	<!--F.r.3.1 Test Result (code)-->
	<xsl:variable name="oidTestResultCode">2.16.840.1.113883.3.989.2.1.1.12</xsl:variable>

	<!-- G.k.8: Action(s) Taken with Drug-->
	<xsl:variable name="emaoidGk8CLVersion">2.0</xsl:variable>

	<!-- Code List Version Variable for OID 2.16.840.1.113883.3.989.2.1.1.21 -->
	<xsl:variable name="emaoidAssignedEntityRoleCodeVersion">2.0</xsl:variable>

	<!-- G.k.10.r: Additional Information on Drug (coded)-->
	<xsl:variable name="emaoidGk10rCLVersion">2.0</xsl:variable>

	<!-- G.k.1: Characterization of Drug Role-->
	<xsl:variable name="emaoidGk1CLVersion">2.0</xsl:variable>

	<!-- E.i.7: Outcome of Reaction / Event at the Time of Last Observation -->
	<xsl:variable name="oidEUBatchMessageTypeN11">2.16.840.1.113883.3.989.5.1.1.5.1</xsl:variable>

	<!-- G.k.2.4: Identification of the Country Where the Drug Was Obtained-->
	<xsl:variable name="emaoidGk24CLVersion">2.0</xsl:variable>

	<!-- G.k.9.i.2.r.3.EU.1: Result of Assessment - captured in the code field  -->
	<xsl:variable name="oidEUResultOfAssessmentCode">2.16.840.1.113883.3.989.5.1.1.5.3</xsl:variable>

	<!-- G.k.9.i.2.r.2.EU.1: EU Method of assessment - captured in the code field -->
	<xsl:variable name="oidEUMethodOfAssessmentCode">2.16.840.1.113883.3.989.5.1.1.5.2</xsl:variable>

	<!-- G.k.9.i.2.r.1.EU.1: EU Source of assessment - captured in the code field-->
	<xsl:variable name="oidEUSourceOfAssessmentCode">2.16.840.1.113883.3.989.5.1.1.5.4</xsl:variable>

	<!-- G.k.9.i.4 Did Reaction Recur on Re-administration?-->
	<xsl:variable name="emaoidGk9i4CLVersion">2.0</xsl:variable>

	<!-- Korea Specific Fields -->
	<xsl:variable name="OIdKRDrugDomestic">2.16.840.1.113883.3.989.5.1.10.2.1</xsl:variable>

	<xsl:variable name="OIdKRDrugForeign">2.16.840.1.113883.6.294</xsl:variable>

	<xsl:variable name="oidKRQualification">2.16.840.1.113883.3.989.5.1.10.1.1</xsl:variable>

	<xsl:variable name="OidKRSenderType">2.16.840.1.113883.3.989.5.1.10.1.2</xsl:variable>

	<xsl:variable name="OidKRObserveStudyCode">2.16.840.1.113883.3.989.5.1.10.1.7</xsl:variable>

	<xsl:variable name="OidKRObservedStudy">2.16.840.1.113883.3.989.5.1.10.1.3</xsl:variable>

	<!-- G.k.9.i.2.r.3.KR.1: WHO-UMC Result of Assessment -->
	<xsl:variable name="oidMFDSWHOResult">2.16.840.1.113883.3.989.5.1.10.1.5</xsl:variable>

	<!-- G.k.9.i.2.r.3.KR.2: KRCT Result of Assessment -->
	<xsl:variable name="oidMFDSKRCTResult">2.16.840.1.113883.3.989.5.1.10.1.6</xsl:variable>

	<!-- G.k.9.i.2.r.2.KR.1: KR Method of Assessment -->
	<xsl:variable name="oidMFDSKRMethod">2.16.840.1.113883.3.989.5.1.10.1.4</xsl:variable>

	<!-- H.5.r.1a and  H.5.r.1b -->
	<xsl:variable name="ichoidAssignedEntityRoleCodeVersion">2.16.840.1.113883.3.989.5.1.10.1.4</xsl:variable>

	<xsl:variable name="ichObservationCLVersion">2.16.840.1.113883.3.989.5.1.10.1.4</xsl:variable>


	<!--Country E2B Codes-->
	<xsl:variable name="country1">AF</xsl:variable>
	<xsl:variable name="country2">AX</xsl:variable>
	<xsl:variable name="country3">AL</xsl:variable>
	<xsl:variable name="country4">DZ</xsl:variable>
	<xsl:variable name="country5">AS</xsl:variable>
	<xsl:variable name="country6">AD</xsl:variable>
	<xsl:variable name="country7">AO</xsl:variable>
	<xsl:variable name="country8">AI</xsl:variable>
	<xsl:variable name="country9">AQ</xsl:variable>
	<xsl:variable name="country10">AG</xsl:variable>
	<xsl:variable name="country11">AR</xsl:variable>
	<xsl:variable name="country12">AM</xsl:variable>
	<xsl:variable name="country13">AW</xsl:variable>
	<xsl:variable name="country14">AU</xsl:variable>
	<xsl:variable name="country15">AT</xsl:variable>
	<xsl:variable name="country16">AZ</xsl:variable>
	<xsl:variable name="country17">BH</xsl:variable>
	<xsl:variable name="country18">BS</xsl:variable>
	<xsl:variable name="country19">BD</xsl:variable>
	<xsl:variable name="country20">BB</xsl:variable>
	<xsl:variable name="country21">BY</xsl:variable>
	<xsl:variable name="country22">BE</xsl:variable>
	<xsl:variable name="country23">BZ</xsl:variable>
	<xsl:variable name="country24">BJ</xsl:variable>
	<xsl:variable name="country25">BM</xsl:variable>
	<xsl:variable name="country26">BT</xsl:variable>
	<xsl:variable name="country27">BO</xsl:variable>
	<xsl:variable name="country28">BQ</xsl:variable>
	<xsl:variable name="country29">BA</xsl:variable>
	<xsl:variable name="country30">BW</xsl:variable>
	<xsl:variable name="country31">BV</xsl:variable>
	<xsl:variable name="country32">BR</xsl:variable>
	<xsl:variable name="country33">IO</xsl:variable>
	<xsl:variable name="country34">BN</xsl:variable>
	<xsl:variable name="country35">BG</xsl:variable>
	<xsl:variable name="country36">BF</xsl:variable>
	<xsl:variable name="country37">BI</xsl:variable>
	<xsl:variable name="country38">KH</xsl:variable>
	<xsl:variable name="country39">CM</xsl:variable>
	<xsl:variable name="country40">CA</xsl:variable>
	<xsl:variable name="country41">CV</xsl:variable>
	<xsl:variable name="country42">KY</xsl:variable>
	<xsl:variable name="country43">CF</xsl:variable>
	<xsl:variable name="country44">TD</xsl:variable>
	<xsl:variable name="country45">CL</xsl:variable>
	<xsl:variable name="country46">CN</xsl:variable>
	<xsl:variable name="country47">CX</xsl:variable>
	<xsl:variable name="country48">CC</xsl:variable>
	<xsl:variable name="country49">CO</xsl:variable>
	<xsl:variable name="country50">KM</xsl:variable>
	<xsl:variable name="country51">CG</xsl:variable>
	<xsl:variable name="country52">CD</xsl:variable>
	<xsl:variable name="country53">CK</xsl:variable>
	<xsl:variable name="country54">CR</xsl:variable>
	<xsl:variable name="country55">CI</xsl:variable>
	<xsl:variable name="country56">HR</xsl:variable>
	<xsl:variable name="country57">CU</xsl:variable>
	<xsl:variable name="country58">CW</xsl:variable>
	<xsl:variable name="country59">CY</xsl:variable>
	<xsl:variable name="country60">CZ</xsl:variable>
	<xsl:variable name="country61">DK</xsl:variable>
	<xsl:variable name="country62">DJ</xsl:variable>
	<xsl:variable name="country63">DM</xsl:variable>
	<xsl:variable name="country64">DO</xsl:variable>
	<xsl:variable name="country65">EC</xsl:variable>
	<xsl:variable name="country66">EG</xsl:variable>
	<xsl:variable name="country67">SV</xsl:variable>
	<xsl:variable name="country68">GQ</xsl:variable>
	<xsl:variable name="country69">ER</xsl:variable>
	<xsl:variable name="country70">EE</xsl:variable>
	<xsl:variable name="country71">ET</xsl:variable>
	<xsl:variable name="country72">FK</xsl:variable>
	<xsl:variable name="country73">FO</xsl:variable>
	<xsl:variable name="country74">FJ</xsl:variable>
	<xsl:variable name="country75">FI</xsl:variable>
	<xsl:variable name="country76">FR</xsl:variable>
	<xsl:variable name="country77">GF</xsl:variable>
	<xsl:variable name="country78">PF</xsl:variable>
	<xsl:variable name="country79">TF</xsl:variable>
	<xsl:variable name="country80">GA</xsl:variable>
	<xsl:variable name="country81">GM</xsl:variable>
	<xsl:variable name="country82">GE</xsl:variable>
	<xsl:variable name="country83">DE</xsl:variable>
	<xsl:variable name="country84">GH</xsl:variable>
	<xsl:variable name="country85">GI</xsl:variable>
	<xsl:variable name="country86">GR</xsl:variable>
	<xsl:variable name="country87">GL</xsl:variable>
	<xsl:variable name="country88">GD</xsl:variable>
	<xsl:variable name="country89">GP</xsl:variable>
	<xsl:variable name="country90">GU</xsl:variable>
	<xsl:variable name="country91">GT</xsl:variable>
	<xsl:variable name="country92">GG</xsl:variable>
	<xsl:variable name="country93">GN</xsl:variable>
	<xsl:variable name="country94">GW</xsl:variable>
	<xsl:variable name="country95">GY</xsl:variable>
	<xsl:variable name="country96">HT</xsl:variable>
	<xsl:variable name="country97">HM</xsl:variable>
	<xsl:variable name="country98">VA</xsl:variable>
	<xsl:variable name="country99">HN</xsl:variable>
	<xsl:variable name="country100">HK</xsl:variable>
	<xsl:variable name="country101">HU</xsl:variable>
	<xsl:variable name="country102">IS</xsl:variable>
	<xsl:variable name="country103">IN</xsl:variable>
	<xsl:variable name="country104">ID</xsl:variable>
	<xsl:variable name="country105">IR</xsl:variable>
	<xsl:variable name="country106">IQ</xsl:variable>
	<xsl:variable name="country107">IE</xsl:variable>
	<xsl:variable name="country108">IM</xsl:variable>
	<xsl:variable name="country109">IL</xsl:variable>
	<xsl:variable name="country110">IT</xsl:variable>
	<xsl:variable name="country111">JM</xsl:variable>
	<xsl:variable name="country112">JP</xsl:variable>
	<xsl:variable name="country113">JE</xsl:variable>
	<xsl:variable name="country114">JO</xsl:variable>
	<xsl:variable name="country115">KZ</xsl:variable>
	<xsl:variable name="country116">KE</xsl:variable>
	<xsl:variable name="country117">KI</xsl:variable>
	<xsl:variable name="country118">KP</xsl:variable>
	<xsl:variable name="country119">KR</xsl:variable>
	<xsl:variable name="country120">KW</xsl:variable>
	<xsl:variable name="country121">KG</xsl:variable>
	<xsl:variable name="country122">LA</xsl:variable>
	<xsl:variable name="country123">LV</xsl:variable>
	<xsl:variable name="country124">LB</xsl:variable>
	<xsl:variable name="country125">LS</xsl:variable>
	<xsl:variable name="country126">LR</xsl:variable>
	<xsl:variable name="country127">LY</xsl:variable>
	<xsl:variable name="country128">LI</xsl:variable>
	<xsl:variable name="country129">LT</xsl:variable>
	<xsl:variable name="country130">LU</xsl:variable>
	<xsl:variable name="country131">MO</xsl:variable>
	<xsl:variable name="country132">MK</xsl:variable>
	<xsl:variable name="country133">MG</xsl:variable>
	<xsl:variable name="country134">MW</xsl:variable>
	<xsl:variable name="country135">MY</xsl:variable>
	<xsl:variable name="country136">MV</xsl:variable>
	<xsl:variable name="country137">ML</xsl:variable>
	<xsl:variable name="country138">MT</xsl:variable>
	<xsl:variable name="country139">MH</xsl:variable>
	<xsl:variable name="country140">MQ</xsl:variable>
	<xsl:variable name="country141">MR</xsl:variable>
	<xsl:variable name="country142">MU</xsl:variable>
	<xsl:variable name="country143">YT</xsl:variable>
	<xsl:variable name="country144">MX</xsl:variable>
	<xsl:variable name="country145">FM</xsl:variable>
	<xsl:variable name="country146">MD</xsl:variable>
	<xsl:variable name="country147">MC</xsl:variable>
	<xsl:variable name="country148">MN</xsl:variable>
	<xsl:variable name="country149">ME</xsl:variable>
	<xsl:variable name="country150">MS</xsl:variable>
	<xsl:variable name="country151">MA</xsl:variable>
	<xsl:variable name="country152">MZ</xsl:variable>
	<xsl:variable name="country153">MM</xsl:variable>
	<xsl:variable name="country154">NA</xsl:variable>
	<xsl:variable name="country155">NR</xsl:variable>
	<xsl:variable name="country156">NP</xsl:variable>
	<xsl:variable name="country157">NL</xsl:variable>
	<xsl:variable name="country158">NC</xsl:variable>
	<xsl:variable name="country159">NZ</xsl:variable>
	<xsl:variable name="country160">NI</xsl:variable>
	<xsl:variable name="country161">NE</xsl:variable>
	<xsl:variable name="country162">NG</xsl:variable>
	<xsl:variable name="country163">NU</xsl:variable>
	<xsl:variable name="country164">NF</xsl:variable>
	<xsl:variable name="country165">MP</xsl:variable>
	<xsl:variable name="country166">NO</xsl:variable>
	<xsl:variable name="country167">OM</xsl:variable>
	<xsl:variable name="country168">PK</xsl:variable>
	<xsl:variable name="country169">PW</xsl:variable>
	<xsl:variable name="country170">PS</xsl:variable>
	<xsl:variable name="country171">PA</xsl:variable>
	<xsl:variable name="country172">PG</xsl:variable>
	<xsl:variable name="country173">PY</xsl:variable>
	<xsl:variable name="country174">PE</xsl:variable>
	<xsl:variable name="country175">PH</xsl:variable>
	<xsl:variable name="country176">PN</xsl:variable>
	<xsl:variable name="country177">PL</xsl:variable>
	<xsl:variable name="country178">PT</xsl:variable>
	<xsl:variable name="country179">PR</xsl:variable>
	<xsl:variable name="country180">QA</xsl:variable>
	<xsl:variable name="country181">RE</xsl:variable>
	<xsl:variable name="country182">RO</xsl:variable>
	<xsl:variable name="country183">RU</xsl:variable>
	<xsl:variable name="country184">RW</xsl:variable>
	<xsl:variable name="country185">BL</xsl:variable>
	<xsl:variable name="country186">SH</xsl:variable>
	<xsl:variable name="country187">KN</xsl:variable>
	<xsl:variable name="country188">LC</xsl:variable>
	<xsl:variable name="country189">MF</xsl:variable>
	<xsl:variable name="country190">PM</xsl:variable>
	<xsl:variable name="country191">VC</xsl:variable>
	<xsl:variable name="country192">WS</xsl:variable>
	<xsl:variable name="country193">SM</xsl:variable>
	<xsl:variable name="country194">ST</xsl:variable>
	<xsl:variable name="country195">SA</xsl:variable>
	<xsl:variable name="country196">SN</xsl:variable>
	<xsl:variable name="country197">RS</xsl:variable>
	<xsl:variable name="country198">SC</xsl:variable>
	<xsl:variable name="country199">SL</xsl:variable>
	<xsl:variable name="country200">SG</xsl:variable>
	<xsl:variable name="country201">SX</xsl:variable>
	<xsl:variable name="country202">SK</xsl:variable>
	<xsl:variable name="country203">SI</xsl:variable>
	<xsl:variable name="country204">SB</xsl:variable>
	<xsl:variable name="country205">SO</xsl:variable>
	<xsl:variable name="country206">ZA</xsl:variable>
	<xsl:variable name="country207">GS</xsl:variable>
	<xsl:variable name="country208">SS</xsl:variable>
	<xsl:variable name="country209">ES</xsl:variable>
	<xsl:variable name="country210">LK</xsl:variable>
	<xsl:variable name="country211">SD</xsl:variable>
	<xsl:variable name="country212">SR</xsl:variable>
	<xsl:variable name="country213">SJ</xsl:variable>
	<xsl:variable name="country214">SZ</xsl:variable>
	<xsl:variable name="country215">SE</xsl:variable>
	<xsl:variable name="country216">CH</xsl:variable>
	<xsl:variable name="country217">SY</xsl:variable>
	<xsl:variable name="country218">TW</xsl:variable>
	<xsl:variable name="country219">TJ</xsl:variable>
	<xsl:variable name="country220">TZ</xsl:variable>
	<xsl:variable name="country221">TH</xsl:variable>
	<xsl:variable name="country222">TL</xsl:variable>
	<xsl:variable name="country223">TG</xsl:variable>
	<xsl:variable name="country224">TK</xsl:variable>
	<xsl:variable name="country225">TO</xsl:variable>
	<xsl:variable name="country226">TT</xsl:variable>
	<xsl:variable name="country227">TN</xsl:variable>
	<xsl:variable name="country228">TR</xsl:variable>
	<xsl:variable name="country229">TM</xsl:variable>
	<xsl:variable name="country230">TC</xsl:variable>
	<xsl:variable name="country231">TV</xsl:variable>
	<xsl:variable name="country232">UG</xsl:variable>
	<xsl:variable name="country233">UA</xsl:variable>
	<xsl:variable name="country234">AE</xsl:variable>
	<xsl:variable name="country235">GB</xsl:variable>
	<xsl:variable name="country236">US</xsl:variable>
	<xsl:variable name="country237">UM</xsl:variable>
	<xsl:variable name="country238">UY</xsl:variable>
	<xsl:variable name="country239">UZ</xsl:variable>
	<xsl:variable name="country240">VU</xsl:variable>
	<xsl:variable name="country241">VE</xsl:variable>
	<xsl:variable name="country242">VN</xsl:variable>
	<xsl:variable name="country243">VG</xsl:variable>
	<xsl:variable name="country244">VI</xsl:variable>
	<xsl:variable name="country245">WF</xsl:variable>
	<xsl:variable name="country246">EH</xsl:variable>
	<xsl:variable name="country247">YE</xsl:variable>
	<xsl:variable name="country248">ZM</xsl:variable>
	<xsl:variable name="country249">ZW</xsl:variable>
	<xsl:variable name="country250">EU</xsl:variable>
	<!--Country E2B Codes-->
	<!--B.4.k.11-->
	<xsl:variable name="AdditionalInformation">2</xsl:variable>
	<!--B.5 - Summary-->
	<!--B.5.3-->
	<xsl:variable name="Diagnosis">15</xsl:variable>
	<xsl:variable name="Sender">1</xsl:variable>
	<!--B.5.5-->
	<xsl:variable name="SummaryAndComment">36</xsl:variable>
	<xsl:variable name="Reporter">2</xsl:variable>
	<!--ACK-->
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
	<!--EU Specific Data fields-->
	<xsl:variable name="oidEUMethodofAssessment">2.16.840.1.113883.3.989.5.1.1.5.2</xsl:variable>
	<xsl:variable name="oidEUMethodofAssessmentCSV">1.0</xsl:variable>
	<xsl:variable name="oidEUResultofAssessment">2.16.840.1.113883.3.989.5.1.1.5.3</xsl:variable>
	<xsl:variable name="oidEUResultofAssessmentCSV">1.0</xsl:variable>
	<xsl:variable name="oidEUSourceofAssessment">2.16.840.1.113883.3.989.5.1.1.5.4</xsl:variable>
	<xsl:variable name="oidEUSourceofAssessmentCSV">1.0</xsl:variable>
	<xsl:variable name="oidEUMessageType">2.16.840.1.113883.3.989.5.1.1.5.1</xsl:variable>
	<xsl:variable name="oidEUMessageTypeCSV">1.0</xsl:variable>
	<!--Other Variables used for Special Cases-->
	<xsl:variable name="Decade">800</xsl:variable>
	<xsl:variable name="Year">801</xsl:variable>
	<xsl:variable name="Month">802</xsl:variable>
	<xsl:variable name="Week">803</xsl:variable>
	<xsl:variable name="Day">804</xsl:variable>
	<xsl:variable name="Trimester">810</xsl:variable>
	<xsl:variable name="oidReportJapCharacterizationCode">2.16.840.1.113883.3.989.5.1.3.2.1.12</xsl:variable>
	<xsl:variable name="oidPMDAReportType">2.16.840.1.113883.3.989.5.1.3.2.1.1</xsl:variable>
	<xsl:variable name="oidPMDAReportTypeCSV">1.1</xsl:variable>
	<xsl:variable name="PMDACSV">1.2</xsl:variable>
	<xsl:variable name="PMDAStudyPhase">2.16.840.1.113883.3.989.5.1.3.2.1.7</xsl:variable>
	<xsl:variable name="StudyPhase">2.16.840.1.113883.3.989.5.1.3.2.1.10</xsl:variable>
	<xsl:variable name="JPNProductTypeCS">2.16.840.1.113883.3.989.5.1.3.2.1.13</xsl:variable>
	<xsl:variable name="JPNProductTypeCSV">1.1</xsl:variable>
	<xsl:variable name="DrugOCTAccessCS">2.16.840.1.113883.3.989.5.1.3.2.1.5</xsl:variable>
	<xsl:variable name="TestResultCS">2.16.840.1.113883.3.989.2.1.1.12</xsl:variable>
	<!--Convert an R2 code into the corresponding R3 code as specificed in the "mapping-codes.xml" file-->

	<xsl:template name="getMapping">
		<xsl:param name="type"/>
		<xsl:param name="code"/>

		<!--check length if numeric and add leading zeros if missing-->
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
							<!--	<xsl:value-of select="$code"/>-->
							<xsl:otherwise>{<xsl:value-of select="translate($code, '', '-')"/>}</xsl:otherwise>
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
					<!--	<xsl:value-of select="$code"/>-->
					<xsl:otherwise>{<xsl:value-of select="translate($code, '', '-')"/>}</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!--Return yes if N8, no otherwise-->
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
	<!--Batch Header : M.1.1, M.1.4 and M.1.7-->
	<xsl:template match="ichicsrtransmissionidentification" mode="part-a">
		<xsl:comment>N.1.2:Batch Number</xsl:comment>
		<xsl:comment>N.1.5:Date of Batch Transmission</xsl:comment>
		<xsl:choose>
			<xsl:when test="string-length(messagenumb) > 0">
				<id extension="{messagenumb}" root="{$oidBatchNumber}"/>										<!--M.1.4	- Batch Number-->
				<creationTime value="{transmissiondate}"/>
			</xsl:when>
			<xsl:otherwise>
				<id extension="NOTAVAILABLE" root="{$oidBatchNumber}"/>
				<creationTime value="20150101"/>
			</xsl:otherwise>															<!--M.1.7 - Date of Batch Transmission-->
		</xsl:choose>

		<!--Mandatory element-->
		<responseModeCode code="D"/>
		<interactionId root="2.16.840.1.113883.1.6" extension="MCCI_IN200100UV01"/>
		<!--EU message types-->
		<xsl:choose>
			<xsl:when test="messagetype = 'ichicsr'">
				<name code="1" codeSystem="{$oidMessageType}" codeSystemVersion="{messagetypecsv}"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="messagetype = 'backlog'"><name code="1" codeSystem="{$oidEUMessageType}" codeSystemVersion="{$oidEUMessageTypeCSV}" displayName="backlog"/> </xsl:when>
					<xsl:when test="messagetype = 'backlogct'"><name code="1" codeSystem="{$oidEUMessageType}" codeSystemVersion="{$oidEUMessageTypeCSV}" displayName="backlog"/> </xsl:when>
					<xsl:when test="messagetype = 'psur'"><name code="1" codeSystem="{$oidMessageType}" codeSystemVersion="{$oidMessageTypeCSV}" displayName="ichicsr"/> </xsl:when>
					<xsl:when test="messagetype = 'ctasr'"><name code="1" codeSystem="{$oidMessageType}" codeSystemVersion="{$oidMessageTypeCSV}" displayName="ichicsr"/> </xsl:when>
					<xsl:when test="messagetype = 'masterichicsr'"><name code="2" codeSystem="{$oidEUMessageType}" codeSystemVersion="{$oidEUMessageTypeCSV}" displayName="masterichicsr"/> </xsl:when>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:comment>N.1.1[Ver]:Types of Message in batch</xsl:comment>
		<xsl:comment>N.1.1.CSV:Types of Message in batch code system version</xsl:comment>
		<!--M.1.1 - Message Type in Batch-->
	</xsl:template>
	<!--Date of this transmission-->
	<xsl:template match="safetyreport" mode="header">

	</xsl:template>
	<!--Message Header : M.2.r.5 and M.2.r.6-->
	<xsl:template match="ichicsrmessageheader" mode="part-b">
		<!--M.2.r.7 - Message Date-->
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

	<!--Batch Footer : M.1.5 and M.1.6-->
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
			<xsl:comment>N.1.3:Batch Sender Identifier</xsl:comment>
		</sender>

	</xsl:template>
	<xsl:template match="transmissiondate">
		<xsl:variable name="version-number" select="../safetyreportversion"/>
		<xsl:choose>
			<xsl:when test="string-length($version-number) = 0"><effectiveTime value="{.}"/> </xsl:when>
			<xsl:when test="string-length($version-number) = 1"><effectiveTime value="{.}00000{$version-number}"/> </xsl:when>
			<xsl:when test="string-length($version-number) = 2"><effectiveTime value="{.}0000{$version-number}"/> </xsl:when>
			<xsl:otherwise><effectiveTime value="{.}"/></xsl:otherwise>
		</xsl:choose>
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

				<xsl:if test="string-length(receivedate) > 0">
					<statusCode code="active"/>
					<effectiveTime>
						<low value = "{receivedate}"/>
					</effectiveTime>
					<xsl:comment>C.1.4:Date Report Was First Received from Source</xsl:comment>
				</xsl:if>
				<!--A.1.7 - Date of Most Recent Information for this Case-->
				<xsl:if test="string-length(receiptdate) > 0">
					<availabilityTime value="{receiptdate}"/>
					<xsl:comment>C.1.5:Date of Most Recent Information for This Report</xsl:comment>
				</xsl:if>
				<!--A.1.8.1.r Document Held by Sender-->
				<xsl:comment> C.1.6.1.r.1: Documents Held by Sender (repeat as necessary) </xsl:comment>
				<xsl:apply-templates select="additionaldocuments"/>

				<!--A.4.r Literature References-->
				<xsl:comment> C.4.r.1: Literature Reference(s) </xsl:comment>
				<xsl:apply-templates select="literature"/>
				<!--J2・15・r-->
				<xsl:apply-templates select="pmdapublishedcountry"/>

				<!--B.1.x - Patient-->
				<xsl:comment> D.1: Patient (name or initials) </xsl:comment>
				<xsl:apply-templates select="patient" mode="identification"/>
				<xsl:apply-templates select="narrativesendercommentnative"/>
				<!--A.1.8.1 - Are Additional Documents Available?-->
				<xsl:comment> C.1.6.1: Are Additional Documents Available? </xsl:comment>
				<component typeCode="COMP">
					<observationEvent classCode="OBS" moodCode="EVN">
						<code code="{$AdditionalDocumentsAvailable}" codeSystem="{$oidObservationCode}" displayName="additionalDocumentsAvailable"/>
						<value xsi:type="BL" value="true"/>
					</observationEvent>
				</component>
				<component typeCode="COMP">
					<observationEvent classCode="OBS" moodCode="EVN">
						<code code="{$LocalCriteriaForExpedited}" codeSystem="{$oidObservationCode}" displayName="localCriteriaForExpedited"/>
						<xsl:choose>
							<xsl:when test="fulfillexpeditecriteria= 1"> <value xsi:type="BL" value="true"/> </xsl:when>
							<xsl:when test="fulfillexpeditecriteria= 2"> <value xsi:type="BL" value="false"/> </xsl:when>
							<xsl:when test="fulfillexpeditecriteria= 'TRUE'"> <value xsi:type="BL" value="true"/> </xsl:when>
							<xsl:when test="fulfillexpeditecriteria= 'FALSE'"> <value xsi:type="BL" value="false"/> </xsl:when>
							<xsl:when test="fulfillexpeditecriteria= 'True'"> <value xsi:type="BL" value="true"/> </xsl:when>
							<xsl:when test="fulfillexpeditecriteria= 'False'"> <value xsi:type="BL" value="false"/> </xsl:when>
							<xsl:when test="fulfillexpeditecriteria= 'true'"> <value xsi:type="BL" value="true"/> </xsl:when>
							<xsl:when test="fulfillexpeditecriteria= 'false'"> <value xsi:type="BL" value="false"/> </xsl:when>
							<xsl:when test="fulfillexpeditecriteria= 'Yes'"> <value xsi:type="BL" value="true"/> </xsl:when>
							<xsl:when test="fulfillexpeditecriteria= 'No'"> <value xsi:type="BL" value="false"/> </xsl:when>
							<xsl:when test="fulfillexpeditecriteria= 'yes'"> <value xsi:type="BL" value="true"/> </xsl:when>
							<xsl:when test="fulfillexpeditecriteria= 'no'"> <value xsi:type="BL" value="false"/> </xsl:when>
							<xsl:when test="fulfillexpeditecriteria= 'YES'"> <value xsi:type="BL" value="true"/> </xsl:when>
							<xsl:when test="fulfillexpeditecriteria= 'NO'"> <value xsi:type="BL" value="false"/> </xsl:when>

							<xsl:otherwise>
								<value xsi:type="BL" nullFlavor="NI"/>
							</xsl:otherwise>
						</xsl:choose>
					</observationEvent>
					<xsl:comment>C.1.7: Does This Case Fulfil the Local Criteria for an Expedited Report?</xsl:comment>
				</component>

				<!--	A.1.9 - Does this Case Fulfil the Local Criteria for an Expedited Report?
<xsl:comment> C.1.7: Does This Case Fulfil the Local Criteria for an Expedited Report? </xsl:comment>
<xsl:call-template name="fulfillexpeditecriteria"/> -->

				<!-- H.5.r Case Summary and Reporter’s Comments in Native Language (repeat as necessary) -->
				<xsl:apply-templates select="summary/casesummarynarrative"/>

				<!-- C.1.8.2: First Sender of This Case -->
				<xsl:if test="string-length(icsrsource)> 0">
					<xsl:comment>C.1.8.2: First Sender of This Case  </xsl:comment>
					<outboundRelationship typeCode="SPRT">
						<relatedInvestigation classCode="INVSTG" moodCode="EVN">
							<code code="{$InitialReport}" codeSystem="{$oidReportRelationCode}" codeSystemVersion="{$emaReportRelationCLVersion}"/>
							<subjectOf2 typeCode="SUBJ">
								<controlActEvent classCode="CACT" moodCode="EVN">
									<author typeCode="AUT">
										<assignedEntity classCode="ASSIGNED">
											<code code="{icsrsource}" codeSystem="{$oidFirstSender}" codeSystemVersion="{$emaoidC182CLVersion}"/>
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
				<xsl:comment> C.2.r Primary Sources </xsl:comment>
				<xsl:apply-templates select="primarysource"/>
				<!--A.3 Sender-->
				<xsl:comment> C.3 Sender </xsl:comment>
				<xsl:apply-templates select="sender"/>
				<!--A.1.11 Report Duplicate-->
				<xsl:apply-templates select="reportduplicate"/>
				<!--A.1.4 - Type of Report-->
				<xsl:comment> C.1.3 Type of Report </xsl:comment>
				<xsl:comment> C.1.3.CSV Type of Report Code System Version</xsl:comment>
				<subjectOf2 typeCode="SUBJ">
					<investigationCharacteristic classCode="OBS" moodCode="EVN">
						<code code="{$ReportType}" codeSystem="{$oidReportCharacterizationCode}" codeSystemVersion="{$oidReportTypeCSV}" displayName="ichReportType" />
						<xsl:choose>
							<xsl:when test="reporttype= 1"><value xsi:type="CE" code="{reporttype}" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="SpontaneousReport"/> </xsl:when>
							<xsl:when test="reporttype= 2"><value xsi:type="CE" code="{reporttype}" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="ReportFromStudy"/> </xsl:when>
							<xsl:when test="reporttype=3"><value xsi:type="CE" code="{reporttype}" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="Other"/> </xsl:when>
							<xsl:when test="reporttype= 4"><value xsi:type="CE" code="{reporttype}" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="Notavailabletosender/unknown"/> </xsl:when>
							<xsl:when test="reporttype= 01"><value xsi:type="CE" code="1" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="SpontaneousReport"/> </xsl:when>
							<xsl:when test="reporttype= 02"><value xsi:type="CE" code="2" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="ReportFromStudy"/> </xsl:when>
							<xsl:when test="reporttype= 03"><value xsi:type="CE" code="3" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="Other"/> </xsl:when>
							<xsl:when test="reporttype= 04"><value xsi:type="CE" code="4" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="Notavailabletosender/unknown"/> </xsl:when>
							<xsl:when test="reporttype= 001"><value xsi:type="CE" code="1" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="SpontaneousReport"/> </xsl:when>
							<xsl:when test="reporttype= 002"><value xsi:type="CE" code="2" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="ReportFromStudy"/> </xsl:when>
							<xsl:when test="reporttype= 003"><value xsi:type="CE" code="3" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="Other"/> </xsl:when>
							<xsl:when test="reporttype= 004"><value xsi:type="CE" code="4" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="Notavailabletosender/unknown"/> </xsl:when>
							<xsl:when test="reporttype= 'StimulatedSpontaneous'"><value xsi:type="CE" code="1" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="StimulatedSpontaneous"/></xsl:when>
							<xsl:when test="reporttype= 'SpontaneousReport'"><value xsi:type="CE" code="1" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="SpontaneousReport"/> </xsl:when>
							<xsl:when test="reporttype= 'ReportFromStudy'"><value xsi:type="CE" code="2" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="Reportfromstudy"/> </xsl:when>
							<xsl:when test="reporttype= 'Other'"><value xsi:type="CE" code="3" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="Other"/> </xsl:when>
							<xsl:when test="reporttype= 'Notavailabletosender/unknown'"><value xsi:type="CE" code="4" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="Notavailabletosender/unknown"/> </xsl:when>
							<xsl:when test="reporttype= 'STIMULATEDSPONTANEOUS'"><value xsi:type="CE" code="1" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="StimulatedSpontaneous"/> </xsl:when>
							<xsl:when test="reporttype= 'SPONTANEOUSREPORT'"><value xsi:type="CE" code="1" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="SpontaneousReport"/> </xsl:when>
							<xsl:when test="reporttype= 'REPORTFROMSTUDY'"><value xsi:type="CE" code="2" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="Reportfromstudy"/> </xsl:when>
							<xsl:when test="reporttype= 'OTHER'"><value xsi:type="CE" code="3" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="Other"/> </xsl:when>
							<xsl:when test="reporttype= 'NOTAVAILABLETOSENDER/UNKNOWN'"><value xsi:type="CE" code="4" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="Notavailabletosender/unknown"/> </xsl:when>

							<xsl:when test="reporttype= 'stimulatedspontaneous'"><value xsi:type="CE" code="1" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="StimulatedSpontaneous"/> </xsl:when>
							<xsl:when test="reporttype= 'spontaneousreport'"><value xsi:type="CE" code="1" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="SpontaneousReport"/> </xsl:when>
							<xsl:when test="reporttype= 'reportfromstudy'"><value xsi:type="CE" code="2" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="Reportfromstudy"/> </xsl:when>
							<xsl:when test="reporttype= 'other'"><value xsi:type="CE" code="3" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="Other"/> </xsl:when>
							<xsl:when test="reporttype= 'Notavailabletosender/unknown'"><value xsi:type="CE" code="4" codeSystemVersion="{reporttypecsv}" codeSystem="{$oidReportType}" displayName="Notavailabletosender/unknown"/> </xsl:when>
						</xsl:choose>
					</investigationCharacteristic>
				</subjectOf2>

				<!--A.1.11 - Other Case Identifiers in Previous Transmissions-->
				<xsl:comment>C.1.9.1: Other Case Identifiers in Previous Transmissions  </xsl:comment>
				<subjectOf2 typeCode="SUBJ">
					<investigationCharacteristic classCode="OBS" moodCode="EVN">
						<code code="{$OtherCaseIDs}" codeSystem="{$oidReportCharacterizationCode}" codeSystemVersion="{$emaReportCharacterizationCLVersion}"/>
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

				<!-- Report Nullification / Amendment  -->

				<xsl:if test="string-length(casenullificationoramendment) > 0">
					<xsl:comment>C.1.11.1: Report Nullification / Amendment</xsl:comment>
					<subjectOf2 typeCode="SUBJ">
						<investigationCharacteristic classCode="OBS" moodCode="EVN">
							<code code="{$NullificationAmendmentCode}" codeSystem="{$oidReportCharacterizationCode}" codeSystemVersion="{$emaReportCharacterizationCLVersion}"/>
							<value xsi:type="CE" code="{casenullificationoramendment}" codeSystem="{$oidNullificationAmendment}" codeSystemVersion="{casenullificationoramendmentcsv}"/>
						</investigationCharacteristic>
					</subjectOf2>
				</xsl:if>
				<!-- Nullification or Amendment reason -->

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

				<!-- C.5.4.KR.1: Other Test Details  -->

				<xsl:if test="string-length(studyidentification/krobservestudytype) > 0">
					<xsl:apply-templates select="studyidentification" mode="MFDS-studyidentification"/>
				</xsl:if>


			</investigationEvent>
		</subject>
	</xsl:template>

	<!--
<xsl:for-each select="narrativesendercommentnative">
<xsl:if test="string-length(summaryandreportercomments) and string-length(summaryandreportercommentslang) > 0" >
<xsl:variable name="positionCaseSumNar">
      <xsl:value-of select="position()"/>
</xsl:variable>
<xsl:comment>H.5.r: Case Summary and Reporter’s Comments in Native Language - (<xsl:value-of select="$positionCaseSumNar"/>)</xsl:comment>
      <component typeCode="COMP">
        <observationEvent moodCode="EVN" classCode="OBS">
          <code code="{$SummaryAndComment}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$ichObservationCLVersion}"/>
			<xsl:comment>H.5.r.1a: Case Summary and Reporter's Comments Text </xsl:comment>
			<xsl:comment>H.5.r.1b: Case Summary and Reporter's Comments Language </xsl:comment>
          <value xsi:type="ED" language="{summaryandreportercommentslang}" mediaType="text/plain">
            <xsl:value-of select="summaryandreportercomments"/>
          </value>
          <author typeCode="AUT">
          <assignedEntity classCode="ASSIGNED">
              <code code="{$Reporter}" codeSystem="{$oidAssignedEntityRoleCode}" codeSystemVersion="{$ichoidAssignedEntityRoleCodeVersion}"/>
            </assignedEntity>
          </author>
		</observationEvent>
	</component>
	</xsl:if>

</xsl:for-each>  -->


	<!-- H.5.r Case Summary and Reporter’s Comments in Native Language (repeat as necessary) -->
	<xsl:template match="summary/casesummarynarrative">
		<xsl:if test="string-length(summaryandreportercomments) > 0">
			<xsl:variable name="positionCaseSumNar">
				<xsl:value-of select="position()"/>
			</xsl:variable>
			<xsl:comment>H.5.r: Case Summary and Reporter’s Comments in Native Language - (<xsl:value-of select="$positionCaseSumNar"/>)</xsl:comment>
			<component typeCode="COMP">
				<observationEvent moodCode="EVN" classCode="OBS">
					<code code="{$SummaryAndComment}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$ichObservationCLVersion}"/>
					<xsl:comment>H.5.r.1a: Case Summary and Reporter's Comments Text </xsl:comment>
					<xsl:comment>H.5.r.1b: Case Summary and Reporter's Comments Language </xsl:comment>
					<value xsi:type="ED" language="{summaryandreportercommentslang}" mediaType="text/plain">
						<xsl:value-of select="summaryandreportercomments"/>
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




	<!--Narrative Include Clinical :
E2B(R2): element "narrativeincludeclinical"
E2B(R3): element "investigationEvent"
-->

	<xsl:template match="summary/narrativeincludeclinical">
		<xsl:if test="string-length(.) > 0">
			<text mediaType="text/plain">
				<xsl:value-of select="."/>
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
					<code codeSystem="{$oidichreferencesource}" code="{$documentsHeldBySender}" displayName="documentsHeldBySender"/>
					<title>
						<xsl:value-of select="documentlist"/>
					</title>
					<xsl:comment>C.1.6.1.r.1:Documents Held by Sender</xsl:comment>
					<xsl:variable name="MediaType">
						<xsl:value-of select="substring-after(mediatype2,'.')"/>
					</xsl:variable>

					<xsl:if test="$MediaType= 'txt'">
						<text mediaType="text/plain">
							<xsl:value-of select="includedocuments"/>
						</text>
						<xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
					</xsl:if>
					<xsl:if test="$MediaType= 'pdf'">
						<text mediaType="application/pdf" representation="B64">
							<xsl:value-of select="includedocuments"/>
						</text>
						<xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
					</xsl:if>
					<xsl:if test="$MediaType= 'png'">
						<text mediaType="image/png" representation="B64">
							<xsl:value-of select="includedocuments"/>
						</text>
						<xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
					</xsl:if>
					<xsl:if test="$MediaType= 'jpeg'">
						<text mediaType="image/jpeg" representation="B64">
							<xsl:value-of select="includedocuments"/>
						</text>
						<xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
					</xsl:if>
					<xsl:if test="$MediaType= 'html'">
						<text mediaType="text/html" representation="B64">
							<xsl:value-of select="includedocuments"/>
						</text>
					</xsl:if>
					<xsl:if test="$MediaType= 'PSD'">
						<text mediaType="application/octet-stream" representation="B64">
							<xsl:value-of select="includedocuments"/>
						</text>
					</xsl:if>
					<xsl:if test="$MediaType= 'TIF'">
						<text mediaType="image/tiff" representation="B64">
							<xsl:value-of select="includedocuments"/>
						</text>
					</xsl:if>
					<xsl:if test="$MediaType= 'DOCX'">
						<text mediaType="application/vnd.openxmlformats-officedocument.wordprocessingml.document" representation="B64">
							<xsl:value-of select="includedocuments"/>
						</text>
					</xsl:if>
					<xsl:if test="$MediaType= 'XLS'">
						<text mediaType="application/vnd.ms-excel" representation="B64">
							<xsl:value-of select="includedocuments"/>
						</text>
					</xsl:if>
					<xsl:if test="$MediaType= 'XLSX'">
						<text mediaType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" representation="B64">
							<xsl:value-of select="includedocuments"/>
						</text>
					</xsl:if>
					<xsl:if test="$MediaType= 'VSD'">
						<text mediaType="application/x-visio" representation="B64">
							<xsl:value-of select="includedocuments"/>
						</text>
					</xsl:if>
					<xsl:if test="$MediaType= 'RTF'">
						<text mediaType="application/rtf" representation="B64">
							<xsl:value-of select="includedocuments"/>
						</text>
					</xsl:if>
					<xsl:if test="$MediaType= 'DOC'">
						<text mediaType="application/msword" representation="B64">
							<xsl:value-of select="includedocuments"/>
						</text>
					</xsl:if>
					<xsl:if test="$MediaType= 'PS'">
						<text mediaType="application/postscript" representation="B64">
							<xsl:value-of select="includedocuments"/>
						</text>
					</xsl:if>
					<xsl:if test="$MediaType= 'MDB'">
						<text mediaType="application/x-msaccess" representation="B64">
							<xsl:value-of select="includedocuments"/>
						</text>
					</xsl:if>
					<xsl:if test="$MediaType= 'BMP'">
						<text mediaType="image/bmp" representation="B64">
							<xsl:value-of select="includedocuments"/>
						</text>
					</xsl:if>
					<xsl:if test="$MediaType= 'XML'">
						<text mediaType="text/xml" representation="B64">
							<xsl:value-of select="includedocuments"/>
						</text>
					</xsl:if>
					<xsl:if test="$MediaType= 'SGM'">
						<text mediaType="text/sgml" representation="B64">
							<xsl:value-of select="includedocuments"/>
						</text>
					</xsl:if>
					<xsl:if test="$MediaType= 'MSG'">
						<text mediaType="application/vnd.ms-outlook" representation="B64">
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
	<!--Report Duplicate :
E2B(R2): element "reportduplicate" inside "safetyreport"
E2B(R3): element "controlActEvent"
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
	E2B(R3): element "relatedInvestigation"	-->

	<xsl:template match="linkreport">
		<xsl:if test="string-length(linkreportnumb)>0">
			<xsl:comment>C.1.10: Linked Report Information </xsl:comment>
			<xsl:variable name="positionLinkRptNum">
				<xsl:value-of select="position()"/>
			</xsl:variable>
			<outboundRelationship typeCode="SPRT">
				<relatedInvestigation classCode="INVSTG" moodCode="EVN">
					<code nullFlavor="NA"/>
					<subjectOf2 typeCode="SUBJ">
						<controlActEvent classCode="CACT" moodCode="EVN">
							<xsl:comment>C.1.10.r: Identification Number of the Report Which Is Linked to This Report - (<xsl:value-of select="$positionLinkRptNum"/>)</xsl:comment>
							<id extension="{linkreportnumb}" root="{$oidWorldWideCaseID}"/>
						</controlActEvent>
					</subjectOf2>
				</relatedInvestigation>
			</outboundRelationship>
		</xsl:if>
	</xsl:template>

	<xsl:template match="primarysource">
		<outboundRelationship typeCode="SPRT">
			<xsl:if test="string-length(casefirstsource) > 0">
				<xsl:if test="position() "><priorityNumber value="1"/> </xsl:if>
				<xsl:comment>C.2.r.5:Primary Source for Regulatory Purposes</xsl:comment>
			</xsl:if>
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
											<xsl:if test="string-length(krmedicalprofessionals) > 0">
												<xsl:comment>C.2.r.4.KR.1: Other medical professionals</xsl:comment>
												<code code="{krmedicalprofessionals}" codeSystem="{$oidKRQualification}" codeSystemVersion="1.0" DisplayName="Other medical professionals" />
											</xsl:if>
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

	<xsl:template match="summary/narrativesendercommentnativeold">
		<xsl:for-each select="Nativedata">
			<xsl:if test="string-length(summaryandreportercomments) > 0">
				<component typeCode="COMP">
					<observationEvent moodCode="EVN" classCode="OBS">
						<code code="{$SummaryAndComment}" codeSystem="{$oidObservationCode}"/>
						<value language="{summaryandreportercommentslang}" xsi:type="ED" mediaType="text/plain"><xsl:value-of select="normalize-space(summaryandreportercomments)"/></value>
						<author typeCode="AUT">
							<assignedEntity classCode="ASSIGNED">
								<code code="{$Reporter}" codeSystem="{$oidAssignedEntityRoleCode}"/>
							</assignedEntity>
						</author>
					</observationEvent>
				</component>
			</xsl:if>
			<xsl:comment> H.5.1a and H.5.1b Narrative and Sendercomment in Native Languague </xsl:comment>
		</xsl:for-each>
	</xsl:template>

	<!--display content of a field, unless it is masked-->
	<xsl:template name="field-or-mask1">
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
						<!--A.3.1	Sender Organization Type-->
						<xsl:choose>
							<xsl:when test="sendertype= 1"><code code="{sendertype}" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="PharmaceuticalCompany"/> </xsl:when>
							<xsl:when test="sendertype= 2"><code code="{sendertype}" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="RegulatoryAuthority"/> </xsl:when>
							<xsl:when test="sendertype=3"><code code="{sendertype}" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="HealthProfessional"/> </xsl:when>
							<xsl:when test="sendertype= 4"><code code="{sendertype}" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="RegionalPharmacovigilanceCentre"/> </xsl:when>
							<xsl:when test="sendertype= 5"><code code="{sendertype}" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="WHOcollaboratingcentresforinternationaldrugmonitoring"/> </xsl:when>
							<xsl:when test="sendertype= 6"><code code="{sendertype}" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="Other(e.g.distributororotherorganisation)"/> </xsl:when>
							<xsl:when test="sendertype= 7"><code code="{sendertype}" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="Patient/Consumer"/> </xsl:when>

							<xsl:when test="sendertype= 01"><code code="1" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="PharmaceuticalCompany"/> </xsl:when>
							<xsl:when test="sendertype= 02"><code code="2" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="RegulatoryAuthority"/> </xsl:when>
							<xsl:when test="sendertype= 03"><code code="3" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="HealthProfessional"/> </xsl:when>
							<xsl:when test="sendertype= 04"><code code="4" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="RegionalPharmacovigilanceCentre"/> </xsl:when>
							<xsl:when test="sendertype= 05"><code code="5" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="WHOcollaboratingcentresforinternationaldrugmonitoring"/> </xsl:when>
							<xsl:when test="sendertype= 06"><code code="6" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="Other(e.g.distributororotherorganisation)"/> </xsl:when>
							<xsl:when test="sendertype= 07"><code code="7" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="Patient/Consumer"/> </xsl:when>

							<xsl:when test="sendertype= 001"><code code="1" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="PharmaceuticalCompany"/> </xsl:when>
							<xsl:when test="sendertype= 002"><code code="2" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="RegulatoryAuthority"/> </xsl:when>
							<xsl:when test="sendertype= 003"><code code="3" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="HealthProfessional"/> </xsl:when>
							<xsl:when test="sendertype= 004"><code code="4" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="RegionalPharmacovigilanceCentre"/> </xsl:when>
							<xsl:when test="sendertype= 005"><code code="5" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="WHOcollaboratingcentresforinternationaldrugmonitoring"/> </xsl:when>
							<xsl:when test="sendertype= 006"><code code="6" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="Other(e.g.distributororotherorganisation)"/> </xsl:when>
							<xsl:when test="sendertype= 007"><code code="7" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="Patient/Consumer"/> </xsl:when>

							<xsl:when test="sendertype= 'PHARMACEUTICAL COMPANY'"><code code="1" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="PharmaceuticalCompany"/> </xsl:when>
							<xsl:when test="sendertype= 'REGULATORY AUTHORITY'"><code code="2" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="RegulatoryAuthority"/> </xsl:when>
							<xsl:when test="sendertype= 'HEALTH PROFESSIONAL'"><code code="3" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="HealthProfessional"/> </xsl:when>
							<xsl:when test="sendertype= 'REGIONAL PHARMACOVIGILANCE CENTRE'"><code code="{$oidSenderType}" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="RegionalPharmacovigilanceCentre"/> </xsl:when>
							<xsl:when test="sendertype= 'WHO COLLABORATING CENTRES FOR INTERNATIONAL DRUG MONITORING'"><code code="5" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="WHOcollaboratingcentresforinternationaldrugmonitoring"/> </xsl:when>
							<xsl:when test="sendertype= 'OTHER'"><code code="6" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="Other(e.g.distributororotherorganisation)"/> </xsl:when>
							<xsl:when test="sendertype= 'PATIENT / CONSUMER'"><code code="7" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="Patient/Consumer"/> </xsl:when>

							<xsl:when test="sendertype= 'pharmaceutical company'"><code code="1" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="PharmaceuticalCompany"/> </xsl:when>
							<xsl:when test="sendertype= 'regulatory authority'"><code code="2" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="RegulatoryAuthority"/> </xsl:when>
							<xsl:when test="sendertype= 'health professional'"><code code="3" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="HealthProfessional"/> </xsl:when>
							<xsl:when test="sendertype= 'regional pharmacovigilance centre'"><code code="4" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="RegionalPharmacovigilanceCentre"/> </xsl:when>
							<xsl:when test="sendertype= 'who collaborating centres for international drug monitoring'"><code code="5" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="WHOcollaboratingcentresforinternationaldrugmonitoring"/> </xsl:when>
							<xsl:when test="sendertype= 'other'"><code code="6" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="Other(e.g.distributororotherorganisation)"/> </xsl:when>
							<xsl:when test="sendertype= 'patient / consumer'"><code code="7" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="Patient/Consumer"/> </xsl:when>

							<xsl:when test="sendertype= 'Pharmaceutical Company'"><code code="1" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="PharmaceuticalCompany"/> </xsl:when>
							<xsl:when test="sendertype= 'Regulatory Authority'"><code code="2" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="RegulatoryAuthority"/> </xsl:when>
							<xsl:when test="sendertype= 'Health Professional'"><code code="3" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="HealthProfessional"/> </xsl:when>
							<xsl:when test="sendertype= 'Regional Pharmacovigilance Centre'"><code code="4" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="RegionalPharmacovigilanceCentre"/> </xsl:when>
							<xsl:when test="sendertype= 'WHO collaborating centres for international drug monitoring'"><code code="5" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="WHOcollaboratingcentresforinternationaldrugmonitoring"/> </xsl:when>
							<xsl:when test="sendertype= 'Other'"><code code="6" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="Other(e.g.distributororotherorganisation)"/> </xsl:when>
							<xsl:when test="sendertype= 'Patient / Consumer'"><code code="7" codeSystem="{$oidSenderType}" codeSystemVersion="{sendertypecsv}" displayName="Patient/Consumer"/> </xsl:when>
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
									<xsl:text>tel: </xsl:text>
									<xsl:if test="string-length(sendertelcountrycode) > 0">+<xsl:value-of select="sendertelcountrycode"/><xsl:text> </xsl:text></xsl:if>
									<xsl:value-of select="sendertel"/>
									<xsl:if test="string-length(sendertelextension) > 0"><xsl:text> </xsl:text><xsl:value-of select="sendertelextension"/></xsl:if>
								</xsl:attribute>
								<xsl:comment>C.3.4.6:Sender’s Telephone</xsl:comment>
							</telecom>
						</xsl:if>
						<!--A.3.4.ijk Sender Fax-->
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

							<!-- C.3.1.KR.1 Medical Professional Details -->
							<xsl:if test="string-length(krsendertype)> 0">
								<asQualifiedEntity classCode="QUAL">
									<xsl:comment>C.3.1.KR.1: Medical Professional Details</xsl:comment>
									<code code="{krsendertype}" codeSystem="{$OidKRSenderType}" codeSystemVersion="1.0" DisplayName="Medical Professional Details"/>
								</asQualifiedEntity>
							</xsl:if>

							<!--A.3.4.e Sender Country Code-->
							<xsl:if test="string-length(sendercountrycode)> 0">
								<asLocatedEntity classCode="LOCE">
									<location classCode="COUNTRY" determinerCode="INSTANCE">
										<xsl:comment>C.3.4.5: Sender's CountryCode</xsl:comment>
										<code code="{sendercountrycode}" codeSystem="{$OidISOCountry}" DisplayName="Sender Country Code"/>
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
									<xsl:if test="senderorganization = 'PRIVACY'"><name nullFlavor="MSK"/></xsl:if>
									<xsl:comment>C.3.2:Sender’s Organisation</xsl:comment>
								</representedOrganization>
							</assignedEntity>
						</representedOrganization>
					</assignedEntity>
				</author>

			</controlActEvent>
		</subjectOf1>
	</xsl:template>

	<!-- C.4.r Literature Reference(s) (repeat as necessary)
	E2B(R2): element "literaturereference" inside "ichicsr/ichicsrbatchheader/ichicsrmessageheader/safetyreport"
	E2B(R3): element "reference"	-->

	<xsl:template match="literature">
		<xsl:if test="string-length(.) > 0">
			<xsl:variable name="positionLitRef">
				<xsl:value-of select="position()"/>
			</xsl:variable>
			<xsl:comment>C.4.r: Literature Reference(s) - (<xsl:value-of select="$positionLitRef" />)</xsl:comment>
			<reference typeCode="REFR">
				<document classCode="DOC" moodCode="EVN">
					<code code="{$literatureReference}" codeSystem="{$oidichreferencesource}" codeSystemVersion="{$ichoidC4rCLVersion}"/>
					<xsl:comment>C.4.r.2: Included Documents</xsl:comment>
					<xsl:if test="string-length(literaturedocuments) > 0">
						<text>
							<xsl:attribute name="mediaType">
								<xsl:value-of select="litmediatype"/>
							</xsl:attribute>
							<xsl:if test="string-length(litrepresentation) > 0">
								<xsl:attribute name="representation">
									<xsl:value-of select="litrepresentation"/>
								</xsl:attribute>
							</xsl:if>
							<xsl:choose>
								<xsl:when  test="string-length(litcompression) > 0">
									<xsl:attribute name="compression">
										<xsl:value-of select="litcompression"/>
									</xsl:attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:if test="litmediatype != 'text/plain'">
										<xsl:attribute name="compression">
											<xsl:value-of select="$ZIPSTREAM_COMPRESSION_ALGORITHM"/>
										</xsl:attribute>
									</xsl:if>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:value-of select="literaturedocuments"/>
						</text>
					</xsl:if>
					<xsl:comment>C.4.r.1: Literature Reference(s)</xsl:comment>
					<xsl:variable name="isNullFlavourLitRef">
						<xsl:call-template name="isNullFlavour">
							<xsl:with-param name="value" select="literaturereference"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:choose>
						<xsl:when test="$isNullFlavourLitRef = 'yes'">
							<xsl:variable name="NullFlavourWOSqBrcktC4r1">
								<xsl:call-template name="getNFValueWithoutSqBrckt">
									<xsl:with-param name="nfvalue" select="literaturereference"/>
								</xsl:call-template>
							</xsl:variable>
							<bibliographicDesignationText nullFlavor="{$NullFlavourWOSqBrcktC4r1}"/>
						</xsl:when>
						<xsl:otherwise>
							<bibliographicDesignationText>
								<xsl:value-of select="literaturereference"/>
							</bibliographicDesignationText>
						</xsl:otherwise>
					</xsl:choose>
				</document>
			</reference>
		</xsl:if>
	</xsl:template>


	<!-- C.5 Study Identification
	E2B(R2): element "studyidentification" inside "ichicsr/ichicsrbatchheader/ichicsrmessageheader/safetyreport"
	E2B(R3): element "researchStudy"
	-->


	<xsl:template match="studyidentification" mode="study">
		<xsl:comment>C.5: Study Identification</xsl:comment>
		<subjectOf1 typeCode="SBJ">
			<researchStudy classCode="CLNTRL" moodCode="EVN">

				<!-- C.5.3: Sponsor Study Number -->
				<xsl:if test="string-length(sponsorstudynumb) > 0">
					<xsl:comment>C.5.3: Sponsor Study Number </xsl:comment>
					<xsl:variable name="isNullFlavourSSN">
						<xsl:call-template name="isNullFlavour">
							<xsl:with-param name="value" select="sponsorstudynumb"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:choose>
						<xsl:when test="$isNullFlavourSSN = 'yes'">
							<xsl:variable name="NullFlavourWOSqBrcktC53">
								<xsl:call-template name="getNFValueWithoutSqBrckt">
									<xsl:with-param name="nfvalue" select="sponsorstudynumb"/>
								</xsl:call-template>
							</xsl:variable>
							<id nullFlavor="{$NullFlavourWOSqBrcktC53}"/>
						</xsl:when>
						<xsl:otherwise>
							<id extension="{sponsorstudynumb}" root="{$SponsorStudyNumber}"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>

				<!-- C.5.4: Study Type Where Reaction(s) / Event(s) Were Observed -->
				<xsl:if test="string-length(observestudytype) > 0">
					<xsl:comment>C.5.4: Study type in which the reaction(s)/event(s) were observed </xsl:comment>
					<code code="{observestudytype}" codeSystem="{$oidStudyType}"  codeSystemVersion="{$emaoidC54CLVersion}" />
				</xsl:if>

				<!-- C.5.2: Study Name -->
				<xsl:if test="string-length(studyname) > 0">
					<xsl:comment>C.5.2: Study Name</xsl:comment>
					<xsl:variable name="isNullFlavourSN">
						<xsl:call-template name="isNullFlavour">
							<xsl:with-param name="value" select="studyname"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:choose>
						<xsl:when test="$isNullFlavourSN = 'yes'">
							<xsl:variable name="NullFlavourWOSqBrcktC52">
								<xsl:call-template name="getNFValueWithoutSqBrckt">
									<xsl:with-param name="nfvalue" select="studyname"/>
								</xsl:call-template>
							</xsl:variable>
							<title nullFlavor="{$NullFlavourWOSqBrcktC52}"/>
						</xsl:when>
						<xsl:otherwise>
							<title>
								<xsl:value-of select="studyname"/>
							</title>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>

				<!-- C.5.1.r Study Registration (Repeat as necessary) -->
				<xsl:apply-templates select="studyregistration" mode="EMA-primary-source"/>

			</researchStudy>
		</subjectOf1>
	</xsl:template>

	<!-- C.5.1.r Study Registration (Repeat as necessary) -->
	<xsl:template match="studyregistration" mode="EMA-primary-source">
		<xsl:if test="string-length(studyregnumb) > 0">
			<xsl:variable name="positionStudyReg">
				<xsl:value-of select="position()"/>
			</xsl:variable>
			<xsl:comment>C.5.1.r: Study Registration - (<xsl:value-of select="$positionStudyReg" />)</xsl:comment>
			<authorization typeCode="AUTH">
				<studyRegistration classCode="ACT" moodCode="EVN">

					<xsl:comment>C.5.1.r.1: Study Registration Number </xsl:comment>
					<xsl:variable name="isNullFlavourSudyReg">
						<xsl:call-template name="isNullFlavour">
							<xsl:with-param name="value" select="studyregnumb"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:choose>
						<xsl:when test="$isNullFlavourSudyReg = 'yes'">
							<xsl:variable name="NullFlavourWOSqBrcktC51r1">
								<xsl:call-template name="getNFValueWithoutSqBrckt">
									<xsl:with-param name="nfvalue" select="studyregnumb"/>
								</xsl:call-template>
							</xsl:variable>
							<id nullFlavor="{$NullFlavourWOSqBrcktC51r1}" root="{$StudyRegistrationNumber}"/>
						</xsl:when>
						<xsl:otherwise>
							<id extension="{studyregnumb}" root="{$StudyRegistrationNumber}"/>
						</xsl:otherwise>
					</xsl:choose>

					<author typeCode="AUT">
						<territorialAuthority classCode="TERR">
							<governingPlace classCode="COUNTRY" determinerCode="INSTANCE">

								<xsl:comment>C.5.1.r.2: Study Registration Country </xsl:comment>
								<xsl:variable name="isNullFlavourSudyRegCntry">
									<xsl:call-template name="isNullFlavour">
										<xsl:with-param name="value" select="studyregcountry"/>
									</xsl:call-template>
								</xsl:variable>
								<xsl:choose>
									<xsl:when test="$isNullFlavourSudyRegCntry = 'yes'">
										<xsl:variable name="NullFlavourWOSqBrcktC51r2">
											<xsl:call-template name="getNFValueWithoutSqBrckt">
												<xsl:with-param name="nfvalue" select="studyregcountry"/>
											</xsl:call-template>
										</xsl:variable>
										<code nullFlavor="{$NullFlavourWOSqBrcktC51r2}"/>
									</xsl:when>
									<xsl:otherwise>
										<code code="{studyregcountry}" codeSystem="{$OidISOCountry}"/>
									</xsl:otherwise>
								</xsl:choose>

							</governingPlace>
						</territorialAuthority>
					</author>
				</studyRegistration>
			</authorization>
		</xsl:if>
	</xsl:template>

	<!--Patient (identification) :
E2B(R2): element "patient"
E2B(R3): element "role"	E2B(R2): element "medicalhistoryepisode"
E2B(R3): element "role"
-->
	<xsl:template match="patient" mode="identification">
		<!--  <component typeCode="COMP"> -->
		<xsl:comment>D: Patient Characteristics</xsl:comment>
		<component typeCode="COMP">
			<adverseEventAssessment classCode="INVSTG" moodCode="EVN">
				<subject1 typeCode="SBJ">
					<primaryRole classCode="INVSBJ">
						<player1 classCode="PSN" determinerCode="INSTANCE">
							<!-- D.1 Patient (name or initials) -->
							<xsl:if test="string-length(patientinitial) > 0">
								<xsl:comment>D.1: Patient (name or initials)</xsl:comment>
								<xsl:call-template name="field-or-mask">
									<xsl:with-param name="element">name</xsl:with-param>
									<xsl:with-param name="value" select="patientinitial"/>
								</xsl:call-template>
							</xsl:if>

							<!-- D.5 Patient Sex -->
							<xsl:if test="string-length(patientsex) > 0">
								<xsl:comment>D.5: Patient Sex</xsl:comment>
								<xsl:call-template name="gender">
									<xsl:with-param name="value" select="patientsex"/>
								</xsl:call-template>
							</xsl:if>

							<!-- D.2.1 Date of Birth -->
							<xsl:if test="string-length(patientbirthdate) > 0">
								<xsl:comment>D.2.1: Date of Birth</xsl:comment>
								<xsl:call-template name="attribute-value-or-mask">
									<xsl:with-param name="element">birthTime</xsl:with-param>
									<xsl:with-param name="value" select="patientbirthdate"/>
								</xsl:call-template>
							</xsl:if>

							<!-- D.9.1 Date of Death -->
							<xsl:if test="string-length(patientdeathdate) > 0">
								<xsl:comment>D.9.1: Date of Death</xsl:comment>
								<xsl:call-template name="attribute-value-or-mask">
									<xsl:with-param name="element">deceasedTime</xsl:with-param>
									<xsl:with-param name="value" select="patientdeathdate"/>
								</xsl:call-template>
							</xsl:if>

							<!-- D.1.1.1 Patient Medical Record Number(s) and Source(s) of the Record Number (GP Medical Record Number) -->
							<xsl:if test="string-length(patientgpmedicalrecordnumb) > 0">
								<xsl:comment>D.1.1.1: Patient Medical Record Number(s) and Source(s) of the Record Number (GP Medical Record Number)</xsl:comment>
								<xsl:call-template name="patient-record">
									<xsl:with-param name="value" select="patientgpmedicalrecordnumb"/>
									<xsl:with-param name="root" select="$oidGPMedicalRecordNumber"/>
									<xsl:with-param name="code" select="$GPMrn"/>
									<xsl:with-param name="codeSystem" select="$oidSourceMedicalRecord"/>
								</xsl:call-template>
							</xsl:if>

							<!-- D.1.1.2 Patient Medical Record Number(s) and Source(s) of the Record Number (Specialist Record Number) -->
							<xsl:if test="string-length(patientspecialistrecordnumb) > 0">
								<xsl:comment>D.1.1.2: Patient Medical Record Number(s) and Source(s) of the Record Number (Specialist Record Number)</xsl:comment>
								<xsl:call-template name="patient-record">
									<xsl:with-param name="value" select="patientspecialistrecordnumb"/>
									<xsl:with-param name="root" select="$oidSpecialistRecordNumber"/>
									<xsl:with-param name="code" select="$SpecialistMrn"/>
									<xsl:with-param name="codeSystem" select="$oidSourceMedicalRecord"/>
								</xsl:call-template>
							</xsl:if>

							<!-- D.1.1.3 Patient Medical Record Number(s) and Source(s) of the Record Number (Hospital Record Number) -->
							<xsl:if test="string-length(patienthospitalrecordnumb) > 0">
								<xsl:comment>D.1.1.3: Patient Medical Record Number(s) and Source(s) of the Record Number (Hospital Record Number)</xsl:comment>
								<xsl:call-template name="patient-record">
									<xsl:with-param name="value" select="patienthospitalrecordnumb"/>
									<xsl:with-param name="root" select="$oidHospitalRecordNumber"/>
									<xsl:with-param name="code" select="$HospitalMrn"/>
									<xsl:with-param name="codeSystem" select="$oidSourceMedicalRecord"/>
								</xsl:call-template>
							</xsl:if>

							<!-- D.1.1.4 Patient Medical Record Number(s) and Source(s) of the Record Number (Investigation Number) -->
							<xsl:if test="string-length(patientinvestigationnumb) > 0">
								<xsl:comment>D.1.1.4: Patient Medical Record Number(s) and Source(s) of the Record Number (Investigation Number) </xsl:comment>
								<xsl:call-template name="patient-record">
									<xsl:with-param name="value" select="patientinvestigationnumb"/>
									<xsl:with-param name="root" select="$oidInvestigationNumber"/>
									<xsl:with-param name="code" select="$Investigation"/>
									<xsl:with-param name="codeSystem" select="$oidSourceMedicalRecord"/>
								</xsl:call-template>
							</xsl:if>

							<!--B.1.10 - Parent-->
							<xsl:apply-templates select="parent" mode="identification"/>

						</player1>

						<!--A.5 - Study-->
						<xsl:apply-templates select="../studyidentification" mode="study"/>

						<!--B.1 - Patient-->
						<xsl:apply-templates select="." mode="characteristics"/>

						<!--B.1.8 - Patient Past Drug Therapy-->

						<!--B.1.9 - Patient Death-->
						<!-- <xsl:apply-templates select="reportedcauseofdeath"/> -->
						<!--B.2.i - Reaction-->
						<xsl:apply-templates select="../reaction"/>
						<!--B.3.r - Test-->
						<!--<xsl:if test="string-length(testdate) > 0 or string-length(testname) > 0 or string-length(resultstestsprocedures) > 0">-->
						<subjectOf2 typeCode="SBJ">
							<organizer classCode="CATEGORY" moodCode="EVN">
								<code code="{$TestsAndProceduresRelevantToTheInvestigation}" codeSystem="{$oidValueGroupingCode}" displayName="testsAndProceduresRelevantToTheInvestigation"/>
								<xsl:apply-templates select="../test" mode="EMA-lab-test"/>
								<xsl:apply-templates select="resulttestprocedures"/>

							</organizer>
						</subjectOf2>
						<!--</xsl:if>-->


						<!--B.4.k - Drug (main)-->
						<subjectOf2 typeCode="SBJ">
							<organizer classCode="CATEGORY" moodCode="EVN">
								<code code="{$DrugInformation}" codeSystem="{$oidValueGroupingCode}" displayName="drugInformation"/>
								<xsl:apply-templates select="../drug" mode="main"/>
							</organizer>
						</subjectOf2>
					</primaryRole>
				</subject1>
				<!--B.4.k - Drug (causality)-->
				<xsl:apply-templates select="../drug" mode="EMA-causality"/>

				<!--B.5 - Summary-->
				<xsl:apply-templates select="../summary"/>
			</adverseEventAssessment>
		</component>
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
				<xsl:when test="string-length(parentidentification) > 0 or string-length(parentsex) > 0 or string-length(parentbirthdate) > 0">
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
						<code code="{$Age}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
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
						<code code="{$BodyWeight}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
						<value xsi:type="PQ" value="{parentweight}" unit="kg"/>
					</observation>
				</subjectOf2>
			</xsl:if>

			<!-- D.10.5 Height (cm) of Parent -->
			<xsl:if test="string-length(parentheight) > 0">
				<xsl:comment>D.10.5: Height (cm) of Parent</xsl:comment>
				<subjectOf2 typeCode="SBJ">
					<observation moodCode="EVN" classCode="OBS">
						<code code="{$Height}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
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
						<code code="{$LastMenstrualPeriodDate}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
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
						<code code="{$RelevantMedicalHistoryAndConcurrentConditions}" codeSystem="{$oidValueGroupingCode}" codeSystemVersion="{$emaValueGroupingCLVersion}"/>


						<!-- D.10.7.1.r Structured Information of Parent (repeat as necessary) -->
						<xsl:apply-templates select="parentmedicalhistoryepisode" mode="EMA-par-structured-info"/>

						<!-- D.10.7.2 Text for Relevant Medical History and Concurrent Conditions of Parent -->
						<xsl:if test="string-length(parentmedicalrelevanttext) > 0">
							<xsl:comment>D.10.7.2: Text for relevant medical history and concurrent conditions  of parent (not including reaction/event)</xsl:comment>
							<component typeCode="COMP">
								<observation moodCode="EVN" classCode="OBS">
									<code code="{$HistoryAndConcurrentConditionText}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
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
						<code code="{$DrugHistory}" codeSystem="{$oidValueGroupingCode}" codeSystemVersion="{$emaValueGroupingCLVersion}"/>
						<xsl:apply-templates select="parentpastdrugtherapy" mode="EMA-par-past-drug-hist"/>
					</organizer>
				</subjectOf2>
			</xsl:if>

		</role>
	</xsl:template>


	<!-- D.10.7.1.r Structured Information of Parent (repeat as necessary)
	E2B(R2): element "parentmedicalhistoryepisode" - "ichicsr\ichicsrbatchheader\ichicsrmessageheader\safetyreport\patient\parent\parentmedicalhistoryepisode"
	E2B(R3): element "role"
	-->
	<xsl:template match="parentmedicalhistoryepisode" mode="EMA-par-structured-info">
		<xsl:variable name="positionParMedHist">
			<xsl:value-of select="position()"/>
		</xsl:variable>
		<xsl:comment>D.10.7.1.r: Medical History (disease / surgical procedure / etc.) (MedDRA code) - (<xsl:value-of select="$positionParMedHist"/>)</xsl:comment>
		<component typeCode="COMP">
			<observation moodCode="EVN" classCode="OBS">
				<xsl:comment>D.10.7.1.r.1a: MedDRA version for parent medical history </xsl:comment>
				<xsl:comment>D.10.7.1.r.1b: Medical History (disease / surgical procedure/ etc.) (MedDRA code)</xsl:comment>
				<code code="{parentmedicalepisodename}" codeSystemVersion="{parentmdepisodemeddraversion}" codeSystem="{$oidMedDRA}"/>

				<!-- B.1.10.7.1r.cd Start Date and End Date -->
				<xsl:if test="string-length(parentmedicalstartdate) > 0 or string-length(parentmedicalenddate) > 0">
					<effectiveTime xsi:type="IVL_TS">
						<!--D.10.7.1.r.2 Start Date-->
						<xsl:if test="string-length(parentmedicalstartdate) > 0">
							<xsl:comment>D.10.7.1.r.2: Start Date</xsl:comment>
							<xsl:call-template name="effectiveTime">
								<xsl:with-param name="element">low</xsl:with-param>
								<xsl:with-param name="value" select="parentmedicalstartdate"/>
							</xsl:call-template>
						</xsl:if>

						<!--D.10.7.1.r.4 End Date-->
						<xsl:if test="string-length(parentmedicalenddate) > 0">
							<xsl:comment>D.10.7.1.r.4: End Date</xsl:comment>
							<xsl:call-template name="effectiveTime">
								<xsl:with-param name="element">high</xsl:with-param>
								<xsl:with-param name="value" select="parentmedicalenddate"/>
							</xsl:call-template>
						</xsl:if>

					</effectiveTime>
				</xsl:if>

				<!-- D.10.7.1.r.5 Comments -->
				<xsl:if test="string-length(parentmedicalcomment) > 0">
					<xsl:comment>D.10.7.1.r.5: Comments</xsl:comment>
					<outboundRelationship2 typeCode="COMP">
						<observation moodCode="EVN" classCode="OBS">
							<code code="{$Comment}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
							<value xsi:type="ED">
								<xsl:value-of select="parentmedicalcomment"/>
							</value>
						</observation>
					</outboundRelationship2>
				</xsl:if>

				<!-- D.10.7.1.r.3 Continuing -->
				<xsl:if test="string-length(parentmedicalcontinue) > 0">
					<xsl:comment>D.10.7.1.r.3: Continuing</xsl:comment>
					<xsl:variable name="isNullFlavourParMedContinue">
						<xsl:call-template name="isNullFlavour">
							<xsl:with-param name="value" select="parentmedicalcontinue"/>
						</xsl:call-template>
					</xsl:variable>
					<inboundRelationship typeCode="REFR">
						<observation moodCode="EVN" classCode="OBS">
							<code code="{$Continuing}"  codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
							<xsl:choose>
								<xsl:when test="$isNullFlavourParMedContinue = 'yes'">
									<xsl:variable name="NullFlavourWOSqBrcktD1071r3">
										<xsl:call-template name="getNFValueWithoutSqBrckt">
											<xsl:with-param name="nfvalue" select="parentmedicalcontinue"/>
										</xsl:call-template>
									</xsl:variable>
									<value xsi:type="BL" nullFlavor="{$NullFlavourWOSqBrcktD1071r3}"/>
								</xsl:when>
								<xsl:otherwise>
									<value xsi:type="BL" value="{parentmedicalcontinue}"/>
								</xsl:otherwise>
							</xsl:choose>
						</observation>
					</inboundRelationship>
				</xsl:if>

			</observation>
		</component>
	</xsl:template>


	<xsl:template match="parentpastdrugtherapy" mode="EMA-par-past-drug-hist">
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
					<xsl:comment>D.10.8.r.1: Name of Drug as Reported-</xsl:comment>
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
										<name nullFlavor="{$NullFlavourWOSqBrcktParDrugName}"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:choose>
											<xsl:when test="(string-length(krparentmedicalproductversion) > 0 and string-length(krparentmedicalproductid) > 0)">
												<xsl:comment>D.10.8.r.1.KR.1a: Medicinal Product Version</xsl:comment>
												<xsl:comment>D.10.8.r.1.KR.1b: Medicinal Product ID</xsl:comment>
												<code code="{krparentmedicalproductid}" codeSystem="{$OIdKRDrugForeign}" codeSystemVersion="{krparentmedicalproductversion}"/>
											</xsl:when>
											<xsl:when test="(string-length(krparentmedicalproductid) > 0)">
												<xsl:comment>D.10.8.r.1.KR.1b: Medicinal Product ID</xsl:comment>
												<code code="{krparentmedicalproductid}" codeSystem="{$OIdKRDrugDomestic}"/>
											</xsl:when>
											<xsl:when test="string-length(parentmpidversion) > 0 and string-length(parentmpid) > 0"  >
												<xsl:comment>D.10.8.r.2a: MPID Version Date / Number</xsl:comment>
												<xsl:comment>D.10.8.r.2b: Medicinal Product Identifier (MPID)</xsl:comment>
												<code code="{parentmpid}" codeSystem="MPID" codeSystemVersion="{parentmpidversion}"/>
											</xsl:when>
											<xsl:when test="string-length(parentphpidversion) > 0 and string-length(parentphpid) > 0"  >
												<xsl:comment>D.10.8.r.3a: PhPID Version Date/Number</xsl:comment>
												<xsl:comment>D.10.8.r.3b: Pharmaceutical Product Identifier (PhPID)</xsl:comment>
												<code code="{parentphpid}" codeSystem="PhPID" codeSystemVersion="{parentphpidversion}"/>
											</xsl:when>
										</xsl:choose>

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
							<code code="{$Indication}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}" displayName="Parent Past indication"/>
							<value xsi:type="CE" code="{parentdrgindication}" codeSystemVersion="{parentdrgindicationmeddraversion}" codeSystem="{$oidMedDRA}"/>
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
							<code code="{$Reaction}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}" displayName="Parent Past Reaction"/>
							<value xsi:type="CE" code="{parentdrgreaction}" codeSystemVersion="{parentdrgreactionmeddraversion}" codeSystem="{$oidMedDRA}"/>
						</observation>
					</outboundRelationship2>
				</xsl:if>

			</substanceAdministration>
		</component>
	</xsl:template>



	<!-- Parent Past Drug Therapy - Substance / Specified Substance Identifier and Strength (repeat as necessary)
	E2B(R2): element "PARENTDRUGSUBSTANCEINFO" -  "ichicsr\ichicsrbatchheader\ichicsrmessageheader\safetyreport\patient\parent\parentpastdrugtherapy\parentdrugsubstanceinfo"
	E2B(R3): element "primaryRole"
	-->
	<xsl:template match="parenteudrugsubstanceinfo" mode="EMA-parent-past-drug-substance-info">
		<xsl:variable name="positionParPastDrugSubInfo">
			<xsl:value-of select="position()"/>
		</xsl:variable>
		<xsl:comment>Parent Past Drug Therapy - Substance / Specified Substance Identifier and Strength - (<xsl:value-of select="$positionParPastDrugSubInfo"/>)</xsl:comment>
		<ingredient classCode="ACTI">
			<xsl:if test="string-length(parentdrgsubstancestrength) > 0 and string-length(parentdrgsubstancestrengthunit) > 0">
				<quantity>
					<xsl:comment>D.10.8.r.EU.r.3a: Strength (number)</xsl:comment>
					<xsl:comment>D.10.8.r.EU.r.3b: Strength (unit)</xsl:comment>
					<xsl:element name="numerator">
						<xsl:attribute name="value">
							<xsl:value-of select="parentdrgsubstancestrength" />
						</xsl:attribute>
						<xsl:attribute name="unit">
							<xsl:call-template name="getMapping">
								<xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="parentdrgsubstancestrengthunit"/>
							</xsl:call-template>
						</xsl:attribute>
					</xsl:element>
					<!--	<numerator value="{parentdrgsubstancestrength}" unit="{parentdrgsubstancestrengthunit}"/> -->
					<denominator value="1"/>
				</quantity>
			</xsl:if>

			<xsl:if test="(string-length(parentdrgsubstancename) > 0) or (string-length(parentdrgsubstancetermid) > 0 and string-length(parentdrgsubstancetermidversion) > 0)">
				<ingredientSubstance classCode="MMAT" determinerCode="KIND">
					<xsl:if test="string-length(parentdrgsubstancetermid) > 0 and string-length(parentdrgsubstancetermidversion) > 0">
						<xsl:comment>D.10.8.r.EU.r.2a: Substance / Specified Substance TermID Version Date / Number</xsl:comment>
						<xsl:comment>D.10.8.r.EU.r.2b: Substance / Specified Substance TermID</xsl:comment>
						<code code="{parentdrgsubstancetermid}" codeSystemVersion="{parentdrgsubstancetermidversion}" codeSystem="{$oidMessageType}"/>
					</xsl:if>

					<xsl:if test="string-length(parentdrgsubstancename) > 0">
						<name>
							<xsl:comment>D.10.8.r.EU.r.1: Substance / Specified Substance Name</xsl:comment>
							<xsl:value-of select="parentdrgsubstancename"/>
						</name>
					</xsl:if>
				</ingredientSubstance>
			</xsl:if>
		</ingredient>
	</xsl:template>



	<xsl:template match="patient" mode="characteristics">
		<!--B.1.2.2.ab Age at time of onset of reaction/event - Rule COD-10-->
		<xsl:if test="string-length(patientonsetage) > 0">
			<subjectOf2 typeCode="SBJ">
				<observation moodCode="EVN" classCode="OBS">
					<code code="{$Age}" codeSystem="{$oidObservationCode}" displayName="age"/>
					<value xsi:type="PQ" value="{patientonsetage}" unit="{patientonsetageunit}" />
					<!-- <xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="patientonsetageunit"/></xsl:call-template></xsl:attribute>
</value> -->
					<xsl:comment>D.2.2a: Age at Time of Onset of Reaction / Event (number) </xsl:comment>
					<xsl:comment>D.2.2b: Age at Time of Onset of Reaction / Event (unit) </xsl:comment>
				</observation>
			</subjectOf2>
		</xsl:if>

		<!--B.1.2.2.1.ab Gestation Period-->
		<xsl:if test="string-length(gestationperiod) > 0">
			<subjectOf2 typeCode="SBJ">
				<observation moodCode="EVN" classCode="OBS">
					<code code="{$GestationPeriod}" codeSystem="{$oidObservationCode}" displayName="gestationPeriod"/>
					<value xsi:type="PQ" value="{gestationperiod}">
						<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="gestationperiodunit"/></xsl:call-template></xsl:attribute>
					</value>
					<xsl:comment>D.2.2.1a: Gestation Period When Reaction / Event Was Observed in the Foetus (number)</xsl:comment>
					<xsl:comment>D.2.2.1b: Gestation Period When Reaction / Event Was Observed in the Foetus (unit) </xsl:comment>
				</observation>
			</subjectOf2>
		</xsl:if>
		<!--B.1.2.3. Age Group-->
		<xsl:if test="string-length(patientagegroup)>0">
			<subjectOf2 typeCode="SBJ">
				<observation moodCode="EVN" classCode="OBS">
					<code code="{$AgeGroup}" codeSystem="{$oidObservationCode}" displayName="ageGroup"/>
					<xsl:choose>
						<xsl:when test="(patientagegroup)= 0"><value xsi:type="CE" code="{patientagegroup}" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Foetus"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 1"><value xsi:type="CE" code="{patientagegroup}" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Neonate(PretermandTermnewborns)"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 2"><value xsi:type="CE" code="{patientagegroup}" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Infant"/> </xsl:when>
						<xsl:when test="(patientagegroup)=3"><value xsi:type="CE" code="{patientagegroup}" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Child"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 4"><value xsi:type="CE" code="{patientagegroup}" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Adolescent"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 5"><value xsi:type="CE" code="{patientagegroup}" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Adult"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 6"><value xsi:type="CE" code="{patientagegroup}" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Elderly"/> </xsl:when>

						<xsl:when test="(patientagegroup)= 01"><value xsi:type="CE" code="1" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Neonate(PretermandTermnewborns)"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 02"><value xsi:type="CE" code="2" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Infant"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 03"><value xsi:type="CE" code="3" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Child"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 04"><value xsi:type="CE" code="4" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Adolescent"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 05"><value xsi:type="CE" code="5" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Adult"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 06"><value xsi:type="CE" code="6" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Elderly"/> </xsl:when>

						<xsl:when test="(patientagegroup)= 001"><value xsi:type="CE" code="1" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Neonate(PretermandTermnewborns)"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 002"><value xsi:type="CE" code="2" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Infant"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 003"><value xsi:type="CE" code="3" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Child"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 004"><value xsi:type="CE" code="4" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Adolescent"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 005"><value xsi:type="CE" code="5" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Adult"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 006"><value xsi:type="CE" code="6" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Elderly"/> </xsl:when>

						<xsl:when test="(patientagegroup)= 'Foetus'"><value xsi:type="CE" code="0" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Foetus"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'Neonate (Preterm and Term newborns)'"><value xsi:type="CE" code="1" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Neonate(PretermandTermnewborns)"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'Infant'"><value xsi:type="CE" code="2" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Infant"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'Child'"><value xsi:type="CE" code="3" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Child"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'Adolescent'"><value xsi:type="CE" code="4" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Adolescent"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'Adult'"><value xsi:type="CE" code="5" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Adult"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'Elderly'"><value xsi:type="CE" code="6" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Elderly"/> </xsl:when>

						<xsl:when test="(patientagegroup)= 'FOETUS'"><value xsi:type="CE" code="0" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Foetus"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'NEONATE (PRETERM AND TERM NEWBORNS)'"><value xsi:type="CE" code="1" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Neonate(PretermandTermnewborns)"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'INFANT'"><value xsi:type="CE" code="2" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Infant"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'CHILD'"><value xsi:type="CE" code="3" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Child"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'ADOLESCENT'"><value xsi:type="CE" code="4" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Adolescent"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'ADULT'"><value xsi:type="CE" code="5" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Adult"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'ELDERLY'"><value xsi:type="CE" code="6" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Elderly"/> </xsl:when>

						<xsl:when test="(patientagegroup)= 'foetus'"><value xsi:type="CE" code="0" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Foetus"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'neonate (preterm and term newborns)'"><value xsi:type="CE" code="1" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Neonate(PretermandTermnewborns)"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'infant'"><value xsi:type="CE" code="2" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Infant"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'child'"><value xsi:type="CE" code="3" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Child"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'adolescent'"><value xsi:type="CE" code="4" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Adolescent"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'adult'"><value xsi:type="CE" code="5" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Adult"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'elderly'"><value xsi:type="CE" code="6" codeSystem="{$oidAgeGroup}" codeSystemVersion="{patientagegroupcsv}" displayName="Elderly"/> </xsl:when>

						<xsl:when test="(patientagegroup)= 'MSK'"><value xsi:type="CE" nullFlavor="MSK" displayName="DataMasked"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'UNK'"><value xsi:type="CE" nullFlavor="UNK" displayName="DataUnknowntoSender"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'ASKU'"><value xsi:type="CE" nullFlavor="ASKU" displayName="DataAskedbutUnknowntoSender"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'NI'"><value xsi:type="CE" nullFlavor="NI" displayName="NoInformationAvailablewithSender"/> </xsl:when>

						<xsl:when test="(patientagegroup)= 'msk'"><value xsi:type="CE" nullFlavor="MSK" displayName="DataMasked"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'unk'"><value xsi:type="CE" nullFlavor="UNK" displayName="DataUnknowntoSender"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'asku'"><value xsi:type="CE" nullFlavor="ASKU" displayName="DataAskedbutUnknowntoSender"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'ni'"><value xsi:type="CE" nullFlavor="NI" displayName="NoInformationAvailablewithSender"/> </xsl:when>

						<xsl:when test="(patientagegroup)= 'Msk'"><value xsi:type="CE" nullFlavor="MSK" displayName="DataMasked"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'Unk'"><value xsi:type="CE" nullFlavor="UNK" displayName="DataUnknowntoSender"/> </xsl:when>
						<xsl:when test="(patientagegroup)= 'Asku'"><value xsi:type="CE" nullFlavor="ASKU" displayName="DataAskedbutUnknowntoSender"/> </xsl:when>

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

		<!-- D.6: Last Menstrual Period Date -->
		<xsl:if test="string-length(patientlastmenstrualdate) > 0">
			<xsl:comment>D.6: Last Menstrual Period Date</xsl:comment>
			<xsl:variable name="isNullFlavourLMP">
				<xsl:call-template name="isNullFlavour">
					<xsl:with-param name="value" select="patientlastmenstrualdate"/>
				</xsl:call-template>
			</xsl:variable>
			<subjectOf2 typeCode="SBJ">
				<observation moodCode="EVN" classCode="OBS">
					<code code="{$LastMenstrualPeriodDate}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
					<xsl:choose>
						<xsl:when test="$isNullFlavourLMP = 'yes'">
							<xsl:variable name="NullFlavourWOSqBrcktD6">
								<xsl:call-template name="getNFValueWithoutSqBrckt">
									<xsl:with-param name="nfvalue" select="patientlastmenstrualdate"/>
								</xsl:call-template>
							</xsl:variable>
							<value xsi:type="TS" nullFlavor="{$NullFlavourWOSqBrcktD6}"/>
						</xsl:when>
						<xsl:otherwise>
							<value xsi:type="TS" value="{patientlastmenstrualdate}"/>
						</xsl:otherwise>
					</xsl:choose>
				</observation>
			</subjectOf2>
		</xsl:if>


		<!-- D.7  -->
		<!-- <xsl:apply-templates select="." mode="EMA-pat-characteristics"/> -->
		<xsl:if test="count(medicalhistoryepisode) > 0 or string-length(patientmedicalhistorytext) > 0">
			<subjectOf2 typeCode="SBJ">
				<organizer classCode="CATEGORY" moodCode="EVN">
					<code code="{$RelevantMedicalHistoryAndConcurrentConditions}" codeSystem="{$oidValueGroupingCode}" codeSystemVersion="{$emaValueGroupingCLVersion}"/>

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
								<code code="{$HistoryAndConcurrentConditionText}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
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
								<code code="{$ConcommitantTherapy}" codeSystem="{$oidConcomitantTherapies}" codeSystemVersion="{$emaoidD73CLVersion}"/>
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
					<code code="{$DrugHistory}" codeSystem="{$oidValueGroupingCode}" codeSystemVersion="{$emaValueGroupingCLVersion}"/>
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
					<code code="{$Autopsy}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
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

	<xsl:template match="medicalhistoryepisode" mode="EMA-pat-medical-history-episode">
		<xsl:variable name="positionPatMedEpi">
			<xsl:value-of select="position()"/>
		</xsl:variable>
		<xsl:comment>D.7.1.r: Relevant Medical History and Concurrent Conditions (not including reaction / event) - (<xsl:value-of select="$positionPatMedEpi"/>)</xsl:comment>
		<component typeCode="COMP">
			<observation moodCode="EVN" classCode="OBS">
				<!--D.7.1.r.1a MedDRA Version for Medical History -->
				<!--D.7.1.r.1b Medical History (disease / surgical procedure / etc.) (MedDRA code) -->
				<xsl:comment>D.7.1.r.1a: MedDRA Version for Medical History</xsl:comment>
				<xsl:comment>D.7.1.r.1b: Medical History (disease / surgical procedure / etc.) (MedDRA code)</xsl:comment>
				<code code="{patientepisodename}" codeSystemVersion="{patientepisodenamemeddraversion}" codeSystem="{$oidMedDRA}"/>

				<xsl:if test="string-length(patientmedicalstartdate) > 0 or string-length(patientmedicalenddate) > 0">
					<effectiveTime xsi:type="IVL_TS">
						<!--D.7.1.r.2 Start Date-->
						<xsl:if test="string-length(patientmedicalstartdate) > 0">
							<xsl:comment>D.7.1.r.2: Start Date</xsl:comment>
							<xsl:call-template name="effectiveTime">
								<xsl:with-param name="element">low</xsl:with-param>
								<xsl:with-param name="value" select="patientmedicalstartdate"/>
							</xsl:call-template>
						</xsl:if>

						<!--D.7.1.r.4 End Date-->
						<xsl:if test="string-length(patientmedicalenddate) > 0">
							<xsl:comment>D.7.1.r.4: End Date</xsl:comment>
							<xsl:call-template name="effectiveTime">
								<xsl:with-param name="element">high</xsl:with-param>
								<xsl:with-param name="value" select="patientmedicalenddate"/>
							</xsl:call-template>
						</xsl:if>

					</effectiveTime>
				</xsl:if>

				<!-- D.7.1.r.5 Comments -->
				<xsl:if test="string-length(patientmedicalcomment) > 0">
					<xsl:comment>D.7.1.r.5: Comments</xsl:comment>
					<outboundRelationship2 typeCode="COMP">
						<observation moodCode="EVN" classCode="OBS">
							<code code="{$Comment}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
							<value xsi:type="ED">
								<xsl:value-of select="patientmedicalcomment"/>
							</value>
						</observation>
					</outboundRelationship2>
				</xsl:if>

				<!-- D.7.1.r.6: Family History -->
				<xsl:if test="string-length(patientfamilyhistory) > 0">
					<xsl:comment>D.7.1.r.6: Family History</xsl:comment>
					<outboundRelationship2 typeCode="EXPL">
						<observation classCode="OBS" moodCode="EVN">
							<code code="{$FamilyHistory}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaoidD71r6CLVersion}"/>
							<value xsi:type="BL" value="{patientfamilyhistory}"/>
						</observation>
					</outboundRelationship2>
				</xsl:if>

				<!-- D.7.1.r.3 Continuing -->
				<xsl:if test="string-length(patientmedicalcontinue) > 0">
					<xsl:comment>D.7.1.r.3: Continuing</xsl:comment>
					<xsl:variable name="isNullFlavourPatMedContinue">
						<xsl:call-template name="isNullFlavour">
							<xsl:with-param name="value" select="patientmedicalcontinue"/>
						</xsl:call-template>
					</xsl:variable>
					<inboundRelationship typeCode="REFR">
						<observation moodCode="EVN" classCode="OBS">
							<code code="{$Continuing}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
							<xsl:choose>
								<xsl:when test="$isNullFlavourPatMedContinue = 'yes'">
									<xsl:variable name="NullFlavourWOSqBrcktD71r3">
										<xsl:call-template name="getNFValueWithoutSqBrckt">
											<xsl:with-param name="nfvalue" select="patientmedicalcontinue"/>
										</xsl:call-template>
									</xsl:variable>
									<value xsi:type="BL" nullFlavor="{$NullFlavourWOSqBrcktD71r3}"/>
								</xsl:when>
								<xsl:otherwise>
									<value xsi:type="BL" value="{patientmedicalcontinue}"/>
								</xsl:otherwise>
							</xsl:choose>
						</observation>
					</inboundRelationship>
				</xsl:if>
			</observation>
		</component>
	</xsl:template>


	<!-- Populating the value in an element and handling null flavour -->
	<xsl:template name="field-or-mask">
		<xsl:param name="element"/>
		<xsl:param name="value"/>
		<xsl:if test="string-length($value) > 0">
			<xsl:element name="{$element}">
				<xsl:variable name="isNullFlavourMask">
					<xsl:call-template name="isNullFlavour">
						<xsl:with-param name="value" select="$value"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:choose>
					<xsl:when test="$isNullFlavourMask = 'yes'">
						<xsl:variable name="NullFlavourWOSqBrckt">
							<xsl:call-template name="getNFValueWithoutSqBrckt">
								<xsl:with-param name="nfvalue" select="$value"/>
							</xsl:call-template>
						</xsl:variable>
						<xsl:attribute name="nullFlavor">
							<xsl:value-of select="$NullFlavourWOSqBrckt"/>
						</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$value"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:element>
		</xsl:if>
	</xsl:template>


	<!-- Populating the value in an element and handling null flavour for reaction start and end dates-->
	<xsl:template name="effectiveTime">
		<xsl:param name="element"/>
		<xsl:param name="value"/>
		<xsl:if test="string-length($value) > 0">
			<xsl:element name="{$element}">
				<xsl:variable name="isNullFlavourReactDate">
					<xsl:call-template name="isNullFlavour">
						<xsl:with-param name="value" select="$value"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:choose>
					<xsl:when test="$isNullFlavourReactDate = 'yes'">
						<xsl:variable name="NullFlavourWOSqBrcktEffectiveTime">
							<xsl:call-template name="getNFValueWithoutSqBrckt">
								<xsl:with-param name="nfvalue" select="$value"/>
							</xsl:call-template>
						</xsl:variable>
						<xsl:attribute name="nullFlavor">
							<xsl:value-of select="$NullFlavourWOSqBrcktEffectiveTime"/>
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

	<!-- Return yes if the value is null flavour, no otherwise -->
	<xsl:template name="isNullFlavour">
		<xsl:param name="value"/>
		<xsl:choose>
			<xsl:when test="string-length($value) > 0">
				<xsl:choose>
					<xsl:when test="$value = '[NA]'">yes</xsl:when>
					<xsl:when test="$value = '[NI]'">yes</xsl:when>
					<xsl:when test="$value = '[NASK]'">yes</xsl:when>
					<xsl:when test="$value = '[ASKU]'">yes</xsl:when>
					<xsl:when test="$value = '[MSK]'">yes</xsl:when>
					<xsl:when test="$value = '[UNK]'">yes</xsl:when>
					<xsl:when test="$value = '[NINF]'">yes</xsl:when>
					<xsl:when test="$value = '[PINF]'">yes</xsl:when>
					<xsl:otherwise>no</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>no</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Return NF value without [] -->
	<xsl:template name="getNFValueWithoutSqBrckt">
		<xsl:param name="nfvalue"/>
		<xsl:choose>
			<xsl:when test="string-length($nfvalue) > 0">
				<xsl:choose>
					<xsl:when test="$nfvalue = '[NA]'">NA</xsl:when>
					<xsl:when test="$nfvalue = '[NI]'">NI</xsl:when>
					<xsl:when test="$nfvalue = '[NASK]'">NASK</xsl:when>
					<xsl:when test="$nfvalue = '[ASKU]'">ASKU</xsl:when>
					<xsl:when test="$nfvalue = '[MSK]'">MSK</xsl:when>
					<xsl:when test="$nfvalue = '[UNK]'">UNK</xsl:when>
					<xsl:when test="$nfvalue = '[NINF]'">NINF</xsl:when>
					<xsl:when test="$nfvalue = '[PINF]'">PINF</xsl:when>
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
					<administrativeGenderCode nullFlavor="{$NullFlavourWOSqBrcktGender}"/>
				</xsl:when>
				<xsl:otherwise>
					<administrativeGenderCode code="{$value}" codeSystem="{$oidGenderCode}"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>


	<!-- Populating the value in an element and handling null flavour for reaction start and end dates-->
	<xsl:template name="attribute-value-or-mask">
		<xsl:param name="element"/>
		<xsl:param name="value"/>
		<xsl:if test="string-length($value) > 0">
			<xsl:element name="{$element}">
				<xsl:variable name="isNullFlavourAttVal">
					<xsl:call-template name="isNullFlavour">
						<xsl:with-param name="value" select="$value"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:choose>
					<xsl:when test="$isNullFlavourAttVal = 'yes'">
						<xsl:variable name="NullFlavourWOSqBrcktAttVal">
							<xsl:call-template name="getNFValueWithoutSqBrckt">
								<xsl:with-param name="nfvalue" select="$value"/>
							</xsl:call-template>
						</xsl:variable>
						<xsl:attribute name="nullFlavor">
							<xsl:value-of select="$NullFlavourWOSqBrcktAttVal"/>
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

	<xsl:template match="patientpastdrugtherapy" mode="EMA-pat-past-drug-hist">
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
					<xsl:comment>D.8.r.1: Name of Drug as Reported</xsl:comment>
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
										<name nullFlavor="{$NullFlavourWOSqBrcktD8r1}"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:choose>
											<xsl:when test="(string-length(krpatientmedicalproductid) > 0 and string-length(krpatientmedicalproductversion) > 0)">
												<xsl:comment>D.8.r.1.KR.1a: Medicinal Product Version</xsl:comment>
												<xsl:comment>D.8.r.1.KR.1b: Medicinal Product ID</xsl:comment>
												<code code="{krpatientmedicalproductid}" codeSystem="{$OIdKRDrugForeign}" codeSystemVersion="{krpatientmedicalproductversion}"/>
											</xsl:when>
											<xsl:when test="(string-length(krpatientmedicalproductid) > 0)">
												<xsl:comment>D.8.r.1.KR.1b: Medicinal Product ID</xsl:comment>
												<code code="{krpatientmedicalproductid}" codeSystem="{$OIdKRDrugDomestic}"/>
											</xsl:when>
											<xsl:when test="string-length(patientmpidversion) > 0 and string-length(patientmpid) > 0"  >
												<xsl:comment>D.8.r.2b: Medicinal Product Identifier (MPID)</xsl:comment>
												<xsl:comment>D.8.r.2a: MPID Version Date/Number</xsl:comment>
												<code code="{patientmpid}" codeSystem="MPID" codeSystemVersion="{patientmpidversion}"/>
											</xsl:when>
											<xsl:when test="string-length(patientphpidversion) > 0 and string-length(patientphpid) > 0"  >
												<xsl:comment>D.8.r.3a: PhPID Version Date/Number</xsl:comment>
												<xsl:comment>D.8.r.3b: Pharmaceutical Product Identifier (PhPID)</xsl:comment>
												<code code="{patientphpid}" codeSystem="PhPID" codeSystemVersion="{patientphpidversion}"/>
											</xsl:when>
										</xsl:choose>
										<name>
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
							<code code="{$Indication}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}" displayName="Patient Past indication"/>
							<value xsi:type="CE" code="{patientdrugindication}" codeSystemVersion="{patientdrugindicationmeddraversion}" codeSystem="{$oidMedDRA}"/>
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
							<code code="{$Reaction}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}" displayName="Patient Past Reaction" />
							<value xsi:type="CE" code="{patientdrugreaction}" codeSystemVersion="{patientdrgreactionmeddraversion}" codeSystem="{$oidMedDRA}"/>
						</observation>
					</outboundRelationship2>
				</xsl:if>

			</substanceAdministration>
		</component>
	</xsl:template>

	<!-- Patient Past Drug Therapy - Substance / Specified Substance Identifier and Strength (repeat as necessary)
	E2B(R2): element "PATIENTDRUGSUBSTANCEINFO" -  "ichicsr\ichicsrbatchheader\ichicsrmessageheader\safetyreport\patient\patientpastdrugtherapy\patientdrugsubstanceinfo"
	E2B(R3): element "primaryRole"
	-->
	<xsl:template match="patientdrugsubstanceinfo" mode="EMA-patient-past-drug-substance-info">
		<xsl:variable name="positionPatPastDrugSubInfo">
			<xsl:value-of select="position()"/>
		</xsl:variable>
		<xsl:comment>Patient Past Drug Therapy - Substance / Specified Substance Identifier and Strength - (<xsl:value-of select="$positionPatPastDrugSubInfo"/>)</xsl:comment>
		<ingredient classCode="ACTI">
			<xsl:if test="string-length(patientdrgsubstancestrength) > 0 and string-length(patientdrgsubstancestrengthunit) > 0">
				<quantity>
					<xsl:comment>D.8.r.EU.r.3a: Strength (number)</xsl:comment>
					<xsl:comment>D.8.r.EU.r.3b: Strength (unit)</xsl:comment>
					<xsl:element name="numerator">
						<xsl:attribute name="value">
							<xsl:value-of select="patientdrgsubstancestrength" />
						</xsl:attribute>
						<xsl:attribute name="unit">
							<xsl:call-template name="getMapping">
								<xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="patientdrgsubstancestrengthunit"/>
							</xsl:call-template>
						</xsl:attribute>
					</xsl:element>
					<!-- <numerator value="{patientdrgsubstancestrength}" unit="{patientdrgsubstancestrengthunit}"/>  -->
					<denominator value="1"/>
				</quantity>
			</xsl:if>

			<xsl:if test="(string-length(patientdrgsubstancename) > 0) or (string-length(patientdrgsubstancetermid) > 0 and string-length(patientdrgsubstancetermidversion) > 0)">
				<ingredientSubstance classCode="MMAT" determinerCode="KIND">
					<xsl:if test="string-length(patientdrgsubstancetermid) > 0 and string-length(patientdrgsubstancetermidversion) > 0">
						<xsl:comment>D.8.r.EU.r.2a: Substance / Specified Substance TermID Version Date / Number</xsl:comment>
						<xsl:comment>D.8.r.EU.r.2b: Substance / Specified Substance TermID</xsl:comment>
						<code code="{patientdrgsubstancetermid}" codeSystemVersion="{patientdrgsubstancetermidversion}" codeSystem="{$oidMessageType}"/>
					</xsl:if>

					<xsl:if test="string-length(patientdrgsubstancename) > 0">
						<name>
							<xsl:comment>D.8.r.EU.r.1: Substance / Specified Substance Name</xsl:comment>
							<xsl:value-of select="patientdrgsubstancename"/>
						</name>
					</xsl:if>
				</ingredientSubstance>
			</xsl:if>
		</ingredient>
	</xsl:template>

	<!-- New logic for Death -->
	<xsl:template match="patientdeathcause">
		<xsl:variable name="positionPatDeathCause">
			<xsl:value-of select="position()"/>
		</xsl:variable>
		<xsl:comment>D.9.2.r: Reported Cause(s) of Death - (<xsl:value-of select="$positionPatDeathCause"/>)</xsl:comment>
		<subjectOf2 typeCode="SBJ">
			<observation moodCode="EVN" classCode="OBS">
				<code code="{$ReportedCauseOfDeath}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
				<xsl:if test="string-length(patientdeathreport) > 0 or string-length(patientdeathreportmeddraversion) > 0 or string-length(patientdeathreporttxt) > 0">
					<xsl:if test="string-length(patientdeathreport) > 0 or string-length(patientdeathreportmeddraversion) > 0">
						<xsl:comment>D.9.2.r.1a: MedDRA Version for Reported Cause(s) of Death</xsl:comment>
						<xsl:comment>D.9.2.r.1b: Reported Cause(s) of Death (MedDRA code)</xsl:comment>
					</xsl:if>
					<value xsi:type="CE">
						<xsl:if test="string-length(patientdeathreport) > 0">
							<xsl:attribute name="codeSystem">
								<xsl:value-of select="$oidMedDRA"/>
							</xsl:attribute>
							<xsl:attribute name="code">
								<xsl:value-of select="patientdeathreport"/>
							</xsl:attribute>
						</xsl:if>
						<xsl:if test="string-length(patientdeathreportmeddraversion) > 0">
							<xsl:attribute name="codeSystemVersion">
								<xsl:value-of select="patientdeathreportmeddraversion"/>
							</xsl:attribute>
						</xsl:if>
						<xsl:if test="string-length(patientdeathreporttxt) > 0">
							<!-- D.9.2.r.2: Reported Cause(s) of Death (free text)-->
							<xsl:comment>D.9.2.r.2: Reported Cause(s) of Death (free text)</xsl:comment>
							<originalText>
								<xsl:value-of select="patientdeathreporttxt"/>
							</originalText>
						</xsl:if>
					</value>
				</xsl:if>
			</observation>
		</subjectOf2>
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
		<xsl:comment>D.9.4.r: Autopsy-determined Cause(s) of Death - (<xsl:value-of select="$positionPatAutopsy"/>)</xsl:comment>
		<outboundRelationship2 typeCode="DRIV">
			<observation moodCode="EVN" classCode="OBS">
				<code code="{$CauseOfDeath}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
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
									<!--  <width value="{reactionduration}" unit="{reactiondurationunit}"/> -->
									<width value="{reactionduration}">
										<xsl:attribute name="unit">
											<xsl:call-template name="getMapping">
												<xsl:with-param name="type">UCUM</xsl:with-param>
												<xsl:with-param name="code" select="reactiondurationunit"/>
											</xsl:call-template>
										</xsl:attribute>
									</width>
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
										<width value="{reactionduration}">
											<xsl:attribute name="unit">
												<xsl:call-template name="getMapping">
													<xsl:with-param name="type">UCUM</xsl:with-param>
													<xsl:with-param name="code" select="reactiondurationunit"/>
												</xsl:call-template>
											</xsl:attribute>
										</width>
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

	<!-- Populating the value in an element and handling null flavour for seriousness criteria -->
	<xsl:template name="seriousness-criteria">
		<xsl:param name="value"/>
		<xsl:if test="string-length($value) > 0">
			<xsl:variable name="isNullFlavourSerious">
				<xsl:call-template name="isNullFlavour">
					<xsl:with-param name="value" select="$value"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:choose>
				<xsl:when test="$isNullFlavourSerious = 'yes'">
					<xsl:variable name="NullFlavourWOSqBrcktSeriousnessCriteria">
						<xsl:call-template name="getNFValueWithoutSqBrckt">
							<xsl:with-param name="nfvalue" select="$value"/>
						</xsl:call-template>
					</xsl:variable>
					<value xsi:type="BL" nullFlavor="{$NullFlavourWOSqBrcktSeriousnessCriteria}"/>
				</xsl:when>
				<xsl:otherwise>
					<value xsi:type="BL" value="{$value}"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>

	<!-- Populating the value in an element and handling null flavour for reaction start and end dates-->
	<xsl:template name="date-of-reaction">
		<xsl:param name="element"/>
		<xsl:param name="value"/>
		<xsl:if test="string-length($value) > 0">
			<xsl:element name="{$element}">
				<xsl:variable name="isNullFlavourReactDate">
					<xsl:call-template name="isNullFlavour">
						<xsl:with-param name="value" select="$value"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:choose>
					<xsl:when test="$isNullFlavourReactDate = 'yes'">
						<xsl:variable name="NullFlavourWOSqBrcktDateOfReaction">
							<xsl:call-template name="getNFValueWithoutSqBrckt">
								<xsl:with-param name="nfvalue" select="$value"/>
							</xsl:call-template>
						</xsl:variable>
						<xsl:attribute name="nullFlavor">
							<xsl:value-of select="$NullFlavourWOSqBrcktDateOfReaction"/>
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


	<!-- F.r Results of Tests and Procedures Relevant to the Investigation of the Patient (repeat as necessary)
	E2B(R2): element "test" - "ichicsr\ichicsrbatchheader\ichicsrmessageheader\safetyreport\test"
	E2B(R3): element ""
	-->
	<xsl:template match="test" mode="EMA-lab-test">
		<xsl:if test="string-length(testdate) > 0 or string-length(testname) > 0 or string-length(testmeddracode) > 0">
			<xsl:variable name="positionLabTest">
				<xsl:value-of select="position()"/>
			</xsl:variable>
			<xsl:comment>F.r: Results of Tests and Procedures Relevant to the Investigation of the Patient - (<xsl:value-of select="$positionLabTest"/>)</xsl:comment>
			<component typeCode="COMP">
				<observation moodCode="EVN" classCode="OBS">
					<xsl:choose>
						<xsl:when test="string-length(testmeddracode) > 0 and string-length(testmeddraversion) > 0">
							<xsl:comment>F.r.2.2a: MedDRA Version for Test Name</xsl:comment>
							<xsl:comment>F.r.2.2b: Test Name (MedDRA code)</xsl:comment>
							<code code="{testmeddracode}" codeSystem="{$oidMedDRA}" codeSystemVersion="{testmeddraversion}">
								<!-- F.r.2.1: Test Name (free text) -->
								<xsl:choose>
									<xsl:when test="string-length(testname) > 0">
										<xsl:comment>F.r.2.1: Test Name (free text)</xsl:comment>
										<originalText>
											<xsl:value-of select="testname"/>
										</originalText>
									</xsl:when>
									<xsl:otherwise>
										<originalText />
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
										<originalText />
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
							<value xsi:type="ED" />
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
							<interpretationCode code="{testresultcode}" codeSystem="{$oidTestResultCode}" codeSystemVersion="{$emaoidFr31CLVersion}"/>
						</xsl:when>
						<xsl:otherwise>
							<interpretationCode codeSystem="{$oidTestResultCode}" codeSystemVersion="{$emaoidFr31CLVersion}"/>
						</xsl:otherwise>
					</xsl:choose>

					<!-- F.r.4: Normal Low Value -->
					<xsl:comment>F.r.4: Normal low range</xsl:comment>
					<referenceRange typeCode="REFV">
						<observationRange classCode="OBS" moodCode="EVN.CRT">
							<xsl:comment>F.r.3.3: Test Result (unit)</xsl:comment>
							<xsl:choose>
								<xsl:when test="number(lowtestrange)">
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
									<value xsi:type="PQ" />
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
								<xsl:when test="number(hightestrange)">
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
									<value xsi:type="PQ" />
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
							<code code="{$Comment}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}" displayName="comment"/>
							<xsl:choose>
								<xsl:when test="string-length(testresultcomments)>0">
									<value xsi:type="ED">
										<xsl:value-of select="testresultcomments"/>
									</value>
								</xsl:when>
								<xsl:otherwise>
									<value xsi:type="ED" />
								</xsl:otherwise>
							</xsl:choose>
						</observation>
					</outboundRelationship2>

					<!-- F.r.7 More Information Available -->
					<xsl:comment>F.r.7: More information available</xsl:comment>
					<outboundRelationship2 typeCode="REFR">
						<observation moodCode="EVN" classCode="OBS">
							<code code="{$MoreInformationAvailable}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}" displayName="moreInformationAvailable"/>
							<xsl:choose>
								<xsl:when test="string-length(moreinformation)>0">
									<value xsi:type="BL" value="{moreinformation}" />
								</xsl:when>
								<xsl:otherwise>
									<value xsi:type="BL" />
								</xsl:otherwise>
							</xsl:choose>
						</observation>
					</outboundRelationship2>
				</observation>
			</component>
		</xsl:if>
	</xsl:template>

	<!-- TEST :
  F.r.3.2: Test Result (value / qualifier)
  F.r.3.3: Test Result (unit)
	-->
	<xsl:template match="test" mode="EMA-testresult-PINF-NINF">
		<value xsi:type="IVL_PQ">
			<xsl:variable name="vartestresult">
				<xsl:value-of select="normalize-space(testresult)" disable-output-escaping="yes"/>
			</xsl:variable>
			<xsl:variable name="vartestresultValue">
				<xsl:value-of select="normalize-space(translate(translate(translate(testresult,'=',''),'&gt;',''),'&lt;',''))" disable-output-escaping="yes"/>
			</xsl:variable>
			<xsl:choose>
				<xsl:when test="number(normalize-space($vartestresultValue))">
					<xsl:choose>
						<xsl:when test="not(contains($vartestresult,'&lt;')) and not(contains($vartestresult,'=')) and not(contains($vartestresult,'&gt;'))">
							<center value="{normalize-space($vartestresultValue)}" unit="{testunit}"/>
						</xsl:when>
						<xsl:when test="not(contains($vartestresult,'&lt;')) and contains($vartestresult,'=') and not(contains($vartestresult,'&gt;'))">
							<center value="{normalize-space($vartestresultValue)}" unit="{testunit}"/>
						</xsl:when>
						<xsl:when test="contains($vartestresult,'&lt;') and not(contains($vartestresult,'=')) and not(contains($vartestresult,'&gt;'))">
							<low nullFlavor="NINF"/>
							<high value="{normalize-space($vartestresultValue)}" unit="{testunit}" inclusive="false"/>
						</xsl:when>
						<xsl:when test="contains($vartestresult,'&lt;') and contains($vartestresult,'=') and not(contains($vartestresult,'&gt;'))">
							<low nullFlavor="NINF"/>
							<high value="{normalize-space($vartestresultValue)}" unit="{testunit}" inclusive="true"/>
						</xsl:when>
						<xsl:when test="not(contains($vartestresult,'&lt;')) and not(contains($vartestresult,'=')) and contains($vartestresult,'&gt;')">
							<low value="{normalize-space($vartestresultValue)}" unit="{testunit}" inclusive="false"/>
							<high nullFlavor="PINF"/>
						</xsl:when>
						<xsl:when test="not(contains($vartestresult,'&lt;')) and contains($vartestresult,'=') and contains($vartestresult,'&gt;')">
							<low value="{normalize-space($vartestresultValue)}" unit="{testunit}" inclusive="true"/>
							<high nullFlavor="PINF"/>
						</xsl:when>
					</xsl:choose>
				</xsl:when>
			</xsl:choose>
		</value>
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
							<xsl:if test="string-length(krdrugmedicalproductid) > 0 and string-length(krdrugmedicalproductversion) > 0"  >
								<xsl:comment>G.k.2.1.KR.1a: Drug Code Version</xsl:comment>
								<xsl:comment>G.k.2.1.KR.1b: Drug Code </xsl:comment>
								<code code="{krdrugmedicalproductid}" codeSystem="{$OIdKRDrugForeign}" codeSystemVersion="{krdrugmedicalproductversion}"/>
							</xsl:if>
							<xsl:if test="string-length(krdrugmedicalproductid) > 0 and string-length(krdrugmedicalproductversion) =0"  >
								<xsl:comment>G.k.2.1.KR.1b </xsl:comment>
								<code code="{krdrugmedicalproductid}" codeSystem="{$OIdKRDrugDomestic}" />
							</xsl:if>
							<xsl:if test="string-length(drugmpidversion) > 0 and string-length(drugmpid) > 0"  >
								<xsl:comment>G.k.2.1.1a: MPID Version Date / Number </xsl:comment>
								<xsl:comment>G.k.2.1.1b: Medicinal Product Identifier (MPID)</xsl:comment>
								<code code="{drugmpid}" codeSystem="MPID" codeSystemVersion="{drugmpidversion}" displayName="Medicinal Product Identifier and Version"/>
							</xsl:if>
							<xsl:if test="string-length(drugphpidversion) > 0 and string-length(drugphpid) > 0"  >
								<xsl:comment>G.k.2.1.2a: PhPID Version Date / Number</xsl:comment>
								<xsl:comment>G.k.2.1.2b: Pharmaceutical Product Identifier (PhPID)</xsl:comment>
								<code code="{drugphpid}" codeSystem="PhPID" codeSystemVersion="{drugphpidversion}" displayName="Pharmaceutical Product Identifier and Version"/>
							</xsl:if>
							<name>
								<xsl:comment>G.k.2.2: Medicinal Product Name as Reported by the Primary Source</xsl:comment>
								<xsl:value-of select="medicinalproduct"/>
							</name>

							<asManufacturedProduct classCode="MANU">
								<xsl:comment>G.k.3: Holder and Authorisation / Application Number of Drug </xsl:comment>
								<xsl:if test="string-length(drugauthorizationnumb) > 0 or string-length(drugauthorizationcountry) > 0 or string-length(drugauthorizationholder) > 0">
									<subjectOf typeCode="SBJ">
										<approval classCode="CNTRCT" moodCode="EVN">
											<xsl:comment>G.k.3.1: Authorisation / Application Number</xsl:comment>
											<xsl:if test="string-length(drugauthorizationnumb) > 0">
												<id extension="{drugauthorizationnumb}" root="{$oidAuthorisationNumber}"/>
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
															<code codeSystem="{$OidISOCountry}" code="{drugauthorizationcountry}"/>
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
							<xsl:comment>G.k.2.4: Identification of the Country Where the Drug Was Obtained</xsl:comment>
							<subjectOf typeCode="SBJ">
								<productEvent classCode="ACT" moodCode="EVN">
									<code code="{$RetailSupply}" codeSystem="{$oidActionPerformedCode}" codeSystemVersion="{$emaoidGk24CLVersion}" displayName="retailSupply"/>
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
							<code code="{$Blinded}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
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
							<code code="{$CumulativeDoseToReaction}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}" displayName="cumulativeDoseToReaction"/>
							<value xsi:type="PQ" value="{drugcumulativedosagenumb}" >
								<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="drugcumulativedosageunit"/></xsl:call-template></xsl:attribute>
							</value>
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
							<code code="{$GestationPeriod}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
							<value xsi:type="PQ" value="{reactiongestationperiod}">
								<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="reactiongestationperiodunit"/></xsl:call-template></xsl:attribute>
							</value>
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
							<code code="{$AdditionalCodedDrugInformation}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
							<xsl:comment>G.k.11: Additional Information on Drug (free text)</xsl:comment>
							<value xsi:type="ST">
								<xsl:value-of select="drugadditionaltext"/>
							</value>
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
							<code code="{actiondrug}" codeSystem="{$oidActionTaken}" codeSystemVersion="{$emaoidGk8CLVersion}"/>
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
			<xsl:comment>G.k.2.3.r: Substance / Specified Substance Identifier and Strength - (<xsl:value-of select="$positionActiveSub"/>)</xsl:comment>
			<ingredient classCode="ACTI">
				<xsl:if test="string-length(substancestrength) > 0 and string-length(substancestrengthunit) > 0">
					<quantity>
						<xsl:comment>G.k.2.3.r.3a: Strength (number)</xsl:comment>
						<xsl:comment>G.k.2.3.r.3b: Strength (unit)</xsl:comment>
						<xsl:element name="numerator">
							<xsl:attribute name="value">
								<xsl:value-of select="substancestrength" />
							</xsl:attribute>
							<xsl:attribute name="unit">
								<xsl:call-template name="getMapping">
									<xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="substancestrengthunit"/>
								</xsl:call-template>
							</xsl:attribute>
						</xsl:element>
						<denominator value="1"/>
					</quantity>
				</xsl:if>
				<ingredientSubstance classCode="MMAT" determinerCode="KIND">
					<xsl:choose>
						<xsl:when test="(string-length(krsubstanceid) > 0 and string-length(krsubstanceversion) > 0)">
							<xsl:comment>G.k.2.3.r.1.KR.1a: Substance Version</xsl:comment>
							<xsl:comment>G.k.2.3.r.1.KR.1b: Substance ID</xsl:comment>
							<code code="{krsubstanceid}" codeSystem="{$OIdKRDrugForeign}" codeSystemVersion="{krsubstanceversion}"/>
						</xsl:when>
						<xsl:when test="(string-length(krsubstanceid) > 0)">
							<xsl:comment>G.k.2.3.r.1.KR.1b: Substance ID</xsl:comment>
							<code code="{krsubstanceid}" codeSystem="{$OIdKRDrugDomestic}"/>
						</xsl:when>
						<xsl:when test="string-length(activesubstancetermid) > 0 and string-length(activesubstancetermidversion) > 0">
							<xsl:comment>G.k.2.3.r.2a: Substance / Specified Substance TermID Version Date / Number </xsl:comment>
							<xsl:comment>G.k.2.3.r.2b: Substance / Specified Substance TermID </xsl:comment>
							<code code="{activesubstancetermid}" codeSystem="TBD-Substance" codeSystemVersion="{activesubstancetermidversion}" displayName="Ingredient Information" />
						</xsl:when>
					</xsl:choose>
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
			<xsl:comment>G.k.7.r: Indication for Use in Case - (<xsl:value-of select="$positionDrugInd"/>)</xsl:comment>
			<inboundRelationship typeCode="RSON">
				<observation moodCode="EVN" classCode="OBS">
					<code code="{$Indication}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
					<xsl:choose>
						<xsl:when test="$isNullFlavourDrugIndication = 'yes'">
							<xsl:variable name="NullFlavourWOSqBrcktDrugIndication">
								<xsl:call-template name="getNFValueWithoutSqBrckt">
									<xsl:with-param name="nfvalue" select="drugindicationterm"/>
								</xsl:call-template>
							</xsl:variable>
							<xsl:comment>G.k.7.r.1: Indication as Reported by the Primary Source</xsl:comment>
							<value xsi:type="CE" nullFlavor="{$NullFlavourWOSqBrcktDrugIndication}"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:choose>
								<xsl:when test="(string-length(drugindication) > 0 and string-length(drugindicationmeddraversion) > 0)">
									<xsl:comment>G.k.7.r.2a: MedDRA Version for Indication</xsl:comment>
									<xsl:comment>G.k.7.r.2b: Indication (MedDRA code)</xsl:comment>
									<xsl:comment>G.k.7.r.1: Indication as Reported by the Primary Source</xsl:comment>
									<value xsi:type="CE" code="{drugindication}" codeSystem="{$oidMedDRA}" codeSystemVersion="{drugindicationmeddraversion}">
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
							<code code="{$SourceReporter}" codeSystem="{$oidAssignedEntityRoleCode}" codeSystemVersion="{$emaoidAssignedEntityRoleCodeVersion}" displayName="sourceReporter"/>
						</assignedEntity>
					</performer>
					<outboundRelationship1 typeCode="REFR">
						<xsl:comment>G.k[GID]: Drug UUID</xsl:comment>
						<actReference classCode="SBADM" moodCode="EVN">
							<id root="{druguniversallyuniqueid}"/>
						</actReference>
					</outboundRelationship1>
				</observation>
			</inboundRelationship>

		</xsl:if>
	</xsl:template>


	<!-- G.k.10.r Additional Information on Drug (coded) (repeat as necessary)
	E2B(R2): element "drugadditionalcode" - "ichicsr/ichicsrbatchheader/ichicsrmessageheader/safetyreport/drug/drugadditionalstructured"
	E2B(R3): element ""
	-->
	<xsl:template match="drugadditionalstructured" mode="EMA-drug-additional-info">
		<xsl:if test="string-length(drugadditionalcode)>0">
			<xsl:variable name="positionDrugAddInfo">
				<xsl:value-of select="position()"/>
			</xsl:variable>
			<xsl:comment>G.k.10.r: Additional Information on Drug (coded)  - (<xsl:value-of select="$positionDrugAddInfo"/>)</xsl:comment>
			<outboundRelationship2 typeCode="REFR">
				<observation classCode="OBS" moodCode="EVN">
					<code code="{$CodedDrugInformation}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
					<xsl:comment>G.k.10.r: Additional Information on Drug (coded)(repeat as necessary) code system version </xsl:comment>
					<value xsi:type="CE" code="{drugadditionalcode}" codeSystem="{$AdditionalInformationOnDrug}" codeSystemVersion="{$emaoidGk10rCLVersion}"/>
				</observation>
			</outboundRelationship2>
		</xsl:if>
	</xsl:template>


	<!-- G.k.9.i.4 Did Reaction Recur on Re-administration?
	E2B(R2): element "drugrecurreadministration" - "ichicsr/ichicsrbatchheader/ichicsrmessageheader/safetyreport/drug/drugeventmatrix"
	E2B(R3): element ""
	-->
	<xsl:template match="drugrelatedness" mode="EMA-recur">
		<xsl:if test="string-length(drugrecurreadministration)>0">
			<outboundRelationship2 typeCode="PERT">
				<observation moodCode="EVN" classCode="OBS">
					<code code="{$RecurranceOfReaction}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
					<xsl:comment>G.k.9.i.4: Did Reaction Recur on Re-administration?</xsl:comment>
					<value xsi:type="CE" code="{drugrecurreadministration}" codeSystem="{$oidRechallenge}" codeSystemVersion="{$emaoidGk9i4CLVersion}"/>
					<xsl:variable name="reaction" select="normalize-space(eventuniversallyuniqueid})"/>
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
		<xsl:comment>G.k.4.r: Dosage and Relevant Information  - (<xsl:value-of select="$positionDrugDosageInfo"/>)</xsl:comment>
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
							<xsl:when test="string-length(drugstartdate) = 0 or string-length(drugenddate) = 0 or string-length(drugtreatmentduration) = 0">
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
									<xsl:comment>G.k.4.r.2: Number of Units in the Interval  </xsl:comment>
									<xsl:comment>G.k.4.r.3: Definition of the Time Interval Unit </xsl:comment>
									<!-- <period value="{drugintervaldosageunitnumb}" unit="{drugintervaldosagedefinition}"/>  -->
									<xsl:element name="period">
										<xsl:attribute name="value">
											<xsl:value-of select="drugintervaldosageunitnumb" />
										</xsl:attribute>
										<xsl:attribute name="unit">
											<xsl:call-template name="getMapping">
												<xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="drugintervaldosagedefinition"/>
											</xsl:call-template>
										</xsl:attribute>
									</xsl:element>
								</xsl:if>
								<xsl:if test="string-length(drugintervaldosageunitnumb) = 0 and string-length(drugintervaldosagedefinition) > 0">
									<xsl:comment>G.k.4.r.3: Definition of the Time Interval Unit </xsl:comment>
									<!-- <period unit="{drugintervaldosagedefinition}"/> -->
									<xsl:element name="period">
										<xsl:attribute name="unit">
											<xsl:call-template name="getMapping">
												<xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="drugintervaldosageunitnumb"/>
											</xsl:call-template>
										</xsl:attribute>
									</xsl:element>
								</xsl:if>
							</comp>

							<!--G.k.4.r.4 Date and Time of Start of Drug-->
							<!--G.k.4.r.5 Date and Time of Last Administration-->
							<!--G.k.4.r.6a Duration of Drug Administration (number)-->
							<!--G.k.4.r.6b Duration of Drug Administration (unit)-->
							<xsl:choose>
								<xsl:when test="string-length(drugstartdate) = 0 or string-length(drugenddate) = 0 or string-length(drugtreatmentduration) = 0">

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
							<routeCode nullFlavor="{$NullFlavourWOSqBrcktGk4r101}"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:choose>
								<xsl:when test="string-length(drugadministrationtermid) > 0 and string-length(drugadministrationtermidversion) > 0">
									<xsl:comment>G.k.4.r.10.2a: Route of Administration TermID Version Date / Number</xsl:comment>
									<xsl:comment>G.k.4.r.10.2b: Route of Administration TermID</xsl:comment>
									<routeCode code="{drugadministrationtermid}" codeSystem="{$oidICHRoute}" codeSystemVersion="{drugadministrationtermidversion}">
										<xsl:if test="string-length(drugadministrationroute) > 0">
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
									<xsl:apply-templates select="../devicecomponentdetails" mode="EMA-drug-device-component"/>
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
											<xsl:when test="string-length(drugdosageformtermidversion) > 0 and string-length(drugdosageformtermid) > 0 and string-length(drugdosageform) > 0">
												<xsl:comment>G.k.4.r.9.1: Pharmaceutical form (Dosage form)</xsl:comment>
												<xsl:comment>G.k.4.r.9.2a: Pharmaceutical Dose Form TermID Version Date / Number </xsl:comment>
												<xsl:comment>G.k.4.r.9.2b: Pharmaceutical Dose Form TermID </xsl:comment>
												<formCode code="{drugdosageformtermidversion}" codeSystem="DoseForm" codeSystemVersion="{drugdosageformtermid}">
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
							<code code="{$ParentRouteOfAdministration}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
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
										<xsl:when test="string-length(drugparadministrationtermid) > 0 and string-length(drugparadministrationtermidversion) > 0">
											<xsl:comment>G.k.4.r.11.2a: Parent Route of Administration TermID Version Date / Number</xsl:comment>
											<xsl:comment>G.k.4.r.11.2b: Parent Route of Administration TermID</xsl:comment>
											<value xsi:type="CE" code="{drugparadministrationtermid}" codeSystem="{$oidICHRoute}" codeSystemVersion="{drugparadministrationtermidversion}">
												<xsl:if test="string-length(drugparadministration) > 0">
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

	<!-- Drug - Device component (repeat as necessary)
	E2B(R2): element "devicecomponentdetails" - "ichicsr/ichicsrbatchheader/ichicsrmessageheader/safetyreport/drug/devicecomponentdetails"
	E2B(R3): element ""
	-->
	<xsl:template match="drug" mode="EMA-drug-dosage-info-for-device-component">
		<outboundRelationship2 typeCode="COMP">
			<substanceAdministration classCode="SBADM" moodCode="EVN">
				<consumable typeCode="CSM">
					<instanceOfKind classCode="INST">
						<productInstanceInstance classCode="MMAT" determinerCode="INSTANCE">

							<!-- Drug - Device component (repeat as necessary) -->
							<xsl:if test="count(devicecomponentdetails) > 0">
								<xsl:apply-templates select="devicecomponentdetails" mode="EMA-drug-device-component"/>
							</xsl:if>

						</productInstanceInstance>
					</instanceOfKind>
				</consumable>
			</substanceAdministration>
		</outboundRelationship2>
	</xsl:template>

	<!-- Drug (causality):
	E2B(R2): element "drug"
	E2B(R3): element "causalityAssessment"
	-->
	<xsl:template match="drug" mode="EMA-causality">
		<xsl:variable name="did"><xsl:value-of select="druguniversallyuniqueid"/>
		</xsl:variable>
		<!--G.k.1 Characterisation of Drug Role -->
		<xsl:if test="string-length(drugcharacterization)>0">
			<xsl:comment>G.k.1: Characterization of Drug Role</xsl:comment>
			<component typeCode="COMP">
				<causalityAssessment classCode="OBS" moodCode="EVN">
					<code code="{$InterventionCharacterization}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
					<value xsi:type="CE" code="{drugcharacterization}" codeSystem="{$oidDrugRole}" codeSystemVersion="{$emaoidGk1CLVersion}"/>
					<xsl:comment>G.k[GID]: Drug UUID</xsl:comment>
					<subject2 typeCode="SUBJ">
						<productUseReference classCode="SBADM" moodCode="EVN">
							<id root="{normalize-space($did)}"/>
						</productUseReference>
					</subject2>
				</causalityAssessment>
			</component>
		</xsl:if>


		<!-- EU Reference instance  - EU Causality assessment (repeat as necessary) -->
		<xsl:apply-templates select="drugrelatedness/drugassesment" mode="EMA-eu-causality-assessment">
			<xsl:with-param name="drugRef" select="normalize-space($did)"/>
		</xsl:apply-templates>

		<!-- G.k.9.i.2.r Assessment of Relatedness of Drug to Reaction(s) / Event(s) (repeat as necessary) -->
		<xsl:apply-templates select="drugrelatedness/drugassesment" mode="EMA-drug-reaction-relatedness">
			<xsl:with-param name="drugRef" select="normalize-space($did)"/>
		</xsl:apply-templates>

	</xsl:template>




	<!--Time Interval between Drug Administration and Reaction-->
	<xsl:template match="drugrelatedness" mode="EMA-interval">
		<xsl:if test="string-length(drugstartperiod) > 0">
			<outboundRelationship1 typeCode="SAS">
				<xsl:comment>G.k.9.i.3.1a: Time Interval between Beginning of Drug Administration and Start of Reaction / Event (number)</xsl:comment>
				<xsl:comment>G.k.9.i.3.1b: Time Interval between Beginning of Drug Administration and Start of Reaction / Event (unit)</xsl:comment>
				<pauseQuantity value="{drugstartperiod}" unit="{drugstartperiodunit}"/>
				<xsl:variable name="reaction" select="normalize-space(eventuniversallyuniqueid)"/>
				<xsl:if test="string-length($reaction) > 0">
					<xsl:comment>G.k.9.i.1: Reaction(s) / Event(s) Assessed</xsl:comment>
					<actReference classCode="OBS" moodCode="EVN">
						<xsl:variable name="rid">
							<xsl:value-of select="$reaction"/>
						</xsl:variable>
						<id root="{normalize-space($rid)}"/>
					</actReference>
				</xsl:if>
			</outboundRelationship1>
		</xsl:if>

		<xsl:if test="string-length(druglastperiod) > 0">
			<outboundRelationship1 typeCode="SAE">
				<xsl:comment>G.k.9.i.3.2a: Time Interval between Last Dose of Drug and Start of Reaction / Event (number)</xsl:comment>
				<xsl:comment>G.k.9.i.3.2b: Time Interval between Last Dose of Drug and Start of Reaction / Event (unit)</xsl:comment>
				<pauseQuantity value="{druglastperiod}" unit="{druglastperiodunit}"/>
				<xsl:variable name="reaction" select="normalize-space(eventuniversallyuniqueid)"/>
				<xsl:if test="string-length($reaction) > 0">
					<xsl:comment>G.k.9.i.1: Reaction(s) / Event(s) Assessed</xsl:comment>
					<actReference classCode="OBS" moodCode="EVN">
						<xsl:variable name="rid">
							<xsl:value-of select="$reaction"/>
						</xsl:variable>
						<id root="{normalize-space($rid)}"/>
					</actReference>
				</xsl:if>
			</outboundRelationship1>
		</xsl:if>
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
			<xsl:comment>G.k.9.i.2.r: Relatedness of Drug to Reaction(s) / Event(s) - (<xsl:value-of select="$positionDrugReactRel"/>)</xsl:comment>
			<component typeCode="COMP">
				<causalityAssessment classCode="OBS" moodCode="EVN">
					<code code="{$Causality}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
					<xsl:choose>
						<!-- G.k.9.i.2.r.3.KR.1: WHO-UMC Result of Assessment -->
						<xsl:when test="string-length(whodrugassessmentresult) > 0">
							<xsl:variable name="isNullFlavourGk9i2r3KR1">
								<xsl:call-template name="isNullFlavour">
									<xsl:with-param name="value" select="whodrugassessmentresult"/>
								</xsl:call-template>
							</xsl:variable>

							<xsl:choose>
								<xsl:when test="$isNullFlavourGk9i2r3KR1 = 'yes'">
									<xsl:variable name="NullFlavourWOSqBrcktGk9i2r3KR1">
										<xsl:call-template name="getNFValueWithoutSqBrckt">
											<xsl:with-param name="nfvalue" select="whodrugassessmentresult"/>
										</xsl:call-template>
									</xsl:variable>
									<xsl:comment>G.k.9.i.2.r.3.KR.1: WHO-UMC Result of Assessment</xsl:comment>
									<value xsi:type="CE" nullFlavor="{$NullFlavourWOSqBrcktGk9i2r3KR1}"/>
								</xsl:when>
								<xsl:when test="(string-length(whodrugassessmentresult) > 0)">
									<xsl:comment>G.k.9.i.2.r.3.KR.1: WHO-UMC Result of Assessment</xsl:comment>
									<value xsi:type="CE" code="{whodrugassessmentresult}" codeSystem="{$oidMFDSWHOResult}" codeSystemVersion="1.0" />
								</xsl:when>
							</xsl:choose>

						</xsl:when>

						<!-- G.k.9.i.2.r.3.KR.2: KRCT Result of Assessment -->
						<xsl:when test="string-length(krdrugassessmentresult) > 0">
							<xsl:variable name="isNullFlavourGk9i2r3KR2">
								<xsl:call-template name="isNullFlavour">
									<xsl:with-param name="value" select="krdrugassessmentresult"/>
								</xsl:call-template>
							</xsl:variable>

							<xsl:choose>
								<xsl:when test="$isNullFlavourGk9i2r3KR2 = 'yes'">
									<xsl:variable name="NullFlavourWOSqBrcktGk9i2r3KR2">
										<xsl:call-template name="getNFValueWithoutSqBrckt">
											<xsl:with-param name="nfvalue" select="krdrugassessmentresult"/>
										</xsl:call-template>
									</xsl:variable>
									<xsl:comment>G.k.9.i.2.r.3.KR.2: KRCT Result of Assessment</xsl:comment>
									<value xsi:type="CE" nullFlavor="{$NullFlavourWOSqBrcktGk9i2r3KR2}"/>
								</xsl:when>
								<xsl:when test="(string-length(krdrugassessmentresult) > 0)">
									<xsl:comment>G.k.9.i.2.r.3.KR.2: KRCT Result of Assessment</xsl:comment>
									<value xsi:type="CE" code="{krdrugassessmentresult}" codeSystem="{$oidMFDSKRCTResult}" codeSystemVersion="1.0" />
								</xsl:when>
							</xsl:choose>

						</xsl:when>

						<!-- G.k.9.i.2.r.3 Result of Assessment -->
						<xsl:when test="string-length(drugresult) > 0">
							<xsl:comment>G.k.9.i.2.r.3: Result of Assessment</xsl:comment>
							<value xsi:type="ST">
								<xsl:value-of select="drugresult"/>
							</value>
						</xsl:when>

					</xsl:choose>
					<xsl:choose>
						<!-- G.k.9.i.2.r.3.KR.2: KRCT Result of Assessment -->
						<xsl:when test="string-length(krdrugassessmentmethod) > 0">
							<xsl:comment>G.k.9.i.2.r.2.KR.1: KR Method of Assessment</xsl:comment>
							<methodCode code="{krdrugassessmentmethod}" codeSystem="{$oidMFDSKRMethod}" codeSystemVersion="1.0" />
						</xsl:when>

						<!-- G.k.9.i.2.r.2 Method of Assessment -->
						<xsl:when test="string-length(drugassessmentmethod) > 0">
							<xsl:comment>G.k.9.i.2.r.2: Method of Assessment</xsl:comment>
							<methodCode>
								<originalText>
									<xsl:value-of select="drugassessmentmethod"/>
								</originalText>
							</methodCode>
						</xsl:when>

					</xsl:choose>
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

	</xsl:template>


	<!--EU Reference instance  - EU Causality assessment (repeat as necessary)
	E2B(R2): element "drugassesment" - "ichicsr/ichicsrbatchheader/ichicsrmessageheader/safetyreport/drug/drugeventmatrix"
	E2B(R3): element "causalityAssessment"
	-->
	<xsl:template match="drugassesment" mode="EMA-eu-causality-assessment">
		<xsl:param name="drugRef"/>
		<xsl:if test="string-length(eudrugassessmentsource) > 0 or string-length(eudrugassessmentmethod) > 0 or string-length(eudrugresult) > 0">
			<xsl:variable name="positionEUDrugReactRel">
				<xsl:value-of select="position()"/>
			</xsl:variable>
			<xsl:comment>EU Reference instance  - EU Causality assessment - (<xsl:value-of select="$positionEUDrugReactRel"/>)</xsl:comment>
			<component typeCode="COMP">
				<causalityAssessment classCode="OBS" moodCode="EVN">
					<code code="{$Causality}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
					<xsl:comment>G.k.9.i.2.r.3.EU.1: Result of Assessment - captured in the code field</xsl:comment>
					<xsl:if test="string-length(eudrugresult) > 0">
						<value xsi:type="CE" code="{eudrugresult}" codeSystem="{$oidEUResultOfAssessmentCode}" codeSystemVersion="1.0" />
					</xsl:if>
					<xsl:comment>G.k.9.i.2.r.2.EU.1: EU Method of assessment - captured in the code field</xsl:comment>
					<xsl:if test="string-length(eudrugassessmentmethod) > 0">
						<methodCode code="{eudrugassessmentmethod}" codeSystem="{$oidEUMethodOfAssessmentCode}" codeSystemVersion="1.0"/>
					</xsl:if>
					<xsl:if test="string-length(eudrugassessmentsource) > 0">
						<author typeCode="AUT">
							<assignedEntity classCode="ASSIGNED">
								<xsl:comment>G.k.9.i.2.r.1.EU.1: EU Source of assessment - captured in the code field</xsl:comment>
								<code code="{eudrugassessmentsource}" codeSystem="{$oidEUSourceOfAssessmentCode}" codeSystemVersion="1.0"/>
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
	</xsl:template>


	<!-- Drug - Device component (repeat as necessary)
	E2B(R2): element "DEVICECOMPONENT" -  "ichicsr\ichicsrbatchheader\ichicsrmessageheader\safetyreport\Drug\devicecomponentdetails"
	E2B(R3): element "primaryRole"
	-->
	<xsl:template match="devicecomponentdetails" mode="EMA-drug-device-component">
		<xsl:if test="string-length(devicecomponentname)>0 or string-length(devicecomponenttermidversion) or string-length(devicecomponenttermid) or  string-length(devicebatchnumber) > 0">
			<xsl:variable name="positionDrugDevComp">
				<xsl:value-of select="position()"/>
			</xsl:variable>
			<xsl:comment>Drug - Device component - (<xsl:value-of select="$positionDrugDevComp"/>)</xsl:comment>
			<part classCode="PART">
				<partDeviceInstance classCode="DEV">
					<xsl:comment>G.k.2.2.EU.9.r.4: Device Batch Lot number</xsl:comment>
					<lotNumberText>
						<xsl:value-of select="devicebatchnumber"/>
					</lotNumberText>
					<asInstanceOfKind>
						<kindOfMaterialKind>
							<xsl:choose>
								<xsl:when test="string-length(devicecomponenttermid)>0 and string-length(devicecomponenttermidversion)  > 0">
									<xsl:comment>G.k.2.2.EU.9.r.3: Device Component TermID</xsl:comment>
									<xsl:comment>G.k.2.2.EU.9.r.2: Device Component TermID version Date/Number</xsl:comment>
									<code code="{devicecomponenttermid}" codeSystem="EUOID" codeSystemVersion="{devicecomponenttermidversion}" />
								</xsl:when>
								<xsl:otherwise>
									<code />
								</xsl:otherwise>
							</xsl:choose>
							<xsl:comment>G.k.2.2.EU.9.r.1: Device Component name (free text)</xsl:comment>
							<name>
								<xsl:value-of select="devicecomponentname"/>
							</name>
						</kindOfMaterialKind>
					</asInstanceOfKind>
				</partDeviceInstance>
			</part>
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
					<code code="{$Comment}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
					<value xsi:type="ED" mediaType="text/plain">
						<xsl:value-of select="reportercomment"/>
					</value>
					<author typeCode="AUT">
						<assignedEntity classCode="ASSIGNED">
							<code code="{$SourceReporter}" codeSystem="{$oidAssignedEntityRoleCode}" codeSystemVersion="{$emaoidAssignedEntityRoleCodeVersion}"/>
						</assignedEntity>
					</author>
				</observationEvent>
			</component1>
		</xsl:if>

		<!-- H.3.r Sender's diagnosis/syndrome code (repeat as necessary)-->
		<xsl:apply-templates select="senderdiagnosisinfo" mode="EMA-case-summary"/>

		<!-- H.4 Sender's Comments -->
		<xsl:if test="string-length(sendercomment) > 0">
			<xsl:comment>H.4: Sender's Comments</xsl:comment>
			<component1 typeCode="COMP">
				<observationEvent moodCode="EVN" classCode="OBS">
					<code code="{$Comment}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
					<value xsi:type="ED" mediaType="text/plain">
						<xsl:value-of select="sendercomment"/>
					</value>
					<author typeCode="AUT">
						<assignedEntity classCode="ASSIGNED">
							<code code="{$Sender}" codeSystem="{$oidAssignedEntityRoleCode}" codeSystemVersion="{$emaoidAssignedEntityRoleCodeVersion}"/>
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
	<xsl:template match="senderdiagnosisinfo" mode="EMA-case-summary">
		<xsl:if test="string-length(senderdiagnosis) > 0">
			<xsl:variable name="positionSendDiag">
				<xsl:value-of select="position()"/>
			</xsl:variable>
			<xsl:comment>H.3.r: Sender's Diagnosis - (<xsl:value-of select="$positionSendDiag"/>)</xsl:comment>
			<component1 typeCode="COMP">
				<observationEvent moodCode="EVN" classCode="OBS">
					<code code="{$Diagnosis}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
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
							<code code="{$Sender}" codeSystem="{$oidAssignedEntityRoleCode}" codeSystemVersion="{$emaoidAssignedEntityRoleCodeVersion}"/>
						</assignedEntity>
					</author>
				</observationEvent>
			</component1>
		</xsl:if>
	</xsl:template>

	<!-- H.5.r Case Summary and Reporter’s Comments in Native Language (repeat as necessary) -->
	<xsl:template match="casesummarynarrative" mode="EMA-case-summary">
		<xsl:if test="string-length(casesummary) > 0">
			<xsl:variable name="positionCaseSumNar">
				<xsl:value-of select="position()"/>
			</xsl:variable>
			<xsl:comment>H.5.r: Case Summary and Reporter’s Comments in Native Language - (<xsl:value-of select="$positionCaseSumNar"/>)</xsl:comment>
			<component typeCode="COMP">
				<observationEvent moodCode="EVN" classCode="OBS">
					<code code="{$SummaryAndComment}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
					<xsl:comment>H.5.r.1a: Case Summary and Reporter's Comments Text </xsl:comment>
					<xsl:comment>H.5.r.1b: Case Summary and Reporter's Comments Language </xsl:comment>
					<value xsi:type="ED" language="{casesummarylang}" mediaType="text/plain">
						<xsl:value-of select="casesummary"/>
					</value>
					<author typeCode="AUT">
						<assignedEntity classCode="ASSIGNED">
							<code code="{$Reporter}" codeSystem="{$oidAssignedEntityRoleCode}" codeSystemVersion="{$emaoidAssignedEntityRoleCodeVersion}"/>
						</assignedEntity>
					</author>
				</observationEvent>
			</component>
		</xsl:if>
	</xsl:template>


	<xsl:template match="studyidentification" mode="MFDS-studyidentification">
		<xsl:if test="string-length(krobservestudytype) > 0">
			<xsl:comment>C.5.4.KR.1: Other Studies Type </xsl:comment>
			<subjectOf2 typeCode="SUBJ">
				<investigationCharacteristic classCode="OBS" moodCode="EVN">
					<code code="1" codeSystem="{$OidKRObserveStudyCode}" codeSystemVersion="1.0" />
					<value xsi:type="CE" code="{krobservestudytype}" codeSystem="{$OidKRObservedStudy}" codeSystemVersion="1.0"/>
				</investigationCharacteristic>
			</subjectOf2>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>