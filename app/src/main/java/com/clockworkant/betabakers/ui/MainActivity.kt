package com.clockworkant.betabakers.ui

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.clockworkant.betabakers.R
import com.clockworkant.betabakers.data.Cake
import kotlinx.android.synthetic.main.activity_cake_list.*
import kotlinx.android.synthetic.main.cake_item.view.*
import org.koin.android.ext.android.get
import android.support.v7.widget.DividerItemDecoration


class MainActivity : AppCompatActivity(), CakeListView {

    private val cakeAdapter: CakeAdapter =
        CakeAdapter(callback = this::onCakeClicked)

    private lateinit var cakeListPresenter: CakeListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cake_list)
        setSupportActionBar(toolbar)

        cakes_list.apply {
            val linearLayoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
            layoutManager = linearLayoutManager
            adapter = cakeAdapter
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    linearLayoutManager.orientation
                )
            )
        }

        cakes_list_refreshlayout.setOnRefreshListener { cakeListPresenter.onRefresh() }

        cakeListPresenter = CakeListPresenter(this, get())
        cakeListPresenter.onAppLoaded()
    }

    override fun onDestroy() {
        super.onDestroy()
        cakeListPresenter.onTerminated()
    }

    private fun onCakeClicked(cake: Cake) {
        AlertDialog.Builder(this)
            .setTitle(cake.title)
            .setMessage(cake.desc)
            .setPositiveButton("Ok", null)
            .create()
            .show()
    }

    override fun showCakes(cakes: List<Cake>) {
        cakeAdapter.setCakes(cakes)
    }

    override fun showLoading(isLoading: Boolean) {
        cakes_list_refreshlayout.isRefreshing = isLoading
    }

    override fun showError(error: Throwable) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage("We're sorry but an error has occured, would you like to try and retrieve the cakes again?")
            .setPositiveButton("Refresh") { dialog, which -> cakeListPresenter.onRefresh()}
            .setNeutralButton("Back", null)
            .create()
            .show()
    }
}

private class CakeAdapter(
    private val cakes: MutableList<Cake> = mutableListOf(),
    private val callback: (cake: Cake) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cake_item, parent, false)
        return ViewHolder(view, callback)
    }

    override fun getItemCount(): Int = cakes.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(cakes[position])
    }

    fun setCakes(newCakes: List<Cake>) {
        cakes.clear()
        cakes.addAll(newCakes)
        notifyDataSetChanged()
    }

}

private class ViewHolder(
    private val view: View,
    private val callback: (cake: Cake) -> Unit
) : RecyclerView.ViewHolder(view) {
    fun bind(cake: Cake) {
        view.cake_item_name.text = cake.title

        bindCakeImage(cake)

        view.setOnClickListener {
            callback.invoke(cake)
        }
    }

    private fun bindCakeImage(cake: Cake) {
        Glide.with(view)
            .load(cake.image)
            .centerCrop()
            .into(view.cake_item_imageview)
    }
}