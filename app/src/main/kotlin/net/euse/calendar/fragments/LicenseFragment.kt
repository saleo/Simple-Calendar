package net.euse.calendar.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.extensions.launchViewIntent
import com.simplemobiletools.commons.extensions.underlineText
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.License
import kotlinx.android.synthetic.main.fragment_about_license.*
import kotlinx.android.synthetic.main.license_faq_item.view.*
import net.euse.calendar.R
import net.euse.calendar.activities.MainActivity
import net.euse.calendar.extensions.config
import net.euse.calendar.helpers.ABOUT_LICENSE_VIEW
import net.euse.calendar.helpers.Formatter

class LicenseFragment: MyFragmentHolder() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view=inflater.inflate(R.layout.fragment_about_license,container,false)
        view.background = ColorDrawable(context!!.config.backgroundColor)
        return view
    }

    override fun onResume() {
        super.onResume()
        currentDayCode= Formatter.getTodayCode(context!!)
        (activity as MainActivity).updateTopBottom(view = ABOUT_LICENSE_VIEW)

//        val linkColor = getAdjustedPrimaryColor()
//        val textColor = baseConfig.textColor
//        context.updateTextColors(ll_licenses_holder)

        val inflater = LayoutInflater.from(activity)
        val licenses = initLicenses()
        val licenseMask = (activity as MainActivity).intent.getIntExtra(APP_LICENSES, 0) or LICENSE_SMT or LICENSE_KOTLIN or LICENSE_JODA or LICENSE_LEAK_CANARY or LICENSE_STETHO or LICENSE_GLCV or LICENSE_NUMBER_PICKER
        licenses.filter { licenseMask and it.id != 0 }.forEach {
            val license = it
            inflater.inflate(R.layout.license_faq_item, null).apply {
                license_faq_title.apply {
                    text = getString(license.titleId)
                    underlineText()
//                    setTextColor(linkColor)
                    setOnClickListener {
                        (activity as MainActivity).launchViewIntent(license.urlId)
                    }
                }

                license_faq_text.text = getString(license.textId)
//                license_faq_text.setTextColor(textColor)
                ll_licenses_holder.addView(this)
            }
        }

    }

    private fun initLicenses() = arrayOf(
            License(LICENSE_SMT, com.simplemobiletools.commons.R.string.simplecalendar_title, com.simplemobiletools.commons.R.string.simplecalendar_text, com.simplemobiletools.commons.R.string.simplecalendar_url),
            License(LICENSE_SMT, com.simplemobiletools.commons.R.string.simplecalendar_title, com.simplemobiletools.commons.R.string.simplecalendar_text, com.simplemobiletools.commons.R.string.simplecalendar_url),
            License(LICENSE_KOTLIN, com.simplemobiletools.commons.R.string.kotlin_title, com.simplemobiletools.commons.R.string.kotlin_text, com.simplemobiletools.commons.R.string.kotlin_url),
            License(LICENSE_SUBSAMPLING, com.simplemobiletools.commons.R.string.subsampling_title, com.simplemobiletools.commons.R.string.subsampling_text, com.simplemobiletools.commons.R.string.subsampling_url),
            License(LICENSE_GLIDE, com.simplemobiletools.commons.R.string.glide_title, com.simplemobiletools.commons.R.string.glide_text, com.simplemobiletools.commons.R.string.glide_url),
            License(LICENSE_CROPPER, com.simplemobiletools.commons.R.string.cropper_title, com.simplemobiletools.commons.R.string.cropper_text, com.simplemobiletools.commons.R.string.cropper_url),
            License(LICENSE_MULTISELECT, com.simplemobiletools.commons.R.string.multiselect_title, com.simplemobiletools.commons.R.string.multiselect_text, com.simplemobiletools.commons.R.string.multiselect_url),
            License(LICENSE_RTL, com.simplemobiletools.commons.R.string.rtl_viewpager_title, com.simplemobiletools.commons.R.string.rtl_viewpager_text, com.simplemobiletools.commons.R.string.rtl_viewpager_url),
            License(LICENSE_JODA, com.simplemobiletools.commons.R.string.joda_title, com.simplemobiletools.commons.R.string.joda_text, com.simplemobiletools.commons.R.string.joda_url),
            License(LICENSE_STETHO, com.simplemobiletools.commons.R.string.stetho_title, com.simplemobiletools.commons.R.string.stetho_text, com.simplemobiletools.commons.R.string.stetho_url),
            License(LICENSE_OTTO, com.simplemobiletools.commons.R.string.otto_title, com.simplemobiletools.commons.R.string.otto_text, com.simplemobiletools.commons.R.string.otto_url),
            License(LICENSE_PHOTOVIEW, com.simplemobiletools.commons.R.string.photoview_title, com.simplemobiletools.commons.R.string.photoview_text, com.simplemobiletools.commons.R.string.photoview_url),
            License(LICENSE_PICASSO, com.simplemobiletools.commons.R.string.picasso_title, com.simplemobiletools.commons.R.string.picasso_text, com.simplemobiletools.commons.R.string.picasso_url),
            License(LICENSE_PATTERN, com.simplemobiletools.commons.R.string.pattern_title, com.simplemobiletools.commons.R.string.pattern_text, com.simplemobiletools.commons.R.string.pattern_url),
            License(LICENSE_REPRINT, com.simplemobiletools.commons.R.string.reprint_title, com.simplemobiletools.commons.R.string.reprint_text, com.simplemobiletools.commons.R.string.reprint_url),
            License(LICENSE_GIF_DRAWABLE, com.simplemobiletools.commons.R.string.gif_drawable_title, com.simplemobiletools.commons.R.string.gif_drawable_text, com.simplemobiletools.commons.R.string.gif_drawable_url),
            License(LICENSE_AUTOFITTEXTVIEW, com.simplemobiletools.commons.R.string.autofittextview_title, com.simplemobiletools.commons.R.string.autofittextview_text, com.simplemobiletools.commons.R.string.autofittextview_url),
            License(LICENSE_ROBOLECTRIC, com.simplemobiletools.commons.R.string.robolectric_title, com.simplemobiletools.commons.R.string.robolectric_text, com.simplemobiletools.commons.R.string.robolectric_url),
            License(LICENSE_ESPRESSO, com.simplemobiletools.commons.R.string.espresso_title, com.simplemobiletools.commons.R.string.espresso_text, com.simplemobiletools.commons.R.string.espresso_url),
            License(LICENSE_GSON, com.simplemobiletools.commons.R.string.gson_title, com.simplemobiletools.commons.R.string.gson_text, com.simplemobiletools.commons.R.string.gson_url),
            License(LICENSE_LEAK_CANARY, com.simplemobiletools.commons.R.string.leak_canary_title, com.simplemobiletools.commons.R.string.leakcanary_text, com.simplemobiletools.commons.R.string.leakcanary_url),
            License(LICENSE_NUMBER_PICKER, com.simplemobiletools.commons.R.string.number_picker_title, com.simplemobiletools.commons.R.string.number_picker_text, com.simplemobiletools.commons.R.string.number_picker_url),
            License(LICENSE_GLCV, com.simplemobiletools.commons.R.string.glcv_title, com.simplemobiletools.commons.R.string.glcv_text, com.simplemobiletools.commons.R.string.glcv_url)    )

    //all below are for placeholder purpose
    override fun goToToday() {
    }

    override fun refreshEvents() {
    }

    override fun shouldGoToTodayBeVisible(): Boolean {
        return true
    }

    override fun updateActionBarTitle() {
    }


    override fun getNewEventDayCode(): String {
        return ""
    }
}
