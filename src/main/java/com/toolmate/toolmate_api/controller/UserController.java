package com.toolmate.toolmate_api.controller;

import com.toolmate.toolmate_api.dto.request.ChangePasswordRequest;
import com.toolmate.toolmate_api.dto.request.UpdateProfileRequest;
import com.toolmate.toolmate_api.dto.response.UserDTO;
import com.toolmate.toolmate_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

//import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User profile management APIs")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user's profile")
    public ResponseEntity<UserDTO> getCurrentUserProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUserProfile(authentication.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user profile by ID")
    public ResponseEntity<UserDTO> getUserProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfileById(id));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user's profile")
    public ResponseEntity<UserDTO> updateProfile(
           @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(userService.updateProfile(request, authentication.getName()));
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change user password")
    public ResponseEntity<String> changePassword(
           @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        userService.changePassword(request, authentication.getName());
        return ResponseEntity.ok("Password changed successfully");
    }

    @PutMapping("/profile-picture")
    @Operation(summary = "Update profile picture URL")
    public ResponseEntity<UserDTO> updateProfilePicture(
            @RequestParam String imageUrl,
            Authentication authentication) {
        return ResponseEntity.ok(userService.updateProfilePicture(imageUrl, authentication.getName()));
    }

    @DeleteMapping("/account")
    @Operation(summary = "Delete user account (soft delete)")
    public ResponseEntity<String> deleteAccount(Authentication authentication) {
        userService.deleteAccount(authentication.getName());
        return ResponseEntity.ok("Account deleted successfully");
    }

//    @GetMapping("/all")
//    @Operation(summary = "Get all users (for search/admin)")
//    public ResponseEntity<List<UserDTO>> getAllUsers() {
//        return ResponseEntity.ok(userService.getAllUsers());
//    }

//    @GetMapping("/search")
//    @Operation(summary = "Search users by name")
//    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String name) {
//        return ResponseEntity.ok(userService.searchUsersByName(name));
//    }


}