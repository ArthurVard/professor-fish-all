﻿namespace csharpBaseline.CompanyModel
{
    public class Employee
    {
        public Employee() {}
        public Employee(Person person)
        {
            Person = person;
        }
        public decimal Salary { get; set; }
        public Person Person { get; set; }

        public override bool Equals(object obj)
        {
            if (GetType() != obj.GetType()) return false;

            var c1 = obj as Employee;
            if (c1 == null) return false;

            return ((c1.Salary == Salary) && (c1.Person.Equals(Person)));
        }

        public override int GetHashCode()
        {
            unchecked
            {
                return (Salary.GetHashCode() * 397) ^ (Person != null ? Person.GetHashCode() : 0);
            }
        }
    }
}
