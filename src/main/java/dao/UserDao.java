package dao;


import bean.User;
import exception.DBException;
import exception.NotUniqueUserEmailException;
import exception.NotUniqueUserLoginException;

import java.sql.SQLException;
import java.util.List;


public interface UserDao {
    public List<User> selectAll() throws DBException;
    public List<User> selectAllWithoutDriverManager() throws DBException, SQLException;
    public int deleteById(int id) throws DBException;

    public void insert(User user) throws DBException, NotUniqueUserLoginException, NotUniqueUserEmailException;
    public int InsertWithReturnGeneratedKeys(User user) throws DBException, NotUniqueUserLoginException, NotUniqueUserEmailException;
    public void BulkInsert(List<User> Users) throws DBException, NotUniqueUserLoginException, NotUniqueUserEmailException;
    public void insertLongSQL(List<User> Users) throws DBException, NotUniqueUserLoginException, NotUniqueUserEmailException;
}
