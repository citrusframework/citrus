/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.generate;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public interface TestGenerator<T extends TestGenerator> {

    /**
     * Create tests with this generator.
     */
    void create();

    String getName();

    String getAuthor();

    String getDescription();

    UnitFramework getFramework();

    boolean isDisabled();

    String getTargetPackage();

    GeneratorMode getMode();

    T withMode(GeneratorMode mode);

    T withName(String name);

    T withDisabled(boolean disabled);

    T withAuthor(String author);

    T withDescription(String description);

    T usePackage(String targetPackage);

    T useSrcDirectory(String srcDirectory);

    T withFramework(UnitFramework framework);

    /**
     * Mode indicating test actor client or server. Based on this mode send and receive directions may differ accordingly.
     */
    enum GeneratorMode {
        CLIENT,
        SERVER
    }
}
