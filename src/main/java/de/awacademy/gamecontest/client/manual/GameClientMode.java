package de.awacademy.gamecontest.client.manual;

public enum GameClientMode {

    START_NEW_GAME("Spieler 1 (gelb)"),

    JOIN_EXISTING_GAME("Spieler 2 (rot)"),

    VIEW_ONLY("Zuschauer");


    private String uiText;


    GameClientMode(String uiText) {
        this.uiText = uiText;
    }

    public String getUiText() {
        return uiText;
    }
}
