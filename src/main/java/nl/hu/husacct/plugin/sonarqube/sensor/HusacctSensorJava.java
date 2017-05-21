package nl.hu.husacct.plugin.sonarqube.sensor;

import nl.hu.husacct.plugin.sonarqube.exceptions.WorkspaceFileException;
import nl.hu.husacct.plugin.sonarqube.util.FileFinder;
import nl.hu.husacct.plugin.sonarqube.util.xmlparser.PomParser;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

import java.util.List;

/**
 * must be activated in the Quality profile.
 */
public class HusacctSensorJava extends HusacctSensor {

    @Override
    protected String getLanguageKey() {
        return "java";
    }

    @Override
    protected String getFileSuffix() {
        return ".java";
    }

    @Override
    protected List<String> getSourcePaths(SensorContext context) {
        return fileFinder.getAllJavaSourcePaths(context.fileSystem());
    }

    /**
     * Searches the HUSACCT workspace file fromd different sources
     * for java: pom.xml (HUSACCT maven plugin and properties)
     * @return absolute path to the file
     */
    @Override
    protected String getHusacctSaccFile(SensorContext context) {
        xmlParser = new PomParser();
        String returnValue;
        FileFinder fF = new FileFinder();

        InputFile pomXml = fF.getXmlFile(context, "pom.xml");
        returnValue = xmlParser.getHussactWorkspaceFile(pomXml.file());

        if(returnValue == null) {
            throw new WorkspaceFileException("Cannot find Husacct Architecture file!");
        }

        return returnValue;
    }
}

