/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.xml.migration;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.CreateVariablesAction;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.LoadPropertiesAction;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.actions.SleepAction;
import org.citrusframework.actions.StopTimeAction;
import org.citrusframework.actions.StopTimerAction;
import org.citrusframework.actions.TraceVariablesAction;
import org.citrusframework.actions.TransformAction;
import org.citrusframework.container.Assert;
import org.citrusframework.container.Async;
import org.citrusframework.container.Catch;
import org.citrusframework.container.Conditional;
import org.citrusframework.container.Iterate;
import org.citrusframework.container.Parallel;
import org.citrusframework.container.RepeatOnErrorUntilTrue;
import org.citrusframework.container.RepeatUntilTrue;
import org.citrusframework.container.Sequence;
import org.citrusframework.container.Template;
import org.citrusframework.container.Timer;
import org.citrusframework.container.Wait;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.xml.XmlTemplateLoader;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.xml.actions.AbstractXmlActionTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import static org.citrusframework.endpoint.direct.DirectEndpoints.direct;

public class SpringXmlTestMigrationTest extends AbstractXmlActionTest {

    private static final String SOURCE_PATH = "classpath:org/citrusframework/xml/migration/spring/";
    private static final String TEST_NS = "http://citrusframework.org/schema/xml/testcase";

    private final SpringXmlTestMigration migration = new SpringXmlTestMigration();

    @TempDir
    private Path tempDir;

    @Test
    public void shouldMigrateAndRunTest() throws Exception {
        String migrated = migrateTest("ActionsIT.xml");
        Assertions.assertEquals(Collections.emptyList(), SpringXmlTestMigration.getWarnings(migrated));
        Assertions.assertFalse(migrated.contains("http://www.citrusframework.org/schema/testcase"));
        Assertions.assertFalse(migrated.contains("http://www.springframework.org/schema/beans"));
        Assertions.assertTrue(migrated.contains("<!-- Trace - - some variables -->"));
        Assertions.assertTrue(migrated.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() + "<!--"));
        Assertions.assertTrue(migrated.contains("Licensed under the Apache License, Version 2.0"));
        Assertions.assertTrue(migrated.contains("<data><![CDATA[Hello <Citrus/>]]></data>"));
        Assertions.assertTrue(migrated.contains("<data>Hello from Citrus!</data>"));

        MessageQueue helloQueue = new DefaultMessageQueue("helloQueue");
        context.getReferenceResolver().bind("helloEndpoint", direct().asynchronous().queue(helloQueue).build());
        MessageQueue xmlQueue = new DefaultMessageQueue("xmlQueue");
        context.getReferenceResolver().bind("xmlEndpoint", direct().asynchronous().queue(xmlQueue).build());
        context.getMessageValidatorRegistry().addMessageValidator("textEqualsMessageValidator",
                new DefaultTextEqualsMessageValidator().enableTrim().normalizeLineEndings());

