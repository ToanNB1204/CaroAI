package scr;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * Màn hình Start Menu với:
 * - Nền sao động (particle stars)
 * - Tiêu đề CARO phát sáng
 * - Chọn chế độ chơi: vs AI / 2 Người
 * - Chọn ký hiệu X / O (chỉ vs AI)
 * - Nút Start Game & Exit
 */
public class StartMenuPanel extends JPanel {

    private static final Color BG_CENTER   = new Color( 18,  22,  45);
    private static final Color BG_EDGE     = new Color(  8,  10,  22);
    private static final Color ACCENT_X    = new Color(229,  57,  53);
    private static final Color ACCENT_O    = new Color( 30, 136, 229);
    private static final Color ACCENT_GOLD = new Color(255, 193,  50);
    private static final Color TEXT_MAIN   = new Color(225, 230, 255);
    private static final Color TEXT_DIM    = new Color(110, 120, 160);
    private static final Color CARD_BG     = new Color(255, 255, 255, 14);
    private static final Color CARD_BORDER = new Color(255, 255, 255, 30);

    //  Trạng thái chọn
    private int selectedMode   = -1;   // 0 = vs AI, 1 = 2 Người
    private int selectedSymbol = 0;    // 0 = X, 1 = O

    // Callback khi nhấn Start 
    private final Runnable onStart;

    //Stars 
    private final float[] sx, sy, sr, sa;
    private final float[] sdx, sdy;
    private static final int STAR_COUNT = 120;
    private final Timer starTimer;

    //UI Components
    private ModeCard cardAI, cardHuman;
    private SymbolToggle symbolToggle;
    private GlowButton startBtn, exitBtn;
    private JLabel symbolLabel;

