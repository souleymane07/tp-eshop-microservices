package com.souleymanesy.eshop_microservices.service;


import com.souleymanesy.eshop_microservices.model.Categorie;
import com.souleymanesy.eshop_microservices.repository.CategorieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategorieService {

    private final CategorieRepository categorieRepository;

    @Autowired
    public CategorieService(CategorieRepository categorieRepository) {
        this.categorieRepository = categorieRepository;
    }

    // Méthode pour obtenir toutes les catégories
    public List<Categorie> getAllCategories() {
        return categorieRepository.findAll();
    }

    // Méthode pour obtenir une catégorie par son ID
    public Optional<Categorie> getCategorieById(Long id) {
        return categorieRepository.findById(id);
    }

    // Méthode pour créer ou mettre à jour une catégorie
    public Categorie saveCategorie(Categorie categorie) {
        return categorieRepository.save(categorie);
    }

    // Méthode pour supprimer une catégorie par son ID
    public void deleteCategorie(Long id) {
        categorieRepository.deleteById(id);
    }
}
