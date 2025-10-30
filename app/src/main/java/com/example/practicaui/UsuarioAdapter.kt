package com.example.practicaui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.practicaui.model.UsuarioEntity

class UsuarioAdapter(
    private val onVerEnMapaClick: (UsuarioEntity) -> Unit
) : ListAdapter<UsuarioEntity, UsuarioAdapter.VH>(Diff()) {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFoto: ImageView = itemView.findViewById(R.id.imgItemFoto)
        val txtNombre: TextView = itemView.findViewById(R.id.txtItemNombre)
        val txtDetalle: TextView = itemView.findViewById(R.id.txtItemDetalle)
        val btnVerMapa: Button = itemView.findViewById(R.id.btnVerMapa)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val u = getItem(position)
        holder.txtNombre.text = u.nombre
        holder.txtDetalle.text = "Género: ${u.genero} | " +
                "Notificaciones: ${if (u.notificaciones) "Sí" else "No"} | " +
                "Estado: ${u.estado} | " +
                "Lat: ${u.latitud?.toString() ?: "N/D"} | Lng:${u.longitud?.toString() ?: "N/D"}"

        val fuente = u.fotoRemotaUrl ?: u.fotoLocalUri
        holder.imgFoto.load(fuente)

        holder.btnVerMapa.setOnClickListener { onVerEnMapaClick(u) }
    }

    private class Diff : DiffUtil.ItemCallback<UsuarioEntity>() {
        override fun areItemsTheSame(oldItem: UsuarioEntity, newItem: UsuarioEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: UsuarioEntity, newItem: UsuarioEntity) = oldItem == newItem
    }
}