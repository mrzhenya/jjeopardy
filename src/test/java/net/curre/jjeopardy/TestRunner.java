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

package net.curre.jjeopardy;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

/**
 * Just a handy runner to execute bits of test code "by hand".
 *
 * @author Yevgeny Nyden
 */
public class TestRunner {


  public static void main(final String[] args) {
    printAvailableFonts();
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
    JFrame frame = new JFrame("Testing Fonts");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setPreferredSize(new Dimension(600, 600));
    JPanel mainPanel = new JPanel();
    frame.add(mainPanel);
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
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
