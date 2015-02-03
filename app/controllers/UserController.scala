package controllers

import domain.Schemas
import org.qbproject.mongo.QBMongoDefaultCollection
import org.qbproject.routing.QBRouter
import org.qbproject.schema.QBSchema._
import play.api.libs.json._
import play.api.mvc.Action
import play.modules.reactivemongo.MongoController
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object UserController extends MongoController with QbCrudController with MongoQueryHelper {

  lazy val collection = QBMongoDefaultCollection("users", db, Schemas.user)

  def view() = Action {
    Ok(Json.toJson(Schemas.viewModelEntryPoint))
  }

  def viewPerUser(id: String) = Action {
    Ok(Json.toJson(Schemas.viewUser))
  }

  def schema = Action {
    Ok(Json.toJson(Schemas.user))
  }

  def find(name: Option[String],
           status: Option[String],
           page: Int = 0, pageSize: Int = 10) = JsonHeaders {
    Action.async {
      _find(name, status, page, pageSize).map(json =>
        Ok(Json.toJson(json)))
    }
  }

  def _find(name: Option[String],
           status: Option[String],
           page: Int = 0, pageSize: Int = 10): Future[List[JsObject]] = {

    val query = mergeQueries(
      name.map(containsTextQuery("name")(_)),
      status.map(containsTextQuery("status")(_))
    )

    fetchUsers(query, page, pageSize)
  }

  def fetchUsers(query: JsObject = Json.obj(), page: Int, pageSize: Int): Future[List[JsObject]] = {
    collection.find(query, page * pageSize, pageSize)
  }

  //  def addTask(userId: String, taskId: String) = JsonHeaders {
  //    // TODO: monad transformer
  //    // TODO: replace with mongo $push/json zipper
  //    Action.async {
  //      collection.findById(userId).flatMap(userOpt =>
  //        userOpt.fold(Future(Ok("User not found")))(
  //          user => {
  //            val updatedUser: JsObject = user.deepMerge(
  //              Json.obj("tasks" -> (taskId :: (user \ "tasks").as[List[String]])))
  //            collection.update(userId, updatedUser).map(Ok(_))
  //          }
  //        )
  //      )
  //    }
  //  }


  //  def tasks = Action {
  //    val allTasks = TaskController.getAll()
  //  }
}

object UserRouter extends QBRouter {
  override def qbRoutes = UserController.crudRoutes
}
