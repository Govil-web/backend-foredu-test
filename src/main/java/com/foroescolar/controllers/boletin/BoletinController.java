package com.foroescolar.controllers.boletin;

import com.foroescolar.dtos.ApiResponseDto;

import com.foroescolar.dtos.boletin.BoletinDto;
import com.foroescolar.dtos.user.UserResponseDTO;
import com.foroescolar.exceptions.ApplicationException;

import com.foroescolar.services.BoletinService;
import com.foroescolar.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/boletin")
public class BoletinController {

    private final BoletinService boletinService;
    private final UserService userService;

    @Autowired
    public BoletinController(BoletinService boletinService, UserService userService) {
        this.boletinService = boletinService;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca un boletin por el id")
    public ResponseEntity<ApiResponseDto<BoletinDto>> getBoletin(@PathVariable("id") Long id){

        Optional<BoletinDto> response= boletinService.findById(id);
        if(response.isPresent()){
            BoletinDto boletin= response.get();
            return new ResponseEntity<>(new ApiResponseDto<>(true,"Boletin encontrado",boletin), HttpStatus.CREATED);
        }
    return ResponseEntity.ok(new ApiResponseDto<>(false,String.valueOf(HttpStatus.NOT_FOUND), null));
    }

    @GetMapping("/getAll")
    @Operation(summary = "Lista todos los boletines")
    public ResponseEntity<ApiResponseDto<BoletinDto>> boletinesList(){

        try {
            Iterable<BoletinDto> listarBoletines = boletinService.findAll();
            return new ResponseEntity<>(new ApiResponseDto<>(true, "Exito", listarBoletines), HttpStatus.CREATED);
        } catch (ApplicationException e) {
            throw new ApplicationException("", "Error al listar los boletines", e.getHttpStatus());
        }
    }

    @PostMapping("/add")
    @Operation(summary = "Agrega un Boletin")
    public ResponseEntity<ApiResponseDto<BoletinDto>> addBoletin(@RequestBody BoletinDto boletinDto) {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserResponseDTO user = userService.findByEmail(userDetails.getUsername());

        if (user.rol().equals("ADMINISTRADOR")) {
            return
                    new ResponseEntity<>(
                            new ApiResponseDto<>(true, "Boletin creado con exito", boletinService.save(boletinDto))
                            , HttpStatus.CREATED);
        }
        return ResponseEntity.ok(new ApiResponseDto<>(false, String.valueOf(HttpStatus.BAD_REQUEST), null));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un boletin")
    public ResponseEntity<ApiResponseDto<BoletinDto>> updateBoletin(@PathVariable("id") Long idBoletin, @RequestBody BoletinDto boletinDto) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserResponseDTO user = userService.findByEmail(userDetails.getUsername());
        if (user.rol().equals("ADMINISTRADOR")) {
            Optional<BoletinDto> responseDto = boletinService.findById(idBoletin);
            if (responseDto.isPresent()) {
                boletinDto.setId(idBoletin);
                boletinService.save(boletinDto);
                ApiResponseDto<BoletinDto> responseSalida = new ApiResponseDto<>(true, "Boletin actualizado", boletinDto);
                return new ResponseEntity<>(responseSalida, HttpStatus.CREATED);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }


}
