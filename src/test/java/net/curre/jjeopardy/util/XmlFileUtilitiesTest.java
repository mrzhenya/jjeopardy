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

import net.curre.jjeopardy.bean.Category;
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.bean.Question;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests for XmlFileUtilities.
 * @author Yevgeny Nyden
 */
public class XmlFileUtilitiesTest {

  /** Path to the test settings directory. */
  private static final String TEST_PATH = "target/test/xmlutils/";

  /** Test file name. */
  private static final String TEST_FILENAME = "test.xml";

  /** Test file path. */
  private static final String TEST_FILEPATH = TEST_PATH + TEST_FILENAME;

  /** Initializes the state before each test run. */
  @Before
  public void init() {
    File testDir = new File(TEST_PATH);
    testDir.mkdirs();
    File testFile = new File(TEST_FILEPATH);
    if (testFile.exists()) {
      testFile.delete();
    }
  }

  /** Tests sanity of the property name methods. */
  @Test
  public void testPropertyNameMethod() {
    assertPropertyNameSane(XmlFileUtilities.getPropertyCategoryName(3), "3");
    assertPropertyNameSane(XmlFileUtilities.getPropertyQuestionPoints(5), "5");
    assertPropertyNameSane(XmlFileUtilities.getPropertyQuestionText(4, 5), "5");
    assertPropertyNameSane(XmlFileUtilities.getPropertyQuestionImage(6, 7), "7");
    assertPropertyNameSane(XmlFileUtilities.getPropertyAnswerText(2, 3), "2");
    assertPropertyNameSane(XmlFileUtilities.getPropertyPlayerName(5), "5");
    assertPropertyNameSane(XmlFileUtilities.getPropertyBonusQuestionText(7), "7");
    assertPropertyNameSane(XmlFileUtilities.getPropertyBonusAnswerText(8), "8");
    assertPropertyNameSane(XmlFileUtilities.getPropertyBonusQuestionImage(4), "4");
  }

  /** Tests creating a game file. */
  @Test
  public void testCreateGameFile() throws IOException {
    // Creating a small game data and trying to persist it.
    GameData testData = new GameData("doesntmatter", null, false);

    testData.setGameName("testGameName");
    testData.setGameDescription("testGameDescription");

    List<Category> categories = new ArrayList<>();
    List<Question> questions = new ArrayList<>();
    questions.add(new Question(
        "SayWhat111", "testImage111",
        "TheAnswer111", "testImage000", 111));
    Category category = new Category("testCategoryName", questions);
    categories.add(category);
    testData.setCategories(categories);

    List<String> playerNames = new ArrayList<>();
    playerNames.add("Player1");
    playerNames.add("Player2");
    testData.setPlayersNames(playerNames);

    List<Question> bonusQuestions = new ArrayList<>();
    bonusQuestions.add(new Question(
        "BonusWhat", "bonusImage1",
        "bonusAnswer", "bonusImage2", 222));
    testData.setBonusQuestions(bonusQuestions);

    XmlFileUtilities.createGameFile(testData, TEST_FILEPATH);

    // Verifying the file data - just basic validation that test string are found in the file.
    File testFile = new File(TEST_FILEPATH);
    assertTrue("Test file does not exist", testFile.exists());
    String data = FileUtils.readFileToString(testFile, "UTF-8");
    assertTrue("Game name is not found", data.contains("testGameName"));
    assertTrue("Game description is not found", data.contains("testGameDescription"));
    assertTrue("Question points value is not found", data.contains("111"));
    assertTrue("Question test is not found", data.contains("SayWhat111"));
    assertTrue("question image is not found", data.contains("testImage111"));
    assertTrue("Question answer is not found", data.contains("TheAnswer111"));
    assertTrue("Category name is not found", data.contains("testCategoryName"));
    assertTrue("Player 1 is not found", data.contains("Player1"));
    assertTrue("Player 2 is not found", data.contains("Player2"));
    assertTrue("Bonus question text is not found", data.contains("BonusWhat"));
    assertTrue("Bonus question image is not found", data.contains("bonusImage"));
    assertTrue("Bonus question answer is not found", data.contains("bonusAnswer"));
    assertTrue("Bonus question points value is not found", data.contains("222"));
  }

  /** Basic test of the GameEntry. */
  @Test
  public void testGameEntry() {
    XmlFileUtilities.GameEntry name = new XmlFileUtilities.GameEntry(XmlFileUtilities.PROPERTY_NAME, "");
    XmlFileUtilities.GameEntry description = new XmlFileUtilities.GameEntry(XmlFileUtilities.PROPERTY_DESCRIPTION, "");
    XmlFileUtilities.GameEntry points1 = new XmlFileUtilities.GameEntry("question.1.points", "");
    XmlFileUtilities.GameEntry points2 = new XmlFileUtilities.GameEntry("question.2.points", "");
    XmlFileUtilities.GameEntry question = new XmlFileUtilities.GameEntry("category.1.question.1", "");
    assertTrue("Wrong comparison result", name.compareTo(description) < 0);
    assertTrue("Wrong comparison result", description.compareTo(name) > 0);
    assertTrue("Wrong comparison result", points1.compareTo(name) > 0);
    assertTrue("Wrong comparison result", points2.compareTo(points1) > 0);
    assertTrue("Wrong comparison result", description.compareTo(points1) < 0);
    assertTrue("Wrong comparison result", question.compareTo(points1) > 0);
    assertTrue("Wrong comparison result", question.compareTo(description) > 0);
    assertTrue("Wrong comparison result", question.compareTo(name) > 0);
  }

  /** Basic test of the GameProperties. */
  @Test
  public void testGameProperties() {
    XmlFileUtilities.GameProperties properties = new XmlFileUtilities.GameProperties();
    properties.setProperty("question.3.points", "");
    properties.setProperty(XmlFileUtilities.PROPERTY_DESCRIPTION, "");
    properties.setProperty("category.2.question.1", "");
    properties.setProperty("category.2.answer.1", "");
    properties.setProperty("category.2.name", "");
    properties.setProperty("category.2.answer.1.img", "");
    properties.setProperty(XmlFileUtilities.PROPERTY_NAME, "");

    Set<Map.Entry<Object, Object>> entrySet = properties.entrySet();
    assertNotNull("Entry set is null", entrySet);
    Iterator<Map.Entry<Object, Object>> it = entrySet.iterator();
    assertEquals("Wrong order", XmlFileUtilities.PROPERTY_NAME, it.next().getKey());
    assertEquals("Wrong order", XmlFileUtilities.PROPERTY_DESCRIPTION, it.next().getKey());
    assertEquals("Wrong order", "question.3.points", it.next().getKey());
    assertEquals("Wrong order", "category.2.name", it.next().getKey());
    assertEquals("Wrong order", "category.2.question.1", it.next().getKey());
    assertEquals("Wrong order", "category.2.answer.1", it.next().getKey());
    assertEquals("Wrong order", "category.2.answer.1.img", it.next().getKey());
  }

  /**
   * Asserts the sanity of a property name string, which is - not null and containing a key string.
   * @param propertyName property name string to test
   * @param key some value expected to be in the property name
   */
  private void assertPropertyNameSane(String propertyName, String key) {
    assertNotNull("Property name should not be null", propertyName);
    assertTrue("Key is not found in property name", propertyName.contains(key));
  }
}
