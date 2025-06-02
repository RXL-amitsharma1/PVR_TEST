<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="urn:hl7-org:v3">


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
								<xsl:when
										test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = concat('00',$code)]) = 1">
									<xsl:value-of
											select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = concat('00',$code)]/@r3"/>
								</xsl:when>
								<xsl:otherwise>{<xsl:value-of select="concat('00',$code)"/>}
								</xsl:otherwise>
							</xsl:choose>
						</xsl:if>
						<xsl:if test="string-length($code) = 2">
							<xsl:choose>
								<xsl:when
										test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = concat('0',$code)]) = 1">
									<xsl:value-of
											select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = concat('0',$code)]/@r3"/>
								</xsl:when>
								<xsl:otherwise>{<xsl:value-of select="$code"/>}
								</xsl:otherwise>
							</xsl:choose>
						</xsl:if>
						<xsl:if test="string-length($code) > 2">
							<xsl:choose>
								<xsl:when
										test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = $code]) = 1">
									<xsl:value-of
											select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = $code]/@r3"/>
								</xsl:when>
								<xsl:otherwise>{<xsl:value-of select="$code"/>}
								</xsl:otherwise>
							</xsl:choose>
						</xsl:if>
					</xsl:when>
					<xsl:otherwise>
						<xsl:choose>
							<xsl:when
									test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = $code]) = 1">
								<xsl:value-of
										select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = $code]/@r3"/>
							</xsl:when>
							<xsl:when
									test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[text()= $code]) = 1">
								<xsl:value-of
										select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[text()= $code]/@r3"/>
							</xsl:when>
							<xsl:when
									test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r3 = $code]) > 0">
								<xsl:value-of
										select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r3 = $code][1]/@r3"/>
							</xsl:when>
							<!--	<xsl:value-of select="$code"/>-->
							<xsl:otherwise>{<xsl:value-of select="translate($code, '', '-')"/>}
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when
							test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = $code]) = 1">
						<xsl:value-of
								select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r2 = $code]/@r3"/>
					</xsl:when>
					<xsl:when
							test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[text()= $code]) = 1">
						<xsl:value-of
								select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[text()= $code]/@r3"/>
					</xsl:when>
					<xsl:when
							test="count(document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r3 = $code]) > 0">
						<xsl:value-of
								select="document('mapping-codes.xml')/mapping-codes/mapping-code[./@type=$type]/code[./@r3 = $code][1]/@r3"/>
					</xsl:when>
					<!--	<xsl:value-of select="$code"/>-->
					<xsl:otherwise>{<xsl:value-of select="translate($code, '', '-')"/>}
					</xsl:otherwise>
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
		<id extension="{messagenumb}" root="{$oidBatchNumber}"/>                                        <!--M.1.4	- Batch Number-->
		<creationTime value="{transmissiondate}"/>
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
					<xsl:when test="messagetype = 'backlog'">
						<name code="1" codeSystem="{$oidEUMessageType}" codeSystemVersion="{$oidEUMessageTypeCSV}"
							  displayName="backlog"/>
					</xsl:when>
					<xsl:when test="messagetype = 'backlogct'">
						<name code="1" codeSystem="{$oidEUMessageType}" codeSystemVersion="{$oidEUMessageTypeCSV}"
							  displayName="backlog"/>
					</xsl:when>
					<xsl:when test="messagetype = 'psur'">
						<name code="1" codeSystem="{$oidMessageType}" codeSystemVersion="{$oidMessageTypeCSV}"
							  displayName="ichicsr"/>
					</xsl:when>
					<xsl:when test="messagetype = 'ctasr'">
						<name code="1" codeSystem="{$oidMessageType}" codeSystemVersion="{$oidMessageTypeCSV}"
							  displayName="ichicsr"/>
					</xsl:when>
					<xsl:when test="messagetype = 'masterichicsr'">
						<name code="2" codeSystem="{$oidEUMessageType}" codeSystemVersion="{$oidEUMessageTypeCSV}"
							  displayName="masterichicsr"/>
					</xsl:when>
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
				<id extension="{messagereceiveridentifier}" root="{$oidMessageReceiverId}"/>
				<xsl:comment>N.2.r.3:Message Receiver Identifier</xsl:comment>
			</device>
		</receiver>
		<sender typeCode="SND">
			<device classCode="DEV" determinerCode="INSTANCE">
				<id extension="{messagesenderidentifier}" root="{$oidMessageSenderId}"/>
				<xsl:comment>N.2.r.2:Message Sender Identifier</xsl:comment>
			</device>
		</sender>
	</xsl:template>

	<!--Batch Footer : M.1.5 and M.1.6-->
	<xsl:template match="ichicsrtransmissionidentification" mode="part-c">
		<receiver typeCode="RCV">
			<device classCode="DEV" determinerCode="INSTANCE">
				<id extension="{messagereceiveridentifier}" root="{$oidBatchReceiverId}"/>
			</device>
		</receiver>
		<xsl:comment>N.1.4:Batch Receiver Identifier</xsl:comment>
		<sender typeCode="SND">
			<device classCode="DEV" determinerCode="INSTANCE">
				<id extension="{messagesenderidentifier}" root="{$oidBatchSenderId}"/>
			</device>
			<xsl:comment>N.1.3:Batch Sender Identifier</xsl:comment>
		</sender>

	</xsl:template>

	<xsl:template match="transmissiondate">
		<xsl:variable name="version-number" select="../safetyreportversion"/>
		<xsl:choose>
			<xsl:when test="string-length($version-number) = 0">
				<effectiveTime value="{.}"/>
			</xsl:when>
			<xsl:when test="string-length($version-number) = 1">
				<effectiveTime value="{.}00000{$version-number}"/>
			</xsl:when>
			<xsl:when test="string-length($version-number) = 2">
				<effectiveTime value="{.}0000{$version-number}"/>
			</xsl:when>
			<xsl:otherwise>
				<effectiveTime value="{.}"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--Report Duplicate :
