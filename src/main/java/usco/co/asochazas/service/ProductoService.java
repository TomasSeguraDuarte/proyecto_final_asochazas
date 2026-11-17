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
import usco.co.asochazas.repository.RolRepository;
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
	
	@Autowired
	private RolRepository rolRepository;
	
	public Producto crearProducto (Long vendedorId, int precio, int stock, String nombre, String descripcion, String categoria) {
		if(nombre.isBlank()){
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Se requiere un nombre de producto");
		}
		if(descripcion.isBlank()){
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Se requiere una descripcion de producto");
		}
		if(categoria.isBlank()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Inserte una categoria valida");
		}
		if(precio <= 0){
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Precio invalido");
		}
		if(stock <= 0){
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock invalido");
		}
		
		Usuario vendedor = usuarioRepository.findById(vendedorId).orElseThrow();
		
		if(!vendedor.getRol().getRol().equals("VENDEDOR")){
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Este usuario no esta registrado como un vendedor");
		}
		if(vendedor.getEstado().getEstadoUsuario().equals("SUSPENDIDO")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Este usuario se esncuentra suspendido");
		}
		
		Producto producto = new Producto();
		producto.setNombre(nombre);
		producto.setDescripcion(descripcion);
		producto.setEstado(estadoRepository.findByEstadoProducto("ACTIVO"));
		producto.setCategoria(categoriaRepository.findByCategoria(categoria));
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
			if (estadoActual.equals("ELIMINADO")) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede editar un producto en estado ELIMINADO");
			}


			if (nombre != null && !nombre.isBlank()) {
				producto.setNombre(nombre);
			}

			if (descripcion != null && !descripcion.isBlank()) {
				producto.setDescripcion(descripcion);
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
				Categoria cat = categoriaRepository.findByCategoria(categoria);
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
		Usuario vendedor = usuarioRepository.findById(vendedorId).orElseThrow();
		Producto producto = productoRepository.findById(productoId).orElseThrow();
		
		if(vendedor.getId() != producto.getVendedor().getId()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Edicion no autorizada");
		}
		if(!vendedor.getRol().getRol().equals("VENDEDOR")){
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Este usuario no esta registrado como un vendedor");
		}
		if(vendedor.getEstado().getEstadoUsuario().equals("SUSPENDIDO")) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Este usuario se esncuentra suspendido");
		}
		
		producto.setEstado(estadoRepository.findByEstadoProducto("ELIMINADO"));
		return productoRepository.save(producto);
	}
	
	public List<Producto> listarMisProductos(Long vendedorId){
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
}
