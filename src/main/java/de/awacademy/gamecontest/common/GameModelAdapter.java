package de.awacademy.gamecontest.common;

/**
 * Methoden, die der Client beim Server aufruft, um das Spiel zu steuern.
 */
public interface GameModelAdapter {

    /**
     * Startet auf dem Server ein neues Spiel.
     * Der Spieler, der dieses Kommando aufruft, wird automatisch Spieler 1 (gelb) und beginnt das Spiel.
     */
    public void newGame();

    /**
     * Der Spieler nimmt an einem Spiel teil, welches ein anderer Spieler vorher erzeugt hat.
     * Der Spieler, der dieses Kommando aufruft, wird automatisch Spieler 2 (rot) und zieht als zweiter.
     *
     * @param player1Name Name des Spielers, der das Spiel gestartet hat (Spieler 1 gelb).
     */
    public void joinGame(String player1Name);

    /**
     * Der Spieler, der dieses Kommando aufruft, erhält alle Nachrichten zum Fortgang des Spiels {@link GameModelListener}.
     * Derzeit muß dieses Kommando von beiden Spielern aufgerufen werden, damit sie wissen, was der andere Spieler
     * gezogen hat und wann sie an der Reihe sind.
     * Außerdem kann ein fremder Spieler dieses Kommando aufrufen, um sich ein Spiel als Zuschauer anzusehen.
     *
     * @param playerName Name eines Spielers, der am Spiel beteiligt ist.
     *                   Legt fest, welches Spiel angesehen werden soll.
     */
    public void viewGame(String playerName);

    /**
     * Der Spieler macht einen Zug.
     *
     * @param columnNo Spalte, in den der Spieler seinen Stein legt (0 für die erste Spalte).
     */
    public void move(int columnNo);
}
