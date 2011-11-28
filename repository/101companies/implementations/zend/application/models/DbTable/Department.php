<?php

class Application_Model_DbTable_Department extends Zend_Db_Table_Abstract
{

    protected $_name = 'department';

    public function getDepartmentsForCompany($id) {
        $id = (int)$id;
        $rows = $this->fetchAll('cid = ' . $id . ' AND did IS NULL');
        
        $result = array();
        foreach ($rows as $row) {
            $result[] = $row->name;
        }
        return $rows->toArray();
    }
    
        public function getDepartment($id) {
        $id = (int)$id;
        $row = $this->fetchRow('id = ' . $id);
        if (!$row) {
            throw new Exception("Could not find row $id");
        }
        return $row->toArray();
    }
}

