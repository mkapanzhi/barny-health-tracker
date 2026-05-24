//package com.example.barnyhealth.ui.weightdebug
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.example.barnyhealth.domain.AddMeasurementByMetricCodeUseCase
//import com.example.barnyhealth.domain.GetWeightHistoryUseCase
//
//class WeightDebugViewModelFactory(
//    private val addMeasurementByMetricCodeUseCase: AddMeasurementByMetricCodeUseCase,
//    private val getWeightHistoryUseCase: GetWeightHistoryUseCase
//) : ViewModelProvider.Factory {
//
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(WeightDebugViewModel::class.java)) {
//            return WeightDebugViewModel(
//                addMeasurementByMetricCodeUseCase = addMeasurementByMetricCodeUseCase,
//                getWeightHistoryUseCase = getWeightHistoryUseCase
//            ) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
//    }
//}
