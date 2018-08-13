package com.simplemobiletools.commons.activities

import android.os.Bundle
import android.view.LayoutInflater
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.underlineText
import com.simplemobiletools.commons.helpers.APP_FAQ
import com.simplemobiletools.commons.models.FAQItem
import kotlinx.android.synthetic.main.activity_faq.*
import kotlinx.android.synthetic.main.license_faq_item.view.*
import java.util.*

class FAQActivity : BaseSimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)

        val titleColor = getAdjustedPrimaryColor()
        val textColor = baseConfig.textColor

        val inflater = LayoutInflater.from(this)
        val faqItems = intent.getSerializableExtra(APP_FAQ) as ArrayList<FAQItem>
        faqItems.forEach {
            val faqItem = it
            inflater.inflate(R.layout.license_faq_item, null).apply {
                license_faq_title.apply {
                    text = if (faqItem.title is Int) getString(faqItem.title) else faqItem.title as String
                    underlineText()
                    setTextColor(titleColor)
                }

                license_faq_text.apply {
                    text = if (faqItem.text is Int) getString(faqItem.text) else faqItem.text as String
                    setTextColor(textColor)
                }
                faq_holder.addView(this)
            }
        }
    }
}
