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
        $department = new Application_Model_DbTable_Department();
        
        $id = 1;
        
        $c = $company->getCompany($id);
        $c[total] = $employee->getTotalForCompany($id);

        $form->populate($c);
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
            
            $id = (int)$form->getValue('id');
            $this->_helper->redirector('department', 'index', null, array('id' => $id));
        } else {
            $id = $this->_getParam('id', 0);
            
            $d = $department->getDepartment($id);
            $form->populate($d); 
        }
        
        
    }

    

    public function employeeAction()
    {
        // action body
    }


}













