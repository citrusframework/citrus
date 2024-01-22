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

import org.citrusframework.TestCase;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.stream.IntStream.range;
import static org.citrusframework.sharding.Shard.createShard;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ShardTest {

    @Mock
    private ShardingConfiguration shardingConfigurationMock;

    private List<TestCase> testCases;

    @BeforeMethod
    void beforeMethodSetup() {
        MockitoAnnotations.openMocks(this);

        testCases = createTestCases(10);
    }

    private static List<TestCase> createTestCases(int numberOfTestCases) {
        assertTrue(numberOfTestCases > 0);
        return range(0, numberOfTestCases)
                .mapToObj(i -> mock(TestCase.class))
                .toList();
    }

    @Test
    public void loadOneShard() {
        var shard1 = prepareAndLoadShard(1, 0);
        assertEquals(10, shard1.size());
    }

    @Test
    public void loadMultipleShards() {
        var shard1 = prepareAndLoadShard(2, 0);
        var shard2 = prepareAndLoadShard(2, 1);

        assertEquals(5, shard1.size());
        assertEquals(5, shard2.size());

        assertTrue(shard1.stream().noneMatch(shard2::contains));
    }

    @Test
    public void unequalDistributionOnOddNumberOfShards() {
        var shard1 = prepareAndLoadShard(3, 0);
        var shard2 = prepareAndLoadShard(3, 1);
        var shard3 = prepareAndLoadShard(3, 2);

        assertEquals(4, shard1.size());
        assertEquals(4, shard2.size());
        assertEquals(2, shard3.size());

        assertTrue(shard1.stream().noneMatch(shard2::contains));
        assertTrue(shard1.stream().noneMatch(shard3::contains));
        assertTrue(shard2.stream().noneMatch(shard3::contains));
    }

    @Test
    public void unequalDistributionOnOddNumberOfTestCases() {
        doReturn(2).when(shardingConfigurationMock).getTotalNumberOfShards();

        var oddNumberOfTestCases = createTestCases(11);

        doReturn(0).when(shardingConfigurationMock).getShardNumber();
        var shard1 = createShard(oddNumberOfTestCases.stream(), shardingConfigurationMock).toList();

        doReturn(1).when(shardingConfigurationMock).getShardNumber();
        var shard2 = createShard(oddNumberOfTestCases.stream(), shardingConfigurationMock).toList();

        assertEquals(6, shard1.size());
        assertEquals(5, shard2.size());

        assertTrue(shard1.stream().noneMatch(shard2::contains));
    }

    @Test
    public void sameSeedEquality() {
        doReturn(2).when(shardingConfigurationMock).getTotalNumberOfShards();
        doReturn(0).when(shardingConfigurationMock).getShardNumber();

        var testCases = createTestCases(10);

        // Create two sharded test loaders with same seed => should load same shard
        doReturn("first-seed".hashCode()).when(shardingConfigurationMock).getSeed();

        var shard1 = createShard(testCases.stream(), shardingConfigurationMock).toList();
        var shard2 = createShard(testCases.stream(), shardingConfigurationMock).toList();

        assertEquals(shard1, shard2);

        verify(shardingConfigurationMock, times(2)).getSeed();
        clearInvocations(shardingConfigurationMock);

        // Now switch the seed => new shards, unequal to first two
        doReturn("different-seed".hashCode()).when(shardingConfigurationMock).getSeed();

        var shard3 = createShard(testCases.stream(), shardingConfigurationMock).toList();
        var shard4 = createShard(testCases.stream(), shardingConfigurationMock).toList();

        assertNotEquals(shard1, shard3);
        assertNotEquals(shard2, shard3);
        assertNotEquals(shard1, shard4);
        assertNotEquals(shard2, shard4);

        assertEquals(shard3, shard4);

        verify(shardingConfigurationMock, times(2)).getSeed();
    }

    private List<TestCase> prepareAndLoadShard(int totalNumberOfShards, int shardNumber) {
        doReturn(totalNumberOfShards).when(shardingConfigurationMock).getTotalNumberOfShards();
        doReturn(shardNumber).when(shardingConfigurationMock).getShardNumber();

        return createShard(testCases.stream(), shardingConfigurationMock).toList();
    }
}
