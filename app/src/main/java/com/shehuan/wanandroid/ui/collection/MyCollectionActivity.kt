package com.shehuan.wanandroid.ui.collection

import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import com.shehuan.wanandroid.R
import com.shehuan.wanandroid.adapter.CollectionListAdapter
import com.shehuan.wanandroid.base.activity.BaseMvpActivity
import com.shehuan.wanandroid.base.net.exception.ResponseException
import com.shehuan.wanandroid.bean.article.ArticleBean
import com.shehuan.wanandroid.ui.article.ArticleActivity
import com.shehuan.wanandroid.utils.ToastUtil
import com.shehuan.wanandroid.widget.DivideItemDecoration
import com.shehuan.wanandroid.widget.WrapLinearLayoutManager
import kotlinx.android.synthetic.main.activity_my_collection.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class MyCollectionActivity : BaseMvpActivity<MyCollectionPresenterImpl>(), MyCollectionContract.View {
    private var pageNum: Int = 0
    private lateinit var collectionListAdapter: CollectionListAdapter
    private var collectPosition: Int = 0

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MyCollectionActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initPresenter(): MyCollectionPresenterImpl {
        return MyCollectionPresenterImpl(this)
    }

    override fun loadData() {
        statusView.showLoadingView()
        presenter.getCollectionList(pageNum)
    }

    override fun initLayoutResID(): Int {
        return R.layout.activity_my_collection
    }

    override fun initData() {

    }

    override fun initView() {
        initToolbar("收藏")

        collectionListAdapter = CollectionListAdapter(mContext, null, true)
        collectionListAdapter.setLoadingView(R.layout.rv_loading_layout)
        collectionListAdapter.setLoadEndView(R.layout.rv_load_end_layout)
        collectionListAdapter.setLoadFailedView(R.layout.rv_load_failed_layout)

        collectionListAdapter.setOnItemClickListener { _, data, _ ->
            ArticleActivity.start(mContext, data.title, data.link)
        }
        collectionListAdapter.setOnItemChildClickListener(R.id.articleCollectIv) { _, data, position ->
            collectPosition = position
            presenter.cancelCollection(data.id, data.originId)

        }
        collectionListAdapter.setOnLoadMoreListener {
            presenter.getCollectionList(pageNum)
        }
        val linearLayoutManager = WrapLinearLayoutManager(mContext)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        collectionRv.layoutManager = linearLayoutManager
        collectionRv.addItemDecoration(DivideItemDecoration())
        collectionRv.adapter = collectionListAdapter

        statusView = initStatusView(R.id.collectionRv) {
            loadData()
        }
    }

    override fun onCollectionListSuccess(data: ArticleBean) {
        if (pageNum == 0) {
            statusView.showContentView()
            collectionListAdapter.setNewData(data.datas)
        } else {
            collectionListAdapter.setLoadMoreData(data.datas)
        }
        pageNum++
        if (pageNum == data.pageCount) {
            collectionListAdapter.loadEnd()
            return
        }
    }

    override fun onCollectionListError(e: ResponseException) {
        if (pageNum == 0) {
            statusView.showErrorView()
        } else {
            collectionListAdapter.loadFailed()
        }
    }

    override fun onCancelCollectionSuccess(data: String) {
        collectionListAdapter.remove(collectPosition)
        ToastUtil.showToast(mContext, "取消收藏成功")
    }

    override fun onCancelCollectionError(e: ResponseException) {

    }
}
