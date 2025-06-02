<!-- ===========================================================================
	incident_out_<version>.xsl

 	Vittorio Reina, JRC, 2018-10-31
	
	Walter Schnell, for Optimal Systems, 2009-11-16
	Projekt MEDDEV3 for BfArM

	the xslt-out transformation that is used in the MEDDEV PDF form
	basically, these are the things that Acrobat cannot do in LiveCycle Designer:
	- fill multiple tags from one form field
	- set the xsd-attribute to the top level tag (incident)

	ideas:
	set to UTF-16 later
 
 	history (incomplete):
	7.2.1	2020-03-03  VR  new name of xsd files
	7.0 	2018-10-31  VR  schemas V7.0
  5.0 	2018-05-30  VR  schemas V5.0
  4.0 	2017-12-14  VR  schemas excludeTagEcMirV4.0
	3.6		2013-06-05	WS	schemas V2.7;
	3.5		2012-12-03	WS	schemas V2.6;
	3.4		2012-11-23	WS	schemas V2.5;
	3.3		2012-02-22	WS	coutries doubles on reimport corrected; version + SchemaLocation updated;
	3.2		2012-01-25	WS	noNamespaceSchemaLocation without path;
	3.1		2011-12-12	WS	eliminiate reporterOtherText if statusReporter is not "Others"; 
	3.0		2011-09-27	WS	shorter delete of empties; 
 
=========================================================================== -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" indent="yes" encoding="UTF-8" /> 

<!-- switch the contact info so it will be interpreted by the XSD according to the statusReporter field -->
<xsl:template match="/incident/admin_info/contact_info/reporterMfr">
	<xsl:variable name="value" select="/incident/admin_info/contact_info/reporterMfr/statusReporter" />
	<xsl:choose>
		<xsl:when test="$value = 'Manufacturer'">
			<reporterMfr>
				<xsl:apply-templates/>
			</reporterMfr>
		</xsl:when>
		<xsl:when test="$value = 'Authorised representative'">
			<reporterAR>
				<xsl:apply-templates/>
			</reporterAR>
		</xsl:when>
		<xsl:when test="$value = 'Others'">
			<reporterOther>
				<xsl:apply-templates/>
			</reporterOther>
		</xsl:when>
	</xsl:choose>
</xsl:template>

<xsl:template match="reporterOtherText">
	<xsl:variable name="value" select="/incident/admin_info/contact_info/reporterMfr/statusReporter" />
	<xsl:if test="$value = 'Others'">
		<reporterOtherText>
			<xsl:apply-templates/>
		</reporterOtherText>
	</xsl:if>
</xsl:template>


<!-- this code allows to have multiple items in the filed ncaRefMultiDev separated with ; -->
<!-- in this case new elements ncaRefMultiDevLI are created -->
<xsl:template match="/incident/admin_info/ncaRefMultiDev">
  <ncaRefMultiDev><xsl:value-of select="/incident/admin_info/ncaRefMultiDev" /></ncaRefMultiDev>
  <xsl:call-template name="ncaRefMultiDevList">
    <xsl:with-param name="list"><xsl:value-of select="/incident/admin_info/ncaRefMultiDev" /></xsl:with-param>
    <xsl:with-param name="delimiter">;</xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template name="ncaRefMultiDevList">
  <xsl:param name="list" />
  <xsl:param name="delimiter" />
  <xsl:variable name="newlist">
    <xsl:choose>
      <xsl:when test="contains($list, $delimiter)"><xsl:value-of select="normalize-space($list)" /></xsl:when>   
       <xsl:otherwise><xsl:value-of select="concat(normalize-space($list), $delimiter)"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="first" select="substring-before($newlist, $delimiter)" />
  <xsl:variable name="remaining" select="substring-after($newlist, $delimiter)" />
  
  <ncaRefMultiDevLI><xsl:value-of select="$first" /></ncaRefMultiDevLI>
  
  <xsl:if test="$remaining">
    <xsl:call-template name="ncaRefMultiDevList">
      <xsl:with-param name="list" select="$remaining" />
      <xsl:with-param name="delimiter"><xsl:value-of select="$delimiter"/></xsl:with-param>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

    <xsl:template match="/incident/device_info/distribution/distributionEEA">
        <!--<distributionEEA><xsl:value-of select="/incident/device_info/distribution/distributionEEA" /></distributionEEA>-->
        <xsl:call-template name="distributionEEAList">
            <xsl:with-param name="list"><xsl:value-of select="/incident/device_info/distribution/distributionEEA" /></xsl:with-param>
            <xsl:with-param name="delimiter">;</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="distributionEEAList">
        <xsl:param name="list" />
        <xsl:param name="delimiter" />
        <xsl:variable name="newlist">
            <xsl:choose>
                <xsl:when test="contains($list, $delimiter)"><xsl:value-of select="normalize-space($list)" /></xsl:when>
                <xsl:otherwise><xsl:value-of select="concat(normalize-space($list), $delimiter)"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="first" select="substring-before($newlist, $delimiter)" />
        <xsl:variable name="remaining" select="substring-after($newlist, $delimiter)" />

        <distributionEEA><xsl:value-of select="$first" /></distributionEEA>

        <xsl:if test="$remaining">
            <xsl:call-template name="distributionEEAList">
                <xsl:with-param name="list" select="$remaining" />
                <xsl:with-param name="delimiter"><xsl:value-of select="$delimiter"/></xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

