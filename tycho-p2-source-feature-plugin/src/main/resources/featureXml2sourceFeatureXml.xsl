<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		version="1.0" />

	<!-- fix <feature> :: id=".feature.source" and label=" Source" and remove plugin="" -->
	<xsl:template match="feature">
		<feature id="{@id}.source" label="{@label} Source" version="{@version}"
			provider-name="{@provider-name}">
			<xsl:apply-templates />
		</feature>
	</xsl:template>

	<!-- fix <plugin id="org.jboss.tools.jmx.core" download-size="0" install-size="0" 
		version="0.0.0" unpack="false"/> -->
	<xsl:template match="//plugin">
		<plugin id="{@id}.source" download-size="{@download-size}" install-size="{@install-size}"
			version="{@version}" unpack="{@unpack}" />
	</xsl:template>

	<!-- remove the <requires> -->
	<xsl:template match="requires" />

	<!-- Copy everything else unchanged -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>