package com.consol.citrus.ssh;

import java.io.Writer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

/**
 * Simple class for doing XML mappings via XStream, putting the content
 * into CDATA sections
 *
 * @author Roland Huss
 * @since 02.10.12
 */
public class XmlMapper extends XStream {

    /**
     * Default constructor.
     */
    public XmlMapper() {
        super(getXppDriver());
        alias("ssh-request",SshRequest.class);
        alias("ssh-response",SshResponse.class);
    }

    /**
     * Provides driver with cdata support.
     * @return
     */
    private static XppDriver getXppDriver() {
        return new XppDriver() {
            public HierarchicalStreamWriter createWriter(Writer out) {
                return new PrettyPrintWriter(out) {
                    boolean cdata = false;
                    @SuppressWarnings("rawtypes")
                    public void startNode(String name, Class clazz){
                        super.startNode(name, clazz);
                        cdata = (name.equals("command") ||
                                 name.equals("stdin")  ||
                                 name.equals("stdout") ||
                                 name.equals("stderr"));
                    }
                    protected void writeText(QuickWriter writer, String text) {
                        if(cdata) {
                            writer.write("<![CDATA[");
                            writer.write(text);
                            writer.write("]]>");
                        } else {
                            writer.write(text);
                        }
                    }
                };
            }
        };
    }
}
