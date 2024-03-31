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

package net.curre.jjeopardy.ui.edit;

import net.curre.jjeopardy.service.LocaleService;

/**
 * View or print mode for the edit game table.
 *
 * @author Yevgeny Nyden
 */
public enum EditTableMode {

  /** View or print only answers. */
  ANSWERS("jj.print.mode.answers", "jj.print.mode.answers.tooltip"),

  /** View or print only questions. */
  QUESTIONS("jj.print.mode.questions", "jj.print.mode.questions.tooltip"),

  /** View or print all (questions and answers). */
  ALL("jj.print.mode.all", "jj.print.mode.all.tooltip");

  /** Message key for this view/print mode. */
  private final String messageKey;

  /** Message key for this view/print mode tooltip. */
  private final String tooltipKey;

  /**
   * Ctor.
   * @param messageKey message key
   */
  EditTableMode(String messageKey, String tooltipKey) {
    this.messageKey = messageKey;
    this.tooltipKey = tooltipKey;
  }

  /**
   * Gets the message corresponding to this view/print mode.
   * @return view/print mode message
   */
  public String getMessage() {
    return LocaleService.getString(this.messageKey);
  }

  /**
   * Gets the tooltip message corresponding to this view/print mode.
   * @return the tooltip message
   */
  public String getTooltip() {
    return LocaleService.getString(this.tooltipKey);
  }
}
