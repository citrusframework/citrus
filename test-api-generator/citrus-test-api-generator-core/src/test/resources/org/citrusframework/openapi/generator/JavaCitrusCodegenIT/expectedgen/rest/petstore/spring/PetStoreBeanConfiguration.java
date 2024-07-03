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

package org.citrusframework.openapi.generator.rest.petstore.spring;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import org.citrusframework.openapi.generator.rest.petstore.request.PetApi;
import org.citrusframework.openapi.generator.rest.petstore.request.StoreApi;
import org.citrusframework.openapi.generator.rest.petstore.request.UserApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen", date = "2024-07-03T15:24:45.610010900+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class PetStoreBeanConfiguration {

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public PetApi.AddPetRequest addPetRequest() {
        return new PetApi.AddPetRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public PetApi.DeletePetRequest deletePetRequest() {
        return new PetApi.DeletePetRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public PetApi.FindPetsByStatusRequest findPetsByStatusRequest() {
        return new PetApi.FindPetsByStatusRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public PetApi.FindPetsByTagsRequest findPetsByTagsRequest() {
        return new PetApi.FindPetsByTagsRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public PetApi.GetPetByIdRequest getPetByIdRequest() {
        return new PetApi.GetPetByIdRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public PetApi.UpdatePetRequest updatePetRequest() {
        return new PetApi.UpdatePetRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public PetApi.UpdatePetWithFormRequest updatePetWithFormRequest() {
        return new PetApi.UpdatePetWithFormRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public PetApi.UploadFileRequest uploadFileRequest() {
        return new PetApi.UploadFileRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public StoreApi.DeleteOrderRequest deleteOrderRequest() {
        return new StoreApi.DeleteOrderRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public StoreApi.GetInventoryRequest getInventoryRequest() {
        return new StoreApi.GetInventoryRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public StoreApi.GetOrderByIdRequest getOrderByIdRequest() {
        return new StoreApi.GetOrderByIdRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public StoreApi.PlaceOrderRequest placeOrderRequest() {
        return new StoreApi.PlaceOrderRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public UserApi.CreateUserRequest createUserRequest() {
        return new UserApi.CreateUserRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public UserApi.CreateUsersWithArrayInputRequest createUsersWithArrayInputRequest() {
        return new UserApi.CreateUsersWithArrayInputRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public UserApi.CreateUsersWithListInputRequest createUsersWithListInputRequest() {
        return new UserApi.CreateUsersWithListInputRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public UserApi.DeleteUserRequest deleteUserRequest() {
        return new UserApi.DeleteUserRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public UserApi.GetUserByNameRequest getUserByNameRequest() {
        return new UserApi.GetUserByNameRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public UserApi.LoginUserRequest loginUserRequest() {
        return new UserApi.LoginUserRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public UserApi.LogoutUserRequest logoutUserRequest() {
        return new UserApi.LogoutUserRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public UserApi.UpdateUserRequest updateUserRequest() {
        return new UserApi.UpdateUserRequest();
    }
}
