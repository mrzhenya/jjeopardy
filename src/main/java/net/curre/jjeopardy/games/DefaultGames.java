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

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Serves as an anchor to the default games resource folder and a utility
 * to load the default games.
 *
 * @author Yevgeny Nyden
 */
public class DefaultGames {

  /**
   * Loads game data files from the default games folder.
   * @return a list of File objects representing default game data files
   * @throws URISyntaxException on loading error
   */
  public static List<File> getDefaultGameFiles() throws URISyntaxException {
    final List<File> files = new ArrayList<>();
    // Obtaining the list of files in the games directory.
    URL resource = DefaultGames.class.getResource("");
    for (File gameFile : Objects.requireNonNull(Paths.get(resource.toURI()).toFile().listFiles())) {
      if (StringUtils.startsWith(gameFile.getName(), DefaultGames.class.getSimpleName())) {
        continue;
      }
      files.add(gameFile);
    }
    return files;
  }
}
