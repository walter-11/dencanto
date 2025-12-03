/**
 * SCRIPT PARA LA PÁGINA DE USUARIOS.HTML
 * Colchones D'Encanto - Gestión de Usuarios
 */

// =============================================
// SIDEBAR TOGGLE
// =============================================
document.addEventListener('DOMContentLoaded', function () {
  const hamburger = document.querySelector(".toggle-btn");
  const toggler = document.querySelector("#icon");
  const sidebar = document.getElementById('sidebar');

  if (hamburger && sidebar && toggler) {
    hamburger.addEventListener("click", function () {
      sidebar.classList.toggle("expand");
      sidebar.classList.toggle("hide");
      toggler.classList.toggle("bxs-chevrons-right");
      toggler.classList.toggle("bxs-chevrons-left");
    });
  }

  // Función para filtrar usuarios
  const searchInput = document.querySelector('.search-container input');
  if (searchInput) {
    searchInput.addEventListener('input', function (e) {
      const searchTerm = e.target.value.toLowerCase();
      const rows = document.querySelectorAll('tbody tr');

      rows.forEach(row => {
        const cells = row.querySelectorAll('td');
        if (cells.length > 2) {
          const userName = cells[2].textContent.toLowerCase();
          row.style.display = userName.includes(searchTerm) ? '' : 'none';
        }
      });
    });
  }

  // Generar contraseña temporal automáticamente
  const passwordInput = document.getElementById('userPassword');
  if (passwordInput) {
    passwordInput.addEventListener('focus', function () {
      if (!this.value) {
        const tempPassword = Math.random().toString(36).slice(-8);
        this.value = tempPassword;
      }
    });
  }

  // Configurar validación en tiempo real
  configurarValidacionTiempoReal();
  
  // ========== INICIALIZACIÓN DE USUARIOS ==========
  
  // Validación en tiempo real para formulario AGREGAR
  const formAgregar = document.getElementById('formAgregarUsuario');
  if (formAgregar) {
    formAgregar.addEventListener('submit', function(e) {
      if (!validarFormularioAgregarUsuario()) {
        e.preventDefault();
        e.stopPropagation();
      }
    });
  }
  
  // Validación en tiempo real para formulario EDITAR
  const formEditar = document.getElementById('formEditarUsuario');
  if (formEditar) {
    formEditar.addEventListener('submit', function(e) {
      if (!validarFormularioEditarUsuario()) {
        e.preventDefault();
        e.stopPropagation();
      }
    });
  }

  // Cargar datos en modal de edición
  const editButtons = document.querySelectorAll('.edit-btn');
  editButtons.forEach(button => {
    button.addEventListener('click', function () {
      const id = this.getAttribute('data-id');
      const nombreUsuario = this.getAttribute('data-nombreusuario');
      const nombreCompleto = this.getAttribute('data-nombrecompleto');
      const correo = this.getAttribute('data-correo');
      const telefono = this.getAttribute('data-telefono');
      const rolId = this.getAttribute('data-rolid');

      // Llenar el formulario de edición
      const editUserId = document.getElementById('editUserId');
      const editNombreUsuario = document.getElementById('editNombreUsuario');
      const editNombreCompleto = document.getElementById('editNombreCompleto');
      const editCorreo = document.getElementById('editCorreo');
      const editTelefono = document.getElementById('editTelefono');
      const editRolId = document.getElementById('editRolId');
      const editContrasenaHash = document.getElementById('editContrasenaHash');
      
      if (editUserId) editUserId.value = id;
      if (editNombreUsuario) editNombreUsuario.value = nombreUsuario || '';
      if (editNombreCompleto) editNombreCompleto.value = nombreCompleto || '';
      if (editCorreo) editCorreo.value = correo || '';
      if (editTelefono) editTelefono.value = telefono || '';
      if (editRolId) editRolId.value = rolId || '';
      if (editContrasenaHash) editContrasenaHash.value = '';
      
      // Limpiar errores previos
      document.querySelectorAll('#editUserModal .validation-error').forEach(el => el.textContent = '');
      document.querySelectorAll('#editUserModal .is-invalid').forEach(el => el.classList.remove('is-invalid'));
      document.querySelectorAll('#editUserModal .is-valid').forEach(el => el.classList.remove('is-valid'));
    });
  });

  // Recargar página si hay mensaje de éxito
  const successMessage = document.querySelector('.alert-success');
  if (successMessage) {
    setTimeout(() => location.reload(), 1500);
  }
  
  // Limpiar formulario al abrir modal de agregar
  const addModal = document.getElementById('addUserModal');
  if (addModal) {
    addModal.addEventListener('show.bs.modal', function() {
      const form = document.getElementById('formAgregarUsuario');
      if (form) form.reset();
      document.querySelectorAll('#addUserModal .validation-error').forEach(el => el.textContent = '');
      document.querySelectorAll('#addUserModal .is-invalid').forEach(el => el.classList.remove('is-invalid'));
      document.querySelectorAll('#addUserModal .is-valid').forEach(el => el.classList.remove('is-valid'));
    });
  }
});

