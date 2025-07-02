package com.foroescolar.services.impl;

import com.foroescolar.dtos.asistencia.AsistenciaDTO;
import com.foroescolar.dtos.asistencia.AsistenciaRequest;
import com.foroescolar.dtos.asistencia.AsistenciaRequestDto;
import com.foroescolar.dtos.asistencia.DetalleAsistenciaByAlumno;
import com.foroescolar.enums.EstadoAsistencia;
import com.foroescolar.exceptions.model.EntityNotFoundException;
import com.foroescolar.mapper.asistencia.AsistenciaMapper;
import com.foroescolar.model.Asistencia;
import com.foroescolar.model.Estudiante;
import com.foroescolar.model.Fecha;
import com.foroescolar.model.Grado;
import com.foroescolar.repository.AsistenciaRepository;
import com.foroescolar.repository.EstudianteRepository;
import com.foroescolar.repository.GradoRepository;
import com.foroescolar.services.AsistenciaService;
import com.foroescolar.services.EstudianteService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AsistenciaServiceImpl implements AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final GradoRepository gradoRepository;
    private final EstudianteRepository estudianteRepository;
    private final AsistenciaMapper asistenciaMapper;
    private final FechaService fechaService;
    private final EstudianteService estudianteService;

    private static final String GRADO_NO_ENCONTRADO = "Grado no encontrado";

    @Autowired
    public AsistenciaServiceImpl(AsistenciaRepository asistenciaRepository, AsistenciaMapper asistenciaMapper,
                                 GradoRepository gradoRepository, EstudianteRepository estudianteRepository, FechaService fechaService, EstudianteService estudianteService) {
        this.asistenciaRepository = asistenciaRepository;
        this.asistenciaMapper = asistenciaMapper;
        this.gradoRepository = gradoRepository;
        this.estudianteRepository = estudianteRepository;
        this.fechaService = fechaService;
        this.estudianteService = estudianteService;
    }

    @Transactional
    @Override
    public void asistenciaDelDia(AsistenciaRequest request) {

        Map<Long, String> response= request.getAsistencia();


        LocalDate fechaActual= LocalDate.now();
        Grado grado = gradoRepository.findById(request.getGradoId()).orElse(null);
        if (grado == null) {
            throw new EntityNotFoundException(GRADO_NO_ENCONTRADO);
        }
        if (asistenciaRepository.existsByFechaFechaAndGradoId(fechaActual, grado.getId())){
            throw new EntityNotFoundException("Ya se ha pasado la asistencia anteriormente");
        }
        Fecha fecha = fechaService.findByFecha(fechaActual);
        if (fecha == null) {
            fecha= fechaService.save(fechaActual);
        }
        grado.incrementarContador();
        List<Estudiante> estudiantes= new ArrayList<>();

        for(Map.Entry<Long,String> entry: response.entrySet()) {
            Long estudianteId = entry.getKey();
            EstadoAsistencia estado = EstadoAsistencia.valueOf(entry.getValue());

            // Obtener el estudiante correspondiente
            Estudiante estudiante = estudianteService.findByIdToEntity(estudianteId);

            if (estudiante.getGrado().getId() == grado.getId()) {

                // Crear la asistencia para el estudiante
                Asistencia asistencia = new Asistencia();
                asistencia.setFecha(fecha); // Fecha actual
                asistencia.setEstudiante(estudiante);
                asistencia.setGrado(grado);
                asistencia.setEstado(estado);

                asistenciaRepository.save(asistencia);
            } else {
                estudiantes.add(estudiante);
            }
        }
        if (!estudiantes.isEmpty()) {
            StringBuilder mensajeError = new StringBuilder("Los estudiantes con ID: ");
            for (Estudiante estudiante : estudiantes) {
                mensajeError.append(estudiante.getId()).append(", ");
            }
            mensajeError.delete(mensajeError.length() - 2, mensajeError.length()); // eliminar la última coma y espacio
            mensajeError.append(" no pertenecen al grado");
            throw new EntityNotFoundException(mensajeError.toString());
        }
    }
    @Override
    @Transactional
    public void update(AsistenciaRequestDto requestDto) {

        Optional<Asistencia> response = asistenciaRepository.findById(requestDto.getId());
        if (response.isPresent()) {
            Asistencia asistencia= response.get();
            if("JUSTIFICADO".equals(requestDto.getEstado())|| "TARDE".equals(requestDto.getEstado())){
                asistencia.setEstado(EstadoAsistencia.valueOf(requestDto.getEstado()));
                asistencia.setObservaciones(requestDto.getJustificativos());
            }

            asistenciaRepository.save(asistencia);
        } else{
            throw new EntityNotFoundException("No se puede cambiar el estado de la asistencia");
        }
    }
