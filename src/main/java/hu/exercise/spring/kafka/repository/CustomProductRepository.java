package hu.exercise.spring.kafka.repository;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.springframework.stereotype.Repository;

import hu.exercise.spring.kafka.input.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class CustomProductRepository{
	
//	@PersistenceContext
//	public EntityManager entityManager;
//
//    public Stream<Product> findAllProductsNative(String requestid){
//        return entityManager.createQuery("FROM Product p JOIN p.run r WHERE r.requestid <> :id order by id asc", Product.class)
//          .setParameter("id", requestid)
//          .getResultStream();
//    }
    
    
//    public Stream<Product> updateProductsNative(String requestid){
//        return entityManager.createQuery("FROM Product p JOIN p.run r WHERE r.requestid <> :id", Product.class)
//          .setParameter("id", requestid)
//          .getResultStream();
//    }

    public void updateAll(Iterable<Product> productList) {
    	
//    	CriteriaBuilder criteriaBuilder=entityManager.getCriteriaBuilder();
//    	CriteriaDelete<City> criteriaDelete = criteriaBuilder.createCriteriaDelete(City.class);
//
//    	Root<City> root = criteriaDelete.from(City.class);
//    	criteriaDelete.where(root.in(list));
//    	entityManager.createQuery(criteriaDelete).executeUpdate();
//    	List<City> list = ...
//    			int i=0;
//
//    			for(City city:list {
//    			    if(++i%49==0) {
//    			        entityManager.flush();
//    			    }
//    			    entityManager.remove(city);
//    			}
    	
	}
    
//    public void insertAll(Iterable<Product> productList) {
//    	int i=0;
//
//		for(Product p:productList) {
//		    if(++i%49==0) {
//		        entityManager.flush();
//		    }
//		    entityManager.persist(p);
//		}
//	}
//
//	public void deleteAll(Iterable<Product> productList) {
//		int i=0;
//
//		for(Product p:productList) {
//		    if(++i%49==0) {
//		        entityManager.flush();
//		    }
//		    entityManager.remove(p);
//		}
//		
//	}
}
