package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Monto pagado en esta quincena (puede diferir del salary base por bonos/descuentos)
    @Column(nullable = false)
    private Double amount;

    // Fecha en que se hizo el pago
    @Column(nullable = false)
    private LocalDate paymentDate;

    // Inicio del período quincenal cubierto
    @Column(nullable = false)
    private LocalDate periodStart;

    // Fin del período quincenal cubierto
    @Column(nullable = false)
    private LocalDate periodEnd;

    @Column(columnDefinition = "TEXT")
    private String note;

    // Empleado al que se le hizo el pago (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;
}
