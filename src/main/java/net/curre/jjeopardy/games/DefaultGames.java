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

package net.curre.jjeopardy.games;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Serves as an anchor to the default games resource folder and a utility
 * to copy default games (packaged with the game) to the games library folder.
 *
 * @author Yevgeny Nyden
 */
public class DefaultGames {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(DefaultGames.class.getName());

  /**
   * Copies prepackaged games from the resource directory (or a jar) to the library
   * games folder. Only game bundles (not single game files) are supported.
   * @param libGamesDir an absolute path to the game library folder (under game settings)
   * @throws URISyntaxException on URI syntax errors
   * @throws IOException on input/output errors
   */
  public static void copyDefaultLibraryGames(Path libGamesDir) throws URISyntaxException, IOException {
    // Obtaining the list of game bundles (directories) in the games directory.
    URL gameDirResource = DefaultGames.class.getResource("");

    if (gameDirResource == null) {
      logger.error("Unable to find prepackaged library games resource");
    } else if (gameDirResource.getPath().startsWith("file:")) {
      // Running in prod mode (reading from jar).
      String rootPath = StringUtils.split(gameDirResource.getPath(), "!")[1].substring(1);
      String jarPath = StringUtils.substringBetween(gameDirResource.getPath(), "file:", "!");
      copyFromJarFile(jarPath, rootPath, libGamesDir);
    } else {
      // Running in dev mode (reading from files).
      File sourceDir = Paths.get(gameDirResource.toURI()).toFile();
      copyFromFileSystem(sourceDir, libGamesDir);
    }
  }

  /**
   * Copies game bundles from the jar to the specified library location.
   * @param jarPath absolute path to the game jar file
   * @param gamesResourcePath resource path to the games data
   * @param libGamesDir absolute path to the games library folder
   * @throws IOException on input/output errors
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  private static void copyFromJarFile(
      String jarPath, @NotNull String gamesResourcePath, Path libGamesDir) throws IOException {
    logger.info("Opening the app jar: " + jarPath);
    FileInputStream fileInputStream = new FileInputStream(jarPath);
    BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
    ZipInputStream zipInputStream = new ZipInputStream(bufferedInputStream);

    // Go over the zip file entries and copy the ones that match our pattern.
    int rootPathCharPivot = gamesResourcePath.length();
    ZipEntry zipEntry = null;
    while ((zipEntry = zipInputStream.getNextEntry()) != null) {
      if (zipEntry.getName().startsWith(gamesResourcePath)) {
        logger.debug(":::: resource " + zipEntry.getName().substring(rootPathCharPivot));
        String gameResource = zipEntry.getName().substring(rootPathCharPivot);
        String[] parts = gameResource.split("/");
        // Only pick paths that have both 'bundle' and 'file' (game data file).
        if (parts.length == 2) {
          String bundleName = parts[0];
          String fileName = parts[1];
          File destDir = new File(libGamesDir.toString() + File.separatorChar + bundleName);
          if (!destDir.exists()) {
            destDir.mkdir();
          }
          String destFileName = destDir.toString() + File.separatorChar + fileName;
          extractZipEntry(zipInputStream, destFileName);
        } else if (parts.length > 2) {
          // TODO: add support for images ('bundle' / 'games' / 'file').
          logger.warn("Library games with directories are not supported yet: " + gameResource);
        }
      }
    }
    zipInputStream.close();
  }

  /**
   * Extracts a single zip file entry/file - copies it to the destination filepath.
   * @param zipInputStream zip input stream to read
   * @param destFilePath absolute path to the filename where zip entry is copied to
   * @throws IOException on input/output errors
   */
  private static void extractZipEntry(@NotNull ZipInputStream zipInputStream, String destFilePath) throws IOException {
    OutputStream outStream = Files.newOutputStream(Paths.get(destFilePath));
    byte[] buffer = new byte[9000];
    int length;
    while ((length = zipInputStream.read(buffer)) != -1) {
      outStream.write(buffer, 0, length);
    }
    outStream.close();
  }

  /**
   * Copies prepackaged games from the filesystem to the games library folder.
   * @param sourceDir source directory where library game bundles are located
   * @param libGamesDir an absolute path to the game library folder (under game settings)
   *                    where games are copied to
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  private static void copyFromFileSystem(@NotNull File sourceDir, Path libGamesDir) throws IOException {
    // List the game bundles in the resources games directory.
    final List<File> gameBundles = new ArrayList<>();
    for (File gameBundle : Objects.requireNonNull(sourceDir.listFiles())) {
      if (gameBundle.isFile()) {
        // Skipping any non-directory item.
        continue;
      }
      gameBundles.add(gameBundle);
    }

    // Copy resource game directories and their content to the library games directory.
    for (File originalBundle : gameBundles) {
      File destDir = new File(libGamesDir.toString() + File.separatorChar + originalBundle.getName());
      destDir.mkdir();
      for (File file : Objects.requireNonNull(originalBundle.listFiles())) {
        File destFile = new File(destDir.toString() + File.separatorChar + file.getName());
        FileUtils.copyFile(file, destFile);
      }
    }
  }
}
