import org.citrusframework.TestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;

public class DelayTest implements Runnable, TestActionSupport {

    @CitrusResource
    TestActionRunner t;

    @Override
    public void run() {
        t.run(
            delay().milliseconds(200L)
        );
    }

}
