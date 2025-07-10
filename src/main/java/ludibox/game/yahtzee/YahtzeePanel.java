package ludibox.game.yahtzee;

import ludibox.core.GamePanel;
import ludibox.core.MainWindow;
import ludibox.util.ImageLoader;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;


public class YahtzeePanel extends GamePanel {

    // 画像バッファ
    private Image[] diceImages = new Image[6];

    private Dice[] dices = new Dice[5];
    private JButton rollButton;
    private ScoreBoardPanel scoreBoardPanel;

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
            d.roll();
            dices[i] = d;
        }

        rollButton = new JButton("ROLL");
        rollButton.setBounds(20, 200, 100, 40);
        rollButton.addActionListener(e -> {
            for (Dice d : dices) d.roll();
            repaint();

            new javax.swing.Timer(900, ev -> {
                var predicted = ScoreCalculator.predict(dices);
                scoreBoardPanel.getModel().setPredictedScores(predicted);
                ((Timer) ev.getSource()).stop();
            }).start();
        });
        this.add(rollButton);

        scoreBoardPanel = new ScoreBoardPanel();
        scoreBoardPanel.setBounds(480, 50, 250, 375);
        this.add(scoreBoardPanel);

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    /* Diceクラス */
    private class Dice extends JButton  {
        private int value = 1;
        private boolean locked = false;
        private Image[] buffer;

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

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row < ScoreCategory.TOTAL.ordinal()) {
                        ScoreEntry entry = model.getEntry(row);
                        if (entry.myScore == null && entry.myPredictedScore != null) {
                            entry.myScore = entry.myPredictedScore;
                            entry.myPredictedScore = null;
                            model.updateTotal();
                            model.fireTableRowsUpdated(row, row);
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

        public ScoreEntry(ScoreCategory category) {
            this.category = category;
        }

        public String getDisplayMyScore() {
            return myScore != null ? myScore.toString()
                    : myPredictedScore != null ? "(" + myPredictedScore + ")"
                    : "";
        }

        public String getDisplayOpponentScore() {
            return opponentScore != null ? opponentScore.toString() : "";
        }
    }

    /* Table Model */
    class ScoreBoardModel extends AbstractTableModel {
        private final List<ScoreEntry> entries;
        private final String[] colNames = { "Category", "YOU", "CPU" };

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
                    fireTableDataChanged();
                    return;
                }
            }
        }

        public void setPredictedScores(java.util.Map<ScoreCategory, Integer> map) {
            for (ScoreEntry e : entries) {
                if (e.myScore == null && map.containsKey(e.category)) {
                    e.myPredictedScore = map.get(e.category);
                }
            }
            fireTableDataChanged();
        }

        public void clearPredictions() {
            for (ScoreEntry e : entries) {
                e.myPredictedScore = null;
            }
            fireTableDataChanged();
        }

        public void updateTotal() {
            int total = entries.stream()
                    .filter(e -> e.myScore != null && e.category != ScoreCategory.TOTAL)
                    .mapToInt(e -> e.myScore)
                    .sum();
            getEntry(entries.size() - 1).myScore = total;
            fireTableDataChanged();
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

