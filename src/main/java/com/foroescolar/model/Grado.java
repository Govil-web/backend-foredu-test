package com.foroescolar.model;

import com.foroescolar.enums.AulaEnum;
import com.foroescolar.enums.CursoEnum;
import com.foroescolar.enums.MateriaEnum;
import com.foroescolar.enums.TurnoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Grado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private AulaEnum aula;
    @Enumerated(value = EnumType.STRING)
    private CursoEnum curso;
    @Enumerated(value = EnumType.STRING)
    private TurnoEnum turno;
    @Enumerated(value = EnumType.STRING)
    private MateriaEnum materia;
    private int contador= 0;

    @ManyToOne
    @JoinColumn(name="profesor_id")
    private Profesor profesor;

    @ManyToOne
    @JoinColumn(name="institucion_id", nullable = false)
    private Institucion institucion;

    @OneToMany(mappedBy = "grado",cascade = {CascadeType.ALL},orphanRemoval = true,fetch = FetchType.LAZY)
    private List<Estudiante> estudiantes;

    @OneToMany(mappedBy = "grado", cascade = {CascadeType.ALL},orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Asistencia> asistencias;

    public void incrementarContador(){
        this.contador++;
    }


}