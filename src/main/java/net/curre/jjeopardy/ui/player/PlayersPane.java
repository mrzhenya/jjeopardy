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
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.GameDataService;
import org.apache.commons.lang3.StringUtils;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * UI container for the player data - rows of items, each of which has
 * a label, a text input field, and buttons to Remove/Add players.
 * @author Yevgeny Nyden
 */
public class PlayersPane extends JPanel {

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(PlayersPane.class.getName());

  /** Container (ONLY!) for the player item components. */
  private final JPanel containerPane;

  /** Ctor. */
  public PlayersPane() {
    super(new TableLayout(new double[][] {
      {10, TableLayout.FILL, 10},  // columns
      {15, TableLayout.PREFERRED, TableLayout.FILL, 10} // rows
    }));

    this.containerPane = new JPanel();
    this.containerPane.setLayout(new BoxLayout(this.containerPane, BoxLayout.Y_AXIS));
    this.add(this.containerPane, new TableLayoutConstraints(
      1, 1, 1, 1, TableLayout.CENTER, TableLayout.CENTER));

    this.initDefaultPlayersPane();
  }

  /**
   * Gets the currently added names ignoring the blank ones.
   * @return a list of non-blank strings representing players
   */
  public List<String> getPlayerNames() {
    List<String> names = new ArrayList<>();
    for (int ind = 0; ind < this.containerPane.getComponentCount(); ind++) {
      PlayerItem playerItem = (PlayerItem) this.containerPane.getComponent(ind);
      if (!StringUtils.isBlank(playerItem.getPlayerName())) {
        names.add(playerItem.getPlayerName().trim());
      }
    }
    return names;
  }

  /**
   * Adds a new player item at the end of the list.
   * @param playerNameOrNull player name or null
   */
  protected void addNewPlayerItem(String playerNameOrNull) {
    // Redundant check to ensure we don't go above max players.
    int nextPlayerIndex = this.containerPane.getComponentCount();
    if (nextPlayerIndex == AppRegistry.getInstance().getGameDataService().getMaxNumberOfPlayers()) {
      LOGGER.warning("Trying to add too many players, ignoring...");
      return;
    }

    this.containerPane.add(new PlayerItem(playerNameOrNull, nextPlayerIndex, this));
    this.updateButtonState();
    this.revalidate();
  }

  /**
   * Removes player item given its index.
   * @param playerIndex index of the player's line to remove
   */
  protected void removePlayerItem(int playerIndex) {
    this.containerPane.remove(playerIndex);

    // Update the player index labels.
    for (int ind = playerIndex; ind < this.containerPane.getComponentCount(); ind++) {
      ((PlayerItem) this.containerPane.getComponent(ind)).updateIndex(ind);
    }

    this.updateButtonState();
  }

  /**
   * Prunes empty lines from the players pane.
   */
  protected void cleanEmptyPlayers() {
    int minNumberOfPlayers = AppRegistry.getInstance().getGameDataService().getMinNumberOfPlayers();
    for (int ind = 0; ind < this.containerPane.getComponentCount(); ind++) {
      PlayerItem playerItem = (PlayerItem) this.containerPane.getComponent(ind);
      if (StringUtils.isBlank(playerItem.getPlayerName()) && ind >=
              minNumberOfPlayers) {
        this.removePlayerItem(ind--);
      }
    }
  }

  /**
   * Updates the players pane with the player loaded from a game file.
   * @param playerNames list of player names
   */
  protected void updatePlayersPane(List<String> playerNames) {
    if (playerNames.size() < AppRegistry.getInstance().getGameDataService().getMinNumberOfPlayers()) {
      LOGGER.warning("Provided too few players, ignoring.");
      if (this.containerPane.getComponentCount() == 0) {
        this.initDefaultPlayersPane();
      }
      return;
    }
    // Updating player text labels from the passed array.
    final int currPlayersNum = this.containerPane.getComponentCount();
    for (int ind = 0; ind < playerNames.size(); ind++) {
      String playerName = playerNames.get(ind);
      if (ind < currPlayersNum) {
        ((PlayerItem) this.containerPane.getComponent(ind)).updatePlayer(playerName);
      } else {
        this.addNewPlayerItem(playerName);
      }
    }
    // If there are more players in the UI, removing them.
    if (playerNames.size() < currPlayersNum) {
      for (int ind = playerNames.size(); ind < currPlayersNum; ind++) {
        this.removePlayerItem(playerNames.size());
      }
    }
    this.updateButtonState();
  }

  /**
   * Initializes the players pane with default empty player lines.
   */
  private void initDefaultPlayersPane() {
    int minNumberOfPlayers = AppRegistry.getInstance().getGameDataService().getMinNumberOfPlayers();
    for (int ind = 0; ind < minNumberOfPlayers; ind++) {
      this.addNewPlayerItem(null);
    }
  }

  /**
   * Updates the Add/Remove buttons state - Add buttons is added to
   * the last row only, and Remove button is not available on the default
   * (min players) rows.
   */
  private void updateButtonState() {
    int numberOfPlayers = this.containerPane.getComponentCount();
    GameDataService gameService = AppRegistry.getInstance().getGameDataService();
    int maxNumberOfPlayers = gameService.getMaxNumberOfPlayers();
    int minNumberOfPlayers = gameService.getMinNumberOfPlayers();

    for (int ind = 0; ind < numberOfPlayers; ind++) {
      PlayerItem playerItem = (PlayerItem) this.containerPane.getComponent(ind);
      // Add button is visible only on the last row.
      boolean addVisible = (ind + 1 == numberOfPlayers) &&
        (ind + 1 < maxNumberOfPlayers);
      // Remove buttons are visible only on the non-default rows.
      boolean removeVisible = (ind >= minNumberOfPlayers);
      playerItem.updateButtonState(addVisible, removeVisible);
    }
  }
}
