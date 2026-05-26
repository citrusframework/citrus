# Citrus :: Connectors :: OpenAPI Validator (shaded)

This artifact contains no Citrus sources. It bundles
[com.atlassian.oai:openapi-request-validator-core](https://github.com/atlassian/openapi-request-validator)
together with a relocated copy of `com.networknt:json-schema-validator` 2.x
(`org.citrusframework.openapi.shaded.com.networknt`).

`openapi-request-validator-core` is not compatible with `com.networknt:json-schema-validator` 3.x, see
[atlassian/openapi-request-validator#21](https://github.com/atlassian/openapi-request-validator/issues/21).
Shading allows the rest of Citrus to use `com.networknt:json-schema-validator` 3.x.

Please refer to the upstream project for the API documentation of `com.atlassian.oai.validator`.
