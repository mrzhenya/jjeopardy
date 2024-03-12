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

import net.curre.jjeopardy.images.ImageEnum;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.util.JjDefaults;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static net.curre.jjeopardy.bean.FileParsingResult.Message.*;

/**
 * Object of this class represents data parsed from a games file.
 * All game data validation code is also kept in here. To determine if there
 * is enough data for a game (game name and enough categories and questions),
 * call <code>#isGameDataUsable()</code>.
 *
 * @author Yevgeny Nyden
 */
public class GameData implements Comparable<GameData> {

  /** Min total question count to consider game to be Extra Large. */
  private static final int XLARGE_GAME_SIZE = 60;

  /** Min total question count to consider game to be Large. */
  private static final int LARGE_GAME_SIZE = 45;

  /** Min total question count to consider game to be Medium. */
  private static final int MEDIUM_GAME_SIZE = 30;

  /** Estimated question game time worth (to estimate the length of the game). */
  private static final int APPROX_QUESTION_TIME_SEC = JjDefaults.QUESTION_TIME + 40;

  /** Game data file absolute path. */
  private String filePath;

  /** Game bundle directory absolute path. */
  private String bundlePath;

  /** Flag to indicate that the data was acquired from a game-native format (vs parsed from html file). */
  private boolean nativeData;

  /**
   * Indicates that game file was opened and parsed successfully.
   * False indicates that no data in this object besides the file name is set.
   */
  private boolean isFileDataAcquired;

  /** Game name parsed from the file. */
  private String gameName;

  /** Game description. */
  private String gameDescription;

  /** Game questions/categories parsed from the file. */
  private final List<Category> categories;

  /**
   * Optional player names parsed from the file. Note, that these are not
   * necessarily the players used in the game which are stored in GamesDataService.
   */
  private final List<String> playerNames;

  /** Optional bonus questions. */
  private final List<Question> bonusQuestions;

  /** Indicates that some images failed to download. */
  private boolean failedImageDownload;

  /**
   * Ctor.
   * @param filePath absolute path to the XML game file
   * @param bundleOrNull absolute path to the game bundle if the file is in a bundle; or null if it's a standalone file
   * @param isNativeData true if the data was parsed from a game-native data format
   */
  public GameData(String filePath, String bundleOrNull, boolean isNativeData) {
    this.filePath = filePath;
    this.bundlePath = bundleOrNull;
    this.nativeData = isNativeData;
    this.isFileDataAcquired = false;
    this.categories = new ArrayList<>();
    this.playerNames = new ArrayList<>();
    this.bonusQuestions = new ArrayList<>();
    this.failedImageDownload = false;
  }

  /**
   * Gets the game XML file path.
   * @return game file absolute path
   */
  public String getFilePath() {
    return this.filePath;
  }

  /**
   * Sets the game file paths.
   * @param filePath absolute path to the XML game file
   * @param bundleOrNull absolute path to the game bundle if the file is in a bundle; or null if it's a standalone file
   */
  public void setGameFilePaths(String filePath, String bundleOrNull) {
    this.filePath = filePath;
    this.bundlePath = bundleOrNull;
  }

  /**
   * Gets the game bundle directory absolute path.
   * @return game directory bundle absolute path or null if this game has only one (standalone) file
   */
  public String getBundlePath() {
    return this.bundlePath;
  }

  /**
   * Determines whether the data was parsed from a game-native data formatted file
   * (vs from an HTML file acquired elsewhere). This also means that there is an XML file
   * which we can copy when adding this game to the library.
   * @return true if this game data was acquired from a game native data file
   */
  public boolean isNativeData() {
    return this.nativeData;
  }

  /**
   * Marks this game data as backed by game native formatted file/bundle.
   */
  public void changeToNativeData() {
    this.nativeData = true;
  }

  /**
   * Gets the game name.
   * @return current game name
   */
  public String getGameName() {
    return this.gameName;
  }

