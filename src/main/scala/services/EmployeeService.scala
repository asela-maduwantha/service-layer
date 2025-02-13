package services

import models.Employee
import repositories.EmployeeRepository
import scala.concurrent.{Future, ExecutionContext}

class EmployeeService(employeeRepository: EmployeeRepository)(implicit ec: ExecutionContext) {

  def addEmployee(employee: Employee): Future[Int] = {
    employeeRepository.create(employee)
  }

}
