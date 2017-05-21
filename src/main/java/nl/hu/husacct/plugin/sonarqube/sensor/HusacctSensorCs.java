package nl.hu.husacct.plugin.sonarqube.sensor;


import nl.hu.husacct.plugin.sonarqube.exceptions.WorkspaceFileException;
import nl.hu.husacct.plugin.sonarqube.util.FilePredicates;
import nl.hu.husacct.plugin.sonarqube.util.xmlparser.HusacctPropertiesXmlParser;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Loggers;

import java.io.File;
import java.util.List;

public class HusacctSensorCs extends HusacctSensor{

    @Override
    protected String getFileSuffix() {
        return ".cs";
    }

    @Override
    protected List<String> getSourcePaths(SensorContext context) {
        return fileFinder.getAllCsSourcePaths(context.fileSystem());
    }

    @Override
    protected String getHusacctSaccFile(SensorContext context ) {
        xmlParser = new HusacctPropertiesXmlParser();
        String returnValue;
        File husacctProperties = context.fileSystem().inputFile(new FilePredicates.FileWithName("HUSACCT.properties.xml")).file();
        returnValue = xmlParser.getHussactWorkspaceFile(husacctProperties);

        if(returnValue != null) {
            return returnValue;
        }
        Loggers.get(getClass()).error("Cannot find HUSACCT file!");
        throw new WorkspaceFileException("Cannot find HUSACCT file!");
    }

    @Override
    protected String getLanguageKey() {
        return "cs";
    }
}
