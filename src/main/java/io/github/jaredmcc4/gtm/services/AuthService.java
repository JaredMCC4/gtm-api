package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.auth.JwtResponse;
import io.github.jaredmcc4.gtm.dto.auth.LoginRequest;
import io.github.jaredmcc4.gtm.dto.auth.RegistroRequest;

public interface AuthService {

    public Usuario registrarUsuario(RegistroRequest registroRequest);

    public JwtResponse autenticarUsuario(LoginRequest loginRequest);
    public JwtResponse refrescarToken(String refreshToken);

    public void cerrarSesion(String refreshToken);
    public void validarToken(String token);
}
