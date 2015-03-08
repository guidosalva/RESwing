package reswing

import scala.language.implicitConversions
import scala.swing.Color
import scala.swing.ComboBox
import scala.swing.Dimension
import scala.swing.Font
import scala.swing.event.ListElementsAdded
import scala.swing.event.ListChanged
import scala.swing.event.ListElementsRemoved
import scala.swing.event.SelectionChanged

class ReComboBox[A](
    val items: ReSwingValue[Seq[A]] = ReSwingNoValue[Seq[A]],
    `selection.index`: ReSwingValue[Int] = (),
    `selection.item`: ReSwingValue[Option[A]] = ReSwingNoValue[Option[A]],
    background: ReSwingValue[Color] = (),
    foreground: ReSwingValue[Color] = (),
    font: ReSwingValue[Font] = (),
    enabled: ReSwingValue[Boolean] = (),
    minimumSize: ReSwingValue[Dimension] = (),
    maximumSize: ReSwingValue[Dimension] = (),
    preferredSize: ReSwingValue[Dimension] = ())
  extends
    ReComponent(background, foreground, font, enabled,
                minimumSize, maximumSize, preferredSize) {
  override protected lazy val peer = new ComboBox[A](Seq.empty[A]) with ComponentMixin

  protected val javaPeer = peer.peer.asInstanceOf[javax.swing.JComboBox[A]]

  private var model: javax.swing.ListModel[A] = _

  private val modelListener = new javax.swing.event.ListDataListener {
    def contentsChanged(e: javax.swing.event.ListDataEvent)
      { peer publish ListChanged(null) }
    def intervalRemoved(e: javax.swing.event.ListDataEvent)
      { peer publish ListElementsRemoved(null, e.getIndex0 to e.getIndex1) }
    def intervalAdded(e: javax.swing.event.ListDataEvent)
      { peer publish ListElementsAdded(null, e.getIndex0 to e.getIndex1) }
  }

  def modelChanged = {
    if (model != null)
      model removeListDataListener modelListener
    if (javaPeer.getModel != null)
      javaPeer.getModel addListDataListener modelListener
    model = javaPeer.getModel
  }

  javaPeer setModel new ReComboBox.ReComboBoxModel[A]
  modelChanged

  items using (
      { () =>
        javaPeer.getModel match {
          case model: ReComboBox.ReComboBoxModel[A] => model.getItems
          case model => for (i <- 0 until model.getSize) yield model.getElementAt(i)
        }
      },
      { items =>
        val selected =
        (javaPeer.getModel match {
          case model: ReComboBox.ReComboBoxModel[A] => model
          case _ =>
            val model = new ReComboBox.ReComboBoxModel[A]
            javaPeer setModel model
            modelChanged
            model
        })() = items
      },
      classOf[ListChanged[_]])

  class ReSelection(
      val index: ReSwingValue[Int],
      val item: ReSwingValue[Option[A]]) {
    protected[ReComboBox] val peer = ReComboBox.this.peer.selection

    index using (peer.index _, peer.index= _, (peer, classOf[SelectionChanged]))
    item using (
        { () => Option(peer.item) },
        { item => peer.item = item getOrElse null.asInstanceOf[A] },
        (peer, classOf[SelectionChanged]))

    val changed = ReSwingEvent using (peer, classOf[SelectionChanged])
  }

  object ReSelection {
    implicit def toSelection(selection: ReSelection) = selection.peer
  }

  object selection extends ReSelection(`selection.index`, `selection.item`)
}

object ReComboBox {
  implicit def toComboBox[A](component: ReComboBox[A]): ComboBox[A] = component.peer

  class ReComboBoxModel[A]
      extends javax.swing.AbstractListModel[A] with javax.swing.ComboBoxModel[A] {
    private var items = Seq.empty[A]
    def update(listData: Seq[A]) {
      val itemsSize = items.size
      val additional = listData.size - itemsSize
      items = listData

      if (!(items contains selected))
        selected = null

      if (additional > 0)
        fireIntervalAdded(this, itemsSize, listData.size - 1)
      if (additional < 0)
        fireIntervalRemoved(this, listData.size, itemsSize - 1)
      fireContentsChanged(this, 0, listData.size)
    }
    def getElementAt(n: Int) = items(n)
    def getSize = items.size
    def getItems = items

    private var selected: AnyRef = _
    def getSelectedItem = selected.asInstanceOf[AnyRef]
    def setSelectedItem(item: AnyRef) {
      if ((item == null || (items contains item)) &&
           ((selected != null && selected != item) ||
            (selected == null && item != null))) {
        selected = item
        fireContentsChanged(this, -1, -1)
      }
    }
  }
}
