package com.securemarts.domain.catalog.repository;

import com.securemarts.domain.catalog.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByMenuIdOrderByPositionAsc(Long menuId);

    List<MenuItem> findByMenuIdAndParentIsNullOrderByPositionAsc(Long menuId);

    void deleteByMenuId(Long menuId);
}
