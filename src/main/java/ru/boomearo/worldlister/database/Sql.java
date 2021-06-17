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

import org.sqlite.JDBC;

import ru.boomearo.worldlister.WorldLister;
import ru.boomearo.worldlister.database.sections.SectionWorld;
import ru.boomearo.worldlister.database.sections.SectionWorldPlayer;

public class Sql {
    private static Sql instance = null;
    private static final String CON_STR = "jdbc:sqlite:[path]database.db";

    public static synchronized Sql getInstance() throws SQLException {
        if (instance == null)
            instance = new Sql();
        return instance;
    }

    private Connection connection;

    private Sql() throws SQLException {
        DriverManager.registerDriver(new JDBC());
        this.connection = DriverManager.getConnection(CON_STR.replace("[path]", WorldLister.getInstance().getDataFolder() + File.separator));
    }

    public synchronized List<SectionWorld> getAllDataSettings() {
        try (Statement statement = this.connection.createStatement()) {
            List<SectionWorld> collections = new ArrayList<SectionWorld>();
            ResultSet resSet = statement.executeQuery("SELECT id, world, joinIf, access FROM settings");
            while (resSet.next()) {
                collections.add(new SectionWorld(resSet.getInt("id"), resSet.getString("world"), resSet.getBoolean("joinIf"), resSet.getString("access")));
            }
            return collections;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public synchronized SectionWorld getDataSettings(String name) {
        try (Statement statement = this.connection.createStatement()) {
            ResultSet resSet = statement.executeQuery("SELECT id, joinIf, access FROM settings WHERE world = '" + name + "' LIMIT 1");

            if (resSet.next()) {
                return new SectionWorld(resSet.getInt("id"), name, resSet.getBoolean("joinIf"), resSet.getString("access"));
            }
            return null;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public synchronized boolean eraseAllSettings() {
        try (Statement statement = this.connection.createStatement()) {
            return statement.execute("DELETE FROM settings");
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized void putSettings(String name, boolean joinIf, String access) {
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
    }

    public synchronized boolean removeSettings(String name) {
        try (Statement statement = this.connection.createStatement()) {
            return statement.execute("DELETE FROM settings WHERE world = '" + name + "'");
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized void updateSettings(String name, boolean joinIf, String access) {
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
    }


    public synchronized List<SectionWorldPlayer> getAllDataWorldPlayer(String worldName) {
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
    }

    public synchronized SectionWorldPlayer getDataWorldPlayer(String worldName, String name) {
        try (Statement statement = this.connection.createStatement()) {
            ResultSet resSet = statement.executeQuery("SELECT id, type, timeAdded, whoAdd FROM '" + worldName + "' WHERE name = '" + name + "' LIMIT 1");

            if (resSet.next()) {
                return new SectionWorldPlayer(resSet.getInt("id"), name, resSet.getString("type"), resSet.getLong("timeAdded"), resSet.getString("whoAdd"));
            }
            return null;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public synchronized boolean eraseAllWorldPlayer(String worldName) {
        try (Statement statement = this.connection.createStatement()) {
            return statement.execute("DELETE FROM '" + worldName + "'");
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized void putWorldPlayer(String worldName, String name, String type, Long timeAdded, String whoAdd) {
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
    }

    public synchronized boolean removeWorldPlayer(String worldName, String name) {
        try (Statement statement = this.connection.createStatement()) {
            return statement.execute("DELETE FROM '" + worldName + "' WHERE name = '" + name + "'");
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized void updateWorldPlayer(String worldName, String name, String type, Long timeAdded, String whoAdd) {
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
    }

    public synchronized void createNewDatabaseWorld(String world) {
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

    public synchronized void createNewDatabaseSettings() {
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


    public synchronized void Disconnect() throws SQLException {
        this.connection.close();
    }
}
