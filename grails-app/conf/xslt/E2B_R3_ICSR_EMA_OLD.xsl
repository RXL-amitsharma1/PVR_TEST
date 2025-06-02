<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="urn:hl7-org:v3" xmlns:mif="urn:hl7-org:v3/mif">
<!--<xsl:include href="upgrade.xsl"/>
	<xsl:include href="upgrade-m.xsl"/>
	<xsl:include href="upgrade-a1.xsl"/>-->
	<xsl:output indent="yes" method="xml" omit-xml-declaration="no" encoding="utf-8"/>
	<xsl:strip-space elements="*"/>
	<!--ICH ICSR : conversion of the main structure incl. root element and controlActProcess
	E2B(R2): root element "ichicsr"
	E2B(R3): root element "PORR_IN049016UV"
	-->
	<xsl:template match="/">
		<MCCI_IN200100UV01 ITSVersion="XML_1.0">
		<!--edit schema location as needed-->
		<xsl:attribute name="xsi:schemaLocation">urn:hl7-org:v3 http://eudravigilance.ema.europa.eu/XSD/multicacheschemas/MCCI_IN200100UV01.xsd</xsl:attribute>
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

<!-- C.2.r.4: Qualification-->
<xsl:variable name="emaoidC2r4CLVersion">2.0</xsl:variable>

<!-- C.4.r.1 Literature Reference -->
<xsl:variable name="ichoidC4rCLVersion">2.16.840.1.113883.3.989.5.1.10.1.4</xsl:variable>

<xsl:variable name="ZIPSTREAM_COMPRESSION_ALGORITHM">DF</xsl:variable>

<!-- C.5.4: Study type in which the reaction(s)/event(s) were observed-->
<xsl:variable name="emaoidC54CLVersion">2.0</xsl:variable>

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
<xsl:apply-templates select="patient/summary/narrativesendercommentnative"/>
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

<xsl:apply-templates select="casesummarynarrative"/> 

<!--	A.1.9 - Does this Case Fulfil the Local Criteria for an Expedited Report?
<xsl:comment> C.1.7: Does This Case Fulfil the Local Criteria for an Expedited Report? </xsl:comment>
<xsl:call-template name="fulfillexpeditecriteria"/>-->
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

<!--A.1.13 Report Nullification / Amendment-->
<xsl:if test="string-length(casenullificationoramendment)>0">
<subjectOf2 typeCode="SUBJ">
<investigationCharacteristic classCode="OBS" moodCode="EVN">
<code code="{casenullificationoramendment}" displayName="nullificationAmendmentCode"/>
<xsl:choose>
<xsl:when test="casenullificationoramendment= 1 and (casenullificationoramendmentcsv) > 0" >
<value xsi:type="CE" code="1" codeSystemVersion="{casenullificationoramendmentcsv}" displayName="nullificationAmendmentCode"/>
</xsl:when>
<xsl:when test="casenullificationoramendment= 2 and (casenullificationoramendmentcsv) > 0">
<value xsi:type="CE" code="2" codeSystemVersion="{casenullificationoramendmentcsv}" displayName="nullificationAmendmentCode"/>
</xsl:when>
<xsl:when test="casenullificationoramendment= 01 and (casenullificationoramendmentcsv) > 0">
<value xsi:type="CE" code="1" codeSystemVersion="{casenullificationoramendmentcsv}" displayName="nullificationAmendmentCode"/>
</xsl:when>
<xsl:when test="casenullificationoramendment= 02 and (casenullificationoramendmentcsv) > 0">
<value xsi:type="CE" code="2" codeSystemVersion="{casenullificationoramendmentcsv}" displayName="nullificationAmendmentCode"/>
</xsl:when>
<xsl:when test="casenullificationoramendment= 001 and (casenullificationoramendmentcsv) > 0">
<value xsi:type="CE" code="1" codeSystemVersion="{casenullificationoramendmentcsv}" displayName="nullificationAmendmentCode"/>
</xsl:when>
<xsl:when test="casenullificationoramendment= 002 and (casenullificationoramendmentcsv) > 0">
<value xsi:type="CE" code="2" codeSystemVersion="{casenullificationoramendmentcsv}" displayName="nullificationAmendmentCode"/>
</xsl:when>
<xsl:when test="casenullificationoramendment= 'Nullification' and (casenullificationoramendmentcsv) > 0">
<value xsi:type="CE" code="1" codeSystemVersion="{casenullificationoramendmentcsv}" displayName="nullificationAmendmentCode"/>

</xsl:when>
<xsl:when test="casenullificationoramendment= 'Amendment' and (casenullificationoramendmentcsv) > 0">
<value xsi:type="CE" code="2" codeSystemVersion="{casenullificationoramendmentcsv}" displayName="nullificationAmendmentCode"/> </xsl:when>

<xsl:when test="casenullificationoramendment= 'nullification' and (casenullificationoramendmentcsv) > 0">
<value xsi:type="CE" code="1" codeSystemVersion="{casenullificationoramendmentcsv}" displayName="nullificationAmendmentCode"/>
</xsl:when>
<xsl:when test="casenullificationoramendment= 'amendment' and (casenullificationoramendmentcsv) > 0">
<value xsi:type="CE" code="2" codeSystemVersion="{casenullificationoramendmentcsv}" displayName="nullificationAmendmentCode"/>
</xsl:when>

<xsl:when test="casenullificationoramendment= 'NULLIFICATION' and (casenullificationoramendmentcsv) > 0">
<value xsi:type="CE" code="1" codeSystemVersion="{casenullificationoramendmentcsv}" displayName="nullificationAmendmentCode"/>

</xsl:when>
<xsl:when test="casenullificationoramendment= 'AMENDMENT' and (casenullificationoramendmentcsv) > 0">
<value xsi:type="CE" code="2" codeSystemVersion="{casenullificationoramendmentcsv}" displayName="nullificationAmendmentCode"/> </xsl:when>
</xsl:choose>
</investigationCharacteristic>
</subjectOf2>
<xsl:comment>C.1.11.1:Report Nullification / Amendment</xsl:comment>
<xsl:comment>C.1.11.1.CSV:Report Nullification / Amendment Code System Version</xsl:comment>
</xsl:if>
<!--A.1.13.1 Reason for Nullification / Amendment-->
<xsl:if test="nullificationoramendmentreason > 0 and casenullificationoramendment > 0">
<subjectOf2 typeCode="SUBJ">
<investigationCharacteristic classCode="OBS" moodCode="EVN">
<code code="{casenullificationoramendment}" codeSystem="{$oidReportCharacterizationCode}" displayName="nullificationAmendmentReason"/>
<value xsi:type="CE">
<originalText><xsl:value-of select="nullificationoramendmentreason"/></originalText>
</value>
</investigationCharacteristic>
</subjectOf2>
<xsl:comment>C.1.11.2:Reason for Nullification Amendment</xsl:comment>
</xsl:if>
<!--A.1.13.1 Reason for Nullification / Amendment-->	
</investigationEvent>
</subject>
</xsl:template>
<xsl:for-each select="narrativesendercommentnative/Native">
<subject typeCode="SUBJ">
<investigationEvent classCode="INVSTG" moodCode="EVN">
<xsl:if test="string-length(summaryandreportercomments) and string-length(summaryandreportercommentslang) > 0" >
<component typeCode="COMP">
<code code="{$SummaryAndComment}" codeSystem="{$oidObservationCode}"/>
<author typeCode="AUT">
<assignedEntity classCode="ASSIGNED">
<code code="{$Reporter}" codeSystem="{$oidAssignedEntityRoleCode}"/>
<observationEvent moodCode="EVN" classCode="OBS">
<value language="{summaryandreportercommentslang}" xsi:type="ED" mediaType="text/plain"><xsl:value-of select="summaryandreportercomments"/></value>
</observationEvent>
</assignedEntity>
</author>
<xsl:comment> H.5.1a and H.5.1b Narrative and Sendercomment in Native Languague </xsl:comment>
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
<code codeSystem="{$oidichreferencesource}" code="{$documentsHeldBySender}" displayName="documentsHeldBySender"/>
<title>
<xsl:value-of select="documentlist"/>
</title>
<xsl:comment>C.1.6.1.r.1:Documents Held by Sender</xsl:comment>
<xsl:variable name="MediaType">
<xsl:value-of select="substring-after(mediatype2,'.')"/>
</xsl:variable>

<xsl:if test="$MediaType= 'txt'">
<text mediaType="text/plain" representation="TXT">
<xsl:value-of select="includedocuments"/>
</text>
<xsl:comment>C.1.6.1.r.2:Included Documents</xsl:comment>
</xsl:if>
<xsl:if test="$MediaType= 'pdf'">
<text mediaType="application/pdf" representation="B64" compression="DF" >
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
</xsl:if>
<xsl:if test="$MediaType= 'psd'">
<text mediaType="application/octet-stream" representation="B64" compression="DF">
<xsl:value-of select="includedocuments"/>
</text>
</xsl:if>
<xsl:if test="$MediaType= 'tif'">
<text mediaType="image/tiff" representation="B64" compression="DF">
<xsl:value-of select="includedocuments"/>
</text>
</xsl:if>
<xsl:if test="$MediaType= 'docx'">
<text mediaType="application/vnd.openxmlformats-officedocument.wordprocessingml.document" representation="B64" compression="DF">
<xsl:value-of select="includedocuments"/>
</text>
</xsl:if>
<xsl:if test="$MediaType= 'xls'">
<text mediaType="application/vnd.ms-excel" representation="B64" compression="DF">
<xsl:value-of select="includedocuments"/>
</text>
</xsl:if>
<xsl:if test="$MediaType= 'xlsx'">
<text mediaType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" representation="B64" compression="DF">
<xsl:value-of select="includedocuments"/>
</text>
</xsl:if>
<xsl:if test="$MediaType= 'vsd'">
<text mediaType="application/x-visio" representation="B64" compression="DF">
<xsl:value-of select="includedocuments"/>
</text>
</xsl:if>
<xsl:if test="$MediaType= 'rtf'">
<text mediaType="application/rtf" representation="B64" compression="DF">
<xsl:value-of select="includedocuments"/>
</text>
</xsl:if>
<xsl:if test="$MediaType= 'doc'">
<text mediaType="application/msword" representation="B64" compression="DF">
<xsl:value-of select="includedocuments"/>
</text>
</xsl:if>
<xsl:if test="$MediaType= 'ps'">
<text mediaType="application/postscript" representation="B64" compression="DF">
<xsl:value-of select="includedocuments"/>
</text>
</xsl:if>
<xsl:if test="$MediaType= 'mdb'">
<text mediaType="application/x-msaccess" representation="B64" compression="DF">
<xsl:value-of select="includedocuments"/>
</text>
</xsl:if>
<xsl:if test="$MediaType= 'bmp'">
<text mediaType="image/bmp" representation="B64" compression="DF">
<xsl:value-of select="includedocuments"/>
</text>
</xsl:if>
<xsl:if test="$MediaType= 'xml'">
<text mediaType="text/xml" representation="B64" compression="DF">
<xsl:value-of select="includedocuments"/>
</text>
</xsl:if>
<xsl:if test="$MediaType= 'sgm'">
<text mediaType="text/sgml" representation="B64" compression="DF">
<xsl:value-of select="includedocuments"/>
</text>
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
	E2B(R3): element "relatedInvestigation"
	-->
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

<telecom>
<xsl:attribute name="value">
<xsl:text>tel:</xsl:text>
<xsl:if test="string-length(reportertelcountrycode) > 0">+<xsl:value-of select="reportertelcountrycode"/><xsl:text> </xsl:text></xsl:if>
<xsl:value-of select="reportertel"/>
<xsl:if test="string-length(reportertelextension) > 0"><xsl:text></xsl:text><xsl:value-of select="reportertelextension"/></xsl:if>
</xsl:attribute></telecom>
<xsl:comment>C.2.r.2.7:Reporter’s Telephone</xsl:comment>
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
<!--A.2.r.1.4 Reporter Qualification-->
<xsl:if test="string-length(qualification)>0" displayName="sourceReport">
<xsl:comment>C.2.r.4: Qualification</xsl:comment>
<xsl:comment>C.2.r.4.CSV: Qualification Code System Version</xsl:comment>
	<asQualifiedEntity classCode="QUAL">
	<xsl:choose>
		<xsl:when test="qualification= 1"><code code="{qualification}" displayName="Physician" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>
		<xsl:when test="qualification= 2"><code code="{qualification}" displayName="Pharmacist" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>
		<xsl:when test="qualification= 3"><code code="{qualification}" displayName="Otherhealthprofessional" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>
		<xsl:when test="qualification= 4"><code code="{qualification}" displayName="Lawyer" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>
		<xsl:when test="qualification= 5"><code code="{qualification}" displayName="Consumerorothernonhealth" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>
		
		<xsl:when test="qualification= 01"><code code="{qualification}" displayName="Physician" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>
		<xsl:when test="qualification= 02"><code code="{qualification}" displayName="Pharmacist" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>
		<xsl:when test="qualification= 03"><code code="{qualification}" displayName="Otherhealthprofessional" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>
		<xsl:when test="qualification= 04"><code code="{qualification}" displayName="Lawyer" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>
		<xsl:when test="qualification= 05"><code code="{qualification}" displayName="Consumerorothernonhealth" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>

		<xsl:when test="qualification= 001"><code code="{qualification}" displayName="Physician" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>
		<xsl:when test="qualification= 002"><code code="{qualification}" displayName="Pharmacist" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>
		<xsl:when test="qualification= 003"><code code="{qualification}" displayName="Otherhealthprofessional" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>
		<xsl:when test="qualification= 004"><code code="{qualification}" displayName="Lawyer" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>
		<xsl:when test="qualification= 005"><code code="{qualification}" displayName="Consumerorothernonhealth" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>
	
		<xsl:when test="qualification= 'C16960'"><code code="{qualification}" displayName="Patient" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>
		<xsl:when test="qualification= 'C42709'"><code code="{qualification}" displayName="Parent" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>
		<xsl:when test="qualification= 'c16960'"><code code="{qualification}" displayName="Patient" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>
		<xsl:when test="qualification= 'c42709'"><code code="{qualification}" displayName="Parent" codeSystem="{$oidQualification}" codeSystemVersion="{$emaoidC2r4CLVersion}"/> </xsl:when>

		<xsl:otherwise><code nullFlavor="UNK"/></xsl:otherwise>
	</xsl:choose>
	</asQualifiedEntity>
</xsl:if>
<!--A.2.r.1.3 Reporter Country-->

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
<xsl:if test="string-length(reportercountry) > 0">
<asLocatedEntity classCode="LOCE">
<location determinerCode="INSTANCE" classCode="COUNTRY">
<xsl:choose>
<xsl:when test="reportercountry = 'Afghanistan'"><code codeSystem="{$oidISOCountry}" code="{$country1}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Aland Islands !Åland Islands'"><code codeSystem="{$oidISOCountry}" code="{$country2}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Albania'"><code codeSystem="{$oidISOCountry}" code="{$country3}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Algeria'"><code codeSystem="{$oidISOCountry}" code="{$country4}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'American Samoa'"><code codeSystem="{$oidISOCountry}" code="{$country5}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Andorra'"><code codeSystem="{$oidISOCountry}" code="{$country6}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Angola'"><code codeSystem="{$oidISOCountry}" code="{$country7}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Anguilla'"><code codeSystem="{$oidISOCountry}" code="{$country8}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Antarctica'"><code codeSystem="{$oidISOCountry}" code="{$country9}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Antigua and Barbuda'"><code codeSystem="{$oidISOCountry}" code="{$country10}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Argentina'"><code codeSystem="{$oidISOCountry}" code="{$country11}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Armenia'"><code codeSystem="{$oidISOCountry}" code="{$country12}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Aruba'"><code codeSystem="{$oidISOCountry}" code="{$country13}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Australia'"><code codeSystem="{$oidISOCountry}" code="{$country14}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Austria'"><code codeSystem="{$oidISOCountry}" code="{$country15}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Azerbaijan'"><code codeSystem="{$oidISOCountry}" code="{$country16}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Bahamas'"><code codeSystem="{$oidISOCountry}" code="{$country17}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Bahrain'"><code codeSystem="{$oidISOCountry}" code="{$country18}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Bangladesh'"><code codeSystem="{$oidISOCountry}" code="{$country19}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Barbados'"><code codeSystem="{$oidISOCountry}" code="{$country20}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Belarus'"><code codeSystem="{$oidISOCountry}" code="{$country21}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Belgium'"><code codeSystem="{$oidISOCountry}" code="{$country22}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Belize'"><code codeSystem="{$oidISOCountry}" code="{$country23}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Benin'"><code codeSystem="{$oidISOCountry}" code="{$country24}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Bermuda'"><code codeSystem="{$oidISOCountry}" code="{$country25}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Bhutan'"><code codeSystem="{$oidISOCountry}" code="{$country26}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Bolivia (Plurinational State of)'"><code codeSystem="{$oidISOCountry}" code="{$country27}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Bonaire, Sint Eustatius and Saba'"><code codeSystem="{$oidISOCountry}" code="{$country28}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Bosnia and Herzegovina'"><code codeSystem="{$oidISOCountry}" code="{$country29}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Botswana'"><code codeSystem="{$oidISOCountry}" code="{$country30}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Bouvet Island'"><code codeSystem="{$oidISOCountry}" code="{$country31}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Brazil'"><code codeSystem="{$oidISOCountry}" code="{$country32}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'British Indian Ocean Territory'"><code codeSystem="{$oidISOCountry}" code="{$country33}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Brunei Darussalam'"><code codeSystem="{$oidISOCountry}" code="{$country34}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Bulgaria'"><code codeSystem="{$oidISOCountry}" code="{$country35}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Burkina Faso'"><code codeSystem="{$oidISOCountry}" code="{$country36}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Burundi'"><code codeSystem="{$oidISOCountry}" code="{$country37}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Cabo Verde'"><code codeSystem="{$oidISOCountry}" code="{$country38}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Cambodia'"><code codeSystem="{$oidISOCountry}" code="{$country39}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Cameroon'"><code codeSystem="{$oidISOCountry}" code="{$country40}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Canada'"><code codeSystem="{$oidISOCountry}" code="{$country41}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Cayman Islands'"><code codeSystem="{$oidISOCountry}" code="{$country42}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Central African Republic'"><code codeSystem="{$oidISOCountry}" code="{$country43}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Chad'"><code codeSystem="{$oidISOCountry}" code="{$country44}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Chile'"><code codeSystem="{$oidISOCountry}" code="{$country45}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'China'"><code codeSystem="{$oidISOCountry}" code="{$country46}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Christmas Island'"><code codeSystem="{$oidISOCountry}" code="{$country47}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Cocos (Keeling) Islands'"><code codeSystem="{$oidISOCountry}" code="{$country48}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Colombia'"><code codeSystem="{$oidISOCountry}" code="{$country49}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Comoros'"><code codeSystem="{$oidISOCountry}" code="{$country50}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Congo'"><code codeSystem="{$oidISOCountry}" code="{$country51}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Congo (Democratic Republic of the)'"><code codeSystem="{$oidISOCountry}" code="{$country52}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Cook Islands'"><code codeSystem="{$oidISOCountry}" code="{$country53}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Costa Rica'"><code codeSystem="{$oidISOCountry}" code="{$country54}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'European Union'"><code codeSystem="{$oidISOCountry}" code="{$country250}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Croatia'"><code codeSystem="{$oidISOCountry}" code="{$country56}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Cuba'"><code codeSystem="{$oidISOCountry}" code="{$country57}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Curacao !Curaçao'"><code codeSystem="{$oidISOCountry}" code="{$country58}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Cyprus'"><code codeSystem="{$oidISOCountry}" code="{$country59}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Czech Republic'"><code codeSystem="{$oidISOCountry}" code="{$country60}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Denmark'"><code codeSystem="{$oidISOCountry}" code="{$country61}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Djibouti'"><code codeSystem="{$oidISOCountry}" code="{$country62}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Dominica'"><code codeSystem="{$oidISOCountry}" code="{$country63}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Dominican Republic'"><code codeSystem="{$oidISOCountry}" code="{$country64}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Ecuador'"><code codeSystem="{$oidISOCountry}" code="{$country65}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Egypt'"><code codeSystem="{$oidISOCountry}" code="{$country66}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'El Salvador'"><code codeSystem="{$oidISOCountry}" code="{$country67}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Equatorial Guinea'"><code codeSystem="{$oidISOCountry}" code="{$country68}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Eritrea'"><code codeSystem="{$oidISOCountry}" code="{$country69}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Estonia'"><code codeSystem="{$oidISOCountry}" code="{$country70}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Ethiopia'"><code codeSystem="{$oidISOCountry}" code="{$country71}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Falkland Islands (Malvinas)'"><code codeSystem="{$oidISOCountry}" code="{$country72}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Faroe Islands'"><code codeSystem="{$oidISOCountry}" code="{$country73}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Fiji'"><code codeSystem="{$oidISOCountry}" code="{$country74}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Finland'"><code codeSystem="{$oidISOCountry}" code="{$country75}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'France'"><code codeSystem="{$oidISOCountry}" code="{$country76}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'French Guiana'"><code codeSystem="{$oidISOCountry}" code="{$country77}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'French Polynesia'"><code codeSystem="{$oidISOCountry}" code="{$country78}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'French Southern Territories'"><code codeSystem="{$oidISOCountry}" code="{$country79}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Gabon'"><code codeSystem="{$oidISOCountry}" code="{$country80}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Gambia'"><code codeSystem="{$oidISOCountry}" code="{$country81}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Georgia'"><code codeSystem="{$oidISOCountry}" code="{$country82}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Germany'"><code codeSystem="{$oidISOCountry}" code="{$country83}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Ghana'"><code codeSystem="{$oidISOCountry}" code="{$country84}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Gibraltar'"><code codeSystem="{$oidISOCountry}" code="{$country85}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Greece'"><code codeSystem="{$oidISOCountry}" code="{$country86}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Greenland'"><code codeSystem="{$oidISOCountry}" code="{$country87}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Grenada'"><code codeSystem="{$oidISOCountry}" code="{$country88}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Guadeloupe'"><code codeSystem="{$oidISOCountry}" code="{$country89}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Guam'"><code codeSystem="{$oidISOCountry}" code="{$country90}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Guatemala'"><code codeSystem="{$oidISOCountry}" code="{$country91}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Guernsey'"><code codeSystem="{$oidISOCountry}" code="{$country92}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Guinea'"><code codeSystem="{$oidISOCountry}" code="{$country93}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Guinea-Bissau'"><code codeSystem="{$oidISOCountry}" code="{$country94}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Guyana'"><code codeSystem="{$oidISOCountry}" code="{$country95}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Haiti'"><code codeSystem="{$oidISOCountry}" code="{$country96}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Heard Island and McDonald Islands'"><code codeSystem="{$oidISOCountry}" code="{$country97}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Holy See'"><code codeSystem="{$oidISOCountry}" code="{$country98}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Honduras'"><code codeSystem="{$oidISOCountry}" code="{$country99}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Hong Kong'"><code codeSystem="{$oidISOCountry}" code="{$country100}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Hungary'"><code codeSystem="{$oidISOCountry}" code="{$country101}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Iceland'"><code codeSystem="{$oidISOCountry}" code="{$country102}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'India'"><code codeSystem="{$oidISOCountry}" code="{$country103}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Indonesia'"><code codeSystem="{$oidISOCountry}" code="{$country104}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Iran (Islamic Republic of)'"><code codeSystem="{$oidISOCountry}" code="{$country105}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Iraq'"><code codeSystem="{$oidISOCountry}" code="{$country106}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Ireland'"><code codeSystem="{$oidISOCountry}" code="{$country107}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Isle of Man'"><code codeSystem="{$oidISOCountry}" code="{$country108}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Israel'"><code codeSystem="{$oidISOCountry}" code="{$country109}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Italy'"><code codeSystem="{$oidISOCountry}" code="{$country110}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Jamaica'"><code codeSystem="{$oidISOCountry}" code="{$country111}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Japan'"><code codeSystem="{$oidISOCountry}" code="{$country112}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Jersey'"><code codeSystem="{$oidISOCountry}" code="{$country113}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Jordan'"><code codeSystem="{$oidISOCountry}" code="{$country114}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Kazakhstan'"><code codeSystem="{$oidISOCountry}" code="{$country115}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Kenya'"><code codeSystem="{$oidISOCountry}" code="{$country116}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Kiribati'"><code codeSystem="{$oidISOCountry}" code="{$country117}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Kuwait'"><code codeSystem="{$oidISOCountry}" code="{$country120}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Kyrgyzstan'"><code codeSystem="{$oidISOCountry}" code="{$country121}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Latvia'"><code codeSystem="{$oidISOCountry}" code="{$country123}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Lebanon'"><code codeSystem="{$oidISOCountry}" code="{$country124}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Lesotho'"><code codeSystem="{$oidISOCountry}" code="{$country125}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Liberia'"><code codeSystem="{$oidISOCountry}" code="{$country126}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Libya'"><code codeSystem="{$oidISOCountry}" code="{$country127}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Liechtenstein'"><code codeSystem="{$oidISOCountry}" code="{$country128}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Lithuania'"><code codeSystem="{$oidISOCountry}" code="{$country129}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Luxembourg'"><code codeSystem="{$oidISOCountry}" code="{$country130}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Macao'"><code codeSystem="{$oidISOCountry}" code="{$country131}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Macedonia (the former Yugoslav Republic of)'"><code codeSystem="{$oidISOCountry}" code="{$country132}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Madagascar'"><code codeSystem="{$oidISOCountry}" code="{$country133}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Malawi'"><code codeSystem="{$oidISOCountry}" code="{$country134}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Malaysia'"><code codeSystem="{$oidISOCountry}" code="{$country135}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Maldives'"><code codeSystem="{$oidISOCountry}" code="{$country136}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Mali'"><code codeSystem="{$oidISOCountry}" code="{$country137}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Malta'"><code codeSystem="{$oidISOCountry}" code="{$country138}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Marshall Islands'"><code codeSystem="{$oidISOCountry}" code="{$country139}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Martinique'"><code codeSystem="{$oidISOCountry}" code="{$country140}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Mauritania'"><code codeSystem="{$oidISOCountry}" code="{$country141}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Mauritius'"><code codeSystem="{$oidISOCountry}" code="{$country142}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Mayotte'"><code codeSystem="{$oidISOCountry}" code="{$country143}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Mexico'"><code codeSystem="{$oidISOCountry}" code="{$country144}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Micronesia (Federated States of)'"><code codeSystem="{$oidISOCountry}" code="{$country145}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Moldova (Republic of)'"><code codeSystem="{$oidISOCountry}" code="{$country146}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Monaco'"><code codeSystem="{$oidISOCountry}" code="{$country147}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Mongolia'"><code codeSystem="{$oidISOCountry}" code="{$country148}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Montenegro'"><code codeSystem="{$oidISOCountry}" code="{$country149}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Montserrat'"><code codeSystem="{$oidISOCountry}" code="{$country150}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Morocco'"><code codeSystem="{$oidISOCountry}" code="{$country151}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Mozambique'"><code codeSystem="{$oidISOCountry}" code="{$country152}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Myanmar'"><code codeSystem="{$oidISOCountry}" code="{$country153}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Namibia'"><code codeSystem="{$oidISOCountry}" code="{$country154}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Nauru'"><code codeSystem="{$oidISOCountry}" code="{$country155}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Nepal'"><code codeSystem="{$oidISOCountry}" code="{$country156}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Netherlands'"><code codeSystem="{$oidISOCountry}" code="{$country157}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'New Caledonia'"><code codeSystem="{$oidISOCountry}" code="{$country158}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'New Zealand'"><code codeSystem="{$oidISOCountry}" code="{$country159}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Nicaragua'"><code codeSystem="{$oidISOCountry}" code="{$country160}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Niger'"><code codeSystem="{$oidISOCountry}" code="{$country161}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Nigeria'"><code codeSystem="{$oidISOCountry}" code="{$country162}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Niue'"><code codeSystem="{$oidISOCountry}" code="{$country163}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Norfolk Island'"><code codeSystem="{$oidISOCountry}" code="{$country164}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Northern Mariana Islands'"><code codeSystem="{$oidISOCountry}" code="{$country165}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Norway'"><code codeSystem="{$oidISOCountry}" code="{$country166}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Oman'"><code codeSystem="{$oidISOCountry}" code="{$country167}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Pakistan'"><code codeSystem="{$oidISOCountry}" code="{$country168}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Palau'"><code codeSystem="{$oidISOCountry}" code="{$country169}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Palestine, State of'"><code codeSystem="{$oidISOCountry}" code="{$country170}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Panama'"><code codeSystem="{$oidISOCountry}" code="{$country171}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Papua New Guinea'"><code codeSystem="{$oidISOCountry}" code="{$country172}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Paraguay'"><code codeSystem="{$oidISOCountry}" code="{$country173}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Peru'"><code codeSystem="{$oidISOCountry}" code="{$country174}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Philippines'"><code codeSystem="{$oidISOCountry}" code="{$country175}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Pitcairn'"><code codeSystem="{$oidISOCountry}" code="{$country176}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Poland'"><code codeSystem="{$oidISOCountry}" code="{$country177}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Portugal'"><code codeSystem="{$oidISOCountry}" code="{$country178}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Puerto Rico'"><code codeSystem="{$oidISOCountry}" code="{$country179}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Qatar'"><code codeSystem="{$oidISOCountry}" code="{$country180}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Reunion !Réunion'"><code codeSystem="{$oidISOCountry}" code="{$country181}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Romania'"><code codeSystem="{$oidISOCountry}" code="{$country182}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Russian Federation'"><code codeSystem="{$oidISOCountry}" code="{$country183}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Rwanda'"><code codeSystem="{$oidISOCountry}" code="{$country184}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Saint Barthelemy !Saint Barthélemy'"><code codeSystem="{$oidISOCountry}" code="{$country185}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Saint Helena, Ascension and Tristan da Cunha'"><code codeSystem="{$oidISOCountry}" code="{$country186}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Saint Kitts and Nevis'"><code codeSystem="{$oidISOCountry}" code="{$country187}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Saint Lucia'"><code codeSystem="{$oidISOCountry}" code="{$country188}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Saint Martin (French part)'"><code codeSystem="{$oidISOCountry}" code="{$country189}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Saint Pierre and Miquelon'"><code codeSystem="{$oidISOCountry}" code="{$country190}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Saint Vincent and the Grenadines'"><code codeSystem="{$oidISOCountry}" code="{$country191}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Samoa'"><code codeSystem="{$oidISOCountry}" code="{$country192}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'San Marino'"><code codeSystem="{$oidISOCountry}" code="{$country193}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Sao Tome and Principe'"><code codeSystem="{$oidISOCountry}" code="{$country194}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Saudi Arabia'"><code codeSystem="{$oidISOCountry}" code="{$country195}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Senegal'"><code codeSystem="{$oidISOCountry}" code="{$country196}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Serbia'"><code codeSystem="{$oidISOCountry}" code="{$country197}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Seychelles'"><code codeSystem="{$oidISOCountry}" code="{$country198}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Sierra Leone'"><code codeSystem="{$oidISOCountry}" code="{$country199}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Singapore'"><code codeSystem="{$oidISOCountry}" code="{$country200}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Sint Maarten (Dutch part)'"><code codeSystem="{$oidISOCountry}" code="{$country201}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Slovakia'"><code codeSystem="{$oidISOCountry}" code="{$country202}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Slovenia'"><code codeSystem="{$oidISOCountry}" code="{$country203}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Solomon Islands'"><code codeSystem="{$oidISOCountry}" code="{$country204}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Somalia'"><code codeSystem="{$oidISOCountry}" code="{$country205}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'South Africa'"><code codeSystem="{$oidISOCountry}" code="{$country206}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'South Georgia and the South Sandwich Islands'"><code codeSystem="{$oidISOCountry}" code="{$country207}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'South Sudan'"><code codeSystem="{$oidISOCountry}" code="{$country208}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Spain'"><code codeSystem="{$oidISOCountry}" code="{$country209}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Sri Lanka'"><code codeSystem="{$oidISOCountry}" code="{$country210}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Sudan'"><code codeSystem="{$oidISOCountry}" code="{$country211}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Suriname'"><code codeSystem="{$oidISOCountry}" code="{$country212}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Svalbard and Jan Mayen'"><code codeSystem="{$oidISOCountry}" code="{$country213}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Swaziland'"><code codeSystem="{$oidISOCountry}" code="{$country214}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Sweden'"><code codeSystem="{$oidISOCountry}" code="{$country215}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Switzerland'"><code codeSystem="{$oidISOCountry}" code="{$country216}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Syrian Arab Republic'"><code codeSystem="{$oidISOCountry}" code="{$country217}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Taiwan, Province of China[a]'"><code codeSystem="{$oidISOCountry}" code="{$country218}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Tajikistan'"><code codeSystem="{$oidISOCountry}" code="{$country219}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Tanzania, United Republic of'"><code codeSystem="{$oidISOCountry}" code="{$country220}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Thailand'"><code codeSystem="{$oidISOCountry}" code="{$country221}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Timor-Leste'"><code codeSystem="{$oidISOCountry}" code="{$country222}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Togo'"><code codeSystem="{$oidISOCountry}" code="{$country223}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Tokelau'"><code codeSystem="{$oidISOCountry}" code="{$country224}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Tonga'"><code codeSystem="{$oidISOCountry}" code="{$country225}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Trinidad and Tobago'"><code codeSystem="{$oidISOCountry}" code="{$country226}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Tunisia'"><code codeSystem="{$oidISOCountry}" code="{$country227}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Turkey'"><code codeSystem="{$oidISOCountry}" code="{$country228}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Turkmenistan'"><code codeSystem="{$oidISOCountry}" code="{$country229}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Turks and Caicos Islands'"><code codeSystem="{$oidISOCountry}" code="{$country230}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Tuvalu'"><code codeSystem="{$oidISOCountry}" code="{$country231}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Uganda'"><code codeSystem="{$oidISOCountry}" code="{$country232}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Ukraine'"><code codeSystem="{$oidISOCountry}" code="{$country233}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'United Arab Emirates'"><code codeSystem="{$oidISOCountry}" code="{$country234}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'United Kingdom of Great Britain and Northern Ireland'"><code codeSystem="{$oidISOCountry}" code="{$country235}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'United Kingdom'"><code codeSystem="{$oidISOCountry}" code="{$country235}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'United States of America'"><code codeSystem="{$oidISOCountry}" code="{$country236}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'United States'"><code codeSystem="{$oidISOCountry}" code="{$country236}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'United States Minor Outlying Islands'"><code codeSystem="{$oidISOCountry}" code="{$country237}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Uruguay'"><code codeSystem="{$oidISOCountry}" code="{$country238}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Uzbekistan'"><code codeSystem="{$oidISOCountry}" code="{$country239}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Vanuatu'"><code codeSystem="{$oidISOCountry}" code="{$country240}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Venezuela (Bolivarian Republic of)'"><code codeSystem="{$oidISOCountry}" code="{$country241}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Viet Nam'"><code codeSystem="{$oidISOCountry}" code="{$country242}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Virgin Islands (British)'"><code codeSystem="{$oidISOCountry}" code="{$country243}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Virgin Islands (U.S.)'"><code codeSystem="{$oidISOCountry}" code="{$country244}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Wallis and Futuna'"><code codeSystem="{$oidISOCountry}" code="{$country245}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Western Sahara'"><code codeSystem="{$oidISOCountry}" code="{$country246}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Yemen'"><code codeSystem="{$oidISOCountry}" code="{$country247}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Zambia'"><code codeSystem="{$oidISOCountry}" code="{$country248}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'Zimbabwe'"><code codeSystem="{$oidISOCountry}" code="{$country249}" displayName="sourceReport"/> </xsl:when>


