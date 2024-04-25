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

import net.curre.jjeopardy.images.ImageEnum;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.util.JjDefaults;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static net.curre.jjeopardy.bean.FileParsingResult.Message.*;
import static net.curre.jjeopardy.bean.QuestionTest.assertQuestion;
import static org.junit.Assert.*;

/**
 * Tests for the GameData object.
 *
 * @author Yevgeny Nyden
 */
public class GameDataTest {

  /** Tests initialization of the default object state. */
  @Test
  public void testDefault() {
    GameData data = new GameData("TestFilePath", "TestDirPath", true);
    assertEquals("Wrong file path", "TestFilePath", data.getFilePath());
    assertEquals("Wrong dir path", "TestDirPath", data.getBundlePath());
    assertTrue("Wrong nativeData", data.isNativeData());
    assertNull("Game name should not be set", data.getGameName());
    assertNotNull("List of categories should not be null", data.getCategories());
    assertEquals("Wrong size of categories list", 0, data.getCategories().size());
    assertNotNull("List of players should not be null", data.getPlayerNames());
    assertEquals("Wrong size of player list", 0, data.getPlayerNames().size());
    assertNotNull("List of bonus questions should not be null", data.getBonusQuestions());
    assertEquals("Wrong size of bonus questions list", 0, data.getBonusQuestions().size());
    assertFalse("Should not have unanswered bonus questions", data.bonusQuestionsHaveBeenAsked());
    assertFalse("Game data should not be usable", data.isGameDataUsable());
    assertFalse("Should not be enough players", data.hasEnoughPlayers());
  }

  /** Tests createCopy. */
  @Test
  public void testCreateCopy() {
    GameData data = new GameData("TestFilePath", "TestDirPath", true);
    List<Category> categories = new ArrayList<>();
    categories.add(createTestCategory("Category 1"));
    categories.add(createTestCategory("Category 2"));
    data.setCategories(categories);
    data.setImageDownloadFailure();

    List<String> playerNames = new ArrayList<>();
    playerNames.add("Abba");
    playerNames.add("Bubba");
    data.setPlayersNames(playerNames);

    List<Question> bonusQuestions = createTestQuestions(3);
    data.setBonusQuestions(bonusQuestions);

    data.setGameName("testName");
    data.setGameDescription("testDescription");
    data.setImageDownloadFailure();

    GameData copyData = data.createCopy();

    assertEquals("Wrong game name", "testName", copyData.getGameName());
    assertEquals("Wrong game description", "testDescription", copyData.getGameDescription());
    assertTrue("Wrong isImageDownloadFailure", copyData.isImageDownloadFailure());

    List<Category> categoriesAfter = copyData.getCategories();
    assertNotNull("List of copied categories should not be null", categoriesAfter);
    assertEquals("Wrong size of categories list", 2, categoriesAfter.size());
    assertEquals("Wrong name", "Category 1", categoriesAfter.get(0).getName());
    assertEquals("Wrong questions count", 3, categoriesAfter.get(0).getQuestionsCount());
    assertEquals("Wrong name", "Category 2", categoriesAfter.get(1).getName());
    assertEquals("Wrong questions count", 3, categoriesAfter.get(1).getQuestionsCount());

    List<String> playerNamesAfter = copyData.getPlayerNames();
    assertNotNull("Player names list is null", playerNamesAfter);
    assertEquals("Wrong player names list size", 2, playerNamesAfter.size());
    assertEquals("Wrong player 0 name", "Abba", playerNamesAfter.get(0));
    assertEquals("Wrong player 1 name", "Bubba", playerNamesAfter.get(1));

    List<Question> questionsAfter = copyData.getBonusQuestions();
    assertNotNull("List of bonus questions should not be null", questionsAfter);
    assertEquals("Wrong size of bonus questions list", 3, questionsAfter.size());
    assertTrue("Bonus questions should not have been asked", data.bonusQuestionsHaveBeenAsked());
  }

