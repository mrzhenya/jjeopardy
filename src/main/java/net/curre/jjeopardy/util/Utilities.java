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

import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.LocaleService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Object of this class represents a set of
 * common utilities for jjeopardy.
 *
 * @author Yevgeny Nyden
 */
public class Utilities {

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(Utilities.class.getName());

  /** Enumeration tha represents a field type. */
  public static enum FieldType {

    UNDEFINED, INTEGER, DOUBLE
  }

  /** Enumeration tha represents a platform/os type. */
  public static enum PlatformType {

    MAC_OS, LINUX, WINDOWS, UNKNOWN
  }

  /** Helper object to be used in the printTime() method. */
  public static Calendar lastTime = Calendar.getInstance();

  /**
   * Gets a default int property value for the given message name.
   * @param messageName message name
   * @return default int property value
   */
  public static int getDefaultIntProperty(String messageName) {
    ResourceBundle bundle = ResourceBundle.getBundle("default");
    return Integer.parseInt(bundle.getString(messageName).trim());
  }

  /**
   * Validates if the passed component value is valid.
   *
   * @param field Input field, which value to validate.
   * @param type  Type of the value to check (i.e. Utilities.TYPE_INTEGER).
   * @return true If the component value is valid; false otherwise.
   */
  public static boolean validateTextField(JTextField field, FieldType type) {
    String str = field.getText().trim();
    switch (type) {
      case INTEGER:
        return StringUtils.isNumeric(str);
      case DOUBLE:
        try {
          Double.parseDouble(str);
          return true;
        } catch (NumberFormatException e) {
          return false;
        }
      default:
        LOGGER.severe("ERROR: validateTextField: Unknown type: " + type);
    }
    return false;
  }

  /**
   * Computes the X coordinate for the object so that it
   * gets centered in the container, which is basically:
   * (containerWidth - objectWidth) / 2;
   *
   * @param containerWidth container width (where the object is drawn).
   * @param objectWidth    the object width (what is being centered).
   * @return X coordinate for the object, so it is centered in the container.
   */
  public static int computeCenterX(int containerWidth, int objectWidth) {
    return (containerWidth - objectWidth) / 2;
  }

  /**
   * Returns capitalized first letter from the passed text field
   * or null if the text field is empty.
   *
   * @param field Text field to read.
   * @return Capitalized first letter from the passed text field
   *         or null if the text field is empty.
   */
  public static String getFirstLetterFromField(JTextField field) {
    String str = field.getText();
    if (StringUtils.isBlank(str)) {
      return null;
    }
    return str.trim().substring(0, 1).toUpperCase();
  }

  /**
   * Gets and parses text from the given text field.
   * If the text field is null or contains an empty
   * string or white space only, 0 if returned.
   *
   * @param field Text field to parse.
   * @return Parsed integer.
   * @throws NumberFormatException If text field contains an invalid integer.
   */
  public static int parseIntFromTextField(JTextField field) {
    if (field != null) {
      String value = field.getText().trim();
      if (value.length() != 0) {
        return Integer.parseInt(value);
      }
    }
    return 0;
  }

  /**
   * Computes the size of the passed String.
   *
   * @param g2  Graphics object to use.
   * @param str String to be measured.
   * @return The size of the passed string as a <code>Dimension</code> object.
   */
  public static Dimension determineSizeOfString(Graphics2D g2, String str) {
    FontMetrics metrics = g2.getFontMetrics(g2.getFont());
    int height = metrics.getHeight();
    int width = metrics.stringWidth(str);
    return new Dimension(width, height);
  }

  /**
   * Underlines a letter in the given string at
   * the given position; note, that the string is
   * converted to html.
   *
   * @param str String to underline.
   * @param ind Letter index, which to underline.
   * @return A string with underlined letter at the given position.
   */
  public static String underlineLetter(String str, int ind) {
    return "<HTML>" + str.substring(0, ind) + "<U>" + str.charAt(ind) +
        "</U>" + str.substring(ind + 1);
  }