<xsl:when test="reportercountry = 'afghanistan'"><code codeSystem="{$oidISOCountry}" code="{$country1}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'aland islands !åland islands'"><code codeSystem="{$oidISOCountry}" code="{$country2}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'albania'"><code codeSystem="{$oidISOCountry}" code="{$country3}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'algeria'"><code codeSystem="{$oidISOCountry}" code="{$country4}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'american samoa'"><code codeSystem="{$oidISOCountry}" code="{$country5}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'andorra'"><code codeSystem="{$oidISOCountry}" code="{$country6}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'angola'"><code codeSystem="{$oidISOCountry}" code="{$country7}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'anguilla'"><code codeSystem="{$oidISOCountry}" code="{$country8}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'antarctica'"><code codeSystem="{$oidISOCountry}" code="{$country9}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'antigua and barbuda'"><code codeSystem="{$oidISOCountry}" code="{$country10}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'argentina'"><code codeSystem="{$oidISOCountry}" code="{$country11}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'armenia'"><code codeSystem="{$oidISOCountry}" code="{$country12}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'aruba'"><code codeSystem="{$oidISOCountry}" code="{$country13}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'australia'"><code codeSystem="{$oidISOCountry}" code="{$country14}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'austria'"><code codeSystem="{$oidISOCountry}" code="{$country15}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'azerbaijan'"><code codeSystem="{$oidISOCountry}" code="{$country16}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bahamas'"><code codeSystem="{$oidISOCountry}" code="{$country17}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bahrain'"><code codeSystem="{$oidISOCountry}" code="{$country18}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bangladesh'"><code codeSystem="{$oidISOCountry}" code="{$country19}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'barbados'"><code codeSystem="{$oidISOCountry}" code="{$country20}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'belarus'"><code codeSystem="{$oidISOCountry}" code="{$country21}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'belgium'"><code codeSystem="{$oidISOCountry}" code="{$country22}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'belize'"><code codeSystem="{$oidISOCountry}" code="{$country23}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'benin'"><code codeSystem="{$oidISOCountry}" code="{$country24}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bermuda'"><code codeSystem="{$oidISOCountry}" code="{$country25}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bhutan'"><code codeSystem="{$oidISOCountry}" code="{$country26}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bolivia (plurinational state of)'"><code codeSystem="{$oidISOCountry}" code="{$country27}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bonaire, sint eustatius and saba'"><code codeSystem="{$oidISOCountry}" code="{$country28}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bosnia and herzegovina'"><code codeSystem="{$oidISOCountry}" code="{$country29}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'botswana'"><code codeSystem="{$oidISOCountry}" code="{$country30}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bouvet island'"><code codeSystem="{$oidISOCountry}" code="{$country31}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'brazil'"><code codeSystem="{$oidISOCountry}" code="{$country32}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'british indian ocean territory'"><code codeSystem="{$oidISOCountry}" code="{$country33}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'brunei darussalam'"><code codeSystem="{$oidISOCountry}" code="{$country34}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bulgaria'"><code codeSystem="{$oidISOCountry}" code="{$country35}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'burkina faso'"><code codeSystem="{$oidISOCountry}" code="{$country36}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'burundi'"><code codeSystem="{$oidISOCountry}" code="{$country37}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cabo verde'"><code codeSystem="{$oidISOCountry}" code="{$country38}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cambodia'"><code codeSystem="{$oidISOCountry}" code="{$country39}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cameroon'"><code codeSystem="{$oidISOCountry}" code="{$country40}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'canada'"><code codeSystem="{$oidISOCountry}" code="{$country41}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cayman islands'"><code codeSystem="{$oidISOCountry}" code="{$country42}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'central african republic'"><code codeSystem="{$oidISOCountry}" code="{$country43}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'chad'"><code codeSystem="{$oidISOCountry}" code="{$country44}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'chile'"><code codeSystem="{$oidISOCountry}" code="{$country45}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'china'"><code codeSystem="{$oidISOCountry}" code="{$country46}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'christmas island'"><code codeSystem="{$oidISOCountry}" code="{$country47}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cocos (keeling) islands'"><code codeSystem="{$oidISOCountry}" code="{$country48}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'colombia'"><code codeSystem="{$oidISOCountry}" code="{$country49}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'comoros'"><code codeSystem="{$oidISOCountry}" code="{$country50}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'congo'"><code codeSystem="{$oidISOCountry}" code="{$country51}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'congo (democratic republic of the)'"><code codeSystem="{$oidISOCountry}" code="{$country52}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cook islands'"><code codeSystem="{$oidISOCountry}" code="{$country53}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'costa rica'"><code codeSystem="{$oidISOCountry}" code="{$country54}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'european union'"><code codeSystem="{$oidISOCountry}" code="{$country250}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'croatia'"><code codeSystem="{$oidISOCountry}" code="{$country56}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cuba'"><code codeSystem="{$oidISOCountry}" code="{$country57}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'curacao !curaçao'"><code codeSystem="{$oidISOCountry}" code="{$country58}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cyprus'"><code codeSystem="{$oidISOCountry}" code="{$country59}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'czech republic'"><code codeSystem="{$oidISOCountry}" code="{$country60}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'denmark'"><code codeSystem="{$oidISOCountry}" code="{$country61}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'djibouti'"><code codeSystem="{$oidISOCountry}" code="{$country62}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'dominica'"><code codeSystem="{$oidISOCountry}" code="{$country63}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'dominican republic'"><code codeSystem="{$oidISOCountry}" code="{$country64}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ecuador'"><code codeSystem="{$oidISOCountry}" code="{$country65}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'egypt'"><code codeSystem="{$oidISOCountry}" code="{$country66}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'el salvador'"><code codeSystem="{$oidISOCountry}" code="{$country67}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'equatorial guinea'"><code codeSystem="{$oidISOCountry}" code="{$country68}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'eritrea'"><code codeSystem="{$oidISOCountry}" code="{$country69}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'estonia'"><code codeSystem="{$oidISOCountry}" code="{$country70}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ethiopia'"><code codeSystem="{$oidISOCountry}" code="{$country71}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'falkland islands (malvinas)'"><code codeSystem="{$oidISOCountry}" code="{$country72}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'faroe islands'"><code codeSystem="{$oidISOCountry}" code="{$country73}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'fiji'"><code codeSystem="{$oidISOCountry}" code="{$country74}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'finland'"><code codeSystem="{$oidISOCountry}" code="{$country75}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'france'"><code codeSystem="{$oidISOCountry}" code="{$country76}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'french guiana'"><code codeSystem="{$oidISOCountry}" code="{$country77}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'french polynesia'"><code codeSystem="{$oidISOCountry}" code="{$country78}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'french southern territories'"><code codeSystem="{$oidISOCountry}" code="{$country79}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gabon'"><code codeSystem="{$oidISOCountry}" code="{$country80}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gambia'"><code codeSystem="{$oidISOCountry}" code="{$country81}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'georgia'"><code codeSystem="{$oidISOCountry}" code="{$country82}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'germany'"><code codeSystem="{$oidISOCountry}" code="{$country83}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ghana'"><code codeSystem="{$oidISOCountry}" code="{$country84}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gibraltar'"><code codeSystem="{$oidISOCountry}" code="{$country85}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'greece'"><code codeSystem="{$oidISOCountry}" code="{$country86}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'greenland'"><code codeSystem="{$oidISOCountry}" code="{$country87}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'grenada'"><code codeSystem="{$oidISOCountry}" code="{$country88}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'guadeloupe'"><code codeSystem="{$oidISOCountry}" code="{$country89}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'guam'"><code codeSystem="{$oidISOCountry}" code="{$country90}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'guatemala'"><code codeSystem="{$oidISOCountry}" code="{$country91}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'guernsey'"><code codeSystem="{$oidISOCountry}" code="{$country92}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'guinea'"><code codeSystem="{$oidISOCountry}" code="{$country93}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'guinea-bissau'"><code codeSystem="{$oidISOCountry}" code="{$country94}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'guyana'"><code codeSystem="{$oidISOCountry}" code="{$country95}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'haiti'"><code codeSystem="{$oidISOCountry}" code="{$country96}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'heard island and mcdonald islands'"><code codeSystem="{$oidISOCountry}" code="{$country97}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'holy see'"><code codeSystem="{$oidISOCountry}" code="{$country98}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'honduras'"><code codeSystem="{$oidISOCountry}" code="{$country99}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'hong kong'"><code codeSystem="{$oidISOCountry}" code="{$country100}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'hungary'"><code codeSystem="{$oidISOCountry}" code="{$country101}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'iceland'"><code codeSystem="{$oidISOCountry}" code="{$country102}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'india'"><code codeSystem="{$oidISOCountry}" code="{$country103}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'indonesia'"><code codeSystem="{$oidISOCountry}" code="{$country104}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'iran (islamic republic of)'"><code codeSystem="{$oidISOCountry}" code="{$country105}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'iraq'"><code codeSystem="{$oidISOCountry}" code="{$country106}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ireland'"><code codeSystem="{$oidISOCountry}" code="{$country107}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'isle of man'"><code codeSystem="{$oidISOCountry}" code="{$country108}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'israel'"><code codeSystem="{$oidISOCountry}" code="{$country109}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'italy'"><code codeSystem="{$oidISOCountry}" code="{$country110}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'jamaica'"><code codeSystem="{$oidISOCountry}" code="{$country111}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'japan'"><code codeSystem="{$oidISOCountry}" code="{$country112}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'jersey'"><code codeSystem="{$oidISOCountry}" code="{$country113}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'jordan'"><code codeSystem="{$oidISOCountry}" code="{$country114}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'kazakhstan'"><code codeSystem="{$oidISOCountry}" code="{$country115}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'kenya'"><code codeSystem="{$oidISOCountry}" code="{$country116}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'kiribati'"><code codeSystem="{$oidISOCountry}" code="{$country117}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'kuwait'"><code codeSystem="{$oidISOCountry}" code="{$country120}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'kyrgyzstan'"><code codeSystem="{$oidISOCountry}" code="{$country121}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'latvia'"><code codeSystem="{$oidISOCountry}" code="{$country123}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'lebanon'"><code codeSystem="{$oidISOCountry}" code="{$country124}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'lesotho'"><code codeSystem="{$oidISOCountry}" code="{$country125}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'liberia'"><code codeSystem="{$oidISOCountry}" code="{$country126}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'libya'"><code codeSystem="{$oidISOCountry}" code="{$country127}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'liechtenstein'"><code codeSystem="{$oidISOCountry}" code="{$country128}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'lithuania'"><code codeSystem="{$oidISOCountry}" code="{$country129}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'luxembourg'"><code codeSystem="{$oidISOCountry}" code="{$country130}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'macao'"><code codeSystem="{$oidISOCountry}" code="{$country131}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'macedonia (the former yugoslav republic of)'"><code codeSystem="{$oidISOCountry}" code="{$country132}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'madagascar'"><code codeSystem="{$oidISOCountry}" code="{$country133}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'malawi'"><code codeSystem="{$oidISOCountry}" code="{$country134}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'malaysia'"><code codeSystem="{$oidISOCountry}" code="{$country135}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'maldives'"><code codeSystem="{$oidISOCountry}" code="{$country136}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mali'"><code codeSystem="{$oidISOCountry}" code="{$country137}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'malta'"><code codeSystem="{$oidISOCountry}" code="{$country138}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'marshall islands'"><code codeSystem="{$oidISOCountry}" code="{$country139}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'martinique'"><code codeSystem="{$oidISOCountry}" code="{$country140}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mauritania'"><code codeSystem="{$oidISOCountry}" code="{$country141}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mauritius'"><code codeSystem="{$oidISOCountry}" code="{$country142}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mayotte'"><code codeSystem="{$oidISOCountry}" code="{$country143}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mexico'"><code codeSystem="{$oidISOCountry}" code="{$country144}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'micronesia (federated states of)'"><code codeSystem="{$oidISOCountry}" code="{$country145}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'moldova (republic of)'"><code codeSystem="{$oidISOCountry}" code="{$country146}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'monaco'"><code codeSystem="{$oidISOCountry}" code="{$country147}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mongolia'"><code codeSystem="{$oidISOCountry}" code="{$country148}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'montenegro'"><code codeSystem="{$oidISOCountry}" code="{$country149}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'montserrat'"><code codeSystem="{$oidISOCountry}" code="{$country150}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'morocco'"><code codeSystem="{$oidISOCountry}" code="{$country151}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mozambique'"><code codeSystem="{$oidISOCountry}" code="{$country152}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'myanmar'"><code codeSystem="{$oidISOCountry}" code="{$country153}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'namibia'"><code codeSystem="{$oidISOCountry}" code="{$country154}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'nauru'"><code codeSystem="{$oidISOCountry}" code="{$country155}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'nepal'"><code codeSystem="{$oidISOCountry}" code="{$country156}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'netherlands'"><code codeSystem="{$oidISOCountry}" code="{$country157}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'new caledonia'"><code codeSystem="{$oidISOCountry}" code="{$country158}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'new zealand'"><code codeSystem="{$oidISOCountry}" code="{$country159}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'nicaragua'"><code codeSystem="{$oidISOCountry}" code="{$country160}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'niger'"><code codeSystem="{$oidISOCountry}" code="{$country161}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'nigeria'"><code codeSystem="{$oidISOCountry}" code="{$country162}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'niue'"><code codeSystem="{$oidISOCountry}" code="{$country163}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'norfolk island'"><code codeSystem="{$oidISOCountry}" code="{$country164}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'northern mariana islands'"><code codeSystem="{$oidISOCountry}" code="{$country165}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'norway'"><code codeSystem="{$oidISOCountry}" code="{$country166}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'oman'"><code codeSystem="{$oidISOCountry}" code="{$country167}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'pakistan'"><code codeSystem="{$oidISOCountry}" code="{$country168}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'palau'"><code codeSystem="{$oidISOCountry}" code="{$country169}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'palestine, state of'"><code codeSystem="{$oidISOCountry}" code="{$country170}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'panama'"><code codeSystem="{$oidISOCountry}" code="{$country171}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'papua new guinea'"><code codeSystem="{$oidISOCountry}" code="{$country172}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'paraguay'"><code codeSystem="{$oidISOCountry}" code="{$country173}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'peru'"><code codeSystem="{$oidISOCountry}" code="{$country174}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'philippines'"><code codeSystem="{$oidISOCountry}" code="{$country175}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'pitcairn'"><code codeSystem="{$oidISOCountry}" code="{$country176}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'poland'"><code codeSystem="{$oidISOCountry}" code="{$country177}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'portugal'"><code codeSystem="{$oidISOCountry}" code="{$country178}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'puerto rico'"><code codeSystem="{$oidISOCountry}" code="{$country179}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'qatar'"><code codeSystem="{$oidISOCountry}" code="{$country180}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'reunion !réunion'"><code codeSystem="{$oidISOCountry}" code="{$country181}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'romania'"><code codeSystem="{$oidISOCountry}" code="{$country182}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'russian federation'"><code codeSystem="{$oidISOCountry}" code="{$country183}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'rwanda'"><code codeSystem="{$oidISOCountry}" code="{$country184}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'saint barthelemy !saint barthélemy'"><code codeSystem="{$oidISOCountry}" code="{$country185}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'saint helena, ascension and tristan da cunha'"><code codeSystem="{$oidISOCountry}" code="{$country186}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'saint kitts and nevis'"><code codeSystem="{$oidISOCountry}" code="{$country187}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'saint lucia'"><code codeSystem="{$oidISOCountry}" code="{$country188}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'saint martin (french part)'"><code codeSystem="{$oidISOCountry}" code="{$country189}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'saint pierre and miquelon'"><code codeSystem="{$oidISOCountry}" code="{$country190}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'saint vincent and the grenadines'"><code codeSystem="{$oidISOCountry}" code="{$country191}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'samoa'"><code codeSystem="{$oidISOCountry}" code="{$country192}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'san marino'"><code codeSystem="{$oidISOCountry}" code="{$country193}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sao tome and principe'"><code codeSystem="{$oidISOCountry}" code="{$country194}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'saudi arabia'"><code codeSystem="{$oidISOCountry}" code="{$country195}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'senegal'"><code codeSystem="{$oidISOCountry}" code="{$country196}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'serbia'"><code codeSystem="{$oidISOCountry}" code="{$country197}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'seychelles'"><code codeSystem="{$oidISOCountry}" code="{$country198}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sierra leone'"><code codeSystem="{$oidISOCountry}" code="{$country199}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'singapore'"><code codeSystem="{$oidISOCountry}" code="{$country200}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sint maarten (dutch part)'"><code codeSystem="{$oidISOCountry}" code="{$country201}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'slovakia'"><code codeSystem="{$oidISOCountry}" code="{$country202}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'slovenia'"><code codeSystem="{$oidISOCountry}" code="{$country203}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'solomon islands'"><code codeSystem="{$oidISOCountry}" code="{$country204}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'somalia'"><code codeSystem="{$oidISOCountry}" code="{$country205}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'south africa'"><code codeSystem="{$oidISOCountry}" code="{$country206}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'south georgia and the south sandwich islands'"><code codeSystem="{$oidISOCountry}" code="{$country207}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'south sudan'"><code codeSystem="{$oidISOCountry}" code="{$country208}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'spain'"><code codeSystem="{$oidISOCountry}" code="{$country209}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sri lanka'"><code codeSystem="{$oidISOCountry}" code="{$country210}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sudan'"><code codeSystem="{$oidISOCountry}" code="{$country211}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'suriname'"><code codeSystem="{$oidISOCountry}" code="{$country212}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'svalbard and jan mayen'"><code codeSystem="{$oidISOCountry}" code="{$country213}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'swaziland'"><code codeSystem="{$oidISOCountry}" code="{$country214}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sweden'"><code codeSystem="{$oidISOCountry}" code="{$country215}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'switzerland'"><code codeSystem="{$oidISOCountry}" code="{$country216}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'syrian arab republic'"><code codeSystem="{$oidISOCountry}" code="{$country217}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'taiwan, province of china[a]'"><code codeSystem="{$oidISOCountry}" code="{$country218}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tajikistan'"><code codeSystem="{$oidISOCountry}" code="{$country219}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tanzania, united republic of'"><code codeSystem="{$oidISOCountry}" code="{$country220}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'thailand'"><code codeSystem="{$oidISOCountry}" code="{$country221}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'timor-leste'"><code codeSystem="{$oidISOCountry}" code="{$country222}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'togo'"><code codeSystem="{$oidISOCountry}" code="{$country223}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tokelau'"><code codeSystem="{$oidISOCountry}" code="{$country224}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tonga'"><code codeSystem="{$oidISOCountry}" code="{$country225}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'trinidad and tobago'"><code codeSystem="{$oidISOCountry}" code="{$country226}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tunisia'"><code codeSystem="{$oidISOCountry}" code="{$country227}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'turkey'"><code codeSystem="{$oidISOCountry}" code="{$country228}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'turkmenistan'"><code codeSystem="{$oidISOCountry}" code="{$country229}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'turks and caicos islands'"><code codeSystem="{$oidISOCountry}" code="{$country230}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tuvalu'"><code codeSystem="{$oidISOCountry}" code="{$country231}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'uganda'"><code codeSystem="{$oidISOCountry}" code="{$country232}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ukraine'"><code codeSystem="{$oidISOCountry}" code="{$country233}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'united arab emirates'"><code codeSystem="{$oidISOCountry}" code="{$country234}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'united kingdom of great britain and northern ireland'"><code codeSystem="{$oidISOCountry}" code="{$country235}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'united kingdom'"><code codeSystem="{$oidISOCountry}" code="{$country235}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'united states of america'"><code codeSystem="{$oidISOCountry}" code="{$country236}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'united states'"><code codeSystem="{$oidISOCountry}" code="{$country236}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'united states minor outlying islands'"><code codeSystem="{$oidISOCountry}" code="{$country237}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'uruguay'"><code codeSystem="{$oidISOCountry}" code="{$country238}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'uzbekistan'"><code codeSystem="{$oidISOCountry}" code="{$country239}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'vanuatu'"><code codeSystem="{$oidISOCountry}" code="{$country240}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'venezuela (bolivarian republic of)'"><code codeSystem="{$oidISOCountry}" code="{$country241}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'viet nam'"><code codeSystem="{$oidISOCountry}" code="{$country242}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'virgin islands (british)'"><code codeSystem="{$oidISOCountry}" code="{$country243}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'virgin islands (u.s.)'"><code codeSystem="{$oidISOCountry}" code="{$country244}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'wallis and futuna'"><code codeSystem="{$oidISOCountry}" code="{$country245}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'western sahara'"><code codeSystem="{$oidISOCountry}" code="{$country246}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'yemen'"><code codeSystem="{$oidISOCountry}" code="{$country247}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'zambia'"><code codeSystem="{$oidISOCountry}" code="{$country248}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'zimbabwe'"><code codeSystem="{$oidISOCountry}" code="{$country249}" displayName="sourceReport"/> </xsl:when>

<xsl:when test="reportercountry = 'AFGHANISTAN'"><code codeSystem="{$oidISOCountry}" code="{$country1}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ALAND ISLANDS !ÅLAND ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country2}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ALBANIA'"><code codeSystem="{$oidISOCountry}" code="{$country3}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ALGERIA'"><code codeSystem="{$oidISOCountry}" code="{$country4}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'AMERICAN SAMOA'"><code codeSystem="{$oidISOCountry}" code="{$country5}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ANDORRA'"><code codeSystem="{$oidISOCountry}" code="{$country6}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ANGOLA'"><code codeSystem="{$oidISOCountry}" code="{$country7}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ANGUILLA'"><code codeSystem="{$oidISOCountry}" code="{$country8}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ANTARCTICA'"><code codeSystem="{$oidISOCountry}" code="{$country9}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ANTIGUA AND BARBUDA'"><code codeSystem="{$oidISOCountry}" code="{$country10}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ARGENTINA'"><code codeSystem="{$oidISOCountry}" code="{$country11}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ARMENIA'"><code codeSystem="{$oidISOCountry}" code="{$country12}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ARUBA'"><code codeSystem="{$oidISOCountry}" code="{$country13}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'AUSTRALIA'"><code codeSystem="{$oidISOCountry}" code="{$country14}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'AUSTRIA'"><code codeSystem="{$oidISOCountry}" code="{$country15}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'AZERBAIJAN'"><code codeSystem="{$oidISOCountry}" code="{$country16}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BAHAMAS'"><code codeSystem="{$oidISOCountry}" code="{$country17}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BAHRAIN'"><code codeSystem="{$oidISOCountry}" code="{$country18}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BANGLADESH'"><code codeSystem="{$oidISOCountry}" code="{$country19}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BARBADOS'"><code codeSystem="{$oidISOCountry}" code="{$country20}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BELARUS'"><code codeSystem="{$oidISOCountry}" code="{$country21}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BELGIUM'"><code codeSystem="{$oidISOCountry}" code="{$country22}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BELIZE'"><code codeSystem="{$oidISOCountry}" code="{$country23}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BENIN'"><code codeSystem="{$oidISOCountry}" code="{$country24}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BERMUDA'"><code codeSystem="{$oidISOCountry}" code="{$country25}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BHUTAN'"><code codeSystem="{$oidISOCountry}" code="{$country26}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BOLIVIA (PLURINATIONAL STATE OF)'"><code codeSystem="{$oidISOCountry}" code="{$country27}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BONAIRE, SINT EUSTATIUS AND SABA'"><code codeSystem="{$oidISOCountry}" code="{$country28}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BOSNIA AND HERZEGOVINA'"><code codeSystem="{$oidISOCountry}" code="{$country29}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BOTSWANA'"><code codeSystem="{$oidISOCountry}" code="{$country30}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BOUVET ISLAND'"><code codeSystem="{$oidISOCountry}" code="{$country31}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BRAZIL'"><code codeSystem="{$oidISOCountry}" code="{$country32}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BRITISH INDIAN OCEAN TERRITORY'"><code codeSystem="{$oidISOCountry}" code="{$country33}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BRUNEI DARUSSALAM'"><code codeSystem="{$oidISOCountry}" code="{$country34}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BULGARIA'"><code codeSystem="{$oidISOCountry}" code="{$country35}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BURKINA FASO'"><code codeSystem="{$oidISOCountry}" code="{$country36}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'BURUNDI'"><code codeSystem="{$oidISOCountry}" code="{$country37}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'CABO VERDE'"><code codeSystem="{$oidISOCountry}" code="{$country38}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'CAMBODIA'"><code codeSystem="{$oidISOCountry}" code="{$country39}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'CAMEROON'"><code codeSystem="{$oidISOCountry}" code="{$country40}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'CANADA'"><code codeSystem="{$oidISOCountry}" code="{$country41}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'CAYMAN ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country42}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'CENTRAL AFRICAN REPUBLIC'"><code codeSystem="{$oidISOCountry}" code="{$country43}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'CHAD'"><code codeSystem="{$oidISOCountry}" code="{$country44}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'CHILE'"><code codeSystem="{$oidISOCountry}" code="{$country45}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'CHINA'"><code codeSystem="{$oidISOCountry}" code="{$country46}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'CHRISTMAS ISLAND'"><code codeSystem="{$oidISOCountry}" code="{$country47}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'COCOS (KEELING) ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country48}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'COLOMBIA'"><code codeSystem="{$oidISOCountry}" code="{$country49}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'COMOROS'"><code codeSystem="{$oidISOCountry}" code="{$country50}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'CONGO'"><code codeSystem="{$oidISOCountry}" code="{$country51}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'CONGO (DEMOCRATIC REPUBLIC OF THE)'"><code codeSystem="{$oidISOCountry}" code="{$country52}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'COOK ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country53}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'COSTA RICA'"><code codeSystem="{$oidISOCountry}" code="{$country54}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'EUROPEAN UNION'"><code codeSystem="{$oidISOCountry}" code="{$country250}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'CROATIA'"><code codeSystem="{$oidISOCountry}" code="{$country56}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'CUBA'"><code codeSystem="{$oidISOCountry}" code="{$country57}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'CURACAO !CURAÇAO'"><code codeSystem="{$oidISOCountry}" code="{$country58}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'CYPRUS'"><code codeSystem="{$oidISOCountry}" code="{$country59}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'CZECH REPUBLIC'"><code codeSystem="{$oidISOCountry}" code="{$country60}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'DENMARK'"><code codeSystem="{$oidISOCountry}" code="{$country61}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'DJIBOUTI'"><code codeSystem="{$oidISOCountry}" code="{$country62}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'DOMINICA'"><code codeSystem="{$oidISOCountry}" code="{$country63}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'DOMINICAN REPUBLIC'"><code codeSystem="{$oidISOCountry}" code="{$country64}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ECUADOR'"><code codeSystem="{$oidISOCountry}" code="{$country65}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'EGYPT'"><code codeSystem="{$oidISOCountry}" code="{$country66}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'EL SALVADOR'"><code codeSystem="{$oidISOCountry}" code="{$country67}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'EQUATORIAL GUINEA'"><code codeSystem="{$oidISOCountry}" code="{$country68}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ERITREA'"><code codeSystem="{$oidISOCountry}" code="{$country69}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ESTONIA'"><code codeSystem="{$oidISOCountry}" code="{$country70}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ETHIOPIA'"><code codeSystem="{$oidISOCountry}" code="{$country71}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'FALKLAND ISLANDS (MALVINAS)'"><code codeSystem="{$oidISOCountry}" code="{$country72}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'FAROE ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country73}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'FIJI'"><code codeSystem="{$oidISOCountry}" code="{$country74}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'FINLAND'"><code codeSystem="{$oidISOCountry}" code="{$country75}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'FRANCE'"><code codeSystem="{$oidISOCountry}" code="{$country76}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'FRENCH GUIANA'"><code codeSystem="{$oidISOCountry}" code="{$country77}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'FRENCH POLYNESIA'"><code codeSystem="{$oidISOCountry}" code="{$country78}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'FRENCH SOUTHERN TERRITORIES'"><code codeSystem="{$oidISOCountry}" code="{$country79}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'GABON'"><code codeSystem="{$oidISOCountry}" code="{$country80}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'GAMBIA'"><code codeSystem="{$oidISOCountry}" code="{$country81}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'GEORGIA'"><code codeSystem="{$oidISOCountry}" code="{$country82}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'GERMANY'"><code codeSystem="{$oidISOCountry}" code="{$country83}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'GHANA'"><code codeSystem="{$oidISOCountry}" code="{$country84}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'GIBRALTAR'"><code codeSystem="{$oidISOCountry}" code="{$country85}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'GREECE'"><code codeSystem="{$oidISOCountry}" code="{$country86}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'GREENLAND'"><code codeSystem="{$oidISOCountry}" code="{$country87}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'GRENADA'"><code codeSystem="{$oidISOCountry}" code="{$country88}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'GUADELOUPE'"><code codeSystem="{$oidISOCountry}" code="{$country89}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'GUAM'"><code codeSystem="{$oidISOCountry}" code="{$country90}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'GUATEMALA'"><code codeSystem="{$oidISOCountry}" code="{$country91}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'GUERNSEY'"><code codeSystem="{$oidISOCountry}" code="{$country92}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'GUINEA'"><code codeSystem="{$oidISOCountry}" code="{$country93}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'GUINEA-BISSAU'"><code codeSystem="{$oidISOCountry}" code="{$country94}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'GUYANA'"><code codeSystem="{$oidISOCountry}" code="{$country95}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'HAITI'"><code codeSystem="{$oidISOCountry}" code="{$country96}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'HEARD ISLAND AND MCDONALD ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country97}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'HOLY SEE'"><code codeSystem="{$oidISOCountry}" code="{$country98}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'HONDURAS'"><code codeSystem="{$oidISOCountry}" code="{$country99}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'HONG KONG'"><code codeSystem="{$oidISOCountry}" code="{$country100}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'HUNGARY'"><code codeSystem="{$oidISOCountry}" code="{$country101}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ICELAND'"><code codeSystem="{$oidISOCountry}" code="{$country102}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'INDIA'"><code codeSystem="{$oidISOCountry}" code="{$country103}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'INDONESIA'"><code codeSystem="{$oidISOCountry}" code="{$country104}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'IRAN (ISLAMIC REPUBLIC OF)'"><code codeSystem="{$oidISOCountry}" code="{$country105}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'IRAQ'"><code codeSystem="{$oidISOCountry}" code="{$country106}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'IRELAND'"><code codeSystem="{$oidISOCountry}" code="{$country107}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ISLE OF MAN'"><code codeSystem="{$oidISOCountry}" code="{$country108}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ISRAEL'"><code codeSystem="{$oidISOCountry}" code="{$country109}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ITALY'"><code codeSystem="{$oidISOCountry}" code="{$country110}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'JAMAICA'"><code codeSystem="{$oidISOCountry}" code="{$country111}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'JAPAN'"><code codeSystem="{$oidISOCountry}" code="{$country112}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'JERSEY'"><code codeSystem="{$oidISOCountry}" code="{$country113}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'JORDAN'"><code codeSystem="{$oidISOCountry}" code="{$country114}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'KAZAKHSTAN'"><code codeSystem="{$oidISOCountry}" code="{$country115}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'KENYA'"><code codeSystem="{$oidISOCountry}" code="{$country116}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'KIRIBATI'"><code codeSystem="{$oidISOCountry}" code="{$country117}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'KUWAIT'"><code codeSystem="{$oidISOCountry}" code="{$country120}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'KYRGYZSTAN'"><code codeSystem="{$oidISOCountry}" code="{$country121}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'LATVIA'"><code codeSystem="{$oidISOCountry}" code="{$country123}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'LEBANON'"><code codeSystem="{$oidISOCountry}" code="{$country124}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'LESOTHO'"><code codeSystem="{$oidISOCountry}" code="{$country125}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'LIBERIA'"><code codeSystem="{$oidISOCountry}" code="{$country126}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'LIBYA'"><code codeSystem="{$oidISOCountry}" code="{$country127}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'LIECHTENSTEIN'"><code codeSystem="{$oidISOCountry}" code="{$country128}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'LITHUANIA'"><code codeSystem="{$oidISOCountry}" code="{$country129}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'LUXEMBOURG'"><code codeSystem="{$oidISOCountry}" code="{$country130}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MACAO'"><code codeSystem="{$oidISOCountry}" code="{$country131}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MACEDONIA (THE FORMER YUGOSLAV REPUBLIC OF)'"><code codeSystem="{$oidISOCountry}" code="{$country132}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MADAGASCAR'"><code codeSystem="{$oidISOCountry}" code="{$country133}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MALAWI'"><code codeSystem="{$oidISOCountry}" code="{$country134}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MALAYSIA'"><code codeSystem="{$oidISOCountry}" code="{$country135}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MALDIVES'"><code codeSystem="{$oidISOCountry}" code="{$country136}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MALI'"><code codeSystem="{$oidISOCountry}" code="{$country137}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MALTA'"><code codeSystem="{$oidISOCountry}" code="{$country138}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MARSHALL ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country139}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MARTINIQUE'"><code codeSystem="{$oidISOCountry}" code="{$country140}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MAURITANIA'"><code codeSystem="{$oidISOCountry}" code="{$country141}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MAURITIUS'"><code codeSystem="{$oidISOCountry}" code="{$country142}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MAYOTTE'"><code codeSystem="{$oidISOCountry}" code="{$country143}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MEXICO'"><code codeSystem="{$oidISOCountry}" code="{$country144}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MICRONESIA (FEDERATED STATES OF)'"><code codeSystem="{$oidISOCountry}" code="{$country145}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MOLDOVA (REPUBLIC OF)'"><code codeSystem="{$oidISOCountry}" code="{$country146}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MONACO'"><code codeSystem="{$oidISOCountry}" code="{$country147}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MONGOLIA'"><code codeSystem="{$oidISOCountry}" code="{$country148}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MONTENEGRO'"><code codeSystem="{$oidISOCountry}" code="{$country149}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MONTSERRAT'"><code codeSystem="{$oidISOCountry}" code="{$country150}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MOROCCO'"><code codeSystem="{$oidISOCountry}" code="{$country151}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MOZAMBIQUE'"><code codeSystem="{$oidISOCountry}" code="{$country152}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'MYANMAR'"><code codeSystem="{$oidISOCountry}" code="{$country153}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'NAMIBIA'"><code codeSystem="{$oidISOCountry}" code="{$country154}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'NAURU'"><code codeSystem="{$oidISOCountry}" code="{$country155}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'NEPAL'"><code codeSystem="{$oidISOCountry}" code="{$country156}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'NETHERLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country157}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'NEW CALEDONIA'"><code codeSystem="{$oidISOCountry}" code="{$country158}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'NEW ZEALAND'"><code codeSystem="{$oidISOCountry}" code="{$country159}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'NICARAGUA'"><code codeSystem="{$oidISOCountry}" code="{$country160}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'NIGER'"><code codeSystem="{$oidISOCountry}" code="{$country161}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'NIGERIA'"><code codeSystem="{$oidISOCountry}" code="{$country162}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'NIUE'"><code codeSystem="{$oidISOCountry}" code="{$country163}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'NORFOLK ISLAND'"><code codeSystem="{$oidISOCountry}" code="{$country164}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'NORTHERN MARIANA ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country165}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'NORWAY'"><code codeSystem="{$oidISOCountry}" code="{$country166}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'OMAN'"><code codeSystem="{$oidISOCountry}" code="{$country167}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'PAKISTAN'"><code codeSystem="{$oidISOCountry}" code="{$country168}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'PALAU'"><code codeSystem="{$oidISOCountry}" code="{$country169}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'PALESTINE, STATE OF'"><code codeSystem="{$oidISOCountry}" code="{$country170}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'PANAMA'"><code codeSystem="{$oidISOCountry}" code="{$country171}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'PAPUA NEW GUINEA'"><code codeSystem="{$oidISOCountry}" code="{$country172}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'PARAGUAY'"><code codeSystem="{$oidISOCountry}" code="{$country173}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'PERU'"><code codeSystem="{$oidISOCountry}" code="{$country174}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'PHILIPPINES'"><code codeSystem="{$oidISOCountry}" code="{$country175}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'PITCAIRN'"><code codeSystem="{$oidISOCountry}" code="{$country176}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'POLAND'"><code codeSystem="{$oidISOCountry}" code="{$country177}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'PORTUGAL'"><code codeSystem="{$oidISOCountry}" code="{$country178}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'PUERTO RICO'"><code codeSystem="{$oidISOCountry}" code="{$country179}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'QATAR'"><code codeSystem="{$oidISOCountry}" code="{$country180}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'REUNION !RÉUNION'"><code codeSystem="{$oidISOCountry}" code="{$country181}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ROMANIA'"><code codeSystem="{$oidISOCountry}" code="{$country182}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'RUSSIAN FEDERATION'"><code codeSystem="{$oidISOCountry}" code="{$country183}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'RWANDA'"><code codeSystem="{$oidISOCountry}" code="{$country184}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SAINT BARTHELEMY !SAINT BARTHÉLEMY'"><code codeSystem="{$oidISOCountry}" code="{$country185}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SAINT HELENA, ASCENSION AND TRISTAN DA CUNHA'"><code codeSystem="{$oidISOCountry}" code="{$country186}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SAINT KITTS AND NEVIS'"><code codeSystem="{$oidISOCountry}" code="{$country187}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SAINT LUCIA'"><code codeSystem="{$oidISOCountry}" code="{$country188}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SAINT MARTIN (FRENCH PART)'"><code codeSystem="{$oidISOCountry}" code="{$country189}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SAINT PIERRE AND MIQUELON'"><code codeSystem="{$oidISOCountry}" code="{$country190}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SAINT VINCENT AND THE GRENADINES'"><code codeSystem="{$oidISOCountry}" code="{$country191}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SAMOA'"><code codeSystem="{$oidISOCountry}" code="{$country192}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SAN MARINO'"><code codeSystem="{$oidISOCountry}" code="{$country193}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SAO TOME AND PRINCIPE'"><code codeSystem="{$oidISOCountry}" code="{$country194}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SAUDI ARABIA'"><code codeSystem="{$oidISOCountry}" code="{$country195}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SENEGAL'"><code codeSystem="{$oidISOCountry}" code="{$country196}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SERBIA'"><code codeSystem="{$oidISOCountry}" code="{$country197}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SEYCHELLES'"><code codeSystem="{$oidISOCountry}" code="{$country198}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SIERRA LEONE'"><code codeSystem="{$oidISOCountry}" code="{$country199}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SINGAPORE'"><code codeSystem="{$oidISOCountry}" code="{$country200}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SINT MAARTEN (DUTCH PART)'"><code codeSystem="{$oidISOCountry}" code="{$country201}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SLOVAKIA'"><code codeSystem="{$oidISOCountry}" code="{$country202}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SLOVENIA'"><code codeSystem="{$oidISOCountry}" code="{$country203}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SOLOMON ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country204}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SOMALIA'"><code codeSystem="{$oidISOCountry}" code="{$country205}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SOUTH AFRICA'"><code codeSystem="{$oidISOCountry}" code="{$country206}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country207}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SOUTH SUDAN'"><code codeSystem="{$oidISOCountry}" code="{$country208}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SPAIN'"><code codeSystem="{$oidISOCountry}" code="{$country209}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SRI LANKA'"><code codeSystem="{$oidISOCountry}" code="{$country210}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SUDAN'"><code codeSystem="{$oidISOCountry}" code="{$country211}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SURINAME'"><code codeSystem="{$oidISOCountry}" code="{$country212}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SVALBARD AND JAN MAYEN'"><code codeSystem="{$oidISOCountry}" code="{$country213}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SWAZILAND'"><code codeSystem="{$oidISOCountry}" code="{$country214}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SWEDEN'"><code codeSystem="{$oidISOCountry}" code="{$country215}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SWITZERLAND'"><code codeSystem="{$oidISOCountry}" code="{$country216}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'SYRIAN ARAB REPUBLIC'"><code codeSystem="{$oidISOCountry}" code="{$country217}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'TAIWAN, PROVINCE OF CHINA[A]'"><code codeSystem="{$oidISOCountry}" code="{$country218}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'TAJIKISTAN'"><code codeSystem="{$oidISOCountry}" code="{$country219}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'TANZANIA, UNITED REPUBLIC OF'"><code codeSystem="{$oidISOCountry}" code="{$country220}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'THAILAND'"><code codeSystem="{$oidISOCountry}" code="{$country221}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'TIMOR-LESTE'"><code codeSystem="{$oidISOCountry}" code="{$country222}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'TOGO'"><code codeSystem="{$oidISOCountry}" code="{$country223}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'TOKELAU'"><code codeSystem="{$oidISOCountry}" code="{$country224}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'TONGA'"><code codeSystem="{$oidISOCountry}" code="{$country225}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'TRINIDAD AND TOBAGO'"><code codeSystem="{$oidISOCountry}" code="{$country226}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'TUNISIA'"><code codeSystem="{$oidISOCountry}" code="{$country227}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'TURKEY'"><code codeSystem="{$oidISOCountry}" code="{$country228}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'TURKMENISTAN'"><code codeSystem="{$oidISOCountry}" code="{$country229}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'TURKS AND CAICOS ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country230}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'TUVALU'"><code codeSystem="{$oidISOCountry}" code="{$country231}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'UGANDA'"><code codeSystem="{$oidISOCountry}" code="{$country232}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'UKRAINE'"><code codeSystem="{$oidISOCountry}" code="{$country233}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'UNITED ARAB EMIRATES'"><code codeSystem="{$oidISOCountry}" code="{$country234}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'UNITED KINGDOM OF GREAT BRITAIN AND NORTHERN IRELAND'"><code codeSystem="{$oidISOCountry}" code="{$country235}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'UNITED KINGDOM'"><code codeSystem="{$oidISOCountry}" code="{$country235}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'UNITED STATES OF AMERICA'"><code codeSystem="{$oidISOCountry}" code="{$country236}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'UNITED STATES'"><code codeSystem="{$oidISOCountry}" code="{$country236}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'UNITED STATES MINOR OUTLYING ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country237}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'URUGUAY'"><code codeSystem="{$oidISOCountry}" code="{$country238}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'UZBEKISTAN'"><code codeSystem="{$oidISOCountry}" code="{$country239}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'VANUATU'"><code codeSystem="{$oidISOCountry}" code="{$country240}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'VENEZUELA (BOLIVARIAN REPUBLIC OF)'"><code codeSystem="{$oidISOCountry}" code="{$country241}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'VIET NAM'"><code codeSystem="{$oidISOCountry}" code="{$country242}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'VIRGIN ISLANDS (BRITISH)'"><code codeSystem="{$oidISOCountry}" code="{$country243}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'VIRGIN ISLANDS (U.S.)'"><code codeSystem="{$oidISOCountry}" code="{$country244}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'WALLIS AND FUTUNA'"><code codeSystem="{$oidISOCountry}" code="{$country245}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'WESTERN SAHARA'"><code codeSystem="{$oidISOCountry}" code="{$country246}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'YEMEN'"><code codeSystem="{$oidISOCountry}" code="{$country247}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ZAMBIA'"><code codeSystem="{$oidISOCountry}" code="{$country248}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ZIMBABWE'"><code codeSystem="{$oidISOCountry}" code="{$country249}" displayName="sourceReport"/> </xsl:when>

