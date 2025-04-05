package org.example.alaa.rediscaching;

import com.github.javafaker.Faker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final Faker faker;

    @Autowired
    private CacheManager cacheManager;

    @PostConstruct
    public void logCache(){
        System.out.println("Cache Manager: "+cacheManager.getClass().getName());
    }

    @Transactional(rollbackFor = Exception.class)
    public void generateFakeEmployee() {

        List<Employee> employees = IntStream.range(0,5000)
                .mapToObj(i -> new Employee(
                        faker.name().firstName(),
                        faker.name().lastName()
                )).toList();

        employeeRepository.saveAll(employees);
    }


    @Cacheable(value = "employees_by_id", key = "#id")
    public Employee getEmployeeById(long id) {
        try {
           Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return employeeRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Employee with not found")
        );
    }

    @CachePut(value = "employees_by_id",key = "#id")
    @Caching(
            evict = {
                    @CacheEvict(value = "employees_all", allEntries = true),
                    @CacheEvict(value = "search_employee", allEntries = true),
            }
    )
    public Employee updateEmployee(long id, Employee employeeRequest) {
        Optional<Employee> employee = employeeRepository.findById(id);
        if (employee.isEmpty()){
            throw new RuntimeException("Employee not found");
        }
        employee.get().setName(employeeRequest.getName());
        employee.get().setSurname(employeeRequest.getSurname());
        return employeeRepository.save(employee.get());
    }

    @Cacheable(value = "employees_all")
    public List<Employee> getAllEmployees() {
        try {
            Thread.sleep(5000);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        return employeeRepository.findAll();
    }

//    @Cacheable(cacheNames = "search_employee",condition = "#name.length() > 4")
//    @Cacheable(value = "search_employee",key = "#name") // if you want to cache empty list to not call db for invalid data
    @Cacheable(value = "search_employee",key = "#name",unless = "#result == null or #result.isEmpty()")
    public List<Employee> findEmployeesByName(String name) {

        //testing if null values will be cached or not
        return employeeRepository.findEmployeeByName(name);
    }


    @Transactional(rollbackFor = Exception.class)
    @Caching(
            evict = {
                    @CacheEvict(value = "employees_all", allEntries = true),
                    @CacheEvict(value = "search_employee",allEntries = true),
                    @CacheEvict(value = "employees_by_id",key = "#id")
            }
    )
    public String deleteEmployeeById(long id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        if (employee.isPresent()){
            employeeRepository.deleteById(id);
        }else {
            throw new RuntimeException("Employee not found");
        }
        return "Employee deleted";
    }
}
