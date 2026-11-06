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

package org.citrusframework.poc.mcp;

/**
 * Framework-specific host adapter. The Citrus probe owns the bridge and the host;
 * the host owns only framework resources for its generation.
 *
 * <p>Contract: configuration construction performs no I/O. {@link #start} returns
 * a ready handle; startup failure cleans partial resources and preserves its cause.
 * {@link #stop} closes only owned resources and is safe to repeat. Restart allocates
 * a new host/context and generation.
 */
public interface PocHost extends AutoCloseable {

    /** Starts the framework host for the given config generation. Repeated start while running is a no-op. */
    HostHandle start(PocConfig config, CitrusPocEndpoint bridge) throws Exception;

    /** Stops only owned resources. Safe to repeat. */
    void stop() throws Exception;

    /** Current lifecycle state of this host instance. */
    LifecycleState state();

    @Override
    default void close() throws Exception {
        stop();
    }
}
