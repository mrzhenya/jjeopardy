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

package net.curre.jjeopardy.util;

import net.curre.jjeopardy.bean.Category;
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.bean.Question;
import org.apache.commons.lang3.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A set of basic constants and utilities to assist with parsing and generating
 * content for the game XML files.
 * IMPORTANT: when changing the game property keys, make sure to adjust the comparing
 * logic in the GameEntry and the regex pattern constants.
 *
 * @author Yevgeny Nyden
 */
public class XmlFileUtilities {

  /** Name of the game XML file property that contains the game name. */
  public static final String PROPERTY_NAME = "game.name";

  /** Name of the game XML file property that contains the game description. */
  public static final String PROPERTY_DESCRIPTION = "game.description";

  /** Name of the game XML file property that contains the bonus question points. */
  public static final String PROPERTY_BONUS_QUESTION_POINTS = "bonus.question.points";

  /** Name fo the game XML file property that contains image download failure bit. */
  public static final String PROPERTY_IMAGE_FAILURE = "game.image.failure";

  /** Regular question key pattern. */
  private static final Pattern PATTERN_CAT_LINE = Pattern.compile("^category\\.(\\d+)\\.(answer|question)\\.(\\d+)(.*)$");

  /** Category name key pattern. */
  private static final Pattern PATTERN_CAT_NAME = Pattern.compile("^category\\.(\\d+)\\.name$");

  /** Question points key pattern. */
  private static final Pattern PATTERN_POINTS = Pattern.compile("^question\\.(\\d+)\\..*$");

  /**
   * Gets the name of a property that hold the category name.
   * @param categoryNumber category number (starts with 1)
   * @return property name for the category name
   */
  public static String getPropertyCategoryName(int categoryNumber) {
    return "category." + categoryNumber + ".name";
  }

  /**
   * Gets the name of a property that hold the question points value.
   * @param questionNumber question number (starts with 1)
   * @return property name for the question points value
   */
  public static String getPropertyQuestionPoints(int questionNumber) {
    return "question." + questionNumber + ".points";
  }

  /**
   * Gets the name of a property that hold the question text value.
   * @param categoryNumber category number (starts with 1)
   * @param questionNumber question number (starts with 1)
   * @return property name for the question text value
   */
  public static String getPropertyQuestionText(int categoryNumber, int questionNumber) {
    return "category." + categoryNumber + ".question." + questionNumber;
  }

  /**
   * Gets the name of a property that hold the question image file name.
   * @param categoryNumber category number (starts with 1)
   * @param questionNumber question number (starts with 1)
   * @return property name for the question image file name
   */
  public static String getPropertyQuestionImage(int categoryNumber, int questionNumber) {
    return "category." + categoryNumber + ".question." + questionNumber + ".img";
  }

  /**
   * Gets the name of a property that hold the answer text value.
   * @param categoryNumber category number (starts with 1)
   * @param questionNumber question number (starts with 1)
   * @return property name for the answer text value
   */
  public static String getPropertyAnswerText(int categoryNumber, int questionNumber) {
    return "category." + categoryNumber + ".answer." + questionNumber;
  }

  /**
   * Gets the name of a property that hold the answer image file name.
   * @param categoryNumber category number (starts with 1)
   * @param questionNumber question number (starts with 1)
   * @return property name for the answer image file name
   */
  public static String getPropertyAnswerImage(int categoryNumber, int questionNumber) {
    return "category." + categoryNumber + ".answer." + questionNumber + ".img";
  }

  /**
   * Gets the name of a property that hold the player's name value.
   * @param playerNumber player number (starts with 1)
   * @return property name for the player's name value
   */
  public static String getPropertyPlayerName(int playerNumber) {
    return "player." + playerNumber + ".name";
  }

  /**
   * Gets the name of a property that hold the bonus question text value.
   * @param questionNumber question number (starts with 1)
   * @return property name for the bonus question text value
   */
  public static String getPropertyBonusQuestionText(int questionNumber) {
    return "bonus." + questionNumber + ".question";
  }

