package controllers

import play.api.libs.json.{JsObject, Json}
import play.api.libs.json.Json.JsValueWrapper
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._

trait MongoQueryHelper {

  def containsTextQuery(field: String)(value: String) = Json.obj(
    field ->
      Json.obj(
        "$regex" -> (".*" + value + ".*")
      )
  )

  def matchesQuery(field: String)(value: JsValueWrapper) = Json.obj(
    field -> value
  )

  def containsAnyIdOfQuery(bsonIds: List[BSONObjectID]) = Json.obj(
    "_id" -> Json.obj(
      "$in" -> bsonIds
    )
  )

  def mergeQueries(possibleQueries: Option[JsObject]*): JsObject =
    possibleQueries
      .flatten
      .foldLeft(Json.obj())(_ deepMerge _)

}
