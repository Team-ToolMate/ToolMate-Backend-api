package com.toolmate.toolmate_api.repository;

import com.toolmate.toolmate_api.entity.Tool;
import com.toolmate.toolmate_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ToolRepository extends JpaRepository<Tool, Long> {
    List<Tool> findByOwner(User owner);
    List<Tool> findByCategory(String category);
    List<Tool> findByIsAvailableTrue();

    @Query("SELECT t FROM Tool t WHERE t.isAvailable = true AND t.owner.id != :userId")
    List<Tool> findAvailableToolsExcludingUser(@Param("userId") Long userId);
}