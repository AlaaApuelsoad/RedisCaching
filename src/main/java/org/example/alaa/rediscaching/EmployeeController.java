package org.example.alaa.rediscaching;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;


    @PostMapping("/load")
    public String loadEmployeesData(){
        employeeService.generateFakeEmployee();
        return "Employee loaded";
    }

    @GetMapping("/{id}")
    public Employee getEmployee(@PathVariable long id) {
        return employeeService.getEmployeeById(id);
    }

    @GetMapping("/all")
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/search")
    public List<Employee> searchEmployees(@RequestParam("name") String name) {
        return employeeService.findEmployeesByName(name);
    }

    @PostMapping("/update/{id}")
    public Employee updateEmployee(@PathVariable long id, @RequestBody Employee employeeReq) {
        return employeeService.updateEmployee(id,employeeReq);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable long id) {
        return employeeService.deleteEmployeeById(id);
    }

}
