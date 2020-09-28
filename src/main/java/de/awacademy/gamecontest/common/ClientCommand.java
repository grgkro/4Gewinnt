package de.awacademy.gamecontest.common;

/**
 * Kommandos, die der WebSocket-Client versteht.
 */
public enum ClientCommand {

    /**
     * Informationsmeldung insbes. für die Fehlersuche. Der Text sollte vom Client auf der Konsole ausgegeben werden.
     * Keine Parameter. Der Text darf Leerzeichen enthalten.
     */
    INFO(-1),

    /**
     * Ein Spieler hat sich beim Server registriert.
     * Parameter 1: Spieler-Nummer 1 für gelb, 2 für rot
     * Parameter 2: Spieler-Name
     */
    PLAYER_REGISTERED(2),

    /**
     * Ein Spieler hat einen Zug gemacht.
     * Parameter 1: Spieler-Nummer
     *                  1 für gelb
     *                  2 für rot
     * Parameter 2: Zeilen-Index (0 für die unterste Zeile, 5 für die oberste Zeile)
     * Parameter 3: Spalten-Index (0 für die ganz linke Spalte, 6 für die ganz rechte Spalte)
     * Parameter 4: Status, in dem sich das Spiel nach diesem Zug befindet:
     *                  0: Warten auf die Registrierung des gelben Spielers
     *                  1: Warten auf die Registrierung des roten Spielers
     *                  2: Gelb ist am Zug
     *                  3: Rot ist am Zug
     *                  4: Das Spiel ist beendet (durch Sieg oder Unentschieden)
     */
    PLAYER_MOVED(4),

    /**
     * Das Spiel ist durch Sieg eines Spielers oder durch Unentschieden (Spielbrett voll) beendet.
     * Parameter 1: 0 für Uentschieden
     *              1 für Sieg durch Spieler 1 (gelb)
     *              2 für Sieg durch Spieler 2 (rot)
     */
    GAME_FINISHED(1);


    private int expectedParameterCount;


    private ClientCommand(int expectedParameterCount) {
        this.expectedParameterCount = expectedParameterCount;
    }

    public int getExpectedParameterCount() {
        return expectedParameterCount;
    }

    public static ClientCommand fromString(String commandTypeStr) {
        try {
            return ClientCommand.valueOf(commandTypeStr.toUpperCase());
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }
}
