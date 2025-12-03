/**
 * SCRIPT PARA LA PÁGINA DE PRODUCTOS (INTRANET)
 * Colchones D'Encanto - Gestión de Productos
 */

// =============================================
// UTILIDADES
// =============================================

/**
 * Obtiene el token JWT del almacenamiento
 */
function getToken() {
    try {
        const token = localStorage.getItem('jwt_token');
        return token || '';
    } catch (e) {
        try {
            const token = sessionStorage.getItem('jwt_token');
            return token || '';
        } catch (e2) {
            console.warn('No se puede acceder al almacenamiento de tokens');
            return '';
        }
    }
}

/**
 * Confirma y ejecuta cierre de sesión
 */
function confirmLogout() {
    if (confirm('¿Está seguro de que desea cerrar sesión?')) {
        try { localStorage.removeItem('jwt_token'); } catch (e) { }
        try { sessionStorage.removeItem('jwt_token'); } catch (e) { }
        window.location.href = '/intranet/login';
    }
}

// =============================================
// VALIDACIONES DE PRODUCTOS (Sincronizado con Jakarta Validation)
// =============================================

/**
 * Reglas de validación para productos
 * Sincronizadas con las anotaciones de Jakarta Validation en Producto.java:
 * - @NotBlank, @NotNull para campos requeridos
 * - @Size para longitud de texto
 * - @Min, @Max para valores numéricos
 * - @DecimalMin, @DecimalMax para decimales
 * - @Pattern para expresiones regulares
 */
const reglasProducto = {
    // ===== INFORMACIÓN BÁSICA =====
    nombre: {
        required: true,      // @NotBlank
        minLength: 3,        // @Size(min = 3)
        maxLength: 200,      // @Size(max = 200)
        messages: {
            required: 'El nombre es obligatorio',
            minLength: 'El nombre debe tener entre 3 y 200 caracteres',
            maxLength: 'El nombre debe tener entre 3 y 200 caracteres'
        }
    },
    codigo: {
        required: true,                    // @NotBlank
        minLength: 3,                      // @Size(min = 3)
        maxLength: 100,                    // @Size(max = 100)
        pattern: /^[a-zA-Z0-9\-_]+$/,      // @Pattern(regexp = "^[a-zA-Z0-9\\-_]+$")
        messages: {
            required: 'El código es obligatorio',
            minLength: 'El código debe tener entre 3 y 100 caracteres',
            maxLength: 'El código debe tener entre 3 y 100 caracteres',
            pattern: 'El código solo puede contener letras, números, guiones y guiones bajos'
        }
    },
    categoria: {
        required: true,      // @NotBlank
        minLength: 2,        // @Size(min = 2)
        maxLength: 100,      // @Size(max = 100)
        messages: {
            required: 'La categoría es obligatoria',
            minLength: 'La categoría debe tener entre 2 y 100 caracteres',
            maxLength: 'La categoría debe tener entre 2 y 100 caracteres'
        }
    },
    descripcion: {
        required: false,
        maxLength: 1000,     // @Size(max = 1000)
        messages: {
            maxLength: 'La descripción no puede exceder 1000 caracteres'
        }
    },
    
    // ===== PRECIOS Y STOCK =====
    precio: {
        required: true,       // @NotNull
        min: 0.01,            // @DecimalMin(value = "0.01")
        max: 999999.99,       // @DecimalMax(value = "999999.99")
        messages: {
            required: 'El precio es obligatorio',
            min: 'El precio debe ser mayor a 0',
            max: 'El precio no puede exceder 999999.99'
        }
    },
    stock: {
        required: true,       // @NotNull
        min: 0,               // @Min(value = 0)
        max: 999999,          // @Max(value = 999999)
        isInteger: true,
        messages: {
            required: 'El stock es obligatorio',
            min: 'El stock no puede ser negativo',
            max: 'El stock no puede exceder 999999',
            isInteger: 'El stock debe ser un número entero'
        }
    },
    
    // ===== FICHA TÉCNICA =====
    material: {
        required: false,
        maxLength: 200,       // @Size(max = 200)
        messages: {
            maxLength: 'El material no puede exceder 200 caracteres'
        }
    },
    dimensiones: {
        required: false,
        maxLength: 200,       // @Size(max = 200)
        pattern: /^[0-9x,\.\s]*$/,  // @Pattern(regexp = "^[0-9x,\\.\\s]*$")
        messages: {
            maxLength: 'Las dimensiones no pueden exceder 200 caracteres',
            pattern: 'Las dimensiones deben contener solo números, \'x\', comas y puntos'
        }
    },
    peso: {
        required: false,
        maxLength: 100,       // @Size(max = 100)
        pattern: /^[0-9,\.\s\w]*$/,  // @Pattern(regexp = "^[0-9,\\.\\s\\w]*$")
        messages: {
            maxLength: 'El peso no puede exceder 100 caracteres',
            pattern: 'El peso debe ser un valor numérico válido'
        }
    },
    firmeza: {
        required: false,
        maxLength: 100,       // @Size(max = 100)
        messages: {
            maxLength: 'La firmeza no puede exceder 100 caracteres'
        }
    },
    garantia: {
        required: false,
        maxLength: 100,       // @Size(max = 100)
        messages: {
            maxLength: 'La garantía no puede exceder 100 caracteres'
        }
    },
    caracteristicas: {
        required: false,
        maxLength: 2000,      // @Size(max = 2000)
        messages: {
            maxLength: 'Las características no pueden exceder 2000 caracteres'
        }
    }
};