E2B(R2): element "reportduplicate" inside "safetyreport"
E2B(R3): element "controlActEvent"
-->

	<xsl:template match="reportduplicate">
		<xsl:comment>C.1.9.1.r.1: Source(s) of the Case Identifier (repeat as necessary)</xsl:comment>
		<xsl:comment>C.1.9.1.r.2 Case Identifier(s)</xsl:comment>
		<xsl:if test="string-length(duplicatesource)>0 and string-length(duplicatenumb)>0">
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

	<!--display content of a field, unless it is masked-->
	<xsl:template name="field-or-mask1">
		<xsl:param name="element"/>
		<xsl:param name="value"/>

		<xsl:if test="string-length($value) > 0">
			<xsl:element name="{$element}">
				<xsl:choose>
					<xsl:when test="$value = 'PRIVACY'">
						<xsl:attribute name="nullFlavor">MSK</xsl:attribute>
					</xsl:when>
					<xsl:when test="$value = 'UNKNOWN'">
						<xsl:attribute name="nullFlavor">UNK</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$value"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:element>
		</xsl:if>
	</xsl:template>

	<!-- C.4.r Literature Reference(s) (repeat as necessary)
	E2B(R2): element "literaturereference" inside "ichicsr/ichicsrbatchheader/ichicsrmessageheader/safetyreport"
	E2B(R3): element "reference"	-->

	<xsl:template match="literature">
		<xsl:if test="string-length(.) > 0">
			<xsl:variable name="positionLitRef">
				<xsl:value-of select="position()"/>
			</xsl:variable>
			<xsl:comment>C.4.r: Literature Reference(s) - (<xsl:value-of select="$positionLitRef"/>)</xsl:comment>
			<reference typeCode="REFR">
				<document classCode="DOC" moodCode="EVN">
					<code code="{$literatureReference}" codeSystem="{$oidichreferencesource}"
						  codeSystemVersion="{$ichoidC4rCLVersion}"/>

					<xsl:if test="string-length(mhlwlittestorresearchclass) > 0">
						<xsl:comment>J2.17.r: Classification of Study</xsl:comment>
						<title><xsl:value-of select="mhlwlittestorresearchclass"/></title>
					</xsl:if>

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
								<xsl:when test="string-length(litcompression) > 0">
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

					<!-- J2.15.r: Country of Publication -->
					<xsl:if test="string-length(mhlwlitcountryofpublication) > 0">
						<xsl:comment>J2.15.r: Country of Publication</xsl:comment>
						<participation typeCode="AUT">
							<assignedEntity classCode="ASSIGNED">
								<representedOrganization classCode="ORG" determinerCode="INSTANCE">
									<asLocatedEntity classCode="LOCE">
										<location classCode="COUNTRY" determinerCode="INSTANCE">
											<code code="{mhlwlitcountryofpublication}" codeSystem="{$oidISOCountry}"/>
										</location>
									</asLocatedEntity>
								</representedOrganization>
							</assignedEntity>
						</participation>
					</xsl:if>
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
					<xsl:comment>C.5.3: Sponsor Study Number</xsl:comment>
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
							<id nullFlavor="{$NullFlavourWOSqBrcktC53}" root="{$SponsorStudyNumber}"/>
						</xsl:when>
						<xsl:otherwise>
							<id extension="{sponsorstudynumb}" root="{$SponsorStudyNumber}"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>

				<!-- C.5.4: Study Type Where Reaction(s) / Event(s) Were Observed -->
				<xsl:if test="string-length(observestudytype) > 0">
					<xsl:comment>C.5.4: Study type in which the reaction(s)/event(s) were observed</xsl:comment>
					<code code="{observestudytype}" codeSystem="{$oidStudyType}"
						  codeSystemVersion="{$emaoidC54CLVersion}"/>
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

				<xsl:if test="string-length(indnumb) > 0 ">
					<xsl:variable name="isNullFlavourIndnumb">
						<xsl:call-template name="isNullFlavour">
							<xsl:with-param name="value" select="indnumb"/>
						</xsl:call-template>
					</xsl:variable>
					<authorization typeCode="AUTH">
						<studyRegistration classCode="ACT" moodCode="EVN">
							<xsl:choose>
								<xsl:when test="$isNullFlavourIndnumb = 'yes'">
									<xsl:variable name="NullFlavourWOSqBrcktIndnumb">
										<xsl:call-template name="getNFValueWithoutSqBrckt">
											<xsl:with-param name="nfvalue" select="indnumb"/>
										</xsl:call-template>
									</xsl:variable>
									<id nullFlavor="{$NullFlavourWOSqBrcktIndnumb}" root="{$oidIndNumb}"/>
								</xsl:when>
								<xsl:otherwise>
									<id extension="{indnumb}" root="{$oidIndNumb}"/>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:comment>  FDA.C.5.5a: IND Number where AE Occurred </xsl:comment>
						</studyRegistration>
					</authorization>
				</xsl:if>

				<xsl:if test="string-length(preanda) > 0 ">
					<xsl:variable name="isNullFlavourPreanda">
						<xsl:call-template name="isNullFlavour">
							<xsl:with-param name="value" select="preanda"/>
						</xsl:call-template>
					</xsl:variable>
					<authorization typeCode="AUTH">
						<studyRegistration classCode="ACT" moodCode="EVN">
							<xsl:choose>
								<xsl:when test="$isNullFlavourPreanda = 'yes'">
									<xsl:variable name="NullFlavourWOSqBrcktPreanda">
										<xsl:call-template name="getNFValueWithoutSqBrckt">
											<xsl:with-param name="nfvalue" select="preanda"/>
										</xsl:call-template>
									</xsl:variable>
									<id nullFlavor="{$NullFlavourWOSqBrcktPreanda}" root="{$oidPreAnda}"/>
								</xsl:when>
								<xsl:otherwise>
									<id extension="{preanda}" root="{$oidPreAnda}"/>
								</xsl:otherwise>
							</xsl:choose>
							<xsl:comment>  FDA.C.5.5b: Pre-ANDA Number where AE Occurred  </xsl:comment>
						</studyRegistration>
					</authorization>
				</xsl:if>

				<!-- FDA.C.5.6.r: IND number of cross reported IND -->
				<xsl:apply-templates select="crossreportedindinfo" mode="FDA-cross-reported-ind"/>

			</researchStudy>
		</subjectOf1>
	</xsl:template>

	<!-- C.5.1.r Study Registration (Repeat as necessary) -->
	<xsl:template match="studyregistration" mode="EMA-primary-source">
		<xsl:if test="string-length(studyregnumb) > 0">
			<xsl:variable name="positionStudyReg">
				<xsl:value-of select="position()"/>
			</xsl:variable>
			<xsl:comment>C.5.1.r: Study Registration - (<xsl:value-of select="$positionStudyReg"/>)</xsl:comment>
			<authorization typeCode="AUTH">
				<studyRegistration classCode="ACT" moodCode="EVN">

					<xsl:comment>C.5.1.r.1: Study Registration Number</xsl:comment>
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

								<xsl:comment>C.5.1.r.2: Study Registration Country</xsl:comment>
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
								<xsl:comment>D.1.1.4: Patient Medical Record Number(s) and Source(s) of the Record Number (Investigation Number)</xsl:comment>
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
								<code code="{$TestsAndProceduresRelevantToTheInvestigation}"
									  codeSystem="{$oidValueGroupingCode}"
									  displayName="testsAndProceduresRelevantToTheInvestigation"/>
								<xsl:apply-templates select="../test" mode="EMA-lab-test"/>
								<xsl:apply-templates select="resulttestprocedures"/>

							</organizer>
						</subjectOf2>
						<!--</xsl:if>-->


						<!--B.4.k - Drug (main)-->
						<subjectOf2 typeCode="SBJ">
							<organizer classCode="CATEGORY" moodCode="EVN">
								<code code="{$DrugInformation}" codeSystem="{$oidValueGroupingCode}"
									  displayName="drugInformation"/>
								<xsl:apply-templates select="../drug" mode="main"/>
							</organizer>
						</subjectOf2>

						<xsl:if test="count(../../mhlwadminitemsicsr/mhlwabstractofstudy) > 0">
							<xsl:apply-templates select="../../mhlwadminitemsicsr/mhlwabstractofstudy" mode="mhlwabstractofstudy-a"/>
						</xsl:if>

					</primaryRole>
				</subject1>
				<!--B.4.k - Drug (causality)-->
				<xsl:apply-templates select="../drug" mode="EMA-causality"/>

				<!--B.5 - Summary-->
				<xsl:apply-templates select="../summary"/>
			</adverseEventAssessment>
		</component>
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
				<xsl:if test="string-length(parentmedicalepisodename) > 0 ">
					<xsl:comment>D.10.7.1.r.1a: MedDRA version for parent medical history</xsl:comment>
					<xsl:comment>D.10.7.1.r.1b: Medical History (disease / surgical procedure/ etc.) (MedDRA code)</xsl:comment>
					<code code="{parentmedicalepisodename}" codeSystemVersion="{parentmdepisodemeddraversion}"
						  codeSystem="{$oidMedDRA}"/>
				</xsl:if>

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
							<code code="{$Comment}" codeSystem="{$oidObservationCode}"
								  codeSystemVersion="{$emaObservationCLVersion}"/>
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
							<code code="{$Continuing}" codeSystem="{$oidObservationCode}"
								  codeSystemVersion="{$emaObservationCLVersion}"/>
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

	<xsl:template match="medicalhistoryepisode" mode="EMA-pat-medical-history-episode">
		<xsl:variable name="positionPatMedEpi">
			<xsl:value-of select="position()"/>
		</xsl:variable>
		<xsl:if test="string-length(patientepisodename) > 0">
			<xsl:comment>D.7.1.r: Relevant Medical History and Concurrent Conditions (not including reaction / event) - (<xsl:value-of select="$positionPatMedEpi"/>)</xsl:comment>
			<component typeCode="COMP">
				<observation moodCode="EVN" classCode="OBS">
					<!--D.7.1.r.1a MedDRA Version for Medical History -->
					<!--D.7.1.r.1b Medical History (disease / surgical procedure / etc.) (MedDRA code) -->
					<xsl:if test="string-length(patientepisodename) > 0">
						<xsl:comment>D.7.1.r.1a: MedDRA Version for Medical History</xsl:comment>
						<xsl:comment>D.7.1.r.1b: Medical History (disease / surgical procedure / etc.) (MedDRA code)</xsl:comment>
						<code code="{patientepisodename}" codeSystemVersion="{patientepisodenamemeddraversion}"
							  codeSystem="{$oidMedDRA}"/>
					</xsl:if>

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
								<code code="{$Comment}" codeSystem="{$oidObservationCode}"
									  codeSystemVersion="{$emaObservationCLVersion}"/>
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
								<code code="{$FamilyHistory}" codeSystem="{$oidObservationCode}"
									  codeSystemVersion="{$emaoidD71r6CLVersion}"/>
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
								<code code="{$Continuing}" codeSystem="{$oidObservationCode}"
									  codeSystemVersion="{$emaObservationCLVersion}"/>
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
		</xsl:if>
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
					<xsl:when test="$value = '[OTH]'">yes</xsl:when>
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
					<xsl:when test="$nfvalue = '[OTH]'">OTH</xsl:when>
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

	<!-- New logic for Death -->
	<xsl:template match="patientdeathcause">
		<xsl:variable name="positionPatDeathCause">
			<xsl:value-of select="position()"/>
		</xsl:variable>
		<xsl:comment>D.9.2.r: Reported Cause(s) of Death - (<xsl:value-of select="$positionPatDeathCause"/>)</xsl:comment>
		<subjectOf2 typeCode="SBJ">
			<observation moodCode="EVN" classCode="OBS">
				<code code="{$ReportedCauseOfDeath}" codeSystem="{$oidObservationCode}"
					  codeSystemVersion="{$emaObservationCLVersion}"/>
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
							<xsl:if test="string-length(patientdeathreportmeddraversion) > 0">
								<xsl:attribute name="codeSystemVersion">
									<xsl:value-of select="patientdeathreportmeddraversion"/>
								</xsl:attribute>
							</xsl:if>
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
				<xsl:value-of
						select="normalize-space(translate(translate(translate(testresult,'=',''),'&gt;',''),'&lt;',''))"
						disable-output-escaping="yes"/>
			</xsl:variable>
			<xsl:choose>
				<xsl:when test="number(normalize-space($vartestresultValue)) or number(normalize-space(translate($vartestresultValue,'+',''))) >=0 ">
					<xsl:choose>
						<xsl:when
								test="not(contains($vartestresult,'&lt;')) and not(contains($vartestresult,'=')) and not(contains($vartestresult,'&gt;'))">
							<center value="{normalize-space($vartestresultValue)}" unit="{testunit}"/>
						</xsl:when>
						<xsl:when
								test="not(contains($vartestresult,'&lt;')) and contains($vartestresult,'=') and not(contains($vartestresult,'&gt;'))">
							<center value="{normalize-space($vartestresultValue)}" unit="{testunit}"/>
						</xsl:when>
						<xsl:when
								test="contains($vartestresult,'&lt;') and not(contains($vartestresult,'=')) and not(contains($vartestresult,'&gt;'))">
							<low nullFlavor="NINF"/>
							<high value="{normalize-space($vartestresultValue)}" unit="{testunit}" inclusive="false"/>
						</xsl:when>
						<xsl:when
								test="contains($vartestresult,'&lt;') and contains($vartestresult,'=') and not(contains($vartestresult,'&gt;'))">
							<low nullFlavor="NINF"/>
							<high value="{normalize-space($vartestresultValue)}" unit="{testunit}" inclusive="true"/>
						</xsl:when>
						<xsl:when
								test="not(contains($vartestresult,'&lt;')) and not(contains($vartestresult,'=')) and contains($vartestresult,'&gt;')">
							<low value="{normalize-space($vartestresultValue)}" unit="{testunit}" inclusive="false"/>
							<high nullFlavor="PINF"/>
						</xsl:when>
						<xsl:when
								test="not(contains($vartestresult,'&lt;')) and contains($vartestresult,'=') and contains($vartestresult,'&gt;')">
							<low value="{normalize-space($vartestresultValue)}" unit="{testunit}" inclusive="true"/>
							<high nullFlavor="PINF"/>
						</xsl:when>
					</xsl:choose>
				</xsl:when>
			</xsl:choose>
		</value>
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
			<xsl:comment>G.k.10.r: Additional Information on Drug (coded) - (<xsl:value-of select="$positionDrugAddInfo"/>)</xsl:comment>
			<outboundRelationship2 typeCode="REFR">
				<observation classCode="OBS" moodCode="EVN">
					<code code="{$CodedDrugInformation}" codeSystem="{$oidObservationCode}"
						  codeSystemVersion="{$emaObservationCLVersion}"/>
					<xsl:comment>G.k.10.r: Additional Information on Drug (coded)(repeat as necessary) code system version</xsl:comment>
					<value xsi:type="CE" code="{drugadditionalcode}" codeSystem="{$AdditionalInformationOnDrug}"
						   codeSystemVersion="{$emaoidGk10rCLVersion}"/>
				</observation>
			</outboundRelationship2>
		</xsl:if>
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
		<xsl:variable name="did">
			<xsl:value-of select="druguniversallyuniqueid"/>
		</xsl:variable>
		<!--G.k.1 Characterisation of Drug Role -->
		<xsl:if test="string-length(drugcharacterization)>0">
			<xsl:comment>G.k.1: Characterization of Drug Role</xsl:comment>
			<component typeCode="COMP">
				<causalityAssessment classCode="OBS" moodCode="EVN">
					<code code="{$InterventionCharacterization}" codeSystem="{$oidObservationCode}"
						  codeSystemVersion="{$emaObservationCLVersion}"/>
					<value xsi:type="CE" code="{drugcharacterization}" codeSystem="{$oidDrugRole}"
						   codeSystemVersion="{$emaoidGk1CLVersion}"/>
					<xsl:comment>G.k[GID]: Drug UUID</xsl:comment>
					<subject2 typeCode="SUBJ">
						<productUseReference classCode="SBADM" moodCode="EVN">
							<id root="{normalize-space($did)}"/>
						</productUseReference>
					</subject2>
				</causalityAssessment>
			</component>
		</xsl:if>

		<!--FDA.G.k.1.a: FDA Other Characterisation of Drug Role -->
		<xsl:if test="string-length(otherdrugcharacterization)>0">
			<component typeCode="COMP">
				<causalityAssessment classCode="OBS" moodCode="EVN">
					<code code="{$InterventionCharacterization}" codeSystem="{$oidObservationCode}" displayName="Other Drug Characterization"/>
					<value xsi:type="CE" code="{otherdrugcharacterization}" codeSystem="2.16.840.1.113883.3.989.5.1.2.1.1.8"/>
					<xsl:comment>FDA.G.k.1.a: FDA Other Characterisation of Drug Role</xsl:comment>
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


	<!--EU Reference instance  - EU Causality assessment (repeat as necessary)
 E2B(R2): element "drugassesment" - "ichicsr/ichicsrbatchheader/ichicsrmessageheader/safetyreport/drug/drugeventmatrix"
 E2B(R3): element "causalityAssessment"
 -->
	<xsl:template match="drugassesment" mode="EMA-eu-causality-assessment">
		<xsl:param name="drugRef"/>
		<xsl:if test="string-length(eventuniversallyuniqueid) > 0">
		<xsl:if test="string-length(eudrugassessmentsource) > 0 or string-length(eudrugassessmentmethod) > 0 or string-length(eudrugresult) > 0">
			<xsl:variable name="positionEUDrugReactRel">
				<xsl:value-of select="position()"/>
			</xsl:variable>
			<xsl:comment>EU Reference instance - EU Causality assessment - (<xsl:value-of select="$positionEUDrugReactRel"/>)</xsl:comment>
			<component typeCode="COMP">
				<causalityAssessment classCode="OBS" moodCode="EVN">
					<code code="{$Causality}" codeSystem="{$oidObservationCode}"
						  codeSystemVersion="{$emaObservationCLVersion}"/>
					<xsl:comment>G.k.9.i.2.r.3.EU.1: Result of Assessment - captured in the code field</xsl:comment>
					<xsl:if test="string-length(eudrugresult) > 0">
						<value xsi:type="CE" code="{eudrugresult}" codeSystem="{$oidEUResultOfAssessmentCode}"
							   codeSystemVersion="1.0"/>
					</xsl:if>
					<xsl:comment>G.k.9.i.2.r.2.EU.1: EU Method of assessment - captured in the code field</xsl:comment>
					<xsl:if test="string-length(eudrugassessmentmethod) > 0">
						<methodCode code="{eudrugassessmentmethod}" codeSystem="{$oidEUMethodOfAssessmentCode}"
									codeSystemVersion="1.0"/>
					</xsl:if>
					<xsl:if test="string-length(eudrugassessmentsource) > 0">
						<author typeCode="AUT">
							<assignedEntity classCode="ASSIGNED">
								<xsl:comment>G.k.9.i.2.r.1.EU.1: EU Source of assessment - captured in the code field</xsl:comment>
								<code code="{eudrugassessmentsource}" codeSystem="{$oidEUSourceOfAssessmentCode}"
									  codeSystemVersion="1.0"/>
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
									<code code="{devicecomponenttermid}" codeSystem="EUOID"
										  codeSystemVersion="{devicecomponenttermidversion}"/>
								</xsl:when>
								<xsl:otherwise>
									<code/>
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

</xsl:stylesheet>
