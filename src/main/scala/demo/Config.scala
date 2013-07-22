package demo

/**
 * Object that holds current configuration for the application
 *
 */
object Config {
  var domainObjectClient = new DomainObjectClient(FileBasedDomainObjectConfig)
  def reload:Unit = domainObjectClient = new DomainObjectClient(FileBasedDomainObjectConfig)
}
