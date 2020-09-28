package de.awacademy.gamecontest.common;

public class Player {

    private String name;

    private PlayerColor color;


    public Player(String name, PlayerColor color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public PlayerColor getColor() {
        return color;
    }

    public boolean isBeginner() {
        return color.isBeginner();
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", color=" + color +
                '}';
    }
}
