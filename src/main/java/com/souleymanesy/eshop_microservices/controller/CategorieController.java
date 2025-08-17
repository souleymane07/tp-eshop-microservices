package com.souleymanesy.eshop_microservices.controller;

import com.souleymanesy.eshop_microservices.model.Categorie;
import com.souleymanesy.eshop_microservices.service.CategorieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/categories")
public class CategorieController {

    private final CategorieService categorieService;

    @Autowired
    public CategorieController(CategorieService categorieService) {
        this.categorieService = categorieService;
    }

    // GET all categories
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Categorie>>> getAllCategories() {
        List<EntityModel<Categorie>> categories = categorieService.getAllCategories().stream()
                .map(categorie -> EntityModel.of(categorie,
                        linkTo(methodOn(CategorieController.class).getCategorieById(categorie.getId())).withSelfRel(),
                        linkTo(methodOn(CategorieController.class).getAllCategories()).withRel("categories")))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(categories,
                linkTo(methodOn(CategorieController.class).getAllCategories()).withSelfRel()));
    }

    // GET category by ID
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Categorie>> getCategorieById(@PathVariable Long id) {
        return categorieService.getCategorieById(id)
                .map(categorie -> EntityModel.of(categorie,
                        linkTo(methodOn(CategorieController.class).getCategorieById(categorie.getId())).withSelfRel(),
                        linkTo(methodOn(CategorieController.class).getAllCategories()).withRel("categories")))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST (create) a new category
    @PostMapping
    public ResponseEntity<EntityModel<Categorie>> createCategorie(@RequestBody Categorie categorie) {
        Categorie savedCategorie = categorieService.saveCategorie(categorie);
        EntityModel<Categorie> categoryModel = EntityModel.of(savedCategorie,
                linkTo(methodOn(CategorieController.class).getCategorieById(savedCategorie.getId())).withSelfRel(),
                linkTo(methodOn(CategorieController.class).getAllCategories()).withRel("categories"));
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryModel);
    }

    // PUT (update) an existing category
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Categorie>> updateCategorie(@PathVariable Long id, @RequestBody Categorie categorie) {
        return categorieService.getCategorieById(id)
                .map(existingCategorie -> {
                    existingCategorie.setNom(categorie.getNom());
                    existingCategorie.setDescription(categorie.getDescription());
                    Categorie updatedCategorie = categorieService.saveCategorie(existingCategorie);
                    EntityModel<Categorie> categoryModel = EntityModel.of(updatedCategorie,
                            linkTo(methodOn(CategorieController.class).getCategorieById(updatedCategorie.getId())).withSelfRel(),
                            linkTo(methodOn(CategorieController.class).getAllCategories()).withRel("categories"));
                    return ResponseEntity.ok(categoryModel);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE a category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategorie(@PathVariable Long id) {
        if (categorieService.getCategorieById(id).isPresent()) {
            categorieService.deleteCategorie(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}