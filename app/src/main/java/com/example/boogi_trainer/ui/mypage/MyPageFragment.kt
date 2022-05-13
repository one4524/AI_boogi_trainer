package com.example.boogi_trainer.ui.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.boogi_trainer.R
import com.example.boogi_trainer.data.AppMemberInfo
import com.example.boogi_trainer.data.MemberAns
import com.example.boogi_trainer.data.MemberReq
import com.example.boogi_trainer.data.MyApplication
import com.example.boogi_trainer.databinding.FragmentMypageBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val api = com.example.boogi_trainer.data.APIS.create();

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//
//        val templang = AppMemberInfo("ko", MyApplication.uuid.getUid("uuid","nouid"));
//        val templist = listOf(templang);
//
//
//        val tempbody = MemberReq(templist);
//        api.member_info(MyApplication.prefs.getString("token","notoken"), tempbody).enqueue(object :
//            Callback<MemberAns> {
//            override fun onResponse(call: Call<MemberAns>, response: Response<MemberAns>) {
//                Log.d("log",response.toString())
//                Log.d("log", response.body().toString())
//                if(!response.body().toString().isEmpty()) {
//                    val name : TextView = view!!.findViewById(R.id.user_name) // 얘로 정의
//                    name.setText(response.body()?.result?.name) //setText는 이렇게 사용
//                }
//            }
//
//            override fun onFailure(call: Call<MemberAns>, t: Throwable) {
//                // 실패
//                Log.d("log",t.message.toString())
//                Log.d("log","fail")
//            }
//        })

        val notificationsViewModel =
            ViewModelProvider(this).get(MyPageViewModel::class.java)

        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}