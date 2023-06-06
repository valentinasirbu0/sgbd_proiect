package org.example.swingSetUp;

import javax.swing.*;

public class StartSwing {
    public StartSwing() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create the MusicPlayer JFrame and show it
        MusicPlayer musicPlayer = new MusicPlayer();
        musicPlayer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        musicPlayer.setVisible(true);
    }
}
