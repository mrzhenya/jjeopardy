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

package net.curre.jjeopardy.images;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for ImageEnum.
 *
 * @author Yevgeny Nyden
 */
public class ImageEnumTest {

  /**
   * Initializes the state before each test run.
   */
  @Before
  public void init() {
  }

  /** Verifies that all image enums have valid corresponding image files. */
  @Test
  public void testAllImageEnumsHaveValidFiles() {
    for (ImageEnum imageEnum : ImageEnum.values()) {
      assertImageEnum(imageEnum);
    }
  }

  /**
   * Asserts image enum has valid assets.
   * @param imageEnum image enum to test
   */
  private static void assertImageEnum(ImageEnum imageEnum) {
    assertNotNull(imageEnum);
    assertFalse("File name should not be blank", StringUtils.isBlank(imageEnum.getFileName()));
    assertNotNull("Unable to get image icon", imageEnum.toImageIcon());
    assertNotNull("Unable to get image", imageEnum.toImage());
    assertFalse("toString should not be blank", StringUtils.isBlank(imageEnum.toString()));
  }
}