  /** Tests copyFrom. */
  @Test
  public void testCopyFrom() {
    GameData data = new GameData("TestFilePath", "TestDirPath", true);
    List<Category> categories = new ArrayList<>();
    categories.add(createTestCategory("Category 1"));
    categories.add(createTestCategory("Category 2"));
    data.setCategories(categories);
    data.setImageDownloadFailure();

    List<String> playerNames = new ArrayList<>();
    playerNames.add("Abba");
    playerNames.add("Bubba");
    data.setPlayersNames(playerNames);

    List<Question> bonusQuestions = createTestQuestions(3);
    data.setBonusQuestions(bonusQuestions);

    data.setGameName("testName");
    data.setGameDescription("testDescription");
    data.setImageDownloadFailure();

    GameData copyData = new GameData("somePath", null, false);
    copyData.copyFrom(data);

    assertEquals("Wrong game name", "testName", copyData.getGameName());
    assertEquals("Wrong game description", "testDescription", copyData.getGameDescription());
    assertTrue("Wrong isImageDownloadFailure", copyData.isImageDownloadFailure());

    List<Category> categoriesAfter = copyData.getCategories();
    assertNotNull("List of copied categories should not be null", categoriesAfter);
    assertEquals("Wrong size of categories list", 2, categoriesAfter.size());
    assertEquals("Wrong name", "Category 1", categoriesAfter.get(0).getName());
    assertEquals("Wrong questions count", 3, categoriesAfter.get(0).getQuestionsCount());
    assertEquals("Wrong name", "Category 2", categoriesAfter.get(1).getName());
    assertEquals("Wrong questions count", 3, categoriesAfter.get(1).getQuestionsCount());

    List<String> playerNamesAfter = copyData.getPlayerNames();
    assertNotNull("Player names list is null", playerNamesAfter);
    assertEquals("Wrong player names list size", 2, playerNamesAfter.size());
    assertEquals("Wrong player 0 name", "Abba", playerNamesAfter.get(0));
    assertEquals("Wrong player 1 name", "Bubba", playerNamesAfter.get(1));

    List<Question> questionsAfter = copyData.getBonusQuestions();
    assertNotNull("List of bonus questions should not be null", questionsAfter);
    assertEquals("Wrong size of bonus questions list", 3, questionsAfter.size());
    assertTrue("Bonus questions should not have been asked", data.bonusQuestionsHaveBeenAsked());
  }

  /** Tests setGameFilePaths. */
  @Test
  public void testSetGameFilePaths() {
    GameData data = new GameData("TestFilePath", "TestDirPath", true);
    assertEquals("Wrong file path", "TestFilePath", data.getFilePath());
    assertEquals("Wrong dir path", "TestDirPath", data.getBundlePath());
    data.setGameFilePaths("TestFilePath2", "TestDirPath2");
    assertEquals("Wrong file path", "TestFilePath2", data.getFilePath());
    assertEquals("Wrong dir path", "TestDirPath2", data.getBundlePath());
  }

  /** Tests setGameName and getGameName. */
  @Test
  public void testSetGameName() {
    GameData data = new GameData("", null, true);
    assertNull(data.getGameName());
    assertTrue("Wrong isChanged", data.setGameName("TestName"));
    assertEquals("Wrong game name", "TestName", data.getGameName());

    assertTrue("Wrong isChanged", data.setGameName("New Name"));
    assertEquals("Wrong game name", "New Name", data.getGameName());
  }

  /** Tests setGameDescription and getGameDescription. */
  @Test
  public void testSetGameDescription() {
    GameData data = new GameData("", null, true);
    assertNull(data.getGameDescription());
    assertTrue("Wrong isChanged", data.setGameDescription("TestDescription"));
    assertEquals("Wrong game description", "TestDescription", data.getGameDescription());

    assertTrue("Wrong isChanged", data.setGameDescription("New Description"));
    assertEquals("Wrong game description", "New Description", data.getGameDescription());
  }

