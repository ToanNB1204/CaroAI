package scr;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * WinOverlayPanel — Màn hình kết thúc ván.
 *
 * Hiển thị toàn màn hình trên LayeredPane của GameFrame với:
 *  - Nền mờ blur-style
 *  - Confetti rơi (chỉ khi thắng)
 *  - Card trung tâm slide-in + fade-in
 *  - Badge ký hiệu thắng (X / O) phát sáng
 *  - Tiêu đề VICTORY / DEFEAT
 *  - Nút Play Again & Main Menu
 */
public class WinOverlayPanel extends JPanel {

    // ── Màu sắc ───────────────────────────────────────────────
    private static final Color OVERLAY_BG   = new Color(5,   8,  20, 210);
    private static final Color CARD_BG      = new Color(18,  22,  45);
    private static final Color CARD_BORDER  = new Color(255, 255, 255, 30);
    private static final Color ACCENT_X     = new Color(229,  57,  53);
    private static final Color ACCENT_O     = new Color( 30, 136, 229);
    private static final Color GOLD         = new Color(255, 200,  50);
    private static final Color SILVER       = new Color(140, 148, 170);
    private static final Color TEXT_MAIN    = new Color(225, 230, 255);
    private static final Color TEXT_DIM     = new Color(110, 120, 160);

    // Màu confetti
    private static final Color[] CONFETTI_COLORS = {
        new Color(255,  80,  80), new Color( 80, 160, 255),
        new Color(255, 200,  50), new Color( 80, 220, 120),
        new Color(200,  80, 255), new Color(255, 140,  50),
    };

    // ── Trạng thái ────────────────────────────────────────────
    private final String  winnerName;
    private final String  symbol;       // "X" hoặc "O"
    private final boolean humanWon;     // true = thắng, false = thua (khi chơi vs AI)
    private final boolean vsAI;

    // ── Callback ─────────────────────────────────────────────
    private final Runnable onPlayAgain;
    private final Runnable onMainMenu;

    // ── Entrance animation ────────────────────────────────────
    private float progress   = 0f;   // 0→1 card slide-in
    private float bgAlpha    = 0f;   // 0→1 overlay fade
    private final Timer entranceTimer;

    // ── Confetti ─────────────────────────────────────────────
    private static final int CONFETTI_COUNT = 90;
    private final float[] cx, cy, cvx, cvy, cangle, crot, csize;
    private final int[]   ccolorIdx, cshape;  // shape: 0=rect, 1=circle
    private final Timer confettiTimer;

    // ── Buttons ───────────────────────────────────────────────
    private OverlayButton btnPlayAgain, btnMainMenu;
    private boolean buttonsAdded = false;

