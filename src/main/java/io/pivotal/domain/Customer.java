package io.pivotal.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.Region;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "customer")
@Region(name = "customer")
@EqualsAndHashCode(of = "id")
@ToString
@RequiredArgsConstructor(staticName = "id")
@AllArgsConstructor

public class Customer implements Serializable {

	@Id
	@javax.persistence.Id
	private String id;
	@Getter
	private String name;
	@Getter
	private String email;
	@Getter
	private String address;
	@Getter
	private String birthday;

}
