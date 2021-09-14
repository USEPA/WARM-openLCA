initializeSummary = () ->
	$('.with-note').tooltip()

setSummaryResults = (materials, equivalents, baselineTotal, alternativeTotal) ->
	fillSummaryTable materials
	setEquivalents equivalents
	$('#total-change').html numeral(alternativeTotal - baselineTotal).format '0.00'
	$('.result-table #total-baseline').html numeral(baselineTotal).format '0.00'
	$('.result-table #total-alternative').html numeral(alternativeTotal).format '0.00'
	$('.with-note').tooltip()
	return null

fillSummaryTable = (materials) ->
	results = JSON.parse materials
	index = 1
	stack = []
	for result in results
		stack.push result 
	while stack.length isnt 0
		$('.result-table tbody').prepend jade.templates['summary-result-row'] 
			material: stack.pop()
			index: index++
			formatNumber: formatNumber

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

initializeSummary()