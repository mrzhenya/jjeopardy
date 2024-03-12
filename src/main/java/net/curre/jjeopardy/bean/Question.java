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

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a single question (regular or bonus) asked a player in the game.
 * A list of question for the current game is stored in a <code>GameData</code> object and
 * can be obtained via the <code>GameDataService</code> service.
 * <br><br>
 * Note, that a valid question object must have at least text or image for both - the question
 * and the answer part; otherwise, it will not be displayed on the game table - will be 'blanked out'
 * as it has been asked.
 *
 * @see net.curre.jjeopardy.service.GameDataService
 * @author Yevgeny Nyden
 */
public class Question {

  /** The question text for this Question. */
  private final String question;

  /**
   * An image filename for the question if any - a filename of an image
   * in the game bundle directory or a URL.
   */
  private String questionImage;

  /** The answer text of this Question. */
  private final String answer;

  /**
   * An image filename for the answer if any - a filename of an image
   * in the game bundle directory or a URL.
   */
  private String answerImage;

  /** Points value of this question. */
  private final int points;

  /** True if this question has been asked. */
  private boolean hasBeenAsked;

  /** The parent of this question - a player or a category. */
  private HasName parentWithName;

  /**
   * Ctor.
   * @param question the question string
   * @param questionImage question image filename if any
   * @param answer the answer string
   * @param answerImage the answer image filename if any
   * @param points points value of this question
   */
  public Question(String question, String questionImage, String answer, String answerImage, int points) {
    this.question = question;
    this.answer = answer;
    this.points = points;
    this.hasBeenAsked = false;
    this.setQuestionImage(questionImage);
    this.setAnswerImage(answerImage);
  }

  /**
   * Gets the question text.
   * @return the question
   */
  public String getQuestion() {
    return this.question == null ? "" : this.question;
  }

  /**
   * Gets the question image filename - a filename of an image
   * in the game bundle directory or a URL.
   * @return the question image or null if none
   */
  public String getQuestionImage() {
    return this.questionImage;
  }

  /**
   * Sets the question image filename -  a filename of an image
   * in the game bundle directory or a URL.
   * @param questionImage the question image or null if none
   */
  public void setQuestionImage(String questionImage) {
    this.questionImage = StringUtils.isBlank(questionImage) ? null : questionImage;
  }

  /**
   * Gets the answer text.
   * @return the answer
   */
  public String getAnswer() {
    return this.answer == null ? "" : this.answer;
  }

  /**
   * Gets the answer image filename.
   * @return the answer image or null if none
   */
  public String getAnswerImage() {
    return this.answerImage;
  }

  /**
   * Sets the answer image filename.
   * @param answerImage the answer image or null if none
   */
  public void setAnswerImage(String answerImage) {
    this.answerImage = StringUtils.isBlank(answerImage) ? null : answerImage;
  }

  /**
   * Gets the points value of this question.
   * @return the points value
   */
  public int getPoints() {
    return this.points;
  }

  /**
   * Determines if this question has been asked or not yet.
   * @return true if this question has been asked
   */
  public boolean isHasBeenAsked() {
    return this.hasBeenAsked;
  }

  /**
   * Marks this question as asked (used).
   */
  public void setHasBeenAsked() {
    this.hasBeenAsked = true;
  }

  /**
   * Marks this question to be 'not asked'.
   */
  public void resetHasBeenAsked() {
    this.hasBeenAsked = false;
  }

  /**
   * Sets the parent of this question - a player or a category.
   * @param parentWithName parent
   */
  public void setParentWithName(HasName parentWithName) {
    this.parentWithName = parentWithName;
  }

  /**
   * Gets the parent's name string (e.g. Category X or Player 3).
   * @return parent's name string or null if no parent is set
   */
  public String getParentName() {
    return this.parentWithName == null ? null : this.parentWithName.getNameString();
  }

  /**
   * Determines if the question is askable, meaning that there is enough data for asking a question
   * or showing an answer.
   * @return true if both text and image is not present for either question or answer
   */
  public boolean isNotAskable() {
    return (StringUtils.isBlank(this.question) && this.questionImage == null) ||
        (StringUtils.isBlank(this.answer) && this.answerImage == null);
  }
}
