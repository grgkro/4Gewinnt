package de.awacademy.gamecontest.common;

/**
 * Methoden, die der Server beim Client aufruft, um bestimmte Ereignisse zu signalisieren.
 */
public interface GameModelListener {

    /**
     * Ein Spieler hat sich beim Server registriert.
     *
     * @param player Name und Farbe des Spielers.
     */
    public void playerRegistered(Player player);

    /**
     * Ein Spieler hat einen Zug gemacht.
     *
     * @param color Farbe des Spielers, der gezogen hat.
     * @param row Zeile, in die der Spieler einen Stein gelegt hat (0 für die unterste Zeile, 5 für die oberste Zeile).
     * @param col Spalte, in die der Spieler einen Stein gelegt hat (0 für die ganz linke Spalte, 6 für die ganz rechte Spalte).
     * @param status Status des Spiels, nach diesem Zug.
     */
    public void playerMoved(PlayerColor color, int row, int col, GameStatus status);

    /**
     * Das Spiel ist beendet, entweder, weil ein Spieler gewonnen hat oder weil das Spielbrett voll ist (unentschieden).
     *
     * @param winner Farbe des Spielers, der gewonnen hat, bzw. {@code null}, wenn das Spiel unentschieden ist.
     */
    public void gameFinished(PlayerColor winner);
}
