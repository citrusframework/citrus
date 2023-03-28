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

package org.citrusframework.camel.message.format;

import java.util.Map;

import org.citrusframework.camel.dsl.CamelContextAware;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.DataFormatClause;
import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.model.ProcessDefinition;
import org.apache.camel.model.dataformat.BindyType;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.dataformat.YAMLLibrary;
import org.apache.camel.support.jsse.KeyStoreParameters;

/**
 * @author Christoph Deppisch
 */
public class DataFormatClauseSupport<T> {

    private DataFormatDefinition dataFormat;
    private boolean allowNullBody;
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

    public T jacksonXml() {
        delegate.jacksonXml();
        return result;
    }

    public T jacksonXml(Class<?> unmarshalType) {
        delegate.jacksonXml(unmarshalType);
        return result;
    }

    public T jacksonXml(Class<?> unmarshalType, Class<?> jsonView) {
        delegate.jacksonXml(unmarshalType, jsonView);
        return result;
    }

    public T jacksonXml(boolean prettyPrint) {
        delegate.jacksonXml(prettyPrint);
        return result;
    }

    public T jacksonXml(Class<?> unmarshalType, boolean prettyPrint) {
        delegate.jacksonXml(unmarshalType, prettyPrint);
        return result;
    }

    public T jacksonXml(Class<?> unmarshalType, Class<?> jsonView, boolean prettyPrint) {
        delegate.jacksonXml(unmarshalType, jsonView, prettyPrint);
        return result;
    }

    public T jacksonXml(Class<?> unmarshalType, Class<?> jsonView, String include) {
        delegate.jacksonXml(unmarshalType, jsonView, include);
        return result;
    }

