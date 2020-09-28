package de.awacademy.gamecontest.common;

/**
 * Kommandos, die der WebSocket-Server versteht.
 */
public enum ServerCommand {

    /**
     * Informationsmeldung insbes. für die Fehlersuche. Der Text wird vom Server geloggt.
     * Keine Parameter. Der Text darf Leerzeichen enthalten.
     */
    INFO(true, true, -1),

    /**
     * Der angemeldete Spieler erzeugt ein neues Spiel und wird automatisch Spieler 1 (gelb).
     * Keine Parameter.
     */
    NEW_GAME(true, false, 0),

    /**
     * Der angemeldete Spieler nimmt an einem Spiel teil, welches ein anderer Spieler vorher erzeugt hat.
     * Der angemeldete Spieler wird automatisch Spieler 2 (rot).
     * Parameter 1: Name des gelben Spielers, der das Spiel erzeugt hat.
     */
    JOIN_GAME(true, false, 1),

    /**
     * Der angemeldete Spieler, der dieses Kommando aufruft, erhält alle Nachrichten zum Fortgang des Spiels {@link ClientCommand}.
     * Derzeit muß dieses Kommando von beiden Spielern aufgerufen werden, damit sie wissen, was der andere Spieler
     * gezogen hat und wann sie an der Reihe sind.
     * Außerdem kann ein fremder Spieler dieses Kommando aufrufen, um sich ein Spiel als Zuschauer anzusehen.
     * Parameter 1: Name eines Spielers, der am Spiel beteiligt ist. Legt fest, welches Spiel angesehen werden soll.
     */
    VIEW_GAME(true, true, 1),

    /**
     * Der angemeldete Spieler macht einen Zug.
     * Parameter 1: Spalte, in den der Spieler seinen Stein legt (0 für die ganz linke Spalte, 6 für die ganz rechte Spalte).
     */
    MOVE(false, true, 1);


    private boolean validDuringRegistration;

    private boolean validDuringGame;

    private int expectedParameterCount;


    private ServerCommand(boolean validDuringRegistration, boolean validDuringGame, int expectedParameterCount) {
        this.validDuringRegistration = validDuringRegistration;
        this.validDuringGame = validDuringGame;
        this.expectedParameterCount = expectedParameterCount;
    }

    public boolean isValidDuringRegistration() {
        return validDuringRegistration;
    }

    public boolean isValidDuringGame() {
        return validDuringGame;
    }

    public int getExpectedParameterCount() {
        return expectedParameterCount;
    }

    public static ServerCommand fromString(String commandTypeStr) {
        try {
            return ServerCommand.valueOf(commandTypeStr.toUpperCase());
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }
}
