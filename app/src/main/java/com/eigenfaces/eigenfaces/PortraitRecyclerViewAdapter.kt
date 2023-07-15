package com.eigenfaces.eigenfaces

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eigenfaces.eigenfaces.db.Portrait
import java.io.File

class PortraitRecyclerViewAdapter() : RecyclerView.Adapter<PortraitViewHolder>() {

    private val portraitList = ArrayList<Portrait>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortraitViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.list_item, parent, false)
        return PortraitViewHolder(listItem)
    }

    override fun getItemCount(): Int {
        return portraitList.size
    }

    override fun onBindViewHolder(holder: PortraitViewHolder, position: Int) {
        holder.bind(portraitList[position])
    }

    fun setList(portraits : List<Portrait>) {
        portraitList.clear()
        portraitList.addAll(portraits)
    }

}

class PortraitViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    fun bind(portrait : Portrait) {
        val ivPortrait = view.findViewById<ImageView>(R.id.ivFace)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val file = File(view.context.filesDir.path, portrait.fileName)
        Log.i("RECYCLER_TAG", file.path)
        val bitmap = BitmapFactory.decodeFile(file.path)
        ivPortrait.setImageBitmap(bitmap)
        tvName.text = portrait.name
    }
}