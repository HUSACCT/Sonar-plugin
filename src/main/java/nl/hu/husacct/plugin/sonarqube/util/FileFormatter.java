package nl.hu.husacct.plugin.sonarqube.util;

import org.sonar.api.utils.log.Loggers;

import java.io.File;

public class FileFormatter {

    private FileFormatter() {}

    public static String formatFilePath(String file) {
        String returnValue = file.replace('.', File.separatorChar);
        returnValue = returnValue.replace('\\', File.separatorChar);
        returnValue = returnValue.replace('/', File.separatorChar);
        return returnValue;
    }

    public static String formatWorkspaceFile(String workspacePath) {
        String pathWithoutSuffix;
        if(workspacePath.endsWith(".xml")) {
            pathWithoutSuffix = workspacePath.substring(0, workspacePath.lastIndexOf('.'));
        } else {
            pathWithoutSuffix = workspacePath;
        }

        String formattedWorkspacePath = FileFormatter.formatFilePath(pathWithoutSuffix);

        String formattedWorkspaceFile = formattedWorkspacePath.substring(formattedWorkspacePath.lastIndexOf(File.separator)+1) + ".xml";
        Loggers.get("PomParser").debug(String.format("Found HUSACCT file: %s", formattedWorkspaceFile));
        return formattedWorkspaceFile;
    }
}
