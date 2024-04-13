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

import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.bean.Player;
import net.curre.jjeopardy.bean.Question;
import net.curre.jjeopardy.games.DefaultGames;
import net.curre.jjeopardy.images.ImageUtilities;
import net.curre.jjeopardy.ui.dialog.ProgressDialog;
import net.curre.jjeopardy.ui.edit.EditGameWindow;
import net.curre.jjeopardy.util.JjDefaults;
import net.curre.jjeopardy.util.XmlFileUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Service responsible for handling an active game data. This service assists
 * with handling player names, keeping game scores, loading library games,
 * storing and copying game data/files, and other game data related tasks.<br><br>
 *
 * Game UI actions (such as starting a new game) are handled in the <code>GameService</code>.
 * <br><br>
 * An instance of this service object should be obtained from the AppRegistry.
 *
 * @see GameService
 * @author Yevgeny Nyden
 */
public class GameDataService {

  /** Extension of the game bundle directory. */
  private static final String BUNDLE_EXTENSION = ".jj";

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(GameDataService.class.getName());

  /** Name of the directory under settings where library games are stored. */
  private static final String GAME_DIRECTORY = "games";

  /** Current game data (game name, questions, categories, and optional players). */
  private GameData currentGameData;

  /** Current game players and their scores. */
  private final List<Player> currentPlayers;

  /** All known games in the game library. */
  private final List<GameData> libraryGames;

  /** Reference to the (game native format) XML parsing service. */
  private final XmlParsingService xmlParser;

  /** Reference to the HTML (game non-native format) parsing service. */
  private final HtmlParsingService htmlParser;

