package com.reliaquest.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;

@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeController implements IEmployeeController<Employee, Object> {

	private final EmployeeService employeeService;

	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@Override
	@GetMapping()
	public ResponseEntity<List<Employee>> getAllEmployees() {
		return new ResponseEntity<>(employeeService.getAllEmployees(), HttpStatus.OK);
	}

	@Override
	@GetMapping("/search/{searchString}")
	public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
		return new ResponseEntity<>(employeeService.getEmployeesByNameSearch(searchString), HttpStatus.OK);
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<Employee> getEmployeeById(String id) {
		return new ResponseEntity<>(employeeService.getEmployeeById(id), HttpStatus.OK);
	}

	@Override
	@GetMapping("/highestSalary")
	public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
		return new ResponseEntity<>(employeeService.getHighestSalaryOfEmployees(), HttpStatus.OK);
	}

	@Override
	@GetMapping("/topTenHighestEarningEmployeesName")
	public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
		return new ResponseEntity<>(employeeService.getTopTenHighestEarningEmployeeNames(), HttpStatus.OK);
	}

	@Override
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteEmployeeById(String id) {
		return new ResponseEntity<>(employeeService.deleteEmployee(id), HttpStatus.OK);
	}

	@Override
	@PostMapping()
	public ResponseEntity<Employee> createEmployee(@RequestBody Object employeeInput) {
		return new ResponseEntity<>(employeeService.createEmployee(employeeInput), HttpStatus.OK);
	}

}
