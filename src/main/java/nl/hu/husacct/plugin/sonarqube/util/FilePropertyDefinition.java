package nl.hu.husacct.plugin.sonarqube.util;

import org.sonar.api.config.PropertyDefinition;

public class FilePropertyDefinition {
    public static PropertyDefinition getPropertyDefinition() {
        return PropertyDefinition.builder("sonar.husacct.workspacePath")
                .name("HUSACCT file path")
                .description("Path to the husacct xml file")
                .build();
    }
}
