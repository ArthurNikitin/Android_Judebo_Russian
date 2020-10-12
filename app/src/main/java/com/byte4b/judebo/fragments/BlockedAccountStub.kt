package com.byte4b.judebo.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.byte4b.judebo.R
import com.byte4b.judebo.activities.MainActivity

class BlockedAccountStub : Fragment(R.layout.fragment_blocked_account_stub) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).restartFragment(SettingFragment())
    }

}