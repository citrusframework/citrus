<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright the original author or authors.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<!--
  Migrates Spring bean XML test cases (citrus-testcase.xsd in citrus-spring) to the plain XML test DSL (citrus-testcase.xsd in citrus-xml).

  Without parameters the stylesheet migrates the single test case in the source document.
  The parameter "template" selects a template defined in the source document by its name and migrates this template instead.

  Actions of the Docker, Camel and Selenium modules are migrated to the actions of the same modules in the plain XML DSL.
  Elements that have no counterpart in the plain XML DSL are copied as is and are marked with a migration warning comment.
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:spring="http://www.springframework.org/schema/beans"
                xmlns:citrus="http://www.citrusframework.org/schema/testcase"
                xmlns:docker="http://www.citrusframework.org/schema/docker/testcase"
                xmlns:camel="http://www.citrusframework.org/schema/camel/testcase"
                xmlns:selenium="http://www.citrusframework.org/schema/selenium/testcase"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns="http://citrusframework.org/schema/xml/testcase"
                exclude-result-prefixes="spring citrus docker camel selenium">

  <xsl:output method="xml" encoding="UTF-8" indent="yes"
              cdata-section-elements="data builder script source xslt value"/>

  <xsl:strip-space elements="spring:* citrus:* docker:* camel:* selenium:*"/>
  <xsl:preserve-space elements="citrus:data citrus:message citrus:value citrus:script citrus:builder citrus:statement
                                citrus:groovy citrus:xml-data citrus:xslt-data citrus:select citrus:description
                                docker:result camel:language camel:result selenium:script selenium:alert-text"/>

  <!-- Name of a template in the source document that should be migrated instead of the test case -->
  <xsl:param name="template" select="''"/>

  <!--
    Optional namespace for marker attributes that hold the namespace of unprefixed payload elements.
    Some processors (e.g. the XSLTC processor built into the JDK) generate prefixes for computed element names without prefix
    and ignore an empty namespace, so callers are able to use the marker attributes to fix the element names after the transformation.
  -->
  <xsl:param name="namespaceFixup" select="''"/>

  <xsl:variable name="springNs" select="'http://www.springframework.org/schema/beans'"/>
  <xsl:variable name="citrusNs" select="'http://www.citrusframework.org/schema/testcase'"/>
  <xsl:variable name="schemaLocation" select="'http://citrusframework.org/schema/xml/testcase http://citrusframework.org/schema/xml/testcase/citrus-testcase.xsd'"/>
  <xsl:variable name="warning" select="'MIGRATION WARNING: '"/>

  <!-- ============================================================== -->
  <!-- Document root                                                  -->
  <!-- ============================================================== -->

  <xsl:template match="/">
    <xsl:copy-of select="comment()"/>
    <xsl:choose>
      <xsl:when test="$template != ''">
        <xsl:choose>
          <xsl:when test="count(//citrus:template[@name = $template]) = 1">
            <xsl:apply-templates select="//citrus:template[@name = $template]" mode="root"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:message terminate="yes">Unable to find template '<xsl:value-of select="$template"/>' in Spring XML test source</xsl:message>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="count(//citrus:testcase) = 1">
        <xsl:apply-templates select="//citrus:testcase" mode="root"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">Spring XML test source must define exactly one test case</xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="citrus:testcase" mode="root">
    <test name="{@name}">
      <xsl:if test="citrus:meta-info/citrus:author">
        <xsl:attribute name="author"><xsl:value-of select="normalize-space(citrus:meta-info/citrus:author)"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="citrus:meta-info/citrus:status">
        <xsl:attribute name="status"><xsl:value-of select="normalize-space(citrus:meta-info/citrus:status)"/></xsl:attribute>
      </xsl:if>
      <xsl:attribute name="xsi:schemaLocation"><xsl:value-of select="$schemaLocation"/></xsl:attribute>
      <xsl:for-each select="//citrus:template">
        <xsl:call-template name="warn">
          <xsl:with-param name="message">Template '<xsl:value-of select="@name"/>' defined in the Spring XML test source has been migrated to a separate template file - please make it available to the apply-template action</xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:apply-templates select="citrus:description"/>
      <xsl:apply-templates select="citrus:variables"/>
      <xsl:if test="citrus:actions">
        <actions>
          <xsl:apply-templates select="citrus:actions/*"/>
        </actions>
      </xsl:if>
      <xsl:if test="citrus:finally">
        <finally>
          <xsl:apply-templates select="citrus:finally/*"/>
        </finally>
      </xsl:if>
    </test>
  </xsl:template>

  <xsl:template match="citrus:template" mode="root">
    <template name="{@name}">
      <xsl:attribute name="global-context">
        <xsl:choose>
          <xsl:when test="@global-context"><xsl:value-of select="@global-context"/></xsl:when>
          <xsl:otherwise>true</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:attribute name="xsi:schemaLocation"><xsl:value-of select="$schemaLocation"/></xsl:attribute>
      <xsl:apply-templates select="citrus:description"/>
      <actions>
        <xsl:call-template name="nested-actions"/>
      </actions>
    </template>
  </xsl:template>

  <!-- ============================================================== -->
  <!-- Common elements                                                -->
  <!-- ============================================================== -->

  <xsl:template match="citrus:description">
    <description>
      <xsl:call-template name="trim">
        <xsl:with-param name="text" select="."/>
      </xsl:call-template>
    </description>
  </xsl:template>

  <!-- Some actions in the plain XML DSL do not support a description, so keep the description as a comment -->
  <xsl:template match="citrus:description" mode="comment">
    <xsl:comment>
      <xsl:text> </xsl:text>
      <xsl:call-template name="comment-safe">
        <xsl:with-param name="text" select="normalize-space(.)"/>
      </xsl:call-template>
      <xsl:text> </xsl:text>
    </xsl:comment>
  </xsl:template>

  <xsl:template match="citrus:variables">
    <variables>
      <xsl:apply-templates select="citrus:variable"/>
    </variables>
  </xsl:template>

  <!-- the Spring XML test parser trims variable values given as data element -->
  <xsl:template match="citrus:variable">
    <variable name="{@name}">
      <xsl:copy-of select="@value"/>
      <xsl:if test="citrus:value">
        <value>
          <xsl:apply-templates select="citrus:value/citrus:script" mode="script"/>
          <xsl:if test="citrus:value/citrus:data">
            <data>
              <xsl:call-template name="trim">
                <xsl:with-param name="text" select="citrus:value/citrus:data"/>
              </xsl:call-template>
            </data>
          </xsl:if>
        </value>
      </xsl:if>
    </variable>
  </xsl:template>

  <xsl:template match="*" mode="script">
    <script>
      <xsl:copy-of select="@type|@file|@charset"/>
      <xsl:value-of select="."/>
    </script>
  </xsl:template>

  <xsl:template match="citrus:selector">
    <selector>
      <xsl:for-each select="citrus:element">
        <element name="{@name}" value="{@value}"/>
      </xsl:for-each>
      <xsl:if test="citrus:value">
        <value><xsl:value-of select="citrus:value"/></value>
      </xsl:if>
    </selector>
  </xsl:template>

  <!-- Applies all nested test actions of a container skipping the optional description -->
  <xsl:template name="nested-actions">
    <xsl:apply-templates select="*[not(self::citrus:description)]"/>
  </xsl:template>

  <!-- ============================================================== -->
  <!-- Send and receive                                               -->
  <!-- ============================================================== -->

  <xsl:template match="citrus:send">
    <send>
      <xsl:copy-of select="@endpoint|@actor|@fork"/>
      <xsl:apply-templates select="citrus:description"/>
      <xsl:call-template name="message"/>
      <xsl:apply-templates select="citrus:extract"/>
    </send>
  </xsl:template>

  <xsl:template match="citrus:receive">
    <receive>
      <xsl:copy-of select="@endpoint|@actor|@timeout"/>
      <xsl:copy-of select="citrus:message/@validator|citrus:message/@validators|citrus:message/@header-validator|citrus:message/@header-validators"/>
      <xsl:apply-templates select="citrus:description"/>
      <xsl:apply-templates select="citrus:selector"/>
      <xsl:call-template name="message"/>
      <xsl:for-each select="citrus:message/citrus:ignore">
        <ignore path="{@path}"/>
      </xsl:for-each>
      <xsl:for-each select="citrus:message/citrus:namespace">
        <namespace prefix="{@prefix}" value="{@value}"/>
      </xsl:for-each>
      <xsl:apply-templates select="citrus:message/citrus:validate"/>
      <xsl:apply-templates select="citrus:extract"/>
    </receive>
  </xsl:template>

  <!-- Builds the message with headers, body and path expressions from the Spring XML message and header elements -->
  <xsl:template name="message">
    <xsl:variable name="body" select="citrus:message/citrus:payload|citrus:message/citrus:data|citrus:message/citrus:resource|citrus:message/citrus:builder"/>
    <xsl:if test="citrus:message or citrus:header">
      <message>
        <xsl:copy-of select="citrus:message/@name|citrus:message/@type|citrus:message/@data-dictionary|citrus:message/@schema-validation|citrus:message/@schema|citrus:message/@schema-repository"/>
        <xsl:apply-templates select="citrus:header"/>
        <xsl:if test="$body">
          <body>
            <xsl:apply-templates select="$body" mode="body"/>
          </body>
        </xsl:if>
        <xsl:for-each select="citrus:message/citrus:element">
          <expression path="{@path}" value="{@value}"/>
        </xsl:for-each>
      </message>
    </xsl:if>
  </xsl:template>

  <xsl:template match="citrus:payload" mode="body">
    <payload>
      <xsl:apply-templates select="*" mode="payload"/>
    </payload>
  </xsl:template>

  <!-- the message body data gets trimmed in both test DSLs -->
  <xsl:template match="citrus:data" mode="body">
    <data>
      <xsl:call-template name="trim">
        <xsl:with-param name="text" select="."/>
      </xsl:call-template>
    </data>
  </xsl:template>

  <xsl:template match="citrus:resource" mode="body">
    <resource>
      <xsl:copy-of select="@file|@charset"/>
    </resource>
  </xsl:template>

  <xsl:template match="citrus:builder" mode="body">
    <builder>
      <xsl:copy-of select="@type|@file"/>
      <xsl:value-of select="."/>
    </builder>
  </xsl:template>

  <xsl:template match="citrus:header">
    <headers>
      <xsl:copy-of select="@ignore-case"/>
      <xsl:for-each select="*">
        <xsl:choose>
          <xsl:when test="self::citrus:element">
            <header name="{@name}" value="{@value}">
              <xsl:copy-of select="@type"/>
            </header>
          </xsl:when>
          <xsl:when test="self::citrus:data">
            <header>
              <data>
                <xsl:call-template name="trim">
                  <xsl:with-param name="text" select="."/>
                </xsl:call-template>
              </data>
            </header>
          </xsl:when>
          <xsl:when test="self::citrus:resource">
            <header>
              <resource>
                <xsl:copy-of select="@file|@charset"/>
              </resource>
            </header>
          </xsl:when>
          <xsl:when test="self::citrus:fragment">
            <header>
              <fragment>
                <xsl:apply-templates select="*" mode="payload"/>
              </fragment>
            </header>
          </xsl:when>
        </xsl:choose>
      </xsl:for-each>
    </headers>
  </xsl:template>

  <xsl:template match="citrus:validate">
    <validate>
      <xsl:copy-of select="@path|@value|@result-type"/>
      <xsl:apply-templates select="citrus:script" mode="script"/>
      <xsl:for-each select="citrus:xpath">
        <xpath expression="{@expression}" value="{@value}">
          <xsl:copy-of select="@result-type"/>
        </xpath>
      </xsl:for-each>
      <xsl:for-each select="citrus:json-path">
        <json-path expression="{@expression}" value="{@value}"/>
      </xsl:for-each>
      <xsl:for-each select="citrus:namespace">
        <namespace prefix="{@prefix}" value="{@value}"/>
      </xsl:for-each>
    </validate>
  </xsl:template>

  <xsl:template match="citrus:extract">
    <extract>
      <xsl:for-each select="citrus:header">
        <header name="{@name}" variable="{@variable}"/>
      </xsl:for-each>
      <xsl:for-each select="citrus:message">
        <body path="{@path}" variable="{@variable}">
          <xsl:copy-of select="@result-type"/>
        </body>
      </xsl:for-each>
    </extract>
  </xsl:template>

  <!--
    Copies inline XML payloads (message payload and header fragments).
    Only the namespaces declared on the element itself are copied, just like the Spring XML test parser does,
    so the payload does not get the Spring XML test namespace declarations.
  -->
  <xsl:template match="*" mode="payload">
    <!-- the Spring XML test parser removes the Spring XML test namespace from payload elements -->
    <xsl:variable name="namespace">
      <xsl:if test="namespace-uri() != $citrusNs"><xsl:value-of select="namespace-uri()"/></xsl:if>
    </xsl:variable>
    <xsl:variable name="name">
      <xsl:choose>
        <xsl:when test="namespace-uri() = $citrusNs"><xsl:value-of select="local-name()"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="name()"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="parent" select=".."/>
    <xsl:element name="{$name}" namespace="{$namespace}">
      <xsl:for-each select="namespace::*">
        <xsl:variable name="prefix" select="name()"/>
        <xsl:variable name="uri" select="string(.)"/>
        <xsl:if test="$uri != $citrusNs and $uri != $springNs and $prefix != 'xml'
                      and not($parent/namespace::*[name() = $prefix and string(.) = $uri])">
          <xsl:copy-of select="."/>
        </xsl:if>
      </xsl:for-each>
      <xsl:if test="$namespaceFixup != '' and not(contains($name, ':'))">
        <xsl:attribute name="fixup:namespace" namespace="{$namespaceFixup}"><xsl:value-of select="$namespace"/></xsl:attribute>
      </xsl:if>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="node()" mode="payload"/>
    </xsl:element>
  </xsl:template>

  <!-- Whitespace between child elements is formatting only, the output gets indented anyway -->
  <xsl:template match="text()[normalize-space() = '' and ../*]" mode="payload"/>

  <xsl:template match="text()|comment()|processing-instruction()" mode="payload">
    <xsl:copy/>
  </xsl:template>

  <!-- ============================================================== -->
  <!-- Basic test actions                                             -->
  <!-- ============================================================== -->

  <xsl:template match="citrus:echo">
    <echo>
      <xsl:call-template name="echo-content"/>
    </echo>
  </xsl:template>

  <xsl:template match="citrus:print">
    <print>
      <xsl:call-template name="echo-content"/>
    </print>
  </xsl:template>

  <xsl:template name="echo-content">
    <xsl:apply-templates select="citrus:description"/>
    <xsl:if test="citrus:message">
      <message><xsl:value-of select="citrus:message"/></message>
    </xsl:if>
  </xsl:template>

  <!-- The Spring XML "time" attribute is given in seconds whereas the plain XML "time" attribute uses milliseconds -->
  <xsl:template match="citrus:sleep">
    <sleep>
      <xsl:call-template name="sleep-content"/>
    </sleep>
  </xsl:template>

  <xsl:template match="citrus:delay">
    <delay>
      <xsl:call-template name="sleep-content"/>
    </delay>
  </xsl:template>

  <xsl:template name="sleep-content">
    <xsl:choose>
      <xsl:when test="@seconds">
        <xsl:copy-of select="@seconds"/>
      </xsl:when>
      <xsl:when test="@time">
        <xsl:attribute name="seconds"><xsl:value-of select="@time"/></xsl:attribute>
      </xsl:when>
    </xsl:choose>
    <xsl:copy-of select="@milliseconds"/>
    <xsl:apply-templates select="citrus:description"/>
  </xsl:template>

  <xsl:template match="citrus:create-variables">
    <create-variables>
      <xsl:apply-templates select="citrus:description"/>
      <xsl:for-each select="citrus:variable">
        <variable name="{@name}">
          <xsl:copy-of select="@value"/>
          <xsl:if test="citrus:value">
            <value>
              <xsl:if test="citrus:value/citrus:data">
                <data>
                  <xsl:call-template name="trim">
                    <xsl:with-param name="text" select="citrus:value/citrus:data"/>
                  </xsl:call-template>
                </data>
              </xsl:if>
              <xsl:apply-templates select="citrus:value/citrus:script" mode="script"/>
            </value>
          </xsl:if>
        </variable>
      </xsl:for-each>
    </create-variables>
  </xsl:template>

  <xsl:template match="citrus:trace-variables">
    <xsl:apply-templates select="citrus:description" mode="comment"/>
    <trace-variables>
      <xsl:for-each select="citrus:variable">
        <variable name="{@name}"/>
      </xsl:for-each>
    </trace-variables>
  </xsl:template>

  <xsl:template match="citrus:trace-time">
    <stop-time>
      <xsl:copy-of select="@id|@suffix"/>
      <xsl:apply-templates select="citrus:description"/>
    </stop-time>
  </xsl:template>

  <xsl:template match="citrus:fail">
    <fail>
      <xsl:copy-of select="@message"/>
      <xsl:apply-templates select="citrus:description"/>
    </fail>
  </xsl:template>

  <xsl:template match="citrus:load">
    <load>
      <xsl:for-each select="citrus:properties">
        <properties file="{@file}"/>
      </xsl:for-each>
    </load>
  </xsl:template>

  <xsl:template match="citrus:expect-timeout">
    <xsl:apply-templates select="citrus:description" mode="comment"/>
    <expect-timeout>
      <xsl:copy-of select="@endpoint|@wait"/>
      <xsl:if test="citrus:select">
        <xsl:attribute name="select"><xsl:value-of select="normalize-space(citrus:select)"/></xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="citrus:selector"/>
    </expect-timeout>
  </xsl:template>

  <xsl:template match="citrus:purge-endpoint">
    <xsl:apply-templates select="citrus:description" mode="comment"/>
    <purge-endpoint>
      <xsl:copy-of select="@timeout|@sleep"/>
      <xsl:apply-templates select="citrus:selector"/>
      <xsl:for-each select="citrus:endpoint">
        <endpoint>
          <xsl:copy-of select="@name|@ref"/>
        </endpoint>
      </xsl:for-each>
    </purge-endpoint>
  </xsl:template>

  <xsl:template match="citrus:purge-jms-queues">
    <purge-jms-queues>
      <xsl:copy-of select="@connection-factory|@timeout|@sleep"/>
      <xsl:apply-templates select="citrus:description"/>
      <xsl:for-each select="citrus:queue">
        <queue>
          <xsl:copy-of select="@name|@ref"/>
        </queue>
      </xsl:for-each>
    </purge-jms-queues>
  </xsl:template>

  <xsl:template match="citrus:purge-channel">
    <purge-channels>
      <xsl:copy-of select="@channel-resolver|@message-selector"/>
      <xsl:apply-templates select="citrus:description"/>
      <xsl:for-each select="citrus:channel">
        <channel>
          <xsl:copy-of select="@name|@ref"/>
        </channel>
      </xsl:for-each>
    </purge-channels>
  </xsl:template>

  <xsl:template match="citrus:action">
    <xsl:apply-templates select="citrus:description" mode="comment"/>
    <action reference="{@reference}"/>
  </xsl:template>

  <xsl:template match="citrus:call-template">
    <xsl:apply-templates select="citrus:description" mode="comment"/>
    <apply-template name="{@name}">
      <xsl:if test="citrus:parameter">
        <parameters>
          <xsl:for-each select="citrus:parameter">
            <parameter name="{@name}">
              <xsl:copy-of select="@value"/>
              <xsl:if test="not(@value) and citrus:value">
                <value><xsl:value-of select="citrus:value"/></value>
              </xsl:if>
            </parameter>
          </xsl:for-each>
        </parameters>
      </xsl:if>
    </apply-template>
  </xsl:template>

  <xsl:template match="citrus:transform">
    <transform variable="{@variable}">
      <xsl:apply-templates select="citrus:description"/>
      <source>
        <xsl:copy-of select="citrus:xml-resource/@file|citrus:xml-resource/@charset"/>
        <xsl:value-of select="citrus:xml-data"/>
      </source>
      <xslt>
        <xsl:copy-of select="citrus:xslt-resource/@file|citrus:xslt-resource/@charset"/>
        <xsl:value-of select="citrus:xslt-data"/>
      </xslt>
    </transform>
  </xsl:template>

  <xsl:template match="citrus:ant">
    <ant>
      <xsl:copy-of select="@build-file|@build-listener"/>
      <xsl:apply-templates select="citrus:description"/>
      <xsl:for-each select="citrus:execute">
        <execute>
          <xsl:copy-of select="@target|@targets"/>
        </execute>
      </xsl:for-each>
      <xsl:for-each select="citrus:properties">
        <properties>
          <xsl:copy-of select="@file"/>
          <xsl:for-each select="citrus:property">
            <property name="{@name}" value="{@value}"/>
          </xsl:for-each>
        </properties>
      </xsl:for-each>
    </ant>
  </xsl:template>

  <xsl:template match="citrus:start">
    <xsl:apply-templates select="citrus:description" mode="comment"/>
    <start>
      <xsl:call-template name="servers"/>
    </start>
  </xsl:template>

  <xsl:template match="citrus:stop">
    <xsl:apply-templates select="citrus:description" mode="comment"/>
    <stop>
      <xsl:call-template name="servers"/>
    </stop>
  </xsl:template>

  <xsl:template name="servers">
    <xsl:copy-of select="@server"/>
    <xsl:if test="citrus:servers">
      <servers>
        <xsl:for-each select="citrus:servers/citrus:server">
          <server name="{@name}"/>
        </xsl:for-each>
      </servers>
    </xsl:if>
  </xsl:template>

  <xsl:template match="citrus:groovy">
    <groovy>
      <script>
        <xsl:if test="@resource">
          <xsl:attribute name="file"><xsl:value-of select="@resource"/></xsl:attribute>
        </xsl:if>
        <xsl:copy-of select="@use-script-template|@script-template"/>
        <xsl:value-of select="."/>
      </script>
    </groovy>
  </xsl:template>

  <!-- ============================================================== -->
  <!-- Database actions                                               -->
  <!-- ============================================================== -->

  <xsl:template match="citrus:sql">
    <sql>
      <xsl:copy-of select="@datasource|@ignore-errors"/>
      <xsl:apply-templates select="citrus:description"/>
      <xsl:call-template name="transaction"/>
      <statements>
        <xsl:copy-of select="citrus:resource/@file"/>
        <xsl:for-each select="citrus:statement">
          <statement><xsl:value-of select="."/></statement>
        </xsl:for-each>
      </statements>
      <xsl:for-each select="citrus:validate">
        <validate column="{@column}">
          <xsl:copy-of select="@value"/>
          <xsl:if test="citrus:values">
            <values>
              <xsl:for-each select="citrus:values/citrus:value">
                <value><xsl:value-of select="."/></value>
              </xsl:for-each>
            </values>
          </xsl:if>
        </validate>
      </xsl:for-each>
      <xsl:for-each select="citrus:validate-script">
        <validate>
          <xsl:apply-templates select="." mode="script"/>
        </validate>
      </xsl:for-each>
      <xsl:for-each select="citrus:extract">
        <extract column="{@column}" variable="{@variable}"/>
      </xsl:for-each>
    </sql>
  </xsl:template>

  <xsl:template match="citrus:plsql">
    <plsql>
      <xsl:copy-of select="@datasource|@ignore-errors"/>
      <xsl:apply-templates select="citrus:description"/>
      <xsl:call-template name="transaction"/>
      <statements>
        <xsl:copy-of select="citrus:resource/@file"/>
        <xsl:if test="citrus:script">
          <script><xsl:value-of select="citrus:script"/></script>
        </xsl:if>
      </statements>
    </plsql>
  </xsl:template>

  <xsl:template name="transaction">
    <xsl:if test="@transaction-manager or @transaction-timeout or @transaction-isolation-level">
      <transaction>
        <xsl:if test="@transaction-manager">
          <xsl:attribute name="manager"><xsl:value-of select="@transaction-manager"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="@transaction-timeout">
          <xsl:attribute name="timeout"><xsl:value-of select="@transaction-timeout"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="@transaction-isolation-level">
          <xsl:attribute name="isolation-level"><xsl:value-of select="@transaction-isolation-level"/></xsl:attribute>
        </xsl:if>
      </transaction>
    </xsl:if>
  </xsl:template>

  <!-- ============================================================== -->
  <!-- Test action containers                                         -->
  <!-- ============================================================== -->

  <xsl:template match="citrus:sequential">
    <sequential>
      <xsl:apply-templates select="citrus:description"/>
      <actions>
        <xsl:call-template name="nested-actions"/>
      </actions>
    </sequential>
  </xsl:template>

  <xsl:template match="citrus:parallel">
    <parallel>
      <xsl:apply-templates select="citrus:description"/>
      <actions>
        <xsl:call-template name="nested-actions"/>
      </actions>
    </parallel>
  </xsl:template>

  <xsl:template match="citrus:catch">
    <catch>
      <xsl:copy-of select="@exception"/>
      <xsl:apply-templates select="citrus:description"/>
      <when>
        <xsl:call-template name="nested-actions"/>
      </when>
    </catch>
  </xsl:template>

  <xsl:template match="citrus:assert">
    <assert>
      <xsl:copy-of select="@exception|@message"/>
      <xsl:apply-templates select="citrus:description"/>
      <when>
        <xsl:apply-templates select="citrus:when/*"/>
      </when>
    </assert>
  </xsl:template>

  <xsl:template match="citrus:conditional">
    <conditional when="{@expression}">
      <xsl:apply-templates select="citrus:description"/>
      <actions>
        <xsl:call-template name="nested-actions"/>
      </actions>
    </conditional>
  </xsl:template>

  <xsl:template match="citrus:iterate">
    <xsl:call-template name="unsupported-timeout"/>
    <iterate>
      <xsl:copy-of select="@condition|@index|@step"/>
      <xsl:if test="@start">
        <xsl:attribute name="startsWith"><xsl:value-of select="@start"/></xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="citrus:description"/>
      <actions>
        <xsl:call-template name="nested-actions"/>
      </actions>
    </iterate>
  </xsl:template>

  <xsl:template match="citrus:repeat-until-true">
    <xsl:call-template name="unsupported-timeout"/>
    <repeat until="{@condition}">
      <xsl:copy-of select="@index"/>
      <xsl:apply-templates select="citrus:description"/>
      <actions>
        <xsl:call-template name="nested-actions"/>
      </actions>
    </repeat>
  </xsl:template>

  <xsl:template match="citrus:repeat-onerror-until-true">
    <xsl:call-template name="unsupported-timeout"/>
    <repeat-on-error until="{@condition}">
      <xsl:copy-of select="@index|@auto-sleep"/>
      <xsl:apply-templates select="citrus:description"/>
      <actions>
        <xsl:call-template name="nested-actions"/>
      </actions>
    </repeat-on-error>
  </xsl:template>

  <xsl:template match="citrus:async">
    <async>
      <xsl:apply-templates select="citrus:description"/>
      <actions>
        <xsl:apply-templates select="citrus:actions/*"/>
      </actions>
      <xsl:if test="citrus:error">
        <error>
          <xsl:apply-templates select="citrus:error/*"/>
        </error>
      </xsl:if>
      <xsl:if test="citrus:success">
        <success>
          <xsl:apply-templates select="citrus:success/*"/>
        </success>
      </xsl:if>
    </async>
  </xsl:template>

  <xsl:template match="citrus:timer">
    <timer>
      <xsl:copy-of select="@id|@interval|@delay|@repeatCount|@fork"/>
      <xsl:apply-templates select="citrus:description"/>
      <actions>
        <xsl:call-template name="nested-actions"/>
      </actions>
    </timer>
  </xsl:template>

  <xsl:template match="citrus:stop-timer">
    <stop-timer>
      <xsl:if test="@timerId">
        <xsl:attribute name="id"><xsl:value-of select="@timerId"/></xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="citrus:description"/>
    </stop-timer>
  </xsl:template>

  <!-- The plain XML "timeout" attribute uses milliseconds whereas the Spring XML action supports seconds and milliseconds -->
  <xsl:template match="citrus:wait">
    <wait-for>
      <xsl:copy-of select="@interval"/>
      <xsl:choose>
        <xsl:when test="@milliseconds">
          <xsl:attribute name="timeout"><xsl:value-of select="@milliseconds"/></xsl:attribute>
        </xsl:when>
        <xsl:when test="@seconds and string(number(@seconds)) != 'NaN'">
          <xsl:attribute name="timeout"><xsl:value-of select="round(number(@seconds) * 1000)"/></xsl:attribute>
        </xsl:when>
        <xsl:when test="@seconds">
          <xsl:call-template name="warn">
            <xsl:with-param name="message">Unable to convert wait timeout of '<xsl:value-of select="@seconds"/>' seconds - please set the timeout in milliseconds manually</xsl:with-param>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
      <xsl:apply-templates select="citrus:description"/>
      <xsl:for-each select="citrus:http">
        <http>
          <xsl:copy-of select="@url|@method|@status|@timeout"/>
        </http>
      </xsl:for-each>
      <xsl:for-each select="citrus:file">
        <file path="{@path}"/>
      </xsl:for-each>
      <xsl:for-each select="citrus:message">
        <message name="{@name}"/>
      </xsl:for-each>
      <xsl:for-each select="citrus:action">
        <action>
          <xsl:apply-templates select="*"/>
        </action>
      </xsl:for-each>
    </wait-for>
  </xsl:template>

  <xsl:template name="unsupported-timeout">
    <xsl:if test="@timeout">
      <xsl:call-template name="warn">
        <xsl:with-param name="message">The '<xsl:value-of select="local-name()"/>' container timeout is not supported in the plain XML DSL and has been removed</xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <!-- ============================================================== -->
  <!-- Docker actions                                                 -->
  <!-- ============================================================== -->

  <xsl:template match="docker:info|docker:ping|docker:version|docker:build|docker:pull|docker:inspect|docker:remove|docker:start|docker:stop|docker:create">
    <docker>
      <xsl:if test="@docker-client">
        <xsl:attribute name="client"><xsl:value-of select="@docker-client"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="docker:expect/docker:result">
        <xsl:attribute name="result">
          <xsl:call-template name="trim">
            <xsl:with-param name="text" select="docker:expect/docker:result"/>
          </xsl:call-template>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="docker:description" mode="ext"/>
      <xsl:apply-templates select="." mode="docker-command"/>
    </docker>
  </xsl:template>

  <xsl:template match="docker:info|docker:ping|docker:version" mode="docker-command">
    <xsl:element name="{local-name()}"/>
  </xsl:template>

  <xsl:template match="docker:build" mode="docker-command">
    <build-image>
      <xsl:copy-of select="@tag|@basedir|@no-cache|@quiet|@remove"/>
      <xsl:if test="@dockerfile">
        <xsl:attribute name="docker-file"><xsl:value-of select="@dockerfile"/></xsl:attribute>
      </xsl:if>
    </build-image>
  </xsl:template>

  <xsl:template match="docker:pull" mode="docker-command">
    <pull-image>
      <xsl:copy-of select="@image|@tag|@registry|@repository"/>
    </pull-image>
  </xsl:template>

  <xsl:template match="docker:inspect" mode="docker-command">
    <xsl:choose>
      <xsl:when test="@image">
        <inspect-image image="{@image}"/>
      </xsl:when>
      <xsl:otherwise>
        <inspect>
          <xsl:copy-of select="@container"/>
        </inspect>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="docker:remove" mode="docker-command">
    <xsl:choose>
      <xsl:when test="@image">
        <remove-image image="{@image}">
          <xsl:copy-of select="@force"/>
        </remove-image>
      </xsl:when>
      <xsl:otherwise>
        <remove>
          <xsl:copy-of select="@container|@force"/>
        </remove>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="docker:start|docker:stop" mode="docker-command">
    <xsl:element name="{local-name()}">
      <xsl:copy-of select="@container"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="docker:create" mode="docker-command">
    <create>
      <xsl:copy-of select="@image|@name|@capability-add|@capability-drop|@attach-stderr|@attach-stdin|@attach-stdout|@domain-name|@cmd|@env|@hostname|@entrypoint|@port-specs|@port-bindings|@exposed-ports|@volumes|@working-dir"/>
    </create>
  </xsl:template>

  <!-- ============================================================== -->
  <!-- Camel actions                                                  -->
  <!-- ============================================================== -->

  <xsl:template match="camel:create-routes|camel:start-routes|camel:stop-routes|camel:remove-routes|camel:control-bus">
    <camel>
      <xsl:copy-of select="@camel-context"/>
      <xsl:apply-templates select="camel:description" mode="ext"/>
      <xsl:element name="{local-name()}">
        <xsl:apply-templates select="*[not(self::camel:description)]" mode="camel"/>
      </xsl:element>
    </camel>
  </xsl:template>

  <xsl:template match="camel:route|camel:language|camel:result" mode="camel">
    <xsl:element name="{local-name()}">
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="."/>
    </xsl:element>
  </xsl:template>

  <!-- Embedded Camel route definitions are copied like message payloads -->
  <xsl:template match="*" mode="camel">
    <xsl:apply-templates select="." mode="payload"/>
  </xsl:template>

  <!-- ============================================================== -->
  <!-- Selenium actions                                               -->
  <!-- ============================================================== -->

  <xsl:template match="selenium:start|selenium:stop|selenium:clear-cache|selenium:page|selenium:click|selenium:hover|selenium:set-input|selenium:check-input|selenium:dropdown-select|selenium:wait|selenium:javascript|selenium:screenshot|selenium:navigate|selenium:open-window|selenium:close-window|selenium:switch-window|selenium:store-file|selenium:get-stored-file|selenium:alert|selenium:find">
    <selenium>
      <xsl:copy-of select="@browser"/>
      <xsl:apply-templates select="selenium:description" mode="ext"/>
      <xsl:apply-templates select="." mode="selenium-action"/>
    </selenium>
  </xsl:template>

  <xsl:template match="selenium:start|selenium:stop|selenium:clear-cache" mode="selenium-action">
    <xsl:element name="{local-name()}"/>
  </xsl:template>

  <xsl:template match="selenium:click|selenium:hover|selenium:set-input|selenium:check-input|selenium:dropdown-select|selenium:wait" mode="selenium-action">
    <xsl:element name="{local-name()}">
      <xsl:copy-of select="@*[local-name() != 'browser']"/>
      <xsl:apply-templates select="selenium:element" mode="selenium-element"/>
      <xsl:apply-templates select="selenium:options" mode="retarget"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="selenium:page|selenium:navigate|selenium:open-window|selenium:close-window|selenium:switch-window|selenium:store-file|selenium:get-stored-file|selenium:screenshot" mode="selenium-action">
    <xsl:element name="{local-name()}">
      <xsl:copy-of select="@*[local-name() != 'browser']"/>
      <xsl:apply-templates select="selenium:arguments" mode="retarget"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="selenium:javascript" mode="selenium-action">
    <javascript>
      <script><xsl:value-of select="selenium:script"/></script>
      <xsl:apply-templates select="selenium:errors" mode="retarget"/>
    </javascript>
  </xsl:template>

  <xsl:template match="selenium:alert" mode="selenium-action">
    <alert>
      <xsl:copy-of select="@accept"/>
      <xsl:if test="@text or selenium:alert-text">
        <text>
          <xsl:choose>
            <xsl:when test="selenium:alert-text"><xsl:value-of select="selenium:alert-text"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="@text"/></xsl:otherwise>
          </xsl:choose>
        </text>
      </xsl:if>
    </alert>
  </xsl:template>

  <!-- The Spring XML element holds the locator as well as the expectations, the plain XML DSL separates them -->
  <xsl:template match="selenium:find" mode="selenium-action">
    <find>
      <xsl:apply-templates select="selenium:element" mode="selenium-element"/>
      <validate>
        <xsl:copy-of select="selenium:element/@text|selenium:element/@displayed|selenium:element/@enabled"/>
        <xsl:if test="selenium:element/@validate-tag-name">
          <xsl:attribute name="tag-name"><xsl:value-of select="selenium:element/@validate-tag-name"/></xsl:attribute>
        </xsl:if>
        <xsl:apply-templates select="selenium:element/selenium:attributes|selenium:element/selenium:styles" mode="retarget"/>
      </validate>
    </find>
  </xsl:template>

  <xsl:template match="selenium:element" mode="selenium-element">
    <element>
      <xsl:copy-of select="@id|@name|@tag-name|@class-name|@css-selector|@link-text|@xpath"/>
    </element>
  </xsl:template>

  <!-- Recreates the element in the plain XML DSL namespace -->
  <xsl:template match="*" mode="retarget">
    <xsl:element name="{local-name()}">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="node()" mode="retarget"/>
    </xsl:element>
  </xsl:template>

  <!-- Descriptions of the module namespaces are kept as description element -->
  <xsl:template match="*" mode="ext">
    <description>
      <xsl:call-template name="trim">
        <xsl:with-param name="text" select="."/>
      </xsl:call-template>
    </description>
  </xsl:template>

  <!-- ============================================================== -->
  <!-- Unsupported elements                                           -->
  <!-- ============================================================== -->

  <!--
    Test actions without a plain XML counterpart (e.g. java, input) and actions from other Spring XML namespaces
    (e.g. http, ws, jms) are copied as is so the information is not lost and the user is able to migrate them manually.
  -->
  <xsl:template match="*">
    <xsl:call-template name="warn">
      <xsl:with-param name="message">Unsupported test action '<xsl:value-of select="name()"/>' (<xsl:value-of select="namespace-uri()"/>) has been copied as is - please migrate it manually</xsl:with-param>
    </xsl:call-template>
    <xsl:copy-of select="."/>
  </xsl:template>

  <!-- ============================================================== -->
  <!-- Helpers                                                        -->
  <!-- ============================================================== -->

  <xsl:template name="warn">
    <xsl:param name="message"/>
    <xsl:comment>
      <xsl:text> </xsl:text>
      <xsl:value-of select="$warning"/>
      <xsl:call-template name="comment-safe">
        <xsl:with-param name="text" select="$message"/>
      </xsl:call-template>
      <xsl:text> </xsl:text>
    </xsl:comment>
  </xsl:template>

  <!-- XML comments must not contain double hyphens -->
  <xsl:template name="comment-safe">
    <xsl:param name="text"/>
    <xsl:choose>
      <xsl:when test="contains($text, '--')">
        <xsl:value-of select="substring-before($text, '--')"/>
        <xsl:text>- </xsl:text>
        <xsl:call-template name="comment-safe">
          <xsl:with-param name="text" select="concat('-', substring-after($text, '--'))"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="trim">
    <xsl:param name="text"/>
    <xsl:variable name="leading" select="substring-before($text, substring(normalize-space($text), 1, 1))"/>
    <xsl:variable name="content" select="substring($text, string-length($leading) + 1)"/>
    <xsl:call-template name="trim-right">
      <xsl:with-param name="text" select="$content"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="trim-right">
    <xsl:param name="text"/>
    <xsl:variable name="last" select="substring($text, string-length($text))"/>
    <xsl:choose>
      <xsl:when test="string-length($text) &gt; 0 and normalize-space($last) = ''">
        <xsl:call-template name="trim-right">
          <xsl:with-param name="text" select="substring($text, 1, string-length($text) - 1)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
