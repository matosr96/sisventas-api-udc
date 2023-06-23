package com.api.sisventas.controladores;

import com.api.sisventas.dtos.DtoAuthRespuesta;
import com.api.sisventas.dtos.DtoLogin;
import com.api.sisventas.dtos.DtoRegistro;
import com.api.sisventas.entidades.Roles;
import com.api.sisventas.entidades.Usuarios;
import com.api.sisventas.repositorios.RolesRepositorio;
import com.api.sisventas.repositorios.UsuariosRepositorios;
import com.api.sisventas.seguridad.JwtGenerador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/auth")
public class RestControllerAuth {
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private RolesRepositorio rolesRepository;
    private UsuariosRepositorios usuariosRepository;
    private JwtGenerador jwtGenerador;

    @Autowired
    public RestControllerAuth(AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, RolesRepositorio rolesRepository, UsuariosRepositorios usuariosRepository, JwtGenerador jwtGenerador) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.rolesRepository = rolesRepository;
        this.usuariosRepository = usuariosRepository;
        this.jwtGenerador = jwtGenerador;
    }

    @PostMapping("/signup")
    public ResponseEntity<DtoAuthRespuesta> registrar(@RequestBody DtoRegistro dtoRegistro) {

        Usuarios usuarios = new Usuarios();
        usuarios.setUsername(dtoRegistro.getUsername());
        usuarios.setPassword(passwordEncoder.encode(dtoRegistro.getPassword()));
        usuarios.setNombre(dtoRegistro.getNombre());
        usuarios.setApellido(dtoRegistro.getApellido());
        usuarios.setFoto(dtoRegistro.getFoto());

        Roles roles = rolesRepository.findByName("USER").orElseThrow(() -> new RuntimeException("Role USER no encontrado"));
        usuarios.setRoles(Collections.singletonList(roles));

        usuariosRepository.save(usuarios);

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                dtoRegistro.getUsername(), dtoRegistro.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerador.generarToken(authentication);

        DtoAuthRespuesta authRespuesta = new DtoAuthRespuesta(token, usuarios);

        return new ResponseEntity<>(authRespuesta, HttpStatus.OK);
    }

    @PostMapping("/signupAdm")
    public ResponseEntity<String> registrarAdmin(@RequestBody DtoRegistro dtoRegistro) {
        try {
            if (usuariosRepository.existsByUsername(dtoRegistro.getUsername())) {
                return new ResponseEntity<>("El usuario ya existe, intenta con otro", HttpStatus.BAD_REQUEST);
            }

            Usuarios usuarios = new Usuarios();
            usuarios.setUsername(dtoRegistro.getUsername());
            usuarios.setPassword(passwordEncoder.encode(dtoRegistro.getPassword()));
            usuarios.setNombre(dtoRegistro.getNombre());
            usuarios.setApellido(dtoRegistro.getApellido());
            usuarios.setFoto(dtoRegistro.getFoto());

            Roles roles = rolesRepository.findByName("ADMIN").orElseThrow(() -> new RuntimeException("Role ADMIN no encontrado"));
            usuarios.setRoles(Collections.singletonList(roles));

            usuariosRepository.save(usuarios);
            return new ResponseEntity<>("Registro de admin exitoso", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al registrar admin: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<DtoAuthRespuesta> login(@RequestBody DtoLogin dtoLogin) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    dtoLogin.getUsername(), dtoLogin.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerador.generarToken(authentication);
            Optional<Usuarios> user = usuariosRepository.findByUsername(dtoLogin.getUsername());

            if (user.isPresent()) {
                Usuarios usuario = user.get();
                DtoAuthRespuesta authRespuesta = new DtoAuthRespuesta(token, usuario);
                return new ResponseEntity<>(authRespuesta, HttpStatus.OK);
            } else {
                // El usuario no fue encontrado
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            // Manejar la excepción y devolver una respuesta de error
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
