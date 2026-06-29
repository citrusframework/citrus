package runnable;

import org.citrusframework.TestActionSupport;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;

public class SampleIT implements Runnable, TestActionSupport {

    @CitrusResource
    TestCaseRunner t;

    @Override
    public void run() {
        t.given(
            createVariables().variable("message", "Citrus rocks! With Java!")
        );

        t.then(
            echo().message("${message}")
        );
    }
}
