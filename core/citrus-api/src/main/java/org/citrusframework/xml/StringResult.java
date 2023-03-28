package org.citrusframework.xml;

import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * Helper representing a stream result that is written to a String writer.
 */
public class StringResult extends StreamResult {

    public StringResult() {
        super(new StringWriter());
    }

    public String toString() {
        return getWriter().toString();
    }
}
