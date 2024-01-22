/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.sharding;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.SystemProvider;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.citrusframework.sharding.ShardingConfiguration.SHARD_NUMBER_ENV_VAR_NAME;
import static org.citrusframework.sharding.ShardingConfiguration.SHARD_NUMBER_PROPERTY_NAME;
import static org.citrusframework.sharding.ShardingConfiguration.SHARD_SEED_ENV_VAR_NAME;
import static org.citrusframework.sharding.ShardingConfiguration.SHARD_SEED_PROPERTY_NAME;
import static org.citrusframework.sharding.ShardingConfiguration.TOTAL_SHARD_NUMBER_ENV_VAR_NAME;
import static org.citrusframework.sharding.ShardingConfiguration.TOTAL_SHARD_NUMBER_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.testng.Assert.expectThrows;

public class ShardingConfigurationTest {

    @Mock
    private SystemProvider systemProviderMock;

    @BeforeMethod
    void beforeMethodSetup() {
        openMocks(this);
    }

    @DataProvider
    private static Object[] shardNumbers() {
        return new Object[]{
                1, 2
        };
    }

    private static void assertProperties(ShardingConfiguration fixture, int totalNumberOfShards, int shardNumber, String seed) {
        assertEquals(totalNumberOfShards, getField(fixture, "totalNumberOfShards"));
        assertEquals(shardNumber, getField(fixture, "shardNumber"));
        assertEquals(seed, getField(fixture, "seed"));
    }

    @Test
    public void unshardedDefaultConfiguration() {
        var fixture = new ShardingConfiguration();
        assertProperties(fixture, 1, 0, "1");
    }

    @Test
    public void configureWithoutTotalNumberOfShards() {
        var exception = expectThrows(CitrusRuntimeException.class, () -> new ShardingConfiguration(0, 0));
        assertEquals("Number of total shards must be configured!", exception.getMessage());
    }

    @Test(dataProvider = "shardNumbers")
    public void configureWithNegativeShardNumber(int shardNumber) {
        var exception = expectThrows(CitrusRuntimeException.class, () -> new ShardingConfiguration(1, shardNumber * -1));
        assertEquals("Shard number cannot be negative!", exception.getMessage());
    }

    @Test(dataProvider = "shardNumbers")
    public void configureWithInvalidShardNumber(int shardNumber) {
        var exception = expectThrows(CitrusRuntimeException.class, () -> new ShardingConfiguration(1, shardNumber));
        assertEquals("Shard number must be less than the total number of shards!", exception.getMessage());
    }

    @Test
    public void withValidConfiguration() {
        int totalNumberOfShards = 22;
        int shardNumber = 11;

        var fixture = new ShardingConfiguration(totalNumberOfShards, shardNumber);

        assertProperties(fixture, totalNumberOfShards, shardNumber, String.valueOf(totalNumberOfShards));
    }

    @Test
    public void configurationFromEnvTakesPrecedenceOverProperties() {
        doReturn(Optional.of("44")).when(systemProviderMock).getEnv(TOTAL_SHARD_NUMBER_ENV_VAR_NAME);
        doReturn(Optional.of("33")).when(systemProviderMock).getEnv(SHARD_NUMBER_ENV_VAR_NAME);

        var seed = "seed-from-env";
        doReturn(Optional.of(seed)).when(systemProviderMock).getEnv(SHARD_SEED_ENV_VAR_NAME);

        var fixture = new ShardingConfiguration(systemProviderMock);

        assertProperties(fixture, 44, 33, seed);

        verify(systemProviderMock, never()).getProperty(anyString());
    }

    @Test
    public void configurationFromProperties() {
        doReturn(Optional.of("66")).when(systemProviderMock).getProperty(TOTAL_SHARD_NUMBER_PROPERTY_NAME);
        doReturn(Optional.of("55")).when(systemProviderMock).getProperty(SHARD_NUMBER_PROPERTY_NAME);

        var seed = "seed-from-property";
        doReturn(Optional.of(seed)).when(systemProviderMock).getProperty(SHARD_SEED_PROPERTY_NAME);

        var fixture = new ShardingConfiguration(systemProviderMock);

        assertProperties(fixture, 66, 55, seed);
    }

    @Test
    public void stringTotalNumberOfShardsNumberFormatException() {
        doReturn(Optional.of("non-numeric")).when(systemProviderMock).getEnv(TOTAL_SHARD_NUMBER_ENV_VAR_NAME);

        var exception = expectThrows(CitrusRuntimeException.class, () -> new ShardingConfiguration(systemProviderMock));
        assertEquals("Failed to calculate number of total shards, received string instead of number!", exception.getMessage());
    }

    @Test
    public void stringShardNumberFormatException() {
        doReturn(Optional.of("non-numeric")).when(systemProviderMock).getEnv(SHARD_NUMBER_ENV_VAR_NAME);

        var exception = expectThrows(CitrusRuntimeException.class, () -> new ShardingConfiguration(systemProviderMock));
        assertEquals("Failed to calculate shard number, received string instead of number!", exception.getMessage());
    }
}
