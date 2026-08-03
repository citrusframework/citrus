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

package org.citrusframework.message;

import org.citrusframework.message.processor.DelegatingVariableExtractorBuilder;
import org.citrusframework.message.processor.camel.CamelMessageProcessors;
import org.citrusframework.message.processor.json.JsonMappingValidationProcessorBuilder;
import org.citrusframework.message.processor.json.JsonFormattingMessageProcessorBuilder;
import org.citrusframework.message.processor.json.JsonMarshallingMessageProcessorBuilder;
import org.citrusframework.message.processor.json.JsonMessageProcessors;
import org.citrusframework.message.processor.json.JsonPathMessageProcessorBuilder;
import org.citrusframework.message.processor.json.JsonUnmarshallingMessageProcessorBuilder;
import org.citrusframework.message.processor.xml.SoapFormattingMessageProcessorBuilder;
import org.citrusframework.message.processor.xml.SoapMessageProcessors;
import org.citrusframework.message.processor.xml.XmlFormattingMessageProcessorBuilder;
import org.citrusframework.message.processor.xml.XmlMarshallingMessageProcessorBuilder;
import org.citrusframework.message.processor.xml.XmlMarshallingValidationProcessorBuilder;
import org.citrusframework.message.processor.xml.XmlMessageProcessors;
import org.citrusframework.message.processor.xml.XmlUnmarshallingMessageProcessorBuilder;
import org.citrusframework.message.processor.xml.XpathMessageProcessorBuilder;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.validation.GenericValidationProcessor;
import org.citrusframework.validation.interceptor.BinaryMessageProcessor;
import org.citrusframework.validation.interceptor.GzipMessageProcessor;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.citrusframework.variable.json.JsonPathVariableExtractorBuilder;
import org.citrusframework.variable.xml.XpathPayloadVariableExtractorBuilder;

/**
 * Interface combines default implementations with domain specific language methods for all message processors
 * available in Citrus.
 */
public interface MessageProcessorSupport extends Processors, MessageProcessorLookupSupport {

    @Override
    default BinaryMessageProcessor.Builder toBinary() {
        return new BinaryMessageProcessor.Builder();
    }

    @Override
    default DelegatingPathExpressionProcessor.Builder path() {
        return new DelegatingPathExpressionProcessor.Builder();
    }

    @Override
    default DelegatingVariableExtractorBuilder extract() {
        return new DelegatingVariableExtractorBuilder() {
            @Override
            public DelegatingPayloadVariableExtractor.Builder fromBody() {
                return new DelegatingPayloadVariableExtractor.Builder();
            }

            @Override
            public MessageHeaderVariableExtractor.Builder fromHeaders() {
                return new MessageHeaderVariableExtractor.Builder();
            }
        };
    }

    @Override
    default GzipMessageProcessor.Builder toGzip() {
        return new GzipMessageProcessor.Builder();
    }

    @Override
    default CamelMessageProcessors camel() {
        return lookup("camel");
    }

    @Override
    default JsonMessageProcessors json() {
        return new JsonMessageProcessors() {
            @Override
            public JsonPathMessageProcessorBuilder<?, ?> jsonPath() {
                return lookup("jsonPath");
            }

            @Override
            public JsonPathVariableExtractorBuilder<?, ?> extract() {
                return lookup("jsonExtract");
            }

            @Override
            public <T> JsonMappingValidationProcessorBuilder<T, ?, ?> validate(Class<T> type) {
                return lookup("jsonValidate", type);
            }

            @Override
            public JsonMarshallingMessageProcessorBuilder<?, ?> marshal() {
                return lookup("jsonMarshal");
            }

            @Override
            public MessagePayloadBuilder marshal(Object model) {
                return lookupPayloadBuilder("jsonMarshal", model);
            }

            @Override
            public MessagePayloadBuilder marshal(Object model, Object mapper) {
                return lookupPayloadBuilder("jsonMarshal", model, mapper);
            }

            @Override
            public JsonUnmarshallingMessageProcessorBuilder<?, ?> unmarshal() {
                return lookup("jsonUnmarshal");
            }

            @Override
            public JsonFormattingMessageProcessorBuilder<?, ?> prettyPrint() {
                return lookup("jsonPrettyPrint");
            }
        };
    }

    @Override
    default XmlMessageProcessors xml() {
        return new XmlMessageProcessors() {

            @Override
            public XpathMessageProcessorBuilder<?, ?> xpath() {
                return lookup("xpath");
            }

            @Override
            public XpathPayloadVariableExtractorBuilder<?, ?> extract() {
                return lookup("xpathExtract");
            }

            @Override
            public <T> XmlMarshallingValidationProcessorBuilder<T, ?, ?> validate(GenericValidationProcessor<T> validationProcessor) {
                return lookup("xmlValidate", validationProcessor);
            }

            @Override
            public XmlMarshallingMessageProcessorBuilder<?, ?> marshal() {
                return lookup("xmlMarshal");
            }

            @Override
            public MessagePayloadBuilder marshal(Object model) {
                return lookupPayloadBuilder("xmlMarshal", model);
            }

            @Override
            public MessagePayloadBuilder marshal(Object model, Object marshaller) {
                return lookupPayloadBuilder("xmlMarshal", model, marshaller);
            }

            @Override
            public XmlUnmarshallingMessageProcessorBuilder<?, ?> unmarshal() {
                return lookup("xmlUnmarshal");
            }

            @Override
            public XmlFormattingMessageProcessorBuilder<?, ?> prettyPrint() {
                return lookup("xmlPrettyPrint");
            }

            @Override
            public SoapMessageProcessors soap() {
                return new SoapMessageProcessors() {
                    @Override
                    public SoapFormattingMessageProcessorBuilder<?, ?> prettyPrint() {
                        return lookup("soapPrettyPrint");
                    }
                };
            }
        };
    }

}
