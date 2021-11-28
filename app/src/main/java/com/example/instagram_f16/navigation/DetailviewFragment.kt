package com.example.instagram_f16.navigation

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.instagram_f16.R
import com.example.instagram_f16.navigation.model.AlarmDTO
import com.example.instagram_f16.navigation.model.ContentDTO

import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.item_detail.*
import kotlinx.android.synthetic.main.item_detail.view.*
import java.util.ArrayList
class DetailviewFragment : Fragment() {
    var firestore: FirebaseFirestore? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firestore = FirebaseFirestore.getInstance()
        var view = LayoutInflater.from(inflater.context)
            .inflate(R.layout.fragment_detail, container, false)
        view.detailviewfragment_recyclerview.adapter = DetailRecyclerviewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class DetailRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val contentDTOs: ArrayList<ContentDTO>
        val contentUidList: ArrayList<String>

        init {
            contentDTOs = ArrayList()
            contentUidList = ArrayList()
            //현재 로그인된 유저의 UID(주민등록번호)
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    if (querySnapshot == null) return@addSnapshotListener
                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = (holder as CustomViewHolder).itemView
            //유저 아이디
            viewHolder.detailviewitem_profile_textview.text = contentDTOs!![position].userId
            //이미지
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl)
                .into(viewHolder.detailviewitem_imageview_content)
            //설명 텍스트
            viewHolder.detailviewitem_explain_textview.text = contentDTOs!![position].explain

            //좋아요 카운터 설정
            viewHolder.detailviewitem_favoritecounter_textview.text =
                "좋아요 " + contentDTOs!![position].favoriteCount + "개"
            var uid = FirebaseAuth.getInstance().currentUser!!.uid
            viewHolder.detailviewitem_favorite_imageview.setOnClickListener {
                favoriteEvent(position)
            }
            //좋아요를 클릭
            if (contentDTOs!![position].favorites.containsKey(uid)) {
                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)
                //클릭하지 않았을 경우
            } else {
                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }
            //프로필 이미지를 누르면 상대 프로필로 이동
            viewHolder.detailviewitem_profile_image.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[position].uid)
                bundle.putString("userId", contentDTOs[position].userId)
                fragment.arguments = bundle
                activity!!.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.main_content, fragment)?.commit()

            }

                viewHolder.detailviewitem_comment_imageview.setOnClickListener { v ->
                    var intent = Intent(v.context, CommentActivity::class.java)
                    intent.putExtra("contentUid", contentUidList[position])
                    intent.putExtra("destinationUid",contentDTOs[position].uid)
                    startActivity(intent)
                }
            }

            private fun favoriteEvent(position: Int) {
                var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
                firestore?.runTransaction { transaction ->
                    var uid = FirebaseAuth.getInstance().currentUser!!.uid
                    var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                    if (contentDTO!!.favorites.containsKey(uid)) {
                        //좋아요를 누른상태
                        contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                        contentDTO?.favorites.remove(uid)


                    } else {
                        //좋아요를 누르지 않은 상태
                        contentDTO?.favorites[uid] = true
                        contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                        favoriteAlarm(contentDTOs[position].uid!!)

                    }
                    transaction.set(tsDoc, contentDTO)
                }

            }
        fun favoriteAlarm(destinationUid : String){
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        }
    }

}
