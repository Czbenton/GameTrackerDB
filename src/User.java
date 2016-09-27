import java.util.ArrayList;

/**
 * Created by Zach on 9/21/16.
 */
public class User {
    String name;
    ArrayList<Game> games = new ArrayList<>();

    public User(String name) {
        this.name = name;
    }
}
