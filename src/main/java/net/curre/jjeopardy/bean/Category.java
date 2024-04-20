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

import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.util.JjDefaults;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a question category that has a name, relative index,
 * and a list of questions associated with this category.
 * 
 * @see Question
 * @author Yevgeny Nyden
 */
public class Category implements HasName {

  /** Name of this category. */
  private String name;

  /** List of questions for this category. */
  private final List<Question> questions;

  /**
   * Constructs a new category object.
   * Note, that this method also sets category reference
   * for every question on the passed list of questions.
   *
   * @param name category name
   * @param questions list of questions for this category
   */
  public Category(String name, @NotNull List<Question> questions) {
    this.name = name;
    this.questions = questions;

    for (Question q : questions) {
      q.setParentWithName(this);
    }
  }

  /**
   * Creates a copy/clone of this category.
   * @return a new category object initialized with the same data
   */
  public Category createCopy() {
    List<Question> questions = new ArrayList<>();
    for (Question question : this.questions) {
      questions.add(question.createCopy());
    }
    Category clone = new Category(this.name, questions);
    for (Question question : clone.questions) {
      question.setParentWithName(clone);
    }
    return clone;
  }

  /**
   * Gets the category's name.
   * @return the name of this category
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the category's name.
   * @param newName the new name of this category
   * @return true if the value has changed; false if otherwise
   */
  public boolean setName(String newName) {
    boolean isChanged = !StringUtils.equals(newName, this.name);
    this.name = newName;
    return isChanged;
  }

  /**
   * Gets a title string of this category.
   * @return category string (includes the word "Category")
   */
  public String getNameString() {
    return LocaleService.getString("jj.category.name", this.name);
  }

  /**
   * Gets the questions for this category.
   * @return list of questions for this category
   */
  public List<Question> getQuestions() {
    return this.questions;
  }

  /**
   * Gets the question given its index.
   * @param index index of the question to get
   * @return question with the provided index
   */
  public Question getQuestion(int index) {
    return this.questions.get(index);
  }

  /**
   * Gets the questions count in this category.
   * @return number of questions in this category
   */
  public int getQuestionsCount() {
    return this.questions.size();
  }

  /**
   * Ensures the max number of questions on this category. Extra questions are
   * removed.
   */
  public void ensureMaxQuestionsCount() {
    while (this.questions.size() > JjDefaults.MAX_NUMBER_OF_QUESTIONS) {
      this.questions.remove(this.questions.size() - 1);
    }
  }

  /**
   * Removes a question from this category.
   * @param questionInd index of the question to remove (zero based)
   */
  public void removeQuestion(int questionInd) {
    this.questions.remove(questionInd);
  }
}
