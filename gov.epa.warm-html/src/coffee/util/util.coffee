$('.section-header').on 'click', () ->
	element = $ 'span.glyphicon', $ @
	if element.hasClass('glyphicon-chevron-down')
		element.removeClass 'glyphicon-chevron-down'
		element.addClass 'glyphicon-chevron-right'
	else
		element.removeClass 'glyphicon-chevron-right'
		element.addClass 'glyphicon-chevron-down'

formatNumber = (value, decimalPlaces = 2) ->
	pattern = '0'
	if decimalPlaces
		pattern += '.'
		for i in [1..decimalPlaces]
			pattern += '0'
	return numeral(value).format pattern 