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

package org.citrusframework.camel.actions.infra;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

public class InfraServiceUtilsTest {

    private static InfraService infraService(String service, String impl, List<String> alias, List<String> aliasImpl) {
        return new InfraService(service, impl, "Sample infra service", alias, aliasImpl,
                "org.acme", "test-infra-services", "0.1", "0.1");
    }

    @Test
    public void shouldResolveByServiceNameOnly() {
        InfraService kafka = infraService("org.acme.InfraService", "org.acme.InfraServiceImpl", List.of("kafka"), null);
        List<InfraService> services = List.of(kafka);

        Optional<InfraService> result = InfraServiceUtils.resolveInfraService(services, "kafka", null);
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get(), kafka);
    }

    @Test
    public void shouldResolveByServiceNameWithEmptyAliasImplementation() {
        InfraService kafka = infraService("org.acme.InfraService", "org.acme.InfraServiceImpl", List.of("kafka"), Collections.emptyList());
        List<InfraService> services = List.of(kafka);

        Optional<InfraService> result = InfraServiceUtils.resolveInfraService(services, "kafka", null);
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get(), kafka);
    }

    @Test
    public void shouldResolveByServiceNameAndImplementation() {
        InfraService redpanda = infraService("org.acme.InfraService", "org.acme.InfraServiceImpl", List.of("kafka"), List.of("redpanda"));
        List<InfraService> services = List.of(redpanda);

        Optional<InfraService> result = InfraServiceUtils.resolveInfraService(services, "kafka", "redpanda");
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get(), redpanda);
    }

    @Test
    public void shouldReturnEmptyWhenServiceNameDoesNotMatch() {
        InfraService kafka = infraService("org.acme.InfraService", "org.acme.InfraServiceImpl", List.of("kafka"), null);
        List<InfraService> services = List.of(kafka);

        Optional<InfraService> result = InfraServiceUtils.resolveInfraService(services, "redis", null);
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void shouldReturnEmptyWhenImplementationDoesNotMatch() {
        InfraService redpanda = infraService("org.acme.InfraService", "org.acme.InfraServiceImpl", List.of("kafka"), List.of("redpanda"));
        List<InfraService> services = List.of(redpanda);

        Optional<InfraService> result = InfraServiceUtils.resolveInfraService(services, "kafka", "strimzi");
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void shouldReturnEmptyWhenImplementationGivenButServiceHasNone() {
        InfraService kafka = infraService("org.acme.InfraService", "org.acme.InfraServiceImpl", List.of("kafka"), null);
        List<InfraService> services = List.of(kafka);

        Optional<InfraService> result = InfraServiceUtils.resolveInfraService(services, "kafka", "redpanda");
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void shouldNotMatchServiceWithImplementationWhenNullRequested() {
        InfraService redpanda = infraService("org.acme.InfraService", "org.acme.InfraServiceImpl", List.of("kafka"), List.of("redpanda"));
        List<InfraService> services = List.of(redpanda);

        Optional<InfraService> result = InfraServiceUtils.resolveInfraService(services, "kafka", null);
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void shouldReturnEmptyForEmptyServiceList() {
        Optional<InfraService> result = InfraServiceUtils.resolveInfraService(Collections.emptyList(), "kafka", null);
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void shouldResolveFirstMatchWhenMultipleServicesMatch() {
        InfraService first = infraService("org.acme.InfraService1", "org.acme.InfraServiceImpl1", List.of("kafka"), null);
        InfraService second = infraService("org.acme.InfraService2", "org.acme.InfraServiceImpl2", List.of("kafka"), null);
        List<InfraService> services = List.of(first, second);

        Optional<InfraService> result = InfraServiceUtils.resolveInfraService(services, "kafka", null);
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get(), first);
    }

    @Test
    public void shouldMatchAnyAliasInList() {
        InfraService service = infraService("org.acme.InfraService", "org.acme.InfraServiceImpl", List.of("dynamodb", "dynamo-db"), null);
        List<InfraService> services = List.of(service);

        Assert.assertTrue(InfraServiceUtils.resolveInfraService(services, "dynamodb", null).isPresent());
        Assert.assertTrue(InfraServiceUtils.resolveInfraService(services, "dynamo-db", null).isPresent());
    }

    @Test
    public void shouldReturnEmptyWhenImplementationIsEmptyString() {
        InfraService kafka = infraService("org.acme.InfraService", "org.acme.InfraServiceImpl", List.of("kafka"), null);
        List<InfraService> services = List.of(kafka);

        Optional<InfraService> result = InfraServiceUtils.resolveInfraService(services, "kafka", "");
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void shouldListInfraServices() {
        Assert.assertEquals(InfraServiceUtils.getInfraServiceNames().stream().sorted().collect(Collectors.joining("\n")), """
                 arangodb
                 artemis
                 artemis.amqp
                 aws.cloud-watch
                 aws.config
                 aws.dynamo-db
                 aws.dynamodb
                 aws.ec2
                 aws.event-bridge
                 aws.iam
                 aws.kinesis
                 aws.kms
                 aws.lambda
                 aws.s3
                 aws.secrets-manager
                 aws.sns
                 aws.sqs
                 aws.ssm
                 aws.sts
                 aws.transcribe
                 azure.storage-blob
                 azure.storage-queue
                 cassandra
                 chat-script
                 consul
                 couchbase
                 couchdb
                 docling
                 elasticsearch
                 fhir
                 ftp
                 ftps
                 google.pub-sub
                 hashicorp.vault
                 hazelcast
                 hive-mq
                 hive-mq.sparkplug
                 ibmmq
                 iggy
                 ignite
                 infinispan
                 kafka
                 kafka.confluent
                 kafka.redpanda
                 kafka.strimzi
                 keycloak
                 microprofile.lra
                 milvus
                 minio
                 mongodb
                 mosquitto
                 nats
                 neo4j
                 ollama
                 openldap
                 pinecone
                 postgres
                 pulsar
                 qdrant
                 rabbitmq
                 redis
                 rocketmq
                 sftp
                 smb
                 solr
                 torch-serve
                 weaviate
                 xmpp
                 zookeeper""");
    }

}
