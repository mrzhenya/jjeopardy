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
import net.curre.jjeopardy.bean.Player;
import net.curre.jjeopardy.util.JjDefaults;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;

/**
 * UI container for the player data - rows of items, each of which has
 * a label, a text input field, and buttons to Remove/Add players.
 * @author Yevgeny Nyden
 */
public class PlayersPane extends JPanel {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(App.class.getName());

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
    if (nextPlayerIndex == JjDefaults.MAX_NUMBER_OF_PLAYERS) {
      logger.warn("Trying to add too many players, ignoring...");
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
    for (int ind = 0; ind < this.containerPane.getComponentCount(); ind++) {
      PlayerItem playerItem = (PlayerItem) this.containerPane.getComponent(ind);
      if (StringUtils.isBlank(playerItem.getPlayerName()) &&
          ind >= JjDefaults.MIN_NUMBER_OF_PLAYERS) {
        this.removePlayerItem(ind--);
      }
    }
  }

  /**
   * Updates the players pane.
   * @param players list of players
   */
  protected void updatePlayersPane(List<Player> players) {
    if (players.size() < JjDefaults.MIN_NUMBER_OF_PLAYERS) {
      logger.warn("Provided too few players, ignoring.");
      if (this.containerPane.getComponentCount() == 0) {
        this.initDefaultPlayersPane();
      }
      return;
    }
    // Updating player text labels from the passed array.
    final int currPlayersNum = this.containerPane.getComponentCount();
    for (int ind = 0; ind < players.size(); ind++) {
      Player player = players.get(ind);
      if (ind < currPlayersNum) {
        ((PlayerItem) this.containerPane.getComponent(ind)).updatePlayer(player.getName());
      } else {
        this.addNewPlayerItem(player.getName());
      }
    }
    // If there are more players in the UI, removing them.
    if (players.size() < currPlayersNum) {
      for (int ind = players.size(); ind < currPlayersNum; ind++) {
        this.removePlayerItem(players.size());
      }
    }
    this.updateButtonState();
  }

  /**
   * Initializes the players pane with default empty player lines.
   */
  private void initDefaultPlayersPane() {
    for (int ind = 0; ind < JjDefaults.MIN_NUMBER_OF_PLAYERS; ind++) {
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
    for (int ind = 0; ind < numberOfPlayers; ind++) {
      PlayerItem playerItem = (PlayerItem) this.containerPane.getComponent(ind);
      // Add button is visible only on the last row.
      boolean addVisible = (ind + 1 == numberOfPlayers) &&
        (ind + 1 < JjDefaults.MAX_NUMBER_OF_PLAYERS);
      // Remove buttons are visible only on the non-default rows.
      boolean removeVisible = (ind >= JjDefaults.MIN_NUMBER_OF_PLAYERS);
      playerItem.updateButtonState(addVisible, removeVisible);
    }
  }
}
