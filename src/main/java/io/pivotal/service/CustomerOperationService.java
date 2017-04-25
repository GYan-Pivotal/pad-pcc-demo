package io.pivotal.service;

import io.pivotal.domain.Customer;
import io.pivotal.repo.jpa.CustomerJpaRepository;
import io.pivotal.repo.pcc.CustomerGemFireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

/**
 * Created by gyan on 2017/4/21.
 */
@Service
public class CustomerOperationService {
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

    public void saveCustomersToDb(Iterable<Customer> customers){
        customerJpaRepository.save(customers);
    }

    public void saveCustomersToPCC(Iterable<Customer> customers){
        customerGemFireRepository.save(customers);
    }

    public void deleteAllFromJpa(){
        customerJpaRepository.deleteAll();
    }

    public Iterable<Customer> getAllCustomerFromPcc(){
        return customerJpaRepository.findAll();
    }

    public Iterable<Customer> getAllCustomerFromJpa(){
        return customerJpaRepository.findAll();
    }

    public void deleteAllfromPcc(){
        customerGemFireRepository.deleteAll();
    }
}
