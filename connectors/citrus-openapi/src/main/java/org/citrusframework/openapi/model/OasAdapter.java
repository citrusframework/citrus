package org.citrusframework.openapi.model;

import io.apicurio.datamodels.core.models.Node;

public record OasAdapter<S extends Node, T>(S node, T adapted) {

}
