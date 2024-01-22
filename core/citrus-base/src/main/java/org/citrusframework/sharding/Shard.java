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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static java.lang.Math.ceil;
import static java.lang.Math.min;
import static java.util.Arrays.stream;
import static java.util.Collections.shuffle;
import static java.util.stream.Collectors.toCollection;

/**
 * A utility class for implementing sharded test loading and execution, additionally to traditional test loading. It
 * enhances the citrus framework with the ability to divide test cases into shards. Their loading and execution is still
 * managed by the traditional test loaders and executors.
 * <p>
 * This class is part of the Citrus framework, designed to streamline the process of sharding test cases for efficient
 * and scalable testing.
 * <p>
 * Configuration happens via environment variables. See {@link ShardingConfiguration} for more information.
 *
 * @see ShardingConfiguration
 */
public final class Shard {

    private Shard() {
        throw new IllegalArgumentException("Utility class shall not be instantiated!");
    }

    /**
     * Creates a sharded stream from the input array using the default sharding configuration.
     *
     * @param <T>       The type of elements in the stream.
     * @param testCases The input array to be sharded.
     * @return A sharded stream based on the default sharding configuration.
     */
    public static <T> T[] createShard(T[] testCases) {
        return createShard(testCases, new ShardingConfiguration());
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T[] createShard(T[] testCases, ShardingConfiguration shardingConfiguration) {
        return createShard(stream(testCases), shardingConfiguration)
                .toList()
                .toArray(
                        size -> (T[]) java.lang.reflect.Array.newInstance(
                                testCases.getClass().getComponentType(), size)
                );
    }

    /**
     * Creates a sharded stream from the input stream using the default sharding configuration.
     * Note that the initial stream will be terminated!
     *
     * @param <T>       The type of elements in the stream.
     * @param testCases The input stream to be sharded.
     * @return A sharded stream based on the default sharding configuration.
     */
    public static <T> Stream<T> createShard(Stream<T> testCases) {
        return createShard(testCases, new ShardingConfiguration());
    }

    /**
     * Creates a sharded stream from the input stream using the provided sharding configuration.
     * Note that the initial stream will be terminated!
     *
     * @param <T>                   The type of elements in the stream.
     * @param testCases             The input stream to be sharded.
     * @param shardingConfiguration The configuration for sharding.
     * @return A sharded stream based on the provided sharding configuration.
     */
    public static <T> Stream<T> createShard(Stream<T> testCases, ShardingConfiguration shardingConfiguration) {
        return createShard(testCases, shardingConfiguration, false);
    }


    /**
     * Creates a sharded stream from the input stream using the provided sharding configuration and a flag to determine
     * whether the stream should be parallel.
     * Note that the initial stream will be terminated!
     *
     * @param <T>                   The type of elements in the stream.
     * @param testCases             The input stream to be sharded.
     * @param shardingConfiguration The configuration for sharding.
     * @param parallel              A flag indicating whether the resulting stream should be
     *                              parallel.
     * @return A sharded stream based on the provided sharding configuration.
     */
    public static <T> Stream<T> createShard(Stream<T> testCases, ShardingConfiguration shardingConfiguration, boolean parallel) {
        List<T> itemList = testCases.collect(toCollection(ArrayList::new));

        var random = new Random(shardingConfiguration.getSeed());
        shuffle(itemList, random);

        int shardSize = (int) ceil(itemList.size() / (double) shardingConfiguration.getTotalNumberOfShards());
        int startIndex = shardingConfiguration.getShardNumber() * shardSize;
        int endIndex = min(itemList.size(), startIndex + shardSize);

        var shardedItems = itemList.subList(startIndex, endIndex);

        if (parallel) {
            return shardedItems.parallelStream();
        } else {
            return shardedItems.stream();
        }
    }
}
