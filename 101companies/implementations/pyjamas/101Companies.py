import pyjd # this is dummy in pyjs.
from pyjamas.ui.RootPanel import RootPanel
from pyjamas.ui.Button import Button
from pyjamas.ui.Label import Label
from pyjamas.ui.Grid import Grid
from pyjamas.ui.TextBox import TextBox
from pyjamas.ui.ListBox import ListBox

import pygwt

# ------------------------------------------------------
# ---------------- 101Companies classes ----------------
# ------------------------------------------------------

class Company:
	def __init__(self, id = 0, name = "", departments = []):
		self.id = id
		self.name = name
		self.departments = departments
		
	def total(self):
		total = 0
		for department in self.departments:
			total += department.total()
		return total
	
	def cut(self):
		for department in self.departments:
			department.cut()
		RootPanel().add(Label("yey"))
	
	def save(self, name):
		self.name = name
		
class Department:
	def __init__(self, id = 0, name = "", departments = [], employees = []):
		self.id = id
		self.name = name
		self.departments = departments
		self.employees = employees
		
	def total(self):
		total = 0
		for employee in self.employees:
			total += employee.total()
		for department in self.departments:
			total += department.total()
		return total
	
	def cut(self):
		for employee in self.employees:
			employee.cut()
		for department in self.departments:
			department.cut()
	
	def save(self, name):
		self.name = name
		
class Employee:
	def __init__(self, id = 0, name = "", address = "", salary = 0, manager = 0):
		self.id = id
		self.name = name
		self.address = address
		self.salary = salary
		self.manager = manager
		
	def total(self):
		return self.salary
	
	def cut(self):
		self.salary = self.salary / 2
		
	def save(self, name, address, salary):
		self.name = name
		self.address = address
		self.salary = salary

# ------------------------------------------------------
# ------------------ init application ------------------
# ------------------------------------------------------
	
class CompaniesApp:
	def __init__(self):
	
		craig = Employee(1, "Craig", "Redmond", 123456, 1)
		ray = Employee(2, "Ray", "Redmond", 234567, 1)
		klaus = Employee(3, "Klaus", "Boston", 23456, 1)
		karl = Employee(4, "Karl", "Riga", 2345, 1)
		erik = Employee(5, "Erik", "Utrecht", 12345, 0)
		ralf = Employee(6, "Ralf", "Koblenz", 1234, 0)
		joe = Employee(7, "Joe", "Wifi City", 2344, 0)

		dev11 = Department(1, "Dev1.1", [], [karl, joe])
		dev1 = Department(2, "Dev1", [dev11], [klaus])
		research = Department(3, "Research", [], [craig, erik, ralf])
		development = Department(4, "Development", [dev1], [ray])
	
		self.company = Company(1, "meganalysis", [research, development])
	
class CompaniesAppGUI:
	def __init__(self):
		self.app = CompaniesApp()
		self.name = TextBox()
		self.departments = ListBox(Size=("100%"), VisibleItemCount="5")
		self.employees = ListBox(Size=("100%"), VisibleItemCount="5")
		self.total = TextBox()
		self.current = self.app.company
	
	def initCompanyGUI(self):
		formGrid = Grid(3,3)
		formGrid.setVisible(True)
		
		self.name.setText(self.app.company.name)
		self.departments.clear()
		for item in self.current.departments:
			self.departments.addItem(item.name, item.id)
		if self.departments.getItemCount() > 0:
			self.departments.setSelectedIndex(0)
		self.total.setText(self.app.company.total())
		
		# row 1
		formGrid.setWidget(0, 0, Label("Name:"))
		formGrid.setWidget(1, 0, Label("Department:"))
		formGrid.setWidget(2, 0, Label("Total:"))
		
		# row 2
		formGrid.setWidget(0, 1, self.name)
		formGrid.setWidget(1, 1, self.departments)
		formGrid.setWidget(2, 1, self.total)

		# row 3
		formGrid.setWidget(0, 2, Button("save"))
		formGrid.setWidget(1, 2, Button("select"))
		formGrid.setWidget(2, 2, Button("cut"))
		
		RootPanel().add(formGrid)
	
pyjd.setup("public/101Companies.html")
start = CompaniesAppGUI()
start.initCompanyGUI()