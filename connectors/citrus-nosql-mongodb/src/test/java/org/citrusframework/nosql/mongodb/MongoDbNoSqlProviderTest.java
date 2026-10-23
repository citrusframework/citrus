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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.function.Consumer;

import com.mongodb.MongoException;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.ServerAddress;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.nosql.model.NativeResult;
import org.citrusframework.nosql.model.NoSqlValue;
import org.citrusframework.nosql.model.QueryResult;
import org.citrusframework.nosql.model.WriteResult;
import org.citrusframework.nosql.spi.NoSqlOperation;
import org.citrusframework.nosql.spi.NoSqlOperationKind;
import org.citrusframework.nosql.spi.NoSqlOperationOptions;
import org.citrusframework.nosql.spi.NoSqlPayload;
import org.citrusframework.nosql.spi.NoSqlProviderContext;
import org.citrusframework.nosql.spi.NoSqlProviderRegistry;
import org.mockito.ArgumentMatchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;
import java.util.Arrays;
import com.mongodb.client.MongoCursor;

public class MongoDbNoSqlProviderTest {

    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private MongoDbNoSqlProvider provider;
    private NoSqlProviderContext context;

    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void prepareTest() {
        client = mock(MongoClient.class);
        database = mock(MongoDatabase.class);
        collection = mock(MongoCollection.class);
        provider = new MongoDbNoSqlProvider();
        context = new NoSqlProviderContext("ordersDatabase", "orders", client);

        when(client.getDatabase("orders")).thenReturn(database);
        when(database.getCollection("orders")).thenReturn(collection);
    }

    @Test
    public void shouldQueryDocumentsWithLimit() {
        FindIterable<Document> iterable = iterable();
        when(collection.find(new Document("orderId", "123"))).thenReturn(iterable);
        when(iterable.limit(10)).thenReturn(iterable);

        QueryResult result = (QueryResult) provider.execute(
                new NoSqlOperation(NoSqlOperationKind.QUERY, "orders",
                        NoSqlPayload.text("{ \"orderId\": \"123\" }"), NoSqlOperationOptions.maxResults(10)),
                context);

        assertEquals(result.documents(), List.of(NoSqlValue.object(Map.of("orderId", NoSqlValue.string("123")))));
        assertFalse(result.totalCount().isPresent());
        verify(iterable).limit(10);
    }

    @Test
    public void shouldQueryAllDocumentsWithoutPayload() {
        FindIterable<Document> iterable = iterable();
        when(collection.find(new Document())).thenReturn(iterable);

        provider.execute(NoSqlOperation.of(NoSqlOperationKind.QUERY, "orders", NoSqlPayload.text("")), context);

        verify(collection).find(new Document());
    }

    @Test
    public void shouldInsertDocumentsAndReportGeneratedIds() {
        ObjectId objectId = new ObjectId("507f1f77bcf86cd799439011");
        when(collection.insertMany(anyList()))
                .thenReturn(InsertManyResult.acknowledged(Map.<Integer, org.bson.BsonValue>of(0, new org.bson.BsonObjectId(objectId))));

        WriteResult result = (WriteResult) provider.execute(
                NoSqlOperation.of(NoSqlOperationKind.INSERT, "orders",
                        NoSqlPayload.text("[{ \"orderId\": \"1\" }, { \"orderId\": \"2\" }]")),
                context);

        assertEquals(result.inserted().getAsLong(), 2L);
        assertEquals(result.generatedIds(),
                List.of(NoSqlValue.provider("mongodb", "objectId", "507f1f77bcf86cd799439011")));
        assertFalse(result.matched().isPresent());
    }

    @Test
    public void shouldUpdateSingleDocument() {
        when(collection.updateOne(any(Document.class), any(Document.class),
                any(com.mongodb.client.model.UpdateOptions.class)))
                .thenReturn(UpdateResult.acknowledged(1L, 1L, null));

        WriteResult result = (WriteResult) provider.execute(
                NoSqlOperation.of(NoSqlOperationKind.UPDATE, "orders",
                        NoSqlPayload.text("{ \"filter\": { \"orderId\": \"123\" }, " +
                                "\"update\": { \"$set\": { \"status\": \"COMPLETED\" } } }")),
                context);

        assertEquals(result.matched().getAsLong(), 1L);
        assertEquals(result.modified().getAsLong(), 1L);
        verify(collection, never()).updateMany(any(Document.class), anyList(), any(com.mongodb.client.model.UpdateOptions.class));
    }

