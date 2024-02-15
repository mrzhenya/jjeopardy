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
  private final String name;

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
  public Category(String name, List<Question> questions) {
    this.name = name;
    this.questions = questions;

    for (Question q : questions) {
      q.setParentWithName(this);
    }
  }

  /**
   * Gets the category's name.
   * @return the name of this category
   */
  public String getName() {
    return this.name;
  }

  /**
   * Gets a title string of this category.
   * @return category string (includes the word "Category")
   */
  public String getNameString() {
    return LocaleService.getString("jj.category.name", this.name);
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
}