  /** Tests changeToNativeData. */
  @Test
  public void testChangeToNativeData() {
    GameData data = new GameData("", null, false);
    assertFalse("Wrong nativeData", data.isNativeData());
    data.changeToNativeData();
    assertTrue("Wrong nativeData", data.isNativeData());
  }

  /** Tests setCategories. */
  @Test
  public void testSetCategories() {
    GameData data = new GameData("", null, true);
    List<Category> categories = new ArrayList<>();
    categories.add(createTestCategory("Category 1"));
    categories.add(createTestCategory("Category 2"));
    categories.add(createTestCategory("Category 3"));
    data.setCategories(categories);

    List<Category> categoriesAfter = data.getCategories();
    assertNotNull("List of categories should not be null", categoriesAfter);
    assertEquals("Wrong size of categories list", 3, categoriesAfter.size());
    assertEquals("Wrong name", "Category 1", categoriesAfter.get(0).getName());
    assertEquals("Wrong questions count", 3, categoriesAfter.get(0).getQuestionsCount());
    assertEquals("Wrong name", "Category 2", categoriesAfter.get(1).getName());
    assertEquals("Wrong questions count", 3, categoriesAfter.get(1).getQuestionsCount());
    assertEquals("Wrong name", "Category 3", categoriesAfter.get(2).getName());
    assertEquals("Wrong questions count", 3, categoriesAfter.get(2).getQuestionsCount());
  }

  /** Tests removeCategory. */
  @Test
  public void testRemoveCategory() {
    GameData data = new GameData("", null, true);
    List<Category> categories = new ArrayList<>();
    categories.add(createTestCategory("Category 1"));
    categories.add(createTestCategory("Category 2"));
    categories.add(createTestCategory("Category 3"));
    data.setCategories(categories);

    data.removeCategory(1);
    assertEquals("Wrong size of categories list", 2, data.getCategories().size());
    data.removeCategory(0);
    assertEquals("Wrong size of categories list", 1, data.getCategories().size());
    assertEquals("Wrong name", "Category 3", data.getCategories().get(0).getName());
  }

  /** Tests addCategory by adding a category in the middle. */
  @Test
  public void testAddCategoryMiddle() {
    GameData data = new GameData("", null, true);
    List<Category> categories = new ArrayList<>();
    categories.add(createTestCategory("Category 1"));
    categories.add(createTestCategory("Category 2"));
    categories.add(createTestCategory("Category 3"));
    data.setCategories(categories);

    data.addCategory(1, "Bumblebee", "Bah", "Boo");
    assertEquals("Wrong size of categories list", 4, data.getCategories().size());
    assertEquals("Wrong name", "Category 1", data.getCategories().get(0).getName());
    Category category = data.getCategories().get(1);
    assertEquals("Wrong name", "Bumblebee", category.getName());
    assertEquals("Wrong name", "Category 2", data.getCategories().get(2).getName());
    assertEquals("Wrong name", "Category 3", data.getCategories().get(3).getName());
    List<Question> questions = category.getQuestions();
    assertEquals("Wrong new category questions count", 3, questions.size());
    assertQuestion(questions.get(0), "Bah", null, "Boo", null, 0, false);
    assertQuestion(questions.get(1), "Bah", null, "Boo", null, 1, false);
    assertQuestion(questions.get(2), "Bah", null, "Boo", null, 2, false);
  }

  /** Tests addCategory by adding a category at the end. */
  @Test
  public void testAddCategoryEnd() {
    GameData data = new GameData("", null, true);
    List<Category> categories = new ArrayList<>();
    categories.add(createTestCategory("Category 1"));
    categories.add(createTestCategory("Category 2"));
    data.setCategories(categories);

    data.addCategory(1, "Bumblebee", "Bah", "Boo");
    assertEquals("Wrong size of categories list", 3, data.getCategories().size());
    assertEquals("Wrong name", "Category 1", data.getCategories().get(0).getName());
    Category category = data.getCategories().get(1);
    assertEquals("Wrong name", "Bumblebee", category.getName());
    assertEquals("Wrong name", "Category 2", data.getCategories().get(2).getName());
    List<Question> questions = category.getQuestions();
    assertEquals("Wrong new category questions count", 3, questions.size());
    assertQuestion(questions.get(0), "Bah", null, "Boo", null, 0, false);
    assertQuestion(questions.get(1), "Bah", null, "Boo", null, 1, false);
    assertQuestion(questions.get(2), "Bah", null, "Boo", null, 2, false);
  }

