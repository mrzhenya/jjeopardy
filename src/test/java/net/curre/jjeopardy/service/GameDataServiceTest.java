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
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static net.curre.jjeopardy.service.FileParsingResult.Message.*;
import static org.junit.Assert.*;

/**
 * Tests the game data service - setting player names, scores, parsing game data.
 *
 * @author Yevgeny Nyden
 */
public class GameDataServiceTest {

  /** Path to valid data test game files. */
  private static final String PATH_VALID = "src/test/resources/valid-data/";

  /** Path to invalid data test game files. */
  private static final String PATH_INVALID = "src/test/resources/invalid-data/";

  /**
   * Reference to the game data service to test on each run.
   */
  private GameDataService gameService;

  /**
   * Initializes the state before each test run.
   */
  @Before
  public void init() {
    this.gameService = new GameDataService();
  }

  /**
   * Tests the initial service state.
   */
  @Test
  public void initialRun() {
    assertFalse("Game should not be ready", this.gameService.isGameReady());
    List<Player> players = this.gameService.getPlayers();
    assertNotNull("List of players should not be null", players);
    assertEquals("Players list should be empty", 0, players.size());

    GameData gameData = this.gameService.getGameData();
    assertNotNull("Game data should not be null", gameData);
    assertNull("Game name should not be set", gameData.getGameName());
    assertNotNull("List of players should not be null", gameData.getPlayerNames());
    assertTrue("List of players should be empty", gameData.getPlayerNames().isEmpty());
    assertNotNull("List of categories should not be null", gameData.getCategories());
    assertTrue("List of categories should be empty", gameData.getCategories().isEmpty());
    assertNotNull("List of bonus questions should not be null", gameData.getBonusQuestions());
    assertTrue("List of bonus questions should be empty", gameData.getBonusQuestions().isEmpty());
  }

  /**
   * Tests updating the players/names.
   */
  @Test
  public void testUpdatePlayersFromNames() {
    List<String> playerNames = createDefaultPlayers();
    this.gameService.updatePlayersFromNames(playerNames);
    List<Player> players = this.gameService.getPlayers();
    assertNotNull("List of players should not be null", players);
    assertEquals("Wrong number of players", 3, players.size());
    assertPlayer("Wrong player 0", players.get(0), 0, "One", 0);
    assertPlayer("Wrong player 1", players.get(1), 1, "Two", 0);
    assertPlayer("Wrong player 2", players.get(2), 2, "Three", 0);
    assertFalse("Game should still not be ready", this.gameService.isGameReady());

    // This should reset the players.
    playerNames = new ArrayList<>();
    playerNames.add("Four");
    this.gameService.updatePlayersFromNames(playerNames);
    players = this.gameService.getPlayers();
    assertNotNull("List of players should not be null", players);
    assertEquals("Wrong number of players", 1, players.size());
    assertPlayer("Wrong player 0", players.get(0), 0, "Four", 0);
  }

  /**
   * Tests updating and resetting the players scores.
   */
  @Test
  public void testAdjustingPlayerScores() {
    List<String> playerNames = createDefaultPlayers();
    this.gameService.updatePlayersFromNames(playerNames);
    this.gameService.addToPlayerScore(2, 222);
    this.gameService.addToPlayerScore(1, 111);
    this.gameService.addToPlayerScore(0, 0);
    List<Player> players = this.gameService.getPlayers();
    assertPlayer("Wrong player 0", players.get(0), 0, "One", 0);
    assertPlayer("Wrong player 1", players.get(1), 1, "Two", 111);
    assertPlayer("Wrong player 2", players.get(2), 2, "Three", 222);

    // Adding more.
    this.gameService.addToPlayerScore(0, 10);
    assertPlayer("Wrong player 0", players.get(0), 0, "One", 10);
    this.gameService.addToPlayerScore(1, 20);
    assertPlayer("Wrong player 1", players.get(1), 1, "Two", 131);

    // Testing full scores reset.
    this.gameService.resetPlayerScores();
    players = this.gameService.getPlayers();
    assertPlayer("Wrong player 0", players.get(0), 0, "One", 0);
    assertPlayer("Wrong player 1", players.get(1), 1, "Two", 0);
    assertPlayer("Wrong player 2", players.get(2), 2, "Three", 0);
  }

