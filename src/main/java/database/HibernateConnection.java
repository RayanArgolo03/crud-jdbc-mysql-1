package database;

import exceptions.DatabaseException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import lombok.extern.log4j.Log4j2;

import java.util.function.Consumer;


@Log4j2
public final class HibernateConnection {

    private static HibernateConnection INSTANCE;
    private final EntityManager manager;

    private HibernateConnection(String persistenceUnit) {
        manager = Persistence.createEntityManagerFactory(persistenceUnit)
                .createEntityManager();
    }

    public static HibernateConnection getINSTANCE(String persistenceUnit) {
        if (INSTANCE == null) INSTANCE = new HibernateConnection(persistenceUnit);
        return INSTANCE;
    }

    public EntityManager getManager() {
        return manager;
    }

    public void execute(final Consumer<EntityManager> action) {

        EntityTransaction transaction = null;

        try {
            transaction = manager.getTransaction();
            transaction.begin();
            action.accept(manager);
            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {

                try {
                    transaction.rollback();
                } catch (Exception ee) {
                    log.error("Error in rollback transaction operation: {}", ee.getMessage());
                }
            }

            throw new DatabaseException(e.getCause().getMessage(), e);

        } finally {
            clearPersistenceContext();
        }

    }

    public void clearPersistenceContext() {
        manager.clear();
    }

}
