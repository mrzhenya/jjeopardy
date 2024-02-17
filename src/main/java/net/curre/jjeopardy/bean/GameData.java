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

import net.curre.jjeopardy.util.JjDefaults;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static net.curre.jjeopardy.bean.FileParsingResult.Message.*;

/**
 * Object of this class represents data parsed from a games file.
 * All game data validation code is also kept in here. To determine if there
 * is enough data for a game (game name and enough categories and questions),
 * call <code>#isGameDataUsable()</code>.
 *
 * @author Yevgeny Nyden
 */
public class GameData {

  /** Game data file absolute path. */
  private final String fileName;

  /**
   * Indicates that game file was opened and parsed successfully.
   * False indicates that no data in this object besides the file name is set.
   */
  private boolean isFileDataAcquired;

  /** Game name parsed from the file. */
  private String gameName;

  /** Game description. */
  private String gameDescription;

  /** Game questions/categories parsed from the file. */
  private final List<Category> categories;

  /**
   * Optional player names parsed from the file. Note, that these are not
   * necessarily the players used in the game which are stored in GamesDataService.
   */
  private final List<String> playerNames;

  /** Optional bonus questions. */
  private final List<Question> bonusQuestions;

  /**
   * Ctor.
   * @param fileName file name
   */
  public GameData(String fileName) {
    this.fileName = fileName;
    this.isFileDataAcquired = false;
    this.categories = new ArrayList<>();
    this.playerNames = new ArrayList<>();
    this.bonusQuestions = new ArrayList<>();
  }

  /**
   * Gets the game file name.
   * @return game file name (absolute path)
   */
  public String getFileName() {
    return this.fileName;
  }

  /**
   * Gets the game name.
   * @return current game name
   */
  public String getGameName() {
    return this.gameName;
  }

  /**
   * Sets the game name.
   * @param gameName game name to set
   */
  public void setGameName(String gameName) {
    this.gameName = gameName;
  }

  /**
   * Gets game description.
   * @return game description
   */
  public String getGameDescription() {
    return this.gameDescription;
  }

  /**
   * Sets game description.
   * @param gameDescription game description
   */
  public void setGameDescription(String gameDescription) {
    this.gameDescription = gameDescription;
  }

  /**
   * Gets the game categories/questions.
   * @return current game categories/questions
   */
  public List<Category> getCategories() {
    return this.categories;
  }

