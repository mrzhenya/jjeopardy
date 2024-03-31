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

package net.curre.jjeopardy.util;

import net.curre.jjeopardy.App;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;

import java.util.ResourceBundle;

/**
 * The app default settings that are initialized from a locale
 * independent resource properties file - <code>default.properties</code>.
 *
 * @author Yevgeny Nyden
 */
public class JjDefaults {

  /** Maximum number of players allowed in a game. */
  public static final int MAX_NUMBER_OF_PLAYERS;

  /** Minimum number of players allowed in a game. */
  public static final int MIN_NUMBER_OF_PLAYERS;

  /** Maximum number of question categories. */
  public static final int MAX_NUMBER_OF_CATEGORIES;

  /** Minimum number of question categories. */
  public static final int MIN_NUMBER_OF_CATEGORIES;

  /** Maximum number of questions in a category. */
  public static final int MAX_NUMBER_OF_QUESTIONS;

  /** Minimum number of questions in a category. */
  public static final int MIN_NUMBER_OF_QUESTIONS;

  /** When question points are not specified, this will be the default multiplier for each level. */
  public static final int QUESTION_POINTS_MULTIPLIER;

  /** Maximum number of bonus questions. */
  public static final int MAX_NUMBER_OF_BONUS_QUESTIONS;

  /** Minimum number of bonus questions. */
  public static final int MIN_NUMBER_OF_BONUS_QUESTIONS;

  /** Default points a bonus question is worth. */
  public static final int BONUS_QUESTION_POINTS;

  /** Landing UI preferred width. */
  public static final int LANDING_UI_WIDTH;

  /** Landing UI library preferred height. */
  public static final int LANDING_UI_LIBRARY_HEIGHT;

  /** Min width for the edit game window. */
  public static final int EDIT_GAME_WINDOW_MIN_WIDTH;

  /** Min height for the edit game window. */
  public static final int EDIT_GAME_WINDOW_MIN_HEIGHT;

  /** Min width for the game window. */
  public static final int GAME_WINDOW_MIN_WIDTH;

  /** Min height for the game window. */
  public static final int GAME_WINDOW_MIN_HEIGHT;

  /** Minimum height for a game table row. */
  public static final int GAME_TABLE_MIN_ROW_HEIGHT;

  /** Preferred game table header height (where game categories are displayed). */
  public static final int GAME_TABLE_HEADER_HEIGHT;

  /** Add/remove players dialog width. */
  public static final int PLAYER_DIALOG_WIDTH;

  /** Add/remove players dialog height. */
  public static final int PLAYER_DIALOG_HEIGHT;

  /** Start time for the question timer (in seconds). */
  public static final int QUESTION_TIME;

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(JjDefaults.class.getName());

  /** Name of the default properties bundle. */
  private static final String BUNDLE_NAME = "default";

  static {
    MAX_NUMBER_OF_PLAYERS = getDefaultIntProperty("jj.defaults.players.max");
    MIN_NUMBER_OF_PLAYERS = getDefaultIntProperty("jj.defaults.players.min");
    MAX_NUMBER_OF_CATEGORIES = getDefaultIntProperty("jj.defaults.categories.max");
    MIN_NUMBER_OF_CATEGORIES = getDefaultIntProperty("jj.defaults.categories.min");
    MAX_NUMBER_OF_QUESTIONS = getDefaultIntProperty("jj.defaults.questions.max");
    MIN_NUMBER_OF_QUESTIONS = getDefaultIntProperty("jj.defaults.questions.min");
    QUESTION_POINTS_MULTIPLIER = getDefaultIntProperty("jj.defaults.question.multiplier");
    MAX_NUMBER_OF_BONUS_QUESTIONS = getDefaultIntProperty("jj.defaults.bonus.questions.max");
    MIN_NUMBER_OF_BONUS_QUESTIONS = getDefaultIntProperty("jj.defaults.bonus.questions.min");
    BONUS_QUESTION_POINTS = getDefaultIntProperty("jj.defaults.bonus.question.points");
    LANDING_UI_WIDTH = getDefaultIntProperty("jj.defaults.landing.ui.width");
    LANDING_UI_LIBRARY_HEIGHT = getDefaultIntProperty("jj.defaults.landing.ui.library.height");
    EDIT_GAME_WINDOW_MIN_WIDTH = getDefaultIntProperty("jj.defaults.edit.game.window.min.width");
    EDIT_GAME_WINDOW_MIN_HEIGHT = getDefaultIntProperty("jj.defaults.edit.game.window.min.height");
    GAME_WINDOW_MIN_WIDTH = getDefaultIntProperty("jj.defaults.game.window.min.width");
    GAME_WINDOW_MIN_HEIGHT = getDefaultIntProperty("jj.defaults.game.window.min.height");
    GAME_TABLE_MIN_ROW_HEIGHT = getDefaultIntProperty("jj.defaults.min.row.height");
    GAME_TABLE_HEADER_HEIGHT = getDefaultIntProperty("jj.defaults.preferred.header.height");
    PLAYER_DIALOG_WIDTH = getDefaultIntProperty("jj.defaults.player.dialog.width");
    PLAYER_DIALOG_HEIGHT = getDefaultIntProperty("jj.defaults.player.dialog.height");
    QUESTION_TIME = getDefaultIntProperty("jj.defaults.question.time");
  }

  /**
   * Gets a default int property value for the given message name.
   * @param messageName message name
   * @return default int property value
   */
  private static int getDefaultIntProperty(String messageName) {
    try {
      ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME);
      return Integer.parseInt(bundle.getString(messageName).trim());
    } catch (Exception e) {
      logger.log(Level.FATAL,"Unable to load default property \"" + messageName + "\"", e);
      throw new RuntimeException("Unable to load default properties");
    }
  }
}
