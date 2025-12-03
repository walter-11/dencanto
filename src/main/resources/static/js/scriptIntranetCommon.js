/**
 * Script común para todas las páginas del Intranet
 * Colchones D'Encanto
 * 
 * Contiene funciones de: menú lateral, sidebar toggle, y utilidades compartidas
 */

// ============================================
// INICIALIZACIÓN DE MENÚ - EJECUTAR PRIMERO
// ============================================
function initMenuForCurrentUser() {
    if (!hasToken()) {
        window.location.href = "/intranet/login";
        return;
    }

    const currentUser = getCurrentUser();
    if (currentUser && currentUser.rol) {
        const rolDisplay = document.getElementById("rolDisplay");
        if (rolDisplay) {
            rolDisplay.textContent = currentUser.rol === "ADMIN" ? "Administrador" : "Vendedor";
        }

        // Mostrar elementos del menú según el rol
        if (currentUser.rol === "ADMIN") {
            // ADMIN ve todas las opciones
            const adminMenus = document.querySelectorAll("#adminMenu, #adminMenu2, #adminMenu3, #adminMenu4");
            adminMenus.forEach(menu => menu.style.display = "block");
        } else {
            // VENDEDOR ve sus opciones
            const vendMenus = document.querySelectorAll("#vendMenu, #vendMenu2, #vendMenu3");
            vendMenus.forEach(menu => menu.style.display = "block");
        }
    }
}

// ============================================
// SIDEBAR TOGGLE
// ============================================
function initSidebarToggle() {
    const sidebarToggle = document.querySelector(".toggle-btn");
    const sidebar = document.getElementById("sidebar");
    const icon = document.getElementById("icon");

    if (sidebarToggle && sidebar && icon) {
        sidebarToggle.addEventListener("click", function () {
            sidebar.classList.toggle("hide");
            if (sidebar.classList.contains("hide")) {
                icon.classList.remove("bxs-chevrons-right");
                icon.classList.add("bxs-chevrons-left");
            } else {
                icon.classList.remove("bxs-chevrons-left");
                icon.classList.add("bxs-chevrons-right");
            }
        });
    }
}

// ============================================
// CONFIRMAR LOGOUT
// ============================================
function confirmLogout() {
    if (confirm("¿Estás seguro de que deseas cerrar sesión?")) {
        logoutUser();
    }
}

// ============================================
// AUTO-INICIALIZACIÓN
// ============================================
// Inicializar menú apenas el DOM esté listo
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
        initMenuForCurrentUser();
        initSidebarToggle();
    });
} else {
    initMenuForCurrentUser();
    initSidebarToggle();
}
