package com.example.demo.config;

import com.example.demo.model.Client;
import com.example.demo.model.Order;
import com.example.demo.model.Order.OrderStatus;
import com.example.demo.model.OrderDetail;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Component
@org.springframework.core.annotation.Order(2)
@RequiredArgsConstructor
public class SalesInitializer implements ApplicationRunner {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    private static final Random RND = new Random(42);
    private static final LocalTime SHIFT_START = LocalTime.of(17, 30);
    private static final LocalTime SHIFT_END = LocalTime.of(22, 0);

    private static final String[] FIRST_NAMES = {
        "Carlos", "Maria", "Jose", "Ana", "Pedro", "Lucia", "Diego", "Sofia",
        "Miguel", "Carmen", "Rene", "Gabriela", "Pablo", "Elena", "Roberto",
        "Monica", "Luis", "Patricia", "Fernando", "Silvia", "Alvaro", "Ruth",
        "Hugo", "Lidia", "Mario", "Rosa", "Jorge", "Julia", "Vicente", "Teresa",
        "Alberto", "Roxana", "Raul", "Sandra", "Oscar", "Eva", "Juan", "Martha",
        "Felipe", "Gloria", "Ricardo", "Cecilia", "Daniel", "Nelly", "Marco",
        "Liliana", "Adrian", "Beatriz", "Christian", "Claudia"
    };

