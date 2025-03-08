package modules;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class PersistenceModule extends AbstractModule {
    @Provides
    @Inject
    public EntityManagerFactory provideEntityManagerFactory() {
        return Persistence.createEntityManagerFactory("defaultPersistenceUnit");
    }

    @Provides
    @Singleton
    public EntityManager provideEntityManager(EntityManagerFactory emf) {
        return emf.createEntityManager();
    }
}
