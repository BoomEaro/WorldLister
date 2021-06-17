package ru.boomearo.worldlister.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.sqlite.JDBC;

import ru.boomearo.bungeechatplus.utils.runnable.AdvThreadFactory;
import ru.boomearo.worldlister.WorldLister;
import ru.boomearo.worldlister.database.sections.SectionWorld;
import ru.boomearo.worldlister.database.sections.SectionWorldPlayer;

public class Sql {

    private final ExecutorService executor;
    private final Connection connection;

    private static Sql instance = null;
    private static final String CON_STR = "jdbc:sqlite:[path]database.db";

    public static Sql getInstance() {
        return instance;
    }

    public static void initSql() throws SQLException {
        if (instance != null) {
            return;
        }

        instance = new Sql();
    }

    private Sql() throws SQLException {
        DriverManager.registerDriver(new JDBC());

        this.connection = DriverManager.getConnection(CON_STR.replace("[path]", WorldLister.getInstance().getDataFolder() + File.separator));

        this.executor = Executors.newFixedThreadPool(1, new AdvThreadFactory("WorldLister-SQL", 3));

        for (World w : Bukkit.getWorlds()) {
            Sql.getInstance().createNewDatabaseWorld(w.getName());
        }

        Sql.getInstance().createNewDatabaseSettings();
    }

    public Future<SectionWorld> getDataSettings(String name) {
        return this.executor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("SELECT id, joinIf, access FROM settings WHERE world = ? LIMIT 1")) {
                statement.setString(1, name);

                ResultSet resSet = statement.executeQuery();

                if (resSet.next()) {
                    return new SectionWorld(resSet.getInt("id"), name, resSet.getBoolean("joinIf"), resSet.getString("access"));
                }
                return null;
            }
            catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public void putSettings(String name, boolean joinIf, String access) {
        this.executor.execute(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "INSERT INTO settings(`world`, `joinIf`, `access`) " +
                            "VALUES(?, ?, ?)")) {
                statement.setString(1, name);
                statement.setBoolean(2, joinIf);
                statement.setString(3, access);
                statement.execute();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void removeSettings(String name) {
        this.executor.execute(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("DELETE FROM settings WHERE world = ?")) {
                statement.setString(1, name);

                statement.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void updateSettings(String name, boolean joinIf, String access) {
        this.executor.execute(() -> {
            String sql = "UPDATE settings SET joinIf = ? , "
                    + "access = ? "
                    + "WHERE world = ?";

            try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {

                pstmt.setBoolean(1, joinIf);
                pstmt.setString(2, access);
                pstmt.setString(3, name);
                pstmt.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    //TODO потенциальная иньекция
    public Future<List<SectionWorldPlayer>> getAllDataWorldPlayer(String worldName) {
        return this.executor.submit(() -> {
            try (Statement statement = this.connection.createStatement()) {
                List<SectionWorldPlayer> collections = new ArrayList<SectionWorldPlayer>();
                ResultSet resSet = statement.executeQuery("SELECT id, name, type, timeAdded, whoAdd FROM '" + worldName + "'");
                while (resSet.next()) {
                    collections.add(new SectionWorldPlayer(resSet.getInt("id"), resSet.getString("name"), resSet.getString("type"), resSet.getLong("timeAdded"), resSet.getString("whoAdd")));
                }
                return collections;
            }
            catch (SQLException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        });
    }

    public void putWorldPlayer(String worldName, String name, String type, Long timeAdded, String whoAdd) {
        this.executor.execute(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "INSERT INTO '" + worldName + "'(`name`, `type`, `timeAdded`, `whoAdd`) " +
                            "VALUES(?, ?, ?, ?)")) {
                statement.setString(1, name);
                statement.setString(2, type);
                statement.setLong(3, timeAdded);
                statement.setString(4, whoAdd);
                statement.execute();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void removeWorldPlayer(String worldName, String name) {
        this.executor.execute(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("DELETE FROM '" + worldName + "' WHERE name = ?")) {
                statement.setString(1, name);

                statement.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void updateWorldPlayer(String worldName, String name, String type, Long timeAdded, String whoAdd) {
        this.executor.execute(() -> {
            String sql = "UPDATE '" + worldName + "' SET type = ? , "
                    + "timeAdded = ? , "
                    + "whoAdd = ? "
                    + "WHERE name = ?";

            try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {

                pstmt.setString(1, type);
                pstmt.setLong(2, timeAdded);
                pstmt.setString(3, whoAdd);
                pstmt.setString(4, name);
                pstmt.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void createNewDatabaseWorld(String world) {
        String sql = "CREATE TABLE IF NOT EXISTS '" + world + "' (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	name text NOT NULL,\n"
                + "	type text NOT NULL,\n"
                + "	timeAdded long NOT NULL,\n"
                + "	whoAdd text NOT NULL\n"
                + ");";

        try (Statement stmt = this.connection.createStatement()) {
            stmt.execute(sql);
            WorldLister.getInstance().getLogger().info("Таблица мира '" + world + "' успешно загружена.");
        }
        catch (SQLException e) {

            e.printStackTrace();
        }
    }

    private void createNewDatabaseSettings() {
        String sql = "CREATE TABLE IF NOT EXISTS settings (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	world text NOT NULL,\n"
                + "	joinIf boolean NOT NULL,\n"
                + "	access text NOT NULL\n"
                + ");";

        try (Statement stmt = this.connection.createStatement()) {
            stmt.execute(sql);
            WorldLister.getInstance().getLogger().info("Таблица настроек миров успешно загружена.");
        }
        catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public void disconnect() throws SQLException, InterruptedException {
        this.executor.shutdown();
        this.executor.awaitTermination(15, TimeUnit.SECONDS);
        this.connection.close();
    }
}
