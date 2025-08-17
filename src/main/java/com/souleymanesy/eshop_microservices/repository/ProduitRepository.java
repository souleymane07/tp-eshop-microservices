package com.souleymanesy.eshop_microservices.repository;

import com.souleymanesy.eshop_microservices.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

}