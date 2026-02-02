package com.example.Ask.Controllers;

import com.example.Ask.Service.AnimalService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import com.example.Ask.Entities.Animal;
import com.example.Ask.Entities.Gender;
import java.util.List;
import org.springframework.http.ResponseEntity;
import com.example.Ask.Repositories.RequestRepository;
import com.example.Ask.Service.EmailService;
import com.example.Ask.Entities.Request;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.example.Ask.Service.UserService;
import com.example.Ask.Service.RequestService;
import com.example.Ask.Service.ThingsboardService;
import com.example.Ask.Entities.User;
import java.util.Map;

@RestController
@RequestMapping("/api/animals")
public class AnimalController {

    private AnimalService animalservice;
    private RequestRepository requestRepository;
    private EmailService emailService;
    private UserService userService;
    private RequestService requestService;
    private ThingsboardService thingsboardService;

    public AnimalController(AnimalService animalservice, RequestRepository requestRepository, EmailService emailService, UserService userService, RequestService requestService, ThingsboardService thingsboardService) {
        this.animalservice = animalservice;
        this.requestRepository = requestRepository;
        this.emailService = emailService;
        this.userService = userService;
        this.requestService = requestService;
        this.thingsboardService = thingsboardService;
    }

    @RequestMapping("")
    public List<Animal> showAnimals() {
        return animalservice.getAnimalsWithPresignedUrls();
    }

    @GetMapping("/{id}")
    public Animal showAnimal(@PathVariable Integer id){
        return animalservice.getAnimalWithPresignedUrl(animalservice.getAnimal(id));
    }

    @PostMapping("")
    public Animal createAnimal(@RequestBody Animal animal) {
        Animal savedAnimal = animalservice.saveAnimal(animal);
        
        // Send notification to all users about new animal
        List<User> allUsers = userService.getUsers();
        for (User user : allUsers) {
            if (user.getEmail() != null && user.getEmailVerified() != null && user.getEmailVerified()) {
                try {
                    emailService.sendNewAnimalNotification(
                        user.getEmail(),
                        user.getUsername(),
                        savedAnimal.getName(),
                        savedAnimal.getType(),
                        savedAnimal.getAge(),
                        savedAnimal.getGender() != null ? savedAnimal.getGender().toString() : "Άγνωστο",
                        savedAnimal.getImageUrl()
                    );
                } catch (Exception e) {
                    System.err.println("Failed to send new animal notification to " + user.getEmail() + ": " + e.getMessage());
                }
            }
        }
        
        // Send animal data to ThingsBoard
        try {
            thingsboardService.sendAnimalData(
                savedAnimal.getName(),
                savedAnimal.getType(),
                savedAnimal.getAge(),
                savedAnimal.getGender() != null ? savedAnimal.getGender().toString() : "Άγνωστο",
                savedAnimal.getId()
            );
        } catch (Exception e) {
            System.err.println("Failed to send animal data to ThingsBoard: " + e.getMessage());
        }
        
        return savedAnimal;
    }

    @PutMapping("/{id}")
    public Animal updateAnimal(@PathVariable Integer id, @RequestBody Animal animal) {
        animal.setId(id);
        return animalservice.saveAnimal(animal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnimal(@PathVariable Integer id) {
        Animal animal = animalservice.getAnimal(id);
        animalservice.Delanimal(animal);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/Request/{id}")
    public ResponseEntity<?> requestAnimal(@PathVariable Integer id) {
        Animal animal = animalservice.getAnimal(id);
        if (animal == null) {
            return ResponseEntity.notFound().build();
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        
        // Mark animal as requested
        animal.setReq(1);
        animal.setUserId(user.getId());
        animalservice.saveAnimal(animal);
        
        // Create Request object from Animal
        Request request = new Request();
        request.setName(animal.getName());
        request.setType(animal.getType());
        request.setGender(animal.getGender());
        request.setAge(animal.getAge());
        request.setImageUrl(animal.getImageUrl()); // Copy image URL from Animal
        request.setUserId(user.getId());
        request.setAdminApproved(0);
        request.setDocApproved(0);
        
        Request savedRequest = requestService.saveRequest(request);
        
        // Send email notification
        if (user.getEmail() != null) {
            try {
                emailService.sendRequestSubmittedEmail(user.getEmail(), user.getUsername(), animal.getName());
            } catch (Exception e) {
                System.err.println("Failed to send request email: " + e.getMessage());
            }
        }
        
        return ResponseEntity.ok(savedRequest);
    }

    @PutMapping("/Deny/{id}")
    public Animal denyAnimal(@PathVariable Integer id) {
        Animal animal = animalservice.getAnimal(id);
        animal.setReq(0);
        return animalservice.saveAnimal(animal);
    }

    @PostMapping("/{id}/accept-adoption")
    public ResponseEntity<String> acceptAdoption(@PathVariable Integer id) {
        Animal animal = animalservice.getAnimal(id);
        // Get the user email from the userId field
        String userEmail = null;
        if (animal.getUserId() != null) {
            User user = userService.getUser(animal.getUserId());
            if (user != null) {
                userEmail = user.getEmail();
            }
        }
        if (userEmail != null) {
            emailService.send(userEmail, "Η υιοθεσία σας έγινε αποδεκτή!", "Η υιοθεσία του ζώου " + animal.getName() + " έγινε αποδεκτή.");
        }
        animalservice.Delanimal(animal);
        return ResponseEntity.ok("Adoption accepted, animal deleted, and email sent.");
    }







}