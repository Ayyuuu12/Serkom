package com.example.serkom.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.serkom.data.ItemData
import com.example.serkom.databinding.ItemLayoutBinding

class Adapter(private val itemList: ArrayList<ItemData>) :
    RecyclerView.Adapter<Adapter.ItemHolder>() {
    class ItemHolder(private val binding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ItemData) {
            binding.tvNik.text = item.nik.toString()
            binding.tvName.text = item.name.toString()
            binding.tvPhone.text = item.phone.toString()
            binding.tvGender.text = item.gender.toString()
            binding.tvDate.text = item.date.toString()

            val bytes = android.util.Base64.decode(item.itemImage, android.util.Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.imageDisplay.setImageBitmap(bitmap)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val currentItem = itemList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}