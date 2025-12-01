// ========================================
// GESTIÓN DEL CARRITO DE COTIZACIONES
// ========================================

const STORAGE_KEY = 'carritoCotizaciones';

/**
 * Inicializa el carrito en localStorage si no existe
 */
function inicializarCarrito() {
    if (!localStorage.getItem(STORAGE_KEY)) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify([]));
    }
}

/**
 * Obtiene el carrito actual
 */
function obtenerCarrito() {
    const carrito = localStorage.getItem(STORAGE_KEY);
    return carrito ? JSON.parse(carrito) : [];
}

/**
 * Guarda el carrito en localStorage
 */
function guardarCarrito(carrito) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(carrito));
}

/**
 * Agrega un producto al carrito
 * @param {Object} producto - {id, nombre, precio, imagenPrincipal}
 * @param {number} cantidad - Cantidad a agregar (default 1)
 */
function agregarAlCarrito(producto, cantidad = 1) {
    let carrito = obtenerCarrito();
    
    // Buscar si el producto ya existe
    const existe = carrito.findIndex(p => p.id === producto.id);
    
    if (existe !== -1) {
        // Si existe, sumar cantidad
        carrito[existe].cantidad += cantidad;
    } else {
        // Si no existe, agregarlo
        carrito.push({
            id: producto.id,
            nombre: producto.nombre,
            precio: producto.precio,
            imagenPrincipal: producto.imagenPrincipal,
            cantidad: cantidad
        });
    }
    
    guardarCarrito(carrito);
    mostrarNotificacion(`✓ "${producto.nombre}" agregado al carrito`);
    actualizarBadgeCarrito();
    
    return carrito;
}

/**
 * Elimina un producto del carrito
 */
function eliminarDelCarrito(productoId) {
    let carrito = obtenerCarrito();
    carrito = carrito.filter(p => p.id !== productoId);
    guardarCarrito(carrito);
    actualizarBadgeCarrito();
}

/**
 * Actualiza la cantidad de un producto
 */
function actualizarCantidad(productoId, nuevaCantidad) {
    let carrito = obtenerCarrito();
    const producto = carrito.find(p => p.id === productoId);
    
    if (producto) {
        if (nuevaCantidad <= 0) {
            eliminarDelCarrito(productoId);
        } else {
            producto.cantidad = nuevaCantidad;
            guardarCarrito(carrito);
        }
        actualizarBadgeCarrito();
    }
}

/**
 * Calcula el total del carrito desde API
 */
async function calcularTotal() {
    try {
        const carrito = obtenerCarrito();
        const response = await fetch('/api/carrito/calcular-total', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ items: carrito })
        });
        
        const result = await response.json();
        if (result.success) {
            return result.total;
        }
        // Fallback local
        return carrito.reduce((total, p) => total + (p.precio * p.cantidad), 0);
    } catch (error) {
        console.error('Error calculando total:', error);
        // Fallback local
        const carrito = obtenerCarrito();
        return carrito.reduce((total, p) => total + (p.precio * p.cantidad), 0);
    }
}

/**
 * Obtiene la cantidad de items en el carrito desde API
 */
async function obtenerCantidadItems() {
    try {
        const carrito = obtenerCarrito();
        const response = await fetch('/api/carrito/contar-items', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ items: carrito })
        });
        
        const result = await response.json();
        if (result.success) {
            return result.total;
        }
        // Fallback local
        return carrito.reduce((total, p) => total + p.cantidad, 0);
    } catch (error) {
        console.error('Error contando items:', error);
        // Fallback local
        const carrito = obtenerCarrito();
        return carrito.reduce((total, p) => total + p.cantidad, 0);
    }
}

/**
 * Actualiza el badge del carrito en el navbar
 */
function actualizarBadgeCarrito() {
    const cantidad = obtenerCantidadItems();
    
    // Buscar todos los badges en la página (puede haber múltiples si hay varios navbars)
    const badges = document.querySelectorAll('#cartBadge, [id*="cartBadge"], .cart-badge, [data-cart-badge]');
    
    badges.forEach(badge => {
        if (cantidad > 0) {
            badge.textContent = cantidad;
            badge.style.display = 'inline-block';
            badge.classList.add('badge', 'bg-danger');
        } else {
            badge.style.display = 'none';
        }
    });
    
    // Si no hay badge específico, crear uno
    if (badges.length === 0 && cantidad > 0) {
        const carritoBtn = document.querySelector('[href*="carrito"]');
        if (carritoBtn) {
            let badge = carritoBtn.querySelector('.badge');
            if (!badge) {
                badge = document.createElement('span');
                badge.className = 'badge bg-danger ms-1';
                badge.id = 'cartBadge';
                badge.textContent = cantidad;
                carritoBtn.appendChild(badge);
            } else {
                badge.textContent = cantidad;
            }
        }
    }
}

/**
 * Muestra una notificación temporal
 */
function mostrarNotificacion(mensaje) {
    const container = document.getElementById('notificacionContainer');
    
    if (!container) {
        const div = document.createElement('div');
        div.id = 'notificacionContainer';
        div.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
        `;
        document.body.appendChild(div);
    }

    const notif = document.createElement('div');
    notif.className = 'alert alert-success alert-dismissible fade show';
    notif.role = 'alert';
    notif.innerHTML = `
        ${mensaje}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    notif.style.cssText = `
        min-width: 300px;
        animation: slideIn 0.3s ease-in-out;
    `;

    document.getElementById('notificacionContainer').appendChild(notif);

    setTimeout(() => {
        notif.remove();
    }, 3000);
}

/**
 * Abre el modal del carrito mini (resumen rápido)
 */
function abrirMiniCarrito() {
    const carrito = obtenerCarrito();
    const total = calcularTotal();

    const contenido = carrito.length === 0
        ? '<p class="text-muted">Tu carrito está vacío</p>'
        : `
            <div class="carrito-items">
                ${carrito.map(p => `
                    <div class="carrito-item">
                        <div>
                            <strong>${p.nombre}</strong><br>
                            <small>Cantidad: ${p.cantidad}</small>
                        </div>
                        <div>
                            S/ ${(p.precio * p.cantidad).toFixed(2)}
                        </div>
                    </div>
                `).join('')}
            </div>
            <hr>
            <div class="carrito-total">
                <strong>Total: S/ ${total.toFixed(2)}</strong>
            </div>
            <a href="/carrito/cotizaciones" class="btn btn-primary btn-sm w-100 mt-2">
                Ir a Cotización
            </a>
        `;

    alert(contenido);
}

/**
 * Vacía el carrito
 */
function vaciarCarrito() {
    if (confirm('¿Deseas vaciar el carrito?')) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify([]));
        actualizarBadgeCarrito();
        mostrarNotificacion('Carrito vaciado');
    }
}

// Inicializar al cargar la página
document.addEventListener('DOMContentLoaded', () => {
    inicializarCarrito();
    actualizarBadgeCarrito();
});
