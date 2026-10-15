import org.citrusframework.TestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.TestActionSupport;

public class SetVariableTest implements Runnable, TestActionSupport {

    @CitrusResource
    TestActionRunner t;

    @CitrusResource
    TestContext context;

    @Override
    public void run() {
        context.setVariable("FIRST_TEST_MARKER", "leaked");
        t.run(echo().message("First test: set FIRST_TEST_MARKER"));
    }
}
