import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Zach on 9/21/16.
 */
public class Main {
    static HashMap<String, User> users = new HashMap<>();

    public static void main(String[] args) throws SQLException {
        org.h2.tools.Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();

        stmt.execute("CREATE TABLE IF NOT EXISTS games (id IDENTITY , name VARCHAR , genre VARCHAR , platform VARCHAR , releaseYear INT)");


        Spark.get(
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("userName");
                    User user = users.get(name);
                    ArrayList<Game> games = selectGames(conn);

                    HashMap m = new HashMap();
                    m.put("games", games);
                    if (user == null) {

                        return new ModelAndView(m, "login.html");
                    } else {

                        return new ModelAndView(m, "home.html");
                    }

                }), new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                ((request, response) -> {
                    String name = request.queryParams("loginName");
                    User user = users.get(name);
                    if (user == null) {
                        user = new User(name);
                        users.put(name, user);
                    }
                    Session session = request.session();
                    session.attribute("userName", name);

                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();

                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/create-game",
                ((request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("userName");
                    User user = users.get(name);
                    if (user == null) {
                        throw new Exception("User is not logged in");
                    }
                    String gameGenre = request.queryParams("gameGenre");
                    String gameName = request.queryParams("gameName");
                    String gamePlatform = request.queryParams("gamePlatform");
                    int gameYear = Integer.parseInt(request.queryParams("gameYear"));
                    insertGame(conn, gameName, gameGenre, gamePlatform, gameYear);


                    Game game = new Game(gameName, gameGenre, gamePlatform, gameYear);
                    user.games.add(game);

                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/editGame",
                ((request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("userName");
                    User user = users.get(name);
                    if (user == null) {
                        throw new Exception("User is not logged in");
                    }
                    String gameGenre = request.queryParams("gameGenre");
                    String gameName = request.queryParams("gameName");
                    String gamePlatform = request.queryParams("gamePlatform");
                    int gameYear = Integer.parseInt(request.queryParams("gameYear"));
                    int id = Integer.parseInt(request.queryParams("id"));

                    editGame(conn,gameName,gameGenre,gamePlatform,gameYear,id);

                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/removeGame",
                ((request, response) -> {
                    int id = Integer.parseInt(request.queryParams("id"));
                    deleteGame(conn, id);

                    response.redirect("/");
                    return "";
                })
        );
    }

    public static void insertGame(Connection conn, String name, String genre, String platform, int releaseYear) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO games VALUES (null, ?, ?, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, genre);
        stmt.setString(3, platform);
        stmt.setInt(4, releaseYear);
        stmt.execute();
    }

    public static void deleteGame(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM games WHERE id = ?");
        stmt.setInt(1, id);
        stmt.execute();
    }

    public static ArrayList<Game> selectGames(Connection conn) throws SQLException {
        ArrayList<Game> games = new ArrayList<>();
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT * FROM games");
        while (results.next()){
            int id = results.getInt("id");
            String name = results.getString("name");
            String genre = results.getString("genre");
            String platform = results.getString("platform");
            int releaseYear = results.getInt("releaseYear");
            games.add(new Game(id,name,genre,platform,releaseYear));
        }
        return games;
    }

    public static void editGame(Connection conn, String name, String genre, String platform, int releaseYear, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE games TABLE SET name = ?, genre = ?, platform = ?, releaseYear = ? WHERE id =?");
        stmt.setString(1, name);
        stmt.setString(2, genre);
        stmt.setString(3, platform);
        stmt.setInt(4, releaseYear);
        stmt.setInt(5, id);
        stmt.execute();
    }
}