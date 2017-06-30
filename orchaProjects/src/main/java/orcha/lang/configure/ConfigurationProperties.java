
package orcha.lang.configure;



/**
 * Do not edit this file : auto generated file
 * 
 */
@org.springframework.boot.context.properties.ConfigurationProperties(prefix = "driving")
public class ConfigurationProperties {

    public ConfigurationProperties.OutputFile1 outputFile1 = new OutputFile1();
    public ConfigurationProperties.OutputFile2 outputFile2 = new OutputFile2();

    public ConfigurationProperties.OutputFile1 getOutputFile1() {
        return outputFile1;
    }

    public void setOutputFile1(ConfigurationProperties.OutputFile1 outputFile1) {
        this.outputFile1 = outputFile1;
    }

    public ConfigurationProperties.OutputFile2 getOutputFile2() {
        return outputFile2;
    }

    public void setOutputFile2(ConfigurationProperties.OutputFile2 outputFile2) {
        this.outputFile2 = outputFile2;
    }

    public class OutputFile1 {

        public String directory;
        public String filename;

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

    }

    public class OutputFile2 {

        public String directory;
        public String filename;

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

    }

}