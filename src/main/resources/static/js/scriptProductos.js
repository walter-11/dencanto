// ========== INICIALIZACIÓN ========== 
document.addEventListener('DOMContentLoaded', function () {
    
    // Caché de todos los productos
    const allProducts = Array.from(document.querySelectorAll('[data-product-id]'));
    
    // Referencias a elementos del DOM
    const searchInput = document.getElementById('searchInput');
    const categoryFilter = document.getElementById('categoryFilter');
    const priceFilter = document.getElementById('priceFilter');
    const statusFilter = document.getElementById('statusFilter');
    const priceValue = document.getElementById('priceValue');
    const applyFiltersBtn = document.getElementById('applyFiltersBtn');
    const clearFiltersBtn = document.getElementById('clearFiltersBtn');
    const productsGrid = document.getElementById('productsContainer');
    const emptyState = document.getElementById('emptyState');
    
    // ========== ACTUALIZAR VALOR DEL RANGO ========== 
    if (priceFilter) {
        priceFilter.addEventListener('input', function () {
            priceValue.textContent = `S/ ${parseInt(this.value).toLocaleString('es-PE')}`;
        });
    }
    
    // ========== APLICAR FILTROS ========== 
    function applyFilters() {
        
        const searchTerm = searchInput?.value.toLowerCase() || '';
        const selectedCategory = categoryFilter?.value || '';
        const maxPrice = parseInt(priceFilter?.value) || 5000;
        const selectedStatus = statusFilter?.value || '';
        
        let visibleCount = 0;
        
        // Filtrar productos
        allProducts.forEach(product => {
            const name = product.getAttribute('data-name')?.toLowerCase() || '';
            const category = product.getAttribute('data-category') || '';
            const price = parseFloat(product.getAttribute('data-price')) || 0;
            const status = product.getAttribute('data-status') || '';
            
            // Aplicar criterios de filtro
            const matchesSearch = name.includes(searchTerm);
            const matchesCategory = !selectedCategory || category === selectedCategory;
            const matchesPrice = price <= maxPrice;
            const matchesStatus = !selectedStatus || status === selectedStatus;
            
            const shouldShow = matchesSearch && matchesCategory && matchesPrice && matchesStatus;
            
            if (shouldShow) {
                product.style.display = '';
                visibleCount++;
            } else {
                product.style.display = 'none';
            }
        });
        
        // Mostrar/ocultar estado vacío
        if (visibleCount === 0) {
            emptyState.style.display = 'block';
        } else {
            emptyState.style.display = 'none';
        }
    }
    
    // ========== EVENT LISTENERS PARA FILTROS ========== 
    if (applyFiltersBtn) {
        applyFiltersBtn.addEventListener('click', applyFilters);
    }
    
    if (searchInput) {
        searchInput.addEventListener('input', applyFilters);
    }
    
    if (categoryFilter) {
        categoryFilter.addEventListener('change', applyFilters);
    }
    
    if (priceFilter) {
        priceFilter.addEventListener('change', applyFilters);
    }
    
    if (statusFilter) {
        statusFilter.addEventListener('change', applyFilters);
    }
    
    // ========== LIMPIAR FILTROS ========== 
    if (clearFiltersBtn) {
        clearFiltersBtn.addEventListener('click', function () {
            
            if (searchInput) searchInput.value = '';
            if (categoryFilter) categoryFilter.value = '';
            if (priceFilter) {
                priceFilter.value = 5000;
                priceValue.textContent = 'S/ 5000';
            }
            if (statusFilter) statusFilter.value = '';
            
            emptyState.style.display = 'none';
            
            allProducts.forEach(product => {
                product.style.display = '';
            });
        });
    }
    
    // ========== FUNCIONALIDAD DE ZOOM EN CARRUSEL ========== 
    const zoomInBtns = document.querySelectorAll('.zoom-in-btn');
    const zoomOutBtns = document.querySelectorAll('.zoom-out-btn');
    
    let zoomLevels = {}; // Almacenar niveles de zoom por producto
    
    zoomInBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const productoId = this.getAttribute('data-producto-id');
            const carousel = document.querySelector(`#carouselProducto${productoId} .carousel-image`);
            
            if (carousel) {
                zoomLevels[productoId] = (zoomLevels[productoId] || 1) + 0.2;
                const allImages = document.querySelectorAll(`#carouselProducto${productoId} .carousel-image`);
                allImages.forEach(img => {
                    img.style.transform = `scale(${zoomLevels[productoId]})`;
                });
.toFixed(0)}%`);
            }
        });
    });
    
    zoomOutBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const productoId = this.getAttribute('data-producto-id');
            const carousel = document.querySelector(`#carouselProducto${productoId} .carousel-image`);
            
            if (carousel) {
                zoomLevels[productoId] = Math.max(1, (zoomLevels[productoId] || 1) - 0.2);
                const allImages = document.querySelectorAll(`#carouselProducto${productoId} .carousel-image`);
                allImages.forEach(img => {
                    img.style.transform = `scale(${zoomLevels[productoId]})`;
                });
.toFixed(0)}%`);
            }
        });
    });
    
    // ========== CLICK EN IMAGEN PARA ZOOM ========== 
    const carouselImages = document.querySelectorAll('.carousel-image');
    carouselImages.forEach(img => {
        img.addEventListener('click', function() {
            const productoId = this.getAttribute('data-product-id');
            const currentZoom = zoomLevels[productoId] || 1;
            
            if (currentZoom === 1) {
                zoomLevels[productoId] = 1.5;
                const allImages = document.querySelectorAll(`#carouselProducto${productoId} .carousel-image`);
                allImages.forEach(i => {
                    i.style.transform = 'scale(1.5)';
                });
            } else {
                zoomLevels[productoId] = 1;
                const allImages = document.querySelectorAll(`#carouselProducto${productoId} .carousel-image`);
                allImages.forEach(i => {
                    i.style.transform = 'scale(1)';
                });
            }
        });
    });

    // ========== AGREGAR AL CARRITO ========== 
    const agregarCarritoButtons = document.querySelectorAll('.agregar-carrito-btn');
    agregarCarritoButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const productoId = parseInt(this.getAttribute('data-product-id'));
            const nombre = this.getAttribute('data-nombre');
            const precio = parseFloat(this.getAttribute('data-precio'));
            const imagenProducto = this.getAttribute('data-imagen');
            
            // Obtener cantidad del input
            const modal = this.closest('.modal');
            const cantidadInput = modal?.querySelector('.cantidad-input[data-product-id="' + productoId + '"]');
            const cantidad = cantidadInput ? parseInt(cantidadInput.value) : 1;
            
            // Crear objeto producto
            const producto = {
                id: productoId,
                nombre: nombre,
                precio: precio,
                imagenPrincipal: imagenProducto
            };
            
            // Agregar al carrito
            agregarAlCarrito(producto, cantidad);
`);
        });
    });
});

