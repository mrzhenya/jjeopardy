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

import net.curre.jjeopardy.ui.dialog.BasicDialog;
import net.curre.jjeopardy.ui.dialog.ConfirmDialog;
import net.curre.jjeopardy.ui.dialog.ParsingResultDialog;

import java.awt.Color;
import java.awt.Component;
import java.util.logging.Logger;

/**
 * Service responsible for some common UI look and feel issues.
 *
 * @author Yevgeny Nyden
 */
public class UiService {

  /** Preferred width for the dialog. */
  private static final int DIALOG_WIDTH = 400;

  /** Preferred height for the dialog. */
  private static final int DIALOG_HEIGHT = 450;

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(UiService.class.getName());

  /** This holds a reference to the singleton instance. */
  private static final UiService INSTANCE = new UiService();

  /** Private constructor. */
  private UiService() {}

  /**
   * Returns an instance of this class to use.
   *
   * @return singleton instance of this class to use.
   */
  public static UiService getInstance() {
    return INSTANCE;
  }

  /**
   * Creates a new color that is darker than the
   * passed color according to the passed int parameter.
   * @param color    Model color
   * @param decrease Value to be subtracted from the RGB channels of the model color
   * @return New darker color
   */
  public static Color createDarkerColor(Color color, int decrease) {
    return new Color(color.getRed() - decrease,
      color.getGreen() - decrease,
      color.getBlue() - decrease);
  }

  /**
   * Displays a simple info dialog.
   * @param title the title text
   * @param message text content for the dialog
   * @param parentComponent reference to the relative parent for component position
   */
  public void showInfoDialog(String title, String message, Component parentComponent) {
    ConfirmDialog dialog = new ConfirmDialog(title, message, ConfirmDialog.Type.INFO);
    dialog.showDialog(parentComponent);
  }

  /**
   * Displays a simple warning dialog.
   * @param title the title text
   * @param message text content for the dialog
   * @param parentComponent reference to the relative parent for component position
   */
  public void showWarningDialog(String title, String message, Component parentComponent) {
    ConfirmDialog dialog = new ConfirmDialog(title, message, ConfirmDialog.Type.WARNING);
    dialog.showDialog(parentComponent);
  }

  /**
   * Displays the final, end game dialog.
   * @param title the title text
   * @param message text content for the dialog
   */
  public void showEndGameDialog(String title, String message) {
    ConfirmDialog dialog = new ConfirmDialog(title, message, ConfirmDialog.Type.END);
    dialog.showDialog(null);
  }

  /**
   * Displays a dialog with the parsing results.
   * @param result file parsing result
   * @param parent parent UI component to show this dialog relative to
   */
  public void showParsingResult(FileParsingResult result, Component parent) {
    BasicDialog dialog = new ParsingResultDialog(result);
    dialog.showDialog(parent);
  }

  /**
   * Displays a dialog to tell the user to restart the app.
   */
  public void showRestartGameDialog() {
    this.showInfoDialog(
      LocaleService.getString("jj.dialog.restart.title"),
      LocaleService.getString("jj.dialog.restart.message"),
      null
    );
  }
}
