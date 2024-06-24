package com.example.diplom.adapter

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.diplom.DatabaseHelper
import com.example.diplom.databinding.ItemBankcardLayoutBinding
import com.example.diplom.db.UserManager

class BankCardAdapter(private val context: Context, private val bankcardList:MutableList<BankCard>):RecyclerView.Adapter<BankCardAdapter.BankCardViewHolder>() {

    inner class BankCardViewHolder(private val binding: ItemBankcardLayoutBinding) : RecyclerView.ViewHolder(binding.root){
        val streetTextView = binding.streetTextView
        val houseTextView = binding.houseTextView
        val flatTextView = binding.flatTextView
        val deleteButton = binding.deleteBankCardButton

        init {
            deleteButton.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION){

                    deleteBankCard(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankCardViewHolder {
        val binding = ItemBankcardLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BankCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BankCardViewHolder, position: Int) {
        val bankCard = bankcardList[position]
        Log.d(TAG, "onBindViewHolder() вызван для позиции $position")
        holder.streetTextView.text = bankCard.Street
        holder.houseTextView.text = "дом ${bankCard.House}"
        holder.flatTextView.text = "квартира ${bankCard.Flat}"

    }

    override fun getItemCount(): Int {
        return bankcardList.size
    }

    private fun deleteBankCard(position: Int) {
        val bankCardToDelete = bankcardList[position]

        try {
            val dbHelper = DatabaseHelper(context)
            val database = dbHelper.writableDatabase

            // Удаляем запись из таблицы ClientsBankCard
            val whereClauseClientsBankCard = "DeliveryAddresses_id = ? AND Clients_id = ?"
            val whereArgsClientsBankCard = arrayOf(bankCardToDelete.Id.toString(), UserManager.userId.toString()) // Предполагается, что у вашего объекта BankCard есть поле ID
            val deletedRowsClientsBankCard = database.delete("ClientsDeliveryAddresses", whereClauseClientsBankCard, whereArgsClientsBankCard)

            if (deletedRowsClientsBankCard > 0) {
                // Если запись успешно удалена из ClientsBankCard, удаляем ее из таблицы BankCard
                val whereClauseBankCards = "ID = ?"
                val whereArgsBankCards = arrayOf(bankCardToDelete.Id.toString())
                val deletedRowsBankCards = database.delete("DeliveryAdresses", whereClauseBankCards, whereArgsBankCards)

                if (deletedRowsBankCards > 0) {
                    // Если удалось удалить из обеих таблиц, удаляем элемент из списка и уведомляем адаптер
                    bankcardList.removeAt(position)
                    notifyItemRemoved(position)
                } else {
                    // Если не удалось удалить из таблицы BankCard, восстанавливаем запись в таблице ClientsBankCard
                    database.insert("ClientsDeliveryAddresses", null, ContentValues().apply {
                        put("DeliveryAddresses_id", bankCardToDelete.Id)
                        put("Clients_id", UserManager.userId)
                    })
                    Toast.makeText(context, "Ошибка при удалении банковской карты", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Если не удалось удалить запись из ClientsBankCard, выводим сообщение об ошибке
                Toast.makeText(context, "Ошибка при удалении банковской карты", Toast.LENGTH_SHORT).show()
            }

            database.close()
        } catch (e: Exception) {
            // В случае ошибки выводим сообщение об ошибке
            Log.e(TAG, "Ошибка при удалении банковской карты из базы данных: ${e.message}")
            Toast.makeText(context, "Ошибка при удалении банковской карты", Toast.LENGTH_SHORT).show()
        }
    }
}