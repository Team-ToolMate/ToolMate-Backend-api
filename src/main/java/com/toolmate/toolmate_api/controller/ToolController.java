package com.toolmate.toolmate_api.controller;

import com.toolmate.toolmate_api.dto.request.ToolRequest;
import com.toolmate.toolmate_api.dto.response.ToolResponse;
import com.toolmate.toolmate_api.service.ToolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tools")
@Tag(name = "Tools", description = "Tool management APIs")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;

    @PostMapping
    @Operation(summary = "Create a new tool listing")
    public ResponseEntity<ToolResponse> createTool(
            @RequestBody ToolRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(toolService.createTool(request, authentication.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tool by ID with calculated distance")
    public ResponseEntity<ToolResponse> getToolById(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(toolService.getToolById(id, authentication.getName()));
    }

    @GetMapping("/available")
    @Operation(summary = "Get all available tools nearby")
    public ResponseEntity<List<ToolResponse>> getAvailableTools(
            @RequestParam(required = false) Double maxDistance,
            Authentication authentication) {
        return ResponseEntity.ok(toolService.getAvailableTools(authentication.getName(), maxDistance));
    }

    @GetMapping("/my-tools")
    @Operation(summary = "Get my listed tools")
    public ResponseEntity<List<ToolResponse>> getMyTools(Authentication authentication) {
        return ResponseEntity.ok(toolService.getMyTools(authentication.getName()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update tool information")
    public ResponseEntity<ToolResponse> updateTool(
            @PathVariable Long id,
            @RequestBody ToolRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(toolService.updateTool(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a tool listing")
    public ResponseEntity<Void> deleteTool(
            @PathVariable Long id,
            Authentication authentication) {
        toolService.deleteTool(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}