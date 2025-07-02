package com.foroescolar.controllers.estudiante;

import com.foroescolar.config.security.SecurityService;
import com.foroescolar.controllers.ApiResponse;
import com.foroescolar.dtos.ApiResponseDto;
import com.foroescolar.dtos.asistencia.AsistenciaDTO;
import com.foroescolar.dtos.estudiante.*;
import com.foroescolar.exceptions.model.ForbiddenException;
import com.foroescolar.services.EstudianteService;
import com.foroescolar.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/estudiante")
@Slf4j
@RequiredArgsConstructor
public class EstudianteController {

    private final EstudianteService estudianteService;
    private final SecurityService securityService;
    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene los detalles de un estudiante por ID")
    public ResponseEntity<ApiResponseDto<EstudianteDetalleDTO>> obtenerDetalle(@PathVariable Long id) {
        validarAcceso(id);
        return estudianteService.obtenerDetallePorId(id)
                .map(estudiante -> ApiResponse.success("Estudiante encontrado", estudiante))
                .orElse(ApiResponse.notFound("Estudiante no encontrado"));
    }

    @GetMapping
    @Operation(summary = "Obtiene todos los estudiantes con paginación")
    public ResponseEntity<ApiResponseDto<Page<EstudianteListaDTO>>> obtenerTodos(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        // Validar que es administrador para este endpoint
        validarAccesoAdmin();

        Page<EstudianteListaDTO> estudiantes = estudianteService.obtenerTodosPaginados(pagina, tamano);
        return ApiResponse.success("Estudiantes recuperados exitosamente", estudiantes);
    }

    @GetMapping("/getAll")
    @Operation(summary = "Obtiene todos los estudiantes sin paginación")
    public ResponseEntity<ApiResponseDto<List<EstudiantePerfilDto>>> getAllEstudiantes() {
        // Validar que es administrador para este endpoint
        validarAccesoAdmin();

        List<EstudiantePerfilDto> estudiantes = estudianteService.findAllStudents();
        return ApiResponse.success("Estudiantes recuperados exitosamente", estudiantes);
    }

    @PostMapping
    @Operation(summary = "Crear nuevo estudiante")
    public ResponseEntity<ApiResponseDto<EstudianteDetalleDTO>> crear(
            @Valid @RequestBody EstudianteCreacionDTO dto) {
        validarAccesoAdmin();

        EstudianteDetalleDTO estudiante = estudianteService.crear(dto);
        log.info("Estudiante creado. ID: {}", estudiante.id());
        return ApiResponse.created("Estudiante registrado exitosamente", estudiante);
    }

