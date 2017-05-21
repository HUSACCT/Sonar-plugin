package nl.hu.husacct.plugin.sonarqube.util.xmlparser;

import nl.hu.husacct.plugin.sonarqube.exceptions.WorkspaceFileException;
import org.sonar.api.utils.log.Loggers;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PomParser extends XmlParser {

    private static final String HUSACCTMAVENPLUGINARTIFACT = "husacct-maven-plugin";
    @Override
    protected String findWorkspaceFile(Element document) {
        String workspacePath;
        Element mavenPlugin = findMavenPlugin(document);
        if(mavenPlugin != null) {
            workspacePath = getWorkspaceContent(mavenPlugin);
            return workspacePath;
        }

        Element mavenProperties = findMavenProperties(document);
        if(mavenProperties != null) {
            workspacePath = getWorkspaceContent(mavenProperties);
            return workspacePath;
        }

        Loggers.get(getClass()).warn("Cannot find HUSACCT architecture file location inside pom.xml");
        throw new WorkspaceFileException("Cannot find HUSACCT arschitecture file!");
    }


    private Element findMavenProperties(Element document) {
        Loggers.get(getClass()).info("Start searching for HUSACCT architecture file in maven properties");
        NodeList allProperties = document.getElementsByTagName("properties");

        if(allProperties.getLength() == 1 ) {
            return (Element) allProperties.item(0);
        }
        if(allProperties.getLength() > 1) {
            for(int counter = 0; counter < allProperties.getLength(); counter++) {
                Node currentItem = allProperties.item(counter);
                if("project".equals(currentItem.getParentNode().getNodeName())) {
                    return (Element) currentItem;
                }
            }
        }
        Loggers.get(getClass()).warn("Cannot find maven properties!");
        return null;
    }

    private Element findMavenPlugin(Element document) {
        Loggers.get(getClass()).info("Start searching for HUSACCT architecture file in maven plug-in");

        NodeList allPlugins = document.getElementsByTagName("plugin");
        for(int counter = 0; counter < allPlugins.getLength(); counter++) {
            Element plugin = (Element) allPlugins.item(counter);
            Node artifact = plugin.getElementsByTagName("artifactId").item(0);
            if(artifact.getTextContent().equals(HUSACCTMAVENPLUGINARTIFACT)) {
                return plugin;
            }
        }
        String errorMessage = String.format("Cannot find the HUSACCT Maven plugin within the pom.xml file. artifactId: %s", HUSACCTMAVENPLUGINARTIFACT);
        Loggers.get(getClass()).warn(errorMessage);
        return null;
    }
}
