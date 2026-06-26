import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Producto } from './producto.service';

export interface TiendaFilterState {
  searchQuery: string;
  categoriaActiva: string;
  mostrarSoloFavoritos: boolean;
  ordenActivo: string;
}

@Injectable({ providedIn: 'root' })
export class TiendaFilterService {
  private filterState = new BehaviorSubject<TiendaFilterState>({
    searchQuery: '',
    categoriaActiva: 'Todos',
    mostrarSoloFavoritos: false,
    ordenActivo: 'nombre'
  });

  filterState$ = this.filterState.asObservable();

  readonly categorias = ['Todos', 'Ropa', 'Accesorios', 'Equipamiento'];
  readonly ordenes = [
    { value: 'nombre', label: 'Nombre A-Z' },
    { value: 'precio_asc', label: 'Precio ↑' },
    { value: 'precio_desc', label: 'Precio ↓' },
    { value: 'destacado', label: 'Destacados' }
  ];

  setSearchQuery(query: string): void {
    const state = this.filterState.value;
    this.filterState.next({ ...state, searchQuery: query });
  }

  setCategoriaActiva(categoria: string): void {
    const state = this.filterState.value;
    this.filterState.next({ ...state, categoriaActiva: categoria });
  }

  setMostrarSoloFavoritos(mostrar: boolean): void {
    const state = this.filterState.value;
    this.filterState.next({ ...state, mostrarSoloFavoritos: mostrar });
  }

  setOrdenActivo(orden: string): void {
    const state = this.filterState.value;
    this.filterState.next({ ...state, ordenActivo: orden });
  }

  reset(): void {
    this.filterState.next({
      searchQuery: '',
      categoriaActiva: 'Todos',
      mostrarSoloFavoritos: false,
      ordenActivo: 'nombre'
    });
  }

  /**
   * Aplica los filtros a la lista de productos.
   */
  applyFilters(productos: Producto[], favoritosIds: Set<number>): Producto[] {
    const state = this.filterState.value;
    let result = [...productos];

    if (state.categoriaActiva !== 'Todos') {
      result = result.filter(p => p.categoria === state.categoriaActiva);
    }
    if (state.searchQuery.trim()) {
      const q = state.searchQuery.toLowerCase();
      result = result.filter(p =>
        p.nombre.toLowerCase().includes(q) || p.descripcion?.toLowerCase().includes(q)
      );
    }
    if (state.mostrarSoloFavoritos) {
      result = result.filter(p => favoritosIds.has(p.id));
    }

    // Ordenar
    switch (state.ordenActivo) {
      case 'precio_asc':
        result.sort((a, b) => (a.precio || 0) - (b.precio || 0));
        break;
      case 'precio_desc':
        result.sort((a, b) => (b.precio || 0) - (a.precio || 0));
        break;
      case 'destacado':
        result.sort((a, b) => (b.destacado ? 1 : 0) - (a.destacado ? 1 : 0));
        break;
      default:
        result.sort((a, b) => a.nombre.localeCompare(b.nombre));
    }

    return result;
  }
}