    @Test
    public void shouldUpdateMultipleDocuments() {
        when(collection.updateMany(any(Document.class), any(Document.class),
                any(com.mongodb.client.model.UpdateOptions.class)))
                .thenReturn(UpdateResult.acknowledged(3L, 2L, null));

        WriteResult result = (WriteResult) provider.execute(
                NoSqlOperation.of(NoSqlOperationKind.UPDATE, "orders",
                        NoSqlPayload.text("{ \"filter\": { \"status\": \"NEW\" }, " +
                                "\"update\": { \"$set\": { \"status\": \"OPEN\" } }, \"multi\": true }")),
                context);

        assertEquals(result.matched().getAsLong(), 3L);
        assertEquals(result.modified().getAsLong(), 2L);
    }

    @Test
    public void shouldRejectUpdatePayloadWithoutFilterAndUpdate() {
        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> provider.execute(NoSqlOperation.of(NoSqlOperationKind.UPDATE, "orders",
                        NoSqlPayload.text("{ \"filter\": { \"orderId\": \"123\" } }")), context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.INVALID_PAYLOAD);
    }

    @Test
    public void shouldDeleteDocuments() {
        when(collection.deleteMany(new Document("status", "CANCELLED")))
                .thenReturn(DeleteResult.acknowledged(4));

        WriteResult result = (WriteResult) provider.execute(
                NoSqlOperation.of(NoSqlOperationKind.DELETE, "orders",
                        NoSqlPayload.text("{ \"status\": \"CANCELLED\" }")),
                context);

        assertEquals(result.deleted().getAsLong(), 4L);
    }

    @Test
    public void shouldRunNativeAggregation() {
        AggregateIterable<Document> iterable = aggregateIterable();
        doReturn(iterable).when(collection).aggregate(ArgumentMatchers.<org.bson.conversions.Bson>anyList());

        NativeResult result = (NativeResult) provider.execute(
                new NoSqlOperation(NoSqlOperationKind.NATIVE, "mongodb.aggregate", "orders",
                        NoSqlPayload.text("[{ \"$match\": { \"status\": \"COMPLETED\" } }]"),
                        NoSqlOperationOptions.defaults()),
                context);

        assertEquals(result.providerId(), "mongodb");
        assertEquals(((NoSqlValue.ArrayValue) result.value()).values(),
                List.of(NoSqlValue.object(Map.of("total", NoSqlValue.number(2)))));
    }

    @Test
    public void shouldRejectUndeclaredNativeCommandBeforeClientCall() {
        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> provider.execute(new NoSqlOperation(NoSqlOperationKind.NATIVE, "mongodb.mapReduce", "orders",
                        NoSqlPayload.text("{}"), NoSqlOperationOptions.defaults()), context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.UNSUPPORTED_OPERATION);
        verify(client, never()).getDatabase(any());
    }