// =============================================
// CERRAR SESIÓN
// =============================================
function confirmLogout() {
  if (confirm('¿Está seguro de que desea cerrar sesión?')) {
    if (typeof logoutUser === 'function') {
      logoutUser();
    } else {
      window.location.href = '/intranet/login';
    }
  }
}

// =============================================
// REGLAS DE VALIDACIÓN
// =============================================
const reglas = {
  nombre: {
    patron: /^[A-Za-záéíóúÁÉÍÓÚñÑ\s]{3,50}$/,
    mensaje: 'Solo letras y espacios (3-50 caracteres)'
  },
  apellido: {
    patron: /^[A-Za-záéíóúÁÉÍÓÚñÑ\s]{3,50}$/,
    mensaje: 'Solo letras y espacios (3-50 caracteres)'
  },
  dni: {
    patron: /^\d{8}$/,
    mensaje: 'DNI debe tener exactamente 8 dígitos'
  },
  telefono: {
    patron: /^\d{9}$/,
    mensaje: 'Teléfono debe tener exactamente 9 dígitos'
  },
  usuario: {
    patron: /^[a-zA-Z0-9_]{4,20}$/,
    mensaje: 'Usuario: 4-20 caracteres alfanuméricos o _'
  },
  password: {
    patron: /^.{6,}$/,
    mensaje: 'Contraseña mínimo 6 caracteres'
  },
  email: {
    patron: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    mensaje: 'Ingrese un email válido'
  }
};

// =============================================
// FUNCIONES DE VALIDACIÓN ESPECÍFICAS PARA USUARIOS
// =============================================

/**
 * Valida un campo con reglas específicas
 * @param {HTMLElement} input - Elemento input a validar
 * @param {HTMLElement} errorSpan - Span donde mostrar error
 * @param {Object} reglasValidacion - Reglas de validación
 * @returns {boolean} - true si es válido
 */
function validarCampoUsuario(input, errorSpan, reglasValidacion) {
  if (!input || !errorSpan) return true;
  
  const valor = input.value.trim();
  let mensaje = '';
  
  // Validar requerido
  if (reglasValidacion.required && !valor) {
    mensaje = reglasValidacion.requiredMsg || 'Este campo es obligatorio';
  }
  // Validar longitud mínima
  else if (reglasValidacion.minLength && valor.length < reglasValidacion.minLength) {
    mensaje = `Debe tener al menos ${reglasValidacion.minLength} caracteres`;
  }
  // Validar longitud máxima
  else if (reglasValidacion.maxLength && valor.length > reglasValidacion.maxLength) {
    mensaje = `No puede exceder ${reglasValidacion.maxLength} caracteres`;
  }
  // Validar patrón
  else if (reglasValidacion.pattern && valor && !reglasValidacion.pattern.test(valor)) {
    mensaje = reglasValidacion.patternMsg || 'Formato inválido';
  }
  // Validar email
  else if (reglasValidacion.email && valor && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor)) {
    mensaje = 'El formato del email no es válido';
  }
  
  // Mostrar/ocultar error
  if (mensaje) {
    errorSpan.textContent = mensaje;
    input.classList.add('is-invalid');
    input.classList.remove('is-valid');
    return false;
  } else {
    errorSpan.textContent = '';
    input.classList.remove('is-invalid');
    if (valor) input.classList.add('is-valid');
    return true;
  }
}

/**
 * Valida formulario de agregar usuario
 * @returns {boolean} - true si es válido
 */
