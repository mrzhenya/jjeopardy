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
    Question question = new Question(
        "Question 1", "IMG1","Answer 1", "IMG2", 11);
    assertQuestion(question, "Question 1", "IMG1",
        "Answer 1", "IMG2", 11, false);
    assertNull("Parent name should be null", question.getParentName());
  }

  /** Tests setter methods. */
  @Test
  public void testSetters() {
    Question question = new Question(
        "AAA", "IMG1","XXX", "IMG2", 11);
    assertQuestion(question, "AAA", "IMG1",
        "XXX", "IMG2", 11, false);

    assertFalse("Wrong isChanged question value", question.setQuestion("AAA"));
    assertTrue("Wrong isChanged question value", question.setQuestion("BBB"));
    assertEquals("Wrong new question value", "BBB", question.getQuestion());

    assertFalse("Wrong isChanged question image value", question.setQuestionImage("IMG1"));
    assertTrue("Wrong isChanged question image value", question.setQuestionImage("IMG100"));
    assertEquals("Wrong new question image value", "IMG100", question.getQuestionImage());

    assertFalse("Wrong isChanged answer value", question.setAnswer("XXX"));
    assertTrue("Wrong isChanged answer value", question.setAnswer("YYY"));
    assertEquals("Wrong new answer value", "YYY", question.getAnswer());

    assertFalse("Wrong isChanged answer image value", question.setAnswerImage("IMG2"));
    assertTrue("Wrong isChanged answer image value", question.setAnswerImage("IMG200"));
    assertEquals("Wrong new answer image value", "IMG200", question.getAnswerImage());

    question.setPoints(333);
    assertEquals("Wrong new points value", 333, question.getPoints());
  }

  /** Tests question/answer image related code. */
  @Test
  public void testImageMethods() {
    Question question = new Question(
        "Question 1", "question-image","Answer 1", "answer-image", 11);
    assertEquals("Wrong question", "Question 1", question.getQuestion());
    assertEquals("Wrong question image", "question-image", question.getQuestionImage());
    assertEquals("Wrong answer image", "answer-image", question.getAnswerImage());

    question.setQuestionImage("");
    assertNull("Question image should be null", question.getQuestionImage());
    question.setAnswerImage("  ");
    assertNull("Answer image should be null", question.getAnswerImage());

    question.setQuestionImage("question-image-2");
    assertEquals("Wrong question image", "question-image-2", question.getQuestionImage());
    question.setAnswerImage("answer-image-2");
    assertEquals("Wrong answer image", "answer-image-2", question.getAnswerImage());
  }

  /** Tests hasBeenAsked methods. */
  @Test
  public void testHasBeenAsked() {
    Question question = new Question(
        "", null,"", null,0);
    assertFalse("Question should not be asked yet", question.isHasBeenAsked());

    question.resetHasBeenAsked();
    assertFalse("Question should not be asked", question.isHasBeenAsked());

    question.setHasBeenAsked();
    assertTrue("Question should be asked", question.isHasBeenAsked());

    question.resetHasBeenAsked();
    assertFalse("Question should not be asked", question.isHasBeenAsked());
  }

  /** Tests isNotAskable. */
  @Test
  public void testIsNotAskable() {
    // ******* Askable questions.
    Question question1 = new Question(
        "What?", null,"Nothing!", null,0);
    assertFalse("Question 1 should be askable", question1.isNotAskable());

    Question question2 = new Question(
        "", "question-image","Nothing!", null,0);
    assertFalse("Question 2 should be askable", question2.isNotAskable());

    Question question3 = new Question(
        "What?", null,"", "answer-image",0);
    assertFalse("Question 3 should be askable", question3.isNotAskable());

    Question question4 = new Question(
        null, "question-image","", "answer-image",0);
    assertFalse("Question 4 should be askable", question4.isNotAskable());

    // ******* NOT askable questions.
    Question question5 = new Question(
        "", "","", "answer-image",0);
    assertTrue("Question 5 should not be askable", question5.isNotAskable());

    Question question6 = new Question(
        "", "question-image","", "",0);
    assertTrue("Question 6 should not be askable", question6.isNotAskable());

    Question question7 = new Question(
        null, null,null, null,0);
    assertTrue("Question 7 should not be askable", question7.isNotAskable());

    // ******* Making not askable question - askable.
    question7.setAnswerImage("test-image");
    assertTrue("Question 7 should still be unaskable", question7.isNotAskable());
    question7.setQuestionImage("test-image");
    assertFalse("Question 7 should now be askable", question7.isNotAskable());
  }

  /** Tests parentWithName methods. */
  @Test
  public void testParentWithName() {
    Question question = new Question("", null, "", null,0);
    assertNull("Paren name should be null", question.getParentName());
    HasName parent = () -> "TestParent";
    question.setParentWithName(parent);
    assertEquals("Wrong parent name", "TestParent", question.getParentName());
  }

  /**
   * Asserts question object's state.
   * @param question question to test
   * @param text question's text to assert
   * @param questionImg question's image to assert
   * @param answer question's answer to assert
   * @param answerImg answer's image to assert
   * @param points question's points to assert
   * @param hasBeenAsked question's hasBeenAsked value
   */
  public static void assertQuestion(
      Question question, String text, String questionImg, String answer,
      String answerImg, int points, boolean hasBeenAsked) {
    assertEquals("Wrong question", text, question.getQuestion());
    assertEquals("Wrong question image", questionImg, question.getQuestionImage());
    assertEquals("Wrong answer", answer, question.getAnswer());
    assertEquals("Wrong answer image", answerImg, question.getAnswerImage());
    assertEquals("Wrong question points", points, question.getPoints());
    assertEquals("Wrong question hasBeenAsked", hasBeenAsked, question.isHasBeenAsked());
  }
}
