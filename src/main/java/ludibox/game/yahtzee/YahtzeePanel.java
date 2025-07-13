package ludibox.game.yahtzee;

import ludibox.core.GamePanel;
import ludibox.core.MainWindow;
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
    private JButton rollButton;
    private ScoreBoardPanel scoreBoardPanel;

    private final int MAX_ROLL = 3;
    private int rollCount = 0;
    private boolean isTurn = true;    // True: Player, False: CPU
    private boolean isEnd = false;

    public YahtzeePanel(MainWindow m) {
        super(m);
        init();
    }

    /* 初期化 */
    public void init() {
        this.removeAll();
        this.revalidate();
        this.repaint();

        this.setLayout(null);
        this.setBackground(Color.LIGHT_GRAY);

        // 画像の読み込み
        for (int i = 0; i < 6; i++) {
            diceImages[i] = ImageLoader.loadImage("yahtzee/dice" + (i+1) + ".png");
        }

        // サイコロの初期化
        for (int i = 0; i < dices.length; i++) {
            Dice d = new Dice(diceImages);
            d.setBounds(20 + i * 70, 50, 60, 60);
            this.add(d);
            dices[i] = d;
        }

        rollButton = new JButton("ROLL");
        rollButton.setBounds(40, 200, 100, 40);
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
        this.add(rollButton);

        scoreBoardPanel = new ScoreBoardPanel();
        scoreBoardPanel.setBounds(480, 50, 250, 375);
        this.add(scoreBoardPanel);

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
                        checkGameEnd();

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
        int[] counts = new int[6];
        for (Dice d : dices) counts[d.getValue() - 1]++;

        int maxCount =0, target = 1;
        for (int i = 0; i < 6; i++) {
            if (counts[i] > maxCount) {
                maxCount = counts[i];
                target = i + 1;
            }
        }

        Set<Integer> keep = new HashSet<>();
        keep.add(target);
        return keep;
    }

    /* 最善手の選択 */
    private ScoreCategory selectBestCategory(Map<ScoreCategory, Integer> predicted) {
        int max = -1;
        ScoreCategory best = null;
        for (Map.Entry<ScoreCategory, Integer> entry : predicted.entrySet()) {
            if (entry.getKey() == ScoreCategory.TOTAL) continue;

            ScoreEntry e = scoreBoardPanel.getModel().getEntry(entry.getKey().ordinal());
            if (e.opponentScore == null) {
                if (entry.getValue() > max) {
                    max = entry.getValue();
                    best = entry.getKey();
                }
            }
        }
        return best;
    }

    private void checkGameEnd() {
        boolean myDone = scoreBoardPanel.getModel().isAllMyScoreFilled();
        boolean opponentDone = scoreBoardPanel.getModel().isAllOpponentScoreFilled();

        if (myDone && opponentDone) {
            isTurn = true;

            int myScore = scoreBoardPanel.getModel().getEntry(ScoreCategory.TOTAL.ordinal()).myScore;
            int opponentScore = scoreBoardPanel.getModel().getEntry(ScoreCategory.TOTAL.ordinal()).opponentScore;

            String message;
            if (myScore > opponentScore) {
                message = "You win!\nYour score: " + myScore + "\nCPU score: " + opponentScore;
            } else if (myScore < opponentScore) {
                message = "You Lose!\nYour score: " + myScore + "\nCPU score: " + opponentScore;
            } else {
                message = "Draw!\nYour score: " + myScore + "\nCPU score: " + opponentScore;
            }

            System.out.println(message);
        }

        rollButton.setEnabled(false);
        isTurn = false;
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
                            c.setBackground(Color.WHITE);
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

                    if (row < ScoreCategory.TOTAL.ordinal() && 1 <= col) {
                        ScoreEntry entry = model.getEntry(row);
                        if (entry.myScore == null && entry.myPredictedScore != null) {
                            entry.myScore = entry.myPredictedScore;
                            entry.myPredictedScore = null;
                            model.clearMyPredictions();
                            model.selectCell(row, col);
                            model.updateMyTotal();

                            table.revalidate();
                            table.repaint();

                            checkGameEnd();

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
        TOTAL("Total");

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
            getEntry(entries.size() - 1).myScore = entries.stream()
                    .filter(e -> e.myScore != null && e.category != ScoreCategory.TOTAL)
                    .mapToInt(e -> e.myScore)
                    .sum();
            fireTableDataChanged();
        }

        public void updateOpponentTotal() {
            getEntry(entries.size() - 1).opponentScore = entries.stream()
                    .filter(e -> e.opponentScore != null && e.category != ScoreCategory.TOTAL)
                    .mapToInt(e -> e.opponentScore)
                    .sum();
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

