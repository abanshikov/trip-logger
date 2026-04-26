package com.triplogger.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.triplogger.R
import com.triplogger.utils.PreferencesManager

class SettingsFragment : Fragment() {
    private lateinit var prefs: PreferencesManager
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager(requireContext())
        
        val summerNormInput = view.findViewById<TextInputEditText>(R.id.summerNormInput)
        val winterNormInput = view.findViewById<TextInputEditText>(R.id.winterNormInput)
        val winterStartSpin = view.findViewById<Spinner>(R.id.winterStartSpinner)
        val winterEndSpin = view.findViewById<Spinner>(R.id.winterEndSpinner)
        
        summerNormInput.setText(prefs.summerNorm.toString())
        winterNormInput.setText(prefs.winterNorm.toString())
        
        val months = (1..12).map { if (it < 10) "0$it" else "$it" }
        winterStartSpin.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        winterEndSpin.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        
        winterStartSpin.setSelection(prefs.winterStartMonth - 1)
        winterEndSpin.setSelection(prefs.winterEndMonth - 1)
        
        view.findViewById<Button>(R.id.saveSettingsButton).setOnClickListener {
            prefs.summerNorm = summerNormInput.text.toString().toFloatOrNull() ?: 10.5f
            prefs.winterNorm = winterNormInput.text.toString().toFloatOrNull() ?: 11.5f
            prefs.winterStartMonth = winterStartSpin.selectedItemPosition + 1
            prefs.winterEndMonth = winterEndSpin.selectedItemPosition + 1
            Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()
        }
    }
}
