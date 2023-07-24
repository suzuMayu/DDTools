package com.fmq.ddtools.ui.tools

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fmq.ddtools.databinding.FragmentToolsBinding
import com.fmq.ddtools.ui.tools.tablet.TabletEditActivity

class ToolsFragment : Fragment() {

    private var _binding: FragmentToolsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentToolsBinding.inflate(inflater, container, false)
        val toolsViewModel = ViewModelProvider(this)[ToolsViewModel::class.java]
        val tablet = binding.toolsTablet
        val tabletText = binding.toolsTabletText
        tablet.setOnClickListener {
            startActivity(Intent(activity, TabletEditActivity::class.java))
        }
        tabletText.setOnClickListener {
            startActivity(Intent(activity, TabletEditActivity::class.java))
        }

        toolsViewModel.text.observe(viewLifecycleOwner) {

        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}