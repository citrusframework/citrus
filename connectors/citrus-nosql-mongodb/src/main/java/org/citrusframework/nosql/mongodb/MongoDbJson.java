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

package org.citrusframework.nosql.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

/**
 * Parses MongoDB JSON payloads into BSON documents.
 */
final class MongoDbJson {

    private MongoDbJson() {
    }

    /**
     * Parse a single JSON object.
     */
    static Document document(String json) {
        return Document.parse(json);
    }

    /**
     * Parse either a single JSON object or a JSON array of objects.
     */
    static List<Document> documents(String json) {
        if (!isArray(json)) {
            return List.of(document(json));
        }

        Document wrapper = Document.parse("{\"values\": " + json + "}");
        Object rawValues = wrapper.get("values");
        if (!(rawValues instanceof List<?> entries) || entries.isEmpty()) {
            throw new IllegalArgumentException("MongoDB document list must be a non-empty array of JSON objects");
        }

        List<Document> documents = new ArrayList<>(entries.size());
        for (Object entry : entries) {
            if (!(entry instanceof Document document)) {
                throw new IllegalArgumentException(
                        "MongoDB document list must contain only JSON objects, but found: " + entry);
            }
            documents.add(document);
        }

        return documents;
    }

    /**
     * Parse a native aggregation pipeline given as JSON array.
     */
    static List<Document> pipeline(String json) {
        if (!isArray(json)) {
            return List.of(document(json));
        }

        return documents(json);
    }

    private static boolean isArray(String json) {
        return json.trim().startsWith("[");
    }
}
