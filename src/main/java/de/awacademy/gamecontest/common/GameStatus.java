package de.awacademy.gamecontest.common;

public enum GameStatus {

    WAIT_FOR_YELLOW_REGISTRATION(PlayerColor.YELLOW, "Warte auf Registrierung von Spieler 1 (gelb)"),

    WAIT_FOR_RED_REGISTRATION(PlayerColor.RED, "Warte auf Registrierung von Spieler 2 (rot)"),

    WAIT_FOR_YELLOW_MOVE(PlayerColor.YELLOW, "Gelb ist am Zug"),

    WAIT_FOR_RED_MOVE(PlayerColor.RED, "Rot ist am Zug"),

    FINISHED(null, "ENDE");


    private PlayerColor concerningPlayerColor;

    private String uiText;


    private GameStatus(PlayerColor concerningPlayerColor, String uiText) {
        this.concerningPlayerColor = concerningPlayerColor;
        this.uiText = uiText;
    }

    public PlayerColor getConcerningPlayerColor() {
        return concerningPlayerColor;
    }

    public String getUiText() {
        return uiText;
    }

    public boolean isWaitingForRegistration() {
        return (this == WAIT_FOR_YELLOW_REGISTRATION) || (this == WAIT_FOR_RED_REGISTRATION);
    }

    public boolean isWaitingForMove(PlayerColor playerColor) {
        if ((this == WAIT_FOR_YELLOW_MOVE || this == WAIT_FOR_RED_MOVE)
                && (playerColor == concerningPlayerColor)) {
            return true;
        }
        return false;
    }
}
