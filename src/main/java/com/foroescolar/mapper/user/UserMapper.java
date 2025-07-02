package com.foroescolar.mapper.user;

import com.foroescolar.dtos.user.UserRequestDTO;
import com.foroescolar.dtos.user.UserResponseDTO;
import com.foroescolar.model.User;
import com.foroescolar.repository.InstitucionRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {InstitucionRepository.class})
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source="institucion.id", target = "institucionId")
    UserResponseDTO toResponseDTO(User user);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "authorities", ignore = true)
        //@Mapping(source = "institucionId", target = "institucion")
    User toEntity(UserRequestDTO userRequestDTO);
    //UserRequestDTO toRequestDTO(User user);
}