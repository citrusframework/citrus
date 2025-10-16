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

package org.citrusframework.message.processor.camel;

import java.util.Map;

import org.citrusframework.message.MessageProcessor;

public interface CamelDataFormatClause<T extends MessageProcessor.Builder<?, ?>, S> {

    T avro();

    /**
     * Uses Avro data format with tje given library and schema
     */
    T avro(String library, Object schema);

    /**
     * Uses Avro data format with the given unmarshalType
     */
    T avro(String unmarshalTypeName);

    /**
     * Uses Avro data format with given library and unmarshalType
     */
    T avro(String library, String unmarshalTypeName);

    /**
     * Uses Avro data format with library
     */
    T avro(Object library);

    /**
     * Uses the Avro data format with given unmarshalType
     */
    T avro(Class<?> unmarshalType);

    /**
     * Uses the Avro data format with given library and unmarshalType
     */
    T avro(String library, Class<?> unmarshalType);

    /**
     * Uses the Avro data format with given unmarshalType and schemaResolver
     */
    T avro(Class<?> unmarshalType, String schemaResolver);

    /**
     * Uses the Avro data format with given library, unmarshalType and schemaResolver
     */
    T avro(String library, Class<?> unmarshalType, String schemaResolver);

    /**
     * Uses the base64 data format
     */
    T base64();

    /**
     * Uses the base64 data format
     */
    T base64(int lineLength, String lineSeparator, boolean urlSafe);

    /**
     * Uses the beanio data format
     */
    T beanio(String mapping, String streamName);

    /**
     * Uses the beanio data format
     */
    T beanio(String mapping, String streamName, String encoding);

    /**
     * Uses the beanio data format
     */
    T beanio(String mapping, String streamName, String encoding, boolean ignoreUnidentifiedRecords,
             boolean ignoreUnexpectedRecords, boolean ignoreInvalidRecords);

    /**
     * Uses the beanio data format
     */
    T beanio(String mapping, String streamName, String encoding, String beanReaderErrorHandlerType);

    /**
     * Uses the Bindy data format
     *
     * @param type      the type of bindy data format to use
     * @param classType the POJO class type
     */
    T bindy(String type, Class<?> classType);

    /**
     * Uses the Bindy data format
     *
     * @param type                 the type of bindy data format to use
     * @param classType            the POJO class type
     * @param unwrapSingleInstance whether unmarshal should unwrap if there is a single instance in the result
     */
    T bindy(String type, Class<?> classType, boolean unwrapSingleInstance);

    /**
     * Uses the CBOR data format
     */
    T cbor();

    /**
     * Uses the CBOR data format
     *
     * @param unmarshalType unmarshal type for cbor type
     */
    T cbor(Class<?> unmarshalType);

    /**
     * Uses the CSV data format
     */
    T csv();

    /**
     * Uses the CSV data format for a huge file. Sequential access through an iterator.
     */
    T csvLazyLoad();

    /**
     * Uses the custom data format
     */
    T custom(String ref);

    /**
     * Uses the DFDL data format
     */
    T dfdl(String schemaUri);

    /**
     * Use the Fory data format
     */
    T fory();

    /**
     * Use the Fory data format with the given unmarshalType
     */

    T fory(Class type);

    /**
     * Uses the Grok data format
     */
    T grok(String pattern);

    /**
     * Uses the GZIP deflater data format
     */
    T gzipDeflater();

    /**
     * Uses the HL7 data format
     */
    T hl7();

    /**
     * Uses the HL7 data format
     */
    T hl7(boolean validate);

    /**
     * Uses the iCal data format
     */
    T ical(boolean validating);

    /**
     * Uses the LZF deflater data format
     */
    T lzf();

    /**
     * Uses the MIME Multipart data format
     */
    T mimeMultipart();

    /**
     * Uses the MIME Multipart data format
     *
     * @param multipartSubType Specifies the subtype of the MIME Multipart
     */
    T mimeMultipart(String multipartSubType);

    /**
     * Uses the MIME Multipart data format
     *
     * @param multipartSubType           the subtype of the MIME Multipart
     * @param multipartWithoutAttachment defines whether a message without attachment is also marshaled into a MIME
     *                                   Multipart (with only one body part).
     * @param headersInline              define the MIME Multipart headers as part of the message body or as Camel
     *                                   headers
     * @param binaryContent              have binary encoding for binary content (true) or use Base-64 encoding for
     *                                   binary content (false)
     */
    T mimeMultipart(String multipartSubType, boolean multipartWithoutAttachment, boolean headersInline, boolean binaryContent);

