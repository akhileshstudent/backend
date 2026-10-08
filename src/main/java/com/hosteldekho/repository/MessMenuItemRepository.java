package com.hosteldekho.repository;

import com.hosteldekho.entity.MessMenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessMenuItemRepository extends JpaRepository<MessMenuItem, Long> {
    List<MessMenuItem> findByHostelIdOrderByDayOfWeek(Long hostelId);
    void deleteByHostelId(Long hostelId);
}
