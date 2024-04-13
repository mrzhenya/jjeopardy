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
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests the game data service - setting player names, scores.
 * We also test some parsing here, related to the bundle parsing. Most parsing logic
 * is tested in <code>XmlParsingServiceTest</code>.
 *
 * @author Yevgeny Nyden
 */
public class GameDataServiceTest {

  /**
   * Reference to the game data service to test on each run.
   */
  private GameDataService testGameService;

  /**
   * Initializes the state before each test run.
   */
  @Before
  public void init() {
    this.testGameService = new GameDataService();
  }

  /**
   * Tests the initial service state.
   */
  @Test
  public void initialRun() {
    assertFalse("Game should not be ready", this.testGameService.isGameReady());
    List<Player> players = this.testGameService.getCurrentPlayers();
    assertNotNull("List of players should not be null", players);
    assertEquals("Players list should be empty", 0, players.size());
    assertFalse("There should be no game data", this.testGameService.hasCurrentGameData());
  }

  /**
   * Tests updating and retrieving the players/names.
   */
  @Test
  public void testUpdatePlayersFromNames() {
    List<String> playerNames = createDefaultPlayers();
    this.testGameService.updateCurrentPlayers(playerNames);
    List<Player> players = this.testGameService.getCurrentPlayers();
    assertNotNull("List of players should not be null", players);
    assertEquals("Wrong number of players", 3, players.size());
    assertPlayer("Wrong player 0", players.get(0), 0, "One", 0);
    assertPlayer("Wrong player 1", players.get(1), 1, "Two", 0);
    assertPlayer("Wrong player 2", players.get(2), 2, "Three", 0);
    assertFalse("Game should still not be ready", this.testGameService.isGameReady());

    // Test converting players to player names.
    List<String> newPlayerNames = this.testGameService.getCurrentPlayerNames();
    assertNotNull("List of players names should not be null", newPlayerNames);
    assertEquals("Wrong number of player names", 3, newPlayerNames.size());
    assertEquals("Wrong player 0", "One", newPlayerNames.get(0));
    assertEquals("Wrong player 1", "Two", newPlayerNames.get(1));
    assertEquals("Wrong player 2", "Three", newPlayerNames.get(2));

    // Check if the player scores are reset after calling the setter.
    this.testGameService.updateCurrentPlayers(playerNames);
    players.get(0).addScore(10);
    players.get(1).addScore(20);
    players.get(2).addScore(30);
    players = this.testGameService.getCurrentPlayers();
    assertEquals("Wrong player 0 score", 10, players.get(0).getScore());
    assertEquals("Wrong player 1 score", 20, players.get(1).getScore());
    assertEquals("Wrong player 2 score", 30, players.get(2).getScore());
    this.testGameService.updateCurrentPlayers(playerNames);

    players = this.testGameService.getCurrentPlayers();
    assertEquals("Wrong number of players", 3, players.size());
    assertPlayer("Wrong player 0", players.get(0), 0, "One", 0);
    assertPlayer("Wrong player 1", players.get(1), 1, "Two", 0);
    assertPlayer("Wrong player 2", players.get(2), 2, "Three", 0);

    // This should reset the players.
    playerNames = new ArrayList<>();
    playerNames.add("Four");
    this.testGameService.updateCurrentPlayers(playerNames);
    players = this.testGameService.getCurrentPlayers();
    assertNotNull("List of players should not be null", players);
    assertEquals("Wrong number of players", 1, players.size());
    assertPlayer("Wrong player 0", players.get(0), 0, "Four", 0);
  }

  /**
   * Tests updating and resetting the players scores.
   */
  @Test
  public void testAdjustingPlayerScores() {
    List<String> playerNames = createDefaultPlayers();
    this.testGameService.updateCurrentPlayers(playerNames);
    this.testGameService.addToPlayerScore(2, 222);
    this.testGameService.addToPlayerScore(1, 111);
    this.testGameService.addToPlayerScore(0, 0);
    List<Player> players = this.testGameService.getCurrentPlayers();
    assertPlayer("Wrong player 0", players.get(0), 0, "One", 0);
    assertPlayer("Wrong player 1", players.get(1), 1, "Two", 111);
    assertPlayer("Wrong player 2", players.get(2), 2, "Three", 222);

    // Adding more.
    this.testGameService.addToPlayerScore(0, 10);
    assertPlayer("Wrong player 0", players.get(0), 0, "One", 10);
    this.testGameService.addToPlayerScore(1, 20);
    assertPlayer("Wrong player 1", players.get(1), 1, "Two", 131);

    // Testing full scores reset.
    this.testGameService.resetPlayerScores();
    players = this.testGameService.getCurrentPlayers();
    assertPlayer("Wrong player 0", players.get(0), 0, "One", 0);
    assertPlayer("Wrong player 1", players.get(1), 1, "Two", 0);
    assertPlayer("Wrong player 2", players.get(2), 2, "Three", 0);
  }

  /**
   * Tests updating the players scores.
   */
  @Test
  public void testWinnerPlayer() {
    List<String> playerNames = createDefaultPlayers();
    this.testGameService.updateCurrentPlayers(playerNames);
    this.testGameService.addToPlayerScore(1, 101);
    assertPlayer("Wrong winner 1", this.testGameService.getWinner(), 1, "Two", 101);

    this.testGameService.addToPlayerScore(2, 202);
    assertPlayer("Wrong winner 2", this.testGameService.getWinner(), 2, "Three", 202);

    this.testGameService.addToPlayerScore(1, 200);
    assertPlayer("Wrong winner 1", this.testGameService.getWinner(), 1, "Two", 301);
  }

