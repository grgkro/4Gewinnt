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
    private boolean fourInThisLineStillPossible;


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
        firstMoveCol = -1;
        firstMoveRow = -1;
        secondMoveCol = -1;
        secondMoveRow = -1;
        latestMoveRow = -1;

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
        if (moveCount == 0) {
            System.out.println("******************************* New Move 1 *******************************************");
        }
        for (int col : possibleMoves.keySet()) {
            System.out.println("Going to check move (row = " + possibleMoves.get(col) + ", col = " + col + ") as " + (moveCount + 1) + ". move.");
            addMove(fields, moveCount, col, possibleMoves);

            if (moveCount == 0) {
//                checkFirstMove(fields, possibleMoves, col, moveCount);

                firstMoveRow = latestMoveRow;
                firstMoveCol = col;
                if (gameIsWon(fields, possibleMoves.get(col), col, moveCount)) {
                    makeWinningMove(fields, col, possibleMoves);
                    stopRecursion = true;
                    break;
                } else {
                    try {
                        firstNodeValues.put(col, checkNextMoves(fields, moveCount + 1).getValue());
                    } catch (NullPointerException e) {
                        System.out.println(e);
                        return;
                    }
                }
            } else if (moveCount == 1) {
                secondMoveRow = latestMoveRow;
                secondMoveCol = col;
                if (gameIsLost(fields, possibleMoves.get(col), col, moveCount)) {
                    hinderEnemyFromCompletingFour(fields, col, possibleMoves);
                    break;
                } else {
                    secondNodeValues.put(col, checkNextMoves(fields, moveCount + 1).getValue());
                }

            } else if (moveCount == numCalculateMovesAhead) {
                int value = checkCombos(fields, possibleMoves.get(col), col, moveCount, myValue);
                thirdNodeValues.put(col, value);
                System.out.println("value: " + value);
                removeMove(fields, latestMoveRow, col, possibleMoves);
            }
        }
    }

    private void makeWinningMove(int[][] fields, int col, Map<Integer, Integer> possibleMoves) {
        System.out.println("------------Game can be won immediately in " + col + " --------------");
        move(col);
        removeMove(fields, firstMoveRow, col, possibleMoves);  // removes the move from fields, because after sending the move to the server, the server will reply with the move and then it will be added to the fields again (doesn't really matter actually, the game is over anyway, but just to be sure).
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

    private boolean gameIsLost(int[][] fields, int row, int col, int moveCount) {
        if (checkCombos(fields, row, col, moveCount, enemyValue) >= 500_000) {
            return true;
        } else {
            return false;
        }
    }

    private boolean gameIsWon(int[][] fields, int row, int col, int moveCount) {
        if (checkCombos(fields, row, col, moveCount, myValue) >= 500_000) {
            return true;
        } else {
            return false;
        }
    }

    // Function select an element base on index
    // and return an element
    public int getRandomCol(List<Integer> list) {
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

    // player can be 1 or 2 -> it will check the combos for player 1 or player 2
    private int checkCombos(int[][] fields, int row, int col, int moveCount, int player) {
        int value = 0;
        value = checkCombosHorizontally(fields, row, firstMoveRow, value, moveCount, player);
        value = checkCombosVertically(fields, row, firstMoveRow, col, value, moveCount, player);
        value = checkCombosDiagonallyLeftDownToRightUp(fields, row, firstMoveRow, value, moveCount, player);
        value = checkCombosDiagonallyLeftUpToRightDown(fields, value, moveCount, player);
        return value;
    }

    private int checkCombosHorizontally(int[][] fields, int row, int firstMoveRow, int value, int moveCount, int player) {
        for (int y = 0; y <= (Math.max(row, firstMoveRow)); y++) {
            for (int x = 0; x <= (GameConstants.COL_COUNT - 4); x++) {  // we only need to go to col 3, not col 7 (if col_count = 7)
                int comboLength = checkOneComboHorizontally(fields, y, x, player);
                value = value + evaluateCombo(comboLength, moveCount);
            }
        }
        return value;
    }

    private int checkOneComboHorizontally(int[][] fields, int y, int x, int player) {
        int comboLength = 0;
        // go through the four columns, begin at the startCol and end at the target col + abs(offset - 3) -> z.B. offset = 2 bedeutet, wir fangen 2 links vom gesetzten Stein an. -> wir enden eins abs(2 - 3) rechts vom gesetzten Stein.
        fourInThisLineStillPossible = true;
        for (int i = x; i < (x + 4); i++) {
            comboLength = getComboLengthHorizontally(fields, y, i, comboLength, player);
            if (!fourInThisLineStillPossible) {
                break;
            }
        }
        return comboLength;
    }

    private int getComboLengthHorizontally(int[][] fields, int y, int i, int comboLength, int player) {
        switch (player) {
            case 1:
                if (fields[y][i] == myValue) {
                    return ++comboLength;
                } else if (fields[y][i] == enemyValue) {
                    fourInThisLineStillPossible = false;
                }
                break;
            case 2:
                if (fields[y][i] == enemyValue) {
                    return ++comboLength;
                } else if (fields[y][i] == myValue) {
                    fourInThisLineStillPossible = false;
                }
                break;
        }
        return comboLength;
    }


    private int checkCombosVertically(int[][] fields, int row, int firstMoveRow, int col, int value, int moveCount, int player) {
        // we only need to check the two columns of our two moves and only underneath those moves, not above them.
        for (int x : new int[]{col, firstMoveCol}) {
            for (int y = 0; y <= (Math.max(row, firstMoveRow)); y++) {
                int comboLength = checkOneComboVertically(fields, y, x, player);
                value = value + evaluateCombo(comboLength, moveCount);
            }
        }
        return value;
    }

    private int checkOneComboVertically(int[][] fields, int y, int x, int player) {
        int comboLength = 0;
        // go through the four columns, begin at the startCol and end at the target col + abs(offset - 3) -> zb offset = 2 (wir fangen 2 links vom gesetzten Stein an. -> wir enden eins abs(2 - 3) rechts vom gesetzten Stein.
        fourInThisLineStillPossible = true;
        for (int i = y; i <= (y + 3); i++) {
            if (i < 6) {
                comboLength = getComboLengthVertically(fields, x, i, comboLength, player);
                if (!fourInThisLineStillPossible) {
                    break;
                }
            } else {
                break;
            }
        }
        return comboLength;
    }

    private int getComboLengthVertically(int[][] fields, int x, int i, int comboLength, int player) {
        switch (player) {
            case 1:
                if (fields[i][x] == myValue) {
                    return ++comboLength;
                } else if (fields[i][x] == enemyValue) {
                    fourInThisLineStillPossible = false;
                }
                break;
            case 2:
                if (fields[i][x] == enemyValue) {
                    return ++comboLength;
                } else if (fields[i][x] == myValue) {
                    fourInThisLineStillPossible = false;
                }
                break;
        }
        return comboLength;
    }

    private int checkCombosDiagonallyLeftUpToRightDown(int[][] fields, int value, int moveCount, int player) {
        for (int y = (GameConstants.ROW_COUNT - 1); y >= 3; y--) {   // if y < 3, this means that you cant have 4 in a row anymore, because you start at row 3 and move downwards...
            for (int x = 0; x <= (GameConstants.COL_COUNT - 4); x++) {
                int comboLength = checkOneComboDiagonallyUpToDown(fields, y, x, player);
                value = value + evaluateCombo(comboLength, moveCount);
            }
        }
        return value;
    }

    private int checkOneComboDiagonallyUpToDown(int[][] fields, int y, int x, int player) {
        fourInThisLineStillPossible = true;
        int comboLength = 0;
        int j = y;
        // go through the four columns, begin at the startCol and end at the target col + abs(offset - 3) -> zb offset = 2 (wir fangen 2 links vom gesetzten Stein an. -> wir enden eins abs(2 - 3) rechts vom gesetzten Stein.
        for (int i = x; i <= (x + 3); i++) {
            if (j >= 0) {
                comboLength = getComboLengthDiagonallyUpToDown(fields, j, i, comboLength, player);
                if (!fourInThisLineStillPossible) {
                    break;
                }
                j--;
            } else {
                break;
            }
        }
        return comboLength;
    }

    private int getComboLengthDiagonallyUpToDown(int[][] fields, int j, int i, int comboLength, int player) {
        switch (player) {
            case 1:
                if (fields[j][i] == myValue) {
                    return ++comboLength;
                } else if (fields[j][i] == enemyValue) {
                    fourInThisLineStillPossible = true;
                }
                break;
            case 2:
                if (fields[j][i] == enemyValue) {
                    return ++comboLength;
                } else if (fields[j][i] == myValue) {
                    fourInThisLineStillPossible = true;
                }
                break;
        }
    return comboLength;
}

    private int checkCombosDiagonallyLeftDownToRightUp(int[][] fields, int row, int firstMoveRow, int value, int moveCount, int player) {
        int lastRow = getLastRowWeNeedToCheck(row, firstMoveRow);
        for (int y = 0; y <= lastRow; y++) {
            for (int x = 0; x <= (GameConstants.COL_COUNT - 4); x++) {
                int comboLength = checkOneComboDiagonallyDownToUp(fields, y, x, player);
                value = value + evaluateCombo(comboLength, moveCount);
            }
        }
        return value;
    }

    private int checkOneComboDiagonallyDownToUp(int[][] fields, int y, int x, int player) {
        fourInThisLineStillPossible = true;
        int comboLength = 0;
        int j = y;
        // go through the four columns, begin at the startCol and end at the target col + abs(offset - 3) -> zb offset = 2 (wir fangen 2 links vom gesetzten Stein an. -> wir enden eins abs(2 - 3) rechts vom gesetzten Stein.
        for (int i = x; i <= (x + 3); i++) {
            if (j < 6) {
                comboLength = getComboLengthDiagonallyDownToUp(fields, j, i, comboLength, player);
                if (!fourInThisLineStillPossible) {
                    break;
                }
                j++;
            } else {
                break;
            }

        }
        return comboLength;
    }

    private int getComboLengthDiagonallyDownToUp(int[][] fields, int j, int i, int comboLength, int player) {
        switch (player) {
            case 1:
                if (fields[j][i] == myValue) {
                    return ++comboLength;
                } else if (fields[j][i] == enemyValue) {
                    fourInThisLineStillPossible = true;
                }
                break;
            case 2:
                if (fields[j][i] == enemyValue) {
                    return ++comboLength;
                } else if (fields[j][i] == myValue) {
                    fourInThisLineStillPossible = true;
                }
                break;
        }
        return comboLength;
    }

    private int getLastRowWeNeedToCheck(int row, int firstMoveRow) {
        // we have to be at least four rows away from the upper fields border to get four in a row diagonally upwards
        if (row < (GameConstants.ROW_COUNT - 4) && firstMoveRow < (GameConstants.ROW_COUNT - 4)) {
            return Math.max(row, firstMoveRow);
        } else {
            return (GameConstants.ROW_COUNT - 4);
        }
    }

    private int evaluateCombo(int comboLength, int moveCount) {
        int comboValue = 0;
        if (comboLength == 4) {
            comboValue = 500_000;
        } else if (comboLength == 3 && fourInThisLineStillPossible) {
            switch (moveCount) {  // getting 3 in a row with at least one empty field left next to it is more valuable, when it happens in the first move (of me or the enemy)
                case 0:
                    comboValue = 100_000;
                    break;
                case 1:
                    comboValue = 100_000;
                    break;
                case 2:
                    comboValue = 20_000;
                    break;
            }
        } else if (comboLength == 2 && fourInThisLineStillPossible && moveCount < 2) {   // 2 in a row is only useful if it happens in the first move -> moveCount == 0 (or the first move of the enemy -> moveCount == 1)
            comboValue = 5_000;
        }
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
        for (int col : nonFullColumns) {
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
