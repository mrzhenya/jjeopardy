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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for the Utilities class.
 *
 * @author Yevgeny Nyden
 */
public class UtilitiesTest {

  /** Initializes the state before each test run. */
  @Before
  public void init() {
  }

  /** Tests getPlatformType. */
  @Test
  public void testGetPlatformType() {
    assertNotNull("Platform type should not be null", Utilities.getPlatformType());
  }
}