  /**
   * Sets the game name.
   * @param gameName game name to set
   */
  public void setGameName(String gameName) {
    this.gameName = gameName;
  }

  /**
   * Gets game description.
   * @return game description
   */
  public String getGameDescription() {
    return this.gameDescription;
  }

  /**
   * Sets game description.
   * @param gameDescription game description
   */
  public void setGameDescription(String gameDescription) {
    this.gameDescription = gameDescription;
  }

  /**
   * Gets the game categories/questions.
   * @return current game categories/questions
   */
  public List<Category> getCategories() {
    return this.categories;
  }

  /**
   * Sets the game categories.
   * @param categories question categories data
   */
  public void setCategories(List<Category> categories) {
    this.categories.clear();
    this.categories.addAll(categories);
  }

  /**
   * Gets the player names parsed from a games file.
   * Note, that "real" players that are used in the game are stored in GamesDataService.
   * @return player names
   */
  public List<String> getPlayerNames() {
    return this.playerNames;
  }

  /**
   * Updates player names as parsed from a games file.
   * @param playerNames player names
   */
  public void setPlayersNames(List<String> playerNames) {
    this.playerNames.clear();
    this.playerNames.addAll(playerNames);
  }

  /**
   * Gets optional bonus questions.
   * @return bonus questions
   */
  public List<Question> getBonusQuestions() {
    return this.bonusQuestions;
  }

  /**
   * Determines if bonus questions have been asked (if the first bonus question
   * has been asked, we assume all questions have been asked).
   * @return true if bonus questions have been asked
   */
  public boolean bonusQuestionsHaveBeenAsked() {
    return !this.bonusQuestions.isEmpty() && !this.bonusQuestions.get(0).isHasBeenAsked();
  }

  /**
   * Updates optional bonus questions.
   * @param bonusQuestions bonus questions
   */
  public void setBonusQuestions(List<Question> bonusQuestions) {
    this.bonusQuestions.clear();
    this.bonusQuestions.addAll(bonusQuestions);
  }

  /**
   * Marks this game data with an image download failure bit. This indicates that
   * some images (one or more) failed to download.
   */
  public void setImageDownloadFailure() {
    this.failedImageDownload = true;
  }

  /**
   * Determines if there was an image file download failure.
   * @return true if one or more images for this game data failed to download
   */
  public boolean isImageDownloadFailure() {
    return this.failedImageDownload;
  }

  @Override
  public int compareTo(GameData other) {
    if (this == other) {
      return 0;
    }
    // TODO - implement a more intelligent comparison mechanism (count questions?).

    return this.gameName.compareTo(other.gameName);
  }

  /**
   * Gets the total count of categories in the game.
   * @return total number of categories
   */
  public int getCategoriesCount() {
    return this.categories.size();
  }

  /**
   * Gets the total count of questions in a single category.
   * @return total number of questions in a category
   */
  public int getCategoryQuestionsCount() {
    return this.categories.isEmpty() ? 0 : this.categories.get(0).getQuestionsCount();
  }

  /**
   * Gets the total count of questions in the game.
   * @return total number of questions
   */
  public int getTotalQuestionsCount() {
    return getCategoriesCount() * getCategoryQuestionsCount();
  }

  /**
   * Gets a large image icon corresponding to the size of the current game.
   * @return game size large icon
   */
  public ImageEnum getGameSizeIconLarge() {
    int totalCount = getTotalQuestionsCount();
    if (totalCount > XLARGE_GAME_SIZE) {
      return ImageEnum.SIZE_XL_64;
    } else if (totalCount > LARGE_GAME_SIZE) {
      return ImageEnum.SIZE_L_64;
    } else if (totalCount > MEDIUM_GAME_SIZE) {
      return ImageEnum.SIZE_M_64;
    } else {
      return ImageEnum.SIZE_S_64;
    }
  }

