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

/**
 * Represents a player in the game.
 * Use GameData service to obtain the list of players for the current game.
 *
 * @see net.curre.jjeopardy.service.GameDataService
 * @author Yevgeny Nyden
 */
public class Question {

  /** The question text for this Question. */
  private final String question;

  /** An image filename for the question if any. */
  private final String questionImage;

  /** The answer text of this Question. */
  private final String answer;

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
   * @param points points value of this question
   */
  public Question(String question, String questionImage, String answer, int points) {
    this.question = question;
    this.questionImage = questionImage;
    this.answer = answer;
    this.points = points;
    this.hasBeenAsked = false;
  }

  /**
   * Gets the question text.
   * @return the question
   */
  public String getQuestion() {
    return this.question;
  }

  /**
   * Gets the question image filename.
   * @return the question image or null if none
   */
  public String getQuestionImage() {
    return this.questionImage;
  }

  /**
   * Gets the answer text.
   * @return the answer
   */
  public String getAnswer() {
    return answer;
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
}
