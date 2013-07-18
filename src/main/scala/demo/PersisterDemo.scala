package demo

import scala.collection.mutable.Map
import org.apache.commons.io.FileUtils
import java.io.File

/**
 * Show how to persist classes in a testable way.
 * (So I can just copy this pattern in stead of trying to remember how to do it from scratch...)
 *
 */

/**
 * Demo domain object
 * @param name Some String
 * @param value Some Int
 */
case class DomainObject (name:String, value:Int) extends Ordered[DomainObject] {
  def compare(that: DomainObject) = name.compare(that.name)
  def printAsDataStoreString: String = name + ":" + value
}

/**
 * Basic trait to persist DomainObject instances that should be extended for actual use in production or test code.
 */
trait DomainObjectRepository {
  val domainObjects: Map[String, DomainObject]

  def getByName(name: String): DomainObject = {
    val domainObject = domainObjects.get(name)
    // TODO: ???
    domainObject.map { p => p } getOrElse (throw new TotalPanicException("DomainObject object named " + name + " not found"))
  }

  def add(p: DomainObject): Unit = throw new OperationNotSupportedException("add operation not supported")
  def update(oldDomainObject:DomainObject, newDomainObject: DomainObject): Unit = throw new OperationNotSupportedException("update operation not supported")
  def delete(domainObject: DomainObject): Unit = throw new OperationNotSupportedException("delete operation not supported")
  def reload: Unit = throw new OperationNotSupportedException("reload operation not supported")
}

/**
 * A DomainObjectClient is given a DomainObjectRepository. It knows how to access service methods
 * of a repository. Client delegates to Repository.
 */
class DomainObjectClient(env: { val domainObjectRepository: DomainObjectRepository }) {
  def getByName(name: String): DomainObject = env.domainObjectRepository.getByName(name)
  // TODO: Dubious function this, because it won't scale to really large sets of data.
  def getDomainObjects: Map[String, DomainObject] = env.domainObjectRepository.domainObjects.clone
  def add(DomainObject: DomainObject) = env.domainObjectRepository.add(DomainObject)
  def update(oldDomainObject:DomainObject, newDomainObject: DomainObject) = env.domainObjectRepository.update(oldDomainObject, newDomainObject)
  def delete(DomainObject: DomainObject) = env.domainObjectRepository.delete(DomainObject)
  def reload = env.domainObjectRepository.reload
}

/**
 * This repository is just a Map in memory
 */
class InMemoryDomainObjectRepository extends DomainObjectRepository {
  val domainObjects: Map[String, DomainObject] = Map[String, DomainObject] ()
  override def reload(): Unit = {domainObjects.retain((s,i) => false) }

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
}

/**
 * This repository stores data in a file
 */

class FileBasedDomainObjectRepository extends  DomainObjectRepository {
    val domainObjects = loadDomainObjectsFromFile
    def loadDomainObjectsFromFile: Map[String, DomainObject] = {
      val domainObjectsAsText = FileUtils.readFileToString(new File(FileBasedDomainObjectConfig.domainObjectDatabaseFileName))
      loadDomainObjectsFromAString(domainObjectsAsText)
    }

    private def loadDomainObjectsFromAString(domainObjectsAsText: String): Map[String, DomainObject] = {
      val domainObjectsFromFile = for (line <- domainObjectsAsText.split("\n")) yield {
        val parts = line.split(":")
        (parts(0) -> new DomainObject(parts(0), parts(1).toInt))
      }
      Map(domainObjectsFromFile.toList: _*)
    }

    override def reload(): Unit = {
      domainObjects.retain(((k, v) => false))
      for (domainObject <- loadDomainObjectsFromFile) { domainObjects += domainObject }
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
}

/**
 * FileBasedDomainObjectConfig is used to provide a default implementation of a CategoryRepository.
 * In this case the FileBasedCategoryRepository is used.
 * For testing purposes a different config can be used (see CategoryTest for examples).
 */
object FileBasedDomainObjectConfig {
  var domainObjectDatabaseFileName = "data/domainObjectDatabase.csv"
  lazy val domainObjectRepository = new FileBasedDomainObjectRepository
}

object InMemoryDomainObjectConfig {
  lazy val domainObjectRepository = new InMemoryDomainObjectRepository
}

/**
 * Main class to try things out.
 */
object Demo {
  def main(args:Array[String]) { print("hello")}
}
