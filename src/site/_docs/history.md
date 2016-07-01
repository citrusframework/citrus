---
title: History
layout: docs
permalink: "/docs/history/"
---

{% for release in site.data.releases %}
<h2 id="{{ release.tag }}">Release {{ release.version }} / {% if release.date != nil %}{{ release.date }}{% endif %}{% if release.date == nil %}{{ site.time | date: '%Y-%m-%d' }}{% endif %}</h2>

| Type | Id | Changes | By |
|:----:|:--:|:-------:|:--:|
{% for change in release.changes %}| {{ change.type }} | [#{{ change.id }}]({% if change.type == "Fix" %}{{ release.issues }}{% endif %}{% if change.type != "Fix" %}{{ release.stories }}{% endif %}{{ change.id }}) | {{ change.title }} | {{ change.author }} |
{% endfor %}

{% endfor %}

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
