package nl.hu.husacct.plugin.sonarqube.util;

import nl.hu.husacct.plugin.sonarqube.exceptions.HusacctSonarException;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DummyImportFile {

    /*
        This function is needed because, HUSACCT expects an import file.
     */
    public String createImportFile(SensorContext context) {
        String baserDir = context.fileSystem().baseDir().getAbsolutePath();
        String fileName = "DummyImportFile.xml";

        try {
            PrintWriter writer = new PrintWriter(baserDir + fileName, "UTF-8");
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<report></report>");
            writer.close();
        } catch (IOException e) {
            throw new HusacctSonarException("Cannot create dummy import file." + e.getCause());
        }

        Loggers.get(getClass()).info("Created dummy import file");
        return baserDir + fileName;

    }

    public void removeDummyImportFile(String emptyImportFile) {
        try {
            Files.delete(Paths.get(emptyImportFile));
            Loggers.get(getClass()).info("Dummy import file has successfully deleted.");
        } catch (IOException e) {
            Loggers.get(getClass()).warn("Could not find dummy import file. Maybe it has been deleted manually?");
        }
    }
}
