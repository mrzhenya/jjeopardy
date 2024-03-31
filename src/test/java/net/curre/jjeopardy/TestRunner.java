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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Just a handy runner to execute bits of test code "by hand".
 *
 * @author Yevgeny Nyden
 */
public class TestRunner {


  public static void main(final String[] args) {

    testRegExStuff();

//    testProgressDialog();

/*
    try {
      testCreatingPropertiesFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
*/

//    printAvailableFonts();
  }

  public static void testRegExStuff() {
    String categoryName = "category.1.name";
    Pattern namePattern = Pattern.compile("^category\\.(\\d+)\\.name$");
    Matcher matcher1 = namePattern.matcher(categoryName);
    boolean found1 = matcher1.find();
    String number1 = "";
    if (found1) {
      number1 = matcher1.group(1);
    }
    String line = "category.1.question.2.img";
    Pattern linePattern = Pattern.compile("^category\\.(\\d+)\\.(answer|question)\\.(\\d+)(.*)$");
    Matcher matcher2 = linePattern.matcher(line);
    boolean found2 = matcher2.find();
    String number2 = "";
    if (found2) {
      number2 = matcher2.group(4);
    }
    System.out.println("Found groups " + number1 + " and " + number2);
  }

  public static void testCreatingPropertiesFile()
      throws FileNotFoundException, IOException {

    // Creating properties files from Java program
    Properties properties = new Properties();

    // In the name of userCreated.properties, in the
    // current directory location, the file is created
    FileOutputStream fileOutputStream
        = new FileOutputStream(
        "testGame.xml");

    // As an example, given steps how
    // to keep username and password
    properties.setProperty("username", "value1");
    properties.setProperty("password", "value2");

    // writing properties into properties file
    // from Java As we are writing text format,
    // store() method is used
    properties.storeToXML(
        fileOutputStream,
        "Sample way of creating Properties file from Java program");

    fileOutputStream.close();
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

  private static void testProgressDialog() {
    JFrame frame = createFrame("ProgressMonitor Example");
    JButton button = new JButton("start task");
    button.addActionListener(createStartTaskActionListener(frame));
    frame.add(button, BorderLayout.NORTH);
    frame.setVisible(true);
  }

  private static ActionListener createStartTaskActionListener(Component parent) {
    //for progress monitor dialog title
    UIManager.put("ProgressMonitor.progressText", "Test Progress");
    return (ae) -> {
      new Thread(() -> {
        //creating ProgressMonitor instance
        ProgressMonitor pm = new ProgressMonitor(parent, "Test Task",
            "Task starting", 0, 100);

        //decide after 100 millis whether to show popup or not
        pm.setMillisToDecideToPopup(100);
        //after deciding if predicted time is longer than 100 show popup
        pm.setMillisToPopup(100);
        for (int i = 1; i <= 100; i++) {
          //updating ProgressMonitor note
          pm.setNote("Task step: " + i);
          //updating ProgressMonitor progress
          pm.setProgress(i);
          try {
            //delay for task simulation
            TimeUnit.MILLISECONDS.sleep(200);
          } catch (InterruptedException e) {
            System.err.println(e);
          }
        }
        pm.setNote("Task finished");
      }).start();
    };
  }

  private static JFrame createFrame(String title) {
    JFrame frame = new JFrame(title);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(new Dimension(800, 700));
    return frame;
  }
}