    /**
     * Uses the MIME Multipart data format
     *
     * @param multipartSubType           the subtype of the MIME Multipart
     * @param multipartWithoutAttachment defines whether a message without attachment is also marshaled into a MIME
     *                                   Multipart (with only one body part).
     * @param headersInline              define the MIME Multipart headers as part of the message body or as Camel
     *                                   headers
     * @param includeHeaders             if headersInline is set to true all camel headers matching this regex are also
     *                                   stored as MIME headers on the Multipart
     * @param binaryContent              have binary encoding for binary content (true) or use Base-64 encoding for
     *                                   binary content (false)
     */
    T mimeMultipart(String multipartSubType, boolean multipartWithoutAttachment, boolean headersInline,
                    String includeHeaders, boolean binaryContent);

    /**
     * Uses the MIME Multipart data format
     *
     * @param multipartWithoutAttachment defines whether a message without attachment is also marshaled into a MIME
     *                                   Multipart (with only one body part).
     * @param headersInline              define the MIME Multipart headers as part of the message body or as Camel
     *                                   headers
     * @param binaryContent              have binary encoding for binary content (true) or use Base-64 encoding for
     *                                   binary content (false)
     */
    T mimeMultipart(boolean multipartWithoutAttachment, boolean headersInline, boolean binaryContent);

    /**
     * Uses the PGP data format
     */
    T pgp(String keyFileName, String keyUserid);

    /**
     * Uses the PGP data format
     */
    T pgp(String keyFileName, String keyUserid, String password);

    /**
     * Uses the PGP data format
     */
    T pgp(String keyFileName, String keyUserid, String password, boolean armored, boolean integrity);

    /**
     * Uses the Jackson XML data format
     */
    T jacksonXml();

    /**
     * Uses the Jackson XML data format
     *
     * @param unmarshalType unmarshal type for xml jackson type
     */
    T jacksonXml(Class<?> unmarshalType);

    /**
     * Uses the Jackson XML data format
     *
     * @param unmarshalType unmarshal type for xml jackson type
     * @param jsonView      the view type for xml jackson type
     */
    T jacksonXml(Class<?> unmarshalType, Class<?> jsonView);

    /**
     * Uses the Jackson XML data format using the Jackson library turning pretty printing on or off
     *
     * @param prettyPrint turn pretty printing on or off
     */
    T jacksonXml(boolean prettyPrint);

    /**
     * Uses the Jackson XML data format
     *
     * @param unmarshalType unmarshal type for xml jackson type
     * @param prettyPrint   turn pretty printing on or off
     */
    T jacksonXml(Class<?> unmarshalType, boolean prettyPrint);

    /**
     * Uses the Jackson XML data format
     *
     * @param unmarshalType unmarshal type for xml jackson type
     * @param jsonView      the view type for xml jackson type
     * @param prettyPrint   turn pretty printing on or off
     */
    T jacksonXml(Class<?> unmarshalType, Class<?> jsonView, boolean prettyPrint);

    /**
     * Uses the Jackson XML data format
     *
     * @param unmarshalType unmarshal type for xml jackson type
     * @param jsonView      the view type for xml jackson type
     * @param include       include such as <tt>ALWAYS</tt>, <tt>NON_NULL</tt>, etc.
     */
    T jacksonXml(Class<?> unmarshalType, Class<?> jsonView, String include);

    /**
     * Uses the Jackson XML data format
     *
     * @param unmarshalType unmarshal type for xml jackson type
     * @param jsonView      the view type for xml jackson type
     * @param include       include such as <tt>ALWAYS</tt>, <tt>NON_NULL</tt>, etc.
     * @param prettyPrint   turn pretty printing on or off
     */
    T jacksonXml(Class<?> unmarshalType, Class<?> jsonView, String include, boolean prettyPrint);

    /**
     * Uses the JAXB data format
     */
    T jaxb();

    /**
     * Uses the JAXB data format with context path
     */
    T jaxb(String contextPath);

    /**
     * Uses the JAXB data format turning pretty printing on or off
     */
    T jaxb(boolean prettyPrint);

    /**
     * Uses the JSON data format using the Jackson library
     */
    T json();

    /**
     * Uses the JSON data format using the Jackson library turning pretty printing on or off
     *
     * @param prettyPrint turn pretty printing on or off
     */
    T json(boolean prettyPrint);

    /**
     * Uses the JSON data format
     *
     * @param library the json library to use
     */
    T json(String library);

    /**
     * Uses the JSON data format
     *
     * @param library     the json library to use
     * @param prettyPrint turn pretty printing on or off
     */
    T json(String library, boolean prettyPrint);

    /**
     * Uses the JSON data format
     *
     * @param type          the json type to use
     * @param unmarshalType unmarshal type for json jackson type
     */
    T json(String type, Class<?> unmarshalType);

    /**
     * Uses the JSON data format
     *
     * @param type          the json type to use
     * @param unmarshalType unmarshal type for json jackson type
     * @param prettyPrint   turn pretty printing on or off
     */
    T json(String type, Class<?> unmarshalType, boolean prettyPrint);