<!-- this code allows to have multiple items in the filed eudamedRefMultiDev separated with ; -->
<!-- in this case new elements eudamedRefMultiDevLI are created -->
<xsl:template match="/incident/admin_info/eudamedRefMultiDev">
  <eudamedRefMultiDev><xsl:value-of select="/incident/admin_info/eudamedRefMultiDev" /></eudamedRefMultiDev>
  <xsl:call-template name="eudamedRefMultiDevList">
    <xsl:with-param name="list"><xsl:value-of select="/incident/admin_info/eudamedRefMultiDev" /></xsl:with-param>
    <xsl:with-param name="delimiter">;</xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template name="eudamedRefMultiDevList">
  <xsl:param name="list" />
  <xsl:param name="delimiter" />
  <xsl:variable name="newlist">
    <xsl:choose>
      <xsl:when test="contains($list, $delimiter)"><xsl:value-of select="normalize-space($list)" /></xsl:when>   
       <xsl:otherwise><xsl:value-of select="concat(normalize-space($list), $delimiter)"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="first" select="substring-before($newlist, $delimiter)" />
  <xsl:variable name="remaining" select="substring-after($newlist, $delimiter)" />
  
  <eudamedRefMultiDevLI><xsl:value-of select="$first" /></eudamedRefMultiDevLI>
  
  <xsl:if test="$remaining">
    <xsl:call-template name="eudamedRefMultiDevList">
      <xsl:with-param name="list" select="$remaining" />
      <xsl:with-param name="delimiter"><xsl:value-of select="$delimiter"/></xsl:with-param>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- this code allows to have multiple items in the filed mfrRefMultiDev separated with ; -->
<!-- in this case new elements mfrRefMultiDevLI are created -->
<xsl:template match="/incident/admin_info/mfrRefMultiDev">
  <mfrRefMultiDev><xsl:value-of select="/incident/admin_info/mfrRefMultiDev" /></mfrRefMultiDev>
  <xsl:call-template name="mfrRefMultiDevList">
    <xsl:with-param name="list"><xsl:value-of select="/incident/admin_info/mfrRefMultiDev" /></xsl:with-param>
    <xsl:with-param name="delimiter">;</xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template name="mfrRefMultiDevList">
  <xsl:param name="list" />
  <xsl:param name="delimiter" />
  <xsl:variable name="newlist">
    <xsl:choose>
      <xsl:when test="contains($list, $delimiter)"><xsl:value-of select="normalize-space($list)" /></xsl:when>   
       <xsl:otherwise><xsl:value-of select="concat(normalize-space($list), $delimiter)"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="first" select="substring-before($newlist, $delimiter)" />
  <xsl:variable name="remaining" select="substring-after($newlist, $delimiter)" />
  
  <mfrRefMultiDevLI><xsl:value-of select="$first" /></mfrRefMultiDevLI>
  
  <xsl:if test="$remaining">
    <xsl:call-template name="mfrRefMultiDevList">
      <xsl:with-param name="list" select="$remaining" />
      <xsl:with-param name="delimiter"><xsl:value-of select="$delimiter"/></xsl:with-param>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- this code allows to have multiple items in the filed ncaRefFSCA separated with ; -->
