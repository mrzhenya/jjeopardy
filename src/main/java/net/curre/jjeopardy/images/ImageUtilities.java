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

import net.curre.jjeopardy.App;
import net.curre.jjeopardy.bean.Category;
import net.curre.jjeopardy.bean.GameData;
import net.curre.jjeopardy.bean.ImageTask;
import net.curre.jjeopardy.bean.Question;
import net.curre.jjeopardy.service.SettingsService;
import net.curre.jjeopardy.ui.dialog.ProgressDialog;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A set of utility method to assist with handling and downloading image files.
 *
 * @author Yevgeny Nyden
 */
public class ImageUtilities {

  /** Image download connection timeout in ms. */
  private static final int CONNECT_TIMEOUT_MS = 2000;

  /** Image download read timeout in ms. */
  private static final int READ_TIMEOUT_MS = 10000;

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(App.class.getName());

  /** File that represents temp image directory. */
  private static final File TEMP_IMAGE_PATH = new File(getTempImageDirectory());

  static {
    if (!TEMP_IMAGE_PATH.exists()) {
      //noinspection ResultOfMethodCallIgnored
      TEMP_IMAGE_PATH.mkdirs();
    }
  }

  /**
   * Gets the path to the temp image directory.
   * @return temp image directory path
   */
  public static String getTempImageDirectory() {
    return SettingsService.getVerifiedSettingsDirectoryPath() + File.separatorChar + "images";
  }

  /**
   * Downloads image files for the given game data into the bundle directory (according to the
   * bundle directory set on the game data). Image paths on the game data are also updated as
   * a result of calling this method.
   * @param gameData       game data
   * @param progressDialog progress dialog
   * @return list of failed image downloads (image urls)
   */
  public static List<String> downloadImagesAndUpdatePaths(GameData gameData, ProgressDialog progressDialog) {
    List<ImageTask> imageTasks = new ArrayList<>();

    // Get the image URLs from the regular questions.
    List<Category> categories = gameData.getCategories();
    for (Category category : categories) {
      for (int ind = 0; ind < category.getQuestionsCount(); ind++) {
        Question question = category.getQuestion(ind);
        if (question.getQuestionImage() != null) {
          imageTasks.add(new ImageTask(question.getQuestionImage(), question, true));
        }
        if (question.getAnswerImage() != null) {
          imageTasks.add(new ImageTask(question.getAnswerImage(), question, false));
        }
      }
    }

    // Get the image URLs from the bonus questions.
    for (Question question : gameData.getBonusQuestions()) {
      if (question.getQuestionImage() != null) {
        imageTasks.add(new ImageTask(question.getQuestionImage(), question, true));
      }
      if (question.getAnswerImage() != null) {
        imageTasks.add(new ImageTask(question.getAnswerImage(), question, false));
      }
    }

    return downloadImagesAndUpdateGameQuestions(gameData, imageTasks, progressDialog);
  }

  /**
   * Downloads an image to a temp image directory. The image is not downloaded if it's already
   * present in the temp image directory.
   * @param imageUrl image URL
   * @return image icon initialized with the downloaded image or null if unable to download the file
   */
  public static ImageIcon downloadTempImageResource(String imageUrl) {
    File imageFile = getImageTempFilePathFromUrl(imageUrl);
    if (!imageFile.exists()) {
      if (!downloadImage(imageUrl, imageFile)) {
        return null;
      }
    }
    return new ImageIcon(imageFile.getAbsolutePath());
  }

  /**
   * Creates a safe filepath out of a given string.
   * @param filename filename to convert
   * @return a MD5 hash string or the same string if encoding error is encountered
   */
  protected static String createHashFilename(String filename) {
    String hashString;
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] messageDigest = md.digest(filename.getBytes());

