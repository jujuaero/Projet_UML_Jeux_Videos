package fr.efrei.repository;

import fr.efrei.domain.Employee;

public interface IEmployeeRepository extends IRepository<Employee> {

    Employee findByEmail(String email);
}

