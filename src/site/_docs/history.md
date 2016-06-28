---
title: History
layout: docs
permalink: "/docs/history/"
---

## Release ${project.version} / {{ site.time | date: '%Y-%m-%d' }}
{: #latest}

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
|      |    |         |    |
| Fix | [#107](https://github.com/christophd/citrus/issues/107) | Documentation wrong XML tags | christophd |
| Fix | [#106](https://github.com/christophd/citrus/issues/106) | Documentation wait wrong argument in Java DSL | christophd |
| Fix | [#105](https://github.com/christophd/citrus/issues/105) | Documentation missing maven dependencies | christophd |

## Release 2.6 / 2016-06-27
{: #v2-6}

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
| Fix | [#101](https://github.com/christophd/citrus/issues/101) | NoSuchElementException with custom action container in test runner | christophd |
| Fix | [#100](https://github.com/christophd/citrus/issues/100) | Java DSL should support test behavior inside test container | christophd |
| Add | [#57](https://tree.taiga.io/project/christophd-citrus/us/57) | Hamcrest matcher in iteration conditions | christophd |
| Add | [#56](https://tree.taiga.io/project/christophd-citrus/us/56) | Cucumber BDD support | christophd |
| Add | [#53](https://tree.taiga.io/project/christophd-citrus/us/53) | Spring RestDocs support | christophd |
| Add | [#7](https://tree.taiga.io/project/christophd-citrus/us/7) | Zookeeper support | maherma |
| Fix | [#99](https://github.com/christophd/citrus/issues/99) | Set custom message type in Java DSL | christophd |
| Fix | [#97](https://github.com/christophd/citrus/issues/97) | Support configurable paths for reporters | christophd |
| Fix | [#96](https://github.com/christophd/citrus/issues/96) | Unique particle attribution #cos-nonambig violation in citrus-ws-testcase XSD | christophd |
| Fix | [#95](https://github.com/christophd/citrus/issues/95) | HttpStatus validation broken for BAD_REQUEST status code | christophd |
| Fix | [#94](https://github.com/christophd/citrus/issues/94) | escapeXml() not working with ',' in sub-XML | christophd |
| Fix | [#93](https://github.com/christophd/citrus/issues/93) | Links to blogs on front page is 404 | christophd |

## Release 2.5.2 / 2016-04-01
{: #v2-5-2}

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
| Add | [#50](https://tree.taiga.io/project/christophd-citrus/us/50) | Support Hamcrest matchers | christophd |
| Add | [#49](https://tree.taiga.io/project/christophd-citrus/us/49) | Support Json object functions in JsonPath validation | christophd |
| Add | [#47](https://tree.taiga.io/project/christophd-citrus/us/47) | BinaryBase64 message validator | christophd |
| Add | [#46](https://tree.taiga.io/project/christophd-citrus/us/46) | Annotation config support for all modules | christophd |
| Fix | [#89](https://github.com/christophd/citrus/issues/89) | StringIndexOutOfBoundsException in XMLUtils.getTargetCharset() | christophd |
| Fix | [#90](https://github.com/christophd/citrus/issues/90) | Send binary messages with XML DSL | christophd |
| Fix | [#87](https://github.com/christophd/citrus/issues/87) | Validation matcher support in PlainTextMessageValidator | christophd |
| Fix | [#86](https://github.com/christophd/citrus/issues/86) | XML comments will counted as "normal" child element | christophd |
| Fix | [#85](https://github.com/christophd/citrus/issues/85) | XML comments before root element skip validation | christophd |
| Fix | [#84](https://github.com/christophd/citrus/issues/84) | Custom-Function, unclear Exception-Message | christophd |
| Fix | [#83](https://github.com/christophd/citrus/issues/83) | TestNG DataProvider and CitrusResource injection of TestRunner not working properly | christophd |
| Fix | [#82](https://github.com/christophd/citrus/issues/82) | Multithreaded selectors throw a ConcurrentModificationException | christophd |
| Fix | [#80](https://github.com/christophd/citrus/issues/80) | Empty message payload support in JSON data dictionary | christophd |
| Fix | [#79](https://github.com/christophd/citrus/issues/79) | SQL query file resource produces SQLSyntaxError | christophd |

## Release 2.5.1 / 2016-03-03
{: #v2-5-1}

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
| Add | [#46](https://tree.taiga.io/project/christophd-citrus/us/46) | Annotation config support | christophd |
| Add | [#41](https://tree.taiga.io/project/christophd-citrus/us/41) | Citrus main application class | christophd |
| Fix | [#78](https://github.com/christophd/citrus/issues/78) | Dynamic endpoints not working for http DSL | christophd |
| Fix | [#77](https://github.com/christophd/citrus/issues/77) | Missing 2.5 xsd references in spring.schemas | christophd |
| Fix | [#76](https://github.com/christophd/citrus/issues/76) | Nullpointer in after suite | christophd |
| Fix | [#74](https://github.com/christophd/citrus/issues/74) | MailServer stops on emtpy line | gucce |
| Fix | [#73](https://github.com/christophd/citrus/issues/73) | Cant mvn install citrus with Java 1.7 | christophd |
| Fix | [#70](https://github.com/christophd/citrus/issues/70) | Message payload lost for Http PATCH method | christophd |
| Fix | [#67](https://github.com/christophd/citrus/issues/67) | Form urlencoded marshaller using wrong xsd location | christophd |
| Fix | [#66](https://github.com/christophd/citrus/issues/66) | Empty Http request headers missing on server receive | christophd |
| Fix | [#65](https://github.com/christophd/citrus/issues/65) | Variable extractor not working in http:receive-response XML | christophd |

## Release 2.5 / 2016-01-28
{: #v2-5}

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
| Add | [#39](https://tree.taiga.io/project/christophd-citrus/us/39) | TestContext injection | christophd |
| Add | [#37](https://tree.taiga.io/project/christophd-citrus/us/37) | x-www-form-urlencoded message validator | christophd |
| Fix | [#62](https://github.com/christophd/citrus/issues/62) | DataDictionary not settable via Java DSL | christophd |
| Fix | [#61](https://github.com/christophd/citrus/issues/61) | Global scoped data dictionaries breaking message receipt | christophd |
| Fix | [#60](https://github.com/christophd/citrus/issues/60) | ClassPathResource name needs correction in User Guide | christophd |
| Fix | [#59](https://github.com/christophd/citrus/issues/59) | Docker action sharing docker command instance | christophd |
| Fix | [#58](https://github.com/christophd/citrus/issues/58) | JUnit4CitrusTestDesigner validateScript doesn't work properly | christophd |
| Fix | [#55](https://github.com/christophd/citrus/issues/55) | x-www-form-urlencoded payload lost | christophd |
| Fix | [#50](https://github.com/christophd/citrus/issues/50) | allow variable value definition in cdata section | christophd |
| Add | [#36](https://tree.taiga.io/project/christophd-citrus/us/36) | RMI support | christophd |
| Update | [#34](https://tree.taiga.io/project/christophd-citrus/us/34) | Cleanup validation matcher support | maherma |
| Update | [#32](https://tree.taiga.io/project/christophd-citrus/us/32) | Use Mockito instead of EasyMock | christophd |
| Add | [#31](https://tree.taiga.io/project/christophd-citrus/us/31) | Date range validation matcher | maherma |
| Add | [#29](https://tree.taiga.io/project/christophd-citrus/us/29) | Read file resource function | christophd |
| Add | [#24](https://tree.taiga.io/project/christophd-citrus/us/24) | Timer action container | maherma |
| Update | [#23](https://tree.taiga.io/project/christophd-citrus/us/23) | Optimize Jett-9 startup | maherma |
| Update | [#17](https://tree.taiga.io/project/christophd-citrus/us/17) | Update to Vert.x 3.0 | maherma |

## Release 2.4 / 2015-11-03
{: #v2-4}

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
| Fix | [#47](https://github.com/christophd/citrus/issues/47) | JSON data dictionary throws NullPointerException | christophd |
| Update | [#27](https://tree.taiga.io/project/christophd-citrus/us/27) | Validation callback with TestContext | christophd |
| Update | [#26](https://tree.taiga.io/project/christophd-citrus/us/26) | Improved REST support | christophd |
| Add | [#22](https://tree.taiga.io/project/christophd-citrus/us/22) | Wait-Condition Action | maherma |
| Add | [#21](https://tree.taiga.io/project/christophd-citrus/us/21) | Camel route test actions | christophd |
| Update | [#20](https://tree.taiga.io/project/christophd-citrus/us/20) | Http DELETE with payload | christophd |
| Add | [#19](https://tree.taiga.io/project/christophd-citrus/us/19) | Purge endpoints action | christophd |
| Update | [#14](https://tree.taiga.io/project/christophd-citrus/us/14) | Update plugin versions | christophd |
| Update | [#13](https://tree.taiga.io/project/christophd-citrus/us/13) | Update dependency versions | christophd |
| Add | [#12](https://tree.taiga.io/project/christophd-citrus/us/12) | Release archetypes to central | christophd |
| Add | [#11](https://tree.taiga.io/project/christophd-citrus/us/11) | Release to Maven central | christophd |
| Add | [#6](https://tree.taiga.io/project/christophd-citrus/us/6) | Docker support | christophd |

## Release 2.3 / 2015-08-18
{: #v2-3}

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
| Fix | [#45](https://github.com/christophd/citrus/issues/45) | Assertion error in parallel not causing test case to fail | christophd |
| Fix | [#264](https://citrusframework.atlassian.net/browse/CITRUS-264) | Camel direct endpoint consumer caching | christophd |
| Add | [#263](https://citrusframework.atlassian.net/browse/CITRUS-263) | Customize message validators | christophd |
| Update | [#262](https://citrusframework.atlassian.net/browse/CITRUS-262) | Boolean expression evaluation in Java DSL | christophd |
| Add | [#255](https://citrusframework.atlassian.net/browse/CITRUS-255) | Refactor Java DSL test action execution (TestRunner) | christophd |
| Update | [#253](https://citrusframework.atlassian.net/browse/CITRUS-253) | Update Jetty version | maherma |
| Add | [#248](https://citrusframework.atlassian.net/browse/CITRUS-248) | Websocket support | maherma |
| Add | [#196](https://citrusframework.atlassian.net/browse/CITRUS-196) | JSONPath support | christophd |

## Release 2.2 / 2015-06-26
{: #v2-2}

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
| Fix | [#261](https://citrusframework.atlassian.net/browse/CITRUS-261) | Http server response message tracing | christophd |
| Add | [#260](https://citrusframework.atlassian.net/browse/CITRUS-260) | Customize dynamic endpoint names | christophd |
| Fix | [#259](https://citrusframework.atlassian.net/browse/CITRUS-259) | Nullpointer in SoapMessageConverter | christophd |
| Update | [#254](https://citrusframework.atlassian.net/browse/CITRUS-254) | Citrus annotation support in JUnit | christophd |
| Fix | [#252](https://citrusframework.atlassian.net/browse/CITRUS-252) | ANT tasks and tutorial | christophd |
| Add | [#250](https://citrusframework.atlassian.net/browse/CITRUS-250) | Start/Stop server action | christophd |
| Add | [#249](https://citrusframework.atlassian.net/browse/CITRUS-249) | Arquillian integration | christophd |
| Fix | [#245](https://citrusframework.atlassian.net/browse/CITRUS-245) | Iterating action container in loops | christophd |

## Release 2.1 / 2015-02-20
{: #v2-1}

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
| Update | [#244](https://citrusframework.atlassian.net/browse/CITRUS-244) | Keep SOAP envelope for incoming requests | christophd |
| Update | [#243](https://citrusframework.atlassian.net/browse/CITRUS-243) | Ssh request/response XSD schema | christophd |
| Fix | [#242](https://citrusframework.atlassian.net/browse/CITRUS-242) | WSDL includes not working in schema validation | christophd |
| Add | [#241](https://citrusframework.atlassian.net/browse/CITRUS-241) | SOAP MTOM support | christophd |
| Fix | [#240](https://citrusframework.atlassian.net/browse/CITRUS-240) | Missing Java 7 requirements in user guide | christophd |
| Fix | [#239](https://citrusframework.atlassian.net/browse/CITRUS-239) | Java test action cahing String[] method parameters | christophd |
| Fix | [#238](https://citrusframework.atlassian.net/browse/CITRUS-238) | Conversion exception when logging object message payloads | christophd |
| Update | [#237](https://citrusframework.atlassian.net/browse/CITRUS-237) | TestNG data provider handling | christophd |
| Fix | [#236](https://citrusframework.atlassian.net/browse/CITRUS-236) | Fallback endpoint adapter not settable | christophd |
| Fix | [#235](https://citrusframework.atlassian.net/browse/CITRUS-235) | SOAP 1.2 server support missing | christophd |
| Fix | [#208](https://citrusframework.atlassian.net/browse/CITRUS-208) | Namespaces lost in SOAP envelope | christophd |
| Fix | [#206](https://citrusframework.atlassian.net/browse/CITRUS-206) | Mail message payload namespace | christophd |
| Fix | [#193](https://citrusframework.atlassian.net/browse/CITRUS-193) | Improve TestNG data provider documentation | christophd |

## Release 2.0 / 2014-11-01
{: #v2-0}

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
| Update | [#234](https://citrusframework.atlassian.net/browse/CITRUS-234) | Remove deprecated classes | christophd |
| Update | [#233](https://citrusframework.atlassian.net/browse/CITRUS-233) | Separate citrus-jms module | christophd |
| Add | [#232](https://citrusframework.atlassian.net/browse/CITRUS-232) | Use message converter pattern | christophd |
| Fix | [#231](https://citrusframework.atlassian.net/browse/CITRUS-231) | Schema validation on xs:any | christophd |
| Update | [#230](https://citrusframework.atlassian.net/browse/CITRUS-230) | Upgrade to Spring 4.x | christophd |
| Add | [#229](https://citrusframework.atlassian.net/browse/CITRUS-229) | Update dependency and Maven plugin versions | christophd |
| Fix | [#228](https://citrusframework.atlassian.net/browse/CITRUS-228) | TestNG parameters in Java DSL | christophd |
| Update | [#227](https://citrusframework.atlassian.net/browse/CITRUS-227) | Use TestContext in validation matcher interface | christophd |
| Update | [#226](https://citrusframework.atlassian.net/browse/CITRUS-226) | Use TestContext in function interface | christophd |
| Add | [#225](https://citrusframework.atlassian.net/browse/CITRUS-225) | Create variable validation matcher | christophd |
| Add | [#224](https://citrusframework.atlassian.net/browse/CITRUS-224) | Namespace context configuration component | christophd |
| Update | [#223](https://citrusframework.atlassian.net/browse/CITRUS-223) | Correlate messages by default | christophd |
| Add | [#222](https://citrusframework.atlassian.net/browse/CITRUS-222) | Multiple SOAP attachments | christophd |
| Add | [#221](https://citrusframework.atlassian.net/browse/CITRUS-221) | Multiple SOAP header fragments | christophd |
| Add | [#220](https://citrusframework.atlassian.net/browse/CITRUS-220) | Data dictionary schema component | christophd |
| Update | [#219](https://citrusframework.atlassian.net/browse/CITRUS-219) | Auto sleep in milliseconds | christophd |
| Update | [#218](https://citrusframework.atlassian.net/browse/CITRUS-218) | Sleep action in milliseconds | christophd |
| Update | [#217](https://citrusframework.atlassian.net/browse/CITRUS-217) | Rework MessageListener interface | christophd |
| Update | [#216](https://citrusframework.atlassian.net/browse/CITRUS-216) | Log Citrus version | christophd |
| Update | [#215](https://citrusframework.atlassian.net/browse/CITRUS-215) | Rework root application context | christophd |
| Add | [#214](https://citrusframework.atlassian.net/browse/CITRUS-214) | ValidationMatcher XML schema component | christophd |
| Add | [#213](https://citrusframework.atlassian.net/browse/CITRUS-213) | Function library component | christophd |
| Add | [#212](https://citrusframework.atlassian.net/browse/CITRUS-212) | Sequence after test component | christophd |
| Add | [#211](https://citrusframework.atlassian.net/browse/CITRUS-211) | Sequence before/after suite components | christophd |
| Update | [#210](https://citrusframework.atlassian.net/browse/CITRUS-210) | Rework sample applications | christophd |
| Update | [#209](https://citrusframework.atlassian.net/browse/CITRUS-209) | Rework Maven plugin | christophd |
| Add | [#188](https://citrusframework.atlassian.net/browse/CITRUS-188) | Fail fast when validator is missing | christophd |
| Add | [#171](https://citrusframework.atlassian.net/browse/CITRUS-171) | Ftp server adapter | christophd |
| Add | [#191](https://citrusframework.atlassian.net/browse/CITRUS-191) | JMS Soap message converter | christophd |
| Add | [#90](https://citrusframework.atlassian.net/browse/CITRUS-90) | Citrus Jms header mapper | christophd |

## Release 1.4.1 / 2014-07-11
{: #v1-4-1}

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
| Add | [#207](https://citrusframework.atlassian.net/browse/CITRUS-207) | Dynamic endpoint components | christophd |
| Add | [#205](https://citrusframework.atlassian.net/browse/CITRUS-205) | Apache Camel support | christophd |
| Add | [#203](https://citrusframework.atlassian.net/browse/CITRUS-203) | Vert.x support | christophd |
| Fix | [#202](https://citrusframework.atlassian.net/browse/CITRUS-202) | Missing soap must understand interceptor support | christophd |
| Fix | [#201](https://citrusframework.atlassian.net/browse/CITRUS-201) | Custom interceptors in citrus-ws server component not loaded | christophd |

## Release 1.4 / 2014-05-02
{: #v1-4}

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
| Add | [#200](https://citrusframework.atlassian.net/browse/CITRUS-200) | Global variables component | christophd |
| Fix | [#199](https://citrusframework.atlassian.net/browse/CITRUS-199) | Auto sleep < 1 second | christophd |
| Add | [#198](https://citrusframework.atlassian.net/browse/CITRUS-198) | JSON text validator strict mode | christophd |
| Fix | [#197](https://citrusframework.atlassian.net/browse/CITRUS-197) | Attachment spelling in Java DSL | christophd |
| Add | [#195](https://citrusframework.atlassian.net/browse/CITRUS-195) | Java DSL Http specific send options | christophd |
| Fix | [#194](https://citrusframework.atlassian.net/browse/CITRUS-194) | Misleading create variables action in Java DSL | christophd |
| Fix | [#192](https://citrusframework.atlassian.net/browse/CITRUS-192) | Nested anonymous test actions fail | christophd |
| Update | [#190](https://citrusframework.atlassian.net/browse/CITRUS-190) | Doku: FTP adapter description | christophd |
| Fix | [#187](https://citrusframework.atlassian.net/browse/CITRUS-187) | Class cast exceptions when using object variable value | christophd |
| Fix | [#186](https://citrusframework.atlassian.net/browse/CITRUS-186) | AssertionErrors not handled | christophd |
| Fix | [#185](https://citrusframework.atlassian.net/browse/CITRUS-185) | SSHClient with just username/password | christophd |
| Add | [#184](https://citrusframework.atlassian.net/browse/CITRUS-184) | Mail adapter | christophd |
| Add | [#183](https://citrusframework.atlassian.net/browse/CITRUS-183) | SOAP Http Uri as header | christophd |
| Add | [#166](https://citrusframework.atlassian.net/browse/CITRUS-166) | XML data dictionary | christophd |

## Release 1.3.1 / 2013-09-06
{: #v1-3-1}

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
| Update | [#182](https://citrusframework.atlassian.net/browse/CITRUS-182) | Support multiple test methods in Java DSL TestBuilder class | christophd |
| Update | [#181](https://citrusframework.atlassian.net/browse/CITRUS-181) | Add true/false to BooleanExpressionParser | christophd |
| Fix | [#180](https://citrusframework.atlassian.net/browse/CITRUS-180) | Attribute validation xsi:type namespace dependent | christophd |
| Fix | [#179](https://citrusframework.atlassian.net/browse/CITRUS-179) | XHTML user guide missing tidy dependency | christophd |
| Fix | [#178](https://citrusframework.atlassian.net/browse/CITRUS-178) | Groovy message header validation skipped | christophd |
| Fix | [#177](https://citrusframework.atlassian.net/browse/CITRUS-177) | ProcessContents strategy for xs:any elements in testcase.xsd | christophd |
| Update | [#176](https://citrusframework.atlassian.net/browse/CITRUS-176) | Improve message dispatching handler | christophd |
| Fix | [#175](https://citrusframework.atlassian.net/browse/CITRUS-175) | Empty control message disables validation | christophd |
| Fix | [#174](https://citrusframework.atlassian.net/browse/CITRUS-174) | JAXBHelperImpl using jdk internal NamespacePrefixMapper | christophd |
| Add | [#173](https://citrusframework.atlassian.net/browse/CITRUS-173) | MatchWeekday function | christophd |
| Add | [#172](https://citrusframework.atlassian.net/browse/CITRUS-172) | ChangeDate function | christophd |
| Fix | [#169](https://citrusframework.atlassian.net/browse/CITRUS-169) | FileUtils read to String encoding | christophd |
| Add | [#168](https://citrusframework.atlassian.net/browse/CITRUS-168) | Add XSD schema repository bean definition parser | christophd |
| Update | [#167](https://citrusframework.atlassian.net/browse/CITRUS-167) | Move message constructing interceptor to base interface | christophd |

## Release 1.3 / 2013-04-01
{: #v1-3}

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
| Add | [#165](https://citrusframework.atlassian.net/browse/CITRUS-165) | Add Http params to basic authentication | christophd |
| Add | [#164](https://citrusframework.atlassian.net/browse/CITRUS-164) | Support Jetty server with security handler | christophd |
| Add | [#163](https://citrusframework.atlassian.net/browse/CITRUS-163) | Support multiple SOAP fault detail elements | christophd |
| Fix | [#162](https://citrusframework.atlassian.net/browse/CITRUS-162) | Random number function leading zero numbers | christophd |
| Fix | [#161](https://citrusframework.atlassian.net/browse/CITRUS-161) | SOAP 1.2 fault detail validation | christophd |
| Fix | [#160](https://citrusframework.atlassian.net/browse/CITRUS-160) | CurrentDateFunction not threadsafe with custom date format pattern | christophd |
| Fix | [#159](https://citrusframework.atlassian.net/browse/CITRUS-159) | MessageSelectorBuilder breaks with 'A' 'N' 'D' characters | christophd |
| Add | [#158](https://citrusframework.atlassian.net/browse/CITRUS-158) | Http error code simulation | christophd |
| Add | [#157](https://citrusframework.atlassian.net/browse/CITRUS-157) | SSH adapter for mocking SSH requests roland
| Update | [#156](https://citrusframework.atlassian.net/browse/CITRUS-156) | Extend schema repository with pattern resolver | christophd |
| Add | [#155](https://citrusframework.atlassian.net/browse/CITRUS-155) | Add WSDL support for schema repository | christophd |
| Add | [#154](https://citrusframework.atlassian.net/browse/CITRUS-154) | Add SOAP fault actor support | christophd |
| Add | [#153](https://citrusframework.atlassian.net/browse/CITRUS-153) | Test actors | christophd |
| Fix | [#152](https://citrusframework.atlassian.net/browse/CITRUS-152) | JUnit test execution broken | christophd |
| Fix | [#151](https://citrusframework.atlassian.net/browse/CITRUS-151) | Resolve function throwing StringIndexOutOfBounds | christophd |
| Fix | [#150](https://citrusframework.atlassian.net/browse/CITRUS-150) | WebServiceEndpoint - NullPointerException when there´s no response | christophd |
| Fix | [#149](https://citrusframework.atlassian.net/browse/CITRUS-149) | XPath breaks message selector with "=" character | christophd |
| Fix | [#148](https://citrusframework.atlassian.net/browse/CITRUS-148) | Java action with String argument type | christophd |
| Fix | [#147](https://citrusframework.atlassian.net/browse/CITRUS-147) | Comma character breaks concat function | christophd |
| Update | [#146](https://citrusframework.atlassian.net/browse/CITRUS-146) | Support variables in file resource paths | christophd |
| Fix | [#145](https://citrusframework.atlassian.net/browse/CITRUS-145) | JSONArray as top level element | christophd |
| Fix | [#143](https://citrusframework.atlassian.net/browse/CITRUS-143) | SOAP fault detail schema validation | christophd |
| Add | [#142](https://citrusframework.atlassian.net/browse/CITRUS-142) | Add new test action for running ANT build targets | christophd |
| Add | [#91](https://citrusframework.atlassian.net/browse/CITRUS-91) | JUnit samples | christophd |
| Add | [#84](https://citrusframework.atlassian.net/browse/CITRUS-84) | JMS durable subscribers | christophd |
| Add | [#82](https://citrusframework.atlassian.net/browse/CITRUS-82) | Fork Http send action | christophd |
| Add | [#68](https://citrusframework.atlassian.net/browse/CITRUS-68) | Java DSL for writing test cases | christophd |

## Release 1.2 / 2012-07-05
{: #v1-2}

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
| Add | #368 | Add XML validation matcher | christophd |
| Add | #367 | Add JSON slurper Groovy validation support | danielp |
| Fix | #366 | Fixed issue with JSONArray and simple object values | christophd |
| Add | #365 | Message tracing log files | christophd |
| Add | #364 | Schema mappings on receive action definition | christophd |
| Add | #363 | Typed message headers | christophd |
| Add | #362 | Purge message channel action | christophd |
| Add | #361 | Schema mapping strategy chain | christophd |
| Add | #360 | Root QName schema mapping strategy | christophd |
| Add | #359 | Local host address function | christophd |
| Add | #358 | Function for digest authentication header creation | christophd |
| Add | #356 | Message selector support for message channels | christophd |
| Add | #357 | Root QName message selector on message channels | christophd |
| Fix | #355 | Fixed JBoss Maven repository | christophd |
| Update | #352 | Upgrade to Spring 3.1.1 | christophd |
| Update | #353 | Upgrade to Spring WS 2.1.0 | christophd |
| Update | #354 | Upgrade to Spring Integration 2.1.2 | christophd |
| Add | #348 | Custom validation matchers | cwied |
| Fix | #339 | Work on Sonar reported warnings | christophd |
| Fix | #362 | Do not use ParseException | christophd |
| Add | #331 | Custom actions tutorial | jza |
| Add | #346 | Validation matchers | cwied |
| Add | #400 | Validate REST Http error status codes | christophd |
| Add | #369 | Write blog entry on TestNG parameter support | christophd |
| Fix | #363 | Unknown test error reporting | christophd |
| Fix | #366 | Documentation bug on JmsConnectingMessageHandler package | christophd |
| Fix | #301 | Variable support in Java action | christophd |
| Fix | #367 | Setter on messaging template attribute in message-channel sender/receiver | christophd |
| Fix | #365 | @ property marker escaping in HTML reporter | christophd |
| Fix | #364 | JSON null value validation | christophd |
| Add | #312 | REST support (client and server side) | christophd |
| Add | #247 | Autowired tasks before/after suite | christophd |
| Add | #248 | TestNG parameter as variables | christophd |
| Add | #299 | Maven3 support | christophd |
| Add | #300 | Update Maven plugin versions | christophd |
| Fix | #361 | Fixed Http server connections from other machines | christophd |
| Add | #316 | Mime headers in SOAP client | christophd |
| Add | #303 | Fork mode for SOAP message sending | christophd |
| Add | #317 | Create variable from Groovy script | jblipphaus |
| Add | #314 | HTML test report | philkom |
| Add | #323 | Citrus Maven archetype | christophd |
| Fix | #350 | Variable support in templates | christophd |
| Add | #330 | Groovy SQL result set validation | christophd |
| Add | #347 | Custom imports in Groovy scripts | christophd |
| Add | #349 | Objects as test variables | christophd |
| Add | #344 | Plain text message validator | christophd |
| Add | #343 | JSON message validator | christophd |
| Fix | #345 | No XML specifica in TestContext | christophd |
| Fix | #324 | Multi-line SQL validation stmts | philkom |
| Fix | #322 | onFinish() of JUnitReporter called too late | christophd |
| Add | #342 | Global namespace mappings for XPath | christophd |
| Fix | #195 | Validate namespaces support is broken | christophd |
| Add | #336 | Support message channel name resolving | christophd |
| Add | #321 | Add custom connectors to Citrus Jetty server | christophd |
| Add | #338 | WsAddressing support in Soap message sender | christophd |
| Add | #308 | Maven plugin for test generation from WSDL and XSD | christophd |
| Add | #334 | Dynamic Http endpoint uri resolver | christophd |
| Add | #333 | Dynamic SOAP endpoint uri resolver | christophd |
| Fix | #332 | Handle errors in before suite/class annotated methods | christophd |
| Add | #325 | Log SOAP client errors properly | christophd |
| Fix | #328 | Handle and log errors during ApplicationContext setup | christophd |
| Add | #329 | Log SOAP messages in pure nature | christophd |
| Add | #320 | CDATA vs. any-element | philkom |
| Add | #139 | Dependency cleanup and version updates | christophd |
| Add | #315 | HTTP headers in SOAP message validation | christophd |
| Add | #241 | Validate multiple db rows | philkom |
| Add | #305 | Groovy XML message validation | philkom |
| Add | #306 | Groovy MarkupBuilder | philkom |
| Add | #304 | TestContext in Groovy action | philkom |
| Add | #272 | Variable support in property loader | christophd |

## Release 1.1 / 2010-08-12
{: #v1-1}

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
| Add | #313 | Log message when validation fails | maherma |
| Fix | #290 | Log XSD schema validation errors properly | maherma |
| Fix | #297 | SubstringAfterFunction | cwied |
| Add | #296 | Attachment support for webservice replies | cwied |
| Fix | #295 | Support for namespace uri containing "xmlns" phrase | christophd |
| Add | #287 | SOAP header XML support | christophd |
| Add | #246 | MessageChannelConnecting MessageHandler | christophd |
| Fix | #294 | Template parameter variable support | dimovelev |
| Add | #293 | Template parameter as CDATA | dimovelev |
| Add | #292 | MapValueFunction | dimovelev |
| Add | #291 | RandomEnumValueFunction | dimovelev |
| Add | #286 | Ignore placeholder | christophd |
| Fix | #289 | Fixed Spring wiring bug in AbstractTestNGCitrusTest in test set up methods | maherma |
| Add | #288 | Support setting variables in TestNG tests | maherma |
| Fix | #285 | Finally block not executed in failure state | christophd |
| Add | #284 | Provide line numbers in failure messages | christophd |
| Add | #283 | Improve exception tracing in parallel container | christophd |
| Fix | #191 | TestExecutionAspect not working | christophd |
| Fix | #268 | NPE in ws:receive without attachment data | christophd |
| Fix | #281 | XPath result type support (boolean, string, number, node) | christophd |
| Fix | #277 | XML namespace context support in XPath expressions | christophd |
| Add | #274 | Added SOAP fault support for sending SOAP responses | christophd |
| Fix | #273 | XML processing instruction in inline XML data | christophd |
| Fix | #271 | citrus-ant-tasks Unix file path translation | christophd |
| Fix | #270 | Automatic UTF-8 to UTF-16 conversion when sending messages | christophd |
| Fix | #269 | Receive timeout ignored when using message selector string | christophd |
| Fix | #244 | Purge Jms queues - adjust receive timeout | christophd |
| Fix | #243 | Premature EOF in Citrus WS endpoint | christophd |
| Fix | #226 | SOAP header to JMS header conversion using WebLogic JMS Server | christophd |
| Fix | #240 | Read database values to variables without validation | christophd |
| Fix | #237 | Variable replacement in file resource | christophd |
| Fix | #236 | Handle large SOAP attachments | christophd |
| Fix | #235 | Inline attachment data setter in ws:send | christophd |
| Fix | #222 | Template parameter in parallel container | christophd |
| Fix | #227 | Overwrite message validator instance in test case | christophd |
| Fix | #225 | Validate SOAP attachment with unknown content-id | christophd |
| Fix | #224 | Set SOAP attachment validator in test case | christophd |
| Add | #196 | Add documentation for auto-sleep in repeat-on-error-until-true | christophd |
| Add | #194 | Add documentation for Groovy support | christophd |
| Fix | #221 | JMS to SOAP header conversion | christophd |
| Update | #220 | Purge JMS queue destinations (JNDI support) | christophd |
| Update | #219 | Validating SOAP attachments | christophd |
| Update | #216 | Improved JMS Topic support in sender/receiver | christophd |
| Fix | #217 | Fixed sender/receiver configuration when using JMS topics | christophd |
| Update | #215 | Parallel container failing meaningful when handling single exception | christophd |
| Fix | #212 | JMSTemplate interference with default destination setting | christophd |
| Fix | #210 | Creating new test cases with ant | christophd |
| Fix | #211 | SOAP header mapping for WSEndpoint implementation | christophd |
| Fix | #205 | Receive timeout configuration not compliant with PropertyPlaceholderConfigurer | christophd |
| Fix | #207 | Sync reply destination holder not threadsafe | christophd |
| Fix | #206 | Sync reply message handler not threadsafe | christophd |
| Fix | #208 | JMS receiver ignoring timeout setting when adding JMS selector | christophd |
| Add | #203 | Sending SOAP attachments as a client | christophd |
| Add | #189 | SOAP Fault validation | christophd |
| Add | #187 | Extended exception validation | christophd |
| Add | #204 | Generate test documentation in Excel | christophd |
| Add | #202 | Extend test case meta-info with custom elements | christophd |
| Add | #201 | Write custom actions / extend test case with custom actions | christophd |
| Add | #213 | Interactive test creation in Maven plugin | christophd |
| Add | #214 | Interactive Excel doc generation in Maven plugin | christophd |
| Fix | #186 | XML validation - fixed assert error messages | christophd |
| Fix | #184 | Avoid lower case test names | christophd |
| Fix | #183 | Set targetPackage for test creation in Maven plugin | christophd |
| Fix | #185 | Parallel container not failing correctly | christophd |

## Release 1.0 / 2009-09-25
{: #v1-0}

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
| Update | #000 | Switch to Maven build system | christophd |
| Update | #000 | Code refactoring | christophd |
| Update | #000 | Documentation update | christophd |
| Update | #000 | Installation process | christophd |
| Add | #000 | Maven plugin for test case creation | christophd |
| Add | #000 | Citrus ANT tasks | christophd |
| Add | #000 | Custom Spring 2.x XML configuration schema | christophd |

## Release 0.0.0 / 2006-05-01
{: #v0-0-0}

- Internal usage @ConSol
- Birthday!