/**
 * Muestra un mensaje de error debajo de un campo
 */
function mostrarErrorProducto(inputId, mensaje) {
    const input = document.getElementById(inputId);
    if (!input) return;
    
    // Buscar o crear el contenedor de error
    let errorDiv = input.parentElement.querySelector('.error-producto');
    if (!errorDiv) {
        errorDiv = document.createElement('div');
        errorDiv.className = 'error-producto text-danger small mt-1';
        input.parentElement.appendChild(errorDiv);
    }
    
    errorDiv.textContent = mensaje;
    errorDiv.style.display = 'block';
    input.classList.add('is-invalid');
    input.classList.remove('is-valid');
}

/**
 * Oculta el mensaje de error de un campo
 */
function ocultarErrorProducto(inputId) {
    const input = document.getElementById(inputId);
    if (!input) return;
    
    const errorDiv = input.parentElement.querySelector('.error-producto');
    if (errorDiv) {
        errorDiv.textContent = '';
        errorDiv.style.display = 'none';
    }
    
    input.classList.remove('is-invalid');
    if (input.value.trim()) {
        input.classList.add('is-valid');
    }
}

/**
 * Valida un campo específico
 */
function validarCampoProducto(inputId, reglas) {
    const input = document.getElementById(inputId);
    if (!input) return true;
    
    const valor = input.value.trim();
    
    // Validar requerido
    if (reglas.required && !valor) {
        mostrarErrorProducto(inputId, reglas.messages.required);
        return false;
    }
    
    // Si no es requerido y está vacío, es válido
    if (!reglas.required && !valor) {
        ocultarErrorProducto(inputId);
        return true;
    }
    
    // Validar longitud mínima
    if (reglas.minLength && valor.length < reglas.minLength) {
        mostrarErrorProducto(inputId, reglas.messages.minLength);
        return false;
    }
    
    // Validar longitud máxima
    if (reglas.maxLength && valor.length > reglas.maxLength) {
        mostrarErrorProducto(inputId, reglas.messages.maxLength);
        return false;
    }
    
    // Validar patrón
    if (reglas.pattern && valor && !reglas.pattern.test(valor)) {
        mostrarErrorProducto(inputId, reglas.messages.pattern);
        return false;
    }
    
    // Validar que sea entero
    if (reglas.isInteger && !Number.isInteger(parseFloat(valor))) {
        mostrarErrorProducto(inputId, reglas.messages.isInteger);
        return false;
    }
    
    // Validar mínimo numérico
    if (reglas.min !== undefined && parseFloat(valor) < reglas.min) {
        mostrarErrorProducto(inputId, reglas.messages.min);
        return false;
    }
    
    // Validar máximo numérico
    if (reglas.max !== undefined && parseFloat(valor) > reglas.max) {
        mostrarErrorProducto(inputId, reglas.messages.max);
        return false;
    }
    
    ocultarErrorProducto(inputId);
    return true;
}

/**
 * Valida todo el formulario de agregar producto
 */