    public WinOverlayPanel(String winnerName, String symbol,
                           boolean humanWon, boolean vsAI,
                           Runnable onPlayAgain, Runnable onMainMenu) {
        this.winnerName   = winnerName;
        this.symbol       = symbol;
        this.humanWon     = humanWon;
        this.vsAI         = vsAI;
        this.onPlayAgain  = onPlayAgain;
        this.onMainMenu   = onMainMenu;

        setLayout(null);
        setOpaque(false);

        // ── Confetti init ─────────────────────────────────────
        Random rng = new Random();
        cx       = new float[CONFETTI_COUNT];
        cy       = new float[CONFETTI_COUNT];
        cvx      = new float[CONFETTI_COUNT];
        cvy      = new float[CONFETTI_COUNT];
        cangle   = new float[CONFETTI_COUNT];
        crot     = new float[CONFETTI_COUNT];
        csize    = new float[CONFETTI_COUNT];
        ccolorIdx= new int  [CONFETTI_COUNT];
        cshape   = new int  [CONFETTI_COUNT];

        for (int i = 0; i < CONFETTI_COUNT; i++) resetConfetti(i, rng, true);

        // ── Confetti timer ────────────────────────────────────
        confettiTimer = new Timer(16, e -> {
            for (int i = 0; i < CONFETTI_COUNT; i++) {
                cy[i]     += cvy[i];
                cx[i]     += cvx[i];
                cvy[i]    += 0.12f;          // gravity
                cangle[i] += crot[i];
                if (cy[i] > getHeight() + 20) resetConfetti(i, rng, false);
            }
            repaint();
        });
        if (humanWon || !vsAI) confettiTimer.start();

        // ── Entrance timer ────────────────────────────────────
        entranceTimer = new Timer(12, e -> {
            progress = Math.min(1f, progress + 0.04f);
            bgAlpha  = Math.min(1f, bgAlpha  + 0.06f);
            if (progress >= 1f) ((Timer)e.getSource()).stop();
            repaint();
        });
        entranceTimer.start();

        // Thêm nút sau khi panel có kích thước
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!buttonsAdded && getWidth() > 0) {
                    layoutButtons();
                    buttonsAdded = true;
                }
            }
        });
    }

    private void resetConfetti(int i, Random rng, boolean fromTop) {
        cx[i]       = rng.nextFloat();
        cy[i]       = fromTop ? -20 - rng.nextFloat() * 300 : -20;
        cvx[i]      = (rng.nextFloat() - 0.5f) * 2f;
        cvy[i]      = 1.5f + rng.nextFloat() * 2.5f;
        cangle[i]   = rng.nextFloat() * 360;
        crot[i]     = (rng.nextFloat() - 0.5f) * 6f;
        csize[i]    = 6 + rng.nextFloat() * 8;
        ccolorIdx[i]= rng.nextInt(CONFETTI_COLORS.length);
        cshape[i]   = rng.nextInt(2);
    }

    private void layoutButtons() {
        int W = getWidth(), H = getHeight();
        int btnW = 180, btnH = 48, gap = 20;
        int totalW = btnW * 2 + gap;
        int bx = W / 2 - totalW / 2;
        int by = H / 2 + 110;

        btnPlayAgain = new OverlayButton("▶  Play Again", GOLD, new Color(20, 15, 0));
        btnPlayAgain.setBounds(bx, by, btnW, btnH);
        btnPlayAgain.addActionListener(e -> {
            confettiTimer.stop();
            onPlayAgain.run();
        });

        btnMainMenu = new OverlayButton("⌂  Main Menu", new Color(50, 55, 80), TEXT_MAIN);
        btnMainMenu.setBounds(bx + btnW + gap, by, btnW, btnH);
        btnMainMenu.addActionListener(e -> {
            confettiTimer.stop();
            onMainMenu.run();
        });

        add(btnPlayAgain);
        add(btnMainMenu);
        revalidate();
    }

    // ── Vẽ overlay ────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();

        // ── Nền tối mờ ───────────────────────────────────────
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bgAlpha));
        g2.setColor(OVERLAY_BG);
        g2.fillRect(0, 0, W, H);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // ── Confetti ─────────────────────────────────────────
        if (humanWon || !vsAI) {
            for (int i = 0; i < CONFETTI_COUNT; i++) {
                Color c = CONFETTI_COLORS[ccolorIdx[i]];
                float confettiAlpha = Math.min(1f, bgAlpha * 1.5f);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, confettiAlpha));
                g2.setColor(c);
                AffineTransform old = g2.getTransform();
                int px = (int)(cx[i] * W), py = (int)cy[i];
                g2.rotate(Math.toRadians(cangle[i]), px, py);
                if (cshape[i] == 0)
                    g2.fillRect(px - (int)(csize[i]/2), py - (int)(csize[i]/4),
                                (int)csize[i], (int)(csize[i]/2));
                else
                    g2.fillOval(px - (int)(csize[i]/2), py - (int)(csize[i]/2),
                                (int)csize[i], (int)csize[i]);
                g2.setTransform(old);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
        }

        // ── Card trung tâm (slide-in + scale) ────────────────
        float easedProgress = easeOutBack(progress);
        int cardW = 480, cardH = 300;
        int cardX = W / 2 - cardW / 2;
        int cardY = H / 2 - cardH / 2;

        // Scale từ 0.7→1.0
        float scale = 0.7f + 0.3f * easedProgress;
        AffineTransform cardTransform = AffineTransform.getTranslateInstance(W / 2.0, H / 2.0);
        cardTransform.scale(scale, scale);
        cardTransform.translate(-cardW / 2.0, -cardH / 2.0);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, progress * 2)));
        g2.setTransform(cardTransform);

        // Shadow của card
        for (int s = 20; s > 0; s -= 4) {
            g2.setColor(new Color(0, 0, 0, 8));
            g2.fill(new RoundRectangle2D.Float(-s, -s + 8, cardW + s*2, cardH + s*2, 28, 28));
        }

        // Nền card
        g2.setColor(CARD_BG);
        g2.fill(new RoundRectangle2D.Float(0, 0, cardW, cardH, 24, 24));

        // Viền card
        Color borderColor = humanWon || !vsAI ? GOLD : SILVER;
        g2.setColor(new Color(borderColor.getRed(), borderColor.getGreen(),
                              borderColor.getBlue(), 80));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, cardW - 1, cardH - 1, 24, 24));

        // Dải màu accent trên đỉnh card
        Color accent = symbol.equals("X") ? ACCENT_X : ACCENT_O;
        GradientPaint topBar = new GradientPaint(
            0, 0, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180),
            cardW, 0, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0)
        );
        g2.setPaint(topBar);
        g2.fill(new RoundRectangle2D.Float(0, 0, cardW, 5, 5, 5));

        // ── Nội dung card ─────────────────────────────────────
        g2.setTransform(cardTransform);

        // Badge ký hiệu
        int badgeX = 52, badgeY = cardH / 2 - 20;
        drawSymbolBadge(g2, symbol, badgeX, badgeY, accent);

        // Tiêu đề thắng/thua
        String headline, subline;
        Color headColor;
        if (!vsAI) {
            headline = "GAME OVER";
            headColor = GOLD;
            subline   = winnerName + " wins!";
        } else if (humanWon) {
            headline  = "VICTORY!";
            headColor = GOLD;
            subline   = "You defeated the AI";
        } else {
            headline  = "DEFEAT";
            headColor = SILVER;
            subline   = "The AI wins this round";
        }

        g2.setFont(new Font("Monospaced", Font.BOLD, 34));
        g2.setColor(headColor);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(headline, 110, cardH / 2 - 18);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2.setColor(TEXT_DIM);
        g2.drawString(subline, 110, cardH / 2 + 10);

        // Player name
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.setColor(TEXT_MAIN);
        g2.drawString(winnerName, 110, cardH / 2 + 35);

        // Gợi ý phím dưới card (vẽ trong không gian gốc)
        g2.setTransform(new AffineTransform());
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                        Math.min(1f, Math.max(0f, progress - 0.5f) * 2)));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(TEXT_DIM);
        String hint = "Press R to play again  ·  Esc for main menu";
        fm = g2.getFontMetrics();
        g2.drawString(hint, W / 2 - fm.stringWidth(hint) / 2, H / 2 + 175);

        g2.dispose();
    }

    private void drawSymbolBadge(Graphics2D g2, String sym, int cx, int cy, Color accent) {
        int r = 34;
        // Glow
        for (int i = 3; i >= 1; i--) {
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 20 * i));
            g2.fillOval(cx - r - i*5, cy - r - i*5, (r + i*5)*2, (r + i*5)*2);
        }
        // Vòng tròn nền
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 50));
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        // Viền
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 160));
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        // Ký hiệu
        g2.setFont(new Font("Monospaced", Font.BOLD, 36));
        g2.setColor(accent.brighter());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(sym, cx - fm.stringWidth(sym)/2, cy + fm.getAscent()/2 - 4);
    }

    // Easing: easeOutBack tạo hiệu ứng nảy nhẹ khi xuất hiện
    private float easeOutBack(float t) {
        float c1 = 1.70158f, c3 = c1 + 1;
        return 1 + c3 * (float)Math.pow(t - 1, 3) + c1 * (float)Math.pow(t - 1, 2);
    }

    // ── Keyboard shortcut (R = play again, Esc = menu) ────────
    public void attachKeyBindings(JRootPane rootPane) {
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("R"), "playAgain");
        rootPane.getActionMap().put("playAgain", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                confettiTimer.stop();
                onPlayAgain.run();
            }
        });
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "mainMenu");
        rootPane.getActionMap().put("mainMenu", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                confettiTimer.stop();
                onMainMenu.run();
            }
        });
    }

    // ─── Inner: Nút bấm trong overlay ────────────────────────
    static class OverlayButton extends JComponent {
        private final String label;
        private final Color accent, textColor;
        private boolean hovered = false;
        private float hoverAnim = 0f;
        private final Timer anim;
        private final java.util.List<ActionListener> listeners = new java.util.ArrayList<>();

        OverlayButton(String label, Color accent, Color textColor) {
            this.label     = label;
            this.accent    = accent;
            this.textColor = textColor;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            anim = new Timer(16, e -> {
                float target = hovered ? 1f : 0f;
                hoverAnim += (target - hoverAnim) * 0.18f;
                repaint();
            });
            anim.start();

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true; }
                public void mouseExited(MouseEvent e)  { hovered = false; }
                public void mouseReleased(MouseEvent e) {
                    if (hovered) listeners.forEach(l ->
                        l.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, label)));
                }
            });
        }

        public void addActionListener(ActionListener l) { listeners.add(l); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            int arc = H;

            // Glow
            if (hoverAnim > 0.05f) {
                for (int i = 12; i > 0; i -= 4) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(),
                                         (int)(hoverAnim * 0.06f * (12 - i + 1) * 255)));
                    g2.fill(new RoundRectangle2D.Float(-i, -i, W+i*2, H+i*2, arc+i, arc+i));
                }
            }

            // Fill
            float fillA = 0.70f + 0.25f * hoverAnim;
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(),
                                  (int)(fillA * 255)));
            g2.fill(new RoundRectangle2D.Float(0, 0, W, H, arc, arc));

            // Highlight trên
            g2.setPaint(new GradientPaint(0, 0, new Color(255,255,255,50), 0, H/2f, new Color(255,255,255,0)));
            g2.fill(new RoundRectangle2D.Float(1, 1, W-2, H/2f, arc-2, arc-2));

            // Label
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.setColor(textColor);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, W/2 - fm.stringWidth(label)/2, H/2 + fm.getAscent()/2 - 3);

            g2.dispose();
        }
    }
}