package de.awacademy.gamecontest.client;

import de.awacademy.gamecontest.common.ClientCommand;
import de.awacademy.gamecontest.common.GameConstants;
import de.awacademy.gamecontest.common.GameModelAdapter;
import de.awacademy.gamecontest.common.GameModelListener;
import de.awacademy.gamecontest.common.GameStatus;
import de.awacademy.gamecontest.common.Player;
import de.awacademy.gamecontest.common.PlayerColor;
import de.awacademy.gamecontest.common.ServerCommand;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class GameClient extends WebSocketClient implements GameModelAdapter {

    private GameModelListener gameModelListener;

    private String userName;


    public GameClient(String userName, String userPassword) throws URISyntaxException {
        super(new URI(String.format("ws://h2903214.stratoserver.net:53112/login/%s/%s/", userName, userPassword)));
        this.userName = userName;
    }

    protected abstract GameModelListener createGame();

    protected abstract void startGame();

    protected abstract void logCustom(String str);

    public void startClient() throws URISyntaxException, InterruptedException {
        boolean connected = connectBlocking();
        System.out.println("Connected: " + connected);

        if (!connected) {
            close();
            return;
        }

        this.gameModelListener = createGame();
        startGame();
    }

    protected String getUserName() {
        return userName;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("httpstatusmsg: " + serverHandshake.getHttpStatusMessage());
    }

    @Override
    public void onMessage(String message) {
        log("-> " + message);

        try {
            String[] commandAndArgs = message.split(" ");
            if (commandAndArgs.length == 0) {
                forceClose("Message '" + message + "' does not contain a command!");
            }

            ClientCommand clientCommand = ClientCommand.fromString(commandAndArgs[0]);
            if (clientCommand == null) {
                forceClose("Unknown command in message: '" + message + "'");
            }

            int actualParameterCount = commandAndArgs.length - 1;
            if (clientCommand.getExpectedParameterCount() != actualParameterCount && clientCommand.getExpectedParameterCount() != -1) {
                forceClose("Parameter " + clientCommand.name() + " expects " + clientCommand.getExpectedParameterCount()
                        + " parameters. But messasge '" + message + "' contains " + actualParameterCount + " parameters");
            }

            switch (clientCommand) {
                case INFO: {
                    // already logged
                    break;
                }
                case PLAYER_REGISTERED: {
                    PlayerColor playerColor = parsePlayerColor(message, commandAndArgs[1], 1);
                    String playerName = commandAndArgs[2];
                    Player player = new Player(playerName, playerColor);
                    gameModelListener.playerRegistered(player);
                    break;
                }
                case PLAYER_MOVED: {
                    PlayerColor playerColor = parsePlayerColor(message, commandAndArgs[1], 1);
                    int rowNo = parseRowNumber(message, commandAndArgs[2]);
                    int colNo = parseColNumber(message, commandAndArgs[3]);
                    GameStatus status = parseStatus(message, commandAndArgs[4]);
                    gameModelListener.playerMoved(playerColor, rowNo, colNo, status);
                    break;
                }
                case GAME_FINISHED: {
                    PlayerColor playerColor = parsePlayerColor(message, commandAndArgs[1], 0);
                    gameModelListener.gameFinished(playerColor);
                    break;
                }
            }
        } catch (IllegalStateException ise) {
            // nothing to do, forceClose() covered error
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("GameClient wurde geschlossen: " + s);
    }

    @Override
    public void onError(Exception e) {
        System.out.println("ERROR: " + e);
    }

    private void forceClose(String message) throws IllegalStateException {
        System.out.println("forcing close due to: " + message);
        send(message);
        close();
        throw new IllegalStateException(message);
    }

    private PlayerColor parsePlayerColor(String message, String playerNoStr, int minValue) throws IllegalArgumentException {
        int playerNo = parseInt(message, playerNoStr, "First", minValue, 2);
        if (playerNo == 0) {
            // unentschieden
            return null;
        }
        return PlayerColor.values()[playerNo - 1];
    }

    private int parseRowNumber(String message, String rowNoStr) throws IllegalArgumentException {
        return parseInt(message, rowNoStr, "Second", 0, GameConstants.ROW_COUNT - 1);
    }

    private int parseColNumber(String message, String colNoStr) throws IllegalArgumentException {
        return parseInt(message, colNoStr, "Third", 0, GameConstants.COL_COUNT - 1);
    }

    private GameStatus parseStatus(String message, String statusStr) throws IllegalArgumentException {
        int statusOrdinal = parseInt(message, statusStr, "Fourth", 0, GameStatus.values().length - 1);
        return GameStatus.values()[statusOrdinal];
    }

    private int parseInt(String message, String strValue, String paramPosition, int min, int max) throws IllegalArgumentException {
        int intValue = 0;
        try {
            Integer integerValue = Integer.valueOf(strValue);
            intValue = integerValue.intValue();
            if (intValue < min || intValue > max) {
                forceClose(paramPosition + " parameter in message '" + message + "' has to be a player number between " + min + " and " + max + " !");
            }
        } catch (NumberFormatException nfe) {
            forceClose(paramPosition + " parameter in message '" + message + "' has to be an integer!");
        }
        return intValue;
    }

    protected final void log(String str) {
        System.out.println(str);
        if (gameModelListener != null) {
            logCustom(str);
        }
    }

    @Override
    public void newGame() {
        sendMessage(ServerCommand.NEW_GAME.name());
    }

    @Override
    public void joinGame(String player1Name) {
        sendMessage(ServerCommand.JOIN_GAME.name() + " " + player1Name);
    }

    @Override
    public void viewGame(String playerName) {
        sendMessage(ServerCommand.VIEW_GAME.name() + " " + playerName);
    }

    @Override
    public void move(int columnNo) {
        sendMessage(ServerCommand.MOVE.name() + " " + columnNo);
    }

    private void sendMessage(String message) {
        log("<- " + message);
        send(message);
    }
}
