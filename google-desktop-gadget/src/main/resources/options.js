/*
Copyright (C) 2008 Jeff Black

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/ 

// global variables
var selIndex = -1;				// which list item is selected
var strurls = "";				// the urls as a single string
var arrurls = new Array();		// array of urls

function onOpen() {
    // map minutes to droplist index
    var validMinuteIntervals = new Object();
    validMinuteIntervals[1] = 0;
    validMinuteIntervals[2] = 1;
    validMinuteIntervals[5] = 2;
    validMinuteIntervals[10] = 3;
    intervalMinutes.selectedIndex = validMinuteIntervals[options.getValue("intervalMinutesProp")];

    // load urls setting from data base
    strurls = options.getValue("hudsonUrlsProp");
    // split urls between each , comma
    arrurls = strurls.split(",");
    // load urls in to listview
    reload();
}

function onCancel() {
    // No settings will be saved.
}

function onOk() {
    // store refresh minutes
    options.putValue("intervalMinutesProp", intervalMinutes.value);
    // store hudson urls
	strurls = ""; // clear old url list
	for (var i=0; i<arrurls.length; i++) {
		// if url isn't deleted add it to the string
		if (arrurls[i] != "") {
			strurls += arrurls[i];
            // if it isn't the last item add a comma after it
			if (i < arrurls.length - 1) {
                strurls += ",";
            }
		}
	}
	options.putValue("hudsonUrlsProp", strurls);
}

function validateUrl() {
    var hudsonUrl = urlPath.value;
    var isValidate = true;

    var protocol = hudsonUrl.substring(4,0);
    if (hudsonUrl == null || hudsonUrl.length == 0 || protocol != "http") {
      isValidate = false;
    }

    return isValidate;
}

function intervalMinutes_onchange() {
    var intervalMinutesValue = intervalMinutes.value;
    // debug.trace("intervalMinutes=" + intervalMinutes);
}

// add, delete or update a list value
function updateUrlButton_onclick() {

	if (selIndex == -1) {
        return;
    }

	// add a new url to the list
	if (selIndex == arrurls.length) {
        if (validateUrl()) {
			arrurls[selIndex] = urlPath.value;	
		} else {
			alert("URL is not valid");
			return;
		}
    // remove a url from the list
	} else {
		if (selIndex == arrurls.length) {
            return; // only delete if it was in edit mode instead of add
        }
		arrurls[selIndex] = "";
	}
	// reload with the new list
	reload();
}

// new list item selected fill the textbox
function urlList_onchange() {
    var selectedItem = urlList.selectedItem;
	selIndex = selectedItem.name;
    urlPath.value = arrurls[selIndex];
	// new url has been selected
	if (selIndex == "new") {
		urlPath.value="http";
		selIndex = arrurls.length;
	}
	// update the text on the 'update' button
	urlpath_onchange();
}

// update the text on the 'update'/'delete'/'add' button
function urlpath_onchange(){
	if (selIndex == arrurls.length) {
        updateUrlButton.caption = BUTTON_ADD;
    } else {
		updateUrlButton.caption = BUTTON_REMOVE;
		if (selIndex == arrurls.length) {
            updateUrlButton.visible = false;
        } else {
            updateUrlButton.visible = true;
        }
    }
}

// reloads the urls list
function reload(){
	// clear old list items
	urlList.removeAllElements();
	// loop through all urls in array
	for (var i=0; i<arrurls.length; i++) {
		if (arrurls[i] != "") { // if url "" then it is deleted
			// add each url to list
			item = urlList.appendElement("<item name='"+i+"'></item>");
			item.appendElement("<label>"+arrurls[i]+"</label>");
		}
	}
	item = urlList.appendElement("<item name='new'></item>");
	item.appendElement("<label>"+ADD_URL+"</label>");
	// make 'add new url' selected
	urlPath.value = "http";
	selIndex = arrurls.length;
}
