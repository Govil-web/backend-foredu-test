package com.foroescolar.controllers.asistencia;

import com.foroescolar.config.security.SecurityService;
import com.foroescolar.dtos.ApiResponseDto;
import com.foroescolar.dtos.asistencia.AsistenciaDTO;
import com.foroescolar.dtos.asistencia.AsistenciaRequest;
import com.foroescolar.dtos.asistencia.AsistenciaRequestDto;
import com.foroescolar.dtos.asistencia.DetalleAsistenciaByAlumno;
import com.foroescolar.dtos.user.UserResponseDTO;
import com.foroescolar.exceptions.ApplicationException;
import com.foroescolar.exceptions.model.EntityNotFoundException;
import com.foroescolar.services.AsistenciaService;
import com.foroescolar.services.UserService;
import com.foroescolar.utils.ApiResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("api/asistencia")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;
    private final UserService userService;
    private final SecurityService securityService;

    @Autowired
    public AsistenciaController(
            AsistenciaService asistenciaService,
            UserService userService,
            SecurityService securityService) {
        this.asistenciaService = asistenciaService;
        this.userService = userService;
        this.securityService = securityService;
    }

    @PostMapping("/add")
    @Operation(summary = "Register a new asistencia")
    public ResponseEntity<ApiResponseDto<String>> addAsistencia(@RequestBody AsistenciaRequest asistenciaRequest) {

            try {
                UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                UserResponseDTO user = userService.findByEmail(userDetails.getUsername());

                if (!securityService.canManageGradeAttendance(user.id(), asistenciaRequest.getGradoId())) {
                    return ApiResponseUtils.forbidden("No tienes permiso para registrar asistencia en este grado");
                }

                asistenciaService.asistenciaDelDia(asistenciaRequest);
                return ApiResponseUtils.success("Success", "Asistencia guardada exitosamente");
            } catch (EntityNotFoundException e) {
                return ApiResponseUtils.error("Error al registrar asistencia: " + e.getMessage());
            }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get asistencia by id")
    public ResponseEntity<ApiResponseDto<AsistenciaDTO>> getAsistenciaById(@PathVariable Long id) {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponseDTO user = userService.findByEmail(userDetails.getUsername());

            if (!securityService.canViewAttendance(user.id(), id)) {
                return ApiResponseUtils.forbidden("No tienes permiso para ver esta asistencia");
            }

            return asistenciaService.findById(id)
                    .map(asistencia -> ResponseEntity.ok(new ApiResponseDto<>(true, "Asistencia encontrada", asistencia)))
                    .orElseGet(() -> ApiResponseUtils.forbidden("Asistencia no encontrada"));
        } catch (Exception e) {
            return ApiResponseUtils.error("Error al obtener asistencia: " + e.getMessage());
        }
    }

    @GetMapping("/grado/{id}")
    @Operation(summary = "Get asistencia by grado")
    public ResponseEntity<ApiResponseDto<AsistenciaDTO>> getAsistenciaByGrado(@PathVariable Long id) {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponseDTO user = userService.findByEmail(userDetails.getUsername());

            if (!securityService.canViewGradeAttendance(user.id(), id)) {
                return ApiResponseUtils.forbidden("No tienes permiso para ver las asistencias de este grado");
            }

            Iterable<AsistenciaDTO> listarAsistencias = asistenciaService.getAsistenciasByGrado(id);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "Asistencias encontradas", listarAsistencias));
        } catch (Exception e) {
            return ApiResponseUtils.badRequest("Error obtener asistencias: " + e.getMessage());
        }
    }

    @GetMapping("/getAll")
   // @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    @Operation(summary = "List all asistencias")
    public ResponseEntity<ApiResponseDto<AsistenciaDTO>> asistenciasList() {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponseDTO user = userService.findByEmail(userDetails.getUsername());

            if (securityService.isAdmin(user.id())) {
                return ApiResponseUtils.forbidden("No tienes permiso para ver todas las asistencias");
            }

            Iterable<AsistenciaDTO> listarAsistencias = asistenciaService.findAll();
            return ResponseEntity.ok(new ApiResponseDto<>(true, "Lista de asistencias", listarAsistencias));
        } catch (Exception e) {
            return ApiResponseUtils.error("Error al obtener asistencias: " + e.getMessage());
        }
    }

    @GetMapping("/fechaAndGrado/{id}")
    @Operation(summary = "List all asistencias for a specific date and grade")
    public ResponseEntity<ApiResponseDto<AsistenciaDTO>> getAsistenciasByDateAndGrado(
            @PathVariable Long id,
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponseDTO user = userService.findByEmail(userDetails.getUsername());


            if (!securityService.canViewGradeAttendance(user.id(), id)) {
                return ApiResponseUtils.forbidden("No tienes permiso para ver las asistencias de este grado");
            }

            Iterable<AsistenciaDTO> listarAsistencias = asistenciaService.getByFechaBeetweenAndGrado(id, fechaInicio, fechaFin);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "Asistencias encontradas", listarAsistencias));
        } catch (ApplicationException e) {
            return ApiResponseUtils.badRequest("Error al obtener asistencias: " + e.getMessage());
        }
    }

    @PatchMapping("/update")
    @Operation(summary = "Update asistencia", description = "Solo se necesita ID de la asistencia,justificativo y estado")
    public ResponseEntity<ApiResponseDto<AsistenciaDTO>> updateAsistencia(
            @RequestBody AsistenciaRequestDto asistenciaDTO) {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponseDTO user = userService.findByEmail(userDetails.getUsername());

            if (!securityService.canUpdateAttendance(user.id(), asistenciaDTO.getGrado())) {
                return ApiResponseUtils.forbidden("No tienes permiso para actualizar esta asistencia");
            }

            asistenciaService.update(asistenciaDTO);
            return ApiResponseUtils.success(null, "Asistencia actualizada exitosamente");
        } catch (ApplicationException e) {
            return ApiResponseUtils.badRequest("Error al actualizar asistencia: " + e.getMessage());
        }
    }

    @GetMapping("/detailsByStudent/{id}")
    @Operation(summary = "Obtiene el detalle de asistencias global del alumno en el grado")
    public ResponseEntity<ApiResponseDto<DetalleAsistenciaByAlumno>> detailsByStudent(@PathVariable Long id){

        try{
            Optional<DetalleAsistenciaByAlumno> response= asistenciaService.getDetailsByStudent(id);
            return response.map(detalleAsistenciaByAlumno ->
                    ApiResponseUtils.success(detalleAsistenciaByAlumno, "Busqueda exitosa"))
                    .orElseGet(() -> ApiResponseUtils.forbidden("No tienes permiso para actualizar esta asistencia"));

        }catch (Exception e){

            return ApiResponseUtils.internalError("Ha ocurrido un error: "+ e.getMessage());

        }




    }

}