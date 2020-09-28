package de.awacademy.gamecontest.client.manual;

import javax.swing.JTextField;

public class PrefixTextField extends JTextField {

    private String prefixText;

    private String mainText;


    public PrefixTextField(String prefixText) {
        super(prefixText);
        this.prefixText = prefixText;
        this.mainText = "";
        setEditable(false);
    }

    public void setMainText(String mainText) {
        this.mainText = mainText;
        super.setText(prefixText + mainText);
    }
}
