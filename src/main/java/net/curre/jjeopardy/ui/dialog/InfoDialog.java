package net.curre.jjeopardy.ui.dialog;

import net.curre.jjeopardy.images.ImageEnum;
import net.curre.jjeopardy.service.AppRegistry;

import javax.swing.JLabel;
import java.awt.Component;

/**
 * Represents a game informational dialog (with only one action to acknowledge).
 *
 * @author Yevgeny Nyden
 */
public class InfoDialog extends BasicDialog {

  /** Dialog type. */
  public enum Type {INFO, WARNING, ERROR, END}

  /** Dialog title/header. */
  private final String title;

  /** Dialog message. */
  private final String message;

  /**
   * Creates a modal confirmation dialog.
   * @param title the title
   * @param message the text message
   * @param type type of the dialog
   */
  public InfoDialog(String title, String message, Type type) {
    this.title = title;
    this.message = message;
    ImageEnum icon;
    switch (type) {
      case END:
        icon = ImageEnum.WINNER_64;
        break;
      case ERROR:
        icon = ImageEnum.ERROR_64;
        break;
      case WARNING:
        icon = ImageEnum.WARN_64;
        break;
      default:
        icon = ImageEnum.INFO_64;
    }
    this.initializeDialog(title, icon);
  }

  @Override
  public Component getHeaderComponent() {
    JLabel label = new JLabel(this.title);
    label.setFont(AppRegistry.getInstance().getLafService().getCurrentLafTheme().getDialogHeaderFont());
    return label;
  }

  @Override
  public Component getContentComponent() {
    return this.createTextArea(this.message);
  }
}