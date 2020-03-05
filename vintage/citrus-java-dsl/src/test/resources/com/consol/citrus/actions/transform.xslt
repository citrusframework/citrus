<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
	<html>
		<body>
			<h2>Test Request</h2>
			<p>Message: <xsl:value-of select="TestRequest/Message"/></p>
		</body>  
	</html>
</xsl:template>
</xsl:stylesheet>