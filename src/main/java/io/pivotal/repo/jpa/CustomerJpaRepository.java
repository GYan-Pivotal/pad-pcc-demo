package io.pivotal.repo.jpa;

import io.pivotal.domain.Customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("JpaCustomerRepository")
public interface CustomerJpaRepository extends JpaRepository<Customer, String> {

	<T extends Customer> T findByEmail(final String email);
}
