package de.awacademy.gamecontest.client.manual;

import de.awacademy.gamecontest.client.GameClient;
import de.awacademy.gamecontest.common.GameModelListener;

import java.net.URISyntaxException;

public class ManualGameClient extends GameClient {

    private GameView view;


    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        String userName = args[0];
        String userPassword = args[1];
        new ManualGameClient(userName, userPassword).startClient();
    }

    public ManualGameClient(String userName, String userPassword) throws URISyntaxException {
        super(userName, userPassword);
    }

    @Override
    protected GameModelListener createGame() {
        this.view = new GameView(this, getUserName());
        return view;
    }

    @Override
    protected void startGame() {
        view.startGame(getUserName());
    }

    @Override
    protected void logCustom(String str) {
        view.log(str);
    }
}
