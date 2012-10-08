/*! @license 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2011-2012 ForgeRock AS. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

/**
 * @author jdabrowski
 * 
 * This script validates if notification is valid.
 */

var errors = [];

function requiredValidator(toValidate, fieldName) {
    if (!toValidate || toValidate === "") {
        errors.push(fieldName + " is required");
        return false;
    }
    return true;
}

function isNotificationValid() {
    var notification = openidm.decrypt(object);
    requiredValidator(notification.type, "Notification Type");
    requiredValidator(notification.message, "Notification Message");
    requiredValidator(notification.requestDate, "Request Date");
    requiredValidator(notification.requester, "Notification Requester");
    requiredValidator(notification.userName, "Notification User");
    if(errors.length > 0) {
    	throw errors;
    }
};

isNotificationValid();