  /**
   * Gets a small image icon corresponding to the size of the current game.
   * @return game size small icon
   */
  public ImageEnum getGameSizeIconSmall() {
    int totalCount = getTotalQuestionsCount();
    if (totalCount > XLARGE_GAME_SIZE) {
      return ImageEnum.SIZE_XL_24;
    } else if (totalCount > LARGE_GAME_SIZE) {
      return ImageEnum.SIZE_L_24;
    } else if (totalCount > MEDIUM_GAME_SIZE) {
      return ImageEnum.SIZE_M_24;
    } else {
      return ImageEnum.SIZE_S_24;
    }
  }

  /**
   * Gets a message describing the size of the current game including the game length.
   * @return game size text string
   * @see #getGameDimensionLongMessage()
   */
  public String getGameSizeText() {
    StringBuilder text = new StringBuilder();
    int totalCount = getTotalQuestionsCount();
    if (totalCount > XLARGE_GAME_SIZE) {
      text.append(LocaleService.getString("jj.file.info.size.xlarge"));
    } else if (totalCount > LARGE_GAME_SIZE) {
      text.append(LocaleService.getString("jj.file.info.size.large"));
    } else if (totalCount > MEDIUM_GAME_SIZE) {
      text.append(LocaleService.getString("jj.file.info.size.medium"));
    } else {
      text.append(LocaleService.getString("jj.file.info.size.small"));
    }
    text.append(" - ").append(this.getGameEstimatedLengthMessage());
    return text.toString();
  }

  /**
   * Gets short game dimension string (e.g. 6x5).
   * @return short game dimension string
   */
  public String getGameDimensionShortText() {
    return this.getCategoriesCount() + "x" + this.getCategoryQuestionsCount();
  }

  /**
   * Gets a short text string describing the size / dimension of the game
   * (x questions in y categories).
   * @return game dimension message
   */
  public String getGameDimensionLongMessage() {
    int categoriesCount = this.getCategoriesCount();
    if (categoriesCount == 0) {
      return LocaleService.getString("jj.file.info.msg0");
    }
    int questionsCount = this.getCategoryQuestionsCount();
    int totalCount = questionsCount * categoriesCount;
    return LocaleService.getString("jj.file.info.msg1",
        String.valueOf(totalCount), String.valueOf(categoriesCount),
        String.valueOf(categoriesCount), String.valueOf(questionsCount));
  }

  /**
   * Gets the estimated game duration.
   * @return string describing the length of the game
   */
  public String getGameEstimatedLengthMessage() {
    int estimatedMin = 0;
    int categoriesCount = this.getCategoriesCount();
    if (categoriesCount > 0) {
      int totalQuestions = categoriesCount * this.getCategoryQuestionsCount();
      int bonusQuestionsCount = this.bonusQuestions.size();
      if (bonusQuestionsCount > 0) {
        totalQuestions += bonusQuestionsCount;
      }

      // TODO - take the number of players into account.
      estimatedMin = totalQuestions * APPROX_QUESTION_TIME_SEC / 60;
    }
    return LocaleService.getString("jj.file.info.msg3", String.valueOf(estimatedMin));
  }

  /**
   * Determines if the game data is usable, which means
   * there is a game name and enough categories/questions to play.
   * Bonus questions or players are optional.
   * @return true if the parsed data is usable
   */
  public boolean isGameDataUsable() {
    return this.isGameNameValid() && this.isCategoriesAndQuestionsDataValid(null);
  }

  /**
   * Sets the file data acquired flag on this game data.
   */
  public void setFileDataAcquired() {
    this.isFileDataAcquired = true;
  }

  /**
   * Resets the game data for a new game. Here we reset the
   * "hasBeenAsked" state of all questions (and bonus questions).
   */
  public void resetGameData() {
    for (Category category : this.categories) {
      for (int i = 0; i < category.getQuestionsCount(); i++) {
        category.getQuestion(i).resetHasBeenAsked();
      }
    }
    for (Question bonusQuestion : this.bonusQuestions) {
      bonusQuestion.resetHasBeenAsked();
    }
  }

