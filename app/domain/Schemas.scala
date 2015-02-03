package domain

import org.qbproject.schema.QBSchema._
import org.qbproject.mongo._
import play.api.libs.json.JsBoolean
import QBView._

object Schemas {

  lazy val task = qbClass(
    "id" -> objectId,
    "title" -> qbNonEmptyText,
    "done" -> optional(qbBoolean, JsBoolean(value = false)),
    "priority" -> optional(qbInteger),
    "dueDate" -> optional(qbDateTime)
  )

  lazy val user = qbClass(
    "id" -> objectId,
    "name" -> qbNonEmptyText,
    "status" -> qbEnum("Gott", "Reader", "Writer"),
    "tasks" -> qbList(task("id"))
  )

  val viewModelEntryPoint = QBViewModel(
    qbList(user),
    QBVerticalLayout(
      QBScopedTable(
        QBViewPath("", Some(QBEndPoint("/user"))),
        QBReferenceColumnMapping("id", "Name"), // TODO: replace QBReferenceColumnMapping with annotation on respective column
        Map(
          TableOptions.EnableFiltering -> "false",
          TableOptions.PagingBaseUrl   -> "/user/search"
        ),
        QBTableColumn(QBViewPath("name"),    "Name"),
        QBTableColumn(QBViewPath("status"),     "Status")
      )
    )
  )

  val viewUser = QBViewModel(
    user,
    QBVerticalLayout(
      QBHorizontalLayout(
        QBViewControl(QBViewPath("name"), "Name"),
        QBViewControl(QBViewPath("status"), "Status")
      ),
      QBScopedTable(            // resolves all IDs contained within the property of the currently scoped element
        QBViewPath("tasks",     // the attribute that contains a list of task IDs
          Some(QBEndPoint(      // endpoint from where to fetch the referenced entities
            endPoint = "/task", // the endpoint where to fetch tasks from
            endPointParam = "/search?userId={{id}}"))), // any necessary search parameters
        QBReferenceColumnMapping("id", "Title"),        // defines which column will act
        Map(TableOptions.PagingBaseUrl -> "/task/search?userId={{id}}"),
        QBTableColumn(QBViewPath("title"),    "Title"),
        QBTableColumn(QBViewPath("done"),     "Done"),
        QBTableColumn(QBViewPath("priority"), "Priority"),
        QBTableColumn(QBViewPath("dueDate"),  "Due Date")
      )
    )
  )

  val viewTask = QBViewModel(
    task,
    QBVerticalLayout(
      QBViewControl(QBViewPath("title"),    "Title"),
      QBViewControl(QBViewPath("done"),     "Done"),
      QBViewControl(QBViewPath("priority"), "Priority"),
      QBViewControl(QBViewPath("dueDate"),  "Due Date")
    )
  )
}