<xsl:when test="reportercountry = 'af'"><code codeSystem="{$oidISOCountry}" code="{$country1}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ax'"><code codeSystem="{$oidISOCountry}" code="{$country2}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'al'"><code codeSystem="{$oidISOCountry}" code="{$country3}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'dz'"><code codeSystem="{$oidISOCountry}" code="{$country4}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'as'"><code codeSystem="{$oidISOCountry}" code="{$country5}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ad'"><code codeSystem="{$oidISOCountry}" code="{$country6}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ao'"><code codeSystem="{$oidISOCountry}" code="{$country7}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ai'"><code codeSystem="{$oidISOCountry}" code="{$country8}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'aq'"><code codeSystem="{$oidISOCountry}" code="{$country9}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ag'"><code codeSystem="{$oidISOCountry}" code="{$country10}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ar'"><code codeSystem="{$oidISOCountry}" code="{$country11}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'am'"><code codeSystem="{$oidISOCountry}" code="{$country12}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'aw'"><code codeSystem="{$oidISOCountry}" code="{$country13}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'au'"><code codeSystem="{$oidISOCountry}" code="{$country14}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'at'"><code codeSystem="{$oidISOCountry}" code="{$country15}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'az'"><code codeSystem="{$oidISOCountry}" code="{$country16}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bs'"><code codeSystem="{$oidISOCountry}" code="{$country17}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bh'"><code codeSystem="{$oidISOCountry}" code="{$country18}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bd'"><code codeSystem="{$oidISOCountry}" code="{$country19}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bb'"><code codeSystem="{$oidISOCountry}" code="{$country20}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'by'"><code codeSystem="{$oidISOCountry}" code="{$country21}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'be'"><code codeSystem="{$oidISOCountry}" code="{$country22}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bz'"><code codeSystem="{$oidISOCountry}" code="{$country23}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bj'"><code codeSystem="{$oidISOCountry}" code="{$country24}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bm'"><code codeSystem="{$oidISOCountry}" code="{$country25}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bt'"><code codeSystem="{$oidISOCountry}" code="{$country26}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bo'"><code codeSystem="{$oidISOCountry}" code="{$country27}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bq'"><code codeSystem="{$oidISOCountry}" code="{$country28}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ba'"><code codeSystem="{$oidISOCountry}" code="{$country29}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bw'"><code codeSystem="{$oidISOCountry}" code="{$country30}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bv'"><code codeSystem="{$oidISOCountry}" code="{$country31}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'br'"><code codeSystem="{$oidISOCountry}" code="{$country32}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'io'"><code codeSystem="{$oidISOCountry}" code="{$country33}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bn'"><code codeSystem="{$oidISOCountry}" code="{$country34}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bg'"><code codeSystem="{$oidISOCountry}" code="{$country35}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bf'"><code codeSystem="{$oidISOCountry}" code="{$country36}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bi'"><code codeSystem="{$oidISOCountry}" code="{$country37}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cv'"><code codeSystem="{$oidISOCountry}" code="{$country38}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'kh'"><code codeSystem="{$oidISOCountry}" code="{$country39}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cm'"><code codeSystem="{$oidISOCountry}" code="{$country40}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ca'"><code codeSystem="{$oidISOCountry}" code="{$country41}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ky'"><code codeSystem="{$oidISOCountry}" code="{$country42}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cf'"><code codeSystem="{$oidISOCountry}" code="{$country43}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'td'"><code codeSystem="{$oidISOCountry}" code="{$country44}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cl'"><code codeSystem="{$oidISOCountry}" code="{$country45}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cn'"><code codeSystem="{$oidISOCountry}" code="{$country46}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cx'"><code codeSystem="{$oidISOCountry}" code="{$country47}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cc'"><code codeSystem="{$oidISOCountry}" code="{$country48}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'co'"><code codeSystem="{$oidISOCountry}" code="{$country49}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'km'"><code codeSystem="{$oidISOCountry}" code="{$country50}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cg'"><code codeSystem="{$oidISOCountry}" code="{$country51}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cd'"><code codeSystem="{$oidISOCountry}" code="{$country52}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ck'"><code codeSystem="{$oidISOCountry}" code="{$country53}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cr'"><code codeSystem="{$oidISOCountry}" code="{$country54}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ci'"><code codeSystem="{$oidISOCountry}" code="{$country55}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'hr'"><code codeSystem="{$oidISOCountry}" code="{$country56}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cu'"><code codeSystem="{$oidISOCountry}" code="{$country57}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cw'"><code codeSystem="{$oidISOCountry}" code="{$country58}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cy'"><code codeSystem="{$oidISOCountry}" code="{$country59}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'cz'"><code codeSystem="{$oidISOCountry}" code="{$country60}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'dk'"><code codeSystem="{$oidISOCountry}" code="{$country61}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'dj'"><code codeSystem="{$oidISOCountry}" code="{$country62}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'dm'"><code codeSystem="{$oidISOCountry}" code="{$country63}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'do'"><code codeSystem="{$oidISOCountry}" code="{$country64}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ec'"><code codeSystem="{$oidISOCountry}" code="{$country65}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'eg'"><code codeSystem="{$oidISOCountry}" code="{$country66}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sv'"><code codeSystem="{$oidISOCountry}" code="{$country67}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gq'"><code codeSystem="{$oidISOCountry}" code="{$country68}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'er'"><code codeSystem="{$oidISOCountry}" code="{$country69}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ee'"><code codeSystem="{$oidISOCountry}" code="{$country70}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'et'"><code codeSystem="{$oidISOCountry}" code="{$country71}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'fk'"><code codeSystem="{$oidISOCountry}" code="{$country72}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'fo'"><code codeSystem="{$oidISOCountry}" code="{$country73}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'fj'"><code codeSystem="{$oidISOCountry}" code="{$country74}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'fi'"><code codeSystem="{$oidISOCountry}" code="{$country75}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'fr'"><code codeSystem="{$oidISOCountry}" code="{$country76}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gf'"><code codeSystem="{$oidISOCountry}" code="{$country77}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'pf'"><code codeSystem="{$oidISOCountry}" code="{$country78}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tf'"><code codeSystem="{$oidISOCountry}" code="{$country79}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ga'"><code codeSystem="{$oidISOCountry}" code="{$country80}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gm'"><code codeSystem="{$oidISOCountry}" code="{$country81}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ge'"><code codeSystem="{$oidISOCountry}" code="{$country82}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'de'"><code codeSystem="{$oidISOCountry}" code="{$country83}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gh'"><code codeSystem="{$oidISOCountry}" code="{$country84}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gi'"><code codeSystem="{$oidISOCountry}" code="{$country85}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gr'"><code codeSystem="{$oidISOCountry}" code="{$country86}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gl'"><code codeSystem="{$oidISOCountry}" code="{$country87}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gd'"><code codeSystem="{$oidISOCountry}" code="{$country88}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gp'"><code codeSystem="{$oidISOCountry}" code="{$country89}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gu'"><code codeSystem="{$oidISOCountry}" code="{$country90}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gt'"><code codeSystem="{$oidISOCountry}" code="{$country91}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gg'"><code codeSystem="{$oidISOCountry}" code="{$country92}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gn'"><code codeSystem="{$oidISOCountry}" code="{$country93}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gw'"><code codeSystem="{$oidISOCountry}" code="{$country94}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gy'"><code codeSystem="{$oidISOCountry}" code="{$country95}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ht'"><code codeSystem="{$oidISOCountry}" code="{$country96}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'hm'"><code codeSystem="{$oidISOCountry}" code="{$country97}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'va'"><code codeSystem="{$oidISOCountry}" code="{$country98}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'hn'"><code codeSystem="{$oidISOCountry}" code="{$country99}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'hk'"><code codeSystem="{$oidISOCountry}" code="{$country100}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'hu'"><code codeSystem="{$oidISOCountry}" code="{$country101}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'is'"><code codeSystem="{$oidISOCountry}" code="{$country102}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'in'"><code codeSystem="{$oidISOCountry}" code="{$country103}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'id'"><code codeSystem="{$oidISOCountry}" code="{$country104}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ir'"><code codeSystem="{$oidISOCountry}" code="{$country105}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'iq'"><code codeSystem="{$oidISOCountry}" code="{$country106}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ie'"><code codeSystem="{$oidISOCountry}" code="{$country107}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'im'"><code codeSystem="{$oidISOCountry}" code="{$country108}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'il'"><code codeSystem="{$oidISOCountry}" code="{$country109}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'it'"><code codeSystem="{$oidISOCountry}" code="{$country110}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'jm'"><code codeSystem="{$oidISOCountry}" code="{$country111}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'jp'"><code codeSystem="{$oidISOCountry}" code="{$country112}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'je'"><code codeSystem="{$oidISOCountry}" code="{$country113}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'jo'"><code codeSystem="{$oidISOCountry}" code="{$country114}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'kz'"><code codeSystem="{$oidISOCountry}" code="{$country115}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ke'"><code codeSystem="{$oidISOCountry}" code="{$country116}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ki'"><code codeSystem="{$oidISOCountry}" code="{$country117}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'kp'"><code codeSystem="{$oidISOCountry}" code="{$country118}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'kr'"><code codeSystem="{$oidISOCountry}" code="{$country119}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'kw'"><code codeSystem="{$oidISOCountry}" code="{$country120}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'kg'"><code codeSystem="{$oidISOCountry}" code="{$country121}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'la'"><code codeSystem="{$oidISOCountry}" code="{$country122}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'lv'"><code codeSystem="{$oidISOCountry}" code="{$country123}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'lb'"><code codeSystem="{$oidISOCountry}" code="{$country124}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ls'"><code codeSystem="{$oidISOCountry}" code="{$country125}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'lr'"><code codeSystem="{$oidISOCountry}" code="{$country126}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ly'"><code codeSystem="{$oidISOCountry}" code="{$country127}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'li'"><code codeSystem="{$oidISOCountry}" code="{$country128}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'lt'"><code codeSystem="{$oidISOCountry}" code="{$country129}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'lu'"><code codeSystem="{$oidISOCountry}" code="{$country130}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mo'"><code codeSystem="{$oidISOCountry}" code="{$country131}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mk'"><code codeSystem="{$oidISOCountry}" code="{$country132}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mg'"><code codeSystem="{$oidISOCountry}" code="{$country133}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mw'"><code codeSystem="{$oidISOCountry}" code="{$country134}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'my'"><code codeSystem="{$oidISOCountry}" code="{$country135}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mv'"><code codeSystem="{$oidISOCountry}" code="{$country136}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ml'"><code codeSystem="{$oidISOCountry}" code="{$country137}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mt'"><code codeSystem="{$oidISOCountry}" code="{$country138}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mh'"><code codeSystem="{$oidISOCountry}" code="{$country139}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mq'"><code codeSystem="{$oidISOCountry}" code="{$country140}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mr'"><code codeSystem="{$oidISOCountry}" code="{$country141}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mu'"><code codeSystem="{$oidISOCountry}" code="{$country142}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'yt'"><code codeSystem="{$oidISOCountry}" code="{$country143}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mx'"><code codeSystem="{$oidISOCountry}" code="{$country144}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'fm'"><code codeSystem="{$oidISOCountry}" code="{$country145}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'md'"><code codeSystem="{$oidISOCountry}" code="{$country146}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mc'"><code codeSystem="{$oidISOCountry}" code="{$country147}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mn'"><code codeSystem="{$oidISOCountry}" code="{$country148}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'me'"><code codeSystem="{$oidISOCountry}" code="{$country149}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ms'"><code codeSystem="{$oidISOCountry}" code="{$country150}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ma'"><code codeSystem="{$oidISOCountry}" code="{$country151}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mz'"><code codeSystem="{$oidISOCountry}" code="{$country152}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mm'"><code codeSystem="{$oidISOCountry}" code="{$country153}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'na'"><code codeSystem="{$oidISOCountry}" code="{$country154}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'nr'"><code codeSystem="{$oidISOCountry}" code="{$country155}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'np'"><code codeSystem="{$oidISOCountry}" code="{$country156}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'nl'"><code codeSystem="{$oidISOCountry}" code="{$country157}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'nc'"><code codeSystem="{$oidISOCountry}" code="{$country158}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'nz'"><code codeSystem="{$oidISOCountry}" code="{$country159}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ni'"><code codeSystem="{$oidISOCountry}" code="{$country160}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ne'"><code codeSystem="{$oidISOCountry}" code="{$country161}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ng'"><code codeSystem="{$oidISOCountry}" code="{$country162}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'nu'"><code codeSystem="{$oidISOCountry}" code="{$country163}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'nf'"><code codeSystem="{$oidISOCountry}" code="{$country164}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mp'"><code codeSystem="{$oidISOCountry}" code="{$country165}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'no'"><code codeSystem="{$oidISOCountry}" code="{$country166}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'om'"><code codeSystem="{$oidISOCountry}" code="{$country167}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'pk'"><code codeSystem="{$oidISOCountry}" code="{$country168}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'pw'"><code codeSystem="{$oidISOCountry}" code="{$country169}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ps'"><code codeSystem="{$oidISOCountry}" code="{$country170}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'pa'"><code codeSystem="{$oidISOCountry}" code="{$country171}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'pg'"><code codeSystem="{$oidISOCountry}" code="{$country172}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'py'"><code codeSystem="{$oidISOCountry}" code="{$country173}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'pe'"><code codeSystem="{$oidISOCountry}" code="{$country174}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ph'"><code codeSystem="{$oidISOCountry}" code="{$country175}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'pn'"><code codeSystem="{$oidISOCountry}" code="{$country176}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'pl'"><code codeSystem="{$oidISOCountry}" code="{$country177}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'pt'"><code codeSystem="{$oidISOCountry}" code="{$country178}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'pr'"><code codeSystem="{$oidISOCountry}" code="{$country179}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'qa'"><code codeSystem="{$oidISOCountry}" code="{$country180}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 're'"><code codeSystem="{$oidISOCountry}" code="{$country181}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ro'"><code codeSystem="{$oidISOCountry}" code="{$country182}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ru'"><code codeSystem="{$oidISOCountry}" code="{$country183}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'rw'"><code codeSystem="{$oidISOCountry}" code="{$country184}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'bl'"><code codeSystem="{$oidISOCountry}" code="{$country185}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sh'"><code codeSystem="{$oidISOCountry}" code="{$country186}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'kn'"><code codeSystem="{$oidISOCountry}" code="{$country187}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'lc'"><code codeSystem="{$oidISOCountry}" code="{$country188}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'mf'"><code codeSystem="{$oidISOCountry}" code="{$country189}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'pm'"><code codeSystem="{$oidISOCountry}" code="{$country190}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'vc'"><code codeSystem="{$oidISOCountry}" code="{$country191}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ws'"><code codeSystem="{$oidISOCountry}" code="{$country192}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sm'"><code codeSystem="{$oidISOCountry}" code="{$country193}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'st'"><code codeSystem="{$oidISOCountry}" code="{$country194}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sa'"><code codeSystem="{$oidISOCountry}" code="{$country195}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sn'"><code codeSystem="{$oidISOCountry}" code="{$country196}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'rs'"><code codeSystem="{$oidISOCountry}" code="{$country197}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sc'"><code codeSystem="{$oidISOCountry}" code="{$country198}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sl'"><code codeSystem="{$oidISOCountry}" code="{$country199}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sg'"><code codeSystem="{$oidISOCountry}" code="{$country200}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sx'"><code codeSystem="{$oidISOCountry}" code="{$country201}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sk'"><code codeSystem="{$oidISOCountry}" code="{$country202}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'si'"><code codeSystem="{$oidISOCountry}" code="{$country203}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sb'"><code codeSystem="{$oidISOCountry}" code="{$country204}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'so'"><code codeSystem="{$oidISOCountry}" code="{$country205}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'za'"><code codeSystem="{$oidISOCountry}" code="{$country206}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gs'"><code codeSystem="{$oidISOCountry}" code="{$country207}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ss'"><code codeSystem="{$oidISOCountry}" code="{$country208}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'es'"><code codeSystem="{$oidISOCountry}" code="{$country209}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'lk'"><code codeSystem="{$oidISOCountry}" code="{$country210}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sd'"><code codeSystem="{$oidISOCountry}" code="{$country211}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sr'"><code codeSystem="{$oidISOCountry}" code="{$country212}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sj'"><code codeSystem="{$oidISOCountry}" code="{$country213}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sz'"><code codeSystem="{$oidISOCountry}" code="{$country214}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'se'"><code codeSystem="{$oidISOCountry}" code="{$country215}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ch'"><code codeSystem="{$oidISOCountry}" code="{$country216}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'sy'"><code codeSystem="{$oidISOCountry}" code="{$country217}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tw'"><code codeSystem="{$oidISOCountry}" code="{$country218}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tj'"><code codeSystem="{$oidISOCountry}" code="{$country219}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tz'"><code codeSystem="{$oidISOCountry}" code="{$country220}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'th'"><code codeSystem="{$oidISOCountry}" code="{$country221}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tl'"><code codeSystem="{$oidISOCountry}" code="{$country222}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tg'"><code codeSystem="{$oidISOCountry}" code="{$country223}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tk'"><code codeSystem="{$oidISOCountry}" code="{$country224}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'to'"><code codeSystem="{$oidISOCountry}" code="{$country225}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tt'"><code codeSystem="{$oidISOCountry}" code="{$country226}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tn'"><code codeSystem="{$oidISOCountry}" code="{$country227}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tr'"><code codeSystem="{$oidISOCountry}" code="{$country228}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tm'"><code codeSystem="{$oidISOCountry}" code="{$country229}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tc'"><code codeSystem="{$oidISOCountry}" code="{$country230}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'tv'"><code codeSystem="{$oidISOCountry}" code="{$country231}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ug'"><code codeSystem="{$oidISOCountry}" code="{$country232}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ua'"><code codeSystem="{$oidISOCountry}" code="{$country233}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ae'"><code codeSystem="{$oidISOCountry}" code="{$country234}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'gb'"><code codeSystem="{$oidISOCountry}" code="{$country235}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'us'"><code codeSystem="{$oidISOCountry}" code="{$country236}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'um'"><code codeSystem="{$oidISOCountry}" code="{$country237}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'uy'"><code codeSystem="{$oidISOCountry}" code="{$country238}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'uz'"><code codeSystem="{$oidISOCountry}" code="{$country239}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'vu'"><code codeSystem="{$oidISOCountry}" code="{$country240}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 've'"><code codeSystem="{$oidISOCountry}" code="{$country241}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'vn'"><code codeSystem="{$oidISOCountry}" code="{$country242}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'vg'"><code codeSystem="{$oidISOCountry}" code="{$country243}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'vi'"><code codeSystem="{$oidISOCountry}" code="{$country244}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'wf'"><code codeSystem="{$oidISOCountry}" code="{$country245}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'eh'"><code codeSystem="{$oidISOCountry}" code="{$country246}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'ye'"><code codeSystem="{$oidISOCountry}" code="{$country247}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'zm'"><code codeSystem="{$oidISOCountry}" code="{$country248}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'zw'"><code codeSystem="{$oidISOCountry}" code="{$country249}" displayName="sourceReport"/> </xsl:when>
<xsl:when test="reportercountry = 'eu'"><code codeSystem="{$oidISOCountry}" code="{$country250}" displayName="sourceReport"/> </xsl:when>

<xsl:otherwise>
<code code="{reportercountry}" codeSystem="{$oidISOCountry}" displayName="sourceReport"/>

