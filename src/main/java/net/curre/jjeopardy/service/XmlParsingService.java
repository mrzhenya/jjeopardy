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
import net.curre.jjeopardy.bean.Question;
import net.curre.jjeopardy.util.JjDefaults;
import net.curre.jjeopardy.util.Utilities;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.curre.jjeopardy.util.XmlFileUtilities.*;

/**
 * Service to assist with player handling, keeping game scores,
 * handling questions.<br><br>
 * An instance of this service object should be obtained from the AppRegistry.
 *
 * @author Yevgeny Nyden
 */
public class XmlParsingService {

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(XmlParsingService.class.getName());

  /**
   * Max value for the parse loops, it's not really used since every loop stops with/by an exception,
   * but we need a large enough number for the parse for loop condition.
   */
  private static final int MAX_LOOP = 30;

  /**
   * Ctor.
   */
  protected XmlParsingService() {
  }

  /**
   * Loads game data from a given XML file. Note, that the parsed data is not validated and returned as is.
   * @param fileName absolute path to the file
   * @param bundleOrNull absolute path to the game bundle if the file is in a bundle; or null if it's a standalone file
   * @return parsed game data
   */
  protected GameData parseXmlGameData(String fileName, String bundleOrNull) {
    GameData gameData = new GameData(fileName, bundleOrNull, true);

    // Loading the game data from an XML file.
    Properties props = new Properties();
    try {
      InputStream in = Files.newInputStream(Paths.get(fileName));
      props.loadFromXML(in);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unable to open or parse XML file: " + fileName, e);
      return gameData;
    }
    gameData.setFileDataAcquired();

    // ******* First, required game name.
    try {
      String gameName = Utilities.getProperty(props, PROPERTY_NAME);
      if (StringUtils.isBlank(gameName)) {
        LOGGER.severe("Game name is blank.");
      } else {
        gameData.setGameName(gameName.trim());
      }
    } catch (ServiceException e) {
      LOGGER.severe("Unable to parse game name.");
    }

    // ******* Optional game description.
    final String gameDescription;
    try {
      gameDescription = Utilities.getProperty(props, PROPERTY_DESCRIPTION);
      if (!StringUtils.isBlank(gameDescription)) {
        gameData.setGameDescription(gameDescription.trim());
      }
    } catch (ServiceException e) {
      // Ignore the error since description is optional.
    }

    // Now, parse categories and questions.
    gameData.setCategories(this.parseCategories(props));

    // ****** Now, parse optional data.
    gameData.setPlayersNames(this.parsePlayersDataIfAny(props));
    gameData.setBonusQuestions(this.parseBonusQuestionsIfAny(props));

    String imageFailure = Utilities.getPropertyOrNull(props, PROPERTY_IMAGE_FAILURE);
    if (imageFailure != null && imageFailure.equals(String.valueOf(true))) {
      gameData.setImageDownloadFailure();
    }

    return gameData;
  }

  /**
   * Parses the core of the games data - questions organized into categories.
   * The parsed data has trimmed stings but returned not validated.
   * @param props properties file to parse data from
   * @return parsed categories (or empty list if none is parsed)
   */
  private List<Category> parseCategories(Properties props) {
    List<Category> categories = new ArrayList<>();
    try {
      // Notice that the (user facing) index starts at 1.
      for (int categoryNumber = 1; categoryNumber < MAX_LOOP; ++categoryNumber) {
        String categoryName = Utilities.getProperty(props, getPropertyCategoryName(categoryNumber));
        if (StringUtils.isBlank(categoryName)) {
          categoryName = "";
          LOGGER.severe("Category " + categoryNumber + " name is blank.");
        }
        List<Question> questions = parseQuestions(props, categoryNumber);
        categories.add(new Category(categoryName.trim(), questions));
      }
    } catch (ServiceException e) {
      // Stop at first category parsing error.
    }
    return categories;
  }

  /**
   * Parses questions for a specified category.
   * The parsed data has trimmed stings but returned not validated.
   * @param props properties to parse the data from
   * @param categoryNumber category number to parse the questions for
   * @return list of parsed questions (or none if parsed)
   */
  private List<Question> parseQuestions(Properties props, int categoryNumber) {
    List<Question> questions = new ArrayList<>();
    try {
      for (int questionNumber = 1; questionNumber < MAX_LOOP; questionNumber++) {
        int points;
        try {
          points = Utilities.getIntProperty(props, getPropertyQuestionPoints(questionNumber));
        } catch (ServiceException e) {
          points = questionNumber * JjDefaults.QUESTION_POINTS_MULTIPLIER;
        }
        final String question = Utilities.getPropertyOrNull(props, getPropertyQuestionText(categoryNumber, questionNumber));
        final String answer = Utilities.getPropertyOrNull(props, getPropertyAnswerText(categoryNumber, questionNumber));
        final String questionImage = Utilities.getPropertyOrNull(
            props, getPropertyQuestionImage(categoryNumber, questionNumber));
        final String answerImage = Utilities.getPropertyOrNull(
            props, getPropertyAnswerImage(categoryNumber, questionNumber));
        if ((!StringUtils.isBlank(question) || questionImage != null) &&
            (!StringUtils.isBlank(answer) || answerImage != null)) {
          questions.add(new Question(question, questionImage, answer, answerImage, points));
        }
      }
    } catch (Exception e) {
      // Stop on first error - no further questions for this category is defined.
    }
    return questions;
  }

  /**
   * Parses players from the passed game properties file.
   * Empty player names are skipped/ignored.
   * Note, that it doesn't update current game players - it's assumed
   * to be updated from the code that initiated parsing the file.
   * @param props  properties file
   * @return parsed valid player names list (or empty if none is parsed)
   */
  private List<String> parsePlayersDataIfAny(Properties props) {
    List<String> playerNames = new ArrayList<>();
    try {
      // Notice that the (user facing) index starts at 1.
      for (int playerNumber = 1; playerNumber < MAX_LOOP; playerNumber++) {
        String playerName = Utilities.getProperty(props, getPropertyPlayerName(playerNumber));
        if (!StringUtils.isBlank(playerName)) {
          playerNames.add(playerName);
        }
      }
    } catch (ServiceException e) {
      // Stop on the first error, since there are no more players (or at all).
    }
    return playerNames;
  }

  /**
   * Parses bonus questions if found. Empty string values are ignored/skipped.
   * @param props  properties file
   * @return parsed valid bonus questions (or empty list if no questions are parsed)
   */
  private List<Question> parseBonusQuestionsIfAny(Properties props) {
    int bonusPoints;
    try {
      bonusPoints = Utilities.getIntProperty(props, PROPERTY_BONUS_QUESTION_POINTS);
    } catch (ServiceException e) {
      bonusPoints = JjDefaults.BONUS_QUESTION_POINTS;
    }
    final List<Question> questions = new ArrayList<>();
    // Notice that the (user facing) index starts at 1.
    for (int questionNumber = 1; questionNumber < MAX_LOOP; questionNumber++) {
      String questionText = Utilities.getPropertyOrNull(props, getPropertyBonusQuestionText(questionNumber));
      String answerText = Utilities.getPropertyOrNull(props, getPropertyBonusAnswerText(questionNumber));
      String questionImage = Utilities.getPropertyOrNull(props, getPropertyBonusQuestionImage(questionNumber));
      String answerImage = Utilities.getPropertyOrNull(props, getPropertyBonusAnswerImage(questionNumber));
      Question question = new Question(questionText, questionImage, answerText, answerImage, bonusPoints);
      if (!question.isNotAskable()) {
        questions.add(question);
      }
    }
    return questions;
  }
}