<!-- in this case new elements ncaRefFSCALI are created -->
<xsl:template match="/incident/admin_info/ncaRefFSCA">
  <ncaRefFSCA><xsl:value-of select="/incident/admin_info/ncaRefFSCA" /></ncaRefFSCA>
  <xsl:call-template name="ncaRefFSCAList">
    <xsl:with-param name="list"><xsl:value-of select="/incident/admin_info/ncaRefFSCA" /></xsl:with-param>
    <xsl:with-param name="delimiter">;</xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template name="ncaRefFSCAList">
  <xsl:param name="list" />
  <xsl:param name="delimiter" />
  <xsl:variable name="newlist">
    <xsl:choose>
      <xsl:when test="contains($list, $delimiter)"><xsl:value-of select="normalize-space($list)" /></xsl:when>   
       <xsl:otherwise><xsl:value-of select="concat(normalize-space($list), $delimiter)"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="first" select="substring-before($newlist, $delimiter)" />
  <xsl:variable name="remaining" select="substring-after($newlist, $delimiter)" />
  
  <ncaRefFSCALI><xsl:value-of select="$first" /></ncaRefFSCALI>
  
  <xsl:if test="$remaining">
    <xsl:call-template name="ncaRefFSCAList">
      <xsl:with-param name="list" select="$remaining" />
      <xsl:with-param name="delimiter"><xsl:value-of select="$delimiter"/></xsl:with-param>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- this code allows to have multiple items in the filed eudamedRefFSCA separated with ; -->
<!-- in this case new elements eudamedRefFSCALI are created -->
<xsl:template match="/incident/admin_info/eudamedRefFSCA">
  <eudamedRefFSCA><xsl:value-of select="/incident/admin_info/eudamedRefFSCA" /></eudamedRefFSCA>
  <xsl:call-template name="eudamedRefFSCAList">
    <xsl:with-param name="list"><xsl:value-of select="/incident/admin_info/eudamedRefFSCA" /></xsl:with-param>
    <xsl:with-param name="delimiter">;</xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template name="eudamedRefFSCAList">
  <xsl:param name="list" />
  <xsl:param name="delimiter" />
  <xsl:variable name="newlist">
    <xsl:choose>
      <xsl:when test="contains($list, $delimiter)"><xsl:value-of select="normalize-space($list)" /></xsl:when>   
       <xsl:otherwise><xsl:value-of select="concat(normalize-space($list), $delimiter)"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="first" select="substring-before($newlist, $delimiter)" />
  <xsl:variable name="remaining" select="substring-after($newlist, $delimiter)" />
  
  <eudamedRefFSCALI><xsl:value-of select="$first" /></eudamedRefFSCALI>
  
  <xsl:if test="$remaining">
    <xsl:call-template name="eudamedRefFSCAList">
      <xsl:with-param name="list" select="$remaining" />
      <xsl:with-param name="delimiter"><xsl:value-of select="$delimiter"/></xsl:with-param>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- this code allows to have multiple items in the filed mfrRefFSCA separated with ; -->
