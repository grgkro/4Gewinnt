package de.awacademy.gamecontest.common;

public enum PlayerColor {

    YELLOW(true),

    RED(false);


    private boolean beginner;


    private PlayerColor(boolean beginner) {
        this.beginner = beginner;
    }

    public boolean isBeginner() {
        return beginner;
    }
}