  /** Tests updateBonusQuestions and related logic. */
  @Test
  public void testPlayers() {
    GameData data = new GameData("", null, true);
    List<String> playerNames = new ArrayList<>();
    playerNames.add("One");
    playerNames.add("Two");
    assertTrue("Wrong isChanged", data.setPlayersNames(playerNames));

    List<String> playerNamesAfter = data.getPlayerNames();
    assertNotNull("Player names list is null", playerNamesAfter);
    assertEquals("Wrong player names list size", 2, playerNamesAfter.size());
    assertEquals("Wrong player 0 name", "One", playerNamesAfter.get(0));
    assertEquals("Wrong player 1 name", "Two", playerNamesAfter.get(1));

    // Trying to update with the same players
    assertFalse("Wrong isChanged", data.setPlayersNames(playerNames));

    // Removing one player name
    playerNames.remove(1);
    assertTrue("Wrong isChanged", data.setPlayersNames(playerNames));

    // Updating player should reset the previous players list.
    List<String> newPlayerNames = new ArrayList<>();
    newPlayerNames.add("Three");
    assertTrue("Wrong isChanged", data.setPlayersNames(newPlayerNames));
    assertNotNull("Player names list is null", data.getPlayerNames());
    assertEquals("Wrong player names list size", 1, data.getPlayerNames().size());
    assertEquals("Wrong player 0 name", "Three", data.getPlayerNames().get(0));
  }

  /** Tests updateBonusQuestions and related logic. */
  @Test
  public void testBonusQuestions() {
    GameData data = new GameData("", null, true);
    assertFalse("There should be no bonus questions", data.bonusQuestionsHaveBeenAsked());

    List<Question> questions = createTestQuestions(3);
    data.setBonusQuestions(questions);

    List<Question> questionsAfter = data.getBonusQuestions();
    assertNotNull("List of bonus questions should not be null", questionsAfter);
    assertEquals("Wrong size of bonus questions list", 3, questionsAfter.size());
    assertTrue("Bonus questions should not have been asked", data.bonusQuestionsHaveBeenAsked());

    // "Asking" bonus questions.
    questionsAfter.get(0).setHasBeenAsked();
    assertFalse("There should be no bonus questions", data.bonusQuestionsHaveBeenAsked());
    questionsAfter.get(1).setHasBeenAsked();
    assertFalse("There should be no bonus questions", data.bonusQuestionsHaveBeenAsked());
    questionsAfter.get(2).setHasBeenAsked();
    assertFalse("There should be no bonus questions", data.bonusQuestionsHaveBeenAsked());
  }

  /** Tests isGameDataUsable. */
  @Test
  public void testIsGameDataUsable() {
    GameData data = new GameData("", null, true);
    assertFalse("Game data should not be usable", data.isGameDataUsable());
    data.setFileDataAcquired();
    assertFalse("Game data should not be usable", data.isGameDataUsable());
    data.setGameName("TestName");
    assertFalse("Game data should not be usable", data.isGameDataUsable());
    data.setGameDescription("Some description");
    assertFalse("Game data should not be usable", data.isGameDataUsable());
    List<Category> categories = new ArrayList<>();
    categories.add(createTestCategory("Category 1"));
    data.setCategories(categories);
    assertFalse("Game data should not be usable", data.isGameDataUsable());
    categories.add(createTestCategory("Category 2"));
    categories.add(createTestCategory("Category 3"));
    data.setCategories(categories);
    assertTrue("Game data should be usable", data.isGameDataUsable());
  }

