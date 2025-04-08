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
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final Faker faker;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private CacheManager cacheManager;

    @PostConstruct
    public void logCache(){
        System.out.println("Cache Manager: "+cacheManager.getClass().getName());
    }

    @Transactional(rollbackFor = Exception.class)
    public void generateFakeEmployee() {

        List<Employee> employees = IntStream.range(0,500000)
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


    public ResponseEntity<InputStreamResource> exportToCSV(){
        List<Employee> employees = employeeRepository.findAll();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(outputStream)){
            writer.print("ID,Name,Surname");

            for (Employee employee : employees){
                writer.printf("%d,%s,%s%n", employee.getId(), employee.getName(), employee.getSurname());
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition","attachment; filename=employees.csv");

        return ResponseEntity.ok()
                .headers(headers).
                body(new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray())));
    }


    public ResponseEntity<Resource> exportToCSVEnhanced() throws IOException {

        Path tempFile = Files.createTempFile("employee-", ".csv");

        System.out.println("Temporary file location: "+tempFile.toString());

        try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
            writer.write("ID,Name,Surname\n");

            jdbcTemplate.query("SELECT emp_id, name, surname FROM employee", rs -> {
                while (rs.next()) {
                    long id = rs.getLong("emp_id");
                    String name = rs.getString("name");
                    String surname = rs.getString("surname");

                    try {
                        writer.write(String.format("%d,%s,%s%n", id, name, surname));
                    } catch (IOException e) {
                        throw new RuntimeException("Error writing to CSV file", e);
                    }
                }
            });
        }

        String headerValue = "attachment;" + "filename=employees_"+System.currentTimeMillis()+".csv";

        Resource resource = new UrlResource(tempFile.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

}
