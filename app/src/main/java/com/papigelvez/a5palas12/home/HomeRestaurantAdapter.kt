package com.papigelvez.a5palas12.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.papigelvez.a5palas12.R
import com.papigelvez.a5palas12.entities.RestaurantEntity

class HomeRestaurantAdapter(private val restaurantList: MutableList<RestaurantEntity>) :
    RecyclerView.Adapter<HomeRestaurantAdapter.RestaurantViewHolder>() {

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.restaurantName)
        val categoryTextView: TextView = itemView.findViewById(R.id.restaurantCategories)
        val ratingTextView: TextView = itemView.findViewById(R.id.restaurantRating)
    }

    //referenciar el layout de cada item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.restaurant_item, parent, false)
        return RestaurantViewHolder(view)
    }

    //mostrar data en la posicion especificada
    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurantList[position]
        holder.nameTextView.text = restaurant.name
        var categories = restaurant.categories.toString()
        categories = categories.replace("[", "")
        categories = categories.replace("]", "")
        categories = categories.replace(",", " -")
        holder.categoryTextView.text = categories
        holder.ratingTextView.text = restaurant.rating.toString()
    }

    override fun getItemCount(): Int {
        return restaurantList.size
    }


    fun updateRestaurants(newRestaurants: List<RestaurantEntity>) {
        restaurantList.clear()
        restaurantList.addAll(newRestaurants)
        notifyDataSetChanged()
    }


}