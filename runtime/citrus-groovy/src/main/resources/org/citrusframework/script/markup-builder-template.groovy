import groovy.xml.MarkupBuilder

def writer = new StringWriter()
def markupBuilder = new MarkupBuilder(writer)

@SCRIPTBODY@

return writer.toString()
