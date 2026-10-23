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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.mongodb.MongoException;
import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoSocketReadTimeoutException;
import com.mongodb.MongoSocketWriteTimeoutException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.nosql.model.NativeResult;
import org.citrusframework.nosql.model.NoSqlResult;
import org.citrusframework.nosql.model.NoSqlValue;
import org.citrusframework.nosql.model.QueryResult;
import org.citrusframework.nosql.model.WriteResult;
import org.citrusframework.nosql.spi.NoSqlCapability;
import org.citrusframework.nosql.spi.NoSqlOperation;
import org.citrusframework.nosql.spi.NoSqlOperationKind;
import org.citrusframework.nosql.spi.NoSqlProvider;
import org.citrusframework.nosql.spi.NoSqlProviderContext;

/**
 * MongoDB NoSQL provider.
 *
 * <p>The provider supports the generic query, insert, update and delete operations plus the
 * {@code mongodb.aggregate} native command. Payloads use MongoDB JSON syntax. Command names that
 * are not declared by this provider are rejected before the client is called.</p>
 */
public class MongoDbNoSqlProvider implements NoSqlProvider {

    public static final String ID = "mongodb";
    public static final String NATIVE_AGGREGATE = "mongodb.aggregate";

    private final MongoDbValueMapper valueMapper = new MongoDbValueMapper();

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Set<NoSqlCapability> capabilities() {
        return Set.of(NoSqlCapability.QUERY, NoSqlCapability.INSERT, NoSqlCapability.UPDATE,
                NoSqlCapability.DELETE, NoSqlCapability.NATIVE);
    }

    @Override
    public Set<String> nativeCommands() {
        return Set.of(NATIVE_AGGREGATE);
    }

    @Override
    public NoSqlResult execute(NoSqlOperation operation, NoSqlProviderContext context) {
        if (operation.kind() == NoSqlOperationKind.NATIVE
                && (operation.nativeCommand() == null
                || !nativeCommands().contains(operation.nativeCommand()))) {
            throw new NoSqlException(NoSqlErrorClass.UNSUPPORTED_OPERATION,
                    String.format("MongoDB provider does not support native command '%s'", operation.nativeCommand()));
        }

        MongoClient client = client(context);
        MongoCollection<Document> collection = database(client, context).getCollection(target(operation));

        return switch (operation.kind()) {
            case QUERY -> guarded(operation.kind(), () -> query(collection, operation));
            case INSERT -> guarded(operation.kind(), () -> insert(collection, operation));
            case UPDATE -> guarded(operation.kind(), () -> update(collection, operation));
            case DELETE -> guarded(operation.kind(), () -> delete(collection, operation));
            case NATIVE -> guarded(operation.kind(), () -> nativeOperation(collection, operation));
        };
    }

    private QueryResult query(MongoCollection<Document> collection, NoSqlOperation operation) {
        Document filter = operation.payload().isEmpty()
                ? new Document()
                : MongoDbJson.document(operation.payload().content());

        FindIterable<Document> iterable = collection.find(filter);
        operation.options().maxResults().ifPresent(iterable::limit);

        List<NoSqlValue> documents = new ArrayList<>();
        iterable.forEach(document -> documents.add(valueMapper.map(document)));

        return QueryResult.of(documents);
    }

    private WriteResult insert(MongoCollection<Document> collection, NoSqlOperation operation) {
        requirePayload(operation);
        List<Document> documents = MongoDbJson.documents(operation.payload().content());

        InsertManyResult result = collection.insertMany(documents);
        if (!result.wasAcknowledged()) {
            return WriteResult.empty();
        }

        List<NoSqlValue> generatedIds = new ArrayList<>();
        result.getInsertedIds().values().forEach(identifier -> generatedIds.add(valueMapper.map(identifier)));

        return WriteResult.inserted(documents.size(), generatedIds);
    }

    private WriteResult update(MongoCollection<Document> collection, NoSqlOperation operation) {
        requirePayload(operation);
        Document payload = MongoDbJson.document(operation.payload().content());

        Object rawFilter = payload.get("filter");
        Object rawUpdate = payload.get("update");
        if (!(rawFilter instanceof Document filter) || !(rawUpdate instanceof Document update)) {
            throw new NoSqlException(NoSqlErrorClass.INVALID_PAYLOAD,
                    "MongoDB update payload requires 'filter' and 'update' documents");
        }

        boolean multi = booleanOption(payload, "multi");
        boolean upsert = booleanOption(payload, "upsert");

        UpdateResult result = multi
                ? collection.updateMany(filter, update, new UpdateOptions().upsert(upsert))
                : collection.updateOne(filter, update, new UpdateOptions().upsert(upsert));

        if (!result.wasAcknowledged()) {
            return WriteResult.empty();
        }

        List<NoSqlValue> generatedIds = new ArrayList<>();
        if (result.getUpsertedId() != null) {
            generatedIds.add(valueMapper.map(result.getUpsertedId()));
        }

        return WriteResult.updated(result.getMatchedCount(), result.getModifiedCount(), generatedIds);
    }

