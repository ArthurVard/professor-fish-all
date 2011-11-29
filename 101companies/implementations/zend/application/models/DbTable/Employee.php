<?php

class Application_Model_DbTable_Employee extends Zend_Db_Table_Abstract
{

    protected $_name = 'employee';
    
    public function getEmployeesForDepartment($id) {
        $id = (int)$id;
        $rows = $this->fetchAll('did = ' . $id);
        return $rows->toArray();
    }
    
    public function getTotalForCompany($id) {
        $id = (int)$id;
        $rows = $this->fetchAll('cid = ' . $id);
        $total = 0;
        foreach ($rows as $row) {
            $total += $row->salary;
        }
        return $total;
    }
    
        public function getTotalForDepartment($id) {
        $id = (int)$id;
        $rows = $this->fetchAll('did = ' . $id);
        $total = 0;
        foreach ($rows as $row) {
            $total += $row->salary;
        }
        $department = new Application_Model_DbTable_Department();
        $subdepartments = $department->getDepartmentsForDepartment($id);
        foreach ($subdepartments as $dep) {
            $depId = $dep[id];
            $total += $this->getTotalForDepartment($depId);
        }
        return $total;
    }
    
    public function cutCompany($id) {
        $this->_db->query("UPDATE employee SET salary = salary / 2 WHERE cid = " .$id);
    }
    
    public function getManagerForDepartment($id) {
        $id = (int)$id;
        $row = $this->fetchRow('did = ' . $id . ' AND manager = 1');
        return $row->toArray();
    }
}

