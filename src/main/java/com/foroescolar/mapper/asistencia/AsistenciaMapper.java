package com.foroescolar.mapper.asistencia;

import com.foroescolar.dtos.asistencia.AsistenciaDTO;
import com.foroescolar.dtos.asistencia.AsistenciaRequestDto;
import com.foroescolar.enums.EstadoAsistencia;
import com.foroescolar.model.Asistencia;
import com.foroescolar.model.Estudiante;
import com.foroescolar.model.Grado;
import com.foroescolar.repository.AsistenciaRepository;
import com.foroescolar.repository.EstudianteRepository;
import com.foroescolar.repository.GradoRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class AsistenciaMapper {

    @Autowired
    private EstudianteRepository estudianteRepository;
    @Autowired
    private AsistenciaRepository asistenciaRepository;
    @Autowired
    private GradoRepository gradoRepository;


    @Mapping(source = "estudiante", target = "estudiante", qualifiedByName = "longToEstudiante")
    @Mapping(source = "grado", target = "grado", qualifiedByName = "longToGrado")
    @Mapping(source = "justificativos", target = "observaciones")
    public abstract Asistencia toEntity(AsistenciaRequestDto asistenciaRquestDto);


    @Mapping(source = "id", target = "id")
    @Mapping(source = "estudiante.id", target = "porcentajeAsistencia", qualifiedByName = "calcularPorcentaje")
    @Mapping(source = "observaciones", target = "justificativos")
    @Mapping(source = "estudiante", target = "nombreEstudiante", qualifiedByName = "estudianteName")
    @Mapping(source = "estudiante", target = "estudiante", qualifiedByName = "estudianteToLong")
    @Mapping(source = "grado", target = "grado", qualifiedByName = "gradoToLong")
    @Mapping(source = "fecha.fecha", target = "fecha")
    public abstract AsistenciaDTO toResponseDto(Asistencia asistencia);


    @Named("longToEstudiante")
    protected Estudiante longToEstudiante(Long id) {
        return id != null ? estudianteRepository.findById(id).orElse(null) : null;
    }

    @Named("longToGrado")
    protected Grado longToGrado(Long id) {
        return id != null ? gradoRepository.findById(id).orElse(null) : null;
    }


    @Named("estudianteToLong")
    protected Long estudianteToLong(Estudiante estudiante) {
        return estudiante != null ? estudiante.getId() : null;
    }

    @Named("gradoToLong")
    protected Long gradoToLong(Grado grado) {
        return grado != null ? grado.getId() : null;
    }

    @Named("estudianteName")
    protected String estudianteName(Estudiante estudiante) {
        return estudiante != null ? estudiante.getNombre() + " " + estudiante.getApellido() : null;
    }
    @Named("calcularPorcentaje")
    protected Double calcularPorcentaje(Long id) {
        long diasAsistidos = asistenciaRepository.countByEstudianteIdAndEstado(id, EstadoAsistencia.PRESENTE);
        long totalClases = obtenerContadorGrado(id);
      return (double) (diasAsistidos * 100 )/ totalClases;
   }

    private long obtenerContadorGrado(Long estudianteId) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId).orElse(null);

        assert estudiante != null;
        return   estudiante.getGrado().getContador();
    }

}

