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
package org.citrusframework.grpc.xml;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.grpc.actions.GrpcActionBuilder;
import org.citrusframework.grpc.actions.GrpcClientActionBuilder;
import org.citrusframework.grpc.actions.GrpcClientRequestActionBuilder;
import org.citrusframework.grpc.actions.GrpcClientResponseActionBuilder;
import org.citrusframework.grpc.actions.GrpcServerActionBuilder;
import org.citrusframework.grpc.actions.GrpcServerRequestActionBuilder;
import org.citrusframework.grpc.actions.GrpcServerResponseActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.xml.actions.Message;
import org.citrusframework.xml.actions.Receive;
import org.citrusframework.xml.actions.Send;

/**
 * XML DSL of the gRPC test actions: {@code <grpc client="...">} calls a service,
 * {@code <grpc server="...">} simulates one.
 */
@XmlRootElement(name = "grpc")
public class Grpc implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private final GrpcActionBuilder actionBuilder = new GrpcActionBuilder();

    private Object builder;
    private Send send;
    private Receive receive;

    private String description;
    private String actor;
    private ReferenceResolver referenceResolver;

    @XmlElement
    public void setDescription(String value) {
        this.description = value;
    }

    @XmlAttribute(name = "actor")
    public void setActor(String actor) {
        this.actor = actor;
    }

    @XmlAttribute(name = "client")
    public void setClient(String client) {
        builder = actionBuilder.client(client);
    }

    @XmlAttribute(name = "server")
    public void setServer(String server) {
        builder = actionBuilder.server(server);
    }

    @XmlElement(name = "send")
    public void setSend(SendElement element) {
        if (builder instanceof GrpcClientActionBuilder clientBuilder) {
            GrpcClientRequestActionBuilder requestBuilder = clientBuilder.send();
            requestBuilder.description(description);

            if (element.call != null) {
                requestBuilder.call(trim(element.call));
            }
            element.metadata.forEach(metadata -> requestBuilder.metadata(metadata.name, metadata.value));
            if (element.deadline != null) {
                requestBuilder.deadline(Duration.ofMillis(element.deadline));
            }

            send = new Send(requestBuilder) {
                @Override
                protected SendMessageAction doBuild() {
                    return null;
                }
            };

            if (element.message != null) {
                send.setMessage(element.message);
            }
            if (element.fork != null) {
                send.setFork(element.fork);
            }
            if (element.extract != null) {
                send.setExtract(element.extract);
            }

            builder = requestBuilder;
            return;
        }

        if (builder instanceof GrpcServerActionBuilder serverBuilder) {
            GrpcServerResponseActionBuilder responseBuilder = serverBuilder.send();
            responseBuilder.description(description);
            applyResponse(responseBuilder, element);

            send = new Send(responseBuilder) {
                @Override
                protected SendMessageAction doBuild() {
                    return null;
                }
            };

            if (element.message != null) {
                send.setMessage(element.message);
            }
            if (element.extract != null) {
                send.setExtract(element.extract);
            }

            builder = responseBuilder;
            return;
        }

        throw new CitrusRuntimeException("Missing gRPC client or server action - set the 'client' or 'server' attribute");
    }

    @XmlElement(name = "receive")
    public void setReceive(ReceiveElement element) {
        if (builder instanceof GrpcClientActionBuilder clientBuilder) {
            GrpcClientResponseActionBuilder responseBuilder = clientBuilder.receive();
            responseBuilder.description(description);
            applyResponse(responseBuilder, element);

            if (element.statusDescription != null) {
                responseBuilder.statusDescription(trim(element.statusDescription));
            }
            element.trailers.forEach(trailer -> responseBuilder.trailer(trailer.name, trailer.value));

            receive = receive(responseBuilder, element);
            builder = responseBuilder;
            return;
        }

        if (builder instanceof GrpcServerActionBuilder serverBuilder) {
            GrpcServerRequestActionBuilder requestBuilder = serverBuilder.receive();
            requestBuilder.description(description);

            if (element.call != null) {
                requestBuilder.call(trim(element.call));
            }
            element.metadata.forEach(metadata -> requestBuilder.metadata(metadata.name, metadata.value));

            receive = receive(requestBuilder, element);
            builder = requestBuilder;
            return;
        }

        throw new CitrusRuntimeException("Missing gRPC client or server action - set the 'client' or 'server' attribute");
    }

    @XmlElement(name = "cancel")
    public void setCancel(Cancel cancel) {
        builder = asClientBuilder().cancel();
    }

    @Override
    public TestAction build() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing gRPC client or server action - please provide proper action details");
        }

        if (send != null) {
            send.setReferenceResolver(referenceResolver);
            send.setActor(actor);
            send.build();
        }

        if (receive != null) {
            receive.setReferenceResolver(referenceResolver);
            receive.setActor(actor);
            receive.build();
        }

        if (builder instanceof ReferenceResolverAware referenceResolverAware) {
            referenceResolverAware.setReferenceResolver(referenceResolver);
        }

        return ((TestActionBuilder<?>) builder).build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    private static void applyResponse(GrpcClientResponseActionBuilder builder, SendElement element) {
        if (element.response != null) {
            builder.response(status(element.response.status));
        } else if (element.streamMessage != null) {
            builder.streamMessage();
        } else if (element.streamCompleted != null) {
            builder.streamCompleted(status(element.streamCompleted.status));
        }
    }

    private static void applyResponse(GrpcServerResponseActionBuilder builder, SendElement element) {
        if (element.response != null) {
            builder.response(status(element.response.status));
        } else if (element.streamMessage != null) {
            builder.streamMessage();
        } else if (element.streamCompleted != null) {
            builder.streamCompleted(status(element.streamCompleted.status));
        }

        if (element.statusDescription != null) {
            builder.statusDescription(trim(element.statusDescription));
        }
        element.trailers.forEach(trailer -> builder.trailer(trailer.name, trailer.value));
    }

    private static void applyResponse(GrpcClientResponseActionBuilder builder, ReceiveElement element) {
        if (element.response != null) {
            builder.response(status(element.response.status));
        } else if (element.streamMessage != null) {
            builder.streamMessage();
        } else if (element.streamCompleted != null) {
            builder.streamCompleted(status(element.streamCompleted.status));
        }
    }

    private static Receive receive(ReceiveMessageAction.ReceiveMessageActionBuilder<?, ?, ?> receiveBuilder,
                                   ReceiveElement element) {
        Receive receive = new Receive(receiveBuilder) {
            @Override
            protected ReceiveMessageAction doBuild() {
                return null;
            }
        };

        if (element.message != null) {
            receive.setMessage(element.message);
        }
        if (element.timeout != null) {
            receive.setTimeout(element.timeout);
        }
        if (element.selector != null) {
            receive.setSelect(element.selector);
        }
        receive.setValidator(element.validator);
        receive.setValidators(element.validators);
        receive.setHeaderValidator(element.headerValidator);
        receive.setHeaderValidators(element.headerValidators);
        element.getValidates().forEach(receive.getValidates()::add);
        if (element.extract != null) {
            receive.setExtract(element.extract);
        }

        return receive;
    }

    private static String status(String status) {
        return status == null || status.isBlank() ? "OK" : status;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private GrpcClientActionBuilder asClientBuilder() {
        if (builder instanceof GrpcClientActionBuilder clientBuilder) {
            return clientBuilder;
        }

        throw new CitrusRuntimeException("Failed to convert '%s' to gRPC client action builder - set the 'client' attribute"
                .formatted(Optional.ofNullable(builder).map(Object::getClass).map(Class::getName).orElse("null")));
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class SendElement {
        @XmlElement(name = "call")
        protected String call;

        @XmlElement(name = "metadata")
        protected List<Metadata> metadata = new ArrayList<>();

        @XmlElement(name = "deadline")
        protected Long deadline;

        @XmlElement(name = "response")
        protected Status response;

        @XmlElement(name = "stream-message")
        protected Empty streamMessage;

        @XmlElement(name = "stream-completed")
        protected Status streamCompleted;

        @XmlElement(name = "status-description")
        protected String statusDescription;

        @XmlElement(name = "trailer")
        protected List<Metadata> trailers = new ArrayList<>();

        @XmlElement(name = "message")
        protected Message message;

        @XmlElement(name = "extract")
        protected Message.Extract extract;

        @XmlAttribute
        protected Boolean fork;

        @XmlAttribute
        protected String uri;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ReceiveElement {
        @XmlElement(name = "call")
        protected String call;

        @XmlElement(name = "metadata")
        protected List<Metadata> metadata = new ArrayList<>();

        @XmlElement(name = "response")
        protected Status response;

        @XmlElement(name = "stream-message")
        protected Empty streamMessage;

        @XmlElement(name = "stream-completed")
        protected Status streamCompleted;

        @XmlElement(name = "status-description")
        protected String statusDescription;

        @XmlElement(name = "trailer")
        protected List<Metadata> trailers = new ArrayList<>();

        @XmlElement(name = "message")
        protected Message message;

        @XmlElement(name = "validate")
        protected List<Receive.Validate> validates = new ArrayList<>();

        @XmlElement(name = "extract")
        protected Message.Extract extract;

        @XmlAttribute
        protected Integer timeout;

        @XmlAttribute
        protected String selector;

        @XmlAttribute
        protected String validator;

        @XmlAttribute
        protected String validators;

        @XmlAttribute(name = "header-validator")
        protected String headerValidator;

        @XmlAttribute(name = "header-validators")
        protected String headerValidators;

        public List<Receive.Validate> getValidates() {
            return validates;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Metadata {
        @XmlAttribute(required = true)
        protected String name;

        @XmlValue
        protected String value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Status {
        @XmlAttribute
        protected String status;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Empty {
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Cancel {
    }
}
