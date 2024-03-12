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

import net.curre.jjeopardy.bean.Category;
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.bean.Question;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTML parsing service to parse games from the jeopardylabs.com HTML files.
 *
 * @author Yevgeny Nyden
 */
public class HtmlParsingService {

  /** Max length of the description (in chars). */
  protected static final int MAX_DESCRIPTION_LENGTH = 250;

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(HtmlParsingService.class.getName());

  /** Base URL for the jeopardylabs.com site. */
  private static final String JLABS_BASE_URL = "https://jeopardylabs.com";

  /** Ctor. */
  protected HtmlParsingService() {
  }

  /**
   * Parses an HTML game file downloaded from the jeopardylabs.com website.
   * Important: the image files are not downloaded at this point, only the URLs are set
   * on the corresponding question properties.
   * @param filePath path to the file to parse
   * @return parsed game data
   */
  protected GameData parseJeopardyLabsHtmlFile(String filePath) {
    GameData gameData = new GameData(filePath, null, false);
    try {
      LOGGER.info("Parsing HTML file: " + filePath);
      Document doc = Jsoup.parse(new File(filePath), "UTF-8", JLABS_BASE_URL);

      gameData.setFileDataAcquired();
      gameData.setGameName(parseGameName(doc));
      gameData.setGameDescription(parseGameDescription(doc));

      // Find the main game table grid (direct <div> children of the <div class="grid"> element).
      Elements gameBoard = doc.select("div.grid > div");
      if (gameBoard.size() < 3) {
        // Something is not right, too few elements.
        LOGGER.log(Level.SEVERE, "Game grid doesn't have enough rows: " + gameBoard.size());
        return gameData;
      }

      // Read game categories from the first "row" of <div role="columnheader"> elements.
      Map<String, List<Question>> questionsMap = new HashMap<>();
      ArrayList<String> categoryNames = new ArrayList<>();
      Elements categoryEls = gameBoard.first().getElementsByAttributeValue("role", "columnheader");
      for (Element catEl : categoryEls) {
        categoryNames.add(catEl.text());
        questionsMap.put(catEl.text(), new ArrayList<>());
      }

      // Now, go over all "rows" after the header row (with categories), and parse the questions.
      for (int ind = 1; ind < gameBoard.size(); ind++) {
        // Get all data cells in the row (these are going to be questions of the same value for
        // all game categories).
        for (Element questionEl : gameBoard.get(ind).getElementsByAttributeValue("role", "cell")) {
          parseQuestionData(questionsMap, questionEl);
        }
      }

      // Populate game data with parsed categories/questions.
      List<Category> categories = new ArrayList<>();
      for (String categoryName : categoryNames) {
        categories.add(new Category(categoryName, questionsMap.get(categoryName)));
      }
      gameData.setCategories(categories);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unable to open or parse HTML file: " + filePath, e);
    }
    return gameData;
  }

  /**
   * Parses data for a single question.
   * @param questionsMap map where the parsed question is stored
   * @param dataEl data element to parse
   * @throws ServiceException when a critical parsing assumption is not true
   */
  private static void parseQuestionData(Map<String, List<Question>> questionsMap, Element dataEl)
      throws ServiceException {
    // Find the first div element that contains category name (in the attribute) and the
    // points value in the text data.
    Element categoryEl = dataEl.getElementsByAttribute("data-category").first();
    String category = sanitizeString(categoryEl.attribute("data-category").getValue());
    int points = 0;
    try {
      points = Integer.parseInt(categoryEl.text());
    } catch (NumberFormatException e) {
      LOGGER.warning("Unable to parse question points: \"" + categoryEl.text() + "\"!");
    }
    List<Question> categoryQuestions = questionsMap.get(category);
    if (categoryQuestions == null) {
      // This is a critical error - the table header category names don't match
      // those that are parsed from the individual question category association.
      throw new ServiceException("Unable to parse questions; category \"" + category + "\" is not found!");
    }

    // Question and Answer text is nested in <div> elements with the corresponding classes.
    // There are also 'question' and 'answer' classes, but they seemed to be flipped.
    String questionStr = parseStringFromFirstChild(dataEl.getElementsByClass("front"));
    String questionImage = parseImageFromFirstChild(dataEl.getElementsByClass("front"));
    String answerStr = parseStringFromFirstChild(dataEl.getElementsByClass("back"));
    String answerImage = parseImageFromFirstChild(dataEl.getElementsByClass("back"));

    categoryQuestions.add(new Question(questionStr, questionImage, answerStr, answerImage, points));
  }

  /**
   * Parses the trimmed string value from the first child on the given Elements object.
   * @param elements the parent
   * @return trimmed string value or null if the parent is childless or if the string value is blank
   */
  private static String parseStringFromFirstChild(Elements elements) {
    if (elements.isEmpty()) {
      return null;
    }
    String text = elements.first().text();
    if (StringUtils.isBlank(text)) {
      return null;
    }
    return text;
  }

  /**
   * Parses the image value from the first child on the given Elements object.
   * @param elements the parent
   * @return image value or null if the parent is childless or if there is not img nested
   */
  private static String parseImageFromFirstChild(Elements elements) {
    if (elements.isEmpty()) {
      return null;
    }
    Elements imgElements = elements.first().getElementsByTag("img");
    if (imgElements.isEmpty()) {
      return null;
    }
    Attribute src = imgElements.first().attribute("src");
    if (src == null) {
      return null;
    }
    String imagePath = src.getValue().trim();
    if (imagePath.startsWith("/")) {
      imagePath = JLABS_BASE_URL + imagePath;
    }
    return imagePath;
  }

  /**
   * Parses game name.
   * @param doc HTML document
   * @return parsed game name
   */
  private static String parseGameName(Document doc) {
    Element titleEl = doc.getElementsByTag("title").first();
    return titleEl.text();
  }

  /**
   * Parses game description.
   * @param doc HTML document
   * @return parsed description or null
   */
  private static String parseGameDescription(Document doc) {
    // Description is parsed from: <meta name="description" content="???">.
    Elements metaEls = doc.getElementsByTag("meta");
    for (Element metaEl : metaEls) {
      Attribute nameAttr = metaEl.attribute("name");
      Attribute contentAttr = metaEl.attribute("content");
      if (nameAttr != null && contentAttr != null &&
          StringUtils.equalsAnyIgnoreCase("description", nameAttr.getValue())) {
        String description = contentAttr.getValue().trim();
        String appendStr = description.length() >= MAX_DESCRIPTION_LENGTH ? "..." : "";
        return StringUtils.substring(description, 0, MAX_DESCRIPTION_LENGTH - 3) + appendStr;
      }
    }
    return null;
  }

  /**
   * Cleans the passed string of unwanted characters.
   * @param value value to sanitize
   * @return sanitized string
   */
  private static String sanitizeString(String value) {
    return RegExUtils.replaceAll(value, "( |\r|\n)+", " ").trim();
  }
}
