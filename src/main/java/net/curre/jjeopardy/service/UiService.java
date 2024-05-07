/*
 * Copyright 2024 Yevgeny Nyden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.curre.jjeopardy.service;

import net.curre.jjeopardy.bean.FileParsingResult;
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.ui.dialog.BasicDialog;
import net.curre.jjeopardy.ui.dialog.ConfirmDialog;
import net.curre.jjeopardy.ui.dialog.EndGameDialog;
import net.curre.jjeopardy.ui.dialog.GameInfoDialog;
import net.curre.jjeopardy.ui.dialog.InfoDialog;
import net.curre.jjeopardy.ui.dialog.ParsingResultDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.awt.Component;

/**
 * Service responsible for common UI tasks like opening dialogs.
 *
 * @author Yevgeny Nyden
 */
public class UiService {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(UiService.class.getName());

  /** Ctor. */
  protected UiService() {}

  /**
   * Displays a simple info dialog.
   * @param title the title text
   * @param message text content for the dialog
   * @param parentComponent reference to the relative parent for component position
   */
  public void showInfoDialog(String title, String message, Component parentComponent) {
    logger.info("Showing info dialog: " + title);
    InfoDialog dialog = new InfoDialog(title, message, InfoDialog.Type.INFO);
    dialog.showDialog(parentComponent);
  }

  /**
   * Displays a simple info dialog.
   * @param title the title text
   * @param message text content for the dialog
   * @param yesHandler handler for the Yes action (required)
   * @param noHandler handler for the No action (optional)
   * @param parentComponent reference to the relative parent for component position
   */
  public void showConfirmationDialog(@NotNull String title, String message,
                                     @NotNull Runnable yesHandler, @Null Runnable noHandler,
                                     Component parentComponent) {
    logger.info("Showing confirmation dialog: " + title);
    ConfirmDialog dialog = new ConfirmDialog(title, message, yesHandler, noHandler);
    dialog.showDialog(parentComponent);
  }

  /**
   * Displays a simple warning dialog.
   * @param title the title text
   * @param message text content for the dialog
   * @param parentComponent reference to the relative parent for component position
   */
  public void showWarningDialog(String title, String message, Component parentComponent) {
    logger.info("Showing warning dialog: " + title);
    InfoDialog dialog = new InfoDialog(title, message, InfoDialog.Type.WARNING);
    dialog.showDialog(parentComponent);
  }

  /**
   * Displays a simple error dialog.
   * @param title the title text
   * @param message text content for the dialog
   * @param parentComponent reference to the relative parent for component position
   */
  public void showErrorDialog(String title, String message, Component parentComponent) {
    logger.info("Showing warning dialog: " + title);
    InfoDialog dialog = new InfoDialog(title, message, InfoDialog.Type.ERROR);
    dialog.showDialog(parentComponent);
    java.awt.EventQueue.invokeLater(() -> {
      dialog.toFront();
      dialog.repaint();
    });
  }

  /**
   * Displays the final, end game dialog.
   * @param winnerName the winner name
   * @param winnerScore winner's final score
   */
  public void showEndGameDialog(String winnerName, int winnerScore) {
    logger.info("Showing end game dialog");
    EndGameDialog dialog = new EndGameDialog(winnerName, winnerScore);
    dialog.showDialog(null);
  }

  /**
   * Displays a dialog with the parsing results.
   * @param result file parsing result
   * @param parent parent UI component to show this dialog relative to
   */
  public void showParsingResult(FileParsingResult result, Component parent) {
    logger.info("Showing parsing result dialog");
    BasicDialog dialog = new ParsingResultDialog(result);
    dialog.showDialog(parent);
  }

  /**
   * Displays a dialog to show game info.
   * @param gameData game data
   * @param parent parent UI component to show this dialog relative to
   */
  public void showGameInfoDialog(GameData gameData, Component parent) {
    logger.info("Showing parsing result dialog");
    BasicDialog dialog = new GameInfoDialog(gameData);
    dialog.pack();
    dialog.showDialog(parent);
  }

  /**
   * Displays a dialog to tell the user to restart the app.
   */
  public void showRestartGameDialog() {
    logger.info("Showing restart game info dialog");
    this.showInfoDialog(
      LocaleService.getString("jj.dialog.restart.title"),
      LocaleService.getString("jj.dialog.restart.message"),
      null
    );
  }

  /**
   * Creates an instance of <code>JTextPane</code> initialized to default, non-editable
   * state and center aligned text.
   * @return an instance of <code>JTextPane</code> initialized to defaults
   */
  public static @NotNull JTextPane createDefaultTextPane() {
    JTextPane textPane = new JTextPane();
    textPane.setEditable(false);
    textPane.setFocusable(false);
    textPane.setDragEnabled(false);
    textPane.setOpaque(true);

    addAlignCenterToTextPane(textPane);

    return textPane;
  }

  /**
   * Adds style alignment attributes so that the text is centered in the passed text pane.
   * @param textPane text pane to add style attributes to
   */
  public static void addAlignCenterToTextPane(@NotNull JTextPane textPane) {
    StyledDocument doc = textPane.getStyledDocument();
    SimpleAttributeSet center = new SimpleAttributeSet();
    StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
    doc.setParagraphAttributes(0, doc.getLength(), center, false);
  }
}
