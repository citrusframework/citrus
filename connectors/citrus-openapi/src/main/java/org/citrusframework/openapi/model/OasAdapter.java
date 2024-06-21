package org.citrusframework.openapi.model;

import io.apicurio.datamodels.core.models.Node;

public class OasAdapter<S extends Node, T> {

    private final S node;

    private final T adapted;

    public OasAdapter(S node, T adapted) {
        this.node = node;
        this.adapted = adapted;
    }

    public S getNode() {
        return node;
    }

    public T getAdapted() {
        return adapted;
    }

}
