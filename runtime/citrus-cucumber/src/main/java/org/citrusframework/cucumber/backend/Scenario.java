package org.citrusframework.cucumber.backend;

import java.net.URI;
import java.util.Collection;

import io.cucumber.core.backend.TestCaseState;
import io.cucumber.java.Status;

/**
 * @author Christoph Deppisch
 */
public final class Scenario {

    private final TestCaseState delegate;

    Scenario(TestCaseState delegate) {
        this.delegate = delegate;
    }

    public Collection<String> getSourceTagNames() {
        return delegate.getSourceTagNames();
    }

    public Status getStatus() {
        return Status.valueOf(delegate.getStatus().name());
    }

    public boolean isFailed() {
        return delegate.isFailed();
    }

    public String getName() {
        return delegate.getName();
    }

    public String getId() {
        return delegate.getId();
    }

    public URI getUri() {
        return delegate.getUri();
    }

    public Integer getLine() {
        return delegate.getLine();
    }
}
