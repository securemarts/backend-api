package com.securemarts.domain.catalog.service;

import com.securemarts.domain.catalog.entity.Collection;
import com.securemarts.domain.catalog.entity.Menu;
import com.securemarts.domain.catalog.entity.MenuItem;
import com.securemarts.domain.catalog.repository.CollectionRepository;
import com.securemarts.domain.catalog.repository.MenuItemRepository;
import com.securemarts.domain.catalog.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreBootstrapService {

    private final CollectionRepository collectionRepository;
    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;

    /**
     * Creates default content for a newly created store:
     * "All Products" collection, "Main Menu", and "Footer Menu".
     * Idempotent -- skips any default that already exists.
     */
    @Transactional
    public void bootstrapDefaults(Long storeId) {
        try {
            Collection allProducts = bootstrapAllProductsCollection(storeId);
            bootstrapMainMenu(storeId, allProducts);
            bootstrapFooterMenu(storeId);
            log.info("Bootstrapped default content for store {}", storeId);
        } catch (Exception e) {
            log.error("Failed to bootstrap defaults for store {} – {}", storeId, e.getMessage(), e);
        }
    }

    private Collection bootstrapAllProductsCollection(Long storeId) {
        return collectionRepository.findByHandleAndStoreId("all", storeId)
                .orElseGet(() -> {
                    Collection c = new Collection();
                    c.setStoreId(storeId);
                    c.setTitle("All Products");
                    c.setHandle("all");
                    c.setDescription("All products in the store");
                    c.setCollectionType(Collection.CollectionType.SMART);
                    c.setConditionsOperator("all");
                    return collectionRepository.save(c);
                });
    }

    private void bootstrapMainMenu(Long storeId, Collection allProducts) {
        if (menuRepository.existsByStoreIdAndHandle(storeId, "main-menu")) return;

        Menu menu = new Menu();
        menu.setStoreId(storeId);
        menu.setHandle("main-menu");
        menu.setTitle("Main Menu");
        menu = menuRepository.save(menu);

        MenuItem home = new MenuItem();
        home.setMenu(menu);
        home.setTitle("Home");
        home.setType(MenuItem.MenuItemType.FRONTPAGE);
        home.setUrl("/");
        home.setPosition(0);
        menuItemRepository.save(home);

        MenuItem catalog = new MenuItem();
        catalog.setMenu(menu);
        catalog.setTitle("Catalog");
        catalog.setType(MenuItem.MenuItemType.COLLECTION);
        catalog.setResourceId(allProducts.getPublicId());
        catalog.setUrl("/collections/" + allProducts.getPublicId());
        catalog.setPosition(1);
        menuItemRepository.save(catalog);
    }

    private void bootstrapFooterMenu(Long storeId) {
        if (menuRepository.existsByStoreIdAndHandle(storeId, "footer")) return;

        Menu menu = new Menu();
        menu.setStoreId(storeId);
        menu.setHandle("footer");
        menu.setTitle("Footer");
        menu = menuRepository.save(menu);

        MenuItem home = new MenuItem();
        home.setMenu(menu);
        home.setTitle("Home");
        home.setType(MenuItem.MenuItemType.FRONTPAGE);
        home.setUrl("/");
        home.setPosition(0);
        menuItemRepository.save(home);
    }
}
