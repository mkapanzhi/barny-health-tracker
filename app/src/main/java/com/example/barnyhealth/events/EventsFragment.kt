package com.example.barnyhealth.events

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.barnyhealth.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class EventsFragment : Fragment() {
    private var selected = EventDates.today()
    private val month = MutableStateFlow(EventDates.month(selected))
    private var events = emptyList<CalendarEvent>()
    private lateinit var grid: GridLayout
    private lateinit var list: LinearLayout
    private lateinit var title: MaterialTextView
    private lateinit var dayTitle: MaterialTextView
    private lateinit var status: MaterialTextView
    private lateinit var add: MaterialButton
    private val dao get() = EventDatabase.get(requireContext()).events()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selected = savedInstanceState?.getInt("selected") ?: selected
        month.value = savedInstanceState?.getInt("month") ?: EventDates.month(selected)
        childFragmentManager.setFragmentResultListener("event_saved", this) { _, result ->
            val date = result.getInt("date")
            val now = System.currentTimeMillis()
            val oldId = result.getString("id")
            val event = CalendarEvent(oldId ?: UUID.randomUUID().toString(), date,
                result.getString("text").orEmpty(),
                if (oldId == null) now else result.getLong("created"), now)
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    if (oldId == null) dao.insert(event)
                    else check(dao.update(event) == 1)
                    selected = date
                    val target = EventDates.month(date)
                    if (month.value == target) render() else month.value = target
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage(R.string.events_save_failed)
                        .setPositiveButton(R.string.events_retry) { _, _ -> editor(event) }
                        .setNegativeButton(R.string.events_cancel, null).show()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        val scroll = NestedScrollView(requireContext()).apply {
            setBackgroundResource(R.drawable.bg_main_gradient)
            isFillViewport = true
        }
        val body = column().apply { setPadding(dp(16), dp(16), dp(16), dp(24)) }
        scroll.addView(body)
        body.addView(label(getString(R.string.events_title), 26f))
        val row = LinearLayout(requireContext()).apply { gravity = Gravity.CENTER_VERTICAL }
        row.addView(button("‹", getString(R.string.events_previous)) { changeMonth(-1) },
            LinearLayout.LayoutParams(dp(56), dp(56)))
        title = label("", 20f).apply { gravity = Gravity.CENTER }
        row.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(button("›", getString(R.string.events_next)) { changeMonth(1) },
            LinearLayout.LayoutParams(dp(56), dp(56)))
        body.addView(row)
        val card = MaterialCardView(requireContext()).apply {
            radius = dp(24).toFloat()
            setCardBackgroundColor(color(com.google.android.material.R.attr.colorSurface))
            strokeColor = color(com.google.android.material.R.attr.colorOutline)
            strokeWidth = dp(1)
        }
        grid = GridLayout(requireContext()).apply {
            columnCount = 7
            setPadding(dp(4), dp(8), dp(4), dp(8))
        }
        card.addView(grid, ViewGroup.LayoutParams(-1, -2))
        body.addView(card, LinearLayout.LayoutParams(-1, -2))
        body.addView(label(getString(R.string.events_legend), 13f))
        add = button(getString(R.string.events_add)) { editor() }
        body.addView(add, LinearLayout.LayoutParams(-1, -2))
        dayTitle = label("", 19f)
        body.addView(dayTitle)
        status = label("", 15f)
        body.addView(status)
        list = column()
        body.addView(list)
        return scroll
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        render()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                month.collectLatest { key ->
                    events = emptyList()
                    status.setText(R.string.events_loading)
                    render()
                    try {
                        dao.observeMonth(key, EventDates.last(key)).collectLatest {
                            events = it
                            status.text = ""
                            render()
                        }
                    } catch (e: CancellationException) { throw e }
                    catch (e: Exception) { status.setText(R.string.events_load_failed) }
                }
            }
        }
    }

    private fun changeMonth(delta: Int) {
        val next = EventDates.shift(month.value, delta)
        if (next / 10000 !in 1..9999) return
        selected = next
        month.value = next
    }

    private fun render() {
        if (!::grid.isInitialized || view == null) return
        title.text = SimpleDateFormat("LLLL yyyy", Locale.getDefault())
            .format(EventDates.calendar(month.value).time)
        grid.removeAllViews()
        val counts = events.groupingBy { it.dateKey }.eachCount()
        getString(R.string.events_weekdays).split("|").forEach { day ->
            grid.addView(label(day, 12f).apply { gravity = Gravity.CENTER }, cell(dp(32)))
        }
        val offset = EventDates.offset(month.value)
        val days = EventDates.last(month.value) % 100
        val slots = ((offset + days + 6) / 7) * 7
        repeat(slots) { index ->
            val day = index - offset + 1
            if (day !in 1..days) {
                grid.addView(View(requireContext()), cell(dp(56)))
            } else {
                val key = month.value + day - 1
                val count = counts[key] ?: 0
                val isSelected = key == selected
                val today = key == EventDates.today()
                val dayView = MaterialTextView(requireContext()).apply {
                    gravity = Gravity.CENTER
                    textSize = 14f
                    text = "$day" + if (count > 0) "\n• $count" else ""
                    setTextColor(color(if (isSelected) com.google.android.material.R.attr.colorOnPrimary
                        else com.google.android.material.R.attr.colorOnSurface))
                    if (today || count > 0) setTypeface(typeface, Typeface.BOLD)
                    background = GradientDrawable().apply {
                        cornerRadius = dp(12).toFloat()
                        setColor(if (isSelected) color(androidx.appcompat.R.attr.colorPrimary)
                            else if (count > 0) color(com.google.android.material.R.attr.colorPrimaryContainer)
                            else Color.TRANSPARENT)
                        if (today) setStroke(dp(2), color(com.google.android.material.R.attr.colorSecondary))
                    }
                    isFocusable = true
                    isClickable = true
                    contentDescription = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                        .format(EventDates.calendar(key).time) + ", " + getString(R.string.events_count, count)
                    setOnClickListener { selected = key; render() }
                }
                grid.addView(dayView, cell(dp(56)))
            }
        }
        dayTitle.text = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            .format(EventDates.calendar(selected).time)
        list.removeAllViews()
        val daily = events.filter { it.dateKey == selected }
        if (daily.isEmpty()) list.addView(label(getString(R.string.events_empty), 16f))
        daily.forEach { event ->
            val card = MaterialCardView(requireContext()).apply {
                radius = dp(20).toFloat()
                setCardBackgroundColor(color(com.google.android.material.R.attr.colorSurface))
                strokeColor = color(com.google.android.material.R.attr.colorOutline)
                strokeWidth = dp(1)
            }
            val content = column().apply { setPadding(dp(16), dp(12), dp(16), dp(8)) }
            content.addView(label(event.text, 16f))
            val actions = LinearLayout(requireContext()).apply { gravity = Gravity.END }
            actions.addView(button(getString(R.string.events_edit)) { editor(event) })
            actions.addView(button(getString(R.string.events_delete)) {
                MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.events_delete_question)
                    .setMessage(event.text).setNegativeButton(R.string.events_cancel, null)
                    .setPositiveButton(R.string.events_delete) { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            try { dao.delete(event.id) }
                            catch (e: CancellationException) { throw e }
                            catch (e: Exception) {
                                Toast.makeText(requireContext(), R.string.events_delete_failed, Toast.LENGTH_LONG).show()
                            }
                        }
                    }.show()
            })
            content.addView(actions)
            card.addView(content)
            list.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
        }
    }

    private fun editor(event: CalendarEvent? = null) {
        if (childFragmentManager.findFragmentByTag("event_editor") != null) return
        EventEditorDialog().apply {
            arguments = if (event == null) bundleOf("date" to selected)
                else bundleOf("id" to event.id, "date" to event.dateKey,
                    "text" to event.text, "created" to event.createdAt)
        }.show(childFragmentManager, "event_editor")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("selected", selected)
        outState.putInt("month", month.value)
        super.onSaveInstanceState(outState)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun color(attr: Int) = MaterialColors.getColor(requireContext(), attr, "Events")
    private fun column() = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }
    private fun label(value: String, size: Float) = MaterialTextView(requireContext()).apply {
        text = value
        textSize = size
        setTextColor(color(com.google.android.material.R.attr.colorOnSurface))
        setPadding(0, dp(8), 0, dp(8))
    }
    private fun button(value: String, description: String = value, action: () -> Unit) =
        MaterialButton(requireContext()).apply {
            text = value
            contentDescription = description
            setOnClickListener { action() }
        }
    private fun cell(height: Int) = GridLayout.LayoutParams().apply {
        width = 0
        this.height = height
        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        setMargins(dp(1), dp(1), dp(1), dp(1))
    }
}