  /**
   * Tests updating the players scores.
   */
  @Test
  public void testWinnerPlayer() {
    List<String> playerNames = createDefaultPlayers();
    this.gameService.updatePlayersFromNames(playerNames);
    this.gameService.addToPlayerScore(1, 101);
    assertPlayer("Wrong winner 1", this.gameService.getWinner(), 1, "Two", 101);

    this.gameService.addToPlayerScore(2, 202);
    assertPlayer("Wrong winner 2", this.gameService.getWinner(), 2, "Three", 202);

    this.gameService.addToPlayerScore(1, 200);
    assertPlayer("Wrong winner 1", this.gameService.getWinner(), 1, "Two", 301);
  }

  /**
   * Tests loading game data from a valid game file.
   */
  @Test
  public void testLoadGameDataValidDefault() {
    FileParsingResult result = loadGameTestFile(PATH_VALID + "default.xml");
    assertTrue("Game data should be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 0, 0, 3);
    assertThreeInfoMessages(result.getInfoMessages(), 3, 9, 0, 0);

    assertEquals("Wrong file name",
            new File(PATH_VALID + "default.xml").getAbsolutePath(), result.getFileName());
    assertFalse("Short result message should not be blank", StringUtils.isBlank(result.getResulTitleShort()));
    assertFalse("Long result message should not be blank", StringUtils.isBlank(result.getResulTitleLong()));

    // There should still be no players.
    List<Player> players = this.gameService.getPlayers();
    assertNotNull("List of players should not be null", players);
    assertEquals("Wrong number of players", 0, players.size());

    GameData gameData = this.gameService.getGameData();
    assertDefaultValidData(gameData, "valid-default",
            10, 20, 30, 0, 0);
  }

  /** Tests loading game data from a valid game file w/o scores. */
  @Test
  public void testLoadGameDataValidNoScores() {
    FileParsingResult result = loadGameTestFile(PATH_VALID + "no-scores.xml");
    assertTrue("Game data should be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 0, 0, 3);
    assertThreeInfoMessages(result.getInfoMessages(), 3, 9, 0, 0);

    // The same default data is expected but question points should be initialized to defaults.
    GameData gameData = this.gameService.getGameData();
    assertDefaultValidData(gameData, "valid-no-scores",
            50, 100, 150, 0, 0);
  }

  /** Tests loading game data from a valid game file with valid number of players. */
  @Test
  public void testLoadGameDataValidWithEnoughPlayers() {
    FileParsingResult result = loadGameTestFile(PATH_VALID + "with-enough-players.xml");
    assertTrue("Game data should be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 0, 0, 3);
    assertThreeInfoMessages(result.getInfoMessages(), 3, 9, 3, 0);

    GameData gameData = this.gameService.getGameData();
    assertDefaultValidData(gameData, "valid-with-enough-players",
            10, 20, 30, 3, 0);
    List<String> playerNames = gameData.getPlayerNames();
    assertEquals("Wrong player 0 name", "One", playerNames.get(0));
    assertEquals("Wrong player 1 name", "Two", playerNames.get(1));
    assertEquals("Wrong player 2 name", "Three", playerNames.get(2));
  }

  /** Tests loading game data from a valid game file with not enough players. */
  @Test
  public void testLoadGameDataValidNotEnoughPlayers() {
    FileParsingResult result = loadGameTestFile(PATH_VALID + "not-enough-players.xml");
    assertTrue("Game data should be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 0, 1, 2);
    assertGeneralInfoMessage(result.getInfoMessages().get(0), 3, 9);
    assertBonusQuestionsInfoMessage(result.getInfoMessages().get(1), 0);

    GameData gameData = this.gameService.getGameData();
    assertDefaultValidData(gameData, "valid-not-enough-players",
            10, 20, 30, 0, 0);
  }

  /** Tests loading game data from a valid game file but with too many players. */
  @Test
  public void testLoadGameDataValidTooManyPlayers() {
    FileParsingResult result = loadGameTestFile(PATH_VALID + "too-many-players.xml");
    assertTrue("Game data should be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 0, 1, 3);
    assertThreeInfoMessages(result.getInfoMessages(), 3, 9, 6, 0);

    // Extra players are ignored.
    GameData gameData = this.gameService.getGameData();
    assertDefaultValidData(gameData, "valid-too-many-players",
            10, 20, 30, 6, 0);
    List<String> playerNames = gameData.getPlayerNames();
    assertEquals("Wrong player 0 name", "One", playerNames.get(0));
    assertEquals("Wrong player 1 name", "Two", playerNames.get(1));
    assertEquals("Wrong player 2 name", "Three", playerNames.get(2));
    assertEquals("Wrong player 3 name", "Four", playerNames.get(3));
    assertEquals("Wrong player 4 name", "Five", playerNames.get(4));
    assertEquals("Wrong player 5 name", "Six", playerNames.get(5));
  }

  /** Tests loading game data from a valid game file but with a lot of leading/trailing whitespace. */
  @Test
  public void testLoadGameDataValidLotsOfWhitespace() {
    FileParsingResult result = loadGameTestFile(PATH_VALID + "lots-of-whitespace.xml");
    assertTrue("Game data should be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 0, 0, 3);
    assertThreeInfoMessages(result.getInfoMessages(), 3, 9, 4, 0);

    // Extra players are ignored.
    GameData gameData = this.gameService.getGameData();
    assertDefaultValidData(gameData, "valid-lots-of-whitespace",
            10, 20, 30, 4, 0);
    List<String> playerNames = gameData.getPlayerNames();
    assertEquals("Wrong player 0 name", "One", playerNames.get(0));
    assertEquals("Wrong player 1 name", "Two", playerNames.get(1));
    assertEquals("Wrong player 2 name", "Three", playerNames.get(2));
    assertEquals("Wrong player 3 name", "Four", playerNames.get(3));
  }

  /** Tests loading game data from a valid game file with enough bonus questions. */
  @Test
  public void testLoadGameDataValidWithEnoughBonusQuestions() {
    FileParsingResult result = loadGameTestFile(PATH_VALID + "with-enough-bonus-questions.xml");
    assertTrue("Game data should be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 0, 0, 3);
    assertThreeInfoMessages(result.getInfoMessages(), 3, 9, 0, 3);

    GameData gameData = this.gameService.getGameData();
    assertDefaultValidData(gameData, "valid-with-enough-bonus-questions",
            10, 20, 30, 0, 3);
    List<Question> bonusQuestions = gameData.getBonusQuestions();
    for (int ind = 0; ind < bonusQuestions.size(); ind++) {
      assertQuestion("Bonus question " + ind, bonusQuestions.get(ind),
              "Bonus Question " + (ind + 1), "Bonus Answer " + (ind + 1), 55, false);
    }
  }

  /** Tests loading game data from a valid game file with not enough bonus questions. */
  @Test
  public void testLoadGameDataValidNotEnoughBonusQuestions() {
    FileParsingResult result = loadGameTestFile(PATH_VALID + "not-enough-bonus-questions.xml");
    assertTrue("Game data should be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 0, 1, 2);
    assertGeneralInfoMessage(result.getInfoMessages().get(0), 3, 9);
    assertPlayersInfoMessage(result.getInfoMessages().get(1), 3);

    // Bonus questions should not be parsed because they are too few for the number of players.
    GameData gameData = this.gameService.getGameData();
    assertDefaultValidData(gameData, "valid-not-enough-bonus-questions",
            10, 20, 30, 3, 0);
  }

  /** Tests loading game data from an invalid game file with not enough categories. */
  @Test
  public void testLoadGameDataInvalidTooFewCategories() {
    FileParsingResult result = loadGameTestFile(PATH_INVALID + "too-few-categories.xml");
    assertFalse("Game data should not be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 1, 0, 0);
  }

  /** Tests loading game data from an invalid game file with not enough questions. */
  @Test
  public void testLoadGameDataInvalidTooFewQuestions() {
    FileParsingResult result = loadGameTestFile(PATH_INVALID + "too-few-questions.xml");
    assertFalse("Game data should not be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 1, 0, 0);
  }

  /**
   * Tests loading game data from an invalid game file with different questions
   * number in one category.
   */
  @Test
  public void testLoadGameDataInvalidNotEvenQuestions() {
    FileParsingResult result = loadGameTestFile(PATH_INVALID + "not-even-questions.xml");
    assertFalse("Game data should not be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 1, 0, 0);
  }

  /** Tests loading game data from an invalid not-XML file. */
  @Test
  public void testLoadGameDataInvalidNotXml() {
    FileParsingResult result = loadGameTestFile(PATH_INVALID + "not-xml-format.xml");
    assertFalse("Game data should not be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 1, 0, 0);
  }

  /** Tests loading game data from an invalid game file w/o game name. */
  @Test
  public void testLoadGameDataInvalidNoGameName() {
    FileParsingResult result = loadGameTestFile(PATH_INVALID + "no-game-name.xml");
    assertFalse("Game data should not be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 1, 0, 0);
  }

  /** Tests loading game data from an invalid game file with blank game name. */
  @Test
  public void testLoadGameDataInvalidGameNameBlank() {
    FileParsingResult result = loadGameTestFile(PATH_INVALID + "blank-game-name.xml");
    assertFalse("Game data should not be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 1, 0, 0);
  }

  /** Tests loading game data from an invalid game file with blank category name. */
  @Test
  public void testLoadGameDataInvalidCategoryNameBlank() {
    FileParsingResult result = loadGameTestFile(PATH_INVALID + "blank-category-name.xml");
    assertFalse("Game data should not be usable", result.isGameDataUsable());
    // Two errors - one for empty category, another one for not enough categories parsed
    assertResultMessageNumbers(result, 2, 0, 0);
  }

  /** Tests loading game data from an invalid game file w/o category name. */
  @Test
  public void testLoadGameDataInvalidNoCategoryName() {
    FileParsingResult result = loadGameTestFile(PATH_INVALID + "no-category-name.xml");
    assertFalse("Game data should not be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 1, 0, 0);
  }

  /** Tests loading game data from an invalid game file with one blank question. */
  @Test
  public void testLoadGameDataInvalidOneQuestionBlank() {
    FileParsingResult result = loadGameTestFile(PATH_INVALID + "one-question-blank.xml");
    assertFalse("Game data should not be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 1, 0, 0);
  }

  /** Tests loading game data from an invalid game file with one blank answer. */
  @Test
  public void testLoadGameDataInvalidOneAnswerBlank() {
    FileParsingResult result = loadGameTestFile(PATH_INVALID + "one-answer-blank.xml");
    assertFalse("Game data should not be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 1, 0, 0);
  }

  /** Tests loading game data from an invalid game file with a missing answer. */
  @Test
  public void testLoadGameDataInvalidMissingAnswer() {
    FileParsingResult result = loadGameTestFile(PATH_INVALID + "missing-one-answer.xml");
    assertFalse("Game data should not be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 1, 0, 0);
  }

  /** Tests loading game data from an invalid game file with too many questions. */
  @Test
  public void testLoadGameDataInvalidTooManyQuestions() {
    FileParsingResult result = loadGameTestFile(PATH_INVALID + "too-many-questions.xml");
    assertFalse("Game data should not be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 1, 0, 0);
  }

  /** Tests loading game data from an invalid game file with too many categories. */
  @Test
  public void testLoadGameDataInvalidTooManyCategories() {
    FileParsingResult result = loadGameTestFile(PATH_INVALID + "too-many-categories.xml");
    assertFalse("Game data should not be usable", result.isGameDataUsable());
    assertResultMessageNumbers(result, 1, 0, 0);
  }

  /**
   * Asserts the player object's state.
   * @param message string to add to the error messages
   * @param player player object to test
   * @param index expected player's index
   * @param name expected player's name
   * @param score expected player's score
   */
  private static void assertPlayer(String message, Player player, int index, String name, int score) {
    assertNotNull(message + "; Player should not be null", player);
    assertEquals(message + "; Wrong player name", name, player.getName());
    assertEquals(message + "; Wrong player index", index, player.getIndex());
    assertEquals(message + "; Wrong player score", score, player.getScore());
    assertNotNull(message + "; Player name string should not be null", player.getNameString());
    assertEquals(message + "; Player name string should contain player name",
            1, StringUtils.countMatches(player.getNameString(), name));
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
   * Asserts result message counts.
   * @param result result object to test
   * @param errorsCount expected number of error messages
   * @param warningsCount expected number of warning messages
   * @param infoCount expected number of informational messages
   */
  private static void assertResultMessageNumbers(FileParsingResult result,
                                                 int errorsCount, int warningsCount, int infoCount) {
    List<String> messages = result.getErrorMessages();
    assertNotNull("Error messages list should not be null", messages);
    assertEquals("Wrong number of error messages", errorsCount, messages.size());
    messages = result.getWarningMessages();
    assertNotNull("Warning messages list should not be null", messages);
    assertEquals("Wrong number of warning messages", warningsCount, messages.size());
    messages = result.getInfoMessages();
    assertNotNull("Info messages list should not be null", messages);
    assertEquals("Wrong number of informational messages", infoCount, messages.size());
    for (int ind = 0; ind < infoCount; ind++) {
      assertFalse("Info message " + ind + " should not be blank", StringUtils.isBlank(messages.get(ind)));
    }
  }

  /**
   * Asserts default valid game data file.
   * @param gameData game data to test
   * @param gameName expected game name
   * @param points1 expected points for question 1
   * @param points2 expected points for question 2
   * @param points3 expected points for question 3
   * @param playersCount expected players count
   * @param bonusQuestionsCount expected bonus questions count
   */
  private static void assertDefaultValidData(
          GameData gameData, String gameName, int points1, int points2, int points3,
          int playersCount, int bonusQuestionsCount) {
    assertNotNull("Game data should not be null", gameData);
    assertEquals("Wrong game name", gameName, gameData.getGameName());
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
   * Asserts typical 3 info messages.
   * @param infoMessages info messages to validate
   * @param questionsCount expected total questions count
   * @param categoriesCount expected categories count
   * @param playersCount expected players count
   * @param bonusQuestionsCount expected bonus question count
   */
  private static void assertThreeInfoMessages(
          List<String> infoMessages, int questionsCount, int categoriesCount, int playersCount, int bonusQuestionsCount) {
    assertGeneralInfoMessage(infoMessages.get(0), questionsCount, categoriesCount);
    assertPlayersInfoMessage(infoMessages.get(1), playersCount);
    assertBonusQuestionsInfoMessage(infoMessages.get(2), bonusQuestionsCount);
  }

  /**
   * Asserts parsed players info message.
   * @param message message to test
   * @param questionsCount expected total questions count
   * @param categoriesCount expected categories count
   */
  private static void assertGeneralInfoMessage(String message, int questionsCount, int categoriesCount) {
    assertEquals("Wrong questions info message",
            LocaleService.getString(MSG_QUESTIONS_PARSED.getPropertyName(),
                    String.valueOf(categoriesCount), String.valueOf(questionsCount)),
            message);
  }

  /**
   * Asserts parsed players info message.
   * @param message message to test
   * @param playersCount expected players count
   */
  private static void assertPlayersInfoMessage(String message, int playersCount) {
    assertEquals("Wrong players info message",
            LocaleService.getString(MSG_PLAYERS_PARSED.getPropertyName(), String.valueOf(playersCount)),
            message);
  }

  /**
   * Asserts parsed bonus questions info message.
   * @param message message to test
   * @param bonusQuestionsCount expected bonus question count
   */
  private static void assertBonusQuestionsInfoMessage(String message, int bonusQuestionsCount) {
    assertEquals("Wrong bonus questions info message",
            LocaleService.getString(MSG_BONUS_QUESTIONS_PARSED.getPropertyName(), String.valueOf(bonusQuestionsCount)),
            message);
  }

  /**
   * Loads game test file.
   * @param fileName filename/path
   * @return result object
   */
  private FileParsingResult loadGameTestFile(String fileName) {
    File file = new File(fileName);
    assertTrue("Unable to find test file: " + fileName, file.exists());
    FileParsingResult result = this.gameService.loadGameData(file.getAbsolutePath());
    assertNotNull("Parsing result should not be null", result);
    return result;
  }

  /**
   * Creates a list with three default player names.
   * @return list of player name strings
   */
  private static List<String> createDefaultPlayers() {
    List<String> playerNames = new ArrayList<>();
    playerNames.add("One");
    playerNames.add("Two");
    playerNames.add("Three");
    return playerNames;
  }
}
