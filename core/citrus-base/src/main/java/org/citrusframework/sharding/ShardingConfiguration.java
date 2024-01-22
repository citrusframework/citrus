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

import java.util.Optional;

import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

/**
 * A configuration class for sharded test loading and execution withing the citrus framework. It uses environment
 * variables and system properties to configure the sharding behavior.
 * <p>
 * This class is part of the Citrus framework, designed to streamline the process of sharding test cases for efficient
 * and scalable testing.
 * <p>
 * <h3>Configuration Example:</h3>
 * <p>To configure the sharding behavior, set the following environment variables or system properties:</p>
 * <ul>
 *   <li><b>Total number of shards:</b>
 *     <ul>
 *       <li>Environment Variable: <code>CITRUS_SHARDING_TOTAL</code></li>
 *       <li>System Property: <code>citrus.sharding.total</code></li>
 *       <li>Description: Specifies the total number of shards into which the test cases will be divided.</li>
 *     </ul>
 *   </li>
 *   <li><b>Shard number:</b>
 *     <ul>
 *       <li>Environment Variable: <code>CITRUS_SHARDING_NUMBER</code></li>
 *       <li>System Property: <code>citrus.sharding.number</code></li>
 *       <li>
 *           Description: Indicates the specific shard number of the current test loader. This should be a value between
 *           0 and the total number of shards minus one.
 *       </li>
 *     </ul>
 *   </li>
 *   <li><b>Shard seed:</b>
 *     <ul>
 *       <li>Environment Variable: <code>CITRUS_SHARDING_SEED</code></li>
 *       <li>System Property: <code>citrus.sharding.seed</code></li>
 *       <li>
 *           Description: Specifies a seed value used for shuffling test cases within a shard. Providing a consistent
 *           seed value ensures the same shuffling order across different executions.
 *       </li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <p>To configure a system with 4 total shards and assign this instance to shard number 1 (second shard, since
 * numbering starts at 0), set the environment variables or system properties as follows:</p>
 * <ul>
 *   <li>
 *       Set <code>CITRUS_SHARDING_TOTAL</code> or <code>citrus.sharding.total</code> to 4.
 *   </li>
 *   <li>
 *       Set <code>CITRUS_SHARDING_NUMBER</code> or <code>citrus.sharding.number</code> to 1.
 *   </li>
 *   <li>
 *       Optionally, set a seed for shuffling test cases using <code>CITRUS_SHARDING_SEED</code>
 *       or <code>citrus.sharding.seed</code>. The total number of shards will be used as see by default.
 *   </li>
 * </ul>
 *
 * @see Shard
 */
public final class ShardingConfiguration {

    public static final String TOTAL_SHARD_NUMBER_PROPERTY_NAME = "citrus.sharding.total";
    public static final String TOTAL_SHARD_NUMBER_ENV_VAR_NAME = TOTAL_SHARD_NUMBER_PROPERTY_NAME.replace(".", "_").toUpperCase();

    public static final String SHARD_NUMBER_PROPERTY_NAME = "citrus.sharding.number";
    public static final String SHARD_NUMBER_ENV_VAR_NAME = SHARD_NUMBER_PROPERTY_NAME.replace(".", "_").toUpperCase();

    public static final String SHARD_SEED_PROPERTY_NAME = "citrus.sharding.seed";
    public static final String SHARD_SEED_ENV_VAR_NAME = SHARD_SEED_PROPERTY_NAME.replace(".", "_").toUpperCase();

    private final int totalNumberOfShards;
    private final int shardNumber;
    private final String seed;

    /**
     * Default sharding configuration which initializes the sharding with system properties and environment variables.
     */
    public ShardingConfiguration() {
        this(new SystemProvider());
    }

    /**
     * Constructor that allows for injecting a custom {@link SystemProvider}. This is primarily intended for testing
     * purposes, enabling the mocking and overriding of system environment and properties.
     *
     * @param systemProvider a provider for system environment variables and properties.
     */
    protected ShardingConfiguration(SystemProvider systemProvider) {
        this(getTotalNumberOfShards(systemProvider), getShardNumber(systemProvider), systemProvider);
    }

    /**
     * Create a new sharding configuration with explicit total number of shards and shard number.
     *
     * @param totalNumberOfShards the total number of shards to be used.
     * @param shardNumber         the specific shard number for this loader, zero-based.
     */
    public ShardingConfiguration(int totalNumberOfShards, int shardNumber) {
        this(totalNumberOfShards, shardNumber, new SystemProvider());
    }

    /**
     * Constructor that sets the total number of shards, shard number, and allows for injecting a
     * custom {@link SystemProvider}. Primarily used for testing purposes.
     *
     * @param totalNumberOfShards the total number of shards.
     * @param shardNumber         the shard number for this loader, zero-based.
     * @param systemProvider      a provider for system environment variables and properties.
     */
    protected ShardingConfiguration(int totalNumberOfShards, int shardNumber, SystemProvider systemProvider) {
        this.totalNumberOfShards = totalNumberOfShards;
        this.shardNumber = shardNumber;

        seed = getSeedOrDefaultValue(systemProvider, totalNumberOfShards);

        sanitizeConfiguration();
    }

    private static int getTotalNumberOfShards(SystemProvider systemProvider) {
        return extractEnvOrProperty(systemProvider, TOTAL_SHARD_NUMBER_ENV_VAR_NAME, TOTAL_SHARD_NUMBER_PROPERTY_NAME, 1, "Failed to calculate number of total shards, received string instead of number!");
    }

    private static int getShardNumber(SystemProvider systemProvider) {
        return extractEnvOrProperty(systemProvider, SHARD_NUMBER_ENV_VAR_NAME, SHARD_NUMBER_PROPERTY_NAME, 0, "Failed to calculate shard number, received string instead of number!");
    }

    private static String getSeedOrDefaultValue(SystemProvider systemProvider, int totalNumberOfShards) {
        return extractEnvOrProperty(systemProvider, SHARD_SEED_ENV_VAR_NAME, SHARD_SEED_PROPERTY_NAME)
                .orElseGet(() -> valueOf(totalNumberOfShards));
    }

    private static int extractEnvOrProperty(SystemProvider systemProvider, String envVarName, String fallbackPropertyName, int defaultValue, String numberParseErrorMessage) {
        try {
            return parseInt(extractEnvOrProperty(systemProvider, envVarName, fallbackPropertyName)
                    .orElseGet(() -> valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            throw new CitrusRuntimeException(numberParseErrorMessage, e);
        }
    }

    private static Optional<String> extractEnvOrProperty(SystemProvider systemProvider, String envVarName, String fallbackPropertyName) {
        return systemProvider.getEnv(envVarName)
                .or(() -> systemProvider.getProperty(fallbackPropertyName));
    }

    private void sanitizeConfiguration() {
        if (totalNumberOfShards <= 0) {
            throw new CitrusRuntimeException("Number of total shards must be configured!");
        } else if (shardNumber < 0) {
            throw new CitrusRuntimeException("Shard number cannot be negative!");
        } else if (shardNumber >= totalNumberOfShards) {
            throw new CitrusRuntimeException("Shard number must be less than the total number of shards!");
        }
    }

    public int getTotalNumberOfShards() {
        return totalNumberOfShards;
    }

    public int getShardNumber() {
        return shardNumber;
    }

    public int getSeed() {
        return seed.hashCode();
    }
}