  /** Tests isPlayersValid with null argument. */
  @Test
  public void testPlayersValid() {
    GameData data = new GameData("", null, true);
    assertTrue("Players data considered valid with 0 players", data.isPlayersValid(null));
    List<String> players = new ArrayList<>();
    players.add("One");
    data.setPlayersNames(players);
    assertFalse("Players data should not be valid with 1 player", data.isPlayersValid(null));
    players.add("Two");
    players.add("Three");
    data.setPlayersNames(players);
    assertTrue("Players data should be valid", data.isPlayersValid(null));
  }

  /** Tests hasEnoughPlayers. */
  @Test
  public void testHasEnoughPlayers() {
    GameData data = new GameData("", null, true);
    assertFalse("Should not have enough players for a game", data.hasEnoughPlayers());
    List<String> players = new ArrayList<>();
    players.add("One");
    data.setPlayersNames(players);
    assertFalse("Should not have enough players for a game", data.hasEnoughPlayers());
    players.add("Two");
    players.add("Three");
    data.setPlayersNames(players);
    assertTrue("Should have enough players for a game", data.hasEnoughPlayers());
  }

  /** Tests resetGameData. */
  @Test
  public void testReset() {
    // Adding questions and "asking" one.
    GameData data = createMinViableTestGameData();
    List<Category> categories = data.getCategories();
    Question question = categories.get(1).getQuestion(1);
    question.setHasBeenAsked();

    // Adding bonus questions and "aAsking" one.
    List<Question> questions = createTestQuestions(3);
    data.setBonusQuestions(questions);
    questions = data.getBonusQuestions();
    questions.get(1).setHasBeenAsked();

    data.resetGameData();

    categories = data.getCategories();
    question = categories.get(1).getQuestion(1);
    assertFalse("Question should not have been asked", question.isHasBeenAsked());

    questions = data.getBonusQuestions();
    assertFalse("Bonus question should not have been asked", questions.get(1).isHasBeenAsked());
  }

  /** Tests getGameSizeIconLarge. */
  @Test
  public void testGetGameSizeIconLarge() {
    GameData data1 = new GameData("dosntmatter", null, true);
    ImageEnum imageEnum1 = data1.getGameSizeIconLarge();
    assertNotNull("Image enum 1 is null", imageEnum1);

    GameData data2 = createMinViableTestGameData();
    ImageEnum imageEnum2 = data2.getGameSizeIconLarge();
    assertNotNull("Image enum 2 is null", imageEnum2);
  }

  /** Tests getGameSizeIconSmall. */
  @Test
  public void testGetGameSizeIconSmall() {
    GameData data1 = new GameData("dosntmatter", null, true);
    ImageEnum imageEnum1 = data1.getGameSizeIconSmall();
    assertNotNull("Image enum 1 is null", imageEnum1);

    GameData data2 = createMinViableTestGameData();
    ImageEnum imageEnum2 = data2.getGameSizeIconSmall();
    assertNotNull("Image enum 2 is null", imageEnum2);
  }

  /** Tests getGameSizeText. */
  @Test
  public void testGetGameSizeText() {
    GameData data1 = new GameData("dosntmatter", null, true);
    String text1 = data1.getGameSizeText();
    assertNotNull("Size string 1 is null", text1);
    assertTrue("Size string 1 is too short", text1.length() > 5);

    GameData data2 = createMinViableTestGameData();
    String text2 = data2.getGameSizeText();
    assertNotNull("Size string 2 is null", text2);
    assertTrue("Size string 2 is too short", text2.length() > 5);
  }

  /** Tests getGameDimensionShortText. */
  @Test
  public void testGetGameDimensionShortText() {
    GameData data1 = new GameData("dosntmatter", null, true);
    String text1 = data1.getGameDimensionShortText();
    assertEquals("Dimension string 1 is wrong", "0x0", text1);

    GameData data2 = createMinViableTestGameData();
    String text2 = data2.getGameDimensionShortText();
    assertEquals("Dimension string 2 is wrong", "3x3", text2);
  }

