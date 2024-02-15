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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests Player object.
 *
 * @author Yevgeny Nyden
 */
public class PlayerTest {

  /** Tests initialization of the default object state. */
  @Test
  public void testDefault() {
    Player player = new Player("Abba", 3);
    assertEquals("Wrong player name", "Abba", player.getName());
    assertEquals("Wrong player index", 3, player.getIndex());
    assertEquals("Wrong default score", 0, player.getScore());
    assertNotNull("Player number string is null", player.getNumberString());
    assertTrue("Player number string should contain number", player.getNumberString().contains("4"));
    assertNotNull("Player name string is null", player.getNameString());
    assertTrue("Player number string should contain name", player.getNameString().contains("Abba"));
  }

  /** Tests score related logic. */
  @Test
  public void testPlayerScore() {
    Player player = new Player("Bubba", 0);
    assertEquals("Wrong default score", 0, player.getScore());
    player.addScore(22);
    assertEquals("Wrong score", 22, player.getScore());
    player.addScore(33);
    assertEquals("Wrong score", 55, player.getScore());
    player.resetScore();
    assertEquals("Wrong score after reset", 0, player.getScore());
  }
}