package demo2

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.scalatest.matchers.MustMatchers
import java.io.File
import org.apache.commons.io.FileUtils
import scala.collection.mutable.Map

@RunWith(classOf[JUnitRunner])
class PerstisterDemoTest extends FeatureSpec with GivenWhenThen with MustMatchers {

  def setupFileBased = {
    Config.domainObjectRepository = new FileBasedDomainObjectRepository
    val dataFile = new File(FileBasedDomainObjectConfig.domainObjectDatabaseFileName)
    FileUtils.writeStringToFile(dataFile, "")
    val domainObjects = Map("name1" -> DomainObject("name1", "1(from file)"), "name2" -> DomainObject("name2", "2(from file)"))
    for (domainObject <- domainObjects) {
      FileUtils.writeStringToFile(dataFile, domainObject._2.printAsDataStoreString + "\n", true)
    }
    Config.reload
  }

  def setupInMemory = {
    Config.domainObjectRepository = new InMemoryDomainObjectRepository
    Config.reload
    Config.domainObjectRepository.add(DomainObject("name1", "1(from memory)"))
    Config.domainObjectRepository.add(DomainObject("name2", "2(from memory)"))
  }

  scenario("The default implementation of a dependency works for the default use case") {
    Given("a default DomainObjectRepository")
    setupFileBased
    val domainObjectRepository = Config.domainObjectRepository
    When("we search for 'name1'")
    val domainObject = domainObjectRepository.getByName("name1")
    Then("we get 'DomainObject(name1,[FileBasedDomainObjectRepository] 1(from file)'")
    val expected: DomainObject = DomainObject("name1", "[FileBasedDomainObjectRepository] 1(from file)")
    expected must be === domainObject
  }

  scenario("The implementation can be switched to a in memory version easily by changing the Config object") {
    Given("a DomainObjectRepository with a InMemoryDomainObjectRepository")
    setupInMemory
    val domainObjectRepository = Config.domainObjectRepository
    When("we search for 'name1'")
    val domainObject = domainObjectRepository.getByName("name1")
    Then("we get 'DomainObject(name1,[InMemoryDomainObjectRepository] 1(from memory)'")
    val expected: DomainObject = DomainObject("name1", "[InMemoryDomainObjectRepository] 1(from memory)")
    expected must be === domainObject
  }

  scenario("And all this also works if the DomainObject is accessed through 2 layers of objects") {
    Given("a ThirdLevelClient with a default repository")
    setupFileBased
    val t = new ThirdLevelClient
    val domainObject = t.getByName("name1")
    val expected: DomainObject = DomainObject("name1", "[FileBasedDomainObjectRepository] 1(from file)")
    expected must be === domainObject
    When("we switch clients")
    Config.domainObjectRepository = new InMemoryDomainObjectRepository
    val domainObjectRepository = Config.domainObjectRepository
    domainObjectRepository.add(DomainObject("name1", "1(from memory)"))
    domainObjectRepository.add(DomainObject("name2", "2(from memory)"))
    val newDomainObject = t.getByName("name1")
    Then("we get 'DomainObject(name1,[InMemoryDomainObjectRepository] 1(from memory)'")
    val newExpected: DomainObject = DomainObject("name1", "[InMemoryDomainObjectRepository] 1(from memory)")
    newExpected must be === newDomainObject
  }
}