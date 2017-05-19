package nl.hu.husacct.plugin.sonarqube.util;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Loggers;

import java.io.File;
import java.util.ArrayList;

public class FileFinder {

    public ArrayList<String> getAllJavaSourcePaths(FileSystem fs) {
        ArrayList<String> javaPaths = new ArrayList<>();
        for (InputFile file : fs.inputFiles(fs.predicates().hasLanguage("java"))) {
            String path = file.path().toString().substring(0, file.path().toString().lastIndexOf(File.separator));
            if (!javaPaths.contains(path)) {
                javaPaths.add(path);
                Loggers.get(getClass()).debug(String.format("Found path: %s", path));
            }
        }
        return javaPaths;
    }

    public ArrayList<String> getAllCsSourcePaths(FileSystem fs) {
        ArrayList<String> javaPaths = new ArrayList<>();
        for (InputFile file : fs.inputFiles(fs.predicates().hasLanguage("cz"))) {
            String path = file.path().toString().substring(0, file.path().toString().lastIndexOf(File.separator));
            if (!javaPaths.contains(path)) {
                javaPaths.add(path);
                Loggers.get(getClass()).debug(String.format("Found path: %s", path));
            }
        }
        return javaPaths;
    }

    public InputFile getHUSACCTFile(SensorContext context, String fileName) {
        FileSystem fs = context.fileSystem();
        InputFile HUSACCTFile = fs.inputFile(new FilePredicates.fileWithName(fileName));
        Loggers.get(getClass()).debug(String.format("Found architecture file: %s", HUSACCTFile));
        return HUSACCTFile;
    }

    public Iterable<InputFile> getAllXmlFiles(SensorContext context) {
        FileSystem fs = context.fileSystem();
        Iterable<InputFile> xmlFiles = fs.inputFiles(new FilePredicates.XmlPredicate());
        for (InputFile input : xmlFiles) {
            Loggers.get(getClass()).debug(String.format("Found xml file: %s", input.file().getName()));
        }
        return xmlFiles;
    }
}