function validarFormularioProductoAgregar() {
    let esValido = true;
    
    // Información básica (requeridos)
    esValido = validarCampoProducto('nombre', reglasProducto.nombre) && esValido;
    esValido = validarCampoProducto('codigo', reglasProducto.codigo) && esValido;
    esValido = validarCampoProducto('categoria', reglasProducto.categoria) && esValido;
    esValido = validarCampoProducto('descripcion', reglasProducto.descripcion) && esValido;
    
    // Precios y stock
    esValido = validarCampoProducto('precio', reglasProducto.precio) && esValido;
    esValido = validarCampoProducto('stock', reglasProducto.stock) && esValido;
    
    // Ficha técnica (opcionales pero con validación de formato)
    esValido = validarCampoProducto('material', reglasProducto.material) && esValido;
    esValido = validarCampoProducto('dimensiones', reglasProducto.dimensiones) && esValido;
    esValido = validarCampoProducto('peso', reglasProducto.peso) && esValido;
    esValido = validarCampoProducto('firmeza', reglasProducto.firmeza) && esValido;
    esValido = validarCampoProducto('garantia', reglasProducto.garantia) && esValido;
    esValido = validarCampoProducto('caracteristicas', reglasProducto.caracteristicas) && esValido;
    
    return esValido;
}

/**
 * Valida todo el formulario de editar producto
 */
function validarFormularioProductoEditar() {
    let esValido = true;
    
    // Información básica (requeridos)
    esValido = validarCampoProducto('editNombre', reglasProducto.nombre) && esValido;
    esValido = validarCampoProducto('editCodigo', reglasProducto.codigo) && esValido;
    esValido = validarCampoProducto('editCategoria', reglasProducto.categoria) && esValido;
    esValido = validarCampoProducto('editDescripcion', reglasProducto.descripcion) && esValido;
    
    // Precios y stock
    esValido = validarCampoProducto('editPrecio', reglasProducto.precio) && esValido;
    esValido = validarCampoProducto('editStock', reglasProducto.stock) && esValido;
    
    // Ficha técnica (opcionales pero con validación de formato)
    esValido = validarCampoProducto('editMaterial', reglasProducto.material) && esValido;
    esValido = validarCampoProducto('editDimensiones', reglasProducto.dimensiones) && esValido;
    esValido = validarCampoProducto('editPeso', reglasProducto.peso) && esValido;
    esValido = validarCampoProducto('editFirmeza', reglasProducto.firmeza) && esValido;
    esValido = validarCampoProducto('editGarantia', reglasProducto.garantia) && esValido;
    esValido = validarCampoProducto('editCaracteristicas', reglasProducto.caracteristicas) && esValido;
    
    return esValido;
}

/**
 * Configura validación en tiempo real para formulario de agregar
 */
function configurarValidacionProductoAgregar() {
    const campos = [
        // Información básica
        { id: 'nombre', reglas: reglasProducto.nombre },
        { id: 'codigo', reglas: reglasProducto.codigo },
        { id: 'categoria', reglas: reglasProducto.categoria },
        { id: 'descripcion', reglas: reglasProducto.descripcion },
        // Precios y stock
        { id: 'precio', reglas: reglasProducto.precio },
        { id: 'stock', reglas: reglasProducto.stock },
        // Ficha técnica
        { id: 'material', reglas: reglasProducto.material },
        { id: 'dimensiones', reglas: reglasProducto.dimensiones },
        { id: 'peso', reglas: reglasProducto.peso },
        { id: 'firmeza', reglas: reglasProducto.firmeza },
        { id: 'garantia', reglas: reglasProducto.garantia },
        { id: 'caracteristicas', reglas: reglasProducto.caracteristicas }
    ];
    
    campos.forEach(campo => {
        const input = document.getElementById(campo.id);
        if (input) {
            // Validar al perder foco
            input.addEventListener('blur', () => validarCampoProducto(campo.id, campo.reglas));
            
            // Validar mientras escribe (solo si ya tiene error)
            input.addEventListener('input', () => {
                if (input.classList.contains('is-invalid')) {
                    validarCampoProducto(campo.id, campo.reglas);
                }
            });
        }
    });
}

/**
 * Configura validación en tiempo real para formulario de editar
 */
