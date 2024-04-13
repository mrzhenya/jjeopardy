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

package net.curre.jjeopardy.ui.edit;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.event.ClickAndKeyAction;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.util.JjDefaults;
import net.curre.jjeopardy.util.Utilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.awt.Dimension;
import java.awt.Font;

/**
 * A dialog to edit additional game information such as game name and description.
 *
 * @author Yevgeny Nyden
 */
public class EditInfoDialog extends JDialog {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(EditInfoDialog.class.getName());

  /** Code to run to update the game information on the Save action. */
  private final Runnable updateGameInfoFn;

  /** Game name text input pane. */
  private JTextPane gameNamePane;

  /** Game description input text pane. */
  private JTextPane gameDescriptionPane;

  /**
   * Ctor.
   * @param updateGameInfoFn function to run to save game additional information
   */
  public EditInfoDialog(Runnable updateGameInfoFn) {
    this.updateGameInfoFn = updateGameInfoFn;

    this.setModal(true);
    this.setAlwaysOnTop(true);
    this.setResizable(false);
    this.setPreferredSize(new Dimension(JjDefaults.EDIT_INFO_DIALOG_MIN_WIDTH, JjDefaults.EDIT_INFO_DIALOG_MIN_HEIGHT));
    this.setTitle(LocaleService.getString("jj.editinfo.title"));

    this.initComponents();

    if (Utilities.isMacOs()) {
      // Remove application name for the frame panel.
      this.getRootPane().putClientProperty( "apple.awt.windowTitleVisible", false);
    }

    this.pack();
  }

  /**
   * Updates the game info in the dialog and shows the dialog.
   * @param gameData current game data
   */
  public void showDialog(@NotNull GameData gameData) {
    logger.info("Showing the dialog");
    this.gameNamePane.setText(gameData.getGameName());
    this.gameDescriptionPane.setText(gameData.getGameDescription());
    super.setVisible(true);
  }

  /** Initializes the layout and components for this dialog. */
  private void initComponents() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final int padding = lafTheme.getPanelPadding();
    Font labelFont = lafTheme.getButtonFont();

    // Settings the main layout.
    this.setLayout(new TableLayout(new double[][] {
        {padding, TableLayout.FILL, padding}, // columns
        {padding, TableLayout.PREFERRED, 3, TableLayout.PREFERRED, 30,
            TableLayout.PREFERRED, 3, TableLayout.FILL, padding, TableLayout.PREFERRED, padding}})); // rows

    // ******* Game name label and text pane.
    JLabel nameLabel = new JLabel();
    nameLabel.setText(LocaleService.getString("jj.editinfo.game.name"));
    nameLabel.setFont(labelFont);
    this.add(nameLabel, new TableLayoutConstraints(
        1, 1, 1, 1, TableLayout.FULL, TableLayout.CENTER));
    this.gameNamePane = new JTextPane();
    this.add(this.gameNamePane, new TableLayoutConstraints(
        1, 3, 1, 3, TableLayout.FULL, TableLayout.CENTER));

    // ******* Game description label and text pane.
    JLabel descriptionLabel = new JLabel();
    descriptionLabel.setText(LocaleService.getString("jj.editinfo.game.description"));
    descriptionLabel.setFont(labelFont);
    this.add(descriptionLabel, new TableLayoutConstraints(
        1, 5, 1, 5, TableLayout.FULL, TableLayout.CENTER));
    this.gameDescriptionPane = new JTextPane();
    this.add(this.gameDescriptionPane, new TableLayoutConstraints(
        1, 7, 1, 7, TableLayout.FULL, TableLayout.FULL));

    // ******* Buttons panel.
    JPanel buttonPanel = this.createButtonPanel();
    this.add(buttonPanel, new TableLayoutConstraints(
        1, 9, 1, 9, TableLayout.CENTER, TableLayout.CENTER));
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
    ClickAndKeyAction.createAndAddAction(okButton, this::handleOkQuestionAction);
    okButton.setFont(lafTheme.getButtonFont());
    okButton.setText(LocaleService.getString("jj.dialog.button.ok"));
    panel.add(okButton);
    panel.add(Box.createRigidArea(new Dimension(spacing, 1)));

    // ******* Cancel button.
    final JButton cancelButton = new JButton();
    ClickAndKeyAction.createAndAddAction(cancelButton, this::handleCancelQuestionAction);
    cancelButton.setFont(lafTheme.getButtonFont());
    cancelButton.setText(LocaleService.getString("jj.dialog.button.cancel"));
    panel.add(cancelButton);

    return panel;
  }

  /**
   * Handles cancelling the changes action and closes the dialog.
   */
  private void handleCancelQuestionAction() {
    logger.info("Cancelling the changes and closing the dialog.");
    this.setVisible(false);
  }

  /**
   * Handles saving the changes action and closes the dialog.
   */
  private void handleOkQuestionAction() {
    logger.info("Saving the game info changes and closing the dialog.");

    String nameText = StringUtils.trimToNull(this.gameNamePane.getText());
    // If there are errors, stop here.
    if (nameText == null) {
      this.showNameErrorDialog();
      return;
    }

    this.updateGameInfoFn.run();
    this.setVisible(false);
  }

  /**
   * Shows the name error dialog to the user.
   */
  private void showNameErrorDialog() {
    this.setModal(false);
    this.setAlwaysOnTop(false);
    AppRegistry.getInstance().getUiService().showErrorDialog(
        LocaleService.getString("jj.editinfo.error.name.title"),
        LocaleService.getString("jj.editinfo.error.name.message"), this);
    this.setModal(true);
    this.setAlwaysOnTop(true);
  }

  /**
   * Gets the current game name.
   * @return game name
   */
  public @NotNull String getGameName() {
    String gameName = StringUtils.trimToNull(this.gameNamePane.getText());
    assert(gameName != null);
    return gameName;
  }

  /**
   * Gets the current game description.
   * @return game description or null if non set
   */
  public @Null String getGameDescription() {
    return StringUtils.trimToNull(this.gameDescriptionPane.getText());
  }
}