  /** Tests getGameDimensionLongMessage. */
  @Test
  public void testGetGameDimensionLongMessage() {
    GameData data1 = new GameData("dosntmatter", null, true);
    String text1 = data1.getGameDimensionLongMessage();
    assertNotNull("Dimension string 1 is null", text1);
    assertTrue("Dimension string 1 is too short", text1.length() > 5);

    GameData data2 = createMinViableTestGameData();
    String text2 = data2.getGameDimensionLongMessage();
    assertNotNull("Dimension string 2 is null", text2);
    assertTrue("Dimension string 2 is too short", text2.length() > 5);
  }

  /** Tests getGameEstimatedLengthMessage. */
  @Test
  public void testGetGameEstimatedLengthMessage() {
    GameData data1 = new GameData("dosntmatter", null, true);
    String text1 = data1.getGameEstimatedLengthMessage();
    assertNotNull("Game duration string 1 is null", text1);
    assertTrue("Game duration string 1 is too short", text1.length() > 5);

    GameData data2 = createMinViableTestGameData();
    String text2 = data2.getGameEstimatedLengthMessage();
    assertNotNull("Game duration string 2 is null", text2);
    assertTrue("Game duration string 2 is too short", text2.length() > 5);
  }

  /** Tests generateFileParsingResult with empty data. */
  @Test
  public void testGenerateFileParsingResultWithEmptyData() {
    GameData data = new GameData("", null, true);
    FileParsingResult result = data.generateFileParsingResult();
    assertResultMessageNumbers(result, 1, 0, 0);
    assertFalse("Short result message should not be blank", StringUtils.isBlank(result.getResulTitleShort()));
    assertFalse("Long result message should not be blank", StringUtils.isBlank(result.getResulTitleLong()));
  }

  /** Tests generateFileParsingResult with a valid data. */
  @Test
  public void testGenerateFileParsingResultWithMinValid() {
    GameData data = createMinViableTestGameData();
    FileParsingResult result = data.generateFileParsingResult();
    assertResultMessageNumbers(result, 0, 0, 3);
    assertThreeInfoMessages(result.getInfoMessages(), 9, 3, 0, 0);
  }

  /** Tests generateFileParsingResult with valid data and players. */
  @Test
  public void testGenerateFileParsingResultWithValidWithPlayer() {
    GameData data = createMinViableTestGameData();
    data.setPlayersNames(createTestPlayerNames(3));
    FileParsingResult result = data.generateFileParsingResult();
    assertResultMessageNumbers(result, 0, 0, 3);
    assertThreeInfoMessages(result.getInfoMessages(), 9, 3, 3, 0);
  }

  /** Tests generateFileParsingResult with valid data and players and bonus questions. */
  @Test
  public void testGenerateFileParsingResultWithValidWithPlayerAndBonusQuestions() {
    GameData data = createMinViableTestGameData();
    data.setPlayersNames(createTestPlayerNames(3));
    data.setBonusQuestions(createTestQuestions(6));
    FileParsingResult result = data.generateFileParsingResult();
    assertResultMessageNumbers(result, 0, 0, 3);
    assertThreeInfoMessages(result.getInfoMessages(), 9, 3, 3, 6);
  }

  /** Tests generateFileParsingResult with valid data and not enough players. */
  @Test
  public void testGenerateFileParsingResultWithValidNotEnoughPlayers() {
    GameData data = createMinViableTestGameData();
    data.setPlayersNames(createTestPlayerNames(1));
    FileParsingResult result = data.generateFileParsingResult();
    assertResultMessageNumbers(result, 0, 1, 2);
  }

  /** Tests generateFileParsingResult with valid data and too many players. */
  @Test
  public void testGenerateFileParsingResultWithValidTooManyPlayers() {
    GameData data = createMinViableTestGameData();
    data.setPlayersNames(createTestPlayerNames(10));
    FileParsingResult result = data.generateFileParsingResult();
    assertResultMessageNumbers(result, 0, 1, 3);
  }