    /**
     * Uses the Jackson JSON data format
     *
     * @param unmarshalType unmarshal type for json jackson type
     */
    T json(Class<?> unmarshalType);

    /**
     * Uses the Jackson JSON data format
     *
     * @param unmarshalType unmarshal type for json jackson type
     * @param jsonView      the view type for json jackson type
     */
    T json(Class<?> unmarshalType, Class<?> jsonView);

    /**
     * Uses the Jackson JSON data format
     *
     * @param unmarshalType unmarshal type for json jackson type
     * @param jsonView      the view type for json jackson type
     * @param prettyPrint   turn pretty printing on or off
     */
    T json(Class<?> unmarshalType, Class<?> jsonView, boolean prettyPrint);

    /**
     * Uses the Jackson JSON data format
     *
     * @param unmarshalType unmarshal type for json jackson type
     * @param jsonView      the view type for json jackson type
     * @param include       include such as <tt>ALWAYS</tt>, <tt>NON_NULL</tt>, etc.
     */
    T json(Class<?> unmarshalType, Class<?> jsonView, String include);

    /**
     * Uses the Jackson JSON data format
     *
     * @param unmarshalType unmarshal type for json jackson type
     * @param jsonView      the view type for json jackson type
     * @param include       include such as <tt>ALWAYS</tt>, <tt>NON_NULL</tt>, etc.
     * @param prettyPrint   turn pretty printing on or off
     */
    T json(Class<?> unmarshalType, Class<?> jsonView, String include, boolean prettyPrint);

    /**
     * Uses the JSON API data format
     */
    T jsonApi();

    /**
     * Uses the protobuf data format
     */
    T protobuf();

    T protobuf(Object defaultInstance);

    T protobuf(Object defaultInstance, String contentTypeFormat);

    T protobuf(String instanceClassName, String contentTypeFormat);

    /**
     * Uses the Protobuf data format with given library or instance class name.
     */
    T protobuf(String libraryOrInstanceClassName);

    /**
     * Uses the Protobuf data format with given library and unmarshalType
     */
    T protobuf(String library, Class<?> unmarshalType);

    /**
     * Uses the Protobuf data format with given library, unmarshalType and schemaResolver
     */
    T protobuf(String library, Class<?> unmarshalType, String schemaResolver);

    /**
     * Uses the RSS data format
     */
    T rss();

    /**
     * Uses the Smooks data format
     */
    T smooks(String smooksConfig);

    /**
     * Uses the Soap v1.1 data format
     */
    T soap();

    /**
     * Uses the Soap v1.1 data format
     */
    T soap(String contextPath);

    /**
     * Uses the Soap v1.1 data format
     */
    T soap(String contextPath, String elementNameStrategyRef);

    /**
     * Uses the Soap v1.1 data format
     */
    T soap(String contextPath, Object elementNameStrategy);

    /**
     * Uses the Soap v1.2 data format
     */
    T soap12();

    /**
     * Uses the Soap v1.2 data format
     */
    T soap12(String contextPath);

    /**
     * Uses the Soap v1.2 data format
     */
    T soap12(String contextPath, String elementNameStrategyRef);

    /**
     * Uses the Soap v1.2 data format
     */
    T soap12(String contextPath, Object elementNameStrategy);

    /**
     * Uses the SWIFT MX data format
     */
    T swiftMx();

    /**
     * Uses the SWIFT MX data format.
     */
    T swiftMx(boolean writeInJson);

    /**
     * Uses the SWIFT MX data format.
     */
    T swiftMx(boolean writeInJson, String readMessageId, Object readConfig);

    /**
     * Uses the SWIFT MX data format.
     */
    T swiftMx(boolean writeInJson, String readMessageId, String readConfigRef);

    /**
     * Uses the SWIFT MX data format.
     */
    T swiftMx(Object writeConfig, String readMessageId, Object readConfig);

    /**
     * Uses the SWIFT MX data format.
     */
    T swiftMx(String writeConfigRef, String readMessageId, String readConfigRef);

    /**
     * Uses the SWIFT MT data format
     */
    T swiftMt();

    /**
     * Uses the SWIFT MT data format.
     */
    T swiftMt(boolean writeInJson);

    /**
     * Uses the Syslog data format
     */
    T syslog();

    /**
     * Uses the Thrift data format
     */
    T thrift();

    T thrift(Object defaultInstance);

    T thrift(Object defaultInstance, String contentTypeFormat);

    T thrift(String instanceClassName);

    T thrift(String instanceClassName, String contentTypeFormat);

    /**
     * Uses the YAML data format
     *
     * @param library the yaml library to use
     */
    T yaml(String library);

