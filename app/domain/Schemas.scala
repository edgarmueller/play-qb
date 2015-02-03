package domain

import org.qbproject.schema.QBSchema._
import org.qbproject.mongo._
import play.api.libs.json.JsBoolean
import view.QBView._

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
      QBDerefTable(
        QBViewPath("", "/user"),
        QBReferenceColumnMapping("id", "Name"), // TODO: replace QBReferenceColumnMapping with annotation on respective column
        QBTableColumn(QBViewPath("name"),    "Name"),
        QBTableColumn(QBViewPath("status"),     "Status")
      )
    )
  )

  // fetche
  val viewUser = QBViewModel(
    user,
    QBVerticalLayout(
      QBHorizontalLayout(
        QBViewControl(QBViewPath("name"), "Name"),
        QBViewControl(QBViewPath("status"), "Status"),
        QBDerefTable(
          QBViewPath("tasks",   // the attribute that contains a list of task ids
            endPoint = "/task", // the endpoint where to fetch tasks from
            endPointParam = "/search?userId={{id}}"), // and search parameters that are necessary to filter the fetched tasks
          QBReferenceColumnMapping("id", "Title"),
          QBTableColumn(QBViewPath("title"),    "Title"),
          QBTableColumn(QBViewPath("done"),     "Done"),
          QBTableColumn(QBViewPath("priority"), "Priority"),
          QBTableColumn(QBViewPath("dueDate"),  "Due Date")
        )
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
