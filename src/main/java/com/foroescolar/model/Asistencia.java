package com.foroescolar.model;

import com.foroescolar.enums.EstadoAsistencia;
import jakarta.persistence.*;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "asistencia")
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String observaciones= "Sin observacion";

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoAsistencia estado;

    @ManyToOne
    @JoinColumn(name = "fecha_id")
    private Fecha fecha;

    @ManyToOne
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;

    @ManyToOne
    @JoinColumn(name = "grado_id")
    private Grado grado;

    public void actualizarContadorGrado(){
        if(this.grado!=null){
            this.grado.incrementarContador();
        }
    }

}