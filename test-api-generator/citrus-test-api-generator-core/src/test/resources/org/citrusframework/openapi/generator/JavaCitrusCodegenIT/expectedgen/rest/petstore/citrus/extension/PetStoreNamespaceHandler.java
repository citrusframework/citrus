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

package org.citrusframework.openapi.generator.rest.petstore.citrus.extension;

import org.citrusframework.openapi.generator.rest.petstore.request.PetApi;
import org.citrusframework.openapi.generator.rest.petstore.request.StoreApi;
import org.citrusframework.openapi.generator.rest.petstore.request.UserApi;
import org.citrusframework.openapi.generator.rest.petstore.citrus.PetStoreBeanDefinitionParser;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen", date = "2024-07-03T15:24:45.610010900+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class PetStoreNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("addPetRequest", new PetStoreBeanDefinitionParser(PetApi.AddPetRequest.class));
        registerBeanDefinitionParser("deletePetRequest", new PetStoreBeanDefinitionParser(PetApi.DeletePetRequest.class));
        registerBeanDefinitionParser("findPetsByStatusRequest", new PetStoreBeanDefinitionParser(PetApi.FindPetsByStatusRequest.class));
        registerBeanDefinitionParser("findPetsByTagsRequest", new PetStoreBeanDefinitionParser(PetApi.FindPetsByTagsRequest.class));
        registerBeanDefinitionParser("getPetByIdRequest", new PetStoreBeanDefinitionParser(PetApi.GetPetByIdRequest.class));
        registerBeanDefinitionParser("updatePetRequest", new PetStoreBeanDefinitionParser(PetApi.UpdatePetRequest.class));
        registerBeanDefinitionParser("updatePetWithFormRequest", new PetStoreBeanDefinitionParser(PetApi.UpdatePetWithFormRequest.class));
        registerBeanDefinitionParser("uploadFileRequest", new PetStoreBeanDefinitionParser(PetApi.UploadFileRequest.class));
        registerBeanDefinitionParser("deleteOrderRequest", new PetStoreBeanDefinitionParser(StoreApi.DeleteOrderRequest.class));
        registerBeanDefinitionParser("getInventoryRequest", new PetStoreBeanDefinitionParser(StoreApi.GetInventoryRequest.class));
        registerBeanDefinitionParser("getOrderByIdRequest", new PetStoreBeanDefinitionParser(StoreApi.GetOrderByIdRequest.class));
        registerBeanDefinitionParser("placeOrderRequest", new PetStoreBeanDefinitionParser(StoreApi.PlaceOrderRequest.class));
        registerBeanDefinitionParser("createUserRequest", new PetStoreBeanDefinitionParser(UserApi.CreateUserRequest.class));
        registerBeanDefinitionParser("createUsersWithArrayInputRequest", new PetStoreBeanDefinitionParser(UserApi.CreateUsersWithArrayInputRequest.class));
        registerBeanDefinitionParser("createUsersWithListInputRequest", new PetStoreBeanDefinitionParser(UserApi.CreateUsersWithListInputRequest.class));
        registerBeanDefinitionParser("deleteUserRequest", new PetStoreBeanDefinitionParser(UserApi.DeleteUserRequest.class));
        registerBeanDefinitionParser("getUserByNameRequest", new PetStoreBeanDefinitionParser(UserApi.GetUserByNameRequest.class));
        registerBeanDefinitionParser("loginUserRequest", new PetStoreBeanDefinitionParser(UserApi.LoginUserRequest.class));
        registerBeanDefinitionParser("logoutUserRequest", new PetStoreBeanDefinitionParser(UserApi.LogoutUserRequest.class));
        registerBeanDefinitionParser("updateUserRequest", new PetStoreBeanDefinitionParser(UserApi.UpdateUserRequest.class));
    }
}