    private static final String[] LAST_NAMES = {
        "Mamani", "Condori", "Quispe", "Flores", "Choque", "Vargas", "Gutierrez",
        "Rojas", "Torrez", "Paredes", "Morales", "Herrera", "Rivas", "Claure",
        "Rocha", "Alberto", "Zeballos", "Antelo", "Justiniano", "Roca", "Balcazar",
        "Rivero", "Cortez", "Suarez", "Ramos", "Lopez", "Garcia", "Martinez",
        "Rodriguez", "Perez", "Sanchez", "Romero", "Cruz", "Miranda", "Delgado",
        "Castro", "Ortiz", "Mendoza", "Vega", "Salinas", "Aguilar", "Navarro",
        "Peña", "Guillen", "Valdez", "Cardenas", "Soliz", "Barrientos", "Villca",
        "Rivas"
    };

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (orderRepository.count() > 0) {
            log.info("Ya hay ordenes registradas. Se omite el seed.");
            return;
        }

        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            log.warn("No hay usuarios registrados. Se omite el seed de ventas.");
            return;
        }

        Map<String, Product> products = productRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Product::getName, p -> p, (a, b) -> a));

        if (products.isEmpty()) {
            log.warn("No hay productos en la BD. Se omite el seed de ventas.");
            return;
        }

        seedClients();
        List<Client> clients = clientRepository.findAll();

        String[] productNames = products.keySet().toArray(new String[0]);
        LocalDate start = LocalDate.now().minusMonths(4);
        LocalDate end = LocalDate.now();

        int orderCount = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }
            int ordersToday = 3 + RND.nextInt(6);
            for (int i = 0; i < ordersToday; i++) {
                int minutesOffset = RND.nextInt(270);
                LocalTime time = SHIFT_START.plusMinutes(minutesOffset);
                if (time.isAfter(SHIFT_END)) break;

                LocalDateTime dt = date.atTime(time);
                int table = 1 + RND.nextInt(8);
                String payment = randomPayment();
                Client client = RND.nextInt(100) < 45 ? clients.get(RND.nextInt(clients.size())) : null;

                int itemCount = 1 + RND.nextInt(4);
                List<String[]> items = new ArrayList<>();
                for (int j = 0; j < itemCount; j++) {
                    String pName = productNames[RND.nextInt(productNames.length)];
                    int qty = 1 + RND.nextInt(3);
                    items.add(new String[]{pName, String.valueOf(qty)});
                }

                User cashier = getCashierForDay(date, users);
                if (cashier == null) {
                    log.warn("No hay cajero asignado para {}. Saltando orden.", date.getDayOfWeek());
                    continue;
                }
                double total = createOrder(cashier, products, table, payment, dt, client, items);
                orderCount++;
            }
        }

        log.info("Seeder completado: {} ordenes generadas en {} dias habiles.", orderCount, countWeekdays(start, end));
    }

    // ─── Clients ──────────────────────────────────────────────────────────────

    private void seedClients() {
        if (clientRepository.count() > 0) return;

        List<String[]> used = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            String name1 = FIRST_NAMES[RND.nextInt(FIRST_NAMES.length)];
            String name2 = LAST_NAMES[RND.nextInt(LAST_NAMES.length)];
            String fullName = name1 + " " + name2;

            String nit = String.format("%010d", RND.nextInt(2_000_000_000));
            String ci = String.valueOf(1_000_000 + RND.nextInt(8_000_000));
            String phone = String.format("%s%07d", RND.nextBoolean() ? "6" : "7", RND.nextInt(10_000_000));
            String email = (name1 + "." + name2 + i + "@gmail.com").toLowerCase();

            Client c = new Client();
            c.setName(fullName);
            c.setNit(nit);
            c.setCi(ci);
            c.setPhone(phone);
            c.setEmail(email);
            clientRepository.save(c);
            used.add(new String[]{fullName, nit});
        }
        log.info("{} clientes creados.", used.size());
    }

    // ─── Orders ───────────────────────────────────────────────────────────────

    private double createOrder(User user, Map<String, Product> products,
                               int table, String payment, LocalDateTime dt,
                               Client client, List<String[]> items) {
        Order order = new Order();
        order.setTableNumber(table);
        order.setPaymentMethod(payment);
        order.setDateTime(dt);
        order.setStatus(OrderStatus.DELIVERED);
        order.setUser(user);
        order.setClient(client);

        List<OrderDetail> details = new ArrayList<>();
        double total = 0;

        for (String[] item : items) {
            String productName = item[0];
            int qty = Integer.parseInt(item[1]);
            Product product = products.get(productName);
            if (product == null) continue;

            OrderDetail detail = new OrderDetail();
            detail.setProduct(product);
            detail.setQuantity(qty);
            detail.setSubtotal(product.getPrice() * qty);
            detail.setOrder(order);
            details.add(detail);
            total += detail.getSubtotal();
        }

        if (details.isEmpty()) return 0;

        order.setDetails(details);
        order.setTotal(total);

        if ("EFECTIVO".equals(payment)) {
            double roundedUp = Math.ceil(total / 5.0) * 5.0;
            double paid = roundedUp + (RND.nextBoolean() ? 0 : 5.0);
            order.setAmountPaid(paid);
            order.setChange(paid - total);
        } else {
            order.setAmountPaid(total);
            order.setChange(0.0);
        }

        try {
            orderRepository.save(order);
            String name = user.getName() + " " + user.getLastname();
            orderRepository.setAuditUser(order.getId(), name);
        } catch (Exception e) {
            log.warn("Error al guardar pedido (mesa {}, {}): {}", table, dt, e.getMessage());
        }
        return total;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String randomPayment() {
        int r = RND.nextInt(100);
        if (r < 60) return "EFECTIVO";
        if (r < 85) return "QR";
        return "TARJETA";
    }

    private long countWeekdays(LocalDate start, LocalDate end) {
        long count = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY) {
                count++;
            }
        }
        return count;
    }

    private User getCashierForDay(LocalDate date, List<User> users) {
        DayOfWeek day = date.getDayOfWeek();
        String targetEmail;
        if (day == DayOfWeek.MONDAY || day == DayOfWeek.TUESDAY || day == DayOfWeek.WEDNESDAY) {
            targetEmail = "rassimiranda23@gmail.com";
        } else {
            targetEmail = "pokeplace889988@gmail.com";
        }
        return users.stream()
                .filter(u -> u.getEmail().equals(targetEmail))
                .findFirst()
                .orElse(null);
    }
}
