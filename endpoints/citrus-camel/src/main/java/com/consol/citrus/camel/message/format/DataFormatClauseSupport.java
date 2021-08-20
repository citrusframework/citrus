/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.camel.message.format;

import java.util.List;
import java.util.Map;

import com.consol.citrus.camel.dsl.CamelContextAware;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.DataFormatClause;
import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.model.ProcessDefinition;
import org.apache.camel.model.dataformat.Any23Type;
import org.apache.camel.model.dataformat.BindyType;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.dataformat.YAMLLibrary;
import org.apache.camel.support.jsse.KeyStoreParameters;

/**
 * @author Christoph Deppisch
 */
public class DataFormatClauseSupport<T> {

    private DataFormatDefinition dataFormat;
    private final DataFormatClause<InlineProcessDefinition> delegate;
    private final T result;

    private CamelContext camelContext;

    public DataFormatClauseSupport(T result, DataFormatClause.Operation operation) {
        this.delegate = new DataFormatClause<>(new InlineProcessDefinition(), operation);
        this.result = result;
    }

    public DataFormatClauseSupport<T> camelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
        if (result instanceof CamelContextAware<?>) {
            ((CamelContextAware<?>) result).camelContext(camelContext);
        }
        return this;
    }

    public T any23(String baseuri) {
        delegate.any23(baseuri);
        return result;
    }

    public T any23(String baseuri, Any23Type outputformat) {
        delegate.any23(baseuri, outputformat);
        return result;
    }

    public T any23(String baseuri, Any23Type outputformat, Map<String, String> configurations) {
        delegate.any23(baseuri, outputformat, configurations);
        return result;
    }

    public T any23(String baseuri, Any23Type outputformat, Map<String, String> configurations, List<String> extractors) {
        delegate.any23(baseuri, outputformat, configurations, extractors);
        return result;
    }

    public T avro() {
        delegate.avro();
        return result;
    }

    public T avro(Object schema) {
        delegate.avro(schema);
        return result;
    }

    public T avro(String instanceClassName) {
        delegate.avro(instanceClassName);
        return result;
    }

    public T base64() {
        delegate.base64();
        return result;
    }

    public T base64(int lineLength, String lineSeparator, boolean urlSafe) {
        delegate.base64(lineLength, lineSeparator, urlSafe);
        return result;
    }

    public T beanio(String mapping, String streamName) {
        delegate.beanio(mapping, streamName);
        return result;
    }

    public T beanio(String mapping, String streamName, String encoding) {
        delegate.beanio(mapping, streamName, encoding);
        return result;
    }

    public T beanio(
            String mapping, String streamName, String encoding, boolean ignoreUnidentifiedRecords,
            boolean ignoreUnexpectedRecords, boolean ignoreInvalidRecords) {
        delegate.beanio(mapping, streamName, encoding, ignoreUnidentifiedRecords, ignoreUnexpectedRecords, ignoreInvalidRecords);
        return result;
    }

    public T beanio(String mapping, String streamName, String encoding, String beanReaderErrorHandlerType) {
        delegate.beanio(mapping, streamName, encoding, beanReaderErrorHandlerType);
        return result;
    }

    public T bindy(BindyType type, Class<?> classType) {
        delegate.bindy(type, classType);
        return result;
    }

    public T bindy(BindyType type, Class<?> classType, boolean unwrapSingleInstance) {
        delegate.bindy(type, classType, unwrapSingleInstance);
        return result;
    }

    public T cbor() {
        delegate.cbor();
        return result;
    }

    public T cbor(Class<?> unmarshalType) {
        delegate.cbor(unmarshalType);
        return result;
    }

    public T csv() {
        delegate.csv();
        return result;
    }

    public T csvLazyLoad() {
        delegate.csvLazyLoad();
        return result;
    }

    public T custom(String ref) {
        delegate.custom(ref);
        return result;
    }

    public T grok(String pattern) {
        delegate.grok(pattern);
        return result;
    }

    public T gzipDeflater() {
        delegate.gzipDeflater();
        return result;
    }

    public T hl7() {
        delegate.hl7();
        return result;
    }

    public T hl7(boolean validate) {
        delegate.hl7(validate);
        return result;
    }

    public T hl7(Object parser) {
        delegate.hl7(parser);
        return result;
    }

    public T ical(boolean validating) {
        delegate.ical(validating);
        return result;
    }

    public T lzf() {
        delegate.lzf();
        return result;
    }

    public T mimeMultipart() {
        delegate.mimeMultipart();
        return result;
    }

    public T mimeMultipart(String multipartSubType) {
        delegate.mimeMultipart(multipartSubType);
        return result;
    }

    public T mimeMultipart(
            String multipartSubType, boolean multipartWithoutAttachment, boolean headersInline, boolean binaryContent) {
        delegate.mimeMultipart(multipartSubType, multipartWithoutAttachment, headersInline, binaryContent);
        return result;
    }

    public T mimeMultipart(
            String multipartSubType, boolean multipartWithoutAttachment, boolean headersInline, String includeHeaders,
            boolean binaryContent) {
        delegate.mimeMultipart(multipartSubType, multipartWithoutAttachment, headersInline, includeHeaders, binaryContent);
        return result;
    }

    public T mimeMultipart(boolean multipartWithoutAttachment, boolean headersInline, boolean binaryContent) {
        delegate.mimeMultipart(multipartWithoutAttachment, headersInline, binaryContent);
        return result;
    }

    public T pgp(String keyFileName, String keyUserid) {
        delegate.pgp(keyFileName, keyUserid);
        return result;
    }

    public T pgp(String keyFileName, String keyUserid, String password) {
        delegate.pgp(keyFileName, keyUserid, password);
        return result;
    }

    public T pgp(String keyFileName, String keyUserid, String password, boolean armored, boolean integrity) {
        delegate.pgp(keyFileName, keyUserid, password, armored, integrity);
        return result;
    }

    public T jacksonxml() {
        delegate.jacksonxml();
        return result;
    }

    public T jacksonxml(Class<?> unmarshalType) {
        delegate.jacksonxml(unmarshalType);
        return result;
    }

    public T jacksonxml(Class<?> unmarshalType, Class<?> jsonView) {
        delegate.jacksonxml(unmarshalType, jsonView);
        return result;
    }

    public T jacksonxml(boolean prettyPrint) {
        delegate.jacksonxml(prettyPrint);
        return result;
    }

    public T jacksonxml(Class<?> unmarshalType, boolean prettyPrint) {
        delegate.jacksonxml(unmarshalType, prettyPrint);
        return result;
    }

    public T jacksonxml(Class<?> unmarshalType, Class<?> jsonView, boolean prettyPrint) {
        delegate.jacksonxml(unmarshalType, jsonView, prettyPrint);
        return result;
    }

    public T jacksonxml(Class<?> unmarshalType, Class<?> jsonView, String include) {
        delegate.jacksonxml(unmarshalType, jsonView, include);
        return result;
    }

    public T jacksonxml(Class<?> unmarshalType, Class<?> jsonView, String include, boolean prettyPrint) {
        delegate.jacksonxml(unmarshalType, jsonView, include, prettyPrint);
        return result;
    }

    public T jaxb() {
        delegate.jaxb();
        return result;
    }

    public T jaxb(String contextPath) {
        delegate.jaxb(contextPath);
        return result;
    }

    public T jaxb(boolean prettyPrint) {
        delegate.jaxb(prettyPrint);
        return result;
    }

    public T json() {
        delegate.json();
        return result;
    }

    public T json(boolean prettyPrint) {
        delegate.json(prettyPrint);
        return result;
    }

    public T json(JsonLibrary library) {
        delegate.json(library);
        return result;
    }

    public T json(JsonLibrary library, boolean prettyPrint) {
        delegate.json(library, prettyPrint);
        return result;
    }

    public T json(JsonLibrary type, Class<?> unmarshalType) {
        delegate.json(type, unmarshalType);
        return result;
    }

    public T json(JsonLibrary type, Class<?> unmarshalType, boolean prettyPrint) {
        delegate.json(type, unmarshalType, prettyPrint);
        return result;
    }

    public T json(Class<?> unmarshalType, Class<?> jsonView) {
        delegate.json(unmarshalType, jsonView);
        return result;
    }

    public T json(Class<?> unmarshalType, Class<?> jsonView, boolean prettyPrint) {
        delegate.json(unmarshalType, jsonView, prettyPrint);
        return result;
    }

    public T json(Class<?> unmarshalType, Class<?> jsonView, String include) {
        delegate.json(unmarshalType, jsonView, include);
        return result;
    }

    public T json(Class<?> unmarshalType, Class<?> jsonView, String include, boolean prettyPrint) {
        delegate.json(unmarshalType, jsonView, include, prettyPrint);
        return result;
    }

    public T jsonApi() {
        delegate.jsonApi();
        return result;
    }

    public T protobuf() {
        delegate.protobuf();
        return result;
    }

    public T protobuf(Object defaultInstance) {
        delegate.protobuf(defaultInstance);
        return result;
    }

    public T protobuf(Object defaultInstance, String contentTypeFormat) {
        delegate.protobuf(defaultInstance, contentTypeFormat);
        return result;
    }

    public T protobuf(String instanceClassName) {
        delegate.protobuf(instanceClassName);
        return result;
    }

    public T protobuf(String instanceClassName, String contentTypeFormat) {
        delegate.protobuf(instanceClassName, contentTypeFormat);
        return result;
    }

    public T rss() {
        delegate.rss();
        return result;
    }

    public T soapjaxb() {
        delegate.soapjaxb();
        return result;
    }

    public T soapjaxb(String contextPath) {
        delegate.soapjaxb(contextPath);
        return result;
    }

    public T soapjaxb(String contextPath, String elementNameStrategyRef) {
        delegate.soapjaxb(contextPath, elementNameStrategyRef);
        return result;
    }

    public T soapjaxb(String contextPath, Object elementNameStrategy) {
        delegate.soapjaxb(contextPath, elementNameStrategy);
        return result;
    }

    public T soapjaxb12() {
        delegate.soapjaxb12();
        return result;
    }

    public T soapjaxb12(String contextPath) {
        delegate.soapjaxb12(contextPath);
        return result;
    }

    public T soapjaxb12(String contextPath, String elementNameStrategyRef) {
        delegate.soapjaxb12(contextPath, elementNameStrategyRef);
        return result;
    }

    public T soapjaxb12(String contextPath, Object elementNameStrategy) {
        delegate.soapjaxb12(contextPath, elementNameStrategy);
        return result;
    }

    public T syslog() {
        delegate.syslog();
        return result;
    }

    public T thrift() {
        delegate.thrift();
        return result;
    }

    public T thrift(Object defaultInstance) {
        delegate.thrift(defaultInstance);
        return result;
    }

    public T thrift(Object defaultInstance, String contentTypeFormat) {
        delegate.thrift(defaultInstance, contentTypeFormat);
        return result;
    }

    public T thrift(String instanceClassName) {
        delegate.thrift(instanceClassName);
        return result;
    }

    public T thrift(String instanceClassName, String contentTypeFormat) {
        delegate.thrift(instanceClassName, contentTypeFormat);
        return result;
    }

    public T tidyMarkup(Class<?> dataObjectType) {
        delegate.tidyMarkup(dataObjectType);
        return result;
    }

    public T tidyMarkup() {
        delegate.tidyMarkup();
        return result;
    }

    public T xstream() {
        delegate.xstream();
        return result;
    }

    public T xstream(String encodingOrPermission) {
        delegate.xstream(encodingOrPermission);
        return result;
    }

    public T xstream(String encoding, String permission) {
        delegate.xstream(encoding, permission);
        return result;
    }

    public T xstream(Class<?> type) {
        delegate.xstream(type);
        return result;
    }

    public T xstream(String encoding, Class<?>... type) {
        delegate.xstream(encoding, type);
        return result;
    }

    public T yaml(YAMLLibrary library) {
        delegate.yaml(library);
        return result;
    }

    public T yaml(YAMLLibrary library, Class<?> type) {
        delegate.yaml(library, type);
        return result;
    }

    public T secureXML(byte[] passPhraseByte) {
        delegate.secureXML(passPhraseByte);
        return result;
    }

    public T secureXML(String secureTag, boolean secureTagContents, String passPhrase) {
        delegate.secureXML(secureTag, secureTagContents, passPhrase);
        return result;
    }

    public T secureXML(String secureTag, Map<String, String> namespaces, boolean secureTagContents, String passPhrase) {
        delegate.secureXML(secureTag, namespaces, secureTagContents, passPhrase);
        return result;
    }

    public T secureXML(String secureTag, boolean secureTagContents, String passPhrase, String xmlCipherAlgorithm) {
        delegate.secureXML(secureTag, secureTagContents, passPhrase, xmlCipherAlgorithm);
        return result;
    }

    public T secureXML(
            String secureTag, Map<String, String> namespaces, boolean secureTagContents, String passPhrase,
            String xmlCipherAlgorithm) {
        delegate.secureXML(secureTag, namespaces, secureTagContents, passPhrase, xmlCipherAlgorithm);
        return result;
    }

    public T secureXML(String secureTag, boolean secureTagContents, byte[] passPhraseByte) {
        delegate.secureXML(secureTag, secureTagContents, passPhraseByte);
        return result;
    }

    public T secureXML(String secureTag, Map<String, String> namespaces, boolean secureTagContents, byte[] passPhraseByte) {
        delegate.secureXML(secureTag, namespaces, secureTagContents, passPhraseByte);
        return result;
    }

    public T secureXML(String secureTag, boolean secureTagContents, byte[] passPhraseByte, String xmlCipherAlgorithm) {
        delegate.secureXML(secureTag, secureTagContents, passPhraseByte, xmlCipherAlgorithm);
        return result;
    }

    public T secureXML(
            String secureTag, Map<String, String> namespaces, boolean secureTagContents, byte[] passPhraseByte,
            String xmlCipherAlgorithm) {
        delegate.secureXML(secureTag, namespaces, secureTagContents, passPhraseByte, xmlCipherAlgorithm);
        return result;
    }

    public T secureXML(
            String secureTag, boolean secureTagContents, String recipientKeyAlias, String xmlCipherAlgorithm,
            String keyCipherAlgorithm,
            String keyOrTrustStoreParametersId) {
        delegate.secureXML(secureTag, secureTagContents, recipientKeyAlias,
                xmlCipherAlgorithm, keyCipherAlgorithm, keyOrTrustStoreParametersId);
        return result;
    }

    public T secureXML(
            String secureTag, boolean secureTagContents, String recipientKeyAlias, String xmlCipherAlgorithm,
            String keyCipherAlgorithm,
            String keyOrTrustStoreParametersId, String keyPassword) {
        delegate.secureXML(secureTag, secureTagContents, recipientKeyAlias,
                xmlCipherAlgorithm, keyCipherAlgorithm, keyOrTrustStoreParametersId, keyPassword);
        return result;
    }

    public T secureXML(
            String secureTag, boolean secureTagContents, String recipientKeyAlias, String xmlCipherAlgorithm,
            String keyCipherAlgorithm,
            KeyStoreParameters keyOrTrustStoreParameters) {
        delegate.secureXML(secureTag, secureTagContents, recipientKeyAlias,
                xmlCipherAlgorithm, keyCipherAlgorithm, keyOrTrustStoreParameters);
        return result;
    }

    public T secureXML(
            String secureTag, boolean secureTagContents, String recipientKeyAlias, String xmlCipherAlgorithm,
            String keyCipherAlgorithm,
            KeyStoreParameters keyOrTrustStoreParameters, String keyPassword) {
        delegate.secureXML(secureTag, secureTagContents, recipientKeyAlias,
                xmlCipherAlgorithm, keyCipherAlgorithm, keyOrTrustStoreParameters, keyPassword);
        return result;
    }

    public T secureXML(
            String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
            String xmlCipherAlgorithm, String keyCipherAlgorithm,
            String keyOrTrustStoreParametersId) {
        delegate.secureXML(secureTag, namespaces, secureTagContents, recipientKeyAlias,
                xmlCipherAlgorithm, keyCipherAlgorithm, keyOrTrustStoreParametersId);
        return result;
    }

    public T secureXML(
            String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
            String xmlCipherAlgorithm, String keyCipherAlgorithm,
            String keyOrTrustStoreParametersId, String keyPassword) {
        delegate.secureXML(secureTag, namespaces, secureTagContents, recipientKeyAlias,
                xmlCipherAlgorithm, keyCipherAlgorithm, keyOrTrustStoreParametersId, keyPassword);
        return result;
    }

    public T secureXML(
            String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
            String xmlCipherAlgorithm, String keyCipherAlgorithm,
            KeyStoreParameters keyOrTrustStoreParameters) {
        delegate.secureXML(secureTag, namespaces, secureTagContents, recipientKeyAlias,
                xmlCipherAlgorithm, keyCipherAlgorithm, keyOrTrustStoreParameters);
        return result;
    }

    public T secureXML(
            String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
            String xmlCipherAlgorithm, String keyCipherAlgorithm,
            KeyStoreParameters keyOrTrustStoreParameters, String keyPassword) {
        delegate.secureXML(secureTag, namespaces, secureTagContents, recipientKeyAlias,
                xmlCipherAlgorithm, keyCipherAlgorithm, keyOrTrustStoreParameters, keyPassword);
        return result;
    }

    public T secureXML(
            String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
            String xmlCipherAlgorithm, String keyCipherAlgorithm,
            KeyStoreParameters keyOrTrustStoreParameters, String keyPassword, String digestAlgorithm) {
        delegate.secureXML(secureTag, namespaces, secureTagContents, recipientKeyAlias, xmlCipherAlgorithm,
                keyCipherAlgorithm, keyOrTrustStoreParameters, keyPassword, digestAlgorithm);
        return result;
    }

    public T tarFile() {
        delegate.tarFile();
        return result;
    }

    public T zipDeflater() {
        delegate.zipDeflater();
        return result;
    }

    public T zipDeflater(int compressionLevel) {
        delegate.zipDeflater(compressionLevel);
        return result;
    }

    public T zipFile() {
        delegate.zipFile();
        return result;
    }

    public T asn1() {
        delegate.asn1();
        return result;
    }

    public T asn1(String clazzName) {
        delegate.asn1(clazzName);
        return result;
    }

    public T asn1(Boolean usingIterator) {
        delegate.asn1(usingIterator);
        return result;
    }

    public T fhirJson() {
        delegate.fhirJson();
        return result;
    }

    public T fhirJson(String version) {
        delegate.fhirJson(version);
        return result;
    }

    public T fhirJson(boolean prettyPrint) {
        delegate.fhirJson(prettyPrint);
        return result;
    }

    public T fhirJson(String version, boolean prettyPrint) {
        delegate.fhirJson(version, prettyPrint);
        return result;
    }

    /**
     * Uses the FHIR XML data format
     */
    public T fhirXml() {
        delegate.fhirXml();
        return result;
    }

    public T fhirXml(String version) {
        delegate.fhirXml(version);
        return result;
    }

    public T fhirXml(boolean prettyPrint) {
        delegate.fhirXml(prettyPrint);
        return result;
    }

    public T fhirXml(String version, boolean prettyPrint) {
        delegate.fhirXml(version, prettyPrint);
        return result;
    }

    public class InlineProcessDefinition extends ProcessDefinition {
        @Override
        public ProcessDefinition marshal(DataFormatDefinition dataFormatType) {
            dataFormat = dataFormatType;
            return this;
        }

        @Override
        public ProcessDefinition unmarshal(DataFormatDefinition dataFormatType) {
            dataFormat = dataFormatType;
            return this;
        }
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }

    public DataFormatDefinition getDataFormat() {
        return dataFormat;
    }
}
