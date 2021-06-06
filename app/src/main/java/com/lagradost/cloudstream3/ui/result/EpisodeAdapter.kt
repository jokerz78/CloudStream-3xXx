package com.lagradost.cloudstream3.ui.result

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lagradost.cloudstream3.*
import kotlinx.android.synthetic.main.result_episode.view.*

const val ACTION_PLAY_EPISODE = 1
const val ACTION_RELOAD_EPISODE = 2

data class EpisodeClickEvent(val action: Int, val data: ResultEpisode)

class EpisodeAdapter(
    private var activity: Activity,
    var cardList: ArrayList<ResultEpisode>,
    val resView: RecyclerView,
    val clickCallback: (EpisodeClickEvent) -> Unit,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CardViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.result_episode, parent, false),
            activity,
            resView,
            clickCallback
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CardViewHolder -> {
                holder.bind(cardList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    class CardViewHolder
    constructor(
        itemView: View,
        val activity: Activity,
        resView: RecyclerView,
        private val clickCallback: (EpisodeClickEvent) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {
        private val episodeViewPrecentage: View = itemView.episode_view_procentage
        private val episodeViewPercentageOff: View = itemView.episode_view_procentage_off
        private val episodeText: TextView = itemView.episode_text
        val episodeExtra: ImageView = itemView.episode_extra
        private val episodePlay: ImageView = itemView.episode_play
        private val episodeHolder = itemView.episode_holder

        fun bind(card: ResultEpisode) {
            episodeText.text = card.name ?: "Episode ${card.episode}"

            fun setWidth(v: View, procentage: Float) {
                val param = LinearLayout.LayoutParams(
                    v.layoutParams.width,
                    v.layoutParams.height,
                    procentage
                )
                v.layoutParams = param
            }
            setWidth(episodeViewPrecentage, card.watchProgress)
            setWidth(episodeViewPercentageOff, 1 - card.watchProgress)

            episodeHolder.setOnClickListener {
                clickCallback.invoke(EpisodeClickEvent(ACTION_PLAY_EPISODE, card))
            }
        }
    }
}
