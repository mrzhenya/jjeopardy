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

package net.curre.jjeopardy.ui.dialog;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import net.curre.jjeopardy.bean.Player;
import net.curre.jjeopardy.bean.Question;
import net.curre.jjeopardy.images.ImageEnum;
import net.curre.jjeopardy.service.AppRegistry;
import net.curre.jjeopardy.service.GameDataService;
import net.curre.jjeopardy.service.SoundService;
import net.curre.jjeopardy.sounds.SoundEnum;
import net.curre.jjeopardy.ui.game.TimerLabel;
import net.curre.jjeopardy.ui.laf.theme.LafTheme;
import net.curre.jjeopardy.util.Utilities;

import javax.sound.sampled.Clip;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Question dialog UI that could be adapted to three different versions:
 * main question UI, bonus question UI, and the answer UI.
 * <br><br>
 * Due to the thread responsible for updating the timer label, this dialog
 * can not be modal; we have to fake the modality by disabling actions on the
 * main window while the question dialog is open.
 *
 * @author Yevgeny Nyden
 */
public class QuestionDialog extends JDialog {

  /** Private class logger. */
  private static final Logger LOGGER = Logger.getLogger(QuestionDialog.class.getName());

  /** Width of the Question pane (where question text is rendered). */
  private static final int QUESTION_COLUMN_WIDTH = 600;

  /** Time before bonus question is asked. */
  private static final long BONUS_GETREADY_TIME = 3500L;

  /** Reference to the current question. */
  private Question currentQuestion;

  /** Title panel where question header/title is displayed. */
  private QuestionTitle titlePanel;

  /** Reference to the main content UI (where question text is displayed). */
  private QuestionPane questionPane;

  /** Timer label. */
  private TimerLabel timeLabel;

  /** Current player index (used for bonus questions). */
  private int currBonusPlayerIndex;

  /** List of current bonus questions (initialized when bonus questions session starts. */
  private List<Question> currBonusQuestions;

  /** Ctor. */
  public QuestionDialog() {
    this.currBonusPlayerIndex = -1;

    // IMPORTANT: unfortunately, this dialog can not be modal due to
    // the thread code that's responsible for updating the timer label.

    this.setResizable(false);
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    this.initComponents();

    if (Utilities.isMacOs()) {
      // Remove application name for the frame panel.
      this.getRootPane().putClientProperty( "apple.awt.windowTitleVisible", false);
    }
    AppRegistry.getInstance().getLafService().registerUITreeForUpdates(this);
  }

  /**
   * Gets the current bonus questions player index.
   * @return current bonus player index
   */
  public int getCurrentBonusPlayerIndex() {
    return this.currBonusPlayerIndex;
  }

  /**
   * Gets the cost of the current question.
   * @return cost of the current question
   */
  public int getCurrentQuestionCost() {
    return this.currentQuestion.getPoints();
  }

  /**
   * Shows or hides the dialog.
   * @param isVisible true to show dialog; false to hide it
   */
  @Override
  public void setVisible(boolean isVisible) {
    setLocationRelativeTo(getOwner());
    super.setVisible(isVisible);
    if (isVisible) {
      AppRegistry.getInstance().getMainWindow().disableActions();
      super.setAlwaysOnTop(true);
    } else {
      AppRegistry.getInstance().getMainWindow().enableActions();
    }
  }

  /**
   * Hides this dialog.
   */
  public void hideQuestionDialog() {
    this.questionPane.clearTextLabels();
    this.questionPane.switchToEmptyCard();
    this.titlePanel.reset();

    // Helps to avoid a glitch when switching UI to the next question.
    SwingUtilities.invokeLater(() -> QuestionDialog.this.setVisible(false));
  }

  /**
   * Shows UI for the given question and starts the timer.
   * @param question question to ask
   * @param isBonus true if this is a bonus question
   */
  public void askQuestion(Question question, boolean isBonus) {
    this.currentQuestion = question;
    this.questionPane.showQuestion(question, isBonus);
    this.titlePanel.updateTitle(question);
    this.pack();

    SwingUtilities.invokeLater(() -> {
      QuestionDialog.this.setVisible(true);
      startTimer();
    });

    // stating the thinking music
    final SoundService soundService = AppRegistry.getInstance().getSoundService();
    soundService.stopAllMusic();
    soundService.startMusic(SoundEnum.THINKING, Clip.LOOP_CONTINUOUSLY);
  }

