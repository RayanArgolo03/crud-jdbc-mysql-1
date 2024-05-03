package dao.interfaces;

public interface EntityDAO<T> {
    void save(T t);
    int deleteById(long id);
}