function configurarValidacionProductoEditar() {
    const campos = [
        // Información básica
        { id: 'editNombre', reglas: reglasProducto.nombre },
        { id: 'editCodigo', reglas: reglasProducto.codigo },
        { id: 'editCategoria', reglas: reglasProducto.categoria },
        { id: 'editDescripcion', reglas: reglasProducto.descripcion },
        // Precios y stock
        { id: 'editPrecio', reglas: reglasProducto.precio },
        { id: 'editStock', reglas: reglasProducto.stock },
        // Ficha técnica
        { id: 'editMaterial', reglas: reglasProducto.material },
        { id: 'editDimensiones', reglas: reglasProducto.dimensiones },
        { id: 'editPeso', reglas: reglasProducto.peso },
        { id: 'editFirmeza', reglas: reglasProducto.firmeza },
        { id: 'editGarantia', reglas: reglasProducto.garantia },
        { id: 'editCaracteristicas', reglas: reglasProducto.caracteristicas }
    ];
    
    campos.forEach(campo => {
        const input = document.getElementById(campo.id);
        if (input) {
            // Validar al perder foco
            input.addEventListener('blur', () => validarCampoProducto(campo.id, campo.reglas));
            
            // Validar mientras escribe (solo si ya tiene error)
            input.addEventListener('input', () => {
                if (input.classList.contains('is-invalid')) {
                    validarCampoProducto(campo.id, campo.reglas);
                }
            });
        }
    });
}

/**
 * Limpia las validaciones del formulario de agregar
 */
function limpiarValidacionesAgregar() {
    const campos = [
        'nombre', 'codigo', 'categoria', 'precio', 'stock', 'descripcion',
        'material', 'dimensiones', 'peso', 'firmeza', 'garantia', 'caracteristicas'
    ];
    campos.forEach(id => {
        const input = document.getElementById(id);
        if (input) {
            input.classList.remove('is-invalid', 'is-valid');
            const errorDiv = input.parentElement.querySelector('.error-producto');
            if (errorDiv) {
                errorDiv.textContent = '';
                errorDiv.style.display = 'none';
            }
        }
    });
}

/**
 * Limpia las validaciones del formulario de editar
 */
function limpiarValidacionesEditar() {
    const campos = [
        'editNombre', 'editCodigo', 'editCategoria', 'editPrecio', 'editStock', 'editDescripcion',
        'editMaterial', 'editDimensiones', 'editPeso', 'editFirmeza', 'editGarantia', 'editCaracteristicas'
    ];
    campos.forEach(id => {
        const input = document.getElementById(id);
        if (input) {
            input.classList.remove('is-invalid', 'is-valid');
            const errorDiv = input.parentElement.querySelector('.error-producto');
            if (errorDiv) {
                errorDiv.textContent = '';
                errorDiv.style.display = 'none';
            }
        }
    });
}

// =============================================
// MANEJO DE IMÁGENES - CONVERSIÓN Y PREVIEW
// =============================================

/**
 * Convierte una imagen a Base64
 */
function convertirImagenABase64(file, callback) {
    if (!file) {
        callback(null);
        return;
    }
    const reader = new FileReader();
    reader.onload = function (e) {
        const base64 = e.target.result.split(',')[1];
        callback(base64);
    };
    reader.readAsDataURL(file);
}

/**
 * Preview Imagen Principal (Agregar)
 */
function previewImagePrincipal(input) {
    if (input.files && input.files[0]) {
        const preview = document.getElementById('previewImagenPrincipal');
        const reader = new FileReader();
        reader.onload = function (e) {
            preview.src = e.target.result;
            preview.style.display = 'block';
            const base64 = e.target.result.split(',')[1];
            document.getElementById('imagenPrincipal').value = base64;
        };
        reader.readAsDataURL(input.files[0]);
    }
}

/**
 * Preview Imagen Técnica 1 (Agregar)
 */
function previewImagenTecnica1(input) {
    if (input.files && input.files[0]) {
        const preview = document.getElementById('previewImagenTecnica1');
        const reader = new FileReader();
        reader.onload = function (e) {
            preview.src = e.target.result;
            preview.style.display = 'block';
            const base64 = e.target.result.split(',')[1];
            document.getElementById('imagenTecnica1').value = base64;
        };
        reader.readAsDataURL(input.files[0]);
    }
}

/**
 * Preview Imagen Técnica 2 (Agregar)
 */
function previewImagenTecnica2(input) {
    if (input.files && input.files[0]) {
        const preview = document.getElementById('previewImagenTecnica2');
        const reader = new FileReader();
        reader.onload = function (e) {
            preview.src = e.target.result;
            preview.style.display = 'block';
            const base64 = e.target.result.split(',')[1];
            document.getElementById('imagenTecnica2').value = base64;
        };
        reader.readAsDataURL(input.files[0]);
    }
}