</xsl:otherwise>
</xsl:choose>
<xsl:comment>C.2.r.3:Reporter’s Country Code</xsl:comment>
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
<value language="{summaryandreportercommentslang}" xsi:type="ED" mediaType="text/plain"><xsl:value-of select="summaryandreportercomments"/></value>
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
<!--A.3.4.e Sender Country Code-->
<xsl:if test="string-length(sendercountrycode)>0">
<asLocatedEntity classCode="LOCE">
<location classCode="COUNTRY" determinerCode="INSTANCE">
<xsl:choose>
<xsl:when test="sendercountrycode = 'AFGHANISTAN'"><code codeSystem="{$oidISOCountry}" code="{$country1}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ALAND ISLANDS !ÅLAND ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country2}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ALBANIA'"><code codeSystem="{$oidISOCountry}" code="{$country3}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ALGERIA'"><code codeSystem="{$oidISOCountry}" code="{$country4}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'AMERICAN SAMOA'"><code codeSystem="{$oidISOCountry}" code="{$country5}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ANDORRA'"><code codeSystem="{$oidISOCountry}" code="{$country6}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ANGOLA'"><code codeSystem="{$oidISOCountry}" code="{$country7}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ANGUILLA'"><code codeSystem="{$oidISOCountry}" code="{$country8}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ANTARCTICA'"><code codeSystem="{$oidISOCountry}" code="{$country9}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ANTIGUA AND BARBUDA'"><code codeSystem="{$oidISOCountry}" code="{$country10}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ARGENTINA'"><code codeSystem="{$oidISOCountry}" code="{$country11}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ARMENIA'"><code codeSystem="{$oidISOCountry}" code="{$country12}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ARUBA'"><code codeSystem="{$oidISOCountry}" code="{$country13}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'AUSTRALIA'"><code codeSystem="{$oidISOCountry}" code="{$country14}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'AUSTRIA'"><code codeSystem="{$oidISOCountry}" code="{$country15}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'AZERBAIJAN'"><code codeSystem="{$oidISOCountry}" code="{$country16}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BAHAMAS'"><code codeSystem="{$oidISOCountry}" code="{$country17}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BAHRAIN'"><code codeSystem="{$oidISOCountry}" code="{$country18}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BANGLADESH'"><code codeSystem="{$oidISOCountry}" code="{$country19}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BARBADOS'"><code codeSystem="{$oidISOCountry}" code="{$country20}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BELARUS'"><code codeSystem="{$oidISOCountry}" code="{$country21}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BELGIUM'"><code codeSystem="{$oidISOCountry}" code="{$country22}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BELIZE'"><code codeSystem="{$oidISOCountry}" code="{$country23}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BENIN'"><code codeSystem="{$oidISOCountry}" code="{$country24}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BERMUDA'"><code codeSystem="{$oidISOCountry}" code="{$country25}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BHUTAN'"><code codeSystem="{$oidISOCountry}" code="{$country26}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BOLIVIA (PLURINATIONAL STATE OF)'"><code codeSystem="{$oidISOCountry}" code="{$country27}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BONAIRE, SINT EUSTATIUS AND SABA'"><code codeSystem="{$oidISOCountry}" code="{$country28}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BOSNIA AND HERZEGOVINA'"><code codeSystem="{$oidISOCountry}" code="{$country29}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BOTSWANA'"><code codeSystem="{$oidISOCountry}" code="{$country30}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BOUVET ISLAND'"><code codeSystem="{$oidISOCountry}" code="{$country31}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BRAZIL'"><code codeSystem="{$oidISOCountry}" code="{$country32}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BRITISH INDIAN OCEAN TERRITORY'"><code codeSystem="{$oidISOCountry}" code="{$country33}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BRUNEI DARUSSALAM'"><code codeSystem="{$oidISOCountry}" code="{$country34}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BULGARIA'"><code codeSystem="{$oidISOCountry}" code="{$country35}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BURKINA FASO'"><code codeSystem="{$oidISOCountry}" code="{$country36}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'BURUNDI'"><code codeSystem="{$oidISOCountry}" code="{$country37}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'CABO VERDE'"><code codeSystem="{$oidISOCountry}" code="{$country38}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'CAMBODIA'"><code codeSystem="{$oidISOCountry}" code="{$country39}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'CAMEROON'"><code codeSystem="{$oidISOCountry}" code="{$country40}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'CANADA'"><code codeSystem="{$oidISOCountry}" code="{$country41}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'CAYMAN ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country42}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'CENTRAL AFRICAN REPUBLIC'"><code codeSystem="{$oidISOCountry}" code="{$country43}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'CHAD'"><code codeSystem="{$oidISOCountry}" code="{$country44}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'CHILE'"><code codeSystem="{$oidISOCountry}" code="{$country45}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'CHINA'"><code codeSystem="{$oidISOCountry}" code="{$country46}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'CHRISTMAS ISLAND'"><code codeSystem="{$oidISOCountry}" code="{$country47}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'COCOS (KEELING) ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country48}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'COLOMBIA'"><code codeSystem="{$oidISOCountry}" code="{$country49}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'COMOROS'"><code codeSystem="{$oidISOCountry}" code="{$country50}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'CONGO'"><code codeSystem="{$oidISOCountry}" code="{$country51}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'CONGO (DEMOCRATIC REPUBLIC OF THE)'"><code codeSystem="{$oidISOCountry}" code="{$country52}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'COOK ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country53}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'COSTA RICA'"><code codeSystem="{$oidISOCountry}" code="{$country54}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'EUROPEAN UNION'"><code codeSystem="{$oidISOCountry}" code="{$country250}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'CROATIA'"><code codeSystem="{$oidISOCountry}" code="{$country56}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'CUBA'"><code codeSystem="{$oidISOCountry}" code="{$country57}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'CURACAO !CURAÇAO'"><code codeSystem="{$oidISOCountry}" code="{$country58}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'CYPRUS'"><code codeSystem="{$oidISOCountry}" code="{$country59}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'CZECH REPUBLIC'"><code codeSystem="{$oidISOCountry}" code="{$country60}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'DENMARK'"><code codeSystem="{$oidISOCountry}" code="{$country61}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'DJIBOUTI'"><code codeSystem="{$oidISOCountry}" code="{$country62}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'DOMINICA'"><code codeSystem="{$oidISOCountry}" code="{$country63}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'DOMINICAN REPUBLIC'"><code codeSystem="{$oidISOCountry}" code="{$country64}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ECUADOR'"><code codeSystem="{$oidISOCountry}" code="{$country65}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'EGYPT'"><code codeSystem="{$oidISOCountry}" code="{$country66}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'EL SALVADOR'"><code codeSystem="{$oidISOCountry}" code="{$country67}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'EQUATORIAL GUINEA'"><code codeSystem="{$oidISOCountry}" code="{$country68}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ERITREA'"><code codeSystem="{$oidISOCountry}" code="{$country69}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ESTONIA'"><code codeSystem="{$oidISOCountry}" code="{$country70}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ETHIOPIA'"><code codeSystem="{$oidISOCountry}" code="{$country71}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'FALKLAND ISLANDS (MALVINAS)'"><code codeSystem="{$oidISOCountry}" code="{$country72}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'FAROE ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country73}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'FIJI'"><code codeSystem="{$oidISOCountry}" code="{$country74}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'FINLAND'"><code codeSystem="{$oidISOCountry}" code="{$country75}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'FRANCE'"><code codeSystem="{$oidISOCountry}" code="{$country76}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'FRENCH GUIANA'"><code codeSystem="{$oidISOCountry}" code="{$country77}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'FRENCH POLYNESIA'"><code codeSystem="{$oidISOCountry}" code="{$country78}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'FRENCH SOUTHERN TERRITORIES'"><code codeSystem="{$oidISOCountry}" code="{$country79}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'GABON'"><code codeSystem="{$oidISOCountry}" code="{$country80}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'GAMBIA'"><code codeSystem="{$oidISOCountry}" code="{$country81}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'GEORGIA'"><code codeSystem="{$oidISOCountry}" code="{$country82}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'GERMANY'"><code codeSystem="{$oidISOCountry}" code="{$country83}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'GHANA'"><code codeSystem="{$oidISOCountry}" code="{$country84}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'GIBRALTAR'"><code codeSystem="{$oidISOCountry}" code="{$country85}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'GREECE'"><code codeSystem="{$oidISOCountry}" code="{$country86}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'GREENLAND'"><code codeSystem="{$oidISOCountry}" code="{$country87}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'GRENADA'"><code codeSystem="{$oidISOCountry}" code="{$country88}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'GUADELOUPE'"><code codeSystem="{$oidISOCountry}" code="{$country89}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'GUAM'"><code codeSystem="{$oidISOCountry}" code="{$country90}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'GUATEMALA'"><code codeSystem="{$oidISOCountry}" code="{$country91}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'GUERNSEY'"><code codeSystem="{$oidISOCountry}" code="{$country92}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'GUINEA'"><code codeSystem="{$oidISOCountry}" code="{$country93}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'GUINEA-BISSAU'"><code codeSystem="{$oidISOCountry}" code="{$country94}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'GUYANA'"><code codeSystem="{$oidISOCountry}" code="{$country95}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'HAITI'"><code codeSystem="{$oidISOCountry}" code="{$country96}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'HEARD ISLAND AND MCDONALD ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country97}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'HOLY SEE'"><code codeSystem="{$oidISOCountry}" code="{$country98}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'HONDURAS'"><code codeSystem="{$oidISOCountry}" code="{$country99}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'HONG KONG'"><code codeSystem="{$oidISOCountry}" code="{$country100}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'HUNGARY'"><code codeSystem="{$oidISOCountry}" code="{$country101}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ICELAND'"><code codeSystem="{$oidISOCountry}" code="{$country102}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'INDIA'"><code codeSystem="{$oidISOCountry}" code="{$country103}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'INDONESIA'"><code codeSystem="{$oidISOCountry}" code="{$country104}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'IRAN (ISLAMIC REPUBLIC OF)'"><code codeSystem="{$oidISOCountry}" code="{$country105}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'IRAQ'"><code codeSystem="{$oidISOCountry}" code="{$country106}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'IRELAND'"><code codeSystem="{$oidISOCountry}" code="{$country107}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ISLE OF MAN'"><code codeSystem="{$oidISOCountry}" code="{$country108}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ISRAEL'"><code codeSystem="{$oidISOCountry}" code="{$country109}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ITALY'"><code codeSystem="{$oidISOCountry}" code="{$country110}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'JAMAICA'"><code codeSystem="{$oidISOCountry}" code="{$country111}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'JAPAN'"><code codeSystem="{$oidISOCountry}" code="{$country112}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'JERSEY'"><code codeSystem="{$oidISOCountry}" code="{$country113}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'JORDAN'"><code codeSystem="{$oidISOCountry}" code="{$country114}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'KAZAKHSTAN'"><code codeSystem="{$oidISOCountry}" code="{$country115}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'KENYA'"><code codeSystem="{$oidISOCountry}" code="{$country116}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'KIRIBATI'"><code codeSystem="{$oidISOCountry}" code="{$country117}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'KUWAIT'"><code codeSystem="{$oidISOCountry}" code="{$country120}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'KYRGYZSTAN'"><code codeSystem="{$oidISOCountry}" code="{$country121}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'LATVIA'"><code codeSystem="{$oidISOCountry}" code="{$country123}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'LEBANON'"><code codeSystem="{$oidISOCountry}" code="{$country124}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'LESOTHO'"><code codeSystem="{$oidISOCountry}" code="{$country125}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'LIBERIA'"><code codeSystem="{$oidISOCountry}" code="{$country126}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'LIBYA'"><code codeSystem="{$oidISOCountry}" code="{$country127}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'LIECHTENSTEIN'"><code codeSystem="{$oidISOCountry}" code="{$country128}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'LITHUANIA'"><code codeSystem="{$oidISOCountry}" code="{$country129}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'LUXEMBOURG'"><code codeSystem="{$oidISOCountry}" code="{$country130}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MACAO'"><code codeSystem="{$oidISOCountry}" code="{$country131}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MACEDONIA (THE FORMER YUGOSLAV REPUBLIC OF)'"><code codeSystem="{$oidISOCountry}" code="{$country132}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MADAGASCAR'"><code codeSystem="{$oidISOCountry}" code="{$country133}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MALAWI'"><code codeSystem="{$oidISOCountry}" code="{$country134}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MALAYSIA'"><code codeSystem="{$oidISOCountry}" code="{$country135}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MALDIVES'"><code codeSystem="{$oidISOCountry}" code="{$country136}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MALI'"><code codeSystem="{$oidISOCountry}" code="{$country137}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MALTA'"><code codeSystem="{$oidISOCountry}" code="{$country138}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MARSHALL ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country139}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MARTINIQUE'"><code codeSystem="{$oidISOCountry}" code="{$country140}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MAURITANIA'"><code codeSystem="{$oidISOCountry}" code="{$country141}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MAURITIUS'"><code codeSystem="{$oidISOCountry}" code="{$country142}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MAYOTTE'"><code codeSystem="{$oidISOCountry}" code="{$country143}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MEXICO'"><code codeSystem="{$oidISOCountry}" code="{$country144}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MICRONESIA (FEDERATED STATES OF)'"><code codeSystem="{$oidISOCountry}" code="{$country145}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MOLDOVA (REPUBLIC OF)'"><code codeSystem="{$oidISOCountry}" code="{$country146}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MONACO'"><code codeSystem="{$oidISOCountry}" code="{$country147}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MONGOLIA'"><code codeSystem="{$oidISOCountry}" code="{$country148}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MONTENEGRO'"><code codeSystem="{$oidISOCountry}" code="{$country149}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MONTSERRAT'"><code codeSystem="{$oidISOCountry}" code="{$country150}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MOROCCO'"><code codeSystem="{$oidISOCountry}" code="{$country151}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MOZAMBIQUE'"><code codeSystem="{$oidISOCountry}" code="{$country152}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'MYANMAR'"><code codeSystem="{$oidISOCountry}" code="{$country153}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'NAMIBIA'"><code codeSystem="{$oidISOCountry}" code="{$country154}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'NAURU'"><code codeSystem="{$oidISOCountry}" code="{$country155}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'NEPAL'"><code codeSystem="{$oidISOCountry}" code="{$country156}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'NETHERLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country157}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'NEW CALEDONIA'"><code codeSystem="{$oidISOCountry}" code="{$country158}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'NEW ZEALAND'"><code codeSystem="{$oidISOCountry}" code="{$country159}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'NICARAGUA'"><code codeSystem="{$oidISOCountry}" code="{$country160}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'NIGER'"><code codeSystem="{$oidISOCountry}" code="{$country161}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'NIGERIA'"><code codeSystem="{$oidISOCountry}" code="{$country162}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'NIUE'"><code codeSystem="{$oidISOCountry}" code="{$country163}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'NORFOLK ISLAND'"><code codeSystem="{$oidISOCountry}" code="{$country164}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'NORTHERN MARIANA ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country165}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'NORWAY'"><code codeSystem="{$oidISOCountry}" code="{$country166}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'OMAN'"><code codeSystem="{$oidISOCountry}" code="{$country167}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'PAKISTAN'"><code codeSystem="{$oidISOCountry}" code="{$country168}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'PALAU'"><code codeSystem="{$oidISOCountry}" code="{$country169}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'PALESTINE, STATE OF'"><code codeSystem="{$oidISOCountry}" code="{$country170}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'PANAMA'"><code codeSystem="{$oidISOCountry}" code="{$country171}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'PAPUA NEW GUINEA'"><code codeSystem="{$oidISOCountry}" code="{$country172}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'PARAGUAY'"><code codeSystem="{$oidISOCountry}" code="{$country173}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'PERU'"><code codeSystem="{$oidISOCountry}" code="{$country174}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'PHILIPPINES'"><code codeSystem="{$oidISOCountry}" code="{$country175}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'PITCAIRN'"><code codeSystem="{$oidISOCountry}" code="{$country176}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'POLAND'"><code codeSystem="{$oidISOCountry}" code="{$country177}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'PORTUGAL'"><code codeSystem="{$oidISOCountry}" code="{$country178}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'PUERTO RICO'"><code codeSystem="{$oidISOCountry}" code="{$country179}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'QATAR'"><code codeSystem="{$oidISOCountry}" code="{$country180}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'REUNION !RÉUNION'"><code codeSystem="{$oidISOCountry}" code="{$country181}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ROMANIA'"><code codeSystem="{$oidISOCountry}" code="{$country182}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'RUSSIAN FEDERATION'"><code codeSystem="{$oidISOCountry}" code="{$country183}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'RWANDA'"><code codeSystem="{$oidISOCountry}" code="{$country184}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SAINT BARTHELEMY !SAINT BARTHÉLEMY'"><code codeSystem="{$oidISOCountry}" code="{$country185}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SAINT HELENA, ASCENSION AND TRISTAN DA CUNHA'"><code codeSystem="{$oidISOCountry}" code="{$country186}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SAINT KITTS AND NEVIS'"><code codeSystem="{$oidISOCountry}" code="{$country187}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SAINT LUCIA'"><code codeSystem="{$oidISOCountry}" code="{$country188}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SAINT MARTIN (FRENCH PART)'"><code codeSystem="{$oidISOCountry}" code="{$country189}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SAINT PIERRE AND MIQUELON'"><code codeSystem="{$oidISOCountry}" code="{$country190}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SAINT VINCENT AND THE GRENADINES'"><code codeSystem="{$oidISOCountry}" code="{$country191}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SAMOA'"><code codeSystem="{$oidISOCountry}" code="{$country192}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SAN MARINO'"><code codeSystem="{$oidISOCountry}" code="{$country193}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SAO TOME AND PRINCIPE'"><code codeSystem="{$oidISOCountry}" code="{$country194}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SAUDI ARABIA'"><code codeSystem="{$oidISOCountry}" code="{$country195}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SENEGAL'"><code codeSystem="{$oidISOCountry}" code="{$country196}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SERBIA'"><code codeSystem="{$oidISOCountry}" code="{$country197}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SEYCHELLES'"><code codeSystem="{$oidISOCountry}" code="{$country198}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SIERRA LEONE'"><code codeSystem="{$oidISOCountry}" code="{$country199}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SINGAPORE'"><code codeSystem="{$oidISOCountry}" code="{$country200}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SINT MAARTEN (DUTCH PART)'"><code codeSystem="{$oidISOCountry}" code="{$country201}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SLOVAKIA'"><code codeSystem="{$oidISOCountry}" code="{$country202}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SLOVENIA'"><code codeSystem="{$oidISOCountry}" code="{$country203}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SOLOMON ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country204}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SOMALIA'"><code codeSystem="{$oidISOCountry}" code="{$country205}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SOUTH AFRICA'"><code codeSystem="{$oidISOCountry}" code="{$country206}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country207}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SOUTH SUDAN'"><code codeSystem="{$oidISOCountry}" code="{$country208}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SPAIN'"><code codeSystem="{$oidISOCountry}" code="{$country209}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SRI LANKA'"><code codeSystem="{$oidISOCountry}" code="{$country210}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SUDAN'"><code codeSystem="{$oidISOCountry}" code="{$country211}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SURINAME'"><code codeSystem="{$oidISOCountry}" code="{$country212}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SVALBARD AND JAN MAYEN'"><code codeSystem="{$oidISOCountry}" code="{$country213}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SWAZILAND'"><code codeSystem="{$oidISOCountry}" code="{$country214}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SWEDEN'"><code codeSystem="{$oidISOCountry}" code="{$country215}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SWITZERLAND'"><code codeSystem="{$oidISOCountry}" code="{$country216}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'SYRIAN ARAB REPUBLIC'"><code codeSystem="{$oidISOCountry}" code="{$country217}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'TAIWAN, PROVINCE OF CHINA[A]'"><code codeSystem="{$oidISOCountry}" code="{$country218}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'TAJIKISTAN'"><code codeSystem="{$oidISOCountry}" code="{$country219}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'TANZANIA, UNITED REPUBLIC OF'"><code codeSystem="{$oidISOCountry}" code="{$country220}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'THAILAND'"><code codeSystem="{$oidISOCountry}" code="{$country221}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'TIMOR-LESTE'"><code codeSystem="{$oidISOCountry}" code="{$country222}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'TOGO'"><code codeSystem="{$oidISOCountry}" code="{$country223}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'TOKELAU'"><code codeSystem="{$oidISOCountry}" code="{$country224}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'TONGA'"><code codeSystem="{$oidISOCountry}" code="{$country225}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'TRINIDAD AND TOBAGO'"><code codeSystem="{$oidISOCountry}" code="{$country226}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'TUNISIA'"><code codeSystem="{$oidISOCountry}" code="{$country227}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'TURKEY'"><code codeSystem="{$oidISOCountry}" code="{$country228}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'TURKMENISTAN'"><code codeSystem="{$oidISOCountry}" code="{$country229}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'TURKS AND CAICOS ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country230}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'TUVALU'"><code codeSystem="{$oidISOCountry}" code="{$country231}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'UGANDA'"><code codeSystem="{$oidISOCountry}" code="{$country232}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'UKRAINE'"><code codeSystem="{$oidISOCountry}" code="{$country233}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'UNITED ARAB EMIRATES'"><code codeSystem="{$oidISOCountry}" code="{$country234}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'UNITED KINGDOM OF GREAT BRITAIN AND NORTHERN IRELAND'"><code codeSystem="{$oidISOCountry}" code="{$country235}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'UNITED KINGDOM'"><code codeSystem="{$oidISOCountry}" code="{$country235}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'UNITED STATES OF AMERICA'"><code codeSystem="{$oidISOCountry}" code="{$country236}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'UNITED STATES'"><code codeSystem="{$oidISOCountry}" code="{$country236}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'UNITED STATES MINOR OUTLYING ISLANDS'"><code codeSystem="{$oidISOCountry}" code="{$country237}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'URUGUAY'"><code codeSystem="{$oidISOCountry}" code="{$country238}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'UZBEKISTAN'"><code codeSystem="{$oidISOCountry}" code="{$country239}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'VANUATU'"><code codeSystem="{$oidISOCountry}" code="{$country240}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'VENEZUELA (BOLIVARIAN REPUBLIC OF)'"><code codeSystem="{$oidISOCountry}" code="{$country241}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'VIET NAM'"><code codeSystem="{$oidISOCountry}" code="{$country242}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'VIRGIN ISLANDS (BRITISH)'"><code codeSystem="{$oidISOCountry}" code="{$country243}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'VIRGIN ISLANDS (U.S.)'"><code codeSystem="{$oidISOCountry}" code="{$country244}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'WALLIS AND FUTUNA'"><code codeSystem="{$oidISOCountry}" code="{$country245}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'WESTERN SAHARA'"><code codeSystem="{$oidISOCountry}" code="{$country246}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'YEMEN'"><code codeSystem="{$oidISOCountry}" code="{$country247}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ZAMBIA'"><code codeSystem="{$oidISOCountry}" code="{$country248}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ZIMBABWE'"><code codeSystem="{$oidISOCountry}" code="{$country249}"/> </xsl:when>


<xsl:when test="sendercountrycode = 'Afghanistan'"><code codeSystem="{$oidISOCountry}" code="{$country1}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Aland Islands !Åland Islands'"><code codeSystem="{$oidISOCountry}" code="{$country2}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Albania'"><code codeSystem="{$oidISOCountry}" code="{$country3}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Algeria'"><code codeSystem="{$oidISOCountry}" code="{$country4}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'American Samoa'"><code codeSystem="{$oidISOCountry}" code="{$country5}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Andorra'"><code codeSystem="{$oidISOCountry}" code="{$country6}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Angola'"><code codeSystem="{$oidISOCountry}" code="{$country7}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Anguilla'"><code codeSystem="{$oidISOCountry}" code="{$country8}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Antarctica'"><code codeSystem="{$oidISOCountry}" code="{$country9}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Antigua and Barbuda'"><code codeSystem="{$oidISOCountry}" code="{$country10}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Argentina'"><code codeSystem="{$oidISOCountry}" code="{$country11}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Armenia'"><code codeSystem="{$oidISOCountry}" code="{$country12}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Aruba'"><code codeSystem="{$oidISOCountry}" code="{$country13}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Australia'"><code codeSystem="{$oidISOCountry}" code="{$country14}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Austria'"><code codeSystem="{$oidISOCountry}" code="{$country15}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Azerbaijan'"><code codeSystem="{$oidISOCountry}" code="{$country16}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Bahamas'"><code codeSystem="{$oidISOCountry}" code="{$country17}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Bahrain'"><code codeSystem="{$oidISOCountry}" code="{$country18}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Bangladesh'"><code codeSystem="{$oidISOCountry}" code="{$country19}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Barbados'"><code codeSystem="{$oidISOCountry}" code="{$country20}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Belarus'"><code codeSystem="{$oidISOCountry}" code="{$country21}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Belgium'"><code codeSystem="{$oidISOCountry}" code="{$country22}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Belize'"><code codeSystem="{$oidISOCountry}" code="{$country23}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Benin'"><code codeSystem="{$oidISOCountry}" code="{$country24}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Bermuda'"><code codeSystem="{$oidISOCountry}" code="{$country25}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Bhutan'"><code codeSystem="{$oidISOCountry}" code="{$country26}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Bolivia (Plurinational State of)'"><code codeSystem="{$oidISOCountry}" code="{$country27}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Bonaire, Sint Eustatius and Saba'"><code codeSystem="{$oidISOCountry}" code="{$country28}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Bosnia and Herzegovina'"><code codeSystem="{$oidISOCountry}" code="{$country29}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Botswana'"><code codeSystem="{$oidISOCountry}" code="{$country30}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Bouvet Island'"><code codeSystem="{$oidISOCountry}" code="{$country31}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Brazil'"><code codeSystem="{$oidISOCountry}" code="{$country32}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'British Indian Ocean Territory'"><code codeSystem="{$oidISOCountry}" code="{$country33}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Brunei Darussalam'"><code codeSystem="{$oidISOCountry}" code="{$country34}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Bulgaria'"><code codeSystem="{$oidISOCountry}" code="{$country35}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Burkina Faso'"><code codeSystem="{$oidISOCountry}" code="{$country36}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Burundi'"><code codeSystem="{$oidISOCountry}" code="{$country37}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Cabo Verde'"><code codeSystem="{$oidISOCountry}" code="{$country38}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Cambodia'"><code codeSystem="{$oidISOCountry}" code="{$country39}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Cameroon'"><code codeSystem="{$oidISOCountry}" code="{$country40}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Canada'"><code codeSystem="{$oidISOCountry}" code="{$country41}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Cayman Islands'"><code codeSystem="{$oidISOCountry}" code="{$country42}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Central African Republic'"><code codeSystem="{$oidISOCountry}" code="{$country43}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Chad'"><code codeSystem="{$oidISOCountry}" code="{$country44}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Chile'"><code codeSystem="{$oidISOCountry}" code="{$country45}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'China'"><code codeSystem="{$oidISOCountry}" code="{$country46}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Christmas Island'"><code codeSystem="{$oidISOCountry}" code="{$country47}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Cocos (Keeling) Islands'"><code codeSystem="{$oidISOCountry}" code="{$country48}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Colombia'"><code codeSystem="{$oidISOCountry}" code="{$country49}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Comoros'"><code codeSystem="{$oidISOCountry}" code="{$country50}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Congo'"><code codeSystem="{$oidISOCountry}" code="{$country51}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Congo (Democratic Republic of the)'"><code codeSystem="{$oidISOCountry}" code="{$country52}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Cook Islands'"><code codeSystem="{$oidISOCountry}" code="{$country53}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Costa Rica'"><code codeSystem="{$oidISOCountry}" code="{$country54}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'European Union'"><code codeSystem="{$oidISOCountry}" code="{$country250}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Croatia'"><code codeSystem="{$oidISOCountry}" code="{$country56}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Cuba'"><code codeSystem="{$oidISOCountry}" code="{$country57}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Curacao !Curaçao'"><code codeSystem="{$oidISOCountry}" code="{$country58}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Cyprus'"><code codeSystem="{$oidISOCountry}" code="{$country59}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Czech Republic'"><code codeSystem="{$oidISOCountry}" code="{$country60}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Denmark'"><code codeSystem="{$oidISOCountry}" code="{$country61}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Djibouti'"><code codeSystem="{$oidISOCountry}" code="{$country62}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Dominica'"><code codeSystem="{$oidISOCountry}" code="{$country63}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Dominican Republic'"><code codeSystem="{$oidISOCountry}" code="{$country64}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Ecuador'"><code codeSystem="{$oidISOCountry}" code="{$country65}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Egypt'"><code codeSystem="{$oidISOCountry}" code="{$country66}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'El Salvador'"><code codeSystem="{$oidISOCountry}" code="{$country67}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Equatorial Guinea'"><code codeSystem="{$oidISOCountry}" code="{$country68}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Eritrea'"><code codeSystem="{$oidISOCountry}" code="{$country69}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Estonia'"><code codeSystem="{$oidISOCountry}" code="{$country70}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Ethiopia'"><code codeSystem="{$oidISOCountry}" code="{$country71}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Falkland Islands (Malvinas)'"><code codeSystem="{$oidISOCountry}" code="{$country72}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Faroe Islands'"><code codeSystem="{$oidISOCountry}" code="{$country73}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Fiji'"><code codeSystem="{$oidISOCountry}" code="{$country74}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Finland'"><code codeSystem="{$oidISOCountry}" code="{$country75}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'France'"><code codeSystem="{$oidISOCountry}" code="{$country76}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'French Guiana'"><code codeSystem="{$oidISOCountry}" code="{$country77}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'French Polynesia'"><code codeSystem="{$oidISOCountry}" code="{$country78}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'French Southern Territories'"><code codeSystem="{$oidISOCountry}" code="{$country79}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Gabon'"><code codeSystem="{$oidISOCountry}" code="{$country80}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Gambia'"><code codeSystem="{$oidISOCountry}" code="{$country81}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Georgia'"><code codeSystem="{$oidISOCountry}" code="{$country82}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Germany'"><code codeSystem="{$oidISOCountry}" code="{$country83}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Ghana'"><code codeSystem="{$oidISOCountry}" code="{$country84}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Gibraltar'"><code codeSystem="{$oidISOCountry}" code="{$country85}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Greece'"><code codeSystem="{$oidISOCountry}" code="{$country86}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Greenland'"><code codeSystem="{$oidISOCountry}" code="{$country87}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Grenada'"><code codeSystem="{$oidISOCountry}" code="{$country88}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Guadeloupe'"><code codeSystem="{$oidISOCountry}" code="{$country89}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Guam'"><code codeSystem="{$oidISOCountry}" code="{$country90}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Guatemala'"><code codeSystem="{$oidISOCountry}" code="{$country91}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Guernsey'"><code codeSystem="{$oidISOCountry}" code="{$country92}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Guinea'"><code codeSystem="{$oidISOCountry}" code="{$country93}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Guinea-Bissau'"><code codeSystem="{$oidISOCountry}" code="{$country94}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Guyana'"><code codeSystem="{$oidISOCountry}" code="{$country95}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Haiti'"><code codeSystem="{$oidISOCountry}" code="{$country96}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Heard Island and McDonald Islands'"><code codeSystem="{$oidISOCountry}" code="{$country97}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Holy See'"><code codeSystem="{$oidISOCountry}" code="{$country98}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Honduras'"><code codeSystem="{$oidISOCountry}" code="{$country99}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Hong Kong'"><code codeSystem="{$oidISOCountry}" code="{$country100}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Hungary'"><code codeSystem="{$oidISOCountry}" code="{$country101}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Iceland'"><code codeSystem="{$oidISOCountry}" code="{$country102}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'India'"><code codeSystem="{$oidISOCountry}" code="{$country103}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Indonesia'"><code codeSystem="{$oidISOCountry}" code="{$country104}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Iran (Islamic Republic of)'"><code codeSystem="{$oidISOCountry}" code="{$country105}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Iraq'"><code codeSystem="{$oidISOCountry}" code="{$country106}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Ireland'"><code codeSystem="{$oidISOCountry}" code="{$country107}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Isle of Man'"><code codeSystem="{$oidISOCountry}" code="{$country108}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Israel'"><code codeSystem="{$oidISOCountry}" code="{$country109}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Italy'"><code codeSystem="{$oidISOCountry}" code="{$country110}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Jamaica'"><code codeSystem="{$oidISOCountry}" code="{$country111}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Japan'"><code codeSystem="{$oidISOCountry}" code="{$country112}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Jersey'"><code codeSystem="{$oidISOCountry}" code="{$country113}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Jordan'"><code codeSystem="{$oidISOCountry}" code="{$country114}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Kazakhstan'"><code codeSystem="{$oidISOCountry}" code="{$country115}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Kenya'"><code codeSystem="{$oidISOCountry}" code="{$country116}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Kiribati'"><code codeSystem="{$oidISOCountry}" code="{$country117}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Kuwait'"><code codeSystem="{$oidISOCountry}" code="{$country120}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Kyrgyzstan'"><code codeSystem="{$oidISOCountry}" code="{$country121}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Latvia'"><code codeSystem="{$oidISOCountry}" code="{$country123}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Lebanon'"><code codeSystem="{$oidISOCountry}" code="{$country124}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Lesotho'"><code codeSystem="{$oidISOCountry}" code="{$country125}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Liberia'"><code codeSystem="{$oidISOCountry}" code="{$country126}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Libya'"><code codeSystem="{$oidISOCountry}" code="{$country127}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Liechtenstein'"><code codeSystem="{$oidISOCountry}" code="{$country128}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Lithuania'"><code codeSystem="{$oidISOCountry}" code="{$country129}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Luxembourg'"><code codeSystem="{$oidISOCountry}" code="{$country130}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Macao'"><code codeSystem="{$oidISOCountry}" code="{$country131}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Macedonia (the former Yugoslav Republic of)'"><code codeSystem="{$oidISOCountry}" code="{$country132}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Madagascar'"><code codeSystem="{$oidISOCountry}" code="{$country133}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Malawi'"><code codeSystem="{$oidISOCountry}" code="{$country134}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Malaysia'"><code codeSystem="{$oidISOCountry}" code="{$country135}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Maldives'"><code codeSystem="{$oidISOCountry}" code="{$country136}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Mali'"><code codeSystem="{$oidISOCountry}" code="{$country137}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Malta'"><code codeSystem="{$oidISOCountry}" code="{$country138}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Marshall Islands'"><code codeSystem="{$oidISOCountry}" code="{$country139}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Martinique'"><code codeSystem="{$oidISOCountry}" code="{$country140}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Mauritania'"><code codeSystem="{$oidISOCountry}" code="{$country141}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Mauritius'"><code codeSystem="{$oidISOCountry}" code="{$country142}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Mayotte'"><code codeSystem="{$oidISOCountry}" code="{$country143}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Mexico'"><code codeSystem="{$oidISOCountry}" code="{$country144}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Micronesia (Federated States of)'"><code codeSystem="{$oidISOCountry}" code="{$country145}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Moldova (Republic of)'"><code codeSystem="{$oidISOCountry}" code="{$country146}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Monaco'"><code codeSystem="{$oidISOCountry}" code="{$country147}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Mongolia'"><code codeSystem="{$oidISOCountry}" code="{$country148}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Montenegro'"><code codeSystem="{$oidISOCountry}" code="{$country149}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Montserrat'"><code codeSystem="{$oidISOCountry}" code="{$country150}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Morocco'"><code codeSystem="{$oidISOCountry}" code="{$country151}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Mozambique'"><code codeSystem="{$oidISOCountry}" code="{$country152}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Myanmar'"><code codeSystem="{$oidISOCountry}" code="{$country153}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Namibia'"><code codeSystem="{$oidISOCountry}" code="{$country154}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Nauru'"><code codeSystem="{$oidISOCountry}" code="{$country155}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Nepal'"><code codeSystem="{$oidISOCountry}" code="{$country156}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Netherlands'"><code codeSystem="{$oidISOCountry}" code="{$country157}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'New Caledonia'"><code codeSystem="{$oidISOCountry}" code="{$country158}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'New Zealand'"><code codeSystem="{$oidISOCountry}" code="{$country159}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Nicaragua'"><code codeSystem="{$oidISOCountry}" code="{$country160}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Niger'"><code codeSystem="{$oidISOCountry}" code="{$country161}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Nigeria'"><code codeSystem="{$oidISOCountry}" code="{$country162}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Niue'"><code codeSystem="{$oidISOCountry}" code="{$country163}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Norfolk Island'"><code codeSystem="{$oidISOCountry}" code="{$country164}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Northern Mariana Islands'"><code codeSystem="{$oidISOCountry}" code="{$country165}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Norway'"><code codeSystem="{$oidISOCountry}" code="{$country166}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Oman'"><code codeSystem="{$oidISOCountry}" code="{$country167}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Pakistan'"><code codeSystem="{$oidISOCountry}" code="{$country168}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Palau'"><code codeSystem="{$oidISOCountry}" code="{$country169}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Palestine, State of'"><code codeSystem="{$oidISOCountry}" code="{$country170}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Panama'"><code codeSystem="{$oidISOCountry}" code="{$country171}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Papua New Guinea'"><code codeSystem="{$oidISOCountry}" code="{$country172}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Paraguay'"><code codeSystem="{$oidISOCountry}" code="{$country173}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Peru'"><code codeSystem="{$oidISOCountry}" code="{$country174}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Philippines'"><code codeSystem="{$oidISOCountry}" code="{$country175}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Pitcairn'"><code codeSystem="{$oidISOCountry}" code="{$country176}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Poland'"><code codeSystem="{$oidISOCountry}" code="{$country177}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Portugal'"><code codeSystem="{$oidISOCountry}" code="{$country178}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Puerto Rico'"><code codeSystem="{$oidISOCountry}" code="{$country179}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Qatar'"><code codeSystem="{$oidISOCountry}" code="{$country180}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Reunion !Réunion'"><code codeSystem="{$oidISOCountry}" code="{$country181}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Romania'"><code codeSystem="{$oidISOCountry}" code="{$country182}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Russian Federation'"><code codeSystem="{$oidISOCountry}" code="{$country183}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Rwanda'"><code codeSystem="{$oidISOCountry}" code="{$country184}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Saint Barthelemy !Saint Barthélemy'"><code codeSystem="{$oidISOCountry}" code="{$country185}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Saint Helena, Ascension and Tristan da Cunha'"><code codeSystem="{$oidISOCountry}" code="{$country186}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Saint Kitts and Nevis'"><code codeSystem="{$oidISOCountry}" code="{$country187}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Saint Lucia'"><code codeSystem="{$oidISOCountry}" code="{$country188}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Saint Martin (French part)'"><code codeSystem="{$oidISOCountry}" code="{$country189}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Saint Pierre and Miquelon'"><code codeSystem="{$oidISOCountry}" code="{$country190}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Saint Vincent and the Grenadines'"><code codeSystem="{$oidISOCountry}" code="{$country191}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Samoa'"><code codeSystem="{$oidISOCountry}" code="{$country192}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'San Marino'"><code codeSystem="{$oidISOCountry}" code="{$country193}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Sao Tome and Principe'"><code codeSystem="{$oidISOCountry}" code="{$country194}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Saudi Arabia'"><code codeSystem="{$oidISOCountry}" code="{$country195}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Senegal'"><code codeSystem="{$oidISOCountry}" code="{$country196}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Serbia'"><code codeSystem="{$oidISOCountry}" code="{$country197}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Seychelles'"><code codeSystem="{$oidISOCountry}" code="{$country198}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Sierra Leone'"><code codeSystem="{$oidISOCountry}" code="{$country199}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Singapore'"><code codeSystem="{$oidISOCountry}" code="{$country200}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Sint Maarten (Dutch part)'"><code codeSystem="{$oidISOCountry}" code="{$country201}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Slovakia'"><code codeSystem="{$oidISOCountry}" code="{$country202}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Slovenia'"><code codeSystem="{$oidISOCountry}" code="{$country203}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Solomon Islands'"><code codeSystem="{$oidISOCountry}" code="{$country204}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Somalia'"><code codeSystem="{$oidISOCountry}" code="{$country205}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'South Africa'"><code codeSystem="{$oidISOCountry}" code="{$country206}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'South Georgia and the South Sandwich Islands'"><code codeSystem="{$oidISOCountry}" code="{$country207}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'South Sudan'"><code codeSystem="{$oidISOCountry}" code="{$country208}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Spain'"><code codeSystem="{$oidISOCountry}" code="{$country209}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Sri Lanka'"><code codeSystem="{$oidISOCountry}" code="{$country210}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Sudan'"><code codeSystem="{$oidISOCountry}" code="{$country211}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Suriname'"><code codeSystem="{$oidISOCountry}" code="{$country212}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Svalbard and Jan Mayen'"><code codeSystem="{$oidISOCountry}" code="{$country213}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Swaziland'"><code codeSystem="{$oidISOCountry}" code="{$country214}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Sweden'"><code codeSystem="{$oidISOCountry}" code="{$country215}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Switzerland'"><code codeSystem="{$oidISOCountry}" code="{$country216}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Syrian Arab Republic'"><code codeSystem="{$oidISOCountry}" code="{$country217}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Taiwan, Province of China[a]'"><code codeSystem="{$oidISOCountry}" code="{$country218}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Tajikistan'"><code codeSystem="{$oidISOCountry}" code="{$country219}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Tanzania, United Republic of'"><code codeSystem="{$oidISOCountry}" code="{$country220}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Thailand'"><code codeSystem="{$oidISOCountry}" code="{$country221}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Timor-Leste'"><code codeSystem="{$oidISOCountry}" code="{$country222}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Togo'"><code codeSystem="{$oidISOCountry}" code="{$country223}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Tokelau'"><code codeSystem="{$oidISOCountry}" code="{$country224}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Tonga'"><code codeSystem="{$oidISOCountry}" code="{$country225}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Trinidad and Tobago'"><code codeSystem="{$oidISOCountry}" code="{$country226}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Tunisia'"><code codeSystem="{$oidISOCountry}" code="{$country227}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Turkey'"><code codeSystem="{$oidISOCountry}" code="{$country228}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Turkmenistan'"><code codeSystem="{$oidISOCountry}" code="{$country229}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Turks and Caicos Islands'"><code codeSystem="{$oidISOCountry}" code="{$country230}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Tuvalu'"><code codeSystem="{$oidISOCountry}" code="{$country231}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Uganda'"><code codeSystem="{$oidISOCountry}" code="{$country232}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Ukraine'"><code codeSystem="{$oidISOCountry}" code="{$country233}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'United Arab Emirates'"><code codeSystem="{$oidISOCountry}" code="{$country234}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'United Kingdom of Great Britain and Northern Ireland'"><code codeSystem="{$oidISOCountry}" code="{$country235}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'United Kingdom'"><code codeSystem="{$oidISOCountry}" code="{$country235}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'United States of America'"><code codeSystem="{$oidISOCountry}" code="{$country236}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'United States'"><code codeSystem="{$oidISOCountry}" code="{$country236}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'United States Minor Outlying Islands'"><code codeSystem="{$oidISOCountry}" code="{$country237}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Uruguay'"><code codeSystem="{$oidISOCountry}" code="{$country238}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Uzbekistan'"><code codeSystem="{$oidISOCountry}" code="{$country239}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Vanuatu'"><code codeSystem="{$oidISOCountry}" code="{$country240}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Venezuela (Bolivarian Republic of)'"><code codeSystem="{$oidISOCountry}" code="{$country241}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Viet Nam'"><code codeSystem="{$oidISOCountry}" code="{$country242}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Virgin Islands (British)'"><code codeSystem="{$oidISOCountry}" code="{$country243}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Virgin Islands (U.S.)'"><code codeSystem="{$oidISOCountry}" code="{$country244}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Wallis and Futuna'"><code codeSystem="{$oidISOCountry}" code="{$country245}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Western Sahara'"><code codeSystem="{$oidISOCountry}" code="{$country246}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Yemen'"><code codeSystem="{$oidISOCountry}" code="{$country247}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Zambia'"><code codeSystem="{$oidISOCountry}" code="{$country248}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'Zimbabwe'"><code codeSystem="{$oidISOCountry}" code="{$country249}"/> </xsl:when>