<!-- in this case new elements mfrRefFSCALI are created -->
<xsl:template match="/incident/admin_info/mfrRefFSCA">
  <mfrRefFSCA><xsl:value-of select="/incident/admin_info/mfrRefFSCA" /></mfrRefFSCA>
  <xsl:call-template name="mfrRefFSCAList">
    <xsl:with-param name="list"><xsl:value-of select="/incident/admin_info/mfrRefFSCA" /></xsl:with-param>
    <xsl:with-param name="delimiter">;</xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template name="mfrRefFSCAList">
  <xsl:param name="list" />
  <xsl:param name="delimiter" />
  <xsl:variable name="newlist">
    <xsl:choose>
      <xsl:when test="contains($list, $delimiter)"><xsl:value-of select="normalize-space($list)" /></xsl:when>   
       <xsl:otherwise><xsl:value-of select="concat(normalize-space($list), $delimiter)"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="first" select="substring-before($newlist, $delimiter)" />
  <xsl:variable name="remaining" select="substring-after($newlist, $delimiter)" />
  
  <mfrRefFSCALI><xsl:value-of select="$first" /></mfrRefFSCALI>
  
  <xsl:if test="$remaining">
    <xsl:call-template name="mfrRefFSCAList">
      <xsl:with-param name="list" select="$remaining" />
      <xsl:with-param name="delimiter"><xsl:value-of select="$delimiter"/></xsl:with-param>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- this code allows to have multiple items in the filed psrId separated with ; -->
<!-- in this case new elements psrIdLI are created -->
<xsl:template match="/incident/admin_info/psrId">
  <psrId><xsl:value-of select="/incident/admin_info/psrId" /></psrId>
  <xsl:call-template name="psrIdList">
    <xsl:with-param name="list"><xsl:value-of select="/incident/admin_info/psrId" /></xsl:with-param>
    <xsl:with-param name="delimiter">;</xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template name="psrIdList">
  <xsl:param name="list" />
  <xsl:param name="delimiter" />
  <xsl:variable name="newlist">
    <xsl:choose>
      <xsl:when test="contains($list, $delimiter)"><xsl:value-of select="normalize-space($list)" /></xsl:when>   
       <xsl:otherwise><xsl:value-of select="concat(normalize-space($list), $delimiter)"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="first" select="substring-before($newlist, $delimiter)" />
  <xsl:variable name="remaining" select="substring-after($newlist, $delimiter)" />
  
  <psrIdLI><xsl:value-of select="$first" /></psrIdLI>
  
  <xsl:if test="$remaining">
    <xsl:call-template name="psrIdList">
      <xsl:with-param name="list" select="$remaining" />
      <xsl:with-param name="delimiter"><xsl:value-of select="$delimiter"/></xsl:with-param>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- this code allows to have multiple items in the filed pmcfpmpfId separated with ; -->
<!-- in this case new elements pmcfpmpfIdLI are created -->
<xsl:template match="/incident/admin_info/pmcfpmpfId">
  <pmcfpmpfId><xsl:value-of select="/incident/admin_info/pmcfpmpfId" /></pmcfpmpfId>
  <xsl:call-template name="pmcfpmpfIdList">
    <xsl:with-param name="list"><xsl:value-of select="/incident/admin_info/pmcfpmpfId" /></xsl:with-param>
    <xsl:with-param name="delimiter">;</xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template name="pmcfpmpfIdList">
  <xsl:param name="list" />
  <xsl:param name="delimiter" />
  <xsl:variable name="newlist">
    <xsl:choose>
      <xsl:when test="contains($list, $delimiter)"><xsl:value-of select="normalize-space($list)" /></xsl:when>   
       <xsl:otherwise><xsl:value-of select="concat(normalize-space($list), $delimiter)"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="first" select="substring-before($newlist, $delimiter)" />
  <xsl:variable name="remaining" select="substring-after($newlist, $delimiter)" />
  
  <pmcfpmpfIdLI><xsl:value-of select="$first" /></pmcfpmpfIdLI>
  
  <xsl:if test="$remaining">
    <xsl:call-template name="pmcfpmpfIdList">
      <xsl:with-param name="list" select="$remaining" />
      <xsl:with-param name="delimiter"><xsl:value-of select="$delimiter"/></xsl:with-param>
    </xsl:call-template>
  </xsl:if>
</xsl:template>





