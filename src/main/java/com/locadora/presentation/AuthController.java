package com.locadora.presentation;
import com.locadora.application.dto.request.UsuarioLoginRequest;
import com.locadora.application.dto.request.UsuarioRequest;
import com.locadora.application.dto.response.LoginResponse;
import com.locadora.application.dto.response.UsuarioResponse;
import com.locadora.application.service.JwtService;
import com.locadora.application.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

// autenticação e registro de usuário

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody UsuarioRequest request) {
        try {
            UsuarioResponse response = usuarioService.registrar(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Erro ao registrar usuário: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody UsuarioLoginRequest request) {
        try {
            if (!usuarioService.validarCredenciais(request)) {
                return ResponseEntity.status(401).build();
            }

            return usuarioService.buscarPorEmailECpf(request.getEmail(), request.getCpf())
                    .map(usuario -> {
                        String token = jwtService.gerarToken(usuario);
                        UsuarioResponse usuarioResponse = usuarioService.toResponse(usuario);
                        return ResponseEntity.ok(new LoginResponse(token, usuarioResponse));
                    })
                    .orElse(ResponseEntity.status(401).build());
        } catch (Exception e) {
            System.err.println("Erro ao realizar login: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
