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

package net.curre.jjeopardy.service;

import net.curre.jjeopardy.bean.Category;
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.bean.Player;
import net.curre.jjeopardy.bean.Question;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.curre.jjeopardy.service.FileParsingResult.Message.*;

/**
 * Service to assist with player handling, keeping game scores,
 * handling questions.
 *
 * @author Yevgeny Nyden
 */
public class GameDataService {

  /** Maximum number of players allowed in a game. */
  public static final int MAX_NUMBER_OF_PLAYERS = 6;

  /** Minimum number of players allowed in a game. */
  public static final int MIN_NUMBER_OF_PLAYERS = 2;

  /** Maximum number of categories. */
  private static final int MAX_NUMBER_OF_CATEGORIES = 7;

  /** Minimum number of categories. */
  private static final int MIN_NUMBER_OF_CATEGORIES = 3;

  /** Maximum number of questions in a category. */
  private static final int MAX_NUMBER_OF_QUESTIONS = 7;

  /** Minimum number of questions in a category. */
  private static final int MIN_NUMBER_OF_QUESTIONS = 3;

  /** Maximum number of bonus questions. */
  private static final int MAX_NUMBER_OF_BONUS_QUESTIONS = 10;

  /** Default points a bonus question is worth. */
  private static final int DEFAULT_BONUS_QUESTION_POINTS = 300;

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(GameDataService.class.getName());

  /** Game file data (game name, questions, categories, and optional players). */
  private final GameData gameFileData;

  /** Current game players and their scores. */
  private final List<Player> players;

  /**
   * Ctor.
   */
  protected GameDataService() {
    this.gameFileData = new GameData();
    this.players = new ArrayList<>();
  }

  /**
   * Gets the current game data loaded from a game file.
   * @return game file data
   */
  public GameData getGameFileData() {
    return this.gameFileData;
  }

  /**
   * Gets question from the games data.
   * @param catIndex category index for the question to fetch
   * @param questIndex question index for the questino to fetch
   * @return the question data object
   */
  public Question getQuestion(int catIndex, int questIndex) {
    return this.gameFileData.getCategories().get(catIndex).getQuestion(questIndex);
  }

  /**
   * Adjusts teams score.
   * @param teamIndex index of the team.
   * @param value value to use when asjusting the score.
   */
  public void adjustTeamScore(int teamIndex, int value) {
    final Player player = this.players.get(teamIndex);
    player.adjustScore(value);
  }

  /**
   * Resets all players scores.
   */
  public void resetPlayerScores() {
    for (Player player : this.players) {
      player.resetScore();
    }
  }

  /**
   * Gets the winner of the game.
   * @return player with the most score
   */
  public Player getWinner() {
    Player winner = this.players.get(0);
    for (Player player : this.players) {
      if (player.getScore() > winner.getScore()) {
        winner = player;
      }
    }
    return winner;
  }

  /**
   * Gets the players.
   * @return player objects (with scores)
   */
  public List<Player> getPlayers() {
    return this.players;
  }

  /**
   * Determines if the game is ready to start.
   * @return true if we have enough data to start a game; false if otherwise
   */
  public boolean isGameReady() {
    if (this.players.size() < MIN_NUMBER_OF_PLAYERS) {
      return false;
    }
    return this.gameFileData.haveEnoughGameData();
  }

  /**
   * Loads game data from a given file.
   * @param fileName absolute path to the file
   * @return parsed game data file results
   */
  public FileParsingResult loadGameData(String fileName) {
    final FileParsingResult result = new FileParsingResult(fileName);

    // Loading the game data from an XML file.
    Properties props = new Properties();
    try {
      InputStream in = Files.newInputStream(Paths.get(fileName));
      props.loadFromXML(in);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unable to open or parse XML file: " + fileName, e);
      result.addErrorMessage(MSG_PARSING);
      return result;
    }

    // ******* First, parse required games data - name and questions/categories.
    final String gameName;
    try {
      gameName = getProperty(props, "game.name");
      if (StringUtils.isBlank(gameName)) {
        LOGGER.severe("Game name is blank.");
        result.addErrorMessage(MSG_BLANK_NAME);
        return result;
      }
    } catch (ServiceException e) {
      LOGGER.severe("Unable to parse game name.");
      result.addErrorMessage(MSG_NAME_NOT_FOUND);
      return result;
    }

    final List<Category> categories = this.parseCategories(props, result);
    if (categories == null || categories.isEmpty()) {
      return result;
    }

    this.gameFileData.updateGameData(gameName, categories);
    result.setGameDataUsable();

    // ****** Now, parse optional data.
    this.parsePlayersDataIfAny(props, result);
    this.parseBonusQuestionsIfAny(props, result);

    return result;
  }

