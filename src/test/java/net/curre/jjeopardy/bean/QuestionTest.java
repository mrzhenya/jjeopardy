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

import static org.junit.Assert.*;

/**
 * Tests the Question object.
 *
 * @author Yevgeny Nyden
 */
public class QuestionTest {

  /** Tests Ctor and the getter methods. */
  @Test
  public void testDefault() {
    Question question = new Question("Question 1", "Answer 1", 11);
    assertEquals("Wrong question", "Question 1", question.getQuestion());
    assertEquals("Wrong answer", "Answer 1", question.getAnswer());
    assertEquals("Wrong points", 11, question.getPoints());
    assertNull("Paren name should be null", question.getParentName());
    assertFalse("Question should not be asked yet", question.isHasBeenAsked());
  }

  /** Tests hasBeenAsked methods. */
  @Test
  public void testHasBeenAsked() {
    Question question = new Question("", "", 0);
    assertFalse("Question should not be asked yet", question.isHasBeenAsked());

    question.resetHasBeenAsked();
    assertFalse("Question should not be asked", question.isHasBeenAsked());

    question.setHasBeenAsked();
    assertTrue("Question should be asked", question.isHasBeenAsked());

    question.resetHasBeenAsked();
    assertFalse("Question should not be asked", question.isHasBeenAsked());
  }

  /** Tests parentWithName methods. */
  @Test
  public void testParentWithName() {
    Question question = new Question("", "", 0);
    assertNull("Paren name should be null", question.getParentName());
    HasName parent = () -> "TestParent";
    question.setParentWithName(parent);
    assertEquals("Wrong parent name", "TestParent", question.getParentName());
  }
}
