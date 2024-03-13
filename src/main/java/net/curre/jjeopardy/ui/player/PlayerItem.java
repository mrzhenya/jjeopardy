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
import net.curre.jjeopardy.App;
import net.curre.jjeopardy.event.ClickAndKeyAction;
import net.curre.jjeopardy.service.LocaleService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Represents a single row component where player name is provided with
 * Add/Remove buttons.
 *
 * @author Yevgeny Nyden
 */
class PlayerItem extends JPanel {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(App.class.getName());

  /** Reference to the PlayersPane component. */
  private final PlayersPane playersPane;

  /** Player's into label. */
  private final JLabel playerLabel;

  /** Player's name input field. */
  private final JTextField inputField;

  /** Player's Remove button. */
  private final JButton removeButton;

  /** Player's Add button. */
  private final JButton addButton;

  /** This player's index in the list. */
  private int playerIndex;

  /**
   * Ctor.
   * @param playerNameOrNull player name or null
   * @param playerIndex player's index on the list
   * @param playersPane reference to the PlayersPane
   */
  public PlayerItem(String playerNameOrNull, int playerIndex, PlayersPane playersPane) {
    this.playerIndex = playerIndex;
    this.playersPane = playersPane;
    this.setLayout(new TableLayout(new double[][] {
      {TableLayout.FILL, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 10, 50, 6,
        50, TableLayout.FILL},  // columns
      {5, TableLayout.PREFERRED, 5}}));  // rows

    // ******* Player index label.
    this.playerLabel = new JLabel();
    this.playerLabel.setText(this.getPlayerLabel());
    this.add(this.playerLabel, new TableLayoutConstraints(
      1, 1, 1, 1, TableLayout.CENTER, TableLayout.CENTER));

    // ******* Player input field.
    this.inputField = new JTextField(12);
    if (playerNameOrNull != null) {
      this.inputField.setText(playerNameOrNull);
    }
    this.add(this.inputField, new TableLayoutConstraints(
      3, 1, 3, 1, TableLayout.CENTER, TableLayout.CENTER));

    // ******* Remove player button.
    this.removeButton = new JButton();
    ClickAndKeyAction.createAndAddAction(this.removeButton, this::handleRemovePlayerAction);
    this.removeButton.setText(" " + LocaleService.getString("jj.playerdialog.addplayers.button.remove") + " ");
    // By default, the button is disabled and not visible.
    this.removeButton.setEnabled(false);
    this.removeButton.setVisible(false);
    this.add(this.removeButton, new TableLayoutConstraints(
      5, 1, 5, 1, TableLayout.CENTER, TableLayout.CENTER));

    // ******* Add a new player button.
    this.addButton = new JButton();
    ClickAndKeyAction.createAndAddAction(this.addButton, this::handleAddPlayerAction);
    this.addButton.setText(" " + LocaleService.getString("jj.playerdialog.addplayers.button.add") + " ");
    // By default, the button is disabled and not visible.
    this.addButton.setEnabled(false);
    this.addButton.setVisible(false);
    this.add(this.addButton, new TableLayoutConstraints(
      7, 1, 7, 1, TableLayout.CENTER, TableLayout.CENTER));
  }

  /**
   * Gets the player's current raw name.
   * @return player's name (non-trimmed)
   */
  protected String getPlayerName() {
    return this.inputField.getText();
  }

  /**
   * Updates player name in the input text field.
   * @param playerName player name
   */
  protected void updatePlayer(String playerName) {
    this.inputField.setText(playerName);
  }

  /**
   * Updates the current player's index and label.
   * @param ind the new players' index
   */
  protected void updateIndex(int ind) {
    this.playerIndex = ind;
    this.playerLabel.setText(this.getPlayerLabel());
  }

  /**
   * Updates states of the Add and Remove buttons.
   * @param addVisible Add should be visible
   * @param removeVisible Remove should be visible
   */
  protected void updateButtonState(boolean addVisible, boolean removeVisible) {
    this.addButton.setEnabled(addVisible);
    this.addButton.setVisible(addVisible);
    this.removeButton.setEnabled(removeVisible);
    this.removeButton.setVisible(removeVisible);
  }

  /**
   * Generates player number label.
   * @return String that precedes the text input field.
   */
  private String getPlayerLabel() {
    return LocaleService.getString("jj.player.name", String.valueOf(this.playerIndex + 1));
  }

  /** Handles add player button action. */
  protected void handleAddPlayerAction() {
    logger.info("Adding player.");
    this.playersPane.addNewPlayerItem(null);
  }

  /** Handles remove player button action. */
  protected void handleRemovePlayerAction() {
    logger.info("Removing player.");
    this.playersPane.removePlayerItem(this.playerIndex);
  }
}