  /**
   * Tests the isLibraryGame method.
   */
  @Test
  public void testIsLibraryGame() {
    String libPath = GameDataService.getGameLibraryDirectoryPath();
    GameData testGameData = new GameData(
        libPath + File.separatorChar + "somefile.xml", null, true);
    assertTrue("The game should be in the library", this.testGameService.isLibraryGame(testGameData));

    // Just in case, test one level down.
    GameData testGameData2 = new GameData(
        libPath + File.separatorChar + "dir" + File.separatorChar + "somefile.xml", null, true);
    assertTrue("The game should be in the library", this.testGameService.isLibraryGame(testGameData2));

    GameData testGameData3 = new GameData("somefile.xml", null, true);
    assertFalse("The game should not be in the library", this.testGameService.isLibraryGame(testGameData3));
  }

  /**
   * Tests loading game data from a valid game file.
   */
  @Test
  public void testLoadGameDataValidDefault() {
    loadGameTestFile(XmlParsingServiceTest.PATH_VALID + "default.xml");

    // There should still be no players.
    List<Player> players = this.testGameService.getCurrentPlayers();
    assertNotNull("List of players should not be null", players);
    assertEquals("Wrong number of players", 0, players.size());
  }

  /**
   * Tests loading game data from a valid game file.
   */
  @Test
  public void testLoadGameBundleReadingFromFile() {
    GameData gameData = loadGameTestFile(XmlParsingServiceTest.PATH_VALID + "/test-bundle.jj/test-bundle.xml");
    assertEquals("Wrong file path",
            new File(XmlParsingServiceTest.PATH_VALID + "/test-bundle.jj/test-bundle.xml").getAbsolutePath(), gameData.getFilePath());
    assertEquals("Wrong bundle directory",
        new File(XmlParsingServiceTest.PATH_VALID + "/test-bundle.jj").getAbsolutePath(), gameData.getBundlePath());

    XmlParsingServiceTest.assertDefaultValidData(gameData, "valid-default", "test-description",
            10, 20, 30, 0, 0);
    assertTrue("Game data should be usable", gameData.isGameDataUsable());
  }

  /**
   * Tests loading game data from a valid game file.
   */
  @Test
  public void testLoadGameBundleReadingFromDirectory() {
    GameData gameData = loadGameTestFile(XmlParsingServiceTest.PATH_VALID + "/test-bundle.jj");
    assertEquals("Wrong file path",
            new File(XmlParsingServiceTest.PATH_VALID + "/test-bundle.jj/test-bundle.xml").getAbsolutePath(), gameData.getFilePath());
    assertEquals("Wrong bundle directory",
            new File(XmlParsingServiceTest.PATH_VALID + "/test-bundle.jj").getAbsolutePath(), gameData.getBundlePath());

    XmlParsingServiceTest.assertDefaultValidData(gameData, "valid-default", "test-description",
            10, 20, 30, 0, 0);
    assertTrue("Game data should be usable", gameData.isGameDataUsable());
  }

  /**
   * Tests loading game data from a valid HTML game file.
   */
  @Test
  public void testLoadGameFileReadingFromHtmlFile() {
    GameData gameData = loadGameTestFile(HtmlParsingServiceTest.TEST_DIR_PATH + "valid-simple.html");
    assertEquals("Wrong file path",
        new File(HtmlParsingServiceTest.TEST_DIR_PATH + "valid-simple.html").getAbsolutePath(),
        gameData.getFilePath());
    assertNull("Bundle directory should be null", gameData.getBundlePath());
    assertEquals("Wrong game name", "Test Jlabs Game", gameData.getGameName());
    // No more testing is necessary, we got the file (more tests are in HtmlParsingServiceTest).
  }

  /**
   * Asserts the player object's state.
   * @param message string to add to the error messages
   * @param player player object to test
   * @param index expected player's index
   * @param name expected player's name
   * @param score expected player's score
   */
  private static void assertPlayer(String message, Player player, int index, String name, int score) {
    assertNotNull(message + "; Player should not be null", player);
    assertEquals(message + "; Wrong player name", name, player.getName());
    assertEquals(message + "; Wrong player index", index, player.getIndex());
    assertEquals(message + "; Wrong player score", score, player.getScore());
    assertNotNull(message + "; Player name string should not be null", player.getNameString());
    assertEquals(message + "; Player name string should contain player name",
            1, StringUtils.countMatches(player.getNameString(), name));
  }

  /**
   * Loads game test file.
   * @param fileName filename/path
   * @return result object
   */
  private GameData loadGameTestFile(String fileName) {
    File file = new File(fileName);
    assertTrue("Unable to find test file: " + fileName, file.exists());
    GameData gameData = this.testGameService.parseGameFileOrBundle(file.getAbsolutePath());
    assertNotNull("Parsing result should not be null", gameData);
    return gameData;
  }

  /**
   * Creates a list with three default player names.
   * @return list of player name strings
   */
  private static List<String> createDefaultPlayers() {
    List<String> playerNames = new ArrayList<>();
    playerNames.add("One");
    playerNames.add("Two");
    playerNames.add("Three");
    return playerNames;
  }
}
