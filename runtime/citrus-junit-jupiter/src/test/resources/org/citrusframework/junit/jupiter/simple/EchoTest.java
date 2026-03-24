import org.citrusframework.TestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;

public class EchoTest implements Runnable, TestActionSupport {

    @CitrusResource
    TestActionRunner t;

    @Override
    public void run() {
        t.run(
            echo().message("Hello Citrus!")
        );
    }

}
