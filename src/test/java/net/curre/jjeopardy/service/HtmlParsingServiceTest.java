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
import net.curre.jjeopardy.bean.QuestionTest;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static net.curre.jjeopardy.service.HtmlParsingService.MAX_DESCRIPTION_LENGTH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for HtmlParsingService.
 *
 * @author Yevgeny Nyden
 */
public class HtmlParsingServiceTest {

  /** Path to test data directory. */
  protected static final String TEST_DIR_PATH = "src/test/resources/jlabs/";

  /** Reference to the HTML parsing service. */
  private HtmlParsingService htmlParser;

  /**
   * Initializes the state before each test run.
   */
  @Before
  public void init() {
    this.htmlParser = new HtmlParsingService();
  }

  /**
   * Tests parseJeopardyLabsHtmlFile with valid data.
   */
  @Test
  public void testParseJeopardyLabsHtmlFileWithValidData() {
    GameData testData = this.htmlParser.parseJeopardyLabsHtmlFile(TEST_DIR_PATH + "valid-simple.html");
    assertNotNull("Game data is null", testData);
    assertEquals("Wrong game name", "Test Jlabs Game", testData.getGameName());
    assertEquals("Wrong game description", "Test Jlabs Description", testData.getGameDescription());

    List<Category> categories = testData.getCategories();
    assertEquals("Wrong number of categories", 2, categories.size());
    Category category1 = categories.get(0);
    assertEquals("Wrong category 1 name", "Christmas songs", category1.getName());
    assertEquals("Wrong number of questions in category 1", 2, category1.getQuestionsCount());
    QuestionTest.assertQuestion(
        category1.getQuestion(0),
        "Which song? \"...open sleigh\"", null,"Jingle Bells", null, 100, false);
    QuestionTest.assertQuestion(
        category1.getQuestion(1),
        "Finish! \"I'm ______\"", null, "white", null, 200, false);

    Category category2 = categories.get(1);
    assertEquals("Wrong category 2 name", "Christmas Traditions", category2.getName());
    assertEquals("Wrong number of questions in category 2", 2, category2.getQuestionsCount());
    QuestionTest.assertQuestion(
        category2.getQuestion(0),
        "What food?", null, "Cookies", null, 100, false);
    QuestionTest.assertQuestion(
        category2.getQuestion(1),
        "How many days?", null, "24", null, 200, false);
  }

  /**
   * Tests parseJeopardyLabsHtmlFile with valid large data.
   */
  @Test
  public void testParseJeopardyLabsHtmlFileLarge() {
    GameData testData = this.htmlParser.parseJeopardyLabsHtmlFile(
        TEST_DIR_PATH + "christmas-jeopardy-20251.html");
    assertNotNull("Game data is null", testData);
    assertEquals("Wrong game name", "Christmas Jeopardy Jeopardy Template", testData.getGameName());
    assertNotNull("Game description should not be null", testData.getGameDescription());
    assertEquals("Wrong description length", MAX_DESCRIPTION_LENGTH, testData.getGameDescription().length());

    List<Category> categories = testData.getCategories();
    assertEquals("Wrong number of categories", 7, categories.size());
    for (int ind = 0; ind < categories.size(); ind++) {
      Category category = categories.get(ind);
      assertEquals("Wrong number of questions in category " + ind, 10, category.getQuestionsCount());
      int points = 0;
      for (int i = 0; i < category.getQuestionsCount(); i++) {
        points += 100;
        assertEquals("Wrong question " + i + " points value", points, category.getQuestion(i).getPoints());
      }
    }
  }

  /**
   * Tests parseJeopardyLabsHtmlFile with valid data and with values having lots
   * of leading and trailing whitespace.
   */
  @Test
  public void testParseJeopardyLabsHtmlFileLotsWhitespace() {
    GameData testData = this.htmlParser.parseJeopardyLabsHtmlFile(
        TEST_DIR_PATH + "lots-of-whitespace.html");
    assertNotNull("Game data is null", testData);
    assertEquals("Wrong game name", "Test Jlabs Game", testData.getGameName());
    assertEquals("Wrong game description", "Test Jlabs Description", testData.getGameDescription());

    List<Category> categories = testData.getCategories();
    assertEquals("Wrong number of categories", 1, categories.size());
    Category category1 = categories.get(0);
    assertEquals("Wrong category 1 name", "Christmas songs", category1.getName());
    assertEquals("Wrong number of questions in category 1", 2, category1.getQuestionsCount());
    QuestionTest.assertQuestion(
        category1.getQuestion(0),
        "Which song? \"...open sleigh\"", null,
        "Jingle Bells", null,100, false);
    QuestionTest.assertQuestion(
        category1.getQuestion(1),
        "Finish! \"I'm ______\"", null,
        "white", null,200, false);
  }

  /**
   * Tests parseJeopardyLabsHtmlFile with valid data and with images in questions and answers.
   */
  @Test
  public void testParseJeopardyLabsHtmlFileWithImages() {
    GameData testData = this.htmlParser.parseJeopardyLabsHtmlFile(TEST_DIR_PATH + "with-images.html");
    assertNotNull("Game data is null", testData);
    assertEquals("Wrong game name", "School School School!", testData.getGameName());

    List<Category> categories = testData.getCategories();
    assertEquals("Wrong number of categories", 2, categories.size());
    Category category1 = categories.get(0);
    assertEquals("Wrong category 1 name", "RANDOM 1", category1.getName());
    assertEquals("Wrong number of questions in category 1", 1, category1.getQuestionsCount());
    QuestionTest.assertQuestion(
        category1.getQuestion(0),
        "What do you use to cut paper?", null, "We use scissors.",
        "https://th.bing.com/th/id/OIP.vXth1ZgAdr5AUS5Cbw-65QHaHZ?pid=ImgDet&rs=1",
        100, false);

    Category category2 = categories.get(1);
    assertEquals("Wrong category 2 name", "Math Facts", category2.getName());
    assertEquals("Wrong number of questions in category 2", 1, category2.getQuestionsCount());
    QuestionTest.assertQuestion(
        category2.getQuestion(0),
        "17+_=62", "https://clipartspub.com/images/math-clipart-clear-background.png",
        "45","https://clipartcraft.com/images/math-clipart-8.png",
        100, false);
  }
}
