package org.example.dataSets;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.example.DAO.AlbumDAO;
import org.example.DAO.ArtistDAO;
import org.example.DAO.GenreDAO;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CSVData {
    public CSVData() {

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("C:\\Users\\Valea\\Desktop\\java\\_8\\albumlist.csv"));
            CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(br);
            var artists = new ArtistDAO();
            var genres = new GenreDAO();
            var albums = new AlbumDAO();
            for (CSVRecord record : parser) {
                String field1 = record.get("Artist");
                System.out.println(field1);
                artists.create(field1);
                String field2 = record.get("Genre");
                genres.create(field2);
                String field3 = record.get("Year");
                albums.create(Integer.parseInt(field3), removeSpecialChars(record.get("Album")), field1, field2);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // Handle the exception
                }
            }
        }
    }

    public static String removeSpecialChars(String input) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                output.append(c);
            } else if (i > 0 && Character.isLetterOrDigit(input.charAt(i - 1))) {
                output.append("_");
            }
        }
        return output.toString().toLowerCase();
    }
}