  /**
   * Updates current game players using data from the settings UI.
   * @param playerNames player names from the player dialog
   */
  public void updatePlayersFromNames(List<String> playerNames) {
    this.players.clear();
    for (int ind = 0; ind < playerNames.size(); ind++) {
      this.players.add(new Player(playerNames.get(ind), ind));
    }
  }

  /**
   * Parses the core of the games data - questions organized into categories.
   * @param props  properties file
   * @param result object to add parsing messages to
   * @return parsed categories or null if unable to parse
   */
  private List<Category> parseCategories(Properties props, FileParsingResult result) {
    List<Category> categories = new ArrayList<>();
    int totalQuestionsCount = 0;
    try {
      // getting the questions/answers data
      for (int index = 1; index <= MAX_NUMBER_OF_CATEGORIES + 1; ++index) {
        final String catName = getProperty(props, "category." + index + ".name");
        List<Question> questions = new ArrayList<>();
        try {
          for (int k = 1; k <= MAX_NUMBER_OF_QUESTIONS + 1; ++k) {
            int points;
            try {
              points = getIntProperty(props, "question." + k + ".points");
            } catch (ServiceException e) {
              points = k * 50;
            }
            final String question = getProperty(props, "category." + index + ".question." + k);
            final String answer = getProperty(props, "category." + index + ".answer." + k);
            Question q = new Question(question, answer, points);
            questions.add(q);
            totalQuestionsCount++;
          }
        } catch (Exception e) {
          // Stop on first error - no further questions for this category is defined.
        }

        Category c = new Category(catName, index, questions);
        categories.add(c);
      }
    } catch (ServiceException e) {
      // Stop at first category parsing error.
    }
    if (categories.isEmpty()) {
      result.addErrorMessage(MSG_NO_CATEGORIES);
      LOGGER.severe("No questions/categories are parsed.");
      return null;
    }
    if (categories.size() < MIN_NUMBER_OF_CATEGORIES) {
      result.addErrorMessage(MSG_NOT_ENOUGH_CATEGORIES, String.valueOf(MIN_NUMBER_OF_CATEGORIES));
      LOGGER.severe("Not enough categories are parsed - " + categories.size());
      return null;
    }
    if (categories.size() > MAX_NUMBER_OF_CATEGORIES) {
      result.addErrorMessage(MSG_TOO_MANY_CATEGORIES, String.valueOf(MAX_NUMBER_OF_CATEGORIES));
      LOGGER.severe("Too many categories are parsed - " + categories.size());
      return null;
    }
    final int numberOfQuestions = categories.get(0).getQuestionsCount();
    for (Category category : categories) {
      if (category.getQuestionsCount() == 0) {
        result.addErrorMessage(MSG_NO_QUESTIONS);
        LOGGER.severe("No questions are parse in a category");
        return null;
      } else if (category.getQuestionsCount() < MIN_NUMBER_OF_QUESTIONS) {
        result.addErrorMessage(MSG_NOT_ENOUGH_QUESTIONS, String.valueOf(MIN_NUMBER_OF_QUESTIONS));
        LOGGER.severe("Not enough questions in a category - " + category.getQuestionsCount());
        return null;
      } else if (category.getQuestionsCount() > MAX_NUMBER_OF_QUESTIONS) {
        result.addErrorMessage(MSG_TOO_MANY_QUESTIONS, String.valueOf(MAX_NUMBER_OF_QUESTIONS));
        LOGGER.severe("Too many questions are parse in a category - " + category.getQuestionsCount());
        return null;
      }
      if (numberOfQuestions != category.getQuestionsCount()) {
        result.addErrorMessage(MSG_NOT_MATCHING_QUESTIONS);
        LOGGER.severe("Uneven number of questions in categories - " + category.getQuestionsCount());
        return null;
      }
    }
    result.addInfoMessage(MSG_QUESTIONS_PARSED, String.valueOf(totalQuestionsCount), String.valueOf(categories.size()));
    return categories;
  }

