package com.reliaquest.api.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEmployeeDto {

    @NotBlank(message = "Employee Name cannot be empty")
    private String name;

    @Positive(message = "Salary of Employee should be greater than 0")
    private Integer salary;

    @Min(value = 16, message = "Age must be at least 16")
    @Max(value = 75, message = "Age must be at most 75")
    private Integer age;

    @NotBlank(message = "Title cannot be empty")
    private String title;

}
