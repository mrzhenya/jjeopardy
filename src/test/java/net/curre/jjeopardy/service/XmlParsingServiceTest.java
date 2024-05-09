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

import net.curre.jjeopardy.TestSettings;
import net.curre.jjeopardy.bean.Category;
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.bean.Question;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests the XML parsing service.
 *
 * @author Yevgeny Nyden
 */
public class XmlParsingServiceTest {

  /**
   * Reference to the XML parsing service to test on each run.
   */
  private XmlParsingService testXmlParser;

  /**
   * Initializes the state before each test run.
   */
  @Before
  public void init() {
    this.testXmlParser = new XmlParsingService();
  }

  /**
   * Tests parsing game data from a valid game file.
   */
  @Test
  public void testParseXmlGameDataValidDefault() {
    GameData gameData = parseXmlGameTestFile(TestSettings.VALID_DATA_PATH + "default.xml");
    assertEquals("Wrong file path",
        new File(TestSettings.VALID_DATA_PATH + "default.xml").getAbsolutePath(), gameData.getFilePath());

    assertDefaultValidData(gameData, "valid-default", "test-description",
        10, 20, 30, 0, 0);
    assertTrue("Game data should be usable", gameData.isGameDataUsable());
  }

  /**
   * Tests parsing game data from a valid game file where some questions
   * and answer are represented by images only.
   */
  @Test
  public void testParseXmlGameDataValidOnlyImages() {
    GameData gameData = parseXmlGameTestFile(TestSettings.VALID_DATA_PATH + "only-images.xml");
    assertNotNull("Game data should not be null", gameData);
    assertEquals("Wrong file path",
        new File(TestSettings.VALID_DATA_PATH + "only-images.xml").getAbsolutePath(), gameData.getFilePath());

    assertNotNull("Null categories", gameData.getCategories());
    assertEquals("Wrong categories number", 3, gameData.getCategories().size());
    Category category = gameData.getCategories().get(0);
    assertNotNull("Null category 0", category);
    assertEquals("Wrong questions count", 3, category.getQuestionsCount());
    assertOnlyImageQuestion(
        category.getQuestion(0), "questionImage1.jpg", "answerImage1.jpg");
    assertOnlyImageQuestion(
        category.getQuestion(1), "questionImage2.jpg", "answerImage2.jpg");
    assertOnlyImageQuestion(
        category.getQuestion(2), "questionImage3.jpg", "answerImage3.jpg");

    assertTrue("Game data should be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from a valid game file w/o scores. */
  @Test
  public void testParseXmlGameDataValidNoScores() {
    // The same default data is expected but question points should be initialized to defaults.
    GameData gameData = parseXmlGameTestFile(TestSettings.VALID_DATA_PATH + "no-scores.xml");
    assertDefaultValidData(gameData, "valid-no-scores", null,
            50, 100, 150, 0, 0);
    assertTrue("Game data should be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from a valid game file with valid number of players. */
  @Test
  public void testParseXmlGameDataValidWithEnoughPlayers() {
    GameData gameData = parseXmlGameTestFile(TestSettings.VALID_DATA_PATH + "with-enough-players.xml");
    assertDefaultValidData(gameData, "valid-with-enough-players", null,
            10, 20, 30, 3, 0);
    List<String> playerNames = gameData.getPlayerNames();
    assertEquals("Wrong player 0 name", "One", playerNames.get(0));
    assertEquals("Wrong player 1 name", "Two", playerNames.get(1));
    assertEquals("Wrong player 2 name", "Three", playerNames.get(2));
    assertTrue("Game data should be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from a valid game file with not enough players. */
  @Test
  public void testParseXmlGameDataValidNotEnoughPlayers() {
    GameData gameData = parseXmlGameTestFile(TestSettings.VALID_DATA_PATH + "not-enough-players.xml");
    assertDefaultValidData(gameData, "valid-not-enough-players", null,
            10, 20, 30, 1, 0);
    assertTrue("Game data should be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from a valid game file but with too many players. */
  @Test
  public void testParseXmlGameDataValidTooManyPlayers() {
    GameData gameData = parseXmlGameTestFile(TestSettings.VALID_DATA_PATH + "too-many-players.xml");

    // Extra players are ignored.
    assertDefaultValidData(gameData, "valid-too-many-players", null,
            10, 20, 30, 7, 0);
    List<String> playerNames = gameData.getPlayerNames();
    assertEquals("Wrong player 0 name", "One", playerNames.get(0));
    assertEquals("Wrong player 1 name", "Two", playerNames.get(1));
    assertEquals("Wrong player 2 name", "Three", playerNames.get(2));
    assertEquals("Wrong player 3 name", "Four", playerNames.get(3));
    assertEquals("Wrong player 4 name", "Five", playerNames.get(4));
    assertEquals("Wrong player 5 name", "Six", playerNames.get(5));
    assertTrue("Game data should be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from a valid game file but with a lot of leading/trailing whitespace. */
  @Test
  public void testParseXmlGameDataValidLotsOfWhitespace() {
    GameData gameData = parseXmlGameTestFile(TestSettings.VALID_DATA_PATH + "lots-of-whitespace.xml");

    // Extra players are ignored.
    assertDefaultValidData(gameData, "valid-lots-of-whitespace", null,
            10, 20, 30, 4, 0);
    List<String> playerNames = gameData.getPlayerNames();
    assertEquals("Wrong player 0 name", "One", playerNames.get(0));
    assertEquals("Wrong player 1 name", "Two", playerNames.get(1));
    assertEquals("Wrong player 2 name", "Three", playerNames.get(2));
    assertEquals("Wrong player 3 name", "Four", playerNames.get(3));
    assertTrue("Game data should be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from a valid game file with a question as a list of items. */
  @Test
  public void testParseXmlGameDataValidItemsList() {
    GameData gameData = parseXmlGameTestFile(TestSettings.VALID_DATA_PATH + "items-list.xml");
    assertEquals("Wrong file path",
        new File(TestSettings.VALID_DATA_PATH + "items-list.xml").getAbsolutePath(), gameData.getFilePath());
    assertTrue("Game data should be usable", gameData.isGameDataUsable());

    List<Category> categories = gameData.getCategories();
    assertNotNull("List of categories should not be null", categories);
    assertEquals("Wrong size of categories", 3, categories.size());

    Category category = categories.get(0);
    assertEquals("Wrong number of questions in category 0", 3, category.getQuestionsCount());
    Question question = category.getQuestion(0);
    assertNotNull("Question 0 should not be null", question);
    assertEquals("Wrong question 0 string",
        "List of items:\n        a. One\n        b. Two\n        c. Three", question.getQuestion());
  }

  /** Tests loading game data from a valid game file with enough bonus questions. */
  @Test
  public void testParseXmlGameDataValidWithEnoughBonusQuestions() {
    GameData gameData = parseXmlGameTestFile(TestSettings.VALID_DATA_PATH + "with-enough-bonus-questions.xml");
    assertDefaultValidData(gameData, "valid-with-enough-bonus-questions", null,
            10, 20, 30, 0, 3);
    List<Question> bonusQuestions = gameData.getBonusQuestions();
    for (int ind = 0; ind < bonusQuestions.size(); ind++) {
      assertQuestion("Bonus question " + ind, bonusQuestions.get(ind),
              "Bonus Question " + (ind + 1), "Bonus Answer " + (ind + 1), 55, false);
    }
    assertTrue("Game data should be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from a valid game file with not enough bonus questions. */
  @Test
  public void testParseXmlGameDataValidNotEnoughBonusQuestions() {
    GameData gameData = parseXmlGameTestFile(TestSettings.VALID_DATA_PATH + "not-enough-bonus-questions.xml");

    // Bonus questions should not be parsed because they are too few for the number of players.
    assertDefaultValidData(gameData, "valid-not-enough-bonus-questions", null,
            10, 20, 30, 3, 2);
    assertTrue("Game data should be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from an invalid game file with not enough categories. */
  @Test
  public void testParseXmlGameDataInvalidTooFewCategories() {
    GameData gameData = parseXmlGameTestFile(TestSettings.INVALID_DATA_PATH + "too-few-categories.xml");
    assertNotNull("Game data should not be null", gameData);
    assertFalse("Game data should not be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from an invalid game file with not enough questions. */
  @Test
  public void testParseXmlGameDataInvalidTooFewQuestions() {
    GameData gameData = parseXmlGameTestFile(TestSettings.INVALID_DATA_PATH + "too-few-questions.xml");
    assertNotNull("Game data should not be null", gameData);
    assertFalse("Game data should not be usable", gameData.isGameDataUsable());
  }

  /**
   * Tests loading game data from an invalid game file with different questions
   * number in one category.
   */
  @Test
  public void testParseXmlGameDataInvalidNotEvenQuestions() {
    GameData gameData = parseXmlGameTestFile(TestSettings.INVALID_DATA_PATH + "not-even-questions.xml");
    assertNotNull("Game data should not be null", gameData);
    assertFalse("Game data should not be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from an invalid not-XML file. */
  @Test
  public void testParseXmlGameDataInvalidNotXml() {
    GameData gameData = parseXmlGameTestFile(TestSettings.INVALID_DATA_PATH + "not-xml-format.xml");
    assertNotNull("Game data should not be null", gameData);
    assertFalse("Game data should not be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from an invalid game file w/o game name. */
  @Test
  public void testParseXmlGameDataInvalidNoGameName() {
    GameData gameData = parseXmlGameTestFile(TestSettings.INVALID_DATA_PATH + "no-game-name.xml");
    assertNotNull("Game data should not be null", gameData);
    assertFalse("Game data should not be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from an invalid game file with blank game name. */
  @Test
  public void testParseXmlGameDataInvalidGameNameBlank() {
    GameData gameData = parseXmlGameTestFile(TestSettings.INVALID_DATA_PATH + "blank-game-name.xml");
    assertNotNull("Game data should not be null", gameData);
    assertFalse("Game data should not be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from an invalid game file with blank category name. */
  @Test
  public void testParseXmlGameDataInvalidCategoryNameBlank() {
    GameData gameData = parseXmlGameTestFile(TestSettings.INVALID_DATA_PATH + "blank-category-name.xml");
    // Two errors - one for empty category, another one for not enough categories parsed
    assertNotNull("Game data should not be null", gameData);
    assertFalse("Game data should not be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from an invalid game file w/o category name. */
  @Test
  public void testParseXmlGameDataInvalidNoCategoryName() {
    GameData gameData = parseXmlGameTestFile(TestSettings.INVALID_DATA_PATH + "no-category-name.xml");
    assertNotNull("Game data should not be null", gameData);
    assertFalse("Game data should not be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from an invalid game file with one blank question. */
  @Test
  public void testParseXmlGameDataInvalidOneQuestionBlank() {
    GameData gameData = parseXmlGameTestFile(TestSettings.INVALID_DATA_PATH + "one-question-blank.xml");
    assertNotNull("Game data should not be null", gameData);
    assertFalse("Game data should not be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from an invalid game file with one blank answer. */
  @Test
  public void testParseXmlGameDataInvalidOneAnswerBlank() {
    GameData gameData = parseXmlGameTestFile(TestSettings.INVALID_DATA_PATH + "one-answer-blank.xml");
    assertNotNull("Game data should not be null", gameData);
    assertFalse("Game data should not be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from an invalid game file with a missing answer. */
  @Test
  public void testParseXmlGameDataInvalidMissingAnswer() {
    GameData gameData = parseXmlGameTestFile(TestSettings.INVALID_DATA_PATH + "missing-one-answer.xml");
    assertNotNull("Game data should not be null", gameData);
    assertFalse("Game data should not be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from an invalid game file with too many questions. */
  @Test
  public void testParseXmlGameDataInvalidTooManyQuestions() {
    GameData gameData = parseXmlGameTestFile(TestSettings.INVALID_DATA_PATH + "too-many-questions.xml");
    assertNotNull("Game data should not be null", gameData);
    assertFalse("Game data should not be usable", gameData.isGameDataUsable());
  }

  /** Tests loading game data from an invalid game file with too many categories. */
  @Test
  public void testParseXmlGameDataInvalidTooManyCategories() {
    GameData gameData = parseXmlGameTestFile(TestSettings.INVALID_DATA_PATH + "too-many-categories.xml");
    assertNotNull("Game data should not be null", gameData);
    assertTrue("Game data should be usable", gameData.isGameDataUsable());
  }

  /**
   * Asserts Question object state.
   * @param message string to add to error messages
   * @param question question object to test
   * @param q expected  question string
   * @param a expected  answer string
   * @param points expected number of points
   * @param hasParent true if the question is expected to have a parent
   */
  private static void assertQuestion(
          String message, Question question, String q, String a, int points, boolean hasParent) {
    assertNotNull(message + "; question is null", question);
    assertFalse(message + "; question should not be asked yet", question.isHasBeenAsked());
    assertEquals(message + "; wrong question string", q, question.getQuestion());
    assertEquals(message + "; wrong answer string", a, question.getAnswer());
    assertEquals(message + "; wrong number of points", points, question.getPoints());
    if (hasParent) {
      assertFalse(message + "; parent name should not be blank", StringUtils.isBlank(question.getParentName()));
    } else {
      assertNull(message + "; parent name should be null", question.getParentName());
    }
  }

  /**
   * Asserts default valid game data file.
   * @param gameData game data to test
   * @param gameName expected game name
   * @param description expected game description
   * @param points1 expected points for question 1
   * @param points2 expected points for question 2
   * @param points3 expected points for question 3
   * @param playersCount expected players count
   * @param bonusQuestionsCount expected bonus questions count
   */
  protected static void assertDefaultValidData(
          GameData gameData, String gameName, String description, int points1, int points2, int points3,
          int playersCount, int bonusQuestionsCount) {
    assertNotNull("Game data should not be null", gameData);
    assertEquals("Wrong game name", gameName, gameData.getGameName());
    assertEquals("Wrong game description", description, gameData.getGameDescription());
    assertNotNull("List of bonus questions should not be null", gameData.getBonusQuestions());
    assertEquals("Wrong number of bonus questions", bonusQuestionsCount, gameData.getBonusQuestions().size());
    assertNotNull("List of players should not be null", gameData.getPlayerNames());
    assertEquals("Wrong number of players", playersCount, gameData.getPlayerNames().size());
    List<Category> categories = gameData.getCategories();
    assertNotNull("List of categories should not be null", categories);
    assertEquals("Wrong size of categories", 3, categories.size());
    for (int ind = 0; ind < categories.size(); ind++) {
      Category category = categories.get(ind);
      assertEquals("Wrong number of questions in category " + ind, 3, category.getQuestionsCount());
      assertEquals("Wrong category " + ind + " name", "Category " + (ind + 1), category.getName());
      assertFalse("Category " + ind + " string should not be blank",
              StringUtils.isBlank(category.getNameString()));
      assertQuestion("Question 0", category.getQuestion(0),
              "Category " + (ind + 1) + ", question 1",
              "Category " + (ind + 1) + ", answer 1",
              points1, true);
      assertQuestion("Question 1", category.getQuestion(1),
              "Category " + (ind + 1) + ", question 2",
              "Category " + (ind + 1) + ", answer 2",
              points2, true);
      assertQuestion("Question 2", category.getQuestion(2),
              "Category " + (ind + 1) + ", question 3",
              "Category " + (ind + 1) + ", answer 3",
              points3, true);
    }
  }

  /**
   * Asserts images on a question that only contains images (no text strings).
   * @param question question to test
   * @param questionImage value for the question image
   * @param answerImage value for the answer image
   */
  private void assertOnlyImageQuestion(Question question, String questionImage, String answerImage) {
    assertNotNull(question);
    assertEquals("Wrong question text", "", question.getQuestion());
    assertEquals("Wrong question answer", "", question.getAnswer());
    assertEquals("Wrong question image", questionImage, question.getQuestionImage());
    assertEquals("Wrong answer image", answerImage, question.getAnswerImage());
  }

  /**
   * Parses game test file.
   * @param fileName filename/path
   * @return result object
   */
  private GameData parseXmlGameTestFile(String fileName) {
    File file = new File(fileName);
    assertTrue("Unable to find test file: " + fileName, file.exists());
    GameData gameData = this.testXmlParser.parseXmlGameData(file.getAbsolutePath(), null);
    assertNotNull("Parsing result should not be null", gameData);
    return gameData;
  }
}
