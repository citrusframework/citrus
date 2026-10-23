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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.ServerAddress;
import com.mongodb.MongoSocketWriteTimeoutException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.nosql.model.NativeResult;
import org.citrusframework.nosql.model.WriteResult;
import org.citrusframework.nosql.spi.NoSqlOperation;
import org.citrusframework.nosql.spi.NoSqlOperationKind;
import org.citrusframework.nosql.spi.NoSqlOperationOptions;
import org.citrusframework.nosql.spi.NoSqlPayload;
import org.citrusframework.nosql.spi.NoSqlProviderContext;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;
import java.util.Arrays;
import com.mongodb.client.MongoCursor;
import org.mockito.ArgumentMatchers;

/**
 * Regression tests for the defects found in the second implementation review.
 */
public class MongoDbNoSqlProviderRegressionTest {

    private MongoDbNoSqlProvider provider;
    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    @BeforeMethod
    public void prepareTest() {
        provider = new MongoDbNoSqlProvider();
        client = mock(MongoClient.class);
        database = mock(MongoDatabase.class);
        collection = mock(MongoCollection.class);

        when(client.getDatabase(anyString())).thenReturn(database);
        when(database.getCollection(anyString())).thenReturn(collection);
    }

    @Test
    public void shouldMapWriteTimeoutToTimeoutWithUnknownOutcome() {
        when(collection.insertMany(anyList())).thenThrow(new MongoSocketWriteTimeoutException(
                "write timed out", new ServerAddress("localhost", 27017), null));

        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> provider.execute(insertOperation("{}"), context()));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.TIMEOUT);
        assertTrue(exception.isWriteOutcomeUnknown());
    }

    @Test
    public void shouldMapServerSideTimeoutToTimeout() {
        when(collection.insertMany(anyList())).thenThrow(new MongoExecutionTimeoutException(50, "operation exceeded time limit"));

        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> provider.execute(insertOperation("{}"), context()));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.TIMEOUT);
    }

    @Test
    public void shouldRejectNativeOperationWithoutCommand() {
        NoSqlOperation operation = new NoSqlOperation(NoSqlOperationKind.NATIVE, null, "orders",
                NoSqlPayload.text("[]"), NoSqlOperationOptions.defaults());

        NoSqlException exception = expectThrows(NoSqlException.class, () -> provider.execute(operation, context()));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.UNSUPPORTED_OPERATION);
    }

    @Test
    public void shouldRejectBlankTarget() {
        NoSqlOperation operation = new NoSqlOperation(NoSqlOperationKind.QUERY, null, " ",
                NoSqlPayload.text("{}"), NoSqlOperationOptions.defaults());

        NoSqlException exception = expectThrows(NoSqlException.class, () -> provider.execute(operation, context()));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONFIGURATION);
    }

    @Test
    public void shouldRejectNonDocumentFilterInUpdatePayload() {
        NoSqlOperation operation = new NoSqlOperation(NoSqlOperationKind.UPDATE, null, "orders",
                NoSqlPayload.text("{\"filter\": \"not-a-document\", \"update\": {\"$set\": {\"a\": 1}}}"),
                NoSqlOperationOptions.defaults());

        NoSqlException exception = expectThrows(NoSqlException.class, () -> provider.execute(operation, context()));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.INVALID_PAYLOAD);
    }

    @Test
    public void shouldRejectNonBooleanUpdateOption() {
        NoSqlOperation operation = new NoSqlOperation(NoSqlOperationKind.UPDATE, null, "orders",
                NoSqlPayload.text("{\"filter\": {}, \"update\": {\"$set\": {\"a\": 1}}, \"multi\": \"yes\"}"),
                NoSqlOperationOptions.defaults());

        NoSqlException exception = expectThrows(NoSqlException.class, () -> provider.execute(operation, context()));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.INVALID_PAYLOAD);
    }

    @Test
    public void shouldReportUnacknowledgedWriteWithoutCounters() {
        when(collection.insertMany(anyList())).thenReturn(InsertManyResult.unacknowledged());

        WriteResult result = (WriteResult) provider.execute(insertOperation("{}"), context());

        assertTrue(result.inserted().isEmpty());
        assertTrue(result.matched().isEmpty());
        assertTrue(result.generatedIds().isEmpty());
    }

    @Test
    public void shouldReportUpsertedIdInGeneratedIds() {
        when(collection.updateOne(Mockito.any(Document.class), Mockito.any(Document.class), Mockito.any()))
                .thenReturn(UpdateResult.acknowledged(0L, 0L, new org.bson.BsonObjectId(new org.bson.types.ObjectId())));

        NoSqlOperation operation = new NoSqlOperation(NoSqlOperationKind.UPDATE, null, "orders",
                NoSqlPayload.text("{\"filter\": {\"id\": 1}, \"update\": {\"$set\": {\"a\": 1}}, \"upsert\": true}"),
                NoSqlOperationOptions.defaults());

        WriteResult result = (WriteResult) provider.execute(operation, context());

        assertEquals(result.generatedIds().size(), 1);
    }

    @Test
    public void shouldLimitNativeAggregationAndReportMetadata() {
        AggregateIterable<Document> iterable = mock(AggregateIterable.class);
        MongoCursor<Document> cursor = cursor(
                new Document("status", "NEW"),
                new Document("status", "NEW"),
                new Document("status", "COMPLETED"));

        when(collection.aggregate(ArgumentMatchers.<org.bson.conversions.Bson>anyList())).thenReturn(iterable);
        doReturn(cursor).when(iterable).iterator();
        NoSqlOperation operation = new NoSqlOperation(NoSqlOperationKind.NATIVE, "mongodb.aggregate", "orders",
                NoSqlPayload.text("[{\"$group\": {\"_id\": \"$status\"}}]"),
                NoSqlOperationOptions.maxResults(2));

        NativeResult result = (NativeResult) provider.execute(operation, context());

        assertEquals(result.value() instanceof org.citrusframework.nosql.model.NoSqlValue.ArrayValue arrayValue
                ? arrayValue.values().size() : -1, 2);
        assertEquals(result.metadata().get("command").render(), "mongodb.aggregate");
        assertEquals(result.metadata().get("target").render(), "orders");
    }

    private NoSqlOperation insertOperation(String payload) {
        return new NoSqlOperation(NoSqlOperationKind.INSERT, null, "orders",
                NoSqlPayload.text(payload), NoSqlOperationOptions.defaults());
    }

    private NoSqlProviderContext context() {
        return new NoSqlProviderContext("ordersDatabase", "orders", client);
    }

    private static Iterator<Document> iterator(List<Document> documents) {
        return Collections.unmodifiableList(documents).iterator();
    }

    @SuppressWarnings("unchecked")
    private static MongoCursor<Document> cursor(Document... documents) {
        MongoCursor<Document> cursor = mock(MongoCursor.class);
        List<Document> all = List.of(documents);
        AtomicInteger index = new AtomicInteger();
        doAnswer(invocation -> index.get() < all.size()).when(cursor).hasNext();
        doAnswer(invocation -> all.get(index.getAndIncrement())).when(cursor).next();
        return cursor;
    }
}
