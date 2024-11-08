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

package org.citrusframework.camel.xml;

import java.util.stream.Collectors;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spring.xml.CamelRouteContextFactoryBean;
import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.camel.actions.AbstractCamelAction;
import org.citrusframework.camel.actions.CamelControlBusAction;
import org.citrusframework.camel.actions.CamelRunIntegrationAction;
import org.citrusframework.camel.actions.CamelStopIntegrationAction;
import org.citrusframework.camel.actions.CamelVerifyIntegrationAction;
import org.citrusframework.camel.actions.CreateCamelComponentAction;
import org.citrusframework.camel.actions.CreateCamelContextAction;
import org.citrusframework.camel.actions.CreateCamelRouteAction;
import org.citrusframework.camel.actions.RemoveCamelRouteAction;
import org.citrusframework.camel.actions.StartCamelContextAction;
import org.citrusframework.camel.actions.StartCamelRouteAction;
import org.citrusframework.camel.actions.StopCamelContextAction;
import org.citrusframework.camel.actions.StopCamelRouteAction;
import org.citrusframework.camel.util.CamelUtils;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.StringUtils;

@XmlRootElement(name = "camel")
public class Camel implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private AbstractTestActionBuilder<?, ?> builder;

    private String description;
    private String actor;
    private String camelContext;

    private ReferenceResolver referenceResolver;

    @XmlElement
    public Camel setDescription(String value) {
        this.description = value;
        return this;
    }

    @XmlAttribute(name = "actor")
    public Camel setActor(String actor) {
        this.actor = actor;
        return this;
    }

    @XmlAttribute(name = "camel-context")
    public Camel setCamelContext(String camelContext) {
        this.camelContext = camelContext;
        return this;
    }

    @XmlElement(name = "control-bus")
    public Camel setControlBus(ControlBus controlBus) {
        CamelControlBusAction.Builder builder = new CamelControlBusAction.Builder()
                .result(controlBus.getResult());

        if (controlBus.route != null) {
            builder.route(controlBus.getRoute().getId(), controlBus.getRoute().getAction());
        }

        if (controlBus.language != null) {
            builder.language(controlBus.getLanguage().getType(), controlBus.getLanguage().getExpression());
        }

        this.builder = builder;
        return this;
    }

    @XmlElement(name = "create-component")
    public Camel setCreateComponent(Component component) {
        CreateCamelComponentAction.Builder builder = new CreateCamelComponentAction.Builder();

        builder.componentName(component.getName());

        if (StringUtils.hasText(component.getScript())) {
            builder.component(component.getScript());
        }

        if (StringUtils.hasText(component.getFile())) {
            builder.component(Resources.create(component.getFile()));
        }

        this.builder = builder;
        return this;
    }

    @XmlElement(name = "create-context")
    public Camel setCreateContext(CamelContext createContext) {
        CreateCamelContextAction.Builder builder = new CreateCamelContextAction.Builder();

        builder.autoStart(createContext.isAutoStart());
        builder.contextName(createContext.getName());

        this.builder = builder;
        return this;
    }

    @XmlElement(name = "start-context")
    public Camel setStartContext(CamelContext startContext) {
        StartCamelContextAction.Builder builder = new StartCamelContextAction.Builder();
        builder.contextName(startContext.getName());

        this.builder = builder;
        return this;
    }

    @XmlElement(name = "stop-context")
    public Camel setStopContext(CamelContext stopContext) {
        StopCamelContextAction.Builder builder = new StopCamelContextAction.Builder();
        builder.contextName(stopContext.getName());

        this.builder = builder;
        return this;
    }

    @XmlElement(name = "create-routes")
    public Camel setCreateRoutes(CreateRoutes createRoutes) {
        CreateCamelRouteAction.Builder builder = new CreateCamelRouteAction.Builder();

        if (createRoutes.routeSpec != null) {
            try {
                CamelRouteContextFactoryBean factoryBean = (CamelRouteContextFactoryBean) CamelUtils.getJaxbContext().createUnmarshaller().unmarshal(createRoutes.routeSpec);
                builder.routes(factoryBean.getRoutes());
            } catch (JAXBException | ClassCastException e) {
                try {
                    RouteDefinition rd = (RouteDefinition) CamelUtils.getJaxbContext().createUnmarshaller().unmarshal(createRoutes.routeSpec);
                    builder.route(rd);
                } catch (JAXBException | ClassCastException ex) {
                    throw new CitrusRuntimeException("Failed to parse routes from given route specification", ex);
                }
            }
        }

        this.builder = builder;
        return this;
    }

    @XmlElement(name = "jbang")
    public Camel setJBang(JBang jbang) {
        if (jbang.getRun() != null) {
            CamelRunIntegrationAction.Builder builder = new CamelRunIntegrationAction.Builder()
                    .integrationName(jbang.getRun().getIntegration().getName());

            if (jbang.getRun().getIntegration().getSource() != null) {
                builder.integration(jbang.getRun().getIntegration().getSource());
            }

            builder.autoRemove(jbang.getRun().isAutoRemove());
            builder.waitForRunningState(jbang.getRun().isWaitForRunningState());

            if (jbang.getRun().getArgLine() != null) {
                builder.withArgs(jbang.getRun().getArgLine().split(" "));
            }

            if (jbang.getRun().getArgs() != null) {
                builder.withArgs(jbang.getRun().getArgs().getArgs().toArray(String[]::new));
            }

            if (jbang.getRun().getResources() != null) {
                jbang.getRun().getResources().getResources().forEach(builder::addResource);
            }

            if (jbang.getRun().getIntegration().getFile() != null) {
                builder.integration(Resources.create(jbang.getRun().getIntegration().getFile()));
            }

            if (jbang.getRun().getIntegration().getSystemProperties() != null) {
                if (jbang.getRun().getIntegration().getSystemProperties().getFile() != null) {
                    builder.withSystemProperties(Resources.create(
                            jbang.getRun().getIntegration().getSystemProperties().getFile()));
                }

                jbang.getRun().getIntegration().getSystemProperties()
                        .getProperties()
                        .forEach(property -> builder.withSystemProperty(property.getName(), property.getValue()));
            }

            if (jbang.getRun().getIntegration().getEnvironment() != null) {
                if (jbang.getRun().getIntegration().getEnvironment().getFile() != null) {
                    builder.withEnvs(Resources.create(
                            jbang.getRun().getIntegration().getEnvironment().getFile()));
                }

                jbang.getRun().getIntegration().getEnvironment()
                        .getVariables()
                        .forEach(variable -> builder.withEnv(variable.getName(), variable.getValue()));
            }

            this.builder = builder;
        } else if (jbang.getStop() != null) {
            CamelStopIntegrationAction.Builder builder = new CamelStopIntegrationAction.Builder()
                    .integrationName(jbang.getStop().getIntegration());
            this.builder = builder;
        } else if (jbang.getVerify() != null) {
            CamelVerifyIntegrationAction.Builder builder = new CamelVerifyIntegrationAction.Builder()
                    .integrationName(jbang.getVerify().getIntegration())
                    .isInPhase(jbang.getVerify().getPhase())
                    .stopOnErrorStatus(jbang.getVerify().isStopOnErrorStatus())
                    .printLogs(jbang.getVerify().isPrintLogs())
                    .maxAttempts(jbang.getVerify().getMaxAttempts())
                    .delayBetweenAttempts(jbang.getVerify().getDelayBetweenAttempts());

            if (jbang.getVerify().getLogMessage() != null) {
                builder.waitForLogMessage(jbang.getVerify().getLogMessage());
            }

            builder.camelVersion(jbang.getCamelVersion());
            builder.kameletsVersion(jbang.getKameletsVersion());

            this.builder = builder;
        }

        return this;
    }

    @XmlElement(name = "start-routes")
    public Camel setStartRoutes(Routes startRoutes) {
        StartCamelRouteAction.Builder builder = new StartCamelRouteAction.Builder();

        builder.routeIds(startRoutes.getRoutes().stream().map(Route::getId).collect(Collectors.toList()));

        this.builder = builder;
        return this;
    }

    @XmlElement(name = "stop-routes")
    public Camel setStopRoutes(Routes stopRoutes) {
        StopCamelRouteAction.Builder builder = new StopCamelRouteAction.Builder();

        builder.routeIds(stopRoutes.getRoutes().stream().map(Route::getId).collect(Collectors.toList()));

        this.builder = builder;
        return this;
    }

    @XmlElement(name = "remove-routes")
    public Camel setRemoveRoutes(Routes removeRoutes) {
        RemoveCamelRouteAction.Builder builder = new RemoveCamelRouteAction.Builder();

        builder.routeIds(removeRoutes.getRoutes().stream().map(Route::getId).collect(Collectors.toList()));

        this.builder = builder;
        return this;
    }

    @Override
    public TestAction build() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing Camel action - please provide proper action details");
        }

        if (builder instanceof ReferenceResolverAware referenceResolverAware) {
            referenceResolverAware.setReferenceResolver(referenceResolver);
        }

        builder.description(description);

        if (referenceResolver != null) {
            if (actor != null) {
                builder.actor(referenceResolver.resolve(actor, TestActor.class));
            }

            if (camelContext != null && builder instanceof AbstractCamelAction.Builder<?, ?> camelActionBuilder) {
                camelActionBuilder.context(referenceResolver.resolve(camelContext, org.apache.camel.CamelContext.class));
            }
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