  /**
   * Ctor.
   */
  public GameDataService() {
    this.currentPlayers = new ArrayList<>();
    this.libraryGames = new ArrayList<>();
    this.xmlParser = new XmlParsingService();
    this.htmlParser = new HtmlParsingService();
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
  public void setCurrentGameData(@NotNull GameData gameData) {
    gameData.ensureMaxCategoriesAndQuestions();
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
   * Gets the current player names. Note that in most cases, <code>getCurrentPlayers</code>
   * method should be called.
   * @return player names
   * @see #getCurrentPlayers()
   */
  public @NotNull List<String> getCurrentPlayerNames() {
    List<String> playerNames = new ArrayList<>(this.currentPlayers.size());
    for (Player player : this.currentPlayers) {
      playerNames.add(player.getName());
    }
    return playerNames;
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
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static void copyDefaultGamesToLibraryIfNeeded() {
    Path gamesDir = Paths.get(getGameLibraryDirectoryPath());
    if (Files.exists(gamesDir)) {
      logger.info("It appears that the library games were copied already to: " + gamesDir);
      // If the games directory exists, assume the files have been copied there already.
      return;
    }
    logger.info("Copying the library games to: " + gamesDir);
    try {
      gamesDir.toFile().mkdir();
      DefaultGames.copyDefaultLibraryGames(gamesDir);
    } catch (Exception e) {
      logger.log(Level.ERROR, "Unable to copy default game files", e);
    }
    logger.info("Finished copying the library games");
  }

  /**
   * Determines if the game resides in the library.
   * @param gameData game data to check
   * @return true if the game file path is in the library directory
   */
  public boolean isLibraryGame(GameData gameData) {
    return FilenameUtils.directoryContains(getGameLibraryDirectoryPath(), gameData.getFilePath());
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
  public boolean gameExistsInLibrary(@NotNull GameData gameData) {
    Path libGamesDir = Paths.get(getGameLibraryDirectoryPath());
    if (gameData.isNativeData()) {
      // For the native data, check if the bundle or game file exist in the library.
      if (gameData.getBundlePath() == null) {
        File originalFile = new File(gameData.getFilePath());
        File destFile = new File(libGamesDir.toString() + File.separatorChar + originalFile.getName());
        return destFile.exists();
      } else {
        File originalDir = new File(gameData.getBundlePath());
        File destDir = new File(libGamesDir.toString() + File.separatorChar + originalDir.getName());
        return destDir.exists();
      }
    } else {
      // For non-native data, check for the library directory named the same as the file name (w/o extension).
      File originalFile = new File(gameData.getFilePath());
      String nameWoExtension = FilenameUtils.removeExtension(originalFile.getName());
      File destDir = new File(
          libGamesDir.toString() + File.separatorChar + nameWoExtension + BUNDLE_EXTENSION);
      return destDir.exists();
    }
  }

  /**
   * Adds game to the game Library by copying original game files to the
   * settings game library folder if they don't exist there already.
   *
   * @param gameData       game to add
   */
  public void addGameToLibrary(@NotNull GameData gameData) {
    if (!gameData.isGameDataUsable()) {
      // Don't add unusable games.
      logger.warn("Trying to add unusable game to the library: " + gameData.getFilePath());
    }
    try {
      if (gameData.isNativeData()) {
        copyNativeFormatGame(gameData);
      } else {
        copyNonNativeFormatGame(gameData);
      }
    } catch (Exception e) {
      logger.log(Level.WARN, "Unable to copy game file: " + gameData.getFilePath(), e);
    }
  }

  /**
   * Copies native formatted game (that's originated from a "game native" XML file or bundle)
   * to the game Library folder.
   *
   * @param gameData       game to add
   * @throws Exception on copy errors
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  private void copyNativeFormatGame(@NotNull GameData gameData) throws Exception {
    boolean isGameAdded = false;
    String gameFilePath = null;
    String gameBundlePath = null;
    Path libGamesDir = Paths.get(getGameLibraryDirectoryPath());
    if (gameData.getBundlePath() == null) {
      // If game is a single (native format) file, copy the file to the game library folder.
      File originalFile = new File(gameData.getFilePath());
      File destFile = new File(libGamesDir.toString() + File.separatorChar + originalFile.getName());
      if (!destFile.exists()) {
        FileUtils.copyFile(originalFile, destFile);
        gameFilePath = destFile.getAbsolutePath();
        isGameAdded = true;
      }
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
        isGameAdded = true;
        gameBundlePath = destDir.getAbsolutePath();
      }
    }

    if (isGameAdded) {
      // Update game file path on the current game data.
      gameData.setGameFilePaths(gameFilePath, gameBundlePath);
      updateLibraryGames(gameData);
    }
  }

  /**
   * Copies non-native formatted game (that's originated from a "3 party" file)
   * to the game Library folder.
   * @param gameData       game to add
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  private void copyNonNativeFormatGame(GameData gameData) {
    ProgressDialog progressDialog = new ProgressDialog(AppRegistry.getInstance().getLandingUi(),
        LocaleService.getString("jj.dialog.copy.title"),
        LocaleService.getString("jj.dialog.copy.header"));
    progressDialog.start(() -> {
      List<String> failedUrls = null;
      try {
        // Check if a bundle directory with the same name as the file
        File originalFile = new File(gameData.getFilePath());
        String nameWoExtension = FilenameUtils.removeExtension(originalFile.getName());
        Path libGamesDir = Paths.get(getGameLibraryDirectoryPath());
        File destDir = new File(
            libGamesDir.toString() + File.separatorChar + nameWoExtension + BUNDLE_EXTENSION);
        if (!destDir.exists()) {
          // Create a bundle directory in the library folder and copy all content there.
          destDir.mkdir();
          progressDialog.incrementProgress(1);

          // First, update game file path on the current game data.
          String destFilePath = destDir.toString() + File.separatorChar + nameWoExtension + ".xml";
          gameData.setGameFilePaths(destFilePath, destDir.getAbsolutePath());

          failedUrls = downloadImagesAndFinishGameCopy(gameData, destFilePath, progressDialog);
          progressDialog.completeAndFinish();
        }
      } catch (Exception e) {
        logger.log(Level.WARN, "Unable to copy files", e);
        progressDialog.finish();
      }
      maybeShowFailedDownloadDialog(failedUrls);
    });
  }

  /**
   * Downloads images for a given game data and finishes game copy.
   * @param gameData       game data
   * @param destFilePath   destination filepath of the game bundle
   * @param progressDialog progress dialog
   * @return list of failed downloads (image urls)
   */
  private @NotNull List<String> downloadImagesAndFinishGameCopy(
      GameData gameData, String destFilePath, ProgressDialog progressDialog) throws IOException {
    // Download the image files if any and update the image filename on the game data.
    List<String> imageUrls = ImageUtilities.downloadImagesAndUpdatePaths(gameData, progressDialog);

    if (!imageUrls.isEmpty()) {
      gameData.setImageDownloadFailure();
    }

    // Now, that the game data has been updated, create a game data file.
    XmlFileUtilities.createGameFile(gameData, destFilePath);
    gameData.changeToNativeData();
    updateLibraryGames(gameData);
    AppRegistry.getInstance().getLandingUi().updateLibrary();

    return imageUrls;
  }

  /**
   * Deletes the given game from the library (memory and disk).
   * @param gameData game to delete
   */
  public void deleteGameFromLibrary(@NotNull GameData gameData) {
    try {
      if (gameData.getBundlePath() == null) {
        File gameFile = new File(gameData.getFilePath());
        FileUtils.delete(gameFile);
      } else {
        File gameDir = new File(gameData.getBundlePath());
        FileUtils.deleteDirectory(gameDir);
      }
      this.libraryGames.remove(gameData);
      Collections.sort(this.libraryGames);
    } catch (IOException e) {
      logger.log(Level.WARN, "Unable to delete game file: " + gameData.getFilePath(), e);
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
   * Loads game data from a given game file (xml or html) or a game bundle (directory with a .jj extension).
   * Note, that the parsed data is not validated and returned as is. No instance state on the service
   * object is modified as a result, so if this is the game to load, call <code>#setCurrentGameData</code> method.
   * @param fileName absolute path to the file or bundle directory
   * @return parsed game data
   * @see #setCurrentGameData(GameData)
   */
  public GameData parseGameFileOrBundle(String fileName) {
    File gameFile = new File(fileName);
    if (gameFile.isDirectory()) {
      // Try to parse a game-native bundle directory.
      File[] files = Objects.requireNonNull(gameFile.listFiles());
      if (files.length == 0) {
        logger.warn("No files are found in bundle directory: " + fileName);
      }
      for (File bundleFile : files) {
        if (StringUtils.endsWithIgnoreCase(bundleFile.getName(), ".xml")) {
          // It's assumed there is only one xml file in a game bundle.
          return this.xmlParser.parseXmlGameData(bundleFile.getAbsolutePath(), gameFile.getAbsolutePath());
        }
      }
    } else if (StringUtils.endsWithIgnoreCase(gameFile.getName(), ".xml")) {
      File parentDir = gameFile.getParentFile();
      String bundleDir = parentDir.getName().endsWith(BUNDLE_EXTENSION) ? parentDir.getAbsolutePath(): null;
      return this.xmlParser.parseXmlGameData(fileName, bundleDir);
    } else if (StringUtils.endsWithIgnoreCase(gameFile.getName(), ".html")) {
      return this.htmlParser.parseJeopardyLabsHtmlFile(fileName);
    } else {
      logger.warn("Unsupported game file extension, ignoring file: " + fileName);
    }

    return new GameData(fileName, null, false);
  }

  /**
   * Saves the passed game.
   * @param gameData game data to save
   * @param editGameWindow reference to the parent game window
   */
  public void saveGameData(GameData gameData, EditGameWindow editGameWindow) {
    ProgressDialog progressDialog = new ProgressDialog(editGameWindow,
        LocaleService.getString("jj.dialog.save.title"),
        LocaleService.getString("jj.dialog.save.header"));
    progressDialog.start(() -> {
      List<String> failedUrls = null;
      // If game bundle is present, copy image files to the game bundle directory.
      if (gameData.getBundlePath() != null) {
        // Download the image files if any and update the image filename on the game data.
        failedUrls = ImageUtilities.downloadImagesAndUpdatePaths(gameData, progressDialog);
        ImageUtilities.copyImageFilesToGameBundle(gameData);
      }
      // Now, overwrite the game file with the updated game data.
      try {
        XmlFileUtilities.createGameFile(gameData, gameData.getFilePath());
      } catch (IOException e) {
        logger.error("Unable to create a game file " + gameData.getFilePath());
      }
      progressDialog.completeAndFinish();
      maybeShowFailedDownloadDialog(failedUrls);
    });
  }

  /**
   * Updates current game players using data from the settings UI. Note that
   * the current player state (including players' scores) will be erased as a result
   * of calling this method.
   * @param playerNames player names from the player dialog
   */
  public void updateCurrentPlayers(@NotNull List<String> playerNames) {
    this.currentPlayers.clear();
    for (int ind = 0; ind < playerNames.size() && ind < JjDefaults.MAX_NUMBER_OF_PLAYERS; ind++) {
      this.currentPlayers.add(new Player(playerNames.get(ind), ind));
    }
  }

  /**
   * Returns an absolute path to the game library folder (under game settings).
   * Note, that the path may not exist yet.
   * @return absolute path to the games folder
   */
  protected static @NotNull String getGameLibraryDirectoryPath() {
    return SettingsService.getVerifiedSettingsDirectoryPath() + File.separatorChar + GAME_DIRECTORY;
  }

  /**
   * Updates library games when a new game was added to the library folder.
   * @param gameData game data
   */
  private void updateLibraryGames(GameData gameData) {
    this.libraryGames.add(gameData);
    Collections.sort(this.libraryGames);
  }

  /**
   * If there are failed images, shows failed image download dialog.
   * @param failedUrls list of failed image downloads
   */
  private void maybeShowFailedDownloadDialog(List<String> failedUrls) {
    if (failedUrls != null && !failedUrls.isEmpty()) {
      StringBuilder failedMessage = new StringBuilder(LocaleService.getString("jj.dialog.copy.failed.message") + "\n\n");
      for (String failedUrl : failedUrls) {
        failedMessage.append(failedUrl).append("\n");
      }
      AppRegistry.getInstance().getUiService().showWarningDialog(
          LocaleService.getString("jj.dialog.copy.failed.title"),
          failedMessage.toString(),
          AppRegistry.getInstance().getLandingUi()
      );
    }
  }
}
