package com.jenny.deara.diary

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.jenny.deara.R
import com.jenny.deara.databinding.FragmentDiaryBinding
import com.jenny.deara.databinding.FragmentDiaryEditVer1Binding

class DiaryEditVer1Fragment : Fragment() {

    private lateinit var binding: FragmentDiaryEditVer1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_diary_edit_ver1, container, false)

        binding.toggleBtn1.setOnClickListener {
            it.findNavController().navigate(R.id.action_diaryEditVer1Fragment_to_diaryEditVer2Fragment)
        }
        return binding.root
    }
}