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

package net.curre.jjeopardy.ui.dialog;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.event.ClickAndKeyAction;
import net.curre.jjeopardy.images.ImageEnum;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.service.UiService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.util.Utilities;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Represents a baseclass for a basic modal dialog.
 * To create a dialog, extend this class and implement #setHeaderComponent
 * and #setContentComponent methods, and call #initializeDialog immediately after
 * creating an instance of this class (or in the subclass ctor).
 * To show dialog, call #showDialog.
 * <p />
 * Dialog has default handlers for the window close action (#handleCloseAction)
 * and the button action (#handleButtonAction), both of which just hide and dispose
 * the dialog. If alternative action is desired, override these methods.
 *
 * @author Yevgeny Nyden
 */
public abstract class BasicDialog extends JDialog {

  /** Width of the dialog icon column. */
  protected static final int ICON_COLUMN_WIDTH = 100;

  /** Width of the dialog text column. */
  protected static final int TEXT_COLUMN_WIDTH = 500;

  /** Max height for the main content text area. */
  private static final int MAX_TEXT_AREA_HEIGHT = 540;

  /** Ctor. */
  public BasicDialog() {
    this.setModal(true);

    if (Utilities.isMacOs()) {
      // Remove application name for the frame panel.
      this.getRootPane().putClientProperty("apple.awt.windowTitleVisible", false);
    }
  }

  /**
   * Initializes an instance of this dialog. Must be the first
   * method to call after creating an instance of this class.
   * @param title the title
   * @param iconOrNull icon image for the dialog or null is no icon should be displayed
   */
  public void initializeDialog(String title, ImageEnum iconOrNull) {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final int padding = lafTheme.getPanelPadding();

    this.setTitle(title);
    this.setResizable(false);
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    // Setting the dialog layout.
    int columnShift = 0;
    double[][] layoutDoubles = new double[2][];
    if (iconOrNull == null) {
      layoutDoubles[0] = new double[] {padding, TableLayout.PREFERRED, padding}; // columns
    } else {
      layoutDoubles[0] = new double[] {padding, ICON_COLUMN_WIDTH, padding, TableLayout.PREFERRED, padding}; // columns
      columnShift = 2; // shifting columns if the icon is displayed
    }
    layoutDoubles[1] = new double[] {padding, TableLayout.PREFERRED, padding, TableLayout.PREFERRED,
      padding, TableLayout.PREFERRED, padding + 5};  // rows
    this.setLayout(new TableLayout(layoutDoubles));

    // Optional icon.
    if (iconOrNull != null) {
      JLabel dialogIcon = new JLabel(iconOrNull.toImageIcon());
      this.add(dialogIcon, new TableLayoutConstraints(
        1, 3, 1, 3, TableLayout.CENTER, TableLayout.CENTER));
    }

    // The header.
    Component header = this.getHeaderComponent();
    this.add(header, new TableLayoutConstraints(
      1 + columnShift, 1, 1 + columnShift, 1, TableLayout.CENTER, TableLayout.CENTER));

    // The content.
    this.add(this.getContentComponent(), new TableLayoutConstraints(
      1 + columnShift, 3, 1 + columnShift, 3, TableLayout.CENTER, TableLayout.CENTER));

    // Action button.
    Component buttons = this.getButtonComponent();
    this.add(buttons, new TableLayoutConstraints(
      1 + columnShift, 5, 1 + columnShift, 5, TableLayout.CENTER, TableLayout.CENTER));
  }

  /**
   * Gets the button component.
   * @return default OK button
   */
  public Component getButtonComponent() {
    return this.createDefaultButton(null);
  }

  /**
   * Gets the header component for this dialog, which is displayed
   * at the very top of the panel in a bolder font.
   * @return the header component for this dialog
   */
  public abstract Component getHeaderComponent();

  /**
   * Gets the content component for this dialog, which is displayed
   * in the center of the content pane.
   * @return the content component for this dialog
   */
  public abstract Component getContentComponent();

  /**
   * Creates default OK button.
   * @return default button
   */
  protected JButton createDefaultButton(String textOrNull) {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    JButton button = new JButton();
    ClickAndKeyAction.createAndAddAction(button, this::handleButtonAction);
    button.setText(textOrNull == null ? LocaleService.getString("jj.dialog.button.ok") : textOrNull);
    button.setFont(lafTheme.getButtonFont());
    return button;
  }

  /**
   * Shows this dialog (also sets the relative location to the
   * passed parent if provided). If the parent is not provided, dialog
   * will have a default position.
   * @param parentOrNull parent component for the relative positioning of this dialog or null
   */
  public void showDialog(Component parentOrNull) {
    this.pack();
    this.setLocationRelativeTo(parentOrNull);
    this.setVisible(true);
  }

  /**
   * Creates a text area component to use for displaying
   * multi-line text in the dialogs.
   * @param message string to set as the area's text
   * @param fontSizeScale the multiplier to scale the font
   * @return created and initialized JTextArea component
   */
  protected JTextArea createTextArea(String message, float fontSizeScale) {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    Font font = lafTheme.getDialogTextFont();
    if (fontSizeScale != 1f) {
      font = font.deriveFont(font.getStyle(), font.getSize() * fontSizeScale);
    }
    JTextArea textArea = createDefaultTextArea(font);
    textArea.setText(message);

    // Determine the approximate minimum height of the text pane.
    int textAreaHeight = UiService.getHeightOfTextArea(this, font, message, TEXT_COLUMN_WIDTH, 3);
    if (textAreaHeight > MAX_TEXT_AREA_HEIGHT) {
      textAreaHeight = MAX_TEXT_AREA_HEIGHT;
    }
    textArea.setPreferredSize(new Dimension(TEXT_COLUMN_WIDTH, textAreaHeight));

    return textArea;
  }

  /**
   * Creates a basic text area component to use for displaying in dialogs.
   * @param font for the text Font
   * @return created and initialized JTextArea component
   */
  protected static JTextArea createDefaultTextArea(Font font) {
    JTextArea textArea = new JTextArea();

    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final Color backgroundColor = lafTheme.getDefaultBackgroundColor();

    textArea.setEditable(false);
    textArea.setFocusable(false);
    textArea.setDragEnabled(false);
    textArea.setWrapStyleWord(true);
    textArea.setLineWrap(true);
    textArea.setFont(font);
    textArea.setBackground(backgroundColor);
    textArea.setBorder(null);
    textArea.setOpaque(true);
    return textArea;
  }

  /**
   * Default handler for the button action, which hides and disposes
   * this dialog instance. Feel free to override with another handler (e.g.
   * to keep the dialog instance in the memory).
   */
  private void handleButtonAction() {
    this.setVisible(false);
    this.dispose();
  }
}
