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

package net.curre.jjeopardy.ui.player;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.event.ClickAndKeyAction;
import net.curre.jjeopardy.event.ClosingWindowListener;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.util.JjDefaults;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.validation.constraints.NotNull;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

/**
 * Dialog to add or update players for a game.
 *
 * @author Yevgeny Nyden
 */
public class PlayerDialog extends JDialog {

  /** Class logger. */
  private static final Logger logger = LogManager.getLogger(PlayerDialog.class.getName());

  /** Container of the players' names/data. */
  private PlayersPane playersPane;

  /** Code to run when players have been updated (on a valid Save button action). */
  private final Runnable updatePlayersFn;

  /**
   * Ctor.
   * @param updatePlayersFn handler for a valid Save button action
   */
  public PlayerDialog(Runnable updatePlayersFn) {
    // Providing a new frame for the dialog will enable handling
    // multiple JDialogs at the same time.
    super(new JFrame());

    this.updatePlayersFn = updatePlayersFn;
    this.setTitle(LocaleService.getString("jj.playerdialog.addplayers.title"));
    this.setResizable(false);
    this.setModal(true);
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new ClosingWindowListener(this::handleWindowClosing));

    this.initComponents();
  }

  /**
   * Updates the players in the dialog and shows the dialog.
   * @param playerNames most current list of player names
   */
  public void showDialog(@NotNull List<String> playerNames) {
    this.playersPane.updatePlayersPane(playerNames);
    super.setVisible(true);
  }

  /**
   * Gets the current dialog player names. If this method is called in a handler
   * for the Save button, it can be assumed the players value is valid (min count of not blank names).
   * @return list of player names
   */
  public @NotNull List<String> getPlayerNames() {
    return this.playersPane.getPlayerNames();
  }

  /**
   * Initializes the dialog's UI components.
   */
  private void initComponents() {
    // Setting the main layout.
    this.setLayout(new TableLayout(new double[][] {
      {TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED},  // columns
      {15, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, 20}}));  // rows

    Font font = AppRegistry.getInstance().getLafService().getCurrentLafTheme().getButtonFont();

    // ******* Top panel label.
    final JLabel label = new JLabel();
    label.setFont(font);
    label.setText(LocaleService.getString("jj.playerdialog.addplayers.header"));
    this.add(label, new TableLayoutConstraints(
      1, 1, 1, 1, TableLayout.CENTER, TableLayout.CENTER));

    // ******* Players pane.
    this.playersPane = new PlayersPane();
    this.add(this.playersPane, new TableLayoutConstraints(
      1, 3, 1, 3, TableLayout.CENTER, TableLayout.CENTER));

    // ******* Delete All button.
    JPanel buttonPanel = new JPanel();
    JButton deleteAllButton = new JButton();
    ClickAndKeyAction.createAndAddAction(deleteAllButton, this::handleDeleteAllPlayersAction);
    deleteAllButton.setFont(font);
    deleteAllButton.setText(LocaleService.getString("jj.dialog.button.deleteall"));
    buttonPanel.add(deleteAllButton);

    // ******* Save button.
    JButton saveButton = new JButton();
    ClickAndKeyAction.createAndAddAction(saveButton, this::handleSavePlayersAction);
    saveButton.setFont(font);
    saveButton.setText(LocaleService.getString("jj.dialog.button.save"));
    buttonPanel.add(saveButton);

    this.add(buttonPanel, new TableLayoutConstraints(
      1, 5, 1, 5, TableLayout.CENTER, TableLayout.CENTER));

    this.setPreferredSize(new Dimension(JjDefaults.PLAYER_DIALOG_WIDTH, JjDefaults.PLAYER_DIALOG_HEIGHT));
    this.pack();
  }

  /** Handles the Delete all players action. */
  private void handleDeleteAllPlayersAction() {
    logger.info("Deleting all players.");
    PlayerDialog.this.playersPane.deleteAllPlayers();
    this.updatePlayersFn.run();
    this.setVisible(false);
  }

  /** Handles the Save players action. */
  private void handleSavePlayersAction() {
    logger.info("Saving players.");

    this.playersPane.cleanEmptyPlayers();
    List<String> playerNames = this.playersPane.getPlayerNames();
    if (playerNames.size() < JjDefaults.MIN_NUMBER_OF_PLAYERS) {
      logger.info("Not enough non-blank player names");
      this.setVisible(false);
      AppRegistry.getInstance().getUiService().showWarningDialog(
          LocaleService.getString("jj.playerdialog.addplayers.warn.title"),
          LocaleService.getString("jj.playerdialog.addplayers.warn.msg",
              String.valueOf(JjDefaults.MIN_NUMBER_OF_PLAYERS)),this);
      this.setVisible(true);
      return;
    }

    this.updatePlayersFn.run();
    this.setVisible(false);
  }

  /** Clears the players pane when window closes. */
  private void handleWindowClosing() {
    // Closing window has no effect on the game state.
    this.setVisible(false);
    this.playersPane.cleanEmptyPlayers();
  }
}