/**
 * Preview Imagen Principal (Editar)
 */
function previewImagePrincipalEdit(input) {
    if (input.files && input.files[0]) {
        const preview = document.getElementById('previewImagenPrincipalEdit');
        const reader = new FileReader();
        reader.onload = function (e) {
            preview.src = e.target.result;
            preview.style.display = 'block';
            const base64 = e.target.result.split(',')[1];
            document.getElementById('editImagenPrincipal').value = base64;
        };
        reader.readAsDataURL(input.files[0]);
    }
}

/**
 * Preview Imagen Técnica 1 (Editar)
 */
function previewImagenTecnica1Edit(input) {
    if (input.files && input.files[0]) {
        const preview = document.getElementById('previewImagenTecnica1Edit');
        const reader = new FileReader();
        reader.onload = function (e) {
            preview.src = e.target.result;
            preview.style.display = 'block';
            const base64 = e.target.result.split(',')[1];
            document.getElementById('editImagenTecnica1').value = base64;
        };
        reader.readAsDataURL(input.files[0]);
    }
}

/**
 * Preview Imagen Técnica 2 (Editar)
 */
function previewImagenTecnica2Edit(input) {
    if (input.files && input.files[0]) {
        const preview = document.getElementById('previewImagenTecnica2Edit');
        const reader = new FileReader();
        reader.onload = function (e) {
            preview.src = e.target.result;
            preview.style.display = 'block';
            const base64 = e.target.result.split(',')[1];
            document.getElementById('editImagenTecnica2').value = base64;
        };
        reader.readAsDataURL(input.files[0]);
    }
}

// =============================================
// FILTROS Y BÚSQUEDA
// =============================================

/**
 * Aplica filtros de búsqueda
 */
function aplicarFiltros() {
    const termino = document.getElementById('inputBusqueda').value;
    const categoria = document.getElementById('selectCategoria').value;
    const estado = document.getElementById('selectEstado').value;

    fetch(`/intranet/productos/api/filtrar?termino=${encodeURIComponent(termino)}&categoria=${encodeURIComponent(categoria)}&estado=${encodeURIComponent(estado)}`, {
        headers: {
            'Authorization': 'Bearer ' + getToken()
        }
    })
        .then(r => r.json())
        .then(productos => {
            mostrarProductos(productos);
        })
        .catch(e => console.error('Error:', e));
}

/**
 * Limpia todos los filtros
 */
function limpiarFiltros() {
    document.getElementById('inputBusqueda').value = '';
    document.getElementById('selectCategoria').value = '';
    document.getElementById('selectEstado').value = '';
    aplicarFiltros();
}

/**
 * Muestra productos en la tabla
 */
