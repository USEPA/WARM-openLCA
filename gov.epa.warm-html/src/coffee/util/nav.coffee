interceptLinks = (selector, callback) ->
	$(selector).on 'click', (event) ->
		if event.preventDefault
			event.preventDefault()
		else
			event.returnValue = false
		event.doit = false
		href = $(@).attr 'href'
		currentStep = $(@).attr 'data-current-step'
		if parseInt(currentStep) is 1 and $('.scenario-table > tbody > tr[data-index].danger').length
				$('.modal').modal()
				$('.modal').on 'hide.bs.modal', () ->
					window[callback]?(href)
		else
			window[callback]?(href)

interceptLinks 'a.navigate', 'navigate'
interceptLinks 'a.external', 'openInExternalBrowser'