function validarFormularioAgregarUsuario() {
  let valido = true;
  
  valido = validarCampoUsuario(
    document.getElementById('nombreUsuario'),
    document.getElementById('errorNombreUsuario'),
    { required: true, minLength: 5, maxLength: 80, pattern: /^[a-zA-Z0-9_]+$/, patternMsg: 'Solo letras, números y guión bajo' }
  ) && valido;
  
  valido = validarCampoUsuario(
    document.getElementById('nombreCompleto'),
    document.getElementById('errorNombreCompleto'),
    { required: true, minLength: 3, maxLength: 150 }
  ) && valido;
  
  valido = validarCampoUsuario(
    document.getElementById('correo'),
    document.getElementById('errorCorreo'),
    { required: true, email: true, maxLength: 150 }
  ) && valido;
  
  valido = validarCampoUsuario(
    document.getElementById('telefono'),
    document.getElementById('errorTelefono'),
    { pattern: /^[0-9]{7,15}$/, patternMsg: 'Debe tener entre 7 y 15 dígitos' }
  ) && valido;
  
  valido = validarCampoUsuario(
    document.getElementById('contrasenaHash'),
    document.getElementById('errorContrasena'),
    { required: true, minLength: 6, maxLength: 100 }
  ) && valido;
  
  valido = validarCampoUsuario(
    document.getElementById('rolId'),
    document.getElementById('errorRol'),
    { required: true, requiredMsg: 'Debe seleccionar un rol' }
  ) && valido;
  
  return valido;
}

/**
 * Valida formulario de editar usuario
 * @returns {boolean} - true si es válido
 */
function validarFormularioEditarUsuario() {
  let valido = true;
  
  valido = validarCampoUsuario(
    document.getElementById('editNombreUsuario'),
    document.getElementById('errorEditNombreUsuario'),
    { required: true, minLength: 5, maxLength: 80, pattern: /^[a-zA-Z0-9_]+$/, patternMsg: 'Solo letras, números y guión bajo' }
  ) && valido;
  
  valido = validarCampoUsuario(
    document.getElementById('editNombreCompleto'),
    document.getElementById('errorEditNombreCompleto'),
    { required: true, minLength: 3, maxLength: 150 }
  ) && valido;
  
  valido = validarCampoUsuario(
    document.getElementById('editCorreo'),
    document.getElementById('errorEditCorreo'),
    { required: true, email: true, maxLength: 150 }
  ) && valido;
  
  valido = validarCampoUsuario(
    document.getElementById('editTelefono'),
    document.getElementById('errorEditTelefono'),
    { pattern: /^[0-9]{7,15}$/, patternMsg: 'Debe tener entre 7 y 15 dígitos' }
  ) && valido;
  
  // Contraseña es opcional en edición, pero si se ingresa debe cumplir mínimo
  const passEdit = document.getElementById('editContrasenaHash');
  if (passEdit && passEdit.value.trim()) {
    valido = validarCampoUsuario(
      passEdit,
      document.getElementById('errorEditContrasena'),
      { minLength: 6, maxLength: 100 }
    ) && valido;
  }
  
  valido = validarCampoUsuario(
    document.getElementById('editRolId'),
    document.getElementById('errorEditRol'),
    { required: true, requiredMsg: 'Debe seleccionar un rol' }
  ) && valido;
  
  return valido;
}

// =============================================
// FUNCIONES DE VALIDACIÓN GENÉRICAS
// =============================================

/**
 * Valida un campo específico
 * @param {HTMLElement} input - Elemento input a validar
 * @param {string} tipo - Tipo de validación a aplicar
 * @returns {boolean} - true si es válido, false si no
 */
function validarCampo(input, tipo) {
  const valor = input.value.trim();
  const regla = reglas[tipo];
  let errorSpan = input.nextElementSibling;

  // Crear span de error si no existe
  if (!errorSpan || !errorSpan.classList.contains('validation-error')) {
    errorSpan = document.createElement('span');
    errorSpan.className = 'validation-error';
    input.parentNode.insertBefore(errorSpan, input.nextSibling);
  }

  // Limpiar clases previas
  input.classList.remove('is-invalid', 'is-valid');

  // Si está vacío y es requerido
  if (!valor && input.hasAttribute('required')) {
    input.classList.add('is-invalid');
    errorSpan.textContent = 'Este campo es obligatorio';
    errorSpan.style.display = 'block';
    return false;
  }

  // Si tiene valor pero no cumple patrón
  if (valor && regla && !regla.patron.test(valor)) {
    input.classList.add('is-invalid');
    errorSpan.textContent = regla.mensaje;
    errorSpan.style.display = 'block';
    return false;
  }

  // Válido
  input.classList.add('is-valid');
  errorSpan.style.display = 'none';
  return true;
}

/**
 * Configura validación en tiempo real para todos los campos
 */
