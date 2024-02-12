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

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Object of this class represents data parsed from a games file.
 *
 * @author Yevgeny Nyden
 */
public class GameData {

  /** Game name parsed from the file. */
  private String gameName;

  /** Game questions/categories parsed from the file. */
  private final List<Category> categories;

  /**
   * Optional player names parsed from the file. Note, that these are not
   * necessarily the players used in the game which are stored in GamesDataService.
   */
  private final List<String> playerNames;

  /** Optional bonus questions. */
  private final List<Question> bonusQuestions;

  /** Ctor. */
  public GameData() {
    this.categories = new ArrayList<>();
    this.playerNames = new ArrayList<>();
    this.bonusQuestions = new ArrayList<>();
  }

  /**
   * Resets the game data for a new game.
   */
  public void resetGameData() {
    for (Category category : this.categories) {
      for (int i = 0; i < category.getQuestionsCount(); i++) {
        category.getQuestion(i).resetAnswered();
      }
    }
    for (Question bonusQuestion : this.bonusQuestions) {
      bonusQuestion.resetAnswered();
    }
  }

  /**
   * Gets the game name.
   * @return current game name
   */
  public String getGameName() {
    return this.gameName;
  }

  /**
   * Gets the game categories/questions.
   * @return current game categories/questions
   */
  public List<Category> getCategories() {
    return this.categories;
  }

  /**
   * Gets the player names parsed from a games file.
   * Note, that "real" players that are used in the game are stored in GamesDataService.
   * @return player names
   */
  public List<String> getPlayerNames() {
    return this.playerNames;
  }

  /**
   * Updates player names as parsed from a games file.
   * @param playerNames player names
   */
  public void updatePlayersNames(List<String> playerNames) {
    this.playerNames.clear();
    this.playerNames.addAll(playerNames);
  }

  /**
   * Gets optional bonus questions.
   * @return bonus questions
   */
  public List<Question> getBonusQuestions() {
    return this.bonusQuestions;
  }

  /**
   * Determines if there are unanswered bonus questions.
   * @return true if there are unanswered bonus questions, false if otherwise
   */
  public boolean hasUnansweredBonusQuestions() {
    // Assume, if the first question is answered - all are answered.
    return this.bonusQuestions.size() > 0 && !this.bonusQuestions.get(0).isHasBeenAsked();
  }

  /**
   * Updates optional bonus questions.
   * @param bonusQuestions bonus questions
   */
  public void updateBonusQuestions(List<Question> bonusQuestions) {
    this.bonusQuestions.clear();
    this.bonusQuestions.addAll(bonusQuestions);
  }

  /**
   * Updates name and categories.
   * @param name game name
   * @param categories question categories data
   */
  public void updateGameData(String name, List<Category> categories) {
    this.gameName = name;
    this.categories.clear();
    this.categories.addAll(categories);
  }

  /**
   * Determines if there is enough game data for the game, which means
   * the game name is non-blank and there is enough categories/questions.
   * @return true if we have enough game data (ignoring the players)
   */
  public boolean haveEnoughGameData() {
    if (StringUtils.isBlank(this.gameName)) {
      return false;
    }
    if (this.categories.isEmpty()) {
      return false;
    }
    return true;
  }
}
