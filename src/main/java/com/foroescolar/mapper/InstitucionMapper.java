package com.foroescolar.mapper;

import com.foroescolar.dtos.institucion.InstitucionFullDto;
import com.foroescolar.dtos.institucion.InstitucionRequestDto;
import com.foroescolar.dtos.institucion.InstitucionResponseDto;
import com.foroescolar.model.Grado;
import com.foroescolar.model.Institucion;
import com.foroescolar.model.User;
import com.foroescolar.repository.GradoRepository;
import com.foroescolar.repository.InstitucionRepository;
import com.foroescolar.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class InstitucionMapper {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GradoRepository gradoRepository;
    @Autowired
    private InstitucionRepository institucionRepository;

    public abstract InstitucionResponseDto toResponseDto(Institucion institucion);

    public abstract Institucion toEntity(InstitucionRequestDto institucionRequestDto);

    @Mapping(source = "grados", target = "gradoIds", qualifiedByName = "gradosToLongList")
    @Mapping(source = "users", target = "userIds", qualifiedByName = "usersToLongList")
    abstract InstitucionFullDto toFullDto(Institucion institucion);

    @Named("gradosToLongList")
    protected List<Long> gradosToLongList(List<Grado> grados) {
        return grados.stream().map(Grado::getId).toList();
    }

    @Named("usersToLongList")
    protected List<Long> usersToLongList(List<User> users) {
        return users.stream().map(User::getId).toList();
    }


}
