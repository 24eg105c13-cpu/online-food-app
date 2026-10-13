package com.example.foodordering.config;

import com.example.foodordering.model.Category;
import com.example.foodordering.model.FoodItem;
import com.example.foodordering.repository.CategoryRepository;
import com.example.foodordering.repository.FoodItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class SampleDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final FoodItemRepository foodItemRepository;

    @Override
    public void run(String... args) {
        try {
            if (categoryRepository.count() == 0) {
                log.info("Seeding initial categories and delicious menu items for live demonstration...");
                seedData();
                log.info("Sample menu catalog initialized successfully.");
            }
        } catch (Exception e) {
            log.warn("Could not seed initial sample data (MongoDB may be in read-only or offline mode): {}", e.getMessage());
        }
    }

    private void seedData() {
        // Seed Categories
        Map<String, String> catMap = new HashMap<>();

        Category burgers = categoryRepository.save(Category.builder()
                .name("Gourmet Burgers")
                .description("Handcrafted smash burgers and toasted brioche buns with premium cheeses")
                .imageUrl("https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
        catMap.put("burgers", burgers.getId());

        Category pizzas = categoryRepository.save(Category.builder()
                .name("Wood-Fired Pizzas")
                .description("Neapolitan style sourdough crust baked in 900-degree stone ovens")
                .imageUrl("https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=600&q=80")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
        catMap.put("pizzas", pizzas.getId());

        Category bowls = categoryRepository.save(Category.builder()
                .name("Asian Noodles & Bowls")
                .description("Aromatic broth bowls, stir-fried noodles, and rich curries")
                .imageUrl("https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=600&q=80")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
        catMap.put("bowls", bowls.getId());

        Category salads = categoryRepository.save(Category.builder()
                .name("Fresh Greens & Bowls")
                .description("Farm-to-table organic salad bowls with house dressings and protein")
                .imageUrl("https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=600&q=80")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
        catMap.put("salads", salads.getId());

        Category desserts = categoryRepository.save(Category.builder()
                .name("Desserts & Beverages")
                .description("Decadent lava cakes, cold brews, and seasonal fresh mocktails")
                .imageUrl("https://images.unsplash.com/photo-1551024709-8f23befc6f87?auto=format&fit=crop&w=600&q=80")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
        catMap.put("desserts", desserts.getId());

        // Seed Food Items
        foodItemRepository.save(FoodItem.builder()
                .name("Truffle Swiss Burger")
                .description("Double Angus beef patties, melted Gruyere cheese, caramelized onions, and black truffle aioli on a brioche bun.")
                .categoryId(catMap.get("burgers"))
                .price(new BigDecimal("12.99"))
                .imageUrl("https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80")
                .available(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        foodItemRepository.save(FoodItem.builder()
                .name("Smoky BBQ Bacon Burger")
                .description("Crispy smoked bacon, aged cheddar, beer-battered onion rings, and sweet hickory BBQ glaze.")
                .categoryId(catMap.get("burgers"))
                .price(new BigDecimal("11.50"))
                .imageUrl("https://images.unsplash.com/photo-1586190848861-99aa4a171e90?auto=format&fit=crop&w=600&q=80")
                .available(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        foodItemRepository.save(FoodItem.builder()
                .name("Margherita Di Bufala Pizza")
                .description("San Marzano tomato sauce, fresh buffalo mozzarella, fresh sweet basil, and extra virgin olive oil drizzle.")
                .categoryId(catMap.get("pizzas"))
                .price(new BigDecimal("14.00"))
                .imageUrl("https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?auto=format&fit=crop&w=600&q=80")
                .available(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        foodItemRepository.save(FoodItem.builder()
                .name("Spicy Pepperoni & Hot Honey Pizza")
                .description("Crispy cupping pepperoni, mozzarella, crushed red chili flakes, and a drizzle of spicy clover honey.")
                .categoryId(catMap.get("pizzas"))
                .price(new BigDecimal("15.75"))
                .imageUrl("https://images.unsplash.com/photo-1534308983496-4fabb1a015ee?auto=format&fit=crop&w=600&q=80")
                .available(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        foodItemRepository.save(FoodItem.builder()
                .name("Tokyo Shoyu Ramen")
                .description("Slow-simmered chicken and dashi broth, springy handmade noodles, tender chashu, and a marinated ajitsuke egg.")
                .categoryId(catMap.get("bowls"))
                .price(new BigDecimal("13.50"))
                .imageUrl("https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=600&q=80")
                .available(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        foodItemRepository.save(FoodItem.builder()
                .name("Teriyaki Glazed Salmon Bowl")
                .description("Pan-seared Atlantic salmon fillet, steamed jasmine rice, edamame, pickled cucumber, and toasted sesame seeds.")
                .categoryId(catMap.get("bowls"))
                .price(new BigDecimal("16.25"))
                .imageUrl("https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=600&q=80")
                .available(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        foodItemRepository.save(FoodItem.builder()
                .name("Mediterranean Quinoa & Avocado Salad")
                .description("Baby spinach, tri-color quinoa, ripe Hass avocado, Kalamata olives, feta cheese, and lemon-herb vinaigrette.")
                .categoryId(catMap.get("salads"))
                .price(new BigDecimal("10.50"))
                .imageUrl("https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=600&q=80")
                .available(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        foodItemRepository.save(FoodItem.builder()
                .name("Warm Belgian Molten Chocolate Lava Cake")
                .description("Rich dark chocolate fondant cake with a decadent warm gooey center, served with vanilla bean cream.")
                .categoryId(catMap.get("desserts"))
                .price(new BigDecimal("7.99"))
                .imageUrl("https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=600&q=80")
                .available(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        foodItemRepository.save(FoodItem.builder()
                .name("Iced Caramel Cold Brew")
                .description("18-hour cold steeped single-origin coffee with silky salted caramel syrup and velvety cold foam.")
                .categoryId(catMap.get("desserts"))
                .price(new BigDecimal("5.25"))
                .imageUrl("https://images.unsplash.com/photo-1517701550927-30cf4ba1dba5?auto=format&fit=crop&w=600&q=80")
                .available(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
    }
}
