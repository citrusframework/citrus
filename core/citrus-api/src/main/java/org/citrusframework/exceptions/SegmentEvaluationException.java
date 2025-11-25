package org.citrusframework.exceptions;

public final class SegmentEvaluationException extends Exception {

    private final String renderedObject;

    public SegmentEvaluationException(String reason, String renderedObject) {
        super(reason);
        this.renderedObject = renderedObject;
    }

    public String getRenderedObject() {
        return renderedObject;
    }
}
