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

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests for JjDefaults.
 *
 * @author Yevgeny Nyden
 */
public class JjDefaultsTest {

  /**
   * Tests initialization of the default object state.
   */
  @Test
  public void testDefault() {
    assertTrue(JjDefaults.MAX_NUMBER_OF_PLAYERS > 0);
    assertTrue(JjDefaults.MIN_NUMBER_OF_PLAYERS > 0);
    assertTrue(JjDefaults.MAX_NUMBER_OF_CATEGORIES > 0);
    assertTrue(JjDefaults.MIN_NUMBER_OF_CATEGORIES > 0);
    assertTrue(JjDefaults.MAX_NUMBER_OF_QUESTIONS > 0);
    assertTrue(JjDefaults.MIN_NUMBER_OF_QUESTIONS > 0);
    assertTrue(JjDefaults.QUESTION_POINTS_MULTIPLIER > 0);
    assertTrue(JjDefaults.MAX_NUMBER_OF_BONUS_QUESTIONS > 0);
    assertTrue(JjDefaults.BONUS_QUESTION_POINTS > 0);
    assertTrue(JjDefaults.LANDING_UI_WIDTH > 0);
    assertTrue(JjDefaults.LANDING_UI_LIBRARY_HEIGHT > 0);
    assertTrue(JjDefaults.GAME_TABLE_MIN_WIDTH > 0);
    assertTrue(JjDefaults.GAME_TABLE_MIN_HEIGHT > 0);
    assertTrue(JjDefaults.GAME_TABLE_MIN_ROW_HEIGHT > 0);
    assertTrue(JjDefaults.GAME_TABLE_HEADER_HEIGHT > 0);
  }
}
