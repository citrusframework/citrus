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

package org.citrusframework.openapi.generator.sample;

import java.util.HashMap;
import java.util.Map;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.generator.TestApiClientRequestActionBuilder;
import org.citrusframework.openapi.generator.rest.petstore.model.Pet;
import org.citrusframework.testapi.GeneratedApi;

@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen", date = "2024-07-20T08:47:39.378047600+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class PetApi implements GeneratedApi {

    public static final PetApi INSTANCE = new PetApi();

    public String getApiTitle() {
        return "OpenAPI Petstore";
    }

    public String getApiVersion() {
        return "1.0.0";
    }

    public String getApiPrefix() {
        return "PetStore";
    }

    private OpenApiSpecification openApiSpecification = null;

    public Map<String, String> getApiInfoExtensions() {
        Map<String, String> infoExtensionMap = new HashMap<>();
        infoExtensionMap.put("x-citrus-api-name", "petstore");
        infoExtensionMap.put("x-citrus-app", "PETS");
        return infoExtensionMap;
    }

    public static PetApi openApiPetStore(HttpClient httpClient) {

        return new PetApi();
    }

    private static OpenApiSpecification petApi() {
        // TODO implement me
        return null;
    }

    public AddPetActionBuilder addPet() {
        return new AddPetActionBuilder();
    }

    public DeletePetActionBuilder deletePet() {
        return new DeletePetActionBuilder();
    }

    public FindPetByStatusActionBuilder findPetsByStatus() {
        return new FindPetByStatusActionBuilder();
    }

    public FindPetsByTagsActionBuilder findPetsByTags() {
        return new FindPetsByTagsActionBuilder();
    }

    public GetPetByIdActionBuilder getPetById() {
        return new GetPetByIdActionBuilder();
    }

    public class AddPetActionBuilder extends PetStoreAbstractSendAction.Builder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/pet";

        private static final String OPERATION_NAME = "addPet";

        public AddPetActionBuilder() {
            super(openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
        }

        public AddPetActionBuilder withStatus(String status) {
            queryParam("status", status);
            return this;
        }

        public AddPetActionBuilder withPet(Pet pet) {
            // TODO: fix this
            getMessageBuilderSupport().body(pet.toString());
            return this;
        }

    }

    public class DeletePetActionBuilder extends TestApiClientRequestActionBuilder {

        private static final String METHOD = "DELETE";

        private static final String ENDPOINT = "/pet/{petId}";

        private static final String OPERATION_NAME = "deletePet";

        public DeletePetActionBuilder() {
            super(openApiSpecification, METHOD, ENDPOINT, OPERATION_NAME);
        }

        public DeletePetActionBuilder withId(String id) {
            pathParameter("id", id);
            return this;
        }

        public DeletePetActionBuilder withPet(Pet pet) {
            // TODO: fix this pet.toString will not properly work
            getMessageBuilderSupport().body(pet.toString());
            return this;
        }

    }

    public static class FindPetByStatusActionBuilder extends PetStoreAbstractSendAction.Builder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/pet/findByStatus";

        private static final String OPERATION_NAME = "findPetsByStatus";

        public FindPetByStatusActionBuilder() {
            super(PetApi.petApi(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        public FindPetByStatusActionBuilder withStatus(String status) {
            queryParam("status", status);
            return this;
        }

    }

    public static class FindPetsByTagsActionBuilder extends PetStoreAbstractSendAction.Builder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/pet/findByTags";

        private static final String OPERATION_NAME = "findPetsByTags";

        public FindPetsByTagsActionBuilder() {
            super(PetApi.petApi(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        public FindPetsByTagsActionBuilder withTags(String... tags) {
            queryParam("tags", toQueryParam(tags));
            return this;
        }
    }

    public static class GetPetByIdActionBuilder extends PetStoreAbstractSendAction.Builder {

        private static final String METHOD = "GET";

        private static final String ENDPOINT = "/pet/{petId}";

        private static final String OPERATION_NAME = "getPetById";

        public GetPetByIdActionBuilder() {
            super(PetApi.petApi(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        public GetPetByIdActionBuilder withId(String id) {
            pathParameter("id", id);
            return this;
        }



        // TODO: find solution for authentication
//        public GetPetByIdActionBuilder withBasicUsername(String basicUsername) {
//            this.basicUsername = basicUsername;
//            return this;
//        }
//
//        public GetPetByIdActionBuilder withBasicPassword(String basicPassword) {
//            this.basicPassword = basicPassword;
//            return this;
//        }
    }

    public static class UpdatePetActionBuilder extends PetStoreAbstractSendAction.Builder {

        private static final String METHOD = "PUT";

        private static final String ENDPOINT = "/pet";

        private static final String OPERATION_NAME = "updatePet";

        public UpdatePetActionBuilder() {
            super(PetApi.petApi(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        public UpdatePetActionBuilder withId(String id) {
            pathParameter("id", id);
            return this;
        }

        public UpdatePetActionBuilder withPet(Pet pet) {
            // TODO: fix this pet.toString
            getMessageBuilderSupport().body(pet.toString());
            return this;
        }
    }

    public static class UpdatePetWithFormDataActionBuilder extends
        PetStoreAbstractSendAction.Builder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/pet/{petId}";

        private static final String OPERATION_NAME = "updatePetWithForm";

        public UpdatePetWithFormDataActionBuilder() {
            super(PetApi.petApi(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        public UpdatePetWithFormDataActionBuilder withId(String id) {
            pathParameter("id", id);
            return this;
        }

        // TODO: what is the magic about form data request?
    }

    public static class UploadFileActionBuilder extends PetStoreAbstractSendAction.Builder {

        private static final String METHOD = "POST";

        private static final String ENDPOINT = "/pet/{petId}/uploadImage";

        private static final String OPERATION_NAME = "uploadImage";

        public UploadFileActionBuilder() {
            super(PetApi.petApi(), METHOD, ENDPOINT, OPERATION_NAME);
        }

        public UploadFileActionBuilder withId(String id) {
            pathParameter("id", id);
            return this;
        }

        public UploadFileActionBuilder withAdditionalMetadata(String additionalMetadata) {

            // TODO: what is the magic about form data request?
            formData("additionalMetadata", additionalMetadata);
            return this;
        }
    }



}
