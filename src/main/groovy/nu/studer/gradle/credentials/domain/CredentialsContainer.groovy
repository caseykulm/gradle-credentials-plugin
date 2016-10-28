package nu.studer.gradle.credentials.domain
/**
 * Transiently retrieves and adds credentials.
 */
final class CredentialsContainer {

  private final CredentialsEncryptor credentialsEncryptor
//  private final OrderedProperties credentials
  private LinkedHashMap<String, String> orderedProperties;

  CredentialsContainer(CredentialsEncryptor credentialsEncryptor, LinkedHashMap<String, String> orderedProperties) {
    this.credentialsEncryptor = credentialsEncryptor
    this.orderedProperties = orderedProperties;
  }

  def propertyMissing(String name) {
    if (orderedProperties.containsKey(name)) {
      credentialsEncryptor.decrypt(orderedProperties.get(name))
    } else {
      null
    }
  }

  def propertyMissing(String name, value) {
    orderedProperties.put(name, credentialsEncryptor.encrypt(value as String))
  }

}
