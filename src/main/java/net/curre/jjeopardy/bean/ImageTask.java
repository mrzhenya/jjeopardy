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
 * Represents a triplet of information to assist with copying/downloading image files.
 *
 * @author Yevgeny Nyden
 */
public class ImageTask {

  /** Image URL. */
  private final String url;

  /** Question object this image belongs to. */
  private final Question question;

  /** Whether this image belongs to the question or the answer part. */
  private final boolean isQuestionImage;

  /**
   * Ctor.
   * @param url image URL
   * @param question question object this image belongs to
   * @param isQuestionImage true if this image is for the question part; false if for the answer
   */
  public ImageTask(String url, Question question, boolean isQuestionImage) {
    this.url = url;
    this.question = question;
    this.isQuestionImage = isQuestionImage;
  }

  /**
   * Gets the image URL.
   * @return the image URL.
   */
  public String getUrl() {
    return this.url;
  }

  /**
   * Gets the question this image belongs to.
   * @return the image's question object
   */
  public Question getQuestion() {
    return this.question;
  }

  /**
   * Whether this image belongs to the question or the answer part.
   * @return true if this image belongs to the question part; false if to the answer part
   */
  public boolean isImageForQuestion() {
    return this.isQuestionImage;
  }
}
