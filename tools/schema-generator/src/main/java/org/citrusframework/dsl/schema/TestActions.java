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

package org.citrusframework.dsl.schema;

import org.citrusframework.agent.connector.yaml.Agent;
import org.citrusframework.camel.yaml.Camel;
import org.citrusframework.groovy.yaml.Groovy;
import org.citrusframework.http.yaml.Http;
import org.citrusframework.jbang.yaml.JBang;
import org.citrusframework.jms.yaml.PurgeQueues;
import org.citrusframework.knative.yaml.Knative;
import org.citrusframework.kubernetes.yaml.Kubernetes;
import org.citrusframework.openapi.yaml.OpenApi;
import org.citrusframework.selenium.yaml.Selenium;
import org.citrusframework.sql.yaml.Plsql;
import org.citrusframework.sql.yaml.Sql;
import org.citrusframework.testcontainers.yaml.Testcontainers;
import org.citrusframework.ws.yaml.Soap;
import org.citrusframework.yaml.SchemaProperty;
import org.citrusframework.yaml.actions.*;
import org.citrusframework.yaml.container.Assert;
import org.citrusframework.yaml.container.Async;
import org.citrusframework.yaml.container.Catch;
import org.citrusframework.yaml.container.Conditional;
import org.citrusframework.yaml.container.DoFinally;
import org.citrusframework.yaml.container.Iterate;
import org.citrusframework.yaml.container.Parallel;
import org.citrusframework.yaml.container.Repeat;
import org.citrusframework.yaml.container.RepeatOnError;
import org.citrusframework.yaml.container.Sequential;
import org.citrusframework.yaml.container.Timer;
import org.citrusframework.yaml.container.WaitFor;

import static org.citrusframework.yaml.SchemaProperty.Kind.ACTION;
import static org.citrusframework.yaml.SchemaProperty.Kind.CONTAINER;
import static org.citrusframework.yaml.SchemaProperty.Kind.GROUP;

public class TestActions {

    @SchemaProperty(kind = ACTION, description = "Generic test action.")
    public void setAction(Action builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Apply template test action.")
    public void setApplyTemplate(ApplyTemplate builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Delay test action.")
    public void setDelay(Delay builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Sleep test action.")
    public void setSleep(Sleep builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Ant run test action.")
    public void setAnt(AntRun builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Echo test action.")
    public void setEcho(Echo builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Print test action.")
    public void setPrint(Print builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Purge test action.")
    public void setPurge(PurgeEndpoint builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Start server test action.")
    public void setStart(Start builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Stop server test action.")
    public void setStop(Stop builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Stop time test action.")
    public void setStopTime(StopTime builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Stop timer test action.")
    public void setStopTimer(StopTimer builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Trace variables test action.")
    public void setTrace(TraceVariables builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Transform test action.")
    public void setTransform(Transform builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Load properties test action.")
    public void setLoad(LoadProperties builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Create variables test action.")
    public void setCreateVariables(CreateVariables builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Create endpoints test action.")
    public void setCreateEndpoint(CreateEndpointWrapper builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Send message test action.")
    public void setSend(Send builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Receive message test action.")
    public void setReceive(Receive builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Expect timeout test action.")
    public void setExpectTimeout(ExpectTimeout builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Fail test action.")
    public void setFail(Fail builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Wait for test action.")
    public void setWaitFor(WaitFor builder) {
    }

    @SchemaProperty(kind = CONTAINER, description = "Assert exception test action.")
    public void setAssert(Assert builder) {
    }

    @SchemaProperty(kind = CONTAINER, description = "Catch exception test action.")
    public void setCatch(Catch builder) {
    }

    @SchemaProperty(kind = CONTAINER, description = "Runs test actions after the test.")
    public void setDoFinally(DoFinally builder) {
    }

    @SchemaProperty(kind = CONTAINER, description = "Conditional test action.")
    public void setConditional(Conditional builder) {
    }

    @SchemaProperty(kind = CONTAINER, description = "Sequential test action.")
    public void setSequential(Sequential builder) {
    }

    @SchemaProperty(kind = CONTAINER, description = "Iterate test action.")
    public void setIterate(Iterate builder) {
    }

    @SchemaProperty(kind = CONTAINER, description = "Parallel test action.")
    public void setParallel(Parallel builder) {
    }

    @SchemaProperty(kind = CONTAINER, description = "Repeat test action.")
    public void setRepeat(Repeat builder) {
    }

    @SchemaProperty(kind = CONTAINER, description = "Repeat on error test action.")
    public void setRepeatOnError(RepeatOnError builder) {
    }

    @SchemaProperty(kind = CONTAINER, description = "Timer test action.")
    public void setTimer(Timer builder) {
    }

    @SchemaProperty(kind = CONTAINER, description = "Async test action.")
    public void setAsync(Async builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Groovy test action.")
    public void setGroovy(Groovy builder) {
    }

    @SchemaProperty(kind = GROUP, description = "Test actions related to Apache Camel.")
    public void setCamel(Camel builder) {
    }

    @SchemaProperty(kind = GROUP, description = "Http related test actions.")
    public void setHttp(Http builder) {
    }

    @SchemaProperty(kind = GROUP, description = "OpenAPI related test actions.")
    public void setOpenapi(OpenApi builder) {
    }

    @SchemaProperty(kind = GROUP, description = "SOAP Web Services related test actions.")
    public void setSoap(Soap builder) {
    }

    @SchemaProperty(kind = GROUP, description = "Agent service test actions.")
    public void setAgent(Agent builder) {
    }

    @SchemaProperty(kind = GROUP, description = "Kubernetes test actions.")
    public void setKubernetes(Kubernetes builder) {
    }

    @SchemaProperty(kind = GROUP, description = "Knative test actions.")
    public void setKnative(Knative builder) {
    }

    @SchemaProperty(kind = GROUP, description = "Selenium test actions.")
    public void setSelenium(Selenium builder) {
    }

    @SchemaProperty(kind = GROUP, description = "Testcontainers test actions.")
    public void setTestcontainers(Testcontainers builder) {
    }

    @SchemaProperty(kind = ACTION, description = "SQL test actions.")
    public void setSql(Sql builder) {
    }

    @SchemaProperty(kind = ACTION, description = "PLSQL test actions.")
    public void setPlsql(Plsql builder) {
    }

    @SchemaProperty(kind = ACTION, description = "JBang test actions.")
    public void setJbang(JBang builder) {
    }

    @SchemaProperty(kind = ACTION, description = "Purge JMS queue test action.")
    public void setPurgeQueues(PurgeQueues builder) {
    }

    public static class CreateEndpointWrapper extends CreateEndpoint implements Endpoints {
    }
}
