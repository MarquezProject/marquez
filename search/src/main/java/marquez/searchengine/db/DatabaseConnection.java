package marquez.searchengine.db;

import org.jdbi.v3.core.Jdbi;

public class DatabaseConnection {

    public static Jdbi initializeJdbi() {
        String jdbcUrl = "jdbc:postgresql://localhost:5432/marquez";
        String username = "marquez";
        String password = "marquez";

        return Jdbi.create(jdbcUrl, username, password);
    }
}