package controllers

import domain.Schemas
import org.qbproject.mongo.{QBAdaptedMongoCollection, QBMongoDefaultCollection}
import org.qbproject.routing.QBRouter
import org.qbproject.schema.QBSchema._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._
import play.api.mvc.Action
import play.modules.reactivemongo.MongoController
import reactivemongo.bson.BSONObjectID
import scala.concurrent.Future
import scalaz._
import Scalaz._


object TaskController extends MongoController with QbCrudController with MongoQueryHelper {

  override def collection: QBAdaptedMongoCollection = QBMongoDefaultCollection("tasks", db, Schemas.task)

  def list(page: Int = 0, pageSize: Int = 10, includeMeta: Boolean = false) = {
    getAll(page, pageSize, includeMeta)
  }

  def viewPerTask(taskId: String) = Action {
    Ok(Json.toJson(Schemas.viewTask))
  }

  def view() = Action {
    Ok(Json.toJson(Schemas.viewTask))
  }

  def schema = Action {
    Ok(Json.toJson(Schemas.task))
  }

//  def find(stringQuery: String = "") = {
//    val q = if (stringQuery.isEmpty) {
//      Json.obj()
//    } else {
//      // TODO: ugly
//      val splitted: Array[String] = stringQuery.split(",")
//      Json.obj(
//        splitted(0) -> Json.obj(
//          "$regex" -> (".*" + splitted(1) + ".*")
//        )
//      )
//    }
//    JsonHeaders {
//      Action.async {
//        collection.find(q).map { result =>
//          Ok(Json.toJson(result))
//        }
//      }
//    }
//  }

  def findTasksByUser(userId: String,
                            title: Option[String],
                            done: Option[Boolean],
                            priority: Option[Int],
                            page: Int = 0, pageSize: Int = 10) = {
    JsonHeaders {
      Action.async {
        _findTasksByUser(userId, title, done, priority, page, pageSize).map(json =>
          Ok(Json.toJson(json)))
      }
    }
  }

  def _findTasksByUser(userId: String,
                      title: Option[String],
                      done: Option[Boolean],
                      priority: Option[Int],
                      page: Int = 0, pageSize: Int = 10): Future[List[JsObject]] = {

    val titleQuery    = title.map(containsTextQuery("title")(_))
    val doneQuery     = done.map(matchesQuery("done")(_))
    val priorityQuery = priority.map(matchesQuery("priority")(_))

    val query = List(titleQuery, doneQuery, priorityQuery)
      .flatten
      .foldLeft(Json.obj())(_ deepMerge _)

    fetchTasksOfUser(userId, query, page, pageSize)
  }

  def fetchTasksOfUser(userId: String, query: JsObject = Json.obj(), page: Int, pageSize: Int): Future[List[JsObject]] = {
    UserController.collection.findById(userId).flatMap(userOpt =>
      userOpt.fold(Future.successful(List[JsObject]()))(
        user => {
          fetchTasks((user \ "tasks").as[List[String]], query, page, pageSize)
        }
      )
    )
  }


  def fetchTasks(taskIds: List[String],
                 query: JsObject = Json.obj(),
                 page: Int = 0, pageSize: Int = 10): Future[List[JsObject]] = {

    val bsonIds = taskIds.map(BSONObjectID.parse).filter(_.isSuccess).map(_.get)
    val q = containsAnyIdOfQuery(bsonIds).deepMerge(query)
    collection.find(q, page * pageSize, pageSize)
  }


//  def markAsDone(taskId: String) = JsonHeaders {
//    Action.async {
//      collection.findById(taskId).flatMap(taskOpt =>
//        taskOpt.fold(Future(Ok("Task not found")))(
//          task =>GET     /user/view/:id            controllers.UserController.viewPerUser(id)
//
//        )
//      )
//    }
//  }

}

object TaskRouter extends QBRouter {
  override def qbRoutes = TaskController.crudRoutes
}

