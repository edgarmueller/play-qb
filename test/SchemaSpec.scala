import domain.Schemas
import org.specs2.mutable.Specification
import play.api.libs.json.{JsValue, Json}

class SchemaSpec extends Specification {

  "The view models" should {

    "be serializable in case of a task" in {
      val json = Json.toJson(Schemas.viewTask)
      println(json)
      json must beAnInstanceOf[JsValue]
    }

    "be serializable in case of an user" in {
      val json = Json.toJson(Schemas.viewUser)
      println(Json.prettyPrint(json))
      json must beAnInstanceOf[JsValue]
    }

  }

}
