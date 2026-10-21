package com.example.barnyhealth.events

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
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
    private var loading = true
    private var loadFailed = false

    private lateinit var grid: GridLayout
    private lateinit var list: LinearLayout
    private lateinit var title: MaterialTextView
    private lateinit var dayTitle: MaterialTextView
    private lateinit var status: MaterialTextView

    private val dao
        get() = EventDatabase.get(requireContext()).events()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        selected = savedInstanceState?.getInt("selected") ?: selected
        month.value =
            savedInstanceState?.getInt("month") ?: EventDates.month(selected)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val scroll = NestedScrollView(requireContext()).apply {
            setBackgroundResource(R.drawable.bg_main_gradient)
            isFillViewport = true
            clipToPadding = false
        }

        val body = column().apply {
            setPadding(dp(16), dp(16), dp(16), dp(24))
        }

        scroll.addView(
            body,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        body.addView(
            label(getString(R.string.events_title), 26f)
        )

        val monthRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        monthRow.addView(
            monthArrow(
                symbol = "‹",
                description = getString(R.string.events_previous)
            ) {
                changeMonth(-1)
            },
            LinearLayout.LayoutParams(dp(48), dp(48))
        )

        title = label("", 20f).apply {
            gravity = Gravity.CENTER
        }

        monthRow.addView(
            title,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        monthRow.addView(
            monthArrow(
                symbol = "›",
                description = getString(R.string.events_next)
            ) {
                changeMonth(1)
            },
            LinearLayout.LayoutParams(dp(48), dp(48))
        )

        body.addView(
            monthRow,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(8)
            }
        )

        val calendarCard = MaterialCardView(requireContext()).apply {
            radius = dp(24).toFloat()
            cardElevation = 0f

            setCardBackgroundColor(
                color(com.google.android.material.R.attr.colorSurface)
            )

            strokeColor =
                color(com.google.android.material.R.attr.colorOutline)

            strokeWidth = dp(1)
        }

        grid = GridLayout(requireContext()).apply {
            columnCount = 7
            setPadding(dp(6), dp(8), dp(6), dp(8))
        }

        calendarCard.addView(
            grid,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        body.addView(
            calendarCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val addButton = button(
            value = getString(R.string.events_add)
        ) {
            editor()
        }

        body.addView(
            addButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
                bottomMargin = dp(8)
            }
        )

        dayTitle = label("", 19f)
        body.addView(dayTitle)

        status = label("", 15f).apply {
            visibility = View.GONE
        }
        body.addView(status)

        list = column()
        body.addView(
            list,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        return scroll
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.setFragmentResultListener(
            "event_saved",
            viewLifecycleOwner
        ) { _, result ->
            saveEvent(result)
        }

        render()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                month.collectLatest { key ->
                    events = emptyList()
                    loading = true
                    loadFailed = false
                    render()

                    try {
                        dao.observeMonth(
                            key,
                            EventDates.last(key)
                        ).collectLatest { loadedEvents ->
                            events = loadedEvents
                            loading = false
                            loadFailed = false
                            render()
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        loading = false
                        loadFailed = true
                        render()
                    }
                }
            }
        }
    }

    private fun saveEvent(result: Bundle) {
        val date = result.getInt("date")
        val now = System.currentTimeMillis()
        val oldId = result.getString("id")

        val event = CalendarEvent(
            id = oldId ?: UUID.randomUUID().toString(),
            dateKey = date,
            text = result.getString("text").orEmpty(),
            createdAt = if (oldId == null) {
                now
            } else {
                result.getLong("created")
            },
            updatedAt = now
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (oldId == null) {
                    dao.insert(event)
                } else {
                    check(dao.update(event) == 1)
                }

                selected = date

                val targetMonth = EventDates.month(date)

                if (month.value == targetMonth) {
                    render()
                } else {
                    events = emptyList()
                    loading = true
                    loadFailed = false
                    month.value = targetMonth
                    render()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.events_save_failed)
                    .setPositiveButton(R.string.events_retry) { _, _ ->
                        editor(
                            event = event,
                            isNew = oldId == null
                        )
                    }
                    .setNegativeButton(R.string.events_cancel, null)
                    .show()
            }
        }
    }

    private fun changeMonth(delta: Int) {
        val next = EventDates.shift(month.value, delta)

        if (next / 10000 !in 1..9999) return

        selected = next
        events = emptyList()
        loading = true
        loadFailed = false
        month.value = next

        render()
    }

    private fun render() {
        if (view == null || !::grid.isInitialized) return

        title.text = SimpleDateFormat(
            "LLLL yyyy",
            Locale.getDefault()
        ).format(
            EventDates.calendar(month.value).time
        )

        renderCalendar()
        renderEvents()
    }

    private fun renderCalendar() {
        grid.removeAllViews()

        val counts = events.groupingBy { it.dateKey }.eachCount()
        val todayKey = EventDates.today()

        getString(R.string.events_weekdays)
            .split("|")
            .forEach { weekday ->
                val weekdayView = MaterialTextView(requireContext()).apply {
                    text = weekday
                    textSize = 12f
                    gravity = Gravity.CENTER
                    includeFontPadding = false

                    setTextColor(
                        color(
                            com.google.android.material.R.attr
                                .colorOnSurfaceVariant
                        )
                    )

                    importantForAccessibility =
                        View.IMPORTANT_FOR_ACCESSIBILITY_NO
                }

                grid.addView(
                    weekdayView,
                    cell(dp(32))
                )
            }

        val offset = EventDates.offset(month.value)
        val days = EventDates.last(month.value) % 100
        val slots = ((offset + days + 6) / 7) * 7

        repeat(slots) { index ->
            val day = index - offset + 1

            if (day !in 1..days) {
                grid.addView(
                    View(requireContext()),
                    cell(dp(56))
                )
            } else {
                val dateKey = month.value + day - 1
                val count = counts[dateKey] ?: 0

                grid.addView(
                    createDayCell(
                        day = day,
                        dateKey = dateKey,
                        count = count,
                        isSelected = dateKey == selected,
                        isToday = dateKey == todayKey
                    ),
                    cell(dp(56))
                )
            }
        }
    }

    private fun createDayCell(
        day: Int,
        dateKey: Int,
        count: Int,
        isSelected: Boolean,
        isToday: Boolean
    ): View {
        val primary = color(androidx.appcompat.R.attr.colorPrimary)

        val textColor = color(
            if (isSelected) {
                com.google.android.material.R.attr.colorOnPrimary
            } else {
                com.google.android.material.R.attr.colorOnSurface
            }
        )

        val indicatorColor = if (isSelected) {
            textColor
        } else {
            primary
        }

        return FrameLayout(requireContext()).apply {
            background = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()

                setColor(
                    if (isSelected) primary else Color.TRANSPARENT
                )

                if (isToday && !isSelected) {
                    setStroke(dp(1), primary)
                }
            }

            isClickable = true
            isFocusable = true
            this.isSelected = isSelected

            val formattedDate = SimpleDateFormat(
                "dd MMMM yyyy",
                Locale.getDefault()
            ).format(
                EventDates.calendar(dateKey).time
            )

            contentDescription =
                "$formattedDate, " +
                        getString(R.string.events_count, count)

            val dayNumber = MaterialTextView(requireContext()).apply {
                text = day.toString()
                textSize = 16f
                gravity = Gravity.CENTER
                includeFontPadding = false
                setTextColor(textColor)

                if (isToday || isSelected) {
                    setTypeface(typeface, Typeface.BOLD)
                }

                importantForAccessibility =
                    View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }

            addView(
                dayNumber,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(30),
                    Gravity.TOP
                ).apply {
                    topMargin = dp(4)
                }
            )

            val eventCount = MaterialTextView(requireContext()).apply {
                text = "• $count"
                textSize = 11f
                gravity = Gravity.CENTER
                includeFontPadding = false
                setTextColor(indicatorColor)

                visibility = if (count > 0) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }

                importantForAccessibility =
                    View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }

            addView(
                eventCount,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(18),
                    Gravity.TOP
                ).apply {
                    topMargin = dp(34)
                }
            )

            setOnClickListener {
                selected = dateKey
                render()
            }
        }
    }

    private fun renderEvents() {
        dayTitle.text = SimpleDateFormat(
            "dd MMMM yyyy",
            Locale.getDefault()
        ).format(
            EventDates.calendar(selected).time
        )

        list.removeAllViews()

        when {
            loading -> {
                status.setText(R.string.events_loading)
                status.visibility = View.VISIBLE
                return
            }

            loadFailed -> {
                status.setText(R.string.events_load_failed)
                status.visibility = View.VISIBLE
                return
            }

            else -> {
                status.text = ""
                status.visibility = View.GONE
            }
        }

        val daily = events.filter { it.dateKey == selected }

        if (daily.isEmpty()) {
            list.addView(
                label(
                    getString(R.string.events_empty),
                    16f
                ).apply {
                    setTextColor(
                        color(
                            com.google.android.material.R.attr
                                .colorOnSurfaceVariant
                        )
                    )
                }
            )
            return
        }

        daily.forEach { event ->
            list.addView(
                createEventCard(event),
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(8)
                }
            )
        }
    }

    private fun createEventCard(
        event: CalendarEvent
    ): MaterialCardView {
        val card = MaterialCardView(requireContext()).apply {
            radius = dp(20).toFloat()
            cardElevation = 0f

            setCardBackgroundColor(
                color(com.google.android.material.R.attr.colorSurface)
            )

            strokeColor =
                color(com.google.android.material.R.attr.colorOutline)

            strokeWidth = dp(1)
        }

        val content = column().apply {
            setPadding(dp(16), dp(12), dp(16), dp(8))
        }

        content.addView(
            label(event.text, 16f)
        )

        val actions = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }

        actions.addView(
            button(getString(R.string.events_edit)) {
                editor(event)
            }
        )

        actions.addView(
            button(getString(R.string.events_delete)) {
                confirmDelete(event)
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dp(8)
            }
        )

        content.addView(actions)
        card.addView(content)

        return card
    }

    private fun confirmDelete(event: CalendarEvent) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.events_delete_question)
            .setMessage(event.text)
            .setNegativeButton(R.string.events_cancel, null)
            .setPositiveButton(R.string.events_delete) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        dao.delete(event.id)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            R.string.events_delete_failed,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .show()
    }

    private fun editor(
        event: CalendarEvent? = null,
        isNew: Boolean = false
    ) {
        if (childFragmentManager.isStateSaved) return

        if (
            childFragmentManager.findFragmentByTag("event_editor") != null
        ) {
            return
        }

        EventEditorDialog().apply {
            arguments = if (event == null) {
                bundleOf(
                    "date" to selected
                )
            } else {
                bundleOf(
                    "date" to event.dateKey,
                    "text" to event.text
                ).apply {
                    if (!isNew) {
                        putString("id", event.id)
                        putLong("created", event.createdAt)
                    }
                }
            }
        }.show(
            childFragmentManager,
            "event_editor"
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("selected", selected)
        outState.putInt("month", month.value)
        super.onSaveInstanceState(outState)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun color(attr: Int): Int {
        return MaterialColors.getColor(
            requireContext(),
            attr,
            "Events"
        )
    }

    private fun column(): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }
    }

    private fun label(
        value: String,
        size: Float
    ): MaterialTextView {
        return MaterialTextView(requireContext()).apply {
            text = value
            textSize = size

            setTextColor(
                color(com.google.android.material.R.attr.colorOnSurface)
            )

            setPadding(0, dp(8), 0, dp(8))
        }
    }

    private fun button(
        value: String,
        description: String = value,
        action: () -> Unit
    ): MaterialButton {
        return MaterialButton(requireContext()).apply {
            text = value
            contentDescription = description
            setOnClickListener { action() }
        }
    }

    private fun cell(height: Int): GridLayout.LayoutParams {
        return GridLayout.LayoutParams().apply {
            width = 0
            this.height = height

            columnSpec = GridLayout.spec(
                GridLayout.UNDEFINED,
                1f
            )

            setMargins(
                dp(1),
                dp(1),
                dp(1),
                dp(1)
            )
        }
    }

    private fun monthArrow(
        symbol: String,
        description: String,
        action: () -> Unit
    ): AppCompatTextView {
        val ripple = TypedValue()

        val hasRipple = requireContext().theme.resolveAttribute(
            android.R.attr.selectableItemBackgroundBorderless,
            ripple,
            true
        )

        return AppCompatTextView(requireContext()).apply {
            text = symbol
            textSize = 30f
            gravity = Gravity.CENTER
            includeFontPadding = false
            contentDescription = description

            setTextColor(
                color(com.google.android.material.R.attr.colorOnSurface)
            )

            if (hasRipple && ripple.resourceId != 0) {
                setBackgroundResource(ripple.resourceId)
            }

            isClickable = true
            isFocusable = true

            setOnClickListener { action() }
        }
    }
}