function configurarValidacionTiempoReal() {
  // Campos del formulario agregar
  const camposAgregar = {
    'userFirstName': 'nombre',
    'userLastName': 'apellido',
    'userDni': 'dni',
    'userPhone': 'telefono',
    'userEmail': 'email',
    'userUsername': 'usuario',
    'userPassword': 'password'
  };

  // Campos del formulario editar
  const camposEditar = {
    'editFirstName': 'nombre',
    'editLastName': 'apellido',
    'editDni': 'dni',
    'editPhone': 'telefono',
    'editEmail': 'email',
    'editUsername': 'usuario'
  };

  // Configurar listeners para formulario agregar
  Object.entries(camposAgregar).forEach(([id, tipo]) => {
    const input = document.getElementById(id);
    if (input) {
      input.addEventListener('blur', () => validarCampo(input, tipo));
      input.addEventListener('input', () => {
        if (input.classList.contains('is-invalid')) {
          validarCampo(input, tipo);
        }
      });
    }
  });

  // Configurar listeners para formulario editar
  Object.entries(camposEditar).forEach(([id, tipo]) => {
    const input = document.getElementById(id);
    if (input) {
      input.addEventListener('blur', () => validarCampo(input, tipo));
      input.addEventListener('input', () => {
        if (input.classList.contains('is-invalid')) {
          validarCampo(input, tipo);
        }
      });
    }
  });
}

/**
 * Valida todo el formulario de agregar usuario (versión alternativa)
 * @returns {boolean} - true si es válido, false si no
 */
function validarFormularioAgregar() {
  const campos = [
    { id: 'userFirstName', tipo: 'nombre' },
    { id: 'userLastName', tipo: 'apellido' },
    { id: 'userDni', tipo: 'dni' },
    { id: 'userPhone', tipo: 'telefono' },
    { id: 'userEmail', tipo: 'email' },
    { id: 'userUsername', tipo: 'usuario' },
    { id: 'userPassword', tipo: 'password' }
  ];

  let esValido = true;

  campos.forEach(campo => {
    const input = document.getElementById(campo.id);
    if (input && !validarCampo(input, campo.tipo)) {
      esValido = false;
    }
  });

  // Validar rol
  const rolSelect = document.getElementById('userRole');
  if (rolSelect && !rolSelect.value) {
    rolSelect.classList.add('is-invalid');
    esValido = false;
  } else if (rolSelect) {
    rolSelect.classList.remove('is-invalid');
    rolSelect.classList.add('is-valid');
  }

  if (!esValido) {
    alert('Por favor, corrija los errores en el formulario antes de continuar.');
  }

  return esValido;
}

/**
 * Valida todo el formulario de editar usuario (versión alternativa)
 * @returns {boolean} - true si es válido, false si no
 */
function validarFormularioEditar() {
  const campos = [
    { id: 'editFirstName', tipo: 'nombre' },
    { id: 'editLastName', tipo: 'apellido' },
    { id: 'editDni', tipo: 'dni' },
    { id: 'editPhone', tipo: 'telefono' },
    { id: 'editEmail', tipo: 'email' },
    { id: 'editUsername', tipo: 'usuario' }
  ];

  let esValido = true;

  campos.forEach(campo => {
    const input = document.getElementById(campo.id);
    if (input && !validarCampo(input, campo.tipo)) {
      esValido = false;
    }
  });

  // Validar rol
  const rolSelect = document.getElementById('editRole');
  if (rolSelect && !rolSelect.value) {
    rolSelect.classList.add('is-invalid');
    esValido = false;
  } else if (rolSelect) {
    rolSelect.classList.remove('is-invalid');
    rolSelect.classList.add('is-valid');
  }

  if (!esValido) {
    alert('Por favor, corrija los errores en el formulario antes de continuar.');
  }

  return esValido;
}

/**
 * Limpia la validación de un formulario
 * @param {string} formId - ID del formulario a limpiar
 */
function limpiarValidacion(formId) {
  const form = document.getElementById(formId);
  if (form) {
    const inputs = form.querySelectorAll('input, select');
    inputs.forEach(input => {
      input.classList.remove('is-invalid', 'is-valid');
      const errorSpan = input.nextElementSibling;
      if (errorSpan && errorSpan.classList.contains('validation-error')) {
        errorSpan.style.display = 'none';
      }
    });
    form.reset();
  }
}

// Limpiar validación al abrir modal de agregar
const modalAgregar = document.getElementById('agregarUsuarioModal');
if (modalAgregar) {
  modalAgregar.addEventListener('show.bs.modal', function () {
    limpiarValidacion('formAgregarUsuario');
  });
}

// Limpiar validación al cerrar modal de editar
const modalEditar = document.getElementById('editarUsuarioModal');
if (modalEditar) {
  modalEditar.addEventListener('hidden.bs.modal', function () {
    limpiarValidacion('formEditarUsuario');
  });
}