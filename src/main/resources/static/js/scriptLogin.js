/**
 * Script para la página de Login del Intranet
 * Colchones D'Encanto
 */

// Limpiar mensajes de error al escribir en los campos
document.getElementById('username').addEventListener('input', function() {
    document.getElementById('usernameError').textContent = '';
    this.classList.remove('is-invalid');
});

document.getElementById('password').addEventListener('input', function() {
    document.getElementById('passwordError').textContent = '';
    this.classList.remove('is-invalid');
});

// Manejar el formulario de login
document.getElementById('loginForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    // Limpiar errores previos
    document.getElementById('usernameError').textContent = '';
    document.getElementById('passwordError').textContent = '';
    document.getElementById('authenticationError').style.display = 'none';
    document.getElementById('username').classList.remove('is-invalid');
    document.getElementById('password').classList.remove('is-invalid');

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    // Validaciones del lado del cliente
    let hasErrors = false;

    if (!username) {
        document.getElementById('usernameError').textContent = 'El usuario es requerido';
        document.getElementById('username').classList.add('is-invalid');
        hasErrors = true;
    } else if (username.length < 3) {
        document.getElementById('usernameError').textContent = 'El usuario debe tener al menos 3 caracteres';
        document.getElementById('username').classList.add('is-invalid');
        hasErrors = true;
    }

    if (!password) {
        document.getElementById('passwordError').textContent = 'La contraseña es requerida';
        document.getElementById('password').classList.add('is-invalid');
        hasErrors = true;
    } else if (password.length < 4) {
        document.getElementById('passwordError').textContent = 'La contraseña debe tener al menos 4 caracteres';
        document.getElementById('password').classList.add('is-invalid');
        hasErrors = true;
    }

    if (hasErrors) {
        return;
    }

    // Mostrar loading
    document.querySelector('.btn-login').disabled = true;
    document.querySelector('.btn-login').innerHTML = '<i class="bx bx-loader bx-spin"></i> Conectando...';

    try {
        const result = await loginWithJWT(username, password);

        if (result.success) {
            // Mostrar modal de éxito
            document.getElementById('welcomeMessage').textContent = 'Bienvenido, ' + result.data.username;
            document.getElementById('roleMessage').textContent = 'Rol: ' + result.data.rol;
            document.getElementById('successModal').style.display = 'flex';

            // Esperar 1 segundo y luego redirigir
            // El JWT ya está guardado en localStorage y authUtils.js lo usará automáticamente
            setTimeout(function() {
                // Navegar de forma segura - el JwtFilter validará el JWT en el header
                window.location.href = '/intranet/dashboard';
            }, 1500);
        } else {
            // Mostrar errores de validación si los hay
            if (result.errors && typeof result.errors === 'object') {
                if (result.errors.username) {
                    document.getElementById('usernameError').textContent = result.errors.username;
                    document.getElementById('username').classList.add('is-invalid');
                }
                if (result.errors.password) {
                    document.getElementById('passwordError').textContent = result.errors.password;
                    document.getElementById('password').classList.add('is-invalid');
                }
                if (result.errors.authentication) {
                    document.getElementById('authenticationError').textContent = result.errors.authentication;
                    document.getElementById('authenticationError').style.display = 'block';
                }
            } else {
                // Mostrar modal de error genérico
                document.getElementById('errorMessage').textContent = result.error || 'Usuario y/o contraseña incorrectos';
                document.getElementById('errorModal').style.display = 'flex';
            }
            
            // Restaurar botón
            document.querySelector('.btn-login').disabled = false;
            document.querySelector('.btn-login').innerHTML = '<i class="bx bxs-check-circle"></i> Iniciar';
        }
    } catch (error) {
        console.error('Error:', error);
        document.getElementById('authenticationError').textContent = 'Error de conexión. Intenta de nuevo.';
        document.getElementById('authenticationError').style.display = 'block';
        
        // Restaurar botón
        document.querySelector('.btn-login').disabled = false;
        document.querySelector('.btn-login').innerHTML = '<i class="bx bxs-check-circle"></i> Iniciar';
    }
});

// Cerrar modal de error
function closeModal() {
    document.getElementById('errorModal').style.display = 'none';
}

// Cerrar modales si se hace click fuera
window.addEventListener('click', function(event) {
    const successModal = document.getElementById('successModal');
    const errorModal = document.getElementById('errorModal');
    
    if (event.target === successModal) {
        successModal.style.display = 'none';
    }
    if (event.target === errorModal) {
        errorModal.style.display = 'none';
    }
});
