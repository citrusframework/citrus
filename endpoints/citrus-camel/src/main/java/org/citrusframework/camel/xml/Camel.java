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
import org.citrusframework.camel.actions.*;
import org.citrusframework.camel.actions.infra.CamelRunInfraAction;
import org.citrusframework.camel.actions.infra.CamelStopInfraAction;
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
    public void setDescription(String value) {
        this.description = value;
    }

    @XmlAttribute(name = "actor")
    public void setActor(String actor) {
        this.actor = actor;
    }

    @XmlAttribute(name = "camel-context")
    public void setCamelContext(String camelContext) {
        this.camelContext = camelContext;
    }

    @XmlElement(name = "control-bus")
    public void setControlBus(ControlBus controlBus) {
        CamelControlBusAction.Builder builder = new CamelControlBusAction.Builder()
                .result(controlBus.getResult());

        if (controlBus.route != null) {
            builder.route(controlBus.getRoute().getId(), controlBus.getRoute().getAction());
        }

        if (controlBus.language != null) {
            builder.language(controlBus.getLanguage().getType(), controlBus.getLanguage().getExpression());
        }

        this.builder = builder;
    }

    @XmlElement(name = "create-component")
    public void setCreateComponent(Component component) {
        CreateCamelComponentAction.Builder builder = new CreateCamelComponentAction.Builder();

        builder.componentName(component.getName());

        if (StringUtils.hasText(component.getScript())) {
            builder.component(component.getScript());
        }

        if (StringUtils.hasText(component.getFile())) {
            builder.component(Resources.create(component.getFile()));
        }

        this.builder = builder;
    }

    @XmlElement(name = "create-context")
    public void setCreateContext(CamelContext createContext) {
        CreateCamelContextAction.Builder builder = new CreateCamelContextAction.Builder();

        builder.autoStart(createContext.isAutoStart());
        builder.contextName(createContext.getName());

        this.builder = builder;
    }

    @XmlElement(name = "start-context")
    public void setStartContext(CamelContext startContext) {
        StartCamelContextAction.Builder builder = new StartCamelContextAction.Builder();
        builder.contextName(startContext.getName());

        this.builder = builder;
    }

    @XmlElement(name = "stop-context")
    public void setStopContext(CamelContext stopContext) {
        StopCamelContextAction.Builder builder = new StopCamelContextAction.Builder();
        builder.contextName(stopContext.getName());

        this.builder = builder;
    }

    @XmlElement(name = "create-routes")
    public void setCreateRoutes(CreateRoutes createRoutes) {
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

        if (createRoutes.route != null) {
            if (StringUtils.hasText(createRoutes.getRoute().getId())) {
                builder.routeId(createRoutes.getRoute().getId());
            }

            if (StringUtils.hasText(createRoutes.getRoute().getFile())) {
                builder.route(Resources.create(createRoutes.getRoute().getFile()));
            } else if (StringUtils.hasText(createRoutes.getRoute().getRoute())) {
                builder.route(createRoutes.getRoute().getRoute().trim());
            }
        }

        this.builder = builder;
    }

    @XmlElement(name = "infra")
    public void setInfra(Infra infra) {
        if (infra.getRun() != null) {
            CamelRunInfraAction.Builder builder = new CamelRunInfraAction.Builder()
                    .service(infra.getRun().getService())
                    .implementation(infra.getRun().getImplementation());

            builder.autoRemove(infra.getRun().isAutoRemove());
            builder.dumpServiceOutput(infra.getRun().isDumpServiceOutput());

            this.builder = builder;
        } else if (infra.getStop() != null) {
            CamelStopInfraAction.Builder builder = new CamelStopInfraAction.Builder()
                    .service(infra.getStop().getService())
                    .implementation(infra.getStop().getImplementation());
            this.builder = builder;
        }
    }

    @XmlElement(name = "jbang")
    public void setJBang(JBang jbang) {
        if (jbang.getRun() != null) {
            CamelRunIntegrationAction.Builder builder = new CamelRunIntegrationAction.Builder()
                    .integrationName(jbang.getRun().getIntegration().getName());

            if (jbang.getRun().getIntegration().getSource() != null) {
                builder.integration(jbang.getRun().getIntegration().getSource());
            }

            builder.autoRemove(jbang.getRun().isAutoRemove());
            builder.waitForRunningState(jbang.getRun().isWaitForRunningState());
            builder.dumpIntegrationOutput(jbang.getRun().isDumpIntegrationOutput());

            if (jbang.getRun().getArgLine() != null) {
                builder.withArgs(jbang.getRun().getArgLine().split(" "));
            }

            if (jbang.getRun().getArgs() != null) {
                builder.withArgs(jbang.getRun().getArgs().getArgs().toArray(String[]::new));
            }

            if (jbang.getRun().getStub() != null) {
                builder.stub(jbang.getRun().getStub().split(" "));
            }

            if (jbang.getRun().getStubs() != null) {
                builder.stub(jbang.getRun().getStubs().getStubs().toArray(String[]::new));
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
        }  else if (jbang.getCustom() != null) {
            CamelCustomizedRunIntegrationAction.Builder builder = new CamelCustomizedRunIntegrationAction.Builder()
                    .commands(jbang.getCustom().getCommands().toArray(new String[0]));

            if (jbang.getCustom().getCommandLine() != null) {
                builder.commands(jbang.getCustom().getCommandLine().split(" "));
            }

            builder.workDir(jbang.getCustom().getWorkDir());
            if (jbang.getCustom().getProcessName() != null) {
                builder.processName(jbang.getCustom().getProcessName());
            }

            builder.autoRemove(jbang.getCustom().isAutoRemove());
            builder.waitForRunningState(jbang.getCustom().isWaitForRunningState());
            builder.dumpIntegrationOutput(jbang.getCustom().isDumpIntegrationOutput());

            if (jbang.getCustom().getArgLine() != null) {
                builder.withArgs(jbang.getCustom().getArgLine().split(" "));
            }

            if (jbang.getCustom().getArgs() != null) {
                builder.withArgs(jbang.getCustom().getArgs().getArgs().toArray(String[]::new));
            }

            if (jbang.getCustom().getResources() != null) {
                jbang.getCustom().getResources().getResources().forEach(builder::addResource);
            }

            if (jbang.getCustom().getIntegration().getFile() != null) {
                builder.addResource(jbang.getCustom().getIntegration().getFile());
            }

            if (jbang.getCustom().getIntegration().getSystemProperties() != null) {
                if (jbang.getCustom().getIntegration().getSystemProperties().getFile() != null) {
                    builder.withSystemProperties(Resources.create(
                            jbang.getCustom().getIntegration().getSystemProperties().getFile()));
                }

                jbang.getCustom().getIntegration().getSystemProperties()
                        .getProperties()
                        .forEach(property -> builder.withSystemProperty(property.getName(), property.getValue()));
            }

            if (jbang.getCustom().getIntegration().getEnvironment() != null) {
                if (jbang.getCustom().getIntegration().getEnvironment().getFile() != null) {
                    builder.withEnvs(Resources.create(
                            jbang.getCustom().getIntegration().getEnvironment().getFile()));
                }

                jbang.getCustom().getIntegration().getEnvironment()
                        .getVariables()
                        .forEach(variable -> builder.withEnv(variable.getName(), variable.getValue()));
            }

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
        } else if (jbang.getPlugin() != null) {
            if (jbang.getPlugin().getAdd() != null) {
                AddCamelPluginAction.Builder builder = new AddCamelPluginAction.Builder();
                builder.pluginName(jbang.getPlugin().getAdd().getName());
                if (jbang.getPlugin().getAdd().getArgLine() != null) {
                    builder.withArgs(jbang.getPlugin().getAdd().getArgLine().split(" "));
                }
                this.builder = builder;
            }
        } else if (jbang.getCmd() != null) {
            if (jbang.getCmd().getSend() != null) {
                CamelCmdSendAction.Builder builder = new CamelCmdSendAction.Builder();
                builder.integration(jbang.getCmd().getSend().getIntegration());

                builder.timeout(jbang.getCmd().getSend().getTimeout());

                if (jbang.getCmd().getSend().getHeaders() != null) {
                    for (JBang.Cmd.Send.Headers.Header header : jbang.getCmd().getSend().getHeaders().getHeaders()) {
                        builder.header(header.getName(), header.getValue());
                    }
                }

                if (jbang.getCmd().getSend().getBody() != null) {
                    if (jbang.getCmd().getSend().getBody().getData() != null) {
                        builder.body(jbang.getCmd().getSend().getBody().getData());
                    } else if (jbang.getCmd().getSend().getBody().getFile() != null) {
                        builder.body("file:" + jbang.getCmd().getSend().getBody().getFile());
                    }
                }

                if (jbang.getCmd().getSend().getEndpoint() != null) {
                    builder.endpoint(jbang.getCmd().getSend().getEndpoint());
                }

                if (jbang.getCmd().getSend().getUri() != null) {
                    builder.endpointUri(jbang.getCmd().getSend().getUri());
                }

                if (jbang.getCmd().getSend().getArgLine() != null) {
                    builder.withArgs(jbang.getCmd().getSend().getArgLine().split(" "));
                }

                builder.reply(jbang.getCmd().getSend().isReply());

                this.builder = builder;
            } else if (jbang.getCmd().getReceive() != null) {
                CamelCmdReceiveAction.Builder builder = new CamelCmdReceiveAction.Builder();
                builder.integration(jbang.getCmd().getReceive().getIntegration());

                if (jbang.getCmd().getReceive().getEndpoint() != null) {
                    builder.endpoint(jbang.getCmd().getReceive().getEndpoint());
                }

                if (jbang.getCmd().getReceive().getUri() != null) {
                    builder.endpointUri(jbang.getCmd().getReceive().getUri());
                }

                if (jbang.getCmd().getReceive().getArgLine() != null) {
                    builder.withArgs(jbang.getCmd().getReceive().getArgLine().split(" "));
                }

                if (jbang.getCmd().getReceive().getGrep() != null) {
                    builder.grep(jbang.getCmd().getReceive().getGrep());
                }

                builder.loggingColor(jbang.getCmd().getReceive().isLoggingColor());

                if (jbang.getCmd().getReceive().getSince() != null) {
                    builder.since(jbang.getCmd().getReceive().getSince());
                }

                if (jbang.getCmd().getReceive().getTail() != null) {
                    builder.tail(jbang.getCmd().getReceive().getTail());
                }

                builder.maxAttempts(jbang.getCmd().getReceive().getMaxAttempts());
                builder.delayBetweenAttempts(jbang.getCmd().getReceive().getDelayBetweenAttempts());

                this.builder = builder;
            }
        } else if (jbang.getKubernetes() != null) {
            if (jbang.getKubernetes().getRun() != null) {
                CamelKubernetesRunIntegrationAction.Builder builder = new CamelKubernetesRunIntegrationAction.Builder();

                JBang.Kubernetes.Run run = jbang.getKubernetes().getRun();

                if (run.getIntegration().getFile() != null) {
                    builder.integration(Resources.create(run.getIntegration().getFile()));
                }
                if (run.getRuntime() != null) {
                    builder.runtime(run.getRuntime());
                }
                if (run.getImageBuilder() != null) {
                    builder.imageBuilder(run.getImageBuilder());
                }
                if (run.getImageRegistry() != null) {
                    builder.imageRegistry(run.getImageRegistry());
                }
                if (run.getClusterType() != null) {
                    builder.clusterType(run.getClusterType());
                }
                if (run.getBuildProperties() != null) {
                    run.getBuildProperties()
                            .getProperties()
                            .forEach(property -> builder.withBuildProperties(property.getName() + "=\"" + property.getValue() + "\""));
                }
                if (run.getProperties() != null) {
                    run.getProperties()
                            .getProperties()
                            .forEach(property -> builder.withProperties(property.getName() + "=\"" + property.getValue() + "\""));
                }
                if (run.getTraits() != null) {
                    run.getTraits()
                            .getTraits()
                            .forEach(trait -> builder.withTrait(trait.getName() + "=\"" + trait.getValue() + "\""));
                }
                if (run.getArgs() != null) {
                    builder.withArgs(run.getArgs().getArgs().toArray(String[]::new));
                }
                if (run.getArgLine() != null) {
                    builder.withArgs(run.getArgLine().split(" "));
                }

                builder.verbose(run.isVerbose());

                builder.autoRemove(run.isAutoRemove());

                builder.waitForRunningState(run.isWaitForRunningState());

                this.builder = builder;
            } else if (jbang.getKubernetes().getVerify() != null) {
                CamelKubernetesVerifyIntegrationAction.Builder builder = new CamelKubernetesVerifyIntegrationAction.Builder();

                builder.integration(jbang.getKubernetes().getVerify().getIntegration())
                        .label(jbang.getKubernetes().getVerify().getLabel())
                        .namespace(jbang.getKubernetes().getVerify().getNamespace())
                        .printLogs(jbang.getKubernetes().getVerify().isPrintLogs())
                        .maxAttempts(jbang.getKubernetes().getVerify().getMaxAttempts())
                        .delayBetweenAttempts(jbang.getKubernetes().getVerify().getDelayBetweenAttempts());
                if (jbang.getKubernetes().getVerify().getLogMessage() != null) {
                    builder.waitForLogMessage(jbang.getKubernetes().getVerify().getLogMessage());
                }
                if (jbang.getKubernetes().getVerify().getArgs() != null) {
                    builder.withArgs(jbang.getKubernetes().getVerify().getArgs().getArgs().toArray(String[]::new));
                }
                this.builder = builder;
            } else if (jbang.getKubernetes().getDelete() != null) {
                CamelKubernetesDeleteIntegrationAction.Builder builder = new CamelKubernetesDeleteIntegrationAction.Builder();

                if (jbang.getKubernetes().getDelete().getIntegration() != null) {
                    if (jbang.getKubernetes().getDelete().getIntegration().getFile() != null) {
                        builder.integration(Resources.create(jbang.getKubernetes().getDelete().getIntegration().getFile()));
                    }
                    if (jbang.getKubernetes().getDelete().getIntegration().getName() != null) {
                        builder.integration(jbang.getKubernetes().getDelete().getIntegration().getName());
                    }
                }
                builder.clusterType(jbang.getKubernetes().getDelete().getClusterType())
                        .namespace(jbang.getKubernetes().getDelete().getNamespace())
                        .workingDir(jbang.getKubernetes().getDelete().getWorkingDir());
                this.builder = builder;
            }
        }
    }

    @XmlElement(name = "start-routes")
    public void setStartRoutes(Routes startRoutes) {
        StartCamelRouteAction.Builder builder = new StartCamelRouteAction.Builder();

        builder.routeIds(startRoutes.getRoutes().stream().map(Route::getId).collect(Collectors.toList()));

        this.builder = builder;
    }

    @XmlElement(name = "stop-routes")
    public void setStopRoutes(Routes stopRoutes) {
        StopCamelRouteAction.Builder builder = new StopCamelRouteAction.Builder();

        builder.routeIds(stopRoutes.getRoutes().stream().map(Route::getId).collect(Collectors.toList()));

        this.builder = builder;
    }

    @XmlElement(name = "remove-routes")
    public void setRemoveRoutes(Routes removeRoutes) {
        RemoveCamelRouteAction.Builder builder = new RemoveCamelRouteAction.Builder();

        builder.routeIds(removeRoutes.getRoutes().stream().map(Route::getId).collect(Collectors.toList()));

        this.builder = builder;
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