<!-- this code allows to have multiple items in the filed nbCertNum separated with ; -->
<!-- in this case new elements nbCertNumLI are created -->
<xsl:template match="/incident/device_info/nbCertNum">
  <nbCertNum><xsl:value-of select="/incident/device_info/nbCertNum" /></nbCertNum>
  <xsl:call-template name="nbCertNumList">
    <xsl:with-param name="list"><xsl:value-of select="/incident/device_info/nbCertNum" /></xsl:with-param>
    <xsl:with-param name="delimiter">;</xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template name="nbCertNumList">
  <xsl:param name="list" />
  <xsl:param name="delimiter" />
  <xsl:variable name="newlist">
    <xsl:choose>
      <xsl:when test="contains($list, $delimiter)"><xsl:value-of select="normalize-space($list)" /></xsl:when>   
       <xsl:otherwise><xsl:value-of select="concat(normalize-space($list), $delimiter)"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="first" select="substring-before($newlist, $delimiter)" />
  <xsl:variable name="remaining" select="substring-after($newlist, $delimiter)" />
  
  <nbCertNumLI><xsl:value-of select="$first" /></nbCertNumLI>
  
  <xsl:if test="$remaining">
    <xsl:call-template name="nbCertNumList">
      <xsl:with-param name="list" select="$remaining" />
      <xsl:with-param name="delimiter"><xsl:value-of select="$delimiter"/></xsl:with-param>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- this code allows to have multiple items in the filed nbCertNum2 separated with ; -->
<!-- in this case new elements nbCertNum2LI are created -->
<xsl:template match="/incident/device_info/nbCertNum2">
  <nbCertNum2><xsl:value-of select="/incident/device_info/nbCertNum2" /></nbCertNum2>
  <xsl:call-template name="nbCertNum2List">
    <xsl:with-param name="list"><xsl:value-of select="/incident/device_info/nbCertNum2" /></xsl:with-param>
    <xsl:with-param name="delimiter">;</xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template name="nbCertNum2List">
  <xsl:param name="list" />
  <xsl:param name="delimiter" />
  <xsl:variable name="newlist">
    <xsl:choose>
      <xsl:when test="contains($list, $delimiter)"><xsl:value-of select="normalize-space($list)" /></xsl:when>   
       <xsl:otherwise><xsl:value-of select="concat(normalize-space($list), $delimiter)"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="first" select="substring-before($newlist, $delimiter)" />
  <xsl:variable name="remaining" select="substring-after($newlist, $delimiter)" />
  
  <nbCertNum2LI><xsl:value-of select="$first" /></nbCertNum2LI>
  
  <xsl:if test="$remaining">
    <xsl:call-template name="nbCertNum2List">
      <xsl:with-param name="list" select="$remaining" />
      <xsl:with-param name="delimiter"><xsl:value-of select="$delimiter"/></xsl:with-param>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- this code allows to have multiple items in the filed otherCountries separated with ; -->
<!-- in this case new elements otherCountriesLI are created -->
<xsl:template match="/incident/device_info/distribution/otherCountries">
  <otherCountries><xsl:value-of select="/incident/device_info/distribution/otherCountries" /></otherCountries>
  <xsl:call-template name="distributionOtherList">
    <xsl:with-param name="list"><xsl:value-of select="/incident/device_info/distribution/otherCountries" /></xsl:with-param>
    <xsl:with-param name="delimiter">;</xsl:with-param>
  </xsl:call-template>
</xsl:template>


<xsl:template name="distributionOtherList">
  <xsl:param name="list" />
  <xsl:param name="delimiter" />
  <xsl:variable name="newlist">
    <xsl:choose>
      <xsl:when test="contains($list, $delimiter)"><xsl:value-of select="normalize-space($list)" /></xsl:when>   
       <xsl:otherwise><xsl:value-of select="concat(normalize-space($list), $delimiter)"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="first" select="substring-before($newlist, $delimiter)" />
  <xsl:variable name="remaining" select="substring-after($newlist, $delimiter)" />
  
  <otherCountriesLI><xsl:value-of select="$first" /></otherCountriesLI>
  
  <xsl:if test="$remaining">
    <xsl:call-template name="distributionOtherList">
      <xsl:with-param name="list" select="$remaining" />
      <xsl:with-param name="delimiter"><xsl:value-of select="$delimiter"/></xsl:with-param>
    </xsl:call-template>
  </xsl:if>
</xsl:template>


