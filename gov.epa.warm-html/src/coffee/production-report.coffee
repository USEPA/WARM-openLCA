initializeProduction = () ->
	$('.with-note').tooltip()

setProductionResults = (materials, baselineTotal, alternativeTotal) ->
	fillProductionTable materials
	$('.info-box #total-baseline').html numeral(baselineTotal).format '0.00'
	$('.info-box #total-alternative').html numeral(alternativeTotal).format '0.00'
	$('.info-box #total-change').html numeral(alternativeTotal - baselineTotal).format '0.00'
	$('.with-note').tooltip()
	return null

fillProductionTable = (materials) ->
	results = JSON.parse materials
	index = 1
	stack = []
	for result in results
		stack.push result
	body = $('body')
	type = body.attr 'data-type'
	unit = body.attr 'data-unit'
	# alert("body="+body+"; type="+type+"; unit="+unit+"; stack.length="+stack.length+"; formatNumber="+formatNumber)
	while stack.length isnt 0
		nextMaterial = stack.pop()
		# alert("#"+stack.length+": nextMaterial="+nextMaterial.name)
		# alert("jade.templates['production-result-row'] = "+jade.templates['production-result-row'])
		try
			$('.result-table tbody').prepend jade.templates['production-result-row'] 
				material: nextMaterial
				type: type
				unit: unit
				formatNumber: formatNumber
		catch err
			document.getElementById("exception").innerHTML = err.message		
		
setEquivalents = (equivalentsString) ->
	equivalents = JSON.parse equivalentsString
	for id in Object.keys(equivalents)
		value = equivalents[id]
		if id.indexOf('annual') isnt -1
			value = Math.abs formatNumber value, 5
			doShow = parseInt(value * 100000) >= 1
		else 
			value = parseInt Math.abs value
			doShow = value >= 1
		box = $ "##{id}.equivalent" 
		if doShow
			box.removeClass 'hidden'
			if equivalents[id] >= 0
				$('.positive-prefix', box).removeClass 'hidden'
				$('.negative-prefix', box).addClass 'hidden'
			else
				$('.positive-prefix', box).addClass 'hidden'
				$('.negative-prefix', box).removeClass 'hidden'
			$('.equivalent-value', box).html value		
		else
			box.addClass 'hidden'			

initializeProduction()