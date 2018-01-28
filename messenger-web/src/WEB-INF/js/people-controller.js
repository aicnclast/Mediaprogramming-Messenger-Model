/**
 * de_sb_messenger.PeopleController: messenger people controller.
 * Copyright (c) 2013 Sascha Baumeister
 */
"use strict";

this.de_sb_messenger = this.de_sb_messenger || {};
(function () {
	// imports
	const Controller = de_sb_messenger.Controller;
	const AJAX = de_sb_util.AJAX;
	const APPLICATION = de_sb_messenger.APPLICATION;

	// parameter names for person criteria queries
	const QUERY_PARAMETER_NAMES = ["email", "givenName", "familyName", "street", "city"];


	/**
	 * Creates a new people controller that is derived from an abstract controller.
	 * @param entityCache {de_sb_util.EntityCache} an entity cache
	 */
	const PeopleController = de_sb_messenger.PeopleController = function (entityCache) {
		Controller.call(this, 2, entityCache);
	}
	PeopleController.prototype = Object.create(Controller.prototype);
	PeopleController.prototype.constructor = PeopleController;


	/**
	 * Displays the associated view.
	 */
	PeopleController.prototype.display = function () {
		if (!APPLICATION.sessionUser) return;

		Controller.prototype.display.call(this);
		this.displayStatus(200, "OK");

		const mainElement = document.querySelector("main");
		let sectionElement = document.querySelector("#people-observing-template").content.cloneNode(true).firstElementChild;
		this.refreshAvatarSlider(sectionElement.querySelector("div.image-slider"), APPLICATION.sessionUser.observingReferences, this.toggleObservation);
		mainElement.appendChild(sectionElement);

		sectionElement = document.querySelector("#people-observed-template").content.cloneNode(true).firstElementChild;
		this.refreshAvatarSlider(sectionElement.querySelector("div.image-slider"), APPLICATION.sessionUser.observedReferences, this.toggleObservation);
		mainElement.appendChild(sectionElement);

		sectionElement = document.querySelector("#candidates-template").content.cloneNode(true).firstElementChild;
		sectionElement.querySelector("button").addEventListener("click", this.query.bind(this));
		mainElement.appendChild(sectionElement);
	}


	/**
	 * Performs a REST based criteria query, and refreshes the people
	 * view's bottom avatar slider with the result.
	 */
	PeopleController.prototype.query = function () {
		// TODO
    const inputElements = document.querySelectorAll("section.candidates input");
    const header = {"Accept": "application/json"};
    let path = "/services/people?";
		const params = [];
    inputElements.forEach((input, index) => {
    	let param = input.value.trim();
    	if(!!param){
    		params.push(QUERY_PARAMETER_NAMES[index] + "=" + param);
			}
		});
    path += params.join("&");
    AJAX.invoke(path, "GET", header, null, null, request => {
      if (request.status === 200) {
				var response = JSON.parse(request.responseText);
				var identities = [];
				response.forEach(person => {
					identities.push(person.identity);
				});

      	this.refreshAvatarSlider(document.querySelector("section.candidates .image-slider"), identities, this.toggleObservation);
			} else {
	      this.displayStatus(request.status, request.statusText);
	    }
		});
	}


	/**
	 * Updates the user's observed people with the given person. Removes the
	 * person if it is already observed by the user, or adds it if not.
	 * @param {String} personIdentity the identity of the person to add or remove
	 */
	PeopleController.prototype.toggleObservation = function (personIdentity) {
    // TODO
		const user = de_sb_messenger.APPLICATION.sessionUser;
		const observed = user.observedReferences.slice(0); // slice(0) returns a clone
		const index = observed.indexOf(personIdentity);
		(index >= 0) ? observed.splice(index, 1) : observed.push(personIdentity);
		const data = []
    observed.forEach(id => {
      data.push(encodeURIComponent(`peopleObserved=${id}`));
		});

    const header = {"Content-Type": "application/x-www-form-urlencoded"};
    const body = data.join('&');
    let path = `/services/people/${user.identity}/peopleObserved`;

    AJAX.invoke(path, "PUT", header, body, null, request => {
      if (request.status === 204) {
      	user.observedReferences = observed;
				this.refreshAvatarSlider(document.querySelector("section.people-observed .image-slider"), observed, this.toggleObservation);
			} else {
  	    this.displayStatus(request.status, request.statusText);
   	 	}
		});
  }

} ());