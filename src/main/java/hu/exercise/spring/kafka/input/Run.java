package hu.exercise.spring.kafka.input;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "RUN")
@EqualsAndHashCode(of = "requestid")
public class Run {

	@Id
	//@GeneratedValue(strategy = GenerationType.UUID)
	private String requestid = UUID.randomUUID().toString();

	private String filename;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(updatable = false)
	private Date created;

	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;
	
	public Run(String filename) {
		this.filename = filename;
	}
}