// metodo vacio por implentacion del serviceGeneric en este caso se desestima
    @Override
    public AsistenciaDTO save(AsistenciaDTO requestDTO) {
        return null;
    }

    @Override
    public Optional<AsistenciaDTO> findById(Long id) {
        Optional<Asistencia> asistenciaDTO = asistenciaRepository.findById(id);

        if (asistenciaDTO.isPresent()) {
            return asistenciaDTO.map(asistenciaMapper::toResponseDto);
        } else {
            throw new EntityNotFoundException("Asistencia no encontrada: " + id);
        }
    }

    @Override
    public Iterable<AsistenciaDTO> findAll() {
        return asistenciaRepository.findAll().stream()
                .map(asistenciaMapper::toResponseDto)
                .toList();
    }

    @Override
    public void deleteById(Long asistenciaId) {
        asistenciaRepository.deleteById(asistenciaId);
    }

    // --------------------------- CONSULTAS ---------------------------
    @Override
    public Iterable<AsistenciaDTO> getAsistenciasByEstudianteID(Long estudianteId) {
        List<Asistencia> asistencias = asistenciaRepository.findByEstudianteId(estudianteId);
        return asistencias.stream().map(asistenciaMapper::toResponseDto)
                .toList();
    }
    @Override
    public Iterable<AsistenciaDTO> getAsistenciasByGradoAndEstudiante(Long tutorId, Long gradoId) {

        Optional<Grado> grado = gradoRepository.findById(gradoId);
        if (grado.isEmpty()) {
            throw new EntityNotFoundException(GRADO_NO_ENCONTRADO);
        }
        List<Estudiante> estudiantes = estudianteRepository.findByTutorId(tutorId);
        return estudiantes.stream().
                flatMap(estudiante -> asistenciaRepository.findByEstudianteIdAndGradoId(estudiante.getId(), gradoId).stream())
                .map(asistenciaMapper::toResponseDto)
                .toList();
    }

    @Override
    public Iterable<AsistenciaDTO> getAsistenciasByGrado(Long gradoId) {

        Optional<Grado> grado = gradoRepository.findById(gradoId);
        if (grado.isPresent()) {
            List<Asistencia> asistencias = asistenciaRepository.findByGradoId(gradoId);
            return asistencias.stream()
                    .map(asistenciaMapper::toResponseDto).toList();
        }
        throw new EntityNotFoundException(GRADO_NO_ENCONTRADO);
    }

    @Override
    public List<AsistenciaDTO> getByFechaBeetweenAndGrado(Long gradoId, LocalDate fechaDesde, LocalDate fechaHasta) {

        Optional<Grado> grado = gradoRepository.findById(gradoId);
        if (grado.isEmpty()) {
            throw new EntityNotFoundException(GRADO_NO_ENCONTRADO);
        }
        List<Asistencia> asistencias = asistenciaRepository.findByFechaFechaBetweenAndGradoId(fechaDesde, fechaHasta, gradoId);

        if (asistencias.isEmpty()) {
            throw new EntityNotFoundException("No se encontraron asistencias en el rango de fechas");
        }
        return asistencias.stream().map(asistenciaMapper::toResponseDto)
                .toList();
    }

    @Override
    public Optional<DetalleAsistenciaByAlumno> getDetailsByStudent(Long estudianteId) {
        return estudianteRepository.findById(estudianteId)
                .map(estudiante -> {
                    int totalClases = estudiante.getGrado().getContador();
                    DetalleAsistenciaByAlumno detalleAsistencia = new DetalleAsistenciaByAlumno();
                    detalleAsistencia.setNombreEstudiante(estudiante.getNombre());
                    detalleAsistencia.setIdEstudiante(estudianteId);
                    detalleAsistencia.setAsistenciasPresente(asistenciaRepository.countByEstudianteIdAndEstado(estudianteId, EstadoAsistencia.PRESENTE));
                    detalleAsistencia.setAsistenciasAusente(asistenciaRepository.countByEstudianteIdAndEstado(estudianteId, EstadoAsistencia.AUSENTE));
                    detalleAsistencia.setAsistenciasTarde(asistenciaRepository.countByEstudianteIdAndEstado(estudianteId, EstadoAsistencia.TARDE));
                    detalleAsistencia.setAsistenciasJustificadas(asistenciaRepository.countByEstudianteIdAndEstado(estudianteId, EstadoAsistencia.JUSTIFICADO));
                    detalleAsistencia.setGrado(estudiante.getGrado().getCurso() + " " + estudiante.getGrado().getAula());
                    detalleAsistencia.setClasesVistasDelGrado(totalClases);

                    int clasesAsistidas= detalleAsistencia.getAsistenciasPresente()+detalleAsistencia.getAsistenciasJustificadas();

                    detalleAsistencia.setPorcentajeDeAsistencias(calcularPorcentajeDeCLases(totalClases, clasesAsistidas));
                    return detalleAsistencia;
                });
    }

    protected Double calcularPorcentajeDeCLases(int contador, int totalAsistencias){

        return (double) (totalAsistencias * 100 )/ contador;

    }

}
