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

package net.curre.jjeopardy.ui.game;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.bean.Player;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a row of player names/scores displayed below the game table.
 *
 * @see GameWindow
 * @author Yevgeny Nyden
 */
public class PlayerScoresPanel extends JPanel {

  /** Ordered list of rendered player items. */
  private final List<Item> playerItems;

  /** Ctor. */
  public PlayerScoresPanel() {
    this.playerItems = new ArrayList<>();
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    this.setBackground(lafTheme.getGameTableScorePlayerBackground());
  }

  /**
   * Prepares the game for a new round.
   */
  public void prepareGame() {
    this.removeAll();
    List<Player> players = AppRegistry.getInstance().getGameDataService().getCurrentPlayers();
    final int playersCount = players.size();
    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final int padding = lafTheme.getPanelPadding();

    // Settings the main layout.
    double[][] layoutDoubles = new double[2][];

    // Creating the column doubles, the first and last one are padding values, others are column layout constants.
    layoutDoubles[0] = new double[playersCount + 2];
    layoutDoubles[0][0] = layoutDoubles[0][playersCount + 1] = padding;
    for (int ind = 0; ind < playersCount; ind++) {
      layoutDoubles[0][ind + 1] = TableLayout.FILL;
    }

    // Creating the rows' layout.
    layoutDoubles[1] = new double[] {padding, TableLayout.PREFERRED, padding};
    this.setLayout(new TableLayout(layoutDoubles));

    this.playerItems.clear();
    int columnIndex = 1;
    for (Player player : players) {
      Item playerItem = new Item(player);
      this.playerItems.add(playerItem);
      this.add(playerItem, new TableLayoutConstraints(
        columnIndex, 1, columnIndex, 1, TableLayout.CENTER, TableLayout.CENTER));
      columnIndex++;
    }
  }

  /**
   * Updates the players' scores.
   */
  public void updateScores() {
    List<Player> players = AppRegistry.getInstance().getGameDataService().getCurrentPlayers();
    for (int index = 0; index < players.size(); index++) {
      Player player = players.get(index);
      String score = String.valueOf(player.getScore());
      this.playerItems.get(index).updateScore(score);
    }
  }

  /**
   * Represents a single player score item with player number, name, and score.
   */
  private static class Item extends JPanel {

    /** Score label. */
    private final JLabel scoreLabel;

    /** Ctor. */
    public Item(@NotNull Player player) {
      LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
      this.setBackground(lafTheme.getGameTableScorePlayerBackground());
      this.setLayout(new TableLayout(new double[][] {
        {TableLayout.PREFERRED}, // columns
        {TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED}})); // rows

      // Player number label.
      final JLabel numberLabel = new JLabel();
      numberLabel.setFont(lafTheme.getGameTableScorePlayerFont());
      numberLabel.setText(player.getNumberString());
      this.add(numberLabel, new TableLayoutConstraints(
        0, 0, 0, 0, TableLayout.CENTER, TableLayout.CENTER));

      // Player score.
      this.scoreLabel = new JLabel();
      this.scoreLabel.setFont(lafTheme.getGameTableScoreFont());
      this.scoreLabel.setText("0");
      this.add(this.scoreLabel, new TableLayoutConstraints(
        0, 2, 0, 2, TableLayout.CENTER, TableLayout.CENTER));

      // Player name.
      final JLabel nameLabel = new JLabel();
      nameLabel.setFont(lafTheme.getGameTableScorePlayerFont());
      nameLabel.setText(player.getName());
      this.add(nameLabel, new TableLayoutConstraints(
        0, 4, 0, 4, TableLayout.CENTER, TableLayout.CENTER));
    }

    /**
     * Updates the score label text.
     * @param score the new score
     */
    protected void updateScore(String score) {
      this.scoreLabel.setText(score);
    }
  }
}
