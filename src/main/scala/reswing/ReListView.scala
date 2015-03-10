package reswing

import scala.language.implicitConversions
import scala.collection.JavaConverters._
import scala.swing.Color
import scala.swing.Dimension
import scala.swing.Font
import scala.swing.ListView
import scala.swing.ListView.IntervalMode
import scala.swing.event.ListSelectionChanged
import scala.swing.event.ListElementsAdded
import scala.swing.event.ListChanged
import scala.swing.event.ListElementsRemoved

class ReListView[A](
    val listData: ReSwingValue[Seq[A]] = ReSwingNoValue[Seq[A]],
    val visibleRowCount: ReSwingValue[Int] = (),
    val selectionForeground: ReSwingValue[Color] = (),
    val selectionBackground: ReSwingValue[Color] = (),
    selectIndices: ReSwingEvent[Seq[Int]] = (),
    ensureIndexIsVisible: ReSwingEvent[Int] = (),
    `selection.listDataSync`: ReSwingValue[Boolean] = (),
    `selection.intervalMode`: ReSwingValue[IntervalMode.Value] = (),
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
  override protected lazy val peer = new ListView[A] with ComponentMixin

  protected val javaPeer = peer.peer.asInstanceOf[javax.swing.JList[A]]

  private var model: javax.swing.ListModel[A] = _

  private val modelListener = new javax.swing.event.ListDataListener {
    def contentsChanged(e: javax.swing.event.ListDataEvent)
      { peer publish ListChanged(peer) }
    def intervalRemoved(e: javax.swing.event.ListDataEvent)
      { peer publish ListElementsRemoved(peer, e.getIndex0 to e.getIndex1) }
    def intervalAdded(e: javax.swing.event.ListDataEvent)
      { peer publish ListElementsAdded(peer, e.getIndex0 to e.getIndex1) }
  }

  def modelChanged {
    if (model != null)
      model removeListDataListener modelListener
    if (javaPeer.getModel != null)
      javaPeer.getModel addListDataListener modelListener
    model = javaPeer.getModel
  }

  javaPeer setModel new ReListView.ReListModel[A]
  modelChanged

  listData using (
      peer.listData _,
      { listData =>
        val selected =
          if (selection.listDataSync.get) javaPeer.getSelectedValuesList.asScala.toSet
          else null

        (javaPeer.getModel match {
          case model: ReListView.ReListModel[A] => model
          case _ =>
            val model = new ReListView.ReListModel[A]
            javaPeer setModel model
            modelChanged
            model
        })() = listData

        if (selected != null)
          javaPeer setSelectedIndices (listData.zipWithIndex collect {
            case (el, index) if selected contains el => index }).toArray
      },
      classOf[ListChanged[_]])

  visibleRowCount using (peer.visibleRowCount _, peer.visibleRowCount_= _, "visibleRowCount")
  selectionForeground using (peer.selectionForeground _, peer.selectionForeground_= _, "selectionForeground")
  selectionBackground using (peer.selectionBackground _, peer.selectionBackground_= _, "selectionBackground")

  selectIndices using peer.selectIndices _
  ensureIndexIsVisible using peer.ensureIndexIsVisible _

  val contentsChanged = ReSwingEvent using classOf[ListChanged[A]]
  val intervalRemoved = ReSwingEvent using classOf[ListElementsRemoved[A]]
  val intervalAdded = ReSwingEvent using classOf[ListElementsAdded[A]]

  class ReSelection(
      val intervalMode: ReSwingValue[IntervalMode.Value],
      val listDataSync: ReSwingValue[Boolean]) {
    protected[ReListView] val peer = ReListView.this.peer.selection

    private[ReListView] var listDataSyncVar = false

    val leadIndex = ReSwingValue using (peer.leadIndex _, (peer, classOf[ListSelectionChanged[_]]))
    val anchorIndex = ReSwingValue using (peer.anchorIndex _, (peer, classOf[ListSelectionChanged[_]]))
    val indices = ReSwingValue using (
        { () => javaPeer.getSelectedIndices.toSet },
        (peer, classOf[ListSelectionChanged[_]]))
    val items = ReSwingValue using (
        { () => javaPeer.getSelectedValuesList.asScala.toSeq },
        (peer, classOf[ListSelectionChanged[_]]), classOf[ListChanged[_]])

    val index = ReSwingValue using (
        { () => javaPeer.getSelectedIndex },
        (peer, classOf[ListSelectionChanged[_]]))
    val item = ReSwingValue using (
        { () => Option(javaPeer.getSelectedValue) },
        (peer, classOf[ListSelectionChanged[_]]), classOf[ListChanged[_]])

    intervalMode using (peer.intervalMode _, peer.intervalMode_= _)
    listDataSync using (listDataSyncVar _, listDataSyncVar= _)

    val changed = ReSwingEvent using (peer, classOf[ListSelectionChanged[A]])
  }

  object ReSelection {
    implicit def toSelection(selection: ReSelection) = selection.peer
  }

  object selection extends ReSelection(
      `selection.intervalMode`, `selection.listDataSync`)
}

object ReListView {
  implicit def toListView[A](component: ReListView[A]): ListView[A] = component.peer

  class ReListModel[A] extends javax.swing.AbstractListModel[A] {
    private var items = Seq.empty[A]
    def update(listData: Seq[A]) {
      val itemsSize = items.size
      val additional = listData.size - itemsSize
      items = listData

      if (additional > 0)
        fireIntervalAdded(this, itemsSize, listData.size - 1)
      if (additional < 0)
        fireIntervalRemoved(this, listData.size, itemsSize - 1)
      fireContentsChanged(this, 0, listData.size)
    }
    def getElementAt(n: Int) = items(n)
    def getSize = items.size
    def getItems = items
  }
}
