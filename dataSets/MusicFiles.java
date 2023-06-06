package org.example.dataSets;

import org.example.database.Database;
import org.postgresql.util.PGobject;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MusicFiles {
    private static Connection connection = null;

    public MusicFiles() {
        try {
            connection = Database.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        populateWithMusicFiles();
    }

    private static void populateWithMusicFiles() {
        Path rootDirectory = Paths.get("C:\\Users\\Valea\\Desktop\\java\\_8\\archive(1)");
        traverseDirectories(rootDirectory, 1);
    }

    private static void traverseDirectories(Path directory, int depth) {
        if (depth == 1 && Files.isDirectory(directory)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
                for (Path subpath : stream) {
                    if (Files.isDirectory(subpath)) {
                        System.out.println("Directory: " + subpath);
                        findAlbum(subpath);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (Files.isDirectory(directory)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
                for (Path subpath : stream) {
                    traverseDirectories(subpath, depth + 1);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void findAlbum(Path path) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM albums LIMIT 1");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                String name = rs.getString("title");
                while (existsAlbum(name.toLowerCase())) {
                    name = getNextAlbumName(name);
                }
                System.out.println("Created album " + name);
                createAlbum(name);
                addMusicFile(name, path);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getNextAlbumName(String name) {
        String query = "SELECT title FROM albums WHERE id > (SELECT id FROM albums WHERE title = ? LIMIT 1) ORDER BY id LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String nextName = rs.getString("title");
                    return nextName;
                } else {
                    return name;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addMusicFile(String name, Path path) {
        String query = "INSERT INTO " + name + "(file_name, file_data) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            File folder = new File(path.toUri());
            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".mid")) {
                    String file_name = file.getName();
                    byte[] file_data = Files.readAllBytes(file.toPath());
                    PGobject dataObject = new PGobject();
                    dataObject.setType("bytea");
                    dataObject.setValue(new String(file_data));
                    stmt.setString(1, file_name);
                    stmt.setObject(2, file_data);
                    stmt.executeUpdate();
                    System.out.println("Added music file " + file_name + " to database.");
                }
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean existsAlbum(String name) {
        String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = 'public' AND table_name = ?";
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private static void createAlbum(String name) {
        String sql =
                "CREATE TABLE " + name + "(\n" +
                        "    id SERIAL PRIMARY KEY NOT NULL ,\n" +
                        "    file_name VARCHAR(255) NOT NULL,\n" +
                        "    file_data BYTEA NOT NULL\n" +
                        ");";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
