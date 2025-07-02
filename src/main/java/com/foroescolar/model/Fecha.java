package com.foroescolar.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter@Setter
@Table(name = "fecha")
@AllArgsConstructor
@NoArgsConstructor
public class Fecha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private int mes;

    @Column(nullable = false)
    private int dia;

    @Column(nullable = false)
    private int trimestre;

    @Column(nullable = false)
    private int semana;

   @OneToMany(mappedBy = "fecha", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Asistencia> asistencias;

   public Fecha(LocalDate fecha) {
        this.fecha = fecha;
        this.anio = fecha.getYear();
        this.mes = fecha.getMonthValue();
        this.dia = fecha.getDayOfMonth();
        this.trimestre = (mes - 1) / 3 + 1;
        this.semana = (dia - 1) / 7 + 1;
    }

}
