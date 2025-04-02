package com.reliaquest.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.reliaquest.api.model.Employee;

@Service
public interface IEmployeeService {
	
	List<Employee> getAllEmployees();

    List<Employee> getEmployeesByNameSearch(String searchString);

    Employee getEmployeeById(String id);

    Integer getHighestSalaryOfEmployees();

    List<String> getTopTenHighestEarningEmployeeNames();
    
    String deleteEmployee(String id);
    
    Employee createEmployee(Object employee);

}
