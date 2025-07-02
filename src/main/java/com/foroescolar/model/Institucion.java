package com.foroescolar.model;

import com.foroescolar.enums.NivelEducativo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "institucion")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Institucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private String logo;
    private String identificacion;
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_educativo")
    private NivelEducativo nivelEducativo;


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "institucion", orphanRemoval = true)
    private List<Grado> grados;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "institucion" , orphanRemoval = true)
    private List<User> users;
}