    @Test
    public void shouldRejectWrongClientType() {
        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> provider.execute(NoSqlOperation.of(NoSqlOperationKind.QUERY, "orders", NoSqlPayload.text("{}")),
                        new NoSqlProviderContext("ordersDatabase", "orders", new Object())));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONFIGURATION);
    }

    @Test
    public void shouldRejectMissingDatabaseName() {
        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> provider.execute(NoSqlOperation.of(NoSqlOperationKind.QUERY, "orders", NoSqlPayload.text("{}")),
                        new NoSqlProviderContext("ordersDatabase", " ", client)));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONFIGURATION);
    }

    @Test
    public void shouldMapWriteTimeoutToUnknownOutcome() {
        doThrow(new MongoTimeoutException("timed out")).when(collection).insertMany(anyList());

        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> provider.execute(NoSqlOperation.of(NoSqlOperationKind.INSERT, "orders",
                        NoSqlPayload.text("{ \"orderId\": \"123\" }")), context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.TIMEOUT);
        assertTrue(exception.isWriteOutcomeUnknown());
        verify(collection).insertMany(anyList());
    }

    @Test
    public void shouldMapServerSelectionFailureToConnectionFailure() {
        when(collection.find(any(Document.class))).thenThrow(new MongoTimeoutException(
                "Timed out while waiting for a server. Client view of cluster state is {type=UNKNOWN, " +
                        "servers=[{address=localhost:1, exception={com.mongodb.MongoSocketOpenException: " +
                        "Exception opening socket}, caused by {java.net.ConnectException: Connection refused}}]}"));

        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> provider.execute(NoSqlOperation.of(NoSqlOperationKind.QUERY, "orders", NoSqlPayload.text("{}")),
                        context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONNECTION);
    }

    @Test
    public void shouldMapReadTimeoutToKnownOutcome() {
        when(collection.find(any(Document.class))).thenThrow(new MongoTimeoutException("timed out"));

        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> provider.execute(NoSqlOperation.of(NoSqlOperationKind.QUERY, "orders", NoSqlPayload.text("{}")),
                        context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.TIMEOUT);
        assertFalse(exception.isWriteOutcomeUnknown());
    }

    @Test
    public void shouldMapConnectionFailure() {
        when(collection.find(any(Document.class)))
                .thenThrow(new MongoSocketOpenException("connection refused", new ServerAddress()));

        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> provider.execute(NoSqlOperation.of(NoSqlOperationKind.QUERY, "orders", NoSqlPayload.text("{}")),
                        context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONNECTION);
    }

    @Test
    public void shouldMapExecutionFailure() {
        when(collection.find(any(Document.class))).thenThrow(new MongoException("command failed"));

        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> provider.execute(NoSqlOperation.of(NoSqlOperationKind.QUERY, "orders", NoSqlPayload.text("{}")),
                        context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.EXECUTION);
    }

    @Test
    public void shouldMapMalformedPayload() {
        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> provider.execute(NoSqlOperation.of(NoSqlOperationKind.QUERY, "orders",
                        NoSqlPayload.text("not-json")), context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.INVALID_PAYLOAD);

        NoSqlException primitiveArray = expectThrows(NoSqlException.class,
                () -> provider.execute(NoSqlOperation.of(NoSqlOperationKind.INSERT, "orders",
                        NoSqlPayload.text("[1, 2]")), context));

        assertEquals(primitiveArray.getErrorClass(), NoSqlErrorClass.INVALID_PAYLOAD);
    }

    @Test
    public void shouldRejectInsertWithoutPayload() {
        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> provider.execute(NoSqlOperation.of(NoSqlOperationKind.INSERT, "orders",
                        NoSqlPayload.text(" ")), context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.INVALID_PAYLOAD);
    }

    @Test
    public void shouldBeRegisteredOnClasspath() {
        NoSqlProviderRegistry registry = NoSqlProviderRegistry.lookup();

        assertTrue(registry.resolve("mongodb") instanceof MongoDbNoSqlProvider);
    }

    private FindIterable<Document> iterable() {
        return iterable(new Document("orderId", "123"));
    }

    @SuppressWarnings("unchecked")
    private FindIterable<Document> iterable(Document... documents) {
        FindIterable<Document> iterable = mock(FindIterable.class);
        doAnswer(invocation -> {
            Consumer<Document> consumer = invocation.getArgument(0);
            List.of(documents).forEach(consumer);
            return null;
        }).when(iterable).forEach(any());
        return iterable;
    }

    @SuppressWarnings("unchecked")
    private AggregateIterable<Document> aggregateIterable() {
        AggregateIterable<Document> iterable = mock(AggregateIterable.class);
        doAnswer(invocation -> {
            Consumer<Document> consumer = invocation.getArgument(0);
            consumer.accept(new Document("total", 2));
            return null;
        }).when(iterable).forEach(any());
        doReturn(cursor(new Document("total", 2))).when(iterable).iterator();
        return iterable;
    }

    @SuppressWarnings("unchecked")
    private MongoCursor<Document> cursor(Document... documents) {
        MongoCursor<Document> cursor = mock(MongoCursor.class);
        List<Document> all = List.of(documents);
        AtomicInteger index = new AtomicInteger();
        doAnswer(invocation -> index.get() < all.size()).when(cursor).hasNext();
        doAnswer(invocation -> all.get(index.getAndIncrement())).when(cursor).next();
        return cursor;
    }
}
