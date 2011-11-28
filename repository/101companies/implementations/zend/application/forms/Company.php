<?php

class Application_Form_Company extends Zend_Form
{

    public function init()
    {
        $this->setName('company');
        $id = new Zend_Form_Element_Hidden('id');
        $id->addFilter('Int');
        $name = new Zend_Form_Element_Text('name');
        $name ->setLabel('Name')
                ->setRequired(true)
                ->addFilter('StripTags')
                ->addFilter('StringTrim')
                ->addValidator('NotEmpty');
        
        $submit = new Zend_Form_Element_Submit('save');
        $submit ->setAttrib('id', 'submitbutton')
                ->setOptions(array('class' => 'button'));
        
        $departmentList = new Zend_Form_Element_Select('departments');
        $departmentList ->setLabel('Departments');
        
        $department = new Application_Model_DbTable_Department();
        $departments = $department->getDepartmentsForCompany($id);
        
        foreach($departments as $dep) {
            $departmentList->addMultiOption($dep[id], $dep[name]);
        }
        
        $select = new Zend_Form_Element_Submit('select');
        $select ->setAttrib('id', 'submitbutton');
        
        $total = new Zend_Form_Element_Text('total', array("readonly" => "readonly"));
        $total  ->setLabel('Total');
        
        $cut = new Zend_Form_Element_Submit('cut');
        $cut->setAttrib('id', 'submitbutton');

        $this->addElements(array($id, $name, $submit));
        $this->addElements(array($departmentList, $select));
        $this->addElements(array($total, $cut));
    }


}
?>
