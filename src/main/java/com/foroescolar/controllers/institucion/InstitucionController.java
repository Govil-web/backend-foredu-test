package com.foroescolar.controllers.institucion;

import com.foroescolar.dtos.ApiResponseDto;
import com.foroescolar.dtos.institucion.InstitucionRequestDto;
import com.foroescolar.dtos.institucion.InstitucionResponseDto;
import com.foroescolar.exceptions.ApplicationException;
import com.foroescolar.services.impl.InstitucionService;
import com.foroescolar.utils.ApiResponseUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/institucion")
@PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
public class InstitucionController {

    private final InstitucionService institucionService;

    public InstitucionController(InstitucionService institucionService) {
        this.institucionService = institucionService;
    }

    @PostMapping()
    public ResponseEntity<ApiResponseDto<String>> save(@RequestBody @Valid InstitucionRequestDto institucionRequestDto) {

        try {

          InstitucionResponseDto responseDto =  institucionService.save(institucionRequestDto);
            return ApiResponseUtils.success("Success", "Institucion guardada exitosamente");
        } catch (ApplicationException e) {
            return ApiResponseUtils.error("Error al crear la institucion: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<InstitucionResponseDto>> getInstitucionById(@PathVariable Long id) {

        try {
          InstitucionResponseDto responseDto = institucionService.findById(id);
            return ApiResponseUtils.success(responseDto, "Institucion encontrada");
        } catch (ApplicationException e) {
            return ApiResponseUtils.error("Error al obtener la institucion: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> delete(@PathVariable Long id) {
        try {
            institucionService.deleteById(id);
            return ApiResponseUtils.success("Institucion eliminada exitosamente", " ");
        } catch (ApplicationException e) {
            return ApiResponseUtils.error("Error al eliminar la institucion: " + e.getMessage());
        }
    }

    @GetMapping()
    public ResponseEntity<ApiResponseDto<InstitucionResponseDto>> getAll() {
        try {
            Iterable<InstitucionResponseDto> instituciones = institucionService.findAll();
            return ResponseEntity.ok(new ApiResponseDto<>(true, "Exito", instituciones));
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponseDto<>(false, "Error al obtener las instituciones: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
