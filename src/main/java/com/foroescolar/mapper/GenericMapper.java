package com.foroescolar.mapper;

public interface GenericMapper <T, RequestDTO, ResponseDTO>{

    T toEntity(RequestDTO requestDTO);

    ResponseDTO toResponseDto(T entity);

}
