package com.example.demo.config;

import com.example.demo.model.Category;
import com.example.demo.model.Edible;
import com.example.demo.model.Inedible;
import com.example.demo.model.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        seedUsers();
        seedCategories();
        seedProducts();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;

        Object[][] users = {
            {"Diego",   "Nava",     "pokeplace889988@gmail.com",   User.Role.ADMIN},
            {"Rassiel", "Miranda",  "rassimiranda23@gmail.com",    User.Role.CASHIER},
            {"Gustavo", "Nava",     "gusnava57@gmail.com",         User.Role.KITCHEN},
        };

        for (Object[] row : users) {
            if (userRepository.findByEmail((String) row[2]).isPresent()) continue;
            User u = new User();
            u.setName((String) row[0]);
            u.setLastname((String) row[1]);
            u.setEmail((String) row[2]);
            u.setRole((User.Role) row[3]);
            userRepository.save(u);
            log.info("Usuario creado: {} {} ({})", row[0], row[1], row[2]);
        }
    }

    private void seedCategories() {
        List.of("Hamburguesas", "Cafés Calientes", "Bebidas Frías", "Llaveros").forEach(name -> {
            if (!categoryRepository.existsByName(name)) {
                Category cat = new Category();
                cat.setName(name);
                categoryRepository.save(cat);
                log.info("Categoria creada: {}", name);
            }
        });
    }

    private void seedProducts() {
        Long burgers   = categoryRepository.findByName("Hamburguesas").map(Category::getId).orElse(null);
        Long hotDrinks = categoryRepository.findByName("Cafés Calientes").map(Category::getId).orElse(null);
        Long icedDrinks = categoryRepository.findByName("Bebidas Frías").map(Category::getId).orElse(null);
        Long keychains = categoryRepository.findByName("Llaveros").map(Category::getId).orElse(null);

        if (burgers == null || hotDrinks == null || icedDrinks == null || keychains == null) {
            log.error("No se encontraron todas las categorias. Abortando seed de productos.");
            return;
        }

        // Hamburguesas
        edible("PokéBurger", "PokéBurger",
                "Jugosa carne especial a la parrilla con tomate fresco, pepinillos, lechuga crujiente y nuestra salsa especial de la casa. Incluye papas fritas.",
                15.0, 8.0, burgers);
        edible("SuperBurger", "SuperBurger",
                "Carne especial a la parrilla con tomate, pepinillos, lechuga, queso cheddar, jamón y salsa especial de la casa. Incluye papas fritas.",
                20.0, 10.0, burgers);
        edible("UltraBurger", "UltraBurger",
                "La combinación definitiva: carne especial, tomate, pepinillos, lechuga, queso cheddar, jamón, tocino crujiente y extra salsa especial de la casa. Incluye papas fritas.",
                25.0, 12.0, burgers);
        edible("MasterBurger", "MasterBurger",
                "El nivel maestro: carne especial, tomate, pepinillos, lechuga, queso cheddar, jamón, tocino, salchicha y extra salsa especial de la casa. Incluye papas fritas.",
                30.0, 15.0, burgers);

        // Cafes Calientes
        edible("Café Bombón con Canela", "Hypno",
                "Intenso espresso con leche condensada y un toque aromático de canela. Dulce, cremoso y reconfortante.",
                12.0, 6.0, hotDrinks);
        edible("Café Moca con Canela", "Golem",
                "Espresso con chocolate oscuro, leche vaporizada y un toque de canela. Suave, aromático e irresistible.",
                12.0, 6.0, hotDrinks);
        edible("Café Bombón", "Drowzee",
                "Espresso con leche condensada en perfecta proporción. Dulce e intenso para los amantes del café.",
                11.0, 5.5, hotDrinks);
        edible("Café Moca", "Graveler",
                "Espresso con chocolate y leche vaporizada. El clásico sabor moca que enamora.",
                11.0, 5.5, hotDrinks);
        edible("Latte con Canela", "Kadabra",
                "Suave espresso con generosa capa de leche vaporizada y un toque especial de canela.",
                11.0, 5.0, hotDrinks);
        edible("Latte", "Abra",
                "Espresso suave envuelto en una amplia capa de leche vaporizada. Simple, cremoso y equilibrado.",
                10.0, 4.5, hotDrinks);
        edible("Doble Espresso", "Doduo",
                "Doble dosis de espresso concentrado y puro. Para los que buscan energía sin concesiones.",
                10.0, 4.5, hotDrinks);
        edible("Capuchino", "Cubone",
                "Espresso con leche vaporizada y espuma cremosa en perfecta armonía. Un clásico irresistible.",
                10.0, 4.5, hotDrinks);
        edible("Submarino Blanco", "Dewgong",
                "Chocolate blanco fundido en leche caliente vaporizada. Dulce, cremoso y reconfortante.",
                11.0, 5.0, hotDrinks);
        edible("Submarino Oscuro", "Kangaskhan",
                "Chocolate negro fundido en leche caliente vaporizada. Intenso, profundo y reconfortante.",
                11.0, 5.0, hotDrinks);

        // Bebidas Frias
        edible("Iced Durazno y Manzana", "Charizard",
                "Refrescante mezcla de hielo, durazno en almíbar, sidra de manzana y azúcar. Dulce y frutal.",
                12.0, 4.0, icedDrinks);
        edible("Iced Durazno y Chocolate", "Raichu",
                "Bebida helada con durazno dulce y suave toque de chocolate. Una combinación sorprendentemente deliciosa.",
                12.0, 4.0, icedDrinks);
        edible("Iced Frutilla, Granada y Leche", "Jigglypuff",
                "Dulce y cremosa mezcla helada de frutilla, granada y leche. Rosada, frutal y deliciosa.",
                12.0, 4.0, icedDrinks);
        edible("Iced Piña y Chocolate Blanco", "Psyduck",
                "Tropical y cremoso: piña fresca con chocolate blanco sobre hielo. Una combinación única e irresistible.",
                12.0, 4.0, icedDrinks);
        edible("Iced Piña y Granada", "Pikachu",
                "Explosiva y refrescante mezcla de piña tropical y granada sobre hielo. Energizante y frutal.",
                12.0, 4.0, icedDrinks);
        edible("Iced Oreo y Leche", "Dugtrio",
                "Cremosa bebida helada de leche con galletas Oreo trituradas. Dulce, espesa e irresistible.",
                12.0, 6.0, icedDrinks);
        edible("Iced Vainilla y Café Espresso", "Alakazam",
                "Espresso frío con vainilla cremosa sobre hielo. La combinación perfecta de intensidad y dulzura.",
                12.0, 5.0, icedDrinks);

        // Llaveros
        inedible("Llavero Pikachu",   10.0, 5.0, 15, 3, keychains);
        inedible("Llavero Charmander",10.0, 5.0, 12, 3, keychains);
        inedible("Llavero Bulbasaur", 10.0, 5.0, 10, 3, keychains);
        inedible("Llavero Squirtle",  10.0, 5.0,  8, 3, keychains);
        inedible("Llavero Meowth",    10.0, 5.0, 14, 3, keychains);
        inedible("Llavero Snorlax",   10.0, 5.0, 11, 3, keychains);
        inedible("Llavero Psyduck",   10.0, 5.0,  9, 3, keychains);
        inedible("Llavero Eevee",     15.0, 7.0,  7, 2, keychains);
        inedible("Llavero Mewtwo",    15.0, 7.0,  5, 2, keychains);
        inedible("Llavero Gengar",    15.0, 7.0,  6, 2, keychains);
        inedible("Llavero Jolteon",   15.0, 7.0,  8, 2, keychains);
        inedible("Llavero Flareon",   15.0, 7.0,  7, 2, keychains);
        inedible("Llavero Vaporeon",  15.0, 7.0,  6, 2, keychains);
        inedible("Llavero Gyarados",  15.0, 7.0,  5, 2, keychains);
    }

    private void edible(String name, String pokeName, String description, double price, double cost, Long categoryId) {
        if (productRepository.existsByName(name)) return;
        Edible e = new Edible();
        e.setName(name);
        e.setPokeName(pokeName);
        e.setDescription(description);
        e.setPrice(price);
        e.setCost(cost);
        Category cat = new Category();
        cat.setId(categoryId);
        e.setCategory(cat);
        try {
            productService.create(e);
            log.info("Producto creado: {}", name);
        } catch (Exception ex) {
            log.warn("No se pudo crear '{}': {}", name, ex.getMessage());
        }
    }

    private void inedible(String name, double price, double cost, int stock, int minStock, Long categoryId) {
        if (productRepository.existsByName(name)) return;
        Inedible i = new Inedible();
        i.setName(name);
        i.setPrice(price);
        i.setCost(cost);
        i.setStock(stock);
        i.setMinStock(minStock);
        Category cat = new Category();
        cat.setId(categoryId);
        i.setCategory(cat);
        try {
            productService.create(i);
            log.info("Producto creado: {}", name);
        } catch (Exception ex) {
            log.warn("No se pudo crear '{}': {}", name, ex.getMessage());
        }
    }
}
