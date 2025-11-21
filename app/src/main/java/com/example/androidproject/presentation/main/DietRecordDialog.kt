package com.example.androidproject.presentation.main

import android.app.Dialog
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
import com.example.androidproject.databinding.DialogDietRecordBinding
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DietRecordDialog : DialogFragment() {

    private var _binding: DialogDietRecordBinding? = null
    private val binding get() = _binding!!

    // [수정] activityViewModels()로 변경하여 HomeFragment와 같은 ViewModel 인스턴스 공유
    private val viewModel: RehabViewModel by activityViewModels()

    private var selectedPhotoUri: Uri? = null

    // 사진 선택기
    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
            binding.foodPhotoImageView.setImageURI(uri)
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

        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()
        // 다이얼로그 크기 조정
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

        // ViewModel을 통해 저장
        viewModel.recordDiet(
            foodName = foodName,
            photoUri = selectedPhotoUri,
            mealType = mealType,
            quantity = quantity,
            unit = unit,
            satisfaction = satisfaction
        )

        Toast.makeText(context, "식단이 기록되었습니다", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DietRecordDialog"
    }
}