  /**
   * Gets the name of a property that hold the bonus question image file name.
   * @param questionNumber question number (starts with 1)
   * @return property name for the bonus question image file name
   */
  public static String getPropertyBonusQuestionImage(int questionNumber) {
    return "bonus." + questionNumber + ".question.img";
  }

  /**
   * Gets the name of a property that hold the bonus question's answer text value.
   * @param questionNumber question number (starts with 1)
   * @return property name for the bonus question's answer text value
   */
  public static String getPropertyBonusAnswerText(int questionNumber) {
    return "bonus." + questionNumber + ".answer";
  }

  /**
   * Gets the name of a property that hold the bonus answer image file name.
   * @param questionNumber question number (starts with 1)
   * @return property name for the bonus answer image file name
   */
  public static String getPropertyBonusAnswerImage(int questionNumber) {
    return "bonus." + questionNumber + ".answer.img";
  }

  /**
   * Creates a game XML properties file populated with the passed game data.
   * @param gameData game data to use
   * @param filePath absolute filepath to the destination file
   */
  public static void createGameFile(GameData gameData, String filePath) throws IOException {
    Properties properties = new GameProperties();
    FileOutputStream fileOutputStream = new FileOutputStream(filePath);

    // Adding game name and an optional game description.
    properties.setProperty(PROPERTY_NAME, gameData.getGameName());
    if (!StringUtils.isBlank(gameData.getGameDescription())) {
      properties.setProperty(PROPERTY_DESCRIPTION, gameData.getGameDescription());
    }

    // Adding questions points (we pick them from the first category).
    List<Category> categories = gameData.getCategories();
    Category category = categories.get(0);
    for (int ind = 0; ind < category.getQuestionsCount(); ind++) {
      // Note the question index starts at 1.
      properties.setProperty(getPropertyQuestionPoints(ind + 1),
          String.valueOf(category.getQuestion(ind).getPoints()));
    }

    // Adding game categories and questions.
    for (int ind = 0; ind < categories.size(); ind++) {
      addCategoryToProperties(properties, categories.get(ind), ind + 1);
    }

    // Adding optional game bonus questions.
    List<Question> bonusQuestions = gameData.getBonusQuestions();
    if (!bonusQuestions.isEmpty()) {
      properties.setProperty(PROPERTY_BONUS_QUESTION_POINTS, String.valueOf(bonusQuestions.get(0).getPoints()));
      for (int ind = 0; ind < bonusQuestions.size(); ind++) {
        // Note that the bonus questions index starts at 1.
        addBonusQuestionToProperties(properties, bonusQuestions.get(ind), ind + 1);
      }
    }

    // Adding optional game players.
    List<String> playerNames = gameData.getPlayerNames();
    if (!playerNames.isEmpty()) {
      for (int ind = 0; ind < playerNames.size(); ind++) {
        // Note that the player index starts at 1.
        properties.setProperty(getPropertyPlayerName(ind + 1), playerNames.get(ind));
      }
    }

    if (gameData.isImageDownloadFailure()) {
      properties.setProperty(PROPERTY_IMAGE_FAILURE, String.valueOf(true));
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
    properties.storeToXML(fileOutputStream,
        "JJeopardy game file generated on " + dateFormat.format(new Date()));

    fileOutputStream.close();
  }

  /**
   * Adds a game category (with questions) to the passed properties.
   * @param properties properties to add data to
   * @param category category with questions to add
   * @param categoryNumber category number (starts at 1)
   */
  private static void addCategoryToProperties(Properties properties, Category category, int categoryNumber) {
    properties.setProperty(getPropertyCategoryName(categoryNumber), category.getName());
    for (int ind = 0; ind < category.getQuestionsCount(); ind++) {
      int questionNumber = ind + 1;
      Question question = category.getQuestion(ind);
      properties.setProperty(getPropertyQuestionText(categoryNumber, questionNumber), question.getQuestion());
      String questionImage = question.getQuestionImage();
      if (questionImage != null) {
        properties.setProperty(getPropertyQuestionImage(categoryNumber, questionNumber), questionImage);
      }
      String answerImage = question.getAnswerImage();
      if (answerImage != null) {
        properties.setProperty(getPropertyAnswerImage(categoryNumber, questionNumber), answerImage);
      }
      try {
        properties.setProperty(getPropertyAnswerText(categoryNumber, questionNumber), question.getAnswer());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Adds a bonus question to the properties.
   * @param properties properties to add bonus question to
   * @param question bonus question to add
   * @param questionNumber question number (starts at 1)
   */
  private static void addBonusQuestionToProperties(Properties properties, Question question, int questionNumber) {
    properties.setProperty(getPropertyBonusQuestionText(questionNumber), question.getQuestion());
    String questionImage = question.getQuestionImage();
    if (questionImage != null) {
      properties.setProperty(getPropertyBonusQuestionImage(questionNumber), questionImage);
    }
    properties.setProperty(getPropertyBonusAnswerText(questionNumber), question.getAnswer());
  }

  /**
   * Game properties file that keeps the property keys in a proper order.
   */
  protected static class GameProperties extends Properties {

    /** Ctor. */
    public GameProperties() {
    }

    /** @inheritDoc */
    @SuppressWarnings("SortedCollectionWithNonComparableKeys")
    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
      Set<Map.Entry<Object, Object>> entrySet = super.entrySet();
      TreeSet<Map.Entry<Object, Object>> sortedSet = new TreeSet<>();
      for (Map.Entry<Object, Object> entry : entrySet) {
        GameEntry newEntry = new GameEntry(entry.getKey(), entry.getValue());
        sortedSet.add(newEntry);
      }
      return Collections.synchronizedSet(sortedSet);
    }
  }

  /**
   * Represents a map entry for a game XML property file. These entries
   * can be sorted in a predictable (alphabetically sorted) way.
   */
  protected static class GameEntry implements Map.Entry<Object, Object>, Comparable<GameEntry> {

    /** Property key. */
    private final String key;

    /** Property value. */
    private String value;

    /**
     * Ctor.
      * @param key the property key
     * @param value the property value
     */
    GameEntry(Object key, Object value) {
      this.key = (String) key;
      this.value = (String) value;
    }

    /** @inheritDoc */
    @Override
    public String getKey() {
      return this.key;
    }

    /** @inheritDoc */
    @Override
    public String getValue() {
      return this.value;
    }

    /** @inheritDoc */
    @Override
    public String setValue(Object value) {
      String old = this.value;
      this.value = (String) value;
      return old;
    }

    /** @inheritDoc */
    @Override
    public int compareTo(GameEntry other) {
      if (other == null) {
        return -1;
      }
      if (this.key == null && other.key == null) {
        return 0;
      }
      String thisKey = createSortKey(this.key);
      String otherKey = createSortKey(other.key);
      return thisKey.compareTo(otherKey);
    }
  }

  /**
   * Converts the property key string to a string that can be used for alphabetical sorting
   * of property keys (lines in property file).
   * @param key key to use
   * @return sort key to use - (!!!) each key uniquely represents a line
   */
  private static String createSortKey(String key) {
    if (key == null) {
      return "";
    }
    Matcher lineMatcher = PATTERN_CAT_LINE.matcher(key);
    if (lineMatcher.find()) {
      // This is a regular question line (most common).
      String q = "x";
      if (lineMatcher.group(2).startsWith("question")) {
        q = "a";
      }
      return "category" + lineMatcher.group(1) + lineMatcher.group(3) + q + lineMatcher.group(4);
    }
    Matcher nameMatcher = PATTERN_CAT_NAME.matcher(key);
    if (nameMatcher.find()) {
      // This is a category name line.
      return "category" + nameMatcher.group(1) + "00";
    }
    if (key.startsWith(PROPERTY_NAME)) {
      // Game name should be the first, topmost row.
      return "a";
    }
    if (key.startsWith(PROPERTY_DESCRIPTION)) {
      // Game description is right after the game name.
      return "b";
    }
    Matcher pointsMatcher = PATTERN_POINTS.matcher(key);
    if (pointsMatcher.find()) {
      // Question points value line is after name and description.
      return "caa" + pointsMatcher.group(1);
    }
    // Other keys returned as is.
    return key;
  }
}
