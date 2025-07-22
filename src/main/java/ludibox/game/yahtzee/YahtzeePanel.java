package ludibox.game.yahtzee;

import ludibox.core.GamePanel;
import ludibox.core.MainWindow;
import ludibox.ui.CustomButton;
import ludibox.ui.CustomButtonStyle;
import ludibox.util.ImageLoader;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;


public class YahtzeePanel extends GamePanel {

    // 画像バッファ
    private final Image[] diceImages = new Image[6];

    private final Dice[] dices = new Dice[5];
    private CustomButton rollButton;
    private ScoreBoardPanel scoreBoardPanel;
    private JPanel endPanel;
    private JLabel resultLabel;

    private final int MAX_ROLL = 3;
    private int rollCount = 0;
    private boolean isTurn = true;    // True: Player, False: CPU
    private boolean isEnd = false;

    // AI用 - スコア重みづけ
    private final Map<ScoreCategory, Double> weights = Map.of(
            ScoreCategory.YAHTZEE, 2.0,
            ScoreCategory.FULL_HOUSE, 1.2,
            ScoreCategory.SMALL_STRAIGHT, 1.1,
            ScoreCategory.LARGE_STRAIGHT, 1.5,
            ScoreCategory.THREE_OF_A_KIND, 1.0,
            ScoreCategory.FOUR_OF_A_KIND, 1.0,
            ScoreCategory.CHANCE, 0.8
    );

    public YahtzeePanel(MainWindow m) {
        super(m);
        init();
    }

    /* 初期化 */
    public void init() {
        this.removeAll();
        this.revalidate();
        this.repaint();

        this.setLayout(new BorderLayout());
        this.setBackground(Color.LIGHT_GRAY);

        // 画像の読み込み
        for (int i = 0; i < 6; i++) {
            diceImages[i] = ImageLoader.loadImage("yahtzee/dice" + (i+1) + ".png");
        }

        // 左パネル
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.LIGHT_GRAY);

        // サイコロの初期化
        JPanel dicePanel = new JPanel();
        dicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        dicePanel.setOpaque(false);

        for (int i = 0; i < dices.length; i++) {
            Dice d = new Dice(diceImages);
            d.setPreferredSize(new Dimension(60, 60));
            dices[i] = d;
            dicePanel.add(d);
        }

