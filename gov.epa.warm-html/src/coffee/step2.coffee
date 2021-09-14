initialized = false

initialize = (data) ->
	if initialized
		return
	initialized = true
	locations = Object.keys regions
	for location in locations 
		$('#state').append('<option value="' + location + '">' + location + '</option>')
	setInputs data
	updateTransportStatus inputs['transport_distance']?['value']
	updateLandfillStatus inputs['landfill_type']?['value']
	updateStateStatus inputs['state']?['value']
	initListeners()
	recordInputs()

initListeners = () ->
	$("input[name=transport_distance]").on 'click', () ->
		updateTransportStatus $(@).attr 'id'
	$("input[name=landfill_type]").on 'click', () ->
		updateLandfillStatus $(@).attr 'id'
	$('#state').on 'change', () ->
		updateStateStatus $(@).val()

updateTransportStatus = (selectionId = 'transport_distance_default') ->
	doEnable = selectionId is 'transport_distance_define'
	$('.distance-table input').prop 'disabled', !doEnable

updateLandfillStatus = (selectionId = 'landfill_type_national_average') ->
	if selectionId is 'landfill_type_lfg_recovery'
		$('.type-lfg-sub-selection input, #landfill-characteristics-2 input, #landfill-characteristics-3 input').prop 'disabled', false
		$('.type-lfg-sub-selection input:checked').trigger 'change'
	else if selectionId is 'landfill_type_national_average'	
		$('.type-lfg-sub-selection input, #landfill-characteristics-2 input').prop 'disabled', true
		$('#landfill_gas_recovery_typical_operation').prop 'checked', true
		$('#landfill_gas_recovery_typical_operation').trigger 'change'
		$('#landfill-characteristics-3 input').prop 'disabled', false
	else
		$('.type-lfg-sub-selection input, #landfill-characteristics-2 input, #landfill-characteristics-3 input').prop 'disabled', true
		$('#landfill_gas_recovery_typical_operation').prop 'checked', true
		$('#landfill_gas_recovery_typical_operation').trigger 'change'
		$('#landfill_moisture_national_average').prop 'checked', true
		$('#landfill_moisture_national_average').trigger 'change'

updateStateStatus = (selection = 'National Average') ->
	region = regions[selection]
	$('#location_display').html region
	$('#location').val locationIds[region]
	$('#location').trigger 'change'