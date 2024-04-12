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

package net.curre.jjeopardy.ui.edit;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.event.ButtonMouseHoverListener;
import net.curre.jjeopardy.event.ClickAndKeyAction;
import net.curre.jjeopardy.images.ImageEnum;
import net.curre.jjeopardy.images.ImageUtilities;
import net.curre.jjeopardy.service.LocaleService;
import net.curre.jjeopardy.util.JjDefaults;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.validation.constraints.NotNull;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

/**
 * Represents an image picker panel with the image and the image select UI.
 *
 * @author Yevgeny Nyden
 */
public class ImagePickerPanel extends JPanel {

  /** Private class logger. */
  private static final Logger logger = LogManager.getLogger(ImagePickerPanel.class.getName());

  /** Image preferred height. */
  private static final int IMAGE_HEIGHT = 130;

  /** Path to the game bundle or null if none set. */
  private final String gameBundlePath;

  /** Image filename input field. */
  private JTextField imageInputField;

  /** Reference to the image label. */
  private final JLabel imageLabel;

  /** Indicates an invalid image (image path is set AND the image fails to load). */
  private boolean invalidImage;

  /**
   * Ctor.
   * @param imageFilepath image filepath (either absolute or relative to the game bundle when it's set) or null
   * @param gameBundlePath path to the game bundle or null if none set
   */
  public ImagePickerPanel(String imageFilepath, String gameBundlePath) {
    this.gameBundlePath = gameBundlePath;
    this.invalidImage = false;

    this.setLayout(new TableLayout(new double[][] {
        {TableLayout.FILL},  // columns
        {10, TableLayout.FILL, 10, TableLayout.PREFERRED}}));  // rows

    // ***** Label where the image is rendered when present.
    this.imageLabel = new JLabel();
    this.setImageLabel(imageFilepath);
    this.add(this.imageLabel, new TableLayoutConstraints(
        0, 1, 0, 1, TableLayout.CENTER, TableLayout.CENTER));

    // ***** Image file name/path/url input UI.
    JPanel inputPanel = this.createInputPanel(imageFilepath);
    this.add(inputPanel, new TableLayoutConstraints(
        0, 3, 0, 3, TableLayout.FULL, TableLayout.FULL));
  }

  /**
   * Gets the image path if it's set.
   * @return the image path or null if it's not set
   */
  protected String getImagePathOrNull() {
    return StringUtils.stripToNull(this.imageInputField.getText());
  }

  /**
   * Indicates that an image path is set but the image failed to load.
   * @return true if the image path is set AND the image failed to load
   */
  protected boolean isInvalidImage() {
    return this.invalidImage;
  }

  /**
   * Sets an appropriate image icon on the image label.
   * @param imageFilepathOrUrl image url or filepath (either absolute or relative to the game bundle when it's set) or null
   */
  private void setImageLabel(String imageFilepathOrUrl) {
    this.invalidImage = false;
    if (imageFilepathOrUrl == null) {
      this.imageLabel.setIcon(ImageEnum.IMAGE_64.toImageIcon());
      this.imageLabel.setPreferredSize(new Dimension(64, 64));
    } else {
      String bundlePath = null;
      if (!imageFilepathOrUrl.startsWith("http")) {
        File imageFile = new File(imageFilepathOrUrl);
        if (!imageFile.exists()) {
          bundlePath = this.gameBundlePath;
        }
      }
      boolean success = ImageUtilities.updateLabelIconImage(
          this.imageLabel, imageFilepathOrUrl, bundlePath,
          JjDefaults.EDIT_DIALOG_MIN_WIDTH - 100, IMAGE_HEIGHT);
      if (!success) {
        this.invalidImage = true;
        this.imageLabel.setIcon(ImageEnum.IMAGE_FAILURE_64.toImageIcon());
        this.imageLabel.setPreferredSize(new Dimension(64, 64));
      }
    }
  }

