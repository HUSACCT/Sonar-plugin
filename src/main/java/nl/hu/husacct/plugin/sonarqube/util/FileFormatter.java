package nl.hu.husacct.plugin.sonarqube.util;

import org.sonar.api.utils.log.Loggers;

import java.io.File;

public class FileFormatter {

    public static String formatFilePath(String file) {
        String returnValue = file.replace('.', File.separatorChar);
        returnValue = returnValue.replace('\\', File.separatorChar);
        returnValue = returnValue.replace('/', File.separatorChar);
        return returnValue;
    }

    public static String formatWorkspaceFile(String workspacePath) {
        if(workspacePath.endsWith(".xml")) {
            workspacePath = workspacePath.substring(0, workspacePath.lastIndexOf('.'));
        }

        String formattedWorkspacePath = FileFormatter.formatFilePath(workspacePath);

        String formattedWorkspaceFile = formattedWorkspacePath.substring(formattedWorkspacePath.lastIndexOf(File.separator)+1) + ".xml";
        Loggers.get("PomParser").debug(String.format("Found HUSACCT file: %s", formattedWorkspaceFile));
        return formattedWorkspaceFile;
    }
}
