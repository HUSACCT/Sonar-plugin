package nl.hu.husacct.plugin.sonarqube.util;

import java.io.File;

public class FileFormatter {

    public static String formatFilePath(String file) {
        String returnValue = file.replace('.', File.separatorChar);
        returnValue = returnValue.replace('\\', File.separatorChar);
        returnValue = returnValue.replace('/', File.separatorChar);
        return returnValue;
    }
}
