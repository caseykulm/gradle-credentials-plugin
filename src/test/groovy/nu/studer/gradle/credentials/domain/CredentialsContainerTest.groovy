package nu.studer.gradle.credentials.domain

import org.apache.commons.configuration2.builder.fluent.Configurations

@SuppressWarnings("GrUnresolvedAccess")
class CredentialsContainerTest extends GroovyTestCase {

  Configurations configs = new Configurations();

  void testSetGetCredentials() {
    def encryptor = CredentialsEncryptor.withPassphrase("somePassphrase".toCharArray());
//  def configs = new Configurations();
    def orderedProperties = new LinkedHashMap();
    def container = new CredentialsContainer(encryptor, orderedProperties);
    def value = 'someValue'

    container.someKey = value
    def actualValue = container.someKey

    assertEquals(value, actualValue)
  }

  void testGetUnknownPropertyReturnsNull() {
    def encryptor = CredentialsEncryptor.withPassphrase("somePassphrase".toCharArray());
    def orderedProperties = new LinkedHashMap();
    def container = new CredentialsContainer(encryptor, orderedProperties);

    def value = container.someKey

    assertNull(value)
  }

}
