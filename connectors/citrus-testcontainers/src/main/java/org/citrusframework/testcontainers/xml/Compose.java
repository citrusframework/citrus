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

package org.citrusframework.testcontainers.xml;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestActor;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.citrusframework.testcontainers.WaitStrategyHelper;
import org.citrusframework.testcontainers.actions.AbstractTestcontainersAction;
import org.citrusframework.testcontainers.compose.ComposeDownAction;
import org.citrusframework.testcontainers.compose.ComposeUpAction;
import org.citrusframework.util.ObjectHelper;
import org.citrusframework.util.StringUtils;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

public class Compose extends AbstractTestcontainersAction.Builder<AbstractTestcontainersAction, Compose> implements ReferenceResolverAware {

    private AbstractTestcontainersAction.Builder<? extends AbstractTestcontainersAction, ?> delegate;

    @XmlElement
    public void setUp(Up composeUp) {
        ComposeUpAction.Builder builder = new ComposeUpAction.Builder();

        builder.containerName(composeUp.getName());
        builder.file(composeUp.getFile());
        builder.autoRemove(composeUp.isAutoRemove());

        if (composeUp.getStartUpTimeout() > 0) {
            builder.withStartupTimeout(composeUp.getStartUpTimeout());
        }

        composeUp.getExposedServices().forEach(exposedService -> {
            if (exposedService.getWaitFor() != null) {
                WaitStrategy waitStrategy;
                if (exposedService.getWaitFor().isDisabled()) {
                    waitStrategy = WaitStrategyHelper.getNoopStrategy();
                } else if (StringUtils.hasText(exposedService.getWaitFor().getLogMessage())) {
                    waitStrategy = WaitStrategyHelper.waitFor(exposedService.getWaitFor().getLogMessage());
                } else if (StringUtils.hasText(exposedService.getWaitFor().getUrl())) {
                    try {
                        waitStrategy = WaitStrategyHelper.waitFor(new URL(exposedService.getWaitFor().getUrl()));
                    } catch (MalformedURLException e) {
                        throw new CitrusRuntimeException("Invalid Http(s) URL to wait for: %s".formatted(exposedService.getWaitFor().getUrl()), e);
                    }
                } else {
                    waitStrategy = Wait.defaultWaitStrategy();
                }

                builder.withExposedService(exposedService.getName(), exposedService.getPort(), waitStrategy);
            } else {
                builder.withExposedService(exposedService.getName(), exposedService.getPort());
            }
        });

        delegate = builder;
    }

    @XmlElement
    public void setDown(Down composeDown) {
        ComposeDownAction.Builder builder = new ComposeDownAction.Builder();
        builder.containerName(composeDown.getName());
        delegate = builder;
    }

    @Override
    public Compose description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Compose actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    public AbstractTestcontainersAction doBuild() {
        ObjectHelper.assertNotNull(delegate);
        return delegate.build();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "exposedServices",
    })
    public static class Up {

        @XmlAttribute
        private String name;

        @XmlAttribute
        private String file;

        @XmlAttribute
        private int startUpTimeout;

        @XmlAttribute
        protected boolean autoRemove = TestContainersSettings.isAutoRemoveResources();

        @XmlElement
        protected List<ExposedService> exposedServices;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public boolean isAutoRemove() {
            return autoRemove;
        }

        public void setAutoRemove(boolean autoRemove) {
            this.autoRemove = autoRemove;
        }

        public int getStartUpTimeout() {
            return startUpTimeout;
        }

        public void setStartUpTimeout(int startUpTimeout) {
            this.startUpTimeout = startUpTimeout;
        }

        public void setExposedServices(List<ExposedService> services) {
            this.exposedServices = services;
        }

        public List<ExposedService> getExposedServices() {
            if (exposedServices == null) {
                exposedServices = new ArrayList<>();
            }
            return exposedServices;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "waitFor",
    })
    public static class ExposedService {

        @XmlAttribute
        protected String name;

        @XmlAttribute
        protected int port;

        @XmlElement(name = "wait-for")
        protected WaitFor waitFor;

        public String getName() {
            return name;
        }

        public void setName(String value) {
            this.name = value;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public WaitFor getWaitFor() {
            return waitFor;
        }

        public void setWaitFor(WaitFor waitFor) {
            this.waitFor = waitFor;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class WaitFor {

            @XmlAttribute(name = "log-message")
            private String logMessage;

            @XmlAttribute
            private String url;

            @XmlAttribute
            private boolean disabled;

            public String getLogMessage() {
                return logMessage;
            }

            public void setLogMessage(String logMessage) {
                this.logMessage = logMessage;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public boolean isDisabled() {
                return disabled;
            }

            public void setDisabled(boolean disabled) {
                this.disabled = disabled;
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Down {

        @XmlAttribute
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
