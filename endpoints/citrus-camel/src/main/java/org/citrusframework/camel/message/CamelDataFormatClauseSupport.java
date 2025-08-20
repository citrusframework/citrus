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

package org.citrusframework.camel.message;

import java.util.Arrays;
import java.util.Map;

import org.apache.camel.builder.DataFormatClause;
import org.apache.camel.model.dataformat.AvroLibrary;
import org.apache.camel.model.dataformat.BindyType;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.dataformat.ProtobufLibrary;
import org.apache.camel.model.dataformat.YAMLLibrary;
import org.apache.camel.support.jsse.KeyStoreParameters;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.processor.camel.CamelDataFormatClause;

public class CamelDataFormatClauseSupport<T extends InlineProcessDefinition> extends DataFormatClause<T>
        implements CamelDataFormatClause<T, DataFormatClause<T>> {

    public CamelDataFormatClauseSupport(T processorType, Operation operation) {
        super(processorType, operation);
    }

    @Override
    public T avro(String library, Object schema) {
        return avro(Arrays.stream(AvroLibrary.values())
                .filter(lib -> lib.getDataFormatName().equals(library))
                .findFirst()
                .orElse(AvroLibrary.valueOf(library)), schema);
    }

    @Override
    public T avro(String library, String unmarshalTypeName) {
        return avro(Arrays.stream(AvroLibrary.values())
                .filter(lib -> lib.getDataFormatName().equals(library))
                .findFirst()
                .orElse(AvroLibrary.valueOf(library)), unmarshalTypeName);
    }

    @Override
    public T avro(Object library) {
        if (library instanceof AvroLibrary avroLibrary) {
            return avro(avroLibrary);
        } else {
            return avro(Arrays.stream(AvroLibrary.values())
                    .filter(lib -> lib.getDataFormatName().equals(library.toString()))
                    .findFirst()
                    .orElse(AvroLibrary.valueOf(library.toString())));
        }
    }

    @Override
    public T avro(String library, Class<?> unmarshalType) {
        return avro(Arrays.stream(AvroLibrary.values())
                .filter(lib -> lib.getDataFormatName().equals(library))
                .findFirst()
                .orElse(AvroLibrary.valueOf(library)), unmarshalType);
    }

    @Override
    public T avro(String library, Class<?> unmarshalType, String schemaResolver) {
        return avro(Arrays.stream(AvroLibrary.values())
                .filter(lib -> lib.getDataFormatName().equals(library))
                .findFirst()
                .orElse(AvroLibrary.valueOf(library)), unmarshalType, schemaResolver);
    }

    @Override
    public T bindy(String type, Class<?> classType) {
        return bindy(Arrays.stream(BindyType.values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElse(BindyType.valueOf(type)), classType);
    }

    @Override
    public T bindy(String type, Class<?> classType, boolean unwrapSingleInstance) {
        return bindy(Arrays.stream(BindyType.values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElse(BindyType.valueOf(type)), classType, unwrapSingleInstance);
    }

    @Override
    public T json(String library) {
        return json(Arrays.stream(JsonLibrary.values())
                .filter(lib -> lib.getDataFormatName().equalsIgnoreCase(library))
                .findFirst()
                .orElse(JsonLibrary.valueOf(library)));
    }

    @Override
    public T json(String library, boolean prettyPrint) {
        return json(Arrays.stream(JsonLibrary.values())
                .filter(lib -> lib.getDataFormatName().equalsIgnoreCase(library))
                .findFirst()
                .orElse(JsonLibrary.valueOf(library)), prettyPrint);
    }

    @Override
    public T json(String library, Class<?> unmarshalType) {
        return json(Arrays.stream(JsonLibrary.values())
                .filter(lib -> lib.getDataFormatName().equalsIgnoreCase(library))
                .findFirst()
                .orElse(JsonLibrary.valueOf(library)), unmarshalType);
    }

    @Override
    public T json(String library, Class<?> unmarshalType, boolean prettyPrint) {
        return json(Arrays.stream(JsonLibrary.values())
                .filter(lib -> lib.getDataFormatName().equalsIgnoreCase(library))
                .findFirst()
                .orElse(JsonLibrary.valueOf(library)), unmarshalType, prettyPrint);
    }

    @Override
    public T protobuf(String library) {
        return Arrays.stream(ProtobufLibrary.values())
                .filter(lib -> lib.getDataFormatName().equalsIgnoreCase(library))
                .findFirst()
                .map(this::protobuf)
                .orElseGet(() -> super.protobuf(library));
    }

    @Override
    public T protobuf(String library, Class<?> unmarshalType) {
        return protobuf(Arrays.stream(ProtobufLibrary.values())
                .filter(lib -> lib.getDataFormatName().equalsIgnoreCase(library))
                .findFirst()
                .orElse(ProtobufLibrary.valueOf(library)), unmarshalType);
    }

    @Override
    public T protobuf(String library, Class<?> unmarshalType, String schemaResolver) {
        return protobuf(Arrays.stream(ProtobufLibrary.values())
                .filter(lib -> lib.getDataFormatName().equalsIgnoreCase(library))
                .findFirst()
                .orElse(ProtobufLibrary.valueOf(library)), unmarshalType, schemaResolver);
    }

    @Override
    public T yaml(String library) {
        return yaml(Arrays.stream(YAMLLibrary.values())
                .filter(lib -> lib.getDataFormatName().equalsIgnoreCase(library))
                .findFirst()
                .orElse(YAMLLibrary.valueOf(library)));
    }

    @Override
    public T yaml(String library, Class<?> type) {
        return yaml(Arrays.stream(YAMLLibrary.values())
                .filter(lib -> lib.getDataFormatName().equalsIgnoreCase(library))
                .findFirst()
                .orElse(YAMLLibrary.valueOf(library)), type);
    }

    @Override
    public T xmlSecurity(String secureTag, boolean secureTagContents, String recipientKeyAlias, String xmlCipherAlgorithm,
                         String keyCipherAlgorithm, Object keyOrTrustStoreParameters) {
        if (keyOrTrustStoreParameters instanceof KeyStoreParameters keyStoreParameters) {
            return xmlSecurity(secureTag, secureTagContents, recipientKeyAlias, xmlCipherAlgorithm, keyCipherAlgorithm, keyStoreParameters);
        } else {
            throw new CitrusRuntimeException(("Invalid type for key or trust store parameters, expected a KeyStoreParameters, " +
                    "but got: %s").formatted(keyOrTrustStoreParameters.getClass().getName()));
        }
    }

    @Override
    public T xmlSecurity(String secureTag, boolean secureTagContents, String recipientKeyAlias, String xmlCipherAlgorithm,
                         String keyCipherAlgorithm, Object keyOrTrustStoreParameters, String keyPassword) {
        if (keyOrTrustStoreParameters instanceof KeyStoreParameters keyStoreParameters) {
            return xmlSecurity(secureTag, secureTagContents, recipientKeyAlias, xmlCipherAlgorithm, keyCipherAlgorithm, keyStoreParameters, keyPassword);
        } else {
            throw new CitrusRuntimeException(("Invalid type for key or trust store parameters, expected a KeyStoreParameters, " +
                    "but got: %s").formatted(keyOrTrustStoreParameters.getClass().getName()));
        }
    }

    @Override
    public T xmlSecurity(String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
                         String xmlCipherAlgorithm, String keyCipherAlgorithm, Object keyOrTrustStoreParameters) {
        if (keyOrTrustStoreParameters instanceof KeyStoreParameters keyStoreParameters) {
            return xmlSecurity(secureTag, namespaces, secureTagContents, recipientKeyAlias, xmlCipherAlgorithm, keyCipherAlgorithm, keyStoreParameters);
        } else {
            throw new CitrusRuntimeException(("Invalid type for key or trust store parameters, expected a KeyStoreParameters, " +
                    "but got: %s").formatted(keyOrTrustStoreParameters.getClass().getName()));
        }
    }

    @Override
    public T xmlSecurity(String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
                         String xmlCipherAlgorithm, String keyCipherAlgorithm, Object keyOrTrustStoreParameters, String keyPassword) {
        if (keyOrTrustStoreParameters instanceof KeyStoreParameters keyStoreParameters) {
            return xmlSecurity(secureTag, namespaces, secureTagContents, recipientKeyAlias, xmlCipherAlgorithm, keyCipherAlgorithm, keyStoreParameters, keyPassword);
        } else {
            throw new CitrusRuntimeException(("Invalid type for key or trust store parameters, expected a KeyStoreParameters, " +
                    "but got: %s").formatted(keyOrTrustStoreParameters.getClass().getName()));
        }
    }

    @Override
    public T xmlSecurity(String secureTag, Map<String, String> namespaces, boolean secureTagContents, String recipientKeyAlias,
                         String xmlCipherAlgorithm, String keyCipherAlgorithm, Object keyOrTrustStoreParameters, String keyPassword, String digestAlgorithm) {
        if (keyOrTrustStoreParameters instanceof KeyStoreParameters keyStoreParameters) {
            return xmlSecurity(secureTag, namespaces, secureTagContents, recipientKeyAlias, xmlCipherAlgorithm, keyCipherAlgorithm, keyStoreParameters, keyPassword, digestAlgorithm);
        } else {
            throw new CitrusRuntimeException(("Invalid type for key or trust store parameters, expected a KeyStoreParameters, " +
                    "but got: %s").formatted(keyOrTrustStoreParameters.getClass().getName()));
        }
    }
}