        TestCase result = load(migrated, "ActionsIT.xml");
        Assertions.assertEquals("ActionsIT", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals("Spring XML test using basic test actions", result.getDescription());
        Assertions.assertEquals(14L, result.getActionCount());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        int actionIndex = 0;
        EchoAction echo = (EchoAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("Print the text", echo.getDescription());
        Assertions.assertEquals("${text}", echo.getMessage());
        Assertions.assertEquals("print", result.getTestAction(actionIndex++).getName());

        SleepAction sleep = (SleepAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("0.1", sleep.getTime());
        Assertions.assertEquals("SECONDS", sleep.getTimeUnit().name());
        sleep = (SleepAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("50", sleep.getTime());
        Assertions.assertEquals("MILLISECONDS", sleep.getTimeUnit().name());
        sleep = (SleepAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("0.05", sleep.getTime());
        Assertions.assertEquals("SECONDS", sleep.getTimeUnit().name());

        Assertions.assertEquals(CreateVariablesAction.class, result.getTestAction(actionIndex++).getClass());
        TraceVariablesAction traceVariables = (TraceVariablesAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(List.of("name", "greeting"), traceVariables.getVariableNames());
        Assertions.assertEquals(StopTimeAction.class, result.getTestAction(actionIndex++).getClass());
        Assertions.assertEquals(LoadPropertiesAction.class, result.getTestAction(actionIndex++).getClass());
        Assertions.assertEquals(TransformAction.class, result.getTestAction(actionIndex++).getClass());

        SendMessageAction send = (SendMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("helloEndpoint", send.getEndpointUri());
        ReceiveMessageAction receive = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(5000L, receive.getReceiveTimeout());
        Assertions.assertEquals("xmlEndpoint", ((SendMessageAction) result.getTestAction(actionIndex++)).getEndpointUri());

        StopTimeAction stopTime = (StopTimeAction) result.getTestAction(actionIndex);
        Assertions.assertEquals("timer", stopTime.getId());
        Assertions.assertEquals("done", stopTime.getSuffix());

        Assertions.assertEquals("Hello <Citrus/>", context.getVariable("multiline"));
        Assertions.assertEquals("Hello Citrus!", context.getVariable("greeting"));
        Assertions.assertEquals("3", context.getVariable("sum"));
        Assertions.assertEquals("Citrus rocks!", context.getVariable("property.load.test"));
        Assertions.assertTrue(context.getVariable("transformed").contains("<p>Message: Hello World!</p>"));
        Assertions.assertEquals("sayHello", context.getVariable("sentOperation"));
        Assertions.assertEquals("5", context.getVariable("count"));
        Assertions.assertEquals("executed", context.getVariable("finally"));

        // Payload elements in the Spring XML test namespace do not have a namespace, just like with the Spring XML test parser
        Assertions.assertTrue(migrated.contains("<Message xmlns=\"\">Hello</Message>"));
        Document payload = parse(xmlQueue.receive().getPayload(String.class));
        Assertions.assertEquals("http://citrusframework.org/schemas/test", payload.getDocumentElement().getNamespaceURI());
        Assertions.assertEquals("TestRequest", payload.getDocumentElement().getLocalName());
        Element message = (Element) payload.getDocumentElement().getElementsByTagName("Message").item(0);
        Assertions.assertNull(message.getNamespaceURI());
        Assertions.assertEquals("Hello", message.getTextContent());
    }

    @Test
    public void shouldMigrateAndRunContainers() throws Exception {
        String migrated = migrateTest("ContainersIT.xml");
        Assertions.assertEquals(List.of("Template 'greet' defined in the Spring XML test source has been migrated to a separate template file - please make it available to the apply-template action"),
                SpringXmlTestMigration.getWarnings(migrated));

        String migratedTemplate = migration.migrateTemplate(readSource("ContainersIT.xml"), "greet");
        validate(migratedTemplate);
        Path templateFile = tempDir.resolve("ContainersIT-greet.xml");
        Files.writeString(templateFile, migratedTemplate, StandardCharsets.UTF_8);
        Template template = new XmlTemplateLoader()
                .withReferenceResolver(context.getReferenceResolver())
                .load("file:" + templateFile);
        Assertions.assertEquals("greet", template.getTemplateName());
        Assertions.assertTrue(template.isGlobalContext());
        context.getReferenceResolver().bind("greet", template);

        TestCase result = load(migrated, "ContainersIT.xml");
        Assertions.assertEquals("ContainersIT", result.getName());
        Assertions.assertEquals(TestCaseMetaInfo.Status.DRAFT, result.getMetaInfo().getStatus());
        Assertions.assertEquals(13L, result.getActionCount());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        int actionIndex = 0;
        Sequence sequential = (Sequence) result.getTestAction(actionIndex++);
        Assertions.assertEquals("Sequential actions", sequential.getDescription());
        Assertions.assertEquals(2L, sequential.getActionCount());
        Assertions.assertEquals(2L, ((Parallel) result.getTestAction(actionIndex++)).getActionCount());

        Iterate iterate = (Iterate) result.getTestAction(actionIndex++);
        Assertions.assertEquals("i lt= 6", iterate.getCondition());
        Assertions.assertEquals("i", iterate.getIndexName());
        Assertions.assertEquals(2, iterate.getStart());
        Assertions.assertEquals(2, iterate.getStep());

        RepeatUntilTrue repeat = (RepeatUntilTrue) result.getTestAction(actionIndex++);
        Assertions.assertEquals("r gt= 3", repeat.getCondition());
        Assertions.assertEquals("r", repeat.getIndexName());

        RepeatOnErrorUntilTrue repeatOnError = (RepeatOnErrorUntilTrue) result.getTestAction(actionIndex++);
        Assertions.assertEquals("e gt= 2", repeatOnError.getCondition());
        Assertions.assertEquals("e", repeatOnError.getIndexName());
        Assertions.assertEquals(10L, repeatOnError.getAutoSleep());

        Assertions.assertEquals("1 = 1", ((Conditional) result.getTestAction(actionIndex++)).getCondition());
        Assertions.assertEquals("org.citrusframework.exceptions.CitrusRuntimeException", ((Catch) result.getTestAction(actionIndex++)).getException());

        Assert assertAction = (Assert) result.getTestAction(actionIndex++);
        Assertions.assertEquals(CitrusRuntimeException.class, assertAction.getException());
        Assertions.assertEquals("Expected failure", assertAction.getMessage());

        Async async = (Async) result.getTestAction(actionIndex++);
        Assertions.assertEquals(1L, async.getActionCount());
        Assertions.assertEquals(1L, async.getSuccessActions().size());
        Assertions.assertEquals(1L, async.getErrorActions().size());

        Timer timer = (Timer) result.getTestAction(actionIndex++);
        Assertions.assertEquals("myTimer", timer.getTimerId());
        Assertions.assertEquals(50L, timer.getInterval());
        Assertions.assertEquals(10L, timer.getDelay());
        Assertions.assertEquals(2, timer.getRepeatCount());
        Assertions.assertEquals("myTimer", ((StopTimerAction) result.getTestAction(actionIndex++)).getTimerId());

        Wait wait = (Wait) result.getTestAction(actionIndex++);
        Assertions.assertEquals("1000", wait.getTime());
        Assertions.assertEquals("100", wait.getInterval());

        Template applyTemplate = (Template) result.getTestAction(actionIndex);
        Assertions.assertEquals("greet", applyTemplate.getTemplateName());
        Assertions.assertEquals(Map.of("greeting", "Hello", "name", "Citrus"), applyTemplate.getParameter());

        Assertions.assertEquals("6", context.getVariable("lastIndex"));
        Assertions.assertEquals("executed", context.getVariable("conditional"));
        Assertions.assertEquals("Hello Citrus", context.getVariable("templateResult"));
    }

    @Test
    public void shouldMigrateMessages() throws Exception {
        String migrated = migrateTest("MessagesIT.xml");
        Assertions.assertEquals(Collections.emptyList(), SpringXmlTestMigration.getWarnings(migrated));
        Document document = parse(migrated);

        String send = "/t:test/t:actions/t:send";
        assertXPath(document, "helloEndpoint", send + "/@endpoint");
        assertXPath(document, "client", send + "/@actor");
        assertXPath(document, "true", send + "/@fork");
        assertXPath(document, "Send hello request", send + "/t:description");
        assertXPath(document, "xml", send + "/t:message/@type");
        assertXPath(document, "myDictionary", send + "/t:message/@data-dictionary");
        assertXPath(document, "false", send + "/t:message/@schema-validation");
        assertXPath(document, "Hello", send + "/t:message/t:body/t:payload/h:HelloRequest/h:Text");
        assertXPath(document, "1", send + "/t:message/t:body/t:payload/h:HelloRequest/@c:id");
        assertXPath(document, "/hello:HelloRequest/hello:Text", send + "/t:message/t:expression/@path");
        assertXPath(document, "Hi", send + "/t:message/t:expression/@value");
        assertXPath(document, "4", "count(" + send + "/t:message/t:headers/t:header)");
        assertXPath(document, "<Header>header data</Header>", send + "/t:message/t:headers/t:header[1]/t:data");
        Assertions.assertTrue(migrated.contains("<data><![CDATA[<Header>header data</Header>]]></data>"));
        assertXPath(document, "classpath:header.xml", send + "/t:message/t:headers/t:header[2]/t:resource/@file");
        assertXPath(document, "UTF-8", send + "/t:message/t:headers/t:header[2]/t:resource/@charset");
        assertXPath(document, "sayHello", send + "/t:message/t:headers/t:header[3]/t:fragment/o:Operation");
        assertXPath(document, "operation", send + "/t:message/t:headers/t:header[4]/@name");
        assertXPath(document, "operation", send + "/t:extract/t:header/@variable");

        // Inline payloads only keep the namespace declarations that are required
        String payload = migrated.substring(migrated.indexOf("<payload>"), migrated.indexOf("</payload>"));
        Assertions.assertFalse(payload.contains("http://www.springframework.org/schema/beans"));
        Assertions.assertFalse(payload.contains("http://www.citrusframework.org/schema/testcase"));
        Assertions.assertFalse(payload.contains("http://www.w3.org/2001/XMLSchema-instance"));
        Assertions.assertTrue(payload.contains("xmlns:hello=\"http://citrusframework.org/schemas/hello\""));
        Assertions.assertTrue(payload.contains("xmlns:ctx=\"http://citrusframework.org/schemas/context\""));
        Assertions.assertTrue(migrated.contains("<Operation xmlns=\"http://citrusframework.org/header\">sayHello</Operation>"));

        String receive = "/t:test/t:actions/t:receive[1]";
        assertXPath(document, "1000", receive + "/@timeout");
        assertXPath(document, "server", receive + "/@actor");
        assertXPath(document, "myValidator", receive + "/@validator");
        assertXPath(document, "myHeaderValidator", receive + "/@header-validator");
        assertXPath(document, "operation", receive + "/t:selector/t:element/@name");
        assertXPath(document, "helloResponse", receive + "/t:message/@name");
        assertXPath(document, "classpath:hello-response.xml", receive + "/t:message/t:body/t:resource/@file");
        assertXPath(document, "/HelloResponse/Text", receive + "/t:message/t:expression/@path");
        assertXPath(document, "true", receive + "/t:message/t:headers/@ignore-case");
        assertXPath(document, "/HelloResponse/Id", receive + "/t:ignore/@path");
        assertXPath(document, "hello", receive + "/t:namespace/@prefix");
        assertXPath(document, "/HelloResponse/Text", receive + "/t:validate/@path");
        assertXPath(document, "string", receive + "/t:validate/@result-type");
        assertXPath(document, "classpath:validate.groovy", receive + "/t:validate/t:script/@file");
        assertXPath(document, "groovy", receive + "/t:validate/t:script/@type");
        assertXPath(document, "/HelloResponse/Id", receive + "/t:validate/t:xpath/@expression");
        assertXPath(document, "$.text", receive + "/t:validate/t:json-path/@expression");
        assertXPath(document, "ns", receive + "/t:validate/t:namespace/@prefix");
        assertXPath(document, "operation", receive + "/t:extract/t:header/@name");
        assertXPath(document, "/HelloResponse/Text", receive + "/t:extract/t:body/@path");
        assertXPath(document, "text", receive + "/t:extract/t:body/@variable");
        assertXPath(document, "string", receive + "/t:extract/t:body/@result-type");

        receive = "/t:test/t:actions/t:receive[2]";
        assertXPath(document, "operation = 'sayHello'", receive + "/t:selector/t:value");
        assertXPath(document, "json", receive + "/t:message/@type");
        assertXPath(document, "groovy", receive + "/t:message/t:body/t:builder/@type");
        assertXPath(document, "json.message { text 'Hello' }", "normalize-space(" + receive + "/t:message/t:body/t:builder)");

        assertXPath(document, "operation = 'sayHello'", "/t:test/t:actions/t:expect-timeout/@select");
        assertXPath(document, "500", "/t:test/t:actions/t:expect-timeout/@wait");
        assertXPath(document, " No message expected ", "/t:test/t:actions/comment()[1]");

        String purge = "/t:test/t:actions/t:purge-endpoint";
        assertXPath(document, "100", purge + "/@timeout");
        assertXPath(document, "10", purge + "/@sleep");
        assertXPath(document, "operation", purge + "/t:selector/t:element/@name");
        assertXPath(document, "helloEndpoint", purge + "/t:endpoint[1]/@name");
        assertXPath(document, "otherEndpoint", purge + "/t:endpoint[2]/@ref");
    }

    @Test
    public void shouldMigrateModuleActions() throws Exception {
        String migrated = migrateTest("nested/ModulesIT.xml");
        Assertions.assertEquals(Collections.emptyList(), SpringXmlTestMigration.getWarnings(migrated));
        Document document = parse(migrated);

        String sql = "/t:test/t:actions/t:sql[1]";
        assertXPath(document, "dataSource", sql + "/@datasource");
        assertXPath(document, "true", sql + "/@ignore-errors");
        assertXPath(document, "txManager", sql + "/t:transaction/@manager");
        assertXPath(document, "5000", sql + "/t:transaction/@timeout");
        assertXPath(document, "ISOLATION_READ_COMMITTED", sql + "/t:transaction/@isolation-level");
        assertXPath(document, "2", "count(" + sql + "/t:statements/t:statement)");
        assertXPath(document, "DELETE FROM ORDERS", sql + "/t:statements/t:statement[1]");

        sql = "/t:test/t:actions/t:sql[2]";
        assertXPath(document, "classpath:query.sql", sql + "/t:statements/@file");
        assertXPath(document, "1", sql + "/t:validate[1]/@value");
        assertXPath(document, "DESCRIPTION", sql + "/t:validate[2]/@column");
        assertXPath(document, "@ignore@", sql + "/t:validate[2]/t:values/t:value[2]");
        assertXPath(document, "orderId", sql + "/t:extract/@variable");

        sql = "/t:test/t:actions/t:sql[3]";
        assertXPath(document, "groovy", sql + "/t:validate/t:script/@type");
        assertXPath(document, "assert rows.size() == 1", sql + "/t:validate/t:script");

        assertXPath(document, "BEGIN NULL; END;", "/t:test/t:actions/t:plsql[1]/t:statements/t:script");
        assertXPath(document, "false", "/t:test/t:actions/t:plsql[1]/@ignore-errors");
        assertXPath(document, "classpath:script.sql", "/t:test/t:actions/t:plsql[2]/t:statements/@file");

        assertXPath(document, "println 'Hello Citrus'", "/t:test/t:actions/t:groovy[1]/t:script");
        assertXPath(document, "classpath:script.groovy", "/t:test/t:actions/t:groovy[2]/t:script/@file");
        assertXPath(document, "false", "/t:test/t:actions/t:groovy[2]/t:script/@use-script-template");

        assertXPath(document, "connectionFactory", "/t:test/t:actions/t:purge-jms-queues/@connection-factory");
        assertXPath(document, "JMS.Queue", "/t:test/t:actions/t:purge-jms-queues/t:queue[1]/@name");
        assertXPath(document, "jmsQueue", "/t:test/t:actions/t:purge-jms-queues/t:queue[2]/@ref");
        assertXPath(document, "mySelector", "/t:test/t:actions/t:purge-channels/@message-selector");
        assertXPath(document, "myChannel", "/t:test/t:actions/t:purge-channels/t:channel/@name");

        assertXPath(document, "classpath:build.xml", "/t:test/t:actions/t:ant/@build-file");
        assertXPath(document, "myListener", "/t:test/t:actions/t:ant/@build-listener");
        assertXPath(document, "sayHello", "/t:test/t:actions/t:ant/t:execute/@target");
        assertXPath(document, "classpath:build.properties", "/t:test/t:actions/t:ant/t:properties/@file");
        assertXPath(document, "welcomeText", "/t:test/t:actions/t:ant/t:properties/t:property/@name");

        assertXPath(document, "myServer", "/t:test/t:actions/t:start/@server");
        assertXPath(document, "2", "count(/t:test/t:actions/t:stop/t:servers/t:server)");
        assertXPath(document, " Stop servers ", "/t:test/t:actions/t:stop/preceding-sibling::comment()[1]");
        assertXPath(document, "myAction", "/t:test/t:actions/t:action/@reference");

        assertXPath(document, "2000", "/t:test/t:actions/t:wait-for[1]/@timeout");
        assertXPath(document, "http://localhost:8080/health", "/t:test/t:actions/t:wait-for[1]/t:http/@url");
        assertXPath(document, "500", "/t:test/t:actions/t:wait-for[1]/t:http/@timeout");
        assertXPath(document, "1500", "/t:test/t:actions/t:wait-for[2]/@timeout");
        assertXPath(document, "classpath:test.txt", "/t:test/t:actions/t:wait-for[2]/t:file/@path");
        assertXPath(document, "helloRequest", "/t:test/t:actions/t:wait-for[3]/t:message/@name");
    }

    @Test
    public void shouldMigrateConnectorActions() throws Exception {
        String migrated = migrateTest("ConnectorsIT.xml");
        Assertions.assertEquals(Collections.emptyList(), SpringXmlTestMigration.getWarnings(migrated));
        Assertions.assertFalse(migrated.contains("http://www.citrusframework.org/schema/docker/testcase"));
        Assertions.assertFalse(migrated.contains("http://www.citrusframework.org/schema/camel/testcase"));
        Assertions.assertFalse(migrated.contains("http://www.citrusframework.org/schema/selenium/testcase"));
        Assertions.assertFalse(migrated.contains("http://www.citrusframework.org/schema/zookeeper/testcase"));
        Document document = parse(migrated);

        String actions = "/t:test/t:actions/";
        assertXPath(document, "1", "count(" + actions + "t:docker[1]/t:ping)");
        assertXPath(document, "myDockerClient", actions + "t:docker[2]/@client");
        assertXPath(document, "Check version", actions + "t:docker[2]/t:description");
        assertXPath(document, "{\"Version\":\"@ignore@\"}", actions + "t:docker[2]/@result");
        assertXPath(document, "1", "count(" + actions + "t:docker[2]/t:version)");
        assertXPath(document, "Dockerfile", actions + "t:docker[3]/t:build-image/@docker-file");
        assertXPath(document, "true", actions + "t:docker[3]/t:build-image/@no-cache");
        assertXPath(document, "library", actions + "t:docker[4]/t:pull-image/@repository");
        assertXPath(document, "top", actions + "t:docker[5]/t:create/@cmd");
        assertXPath(document, "myContainer", actions + "t:docker[6]/t:start/@container");
        assertXPath(document, "myContainer", actions + "t:docker[7]/t:inspect/@container");
        assertXPath(document, "busybox", actions + "t:docker[8]/t:inspect-image/@image");
        assertXPath(document, "myContainer", actions + "t:docker[9]/t:stop/@container");
        assertXPath(document, "true", actions + "t:docker[10]/t:remove/@force");
        assertXPath(document, "busybox", actions + "t:docker[11]/t:remove-image/@image");

        assertXPath(document, "camelContext", actions + "t:camel[1]/@camel-context");
        assertXPath(document, "Create routes", actions + "t:camel[1]/t:description");
        assertXPath(document, "myRoute", actions + "t:camel[1]/t:create-routes/*[local-name() = 'routeContext']/*[local-name() = 'route']/@id");
        assertXPath(document, "2", "count(" + actions + "t:camel[2]/t:start-routes/t:route)");
        assertXPath(document, "route_1", actions + "t:camel[3]/t:stop-routes/t:route/@id");
        assertXPath(document, "route_1", actions + "t:camel[4]/t:remove-routes/t:route/@id");
        assertXPath(document, "status", actions + "t:camel[5]/t:control-bus/t:route/@action");
        assertXPath(document, "Stopped", actions + "t:camel[5]/t:control-bus/t:result");
        assertXPath(document, "simple", actions + "t:camel[6]/t:control-bus/t:language/@type");
        assertXPath(document, "${camelContext.stop()}", actions + "t:camel[6]/t:control-bus/t:language");

        assertXPath(document, "webBrowser", actions + "t:selenium[1]/@browser");
        assertXPath(document, "1", "count(" + actions + "t:selenium[1]/t:start)");
        assertXPath(document, "http://localhost:19090", actions + "t:selenium[2]/t:navigate/@page");
        assertXPath(document, "Find heading", actions + "t:selenium[3]/t:description");
        assertXPath(document, "h1", actions + "t:selenium[3]/t:find/t:element/@tag-name");
        assertXPath(document, "Welcome!", actions + "t:selenium[3]/t:find/t:validate/@text");
        assertXPath(document, "h1", actions + "t:selenium[3]/t:find/t:validate/@tag-name");
        assertXPath(document, "true", actions + "t:selenium[3]/t:find/t:validate/@displayed");
        assertXPath(document, "heading", actions + "t:selenium[3]/t:find/t:validate/t:attributes/t:attribute/@value");
        assertXPath(document, "40px", actions + "t:selenium[3]/t:find/t:validate/t:styles/t:style/@value");
        assertXPath(document, "open-alert", actions + "t:selenium[4]/t:click/t:element/@id");
        assertXPath(document, "Say Hello!", actions + "t:selenium[5]/t:hover/t:element/@link-text");
        assertXPath(document, "Hello", actions + "t:selenium[6]/t:alert/t:text");
        assertXPath(document, "Warning", actions + "t:selenium[7]/t:alert/t:text");
        assertXPath(document, "false", actions + "t:selenium[7]/t:alert/@accept");
        assertXPath(document, "${username}", actions + "t:selenium[8]/t:page/t:arguments/t:argument");
        assertXPath(document, "Citrus", actions + "t:selenium[9]/t:set-input/@value");
        assertXPath(document, "//input[@type='checkbox']", actions + "t:selenium[10]/t:check-input/t:element/@xpath");
        assertXPath(document, "adult", actions + "t:selenium[11]/t:dropdown-select/@option");
        assertXPath(document, "Dr.", actions + "t:selenium[12]/t:dropdown-select/t:options/t:option[2]");
        assertXPath(document, "hidden", actions + "t:selenium[13]/t:wait/@until");
        assertXPath(document, "alert('Hello')", actions + "t:selenium[14]/t:javascript/t:script");
        assertXPath(document, "Something went wrong", actions + "t:selenium[14]/t:javascript/t:errors/t:error");
        assertXPath(document, "otherBrowser", actions + "t:selenium[15]/@browser");
        assertXPath(document, "target", actions + "t:selenium[15]/t:screenshot/@output-dir");
        assertXPath(document, "newWindow", actions + "t:selenium[16]/t:open-window/@name");
        assertXPath(document, "newWindow", actions + "t:selenium[17]/t:switch-window/@name");
        assertXPath(document, "newWindow", actions + "t:selenium[18]/t:close-window/@name");
        assertXPath(document, "classpath:download/file.txt", actions + "t:selenium[19]/t:store-file/@file-path");
        assertXPath(document, "file.txt", actions + "t:selenium[20]/t:get-stored-file/@file-name");
        assertXPath(document, "1", "count(" + actions + "t:selenium[21]/t:clear-cache)");
        assertXPath(document, "1", "count(" + actions + "t:selenium[22]/t:stop)");

        assertXPath(document, "zookeeperClient", actions + "t:zookeeper[1]/@client");
        assertXPath(document, "{\"responseData\":{\"state\":\"@ignore@\"}}", actions + "t:zookeeper[1]/@result");
        assertXPath(document, "1", "count(" + actions + "t:zookeeper[1]/t:info)");
        assertXPath(document, "$.responseData.state", actions + "t:zookeeper[1]/t:validate/@path");
        assertXPath(document, "CONNECTED", actions + "t:zookeeper[1]/t:validate/@value");
        assertXPath(document, "$.responseData.sessionId", actions + "t:zookeeper[1]/t:extract/@path");
        assertXPath(document, "sessionId", actions + "t:zookeeper[1]/t:extract/@variable");
        assertXPath(document, "/node", actions + "t:zookeeper[2]/t:create/@path");
        assertXPath(document, "Some Data", actions + "t:zookeeper[2]/t:create/@data");
        assertXPath(document, "OPEN_ACL_UNSAFE", actions + "t:zookeeper[2]/t:create/@acl");
        assertXPath(document, "PERSISTENT", actions + "t:zookeeper[2]/t:create/@mode");
        assertXPath(document, "/node", actions + "t:zookeeper[3]/t:exists/@path");
        assertXPath(document, "/node", actions + "t:zookeeper[4]/t:get/@path");
        assertXPath(document, "New Data", actions + "t:zookeeper[5]/t:set/@data");
        assertXPath(document, "1", "count(" + actions + "t:zookeeper[6]/t:children)");
        assertXPath(document, "/node", actions + "t:zookeeper[7]/t:delete/@path");
        assertXPath(document, "1", actions + "t:zookeeper[7]/t:delete/@version");
    }

    @Test
    public void shouldMigrateEndpointActions() throws Exception {
        String migrated = migrateTest("EndpointsIT.xml");
        Assertions.assertFalse(migrated.contains("http://www.citrusframework.org/schema/kubernetes/testcase"));
        Assertions.assertFalse(migrated.contains("http://www.citrusframework.org/schema/http/testcase"));
        Assertions.assertFalse(migrated.contains("http://www.citrusframework.org/schema/ws/testcase"));
        Assertions.assertFalse(migrated.contains("http://www.citrusframework.org/schema/jms/testcase"));

        Assertions.assertEquals(List.of(
                "The 'uri' attribute of the Http response is not supported in the plain XML DSL and has been removed",
                "The result validation of 'k8s:delete-service' is not supported by the delete action in the plain XML DSL and has been removed"
        ), SpringXmlTestMigration.getWarnings(migrated));
        Document document = parse(migrated);

        String actions = "/t:test/t:actions/";
        assertXPath(document, "k8sClient", actions + "t:kubernetes[1]/@client");
        assertXPath(document, "Kubernetes info", actions + "t:kubernetes[1]/t:description");
        assertXPath(document, "{\"result\": {\"kind\": \"Info\"}}", actions + "t:kubernetes[1]/t:execute/t:info/@result");
        assertXPath(document, "false", actions + "t:kubernetes[2]/@auto-remove");
        assertXPath(document, "test", actions + "t:kubernetes[2]/@namespace");
        assertXPath(document, "classpath:templates/pod.yml", actions + "t:kubernetes[2]/t:create-resource/@file");
        assertXPath(document, "app=todo", actions + "t:kubernetes[3]/t:execute/t:list-pods/@label");
        assertXPath(document, "test", actions + "t:kubernetes[3]/t:execute/t:list-pods/@namespace");
        assertXPath(document, "2", "count(" + actions + "t:kubernetes[3]/t:execute/t:list-pods/t:validate)");
        assertXPath(document, "Running", actions + "t:kubernetes[3]/t:execute/t:list-pods/t:validate[2]/@value");
        assertXPath(document, "my-pod", actions + "t:kubernetes[4]/t:execute/t:get-pod/@name");
        assertXPath(document, "classpath:templates/service.yml", actions + "t:kubernetes[5]/t:create-resource/@file");
        assertXPath(document, "app=todo", actions + "t:kubernetes[6]/t:execute/t:list-services/@label");
        assertXPath(document, "my-service", actions + "t:kubernetes[7]/t:execute/t:get-service/@name");
        assertXPath(document, "1", "count(" + actions + "t:kubernetes[8]/t:execute/t:list-endpoints)");
        assertXPath(document, "1", "count(" + actions + "t:kubernetes[9]/t:execute/t:list-events)");
        assertXPath(document, "app=todo", actions + "t:kubernetes[10]/t:delete-pod/@label");
        assertXPath(document, "my-service", actions + "t:kubernetes[11]/t:delete-service/@name");

        assertXPath(document, "httpClient", actions + "t:http[1]/@client");
        assertXPath(document, "http://localhost:8080/test", actions + "t:http[1]/t:send-request/@uri");
        assertXPath(document, "true", actions + "t:http[1]/t:send-request/@fork");
        assertXPath(document, "/order", actions + "t:http[1]/t:send-request/t:POST/@path");
        assertXPath(document, "application/xml", actions + "t:http[1]/t:send-request/t:POST/@content-type");
        assertXPath(document, "application/xml", actions + "t:http[1]/t:send-request/t:POST/@accept");
        assertXPath(document, "HTTP/1.1", actions + "t:http[1]/t:send-request/t:POST/@version");
        assertXPath(document, "order", actions + "t:http[1]/t:send-request/t:POST/@name");
        assertXPath(document, "gold", actions + "t:http[1]/t:send-request/t:POST/t:param[@name='type']/@value");
        assertXPath(document, "1001", actions + "t:http[1]/t:send-request/t:POST/t:headers/t:header[@name='userId']/@value");
        assertXPath(document, "<order><id>${id}</id></order>", actions + "t:http[1]/t:send-request/t:POST/t:body/t:data");
        assertXPath(document, "/order/id", actions + "t:http[1]/t:send-request/t:POST/t:expression/@path");
        assertXPath(document, "location", actions + "t:http[1]/t:send-request/t:extract/t:header/@variable");
        assertXPath(document, "1", "count(" + actions + "t:http[2]/t:send-request/t:GET)");

        assertXPath(document, "2000", actions + "t:http[3]/t:receive-response/@timeout");
        assertXPath(document, "Receive order", actions + "t:http[3]/t:description");
        assertXPath(document, "myValidator", actions + "t:http[3]/t:receive-response/@validator");
        assertXPath(document, "404", actions + "t:http[4]/t:receive-response/t:response/@status");
        assertXPath(document, "OK", actions + "t:http[3]/t:receive-response/t:response/@reason-phrase");
        assertXPath(document, "HTTP/1.1", actions + "t:http[3]/t:receive-response/t:response/@version");
        assertXPath(document, "false", actions + "t:http[3]/t:receive-response/t:response/t:headers/@ignore-case");
        assertXPath(document, "${id}", actions + "t:http[3]/t:receive-response/t:response/t:body/t:payload/*[local-name() = 'order']/*[local-name() = 'id']");
        assertXPath(document, "1001", actions + "t:http[3]/t:receive-response/t:validate[1]/@value");
        assertXPath(document, "$.id", actions + "t:http[3]/t:receive-response/t:validate[2]/t:json-path/@expression");
        assertXPath(document, "string", actions + "t:http[3]/t:receive-response/t:validate[2]/t:xpath/@result-type");
        assertXPath(document, "o", actions + "t:http[3]/t:receive-response/t:validate[2]/t:namespace/@prefix");
        assertXPath(document, "orderId", actions + "t:http[3]/t:receive-response/t:extract/t:body/@variable");

        assertXPath(document, "soapClient", actions + "t:soap[1]/@endpoint");
        assertXPath(document, "Send SOAP request", actions + "t:soap[1]/t:description");
        assertXPath(document, "true", actions + "t:soap[1]/t:send-request/@fork");
        assertXPath(document, "sayHello", actions + "t:soap[1]/t:send-request/t:message/@soap-action");
        assertXPath(document, "false", actions + "t:soap[1]/t:send-request/t:message/@mtom-enabled");
        assertXPath(document, "text/xml", actions + "t:soap[1]/t:send-request/t:message/@content-type");
        assertXPath(document, "sayHello", actions + "t:soap[1]/t:send-request/t:message/t:headers/t:header/@value");
        assertXPath(document, "UTF-8", actions + "t:soap[1]/t:send-request/t:message/t:attachment[1]/@charset");
        assertXPath(document, "Attachment content", actions + "t:soap[1]/t:send-request/t:message/t:attachment[1]/t:content");
        assertXPath(document, "classpath:attachment.xml", actions + "t:soap[1]/t:send-request/t:message/t:attachment[2]/t:resource/@file");
        assertXPath(document, "op", actions + "t:soap[1]/t:send-request/t:extract/t:header/@variable");
        assertXPath(document, "soapClient", actions + "t:soap[2]/@endpoint");
        assertXPath(document, "3000", actions + "t:soap[2]/t:receive-response/@timeout");
        assertXPath(document, "soapValidator", actions + "t:soap[2]/t:receive-response/@validator");
        assertXPath(document, "attachmentValidator", actions + "t:soap[2]/t:receive-response/@attachment-validator");
        assertXPath(document, "Hello", actions + "t:soap[2]/t:receive-response/t:message/t:body/t:payload/*[local-name() = 'HelloResponse']");
        assertXPath(document, "Hello", actions + "t:soap[2]/t:receive-response/t:validate/@value");

        assertXPath(document, "helloSoapClient", actions + "t:soap[3]/@client");
        assertXPath(document, "{http://citrusframework.org/faults}TEC-1000", actions + "t:soap[3]/t:assert-fault/@fault-code");
        assertXPath(document, "Invalid request", actions + "t:soap[3]/t:assert-fault/@fault-string");
        assertXPath(document, "SERVER", actions + "t:soap[3]/t:assert-fault/@fault-actor");
        assertXPath(document, "xmlSoapFaultValidator", actions + "t:soap[3]/t:assert-fault/@validator");
        assertXPath(document, "true", "contains(" + actions + "t:soap[3]/t:assert-fault/t:fault-detail[1]/t:content, 'TEC-1000')");
        assertXPath(document, "classpath:fault-detail.xml", actions + "t:soap[3]/t:assert-fault/t:fault-detail[2]/t:resource/@file");
        assertXPath(document, "1", "count(" + actions + "t:soap[3]/t:assert-fault/t:when/t:send)");

        assertXPath(document, "soapResponseEndpoint", actions + "t:soap[4]/@server");
        assertXPath(document, "{http://citrusframework.org/faults}citrus-ns:TEC-1000", actions + "t:soap[4]/t:send-fault/t:message/@fault-code");
        assertXPath(document, "Invalid request", actions + "t:soap[4]/t:send-fault/t:message/@fault-string");
        assertXPath(document, "SERVER", actions + "t:soap[4]/t:send-fault/t:message/@fault-actor");
        assertXPath(document, "true", "contains(" + actions + "t:soap[4]/t:send-fault/t:message/t:fault-detail/t:content, 'TEC-1000')");
        assertXPath(document, "faultResponse", actions + "t:soap[4]/t:send-fault/t:message/t:headers/t:header/@value");

        assertXPath(document, "k8sClient", actions + "t:kubernetes[12]/@client");
        assertXPath(document, "my-service", actions + "t:kubernetes[12]/t:delete-service/@name");
        assertXPath(document, "0", "count(" + actions + "t:kubernetes[12]//t:validate)");

        assertXPath(document, "jmsConnectionFactory", actions + "t:purge-jms-queues/@connection-factory");
        assertXPath(document, "500", actions + "t:purge-jms-queues/@timeout");
        assertXPath(document, "Purge queues", actions + "t:purge-jms-queues/t:description");
        assertXPath(document, "jms.queue.one", actions + "t:purge-jms-queues/t:queue[1]/@name");
        assertXPath(document, "queueTwo", actions + "t:purge-jms-queues/t:queue[2]/@ref");
    }

    /**
     * The XML test schema does not define the cookies and the context path of Http messages, so this test does not validate the result.
     */
    @Test
    public void shouldMigrateHttpCookies() throws Exception {
        String migrated = migration.migrateTest(springTest("""
                <testcase name="HttpCookiesIT">
                  <actions xmlns:http="http://www.citrusframework.org/schema/http/testcase">
                    <http:send-request client="httpClient">
                      <http:POST path="/user" context-path="/test">
                        <http:headers>
                          <http:header name="userId" value="1001"/>
                          <http:cookie name="session" value="abc123" path="/user" max-age="3600" secure="true" http-only="true"/>
                        </http:headers>
                      </http:POST>
                    </http:send-request>
                    <http:receive-response client="httpClient">
                      <http:headers status="200">
                        <http:cookie name="session" value="abc123"/>
                      </http:headers>
                    </http:receive-response>
                  </actions>
                </testcase>
                """));
        Assertions.assertEquals(Collections.emptyList(), SpringXmlTestMigration.getWarnings(migrated));
        Document document = parse(migrated);

        String actions = "/t:test/t:actions/";
        assertXPath(document, "/test", actions + "t:http[1]/t:send-request/t:POST/@context-path");
        assertXPath(document, "abc123", actions + "t:http[1]/t:send-request/t:POST/t:cookie/@value");
        assertXPath(document, "3600", actions + "t:http[1]/t:send-request/t:POST/t:cookie/@max-age");
        assertXPath(document, "true", actions + "t:http[1]/t:send-request/t:POST/t:cookie/@http-only");
        assertXPath(document, "abc123", actions + "t:http[2]/t:receive-response/t:response/t:cookie/@value");
    }

    @Test
    public void shouldMigrateHttpServerActions() throws Exception {
        String migrated = migration.migrateTest(springTest("""
                <testcase name="HttpServerIT">
                  <actions xmlns:http="http://www.citrusframework.org/schema/http/testcase">
                    <http:receive-request server="httpServer" timeout="2000">
                      <http:POST path="/order">
                        <http:param name="type" value="gold"/>
                        <http:headers content-type="application/xml" accept="application/xml">
                          <http:header name="userId" value="1001"/>
                        </http:headers>
                        <http:body validator="myValidator">
                          <http:data>&lt;order&gt;&lt;id&gt;1001&lt;/id&gt;&lt;/order&gt;</http:data>
                          <http:validate path="/order/id" value="1001"/>
                        </http:body>
                      </http:POST>
                      <http:extract>
                        <http:header name="Host" variable="host"/>
                      </http:extract>
                    </http:receive-request>
                    <http:send-response server="httpServer">
                      <http:headers status="201" reason-phrase="Created">
                        <http:header name="Location" value="/order/1001"/>
                      </http:headers>
                      <http:body>
                        <http:data>{"id": 1001}</http:data>
                      </http:body>
                    </http:send-response>
                  </actions>
                </testcase>
                """));
        Assertions.assertEquals(Collections.emptyList(), SpringXmlTestMigration.getWarnings(migrated));
        validate(migrated);
        Document document = parse(migrated);

        String actions = "/t:test/t:actions/";
        assertXPath(document, "httpServer", actions + "t:http[1]/@server");
        assertXPath(document, "2000", actions + "t:http[1]/t:receive-request/@timeout");
        assertXPath(document, "myValidator", actions + "t:http[1]/t:receive-request/@validator");
        assertXPath(document, "/order", actions + "t:http[1]/t:receive-request/t:POST/@path");
        assertXPath(document, "application/xml", actions + "t:http[1]/t:receive-request/t:POST/@content-type");
        assertXPath(document, "gold", actions + "t:http[1]/t:receive-request/t:POST/t:param/@value");
        assertXPath(document, "1001", actions + "t:http[1]/t:receive-request/t:POST/t:headers/t:header/@value");
        assertXPath(document, "<order><id>1001</id></order>", actions + "t:http[1]/t:receive-request/t:POST/t:body/t:data");
        assertXPath(document, "1001", actions + "t:http[1]/t:receive-request/t:validate/@value");
        assertXPath(document, "host", actions + "t:http[1]/t:receive-request/t:extract/t:header/@variable");

        assertXPath(document, "httpServer", actions + "t:http[2]/@server");
        assertXPath(document, "201", actions + "t:http[2]/t:send-response/t:response/@status");
        assertXPath(document, "Created", actions + "t:http[2]/t:send-response/t:response/@reason-phrase");
        assertXPath(document, "/order/1001", actions + "t:http[2]/t:send-response/t:response/t:headers/t:header/@value");
        assertXPath(document, "{\"id\": 1001}", actions + "t:http[2]/t:send-response/t:response/t:body/t:data");
    }

    @Test
    public void shouldMarkUnsupportedElements() throws Exception {
        String migrated = migrateTest("UnsupportedIT.xml");

        List<String> warnings = SpringXmlTestMigration.getWarnings(migrated);
        Assertions.assertEquals(3, warnings.size());
        Assertions.assertEquals("Unsupported test action 'java' (http://www.citrusframework.org/schema/testcase) has been copied as is - please migrate it manually", warnings.get(0));
        Assertions.assertEquals("Unsupported test action 'input' (http://www.citrusframework.org/schema/testcase) has been copied as is - please migrate it manually", warnings.get(1));
        Assertions.assertEquals("Unable to convert wait timeout of '${waitTime}' seconds - please set the timeout in milliseconds manually", warnings.get(2));

        Document document = parse(migrated);
        assertXPath(document, "sayHello", "/t:test/t:actions/s:java/s:method/@name");
        assertXPath(document, "answer", "/t:test/t:actions/s:input/@variable");
        assertXPath(document, "zookeeperClient", "/t:test/t:actions/t:zookeeper/@client");
        assertXPath(document, "1", "count(/t:test/t:actions/t:zookeeper/t:info)");
        assertXPath(document, "i lt 3", "/t:test/t:actions/t:iterate/@condition");
        assertXPath(document, "PT10S", "/t:test/t:actions/t:iterate/@timeout");
        assertXPath(document, "false", "boolean(/t:test/t:actions/t:wait-for/@timeout)");
    }

    @Test
    public void shouldMigrateDirectory() throws Exception {
        Path sourceDirectory = Path.of(Objects.requireNonNull(getClass().getResource("/org/citrusframework/xml/migration/spring")).toURI());
        Path targetDirectory = tempDir.resolve("migrated");

        SpringXmlTestMigration.MigrationResult result = migration.migrate(sourceDirectory, targetDirectory);

        Assertions.assertEquals(List.of(sourceDirectory.resolve("nested/endpoint-context.xml")), result.skipped());
        Assertions.assertEquals(Collections.emptyList(), result.failed());
        Assertions.assertEquals(List.of(
                        targetDirectory.resolve("ActionsIT.xml"),
                        targetDirectory.resolve("ConnectorsIT.xml"),
                        targetDirectory.resolve("ContainersIT.xml"),
                        targetDirectory.resolve("ContainersIT-greet.xml"),
                        targetDirectory.resolve("EndpointsIT.xml"),
                        targetDirectory.resolve("MessagesIT.xml"),
                        targetDirectory.resolve("UnsupportedIT.xml"),
                        targetDirectory.resolve("nested/ModulesIT.xml"),
                        targetDirectory.resolve("nested/templates-sayHello.xml"),
                        targetDirectory.resolve("nested/templates-say_goodbye.xml")),
                result.migrated().stream().map(SpringXmlTestMigration.MigratedFile::target).toList());

        for (SpringXmlTestMigration.MigratedFile migrated : result.migrated()) {
            String content = Files.readString(migrated.target(), StandardCharsets.UTF_8);
            validate(content);
            Assertions.assertEquals(SpringXmlTestMigration.getWarnings(content), migrated.warnings());
        }

        Assertions.assertEquals(3, result.migrated().get(6).warnings().size());

        Template template = new XmlTemplateLoader()
                .withReferenceResolver(context.getReferenceResolver())
                .load("file:" + targetDirectory.resolve("nested/templates-say_goodbye.xml"));
        Assertions.assertEquals("say goodbye", template.getTemplateName());
        Assertions.assertFalse(template.isGlobalContext());
        Assertions.assertEquals(1L, template.getActions().size());

        template = new XmlTemplateLoader()
                .withReferenceResolver(context.getReferenceResolver())
                .load("file:" + targetDirectory.resolve("nested/templates-sayHello.xml"));
        Assertions.assertEquals("sayHello", template.getTemplateName());
        Assertions.assertEquals("Says hello", template.getDescription());
        Assertions.assertTrue(template.isGlobalContext());
    }

    @Test
    public void shouldMigrateSingleFile() throws Exception {
        Path sourceFile = tempDir.resolve("source/EchoIT.xml");
        Files.createDirectories(sourceFile.getParent());
        Files.writeString(sourceFile, "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
                springTest("<testcase name=\"EchoIT\"><actions><echo><message>Gr\u00fc\u00dfe</message></echo></actions></testcase>"),
                StandardCharsets.ISO_8859_1);

        SpringXmlTestMigration.MigrationResult result = migration.migrate(sourceFile, tempDir.resolve("target"));

        Assertions.assertEquals(1, result.migrated().size());
        Assertions.assertEquals(tempDir.resolve("target/EchoIT.xml"), result.migrated().get(0).target());
        Document document = parse(Files.readString(tempDir.resolve("target/EchoIT.xml"), StandardCharsets.UTF_8));
        assertXPath(document, "EchoIT", "/t:test/@name");
        assertXPath(document, "Gr\u00fc\u00dfe", "/t:test/t:actions/t:echo/t:message");
    }

    @Test
    public void shouldReportFailedFiles() throws Exception {
        Path sourceDirectory = tempDir.resolve("source");
        Files.createDirectories(sourceDirectory);
        Files.writeString(sourceDirectory.resolve("MultipleIT.xml"), springTest("<testcase name=\"FirstIT\"><actions/></testcase><testcase name=\"SecondIT\"><actions/></testcase>"));
        Files.writeString(sourceDirectory.resolve("InvalidIT.xml"), "<spring:beans");

        SpringXmlTestMigration.MigrationResult result = migration.migrate(sourceDirectory, tempDir.resolve("target"));

        Assertions.assertEquals(0, result.migrated().size());
        Assertions.assertEquals(2, result.failed().size());
        Assertions.assertEquals(sourceDirectory.resolve("InvalidIT.xml"), result.failed().get(0).source());
        Assertions.assertTrue(result.failed().get(0).error().startsWith("Failed to parse Spring XML test source"));
        Assertions.assertEquals(sourceDirectory.resolve("MultipleIT.xml"), result.failed().get(1).source());
        Assertions.assertEquals("Spring XML test source must define exactly one test case, but found 2", result.failed().get(1).error());
    }

    @Test
    public void shouldRejectInvalidArguments() {
        Path sourceDirectory = tempDir.resolve("source");
        Assertions.assertThrows(CitrusRuntimeException.class, () -> migration.migrate(sourceDirectory, tempDir.resolve("target")));
        Assertions.assertThrows(CitrusRuntimeException.class, () -> migration.migrate(tempDir, tempDir));

        String springXml = springTest("<template name=\"foo\"><echo><message>Hello</message></echo></template>");
        CitrusRuntimeException error = Assertions.assertThrows(CitrusRuntimeException.class, () -> migration.migrateTest(springXml));
        Assertions.assertEquals("Spring XML test source must define exactly one test case, but found 0", error.getMessage());

        error = Assertions.assertThrows(CitrusRuntimeException.class, () -> migration.migrateTemplate(springXml, "bar"));
        Assertions.assertEquals("Unable to find template 'bar' in Spring XML test source", error.getMessage());

        String duplicateTemplates = springTest("<template name=\"foo\"><sleep/></template><template name=\"foo\"><sleep/></template>");
        error = Assertions.assertThrows(CitrusRuntimeException.class, () -> migration.migrateTemplate(duplicateTemplates, "foo"));
        Assertions.assertEquals("Spring XML test source defines template 'foo' more than once", error.getMessage());
    }

    private String migrateTest(String sourceFile) throws Exception {
        String migrated = migration.migrateTest(readSource(sourceFile));
        validate(migrated);
        Assertions.assertFalse(migrated.contains("ns0:"));
        return migrated;
    }

    private TestCase load(String migrated, String fileName) throws IOException {
        Path testFile = tempDir.resolve(fileName);
        Files.writeString(testFile, migrated, StandardCharsets.UTF_8);

        XmlTestLoader testLoader = createTestLoader("file:" + testFile);
        testLoader.load();
        return testLoader.getTestCase();
    }

    private static String readSource(String sourceFile) throws IOException {
        return FileUtils.readToString(Resources.create(SOURCE_PATH + sourceFile));
    }

    private static String springTest(String content) {
        return "<spring:beans xmlns=\"http://www.citrusframework.org/schema/testcase\" xmlns:spring=\"http://www.springframework.org/schema/beans\">" +
                content + "</spring:beans>";
    }

    /**
     * Validates the migrated source against the plain XML test DSL schema.
     */
    private static void validate(String migrated) throws Exception {
        Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(new StreamSource(Resources.create("classpath:org/citrusframework/schema/xml/testcase/citrus-testcase.xsd").getInputStream()));
        schema.newValidator().validate(new StreamSource(new StringReader(migrated)));
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static void assertXPath(Document document, String expected, String expression) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new MigrationNamespaceContext());
        Assertions.assertEquals(expected, xpath.evaluate(expression, document, XPathConstants.STRING), expression);
    }

    private static class MigrationNamespaceContext implements NamespaceContext {
        private static final Map<String, String> NAMESPACES = Map.of(
                "t", TEST_NS,
                "s", "http://www.citrusframework.org/schema/testcase",
                "zk", "http://www.citrusframework.org/schema/zookeeper/testcase",
                "h", "http://citrusframework.org/schemas/hello",
                "c", "http://citrusframework.org/schemas/context",
                "o", "http://citrusframework.org/header");

        @Override
        public String getNamespaceURI(String prefix) {
            return NAMESPACES.getOrDefault(prefix, XMLConstants.NULL_NS_URI);
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return null;
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            return Collections.emptyIterator();
        }
    }
}
