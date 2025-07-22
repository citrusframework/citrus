import org.citrusframework.TestActionSupport;
import org.citrusframework.TestActionRunner;
import org.citrusframework.annotations.CitrusResource;

public class JavaTest implements Runnable, TestActionSupport {

    @CitrusResource
    TestActionRunner t;

    @Override
    public void run() {
        t.run(
            echo().message("Hello Citrus!")
        );
    }

}
