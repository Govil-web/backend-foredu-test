package com.foroescolar.mapper.grado;

import com.foroescolar.dtos.grado.GradoDto;
import com.foroescolar.model.Grado;
import com.foroescolar.model.Profesor;
import com.foroescolar.services.ProfesorService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class GradoMapper {

    @Autowired
    private ProfesorService profesorService;

    @Mapping(source = "profesor", target = "profesor", qualifiedByName = "profesorToLong")
    @Mapping(source = "profesor", target = "profesorNombre", qualifiedByName = "profesorName")
   public abstract GradoDto toResponseDto(Grado grado);

    @Mapping(source = "profesor", target = "profesor", qualifiedByName = "longToProfesor")
    public abstract Grado toEntity(GradoDto gradoDto);

    @Named("longToProfesor")
    protected Profesor longToProfesor(Long id) {
        if (id == null) {
            return null;
        }
        Profesor profesor = new Profesor();
        profesor.setId(id);
        return profesor;
    }
    @Named("profesorToLong")
    protected Long profesorToLong(Profesor profesor) {
        return profesor != null ? profesor.getId() : null;
    }

    @Named("profesorName")
    protected String profesorName(Profesor profesor) {
        return profesor != null ? profesor.getNombre() : null;
    }


}
