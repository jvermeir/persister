package demo

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{GivenWhenThen, FeatureSpec}
import org.scalatest.matchers.MustMatchers
import org.junit.Before
import java.io.File
import org.apache.commons.io.FileUtils
import scala.collection.mutable.Map

@RunWith(classOf[JUnitRunner])
class PerstisterDemoTest extends FeatureSpec with GivenWhenThen with MustMatchers {

  @Before
  def setup= {
    val dataFile = new File(FileBasedDomainObjectConfig.domainObjectDatabaseFileName)
    FileUtils.writeStringToFile(dataFile, "")
    val domainObjects = Map("name1" -> DomainObject("name1","1(from file)"),"name2" -> DomainObject("name2","2(from file)"))
    for (domainObject <- domainObjects) {
      FileUtils.writeStringToFile(dataFile, domainObject._2.printAsDataStoreString +"\n", true)
    }
    Config.reload
  }

  feature("Implemenations of a data store can be configured at runtime") {
    info("As a developer")
    info("I want to be able to configure dependencies in a easy to manage way")
    info("So I can change them for a test.")

    scenario("DomainObjects can be compared") {
      Given("a bunch of domain objects")
      setup
      val d1 = DomainObject("n1", "v1")
      val d2equalsd1 = DomainObject("n1", "v1")
      val d3 = DomainObject("n2", "v1")
      val d4 = DomainObject("n", "v1")
      When("they are compared to each other")
      Then("we get results that make sense...")
      d1 must be === d2equalsd1
      d1 must be === d1
      d3 must be > d1
      d4 must be < d1
      d4 must be < d3
    }
    scenario("The default implementation of a dependency works for the default use case") {
      Given("a default DomainObjectClient")
      setup
      val domainObjectClient = Config.domainObjectClient
      When("we search for 'name1'")
      val domainObject = domainObjectClient.getByName("name1")
      Then("we get 'DomainObject(name1,[FileBasedDomainObjectRepository] 1(from file)'")
      val expected: DomainObject = DomainObject("name1", "[FileBasedDomainObjectRepository] 1(from file)")
      expected must be === domainObject
    }

    scenario("The implementation can be switched to a in memory version easily by changing the Config object") {
      Given("a DomainObjectClient with a InMemoryDomainObjectRepository")
      setup
      Config.domainObjectClient = new DomainObjectClient(InMemoryDomainObjectConfig)
      val domainObjectClient = Config.domainObjectClient
      domainObjectClient.add(DomainObject("name1","1(from memory)"))
      domainObjectClient.add(DomainObject("name2","2(from memory)"))
      When("we search for 'name1'")
      val domainObject = domainObjectClient.getByName("name1")
      Then("we get 'DomainObject(name1,[InMemoryDomainObjectRepository] 1(from memory)'")
      val expected: DomainObject = DomainObject("name1", "[InMemoryDomainObjectRepository] 1(from memory)")
      expected must be === domainObject
    }

    scenario("And all this also works if the DomainObject is accessed through 2 layers of objects") {
      Given("a ThirdLevelClient with a default repository")
      setup
      val t = new ThirdLevelClient
      val domainObject = t.getByName("name1")
      val expected: DomainObject = DomainObject("name1", "[FileBasedDomainObjectRepository] 1(from file)")
      expected must be === domainObject
      When("we switch clients")
      Config.domainObjectClient = new DomainObjectClient(InMemoryDomainObjectConfig)
      val domainObjectClient = Config.domainObjectClient
      domainObjectClient.add(DomainObject("name1","1(from memory)"))
      domainObjectClient.add(DomainObject("name2","2(from memory)"))
      val newDomainObject = t.getByName("name1")
      Then("we get 'DomainObject(name1,[InMemoryDomainObjectRepository] 1(from memory)'")
      val newExpected: DomainObject = DomainObject("name1", "[InMemoryDomainObjectRepository] 1(from memory)")
      newExpected must be === newDomainObject
    }
  }
}