<xsl:when test="sendercountrycode = 'afghanistan'"><code codeSystem="{$oidISOCountry}" code="{$country1}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'aland islands !åland islands'"><code codeSystem="{$oidISOCountry}" code="{$country2}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'albania'"><code codeSystem="{$oidISOCountry}" code="{$country3}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'algeria'"><code codeSystem="{$oidISOCountry}" code="{$country4}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'american samoa'"><code codeSystem="{$oidISOCountry}" code="{$country5}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'andorra'"><code codeSystem="{$oidISOCountry}" code="{$country6}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'angola'"><code codeSystem="{$oidISOCountry}" code="{$country7}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'anguilla'"><code codeSystem="{$oidISOCountry}" code="{$country8}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'antarctica'"><code codeSystem="{$oidISOCountry}" code="{$country9}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'antigua and barbuda'"><code codeSystem="{$oidISOCountry}" code="{$country10}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'argentina'"><code codeSystem="{$oidISOCountry}" code="{$country11}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'armenia'"><code codeSystem="{$oidISOCountry}" code="{$country12}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'aruba'"><code codeSystem="{$oidISOCountry}" code="{$country13}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'australia'"><code codeSystem="{$oidISOCountry}" code="{$country14}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'austria'"><code codeSystem="{$oidISOCountry}" code="{$country15}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'azerbaijan'"><code codeSystem="{$oidISOCountry}" code="{$country16}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'bahamas'"><code codeSystem="{$oidISOCountry}" code="{$country17}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'bahrain'"><code codeSystem="{$oidISOCountry}" code="{$country18}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'bangladesh'"><code codeSystem="{$oidISOCountry}" code="{$country19}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'barbados'"><code codeSystem="{$oidISOCountry}" code="{$country20}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'belarus'"><code codeSystem="{$oidISOCountry}" code="{$country21}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'belgium'"><code codeSystem="{$oidISOCountry}" code="{$country22}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'belize'"><code codeSystem="{$oidISOCountry}" code="{$country23}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'benin'"><code codeSystem="{$oidISOCountry}" code="{$country24}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'bermuda'"><code codeSystem="{$oidISOCountry}" code="{$country25}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'bhutan'"><code codeSystem="{$oidISOCountry}" code="{$country26}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'bolivia (plurinational state of)'"><code codeSystem="{$oidISOCountry}" code="{$country27}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'bonaire, sint eustatius and saba'"><code codeSystem="{$oidISOCountry}" code="{$country28}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'bosnia and herzegovina'"><code codeSystem="{$oidISOCountry}" code="{$country29}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'botswana'"><code codeSystem="{$oidISOCountry}" code="{$country30}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'bouvet island'"><code codeSystem="{$oidISOCountry}" code="{$country31}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'brazil'"><code codeSystem="{$oidISOCountry}" code="{$country32}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'british indian ocean territory'"><code codeSystem="{$oidISOCountry}" code="{$country33}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'brunei darussalam'"><code codeSystem="{$oidISOCountry}" code="{$country34}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'bulgaria'"><code codeSystem="{$oidISOCountry}" code="{$country35}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'burkina faso'"><code codeSystem="{$oidISOCountry}" code="{$country36}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'burundi'"><code codeSystem="{$oidISOCountry}" code="{$country37}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'cabo verde'"><code codeSystem="{$oidISOCountry}" code="{$country38}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'cambodia'"><code codeSystem="{$oidISOCountry}" code="{$country39}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'cameroon'"><code codeSystem="{$oidISOCountry}" code="{$country40}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'canada'"><code codeSystem="{$oidISOCountry}" code="{$country41}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'cayman islands'"><code codeSystem="{$oidISOCountry}" code="{$country42}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'central african republic'"><code codeSystem="{$oidISOCountry}" code="{$country43}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'chad'"><code codeSystem="{$oidISOCountry}" code="{$country44}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'chile'"><code codeSystem="{$oidISOCountry}" code="{$country45}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'china'"><code codeSystem="{$oidISOCountry}" code="{$country46}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'christmas island'"><code codeSystem="{$oidISOCountry}" code="{$country47}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'cocos (keeling) islands'"><code codeSystem="{$oidISOCountry}" code="{$country48}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'colombia'"><code codeSystem="{$oidISOCountry}" code="{$country49}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'comoros'"><code codeSystem="{$oidISOCountry}" code="{$country50}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'congo'"><code codeSystem="{$oidISOCountry}" code="{$country51}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'congo (democratic republic of the)'"><code codeSystem="{$oidISOCountry}" code="{$country52}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'cook islands'"><code codeSystem="{$oidISOCountry}" code="{$country53}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'costa rica'"><code codeSystem="{$oidISOCountry}" code="{$country54}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'european union'"><code codeSystem="{$oidISOCountry}" code="{$country250}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'croatia'"><code codeSystem="{$oidISOCountry}" code="{$country56}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'cuba'"><code codeSystem="{$oidISOCountry}" code="{$country57}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'curacao !curaçao'"><code codeSystem="{$oidISOCountry}" code="{$country58}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'cyprus'"><code codeSystem="{$oidISOCountry}" code="{$country59}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'czech republic'"><code codeSystem="{$oidISOCountry}" code="{$country60}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'denmark'"><code codeSystem="{$oidISOCountry}" code="{$country61}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'djibouti'"><code codeSystem="{$oidISOCountry}" code="{$country62}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'dominica'"><code codeSystem="{$oidISOCountry}" code="{$country63}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'dominican republic'"><code codeSystem="{$oidISOCountry}" code="{$country64}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ecuador'"><code codeSystem="{$oidISOCountry}" code="{$country65}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'egypt'"><code codeSystem="{$oidISOCountry}" code="{$country66}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'el salvador'"><code codeSystem="{$oidISOCountry}" code="{$country67}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'equatorial guinea'"><code codeSystem="{$oidISOCountry}" code="{$country68}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'eritrea'"><code codeSystem="{$oidISOCountry}" code="{$country69}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'estonia'"><code codeSystem="{$oidISOCountry}" code="{$country70}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ethiopia'"><code codeSystem="{$oidISOCountry}" code="{$country71}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'falkland islands (malvinas)'"><code codeSystem="{$oidISOCountry}" code="{$country72}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'faroe islands'"><code codeSystem="{$oidISOCountry}" code="{$country73}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'fiji'"><code codeSystem="{$oidISOCountry}" code="{$country74}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'finland'"><code codeSystem="{$oidISOCountry}" code="{$country75}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'france'"><code codeSystem="{$oidISOCountry}" code="{$country76}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'french guiana'"><code codeSystem="{$oidISOCountry}" code="{$country77}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'french polynesia'"><code codeSystem="{$oidISOCountry}" code="{$country78}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'french southern territories'"><code codeSystem="{$oidISOCountry}" code="{$country79}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'gabon'"><code codeSystem="{$oidISOCountry}" code="{$country80}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'gambia'"><code codeSystem="{$oidISOCountry}" code="{$country81}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'georgia'"><code codeSystem="{$oidISOCountry}" code="{$country82}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'germany'"><code codeSystem="{$oidISOCountry}" code="{$country83}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ghana'"><code codeSystem="{$oidISOCountry}" code="{$country84}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'gibraltar'"><code codeSystem="{$oidISOCountry}" code="{$country85}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'greece'"><code codeSystem="{$oidISOCountry}" code="{$country86}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'greenland'"><code codeSystem="{$oidISOCountry}" code="{$country87}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'grenada'"><code codeSystem="{$oidISOCountry}" code="{$country88}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'guadeloupe'"><code codeSystem="{$oidISOCountry}" code="{$country89}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'guam'"><code codeSystem="{$oidISOCountry}" code="{$country90}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'guatemala'"><code codeSystem="{$oidISOCountry}" code="{$country91}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'guernsey'"><code codeSystem="{$oidISOCountry}" code="{$country92}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'guinea'"><code codeSystem="{$oidISOCountry}" code="{$country93}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'guinea-bissau'"><code codeSystem="{$oidISOCountry}" code="{$country94}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'guyana'"><code codeSystem="{$oidISOCountry}" code="{$country95}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'haiti'"><code codeSystem="{$oidISOCountry}" code="{$country96}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'heard island and mcdonald islands'"><code codeSystem="{$oidISOCountry}" code="{$country97}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'holy see'"><code codeSystem="{$oidISOCountry}" code="{$country98}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'honduras'"><code codeSystem="{$oidISOCountry}" code="{$country99}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'hong kong'"><code codeSystem="{$oidISOCountry}" code="{$country100}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'hungary'"><code codeSystem="{$oidISOCountry}" code="{$country101}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'iceland'"><code codeSystem="{$oidISOCountry}" code="{$country102}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'india'"><code codeSystem="{$oidISOCountry}" code="{$country103}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'indonesia'"><code codeSystem="{$oidISOCountry}" code="{$country104}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'iran (islamic republic of)'"><code codeSystem="{$oidISOCountry}" code="{$country105}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'iraq'"><code codeSystem="{$oidISOCountry}" code="{$country106}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ireland'"><code codeSystem="{$oidISOCountry}" code="{$country107}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'isle of man'"><code codeSystem="{$oidISOCountry}" code="{$country108}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'israel'"><code codeSystem="{$oidISOCountry}" code="{$country109}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'italy'"><code codeSystem="{$oidISOCountry}" code="{$country110}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'jamaica'"><code codeSystem="{$oidISOCountry}" code="{$country111}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'japan'"><code codeSystem="{$oidISOCountry}" code="{$country112}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'jersey'"><code codeSystem="{$oidISOCountry}" code="{$country113}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'jordan'"><code codeSystem="{$oidISOCountry}" code="{$country114}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'kazakhstan'"><code codeSystem="{$oidISOCountry}" code="{$country115}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'kenya'"><code codeSystem="{$oidISOCountry}" code="{$country116}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'kiribati'"><code codeSystem="{$oidISOCountry}" code="{$country117}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'kuwait'"><code codeSystem="{$oidISOCountry}" code="{$country120}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'kyrgyzstan'"><code codeSystem="{$oidISOCountry}" code="{$country121}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'latvia'"><code codeSystem="{$oidISOCountry}" code="{$country123}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'lebanon'"><code codeSystem="{$oidISOCountry}" code="{$country124}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'lesotho'"><code codeSystem="{$oidISOCountry}" code="{$country125}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'liberia'"><code codeSystem="{$oidISOCountry}" code="{$country126}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'libya'"><code codeSystem="{$oidISOCountry}" code="{$country127}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'liechtenstein'"><code codeSystem="{$oidISOCountry}" code="{$country128}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'lithuania'"><code codeSystem="{$oidISOCountry}" code="{$country129}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'luxembourg'"><code codeSystem="{$oidISOCountry}" code="{$country130}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'macao'"><code codeSystem="{$oidISOCountry}" code="{$country131}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'macedonia (the former yugoslav republic of)'"><code codeSystem="{$oidISOCountry}" code="{$country132}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'madagascar'"><code codeSystem="{$oidISOCountry}" code="{$country133}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'malawi'"><code codeSystem="{$oidISOCountry}" code="{$country134}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'malaysia'"><code codeSystem="{$oidISOCountry}" code="{$country135}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'maldives'"><code codeSystem="{$oidISOCountry}" code="{$country136}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'mali'"><code codeSystem="{$oidISOCountry}" code="{$country137}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'malta'"><code codeSystem="{$oidISOCountry}" code="{$country138}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'marshall islands'"><code codeSystem="{$oidISOCountry}" code="{$country139}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'martinique'"><code codeSystem="{$oidISOCountry}" code="{$country140}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'mauritania'"><code codeSystem="{$oidISOCountry}" code="{$country141}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'mauritius'"><code codeSystem="{$oidISOCountry}" code="{$country142}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'mayotte'"><code codeSystem="{$oidISOCountry}" code="{$country143}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'mexico'"><code codeSystem="{$oidISOCountry}" code="{$country144}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'micronesia (federated states of)'"><code codeSystem="{$oidISOCountry}" code="{$country145}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'moldova (republic of)'"><code codeSystem="{$oidISOCountry}" code="{$country146}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'monaco'"><code codeSystem="{$oidISOCountry}" code="{$country147}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'mongolia'"><code codeSystem="{$oidISOCountry}" code="{$country148}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'montenegro'"><code codeSystem="{$oidISOCountry}" code="{$country149}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'montserrat'"><code codeSystem="{$oidISOCountry}" code="{$country150}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'morocco'"><code codeSystem="{$oidISOCountry}" code="{$country151}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'mozambique'"><code codeSystem="{$oidISOCountry}" code="{$country152}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'myanmar'"><code codeSystem="{$oidISOCountry}" code="{$country153}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'namibia'"><code codeSystem="{$oidISOCountry}" code="{$country154}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'nauru'"><code codeSystem="{$oidISOCountry}" code="{$country155}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'nepal'"><code codeSystem="{$oidISOCountry}" code="{$country156}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'netherlands'"><code codeSystem="{$oidISOCountry}" code="{$country157}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'new caledonia'"><code codeSystem="{$oidISOCountry}" code="{$country158}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'new zealand'"><code codeSystem="{$oidISOCountry}" code="{$country159}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'nicaragua'"><code codeSystem="{$oidISOCountry}" code="{$country160}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'niger'"><code codeSystem="{$oidISOCountry}" code="{$country161}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'nigeria'"><code codeSystem="{$oidISOCountry}" code="{$country162}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'niue'"><code codeSystem="{$oidISOCountry}" code="{$country163}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'norfolk island'"><code codeSystem="{$oidISOCountry}" code="{$country164}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'northern mariana islands'"><code codeSystem="{$oidISOCountry}" code="{$country165}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'norway'"><code codeSystem="{$oidISOCountry}" code="{$country166}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'oman'"><code codeSystem="{$oidISOCountry}" code="{$country167}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'pakistan'"><code codeSystem="{$oidISOCountry}" code="{$country168}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'palau'"><code codeSystem="{$oidISOCountry}" code="{$country169}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'palestine, state of'"><code codeSystem="{$oidISOCountry}" code="{$country170}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'panama'"><code codeSystem="{$oidISOCountry}" code="{$country171}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'papua new guinea'"><code codeSystem="{$oidISOCountry}" code="{$country172}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'paraguay'"><code codeSystem="{$oidISOCountry}" code="{$country173}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'peru'"><code codeSystem="{$oidISOCountry}" code="{$country174}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'philippines'"><code codeSystem="{$oidISOCountry}" code="{$country175}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'pitcairn'"><code codeSystem="{$oidISOCountry}" code="{$country176}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'poland'"><code codeSystem="{$oidISOCountry}" code="{$country177}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'portugal'"><code codeSystem="{$oidISOCountry}" code="{$country178}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'puerto rico'"><code codeSystem="{$oidISOCountry}" code="{$country179}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'qatar'"><code codeSystem="{$oidISOCountry}" code="{$country180}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'reunion !réunion'"><code codeSystem="{$oidISOCountry}" code="{$country181}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'romania'"><code codeSystem="{$oidISOCountry}" code="{$country182}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'russian federation'"><code codeSystem="{$oidISOCountry}" code="{$country183}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'rwanda'"><code codeSystem="{$oidISOCountry}" code="{$country184}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'saint barthelemy !saint barthélemy'"><code codeSystem="{$oidISOCountry}" code="{$country185}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'saint helena, ascension and tristan da cunha'"><code codeSystem="{$oidISOCountry}" code="{$country186}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'saint kitts and nevis'"><code codeSystem="{$oidISOCountry}" code="{$country187}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'saint lucia'"><code codeSystem="{$oidISOCountry}" code="{$country188}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'saint martin (french part)'"><code codeSystem="{$oidISOCountry}" code="{$country189}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'saint pierre and miquelon'"><code codeSystem="{$oidISOCountry}" code="{$country190}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'saint vincent and the grenadines'"><code codeSystem="{$oidISOCountry}" code="{$country191}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'samoa'"><code codeSystem="{$oidISOCountry}" code="{$country192}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'san marino'"><code codeSystem="{$oidISOCountry}" code="{$country193}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'sao tome and principe'"><code codeSystem="{$oidISOCountry}" code="{$country194}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'saudi arabia'"><code codeSystem="{$oidISOCountry}" code="{$country195}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'senegal'"><code codeSystem="{$oidISOCountry}" code="{$country196}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'serbia'"><code codeSystem="{$oidISOCountry}" code="{$country197}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'seychelles'"><code codeSystem="{$oidISOCountry}" code="{$country198}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'sierra leone'"><code codeSystem="{$oidISOCountry}" code="{$country199}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'singapore'"><code codeSystem="{$oidISOCountry}" code="{$country200}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'sint maarten (dutch part)'"><code codeSystem="{$oidISOCountry}" code="{$country201}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'slovakia'"><code codeSystem="{$oidISOCountry}" code="{$country202}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'slovenia'"><code codeSystem="{$oidISOCountry}" code="{$country203}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'solomon islands'"><code codeSystem="{$oidISOCountry}" code="{$country204}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'somalia'"><code codeSystem="{$oidISOCountry}" code="{$country205}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'south africa'"><code codeSystem="{$oidISOCountry}" code="{$country206}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'south georgia and the south sandwich islands'"><code codeSystem="{$oidISOCountry}" code="{$country207}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'south sudan'"><code codeSystem="{$oidISOCountry}" code="{$country208}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'spain'"><code codeSystem="{$oidISOCountry}" code="{$country209}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'sri lanka'"><code codeSystem="{$oidISOCountry}" code="{$country210}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'sudan'"><code codeSystem="{$oidISOCountry}" code="{$country211}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'suriname'"><code codeSystem="{$oidISOCountry}" code="{$country212}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'svalbard and jan mayen'"><code codeSystem="{$oidISOCountry}" code="{$country213}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'swaziland'"><code codeSystem="{$oidISOCountry}" code="{$country214}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'sweden'"><code codeSystem="{$oidISOCountry}" code="{$country215}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'switzerland'"><code codeSystem="{$oidISOCountry}" code="{$country216}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'syrian arab republic'"><code codeSystem="{$oidISOCountry}" code="{$country217}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'taiwan, province of china[a]'"><code codeSystem="{$oidISOCountry}" code="{$country218}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'tajikistan'"><code codeSystem="{$oidISOCountry}" code="{$country219}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'tanzania, united republic of'"><code codeSystem="{$oidISOCountry}" code="{$country220}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'thailand'"><code codeSystem="{$oidISOCountry}" code="{$country221}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'timor-leste'"><code codeSystem="{$oidISOCountry}" code="{$country222}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'togo'"><code codeSystem="{$oidISOCountry}" code="{$country223}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'tokelau'"><code codeSystem="{$oidISOCountry}" code="{$country224}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'tonga'"><code codeSystem="{$oidISOCountry}" code="{$country225}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'trinidad and tobago'"><code codeSystem="{$oidISOCountry}" code="{$country226}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'tunisia'"><code codeSystem="{$oidISOCountry}" code="{$country227}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'turkey'"><code codeSystem="{$oidISOCountry}" code="{$country228}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'turkmenistan'"><code codeSystem="{$oidISOCountry}" code="{$country229}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'turks and caicos islands'"><code codeSystem="{$oidISOCountry}" code="{$country230}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'tuvalu'"><code codeSystem="{$oidISOCountry}" code="{$country231}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'uganda'"><code codeSystem="{$oidISOCountry}" code="{$country232}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'ukraine'"><code codeSystem="{$oidISOCountry}" code="{$country233}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'united arab emirates'"><code codeSystem="{$oidISOCountry}" code="{$country234}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'united kingdom of great britain and northern ireland'"><code codeSystem="{$oidISOCountry}" code="{$country235}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'united kingdom'"><code codeSystem="{$oidISOCountry}" code="{$country235}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'united states of america'"><code codeSystem="{$oidISOCountry}" code="{$country236}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'united states'"><code codeSystem="{$oidISOCountry}" code="{$country236}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'united states minor outlying islands'"><code codeSystem="{$oidISOCountry}" code="{$country237}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'uruguay'"><code codeSystem="{$oidISOCountry}" code="{$country238}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'uzbekistan'"><code codeSystem="{$oidISOCountry}" code="{$country239}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'vanuatu'"><code codeSystem="{$oidISOCountry}" code="{$country240}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'venezuela (bolivarian republic of)'"><code codeSystem="{$oidISOCountry}" code="{$country241}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'viet nam'"><code codeSystem="{$oidISOCountry}" code="{$country242}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'virgin islands (british)'"><code codeSystem="{$oidISOCountry}" code="{$country243}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'virgin islands (u.s.)'"><code codeSystem="{$oidISOCountry}" code="{$country244}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'wallis and futuna'"><code codeSystem="{$oidISOCountry}" code="{$country245}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'western sahara'"><code codeSystem="{$oidISOCountry}" code="{$country246}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'yemen'"><code codeSystem="{$oidISOCountry}" code="{$country247}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'zambia'"><code codeSystem="{$oidISOCountry}" code="{$country248}"/> </xsl:when>
<xsl:when test="sendercountrycode = 'zimbabwe'"><code codeSystem="{$oidISOCountry}" code="{$country249}"/> </xsl:when>

