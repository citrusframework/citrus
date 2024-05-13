/** 
 * ==================================================
 * GENERATED CLASS, ALL CHANGES WILL BE LOST
 * ==================================================
 */

package org.citrusframework.openapi.generator.rest.petstore.spring;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import org.citrusframework.openapi.generator.rest.petstore.request.PetApi;
import org.citrusframework.openapi.generator.rest.petstore.request.StoreApi;
import org.citrusframework.openapi.generator.rest.petstore.request.UserApi;
import javax.annotation.processing.Generated;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen")
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
