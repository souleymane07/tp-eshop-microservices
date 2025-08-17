package com.souleymanesy.eshop_microservices.repository;

import com.souleymanesy.eshop_microservices.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // Pour la liste des commandes par utilisateur

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {

}