<xsl:when test="sendercountrycode= 'af'"><code codeSystem="{$oidISOCountry}" code="{$country1}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ax'"><code codeSystem="{$oidISOCountry}" code="{$country2}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'al'"><code codeSystem="{$oidISOCountry}" code="{$country3}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'dz'"><code codeSystem="{$oidISOCountry}" code="{$country4}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'as'"><code codeSystem="{$oidISOCountry}" code="{$country5}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ad'"><code codeSystem="{$oidISOCountry}" code="{$country6}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ao'"><code codeSystem="{$oidISOCountry}" code="{$country7}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ai'"><code codeSystem="{$oidISOCountry}" code="{$country8}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'aq'"><code codeSystem="{$oidISOCountry}" code="{$country9}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ag'"><code codeSystem="{$oidISOCountry}" code="{$country10}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ar'"><code codeSystem="{$oidISOCountry}" code="{$country11}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'am'"><code codeSystem="{$oidISOCountry}" code="{$country12}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'aw'"><code codeSystem="{$oidISOCountry}" code="{$country13}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'au'"><code codeSystem="{$oidISOCountry}" code="{$country14}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'at'"><code codeSystem="{$oidISOCountry}" code="{$country15}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'az'"><code codeSystem="{$oidISOCountry}" code="{$country16}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'bs'"><code codeSystem="{$oidISOCountry}" code="{$country17}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'bh'"><code codeSystem="{$oidISOCountry}" code="{$country18}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'bd'"><code codeSystem="{$oidISOCountry}" code="{$country19}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'bb'"><code codeSystem="{$oidISOCountry}" code="{$country20}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'by'"><code codeSystem="{$oidISOCountry}" code="{$country21}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'be'"><code codeSystem="{$oidISOCountry}" code="{$country22}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'bz'"><code codeSystem="{$oidISOCountry}" code="{$country23}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'bj'"><code codeSystem="{$oidISOCountry}" code="{$country24}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'bm'"><code codeSystem="{$oidISOCountry}" code="{$country25}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'bt'"><code codeSystem="{$oidISOCountry}" code="{$country26}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'bo'"><code codeSystem="{$oidISOCountry}" code="{$country27}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'bq'"><code codeSystem="{$oidISOCountry}" code="{$country28}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ba'"><code codeSystem="{$oidISOCountry}" code="{$country29}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'bw'"><code codeSystem="{$oidISOCountry}" code="{$country30}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'bv'"><code codeSystem="{$oidISOCountry}" code="{$country31}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'br'"><code codeSystem="{$oidISOCountry}" code="{$country32}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'io'"><code codeSystem="{$oidISOCountry}" code="{$country33}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'bn'"><code codeSystem="{$oidISOCountry}" code="{$country34}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'bg'"><code codeSystem="{$oidISOCountry}" code="{$country35}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'bf'"><code codeSystem="{$oidISOCountry}" code="{$country36}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'bi'"><code codeSystem="{$oidISOCountry}" code="{$country37}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'cv'"><code codeSystem="{$oidISOCountry}" code="{$country38}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'kh'"><code codeSystem="{$oidISOCountry}" code="{$country39}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'cm'"><code codeSystem="{$oidISOCountry}" code="{$country40}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ca'"><code codeSystem="{$oidISOCountry}" code="{$country41}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ky'"><code codeSystem="{$oidISOCountry}" code="{$country42}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'cf'"><code codeSystem="{$oidISOCountry}" code="{$country43}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'td'"><code codeSystem="{$oidISOCountry}" code="{$country44}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'cl'"><code codeSystem="{$oidISOCountry}" code="{$country45}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'cn'"><code codeSystem="{$oidISOCountry}" code="{$country46}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'cx'"><code codeSystem="{$oidISOCountry}" code="{$country47}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'cc'"><code codeSystem="{$oidISOCountry}" code="{$country48}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'co'"><code codeSystem="{$oidISOCountry}" code="{$country49}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'km'"><code codeSystem="{$oidISOCountry}" code="{$country50}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'cg'"><code codeSystem="{$oidISOCountry}" code="{$country51}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'cd'"><code codeSystem="{$oidISOCountry}" code="{$country52}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ck'"><code codeSystem="{$oidISOCountry}" code="{$country53}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'cr'"><code codeSystem="{$oidISOCountry}" code="{$country54}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ci'"><code codeSystem="{$oidISOCountry}" code="{$country55}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'hr'"><code codeSystem="{$oidISOCountry}" code="{$country56}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'cu'"><code codeSystem="{$oidISOCountry}" code="{$country57}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'cw'"><code codeSystem="{$oidISOCountry}" code="{$country58}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'cy'"><code codeSystem="{$oidISOCountry}" code="{$country59}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'cz'"><code codeSystem="{$oidISOCountry}" code="{$country60}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'dk'"><code codeSystem="{$oidISOCountry}" code="{$country61}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'dj'"><code codeSystem="{$oidISOCountry}" code="{$country62}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'dm'"><code codeSystem="{$oidISOCountry}" code="{$country63}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'do'"><code codeSystem="{$oidISOCountry}" code="{$country64}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ec'"><code codeSystem="{$oidISOCountry}" code="{$country65}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'eg'"><code codeSystem="{$oidISOCountry}" code="{$country66}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'sv'"><code codeSystem="{$oidISOCountry}" code="{$country67}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'gq'"><code codeSystem="{$oidISOCountry}" code="{$country68}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'er'"><code codeSystem="{$oidISOCountry}" code="{$country69}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ee'"><code codeSystem="{$oidISOCountry}" code="{$country70}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'et'"><code codeSystem="{$oidISOCountry}" code="{$country71}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'fk'"><code codeSystem="{$oidISOCountry}" code="{$country72}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'fo'"><code codeSystem="{$oidISOCountry}" code="{$country73}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'fj'"><code codeSystem="{$oidISOCountry}" code="{$country74}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'fi'"><code codeSystem="{$oidISOCountry}" code="{$country75}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'fr'"><code codeSystem="{$oidISOCountry}" code="{$country76}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'gf'"><code codeSystem="{$oidISOCountry}" code="{$country77}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'pf'"><code codeSystem="{$oidISOCountry}" code="{$country78}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'tf'"><code codeSystem="{$oidISOCountry}" code="{$country79}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ga'"><code codeSystem="{$oidISOCountry}" code="{$country80}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'gm'"><code codeSystem="{$oidISOCountry}" code="{$country81}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ge'"><code codeSystem="{$oidISOCountry}" code="{$country82}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'de'"><code codeSystem="{$oidISOCountry}" code="{$country83}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'gh'"><code codeSystem="{$oidISOCountry}" code="{$country84}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'gi'"><code codeSystem="{$oidISOCountry}" code="{$country85}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'gr'"><code codeSystem="{$oidISOCountry}" code="{$country86}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'gl'"><code codeSystem="{$oidISOCountry}" code="{$country87}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'gd'"><code codeSystem="{$oidISOCountry}" code="{$country88}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'gp'"><code codeSystem="{$oidISOCountry}" code="{$country89}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'gu'"><code codeSystem="{$oidISOCountry}" code="{$country90}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'gt'"><code codeSystem="{$oidISOCountry}" code="{$country91}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'gg'"><code codeSystem="{$oidISOCountry}" code="{$country92}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'gn'"><code codeSystem="{$oidISOCountry}" code="{$country93}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'gw'"><code codeSystem="{$oidISOCountry}" code="{$country94}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'gy'"><code codeSystem="{$oidISOCountry}" code="{$country95}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ht'"><code codeSystem="{$oidISOCountry}" code="{$country96}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'hm'"><code codeSystem="{$oidISOCountry}" code="{$country97}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'va'"><code codeSystem="{$oidISOCountry}" code="{$country98}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'hn'"><code codeSystem="{$oidISOCountry}" code="{$country99}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'hk'"><code codeSystem="{$oidISOCountry}" code="{$country100}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'hu'"><code codeSystem="{$oidISOCountry}" code="{$country101}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'is'"><code codeSystem="{$oidISOCountry}" code="{$country102}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'in'"><code codeSystem="{$oidISOCountry}" code="{$country103}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'id'"><code codeSystem="{$oidISOCountry}" code="{$country104}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ir'"><code codeSystem="{$oidISOCountry}" code="{$country105}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'iq'"><code codeSystem="{$oidISOCountry}" code="{$country106}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ie'"><code codeSystem="{$oidISOCountry}" code="{$country107}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'im'"><code codeSystem="{$oidISOCountry}" code="{$country108}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'il'"><code codeSystem="{$oidISOCountry}" code="{$country109}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'it'"><code codeSystem="{$oidISOCountry}" code="{$country110}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'jm'"><code codeSystem="{$oidISOCountry}" code="{$country111}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'jp'"><code codeSystem="{$oidISOCountry}" code="{$country112}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'je'"><code codeSystem="{$oidISOCountry}" code="{$country113}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'jo'"><code codeSystem="{$oidISOCountry}" code="{$country114}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'kz'"><code codeSystem="{$oidISOCountry}" code="{$country115}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ke'"><code codeSystem="{$oidISOCountry}" code="{$country116}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ki'"><code codeSystem="{$oidISOCountry}" code="{$country117}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'kp'"><code codeSystem="{$oidISOCountry}" code="{$country118}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'kr'"><code codeSystem="{$oidISOCountry}" code="{$country119}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'kw'"><code codeSystem="{$oidISOCountry}" code="{$country120}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'kg'"><code codeSystem="{$oidISOCountry}" code="{$country121}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'la'"><code codeSystem="{$oidISOCountry}" code="{$country122}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'lv'"><code codeSystem="{$oidISOCountry}" code="{$country123}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'lb'"><code codeSystem="{$oidISOCountry}" code="{$country124}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ls'"><code codeSystem="{$oidISOCountry}" code="{$country125}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'lr'"><code codeSystem="{$oidISOCountry}" code="{$country126}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ly'"><code codeSystem="{$oidISOCountry}" code="{$country127}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'li'"><code codeSystem="{$oidISOCountry}" code="{$country128}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'lt'"><code codeSystem="{$oidISOCountry}" code="{$country129}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'lu'"><code codeSystem="{$oidISOCountry}" code="{$country130}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'mo'"><code codeSystem="{$oidISOCountry}" code="{$country131}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'mk'"><code codeSystem="{$oidISOCountry}" code="{$country132}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'mg'"><code codeSystem="{$oidISOCountry}" code="{$country133}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'mw'"><code codeSystem="{$oidISOCountry}" code="{$country134}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'my'"><code codeSystem="{$oidISOCountry}" code="{$country135}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'mv'"><code codeSystem="{$oidISOCountry}" code="{$country136}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ml'"><code codeSystem="{$oidISOCountry}" code="{$country137}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'mt'"><code codeSystem="{$oidISOCountry}" code="{$country138}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'mh'"><code codeSystem="{$oidISOCountry}" code="{$country139}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'mq'"><code codeSystem="{$oidISOCountry}" code="{$country140}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'mr'"><code codeSystem="{$oidISOCountry}" code="{$country141}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'mu'"><code codeSystem="{$oidISOCountry}" code="{$country142}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'yt'"><code codeSystem="{$oidISOCountry}" code="{$country143}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'mx'"><code codeSystem="{$oidISOCountry}" code="{$country144}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'fm'"><code codeSystem="{$oidISOCountry}" code="{$country145}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'md'"><code codeSystem="{$oidISOCountry}" code="{$country146}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'mc'"><code codeSystem="{$oidISOCountry}" code="{$country147}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'mn'"><code codeSystem="{$oidISOCountry}" code="{$country148}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'me'"><code codeSystem="{$oidISOCountry}" code="{$country149}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ms'"><code codeSystem="{$oidISOCountry}" code="{$country150}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ma'"><code codeSystem="{$oidISOCountry}" code="{$country151}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'mz'"><code codeSystem="{$oidISOCountry}" code="{$country152}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'mm'"><code codeSystem="{$oidISOCountry}" code="{$country153}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'na'"><code codeSystem="{$oidISOCountry}" code="{$country154}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'nr'"><code codeSystem="{$oidISOCountry}" code="{$country155}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'np'"><code codeSystem="{$oidISOCountry}" code="{$country156}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'nl'"><code codeSystem="{$oidISOCountry}" code="{$country157}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'nc'"><code codeSystem="{$oidISOCountry}" code="{$country158}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'nz'"><code codeSystem="{$oidISOCountry}" code="{$country159}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ni'"><code codeSystem="{$oidISOCountry}" code="{$country160}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ne'"><code codeSystem="{$oidISOCountry}" code="{$country161}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ng'"><code codeSystem="{$oidISOCountry}" code="{$country162}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'nu'"><code codeSystem="{$oidISOCountry}" code="{$country163}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'nf'"><code codeSystem="{$oidISOCountry}" code="{$country164}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'mp'"><code codeSystem="{$oidISOCountry}" code="{$country165}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'no'"><code codeSystem="{$oidISOCountry}" code="{$country166}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'om'"><code codeSystem="{$oidISOCountry}" code="{$country167}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'pk'"><code codeSystem="{$oidISOCountry}" code="{$country168}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'pw'"><code codeSystem="{$oidISOCountry}" code="{$country169}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ps'"><code codeSystem="{$oidISOCountry}" code="{$country170}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'pa'"><code codeSystem="{$oidISOCountry}" code="{$country171}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'pg'"><code codeSystem="{$oidISOCountry}" code="{$country172}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'py'"><code codeSystem="{$oidISOCountry}" code="{$country173}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'pe'"><code codeSystem="{$oidISOCountry}" code="{$country174}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ph'"><code codeSystem="{$oidISOCountry}" code="{$country175}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'pn'"><code codeSystem="{$oidISOCountry}" code="{$country176}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'pl'"><code codeSystem="{$oidISOCountry}" code="{$country177}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'pt'"><code codeSystem="{$oidISOCountry}" code="{$country178}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'pr'"><code codeSystem="{$oidISOCountry}" code="{$country179}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'qa'"><code codeSystem="{$oidISOCountry}" code="{$country180}"/> </xsl:when>
<xsl:when test="sendercountrycode= 're'"><code codeSystem="{$oidISOCountry}" code="{$country181}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ro'"><code codeSystem="{$oidISOCountry}" code="{$country182}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ru'"><code codeSystem="{$oidISOCountry}" code="{$country183}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'rw'"><code codeSystem="{$oidISOCountry}" code="{$country184}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'bl'"><code codeSystem="{$oidISOCountry}" code="{$country185}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'sh'"><code codeSystem="{$oidISOCountry}" code="{$country186}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'kn'"><code codeSystem="{$oidISOCountry}" code="{$country187}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'lc'"><code codeSystem="{$oidISOCountry}" code="{$country188}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'mf'"><code codeSystem="{$oidISOCountry}" code="{$country189}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'pm'"><code codeSystem="{$oidISOCountry}" code="{$country190}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'vc'"><code codeSystem="{$oidISOCountry}" code="{$country191}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ws'"><code codeSystem="{$oidISOCountry}" code="{$country192}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'sm'"><code codeSystem="{$oidISOCountry}" code="{$country193}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'st'"><code codeSystem="{$oidISOCountry}" code="{$country194}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'sa'"><code codeSystem="{$oidISOCountry}" code="{$country195}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'sn'"><code codeSystem="{$oidISOCountry}" code="{$country196}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'rs'"><code codeSystem="{$oidISOCountry}" code="{$country197}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'sc'"><code codeSystem="{$oidISOCountry}" code="{$country198}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'sl'"><code codeSystem="{$oidISOCountry}" code="{$country199}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'sg'"><code codeSystem="{$oidISOCountry}" code="{$country200}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'sx'"><code codeSystem="{$oidISOCountry}" code="{$country201}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'sk'"><code codeSystem="{$oidISOCountry}" code="{$country202}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'si'"><code codeSystem="{$oidISOCountry}" code="{$country203}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'sb'"><code codeSystem="{$oidISOCountry}" code="{$country204}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'so'"><code codeSystem="{$oidISOCountry}" code="{$country205}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'za'"><code codeSystem="{$oidISOCountry}" code="{$country206}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'gs'"><code codeSystem="{$oidISOCountry}" code="{$country207}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ss'"><code codeSystem="{$oidISOCountry}" code="{$country208}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'es'"><code codeSystem="{$oidISOCountry}" code="{$country209}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'lk'"><code codeSystem="{$oidISOCountry}" code="{$country210}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'sd'"><code codeSystem="{$oidISOCountry}" code="{$country211}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'sr'"><code codeSystem="{$oidISOCountry}" code="{$country212}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'sj'"><code codeSystem="{$oidISOCountry}" code="{$country213}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'sz'"><code codeSystem="{$oidISOCountry}" code="{$country214}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'se'"><code codeSystem="{$oidISOCountry}" code="{$country215}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ch'"><code codeSystem="{$oidISOCountry}" code="{$country216}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'sy'"><code codeSystem="{$oidISOCountry}" code="{$country217}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'tw'"><code codeSystem="{$oidISOCountry}" code="{$country218}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'tj'"><code codeSystem="{$oidISOCountry}" code="{$country219}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'tz'"><code codeSystem="{$oidISOCountry}" code="{$country220}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'th'"><code codeSystem="{$oidISOCountry}" code="{$country221}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'tl'"><code codeSystem="{$oidISOCountry}" code="{$country222}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'tg'"><code codeSystem="{$oidISOCountry}" code="{$country223}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'tk'"><code codeSystem="{$oidISOCountry}" code="{$country224}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'to'"><code codeSystem="{$oidISOCountry}" code="{$country225}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'tt'"><code codeSystem="{$oidISOCountry}" code="{$country226}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'tn'"><code codeSystem="{$oidISOCountry}" code="{$country227}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'tr'"><code codeSystem="{$oidISOCountry}" code="{$country228}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'tm'"><code codeSystem="{$oidISOCountry}" code="{$country229}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'tc'"><code codeSystem="{$oidISOCountry}" code="{$country230}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'tv'"><code codeSystem="{$oidISOCountry}" code="{$country231}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ug'"><code codeSystem="{$oidISOCountry}" code="{$country232}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ua'"><code codeSystem="{$oidISOCountry}" code="{$country233}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ae'"><code codeSystem="{$oidISOCountry}" code="{$country234}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'gb'"><code codeSystem="{$oidISOCountry}" code="{$country235}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'us'"><code codeSystem="{$oidISOCountry}" code="{$country236}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'um'"><code codeSystem="{$oidISOCountry}" code="{$country237}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'uy'"><code codeSystem="{$oidISOCountry}" code="{$country238}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'uz'"><code codeSystem="{$oidISOCountry}" code="{$country239}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'vu'"><code codeSystem="{$oidISOCountry}" code="{$country240}"/> </xsl:when>
<xsl:when test="sendercountrycode= 've'"><code codeSystem="{$oidISOCountry}" code="{$country241}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'vn'"><code codeSystem="{$oidISOCountry}" code="{$country242}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'vg'"><code codeSystem="{$oidISOCountry}" code="{$country243}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'vi'"><code codeSystem="{$oidISOCountry}" code="{$country244}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'wf'"><code codeSystem="{$oidISOCountry}" code="{$country245}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'eh'"><code codeSystem="{$oidISOCountry}" code="{$country246}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'ye'"><code codeSystem="{$oidISOCountry}" code="{$country247}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'zm'"><code codeSystem="{$oidISOCountry}" code="{$country248}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'zw'"><code codeSystem="{$oidISOCountry}" code="{$country249}"/> </xsl:when>
<xsl:when test="sendercountrycode= 'eu'"><code codeSystem="{$oidISOCountry}" code="{$country250}"/> </xsl:when>
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
                    <code code="{studyregcountry}" codeSystem="{$oidISOCountry}"/>
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
        <xsl:when test="string-length(parentidentification) > 0 or string-length(parentsexr3) > 0 or string-length(parentbirthdater3) > 0">
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
			<xsl:if test="string-length(parentmedicalhistoryepisode) > 0">
            <xsl:apply-templates select="parentmedicalhistoryepisode" mode="EMA-par-structured-info"/>
			</xsl:if>
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
            <xsl:if test="string-length(parentdrugstartdater3) > 0">
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
					<xsl:if test="string-length(parentmpidversion) > 0 and string-length(parentmpid) > 0"  >
						<xsl:comment>D.10.8.r.2a: MPID Version Date / Number</xsl:comment>
						<xsl:comment>D.10.8.r.2b: Medicinal Product Identifier (MPID)</xsl:comment>
						<code code="{parentmpid}" codeSystem="MPID" codeSystemVersion="{parentmpidversion}"/>
					 </xsl:if>
					<xsl:if test="string-length(parentphpidversion) > 0 and string-length(parentphpid) > 0"  >
						<xsl:comment>D.10.8.r.3a: PhPID Version Date/Number</xsl:comment>
						<xsl:comment>D.10.8.r.3b: Pharmaceutical Product Identifier (PhPID)</xsl:comment>
						<code code="{parentphpid}" codeSystem="PhPID" codeSystemVersion="{parentphpidversion}"/>
					 </xsl:if>		
                    <name>
					  <xsl:comment>D.10.8.r.1: Name of Drug as Reported-</xsl:comment>					  
                      <xsl:value-of select="parentpastdrug"/>
						<xsl:if test="string-length(parentdrginventedname) > 0"  >
							<xsl:comment>D.10.8.r.1.EU.1: Name part - Invented name</xsl:comment>
							<delimiter qualifier="INV"><xsl:value-of select="parentdrginventedname"/></delimiter>
						</xsl:if>						  
						<xsl:if test="string-length(parentdrgscientificname) > 0"  >
							<xsl:comment>D.10.8.r.1.EU.2: Name part - Scientific name</xsl:comment>
							<delimiter qualifier="SCI"><xsl:value-of select="parentdrgscientificname"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(parentdrgtrademarkname) > 0"  >
							<xsl:comment>D.10.8.r.1.EU.3: Name part - Trademark name</xsl:comment>
							<delimiter qualifier="TMK"><xsl:value-of select="parentdrgtrademarkname"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(parentdrgstrengthname) > 0"  >
							<xsl:comment>D.10.8.r.1.EU.4: Name part - Strength name</xsl:comment>
							<delimiter qualifier="STR"><xsl:value-of select="parentdrgstrengthname"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(parentdrgformname) > 0"  >
							<xsl:comment>D.10.8.r.1.EU.5: Name part - Form name</xsl:comment>
							<delimiter qualifier="FRM"><xsl:value-of select="parentdrgformname"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(parentdrgcontainername) > 0"  >
							<xsl:comment>D.10.8.r.1.EU.6: Name part - Container name</xsl:comment>
							<delimiter qualifier="CON"><xsl:value-of select="parentdrgcontainername"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(parentdrgdevicename) > 0"  >
							<xsl:comment>D.10.8.r.1.EU.7: Name part - Device name</xsl:comment>
							<delimiter qualifier="DEV"><xsl:value-of select="parentdrgdevicename"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(parentdrgintendedname) > 0"  >
							<xsl:comment>D.10.8.r.1.EU.8: Name part - Intended use name</xsl:comment>
							<delimiter qualifier="USE"><xsl:value-of select="parentdrgintendedname"/></delimiter>
						</xsl:if>					  
                    </name>
                  </xsl:otherwise>
                </xsl:choose>
				
				<!-- Parent Past Drug Therapy - Substance / Specified Substance Identifier and Strength (repeat as necessary) -->
				<xsl:if test="count(parentdrugsubstanceinfo) > 0">
					<xsl:apply-templates select="parentdrugsubstanceinfo" mode="EMA-parent-past-drug-substance-info"/>
				</xsl:if>
				
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
  <xsl:template match="parentdrugsubstanceinfo" mode="EMA-parent-past-drug-substance-info">
    <xsl:variable name="positionParPastDrugSubInfo">
      <xsl:value-of select="position()"/>
    </xsl:variable>
    <xsl:comment>Parent Past Drug Therapy - Substance / Specified Substance Identifier and Strength - (<xsl:value-of select="$positionParPastDrugSubInfo"/>)</xsl:comment>    
	<ingredient classCode="ACTI">
	<xsl:if test="string-length(parentdrgsubstancestrength) > 0 and string-length(parentdrgsubstancestrengthunit) > 0">
		<quantity>
			<xsl:comment>D.10.8.r.EU.r.3a: Strength (number)</xsl:comment>
			<xsl:comment>D.10.8.r.EU.r.3b: Strength (unit)</xsl:comment>	
			<!--	<xsl:element name="numerator">
					<xsl:attribute name="value">
					    <xsl:value-of select="parentdrgsubstancestrength" />
					</xsl:attribute>  				 
				  	<xsl:attribute name="unit">
					    <xsl:call-template name="getMapping">
							<xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="parentdrgsubstancestrengthunit"/>
					    </xsl:call-template>
					</xsl:attribute> 
			    </xsl:element>  -->
		    <numerator value="{parentdrgsubstancestrength}" unit="{parentdrgsubstancestrengthunit}"/>
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
<value xsi:type="PQ" value="{patientonsetage}" unit="{patientonsetageunit}"/>
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
<value xsi:type="PQ" value="{gestationperiod}" unit="{gestationperiodunit}"/>
<!--<xsl:attribute name="unit"><xsl:call-template name="getMapping"><xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="gestationperiodunit"/></xsl:call-template></xsl:attribute>
</value> -->
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
<!--B.1.6 Last Menstrual Period Date-->
<xsl:if test="string-length(patientlastmenstrualdate) > 0">
<subjectOf2 typeCode="SBJ">
<observation moodCode="EVN" classCode="OBS">
<code code="{$LastMenstrualPeriodDate}" codeSystem="{$oidObservationCode}" displayName="lastMenstrualPeriodDate"/>
<value xsi:type="TS" value="{patientlastmenstrualdate}"/>
<xsl:comment>D.6: Last Menstrual Period Date</xsl:comment>
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
            <id nullFlavor="{$NullFlavourWOSqBrcktPatRec}"/>
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
					<xsl:if test="string-length(patientmpidversion) > 0 and string-length(patientmpid) > 0"  >
						<xsl:comment>D.8.r.2b: Medicinal Product Identifier (MPID)</xsl:comment>
						<xsl:comment>D.8.r.2a: MPID Version Date/Number</xsl:comment>						
						<code code="{patientmpid}" codeSystem="MPID" codeSystemVersion="{patientmpidversion}"/>
					 </xsl:if>
					<xsl:if test="string-length(patientphpidversion) > 0 and string-length(patientphpid) > 0"  >
						<xsl:comment>D.8.r.3a: PhPID Version Date/Number</xsl:comment>
						<xsl:comment>D.8.r.3b: Pharmaceutical Product Identifier (PhPID)</xsl:comment>
						<code code="{patientphpid}" codeSystem="PhPID" codeSystemVersion="{patientphpidversion}"/>
					 </xsl:if>					 
                    <name>
					  <xsl:comment>D.8.r.1: Name of Drug as Reported</xsl:comment>					   
                      <xsl:value-of select="patientdrugname"/>
						<xsl:if test="string-length(patientdruginventedname) > 0"  >
							<xsl:comment>D.8.r.1.EU.1: Name part - Invented name</xsl:comment>
							<delimiter qualifier="INV"><xsl:value-of select="patientdruginventedname"/></delimiter>
						</xsl:if>						  
						<xsl:if test="string-length(patientdrugscientificname) > 0"  >
							<xsl:comment>D.8.r.1.EU.2: Name part - Scientific name</xsl:comment>
							<delimiter qualifier="SCI"><xsl:value-of select="patientdrugscientificname"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(patientdrugtrademarkname) > 0"  >
							<xsl:comment>D.8.r.1.EU.3: Name part - Trademark name</xsl:comment>
							<delimiter qualifier="TMK"><xsl:value-of select="patientdrugtrademarkname"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(patientdrugstrengthname) > 0"  >
							<xsl:comment>D.8.r.1.EU.4: Name part - Strength name</xsl:comment>
							<delimiter qualifier="STR"><xsl:value-of select="patientdrugstrengthname"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(patientdrugformname) > 0"  >
							<xsl:comment>D.8.r.1.EU.5: Name part - Form name</xsl:comment>
							<delimiter qualifier="FRM"><xsl:value-of select="patientdrugformname"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(patientdrugcontainername) > 0"  >
							<xsl:comment>D.8.r.1.EU.6: Name part - Container name</xsl:comment>
							<delimiter qualifier="CON"><xsl:value-of select="patientdrugcontainername"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(patientdrugdevicename) > 0"  >
							<xsl:comment>D.8.r.1.EU.7: Name part - Device name</xsl:comment>
							<delimiter qualifier="DEV"><xsl:value-of select="patientdrugdevicename"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(patientdrugintendedname) > 0"  >
							<xsl:comment>D.8.r.1.EU.8: Name part - Intended use name</xsl:comment>
							<delimiter qualifier="USE"><xsl:value-of select="patientdrugintendedname"/></delimiter>
						</xsl:if>						
                    </name>
                  </xsl:otherwise>
                </xsl:choose>
				
				<!-- Patient Past Drug Therapy - Substance / Specified Substance Identifier and Strength (repeat as necessary) -->
				<xsl:if test="count(patientdrugsubstanceinfo) > 0">
					<xsl:apply-templates select="patientdrugsubstanceinfo" mode="EMA-patient-past-drug-substance-info"/>
				</xsl:if>					
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
				<!-- <xsl:element name="numerator">
					<xsl:attribute name="value">
					    <xsl:value-of select="patientdrgsubstancestrength" />
					</xsl:attribute>  				 
				  	<xsl:attribute name="unit">
					    <xsl:call-template name="getMapping">
							<xsl:with-param name="type">UCUM</xsl:with-param><xsl:with-param name="code" select="patientdrgsubstancestrengthunit"/>
					    </xsl:call-template>
					</xsl:attribute> 
			    </xsl:element>  -->
			 <numerator value="{patientdrgsubstancestrength}" unit="{patientdrgsubstancestrengthunit}"/>
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
                  <width value="{reactionduration}" unit="{reactiondurationunit}"/> 
				 <!-- <width value="{reactionduration}">
					<xsl:attribute name="unit">	
					<xsl:call-template name="getMapping">
					<xsl:with-param name="type">UCUM</xsl:with-param>
					<xsl:with-param name="code" select="reactiondurationunit"/>
					</xsl:call-template>
					</xsl:attribute>
				  </width> -->
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
                    <width value="{reactionduration}" unit="{reactiondurationunit}" />
					<!-- <xsl:attribute name="unit">	
					<xsl:call-template name="getMapping">
					<xsl:with-param name="type">UCUM</xsl:with-param>
					<xsl:with-param name="code" select="reactiondurationunit"/>
					</xsl:call-template>
					</xsl:attribute>  
				    </width>	-->				
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
                <code code="{reactionoccurcountry}" codeSystem="{$oidISOCountry}"/>
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



<xsl:template match="reactionold">
<subjectOf2 typeCode="SBJ">
<observation moodCode="EVN" classCode="OBS">
<!--internal reaction id-->
<id root="RID{position()}"/>
<code code="{$Reaction}" codeSystem="{$oidObservationCode}" displayName="Event Startdate Event Enddate and Event Continuation details"/>
<!--B.2.i.3, 4, 5 Start, End and Duration of Reaction/Event-->
<!--<xsl:if test="string-length(reactionstartdate) > 0 or string-length(reactionenddate) > 0 or string-length(reactionduration) > 0">-->
<xsl:choose>
<xsl:when test="string-length(reactionstartdate) and string-length(reactionenddate) and string-length(reactionduration) > 0">
<effectiveTime xsi:type="SXPR_TS">

<comp xsi:type="IVL_TS">
<xsl:if test="string-length(reactionstartdate) > 0">
<low value="{reactionstartdate}"/>
<xsl:comment>E.i.4: Date of Start of Reaction / Event</xsl:comment>
</xsl:if>
</comp>	


<comp xsi:type="IVL_TS">
<xsl:if test="string-length(reactionenddate) > 0">
<high value="{reactionenddate}"/>
<xsl:comment>E.i.5: Date of End of Reaction / Event</xsl:comment>
</xsl:if>
</comp>

<comp xsi:type="IVL_TS" operator="A">
<xsl:if test="string-length(reactionduration) > 0">
<width value="{reactionduration}" unit="{reactiondurationunit}"/>
<!-- <xsl:attribute name="unit">	
<xsl:call-template name="getMapping">
<xsl:with-param name="type">UCUM</xsl:with-param>
<xsl:with-param name="code" select="reactiondurationunit"/>
</xsl:call-template>
</xsl:attribute>
</width> -->
</xsl:if>
<xsl:comment>E.i.6a: Duration of Reaction / Event (number)</xsl:comment>
<xsl:comment>E.i.6b: Duration of Reaction / Event (unit)</xsl:comment>
</comp>
</effectiveTime>
</xsl:when>
<xsl:otherwise>
<effectiveTime xsi:type="IVL_TS">
<xsl:if test="string-length(reactionstartdate) > 0">
<low value="{reactionstartdate}"/>
<xsl:comment>E.i.4: Date of Start of Reaction / Event</xsl:comment>
</xsl:if>
<xsl:if test="string-length(reactionenddate) > 0">
<high value="{reactionenddate}"/>
<xsl:comment>E.i.5: Date of End of Reaction / Event</xsl:comment>
</xsl:if>
</effectiveTime>
</xsl:otherwise>
</xsl:choose>
<xsl:if test="string-length(reactionoccurcountry)>0">
<location typeCode="LOC">
<locatedEntity classCode="LOCE">
<locatedPlace classCode="COUNTRY" determinerCode="INSTANCE">
<xsl:choose>
<xsl:when test="reactionoccurcountry = 'AFGHANISTAN'"><code code="{$country1}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ALAND ISLANDS !ÅLAND ISLANDS'"><code code="{$country2}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ALBANIA'"><code code="{$country3}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ALGERIA'"><code code="{$country4}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'AMERICAN SAMOA'"><code code="{$country5}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ANDORRA'"><code code="{$country6}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ANGOLA'"><code code="{$country7}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ANGUILLA'"><code code="{$country8}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ANTARCTICA'"><code code="{$country9}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ANTIGUA AND BARBUDA'"><code code="{$country10}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ARGENTINA'"><code code="{$country11}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ARMENIA'"><code code="{$country12}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ARUBA'"><code code="{$country13}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'AUSTRALIA'"><code code="{$country14}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'AUSTRIA'"><code code="{$country15}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'AZERBAIJAN'"><code code="{$country16}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BAHAMAS'"><code code="{$country17}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BAHRAIN'"><code code="{$country18}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BANGLADESH'"><code code="{$country19}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BARBADOS'"><code code="{$country20}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BELARUS'"><code code="{$country21}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BELGIUM'"><code code="{$country22}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BELIZE'"><code code="{$country23}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BENIN'"><code code="{$country24}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BERMUDA'"><code code="{$country25}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BHUTAN'"><code code="{$country26}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BOLIVIA (PLURINATIONAL STATE OF)'"><code code="{$country27}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BONAIRE, SINT EUSTATIUS AND SABA'"><code code="{$country28}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BOSNIA AND HERZEGOVINA'"><code code="{$country29}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BOTSWANA'"><code code="{$country30}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BOUVET ISLAND'"><code code="{$country31}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BRAZIL'"><code code="{$country32}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BRITISH INDIAN OCEAN TERRITORY'"><code code="{$country33}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BRUNEI DARUSSALAM'"><code code="{$country34}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BULGARIA'"><code code="{$country35}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BURKINA FASO'"><code code="{$country36}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'BURUNDI'"><code code="{$country37}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'CABO VERDE'"><code code="{$country38}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'CAMBODIA'"><code code="{$country39}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'CAMEROON'"><code code="{$country40}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'CANADA'"><code code="{$country41}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'CAYMAN ISLANDS'"><code code="{$country42}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'CENTRAL AFRICAN REPUBLIC'"><code code="{$country43}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'CHAD'"><code code="{$country44}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'CHILE'"><code code="{$country45}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'CHINA'"><code code="{$country46}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'CHRISTMAS ISLAND'"><code code="{$country47}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'COCOS (KEELING) ISLANDS'"><code code="{$country48}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'COLOMBIA'"><code code="{$country49}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'COMOROS'"><code code="{$country50}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'CONGO'"><code code="{$country51}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'CONGO (DEMOCRATIC REPUBLIC OF THE)'"><code code="{$country52}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'COOK ISLANDS'"><code code="{$country53}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'COSTA RICA'"><code code="{$country54}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'EUROPEAN UNION'"><code code="{$country250}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'CROATIA'"><code code="{$country56}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'CUBA'"><code code="{$country57}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'CURACAO !CURAÇAO'"><code code="{$country58}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'CYPRUS'"><code code="{$country59}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'CZECH REPUBLIC'"><code code="{$country60}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'DENMARK'"><code code="{$country61}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'DJIBOUTI'"><code code="{$country62}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'DOMINICA'"><code code="{$country63}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'DOMINICAN REPUBLIC'"><code code="{$country64}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ECUADOR'"><code code="{$country65}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'EGYPT'"><code code="{$country66}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'EL SALVADOR'"><code code="{$country67}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'EQUATORIAL GUINEA'"><code code="{$country68}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ERITREA'"><code code="{$country69}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ESTONIA'"><code code="{$country70}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ETHIOPIA'"><code code="{$country71}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'FALKLAND ISLANDS (MALVINAS)'"><code code="{$country72}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'FAROE ISLANDS'"><code code="{$country73}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'FIJI'"><code code="{$country74}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'FINLAND'"><code code="{$country75}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'FRANCE'"><code code="{$country76}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'FRENCH GUIANA'"><code code="{$country77}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'FRENCH POLYNESIA'"><code code="{$country78}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'FRENCH SOUTHERN TERRITORIES'"><code code="{$country79}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'GABON'"><code code="{$country80}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'GAMBIA'"><code code="{$country81}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'GEORGIA'"><code code="{$country82}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'GERMANY'"><code code="{$country83}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'GHANA'"><code code="{$country84}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'GIBRALTAR'"><code code="{$country85}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'GREECE'"><code code="{$country86}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'GREENLAND'"><code code="{$country87}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'GRENADA'"><code code="{$country88}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'GUADELOUPE'"><code code="{$country89}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'GUAM'"><code code="{$country90}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'GUATEMALA'"><code code="{$country91}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'GUERNSEY'"><code code="{$country92}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'GUINEA'"><code code="{$country93}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'GUINEA-BISSAU'"><code code="{$country94}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'GUYANA'"><code code="{$country95}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'HAITI'"><code code="{$country96}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'HEARD ISLAND AND MCDONALD ISLANDS'"><code code="{$country97}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'HOLY SEE'"><code code="{$country98}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'HONDURAS'"><code code="{$country99}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'HONG KONG'"><code code="{$country100}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'HUNGARY'"><code code="{$country101}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ICELAND'"><code code="{$country102}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'INDIA'"><code code="{$country103}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'INDONESIA'"><code code="{$country104}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'IRAN (ISLAMIC REPUBLIC OF)'"><code code="{$country105}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'IRAQ'"><code code="{$country106}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'IRELAND'"><code code="{$country107}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ISLE OF MAN'"><code code="{$country108}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ISRAEL'"><code code="{$country109}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ITALY'"><code code="{$country110}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'JAMAICA'"><code code="{$country111}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'JAPAN'"><code code="{$country112}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'JERSEY'"><code code="{$country113}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'JORDAN'"><code code="{$country114}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'KAZAKHSTAN'"><code code="{$country115}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'KENYA'"><code code="{$country116}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'KIRIBATI'"><code code="{$country117}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'KUWAIT'"><code code="{$country120}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'KYRGYZSTAN'"><code code="{$country121}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'LATVIA'"><code code="{$country123}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'LEBANON'"><code code="{$country124}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'LESOTHO'"><code code="{$country125}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'LIBERIA'"><code code="{$country126}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'LIBYA'"><code code="{$country127}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'LIECHTENSTEIN'"><code code="{$country128}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'LITHUANIA'"><code code="{$country129}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'LUXEMBOURG'"><code code="{$country130}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MACAO'"><code code="{$country131}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MACEDONIA (THE FORMER YUGOSLAV REPUBLIC OF)'"><code code="{$country132}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MADAGASCAR'"><code code="{$country133}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MALAWI'"><code code="{$country134}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MALAYSIA'"><code code="{$country135}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MALDIVES'"><code code="{$country136}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MALI'"><code code="{$country137}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MALTA'"><code code="{$country138}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MARSHALL ISLANDS'"><code code="{$country139}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MARTINIQUE'"><code code="{$country140}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MAURITANIA'"><code code="{$country141}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MAURITIUS'"><code code="{$country142}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MAYOTTE'"><code code="{$country143}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MEXICO'"><code code="{$country144}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MICRONESIA (FEDERATED STATES OF)'"><code code="{$country145}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MOLDOVA (REPUBLIC OF)'"><code code="{$country146}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MONACO'"><code code="{$country147}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MONGOLIA'"><code code="{$country148}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MONTENEGRO'"><code code="{$country149}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MONTSERRAT'"><code code="{$country150}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MOROCCO'"><code code="{$country151}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MOZAMBIQUE'"><code code="{$country152}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'MYANMAR'"><code code="{$country153}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'NAMIBIA'"><code code="{$country154}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'NAURU'"><code code="{$country155}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'NEPAL'"><code code="{$country156}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'NETHERLANDS'"><code code="{$country157}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'NEW CALEDONIA'"><code code="{$country158}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'NEW ZEALAND'"><code code="{$country159}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'NICARAGUA'"><code code="{$country160}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'NIGER'"><code code="{$country161}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'NIGERIA'"><code code="{$country162}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'NIUE'"><code code="{$country163}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'NORFOLK ISLAND'"><code code="{$country164}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'NORTHERN MARIANA ISLANDS'"><code code="{$country165}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'NORWAY'"><code code="{$country166}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'OMAN'"><code code="{$country167}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'PAKISTAN'"><code code="{$country168}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'PALAU'"><code code="{$country169}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'PALESTINE, STATE OF'"><code code="{$country170}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'PANAMA'"><code code="{$country171}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'PAPUA NEW GUINEA'"><code code="{$country172}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'PARAGUAY'"><code code="{$country173}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'PERU'"><code code="{$country174}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'PHILIPPINES'"><code code="{$country175}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'PITCAIRN'"><code code="{$country176}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'POLAND'"><code code="{$country177}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'PORTUGAL'"><code code="{$country178}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'PUERTO RICO'"><code code="{$country179}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'QATAR'"><code code="{$country180}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'REUNION !RÉUNION'"><code code="{$country181}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ROMANIA'"><code code="{$country182}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'RUSSIAN FEDERATION'"><code code="{$country183}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'RWANDA'"><code code="{$country184}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SAINT BARTHELEMY !SAINT BARTHÉLEMY'"><code code="{$country185}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SAINT HELENA, ASCENSION AND TRISTAN DA CUNHA'"><code code="{$country186}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SAINT KITTS AND NEVIS'"><code code="{$country187}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SAINT LUCIA'"><code code="{$country188}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SAINT MARTIN (FRENCH PART)'"><code code="{$country189}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SAINT PIERRE AND MIQUELON'"><code code="{$country190}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SAINT VINCENT AND THE GRENADINES'"><code code="{$country191}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SAMOA'"><code code="{$country192}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SAN MARINO'"><code code="{$country193}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SAO TOME AND PRINCIPE'"><code code="{$country194}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SAUDI ARABIA'"><code code="{$country195}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SENEGAL'"><code code="{$country196}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SERBIA'"><code code="{$country197}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SEYCHELLES'"><code code="{$country198}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SIERRA LEONE'"><code code="{$country199}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SINGAPORE'"><code code="{$country200}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SINT MAARTEN (DUTCH PART)'"><code code="{$country201}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SLOVAKIA'"><code code="{$country202}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SLOVENIA'"><code code="{$country203}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SOLOMON ISLANDS'"><code code="{$country204}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SOMALIA'"><code code="{$country205}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SOUTH AFRICA'"><code code="{$country206}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS'"><code code="{$country207}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SOUTH SUDAN'"><code code="{$country208}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SPAIN'"><code code="{$country209}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SRI LANKA'"><code code="{$country210}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SUDAN'"><code code="{$country211}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SURINAME'"><code code="{$country212}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SVALBARD AND JAN MAYEN'"><code code="{$country213}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SWAZILAND'"><code code="{$country214}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SWEDEN'"><code code="{$country215}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SWITZERLAND'"><code code="{$country216}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'SYRIAN ARAB REPUBLIC'"><code code="{$country217}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'TAIWAN, PROVINCE OF CHINA[A]'"><code code="{$country218}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'TAJIKISTAN'"><code code="{$country219}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'TANZANIA, UNITED REPUBLIC OF'"><code code="{$country220}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'THAILAND'"><code code="{$country221}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'TIMOR-LESTE'"><code code="{$country222}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'TOGO'"><code code="{$country223}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'TOKELAU'"><code code="{$country224}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'TONGA'"><code code="{$country225}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'TRINIDAD AND TOBAGO'"><code code="{$country226}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'TUNISIA'"><code code="{$country227}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'TURKEY'"><code code="{$country228}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'TURKMENISTAN'"><code code="{$country229}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'TURKS AND CAICOS ISLANDS'"><code code="{$country230}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'TUVALU'"><code code="{$country231}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'UGANDA'"><code code="{$country232}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'UKRAINE'"><code code="{$country233}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'UNITED ARAB EMIRATES'"><code code="{$country234}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'UNITED KINGDOM OF GREAT BRITAIN AND NORTHERN IRELAND'"><code code="{$country235}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'UNITED KINGDOM'"><code code="{$country235}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'UNITED STATES OF AMERICA'"><code code="{$country236}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'UNITED STATES'"><code code="{$country236}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'UNITED STATES MINOR OUTLYING ISLANDS'"><code code="{$country237}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'URUGUAY'"><code code="{$country238}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'UZBEKISTAN'"><code code="{$country239}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'VANUATU'"><code code="{$country240}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'VENEZUELA (BOLIVARIAN REPUBLIC OF)'"><code code="{$country241}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'VIET NAM'"><code code="{$country242}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'VIRGIN ISLANDS (BRITISH)'"><code code="{$country243}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'VIRGIN ISLANDS (U.S.)'"><code code="{$country244}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'WALLIS AND FUTUNA'"><code code="{$country245}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'WESTERN SAHARA'"><code code="{$country246}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'YEMEN'"><code code="{$country247}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ZAMBIA'"><code code="{$country248}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ZIMBABWE'"><code code="{$country249}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>


