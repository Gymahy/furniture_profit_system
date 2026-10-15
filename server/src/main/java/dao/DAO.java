package dao;

import java.util.List;

public interface DAO<T> {
    List<T> findAll() throws Exception;

    T save(T entity) throws Exception;

    void delete(Integer id) throws Exception;
}
