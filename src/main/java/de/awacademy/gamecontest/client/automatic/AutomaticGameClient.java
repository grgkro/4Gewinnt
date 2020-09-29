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
    private boolean stopRecursion;
    List<Integer> losingMoves = new ArrayList();

    Map<Integer, Integer> firstNodeValues = new HashMap<>();   // first Integer (Key) = col of that move, second Integer (value) = the calculated value of that move. exp. (2, 50)
    Map<Integer, Integer> secondNodeValues = new HashMap<>();   // first Integer (Key) = col of that move, second Integer (value) = the calculated value of that move. exp. (2, 50)
    Map<Integer, Integer> thirdNodeValues = new HashMap<>();   // first Integer (Key) = col of that move, second Integer (value) = the calculated value of that move. exp. (2, 50)



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
            enemyValue = 1;
            joinGame(otherPlayerName);
        }
        viewGame(getUserName());
    }

    @Override
    public void playerRegistered(Player player) {
        if (player1 == null) {
            player1 = player;
            move(2);
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
        firstNodeValues.clear();
        secondNodeValues.clear();
        thirdNodeValues.clear();
        losingMoves.clear();
        stopRecursion = false;
//        sleep();
        checkNextMoves(fields, moveCount);
    }

    private Map.Entry<Integer, Integer> checkNextMoves(int[][] fields, int moveCount) {
        if (stopRecursion) {
            return null;
        }

        Map<Integer, Integer> possibleMoves = findPossibleMoves(fields);

        Map.Entry<Integer, Integer> bestMove = null;

        tryEachPossibleMove(fields, possibleMoves, moveCount);

        if (stopRecursion) {   // we could have moved in tryEachPossibleMove(), so we need to check again, if recursion should be stopped
            return null;
        }

        // Auswertung:
        bestMove = findAndMakeBestMove(fields, possibleMoves, bestMove, moveCount);

        return bestMove;
    }

    private void tryEachPossibleMove(int[][] fields, Map<Integer, Integer> possibleMoves, int moveCount) {
        for (int col : possibleMoves.keySet()) {
            System.out.println("Going to check move (row = " + possibleMoves.get(col) + ", col = " + col + ") as " + (moveCount + 1) + ". move.");
            addMove(fields, moveCount, col, possibleMoves);

            if (moveCount == 0) {
                firstMoveRow = latestMoveRow;
                firstMoveCol = col;
                if(gameIsWon(fields, possibleMoves.get(col), col)) {
                    System.out.println("------------Game can be won immediately in " + col + " --------------");
                    move(col);
                    removeMove(fields, firstMoveRow, col, possibleMoves);
                    stopRecursion = true;
                    break;
                }
                try {
                    firstNodeValues.put(col, checkNextMoves(fields, moveCount + 1).getValue());
                } catch (NullPointerException e) {
                    System.out.println(e);
                    return;
                }

            } else if (moveCount == 1) {
                secondMoveRow = latestMoveRow;
                secondMoveCol = col;
                if(gameIsLost(fields, possibleMoves.get(col), col)) {
                   hinderEnemyFromCompletingFour(fields, col, possibleMoves);
                   break;
                }
                secondNodeValues.put(col, checkNextMoves(fields, moveCount + 1).getValue());
            } else if (moveCount == numCalculateMovesAhead) {
                int value = checkMyCombos(fields, possibleMoves.get(col), col);
                thirdNodeValues.put(col, value);
                System.out.println("value: " + value);
                removeMove(fields, latestMoveRow, col, possibleMoves);
            }
        }
    }

    private void hinderEnemyFromCompletingFour(int[][] fields, int col, Map<Integer, Integer> possibleMoves) {
        removeMove(fields, secondMoveRow, col, possibleMoves);
        removeMove(fields, firstMoveRow, col, possibleMoves);
        System.out.println("------------....Game would have been lost in " + col + " --------------");
        if (col == firstMoveCol) {  // wenn col == firstMoveCol, bedeutet dass, dass der Gewinnerzug vom Gegner überhaupt erst möglich wurde durch setzen meines davorigen Steines in der Reihe. Wir wollen dann gerade den Zug nicht machen.
            System.out.println("------------Removed move in " + col + " from possibleMoves --------------");
            possibleMoves.remove(col);
            losingMoves.add(col);
        } else {
            System.out.println("------------Enemy can win -> The move in " + col + " is needed immediately, no need to check all the other possible moves --------------");
            move(col);
            stopRecursion = true;
        }
    }

    private Map.Entry<Integer, Integer> findAndMakeBestMove(int[][] fields, Map<Integer, Integer> possibleMoves, Map.Entry<Integer, Integer> bestMove, int moveCount) {
        switch (moveCount) {
            case 0:
                bestMove = findValueOfBestMove(firstNodeValues, moveCount, bestMove);
                move(bestMove.getKey());
                break;
            case 1:
                bestMove = findValueOfBestMove(secondNodeValues, moveCount, bestMove);
                removeMove(fields, firstMoveRow, firstMoveCol, possibleMoves);
                break;
            case 2:
                bestMove = findValueOfBestMove(thirdNodeValues, moveCount, bestMove);
                removeMove(fields, secondMoveRow, secondMoveCol, possibleMoves);
                break;
        }
        return bestMove;
    }

    private boolean gameIsLost(int[][] fields, int row, int col) {
        if (checkEnemiesCombos(fields, row, col, 0) >= 500_000 ) {
            return true;
        } else {
            return false;
        }
    }

    private boolean gameIsWon(int[][] fields, int row, int col) {
        if (checkMyCombos(fields, row, col) >= 500_000) {
            return true;
        } else {
            return false;
        }
    }

    // Function select an element base on index
    // and return an element
    public int getRandomCol(List<Integer> list)
    {
        return list.get(random.nextInt(list.size()));
    }

//    if (possibleMoves.isEmpty()) {
//        System.out.println("------ Goodbye -----");
//        List<Integer> nonFullRows = findNonNullRows(fields);
//        move(getRandomCol(nonFullRows));
//        break;
//    }

    private Map.Entry<Integer, Integer> findValueOfBestMove(Map<Integer, Integer> values, int moveCount, Map.Entry<Integer, Integer> bestMove) {
        bestMove = checkValues(values, moveCount);
//        System.out.println("Best Move for Zug: " + (moveCount + 1) + " is: " + bestMove.getKey() + " with value: " + bestMove.getValue());
        return bestMove;
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
//        System.out.println("min value out of values: " + values.values().toString() + " is: " + minEntry + ". From the last Block of moves, the Enemy should make the move which belongs to this value: " + values.toString());
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

    private int checkEnemiesCombos(int[][] fields, int row, int col, int firstMoveRow) {
        int value = 0;
        value = checkEnemyHorizontally(fields, row, firstMoveRow, col, value);
        value = checkEnemyVertically(fields, row, firstMoveRow, col, value);
        value = checkEnemyDiagonallyLeftDownToRightUp(fields, row, firstMoveRow, col, value);
        value = checkEnemyDiagonallyLeftUpToRightDown(fields, value);
        return value;
    }

    private int checkEnemyDiagonallyLeftUpToRightDown(int[][] fields, int value) {
        for (int y = (GameConstants.ROW_COUNT - 1); y >= 0 ; y--) {
            for (int x = 0; x <= (GameConstants.COL_COUNT / 2); x++) {
                int comboLength = 0;
                int j = y;
                // go through the four columns, begin at the startCol and end at the target col + abs(offset - 3) -> zb offset = 2 (wir fangen 2 links vom gesetzten Stein an. -> wir enden eins abs(2 - 3) rechts vom gesetzten Stein.
                for (int i = x; i <= (x + 3); i++) {
                    if (j >= 0) {
                        if (fields[j][i] == myValue) break;
                        if (fields[j][i] == enemyValue) comboLength++;
                        j--;
                    } else {
                        break;
                    }
                }
                value = value + evaluateCombo((comboLength));
            }
        }
        return value;
    }

    private int checkEnemyDiagonallyLeftDownToRightUp(int[][] fields, int row, int firstMoveRow, int col, int value) {
        for (int y = 0; y <= (Math.max(row, firstMoveRow)) ; y++) {
            for (int x = 0; x <= (GameConstants.COL_COUNT / 2); x++) {
                int comboLength = 0;
                int j = y;
                // go through the four columns, begin at the startCol and end at the target col + abs(offset - 3) -> zb offset = 2 (wir fangen 2 links vom gesetzten Stein an. -> wir enden eins abs(2 - 3) rechts vom gesetzten Stein.
                for (int i = x; i <= (x + 3); i++) {
                    if (j < 6) {
                        if (fields[j][i] == myValue) break;
                        if (fields[j][i] == enemyValue) comboLength++;
                        j++;
                    } else {
                        break;
                    }

                }
                value = value + evaluateCombo((comboLength));
            }
        }
        return value;
    }

    private int checkEnemyVertically(int[][] fields, int row, int firstMoveRow, int col, int value) {
        for (int x = 0; x < GameConstants.COL_COUNT ; x++) {
            for (int y = 0; y <= (Math.max(row, firstMoveRow)); y++) {
                int comboLength = 0;
                // go through the four columns, begin at the startCol and end at the target col + abs(offset - 3) -> zb offset = 2 (wir fangen 2 links vom gesetzten Stein an. -> wir enden eins abs(2 - 3) rechts vom gesetzten Stein.

                for (int i = y; i <= (y + 3); i++) {
                    if (i < 6) {
                        if (fields[i][x] == myValue) break;
                        if (fields[i][x] == enemyValue) comboLength++;
                    } else {
                        break;
                    }

                }
                value = value + evaluateCombo((comboLength));
            }
        }
        return value;
    }

    private int checkEnemyHorizontally(int[][] fields, int row, int firstMoveRow, int col, int value) {
        for (int y = 0; y <= (Math.max(row, firstMoveRow)) ; y++) {
            for (int x = 0; x <= (GameConstants.COL_COUNT / 2); x++) {
                int comboLength = 0;
                // go through the four columns, begin at the startCol and end at the target col + abs(offset - 3) -> zb offset = 2 (wir fangen 2 links vom gesetzten Stein an. -> wir enden eins abs(2 - 3) rechts vom gesetzten Stein.

                for (int i = x; i <= (x + 3); i++) {
                    if (fields[y][i] == myValue) break;
                    if (fields[y][i] == enemyValue) comboLength++;
                }
                value = value + evaluateCombo((comboLength));
            }
        }
        return value;
    }

    private int checkMyCombos(int[][] fields, int row, int col) {
        int value = 0;
        value = checkCombosHorizontally(fields, row, firstMoveRow, col, value);
        value = checkCombosVertically(fields, row, firstMoveRow, col, value);
        value = checkCombosDiagonallyLeftDownToRightUp(fields, row, firstMoveRow, col, value);
        value = checkCombosDiagonallyLeftUpToRightDown(fields, value);
        return value;
    }

    private int checkCombosDiagonallyLeftUpToRightDown(int[][] fields, int value) {
        for (int y = (GameConstants.ROW_COUNT - 1); y >= 0 ; y--) {
            for (int x = 0; x <= (GameConstants.COL_COUNT / 2); x++) {
                int comboLength = 0;
                int j = y;
                // go through the four columns, begin at the startCol and end at the target col + abs(offset - 3) -> zb offset = 2 (wir fangen 2 links vom gesetzten Stein an. -> wir enden eins abs(2 - 3) rechts vom gesetzten Stein.
                for (int i = x; i <= (x + 3); i++) {
                    if (j >= 0) {
                        if (fields[j][i] == myValue) comboLength++;
                        if (fields[j][i] == enemyValue) break;
                        j--;
                    } else {
                        break;
                    }
                }
                value = value + evaluateCombo((comboLength));
            }
        }
        return value;
    }

    private int checkCombosDiagonallyLeftDownToRightUp(int[][] fields, int row, int firstMoveRow, int col, int value) {
        for (int y = 0; y <= (Math.max(row, firstMoveRow)) ; y++) {
            for (int x = 0; x <= (GameConstants.COL_COUNT / 2); x++) {
                int comboLength = 0;
                int j = y;
                // go through the four columns, begin at the startCol and end at the target col + abs(offset - 3) -> zb offset = 2 (wir fangen 2 links vom gesetzten Stein an. -> wir enden eins abs(2 - 3) rechts vom gesetzten Stein.
                for (int i = x; i <= (x + 3); i++) {
                    if (j < 6) {
                        if (fields[j][i] == myValue) comboLength++;
                        if (fields[j][i] == enemyValue) break;
                        j++;
                    } else {
                        break;
                    }

                }
                value = value + evaluateCombo((comboLength));
            }
        }
        return value;
    }

    private int checkCombosVertically(int[][] fields, int row, int firstMoveRow, int col, int value) {
        for (int x = 0; x < GameConstants.COL_COUNT ; x++) {
            for (int y = 0; y <= (Math.max(row, firstMoveRow)); y++) {
                int comboLength = 0;
                // go through the four columns, begin at the startCol and end at the target col + abs(offset - 3) -> zb offset = 2 (wir fangen 2 links vom gesetzten Stein an. -> wir enden eins abs(2 - 3) rechts vom gesetzten Stein.

                for (int i = y; i <= (y + 3); i++) {
                    if (i < 6) {
                        if (fields[i][x] == myValue) comboLength++;
                        if (fields[i][x] == enemyValue) break;
                    } else {
                        break;
                    }

                }
                value = value + evaluateCombo((comboLength));
            }
        }
        return value;
    }

    private int checkCombosHorizontally(int[][] fields, int row, int firstMoveRow, int col, int value) {

        for (int y = 0; y <= (Math.max(row, firstMoveRow)) ; y++) {
            for (int x = 0; x <= (GameConstants.COL_COUNT / 2); x++) {
                int comboLength = 0;
                // go through the four columns, begin at the startCol and end at the target col + abs(offset - 3) -> zb offset = 2 (wir fangen 2 links vom gesetzten Stein an. -> wir enden eins abs(2 - 3) rechts vom gesetzten Stein.

                    for (int i = x; i <= (x + 3); i++) {
                        if (fields[y][i] == myValue) comboLength++;
                        if (fields[y][i] == enemyValue) break;
                    }
                    value = value + evaluateCombo((comboLength));
            }
        }
       return value;
    }

//    private int checkCombos(int[][] fields, int row, int col, int value) {
//        value = checkCombo(fields, row, col, value, 3);
//        value = value + checkCombo(fields, row, col, value, 2);
//        value = value + checkCombo(fields, row, col, value, 1);
//        value = value + checkCombo(fields, row, col, value, 0);
//        return value;
//    }
//
//    private int checkCombo(int[][] fields, int row, int col, int value, int offset) {
//        int startCol = col - offset;
//        if (startCol < 0) return value;
//        value = evaluateCombo(getComboLength(fields, startCol, row, col, offset));
//        return value;
//    }
//
//    private int getComboLength(int[][] fields, int startCol, int row, int col, int offset) {
//        int comboLength = 0;
//        // go through the four columns, begin at the startCol and end at the target col + abs(offset - 3) -> zb offset = 2 (wir fangen 2 links vom gesetzten Stein an. -> wir enden eins abs(2 - 3) rechts vom gesetzten Stein.
//        if (col + abs(offset - 3) < GameConstants.COL_COUNT) {
//            for (int i = startCol; i <= col + abs(offset - 3); i++) {
//
//                if (fields[row][i] == myValue) comboLength++;
//                if (fields[row][i] == enemyValue) break;
//            }
//        }
//        return comboLength;
//    }

    private int evaluateCombo(int comboLength) {
        int comboValue = 0;
        if (comboLength == 4) comboValue = 500_000;
        if (comboLength == 3) comboValue = 500;
        if (comboLength == 2) comboValue = 50;
        return comboValue;
    }

    // add the move in this col to copiedFields
    private void addMove(int[][] fields, int moveCount, int col, Map<Integer, Integer> possibleMoves) {
        if (moveCount % 2 == 0) {
            latestMoveRow = possibleMoves.get(col);
            fields[latestMoveRow][col] = myValue;
        } else {
            latestMoveRow = possibleMoves.get(col);
            fields[latestMoveRow][col] = enemyValue;
        }
    }

    private void removeMove(int[][] fields, int row, int col, Map<Integer, Integer> possibleMoves) {

        fields[row][col] = 0;
    }

    private Map<Integer, Integer> findPossibleMoves(int[][] fields) {
        List<Integer> nonFullColumns = findNonNullRows(fields);
        Map<Integer, Integer> possibleMoves = new HashMap<>();
        for (int col: nonFullColumns) {
            if (!losingMoves.contains(col)) {
                possibleMoves.put(col, getRowForThisMove(fields, col));
            }
        }
        return possibleMoves;
    }

    private List<Integer> findNonNullRows(int[][] fields) {
        List<Integer> nonFullColumns = new ArrayList<>();
        for (int i = 0; i < GameConstants.COL_COUNT - 1; i++) {
            if (fields[GameConstants.ROW_COUNT - 1][i] != 1 && fields[GameConstants.ROW_COUNT - 1][i] != 2) {
                nonFullColumns.add(i);
            }
        }
        return nonFullColumns;
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

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
