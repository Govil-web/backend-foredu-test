package com.foroescolar.services;

import com.foroescolar.dtos.user.UserRequestDTO;
import com.foroescolar.dtos.user.UserResponseDTO;
import com.foroescolar.model.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService extends GenericService<User, Long, UserRequestDTO ,UserResponseDTO>{

    Optional<UserResponseDTO> findById(Long id);
    UserResponseDTO save(UserRequestDTO user);
    void deleteById(Long id);
    Iterable<UserResponseDTO> findAll();
    UserResponseDTO findByEmail(String email);

    UserResponseDTO update(UserRequestDTO user);
}