    public StartMenuPanel(Runnable onStart) {
        this.onStart = onStart;
        setLayout(null);
        setBackground(BG_EDGE);

        Random rng = new Random(42);
        sx  = new float[STAR_COUNT]; sy  = new float[STAR_COUNT];
        sr  = new float[STAR_COUNT]; sa  = new float[STAR_COUNT];
        sdx = new float[STAR_COUNT]; sdy = new float[STAR_COUNT];
        for (int i = 0; i < STAR_COUNT; i++) {
            sx[i]  = rng.nextFloat();
            sy[i]  = rng.nextFloat();
            sr[i]  = 0.5f + rng.nextFloat() * 1.8f;
            sa[i]  = 0.2f + rng.nextFloat() * 0.8f;
            sdx[i] = (rng.nextFloat() - 0.5f) * 0.00015f;
            sdy[i] = (rng.nextFloat() - 0.5f) * 0.00015f;
        }

        starTimer = new Timer(30, e -> {
            for (int i = 0; i < STAR_COUNT; i++) {
                sx[i] += sdx[i]; sy[i] += sdy[i];
                if (sx[i] < 0) sx[i] = 1; if (sx[i] > 1) sx[i] = 0;
                if (sy[i] < 0) sy[i] = 1; if (sy[i] > 1) sy[i] = 0;
                // Nhấp nháy nhẹ
                sa[i] += (rng.nextFloat() - 0.5f) * 0.03f;
                sa[i] = Math.max(0.1f, Math.min(1f, sa[i]));
            }
            repaint();
        });
        starTimer.start();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutComponents();
            }
        });
    }

    //Đặt vị trí component khi resize 
    private void layoutComponents() {
        removeAll();
        int W = getWidth(), H = getHeight();
        if (W == 0 || H == 0) return;

        int centerX = W / 2;

        //Cards chế độ
        int cardW = 200, cardH = 130, cardGap = 24;
        int cardY = H / 2 - 30;

        cardAI    = new ModeCard("🤖", "vs Máy", "Thử sức với AI", 0);
        cardHuman = new ModeCard("👥", "2 Người", "Chơi cùng bạn bè", 1);

        int totalCards = cardW * 2 + cardGap;
        int startX = centerX - totalCards / 2;

        cardAI.setBounds(startX, cardY, cardW, cardH);
        cardHuman.setBounds(startX + cardW + cardGap, cardY, cardW, cardH);

        add(cardAI);
        add(cardHuman);

        //Chọn ký hiệu (chỉ hiện khi vs AI) 
        symbolLabel = new JLabel("Chọn ký hiệu của bạn:");
        symbolLabel.setForeground(TEXT_DIM);
        symbolLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        symbolLabel.setHorizontalAlignment(SwingConstants.CENTER);
        symbolLabel.setBounds(centerX - 130, cardY + cardH + 18, 260, 18);
        add(symbolLabel);

        symbolToggle = new SymbolToggle();
        symbolToggle.setBounds(centerX - 60, cardY + cardH + 42, 120, 36);
        add(symbolToggle);

        //Nút Start 
        startBtn = new GlowButton("▶  BẮT ĐẦU", ACCENT_GOLD, new Color(20, 15, 5));
        startBtn.setBounds(centerX - 110, cardY + cardH + 100, 220, 50);
        startBtn.addActionListener(e -> {
            if (selectedMode == -1) {
                shakePanel(cardAI);
                shakePanel(cardHuman);
                return;
            }
            starTimer.stop();
            onStart.run();
        });
        add(startBtn);

        //Nút Exit 
        exitBtn = new GlowButton("Thoát", new Color(80, 85, 110), TEXT_MAIN);
        exitBtn.setBounds(centerX - 60, cardY + cardH + 162, 120, 36);
        exitBtn.addActionListener(e -> System.exit(0));
        add(exitBtn);

        updateSymbolVisibility();
        revalidate();
        repaint();
    }

    private void updateSymbolVisibility() {
        boolean show = (selectedMode == 0);
        if (symbolLabel   != null) symbolLabel.setVisible(show);
        if (symbolToggle  != null) symbolToggle.setVisible(show);
    }

    //Getters để GameFrame đọc lựa chọn
    public int  getSelectedMode()   { return selectedMode; }
    public boolean isVsAI()         { return selectedMode == 0; }
    public boolean playerChoseX()   { return selectedSymbol == 0; }

    //Hiệu ứng rung khi chưa chọn 
    private void shakePanel(JComponent c) {
        Point orig = c.getLocation();
        Timer t = new Timer(40, null);
        final int[] ticks = {0};
        final int[] offsets = {-6, 6, -5, 5, -3, 3, -1, 1, 0};
        t.addActionListener(e -> {
            if (ticks[0] < offsets.length) {
                c.setLocation(orig.x + offsets[ticks[0]], orig.y);
                ticks[0]++;
            } else {
                c.setLocation(orig);
                t.stop();
            }
        });
        t.start();
    }

    // Vẽ nền + stars + tiêu đề 
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();

        // Nền radial gradient
        RadialGradientPaint bgPaint = new RadialGradientPaint(
            W / 2f, H / 2f, Math.max(W, H) * 0.65f,
            new float[]{0f, 1f},
            new Color[]{BG_CENTER, BG_EDGE}
        );
        g2.setPaint(bgPaint);
        g2.fillRect(0, 0, W, H);

        // Lưới mờ trang trí
        g2.setColor(new Color(255, 255, 255, 5));
        g2.setStroke(new BasicStroke(1));
        int gridSize = 50;
        for (int x = 0; x < W; x += gridSize)
            g2.drawLine(x, 0, x, H);
        for (int y = 0; y < H; y += gridSize)
            g2.drawLine(0, y, W, y);

        // Stars
        for (int i = 0; i < STAR_COUNT; i++) {
            int px = (int)(sx[i] * W);
            int py = (int)(sy[i] * H);
            float r  = sr[i];
            g2.setColor(new Color(1f, 1f, 1f, sa[i]));
            g2.fillOval((int)(px - r), (int)(py - r), (int)(r * 2), (int)(r * 2));
        }

        //Tiêu đề CARO
        int titleY = H / 2 - 170;
        drawGlowText(g2, "CARO", W / 2, titleY, 78, ACCENT_GOLD);

        // Sub-title
        g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g2.setColor(TEXT_DIM);
        String sub = "STRATEGY BOARD GAME";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(sub, W / 2 - fm.stringWidth(sub) / 2, titleY + 28);

        // Đường kẻ trang trí dưới tiêu đề
        int lineW = 200;
        GradientPaint linePaint = new GradientPaint(
            W / 2f - lineW / 2f, 0, new Color(0, 0, 0, 0),
            W / 2f, 0, ACCENT_GOLD,
            true
        );
        g2.setPaint(linePaint);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(W / 2 - lineW / 2, titleY + 42, W / 2 + lineW / 2, titleY + 42);

        //Nhãn "Chọn chế độ"
        if (getComponentCount() > 0) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.setColor(TEXT_DIM);
            String modeLabel = "CHỌN CHẾ ĐỘ CHƠI";
            fm = g2.getFontMetrics();
            int cardY = H / 2 - 30;
            g2.drawString(modeLabel, W / 2 - fm.stringWidth(modeLabel) / 2, cardY - 14);
        }

        g2.dispose();
    }

    private void drawGlowText(Graphics2D g2, String text, int cx, int cy, int size, Color color) {
        Font f = new Font("Monospaced", Font.BOLD, size);
        g2.setFont(f);
        FontMetrics fm = g2.getFontMetrics();
        int tx = cx - fm.stringWidth(text) / 2;
        int ty = cy + fm.getAscent() / 2 - 8;

        // Glow layers
        for (int radius = 24; radius >= 4; radius -= 4) {
            float alpha = 0.04f + (24 - radius) * 0.004f;
            g2.setColor(new Color(color.getRed() / 255f,
                                  color.getGreen() / 255f,
                                  color.getBlue() / 255f, alpha));
            for (int dx = -radius; dx <= radius; dx += radius)
                for (int dy = -radius; dy <= radius; dy += radius)
                    if (dx != 0 || dy != 0)
                        g2.drawString(text, tx + dx, ty + dy);
        }

        // Chữ chính
        g2.setColor(color);
        g2.drawString(text, tx, ty);
    }

    // Inner: Card chế độ 
    class ModeCard extends JComponent {
        private final String icon, title, desc;
        private final int mode;
        private float hoverAnim = 0f;
        private boolean hovered = false;
        private final Timer anim;

        ModeCard(String icon, String title, String desc, int mode) {
            this.icon = icon; this.title = title;
            this.desc = desc; this.mode  = mode;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            anim = new Timer(16, e -> {
                float target = hovered ? 1f : 0f;
                hoverAnim += (target - hoverAnim) * 0.15f;
                repaint();
            });
            anim.start();

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true; }
                public void mouseExited(MouseEvent e)  { hovered = false; }
                public void mouseClicked(MouseEvent e) {
                    selectedMode = mode;
                    updateSymbolVisibility();
                    if (cardAI    != null) cardAI.repaint();
                    if (cardHuman != null) cardHuman.repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            boolean selected = (selectedMode == mode);
            Color accent = (mode == 0) ? ACCENT_O : ACCENT_X;

            // Nền card
            Color bg = selected
                ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40)
                : interpolate(CARD_BG, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25), hoverAnim);
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, W, H, 18, 18));

            // Viền
            Color border = selected ? accent : interpolate(CARD_BORDER,
                new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 120), hoverAnim);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(selected ? 2f : 1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, W - 1, H - 1, 18, 18));

            // Icon
            g2.setFont(new Font("SansSerif", Font.PLAIN, 32));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(icon, W / 2 - fm.stringWidth(icon) / 2, 44);

            // Title
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            g2.setColor(selected ? accent.brighter() : TEXT_MAIN);
            fm = g2.getFontMetrics();
            g2.drawString(title, W / 2 - fm.stringWidth(title) / 2, 68);

            // Desc
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(TEXT_DIM);
            fm = g2.getFontMetrics();
            g2.drawString(desc, W / 2 - fm.stringWidth(desc) / 2, 88);

            // Dấu tick nếu được chọn
            if (selected) {
                g2.setColor(accent);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                g2.drawString("✓ ĐÃ CHỌN", W / 2 - 32, H - 10);
            }

            g2.dispose();
        }

        private Color interpolate(Color a, Color b, float t) {
            return new Color(
                clamp((int)(a.getRed()   + (b.getRed()   - a.getRed())   * t)),
                clamp((int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t)),
                clamp((int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t)),
                clamp((int)(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t))
            );
        }
        private int clamp(int v) { return Math.max(0, Math.min(255, v)); }
    }

    // Inner: Toggle X/O 
    class SymbolToggle extends JComponent {
        private float slideAnim = 0f; // 0 = X, 1 = O
        private final Timer anim;

        SymbolToggle() {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            anim = new Timer(16, e -> {
                float target = selectedSymbol;
                slideAnim += (target - slideAnim) * 0.2f;
                repaint();
            });
            anim.start();

            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    selectedSymbol = (selectedSymbol == 0) ? 1 : 0;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            int halfW = W / 2;

            // Track
            g2.setColor(new Color(255, 255, 255, 15));
            g2.fill(new RoundRectangle2D.Float(0, 0, W, H, H, H));
            g2.setColor(CARD_BORDER);
            g2.setStroke(new BasicStroke(1));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, W - 1, H - 1, H, H));

            // Thumb slide
            float thumbX = slideAnim * halfW;
            Color thumbColor = (selectedSymbol == 0) ? ACCENT_X : ACCENT_O;
            g2.setColor(new Color(thumbColor.getRed(), thumbColor.getGreen(),
                                  thumbColor.getBlue(), 200));
            g2.fill(new RoundRectangle2D.Float(thumbX + 2, 2, halfW - 4, H - 4, H - 4, H - 4));

            // Labels
            g2.setFont(new Font("Monospaced", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            // X
            Color xColor = (selectedSymbol == 0) ? Color.WHITE : TEXT_DIM;
            g2.setColor(xColor);
            g2.drawString("X", halfW / 2 - fm.stringWidth("X") / 2, H / 2 + fm.getAscent() / 2 - 3);
            // O
            Color oColor = (selectedSymbol == 1) ? Color.WHITE : TEXT_DIM;
            g2.setColor(oColor);
            g2.drawString("O", halfW + halfW / 2 - fm.stringWidth("O") / 2, H / 2 + fm.getAscent() / 2 - 3);

            g2.dispose();
        }
    }

    //  Inner: Glow Button 
    static class GlowButton extends JComponent {
        private final String label;
        private final Color accentColor, textColor;
        private boolean hovered = false, pressed = false;
        private float hoverAnim = 0f;
        private final Timer anim;
        private final java.util.List<ActionListener> listeners = new java.util.ArrayList<>();

        GlowButton(String label, Color accentColor, Color textColor) {
            this.label = label;
            this.accentColor = accentColor;
            this.textColor = textColor;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            anim = new Timer(16, e -> {
                float target = hovered ? 1f : 0f;
                hoverAnim += (target - hoverAnim) * 0.15f;
                repaint();
            });
            anim.start();

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e)  { hovered = true; }
                public void mouseExited(MouseEvent e)   { hovered = false; pressed = false; }
                public void mousePressed(MouseEvent e)  { pressed = true; repaint(); }
                public void mouseReleased(MouseEvent e) {
                    if (hovered) {
                        listeners.forEach(l -> l.actionPerformed(
                            new ActionEvent(this, ActionEvent.ACTION_PERFORMED, label)));
                    }
                    pressed = false;
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

            // Glow halo
            if (hoverAnim > 0.05f) {
                int glowR = 18;
                for (int i = glowR; i > 0; i -= 4) {
                    float a = hoverAnim * 0.04f * (glowR - i + 1);
                    g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                                         accentColor.getBlue(), (int)(a * 255)));
                    g2.fill(new RoundRectangle2D.Float(-i, -i, W + i * 2, H + i * 2, arc + i, arc + i));
                }
            }

            // Nền nút
            float fillAlpha = pressed ? 0.95f : 0.75f + 0.2f * hoverAnim;
            g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(),
                                  accentColor.getBlue(), (int)(fillAlpha * 255)));
            g2.fill(new RoundRectangle2D.Float(0, 0, W, H, arc, arc));

            // Highlight trên
            g2.setPaint(new GradientPaint(0, 0, new Color(255,255,255,50), 0, H/2f, new Color(255,255,255,0)));
            g2.fill(new RoundRectangle2D.Float(1, 1, W - 2, H / 2f, arc - 2, arc - 2));

            // Chữ
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.setColor(textColor);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, W / 2 - fm.stringWidth(label) / 2, H / 2 + fm.getAscent() / 2 - 3);

            g2.dispose();
        }
    }
}