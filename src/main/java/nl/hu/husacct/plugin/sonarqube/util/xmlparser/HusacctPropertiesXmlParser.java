package nl.hu.husacct.plugin.sonarqube.util.xmlparser;

import nl.hu.husacct.plugin.sonarqube.util.xmlparser.XmlParser;
import org.w3c.dom.Element;

public class HusacctPropertiesXmlParser extends XmlParser {

    @Override
    protected String findWorkspaceFile(Element document) {
        return getWorkspaceContent(document);
    }
}
