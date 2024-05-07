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
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.ui.dialog.EditBaseDialog;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.util.JjDefaults;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.awt.Font;

/**
 * A dialog to edit additional game information such as game name and description.
 * This dialog could be reused (a single instance could be reused for multiple shows).
 *
 * @author Yevgeny Nyden
 */
public class EditInfoDialog extends EditBaseDialog {

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
    this.initializeDialog(LocaleService.getString("jj.editinfo.title"),
        JjDefaults.EDIT_INFO_DIALOG_MIN_WIDTH, JjDefaults.EDIT_INFO_DIALOG_MIN_HEIGHT);
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

  /**
   * Updates the game info in the dialog and shows the dialog.
   * @param gameName current game name
   * @param gameDescription current description of the game
   */
  public void showDialog(String gameName, String gameDescription) {
    logger.info("Showing the dialog");
    this.gameNamePane.setText(gameName);
    this.gameDescriptionPane.setText(gameDescription);
    super.setVisible(true);
  }

  /** @inheritDoc */
  @Override
  protected void handleCancelAction() {
    logger.info("Cancelling the changes and closing the dialog.");
    this.setVisible(false);
  }

  /** @inheritDoc */
  @Override
  protected void handleOkAction() {
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

  /** @inheritDoc */
  @Override
  protected JPanel getDialogBodyComponent() {
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final int padding = lafTheme.getPanelPadding();
    Font labelFont = lafTheme.getButtonFont();
    JPanel bodyPanel = new JPanel();
    bodyPanel.setLayout(new TableLayout(new double[][] {
        {TableLayout.FILL}, // columns
        {TableLayout.PREFERRED, 3, TableLayout.PREFERRED, 30,
            TableLayout.PREFERRED, 3, TableLayout.FILL, padding, TableLayout.PREFERRED, padding}})); // rows

    // ******* Game name label and text pane.
    JLabel nameLabel = new JLabel();
    nameLabel.setText(LocaleService.getString("jj.editinfo.game.name"));
    nameLabel.setFont(labelFont);
    bodyPanel.add(nameLabel, new TableLayoutConstraints(
        0, 0, 0, 0, TableLayout.FULL, TableLayout.CENTER));
    this.gameNamePane = new JTextPane();
    bodyPanel.add(this.gameNamePane, new TableLayoutConstraints(
        0, 2, 0, 2, TableLayout.FULL, TableLayout.CENTER));

    // ******* Game description label and text pane.
    JLabel descriptionLabel = new JLabel();
    descriptionLabel.setText(LocaleService.getString("jj.editinfo.game.description"));
    descriptionLabel.setFont(labelFont);
    bodyPanel.add(descriptionLabel, new TableLayoutConstraints(
        0, 4, 0, 4, TableLayout.FULL, TableLayout.CENTER));
    this.gameDescriptionPane = new JTextPane();
    bodyPanel.add(this.gameDescriptionPane, new TableLayoutConstraints(
        0, 6, 0, 6, TableLayout.FULL, TableLayout.FULL));

    return bodyPanel;
  }

  /**
   * Shows the name error dialog to the user.
   */
  private void showNameErrorDialog() {
    super.showErrorDialog(
        LocaleService.getString("jj.editinfo.error.name.title"),
        LocaleService.getString("jj.editinfo.error.name.message"));
  }
}
