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
import net.curre.jjeopardy.bean.Player;
import net.curre.jjeopardy.bean.Question;
import net.curre.jjeopardy.games.DefaultGames;
import net.curre.jjeopardy.util.JjDefaults;
import net.curre.jjeopardy.util.Utilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service to assist with player handling, keeping game scores,
 * handling questions.<br><br>
 * An instance of this service object should be obtained from the AppRegistry.
 *
 * @author Yevgeny Nyden
 */
public class GameDataService {

  /** Extension of the game bundle directory. */
  private static final String BUNDLE_EXTENSION = ".jj";

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(GameDataService.class.getName());

  /**
   * Max value for the parse loops, it's not really used since every loop stops with/by an exception,
   * but we need a large enough number for the parse for loop condition.
   */
  private static final int MAX_LOOP = 30;

  /** Name of the directory under settings where library games are stored. */
  private static final String GAME_DIRECTORY = "games";

  /** Current game data (game name, questions, categories, and optional players). */
  private GameData currentGameData;

  /** Current game players and their scores. */
  private final List<Player> currentPlayers;

  /** All known games in the game library. */
  private final List<GameData> libraryGames;

  /**
   * Ctor.
   */
  protected GameDataService() {
    this.currentPlayers = new ArrayList<>();
    this.libraryGames = new ArrayList<>();
  }

  /**
   * Determines if there is a current game data loaded.
   * @return true if there is a current game data loaded; false if otherwise
   */
  public boolean hasCurrentGameData() {
    return this.currentGameData != null;
  }

  /**
   * Gets the current game data. Note, that you must ensure there is a current game data loaded
   * before calling this method (with <code>#hasCurrentGameData</code>) or a runtime exception will
   * be thrown.
   * @return current game data
   * @throws RuntimeException when there is no current game data loaded
   * @see #hasCurrentGameData()
   */
  public GameData getCurrentGameData() {
    if (this.currentGameData == null) {
      throw new RuntimeException("Asking for current game data when there is none (use #hasCurrentGameData).");
    }
    return this.currentGameData;
  }

  /**
   * Sets the current game data.
    * @param gameData game data to set as the current game
   */
  public void setCurrentGameData(GameData gameData) {
    this.currentGameData = gameData;

    // If there are enough players, updating the current state.
    if (gameData.hasEnoughPlayers()) {
      this.currentPlayers.clear();
      List<String> playerNames = gameData.getPlayerNames();
      for (int ind = 0; ind < playerNames.size(); ind++) {
        this.currentPlayers.add(new Player(playerNames.get(ind), ind));
      }
    }
  }

  /**
   * Gets question from the games data.
   * @param catIndex category index for the question to fetch
   * @param questIndex question index for the question to fetch
   * @return the question data object
   */
  public Question getQuestion(int catIndex, int questIndex) {
    return this.currentGameData.getCategories().get(catIndex).getQuestion(questIndex);
  }

  /**
   * Adds a specified value to the player's current score.
   * @param playerIndex index of the player
   * @param value value to add
   */
  public void addToPlayerScore(int playerIndex, int value) {
    final Player player = this.currentPlayers.get(playerIndex);
    player.addScore(value);
  }

  /**
   * Resets all players scores.
   */
  public void resetPlayerScores() {
    for (Player player : this.currentPlayers) {
      player.resetScore();
    }
  }

  /**
   * Gets the winner of the game.
   * @return player with the most score
   */
  public Player getWinner() {
    Player winner = this.currentPlayers.get(0);
    for (Player player : this.currentPlayers) {
      if (player.getScore() > winner.getScore()) {
        winner = player;
      }
    }
    return winner;
  }

  /**
   * Gets the current players.
   * @return player objects (with scores)
   */
  public List<Player> getCurrentPlayers() {
    return this.currentPlayers;
  }

  /**
   * Determines if the game is ready to start, which means it has enough players
   * and enough game categories and questions.
   * @return true if we have enough data to start a game; false if otherwise
   */
  public boolean isGameReady() {
    // Do we have enough players?
    if (this.currentPlayers.size() < JjDefaults.MIN_NUMBER_OF_PLAYERS) {
      return false;
    }
    // Is there usable game data?
     if (this.currentGameData == null) {
       return false;
     }
    return this.currentGameData.isGameDataUsable();
  }

