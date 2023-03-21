package com.jenny.deara.record

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.jenny.deara.R
import com.jenny.deara.databinding.ActivityAddRecordBinding
import com.jenny.deara.databinding.ActivityRmRecordBinding
import com.jenny.deara.utils.FBAuth
import com.jenny.deara.utils.FBRef
import java.util.*

class EditRecordActivity : AppCompatActivity() {

    val binding by lazy { ActivityRmRecordBinding.inflate(layoutInflater) }

    lateinit var pillListAdapter: PillListAdapter

    // 복용 약 리싸이클러뷰
    val pillList = mutableListOf<pillData>()
    val pillkeyList = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initRecycler()
        getPillData()

        binding.root.setOnClickListener {
            hideKeyboard()
        }

        binding.linearlayout.setOnClickListener {
            hideKeyboard()
        }

        // 이미 저장된 상세내역 파이어베이스에서 불러오기
        val key = intent.getStringExtra("key").toString()
        getRecordData(key)

        // 날짜 선택
        binding.reDateBtn.setOnClickListener {
            chooseDate()
        }

        binding.reTimeBtn.setOnClickListener {
            choosetime()
        }

        // 뒤로가기
        binding.backBtn.setOnClickListener {
            finish()
        }

        // 변경된 내용으로 수정
        binding.saveBtn.setOnClickListener {
            editRecord(key)
        }

        // 아이템 삭제
        binding.removeBtn.setOnClickListener {
            removeRecord(key)
        }


//         복용 약 추가
        binding.reAddPillBtn.setOnClickListener{

            // 내용 존재할때만 추가 가능
            if(binding.pillName.text.toString().trim().isEmpty() || binding.dosage.text.toString().trim().isEmpty()){

            }else{
                // 파이어베이스에 복용약 내용 따로 저장
                saveFBPillData()
                pillListAdapter.notifyDataSetChanged()
            }
        }

    }


    // 날짜 - Datepicekr
    private fun chooseDate() {
        var dateString = ""

        val cal = Calendar.getInstance()    //캘린더뷰 만들기
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            dateString = "${year}년 ${month + 1}월 ${dayOfMonth}일"
            binding.reDate.text = dateString
        }
        DatePickerDialog(
            this,
            dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()

    }

    // 시간 - TimePicker
    private fun choosetime() {

        var timeString = ""

        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            timeString = "${hourOfDay}시 ${minute}분"
            binding.reTime.text = timeString
        }
        TimePickerDialog(
            this,
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }


    private fun getRecordData(key: String) {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val dataModel = dataSnapshot.getValue(RecordData::class.java)

                binding.reHospitalName.setText(dataModel?.hospitalName)
                binding.reDate.setText(dataModel?.date)
                binding.reTime.setText(dataModel?.time)
//                binding.rePillName.setText(dataModel?.pillName)
//                binding.reDosage.setText(dataModel?.dosage)
                binding.reMemo.setText(dataModel?.memo)
                binding.reSymptom.setText(dataModel?.symptom)

                // 파이어베이스에서 기존 복용 약 데이터 불러오기
                // recyclerview 데이터 리스트에 저장
//                pillList.add(pillData(pillNameTxt,dosageTxt))
                // 복용 약 여러개여서 루프 돌아야하나? 진료기록 리스트 불러올때는 어떠헥 했지?
//                var
//                pillList.add()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("recordEdit", "loadPost:onCancelled", databaseError.toException())
            }

        }

        FBRef.recordRef.child(key).addValueEventListener(postListener)

    }

    private fun editRecord(key : String){

        // 아이템 키값으로 구별
        FBRef.recordRef.child(key).setValue(
            RecordData(
                binding.reHospitalName.text.toString(),
                binding.reDate.text.toString(),
                binding.reTime.text.toString(),
//                binding.rePillName.text.toString(),
//                binding.reDosage.text.toString(),
                binding.reMemo.text.toString(),
                binding.reSymptom.text.toString(),
                FBAuth.getUid()
                )
        )

        Toast.makeText(this,"수정완료", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun removeRecord(key : String){
        FBRef.recordRef.child(key).removeValue()
        Toast.makeText(baseContext,"삭제가 완료되었습니다.",Toast.LENGTH_LONG).show()

        finish()
    }


    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun hideKeyboard() {
        if(this != null && this.currentFocus != null) {
            val inputManager: InputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    // 복용 약 RecyclerView 띄우기
    private fun initRecycler(){

        pillListAdapter = PillListAdapter(baseContext,pillList)

        val rv : RecyclerView = binding.pillLayout
        rv.adapter = pillListAdapter

        pillListAdapter.pills = pillList
        pillListAdapter.notifyDataSetChanged()
    }

    private fun saveFBPillData(){

        var pillNameTxt : String = binding.pillName.text.toString()
        var dosageTxt : String = binding.dosage.text.toString()

        var key = FBRef.pillRef.push().key.toString()
        val uid = FBAuth.getUid()

        // 복용 약 객체 형태로 저장
        FBRef.pillRef
            .child(key)
            .setValue(pillData(pillNameTxt,dosageTxt,uid))

        // recyclerview 데이터 리스트에 저장
        pillList.add(pillData(pillNameTxt,dosageTxt))

        // 저장 후에 editTextView 빈칸으로 비우기
        binding.pillName.setText("")
        binding.dosage.setText("")
    }

    private fun getPillData(){

//        FBRef.recordRef.child(key).setValue

        // 데이터 불러오기
        val postListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                pillList.clear()

                var key2 = FBRef.pillRef.key.toString()

                for (dataModel in dataSnapshot.children){
                    // 리싸이클러뷰 데이터에 항목 세개만 넣어서 추가하기
                    val item = dataModel.getValue(pillData::class.java)

                    if (item != null) {
                        // uid 에 맞는 진료기록들을 불러오기
                        // uid 안에서 해당 진료일정 항목 key와도 일치해야함 -> 이게 안되고있다!
                        if (FBAuth.getUid() == item.uid) {
                            if(key2 == dataSnapshot.key)
                            pillList.add(item)
                            Log.d("itemmmmm", item.toString())
//                            recordkeyList.add(dataModel.key.toString())

                        }

                    }
                }
                pillList.reverse()
                pillListAdapter.notifyDataSetChanged()

//                recordkeyList.reverse()

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        FBRef.pillRef.addValueEventListener(postListener)
    }
}