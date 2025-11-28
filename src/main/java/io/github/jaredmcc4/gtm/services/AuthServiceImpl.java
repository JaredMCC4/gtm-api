package io.github.jaredmcc4.gtm.services;

import io.github.jaredmcc4.gtm.domain.RefreshToken;
import io.github.jaredmcc4.gtm.domain.Rol;
import io.github.jaredmcc4.gtm.domain.Usuario;
import io.github.jaredmcc4.gtm.dto.auth.JwtResponse;
import io.github.jaredmcc4.gtm.dto.auth.LoginRequest;
import io.github.jaredmcc4.gtm.dto.auth.RegistroRequest;
import io.github.jaredmcc4.gtm.exception.ResourceNotFoundException;
import io.github.jaredmcc4.gtm.exception.UnauthorizedException;
import io.github.jaredmcc4.gtm.repository.RefreshTokenRepository;
import io.github.jaredmcc4.gtm.repository.RolRepository;
import io.github.jaredmcc4.gtm.repository.UsuarioRepository;
import io.github.jaredmcc4.gtm.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementacion de {@link AuthService} encargada de registro, autenticacion
 * y administracion de tokens JWT y refresh tokens persistidos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Registra un nuevo usuario con rol USER y contraseña cifrada.
     *
     * @param registroRequest datos de registro
     * @return usuario creado
     */
    @Override
    @Transactional
    public Usuario registrarUsuario(RegistroRequest registroRequest) {
        log.info("Registrando usuario nuevo con el email: {}", registroRequest.getEmail());

        if (usuarioRepository.existsByEmail(registroRequest.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email proporcionado.");
        }

        Rol rol = rolRepository.findByNombreRol("USER").orElseThrow(() -> new ResourceNotFoundException("El rol de usuario no existe."));

        Set<Rol> roles = new HashSet<>();
        roles.add(rol);

        Usuario usuario = Usuario.builder()
                .email(registroRequest.getEmail())
                .contrasenaHash(passwordEncoder.encode(registroRequest.getPassword()))
                .nombreUsuario(registroRequest.getNombreUsuario())
                .zonaHoraria(registroRequest.getZonaHoraria())
                .activo(true)
                .roles(roles)
                .build();
        return usuarioRepository.save(usuario);
    }

    /**
     * Autentica credenciales, valida usuario activo y genera JWT + refresh token.
     *
     * @param loginRequest email y contraseña
     * @return respuesta con tokens y expiraciones
     */
    @Override
    @Transactional
    public JwtResponse autenticarUsuario(LoginRequest loginRequest) {
        log.info("Autenticando usuario con el email: {}", loginRequest.getEmail());

        Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getContrasenaHash())) {
            throw new BadCredentialsException("Credenciales inválidas.");
        }

        if (!usuario.isActivo()) {
            throw new BadCredentialsException("El usuario no está activo.");
        }

        return emitirTokensParaUsuario(usuario);
    }

    /**
     * Valida un refresh token y emite un nuevo JWT manteniendo el refresh.
     *
     * @param refreshToken token de refresco actual
     * @return nuevos datos de autentificacion
     */
    @Override
    @Transactional
    public JwtResponse refrescarToken(String refreshToken) {
        log.info("Refrescando el token.");

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("El refresh token es inválido."));

        if (token.getRevoked() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("El refresh token ha sido revocado o expirado.");
        }

        Usuario usuario = token.getUsuario();
        List<String> roles = usuario.getRoles().stream().map(Rol::getNombreRol).collect(Collectors.toList());
        String jwtTokenGenerado = jwtUtil.generarToken(usuario.getEmail(), usuario.getId(), roles);

        return JwtResponse.builder()
                .jwtToken(jwtTokenGenerado)
                .type("Bearer")
                .expiresIn(jwtExpiration)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Revoca el refresh token indicado y cierra la sesión.
     *
     * @param refreshToken token a revocar
     */
    @Override
    @Transactional
    public void cerrarSesion(String refreshToken) {
        log.info("Cerrando sesión");

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token no encontrado."));

        token.setRevoked(true);
        refreshTokenRepository.save(token);

    }

    /**
     * Valida la integridad y vigencia de un JWT de acceso.
     *
     * @param token JWT recibido
     */
    @Override
    public void validarToken(String token) {
        String email = jwtUtil.extraerEmail(token);
        if (!jwtUtil.validarToken(token, email)) {
            throw new IllegalArgumentException("Token inválido.");
        }
    }

    /**
     * Emite un JWT + refresh token para un usuario autenticado (por ejemplo, login social).
     *
     * @param usuario entidad existente y habilitada
     * @return respuesta con tokens
     */
    @Override
    @Transactional
    public JwtResponse emitirTokensParaUsuario(Usuario usuario) {
        List<String> roles = usuario.getRoles().stream().map(Rol::getNombreRol).collect(Collectors.toList());

        String jwtToken = jwtUtil.generarToken(usuario.getEmail(), usuario.getId(), roles);
        String refreshToken = crearRefreshToken(usuario);

        return JwtResponse.builder()
                .jwtToken(jwtToken)
                .type("Bearer")
                .expiresIn(jwtExpiration)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Genera y persiste un refresh token con vigencia de 30 días para el usuario.
     *
     * @param usuario propietario
     * @return cadena del refresh token
     */
    private String crearRefreshToken(Usuario usuario) {
        String token = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }
}
