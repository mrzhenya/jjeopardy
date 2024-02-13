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

package net.curre.jjeopardy.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the results of parsing a game file - mostly
 * just the error/warn/info messages, not the game data.
 *
 * @author Yevgeny Nyden
 */
public class FileParsingResult {

  /**
   * Parsing messages (errors, warnings, and informational messages).
   */
  public enum Message {
    /** Error of opening or parsing a file. */
    MSG_PARSING("jj.file.msg.error.parsing"),

    /** Blank game name error. */
    MSG_BLANK_NAME("jj.file.msg.error.name.blank"),

    /** Name is not specified error. */
    MSG_NAME_NOT_FOUND("jj.file.msg.error.name.notfound"),

    /** Blank category name error. */
    MSG_BLANK_CATEGORY_NAME("jj.file.msg.error.category.name.empty"),

    /** Blank question error. */
    MSG_BLANK_QUESTION("jj.file.msg.error.question.empty"),

    /** Blank question error. */
    MSG_BLANK_ANSWER("jj.file.msg.error.answer.empty"),

    /** Empty categories error. */
    MSG_NO_CATEGORIES("jj.file.msg.error.categories.empty"),

    /** Not enough categories error. */
    MSG_NOT_ENOUGH_CATEGORIES("jj.file.msg.error.categories.notenough"),

    /** Too many categories error. */
    MSG_TOO_MANY_CATEGORIES("jj.file.msg.error.categories.toomany"),

    /** Not matching number of questions for a category. */
    MSG_NOT_MATCHING_QUESTIONS("jj.file.msg.error.questions.number"),

    /** No questions are parsed. */
    MSG_NO_QUESTIONS("jj.file.msg.error.questions.empty"),

    /** Not enough questions are parsed. */
    MSG_NOT_ENOUGH_QUESTIONS("jj.file.msg.error.questions.notenough"),

    /** Too many questions are parsed. */
    MSG_TOO_MANY_QUESTIONS("jj.file.msg.error.questions.toomany"),

    /** Blank player name warning message. */
    MSG_BLANK_PLAYER_NAME("jj.file.msg.error.player.blankname"),

    /** Not enough players warning message. */
    MSG_TOO_FEW_PLAYERS("jj.file.msg.warn.player.toofew"),

    /** Too many players warning message. */
    MSG_TOO_MANY_PLAYERS("jj.file.msg.warn.player.toomany"),

    /** Not enough bonus questions warning message. */
    MSG_TOO_FEW_BONUS_QUESTIONS("jj.file.msg.warn.bonusquestions.toofew"),

    /** Number of parsed questions and categories informational message. */
    MSG_QUESTIONS_PARSED("jj.file.msg.info.questions"),

    /** Number of parsed players informational message. */
    MSG_PLAYERS_PARSED("jj.file.msg.info.players"),

    /** Number of parsed bonus questions informational message. */
    MSG_BONUS_QUESTIONS_PARSED("jj.file.msg.info.bonusquestions");

    /** Property name for this message. */
    public final String propertyName;

    /**
     * Gets the property name for this message.
     * @return property name
     */
    public String getPropertyName() {
      return this.propertyName;
    }

    /**
     * Ctor.
     * @param propertyName property name for this message
     */
    private Message(String propertyName) {
      this.propertyName = propertyName;
    }
  }

  /** Name of the file that's being parsed. */
  private final String fileName;

  /** True indicates that the parsed data is usable. */
  private boolean gameDataUsable;

  /** List of error messages. */
  private final List<String> errorMessages;

  /** List of warning messages. */
  private final List<String> warningMessages;

  /** List of informational messages. */
  private final List<String> infoMessages;

  /**
   * Ctor.
   * @param fileName name of the file that's being parsed
   */
  public FileParsingResult(String fileName) {
    this.fileName = fileName;
    this.gameDataUsable = false;
    this.errorMessages = new ArrayList<>();
    this.warningMessages = new ArrayList<>();
    this.infoMessages = new ArrayList<>();
  }

  /**
   * Gets the filename that's being parsed.
   * @return filename
   */
  public String getFileName() {
    return this.fileName;
  }

  /**
   * Determines if the parsed data is usable.
   * @return true if the parsed data is usable
   */
  public boolean isGameDataUsable() {
    return gameDataUsable;
  }

  /**
   * Sets the parsing success.
   */
  public void setGameDataUsable() {
    this.gameDataUsable = true;
  }

  /**
   * Gets error message from this parsing action.
   * @return error messages
   */
  public List<String> getErrorMessages() {
    return this.errorMessages;
  }

  /**
   * Gets warning message from this parsing action.
   * @return warning messages
   */
  public List<String> getWarningMessages() {
    return this.warningMessages;
  }

  /**
   * Gets informational message from this parsing action.
   * @return informational messages
   */
  public List<String> getInfoMessages() {
    return this.infoMessages;
  }

  /**
   * Adds an error message to the error message list.
   * @param error error message to add
   * @param args arguments to the property string
   */
  public void addErrorMessage(Message error, String... args) {
    this.errorMessages.add(LocaleService.getString(error.getPropertyName(), args));
  }

  /**
   * Adds a warning message to the warning message list.
   * @param warning error message to add
   * @param args arguments to the property string
   */
  public void addWarningMessage(Message warning, String... args) {
    this.warningMessages.add(LocaleService.getString(warning.getPropertyName(), args));
  }

  /**
   * Adds an info message to the info message list.
   * @param info error message to add
   * @param args arguments to the property string
   */
  public void addInfoMessage(Message info, String... args) {
    this.infoMessages.add(LocaleService.getString(info.getPropertyName(), args));
  }

  /**
   * Gets a very short message that is displayed in a dialog title.
   * @return short result message
   */
  public String getResulTitleShort() {
    if (this.gameDataUsable) {
      return LocaleService.getString("jj.file.result.title.success");
    } else {
      return LocaleService.getString("jj.file.result.title.failure");
    }
  }

  /**
   * Gets a long message that is displayed in a dialog header.
   * @return long result message (but not the content message)
   */
  public String getResulTitleLong() {
    if (this.gameDataUsable) {
      return LocaleService.getString("jj.file.result.header.success");
    } else {
      return LocaleService.getString("jj.file.result.header.failure");
    }
  }
}
