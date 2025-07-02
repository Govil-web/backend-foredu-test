package com.foroescolar.config.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public record SecurityFilterContext(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

}