    public T jacksonXml(Class<?> unmarshalType, Class<?> jsonView, String include, boolean prettyPrint) {
        delegate.jacksonXml(unmarshalType, jsonView, include, prettyPrint);
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

    public T soap() {
        delegate.soap();
        return result;
    }

    public T soap(String contextPath) {
        delegate.soap(contextPath);
        return result;
    }

    public T soap(String contextPath, String elementNameStrategyRef) {
        delegate.soap(contextPath, elementNameStrategyRef);
        return result;
    }

    public T soap(String contextPath, Object elementNameStrategy) {
        delegate.soap(contextPath, elementNameStrategy);
        return result;
    }

    public T soap12() {
        delegate.soap12();
        return result;
    }

    public T soap12(String contextPath) {
        delegate.soap12(contextPath);
        return result;
    }

    public T soap12(String contextPath, String elementNameStrategyRef) {
        delegate.soap12(contextPath, elementNameStrategyRef);
        return result;
    }

    public T soap12(String contextPath, Object elementNameStrategy) {
        delegate.soap12(contextPath, elementNameStrategy);
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

    public T yaml(YAMLLibrary library) {
        delegate.yaml(library);
        return result;
    }

    public T yaml(YAMLLibrary library, Class<?> type) {
        delegate.yaml(library, type);
        return result;
    }

    public T xmlSecurity(byte[] passPhraseByte) {
        delegate.xmlSecurity(passPhraseByte);
        return result;
    }

    public T xmlSecurity(String secureTag, boolean secureTagContents, String passPhrase) {
        delegate.xmlSecurity(secureTag, secureTagContents, passPhrase);
        return result;
    }

    public T xmlSecurity(String secureTag, Map<String, String> namespaces, boolean secureTagContents, String passPhrase) {
        delegate.xmlSecurity(secureTag, namespaces, secureTagContents, passPhrase);
        return result;
    }

    public T xmlSecurity(String secureTag, boolean secureTagContents, String passPhrase, String xmlCipherAlgorithm) {
        delegate.xmlSecurity(secureTag, secureTagContents, passPhrase, xmlCipherAlgorithm);
        return result;
    }

    public T xmlSecurity(
            String secureTag, Map<String, String> namespaces, boolean secureTagContents, String passPhrase,
            String xmlCipherAlgorithm) {
        delegate.xmlSecurity(secureTag, namespaces, secureTagContents, passPhrase, xmlCipherAlgorithm);
        return result;
    }

    public T xmlSecurity(String secureTag, boolean secureTagContents, byte[] passPhraseByte) {
        delegate.xmlSecurity(secureTag, secureTagContents, passPhraseByte);
        return result;
    }

    public T xmlSecurity(String secureTag, Map<String, String> namespaces, boolean secureTagContents, byte[] passPhraseByte) {
        delegate.xmlSecurity(secureTag, namespaces, secureTagContents, passPhraseByte);
        return result;
    }

    public T xmlSecurity(String secureTag, boolean secureTagContents, byte[] passPhraseByte, String xmlCipherAlgorithm) {
        delegate.xmlSecurity(secureTag, secureTagContents, passPhraseByte, xmlCipherAlgorithm);
        return result;
    }

    public T xmlSecurity(
            String secureTag, Map<String, String> namespaces, boolean secureTagContents, byte[] passPhraseByte,
            String xmlCipherAlgorithm) {
        delegate.xmlSecurity(secureTag, namespaces, secureTagContents, passPhraseByte, xmlCipherAlgorithm);
        return result;
    }

    public T xmlSecurity(
            String secureTag, boolean secureTagContents, String recipientKeyAlias, String xmlCipherAlgorithm,
            String keyCipherAlgorithm,
            String keyOrTrustStoreParametersId) {
        delegate.xmlSecurity(secureTag, secureTagContents, recipientKeyAlias,
                xmlCipherAlgorithm, keyCipherAlgorithm, keyOrTrustStoreParametersId);
        return result;
    }

    public T xmlSecurity(
            String secureTag, boolean secureTagContents, String recipientKeyAlias, String xmlCipherAlgorithm,
            String keyCipherAlgorithm,
            String keyOrTrustStoreParametersId, String keyPassword) {
        delegate.xmlSecurity(secureTag, secureTagContents, recipientKeyAlias,
                xmlCipherAlgorithm, keyCipherAlgorithm, keyOrTrustStoreParametersId, keyPassword);
        return result;
    }

    public T xmlSecurity(
            String secureTag, boolean secureTagContents, String recipientKeyAlias, String xmlCipherAlgorithm,
            String keyCipherAlgorithm,
            KeyStoreParameters keyOrTrustStoreParameters) {
        delegate.xmlSecurity(secureTag, secureTagContents, recipientKeyAlias,
                xmlCipherAlgorithm, keyCipherAlgorithm, keyOrTrustStoreParameters);
        return result;
    }

    public T xmlSecurity(
            String secureTag, boolean secureTagContents, String recipientKeyAlias, String xmlCipherAlgorithm,
            String keyCipherAlgorithm,
            KeyStoreParameters keyOrTrustStoreParameters, String keyPassword) {
        delegate.xmlSecurity(secureTag, secureTagContents, recipientKeyAlias,
                xmlCipherAlgorithm, keyCipherAlgorithm, keyOrTrustStoreParameters, keyPassword);
        return result;
    }

    public T xmlSecurity(
            String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
            String xmlCipherAlgorithm, String keyCipherAlgorithm,
            String keyOrTrustStoreParametersId) {
        delegate.xmlSecurity(secureTag, namespaces, secureTagContents, recipientKeyAlias,
                xmlCipherAlgorithm, keyCipherAlgorithm, keyOrTrustStoreParametersId);
        return result;
    }

    public T xmlSecurity(
            String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
            String xmlCipherAlgorithm, String keyCipherAlgorithm,
            String keyOrTrustStoreParametersId, String keyPassword) {
        delegate.xmlSecurity(secureTag, namespaces, secureTagContents, recipientKeyAlias,
                xmlCipherAlgorithm, keyCipherAlgorithm, keyOrTrustStoreParametersId, keyPassword);
        return result;
    }

    public T xmlSecurity(
            String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
            String xmlCipherAlgorithm, String keyCipherAlgorithm,
            KeyStoreParameters keyOrTrustStoreParameters) {
        delegate.xmlSecurity(secureTag, namespaces, secureTagContents, recipientKeyAlias,
                xmlCipherAlgorithm, keyCipherAlgorithm, keyOrTrustStoreParameters);
        return result;
    }

    public T xmlSecurity(
            String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
            String xmlCipherAlgorithm, String keyCipherAlgorithm,
            KeyStoreParameters keyOrTrustStoreParameters, String keyPassword) {
        delegate.xmlSecurity(secureTag, namespaces, secureTagContents, recipientKeyAlias,
                xmlCipherAlgorithm, keyCipherAlgorithm, keyOrTrustStoreParameters, keyPassword);
        return result;
    }

    public T xmlSecurity(
            String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
            String xmlCipherAlgorithm, String keyCipherAlgorithm,
            KeyStoreParameters keyOrTrustStoreParameters, String keyPassword, String digestAlgorithm) {
        delegate.xmlSecurity(secureTag, namespaces, secureTagContents, recipientKeyAlias, xmlCipherAlgorithm,
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

    public T allowNullBody() {
        delegate.allowNullBody();
        return result;
    }

    public T allowNullBody(boolean allowNullBody) {
        this.allowNullBody = allowNullBody;
        delegate.allowNullBody(allowNullBody);
        return result;
    }

    public T swiftMx() {
        delegate.swiftMx();
        return result;
    }

    public T swiftMx(boolean writeInJson) {
        delegate.swiftMx(writeInJson);
        return result;
    }

    public T swiftMx(boolean writeInJson, String readMessageId, Object readConfig) {
        delegate.swiftMx(writeInJson, readMessageId, readConfig);
        return result;
    }

    public T swiftMx(boolean writeInJson, String readMessageId, String readConfigRef) {
        delegate.swiftMx(writeInJson, readMessageId, readConfigRef);
        return result;
    }

    public T swiftMx(Object writeConfig, String readMessageId, Object readConfig) {
        delegate.swiftMx(writeConfig, readMessageId, readConfig);
        return result;
    }

    public T swiftMx(String writeConfigRef, String readMessageId, String readConfigRef) {
        delegate.swiftMx(writeConfigRef, readMessageId, readConfigRef);
        return result;
    }

    public T swiftMt() {
        delegate.swiftMt();
        return result;
    }

    public T swiftMt(boolean writeInJson) {
        delegate.swiftMt(writeInJson);
        return result;
    }

    public class InlineProcessDefinition extends ProcessDefinition {
        @Override
        public ProcessDefinition marshal(DataFormatDefinition dataFormatType) {
            dataFormat = dataFormatType;
            return this;
        }

        @Override
        public ProcessDefinition unmarshal(DataFormatDefinition dataFormatType, boolean allowNullBody) {
            dataFormat = dataFormatType;
            DataFormatClauseSupport.this.allowNullBody = allowNullBody;
            return this;
        }
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }

    public DataFormatDefinition getDataFormat() {
        return dataFormat;
    }

    public boolean isAllowNullBody() {
        return allowNullBody;
    }
}
