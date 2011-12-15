/* start module: 101Companies */
$pyjs.loaded_modules['101Companies'] = function (__mod_name__) {
	if($pyjs.loaded_modules['101Companies'].__was_initialized__) return $pyjs.loaded_modules['101Companies'];
	var $m = $pyjs.loaded_modules["101Companies"];
	$m.__repr__ = function() { return "<module: 101Companies>"; };
	$m.__was_initialized__ = true;
	if ((__mod_name__ === null) || (typeof __mod_name__ == 'undefined')) __mod_name__ = '101Companies';
	$m.__name__ = __mod_name__;


	$m['pyjd'] = $p['___import___']('pyjd', null);
	$m['RootPanel'] = $p['___import___']('pyjamas.ui.RootPanel.RootPanel', null, null, false);
	$m['Button'] = $p['___import___']('pyjamas.ui.Button.Button', null, null, false);
	$m['Label'] = $p['___import___']('pyjamas.ui.Label.Label', null, null, false);
	$m['Grid'] = $p['___import___']('pyjamas.ui.Grid.Grid', null, null, false);
	$m['TextBox'] = $p['___import___']('pyjamas.ui.TextBox.TextBox', null, null, false);
	$m['ListBox'] = $p['___import___']('pyjamas.ui.ListBox.ListBox', null, null, false);
	$m['pygwt'] = $p['___import___']('pygwt', null);
	$m['Company'] = (function(){
		var $cls_definition = new Object();
		var $method;
		$cls_definition.__module__ = '101Companies';
		$method = $pyjs__bind_method2('__init__', function(id, name, departments) {
			if (this.__is_instance__ === true) {
				var self = this;
			} else {
				var self = arguments[0];
				id = arguments[1];
				name = arguments[2];
				departments = arguments[3];
			}
			if (typeof id == 'undefined') id=arguments.callee.__args__[3][1];
			if (typeof name == 'undefined') name=arguments.callee.__args__[4][1];
			if (typeof departments == 'undefined') departments=arguments.callee.__args__[5][1];

			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('id', id) : $p['setattr'](self, 'id', id);
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('$$name', name) : $p['setattr'](self, '$$name', name);
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('departments', departments) : $p['setattr'](self, 'departments', departments);
			return null;
		}
	, 1, [null,null,['self'],['id', 0],['name', ''],['departments', $p['list']([])]]);
		$cls_definition['__init__'] = $method;
		$method = $pyjs__bind_method2('total', function() {
			if (this.__is_instance__ === true) {
				var self = this;
			} else {
				var self = arguments[0];
			}
			var $iter1_nextval,$iter1_type,$iter1_iter,$add2,$add1,$iter1_array,department,total,$iter1_idx;
			total = 0;
			$iter1_iter = self['departments'];
			$iter1_nextval=$p['__iter_prepare']($iter1_iter,false);
			while (typeof($p['__wrapped_next']($iter1_nextval).$nextval) != 'undefined') {
				department = $iter1_nextval.$nextval;
				total = $p['__op_add']($add1=total,$add2=department['total']());
			}
			return total;
		}
	, 1, [null,null,['self']]);
		$cls_definition['total'] = $method;
		$method = $pyjs__bind_method2('cut', function() {
			if (this.__is_instance__ === true) {
				var self = this;
			} else {
				var self = arguments[0];
			}
			var $iter2_nextval,$iter2_type,$iter2_iter,$iter2_idx,department,$iter2_array;
			$iter2_iter = self['departments'];
			$iter2_nextval=$p['__iter_prepare']($iter2_iter,false);
			while (typeof($p['__wrapped_next']($iter2_nextval).$nextval) != 'undefined') {
				department = $iter2_nextval.$nextval;
				department['cut']();
			}
			$m['RootPanel']()['add']($m['Label']('yey'));
			return null;
		}
	, 1, [null,null,['self']]);
		$cls_definition['cut'] = $method;
		$method = $pyjs__bind_method2('save', function(name) {
			if (this.__is_instance__ === true) {
				var self = this;
			} else {
				var self = arguments[0];
				name = arguments[1];
			}

			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('$$name', name) : $p['setattr'](self, '$$name', name);
			return null;
		}
	, 1, [null,null,['self'],['name']]);
		$cls_definition['save'] = $method;
		var $bases = new Array(pyjslib.object);
		var $data = $p['dict']();
		for (var $item in $cls_definition) { $data.__setitem__($item, $cls_definition[$item]); }
		return $p['_create_class']('Company', $p['tuple']($bases), $data);
	})();
	$m['Department'] = (function(){
		var $cls_definition = new Object();
		var $method;
		$cls_definition.__module__ = '101Companies';
		$method = $pyjs__bind_method2('__init__', function(id, name, departments, employees) {
			if (this.__is_instance__ === true) {
				var self = this;
			} else {
				var self = arguments[0];
				id = arguments[1];
				name = arguments[2];
				departments = arguments[3];
				employees = arguments[4];
			}
			if (typeof id == 'undefined') id=arguments.callee.__args__[3][1];
			if (typeof name == 'undefined') name=arguments.callee.__args__[4][1];
			if (typeof departments == 'undefined') departments=arguments.callee.__args__[5][1];
			if (typeof employees == 'undefined') employees=arguments.callee.__args__[6][1];

			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('id', id) : $p['setattr'](self, 'id', id);
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('$$name', name) : $p['setattr'](self, '$$name', name);
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('departments', departments) : $p['setattr'](self, 'departments', departments);
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('employees', employees) : $p['setattr'](self, 'employees', employees);
			return null;
		}
	, 1, [null,null,['self'],['id', 0],['name', ''],['departments', $p['list']([])],['employees', $p['list']([])]]);
		$cls_definition['__init__'] = $method;
		$method = $pyjs__bind_method2('total', function() {
			if (this.__is_instance__ === true) {
				var self = this;
			} else {
				var self = arguments[0];
			}
			var $iter3_idx,$iter4_type,$iter4_nextval,$iter3_type,$add5,$iter4_idx,$add3,department,$add6,$iter3_iter,$add4,$iter3_array,employee,total,$iter4_iter,$iter3_nextval,$iter4_array;
			total = 0;
			$iter3_iter = self['employees'];
			$iter3_nextval=$p['__iter_prepare']($iter3_iter,false);
			while (typeof($p['__wrapped_next']($iter3_nextval).$nextval) != 'undefined') {
				employee = $iter3_nextval.$nextval;
				total = $p['__op_add']($add3=total,$add4=employee['total']());
			}
			$iter4_iter = self['departments'];
			$iter4_nextval=$p['__iter_prepare']($iter4_iter,false);
			while (typeof($p['__wrapped_next']($iter4_nextval).$nextval) != 'undefined') {
				department = $iter4_nextval.$nextval;
				total = $p['__op_add']($add5=total,$add6=department['total']());
			}
			return total;
		}
	, 1, [null,null,['self']]);
		$cls_definition['total'] = $method;
		$method = $pyjs__bind_method2('cut', function() {
			if (this.__is_instance__ === true) {
				var self = this;
			} else {
				var self = arguments[0];
			}
			var $iter5_nextval,$iter6_idx,$iter6_type,$iter5_idx,department,$iter6_array,$iter5_iter,$iter5_array,employee,$iter5_type,$iter6_iter,$iter6_nextval;
			$iter5_iter = self['employees'];
			$iter5_nextval=$p['__iter_prepare']($iter5_iter,false);
			while (typeof($p['__wrapped_next']($iter5_nextval).$nextval) != 'undefined') {
				employee = $iter5_nextval.$nextval;
				employee['cut']();
			}
			$iter6_iter = self['departments'];
			$iter6_nextval=$p['__iter_prepare']($iter6_iter,false);
			while (typeof($p['__wrapped_next']($iter6_nextval).$nextval) != 'undefined') {
				department = $iter6_nextval.$nextval;
				department['cut']();
			}
			return null;
		}
	, 1, [null,null,['self']]);
		$cls_definition['cut'] = $method;
		$method = $pyjs__bind_method2('save', function(name) {
			if (this.__is_instance__ === true) {
				var self = this;
			} else {
				var self = arguments[0];
				name = arguments[1];
			}

			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('$$name', name) : $p['setattr'](self, '$$name', name);
			return null;
		}
	, 1, [null,null,['self'],['name']]);
		$cls_definition['save'] = $method;
		var $bases = new Array(pyjslib.object);
		var $data = $p['dict']();
		for (var $item in $cls_definition) { $data.__setitem__($item, $cls_definition[$item]); }
		return $p['_create_class']('Department', $p['tuple']($bases), $data);
	})();
	$m['Employee'] = (function(){
		var $cls_definition = new Object();
		var $method;
		$cls_definition.__module__ = '101Companies';
		$method = $pyjs__bind_method2('__init__', function(id, name, address, salary, manager) {
			if (this.__is_instance__ === true) {
				var self = this;
			} else {
				var self = arguments[0];
				id = arguments[1];
				name = arguments[2];
				address = arguments[3];
				salary = arguments[4];
				manager = arguments[5];
			}
			if (typeof id == 'undefined') id=arguments.callee.__args__[3][1];
			if (typeof name == 'undefined') name=arguments.callee.__args__[4][1];
			if (typeof address == 'undefined') address=arguments.callee.__args__[5][1];
			if (typeof salary == 'undefined') salary=arguments.callee.__args__[6][1];
			if (typeof manager == 'undefined') manager=arguments.callee.__args__[7][1];

			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('id', id) : $p['setattr'](self, 'id', id);
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('$$name', name) : $p['setattr'](self, '$$name', name);
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('address', address) : $p['setattr'](self, 'address', address);
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('salary', salary) : $p['setattr'](self, 'salary', salary);
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('manager', manager) : $p['setattr'](self, 'manager', manager);
			return null;
		}
	, 1, [null,null,['self'],['id', 0],['name', ''],['address', ''],['salary', 0],['manager', 0]]);
		$cls_definition['__init__'] = $method;
		$method = $pyjs__bind_method2('total', function() {
			if (this.__is_instance__ === true) {
				var self = this;
			} else {
				var self = arguments[0];
			}
			var $attr1,$attr2;
			return (($attr1=($attr2=self)['salary']) == null || (($attr2.__is_instance__) && typeof $attr1 == 'function') || (typeof $attr1['__get__'] == 'function')?
						$p['getattr']($attr2, 'salary'):
						self['salary']);
		}
	, 1, [null,null,['self']]);
		$cls_definition['total'] = $method;
		$method = $pyjs__bind_method2('cut', function() {
			if (this.__is_instance__ === true) {
				var self = this;
			} else {
				var self = arguments[0];
			}
			var $div2,$div1,$attr3,$attr4;
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('salary', (typeof ($div1=(($attr3=($attr4=self)['salary']) == null || (($attr4.__is_instance__) && typeof $attr3 == 'function') || (typeof $attr3['__get__'] == 'function')?
						$p['getattr']($attr4, 'salary'):
						self['salary']))==typeof ($div2=2) && typeof $div1=='number' && $div2 !== 0?
				$div1/$div2:
				$p['op_div']($div1,$div2))) : $p['setattr'](self, 'salary', (typeof ($div1=(($attr3=($attr4=self)['salary']) == null || (($attr4.__is_instance__) && typeof $attr3 == 'function') || (typeof $attr3['__get__'] == 'function')?
						$p['getattr']($attr4, 'salary'):
						self['salary']))==typeof ($div2=2) && typeof $div1=='number' && $div2 !== 0?
				$div1/$div2:
				$p['op_div']($div1,$div2)));
			return null;
		}
	, 1, [null,null,['self']]);
		$cls_definition['cut'] = $method;
		$method = $pyjs__bind_method2('save', function(name, address, salary) {
			if (this.__is_instance__ === true) {
				var self = this;
			} else {
				var self = arguments[0];
				name = arguments[1];
				address = arguments[2];
				salary = arguments[3];
			}

			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('$$name', name) : $p['setattr'](self, '$$name', name);
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('address', address) : $p['setattr'](self, 'address', address);
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('salary', salary) : $p['setattr'](self, 'salary', salary);
			return null;
		}
	, 1, [null,null,['self'],['name'],['address'],['salary']]);
		$cls_definition['save'] = $method;
		var $bases = new Array(pyjslib.object);
		var $data = $p['dict']();
		for (var $item in $cls_definition) { $data.__setitem__($item, $cls_definition[$item]); }
		return $p['_create_class']('Employee', $p['tuple']($bases), $data);
	})();
	$m['CompaniesApp'] = (function(){
		var $cls_definition = new Object();
		var $method;
		$cls_definition.__module__ = '101Companies';
		$method = $pyjs__bind_method2('__init__', function() {
			if (this.__is_instance__ === true) {
				var self = this;
			} else {
				var self = arguments[0];
			}
			var development,joe,klaus,craig,research,dev1,erik,dev11,ralf,karl,ray;
			craig = $m['Employee'](1, 'Craig', 'Redmond', 123456, 1);
			ray = $m['Employee'](2, 'Ray', 'Redmond', 234567, 1);
			klaus = $m['Employee'](3, 'Klaus', 'Boston', 23456, 1);
			karl = $m['Employee'](4, 'Karl', 'Riga', 2345, 1);
			erik = $m['Employee'](5, 'Erik', 'Utrecht', 12345, 0);
			ralf = $m['Employee'](6, 'Ralf', 'Koblenz', 1234, 0);
			joe = $m['Employee'](7, 'Joe', 'Wifi City', 2344, 0);
			dev11 = $m['Department'](1, 'Dev1.1', $p['list']([]), $p['list']([karl, joe]));
			dev1 = $m['Department'](2, 'Dev1', $p['list']([dev11]), $p['list']([klaus]));
			research = $m['Department'](3, 'Research', $p['list']([]), $p['list']([craig, erik, ralf]));
			development = $m['Department'](4, 'Development', $p['list']([dev1]), $p['list']([ray]));
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('company', $m['Company'](1, 'meganalysis', $p['list']([research, development]))) : $p['setattr'](self, 'company', $m['Company'](1, 'meganalysis', $p['list']([research, development])));
			return null;
		}
	, 1, [null,null,['self']]);
		$cls_definition['__init__'] = $method;
		var $bases = new Array(pyjslib.object);
		var $data = $p['dict']();
		for (var $item in $cls_definition) { $data.__setitem__($item, $cls_definition[$item]); }
		return $p['_create_class']('CompaniesApp', $p['tuple']($bases), $data);
	})();
	$m['CompaniesAppGUI'] = (function(){
		var $cls_definition = new Object();
		var $method;
		$cls_definition.__module__ = '101Companies';
		$method = $pyjs__bind_method2('__init__', function() {
			if (this.__is_instance__ === true) {
				var self = this;
			} else {
				var self = arguments[0];
			}
			var $attr5,$attr6;
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('app', $m['CompaniesApp']()) : $p['setattr'](self, 'app', $m['CompaniesApp']());
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('$$name', $m['TextBox']()) : $p['setattr'](self, '$$name', $m['TextBox']());
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('departments', $pyjs_kwargs_call(null, $m['ListBox'], null, null, [{Size:'100%', VisibleItemCount:'5'}])) : $p['setattr'](self, 'departments', $pyjs_kwargs_call(null, $m['ListBox'], null, null, [{Size:'100%', VisibleItemCount:'5'}]));
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('employees', $pyjs_kwargs_call(null, $m['ListBox'], null, null, [{Size:'100%', VisibleItemCount:'5'}])) : $p['setattr'](self, 'employees', $pyjs_kwargs_call(null, $m['ListBox'], null, null, [{Size:'100%', VisibleItemCount:'5'}]));
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('total', $m['TextBox']()) : $p['setattr'](self, 'total', $m['TextBox']());
			self.__is_instance__ && typeof self.__setattr__ == 'function' ? self.__setattr__('current', (($attr5=($attr6=self['app'])['company']) == null || (($attr6.__is_instance__) && typeof $attr5 == 'function') || (typeof $attr5['__get__'] == 'function')?
						$p['getattr']($attr6, 'company'):
						self['app']['company'])) : $p['setattr'](self, 'current', (($attr5=($attr6=self['app'])['company']) == null || (($attr6.__is_instance__) && typeof $attr5 == 'function') || (typeof $attr5['__get__'] == 'function')?
						$p['getattr']($attr6, 'company'):
						self['app']['company']));
			return null;
		}
	, 1, [null,null,['self']]);
		$cls_definition['__init__'] = $method;
		$method = $pyjs__bind_method2('initCompanyGUI', function() {
			if (this.__is_instance__ === true) {
				var self = this;
			} else {
				var self = arguments[0];
			}
			var $attr9,$attr8,formGrid,$attr16,$iter7_nextval,$iter7_iter,$iter7_array,$attr7,item,$attr18,$iter7_idx,$attr15,$attr14,$attr17,$iter7_type,$attr11,$attr10,$attr13,$attr12;
			formGrid = $m['Grid'](3, 3);
			formGrid['setVisible'](true);
			self['$$name']['setText']((($attr7=($attr8=self['app']['company'])['$$name']) == null || (($attr8.__is_instance__) && typeof $attr7 == 'function') || (typeof $attr7['__get__'] == 'function')?
						$p['getattr']($attr8, '$$name'):
						self['app']['company']['$$name']));
			self['departments']['clear']();
			$iter7_iter = self['current']['departments'];
			$iter7_nextval=$p['__iter_prepare']($iter7_iter,false);
			while (typeof($p['__wrapped_next']($iter7_nextval).$nextval) != 'undefined') {
				item = $iter7_nextval.$nextval;
				self['departments']['addItem']((($attr9=($attr10=item)['$$name']) == null || (($attr10.__is_instance__) && typeof $attr9 == 'function') || (typeof $attr9['__get__'] == 'function')?
							$p['getattr']($attr10, '$$name'):
							item['$$name']), (($attr11=($attr12=item)['id']) == null || (($attr12.__is_instance__) && typeof $attr11 == 'function') || (typeof $attr11['__get__'] == 'function')?
							$p['getattr']($attr12, 'id'):
							item['id']));
			}
			if ($p['bool'](($p['cmp'](self['departments']['getItemCount'](), 0) == 1))) {
				self['departments']['setSelectedIndex'](0);
			}
			self['total']['setText'](self['app']['company']['total']());
			formGrid['setWidget'](0, 0, $m['Label']('Name:'));
			formGrid['setWidget'](1, 0, $m['Label']('Department:'));
			formGrid['setWidget'](2, 0, $m['Label']('Total:'));
			formGrid['setWidget'](0, 1, (($attr13=($attr14=self)['$$name']) == null || (($attr14.__is_instance__) && typeof $attr13 == 'function') || (typeof $attr13['__get__'] == 'function')?
						$p['getattr']($attr14, '$$name'):
						self['$$name']));
			formGrid['setWidget'](1, 1, (($attr15=($attr16=self)['departments']) == null || (($attr16.__is_instance__) && typeof $attr15 == 'function') || (typeof $attr15['__get__'] == 'function')?
						$p['getattr']($attr16, 'departments'):
						self['departments']));
			formGrid['setWidget'](2, 1, (($attr17=($attr18=self)['total']) == null || (($attr18.__is_instance__) && typeof $attr17 == 'function') || (typeof $attr17['__get__'] == 'function')?
						$p['getattr']($attr18, 'total'):
						self['total']));
			formGrid['setWidget'](0, 2, $m['Button']('save'));
			formGrid['setWidget'](1, 2, $m['Button']('select'));
			formGrid['setWidget'](2, 2, $m['Button']('cut'));
			$m['RootPanel']()['add'](formGrid);
			return null;
		}
	, 1, [null,null,['self']]);
		$cls_definition['initCompanyGUI'] = $method;
		var $bases = new Array(pyjslib.object);
		var $data = $p['dict']();
		for (var $item in $cls_definition) { $data.__setitem__($item, $cls_definition[$item]); }
		return $p['_create_class']('CompaniesAppGUI', $p['tuple']($bases), $data);
	})();
	$m['pyjd']['setup']('public/101Companies.html');
	$m['start'] = $m['CompaniesAppGUI']();
	$m['start']['initCompanyGUI']();
	return this;
}; /* end 101Companies */


/* end module: 101Companies */


/*
PYJS_DEPS: ['pyjd', 'pyjamas.ui.RootPanel.RootPanel', 'pyjamas', 'pyjamas.ui', 'pyjamas.ui.RootPanel', 'pyjamas.ui.Button.Button', 'pyjamas.ui.Button', 'pyjamas.ui.Label.Label', 'pyjamas.ui.Label', 'pyjamas.ui.Grid.Grid', 'pyjamas.ui.Grid', 'pyjamas.ui.TextBox.TextBox', 'pyjamas.ui.TextBox', 'pyjamas.ui.ListBox.ListBox', 'pyjamas.ui.ListBox', 'pygwt']
*/
