import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;
import static spark.Spark.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        port(Integer.valueOf(System.getenv("PORT")));
        staticFileLocation("/public");

        get("/hello", (req, res) -> "Hello World");

        get("/test1", (req, res) -> "First App Test");

        get("/send", (req, res) -> {
            Producer prod = new Producer();
            WorkerProcess worker = new WorkerProcess();
            Runnable workerRunnable = new Runnable() {
                public void run() {
                    try {
                        worker.receive();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            new Thread(workerRunnable).start();

            try {
                prod.publish("test-message");
            } catch (Exception e) {
                e.printStackTrace();
            }

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("message", "Hel");
            return new ModelAndView(attributes, "send.ftl");
        }, new FreeMarkerEngine());

        get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("message", "msg1");

            return new ModelAndView(attributes, "index.ftl");
        }, new FreeMarkerEngine());

        HikariConfig config = new  HikariConfig();
        config.setJdbcUrl(System.getenv("JDBC_DATABASE_URL"));
        final HikariDataSource dataSource = (config.getJdbcUrl() != null) ?
                new HikariDataSource(config) : new HikariDataSource();

        get("/db", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            try(Connection connection = dataSource.getConnection()) {
                Statement stmt = connection.createStatement();
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
                stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
                ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

                ArrayList<String> output = new ArrayList<String>();
                while (rs.next()) {
                    output.add( "Read from DB: " + rs.getTimestamp("tick"));
                }

                attributes.put("results", output);
                return new ModelAndView(attributes, "db.ftl");
            } catch (Exception e) {
                attributes.put("message", "There was an error: " + e);
                return new ModelAndView(attributes, "error.ftl");
            }
        }, new FreeMarkerEngine());

    }

}
