package com.unibo.android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unibo.android.domain.models.AccommodationType
import com.unibo.android.ui.R
import com.unibo.android.ui.databinding.ItemListHotelBinding

class AccomodationAdapter(
    private val dataSet: MutableList<AccommodationType>
): RecyclerView.Adapter<AccomodationAdapter.ViewHolder>() {

    //Versione ViewHolder classica
//    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
//        val txtAccomodationType = view.findViewById<TextView>(R.id.txtAccomodationType)
//        val txtAccomodationName = view.findViewById<TextView>(R.id.txtAccomodationName)
//        val accomodationImage = view.findViewById<ImageView>(R.id.accomodationImage)
//        val txtAccomodationDescription = view.findViewById<TextView>(R.id.txtAccomodationDescription)
//        val txtScore = view.findViewById<TextView>(R.id.txtScore)
//    }

    fun updateList(
        newList: List<AccommodationType>
    ) {
        dataSet.clear()
        dataSet.addAll(newList)
        notifyDataSetChanged()
    }

    //Versione ViewHolder con ViewBinding
    class ViewHolder(
        private val binding: ItemListHotelBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(uiAccomodationType: AccommodationType) {
            binding.txtAccomodationType.text = when(uiAccomodationType) {
                is AccommodationType.Hotel -> binding.root.context.getString(R.string.hotel)
                is AccommodationType.Apartment -> binding.root.context.getString(R.string.apartment)
            }
            binding.txtAccomodationName.text = uiAccomodationType.name
            binding.txtAccomodationDescription.text = uiAccomodationType.description
            binding.txtScore.text = uiAccomodationType.score.toString()

            when {
                uiAccomodationType.score > 8.0 -> binding.txtScore.setTextColor(itemView.context.getColor(R.color.green))
                else -> binding.txtScore.setTextColor(itemView.context.getColor(R.color.red))
            }

            Glide.with(binding.root.context)
                .load(uiAccomodationType.pictureUrl)
                .into(binding.accomodationImage)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        //VERSIONE View Binding
        val binding = ItemListHotelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)

        //VERSIONE ViewHolder classica
//        val view = LayoutInflater.from(
//            parent.context
//        ).inflate(
//            R.layout.item_list_hotel,
//            parent,
//            false
//        )
//        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val accomodation = dataSet[position]

        //VERSIONE View Binding
        holder.bind(accomodation)


        //VERSIONE ViewHolder classica
//        holder.txtAccomodationType.text = when(accomodation) {
//            is AccommodationType.Hotel -> holder.itemView.context.getString(R.string.hotel)
//            is AccommodationType.Apartment -> holder.itemView.context.getString(R.string.apartment)
//        }
//        holder.txtAccomodationName.text = accomodation.name
//        holder.txtAccomodationDescription.text = accomodation.description
//        holder.txtScore.text = accomodation.score.toString()
//
//        when {
//            accomodation.score > 8.0 -> holder.txtScore.setTextColor(holder.itemView.context.getColor(R.color.green))
//            else -> holder.txtScore.setTextColor(holder.itemView.context.getColor(R.color.red))
//        }
//
//        Glide.with(holder.itemView.context)
//            .load(accomodation.pictureUrl)
//            .into(holder.accomodationImage)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}