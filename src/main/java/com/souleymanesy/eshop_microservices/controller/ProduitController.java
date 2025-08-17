package com.souleymanesy.eshop_microservices.controller;

import com.souleymanesy.eshop_microservices.model.Produit;
import com.souleymanesy.eshop_microservices.service.ProduitService;
import com.souleymanesy.eshop_microservices.service.CategorieService;
import com.souleymanesy.eshop_microservices.model.Categorie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;


@RestController
@RequestMapping("/api/produits")
public class ProduitController {

    private final ProduitService produitService;
    private final CategorieService categorieService;

    @Autowired
    public ProduitController(ProduitService produitService, CategorieService categorieService) {
        this.produitService = produitService;
        this.categorieService = categorieService;
    }

    // GET all produits
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Produit>>> getAllProduits() {
        List<EntityModel<Produit>> produits = produitService.getAllProduits().stream()
                .map(produit -> EntityModel.of(produit,
                        linkTo(methodOn(ProduitController.class).getProduitById(produit.getId())).withSelfRel(),
                        linkTo(methodOn(ProduitController.class).getAllProduits()).withRel("produits"),
                        linkTo(methodOn(CategorieController.class).getCategorieById(produit.getCategorie().getId())).withRel("categorie")))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(produits,
                linkTo(methodOn(ProduitController.class).getAllProduits()).withSelfRel()));
    }

    // GET produit by ID
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Produit>> getProduitById(@PathVariable Long id) {
        return produitService.getProduitById(id)
                .map(produit -> EntityModel.of(produit,
                        linkTo(methodOn(ProduitController.class).getProduitById(produit.getId())).withSelfRel(),
                        linkTo(methodOn(ProduitController.class).getAllProduits()).withRel("produits"),
                        linkTo(methodOn(CategorieController.class).getCategorieById(produit.getCategorie().getId())).withRel("categorie")))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().<EntityModel<Produit>>build());
    }

    // POST (create) a new produit
    @PostMapping
    public ResponseEntity<EntityModel<Produit>> createProduit(@RequestBody Produit produit) {
        if (produit.getCategorie() != null && produit.getCategorie().getId() != null) {
            Optional<Categorie> existingCategorie = categorieService.getCategorieById(produit.getCategorie().getId());
            if (existingCategorie.isPresent()) {
                produit.setCategorie(existingCategorie.get());
                Produit savedProduit = produitService.saveProduit(produit);
                EntityModel<Produit> productModel = EntityModel.of(savedProduit,
                        linkTo(methodOn(ProduitController.class).getProduitById(savedProduit.getId())).withSelfRel(),
                        linkTo(methodOn(ProduitController.class).getAllProduits()).withRel("produits"),
                        linkTo(methodOn(CategorieController.class).getCategorieById(savedProduit.getCategorie().getId())).withRel("categorie"));
                return ResponseEntity.status(HttpStatus.CREATED).body(productModel);
            } else {
                return ResponseEntity.badRequest().<EntityModel<Produit>>build();
            }
        } else {
            return ResponseEntity.badRequest().<EntityModel<Produit>>build();
        }
    }

    // PUT (update) an existing produit
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Produit>> updateProduit(@PathVariable Long id, @RequestBody Produit produit) {
        return produitService.getProduitById(id)
                .map(existingProduit -> {
                    existingProduit.setNom(produit.getNom());
                    existingProduit.setDescription(produit.getDescription());
                    existingProduit.setPrix(produit.getPrix());
                    existingProduit.setQuantiteStock(produit.getQuantiteStock());

                    if (produit.getCategorie() != null && produit.getCategorie().getId() != null) {
                        Optional<Categorie> newCategorie = categorieService.getCategorieById(produit.getCategorie().getId());
                        if (newCategorie.isPresent()) {
                            existingProduit.setCategorie(newCategorie.get());
                        } else {
                            return ResponseEntity.badRequest().<EntityModel<Produit>>build();
                        }
                    }

                    Produit updatedProduit = produitService.saveProduit(existingProduit);
                    EntityModel<Produit> productModel = EntityModel.of(updatedProduit,
                            linkTo(methodOn(ProduitController.class).getProduitById(updatedProduit.getId())).withSelfRel(),
                            linkTo(methodOn(ProduitController.class).getAllProduits()).withRel("produits"),
                            linkTo(methodOn(CategorieController.class).getCategorieById(updatedProduit.getCategorie().getId())).withRel("categorie"));
                    return ResponseEntity.ok(productModel);
                })
                .orElse(ResponseEntity.notFound().<EntityModel<Produit>>build());
    }

    // DELETE a produit
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduit(@PathVariable Long id) {
        if (produitService.getProduitById(id).isPresent()) {
            produitService.deleteProduit(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}