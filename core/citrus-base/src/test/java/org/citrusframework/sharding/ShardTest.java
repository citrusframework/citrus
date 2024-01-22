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

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.citrusframework.TestCase;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Array;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
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
import static org.mockito.MockitoAnnotations.openMocks;
import static org.testng.AssertJUnit.assertFalse;

public class ShardTest {

    private static final List<TestCase> TEST_CASES = createTestCases(10);

    private static List<TestCase> createTestCases(int numberOfTestCases) {
        assertTrue(numberOfTestCases > 0);
        return range(0, numberOfTestCases)
                .mapToObj(i -> mock(TestCase.class))
                .toList();
    }

    @Mock
    protected ShardingConfiguration shardingConfigurationMock;

    @BeforeMethod
    public void beforeMethodSetup() {
        openMocks(this);
    }

    protected void configureShardingConfiguration(int totalNumberOfShards, int shardNumber) {
        doReturn(totalNumberOfShards).when(shardingConfigurationMock).getTotalNumberOfShards();
        doReturn(shardNumber).when(shardingConfigurationMock).getShardNumber();
    }

    protected <T> Pair<T, T> testMultipleShards(BiFunction<Integer, Integer, T> createShardWithConfiguration, Function<T, Integer> sizeExtractor) {
        var shard1 = createShardWithConfiguration.apply(2, 0);
        var shard2 = createShardWithConfiguration.apply(2, 1);

        assertEquals(5, sizeExtractor.apply(shard1));
        assertEquals(5, sizeExtractor.apply(shard2));

        return Pair.of(shard1, shard2);
    }

    protected <T> Triple<T, T, T> testUnequalDistributionOnOddNumberOfShards(BiFunction<Integer, Integer, T> createShardWithConfiguration, Function<T, Integer> sizeExtractor) {
        var shard1 = createShardWithConfiguration.apply(3, 0);
        var shard2 = createShardWithConfiguration.apply(3, 1);
        var shard3 = createShardWithConfiguration.apply(3, 2);

        assertEquals(4, sizeExtractor.apply(shard1));
        assertEquals(4, sizeExtractor.apply(shard2));
        assertEquals(2, sizeExtractor.apply(shard3));

        return Triple.of(shard1, shard2, shard3);
    }

    protected <T> Pair<T, T> testUnequalDistributionOnOddNumberOfTestCases(BiFunction<List<TestCase>, ShardingConfiguration, T> testCasesToResult, Function<T, Integer> sizeExtractor) {
        doReturn(2).when(shardingConfigurationMock).getTotalNumberOfShards();

        var oddNumberOfTestCases = createTestCases(11);

        doReturn(0).when(shardingConfigurationMock).getShardNumber();
        var shard1 = testCasesToResult.apply(oddNumberOfTestCases, shardingConfigurationMock);

        doReturn(1).when(shardingConfigurationMock).getShardNumber();
        var shard2 = testCasesToResult.apply(oddNumberOfTestCases, shardingConfigurationMock);

        assertEquals(6, sizeExtractor.apply(shard1));
        assertEquals(5, sizeExtractor.apply(shard2));

        return Pair.of(shard1, shard2);
    }

    public static class ArrayApiTest extends ShardTest {

        @Test
        public void unsharded() {
            var unsharded = createArrayShardWithShardingConfiguration(1, 0);
            assertEquals(10, unsharded.length);
        }

        @Test
        public void unshardedDefaultConfiguration() {
            var unsharded = createShard(TEST_CASES.toArray(new TestCase[0]));
            assertEquals(10, unsharded.length);
        }

        @Test
        public void withMultipleShards() {
            var shards = testMultipleShards(this::createArrayShardWithShardingConfiguration, Array::getLength);
            assertTrue(stream(shards.getLeft()).noneMatch(asList(shards.getRight())::contains));
        }

        @Test
        public void unequalDistributionOnOddNumberOfShards() {
            var shards = testUnequalDistributionOnOddNumberOfShards(this::createArrayShardWithShardingConfiguration, Array::getLength);

            assertTrue(stream(shards.getLeft()).noneMatch(asList(shards.getMiddle())::contains));
            assertTrue(stream(shards.getLeft()).noneMatch(asList(shards.getRight())::contains));
            assertTrue(stream(shards.getMiddle()).noneMatch(asList(shards.getRight())::contains));
        }

        @Test
        public void unequalDistributionOnOddNumberOfTestCases() {
            var shards = testUnequalDistributionOnOddNumberOfTestCases((testCases, shardingConfiguration) -> createShard(testCases.toArray(new TestCase[0]), shardingConfiguration), Array::getLength);
            assertTrue(stream(shards.getLeft()).noneMatch(asList(shards.getRight())::contains));
        }

        private TestCase[] createArrayShardWithShardingConfiguration(int totalNumberOfShards, int shardNumber) {
            configureShardingConfiguration(totalNumberOfShards, shardNumber);

            return createShard(TEST_CASES.toArray(new TestCase[0]), shardingConfigurationMock);
        }
    }

    public static class StreamApiTest extends ShardTest {

        @Test
        public void unsharded() {
            var unsharded = createStreamApiShardWithShardingConfiguration(1, 0);
            assertEquals(10, unsharded.size());
        }

        @Test
        public void unshardedDefaultConfiguration() {
            var unsharded = createShard(TEST_CASES.stream()).toList();
            assertEquals(10, unsharded.size());
        }

        @Test
        public void multipleShards() {
            var shards = testMultipleShards(this::createStreamApiShardWithShardingConfiguration, List::size);
            assertTrue(shards.getLeft().stream().noneMatch(shards.getRight()::contains));
        }

        @Test
        public void unequalDistributionOnOddNumberOfShards() {
            var shards = testUnequalDistributionOnOddNumberOfShards(this::createStreamApiShardWithShardingConfiguration, List::size);

            assertTrue(shards.getLeft().stream().noneMatch(shards.getMiddle()::contains));
            assertTrue(shards.getLeft().stream().noneMatch(shards.getRight()::contains));
            assertTrue(shards.getMiddle().stream().noneMatch(shards.getRight()::contains));
        }

        @Test
        public void unequalDistributionOnOddNumberOfTestCases() {
            var shards = testUnequalDistributionOnOddNumberOfTestCases((testCases, shardingConfiguration) -> createShard(testCases.stream(), shardingConfiguration).toList(), List::size);
            assertTrue(shards.getLeft().stream().noneMatch(shards.getRight()::contains));
        }

        @Test
        public void sameSeedEquality() {
            configureShardingConfiguration(2, 0);

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

        private List<TestCase> createStreamApiShardWithShardingConfiguration(int totalNumberOfShards, int shardNumber) {
            configureShardingConfiguration(totalNumberOfShards, shardNumber);

            var stream = createShard(TEST_CASES.stream(), shardingConfigurationMock);
            assertFalse(stream.isParallel());

            return stream.toList();
        }

        @Test
        public void parallelStream() {
            int numberOfTestCases = 10;
            var resultingStream = createShard(createTestCases(numberOfTestCases).stream(), new ShardingConfiguration(), true);
            assertTrue(resultingStream.isParallel());
            assertEquals(numberOfTestCases, resultingStream.toList().size());
        }
    }
}
