package com.foroescolar.controllers.grado;

import com.foroescolar.config.security.SecurityService;
import com.foroescolar.dtos.ApiResponseDto;
import com.foroescolar.dtos.grado.GradoDto;
import com.foroescolar.dtos.user.UserPrincipal;
import com.foroescolar.dtos.user.UserResponseDTO;
import com.foroescolar.exceptions.ApplicationException;
import com.foroescolar.services.GradoService;
import com.foroescolar.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.mapstruct.control.MappingControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/grado")
@Tag(name = "Grado", description = "Endpoints para gestión de grados")
public class GradoController {

    private final GradoService gradoService;
    private final UserService userService;
    private final SecurityService securityService;

    private static final String ROLE_ADMINISTRADOR = "ROLE_ADMINISTRADOR";
    private static final String GRADO_NO_ENCONTRADO = "Grado no encontrado";

    @Autowired
    public GradoController(GradoService gradoService,
                           UserService userService,
                           SecurityService securityService) {
        this.gradoService = gradoService;
        this.userService = userService;
        this.securityService = securityService;
    }

    @PostMapping("/register")
    @Operation(summary = "Registra un nuevo grado", description = "Solo los administradores pueden registrar grados")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDto<GradoDto>> registerGrado(@RequestBody @Valid GradoDto gradoDto) {
        try {
           // UserPrincipal user= securityService.getCurrentUser();
            UserPrincipal user = getCurrentUser();
            if (!securityService.isAdmin(user.id())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponseDto<>(false, "Solo los administradores pueden registrar grados", null));
            }

          //  GradoDto gradoCreado = gradoService.createGrado(gradoDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponseDto<>(true, "Grado creado exitosamente", gradoService.createGrado(gradoDto)));
        } catch (ApplicationException e) {
            throw new ApplicationException("", "Error al registrar grado: " , e.getHttpStatus());
        }
    }

    @GetMapping("/getAll")
    @Operation(summary = "Obtiene todos los grados", description = "Solo los administradores pueden ver todos los grados." +
            " Los profesores ven sus grados asignados y" +
            " Los tutores pueden ver los grados de sus hijos")
    public ResponseEntity<ApiResponseDto<GradoDto>> getAll() {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponseDTO user = userService.findByEmail(userDetails.getUsername());

            // El ADMINISTRADOR puede ver todos los grados
            // PROFESOR puede ver los grados asignados
            // TUTOR puede ver los grados donde tiene estudiantes
            Iterable<GradoDto> grados;
            switch (user.rol()) {
                case ROLE_ADMINISTRADOR -> grados = gradoService.findAll();
                case "ROLE_PROFESOR" -> grados = gradoService.findGradosByProfesorId(user.id());
                case "ROLE_TUTOR" -> grados = gradoService.findGradosByTutorId(user.id());
                default -> {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new ApiResponseDto<>(false, "No tiene permisos para ver los grados", null));
                }
            }

            return ResponseEntity.ok(new ApiResponseDto<>(true, "Grados encontrados", grados));
        } catch (ApplicationException e) {
            throw new ApplicationException("","Error al obtener grados: " , e.getHttpStatus());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un grado por Id", description = "Solo los administradores pueden ver los grados de otros usuarios")
    public ResponseEntity<ApiResponseDto<GradoDto>> getById(@PathVariable("id") Long id) {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponseDTO user = userService.findByEmail(userDetails.getUsername());

            // Verificar permisos para ver el grado específico
            if (!securityService.canViewGrade(user.id(), id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponseDto<>(false, "No tiene permisos para ver este grado", null));
            }

            Optional<GradoDto> response = gradoService.findById(id);
            return response.map(gradoDto -> ResponseEntity.ok(new ApiResponseDto<>(true, "Grado encontrado", gradoDto))).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(false, GRADO_NO_ENCONTRADO, null)));
        } catch (ApplicationException e) {
            throw new ApplicationException(null, "Error al obtener el grado: " , e.getHttpStatus());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un grado por Id", description = "Solo los administradores pueden eliminar grados")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDto<GradoDto>> deleteById(@PathVariable("id") Long id) {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponseDTO user = userService.findByEmail(userDetails.getUsername());

            if (!user.rol().equals(ROLE_ADMINISTRADOR)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponseDto<>(false, "Solo los administradores pueden eliminar grados", null));
            }

            Optional<GradoDto> gradoToDelete = gradoService.findById(id);
            if (gradoToDelete.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>(false, GRADO_NO_ENCONTRADO, null));
            }

            // Verificar si el grado tiene estudiantes o profesores asignados
            if (gradoService.hasActiveAssociations(id)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDto<>(false,
                                "No se puede eliminar el grado porque tiene estudiantes o profesores asignados", null));
            }

            gradoService.deleteById(id);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "Grado eliminado exitosamente", gradoToDelete.get()));
        } catch (ApplicationException e) {
            throw new ApplicationException(null, "Error al eliminar el grado: ", e.getHttpStatus());
        }
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Actualiza un grado", description = "Solo los administradores pueden actualizar grados")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDto<GradoDto>> updateGrado(
            @PathVariable Long id,
            @RequestBody @Valid GradoDto gradoDto) {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserResponseDTO user = userService.findByEmail(userDetails.getUsername());

            if (!user.rol().equals(ROLE_ADMINISTRADOR)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponseDto<>(false, "Solo los administradores pueden actualizar grados", null));
            }

            if (!gradoService.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>(false, GRADO_NO_ENCONTRADO, null));
            }

            gradoDto.setId(id); // Asegurar que el ID coincida
            GradoDto updatedGrado = gradoService.update(gradoDto);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "Grado actualizado exitosamente", updatedGrado));
        } catch (ApplicationException e) {
            throw new ApplicationException(null, "Error al actualizar el grado: ", e.getHttpStatus());
        }
    }

    public UserPrincipal getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        UserResponseDTO user = userService.findByEmail(userDetails.getUsername());
        return new UserPrincipal(user.id(), user.email());
    }
}
