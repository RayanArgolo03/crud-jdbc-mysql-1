package repositories.interfaces;

import org.bson.types.ObjectId;

import java.util.Objects;

public interface EntityRepository<T> {
    void save(T t);
    int deleteById(Object id);
}
