package com.foroescolar.services;

import com.foroescolar.dtos.asistencia.AsistenciaDTO;
import com.foroescolar.dtos.estudiante.*;
import com.foroescolar.model.Estudiante;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz de servicio para operaciones relacionadas con estudiantes
 */
public interface EstudianteService {

    /**
     * Obtiene todos los estudiantes como DTOs de perfil
     * @return Lista de DTOs con información de perfil de estudiantes
     */
    List<EstudiantePerfilDto> findAllStudents();

    /**
     * Obtiene todos los estudiantes con paginación
     * @param pagina Número de página (desde 0)
     * @param tamano Tamaño de la página
     * @return Página de DTOs con información básica de estudiantes
     */
    Page<EstudianteListaDTO> obtenerTodosPaginados(int pagina, int tamano);

    /**
     * Obtiene los detalles completos de un estudiante por ID
     * @param id ID del estudiante
     * @return DTO con detalles completos o empty si no existe
     */
    Optional<EstudianteDetalleDTO> obtenerDetallePorId(Long id);

    /**
     * Obtiene estudiantes por grado como ListaDTO
     * @param gradoId ID del grado
     * @return Lista de DTOs con información básica
     */
    List<EstudianteListaDTO> obtenerPorGrado(Long gradoId);

    /**
     * Obtiene estudiantes por grado como PerfilDto
     * @param gradoId ID del grado
     * @return Lista de DTOs con información de perfil
     */
    List<EstudiantePerfilDto> findByGradoId(Long gradoId);

    /**
     * Crea un nuevo estudiante
     * @param dto DTO con la información para la creación
     * @return DTO con los detalles del estudiante creado
     */
    EstudianteDetalleDTO crear(EstudianteCreacionDTO dto);

    /**
     * Actualiza un estudiante existente
     * @param dto DTO con la información para la actualización
     * @return DTO con los detalles del estudiante actualizado
     */
    EstudianteDetalleDTO actualizar(EstudianteActualizacionDTO dto);

    /**
     * Elimina un estudiante por ID
     * @param id ID del estudiante a eliminar
     */
    void eliminar(Long id);

    /**
     * Cambia el estado activo de un estudiante
     * @param id ID del estudiante
     * @return nuevo estado (true = activo, false = inactivo)
     */
    boolean cambiarEstadoActivo(Long id);

    /**
     * Obtiene las asistencias de un estudiante
     * @param estudianteId ID del estudiante
     * @return Lista de DTOs con información de asistencias
     */
    List<AsistenciaDTO> obtenerAsistencias(Long estudianteId);

    /**
     * Obtiene la entidad Estudiante por ID
     * @param id ID del estudiante
     * @return Entidad Estudiante
     * @throws com.foroescolar.exceptions.model.EntityNotFoundException si no existe
     */
    Estudiante obtenerEntidadPorId(Long id);

    /**
     * Obtiene múltiples entidades Estudiante por sus IDs
     * @param ids Lista de IDs
     * @return Lista de entidades Estudiante
     */
    List<Estudiante> obtenerEntidadesPorIds(List<Long> ids);

    /**
     * Obtiene un resumen de todos los estudiantes (optimizado)
     * @return Lista de DTOs con información resumida
     */
    List<EstudianteResumenDTO> obtenerTodosResumen();

    /**
     * Obtiene un resumen paginado de estudiantes (optimizado)
     * @param pagina Número de página (desde 0)
     * @param tamano Tamaño de la página
     * @return Página de DTOs con información resumida
     */
    Page<EstudianteResumenDTO> obtenerTodosResumenPaginados(int pagina, int tamano);

    /**
     * Obtiene un resumen de estudiantes por tutor (optimizado)
     * @param tutorId ID del tutor
     * @return Lista de DTOs con información resumida
     */
    List<EstudianteResumenDTO> obtenerResumenPorTutor(Long tutorId);

    /**
     * Obtiene la entidad Estudiante por ID (alias para obtenerEntidadPorId)
     * @param estudianteId ID del estudiante
     * @return Entidad Estudiante
     * @throws com.foroescolar.exceptions.model.EntityNotFoundException si no existe
     */
    Estudiante findByIdToEntity(Long estudianteId);
}