package com.securemarts.domain.catalog.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.catalog.dto.*;
import com.securemarts.domain.catalog.entity.Menu;
import com.securemarts.domain.catalog.entity.MenuItem;
import com.securemarts.domain.catalog.repository.MenuItemRepository;
import com.securemarts.domain.catalog.repository.MenuRepository;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private static final int MAX_DEPTH = 3;

    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;
    private final StoreRepository storeRepository;

    public Long resolveStoreId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .map(com.securemarts.domain.onboarding.entity.Store::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
    }

    @Transactional
    public MenuResponse create(Long storeId, CreateMenuRequest request) {
        String handle = request.getHandle().toLowerCase().trim();
        if (menuRepository.existsByStoreIdAndHandle(storeId, handle)) {
            throw new BusinessRuleException("A menu with handle '" + handle + "' already exists for this store");
        }

        Menu menu = new Menu();
        menu.setStoreId(storeId);
        menu.setHandle(handle);
        menu.setTitle(request.getTitle().trim());
        menu = menuRepository.save(menu);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            persistItems(menu, null, request.getItems(), 1);
        }

        return MenuResponse.from(menuRepository.findByPublicIdAndStoreIdWithItems(menu.getPublicId(), storeId)
                .orElse(menu));
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> list(Long storeId) {
        return menuRepository.findByStoreIdOrderByCreatedAtAsc(storeId).stream()
                .map(MenuResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MenuResponse get(Long storeId, String menuPublicId) {
        Menu menu = menuRepository.findByPublicIdAndStoreIdWithItems(menuPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", menuPublicId));
        return MenuResponse.from(menu);
    }

    @Transactional(readOnly = true)
    public MenuResponse getByHandle(Long storeId, String handle) {
        Menu menu = menuRepository.findByStoreIdAndHandleWithItems(storeId, handle.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Menu", handle));
        return MenuResponse.from(menu);
    }

    @Transactional
    public MenuResponse update(Long storeId, String menuPublicId, UpdateMenuRequest request) {
        Menu menu = menuRepository.findByPublicIdAndStoreId(menuPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", menuPublicId));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            menu.setTitle(request.getTitle().trim());
        }

        if (request.getItems() != null) {
            menuItemRepository.deleteByMenuId(menu.getId());
            menu.getItems().clear();
            menuRepository.saveAndFlush(menu);

            if (!request.getItems().isEmpty()) {
                persistItems(menu, null, request.getItems(), 1);
            }
        }

        menu = menuRepository.save(menu);
        return MenuResponse.from(menuRepository.findByPublicIdAndStoreIdWithItems(menu.getPublicId(), storeId)
                .orElse(menu));
    }

    @Transactional
    public void delete(Long storeId, String menuPublicId) {
        Menu menu = menuRepository.findByPublicIdAndStoreId(menuPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", menuPublicId));
        menuRepository.delete(menu);
    }

    private void persistItems(Menu menu, MenuItem parent, List<MenuItemRequest> requests, int depth) {
        if (depth > MAX_DEPTH) {
            throw new BusinessRuleException("Menu items cannot be nested more than " + MAX_DEPTH + " levels deep");
        }
        if (requests == null) return;

        for (int i = 0; i < requests.size(); i++) {
            MenuItemRequest req = requests.get(i);
            validateItemRequest(req);

            MenuItem item = new MenuItem();
            item.setMenu(menu);
            item.setParent(parent);
            item.setTitle(req.getTitle().trim());
            item.setType(parseType(req.getType()));
            item.setResourceId(req.getResourceId());
            item.setUrl(resolveUrl(item.getType(), req.getResourceId(), req.getUrl()));
            item.setPosition(req.getPosition() != null ? req.getPosition() : i);
            item = menuItemRepository.save(item);

            if (req.getChildren() != null && !req.getChildren().isEmpty()) {
                persistItems(menu, item, req.getChildren(), depth + 1);
            }
        }
    }

    private void validateItemRequest(MenuItemRequest req) {
        MenuItem.MenuItemType type = parseType(req.getType());
        if ((type == MenuItem.MenuItemType.COLLECTION || type == MenuItem.MenuItemType.PRODUCT)
                && (req.getResourceId() == null || req.getResourceId().isBlank())) {
            throw new BusinessRuleException("resourceId is required for " + type + " menu items");
        }
        if (type == MenuItem.MenuItemType.HTTP && (req.getUrl() == null || req.getUrl().isBlank())) {
            throw new BusinessRuleException("url is required for HTTP menu items");
        }
    }

    private MenuItem.MenuItemType parseType(String type) {
        try {
            return MenuItem.MenuItemType.valueOf(type.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid menu item type: " + type
                    + ". Must be one of: COLLECTION, PRODUCT, HTTP, FRONTPAGE");
        }
    }

    private String resolveUrl(MenuItem.MenuItemType type, String resourceId, String rawUrl) {
        return switch (type) {
            case COLLECTION -> "/collections/" + resourceId;
            case PRODUCT -> "/products/" + resourceId;
            case HTTP -> rawUrl;
            case FRONTPAGE -> "/";
        };
    }
}
