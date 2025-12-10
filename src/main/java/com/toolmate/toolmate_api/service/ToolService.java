package com.toolmate.toolmate_api.service;

import com.toolmate.toolmate_api.dto.response.OwnerDTO;
import com.toolmate.toolmate_api.dto.request.ToolRequest;
import com.toolmate.toolmate_api.dto.response.ToolResponse;
import com.toolmate.toolmate_api.entity.Tool;
import com.toolmate.toolmate_api.entity.User;
import com.toolmate.toolmate_api.repository.ToolRepository;
import com.toolmate.toolmate_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ToolService {

    private final ToolRepository toolRepository;
    private final UserRepository userRepository;

    public ToolResponse createTool(ToolRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Tool tool = new Tool();
        tool.setName(request.getName());
        tool.setDescription(request.getDescription());
        tool.setCategory(request.getCategory());
        tool.setCondition(request.getCondition());
        tool.setImageUrls(request.getImageUrls());
        tool.setRentalFee(request.getRentalFee());
        tool.setIsFullyCharged(request.getIsFullyCharged());
        tool.setOwner(user);

        Tool savedTool = toolRepository.save(tool);
        return convertToResponse(savedTool, 0.0);
    }

    public ToolResponse getToolById(Long toolId, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Tool tool = toolRepository.findById(toolId)
                .orElseThrow(() -> new IllegalArgumentException("Tool not found"));

        double distance = calculateDistance(
                currentUser.getLatitude(), currentUser.getLongitude(),
                tool.getOwner().getLatitude(), tool.getOwner().getLongitude()
        );

        return convertToResponse(tool, distance);
    }

    public List<ToolResponse> getAvailableTools(String userEmail, Double maxDistance) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Tool> tools = toolRepository.findAvailableToolsExcludingUser(currentUser.getId());

        return tools.stream()
                .map(tool -> {
                    double distance = calculateDistance(
                            currentUser.getLatitude(), currentUser.getLongitude(),
                            tool.getOwner().getLatitude(), tool.getOwner().getLongitude()
                    );
                    return convertToResponse(tool, distance);
                })
                .filter(toolResponse -> maxDistance == null || toolResponse.getDistance() <= maxDistance)
                .sorted((t1, t2) -> Double.compare(t1.getDistance(), t2.getDistance()))
                .collect(Collectors.toList());
    }

    public List<ToolResponse> getMyTools(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return toolRepository.findByOwner(user).stream()
                .map(tool -> convertToResponse(tool, 0.0))
                .collect(Collectors.toList());
    }

    public ToolResponse updateTool(Long toolId, ToolRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Tool tool = toolRepository.findById(toolId)
                .orElseThrow(() -> new IllegalArgumentException("Tool not found"));

        if (!tool.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only update your own tools");
        }

        tool.setName(request.getName());
        tool.setDescription(request.getDescription());
        tool.setCategory(request.getCategory());
        tool.setCondition(request.getCondition());
        tool.setImageUrls(request.getImageUrls());
        tool.setRentalFee(request.getRentalFee());
        tool.setIsFullyCharged(request.getIsFullyCharged());

        Tool updatedTool = toolRepository.save(tool);
        return convertToResponse(updatedTool, 0.0);
    }

    public void deleteTool(Long toolId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Tool tool = toolRepository.findById(toolId)
                .orElseThrow(() -> new IllegalArgumentException("Tool not found"));

        if (!tool.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only delete your own tools");
        }

        toolRepository.delete(tool);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth's radius in kilometers

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        return R * c;
    }

    private ToolResponse convertToResponse(Tool tool, double distance) {
        OwnerDTO ownerDTO = new OwnerDTO(
                tool.getOwner().getId(),
                tool.getOwner().getFullName(),
                tool.getOwner().getRating(),
                tool.getOwner().getProfileImageUrl()
        );

        return new ToolResponse(
                tool.getId(),
                tool.getName(),
                tool.getDescription(),
                tool.getCategory(),
                tool.getCondition(),
                tool.getImageUrls(),
                tool.getIsAvailable(),
                tool.getRentalFee(),
                tool.getIsFullyCharged(),
                distance,
                tool.getRating(),
                tool.getTotalBorrows(),
                ownerDTO,
                tool.getCreatedAt()
        );
    }
}