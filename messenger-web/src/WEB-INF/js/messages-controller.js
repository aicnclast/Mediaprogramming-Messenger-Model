/**
 * de_sb_messenger.MessagesController: messenger messages controller.
 * Copyright (c) 2013 Sascha Baumeister
 */
"use strict";

this.de_sb_messenger = this.de_sb_messenger || {};
(function () {
	// imports
	const Semaphore = de_sb_util.Semaphore;
	const StatusAccumulator = de_sb_util.StatusAccumulator;
	const Controller = de_sb_messenger.Controller;
	const AJAX = de_sb_util.AJAX;
	const APPLICATION = de_sb_messenger.APPLICATION;


	/**
	 * Creates a new messages controller that is derived from an abstract controller.
	 * @param entityCache {de_sb_util.EntityCache} an entity cache
	 */
	const MessagesController = de_sb_messenger.MessagesController = function (entityCache) {
		Controller.call(this, 1, entityCache);
	}
	MessagesController.prototype = Object.create(Controller.prototype);
	MessagesController.prototype.constructor = MessagesController;


	/**
	 * Displays the associated view.
	 */
	MessagesController.prototype.display = function () {
		const sessionUser = APPLICATION.sessionUser;
		if (!sessionUser) return;
		Controller.prototype.display.call(this);

		const subjectIdentities = [sessionUser.identity].concat(sessionUser.observedReferences);
		const mainElement = document.querySelector("main");
		const subjectsElement = document.querySelector("#subjects-template").content.cloneNode(true).firstElementChild;
		const messagesElement = document.querySelector("#messages-template").content.cloneNode(true).firstElementChild;
		mainElement.appendChild(subjectsElement);
		mainElement.appendChild(messagesElement);

		this.refreshAvatarSlider(subjectsElement.querySelector("div.image-slider"), subjectIdentities, this.displayMessageEditor.bind(this, messagesElement));
		this.displayRootMessages();
	}


	/**
	 * Displays the root messages.
	 */
	MessagesController.prototype.displayRootMessages = function () {
		// TODO
	}


	/**
	 * Discards an existing message editor if present, and displays a new one
	 * for the given subject.
	 * @param parentElement {Element} the parent element
	 * @param subjectIdentity {String} the subject identity
	 */
	MessagesController.prototype.displayMessageEditor = function (parentElement, subjectIdentity) {
		// TODO
	}


	/**
	 * Persists a new message with the current user as author, and the given
	 * subject.
	 * @param messageElement {element} the message element
	 * @param subjectIdentity the subject identity
	 */
	MessagesController.prototype.persistMessage = function (messageElement, subjectIdentity) {
		// TODO
	}
} ());