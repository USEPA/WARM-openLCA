$(document).keydown (event) =>
	if event.ctrlKey == true and (event.which == '61' or event.which == '107' or event.which == '173' or event.which == '109' or event.which == '187' or event.which == '189')
		event.preventDefault()
	
window.addEventListener 'wheel', (event) =>
	event.preventDefault()
, { passive: false }