  /**
   * Updates the player names in the GameFileData.
   * Note, that it doesn't update current game players - it's assumed
   * to be updated from the code that initiated parsing the file.
   * @param props  properties file
   * @param result object to add parsing messages to
   */
  private void parsePlayersDataIfAny(Properties props, FileParsingResult result) {
    List<String> playerNames = new ArrayList<>();
    // Notice that the (user facing) index starts at 1.
    for (int ind = 1; ind <= MAX_NUMBER_OF_PLAYERS; ind++) {
      try {
        String playerName = getProperty(props, "player." + ind + ".name");
        if (StringUtils.isBlank(playerName)) {
          result.addWarningMessage(MSG_BLANK_PLAYER_NAME, String.valueOf(ind));
        } else {
          playerNames.add(playerName.trim());
        }
      } catch (ServiceException e) {
        // Stop on the first error, since there are no more players (or at all).
        break;
      }
    }
    int playersCount = playerNames.size();
    if (playersCount > 0 && playersCount < MIN_NUMBER_OF_PLAYERS) {
      LOGGER.warning("Ignoring players since the number is less then " + MSG_TOO_FEW_PLAYERS);
      result.addWarningMessage(MSG_TOO_FEW_PLAYERS, String.valueOf(MIN_NUMBER_OF_PLAYERS));
      return;
    }
    result.addInfoMessage(MSG_PLAYERS_PARSED, String.valueOf(playersCount));
    this.gameFileData.updatePlayersNames(playerNames);
  }

  /**
   * Parses bonus questions if found.
   * @param props  properties file
   * @param result object to add parsing messages to
   */
  private void parseBonusQuestionsIfAny(Properties props, FileParsingResult result) {
    int bonusPoints;
    try {
      bonusPoints = getIntProperty(props, "bonus.question.points");
    } catch (ServiceException e) {
      bonusPoints = DEFAULT_BONUS_QUESTION_POINTS;
    }
    // Notice that the (user facing) index starts at 1.
    final List<Question> questions = new ArrayList<>();
    for (int ind = 1; ind <= MAX_NUMBER_OF_BONUS_QUESTIONS; ind++) {
      try {
        String bQuestion = getProperty(props, "bonus." + ind + ".question");
        String bAnswer = getProperty(props, "bonus." + ind + ".answer");
        questions.add(new Question(bQuestion, bAnswer, bonusPoints));
      } catch (ServiceException e) {
        // Stop on the first error, since there are no more questions (or at all).
        break;
      }
    }
    int playersCount = gameFileData.getPlayerNames().size();
    int bonusQuestionsCount = questions.size();
    if (bonusQuestionsCount > 0) {
      if (playersCount > 0 && bonusQuestionsCount < playersCount) {
        result.addWarningMessage(MSG_TOO_FEW_BONUS_QUESTIONS, String.valueOf(playersCount));
      } else {
        this.gameFileData.updateBonusQuestions(questions);
      }
    }
    result.addInfoMessage(MSG_BONUS_QUESTIONS_PARSED, String.valueOf(bonusQuestionsCount));
  }

  /**
   * Fetches a property from the given property object
   * and returns its String value.
   * @param props    property object to use.
   * @param propName property name.
   * @return property string value.
   * @throws ServiceException if the property is not present or it is empty.
   */
  private static String getProperty(Properties props, String propName) throws ServiceException {
    final String propStr = props.getProperty(propName);
    if (StringUtils.isBlank(propStr)) {
      throw new ServiceException("XML error: \"" + propName + "\" is empty!");
    }
    return propStr;
  }

  /**
   * Fetches a property from the given property object
   * and returns its int value.
   * @param props    property object to use.
   * @param propName property name.
   * @return property int value.
   * @throws ServiceException if the property is not present, is empty
   *                          or does not represent an integer.
   */
  private static int getIntProperty(Properties props, String propName) throws ServiceException {
    final String propStr = props.getProperty(propName);
    if (StringUtils.isBlank(propStr)) {
      throw new ServiceException("XML error: \"" + propName + "\" is empty!");
    }
    try {
      return Integer.parseInt(propStr);
    } catch (NumberFormatException e) {
      throw new ServiceException("XML error: \"" + propName + "\" is not an integer!");
    }
  }
}