<!-- Remove risk class equal to No -->
<xsl:template match="incident/device_info/deviceClassMDRType/implantable">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassMDRType/activedevice">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassMDRType/intendedtoadminister">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassMDRType/sterileconditions">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassMDRType/measuringfunction">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassMDRType/reusable">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassMDRType/software">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassMDRType/systems">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassMDRType/procedurepacks">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassMDRType/custommade">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassMDRType/non-medical">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassIVDRType/selftesting">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassIVDRType/nearpatienttesting">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassIVDRType/professionaltesting">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassIVDRType/companiondiagnostics">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassIVDRType/reagent">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassIVDRType/software">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassIVDRType/instrument">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="incident/device_info/deviceClassIVDRType/sterileconditions">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != 'No'">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<!-- delete the empty entries -->
<!-- 
<xsl:template match="mfrRegNum|arDetails|distributionEEA|distribution_all|otherCountries|similarVariant|reporterOtherText|timePeriodN-1|timePeriodN-2|timePeriodN-3">
	<xsl:variable name="value" select="." />
	<xsl:if test="$value != ''">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>
-->
<xsl:template match="*[not(descendant::text()[normalize-space()])]" />

<xsl:template match="@*|node()">
	<xsl:copy>
		<xsl:apply-templates select="@*|node()"/>
	</xsl:copy>
</xsl:template>

<!-- delete nomenclatureSystem when nomenclatureCode and nomenclature text are empty 
<xsl:template match="nomenclatureSystem">
	<xsl:variable name="nomenclatureCode" select="/incident/device_info/nomenclatureCode" />
	<xsl:variable name="nomenclatureText" select="/incident/device_info/nomenclatureCodeDefinedInText" />
	<xsl:if test="$nomenclatureCode != '' or $nomenclatureText != ''">
		<xsl:copy>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:if>
</xsl:template>
-->

<!--don't generate content for the <excludeTagEcMir>, just apply-templates to it's children-->
<xsl:template match="excludeTagEcMir">
    <xsl:apply-templates/>
</xsl:template>
<!-- top level element, modfy the reference to the XSD to be used; then go into the rest of the input tree -->
<xsl:strip-space elements="*"/>
<xsl:template match="/incident">
	<xsl:variable name="value" select="/incident/admin_info/reportType" />
	<incident version="V5.0 2018-05-30" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" >
		<xsl:choose>
			<xsl:when test="$value = 'Initial'">
				<xsl:attribute name="xsi:noNamespaceSchemaLocation">incident-Initial-v7.2.1.xsd</xsl:attribute>
			</xsl:when>
			<xsl:when test="$value = 'Combined initial and final'">
				<xsl:attribute name="xsi:noNamespaceSchemaLocation">incident-InitialFinal-v7.2.1.xsd</xsl:attribute>
			</xsl:when>
			<xsl:when test="$value = 'Follow up'">
				<xsl:attribute name="xsi:noNamespaceSchemaLocation">incident-Followup-v7.2.1.xsd</xsl:attribute>
			</xsl:when>
			<xsl:when test="$value = 'Final (Reportable incident)'">
				<xsl:attribute name="xsi:noNamespaceSchemaLocation">incident-FinalRep-v7.2.1.xsd</xsl:attribute>
			</xsl:when>
			<xsl:when test="$value = 'Final (Non-reportable incident)'">
				<xsl:attribute name="xsi:noNamespaceSchemaLocation">incident-FinalNonRep-v7.2.1.xsd</xsl:attribute>
			</xsl:when>
			<xsl:otherwise></xsl:otherwise>
		</xsl:choose>
		<xsl:attribute name="sCreateTimeStamp"><xsl:value-of select="/incident/admin_info/reportDate" /></xsl:attribute>
		<xsl:attribute name="sFormLanguage">en</xsl:attribute>
		<xsl:apply-templates/>
	</incident>
</xsl:template>

<!-- indentity transformation = keep output like input tree -->
<xsl:template match="node()|@*">
	<xsl:copy>
		<xsl:apply-templates select="node()|@*"/>
	</xsl:copy>
</xsl:template>

</xsl:stylesheet>

