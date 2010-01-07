<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:tns="http://www.citrusframework.org/samples/greeting">
                              
	<xsl:template match="/tns:GreetingRequestMessage">
		<tns:GreetingResponseMessage>
            <tns:CorrelationId><xsl:value-of select="tns:CorrelationId"/></tns:CorrelationId>
            <tns:Operation><xsl:value-of select="tns:Operation"/></tns:Operation>
            <tns:User>GreetingService</tns:User>
            <tns:Text>Hello <xsl:value-of select="tns:User"/>!</tns:Text>
        </tns:GreetingResponseMessage>
	</xsl:template>
  
  <xsl:template match="text()"/>
</xsl:stylesheet>