  /**
   * Opens a new frame and displays a warning message in it
   * (given its resource key) with two buttons OK and CANCEL.
   *
   * @param messageKey Message key.
   * @param yesKey     Yes button test key.
   * @param cancelKey  Cancel button text key.
   * @return return true if the user has chosen to continue (hit the OK button);
   *         false if the CANCEL optoins was chosen.
   */
  public static boolean displayOkCancelMessage(String messageKey, String yesKey, String cancelKey) {
    String msg = LocaleService.getString(messageKey);
    String yes = LocaleService.getString(yesKey);
    String cancel = LocaleService.getString(cancelKey);

    int answer = JOptionPane.showOptionDialog(
        AppRegistry.getInstance().getMainWindow(), msg, "Warning", JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.WARNING_MESSAGE, null, new Object[]{yes, cancel}, cancel);

    return answer == JOptionPane.OK_OPTION;
  }

  /**
   * This method is used for debugging purposes
   * to print timestamps and the elapsed time since the last
   * call to this method.
   *
   * @param msg Message to add to the print statement.
   */
  public static void printTime(String msg) {
    Calendar currTime = Calendar.getInstance();
    Calendar diff = Calendar.getInstance();
    long currMls = currTime.getTimeInMillis();
    diff.setTimeInMillis(currMls - lastTime.getTimeInMillis());
    FastDateFormat f = FastDateFormat.getInstance("mm:ss:SSS");
    System.out.println("TIME:::::: " + f.format(currTime.getTime()) +
        " (" + f.format(diff) + ") - " + msg);
    lastTime.setTimeInMillis(currMls);
  }

  /** Prints available looks and feels. */
  public static void printLookAndFeels() {
    UIManager.LookAndFeelInfo laf[] = UIManager.getInstalledLookAndFeels();
    for (int i = 0, n = laf.length; i < n; i++) {
      System.out.print("LAF Name: " + laf[i].getName() + "\t");
      System.out.println("  LAF Class name: " + laf[i].getClassName());
    }
  }

  /** Displays available fonts in a frame. */
  public static void printAvailableFonts() {
    JFrame f = new JFrame("Testing Fonts");
    f.setSize(400, 400);
    JPanel mainPanel = new JPanel();
    f.add(mainPanel);
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    String fontNames[] = ge.getAvailableFontFamilyNames();
    int j = fontNames.length;
    for (int i = 0; i < j; ++i) {
      JPanel panel = new JPanel();
      JLabel label = new JLabel(fontNames[i]);
      label.setFont(new Font(fontNames[i], Font.PLAIN, 16));
      panel.add(label);
      mainPanel.add(panel);

      System.out.println(fontNames[i]);
    }
    f.pack();
    f.setVisible(true);
  }

  /**
   * Determines the platform/os type we are running on.
   *
   * @return A PlatformType enumeration that represents the platform/os.
   */
  public static PlatformType getPlatformType() {
    if (System.getProperty("mrj.version") == null) {
      String osProp = System.getProperty("os.name").toLowerCase();
      if (osProp.startsWith("windows")) {
        return PlatformType.WINDOWS;
      } else if (osProp.startsWith("mac")) {
        return PlatformType.MAC_OS;
      } else if (osProp.startsWith("linux")) {
        return PlatformType.LINUX;
      } else {
        return PlatformType.UNKNOWN;
      }
    }
    return PlatformType.MAC_OS;
  }

  /**
   * Returns true if we are running on Mac OS; false otehrwise.
   *
   * @return True if we are on Mac OS; false otherwise.
   */
  public static boolean isMacOs() {
    return getPlatformType() == PlatformType.MAC_OS;
  }

  /**
   * Computes preferred size.
   *
   * @param panel panel to compute the preferred size of.
   * @return dimension object that represent the size.
   */
  public static Dimension getPreferredSize(JPanel panel) {
    Dimension preferredSize = new Dimension();
    for (Component component : panel.getComponents()) {
      Rectangle bounds = component.getBounds();
      preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
      preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
    }
    Insets insets = panel.getInsets();
    preferredSize.width += insets.right;
    preferredSize.height += insets.bottom;
    return preferredSize;
  }

}
