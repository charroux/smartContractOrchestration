
package orcha.lang.configure;



/**
 * Do not edit this file : auto generated file
 * 
 */
@org.springframework.boot.context.properties.ConfigurationProperties(prefix = "driving")
public class ConfigurationProperties {

    public ConfigurationProperties.SimpleApplicationOutput simpleApplicationOutput = new SimpleApplicationOutput();

    public ConfigurationProperties.SimpleApplicationOutput getSimpleApplicationOutput() {
        return simpleApplicationOutput;
    }

    public void setSimpleApplicationOutput(ConfigurationProperties.SimpleApplicationOutput simpleApplicationOutput) {
        this.simpleApplicationOutput = simpleApplicationOutput;
    }

    public class SimpleApplicationOutput {

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
