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
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Tests for ImageUtilities.
 *
 * @author Yevgeny Nyden
 */
public class ImageUtilitiesTest {

  /** Path to test image files. */
  private static final String TEST_PATH = "src/test/resources/images/";

  /** Tests getImageExtension. */
  @Test
  public void testGetImageExtension() {
    File image1 = new File(TEST_PATH + "test-image-1.jpg");
    assertEquals("Wrong image1 extension", "jpg", ImageUtilities.getImageExtension(image1));
    File image2 = new File(TEST_PATH + "doesnexist.jpg");
    assertEquals("Wrong image2 extension", "jpg", ImageUtilities.getImageExtension(image2));
    File image3 = new File(TEST_PATH + "test-image-1-copy");
    assertEquals("Wrong image3 extension", "jpg", ImageUtilities.getImageExtension(image3));
    File image4 = new File(TEST_PATH + "test-image-3.png");
    assertEquals("Wrong image4 extension", "png", ImageUtilities.getImageExtension(image4));
    File image5 = new File(TEST_PATH + "test-image-3-copy");
    assertEquals("Wrong image5 extension", "png", ImageUtilities.getImageExtension(image5));
    File image6 = new File(TEST_PATH + "test-image-4.gif");
    assertEquals("Wrong image6 extension", "gif", ImageUtilities.getImageExtension(image6));
    File image7 = new File(TEST_PATH + "test-image-4-copy");
    assertEquals("Wrong image7 extension", "gif", ImageUtilities.getImageExtension(image7));
  }

  /** Tests getTempImageDirectory. */
  @Test
  public void testGetTempImageDirectory() {
    String filepath = ImageUtilities.getTempImageDirectory();
    assertFalse("Path should not be blank", StringUtils.isBlank(filepath));
  }

  /** Tests createHashFilename. */
  @Test
  public void testCreateHashFilename() {
    String name1 = ImageUtilities.createHashFilename("test-image");
    assertNotNull("Filename 1 is null", name1);
    assertFalse("Filename 1 should not be blank", StringUtils.isBlank(name1));

    String name2 = ImageUtilities.createHashFilename("new-test-image");
    assertNotNull("Filename 2 is null", name2);
    assertFalse("Filename 2 should not be blank", StringUtils.isBlank(name2));

    assertNotEquals("Filenames 1 and 2 should be different", name1, name2);

    String name3 = ImageUtilities.createHashFilename("test-image");
    assertEquals("Filenames 1 and 3 should be the same", name1, name3);
  }

  /** Tests getImageTempFilePathFromUrl. */
  @Test
  public void testGetImageTempFilePathFromUrl() {
    File file = ImageUtilities.getImageTempFilePathFromUrl("some/image/path.jpg");
    assertNotNull("Returned file is null", file);
  }
}