  /**
   * Generates file parsing results for this game data.
   * @return game data parsing result
   */
  public FileParsingResult generateFileParsingResult() {
    final FileParsingResult result = new FileParsingResult(this.filePath);
    result.setGameData(this);

    // First, check if the game file was located and parsed successfully.
    if (!this.isFileDataAcquired) {
      result.addErrorMessage(MSG_PARSING);
      return result;
    }

    // Game name is required.
    boolean nameValid = isGameNameValid();
    if (!nameValid) {
      result.addErrorMessage(MSG_MISSING_NAME);
    }

    // Question categories/questions are required as well.
    boolean questionsUsable = isCategoriesAndQuestionsDataValid(result);
    if (nameValid && questionsUsable) {
      result.setGameDataUsable();
    }

    // Generating the info messages for each data part if its parsing was successful.
    if (questionsUsable) {
      int totalQuestions = this.categories.get(0).getQuestionsCount() * this.categories.size();
      result.addInfoMessage(MSG_QUESTIONS_PARSED,
          String.valueOf(totalQuestions), String.valueOf(this.categories.size()));
    }
    if (isBonusQuestionsDataValid(result)) {
      result.addInfoMessage(MSG_BONUS_QUESTIONS_PARSED, String.valueOf(this.bonusQuestions.size()));
    }
    if (isPlayersValid(result)) {
      result.addInfoMessage(MSG_PLAYERS_PARSED, String.valueOf(this.playerNames.size()));
    }
    return result;
  }

  /**
   * Determines if the current players represent a group enough for a game.
   * @return true if there are enough players for a game; false if otherwise
   */
  public boolean hasEnoughPlayers() {
    return this.playerNames.size() >= JjDefaults.MIN_NUMBER_OF_PLAYERS;
  }

  /**
   * Determines if the players data is valid, which is the number of players is either zero or min number.
   * @param resultOrNull result object to add warning messages to; or null if not messages are needed
   * @return true if player data is valid
   */
  public boolean isPlayersValid(FileParsingResult resultOrNull) {
    if (this.playerNames.isEmpty()) {
      return true;
    }
    if (this.playerNames.size() < JjDefaults.MIN_NUMBER_OF_PLAYERS) {
      maybeAddWarning(resultOrNull, MSG_TOO_FEW_PLAYERS, String.valueOf(JjDefaults.MIN_NUMBER_OF_PLAYERS));
      return false;
    } else if (this.playerNames.size() > JjDefaults.MAX_NUMBER_OF_PLAYERS) {
      maybeAddWarning(resultOrNull, MSG_TOO_MANY_PLAYERS);
    }
    return true;
  }

  /**
   * Determines if the game name valid.
   * @return true if game name is valid
   */
  private boolean isGameNameValid() {
    return !StringUtils.isBlank(this.gameName);
  }

