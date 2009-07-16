<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:spring="http://www.springframework.org/schema/beans" xmlns="http://www.consol.de/testframework">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" cdata-section-elements="data"/>
	
	<xsl:template match="spring:beans">
		<spring:beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.consol.de/testframework http://www.consol.de/testframework/consol_ts.xsd">
				<xsl:apply-templates/>	
		</spring:beans>
	</xsl:template>
	
	<xsl:template match="spring:bean[@parent='testCase']">
		<xsl:element name="testcase">
			<xsl:attribute name="name"><xsl:value-of select="@name"></xsl:value-of></xsl:attribute>
			
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="spring:bean/spring:description">
		<description><xsl:value-of select="."></xsl:value-of></description>
	</xsl:template>
	
	<xsl:template match="spring:property[@name='testVariables']">
		<variables>
			<xsl:for-each select="spring:map/spring:entry">
				<xsl:element name="variable">
					<xsl:attribute name="name"><xsl:value-of select="@key"></xsl:value-of></xsl:attribute>
					<xsl:attribute name="value"><xsl:value-of select="@value"></xsl:value-of></xsl:attribute>
				</xsl:element>
			</xsl:for-each>
		</variables>
	</xsl:template>
	
	<xsl:template match="spring:property[@name='testChain']">
		<actions>
			<!-- <xsl:apply-templates select="spring:list/spring:bean"/> -->
			<xsl:apply-templates/>
		</actions>
	</xsl:template>
	
	<xsl:template match="spring:property[@name='finallyChain']">
		<cleanup>
			<xsl:apply-templates select="spring:list/spring:bean"/>
		</cleanup>
	</xsl:template>
	
	<xsl:template match="spring:bean">
		<xsl:choose>
			<xsl:when test="starts-with(@parent, 'send')">
				<xsl:call-template name="send"></xsl:call-template>
			</xsl:when>
			<xsl:when test="starts-with(@parent, 'receive')">
				<xsl:choose>
					<xsl:when test="starts-with(@parent, 'receiveTimeout')">
						<xsl:call-template name="expectTimeout"></xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="receive"></xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="starts-with(@parent, 'wait')">
				<xsl:call-template name="sleep"></xsl:call-template>
			</xsl:when>
			<xsl:when test="starts-with(@parent, 'databaseAccess')">
				<xsl:call-template name="updateDatabase"></xsl:call-template>
			</xsl:when>
			<xsl:when test="starts-with(@parent, 'databaseQuery')">
				<xsl:call-template name="queryDatabase"></xsl:call-template>
			</xsl:when>
			<xsl:when test="starts-with(@parent, 'runTrigger')">
				<xsl:call-template name="java"></xsl:call-template>
			</xsl:when>
			<xsl:when test="starts-with(@parent, 'infoBean')">
				<xsl:call-template name="traceVariables"></xsl:call-template>
			</xsl:when>
			<xsl:when test="starts-with(@parent, 'settingVariables')">
				<xsl:call-template name="createVariables"></xsl:call-template>
			</xsl:when>
			<xsl:when test="starts-with(@parent, 'timeWatcher')">
				<xsl:call-template name="traceTime"></xsl:call-template>
			</xsl:when>
			<xsl:when test="starts-with(@parent, 'echo')">
				<xsl:call-template name="echo"></xsl:call-template>
			</xsl:when>			
			<xsl:when test="starts-with(@parent, 'purge')">
				<xsl:call-template name="purgeJmsQueues"></xsl:call-template>
			</xsl:when>
			<xsl:when test="starts-with(@parent, 'switch')">
				<xsl:element name="bean"><xsl:attribute name="parent"><xsl:value-of select="@parent"/></xsl:attribute></xsl:element>
			</xsl:when>
			<xsl:when test="starts-with(@parent, 'consume')">
				<xsl:element name="bean"><xsl:attribute name="parent"><xsl:value-of select="@parent"/></xsl:attribute></xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="error"></xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="send">
		<xsl:param name="fileValue"  select="spring:property[@name='xmlResource']/@value" />
		<xsl:element name="send">
			<xsl:attribute name="type"><xsl:value-of select="@parent"></xsl:value-of></xsl:attribute>
			
			<xsl:if test="spring:property[@name='destination']">
				<destination><xsl:value-of select="spring:property[@name='destination']/@value"></xsl:value-of></destination>
			</xsl:if>
		
			<message>
				<xsl:if test="spring:property[@name='xmlData']">
					<data>
						<xsl:value-of select="spring:property[@name='xmlData']/spring:value"></xsl:value-of>
					</data>
				</xsl:if>
				<xsl:if test="spring:property[@name='validateMessageValues']">
					<xsl:for-each select="spring:property[@name='validateMessageValues']/spring:map/spring:entry">
						<xsl:element name="element">
							<xsl:attribute name="path"><xsl:value-of select="@key"></xsl:value-of></xsl:attribute>
							<xsl:attribute name="value"><xsl:value-of select="@value"></xsl:value-of></xsl:attribute>
						</xsl:element>
					</xsl:for-each>
				</xsl:if>
				<xsl:if test="spring:property[@name='xmlResource']">
							
					<xsl:element name="resource">
						<xsl:attribute name="file">	
							<xsl:choose>
								<xsl:when test="contains($fileValue,'file:')"><xsl:value-of select="substring-after($fileValue,'file:')"/></xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$fileValue"/>
							</xsl:otherwise>
							</xsl:choose>								
						</xsl:attribute>	
					</xsl:element>
				</xsl:if>  
				<!-- <xsl:if test="spring:property[@name='xmlResource']">
					<xsl:element name="resource">
						<xsl:attribute name="file">
							<xsl:value-of select="spring:property[@name='xmlResource']/@value"></xsl:value-of>
						</xsl:attribute>	
					</xsl:element>
				</xsl:if> -->
				
				<xsl:apply-templates select="spring:property[@name='setMessageValues']"/>
			</message>

			<xsl:if test="spring:property[@name='setHeaderValues']">
				<xsl:apply-templates select="spring:property[@name='setHeaderValues']"/>
			</xsl:if>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="receive">
	<xsl:param name="value"	  select="spring:property[@name='xmlResource']/@value" />
		<xsl:element name="receive">
			<xsl:attribute name="type"><xsl:value-of select="@parent"></xsl:value-of></xsl:attribute>
			
			<xsl:if test="spring:property[@name='destination']">
				<destination><xsl:value-of select="spring:property[@name='destination']/@value"></xsl:value-of></destination>
			</xsl:if>
			
			<message>
			
			
				<xsl:if test="spring:property[@name='xmlData']">
					<data>
							<xsl:value-of select="spring:property[@name='xmlData']/spring:value"></xsl:value-of>
					</data>
				</xsl:if>
					<xsl:if test="spring:property[@name='validateMessageValues']">
					<xsl:for-each select="spring:property[@name='validateMessageValues']/spring:map/spring:entry">
				<xsl:element name="element">
					<xsl:attribute name="path"><xsl:value-of select="@key"></xsl:value-of></xsl:attribute>
					<xsl:attribute name="value"><xsl:value-of select="@value"></xsl:value-of></xsl:attribute>
				</xsl:element>
				</xsl:for-each>
				</xsl:if>
				
				<xsl:if test="spring:property[@name='xmlResource']">
				
					<xsl:element name="resource">
						<xsl:attribute name="file">	
										<xsl:choose>
								<xsl:when test="contains($value,'file:')"><xsl:value-of select="substring-after($value,'file:')"/></xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$value"/>
							</xsl:otherwise>
							</xsl:choose>								
						</xsl:attribute>	
					</xsl:element>
				</xsl:if> 
				
				<xsl:apply-templates select="spring:property[@name='setMessageValues']"/>
				<xsl:apply-templates select="spring:property[@name='ignoreValues']"/>
			</message>

			<xsl:if test="spring:property[@name='setHeaderValues']">
				<xsl:apply-templates select="spring:property[@name='setHeaderValues']"/>
			</xsl:if>
			
			<xsl:if test="spring:property[@name='validateHeaderValues']">
				<xsl:apply-templates select="spring:property[@name='validateHeaderValues']"/>
			</xsl:if>
			
			<xsl:choose>
				<xsl:when test="spring:property[@name='getHeaderValues']">
					<extract>
						<xsl:apply-templates select="spring:property[@name='getHeaderValues']"/>
						
						<xsl:if test="spring:property[@name='getMessageValues']">
								<xsl:apply-templates select="spring:property[@name='getMessageValues']"/>
						</xsl:if>
					</extract>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="spring:property[@name='getMessageValues']">
						<extract>
							<xsl:apply-templates select="spring:property[@name='getMessageValues']"/>
						</extract>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:element>
	</xsl:template>	
	
	
	
	<xsl:template match="spring:property[@name='setMessageValues']">
		<xsl:for-each select="spring:map/spring:entry">
			<xsl:element name="element">
				<xsl:attribute name="path"><xsl:value-of select="@key"></xsl:value-of></xsl:attribute>
				<xsl:attribute name="value"><xsl:value-of select="@value"></xsl:value-of></xsl:attribute>
			</xsl:element>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="spring:property[@name='setHeaderValues']">
		<header>
			<xsl:for-each select="spring:map/spring:entry">
				<xsl:element name="element">
					<xsl:attribute name="name"><xsl:value-of select="@key"></xsl:value-of></xsl:attribute>
					<xsl:attribute name="value"><xsl:value-of select="@value"></xsl:value-of></xsl:attribute>
				</xsl:element>
			</xsl:for-each>
		</header>
	</xsl:template>
	
	<xsl:template match="spring:property[@name='validateHeaderValues']">
		<header>
			<xsl:for-each select="spring:map/spring:entry">
				<xsl:element name="element">
					<xsl:attribute name="name"><xsl:value-of select="@key"></xsl:value-of></xsl:attribute>
					<xsl:attribute name="value"><xsl:value-of select="@value"></xsl:value-of></xsl:attribute>
				</xsl:element>
			</xsl:for-each>
		</header>
	</xsl:template>

	<xsl:template match="spring:property[@name='ignoreValues']">
		<xsl:for-each select="spring:list/spring:value">
				<xsl:element name="ignore">
					<xsl:attribute name="path"><xsl:value-of select="."></xsl:value-of></xsl:attribute>
				</xsl:element>
			</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="spring:property[@name='getHeaderValues']">
			<xsl:for-each select="spring:map/spring:entry">
				<xsl:element name="header">
					<xsl:attribute name="name"><xsl:value-of select="@key"></xsl:value-of></xsl:attribute>
					<xsl:attribute name="variable"><xsl:value-of select="@value"></xsl:value-of></xsl:attribute>
				</xsl:element>
			</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="spring:property[@name='getMessageValues']">
		<xsl:for-each select="spring:map/spring:entry">
				<xsl:element name="message">
					<xsl:attribute name="path"><xsl:value-of select="@key"></xsl:value-of></xsl:attribute>
					<xsl:attribute name="variable"><xsl:value-of select="@value"></xsl:value-of></xsl:attribute>
				</xsl:element>
			</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="spring:property[@name='validateMessageValues']">
		<xsl:for-each select="spring:map/spring:entry">
				<xsl:element name="validate">
					<xsl:attribute name="path"><xsl:value-of select="@key"></xsl:value-of></xsl:attribute>
					<xsl:attribute name="value"><xsl:value-of select="@value"></xsl:value-of></xsl:attribute>
				</xsl:element>
			</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="sleep">
		<xsl:element name="sleep">
			<xsl:choose>
				<xsl:when test="spring:property[@name='delay']">
					<xsl:attribute name="time"><xsl:value-of select="spring:property[@name='delay']/@value"></xsl:value-of></xsl:attribute>
				</xsl:when>
			</xsl:choose>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="updateDatabase">
		<xsl:element name="updateDatabase">
			<xsl:attribute name="connect"><xsl:value-of select="@parent"></xsl:value-of></xsl:attribute>
			
			<xsl:if test="spring:property[@name='ignoreErrors']">
				<xsl:attribute name="ignoreErrors"><xsl:value-of select="spring:property[@name='ignoreErrors']/@value"></xsl:value-of></xsl:attribute>
			</xsl:if>
		
			<xsl:if test="spring:property[@name='sqlResource']">
				<xsl:element name="resource">
					<xsl:attribute name="file"><xsl:value-of select="spring:property[@name='sqlResource']/@value"></xsl:value-of></xsl:attribute>
				</xsl:element>
			</xsl:if>
			
			<xsl:if test="spring:property[@name='statements']">
				<xsl:for-each select="spring:property[@name='statements']/spring:list/spring:value">
					<statement><xsl:value-of select="."></xsl:value-of></statement>
				</xsl:for-each>
			</xsl:if>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="queryDatabase">
		<xsl:element name="queryDatabase">
			<xsl:attribute name="connect"><xsl:value-of select="@parent"></xsl:value-of></xsl:attribute>
			
			<xsl:if test="spring:property[@name='statements']">
				<xsl:for-each select="spring:property[@name='statements']/spring:list/spring:value">
					<statement><xsl:value-of select="."></xsl:value-of></statement>
				</xsl:for-each>
			</xsl:if>
			
			<xsl:if test="spring:property[@name='sqlResource']">
				<xsl:element name="resource">
					<xsl:attribute name="file"><xsl:value-of select="spring:property[@name='sqlResource']/@value"></xsl:value-of></xsl:attribute>
				</xsl:element>
			</xsl:if>
			
			<xsl:if test="spring:property[@name='validateDBValues']">
				<xsl:for-each select="spring:property[@name='validateDBValues']/spring:map/spring:entry">
					<xsl:element name="validate">
						<xsl:attribute name="column"><xsl:value-of select="@key"></xsl:value-of></xsl:attribute>
						<xsl:attribute name="value"><xsl:value-of select="@value"></xsl:value-of></xsl:attribute>
					</xsl:element>
				</xsl:for-each>
			</xsl:if>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="java">
		<xsl:element name="java">
			<xsl:attribute name="class">
				<xsl:choose>
					<xsl:when test="spring:property[@name='className']">
						<xsl:value-of select="spring:property[@name='className']/@value"/>
					</xsl:when>
					<xsl:when test="spring:property[@name='instance']">
						<xsl:value-of select="spring:property[@name='instance']/spring:bean/@class"/>
					</xsl:when>
				</xsl:choose>			
			</xsl:attribute>
				
			<xsl:if test="spring:property[@name='instance']">
				<constructor>
					<xsl:for-each select="spring:property[@name='instance']/spring:bean/spring:constructor-arg">
						<xsl:element name="argument">						
							<xsl:attribute name="type"><xsl:value-of select="@type"></xsl:value-of></xsl:attribute>
							<xsl:value-of select="@value"></xsl:value-of>
						</xsl:element>
					</xsl:for-each>
				</constructor>
			</xsl:if>

			<xsl:if test="spring:property[@name='constructorArgs']">
				<constructor>
					<xsl:for-each select="spring:property[@name='constructorArgs']/spring:list/spring:value">
						<xsl:element name="argument">						
							<xsl:attribute name="type"><xsl:value-of select="@type"></xsl:value-of></xsl:attribute>
							<xsl:value-of select="."></xsl:value-of>
						</xsl:element>
					</xsl:for-each>
				</constructor>
			</xsl:if>
			
			<xsl:if test="spring:property[@name='methodName']">
				<xsl:element name="method">
					<xsl:attribute name="name"><xsl:value-of select="spring:property[@name='methodName']/@value"></xsl:value-of></xsl:attribute>
					
					<xsl:for-each select="spring:property[@name='methodArgs']/spring:list/*">
						<xsl:if test="spring:list">
							<xsl:element name="argument">
								<xsl:attribute name="type">String[]</xsl:attribute>
								<xsl:for-each select="spring:list/spring:value">
									<xsl:value-of select="."></xsl:value-of>,
								</xsl:for-each>
							</xsl:element>
						</xsl:if>
						
						<xsl:if test="spring:value">
							<xsl:element name="argument">
								<xsl:attribute name="type"><xsl:value-of select="spring:value/@type"></xsl:value-of></xsl:attribute>
								<xsl:value-of select="."></xsl:value-of>								
							</xsl:element>
						</xsl:if>
					</xsl:for-each>
				</xsl:element>
			</xsl:if>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="traceVariables">
		<xsl:element name="traceVariables">
			<xsl:for-each select="spring:property[@name='infoValues']/spring:list/spring:value">
				<xsl:element name="variable">
					<xsl:attribute name="name"><xsl:value-of select="."></xsl:value-of></xsl:attribute>
				</xsl:element>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="createVariables">
		<xsl:element name="createVariables">
			<xsl:for-each select="spring:property[@name='variables']/spring:map/spring:entry">
				<xsl:element name="variable">
					<xsl:attribute name="name"><xsl:value-of select="@key"></xsl:value-of></xsl:attribute>
					<xsl:attribute name="value"><xsl:value-of select="@value"></xsl:value-of></xsl:attribute>
				</xsl:element>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="traceTime">
		<xsl:element name="traceTime">
			<xsl:if test="spring:property[@name='id']">
				<xsl:attribute name="id"><xsl:value-of select="spring:property[@name='id']/@value"></xsl:value-of></xsl:attribute>
			</xsl:if>
			
			<xsl:if test="spring:property[@name='description']">
				<description><xsl:value-of select="spring:property[@name='description']/@value"></xsl:value-of><xsl:value-of select="."></xsl:value-of></description>
			</xsl:if>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="echo">
		<xsl:element name="echo">
			<xsl:if test="spring:property[@name='message']">
				<message><xsl:value-of select="spring:property[@name='message']/@value"></xsl:value-of></message>
			</xsl:if>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="expectTimeout">
		<xsl:element name="expectTimeout">
			<xsl:attribute name="connect"><xsl:value-of select="@parent"></xsl:value-of></xsl:attribute>
			<xsl:if test="spring:property[@name='timeout']">
				<xsl:attribute name="wait"><xsl:value-of select="spring:property[@name='timeout']/@value"></xsl:value-of></xsl:attribute>
			</xsl:if>
			<xsl:if test="spring:property[@name='destination']">
				<destination><xsl:value-of select="spring:property[@name='destination']/@value"></xsl:value-of></destination>
			</xsl:if>
		</xsl:element>
	</xsl:template>
	
	<xsl:template name="purgeJmsQueues">
		<xsl:element name="purgeJmsQueues">
			<xsl:choose>
				<xsl:when test="@parent">
					<xsl:attribute name="connect"><xsl:value-of select="@parent"></xsl:value-of></xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="connect">
						<xsl:value-of select="spring:property[@name='queueConnectionFactory']/spring:ref/@bean"></xsl:value-of>
					</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			
			<xsl:for-each select="spring:property[@name='queueNames']/spring:list/spring:value">
				<xsl:element name="queue">
					<xsl:attribute name="name"><xsl:value-of select="."></xsl:value-of></xsl:attribute>
				</xsl:element>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="text()"></xsl:template>
	
	<xsl:template match="comment()">
		<xsl:comment>
			<xsl:value-of select="."/>
		</xsl:comment>
	</xsl:template>
	
	<xsl:template name="error">
		<xsl:message terminate="yes">Unknown XML Node was found: <xsl:value-of select="."></xsl:value-of></xsl:message>
	</xsl:template>
	
</xsl:stylesheet>
