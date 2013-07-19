package demo

/**
 * Object that holds current configuration for the application
 *
 * TODO: we need a production and a test version of this Config thingy
 * In a test it should be possible to switch to InMemory config while the normal
 * production version should use FileBased.
 * What happens if the thing we want to configure is buried in layers of other classes?
 * If a class has a dependency it should delegate to the current Config implementation.
 * But how do we choose a new Config implementation? Should Config hold vars so it can be changed?
 * Or should it be an trait with a bunch of implementations that can be chosen on the command line?
 */
object Config {
  val domainObjectClient = new DomainObjectClient(InMemoryDomainObjectConfig)
}