  /**
   * Creates the image selection panel UI.
   * @param imageFilepath image filepath (either absolute or relative to the game bundle when it's set)
   * @return image selection UI
   */
  private @NotNull JPanel createInputPanel(String imageFilepath) {
    JPanel inputPanel = new JPanel();
    inputPanel.setLayout(new TableLayout(new double[][] {
        {5, TableLayout.PREFERRED, 5, TableLayout.FILL, 5, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 5},  // columns
        {5, TableLayout.PREFERRED, 5}}));  // rows

    // ***** Image input line header.
    JLabel titleLabel = new JLabel(LocaleService.getString("jj.editdialog.image.header"));
    inputPanel.add(titleLabel, new TableLayoutConstraints(
        1, 1, 1, 1, TableLayout.CENTER, TableLayout.CENTER));

    // ***** Image file path/name/url input text field.
    this.imageInputField = new JTextField();
    if (imageFilepath != null) {
      this.imageInputField.setText(imageFilepath);
    }
    this.imageInputField.addKeyListener(new ImageInputFieldKeyListener(this));
    inputPanel.add(this.imageInputField, new TableLayoutConstraints(
        3, 1, 3, 1, TableLayout.FULL, TableLayout.CENTER));

    // ***** Select image file button.
    JButton selectButton = new JButton();
    selectButton.addMouseListener(new ButtonMouseHoverListener(
        ImageEnum.OPEN_FILE_24.toImageIcon(), ImageEnum.OPEN_FILE_24_HOVER.toImageIcon()));
    ClickAndKeyAction.createAndAddAction(selectButton, this::handleSelectFileAction);
    selectButton.setIcon(ImageEnum.OPEN_FILE_24.toImageIcon());
    inputPanel.add(selectButton, new TableLayoutConstraints(
        5, 1, 5, 1, TableLayout.CENTER, TableLayout.CENTER));

    // ***** Delete image button.
    JButton deleteButton = new JButton();
    deleteButton.addMouseListener(new ButtonMouseHoverListener(
        ImageEnum.TRASH_24.toImageIcon(), ImageEnum.TRASH_24_HOVER.toImageIcon()));
    ClickAndKeyAction.createAndAddAction(deleteButton, this::handleDeleteImageAction);
    deleteButton.setIcon(ImageEnum.TRASH_24.toImageIcon());
    inputPanel.add(deleteButton, new TableLayoutConstraints(
        7, 1, 7, 1, TableLayout.CENTER, TableLayout.CENTER));

    return inputPanel;
  }

  /**
   * Handles select image file button action.
   */
  private void handleSelectFileAction() {
    logger.info("Handling the Load file action.");

    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    fileChooser.addChoosableFileFilter(new ImageFileFilter());
    String imageFilepath = StringUtils.trimToNull(this.imageInputField.getText());
    if (imageFilepath != null) {
      File file = new File(imageFilepath);
      // Check if bundle is set and the specified file is in the bundle (no parent).
      if (this.gameBundlePath != null && file.getParentFile() == null) {
        // If so, open filechooser in the game bundle directory.
        fileChooser.setCurrentDirectory(new File(this.gameBundlePath));
      } else {
        // Otherwise, try to open in the specified file's directory.
        fileChooser.setCurrentDirectory(file);
      }
    } else if (this.gameBundlePath != null) {
      // In the default case, open filechooser in the game bundle directory.
      fileChooser.setCurrentDirectory(new File(this.gameBundlePath));
    }

    // Open the file dialog and handle the file selection.
    final int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      this.loadNewImageFile(fileChooser.getSelectedFile().getAbsolutePath());
    }
  }

  /**
   * Handles delete image button action.
   */
  private void handleDeleteImageAction() {
    this.loadNewImageFile(null);
  }

  /**
   * Tried to load a new image file into the dialog.
   * @param filePath file path to load or null to clear the image
   */
  private void loadNewImageFile(String filePath) {
    if (filePath == null) {
      this.imageInputField.setText("");
      this.setImageLabel(null);
    } else {
      String newImageFilepath = filePath;
      if (!filePath.startsWith("http")) {
        File file = new File(filePath);
        if (file.getParentFile() == null ||
            StringUtils.equals(this.gameBundlePath, file.getParentFile().getAbsolutePath())) {
          newImageFilepath = file.getName();
        } else {
          newImageFilepath = file.getAbsolutePath();
        }
      }
      this.imageInputField.setText(newImageFilepath);
      this.setImageLabel(newImageFilepath);
    }
  }

  /** File selection filter for the image files. */
  private static class ImageFileFilter extends FileFilter {

    /**
     * Recognizes either directories or image files.
     * @param f the File to test
     * @return true to accept the file
     */
    @Override
    public boolean accept(@NotNull File f) {
      if (f.isDirectory()) {
        return true;
      }
      if (f.isFile()) {
        switch (FilenameUtils.getExtension(f.getName()).toLowerCase()) {
          case "jpg":
          case "jpeg":
          case "gif":
          case "png":
            return true;
        }
      }
      return false;
    }

    /** @inheritDoc */
    @Override
    public @NotNull String getDescription() {
      return "JJeopardy supported image files or directories";
    }
  }

  /**
   * Key listener to handle Enter press on the image filename input field.
   */
  private static class ImageInputFieldKeyListener implements KeyListener {

    /** Reference to the image picker panel. */
    private final ImagePickerPanel imagePickerPanel;

    /**
     * Ctor.
     * @param imagePickerPanel reference to the image picker panel
     */
    public ImageInputFieldKeyListener(ImagePickerPanel imagePickerPanel) {
      this.imagePickerPanel = imagePickerPanel;
    }

    /** Does nothing. */
    @Override
    public void keyTyped(KeyEvent e) {}

    /**
     * Attempts to load image specified in the input field.
     * @param e the event to be processed
     */
    @Override
    public void keyPressed(@NotNull KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        String filePath = StringUtils.trimToNull(this.imagePickerPanel.imageInputField.getText());
        this.imagePickerPanel.loadNewImageFile(filePath);
      }
    }

    /** Does nothing. */
    @Override
    public void keyReleased(KeyEvent e) {}
  }
}
