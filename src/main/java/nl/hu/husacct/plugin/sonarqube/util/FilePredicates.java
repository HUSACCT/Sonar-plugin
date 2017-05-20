package nl.hu.husacct.plugin.sonarqube.util;


import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;

public class FilePredicates {

    private FilePredicates() {}

    public static class XmlPredicate implements FilePredicate {

        @Override
        public boolean apply(InputFile inputFile) {
            if(inputFile.file().getName().endsWith(".xml")) {
                return true;
            }
            return false;
        }
    }

    public static class JavaFileWithPath implements  FilePredicate {
        private String absolutePath;
        public JavaFileWithPath(String absolutePath) { this.absolutePath = absolutePath;}

        @Override
        public boolean apply(InputFile inputFile) {
            String inputFileAbsolutePath = FileFormatter.formatFilePath(
                    inputFile.absolutePath().substring(0, inputFile.absolutePath().length() -5))
                    + ".java";
            if(inputFileAbsolutePath.endsWith(absolutePath)) {
                return true;
            }
            return false;
        }
    }

    public static class FileWithName implements FilePredicate {
        private String fileName;
        public FileWithName(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public boolean apply(InputFile inputFile) {
            if(inputFile.file().getName().equals(fileName)) {
                return true;
            }
            return false;
        }
    }
}
