inputs = {} 

setInputs = (data = '{}', typeForAll) ->
	inputs = JSON.parse data
	keys = getOrderedKeys inputs
	for id in keys
		if id is 'materials'
			continue
		input = inputs[id]
		value = input.value
		type = typeForAll or input.type
		switch type
			when 'text'
				$("##{id}").val value
			when 'radio'
				$("##{input.value}").click()
			when 'select'
				$("##{id} option[value='#{value}']").prop 'selected', true
			when 'date'
				$("##{id}").val value				
			when 'element'
				if input.type is 'date'
					value = moment(value).format 'MM/DD/YYYY'
				$("##{id}").html value				
		if type is 'text' or type is 'select'
			$("##{id}").trigger 'change'
	if inputs['materials']
		materials = Object.keys inputs['materials']
		for index in materials
			row = $("tr[data-index=#{index}]")
			fields = Object.keys inputs['materials'][index]
			for field in fields
				inputField = $("##{field}", row)
				inputField.val inputs['materials'][index][field]
				inputField.trigger 'change'
	return null

getInputs = () ->
	return JSON.stringify inputs

recordInputs = () ->
	$('input, select, textarea').on 'change', () ->
		record $ @
	for input in $('input, select, textarea')
		record $(input), false

record = (element, save = true) ->
	console.log inputs
	id = element.attr 'id'
	value = element.val()
	type = getType element
	switch type
		when 'material'
			index = getIndex element
			ensureMaterialEntryExists index
			inputs['materials'][index][id] = value
		when 'radio'
			unless element.is(':checked')
				return
			name = element.attr 'name'
			order = getOrder element
			inputs[name] = 
				type: type
				value: id
				order: order
		else
			inputs[id] = 
				type: type
				value: value
	if save
		saveInputs JSON.stringify inputs

getOrderedKeys = (inputs) ->
	keys = Object.keys inputs
	return keys.sort (a, b) ->
		order1 = 0
		order2 = 0
		if inputs[a]['order']
			order1 = inputs[a]['order']
		if inputs[b]['order']
			order2 = inputs[b]['order']
		return order1 - order2


getOrder = (element) ->
	while !element.is('body') && !element.attr('data-order')
		element = element.parent()
	return element.attr('data-order')

ensureMaterialEntryExists = (index) ->
	unless inputs['materials']
		inputs['materials'] = {}
	unless inputs['materials'][index]
		inputs['materials'][index] = materials[parseInt index]

getType = (element) ->
	if element.is 'select'
		return 'select'
	if element.is 'textarea'
		return 'text'
	if element.is 'input'
		isMaterialInput = element.hasClass 'material-input'
		if isMaterialInput
			return 'material'
		else
			type = element.attr 'type'
			switch type
				when 'text' then return 'text'
				when 'date' then return 'date'
				when 'hidden' then return 'text'
				when 'radio' then return 'radio'
	return null

getIndex = (materialInput) ->
	row = getParentRow materialInput
	return row.attr 'data-index'

getMaterialName = (materialInput) ->
	row = getParentRow materialInput
	materialCell = $ '.material .with-note', row
	unless materialCell?.length
		materialCell = $ '.material', row
	text = materialCell.html()
	if text.indexOf('<sup>') isnt -1
		text = text.substring 0, text.indexOf '<sup>'
	return text

getParentRow = (materialInput) ->
	element = materialInput
	while !element.is('tr')
		element = element.parent()
	return element