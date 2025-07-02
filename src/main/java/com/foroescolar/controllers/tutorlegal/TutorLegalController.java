package com.foroescolar.controllers.tutorlegal;

import com.foroescolar.config.security.SecurityService;
import com.foroescolar.dtos.ApiResponseDto;
import com.foroescolar.dtos.asistencia.AsistenciaDTO;
import com.foroescolar.dtos.tutorlegal.TutorLegalRequestDTO;
import com.foroescolar.dtos.tutorlegal.TutorLegalResponseDTO;
import com.foroescolar.dtos.user.UserResponseDTO;
import com.foroescolar.exceptions.ApplicationException;
import com.foroescolar.exceptions.model.DniDuplicadoException;
import com.foroescolar.services.TutorLegalService;
import com.foroescolar.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/tutorlegal")
@Tag(name = "Tutor Legal", description = "Endpoints para gestión de tutores legales")
public class TutorLegalController {

    private final TutorLegalService tutorLegalService;
    private final UserService userService;
    private final SecurityService securityService;

    @Autowired
    public TutorLegalController(
            TutorLegalService tutorLegalService,
            UserService userService,
            SecurityService securityService) {
        this.tutorLegalService = tutorLegalService;
        this.userService = userService;
        this.securityService = securityService;
    }

    @GetMapping("/getAll")
    @Operation(summary = "Obtiene la lista de tutores legales")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDto<TutorLegalResponseDTO>> getAll() {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponseDTO user = userService.findByEmail(userDetails.getUsername());

            if (securityService.isAdmin(user.id())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponseDto<>(false, "Solo administradores pueden ver la lista completa de tutores", null));
            }

            Iterable<TutorLegalResponseDTO> tutores = tutorLegalService.findAll();
            return ResponseEntity.ok(new ApiResponseDto<>(true, "Lista de tutores obtenida exitosamente", tutores));
        } catch (ApplicationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(false, "Error al obtener la lista de tutores: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un tutor legal por su ID")
    public ResponseEntity<ApiResponseDto<TutorLegalResponseDTO>> getById(@PathVariable Long id) {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            UserResponseDTO currentUser = userService.findByEmail(userDetails.getUsername());

            // Verificar permisos
            if (!securityService.canAccessTutorInfo(currentUser.id(), id)) {
                log.warn("Intento de acceso no autorizado al tutor ID: {} por usuario ID: {}",
                        id, currentUser.id());
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponseDto<>(
                                false,
                                "No tiene permisos para acceder a esta información",
                                null
                        ));
            }

            // Buscar el tutor
            return tutorLegalService.findById(id)
                    .map(tutor -> ResponseEntity.ok(
                            new ApiResponseDto<>(true, "Tutor encontrado exitosamente", tutor)
                    ))
                    .orElseGet(() -> ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponseDto<>(
                                    false,
                                    "No se encontró el tutor solicitado",
                                    null
                            )));

        } catch (UsernameNotFoundException e) {
            log.error("Error de autenticación al acceder al tutor ID: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDto<>(
                            false,
                            "Error de autenticación",
                            null
                    ));
        } catch (Exception e) {
            log.error("Error al obtener el tutor ID: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(
                            false,
                            "Error interno del servidor",
                            null
                    ));
        }
    }

    @GetMapping("/asistenciaHijo/{idGrado}")
    @Operation(summary = "Obtiene las asistencias del estudiante por grado")
    public ResponseEntity<ApiResponseDto<AsistenciaDTO>> getAsistenciaHijo(@PathVariable Long idGrado) {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponseDTO user = userService.findByEmail(userDetails.getUsername());

            if (securityService.canViewGradeAttendance(user.id(), idGrado)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponseDto<>(false, "No tiene permisos para ver estas asistencias", null));
            }

            Iterable<AsistenciaDTO> asistencias = tutorLegalService.findAsistenciasByEstudianteId(user.id(), idGrado);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "Asistencias encontradas", asistencias));
        } catch (ApplicationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(false, "Error al obtener las asistencias: " + e.getMessage(), null));
        }
    }

    @PostMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra un nuevo tutor legal")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDto<TutorLegalResponseDTO>> registerTutorLegal(
            @RequestBody @Valid TutorLegalRequestDTO tutorLegalRequestDTO) {
        try {
//            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//            UserResponseDTO user = userService.findByEmail(userDetails.getUsername());
//
//            if (!securityService.isAdmin(user.id())) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body(new ApiResponseDto<>(false, "Solo administradores pueden registrar tutores", null));
//            }

            TutorLegalResponseDTO tutorLegal = tutorLegalService.save(tutorLegalRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponseDto<>(true, "Tutor legal registrado exitosamente", tutorLegal));
        } catch (DniDuplicadoException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(false, "Error al registrar tutor: " + e.getMessage(), null));
        }
    }

    @PutMapping("/update")
    @Operation(summary = "Actualiza un tutor legal")
    public ResponseEntity<ApiResponseDto<TutorLegalResponseDTO>> update(
            @RequestBody @Valid TutorLegalRequestDTO tutorLegalRequestDTO) {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponseDTO user = userService.findByEmail(userDetails.getUsername());

            if (!securityService.canUpdateTutor(user.id(), tutorLegalRequestDTO.id())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponseDto<>(false, "No tiene permisos para actualizar este tutor", null));
            }

            TutorLegalResponseDTO tutor = tutorLegalService.update(tutorLegalRequestDTO);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "Tutor actualizado exitosamente", tutor));
        } catch (ApplicationException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(false, "Error al actualizar tutor: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un tutor legal")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable Long id) {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponseDTO user = userService.findByEmail(userDetails.getUsername());

            // Verificar si es administrador
            if (securityService.isAdmin(user.id())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponseDto<>(false, "Solo administradores pueden eliminar tutores", null));
            }

            // Verificar si tiene estudiantes asociados
            if (tutorLegalService.hasActiveStudents(id)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDto<>(false, "No se puede eliminar un tutor con estudiantes activos", null));
            }

            tutorLegalService.deleteById(id);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "Tutor eliminado exitosamente", null));
        } catch (ApplicationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(false, "Error al eliminar tutor: " + e.getMessage(), null));
        }
    }
}