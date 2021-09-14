initializeAnalysis = () ->
	$('.with-note').tooltip()

setAnalysisResults = (materials, baselineTotal, alternativeTotal) ->
	fillAnalysisTable materials
	$('.info-box #total-baseline').html numeral(baselineTotal).format '0.00'
	$('.info-box #total-alternative').html numeral(alternativeTotal).format '0.00'
	$('.info-box #total-change').html numeral(alternativeTotal - baselineTotal).format '0.00'
	$('.with-note').tooltip()
	return null

fillAnalysisTable = (materials) ->
	results = JSON.parse materials
	index = 1
	stack = []
	for result in results
		stack.push result
	body = $('body')
	type = body.attr 'data-type'
	unit = body.attr 'data-unit'
	while stack.length isnt 0
		nextMaterial = stack.pop()
		$('.per-ton-result-table tbody').prepend jade.templates['per-ton-result-row'] 
			material: nextMaterial
			type: type
			unit: unit
			formatNumber: formatNumber
		$('.analysis-result-table[data-alternative=false] tbody').prepend jade.templates['analysis-result-row'] 
			material: nextMaterial
			type: type
			unit: unit
			isAlternative: false
			formatNumber: formatNumber
		$('.analysis-result-table[data-alternative=true] tbody').prepend jade.templates['analysis-result-row'] 
			material: nextMaterial
			type: type
			unit: unit
			isAlternative: true
			formatNumber: formatNumber
		$('.increment-table tbody').prepend jade.templates['increment-result-row'] 
			material: nextMaterial
			type: type
			unit: unit
			formatNumber: formatNumber

initializeAnalysis()