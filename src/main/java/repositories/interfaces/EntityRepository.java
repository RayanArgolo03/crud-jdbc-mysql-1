package repositories.interfaces;

public interface EntityRepository<T> {
    void save(T t);
}
