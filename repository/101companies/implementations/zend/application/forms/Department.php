<?php

class Application_Form_Department extends Zend_Form
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
        $submit->setAttrib('id', 'submitbutton');
        
        $this->addElements(array($id, $name, $submit));
    }


}

