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

package org.citrusframework.openapi.util;

import static java.lang.String.format;

import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasSchema;
import jakarta.annotation.Nonnull;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.openapi.OpenApiConstants;
import org.citrusframework.util.StringUtils;

public class OpenApiUtils {

    private OpenApiUtils() {
        // Static access only
    }

    public static String getMethodPath(@Nonnull HttpMessage httpMessage) {
        Object methodHeader = httpMessage.getHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD);
        Object path = httpMessage.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI);

        return getMethodPath(methodHeader != null ? methodHeader.toString().toLowerCase() : "null",
            path != null ? path.toString() : "null");
    }

    public static String getMethodPath(@Nonnull String method, @Nonnull String path) {
        if (StringUtils.hasText(path) && path.startsWith("/")) {
            path = path.substring(1);
        }
        return format("/%s/%s", method.toLowerCase(), path);
    }

    /**
     * @return a unique scenario id for the {@link OasOperation}
     */
    public static String createFullPathOperationIdentifier(String path, OasOperation oasOperation) {
        return format("%s_%s", oasOperation.getMethod().toUpperCase(), path);
    }

    public static boolean isAnyNumberScheme(OasSchema schema) {
        return (
            schema != null &&
                (OpenApiConstants.TYPE_INTEGER.equalsIgnoreCase(schema.type) ||
                    OpenApiConstants.TYPE_NUMBER.equalsIgnoreCase(schema.type))
        );
    }

}
