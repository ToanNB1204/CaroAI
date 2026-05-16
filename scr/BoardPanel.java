package scr;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class BoardPanel extends JPanel {
    private GameEngine engine;
    private AI ai;
    private int cellSize = 40;
    private int offsetX = 0, offsetY = 0;
    private Point lastMove = null;
    private List<Point> winningLine = new ArrayList<>();
    private Point hoverPoint = null;
    private boolean gameOver = false;   // Ngăn click sau khi ván kết thúc

    public BoardPanel(GameEngine engine, AI ai) {
        this.engine = engine;
        this.ai     = ai;
        setPreferredSize(new Dimension(800, 800));

        // Hover preview
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (gameOver) return;
                int col = e.getX() / cellSize + offsetX;
                int row = e.getY() / cellSize + offsetY;
                Point p = new Point(col, row);
                if (!p.equals(hoverPoint)) { hoverPoint = p; repaint(); }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!gameOver) handleClick(e.getX(), e.getY());
                requestFocusInWindow();
            }
            @Override
            public void mouseExited(MouseEvent e) { hoverPoint = null; repaint(); }
        });

        // Phím WASD di chuyển camera
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: offsetY--; break;
                    case KeyEvent.VK_S: offsetY++; break;
                    case KeyEvent.VK_A: offsetX--; break;
                    case KeyEvent.VK_D: offsetX++; break;
                }
                repaint();
            }
        });
    }

    //  Xử lý click 
    private void handleClick(int mouseX, int mouseY) {
        int col = mouseX / cellSize + offsetX;
        int row = mouseY / cellSize + offsetY;
        Point p = new Point(col, row);

        GameEngine.Cell playerSymbol = engine.getPlayerSympol();

        if (engine.makeMove(p, playerSymbol)) {
            lastMove = p;
            ((GameFrame) SwingUtilities.getWindowAncestor(this)).recordMove(p);

            // Đồng bộ AI board (chỉ khi có AI)
            if (ai != null)
                ai.setCell(row, col, playerSymbol == GameEngine.Cell.X ? 1 : -1);

            repaint();

            if (engine.checkWin(p)) {
                winningLine = engine.getWinningLine(p);
                triggerWin();
                return;
            }

            engine.switchTurn();

            // AI đi
            if (ai != null && engine.getCurrentPlayer().isAI()) {
                Timer timer = new Timer(300, ev -> {
                    Point best = ai.getBestMove(engine.getBoard(), engine.getAISymbol());
                    if (best != null) {
                        Point aiMove = new Point(best.x, best.y);
                        engine.makeMove(aiMove, engine.getAISymbol());
                        ai.setCell(best.y, best.x,
                                   engine.getAISymbol() == GameEngine.Cell.X ? 1 : -1);
                        lastMove = aiMove;
                        ((GameFrame) SwingUtilities.getWindowAncestor(this)).recordMove(aiMove);
                        repaint();

                        if (engine.checkWin(aiMove)) {
                            winningLine = engine.getWinningLine(aiMove);
                            triggerWin();
                            return;
                        }
                        engine.switchTurn();
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        }
    }

    //  Kích hoạt chuỗi thắng → overlay 
    private void triggerWin() {
        gameOver = true;
        hoverPoint = null;

        // Nhấp nháy 6 lần (300ms/lần) rồi hiện overlay
        Timer blink = new Timer(300, null);
        final int[] count = {0};
        blink.addActionListener(e -> {
            count[0]++;
            repaint();
            if (count[0] >= 6) {
                blink.stop();
                // Gọi GameFrame hiển thị overlay đẹp
                GameFrame frame = (GameFrame) SwingUtilities.getWindowAncestor(this);
                if (frame != null) frame.showWinOverlay(engine.getCurrentPlayer());
            }
        });
        blink.start();
    }

    //  Cho phép reset trạng thái khi ván mới 
    public void resetState() {
        gameOver    = false;
        lastMove    = null;
        winningLine = new ArrayList<>();
        hoverPoint  = null;
        offsetX     = 0;
        offsetY     = 0;
    }

    //  Vẽ 
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Nền
        g2d.setColor(new Color(248, 249, 250));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        int viewRows = getHeight() / cellSize + 1;
        int viewCols = getWidth()  / cellSize + 1;

        // Lưới
        g2d.setColor(new Color(222, 226, 230));
        g2d.setStroke(new BasicStroke(1.0f));
        for (int i = 0; i <= viewCols; i++)
            g2d.drawLine(i * cellSize, 0, i * cellSize, getHeight());
        for (int i = 0; i <= viewRows; i++)
            g2d.drawLine(0, i * cellSize, getWidth(), i * cellSize);

        // Hover preview
        if (hoverPoint != null && !engine.getBoard().containsKey(hoverPoint)
                && !engine.getCurrentPlayer().isAI()) {
            int screenX = (hoverPoint.x - offsetX) * cellSize;
            int screenY = (hoverPoint.y - offsetY) * cellSize;
            if (screenX >= 0 && screenY >= 0 && screenX < getWidth() && screenY < getHeight()) {
                GameEngine.Cell cur = engine.getCurrentPlayer().getSymbol().equals("X")
                        ? GameEngine.Cell.X : GameEngine.Cell.O;
                g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int pad = 10;
                if (cur == GameEngine.Cell.X) {
                    g2d.setColor(new Color(231, 76, 60, 70));
                    g2d.drawLine(screenX+pad, screenY+pad, screenX+cellSize-pad, screenY+cellSize-pad);
                    g2d.drawLine(screenX+cellSize-pad, screenY+pad, screenX+pad, screenY+cellSize-pad);
                } else {
                    g2d.setColor(new Color(41, 128, 185, 70));
                    g2d.drawOval(screenX+pad, screenY+pad, cellSize-pad*2, cellSize-pad*2);
                }
            }
        }

        // Quân cờ
        g2d.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (Point p : engine.getBoard().keySet()) {
            int screenX = (p.x - offsetX) * cellSize;
            int screenY = (p.y - offsetY) * cellSize;
            if (screenX < -cellSize || screenY < -cellSize ||
                screenX > getWidth() || screenY > getHeight()) continue;
            int pad = 8;
            if (engine.getBoard().get(p) == GameEngine.Cell.X) {
                g2d.setColor(new Color(231, 76, 60));
                g2d.drawLine(screenX+pad, screenY+pad, screenX+cellSize-pad, screenY+cellSize-pad);
                g2d.drawLine(screenX+cellSize-pad, screenY+pad, screenX+pad, screenY+cellSize-pad);
            } else {
                g2d.setColor(new Color(41, 128, 185));
                g2d.drawOval(screenX+pad, screenY+pad, cellSize-pad*2, cellSize-pad*2);
            }
        }

        // Highlight nước đi cuối
        if (lastMove != null) {
            int sx = (lastMove.x - offsetX) * cellSize;
            int sy = (lastMove.y - offsetY) * cellSize;
            if (sx >= 0 && sy >= 0 && sx < getWidth() && sy < getHeight()) {
                g2d.setColor(new Color(46, 204, 113, 40));
                g2d.fillRect(sx+1, sy+1, cellSize-1, cellSize-1);
                g2d.setColor(new Color(46, 204, 113));
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawRect(sx+1, sy+1, cellSize-2, cellSize-2);
            }
        }

        // Highlight đường thắng
        if (!winningLine.isEmpty()) {
            g2d.setColor(new Color(241, 196, 15, 45));
            for (Point p : winningLine) {
                int sx = (p.x - offsetX) * cellSize;
                int sy = (p.y - offsetY) * cellSize;
                if (sx >= 0 && sy >= 0 && sx < getWidth() && sy < getHeight())
                    g2d.fillRect(sx+1, sy+1, cellSize-1, cellSize-1);
            }
            g2d.setColor(new Color(241, 196, 15));
            g2d.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (winningLine.size() >= 2) {
                Point first = winningLine.get(0);
                Point last  = winningLine.get(winningLine.size()-1);
                g2d.drawLine(
                    (first.x - offsetX)*cellSize + cellSize/2,
                    (first.y - offsetY)*cellSize + cellSize/2,
                    (last.x  - offsetX)*cellSize + cellSize/2,
                    (last.y  - offsetY)*cellSize + cellSize/2
                );
            }
        }
    }
}