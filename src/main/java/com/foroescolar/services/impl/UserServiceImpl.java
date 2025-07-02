package com.foroescolar.services.impl;

import com.foroescolar.dtos.user.UserRequestDTO;
import com.foroescolar.dtos.user.UserResponseDTO;
import com.foroescolar.enums.RoleEnum;
import com.foroescolar.exceptions.ApplicationException;
import com.foroescolar.exceptions.model.EntityNotFoundException;
import com.foroescolar.exceptions.model.ForbiddenException;
import com.foroescolar.mapper.user.UserMapper;
import com.foroescolar.model.UpdatedEntities;
import com.foroescolar.model.User;
import com.foroescolar.repository.InstitucionRepository;
import com.foroescolar.repository.UserRepository;
import com.foroescolar.services.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final InstitucionRepository institucionRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, InstitucionRepository institucionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = (BCryptPasswordEncoder) passwordEncoder;
        this.institucionRepository = institucionRepository;
    }

    @Override
    public Optional<UserResponseDTO> findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if(user.isPresent()){
            return Optional.ofNullable(UserMapper.INSTANCE.toResponseDTO(user.get()));
        }else{
            throw new EntityNotFoundException("Usuario no encontrado");
        }
    }

    @Override
    @Transactional
    public UserResponseDTO save(UserRequestDTO user) {
        try {

            if(institucionRepository.count() > 0 && user.institucionId() ==null){
                throw new ForbiddenException("Debe seleccionar una institución");
            }

            if(userRepository.existsByEmail(user.email())) {
                throw new ForbiddenException("Usuario con email ya existente: " + user.email());
            }
            User newUser = UserMapper.INSTANCE.toEntity(user);
            newUser.setContrasena(passwordEncoder.encode(user.contrasena()));
            newUser.setRol(RoleEnum.valueOf("ROLE_ADMINISTRADOR"));
            newUser.setActivo(true);
            newUser = userRepository.save(newUser);
            return UserMapper.INSTANCE.toResponseDTO(newUser);
        } catch (ApplicationException e) {
            throw new ForbiddenException("Error al guardar el usuario: " + e.getMessage());
        }
    }

    @Override
    public void deleteById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            user.get().setActivo(false);
        }else{
            throw new EntityNotFoundException("Usuario no encontrado");
        }
    }
    @Override
    public Iterable<UserResponseDTO> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserMapper.INSTANCE::toResponseDTO)
                .toList();
    }


    @Override
    public UserResponseDTO findByEmail(String username){
       User user= userRepository.findByEmail(username).orElse(null);
       return  UserMapper.INSTANCE.toResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO update(UserRequestDTO user) {

        Optional<User> existingUser = userRepository.findById(user.id());

        if (existingUser.isPresent()) {
            User updatedUser = (User) UpdatedEntities.update(existingUser.get(), user);
            if(user.contrasena() != null){
                validarPassword(user.contrasena());
                updatedUser.setContrasena(passwordEncoder.encode(user.contrasena()));
            }
            return UserMapper.INSTANCE.toResponseDTO(userRepository.save(updatedUser));
        }else {
            throw new EntityNotFoundException("La entidad con ese ID no fue encontrado");
        }
    }

    protected void validarPassword(String contrasena) {
        if (contrasena!=null && contrasena.length() < 8) {
            throw new ForbiddenException("La contraseña debe tener al menos 8 caracteres");
        }
    }
}
