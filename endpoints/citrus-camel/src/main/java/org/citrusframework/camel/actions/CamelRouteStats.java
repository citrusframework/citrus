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

package org.citrusframework.camel.actions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model object representing the statistics produced by
 * {@code ManagedPerformanceCounterMBean#dumpStatsAsJSon(boolean)}.
 * Instances are deserialized from the JSON returned by that MBean method and
 * passed to the user-supplied {@code BiConsumer} validator in
 * {@link CamelVerifyRouteStatsAction}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CamelRouteStats {

    @JsonProperty("exchangesCompleted")
    private long exchangesCompleted;

    @JsonProperty("exchangesFailed")
    private long exchangesFailed;

    @JsonProperty("exchangesTotal")
    private long exchangesTotal;

    @JsonProperty("exchangesInflight")
    private long exchangesInflight;

    @JsonProperty("meanProcessingTime")
    private long meanProcessingTime;

    @JsonProperty("minProcessingTime")
    private long minProcessingTime;

    @JsonProperty("maxProcessingTime")
    private long maxProcessingTime;

    @JsonProperty("totalProcessingTime")
    private long totalProcessingTime;

    @JsonProperty("lastProcessingTime")
    private long lastProcessingTime;

    @JsonProperty("deltaProcessingTime")
    private long deltaProcessingTime;

    @JsonProperty("lastExchangeCompletedTimestamp")
    private String lastExchangeCompletedTimestamp;

    @JsonProperty("lastExchangeFailureTimestamp")
    private String lastExchangeFailureTimestamp;

    public long getExchangesCompleted() {
        return exchangesCompleted;
    }

    public long getExchangesFailed() {
        return exchangesFailed;
    }

    public long getExchangesTotal() {
        return exchangesTotal;
    }

    public long getExchangesInflight() {
        return exchangesInflight;
    }

    public long getMeanProcessingTime() {
        return meanProcessingTime;
    }

    public long getMinProcessingTime() {
        return minProcessingTime;
    }

    public long getMaxProcessingTime() {
        return maxProcessingTime;
    }

    public long getTotalProcessingTime() {
        return totalProcessingTime;
    }

    public long getLastProcessingTime() {
        return lastProcessingTime;
    }

    public long getDeltaProcessingTime() {
        return deltaProcessingTime;
    }

    public String getLastExchangeCompletedTimestamp() {
        return lastExchangeCompletedTimestamp;
    }

    public String getLastExchangeFailureTimestamp() {
        return lastExchangeFailureTimestamp;
    }
}
