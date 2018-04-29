/*
 * Copyright (c) Ricki Hirner (bitfire web engineering).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.icsdroid

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder

class AccountAuthenticatorService: Service() {

    private val accountAuthenticator: AccountAuthenticator by lazy { AccountAuthenticator(this) }

	override fun onBind(intent: Intent): IBinder? {
		if (intent.action == android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT)
			return accountAuthenticator.iBinder
		return null
	}
	
	
	class AccountAuthenticator(
            val context: Context
    ): AbstractAccountAuthenticator(context) {

        override fun addAccount(response: AccountAuthenticatorResponse?, accountType: String, authTokenType: String?, requiredFeatures: Array<String>?, options: Bundle?): Bundle? {
			return null
		}

        override fun getAuthTokenLabel(p0: String?) = null
        override fun confirmCredentials(p0: AccountAuthenticatorResponse?, p1: Account?, p2: Bundle?) = null
        override fun updateCredentials(p0: AccountAuthenticatorResponse?, p1: Account?, p2: String?, p3: Bundle?) = null
        override fun getAuthToken(p0: AccountAuthenticatorResponse?, p1: Account?, p2: String?, p3: Bundle?) = null
        override fun hasFeatures(p0: AccountAuthenticatorResponse?, p1: Account?, p2: Array<out String>?) = null
        override fun editProperties(p0: AccountAuthenticatorResponse?, p1: String?) = null

	}

}