    private boolean booleanOption(Document payload, String name) {
        Object rawValue = payload.get(name);
        if (rawValue == null) {
            return false;
        }

        if (rawValue instanceof Boolean booleanValue) {
            return booleanValue;
        }

        throw new NoSqlException(NoSqlErrorClass.INVALID_PAYLOAD,
                String.format("MongoDB update option '%s' must be a boolean", name));
    }

    private WriteResult delete(MongoCollection<Document> collection, NoSqlOperation operation) {
        Document filter = operation.payload().isEmpty()
                ? new Document()
                : MongoDbJson.document(operation.payload().content());

        DeleteResult result = collection.deleteMany(filter);
        if (!result.wasAcknowledged()) {
            return WriteResult.empty();
        }

        return WriteResult.deleted(result.getDeletedCount());
    }

    private NativeResult nativeOperation(MongoCollection<Document> collection, NoSqlOperation operation) {
        if (!NATIVE_AGGREGATE.equals(operation.nativeCommand())) {
            throw new NoSqlException(NoSqlErrorClass.UNSUPPORTED_OPERATION,
                    String.format("MongoDB provider does not support native command '%s'", operation.nativeCommand()));
        }

        requirePayload(operation);
        List<Document> pipeline = MongoDbJson.pipeline(operation.payload().content());

        AggregateIterable<Document> iterable = collection.aggregate(pipeline);
        int limit = operation.options().maxResults().orElse(Integer.MAX_VALUE);

        List<NoSqlValue> documents = new ArrayList<>();
        for (Document document : iterable) {
            if (documents.size() >= limit) {
                break;
            }

            documents.add(valueMapper.map(document));
        }

        Map<String, NoSqlValue> metadata = new LinkedHashMap<>();
        metadata.put("command", NoSqlValue.string(operation.nativeCommand()));
        metadata.put("target", NoSqlValue.string(operation.target()));

        return new NativeResult(ID, NoSqlValue.array(documents), metadata);
    }

    private NoSqlResult guarded(NoSqlOperationKind kind, Supplier<NoSqlResult> call) {
        try {
            return call.get();
        } catch (NoSqlException e) {
            throw e;
        } catch (MongoSocketReadTimeoutException | MongoSocketWriteTimeoutException e) {
            throw timeout(kind, e);
        } catch (MongoTimeoutException e) {
            if (isConnectionFailure(e)) {
                throw new NoSqlException(NoSqlErrorClass.CONNECTION,
                        String.format("MongoDB connection failed during operation '%s'", kind), e);
            }

            throw timeout(kind, e);
        } catch (MongoSocketException e) {
            throw new NoSqlException(NoSqlErrorClass.CONNECTION,
                    String.format("MongoDB connection failed during operation '%s'", kind), e);
        } catch (MongoExecutionTimeoutException e) {
            throw timeout(kind, e);
        } catch (MongoException e) {
            throw new NoSqlException(NoSqlErrorClass.EXECUTION,
                    String.format("MongoDB operation '%s' failed", kind), e);
        } catch (IllegalArgumentException | org.bson.json.JsonParseException e) {
            throw new NoSqlException(NoSqlErrorClass.INVALID_PAYLOAD,
                    String.format("Invalid MongoDB payload for operation '%s'", kind), e);
        }
    }

    private NoSqlException timeout(NoSqlOperationKind kind, RuntimeException cause) {
        return new NoSqlException(NoSqlErrorClass.TIMEOUT,
                String.format("MongoDB operation '%s' timed out", kind), cause, isWrite(kind));
    }

    /**
     * Only explicit write operations report an unknown write outcome. A native command is a read or
     * a write depending on the command, so only the provider can decide.
     */
    private boolean isWrite(NoSqlOperationKind kind) {
        return kind == NoSqlOperationKind.INSERT
                || kind == NoSqlOperationKind.UPDATE
                || kind == NoSqlOperationKind.DELETE;
    }

    private boolean isConnectionFailure(MongoTimeoutException exception) {
        for (Throwable cause = exception.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof MongoSocketWriteTimeoutException) {
                return false;
            }

            if (cause instanceof MongoSocketException && !(cause instanceof MongoSocketReadTimeoutException)) {
                return true;
            }
        }

        String message = exception.getMessage();
        return message != null
                && (message.contains("MongoSocketOpenException")
                || message.contains("MongoSocketException")
                || message.contains("Connection refused"));
    }

    private String target(NoSqlOperation operation) {
        if (operation.target() == null || operation.target().isBlank()) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "MongoDB operation requires a non-blank target");
        }

        return operation.target();
    }

    private MongoClient client(NoSqlProviderContext context) {
        if (!(context.client() instanceof MongoClient mongoClient)) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    String.format("MongoDB configuration '%s' requires a MongoClient", context.configurationName()));
        }

        return mongoClient;
    }

    private MongoDatabase database(MongoClient client, NoSqlProviderContext context) {
        if (context.database() == null || context.database().isBlank()) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    String.format("MongoDB configuration '%s' requires a database name", context.configurationName()));
        }

        return client.getDatabase(context.database());
    }

    private void requirePayload(NoSqlOperation operation) {
        if (operation.payload().isEmpty()) {
            throw new NoSqlException(NoSqlErrorClass.INVALID_PAYLOAD,
                    String.format("MongoDB operation '%s' requires a payload", operation.kind()));
        }
    }
}
