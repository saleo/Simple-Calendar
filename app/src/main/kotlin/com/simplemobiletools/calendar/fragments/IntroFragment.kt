package com.simplemobiletools.calendar.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.calendar.R
import com.simplemobiletools.calendar.helpers.INTRO_TYPE
import kotlinx.android.synthetic.main.fragment_intro.view.*

class IntroFragment: Fragment() {

    private var mIntroType=""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_intro, container, false)
        mIntroType=arguments!!.getString(INTRO_TYPE)
        when (mIntroType){
            "skcal" -> view.txt_intro.text=getText(R.string.str_intro_skcal)
            "sk" -> view.txt_intro.text=getText(R.string.str_intro_sk)
            "donate" -> view.txt_intro.text=getText(R.string.str_intro_donate)
            "xiangyu" -> view.txt_intro.text=getText(R.string.str_intro_xiangyu)
        }
        return view;
    }
}
