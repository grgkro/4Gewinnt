package de.awacademy.gamecontest.client.automatic;

import de.awacademy.gamecontest.client.GameClient;
import de.awacademy.gamecontest.common.GameConstants;
import de.awacademy.gamecontest.common.GameModelListener;
import de.awacademy.gamecontest.common.GameStatus;
import de.awacademy.gamecontest.common.Player;
import de.awacademy.gamecontest.common.PlayerColor;
import org.apache.log4j.BasicConfigurator;

import java.net.URISyntaxException;
import java.util.*;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutomaticGameClient extends GameClient implements GameModelListener {

    private String otherPlayerName;

    public int[][] fields;

    private Random random;

    private Player player1;
    private Player player2;

    private boolean iAmYellow;
    private int myValue;
    private int enemyValue;
    private int numCalculateMovesAhead = 2;
    private int firstMoveRow;
    private int firstMoveCol;
    private int secondMoveRow;
    private int secondMoveCol;
    private int latestMoveRow;



//    private int[][] savedGame = [{-1 -1 -1 -1 -1}]


    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        String userName = args[0];
        String userPassword = args[1];
        if (args.length == 3) {
            String otherPlayerName = args[2];
            new AutomaticGameClient(userName, userPassword, otherPlayerName).startClient();
        } else {
            new AutomaticGameClient(userName, userPassword).startClient();
        }
        BasicConfigurator.configure();
    }

    public AutomaticGameClient(String userName, String userPassword) throws URISyntaxException {
        this(userName, userPassword, null);
    }

    public AutomaticGameClient(String userName, String userPassword, String otherPlayerName) throws URISyntaxException {
        super(userName, userPassword);
        this.otherPlayerName = otherPlayerName;
        this.iAmYellow = (otherPlayerName == null);
        this.random = new Random();
    }

    @Override
    protected GameModelListener createGame() {
        this.fields = new int[GameConstants.ROW_COUNT][GameConstants.COL_COUNT];
        return this;
    }

    @Override
    protected void startGame() {
        if (iAmYellow) {
            myValue = 1;
            enemyValue = 2;
            newGame();
        } else {
            myValue = 2;
            enemyValue = 2;
            joinGame(otherPlayerName);
        }
        viewGame(getUserName());
    }

    @Override
    public void playerRegistered(Player player) {
        if (player1 == null) {
            player1 = player;
        } else {
            player2 = player;
            if (iAmYellow) {
                move(3);
            }
        }
    }

    @Override
    public void playerMoved(PlayerColor color, int row, int col, GameStatus status) {
        fields[row][col] = (color == PlayerColor.YELLOW ? 1 : 2);
        if ((iAmYellow && status == GameStatus.WAIT_FOR_YELLOW_MOVE)
                || (!iAmYellow && status == GameStatus.WAIT_FOR_RED_MOVE)) {
            startRecursion();
        }
    }

    public void startRecursion() {
        int moveCount = 0;
        Map<Integer, Integer> firstNodeValues = new HashMap<>();   // first Integer (Key) = col of that move, second Integer (value) = the calculated value of that move. exp. (2, 50)
        Map<Integer, Integer> secondNodeValues = new HashMap<>();   // first Integer (Key) = col of that move, second Integer (value) = the calculated value of that move. exp. (2, 50)
        Map<Integer, Integer> finalNodeValues = new HashMap<>();   // first Integer (Key) = col of that move, second Integer (value) = the calculated value of that move. exp. (2, 50)
        checkNextMoves(fields, moveCount, firstNodeValues, secondNodeValues, finalNodeValues);
    }

    private int checkNextMoves(int[][] fields, int moveCount, Map<Integer, Integer> firstNodeValues, Map<Integer, Integer> secondNodeValues, Map<Integer, Integer> finalNodeValues) {
        Map<Integer, Integer> possibleMoves = findPossibleMoves(fields);

        int[][] copiedFields = fields;

        for (int col : possibleMoves.keySet()) {
            System.out.println("Going to check move (row = " + possibleMoves.get(col) + ", col = " + col + ") as " + (moveCount + 1) + ". move.");
            copiedFields = addMove(copiedFields, moveCount, col, possibleMoves);
            if (moveCount < numCalculateMovesAhead) {
                if (moveCount == 0) {
                    firstMoveRow = latestMoveRow;
                    firstMoveCol = col;
                    firstNodeValues.put(col, checkNextMoves(copiedFields, moveCount + 1, firstNodeValues, secondNodeValues, finalNodeValues));
                } else if (moveCount == 1) {
                    secondMoveRow = latestMoveRow;
                    secondMoveCol = col;
                    secondNodeValues.put(col, checkNextMoves(copiedFields, moveCount + 1, firstNodeValues, secondNodeValues, finalNodeValues));
                }
            } else if (moveCount == numCalculateMovesAhead) {
                int value = evaluate(copiedFields, possibleMoves.get(col), col);
                finalNodeValues.put(col, value);
                System.out.println("value: " + value);
                copiedFields = removeMove(copiedFields, latestMoveRow, col, possibleMoves);
            }
        }
        // Auswertung:
        int bestMoveValue = 0;
        switch (moveCount) {
            case 0:
                bestMoveValue = findValueOfBestMove(firstNodeValues, moveCount);
                break;
            case 1:
                bestMoveValue = findValueOfBestMove(secondNodeValues, moveCount);
                copiedFields = removeMove(copiedFields, firstMoveRow, firstMoveCol, possibleMoves);
                break;
            case 2:
                bestMoveValue = findValueOfBestMove(finalNodeValues, moveCount);
                copiedFields = removeMove(copiedFields, secondMoveRow, secondMoveCol, possibleMoves);
                break;
        }
        System.out.println(fields.toString());
        return bestMoveValue;
    }

    private int findValueOfBestMove(Map<Integer, Integer> values, int moveCount) {
        Map.Entry<Integer, Integer> bestMove = checkValues(values, moveCount);
        System.out.println("Best Move for Zug: " + moveCount + 1 + " is: " + bestMove.getKey() + " with value: " + bestMove.getValue());
        if (moveCount == 0) {
            move(bestMove.getKey()); //this doesnt work... it will continue and then move again.
        }
        moveCount = moveCount - 1;
        return bestMove.getValue();
    }


    private Map.Entry<Integer, Integer> checkValues(Map<Integer, Integer> values, int moveCount) {
        if (moveCount % 2 == 0) {
            return findMax(values);
        } else {
            return findMin(values);
        }
    }

    private Map.Entry<Integer, Integer> findMin(Map<Integer, Integer> values) {
        Map.Entry<Integer, Integer> minEntry = null;

        for (Map.Entry<Integer, Integer> entry : values.entrySet()) {
            if (minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0) {
                minEntry = entry;
            }
        }
        System.out.println("min value out of values: " + values.values().toString() + " is: " + minEntry + ". From the last Block of moves, the Enemy should make the move which belongs to this value: " + values.toString());
        return minEntry;
    }

    private Map.Entry<Integer, Integer> findMax(Map<Integer, Integer> values) {
        Map.Entry<Integer, Integer> maxEntry = null;

        for (Map.Entry<Integer, Integer> entry : values.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        return maxEntry;
    }


    private int evaluate(int[][] fields, int row, int col) {
        int value = 0;
        value = checkCombos(fields, row, col, value);
        return value;
    }

    private int checkCombos(int[][] fields, int row, int col, int value) {
        value = checkCombo(fields, row, col, value, 3);
        value = value + checkCombo(fields, row, col, value, 2);
        value = value + checkCombo(fields, row, col, value, 1);
        value = value + checkCombo(fields, row, col, value, 0);
        return value;
    }

    private int checkCombo(int[][] fields, int row, int col, int value, int offset) {
        int startCol = col - offset;
        if (startCol < 0) return value;
        value = evaluateCombo(getComboLength(startCol, row, col, offset));
        return value;
    }

    private int getComboLength(int startCol, int row, int col, int offset) {
        int comboLength = 0;
        // go through the four columns, begin at the startCol and end at the target col + abs(offset - 3) -> zb offset = 2 (wir fangen 2 links vom gesetzten Stein an. -> wir enden eins abs(2 - 3) rechts vom gesetzten Stein.
        if (col + abs(offset - 3) < GameConstants.COL_COUNT) {
            for (int i = startCol; i <= col + abs(offset - 3); i++) {

                if (fields[row][i] == myValue) comboLength++;
                if (fields[row][i] == enemyValue) break;
            }
        }
        return comboLength;
    }

    private int evaluateCombo(int comboLength) {
        int comboValue = 0;
        if (comboLength == 4) comboValue = 500_000;
        if (comboLength == 3) comboValue = 500;
        if (comboLength == 2) comboValue = 50;
        return comboValue;
    }

    // add the move in this col to copiedFields
    private int[][] addMove(int[][] copiedFields, int moveCount, int col, Map<Integer, Integer> possibleMoves) {
        if (moveCount % 2 == 0) {
            latestMoveRow = possibleMoves.get(col);
            copiedFields[latestMoveRow][col] = myValue;
        } else {
            latestMoveRow = possibleMoves.get(col);
            copiedFields[latestMoveRow][col] = enemyValue;
        }
        return copiedFields;
    }

    private int[][] removeMove(int[][] copiedFields, int row, int col, Map<Integer, Integer> possibleMoves) {

        copiedFields[row][col] = 0;

        return copiedFields;
    }

    private Map<Integer, Integer> findPossibleMoves(int[][] fields) {
        List<Integer> nonFullColumns = new ArrayList<>();
        Map<Integer, Integer> possibleMoves = new HashMap<>();
        for (int i = 0; i < GameConstants.COL_COUNT - 1; i++) {
            if (fields[GameConstants.ROW_COUNT - 1][i] != 1 && fields[GameConstants.ROW_COUNT - 1][i] != 2) {
                nonFullColumns.add(i);
            }
        }
        for (int col: nonFullColumns) {
            possibleMoves.put(col, getRowForThisMove(fields, col));
        }
        return possibleMoves;
    }

    private int getRowForThisMove(int[][] fields, int col) {
        for (int i = 0; i < GameConstants.ROW_COUNT; i++) {
            if (fields[i][col] == 0) return i;
        }
        return Integer.parseInt(null); //we shouldn't reach that point.
    }


    public boolean isInBound(int columnNo) {
        return fields[GameConstants.ROW_COUNT - 1][columnNo] != 0;
    }


//    @Before
//    public void before() throws URISyntaxException, InterruptedException {
//        main(new String[]{"Georg1", "geheim1"});
//        System.out.println("Hello");
//    }
//
//    @Test
//    public void whenMakeMove_columnNoIsZero() throws URISyntaxException, InterruptedException {
////        agcY.playerMoved(PlayerColor.YELLOW, 0,0, WAIT_FOR_YELLOW_MOVE);
//
//        int rowMoved = makeMove();
//        assertEquals(rowMoved, 0);
//
//    }

    @Override
    public void gameFinished(PlayerColor winner) {
    }

    @Override
    protected void logCustom(String str) {
    }
}
