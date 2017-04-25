package io.pivotal.repo.pcc;

import io.pivotal.domain.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("CustomerGemFireRepository")
public interface CustomerGemFireRepository extends CrudRepository<Customer, String> {

    <T extends Customer> T findByEmail(final String email);
}