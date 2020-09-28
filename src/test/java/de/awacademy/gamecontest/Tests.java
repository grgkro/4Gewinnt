package de.awacademy.gamecontest;

import de.awacademy.gamecontest.client.automatic.AutomaticGameClient;
import de.awacademy.gamecontest.common.GameStatus;
import de.awacademy.gamecontest.common.PlayerColor;
import org.junit.Assert;
import org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Random;

import static de.awacademy.gamecontest.common.GameStatus.WAIT_FOR_YELLOW_MOVE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class Tests {

    @InjectMocks
    AutomaticGameClient agcY;        //we test as yellow

    private Random random;
    private int[][] fields;

    @Before
    public void setUp() throws Exception {
        agcY = spy(AutomaticGameClient.class);

        random = new Random();
    }

    @Test
    public void contextLoads() {
    }

    @Test
    public void whenMakeMove_columnNoIsZero() {
//        agcY.playerMoved(PlayerColor.YELLOW, 0,0, WAIT_FOR_YELLOW_MOVE);
//        agcY.fields = new int[][]{{0, 0, 0, 0, 0,0,0}, {0, 0, 0, 0, 0,0,0}, {0, 0, 0, 0, 0,0,0}, {0, 0, 0, 0, 0,0,0}, {0, 0, 0, 0, 0,0,0}, {0, 0, 0, 0, 0,0,0}};
//
//        agcY.playerMoved(PlayerColor.YELLOW, 0,0, WAIT_FOR_YELLOW_MOVE);
//        int rowMoved = agcY.makeMove();
//        assertEquals(rowMoved, 0);

    }

}
