package demo

/**
 * Object that holds current configuration for the application
 *
 * TODO: How to reload? What happens if we switch domainObjectClient to a different implementation?
 * I guess some reloading or initializing should be done. How do we manage that?
 */
object Config {
  var domainObjectClient = new DomainObjectClient(FileBasedDomainObjectConfig)
  def reload:Unit = { domainObjectClient = new DomainObjectClient(FileBasedDomainObjectConfig) }
}
