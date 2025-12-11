# ToolMate-Backend-api

📁 Project Structure

        toolmate-api/
              ├── src/main/java/com/toolmate/api/
              │   ├── ToolmateApiApplication.java
              │   ├── config/
              │   │   ├── CorsConfig.java
              │   │   └── SwaggerConfig.java
              │   ├── controller/
              │   │   ├── AuthController.java
              │   │   ├── ToolController.java
              │   │   ├── BorrowRequestController.java
              │   │   ├── MessageController.java
              │   │   ├── ReviewController.java
              │   │   └── UserController.java
              │   ├── dto/
              │   │   ├── request/
              │   │   │   ├── BorrowRequestRequest.java
              │   │   │   ├── ChangePasswordRequest.java
              │   │   │   ├── LoginRequest.java
              │   │   │   ├── MessageRequest.java
              │   │   │   ├── RegisterRequest.java
              │   │   │   ├── ReviewRequest.java
              │   │   │   ├── ToolRequest.java
              │   │   │   └── UpdateProfileRequest.java
              │   │   └── response/
              │   │       ├── AuthResponse.java
              │   │       ├── BorrowRequestResponse.java
              │   │       ├── MessageResponse.java
              │   │       ├── OwnerDTO.java
              │   │       ├── ReviewResponse.java
              │   │       ├── ToolResponse.java
              │   │       └── UserDTO.java
              │   ├── entity/
              │   │   ├── User.java
              │   │   ├── Tool.java
              │   │   ├── BorrowRequest.java
              │   │   ├── BorrowRequestStatus.java
              │   │   ├── Message.java
              │   │   ├── Review.java
              │   │   ├── ConditionChecklist.java
              │   │   └── TrustBadge.java
              │   ├── repository/
              │   │   ├── UserRepository.java
              │   │   ├── ToolRepository.java
              │   │   ├── BorrowRequestRepository.java
              │   │   ├── MessageRepository.java
              │   │   ├── ReviewRepository.java
              │   │   ├── ConditionChecklistRepository.java
              │   │   └── TrustBadgeRepository.java
              │   ├── security/
              │   │   ├── JwtUtil.java
              │   │   ├── JwtAuthenticationFilter.java
              │   │   ├── UserDetailsServiceImpl.java
              │   │   └── SecurityConfig.java
              │   └── service/
              │       ├── AuthService.java
              │       ├── ToolService.java
              │       ├── BorrowRequestService.java
              │       ├── MessageService.java
              │       ├── ReviewService.java
              │       └── UserService.java
              └── src/main/resources/
                  └── application.properties

    


    
