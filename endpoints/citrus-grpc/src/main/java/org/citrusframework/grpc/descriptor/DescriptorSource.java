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

package org.citrusframework.grpc.descriptor;

import java.util.List;

import com.google.protobuf.Descriptors.FileDescriptor;

/**
 * Provides Protobuf file descriptors from one configuration value (a
 * descriptor set file, a generated class or a {@code .proto} file).
 * Implementations load lazily on {@link #getDescriptors()} and raise a
 * {@link DescriptorRegistryException} naming the resource when loading fails.
 */
@FunctionalInterface
public interface DescriptorSource {

    /**
     * Loads the file descriptors this source provides. The returned files may
     * reference dependencies that the source itself does not return; dependency
     * resolution across sources is the registry's concern.
     * @return the file descriptors, never empty
     */
    List<FileDescriptor> getDescriptors();
}