        rollButton = new CustomButton("ROLL", CustomButtonStyle.DARK);
        rollButton.setPreferredSize(new Dimension(150, 40));
        rollButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        rollButton.addActionListener(e -> {
            if (rollCount >= MAX_ROLL) return;

            rollButton.setEnabled(false);
            rollCount++;
            updateRollButtonLabel();
            for (Dice d : dices) d.roll();
            repaint();

            new javax.swing.Timer(900, ev -> {
                var predicted = ScoreCalculator.predict(dices);
                scoreBoardPanel.getModel().setMyPredictedScores(predicted);
                ((Timer) ev.getSource()).stop();

                if (rollCount < MAX_ROLL) {
                    rollButton.setEnabled(true);
                }
            }).start();
        });
        updateRollButtonLabel();
        // リザルト
        resultLabel = new JLabel("");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 20));
        resultLabel.setBounds(160, 200, 300, 40);
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // 終了ボタンパネル
        endPanel = new JPanel();
        endPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        endPanel.setBackground(Color.LIGHT_GRAY);
        endPanel.setVisible(false);
        CustomButton restartButton = new CustomButton("Restart", CustomButtonStyle.DARK);
        restartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        restartButton.addActionListener(e -> restartGame());
        CustomButton mainMenuButton = new CustomButton("Main Menu", CustomButtonStyle.DARK);
        mainMenuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainMenuButton.addActionListener(e -> quit());
        endPanel.add(restartButton);
        endPanel.add(mainMenuButton);

        leftPanel.add(Box.createVerticalStrut(100));
        leftPanel.add(dicePanel);
        leftPanel.add(rollButton);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(endPanel);
        leftPanel.add(Box.createVerticalGlue());

        scoreBoardPanel = new ScoreBoardPanel();
        scoreBoardPanel.setPreferredSize(new Dimension(300, 0));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(scoreBoardPanel, BorderLayout.CENTER);
        rightPanel.add(resultLabel, BorderLayout.SOUTH);

        this.add(leftPanel, BorderLayout.CENTER);
        this.add(rightPanel, BorderLayout.EAST);
    }

    private void restartGame() {
        this.removeAll();
        this.revalidate();
        this.repaint();

        isTurn = true;
        isEnd = false;
        rollCount = 0;
        resultLabel.setText("");
        rollButton.setEnabled(true);
        updateRollButtonLabel();
        init();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    private void updateRollButtonLabel() {
        int remaining = MAX_ROLL - rollCount;
        if (remaining > 0) {
            rollButton.setText("Roll - " + remaining);
        } else {
            rollButton.setText("No Rolls Left...");
        }
    }

    /* AIムーブ */
    private void aiMove() {

        isTurn = false;

        for (Dice d : dices) {
            d.value = 0;
            d.locked = false;
        }

        ScoreBoardModel model = scoreBoardPanel.getModel();
        final int[] step = {0};

        new Timer(1200, e -> {
            if (step[0] < MAX_ROLL) {
                // ロール
                for (Dice d : dices) if (!d.locked) d.roll();

                // ロック
                new Timer(900, ev -> {
                    ((Timer) ev.getSource()).stop();

                    Map<ScoreCategory, Integer> predicted = ScoreCalculator.predict(dices);
                    model.setOpponentPredictedScores(predicted);
                    scoreBoardPanel.repaint();

                    Set<Integer> bestValues = chooseDiceValuesToKeep();
                    for (Dice d : dices) {
                        d.locked = bestValues.contains(d.getValue());
                        d.repaint();
                    }

                    step[0]++;
                }).start();
            } else {
                ((Timer) e.getSource()).stop();

                // スコアの確定
                new Timer(100, evt -> {
                    ((Timer) evt.getSource()).stop();
                    Map<ScoreCategory, Integer> predicted = ScoreCalculator.predict(dices);
                    ScoreCategory best = selectBestCategory(predicted);
                    if (best != null) {
                        model.setOpponentScore(best, predicted.get(best));
                        model.selectCell(best.ordinal(), 2);
                        model.updateOpponentTotal();
                        scoreBoardPanel.repaint();
                        if (checkGameEnd()) return;

                        if (!isEnd) {
                            // ターンを渡す
                            model.clearOpponentPredictions();
                            for (Dice d : dices) {
                                d.value = 0;
                                d.locked = false;
                                d.repaint();
                            }
                            rollCount = 0;
                            updateRollButtonLabel();
                            rollButton.setEnabled(true);
                            isTurn = true;
                        }
                    }
                }).start();
            }
        }).start();
    }

    /* ロックする目を選ぶ */
    private Set<Integer> chooseDiceValuesToKeep() {
        // 現在のスコアボード
        ScoreBoardModel model = scoreBoardPanel.getModel();
        // 現在のスコア予測
        Map<ScoreCategory, Integer> predicted = ScoreCalculator.predict(dices);

        // 未確定の役のみを評価対象にする
        List<ScoreCategory> remainingCategories = new ArrayList<>();
        for (ScoreCategory cate : ScoreCategory.values())
            if (cate != ScoreCategory.TOTAL && model.getEntry(cate.ordinal()).opponentScore == null)
                remainingCategories.add(cate);

        // 最もスコアが高い役を見つける
        ScoreCategory targetCategory = null;
        int maxScore = -1;
        for (ScoreCategory cate : remainingCategories) {
            int score = predicted.getOrDefault(cate, 0);
            if (score > maxScore) {
                maxScore = score;
                targetCategory = cate;
            }
        }

        Set<Integer> keep = new HashSet<>();

        // 選んだカテゴリに応じてロックを変更
        if (targetCategory != null) {
            switch (targetCategory) {
                case FULL_HOUSE, THREE_OF_A_KIND, FOUR_OF_A_KIND, YAHTZEE -> {
                    // 最頻値をロック
                    int[] counts = new int[6];
                    for (Dice d : dices) counts[d.getValue() - 1]++;
                    int target = 1;
                    for (int i = 0; i < 6; i++) if (counts[i] > counts[target - 1]) target = i + 1;
                    keep.add(target);
                }
                case SMALL_STRAIGHT, LARGE_STRAIGHT -> {
                    // ストレートに必要な目だけロック
                    List<Integer> straightNumbers = Arrays.asList(1, 2, 3, 4, 5, 6);
                    for (Dice d : dices) if (straightNumbers.contains(d.getValue())) keep.add(d.getValue());
                }
                default -> {    // CHANCE
                    // 出目が大きいダイスをロック
                    for (Dice d : dices) if (d.getValue() >= 5) keep.add(d.getValue());
                }
            }
        }
        return keep;
    }

    /* 最善手の選択 */
    private ScoreCategory selectBestCategory(Map<ScoreCategory, Integer> predicted) {
        double max = -1;
        ScoreCategory best = null;
        for (Map.Entry<ScoreCategory, Integer> entry : predicted.entrySet()) {
            if (entry.getKey() == ScoreCategory.TOTAL) continue;

            ScoreEntry e = scoreBoardPanel.getModel().getEntry(entry.getKey().ordinal());
            if (e.opponentScore == null) {
                double weight = weights.getOrDefault(entry.getKey(), 1.0);
                double score = entry.getValue() * weight;

                if (score > max) {
                    max = score;
                    best = entry.getKey();
                }
            }
        }
        return best;
    }

    private boolean checkGameEnd() {
        boolean myDone = scoreBoardPanel.getModel().isAllMyScoreFilled();
        boolean opponentDone = scoreBoardPanel.getModel().isAllOpponentScoreFilled();

        if (myDone && opponentDone) {
            isTurn = true;

            int myScore = scoreBoardPanel.getModel().getEntry(ScoreCategory.TOTAL.ordinal()).myScore;
            int opponentScore = scoreBoardPanel.getModel().getEntry(ScoreCategory.TOTAL.ordinal()).opponentScore;

            String message;
            if (myScore > opponentScore) {
                message = "<html>You win!<br>Your score: " + myScore + "<br>CPU score: " + opponentScore + "</html>";
            } else if (myScore < opponentScore) {
                message = "<html>You Lose!<br>Your score: " + myScore + "<br>CPU score: " + opponentScore + "</html>";
            } else {
                message = "<html>Draw!<br>Your score: " + myScore + "<br>CPU score: " + opponentScore + "</html>";
            }

            resultLabel.setText(message);
            rollButton.setEnabled(false);

            endPanel.setVisible(true);

            rollButton.setText("Game End");
            isTurn = false;
            return true;
        }
        return false;
    }





    /* Diceクラス */
    private class Dice extends JButton  {
        private int value = 0;
        private boolean locked = false;
        private final Image[] buffer;

        // アニメーション
        private Timer rollTimer;
        private int animationFrame = 0;
        private double scale = 1.0;

        public Dice(Image[] buffer) {
            this.buffer = buffer;
            this.setPreferredSize(new Dimension(60, 60));
            this.setContentAreaFilled(false);
            this.setBorderPainted(false);
            this.setFocusPainted(false);

            this.addActionListener(e -> {
                if (!isTurn) return;
                toggleLock();
                repaint();
            });
        }

        public void roll() {
            if (locked) return;

            if (rollTimer != null && rollTimer.isRunning()) {
                rollTimer.stop();
            }

            animationFrame = 0;
            rollTimer = new Timer(50, e -> {
                value = 1 + new Random().nextInt(6);

                int totalFrames = 10;
                double maxScale = 1.2;

                double progress = (double) animationFrame / totalFrames;
                scale = 1.0 + Math.sin(progress * Math.PI) * (maxScale - 1.0);

                repaint();
                animationFrame++;

                if (animationFrame >= 10) {
                    ((Timer) e.getSource()).stop();
                    value = 1 + new Random().nextInt(6);
                    scale = 1.0;
                    repaint();
                }
            });

            rollTimer.start();
        }

        public void toggleLock() {
            locked = !locked;
        }

        public boolean isLocked() { return locked; }
        public int getValue() { return value; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g.create();

            // スケーリングの中心
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g2d.translate(cx, cy);
            g2d.scale(scale, scale);
            g2d.translate(-cx, -cy);

            if (diceImages != null && value >= 1 && value <= 6) {
                g2d.drawImage(diceImages[value - 1], 0, 0, getWidth(), getHeight(), this);
            }

            if (locked) {
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            g2d.dispose();
        }
    }



    /* ScoreBoard */
    public class ScoreBoardPanel extends JPanel {
        private final JTable table;
        private final ScoreBoardModel model;

        public ScoreBoardPanel() {
            this.setLayout(new BorderLayout());
            this.model = new ScoreBoardModel();
            this.table = new JTable(model);

            table.setRowHeight(25);
            table.setFocusable(false);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowSelectionAllowed(false);
            table.setColumnSelectionAllowed(false);
            table.setCellSelectionEnabled(false);
            table.getColumnModel().getColumn(0).setPreferredWidth(120);

            for (int i = 0; i < table.getColumnCount(); i++) {
                final int col = i;

                table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table, Object value, boolean isSelected,
                            boolean hasFocus, int row, int column
                    ) {
                        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        setHorizontalAlignment(SwingConstants.CENTER);
                        c.setFont(new Font("Arial", Font.BOLD, 14));

                        // 背景色 & 文字色
                        // 列ごとの設定
                        if (col == 1) {
                            if (model.isCellSelected(row, column)) {
                                c.setBackground(Color.RED);
                                c.setForeground(Color.WHITE);
                            } else {
                                c.setBackground(new Color(255, 230, 230));
                                c.setForeground(new Color(120, 120, 120));
                            }
                        } else if (col == 2) {
                            if (model.isCellSelected(row, column)) {
                                c.setBackground(Color.BLUE);
                                c.setForeground(Color.WHITE);
                            } else {
                                c.setBackground(new Color(230, 240, 255));
                                c.setForeground(new Color(120, 120, 120));
                            }
                        } else {
                            if (row == ScoreCategory.BONUS.ordinal() || row == ScoreCategory.TOTAL.ordinal()) {
                                c.setBackground(Color.LIGHT_GRAY);
                            } else {
                                c.setBackground(Color.WHITE);
                            }
                            c.setForeground(Color.BLACK);
                        }

                        return c;
                    }
                });
            }

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());

                    if (row < ScoreCategory.TOTAL.ordinal() && row != ScoreCategory.BONUS.ordinal() && 1 <= col) {
                        ScoreEntry entry = model.getEntry(row);
                        if (entry.myScore == null && entry.myPredictedScore != null) {
                            entry.myScore = entry.myPredictedScore;
                            entry.myPredictedScore = null;
                            model.clearMyPredictions();
                            model.selectCell(row, col);
                            model.updateMyTotal();

                            table.revalidate();
                            table.repaint();

                            if (checkGameEnd()) return;

                            if (!isEnd) {
                                // ロールボタンの更新
                                rollButton.setEnabled(false);
                                rollButton.setText("CPU is thinking...");

                                table.setEnabled(false);

                                // AIの処理へ移行
                                new Timer(300, evt -> {
                                    ((Timer) evt.getSource()).stop();
                                    aiMove();
                                    table.setEnabled(true);
                                }).start();
                            }
                        }
                    }
                }
            });

            this.add(new JScrollPane(table), BorderLayout.CENTER);
        }

        public ScoreBoardModel getModel() { return model; }
    }



    /* ScoreCategory */
    enum ScoreCategory {
        ONES("Ones"), TWOS("Twos"), THREES("Threes"), FOURS("Fours"), FIVES("Fives"), SIXES("Sixes"),
        THREE_OF_A_KIND("3 of a Kind"), FOUR_OF_A_KIND("4 of a Kind"), FULL_HOUSE("Full House"),
        SMALL_STRAIGHT("Small Straight"), LARGE_STRAIGHT("Large Straight"), YAHTZEE("Yahtzee"), CHANCE("Chance"),
        BONUS("Bonus"), TOTAL("Total");

        public final String label;
        ScoreCategory(String label) {
            this.label = label;
        }

        @Override
        public String toString() { return label; }
    }

    /* Score Entry */
    class ScoreEntry {
        public final ScoreCategory category;
        private Integer myScore = null;
        private Integer myPredictedScore = null;
        private Integer opponentScore = null;
        private Integer opponentPredictedScore = null;

        public ScoreEntry(ScoreCategory category) {
            this.category = category;
        }

        public String getDisplayMyScore() {
            return myScore != null ? myScore.toString()
                    : myPredictedScore != null ? String.valueOf(myPredictedScore)
                    : "";
        }

        public String getDisplayOpponentScore() {
            return opponentScore != null ? opponentScore.toString()
                    : opponentPredictedScore != null ? String.valueOf(opponentPredictedScore)
                    : "";
        }
    }



    /* Table Model */
    class ScoreBoardModel extends AbstractTableModel {
        private final List<ScoreEntry> entries;
        private final String[] colNames = { "Category", "YOU", "CPU" };
        private final Set<Point> selectedCells = new HashSet<>();

        public ScoreBoardModel() {
            entries = new ArrayList<>();
            for (ScoreCategory cat : ScoreCategory.values()) {
                entries.add(new ScoreEntry(cat));
            }
        }

        public void setMyScore(ScoreCategory cat, int score) {
            for (ScoreEntry e : entries) {
                if (e.category == cat) {
                    e.myScore = score;
                    e.myPredictedScore = null;

                    // ロール回数のリセット
                    rollCount = 0;

                    // 予測スコアのクリア
                    clearMyPredictions();
                    updateMyTotal();
                    fireTableDataChanged();
                    return;
                }
            }
        }

        public void setOpponentScore(ScoreCategory cat, int score) {
            for (ScoreEntry e : entries) {
                if (e.category == cat) {
                    e.opponentScore = score;
                    fireTableDataChanged();
                    return;
                }
            }
        }

        public void setMyPredictedScores(java.util.Map<ScoreCategory, Integer> map) {
            for (ScoreEntry e : entries) {
                if (e.myScore == null && map.containsKey(e.category)) {
                    e.myPredictedScore = map.get(e.category);
                }
            }
            fireTableDataChanged();
        }

        public void setOpponentPredictedScores(java.util.Map<ScoreCategory, Integer> map) {
            for (ScoreEntry e : entries) {
                if (e.opponentScore == null && map.containsKey(e.category)) {
                    e.opponentPredictedScore = map.get(e.category);
                }
            }
            fireTableDataChanged();
        }

        public void clearMyPredictions() {
            for (ScoreEntry e : entries) e.myPredictedScore = null;
            fireTableDataChanged();
        }

        public void clearOpponentPredictions() {
            for (ScoreEntry e : entries) e.opponentPredictedScore = null;
            fireTableDataChanged();
        }

        public void updateMyTotal() {
            int upperSum = 0, lowerSum = 0;

            for (ScoreEntry e : entries) {
                if (e.myScore != null) {
                    if (e.category.ordinal() <= ScoreCategory.SIXES.ordinal()) upperSum += e.myScore;
                    else if (e.category != ScoreCategory.BONUS && e.category != ScoreCategory.TOTAL) lowerSum += e.myScore;
                }
            }

            // BONUS計算
            ScoreEntry bonusEntry = getEntry(ScoreCategory.BONUS.ordinal());
            bonusEntry.myScore = (upperSum >= 63) ? 35 : 0;

            // TOTAL更新
            getEntry(ScoreCategory.TOTAL.ordinal()).myScore = upperSum + lowerSum + bonusEntry.myScore;

            fireTableDataChanged();
        }

        public void updateOpponentTotal() {
            int upperSum = 0, lowerSum = 0;

            for (ScoreEntry e : entries) {
                if (e.opponentScore != null) {
                    if (e.category.ordinal() <= ScoreCategory.SIXES.ordinal()) upperSum += e.opponentScore;
                    else if (e.category != ScoreCategory.BONUS && e.category != ScoreCategory.TOTAL) lowerSum += e.opponentScore;
                }
            }

            // BONUS計算
            ScoreEntry bonusEntry = getEntry(ScoreCategory.BONUS.ordinal());
            bonusEntry.opponentScore = (upperSum >= 63) ? 35 : 0;

            // TOTAL更新
            getEntry(ScoreCategory.TOTAL.ordinal()).opponentScore = upperSum + lowerSum + bonusEntry.opponentScore;

            fireTableDataChanged();
        }

        public void selectCell(int row, int col) {
            Point cell = new Point(row, col);
            selectedCells.add(cell);
            fireTableCellUpdated(row, col);
        }

        public boolean isCellSelected(int row, int col) {
            return selectedCells.contains(new Point(row, col));
        }

        public boolean isAllMyScoreFilled() {
            return entries.stream()
                .filter(e -> e.category != ScoreCategory.TOTAL)
                .allMatch(e -> e.myScore != null || e.myPredictedScore != null);
        }

        public boolean isAllOpponentScoreFilled() {
            return entries.stream()
                .filter(e -> e.category != ScoreCategory.TOTAL)
                .allMatch(e -> e.opponentScore != null || e.opponentPredictedScore != null);
        }

        public ScoreEntry getEntry(int row) { return entries.get(row); }
        @Override
        public int getRowCount() { return entries.size(); }
        @Override
        public int getColumnCount() { return colNames.length; }

        @Override
        public Object getValueAt(int rowIdx, int colIdx) {
            ScoreEntry e = entries.get(rowIdx);
            return switch (colIdx) {
                case 0 -> e.category.label;
                case 1 -> e.getDisplayMyScore();
                case 2 -> e.getDisplayOpponentScore();
                default -> "";
            };
        }

        @Override
        public String getColumnName(int col) { return colNames[col]; }

        @Override
        public boolean isCellEditable(int rowIdx, int colIdx) { return false; }
    }



    /* スコアの計算クラス */
    public class ScoreCalculator {
        public static Map<ScoreCategory, Integer> predict(Dice[] dice) {
            Map<ScoreCategory, Integer> result = new EnumMap<>(ScoreCategory.class);

            // 1~6
            for (int i = 1; i <= 6; i++) {
                int sum = 0;
                for (Dice d : dice) if (d.getValue() == i) sum += i;
                result.put(ScoreCategory.values()[i - 1], sum);
            }

            // CHANCE
            int total = 0;
            for (Dice d : dice) total += d.getValue();
            result.put(ScoreCategory.CHANCE, total);

            // そのほか
            result.put(ScoreCategory.THREE_OF_A_KIND, isThreeOfAKind(dice) ? total : 0);
            result.put(ScoreCategory.FOUR_OF_A_KIND, isFourOfAKind(dice) ? total : 0);
            result.put(ScoreCategory.FULL_HOUSE, isFullHouse(dice) ? 25 : 0);
            result.put(ScoreCategory.SMALL_STRAIGHT, isSmallStraight(dice) ? 30 : 0);
            result.put(ScoreCategory.LARGE_STRAIGHT, isLargeStraight(dice) ? 40 : 0);
            result.put(ScoreCategory.YAHTZEE, isYahtzee(dice) ? 50 : 0);

            return result;
        }

        private static boolean isThreeOfAKind(Dice[] dice) {
            int[] counts = new int[6];
            for (Dice d : dice) counts[d.getValue() - 1]++;
            for (int count : counts) if (count >= 3) return true;
            return false;
        }

        private static boolean isFourOfAKind(Dice[] dice) {
            int[] counts = new int[6];
            for (Dice d : dice) counts[d.getValue() - 1]++;
            for (int count : counts) if (count >= 4) return true;
            return false;
        }

        private static boolean isFullHouse(Dice[] dice) {
            Map<Integer, Integer> count = new HashMap<>();
            for (Dice d : dice) count.put(d.getValue(), count.getOrDefault(d.getValue(), 0) + 1);
            return count.containsValue(3) && count.containsValue(2);
        }

        private static boolean isYahtzee(Dice[] dice) {
            int first = dice[0].getValue();
            for (Dice d : dice) if (d.getValue() != first) return false;
            return true;
        }

        private static boolean isSmallStraight(Dice[] dice) {
            Set<Integer> set = new TreeSet<>();
            for (Dice d : dice) set.add(d.getValue());
            Integer[] arr = set.toArray(new Integer[0]);
            String s = Arrays.toString(arr);
            return s.contains("1, 2, 3, 4") || s.contains("2, 3, 4, 5") || s.contains("3, 4, 5, 6");
        }

        private static boolean isLargeStraight(Dice[] dice) {
            Set<Integer> set = new TreeSet<>();
            for (Dice d : dice) set.add(d.getValue());
            Integer[] arr = set.toArray(new Integer[0]);
            return Arrays.equals(arr, new Integer[]{1,2,3,4,5}) || Arrays.equals(arr, new Integer[]{2,3,4,5,6});
        }
    }
}

