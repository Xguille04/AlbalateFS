import { TestBed } from '@angular/core/testing';
import { TiendaFilterService } from './tienda-filter.service';
import { Producto } from './producto.service';

describe('TiendaFilterService', () => {
  let service: TiendaFilterService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TiendaFilterService]
    });
    service = TestBed.inject(TiendaFilterService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should filter by category', () => {
    const productos: Producto[] = [
      { id: 1, nombre: 'Jersey Local', categoria: 'Ropa', descripcion: 'Jersey de juego', precio: 40, stock: 10 },
      { id: 2, nombre: 'Gorra', categoria: 'Accesorios', descripcion: 'Gorra del club', precio: 15, stock: 20 },
      { id: 3, nombre: 'Jersey Visitante', categoria: 'Ropa', descripcion: 'Jersey visitante', precio: 40, stock: 10 }
    ];
    const favoritosIds = new Set<number>();

    service.setCategoriaActiva('Ropa');
    const result = service.applyFilters(productos, favoritosIds);

    expect(result.length).toBe(2);
    expect(result.every(p => p.categoria === 'Ropa')).toBe(true);
  });

  it('should filter by search query', () => {
    const productos: Producto[] = [
      { id: 1, nombre: 'Jersey Local', categoria: 'Ropa', descripcion: 'Jersey de juego', precio: 40, stock: 10 },
      { id: 2, nombre: 'Gorra', categoria: 'Accesorios', descripcion: 'Gorra del club', precio: 15, stock: 20 }
    ];
    const favoritosIds = new Set<number>();

    service.setSearchQuery('Jersey');
    const result = service.applyFilters(productos, favoritosIds);

    expect(result.length).toBe(1);
    expect(result[0].nombre).toContain('Jersey');
  });

  it('should filter by favoritos', () => {
    const productos: Producto[] = [
      { id: 1, nombre: 'Jersey', categoria: 'Ropa', descripcion: 'Jersey de juego', precio: 40, stock: 10 },
      { id: 2, nombre: 'Gorra', categoria: 'Accesorios', descripcion: 'Gorra del club', precio: 15, stock: 20 },
      { id: 3, nombre: 'Botín', categoria: 'Equipamiento', descripcion: 'Botines', precio: 120, stock: 5 }
    ];
    const favoritosIds = new Set<number>([1, 3]);

    service.setMostrarSoloFavoritos(true);
    const result = service.applyFilters(productos, favoritosIds);

    expect(result.length).toBe(2);
    expect(result.every(p => favoritosIds.has(p.id))).toBe(true);
  });

  it('should sort by price ascending', () => {
    const productos: Producto[] = [
      { id: 1, nombre: 'Caro', categoria: 'Ropa', descripcion: '', precio: 100, stock: 10 },
      { id: 2, nombre: 'Barato', categoria: 'Ropa', descripcion: '', precio: 20, stock: 10 }
    ];
    const favoritosIds = new Set<number>();

    service.setOrdenActivo('precio_asc');
    const result = service.applyFilters(productos, favoritosIds);

    expect(result[0].nombre).toBe('Barato');
    expect(result[1].nombre).toBe('Caro');
  });

  it('should reset filters', () => {
    service.setSearchQuery('test');
    service.setCategoriaActiva('Ropa');
    service.setMostrarSoloFavoritos(true);
    service.setOrdenActivo('precio_desc');

    service.reset();

    service.filterState$.subscribe(state => {
      expect(state.searchQuery).toBe('');
      expect(state.categoriaActiva).toBe('Todos');
      expect(state.mostrarSoloFavoritos).toBe(false);
      expect(state.ordenActivo).toBe('nombre');
    });
  });
});