<xsl:when test="reactionoccurcountry = 'Afghanistan'"><code code="{$country1}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Aland Islands !Åland Islands'"><code code="{$country2}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Albania'"><code code="{$country3}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Algeria'"><code code="{$country4}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'American Samoa'"><code code="{$country5}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Andorra'"><code code="{$country6}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Angola'"><code code="{$country7}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Anguilla'"><code code="{$country8}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Antarctica'"><code code="{$country9}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Antigua and Barbuda'"><code code="{$country10}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Argentina'"><code code="{$country11}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Armenia'"><code code="{$country12}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Aruba'"><code code="{$country13}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Australia'"><code code="{$country14}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Austria'"><code code="{$country15}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Azerbaijan'"><code code="{$country16}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Bahamas'"><code code="{$country17}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Bahrain'"><code code="{$country18}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Bangladesh'"><code code="{$country19}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Barbados'"><code code="{$country20}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Belarus'"><code code="{$country21}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Belgium'"><code code="{$country22}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Belize'"><code code="{$country23}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Benin'"><code code="{$country24}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Bermuda'"><code code="{$country25}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Bhutan'"><code code="{$country26}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Bolivia (Plurinational State of)'"><code code="{$country27}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Bonaire, Sint Eustatius and Saba'"><code code="{$country28}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Bosnia and Herzegovina'"><code code="{$country29}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Botswana'"><code code="{$country30}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Bouvet Island'"><code code="{$country31}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Brazil'"><code code="{$country32}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'British Indian Ocean Territory'"><code code="{$country33}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Brunei Darussalam'"><code code="{$country34}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Bulgaria'"><code code="{$country35}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Burkina Faso'"><code code="{$country36}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Burundi'"><code code="{$country37}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Cabo Verde'"><code code="{$country38}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Cambodia'"><code code="{$country39}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Cameroon'"><code code="{$country40}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Canada'"><code code="{$country41}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Cayman Islands'"><code code="{$country42}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Central African Republic'"><code code="{$country43}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Chad'"><code code="{$country44}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Chile'"><code code="{$country45}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'China'"><code code="{$country46}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Christmas Island'"><code code="{$country47}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Cocos (Keeling) Islands'"><code code="{$country48}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Colombia'"><code code="{$country49}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Comoros'"><code code="{$country50}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Congo'"><code code="{$country51}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Congo (Democratic Republic of the)'"><code code="{$country52}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Cook Islands'"><code code="{$country53}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Costa Rica'"><code code="{$country54}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'European Union'"><code code="{$country250}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Croatia'"><code code="{$country56}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Cuba'"><code code="{$country57}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Curacao !Curaçao'"><code code="{$country58}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Cyprus'"><code code="{$country59}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Czech Republic'"><code code="{$country60}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Denmark'"><code code="{$country61}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Djibouti'"><code code="{$country62}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Dominica'"><code code="{$country63}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Dominican Republic'"><code code="{$country64}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Ecuador'"><code code="{$country65}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Egypt'"><code code="{$country66}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'El Salvador'"><code code="{$country67}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Equatorial Guinea'"><code code="{$country68}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Eritrea'"><code code="{$country69}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Estonia'"><code code="{$country70}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Ethiopia'"><code code="{$country71}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Falkland Islands (Malvinas)'"><code code="{$country72}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Faroe Islands'"><code code="{$country73}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Fiji'"><code code="{$country74}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Finland'"><code code="{$country75}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'France'"><code code="{$country76}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'French Guiana'"><code code="{$country77}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'French Polynesia'"><code code="{$country78}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'French Southern Territories'"><code code="{$country79}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Gabon'"><code code="{$country80}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Gambia'"><code code="{$country81}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Georgia'"><code code="{$country82}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Germany'"><code code="{$country83}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Ghana'"><code code="{$country84}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Gibraltar'"><code code="{$country85}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Greece'"><code code="{$country86}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Greenland'"><code code="{$country87}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Grenada'"><code code="{$country88}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Guadeloupe'"><code code="{$country89}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Guam'"><code code="{$country90}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Guatemala'"><code code="{$country91}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Guernsey'"><code code="{$country92}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Guinea'"><code code="{$country93}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Guinea-Bissau'"><code code="{$country94}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Guyana'"><code code="{$country95}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Haiti'"><code code="{$country96}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Heard Island and McDonald Islands'"><code code="{$country97}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Holy See'"><code code="{$country98}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Honduras'"><code code="{$country99}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Hong Kong'"><code code="{$country100}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Hungary'"><code code="{$country101}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Iceland'"><code code="{$country102}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'India'"><code code="{$country103}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Indonesia'"><code code="{$country104}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Iran (Islamic Republic of)'"><code code="{$country105}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Iraq'"><code code="{$country106}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Ireland'"><code code="{$country107}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Isle of Man'"><code code="{$country108}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Israel'"><code code="{$country109}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Italy'"><code code="{$country110}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Jamaica'"><code code="{$country111}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Japan'"><code code="{$country112}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Jersey'"><code code="{$country113}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Jordan'"><code code="{$country114}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Kazakhstan'"><code code="{$country115}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Kenya'"><code code="{$country116}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Kiribati'"><code code="{$country117}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Kuwait'"><code code="{$country120}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Kyrgyzstan'"><code code="{$country121}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Latvia'"><code code="{$country123}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Lebanon'"><code code="{$country124}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Lesotho'"><code code="{$country125}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Liberia'"><code code="{$country126}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Libya'"><code code="{$country127}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Liechtenstein'"><code code="{$country128}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Lithuania'"><code code="{$country129}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Luxembourg'"><code code="{$country130}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Macao'"><code code="{$country131}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Macedonia (the former Yugoslav Republic of)'"><code code="{$country132}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Madagascar'"><code code="{$country133}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Malawi'"><code code="{$country134}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Malaysia'"><code code="{$country135}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Maldives'"><code code="{$country136}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Mali'"><code code="{$country137}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Malta'"><code code="{$country138}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Marshall Islands'"><code code="{$country139}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Martinique'"><code code="{$country140}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Mauritania'"><code code="{$country141}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Mauritius'"><code code="{$country142}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Mayotte'"><code code="{$country143}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Mexico'"><code code="{$country144}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Micronesia (Federated States of)'"><code code="{$country145}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Moldova (Republic of)'"><code code="{$country146}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Monaco'"><code code="{$country147}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Mongolia'"><code code="{$country148}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Montenegro'"><code code="{$country149}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Montserrat'"><code code="{$country150}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Morocco'"><code code="{$country151}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Mozambique'"><code code="{$country152}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Myanmar'"><code code="{$country153}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Namibia'"><code code="{$country154}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Nauru'"><code code="{$country155}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Nepal'"><code code="{$country156}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Netherlands'"><code code="{$country157}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'New Caledonia'"><code code="{$country158}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'New Zealand'"><code code="{$country159}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Nicaragua'"><code code="{$country160}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Niger'"><code code="{$country161}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Nigeria'"><code code="{$country162}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Niue'"><code code="{$country163}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Norfolk Island'"><code code="{$country164}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Northern Mariana Islands'"><code code="{$country165}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Norway'"><code code="{$country166}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Oman'"><code code="{$country167}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Pakistan'"><code code="{$country168}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Palau'"><code code="{$country169}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Palestine, State of'"><code code="{$country170}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Panama'"><code code="{$country171}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Papua New Guinea'"><code code="{$country172}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Paraguay'"><code code="{$country173}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Peru'"><code code="{$country174}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Philippines'"><code code="{$country175}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Pitcairn'"><code code="{$country176}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Poland'"><code code="{$country177}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Portugal'"><code code="{$country178}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Puerto Rico'"><code code="{$country179}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Qatar'"><code code="{$country180}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Reunion !Réunion'"><code code="{$country181}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Romania'"><code code="{$country182}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Russian Federation'"><code code="{$country183}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Rwanda'"><code code="{$country184}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Saint Barthelemy !Saint Barthélemy'"><code code="{$country185}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Saint Helena, Ascension and Tristan da Cunha'"><code code="{$country186}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Saint Kitts and Nevis'"><code code="{$country187}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Saint Lucia'"><code code="{$country188}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Saint Martin (French part)'"><code code="{$country189}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Saint Pierre and Miquelon'"><code code="{$country190}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Saint Vincent and the Grenadines'"><code code="{$country191}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Samoa'"><code code="{$country192}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'San Marino'"><code code="{$country193}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Sao Tome and Principe'"><code code="{$country194}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Saudi Arabia'"><code code="{$country195}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Senegal'"><code code="{$country196}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Serbia'"><code code="{$country197}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Seychelles'"><code code="{$country198}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Sierra Leone'"><code code="{$country199}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Singapore'"><code code="{$country200}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Sint Maarten (Dutch part)'"><code code="{$country201}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Slovakia'"><code code="{$country202}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Slovenia'"><code code="{$country203}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Solomon Islands'"><code code="{$country204}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Somalia'"><code code="{$country205}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'South Africa'"><code code="{$country206}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'South Georgia and the South Sandwich Islands'"><code code="{$country207}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'South Sudan'"><code code="{$country208}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Spain'"><code code="{$country209}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Sri Lanka'"><code code="{$country210}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Sudan'"><code code="{$country211}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Suriname'"><code code="{$country212}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Svalbard and Jan Mayen'"><code code="{$country213}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Swaziland'"><code code="{$country214}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Sweden'"><code code="{$country215}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Switzerland'"><code code="{$country216}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Syrian Arab Republic'"><code code="{$country217}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Taiwan, Province of China[a]'"><code code="{$country218}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Tajikistan'"><code code="{$country219}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Tanzania, United Republic of'"><code code="{$country220}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Thailand'"><code code="{$country221}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Timor-Leste'"><code code="{$country222}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Togo'"><code code="{$country223}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Tokelau'"><code code="{$country224}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Tonga'"><code code="{$country225}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Trinidad and Tobago'"><code code="{$country226}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Tunisia'"><code code="{$country227}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Turkey'"><code code="{$country228}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Turkmenistan'"><code code="{$country229}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Turks and Caicos Islands'"><code code="{$country230}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Tuvalu'"><code code="{$country231}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Uganda'"><code code="{$country232}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Ukraine'"><code code="{$country233}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'United Arab Emirates'"><code code="{$country234}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'United Kingdom of Great Britain and Northern Ireland'"><code code="{$country235}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'United Kingdom'"><code code="{$country235}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'United States of America'"><code code="{$country236}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'United States'"><code code="{$country236}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'United States Minor Outlying Islands'"><code code="{$country237}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Uruguay'"><code code="{$country238}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Uzbekistan'"><code code="{$country239}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Vanuatu'"><code code="{$country240}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Venezuela (Bolivarian Republic of)'"><code code="{$country241}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Viet Nam'"><code code="{$country242}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Virgin Islands (British)'"><code code="{$country243}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Virgin Islands (U.S.)'"><code code="{$country244}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Wallis and Futuna'"><code code="{$country245}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Western Sahara'"><code code="{$country246}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Yemen'"><code code="{$country247}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Zambia'"><code code="{$country248}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'Zimbabwe'"><code code="{$country249}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>


<xsl:when test="reactionoccurcountry = 'afghanistan'"><code code="{$country1}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'aland islands !åland islands'"><code code="{$country2}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'albania'"><code code="{$country3}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'algeria'"><code code="{$country4}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'american samoa'"><code code="{$country5}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'andorra'"><code code="{$country6}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'angola'"><code code="{$country7}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'anguilla'"><code code="{$country8}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'antarctica'"><code code="{$country9}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'antigua and barbuda'"><code code="{$country10}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'argentina'"><code code="{$country11}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'armenia'"><code code="{$country12}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'aruba'"><code code="{$country13}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'australia'"><code code="{$country14}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'austria'"><code code="{$country15}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'azerbaijan'"><code code="{$country16}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'bahamas'"><code code="{$country17}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'bahrain'"><code code="{$country18}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'bangladesh'"><code code="{$country19}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'barbados'"><code code="{$country20}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'belarus'"><code code="{$country21}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'belgium'"><code code="{$country22}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'belize'"><code code="{$country23}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'benin'"><code code="{$country24}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'bermuda'"><code code="{$country25}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'bhutan'"><code code="{$country26}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'bolivia (plurinational state of)'"><code code="{$country27}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'bonaire, sint eustatius and saba'"><code code="{$country28}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'bosnia and herzegovina'"><code code="{$country29}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'botswana'"><code code="{$country30}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'bouvet island'"><code code="{$country31}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'brazil'"><code code="{$country32}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'british indian ocean territory'"><code code="{$country33}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'brunei darussalam'"><code code="{$country34}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'bulgaria'"><code code="{$country35}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'burkina faso'"><code code="{$country36}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'burundi'"><code code="{$country37}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'cabo verde'"><code code="{$country38}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'cambodia'"><code code="{$country39}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'cameroon'"><code code="{$country40}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'canada'"><code code="{$country41}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'cayman islands'"><code code="{$country42}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'central african republic'"><code code="{$country43}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'chad'"><code code="{$country44}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'chile'"><code code="{$country45}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'china'"><code code="{$country46}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'christmas island'"><code code="{$country47}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'cocos (keeling) islands'"><code code="{$country48}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'colombia'"><code code="{$country49}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'comoros'"><code code="{$country50}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'congo'"><code code="{$country51}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'congo (democratic republic of the)'"><code code="{$country52}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'cook islands'"><code code="{$country53}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'costa rica'"><code code="{$country54}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'european union'"><code code="{$country250}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'croatia'"><code code="{$country56}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'cuba'"><code code="{$country57}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'curacao !curaçao'"><code code="{$country58}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'cyprus'"><code code="{$country59}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'czech republic'"><code code="{$country60}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'denmark'"><code code="{$country61}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'djibouti'"><code code="{$country62}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'dominica'"><code code="{$country63}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'dominican republic'"><code code="{$country64}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ecuador'"><code code="{$country65}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'egypt'"><code code="{$country66}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'el salvador'"><code code="{$country67}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'equatorial guinea'"><code code="{$country68}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'eritrea'"><code code="{$country69}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'estonia'"><code code="{$country70}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ethiopia'"><code code="{$country71}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'falkland islands (malvinas)'"><code code="{$country72}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'faroe islands'"><code code="{$country73}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'fiji'"><code code="{$country74}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'finland'"><code code="{$country75}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'france'"><code code="{$country76}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'french guiana'"><code code="{$country77}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'french polynesia'"><code code="{$country78}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'french southern territories'"><code code="{$country79}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'gabon'"><code code="{$country80}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'gambia'"><code code="{$country81}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'georgia'"><code code="{$country82}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'germany'"><code code="{$country83}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ghana'"><code code="{$country84}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'gibraltar'"><code code="{$country85}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'greece'"><code code="{$country86}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'greenland'"><code code="{$country87}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'grenada'"><code code="{$country88}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'guadeloupe'"><code code="{$country89}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'guam'"><code code="{$country90}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'guatemala'"><code code="{$country91}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'guernsey'"><code code="{$country92}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'guinea'"><code code="{$country93}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'guinea-bissau'"><code code="{$country94}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'guyana'"><code code="{$country95}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'haiti'"><code code="{$country96}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'heard island and mcdonald islands'"><code code="{$country97}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'holy see'"><code code="{$country98}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'honduras'"><code code="{$country99}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'hong kong'"><code code="{$country100}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'hungary'"><code code="{$country101}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'iceland'"><code code="{$country102}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'india'"><code code="{$country103}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'indonesia'"><code code="{$country104}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'iran (islamic republic of)'"><code code="{$country105}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'iraq'"><code code="{$country106}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ireland'"><code code="{$country107}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'isle of man'"><code code="{$country108}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'israel'"><code code="{$country109}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'italy'"><code code="{$country110}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'jamaica'"><code code="{$country111}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'japan'"><code code="{$country112}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'jersey'"><code code="{$country113}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'jordan'"><code code="{$country114}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'kazakhstan'"><code code="{$country115}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'kenya'"><code code="{$country116}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'kiribati'"><code code="{$country117}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'kuwait'"><code code="{$country120}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'kyrgyzstan'"><code code="{$country121}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'latvia'"><code code="{$country123}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'lebanon'"><code code="{$country124}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'lesotho'"><code code="{$country125}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'liberia'"><code code="{$country126}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'libya'"><code code="{$country127}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'liechtenstein'"><code code="{$country128}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'lithuania'"><code code="{$country129}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'luxembourg'"><code code="{$country130}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'macao'"><code code="{$country131}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'macedonia (the former yugoslav republic of)'"><code code="{$country132}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'madagascar'"><code code="{$country133}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'malawi'"><code code="{$country134}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'malaysia'"><code code="{$country135}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'maldives'"><code code="{$country136}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'mali'"><code code="{$country137}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'malta'"><code code="{$country138}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'marshall islands'"><code code="{$country139}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'martinique'"><code code="{$country140}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'mauritania'"><code code="{$country141}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'mauritius'"><code code="{$country142}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'mayotte'"><code code="{$country143}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'mexico'"><code code="{$country144}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'micronesia (federated states of)'"><code code="{$country145}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'moldova (republic of)'"><code code="{$country146}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'monaco'"><code code="{$country147}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'mongolia'"><code code="{$country148}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'montenegro'"><code code="{$country149}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'montserrat'"><code code="{$country150}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'morocco'"><code code="{$country151}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'mozambique'"><code code="{$country152}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'myanmar'"><code code="{$country153}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'namibia'"><code code="{$country154}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'nauru'"><code code="{$country155}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'nepal'"><code code="{$country156}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'netherlands'"><code code="{$country157}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'new caledonia'"><code code="{$country158}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'new zealand'"><code code="{$country159}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'nicaragua'"><code code="{$country160}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'niger'"><code code="{$country161}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'nigeria'"><code code="{$country162}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'niue'"><code code="{$country163}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'norfolk island'"><code code="{$country164}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'northern mariana islands'"><code code="{$country165}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'norway'"><code code="{$country166}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'oman'"><code code="{$country167}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'pakistan'"><code code="{$country168}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'palau'"><code code="{$country169}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'palestine, state of'"><code code="{$country170}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'panama'"><code code="{$country171}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'papua new guinea'"><code code="{$country172}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'paraguay'"><code code="{$country173}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'peru'"><code code="{$country174}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'philippines'"><code code="{$country175}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'pitcairn'"><code code="{$country176}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'poland'"><code code="{$country177}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'portugal'"><code code="{$country178}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'puerto rico'"><code code="{$country179}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'qatar'"><code code="{$country180}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'reunion !réunion'"><code code="{$country181}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'romania'"><code code="{$country182}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'russian federation'"><code code="{$country183}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'rwanda'"><code code="{$country184}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'saint barthelemy !saint barthélemy'"><code code="{$country185}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'saint helena, ascension and tristan da cunha'"><code code="{$country186}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'saint kitts and nevis'"><code code="{$country187}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'saint lucia'"><code code="{$country188}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'saint martin (french part)'"><code code="{$country189}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'saint pierre and miquelon'"><code code="{$country190}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'saint vincent and the grenadines'"><code code="{$country191}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'samoa'"><code code="{$country192}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'san marino'"><code code="{$country193}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'sao tome and principe'"><code code="{$country194}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'saudi arabia'"><code code="{$country195}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'senegal'"><code code="{$country196}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'serbia'"><code code="{$country197}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'seychelles'"><code code="{$country198}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'sierra leone'"><code code="{$country199}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'singapore'"><code code="{$country200}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'sint maarten (dutch part)'"><code code="{$country201}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'slovakia'"><code code="{$country202}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'slovenia'"><code code="{$country203}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'solomon islands'"><code code="{$country204}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'somalia'"><code code="{$country205}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'south africa'"><code code="{$country206}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'south georgia and the south sandwich islands'"><code code="{$country207}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'south sudan'"><code code="{$country208}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'spain'"><code code="{$country209}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'sri lanka'"><code code="{$country210}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'sudan'"><code code="{$country211}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'suriname'"><code code="{$country212}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'svalbard and jan mayen'"><code code="{$country213}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'swaziland'"><code code="{$country214}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'sweden'"><code code="{$country215}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'switzerland'"><code code="{$country216}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'syrian arab republic'"><code code="{$country217}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'taiwan, province of china[a]'"><code code="{$country218}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'tajikistan'"><code code="{$country219}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'tanzania, united republic of'"><code code="{$country220}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'thailand'"><code code="{$country221}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'timor-leste'"><code code="{$country222}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'togo'"><code code="{$country223}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'tokelau'"><code code="{$country224}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'tonga'"><code code="{$country225}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'trinidad and tobago'"><code code="{$country226}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'tunisia'"><code code="{$country227}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'turkey'"><code code="{$country228}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'turkmenistan'"><code code="{$country229}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'turks and caicos islands'"><code code="{$country230}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'tuvalu'"><code code="{$country231}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'uganda'"><code code="{$country232}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'ukraine'"><code code="{$country233}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'united arab emirates'"><code code="{$country234}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'united kingdom of great britain and northern ireland'"><code code="{$country235}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'united kingdom'"><code code="{$country235}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'united states of america'"><code code="{$country236}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'united states'"><code code="{$country236}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'united states minor outlying islands'"><code code="{$country237}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'uruguay'"><code code="{$country238}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'uzbekistan'"><code code="{$country239}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'vanuatu'"><code code="{$country240}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'venezuela (bolivarian republic of)'"><code code="{$country241}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'viet nam'"><code code="{$country242}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'virgin islands (british)'"><code code="{$country243}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'virgin islands (u.s.)'"><code code="{$country244}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'wallis and futuna'"><code code="{$country245}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'western sahara'"><code code="{$country246}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'yemen'"><code code="{$country247}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'zambia'"><code code="{$country248}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry = 'zimbabwe'"><code code="{$country249}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/> </xsl:when>


<xsl:when test="reactionoccurcountry= 'af'"><code codeSystem="{$oidISOCountry}" code="{$country1}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ax'"><code codeSystem="{$oidISOCountry}" code="{$country2}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'al'"><code codeSystem="{$oidISOCountry}" code="{$country3}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'dz'"><code codeSystem="{$oidISOCountry}" code="{$country4}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'as'"><code codeSystem="{$oidISOCountry}" code="{$country5}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ad'"><code codeSystem="{$oidISOCountry}" code="{$country6}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ao'"><code codeSystem="{$oidISOCountry}" code="{$country7}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ai'"><code codeSystem="{$oidISOCountry}" code="{$country8}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'aq'"><code codeSystem="{$oidISOCountry}" code="{$country9}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ag'"><code codeSystem="{$oidISOCountry}" code="{$country10}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ar'"><code codeSystem="{$oidISOCountry}" code="{$country11}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'am'"><code codeSystem="{$oidISOCountry}" code="{$country12}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'aw'"><code codeSystem="{$oidISOCountry}" code="{$country13}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'au'"><code codeSystem="{$oidISOCountry}" code="{$country14}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'at'"><code codeSystem="{$oidISOCountry}" code="{$country15}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'az'"><code codeSystem="{$oidISOCountry}" code="{$country16}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'bs'"><code codeSystem="{$oidISOCountry}" code="{$country17}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'bh'"><code codeSystem="{$oidISOCountry}" code="{$country18}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'bd'"><code codeSystem="{$oidISOCountry}" code="{$country19}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'bb'"><code codeSystem="{$oidISOCountry}" code="{$country20}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'by'"><code codeSystem="{$oidISOCountry}" code="{$country21}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'be'"><code codeSystem="{$oidISOCountry}" code="{$country22}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'bz'"><code codeSystem="{$oidISOCountry}" code="{$country23}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'bj'"><code codeSystem="{$oidISOCountry}" code="{$country24}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'bm'"><code codeSystem="{$oidISOCountry}" code="{$country25}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'bt'"><code codeSystem="{$oidISOCountry}" code="{$country26}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'bo'"><code codeSystem="{$oidISOCountry}" code="{$country27}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'bq'"><code codeSystem="{$oidISOCountry}" code="{$country28}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ba'"><code codeSystem="{$oidISOCountry}" code="{$country29}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'bw'"><code codeSystem="{$oidISOCountry}" code="{$country30}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'bv'"><code codeSystem="{$oidISOCountry}" code="{$country31}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'br'"><code codeSystem="{$oidISOCountry}" code="{$country32}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'io'"><code codeSystem="{$oidISOCountry}" code="{$country33}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'bn'"><code codeSystem="{$oidISOCountry}" code="{$country34}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'bg'"><code codeSystem="{$oidISOCountry}" code="{$country35}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'bf'"><code codeSystem="{$oidISOCountry}" code="{$country36}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'bi'"><code codeSystem="{$oidISOCountry}" code="{$country37}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'cv'"><code codeSystem="{$oidISOCountry}" code="{$country38}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'kh'"><code codeSystem="{$oidISOCountry}" code="{$country39}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'cm'"><code codeSystem="{$oidISOCountry}" code="{$country40}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ca'"><code codeSystem="{$oidISOCountry}" code="{$country41}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ky'"><code codeSystem="{$oidISOCountry}" code="{$country42}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'cf'"><code codeSystem="{$oidISOCountry}" code="{$country43}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'td'"><code codeSystem="{$oidISOCountry}" code="{$country44}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'cl'"><code codeSystem="{$oidISOCountry}" code="{$country45}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'cn'"><code codeSystem="{$oidISOCountry}" code="{$country46}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'cx'"><code codeSystem="{$oidISOCountry}" code="{$country47}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'cc'"><code codeSystem="{$oidISOCountry}" code="{$country48}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'co'"><code codeSystem="{$oidISOCountry}" code="{$country49}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'km'"><code codeSystem="{$oidISOCountry}" code="{$country50}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'cg'"><code codeSystem="{$oidISOCountry}" code="{$country51}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'cd'"><code codeSystem="{$oidISOCountry}" code="{$country52}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ck'"><code codeSystem="{$oidISOCountry}" code="{$country53}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'cr'"><code codeSystem="{$oidISOCountry}" code="{$country54}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ci'"><code codeSystem="{$oidISOCountry}" code="{$country55}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'hr'"><code codeSystem="{$oidISOCountry}" code="{$country56}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'cu'"><code codeSystem="{$oidISOCountry}" code="{$country57}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'cw'"><code codeSystem="{$oidISOCountry}" code="{$country58}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'cy'"><code codeSystem="{$oidISOCountry}" code="{$country59}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'cz'"><code codeSystem="{$oidISOCountry}" code="{$country60}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'dk'"><code codeSystem="{$oidISOCountry}" code="{$country61}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'dj'"><code codeSystem="{$oidISOCountry}" code="{$country62}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'dm'"><code codeSystem="{$oidISOCountry}" code="{$country63}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'do'"><code codeSystem="{$oidISOCountry}" code="{$country64}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ec'"><code codeSystem="{$oidISOCountry}" code="{$country65}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'eg'"><code codeSystem="{$oidISOCountry}" code="{$country66}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'sv'"><code codeSystem="{$oidISOCountry}" code="{$country67}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'gq'"><code codeSystem="{$oidISOCountry}" code="{$country68}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'er'"><code codeSystem="{$oidISOCountry}" code="{$country69}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ee'"><code codeSystem="{$oidISOCountry}" code="{$country70}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'et'"><code codeSystem="{$oidISOCountry}" code="{$country71}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'fk'"><code codeSystem="{$oidISOCountry}" code="{$country72}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'fo'"><code codeSystem="{$oidISOCountry}" code="{$country73}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'fj'"><code codeSystem="{$oidISOCountry}" code="{$country74}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'fi'"><code codeSystem="{$oidISOCountry}" code="{$country75}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'fr'"><code codeSystem="{$oidISOCountry}" code="{$country76}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'gf'"><code codeSystem="{$oidISOCountry}" code="{$country77}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'pf'"><code codeSystem="{$oidISOCountry}" code="{$country78}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'tf'"><code codeSystem="{$oidISOCountry}" code="{$country79}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ga'"><code codeSystem="{$oidISOCountry}" code="{$country80}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'gm'"><code codeSystem="{$oidISOCountry}" code="{$country81}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ge'"><code codeSystem="{$oidISOCountry}" code="{$country82}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'de'"><code codeSystem="{$oidISOCountry}" code="{$country83}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'gh'"><code codeSystem="{$oidISOCountry}" code="{$country84}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'gi'"><code codeSystem="{$oidISOCountry}" code="{$country85}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'gr'"><code codeSystem="{$oidISOCountry}" code="{$country86}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'gl'"><code codeSystem="{$oidISOCountry}" code="{$country87}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'gd'"><code codeSystem="{$oidISOCountry}" code="{$country88}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'gp'"><code codeSystem="{$oidISOCountry}" code="{$country89}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'gu'"><code codeSystem="{$oidISOCountry}" code="{$country90}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'gt'"><code codeSystem="{$oidISOCountry}" code="{$country91}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'gg'"><code codeSystem="{$oidISOCountry}" code="{$country92}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'gn'"><code codeSystem="{$oidISOCountry}" code="{$country93}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'gw'"><code codeSystem="{$oidISOCountry}" code="{$country94}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'gy'"><code codeSystem="{$oidISOCountry}" code="{$country95}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ht'"><code codeSystem="{$oidISOCountry}" code="{$country96}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'hm'"><code codeSystem="{$oidISOCountry}" code="{$country97}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'va'"><code codeSystem="{$oidISOCountry}" code="{$country98}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'hn'"><code codeSystem="{$oidISOCountry}" code="{$country99}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'hk'"><code codeSystem="{$oidISOCountry}" code="{$country100}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'hu'"><code codeSystem="{$oidISOCountry}" code="{$country101}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'is'"><code codeSystem="{$oidISOCountry}" code="{$country102}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'in'"><code codeSystem="{$oidISOCountry}" code="{$country103}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'id'"><code codeSystem="{$oidISOCountry}" code="{$country104}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ir'"><code codeSystem="{$oidISOCountry}" code="{$country105}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'iq'"><code codeSystem="{$oidISOCountry}" code="{$country106}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ie'"><code codeSystem="{$oidISOCountry}" code="{$country107}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'im'"><code codeSystem="{$oidISOCountry}" code="{$country108}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'il'"><code codeSystem="{$oidISOCountry}" code="{$country109}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'it'"><code codeSystem="{$oidISOCountry}" code="{$country110}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'jm'"><code codeSystem="{$oidISOCountry}" code="{$country111}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'jp'"><code codeSystem="{$oidISOCountry}" code="{$country112}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'je'"><code codeSystem="{$oidISOCountry}" code="{$country113}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'jo'"><code codeSystem="{$oidISOCountry}" code="{$country114}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'kz'"><code codeSystem="{$oidISOCountry}" code="{$country115}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ke'"><code codeSystem="{$oidISOCountry}" code="{$country116}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ki'"><code codeSystem="{$oidISOCountry}" code="{$country117}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'kp'"><code codeSystem="{$oidISOCountry}" code="{$country118}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'kr'"><code codeSystem="{$oidISOCountry}" code="{$country119}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'kw'"><code codeSystem="{$oidISOCountry}" code="{$country120}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'kg'"><code codeSystem="{$oidISOCountry}" code="{$country121}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'la'"><code codeSystem="{$oidISOCountry}" code="{$country122}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'lv'"><code codeSystem="{$oidISOCountry}" code="{$country123}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'lb'"><code codeSystem="{$oidISOCountry}" code="{$country124}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ls'"><code codeSystem="{$oidISOCountry}" code="{$country125}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'lr'"><code codeSystem="{$oidISOCountry}" code="{$country126}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ly'"><code codeSystem="{$oidISOCountry}" code="{$country127}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'li'"><code codeSystem="{$oidISOCountry}" code="{$country128}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'lt'"><code codeSystem="{$oidISOCountry}" code="{$country129}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'lu'"><code codeSystem="{$oidISOCountry}" code="{$country130}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'mo'"><code codeSystem="{$oidISOCountry}" code="{$country131}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'mk'"><code codeSystem="{$oidISOCountry}" code="{$country132}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'mg'"><code codeSystem="{$oidISOCountry}" code="{$country133}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'mw'"><code codeSystem="{$oidISOCountry}" code="{$country134}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'my'"><code codeSystem="{$oidISOCountry}" code="{$country135}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'mv'"><code codeSystem="{$oidISOCountry}" code="{$country136}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ml'"><code codeSystem="{$oidISOCountry}" code="{$country137}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'mt'"><code codeSystem="{$oidISOCountry}" code="{$country138}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'mh'"><code codeSystem="{$oidISOCountry}" code="{$country139}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'mq'"><code codeSystem="{$oidISOCountry}" code="{$country140}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'mr'"><code codeSystem="{$oidISOCountry}" code="{$country141}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'mu'"><code codeSystem="{$oidISOCountry}" code="{$country142}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'yt'"><code codeSystem="{$oidISOCountry}" code="{$country143}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'mx'"><code codeSystem="{$oidISOCountry}" code="{$country144}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'fm'"><code codeSystem="{$oidISOCountry}" code="{$country145}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'md'"><code codeSystem="{$oidISOCountry}" code="{$country146}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'mc'"><code codeSystem="{$oidISOCountry}" code="{$country147}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'mn'"><code codeSystem="{$oidISOCountry}" code="{$country148}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'me'"><code codeSystem="{$oidISOCountry}" code="{$country149}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ms'"><code codeSystem="{$oidISOCountry}" code="{$country150}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ma'"><code codeSystem="{$oidISOCountry}" code="{$country151}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'mz'"><code codeSystem="{$oidISOCountry}" code="{$country152}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'mm'"><code codeSystem="{$oidISOCountry}" code="{$country153}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'na'"><code codeSystem="{$oidISOCountry}" code="{$country154}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'nr'"><code codeSystem="{$oidISOCountry}" code="{$country155}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'np'"><code codeSystem="{$oidISOCountry}" code="{$country156}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'nl'"><code codeSystem="{$oidISOCountry}" code="{$country157}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'nc'"><code codeSystem="{$oidISOCountry}" code="{$country158}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'nz'"><code codeSystem="{$oidISOCountry}" code="{$country159}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ni'"><code codeSystem="{$oidISOCountry}" code="{$country160}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ne'"><code codeSystem="{$oidISOCountry}" code="{$country161}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ng'"><code codeSystem="{$oidISOCountry}" code="{$country162}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'nu'"><code codeSystem="{$oidISOCountry}" code="{$country163}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'nf'"><code codeSystem="{$oidISOCountry}" code="{$country164}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'mp'"><code codeSystem="{$oidISOCountry}" code="{$country165}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'no'"><code codeSystem="{$oidISOCountry}" code="{$country166}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'om'"><code codeSystem="{$oidISOCountry}" code="{$country167}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'pk'"><code codeSystem="{$oidISOCountry}" code="{$country168}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'pw'"><code codeSystem="{$oidISOCountry}" code="{$country169}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ps'"><code codeSystem="{$oidISOCountry}" code="{$country170}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'pa'"><code codeSystem="{$oidISOCountry}" code="{$country171}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'pg'"><code codeSystem="{$oidISOCountry}" code="{$country172}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'py'"><code codeSystem="{$oidISOCountry}" code="{$country173}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'pe'"><code codeSystem="{$oidISOCountry}" code="{$country174}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ph'"><code codeSystem="{$oidISOCountry}" code="{$country175}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'pn'"><code codeSystem="{$oidISOCountry}" code="{$country176}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'pl'"><code codeSystem="{$oidISOCountry}" code="{$country177}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'pt'"><code codeSystem="{$oidISOCountry}" code="{$country178}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'pr'"><code codeSystem="{$oidISOCountry}" code="{$country179}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'qa'"><code codeSystem="{$oidISOCountry}" code="{$country180}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 're'"><code codeSystem="{$oidISOCountry}" code="{$country181}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ro'"><code codeSystem="{$oidISOCountry}" code="{$country182}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ru'"><code codeSystem="{$oidISOCountry}" code="{$country183}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'rw'"><code codeSystem="{$oidISOCountry}" code="{$country184}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'bl'"><code codeSystem="{$oidISOCountry}" code="{$country185}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'sh'"><code codeSystem="{$oidISOCountry}" code="{$country186}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'kn'"><code codeSystem="{$oidISOCountry}" code="{$country187}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'lc'"><code codeSystem="{$oidISOCountry}" code="{$country188}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'mf'"><code codeSystem="{$oidISOCountry}" code="{$country189}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'pm'"><code codeSystem="{$oidISOCountry}" code="{$country190}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'vc'"><code codeSystem="{$oidISOCountry}" code="{$country191}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ws'"><code codeSystem="{$oidISOCountry}" code="{$country192}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'sm'"><code codeSystem="{$oidISOCountry}" code="{$country193}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'st'"><code codeSystem="{$oidISOCountry}" code="{$country194}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'sa'"><code codeSystem="{$oidISOCountry}" code="{$country195}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'sn'"><code codeSystem="{$oidISOCountry}" code="{$country196}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'rs'"><code codeSystem="{$oidISOCountry}" code="{$country197}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'sc'"><code codeSystem="{$oidISOCountry}" code="{$country198}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'sl'"><code codeSystem="{$oidISOCountry}" code="{$country199}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'sg'"><code codeSystem="{$oidISOCountry}" code="{$country200}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'sx'"><code codeSystem="{$oidISOCountry}" code="{$country201}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'sk'"><code codeSystem="{$oidISOCountry}" code="{$country202}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'si'"><code codeSystem="{$oidISOCountry}" code="{$country203}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'sb'"><code codeSystem="{$oidISOCountry}" code="{$country204}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'so'"><code codeSystem="{$oidISOCountry}" code="{$country205}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'za'"><code codeSystem="{$oidISOCountry}" code="{$country206}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'gs'"><code codeSystem="{$oidISOCountry}" code="{$country207}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ss'"><code codeSystem="{$oidISOCountry}" code="{$country208}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'es'"><code codeSystem="{$oidISOCountry}" code="{$country209}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'lk'"><code codeSystem="{$oidISOCountry}" code="{$country210}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'sd'"><code codeSystem="{$oidISOCountry}" code="{$country211}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'sr'"><code codeSystem="{$oidISOCountry}" code="{$country212}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'sj'"><code codeSystem="{$oidISOCountry}" code="{$country213}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'sz'"><code codeSystem="{$oidISOCountry}" code="{$country214}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'se'"><code codeSystem="{$oidISOCountry}" code="{$country215}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ch'"><code codeSystem="{$oidISOCountry}" code="{$country216}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'sy'"><code codeSystem="{$oidISOCountry}" code="{$country217}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'tw'"><code codeSystem="{$oidISOCountry}" code="{$country218}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'tj'"><code codeSystem="{$oidISOCountry}" code="{$country219}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'tz'"><code codeSystem="{$oidISOCountry}" code="{$country220}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'th'"><code codeSystem="{$oidISOCountry}" code="{$country221}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'tl'"><code codeSystem="{$oidISOCountry}" code="{$country222}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'tg'"><code codeSystem="{$oidISOCountry}" code="{$country223}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'tk'"><code codeSystem="{$oidISOCountry}" code="{$country224}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'to'"><code codeSystem="{$oidISOCountry}" code="{$country225}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'tt'"><code codeSystem="{$oidISOCountry}" code="{$country226}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'tn'"><code codeSystem="{$oidISOCountry}" code="{$country227}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'tr'"><code codeSystem="{$oidISOCountry}" code="{$country228}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'tm'"><code codeSystem="{$oidISOCountry}" code="{$country229}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'tc'"><code codeSystem="{$oidISOCountry}" code="{$country230}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'tv'"><code codeSystem="{$oidISOCountry}" code="{$country231}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ug'"><code codeSystem="{$oidISOCountry}" code="{$country232}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ua'"><code codeSystem="{$oidISOCountry}" code="{$country233}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ae'"><code codeSystem="{$oidISOCountry}" code="{$country234}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'gb'"><code codeSystem="{$oidISOCountry}" code="{$country235}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'us'"><code codeSystem="{$oidISOCountry}" code="{$country236}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'um'"><code codeSystem="{$oidISOCountry}" code="{$country237}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'uy'"><code codeSystem="{$oidISOCountry}" code="{$country238}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'uz'"><code codeSystem="{$oidISOCountry}" code="{$country239}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'vu'"><code codeSystem="{$oidISOCountry}" code="{$country240}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 've'"><code codeSystem="{$oidISOCountry}" code="{$country241}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'vn'"><code codeSystem="{$oidISOCountry}" code="{$country242}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'vg'"><code codeSystem="{$oidISOCountry}" code="{$country243}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'vi'"><code codeSystem="{$oidISOCountry}" code="{$country244}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'wf'"><code codeSystem="{$oidISOCountry}" code="{$country245}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'eh'"><code codeSystem="{$oidISOCountry}" code="{$country246}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'ye'"><code codeSystem="{$oidISOCountry}" code="{$country247}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'zm'"><code codeSystem="{$oidISOCountry}" code="{$country248}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'zw'"><code codeSystem="{$oidISOCountry}" code="{$country249}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:when test="reactionoccurcountry= 'eu'"><code codeSystem="{$oidISOCountry}" code="{$country250}" displayName="EventOccuredCountry"/> </xsl:when>
<xsl:otherwise>
<code code="{reactionoccurcountry}" codeSystem="{$oidISOCountry}" displayName="EventOccuredCountry"/>
</xsl:otherwise>
</xsl:choose>
<xsl:comment>E.i.9:IdentificationoftheCountryWheretheReaction/EventOccurred</xsl:comment>
</locatedPlace>
</locatedEntity>
</location>
</xsl:if> 
<!--B.2.i.0, 1 Reaction/Event as Reported by Primary Source and in MedDRA terminology-->
<xsl:if test="string-length(reactionmeddraversion) > 0 or string-length(reactionmeddracode) > 0 or string-length(primarysourcereactionnative) > 0">
<outboundRelationship2 typeCode="CAUS">
<observation classCode="OBS" moodCode="EVN">
<code code="{$Reaction}" codeSystem="{$oidObservationCode}" displayName="reaction"/>					
<value xsi:type="CE" code="{reactionmeddracode}" codeSystem="{$oidMedDRA}" codeSystemVersion="{reactionmeddraversion}"> 
<xsl:comment>E.i.2.1a: MedDRA Version for Reaction / Event</xsl:comment>
<xsl:comment>E.i.2.1b: Reaction / Event (MedDRA code)</xsl:comment>		
<xsl:if test="string-length(primarysourcereactionnative) > 0">
<originalText language="{primarysourcereactionnativelang}"><xsl:value-of select="primarysourcereactionnative"/></originalText>
<xsl:comment>E.i.1.1a: Reaction / Event as Reported by the Primary Source in Native Language</xsl:comment>
<xsl:comment>E.i.1.1b: Reaction / Event as Reported by the Primary Source Language</xsl:comment>
</xsl:if>
</value>
</observation>					 
</outboundRelationship2>
</xsl:if>
<!--B.2.i Identification of the Country where the Reaction Occurred-->

