TODO
TODO: document properties for security and others
TODO: document default properties for the endpoint -> prefix with lowercase 

## Generated Java Code for API Testing

The code generator creates Java classes from an OpenAPI specification to facilitate API testing using the Citrus framework. 
The Java classes represent Citrus Send and Receive ActionBuilders for each operation of the OpenAPI. Each builder provides
setter for the actual operation parameters. In general a type-safe method is provided, that reflects the correct operation type.
In addition a setter in string representation is provided to allow for citrus dynamic content setting using string expressions.
For each builder a specific operation is added to ...
A type-safe method providing the correct java type and a non type-safe method using a string type, allowing the usage of citrus expressions for the respective content.

1. **Type-Safe Parameter Method**: This method accepts the required parameters directly.
2. **String-Based Parameter Method**: This method accepts parameters as strings to allow for dynamic content.

Method names of non type-safe methods are prepended with a `$`. This is mainly to avoid conflicts for 

### Structure of the Generated API Class

For each API operation, the generated class includes:

1. **Builder with Type-Safe Required Parameters**: A method that takes the required parameters directly and returns a builder configured with these parameters.

2. **Builder with Parameters as Strings**: A method that takes parameters as strings, allowing dynamic replacements via the Citrus framework. The method name is suffixed with `$` to distinguish it from the type-safe version.

### Example

Consider an operation to delete a pet with the following parameter:
- `petId` (required, type `Long`)

The generated Java API class might look like this:

```java
public class PetsApi {

    /**
     * Builder with type safe required parameters.
     */
    public DeletePetRequestActionBuilder sendDeletePet(Long petId) {
        DeletePetRequestActionBuilder builder = new DeletePetRequestActionBuilder(openApiSpecification, petId);
        builder.endpoint(httpClient);
        return builder;
    }

    /**
     * Builder with required parameters as string, allowing dynamic content using citrus expressions.
     */
    public DeletePetRequestActionBuilder sendDeletePet$(String petIdExpression) {
        DeletePetRequestActionBuilder builder = new DeletePetRequestActionBuilder(petIdExpression, openApiSpecification);
        builder.endpoint(httpClient);
        return builder;
    }
}
```

## Known issues

## Validation

It is known, that the used OpenAPI validator is not able to validate certain situations.
E.g. certain array encoding situations related to object encoding 

## Variable processing

Processing of variables in case of parameter serialization, in some cases causes problems. For example, it  
is not possible to assign a json string to a variable and path it into an object into the current 
parameter serialization mechanism. This expects a real json, which cannot be resolved. To solve this issue,
serialization of arrays must happen as late as possible. Maybe it is feasible to create an OpenApiEndpointConfiguration
with a respective message converter.

## Handling of Array Parameters

Currently, all array parameters are handled in explode mode, regardless of the `explode` setting specified
in the API definition. This means that each item in an array will be serialized as a separate query 
parameter, even if the `explode` setting is set to `false` in the OpenAPI specification.

### Example

Suppose the OpenAPI specification defines an array parameter named `status` with the following attributes:

```yaml
parameters:
  - name: status
    in: query
    description: Status values that need to be considered for filter
    required: false
    explode: false
    schema:
      type: string
      default: available
      enum:
        - available
        - pending
        - sold
```

Despite the explode: false setting, the request will be serialized as follows:

```
?status=available&status=pending&status=sold
```