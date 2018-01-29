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
	MessagesController.prototype.displayRootMessages = async function (parentId = null) {
		// TODO
		const sessionUser = APPLICATION.sessionUser;
    let subjectIdentities = [parentId];
		if(parentId == null) { //root level
      subjectIdentities = [sessionUser.identity].concat(sessionUser.observedReferences);
    }
		const messages = [];
		const options = {method: "GET", headers: {"Accept": "application/json"}, credentials: "include"};
		for (let identity of subjectIdentities) { //await wont't work with forEach
			let path = `/services/entities/${identity}/messagesCaused`;
			let request = await fetch(path, options);
			if (request.status == 200) {
				let response = await request.json();
				messages.push(...response); //map response to push it into messages instead of reassigning a new array with concat
			} else {
				this.displayStatus(request.status, request.statusText);
			}
		}
    messages.sort(function(a,b) {return (b.creationTimestamp - a.creationTimestamp);}); // absteigendes Erzeugungsdatum

		//Add Elements to View:
    let queryString = "section.messages ul";
    if (parentId != null) {
      queryString += ` [data-identity='${parentId}']`;
      document.querySelector(queryString).className += " extended";
      queryString += " ul";
    }
		const messageList = document.querySelector(queryString);
    while (messageList.lastChild) messageList.removeChild(messageList.lastChild); //clear list
    messages.forEach(message => {
      const messageElement = document.querySelector("#message-output-template").content.cloneNode(true).firstElementChild;

      this.entityCache.resolve(message.authorReference, author => {
        messageElement.querySelector("img").src = `/services/people/${author.identity}/avatar`;
        let time = new Date(message.creationTimestamp);
        messageElement.querySelector("output").innerText = `${author.name.given} ${author.name.family} (${time.toLocaleDateString()}, ${time.toLocaleTimeString()})`;
        messageElement.querySelector("div:nth-of-type(2) output").innerText = message.body;
        messageElement.querySelector("a").addEventListener("click", () => {this.displayRootMessages(message.identity);});
        messageElement.querySelector("a:nth-of-type(2)").addEventListener("click", () => {this.displayMessageEditor(messageElement, message.identity);});

        messageElement.dataset.identity = message.identity; //make it easier to find
        messageList.appendChild(messageElement);
      });
    });

	}


	/**
	 * Discards an existing message editor if present, and displays a new one
	 * for the given subject.
	 * @param parentElement {Element} the parent element
	 * @param subjectIdentity {String} the subject identity
	 */
	MessagesController.prototype.displayMessageEditor = function (parentElement, subjectIdentity) {
		// TODO
    while (document.querySelector(".message-input")) document.querySelector(".message-input").remove(); //remove existing editors (not supported in IE), added message-input as class in the template
    const sessionUser = APPLICATION.sessionUser.identity;

    this.entityCache.resolve(sessionUser, user => {
      parentElement.className += "extended";
      const messageEditor = document.querySelector("#message-input-template").content.cloneNode(true).firstElementChild;

      messageEditor.querySelector("img").src = `/services/people/${user.identity}/avatar`;
      let time = new Date(); //default is now
      messageEditor.querySelector("output").innerText = `${user.name.given} ${user.name.family} (${time.toLocaleDateString()}, ${time.toLocaleTimeString()})`;
			//subjectIdentity und Autor werden als parameter an persist übergeben, statt sie im dom einzubinden.
      messageEditor.querySelector("button").addEventListener("click", () => {this.persistMessage(messageEditor, subjectIdentity);});

      const parentList = parentElement.querySelector("ul");
      parentList.insertBefore(messageEditor, parentList.firstChild); //insert at the top if replies are shown;
    });
  }


	/**
	 * Persists a new message with the current user as author, and the given
	 * subject.
	 * @param messageElement {element} the message element
	 * @param subjectIdentity the subject identity
	 */
	MessagesController.prototype.persistMessage = function (messageElement, subjectIdentity) {
		// TODO

    const text = messageElement.querySelector("textarea").value.trim();
    const data = [];
    data.push(encodeURIComponent(`authorReference=${APPLICATION.sessionUser.identity}`));
    data.push(encodeURIComponent(`subjectReference=${subjectIdentity}`));
    data.push(encodeURIComponent(`body=${text}`));

    const header = {"Content-Type": "application/x-www-form-urlencoded"};
    const body = data.join('&');
    let path = `/services/messages`;

    AJAX.invoke(path, "PUT", header, body, null, request => {
      if (request.status === 204) {
      	this.displayRootMessages();
        this.displayStatus("OK", "Message sent");
      } else {
        this.displayStatus(request.status, request.statusText);
      }
    });
  }
} ());