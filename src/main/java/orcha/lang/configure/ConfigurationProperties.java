
package orcha.lang.configure;



/**
 * Do not edit this file : auto generated file
 * 
 */
@org.springframework.boot.context.properties.ConfigurationProperties(prefix = "driving")
public class ConfigurationProperties {

    public ConfigurationProperties.TripAgencyCustomer tripAgencyCustomer = new TripAgencyCustomer();

    public ConfigurationProperties.TripAgencyCustomer getTripAgencyCustomer() {
        return tripAgencyCustomer;
    }

    public void setTripAgencyCustomer(ConfigurationProperties.TripAgencyCustomer tripAgencyCustomer) {
        this.tripAgencyCustomer = tripAgencyCustomer;
    }

    public class TripAgencyCustomer {

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