  /**
   * Starts the Bonus questions round. We give each player a turn
   * to answer a bonus question assuming there are enough questions.
   * Assumes there is at least one bonus question.
   */
  public void startAskingBonusQuestions() {
    this.currBonusPlayerIndex = 0;
    GameDataService gameService = AppRegistry.getInstance().getGameDataService();
    this.currBonusQuestions = new ArrayList<>(gameService.getCurrentGameData().getBonusQuestions());
    this.askBonusQuestion(this.currBonusPlayerIndex);
  }

  /**
   * Continues asking bonus questions if there are any
   * and if there are more players to ask.
   */
  public void continueAskingBonusQuestions() {
    this.currBonusPlayerIndex++;
    this.askBonusQuestion(this.currBonusPlayerIndex);
  }

  /**
   * Asks a bonus question. Stops if there are no more questions to offer
   * or if there are no more non-asked players to ask.
   * @param playerIndex index of the player who is going to be asked
   */
  private void askBonusQuestion(final int playerIndex) {
    GameDataService gameService = AppRegistry.getInstance().getGameDataService();
    List<Player> players = gameService.getCurrentPlayers();
    if (playerIndex >= players.size() || this.currBonusQuestions.isEmpty()) {
      this.setVisible(false);
      return;
    }
    Random random = new Random();
    Question question = this.currBonusQuestions.get(random.nextInt(this.currBonusQuestions.size()));
    this.currBonusQuestions.remove(question);
    question.setParentWithName(gameService.getCurrentPlayers().get(playerIndex));

    // Setting the title.
    this.titlePanel.updateTitle(question);

    // Show the 'get ready' note for the next player and then switch to the question panel.
    this.timeLabel.resetTimer();
    this.questionPane.showBonusIntro();
    this.setVisible(true);
    this.pack();
    SwingUtilities.invokeLater(() -> {
      try {
        Thread.sleep(BONUS_GETREADY_TIME);
      } catch (InterruptedException e) {
        LOGGER.log(Level.WARNING, "Error in Runnable", e);
      }
      QuestionDialog.this.askQuestion(question, true);
    });
  }

  /** Initializes the UI components. */
  private void initComponents() {
    this.timeLabel = new TimerLabel();

    LafTheme lafTheme = AppRegistry.getInstance().getLafService().getCurrentLafTheme();
    final int padding = lafTheme.getPanelPadding();

    // Settings the main layout.
    Container contentPane = getContentPane();
    contentPane.setLayout(new TableLayout(new double[][] {
      {padding, BasicDialog.ICON_COLUMN_WIDTH, padding, QUESTION_COLUMN_WIDTH, padding}, // columns
      {padding, TableLayout.PREFERRED, TableLayout.PREFERRED, padding + 20}})); // rows

    // ******* Title label.
    this.titlePanel = new QuestionTitle();
    contentPane.add(this.titlePanel, new TableLayoutConstraints(
      3, 1, 3, 1, TableLayout.CENTER, TableLayout.CENTER));

    // ******* Main content question pane (with card layout to update itself to
    // question, bonus question, or answer UI).
    this.questionPane = new QuestionPane(this);
    contentPane.add(this.questionPane, new TableLayoutConstraints(
      3, 2, 3, 2, TableLayout.FULL, TableLayout.FULL));

    // ******* Timer panel (question icon and the timer label).
    JPanel timerPanel = new JPanel();
    timerPanel.setLayout(new TableLayout(new double[][] {
      {TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL}, // columns
      {TableLayout.FILL, TableLayout.PREFERRED, 10, TableLayout.PREFERRED, TableLayout.FILL}})); // rows


    // Timer label.
    timeLabel.setText("00");
    timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
    timerPanel.add(timeLabel, new TableLayoutConstraints(
      1, 1, 1, 1, TableLayoutConstraints.CENTER, TableLayoutConstraints.CENTER));

    // Question icon.
    JLabel questionLabel = new JLabel(ImageEnum.QUESTION_BLUE_64.toImageIcon());
    timerPanel.add(questionLabel, new TableLayoutConstraints(
      1, 3, 1, 3, TableLayout.CENTER, TableLayout.CENTER));

    contentPane.add(timerPanel, new TableLayoutConstraints(
      1, 0, 1, 3, TableLayout.FULL, TableLayout.FULL));

    pack();
    setLocationRelativeTo(getOwner());
  }

  /**
   * Starts the question timer.
   */
  private void startTimer() {
    Thread timerThread = new Thread(this.timeLabel, "Timer");
    timerThread.start();
  }

  /**
   * Stops the timer.
   */
  protected void stopTimer() {
    this.timeLabel.stopTimer();
  }
}
