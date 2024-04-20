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
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for the Category object.
 *
 * @author Yevgeny Nyden
 */
public class CategoryTest {

  /**
   * Tests createCopy;
   */
  @Test
  public void testCreateCopy() {
    List<Question> questions = new ArrayList<>();
    Question question = new Question(
        "Question 1", "question.jpg", "Answer 1", "answer.jpg",11);
    questions.add(question);
    Category category = new Category("Bum", questions);

    Category copyCategory = category.createCopy();

    assertEquals("Wrong category name", "Bum", copyCategory.getName());
    assertEquals("Wrong number of questions", 1, copyCategory.getQuestionsCount());
    assertNotNull("Questions should not be null", copyCategory.getQuestions());
    assertEquals("Wrong number of questions", 1, copyCategory.getQuestions().size());
    Question copyQuestion = copyCategory.getQuestion(0);
    assertEquals("Wrong question text", "Question 1", copyQuestion.getQuestion());
    assertEquals("Wrong question answer", "Answer 1", copyQuestion.getAnswer());
    assertEquals("Wrong question image", "question.jpg", copyQuestion.getQuestionImage());
    assertEquals("Wrong answer image", "answer.jpg", copyQuestion.getAnswerImage());
    assertEquals("Wrong question points", 11, copyQuestion.getPoints());
    assertNotNull("Parent name should not be blank", question.getParentName());
    assertTrue("Wrong parent question name", question.getParentName().contains("Bum"));

    // Now change the original question and check the copy doesn't change.
    question.setQuestion("New Question");
    question.setAnswer("New answer");
    question.setQuestionImage("newQuestion.jpg");
    question.setAnswerImage("newAnswer.jpg");
    question.setPoints(555);

    assertEquals("Wrong question text", "Question 1", copyQuestion.getQuestion());
    assertEquals("Wrong question answer", "Answer 1", copyQuestion.getAnswer());
    assertEquals("Wrong question image", "question.jpg", copyQuestion.getQuestionImage());
    assertEquals("Wrong answer image", "answer.jpg", copyQuestion.getAnswerImage());
    assertEquals("Wrong question points", 11, copyQuestion.getPoints());
  }

  /** Tests ctor and the getter methods. */
  @Test
  public void testGetters() {
    List<Question> questions = new ArrayList<>();
    Question question = new Question(
        "Question 1", null, "Answer 1", null,11);
    questions.add(question);
    Category category = new Category("Bum", questions);

    assertEquals("Wrong category name", "Bum", category.getName());
    assertFalse("Category name string should not be blank",
        StringUtils.isBlank(category.getNameString()));
    assertTrue("Category name is not found in category string", category.getNameString().contains("Bum"));
    assertEquals("Wrong number of questions", 1, category.getQuestionsCount());
    assertEquals("Wrong question", question, category.getQuestion(0));

    assertNotNull("Questions should not be null", category.getQuestions());
    assertEquals("Wrong number of questions", 1, category.getQuestions().size());

    assertNotNull("Parent name should not be blank", question.getParentName());
    assertTrue("Wrong parent question name", question.getParentName().contains("Bum"));
  }

  /** Tests the setName method. */
  @Test
  public void testSetName() {
    Category category = new Category("Bum", new ArrayList<>());
    assertEquals("Wrong category name", "Bum", category.getName());
    assertFalse("Wrong isChanged value", category.setName("Bum"));
    assertEquals("Wrong category name", "Bum", category.getName());
    assertTrue("Wrong isChanged value", category.setName("Zoom"));
    assertEquals("Wrong category name", "Zoom", category.getName());
  }

  /** Tests ensureMaxQuestionsCount. */
  @Test
  public void testEnsureMaxQuestionsCount() {
    List<Question> questions = new ArrayList<>();
    questions.add(new Question("a", null, "a", null, 1));
    questions.add(new Question("a", null, "a", null, 1));
    questions.add(new Question("a", null, "a", null, 1));
    questions.add(new Question("a", null, "a", null, 1));
    questions.add(new Question("a", null, "a", null, 1));
    questions.add(new Question("a", null, "a", null, 1));
    questions.add(new Question("a", null, "a", null, 1));
    questions.add(new Question("a", null, "a", null, 1));
    questions.add(new Question("a", null, "a", null, 1));
    questions.add(new Question("a", null, "a", null, 1));
    questions.add(new Question("a", null, "a", null, 1));
    questions.add(new Question("a", null, "a", null, 1));
    questions.add(new Question("a", null, "a", null, 1));
    Category category = new Category("Bum", questions);
    assertEquals("Wrong number of questions", 13, category.getQuestionsCount());
    assertEquals("Wrong number of questions", 13, category.getQuestions().size());

    category.ensureMaxQuestionsCount();
    assertEquals("Wrong number of questions", JjDefaults.MAX_NUMBER_OF_QUESTIONS, category.getQuestionsCount());
  }
}