  /** Tests generateFileParsingResult with valid data and not enough bonus questions. */
  @Test
  public void testGenerateFileParsingResultWithValidNotEnoughBonusQuestions() {
    GameData data = createMinViableTestGameData();
    data.setBonusQuestions(createTestQuestions(1));
    FileParsingResult result = data.generateFileParsingResult();
    assertResultMessageNumbers(result, 0, 1, 2);
  }

  /** Tests generateFileParsingResult with valid data and too many bonus questions. */
  @Test
  public void testGenerateFileParsingResultWithValidTooManyBonusQuestions() {
    GameData data = createMinViableTestGameData();
    data.setBonusQuestions(createTestQuestions(30));
    FileParsingResult result = data.generateFileParsingResult();
    assertResultMessageNumbers(result, 0, 1, 3);
  }

  /** Tests ensureMaxCategoriesAndQuestions. */
  @Test
  public void testEnsureMaxCategoriesAndQuestions() {
    GameData data = new GameData("filepathdoesntmatter", null, true);
    data.setGameName("TestName");
    List<Category> categories = new ArrayList<>();
    categories.add(new Category("Category 1", createTestQuestions(15)));
    categories.add(new Category("Category 2", createTestQuestions(15)));
    categories.add(new Category("Category 3", createTestQuestions(15)));
    categories.add(new Category("Category 4", createTestQuestions(15)));
    categories.add(new Category("Category 5", createTestQuestions(15)));
    categories.add(new Category("Category 6", createTestQuestions(15)));
    categories.add(new Category("Category 7", createTestQuestions(15)));
    categories.add(new Category("Category 8", createTestQuestions(15)));
    categories.add(new Category("Category 9", createTestQuestions(15)));
    categories.add(new Category("Category 10", createTestQuestions(15)));
    categories.add(new Category("Category 11", createTestQuestions(15)));
    categories.add(new Category("Category 12", createTestQuestions(15)));
    categories.add(new Category("Category 13", createTestQuestions(15)));
    data.setCategories(categories);

    assertEquals("Wrong categories count", 13, data.getCategoriesCount());
    assertEquals("Wrong questions count", 15, data.getCategoryQuestionsCount());

    data.ensureMaxCategoriesAndQuestions();
    assertEquals("Wrong categories count", JjDefaults.MAX_NUMBER_OF_CATEGORIES, data.getCategoriesCount());
    assertEquals("Wrong questions count", JjDefaults.MAX_NUMBER_OF_QUESTIONS, data.getCategoryQuestionsCount());
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
    assertBonusQuestionsInfoMessage(infoMessages.get(1), bonusQuestionsCount);
    assertPlayersInfoMessage(infoMessages.get(2), playersCount);
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
            String.valueOf(questionsCount), String.valueOf(categoriesCount)),
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
   * Creates minimally viable test game data.
   * @return game data to test
   */
  private static GameData createMinViableTestGameData() {
    GameData data = new GameData("filepathdoesntmatter", null, true);
    data.setGameName("TestName");
    List<Category> categories = new ArrayList<>();
    categories.add(createTestCategory("Category 1"));
    categories.add(createTestCategory("Category 2"));
    categories.add(createTestCategory("Category 3"));
    data.setCategories(categories);
    data.setFileDataAcquired();
    return data;
  }

  /**
   * Creates a test category object.
   * @param name category name
   * @return a new category object
   */
  private static Category createTestCategory(String name) {
    return new Category(name, createTestQuestions(3));
  }

  /**
   * Creates test questions list.
   * @param count number of questions to create
   * @return a list of test questions.
   */
  private static List<Question> createTestQuestions(int count) {
    List<Question> questions = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      questions.add(new Question("", null, "", null, i));
    }
    return questions;
  }

  /**
   * Creates test player names list.
   * @param count number player names to create
   * @return player names list
   */
  private List<String> createTestPlayerNames(int count) {
    List<String> playerNames = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      playerNames.add(String.valueOf(i + 1));
    }
    return playerNames;
  }
}
