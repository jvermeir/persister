package demo2

/**
 * Object that holds current configuration for the application
 *
 */
object Config {

  var domainObjectRepository:DomainObjectRepository = new FileBasedDomainObjectRepository
  def reload:Unit = domainObjectRepository.reload
}
