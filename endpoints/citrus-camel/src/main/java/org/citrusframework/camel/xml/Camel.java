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
                    .implementation(infra.getRun().getImplementation())
                    .port(infra.getRun().getPort())
                    .fixedPort(infra.getRun().isFixedPort());

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
    public void setJBang(Cli cli) {
        setCli(cli);
    }

    @XmlElement(name = "cli")
    public void setCli(Cli cli) {
        if (cli.getRun() != null) {
            CamelRunIntegrationAction.Builder builder = new CamelRunIntegrationAction.Builder();

            if (cli.getRun().getIntegration().getName() != null) {
                builder.integrationName(cli.getRun().getIntegration().getName());
            }

            if (cli.getRun().getIntegration().getSource() != null) {
                builder.integration(cli.getRun().getIntegration().getSource());
            }

            builder.autoRemove(cli.getRun().isAutoRemove());
            builder.waitForRunningState(cli.getRun().isWaitForRunningState());
            builder.dumpIntegrationOutput(cli.getRun().isDumpIntegrationOutput());

            if (cli.getRun().getArgLine() != null) {
                builder.withArgs(cli.getRun().getArgLine().split(" "));
            }

            if (cli.getRun().getArgs() != null) {
                builder.withArgs(cli.getRun().getArgs().getArgs().toArray(String[]::new));
            }

            if (cli.getRun().getStub() != null) {
                builder.stub(cli.getRun().getStub().split(" "));
            }

            if (cli.getRun().getStubs() != null) {
                builder.stub(cli.getRun().getStubs().getStubs().toArray(String[]::new));
            }

            if (cli.getRun().getResources() != null) {
                cli.getRun().getResources().getResources().forEach(builder::addResource);
            }

            if (cli.getRun().getIntegration().getFile() != null) {
                builder.integration(Resources.create(cli.getRun().getIntegration().getFile()));
            }

            if (cli.getRun().getIntegration().getSystemProperties() != null) {
                if (cli.getRun().getIntegration().getSystemProperties().getFile() != null) {
                    builder.withSystemProperties(Resources.create(
                            cli.getRun().getIntegration().getSystemProperties().getFile()));
                }

                cli.getRun().getIntegration().getSystemProperties()
                        .getProperties()
                        .forEach(property -> builder.withSystemProperty(property.getName(), property.getValue()));
            }

            if (cli.getRun().getIntegration().getEnvironment() != null) {
                if (cli.getRun().getIntegration().getEnvironment().getFile() != null) {
                    builder.withEnvs(Resources.create(
                            cli.getRun().getIntegration().getEnvironment().getFile()));
                }

                cli.getRun().getIntegration().getEnvironment()
                        .getVariables()
                        .forEach(variable -> builder.withEnv(variable.getName(), variable.getValue()));
            }

            this.builder = builder;
        } else if (cli.getStop() != null) {
            CamelStopIntegrationAction.Builder builder = new CamelStopIntegrationAction.Builder()
                    .integrationName(cli.getStop().getIntegration());
            this.builder = builder;
        }  else if (cli.getCustom() != null) {
            CamelCustomizedRunIntegrationAction.Builder builder = new CamelCustomizedRunIntegrationAction.Builder()
                    .commands(cli.getCustom().getCommands().toArray(new String[0]));

            if (cli.getCustom().getCommandLine() != null) {
                builder.commands(cli.getCustom().getCommandLine().split(" "));
            }

            builder.workDir(cli.getCustom().getWorkDir());
            if (cli.getCustom().getProcessName() != null) {
                builder.processName(cli.getCustom().getProcessName());
            }

            builder.autoRemove(cli.getCustom().isAutoRemove());
            builder.waitForRunningState(cli.getCustom().isWaitForRunningState());
            builder.dumpIntegrationOutput(cli.getCustom().isDumpIntegrationOutput());

            if (cli.getCustom().getArgLine() != null) {
                builder.withArgs(cli.getCustom().getArgLine().split(" "));
            }

            if (cli.getCustom().getArgs() != null) {
                builder.withArgs(cli.getCustom().getArgs().getArgs().toArray(String[]::new));
            }

            if (cli.getCustom().getResources() != null) {
                cli.getCustom().getResources().getResources().forEach(builder::addResource);
            }

            if (cli.getCustom().getIntegration().getFile() != null) {
                builder.addResource(cli.getCustom().getIntegration().getFile());
            }

            if (cli.getCustom().getIntegration().getSystemProperties() != null) {
                if (cli.getCustom().getIntegration().getSystemProperties().getFile() != null) {
                    builder.withSystemProperties(Resources.create(
                            cli.getCustom().getIntegration().getSystemProperties().getFile()));
                }

                cli.getCustom().getIntegration().getSystemProperties()
                        .getProperties()
                        .forEach(property -> builder.withSystemProperty(property.getName(), property.getValue()));
            }

            if (cli.getCustom().getIntegration().getEnvironment() != null) {
                if (cli.getCustom().getIntegration().getEnvironment().getFile() != null) {
                    builder.withEnvs(Resources.create(
                            cli.getCustom().getIntegration().getEnvironment().getFile()));
                }

                cli.getCustom().getIntegration().getEnvironment()
                        .getVariables()
                        .forEach(variable -> builder.withEnv(variable.getName(), variable.getValue()));
            }

            this.builder = builder;
        } else if (cli.getVerify() != null) {
            CamelVerifyIntegrationAction.Builder builder = new CamelVerifyIntegrationAction.Builder()
                    .integrationName(cli.getVerify().getIntegration())
                    .isInPhase(cli.getVerify().getPhase())
                    .stopOnErrorStatus(cli.getVerify().isStopOnErrorStatus())
                    .printLogs(cli.getVerify().isPrintLogs())
                    .maxAttempts(cli.getVerify().getMaxAttempts())
                    .delayBetweenAttempts(cli.getVerify().getDelayBetweenAttempts());

            if (cli.getVerify().getLogMessage() != null) {
                builder.waitForLogMessage(cli.getVerify().getLogMessage());
            }

            builder.camelVersion(cli.getCamelVersion());
            builder.kameletsVersion(cli.getKameletsVersion());

            this.builder = builder;
        } else if (cli.getPlugin() != null) {
            if (cli.getPlugin().getAdd() != null) {
                AddCamelPluginAction.Builder builder = new AddCamelPluginAction.Builder();
                builder.pluginName(cli.getPlugin().getAdd().getName());
                if (cli.getPlugin().getAdd().getArgLine() != null) {
                    builder.withArgs(cli.getPlugin().getAdd().getArgLine().split(" "));
                }
                builder.autoRemove(cli.getPlugin().getAdd().isAutoRemove());
                this.builder = builder;
            } else if (cli.getPlugin().getDelete() != null) {
                DeleteCamelPluginAction.Builder builder = new DeleteCamelPluginAction.Builder();
                builder.pluginName(cli.getPlugin().getDelete().getName());
                this.builder = builder;
            }
        } else if (cli.getCmd() != null) {
            if (cli.getCmd().getSend() != null) {
                CamelCmdSendAction.Builder builder = new CamelCmdSendAction.Builder();
                builder.integration(cli.getCmd().getSend().getIntegration());

                builder.timeout(cli.getCmd().getSend().getTimeout());

                if (cli.getCmd().getSend().getHeaders() != null) {
                    for (Cli.Cmd.Send.Headers.Header header : cli.getCmd().getSend().getHeaders().getHeaders()) {
                        builder.header(header.getName(), header.getValue());
                    }
                }

                if (cli.getCmd().getSend().getBody() != null) {
                    if (cli.getCmd().getSend().getBody().getData() != null) {
                        builder.body(cli.getCmd().getSend().getBody().getData());
                    } else if (cli.getCmd().getSend().getBody().getFile() != null) {
                        builder.body("file:" + cli.getCmd().getSend().getBody().getFile());
                    }
                }

                if (cli.getCmd().getSend().getInfra() != null) {
                    builder.endpoint(cli.getCmd().getSend().getInfra());
                }

                if (cli.getCmd().getSend().getEndpoint() != null) {
                    builder.endpoint(cli.getCmd().getSend().getEndpoint());
                }

                if (cli.getCmd().getSend().getUri() != null) {
                    builder.endpointUri(cli.getCmd().getSend().getUri());
                }

                if (cli.getCmd().getSend().getArgLine() != null) {
                    builder.withArgs(cli.getCmd().getSend().getArgLine().split(" "));
                }

                builder.reply(cli.getCmd().getSend().isReply());

                this.builder = builder;
            } else if (cli.getCmd().getReceive() != null) {
                CamelCmdReceiveAction.Builder builder = new CamelCmdReceiveAction.Builder();
                builder.integration(cli.getCmd().getReceive().getIntegration());

                if (cli.getCmd().getReceive().getEndpoint() != null) {
                    builder.endpoint(cli.getCmd().getReceive().getEndpoint());
                }

                if (cli.getCmd().getReceive().getUri() != null) {
                    builder.endpointUri(cli.getCmd().getReceive().getUri());
                }

                if (cli.getCmd().getReceive().getArgLine() != null) {
                    builder.withArgs(cli.getCmd().getReceive().getArgLine().split(" "));
                }

                if (cli.getCmd().getReceive().getGrep() != null) {
                    builder.grep(cli.getCmd().getReceive().getGrep());
                }

                builder.loggingColor(cli.getCmd().getReceive().isLoggingColor());

                builder.jsonOutput(cli.getCmd().getReceive().isJsonOutput());

                if (cli.getCmd().getReceive().getSince() != null) {
                    builder.since(cli.getCmd().getReceive().getSince());
                }

                builder.tail(cli.getCmd().getReceive().getTail());

                builder.maxAttempts(cli.getCmd().getReceive().getMaxAttempts());
                builder.delayBetweenAttempts(cli.getCmd().getReceive().getDelayBetweenAttempts());

                this.builder = builder;
            }
        } else if (cli.getKubernetes() != null) {
            if (cli.getKubernetes().getRun() != null) {
                CamelKubernetesRunIntegrationAction.Builder builder = new CamelKubernetesRunIntegrationAction.Builder();

                Cli.Kubernetes.Run run = cli.getKubernetes().getRun();

                if (run.getIntegration().getName() != null) {
                    builder.integrationName(run.getIntegration().getName());
                }
                if (run.getIntegration().getSource() != null) {
                    builder.integration(run.getIntegration().getSource());
                }
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
            } else if (cli.getKubernetes().getVerify() != null) {
                CamelKubernetesVerifyIntegrationAction.Builder builder = new CamelKubernetesVerifyIntegrationAction.Builder();

                builder.integration(cli.getKubernetes().getVerify().getIntegration())
                        .label(cli.getKubernetes().getVerify().getLabel())
                        .namespace(cli.getKubernetes().getVerify().getNamespace())
                        .printLogs(cli.getKubernetes().getVerify().isPrintLogs())
                        .maxAttempts(cli.getKubernetes().getVerify().getMaxAttempts())
                        .delayBetweenAttempts(cli.getKubernetes().getVerify().getDelayBetweenAttempts());
                if (cli.getKubernetes().getVerify().getLogMessage() != null) {
                    builder.waitForLogMessage(cli.getKubernetes().getVerify().getLogMessage());
                }
                if (cli.getKubernetes().getVerify().getArgs() != null) {
                    builder.withArgs(cli.getKubernetes().getVerify().getArgs().getArgs().toArray(String[]::new));
                }
                this.builder = builder;
            } else if (cli.getKubernetes().getDelete() != null) {
                CamelKubernetesDeleteIntegrationAction.Builder builder = new CamelKubernetesDeleteIntegrationAction.Builder();

                if (cli.getKubernetes().getDelete().getIntegration() != null) {
                    if (cli.getKubernetes().getDelete().getIntegration().getFile() != null) {
                        builder.integration(Resources.create(cli.getKubernetes().getDelete().getIntegration().getFile()));
                    }
                    if (cli.getKubernetes().getDelete().getIntegration().getName() != null) {
                        builder.integration(cli.getKubernetes().getDelete().getIntegration().getName());
                    }
                }
                builder.clusterType(cli.getKubernetes().getDelete().getClusterType())
                        .namespace(cli.getKubernetes().getDelete().getNamespace())
                        .workingDir(cli.getKubernetes().getDelete().getWorkingDir());
                this.builder = builder;
            }
        }

        if (this.builder instanceof AbstractCamelCliAction.Builder<?, ?> camelBuilder) {
            if (cli.getCamelVersion() != null) {
                camelBuilder.camelVersion(cli.getCamelVersion());
            }

            if (cli.getKameletsVersion() != null) {
                camelBuilder.kameletsVersion(cli.getKameletsVersion());
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
