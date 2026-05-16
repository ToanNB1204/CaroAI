package scr;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class MenuPanel extends JPanel {
    private TurnBadge turnBadge;
    private GameFrame frame;

    //  Bảng màu
    private static final Color BG_TOP       = new Color(13,  17,  30);
    private static final Color BG_BOT       = new Color(22,  28,  55);
    private static final Color ACCENT_X     = new Color(229,  57,  53);   // Đỏ
    private static final Color ACCENT_O     = new Color( 30, 136, 229);   // Xanh
    private static final Color BTN_IDLE     = new Color(255, 255, 255,  18);
    private static final Color BTN_HOVER    = new Color(255, 255, 255,  40);
    private static final Color BTN_PRESS    = new Color(255, 255, 255,  60);
    private static final Color TEXT_PRIMARY = new Color(230, 235, 255);
    private static final Color TEXT_DIM     = new Color(120, 130, 170);
    private static final Color DIVIDER      = new Color(255, 255, 255,  20);

    public MenuPanel(GameFrame frame) {
        this.frame = frame;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(230, 0));
        setOpaque(false);

        //  Tiêu đề 
        add(Box.createRigidArea(new Dimension(0, 36)));
        add(makeTitleBlock());

        //  Đường kẻ mờ 
        add(Box.createRigidArea(new Dimension(0, 18)));
        add(makeDivider());

        //  Badge lượt đi 
        add(Box.createRigidArea(new Dimension(0, 20)));
        turnBadge = new TurnBadge("X");
        turnBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(turnBadge);

        //  Nhãn phụ 
        add(Box.createRigidArea(new Dimension(0, 8)));
        JLabel hint = makeLabel("Đang đến lượt", TEXT_DIM, 11, Font.PLAIN);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(hint);

        //  Đường kẻ mờ 
        add(Box.createRigidArea(new Dimension(0, 24)));
        add(makeDivider());
        add(Box.createRigidArea(new Dimension(0, 24)));

        //  Nhóm nút 
        String[][] btns = {
            {"⟲",  "Undo",    "#hoàn tác nước đi"},
            {"↺",  "Restart", "#ván mới"},
            {"✕",  "Exit",    "#thoát game"},
        };

        for (String[] b : btns) {
            FancyButton btn = new FancyButton(b[0], b[1]);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            String action = b[1];
            btn.addActionListener(e -> {
                switch (action) {
                    case "Undo":    frame.undoMove();    break;
                    case "Restart": frame.restartGame(); break;
                    case "Exit":    System.exit(0);      break;
                }
            });
            add(btn);
            add(Box.createRigidArea(new Dimension(0, 10)));
        }

        //  Phiên bản nhỏ ở cuối 
        add(Box.createVerticalGlue());
        JLabel ver = makeLabel("Cờ Caro v1.0", TEXT_DIM, 10, Font.PLAIN);
        ver.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(ver);
        add(Box.createRigidArea(new Dimension(0, 16)));
    }

    //  Cập nhật lượt từ GameFrame 
    public void updateCurrentPlayer(String symbol) {
        turnBadge.setSymbol(symbol);
    }

    //  Nền gradient 
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Gradient nền chính
        GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOT);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Vệt sáng trang trí góc trên
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.07f));
        g2.setColor(Color.WHITE);
        g2.fillOval(-60, -60, 200, 200);

        // Đường viền phải mờ
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g2.setColor(DIVIDER);
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());

        g2.dispose();
    }

    //  Helper: tiêu đề 
    private JPanel makeTitleBlock() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        // Icon / logo chữ
        JLabel logo = new JLabel("✦ CARO");
        logo.setFont(new Font("Monospaced", Font.BOLD, 22));
        logo.setForeground(TEXT_PRIMARY);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(logo);

        JLabel sub = makeLabel("STRATEGY BOARD GAME", TEXT_DIM, 9, Font.PLAIN);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(Box.createRigidArea(new Dimension(0, 4)));
        p.add(sub);

        return p;
    }

    private JComponent makeDivider() {
        JPanel d = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(DIVIDER);
                g2.drawLine(20, 0, getWidth() - 20, 0);
            }
        };
        d.setOpaque(false);
        d.setPreferredSize(new Dimension(230, 1));
        d.setMaximumSize(new Dimension(230, 1));
        return d;
    }

    private JLabel makeLabel(String text, Color color, int size, int style) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        l.setFont(new Font("SansSerif", style, size));
        return l;
    }

    //  Badge hiển thị lượt đi (custom painted) 
    static class TurnBadge extends JComponent {
        private String symbol;
        private float pulse = 0f;
        private Timer pulseTimer;

        TurnBadge(String symbol) {
            this.symbol = symbol;
            setPreferredSize(new Dimension(88, 88));
            setMaximumSize(new Dimension(88, 88));

            pulseTimer = new Timer(30, e -> {
                pulse = (pulse + 0.05f) % (2 * (float) Math.PI);
                repaint();
            });
            pulseTimer.start();
        }

        void setSymbol(String symbol) {
            this.symbol = symbol;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color accent = symbol.equals("X") ? ACCENT_X : ACCENT_O;
            int cx = getWidth() / 2, cy = getHeight() / 2;
            int r = 34;

            // Vòng sáng pulse
            float alpha = 0.10f + 0.08f * (float) Math.sin(pulse);
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(),
                    (int)(alpha * 255)));
            g2.fillOval(cx - r - 8, cy - r - 8, (r + 8) * 2, (r + 8) * 2);

            // Vòng ngoài
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(cx - r - 2, cy - r - 2, (r + 2) * 2, (r + 2) * 2);

            // Nền tròn
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);

            // Chữ ký hiệu
            g2.setFont(new Font("Monospaced", Font.BOLD, 32));
            g2.setColor(accent.brighter());
            FontMetrics fm = g2.getFontMetrics();
            int tx = cx - fm.stringWidth(symbol) / 2;
            int ty = cy + fm.getAscent() / 2 - 3;
            g2.drawString(symbol, tx, ty);

            g2.dispose();
        }
    }

    //  Nút tùy biến bo góc, icon + label 
    static class FancyButton extends JComponent {
        private final String icon, label;
        private boolean hovered = false, pressed = false;
        private float hoverAnim = 0f;
        private Timer animTimer;
        private java.util.List<ActionListener> listeners = new java.util.ArrayList<>();

        FancyButton(String icon, String label) {
            this.icon  = icon;
            this.label = label;
            setPreferredSize(new Dimension(190, 46));
            setMaximumSize(new Dimension(190, 46));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            animTimer = new Timer(16, e -> {
                float target = hovered ? 1f : 0f;
                hoverAnim += (target - hoverAnim) * 0.18f;
                repaint();
            });
            animTimer.start();

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true; }
                public void mouseExited(MouseEvent e)  { hovered = false; pressed = false; }
                public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                public void mouseReleased(MouseEvent e) {
                    if (pressed && hovered) {
                        pressed = false;
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

            int w = getWidth(), h = getHeight();
            int arc = 14;

            // Nền nút
            Color fill = pressed ? BTN_PRESS
                        : interpolate(BTN_IDLE, BTN_HOVER, hoverAnim);
            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

            // Viền nút
            float borderAlpha = 0.15f + 0.25f * hoverAnim;
            g2.setColor(new Color(255, 255, 255, (int)(borderAlpha * 255)));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));

            // Vệt sáng highlight khi hover
            if (hoverAnim > 0.01f) {
                GradientPaint shine = new GradientPaint(
                    0, 0, new Color(255, 255, 255, (int)(30 * hoverAnim)),
                    0, h / 2f, new Color(255, 255, 255, 0));
                g2.setPaint(shine);
                g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, h / 2f, arc - 2, arc - 2));
            }

            // Icon
            g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
            g2.setColor(TEXT_PRIMARY);
            g2.drawString(icon, 18, h / 2 + 7);

            // Label
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.setColor(TEXT_PRIMARY);
            g2.drawString(label, 46, h / 2 + 5);

            g2.dispose();
        }

        private Color interpolate(Color a, Color b, float t) {
            return new Color(
                (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t),
                (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t),
                (int)(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t)
            );
        }
    }
}