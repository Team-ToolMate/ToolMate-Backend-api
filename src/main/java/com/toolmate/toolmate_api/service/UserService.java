package com.toolmate.toolmate_api.service;

import com.toolmate.toolmate_api.dto.request.ChangePasswordRequest;
import com.toolmate.toolmate_api.dto.request.UpdateProfileRequest;
import com.toolmate.toolmate_api.dto.response.UserDTO;
import com.toolmate.toolmate_api.entity.User;
import com.toolmate.toolmate_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


//      Get current user's profile

    public UserDTO getCurrentUserProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return convertToDTO(user);
    }


//     Get any user's profile by ID (for viewing other users)

    public UserDTO getUserProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return convertToDTO(user);
    }


//     Update current user's profile

    @Transactional
    public UserDTO updateProfile(UpdateProfileRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update only provided fields (null values will be ignored)

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getLatitude() != null) {
            user.setLatitude(request.getLatitude());
        }

        if (request.getLongitude() != null) {
            user.setLongitude(request.getLongitude());
        }

        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }


//     Change user password

    @Transactional
    public void changePassword(ChangePasswordRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }


//     Upload/Update profile picture

    @Transactional
    public UserDTO updateProfilePicture(String imageUrl, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setProfileImageUrl(imageUrl);
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }


//      Delete user account

    @Transactional
    public void deleteAccount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Set user as inactive instead of deleting (soft delete)
        user.setIsActive(false);
        userRepository.save(user);

        // Or completely delete:
        // userRepository.delete(user);
    }


    @Transactional
    public void updateFcmToken(String fcmToken, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }


//     Get all users (for admin or search purposes)

//    public List<UserDTO> getAllUsers() {
//        return userRepository.findAll().stream()
//                .filter(User::getIsActive)
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }


//     Search users by name

//    public List<UserDTO> searchUsersByName(String name) {
//        return userRepository.findAll().stream()
//                .filter(user -> user.getIsActive() &&
//                        user.getFullName().toLowerCase().contains(name.toLowerCase()))
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }


//     Helper method to convert User entity to UserDTO

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getLatitude(),
                user.getLongitude(),
                user.getAddress(),
                user.getProfileImageUrl(),
                user.getRating(),
                user.getTotalBorrows(),
                user.getTotalLends()
        );
    }
}