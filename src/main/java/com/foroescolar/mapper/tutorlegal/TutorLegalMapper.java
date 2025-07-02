package com.foroescolar.mapper.tutorlegal;

import com.foroescolar.dtos.tutorlegal.TutorLegalRequestDTO;
import com.foroescolar.dtos.tutorlegal.TutorLegalResponseDTO;
import com.foroescolar.model.Estudiante;
import com.foroescolar.model.Institucion;
import com.foroescolar.model.TutorLegal;
import com.foroescolar.repository.EstudianteRepository;
import com.foroescolar.repository.InstitucionRepository;
import com.foroescolar.repository.TutorLegalRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {TutorLegalRepository.class, EstudianteRepository.class})
public abstract class TutorLegalMapper {

    @Autowired
    private EstudianteRepository estudianteRepository;
    @Autowired
    private InstitucionRepository institucionRepository;




    @Mapping(source = "estudiante", target = "estudiante", qualifiedByName = "longListToEstudiante")
    @Mapping(source = "institucionId", target = "institucion", qualifiedByName = "longToInstitucion")
    public abstract TutorLegal toEntity(TutorLegalRequestDTO tutorLegalRequestDTO);

    @Mapping(source = "estudiante", target = "estudiante", qualifiedByName = "estudianteToLongList")
    @Mapping(source = "institucion.nombre", target = "institucion")
    public abstract TutorLegalResponseDTO toResponseDTO(TutorLegal tutorLegal);

    @Named("longToInstitucion")
    protected Institucion longToInstitucion(Long institucionId) {
        return institucionRepository.findById(institucionId).orElse(null);
    }

    @Named("estudianteToLongList")
    public List<Long> estudianteToLongList(List<Estudiante> estudiantes) {
        return estudiantes.stream()
                .filter(estudiante -> estudiante != null && estudiante.getId() != null)
                .map(Estudiante::getId)
                .collect(Collectors.toList());
    }

    @Named("longListToEstudiante")
    protected List<Estudiante> longListToEstudiante(List<Long> ids) {
        return ids != null ? ids.stream().map(id -> estudianteRepository.findById(id).orElse(null)).collect(Collectors.toList()) : null;
    }
}