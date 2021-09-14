initialize = () ->
	$('.btn-user-guide').on 'click', (event) ->
		preventDefault event
		downloadUserGuide()
	$('.btn-get-started').on 'click', (event) ->
		preventDefault event
		start()

preventDefault = (event) ->
	if event.preventDefault
		event.preventDefault()
	else
		event.returnValue = false 
	event.doit = false

initialize()