package controllers

import org.qbproject.mongo.QBAdaptedMongoCollection
import org.qbproject.routing._
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

trait QbCrudController extends Controller {

  def collection: QBAdaptedMongoCollection

  // Routes --
  def getAllRoute = GET / ? to getAll()

  def getByIdRoute = GET / string to getById

  def createRoute = POST / ? to create

  def updateRoute = POST / string to update

  def deleteRoute = DELETE / string to delete

  def crudRoutes: List[QBRoute] = List(
    getAllRoute,
    getByIdRoute,
    createRoute,
    deleteRoute,
    updateRoute)

  // TODO if find is fixed this method may be remove?
  def getAll(page: Int = 0, pageSize: Int = 10, includeMeta: Boolean = false) = JsonHeaders {
    Action.async {
      collection.all(page * pageSize, pageSize).map { result =>
        if (includeMeta) {
          Ok(
            Json.obj(
              "_meta" -> Json.obj(
                "page" -> page,
                "pageSize" -> pageSize
              )
            ).deepMerge(Json.toJson(result).as[JsObject])
          )
        } else {
          Ok(Json.toJson(result))
        }
      }
    }
  }

  def find(query: JsObject) = {
    JsonHeaders {
      Action.async {
        collection.find(query).map { result =>
          Ok(Json.toJson(result))
        }
      }
    }
  }

  def getById(id: String) = JsonHeaders {
    Action.async {
      collection.findById(id).map {
        case Some(result) => Ok(Json.toJson(result))
        case _ => NotFound(":(")
      }
    }
  }

  def count = JsonHeaders {
    Action.async {
      collection.count.map { result =>
        Ok(Json.toJson(Json.obj("count" -> result)))
      }
    }
  }

  def create = JsonHeaders {
    Action.async { request =>
      extractJsonFromRequest(request).fold(noJsonResponse)(json =>
        collection.create(json.asInstanceOf[JsObject]).map {
          result =>
            Ok(result)
        })
    }
  }

  def update(id: String) = JsonHeaders {
    Action.async { request =>
      extractJsonFromRequest(request).fold(noJsonResponse)(json =>
        collection.update(id, json.asInstanceOf[JsObject]).map {
          result =>
            Ok(result)
        })
    }
  }



  // TODO: current behaviour also returns true, if there is no document to delete
  //       verify, whether this behavior is wanted or not
  def delete(id: String) = Action.async {
    collection.delete(id).map { result =>
      Ok(Json.obj("deleteSuccess" -> result))
    }
  }

  def extractJsonFromRequest[A](implicit request: Request[A]): Option[JsValue] = {
    request.body match {
      case body: play.api.mvc.AnyContent if body.asJson.isDefined => Some(body.asJson.get)
      case body: play.api.libs.json.JsValue => Some(body)
      case _ => None
    }
  }


  def noJsonResponse: Future[Result] = Future(BadRequest(
    Json.toJson(QBAPIStatusMessage("error", "No valid json found."))
  ))

  def jsonInvalidResponse(error: JsError): Future[Result] = Future(BadRequest(
    Json.toJson(QBAPIStatusMessage("error", "Json input didn't pass validation", Some(JsError.toFlatJson(error))))
  ))

  case class QBAPIStatusMessage(status: String, message: String = "", details: Option[JsValue] = None)
  object QBAPIStatusMessage {
    implicit val format: Format[QBAPIStatusMessage] = Json.format[QBAPIStatusMessage]
  }

  /**
   * Set json headers, so that api calls aren't cached. Especially a problem with IE.
   */
  def JsonHeaders(action: EssentialAction): EssentialAction = EssentialAction { requestHeader =>
    action(requestHeader).map(result => result.withHeaders(
      CACHE_CONTROL -> "no-store, no-cache, must-revalidate",
      EXPIRES -> "Sat, 23 May 1987 12:00:00 GMT",
      PRAGMA -> "no-cache",
      CONTENT_TYPE -> "application/json; charset=utf-8",
      ACCESS_CONTROL_ALLOW_ORIGIN -> "*"))
  }
}