      BigInteger bigint = new BigInteger(1, messageDigest);
      String hexText = bigint.toString(16);
      while (hexText.length() < 32) {
        hexText = "0".concat(hexText);
      }
      hashString = hexText;
    } catch (NoSuchAlgorithmException e) {
      // In the unlikely scenario of missing MD5, return the same name.
      return filename;
    }
    return hashString;
  }

  /**
   * Gets the image temp file path given a url (where images should be temporarily downloaded to).
   * Note that the image name is converted to a safe MD5 hash string.
   * @param imageUrl image URL
   * @return temp image file path
   */
  protected static File getImageTempFilePathFromUrl(String imageUrl) {
    String imageFilename = FilenameUtils.getName(imageUrl);
    String hashFilename = createHashFilename(imageFilename);
    return new File(TEMP_IMAGE_PATH.getAbsolutePath() + File.separatorChar + hashFilename);
  }

  /**
   * Determines the extension of the image file.
   * @param file image file to determine extension of
   * @return image file extension ('jpg', 'png', etc.)
   */
  protected static String getImageExtension(File file) {
    try {
      // First, try reading it from the file name.
      String imageExt = FilenameUtils.getExtension(file.getName());
      switch (imageExt) {
        case "jpg":
        case "png":
        case "gif":
          return imageExt;
      }

      // Now, try reading it from the file.
      ImageInputStream inputStream = ImageIO.createImageInputStream(file);
      Iterator<ImageReader> iter = ImageIO.getImageReaders(inputStream);
      if (!iter.hasNext()) {
        return null;
      }
      ImageReader reader = iter.next();
      inputStream.close();
      if ("JPEG".equals(reader.getFormatName())) {
        return "jpg";
      }
      return reader.getFormatName();
    } catch (Exception e) {
      logger.log(Level.WARN, "Unable to determine image type of : " + file.getName(), e);
    }
    return null;
  }

  /**
   * Downloads images and updates game question image paths.
   * @param gameData game data
   * @param imageTasks list of triples of data (image URL, question, is bonus question)
   * @param progressDialog reference to the progress dialog
   * @return list failed downloads (image urls)
   */
  private static List<String> downloadImagesAndUpdateGameQuestions(
      GameData gameData, List<ImageTask> imageTasks, ProgressDialog progressDialog) {
    List<String> failedUrls = new ArrayList<>();
    int progressIncrement = imageTasks.isEmpty() ? 100 : ProgressDialog.FULL_PROGRESS / imageTasks.size();
    // Now, download images to the image bundle folder.
    for (ImageTask imageTask : imageTasks) {
      // If this game has already been played, the image files should have already been downloaded.
      // If not, download image to the temp location.
      File tempImagePath = getImageTempFilePathFromUrl(imageTask.getUrl());
      if (!tempImagePath.exists()) {
        if (!downloadImage(imageTask.getUrl(), tempImagePath)) {
          failedUrls.add(imageTask.getUrl());
          continue;
        }
      }

      // Now, copy the image from the temp to the game bundle directory.
      StringBuilder destFilename = new StringBuilder(createHashFilename(FilenameUtils.getName(imageTask.getUrl())));
      String ext = getImageExtension(tempImagePath);
      if (ext != null) {
        destFilename.append('.').append(ext);
      }
      File destFile = new File(gameData.getBundlePath() + File.separatorChar + destFilename);
      if (!destFile.exists()) {
        try {
          FileUtils.moveFile(tempImagePath, destFile);
        } catch (IOException e) {
          failedUrls.add(imageTask.getUrl());
          logger.log(Level.WARN, "Unable to copy file: " + tempImagePath, e);
          continue;
        }
      }

      // Update the image file name on the question.
      if (imageTask.isImageForQuestion()) {
        imageTask.getQuestion().setQuestionImage(destFilename.toString());
      } else {
        imageTask.getQuestion().setAnswerImage(destFilename.toString());
      }
      progressDialog.incrementProgress(progressIncrement);

    }
    for (String failedUrl : failedUrls) {
      logger.warn("Failed to download image " + failedUrl);
    }
    return failedUrls;
  }

  /**
   * Downloads an image given its URL.
   * @param imageUrl image URL
   * @return true if the image was downloaded successfully; false if otherwise
   */
  private static boolean downloadImage(String imageUrl, File imageFile) {
    try {
      if (!imageFile.exists()) {
        FileUtils.copyURLToFile(new URL(imageUrl), imageFile, CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS);
      }
      return true;
    } catch (Exception e) {
      logger.log(Level.WARN, "Unable to download image: " + imageUrl, e);
    }
    return false;
  }
}
