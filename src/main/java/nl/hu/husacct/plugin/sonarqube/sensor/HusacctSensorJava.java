package nl.hu.husacct.plugin.sonarqube.sensor;

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
     * Retrieve the set workspace path in property sonar.husacct.workspacePath
     *
     * @return workspace path
     */
    @Override
    protected String getHusacctSaccFile(SensorContext context) {
        return context.settings().getString("sonar.husacct.workspacePath");
    }
}