  /**
   * Validates game categories and their questions.
   * @param resultOrNull result object to add error and warning messages to; or null if not messages are needed
   * @return true if the categories and questions data is valid
   */
  private boolean isCategoriesAndQuestionsDataValid(FileParsingResult resultOrNull) {
    boolean isValid = true;
    if (this.categories.isEmpty()) {
      maybeAddError(resultOrNull, MSG_NO_CATEGORIES);
      isValid = false;
    } else {
      if (this.categories.size() < JjDefaults.MIN_NUMBER_OF_CATEGORIES) {
        maybeAddError(resultOrNull, MSG_NOT_ENOUGH_CATEGORIES, String.valueOf(JjDefaults.MIN_NUMBER_OF_CATEGORIES));
        isValid = false;
      } else if (this.categories.size() >= JjDefaults.MAX_NUMBER_OF_CATEGORIES) {
        // Too many categories is just a warning, extra categories will be ignored.
        maybeAddWarning(resultOrNull, MSG_TOO_MANY_CATEGORIES, String.valueOf(JjDefaults.MAX_NUMBER_OF_CATEGORIES));
      }
      boolean categoryNameBlankError = false;
      boolean noQuestionsError = false;
      boolean tooFewQuestionsError = false;
      boolean tooManyQuestionsError = false;
      for (Category category : this.categories) {
        if (StringUtils.isBlank(category.getName())) {
          categoryNameBlankError = true;
        }
        if (category.getQuestionsCount() == 0) {
          noQuestionsError = true;
        } else if (category.getQuestionsCount() < JjDefaults.MIN_NUMBER_OF_QUESTIONS) {
          tooFewQuestionsError = true;
        } else if (category.getQuestionsCount() > JjDefaults.MAX_NUMBER_OF_QUESTIONS) {
          tooManyQuestionsError = true;
        }
      }
      if (categoryNameBlankError) {
        maybeAddError(resultOrNull, MSG_BLANK_CATEGORY_NAME);
        isValid = false;
      }
      if (noQuestionsError) {
        maybeAddError(resultOrNull, MSG_NO_QUESTIONS);
        isValid = false;
      }
      if (tooFewQuestionsError) {
        maybeAddError(resultOrNull, MSG_NOT_ENOUGH_QUESTIONS, String.valueOf(JjDefaults.MIN_NUMBER_OF_QUESTIONS));
        isValid = false;
      }
      if (tooManyQuestionsError) {
        // Too many questions is just a warning, extra questions will be ignored.
        maybeAddWarning(resultOrNull, MSG_TOO_MANY_QUESTIONS, String.valueOf(JjDefaults.MAX_NUMBER_OF_QUESTIONS));
      }

      if (categories.size() > 1) {
        int numOfQuestions = categories.get(0).getQuestionsCount();
        for (int i = 1; i < categories.size(); i++) {
          if (numOfQuestions != categories.get(i).getQuestionsCount()) {
            maybeAddError(resultOrNull, MSG_NOT_MATCHING_QUESTIONS);
            isValid = false;
            break;
          }
        }
      }
    }
    return isValid;
  }

  /**
   * Determines bonus questions are valid - either no questions or at least min number of questions.
   * @param resultOrNull result object to add warning messages to; or null if not messages are needed
   * @return true bonus questions are valid
   */
  private boolean isBonusQuestionsDataValid(FileParsingResult resultOrNull) {
    if (this.bonusQuestions.isEmpty()) {
      return true;
    }
    if (this.bonusQuestions.size() < JjDefaults.MIN_NUMBER_OF_BONUS_QUESTIONS) {
      maybeAddWarning(resultOrNull, MSG_TOO_FEW_BONUS_QUESTIONS);
      return false;
    }
    if (this.bonusQuestions.size() > JjDefaults.MAX_NUMBER_OF_BONUS_QUESTIONS) {
      maybeAddWarning(resultOrNull, MSG_TOO_MANY_BONUS_QUESTIONS);
    }
    return true;
  }

  /**
   * Adds a warning message to the passed result object if it's not null.
   * @param resultOrNull result or null
   * @param msgNoCategories message to add
   * @param args arguments for the message
   */
  private static void maybeAddWarning(
      FileParsingResult resultOrNull, FileParsingResult.Message msgNoCategories, String... args) {
    if (resultOrNull != null) {
      resultOrNull.addWarningMessage(msgNoCategories, args);
    }
  }

  /**
   * Adds an error message to the passed result object if it's not null.
   * @param resultOrNull result or null
   * @param msgNoCategories message to add
   * @param args arguments for the message
   */
  private static void maybeAddError(
      FileParsingResult resultOrNull, FileParsingResult.Message msgNoCategories, String... args) {
    if (resultOrNull != null) {
      resultOrNull.addErrorMessage(msgNoCategories, args);
    }
  }
}
