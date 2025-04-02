package com.reliaquest.api.service;

import com.reliaquest.api.model.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestTemplateService {

    private  RestTemplate restTemplate;

    public RestTemplateService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<EmployeeList> getAllEmployeesList(String baseUrl, HttpMethod httpMethod,
                                                            Class<EmployeeList> employeeClass) {
        return restTemplate.exchange(baseUrl, httpMethod, null, employeeClass);
    }

    public ResponseEntity<EmployeeResponse> getEmployeeById(String baseUrl, HttpMethod httpMethod,
                                                            Class<EmployeeResponse> employeeClass,
                                                            String employeeId) {
        return restTemplate.exchange(baseUrl, httpMethod, null, employeeClass, employeeId);
    }

    public ResponseEntity<DeleteEmployeeResponse> deleteEmployeeByName(String baseUrl, HttpMethod httpMethod,
                                                                       HttpEntity<DeleteEmployeeInput>
                                                                         deleteEmployeeInputEntity) {
      return restTemplate.exchange(baseUrl, httpMethod, deleteEmployeeInputEntity, DeleteEmployeeResponse.class);
    }

    public ResponseEntity<EmployeeResponse> createEmployee(String baseUrl, HttpMethod httpMethod,
                                                           HttpEntity<CreateEmployeeDto> employee,
                                                           Class<EmployeeResponse> employeeResponseClass) {
        return restTemplate.exchange(baseUrl, httpMethod, employee, employeeResponseClass);
    }

}
