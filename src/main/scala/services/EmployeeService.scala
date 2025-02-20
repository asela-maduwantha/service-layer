package services

import models.Employee
import repositories.EmployeeRepository

import scala.concurrent.Future

class EmployeeService(employeeRepository: EmployeeRepository) {
  def addEmployee(employee: Employee): Future[Int] = {
    employeeRepository.create(employee)
  }

  def getEmployeeById(id: Int): Future[Option[Employee]] = {
    employeeRepository.getById(id)
  }

  def updateEmployee(employee: Employee) : Future[Int] = {
    employeeRepository.update(employee)
  }

  def deleteEmployeeById(id: Int): Future[Int] = {
    employeeRepository.delete(id)
  }

}