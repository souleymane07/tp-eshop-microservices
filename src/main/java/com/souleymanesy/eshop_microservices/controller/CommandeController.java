package com.souleymanesy.eshop_microservices.controller;

import com.souleymanesy.eshop_microservices.model.Commande;
import com.souleymanesy.eshop_microservices.model.User;
import com.souleymanesy.eshop_microservices.service.CommandeService;
import com.souleymanesy.eshop_microservices.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/commandes")
public class CommandeController {

    private final CommandeService commandeService;
    private final UserService userService;

    @Autowired
    public CommandeController(CommandeService commandeService, UserService userService) {
        this.commandeService = commandeService;
        this.userService = userService;
    }

    // GET all commandes
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Commande>>> getAllCommandes() {
        List<EntityModel<Commande>> commandes = commandeService.getAllCommandes().stream()
                .map(commande -> EntityModel.of(commande,
                        linkTo(methodOn(CommandeController.class).getCommandeById(commande.getId())).withSelfRel(),
                        linkTo(methodOn(CommandeController.class).getAllCommandes()).withRel("commandes"),
                        linkTo(methodOn(UserController.class).getUserById(commande.getUser().getId())).withRel("user")))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(commandes,
                linkTo(methodOn(CommandeController.class).getAllCommandes()).withSelfRel()));
    }

    // GET commande by ID
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Commande>> getCommandeById(@PathVariable Long id) {
        return commandeService.getCommandeById(id)
                .map(commande -> EntityModel.of(commande,
                        linkTo(methodOn(CommandeController.class).getCommandeById(commande.getId())).withSelfRel(),
                        linkTo(methodOn(CommandeController.class).getAllCommandes()).withRel("commandes"),
                        linkTo(methodOn(UserController.class).getUserById(commande.getUser().getId())).withRel("user")))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().<EntityModel<Commande>>build());
    }

    // POST (create) a new commande
    @PostMapping
    public ResponseEntity<EntityModel<Commande>> createCommande(@RequestBody Commande commande) {
        if (commande.getUser() != null && commande.getUser().getId() != null) {
            Optional<User> existingUser = userService.getUserById(commande.getUser().getId());
            if (existingUser.isPresent()) {
                commande.setUser(existingUser.get());
                if (commande.getDateCommande() == null) {
                    commande.setDateCommande(LocalDateTime.now());
                }
                Commande savedCommande = commandeService.saveCommande(commande);
                EntityModel<Commande> orderModel = EntityModel.of(savedCommande,
                        linkTo(methodOn(CommandeController.class).getCommandeById(savedCommande.getId())).withSelfRel(),
                        linkTo(methodOn(CommandeController.class).getAllCommandes()).withRel("commandes"),
                        linkTo(methodOn(UserController.class).getUserById(savedCommande.getUser().getId())).withRel("user"));
                return ResponseEntity.status(HttpStatus.CREATED).body(orderModel);
            } else {
                return ResponseEntity.badRequest().<EntityModel<Commande>>build();
            }
        } else {
            return ResponseEntity.badRequest().<EntityModel<Commande>>build();
        }
    }

    // PUT (update) an existing commande
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Commande>> updateCommande(@PathVariable Long id, @RequestBody Commande commande) {
        return commandeService.getCommandeById(id)
                .map(existingCommande -> {
                    existingCommande.setDateCommande(commande.getDateCommande() != null ? commande.getDateCommande() : existingCommande.getDateCommande());
                    existingCommande.setMontantTotal(commande.getMontantTotal());
                    existingCommande.setStatut(commande.getStatut());

                    // Mise à jour de l'utilisateur si elle est fournie
                    if (commande.getUser() != null && commande.getUser().getId() != null) {
                        Optional<User> newUser = userService.getUserById(commande.getUser().getId());
                        if (newUser.isPresent()) {
                            existingCommande.setUser(newUser.get());
                        } else {
                            return ResponseEntity.badRequest().<EntityModel<Commande>>build();
                        }
                    }

                    Commande updatedCommande = commandeService.saveCommande(existingCommande);
                    EntityModel<Commande> orderModel = EntityModel.of(updatedCommande,
                            linkTo(methodOn(CommandeController.class).getCommandeById(updatedCommande.getId())).withSelfRel(),
                            linkTo(methodOn(CommandeController.class).getAllCommandes()).withRel("commandes"),
                            linkTo(methodOn(UserController.class).getUserById(updatedCommande.getUser().getId())).withRel("user"));
                    return ResponseEntity.ok(orderModel);
                })
                .orElse(ResponseEntity.notFound().<EntityModel<Commande>>build());
    }

    // DELETE a commande
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommande(@PathVariable Long id) {
        if (commandeService.getCommandeById(id).isPresent()) {
            commandeService.deleteCommande(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}