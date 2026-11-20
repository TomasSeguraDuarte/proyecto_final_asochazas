package usco.co.asochazas.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import usco.co.asochazas.model.Interaccion;
import usco.co.asochazas.model.Producto;
import usco.co.asochazas.model.Usuario;
import usco.co.asochazas.repository.EstadoInteraccionRepository;
import usco.co.asochazas.repository.InteraccionRepository;
import usco.co.asochazas.repository.ProductoRepository;
import usco.co.asochazas.repository.UsuarioRepository;

@Service
public class InteraccionService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InteraccionRepository interaccionRepository;

    @Autowired
    private EstadoInteraccionRepository estadoInteraccionRepository;

    @Transactional
    public Interaccion contactarVendedor(Long compradorId, Long productoId, int cantidad) {
        if (cantidad <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Se requiere una cantidad valida a comprar");
        }

        Usuario comprador = usuarioRepository.findById(compradorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        if (comprador.getEstado().getEstadoUsuario().equals("SUSPENDIDO")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario se encuentra suspendido");
        }
        if (comprador.getId() == producto.getVendedor().getId()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes contactar tu propio producto");
        }
        if (!producto.getEstado().getEstadoProducto().equals("ACTIVO")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El producto no esta disponible");
        }
        if (interaccionRepository.existsByUsuario_IdAndProducto_IdAndEstado_EstadoInteraccion(
                compradorId, productoId, "CONTACTADO")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya tienes un contacto abierto para este producto");
        }

        // Descuenta y reserva el stock
        int filasActualizadas = productoRepository.descontarStock(productoId, cantidad);
        if (filasActualizadas == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El stock no es suficiente");
        }

        Interaccion interaccion = new Interaccion();
        interaccion.setUsuario(comprador);
        interaccion.setProducto(producto);
        interaccion.setCantidad(cantidad);
        interaccion.setPrecio_unitario(producto.getPrecio()); // Snapshot del precio unitario al momento del pedido
        interaccion.setEstado(estadoInteraccionRepository.findByEstadoInteraccion("CONTACTADO"));
        interaccion.setFecha_contacto(LocalDateTime.now());

        return interaccionRepository.save(interaccion);
    }

    public Interaccion confirmarVenta(Long interaccionId, Long vendedorId) {
        Interaccion interaccion = interaccionRepository.findById(interaccionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interaccion no encontrada"));

        if (interaccion.getProducto().getVendedor().getId() != vendedorId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para confirmar esta venta");
        }
        if (!interaccion.getEstado().getEstadoInteraccion().equals("CONTACTADO")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta interaccion ya fue cerrada");
        }


        interaccion.setFecha_resolucion(LocalDateTime.now());
        interaccion.setEstado(estadoInteraccionRepository.findByEstadoInteraccion("VENTA_CONFIRMADA"));

        return interaccionRepository.save(interaccion);
    }

    @Transactional
    public Interaccion marcarNoConcretada(Long interaccionId, Long vendedorId) {
        Interaccion interaccion = interaccionRepository.findById(interaccionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interaccion no encontrada"));

        if (interaccion.getProducto().getVendedor().getId() != vendedorId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para cancelar esta venta");
        }
        if (!interaccion.getEstado().getEstadoInteraccion().equals("CONTACTADO")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta interaccion ya fue cerrada");
        }

        // Devuelve el stock reservado al producto
        productoRepository.devolverStock(
                interaccion.getProducto().getId(),
                interaccion.getCantidad()
        );

        interaccion.setFecha_resolucion(LocalDateTime.now());
        interaccion.setEstado(estadoInteraccionRepository.findByEstadoInteraccion("NO_CONCRETADA"));

        return interaccionRepository.save(interaccion);
    }

    public List<Interaccion> listarContactosRecibidos(Long vendedorId) {
        return interaccionRepository.findByProducto_Vendedor_Id(vendedorId);
    }

    public List<Interaccion> listarMisContactos(Long compradorId) {
        return interaccionRepository.findByUsuario_Id(compradorId);
    }
}