function mostrarProductos(productos) {
    const tbody = document.getElementById('bodyProductos');
    const noResultados = document.getElementById('noResultados');
    const countProductos = document.getElementById('countProductos');

    if (productos.length === 0) {
        tbody.style.display = 'none';
        noResultados.style.display = 'block';
        countProductos.textContent = '0';
        return;
    }

    tbody.style.display = 'table-row-group';
    noResultados.style.display = 'none';
    countProductos.textContent = productos.length;

    tbody.innerHTML = productos.map(p => `
        <tr>
            <td>${p.id}</td>
            <td>
                ${p.imagenPrincipal ? `<img src="data:image/jpeg;base64,${p.imagenPrincipal}" style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;">` : '<span class="badge bg-secondary">Sin imagen</span>'}
            </td>
            <td>${p.nombre}</td>
            <td>${p.categoria}</td>
            <td class="product-price">S/ ${p.precio.toFixed(2)}</td>
            <td>
                ${p.stock > 0 ? `<span class="badge bg-success">${p.stock} un.</span>` : '<span class="badge bg-danger">Agotado</span>'}
            </td>
            <td>
                <span class="badge ${p.estado === 'Disponible' ? 'bg-success' :
            p.estado === 'Agotado' ? 'bg-danger' :
                p.estado === 'Descontinuado' ? 'bg-warning' :
                    'bg-secondary'
        }">${p.estado}</span>
            </td>
            <td>
                <button class="btn btn-sm btn-outline-primary" data-bs-toggle="modal" data-bs-target="#editProductModal" onclick="cargarProductoEnModal(this)" data-id="${p.id}">
                    <i class="bx bx-edit"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="eliminarProductoRest(${p.id})">
                    <i class="bx bx-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

// =============================================
// CRUD DE PRODUCTOS
// =============================================

/**
 * Carga producto en el modal de edición
 */
function cargarProductoEnModal(button) {
    const id = button.getAttribute('data-id');
    fetch(`/intranet/productos/api/obtener/${id}`, {
        headers: {
            'Authorization': 'Bearer ' + getToken()
        }
    })
        .then(r => r.json())
        .then(producto => {
            // Información Básica
            document.getElementById('editId').value = producto.id;
            document.getElementById('editNombre').value = producto.nombre || '';
            document.getElementById('editCodigo').value = producto.codigo || '';
            document.getElementById('editCategoria').value = producto.categoria || '';
            document.getElementById('editEstado').value = producto.estado || '';
            document.getElementById('editDescripcion').value = producto.descripcion || '';

            // Precios y Stock
            document.getElementById('editPrecio').value = producto.precio || '';
            document.getElementById('editStock').value = producto.stock || '';

            // Imagen Principal
            document.getElementById('editImagenPrincipal').value = producto.imagenPrincipal || '';
            if (producto.imagenPrincipal) {
                const previewEdit = document.getElementById('previewImagenPrincipalEdit');
                previewEdit.src = 'data:image/jpeg;base64,' + producto.imagenPrincipal;
                previewEdit.style.display = 'block';
            }

            // Ficha Técnica
            document.getElementById('editMaterial').value = producto.material || '';
            document.getElementById('editDimensiones').value = producto.dimensiones || '';
            document.getElementById('editPeso').value = producto.peso || '';
            document.getElementById('editFirmeza').value = producto.firmeza || '';
            document.getElementById('editGarantia').value = producto.garantia || '';
            document.getElementById('editCaracteristicas').value = producto.caracteristicas || '';

            // Imágenes Técnicas
            document.getElementById('editImagenTecnica1').value = producto.imagenTecnica1 || '';
            if (producto.imagenTecnica1) {
                const previewTec1 = document.getElementById('previewImagenTecnica1Edit');
                previewTec1.src = 'data:image/jpeg;base64,' + producto.imagenTecnica1;
                previewTec1.style.display = 'block';
            }

            document.getElementById('editImagenTecnica2').value = producto.imagenTecnica2 || '';
            if (producto.imagenTecnica2) {
                const previewTec2 = document.getElementById('previewImagenTecnica2Edit');
                previewTec2.src = 'data:image/jpeg;base64,' + producto.imagenTecnica2;
                previewTec2.style.display = 'block';
            }
        })
        .catch(e => console.error('Error al cargar producto:', e));
}

/**
 * Guarda un nuevo producto
 */
function guardarProductoAgregar() {
    const modal = document.getElementById('addProductModal');
    const form = modal.querySelector('form');

    if (!form) {
        console.error('No se encontró el formulario de agregar');
        return;
    }

    // Validar formulario con feedback visual
    if (!validarFormularioProductoAgregar()) {
        // Abrir el accordion de información básica si hay errores ahí
        const collapseBasico = document.getElementById('collapseBasico');
        if (collapseBasico && !collapseBasico.classList.contains('show')) {
            const bsCollapse = new bootstrap.Collapse(collapseBasico, { toggle: true });
        }
        return;
    }

    // Recopilar datos del formulario
    const nombre = document.getElementById('nombre').value.trim();
    const codigo = document.getElementById('codigo').value.trim();
    const categoria = document.getElementById('categoria').value.trim();
    const precio = document.getElementById('precio').value;
    const stock = document.getElementById('stock').value;
    const imagenPrincipal = document.getElementById('imagenPrincipal').value;
    const imagenTecnica1 = document.getElementById('imagenTecnica1').value;
    const imagenTecnica2 = document.getElementById('imagenTecnica2').value;

    const producto = {
        nombre: nombre,
        codigo: codigo,
        categoria: categoria,
        estado: document.getElementById('estado').value || 'Disponible',
        descripcion: document.getElementById('descripcion').value.trim(),
        precio: parseFloat(precio),
        stock: parseInt(stock),
        imagenPrincipal: imagenPrincipal || null,
        material: document.getElementById('material').value.trim() || null,
        dimensiones: document.getElementById('dimensiones').value.trim() || null,
        peso: document.getElementById('peso').value.trim() || null,
        firmeza: document.getElementById('firmeza').value.trim() || null,
        garantia: document.getElementById('garantia').value.trim() || null,
        caracteristicas: document.getElementById('caracteristicas').value.trim() || null,
        imagenTecnica1: imagenTecnica1 || null,
        imagenTecnica2: imagenTecnica2 || null
    };

    fetch('/intranet/productos/api/agregar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + getToken()
        },
        body: JSON.stringify(producto)
    })
        .then(response => {
            return response.json();
        })
        .then(data => {
            if (data.success) {
                const bootstrapModal = bootstrap.Modal.getInstance(document.getElementById('addProductModal'));
                if (bootstrapModal) bootstrapModal.hide();
                alert('✅ ' + data.message);
                setTimeout(() => location.reload(), 500);
            } else if (data.error) {
                alert('❌ Error: ' + data.error);
            } else if (data.detalles) {
                // Mostrar errores de validación del servidor en los campos
                for (const field in data.detalles) {
                    mostrarErrorProducto(field, data.detalles[field]);
                }
            } else {
                alert('❌ Error al guardar el producto');
            }
        })
        .catch(e => {
            console.error('🔴 Error en fetch:', e);
            alert('❌ Error: ' + e.message);
        });
}

