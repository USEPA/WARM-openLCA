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
	updateAnaerobicDigestionStatus inputs
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
	
updateAnaerobicDigestionStatus = (inputs) ->
	#alert "updateAnaerobicDigestionStatus("+inputs+")"
	dry_only = false
	materials = Object.keys inputs['materials']
	for index in materials
		if inputs['materials'][index]['dry_only_digestion']
			baseline = Number(inputs['materials'][index]['baseline_anaerobic_digestion'])
			alternative = Number(inputs['materials'][index]['alternative_anaerobic_digestion'])
			#alert inputs['materials'][index]['name']+": "+inputs['materials'][index]['baseline_anaerobic_digestion']+"; "+inputs['materials'][index]['alternative_anaerobic_digestion']
			if !isNaN(baseline) and !isNaN(alternative) and ((baseline != 0) or (alternative != 0))
				#alert (Number(inputs['materials'][index]['baseline_anaerobic_digestion']) != 0) + "; " + (Number(inputs['materials'][index]['alternative_anaerobic_digestion']) != 0)
				dry_only = true
	if dry_only
		#alert "Dry only"
		$('#anaerobic_digestion_wet').prop 'disabled', true
		$('#anaerobic_digestion_wet').prop 'checked', false
		$('#anaerobic_digestion_dry').prop 'checked', true
		