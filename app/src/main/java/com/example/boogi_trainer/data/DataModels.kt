package com.example.boogi_trainer.data

data class AppMemberInfo(
    val lang: String,
    val uuid: String
)

data class MemberReq(
    val AppMemberInfo: List<AppMemberInfo>
)

data class Result(
    val uid: String,
    val password: String,
    val name: String,
    val body_form: String,
    val weight: Int,
    val height: Int
)


data class MemberAns(
    val code: Int,
    val message: String,
    val result: Result
)