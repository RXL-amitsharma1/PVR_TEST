<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="urn:hl7-org:v3">

	<xsl:variable name="oidISOCountry">1.0.3166.1.2.2</xsl:variable>
	<xsl:variable name="oidGenderCode">1.0.5218</xsl:variable>
	<xsl:variable name="oidMedDRA">2.16.840.1.113883.6.163</xsl:variable>
	<xsl:variable name="oidObservationCode">2.16.840.1.113883.3.989.2.1.1.19</xsl:variable>

	<xsl:variable name="oidAssignedEntityRoleCode">2.16.840.1.113883.3.989.2.1.1.21</xsl:variable>
	<xsl:variable name="oidActionPerformedCode">2.16.840.1.113883.3.989.2.1.1.18</xsl:variable>
	<xsl:variable name="oidReportRelationCode">2.16.840.1.113883.3.989.2.1.1.22</xsl:variable>
	<xsl:variable name="oidReportCharacterizationCode">2.16.840.1.113883.3.989.2.1.1.23</xsl:variable>
	<!--B.3 - Test-->

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
	<xsl:variable name="AdditionalDocumentsAvailable">1</xsl:variable>
	<!--A.1.9-->
	<xsl:variable name="LocalCriteriaForExpedited">23</xsl:variable>
	<!--A.1.10.1/12-->

	<xsl:variable name="oidWorldWideCaseID">2.16.840.1.113883.3.989.2.1.3.2</xsl:variable>
	<!--A.1.10.2-->
	<xsl:variable name="InitialReport">1</xsl:variable>
	<xsl:variable name="oidFirstSender">2.16.840.1.113883.3.989.2.1.1.3</xsl:variable>
	<xsl:variable name="OtherCaseIDs">2</xsl:variable>
	<!--A.1.11.r.2-->
	<xsl:variable name="oidCaseIdentifier">2.16.840.1.113883.3.989.2.1.3.3</xsl:variable>
	<!--A.1.13-->
	<xsl:variable name="NullificationAmendmentCode">3</xsl:variable>

	<!--A.2 - Primary Source-->
	<!--A.2-->
	<xsl:variable name="SourceReport">2</xsl:variable>
	<!--A.2.r.1.4-->
	<xsl:variable name="oidQualification">2.16.840.1.113883.3.989.2.1.1.6</xsl:variable>
	<!--A.3 - Sender-->
	<!--A.3.1-->
	<xsl:variable name="oidSenderType">2.16.840.1.113883.3.989.2.1.1.7</xsl:variable>
	<!--A.5 - Study Identification-->
	<!--A.5-->
	<xsl:variable name="SponsorStudyNumber">2.16.840.1.113883.3.989.2.1.3.5</xsl:variable>
	<!--A.5.1.r.1-->
	<xsl:variable name="StudyRegistrationNumber">2.16.840.1.113883.3.989.2.1.3.6</xsl:variable>
	<!--A.5.4-->
	<xsl:variable name="oidStudyType">2.16.840.1.113883.3.989.2.1.1.8</xsl:variable>
	<!--A.1.13-->
	<xsl:variable name="NullificationAmendmentCode">3</xsl:variable>
	<xsl:variable name="oidNullificationAmendment">2.16.840.1.113883.3.989.2.1.1.5</xsl:variable>
	<xsl:variable name="oidNullificationAmendmentCSV">2.0</xsl:variable>
	<!--A.1.13.1-->
	<xsl:variable name="NullificationAmendmentReason">4</xsl:variable>
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
	<!--B.1.3-->
	<xsl:variable name="BodyWeight">7</xsl:variable>
	<!--B.1.4-->
	<xsl:variable name="Height">17</xsl:variable>
	<!--B.1.6-->
	<xsl:variable name="LastMenstrualPeriodDate">22</xsl:variable>
	<!--B.1.10-->
	<xsl:variable name="Parent">PRN</xsl:variable>
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
	<!--B.2.i.0.b-->
	<xsl:variable name="ReactionForTranslation">30</xsl:variable>
	<!--B.2.i.2.1-->
	<xsl:variable name="TermHighlightedByReporter">37</xsl:variable>
	<xsl:variable name="oidTermHighlighted">2.16.840.1.113883.3.989.2.1.1.10</xsl:variable>
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
	<!--B.4.k.2.4-->
	<xsl:variable name="RetailSupply">1</xsl:variable>
	<!--B.4.k.2.5-->
	<xsl:variable name="Blinded">6</xsl:variable>
	<!--B.4.k.3-->
	<xsl:variable name="oidAuthorisationNumber">2.16.840.1.113883.3.989.2.1.3.4</xsl:variable>
	<!--B.4.k.4.r.12/13-->
	<xsl:variable name="oidICHRoute">0.4.0.127.0.16.1.1.2.6</xsl:variable>
	<!--B.4.k.4.r.13.2-->
	<xsl:variable name="ParentRouteOfAdministration">28</xsl:variable>
	<!--B.4.k.5.1-->
	<xsl:variable name="CumulativeDoseToReaction">14</xsl:variable>
	<!--B.4.k.7-->
	<xsl:variable name="oidICHFORM">0.4.0.127.0.16.1.1.2.1</xsl:variable>
	<xsl:variable name="SourceReporter">3</xsl:variable>
	<!--B.4.k.8-->
	<xsl:variable name="oidActionTaken">2.16.840.1.113883.3.989.2.1.1.15</xsl:variable>
	<!--B.4.k.9.i.2-->
	<xsl:variable name="Causality">39</xsl:variable>
	<!--B.4.k.9.i.4-->
	<xsl:variable name="RecurranceOfReaction">31</xsl:variable>
	<xsl:variable name="oidRechallenge">2.16.840.1.113883.3.989.2.1.1.16</xsl:variable>
	<!--B.4.k.10-->
	<xsl:variable name="CodedDrugInformation">9</xsl:variable>
	<xsl:variable name="AdditionalCodedDrugInformation">2</xsl:variable>
	<xsl:variable name="AdditionalInformationOnDrug">2.16.840.1.113883.3.989.2.1.1.17</xsl:variable>
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
	<xsl:variable name="ichoidC4rCLVersion">2.0</xsl:variable>

	<!-- C.1.6.1 Add Doc  -->
	<xsl:variable name="ichObservationCLVersion">2.0</xsl:variable>

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

	<xsl:variable name="OidISOCountry">1.0.3166.1.2.2</xsl:variable>


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
	<!--B.5 - Summary-->
	<!--B.5.3-->
	<xsl:variable name="Diagnosis">15</xsl:variable>
	<xsl:variable name="Sender">1</xsl:variable>
	<!--B.5.5-->
	<xsl:variable name="SummaryAndComment">36</xsl:variable>
	<xsl:variable name="Reporter">2</xsl:variable>
	<xsl:variable name="receiptDate">1</xsl:variable>
	<!--EU Specific Data fields-->
	<xsl:variable name="oidEUMessageType">2.16.840.1.113883.3.989.5.1.1.5.1</xsl:variable>
	<xsl:variable name="oidEUMessageTypeCSV">1.0</xsl:variable>
	<xsl:variable name="oidPMDAReportType">2.16.840.1.113883.3.989.5.1.3.2.1.1</xsl:variable>
	<xsl:variable name="oidPMDAReportTypeCSV">1.1</xsl:variable>

	<xsl:variable name="ichoidAssignedEntityRoleCodeVersion">2.0</xsl:variable>

	<!--NMPA (China) Specific Data fields-->
	<xsl:variable name="ReportSource">CN-6</xsl:variable>
	<xsl:variable name="nmpaoidC1CN1LVersion">1.1</xsl:variable>
	<xsl:variable name="ReportCategory">CN-11</xsl:variable>
	<xsl:variable name="MAHID">CN-12</xsl:variable>
	<xsl:variable name="PatientMinority">CN-1</xsl:variable>
	<xsl:variable name="PatienTrace">CN-2</xsl:variable>
	<xsl:variable name="MedicalInstitutionName">CN-3</xsl:variable>
	<xsl:variable name="PatientNationality">CN-9</xsl:variable>
	<xsl:variable name="PatientTelephone">CN-10</xsl:variable>
	<xsl:variable name="PregnancyDescription">CN-13</xsl:variable>
	<xsl:variable name="CNGenericName">CN-7</xsl:variable>
	<xsl:variable name="RelatedDevice">CN-14</xsl:variable>
	<xsl:variable name="CNMarketAuthHolder">CN-15</xsl:variable>
	<xsl:variable name="CNApprovalNumber">CN-16</xsl:variable>
	<xsl:variable name="RecurranceOfListedness">CN-4</xsl:variable>
	<xsl:variable name="DeChallenge">CN-5</xsl:variable>
	<xsl:variable name="DrugExpiryDate">CN-8</xsl:variable>
	<xsl:variable name="CNResultAssess">39</xsl:variable>
	<xsl:variable name="oidCNresultassess">CN-CSV</xsl:variable>
	<xsl:variable name="nmpaoidEi7CNResultAssessCLVersion">1.0</xsl:variable>

	<!-- ICH standard variable-->
	<xsl:variable name="ichoidObservationCLVersion">2.0</xsl:variable>
	<xsl:variable name="ichoidGk9i4CLVersion">2.0</xsl:variable>
	<xsl:variable name="ichReportRelationCLVersion">2.0</xsl:variable>
	<xsl:variable name="ichoidC182CLVersion">2.0</xsl:variable>
	<xsl:variable name="ichReportCharacterizationCLVersion">1.0</xsl:variable>
	<xsl:variable name="ichoidC2r4CLVersion">2.0</xsl:variable>
	<xsl:variable name="ichValueGroupingCLVersion">2.0</xsl:variable>
	<xsl:variable name="ichoidD73CLVersion">2.0</xsl:variable>
	<xsl:variable name="ichSourceMedicalRecordCLVersion">2.0</xsl:variable>
	<xsl:variable name="ichoidEi31TermHighlightedCLVersion">2.0</xsl:variable>
	<xsl:variable name="ichoidEi7OutcomeCLVersion">2.0</xsl:variable>
	<xsl:variable name="ichoidFr31CLVersion">2.0</xsl:variable>
	<xsl:variable name="ichoidGk24CLVersion">2.0</xsl:variable>
	<xsl:variable name="ichoidGk8CLVersion">2.0</xsl:variable>
	<xsl:variable name="ichoidAssignedEntityCodeVersion">2.0</xsl:variable>

	<!--EMDR varible declaration -->
	<xsl:variable name="emdrOidObservationCode">2.16.840.1.113883.3.26.1.1</xsl:variable>
	<xsl:variable name="emdrOidRootCode">2.16.840.1.113883.3.24</xsl:variable>
	<xsl:variable name="emdrOidInteractionId">2.16.840.1.113883.1.6</xsl:variable>
	<!--  -->

	<!-- PMDA standard variable -->
	<xsl:variable name="MhlwStartDateReportingTimefram">2</xsl:variable>
	<xsl:variable name="pmdaoidReportingTimeFrameCode">2.16.840.1.113883.3.989.5.1.3.2.1.12</xsl:variable>
	<xsl:variable name="MhlwsttdtReportingTimeframCmnt">3</xsl:variable>
	<xsl:variable name="RetrospectiveAnalysisInfection">4</xsl:variable>
	<xsl:variable name="pmdaoidRetrospectiveAnalysisCode">2.16.840.1.113883.3.989.5.1.3.2.1.10</xsl:variable>
	<xsl:variable name="pmdaoidObservationCLVersion">1.1</xsl:variable>
	<xsl:variable name="FutureMeasures">5</xsl:variable>
	<xsl:variable name="OtherReferencesItem">6</xsl:variable>
	<xsl:variable name="SummaryReportContent">12</xsl:variable>
	<xsl:variable name="pmdaRemark1">13</xsl:variable>
	<xsl:variable name="pmdaRemark2">14</xsl:variable>
	<xsl:variable name="pmdaRemark3">15</xsl:variable>
	<xsl:variable name="pmdaRemark4">16</xsl:variable>
	<xsl:variable name="MhlwStatusCategoryOfNewDrugs">1</xsl:variable>
	<xsl:variable name="pmdaoidDrugCategoryStatusCode">2.16.840.1.113883.3.989.5.1.3.2.1.3</xsl:variable>
	<xsl:variable name="pmdaoidDrugCategoryStatusCLVersion">1.2</xsl:variable>
	<xsl:variable name="pmdaoidClinicalTrialCode">2.16.840.1.113883.3.989.5.1.3.2.1.11</xsl:variable>
	<xsl:variable name="pmdaoidPhaseOfStudiesCode">2.16.840.1.113883.3.989.5.1.3.2.1.7</xsl:variable>
	<xsl:variable name="MhlwAdmicsrCaseNumClassr3">1</xsl:variable>
	<xsl:variable name="MhlwAdmIcsrCommentsIncomplete">3</xsl:variable>
	<xsl:variable name="MhlwFlagForUrgentReport">4</xsl:variable>
	<xsl:variable name="pmdaoidUrgentReportCode">2.16.840.1.113883.3.989.5.1.3.2.1.2</xsl:variable>
	<xsl:variable name="oidTestRangeCode">2.16.840.1.113883.5.83</xsl:variable>
	<xsl:variable name="oidKnownUnknownCode">2.16.840.1.113883.3.989.5.1.3.2.1.8</xsl:variable>
	<xsl:variable name="pmdaoidDrugCategoryRiskCode">2.16.840.1.113883.3.989.5.1.3.2.1.4</xsl:variable>

	<!--   FDA E2b R3 variables -->

	<!-- Batch Wrapper -->
	<xsl:variable name="oidBatchHeaderInteractionId">2.16.840.1.113883.1.6</xsl:variable>
	<!-- @@@C.1.6.1.r: Documents Held by Sender-->
	<xsl:variable name="fdaOidC161rCLVersion">1.0</xsl:variable>
	<!--FDA.C.1.7.1: Local Criteria Report Type-->
	<xsl:variable name="oidLocalCriteriaReportTypeCode">2.16.840.1.113883.3.26.1.1</xsl:variable>
	<xsl:variable name="oidLocalCriteriaReportTypeValue">2.16.840.1.113883.3.989.5.1.2.2.1.1.1</xsl:variable>
	<!-- FDA.C.1.12 Combination Product Report Indicator -->
	<xsl:variable name="oidCombinationProductReport">2.16.840.1.113883.3.26.1.1</xsl:variable>
	<!-- FDA.C.5.5a: IND Number where AE Occurred -->
	<xsl:variable name="oidIndNumb">2.16.840.1.113883.3.989.5.1.2.2.1.2.1</xsl:variable>
	<!-- FDA.C.5.5b: Pre-ANDA Number where AE Occurred -->
	<xsl:variable name="oidPreAnda">2.16.840.1.113883.3.989.5.1.2.2.1.2.2</xsl:variable>
	<!-- FDA.C.5.6.r: IND number of cross reported IND -->
	<xsl:variable name="oidCrossReportedInd">2.16.840.1.113883.3.989.5.1.2.2.1.2.3</xsl:variable>
	<!-- D.8.r Relevant Past Drug History (repeat as necessary) -->
	<xsl:variable name="oidProdIdentifier">2.16.840.1.113883.6.69</xsl:variable>
	<!-- FDA.G.k.13.r: FDA Specialized Product Category -->
	<xsl:variable name="oidSpprodcategory">2.16.840.1.113883.3.26.1.1</xsl:variable>
	<!-- FDA.G.k.13.r: FDA Specialized Product Category -->
	<xsl:variable name="Spprodcategory">C94031</xsl:variable>
	<!-- FDA.G.k.10a FDA Additional Information on Drug  -->
	<xsl:variable name="FDAAdditionalInformationOnDrug">2.16.840.1.113883.3.989.5.1.2.1.1.7</xsl:variable>
	<!-- FDA.D.12: Patient Ethnicity Group -->
	<xsl:variable name="oidPatientEthnicityGroup">2.16.840.1.113883.3.26.1.1</xsl:variable>
	<!--FDA.E.i.3.2h:  Required Intervention-->
	<xsl:variable name="oidReqiredIntervention">2.16.840.1.113883.3.989.5.1.2.2.1.3</xsl:variable>


</xsl:stylesheet>