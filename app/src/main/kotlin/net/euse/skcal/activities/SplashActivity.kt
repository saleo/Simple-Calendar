package net.euse.skcal.activities

import android.content.Intent
import net.euse.skcal.helpers.DAY_CODE
import net.euse.skcal.helpers.EVENT_ID
import net.euse.skcal.helpers.EVENT_OCCURRENCE_TS
import net.euse.skcal.helpers.OPEN_MONTH
import com.simplemobiletools.commons.activities.BaseSplashActivity

class SplashActivity : BaseSplashActivity() {
    override fun initActivity() {
        when {
            intent.extras?.containsKey(DAY_CODE) == true -> Intent(this, net.euse.skcal.activities.MainActivity::class.java).apply {
                putExtra(DAY_CODE, intent.getStringExtra(DAY_CODE))
                putExtra(OPEN_MONTH, intent.getBooleanExtra(OPEN_MONTH, false))
                startActivity(this)
            }
            intent.extras?.containsKey(EVENT_ID) == true -> Intent(this, net.euse.skcal.activities.MainActivity::class.java).apply {
                putExtra(EVENT_ID, intent.getIntExtra(EVENT_ID, 0))
                putExtra(EVENT_OCCURRENCE_TS, intent.getIntExtra(EVENT_OCCURRENCE_TS, 0))
                startActivity(this)
            }
            else -> startActivity(Intent(this, net.euse.skcal.activities.MainActivity::class.java))
        }
        finish()
    }
}
