package fr.efrei.repository;

import java.util.List;

public interface IRepository<T> {

    T save(T entity);

    T findById(String id);

    List<T> findAll();

    boolean update(T entity);

    boolean delete(String id);
}

