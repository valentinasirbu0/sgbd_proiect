package org.example.DAO;

import org.example.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AlbumDAO {
    private static Connection connection = null;

    public AlbumDAO() {
        try {
            connection = Database.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void create(int year, String title, String artist, String genre) {
        if (getAlbumId(year, title) == null) {
            addInAlbums(year, title, artist);
            addInAlbumsGenre(year, title, genre);
        }
    }

    private void addInAlbums(java.lang.Integer year, String title, String artist) {
        String sql = "INSERT INTO albums (year, title, artist_id) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, year);
            statement.setString(2, title);
            statement.setInt(3, ArtistDAO.getArtistId(artist));
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addInAlbumsGenre(Integer year, String title, String genre) {
        String[] words = genre.split("[, /\"]+");
        for (String word : words) {
            String sqlSelect = "SELECT COUNT(*) FROM album_genre WHERE album_id = ? AND genre_id = ?";
            String sqlInsert = "INSERT INTO album_genre (album_id, genre_id) VALUES (?, ?)";

            try (PreparedStatement selectStatement = connection.prepareStatement(sqlSelect);
                 PreparedStatement insertStatement = connection.prepareStatement(sqlInsert)) {
                int albumId = getAlbumId(year, title);
                Integer genreId = GenreDAO.getGenreId(word);

                if (genreId == null) {
                    var genres = new GenreDAO();
                    genres.create(word);
                    genreId = GenreDAO.getGenreId(word);
                }

                selectStatement.setInt(1, albumId);
                selectStatement.setInt(2, genreId);
                ResultSet resultSet = selectStatement.executeQuery();
                resultSet.next();
                int count = resultSet.getInt(1);

                if (count == 0) {
                    insertStatement.setInt(1, albumId);
                    insertStatement.setInt(2, genreId);
                    insertStatement.executeUpdate();
                    connection.commit();
                } else {
                    System.out.println("Duplicate entry: Album ID = " + albumId + ", Genre ID = " + genreId);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private java.lang.Integer getAlbumId(java.lang.Integer year, String title) {
        String sql = "SELECT id FROM albums WHERE year = CAST(? AS INTEGER) AND title = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, year);
            statement.setString(2, title);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
