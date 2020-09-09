package com.merpati.durgence.views.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.andresaftari.durgence.R
import com.merpati.durgence.currentLat
import com.merpati.durgence.currentLng
import com.merpati.durgence.model.FirstAid
import com.merpati.durgence.utils.adapter.FirstAidAdapter
import com.merpati.durgence.utils.helper.LocationHelper
import kotlinx.android.synthetic.main.fragment_menu.*

class MenuFragment : Fragment() {
    private val list = ArrayList<FirstAid>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_menu, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pb_aid.visibility = View.VISIBLE
        retainInstance = true

        when (savedInstanceState) {
            null -> {
                list.addAll(getGridMenu())
                populateViews()
            }
            else -> {
                val stateGrid = savedInstanceState.getParcelableArrayList<FirstAid>(STATE_GRID)
                if (stateGrid != null) list.addAll(stateGrid)
            }
        }
        LocationHelper(requireContext()).getLocation(currentLat, currentLng)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(STATE_GRID, list)
    }

    @SuppressLint("Recycle")
    fun getGridMenu(): ArrayList<FirstAid> {
        val dataThumbnail = resources.obtainTypedArray(R.array.aidThumb)
        val dataName = resources.getStringArray(R.array.aidName)

        val aidList = ArrayList<FirstAid>()

        for (position in dataName.indices) {
            val aid = FirstAid(dataThumbnail.getResourceId(position, -1), dataName[position])
            aidList.add(aid)

            Log.i(TAG, "list: $aidList")
        }
        return aidList
    }

    private fun populateViews() {
        try {
            pb_aid.visibility = View.GONE
            rv_aid?.apply {
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(activity, 2)
                adapter = FirstAidAdapter(list)
            }
        } catch (e: Exception) {
            Log.i(TAG, "${e.message} --- ${e.printStackTrace()}")
        } catch (e: KotlinNullPointerException) {
            Log.i(TAG, "${e.message} --- ${e.printStackTrace()}")
        }
    }

    companion object {
        private const val STATE_GRID = "state_grid"
        private const val TAG = "MenuFragment"
    }
}