  /**
   * Copied default (packaged games) to the library folder (under settings) if
   * they have not been copied there already.
   */
  public static void copyDefaultGamesToLibraryIfNeeded() {
    Path gamesDir = Paths.get(getGameLibraryDirectoryPath());
    if (Files.exists(gamesDir)) {
      // If the games directory exists, assume the files have been copied there already.
      return;
    }
    try {
      gamesDir.toFile().mkdir();
      List<File> gameBundles = DefaultGames.getDefaultGameBundles();
      for (File originalBundle : gameBundles) {
        File destDir = new File(gamesDir.toString() + File.separatorChar + originalBundle.getName());
        destDir.mkdir();
        for (File file : originalBundle.listFiles()) {
          File destFile = new File(destDir.toString() + File.separatorChar + file.getName());
          FileUtils.copyFile(file, destFile);
        }
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Unable to copy default game files", e);
    }
  }

  /**
   * Loads valid library games from disk to memory. The games are expected to be
   * in the game settings directory nested in its own directory. Invalid (non-playable
   * games are ignored).
   * @return true if the loading was successful; false if otherwise
   */
  public boolean loadLibraryGames() {
    try {
      Path gamesDir = Paths.get(getGameLibraryDirectoryPath());
      if (!Files.exists(gamesDir)) {
        return false;
      }

      // Obtaining the list of files in the games directory.
      final List<File> gameBundles = new ArrayList<>();
      for (File gameFile : Objects.requireNonNull(gamesDir.toFile().listFiles())) {
        if (StringUtils.startsWith(gameFile.getName(), DefaultGames.class.getSimpleName())) {
          continue;
        }
        gameBundles.add(gameFile);
      }

      // Parsing and validating game files.
      for (File gameBundle : gameBundles) {
        GameData gameData = parseGameFileOrBundle(gameBundle.getAbsolutePath());
        if (gameData.isGameDataUsable()) {
          this.libraryGames.add(gameData);
        }
      }
      Collections.sort(this.libraryGames);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Checks if the game already exists in the library folder.
   * @param gameData game to check
   * @return true if the game already exists in the library folder
   */
  public boolean gameExistsInLibrary(GameData gameData) {
    Path libGamesDir = Paths.get(getGameLibraryDirectoryPath());
    if (gameData.getBundlePath() == null) {
      File originalFile = new File(gameData.getFilePath());
      File destFile = new File(libGamesDir.toString() + File.separatorChar + originalFile.getName());
      return destFile.exists();
    } else {
      File originalDir = new File(gameData.getBundlePath());
      File destDir = new File(libGamesDir.toString() + File.separatorChar + originalDir.getName());
      return destDir.exists();
    }
  }

  /**
   * Adds game to the game Library by copying original game files to the
   * settings game library folder if they don't exist there already.
   * @param gameData game to add
   */
  public void addGameToLibrary(GameData gameData) {
    if (gameData.isGameDataUsable()) {
      try {
        boolean updateLibrary = false;
        String gameFilePath = null;
        String gameBundlePath = null;
        Path libGamesDir = Paths.get(getGameLibraryDirectoryPath());

        if (gameData.getBundlePath() == null) {
          // If game is a single file, copy the file to the game library folder.
          File originalFile = new File(gameData.getFilePath());
          File destFile = new File(libGamesDir.toString() + File.separatorChar + originalFile.getName());
          if (!destFile.exists()) {
            FileUtils.copyFile(originalFile, destFile);
            updateLibrary = true;
          }
          gameFilePath = destFile.getAbsolutePath();
        } else {
          // If game has a bundle directory, check if the directory exists in the library.
          File bundleDir = new File(gameData.getBundlePath());
          File destDir = new File(libGamesDir.toString() + File.separatorChar + bundleDir.getName());
          if (!destDir.exists()) {
            // Create a bundle directory in the library folder and copy all content there.
            destDir.mkdir();
            for (File gameItem : Objects.requireNonNull(bundleDir.listFiles())) {
              File destFile = new File(destDir.toString() + File.separatorChar + gameItem.getName());
              FileUtils.copyFile(gameItem, destFile);
              if (StringUtils.endsWithIgnoreCase(gameItem.getName(), ".xml")) {
                gameFilePath = destFile.getAbsolutePath();
              }
            }
            updateLibrary = true;
            gameBundlePath = destDir.getAbsolutePath();
          }
        }

        if (updateLibrary) {
          // Update game file path on the current game data.
          gameData.setGameFilePaths(gameFilePath, gameBundlePath);

          // Update library games if a new game was added to the library folder.
          this.libraryGames.add(gameData);
          Collections.sort(this.libraryGames);
        }
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Unable to copy game file: " + gameData.getFilePath(), e);
      }
    }
  }

  /**
   * Deletes the given game from the library (memory and disk).
   * @param gameData game to delete
   */
  public void deleteGameFromLibrary(GameData gameData) {
    this.libraryGames.remove(gameData);
    Collections.sort(this.libraryGames);
    File gameFile = new File(gameData.getFilePath());
    try {
      FileUtils.delete(gameFile);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Unable to delete game file: " + gameData.getFilePath(), e);
    }
  }

  /**
   * Gets all known games.
   * @return all games data
   */
  public List<GameData> getLibraryGames() {
    return this.libraryGames;
  }

  /**
   * Loads game data from a given game file (xml) or a game bundle (directory with a .jj extension).
   * Note, that the parsed data is not validated and returned as is. No instance state on the service
   * object is modified as a result, so if this is the game to load, call <code>#setCurrentGameData</code> method.
   * @param fileName absolute path to the file or bundle directory
   * @return parsed game data
   * @see #setCurrentGameData(GameData)
   */
  public GameData parseGameFileOrBundle(String fileName) {
    File gameFile = new File(fileName);
    if (gameFile.isDirectory()) {
      for (File bundleFile : Objects.requireNonNull(gameFile.listFiles())) {
        if (StringUtils.endsWithIgnoreCase(bundleFile.getName(), ".xml")) {
          // It's assumed there is only one xml file in a game bundle.
          return parseGameData(bundleFile.getAbsolutePath(), gameFile.getAbsolutePath());
        }
      }
    } else {
      File parentDir = gameFile.getParentFile();
      String bundleDir = parentDir.getName().endsWith(BUNDLE_EXTENSION) ? parentDir.getAbsolutePath(): null;
      return parseGameData(fileName, bundleDir);
    }

    return new GameData(fileName, null);
  }

  /**
   * Loads game data from a given file. Note, that the parsed data is not validated and returned
   * as is. No instance state on the service object is modified as a result, so if this is the game
   * to load, call <code>#setCurrentGameData</code> method.
   * @param fileName absolute path to the file
   * @param bundleOrNull absolute path to the game bundle if the file is in a bundle; or null if it's a standalone file
   * @return parsed game data
   * @see #setCurrentGameData(GameData)
   */
  protected GameData parseGameData(String fileName, String bundleOrNull) {
    GameData gameData = new GameData(fileName, bundleOrNull);

    // Loading the game data from an XML file.
    Properties props = new Properties();
    try {
      InputStream in = Files.newInputStream(Paths.get(fileName));
      props.loadFromXML(in);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unable to open or parse XML file: " + fileName, e);
      return gameData;
    }
    gameData.setFileDataAcquired();

    // ******* First, required game name.
    try {
      String gameName = getProperty(props, "game.name");
      if (StringUtils.isBlank(gameName)) {
        LOGGER.severe("Game name is blank.");
      } else {
        gameData.setGameName(gameName.trim());
      }
    } catch (ServiceException e) {
      LOGGER.severe("Unable to parse game name.");
    }

    // ******* Optional game description.
    final String gameDescription;
    try {
      gameDescription = getProperty(props, "game.description");
      if (!StringUtils.isBlank(gameDescription)) {
        gameData.setGameDescription(gameDescription.trim());
      }
    } catch (ServiceException e) {
      // Ignore the error since description is optional.
    }

    // Now, parse categories and questions.
    gameData.setCategories(this.parseCategories(props));

    // ****** Now, parse optional data.
    gameData.setPlayersNames(this.parsePlayersDataIfAny(props));
    gameData.setBonusQuestions(this.parseBonusQuestionsIfAny(props));

    return gameData;
  }

  /**
   * Updates current game players using data from the settings UI.
   * @param playerNames player names from the player dialog
   */
  public void updateCurrentPlayers(List<String> playerNames) {
    this.currentPlayers.clear();
    for (int ind = 0; ind < playerNames.size() && ind < JjDefaults.MAX_NUMBER_OF_PLAYERS; ind++) {
      this.currentPlayers.add(new Player(playerNames.get(ind), ind));
    }
  }

  /**
   * Parses the core of the games data - questions organized into categories.
   * The parsed data has trimmed stings but returned not validated.
   * @param props properties file to parse data from
   * @return parsed categories (or empty list if none is parsed)
   */
  private List<Category> parseCategories(Properties props) {
    List<Category> categories = new ArrayList<>();
    try {
      // Notice that the (user facing) index starts at 1.
      for (int categoryNumber = 1; categoryNumber < MAX_LOOP; ++categoryNumber) {
        String categoryName = getProperty(props, "category." + categoryNumber + ".name");
        if (StringUtils.isBlank(categoryName)) {
          categoryName = "";
          LOGGER.severe("Category " + categoryNumber + " name is blank.");
        }
        List<Question> questions = parseQuestions(props, categoryNumber);
        categories.add(new Category(categoryName.trim(), questions));
      }
    } catch (ServiceException e) {
      // Stop at first category parsing error.
    }
    return categories;
  }

  /**
   * Parses questions for a specified category.
   * The parsed data has trimmed stings but returned not validated.
   * @param props properties to parse the data from
   * @param categoryNumber category number to parse the questions for
   * @return list of parsed questions (or none if parsed)
   */
  private List<Question> parseQuestions(Properties props, int categoryNumber) {
    List<Question> questions = new ArrayList<>();
    try {
      for (int questionNumber = 1; questionNumber < MAX_LOOP; questionNumber++) {
        int points;
        try {
          points = getIntProperty(props, "question." + questionNumber + ".points");
        } catch (ServiceException e) {
          points = questionNumber * JjDefaults.QUESTION_POINTS_MULTIPLIER;
        }
        final String question = getProperty(
            props, "category." + categoryNumber + ".question." + questionNumber);
        final String answer = getProperty(
            props, "category." + categoryNumber + ".answer." + questionNumber);
        if (!StringUtils.isBlank(question) && !StringUtils.isBlank(answer)) {
          questions.add(new Question(question.trim(), answer.trim(), points));
        }
      }
    } catch (Exception e) {
      // Stop on first error - no further questions for this category is defined.
    }
    return questions;
  }

  /**
   * Parses players from the passed game properties file.
   * Empty player names are skipped/ignored.
   * Note, that it doesn't update current game players - it's assumed
   * to be updated from the code that initiated parsing the file.
   * @param props  properties file
   * @return parsed valid player names list (or empty if none is parsed)
   */
  private List<String> parsePlayersDataIfAny(Properties props) {
    List<String> playerNames = new ArrayList<>();
    try {
      // Notice that the (user facing) index starts at 1.
      for (int playerNumber = 1; playerNumber < MAX_LOOP; playerNumber++) {
        String playerName = getProperty(props, "player." + playerNumber + ".name");
        if (!StringUtils.isBlank(playerName)) {
          playerNames.add(playerName.trim());
        }
      }
    } catch (ServiceException e) {
      // Stop on the first error, since there are no more players (or at all).
    }
    return playerNames;
  }

  /**
   * Parses bonus questions if found. Empty string values are ignored/skipped.
   * @param props  properties file
   * @return parsed valid bonus questions (or empty list if no questions are parsed)
   */
  private List<Question> parseBonusQuestionsIfAny(Properties props) {
    int bonusPoints;
    try {
      bonusPoints = getIntProperty(props, "bonus.question.points");
    } catch (ServiceException e) {
      bonusPoints = JjDefaults.BONUS_QUESTION_POINTS;
    }
    final List<Question> questions = new ArrayList<>();
    try {
      // Notice that the (user facing) index starts at 1.
      for (int questionNumber = 1; questionNumber < MAX_LOOP; questionNumber++) {
        String questionStr = getProperty(props, "bonus." + questionNumber + ".question");
        String answerStr = getProperty(props, "bonus." + questionNumber + ".answer");
        if (!StringUtils.isBlank(questionStr) && !StringUtils.isBlank(answerStr)) {
          questions.add(new Question(questionStr.trim(), answerStr.trim(), bonusPoints));
        }
      }
    } catch (ServiceException e) {
      // Stop on the first error, since there are no more questions (or at all).
    }
    return questions;
  }

  /**
   * Fetches a raw (non-trimmed) property from the given property object
   * and returns its String value.
   * @param props    property object to use
   * @param propName property name
   * @return property string value
   * @throws ServiceException if the property is not present
   */
  private static String getProperty(Properties props, String propName) throws ServiceException {
    final String propStr = props.getProperty(propName);
    if (propStr == null) {
      throw new ServiceException("String property \"" + propName + "\" is not found!");
    }
    return Utilities.removeEndsWhitespace(propStr);
  }

  /**
   * Fetches a property from the given property object
   * and returns its int value.
   * @param props    property object to use
   * @param propName property name
   * @return property int value
   * @throws ServiceException if the property is not present or does not represent an integer
   */
  private static int getIntProperty(Properties props, String propName) throws ServiceException {
    final String propStr = props.getProperty(propName);
    if (propStr == null) {
      throw new ServiceException("Int property \"" + propName + "\" is not found!");
    }
    try {
      return Integer.parseInt(propStr.trim());
    } catch (NumberFormatException e) {
      throw new ServiceException("Int property \"" + propName + "\" is not an integer!");
    }
  }

  /**
   * Returns an absolute path to the game library folder (under game settings).
   * Note, that the path may not exist yet.
   * @return absolute path to the games folder
   */
  private static String getGameLibraryDirectoryPath() {
    return SettingsService.getVerifiedSettingsDirectoryPath() + File.separatorChar + GAME_DIRECTORY;
  }
}
