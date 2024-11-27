package grabber;

import grabber.model.Post;
import grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static grabber.HabrCareerParse.SOURCE_LINK;

public class PsqlStore implements Store {

    private Connection connection;

    public PsqlStore(Properties config) {
        try {
            Class.forName(config.getProperty("jdbc.driver"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Post save(Post post) {
        try (PreparedStatement statement =
                     connection.prepareStatement("INSERT INTO posts(name, text, link, created) VALUES (?, ?, ?, ?) "
                                     + "ON CONFLICT(link) DO UPDATE SET "
                                     + "name=EXCLUDED.name, text=EXCLUDED.text, created=EXCLUDED.created",
                             Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM posts")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(create(resultSet));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    private Post create(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("link"),
                resultSet.getString("text"),
                resultSet.getTimestamp("created").toLocalDateTime()
        );
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM posts WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    post = create(resultSet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public static void main(String[] args) {
        try (InputStream input = PsqlStore.class.getClassLoader()
                .getResourceAsStream("db/liquibase.properties")) {
            Properties config = new Properties();
            config.load(input);
            PsqlStore psqlstore = new PsqlStore(config);
            HabrCareerParse habrcareerparse = new HabrCareerParse(new HabrCareerDateTimeParser());
            List<Post> postList = habrcareerparse.list(SOURCE_LINK);
            for (Post post : postList) {
                psqlstore.save(post);
            }
            System.out.println("getAll()");
            System.out.println(psqlstore.getAll());
            System.out.println("getById(10)");
            System.out.println(psqlstore.findById(10));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}