package de.awacademy.gamecontest.client.manual;

import de.awacademy.gamecontest.common.GameConstants;
import de.awacademy.gamecontest.common.GameModelAdapter;
import de.awacademy.gamecontest.common.GameModelListener;
import de.awacademy.gamecontest.common.GameStatus;
import de.awacademy.gamecontest.common.Player;
import de.awacademy.gamecontest.common.PlayerColor;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import static de.awacademy.gamecontest.common.PlayerColor.RED;
import static de.awacademy.gamecontest.common.PlayerColor.YELLOW;

public class GameView extends JFrame implements GameModelListener {

    private GameModelAdapter modelAdapter;

    private GameClientMode clientMode;

    private int rowCount;

    private int colCount;

    private PrefixTextField player1TextField;
    private PrefixTextField player2TextField;
    private PrefixTextField gameStatusTextField;

    private StonePanel[][] stonePanels;

    private JTextArea consoleTextArea;

    private Player player1;
    private Player player2;

    private PlayerColor nextPlayerColor;


    public GameView(GameModelAdapter modelAdapter, String userName) throws HeadlessException {
        super("Game Client - " + userName);

        this.modelAdapter = modelAdapter;
        setClientMode(userName);
        this.rowCount = GameConstants.ROW_COUNT;
        this.colCount = GameConstants.COL_COUNT;
        this.stonePanels = new StonePanel[rowCount][colCount];

        GridBagLayout mainLayout = new GridBagLayout();
        getContentPane().setLayout(mainLayout);

        createStatusPanel();

        createPlayGrid();

        createConsolePanel();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });

        setSize(650, 700);
        setVisible(true);
    }

    private void setClientMode(String userName) {
        JPanel panel = new JPanel(new GridBagLayout());
        Object[] clientModes = Arrays.stream(GameClientMode.values()).map(clientMode -> clientMode.getUiText()).toArray();
        JComboBox comboBox = new JComboBox(clientModes);
        comboBox.setSelectedIndex(0);
        JOptionPane.showMessageDialog(null, comboBox, "Client-Modus", JOptionPane.QUESTION_MESSAGE);
        panel.add(comboBox);

        this.clientMode = GameClientMode.values()[comboBox.getSelectedIndex()];
    }

    public void startGame(String userName) {
        switch (clientMode) {
            case START_NEW_GAME:
                modelAdapter.newGame();
                modelAdapter.viewGame(userName);
                break;
            case JOIN_EXISTING_GAME:
                String otherPlayerName = JOptionPane.showInputDialog(null, "Bitte geben Sie den Benutzernamen Ihres Spiel-Partners ein:");
                modelAdapter.joinGame(otherPlayerName);
                modelAdapter.viewGame(otherPlayerName);
                break;
            case VIEW_ONLY:
                String onePlayerName = JOptionPane.showInputDialog(null, "Bitte geben Sie den Benutzernamen eines Spielers ein:");
                modelAdapter.viewGame(onePlayerName);
                break;
        }
    }

    private void createStatusPanel() {
        JPanel statusPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(3, 1);
        statusPanel.setLayout(gridLayout);
        getContentPane().add(statusPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        this.player1TextField = createPrefixTextField(statusPanel, "Spieler 1 (gelb): ", clientMode == GameClientMode.START_NEW_GAME);
        this.player2TextField = createPrefixTextField(statusPanel, "Spieler 2 (rot): ", clientMode == GameClientMode.JOIN_EXISTING_GAME);
        this.gameStatusTextField = createPrefixTextField(statusPanel, "Status: ", false);
    }

    private PrefixTextField createPrefixTextField(JPanel statusPanel, String prefixText, boolean useBoldFont) {
        PrefixTextField prefixTextField = new PrefixTextField(prefixText);
        if (useBoldFont) {
            Font boldFont = new Font("Courier", Font.BOLD, 12);
            prefixTextField.setFont(boldFont);
        }
        statusPanel.add(prefixTextField);
        return prefixTextField;
    }

    private void createPlayGrid() {
        JPanel gridPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(rowCount, colCount);
        gridPanel.setLayout(gridLayout);
        getContentPane().add(gridPanel, new GridBagConstraints(0, 1, 1, 1, 1, 3, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        for (int row = rowCount - 1; row >= 0; row--) {
            for (int col = 0; col < colCount; col++) {
                StonePanel stonePanel = new StonePanel(this, row, col);
                stonePanels[row][col] = stonePanel;
                stonePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                gridPanel.add(stonePanel);
            }
        }
    }

    private void createConsolePanel() {
        this.consoleTextArea = new JTextArea();
        consoleTextArea.setText("Welcome to GameClient 0.1 beta...\n");

        JScrollPane scrollPane = new JScrollPane(consoleTextArea);

        getContentPane().add(scrollPane, new GridBagConstraints(0, 2, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }

    public void log(String text) {
        consoleTextArea.setText(consoleTextArea.getText() + text + "\n");
    }

    protected void fieldClicked(int row, int col) {
        try {
            modelAdapter.move(col);
            this.nextPlayerColor = (nextPlayerColor == YELLOW ? RED : YELLOW);
        } catch (IllegalStateException ise) {
            log(ise.getMessage());
            throw ise;
        }
    }

    @Override
    public void playerRegistered(Player player) {
        log("playerRegistered " + player);
        if (player.getColor() == YELLOW) {
            player1 = player;
            player1TextField.setMainText(player.getName());
        } else {
            player2 = player;
            player2TextField.setMainText(player.getName());
            this.nextPlayerColor = YELLOW;
        }
    }

    @Override
    public void playerMoved(PlayerColor color, int row, int col, GameStatus status) {
        log("playerMoved " + color.name() + " " + row + "," + col + " " + status.getUiText());
        gameStatusTextField.setMainText(status.getUiText());
        stonePanels[row][col].setStoneColor(color);
        stonePanels[row][col].repaint();
        switch (status) {
            case WAIT_FOR_YELLOW_MOVE:
            case WAIT_FOR_RED_MOVE:
                this.nextPlayerColor = status.getConcerningPlayerColor();
                break;
            default:
                this.nextPlayerColor = null;
        }
    }

    @Override
    public void gameFinished(PlayerColor winner) {
        log("gameFinished winner: " + winner);

        String statusText = GameStatus.FINISHED.getUiText() + ": ";

        if (winner == null) {
            statusText += "unentschieden";
        } else {
            Player player = (winner == YELLOW ? player1 : player2);
            statusText += player.getName() + " hat gewonnen!";
        }
        gameStatusTextField.setMainText(statusText);
    }

    public boolean isActivePlayerNext() {
        return (clientMode == GameClientMode.START_NEW_GAME && nextPlayerColor == PlayerColor.YELLOW)
                || (clientMode == GameClientMode.JOIN_EXISTING_GAME && nextPlayerColor == RED);
    }
}
