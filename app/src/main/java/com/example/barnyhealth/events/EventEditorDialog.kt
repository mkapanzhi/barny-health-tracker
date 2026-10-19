package com.example.barnyhealth.events

import android.app.Dialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.barnyhealth.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EventEditorDialog : DialogFragment() {
    private var dateKey = 0
    private lateinit var input: TextInputEditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        dateKey = savedInstanceState?.getInt("date") ?: args.getInt("date")
        val context = requireContext()
        val padding = (24 * resources.displayMetrics.density).toInt()
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding / 2, padding, 0)
        }
        val dateButton = MaterialButton(context).apply {
            isSaveEnabled = false
            fun label() = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                .format(EventDates.calendar(dateKey).time)
            text = label()
            contentDescription = getString(R.string.events_date)
            setOnClickListener {
                val cal = EventDates.calendar(dateKey)
                DatePickerDialog(context, { _, year, month, day ->
                    dateKey = year * 10000 + (month + 1) * 100 + day
                    text = label()
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }
        }
        content.addView(dateButton, LinearLayout.LayoutParams(-1, -2))
        val field = TextInputLayout(context).apply { hint = getString(R.string.events_text) }
        input = TextInputEditText(context).apply {
            isSaveEnabled = false
            minLines = 2
            maxLines = 6
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setText(savedInstanceState?.getString("text") ?: args.getString("text", ""))
        }
        field.addView(input)
        content.addView(field, LinearLayout.LayoutParams(-1, -2))
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(if (args.containsKey("id")) R.string.events_edit else R.string.events_add)
            .setView(content)
            .setNegativeButton(R.string.events_cancel, null)
            .setPositiveButton(R.string.events_save, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val text = input.text?.toString()?.trim().orEmpty()
                if (text.isBlank()) {
                    field.error = getString(R.string.events_required)
                } else {
                    parentFragmentManager.setFragmentResult("event_saved", bundleOf(
                        "id" to args.getString("id"), "date" to dateKey,
                        "text" to text, "created" to args.getLong("created")
                    ))
                    dismiss()
                }
            }
        }
        return dialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("date", dateKey)
        if (::input.isInitialized) outState.putString("text", input.text?.toString())
        super.onSaveInstanceState(outState)
    }
}
