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
 * @author roland
 * @since 02.10.12
 */
public class XmlMapper extends XStream {

    public XmlMapper() {
        super(getXppDriver());
        alias("ssh-request",SshRequest.class);
        alias("ssh-response",SshResponse.class);
    }

    private static XppDriver getXppDriver() {
        return new XppDriver() {
            public HierarchicalStreamWriter createWriter(Writer out) {
                return new PrettyPrintWriter(out) {
                    boolean cdata = false;
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
