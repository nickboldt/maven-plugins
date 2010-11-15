<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		version="1.0" />

	<!-- TODO: <artifactId>org.jboss.tools.jmx.feature</artifactId> - want .source.feature -->
	<xsl:template match="/project/artifactId">
		<xsl:choose>
			<xsl:when test="contains(.,'.feature')">
				<artifactId><xsl:value-of select="."/>.source</artifactId>
			</xsl:when>
			<xsl:otherwise>
				<artifactId><xsl:value-of select="."/></artifactId>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Copy everything else unchanged -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>