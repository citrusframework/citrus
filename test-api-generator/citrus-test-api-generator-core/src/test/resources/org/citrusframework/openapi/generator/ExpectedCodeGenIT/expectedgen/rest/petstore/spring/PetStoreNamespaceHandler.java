package org.citrusframework.openapi.generator.rest.petstore.spring;

import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.testapi.RestApiSendMessageActionBuilder;
import org.citrusframework.openapi.testapi.RestApiReceiveMessageActionBuilder;
import org.citrusframework.openapi.generator.rest.petstore.request.PetApi;
import org.citrusframework.openapi.generator.rest.petstore.request.StoreApi;
import org.citrusframework.openapi.generator.rest.petstore.request.UserApi;
import org.citrusframework.openapi.testapi.spring.RestApiReceiveMessageActionParser;
import org.citrusframework.openapi.testapi.spring.RestApiSendMessageActionParser;
import org.citrusframework.openapi.generator.rest.petstore.PetStore;
import org.citrusframework.openapi.testapi.GeneratedApi;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2024-10-28T13:20:43.309584600+01:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class PetStoreNamespaceHandler extends NamespaceHandlerSupport {

    private final OpenApiSpecification openApiSpecification = OpenApiSpecification.from(
        PetStore.petStoreApi());

    @Override
    public void init() {

            registerOperationParsers(PetApi.class,"add-pet", "addPet", "/pet",
                PetApi.AddPetSendActionBuilder.class,
                PetApi.AddPetReceiveActionBuilder.class,
                new String[]{  },
            new String[]{  });

            registerOperationParsers(PetApi.class,"delete-pet", "deletePet", "/pet/{petId}",
                PetApi.DeletePetSendActionBuilder.class,
                PetApi.DeletePetReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{ "apiKey" });

            registerOperationParsers(PetApi.class,"find-pets-by-status", "findPetsByStatus", "/pet/findByStatus",
                PetApi.FindPetsByStatusSendActionBuilder.class,
                PetApi.FindPetsByStatusReceiveActionBuilder.class,
                new String[]{  },
            new String[]{ "status" });

            registerOperationParsers(PetApi.class,"find-pets-by-tags", "findPetsByTags", "/pet/findByTags",
                PetApi.FindPetsByTagsSendActionBuilder.class,
                PetApi.FindPetsByTagsReceiveActionBuilder.class,
                new String[]{  },
            new String[]{ "tags" });

            registerOperationParsers(PetApi.class,"get-pet-by-id", "getPetById", "/pet/{petId}",
                PetApi.GetPetByIdSendActionBuilder.class,
                PetApi.GetPetByIdReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{ "apiKey" });

            registerOperationParsers(PetApi.class,"update-pet", "updatePet", "/pet",
                PetApi.UpdatePetSendActionBuilder.class,
                PetApi.UpdatePetReceiveActionBuilder.class,
                new String[]{  },
            new String[]{  });

            registerOperationParsers(PetApi.class,"update-pet-with-form", "updatePetWithForm", "/pet/{petId}",
                PetApi.UpdatePetWithFormSendActionBuilder.class,
                PetApi.UpdatePetWithFormReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{ "_name", "status" });

            registerOperationParsers(PetApi.class,"upload-file", "uploadFile", "/pet/{petId}/uploadImage",
                PetApi.UploadFileSendActionBuilder.class,
                PetApi.UploadFileReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{ "additionalMetadata", "ERROR_UNKNOWN" });

            registerOperationParsers(StoreApi.class,"delete-order", "deleteOrder", "/store/order/{orderId}",
                StoreApi.DeleteOrderSendActionBuilder.class,
                StoreApi.DeleteOrderReceiveActionBuilder.class,
                new String[]{ "orderId" },
            new String[]{  });

            registerOperationParsers(StoreApi.class,"get-inventory", "getInventory", "/store/inventory",
                StoreApi.GetInventorySendActionBuilder.class,
                StoreApi.GetInventoryReceiveActionBuilder.class,
                new String[]{  },
            new String[]{ "apiKey" });

            registerOperationParsers(StoreApi.class,"get-order-by-id", "getOrderById", "/store/order/{orderId}",
                StoreApi.GetOrderByIdSendActionBuilder.class,
                StoreApi.GetOrderByIdReceiveActionBuilder.class,
                new String[]{ "orderId" },
            new String[]{  });

            registerOperationParsers(StoreApi.class,"place-order", "placeOrder", "/store/order",
                StoreApi.PlaceOrderSendActionBuilder.class,
                StoreApi.PlaceOrderReceiveActionBuilder.class,
                new String[]{  },
            new String[]{ "ERROR_UNKNOWN" });

            registerOperationParsers(UserApi.class,"create-user", "createUser", "/user",
                UserApi.CreateUserSendActionBuilder.class,
                UserApi.CreateUserReceiveActionBuilder.class,
                new String[]{  },
            new String[]{ "ERROR_UNKNOWN" });

            registerOperationParsers(UserApi.class,"create-users-with-list-input", "createUsersWithListInput", "/user/createWithList",
                UserApi.CreateUsersWithListInputSendActionBuilder.class,
                UserApi.CreateUsersWithListInputReceiveActionBuilder.class,
                new String[]{  },
            new String[]{ "user" });

            registerOperationParsers(UserApi.class,"delete-user", "deleteUser", "/user/{username}",
                UserApi.DeleteUserSendActionBuilder.class,
                UserApi.DeleteUserReceiveActionBuilder.class,
                new String[]{ "username" },
            new String[]{  });

            registerOperationParsers(UserApi.class,"get-user-by-name", "getUserByName", "/user/{username}",
                UserApi.GetUserByNameSendActionBuilder.class,
                UserApi.GetUserByNameReceiveActionBuilder.class,
                new String[]{ "username" },
            new String[]{  });

            registerOperationParsers(UserApi.class,"login-user", "loginUser", "/user/login",
                UserApi.LoginUserSendActionBuilder.class,
                UserApi.LoginUserReceiveActionBuilder.class,
                new String[]{  },
            new String[]{ "username", "password" });

            registerOperationParsers(UserApi.class,"logout-user", "logoutUser", "/user/logout",
                UserApi.LogoutUserSendActionBuilder.class,
                UserApi.LogoutUserReceiveActionBuilder.class,
                new String[]{  },
            new String[]{  });

            registerOperationParsers(UserApi.class,"update-user", "updateUser", "/user/{username}",
                UserApi.UpdateUserSendActionBuilder.class,
                UserApi.UpdateUserReceiveActionBuilder.class,
                new String[]{ "username" },
            new String[]{ "ERROR_UNKNOWN" });
    }

    private void registerOperationParsers(Class<? extends GeneratedApi> apiClass, String elementName, String operationName, String path,
        Class<? extends RestApiSendMessageActionBuilder> sendBeanClass,
        Class<? extends RestApiReceiveMessageActionBuilder> receiveBeanClass,
        String[] constructorParameters,
        String[] nonConstructorParameters) {

        RestApiSendMessageActionParser sendParser = new RestApiSendMessageActionParser(openApiSpecification, operationName,
            path,
            apiClass,
            sendBeanClass,
            receiveBeanClass,
            "petstore.endpoint");
        sendParser.setConstructorParameters(constructorParameters);
        sendParser.setNonConstructorParameters(nonConstructorParameters);
        registerBeanDefinitionParser("send-"+elementName, sendParser);

        RestApiReceiveMessageActionParser receiveParser = new RestApiReceiveMessageActionParser(openApiSpecification,
        operationName, apiClass, receiveBeanClass, "petstore.endpoint");
        registerBeanDefinitionParser("receive-"+elementName, receiveParser);
    }
}
