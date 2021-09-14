materialContributionsLoaded = false
subtypeContributionsLoaded = false
groupedContributionsLoaded = false
currentTab = 'flowContributions'
availableMaterials = []
charts = {}
chartData = {}

setAvailableMaterials = (materials) ->
	availableMaterials = JSON.parse materials
	return null

initializeCharts = () ->
	$('#chart-tabs > li > a').on 'click', () ->
		tab = $ @
		href = tab.attr 'href'
		# e.g. href='#flowContributionsTab'
		#      id='flowContributions'
		id = href.substring 1, href.length - 3
		currentTab = id
		if id is 'materialContributions' and !materialContributionsLoaded
			materialContributionsLoaded = true
			applyFilterToMaterialContributions?()
		else if id is 'subtypeContributions' and !subtypeContributionsLoaded
			subtypeContributionsLoaded = true
			applyFilterToSubtypeContributions?()
		else if id is 'groupedContributions' and !groupedContributionsLoaded
			groupedContributionsLoaded = true
			loadGroupedContributions?()
		else
			setTimeout () ->
				updateChart chartData[id]
			, 100
		$('.open-material-selection').on 'click', (event) ->
			selectMaterials()

selectMaterials = () ->
	materialNames = []
	for material in availableMaterials
		materialNames.push material.name
	modal = jade.templates['material-selection']
		materials: materialNames
	if $('.modal')
		$('.modal').modal 'hide'
		$('.modal').remove()
	$('body').append modal
	$('.modal').modal()
	$('.modal input').on 'click', (event) ->
		target = event.target or event.srcElement or event.originalTarget
		value = $(target).is(':checked')
		count = $('.modal input:checked').length
		if value and count is 7 
			if event.preventDefault
				event.preventDefault()
			else 
				event.returnValue = false
			alert 'You can only select 6 materials at a time'
	$('.modal button#apply-selection').on 'click', (event) ->
		selection = ''
		for element in $('.modal input:checked')
			element = $ element
			if selection
				selection += '@'
			selection += element.val()
		if selection
			$('.modal').modal 'hide'
			$('.modal').remove()	
			if currentTab is 'materialContributions'
				applyFilterToMaterialContributions selection
			else if currentTab is 'subtypeContributions'
				applyFilterToSubtypeContributions selection

updateChart = (data) ->
	id = data.identifier
	chartData[id] = data
	min = 0
	max = 0
	for series in data.series
		if $.isArray(series)
			for value in series
				if value.value or value.value is 0
					value = value.value
				min = Math.min min, value
				max = Math.max max, value
		else
			min = Math.min min, series
			max = Math.max max, series
	options =
		high: Math.ceil(max)
		low: Math.floor(min)
		axisX:
			offset: 60
		plugins: [
			Chartist.plugins.tooltip()
		]
	switch data.series.length
		when 2 then options.seriesBarDistance = 40
		when 4 then options.seriesBarDistance = 20
		when 6 then options.seriesBarDistance = 18
		when 8 then options.seriesBarDistance = 16
		when 10 then options.seriesBarDistance = 14
		when 12 then options.seriesBarDistance = 12
	$("##{id}Tab").removeClass 'one-bar-per-series two-bar-per-series three-bar-per-series four-bar-per-series five-bar-per-series six-bar-per-series'
	switch data.series.length
		when 2 then chartClass = 'one-bar-per-series'
		when 4 then chartClass = 'two-bar-per-series'
		when 6 then chartClass = 'three-bar-per-series'
		when 8 then chartClass = 'four-bar-per-series'
		when 10 then chartClass = 'five-bar-per-series'
		when 12 then chartClass = 'six-bar-per-series'
	$("##{id}Tab").addClass chartClass
	$("##{id}").height($(window).innerHeight() - 300)
	charts[id] = new Chartist.Bar("##{id}", data, options, []);
	if data.legend?.length
		fillLegend id, data.legend
	else
		$('.ct-legend', "##{id}Tab").addClass 'hidden'
	return null

fillLegend = (id, labels) ->
	legend = $ '.ct-legend', "##{id}Tab"
	legend.empty()
	letters = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l']
	for label, index in labels
		legend.append '<div><span class="ct-legend-entry ct-series-' + letters[index] + '">&nbsp;</span><span>' + label + '</span></div>'
	legend.removeClass 'hidden'

collectFieldValues = (entries, field) ->
	values = []
	for entry in entries
		values.push entry[field]
	return values

initializeCharts()