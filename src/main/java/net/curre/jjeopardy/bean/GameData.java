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

import javax.validation.constraints.NotNull;
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
  private boolean fileDataAcquired;

  /** Indicates that game data was just created and has not been saved to the disk yet. */
  private boolean newGameData;

  /** Indicates that some images failed to download. */
  private boolean failedImageDownload;

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
    this.fileDataAcquired = false;
    this.newGameData = false; // default case - data to be acquired from file
    this.failedImageDownload = false;
    this.gameName = null;
    this.gameDescription = null;
    this.categories = new ArrayList<>();
    this.playerNames = new ArrayList<>();
    this.bonusQuestions = new ArrayList<>();
  }

  /**
   * Creates a copy/clone of this game data.
   * @return a new game data object identical to this game data
   */
  public @NotNull GameData createCopy() {
    GameData gameData = new GameData(this.filePath, this.bundlePath, this.nativeData);
    gameData.copyFrom(this);
    return gameData;
  }

  /**
   * Initializes this game data with the data from the passed gameData.
   * @param gameData game data to copy
   */
  public void copyFrom(@NotNull GameData gameData) {
    this.filePath = gameData.filePath;
    this.bundlePath = gameData.bundlePath;
    this.nativeData = gameData.nativeData;
    this.fileDataAcquired = gameData.fileDataAcquired;
    this.newGameData = gameData.newGameData;
    this.failedImageDownload = gameData.failedImageDownload;
    this.gameName = gameData.gameName;
    this.gameDescription = gameData.getGameDescription();
    this.categories.clear();
    for (Category category : gameData.categories) {
      this.categories.add(category.createCopy());
    }
    this.setPlayersNames(gameData.playerNames);
    this.bonusQuestions.clear();
    for (Question question : gameData.bonusQuestions) {
      this.bonusQuestions.add(question.createCopy());
    }
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
   * @return true if the name changed as a result of calling this method; false if otherwise
   */
  public boolean setGameName(String gameName) {
    boolean isChanged = !StringUtils.equals(gameName, this.gameName);
    this.gameName = gameName;
    return isChanged;
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
   * @return true if the description changed as a result of calling this method; false if otherwise
   */
  public boolean setGameDescription(String gameDescription) {
    boolean isChanged = !StringUtils.equals(gameDescription, this.gameDescription);
    this.gameDescription = gameDescription;
    return isChanged;
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
   * Removes a category (with its questions) from the game data.
   * @param categoryInd index of the category to remove (zero based)
   */
  public void removeCategory(int categoryInd) {
    this.categories.remove(categoryInd);
  }

  /**
   * Adds a new category (with blank questions) at the given index location.
   * @param categoryInd index at which a new category is added
   * @param categoryName category name
   * @param questionText question text to initialize the questions text to
   * @param answerText answer text to initialize the questions answer text to
   */
  public void addCategory(int categoryInd, String categoryName, String questionText, String answerText) {
    List<Question> newQuestions = new ArrayList<>();
    List<Question> questions = this.categories.get(categoryInd).getQuestions();
    for (Question question : questions) {
      newQuestions.add(
          new Question(questionText, null, answerText, null, question.getPoints()));
    }
    Category category = new Category(categoryName, newQuestions);
    this.categories.add(categoryInd, category);
  }

  /**
   * Adds a new question row at the given index and shifts other questions down.
   * Question points are also updated as a result - we retain the same question sequence
   * and assign the last row an estimated value. We assume the game has a minimum number
   * of questions.
   * @param rowInd index at which a new row of questions is to be added
   * @param questionText question text to initialize the questions text to
   * @param answerText answer text to initialize the questions answer text to
   */
  public void addQuestionRow(int rowInd, String questionText, String answerText) {
    for (Category category : this.categories) {
      // Assign the new question the points value from the original question at this position.
      final int points = category.getQuestion(rowInd).getPoints();
      category.getQuestions().add(
          rowInd, new Question(questionText, null, answerText, null, points));

      // Shift the points down.
      final int lastRowInd = category.getQuestionsCount() - 1;
      for (int ind = rowInd + 1; ind < lastRowInd; ind++) {
        category.getQuestion(ind).setPoints(category.getQuestion(ind + 1).getPoints());
      }

      // Estimate the points value for the last question.
      int lastRowPoints = category.getQuestion(lastRowInd).getPoints() +
          (category.getQuestion(lastRowInd - 1).getPoints() - category.getQuestion(lastRowInd - 2).getPoints());
      category.getQuestion(lastRowInd).setPoints(lastRowPoints);
    }
  }

  /**
   * Moves a category (with its questions) in the game data.
   * @param categoryInd index of the category to move (zero based)
   * @param toRight true if the index of the category should be increased; false if decreased
   */
  public void moveCategory(int categoryInd, boolean toRight) {
    if (toRight) {
      if (categoryInd + 1 >= this.categories.size()) {
        throw new IllegalArgumentException("Unable to increase category index " + categoryInd);
      }
    } else {
      if (categoryInd == 0) {
        throw new IllegalArgumentException("Unable to decrease category index 0");
      }
    }
    Category category = this.categories.remove(categoryInd);
    this.categories.add(categoryInd + (toRight ? 1 : -1), category);
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
   * Updates player names.
   * @param playerNames list of player names
   * @return true if the players count or their names changed as a result of calling this method; false if otherwise
   */
  public boolean setPlayersNames(List<String> playerNames) {
    boolean isChanged = this.playerNames.size() != playerNames.size();
    if (!isChanged) {
      for (int ind = 0; ind < playerNames.size(); ind++) {
        if (!playerNames.get(ind).equals(this.playerNames.get(ind))) {
          isChanged = true;
          break;
        }
      }
    }
    this.playerNames.clear();
    this.playerNames.addAll(playerNames);
    return isChanged;
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

  /** @inheritDoc */
  @SuppressWarnings("NullableProblems")
  @Override
  public int compareTo(@NotNull GameData other) {
    if (this == other) {
      return 0;
    }
    int compareValue = this.gameName.compareTo(other.gameName);
    if (compareValue == 0) {
      return this.filePath.compareTo(other.filePath);
    }
    return compareValue;
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
   * Gets the file data acquired flag on this game data.
   * @return true if the file data was acquired; false if otherwise
   */
  public boolean isFileDataAcquired() {
    return this.fileDataAcquired;
  }

  /**
   * Sets the file data acquired flag on this game data.
   */
  public void setFileDataAcquired() {
    this.fileDataAcquired = true;
  }

  /**
   * Determines if the game data was just created and has no game file
   * saved on disk yet.
   * @return true if there is no game file yet; false if otherwise
   */
  public boolean isGameDataNew() {
    return this.newGameData;
  }

  /**
   * Marks this game data as not new anymore, meaning that there is a corresponding file on the disk.
   */
  public void setGameDataNotNew() {
    this.newGameData = false;
  }

  /**
   * Initializes this game data with a minimum default number of questions/categories.
   * Each question is initialized with a default template string. The data is marked as
   * new, meaning that there is no corresponding file on the filesystem yet.
   * @param gameName the game name
   * @param gameDescription the game description
   */
  public void initializeNewGameData(String gameName, String gameDescription) {
    this.newGameData = true;
    this.fileDataAcquired = true;
    this.setGameName(gameName);
    this.setGameDescription(gameDescription);

    this.categories.clear();
    for (int catIndex = 0; catIndex < JjDefaults.MIN_NUMBER_OF_CATEGORIES; catIndex++) {
      List<Question> questions = new ArrayList<>();
      for (int questionInd = 0; questionInd < JjDefaults.MIN_NUMBER_OF_QUESTIONS; questionInd++) {
        int points = (questionInd + 1) * JjDefaults.QUESTION_POINTS_MULTIPLIER;
        Question question = new Question(
                LocaleService.getString("jj.game.question.placeholder"), null,
                LocaleService.getString("jj.game.answer.placeholder"), null, points);
        questions.add(question);
      }
      Category category = new Category(
              LocaleService.getString("jj.category.name", String.valueOf(catIndex + 1)), questions);
      this.categories.add(category);
    }
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
    if (!this.fileDataAcquired) {
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
   * Ensures the game has max categories and questions. If more categories or
   * questions are found than the max allowed, they are removed from this game data.
   */
  public void ensureMaxCategoriesAndQuestions() {
    while (this.categories.size() > JjDefaults.MAX_NUMBER_OF_CATEGORIES) {
      this.categories.remove(this.categories.size() - 1);
    }
    for (Category category : this.categories) {
      category.ensureMaxQuestionsCount();
    }
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
      } else if (this.categories.size() > JjDefaults.MAX_NUMBER_OF_CATEGORIES) {
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
