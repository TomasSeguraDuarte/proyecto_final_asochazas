package usco.co.asochazas.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class SolicitudVendedor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "fecha_envio",  updatable = false ,columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime fecha_envio;
	
	@Column(name = "fecha_revision", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime fecha_registro = null;
    
	@Column(length = 500)
	private String justificacion_admin;
	
	@Column(nullable = false, length = 100)
	private String propuesta_nombre;
	
	@Column(nullable = false, length = 1000)
	private String propuesta_descripcion;
	
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario_id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "revisado_por")
    private Usuario revisado_por = null;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_solicitud", nullable=false)
    private EstadoSolicitud estado;
}
