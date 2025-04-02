package com.reliaquest.api.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Comparator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.exception.EmployeeException;
import com.reliaquest.api.model.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.reliaquest.api.utils.ApiConstants;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@Service
@Slf4j
public class EmployeeService implements IEmployeeService {

	private final RestTemplateService restTemplateService;

	private ObjectMapper objectMapper;

	public EmployeeService(ObjectMapper objectMapper, RestTemplateService restTemplateService) {
		this.objectMapper = objectMapper;
		this.restTemplateService = restTemplateService;
	}

	@Override
	public List<Employee> getAllEmployees() {
		log.debug("EmployeeService -> getAllEmployeeList -> Entry");
		ResponseEntity<EmployeeList> employeeResponseEntity;
		try {
			log.info(String.valueOf(restTemplateService));
			employeeResponseEntity = restTemplateService.getAllEmployeesList(
					ApiConstants.GET_EMPLOYEE_URL, HttpMethod.GET, EmployeeList.class);
			log.info("Response of Request :{} ", employeeResponseEntity.getBody());

			return employeeResponseEntity.getBody().getData();
		} catch(HttpClientErrorException e) {
			e.getStatusCode().value();
			throw new EmployeeException(HttpStatus.NOT_FOUND,"Employees List Not Found");
		} catch (ResourceAccessException e) {
			throw new EmployeeException(HttpStatus.SERVICE_UNAVAILABLE,"Failed to connect to employee service");
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new EmployeeException(HttpStatus.INTERNAL_SERVER_ERROR,"An unexpected  error occurred");
		}
	}

	@Override
	public List<Employee> getEmployeesByNameSearch(String employeeName) {

		log.debug("Searching employees by name :{} ", employeeName);
		List<Employee> employeeList = getAllEmployees().stream().filter(
				employee -> employee.getName().toLowerCase()
						.contains(employeeName.toLowerCase())).collect(Collectors.toList());

		if(employeeList.isEmpty()) {
			log.error("Employee with name {} not found", employeeName);
			throw new EmployeeException(HttpStatus.NOT_FOUND,"Employee with name " + employeeName + " not found");
		}
		log.debug("Found {} employees matching '{}'", employeeList.size(), employeeName);
		return employeeList;
	}

	@Override
	public Employee getEmployeeById(String id) {
		log.debug("Fetching employee with ID: {}", id);
		try {
			ResponseEntity<EmployeeResponse> employeeResponseEntity = restTemplateService.
					getEmployeeById(ApiConstants.GET_EMPLOYEE_ID_URL, HttpMethod.GET,
							EmployeeResponse.class,id);
			log.info("Response of Request :{} ", employeeResponseEntity.getBody().getData());
			return employeeResponseEntity.getBody().getData();
		} catch(HttpClientErrorException e) {
			throw new EmployeeException(HttpStatus.NOT_FOUND,"Employee with ID " + id + " not found");
		} catch (ResourceAccessException e) {
			throw new EmployeeException(HttpStatus.SERVICE_UNAVAILABLE,"Failed to connect to employee service");
		} catch (Exception e) {
			throw new EmployeeException(HttpStatus.INTERNAL_SERVER_ERROR,"An unexpected  error occurred");
		}
	}

	@Override
	public Integer getHighestSalaryOfEmployees() {
		log.debug("Fetching highest salary of employees...");

		int highestSalary = getAllEmployees().stream().mapToInt(Employee::getSalary).max()
				.orElse(0);
		log.debug("Highest salary found: {}", highestSalary);

		if(highestSalary == 0) {
			log.error("Highest salary not found");
			throw new EmployeeException(HttpStatus.NOT_FOUND,"Employee with highest salary not found");
		}
		return highestSalary;
	}

	@Override
	public List<String> getTopTenHighestEarningEmployeeNames() {
		log.debug("Fetching top 10 highest earning employees...");

		List<String> topTenEmployees = getAllEmployees().stream()
				.sorted(Comparator.comparingInt(Employee::getSalary).reversed())
				.limit(10).map(Employee::getName).collect(Collectors.toList());

		if(topTenEmployees.isEmpty()) {
			log.error("Top 10 highest earning employee list not found");
			throw new EmployeeException(HttpStatus.NOT_FOUND,"Top 10 highest earning employee list not found");
		}
		log.debug("Top 10 highest earning employees: {}", topTenEmployees);
		return topTenEmployees;
	}

	@Override
	public String deleteEmployee(String id) {
		log.debug("EmployeeService -> deleteEmployee -> Entry");
		Employee employee = getEmployeeById(id);

		try {
			DeleteEmployeeInput deleteEmployeeInput = new DeleteEmployeeInput();
			deleteEmployeeInput.setName(employee.getName());
			HttpEntity<DeleteEmployeeInput> requestEntity = new HttpEntity<>(deleteEmployeeInput);
			ResponseEntity<DeleteEmployeeResponse> employeeResponseEntity =
					restTemplateService.deleteEmployeeByName(ApiConstants.GET_EMPLOYEE_URL,
							HttpMethod.DELETE, requestEntity);
			if (employeeResponseEntity.getStatusCode() == HttpStatus.OK) {
				log.debug("Deleted employee with ID {}:", id);
				return employee.getName();
			} else {
				log.error("Failed to delete the Employee Record {}", id);
				throw new EmployeeException(HttpStatus.BAD_REQUEST,"Failed to delete the Employee Record "+id);
			}
		} catch (ResourceAccessException e) {
			throw new EmployeeException(HttpStatus.SERVICE_UNAVAILABLE,"Failed to connect to employee service");
		} catch (Exception e) {
			throw new EmployeeException(HttpStatus.INTERNAL_SERVER_ERROR,"An unexpected  error occurred");
		}
	}

	@Override
	public Employee createEmployee(Object employeeInput) {
		CreateEmployeeDto employee = new CreateEmployeeDto();

		try {
			employee = objectMapper.convertValue(employeeInput, CreateEmployeeDto.class);
			String valid = validateNewEmployeeRequest(employee);
			if(!valid.isEmpty()) {
				log.error("Validation failed");
				throw new EmployeeException(HttpStatus.BAD_REQUEST,"Validation failed:- "+valid);
			}
		} catch (IllegalArgumentException e) {
			log.error("Invalid Employee Input:");
			throw new EmployeeException(HttpStatus.BAD_REQUEST, "Invalid Employee Input: " + e.getMessage());
		}
		try {
			log.debug("Calling service to create an employee");

			HttpEntity<CreateEmployeeDto> requestEntity = new HttpEntity<>(employee);
			ResponseEntity<EmployeeResponse> employeeResponseEntity = restTemplateService.createEmployee(
					ApiConstants.GET_EMPLOYEE_URL, HttpMethod.POST, requestEntity, EmployeeResponse.class);

			log.debug("Response of Request :{} ", employeeResponseEntity.getBody().getData());
			return employeeResponseEntity.getBody().getData();

		} catch (ResourceAccessException e) {
			throw new EmployeeException(HttpStatus.SERVICE_UNAVAILABLE,"Failed to connect to employee service");
		} catch (Exception e) {
			throw new EmployeeException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected  error occurred");
		}
	}

	private String validateNewEmployeeRequest(CreateEmployeeDto employee) {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
		Set<ConstraintViolation<CreateEmployeeDto>> violations = validator.validate(employee);

		if (!violations.isEmpty()) {
			StringBuilder errors = new StringBuilder();
			for (ConstraintViolation<CreateEmployeeDto> violation : violations) {
				errors.append(violation.getMessage()).append("; ");
			}
			return errors.toString();
		}
		return "";
	}

}
