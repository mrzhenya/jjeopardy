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
import net.curre.jjeopardy.ui.laf.theme.LafTheme;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;

/**
 * Represents a game confirmation dialog (with two actions - to accept or to deny).
 *
 * @author Yevgeny Nyden
 */
public class ConfirmDialog extends BasicDialog {

  /** Dialog title/header. */
  private final String title;

  /** Dialog message. */
  private final String message;

  /** Handler for the Yes action. */
  private final Runnable yesHandler;

  /**
   * Creates a modal confirmation dialog.
   * @param title the title
   * @param message the text message
   * @param yesHandler handler for the Yes action
   */
  public ConfirmDialog(String title, String message, Runnable yesHandler) {
    this.title = title;
    this.message = message;
    this.yesHandler = yesHandler;
    this.initializeDialog(title, ImageEnum.QUESTION_BLUE_64);
  }

  @Override
  public Component getHeaderComponent() {
    JLabel label = new JLabel(this.title);
    label.setFont(AppRegistry.getInstance().getLafService().getCurrentLafTheme().getDialogHeaderFont());
    return label;
  }

  @Override
  public Component getContentComponent() {
    return this.createTextArea(this.message, 1);
  }

  /**
   * Gets the button panel.
   * @return panel with buttons for this dialog.
   */
  public Component getButtonComponent() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final int padding = lafTheme.getPanelPadding();
    JPanel panel = new JPanel(new TableLayout(new double[][] {
        {TableLayout.PREFERRED, padding, TableLayout.PREFERRED}, // columns
        {TableLayout.PREFERRED}})); // rows

    JButton yesButton = new JButton();
    ClickAndKeyAction.createAndAddAction(yesButton, this::handleYesAction);
    yesButton.setText(LocaleService.getString("jj.dialog.button.yes"));
    yesButton.setFont(lafTheme.getButtonFont());
    panel.add(yesButton, new TableLayoutConstraints(
        0, 0, 0, 0, TableLayout.CENTER, TableLayout.CENTER));

    JButton defaultButton = createDefaultButton(LocaleService.getString("jj.dialog.button.no"));
    panel.add(defaultButton, new TableLayoutConstraints(
        2, 0, 2, 0, TableLayout.CENTER, TableLayout.CENTER));
    SwingUtilities.invokeLater(defaultButton::requestFocus);
    return panel;
  }

  /**
   * Handles the yes action.
   */
  private void handleYesAction() {
    this.yesHandler.run();
    this.setVisible(false);
    this.dispose();
  }
}
