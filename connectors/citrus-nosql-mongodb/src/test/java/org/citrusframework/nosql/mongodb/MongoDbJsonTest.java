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

import java.util.List;

import org.bson.Document;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class MongoDbJsonTest {

    @Test
    public void shouldParseSingleDocument() {
        Document document = MongoDbJson.document("{ \"orderId\": \"123\", \"amount\": 42.5 }");

        assertEquals(document.getString("orderId"), "123");
        assertEquals(document.getDouble("amount"), 42.5);
    }

    @Test
    public void shouldParseObjectOrArrayOfDocuments() {
        assertEquals(MongoDbJson.documents("{ \"orderId\": \"123\" }").size(), 1);

        List<Document> documents = MongoDbJson.documents("[{ \"orderId\": \"1\" }, { \"orderId\": \"2\" }]");
        assertEquals(documents.size(), 2);
        assertEquals(documents.get(1).getString("orderId"), "2");
    }

    @Test
    public void shouldParsePipeline() {
        List<Document> pipeline = MongoDbJson.pipeline("[{ \"$match\": { \"status\": \"COMPLETED\" } }]");

        assertEquals(pipeline.size(), 1);
        assertEquals(pipeline.get(0).get("$match", Document.class).getString("status"), "COMPLETED");
    }

    @Test
    public void shouldRejectMalformedJson() {
        expectThrows(RuntimeException.class, () -> MongoDbJson.document("not-json"));
        expectThrows(IllegalArgumentException.class, () -> MongoDbJson.documents("[]"));
        expectThrows(IllegalArgumentException.class, () -> MongoDbJson.documents("[1, 2]"));
    }
}
