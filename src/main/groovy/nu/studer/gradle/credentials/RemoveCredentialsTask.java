package nu.studer.gradle.credentials;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.gradle.api.DefaultTask;
import org.gradle.api.internal.tasks.options.Option;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Removes the given credentials, specified as project properties.
 */
public class RemoveCredentialsTask extends DefaultTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveCredentialsTask.class);

//    private CredentialsPersistenceManager credentialsPersistenceManager;
    private FileBasedConfigurationBuilder<FileBasedConfiguration> builder;
    private String key;

//    public void setCredentialsPersistenceManager(CredentialsPersistenceManager credentialsPersistenceManager) {
//        this.credentialsPersistenceManager = credentialsPersistenceManager;
//    }

    public void setConfigBuilder(FileBasedConfigurationBuilder<FileBasedConfiguration> builder) {
        this.builder = builder;
    }

    @Option(option = "key", description = "The credentials key.")
    public void setKey(String key) {
        this.key = key;
    }

    @Input
    public String getCredentialsKey() {
        return key != null ? key : getProjectProperty(CredentialsPlugin.CREDENTIALS_KEY_PROPERTY);
    }

//    @OutputFile
//    public File getEncryptedPropertiesFile() {
//        return credentialsPersistenceManager.getCredentialsFile();
//    }

    @OutputFile
    public File getEncryptedPropertiesFile() {
        return builder.getFileHandler().getFile();
    }

    @TaskAction
    void removeCredentials() throws IOException, ConfigurationException {
        // get credentials key from the project properties
        String key = getCredentialsKey();

        LOGGER.debug(String.format("Remove credentials with key: '%s'", key));

        // read the current persisted credentials
//        OrderedProperties credentials = credentialsPersistenceManager.readCredentials();

        // remove the credentials with the given key
//        credentials.removeProperty(key);

        // persist the updated credentials
//        credentialsPersistenceManager.storeCredentials(credentials);

        Configuration config = builder.getConfiguration();
        config.clearProperty(key);
        builder.save();
    }

    private String getProjectProperty(String key) {
        return (String) getProject().getProperties().get(key);
    }

}
