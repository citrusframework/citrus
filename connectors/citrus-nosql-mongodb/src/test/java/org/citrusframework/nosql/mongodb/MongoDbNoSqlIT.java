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
import java.util.concurrent.TimeUnit;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.TestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.nosql.actions.NoSqlAction;
import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.nosql.model.NativeResult;
import org.citrusframework.nosql.model.NoSqlValue;
import org.citrusframework.nosql.model.QueryResult;
import org.citrusframework.nosql.spi.NoSqlOperation;
import org.citrusframework.nosql.spi.NoSqlOperationKind;
import org.citrusframework.nosql.spi.NoSqlPayload;
import org.citrusframework.nosql.spi.NoSqlProviderContext;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.yaml.YamlTestLoader;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class MongoDbNoSqlIT {

    private static final String DATABASE = "orders";
    private static final String COLLECTION = "orders";

    private MongoDBContainer mongo;
    private MongoClient client;
    private MongoDbNoSqlProvider provider;
    private NoSqlProviderContext providerContext;

    @BeforeClass
    public void startContainer() {
        mongo = new MongoDBContainer(DockerImageName.parse("mongo:8.2.2"));
        mongo.start();

        client = MongoClients.create(mongo.getConnectionString());
        provider = new MongoDbNoSqlProvider();
        providerContext = new NoSqlProviderContext("ordersDatabase", DATABASE, client);
    }

    @AfterClass(alwaysRun = true)
    public void stopContainer() {
        if (client != null) {
            client.close();
        }
        if (mongo != null) {
            mongo.stop();
        }
    }

    @BeforeMethod
    public void prepareCollection() {
        client.getDatabase(DATABASE).getCollection(COLLECTION).drop();
    }

    @Test
    public void shouldRunCrudRoundTrip() {
        TestContext context = testContext();

        execute(context, new NoSqlAction.Builder()
                .client("ordersDatabase")
                .insert()
                .target(COLLECTION)
                .payload("[{ \"orderId\": \"1001\", \"status\": \"NEW\", \"customerId\": \"customer-42\" }," +
                        " { \"orderId\": \"1002\", \"status\": \"NEW\", \"customerId\": \"customer-43\" }]"));

        execute(context, new NoSqlAction.Builder()
                .client("ordersDatabase")
                .query()
                .target(COLLECTION)
                .filter("{ \"orderId\": \"1001\" }")
                .validate("$.documents[0].status", "NEW")
                .extract("$.documents[0].customerId", "customerId"));

        assertEquals(context.getVariable("customerId"), "customer-42");

        execute(context, new NoSqlAction.Builder()
                .client("ordersDatabase")
                .update()
                .target(COLLECTION)
                .payload("{ \"filter\": { \"orderId\": \"1001\" }, " +
                        "\"update\": { \"$set\": { \"status\": \"COMPLETED\" } } }"));

        QueryResult completed = (QueryResult) provider.execute(
                NoSqlOperation.of(NoSqlOperationKind.QUERY, COLLECTION,
                        NoSqlPayload.text("{ \"status\": \"COMPLETED\" }")),
                providerContext);
        assertEquals(completed.documents().size(), 1);

        execute(context, new NoSqlAction.Builder()
                .client("ordersDatabase")
                .delete()
                .target(COLLECTION)
                .payload("{ \"orderId\": \"1002\" }"));

        QueryResult remaining = (QueryResult) provider.execute(
                NoSqlOperation.of(NoSqlOperationKind.QUERY, COLLECTION, NoSqlPayload.text("")),
                providerContext);
        assertEquals(remaining.documents().size(), 1);
    }

    @Test
    public void shouldApplyQueryLimit() {
        TestContext context = testContext();

        execute(context, new NoSqlAction.Builder()
                .client("ordersDatabase")
                .insert()
                .target(COLLECTION)
                .payload("[{ \"orderId\": \"1\" }, { \"orderId\": \"2\" }, { \"orderId\": \"3\" }]"));

        execute(context, new NoSqlAction.Builder()
                .client("ordersDatabase")
                .query()
                .target(COLLECTION)
                .filter("{}")
                .maxResults(2)
                .validate("$.documents.length()", "2"));
    }

    @Test
    public void shouldRunNativeAggregation() {
        execute(testContext(), new NoSqlAction.Builder()
                .client("ordersDatabase")
                .insert()
                .target(COLLECTION)
                .payload("[{ \"orderId\": \"1\", \"status\": \"NEW\" }, " +
                        "{ \"orderId\": \"2\", \"status\": \"NEW\" }, " +
                        "{ \"orderId\": \"3\", \"status\": \"COMPLETED\" }]"));

        NativeResult result = (NativeResult) provider.execute(
                new NoSqlOperation(NoSqlOperationKind.NATIVE, "mongodb.aggregate", COLLECTION,
                        NoSqlPayload.text("[{ \"$group\": { \"_id\": \"$status\", \"total\": { \"$sum\": 1 } } }," +
                                " { \"$sort\": { \"_id\": 1 } }]"),
                        org.citrusframework.nosql.spi.NoSqlOperationOptions.defaults()),
                providerContext);

        assertEquals(result.providerId(), "mongodb");
        List<NoSqlValue> groups = ((NoSqlValue.ArrayValue) result.value()).values();
        assertEquals(groups.size(), 2);
        assertEquals(((NoSqlValue.ObjectValue) groups.get(0)).values().get("total"), NoSqlValue.number(1));
    }

    @Test
    public void shouldExecuteXmlTestWithMongoDb() {
        TestContext context = testContext();
        Citrus citrus = CitrusInstanceManager.getOrDefault();

        XmlTestLoader loader = new XmlTestLoader(getClass(), "MongoDbXmlIT", getClass().getPackageName());
        loader.setSource(new TestSource(TestLoader.XML, "MongoDbXmlIT").sourceCode("""
                <test name="MongoDbXmlIT" xmlns="http://citrusframework.org/schema/xml/testcase">
                  <actions>
                    <nosql client="ordersDatabase" operation="insert" target="orders">
                      <payload>{ "orderId": "3001", "status": "NEW" }</payload>
                    </nosql>
                    <nosql client="ordersDatabase" operation="query" target="orders">
                      <filter>{ "orderId": "3001" }</filter>
                      <validate path="$.documents[0].status" value="NEW"/>
                      <extract path="$.documents[0].orderId" variable="xmlOrderId"/>
                    </nosql>
                  </actions>
                </test>
                """));
        loader.setCitrus(citrus);
        loader.setContext(context);
        loader.load();

        assertEquals(context.getVariable("xmlOrderId"), "3001");
    }

    @Test
    public void shouldExecuteYamlTestWithMongoDb() {
        TestContext context = testContext();
        Citrus citrus = CitrusInstanceManager.getOrDefault();

        YamlTestLoader loader = new YamlTestLoader(getClass(), "MongoDbYamlIT", getClass().getPackageName());
        loader.setSource(new TestSource(TestLoader.YAML, "MongoDbYamlIT").sourceCode("""
                name: MongoDbYamlIT
                actions:
                  - nosql:
                      client: ordersDatabase
                      operation: insert
                      target: orders
                      payload: '{ "orderId": "4001", "status": "NEW" }'
                  - nosql:
                      client: ordersDatabase
                      operation: query
                      target: orders
                      filter: '{ "orderId": "4001" }'
                      validate:
                        - path: "$.documents[0].status"
                          value: NEW
                      extract:
                        - path: "$.documents[0].orderId"
                          variable: yamlOrderId
                """));
        loader.setCitrus(citrus);
        loader.setContext(context);
        loader.load();

        assertEquals(context.getVariable("yamlOrderId"), "4001");
    }

    @Test
    public void shouldReportConnectionFailure() {
        MongoClient unreachable = MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(
                        "mongodb://localhost:1/?serverSelectionTimeoutMS=300&connectTimeoutMS=300"))
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(300, TimeUnit.MILLISECONDS))
                .build());

        try {
            TestContext context = TestContextFactory.newInstance().getObject();
            context.getReferenceResolver().bind("unreachableDatabase",
                    MongoDbConfigurations.create("unreachableDatabase", unreachable, DATABASE));

            NoSqlException exception = expectThrows(NoSqlException.class,
                    () -> execute(context, new NoSqlAction.Builder()
                            .client("unreachableDatabase")
                            .query()
                            .target(COLLECTION)
                            .filter("{}")));

            assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONNECTION);
        } finally {
            unreachable.close();
        }
    }

    @Test
    public void shouldRejectUndeclaredNativeCommand() {
        TestContext context = testContext();

        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> execute(context, new NoSqlAction.Builder()
                        .client("ordersDatabase")
                        .nativeOperation("mongodb.mapReduce")
                        .target(COLLECTION)
                        .payload("{}")));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.UNSUPPORTED_OPERATION);
        assertEquals(client.getDatabase(DATABASE).getCollection(COLLECTION).countDocuments(), 0);
    }

    private TestContext testContext() {
        TestContext context = TestContextFactory.newInstance().getObject();
        context.getReferenceResolver().bind("ordersDatabase",
                MongoDbConfigurations.create("ordersDatabase", client, DATABASE));
        return context;
    }

    private void execute(TestContext context, NoSqlAction.Builder builder) {
        builder.build().execute(context);
    }

}
