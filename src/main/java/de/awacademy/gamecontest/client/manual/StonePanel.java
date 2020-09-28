package de.awacademy.gamecontest.client.manual;

import de.awacademy.gamecontest.common.PlayerColor;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class StonePanel extends JPanel implements MouseListener {

    private GameView view;

    private int row;

    private int col;

    /** Color of stone in this field, <code>null</code> if field is empty. */
    private PlayerColor stoneColor;


    public StonePanel(GameView view, int row, int col) {
        this.view = view;
        this.row = row;
        this.col = col;
        addMouseListener(this);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public PlayerColor getStoneColor() {
        return stoneColor;
    }

    public void setStoneColor(PlayerColor stoneColor) {
        this.stoneColor = stoneColor;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        if (stoneColor == null) {
            return;
        }

        Dimension panelSize = getSize();
        int circleWidth = (int) panelSize.getWidth() - 10;
        int circleHeight = (int) panelSize.getHeight() - 10;

        graphics.setColor(stoneColor == PlayerColor.YELLOW ? Color.YELLOW : Color.RED);
        graphics.fillOval(5, 5, circleWidth, circleHeight);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (view.isActivePlayerNext() && e.getButton() == MouseEvent.BUTTON1) {
            view.fieldClicked(row, col);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
