package ludibox.game.tictactoe;

import ludibox.core.GamePanel;
import ludibox.core.MainWindow;
import ludibox.math.Vec2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TicTacToePanel extends GamePanel {

    private final int GRID_SIZE = 3; // 盤面のサイズ (3x3)
    private CellButton[][] cells = new CellButton[GRID_SIZE][GRID_SIZE];    // セル情報
    private boolean isEnd = false; // ゲーム終了フラグ
    // True: O (Player), False: X (AI)
    private boolean turn = true;

    // UI系
    private OverlayPanel overlayPanel;  // オーバーレイパネル
    private JLabel infoLabel;           // 情報表示用ラベル
    private JPanel bottomButtonPanel;   // ボタンパネル
    private Vec2[] drawPoints = new Vec2[2];    // 勝利時に表示するラインの始点と終点

    // AIレベル
    public enum AILevel {
        RANDOM,
        BASIC,
        MAGIC,
    }
    private AILevel aiLevel = AILevel.MAGIC;

    public TicTacToePanel(MainWindow m, int level) {
        this(m);
        switch (level) {
            case 0 -> aiLevel = AILevel.RANDOM;
            case 1 -> aiLevel = AILevel.BASIC;
            case 2 -> aiLevel = AILevel.MAGIC;
        }
    }
    public TicTacToePanel(MainWindow m) {
        super(m);
        this.setLayout(new BorderLayout());
        init();
    }

    /* 初期化 */
    public void init() {
        // GUI系Componentの一括削除
        this.removeAll();
        this.revalidate();
        this.repaint();

        this.setBackground(Color.LIGHT_GRAY);


        // 内部記憶のリセット
        isEnd = false;
        turn = true;
        cells = new CellButton[GRID_SIZE][GRID_SIZE];
        drawPoints = new Vec2[2];

        // UI部分の生成
        createGridUI();
        createInfoUI();
    }

    /* ゲームの終了判定 */
    private boolean checkEnd() {
        String str = turn ? "O" : "X";
        for (int i = 0; i < GRID_SIZE; i++) {
            // 行チェック
            if (str.equals(cells[i][0].getText()) &&
                str.equals(cells[i][1].getText()) &&
                str.equals(cells[i][2].getText())) {
                drawPoints[0] = cells[i][0].getCenter();
                drawPoints[1] = cells[i][2].getCenter();
                return true;
            }
            // 列チェック
            if (str.equals(cells[0][i].getText()) &&
                str.equals(cells[1][i].getText()) &&
                str.equals(cells[2][i].getText())) {
                drawPoints[0] = cells[0][i].getCenter();
                drawPoints[1] = cells[2][i].getCenter();
                return true;
            }
        }
        // 斜めチェック
        if (str.equals(cells[0][0].getText()) &&
            str.equals(cells[1][1].getText()) &&
            str.equals(cells[2][2].getText())) {
            drawPoints[0] = cells[0][0].getCenter();
            drawPoints[1] = cells[2][2].getCenter();
            return true;
        }
        if (str.equals(cells[0][2].getText()) &&
            str.equals(cells[1][1].getText()) &&
            str.equals(cells[2][0].getText())) {
            drawPoints[0] = cells[0][2].getCenter();
            drawPoints[1] = cells[2][0].getCenter();
            return true;
        }

        return false;
    }

    /* 盤面が埋まったか判定 */
    private boolean checkFull() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (!cells[i][j].isSelected) return false;
            }
        }
        return true;
    }

    /* ターン変更処理 & AIの処理 */
    private void changeTurn() {
        turn = !turn;
        infoLabel.setText("Turn: " + (turn ? "O" : "X"));

        if (!turn && !checkFull()) scheduleAiMove();
    }

    /* どちらかの勝利時の終了処理 */
    private void winGame() {
        System.out.println("NG");
        isEnd = true;

        overlayPanel.setOnAnimationEnd(() -> {
            // 勝者のテキスト表示
            infoLabel.setText((turn ? "O" : "X") + " wins!");
            // ボタンパネルの表示
            bottomButtonPanel.setVisible(true);
        });

        overlayPanel.startAnimation();
    }

    /* 引き分け時の終了処理*/
    private void drawGame() {
        System.out.println("OK");
         isEnd = true;

         overlayPanel.setOnAnimationEnd(null);
         infoLabel.setText("DRAW");
         bottomButtonPanel.setVisible(true);
    }

    /* ターンごとのジャッジ */
    private void judge() {
        if (checkEnd()) {
            winGame();
        } else if(checkFull()) {
            drawGame();
        } else {
            changeTurn();
        }
    }

    /* ゲームを閉じる */
    @Override
    protected void quit() {
        if (overlayPanel != null) {
            overlayPanel.stopAnimation();
            overlayPanel.setOnAnimationEnd(null);
        }

        super.quit();
    }

    /* AIの動作 */
    private void scheduleAiMove() {
        if (isEnd) return;

        int delay = 400;
        Timer aiTimer = new Timer(delay, e -> {
            switch (aiLevel) {
                case RANDOM -> aiMoveRandom();
                case BASIC -> aiMoveBasic();
                case MAGIC -> aiMoveMagic();
            }
        });
        aiTimer.setRepeats(false);
        aiTimer.start();
    }


    /* AI Lv.1 - Random */
    private void aiMoveRandom() {
        // 空いてるマスの中からランダムに置く場所を決める
        List<CellButton> emptyCells = getEmptyCells();
        if (!emptyCells.isEmpty()) {
            CellButton selected = emptyCells.get(new Random().nextInt(emptyCells.size()));
            selected.aiClick();
        }
    }

    /* 空いているセルの取得 */
    private List<CellButton> getEmptyCells() {
        List<CellButton> list = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (!cells[i][j].isSelected) list.add(cells[i][j]);
            }
        }
        return list;
    }


    /* AI Lv.2 - Basic */
    private void aiMoveBasic() {
        // 1. 勝てるなら勝つ
        if (tryPlace("X")) return;
        // 2. 相手が勝ちそうなら守る
        if (tryPlace("O")) return;
        // 3. 中央を優先
        if (!cells[1][1].isSelected) {
            cells[1][1].aiClick();
            return;
        }
        // 4. 角を取る
        int[][] corners = {{0, 0}, {0, 2}, {2, 0}, {2, 2}};
        for (int[] c : corners) {
            if (!cells[c[0]][c[1]].isSelected) {
                cells[c[0]][c[1]].aiClick();
                return;
            }
        }
        // 5. 空いてるマスに適当に置く
        aiMoveRandom();
    }

    /* 自身を選択 -> 勝ちに行く, 相手を選択 -> 守りに行く */
    private boolean tryPlace(String target) {
        for (int i = 0; i < GRID_SIZE; i++) {
            // 行
            if (tryLine(cells[i][0], cells[i][1], cells[i][2], target)) return true;
            // 列
            if (tryLine(cells[0][i], cells[1][i], cells[2][i], target)) return true;
        }
        // 斜め
        if (tryLine(cells[0][0], cells[1][1], cells[2][2], target)) return true;
        return tryLine(cells[0][2], cells[1][1], cells[2][0], target);
    }

    /* 渡された3マスのラインがリーチ状態かどうか判定し処理 */
    private boolean tryLine(CellButton a, CellButton b, CellButton c, String target) {
        int count = 0;
        CellButton emptyButton = null;

        for (CellButton cell : new CellButton[] {a, b, c}) {
            if (cell.getText().equals(target)) count++;
            else if (!cell.isSelected) emptyButton = cell;
        }

        // もしtargetが2つあって，あと1つが空のセルなら置く
        if (count == 2 && emptyButton != null) {
            emptyButton.aiClick();
            return true;
        }

        return false;
    }


    /* AI Lv.3 - Min_Max */
    private void aiMoveMagic() {
        aiMoveBasic(); // 仮置き
    }

    /* ----------------------- */
    /* 画面中央 - グリッド関係のUI */
    private void createGridUI() {
        // グリッド
        JLayeredPane gridPanel = new JLayeredPane();
        gridPanel.setPreferredSize(new Dimension(300, 300));
        gridPanel.setLayout(null);

        JPanel buttonPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 5, 5));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setBounds(0, 0, 300, 300);
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                CellButton button = new CellButton();
                cells[i][j] = button;
                buttonPanel.add(button);
            }
        }

        overlayPanel = new OverlayPanel();
        overlayPanel.setOpaque(false);
        overlayPanel.setBounds(0, 0, 300, 300);

        gridPanel.add(overlayPanel, JLayeredPane.MODAL_LAYER);
        gridPanel.add(buttonPanel, JLayeredPane.PALETTE_LAYER);

        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 50));
        centerWrapper.setOpaque(false);
        centerWrapper.add(gridPanel);

        this.add(centerWrapper, BorderLayout.CENTER);
    }

    /* 画面下部 - 情報UI */
    private void createInfoUI() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        infoLabel = new JLabel("Turn: " + (turn ? "O" : "X"), SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 50));
        bottomPanel.add(infoLabel, BorderLayout.CENTER);

        InfoButton restartButton = new InfoButton("Restart");
        restartButton.addActionListener(e -> init());
        InfoButton exitButton = new InfoButton("Exit");
        exitButton.addActionListener(e -> quit());
        bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomButtonPanel.setOpaque(false);
        bottomButtonPanel.add(restartButton);
        bottomButtonPanel.add(exitButton);
        bottomPanel.add(bottomButtonPanel, BorderLayout.SOUTH);
        bottomButtonPanel.setVisible(false);

        JPanel bottomWrapper = new JPanel();
        bottomWrapper.setLayout(new BoxLayout(bottomWrapper, BoxLayout.Y_AXIS));
        bottomWrapper.setOpaque(false);
        bottomWrapper.add(bottomPanel);
        bottomWrapper.add(Box.createVerticalStrut(100));

        this.add(bottomWrapper, BorderLayout.SOUTH);
    }


    /* グリッドのセル部分 - Button形式 */
    private class CellButton extends JButton {
        public boolean isSelected = false;

        public CellButton() {
            super();
            this.setBackground(Color.WHITE);
            this.setFont(new Font("Arial", Font.BOLD, 60));
            this.setFocusPainted(false);
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

            this.addActionListener(e -> click());
        }

        private void click() {
            if (isSelected || isEnd || !turn) return;

            this.setForeground(turn ? Color.RED : Color.BLUE);
            this.setText(turn ? "O" : "X");
            this.setBackground(turn ? new Color(255, 200, 200) : new Color(200, 200, 255));
            isSelected = true;

            judge();
        }

        private void aiClick() {
            this.setForeground(turn ? Color.RED : Color.BLUE);
            this.setText(turn ? "O" : "X");
            this.setBackground(turn ? new Color(255, 200, 200) : new Color(200, 200, 255));
            isSelected = true;

            judge();
        }

        public Vec2 getCenter() {
            double centerX = this.getX() + this.getWidth() / 2.;
            double centerY = this.getY() + this.getHeight() / 2.;
            return new Vec2(centerX, centerY);
        }
    }

    /* 3line達成時にラインを引くためのレイヤー */
    private class OverlayPanel extends JPanel {
        // アニメーション変数
        private double progress = 0.;
        private final Timer animationTimer;
        private Timer delayTimer;
        private Runnable onAnimationEnd;

        public OverlayPanel() {
            animationTimer = new Timer(16, e -> animate());
            delayTimer = new Timer(300, e -> {
                animationTimer.start();
                delayTimer.stop();
            });
            delayTimer.setRepeats(false);
        }

        public void setOnAnimationEnd(Runnable runnable) {
            this.onAnimationEnd = runnable;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!isEnd || drawPoints[0] == null || drawPoints[1] == null) return;

            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(10));

            Vec2 p1 = drawPoints[0], p2 = drawPoints[1];
            double dx = p2.x - p1.x, dy = p2.y - p1.y;
            double length = Math.sqrt(dx * dx + dy * dy);
            if (length == 0) return; // ゼロ除算対策

            double ext = 20.;   // 線の延長距離
            double ux = dx / length, uy = dy / length;

            int x1 = (int) (p1.x - ux * ext), y1 = (int) (p1.y - uy * ext);
            int x2 = (int) ((p1.x + dx * progress) + ux * ext * progress);
            int y2 = (int) ((p1.y + dy * progress) + uy * ext * progress);

            g2d.drawLine(x1, y1, x2, y2);
        }

        public void startAnimation() {
            progress = 0.;
            delayTimer.start();
        }

        public void stopAnimation() {
            animationTimer.stop();
            delayTimer.stop();
        }

        private void animate() {
            progress += .08;
            if (progress >= 1.) {
                progress = 1.;
                animationTimer.stop();
                if (onAnimationEnd != null) onAnimationEnd.run();
            }
            repaint();
        }
    }

    private class InfoButton extends JButton {

        public InfoButton(String text) {
            super(text);

            this.setContentAreaFilled(false);
            this.setOpaque(true);
            this.setFocusPainted(false);
            this.setBackground(Color.DARK_GRAY);
            this.setForeground(Color.WHITE);
            this.setFont(new Font("Arial", Font.BOLD, 16));

            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    InfoButton.this.setBackground(Color.DARK_GRAY.brighter());
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    InfoButton.this.setBackground(Color.DARK_GRAY);
                }
            });
        }
    }
}
