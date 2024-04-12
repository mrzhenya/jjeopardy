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

  /** Tests ctor and getter methods. */
  @Test
  public void testAll() {
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
