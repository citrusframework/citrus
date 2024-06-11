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

package org.citrusframework.openapi;

import java.util.ArrayList;
import java.util.List;
import org.citrusframework.repository.BaseRepository;
import org.citrusframework.spi.Resource;

/**
 * OpenApi repository holding a set of {@link OpenApiSpecification} known in the test scope.
 * @since 4.4.0
 */
public class OpenApiRepository extends BaseRepository {

    private static final String DEFAULT_NAME = "openApiSchemaRepository";

    /** List of schema resources */
    private final List<OpenApiSpecification> openApiSpecifications = new ArrayList<>();


    /** An optional context path, used for each api, without taking into account any {@link OpenApiSpecification} specific context path. */
    private String rootContextPath;

    public OpenApiRepository() {
        super(DEFAULT_NAME);
    }

    public String getRootContextPath() {
        return rootContextPath;
    }

    public void setRootContextPath(String rootContextPath) {
        this.rootContextPath = rootContextPath;
    }

    @Override
    public void addRepository(Resource openApiResource) {

        OpenApiSpecification openApiSpecification = OpenApiSpecification.from(openApiResource);
        openApiSpecification.setRootContextPath(rootContextPath);

        this.openApiSpecifications.add(openApiSpecification);

        OpenApiSpecificationProcessor.lookup().values().forEach(processor -> processor.process(openApiSpecification));
    }

    public List<OpenApiSpecification> getOpenApiSpecifications() {
        return openApiSpecifications;
    }

}
