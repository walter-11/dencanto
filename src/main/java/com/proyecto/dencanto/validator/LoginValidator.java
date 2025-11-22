package com.proyecto.dencanto.validator;

import com.proyecto.dencanto.dto.AuthRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class LoginValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return AuthRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AuthRequest request = (AuthRequest) target;

        // Validar que username no esté vacío
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            errors.rejectValue("username", "field.required", "El usuario es requerido");
        }

        // Validar que username tenga longitud mínima
        if (request.getUsername() != null && request.getUsername().length() < 3) {
            errors.rejectValue("username", "field.minlength", new Object[]{3}, "El usuario debe tener al menos 3 caracteres");
        }

        // Validar que username no supere longitud máxima
        if (request.getUsername() != null && request.getUsername().length() > 50) {
            errors.rejectValue("username", "field.maxlength", new Object[]{50}, "El usuario no puede exceder 50 caracteres");
        }

        // Validar que password no esté vacío
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            errors.rejectValue("password", "field.required", "La contraseña es requerida");
        }

        // Validar que password tenga longitud mínima
        if (request.getPassword() != null && request.getPassword().length() < 4) {
            errors.rejectValue("password", "field.minlength", new Object[]{4}, "La contraseña debe tener al menos 4 caracteres");
        }

        // Validar que password no supere longitud máxima
        if (request.getPassword() != null && request.getPassword().length() > 100) {
            errors.rejectValue("password", "field.maxlength", new Object[]{100}, "La contraseña no puede exceder 100 caracteres");
        }
    }
}
