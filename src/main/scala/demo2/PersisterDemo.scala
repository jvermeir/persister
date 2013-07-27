package demo2

import scala.collection.mutable.Map
import org.apache.commons.io.FileUtils
import java.io.File
import demo.{TotalPanicException, OperationNotSupportedException}
/**
 * Show how to persist classes in a testable way.
 * (So I can just copy this pattern in stead of trying to remember how to do it from scratch...)
 *
 */

/**
 * Demo domain object
 * @param name Some String
 * @param value Some String
 */
case class DomainObject(name: String, value: String) extends Ordered[DomainObject] {
  def compare(that: DomainObject) = name.compare(that.name)

  def printAsDataStoreString: String = name + ":" + value
}

/**
 * Basic trait to persist domainObject instances that should be extended for actual use in production or test code.
 */
trait DomainObjectRepository {
  protected[demo2] val domainObjects: Map[String, DomainObject]

  def getByName(name: String): DomainObject = {
    val domainObject = domainObjects.get(name)
    domainObject.map {
      p => p
    } getOrElse (throw new TotalPanicException("domainObject object named " + name + " not found"))
  }

  def add(p: DomainObject): Unit = throw new OperationNotSupportedException("add operation not supported")

  def update(oldDomainObject: DomainObject, newDomainObject: DomainObject): Unit = throw new OperationNotSupportedException("update operation not supported")

  def delete(domainObject: DomainObject): Unit = throw new OperationNotSupportedException("delete operation not supported")

  def reload: Unit = throw new OperationNotSupportedException("reload operation not supported")

  protected[demo2] def earMarkInstance(domainObject: DomainObject): DomainObject = throw new OperationNotSupportedException("reload operation not supported")
}

/**
 * This repository is just a Map in memory
 */
class InMemoryDomainObjectRepository extends DomainObjectRepository {
  override val domainObjects: Map[String, DomainObject] = Map[String, DomainObject]()

  override def getByName(name: String): DomainObject = earMarkInstance(super.getByName(name))

  override def reload(): Unit = {
    domainObjects.retain((s, i) => false)
  }

  override def delete(domainObjectToDelete: DomainObject) = {
    domainObjects.remove(domainObjectToDelete.name)
  }

  override def update(domainObjectToUpdate: DomainObject, newDomainObject: DomainObject) = {
    domainObjects.remove(domainObjectToUpdate.name)
    domainObjects += (newDomainObject.name -> newDomainObject)
  }

  override def add(newDomainObject: DomainObject) {
    domainObjects += (newDomainObject.name -> newDomainObject)
  }

  protected[demo2] override def earMarkInstance(domainObject: DomainObject): DomainObject = new DomainObject(domainObject.name, "[InMemoryDomainObjectRepository] " + domainObject.value)
}

/**
 * This repository stores data in a file
 */

class FileBasedDomainObjectRepository extends DomainObjectRepository {
  override val domainObjects = loadDomainObjectsFromFile

  private def loadDomainObjectsFromFile: Map[String, DomainObject] = {
    val domainObjectsAsText = FileUtils.readFileToString(new File(FileBasedDomainObjectConfig.domainObjectDatabaseFileName))
    loadDomainObjectsFromAString(domainObjectsAsText)
  }

  private def loadDomainObjectsFromAString(domainObjectsAsText: String): Map[String, DomainObject] = {
    val domainObjectsFromFile =
      for (line <- domainObjectsAsText.split("\n")) yield {
        val parts = line.split(":")
        (parts(0) -> new DomainObject(parts(0), parts(1)))
      }
    Map(domainObjectsFromFile.toList: _*)
  }

  override def getByName(name: String): DomainObject = earMarkInstance(super.getByName(name))

  override def reload(): Unit = {
    domainObjects.retain(((k, v) => false))
    for (domainObject <- loadDomainObjectsFromFile) {
      domainObjects += domainObject
    }
  }

  def save = {
    val dataFile = new File(FileBasedDomainObjectConfig.domainObjectDatabaseFileName)
    FileUtils.writeStringToFile(dataFile, "")
    for (domainObject <- domainObjects) {
      FileUtils.writeStringToFile(dataFile, domainObject._2.printAsDataStoreString, true)
    }
  }

  override def delete(domainObjectToDelete: DomainObject) = {
    domainObjects.remove(domainObjectToDelete.name)
    save
  }

  override def update(domainObjectToUpdate: DomainObject, newDomainObject: DomainObject) = {
    domainObjects.remove(domainObjectToUpdate.name)
    domainObjects += (newDomainObject.name -> newDomainObject)
    save
  }

  override def add(domainObject: DomainObject) {
    domainObjects += (domainObject.name -> domainObject)
    save
  }

  protected[demo2] override def earMarkInstance(domainObject: DomainObject): DomainObject = new DomainObject(domainObject.name, "[FileBasedDomainObjectRepository] " + domainObject.value)

}

/**
 * FileBasedDomainObjectConfig is used to provide a 'production' implementation of a
 * DomainObjectRepository.
 */
object FileBasedDomainObjectConfig {
  var domainObjectDatabaseFileName = "data/domainObjectDatabase.csv"
  lazy val domainObjectRepository = new FileBasedDomainObjectRepository
}

/**
 * InMemoryDomainObjectConfig is used to provide a test implementation of a
 * DomainObjectRepository.
 */
object InMemoryDomainObjectConfig {
  lazy val domainObjectRepository = new InMemoryDomainObjectRepository
}

/**
 * SecondLevelClient has a reference to a DomainObjectRepository. It offers a getByName method for demo purposes.
 */
class SecondLevelClient {
  def getByName(name: String): DomainObject = Config.domainObjectRepository.getByName(name)
}

/**
 * ThirdLevelClient has a reference to SecondLevelClient which it uses to get the domainObject by name. It doesn't know
 * about the way domainObject instances are stored but can still benefit from dynamic configuration because
 * SecondLevelClient can be configured.
 */
class ThirdLevelClient {
  def getByName(name: String): DomainObject = new SecondLevelClient().getByName(name)
}