  /**
   * Sets the game categories.
   * @param categories question categories data
   */
  public void setCategories(List<Category> categories) {
    this.categories.clear();
    this.categories.addAll(categories);
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
  public void setPlayersNames(List<String> playerNames) {
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
   * Determines if bonus questions have been asked (if the first bonus question
   * has been asked, we assume all questions have been asked).
   * @return true if bonus questions have been asked
   */
  public boolean bonusQuestionsHaveBeenAsked() {
    return !this.bonusQuestions.isEmpty() && !this.bonusQuestions.get(0).isHasBeenAsked();
  }

  /**
   * Updates optional bonus questions.
   * @param bonusQuestions bonus questions
   */
  public void setBonusQuestions(List<Question> bonusQuestions) {
    this.bonusQuestions.clear();
    this.bonusQuestions.addAll(bonusQuestions);
  }

  /**
   * Determines if the game data is usable, which means
   * there is a game name and enough categories/questions to play.
   * Bonus questions or players are optional.
   * @return true if the parsed data is usable
   */
  public boolean isGameDataUsable() {
    return this.isGameNameValid() && this.isCategoriesAndQuestionsDataValid(null);
  }

  /**
   * Sets the file data acquired flag on this game data.
   */
  public void setFileDataAcquired() {
    this.isFileDataAcquired = true;
  }

  /**
   * Resets the game data for a new game. Here we reset the
   * "hasBeenAsked" state of all questions (and bonus questions).
   */
  public void resetGameData() {
    for (Category category : this.categories) {
      for (int i = 0; i < category.getQuestionsCount(); i++) {
        category.getQuestion(i).resetHasBeenAsked();
      }
    }
    for (Question bonusQuestion : this.bonusQuestions) {
      bonusQuestion.resetHasBeenAsked();
    }
  }

  /**
   * Generates file parsing results for this game data.
   * @return game data parsing result
   */
  public FileParsingResult generateFileParsingResult() {
    final FileParsingResult result = new FileParsingResult(this.fileName);
    result.setGameData(this);

    // First, check if the game file was located and parsed successfully.
    if (!this.isFileDataAcquired) {
      result.addErrorMessage(MSG_PARSING);
      return result;
    }

    // Game name is required.
    boolean nameValid = isGameNameValid();
    if (!nameValid) {
      result.addErrorMessage(MSG_MISSING_NAME);
    }

    // Question categories/questions are required as well.
    boolean questionsUsable = isCategoriesAndQuestionsDataValid(result);
    if (nameValid && questionsUsable) {
      result.setGameDataUsable();
    }

    // Generating the info messages for each data part if its parsing was successful.
    if (questionsUsable) {
      result.addInfoMessage(MSG_QUESTIONS_PARSED,
          String.valueOf(this.categories.get(0).getQuestionsCount()), String.valueOf(this.categories.size()));
    }
    if (isBonusQuestionsDataValid(result)) {
      result.addInfoMessage(MSG_BONUS_QUESTIONS_PARSED, String.valueOf(this.bonusQuestions.size()));
    }
    if (isPlayersValid(result)) {
      result.addInfoMessage(MSG_PLAYERS_PARSED, String.valueOf(this.playerNames.size()));
    }
    return result;
  }

  /**
   * Determines if the current players represent a group enough for a game.
   * @return true if there are enough players for a game; false if otherwise
   */
  public boolean hasEnoughPlayers() {
    return this.playerNames.size() >= JjDefaults.MIN_NUMBER_OF_PLAYERS;
  }

  /**
   * Determines if the players data is valid, which is the number of players is either zero or min number.
   * @param resultOrNull result object to add warning messages to; or null if not messages are needed
   * @return true if player data is valid
   */
  public boolean isPlayersValid(FileParsingResult resultOrNull) {
    if (this.playerNames.isEmpty()) {
      return true;
    }
    if (this.playerNames.size() < JjDefaults.MIN_NUMBER_OF_PLAYERS) {
      maybeAddWarning(resultOrNull, MSG_TOO_FEW_PLAYERS, String.valueOf(JjDefaults.MIN_NUMBER_OF_PLAYERS));
      return false;
    } else if (this.playerNames.size() > JjDefaults.MAX_NUMBER_OF_PLAYERS) {
      maybeAddWarning(resultOrNull, MSG_TOO_MANY_PLAYERS);
    }
    return true;
  }

  /**
   * Determines if the game name valid.
   * @return true if game name is valid
   */
  private boolean isGameNameValid() {
    return !StringUtils.isBlank(this.gameName);
  }

  /**
   * Validates game categories and their questions.
   * @param resultOrNull result object to add error and warning messages to; or null if not messages are needed
   * @return true if the categories and questions data is valid
   */
  private boolean isCategoriesAndQuestionsDataValid(FileParsingResult resultOrNull) {
    boolean isValid = true;
    if (this.categories.isEmpty()) {
      maybeAddError(resultOrNull, MSG_NO_CATEGORIES);
      isValid = false;
    } else {
      if (this.categories.size() < JjDefaults.MIN_NUMBER_OF_CATEGORIES) {
        maybeAddError(resultOrNull, MSG_NOT_ENOUGH_CATEGORIES, String.valueOf(JjDefaults.MIN_NUMBER_OF_CATEGORIES));
        isValid = false;
      } else if (this.categories.size() >= JjDefaults.MAX_NUMBER_OF_CATEGORIES) {
        // Too many categories is just a warning, extra categories will be ignored.
        maybeAddWarning(resultOrNull, MSG_TOO_MANY_CATEGORIES, String.valueOf(JjDefaults.MAX_NUMBER_OF_CATEGORIES));
      }
      boolean categoryNameBlankError = false;
      boolean noQuestionsError = false;
      boolean tooFewQuestionsError = false;
      boolean tooManyQuestionsError = false;
      for (Category category : this.categories) {
        if (StringUtils.isBlank(category.getName())) {
          categoryNameBlankError = true;
        }
        if (category.getQuestionsCount() == 0) {
          noQuestionsError = true;
        } else if (category.getQuestionsCount() < JjDefaults.MIN_NUMBER_OF_QUESTIONS) {
          tooFewQuestionsError = true;
        } else if (category.getQuestionsCount() > JjDefaults.MAX_NUMBER_OF_QUESTIONS) {
          tooManyQuestionsError = true;
        }
      }
      if (categoryNameBlankError) {
        maybeAddError(resultOrNull, MSG_BLANK_CATEGORY_NAME);
        isValid = false;
      }
      if (noQuestionsError) {
        maybeAddError(resultOrNull, MSG_NO_QUESTIONS);
        isValid = false;
      }
      if (tooFewQuestionsError) {
        maybeAddError(resultOrNull, MSG_NOT_ENOUGH_QUESTIONS, String.valueOf(JjDefaults.MIN_NUMBER_OF_QUESTIONS));
        isValid = false;
      }
      if (tooManyQuestionsError) {
        // Too many questions is just a warning, extra questions will be ignored.
        maybeAddWarning(resultOrNull, MSG_TOO_MANY_QUESTIONS, String.valueOf(JjDefaults.MAX_NUMBER_OF_QUESTIONS));
      }

      if (categories.size() > 1) {
        int numOfQuestions = categories.get(0).getQuestionsCount();
        for (int i = 1; i < categories.size(); i++) {
          if (numOfQuestions != categories.get(i).getQuestionsCount()) {
            maybeAddError(resultOrNull, MSG_NOT_MATCHING_QUESTIONS);
            isValid = false;
            break;
          }
        }
      }
    }
    return isValid;
  }

  /**
   * Determines bonus questions are valid - either no questions or at least min number of questions.
   * @param resultOrNull result object to add warning messages to; or null if not messages are needed
   * @return true bonus questions are valid
   */
  private boolean isBonusQuestionsDataValid(FileParsingResult resultOrNull) {
    if (this.bonusQuestions.isEmpty()) {
      return true;
    }
    if (this.bonusQuestions.size() < JjDefaults.MIN_NUMBER_OF_BONUS_QUESTIONS) {
      maybeAddWarning(resultOrNull, MSG_TOO_FEW_BONUS_QUESTIONS);
      return false;
    }
    if (this.bonusQuestions.size() > JjDefaults.MAX_NUMBER_OF_BONUS_QUESTIONS) {
      maybeAddWarning(resultOrNull, MSG_TOO_MANY_BONUS_QUESTIONS);
    }
    return true;
  }

  /**
   * Adds a warning message to the passed result object if it's not null.
   * @param resultOrNull result or null
   * @param msgNoCategories message to add
   * @param args arguments for the message
   */
  private static void maybeAddWarning(
      FileParsingResult resultOrNull, FileParsingResult.Message msgNoCategories, String... args) {
    if (resultOrNull != null) {
      resultOrNull.addWarningMessage(msgNoCategories, args);
    }
  }

  /**
   * Adds an error message to the passed result object if it's not null.
   * @param resultOrNull result or null
   * @param msgNoCategories message to add
   * @param args arguments for the message
   */
  private static void maybeAddError(
      FileParsingResult resultOrNull, FileParsingResult.Message msgNoCategories, String... args) {
    if (resultOrNull != null) {
      resultOrNull.addErrorMessage(msgNoCategories, args);
    }
  }
}