    @PutMapping
    @Operation(summary = "Actualiza un estudiante existente")
    public ResponseEntity<ApiResponseDto<EstudianteDetalleDTO>> actualizar(
            @Valid @RequestBody EstudianteActualizacionDTO dto) {
        // Si es admin, permitir actualizar cualquier estudiante
        if (securityService.isCurrentUserAdmin()) {
            validarAcceso(dto.id());
        }

        EstudianteDetalleDTO estudiante = estudianteService.actualizar(dto);
        return ApiResponse.success("Estudiante actualizado exitosamente", estudiante);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un estudiante")
    public ResponseEntity<ApiResponseDto<Void>> eliminar(@PathVariable Long id) {
        validarAccesoAdmin();

        estudianteService.eliminar(id);
        return ApiResponse.success("Estudiante eliminado exitosamente", null);
    }

    @GetMapping("/grado/{gradoId}")
    @Operation(summary = "Obtiene estudiantes por grado como lista DTO")
    public ResponseEntity<ApiResponseDto<List<EstudianteListaDTO>>> obtenerPorGrado(@PathVariable Long gradoId) {
        if (!securityService.canViewGradeAttendance(getCurrentUserId(), gradoId)) {
            return ApiResponse.forbidden("No tienes permisos para ver los estudiantes de este grado");
        }

        List<EstudianteListaDTO> estudiantes = estudianteService.obtenerPorGrado(gradoId);
        if (estudiantes.isEmpty()) {
            return ApiResponse.notFound("No hay estudiantes asignados al grado");
        }

        return ApiResponse.success("Estudiantes recuperados exitosamente", estudiantes);
    }

    @GetMapping("/filterGrado")
    @Operation(summary = "Obtiene estudiantes por grado como PerfilDto")
    public ResponseEntity<ApiResponseDto<List<EstudiantePerfilDto>>> filtroXGrado(@RequestParam("gradoId") Long gradoId) {
        Long userId = securityService.getCurrentUserId();

        if (!securityService.canViewGradeAttendance(userId, gradoId)) {
            return ApiResponse.forbidden("No tienes permisos para ver los estudiantes de este grado");
        }

        List<EstudiantePerfilDto> estudiantePerfilDtos = estudianteService.findByGradoId(gradoId);
        if (estudiantePerfilDtos.isEmpty()) {
            return ApiResponse.notFound("No hay estudiantes asignados al grado");
        }

        return ApiResponse.success("Estudiantes recuperados exitosamente", estudiantePerfilDtos);
    }

    @GetMapping("/{id}/asistencias")
    @Operation(summary = "Obtiene las asistencias de un estudiante")
    public ResponseEntity<ApiResponseDto<List<AsistenciaDTO>>> obtenerAsistencias(@PathVariable Long id) {
        // Si es administrador, permitir acceder a cualquier asistencia
        if (securityService.isCurrentUserAdmin()) {
            validarAcceso(id);
        }

        List<AsistenciaDTO> asistencias = estudianteService.obtenerAsistencias(id);
        if (asistencias.isEmpty()) {
            return ApiResponse.notFound("No hay asistencias para el estudiante");
        }

        return ApiResponse.success("Asistencias recuperadas exitosamente", asistencias);
    }

    @PostMapping("/{id}/activar")
    @Operation(summary = "Activa o desactiva un estudiante")
    public ResponseEntity<ApiResponseDto<Void>> cambiarEstadoActivo(@PathVariable Long id) {
        validarAccesoAdmin();

        boolean nuevoEstado = estudianteService.cambiarEstadoActivo(id);
        String mensaje = nuevoEstado ?
                "Estudiante activado exitosamente" :
                "Estudiante desactivado exitosamente";

        return ApiResponse.success(mensaje, null);
    }

    // Métodos auxiliares
    private Long getCurrentUserId() {
        return securityService.getCurrentUserId();
    }

    private void validarAcceso(Long recursoId) {
        if (!securityService.hasAccessToInformation(recursoId)) {
            throw new ForbiddenException("No tienes permisos para acceder a este recurso");
        }
    }

    private void validarAccesoAdmin() {
        if (securityService.isCurrentUserAdmin()) {
            throw new ForbiddenException("Solo los administradores pueden realizar esta operación");
        }
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponseDto<Void>> manejarExcepcionForbidden(ForbiddenException ex) {
        return ApiResponse.forbidden(ex.getMessage());
    }

    @GetMapping("/resumen")
    @Operation(summary = "Obtiene un resumen de todos los estudiantes")
    public ResponseEntity<ApiResponseDto<List<EstudianteResumenDTO>>> obtenerTodosResumen() {
        List<EstudianteResumenDTO> estudiantes = estudianteService.obtenerTodosResumen();
        return ApiResponse.success("Resumen de estudiantes recuperado exitosamente", estudiantes);
    }

    @GetMapping("/resumen/paginado")
    @Operation(summary = "Obtiene un resumen paginado de estudiantes")
    public ResponseEntity<ApiResponseDto<Page<EstudianteResumenDTO>>> obtenerResumenPaginado(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        Page<EstudianteResumenDTO> estudiantes = estudianteService.obtenerTodosResumenPaginados(pagina, tamano);
        return ApiResponse.success("Resumen de estudiantes recuperado exitosamente", estudiantes);
    }

    @GetMapping("/resumen/tutor/{tutorId}")
    @Operation(summary = "Obtiene un resumen de estudiantes por tutor")
    public ResponseEntity<ApiResponseDto<List<EstudianteResumenDTO>>> obtenerResumenPorTutor(
            @PathVariable Long tutorId) {
        List<EstudianteResumenDTO> estudiantes = estudianteService.obtenerResumenPorTutor(tutorId);
        return ApiResponse.success("Resumen de estudiantes por tutor recuperado exitosamente", estudiantes);
    }
}