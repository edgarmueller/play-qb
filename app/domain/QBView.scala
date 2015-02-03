package domain

import org.qbproject.schema.QBType
import play.api.libs.json._

object QBView {

  case class QBEndPoint(endPoint: String = "", endPointParam: String = "")

  case class QBViewPath(domainPath: String, endPoint: Option[QBEndPoint] = None)

  trait QBViewElement

  case class QBViewControl(path: QBViewPath, name: String) extends QBViewElement

  case class QBLabel(text: String) extends QBViewElement

  case class QBGroup(name: String, elements: QBViewElement*) extends QBContainer

  trait QBContainer extends QBViewElement {
    def elements: Seq[QBViewElement]
  }

  case class QBReferenceColumnMapping(refAttribute: String, label: String)

  trait QBViewLayout extends QBContainer

  case class QBVerticalLayout(elements: QBViewElement*) extends QBViewLayout

  case class QBHorizontalLayout(elements: QBViewElement*) extends QBViewLayout

  case class QBViewModel(domainType: QBType, elements: QBViewElement*)

  case class QBScopedTable(path: QBViewPath,
                           ref: QBReferenceColumnMapping,
                           options: Map[TableOption, String],
                           columns: QBTableColumn*) extends QBViewElement

  type TableOption = String
  case object TableOptions {
    val EnableFiltering = "enableFiltering"
    val PagingBaseUrl = "pagingUrl"
  }

  case class QBTableColumn(path: QBViewPath, label: String)

  implicit def qbViewModelWriter: Writes[QBViewModel] = OWrites[QBViewModel] { viewModel =>
    Json.obj(
      // "domainType" -> "user", // TODO: how to reference
      "elements" -> viewModel.elements.map(qbViewElementWriter.writes))
  }

  implicit def qbViewControlWriter: Writes[QBViewControl] = OWrites[QBViewControl] { viewControl =>
    Json.obj(
      "type" -> "Control",
      "path" -> viewControl.path.domainPath,
      "name" -> viewControl.name)
  }

  implicit def qbViewLabelWriter: Writes[QBLabel] = OWrites[QBLabel] { label =>
    Json.obj(
      "type" -> "Label",
      "text" -> label.text)
  }

  implicit def qbViewGroupWriter: Writes[QBGroup] = OWrites[QBGroup] { group =>
    Json.obj(
      "type" -> "Group",
      "name" -> group.name,
      "elements" -> group.elements.map(qbViewElementWriter.writes))
  }

  implicit def qbViewLayoutWriter: Writes[QBViewLayout] = OWrites[QBViewLayout] { layout =>
    Json.obj(
      "type" -> layout.getClass.getSimpleName,
      "elements" -> layout.elements.map(qbViewElementWriter.writes))
  }

  implicit def qbTableColumnWriter: Writes[QBTableColumn] = OWrites[QBTableColumn] { column =>
    Json.obj(
      "type" -> "Column",
      "path" -> column.path.domainPath
    )
  }

  implicit def qbViewTableWriter: Writes[QBScopedTable] = OWrites[QBScopedTable] { table =>
    Json.obj(
      "type" -> classOf[QBScopedTable].getSimpleName,
      "path" -> table.path.domainPath,
      "idProperty" -> table.ref.refAttribute,
      "idLabel" -> table.ref.label,
      "columns" -> table.columns.map(Json.toJson(_)),
      "options" -> table.options
    ) deepMerge table.path.endPoint.fold(
      Json.obj()
    ) ( endPoint =>
      Json.obj(
        "endPoint" -> endPoint.endPoint,
        "endPointParam" -> endPoint.endPointParam
      )
    )
  }

  implicit def qbViewElementWriter: Writes[QBViewElement] = OWrites[QBViewElement] {
    case ctrl: QBViewControl => qbViewControlWriter.writes(ctrl).as[JsObject]
    case grp: QBGroup => qbViewGroupWriter.writes(grp).as[JsObject]
    case lbl: QBLabel => qbViewLabelWriter.writes(lbl).as[JsObject]
    case layout: QBViewLayout => qbViewLayoutWriter.writes(layout).as[JsObject]
    case table: QBScopedTable => qbViewTableWriter.writes(table).as[JsObject]
  }
}