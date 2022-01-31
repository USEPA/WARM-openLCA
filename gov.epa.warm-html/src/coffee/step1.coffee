initialize = (data) ->
	setMaterials materials
	setInputs data
	$('.table input, .with-note').tooltip()
	registerListeners()
	recordInputs()

setMaterials = (materials) ->
	tableBody = $ '.scenario-table tbody'
	notes = $ '.foot-notes'
	number = 0
	for material, index in materials
		if material.note
			material.note = 
				no: ++number
				text: material.note
			notes.append "<p>#{number}) #{material.note.text}</p>"
		tableBody.append jade.templates['scenario-row'] 
			material: material
			index: index

registerListeners = () -> 
	$('.table input').on 'change', () ->
		element = $ @
		unless element.val() 
			element.val 0
		index = getIndex element
		calculateScenarioRow index
	$('.table input').on 'click', () ->
		$(@).select()
	for row in $('tr[data-index]')
		row = $ row
		calculateScenarioRow row.attr 'data-index'

getIndex = (element) ->
	while !element.is('tr')
		element = element.parent()
	return element.attr 'data-index'

calculateScenarioRow = (index) ->
	baselineTotal = addCellValue 'baseline_recycling', index, new Big 0
	baselineTotal = addCellValue 'baseline_landfilling', index, baselineTotal
	baselineTotal = addCellValue 'baseline_combustion', index, baselineTotal
	baselineTotal = addCellValue 'baseline_composting', index, baselineTotal
	baselineTotal = addCellValue 'baseline_anaerobic_digestion', index, baselineTotal
	setCellValue 'baseline_total', index, baselineTotal.toString()
	alternativeTotal = addCellValue 'alternative_recycling', index, new Big 0
	alternativeTotal = addCellValue 'alternative_landfilling', index, alternativeTotal 
	alternativeTotal = addCellValue 'alternative_combustion', index, alternativeTotal
	alternativeTotal = addCellValue 'alternative_composting', index, alternativeTotal
	alternativeTotal = addCellValue 'alternative_source_reduction', index, alternativeTotal
	alternativeTotal = addCellValue 'alternative_anaerobic_digestion', index, alternativeTotal
	if baselineTotal.eq(alternativeTotal)
		unmarkError index
	else
		markError index
	ensureMaterialEntryExists index
	inputs['materials'][index]['baseline'] = baselineTotal.toString()
	saveInputs JSON.stringify inputs

addCellValue = (type, index, total) ->
	cell = $ "##{type}", getRow index
	if cell.length is 0
		return total
	return new Big(cell.val()).plus new Big total

setCellValue = (type, index, value) ->
	cell = $ "##{type}", getRow index
	if cell.length
		cell.html value

getRow = (index) ->
	return $ ".scenario-table > tbody > tr[data-index=#{index}]"

markError = (index) ->
	row = getRow index
	row.addClass 'danger'
	indicator = $ 'td.error-indicator', row
	indicator.removeClass 'invisible'
	indicator.html '!'

unmarkError = (index) ->
	row = getRow index
	row.removeClass 'danger'
	indicator = $ 'td.error-indicator', row
	indicator.addClass 'invisible'
	indicator.html ''
