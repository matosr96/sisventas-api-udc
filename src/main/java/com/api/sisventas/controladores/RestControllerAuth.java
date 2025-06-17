package com.api.sisventas.controladores;

import com.api.sisventas.dtos.DtoAuthRespuesta;
import com.api.sisventas.dtos.DtoLogin;
import com.api.sisventas.dtos.DtoRegistro;
import com.api.sisventas.entidades.Roles;
import com.api.sisventas.entidades.Usuarios;
import com.api.sisventas.repositorios.RolesRepositorio;
import com.api.sisventas.repositorios.UsuariosRepositorios;
import com.api.sisventas.seguridad.JwtGenerador;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/auth")
@Tag(name = "Autenticación", description = "APIs para gestión de autenticación y registro de usuarios")
public class RestControllerAuth {
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private RolesRepositorio rolesRepository;
    private UsuariosRepositorios usuariosRepository;
    private JwtGenerador jwtGenerador;

    @Autowired
    public RestControllerAuth(AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, 
                            RolesRepositorio rolesRepository, UsuariosRepositorios usuariosRepository, 
                            JwtGenerador jwtGenerador) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.rolesRepository = rolesRepository;
        this.usuariosRepository = usuariosRepository;
        this.jwtGenerador = jwtGenerador;
    }

    @Operation(summary = "Registrar nuevo usuario", description = "Crea un nuevo usuario en el sistema y devuelve un token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = DtoAuthRespuesta.class))),
        @ApiResponse(responseCode = "400", description = "Datos de registro inválidos"),
        @ApiResponse(responseCode = "409", description = "El nombre de usuario ya existe"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/signup")
    public ResponseEntity<DtoAuthRespuesta> registrar(
            @Parameter(description = "Datos de registro del usuario", required = true)
            @RequestBody DtoRegistro dtoRegistro) {
        try {
            if (usuariosRepository.existsByUsername(dtoRegistro.getUsername())) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }

            Usuarios usuarios = new Usuarios();
            usuarios.setUsername(dtoRegistro.getUsername());
            usuarios.setPassword(passwordEncoder.encode(dtoRegistro.getPassword()));
            usuarios.setNombre(dtoRegistro.getNombre());
            usuarios.setApellido(dtoRegistro.getApellido());
            usuarios.setFoto(dtoRegistro.getFoto());

            Roles roles = rolesRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Role USER no encontrado"));
            usuarios.setRoles(Collections.singletonList(roles));

            usuariosRepository.save(usuarios);

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dtoRegistro.getUsername(), dtoRegistro.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerador.generarToken(authentication);

            return new ResponseEntity<>(new DtoAuthRespuesta(token, usuarios), HttpStatus.OK);
        } catch (DataAccessException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve un token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = DtoAuthRespuesta.class))),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/login")
    public ResponseEntity<DtoAuthRespuesta> login(
            @Parameter(description = "Credenciales de acceso", required = true)
            @RequestBody DtoLogin dtoLogin) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dtoLogin.getUsername(), dtoLogin.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerador.generarToken(authentication);

            Optional<Usuarios> usuario = usuariosRepository.findByUsername(dtoLogin.getUsername());
            if (usuario.isPresent()) {
                return new ResponseEntity<>(new DtoAuthRespuesta(token, usuario.get()), HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
