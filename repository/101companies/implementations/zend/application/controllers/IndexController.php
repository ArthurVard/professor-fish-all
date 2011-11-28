<?php

class IndexController extends Zend_Controller_Action
{

    public function init()
    {
        /* Initialize action controller here */
    }

    public function indexAction()
    {
        $form = new Application_Form_Company();
        $form->setAction('index/company');
        $this->view->form = $form;
        
        $company = new Application_Model_DbTable_Company();
        $employee = new Application_Model_DbTable_Employee();
        
        $id = 1;
        
        $c = $company->getCompany($id);
        $c[total] = $employee->getTotalForCompany($id);

        $form->populate($c);
        $form->fillLists($id);
    }

    public function companyAction()
    {
        $form = new Application_Form_Company();
        $form->setAction('index/company');
        $this->view->form = $form;
        
        $company = new Application_Model_DbTable_Company();
        $employee = new Application_Model_DbTable_Employee();
        $department = new Application_Model_DbTable_Department();
        
        if($this->getRequest()->isPost()) {
            $formData = $this->getRequest()->getPost();
            
            if ($form->isValid($formData) && $form->save->isChecked()) {
                $id = (int)$form->getValue('id');
                $name = $form->getValue('name');
                $company->updateCompany($id, $name);
                $this->_helper->redirector('index');
            } else if ($form->isValid($formData) && $form->cut->isChecked()) {
                $id = (int)$form->getValue('id');
                $employee->cutCompany($id);
                $this->_helper->redirector('index');
            } else if ($form->isValid($formData) && $form->select->isChecked()) {
                $depId = (int)$form->getValue('departments');
                
                $this->_helper->redirector('department', 'index', null, array('id' => $depId));
            }
        } else {
            $id = 1;
        
            $c = $company->getCompany($id);
            $c[total] = $employee->getTotalForCompany($id);

            $form->populate($c);
            $form->fillLists($id);
        }
    }
    
    public function departmentAction()
    {
        $department = new Application_Model_DbTable_Department();
        $form = new Application_Form_Department();
        $form->setAction('index/department');
        $this->view->form = $form;
        
        if($this->getRequest()->isPost()) {
            $formData = $this->getRequest()->getPost();
            $form->isValid($formData);
            
            if ($form->isValid($formData) && $form->save->isChecked()) {
                $id = (int)$form->getValue('id');
                $name = $form->getValue('name');
                $department->updateDepartment($id, $name);
                $this->_helper->redirector('department', 'index', null, array('id' => $id));
            } else if ($form->isValid($formData) && $form->cut->isChecked()) {
                $id = (int)$form->getValue('id');
                //TODO
                $this->_helper->redirector('department', 'index', null, array('id' => $id));
            } else if ($form->isValid($formData) && $form->selectDepartment->isChecked()) {
                $depId = (int)$form->getValue('departments');
                
                $this->_helper->redirector('department', 'index', null, array('id' => $depId));
            } else if ($form->isValid($formData) && $form->selectEmployee->isChecked()) {
                $empId = (int)$form->getValue('employees');
                
                $this->_helper->redirector('employee', 'index', null, array('id' => $empId));
            }
        } else {
            $id = $this->_getParam('id', 0);
            
            $d = $department->getDepartment($id);
            $d[total] = $employee->getTotalForDepartment($id);
            
            $form->populate($d);
            $form->fillLists($id);
        }
        
        
    }

    

    public function employeeAction()
    {
        // action body
    }


}













