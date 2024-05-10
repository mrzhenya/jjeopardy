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
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.util.Utilities;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Base dialog for editing game information such as question, players, etc.
 *
 * @author Yevgeny Nyden
 */
public abstract class EditBaseDialog extends JDialog {

  /** Ctor. */
  public EditBaseDialog() {
    // Providing a new frame for the dialog will enable handling
    // multiple JDialogs at the same time.
    super(new JFrame());

    this.setModal(true);
    this.setAlwaysOnTop(true);
    this.setResizable(false);
  }

  /** Installs the ESC key handler. */
  @Override
  protected JRootPane createRootPane() {
    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    JRootPane rootPane = new JRootPane();

    ActionListener actionListener = actionEvent -> EditBaseDialog.this.handleEscKeyPress();
    rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    return rootPane;
  }

  /** Handles ESC key press event. */
  protected void handleEscKeyPress() {
    this.handleCancelAction();
  }

  /**
   * Initializes the dialog.
   * @param title title for this dialog
   * @param width dialogs' width
   * @param height dialog's height
   */
  protected void initializeDialog(String title, int width, int height) {
    this.setPreferredSize(new Dimension(width, height));
    this.setTitle(title);

    this.initComponents();

    if (Utilities.isMacOs()) {
      // Remove application name for the frame panel.
      this.getRootPane().putClientProperty( "apple.awt.windowTitleVisible", false);
    }

    this.pack();
  }

  /**
   * Creates the body component for this dialog.
   * @return panel with the main content of the dialog
   */
  protected abstract JPanel getDialogBodyComponent();

  /**
   * Handles cancelling the changes action and closes the dialog.
   */
  protected abstract void handleCancelAction();

  /**
   * Handles saving the changes action and closes the dialog on success.
   */
  protected abstract void handleOkAction();

  /**
   * Shows an error dialog to the user.
   * @param title error dialog title
   * @param message error dialog message
   */
  protected void showErrorDialog(String title, String message) {
    this.setModal(false);
    this.setAlwaysOnTop(false);
    AppRegistry.getInstance().getUiService().showErrorDialog(title, message, this);
    this.setModal(true);
    this.setAlwaysOnTop(true);
  }

  /** Initializes the layout and components for this dialog. */
  private void initComponents() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final int padding = lafTheme.getPanelPadding();

    // Settings the main layout.
    this.setLayout(new TableLayout(new double[][] {
        {padding, TableLayout.FILL, padding}, // columns
        {padding, TableLayout.FILL, padding, TableLayout.PREFERRED, padding}})); // rows

    // ******* Main content panel.
    JPanel bodyPanel = this.getDialogBodyComponent();
    this.add(bodyPanel, new TableLayoutConstraints(
        1, 1, 1, 1, TableLayout.FULL, TableLayout.FULL));

    // ******* Buttons panel.
    JPanel buttonPanel = this.createButtonPanel();
    this.add(buttonPanel, new TableLayoutConstraints(
        1, 3, 1, 3, TableLayout.CENTER, TableLayout.CENTER));
  }

  /**
   * Creates and initializes the button panel that's displayed at the bottom of the dialog.
   * @return panel that contains the action buttons
   */
  private @NotNull JPanel createButtonPanel() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final int spacing = lafTheme.getButtonSpacing();
    JPanel panel = new JPanel();

    // ******* Save button.
    final JButton okButton = new JButton();
    ClickAndKeyAction.createAndAddAction(okButton, this::handleOkAction);
    okButton.setFont(lafTheme.getButtonFont());
    okButton.setText(LocaleService.getString("jj.dialog.button.ok"));
    panel.add(okButton);
    panel.add(Box.createRigidArea(new Dimension(spacing, 1)));

    // ******* Cancel button.
    final JButton cancelButton = new JButton();
    ClickAndKeyAction.createAndAddAction(cancelButton, this::handleCancelAction);
    cancelButton.setFont(lafTheme.getButtonFont());
    cancelButton.setText(LocaleService.getString("jj.dialog.button.cancel"));
    panel.add(cancelButton);

    return panel;
  }
}
