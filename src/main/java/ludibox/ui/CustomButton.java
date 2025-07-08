package ludibox.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomButton extends JButton {

    public CustomButton(String text, CustomButtonStyle style) {
        super(text);

        this.setContentAreaFilled(false);
        this.setOpaque(true);
        this.setFocusPainted(false);
        this.setFont(new Font("Arial", Font.BOLD, 16));

        applyStyle(style);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setBackground(getBackground().darker());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                applyStyle(style);
            }
        });
    }

    private void applyStyle(CustomButtonStyle style) {
        switch (style) {
            case SIMPLE -> {
                this.setBackground(Color.WHITE);
                this.setForeground(Color.BLACK);
            }
            case DARK -> {
                this.setBackground(Color.DARK_GRAY);
                this.setForeground(Color.WHITE);
            }
            case LIGHT -> {
                this.setBackground(Color.LIGHT_GRAY);
                this.setForeground(Color.BLACK);
            }
        }
    }
}