    /**
     * Uses the YAML data format
     *
     * @param library the yaml type to use
     * @param type    the type for json snakeyaml type
     */
    T yaml(String library, Class<?> type);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(byte[] passPhraseByte);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(String secureTag, boolean secureTagContents, String passPhrase);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(String secureTag, Map<String, String> namespaces, boolean secureTagContents, String passPhrase);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(String secureTag, boolean secureTagContents, String passPhrase, String xmlCipherAlgorithm);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(String secureTag, Map<String, String> namespaces, boolean secureTagContents, String passPhrase, String xmlCipherAlgorithm);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(String secureTag, boolean secureTagContents, byte[] passPhraseByte);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(String secureTag, Map<String, String> namespaces, boolean secureTagContents, byte[] passPhraseByte);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(String secureTag, boolean secureTagContents, byte[] passPhraseByte, String xmlCipherAlgorithm);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(String secureTag, Map<String, String> namespaces, boolean secureTagContents, byte[] passPhraseByte, String xmlCipherAlgorithm);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(String secureTag, boolean secureTagContents, String recipientKeyAlias, String xmlCipherAlgorithm,
                  String keyCipherAlgorithm, String keyOrTrustStoreParametersId);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(String secureTag, boolean secureTagContents, String recipientKeyAlias, String xmlCipherAlgorithm,
                  String keyCipherAlgorithm, String keyOrTrustStoreParametersId, String keyPassword);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(String secureTag, boolean secureTagContents, String recipientKeyAlias, String xmlCipherAlgorithm,
                  String keyCipherAlgorithm, Object keyOrTrustStoreParameters);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(String secureTag, boolean secureTagContents, String recipientKeyAlias, String xmlCipherAlgorithm,
                  String keyCipherAlgorithm, Object keyOrTrustStoreParameters, String keyPassword);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
                  String xmlCipherAlgorithm, String keyCipherAlgorithm, String keyOrTrustStoreParametersId);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
                  String xmlCipherAlgorithm, String keyCipherAlgorithm, String keyOrTrustStoreParametersId, String keyPassword);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
                  String xmlCipherAlgorithm, String keyCipherAlgorithm, Object keyOrTrustStoreParameters);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
                  String xmlCipherAlgorithm, String keyCipherAlgorithm, Object keyOrTrustStoreParameters, String keyPassword);

    /**
     * Uses the XML Security data format
     */
    T xmlSecurity(String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
                  String xmlCipherAlgorithm, String keyCipherAlgorithm, Object keyOrTrustStoreParameters, String keyPassword, String digestAlgorithm);

    /**
     * Uses the Tar file data format
     */
    T tarFile();

    /**
     * Uses the ZIP deflater data format
     */
    T zipDeflater();

    /**
     * Uses the ZIP deflater data format
     */
    T zipDeflater(int compressionLevel);

    /**
     * Uses the ZIP file data format
     */
    T zipFile();

    /**
     * Uses the ASN.1 file data format
     */
    T asn1();

    /**
     * Uses the ASN.1 file data format
     */
    T asn1(String unmarshalType);

    /**
     * Uses the ASN.1 file data format
     */
    T asn1(Class<?> unmarshalType);

    /**
     * Uses the ASN.1 file data format
     */
    T asn1(Boolean usingIterator);

    /**
     * Uses the parquet-avro file data format
     */
    T parquetAvro();

    /**
     * Uses the parquet-avro file data format
     */
    T parquetAvro(String unmarshalType);

    /**
     * Uses the parquet-avro file data format
     */
    T parquetAvro(Class<?> unmarshalType);

    /**
     * Uses the FHIR JSON data format
     */
    T fhirJson();

    T fhirJson(String version);

    T fhirJson(boolean prettyPrint);

    T fhirJson(String version, boolean prettyPrint);

    /**
     * Uses the FHIR XML data format
     */
    T fhirXml();

    T fhirXml(String version);

    T fhirXml(boolean prettyPrint);

    T fhirXml(String version, boolean prettyPrint);

    /**
     * Allows {@code null} as value of a body to unmarshall.
     *
     * @return the builder
     */
    S allowNullBody();

    /**
     * Indicates whether {@code null} is allowed as value of a body to unmarshall.
     *
     * @param  allowNullBody {@code true} if {@code null} is allowed as value of a body to unmarshall, {@code false}
     *                       otherwise
     * @return               the builder
     */
    S allowNullBody(boolean allowNullBody);

    /**
     * To use a variable as the source for the message body to send. This makes it handy to use variables for user data
     * and to easily control what data to use for sending and receiving. Important: When using send variable then the
     * message body is taken from this variable instead of the current Message , however the headers from the Message
     * will still be used as well. In other words, the variable is used instead of the message body, but everything else
     * is as usual.
     */
    S variableSend(String variableSend);
}
