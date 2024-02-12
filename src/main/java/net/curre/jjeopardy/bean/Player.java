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

package net.curre.jjeopardy.bean;

import net.curre.jjeopardy.service.LocaleService;

/**
 * Represents a player in the game. A player has a name, game score,
 * and a relative index (players relative position on the list, starts at 0).
 * Use GameData service to obtain the list of players for the current game.
 *
 * @see net.curre.jjeopardy.service.GameDataService
 * @author Yevgeny Nyden
 */
public class Player implements HasName {

  /** Player's name. */
  private final String name;

  /** Player's relative position on the list (starts at 0). */
  private final int index;

  /** Player's current score. */
  private int score;

  /**
   * Ctor.
   * @param name player's name
   * @param index player's relative position on the list (starts at 0)
   */
  public Player(String name, int index) {
    this.name = name;
    this.index = index;
    this.score = 0;
  }

  /**
   * Gets the player's name.
   * @return player's name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Gets the player's relative position on the list (starts at 0).
   * @return player's index
   */
  public int getIndex() {
    return this.index;
  }

  /**
   * Gets a player's number string.
   * @return player number string (something like "Player #1").
   */
  public String getPlayerNumberString() {
    return LocaleService.getString("jj.player.name", String.valueOf((index + 1)));
  }

  /**
   * Gets the player's current score.
   * @return player's score
   */
  public int getScore() {
    return this.score;
  }

  /**
   * Adjusts the player's current score.
   * @param value value to add to the current score
   */
  public void adjustScore(int value) {
    this.score = this.score + value; 
  }

  /**
   * Resets this player's score.
   */
  public void resetScore() {
    this.score = 0;
  }

  /**
   * Gets a full title string for this player.
   * @return name string (includes the word "Player")
   */
  public String getNameString() {
    return LocaleService.getString("jj.player.name", this.name);
  }
}
