import org.citrusframework.TestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;

public class CheckIsolationTest implements Runnable, TestActionSupport {

    @CitrusResource
    TestActionRunner t;

    @CitrusResource
    TestContext context;

    @Override
    public void run() {
        if (context.getVariables().containsKey("FIRST_TEST_MARKER")) {
            throw new CitrusRuntimeException(
                    "Context leak detected: variable 'FIRST_TEST_MARKER' from previous test is present");
        }
        t.run(echo().message("Second test: context is properly isolated"));
    }
}
