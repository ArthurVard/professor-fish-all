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
                
            }
        } else {
            $id = 1;
        
            $c = $company->getCompany($id);
            $c[total] = $employee->getTotalForCompany($id);
            $this->view->departments = $department->getDepartmentsForCompany($id);

            $form->populate($c);
        }
        
    }

}







