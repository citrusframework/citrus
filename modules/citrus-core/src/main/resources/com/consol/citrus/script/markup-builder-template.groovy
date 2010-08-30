import groovy.xml.MarkupBuilder


def writer = new StringWriter()
def xml = new MarkupBuilder(writer)

@SCRIPTBODY@

return writer.toString()