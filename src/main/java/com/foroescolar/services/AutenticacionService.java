package com.foroescolar.services;

import com.foroescolar.dtos.jwttoken.JWTTokenDTO;
import com.foroescolar.dtos.user.DatosAutenticacionUsuario;

public interface AutenticacionService {

    JWTTokenDTO autenticar(DatosAutenticacionUsuario datosAutenticacionUsuario);
}
