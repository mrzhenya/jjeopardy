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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
    GameData data = new GameData();
    assertNull("Game name should not be set", data.getGameName());
    assertNotNull("List of categories should not be null", data.getCategories());
    assertEquals("Wrong size of categories list", 0, data.getCategories().size());
    assertNotNull("List of players should not be null", data.getPlayerNames());
    assertEquals("Wrong size of player list", 0, data.getPlayerNames().size());
    assertNotNull("List of bonus questions should not be null", data.getBonusQuestions());
    assertEquals("Wrong size of bonus questions list", 0, data.getBonusQuestions().size());
    assertFalse("Should not have unanswered bonus questions", data.bonusQuestionsHaveBeenAsked());
  }

  /** Tests updateGameData. */
  @Test
  public void testUpdateGameData() {
    GameData data = new GameData();
    List<Category> categories = new ArrayList<>();
    categories.add(createTestCategory("Category 1"));
    categories.add(createTestCategory("Category 2"));
    categories.add(createTestCategory("Category 3"));
    data.updateGameData("TestName", categories);

    List<Category> categoriesAfter = data.getCategories();
    assertNotNull("List of categories should not be null", categoriesAfter);
    assertEquals("Wrong size of categories list", 3, categoriesAfter.size());
    assertEquals("Wrong name", "Category 1", categoriesAfter.get(0).getName());
    assertEquals("Wrong questions count", 3, categoriesAfter.get(0).getQuestionsCount());
    assertEquals("Wrong name", "Category 2", categoriesAfter.get(1).getName());
    assertEquals("Wrong questions count", 3, categoriesAfter.get(1).getQuestionsCount());
    assertEquals("Wrong name", "Category 3", categoriesAfter.get(2).getName());
    assertEquals("Wrong questions count", 3, categoriesAfter.get(2).getQuestionsCount());

    assertEquals("Wrong game name", "TestName", data.getGameName());
  }

  /** Tests updateBonusQuestions and related logic. */
  @Test
  public void testPlayers() {
    GameData data = new GameData();
    List<String> playerNames = new ArrayList<>();
    playerNames.add("One");
    playerNames.add("Two");
    data.updatePlayersNames(playerNames);

    List<String> playerNamesAfter = data.getPlayerNames();
    assertNotNull("Player names list is null", playerNamesAfter);
    assertEquals("Wrong player names list size", 2, playerNamesAfter.size());
    assertEquals("Wrong player 0 name", "One", playerNamesAfter.get(0));
    assertEquals("Wrong player 1 name", "Two", playerNamesAfter.get(1));

    // Updating player should reset the previous players list.
    List<String> newPlayerNames = new ArrayList<>();
    newPlayerNames.add("Three");
    data.updatePlayersNames(newPlayerNames);
    assertNotNull("Player names list is null", data.getPlayerNames());
    assertEquals("Wrong player names list size", 1, data.getPlayerNames().size());
    assertEquals("Wrong player 0 name", "Three", data.getPlayerNames().get(0));
  }

  /** Tests updateBonusQuestions and related logic. */
  @Test
  public void testBonusQuestions() {
    GameData data = new GameData();
    assertFalse("There should be no bonus questions", data.bonusQuestionsHaveBeenAsked());

    List<Question> questions = createTestQuestions();
    data.updateBonusQuestions(questions);

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

  /** Tests resetGameData. */
  @Test
  public void testReset() {
    GameData data = new GameData();

    // Adding questions and "asking" one.
    List<Category> categories = new ArrayList<>();
    categories.add(createTestCategory("Category 1"));
    categories.add(createTestCategory("Category 2"));
    categories.add(createTestCategory("Category 3"));
    data.updateGameData("TestName", categories);
    List<Category> categoriesAfter = data.getCategories();
    Question question = categoriesAfter.get(1).getQuestion(1);
    question.setHasBeenAsked();

    // Adding bonus questions and "aAsking" one.
    List<Question> questions = createTestQuestions();
    data.updateBonusQuestions(questions);
    questions = data.getBonusQuestions();
    questions.get(1).setHasBeenAsked();

    data.resetGameData();

    categoriesAfter = data.getCategories();
    question = categoriesAfter.get(1).getQuestion(1);
    assertFalse("Question should not have been asked", question.isHasBeenAsked());

    questions = data.getBonusQuestions();
    assertFalse("Bonus question should not have been asked", questions.get(1).isHasBeenAsked());
  }

  /**
   * Creates a test category object.
   * @param name category name
   * @return a new category object
   */
  private static Category createTestCategory(String name) {
    return new Category(name, createTestQuestions());
  }

  /**
   * Creates test questions list.
   * @return a list of test questions.
   */
  private static List<Question> createTestQuestions() {
    List<Question> questions = new ArrayList<>();
    questions.add(new Question("", "", 0));
    questions.add(new Question("", "", 0));
    questions.add(new Question("", "", 0));
    return questions;
  }
}
