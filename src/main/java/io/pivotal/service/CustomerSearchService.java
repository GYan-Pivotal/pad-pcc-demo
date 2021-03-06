package io.pivotal.service;

import io.pivotal.domain.Customer;
import io.pivotal.repo.jpa.CustomerJpaRepository;
import io.pivotal.repo.pcc.CustomerGemFireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;


@Service
public class CustomerSearchService {

    @Autowired
    CustomerGemFireRepository customerGemFireRepository;

    @Autowired
    CustomerJpaRepository customerJpaRepository;

    @PostConstruct
    public void init() {
        Assert.notNull(this.customerGemFireRepository,
                "A reference to the 'customerGemFireRepository' was not properly configured!");
        Assert.notNull(this.customerJpaRepository,
                "A reference to the 'customerJpaRepository' was not properly configured!");
        System.out.printf("%1$s initialized!%n", getClass().getSimpleName());
    }

    private volatile boolean cacheMiss = false;

    public boolean isCacheMiss() {
        boolean isCacheMiss = this.cacheMiss;
        this.cacheMiss = false;
        return isCacheMiss;
    }

    protected void setCacheMiss() {
        this.cacheMiss = true;
    }

    @Cacheable(value = "customer")
    public Customer getCustomerByEmail(String email) {

        Customer customer = customerGemFireRepository.findByEmail(email);

        if (customer == null) {
            //if can't get from pcc, then get from db and save to pcc
            setCacheMiss();
            customer = customerJpaRepository.findByEmail(email);
            customerGemFireRepository.save(customer);
        }

        return customer;
    }

}
