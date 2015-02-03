package view

import org.qbproject.schema.{QBType}
import play.api.libs.json._

object QBView {


  // TODO: or reference via val?
  case class QBViewPath(domainPath: String, endPoint: String = "", endPointParam: String = "")

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

  case class QBDerefTable(path: QBViewPath, ref: QBReferenceColumnMapping, columns: QBTableColumn*) extends QBViewElement

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

  implicit def qbViewTableWriter: Writes[QBDerefTable] = OWrites[QBDerefTable] { table =>
    Json.obj(
      "type" -> "DerefTable",
      "path" -> table.path.domainPath,
      "endPoint" -> table.path.endPoint,
      "endPointParam" -> table.path.endPointParam,
      "idProperty" -> table.ref.refAttribute,
      "idLabel" -> table.ref.label,
      "columns" -> table.columns.map(Json.toJson(_))
    )
  }

  implicit def qbViewElementWriter: Writes[QBViewElement] = OWrites[QBViewElement] {
    case ctrl: QBViewControl => qbViewControlWriter.writes(ctrl).as[JsObject]
    case grp: QBGroup => qbViewGroupWriter.writes(grp).as[JsObject]
    case lbl: QBLabel => qbViewLabelWriter.writes(lbl).as[JsObject]
    case layout: QBViewLayout => qbViewLayoutWriter.writes(layout).as[JsObject]
    case table: QBDerefTable => qbViewTableWriter.writes(table).as[JsObject]
  }
}