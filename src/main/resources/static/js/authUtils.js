/**
 * authUtils.js - Utilidades para gestión de JWT en el frontend
 * Funciona como una librería centralizada para autenticación con tokens JWT
 */

// ============== GESTIÓN DE TOKENS ==============

/**
 * Guarda el token JWT en localStorage Y en sessionStorage + cookie (para Tracking Prevention)
 */
function saveToken(token) {
    try {
        // Guardar en localStorage
        localStorage.setItem('jwt_token', token);
    } catch (e) {
        console.warn('⚠️ No se pudo guardar en localStorage:', e.message);
    }
    
    try {
        // Guardar en sessionStorage como fallback
        sessionStorage.setItem('jwt_token', token);
    } catch (e) {
        console.warn('⚠️ No se pudo guardar en sessionStorage:', e.message);
    }
    
    try {
        // Guardar en cookie (24 horas de expiracion)
        const date = new Date();
        date.setTime(date.getTime() + (24 * 60 * 60 * 1000)); // 24 horas
        const expires = "expires=" + date.toUTCString();
        document.cookie = "jwt_token=" + token + ";" + expires + ";path=/";
    } catch (e) {
        console.warn('⚠️ No se pudo guardar en cookie:', e.message);
    }
}

/**
 * Obtiene el token JWT del localStorage o sessionStorage (fallback para Tracking Prevention)
 */
function getToken() {
    try {
        // Intentar localStorage primero
        const token = localStorage.getItem('jwt_token');
        if (token) return token;
    } catch (e) {
        console.warn('⚠️ localStorage bloqueado por Tracking Prevention');
    }
    
    try {
        // Fallback a sessionStorage
        const token = sessionStorage.getItem('jwt_token');
        if (token) return token;
    } catch (e) {
        console.warn('⚠️ sessionStorage también bloqueado');
    }
    
    return null;
}

/**
 * Elimina el token JWT del localStorage
 */
function clearToken() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_info');
}

/**
 * Verifica si existe un token válido
 */
function hasToken() {
    return getToken() !== null && getToken() !== '';
}

// ============== AUTENTICACIÓN ==============

/**
 * Realiza login enviando credenciales a /auth/login
 * @param {string} username - Nombre de usuario
 * @param {string} password - Contraseña
 * @returns {Promise} Promesa con la respuesta del servidor
 */
async function loginWithJWT(username, password) {
    try {
        const response = await fetch('/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (!response.ok) {
            // Si hay errores de validación (400 Bad Request)
            if (response.status === 400 && typeof data === 'object') {
                return { 
                    success: false, 
                    errors: data,
                    error: 'Error de validación'
                };
            }
            // Si hay error de autenticación (401 Unauthorized)
            if (response.status === 401) {
                return { 
                    success: false,
                    errors: data,
                    error: data.authentication || 'Usuario y/o contraseña incorrectos'
                };
            }
            throw new Error('Error en la autenticación');
        }

        // Guardar token y datos del usuario
        saveToken(data.token);
        localStorage.setItem('user_info', JSON.stringify({
            username: data.username,
            rol: data.rol,
            roles: data.roles
        }));

        return { success: true, data };
    } catch (error) {
        console.error('Error en login:', error);
        return { 
            success: false, 
            error: error.message,
            errors: {}
        };
    }
}

/**
 * Obtiene la información del usuario autenticado desde /auth/me
 * @returns {Promise} Promesa con los datos del usuario
 */
async function getCurrentUser() {
    try {
        const response = await fetchWithAuth('/auth/me');
        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('user_info', JSON.stringify(data));
            return data;
        }
        return null;
    } catch (error) {
        console.error('Error al obtener usuario actual:', error);
        return null;
    }
}

/**
 * Obtiene los datos del usuario desde localStorage
 * @returns {Object} Objeto con username y rol
 */
function getUserInfo() {
    const userInfoStr = localStorage.getItem('user_info');
    return userInfoStr ? JSON.parse(userInfoStr) : null;
}

// ============== SOLICITUDES HTTP CON JWT ==============

/**
 * Realiza una solicitud HTTP con autenticación JWT
 * Automáticamente añade el header Authorization: Bearer {token}
 * @param {string} url - URL del endpoint
 * @param {Object} options - Opciones de fetch (method, body, headers, etc.)
 * @returns {Promise} Promesa con la respuesta
 */
async function fetchWithAuth(url, options = {}) {
    const token = getToken();
    
    if (!token) {
        // Si no hay token, redirigir al login
        window.location.href = '/intranet/login';
        return Promise.reject(new Error('No token found'));
    }

    const headers = options.headers || {};
    headers['Authorization'] = `Bearer ${token}`;

    const response = await fetch(url, {
        ...options,
        headers
    });

    // Si el token expiró (401), limpiar y redirigir al login
    if (response.status === 401) {
        clearToken();
        window.location.href = '/intranet/login';
    }

    return response;
}

// ============== LOGOUT ==============

/**
 * Realiza logout - limpia el token y redirige al login
 */
function logout() {
    clearToken();
    window.location.href = '/intranet/login';
}

// ============== VERIFICACIÓN DE AUTENTICACIÓN ==============

/**
 * Verifica si el usuario está autenticado
 * Si no lo está, redirige al login
 */
function checkAuthentication() {
    if (!hasToken()) {
        // Redirigir al login solo si estamos en una ruta protegida
        const currentPath = window.location.pathname;
        if (currentPath.startsWith('/intranet') && currentPath !== '/intranet/login') {
            window.location.href = '/intranet/login';
        }
        return false;
    }
    return true;
}

/**
 * Verifica si el usuario tiene un rol específico
 * @param {string} requiredRole - Rol requerido (ADMIN, VENDEDOR, etc.)
 * @returns {boolean} true si el usuario tiene el rol
 */
function hasRole(requiredRole) {
    const userInfo = getUserInfo();
    if (!userInfo || !userInfo.rol) {
        return false;
    }
    return userInfo.rol === requiredRole;
}

/**
 * Verifica si el usuario tiene alguno de los roles especificados
 * @param {Array} roles - Array de roles permitidos
 * @returns {boolean} true si el usuario tiene alguno de los roles
 */
function hasAnyRole(roles) {
    const userInfo = getUserInfo();
    if (!userInfo || !userInfo.rol) {
        return false;
    }
    return roles.includes(userInfo.rol);
}

// ============== INICIALIZACIÓN ==============

/**
 * Ejecuta verificaciones al cargar la página
 * Llamar en el onload de la página o al inicio del script
 */
document.addEventListener('DOMContentLoaded', function() {
    // Verificar autenticación en rutas protegidas
    checkAuthentication();
    
    // Cargar información del usuario si está disponible
    const userInfo = getUserInfo();
    if (userInfo) {
    }
});
