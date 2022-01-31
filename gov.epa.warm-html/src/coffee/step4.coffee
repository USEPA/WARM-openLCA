initialize = (data) ->
	$('.btn-calculate').on 'click', () ->
		if checkMaterials()
			calculate $('input[name=output_unit]:checked').attr 'id'
	setInputs data
	recordInputs()

checkMaterials = () ->
	materials = inputs.materials
	unless materials
		return false
	keys = Object.keys materials
	for type in ['baseline', 'alternative']
		sum = 0
		for index in keys
			material = materials[index]
			for subtype in ['source_reduction', 'landfilling', 'recycling', 'combustion', 'composting', 'anaerobic_digestion']
				if material[type + '_' + subtype]
					sum += parseFloat material[type + '_' + subtype]
		if sum is 0
			$('.modal').modal()
			return false
	return true