package com.dataDoctor.rehabai.presentation.main

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.dataDoctor.rehabai.databinding.DialogDietRecordBinding
import com.dataDoctor.rehabai.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DietRecordDialog : DialogFragment() {

    private var _binding: DialogDietRecordBinding? = null
    private val binding get() = _binding!!

    // [수정] activityViewModels()로 변경하여 HomeFragment와 같은 ViewModel 인스턴스 공유
    private val viewModel: RehabViewModel by activityViewModels()

    // [추가] 수정 모드 데이터
    private var editModeId: String? = null
    private var currentPhotoUrl: String? = null
    private var selectedDate: java.util.Calendar = java.util.Calendar.getInstance()
    private var selectedPhotoUri: Uri? = null // [복구] 사진 URI 변수

    // 사진 선택기
    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedUri = copyUriToInternalStorage(uri)
            if (savedUri != null) {
                selectedPhotoUri = savedUri
                binding.foodPhotoImageView.setImageURI(savedUri)
            } else {
                Toast.makeText(context, "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun copyUriToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val fileName = "diet_${System.currentTimeMillis()}.jpg"
            val file = java.io.File(requireContext().filesDir, fileName)
            val outputStream = java.io.FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDietRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [추가] 전달받은 데이터가 있으면 수정 모드로 초기화
        arguments?.let { args ->
            if (args.containsKey("id")) {
                editModeId = args.getString("id")
                val foodName = args.getString("foodName")
                val mealType = args.getString("mealType")
                val quantity = args.getDouble("quantity")
                val unit = args.getString("unit")
                val satisfaction = args.getInt("satisfaction")
                val timestamp = args.getLong("timestamp")
                currentPhotoUrl = args.getString("photoUrl")

                binding.foodNameEditText.setText(foodName)
                binding.quantityEditText.setText(quantity.toString())
                binding.unitEditText.setText(unit)
                binding.satisfactionRatingBar.rating = satisfaction.toFloat()
                
                // 스피너 설정
                val mealTypes = resources.getStringArray(com.dataDoctor.rehabai.R.array.meal_types)
                val spinnerIndex = mealTypes.indexOf(mealType)
                if (spinnerIndex >= 0) {
                    binding.mealTypeSpinner.setSelection(spinnerIndex)
                }

                // 날짜 설정
                selectedDate.timeInMillis = timestamp
                
                // 사진 설정 (Glide 필요하지만 여기선 간단히 처리하거나 생략)
                // 실제로는 Glide로 currentPhotoUrl 로드 필요. 
                // 여기서는 텍스트로 대체하거나 생략. (이미지뷰는 로컬 URI만 받도록 되어있음)
            }
        }

        updateDateTimeButtons()
        setupClickListeners()
    }

    private fun updateDateTimeButtons() {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        
        binding.dateButton.text = dateFormat.format(selectedDate.time)
        binding.timeButton.text = timeFormat.format(selectedDate.time)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupClickListeners() {
        binding.selectPhotoButton.setOnClickListener {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        // [추가] 날짜 선택
        binding.dateButton.setOnClickListener {
            val datePicker = android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedDate.set(java.util.Calendar.YEAR, year)
                    selectedDate.set(java.util.Calendar.MONTH, month)
                    selectedDate.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateDateTimeButtons()
                },
                selectedDate.get(java.util.Calendar.YEAR),
                selectedDate.get(java.util.Calendar.MONTH),
                selectedDate.get(java.util.Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // [추가] 시간 선택
        binding.timeButton.setOnClickListener {
            val timePicker = android.app.TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    selectedDate.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedDate.set(java.util.Calendar.MINUTE, minute)
                    updateDateTimeButtons()
                },
                selectedDate.get(java.util.Calendar.HOUR_OF_DAY),
                selectedDate.get(java.util.Calendar.MINUTE),
                false
            )
            timePicker.show()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            saveDietRecord()
        }
    }

    private fun saveDietRecord() {
        val foodName = binding.foodNameEditText.text?.toString()?.trim()
        val mealType = binding.mealTypeSpinner.selectedItem.toString()
        val quantity = binding.quantityEditText.text?.toString()?.toDoubleOrNull() ?: 1.0
        val unit = binding.unitEditText.text?.toString()?.trim() ?: "인분"
        val satisfaction = binding.satisfactionRatingBar.rating.toInt()

        if (foodName.isNullOrBlank()) {
            Toast.makeText(context, "음식 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        // ViewModel을 통해 저장 (수정 모드 지원)
        viewModel.recordDiet(
            id = editModeId, // 수정 시 ID 전달
            foodName = foodName,
            photoUri = selectedPhotoUri,
            photoUrl = currentPhotoUrl, // 기존 URL 전달
            mealType = mealType,
            quantity = quantity,
            unit = unit,
            satisfaction = satisfaction,
            date = selectedDate.time // 선택된 날짜 전달
        )

        val message = if (editModeId != null) "식단이 수정되었습니다" else "식단이 기록되었습니다"
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DietRecordDialog"

        // [추가] 수정 모드용 인스턴스 생성 메서드
        fun newInstance(
            id: String,
            foodName: String,
            mealType: String,
            quantity: Double,
            unit: String,
            satisfaction: Int,
            timestamp: Long,
            photoUrl: String?
        ): DietRecordDialog {
            val fragment = DietRecordDialog()
            val args = Bundle().apply {
                putString("id", id)
                putString("foodName", foodName)
                putString("mealType", mealType)
                putDouble("quantity", quantity)
                putString("unit", unit)
                putInt("satisfaction", satisfaction)
                putLong("timestamp", timestamp)
                putString("photoUrl", photoUrl)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
