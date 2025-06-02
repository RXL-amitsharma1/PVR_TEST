<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output encoding="utf-8" omit-xml-declaration="no" indent="yes"/>
    <xsl:strip-space elements="*"/>
    <xsl:template match="ichicsr">
        <ichicsr lang="en">
            <xsl:apply-templates select="@*|node()"/>
        </ichicsr>
    </xsl:template>
    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="*[not(@*|*|comment()|processing-instruction()) and normalize-space()='']"/>
</xsl:stylesheet>