<xsl:if test="string-length(primarysourcereaction)>0">
<outboundRelationship2 typeCode="PERT">
<observation moodCode="EVN" classCode="OBS">
<code code="30" codeSystem="{$oidObservationCode}" displayName="reactionForTranslation"/>
<value xsi:type="ED">
<xsl:value-of select="primarysourcereaction"/>
</value>
<xsl:comment>E.i.1.2: Reaction / Event as Reported by the Primary Source for Translation</xsl:comment>
</observation>
</outboundRelationship2>
</xsl:if>

<!--B.2.i.2.1 Term Highlighted by Reporter-->
<xsl:if test="string-length(termhighlighted)>0">
<outboundRelationship2 typeCode="PERT">
<observation moodCode="EVN" classCode="OBS">
<code code="{$TermHighlightedByReporter}" codeSystem="{$oidObservationCode}" displayName="termHighlightedByReporter"/>
<xsl:choose>
<xsl:when test="termhighlighted= 1"><value xsi:type="CE" code="{termhighlighted}" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="Yes,highlightedbythereporter,NOTserious"/> </xsl:when>

<xsl:when test="termhighlighted= 2"><value xsi:type="CE" code="{termhighlighted}" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="No,nothighlightedbythereporter,NOTserious"/> </xsl:when>

<xsl:when test="termhighlighted=3"><value xsi:type="CE" code="{termhighlighted}" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="Yes,highlightedbythereporter,SERIOUS"/> </xsl:when>

<xsl:when test="termhighlighted= 4"><value xsi:type="CE" code="{termhighlighted}" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="No,nothighlightedbythereporter,SERIOUS"/> </xsl:when>

<xsl:when test="termhighlighted= 'yes'"><value xsi:type="CE" code="1" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="Yes,highlightedbythereporter,NOTserious"/> </xsl:when>

<xsl:when test="termhighlighted= 'YES'"><value xsi:type="CE" code="1" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="Yes,highlightedbythereporter,NOTserious"/> </xsl:when>

<xsl:when test="termhighlighted= 'Yes'"><value xsi:type="CE" code="1" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="Yes,highlightedbythereporter,NOTserious"/> </xsl:when>

<xsl:when test="termhighlighted= 'NO'"><value xsi:type="CE" code="2" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="No,nothighlightedbythereporter,NOTserious"/> </xsl:when>

<xsl:when test="termhighlighted=no"><value xsi:type="CE" code="2" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="No,nothighlightedbythereporter,NOTserious"/> </xsl:when>

<xsl:when test="termhighlighted=No"><value xsi:type="CE" code="2" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="No,nothighlightedbythereporter,NOTserious"/> </xsl:when>

<xsl:when test="termhighlighted= 'Yes, highlighted by the reporter, NOT serious'"><value xsi:type="CE" code="1" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="Yes,highlightedbythereporter,NOTserious"/> </xsl:when>

<xsl:when test="termhighlighted= 'No, not highlighted by the reporter, NOT serious'"><value xsi:type="CE" code="2" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="No,nothighlightedbythereporter,NOTserious"/> </xsl:when>

<xsl:when test="termhighlighted= 'Yes, highlighted by the reporter, SERIOUS'"><value xsi:type="CE" code="3" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="Yes,highlightedbythereporter,SERIOUS"/> </xsl:when>

<xsl:when test="termhighlighted= 'No, not highlighted by the reporter, SERIOUS'"><value xsi:type="CE" code="4" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="No,nothighlightedbythereporter,SERIOUS"/> </xsl:when>

<xsl:when test="termhighlighted= 'yes, highlighted by the reporter, not serious'"><value xsi:type="CE" code="1" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="Yes,highlightedbythereporter,NOTserious"/> </xsl:when>

<xsl:when test="termhighlighted= 'no, not highlighted by the reporter, not serious'"><value xsi:type="CE" code="2" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="No,nothighlightedbythereporter,NOTserious"/> </xsl:when>

<xsl:when test="termhighlighted= 'yes, highlighted by the reporter, serious'"><value xsi:type="CE" code="3" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="Yes,highlightedbythereporter,SERIOUS"/> </xsl:when>

<xsl:when test="termhighlighted= 'no, not highlighted by the reporter, serious'"><value xsi:type="CE" code="4" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="No,nothighlightedbythereporter,SERIOUS"/> </xsl:when>

<xsl:when test="termhighlighted= 'YES, HIGHLIGHTED BY THE REPORTER, NOT SERIOUS'"><value xsi:type="CE" code="1" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="Yes,highlightedbythereporter,NOTserious"/> </xsl:when>

<xsl:when test="termhighlighted= 'NO, NOT HIGHLIGHTED BY THE REPORTER, NOT SERIOUS'"><value xsi:type="CE" code="2" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="No,nothighlightedbythereporter,NOTserious"/> </xsl:when>

<xsl:when test="termhighlighted= 'YES, HIGHLIGHTED BY THE REPORTER, SERIOUS'"><value xsi:type="CE" code="3" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="Yes,highlightedbythereporter,SERIOUS"/> </xsl:when>

<xsl:when test="termhighlighted= 'NO, NOT HIGHLIGHTED BY THE REPORTER, SERIOUS'"><value xsi:type="CE" code="4" codeSystem="{$oidTermHighlighted}" codeSystemVersion="{termhighlightedcsv}" displayName="No,nothighlightedbythereporter,SERIOUS"/> </xsl:when>

</xsl:choose>
<xsl:comment>E.i.3.1:TermHighlightedbytheReporter</xsl:comment>
<xsl:comment>E.i.3.1[Ver]:TermHighlightedbytheReportercodesystemversion</xsl:comment>
</observation>
</outboundRelationship2>
</xsl:if>
<!--B.2.i.2.2 Seriousness Criteria at Event Level-->
<outboundRelationship2 typeCode="PERT">
<observation moodCode="EVN" classCode="OBS">
<code code="{$ResultsInDeath}" codeSystem="{$oidObservationCode}" displayName="resultsInDeath"/>
<xsl:choose>
<xsl:when test="seriousnessdeath= 1"> <value xsi:type="BL" value="true"/> </xsl:when>

<xsl:when test="seriousnessdeath= 'true'"> <value xsi:type="BL" value="true"/> </xsl:when>

<xsl:when test="seriousnessdeath= 'TRUE'"> <value xsi:type="BL" value="true"/> </xsl:when>

<xsl:when test="seriousnessdeath= 'True'"> <value xsi:type="BL" value="true"/> </xsl:when>

<xsl:when test="seriousnessdeath= 'yes'"> <value xsi:type="BL" value="true"/> </xsl:when>

<xsl:when test="seriousnessdeath= 'YES'"> <value xsi:type="BL" value="true"/> </xsl:when>

<xsl:when test="seriousnessdeath= 'Yes'"> <value xsi:type="BL" value="true"/> </xsl:when>

<xsl:otherwise>
<value xsi:type="BL" nullFlavor="NI"/>
</xsl:otherwise>
</xsl:choose>
<xsl:comment>E.i.3.2a:ResultsinDeath</xsl:comment>
</observation>
</outboundRelationship2>
<outboundRelationship2 typeCode="PERT">
<observation moodCode="EVN" classCode="OBS">
<code code="{$LifeThreatening}" codeSystem="{$oidObservationCode}" displayName="isLifeThreatening"/>
<xsl:choose>
<xsl:when test="seriousnesslifethreatening= 1"><value xsi:type="BL" value="true"/> </xsl:when>

<xsl:when test="seriousnesslifethreatening= 'true'"><value xsi:type="BL" value="true"/> </xsl:when>

<xsl:when test="seriousnesslifethreatening= 'TRUE'"><value xsi:type="BL" value="true"/> </xsl:when>

<xsl:when test="seriousnesslifethreatening= 'True'"><value xsi:type="BL" value="true"/> </xsl:when>

<xsl:when test="seriousnesslifethreatening= 'YES'"><value xsi:type="BL" value="true"/> </xsl:when>

<xsl:when test="seriousnesslifethreatening= 'Yes'"><value xsi:type="BL" value="true"/> </xsl:when>

<xsl:when test="seriousnesslifethreatening= 'yes'"><value xsi:type="BL" value="true"/> </xsl:when>

<xsl:otherwise>
<value xsi:type="BL" nullFlavor="NI"/>
</xsl:otherwise>
</xsl:choose>
<xsl:comment>E.i.3.2b:LifeThreatening</xsl:comment>
</observation>
</outboundRelationship2>
<outboundRelationship2 typeCode="PERT">
<observation moodCode="EVN" classCode="OBS">
<code code="{$CausedProlongedHospitalisation}" codeSystem="{$oidObservationCode}" displayName="requiresInpatientHospitalization"/>
<xsl:choose>
<xsl:when test="seriousnesshospitalization= 1"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnesshospitalization= 'true'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnesshospitalization= 'TRUE'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnesshospitalization= 'True'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnesshospitalization= 'yes'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnesshospitalization= 'YES'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnesshospitalization= 'Yes'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:otherwise>
<value xsi:type="BL" nullFlavor="NI"/>
</xsl:otherwise>
</xsl:choose>
<xsl:comment>E.i.3.2c:Caused/ProlongedHospitalisation</xsl:comment>
</observation>
</outboundRelationship2>

<outboundRelationship2 typeCode="PERT">
<observation moodCode="EVN" classCode="OBS">
<code code="{$DisablingIncapaciting}" codeSystem="{$oidObservationCode}" displayName="resultsInPersistentOrSignificantDisability"/>
<xsl:choose>
<xsl:when test="seriousnessdisabling= 1"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnessdisabling= 'true'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnessdisabling= 'TRUE'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnessdisabling= 'True'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnessdisabling= 'yes'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnessdisabling= 'YES'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnessdisabling= 'Yes'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:otherwise>
<value xsi:type="BL" nullFlavor="NI"/>
</xsl:otherwise>
</xsl:choose>
<xsl:comment>E.i.3.2d:Disabling/Incapacitating</xsl:comment>
</observation>
</outboundRelationship2>
<outboundRelationship2 typeCode="PERT">
<observation moodCode="EVN" classCode="OBS">
<code code="{$CongenitalAnomalyBirthDefect}" codeSystem="{$oidObservationCode}" displayName="congenitalAnomalyBirthDefect"/>
<xsl:choose>
<xsl:when test="seriousnesscongenitalanomali= 1"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnesscongenitalanomali= 'true'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnesscongenitalanomali= 'TRUE'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnesscongenitalanomali= 'True'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnesscongenitalanomali= 'yes'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnesscongenitalanomali= 'YES'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnesscongenitalanomali= 'Yes'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:otherwise>
<value xsi:type="BL" nullFlavor="NI"/>
</xsl:otherwise>
</xsl:choose>
<xsl:comment>E.i.3.2e:CongenitalAnomaly/BirthDefect</xsl:comment>
</observation>
</outboundRelationship2>
<outboundRelationship2 typeCode="PERT">
<observation moodCode="EVN" classCode="OBS">
<code code="{$OtherMedicallyImportantCondition}" codeSystem="{$oidObservationCode}" displayName="otherMedicallyImportantCondition"/>
<xsl:choose>
<xsl:when test="seriousnessother= 1"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnessother= 'true'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnessother= 'TRUE'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnessother= 'True'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnessother= 'yes'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnessother= 'YES'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:when test="seriousnessother= 'Yes'"><value xsi:type="BL" value="true"/> </xsl:when>
<xsl:otherwise>
<value xsi:type="BL" nullFlavor="NI"/>
</xsl:otherwise>
</xsl:choose>
<xsl:comment>E.i.3.2f:OtherMedicallyImportantCondition</xsl:comment>
</observation>
</outboundRelationship2>
			
<!--B.2.i.8 Outcome of the Reaction-->
<xsl:if test="string-length(reactionoutcome)>0">
<outboundRelationship2 typeCode="PERT">
<observation moodCode="EVN" classCode="OBS">
<code code="{$Outcome}" codeSystem="{$oidObservationCode}" displayName="outcome"/>
<xsl:choose>
<xsl:when test="reactionoutcome= 0"><value xsi:type="CE" code="{reactionoutcome}" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="unknown"/> </xsl:when>
<xsl:when test="reactionoutcome= 1"><value xsi:type="CE" code="{reactionoutcome}" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="recovered/resolved"/> </xsl:when>
<xsl:when test="reactionoutcome= 2"><value xsi:type="CE" code="{reactionoutcome}" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="recovering/resolving"/> </xsl:when>
<xsl:when test="reactionoutcome=3"><value xsi:type="CE" code="{reactionoutcome}" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="notrecovered/notresolved/ongoing"/> </xsl:when>
<xsl:when test="reactionoutcome= 4"><value xsi:type="CE" code="{reactionoutcome}" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="recovered/resolvedwithsequelae"/> </xsl:when>
<xsl:when test="reactionoutcome= 5"><value xsi:type="CE" code="{reactionoutcome}" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="fatal"/> </xsl:when>

<xsl:when test="reactionoutcome= 'UNKNOWN'"><value xsi:type="CE" code="0" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="unknown"/> </xsl:when>
<xsl:when test="reactionoutcome= 'RECOVERED/RESOLVED'"><value xsi:type="CE" code="1" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="recovered/resolved"/> </xsl:when>
<xsl:when test="reactionoutcome= 'RECOVERING/RESOLVING'"><value xsi:type="CE" code="2" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="recovering/resolving"/> </xsl:when>
<xsl:when test="reactionoutcome= 'NOT RECOVERED/NOT RESOLVED/ONGOING'"><value xsi:type="CE" code="3" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="notrecovered/notresolved/ongoing"/> </xsl:when>
<xsl:when test="reactionoutcome= 'NOT RECOVERED'"><value xsi:type="CE" code="3" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="notrecovered/notresolved/ongoing"/> </xsl:when>
<xsl:when test="reactionoutcome= 'NOT RESOLVED'"><value xsi:type="CE" code="3" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="notrecovered/notresolved/ongoing"/> </xsl:when>
<xsl:when test="reactionoutcome= 'ONGOING'"><value xsi:type="CE" code="3" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="notrecovered/notresolved/ongoing"/> </xsl:when>
<xsl:when test="reactionoutcome= 'RECOVERED/RESOLVED WITH SEQUELAE'"><value xsi:type="CE" code="4" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="recovered/resolvedwithsequelae"/> </xsl:when>
<xsl:when test="reactionoutcome= 'FATAL'"><value xsi:type="CE" code="5" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="fatal"/> </xsl:when>

<xsl:when test="reactionoutcome= 'Unknown'"><value xsi:type="CE" code="0" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="unknown"/> </xsl:when>
<xsl:when test="reactionoutcome= 'Recovered/Resolved'"><value xsi:type="CE" code="1" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="recovered/resolved"/> </xsl:when>
<xsl:when test="reactionoutcome= 'Recovering/Resolving'"><value xsi:type="CE" code="2" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="recovering/resolving"/> </xsl:when>
<xsl:when test="reactionoutcome= 'Not recovered/Not resolved/Ongoing'"><value xsi:type="CE" code="3" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="notrecovered/notresolved/ongoing"/> </xsl:when>
<xsl:when test="reactionoutcome= 'Not recovered'"><value xsi:type="CE" code="3" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="notrecovered/notresolved/ongoing"/> </xsl:when>
<xsl:when test="reactionoutcome= 'Not resolved'"><value xsi:type="CE" code="3" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="notrecovered/notresolved/ongoing"/> </xsl:when>
<xsl:when test="reactionoutcome= 'Ongoing'"><value xsi:type="CE" code="3" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="notrecovered/notresolved/ongoing"/> </xsl:when>
<xsl:when test="reactionoutcome= 'Recovered/Resolved with sequelae'"><value xsi:type="CE" code="4" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="recovered/resolvedwithsequelae"/> </xsl:when>
<xsl:when test="reactionoutcome= 'Fatal'"><value xsi:type="CE" code="5" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="fatal"/> </xsl:when>

<xsl:when test="reactionoutcome= 'Unknown'"><value xsi:type="CE" code="0" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="unknown"/> </xsl:when>
<xsl:when test="reactionoutcome= 'Recovered/Resolved'"><value xsi:type="CE" code="1" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="recovered/resolved"/> </xsl:when>
<xsl:when test="reactionoutcome= 'Recovering/Resolving'"><value xsi:type="CE" code="2" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="recovering/resolving"/> </xsl:when>
<xsl:when test="reactionoutcome= 'Not Recovered/Not Resolved/Ongoing'"><value xsi:type="CE" code="3" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="notrecovered/notresolved/ongoing"/> </xsl:when>
<xsl:when test="reactionoutcome= 'Not Recovered'"><value xsi:type="CE" code="3" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="notrecovered/notresolved/ongoing"/> </xsl:when>
<xsl:when test="reactionoutcome= 'Not Resolved'"><value xsi:type="CE" code="3" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="notrecovered/notresolved/ongoing"/> </xsl:when>
<xsl:when test="reactionoutcome= 'Ongoing'"><value xsi:type="CE" code="3" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="notrecovered/notresolved/ongoing"/> </xsl:when>
<xsl:when test="reactionoutcome= 'Recovered/Resolved With Sequelae'"><value xsi:type="CE" code="4" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="recovered/resolvedwithsequelae"/> </xsl:when>
<xsl:when test="reactionoutcome= 'Fatal'"><value xsi:type="CE" code="5" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="fatal"/> </xsl:when>

<xsl:when test="reactionoutcome= 'unknown'"><value xsi:type="CE" code="0" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="unknown"/> </xsl:when>
<xsl:when test="reactionoutcome= 'recovered/resolved'"><value xsi:type="CE" code="1" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="recovered/resolved"/> </xsl:when>
<xsl:when test="reactionoutcome= 'recovering/resolving'"><value xsi:type="CE" code="2" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="recovering/resolving"/> </xsl:when>
<xsl:when test="reactionoutcome= 'not recovered/not resolved/ongoing'"><value xsi:type="CE" code="3" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="notrecovered/notresolved/ongoing"/> </xsl:when>
<xsl:when test="reactionoutcome= 'not recovered'"><value xsi:type="CE" code="3" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="notrecovered/notresolved/ongoing"/> </xsl:when>
<xsl:when test="reactionoutcome= 'not resolved'"><value xsi:type="CE" code="3" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="notrecovered/notresolved/ongoing"/> </xsl:when>
<xsl:when test="reactionoutcome= 'ongoing'"><value xsi:type="CE" code="3" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="notrecovered/notresolved/ongoing"/> </xsl:when>
<xsl:when test="reactionoutcome= 'recovered/resolved with sequelae'"><value xsi:type="CE" code="4" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="recovered/resolvedwithsequelae"/> </xsl:when>
<xsl:when test="reactionoutcome= 'fatal'"><value xsi:type="CE" code="5" codeSystem="{$oidOutcome}" codeSystemVersion="{reactionouotcomecsv}" displayName="fatal"/> </xsl:when>

</xsl:choose>
<xsl:comment>E.i.7:OutcomeofReaction/EventattheTimeofLastObservation</xsl:comment>
<xsl:comment>E.i.7.CSVOutcomeofReaction/EventattheTimeofLastObservationcodesystemversion</xsl:comment>
</observation>
</outboundRelationship2>
</xsl:if>

<xsl:if test="string-length(reactionmedconfirmed)>0">
<outboundRelationship2 typeCode="PERT">
<observation moodCode="EVN" classCode="OBS">
<code code="24" codeSystem="{$oidObservationCode}" displayName="medicalConfirmationByHealthProfessional"/>

<xsl:choose>
<xsl:when test="reactionmedconfirmed= 1">
<value xsi:type="BL" value="true"/>
</xsl:when>
<xsl:when test="reactionmedconfirmed= 'true'">
<value xsi:type="BL" value="true"/>
</xsl:when>
<xsl:when test="reactionmedconfirmed= 'TRUE'">
<value xsi:type="BL" value="true"/>
</xsl:when>
<xsl:when test="reactionmedconfirmed= 'True'">
<value xsi:type="BL" value="true"/>
</xsl:when>
<xsl:when test="reactionmedconfirmed= 'yes'">
<value xsi:type="BL" value="true"/>
</xsl:when>
<xsl:when test="reactionmedconfirmed= 'YES'">
<value xsi:type="BL" value="true"/>
</xsl:when>
<xsl:when test="reactionmedconfirmed= 'Yes'">
<value xsi:type="BL" value="true"/>
</xsl:when>
<xsl:otherwise>
<value xsi:type="BL" nullFlavor="NI"/>
</xsl:otherwise>
</xsl:choose>
<xsl:comment>E.i.8:MedicalConfirmationbyHealthcareProfessional</xsl:comment>
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
            <xsl:when test="string-length(testresulttext) > 0">
                  <xsl:comment>F.r.3.4: Result Unstructured Data (free text)</xsl:comment>
              <value xsi:type="ED">
                <xsl:value-of select="testresulttext"/>
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
						<xsl:if test="string-length(druginventedname) > 0"  >
							<xsl:comment>G.k.2.2.EU.1: Name part - Invented name</xsl:comment>
							<delimiter qualifier="INV"><xsl:value-of select="druginventedname"/></delimiter>
						</xsl:if>						  
						<xsl:if test="string-length(drugscientificname) > 0"  >
							<xsl:comment>G.k.2.2.EU.2: Name part - Scientific name</xsl:comment>
							<delimiter qualifier="SCI"><xsl:value-of select="drugscientificname"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(drugtrademarkname) > 0"  >
							<xsl:comment>G.k.2.2.EU.3: Name part - Trademark name</xsl:comment>
							<delimiter qualifier="TMK"><xsl:value-of select="drugtrademarkname"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(drugstrengthname) > 0"  >
							<xsl:comment>G.k.2.2.EU.4: Name part - Strength name</xsl:comment>
							<delimiter qualifier="STR"><xsl:value-of select="drugstrengthname"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(drugformname) > 0"  >
							<xsl:comment>G.k.2.2.EU.5: Name part - Form name</xsl:comment>
							<delimiter qualifier="FRM"><xsl:value-of select="drugformname"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(drugcontainername) > 0"  >
							<xsl:comment>G.k.2.2.EU.6: Name part - Container name</xsl:comment>
							<delimiter qualifier="CON"><xsl:value-of select="drugcontainername"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(drugdevicename) > 0"  >
							<xsl:comment>G.k.2.2.EU.7: Name part - Device name</xsl:comment>
							<delimiter qualifier="DEV"><xsl:value-of select="drugdevicename"/></delimiter>
						</xsl:if>
						<xsl:if test="string-length(drugintendedname) > 0"  >
							<xsl:comment>G.k.2.2.EU.8: Name part - Intended use name</xsl:comment>
							<delimiter qualifier="USE"><xsl:value-of select="drugintendedname"/></delimiter>
						</xsl:if>					
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
                                  <code codeSystem="{$oidISOCountry}" code="{drugauthorizationcountry}"/>
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
                  <code code="{$GestationPeriod}" codeSystem="{$oidObservationCode}" codeSystemVersion="{$emaObservationCLVersion}"/>
                  <value xsi:type="PQ" value="{reactiongestationperiod}" unit="{reactiongestationperiodunit}"/>
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
          <xsl:comment>G.k.2.3.r.2a: Substance / Specified Substance TermID Version Date / Number </xsl:comment>
          <xsl:comment>G.k.2.3.r.2b: Substance / Specified Substance TermID </xsl:comment>
          <xsl:if test="string-length(activesubstancetermid) > 0 and string-length(activesubstancetermidversion) > 0">
            <code code="{activesubstancetermid}" codeSystem="TBD-Substance" codeSystemVersion="{activesubstancetermidversion}" displayName="drugInformation" />
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
                <xsl:when test="(string-length(drugindicationmeddracode) > 0 and string-length(drugindicationmeddraversion) > 0)">
          <xsl:comment>G.k.7.r.2a: MedDRA Version for Indication</xsl:comment>
				  <xsl:comment>G.k.7.r.2b: Indication (MedDRA code)</xsl:comment>			  
				  <xsl:comment>G.k.7.r.1: Indication as Reported by the Primary Source</xsl:comment>				  
          <value xsi:type="CE" code="{drugindicationmeddracode}" codeSystem="{$oidMedDRA}" codeSystemVersion="{drugindicationmeddraversion}">
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
              <id root="{../druguniversallyuniqueid}"/>
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
                  <xsl:comment>G.k.4.r.3: Definition of the Time Interval Unit </xsl:comment>
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
                  <value xsi:type="CE" nullFlavor="{$NullFlavourWOSqBrcktGk4r111}"/>
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
                      <value xsi:type="CE">
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

</xsl:stylesheet>