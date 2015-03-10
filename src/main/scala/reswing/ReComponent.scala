package reswing

import scala.language.implicitConversions
import scala.swing.Color
import scala.swing.Component
import scala.swing.Dimension
import scala.swing.Font
import scala.swing.Graphics2D
import scala.swing.event._

abstract class ReComponent(
    val background: ReSwingValue[Color] = (),
    val foreground: ReSwingValue[Color] = (),
    val font: ReSwingValue[Font] = (),
    val enabled: ReSwingValue[Boolean] = (),
    minimumSize: ReSwingValue[Dimension] = (),
    maximumSize: ReSwingValue[Dimension] = (),
    preferredSize: ReSwingValue[Dimension] = ())
  extends
    ReUIElement(minimumSize, maximumSize, preferredSize) {
  
  override protected lazy val peer = new Component with ComponentMixin
  
  val hasFocus = ReSwingValue using (peer.hasFocus _, classOf[FocusGained], classOf[FocusLost])
  
  background using (peer.background _, peer.background_= _, "background")
  foreground using (peer.foreground _, peer.foreground_= _, "foreground")
  font using (peer.font _, peer.font_= _, "font")
  enabled using (peer.enabled _, peer.enabled_= _, "enabled")
  
  object mouse {
    object clicks {
      val clicked = ReSwingEvent using (peer.mouse.clicks, classOf[MouseClicked])
      val pressed = ReSwingEvent using (peer.mouse.clicks, classOf[MousePressed])
      val released = ReSwingEvent using (peer.mouse.clicks, classOf[MouseReleased])
    }
    
    object moves {
      val dragged = ReSwingEvent using (peer.mouse.moves, classOf[MouseDragged])
      val entered = ReSwingEvent using (peer.mouse.moves, classOf[MouseEntered])
      val exited = ReSwingEvent using (peer.mouse.moves, classOf[MouseExited])
      val moved = ReSwingEvent using (peer.mouse.moves, classOf[MouseMoved])
    }
    
    object wheel {
      val moved = ReSwingEvent using (peer.mouse.wheel, classOf[MouseWheelMoved])
    }
  }
  
  object keys {
    val pressed = ReSwingEvent using (peer.keys, classOf[KeyPressed])
    val released = ReSwingEvent using (peer.keys, classOf[KeyReleased])
    val typed = ReSwingEvent using (peer.keys, classOf[KeyTyped])
  }
  
  protected trait ComponentMixin extends Component {
    override def paintComponent(g: Graphics2D) = ReComponent.this.paintComponent(g)
    def __super__paintComponent(g: Graphics2D) = super.paintComponent(g)
    override def paintBorder(g: Graphics2D) = ReComponent.this.paintBorder(g)
    def __super__paintBorder(g: Graphics2D) = super.paintBorder(g)
    override def paintChildren(g: Graphics2D) = ReComponent.this.paintChildren(g)
    def __super__paintChildren(g: Graphics2D) = super.paintChildren(g)
    override def paint(g: Graphics2D) = {
      ReComponent.this.location() = location
      ReComponent.this.bounds() = bounds
      ReComponent.this.size() = size
      ReComponent.this.paint(g)
    }
    def __super__paint(g: Graphics2D) = super.paint(g)
  }

  protected def paintComponent(g: Graphics2D) = peer.__super__paintComponent(g)
  protected def paintBorder(g: Graphics2D) = peer.__super__paintBorder(g)
  protected def paintChildren(g: Graphics2D) = peer.__super__paintChildren(g)
  def paint(g: Graphics2D) = peer.__super__paint(g)
}

object ReComponent {
  implicit def toComponent(component: ReComponent): Component = component.peer
}
