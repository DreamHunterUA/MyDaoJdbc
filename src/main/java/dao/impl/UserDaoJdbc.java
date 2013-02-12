package dao.impl;

import bean.User;
import dao.UserDao;
import exception.DBException;
import exception.NotUniqueUserEmailException;
import exception.NotUniqueUserLoginException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class UserDaoJdbc implements UserDao {
    public static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
    public static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/production_eshop";
    public static final String LOGIN = "username";
    public static final String PASSWORD = "password";

    public static final String SELECT_ALL_SQL = "SELECT id, login, email FROM User";
    public static final String DELETE_BY_ID_SQL = "DELETE FROM User WHERE id = ?";
    public static final String INSERT_SQL = "INSERT INTO User (login, email) VALUES (?, ?)";
    public static final String SELECT_BY_LOGIN = "SELECT id FROM User WHERE login=?";
    public static final String SELECT_BY_EMAIL = "SELECT id FROM User WHERE email=?";

    static {
        JdbcUtils.initDriver(DRIVER_CLASS_NAME);
    }

    private Connection getConnection() throws DBException {
        try {
            return DriverManager.getConnection(JDBC_URL, LOGIN, PASSWORD);
        } catch (SQLException e) {
            throw new DBException("Can't create connection", e);
        }
    }

    @Override
    public List<User> selectAll() throws DBException {
        Connection conn = getConnection();
        Statement statement = null;
        ResultSet rs = null;
        try {
            PrepareConnection(conn);
            statement = conn.createStatement();
            rs = statement.executeQuery(SELECT_ALL_SQL);
            List<User> result = new ArrayList<User>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String login = rs.getString("login");
                String email = rs.getString("email");
                User user = new User(id);
                user.setLogin(login);
                user.setEmail(email);
                result.add(user);
            }
            conn.commit();
            return result;
        } catch (SQLException e) {
            JdbcUtils.rollbackQuietly(conn);
            throw new DBException("Can't execute SQL = '" + SELECT_ALL_SQL + "'", e);
        } finally {
            JdbcUtils.closeQuietly(rs);
            JdbcUtils.closeQuietly(statement);
            JdbcUtils.closeQuietly(conn);
        }
    }

    @Override
    public List<User> selectAllWithoutDriverManager() throws DBException, SQLException {
        Driver driver = new com.mysql.jdbc.Driver();
        java.util.Properties info = new java.util.Properties();
        info.put("user","username");
        info.put("password","password");
        Connection conn = driver.connect(JDBC_URL,info);
        Statement statement = null;
        ResultSet rs = null;
        try {
            PrepareConnection(conn);
            statement = conn.createStatement();
            rs = statement.executeQuery(SELECT_ALL_SQL);
            List<User> result = new ArrayList<User>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String login = rs.getString("login");
                String email = rs.getString("email");
                User user = new User(id);
                user.setLogin(login);
                user.setEmail(email);
                result.add(user);
            }
            conn.commit();
            return result;
        } catch (SQLException e) {
            JdbcUtils.rollbackQuietly(conn);
            throw new DBException("Can't execute SQL = '" + SELECT_ALL_SQL + "'", e);
        } finally {
            JdbcUtils.closeQuietly(rs);
            JdbcUtils.closeQuietly(statement);
            JdbcUtils.closeQuietly(conn);
        }

    }

    @Override
    public int deleteById(int id) throws DBException {
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            PrepareConnection(conn);
            ps = conn.prepareStatement(DELETE_BY_ID_SQL);
            ps.setInt(1, id);
            int result = ps.executeUpdate();
            conn.commit();
            return result;
        } catch (SQLException e) {
            JdbcUtils.rollbackQuietly(conn);
            throw new DBException("Can't execute SQL = '" + DELETE_BY_ID_SQL + "'", e);
        } finally {
            JdbcUtils.closeQuietly(ps);
            JdbcUtils.closeQuietly(conn);
        }
    }

    @Override
    public void insert(User user) throws DBException {
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            PrepareConnection(conn);

            CheckInputUserData(user, conn);

            ps = getInsertPreparedStatement(user, conn, ps,Statement.NO_GENERATED_KEYS);
            conn.commit();
        } catch (SQLException e) {
            JdbcUtils.rollbackQuietly(conn);
            throw new DBException("Can't execute SQL = '" + INSERT_SQL + "'", e);
        } finally {
            JdbcUtils.closeQuietly(ps);
            JdbcUtils.closeQuietly(conn);
        }
    }

    private PreparedStatement getInsertPreparedStatement(User user, Connection conn, PreparedStatement ps,int statement) throws SQLException {
        ps = conn.prepareStatement(INSERT_SQL,statement);
        ps.setString(1, user.getLogin());
        ps.setString(2, user.getEmail());
        ps.executeUpdate();
        return ps;
    }

    private void BatchInsertPreparedStatement(List<User> users, Connection conn, int statement) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(INSERT_SQL, statement);
        for(User user:users){
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getEmail());
            ps.addBatch();
        }
        ps.executeBatch();
    }

    @Override
    public int InsertWithReturnGeneratedKeys(User user) throws DBException {
        Connection conn = getConnection();
        PreparedStatement ps = null;
        int key = -1;
        ResultSet keys = null;
        try {
            PrepareConnection(conn);

            CheckInputUserData(user, conn);

            ps = getInsertPreparedStatement(user, conn, ps,Statement.RETURN_GENERATED_KEYS);
            keys = ps.getGeneratedKeys();
            keys.next();
            key = keys.getInt(1);
            conn.commit();
            return key;
        } catch (SQLException e) {
            JdbcUtils.rollbackQuietly(conn);
            throw new DBException("Can't execute SQL = '" + INSERT_SQL + "'", e);
        } finally {
            JdbcUtils.closeQuietly(ps);
            JdbcUtils.closeQuietly(conn);
        }
    }

    @Override
    public void BulkInsert(List<User> Users) throws DBException {
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            PrepareConnection(conn);
            for(User user:Users)
                CheckInputUserData(user, conn);

            BatchInsertPreparedStatement(Users, conn, Statement.NO_GENERATED_KEYS);

            conn.commit();

        } catch (SQLException e) {
            JdbcUtils.rollbackQuietly(conn);
            throw new DBException("Can't execute SQL = '" + INSERT_SQL + "'", e);
        } finally {
            JdbcUtils.closeQuietly(ps);
            JdbcUtils.closeQuietly(conn);
        }
    }

    @Override
    public void insertLongSQL(List<User> Users) throws DBException, NotUniqueUserLoginException, NotUniqueUserEmailException {

        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            PrepareConnection(conn);
            for(User user:Users){
                CheckInputUserData(user, conn);
                ps = conn.prepareStatement(INSERT_SQL);
                ps.setString(1, user.getLogin());
                ps.setString(2, user.getEmail());
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            JdbcUtils.rollbackQuietly(conn);
            throw new DBException("Can't execute SQL = '" + INSERT_SQL + "'  "+e.getMessage(), e);
        } finally {
            JdbcUtils.closeQuietly(ps);
            JdbcUtils.closeQuietly(conn);
        }

    }


    private void CheckInputUserData(User user, Connection conn) throws SQLException, NotUniqueUserLoginException, NotUniqueUserEmailException {
        if (existWithLogin0(conn, user.getLogin())) {
            throw new NotUniqueUserLoginException("Login '" + user.getLogin() + "' doubled");
        }
        if (existWithEmail0(conn, user.getEmail())) {
            throw new NotUniqueUserEmailException("Email '" + user.getEmail() + "' doubled");
        }
    }

    private void PrepareConnection(Connection conn) throws SQLException {
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        conn.setAutoCommit(false);
    }

    private boolean existWithLogin0(Connection conn, String login) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SELECT_BY_LOGIN);
        ps.setString(1, login);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    private boolean existWithEmail0(Connection conn, String email) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SELECT_BY_EMAIL);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }
}

