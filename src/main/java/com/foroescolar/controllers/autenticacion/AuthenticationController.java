package com.foroescolar.controllers.autenticacion;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.foroescolar.dtos.jwttoken.JWTTokenDTO;
import com.foroescolar.dtos.user.DatosAutenticacionUsuario;
import com.foroescolar.services.AutenticacionService;
import com.foroescolar.services.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/auth")
public class AuthenticationController {

    private final TokenService tokenService;

    private static final String MENSAJE = "mensaje";


    private final AutenticacionService autenticacionService;
    @Autowired
    public AuthenticationController(AutenticacionService autenticacionService, TokenService tokenService) {
        this.autenticacionService = autenticacionService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<JWTTokenDTO> autenticar(@RequestBody @Valid DatosAutenticacionUsuario datosAutenticacionUsuario){
        JWTTokenDTO jwtTokenDTO = autenticacionService.autenticar(datosAutenticacionUsuario);
        return ResponseEntity.ok(jwtTokenDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader) {

        Map<String, String> response = new HashMap<>();

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put(MENSAJE, "Token no proporcionado");
                return ResponseEntity.badRequest().body(response);
            }

            String token = authHeader.substring(7).trim();
            tokenService.invalidateToken(token);

            response.put(MENSAJE, "Sesión cerrada exitosamente");
            return ResponseEntity.ok(response);

        } catch (TokenExpiredException e) {
            response.put(MENSAJE, "El token ya estaba expirado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (Exception e) {
            response.put(MENSAJE, "Error al cerrar sesión");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
