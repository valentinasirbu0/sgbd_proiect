package org.example.swingSetUp;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.sql.*;

public class MusicPlayer extends JFrame implements ActionListener {

    private JComboBox<String> albumComboBox;
    private JComboBox<String> midiComboBox;
    private JButton playButton;
    private JButton stopButton;
    private Sequencer sequencer;
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;

    public MusicPlayer() {
        super("Music Player");

        try {
            // Connect to the database
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/sgbd", "postgres", "valea");
            statement = connection.createStatement();

            // Create GUI components
            albumComboBox = new JComboBox<>();
            albumComboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Load the MIDI file names for the selected album
                    String albumName = (String) albumComboBox.getSelectedItem();
                    loadMidiFileNames(albumName);
                }
            });

            midiComboBox = new JComboBox<>();
            albumComboBox.setPreferredSize(new Dimension(200, 25));
            midiComboBox.setPreferredSize(new Dimension(200, 25));

            playButton = new JButton("Play");
            playButton.addActionListener(this);

            stopButton = new JButton("Stop");
            stopButton.addActionListener(this);

            JPanel controlPanel = new JPanel(new GridLayout(1, 4));
            controlPanel.add(new JLabel("Album:"));
            controlPanel.add(albumComboBox);
            controlPanel.add(new JLabel("MIDI File:"));
            controlPanel.add(midiComboBox);
            controlPanel.add(playButton);
            controlPanel.add(stopButton);

            // Add components to JFrame
            getContentPane().add(controlPanel, BorderLayout.NORTH);
            pack();
            setLocationRelativeTo(null);

            // Load the album names into the album combo box
            ResultSet albumsResultSet = statement.executeQuery("SELECT DISTINCT id, title FROM albums ORDER BY title ASC");
            while (albumsResultSet.next()) {

                String albumTitle = albumsResultSet.getString("title");

                // Check if the album table exists in the database
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet albumTableResultSet = metaData.getTables(null, null, albumTitle, null);
                if (albumTableResultSet.next()) {
                    // The album table exists, add the album title to the combo box
                    System.out.println("Album table found for album: " + albumTitle);
                    albumComboBox.addItem(albumTitle);
                } else {
                    // The album table does not exist
                    System.out.println("Album table not found for album: " + albumTitle);
                }
            }

            // Load the MIDI file names for the first album
            String albumName = (String) albumComboBox.getSelectedItem();
            loadMidiFileNames(albumName);

            // Initialize the sequencer
            sequencer = MidiSystem.getSequencer();
            sequencer.open();

        } catch (MidiUnavailableException ex) {
            throw new RuntimeException(ex);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void loadMidiFileNames(String albumName) {
        try {
            midiComboBox.removeAllItems();
            if (albumName != null) {
                // Check if the table exists
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet resultSet = metaData.getTables(null, null, albumName, null);
                if (resultSet.next()) {
                    // Table exists, load file names
                    PreparedStatement preparedStatement = connection.prepareStatement("SELECT file_name FROM " + albumName);
                    ResultSet fileResultSet = preparedStatement.executeQuery();
                    while (fileResultSet.next()) {
                        midiComboBox.addItem(fileResultSet.getString("file_name"));
                    }
                } else {
                    System.out.println("Table " + albumName + " does not exist.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playMidiFile(String midiName) {
        try {
            // Load the MIDI file from the database and play it
            String albumName = (String) albumComboBox.getSelectedItem();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT file_data FROM " + albumName + " WHERE file_name = ?");
            preparedStatement.setString(1, midiName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                byte[] midiData = resultSet.getBytes("file_data");
                sequencer.setSequence(MidiSystem.getSequence(new ByteArrayInputStream(midiData)));
                sequencer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMidiFile() {
        sequencer.stop();
        sequencer.setMicrosecondPosition(0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == albumComboBox) {
            // Load the MIDI file names for the selected album
            String albumName = (String) albumComboBox.getSelectedItem();
            loadMidiFileNames(albumName);
        } else if (source == playButton) {
            // Play the selected MIDI file
            String midiName = (String) midiComboBox.getSelectedItem();
            playMidiFile(midiName);
        } else if (source == stopButton) {
            // Stop the currently playing MIDI file
            stopMidiFile();
        }
    }
}
