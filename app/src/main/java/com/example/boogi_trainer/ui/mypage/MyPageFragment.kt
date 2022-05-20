package com.example.boogi_trainer.ui.mypage

import android.annotation.SuppressLint
import android.content.Context.MODE_NO_LOCALIZED_COLLATORS
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.boogi_trainer.MainActivity
import com.example.boogi_trainer.R
import com.example.boogi_trainer.data.AppMemberInfo
import com.example.boogi_trainer.data.MemberAns
import com.example.boogi_trainer.data.MemberReq
import com.example.boogi_trainer.data.MyApplication
import com.example.boogi_trainer.databinding.FragmentMypageBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.FileInputStream
import java.io.FileOutputStream

class MyPageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var userID: String = "userID"
    lateinit var fname: String
    lateinit var str: String
    val api = com.example.boogi_trainer.data.APIS.create();

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
"""
        val templang = AppMemberInfo("ko", MyApplication.uuid.getUid("uuid","nouid"));
        val templist = listOf(templang);


        val tempbody = MemberReq(templist);
        api.member_info(MyApplication.prefs.getString("token","notoken"), tempbody).enqueue(object :
            Callback<MemberAns> {
            
            override fun onResponse(call: Call<MemberAns>, response: Response<MemberAns>) {
                Log.d("log",response.toString())
                Log.d("log", response.body().toString())
                if(!response.body().toString().isEmpty()) {
                    val name : TextView = view!!.findViewById(R.id.user_name) // 얘로 정의
                    name.setText(response.body()?.result?.name) //setText는 이렇게 사용
                }
            }

            override fun onFailure(call: Call<MemberAns>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })

"""






        val notificationsViewModel =
            ViewModelProvider(this).get(MyPageViewModel::class.java)

        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.calendarView.setOnDateChangeListener{view, year, month, dayOfMonth ->
            binding.diaryTextView.visibility = View.VISIBLE
            binding.saveBtn.visibility = View.VISIBLE
            binding.contextEditText.visibility = View.VISIBLE
            binding.diaryContent.visibility = View.INVISIBLE
            binding.updateBtn.visibility = View.INVISIBLE
            binding.deleteBtn.visibility = View.INVISIBLE
            binding.diaryTextView.text = String.format("%d / %d / %d", year, month + 1, dayOfMonth)
            binding.contextEditText.setText("")
            checkDay(year, month, dayOfMonth, userID)
        }

        binding.saveBtn.setOnClickListener {
            saveDiary(fname)
            binding.contextEditText.visibility = View.INVISIBLE
            binding.saveBtn.visibility = View.INVISIBLE
            binding.updateBtn.visibility = View.VISIBLE
            binding.deleteBtn.visibility = View.VISIBLE
            str = binding.contextEditText.text.toString()
            binding.diaryContent.text = str
            binding.diaryContent.visibility = View.VISIBLE
        }

        return root


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun checkDay(cYear: Int, cMonth: Int, cDay: Int, userID: String) {
        //저장할 파일 이름설정
        fname = "" + userID + cYear + "-" + (cMonth + 1) + "" + "-" + cDay + ".txt"

        var fileInputStream: FileInputStream
        try {
            fileInputStream = requireActivity().openFileInput(fname)
            val fileData = ByteArray(fileInputStream.available())
            fileInputStream.read(fileData)
            fileInputStream.close()
            str = String(fileData)
            binding.contextEditText.visibility = View.INVISIBLE
            binding.diaryContent.visibility = View.VISIBLE
            binding.diaryContent.text = str
            binding.saveBtn.visibility = View.INVISIBLE
            binding.updateBtn.visibility = View.VISIBLE
            binding.deleteBtn.visibility = View.VISIBLE
            binding.updateBtn.setOnClickListener {
                binding.contextEditText.visibility = View.VISIBLE
                binding.diaryContent.visibility = View.INVISIBLE
                binding.contextEditText.setText(str)
                binding.saveBtn.visibility = View.VISIBLE
                binding.updateBtn.visibility = View.INVISIBLE
                binding.deleteBtn.visibility = View.INVISIBLE
                binding.diaryContent.text = binding.contextEditText.text
            }
            binding.deleteBtn.setOnClickListener {
                binding.diaryContent.visibility = View.INVISIBLE
                binding.updateBtn.visibility = View.INVISIBLE
                binding.deleteBtn.visibility = View.INVISIBLE
                binding.contextEditText.setText("")
                binding.contextEditText.visibility = View.VISIBLE
                binding.saveBtn.visibility = View.VISIBLE
                removeDiary(fname)
            }
            if (binding.diaryContent.text == null) {
                binding.diaryContent.visibility = View.INVISIBLE
                binding.updateBtn.visibility = View.INVISIBLE
                binding.deleteBtn.visibility = View.INVISIBLE
                binding.diaryTextView.visibility = View.VISIBLE
                binding.saveBtn.visibility = View.VISIBLE
                binding.contextEditText.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    //달력 내용 제거
    fun removeDiary(readDay: String?) {
        var fileOutputStream: FileOutputStream
        try {
            fileOutputStream = requireActivity().openFileOutput(readDay, MODE_NO_LOCALIZED_COLLATORS)
            val content = ""
            fileOutputStream.write(content.toByteArray())
            fileOutputStream.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    // 달력 내용 추가
    fun saveDiary(readDay: String?) {
        var fileOutputStream: FileOutputStream
        try {
            fileOutputStream = requireActivity().openFileOutput(readDay, MODE_NO_LOCALIZED_COLLATORS)
            val content = binding.contextEditText.text.toString()
            fileOutputStream.write(content.toByteArray())
            fileOutputStream.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}