package io.pivotal.controller;

import java.util.ArrayList;
import java.util.List;

import com.gemstone.gemfire.cache.Region;
import io.pivotal.service.CustomerOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.codearte.jfairy.Fairy;
import io.codearte.jfairy.producer.person.Person;
import io.pivotal.domain.Customer;
import io.pivotal.service.CustomerSearchService;

import javax.annotation.Resource;

@RestController
public class CustomerController {
	
	@Autowired
	CustomerSearchService customerSearchService;

	@Autowired
	CustomerOperationService customerOperationService;
	
	Fairy fairy = Fairy.create();

	@Resource(name = "customer")
	Region<String, Customer> customerRegion;
	
	
	@RequestMapping("/")
	public String home() {
		return "Customer Search Service -- Available APIs: <br/>"
				+ "<br/>"
				+ "GET /showcache    	               - get all customer info in PCC<br/>"
				+ "GET /clearcache                     - remove all customer info in PCC<br/>"
				+ "GET /showdb  	                   - get all customer info in MySQL<br/>"
				+ "GET /cleardb                        - remove all customer info in MySQL<br/>"
				+ "GET /loaddb                         - generate 500 customer info into MySQL<br/>"
				+ "GET /loadcachefromdb                - load 500 customer info from mysql into PCC<br/>"
				+ "GET /customerSearch?email={email}   - get specific customer info<br/>";
	}

	@RequestMapping(method = RequestMethod.GET, path = "/showcache")
	@ResponseBody
	public String show() throws Exception {
		StringBuilder result = new StringBuilder();

		customerOperationService.getAllCustomerFromPcc().forEach(item->result.append(item+"<br/>"));

		return result.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/clearcache")
	@ResponseBody
	public String clearCache() throws Exception {
		//customerOperationService.deleteAllfromPcc();
		customerRegion.removeAll(customerRegion.keySetOnServer());
		return "Region cleared";
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/showdb")
	@ResponseBody
	public String showDB() throws Exception {
		StringBuilder result = new StringBuilder();

		customerOperationService.getAllCustomerFromJpa().forEach(item->result.append(item+"<br/>"));
		
		return result.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/loaddb")
	@ResponseBody
	public String loadDB() throws Exception {
		
		List<Customer> customers = new ArrayList<>();
		
		for (int i=0; i<500; i++) {
			Person person = fairy.person();
			Customer customer = new Customer(person.passportNumber(), person.fullName(), person.email(), person.getAddress().toString(), person.dateOfBirth().toString());
			customers.add(customer);
		}

		customerOperationService.saveCustomersToDb(customers);

		return "New 500 customers successfully saved into Database";
	}


	@RequestMapping(method = RequestMethod.GET, path = "/loadcachefromdb")
	@ResponseBody
	public String loadCacheFromDB() throws Exception {

		Iterable<Customer> customers = customerOperationService.getAllCustomerFromJpa();

		customerOperationService.saveCustomersToPCC(customers);

		return "New 500 customers successfully saved into cache";
	}
	
	@RequestMapping(method = RequestMethod.GET, path = "/cleardb")
	@ResponseBody
	public String clearDB() throws Exception {

		customerOperationService.deleteAllFromJpa();
		
		return "Database cleared";
	}
	
	@RequestMapping(value = "/customerSearch", method = RequestMethod.GET)
	public String searchCustomerByEmail(@RequestParam(value = "email", required = true) String email) {
		
		long startTime = System.currentTimeMillis();
		Customer customer = customerSearchService.getCustomerByEmail(email);
		long elapsedTime = System.currentTimeMillis();
		Boolean isCacheMiss = customerSearchService.isCacheMiss();
		String sourceFrom = isCacheMiss ? "MySQL" : "PCC";

		return String.format("Result [<b>%1$s</b>] <br/>"
				+ "Cache Miss [<b>%2$s</b>] <br/>"
				+ "Read from [<b>%3$s</b>] <br/>"
				+ "Elapsed Time [<b>%4$s ms</b>]%n", customer, isCacheMiss, sourceFrom, (elapsedTime - startTime));
	}
	
}
