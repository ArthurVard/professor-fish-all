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
        $total = new Zend_Form_Element_Text('total', array("readonly" => "readonly"));
        $total  ->setLabel('Total');
        
        $submit = new Zend_Form_Element_Submit('save');
        $submit->setAttrib('id', 'submitbutton');
        
        $cut = new Zend_Form_Element_Submit('cut');
        $cut->setAttrib('id', 'cutbutton');
        
        $select = new Zend_Form_Element_Submit('select');
        $select->setAttrib('id', 'selectbutton');
        
        $this->addElements(array($id, $name, $total, $submit, $cut, $select));
    }


}
?>