/**
 * Guarda los cambios de un producto existente
 */
function guardarProductoEditar() {
    const modal = document.getElementById('editProductModal');
    const form = modal.querySelector('form');

    if (!form) {
        console.error('No se encontró el formulario de editar');
        return;
    }

    // Validar formulario con feedback visual
    if (!validarFormularioProductoEditar()) {
        return;
    }

    const id = document.getElementById('editId').value;
    const nombre = document.getElementById('editNombre').value.trim();
    const codigo = document.getElementById('editCodigo').value.trim();
    const categoria = document.getElementById('editCategoria').value.trim();
    const precio = document.getElementById('editPrecio').value;
    const stock = document.getElementById('editStock').value;

    const producto = {
        nombre: nombre,
        codigo: codigo,
        categoria: categoria,
        estado: document.getElementById('editEstado').value,
        descripcion: document.getElementById('editDescripcion').value.trim(),
        precio: parseFloat(precio),
        stock: parseInt(stock),
        imagenPrincipal: document.getElementById('editImagenPrincipal').value.trim() || null,
        material: document.getElementById('editMaterial').value.trim() || null,
        dimensiones: document.getElementById('editDimensiones').value.trim() || null,
        peso: document.getElementById('editPeso').value.trim() || null,
        firmeza: document.getElementById('editFirmeza').value.trim() || null,
        garantia: document.getElementById('editGarantia').value.trim() || null,
        caracteristicas: document.getElementById('editCaracteristicas').value.trim() || null,
        imagenTecnica1: document.getElementById('editImagenTecnica1').value.trim() || null,
        imagenTecnica2: document.getElementById('editImagenTecnica2').value.trim() || null
    };

    fetchWithAuth(`/intranet/productos/api/editar/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(producto)
    })
        .then(response => {
            return response.json();
        })
        .then(data => {
            if (data.success) {
                const mdl = bootstrap.Modal.getInstance(document.getElementById('editProductModal'));
                if (mdl) mdl.hide();
                alert('✅ ' + data.message);
                setTimeout(() => location.reload(), 500);
            } else if (data.error) {
                if (data.detalles) {
                    // Mostrar errores de validación del servidor en los campos
                    for (let field in data.detalles) {
                        // Mapear el nombre del campo al ID del campo en el formulario de edición
                        const editFieldId = 'edit' + field.charAt(0).toUpperCase() + field.slice(1);
                        mostrarErrorProducto(editFieldId, data.detalles[field]);
                    }
                } else {
                    alert('❌ Error: ' + data.error);
                }
            } else {
                alert('❌ Error al actualizar el producto');
            }
        })
        .catch(e => {
            console.error('🔴 Error en fetch:', e);
            alert('❌ Error: ' + e.message);
        });
}

/**
 * Elimina (descontinúa) un producto
 */
function eliminarProductoRest(id) {
    if (!confirm('¿Está seguro de que desea descontinuar este producto?')) return;

    fetchWithAuth(`/intranet/productos/api/eliminar/${id}`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => {
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            return response.json();
        })
        .then(data => {
            if (data.success) {
                alert(data.message);
                setTimeout(() => location.reload(), 500);
            } else if (data.error) {
                alert('Error: ' + data.error);
            }
        })
        .catch(e => {
            console.error('❌ Error:', e);
            alert('Error: ' + e.message);
        });
}

// =============================================
// RESETEO DE MODALES
// =============================================

/**
 * Resetea el modal de agregar producto
 */
function resetearModalAgregar() {
    const modal = document.getElementById('addProductModal');
    const form = modal.querySelector('form');

    if (form) {
        form.reset();
    }

    // Limpiar campos hidden de imágenes
    const imgPrincipal = document.getElementById('imagenPrincipal');
    const imgTec1 = document.getElementById('imagenTecnica1');
    const imgTec2 = document.getElementById('imagenTecnica2');
    
    if (imgPrincipal) imgPrincipal.value = '';
    if (imgTec1) imgTec1.value = '';
    if (imgTec2) imgTec2.value = '';

    // Limpiar previsualizaciones
    const previews = ['previewImagenPrincipal', 'previewImagenTecnica1', 'previewImagenTecnica2'];
    previews.forEach(id => {
        const preview = document.getElementById(id);
        if (preview) {
            preview.style.display = 'none';
            preview.src = '';
        }
    });

    // Limpiar validaciones
    limpiarValidacionesAgregar();
}

/**
 * Resetea el modal de editar producto
 */
function resetearModalEditar() {
    const previews = ['previewImagenPrincipalEdit', 'previewImagenTecnica1Edit', 'previewImagenTecnica2Edit'];
    previews.forEach(id => {
        const preview = document.getElementById(id);
        if (preview) {
            preview.style.display = 'none';
            preview.src = '';
        }
    });

    // Limpiar validaciones
    limpiarValidacionesEditar();
}

// =============================================
// INICIALIZACIÓN
// =============================================

document.addEventListener('DOMContentLoaded', function () {
    // Cargar productos al iniciar
    aplicarFiltros();

    // Configurar listeners para filtros
    const inputBusqueda = document.getElementById('inputBusqueda');
    const selectCategoria = document.getElementById('selectCategoria');
    const selectEstado = document.getElementById('selectEstado');

    if (inputBusqueda) inputBusqueda.addEventListener('input', aplicarFiltros);
    if (selectCategoria) selectCategoria.addEventListener('change', aplicarFiltros);
    if (selectEstado) selectEstado.addEventListener('change', aplicarFiltros);

    // Configurar validaciones en tiempo real
    configurarValidacionProductoAgregar();
    configurarValidacionProductoEditar();

    // Resetear modal de agregar cuando se abre
    const addProductModal = document.getElementById('addProductModal');
    if (addProductModal) {
        addProductModal.addEventListener('show.bs.modal', resetearModalAgregar);
    }

    // Resetear modal de editar cuando se abre
    const editProductModal = document.getElementById('editProductModal');
    if (editProductModal) {
        editProductModal.addEventListener('show.bs.modal', resetearModalEditar);
    }

    // Event listeners para inputs de archivo de imágenes técnicas (AGREGAR)
    const inputImagenTecnica1 = document.getElementById('inputImagenTecnica1');
    if (inputImagenTecnica1) {
        inputImagenTecnica1.addEventListener('change', function () {
            previewImagenTecnica1(this);
        });
    }

    const inputImagenTecnica2 = document.getElementById('inputImagenTecnica2');
    if (inputImagenTecnica2) {
        inputImagenTecnica2.addEventListener('change', function () {
            previewImagenTecnica2(this);
        });
    }

    // Event listeners para inputs de archivo de imágenes técnicas (EDITAR)
    const inputImagenTecnica1Edit = document.getElementById('inputImagenTecnica1Edit');
    if (inputImagenTecnica1Edit) {
        inputImagenTecnica1Edit.addEventListener('change', function () {
            previewImagenTecnica1Edit(this);
        });
    }

    const inputImagenTecnica2Edit = document.getElementById('inputImagenTecnica2Edit');
    if (inputImagenTecnica2Edit) {
        inputImagenTecnica2Edit.addEventListener('change', function () {
            previewImagenTecnica2Edit(this);
        });
    }
});
