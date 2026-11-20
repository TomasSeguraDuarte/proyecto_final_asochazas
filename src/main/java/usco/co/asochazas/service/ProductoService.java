package usco.co.asochazas.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import usco.co.asochazas.model.Categoria;
import usco.co.asochazas.model.Producto;
import usco.co.asochazas.model.Usuario;
import usco.co.asochazas.repository.CategoriaRepository;
import usco.co.asochazas.repository.EstadoProductoRepository;
import usco.co.asochazas.repository.ProductoRepository;
import usco.co.asochazas.repository.UsuarioRepository;

@Service
public class ProductoService {

	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private EstadoProductoRepository estadoRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	public Producto crearProducto(Long vendedorId, int precio, int stock, String nombre, String descripcion, String categoria) {
		if (nombre == null || nombre.isBlank()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Se requiere un nombre de producto");
		}
		if (descripcion == null || descripcion.isBlank()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Se requiere una descripcion de producto");
		}
		if (categoria == null || categoria.isBlank()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Inserte una categoria valida");
		}
		if (precio <= 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Precio invalido");
		}
		if (stock <= 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock invalido");
		}

		Usuario vendedor = usuarioRepository.findById(vendedorId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

		if (!vendedor.getRol().getRol().equals("VENDEDOR")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Este usuario no esta registrado como un vendedor");
		}
		if (vendedor.getEstado().getEstadoUsuario().equals("SUSPENDIDO")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Este usuario se encuentra suspendido");
		}

		Categoria cat = categoriaRepository.findByCategoria(categoria.trim());
		if (cat == null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Inserte una categoria valida");
		}

		Producto producto = new Producto();
		producto.setNombre(nombre.trim());
		producto.setDescripcion(descripcion.trim());
		producto.setEstado(estadoRepository.findByEstadoProducto("ACTIVO"));
		producto.setCategoria(cat);
		producto.setFecha_publicacion(LocalDateTime.now());
		producto.setPrecio(precio);
		producto.setStock(stock);
		producto.setVendedor(vendedor);
		return productoRepository.save(producto);
	}

	public Producto editarProducto(Long productoId, Long vendedorId, Integer precio, Integer stock,
            String nombre, String descripcion, String categoria, String estado) {

		Usuario vendedor = usuarioRepository.findById(vendedorId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
		Producto producto = productoRepository.findById(productoId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

		if (vendedor.getId() != producto.getVendedor().getId()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Edicion no autorizada");
		}
		if (!vendedor.getRol().getRol().equals("VENDEDOR")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Este usuario no esta registrado como un vendedor");
		}
		if (vendedor.getEstado().getEstadoUsuario().equals("SUSPENDIDO")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Este usuario se encuentra suspendido");
		}

		String estadoActual = producto.getEstado().getEstadoProducto();
		if (estadoActual.equals("ELIMINADO") || estadoActual.equals("OCULTO")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede editar un producto en estado " + estadoActual);
		}

		if (nombre != null && !nombre.isBlank()) {
			producto.setNombre(nombre.trim());
		}

		if (descripcion != null && !descripcion.isBlank()) {
			producto.setDescripcion(descripcion.trim());
		}

		if (precio != null) {
			if (precio <= 0) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Precio invalido");
			}
			producto.setPrecio(precio);
		}

		if (stock != null) {
			if (stock < 0) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock invalido");
			}
			producto.setStock(stock);
		}

		if (categoria != null && !categoria.isBlank()) {
			Categoria cat = categoriaRepository.findByCategoria(categoria.trim());
			if (cat == null) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Inserte una categoria valida");
			}
			producto.setCategoria(cat);
		}

		if (estado != null && !estado.isBlank()) {
			if (!estado.equals("ACTIVO") && !estado.equals("PAUSADO")) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Estado invalido: solo se permite ACTIVO o PAUSADO");
			}
			producto.setEstado(estadoRepository.findByEstadoProducto(estado));
		}

		return productoRepository.save(producto);
	}

	public Producto eliminarProducto(Long productoId, Long vendedorId) {
		Usuario vendedor = usuarioRepository.findById(vendedorId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
		Producto producto = productoRepository.findById(productoId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

		if (vendedor.getId() != producto.getVendedor().getId()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Edicion no autorizada");
		}
		if (!vendedor.getRol().getRol().equals("VENDEDOR")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Este usuario no esta registrado como un vendedor");
		}
		if (vendedor.getEstado().getEstadoUsuario().equals("SUSPENDIDO")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Este usuario se encuentra suspendido");
		}

		producto.setEstado(estadoRepository.findByEstadoProducto("ELIMINADO"));
		return productoRepository.save(producto);
	}

	public List<Producto> listarMisProductos(Long vendedorId) {
		return productoRepository.findByVendedor_IdAndEstado_EstadoProductoNot(vendedorId, "ELIMINADO");
	}

	public List<Producto> listarCatalogo(String categoria, String busqueda) {
		if (categoria != null && !categoria.isBlank()) {
			return productoRepository.findByEstado_EstadoProductoAndStockGreaterThanAndCategoria_Categoria(
					"ACTIVO", 0, categoria.trim());
		}
		if (busqueda != null && !busqueda.isBlank()) {
			return productoRepository.findByEstado_EstadoProductoAndStockGreaterThanAndNombreContainingIgnoreCase(
					"ACTIVO", 0, busqueda.trim());
		}
		return productoRepository.findByEstado_EstadoProductoAndStockGreaterThan("ACTIVO", 0);
	}

	public Producto obtenerDetalle(Long productoId) {
		Producto producto = productoRepository.findById(productoId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

		String estado = producto.getEstado().getEstadoProducto();
		if (estado.equals("ELIMINADO") || estado.equals("OCULTO")) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no disponible");
		}
		return producto;
	}

	// moderacion de admin

	public List<Producto> listarTodosAdmin() {
		return productoRepository.findAll();
	}

	public Producto ocultarProducto(Long productoId, String justificacion) {
		if (justificacion == null || justificacion.isBlank()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Se requiere una justificacion para ocultar el producto");
		}
		Producto producto = productoRepository.findById(productoId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

		if (producto.getEstado().getEstadoProducto().equals("ELIMINADO")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El producto ya fue eliminado por el vendedor");
		}
		producto.setEstado(estadoRepository.findByEstadoProducto("OCULTO"));
		return productoRepository.save(producto);
	}

	public Producto restaurarProducto(Long productoId) {
		Producto producto = productoRepository.findById(productoId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

		if (!producto.getEstado().getEstadoProducto().equals("OCULTO")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden restaurar productos ocultos");
		}
		producto.setEstado(estadoRepository.findByEstadoProducto("ACTIVO"));
		return productoRepository.save(producto);
	}
}