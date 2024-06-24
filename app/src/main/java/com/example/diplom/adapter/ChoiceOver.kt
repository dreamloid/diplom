package com.example.diplom.adapter

import com.example.diplom.R

data class ChoiceOver(val image_Id: Int, val title: String)

class OverList {val list = arrayListOf(ChoiceOver(R.drawable.ic_phone2, "Позвонить мне. Подобрать замену"),
    ChoiceOver(R.drawable.ic_phone2, "Не звонить. Убрать товар из заказа"))}
