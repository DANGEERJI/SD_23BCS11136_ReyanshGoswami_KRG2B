package com.example.exposeit.Post.Controller;

import com.example.exposeit.Post.Entity.Post;
import com.example.exposeit.Post.Entity.PostDocument;
import com.example.exposeit.Post.Service.PostService;
import com.example.exposeit.User.Entity.User;
import com.example.exposeit.User.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Post postRequest, Principal principal){ //add principal
        try {
            String username = principal.getName();
            User author = userRepository.findByUserName(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found!"));
//            User author = userRepository.findAll().stream().findFirst()
//                    .orElseThrow(() -> new RuntimeException("No users in DB! Please register one user first."));
            postRequest.setAuthor(author);
            postService.createPost(postRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Successfully created!");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/trending")
    public ResponseEntity<List<PostDocument>> getGlobalTrending(){
        return ResponseEntity.ok(postService.getGlobalTrending());
    }

    @GetMapping("/nearby/trending")
    public ResponseEntity<List<PostDocument>> getNearbyTrending(@RequestParam double lat, @RequestParam double lon){
        System.out.println("At Controller");
        return ResponseEntity.ok(postService.getNearbyRecent(lat, lon));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<PostDocument>> getByCategory(@RequestParam Set<String> categories){
        return ResponseEntity.ok(postService.getPostByCategory(categories));
    }
}
