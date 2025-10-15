package ludibox.game.fourinarow;

import ludibox.core.GamePanel;
import ludibox.math.Vec2;
import ludibox.ui.CustomButton;
import ludibox.ui.CustomButtonStyle;

import javax.print.attribute.standard.RequestingUserName;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

public class FourInARowPanel extends GamePanel {

    // -- Constants --
    private static final int COLS = 7;
    private static final int ROWS = 6;
    private static final int MARGIN = 10;
    private static final int CELL_SIZE = 70;
    private static final int ARROW_HEIGHT = 40;
    private static final int DROP_SPEED = 20;

    // -- State --
    // 0: empty, 1: red, 2: yellow
    private final int[][] board = new int[ROWS][COLS];
    private int turn = 1;
    private boolean isFinish = false;

    // -- Animation --
    private boolean isAnimating = false;
    private int fallingCol = -1, fallingRow = -1;
    private int fallingY;
    private int fallingPieceColor;
    private Timer dropTimer;

    // -- UI --
    private Color PIECE_RED = new Color(220, 20, 60);
    private Color PIECE_YELLOW = new Color(255, 215, 0);

    private int hoverCol = -1;

    private Vec2 winStart;
    private Vec2 winEnd;


    public FourInARowPanel(ludibox.core.MainWindow m) {
        super(m);
        this.setLayout(new BorderLayout());
        this.setBackground(Color.LIGHT_GRAY);
        this.setOpaque(true);

        // マウス操作
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isAnimating && !isFinish) handleClick(e.getX(), e.getY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                handleHover(e.getX(), e.getY());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverCol = -1;
                repaint();
            }
        };

        this.addMouseListener(mouseAdapter);
        this.addMouseMotionListener(mouseAdapter);
    }

    // クリック処理
    private void handleClick(int x, int y) {
        int boardWidth = COLS * CELL_SIZE;
        int x0 = (getWidth() - boardWidth) / 2;
        int arrowTop = getHeight() - (ROWS * CELL_SIZE) - ARROW_HEIGHT - 20;

        if (y >= arrowTop && y <= arrowTop + ARROW_HEIGHT) {
            int col = (x - x0) / CELL_SIZE;
            if (col >= 0 && col < COLS) startDropAnimation(col);
        }
    }

    // ホバー処理
    private void handleHover(int x, int y) {
        int boardWidth = COLS * CELL_SIZE;
        int x0 = (getWidth() - boardWidth) / 2;
        int y0 = getHeight() - ROWS * CELL_SIZE - 20;
        int arrowTop = y0 - ARROW_HEIGHT - 5;

        if (y >= arrowTop && y <= arrowTop + ARROW_HEIGHT) {
            int col = (x - x0) / CELL_SIZE;
            if (col >= 0 && col < COLS) {
                if (hoverCol != col) {
                    hoverCol = col;
                    repaint();
                }
                return;
            }
        }

        if (hoverCol != -1) {
            hoverCol = -1;
            repaint();
        }
    }

    // コマの落下アニメーション
    private void startDropAnimation(int col) {
        int targetRow = -1;
        for (int r = ROWS - 1; r >= 0; r--) {
            if (board[r][col] == 0) {
                targetRow = r;
                break;
            }
        }

        if (targetRow == -1) return; // 列が満杯ならスルー

        isAnimating = true;
        fallingCol = col;
        fallingRow = targetRow;
        fallingPieceColor = turn;

        int boardHeight = ROWS * CELL_SIZE;
        int y0 = getHeight() - boardHeight - 20;
        fallingY = y0 - CELL_SIZE * 2; // スタート位置

        // タイマー
        dropTimer = new Timer(12, e -> {
            fallingY += DROP_SPEED;
            int dy = y0 + fallingRow * CELL_SIZE + MARGIN;
            if (fallingY >= dy) {
                fallingY = dy;
                finishDrop();
            }
            repaint();
        });
        dropTimer.start();
    }

    // 落下の完了 & 勝敗判定
    private void finishDrop() {
        dropTimer.stop();
        board[fallingRow][fallingCol] = fallingPieceColor;

        // 勝敗チェック
        if (checkWin(fallingRow, fallingCol)) {
            isFinish = true;
            repaint();
            showResultDialog(fallingPieceColor);
            return;
        }

        // 引き分けチェック
        if (isBoardFull()) {
            isFinish = true;
            repaint();
            showResultDialog(0);
            return;
        }

        turn = (turn == 1 ) ? 2 : 1;
        isAnimating = false;
        repaint();
    }

    // 勝敗判定
    private boolean checkWin(int row, int col) {
        int color = board[row][col];
        if (color == 0) return false;

        int[][] dirs = {
            {1, 0},     // 横
            {0, 1},     // 縦
            {1, 1},     // 斜め右下
            {-1, 1}     // 斜め左下
        };

        for (int[] d : dirs) {
            int count = 1; // 自身

            int[] forward = countDirection(row, col, d[0], d[1], color);
            int[] backward = countDirection(row, col, -d[0], -d[1], color);

            count += forward[0] + backward[0];

            int startRow = backward[1], startCol = backward[2];
            int endRow = forward[1], endCol = forward[2];

            if (count >= 4) {
                winStart = new Vec2(startCol, startRow);
                winEnd = new Vec2(endCol, endRow);
                return true;
            }
        }
        return false;
    }

    // 盤面が埋まっているかの判定
    private boolean isBoardFull() {
        for (int c = 0; c < COLS; c++)
            if (board[0][c] == 0) return false;
        return true;
    }

    // 指定方向の連続数をカウント
    private int[] countDirection(int row, int col, int dx, int dy, int color) {
        int count = 0;
        int r = row;
        int c = col;
        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && board[r][c] == color) {
            r += dy;
            c += dx;
            if (r < 0 || r >= ROWS || c < 0 || c >= COLS || board[r][c] != color) break;
            count++;
        }
        return new int[] {count, r - dy, c - dx};
    }

    // ゲームリセット
    private void resetGame() {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                board[r][c] = 0;

        turn = new Random().nextInt(1, 3);
        fallingRow = -1; fallingCol = -1;
        isFinish = false;
        isAnimating = false;
        winStart = null;
        winEnd = null;

        this.removeAll();
        this.revalidate();
        this.repaint();
    }


    // -- Rendering --
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int boardWidth = COLS * CELL_SIZE;
        int boardHeight = ROWS * CELL_SIZE;

        // centered
        int x0 = (getWidth() - boardWidth) / 2;
        int y0 = getHeight() - boardHeight - 10;

        // arrows
        int arrowTop = y0 - ARROW_HEIGHT - 10;

        for (int c = 0; c < COLS; c++) {
            int cx = x0 + c * CELL_SIZE + CELL_SIZE / 2;
            Polygon triangle = new Polygon();
            triangle.addPoint(cx, arrowTop + ARROW_HEIGHT);
            triangle.addPoint(cx - 15, arrowTop + 10);
            triangle.addPoint(cx + 15, arrowTop + 10);

            if (c == hoverCol) g2d.setColor(new Color(100, 180, 255));
            else g2d.setColor(new Color(60, 60, 60, 160));

            g2d.fill(triangle);
        }

        // static pieces
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int piece = board[r][c];
                if (piece == 0) continue;
                drawPiece(g2d, piece, x0 + c * CELL_SIZE + MARGIN, y0 + r * CELL_SIZE + MARGIN);
            }
        }

        // falling piece
        if (isAnimating) {
            int x = x0 + fallingCol * CELL_SIZE + MARGIN;
            drawPiece(g2d, fallingPieceColor, x, fallingY);
        }

        // board
        RoundRectangle2D boardRect = new RoundRectangle2D.Float(x0, y0, boardWidth, boardHeight, 20, 20);
        Area boardArea = new Area(boardRect);

        // hole
        int diameter = CELL_SIZE - MARGIN * 2;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int cx = x0 + c * CELL_SIZE + MARGIN;
                int cy = y0 + r * CELL_SIZE + MARGIN;
                boardArea.subtract(new Area(new Ellipse2D.Float(cx, cy, diameter, diameter)));
            }
        }

        // fill
        g2d.setColor(new Color(60, 60, 60));
        g2d.fill(boardArea);


        // Winner's line
        if (winStart != null && winEnd != null) {
            int sx = (int) (x0 + winStart.x * CELL_SIZE + (double) CELL_SIZE / 2);
            int sy = (int) (y0 + winStart.y * CELL_SIZE + (double) CELL_SIZE / 2);
            int ex = (int) (x0 + winEnd.x * CELL_SIZE + (double) CELL_SIZE / 2);
            int ey = (int) (y0 + winEnd.y * CELL_SIZE + (double) CELL_SIZE / 2);

            g2d.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(Color.WHITE);
            g2d.drawLine(sx, sy, ex, ey);
        }

        g2d.dispose();
    }

    private void drawPiece(Graphics2D g2d, int colorType, int x, int y) {
        int d = CELL_SIZE - MARGIN * 2;
        Color base = (colorType == 1) ? PIECE_RED : PIECE_YELLOW;
        GradientPaint gp = new GradientPaint(x, y, base.brighter(), x, y + d, base.darker());
        g2d.setPaint(gp);
        g2d.fillOval(x, y, d, d);
    }

    private void showResultDialog(int winner) {
        SwingUtilities.invokeLater(() -> {
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this));

            dialog.setUndecorated(true);
            dialog.setBackground(new Color(0, 0, 0, 0));
            dialog.setModal(true);
            dialog.getContentPane().add(new ResultPanel(dialog, winner));
            dialog.pack();
            dialog.setLocationRelativeTo(window);
            dialog.setVisible(true);
        });
    }


    // -- Result Panel --
    private class ResultPanel extends JPanel {
        JDialog dialog;

        public ResultPanel(JDialog dialog, int winner) {
            this.dialog = dialog;

            this.setPreferredSize(new Dimension(500, 300));
            this.setOpaque(false);

            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            // テキスト
            String text;
            if (winner == 0) text = "It's a draw!";
            else text = (winner == 1) ? "Red wins!" : "Yellow wins!";
            JLabel textLabel = new JLabel(text);
            textLabel.setFont(new Font("Mono", Font.BOLD, 36));
            textLabel.setForeground(Color.WHITE);
            textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Restart
            CustomButton restartButton = new CustomButton("Restart", CustomButtonStyle.SIMPLE);
            restartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            restartButton.addActionListener(e -> {
                resetGame();
                dialog.dispose();
                remove(dialog);
                revalidate();
                repaint();
            });

            // Exit
            CustomButton exitButton = new CustomButton("Exit", CustomButtonStyle.SIMPLE);
            exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            exitButton.addActionListener(e -> {
                quit();
                dialog.dispose();
                remove(dialog);
                revalidate();
                repaint();
            });

            this.add(Box.createRigidArea(new Dimension(0, 80)));
            this.add(textLabel);
            this.add(Box.createRigidArea(new Dimension(0, 20)));
            this.add(restartButton);
            this.add(Box.createRigidArea(new Dimension(0, 20)));
            this.add(exitButton);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(60, 60, 60, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.dispose();

            super.paintComponent(g);
        }
    }
}
