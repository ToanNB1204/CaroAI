package scr;

import javax.swing.*;
import java.awt.*;
import java.util.Stack;

/**
 * GameFrame – cửa sổ chính.
 * CardLayout:  "menu" → StartMenuPanel   |   "game" → BoardPanel + MenuPanel
 * Overlay:     WinOverlayPanel thêm vào LayeredPane khi ván kết thúc.
 */
public class GameFrame extends JFrame {

    private static final String CARD_MENU = "menu";
    private static final String CARD_GAME = "game";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     rootPanel  = new JPanel(cardLayout);

    private StartMenuPanel startMenuPanel;
    private JPanel         gameContainer;
    private MenuPanel      menuPanel;
    private GameEngine     engine;
    private BoardPanel     boardPanel;
    private AI             ai;

    private final Stack<Point> moveHistory = new Stack<>();
    private static final int   BOARD_SIZE  = 50;

    private GameEngine.Cell playerSymbol = GameEngine.Cell.X;
    private GameEngine.Cell aiSymbol     = GameEngine.Cell.O;
    private boolean         vsAI         = true;

    private WinOverlayPanel currentOverlay;

    public GameFrame() {
        setTitle("Cờ Caro");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setSize(1200, 900);
        setLocationRelativeTo(null);

        startMenuPanel = new StartMenuPanel(this::onStartGame);
        gameContainer  = new JPanel(new BorderLayout());

        rootPanel.add(startMenuPanel, CARD_MENU);
        rootPanel.add(gameContainer,  CARD_GAME);
        add(rootPanel);

        cardLayout.show(rootPanel, CARD_MENU);
        setVisible(true);
    }

    //  Callback từ StartMenuPanel 
    private void onStartGame() {
        vsAI = startMenuPanel.isVsAI();
        if (vsAI) {
            playerSymbol = startMenuPanel.playerChoseX() ? GameEngine.Cell.X : GameEngine.Cell.O;
            aiSymbol     = (playerSymbol == GameEngine.Cell.X) ? GameEngine.Cell.O : GameEngine.Cell.X;
        }
        startGame();
        cardLayout.show(rootPanel, CARD_GAME);
        if (boardPanel != null) boardPanel.requestFocusInWindow();
    }

    //  Khởi ván mới 
    public void startGame() {
        removeOverlay();

        Player human, opponent;
        if (vsAI) {
            human    = new Player("Player " + playerSymbol, playerSymbol.toString(), false);
            opponent = new Player("AI "     + aiSymbol,     aiSymbol.toString(),     true);
            ai       = new AI(BOARD_SIZE, 5);
        } else {
            playerSymbol = GameEngine.Cell.X;
            aiSymbol     = GameEngine.Cell.O;
            human        = new Player("Player 1 (X)", "X", false);
            opponent     = new Player("Player 2 (O)", "O", false);
            ai           = null;
        }

        engine = new GameEngine(human, opponent);
        engine.setPlayerSymbol(playerSymbol);
        engine.setAISymbol(aiSymbol);

        menuPanel  = new MenuPanel(this);
        boardPanel = new BoardPanel(engine, ai) {
            @Override
            public void repaint() {
                super.repaint();
                if (menuPanel != null)
                    menuPanel.updateCurrentPlayer(engine.getCurrentPlayer().getSymbol());
            }
        };

        moveHistory.clear();
        gameContainer.removeAll();
        gameContainer.add(menuPanel,  BorderLayout.WEST);
        gameContainer.add(boardPanel, BorderLayout.CENTER);
        gameContainer.revalidate();
        gameContainer.repaint();
        boardPanel.requestFocusInWindow();

        // AI đi trước nếu người chọn O
        if (vsAI && playerSymbol == GameEngine.Cell.O) {
            Point aiMove = ai.getBestMove(engine.getBoard(), aiSymbol);
            engine.makeMove(aiMove, aiSymbol);
            ai.setCell(aiMove.y, aiMove.x, -1);
            recordMove(aiMove);
            boardPanel.repaint();
            engine.switchTurn();
        }
    }

    //  Hiện Win/Lose Overlay 
    public void showWinOverlay(Player winner) {
        boolean humanWon = !winner.isAI();

        currentOverlay = new WinOverlayPanel(
            winner.getName(),
            winner.getSymbol(),
            humanWon,
            vsAI,
            this::startGame,    // Play Again
            this::goToMainMenu  // Main Menu
        );

        JLayeredPane layered = getLayeredPane();
        currentOverlay.setBounds(0, 0, layered.getWidth(), layered.getHeight());
        layered.add(currentOverlay, JLayeredPane.POPUP_LAYER);
        layered.revalidate();
        layered.repaint();

        currentOverlay.attachKeyBindings(getRootPane());
    }

    private void removeOverlay() {
        if (currentOverlay != null) {
            getLayeredPane().remove(currentOverlay);
            getLayeredPane().repaint();
            currentOverlay = null;
        }
    }

    //  Về main menu 
    public void goToMainMenu() {
        removeOverlay();
        rootPanel.remove(startMenuPanel);
        startMenuPanel = new StartMenuPanel(this::onStartGame);
        rootPanel.add(startMenuPanel, CARD_MENU);
        cardLayout.show(rootPanel, CARD_MENU);
    }

    //  Undo 
    public void undoMove() {
        if (moveHistory.isEmpty()) return;
        int steps = vsAI ? 2 : 1;
        for (int i = 0; i < steps && !moveHistory.isEmpty(); i++) {
            Point last = moveHistory.pop();
            engine.getBoard().remove(last);
            if (ai != null) ai.setCell(last.y, last.x, 0);
            engine.switchTurn();
        }
        boardPanel.repaint();
    }

    public void restartGame()   { startGame(); }
    public void recordMove(Point p) { moveHistory.push(p); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameFrame::new);
    }
}