package service;

import com.google.inject.Inject;
import jakarta.persistence.EntityManager;
import model.Datasets;
import java.util.List;

public class DatasetService {

    private final EntityManager entityManager;

    @Inject
    public DatasetService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Datasets> getAllDatasets() {
        if(!entityManager.getTransaction().isActive()){
            entityManager.getTransaction().begin();
        }
        List<Datasets> datasets = entityManager.createQuery("from Datasets", Datasets.class).getResultList();

        for(Datasets dataset : datasets){
            entityManager.refresh(dataset);
        }
        entityManager.getTransaction().commit();
        return datasets;
    }

    public Datasets getDatasetById(String id) {
        if(!entityManager.getTransaction().isActive()){
            entityManager.getTransaction().begin();
        }
        Datasets dataset = entityManager.find(Datasets.class, id);
        if(dataset == null){
            throw new NullPointerException("Dataset with id " + id + " not found");
        }
        entityManager.refresh(dataset);
        entityManager.getTransaction().commit();
        return dataset;
    }

    public void createDataset(Datasets dataset) {
        if(!entityManager.getTransaction().isActive()){
            entityManager.getTransaction().begin();
        }
        if(entityManager.find(Datasets.class, dataset.getId()) != null){
            throw new IllegalArgumentException("Dataset with id " + dataset.getId() + " already exists");
        }
        entityManager.persist(dataset);
        entityManager.getTransaction().commit();
    }

    public void updateDataset(Datasets dataset) {
        if(!entityManager.getTransaction().isActive()){
            entityManager.getTransaction().begin();
        }
        entityManager.merge(dataset);
        entityManager.getTransaction().commit();
    }

    public void deleteDataset(String id) {
        if(!entityManager.getTransaction().isActive()){
            entityManager.getTransaction().begin();
        }
        Datasets dataset = entityManager.find(Datasets.class, id);
        if(dataset == null){
            throw new NullPointerException("Dataset with id " + id + " not found");
        }
        entityManager.remove(dataset);
        entityManager.getTransaction().commit();
    }
}
