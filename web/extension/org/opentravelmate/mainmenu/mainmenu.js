/**
 * Define the main menu startup script.
 * 
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define(['jquery'], function($) {
	
	/**
	 * Add a button to the panel.
	 * 
	 * @param {String} title
	 * @param {String} tooltip
	 * @param {String} imageUrl
	 */
	function addButton(title, tooltip, imageUrl) {
		var button = document.createElement('button');
		button.id = 'button-' + title;
		button.setAttribute('title', tooltip);
		var buttonImage = document.createElement('img');
		buttonImage.src = imageUrl;
		buttonImage.setAttribute('alt', title);
		button.appendChild(buttonImage);
		var buttonSize = $('#button-panel').height();
		button.style.width = buttonSize + 'px';
		button.style.height = buttonSize + 'px';
		
		document.getElementById('button-panel').appendChild(button);
	};
	
	/**
	 * Main menu entry point.
	 */
	return function mainmenu() {
		// Display the more button
		addButton('More', 'More', 'image/ic_btn_more.png');
	};
});
