package com.reliaquest.api.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.exception.EmployeeException;
import com.reliaquest.api.model.*;
import com.reliaquest.api.utils.ApiConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {
    @Mock
    private RestTemplateService restTemplateService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeList employeeList;
    private EmployeeResponse employeeResponse;
    private CreateEmployeeDto createEmployeeDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employeeService = new EmployeeService(objectMapper, restTemplateService);
        employeeResponse.builder().data(employee).build();
    }

    @Test
    void testGetAllEmployees_Success() {
        // Mock Employee Object
        Employee employee1 = Employee.builder().name("John Doe").id("1").salary(1000).title("Manager").email("john@company.com").build();
        Employee employee2 = Employee.builder().name("Jane Austin").id("2").salary(2000).title("Developer").email("jane@company.com").build();
        // Mock EmployeeList Object
        EmployeeList employeeList = EmployeeList.builder().data(Arrays.asList(employee1, employee2)) // Using Collections.singletonList
                .build();

        // Mock API Response
        when(restTemplateService.getAllEmployeesList(eq(ApiConstants.GET_EMPLOYEE_URL), eq(HttpMethod.GET), eq(EmployeeList.class))
        ).thenReturn(new ResponseEntity<>(employeeList, HttpStatus.OK));

        // Call the Service Method
        List<Employee> result = employeeService.getAllEmployees();

        // Assertions
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Austin", result.get(1).getName());
    }

    @Test
    void testGetAllEmployees_HttpClientErrorException_ThrowsException() {
        when(restTemplateService.getAllEmployeesList(anyString(), any(), eq(EmployeeList.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        EmployeeException exception = assertThrows(EmployeeException.class, () -> employeeService.getAllEmployees());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testGetEmployeeById_Success() {
        EmployeeResponse employeeResponse = EmployeeResponse.builder().data(Employee.builder().id("1").name("John Doe").salary(1000)
                        .title("Manager").email("john@company.com").build()).build();

        // Mock API Call
        when(restTemplateService.getEmployeeById(
                anyString(), any(), eq(EmployeeResponse.class), any()))
                .thenReturn(new ResponseEntity<>(employeeResponse, HttpStatus.OK));

        // Call Service Method
        Employee result = employeeService.getEmployeeById("1");

        // Assertions
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("1", result.getId());
    }

    @Test
    void testGetEmployeeById_NotFound_ThrowsException() {
        when(restTemplateService.getEmployeeById(anyString(), any(), eq(EmployeeResponse.class), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // Assert that EmployeeException is thrown
        EmployeeException exception = assertThrows(EmployeeException.class, () -> employeeService.getEmployeeById("1"));

        // Validate Exception Details
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testCreateEmployee_Success() {

        CreateEmployeeDto createEmployeeDto = CreateEmployeeDto.builder().name("John Doe").salary(1000).age(32).title("Manager").build();
        // Initialize Employee Response
        EmployeeResponse employeeResponse = EmployeeResponse.builder().data(Employee.builder().id("1").name("John Doe").salary(1000)
                        .title("Manager").email("john@company.com").build()).build();

        // Mock objectMapper conversion
        when(objectMapper.convertValue(any(), eq(CreateEmployeeDto.class))).thenReturn(createEmployeeDto);

        // Mock API call
        when(restTemplateService.createEmployee(anyString(), any(), any(), eq(EmployeeResponse.class)))
                .thenReturn(new ResponseEntity<>(employeeResponse, HttpStatus.OK));

        // Call the method under test
        Employee result = employeeService.createEmployee(new Object());

        // Assertions
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testCreateEmployee_InvalidInput_ThrowsException() {
        when(objectMapper.convertValue(any(), eq(CreateEmployeeDto.class)))
                .thenThrow(new IllegalArgumentException("Invalid input"));

        EmployeeException exception = assertThrows(EmployeeException.class, () -> employeeService.createEmployee(new Object()));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testDeleteEmployee_Success() {
        EmployeeResponse employeeResponse = new EmployeeResponse();
        Employee employee = new Employee();
        employee.setName("John Doe");
        employeeResponse.setData(employee);

        // Mock DeleteEmployeeResponse
        DeleteEmployeeResponse deleteResponse = new DeleteEmployeeResponse();
        deleteResponse.setFlag(true);  // Ensure name matches expected output

        // Mock API Calls
        when(restTemplateService.getEmployeeById(anyString(), any(), eq(EmployeeResponse.class), any()))
                .thenReturn(new ResponseEntity<>(employeeResponse, HttpStatus.OK));

        when(restTemplateService.deleteEmployeeByName(anyString(), any(), any()))
                .thenReturn(new ResponseEntity<>(deleteResponse, HttpStatus.OK));

        // Call method
        String result = employeeService.deleteEmployee("1");

        // Assertions
        assertNotNull(result);
        assertEquals("John Doe", result); // Ensure returned value matches expected
    }

    @Test
    void testDeleteEmployee_NotFound_ThrowsException() {
        when(restTemplateService.getEmployeeById(anyString(), any(), eq(EmployeeResponse.class), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        EmployeeException exception = assertThrows(EmployeeException.class, () -> employeeService.deleteEmployee("1"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
